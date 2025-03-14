package io.github.sweatunipd;

import io.github.sweatunipd.entity.GPSData;
import io.github.sweatunipd.entity.PointOfInterest;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class NearestPOIRequest extends RichAsyncFunction<GPSData, Tuple2<UUID, PointOfInterest>> {

  private static final Logger LOG = LoggerFactory.getLogger(NearestPOIRequest.class);
  private transient Connection connection;

  /**
   * Initialization method called before the trigger of the async operation
   *
   * @param openContext The context containing information about the context in which the function
   *     is opened.
   * @throws SQLException exception that occurs if the connection to the DB fails
   */
  @Override
  public void open(OpenContext openContext) throws SQLException {
    Properties props = new Properties();
    props.setProperty("user", "admin");
    props.setProperty("password", "adminadminadmin");
    connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/admin", props);
  }

  /** Method that closes the async request */
  @Override
  public void close() {
    if (connection != null) {
      try {
        connection.close();
      } catch (SQLException e) {
        LOG.error(e.getMessage(), e);
      }
    }
  }

  /**
   * Method that triggers the async operation for each element of the stream
   *
   * @param gpsData position emitted by one of the users
   * @param resultFuture Future of the result of the processing; tuple of two element that includes
   *     the ID of the rent and the interested POI saved in his specific POJO
   */
  @Override
  public void asyncInvoke(
      GPSData gpsData, ResultFuture<Tuple2<UUID, PointOfInterest>> resultFuture) {
    CompletableFuture.supplyAsync(
            () -> {
              String sql =
                  "SELECT * FROM points_of_interest AS p JOIN poi_hours ON (p.id = poi_hours.poi_id) "
                      + "WHERE ST_DWithin(ST_Transform(ST_SetSRID(ST_MakePoint(?,?),4326), 3857), "
                      + "ST_Transform(ST_SetSRID(ST_MakePoint(p.latitude,p.longitude),4326), 3857), ?) AND "
                      + "p.id NOT IN (SELECT poi_id FROM advertisements WHERE rent_id=?::uuid) AND "
                      + "p.category IN (SELECT category FROM user_interests JOIN rents ON (user_interests.user_id = rents.user_id) WHERE rents.id=?::UUID) AND "
                      + "? BETWEEN poi_hours.open_at AND poi_hours.close_at AND "
                      + "EXTRACT(ISODOW FROM ?::TIMESTAMP) = poi_hours.day_of_week "
                      + "ORDER BY ST_Distance(ST_SetSRID(ST_MakePoint(?,?),4326), "
                      + "ST_SetSRID(ST_MakePoint(p.latitude,p.longitude),4326)) LIMIT 1";
              try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setFloat(1, gpsData.getLongitude());
                preparedStatement.setFloat(2, gpsData.getLatitude());
                preparedStatement.setInt(3, 100);
                preparedStatement.setString(4, gpsData.getRentId().toString());
                preparedStatement.setString(5, gpsData.getRentId().toString());
                preparedStatement.setTimestamp(6, gpsData.getTimestamp());
                preparedStatement.setTimestamp(7, gpsData.getTimestamp());
                preparedStatement.setFloat(8, gpsData.getLongitude());
                preparedStatement.setFloat(9, gpsData.getLatitude());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                  if (resultSet.next()) {
                    return new Tuple2<>(
                        gpsData.getRentId(),
                        new PointOfInterest(
                            resultSet.getInt("id"),
                            resultSet.getString("merchant_vat"),
                            resultSet.getString("name"),
                            resultSet.getFloat("latitude"),
                            resultSet.getFloat("longitude"),
                            resultSet.getString("category"),
                            resultSet.getString("description")));
                  }
                  return null;
                }
              } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
                return null;
              }
            })
        .thenAccept(
            result -> {
              if (result != null) {
                resultFuture.complete(Collections.singleton(result));
              } else {
                resultFuture.complete(Collections.emptyList());
              }
            });
  }
}

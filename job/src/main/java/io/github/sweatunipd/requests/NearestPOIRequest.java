package io.github.sweatunipd.requests;

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
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class NearestPOIRequest
    extends RichAsyncFunction<GPSData, Tuple2<GPSData, PointOfInterest>> {

  private static final Logger LOG = LoggerFactory.getLogger(NearestPOIRequest.class);
  private transient Connection connection;

  public NearestPOIRequest() {}

  /**
   * Initialization method called before the trigger of the async operation
   *
   * @param openContext The context containing information about the context in which the function
   *     is opened.
   * @throws SQLException exception that occurs if the connection to the DB fails
   */
  @Override
  public void open(OpenContext openContext) throws SQLException {
    if (connection == null) {
      Map<String, String> config = getRuntimeContext().getGlobalJobParameters();
      Properties props = new Properties();
      props.setProperty("user", config.getOrDefault("postgres.username", "admin"));
      props.setProperty("password", config.getOrDefault("postgres.password", "adminadminadmin"));
      connection =
          DriverManager.getConnection(
              config.getOrDefault(
                  "postgres.jdbc.connection.url", "jdbc:postgresql://localhost:5432/admin"),
              props);
    }
  }

  /** Method that closes the async request * */
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
      GPSData gpsData, ResultFuture<Tuple2<GPSData, PointOfInterest>> resultFuture) {
    CompletableFuture.supplyAsync(
            () -> {
              try {
                ResultSet resultSet = getNearestPOI(gpsData);
                if (resultSet.next()) {
                  return new Tuple2<>(
                      gpsData,
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

  /**
   * ResultSet of the nearest POI to the user
   *
   * @param gpsData gps info about the user
   * @return POI in a resultSet
   * @throws SQLException thrown if creation and execution of prepared statement fails
   */
  public ResultSet getNearestPOI(GPSData gpsData) throws SQLException {
    String sql =
        "SELECT * FROM points_of_interest AS p JOIN poi_hours ON (p.id = poi_hours.poi_id) "
            + "WHERE ST_DWithin(ST_Transform(ST_SetSRID(ST_MakePoint(?,?),4326), 3857), "
            + "ST_Transform(ST_SetSRID(ST_MakePoint(p.latitude,p.longitude),4326), 3857), ?) AND "
            + "p.id NOT IN (SELECT poi_id FROM advertisements WHERE rent_id_position=?) AND "
            + "p.category IN (SELECT category FROM user_interests JOIN rents ON (user_interests.user_id = rents.user_id) WHERE rents.id=?) AND "
            + "? BETWEEN poi_hours.open_at AND poi_hours.close_at AND "
            + "EXTRACT(ISODOW FROM ?::TIMESTAMP) = poi_hours.day_of_week "
            + "ORDER BY ST_Distance(ST_SetSRID(ST_MakePoint(?,?),4326), "
            + "ST_SetSRID(ST_MakePoint(p.latitude,p.longitude),4326)) LIMIT 1";
    PreparedStatement preparedStatement = connection.prepareStatement(sql);
    preparedStatement.setFloat(1, gpsData.getLongitude());
    preparedStatement.setFloat(2, gpsData.getLatitude());
    preparedStatement.setInt(3, 100); // FIXME: range
    preparedStatement.setInt(4, gpsData.getRentId());
    preparedStatement.setInt(5, gpsData.getRentId());
    preparedStatement.setTimestamp(6, gpsData.getTimestamp());
    preparedStatement.setTimestamp(7, gpsData.getTimestamp());
    preparedStatement.setFloat(8, gpsData.getLongitude());
    preparedStatement.setFloat(9, gpsData.getLatitude());
    return preparedStatement.executeQuery();
  }
}

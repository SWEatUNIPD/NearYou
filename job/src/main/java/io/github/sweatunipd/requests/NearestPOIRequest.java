package io.github.sweatunipd.requests;

import io.github.sweatunipd.database.DataSourceSingleton;
import io.github.sweatunipd.entity.GPSData;
import io.github.sweatunipd.entity.PointOfInterest;
import java.sql.*;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NearestPOIRequest
    extends RichAsyncFunction<GPSData, Tuple2<GPSData, PointOfInterest>> {

  private static final Logger LOG = LoggerFactory.getLogger(NearestPOIRequest.class);
  private Connection connection;
  private static final String STMT =
      """
                          SELECT p.latitude, p.longitude, p.vat, p.name, p.category, p.offer FROM points_of_interest AS p JOIN poi_hours ON (p.latitude = poi_hours.latitude_poi AND p.longitude=poi_hours.longitude_poi)
                          WHERE ST_DWithin(ST_Transform(ST_SetSRID(ST_MakePoint(?,?),4326), 3857),
                          ST_Transform(ST_SetSRID(ST_MakePoint(p.longitude,p.latitude),4326), 3857), ?) AND
                          (p.latitude, p.longitude) NOT IN (SELECT latitude_poi, longitude_poi FROM advertisements WHERE rent_id=?) AND
                          p.category IN (SELECT user_interests.category FROM user_interests JOIN rents ON (user_interests.user_email=rents.user_email) WHERE rents.id=?) AND
                          ? BETWEEN poi_hours.open_at AND poi_hours.close_at AND
                          EXTRACT(ISODOW FROM ?::TIMESTAMP) = poi_hours.day_of_week
                          ORDER BY ST_Distance(ST_SetSRID(ST_MakePoint(?,?),4326),
                          ST_SetSRID(ST_MakePoint(p.longitude,p.latitude),4326)) LIMIT 1
                  """;

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
    connection = DataSourceSingleton.getConnection();
  }

  /** Method that closes the async request * */
  @Override
  public void close() {
    try {
      connection.close();
    } catch (SQLException e) {
      LOG.error(e.getMessage(), e);
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
              try (PreparedStatement preparedStatement = connection.prepareStatement(STMT)) {
                preparedStatement.setFloat(1, gpsData.getLongitude());
                preparedStatement.setFloat(2, gpsData.getLatitude());
                preparedStatement.setInt(3, 100);
                preparedStatement.setInt(4, gpsData.getRentId());
                preparedStatement.setInt(5, gpsData.getRentId());
                preparedStatement.setTimestamp(6, gpsData.getTimestamp());
                preparedStatement.setTimestamp(7, gpsData.getTimestamp());
                preparedStatement.setFloat(8, gpsData.getLongitude());
                preparedStatement.setFloat(9, gpsData.getLatitude());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                  if (resultSet.next()) {
                    return new Tuple2<>(
                        gpsData,
                        new PointOfInterest(
                            resultSet.getFloat(1),
                            resultSet.getFloat(2),
                            resultSet.getString(3),
                            resultSet.getString(4),
                            resultSet.getString(5),
                            resultSet.getString(6)));
                  }
                }
              } catch (SQLException e) {
                LOG.error(e.getMessage(), e);
              }
              return null;
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

package io.github.sweatunipd;

import io.github.sweatunipd.entity.GPSData;
import io.github.sweatunipd.entity.PointOfInterest;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;


import java.sql.*;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

public class NearestPOIRequest extends RichAsyncFunction<GPSData, Tuple2<Integer, PointOfInterest>> {

  private transient Connection connection;

  @Override
  public void open(OpenContext openContext) throws SQLException {
    Properties props = new Properties();
    props.setProperty("user", "admin");
    props.setProperty("password", "adminadminadmin");
//    props.setProperty("ssl", "true"); FIXME: fixare l'SSL per le comunicazioni
    connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/admin", props);
  }

  @Override
  public void close() throws SQLException {
    connection.close();
  }

  @Override
  public void asyncInvoke(GPSData gpsData, ResultFuture<Tuple2<Integer, PointOfInterest>> resultFuture)
      throws SQLException {
    CompletableFuture.<Tuple2<Integer, PointOfInterest>>supplyAsync(
            () -> {
              String sql =
                  "SELECT * FROM points_of_interest AS p "
                          + "WHERE ST_DWithin(ST_Transform(ST_SetSRID(ST_MakePoint(?,?),4326), 3857), "
                          + "ST_Transform(ST_SetSRID(ST_MakePoint(p.latitude,p.longitude),4326), 3857), ?) AND "
                          + "p.id NOT IN (SELECT poi_id FROM advertisements WHERE rent_id=?)"
                          + "ORDER BY ST_Distance(ST_SetSRID(ST_MakePoint(?,?),4326), "
                          + "ST_SetSRID(ST_MakePoint(p.latitude,p.longitude),4326)) LIMIT 1";
              try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setDouble(1, gpsData.getLatitude());
                preparedStatement.setDouble(2, gpsData.getLongitude());
                preparedStatement.setInt(3, 50); // Dimensione del raggio
                preparedStatement.setInt(4, gpsData.getRentId());
                preparedStatement.setDouble(5, gpsData.getLatitude());
                preparedStatement.setDouble(6, gpsData.getLongitude());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                  if (resultSet.next()) {
                    return new Tuple2<Integer, PointOfInterest>(gpsData.getRentId(), new PointOfInterest.PointOfInterestBuilder()
                            .setMerchantVAT(resultSet.getString("merchant_vat"))
                            .setName(resultSet.getString("name"))
                            .setStartTime(resultSet.getTime("start_at"))
                            .setEndTime(resultSet.getTime("end_at"))
                            .setLatitude(resultSet.getFloat("latitude"))
                            .setLongitude(resultSet.getFloat("longitude"))
                            .build());
                  }
                  return null;
                }
              } catch (SQLException e) {
                return null;
              }
            })
        .thenAccept(
            result -> {
              if (result != null) {
                resultFuture.complete(Collections.singleton(result));
              }else {
                resultFuture.complete(Collections.emptyList());
              }
            });
  }
}

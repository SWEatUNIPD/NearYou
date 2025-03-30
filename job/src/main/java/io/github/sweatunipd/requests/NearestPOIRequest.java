package io.github.sweatunipd.requests;

import io.github.sweatunipd.database.DatabaseConnectionSingleton;
import io.github.sweatunipd.model.GPSData;
import io.github.sweatunipd.model.PointOfInterest;
import io.r2dbc.spi.Connection;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class NearestPOIRequest
    extends RichAsyncFunction<GPSData, Tuple2<GPSData, PointOfInterest>> {

  private static final Logger LOG = LoggerFactory.getLogger(NearestPOIRequest.class);

  private static final String STMT =
      """
                            SELECT p.latitude, p.longitude, p.vat, p.name, p.category, p.offer FROM points_of_interest AS p JOIN poi_hours ON (p.latitude = poi_hours.latitude_poi AND p.longitude=poi_hours.longitude_poi)
                            WHERE ST_DWithin(ST_Transform(ST_SetSRID(ST_MakePoint($1,$2),4326), 3857),
                            ST_Transform(ST_SetSRID(ST_MakePoint(p.longitude,p.latitude),4326), 3857), $3) AND
                            (p.latitude, p.longitude) NOT IN (SELECT latitude_poi, longitude_poi FROM advertisements WHERE position_rent_id=$4) AND
                            p.category IN (SELECT user_interests.category FROM user_interests JOIN rents ON (user_interests.user_email=rents.user_email) WHERE rents.id=$5) AND
                            $6::TIME WITH TIME ZONE BETWEEN poi_hours.open_at AND poi_hours.close_at AND
                            EXTRACT(ISODOW FROM $7::TIMESTAMP) = poi_hours.day_of_week
                            ORDER BY ST_Distance(ST_SetSRID(ST_MakePoint($8,$9),4326),
                            ST_SetSRID(ST_MakePoint(p.longitude,p.latitude),4326)) LIMIT 1
                    """;

  public NearestPOIRequest() {}

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
    ZonedDateTime zonedDateTime = gpsData.timestamp().toInstant().atZone(ZoneId.of("UTC"));
    Mono.usingWhen(
            DatabaseConnectionSingleton.getConnectionFactory().create(),
            connection ->
                Mono.from(
                        connection
                            .createStatement(STMT)
                            .bind("$1", gpsData.longitude())
                            .bind("$2", gpsData.latitude())
                            .bind("$3", 100)
                            .bind("$4", gpsData.rentId())
                            .bind("$5", gpsData.rentId())
                            .bind("$6", zonedDateTime)
                            .bind("$7", zonedDateTime)
                            .bind("$8", gpsData.longitude())
                            .bind("$9", gpsData.latitude())
                            .execute())
                    .flatMap(result -> Mono.from(result.map((row, metadata) -> row))),
            Connection::close)
        .map(
            row ->
                new PointOfInterest(
                    row.get("latitude", Float.class),
                    row.get("longitude", Float.class),
                    row.get("vat", String.class),
                    row.get("name", String.class),
                    row.get("category", String.class),
                    row.get("offer", String.class)))
        .map(poi -> Collections.singleton(new Tuple2<>(gpsData, poi)))
        .defaultIfEmpty(Collections.emptySet())
        .doOnSuccess(resultFuture::complete)
        .doOnError(
            throwable -> {
              LOG.error(throwable.getMessage(), throwable);
              resultFuture.complete(Collections.emptySet());
            })
        .subscribe();
  }
}

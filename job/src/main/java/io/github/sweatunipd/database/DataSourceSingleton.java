package io.github.sweatunipd.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author SWEatUNIPD
 */

public class DataSourceSingleton {
  private static HikariDataSource instance;
  private static HikariConfig config = new HikariConfig();

  static {
    config.setJdbcUrl("jdbc:postgresql://postgis:5432/admin");
    config.setUsername("admin");
    config.setPassword("adminadminadmin");
  }

  private DataSourceSingleton() {}

  public static synchronized Connection getConnection() throws SQLException {
    if(instance==null){
      instance = new HikariDataSource(config);
    }
    return instance.getConnection();
  }
}

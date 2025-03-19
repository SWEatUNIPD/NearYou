package io.github.sweatunipd.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceSingleton {
  private static HikariDataSource instance;
  private static final HikariConfig CONFIG = new HikariConfig();

  static {
    CONFIG.setJdbcUrl(System.getProperty("jdbc.url", "jdbc:postgresql://postgis:5432/admin"));
    CONFIG.setUsername("admin");
    CONFIG.setPassword("adminadminadmin");
  }

  private DataSourceSingleton() {}

  public static synchronized Connection getConnection() throws SQLException {
    if (instance == null) {
      instance = new HikariDataSource(CONFIG);
    }
    return instance.getConnection();
  }
}

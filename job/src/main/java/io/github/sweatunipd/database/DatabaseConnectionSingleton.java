package io.github.sweatunipd.database;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;

public class DatabaseConnectionSingleton {

  private static ConnectionFactory instance;

  private DatabaseConnectionSingleton() {}

  public static synchronized ConnectionFactory getConnectionFactory() {
    if (instance == null) {
      instance =
          ConnectionFactories.get(
              ConnectionFactoryOptions.builder()
                  .option(ConnectionFactoryOptions.DRIVER, "pool")
                  .option(ConnectionFactoryOptions.PROTOCOL, "postgresql")
                  .option(
                      ConnectionFactoryOptions.HOST,
                      System.getProperty("postgres.hostname", "postgis"))
                  .option(
                      ConnectionFactoryOptions.PORT,
                      Integer.parseInt(System.getProperty("postgres.port", "5432")))
                  .option(
                      ConnectionFactoryOptions.USER,
                      System.getProperty("postgres.username", "admin"))
                  .option(
                      ConnectionFactoryOptions.PASSWORD,
                      System.getProperty("postgres.password", "adminadminadmin"))
                  .option(
                      ConnectionFactoryOptions.DATABASE,
                      System.getProperty("postgres.dbname", "admin"))
                  .build());
    }
    return instance;
  }
}

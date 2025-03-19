package io.github.sweatunipd.database;

import com.zaxxer.hikari.HikariDataSource;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DataSourceSingletonTest {

  @Mock private Connection connection;

  @BeforeEach
  public void setUp() throws SQLException, NoSuchFieldException, IllegalAccessException {
    // Not the best solution, used reflection to reset the static instance
    Field instanceField = DataSourceSingleton.class.getDeclaredField("instance");
    instanceField.setAccessible(true);
    instanceField.set(null, null);
  }

  @Test
  @DisplayName("Test connection for first time")
  void testConnection() throws SQLException {
    try (MockedConstruction<HikariDataSource> ignored =
        Mockito.mockConstruction(
            HikariDataSource.class,
            (mock, context) -> Mockito.when(mock.getConnection()).thenReturn(connection))) {
      Assertions.assertInstanceOf(Connection.class, DataSourceSingleton.getConnection());
      Assertions.assertInstanceOf(Connection.class, DataSourceSingleton.getConnection());
    }
  }

  @Test
  @DisplayName("Test connection failure")
  void testConnectionFailure() {
    try (MockedConstruction<HikariDataSource> ignored =
        Mockito.mockConstruction(
            HikariDataSource.class,
            (mock, context) ->
                Mockito.when(mock.getConnection())
                    .thenThrow(new SQLException("Error creating connection")))) {
      SQLException ex =
          Assertions.assertThrows(SQLException.class, DataSourceSingleton::getConnection);
      Assertions.assertEquals("Error creating connection", ex.getMessage());
    }
  }
}

package io.github.sweatunipd.database;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import java.lang.reflect.Field;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DatabaseConnectionSingletonTest {

  @Mock private ConnectionFactory connectionFactory;

  @BeforeEach
  public void setUp() throws NoSuchFieldException, IllegalAccessException {
    // Not the best solution, used reflection to reset the static instance
    Field instanceField = DatabaseConnectionSingleton.class.getDeclaredField("instance");
    instanceField.setAccessible(true);
    instanceField.set(null, null);
  }

  @Test
  @DisplayName("Test connection for first time")
  void testConnection() {
    try (MockedStatic<ConnectionFactories> connectionFactoriesMockedStatic =
        Mockito.mockStatic(ConnectionFactories.class)) {
      connectionFactoriesMockedStatic
          .when(() -> ConnectionFactories.get(Mockito.<ConnectionFactoryOptions>any()))
          .thenReturn(connectionFactory);
      Assertions.assertInstanceOf(
          ConnectionFactory.class, DatabaseConnectionSingleton.getConnectionFactory());
    }
  }

  @Test
  @DisplayName("Test singleton on ConnectionFactory")
  void testSingleton() {
    try (MockedStatic<ConnectionFactories> connectionFactoriesMockedStatic =
        Mockito.mockStatic(ConnectionFactories.class)) {
      connectionFactoriesMockedStatic
          .when(() -> ConnectionFactories.get(Mockito.anyString()))
          .thenReturn(connectionFactory);
      ConnectionFactory connectionFactory1 = DatabaseConnectionSingleton.getConnectionFactory();
      ConnectionFactory connectionFactory2 = DatabaseConnectionSingleton.getConnectionFactory();
      Assertions.assertSame(connectionFactory1, connectionFactory2);
    }
  }
}

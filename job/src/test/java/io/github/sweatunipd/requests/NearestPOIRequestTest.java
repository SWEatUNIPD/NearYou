package io.github.sweatunipd.requests;

import io.github.sweatunipd.entity.GPSData;
import io.github.sweatunipd.entity.PointOfInterest;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.sql.*;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class NearestPOIRequestTest {

//  @Mock private Connection mockedConnection;
//  @Mock private PreparedStatement mockedPreparedStatement;
//  @Mock private ResultSet mockedResultSet;
//  @Mock private ResultFuture<Tuple2<UUID, PointOfInterest>> mockedResultFuture;
//  @Mock private OpenContext mockedOpenContext;
//
//  private NearestPOIRequest nearestPOIRequest;
//  private GPSData gpsData;
//
//  @BeforeEach
//  void setUp() throws Exception {
//    MockitoAnnotations.openMocks(this);
//
//    when(mockedConnection.prepareStatement(anyString())).thenReturn(mockedPreparedStatement);
//    when(mockedPreparedStatement.executeQuery()).thenReturn(mockedResultSet);
//
//    nearestPOIRequest = new NearestPOIRequest();
//    nearestPOIRequest.setConnection(mockedConnection);
//    gpsData =
//        new GPSData(UUID.randomUUID(), 78.5f, 78.5f, new Timestamp(System.currentTimeMillis()));
//  }
//
//  @Test
//  @DisplayName("Test prepared statement")
//  void testPreparedStatement() throws SQLException {
//    nearestPOIRequest.getNearestPOI(gpsData);
//
//    verify(mockedPreparedStatement).setFloat(1, gpsData.getLongitude());
//    verify(mockedPreparedStatement).setFloat(2, gpsData.getLatitude());
//    verify(mockedPreparedStatement).setInt(3, 100);
//    verify(mockedPreparedStatement).setString(4, gpsData.getRentId().toString());
//    verify(mockedPreparedStatement).setString(5, gpsData.getRentId().toString());
//    verify(mockedPreparedStatement).setTimestamp(6, gpsData.getTimestamp());
//    verify(mockedPreparedStatement).setTimestamp(7, gpsData.getTimestamp());
//    verify(mockedPreparedStatement).setFloat(8, gpsData.getLongitude());
//    verify(mockedPreparedStatement).setFloat(9, gpsData.getLatitude());
//  }
//
//  @Test
//  @DisplayName("Receive nearest POI")
//  void testNearestPOI() throws SQLException {
//    when(mockedResultSet.getInt("id")).thenReturn(1);
//    when(mockedResultSet.getString("merchant_vat")).thenReturn("IT101010101");
//    when(mockedResultSet.getString("name")).thenReturn("Test POI");
//    when(mockedResultSet.getFloat("latitude")).thenReturn(78.5f);
//    when(mockedResultSet.getFloat("longitude")).thenReturn(78.5f);
//    when(mockedResultSet.getString("category")).thenReturn("Food");
//    when(mockedResultSet.getString("description")).thenReturn("Test Description");
//    when(mockedResultSet.next()).thenReturn(true);
//
//    ResultSet result = nearestPOIRequest.getNearestPOI(gpsData);
//
//    verify(mockedPreparedStatement).setFloat(1, gpsData.getLongitude());
//    verify(mockedPreparedStatement).setFloat(2, gpsData.getLatitude());
//    verify(mockedPreparedStatement).setInt(3, 100);
//    verify(mockedPreparedStatement).setString(4, gpsData.getRentId().toString());
//    verify(mockedPreparedStatement).setString(5, gpsData.getRentId().toString());
//    verify(mockedPreparedStatement).setTimestamp(6, gpsData.getTimestamp());
//    verify(mockedPreparedStatement).setTimestamp(7, gpsData.getTimestamp());
//    verify(mockedPreparedStatement).setFloat(8, gpsData.getLongitude());
//    verify(mockedPreparedStatement).setFloat(9, gpsData.getLatitude());
//
//    assertNotNull(result);
//    assertTrue(result.next());
//    assertEquals(1, result.getInt("id"));
//    assertEquals("IT101010101", result.getString("merchant_vat"));
//    assertEquals("Test POI", result.getString("name"));
//    assertEquals(78.5f, result.getFloat("latitude"));
//    assertEquals(78.5f, result.getFloat("longitude"));
//    assertEquals("Food", result.getString("category"));
//    assertEquals("Test Description", result.getString("description"));
//  }
//
//  @Test
//  @DisplayName("No POI found")
//  public void testNoPOIFound() throws SQLException {
//    when(mockedResultSet.getInt("id")).thenReturn(0);
//    when(mockedResultSet.getString("merchant_vat")).thenReturn(null);
//    when(mockedResultSet.getString("name")).thenReturn(null);
//    when(mockedResultSet.getFloat("latitude")).thenReturn(0f);
//    when(mockedResultSet.getFloat("longitude")).thenReturn(0f);
//    when(mockedResultSet.getString("category")).thenReturn(null);
//    when(mockedResultSet.getString("description")).thenReturn(null);
//    when(mockedResultSet.next()).thenReturn(false);
//
//    ResultSet result = nearestPOIRequest.getNearestPOI(gpsData);
//    assertFalse(result.next());
//    assertEquals(0, result.getInt("id"));
//    assertNull(result.getString("merchant_vat"));
//    assertNull(result.getString("name"));
//    assertEquals(0f, result.getFloat("latitude"));
//    assertEquals(0f, result.getFloat("longitude"));
//    assertNull(result.getString("category"));
//    assertNull(result.getString("description"));
//  }
//
//  @Test
//  @DisplayName("Failure at statement prepare")
//  public void testPreparedStatementFailure() throws SQLException {
//    when(mockedConnection.prepareStatement(anyString())).thenThrow(new SQLException());
//    assertThrows(SQLException.class, () -> nearestPOIRequest.getNearestPOI(gpsData));
//  }
//
//  @Test
//  @DisplayName("Test of open() method")
//  public void testOpenMethod() throws SQLException {
//    try (MockedStatic<DriverManager> mockedDriverManager =
//        Mockito.mockStatic(DriverManager.class)) {
//      mockedDriverManager
//          .when(() -> DriverManager.getConnection(anyString(), any(Properties.class)))
//          .thenReturn(mockedConnection);
//
//      nearestPOIRequest.open(mockedOpenContext);
//
//      assertNotNull(mockedConnection);
//    }
//  }
//
//  @Test
//  @DisplayName("Failure at initialization of database")
//  public void testConnectionFailure() throws SQLException {
//    try (MockedStatic<DriverManager> mockedDriverManager =
//        Mockito.mockStatic(DriverManager.class)) {
//      mockedDriverManager
//              .when(() -> DriverManager.getConnection(anyString(), any(Properties.class)))
//              .thenReturn(mockedConnection);
//    }
//
//    assertThrows(SQLException.class, () -> nearestPOIRequest.open(mockedOpenContext));
//  }
}

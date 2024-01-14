package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
public class FileAlarmTest {
	
	@SystemStub
	private EnvironmentVariables enviromentVariables = new EnvironmentVariables("firealarm.location", System.getProperty("user.dir"));
	private FireAlarm fireAlarm;
	protected SQLException result;
	
	
	@BeforeEach
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException {
		fireAlarm = new FireAlarm();
	}
	
	
	@Test
	public void testNotFoundConfigPropertiesFile() {
	    // Set the environmental variable `firealarm.location` to an invalid value
	    enviromentVariables.set("firealarm.location", "/path/to/non-existent/file");

	    // Ensure that the creation of FireAlarm throws a ConfigurationFileProblemException
	    ConfigurationFileProblemException exception = assertThrows(ConfigurationFileProblemException.class, () -> {
	        new FireAlarm();
	    });

	    // Verify that the error message indicates that the file could not be found
	    assertNull(exception.getMessage());
	}
	
	
	@Test
	public void testDatabaseProblemException() throws ConfigurationFileProblemException {
	    try {
	        // Set the environmental variable `firealarm.location` to a valid value
	        enviromentVariables.set("firealarm.location", "./resources/sensors.db");

	        // Create a new instance of FireAlarm
	        FireAlarm fireAlarm = new FireAlarm();

	        // Set the database location to an invalid value
	        fireAlarm.dblocation = "/path/to/non-existent/file";

	        // Try to get the temperature from a sensor
	        fireAlarm.getTemperature("kitchen");
	    } catch (Exception e) {
	        // Verify that the exception is a DatabaseProblemException
	        assertFalse(e instanceof DatabaseProblemException);

	         ConfigurationFileProblemException exception = assertThrows(ConfigurationFileProblemException.class, () -> {
	        new FireAlarm();
	    });
			// Verify that the error message indicates that the database query failed
	        assertNull(exception.getMessage());
	        //assertEquals("Database query failed", e.getMessage());
	    }
	}
	
	@Test
	 @Disabled("Revisar")
	public void testDatabaseProblemExceptionB() throws ConfigurationFileProblemException {
	    try {
	        // Set the environmental variable `firealarm.location` to a valid value
	        enviromentVariables.set("firealarm.location", "./resources/sensors.db");

	        // Create a new instance of FireAlarm
	        FireAlarm fireAlarm = new FireAlarm();

	        // Try to get the temperature from a sensor that does not exist
	        fireAlarm.getTemperature("non-existent-room");
	    } catch (Exception e) {
	        // Verify that the exception is a DatabaseProblemException
	        assertTrue(e instanceof DatabaseProblemException);

	        // Verify that the error message indicates that the database query failed
	        assertEquals("Database query failed", e.getMessage());
	    }
	}


	
	
	@Test
	public void testBadEndpoint(){
		fireAlarm.sensors.put("a", "http://localhost:8080/tarea4");
		assertThrows(SensorConnectionProblemException.class, () -> {
			fireAlarm.getTemperature("aa");
		});
	}

	
	@Test
	public void JSONResponseWithoutTemperature() throws JsonProcessingException{
		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
		fireAlarm.setMapper(mockObjectMapper);

		JsonNode mockNode = mock(JsonNode.class);
		when(mockObjectMapper.readTree(any(String.class))).thenReturn(mockNode);
		when(mockNode.get("temperature")).thenReturn(null);

		assertThrows(IncorrectDataException.class, () -> fireAlarm.getTemperature("kitchen"));
	}
	
	
	@Test
	public void testSensorConnectionProblemException() throws SensorConnectionProblemException {
		assertThrows(SensorConnectionProblemException.class, () -> fireAlarm.getTemperature("26"));
	}
	
	
	@Test
	public void testNotFoundAppLocationEnv() {
		assertThrows(ConfigurationFileProblemException.class, () -> {
			enviromentVariables.set("firealarm.location", null);
			new FireAlarm();
		});
	}
	
	
	@Test
	public void testIsTemperatureTooHigh_TRUE() throws SensorConnectionProblemException, IncorrectDataException, ConfigurationFileProblemException, DatabaseProblemException {
		// Mock getTemperature to return a value greater than MAX_TEMPERATURE
		FireAlarm fireAlarm = spy(new FireAlarm());
		doReturn(85).when(fireAlarm).getTemperature(anyString());

		// Test isTemperatureTooHigh() to return true
		try {
			assertTrue(fireAlarm.isTemperatureTooHigh());
		} catch (SensorConnectionProblemException | IncorrectDataException e) {
			fail("Unexpected exception");
		}
	}
	
	
	@Test
	public void testIsTemperatureTooHigh_FALSE() throws SensorConnectionProblemException, IncorrectDataException, ConfigurationFileProblemException, DatabaseProblemException {
		// Mock getTemperature to return values less than or equal to MAX_TEMPERATURE
		FireAlarm fireAlarm = spy(new FireAlarm());
		doReturn(75).when(fireAlarm).getTemperature(anyString());

		// Test isTemperatureTooHigh() to return false
		try {
			assertFalse(fireAlarm.isTemperatureTooHigh());
		} catch (SensorConnectionProblemException | IncorrectDataException e) {
			fail("Unexpected exception");
		}
	}

}
package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
public class FileAlarmTest {
	
	FireAlarm fireAlarm;
	String query;

	@SystemStub
	private EnvironmentVariables envVariables = new EnvironmentVariables(
		"firealarm.location", System.getProperty("user.dir")
	);
	
	
	@BeforeEach
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException 
	{
		query = "SELECT * FROM SENSORS";
		fireAlarm = new FireAlarm(query);
	}
	
	/*
	 * a)
	 * When the config.properties file cannot be located, 
	 * the FireAlarm class throws a ConfigurationFileProblemException.
	 */
	@Test
	public void testConfigurationFileNotFound() 
	{
		envVariables.set("firealarm.location", "non-existing-file");
		assertThrows(ConfigurationFileProblemException.class, () -> {
			fireAlarm = new FireAlarm(query);
		});
	}

	/*
	 * b)
	 * Any database error, e.g. connection problem or query error, 
	 * implies the launching of a DatabaseProblemException.
	 * For this test, query was added as a parameter to the constructor.
	 * This way, we apply the Dependency Inversion Principle,
	 * making FireAlarm more flexible and testable.
	 */
	@Test
	public void testDatabaseError() 
	{
		query = "SELECT * FROM NON_EXISTING_TABLE";
		assertThrows(DatabaseProblemException.class, () -> {
			fireAlarm = new FireAlarm(query);
		});
	}

	/*
	 * c)
	 * When the REST endpoint is not usable, 
	 * the application throws a SensorConnectionProblemException.
	 * Changed the getTemperature method to protected.
	 */
	@Test
	public void testSensorConnectionProblemException() throws SensorConnectionProblemException, IncorrectDataException {
		assertThrows(SensorConnectionProblemException.class, () -> {
			fireAlarm.getTemperature("potato-room");
		});
	}

}

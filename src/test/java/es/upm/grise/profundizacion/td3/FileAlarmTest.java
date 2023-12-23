package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertFalse;import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
public class FileAlarmTest {
	
	FireAlarm fireAlarm;

	@SystemStub
	private EnvironmentVariables environmentVariables = new EnvironmentVariables("firealarm.location", System.getProperty("user.dir"));
	
	@BeforeEach
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException {
		fireAlarm = new FireAlarm();
	}
	
	@Test
	public void test() throws SensorConnectionProblemException, IncorrectDataException {
		assertFalse(fireAlarm.isTemperatureTooHigh());
	}

	// 1º Test: Cuando no se puede localizar el fichero config.properties, la clase FireAlarm lanza una ConfigurationFileProblemException.
	@Test
	public void testConfigurationFileProblemException() throws ConfigurationFileProblemException, DatabaseProblemException {
		environmentVariables.set("firealarm.location", null);
		assertThrows(ConfigurationFileProblemException.class, () -> new FireAlarm());
	}
}

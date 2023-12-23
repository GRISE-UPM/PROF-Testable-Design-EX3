package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertFalse;import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
public class FileAlarmTest {
	
	FireAlarm fireAlarm;
	Properties mockProperties;

	@SystemStub
	private EnvironmentVariables environmentVariables = new EnvironmentVariables("firealarm.location", System.getProperty("user.dir"));
	
	@BeforeEach
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException {
		mockProperties = mock(Properties.class);
		//fireAlarm = new FireAlarm(mockProperties);
	}
	
	/*@Test
	public void test() throws SensorConnectionProblemException, IncorrectDataException {
		assertFalse(fireAlarm.isTemperatureTooHigh());
	}*/

	// 1º Test: Cuando no se puede localizar el fichero config.properties, la clase FireAlarm lanza una ConfigurationFileProblemException.
	@Test
	public void testConfigurationFileProblemException() throws ConfigurationFileProblemException, DatabaseProblemException {
		environmentVariables.set("firealarm.location", null);
		assertThrows(ConfigurationFileProblemException.class, () -> new FireAlarm(mockProperties));
	}

	// 2º Test: Cualquier error de la base de datos, ej: problema de conexión o error en consulta, implica el lanzamiento de una DatabaseProblemException.
	// Modificación en FireAlarm: La variable Properties hay que pasarsela al constructor en vez de iniciarla dentro del constructor. Esta variable se iniciará en FireAlarmApp y se le pasará a FireAlarm. 
	@Test
	public void testDatabaseProblemException() throws ConfigurationFileProblemException, DatabaseProblemException{
		when(mockProperties.getProperty("dblocation")).thenReturn("/invalid/path/to/database");
		assertThrows(DatabaseProblemException.class, () -> new FireAlarm(mockProperties));
		
	}
}

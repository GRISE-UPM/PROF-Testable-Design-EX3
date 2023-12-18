package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertThrows;

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
	private EnvironmentVariables envVariables = new EnvironmentVariables("firealarm.location", System.getProperty("user.dir"));
	
	
	@BeforeEach
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException {
		fireAlarm = new FireAlarm();
	}
	
	@Test
	public void configFileNotFound() throws ConfigurationFileProblemException {
		assertThrows(ConfigurationFileProblemException.class, () -> {
			envVariables.set("firealarm.location", null);
			new FireAlarm();
		});
	}
	
	public void databaseError() throws DatabaseProblemException {
		
	}
	
	public void endpointNotUsable() throws SensorConnectionProblemException {
		
	}
	
	public void responseWithoutJSON() throws IncorrectDataException {
		
	}
	
	public void temperatureIsOk() {
		
	}
	
	public void temperatureIsTooHigh() {
		
	}
}

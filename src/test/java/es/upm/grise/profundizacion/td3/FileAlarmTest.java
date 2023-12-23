package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
	private EnvironmentVariables envVariables = new EnvironmentVariables(
		"firealarm.location", System.getProperty("user.dir")
	);
	
	
	@BeforeEach
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException 
	{
		fireAlarm = new FireAlarm();
	}
	
	/*
	 * When the config.properties file cannot be located, 
	 * the FireAlarm class throws a ConfigurationFileProblemException.
	 */
	@Test
	public void testConfigurationFileNotFound() 
	{
		envVariables.set("firealarm.location", "non-existing-file");
		assertThrows(ConfigurationFileProblemException.class, () -> {
			fireAlarm = new FireAlarm();
		});
	}

	

}

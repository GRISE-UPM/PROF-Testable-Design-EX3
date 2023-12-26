package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
public class FileAlarmTest {
	
	@SystemStub
	private EnvironmentVariables locationEnvironmentVariables = new EnvironmentVariables("firealarm.location", System.getProperty("user.dir"));

	FireAlarm fireAlarm;
	
	@BeforeEach
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException {
		fireAlarm = new FireAlarm();
	}
	
	/**
	 * Test case to verify that a ConfigurationFileProblemException is thrown when there is no absolute path to the 
	 * fire alarm in the configuration file.
	 *
	 * @throws ConfigurationFileProblemException if there is a problem with the configuration file
	 * @throws DatabaseProblemException if there is a problem with the database
	 */
	@Test
	public void test_NoAbsolutePathToFireAlarm_ConfFileException() throws ConfigurationFileProblemException, DatabaseProblemException {
		assertThrows(ConfigurationFileProblemException.class, () -> {
				this.locationEnvironmentVariables.set("firealarm.location", null);
				new FireAlarm();
			});
	}


	/**
	 * Test case to verify that a DatabaseProblemException is thrown when there is no connection to the database.
	 *
	 * @throws ConfigurationFileProblemException if there is a problem with the configuration file
	 * @throws DatabaseProblemException if there is a problem with the database
	 * @throws IOException if there is an I/O problem
	 */
	@Test
	public void test_NoConnectionToDatabase_DatabaseException() throws ConfigurationFileProblemException, DatabaseProblemException, IOException {
			Path tempDir = Files.createTempDirectory("testDir");
			Path resourcesDirectory = tempDir.resolve("resources");
			Files.createDirectories(resourcesDirectory);
			Path configFile = resourcesDirectory.resolve("config.properties");
			Files.createFile(configFile);

			FileWriter writer = new FileWriter(configFile.toFile());
			writer.write("dblocation=errorLocation");
			writer.close();

			assertThrows(DatabaseProblemException.class, () -> {
				locationEnvironmentVariables.set("firealarm.location", tempDir.toString());
				new FireAlarm();
			});
	}


	/**
	 * Test case to verify that the default temperature is not too high.
	 * 
	 * @throws SensorConnectionProblemException if there is a problem with the sensor connection
	 * @throws IncorrectDataException if the data from the sensor is incorrect
	 */
	@Test
	public void test_TemperatureBelow80() throws SensorConnectionProblemException, IncorrectDataException {
		assertFalse(fireAlarm.isTemperatureTooHigh());
	}

	
}

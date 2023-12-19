package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

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
	
	/*
	 * No source code changes are needed to pass this test
	 * @throws ConfigurationFileProblemException
	 * @throws DatabaseProblemException
	 */

	@Test
	public void testNotFoundAppLocationEnv() throws ConfigurationFileProblemException {
		assertThrows(ConfigurationFileProblemException.class, () -> {
			envVariables.set("firealarm.location", null);
			new FireAlarm();
		});
	}
	
	/*
	 * Is needed to change the envVar and content of dbconfig to mock the access into non existing db
	 * @throws DatabaseProblemException
	 * @throws IOException
	 */
	@Test
	public void testErrorDatabase(@TempDir Path tempDir) throws DatabaseProblemException, IOException {
		// creating a temp directory
		Files.createDirectories(tempDir.resolve("resources"));
		File tempConfigDBFile = new File(tempDir.resolve("resources") + "/config.properties");
		tempConfigDBFile.createNewFile();

		// writing wrong db location
		FileWriter myWriter = new FileWriter(tempConfigDBFile);
		myWriter.write("dblocation=no_mas_practicas_por_favor");
		myWriter.close();

		// setting the env variable
		envVariables.set("firealarm.location", tempDir.toString());

		assertThrows(DatabaseProblemException.class, () -> {
			new FireAlarm();
		});
	}
	
	/*
	 * 
	 * 
	 */
	@Test
	public void testBadRoomSensor() throws SensorConnectionProblemException {
		assertThrows(SensorConnectionProblemException.class, () -> {
			fireAlarm.getTemperature("");
		});
	}

	/*
	 * Changed fireAlarm sensors to protected to mock the access into a bad endpoint
	 * @throws SensorConnectionProblemException
	 */
	@Test
	public void testBadEndpoint() throws SensorConnectionProblemException {
		fireAlarm.sensors.put("kitchen", "http://localhost:8080/de_verdad_no_mas_practicas");
		assertThrows(SensorConnectionProblemException.class, () -> {
			fireAlarm.getTemperature("");
		});
	}
	
	@Test
	public void responseWithoutJSON() throws IncorrectDataException {
		
	}
	
	@Test
	public void temperatureIsOk() {
		
	}
	
	@Test
	public void temperatureIsTooHigh() {
		
	}
}

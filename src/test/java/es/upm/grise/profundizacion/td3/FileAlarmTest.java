package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
public class FileAlarmTest {

	private FireAlarm fireAlarm;

	@SystemStub
	private EnvironmentVariables variables = new EnvironmentVariables("firealarm.location",
			System.getProperty("user.dir"));

	@BeforeEach
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException {
		this.fireAlarm = new FireAlarm();
	}


	/**
	 * In this case we just need to change the firealarm.location env var. There is no need to modify any FileAlarm class code.
	 * @throws ConfigurationFileProblemException
	 * @throws DatabaseProblemException
	 */

	@Test
	public void testNotFoundFirealarmLocationEnvVar()
			throws ConfigurationFileProblemException, DatabaseProblemException {
		assertThrows(ConfigurationFileProblemException.class, () -> {
			this.variables.set("firealarm.location", null);
			new FireAlarm();
		}, "Expected throw when no firealarm.location env var found");
	}

	/**
	 * We need to configure a pathing and a file in the tempDir for mocking the access into a non existing db location
	 * @param tempDir
	 * @throws IOException
	 */

	@Test
	public void testNotFoundDBLocationConfigVar(@TempDir Path tempDir) throws IOException {
		Files.createDirectory(tempDir.resolve("resources"));
		File temp = new File(tempDir.resolve("resources") + "/config.properties");
		temp.createNewFile();

		FileWriter myWriter = new FileWriter(temp.getAbsolutePath());
		myWriter.write("dblocation=jdbc:sqlite:/not_existing_db");
		myWriter.close();

		this.variables.set("firealarm.location", tempDir.toString());

		assertThrows(DatabaseProblemException.class, () -> {
			new FireAlarm();
		}, "Expected throw when no dblocation config property found");
	}

	/**
	 * This could have been tested by chaning the FileAlarm class code. Although thanks to reflection we can avoid changing any class code.
	 * In this test we just need to change the value for the URL we want to access into an invalid one.
	 * @throws SensorConnectionProblemException
	 * @throws IncorrectDataException
	 * @throws ClassNotFoundException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws ConfigurationFileProblemException
	 * @throws DatabaseProblemException
	 */

	@Test
	public void testNotValidURL() throws SensorConnectionProblemException, IncorrectDataException,
			ClassNotFoundException, NoSuchFieldException, SecurityException, IllegalArgumentException,
			IllegalAccessException, ConfigurationFileProblemException, DatabaseProblemException {

		FireAlarm privateFireAlarm = new FireAlarm();
		Field privateSensorsField = FireAlarm.class.getDeclaredField("sensors");
		privateSensorsField.setAccessible(true);
		HashMap<String, String> fieldValue = (HashMap<String, String>) privateSensorsField.get(privateFireAlarm);
		System.out.println(fieldValue);
		fieldValue.put("kitchen", "http://www.grise.upm.es/profundizacion/not_working");

		assertThrows(SensorConnectionProblemException.class, () -> {
			privateFireAlarm.isTemperatureTooHigh();
		}, "Expected throw when non valid url");
	}

	/**
	 * This is the base testing provided by the forked testing code.
	 * @throws SensorConnectionProblemException
	 * @throws IncorrectDataException
	 */
	@Test
	public void testTemperatureBelow80() throws SensorConnectionProblemException, IncorrectDataException {
		assertFalse(this.fireAlarm.isTemperatureTooHigh());
	}

	/**
	 * This could have been tested by chaning the FileAlarm class code. Although thanks to reflection we can avoid changing any class code.
	 * This is quite a special case for testing as its bound with a REST API call. To achieve a good mocking testing workflow we will need to boot up a testing webserver.
	 * In this test we just need to change the value for the URL we want to access into the testing webserver we have currently created specifically for this test.
	 * @throws SensorConnectionProblemException
	 * @throws IncorrectDataException
	 * @throws IOException
	 * @throws ConfigurationFileProblemException
	 * @throws DatabaseProblemException
	 * @throws NoSuchFieldException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */

	@Test
	public void testNotValidURLResponse() throws SensorConnectionProblemException, IncorrectDataException, IOException,
			ConfigurationFileProblemException, DatabaseProblemException, NoSuchFieldException, SecurityException,
			IllegalArgumentException, IllegalAccessException {
		MockWebServer serviceMock = new MockWebServer();
		serviceMock.start();
		String url_path = "/v1/kitchen/";
		serviceMock.url(url_path);
		serviceMock.enqueue(new MockResponse()
				.addHeader("Content-Type", "application/json")
				.setBody("{\"temperature\": 81}"));

		FireAlarm privateFireAlarm = new FireAlarm();
		Field privateSensorsField = FireAlarm.class.getDeclaredField("sensors");
		privateSensorsField.setAccessible(true);
		HashMap<String, String> fieldValue = (HashMap<String, String>) privateSensorsField.get(privateFireAlarm);
		fieldValue.put("kitchen", String.format("http://%s:%d%s", serviceMock.getHostName(), serviceMock.getPort(), url_path));

		assertTrue(privateFireAlarm.isTemperatureTooHigh());

		serviceMock.close();
	}

}

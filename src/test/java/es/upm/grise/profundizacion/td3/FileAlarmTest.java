package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
	 * Changed fireAlarm sensors to protected to mock the access into a bad endpoint.
	 * @throws SensorConnectionProblemException
	 */
	@Test
	public void testBadEndpoint() throws SensorConnectionProblemException {
		fireAlarm.sensors.put("kitchen", "http://localhost:8080/de_verdad_no_mas_practicas");
		assertThrows(SensorConnectionProblemException.class, () -> {
			fireAlarm.getTemperature("kitchen");
		});
	}
	
	/*
	 * ObjectMapper has been changed in source code from a local variable to a protected atribute of the class
	 * @throws IncorrectDataException
	 * @throws JsonProcessingException
	 */
	@Test
	public void JSONResponseNull() throws IncorrectDataException, JsonProcessingException{
		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
		when(mockObjectMapper.readTree(any(String.class))).thenReturn(null);

		fireAlarm.mapper = mockObjectMapper;
		assertThrows(IncorrectDataException.class, () -> {
			fireAlarm.getTemperature("kitchen");
		});
	}

	/*
	 * No dedicated changes for this test.
	 * @throws IncorrectDataException
	 * @throws JsonProcessingException
	 */
	@Test
	public void JSONResponseWithoutTemperature() throws IncorrectDataException, JsonProcessingException{
		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
		fireAlarm.mapper = mockObjectMapper;

		JsonNode mockNode = mock(JsonNode.class);
		when(mockObjectMapper.readTree(any(String.class))).thenReturn(mockNode);
		when(mockNode.get("temperature")).thenReturn(null);

		assertThrows(IncorrectDataException.class, () -> fireAlarm.getTemperature("kitchen"));
	}

	/*
	 * No dedicated changes for this test.
	 * @throws IncorrectDataException
	 * @throws JsonProcessingException
	 */
	@Test
	public void JSONResponseTemperatureNotInteger() throws IncorrectDataException, JsonProcessingException {
		ObjectMapper mockObjectMapper = mock(ObjectMapper.class);
		String jsonString = "\"temperature\":\"3_cts\"";
		JsonNode node = mockObjectMapper.readTree(jsonString);
		when(mockObjectMapper.readTree(any(String.class))).thenReturn(node);

		fireAlarm.mapper = mockObjectMapper;
		assertThrows(IncorrectDataException.class, () -> {
			fireAlarm.getTemperature("kitchen");
		});
	}
	
	/*
	 * No source code change needed. A mock is needed to handle if temperature is too high or not.
	 */
	@Test
	public void temperatureIsOk() {
		try {
			doReturn(15).when(fireAlarm).getTemperature(anyString());
			assertFalse(fireAlarm.isTemperatureTooHigh());
		} catch (SensorConnectionProblemException | IncorrectDataException e) {
			fail();
			e.printStackTrace();
		}
	}
	
	/*
	 * No source code change needed, only a mock.
	 */
	@Test
	public void temperatureIsTooHigh() {
		try {
			doReturn(100).when(fireAlarm).getTemperature(anyString());
			assertTrue(fireAlarm.isTemperatureTooHigh());
		} catch (SensorConnectionProblemException | IncorrectDataException e) {
			fail();
			e.printStackTrace();
		}
	}
}

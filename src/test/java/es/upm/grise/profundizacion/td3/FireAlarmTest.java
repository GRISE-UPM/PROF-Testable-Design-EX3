package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.text.MessageFormat;

// Change made to FireAlarm:
// Made a setter for objectMapper in order to be able to insert a mocked version.
// objectMapper was also made into a privat global variable
// Made a function called sqlQuery for testing queries to the database
// Made dblocation a global private value in order to easily use sqlQuery
// Made getTemperature public in order to be able to test it

@ExtendWith(SystemStubsExtension.class)
public class FireAlarmTest {
	// Permits substituting the value in the config file
	private void setConfigFile(String newContent) throws IOException {
		String filePath = Paths.get("resources", "config.properties").toString();

		try (FileWriter writer = new FileWriter(filePath)) {
			writer.write(newContent);
		}
	}

	// Restores the config file to its proper state. Should work on different OSs and independent on where it runs
	private void restoreConfigFile() throws IOException {
		String separator = File.separator;
		if (separator.equals("\\"))
			separator = "\\\\";
		setConfigFile(MessageFormat.format("dblocation=jdbc:sqlite:{0}{1}{2}{1}{3}",getProjectDirectory(),separator,"resources","sensors.db"));
	}


	// Finds out the folder of the project being executed
	private String getProjectDirectory() {
		String directory = System.getProperty("user.dir");
		return directory.replace("\\", "\\\\");
	}



	@SystemStub
	private EnvironmentVariables environmentVariables = new EnvironmentVariables();
	FireAlarm fireAlarm;

	@BeforeEach
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException, IOException {

		restoreConfigFile();
		environmentVariables.set("firealarm.location",getProjectDirectory());
		fireAlarm = spy(new FireAlarm());
	}


	@Test
	public void confFiletest() {

		environmentVariables.set("firealarm.location","WrongPath");
		assertThrows(ConfigurationFileProblemException.class, () -> new FireAlarm());
		environmentVariables.set("firealarm.location",getProjectDirectory());
		assertDoesNotThrow(() -> new FireAlarm());

	}

	@Test
	public void dataBaseErrors() throws IOException, DatabaseProblemException, ConfigurationFileProblemException {
		environmentVariables.set("firealarm.location",getProjectDirectory());
		setConfigFile("dblocation=jdbc:sqlite:WrongPath");
		assertThrows(DatabaseProblemException.class, FireAlarm::new);
		restoreConfigFile();
		assertThrows(DatabaseProblemException.class, () -> fireAlarm.sqlQuery("SELECT * FROM sensorss"));
		assertDoesNotThrow(() -> fireAlarm.sqlQuery("SELECT * FROM sensors"));
	}

	@Test
	public void getTemperatureTest() {
		assertThrows(SensorConnectionProblemException.class, ()-> fireAlarm.getTemperature("NotREAL"));
		assertDoesNotThrow(() -> fireAlarm.getTemperature("kitchen"));
	}

	@Test
	public void lowTempertureTest() {
		try {
			doReturn(5).when(fireAlarm).getTemperature(anyString());
			assertFalse(fireAlarm.isTemperatureTooHigh());
		} catch (SensorConnectionProblemException | IncorrectDataException e) {
			assert false;
		}
	}
	@Test
	public void highTempertureTest() {
		try {
			doReturn(85).when(fireAlarm).getTemperature(anyString());
			assertTrue(fireAlarm.isTemperatureTooHigh());
		} catch (SensorConnectionProblemException | IncorrectDataException e) {
			assert false;
		}
	}

	@Test
	public void testGetTemperatureWithInvalidJson() throws Exception {
		// Mock URL and ObjectMapper
		ObjectMapper mockedMapper = mock(ObjectMapper.class);
		fireAlarm.setObjectMapper(mockedMapper);

		// Prepare JSON node
		JsonNode mockedResult = mock(JsonNode.class);

		// Scenario 1: JSON object (result) itself is null
		when(mockedMapper.readTree(any(URL.class))).thenReturn(null);
		assertThrows(IncorrectDataException.class, () -> fireAlarm.getTemperature("kitchen"));

		// Scenario 2: JSON object does not contain the "temperature" key
		when(mockedMapper.readTree(any(URL.class))).thenReturn(mockedResult);
		when(mockedResult.get("temperature")).thenReturn(null);
		assertThrows(IncorrectDataException.class, () -> fireAlarm.getTemperature("kitchen"));

		// Scenario 3: The value of "temperature" key is not an integer
		JsonNode temperatureNode = mock(JsonNode.class);
		when(mockedResult.get("temperature")).thenReturn(temperatureNode);
		when(temperatureNode.canConvertToInt()).thenReturn(false);
		assertThrows(IncorrectDataException.class, () -> fireAlarm.getTemperature("kitchen"));
	}



}
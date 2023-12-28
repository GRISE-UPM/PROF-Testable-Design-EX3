package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;

public class FileAlarmTest {
	
	FireAlarm fireAlarm;
	
	@BeforeEach
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException {
		fireAlarm = new FireAlarm();
	}
	
	@Test
	public void test() throws SensorConnectionProblemException, IncorrectDataException {
		assertFalse(fireAlarm.isTemperatureTooHigh());
	}

	@SystemStub
	private EnvironmentVariables environmentVariables = new EnvironmentVariables("firealarm.location", System.getProperty("user.dir"));

	@Test
	public void testConfigurationFileProblemException() throws ConfigurationFileProblemException {
		environmentVariables.set("firealarm.location", null);
		assertThrows(ConfigurationFileProblemException.class, () -> new FireAlarm());
	}

	@Test
	public void testDatabaseProblemException() throws DatabaseProblemException, IOException {
		String dir = Files.createTempDirectory("tmp").toFile().getAbsolutePath();
		Path tmp = Paths.get(dir);
		Path resources = Files.createDirectory(tmp.resolve("resources"));
		File config = new File(resources + "/config.properties");
		config.createNewFile();
		FileWriter writer = new FileWriter(config);
		writer.write("dblocation = auxiliar");
		writer.close();

		environmentVariables.set("firealarm.location", tmp.toString());
		assertThrows(ConfigurationFileProblemException.class,()-> new FireAlarm());
	}

	@Test
	public void testSensorConnectionProblemException() throws SensorConnectionProblemException {
		assertThrows(SensorConnectionProblemException.class, () -> fireAlarm.getTemperature("30"));
	}

	@Test
	public void testIncorrectDataException() throws SensorConnectionProblemException, IncorrectDataException {
		ObjectMapper mockMapper = new mock(ObjectMapper.class);
		when(mockMapper.readTree(anyString())).thenReturn(null);
		fireAlarm.mapper = mockMapper;
		assertThrows(IncorrectDataException.class, () -> fireAlarm.isTemperatureTooHigh());
	}

	@Test
	public void testIsTemperatureTooHigh() throws SensorConnectionProblemException, IncorrectDataException {
		try{
			ObjectMapper mockMapper = new mock(ObjectMapper.class);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.readTree("{\"temperature\": 30}");
			when(mockMapper.readTree(anyString())).thenReturn(node);
			fireAlarm.mapper = mockMapper;
			assertFalse(fireAlarm.isTemperatureTooHigh());
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
		
	}

		@Test
	public void testIsTemperatureTooHighTrue() throws SensorConnectionProblemException, IncorrectDataException {
		try{
			ObjectMapper mockMapper = new mock(ObjectMapper.class);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.readTree("{\"temperature\": 120}");
			when(mockMapper.readTree(anyString())).thenReturn(node);
			fireAlarm.mapper = mockMapper;
			assertFalse(fireAlarm.isTemperatureTooHigh());
		} catch (Exception e) {
			fail();
			e.printStackTrace();
		}
		
	}

}

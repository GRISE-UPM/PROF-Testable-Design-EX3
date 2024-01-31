package es.upm.grise.profundizacion.td3;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.junit.jupiter.api.Assertions.*;

import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;



@ExtendWith(SystemStubsExtension.class)
public class FireAlarmTest {
	@SystemStub
	private EnvironmentVariables environmentVariables = new EnvironmentVariables();
	FireAlarm fireAlarm;


	@BeforeEach
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException {
		environmentVariables.set("firealarm.location","C:\\Users\\Andrés\\IdeaProjects\\PROF-Testable-Design-EX3");
		fireAlarm = spy(new FireAlarm());
	}

	@Test
	public void normalConditionsTest() throws SensorConnectionProblemException, IncorrectDataException {
		System.out.println("Test 1");
		assertDoesNotThrow(FireAlarm::new);
		System.out.println("------------------------------------------------------------------------------------------------------------------------------");
	}
	@Test
	public void badLocationTest() throws SensorConnectionProblemException, IncorrectDataException {
		System.out.println("Test 2");
		environmentVariables.set("firealarm.location","Direccion Falsa");
		assertThrows(ConfigurationFileProblemException.class,FireAlarm::new);
		System.out.println("------------------------------------------------------------------------------------------------------------------------------");
	}
	@Test
	public void badDBLocationTest() throws Exception {
		System.out.println("Test 3");
		Path configFilePath = Path.of("C:\\Users\\Andrés\\IdeaProjects\\PROF-Testable-Design-EX3\\resources\\config.properties");
		String originalContent = Files.readString(configFilePath);
		try {
			String invalidContent = "dblocation=valor_invalido";
			Files.writeString(configFilePath, invalidContent);
			assertThrows(DatabaseProblemException.class, FireAlarm::new);
		} finally {
			Files.writeString(configFilePath, originalContent);
		}
		System.out.println("------------------------------------------------------------------------------------------------------------------------------");
	}

	@Test
	public void goodQueryTest() throws DatabaseProblemException {
		System.out.println("Test 4");
		assertDoesNotThrow(()->fireAlarm.sqlQuery("SELECT * FROM sensors"));
		System.out.println("------------------------------------------------------------------------------------------------------------------------------");
	}
	@Test
	public void badQueryTest() throws DatabaseProblemException {
		System.out.println("Test 5");
		assertThrows(DatabaseProblemException.class ,()->fireAlarm.sqlQuery(""));
		System.out.println("------------------------------------------------------------------------------------------------------------------------------");
	}

	@Test
	public void goodEndpointTest() throws ConfigurationFileProblemException, DatabaseProblemException {
		System.out.println("Test 6");
		assertDoesNotThrow(fireAlarm::isTemperatureTooHigh);
		System.out.println("------------------------------------------------------------------------------------------------------------------------------");
	}


	@Test
	public void badEndpointTest() {
		System.out.println("Test 7");
		HashMap<String, String> mockSensors= spy(new HashMap<String, String>());
		when(mockSensors.get(anyString())).thenReturn("http:falso");
		fireAlarm.sensors=mockSensors;
		assertThrows(SensorConnectionProblemException.class ,()->fireAlarm.getTemperature("kitchen"));
		System.out.println("------------------------------------------------------------------------------------------------------------------------------");
	}

	@Test
	public void badJsonTest1() throws Exception{
		System.out.println("Test 7");
		ObjectMapper mockMapper= mock(ObjectMapper.class);
		fireAlarm.mapper = mockMapper;
		when(mockMapper.readTree(any(URL.class))).thenReturn(null);
		assertThrows(IncorrectDataException.class,() -> fireAlarm.getTemperature("kitchen"));
		System.out.println("------------------------------------------------------------------------------------------------------------------------------");
	}

	@Test
	public void badJsonTest2() throws Exception{
		System.out.println("Test 8");

		ObjectMapper mockMapper= mock(ObjectMapper.class);
		fireAlarm.mapper = mockMapper;
		JsonNode mockResult= mock(JsonNode.class);

		when(mockMapper.readTree(any(URL.class))).thenReturn(mockResult);
		when(mockResult.get("temperature")).thenReturn(null);

		assertThrows(IncorrectDataException.class,() -> fireAlarm.getTemperature("kitchen"));

		System.out.println("------------------------------------------------------------------------------------------------------------------------------");
	}

	@Test
	public void badJsonTest3() throws Exception{
		System.out.println("Test 9");

		ObjectMapper mockMapper= mock(ObjectMapper.class);
		fireAlarm.mapper = mockMapper;
		JsonNode mockResult= mock(JsonNode.class);
		JsonNode mockTemperature= mock(JsonNode.class);

		when(mockMapper.readTree(any(URL.class))).thenReturn(mockResult);
		when(mockResult.get("temperature")).thenReturn(mockTemperature);
		when(mockTemperature.canConvertToInt()).thenReturn(false);
		assertThrows(IncorrectDataException.class,() -> fireAlarm.getTemperature("kitchen"));

		System.out.println("------------------------------------------------------------------------------------------------------------------------------");
	}

	@Test
	public void everyTemperatureTest() throws Exception{
		System.out.println("Test 10");
		doReturn(10).when(fireAlarm).getTemperature(anyString());
		assertFalse(fireAlarm.isTemperatureTooHigh());
		System.out.println("------------------------------------------------------------------------------------------------------------------------------");
	}

	@Test
	public void anyTemperatureTest() throws Exception{
		System.out.println("Test 11");
		doReturn(100).when(fireAlarm).getTemperature(anyString());
		assertTrue(fireAlarm.isTemperatureTooHigh());
		System.out.println("------------------------------------------------------------------------------------------------------------------------------");
	}

}
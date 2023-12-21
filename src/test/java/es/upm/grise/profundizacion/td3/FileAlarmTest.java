package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
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
	
	@Test
	public void testConfigurationFileProblemException(){
		envVariables.set("firealarm.location", null);

		assertThrows(ConfigurationFileProblemException.class, () -> {
			new FireAlarm();
		});
	}

	@Test
	public void testDatabaseProblemException(@TempDir Path tmpDir) throws IOException{
		Files.createDirectories(tmpDir.resolve("resources"));
		File tmpConf = new File(tmpDir.resolve("resources") + "/config.properties");
		tmpConf.createNewFile();

		//Create a filewirte in a try with resources to close it automatically and is buffered
		try(FileWriter escritor = new FileWriter(tmpConf)) {
			escritor.write("dblocation=foo");
		}

		envVariables.set("firealarm.location", tmpDir.toString());

		assertThrows(DatabaseProblemException.class, () -> {
			new FireAlarm();
		});
	}

	@Test
	public void testSensorConnectionProblemException(){
		assertThrows(SensorConnectionProblemException.class, ()->{
			fireAlarm.getTemperature("33");
		});
	}

	@Test
	public void testSensorConnectionProblemException2(){
		fireAlarm.sensors.put("bathroom", "https://google.com");
		assertThrows(SensorConnectionProblemException.class, ()->{
			fireAlarm.getTemperature("bathroom");
		});
	}

	@Test
	public void testIncorrectDataException() throws JsonMappingException, JsonProcessingException{
		ObjectMapper mockMapper = mock(ObjectMapper.class);
		when(mockMapper.readTree(anyString())).thenReturn(null);
		
		fireAlarm.setMapper(mockMapper);
		assertThrows(IncorrectDataException.class, ()->{
			fireAlarm.getTemperature("kitchen");
		});
	}

	@Test
	public void testIncorrectDataException2() throws JsonMappingException, JsonProcessingException{
		ObjectMapper mockMapper = mock(ObjectMapper.class);
		JsonNode mockNode = mock(JsonNode.class);
		when(mockMapper.readTree(anyString())).thenReturn(mockNode);
		when(mockNode.get("temperature")).thenReturn(null);		
		
		fireAlarm.setMapper(mockMapper);
		assertThrows(IncorrectDataException.class, ()->{
			fireAlarm.getTemperature("kitchen");
		});
	}

	@Test
	public void testIncorrectDataException3() throws JsonMappingException, JsonProcessingException{
		ObjectMapper mockMapper = mock(ObjectMapper.class);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node=mapper.readTree("{\"temperature\": \"foo\"}");
		when(mockMapper.readTree(anyString())).thenReturn(node);	
		
		fireAlarm.setMapper(mockMapper);
		assertThrows(IncorrectDataException.class, ()->{
			fireAlarm.getTemperature("kitchen");
		});
	}

	@Test
	public void testIsTemperatureTooHigh() throws JsonMappingException, JsonProcessingException{
		ObjectMapper mockMapper = mock(ObjectMapper.class);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node=mapper.readTree("{\"temperature\": 33}");	
		
		try{
			when(mockMapper.readTree(any(URL.class))).thenReturn(node);
			fireAlarm.setMapper(mockMapper);
			assertFalse(fireAlarm.isTemperatureTooHigh());
		}catch(Exception e){
			fail();
			e.printStackTrace();
		}
	}

	@Test
	public void testIsTemperatureTooHigh2() throws JsonMappingException, JsonProcessingException{
		ObjectMapper mockMapper = mock(ObjectMapper.class);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node=mapper.readTree("{\"temperature\": 115}");	
		
		try{
			when(mockMapper.readTree(any(URL.class))).thenReturn(node);
			fireAlarm.setMapper(mockMapper);
			assertTrue(fireAlarm.isTemperatureTooHigh());
		}catch(Exception e){
			fail();
			e.printStackTrace();
		}
	}

}

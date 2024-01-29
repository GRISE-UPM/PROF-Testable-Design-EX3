package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
	private EnvironmentVariables variables = new EnvironmentVariables("firealarm.location", System.getProperty("user.dir"));
	
	@BeforeEach
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException {
		fireAlarm = new FireAlarm();
	}
	
	@Test
	public void noLocalizarFicheroConfigTest() throws ConfigurationFileProblemException {
		variables.set("firealarm.location", null);
		assertThrows(ConfigurationFileProblemException.class, () -> new FireAlarm());
	}
	
	
	@Test
	public void baseDeDatosTest(@TempDir Path tmpDir) throws IOException, DatabaseProblemException{
		Files.createDirectories(tmpDir.resolve("resources"));
		File tmpConf = new File(tmpDir.resolve("resources") + "/config.properties");
		tmpConf.createNewFile();
		FileWriter escritor = new FileWriter(tmpConf);
		escritor.write("dblocation=null");
		variables.set("firealarm.location", tmpDir.toString());
		assertThrows(DatabaseProblemException.class, () -> new FireAlarm());
		escritor.close();
	}
	

	
	@Test
	public void endpointNoUtilizableTest() throws SensorConnectionProblemException{
		fireAlarm.sensors.put("habitacion", "http://www.enlacenovalido.com");
		assertThrows(SensorConnectionProblemException.class, () -> fireAlarm.getTemperature("habitacion"));
	}
	
	@Test
	public void respuestaSinTemperaturaTest() throws IncorrectDataException, JsonProcessingException{
		ObjectMapper om = mock(ObjectMapper.class);
		fireAlarm.map = om;
		JsonNode nodoMock = mock(JsonNode.class);
		when(om.readTree(any(String.class))).thenReturn(nodoMock);
		when(nodoMock.get("temperature")).thenReturn(null);
		assertThrows(IncorrectDataException.class, () -> fireAlarm.getTemperature("habitacion"));
	}
	
	@Test
	public void temperatureIsOk() throws SensorConnectionProblemException, IncorrectDataException {
		doReturn(100).when(fireAlarm.getTemperature(anyString()));
		assertFalse(fireAlarm.isTemperatureTooHigh());		
	}
	
	@Test
	public void testIsTemperatureTooHigh2() throws IncorrectDataException, SensorConnectionProblemException{
		FireAlarm mockFireAlarm = mock(FireAlarm.class);
		when(mockFireAlarm.getTemperature(anyString())).thenReturn(100);
		assertTrue(mockFireAlarm.isTemperatureTooHigh());

	}
	

}

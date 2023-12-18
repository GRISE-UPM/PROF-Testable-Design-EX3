package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

// Método getTemperature hecho publico
// Variable MAX_TEMPERATURE hecha pública y static
// ObjectMapper mapper puesto en el scope de clase para poder hacer mock 

@ExtendWith(SystemStubsExtension.class)
public class FileAlarmTest {

	@SystemStub
	private EnvironmentVariables env = new EnvironmentVariables();

	FireAlarm fireAlarm;

	@BeforeEach
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException {
		env.set("firealarm.location", System.getProperty("user.dir"));
		fireAlarm = Mockito.spy(new FireAlarm());
	}

	@Test
	public void ConfigurationFileProblemIsThrownTest() 
	{
		env.set("firealarm.location", "dummy");
		assertThrows(ConfigurationFileProblemException.class, () -> new FireAlarm());
	}

	@Test
	public void DatabaseProblemExceptionIsThrown() throws IOException{
		Path filePath = Paths.get(System.getProperty("user.dir")+ "/resources/config.properties");
		String originalContent = new String(Files.readAllBytes(filePath));
		
		FileWriter fw = new FileWriter(filePath.toString(), false);
		fw.write("jdbc:sqlite:randomPath");
		fw.close();
		
		assertThrows(DatabaseProblemException.class, () -> new FireAlarm());
		
		// Restore original db config
		FileWriter fw2 = new FileWriter(filePath.toString(), false);
		fw2.write(originalContent);
		fw2.close();
		
	}

	@Test
	public void SensorConnectionProblemExceptionIsThrown() 
	{
		assertThrows(SensorConnectionProblemException.class, () -> new FireAlarm().getTemperature("dummy"));
	}

	@Test
	public void IncorrectDataExceptionIsThrown() throws IOException 
	{
		fireAlarm.mapper = Mockito.mock(ObjectMapper.class);

		// No temperature key
		JsonNode mockedResult = Mockito.mock(JsonNode.class);
		Mockito.when(fireAlarm.mapper.readTree(Mockito.any(URL.class))).thenReturn(mockedResult);
		Mockito.when(mockedResult.get("temperature")).thenReturn(null);
		assertThrows(IncorrectDataException.class,
				() -> fireAlarm.getTemperature("kitchen")); //kitchen is only key in .db?
		
		// Key value is not an integer
		JsonNode temperature = Mockito.mock(JsonNode.class);
		Mockito.when(mockedResult.get("temperature")).thenReturn(temperature);
		Mockito.when(temperature.canConvertToInt()).thenReturn(false);
		assertThrows(IncorrectDataException.class,
				() -> fireAlarm.getTemperature("kitchen"));
		
	}

	@Test
	public void isTemperatureTooLowTest() throws SensorConnectionProblemException, IncorrectDataException 
	{
		Mockito.doReturn(fireAlarm.MAX_TEMPERATURE - 1).when(fireAlarm).getTemperature(Mockito.anyString());
		assertFalse(fireAlarm.isTemperatureTooHigh());
	}

	@Test
	public void isTemperatureTooHighTest() throws SensorConnectionProblemException, IncorrectDataException 
	{
		Mockito.doReturn(fireAlarm.MAX_TEMPERATURE + 1).when(fireAlarm).getTemperature(Mockito.anyString());
		assertTrue(fireAlarm.isTemperatureTooHigh());
	}

}

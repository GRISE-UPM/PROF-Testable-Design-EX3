package es.upm.grise.profundizacion.td3;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
public class FireAlarmTest {
	@SystemStub
	private EnvironmentVariables environmentVariable=new EnvironmentVariables();
	FireAlarm fireAlarm;

	private Path currentPath = Paths.get("").toAbsolutePath();
	private String texto=currentPath.toString();
	private int lastIndex = texto.lastIndexOf("PROF-Testable-Design-EX3");
	private String path = texto.substring(0, lastIndex + "PROF-Testable-Design-EX3".length());
	private ObjectMapper mockMapper;


	@BeforeEach
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException, IOException {
		mockMapper = mock(ObjectMapper.class);
		path=path.replace("\\","\\\\");
		environmentVariable.set("firealarm.location",path);
		if(File.separator.equals("\\"))
			updateConfigFile("dblocation=jdbc:sqlite:" + path + File.separator  + File.separator + "resources" + File.separator + File.separator + "sensors.db");
		else
			updateConfigFile("dblocation=jdbc:sqlite:" + path + File.separator + "resources" + File.separator + "sensors.db");
		fireAlarm = new FireAlarm();
		fireAlarm.setMap(mockMapper);
	}


	@Test
	public void confFileErrorTest(){
		environmentVariable.set("firealarm.location","path");
		assertThrows(ConfigurationFileProblemException.class, FireAlarm::new);
	}

	@Test
	public void dataBaseErrorTest() throws IOException, DatabaseProblemException, ConfigurationFileProblemException {
		updateConfigFile("dblocation=1");
		assertThrows(DatabaseProblemException.class, FireAlarm::new);
		assertThrows(DatabaseProblemException.class, () -> fireAlarm.consult(""));
	}

	@Test
	public void endpointErrorTest() throws IOException, DatabaseProblemException, ConfigurationFileProblemException {
		assertThrows(SensorConnectionProblemException.class, () -> fireAlarm.getTemperature("prueba"));
	}

	@Test
	public void invalidJsonTest() throws Exception {
		JsonNode mockResult = mock(JsonNode.class);

		// Case 1
		when(mockMapper.readTree(any(URL.class))).thenReturn(null);
		assertThrows(IncorrectDataException.class, () -> fireAlarm.getTemperature("kitchen"));

		// Case 2
		when(mockMapper.readTree(any(URL.class))).thenReturn(mockResult);
		when(mockResult.get("temperature")).thenReturn(null);
		assertThrows(IncorrectDataException.class, () -> fireAlarm.getTemperature("kitchen"));

		// Case 3
		JsonNode temperature = mock(JsonNode.class);
		when(mockResult.get("temperature")).thenReturn(temperature);
		when(temperature.canConvertToInt()).thenReturn(false);
		assertThrows(IncorrectDataException.class, () -> fireAlarm.getTemperature("kitchen"));
	}

	@Test
	public void tempetureLowtest() throws Exception,SensorConnectionProblemException, IncorrectDataException, DatabaseProblemException, ConfigurationFileProblemException {
		FireAlarm alarm = spy(FireAlarm.class);
		doReturn(1).when(alarm).getTemperature(anyString());
		assertFalse(alarm.isTemperatureTooHigh());
	}

	@Test
	public void tempetureHightest() throws Exception,SensorConnectionProblemException, IncorrectDataException, DatabaseProblemException, ConfigurationFileProblemException {
		FireAlarm alarm = spy(FireAlarm.class);
		doReturn(81).when(alarm).getTemperature(anyString());
		assertTrue(alarm.isTemperatureTooHigh());
	}

	public void updateConfigFile(String newContent) throws IOException {
		String filePath = Paths.get("resources", "config.properties").toString();

		try (FileWriter writer = new FileWriter(filePath)) {
			writer.write(newContent);
		}
	}
	/*Sección de comentarios
	Los cambios hechos en la clase FireAlarm son:
	La creación de la variable private ObjectMapper mapper=new ObjectMapper(); para poder crear un mock en los tet y así poder probar InvalidJsonTest.
	También se ha creado el método setMapper para setear el ObjectMapper de fireAlarm y el método consult para poder realizar consultas a la base de datos,*/

}

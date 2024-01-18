package es.upm.grise.profundizacion.td3;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
	private EnvironmentVariables environmentVariables;
	
	@BeforeEach
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException {
		environmentVariables = new EnvironmentVariables("firealarm.location", System.getProperty("user.dir"));
		fireAlarm = new FireAlarm();
	}
	
	// Test de que si no se encuentra el fichero config.properties se lanza una excepcion
	@Test
	public void testConfigurationFileProblemException() throws ConfigurationFileProblemException, DatabaseProblemException {
		
		// Hacemos que la variable de entorno firealarm.location  devuelva null
		environmentVariables.set("firealarm.location", null);
		
		assertThrows(ConfigurationFileProblemException.class, () -> new FireAlarm());
	}

	// Test de que un error en la base de datos devuelve una excepcion DatabaseProblemException
	@Test
	public void testDatabaseProblemException() throws DatabaseProblemException, IOException {

		// Se usa la ruta de un directorio de testeo 
		String dbTesteo = System.getProperty("user.dir")+"/reources/testResources";

		environmentVariables.set("firealarm.location", dbTesteo);
		
		assertThrows(DatabaseProblemException.class, () -> new FireAlarm());
	}
	
	
	// Test para comprobar que si el endopoint es incorrecto lanza una excepcion SensorConnectionProblemException 
	@Test
	public void testSensorConnectionProblemExceptionWrong() throws SensorConnectionProblemException {
		
		//La variable sensors se ha cambiado a packager para poder usarla desde el test
		fireAlarm.sensors.put("room", "incorrecto");
		
		assertThrows(SensorConnectionProblemException.class, () -> fireAlarm.getTemperature("room"));
	}


	//Test para comprobar que si el json no tiene la clave "temperature" se lanza una excepcion
	@Test
	public void testIncorrectDataExceptionNoTemperature() throws IncorrectDataException, JsonProcessingException {
		
		//El ObjestMapper se ha cambiado a pakcage para poder usarla desde el test
		ObjectMapper mapper = mock(ObjectMapper.class);
		JsonNode json = mock(JsonNode.class);
		
		when(mapper.readTree(anyString())).thenReturn(json);
		when(json.get("temperature")).thenReturn(null);

		fireAlarm.mapper = mapper;
		assertThrows(IncorrectDataException.class, () -> fireAlarm.getTemperature("room"));
	}

	// Test para comprobar que si el json no tiene un entero se lanza una excepcion
	@Test
	public void testIncorrectDataExceptionNotInteger() throws IncorrectDataException, JsonProcessingException {
		ObjectMapper mapper = mock(ObjectMapper.class);
		JsonNode json = mapper.readTree("temperatura");
		
		when(mapper.readTree(anyString())).thenReturn(json);

		fireAlarm.mapper = mapper;
		assertThrows(IncorrectDataException.class, () -> fireAlarm.getTemperature("room"));
	}

	// Test para comprobar el metodo isTemperatureTooHigh, si es más de 80 deberia devolver true, si no false.
	@Test
	public void testIsTemperature() throws SensorConnectionProblemException, IncorrectDataException {

		// Utilizamos spys en vez de mocks para poder manipular el resultado de getTemperature pero poder usar el isTemperatureTooHigh
		FireAlarm spy = spy(FireAlarm.class);
		
		// 81 > 80
		doReturn(81).when(spy).getTemperature(anyString());
		assertTrue(spy.isTemperatureTooHigh());
		
		// 79 < 80
		doReturn(79).when(spy).getTemperature(anyString());
		assertFalse(spy.isTemperatureTooHigh());


	}
}
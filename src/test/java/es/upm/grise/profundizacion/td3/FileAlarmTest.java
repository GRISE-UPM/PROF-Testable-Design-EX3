package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;

import es.upm.grise.profundizacion.td3.exceptions.ConfigurationFileProblemException;
import es.upm.grise.profundizacion.td3.exceptions.DatabaseProblemException;
import es.upm.grise.profundizacion.td3.exceptions.IncorrectDataException;
import es.upm.grise.profundizacion.td3.exceptions.SensorConnectionProblemException;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

// ------------ Cambios realizados --------------------------------------------------
// Se ha reducido la responsabilidad de la clase FireAlarm, en particular, la gestión
// y ejecución de las peticiones a la base de datos directamente.
// Ahora, el constructor de FireAlarm recibe una interfaz DBConnector, que proporciona
// las funcionalidades de conexión (connect()), carga de información de los sensores (getSensors()),
// y cierre de conexión (closeConnection())
// La gestión y ejecución de las peticiones a la base de datos ahora es gestionada por la clase
// DBConnectorImpl, que implementa DBConnector.
// Se ha añadido el método setMapper() en la clase FireAlarm, que permite inyección del ObjectMapper empleado en el método getTemperature()
// Se ha añadido el método getMaxTemperature() en la clase FireAlarm, que permite devolver el valor máximo a partir del cual se considera una temperatura demasiado elevada.


@ExtendWith(SystemStubsExtension.class)
@ExtendWith(MockitoExtension.class)
public class FileAlarmTest {

	@SystemStub
	EnvironmentVariables environmentVars;

	@Mock
	DBConnector dbConnector;

	@Mock
	ObjectMapper mapper;


	@Test
	public void whenConfigFileNotFound_throwException() throws ConfigurationFileProblemException, DatabaseProblemException {
		environmentVars.set("firealarm.location", "Invalid");
		assertThrows(ConfigurationFileProblemException.class, () -> new FireAlarm(dbConnector));
	}

	@Test
	public void whenErrorOnConnection_throwException() throws DatabaseProblemException {
		String basePath = System.getProperty("user.dir");
		environmentVars.set("firealarm.location", basePath);
		doThrow(DatabaseProblemException.class)
			.when(dbConnector).connect(anyString());	
		assertThrows(DatabaseProblemException.class, () -> new FireAlarm(dbConnector));
	}

	@Test
	public void whenErrorOnGettingSensors_throwException() throws DatabaseProblemException {
		String basePath = System.getProperty("user.dir");
		environmentVars.set("firealarm.location", basePath);
		when(dbConnector.getSensors())
			.thenThrow(DatabaseProblemException.class);	
		assertThrows(DatabaseProblemException.class, () -> new FireAlarm(dbConnector));
	}


	@Test
	public void whenErrorOnClosingConnection_throwException() throws DatabaseProblemException {
		String basePath = System.getProperty("user.dir");
		environmentVars.set("firealarm.location", basePath);
		doThrow(DatabaseProblemException.class)
			.when(dbConnector).closeConnection();
		assertThrows(DatabaseProblemException.class, () -> new FireAlarm(dbConnector));
	}


	@Test
	public void whenEndpointURLInvalid_throwException() throws DatabaseProblemException, ConfigurationFileProblemException {
		String basePath = System.getProperty("user.dir");
		environmentVars.set("firealarm.location", basePath);
		Map<String,String> sensors = new HashMap<>();
		sensors.put("room1", "INVALID");
		when(dbConnector.getSensors())
			.thenReturn(sensors);
		FireAlarm fireAlarm = new FireAlarm(dbConnector);
		assertThrows(SensorConnectionProblemException.class, () -> fireAlarm.isTemperatureTooHigh());
	}

	@Test
	public void whenMappingError_throwException() throws DatabaseProblemException, ConfigurationFileProblemException, IOException {
		String basePath = System.getProperty("user.dir");
		environmentVars.set("firealarm.location", basePath);
		Map<String,String> sensors = new HashMap<>();
		sensors.put("room1", "https://www.example.com");
		when(dbConnector.getSensors())
			.thenReturn(sensors);
		when(mapper.readTree(any(URL.class)))
			.thenThrow(IOException.class);
		FireAlarm fireAlarm = new FireAlarm(dbConnector);
		fireAlarm.setMapper(mapper);
		assertThrows(SensorConnectionProblemException.class, () -> fireAlarm.isTemperatureTooHigh());
	}

	@Test
	public void whenJsonNull_throwException() throws DatabaseProblemException, ConfigurationFileProblemException, IOException {
		String basePath = System.getProperty("user.dir");
		environmentVars.set("firealarm.location", basePath);
		Map<String,String> sensors = new HashMap<>();
		sensors.put("room1", "https://www.example.com");
		when(dbConnector.getSensors())
			.thenReturn(sensors);
		when(mapper.readTree(any(URL.class)))
			.thenReturn(null);
		FireAlarm fireAlarm = new FireAlarm(dbConnector);
		fireAlarm.setMapper(mapper);
		assertThrows(IncorrectDataException.class, () -> fireAlarm.isTemperatureTooHigh());
	}

	@Test
	public void whenJsonDoesNotContainTemperature_throwException() throws DatabaseProblemException, ConfigurationFileProblemException, IOException {
		String basePath = System.getProperty("user.dir");
		environmentVars.set("firealarm.location", basePath);
		Map<String,String> sensors = new HashMap<>();
		JsonNode response = JsonNodeFactory.instance.objectNode().put("node", "");
		sensors.put("room1", "https://www.example.com");
		when(dbConnector.getSensors())
			.thenReturn(sensors);
		when(mapper.readTree(any(URL.class)))
			.thenReturn(response);
		FireAlarm fireAlarm = new FireAlarm(dbConnector);
		fireAlarm.setMapper(mapper);
		assertThrows(IncorrectDataException.class, () -> fireAlarm.isTemperatureTooHigh());
	}


	@Test
	public void whenJsonDoesNotContainNumericTemperature_throwException() throws DatabaseProblemException, ConfigurationFileProblemException, IOException {
		String basePath = System.getProperty("user.dir");
		environmentVars.set("firealarm.location", basePath);
		Map<String,String> sensors = new HashMap<>();
		JsonNode response = JsonNodeFactory.instance.objectNode().put("temperature", "as2d");
		sensors.put("room1", "https://www.example.com");
		when(dbConnector.getSensors())
			.thenReturn(sensors);
		when(mapper.readTree(any(URL.class)))
			.thenReturn(response);
		FireAlarm fireAlarm = new FireAlarm(dbConnector);
		fireAlarm.setMapper(mapper);
		assertThrows(IncorrectDataException.class, () -> fireAlarm.isTemperatureTooHigh());
	}

	@Test
	public void whenAllSensorsDoNotExceedMaxTemp_returnFalse() throws DatabaseProblemException, ConfigurationFileProblemException, IOException, SensorConnectionProblemException, IncorrectDataException {
		String basePath = System.getProperty("user.dir");
		environmentVars.set("firealarm.location", basePath);
		Map<String,String> sensors = new HashMap<>();
		JsonNode response = JsonNodeFactory.instance.objectNode().put("temperature", 25);
		sensors.put("room1", "https://www.example.com");
		sensors.put("room2", "https://www.example.com");
		sensors.put("room3", "https://www.example.com");
		when(dbConnector.getSensors())
			.thenReturn(sensors);
		when(mapper.readTree(any(URL.class)))
			.thenReturn(response)
			.thenReturn(response)
			.thenReturn(response);
		FireAlarm fireAlarm = new FireAlarm(dbConnector);
		fireAlarm.setMapper(mapper);
		assertFalse(fireAlarm.isTemperatureTooHigh());
	}

	@Test
	public void whenOneSensorExceedsMaxTemp_returnTrue() throws DatabaseProblemException, ConfigurationFileProblemException, IOException, SensorConnectionProblemException, IncorrectDataException {
		String basePath = System.getProperty("user.dir");
		environmentVars.set("firealarm.location", basePath);
		Map<String,String> sensors = new HashMap<>();
		JsonNode response = JsonNodeFactory.instance.objectNode().put("temperature", 25);
		JsonNode exceedTempResponse = JsonNodeFactory.instance.objectNode().put("temperature", FireAlarm.getMaxTemperature() + 26);
		sensors.put("room1", "https://www.example.com");
		sensors.put("room2", "https://www.example.com");
		sensors.put("room3", "https://www.example.com");
		when(dbConnector.getSensors())
			.thenReturn(sensors);
		when(mapper.readTree(any(URL.class)))
			.thenReturn(response)
			.thenReturn(response)
			.thenReturn(exceedTempResponse);
		FireAlarm fireAlarm = new FireAlarm(dbConnector);
		fireAlarm.setMapper(mapper);
		assertTrue(fireAlarm.isTemperatureTooHigh());
	}




}

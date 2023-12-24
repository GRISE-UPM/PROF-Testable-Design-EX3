package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
public class FileAlarmTest {
	
	FireAlarm fireAlarm;
	String query;

	@SystemStub
	private EnvironmentVariables envVariables = new EnvironmentVariables(
		"firealarm.location", System.getProperty("user.dir")
	);
	
	
	@BeforeEach
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException {
		query = "SELECT * FROM SENSORS";
		fireAlarm = new FireAlarm(query);
	}
	
	/*
	 * a)
	 * When the config.properties file cannot be located, 
	 * the FireAlarm class throws a ConfigurationFileProblemException.
	 */
	@Test
	public void configurationFileNotFound() {
		envVariables.set("firealarm.location", "non-existing-file");

		assertThrows(ConfigurationFileProblemException.class, () -> {
			fireAlarm = new FireAlarm(query);
		});
	}

	/*
	 * b)
	 * Any database error, e.g. connection problem or query error, 
	 * implies the launching of a DatabaseProblemException.
	 * For this test, query was added as a parameter to the constructor.
	 * This way, we apply the Dependency Inversion Principle,
	 * making FireAlarm more flexible and testable.
	 */
	@Test
	public void databaseError() {
		query = "SELECT * FROM NON_EXISTING_TABLE";

		assertThrows(DatabaseProblemException.class, () -> {
			fireAlarm = new FireAlarm(query);
		});
	}

	/*
	 * c)
	 * When the REST endpoint is not usable, 
	 * the application throws a SensorConnectionProblemException.
	 * Changed the getTemperature method to protected.
	 */
	@Test
	public void sensorConnectionProblemException() throws SensorConnectionProblemException, IncorrectDataException {
		assertThrows(SensorConnectionProblemException.class, () -> {
			fireAlarm.getTemperature("potato-room");
		});
	}

	/*
	 * When the endpoint is incorrect, the application throws SensorConnectionProblemException.
	 * Changed sensors to protected.
	 */
	@Test
	public void incorrectDataException() throws SensorConnectionProblemException, IncorrectDataException {
		fireAlarm.sensors.put("potato-room", "http://steam.com");

		assertThrows(SensorConnectionProblemException.class, () -> {
			fireAlarm.getTemperature("potato-room");
		});
	}

	/*
	 * d)
	 * If no JSON data is returned, we raise an IncorrectDataException
	 * the application throws an IncorrectDataException.
	 * I had to change the objectMapper to protected.
	 */
	@Test
	public void incorrectDataExceptionNoTemperature() throws IncorrectDataException, IOException {
		ObjectMapper mapperMock = mock(ObjectMapper.class);
		fireAlarm.mapper = mapperMock;

		when(mapperMock.readTree("")).thenReturn(null);
		assertThrows(IncorrectDataException.class, () -> 
		{
			fireAlarm.getTemperature("kitchen");
		});
	}

	/*
	 * If the returned JSON object does not contain the key "temperature", 
	 * the application throws an IncorrectDataException.
	 */
	@Test
	public void incorrectDataExceptionNoTemperatureKey() throws IncorrectDataException, IOException {
		ObjectMapper mapperMock = mock(ObjectMapper.class);
		fireAlarm.mapper = mapperMock;
		JsonNode jsonNodeMock = mock(JsonNode.class);

		when(jsonNodeMock.get("temperature")).thenReturn(null);
		when(mapperMock.readTree(any(URL.class))).thenReturn(jsonNodeMock);
		assertThrows(IncorrectDataException.class, () -> {
			fireAlarm.getTemperature("kitchen");
		});
	}

	/*
	 * If the returned value is not integer, 
	 * the application throws an IncorrectDataException.
	 */
	@Test
	public void incorrectDataExceptionTemperatureNotInteger() throws IncorrectDataException, IOException {
		ObjectMapper mapperMock = mock(ObjectMapper.class);
		fireAlarm.mapper = mapperMock;
		JsonNode jsonNodeMock = mock(JsonNode.class);

		when(jsonNodeMock.get("temperature")).thenReturn(new TextNode("not integer"));
		when(mapperMock.readTree(any(URL.class))).thenReturn(jsonNodeMock);
		assertThrows(IncorrectDataException.class, () -> {
			fireAlarm.getTemperature("kitchen");
		});
	}

	/*
	 * e)
	 * When all sensors return a temperature <= MAX_TEMPERATURE, 
	 * the isTemperatureTooHigh() method returns false.
	 * No production code changes were needed.
	 */
	@Test
    public void testIsTemperatureTooHigh_AllSensorsReturnTemperatureLessThanMax() throws Exception {
        FireAlarm fireAlarmSpy = spy(new FireAlarm(query));

        doReturn(33).when(fireAlarmSpy).getTemperature(anyString());
        assertFalse(fireAlarmSpy.isTemperatureTooHigh());
    }

	/*
	 * f)
	 * When any sensor returns a temperature > MAX_TEMPERATURE, 
	 * the isTemperatureTooHigh() method returns true.
	 * No production code changes were needed.
	 */
	@Test
	public void testIsTemperatureTooHigh_AnySensorReturnsTemperatureGreaterThanMax() throws Exception {
		FireAlarm fireAlarmSpy = spy(new FireAlarm(query));

		doReturn(33).when(fireAlarmSpy).getTemperature(anyString());
		doReturn(81).when(fireAlarmSpy).getTemperature("kitchen");
		assertTrue(fireAlarmSpy.isTemperatureTooHigh());
	}
	//FELIZ NAVIDAD!! (estoy escribiendo esto el 24 de diciembre)
	/*
	 * Me ha costado bastante hacer los tests, pero creo que he aprendido bastante.
	 * Intente extraer en una clase aparte el manejo de la base de datos para intentar
	 * seguir el principio de responsabilidad única y de inyeccion de dependencias,
	 * de manera que esa nueva clase DBManager se pasaba al constructor de FireAlarm, 
	 * pero implcaba cambiar mucho código y dejaba de tener sentido el enunciado del ejercicio
	 * así que borré los cambios. Me gustaría saber si en el caso de no haber sido un ejercicio, 
	 * si hubiera sido correcto hacerlo.
	 */

}

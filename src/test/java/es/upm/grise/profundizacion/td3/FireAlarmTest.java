package es.upm.grise.profundizacion.td3;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.URL;
import java.util.HashMap;
@ExtendWith(SystemStubsExtension.class)
/*
Cambios realizados:
Creado un metodo para probar la conexion con la base de datos
getTemperature puesto en publico
Creado metodo setObjectMapper para el nuevo objeto atributo objectMapper
Creado un atributo Properties para las variables de entorno

*/
public class FireAlarmTest {
	
	FireAlarm fireAlarm;
	@SystemStub
	EnvironmentVariables environmentVariables = new EnvironmentVariables();

	@Test
	public void confFiletest() throws SensorConnectionProblemException, IncorrectDataException {
		String mockLocation = "/ruta/de/prueba";

		// Crear un entorno temporal para la prueba y setear la variable de entorno

		environmentVariables.set("firealarm.location", mockLocation);
		String appLocation = System.getenv("firealarm.location");
		assertThrows(ConfigurationFileProblemException.class, FireAlarm::new);
	}
	@Test
	public void DBExceptiontest() throws Exception,SensorConnectionProblemException, IncorrectDataException, DatabaseProblemException, ConfigurationFileProblemException {
		String mockLocation = System.getProperty("user.dir");

		// Crear un entorno temporal para la prueba y setear la variable de entorno

		environmentVariables.set("firealarm.location", mockLocation);
		String appLocation = System.getenv("firealarm.location");
		FireAlarm fa= new FireAlarm();
		assertThrows(DatabaseProblemException.class, () -> {
			fa.queryDB("uwu");
		});
	}
	@Test
	public void SensorExceptiontest() throws Exception,SensorConnectionProblemException, IncorrectDataException, DatabaseProblemException, ConfigurationFileProblemException {
		String mockLocation = System.getProperty("user.dir");

		// Crear un entorno temporal para la prueba y setear la variable de entorno

		environmentVariables.set("firealarm.location", mockLocation);
		String appLocation = System.getenv("firealarm.location");
		FireAlarm fa= new FireAlarm();
		assertThrows(SensorConnectionProblemException.class, () -> {
			fa.getTemperature("uwu");
		});
	}
	@Test
	public void testGetTemperatureWithInvalidJson() throws Exception {
		String mockLocation = System.getProperty("user.dir");

		// Crear un entorno temporal para la prueba y setear la variable de entorno

		environmentVariables.set("firealarm.location", mockLocation);
		String appLocation = System.getenv("firealarm.location");
		FireAlarm fa = spy(new FireAlarm());
		// Mock URL and ObjectMapper
		ObjectMapper mockedMapper = mock(ObjectMapper.class);
		fa.setObjectMapper(mockedMapper);

		// Prepare JSON node
		JsonNode mockedResult = mock(JsonNode.class);

		// Scenario 1: JSON object (result) itself is null
		when(mockedMapper.readTree(any(URL.class))).thenReturn(null);
		assertThrows(IncorrectDataException.class, () -> fa.getTemperature("kitchen"));

		// Scenario 2: JSON object does not contain the "temperature" key
		when(mockedMapper.readTree(any(URL.class))).thenReturn(mockedResult);
		when(mockedResult.get("temperature")).thenReturn(null);
		assertThrows(IncorrectDataException.class, () -> fa.getTemperature("kitchen"));

		// Scenario 3: The value of "temperature" key is not an integer
		JsonNode temperatureNode = mock(JsonNode.class);
		when(mockedResult.get("temperature")).thenReturn(temperatureNode);
		when(temperatureNode.canConvertToInt()).thenReturn(false);
		assertThrows(IncorrectDataException.class, () -> fa.getTemperature("kitchen"));
	}
	@Test
	public void TempLowtest() throws Exception,SensorConnectionProblemException, IncorrectDataException, DatabaseProblemException, ConfigurationFileProblemException {
		FireAlarm fireAlarmMock = mock(FireAlarm.class);
		doReturn(1).when(fireAlarmMock).getTemperature(anyString());
		assertFalse(fireAlarmMock.isTemperatureTooHigh());
	}
	@Test
	public void TempHightest() throws Exception,SensorConnectionProblemException, IncorrectDataException, DatabaseProblemException, ConfigurationFileProblemException {
		FireAlarm fireAlarmMock = mock(FireAlarm.class);
		doReturn(81).when(fireAlarmMock).getTemperature(anyString());
		assertFalse(fireAlarmMock.isTemperatureTooHigh());
	}
}

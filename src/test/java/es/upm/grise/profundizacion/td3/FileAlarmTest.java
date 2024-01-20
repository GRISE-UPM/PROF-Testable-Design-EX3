package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.org.webcompere.systemstubs.SystemStubs;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;


import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.Files;
import java.io.File;
import java.io.FileWriter;

@ExtendWith(SystemStubsExtension.class)
public class FileAlarmTest {
	
	FireAlarm fireAlarm;

	@TempDir
    Path tempDir;


	// Variable de entorno que utiliza FireAlarmApp, la necesita para localizar el fichero de la base datos al inicializar el constructor de FireAlarm()
	@SystemStub
	private EnvironmentVariables fireEnvVariables;	
	
	
	@BeforeEach
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException {

		// appLocation + "/resources/config.properties"
		fireEnvVariables.set("firealarm.location" , System.getProperty("user.dir"));
		// Mockito Spy: Spies execute the real methods of the underlying object by default.
		// Queremos que se ejecuten los metodos pero capaz queramos enmascarar algunas cosas.
		fireAlarm = Mockito.spy(new FireAlarm());
	}
	
	// Si la variable de entorno del stub no posee el valor correcto, falla 
	@Test
	public void locateConfigFileFail() throws ConfigurationFileProblemException {
		// appLocation + "/resources/config.properties", No poner correctamente el CurrentDirectory
		fireEnvVariables.set("firealarm.location" , "");
		assertThrows(ConfigurationFileProblemException.class, () -> new FireAlarm());
	}

	// Si la variable de entorno del stub posee el valor correcto, acierta 
	@Test
	public void locateConfigFileSuccess() throws ConfigurationFileProblemException  {
		assertDoesNotThrow( () -> new FireAlarm());
	}


	// Error en la base de datos 
	// TempDir no sirve para windows
	// https://www.baeldung.com/junit-5-temporary-directory
	@Test
	public void databaseConnectError() throws DatabaseProblemException, IOException {

		String filePath = Paths.get("resources", "config.properties").toString();

		// writing wrong db location
		FileWriter myWriter = new FileWriter(filePath);
		myWriter.write("dblocation=jdbc:sqlite:wrong");
		myWriter.close();

		assertThrows(DatabaseProblemException.class, () -> {new FireAlarm();});

		// Restaurar
		// writing wrong db location
		myWriter = new FileWriter(filePath);
		myWriter.write("dblocation=jdbc:sqlite:./resources/sensors.db");
		myWriter.close();

	}

	// EndPoint no utilizable
	@Test
	public void endpointNotGood() throws ConfigurationFileProblemException  {
		assertThrows(SensorConnectionProblemException.class, () -> new FireAlarm().getTemperature("malo"));
	}
	
	

	// 1) Si el objeto JSON devuelto no contiene la clave “temperature”, 
    // o el valor devuelto no es entero, la aplicación lanza una IncorrectDataException.
    @Test
    public void incorrectDataExceptionThrown() throws IncorrectDataException, JsonProcessingException {
		// Agregamos un sensor, que ajuro debe tener una URL correcta sino salta la exceptcion SensorConnectionProblemException en getTemperature
		fireAlarm.sensors.put("room1", "http://www.room1.com");
		ObjectMapper object2mock = mock(ObjectMapper.class);
		fireAlarm.mapper = object2mock;
		// Al momento que se llame el readTree del ObjectMapper mockeado, devolver un null
		when(object2mock.readTree(any(String.class))).thenReturn(mock(JsonNode.class));
		// mockeamos JsonNode para que al recibir el "temperature", reciba un null 
		when((mock(JsonNode.class)).get("temperature")).thenReturn(null);
		assertThrows(IncorrectDataException.class, () -> { 
			fireAlarm.getTemperature("room1");
		}
		);
    }

    // 2) Cuando todos los sensores devuelven una temperatura <= MAX_TEMPERATURE, 
    // el método isTemperatureTooHigh() devuelve false.
    @Test
    public void allSensorsTemperatureBelowMax() throws SensorConnectionProblemException, IncorrectDataException {
		// Cómo getTemperature devolvera 80 (maxima de temperature), entonces deberá devolver false, porque se encuentra debajo de lo que marca la constante MAX_TEMPERATURE
		doReturn(80).when(fireAlarm).getTemperature(anyString());
		assertFalse(fireAlarm.isTemperatureTooHigh());
    }

    // 3) Cuando algún sensor devuelve una temperatura > MAX_TEMPERATURE, 
    // el método isTemperatureTooHigh() devuelve true.
    @Test
    public void someSensorTemperatureAboveMax() throws SensorConnectionProblemException, IncorrectDataException {
		// Cómo getTemperature devolvera 100 (superior a la maxima de temperature), entonces deberá devolver true, porque se encuentra por encima de lo que marca la constante MAX_TEMPERATURE
        doReturn(100).when(fireAlarm).getTemperature(anyString());
        assertTrue(fireAlarm.isTemperatureTooHigh());
    }


}

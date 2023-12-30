package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
public class FileAlarmTest {

	/*
	 * CAMBIOS en FileAlarm: 
	 * 	- dblocation ahora está fuera del código de inicialización, para poderse hacer queries dummy de testeo.
	 *  - Se ha introducido el método "dummyQuery()" que simula el proceso de conexión y ejecución de una consulta vacía
	 *	  para poder poner a prueba la conexión con la base de datos.
	 *  - mapper ahora está fuera del código de getTemperature(), para poderse sobreescribir al llamar a getTemperature().
	 *  - getTemperature() ahora es un método público que también pide un ObjectMapper() como parámetro. Por defecto, este
	 *    será un objeto nuevo enviado por isTemperatureHigh().
	 * 
	 */

	@SystemStub
	private EnvironmentVariables fireAlarmDependencies = new EnvironmentVariables();

	FireAlarm fireAlarm;
	String projectRoot;
	
	@BeforeEach
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException, IOException {
		// Usar directorio actual para encontrar sensors.db
		String projectRootBase = System.getProperty("user.dir");

		String[] directories = projectRootBase.split("\\\\");
		projectRoot = String.join(File.separator + File.separator, directories); // al leerse después por FireAlarm se pierde un '\'

		fireAlarmDependencies.set("firealarm.location", projectRoot);

		// De nuevo, al leerse para extraer dbLocation, se necesitan dos File.separators - de lo contrario, falla
		FileWriter setter = new FileWriter(projectRoot + File.separator + "resources" + File.separator + "config.properties");
		try (setter) {
			setter.write("dblocation=jdbc:sqlite:"+ projectRoot + File.separator + File.separator + "resources" + File.separator + File.separator + "sensors.db");
		}

		fireAlarm = new FireAlarm();
	}
	
	@Test
	public void testDefault() throws SensorConnectionProblemException, IncorrectDataException {
		//test proporcionado con el ejercicio, describe comportamiento esperado tras haber inicializado el sistema correctamente
		assertFalse(fireAlarm.isTemperatureTooHigh());
	}

	@Test
	public void testBadConfigFile() {
		fireAlarmDependencies.set("firealarm.location", "absolutely_nowhere000Â");
		assertThrows(ConfigurationFileProblemException.class, () -> new FireAlarm());
	}

	@Test
	public void testDatabaseError() throws IOException {
		FileWriter setter = new FileWriter(projectRoot + File.separator + "resources" + File.separator + "config.properties");
		try (setter){
			setter.write("dblocation=location_missing");
		}
		
		//Test: no se encuentra base de datos
		assertThrows(DatabaseProblemException.class, () -> new FireAlarm());

		//Test: no se puede hacer una consulta al no poder acceder a una base de datos
		assertThrows(DatabaseProblemException.class, () -> fireAlarm.dummyQuery());
	}

	@Test
	public void testBadEndpointError(){
		assertThrows(SensorConnectionProblemException.class, () -> fireAlarm.getTemperature("Nowhere", new ObjectMapper()));
	}

	@Test
	public void testInvalidValues() throws IOException{
		ObjectMapper mockMapper = mock(ObjectMapper.class);
		JsonNode mockMatch = mock(JsonNode.class);
		JsonNode mockValue = mock(JsonNode.class);

		//Test: nodo no contiene atributo "temperature"
		when(mockMapper.readTree(any(URL.class))).thenReturn(mockMatch);
		when(mockMatch.get("temperature")).thenReturn(null);
		assertThrows(IncorrectDataException.class, () -> fireAlarm.getTemperature("kitchen", mockMapper));

		//Test: nodo contiene atributo "temperature" con formato incorrecto
		when(mockMatch.get("temperature")).thenReturn(mockValue);
		when(mockValue.canConvertToInt()).thenReturn(false);
		assertThrows(IncorrectDataException.class, () -> fireAlarm.getTemperature("kitchen", mockMapper));

		//Test (no en enunciado): no se encuentran nodos
		when(mockMapper.readTree(any(URL.class))).thenReturn(null);
		assertThrows(IncorrectDataException.class, () -> fireAlarm.getTemperature("kitchen", mockMapper));
	}

	@Test
	public void testSensorTemperatures() throws SensorConnectionProblemException, IncorrectDataException{
		FireAlarm spy = spy(FireAlarm.class);

		//Test: temperaturas no demasiado altas ( > 80), 0 ("ni frio ni calor")
		doReturn(0).when(spy).getTemperature(anyString(), any());
		assertFalse(spy.isTemperatureTooHigh());

		//Test: temperaturas demasiado altas ( > 80)
		doReturn(451).when(spy).getTemperature(anyString(), any());
		assertTrue(spy.isTemperatureTooHigh());
	}

}

package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;


import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(SystemStubsExtension.class)
public class FileAlarmTest {
	
	FireAlarm fireAlarm;

	@SystemStub
	private EnvironmentVariables envVariables = new EnvironmentVariables("firealarm.location", System.getProperty("user.dir"));
	
	@BeforeEach
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException {
		fireAlarm = new FireAlarm();
	}
	
	// Cuando no se puede localizar el fichero config.properties, la clase FireAlarm lanza una ConfigurationFileProblemException.
	@Test
	public void configFileNotFound() {
		envVariables.set("firealarm.location", null);
		assertThrows(ConfigurationFileProblemException.class, () -> new FireAlarm());
	}

	// Cualquier error de la base de datos implica el lanzamiento de una DatabaseProblemException.
	// Se ha sacado del método getTemperature la conexión a la base de datos y se ha puesto en un método aparte.
	@Test
	public void databaseError() throws DatabaseProblemException{
		assertThrows(DatabaseProblemException.class, () -> fireAlarm.conexionBaseDatos(null));
	}

	// Cuando el endpoint REST no es utilizable, la aplicación lanza una SensorConnectionProblemException.
	@Test 
	public void endPointRest() throws SensorConnectionProblemException  {
		assertThrows(SensorConnectionProblemException.class, () -> fireAlarm.getTemperature(anyString()));
	}

	// Si el objeto JSON devuelto no contiene la clave “temperature” la aplicación lanza una IncorrectDataException.
	// Se ha sacado del método getTemperature el tratamiento del JSON y se ha puesto en un método aparte.
	@Test
    public void noTemperatureKey() throws IncorrectDataException {
        assertThrows(IncorrectDataException.class, () -> fireAlarm.jSon(null));
    }

	// Cuando todos los sensores devuelven una temperatura <= MAX_TEMPERATURE, el método isTemperatureTooHigh() devuelve false.
	@Test 
	public void temperatureIsNotTooHigh() throws SensorConnectionProblemException, IncorrectDataException {
		assertThrows(IncorrectDataException.class, () -> fireAlarm.jSon(null));
	}

	// Cuando algún sensor devuelve una temperatura > MAX_TEMPERATURE, el método isTemperatureTooHigh() devuelve true.
	@Test 
	public void temperatureIsTooHigh() throws SensorConnectionProblemException, IncorrectDataException {
		FireAlarm spy = spy(FireAlarm.class);

		// Método getTemperature() del "spy" para que siempre devuelva 150.
		doReturn(150).when(spy).getTemperature(anyString());
		assertTrue(spy.isTemperatureTooHigh());
	}






}

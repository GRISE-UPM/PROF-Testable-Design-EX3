package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/*
 * Para testear la clase correctamente he hecho que haya una clase de Webservice de sensores a la que se le pase
 * la URL del endpoint del sensor y se encargue del proceso de conexión y deserialización.
 * De este modo, podemos testear de forma aislada a las conexiones de red la clase FireAlarm y las deserializaciones
 *
 * Adicionalemente he hecho lo mismo con los sensores, he abstraido toda su logica de carga y obtención de datos
 * en una nueva clase, de modo que sea más sencillo hacer test aislados de la clase FireAlarm como de la carga
 * de los sensores.
 */

@ExtendWith(SystemStubsExtension.class)
public class FireAlarmTest {

	@Mock
	private SensorWebservice sensorWebservice;

	@Mock
	private SensorRepository sensorRepository;

	private AutoCloseable mockClose;


	private FireAlarm fireAlarm;



	
	@BeforeEach
	public void setUp() {
		mockClose = MockitoAnnotations.openMocks(this);
		fireAlarm = new FireAlarm(sensorWebservice, sensorRepository);
	}

	@AfterEach
	public void tearDown() throws Exception {
		mockClose.close();
	}
	
	@Test
	public void test() throws SensorConnectionProblemException, IncorrectDataException {
		assertFalse(fireAlarm.isTemperatureTooHigh());
	}

	@Test
	public void testAllTemperaturesAreUnderOrEqualToMaxTemperature() throws SensorConnectionProblemException, IncorrectDataException {
		Mockito.when(sensorWebservice.getRoomTemperature(Mockito.any())).thenReturn(1,10,80);
		Mockito.when(sensorRepository.getEndpoints()).thenReturn(Arrays.asList("1","2","3"));

		Assertions.assertFalse(fireAlarm.isTemperatureTooHigh());

	}

	@Test
	public void testAllTemperaturesAreHigherThanMaxTemperature() throws SensorConnectionProblemException, IncorrectDataException {
		Mockito.when(sensorWebservice.getRoomTemperature(Mockito.any())).thenReturn(84);
		Mockito.when(sensorRepository.getEndpoints()).thenReturn(Arrays.asList("1","2","3"));

		Assertions.assertTrue(fireAlarm.isTemperatureTooHigh());

	}



}

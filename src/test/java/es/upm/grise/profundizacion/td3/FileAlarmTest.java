package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

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
	public void ConfigurationFileProblemIsThrownTest() {
		env.set("firealarm.location", "dummy");
		assertThrows(ConfigurationFileProblemException.class, () -> new FireAlarm());
	}
		
	@Test
	public void DatabaseProblemExceptionIsThrown() 
	{		
		
	}
	
	@Test
	public void SensorConnectionProblemExceptionIsThrown() 
	{		
		assertThrows(SensorConnectionProblemException.class, 
				() -> new FireAlarm().getTemperature("dummy"));
	}
	
	@Test
	public void isTemperatureTooLowTest() throws SensorConnectionProblemException, IncorrectDataException
	{		
		Mockito.doReturn(fireAlarm.MAX_TEMPERATURE - 1)
			.when(fireAlarm).getTemperature(Mockito.anyString());
		assertFalse(fireAlarm.isTemperatureTooHigh());
	}
	
	@Test
	public void isTemperatureTooHighTest() throws SensorConnectionProblemException, IncorrectDataException
	{		
		Mockito.doReturn(fireAlarm.MAX_TEMPERATURE + 1)
			.when(fireAlarm).getTemperature(Mockito.anyString());
		assertTrue(fireAlarm.isTemperatureTooHigh());
	}

}

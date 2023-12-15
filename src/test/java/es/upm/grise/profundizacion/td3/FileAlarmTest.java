package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FileAlarmTest {
	
	FireAlarm fireAlarm;
	
	@BeforeEach
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException {
		fireAlarm = new FireAlarm();
	}
	
	@Test
	public void test() throws SensorConnectionProblemException, IncorrectDataException {
		assertFalse(fireAlarm.isTemperatureTooHigh());
	}

}

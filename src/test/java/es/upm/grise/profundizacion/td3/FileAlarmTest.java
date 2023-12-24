package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
public class FileAlarmTest {

	@SystemStub
	private EnvironmentVariables env = new EnvironmentVariables("firealarm.location", System.getProperty("user.dir"));

	FireAlarm fireAlarm;

	private FireAlarm fireAlarmMock;

	@BeforeEach
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException {
		fireAlarmMock = mock(FireAlarm.class);
		fireAlarm = new FireAlarm();
	}

	@Test
	public void test() throws SensorConnectionProblemException, IncorrectDataException {
		;
		assertFalse(fireAlarm.isTemperatureTooHigh());
	}

	@Test
	public void test_ConfigurationFileProblemException() {
		assertThrows(ConfigurationFileProblemException.class, () -> {
			env.set("firealarm.location", null);
			fireAlarm.loadSensors();
		});
	}

	@Test
	public void test_DatabaseProblemException() {
		try {
			Path tempDirectory = Files.createTempDirectory("myTempDir");
			Path resourcesDirectory = tempDirectory.resolve("resources");
			Files.createDirectories(resourcesDirectory);
			Path configFile = resourcesDirectory.resolve("config.properties");
			Files.createFile(configFile);

			FileWriter writer = new FileWriter(configFile.toFile());
			writer.write("dblocation = nadaqueveraqui");
			writer.close();

			env.set("firealarm.location", tempDirectory.toString());

			assertThrows(DatabaseProblemException.class, () -> {
				fireAlarm.loadSensors();
			});

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void test_SensorConnectionProblemException()
			throws SensorConnectionProblemException, IncorrectDataException {
		assertThrows(SensorConnectionProblemException.class, () -> {
			Method method = FireAlarm.class.getDeclaredMethod("getTemperature", String.class);
			method.setAccessible(true);
			method.invoke(fireAlarm, "room");
		});
	}

	@Test
	public void test_isTemperatureTooHigh_False() throws SensorConnectionProblemException, IncorrectDataException,
			ConfigurationFileProblemException, DatabaseProblemException {
		FireAlarm spyFireAlarm = spy(fireAlarm);
		spyFireAlarm.loadSensors();
		doReturn(10).when(spyFireAlarm).getTemperature(anyString());
		assertFalse(spyFireAlarm.isTemperatureTooHigh());
	}

	@Test
	public void test_isTemperatureTooHigh_True() throws SensorConnectionProblemException, IncorrectDataException,
			ConfigurationFileProblemException, DatabaseProblemException {
		FireAlarm spyFireAlarm = spy(fireAlarm);
		spyFireAlarm.loadSensors();
		doReturn(90).when(spyFireAlarm).getTemperature(anyString());
		assertTrue(spyFireAlarm.isTemperatureTooHigh());
	}
}

package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith(SystemStubsExtension.class)
public class FileAlarmTest {

	@SystemStub
	private EnvironmentVariables env = new EnvironmentVariables("firealarm.location", System.getProperty("user.dir"));
	
	FireAlarm fireAlarm;
	
	@BeforeEach
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException {
		fireAlarm = new FireAlarm();
	}
	
	@Test
	public void test() throws SensorConnectionProblemException, IncorrectDataException {;
		assertFalse(fireAlarm.isTemperatureTooHigh());
	}

	@Test
	public void test_ConfigurationFileProblemException() {
		assertThrows(ConfigurationFileProblemException.class, () -> {
			env.set("firealarm.location", null);
			new FireAlarm();
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
			new FireAlarm();
		});

        } catch (IOException e) {
            e.printStackTrace();
        }
		
	}

}

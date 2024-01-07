package es.upm.grise.profundizacion.td3;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SystemStubsExtension.class)
public class SensorRepositoryTest {

    public static final String FIREALARM_LOCATION = "firealarm.location";


    @SystemStub
    private final EnvironmentVariables environmentVariables = new EnvironmentVariables(FIREALARM_LOCATION,".");

    @Test
    public void testConfigurationFileProblemExceptionIsThrownWhenFileDoesNotExists(){
        environmentVariables.set(FIREALARM_LOCATION,"/tmp");

        assertThrows(ConfigurationFileProblemException.class, SensorRepository::new);
    }

    @Test
    public void testDatabaseProblemExceptionIsThrownWhenDatabaseDoesNotExists(@TempDir File tempDirectory) throws IOException {
        environmentVariables.set(FIREALARM_LOCATION, tempDirectory.getAbsolutePath());
        File resourcesFile = new File(tempDirectory,"resources");
        resourcesFile.mkdir();
        File propertiesFile = new File(resourcesFile,"config.properties");
        try(FileWriter fileWriter = new FileWriter(propertiesFile)){
            fileWriter.write("dblocation=jdbc:sqlite:" + tempDirectory.getAbsolutePath() + "/resources/sensors.db");
        }

        assertThrows(DatabaseProblemException.class, SensorRepository::new);
    }


}

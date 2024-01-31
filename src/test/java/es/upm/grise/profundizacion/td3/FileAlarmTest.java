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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
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
import java.nio.file.StandardCopyOption;
import java.io.IOException;
import java.nio.file.Files;
import java.io.FileWriter;

/**
 * Cambios realizados en FireAlarm:
 *
 * 1. Se ha sacado la constante MAX_TEMPERATURE del método isTemperatureTooHigh, para poder usarla en el testing sin magic numbers
 *
 * 2. Se ha extraido la logia para obtener el JSON de una url con el nuevo método get_result_from_url(endpoint). De esta forma se puede suplantar el JSON recibido, y evitar que el test dependa de los valores del servidor, que podrian cambiar entre un test y otro, rompiendo el determinismo de estos.
 *
 * 3. Se ha cambiado el atributo sensors de private a protected.
 *
 */

@ExtendWith(SystemStubsExtension.class)
public class FileAlarmTest {
    FireAlarm fireAlarm;

    JsonNode json_mock;

    @SystemStub
    private EnvironmentVariables env_variables;

    private void write_to_config(String content) throws IOException {
        // Write content to the config file
        String path = Paths.get("resources", "config.properties")+"";
        FileWriter writer = new FileWriter(path);
        writer.write(content);
        writer.close();
    }

    private void backup_config() throws IOException {
        Path path = Paths.get("resources", "config.properties");
        Files.copy(path, Paths.get(path+".backup"), StandardCopyOption.REPLACE_EXISTING);
    }

    private void restore_config() throws IOException {
        Path path = Paths.get("resources", "config.properties");
        Files.move(Paths.get(path+".backup"), path, StandardCopyOption.REPLACE_EXISTING);
    }

    @BeforeEach
    public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException, SensorConnectionProblemException, IOException {
        this.backup_config();
        env_variables.set("firealarm.location" , System.getProperty("user.dir"));
        fireAlarm = Mockito.spy(new FireAlarm());

        // Fake JSON response so we don't depend on a server
        json_mock = mock(JsonNode.class);
        doReturn(json_mock).when(fireAlarm).get_result_from_url(anyString());
        doReturn(json_mock).when(fireAlarm).get_result_from_url(null);
        when(json_mock.get("temperature")).thenReturn(json_mock);
        when(json_mock.canConvertToInt()).thenReturn(true);
        when(json_mock.asInt()).thenReturn( 35 ); // Test temperature is 35
    }

    @AfterEach
    public void clean_up() throws IOException {
        this.restore_config();
    }

    @Test
    public void test() throws SensorConnectionProblemException, IncorrectDataException {
        assertFalse(fireAlarm.isTemperatureTooHigh());
    }

    @Test
    @DisplayName("Cuando no se puede localizar el fichero config.properties, la clase FireAlarm lanza una ConfigurationFileProblemException")
    public void configuration_file_not_found() throws ConfigurationFileProblemException {
        env_variables.set("firealarm.location" , "_404_"); // Invalid path
        assertThrows(ConfigurationFileProblemException.class, () -> new FireAlarm());
    }

    @Test
    @DisplayName("Cualquier error de la base de datos, ej: problema de conexión o error en consulta, implica el lanzamiento de una DatabaseProblemException")
    public void database_connection_exception() throws DatabaseProblemException, IOException {
        write_to_config("dblocation=jdbc:sqlite:_404_"); // Invalid path
        assertThrows(DatabaseProblemException.class, () -> new FireAlarm());
    }

    @Test
    @DisplayName("Cuando el endpoint REST no es utilizable, la aplicación lanza una SensorConnectionProblemException.")
    public void invalid_endpoint_exception() throws ConfigurationFileProblemException  {
        assertThrows(SensorConnectionProblemException.class, () -> new FireAlarm().getTemperature("_404_"));
    }

    @Test
    @DisplayName("Si el objeto JSON devuelto no contiene la clave “temperature”, o el valor devuelto no es entero, la aplicación lanza una IncorrectDataException.")
    public void incorrect_data_exception_due_to_json_fields() throws IncorrectDataException, JsonProcessingException {
        fireAlarm.sensors.put("example", "http://example.com");
        when(json_mock.get("temperature")).thenReturn(null);

        assertThrows(IncorrectDataException.class, () -> {
                fireAlarm.getTemperature(":)");
            });
    }

    @Test
    @DisplayName("Cuando todos los sensores devuelven una temperatura <= MAX_TEMPERATURE, el método isTemperatureTooHigh() devuelve false.")
    public void sensors_temperature_below_max_temperature() throws SensorConnectionProblemException, IncorrectDataException {
        when(fireAlarm.getTemperature(anyString())).thenReturn(fireAlarm.MAX_TEMPERATURE);
        assertFalse(fireAlarm.isTemperatureTooHigh());
    }

    @Test
    @DisplayName("Cuando algún sensor devuelve una temperatura > MAX_TEMPERATURE, el método isTemperatureTooHigh() devuelve true.")
    public void sensors_temperature_above_max_temperature() throws SensorConnectionProblemException, IncorrectDataException {
        when(fireAlarm.getTemperature(anyString())).thenReturn(fireAlarm.MAX_TEMPERATURE + 1);
        assertTrue(fireAlarm.isTemperatureTooHigh());
    }
}

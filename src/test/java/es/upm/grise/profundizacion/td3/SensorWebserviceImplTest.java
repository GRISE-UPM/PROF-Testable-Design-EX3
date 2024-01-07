package es.upm.grise.profundizacion.td3;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/*
 * Usamos un MockWebserver que nos ayude a hacer los test de los endpoints y que los resultados son los esperados
 * Esta clase simplemente extrae la lógica ya implementada en FireAlarm y la encapsula en su propia clase.
 */
public class SensorWebserviceImplTest {

    private SensorWebserviceImpl sensorWebservice;

    @BeforeEach
    void setUp(){
        sensorWebservice = new SensorWebserviceImpl();
    }

    @Test
    void testUseInvalidURL(){
        String fakeUrl = "not a url";
        Assertions.assertThrows(SensorConnectionProblemException.class, () -> sensorWebservice.getRoomTemperature(fakeUrl));
    }

    @Test
    void testMissingTemperatureValue() throws IOException {
        String json = "{\"not_temperature\":10}";
        try (MockWebServer server = new MockWebServer()) {
            MockResponse response = new MockResponse();
            response.setBody(json);
            server.enqueue(response);

            server.start();

            String requestUrl = server.url("/test").toString();

            Assertions.assertThrows(IncorrectDataException.class,() -> sensorWebservice.getRoomTemperature(requestUrl));
        }
    }

    @Test
    void testTemperatureInvalidValue() throws IOException {
        String json = "{\"temperature\":true}";
        try (MockWebServer server = new MockWebServer()) {
            MockResponse response = new MockResponse();
            response.setBody(json);
            server.enqueue(response);

            server.start();

            String requestUrl = server.url("/test").toString();

            Assertions.assertThrows(IncorrectDataException.class,() -> sensorWebservice.getRoomTemperature(requestUrl));
        }
    }

    @Test
    void testGetTemperature() throws IOException, SensorConnectionProblemException, IncorrectDataException {
        String json = "{\"temperature\":10}";
        try (MockWebServer server = new MockWebServer()) {
            MockResponse response = new MockResponse();
            response.setBody(json);
            server.enqueue(response);

            server.start();

            String requestUrl = server.url("/test").toString();

            int value = sensorWebservice.getRoomTemperature(requestUrl);

            Assertions.assertEquals(10, value);
        }
    }
}

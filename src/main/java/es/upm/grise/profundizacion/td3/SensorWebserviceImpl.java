package es.upm.grise.profundizacion.td3;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;

public class SensorWebserviceImpl implements SensorWebservice{

    @Override
    public int getRoomTemperature(String endpoint) throws SensorConnectionProblemException, IncorrectDataException {
        URL url;
        ObjectMapper mapper = new ObjectMapper();
        JsonNode result;

        // Using the Jackson library we can get JSON directly from an
        // URL using an ObjectMapper
        try {
            url = new URL(endpoint);
            result = mapper.readTree(url);
        } catch (Exception e) {
            throw new SensorConnectionProblemException();
        }

        // If no JSON data is returned, we raise an exception
        if(result == null)
            throw new IncorrectDataException();

        // The sensor returns an JSON object with a single key/value
        // pair named "temperature".
        result = result.get("temperature");

        // The key "temperature" may not exist
        if(result == null)
            throw new IncorrectDataException();

        // If the value is not integer, we raise the same error
        if(!result.canConvertToInt())
            throw new IncorrectDataException();

        // When everything is correct, we return the temperature as an Int
        return result.asInt();
    }

}

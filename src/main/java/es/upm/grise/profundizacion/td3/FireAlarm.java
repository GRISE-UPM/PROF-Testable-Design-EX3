package es.upm.grise.profundizacion.td3;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import es.upm.grise.profundizacion.td3.exceptions.ConfigurationFileProblemException;
import es.upm.grise.profundizacion.td3.exceptions.DatabaseProblemException;
import es.upm.grise.profundizacion.td3.exceptions.IncorrectDataException;
import es.upm.grise.profundizacion.td3.exceptions.SensorConnectionProblemException;

import java.io.FileInputStream;
import java.net.URL;

public class FireAlarm {
	
	// Sensors are stored in a hash map for easier access
	private Map<String, String> sensors;

	private ObjectMapper mapper;
	
	final static int MAX_TEMPERATURE = 80;
	
	// Constructor: read the sensors from the database and store them
	// in the hash map

	public FireAlarm(DBConnector dbConnector) throws ConfigurationFileProblemException, DatabaseProblemException {
		
		// Read the property file to find out the database location
		// As the executable can be located anywhere, so we store the
		// app directory in a environment variable (without the slash
		// at the end
		Properties configProperties = new Properties();
		String appLocation = System.getenv("firealarm.location");

		try {
			
			configProperties.load(new FileInputStream(appLocation + "/resources/config.properties"));
			
		} catch (Exception e) {
			
			throw new ConfigurationFileProblemException();
			
		}
		  

		// Then we obtain the database location
		String dblocation = configProperties.getProperty("dblocation");
		dbConnector.connect(dblocation);
		this.sensors = dbConnector.getSensors();
		dbConnector.closeConnection();
	}

	// Read the temperature from a sensor
	private int getTemperature(String room) throws SensorConnectionProblemException, IncorrectDataException {

		String endpoint = sensors.get(room);
		URL url;
		ObjectMapper mapper = this.mapper == null ? new ObjectMapper() : this.mapper;
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

	public void setMapper(ObjectMapper mapper) {
		this.mapper = mapper;
	}

	public boolean isTemperatureTooHigh() throws SensorConnectionProblemException, IncorrectDataException {
			
		// If any temperature exceeds MAX_TEMPERATURE, then the 
		// temperature is too high
		for(Entry<String, String> sensor : sensors.entrySet()) {
			
			if(getTemperature(sensor.getKey()) > MAX_TEMPERATURE)
					return true;
		}
		
		return false;
		
	}

	public static int getMaxTemperature() {
		return MAX_TEMPERATURE;
	}
	
}

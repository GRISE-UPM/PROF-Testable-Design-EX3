package es.upm.grise.profundizacion.td3;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

public class FireAlarm {

	private String dblocation;

	// Sensors are stored in a hash map for easier access
	public HashMap<String, String> sensors = new HashMap<String, String>();

	public ObjectMapper mapper = new ObjectMapper();
	public void sqlQuery(String query) throws DatabaseProblemException{
		try{

			Connection connection = DriverManager.getConnection(dblocation);
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			connection.close();
		}catch(Exception e){
			throw new DatabaseProblemException();

		}
	}

	// Constructor: read the sensors from the database and store them
	// in the hash map
	public FireAlarm() throws ConfigurationFileProblemException, DatabaseProblemException {

        // Read the property file to find out the database location
		// As the executable can be located anywhere, so we store the
		// app directory in a environment variable (without the slash
		// at the end
		Properties configProperties = new Properties();
		String appLocation = System.getenv("firealarm.location");

		System.out.println("AppLocation: " + appLocation);

		try {

			configProperties.load(new FileInputStream(appLocation + "/resources/config.properties"));

		} catch (Exception e) {

			throw new ConfigurationFileProblemException();

		}

		// Then we obtain the database location
		dblocation = configProperties.getProperty("dblocation");
		System.out.println("DbLocation: " + dblocation);

		// Now we store the sensors' data in the sensors variable
		// It takes several steps determined by the SQL API
		try {

			// Create DB connection
			Connection connection = DriverManager.getConnection(dblocation);

			// Read from the sensors table
			String query = "SELECT * FROM sensors";
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);


			// Iterate until we get all sensors' data
			while (resultSet.next()) {

				String room = resultSet.getString("room");
				String endpoint = resultSet.getString("endpoint");
				sensors.put(room, endpoint);

			}

			// Close the connection
			connection.close();

		} catch (Exception e) {

			throw new DatabaseProblemException();

		}

	}

	// Read the temperature from a sensor
	public int getTemperature(String room) throws SensorConnectionProblemException, IncorrectDataException {

		String endpoint = sensors.get(room);
		URL url;
		JsonNode result;

		// Using the Jackson library we can get JSON directly from an
		// URL using an ObjectMapper
		try {
			url = new URL(endpoint);
			System.out.println("url: " + url);
			result = mapper.readTree(url);
			System.out.println(result);

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


	public boolean isTemperatureTooHigh() throws SensorConnectionProblemException, IncorrectDataException {

		final int MAX_TEMPERATURE = 80;

		// If any temperature exceeds MAX_TEMPERATURE, then the
		// temperature is too high
		for(Entry<String, String> sensor : sensors.entrySet()) {

			if(getTemperature(sensor.getKey()) > MAX_TEMPERATURE)
					return true;
		}

		return false;

	}
	
}

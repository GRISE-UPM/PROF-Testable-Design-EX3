package es.upm.grise.profundizacion.td3;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.FileInputStream;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.net.URL;
public class FireAlarm {
	
	HashMap<String, String> sensors = new HashMap<String, String>();
	// se incluye una varible private para que sea testeable
	int MAX_TEMPERATURE = 80;

	public FireAlarm() throws ConfigurationFileProblemException, DatabaseProblemException{
		Properties configProperties = new Properties();
		String appLocation = System.getenv("firealarm.location");
		try {configProperties.load(new FileInputStream(appLocation + "/resources/config.properties"));} 
		catch (Exception e) {throw new ConfigurationFileProblemException();}
		String dblocation = configProperties.getProperty("dblocation");
		conectarDB(dblocation);}
	
	// se divide saca del constructor para dividir la función
	void conectarDB(String dblocation) throws DatabaseProblemException{
		try {
			Connection connection = DriverManager.getConnection(dblocation);
			String query = "SELECT * FROM sensors";
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery(query);
			while (resultSet.next()) {
				String room = resultSet.getString("room");
				String endpoint = resultSet.getString("endpoint");
				sensors.put(room, endpoint);}
			connection.close();} 
		catch (Exception e) {throw new DatabaseProblemException();}}
		
	int getTemperature(String room) throws SensorConnectionProblemException, IncorrectDataException {
		String endpoint = sensors.get(room);
		URL url;
		ObjectMapper mapper = new ObjectMapper();
		JsonNode result;
		try {url = new URL(endpoint); result = mapper.readTree(url);}
		catch (Exception e) {throw new SensorConnectionProblemException();}
		return validarData(result);}
	
	// se saca de getTemperature la validación de los datos.
	int validarData(JsonNode result) throws SensorConnectionProblemException, IncorrectDataException{
		if(result == null)throw new IncorrectDataException();
		result = result.get("temperature");
		if(result == null)throw new IncorrectDataException();
		if(!result.canConvertToInt())throw new IncorrectDataException();
		return result.asInt();}

	boolean isTemperatureTooHigh() throws SensorConnectionProblemException, IncorrectDataException {
		for(Entry<String, String> sensor : sensors.entrySet()) {
			if(getTemperature(sensor.getKey()) > MAX_TEMPERATURE){return true;}}
		return false;}
	
}

package es.upm.grise.profundizacion.td3;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

public class SensorRepository {

    // Sensors are stored in a hash map for easier access
    private HashMap<String, String> sensors = new HashMap<String, String>();

    public SensorRepository() throws ConfigurationFileProblemException, DatabaseProblemException {
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

    public String getEndpoint(String room){
        return sensors.get(room);
    }

    public Iterable<String> getEndpoints(){
        return sensors.values();
    }
}

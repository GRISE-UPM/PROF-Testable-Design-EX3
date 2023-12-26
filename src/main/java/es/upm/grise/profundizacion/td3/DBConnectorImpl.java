package es.upm.grise.profundizacion.td3;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import es.upm.grise.profundizacion.td3.exceptions.DatabaseProblemException;

public class DBConnectorImpl implements DBConnector {

    private Connection connection;

    @Override
    public void connect(String dbLocation) throws DatabaseProblemException {
        try{
            this.connection = DriverManager.getConnection(dbLocation);
        } catch (Exception e) {
            throw new DatabaseProblemException();
        }
    }
    

    @Override
    public Map<String,String> getSensors() throws DatabaseProblemException {
        Map<String,String> sensors = new HashMap<>();
        // Now we store the sensors' data in the sensors variable
		// It takes several steps determined by the SQL API
		try {

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

        return sensors;
    }


	@Override
	public void closeConnection() throws DatabaseProblemException {
		if (this.connection==null){
			return;
		}	
		try {
			this.connection.close();
		} catch (SQLException e) {
			throw new DatabaseProblemException();
		}
	}



   
}

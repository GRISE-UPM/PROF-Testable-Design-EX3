package es.upm.grise.profundizacion.td3;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import java.io.FileInputStream;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class FireAlarm {

	private SensorWebservice sensorWebservice;
	
	private SensorRepository sensorRepository;
	
	// Constructor: read the sensors from the database and store them
	// in the hash map
	public FireAlarm(SensorWebservice sensorWebservice, SensorRepository sensorRepository)  {

		this.sensorWebservice = sensorWebservice;
		this.sensorRepository = sensorRepository;

	}

	// Read the temperature from a sensor
	private int getTemperature(String room) throws SensorConnectionProblemException, IncorrectDataException {

		String endpoint = sensorRepository.getEndpoint(room);

		return sensorWebservice.getRoomTemperature(endpoint);
	}


	public boolean isTemperatureTooHigh() throws SensorConnectionProblemException, IncorrectDataException {
		
		final int MAX_TEMPERATURE = 80;
		
		// If any temperature exceeds MAX_TEMPERATURE, then the 
		// temperature is too high
		for(String endpoint : sensorRepository.getEndpoints()) {
			
			if(getTemperature(endpoint) > MAX_TEMPERATURE)
					return true;
		}
		
		return false;
		
	}
	
}

package es.upm.grise.profundizacion.td3;

import es.upm.grise.profundizacion.td3.exceptions.ConfigurationFileProblemException;
import es.upm.grise.profundizacion.td3.exceptions.DatabaseProblemException;
import es.upm.grise.profundizacion.td3.exceptions.IncorrectDataException;
import es.upm.grise.profundizacion.td3.exceptions.SensorConnectionProblemException;

public class FireAlarmApp {

	public static void main(String[] args) {

		try {
			DBConnectorImpl dbConnectorImpl = new DBConnectorImpl();
			FireAlarm fireAlarm = new FireAlarm(dbConnectorImpl);

			// Main loop
			while (true) {

				if (fireAlarm.isTemperatureTooHigh()) {
					
					// ...
				}

			}

		} catch (ConfigurationFileProblemException e) {

			// ...

		} catch (DatabaseProblemException e) {

			// ...

		} catch (SensorConnectionProblemException e) {

			// ...

		} catch (IncorrectDataException e) {

			// ...

		}

	}

}

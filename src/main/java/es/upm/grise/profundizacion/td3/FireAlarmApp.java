package es.upm.grise.profundizacion.td3;

import java.util.Properties;

public class FireAlarmApp {

	public static void main(String[] args) {

		try {
			Properties configProperties = new Properties();
			FireAlarm fireAlarm = new FireAlarm(configProperties);

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

package es.upm.grise.profundizacion.td3;

public class FireAlarmApp {

	public static void main(String[] args) {

		try {

			FireAlarm fireAlarm = new FireAlarm();

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

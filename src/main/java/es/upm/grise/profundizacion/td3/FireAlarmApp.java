package es.upm.grise.profundizacion.td3;

public class FireAlarmApp {

	public static void main(String[] args) {

		try {

			SensorWebservice sensorWebservice = new SensorWebserviceImpl();
			SensorRepository sensorRepository = new SensorRepository();

			FireAlarm fireAlarm = new FireAlarm(sensorWebservice, sensorRepository);

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

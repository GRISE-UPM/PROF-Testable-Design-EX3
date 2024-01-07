package es.upm.grise.profundizacion.td3;

public interface SensorWebservice {

    int getRoomTemperature(String endpoint) throws SensorConnectionProblemException, IncorrectDataException;
}

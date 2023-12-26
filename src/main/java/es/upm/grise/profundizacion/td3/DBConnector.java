package es.upm.grise.profundizacion.td3;

import java.util.Map;

import es.upm.grise.profundizacion.td3.exceptions.DatabaseProblemException;

public interface DBConnector {
    public void connect(String dbConnection) throws DatabaseProblemException;
    public Map<String, String> getSensors() throws DatabaseProblemException;
    public void closeConnection() throws DatabaseProblemException;
}

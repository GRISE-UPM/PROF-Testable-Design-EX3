package es.upm.grise.profundizacion.td3;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;

import uk.org.webcompere.systemstubs.jupiter.*;
import uk.org.webcompere.systemstubs.environment.*;

import static org.mockito.Mockito.*;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

@ExtendWith(SystemStubsExtension.class)
public class FireAlarmTest {
	@SystemStub
	private EnvironmentVariables envVars;
	private File tmpDir;
	private FireAlarm fireAlarm;
	private ObjectMapper mapMock;
	
	@BeforeEach
	void setup(@TempDir File tempDir) throws ConfigurationFileProblemException, DatabaseProblemException {
		envVars.set("firealarm.location", System.getProperty("user.dir"));
		tmpDir = tempDir;
		fireAlarm = new FireAlarm();
		mapMock = spy(new ObjectMapper());
	}

	@Test
	public void testBadConfigProperties() throws Exception {
		envVars.set("firealarm.location", "/wrong/directory");
		
		assertThrowsExactly(ConfigurationFileProblemException.class, () ->
			{new FireAlarm();});
	}

	
	//Se crea un archivo temporal con configuración no válida
	@Test
	public void testNonExistingDB() throws Exception {
		final String configData = "dblocation=jdbc:sqlite:/wrong/db.db";
		File resources = new File(tmpDir, "resources");
		File badConfig = new File(resources, "config.properties");
		
		resources.mkdir();
		badConfig.createNewFile();
		
		FileOutputStream out = new FileOutputStream(badConfig);
		out.write(configData.getBytes());
		out.close();
		
		envVars.set("firealarm.location", tmpDir.getAbsolutePath());

		assertThrowsExactly(DatabaseProblemException.class, () ->
			{new FireAlarm();});
	}

	@Test
	public void testNonExistingEndpoint() throws Exception {
		final String badEndpoint = "wrong_endpoint";
		
		assertThrowsExactly(SensorConnectionProblemException.class, () ->
			{fireAlarm.getTemperature(badEndpoint);});
	}

	//El método getTemperature ahora recibe un ObjectMapper como argumento
	//además de ser protected para poder testearlo directamente
	//Se mantiene la API antigua con una versión que solo recibe un argumento
	@Test
	public void testEmptyJSON() throws Exception {
		final String validEndpoint = "kitchen";
		
		doReturn(mapMock.readTree("")).when(mapMock).readTree(any(URL.class));
		
		assertThrowsExactly(IncorrectDataException.class, () ->
			{fireAlarm.getTemperature(validEndpoint, mapMock);});
	}

	@Test
	public void testBadJSONValue() throws Exception {
		final String validEndpoint = "kitchen";
		final String badJSON = "{ \"temperature\" : \"33\" }";
		
		doReturn(mapMock.readTree(badJSON)).when(mapMock).readTree(any(URL.class));
		
		assertThrowsExactly(IncorrectDataException.class, () ->
			{fireAlarm.getTemperature(validEndpoint, mapMock);});
	}

	@Test
	public void testBadJSONKey() throws Exception {
		final String validEndpoint = "kitchen";
		final String badJSON = "{ \"badKey\" : 33 }";
		
		doReturn(mapMock.readTree(badJSON)).when(mapMock).readTree(any(URL.class));
		
		assertThrowsExactly(IncorrectDataException.class, () ->
			{fireAlarm.getTemperature(validEndpoint, mapMock);});
	}

	//El método isTemperatureTooHigh ahora recibe un ObjectMapper como argumento
	//Se mantiene la API antigua con una versión public que no recibe ningún argumento
	//El método original se marca protected
	@Test
	public void testIsTemperatureHighFalse() throws Exception {
		final String goodJSON = "{ \"temperature\" : 33 }";
		
		doReturn(mapMock.readTree(goodJSON)).when(mapMock).readTree(any(URL.class));
		
		assertFalse(fireAlarm.isTemperatureTooHigh(mapMock));
	}

	@Test
	public void testIsTemperatureHighTrue() throws Exception {
		final String goodJSON = "{ \"temperature\" : 81 }";
		
		doReturn(mapMock.readTree(goodJSON)).when(mapMock).readTree(any(URL.class));
		
		assertTrue(fireAlarm.isTemperatureTooHigh(mapMock));
	}
}

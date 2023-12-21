package es.upm.grise.profundizacion.td3;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.fasterxml.jackson.databind.JsonNode;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.io.IOException;

@ExtendWith(SystemStubsExtension.class)
public class FireAlarmTest {
	FireAlarm fireAlarm;
	
	@SystemStub
	private EnvironmentVariables envVariables = new EnvironmentVariables("firealarm.location", System.getProperty("user.dir"));
	
	@BeforeEach 
	public void setUp() throws ConfigurationFileProblemException, DatabaseProblemException {
		fireAlarm = new FireAlarm();}
	
	@Test
	public void configFileNotFound() {
		assertThrows(ConfigurationFileProblemException.class, () -> {
			envVariables.set("firealarm.location", null);
			new FireAlarm();});}
	
	@Test
	public void databaseError() {
		assertThrows(DatabaseProblemException.class, () -> fireAlarm.conectarDB("null"));}

	@Test
	public void endPointNotUsable(){ 
		assertThrows(SensorConnectionProblemException.class, () -> fireAlarm.getTemperature("others"));}
	
	@Test 
	public void responseWithoutJSON() throws IOException  {
		JsonNode result = null;
		assertThrows(IncorrectDataException.class, () -> fireAlarm.validarData(result));}
	
	@Test 
	public void temperatureIsOk() throws SensorConnectionProblemException, IncorrectDataException {
		assertFalse(fireAlarm.isTemperatureTooHigh());}
	
	@Test 
	public void temperatureIsTooHigh() throws SensorConnectionProblemException, IncorrectDataException {
		fireAlarm.MAX_TEMPERATURE=0;
		assertTrue(fireAlarm.isTemperatureTooHigh());}
}
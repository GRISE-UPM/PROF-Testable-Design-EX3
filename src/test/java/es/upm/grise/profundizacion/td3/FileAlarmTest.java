package es.upm.grise.profundizacion.td3;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;

public class FileAlarmTest {

	FireAlarm fireAlarm;
	//Sea modificado el ObjectMapper de getTemperature para que se una variable global y asi poder usar el siguiente mock
	ObjectMapper objMapMock = mock(ObjectMapper.class);

	@SystemStub
	private EnvironmentVariables ev = new EnvironmentVariables();
	static String save;

	@BeforeAll
	public static void setup() throws IOException {
		save = leerArchivo("resources\\config.properties");
	}

	@BeforeEach
	public void beforeEach() throws ConfigurationFileProblemException, DatabaseProblemException, IOException {
		ev.set("firealarm.location", System.getProperty("user.dir"));
		crearArchivo("resources\\config.properties");
		sobrescribirArchivo("resources\\config.properties", save);
	}

	@Test
	public void ficheroNoLocalizado() throws IOException {
		borrarArchivo("resources\\config.properties");
		assertThrows(ConfigurationFileProblemException.class, () -> new FireAlarm());
	}

	@Test
	public void errorDB() throws SensorConnectionProblemException, IncorrectDataException, IOException {
		sobrescribirArchivo("resources\\config.properties", "dblocation=nothing");
		assertThrows(DatabaseProblemException.class, () -> new FireAlarm());
	}

	@Test
	public void errorREST() throws SensorConnectionProblemException, IncorrectDataException,
			ConfigurationFileProblemException, DatabaseProblemException {
		fireAlarm = new FireAlarm();
		assertThrows(SensorConnectionProblemException.class, () -> fireAlarm.getTemperature("Nothing"));
	}

	@Test
	public void errorTemperatura() throws SensorConnectionProblemException, IncorrectDataException,
			ConfigurationFileProblemException, DatabaseProblemException, IOException {
		fireAlarm = new FireAlarm();
		String jsonStr = "{\"room\": \"kitchen\"}";

		when(objMapMock.readTree((URL) any())).thenReturn(new ObjectMapper().readTree(jsonStr));
		fireAlarm.setMapper(objMapMock);
		assertThrows(IncorrectDataException.class, () -> fireAlarm.getTemperature("kitchen"));
	}

	@Test
	public void errorMenorMaxTemperatura() throws SensorConnectionProblemException, IncorrectDataException, JsonMappingException, JsonProcessingException, IOException, ConfigurationFileProblemException, DatabaseProblemException {
		String jsonStr = "{\"temperature\": 40}";
		fireAlarm = new FireAlarm();
		when(objMapMock.readTree((URL) any())).thenReturn(new ObjectMapper().readTree(jsonStr));
		fireAlarm.setMapper(objMapMock);
		assertFalse(fireAlarm.isTemperatureTooHigh());
	}

	@Test
	public void errorMayorMaxTemperatura() throws SensorConnectionProblemException, IncorrectDataException, ConfigurationFileProblemException, DatabaseProblemException, JsonMappingException, JsonProcessingException, IOException {
		String jsonStr = "{\"temperature\": 100}";
		fireAlarm = new FireAlarm();
		when(objMapMock.readTree((URL) any())).thenReturn(new ObjectMapper().readTree(jsonStr));
		fireAlarm.setMapper(objMapMock);
		assertTrue(fireAlarm.isTemperatureTooHigh());
	}

	public static boolean borrarArchivo(String rutaArchivo) {
		File archivo = new File(rutaArchivo);

		if (archivo.exists()) {
			if (archivo.delete()) {
				System.out.println("Archivo borrado: " + rutaArchivo);
				return true;
			} else {
				System.out.println("No se pudo borrar el archivo.");
				return false;
			}
		} else {
			System.out.println("El archivo no existe.");
			return false;
		}
	}

	// Función para crear un archivo
	public static boolean crearArchivo(String rutaArchivo) {
		File archivo = new File(rutaArchivo);

		try {
			if (archivo.createNewFile()) {
				System.out.println("Archivo creado: " + rutaArchivo);
				return true;
			} else {
				System.out.println("El archivo ya existe.");
				return false;
			}
		} catch (Exception e) {
			System.out.println("Error al crear el archivo.");
			e.printStackTrace();
			return false;
		}
	}

	private static String leerArchivo(String rutaArchivo) throws IOException {
		File archivo = new File(rutaArchivo);

		if (archivo.exists()) {
			try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
				StringBuilder contenido = new StringBuilder();
				String linea;
				while ((linea = br.readLine()) != null) {
					contenido.append(linea).append("\n");
				}
				return contenido.toString();
			}
		} else {
			return "El archivo no existe.";
		}
	}

	// Función para sobrescribir el contenido de un archivo
	private static void sobrescribirArchivo(String rutaArchivo, String nuevoContenido) throws IOException {
		try (FileWriter writer = new FileWriter(rutaArchivo)) {
			writer.write(nuevoContenido);
			System.out.println("Contenido sobrescrito en el archivo.");
		}
	}
}

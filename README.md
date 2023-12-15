La aplicación ``FireAlarmApp`` gestiona un conjunto de sensores en un domicilio. Esta aplicación está disponible en ``https://github.com/GRISE-UPM/PROF-Testable-Design-EX3``. 

**Realice un fork de este repositorio.**

Los sensores están listados en una base de datos. Actualmente, la base de datos se ha implementado mediante SQLite (en un fichero denominado ``sensors.db``) que está almacenada en el directorio ``resources/``. Sin embargo, la base de datos podría implementarse mediante otro gestor relacional. Para facilitar el cambio, el fichero ``config.properties`` almacena la cadena de acceso a la base de daros, que incluye el protocolo utilizado (JDBC en este caso).

Como el ``.jar`` de esta aplicación puede invocarse desde cualquier directorio de trabajo, la aplicación ``FireAlarmApp`` utiliza una cadena de entorno denominada ``firealarm.location`` para localizar el fichero ``config.properties``.

La base de datos contiene una única tabla denominada sensors. Esta tabla almacena la ubicación (``room``) y el endpoint REST (``endpoint``) donde se puede acceder la lectura del sensor mediante el método ``GET``.

En el momento en que se instancia la clase ``FireAlarm``, se almacena en el ``HashMap sensors`` todos los sensores del domicilio. El método privado ``getTemperature()`` permite acceder a la temperatura de un determinado sensor. El endpoint REST devuelve un objeto JSON sencillo con una única clave denominada ``“temperature”``, que tiene asociado como valor la temperatura medida por el sensor.

La aplicación ``FireAlarmApp`` tiene como objetivo principal evitar los incendios en domicilios. Para ello, la clase ``FireAlarm`` posee un método ``isTemperatureTooHigh()``. Este método devuelve ``true`` si alguno de los sensores detecta una temperatura superior a ``MAX_TEMPERATURE``. Este valor está establecido por código para evitar riesgos.

**Realizar las siguientes pruebas:**
- Cuando no se puede localizar el fichero ``config.properties``, la clase ``FireAlarm`` lanza una ``ConfigurationFileProblemException``.
- Cualquier error de la base de datos, ej: problema de conexión o error en consulta, implica el lanzamiento de una ``DatabaseProblemException``.
- Cuando el endpoint REST no es utilizable, la aplicación lanza una ``SensorConnectionProblemException``.
- Si el objeto JSON devuelto no contiene la clave ``“temperature”``, o el valor devuelto no es entero, la aplicación lanza una ``IncorrectDataException``.
- Cuando todos los sensores devuelven una temperatura ``<= MAX_TEMPERATURE``, el método ``isTemperatureTooHigh()`` devuelve ``false``.
- Cuando algún sensor devuelve una temperatura ``> MAX_TEMPERATURE``, el método ``isTemperatureTooHigh()`` devuelve ``true``.

Para realizar las pruebas anteriores será necesario modificar el diseño de las clases del programa. Indique mediante comentario en la clase de test los cambios realizados. También es necesario usar mocks y ``System Stubs``.

**Para entregar el ejercicio, realice un pull request al repositorio original.**

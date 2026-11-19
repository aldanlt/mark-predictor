package es.uvigo.markpredictor.config;

/**
 * PATRÓN SINGLETON (P7 · HU-03).
 *
 * Guarda en un único sitio la configuración de la aplicación: conexión a MySQL y
 * direcciones de los microservicios Python. Los valores se leen de variables de entorno
 * (las define docker-compose.yml) y, si no existen, se usa un valor por defecto.
 *
 * - El constructor es privado: nadie puede hacer "new AppConfig()".
 * - getInstance() crea la instancia la primera vez y después devuelve siempre la misma.
 */
public class AppConfig {

    private static AppConfig instance;

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private final String predictionServiceUrl;
    private final String reportServiceUrl;

    private AppConfig() {
        String dbHost = env("DB_HOST", "localhost");
        String dbPort = env("DB_PORT", "3306");
        String dbName = env("DB_NAME", "mark_predictor");
        dbUrl = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName
                + "?useSSL=false&allowPublicKeyRetrieval=true";
        dbUser = env("DB_USER", "mark");
        dbPassword = env("DB_PASSWORD", "mark");

        predictionServiceUrl = "http://" + env("PREDICTION_SERVICE_HOST", "localhost")
                + ":" + env("PREDICTION_SERVICE_PORT", "8000");
        reportServiceUrl = "http://" + env("REPORT_SERVICE_HOST", "localhost")
                + ":" + env("REPORT_SERVICE_PORT", "8001");
    }

    public static synchronized AppConfig getInstance() {
        if (instance == null) {
            instance = new AppConfig();
        }
        return instance;
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value == null || value.isBlank()) ? defaultValue : value;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public String getPredictionServiceUrl() {
        return predictionServiceUrl;
    }

    public String getReportServiceUrl() {
        return reportServiceUrl;
    }
}

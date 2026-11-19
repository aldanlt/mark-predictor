package es.uvigo.markpredictor.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppConfigTest {

    @Test
    void siempreDevuelveLaMismaInstancia() {
        assertSame(AppConfig.getInstance(), AppConfig.getInstance());
    }

    @Test
    void elConstructorEsPrivado() throws NoSuchMethodException {
        int modifiers = AppConfig.class.getDeclaredConstructor().getModifiers();
        assertTrue(Modifier.isPrivate(modifiers));
    }

    @Test
    void tieneLasDireccionesDeMysqlYDeLosMicroservicios() {
        AppConfig config = AppConfig.getInstance();
        assertTrue(config.getDbUrl().startsWith("jdbc:mysql://"));
        assertTrue(config.getPredictionServiceUrl().startsWith("http://"));
        assertTrue(config.getReportServiceUrl().startsWith("http://"));
    }
}

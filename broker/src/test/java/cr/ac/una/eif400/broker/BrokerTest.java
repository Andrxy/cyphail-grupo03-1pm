package cr.ac.una.eif400.broker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class BrokerTest {
    private Broker broker;

    @BeforeEach
    void setUp() {
        broker = Broker.getInstance();
        broker.setCurrentGraph(null);
    }

    @Test
    @DisplayName("Debe retornar siempre la misma instancia (Singleton)")
    void testGetInstance() {
        Broker instance1 = Broker.getInstance();
        Broker instance2 = Broker.getInstance();

        assertSame(instance1, instance2, "Broker debe ser un Singleton");
    }

    @Test
    void testGraphExists() {
        String graphName = "Amigos";

        boolean exists = broker.graphExists(graphName);

        assertTrue(exists);
    }
}
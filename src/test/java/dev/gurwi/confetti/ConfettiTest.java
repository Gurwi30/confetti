package dev.gurwi.confetti;

import dev.gurwi.confetti.configuration.base.Configuration;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConfettiTest {

    @Test
    void load() {
        Confetti confetti = new Confetti();
        confetti.load(TestConfig.class);

        assertEquals("10", TestConfig.TEST, "TestConfig.TEST should be '10'");
        assertIterableEquals(List.of("a", "b", "c"), TestConfig.ARRAY, "TestConfig.ARRAY contents mismatch");
    }

    @Test
    void recordDeserializer() {
        Confetti confetti = new Confetti();
        Configuration cfg = confetti.load(TestConfig.class);
        Something something = cfg.get("object", Something.class);

        assertNotNull(something, "Something is null");
        assertEquals(10, something.something());
    }

}
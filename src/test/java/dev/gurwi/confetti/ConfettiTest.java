package dev.gurwi.confetti;

import org.junit.jupiter.api.Test;

class ConfettiTest {

    @Test
    void load() {
        Confetti confetti = new Confetti();
        confetti.load(TestConfig.class);
    }

}
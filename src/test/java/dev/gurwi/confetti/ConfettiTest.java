package dev.gurwi.confetti;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

class ConfettiTest {

    @Test
    void load() {
        Confetti confetti = new Confetti();
        confetti.load(TestConfig.class);

        System.out.println(TestConfig.TEST);
        System.out.println(Arrays.toString(TestConfig.ARRAY));

//        assert TestConfig.TEST.equals("10");
//        assert Arrays.equals(TestConfig.ARRAY, new String[]{"a", "b", "c"});
    }

}
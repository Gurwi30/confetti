package dev.gurwi.confetti;

import dev.gurwi.confetti.configuration.base.Configuration;
import dev.gurwi.confetti.element.YamlElement;
import dev.gurwi.confetti.element.YamlObject;
import dev.gurwi.confetti.element.YamlPrimitive;
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

        assertInstanceOf(YamlPrimitive.class, cfg.get("test", YamlElement.class));

        assertEquals(10, cfg.get("nested.value-1"));
        assertEquals(20, cfg.get("nested.value-2"));

        assertEquals(10, TestConfig.NESTED_VALUE_1);
        assertEquals(TestEnum.SOME_ENUM, TestConfig.ENUM);
    }

    @Test
    void write() {
        Confetti confetti = new Confetti();
        Configuration cfg = confetti.load(TestConfig.class);

        cfg.set("write",
                new YamlObject()
                        .set("str", new YamlPrimitive("some-string"))
                        .set("object", new Something(10))
                        .set("enum", TestEnum.SOME_ENUM)
        );
        
    }

}
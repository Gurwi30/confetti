package dev.gurwi.confetti;

import dev.gurwi.confetti.annotation.Path;
import dev.gurwi.confetti.annotation.ResourceConfig;

@ResourceConfig("test.yml")
public class TestConfig {

    @Path("test")
    public static String TEST = "default";

    @Path("array")
    public static String[] ARRAY = {"one", "two", "three"};

}

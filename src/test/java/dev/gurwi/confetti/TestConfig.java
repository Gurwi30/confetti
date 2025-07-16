package dev.gurwi.confetti;

import dev.gurwi.confetti.annotation.Config;
import dev.gurwi.confetti.annotation.Path;

import java.util.ArrayList;
import java.util.List;

@Config(value = "config.yml", defaultResource = "test.yml", autoSave = true)
public class TestConfig {

    @Path("test")
    public static String TEST = "default";

    @Path("array")
    public static List<String> ARRAY = new ArrayList<>();

    @Path("nested.value-1")
    public static Integer NESTED_VALUE_1;

}

package dev.gurwi.confetti;

import dev.gurwi.confetti.annotation.Path;
import dev.gurwi.confetti.annotation.ResourceConfig;

import java.util.ArrayList;
import java.util.List;

@ResourceConfig("test.yml")
public class TestConfig {

    @Path("test")
    public static String TEST = "default";

    @Path("array")
    public static List<String> ARRAY = new ArrayList<>();

    @Path("nested.value-1")
    public static Integer NESTED_VALUE_1;

}

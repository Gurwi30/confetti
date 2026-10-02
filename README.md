# Confetti

A lightweight YAML configuration library for Java, built on top of SnakeYAML.

[![Java](https://img.shields.io/badge/Java-21%2B-030711?style=for-the-badge\&labelColor=030711\&color=0b1220)](https://www.java.com/)
[![SnakeYAML](https://img.shields.io/badge/SnakeYAML-2.4-030711?style=for-the-badge\&labelColor=030711\&color=0b1220)](https://bitbucket.org/snakeyaml/snakeyaml)
[![License](https://img.shields.io/badge/License-MIT-030711?style=for-the-badge\&labelColor=030711\&color=0b1220)](LICENSE)

[![Documentation](https://docs.gurwi.dev/api/v1/badge)](https://docs.gurwi.dev/confetti)
[![Maven Central](https://img.shields.io/badge/Maven%20Central-0.1.5-030711?style=for-the-badge\&labelColor=030711\&color=0b1220)](https://central.sonatype.com/artifact/dev.gurwi/confetti)

## Overview

Confetti provides a simple API for loading, reading, modifying, and saving YAML configuration files.

It is designed around a few simple ideas:

* Load YAML files directly from disk or from the classpath
* Access values using dot-separated paths
* Deserialize YAML values into Java types
* Map configuration values directly into static fields
* Create and modify configuration sections
* Automatically save file configurations after changes
* Reload configurations at runtime
* Register custom serializers and deserializers for your own types
* Define read-only configurations from classpath resources

Confetti is built on SnakeYAML and provides a higher-level API around YAML configuration management.

## Installation

Confetti is published to Maven Central.

### Gradle

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.gurwi:confetti:0.1.5")
}
```

### Maven

```xml
<dependency>
    <groupId>dev.gurwi</groupId>
    <artifactId>confetti</artifactId>
    <version>0.1.5</version>
</dependency>
```

### Manual Build

If you want to build Confetti from source:

```bash
git clone https://github.com/Gurwi30/confetti.git
cd confetti
./gradlew build
```

For Windows:

```powershell
git clone https://github.com/Gurwi30/confetti.git
cd confetti
.\gradlew.bat build
```

## Quick Start

Create a YAML file:

```yaml
server:
  host: localhost
  port: 8080

database:
  enabled: true
```

Load it with Confetti:

```java
import dev.gurwi.confetti.Confetti;
import dev.gurwi.confetti.configuration.base.Configuration;

public class Main {
    public static void main(String[] args) {
        Confetti confetti = new Confetti();

        Configuration config = confetti
                .fromFile("config.yml")
                .load();

        String host = config.getString("server.host");
        Integer port = config.getInt("server.port");
        Boolean databaseEnabled = config.getBool("database.enabled");
    }
}
```

## Annotation-Based Configuration

Confetti can map YAML values directly to static fields using `@Config`.

```java
import dev.gurwi.confetti.annotation.Config;
import dev.gurwi.confetti.annotation.Path;

@Config(
    value = "config.yml",
    autoSave = true
)
public class AppConfig {

    @Path("server.host")
    public static String HOST = "localhost";

    @Path("server.port")
    public static Integer PORT = 8080;

    @Path("database.enabled")
    public static Boolean DATABASE_ENABLED = true;
}
```

Load the configuration with:

```java
Confetti confetti = new Confetti();

confetti.load(AppConfig.class);
```

Values are then available directly through the fields:

```java
System.out.println(AppConfig.HOST);
System.out.println(AppConfig.PORT);
```

## Resource Configuration

Confetti provides `@ResourceConfig` for configurations stored in the application's classpath.

```java
import dev.gurwi.confetti.annotation.Path;
import dev.gurwi.confetti.annotation.ResourceConfig;

@ResourceConfig("defaults.yml")
public class Defaults {

    @Path("server.host")
    public static String HOST;

    @Path("server.port")
    public static Integer PORT;

    @Path("database.enabled")
    public static Boolean DATABASE_ENABLED;
}
```

Place the resource under `src/main/resources`:

```text
src/
└── main/
    └── resources/
        └── defaults.yml
```

Load the resource configuration with:

```java
Confetti confetti = new Confetti();

confetti.load(Defaults.class);
```

`@ResourceConfig` is intended for read-only classpath configuration. It does not create or modify a file on disk.

A class should use either `@Config` or `@ResourceConfig` as its configuration source.

## Configuration Sources

Confetti supports both filesystem and classpath resources.

### File

```java
Configuration config = confetti
        .fromFile("config.yml")
        .load();
```

You can also provide a parent directory:

```java
Configuration config = confetti
        .fromFile("config.yml", dataFolder)
        .load();
```

### Classpath Resource

```java
Configuration config = confetti
        .fromResource("defaults.yml")
        .load();
```

Resource configurations are read-only.

## Reading Values

Confetti provides typed getters for common Java types:

```java
String name = config.getString("server.name");
Integer port = config.getInt("server.port");
Long timeout = config.getLong("server.timeout");
Double ratio = config.getDouble("server.ratio");
Float scale = config.getFloat("server.scale");
Boolean enabled = config.getBool("server.enabled");
```

You can also retrieve arbitrary objects:

```java
Object value = config.get("server.name");
```

Or provide a default:

```java
Object value = config.get("server.name", "localhost");
```

## Nested Paths

Nested YAML values can be accessed using dot-separated paths.

Given:

```yaml
database:
  connection:
    host: localhost
    port: 5432
```

You can access the values using:

```java
String host = config.getString("database.connection.host");
Integer port = config.getInt("database.connection.port");
```

## Modifying Configuration

File configurations can be modified using `set`:

```java
config.set("server.port", 8080);
config.set("server.enabled", true);
```

You can also create sections:

```java
config.createSection("database");
```

Changes can be persisted with:

```java
((FileConfiguration) config).save();
```

With `autoSave` enabled, changes are saved automatically.

Resource configurations are read-only.

## Reloading

File configurations can be reloaded:

```java
FileConfiguration config = confetti
        .fromFile("config.yml")
        .load();

config.reload();
```

Confetti can also reload all registered file configurations:

```java
confetti.reloadAll();
```

When using annotation-based configurations, `reloadAll()` restores the initial field values and loads the updated configuration values back into the annotated fields.

## Default Resources

A file configuration can use a classpath resource as its default configuration.

```java
@Config(
    value = "config.yml",
    defaultResource = "config.yml"
)
public class AppConfig {
}
```

If the target file does not exist, Confetti creates it from the specified classpath resource.

## Custom Serialization

Confetti supports custom serializers and deserializers for types that require special handling.

```java
confetti.registerSerializer(MyType.class, value -> {
    return ...;
});
```

Custom deserializers can be registered in the same way:

```java
confetti.registerDeserializer(MyType.class, (element, type) -> {
    return ...;
});
```

You can also register a combined serializer/deserializer:

```java
confetti.registerSerDe(MyType.class, mySerDe);
```

## Requirements

* Java 21 or newer
* SnakeYAML 2.4

## Project Status

Confetti is currently under active development.

The public API may change between versions while the library evolves.

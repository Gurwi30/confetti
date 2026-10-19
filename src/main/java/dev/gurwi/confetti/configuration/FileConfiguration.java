package dev.gurwi.confetti.configuration;

import dev.gurwi.confetti.Confetti;
import dev.gurwi.confetti.configuration.base.Configuration;
import dev.gurwi.confetti.configuration.base.ConfigurationSection;
import dev.gurwi.confetti.element.YamlElement;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.emitter.Emitter;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.resolver.Resolver;
import org.yaml.snakeyaml.serializer.Serializer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FileConfiguration extends Configuration {

    private final File file;

    private String defaultResource = null;
    private boolean autoSave = false;
    private Node commentSource = null;

    public FileConfiguration(Confetti confetti, @NotNull File file) {
        super(confetti, file.getPath());

        this.file = file;
    }

    public FileConfiguration withAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
        return this;
    }

    public FileConfiguration withDefaultResource(String defaultResource) {
        this.defaultResource = defaultResource;
        return this;
    }

    @Override
    protected @NotNull Map<String, YamlElement> loadData() {
        createFile();

        String content;

        try {
            content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.commentSource = yaml.compose(new StringReader(content));

        Map<String, Object> raw = yaml.load(content);

        return raw == null ? new LinkedHashMap<>() : YamlElement.adapt(raw);
    }

    public void reload() {
        this.data = loadData();
        this.section = new Section(confetti, this, data);

        syncAnnotationClasses();
    }

    public void save() {
        Node root = representer.represent(data);

        if (commentSource != null) {
            copyComments(commentSource, root);
        }

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            Emitter emitter = new Emitter(writer, dumperOptions);
            Serializer serializer = new Serializer(emitter, new Resolver(), dumperOptions, null);

            serializer.open();
            serializer.serialize(root);
            serializer.close();

            syncAnnotationClasses();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ConfigurationSection set(String path, Object value) {
        super.set(path, value);
        runAutoSave();

        return this;
    }

    @Override
    public ConfigurationSection createSection(String path) {
        ConfigurationSection ret = super.createSection(path);
        runAutoSave();

        return ret;
    }

    private void copyComments(@NotNull Node source, @NotNull Node target) {
        target.setBlockComments(source.getBlockComments());
        target.setInLineComments(source.getInLineComments());
        target.setEndComments(source.getEndComments());

        if (source instanceof MappingNode sourceMap && target instanceof MappingNode targetMap) {
            Map<String, NodeTuple> sourceTuples = new HashMap<>();

            for (NodeTuple tuple : sourceMap.getValue()) {
                if (tuple.getKeyNode() instanceof ScalarNode key) {
                    sourceTuples.put(key.getValue(), tuple);
                }
            }

            for (NodeTuple tuple : targetMap.getValue()) {
                if (!(tuple.getKeyNode() instanceof ScalarNode key)) continue;

                NodeTuple match = sourceTuples.get(key.getValue());

                if (match == null) continue;

                copyComments(match.getKeyNode(), tuple.getKeyNode());
                copyComments(match.getValueNode(), tuple.getValueNode());
            }
        } else if (source instanceof SequenceNode sourceSeq && target instanceof SequenceNode targetSeq) {
            List<Node> sourceItems = sourceSeq.getValue();
            List<Node> targetItems = targetSeq.getValue();

            if (sourceItems.size() != targetItems.size()) return;

            for (int i = 0; i < sourceItems.size(); i++) {
                copyComments(sourceItems.get(i), targetItems.get(i));
            }
        }
    }

    private void createFile() {
        File parent = file.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        if (!file.exists()) {
            if (defaultResource != null) {
                copyDefaultResource(defaultResource, file);
            } else {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create empty config file: " + file, e);
                }
            }
        }
    }

    private void copyDefaultResource(String resourceName, File targetFile) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (in == null) {
                throw new FileNotFoundException("Resource not found in classpath: " + resourceName);
            }

            Files.copy(in, targetFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy default resource: " + resourceName, e);
        }
    }

    private void runAutoSave() {
        if (autoSave) save();
    }

    private void syncAnnotationClasses() {
        getLoadedClass().ifPresent(data -> {
            data.restoreInitialValues();
            confetti.loadIntoClass(getClass(), this);
        });
    }

}
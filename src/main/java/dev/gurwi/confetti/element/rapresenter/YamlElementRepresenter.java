package dev.gurwi.confetti.element.rapresenter;

import dev.gurwi.confetti.element.YamlArray;
import dev.gurwi.confetti.element.YamlElement;
import dev.gurwi.confetti.element.YamlObject;
import dev.gurwi.confetti.element.YamlPrimitive;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.util.ArrayList;
import java.util.List;

public class YamlElementRepresenter extends Representer {

    public YamlElementRepresenter(DumperOptions options) {
        super(options);

        this.representers.put(YamlPrimitive.class, data -> representYamlElement((YamlElement) data));
        this.representers.put(YamlArray.class, data -> representYamlElement((YamlElement) data));
        this.representers.put(YamlObject.class, data -> representYamlElement((YamlElement) data));
    }

    private Node representYamlElement(@NotNull YamlElement element) {
        return switch (element) {
            case YamlPrimitive primitive -> representScalar(primitive);
            case YamlArray array -> representSequence(Tag.SEQ, array.getElements(), getDefaultFlowStyle());
            case YamlObject object -> representMapping(object);

            default -> throw new IllegalStateException("Unexpected value: " + element);
        };
    }

    private Node representScalar(@NotNull YamlPrimitive primitive) {
        Object value = primitive.getValue();
        Tag tag = Tag.STR;
        String valueStr = String.valueOf(value);

        if (value instanceof Number) tag = Tag.INT;
        else if (value instanceof Boolean) tag = Tag.BOOL;

        return representScalar(tag, valueStr);
    }

    @Contract("_ -> new")
    private @NotNull Node representMapping(@NotNull YamlObject object) {
        List<NodeTuple> valueList = new ArrayList<>();

        for (var entry : object.getData().entrySet()) {
            Node keyNode = representData(entry.getKey());
            Node valueNode = representYamlElement(entry.getValue());
            valueList.add(new NodeTuple(keyNode, valueNode));
        }

        return new MappingNode(Tag.MAP, valueList, getDefaultFlowStyle());
    }

}

package model;

import java.util.List;

public class Tree {
    private String root;
    private List<Node> node;
    private String type;
    private List<Field> fields;

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public List<Node> getNode() {
        return node;
    }

    public void setNode(List<Node> node) {
        this.node = node;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fieldList) {
        this.fields = fieldList;
    }
}

package model;

import java.util.List;

public class Node {
    private String name;
    private List<Field> fields;
    private String base;
    private String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getType() {
        return type;
    }

    public void setType(String rel) {
        this.type = rel;
    }
}

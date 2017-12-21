package org.ballerinalang.docgen.model;

/**
 * Variable which stores the name, type and description
 */
public class Variable {
    public final String name;

    public final String dataType;

    public final String description;

    /**
     * Constructor
     * @param name variable name
     * @param dataType data type of the variable
     * @param description description of the variable
     */
    public Variable(String name, String dataType, String description) {
        this.name = name;
        this.dataType = dataType;
        this.description = description;
    }

    @Override
    public String toString() {
        if (name.length() == 0) {
            return dataType;
        }
        return dataType + " " + name;
    }
}

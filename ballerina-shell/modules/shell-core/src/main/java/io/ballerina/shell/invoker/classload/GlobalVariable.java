package io.ballerina.shell.invoker.classload;

import java.util.Collection;
import java.util.Objects;

/**
 * A global variable in the REPL.
 * Needs information such as variable name, its code and
 * whether it is an any/error/(any|error)
 */
public class GlobalVariable {
    private final String type;
    private final String variableName;
    private final ElevatedType elevatedType;

    public GlobalVariable(String type, String variableName, ElevatedType elevatedType) {
        this.type = type;
        this.variableName = variableName;
        this.elevatedType = elevatedType;
    }

    /**
     * Function to check whether a variable name is defined inside a collection.
     *
     * @param globalVariables Global variable collection to search.
     * @param variableName    Variable name to search.
     * @return Whether the variable is contained inside the collection.
     */
    public static boolean isDefined(Collection<GlobalVariable> globalVariables, String variableName) {
        return globalVariables.contains(new GlobalVariable("", variableName, null));
    }

    public String getType() {
        return type;
    }

    public String getVariableName() {
        return variableName;
    }

    public ElevatedType getElevatedType() {
        return elevatedType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GlobalVariable that = (GlobalVariable) o;
        return variableName.equals(that.variableName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(variableName);
    }

    @Override
    public String toString() {
        return String.format("<%s> %s %s", elevatedType, type, variableName);
    }
}

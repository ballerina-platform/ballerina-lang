package io.ballerina.shell.invoker.classload;

/**
 * Type after elevating the type of the visited node.
 * If not required, the type is fully visible.
 * Otherwise it would be one of any, error, any|error
 */
public enum ElevatedType {
    NONE("()"),
    ANY("any"),
    ERROR("error"),
    ANY_ERROR("(any|error)");

    private final String repr;

    ElevatedType(String repr) {
        this.repr = repr;
    }

    /**
     * NONE or ANY types can be assigned to any.
     *
     * @return Whether the variable that has this type
     * can be assigned to any type.
     */
    public boolean isAssignableToAny() {
        return this.equals(NONE) || this.equals(ANY);
    }

    @Override
    public String toString() {
        return repr;
    }
}

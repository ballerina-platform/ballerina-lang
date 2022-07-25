package io.ballerina.multiservice;

/**
 * Constants use for Solution Architecture model generation.
 */
public class MultiServiceModelingConstants {

    /**
     * Enum to select the type of the parameter.
     */
    public enum ParameterIn  {
        BODY("body"),
        QUERY("query"),
        HEADER("header"),
        PATH("path");

        private final String parameterIn;

        ParameterIn(String parameterIn) {
            this.parameterIn = parameterIn;
        }

        public String getValue() {
            return this.parameterIn;
        }
    }

    public static final String CAPABILITY_NAME = "multiServiceModelingService";
}

package org.ballerinalang.langserver.test.template;

import java.util.Locale;

/**
 * This class provides shared functionalities across Ballerina Test Templates.
 */
public abstract class AbstractTestTemplate implements BallerinaTestTemplate {
    protected static final String DEFAULT_IP = "0.0.0.0";
    protected static final String DEFAULT_PORT = "9092";
    protected static final String HTTP = "http://";
    protected static final String HTTPS = "https://";
    protected static final String WS = "ws://";
    protected static final String WSS = "wss://";

    /**
     * Uppercase case the first letter of this string.
     *
     * @param name name to be converted
     * @return converted string
     */
    protected static String upperCaseFirstLetter(String name) {
        return name.substring(0, 1).toUpperCase(Locale.getDefault()) + name.substring(1);
    }

    /**
     * Lowercase the first letter of this string.
     *
     * @param name name to be converted
     * @return converted string
     */
    protected static String lowerCaseFirstLetter(String name) {
        return name.substring(0, 1).toLowerCase(Locale.getDefault()) + name.substring(1);
    }
}

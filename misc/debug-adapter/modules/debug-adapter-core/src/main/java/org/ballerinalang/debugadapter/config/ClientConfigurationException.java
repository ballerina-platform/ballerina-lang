
package org.ballerinalang.debugadapter.config;

/**
 * Ballerina DAP Client Configuration related exceptions.
 *
 * @since 2.0.0
 */
public class ClientConfigurationException extends Exception {

    ClientConfigurationException(String message) {
        super(message);
    }
}

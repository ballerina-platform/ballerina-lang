package org.ballerinalang.swagger.model;

import org.ballerinalang.swagger.exception.BallerinaOpenApiException;

/**
 * Contract interface for creating a ballerina wrapper from a swagger parser object model.
 *
 * @param <C> Ballerina wrapper type that is being implemented
 * @param <D> Swagger parser model type
 */
public interface BallerinaSwaggerObject<C, D> {
    /**
     * Build the Ballerina context model {@code C} for Open API definition/component in {@code D}.
     *
     * @param definition Open Api definition or component
     * @return parsed context model {@code C} of Open Api definition/component {@code D}
     * @throws BallerinaOpenApiException on error when parsing the Open Api definition
     */
    C buildContext(D definition) throws BallerinaOpenApiException;

    /**
     * Retrieve the default value for this type.
     *
     * @return default values
     */
    C getDefaultValue();
}

/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.swagger.model;

import io.swagger.v3.oas.models.OpenAPI;
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
     * Build the Ballerina context model {@code C} for Open API definition/component in {@code D}.
     * <p>{@link OpenAPI} definition {@code openApi} can be used to access the parent context
     * helpful for building the current context</p>
     *
     * @param definition Open Api definition or component
     * @param openAPI parent openApi object model
     * @return parsed context model {@code C} of Open Api definition/component {@code D}
     * @throws BallerinaOpenApiException on error when parsing the Open Api definition
     */
    C buildContext(D definition, OpenAPI openAPI) throws BallerinaOpenApiException;

    /**
     * Retrieve the default value for this type.
     *
     * @return default values
     */
    C getDefaultValue();
}

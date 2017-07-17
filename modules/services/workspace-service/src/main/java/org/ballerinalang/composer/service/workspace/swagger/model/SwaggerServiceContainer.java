/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.composer.service.workspace.swagger.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

/**
 * A model container class which is the request and response.
 */
public class SwaggerServiceContainer {
    /**
     * Stores the ballerina source code.
     */
    @JsonProperty("ballerinaDefinition")
    private String ballerinaDefinition = null;
    
    /**
     * Stores the list of swagger definitions to each service in the ballerina source code.
     */
    @JsonProperty("swaggerDefinitions")
    private String[] swaggerDefinitions = null;
    
    public String getBallerinaDefinition() {
        return ballerinaDefinition;
    }
    
    public void setBallerinaDefinition(String ballerinaDefinition) {
        this.ballerinaDefinition = ballerinaDefinition;
    }
    
    public String[] getSwaggerDefinitions() {
        return swaggerDefinitions;
    }
    
    public void setSwaggerDefinitions(String[] swaggerDefinitions) {
        this.swaggerDefinitions = swaggerDefinitions;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SwaggerServiceContainer)) {
            return false;
        }
        
        SwaggerServiceContainer swaggerServiceContainer = (SwaggerServiceContainer) o;
        
        if (!getBallerinaDefinition().equals(swaggerServiceContainer.getBallerinaDefinition())) {
            return false;
        }
        
        return Arrays.equals(getSwaggerDefinitions(), swaggerServiceContainer.getSwaggerDefinitions());
    
    }
    
    @Override
    public int hashCode() {
        int result = getBallerinaDefinition().hashCode();
        result = 31 * result + Arrays.hashCode(getSwaggerDefinitions());
        return result;
    }
    
    @Override
    public String toString() {
        return "Service{" + "ballerinaDefinition='" + ballerinaDefinition + '\'' + ", swaggerDefinitions=" + Arrays
                .toString(swaggerDefinitions) + '}';
    }
}


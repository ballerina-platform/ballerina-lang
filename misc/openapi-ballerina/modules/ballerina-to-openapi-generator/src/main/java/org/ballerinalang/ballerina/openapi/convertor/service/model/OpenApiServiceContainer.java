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

package org.ballerinalang.ballerina.openapi.convertor.service.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.StringUtils;

/**
 * A model container class which is the request and response.
 */
public class OpenApiServiceContainer {
    /**
     * Stores the ballerina source code.
     */
    @JsonProperty("ballerinaDefinition")
    private String ballerinaDefinition = StringUtils.EMPTY;
    
    /**
     * Stores the list of openapi definitions to each service in the ballerina source code.
     */
    @JsonProperty("openApiDefinition")
    private String openApiDefinition = StringUtils.EMPTY;
    
    public String getBallerinaDefinition() {
        return ballerinaDefinition;
    }
    
    public void setBallerinaDefinition(String ballerinaDefinition) {
        this.ballerinaDefinition = ballerinaDefinition;
    }
    
    public String getOpenApiDefinition() {
        return openApiDefinition;
    }
    
    public void setOpenApiDefinition(String openApiDefinition) {
        this.openApiDefinition = openApiDefinition;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OpenApiServiceContainer)) {
            return false;
        }
        
        OpenApiServiceContainer that = (OpenApiServiceContainer) o;
        
        if (!getBallerinaDefinition().equals(that.getBallerinaDefinition())) {
            return false;
        }
        return getOpenApiDefinition().equals(that.getOpenApiDefinition());
    
    }
    
    @Override
    public int hashCode() {
        int result = getBallerinaDefinition().hashCode();
        result = 31 * result + getOpenApiDefinition().hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "OpenApiServiceContainer{" + "ballerinaDefinition='" + ballerinaDefinition + '\'' + ", " +
               "openApiDefinition='" + openApiDefinition + '\'' + '}';
    }
}


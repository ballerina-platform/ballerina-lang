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

package org.ballerinalang.swagger.code.generator;

import io.swagger.models.Swagger;
import org.ballerinalang.net.http.HttpConstants;
import org.ballerinalang.swagger.code.generator.exception.SwaggerGenException;
import org.ballerinalang.swagger.code.generator.util.SwaggerUtils;
import org.ballerinalang.util.codegen.ServiceInfo;

/**
 * Ballerina to swagger definition generation class.
 * See <a href="https://docs.google.com/document/d/1ZU7ogN-mq53Aiew1Wz5rSISTfIwIsQG-VczAJNimqNE/edit">specification</a>.
 *
 * @since 0.91.1
 */
public class BallerinaToSwaggerGenerator {
    
    /**
     * Generates the swagger definition for a given ServiceInfo.
     *
     * @param serviceInfo The service info object.
     * @return Swagger definition as a string.
     */
    public static String generateSwagger(ServiceInfo serviceInfo) throws SwaggerGenException {
        if (null != serviceInfo.getEndpointName() &&
                HttpConstants.HTTP_PACKAGE_PATH.equals(serviceInfo.getEndpointName())) {
            if (serviceInfo.getResourceInfoEntries().length > 0) {
                SwaggerServiceMapper swaggerServiceMapper = new SwaggerServiceMapper();
                Swagger swaggerDefinition = swaggerServiceMapper.convertServiceToSwagger(serviceInfo);
                return SwaggerUtils.generateSwaggerString(swaggerDefinition);
            } else {
                throw new SwaggerGenException("Unable to find a resource for the given service.");
            }
        } else {
            throw new SwaggerGenException("Unable to find a service with http protocol.");
        }
    }
}

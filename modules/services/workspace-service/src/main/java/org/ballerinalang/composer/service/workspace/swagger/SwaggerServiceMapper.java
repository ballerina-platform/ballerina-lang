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

package org.ballerinalang.composer.service.workspace.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.Info;
import io.swagger.models.Swagger;
import io.swagger.util.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ballerinalang.model.Annotation;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;

/**
 * SwaggerServiceMapper provides functionality for reading and writing Swagger, either to and from ballerina service, or
 * to, as well as related functionality for performing conversions between swagger and ballerina.
 */
public class SwaggerServiceMapper {
    private static final Logger logger = LoggerFactory.getLogger(SwaggerServiceMapper.class);
    ObjectMapper objectMapper;

    public SwaggerServiceMapper() {
        //Default object mapper is JSON mapper available in swagger utils.
        this.setObjectMapper(Json.mapper());
    }

    /**
     * @return ObjectMapper instance to be used to generate service definition.
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Object mapper can be used to generate swagger definition in JSON and YAML formats on demand. If user is willing
     * to generate output in specific format he can set mapper accordingly. Default mapper will be JSON mapper.
     *
     * @param objectMapper
     */
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * @param swagger Swagger definition
     * @return String representation of current service object.
     */
    public String generateSwaggerString(Swagger swagger) {
        try {
            String swaggerJson = objectMapper.writeValueAsString(swagger);
            return swaggerJson;
        } catch (JsonProcessingException e) {
            logger.error("Error while generating swagger string from definition" + e);
            return "Error";
        }
    }

    /**
     * This method will convert ballerina @Service to swaggers @Swagger object.
     *
     * @param service ballerina @Service object to be map to swagger definition
     * @return Swagger object which represent current service.
     */
    public Swagger convertServiceToSwagger(Service service) {
        Swagger swagger = new Swagger();
        //TODO replace this with ballerina annotation for information object.
        Info info = new Info()
                .version("1.0.0")
                .title("Swagger Resource");
        swagger.setInfo(info);
        Annotation[] annotations = service.getAnnotations();
        for (Annotation o : annotations) {
            if (o.getName().equals("http:BasePath")) {
                swagger.setBasePath(o.getValue());
            }
            if (o.getName().equals("http:Host")) {
                swagger.setHost(o.getValue());
            }
        }
        Resource[] resources = service.getResources();
        SwaggerResourceMapper resourceMapper = new SwaggerResourceMapper();
        //Mapping resources to swagger object.
        resourceMapper.convertResourcesToOperations(resources).forEach((s, path) -> swagger.path(s, path));
        return swagger;
    }

    /**
     * Assumption made here was ballerina service will be always super set of swagger. Swagger can have its annotations
     * and those will be part of ballerina service without any data loss.
     *
     * @param swagger Swagger to be convert to @Service.
     * @param service Service object that need to update with swagger changes.
     * @return Updated Service object with swagger changes.
     */
    public Service convertSwaggerToService(Swagger swagger, Service service) {
        //We need to pass both swagger definition and service definition for this class
        //as swagger do not have service implementation. When we return service we should be
        //careful to add only new skeleton methods to service. Service resource body should preserve
        //as it is while adding new resources.
        return service;

    }

}
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
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.util.Json;
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * @param objectMapper to set to this instance
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
        createInfoModel(service, swagger);
        createHostModel(service, swagger);
        createBasePathModel(service, swagger);
        createSchemesModel(service, swagger);
//        createConsumesModel(service, swagger);
//        createProducesModel(service, swagger);
        Resource[] resources = service.getResources();
        SwaggerResourceMapper resourceMapper = new SwaggerResourceMapper();
        //Mapping resources to swagger object.
        resourceMapper.convertResourcesToOperations(resources).forEach(swagger::path);
        return swagger;
    }
    
    private void createInfoModel(Service service, Swagger swagger) {
        Optional<AnnotationAttachment> swaggerInfoAnnotation = Arrays.stream(service.getAnnotations())
                .filter(x -> "swagger".equals(x.getPkgName()) && "SwaggerInfo".equals(x.getName()))
                .findFirst();
        
        Info info = new Info()
                .version("1.0.0")
                .title(service.getName());
        if (swaggerInfoAnnotation.isPresent()) {
            info.version(swaggerInfoAnnotation.get().getAttributeNameValuePairs().get("version")
                    .getLiteralValue().stringValue());
            info.title(swaggerInfoAnnotation.get().getAttributeNameValuePairs().get("title")
                    .getLiteralValue().stringValue());
            info.description(swaggerInfoAnnotation.get().getAttributeNameValuePairs().get("description")
                    .getLiteralValue().stringValue());
            info.termsOfService(swaggerInfoAnnotation.get().getAttributeNameValuePairs().get("termsOfService")
                    .getLiteralValue().stringValue());
            // TODO : Create contact and license models
        }
        swagger.setInfo(info);
    }
    
    private void createHostModel(Service service, Swagger swagger) {
        Optional<AnnotationAttachment> swaggerConfigAnnotation = Arrays.stream(service.getAnnotations())
                .filter(x -> "swagger".equals(x.getPkgName()) && "ServiceConfig".equals(x.getName()))
                .findFirst();
        if (swaggerConfigAnnotation.isPresent()) {
            swagger.setHost(swaggerConfigAnnotation.get().getAttribute("host").getLiteralValue().stringValue());
        }
    }
    
    private void createBasePathModel(Service service, Swagger swagger) {
        Optional<AnnotationAttachment> basePathAnnotation = Arrays.stream(service.getAnnotations())
                .filter(a -> "http".equals(a.getPkgName()) && "BasePath".equals(a.getName()))
                .findFirst();
        if (basePathAnnotation.isPresent()) {
            swagger.setBasePath(basePathAnnotation.get().getAttribute("value").getLiteralValue().stringValue());
        }
    }
    
    private void createSchemesModel(Service service, Swagger swagger) {
        Optional<AnnotationAttachment> swaggerConfigAnnotation = Arrays.stream(service.getAnnotations())
                .filter(a -> "swagger".equals(a.getPkgName()) && "ServiceConfig".equals(a.getName()))
                .findFirst();
        if (swaggerConfigAnnotation.isPresent()) {
            List<Scheme> schemesList = Arrays.stream(swaggerConfigAnnotation.get().getAttribute("schemes")
                        .getValueArray())
                    .map(a -> a.getLiteralValue().stringValue())
                    .map(Scheme::forValue)
                    .collect(Collectors.toList());
            swagger.setSchemes(schemesList);
        }
    }
    
//    private void createConsumesModel(Service service, Swagger swagger) {
//        Optional<AnnotationAttachment> consumesAnnotation = Arrays.stream(service.getAnnotations())
//                .filter(x -> "consumes".equals(x.getName()))
//                .findFirst();
//        if (consumesAnnotation.isPresent()) {
//            List<String> consumesList = Arrays.stream(consumesAnnotation.get().getAttribute("value").getValueArray())
//                    .map(a -> a.getLiteralValue().stringValue())
//                    .collect(Collectors.toList());
//            swagger.setConsumes(consumesList);
//        }
//    }
//
//    private void createProducesModel(Service service, Swagger swagger) {
//        Optional<AnnotationAttachment> producesAnnotation = Arrays.stream(service.getAnnotations())
//                .filter(x -> "produces".equals(x.getName()))
//                .findFirst();
//        if (producesAnnotation.isPresent()) {
//            List<String> consumesList = Arrays.stream(producesAnnotation.get().getAttribute("value").getValueArray())
//                    .map(a -> a.getLiteralValue().stringValue())
//                    .collect(Collectors.toList());
//            swagger.setProduces(consumesList);
//        }
//    }
    
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

package org.wso2.ballerina.tooling.service.workspace.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.models.Info;
import io.swagger.models.Swagger;
import io.swagger.util.Json;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;

/**
 * SwaggerServiceMapper provides functionality for reading and writing Swagger,
 * either to and from ballerina service, or to, as well as
 * related functionality for performing conversions between swagger and ballerina.
 */
public class SwaggerServiceMapper {
    ObjectMapper objectMapper;

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SwaggerServiceMapper() {
        //Default object mapper is JSON mapper available in swagger utils.
        this.setObjectMapper(Json.mapper());
    }


    /**
     * @return @String representation of current service object.
     */
    public String generateSwaggerString(Swagger swagger) {
        try {
            String swaggerJson = objectMapper.writeValueAsString(swagger);
            System.out.print(swaggerJson);
            return swaggerJson;
        } catch (JsonProcessingException e) {
        }
        return null;
    }

    /**
     * This method will convert ballerina @Service to swaggers @Swagger object.
     *
     * @param service ballerina @Service object to be map to swagger definition
     * @return @Swagger object which represent current service.
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
            if (o.getName().equals("BasePath")) {
                swagger.setBasePath(o.getValue());
            }
            if (o.getName().equals("Host")) {
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
     * Assumption made here was ballerina service will be always super set of swagger.
     * Swagger can have its annotations and those will be part of ballerina service without any data loss.
     *
     * @param swagger
     * @param service
     * @return
     */
    public Service convertSwaggerToService(Swagger swagger, Service service) {
        //We need to pass both swagger definition and service definition for this class
        //as swagger do not have service implementation. When we return service we should be
        //careful to add only new skeleton methods to service. Service resource body should preserve
        //as it is while adding new resources.
        return service;

    }
}
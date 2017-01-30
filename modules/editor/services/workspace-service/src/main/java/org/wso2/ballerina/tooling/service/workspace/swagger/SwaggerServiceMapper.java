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
     Swagger swagger;
     ObjectMapper objectMapper;

    /*public ObjectMapper getObjectMapper() {
        return objectMapper;
    }*/

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }


    public Swagger getSwagger() {
        return swagger;
    }

    public void setSwagger(Swagger swagger) {
        this.swagger = swagger;
    }


    public SwaggerServiceMapper(Swagger swagger) {
        this.setSwagger(swagger);
        //Default object mapper is JSON mapper available in swagger utils.
        this.setObjectMapper(Json.mapper());
    }

    public SwaggerServiceMapper(Service service) {
        this.setSwagger(convertServiceToSwagger(service));
        //Default object mapper is JSON mapper available in swagger utils.
        this.setObjectMapper(Json.mapper());
    }

    /**
     * Users can initiate swagger service mapper class by providing swagger definition
     * and object mapper. Object mapper can be JSON or YAML and if need can plug different
     * mappers.
     *
     * @param swagger @Swagger object to be used within mapper.
     * @param mapper  @ObjectMapper to be used to object conversion.
     */
    public SwaggerServiceMapper(Swagger swagger, ObjectMapper mapper) {
        this.setSwagger(swagger);
        this.setObjectMapper(mapper);

    }

    /**
     * @return @String representation of current service object.
     */
    public String generateSwaggerString() {
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
    private Swagger convertServiceToSwagger(Service service) {
        Swagger swagger = new Swagger();
        //Create default info object.
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

}

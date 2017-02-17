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
package org.ballerinalang.composer.service.workspace.swagger.impl;

import com.google.gson.JsonObject;
import io.swagger.annotations.ApiParam;
import org.ballerinalang.composer.service.workspace.rest.datamodel.BLangJSONModelBuilder;
import org.ballerinalang.composer.service.workspace.swagger.SwaggerConverterUtils;
import org.ballerinalang.composer.service.workspace.swagger.SwaggerServiceMapper;
import org.ballerinalang.composer.service.workspace.swagger.api.NotFoundException;
import org.ballerinalang.composer.service.workspace.swagger.model.Service;
import org.ballerinalang.model.BallerinaFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/service/swagger")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the services API")
/**
 * Service implementation class for convert both swagger to ballerina service definitions and
 * ballerina to swagger service definitions
 */
public class ServicesApiServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(ServicesApiServiceImpl.class);

    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_NAME = "Access-Control-Allow-Origin";
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_VALUE = "*";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS_NAME = "Access-Control-Allow-Headers";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS_VALUE = "content-type";
    private static final String ACCESS_CONTROL_ALLOW_METHODS_NAME = "Access-Control-Allow-Methods";
    private static final String ACCESS_CONTROL_ALLOW_METHODS_VALUE = "OPTIONS, POST";
    private static final String CONTENT_TYPE_NAME = "Content-Type";
    private static final Object CONTENT_TYPE_VALUE = "text/plain";

    @POST
    @Path("/convert-swagger")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value =
                                                 "Convert swagger to ballerina service definitions",
                                         notes = "This operation can be used to convert service definitions between " +
                                                 "ballerina and swagger ",
                                         response = void.class, tags = {"swagger",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200,
                                                message =
                                                        "Created.  Successful response with the newly created API as " +
                                                                "entity in the body. " +
                                                                "Location header contains URL of newly created API. ",
                                                response = void.class)})
    public Response servicesConvertSwaggerPost(@ApiParam(value = "Type to be convert", required = true)
                                               @QueryParam("expectedType") String expectedType
            , @ApiParam(value = "Service definition to be convert ", required = true) Service serviceDefinition)
            throws NotFoundException {
        try {
            //If ballerina definition is not null then only should process
            String ballerinaDefinition = serviceDefinition.getBallerinaDefinition();
            String swaggerDefinition = serviceDefinition.getSwaggerDefinition();

            if (expectedType.equalsIgnoreCase("ballerina")) {
                //In this case both swagger and ballerina should not be null.
                //If no ballerina present then generate complete new ballerina source from available swagger.
                serviceDefinition.
                        setBallerinaDefinition(generateBallerinaDataModel(swaggerDefinition, ballerinaDefinition));
            }
            return Response.ok().entity(serviceDefinition).header("Access-Control-Allow-Origin", '*').build();
        } catch (IOException ex) {
            logger.error("Error while processing service definition at converter service" + ex.getMessage());
            JsonObject entity = new JsonObject();
            entity.addProperty("Error", ex.toString());
            return Response.status(Response.Status.BAD_REQUEST).entity(entity)
                    .header("Access-Control-Allow-Origin", '*')
                    .type(MediaType.APPLICATION_JSON).build();
        }
    }


    @POST
    @Path("/convert-ballerina")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Convert swagger to ballerina service definitions",
                                         notes = "This operation can be used to convert service definitions between " +
                                                 "ballerina and swagger ",
                                         response = void.class, tags = {"swagger",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Created.  " +
                    "Successful response with ballerina JSON representation. " +
                    "This should directly use from composer to build models. ", response = void.class)})
    public Response servicesConvertBallerinaPost(@ApiParam(value = "Type to be convert", required = true)
                                                 @QueryParam("expectedType") String expectedType
            , @ApiParam(value = "Service definition to be convert ", required = true) Service serviceDefinition)
            throws NotFoundException {
        String ballerinaDefinition = serviceDefinition.getBallerinaDefinition();
        if (expectedType.equalsIgnoreCase("ballerina")) {
            try {
                //Take ballerina source and generate swagger and add it to service definition.
                if (ballerinaDefinition != null && !ballerinaDefinition.isEmpty()) {
                    String response = generateSwaggerDataModel(ballerinaDefinition);
                    serviceDefinition.setSwaggerDefinition(response);
                } else {
                    return Response.noContent().entity("Please provide valid ballerina source").build();
                    //ballerina source cannot be null or empty.
                }
            } catch (IOException e) {
                logger.error("Error while processing service definition at converter service" + e.getMessage());
                JsonObject entity = new JsonObject();
                entity.addProperty("Error", e.toString());
                return Response.status(Response.Status.BAD_REQUEST).entity(entity)
                        .header("Access-Control-Allow-Origin", '*')
                        .type(MediaType.APPLICATION_JSON).build();
            }
        }
        return Response.ok().entity(serviceDefinition).header("Access-Control-Allow-Origin", '*').build();
    }

    @OPTIONS
    @Path("/convert-swagger")
    public Response sendCORSHeadersForConvertSwaggerPost() {
        //TODO: this is temporary fix for handling CORS
        return sendCORSHeaders();
    }

    @OPTIONS
    @Path("/convert-ballerina")
    public Response sendCORSHeadersForConvertBallerinaPost() {
        //TODO: this is temporary fix for handling CORS
        return sendCORSHeaders();
    }

    private Response sendCORSHeaders() {
        return Response.ok()
                .header(ACCESS_CONTROL_ALLOW_ORIGIN_NAME, ACCESS_CONTROL_ALLOW_ORIGIN_VALUE)
                .header(ACCESS_CONTROL_ALLOW_HEADERS_NAME, ACCESS_CONTROL_ALLOW_HEADERS_VALUE)
                .header(ACCESS_CONTROL_ALLOW_METHODS_NAME, ACCESS_CONTROL_ALLOW_METHODS_VALUE)
                .header(CONTENT_TYPE_NAME, CONTENT_TYPE_VALUE)
                .build();
    }

    /**
     * This method will convert ballerina definition to swagger string. Since swagger is subset of ballerina definition
     * we can implement converter logic without data loss.
     *
     * @param ballerinaDefinition String ballerina config to be processed as ballerina service definition
     * @return swagger data model generated from ballerina definition
     * @throws IOException when input process error occur.
     */
    private String generateSwaggerDataModel(String ballerinaDefinition) throws IOException {
        //TODO improve code to avoid additional object creation.
        org.ballerinalang.model.Service[] services = SwaggerConverterUtils.
                getServicesFromBallerinaDefinition(ballerinaDefinition);
        String swaggerDefinition = "";
        if (services.length > 0) {
            //TODO this need to improve iterate through multiple services and generate single swagger file.
            SwaggerServiceMapper swaggerServiceMapper = new SwaggerServiceMapper();
            //TODO mapper type need to set according to expected type.
            //swaggerServiceMapper.setObjectMapper(io.swagger.util.Yaml.mapper());
            swaggerDefinition = swaggerServiceMapper.
                    generateSwaggerString(swaggerServiceMapper.convertServiceToSwagger(services[0]));
        }
        return swaggerDefinition;
    }

    /**
     * This method will generate ballerina string from swagger definition. Since ballerina service definition is super
     * set of swagger definition we will take both swagger and ballerina definition and merge swagger changes to
     * ballerina definition selectively to prevent data loss
     *
     * @param swaggerDefinition   @String swagger definition to be processed as swagger
     * @param ballerinaDefinition @String ballerina definition to be process as ballerina definition
     * @return @String representation of converted ballerina source
     * @throws IOException when error occur while processing input swagger and ballerina definitions.
     */
    private String generateBallerinaDataModel(String swaggerDefinition, String ballerinaDefinition) throws IOException {
        BallerinaFile ballerinaFile = SwaggerConverterUtils.getBFileFromBallerinaDefinition(ballerinaDefinition);
        //Always assume we have only one resource in bfile.
        //TODO this logic need to be reviewed and fix issues. This is temporary commit to test swagger UI flow
        org.ballerinalang.model.Service swaggerService = SwaggerConverterUtils.
                getServiceFromSwaggerDefinition(swaggerDefinition);
        org.ballerinalang.model.Service ballerinaService = ballerinaFile.getServices()[0];
        String serviceName = swaggerService.getSymbolName().getName();
        for (org.ballerinalang.model.Service currentService : ballerinaFile.getServices()) {
            if (currentService.getSymbolName().getName().equalsIgnoreCase(serviceName)) {
                ballerinaService = currentService;
            }
        }
        //Compare ballerina service and swagger service and then substitute values. Then we should get ballerina
        //JSON representation and send back to client.
        //for the moment we directly add swagger service to ballerina service.

        ballerinaFile.getServices()[0] = SwaggerConverterUtils.
                mergeBallerinaService(ballerinaService, swaggerService);
        //Now we have to convert ballerina file to JSON object model composer require.
        JsonObject response = new JsonObject();
        BLangJSONModelBuilder jsonModelBuilder = new BLangJSONModelBuilder(response);
        ballerinaFile.accept(jsonModelBuilder);
        return response.toString();
    }

}


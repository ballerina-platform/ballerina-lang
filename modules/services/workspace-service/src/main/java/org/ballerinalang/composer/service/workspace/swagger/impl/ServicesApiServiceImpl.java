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
import org.ballerinalang.composer.service.workspace.swagger.SwaggerConverterUtils;
import org.ballerinalang.composer.service.workspace.swagger.model.Service;
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

/**
 * Service implementation class for convert both swagger to ballerina service definitions and
 * ballerina to swagger service definitions
 */
@Path("/service/swagger")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the services API")
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
            response = void.class, tags = {"swagger"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200,
                    message =
                            "Created.  Successful response with the newly created API as " +
                                    "entity in the body. " +
                                    "Location header contains URL of newly created API. ",
                    response = void.class)})
    public Response servicesConvertSwaggerPost(@ApiParam(value = "Type to be convert", required = true)
                                               @QueryParam("expectedType") String expectedType,
                                               @ApiParam(value = "Service definition to be convert ", required = true)
                                                       Service serviceDefinition) {
        try {
            //If ballerina definition is not null then only should process
            String ballerinaDefinition = serviceDefinition.getBallerinaDefinition();
            String swaggerDefinition = serviceDefinition.getSwaggerDefinition();

            if (expectedType.equalsIgnoreCase("ballerina")) {
                //In this case both swagger and ballerina should not be null.
                //If no ballerina present then generate complete new ballerina source from available swagger.
                serviceDefinition.
                        setBallerinaDefinition(SwaggerConverterUtils.
                                generateBallerinaDataModel(swaggerDefinition, ballerinaDefinition));
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
            response = void.class, tags = {"swagger"})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "Created.  " +
                    "Successful response with ballerina JSON representation. " +
                    "This should directly use from composer to build models. ", response = void.class)})
    public Response servicesConvertBallerinaPost(@ApiParam(value = "Type to be convert", required = true)
                                                 @QueryParam("expectedType") String expectedType,
                                                 @ApiParam(value = "Service definition to be convert ", required = true)
                                                         Service serviceDefinition) {
        String ballerinaDefinition = serviceDefinition.getBallerinaDefinition();
        if (expectedType.equalsIgnoreCase("ballerina")) {
            try {
                //Take ballerina source and generate swagger and add it to service definition.
                if (ballerinaDefinition != null && !ballerinaDefinition.isEmpty()) {
                    String response = SwaggerConverterUtils.generateSwaggerDataModel(ballerinaDefinition);
                    serviceDefinition.setSwaggerDefinition(response);
                } else {
                    return Response.noContent().entity("Please provide valid ballerina source").build();
                    //ballerina source cannot be null or empty.
                }
            } catch (Throwable throwable) {
                logger.error("Error while processing service definition at converter service" +
                        throwable.getMessage());
                JsonObject entity = new JsonObject();
                entity.addProperty("Error", throwable.toString());
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
}


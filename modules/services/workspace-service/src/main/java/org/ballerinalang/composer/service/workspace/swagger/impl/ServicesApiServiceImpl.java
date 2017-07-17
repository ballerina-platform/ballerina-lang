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
import org.ballerinalang.composer.service.workspace.swagger.SwaggerConverterUtils;
import org.ballerinalang.composer.service.workspace.swagger.model.SwaggerServiceContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Service implementation class for convert both swagger to ballerina service definitions and
 * ballerina to swagger service definitions
 */
@Path("/service/swagger")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ServicesApiServiceImpl {
    private static final Logger logger = LoggerFactory.getLogger(ServicesApiServiceImpl.class);

    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_NAME = "Access-Control-Allow-Origin";
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_VALUE = "*";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS_NAME = "Access-Control-Allow-Headers";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS_VALUE = "content-type";
    private static final String ACCESS_CONTROL_ALLOW_METHODS_NAME = "Access-Control-Allow-Methods";
    private static final String ACCESS_CONTROL_ALLOW_METHODS_VALUE = "OPTIONS, POST";

    @POST
    @Path("/convert-to-swagger")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response convertToSwagger(SwaggerServiceContainer swaggerServiceContainer) {
        try {
            // Get the ballerina source.
            String ballerinaSource = swaggerServiceContainer.getBallerinaDefinition();
            
            // Generate the swagger definitions using ballerina source.
            List<String> swaggerDefinitions = SwaggerConverterUtils.generateSwaggerDefinitions(ballerinaSource);
            swaggerServiceContainer.setSwaggerDefinitions(
                    swaggerDefinitions.toArray(new String[swaggerDefinitions.size()]));
            
            return Response.ok().entity(swaggerServiceContainer).header("Access-Control-Allow-Origin", '*').build();
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
    @Path("/convert-to-ballerina")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response convertToBallerina(SwaggerServiceContainer swaggerServiceContainer) {
        String ballerinaDefinition = swaggerServiceContainer.getBallerinaDefinition();
        try {
//            //Take ballerina source and generate swagger and add it to service definition.
//            if (ballerinaDefinition != null && !ballerinaDefinition.isEmpty()) {
//                String response = SwaggerConverterUtils.generateSwaggerDataModel(ballerinaDefinition);
//                swaggerServiceContainer.setSwaggerDefinitions(response);
//            } else {
//                return Response.noContent().entity("Please provide valid ballerina source").build();
//                //ballerina source cannot be null or empty.
//            }
        } catch (Throwable throwable) {
            logger.error("Error while processing service definition at converter service" +
                    throwable.getMessage());
            JsonObject entity = new JsonObject();
            entity.addProperty("Error", throwable.toString());
            return Response.status(Response.Status.BAD_REQUEST).entity(entity)
                    .header("Access-Control-Allow-Origin", '*')
                    .type(MediaType.APPLICATION_JSON).build();
        }
        return Response.ok().entity(swaggerServiceContainer).header("Access-Control-Allow-Origin", '*')
                .build();
    }

    @OPTIONS
    @Path("/convert-to-ballerina")
    public Response sendCORSHeadersForConvertToBallerinaPost() {
        return sendCORSHeaders();
    }

    @OPTIONS
    @Path("/convert-to-swagger")
    public Response sendCORSHeadersForConvertToSwaggerPost() {
        return sendCORSHeaders();
    }
    
    /**
     * Responses with CORS headers.
     * @return CORS headers.
     */
    private Response sendCORSHeaders() {
        return Response.ok()
                .header(ACCESS_CONTROL_ALLOW_ORIGIN_NAME, ACCESS_CONTROL_ALLOW_ORIGIN_VALUE)
                .header(ACCESS_CONTROL_ALLOW_HEADERS_NAME, ACCESS_CONTROL_ALLOW_HEADERS_VALUE)
                .header(ACCESS_CONTROL_ALLOW_METHODS_NAME, ACCESS_CONTROL_ALLOW_METHODS_VALUE)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN)
                .build();
    }
}


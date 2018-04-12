/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.composer.service.ballerina.swagger.service;

import com.google.gson.JsonObject;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.ballerina.swagger.convertor.service.SwaggerConverterUtils;
import org.ballerinalang.composer.server.core.ServerConstants;
import org.ballerinalang.composer.server.spi.ComposerService;
import org.ballerinalang.composer.server.spi.ServiceInfo;
import org.ballerinalang.composer.server.spi.ServiceType;
import org.ballerinalang.composer.service.ballerina.swagger.Constants;
import org.ballerinalang.composer.service.ballerina.swagger.service.model.SwaggerServiceContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Service implementation class for convert both swagger to ballerina service definitions.
 */
@Path(ServerConstants.CONTEXT_ROOT + "/" + Constants.SERVICE_PATH)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BallerinaToSwaggerService implements ComposerService {
    private static final Logger logger = LoggerFactory.getLogger(BallerinaToSwaggerService.class);

    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_NAME =
            HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString();
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_VALUE = "*";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS_NAME =
            HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS.toString();
    private static final String ACCESS_CONTROL_ALLOW_HEADERS_VALUE = HttpHeaderNames.CONTENT_TYPE.toString();
    private static final String ACCESS_CONTROL_ALLOW_METHODS_NAME =
            HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS.toString();
    private static final String ACCESS_CONTROL_ALLOW_METHODS_VALUE = "OPTIONS, POST";

    @POST
    @Path("/ballerina-to-swagger")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response convertToSwagger(SwaggerServiceContainer swaggerServiceContainer,
                                     @QueryParam("serviceName") String serviceName) {
        try {
            // Get the ballerina source.
            String ballerinaSource = swaggerServiceContainer.getBallerinaDefinition();

            // Generate the swagger definitions using ballerina source.
            String swaggerDefinition = SwaggerConverterUtils.generateOAS3Definitions(ballerinaSource, serviceName);
            swaggerServiceContainer.setSwaggerDefinition(swaggerDefinition);
            return Response.ok().entity(swaggerServiceContainer).header(ACCESS_CONTROL_ALLOW_ORIGIN_NAME, '*').build();
        } catch (Exception ex) {
            logger.error("error: while processing service definition at converter service: " + ex.getMessage(), ex);
            JsonObject entity = new JsonObject();
            entity.addProperty("Error", ex.toString());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(entity)
                    .header(ACCESS_CONTROL_ALLOW_ORIGIN_NAME, '*')
                    .type(MediaType.APPLICATION_JSON).build();
        }
    }

    @OPTIONS
    @Path("/ballerina-to-swagger")
    public Response sendCORSHeadersForConvertToSwaggerPost() {
        return this.sendCORSHeaders();
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

    @Override
    public ServiceInfo getServiceInfo() {
        return new ServiceInfo(Constants.SERVICE_NAME, Constants.SERVICE_PATH, ServiceType.HTTP);
    }
}

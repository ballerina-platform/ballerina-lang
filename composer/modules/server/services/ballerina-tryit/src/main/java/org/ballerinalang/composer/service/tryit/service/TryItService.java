/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.composer.service.tryit.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.composer.server.core.ServerConfig;
import org.ballerinalang.composer.server.core.ServerConstants;
import org.ballerinalang.composer.server.spi.ComposerService;
import org.ballerinalang.composer.server.spi.ServiceInfo;
import org.ballerinalang.composer.server.spi.ServiceType;
import org.ballerinalang.composer.service.ballerina.launcher.service.LaunchManager;
import org.ballerinalang.composer.service.tryit.Constants;
import org.wso2.msf4j.Request;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Set;

/**
 * Microservice for try-it service. This acts as a proxy for different protocols.
 */
@Path(ServerConstants.CONTEXT_ROOT + "/" + Constants.SERVICE_PATH)
public class TryItService implements ComposerService {
    
    private TryItClientFactory tryItClientFactory;

    private ServerConfig serverConfig;
    
    /**
     * Initializing service.
     */
    public TryItService(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        tryItClientFactory = new TryItClientFactory();
    }
    
    /**
     * Invokes a service.
     * @param request The http request.
     * @param protocol The protocol of the service.
     * @param clientArgs The arguments for the proxy client.
     * @return An http response.
     */
    @POST
    @Path("/{protocol}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response tryIt(@Context Request request, @PathParam("protocol") String protocol, String clientArgs) {
        try {
            String hostName = getHostName(request);
            String hostAndPort = new JsonParser().parse(clientArgs)
                    .getAsJsonObject().get(TryItConstants.BASE_URL).getAsString();
            TryItClient tryItClient = tryItClientFactory.getClient(protocol, hostAndPort, clientArgs);
            String responseContent = tryItClient.execute();
            
            return Response
                    .status(Response.Status.OK)
                    .entity(responseContent)
                    .header("Access-Control-Allow-Origin", "*")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            JsonObject entity = new JsonObject();
            entity.addProperty("error", e.getMessage());
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(entity)
                    .header("Access-Control-Allow-Origin", "*")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }
    
    /**
     * Gets the backend url used in try-it executions.
     * @param request The http request.
     * @return The url in a json object.
     */
    @GET
    @Path("/url")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUrl(@Context Request request) {
        try {
            String hostName = getHostName(request);
            Set<String> ports = LaunchManager.getInstance(serverConfig).getPorts();
            JsonObject response = new JsonObject();
            JsonArray urls = new JsonArray();

            ports.forEach(port -> {
                urls.add(hostName + ':' + port);
            });

            response.add("urls", urls);
        
            return Response
                    .status(Response.Status.OK)
                    .entity(response)
                    .header("Access-Control-Allow-Origin", "*")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            JsonObject entity = new JsonObject();
            entity.addProperty("error", e.getMessage());
            return Response
                    .status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(entity)
                    .header("Access-Control-Allow-Origin", "*")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }
    }
    
    /**
     * The options in requests.
     * @return The options in responses.
     */
    @OPTIONS
    @Path("/{protocol}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response options() {
        return Response.ok().header("Access-Control-Max-Age", "600 ")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(), "*")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString(), "true")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS.toString(),
                        "POST, GET, PUT, UPDATE, DELETE, OPTIONS, HEAD")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS.toString(),
                        HttpHeaderNames.CONTENT_TYPE.toString() + ", " + HttpHeaderNames.ACCEPT.toString() +
                                ", X-Requested-With").build();
        
    }
    
    /**
     * Gets the host name from the http request.
     * @param request The http request.
     * @return The host name.
     */
    public String getHostName(Request request) {
        String hostHeader = request.getHeader("Host");
        String[] split = hostHeader.split(":");
        return split[0];
    }

    @Override
    public ServiceInfo getServiceInfo() {
        return new ServiceInfo(Constants.SERVICE_NAME, Constants.SERVICE_PATH, ServiceType.HTTP);
    }
}

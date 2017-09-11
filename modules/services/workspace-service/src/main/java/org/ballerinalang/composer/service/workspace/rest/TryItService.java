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

package org.ballerinalang.composer.service.workspace.rest;

import com.google.gson.JsonObject;
import org.ballerinalang.composer.service.workspace.launcher.LaunchManager;
import org.ballerinalang.composer.service.workspace.tryit.TryItClient;
import org.ballerinalang.composer.service.workspace.tryit.TryItClientFactory;
import org.wso2.msf4j.Request;

import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Microservice for try-it service. This acts as a proxy for different protocols.
 */
@Path("/try-it")
public class TryItService {
    
    private TryItClientFactory tryItClientFactory;
    
    /**
     * Initializing service.
     */
    public TryItService() {
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
            String port = LaunchManager.getInstance().getPort();
            TryItClient tryItClient = tryItClientFactory.getClient(protocol, hostName + ":" + port, clientArgs);
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
    
    @OPTIONS
    @Path("/{protocol}")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response options() {
        return Response.ok().header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, DELETE, OPTIONS, HEAD")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With")
                .build();
        
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
}

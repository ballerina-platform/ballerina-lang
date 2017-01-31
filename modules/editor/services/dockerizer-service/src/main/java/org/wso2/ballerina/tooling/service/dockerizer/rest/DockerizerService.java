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
 *
 */

package org.wso2.ballerina.tooling.service.dockerizer.rest;

import org.wso2.ballerina.tooling.service.dockerizer.bean.DockerizeRequest;
import org.wso2.ballerina.tooling.service.dockerizer.handler.DockerHandler;
import org.wso2.ballerina.tooling.service.dockerizer.utils.Utils;

import java.io.IOException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * A service that will create Docker images for a given set of Ballerina services.
 */
@Path("/service/docker")
public class DockerizerService {

    // TODO: better URIs
    @POST
    @Path("/image/service")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createServiceImage(DockerizeRequest request) {
        try {
            // 0. Validate payload TODO
            String imageName = Utils.getBase64DecodedString(request.getImageName());
            String serviceName = Utils.getBase64DecodedString(request.getServiceName());
            String ballerinaConfig = Utils.getBase64DecodedString(request.getBallerinaConfig());
            String dockerEnv = Utils.getBase64DecodedString(request.getDockerEnv());

            boolean buildSuccessful = DockerHandler.createServiceImage(dockerEnv, imageName, serviceName, ballerinaConfig);
            if (buildSuccessful) {
                // TODO: figure out return status later
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @POST
    @Path("/image/function")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createFunctionImage(DockerizeRequest request) {
        try {
            // 0. Validate payload TODO
            String imageName = Utils.getBase64DecodedString(request.getImageName());
            String serviceName = Utils.getBase64DecodedString(request.getServiceName());
            String ballerinaConfig = Utils.getBase64DecodedString(request.getBallerinaConfig());
            String dockerEnv = Utils.getBase64DecodedString(request.getDockerEnv());

            boolean buildSuccessful = DockerHandler.createFunctionImage(dockerEnv, imageName, serviceName, ballerinaConfig);
            if (buildSuccessful) {
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DELETE
    @Path("/image/env/image-name")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteImage(DockerizeRequest request) {
        boolean deleteSuccessful = DockerHandler.deleteImage(request.getDockerEnv(), request.getImageName());
        return deleteSuccessful ?
                Response.status(Response.Status.OK).build() :
                Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}

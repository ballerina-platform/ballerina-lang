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

import org.wso2.ballerina.containers.docker.BallerinaDockerClient;
import org.wso2.ballerina.containers.docker.exception.DockerHandlerException;
import org.wso2.ballerina.tooling.service.dockerizer.Constants;
import org.wso2.ballerina.tooling.service.dockerizer.bean.DockerRequest;
import org.wso2.ballerina.tooling.service.dockerizer.utils.Utils;

import java.io.IOException;
import java.nio.file.Paths;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * A service that will create Docker images for a given set of Ballerina services.
 */
@Path("/docker")
public class DockerizerService {
    private BallerinaDockerClient dockerClient;

    public DockerizerService(BallerinaDockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    @POST
    @Path("/{" + Constants.REST.SERVICE_NAME + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createImage(@PathParam(Constants.REST.SERVICE_NAME) String serviceName, DockerRequest request) {
        try {
            String bPackagePath = Utils.getBase64DecodedString(request.getPackagePath());
            java.nio.file.Path packagePath = Paths.get(bPackagePath);
            String dockerEnv = Utils.getBase64DecodedString(request.getDockerEnv());
            String type = Utils.getBase64DecodedString(request.getType());
            String imageName;

            switch (type) {
                case org.wso2.ballerina.containers.Constants.TYPE_BALLERINA_SERVICE:
                    imageName = dockerClient.createServiceImage(
                            serviceName, dockerEnv, packagePath);
                    break;
                case org.wso2.ballerina.containers.Constants.TYPE_BALLERINA_FUNCTION:
                    imageName = dockerClient.createFunctionImage(
                            serviceName, dockerEnv, packagePath);
                    break;
                default:
                    return Response.status(Response.Status.BAD_REQUEST).build();
            }

            if (imageName != null) {
                // TODO: figure out return status later
                return Response.status(Response.Status.OK).build();
            } else {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        } catch (DockerHandlerException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }

    @DELETE
    @Path("/{" + Constants.REST.SERVICE_NAME + "}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteImage(@PathParam(Constants.REST.SERVICE_NAME) String imageName, DockerRequest request) {
        String dockerEnv = Utils.getBase64DecodedString(request.getDockerEnv());
        boolean deleteSuccessful = dockerClient.deleteImage(imageName, dockerEnv);
        return deleteSuccessful ?
                Response.status(Response.Status.OK).build() :
                Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @POST
    @Path("/{" + Constants.REST.SERVICE_NAME + "}/run")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response runServiceWithDocker(@PathParam(Constants.REST.SERVICE_NAME) String serviceName, DockerRequest request) throws InterruptedException, IOException {
        String dockerEnv = Utils.getBase64DecodedString(request.getDockerEnv());
        String bPackagePath = Utils.getBase64DecodedString(request.getPackagePath());
        java.nio.file.Path packagePath = Paths.get(bPackagePath);
        String type = Utils.getBase64DecodedString(request.getType());

        // Build image, if not already built
        if (dockerClient.getImage(serviceName.toLowerCase(), dockerEnv) == null) {
            try {
                switch (type) {
                    case org.wso2.ballerina.containers.Constants.TYPE_BALLERINA_SERVICE:
                        dockerClient.createServiceImage(
                                serviceName, dockerEnv, packagePath);
                        dockerClient.runServiceContainer(dockerEnv, serviceName);
                        break;
                    case org.wso2.ballerina.containers.Constants.TYPE_BALLERINA_FUNCTION:
                        dockerClient.createFunctionImage(
                                serviceName, dockerEnv, packagePath);
                        dockerClient.runFunctionContainer(dockerEnv, serviceName);
                        break;
                    default:
                        return Response.status(Response.Status.BAD_REQUEST).build();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            } catch (DockerHandlerException e) {
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        }

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }

    @POST
    @Path("/{" + Constants.REST.SERVICE_NAME + "}/stop")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response stopDockerContainer(@PathParam(Constants.REST.SERVICE_NAME) String serviceName, DockerRequest request) {
        String dockerEnv = Utils.getBase64DecodedString(request.getDockerEnv());
        dockerClient.stopContainer(serviceName, dockerEnv);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
    }
}

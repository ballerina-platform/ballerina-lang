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

package org.wso2.integration.tooling.service.workspace.rest;

import com.google.inject.Inject;
import org.wso2.integration.tooling.service.workspace.Workspace;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Micro-service for exposing the workspace.
 *
 * @since 0.1-SNAPSHOT
 */
@Path("/service/workspace")
public class WorkspaceService {

    @Inject
    private Workspace workspace;

    @GET
    @Path("/root")
    @Produces("application/json")
    public Response root() {
        try {
            return Response.status(Response.Status.OK)
                    .entity(workspace.listRoots())
                    .header("Access-Control-Allow-Origin", '*')
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    @GET
    @Path("/list")
    @Produces("application/json")
    public Response directoriesInPath(@QueryParam("path") String path) {
        try {
            return Response.status(Response.Status.OK)
                    .entity(workspace.listDirectoriesInPath(path))
                    .header("Access-Control-Allow-Origin", '*')
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Exception e) {
            return getErrorResponse(e);
        }
    }

    public Workspace getWorkspace(){
        return this.workspace;
    }

    private Response getErrorResponse(Exception ex){
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("{Error: '" + ex.toString() + "'}")
                .header("Access-Control-Allow-Origin", '*')
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

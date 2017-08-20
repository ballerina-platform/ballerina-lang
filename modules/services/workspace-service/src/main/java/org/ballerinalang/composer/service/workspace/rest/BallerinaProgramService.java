/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 **/

package org.ballerinalang.composer.service.workspace.rest;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.util.program.BLangPrograms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Service for resolving ballerina program
 *
 * @since 0.86
 */
@Path("/service/program")
public class BallerinaProgramService {

    private static final Logger logger = LoggerFactory.getLogger(BallerinaProgramService.class);
    private SymbolScope globalScope;

    public BallerinaProgramService() {

        this.globalScope = BLangPrograms.populateGlobalScope();
    }

    @GET
    @Path("/native/types")
    @Produces("application/json")
    public Response nativeTypes() {
        try {
            return Response.status(Response.Status.OK)
                    .entity(getNativeTypes())
                    .header("Access-Control-Allow-Origin", '*')
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        } catch (Throwable throwable) {
            logger.error("/Ballerina program resolver service error", throwable.getMessage());
            return getErrorResponse(throwable);
        }
    }

    @OPTIONS
    @Path("/native/types")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response options() {
        return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Credentials",
                "true").header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, DELETE, OPTIONS, HEAD")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
    }

    @OPTIONS
    @Path("/packages")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response optionsPackages() {
        return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Credentials",
                "true").header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, DELETE, OPTIONS, HEAD")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
    }

    /**
     * Returns native types
     * @return JsonArray
     */
    private JsonArray getNativeTypes() {
        JsonArray nativeTypes = new JsonArray();
        globalScope.getSymbolMap().values().stream().forEach(symbol -> {
            if (symbol instanceof BType) {
                nativeTypes.add(symbol.getName());
            }
        });
        return nativeTypes;
    }

    /**
     * Generates an error message for the given exception
     * @param ex
     * @return Response
     */
    private Response getErrorResponse(Throwable ex) {
        JsonObject entity = new JsonObject();
        String errMsg = ex.getMessage();
        entity.addProperty("Error", errMsg);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(entity)
                .header("Access-Control-Allow-Origin", '*')
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

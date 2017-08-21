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

package org.ballerinalang.composer.service.workspace.rest.typelattice;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.ballerinalang.model.types.TypeLattice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Service for getting type cast and conversion lattice
 *
 * @since 0.92
 */
@Path("/typelattice")
public class TypeLatticeService {

    private static final Logger logger = LoggerFactory.getLogger(TypeLatticeService.class);

    @OPTIONS
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response options() {
        return Response.ok().header("Access-Control-Allow-Origin", "*")
                       .header("Access-Control-Allow-Credentials", "true")
                       .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, DELETE, OPTIONS, HEAD")
                       .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
    }

    @GET
    @Path("/")
    @Produces("application/json")
    public Response getTypeLattice() {
        String typeLatticeJson = getTypeLatticeResponse();
        try {
            return Response.status(Response.Status.OK)
                           .entity(typeLatticeJson)
                           .header("Access-Control-Allow-Origin", '*')
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        } catch (Throwable throwable) {
            logger.error("/Type lattice service error", throwable.getMessage());
            return getErrorResponse(throwable);
        }
    }

    private String getTypeLatticeResponse() {
        List<SimpleTypeEdge> explicitCastEdges = getSimpleTypeEdges(TypeLattice.getExplicitCastLattice());
        List<SimpleTypeEdge> implicitCastEdges = getSimpleTypeEdges(TypeLattice.getImplicitCastLattice());
        List<SimpleTypeEdge> transformEdges = getSimpleTypeEdges(TypeLattice.getTransformLattice());

        Map<String, List<SimpleTypeEdge>> typeLattices = new HashMap<>();
        typeLattices.put("explicit_cast", explicitCastEdges);
        typeLattices.put("implicit_cast", implicitCastEdges);
        typeLattices.put("conversion", transformEdges);

        Gson gson = new Gson();
        return gson.toJson(typeLattices);
    }

    private List<SimpleTypeEdge> getSimpleTypeEdges(TypeLattice typeLattice) {
        return typeLattice.getEdges()
                           .stream()
                           .map(edge -> {
                               SimpleTypeEdge simpleTypeEdge = new SimpleTypeEdge();
                               simpleTypeEdge.setSafe(edge.isSafe());
                               simpleTypeEdge.setSource(edge.getSource().getType().getName());
                               simpleTypeEdge.setTarget(edge.getTarget().getType().getName());
                               return simpleTypeEdge;
                           }).collect(Collectors.toList());

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

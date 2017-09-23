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

package org.ballerinalang.composer.service.workspace.fileserver;

import com.google.gson.JsonObject;
import org.ballerinalang.composer.service.workspace.fileserver.dto.ConnectorIconRequest;
import org.ballerinalang.composer.service.workspace.rest.datamodel.BLangFileRestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.Request;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;

import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * File server Entry point
 *
 * @since 0.94
 */

@Path("/file")
public class FileContentProvider {
    private static final Logger logger = LoggerFactory.getLogger(BLangFileRestService.class);
    private String contextRoot;

    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }

    @OPTIONS
    @Path("/connector/icon")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response options() {
        return Response.ok().header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, DELETE, OPTIONS, HEAD")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
    }

    @POST
    @Path("/connector/icon")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConnectorIcon(ConnectorIconRequest connectorIconRequest, @Context Request request) {
        JsonObject responseJson;

        if (this.contextRoot != null && this.contextRoot.endsWith("/resources/composer/web")) {
            logger.debug(connectorIconRequest.getIconPath());
            logger.debug(connectorIconRequest.getConnectorName());

            try {
                String filePath = this.contextRoot + File.separator + "images" + File.separator + "connectors" +
                        File.separator + connectorIconRequest.getConnectorName() + ".svg";
                InputStream inputStream = new FileInputStream(new File(filePath));
                responseJson = getImageResponse(inputStream, "svg");
            } catch (IOException e) {
                logger.debug("Error Reading File Content");
                responseJson = new JsonObject();
                responseJson.addProperty("status", "failed");
                responseJson.addProperty("reason", "Cannot read File Content");
            }
        } else {
            logger.debug("Invalid Context Root");
            responseJson = new JsonObject();
            responseJson.addProperty("status", "failed");
            responseJson.addProperty("reason", "Cannot read File Content");
        }

        return Response.status(Response.Status.OK)
                .entity(responseJson)
                .header("Access-Control-Allow-Origin", '*')
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private static JsonObject getImageResponse(InputStream inputStream, String extension) throws IOException {
        JsonObject response = new JsonObject();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int reads = inputStream.read();
        while (reads != -1) {
            baos.write(reads);
            reads = inputStream.read();
        }

        String encodedContent = "data:image/svg+xml;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        response.addProperty("status", "success");
        response.addProperty("extension", extension);
        response.addProperty("content", encodedContent);
        return response;
    }
}

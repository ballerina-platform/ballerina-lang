/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.integration.tooling.service.workspace.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.Request;

import java.io.File;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * Micro-service for exposing Ballerina tooling.
 *
 * @since 0.1-SNAPSHOT
 */
@Path(".*")
public class FileServer {

    private static final Logger log = LoggerFactory.getLogger(FileServer.class);

    private String contextRoot;

    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }

    @GET
    public Response handleGet(@Context Request request) {

        String rawUri = request.getUri();
        String rawUriPath;

        if (rawUri == null || rawUri.trim().length() == 0 || "/".equals(rawUri)) {
            rawUriPath = "/index.html";
        } else {
            int uriPathEndIndex = rawUri.indexOf('?');
            if (uriPathEndIndex != -1) {
                // handling query Params.
                rawUriPath = rawUri.substring(0, uriPathEndIndex);
            } else {
                rawUriPath = rawUri;
            }
        }

        if (log.isDebugEnabled()) {
            log.debug(" Requesting path [" + rawUriPath + "] mapped to file path [" + contextRoot + rawUriPath + "]");
        }
        File file = new File(contextRoot + rawUriPath);
        if (file.exists()) {
            return Response.ok(file).build();
        }
        log.error(" File not found [" + contextRoot + rawUriPath + "], Requesting path [" + rawUriPath + "] ");
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
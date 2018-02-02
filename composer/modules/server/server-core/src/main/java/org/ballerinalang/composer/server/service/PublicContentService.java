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
package org.ballerinalang.composer.server.service;

import org.ballerinalang.composer.server.core.ServerConfig;
import org.ballerinalang.composer.server.spi.ComposerService;
import org.ballerinalang.composer.server.spi.ServiceInfo;
import org.ballerinalang.composer.server.spi.ServiceType;
import org.wso2.msf4j.Request;

import java.io.File;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;


/**
 * Micro service which exposes public content.
 */
@Path(PublicContentService.PATH)
public class PublicContentService implements ComposerService {

    static final String NAME = "public-content";
    static final String PATH = ".*";
    private final ServerConfig serverConfig;

    public PublicContentService(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @GET
    public Response handleGet(@Context Request request) {
        String requestedPath = request.getUri();
        String publicFolder = serverConfig.getPublicPath();
        String targetFilePath = publicFolder + File.separator;
        if (requestedPath.trim().length() == 0 || requestedPath.endsWith("/")) {
            targetFilePath += "index.html";
        } else if (requestedPath.indexOf('?') != -1) {
            targetFilePath += requestedPath.substring(0, requestedPath.indexOf('?'));
        } else {
            targetFilePath += requestedPath;
        }
        File file = new File(targetFilePath);
        if (file.exists()) {
            return Response.ok(file).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public ServiceInfo getServiceInfo() {
        return new ServiceInfo(NAME, PATH, ServiceType.HTTP);
    }
}

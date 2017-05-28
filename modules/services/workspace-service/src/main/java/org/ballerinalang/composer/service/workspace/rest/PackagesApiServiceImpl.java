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

package org.ballerinalang.composer.service.workspace.rest;

import org.ballerinalang.composer.service.workspace.api.NotFoundException;
import org.ballerinalang.composer.service.workspace.api.PackagesApiService;
import org.ballerinalang.composer.service.workspace.model.ModelPackage;
import org.ballerinalang.composer.service.workspace.utils.BallerinaProgramContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * This is the service implementation class for Packages list related operations
 *
 * @since 0.8.0
 */
public class PackagesApiServiceImpl extends PackagesApiService {

    private static final Logger logger = LoggerFactory.getLogger(WorkspaceService.class);

    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_NAME = "Access-Control-Allow-Origin";
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_VALUE = "*";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS_NAME = "Access-Control-Allow-Headers";
    private static final String ACCESS_CONTROL_ALLOW_HEADERS_VALUE = "content-type";
    private static final String ACCESS_CONTROL_ALLOW_METHODS_NAME = "Access-Control-Allow-Methods";
    private static final String ACCESS_CONTROL_ALLOW_METHODS_VALUE = "OPTIONS, GET, POST";

    private BallerinaProgramContentProvider programContentProvider = BallerinaProgramContentProvider.getInstance();

    public PackagesApiServiceImpl() {
    }

    @Override
    public Response packagesGet(Integer limit, Integer offset, String query, String accept, String ifNoneMatch)
            throws NotFoundException {
        return setCORSHeaders(Response.ok(programContentProvider.getAllPackages().values())).build();
    }

    @Override
    public Response packagesPackageNameGet(String packageName, String accept, String ifNoneMatch,
                                           String ifModifiedSince) throws NotFoundException {
        Map<String, ModelPackage> allPackages = programContentProvider.getAllPackages();
        ModelPackage modelPackage = allPackages.get(packageName);
        if (modelPackage == null) {
            return Response.status(Response.Status.NOT_FOUND).header("Access-Control-Allow-Origin", '*').build();
        }
        return setCORSHeaders(Response.ok(modelPackage)).build();
    }

    @Override
    public Response packagesPost(String contentType) throws NotFoundException {
        return null;
    }

    @Override
    public Response packagesSendCORS() {
        return setCORSHeaders(Response.ok()).build();
    }

    /**
     * Set cross origin header to avoid cross origin issues.
     * @param responseBuilder response builder
     * @return {ResponseBuilder} response builder
     * */
    private static Response.ResponseBuilder setCORSHeaders(Response.ResponseBuilder responseBuilder) {
        return responseBuilder
                .header(ACCESS_CONTROL_ALLOW_ORIGIN_NAME, ACCESS_CONTROL_ALLOW_ORIGIN_VALUE)
                .header(ACCESS_CONTROL_ALLOW_HEADERS_NAME, ACCESS_CONTROL_ALLOW_HEADERS_VALUE)
                .header(ACCESS_CONTROL_ALLOW_METHODS_NAME, ACCESS_CONTROL_ALLOW_METHODS_VALUE);
    }
}

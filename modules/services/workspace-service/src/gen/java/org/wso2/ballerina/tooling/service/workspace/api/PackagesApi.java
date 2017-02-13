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

package org.wso2.ballerina.tooling.service.workspace.api;

import io.swagger.annotations.ApiParam;
import org.wso2.ballerina.tooling.service.workspace.PackagesApiServiceFactory;
import org.wso2.ballerina.tooling.service.workspace.model.ModelPackage;
import org.wso2.ballerina.tooling.service.workspace.model.PackageList;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Path("/service/packages")
@Consumes({"application/json"})
@Produces({"application/json"})
@io.swagger.annotations.Api(description = "the packages API")
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2017-01-27T07:45:46.625Z")
public class PackagesApi {
    private final PackagesApiService delegate = PackagesApiServiceFactory.getPackagesApi();

    @GET
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get a list of packages existing in the environment. ",
                                         notes = "This operation provides you a list of available Packages in the " +
                                                 "customers developer environment. ",
                                         response = PackageList.class, tags = {"Packages",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK. List of qualifying Packages are returned. ",
                                                response = PackageList.class),

            @io.swagger.annotations.ApiResponse(code = 304,
                                                message = "Not Modified. Empty body because the client has already " +
                                                        "the latest version of the requested resource (Will be " +
                                                        "supported in future). ",
                                                response = PackageList.class),

            @io.swagger.annotations.ApiResponse(code = 406,
                                                message = "Not Acceptable. The requested media type is not supported ",
                                                response = PackageList.class)})
    public Response packagesGet(
            @ApiParam(value = "Maximum size of resource array to return. ", defaultValue = "25") @DefaultValue("25")
            @QueryParam("limit") Integer limit
            , @ApiParam(value = "Starting point within the complete list of items qualified. ", defaultValue = "0")
            @DefaultValue("0") @QueryParam("offset") Integer offset
            , @ApiParam(value = "Search based on package name") @QueryParam("query") String query
            , @ApiParam(value = "Media types acceptable for the response. Default is application/json. ",
                        defaultValue = "application/json") @HeaderParam("Accept") String accept
            , @ApiParam(
            value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the " +
                    "resourec. ")
            @HeaderParam("If-None-Match") String ifNoneMatch
    )
            throws NotFoundException {
        return delegate.packagesGet(limit, offset, query, accept, ifNoneMatch);
    }

    @GET
    @Path("/{packageName}")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Get details of a Package ",
                                         notes = "Using this operation, you can retrieve complete details of a single" +
                                                 " Package. You need to provide the Name of the Package to retrive it" +
                                                 ". ",
                                         response = ModelPackage.class, tags = {"Package (Individual)",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "OK. Requested Package is returned ",
                                                response = ModelPackage.class),

            @io.swagger.annotations.ApiResponse(code = 304,
                                                message = "Not Modified. Empty body because the client has already " +
                                                        "the latest version of the requested resource (Will be " +
                                                        "supported in future). ",
                                                response = ModelPackage.class),

            @io.swagger.annotations.ApiResponse(code = 404, message = "Not Found. Requested API does not exist. ",
                                                response = ModelPackage.class),

            @io.swagger.annotations.ApiResponse(code = 406,
                                                message = "Not Acceptable. The requested media type is not supported ",
                                                response = ModelPackage.class)})
    public Response packagesPackageNameGet(
            @ApiParam(value = "", required = true) @PathParam("packageName") String packageName
            , @ApiParam(value = "Media types acceptable for the response. Default is application/json. ",
                        defaultValue = "application/json") @HeaderParam("Accept") String accept
            , @ApiParam(
            value = "Validator for conditional requests; based on the ETag of the formerly retrieved variant of the " +
                    "resourec. ")
            @HeaderParam("If-None-Match") String ifNoneMatch
            , @ApiParam(
            value = "Validator for conditional requests; based on Last Modified header of the formerly retrieved " +
                    "variant of the resource. ")
            @HeaderParam("If-Modified-Since") String ifModifiedSince
    )
            throws NotFoundException {
        return delegate.packagesPackageNameGet(packageName, accept, ifNoneMatch, ifModifiedSince);
    }

    @POST
    @Consumes({"application/zip", "application/octet-stream"})
    @Produces({"application/json"})
    @io.swagger.annotations.ApiOperation(value = "Import a package in to the Ballerina environment ",
                                         notes = "This operation can be used to import a new package in to the " +
                                                 "environment.  ",
                                         response = ModelPackage.class, tags = {"Package (Individual)",})
    @io.swagger.annotations.ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 201,
                                                message = "Created. Successful response with the newly created " +
                                                        "package meta data. ",
                                                response = ModelPackage.class),

            @io.swagger.annotations.ApiResponse(code = 400,
                                                message = "Bad Request. Invalid request or validation error if the " +
                                                        "package has parser errors.  ",
                                                response = ModelPackage.class),

            @io.swagger.annotations.ApiResponse(code = 409, message = "Conflict. Package already exists. ",
                                                response = ModelPackage.class),

            @io.swagger.annotations.ApiResponse(code = 415,
                                                message = "Unsupported media type. The entity of the request was in a" +
                                                        " not supported format. ",
                                                response = ModelPackage.class)})
    public Response packagesPost(
            @ApiParam(value = "Media type of the entity in the body. Default is application/json. ", required = true,
                      defaultValue = "application/json") @HeaderParam("Content-Type") String contentType
    )
            throws NotFoundException {
        return delegate.packagesPost(contentType);
    }

    @OPTIONS
    public Response packagesSendCORS(){
        return delegate.packagesSendCORS();
    }

    @OPTIONS
    @Path("/{packageName}")
    public Response packagesPackageNameSendCORS(){
        return delegate.packagesSendCORS();
    }
}

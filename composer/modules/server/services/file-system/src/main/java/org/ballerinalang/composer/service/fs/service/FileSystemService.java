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
package org.ballerinalang.composer.service.fs.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.composer.server.core.ServerConstants;
import org.ballerinalang.composer.server.spi.ComposerService;
import org.ballerinalang.composer.server.spi.ServiceInfo;
import org.ballerinalang.composer.server.spi.ServiceType;
import org.ballerinalang.composer.service.fs.Constants;
import org.ballerinalang.composer.service.fs.FileSystem;
import org.ballerinalang.composer.service.fs.service.request.CreateFileRequest;
import org.ballerinalang.composer.service.fs.service.request.CreateProjectRequest;
import org.ballerinalang.composer.service.fs.service.request.DeleteFileRequest;
import org.ballerinalang.composer.service.fs.service.request.FileExistsRequest;
import org.ballerinalang.composer.service.fs.service.request.ListFilesRequest;
import org.ballerinalang.composer.service.fs.service.request.MoveCopyFileRequest;
import org.ballerinalang.composer.service.fs.service.request.ReadFileRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Paths;
import java.nio.file.ReadOnlyFileSystemException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *  Micro service that exposes the file system to composer.
 */
@Path(ServerConstants.CONTEXT_ROOT + "/" + Constants.SERVICE_PATH)
public class FileSystemService implements ComposerService {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemService.class);
    private static final String STATUS = "status";
    private static final String SUCCESS = "success";
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
    private static final String MIME_APPLICATION_JSON = "application/json";

    private List<java.nio.file.Path> rootPaths;

    private final FileSystem fileSystem;

    public FileSystemService(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @OPTIONS
    @Path("/list/roots")
    public Response listRootsOptions() {
        return createCORSResponse();
    }

    @POST
    @Path("/list/roots")
    @Consumes(MIME_APPLICATION_JSON)
    @Produces(MIME_APPLICATION_JSON)
    public Response listRoots(ListFilesRequest request) {
        try {
            List<String> extensionList = Arrays.asList(request.getExtensions().split(","));
            JsonArray roots = (rootPaths == null || rootPaths.isEmpty()) ? fileSystem.listRoots(extensionList) :
                    fileSystem.getJsonForRoots(rootPaths, extensionList);
            return createOKResponse(roots);
        } catch (Throwable throwable) {
            logger.error("/root service error", throwable.getMessage(), throwable);
            return createErrorResponse(throwable);
        }
    }

    @OPTIONS
    @Path("/exists")
    public Response pathExistsOptions() {
        return createCORSResponse();
    }

    @POST
    @Path("/exists")
    @Consumes(MIME_APPLICATION_JSON)
    @Produces(MIME_APPLICATION_JSON)
    public Response pathExists(FileExistsRequest request) {
        try {
            JsonObject exists = fileSystem.exists(request.getPath());
            return createOKResponse(exists);
        } catch (Throwable throwable) {
            logger.error("/exists service error", throwable.getMessage(), throwable);
            return createErrorResponse(throwable);
        }
    }

    @OPTIONS
    @Path("/create")
    public Response createOptions() {
        return createCORSResponse();
    }

    @POST
    @Path("/create")
    @Consumes(MIME_APPLICATION_JSON)
    @Produces(MIME_APPLICATION_JSON)
    public Response create(CreateFileRequest request) {
        try {
            String filePath = request.getFullPath() != null
                                ? request.getFullPath()
                                : Paths.get(request.getPath(), request.getName()).toString();
            fileSystem.create(filePath, request.getType(), request.getContent());
            JsonObject entity = new JsonObject();
            entity.addProperty(STATUS, SUCCESS);
            return createOKResponse(entity);
        } catch (Throwable throwable) {
            logger.error("/create service error", throwable.getMessage(), throwable);
            return createErrorResponse(throwable);
        }
    }

    @OPTIONS
    @Path("/move")
    public Response moveOptions() {
        return createCORSResponse();
    }

    @POST
    @Path("/move")
    @Consumes(MIME_APPLICATION_JSON)
    @Produces(MIME_APPLICATION_JSON)
    public Response move(MoveCopyFileRequest request) {
        try {
            fileSystem.move(request.getSrcPath(), request.getDestPath());
            JsonObject entity = new JsonObject();
            entity.addProperty(STATUS, SUCCESS);
            return createOKResponse(entity);
        } catch (Throwable throwable) {
            logger.error("/create service error", throwable.getMessage(), throwable);
            return createErrorResponse(throwable);
        }
    }

    @OPTIONS
    @Path("/copy")
    public Response copyOptions() {
        return createCORSResponse();
    }

    @POST
    @Path("/copy")
    @Consumes(MIME_APPLICATION_JSON)
    @Produces(MIME_APPLICATION_JSON)
    public Response copy(MoveCopyFileRequest request) {
        try {
            fileSystem.copy(request.getSrcPath(), request.getDestPath());
            JsonObject entity = new JsonObject();
            entity.addProperty(STATUS, SUCCESS);
            return createOKResponse(entity);
        } catch (Throwable throwable) {
            logger.error("/create service error", throwable.getMessage(), throwable);
            return createErrorResponse(throwable);
        }
    }

    @OPTIONS
    @Path("/delete")
    public Response deleteOptions() {
        return createCORSResponse();
    }

    @POST
    @Path("/delete")
    @Consumes(MIME_APPLICATION_JSON)
    @Produces(MIME_APPLICATION_JSON)
    public Response delete(DeleteFileRequest request) {
        try {
            fileSystem.delete(request.getPath());
            JsonObject entity = new JsonObject();
            entity.addProperty(STATUS, SUCCESS);
            return createOKResponse(entity);
        } catch (Throwable throwable) {
            logger.error("/delete service error", throwable.getMessage(), throwable);
            return createErrorResponse(throwable);
        }
    }

    @OPTIONS
    @Path("/list/files")
    public Response filesInPathOptions() {
        return createCORSResponse();
    }

    @POST
    @Path("/list/files")
    @Consumes(MIME_APPLICATION_JSON)
    @Produces(MIME_APPLICATION_JSON)
    public Response filesInPath(ListFilesRequest request) {
        try {
            List<String> extensionList = Arrays.asList(request.getExtensions().split(","));
            JsonArray filesInPath = fileSystem.listFilesInPath(request.getPath(), extensionList);
            return createOKResponse(filesInPath);
        } catch (Throwable throwable) {
            logger.error("/list service error", throwable.getMessage(), throwable);
            return createErrorResponse(throwable);
        }
    }

    @OPTIONS
    @Path("/write")
    public Response writeOptions() {
        return createCORSResponse();
    }

    @POST
    @Path("/write")
    @Consumes(MIME_APPLICATION_JSON)
    @Produces(MIME_APPLICATION_JSON)
    public Response write(CreateFileRequest request) {
        try {
            java.nio.file.Path filePath = request.getFullPath() != null
                    ? Paths.get(request.getFullPath())
                    : Paths.get(request.getPath(), request.getName());
            // Note that default charset is set UTF-8 via file.encoding property
            byte[] content = request.isBase64Encoded()
                            ? Base64.getDecoder().decode(request.getContent())
                            : request.getContent().getBytes(Charset.defaultCharset());
            Files.write(filePath, content);
            JsonObject entity = new JsonObject();
            entity.addProperty(STATUS, SUCCESS);
            return createOKResponse(entity);
        } catch (Throwable throwable) {
            logger.error("/write service error", throwable.getMessage(), throwable);
            return createErrorResponse(throwable);
        }
    }

    @OPTIONS
    @Path("/read")
    public Response readOptions() {
        return createCORSResponse();
    }

    @POST
    @Path("/read")
    @Consumes(MIME_APPLICATION_JSON)
    @Produces(MIME_APPLICATION_JSON)
    public Response read(ReadFileRequest request) {
        try {
            return createOKResponse(fileSystem.read(request.getPath()));
        } catch (Throwable throwable) {
            logger.error("/read service error", throwable.getMessage(), throwable);
            return createErrorResponse(throwable);
        }
    }

    @OPTIONS
    @Path("/user/home")
    public Response userHomeOptions() {
        return createCORSResponse();
    }

    @GET
    @Path("/user/home")
    @Produces(MIME_APPLICATION_JSON)
    public Response userHome() {
        try {
            return createOKResponse(fileSystem.getUserHome());
        } catch (Throwable throwable) {
            logger.error("/userHome service error", throwable.getMessage(), throwable);
            return createErrorResponse(throwable);
        }
    }

    @OPTIONS
    @Path("/project/create")
    public Response createProjectOptions() {
        return createCORSResponse();
    }

    @POST
    @Path("/project/create")
    @Consumes(MIME_APPLICATION_JSON)
    @Produces(MIME_APPLICATION_JSON)
    public Response createProject(CreateProjectRequest request) {
        try {
            List<String> commandList = new ArrayList<>();
            // path to ballerina
            String ballerinaExecute = System.getProperty("ballerina.home") + File.separator + "bin" + File.separator +
                    "ballerina";

            if (isWindows()) {
                ballerinaExecute += ".bat";
            }
            commandList.add(ballerinaExecute);
            commandList.add("init");
            String projectPath = request.getPath();
            Process p = Runtime.getRuntime().exec(commandList.toArray(new String[0]), null, new File(projectPath));
            p.waitFor();

            JsonObject entity = new JsonObject();
            entity.addProperty(STATUS, SUCCESS);
            return createOKResponse(entity);
        } catch (Throwable throwable) {
            logger.error("/createProject service error", throwable.getMessage(), throwable);
            return createErrorResponse(throwable);
        }
    }

    private boolean isWindows() {
        String os = System.getProperty("os.name").toLowerCase(Locale.getDefault());
        return (os.contains("win"));
    }

    /**
     * Creates the JSON response for given entity.
     *
     * @param entity Response
     * @return Response
     */
    private Response createOKResponse(Object entity) {
        return Response.status(Response.Status.OK)
                .entity(entity)
                .header(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, '*')
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    /**
     * Creates an error response for the given IO Exception.
     *
     * @param ex Thrown Exception
     * @return Error Message
     */
    private Response createErrorResponse(Throwable ex) {
        JsonObject entity = new JsonObject();
        String errMsg = ex.getMessage();
        if (ex instanceof AccessDeniedException) {
            errMsg = "Access Denied to " + ex.getMessage();
        } else if (ex instanceof NoSuchFileException) {
            errMsg = "No such file: " + ex.getMessage();
        } else if (ex instanceof FileAlreadyExistsException) {
            errMsg = "File already exists: " + ex.getMessage();
        } else if (ex instanceof NotDirectoryException) {
            errMsg = "Not a directory: " + ex.getMessage();
        } else if (ex instanceof ReadOnlyFileSystemException) {
            errMsg = "Read only: " + ex.getMessage();
        } else if (ex instanceof DirectoryNotEmptyException) {
            errMsg = "Directory not empty: " + ex.getMessage();
        } else if (ex instanceof FileNotFoundException) {
            errMsg = "File not found: " + ex.getMessage();
        }
        entity.addProperty("Error", errMsg);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(entity)
                .header(ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, '*')
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    /**
     * Create CORS Response allowing all origins.
     *
     * TODO: Find a better solution to handle CORS in a global manner
     * and to avoid redundant logic for CORS in each service.
     *
     * @return CORS Response
     */
    public Response createCORSResponse() {
        return Response.ok()
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(), "*")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString(), "true")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS.toString(), "POST, GET, OPTIONS ")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS.toString(),
                        HttpHeaderNames.CONTENT_TYPE.toString() + ", " + HttpHeaderNames.ACCEPT.toString() +
                                ", X-Requested-With")
                .build();
    }

    public List<java.nio.file.Path> getRootPaths() {
        return rootPaths;
    }

    public void setRootPaths(List<java.nio.file.Path> rootPaths) {
        this.rootPaths = rootPaths;
    }

    @Override
    public ServiceInfo getServiceInfo() {
        return new ServiceInfo(Constants.SERVICE_NAME, Constants.SERVICE_PATH, ServiceType.HTTP);
    }
}

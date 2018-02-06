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
import org.ballerinalang.composer.server.core.ServerConstants;
import org.ballerinalang.composer.server.spi.ComposerService;
import org.ballerinalang.composer.server.spi.ServiceInfo;
import org.ballerinalang.composer.server.spi.ServiceType;
import org.ballerinalang.composer.service.fs.Constants;
import org.ballerinalang.composer.service.fs.FileSystem;
import org.ballerinalang.composer.service.fs.service.request.CreateFileRequest;
import org.ballerinalang.composer.service.fs.service.request.DeleteFileRequest;
import org.ballerinalang.composer.service.fs.service.request.FileExistsRequest;
import org.ballerinalang.composer.service.fs.service.request.ListFilesRequest;
import org.ballerinalang.composer.service.fs.service.request.MoveCopyFileRequest;
import org.ballerinalang.composer.service.fs.service.request.ReadFileRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.file.AccessDeniedException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Paths;
import java.nio.file.ReadOnlyFileSystemException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
    private static final String FILE_SEPARATOR = "file.separator";
    private static final String STATUS = "status";
    private static final String SUCCESS = "success";
    private static final String ACCESS_CONTROL_ALLOW_ORIGIN_HEADER = "Access-Control-Allow-Origin";
    private static final String MIME_APPLICATION_JSON = "application/json";

    private List<java.nio.file.Path> rootPaths;

    private final FileSystem fileSystem;

    public FileSystemService(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @POST
    @Path("/list/roots")
    @Consumes(MIME_APPLICATION_JSON)
    @Produces(MIME_APPLICATION_JSON)
    public Response root(ListFilesRequest request) {
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

    @POST
    @Path("/create")
    @Consumes(MIME_APPLICATION_JSON)
    @Produces(MIME_APPLICATION_JSON)
    public Response create(CreateFileRequest request) {
        try {
            fileSystem.create(request.getPath(), request.getType(), request.getContent());
            JsonObject entity = new JsonObject();
            entity.addProperty(STATUS, SUCCESS);
            return createOKResponse(entity);
        } catch (Throwable throwable) {
            logger.error("/create service error", throwable.getMessage(), throwable);
            return createErrorResponse(throwable);
        }
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

    @POST
    @Path("/write")
    @Produces(MIME_APPLICATION_JSON)
    public Response write(String payload) {
        try {
            String location = "";
            String configName = "";
            String config = "";
            String isImageFile = "";
            Matcher locationMatcher = Pattern.compile("location=(.*?)&configName").matcher(payload);
            while (locationMatcher.find()) {
                location = locationMatcher.group(1);
            }
            Matcher configNameMatcher = Pattern.compile("configName=(.*?)&").matcher(payload);
            while (configNameMatcher.find()) {
                configName = configNameMatcher.group(1);
            }
            Matcher isImageFileMatcher = Pattern.compile("imageFile=(.*?)&config").matcher(payload);
            while (isImageFileMatcher.find()) {
                isImageFile = isImageFileMatcher.group(1);
            }
            String[] splitConfigContent = payload.split("config=");
            if (splitConfigContent.length > 1) {
                config = splitConfigContent[1];
            }

            byte[] base64ConfigName = Base64.getDecoder().decode(configName);
            byte[] base64Location = Base64.getDecoder().decode(location);
            byte[] configDecoded = URLDecoder.decode(config, "UTF-8").getBytes("UTF-8");
            if (isImageFile.equals("true")) {
                byte[] base64Decoded = Base64.getDecoder().decode(configDecoded);
                Files.write(Paths.get(new String(base64Location, Charset.defaultCharset()) +
                                System.getProperty(FILE_SEPARATOR) +
                                new String(base64ConfigName, Charset.defaultCharset())),
                        base64Decoded);
            } else {
                Files.write(Paths.get(new String(base64Location, Charset.defaultCharset()) +
                                System.getProperty(FILE_SEPARATOR) +
                                new String(base64ConfigName, Charset.defaultCharset())),
                        configDecoded);
            }
            JsonObject entity = new JsonObject();
            entity.addProperty(STATUS, SUCCESS);
            return createOKResponse(entity);
        } catch (Throwable throwable) {
            logger.error("/write service error", throwable.getMessage(), throwable);
            return createErrorResponse(throwable);
        }
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

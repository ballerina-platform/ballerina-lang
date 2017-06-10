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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.composer.service.workspace.Constants;
import org.ballerinalang.composer.service.workspace.model.ModelPackage;
import org.ballerinalang.composer.service.workspace.rest.datamodel.BFile;
import org.ballerinalang.composer.service.workspace.util.WorkspaceUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangPrograms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
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

    private static final Logger logger = LoggerFactory.getLogger(WorkspaceService.class);
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

    @POST
    @Path("/packages")
    @Produces("application/json")
    public Response read(BFile bFile) {
        String response = resolveProgramPackages(deriveFilePath(bFile.getFileName(), bFile.getFilePath()),
                bFile.getPackageName());
        return Response.ok(response, MediaType.APPLICATION_JSON).header("Access-Control-Allow-Origin", '*').build();
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
     * Generate a json with packages in program directory
     *
     * @param filePath    - file path to parent directory of the .bal file
     * @param packageName - package name
     */
    private String resolveProgramPackages(java.nio.file.Path filePath, String packageName) {
        JsonObject response = new JsonObject();
        // Filter out Default package scenario
        if (!".".equals(packageName)) {
            // find nested directory count using package name
            int directoryCount = (packageName.contains(".")) ? packageName.split("\\.").length
                    : 1;

            // find program directory
            java.nio.file.Path parentDir = filePath.getParent();
            for (int i = 0; i < directoryCount; ++i) {
                if (parentDir != null) {
                    parentDir = parentDir.getParent();
                } else {
                    return response.toString();
                }
            }

            // we shouldn't proceed if the parent directory is null
            if (parentDir == null) {
                return response.toString();
            }

            // get packages in program directory
            Map<String, ModelPackage> packages = getPackagesInProgramDirectory(parentDir);
            Collection<ModelPackage> modelPackages = packages.values();

            // add package info into response
            Gson gson = new Gson();
            String json = gson.toJson(modelPackages);
            JsonParser parser = new JsonParser();
            JsonArray packagesArray = parser.parse(json).getAsJsonArray();
            response.add("packages", packagesArray);
        }
        return response.toString();
    }


    /**
     * Get packages in program directory
     *
     * @param programDirPath
     * @return a map contains package details
     * @throws BallerinaException
     */
    private Map<String, ModelPackage> getPackagesInProgramDirectory(java.nio.file.Path programDirPath) {
        Map<String, ModelPackage> modelPackageMap = new HashMap();

        programDirPath = BLangPrograms.validateAndResolveProgramDirPath(programDirPath);
        List<java.nio.file.Path> filePaths = new ArrayList<>();
        searchFilePathsForBalFiles(programDirPath, filePaths, Constants.DIRECTORY_DEPTH);

        // add resolved packages into map
        for (java.nio.file.Path filePath : filePaths) {
            int compare = filePath.compareTo(programDirPath);
            String sourcePath = (String) filePath.toString().subSequence(filePath.toString().length() - compare + 1,
                    filePath.toString().length());
            try {
                BLangProgram bLangProgram = new BLangProgramLoader()
                        .loadMain(programDirPath, Paths.get(sourcePath));
                String[] packageNames = {bLangProgram.getMainPackage().getName()};
                modelPackageMap.putAll(WorkspaceUtils.getResolvedPackagesMap(bLangProgram, packageNames));
            } catch (BallerinaException e) {
                logger.warn(e.getMessage());
                // TODO : we shouldn't catch runtime exceptions. Need to validate properly before executing

                // There might be situations where program directory contains unresolvable/un-parsable .bal files. In
                // those scenarios we still needs to proceed even without package resolving for that particular package.
                // Hence ignoring the exception.
            }
        }
        return modelPackageMap;
    }

    /**
     * Recursive method to search for .bal files and add their parent directory paths to the provided List
     * @param programDirPath - program directory path
     * @param filePaths - file path list
     * @param depth - depth of the directory hierarchy which we should search from the program directory
     */
    private void searchFilePathsForBalFiles(java.nio.file.Path programDirPath,
                                                   List<java.nio.file.Path> filePaths, int depth) {
        // this method is a recursive method. depth is the iteration count and we should return based on the depth count
        if (depth < 0) {
            return;
        }
        try {
            DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(programDirPath);
            depth = depth - 1;
            for (java.nio.file.Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    searchFilePathsForBalFiles(entry, filePaths, depth);
                }
                java.nio.file.Path file = entry.getFileName();
                if (file != null) {
                    String fileName = file.toString();
                    if (fileName.endsWith(".bal")) {
                        filePaths.add(entry.getParent());
                    }
                }
            }
            stream.close();
        } catch (IOException e) {
            // we are ignoring any exception and proceed.
            return;
        }
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

    private java.nio.file.Path deriveFilePath(String fileName, String filePath) {
        return Paths.get(filePath + File.separator + fileName);
    }

}

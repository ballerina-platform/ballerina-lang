/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.composer.service.workspace.rest.datamodel;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.BLangProgramLoader;
import org.ballerinalang.composer.service.workspace.Constants;
import org.ballerinalang.composer.service.workspace.model.ModelPackage;
import org.ballerinalang.composer.service.workspace.util.WorkspaceUtils;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.BallerinaFile;
import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.parser.BallerinaLexer;
import org.ballerinalang.util.parser.BallerinaParser;
import org.ballerinalang.util.parser.antlr4.BLangAntlr4Listener;
import org.ballerinalang.util.program.BLangPrograms;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Basic classes which exposes ballerina language object model over REST service.
 */
@Path("/ballerina")
public class BLangFileRestService {

    private static final Logger logger = LoggerFactory.getLogger(BLangFileRestService.class);

    @GET
    @Path("/model")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBallerinaJsonDataModelGivenLocation(@QueryParam("location") String location) throws IOException {
        InputStream stream = null;
        try {
            stream = new FileInputStream(new File(location));
            String response = parseJsonDataModel(stream, Paths.get(location));
            return Response.ok(response, MediaType.APPLICATION_JSON).build();
        } finally {
            if (null != stream) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    @POST
    @Path("/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateBallerinaSource(BFile bFile) throws IOException {
        InputStream stream = new ByteArrayInputStream(bFile.getContent().getBytes(StandardCharsets.UTF_8));
        return Response.status(Response.Status.OK)
                .entity(validate(stream, deriveFilePath(bFile.getFileName(), bFile.getFilePath())))
                .header("Access-Control-Allow-Origin", '*').type(MediaType.APPLICATION_JSON).build();
    }

    @OPTIONS
    @Path("/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateOptions() {
        return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Credentials",
                "true").header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, DELETE, OPTIONS, HEAD")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
    }

    @POST
    @Path("/model/content")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBallerinaJsonDataModelGivenContent(BFile bFile) throws IOException {
        InputStream stream = new ByteArrayInputStream(bFile.getContent().getBytes(StandardCharsets.UTF_8));
        String response = parseJsonDataModel(stream, deriveFilePath(bFile.getFileName(), bFile.getFilePath()));
        return Response.ok(response, MediaType.APPLICATION_JSON).header("Access-Control-Allow-Origin", '*').build();
    }

    @OPTIONS
    @Path("/model/content")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response options() {
        return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Credentials",
                "true").header("Access-Control-Allow-Methods", "POST, GET, PUT, UPDATE, DELETE, OPTIONS, HEAD")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
    }

    /**
     * Parses an input stream into a json model. During this parsing we are compiling the code as well.
     *
     * @param stream - The input stream.
     * @return A string which contains a json model.
     * @throws IOException
     */
    private String parseJsonDataModel(InputStream stream, java.nio.file.Path filePath) throws IOException {

        ANTLRInputStream antlrInputStream = new ANTLRInputStream(stream);
        BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
        CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);

        BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);
        BallerinaComposerErrorStrategy errorStrategy = new BallerinaComposerErrorStrategy();
        ballerinaParser.setErrorHandler(errorStrategy);

        GlobalScope globalScope = GlobalScope.getInstance();
        BTypes.loadBuiltInTypes(globalScope);
        BLangPackage bLangPackage = new BLangPackage(globalScope);
        BLangPackage.PackageBuilder packageBuilder = new BLangPackage.PackageBuilder(bLangPackage);
        BallerinaComposerModelBuilder bLangModelBuilder = new BallerinaComposerModelBuilder(packageBuilder,
                StringUtils.EMPTY);
        BLangAntlr4Listener ballerinaBaseListener = new BLangAntlr4Listener(true, ballerinaToken, bLangModelBuilder,
                filePath);
        ballerinaParser.addParseListener(ballerinaBaseListener);
        ballerinaParser.compilationUnit();
        BallerinaFile bFile = bLangModelBuilder.build();

        JsonObject response = new JsonObject();
        BLangJSONModelBuilder jsonModelBuilder = new BLangJSONModelBuilder(response);
        bFile.accept(jsonModelBuilder);

        // add packages info in the program directory into response.
        addPackagesInProgramDirectory(bFile, filePath, response);

        return response.toString();
    }

    /**
     * Add packages in program directory to given json object
     *
     * @param bFile - ballerina file object
     * @param filePath - file path to parent directory of the .bal file
     * @param response - jsonObject which the package details should be attached to
     */
    private static void addPackagesInProgramDirectory(BallerinaFile bFile, java.nio.file.Path filePath,
                                                      JsonObject response) {
        String pkgPath = bFile.getPackagePath();
        // Filter out Default package scenario
        if (!".".equals(pkgPath)) {
            try {
                // find nested directory count using package name
                int directoryCount = (bFile.getPackagePath().contains(".")) ? bFile.getPackagePath().split("\\.").length
                        : 1;

                // find program directory
                java.nio.file.Path parentDir = filePath.getParent();
                for (int i = 0; i < directoryCount; ++i) {
                    parentDir = parentDir.getParent();
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
            } catch (BallerinaException e) {
                // TODO : we shouldn't catch runtime exceptions. Need to validate properly before executing

                // There might be situations where program directory contains unresolvable/un-parsable .bal files. In
                // those scenarios we still needs to proceed even without package resolving. Hence ignoring the
                // exception
            }
        }
    }

    /**
     * Get packages in program directory
     * @param programDirPath
     * @return a map contains package details
     * @throws BallerinaException
     */
    private static Map<String, ModelPackage> getPackagesInProgramDirectory(java.nio.file.Path programDirPath)
            throws BallerinaException {
        Map<String, ModelPackage> modelPackageMap = new HashMap();

        programDirPath = BLangPrograms.validateAndResolveProgramDirPath(programDirPath);
        List<java.nio.file.Path> filePaths = new ArrayList<>();
        searchFilePathsForBalFiles(programDirPath, filePaths, Constants.DIRECTORY_DEPTH);

        // add resolved packages into map
        for (java.nio.file.Path filePath : filePaths) {
            int compare = filePath.compareTo(programDirPath);
            String sourcePath = (String) filePath.toString().subSequence(filePath.toString().length() - compare + 1,
                    filePath.toString().length());
            BLangProgram bLangProgram = new BLangProgramLoader()
                    .loadMain(programDirPath, Paths.get(sourcePath));
            String[] packageNames = {bLangProgram.getMainPackage().getName()};
            modelPackageMap.putAll(WorkspaceUtils.getResolvedPackagesMap(bLangProgram, packageNames));
        }
        return modelPackageMap;
    }

    /**
     * Recursive method to search for .bal files and add their parent directory paths to the provided List
     * @param programDirPath - program directory path
     * @param filePaths - file path list
     * @param depth - depth of the directory hierarchy which we should search from the program directory
     */
    private static void searchFilePathsForBalFiles(java.nio.file.Path programDirPath,
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
        } catch (IOException e) {
            // we are ignoring any exception and proceed.
            return;
        }
    }

    /**
     * Validates a given ballerina input
     *
     * @param stream - The input stream.
     * @return List of errors if any
     * @throws IOException
     */
    private JsonObject validate(InputStream stream, java.nio.file.Path filePath) throws IOException {

        ANTLRInputStream antlrInputStream = new ANTLRInputStream(stream);
        BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
        CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);

        BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);
        BallerinaComposerErrorStrategy errorStrategy = new BallerinaComposerErrorStrategy();
        ballerinaParser.setErrorHandler(errorStrategy);

        GlobalScope globalScope = GlobalScope.getInstance();
        BTypes.loadBuiltInTypes(globalScope);
        BLangPackage bLangPackage = new BLangPackage(globalScope);
        BLangPackage.PackageBuilder packageBuilder = new BLangPackage.PackageBuilder(bLangPackage);

        BallerinaComposerModelBuilder bLangModelBuilder = new BallerinaComposerModelBuilder(packageBuilder,
                StringUtils.EMPTY);
        BLangAntlr4Listener ballerinaBaseListener = new BLangAntlr4Listener(bLangModelBuilder, filePath);
        ballerinaParser.addParseListener(ballerinaBaseListener);
        ballerinaParser.compilationUnit();

        JsonArray errors = new JsonArray();

        for (SyntaxError error : errorStrategy.getErrorTokens()) {
            errors.add(error.toJson());
        }

        JsonObject result = new JsonObject();
        result.add("errors", errors);

        return result;
    }

    private java.nio.file.Path deriveFilePath(String fileName, String filePath) {
        return Paths.get(filePath + File.separator + fileName);
    }

}

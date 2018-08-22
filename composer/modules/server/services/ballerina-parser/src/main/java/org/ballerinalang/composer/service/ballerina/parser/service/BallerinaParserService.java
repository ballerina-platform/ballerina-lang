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
package org.ballerinalang.composer.service.ballerina.parser.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.composer.server.core.ServerConstants;
import org.ballerinalang.composer.server.spi.ComposerService;
import org.ballerinalang.composer.server.spi.ServiceInfo;
import org.ballerinalang.composer.server.spi.ServiceType;
import org.ballerinalang.composer.service.ballerina.parser.Constants;
import org.ballerinalang.composer.service.ballerina.parser.service.model.BFile;
import org.ballerinalang.composer.service.ballerina.parser.service.model.BLangSourceFragment;
import org.ballerinalang.composer.service.ballerina.parser.service.model.lang.ModelPackage;
import org.ballerinalang.composer.service.ballerina.parser.service.util.BLangFragmentParser;
import org.ballerinalang.composer.service.ballerina.parser.service.util.ParserUtils;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSCompilerException;
import org.ballerinalang.langserver.compiler.LSCompilerUtil;
import org.ballerinalang.langserver.compiler.TreeUtil;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.langserver.compiler.format.JSONGenerationException;
import org.ballerinalang.langserver.compiler.workspace.ExtendedWorkspaceDocumentManagerImpl;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;

/**
 * Micro service for ballerina parser.
 */
@Path(ServerConstants.CONTEXT_ROOT + "/" + Constants.SERVICE_PATH)
public class BallerinaParserService implements ComposerService {

    private static final Logger logger = LoggerFactory.getLogger(BallerinaParserService.class);
    private static final Gson GSON = new Gson();

    @OPTIONS
    @Path("/built-in-packages")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBuiltInPackages() {
        return Response.ok()
                .header(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE.toString(), "600 ")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(), "*")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString(), "true")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS.toString(),
                        "POST, GET, PUT, UPDATE, DELETE, OPTIONS, HEAD")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS.toString(),
                        HttpHeaderNames.CONTENT_TYPE.toString() + ", " + HttpHeaderNames.ACCEPT.toString() +
                                ", X-Requested-With").build();
    }

    @GET
    @Path("/built-in-packages")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateAndParseBFile() {
        JsonObject response = new JsonObject();
        // add package info into response
        Gson gson = new Gson();
        String json = gson.toJson(ParserUtils.getAllPackages().values());
        JsonParser parser = new JsonParser();
        JsonArray packagesArray = parser.parse(json).getAsJsonArray();
        response.add("packages", packagesArray);
        return Response.status(Response.Status.OK)
                .entity(response)
                .header("Access-Control-Allow-Origin", '*').type(MediaType.APPLICATION_JSON).build();
    }

    @OPTIONS
    @Path("/endpoints")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEndpointOptions() {
        return Response.ok()
                .header(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE.toString(), "600 ")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(), "*")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString(), "true")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS.toString(),
                        "POST, GET, PUT, UPDATE, DELETE, OPTIONS, HEAD")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS.toString(),
                        HttpHeaderNames.CONTENT_TYPE.toString() + ", " + HttpHeaderNames.ACCEPT.toString() +
                                ", X-Requested-With").build();
    }

    @GET
    @Path("/endpoints")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEndpoints() {
        JsonObject response = new JsonObject();
        // add package info into response
        Gson gson = new Gson();
        String json = gson.toJson(ParserUtils.getEndpoints());
        JsonParser parser = new JsonParser();
        JsonArray packagesArray = parser.parse(json).getAsJsonArray();
        response.add("packages", packagesArray);
        return Response.status(Response.Status.OK)
                .entity(response)
                .header("Access-Control-Allow-Origin", '*').type(MediaType.APPLICATION_JSON).build();
    }

    @OPTIONS
    @Path("/actions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActionsOptions() {
        return Response.ok()
                .header(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE.toString(), "600 ")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(), "*")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString(), "true")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS.toString(),
                        "POST, GET, PUT, UPDATE, DELETE, OPTIONS, HEAD")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS.toString(),
                        HttpHeaderNames.CONTENT_TYPE.toString() + ", " + HttpHeaderNames.ACCEPT.toString() +
                                ", X-Requested-With").build();
    }

    @GET
    @Path("/actions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActions(@QueryParam(value = "pkgName") String pkgName,
                               @QueryParam(value = "typeName") String typeName) {
        JsonObject response = new JsonObject();
        // add package info into response
        Gson gson = new Gson();
        String json = gson.toJson(ParserUtils.getActions(pkgName, typeName));
        JsonParser parser = new JsonParser();
        JsonArray packagesArray = parser.parse(json).getAsJsonArray();
        response.add("packages", packagesArray);
        return Response.status(Response.Status.OK)
                .entity(response)
                .header("Access-Control-Allow-Origin", '*').type(MediaType.APPLICATION_JSON).build();
    }

    @OPTIONS
    @Path("/built-in-types")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBuiltInTypesOptions() {
        return Response.ok()
                .header(HttpHeaderNames.ACCESS_CONTROL_MAX_AGE.toString(), "600 ")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(), "*")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString(), "true")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS.toString(),
                        "POST, GET, PUT, UPDATE, DELETE, OPTIONS, HEAD")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS.toString(),
                        HttpHeaderNames.CONTENT_TYPE.toString() + ", " + HttpHeaderNames.ACCEPT.toString() +
                                ", X-Requested-With").build();
    }

    @GET
    @Path("/built-in-types")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBuiltInTypes() {
        JsonObject response = new JsonObject();
        // add package info into response
        Gson gson = new Gson();
        String json = gson.toJson(ParserUtils.getBuiltinTypes());
        JsonParser parser = new JsonParser();
        JsonArray packagesArray = parser.parse(json).getAsJsonArray();
        response.add("types", packagesArray);
        return Response.status(Response.Status.OK)
                .entity(response)
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(), '*').type(MediaType.APPLICATION_JSON)
                .build();
    }

    @POST
    @Path("/file/validate-and-parse")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateAndParseBFile(BFile bFileRequest) throws LSCompilerException, JSONGenerationException {
        return Response.status(Response.Status.OK)
                .entity(validateAndParse(bFileRequest))
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(), '*').type(MediaType.APPLICATION_JSON)
                .build();
    }

    @OPTIONS
    @Path("/file/validate-and-parse")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateAndParseOptions() {
        return Response.ok()
                .header("Access-Control-Max-Age", "600 ")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(), "*")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString(), "true")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS.toString(),
                        "POST, GET, PUT, UPDATE, DELETE, OPTIONS, HEAD")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS.toString(),
                        HttpHeaderNames.CONTENT_TYPE.toString() + ", " + HttpHeaderNames.ACCEPT.toString() +
                                ", X-Requested-With").build();
    }

    @POST
    @Path("/model/parse-fragment")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBallerinaJsonDataModelGivenFragment(BLangSourceFragment sourceFragment) throws IOException {
        WorkspaceDocumentManager documentManager = ExtendedWorkspaceDocumentManagerImpl.getInstance();
        String response = BLangFragmentParser.parseFragment(documentManager, sourceFragment);
        return Response.ok(response, MediaType.APPLICATION_JSON).header(
                HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(), '*').build();
    }

    @OPTIONS
    @Path("/model/parse-fragment")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response optionsParseFragment() {
        return Response.ok()
                .header("Access-Control-Max-Age", "600 ")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN.toString(), "*")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS.toString(), "true")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS.toString(), "POST, GET, OPTIONS")
                .header(HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS.toString(),
                        HttpHeaderNames.CONTENT_TYPE.toString() + ", " + HttpHeaderNames.ACCEPT.toString() +
                                ", X-Requested-With").build();
    }

    /**
     * Validates a given ballerina input.
     *
     * @param bFileRequest - Object which holds data about Ballerina content.
     * @return List of errors if any
     */
    private synchronized JsonObject validateAndParse(BFile bFileRequest) throws LSCompilerException,
                                                                                JSONGenerationException {
        final String fileName = bFileRequest.getFileName();
        final String content = bFileRequest.getContent();

        String programDir = "";
        String debugPackagePath = ".";
        java.nio.file.Path filePath;
        if (LSCompilerUtil.UNTITLED_BAL.equals(fileName)) {
            filePath = LSCompilerUtil.createTempFile(LSCompilerUtil.UNTITLED_BAL);
        } else {
            filePath = Paths.get(bFileRequest.getFilePath(), bFileRequest.getFileName());
        }

        BallerinaFile bFile;
        ExtendedWorkspaceDocumentManagerImpl documentManager = ExtendedWorkspaceDocumentManagerImpl.getInstance();
        Optional<Lock> lock = documentManager.enableExplicitMode(filePath);
        LSCompiler lsCompiler = new LSCompiler(documentManager);
        try {
            bFile = lsCompiler.updateAndCompileFile(filePath, content, CompilerPhase.CODE_ANALYZE, documentManager);
        } finally {
            documentManager.disableExplicitMode(lock.orElse(null));
        }
        programDir = (bFile.isBallerinaProject()) ? LSCompilerUtil.getSourceRoot(filePath) : "";

        if (bFile.isBallerinaProject() && bFile.getBLangPackage().isPresent()) {
            debugPackagePath = bFile.getBLangPackage().get().packageID.toString();
        }

        Optional<BLangPackage> model = bFile.getBLangPackage();
        Optional<List<Diagnostic>> diagnostics = bFile.getDiagnostics();

        ErrorCategory errorCategory = ErrorCategory.NONE;
        if (diagnostics.isPresent() && !diagnostics.get().isEmpty()) {
            if (!model.isPresent() || model.get().symbol == null) {
                errorCategory = ErrorCategory.SYNTAX;
            } else {
                errorCategory = ErrorCategory.SEMANTIC;
            }
        }
        JsonArray errors = new JsonArray();
        final String errorCategoryName = errorCategory.name();
        diagnostics.ifPresent(d -> d.forEach(diagnostic -> {

            JsonObject error = new JsonObject();
            Diagnostic.DiagnosticPosition position = diagnostic.getPosition();
            if (position != null) {
                if (!diagnostic.getSource().getCompilationUnitName().equals(fileName)) {
                    return;
                }
                error.addProperty(JSONModelConstants.ROW, position.getStartLine());
                error.addProperty(JSONModelConstants.COLUMN, position.getStartColumn());
                error.addProperty(JSONModelConstants.TYPE, JSONModelConstants.ERROR);
                error.addProperty(JSONModelConstants.CATEGORY, errorCategoryName);
            } else {
                // position == null means it's a bug in core side.
                error.addProperty(JSONModelConstants.CATEGORY, ErrorCategory.RUNTIME.name());
            }

            error.addProperty(JSONModelConstants.TEXT, diagnostic.getMessage());
            errors.add(error);
        }));
        JsonObject result = new JsonObject();
        result.add(JSONModelConstants.ERRORS, errors);

        JsonElement diagnosticsJson = GSON.toJsonTree(diagnostics);
        result.add(JSONModelConstants.DIAGNOSTICS, diagnosticsJson);

        if (model.isPresent() && model.get().symbol != null && bFileRequest.needTree()) {
            BLangCompilationUnit compilationUnit = model.get().getCompilationUnits().stream()
                    .filter(compUnit -> fileName.equals(compUnit.getName()))
                    .findFirst().orElse(null);
            JsonElement modelElement = TreeUtil.generateJSON(compilationUnit);
            result.add(JSONModelConstants.MODEL, modelElement);
        }

        final Map<String, ModelPackage> modelPackage = new HashMap<>();
        ParserUtils.loadPackageMap(JSONModelConstants.CURRENT_PACKAGE_NAME, bFile.getBLangPackage().orElse(null),
                                   modelPackage);

        modelPackage.values().stream().findFirst().filter(pkg -> bFileRequest.needPackageInfo())
                .ifPresent(aPackage -> {
                    JsonElement packageInfo = GSON.toJsonTree(aPackage);
                    result.add(JSONModelConstants.PACKAGE_INFO, packageInfo);
                });
        result.addProperty(JSONModelConstants.PROGRAM_DIR_PATH, programDir);
        result.addProperty("debugPackagePath", debugPackagePath);
        return result;
    }

    /**
     * Enum for Error Category.
     */
    public enum ErrorCategory {
        SYNTAX,
        SEMANTIC,
        RUNTIME,
        NONE;
    }

    @Override
    public ServiceInfo getServiceInfo() {
        return new ServiceInfo(Constants.SERVICE_NAME, Constants.SERVICE_PATH, ServiceType.HTTP);
    }
}

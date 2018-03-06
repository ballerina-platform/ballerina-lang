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
import org.ballerinalang.composer.service.ballerina.parser.service.model.BallerinaFile;
import org.ballerinalang.composer.service.ballerina.parser.service.model.lang.ModelPackage;
import org.ballerinalang.composer.service.ballerina.parser.service.util.BLangFragmentParser;
import org.ballerinalang.composer.service.ballerina.parser.service.util.ParserUtils;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Micro service for ballerina parser.
 */
@Path(ServerConstants.CONTEXT_ROOT + "/" + Constants.SERVICE_PATH)
public class BallerinaParserService implements ComposerService {
    
    private static final String PACKAGE_REGEX = "package\\s+([a-zA_Z_][\\.\\w]*);";

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
    public Response validateAndParseBFile(BFile bFileRequest) throws IOException, InvocationTargetException,
            IllegalAccessException {
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
        String response = BLangFragmentParser.parseFragment(sourceFragment);
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
    private JsonObject validateAndParse(BFile bFileRequest) throws InvocationTargetException, IllegalAccessException {
        final String filePath = bFileRequest.getFilePath();
        final String fileName = bFileRequest.getFileName();
        final String content = bFileRequest.getContent();

        Pattern pkgPattern = Pattern.compile(PACKAGE_REGEX);
        Matcher pkgMatcher = pkgPattern.matcher(content);
        String programDir = null;
        String unitToCompile = fileName;
        if (pkgMatcher.find()) {
            final String packageName = pkgMatcher.group(1);
            if (bFileRequest.needProgramDir() && packageName != null) {
                List<String> pathParts = Arrays.asList(filePath.split(Pattern.quote(File.separator)));
                List<String> pkgParts = Arrays.asList(packageName.split(Pattern.quote(".")));
                Collections.reverse(pkgParts);
                boolean foundProgramDir = true;
                for (int i = 1; i <= pkgParts.size(); i++) {
                    if (!pathParts.get(pathParts.size() - i).equals(pkgParts.get(i - 1))) {
                        foundProgramDir = false;
                        break;
                    }
                }
                if (foundProgramDir) {
                    List<String> programDirParts = pathParts.subList(0, pathParts.size() - pkgParts.size());
                    programDir = String.join(File.separator, programDirParts);
                    unitToCompile = packageName;
                }
            }
        }
        // compile package in disk to resolve constructs in complete package (including constructs from other files)
        final BLangPackage packageFromDisk = Files.exists(Paths.get(filePath, fileName))
                ? ParserUtils.getBallerinaFile(programDir != null ? programDir : filePath, unitToCompile)
                .getBLangPackage()
                : null;
        // always use dirty content from editor to generate model
        // TODO: Remove this once in-memory file resolver with dirty content for compiler is implemented
        final BallerinaFile balFileFromDirtyContent = ParserUtils.getBallerinaFileForContent(fileName, content,
                CompilerPhase.CODE_ANALYZE);
        // always get compilation unit and diagnostics from dirty content
        final BLangPackage model = balFileFromDirtyContent.getBLangPackage();
        final List<Diagnostic> diagnostics = balFileFromDirtyContent.getDiagnostics();

        ErrorCategory errorCategory = ErrorCategory.NONE;
        if (!diagnostics.isEmpty()) {
            if (model == null) {
                errorCategory = ErrorCategory.SYNTAX;
            } else {
                errorCategory = ErrorCategory.SEMANTIC;
            }
        }
        JsonArray errors = new JsonArray();
        final String errorCategoryName = errorCategory.name();
        diagnostics.forEach(diagnostic -> {

            JsonObject error = new JsonObject();
            Diagnostic.DiagnosticPosition position = diagnostic.getPosition();
            if (position != null) {
                if (!diagnostic.getSource().getCompilationUnitName().equals(fileName)) {
                    return;
                }

                error.addProperty("row", position.getStartLine());
                error.addProperty("column", position.getStartColumn());
                error.addProperty("type", "error");
                error.addProperty("category", errorCategoryName);
            } else {
                // position == null means it's a bug in core side.
                error.addProperty("category", ErrorCategory.RUNTIME.name());
            }

            error.addProperty("text", diagnostic.getMessage());
            errors.add(error);
        });
        JsonObject result = new JsonObject();
        result.add("errors", errors);

        Gson gson = new Gson();
        JsonElement diagnosticsJson = gson.toJsonTree(diagnostics);
        result.add("diagnostics", diagnosticsJson);

        if (model != null && bFileRequest.needTree()) {
            BLangCompilationUnit compilationUnit = model.getCompilationUnits().stream().
                    filter(compUnit -> fileName.equals(compUnit.getName())).findFirst().get();
            JsonElement modelElement = CommonUtil.generateJSON(compilationUnit, new HashMap<>());
            result.add("model", modelElement);
        }

        // adding current package info whenever we have a parsed model
        final Map<String, ModelPackage> modelPackage = new HashMap<>();
        // file is in a package, load constructs from package in disk
        if (packageFromDisk != null) {
            ParserUtils.loadPackageMap("Current Package", packageFromDisk, modelPackage);
            // remove constructs from current file and later add them from dirty content
            ParserUtils.removeConstructsOfFile("Current Package", fileName, modelPackage);
        }
        // add constructs in current file's dirty content to package map
        ParserUtils.loadPackageMap("Current Package", balFileFromDirtyContent.getBLangPackage(),
                modelPackage);
        // Add 'packageInfo' only if there are any packages.
        Optional<ModelPackage> packageInfoJson = modelPackage.values().stream().findFirst();
        if (packageInfoJson.isPresent() && bFileRequest.needPackageInfo()) {
            JsonElement packageInfo = gson.toJsonTree(packageInfoJson.get());
            result.add("packageInfo", packageInfo);
        }
        if (programDir != null) {
            result.addProperty("programDirPath", programDir);
        }
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

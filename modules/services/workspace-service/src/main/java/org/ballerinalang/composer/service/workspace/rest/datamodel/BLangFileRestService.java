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

import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.composer.service.workspace.langserver.model.ModelPackage;
import org.ballerinalang.composer.service.workspace.util.WorkspaceUtils;
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Basic classes which exposes ballerina language object model over REST service.
 */
@Path("/ballerina")
public class BLangFileRestService {

    private static final Logger logger = LoggerFactory.getLogger(BLangFileRestService.class);
    public static final String UNESCAPED_VALUE = "unescapedValue";

    @POST
    @Path("/validate")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response validateBallerinaSource(BFile bFile) throws IOException {
        return Response.status(Response.Status.OK)
                .entity(validate(bFile))
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
    public Response getBallerinaJsonDataModelGivenContent(BFile bFile) throws IOException, InvocationTargetException,
            IllegalAccessException {
        String response = parseJsonDataModel(bFile);
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


    @POST
    @Path("/model/parse-fragment")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBallerinaJsonDataModelGivenFragment(BLangSourceFragment sourceFragment) throws IOException {
        String response = BLangFragmentParser.parseFragment(sourceFragment);
        return Response.ok(response, MediaType.APPLICATION_JSON).header("Access-Control-Allow-Origin", '*').build();
    }

    @OPTIONS
    @Path("/model/parse-fragment")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response optionsParseFragment() {
        return Response.ok().header("Access-Control-Allow-Origin", "*").header("Access-Control-Allow-Credentials",
                "true").header("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With").build();
    }

    /**
     * Parses a Ballerina content into a json model. We can provide either a String content or the path to the file.
     * Data is encapsulated in a BFile object
     *
     * @param bFile - Object which holds data about Ballerina content.
     * @return A string which contains a json model.
     * @throws IOException
     */
    private static String parseJsonDataModel(BFile bFile) throws IOException, InvocationTargetException,
            IllegalAccessException {
        String filePath = bFile.getFilePath();
        String fileName = bFile.getFileName();
        String content = bFile.getContent();
    
        org.wso2.ballerinalang.compiler.tree.BLangPackage model;
        List<Diagnostic> diagnostics;
    
        // Sometimes we are getting Ballerina content without a file in the file-system.
        if (!Files.exists(Paths.get(filePath, fileName))) {
            BallerinaFile ballerinaFile = WorkspaceUtils.getBallerinaFileForContent(fileName, content,
                    CompilerPhase.CODE_ANALYZE);
            model = ballerinaFile.getBLangPackage();
            diagnostics = ballerinaFile.getDiagnostics();
        
        } else {
            BallerinaFile ballerinaFile = WorkspaceUtils.getBallerinaFile(filePath, fileName);
            model = ballerinaFile.getBLangPackage();
            diagnostics = ballerinaFile.getDiagnostics();
        }
    
        final Map<String, ModelPackage> modelPackage = new HashMap<>();
        WorkspaceUtils.loadPackageMap("Current Package", model, modelPackage);
    
        Gson gson = new Gson();
        JsonElement diagnosticsJson = gson.toJsonTree(diagnostics);
        JsonObject result = new JsonObject();

        if (model != null) {
            BLangCompilationUnit compilationUnit = model.getCompilationUnits().stream().
                    filter(compUnit -> fileName.equals(compUnit.getName())).findFirst().get();
            JsonElement modelElement = generateJSON(compilationUnit);
            result.add("model", modelElement);
        }
        result.add("diagnostics", diagnosticsJson);
    
        // Add 'packageInfo' only if there are any packages.
        Optional<ModelPackage> packageInfoJson = modelPackage.values().stream().findFirst();
        if (packageInfoJson.isPresent()) {
            JsonElement packageInfo = gson.toJsonTree(packageInfoJson.get());
            result.add("packageInfo", packageInfo);
        }
    
        return result.toString();
    }

    public static JsonElement generateJSON(Node node) throws InvocationTargetException, IllegalAccessException {
        if (node == null) {
            return JsonNull.INSTANCE;
        }
        Set<Method> methods = ClassUtils.getAllInterfaces(node.getClass()).stream()
                .flatMap(aClass -> Arrays.stream(aClass.getMethods()))
                .collect(Collectors.toSet());
        JsonObject nodeJson = new JsonObject();

        JsonArray wsJsonArray = new JsonArray();
        Set<Whitespace> ws = node.getWS();
        if (ws != null && !ws.isEmpty()) {
            for (Whitespace whitespace : ws) {
                JsonObject wsJson = new JsonObject();
                wsJson.addProperty("ws", whitespace.getWs());
                wsJson.addProperty("i", whitespace.getIndex());
                wsJson.addProperty("text", whitespace.getPrevious());
                wsJson.addProperty("static", whitespace.isStatic());
                wsJsonArray.add(wsJson);
            }
            nodeJson.add("ws", wsJsonArray);
        }
        Diagnostic.DiagnosticPosition position = node.getPosition();
        if (position != null) {
            JsonObject positionJson = new JsonObject();
            positionJson.addProperty("startColumn", position.startColumn());
            positionJson.addProperty("startLine", position.getStartLine());
            positionJson.addProperty("endColumn", position.endColumn());
            positionJson.addProperty("endLine", position.getEndLine());
            nodeJson.add("position", positionJson);
        }
        for (Method m : methods) {
            String name = m.getName();

            if (name.equals("getWS") || name.equals("getPosition")) {
                continue;
            }

            String jsonName;
            if (name.startsWith("get")) {
                jsonName = toJsonName(name, 3);
            } else if (name.startsWith("is")) {
                jsonName = toJsonName(name, 2);
            } else {
                continue;
            }

            Object prop = m.invoke(node);

            /* Literal class - This class is escaped in backend to address cases like "ss\"" and 8.0 and null */
            if (node.getKind() == NodeKind.LITERAL && "value".equals(jsonName)) {
                if (prop instanceof String) {
                    nodeJson.addProperty(jsonName, '"' + StringEscapeUtils.escapeJava((String) prop) + '"');
                    nodeJson.addProperty(UNESCAPED_VALUE, String.valueOf(prop));
                } else {
                    nodeJson.addProperty(jsonName, String.valueOf(prop));
                }
                continue;
            }

            if (prop instanceof List && jsonName.equals("types")) {
                // Currently we don't need any Symbols for the UI. So skipping for now.
                continue;
            }

            /* Virtual prop */
            // This is since the invocation symbol abstract method has not currently been exposed from the runtime
            // TODO: This is a temporary fix and will be changed accordingly with the new action invocation impl
            if (node.getKind() == NodeKind.INVOCATION) {
                assert node instanceof BLangInvocation : node.getClass();
                BLangInvocation invocation = (BLangInvocation) node;
                if (invocation.symbol != null) {
                    nodeJson.addProperty("invocationType", invocation.symbol.kind.toString());
                }
            }

            /* Node classes */
            if (prop instanceof Node) {
                nodeJson.add(jsonName, generateJSON((Node) prop));
            } else if (prop instanceof List) {
                List listProp = (List) prop;
                JsonArray listPropJson = new JsonArray();
                nodeJson.add(jsonName, listPropJson);
                for (Object listPropItem : listProp) {
                    if (listPropItem instanceof Node) {
                        listPropJson.add(generateJSON((Node) listPropItem));
                    } else {
                        logger.debug("Can't serialize " + jsonName + ", has a an array of " + listPropItem);
                    }
                }


            /* Runtime model classes */
            } else if (prop instanceof Set && jsonName.equals("flags")) {
                Set flags = (Set) prop;
                for (Flag flag : Flag.values()) {
                    nodeJson.addProperty(StringUtils.lowerCase(flag.toString()), flags.contains(flag));
                }
            } else if (prop instanceof Set) {
                // TODO : limit this else if to getInputs getOutputs of transform.
                Set vars = (Set) prop;
                JsonArray listVarJson = new JsonArray();
                nodeJson.add(jsonName, listVarJson);
                for (Object obj : vars) {
                    listVarJson.add(obj.toString());
                }
            } else if (prop instanceof NodeKind) {
                String kindName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, prop.toString());
                nodeJson.addProperty(jsonName, kindName);
            } else if (prop instanceof OperatorKind) {
                nodeJson.addProperty(jsonName, prop.toString());


            /* Generic classes */
            } else if (prop instanceof String) {
                nodeJson.addProperty(jsonName, (String) prop);
            } else if (prop instanceof Number) {
                nodeJson.addProperty(jsonName, (Number) prop);
            } else if (prop instanceof Boolean) {
                nodeJson.addProperty(jsonName, (Boolean) prop);
            } else if (prop instanceof Enum) {
                nodeJson.addProperty(jsonName, StringUtils.lowerCase(((Enum) prop).name()));
            } else if (prop != null) {
                nodeJson.addProperty(jsonName, prop.toString());
                String message = "Node " + node.getClass().getSimpleName() +
                        " contains unknown type prop: " + jsonName + " of type " + prop.getClass();
                logger.error(message);
            }
        }
        return nodeJson;
    }

    private static String toJsonName(String name, int prefixLen) {
        return Character.toLowerCase(name.charAt(prefixLen)) + name.substring(prefixLen + 1);
    }

    /**
     * Validates a given ballerina input
     *
     * @param bFile - Object which holds data about Ballerina content.
     * @return List of errors if any
     */
    private JsonObject validate(BFile bFile) {
        String fileName = "untitled.bal";
        String content = bFile.getContent();
        BallerinaFile ballerinaFile = WorkspaceUtils.getBallerinaFileForContent(fileName, content,
                CompilerPhase.CODE_ANALYZE);
        List<Diagnostic> diagnostics = ballerinaFile.getDiagnostics();
        ErrorCategory errorCategory = ErrorCategory.NONE;
        BLangPackage model = ballerinaFile.getBLangPackage();
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
                error.addProperty("column", position.startColumn());
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

        // adding current package info whenever we have a parsed model
        final Map<String, ModelPackage> modelPackage = new HashMap<>();
        WorkspaceUtils.loadPackageMap("Current Package", model, modelPackage);
        // Add 'packageInfo' only if there are any packages.
        Optional<ModelPackage> packageInfoJson = modelPackage.values().stream().findFirst();
        if (packageInfoJson.isPresent()) {
            Gson gson = new Gson();
            JsonElement packageInfo = gson.toJsonTree(packageInfoJson.get());
            result.add("packageInfo", packageInfo);
        }

        return result;
    }

    /**
     * Enum for Error Category
     */
    public enum ErrorCategory {
        SYNTAX,
        SEMANTIC,
        RUNTIME,
        NONE;
    }

}

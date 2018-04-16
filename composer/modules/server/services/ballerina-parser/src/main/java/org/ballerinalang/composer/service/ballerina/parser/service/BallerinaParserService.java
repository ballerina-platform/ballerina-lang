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

import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.ballerinalang.langserver.TextDocumentServiceUtil;
import org.ballerinalang.langserver.common.modal.BallerinaFile;
import org.ballerinalang.langserver.common.utils.LSParserUtils;
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachmentPoint;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.ballerinalang.langserver.common.utils.LSParserUtils.UNTITLED_BAL;

/**
 * Micro service for ballerina parser.
 */
@Path(ServerConstants.CONTEXT_ROOT + "/" + Constants.SERVICE_PATH)
public class BallerinaParserService implements ComposerService {

    private static final Logger logger = LoggerFactory.getLogger(BallerinaParserService.class);
    private static final String SYMBOL_TYPE = "symbolType";
    private static final String INVOCATION_TYPE = "invocationType";
    private static final String UNESCAPED_VALUE = "unescapedValue";
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

    public static JsonElement generateJSON(Node node, Map<String, Node> anonStructs)
            throws InvocationTargetException, IllegalAccessException {
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
            positionJson.addProperty("startColumn", position.getStartColumn());
            positionJson.addProperty("startLine", position.getStartLine());
            positionJson.addProperty("endColumn", position.getEndColumn());
            positionJson.addProperty("endLine", position.getEndLine());
            nodeJson.add("position", positionJson);
        }

        /* Virtual props */

        JsonArray type = getType(node);
        if (type != null) {
            nodeJson.add(SYMBOL_TYPE, type);
        }
        if (node.getKind() == NodeKind.INVOCATION) {
            assert node instanceof BLangInvocation : node.getClass();
            BLangInvocation invocation = (BLangInvocation) node;
            if (invocation.symbol != null && invocation.symbol.kind != null) {
                nodeJson.addProperty(INVOCATION_TYPE, invocation.symbol.kind.toString());
            }
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

            if (node.getKind() == NodeKind.ANNOTATION
                    && node instanceof BLangAnnotation) {
                JsonArray attachmentPoints = new JsonArray();
                ((BLangAnnotation) node)
                        .getAttachmentPoints()
                        .stream()
                        .map(BLangAnnotationAttachmentPoint::getAttachmentPoint)
                        .map(BLangAnnotationAttachmentPoint.AttachmentPoint::getValue)
                        .map(JsonPrimitive::new)
                        .forEach(attachmentPoints::add);
                nodeJson.add("attachmentPoints", attachmentPoints);
            }

            if (node.getKind() == NodeKind.USER_DEFINED_TYPE && jsonName.equals("typeName")) {
                IdentifierNode typeNode = (IdentifierNode) prop;
                Node structNode;
                if (typeNode.getValue().startsWith("$anonStruct$") &&
                        (structNode = anonStructs.remove(typeNode.getValue())) != null) {
                    JsonObject anonStruct = generateJSON(structNode, anonStructs).getAsJsonObject();
                    anonStruct.addProperty("anonStruct", true);
                    nodeJson.add("anonStruct", anonStruct);
                    continue;
                }
            }

            if (prop instanceof List && jsonName.equals("types")) {
                // Currently we don't need any Symbols for the UI. So skipping for now.
                continue;
            }


            /* Node classes */
            if (prop instanceof Node) {
                nodeJson.add(jsonName, generateJSON((Node) prop, anonStructs));
            } else if (prop instanceof List) {
                List listProp = (List) prop;
                JsonArray listPropJson = new JsonArray();
                nodeJson.add(jsonName, listPropJson);
                for (Object listPropItem : listProp) {
                    if (listPropItem instanceof Node) {
                        /* Remove top level anon func and struct */
                        if (node.getKind() == NodeKind.COMPILATION_UNIT) {
                            if (listPropItem instanceof BLangStruct && ((BLangStruct) listPropItem).isAnonymous) {
                                anonStructs.put(((BLangStruct) listPropItem).getName().getValue(),
                                        ((BLangStruct) listPropItem));
                                continue;
                            }
                            if (listPropItem instanceof BLangFunction
                                    && (((BLangFunction) listPropItem)).name.value.startsWith("$lambda$")) {
                                continue;
                            }
                        }
                        listPropJson.add(generateJSON((Node) listPropItem, anonStructs));
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

    private static JsonArray getType(Node node) {
        BType type = ((BLangNode) node).type;
        if (node instanceof BLangInvocation) {
            JsonArray jsonElements = new JsonArray();
            /*for (BType returnType : ((BLangInvocation) node).types) {
                jsonElements.add(returnType.getKind().typeName());
            }*/
            return jsonElements;
        } else if (type != null) {
            JsonArray jsonElements = new JsonArray();
            jsonElements.add(type.getKind().typeName());
            return jsonElements;
        }
        return null;
    }

    private static String toJsonName(String name, int prefixLen) {
        return Character.toLowerCase(name.charAt(prefixLen)) + name.substring(prefixLen + 1);
    }

    /**
     * Validates a given ballerina input.
     *
     * @param bFileRequest - Object which holds data about Ballerina content.
     * @return List of errors if any
     */
    private JsonObject validateAndParse(BFile bFileRequest) throws InvocationTargetException, IllegalAccessException {
        final String fileName = bFileRequest.getFileName();
        final String content = bFileRequest.getContent();

        BallerinaFile bFile;
        String programDir = "";
        if (UNTITLED_BAL.equals(fileName)) {
            bFile = LSParserUtils.compile(content, CompilerPhase.CODE_ANALYZE);
        } else {
            java.nio.file.Path filePath = Paths.get(bFileRequest.getFilePath(), bFileRequest.getFileName());
            bFile = LSParserUtils.compile(content, filePath, CompilerPhase.CODE_ANALYZE);
            programDir = (bFile.isBallerinaProject()) ? TextDocumentServiceUtil.getSourceRoot(filePath) : "";
        }

        final BLangPackage model = bFile.getBLangPackage();
        final List<Diagnostic> diagnostics = bFile.getDiagnostics();

        ErrorCategory errorCategory = ErrorCategory.NONE;
        if (!diagnostics.isEmpty()) {
            if (model == null || model.symbol == null) {
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

        if (model != null && model.symbol != null && bFileRequest.needTree()) {
            BLangCompilationUnit compilationUnit = model.getCompilationUnits().stream().
                    filter(compUnit -> fileName.equals(compUnit.getName())).findFirst().get();
            JsonElement modelElement = generateJSON(compilationUnit, new HashMap<>());
            result.add("model", modelElement);
        }

        final Map<String, ModelPackage> modelPackage = new HashMap<>();
        ParserUtils.loadPackageMap("Current Package", bFile.getBLangPackage(), modelPackage);
        Optional<ModelPackage> packageInfoJson = modelPackage.values().stream().findFirst();
        if (packageInfoJson.isPresent() && bFileRequest.needPackageInfo()) {
            JsonElement packageInfo = gson.toJsonTree(packageInfoJson.get());
            result.add("packageInfo", packageInfo);
        }
        result.addProperty("programDirPath", programDir);
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

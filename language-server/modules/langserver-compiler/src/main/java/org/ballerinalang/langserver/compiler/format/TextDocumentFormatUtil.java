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
package org.ballerinalang.langserver.compiler.format;

import com.google.common.base.CaseFormat;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Utilities for text document format.
 */
public class TextDocumentFormatUtil {

    private static final Logger logger = LoggerFactory.getLogger(TextDocumentFormatUtil.class);

    private static final String SYMBOL_TYPE = "symbolType";

    private static final String INVOCATION_TYPE = "invocationType";

    private static final String UNESCAPED_VALUE = "unescapedValue";

    /**
     * Get the AST for the current text document's content.
     *
     * @param params          Document Formatting parameters
     * @param documentManager Workspace document manager instance
     * @param context         Document formatting context
     * @return {@link JsonObject}   AST as a Json Object
     */
    public static JsonObject getAST(DocumentFormattingParams params, WorkspaceDocumentManager documentManager,
                                    LSContext context) {
        String documentUri = params.getTextDocument().getUri();
        String[] uriParts = documentUri.split(Pattern.quote(File.separator));
        String fileName = uriParts[uriParts.length - 1];
        final BLangPackage bLangPackage = LSCompiler.getBLangPackage(context, documentManager,
                                                                     true, LSCustomErrorStrategy.class, false).get(0);
        context.put(DocumentServiceKeys.CURRENT_PACKAGE_NAME_KEY, bLangPackage.symbol.getName().getValue());
        final List<Diagnostic> diagnostics = new ArrayList<>();
        JsonArray errors = new JsonArray();
        JsonObject result = new JsonObject();
        result.add("errors", errors);

        Gson gson = new Gson();
        JsonElement diagnosticsJson = gson.toJsonTree(diagnostics);
        result.add("diagnostics", diagnosticsJson);

        BLangCompilationUnit compilationUnit = bLangPackage.getCompilationUnits().stream().
                filter(compUnit -> fileName.equals(compUnit.getName())).findFirst().orElseGet(null);
        JsonElement modelElement = generateJSON(compilationUnit, new HashMap<>());
        result.add("model", modelElement);

        return result;
    }

    /**
     * Generate json representation for the given node.
     *
     * @param node        Node to get the json representation
     * @param anonStructs Map of anonymous structs
     * @return {@link JsonElement}          Json Representation of the node
     */
    public static JsonElement generateJSON(Node node, Map<String, Node> anonStructs) {
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
        org.ballerinalang.util.diagnostic.Diagnostic.DiagnosticPosition position = node.getPosition();
        if (position != null) {
            JsonObject positionJson = new JsonObject();
            positionJson.addProperty("startColumn", position.getStartColumn());
            positionJson.addProperty("startLine", position.getStartLine());
            positionJson.addProperty("endColumn", position.getEndColumn());
            positionJson.addProperty("endLine", position.getEndLine());
            nodeJson.add("position", positionJson);
        }

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

            Object prop = null;
            try {
                prop = m.invoke(node);
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error("Error while serializing source to JSON: [" + e.getMessage() + "]");
            }

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

    /**
     * Convert given name to the Json object name.
     *
     * @param name      Name to be converted
     * @param prefixLen Length of prefix
     * @return {@link String}   Converted value
     */
    private static String toJsonName(String name, int prefixLen) {
        return Character.toLowerCase(name.charAt(prefixLen)) + name.substring(prefixLen + 1);
    }


    /**
     * Get Type of the node as an Json Array.
     *
     * @param node Node to get the types
     * @return {@link JsonArray}    Converted array value
     */
    public static JsonArray getType(Node node) {
        if (node instanceof BLangNode) {
            BType type = ((BLangNode) node).type;
            if (node instanceof BLangInvocation) {
                JsonArray jsonElements = new JsonArray();
                jsonElements.add(((BLangInvocation) node).type.getKind().typeName());
                return jsonElements;
            } else if (type != null) {
                JsonArray jsonElements = new JsonArray();
                jsonElements.add(type.getKind().typeName());
                return jsonElements;
            }
        }

        return null;
    }
}

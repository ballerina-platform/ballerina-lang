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
import com.google.gson.JsonPrimitive;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.common.LSDocumentIdentifierImpl;
import org.ballerinalang.langserver.compiler.common.modal.SymbolMetaInfo;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.model.Whitespace;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.ActionNode;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.util.Flags;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
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
     * @param file            File path as a URI
     * @param documentManager Workspace document manager instance
     * @param context         Document formatting context
     * @return {@link JsonObject}   AST as a Json Object
     * @throws JSONGenerationException    when AST build fails
     * @throws CompilationFailedException when compilation fails
     */
    public static JsonObject getAST(Path file, WorkspaceDocumentManager documentManager, LSContext context)
            throws JSONGenerationException, CompilationFailedException {
        String path = file.toAbsolutePath().toString();
        LSDocumentIdentifier lsDocument = new LSDocumentIdentifierImpl(file.toUri().toString());
        String packageName = lsDocument.getOwnerModule();
        String[] breakFromPackage = path.split(Pattern.quote(packageName + File.separator));
        String relativePath = breakFromPackage[breakFromPackage.length - 1];

        final BLangPackage bLangPackage = LSModuleCompiler.getBLangPackage(context, documentManager, false, false);
        final List<Diagnostic> diagnostics = new ArrayList<>();
        JsonArray errors = new JsonArray();
        JsonObject result = new JsonObject();
        result.add("errors", errors);

        Gson gson = new Gson();
        JsonElement diagnosticsJson = gson.toJsonTree(diagnostics);
        result.add("diagnostics", diagnosticsJson);
        BLangCompilationUnit compilationUnit;

        // If package is testable package process as tests
        // else process normally
        if (isTestablePackage(relativePath)) {
            compilationUnit = bLangPackage.getTestablePkg().getCompilationUnits().stream().
                    filter(compUnit -> (relativePath).equals(compUnit.getName()))
                    .findFirst().orElse(null);
        } else {
            compilationUnit = bLangPackage.getCompilationUnits().stream().
                    filter(compUnit -> relativePath.equals(compUnit.getName())).findFirst().orElse(null);
        }

        JsonElement modelElement = generateJSON(compilationUnit, new HashMap<>(), new HashMap<>());
        result.add("model", modelElement);
        return result;
    }

    private static boolean isTestablePackage(String relativeFilePath) {
        return relativeFilePath.startsWith("tests" + File.separator);
    }

    /**
     * Generate json representation for the given node.
     *
     * @param node             Node to get the json representation
     * @param anonStructs      Map of anonymous structs
     * @param visibleEPsByNode Visible endpoints by node map
     * @return {@link JsonElement}          Json Representation of the node
     * @throws JSONGenerationException when Json error occurs
     */
    public static JsonElement generateJSON(Node node, Map<String, Node> anonStructs,
                                           Map<BLangNode, List<SymbolMetaInfo>> visibleEPsByNode)
            throws JSONGenerationException {
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

        // Add identity hash code for each node as the node's ID.
        nodeJson.addProperty("id", System.identityHashCode(node));

        // Add the visible endpoints for a given node
        if (visibleEPsByNode.containsKey(node)) {
            List<SymbolMetaInfo> endpointMetaList = visibleEPsByNode.get(node);
            JsonArray endpoints = new JsonArray();
            endpointMetaList.forEach(symbolMetaInfo -> endpoints.add(symbolMetaInfo.getJson()));
            nodeJson.add("VisibleEndpoints", endpoints);
        }

        if (node instanceof BLangSimpleVariableDef) {
            nodeJson.addProperty("isEndpoint", isClientObject(((BLangSimpleVariableDef) node).var.symbol));
        } else if (node instanceof BLangSimpleVariable) {
            nodeJson.addProperty("isEndpoint", isClientObject(((BLangSimpleVariable) node).symbol));
        } else if (node instanceof BLangSimpleVarRef) {
            BSymbol varSymbol = ((BLangSimpleVarRef) node).symbol;
            nodeJson.addProperty("isEndpoint", varSymbol != null && isClientObject(varSymbol));
        }

        if (node instanceof ActionNode) {
            nodeJson.addProperty("actionInvocation", true);
        }

        JsonArray type = getType(node);
        if (type != null) {
            nodeJson.add(SYMBOL_TYPE, type);
        }
        JsonObject typeInfo = getTypeInfo(node);
        if (typeInfo != null) {
            nodeJson.add("typeInfo", typeInfo);
        }
        if (node.getKind() == NodeKind.INVOCATION) {

            assert node instanceof BLangInvocation : node.getClass();
            BLangInvocation invocation = (BLangInvocation) node;
            if (invocation.symbol != null && invocation.symbol.kind != null) {
                nodeJson.addProperty(INVOCATION_TYPE, invocation.symbol.kind.toString());
                JsonArray defLink = new JsonArray();
                getDefinitionLink(invocation.symbol, defLink);
                nodeJson.add("definition", defLink);
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
                throw new JSONGenerationException("Error occurred while generating JSON", e);
            }

            /* Literal class - This class is escaped in backend to address cases like "ss\"" and 8.0 and null */
            if ((node.getKind() == NodeKind.LITERAL || node.getKind() == NodeKind.NUMERIC_LITERAL) &&
                    "value".equals(jsonName)) {
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
                        .getAttachPoints()
                        .stream()
                        .map(attachPoint -> attachPoint.point.getValue())
                        .map(JsonPrimitive::new)
                        .forEach(attachmentPoints::add);
                nodeJson.add("attachmentPoints", attachmentPoints);
            }

            if (prop instanceof List && jsonName.equals("types")) {
                // Currently we don't need any Symbols for the UI. So skipping for now.
                continue;
            }

            /* Node classes */
            if (prop instanceof Node) {
                nodeJson.add(jsonName, generateJSON((Node) prop, anonStructs, visibleEPsByNode));
            } else if (prop instanceof List) {
                List listProp = (List) prop;
                JsonArray listPropJson = new JsonArray();
                nodeJson.add(jsonName, listPropJson);
                for (Object listPropItem : listProp) {
                    if (listPropItem instanceof Node) {
                        /* Remove top level anon func and struct */
                        if (node.getKind() == NodeKind.COMPILATION_UNIT) {
                            if (listPropItem instanceof BLangFunction
                                    && (((BLangFunction) listPropItem)).name.value.startsWith("$lambda$")) {
                                continue;
                            }
                        }
                        listPropJson.add(generateJSON((Node) listPropItem, anonStructs, visibleEPsByNode));
                    } else if (listPropItem instanceof BLangRecordVarRef.BLangRecordVarRefKeyValue) {
                        listPropJson.add(generateJSON(((BLangRecordVarRef.BLangRecordVarRefKeyValue) listPropItem)
                                .getVariableName(), anonStructs, visibleEPsByNode));
                        listPropJson.add(generateJSON(((BLangRecordVarRef.BLangRecordVarRefKeyValue) listPropItem)
                                .getBindingPattern(), anonStructs, visibleEPsByNode));
                    } else if (listPropItem instanceof BLangRecordVariable.BLangRecordVariableKeyValue) {
                        listPropJson.add(generateJSON(((BLangRecordVariable.BLangRecordVariableKeyValue) listPropItem)
                                .getKey(), anonStructs, visibleEPsByNode));
                        listPropJson.add(generateJSON(((BLangRecordVariable.BLangRecordVariableKeyValue) listPropItem)
                                .getValue(), anonStructs, visibleEPsByNode));
                    } else if (listPropItem instanceof BLangErrorVariable.BLangErrorDetailEntry) {
                        listPropJson.add(generateJSON(((BLangErrorVariable.BLangErrorDetailEntry) listPropItem)
                                .getKey(), anonStructs, visibleEPsByNode));
                        listPropJson.add(generateJSON(((BLangErrorVariable.BLangErrorDetailEntry) listPropItem)
                                .getValue(), anonStructs, visibleEPsByNode));
                    } else if (listPropItem instanceof String) {
                        listPropJson.add((String) listPropItem);
                    } else if (listPropItem instanceof BLangLetVariable) {
                        // TODO: check with language team whether this is the correct way to handle LetVariable.
                        BLangLetVariable variable = (BLangLetVariable) listPropItem;
                        listPropJson.add(generateJSON(variable.definitionNode, anonStructs, visibleEPsByNode));
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
            } else if (prop instanceof int[]) {
                int[] intArray = ((int[]) prop);
                JsonArray intArrayPropJson = new JsonArray();
                nodeJson.add(jsonName, intArrayPropJson);
                for (int intProp : intArray) {
                    intArrayPropJson.add(intProp);
                }
            } else if (prop != null) {
                nodeJson.addProperty(jsonName, prop.toString());
            }
        }
        return nodeJson;
    }

    /**
     * Get a list of names of the owners of the invocation node.
     *
     * @param symbol The symbol of which the owners are found
     * @param owners Array of strings that will be filled with the names of owners in the chain
     */
    public static void getDefinitionLink(BSymbol symbol, JsonArray owners) {
        if (symbol == null) {
            return;
        }

        JsonArray part = new JsonArray();
        if (symbol.name == null) {
            part.add((JsonElement) null);
        } else {
            part.add(symbol.name.value);
        }

        if (symbol.kind == null) {
            part.add((JsonElement) null);
        } else {
            part.add(symbol.kind.name());
        }
        owners.add(part);
        getDefinitionLink(symbol.owner, owners);
    }

    /**
     * Convert given name to the Json object name.
     *
     * @param name      Name to be converted
     * @param prefixLen Length of prefix
     * @return {@link String}   Converted value
     */
    public static String toJsonName(String name, int prefixLen) {
        return Character.toLowerCase(name.charAt(prefixLen)) + name.substring(prefixLen + 1);
    }


    /**
     * Get Type of the node as an Json Array.
     *
     * @param node Node to get the types
     * @return {@link JsonArray}    Converted array value
     */
    public static JsonArray getType(Node node) {
        if (!(node instanceof BLangNode)) {
            return null;
        }
        BType type = ((BLangNode) node).type;
        if (node instanceof BLangInvocation) {
            return new JsonArray();
        } else if (type != null) {
            JsonArray jsonElements = new JsonArray();
            jsonElements.add(type.getKind().typeName());
            return jsonElements;
        }
        return null;
    }

    /**
     * Get Type info of the node.
     *
     * @param node Node to get the types
     * @return {@link JsonObject} TypeInfo object.
     */
    public static JsonObject getTypeInfo(Node node) {
        if (!(node instanceof BLangNode)) {
            return null;
        }
        BType type = ((BLangNode) node).type;
        if (node instanceof BLangInvocation) {
            return null;
        } else if (type != null &&  type.tsymbol != null &&  type.tsymbol.pkgID != null) {
            PackageID pkgID = type.tsymbol.pkgID;
            JsonObject typeObj = new JsonObject();
            typeObj.addProperty("orgName", pkgID.getOrgName().value);
            typeObj.addProperty("modName", pkgID.getName().value);
            typeObj.addProperty("version", pkgID.getPackageVersion().value);
            typeObj.addProperty("name", type.tsymbol.name.value);
            return typeObj;
        }
        return null;
    }

    /**
     * Check whether a given symbol is client object or not.
     *
     * @param bSymbol BSymbol to evaluate
     * @return {@link Boolean}  Symbol evaluation status
     */
    public static boolean isClientObject(BSymbol bSymbol) {
        return bSymbol != null && bSymbol.type != null && bSymbol.type.tsymbol != null
                && SymbolKind.OBJECT.equals(bSymbol.type.tsymbol.kind)
                && (bSymbol.type.tsymbol.flags & Flags.CLIENT) == Flags.CLIENT;
    }
}

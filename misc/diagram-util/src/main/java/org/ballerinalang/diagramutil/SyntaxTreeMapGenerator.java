/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.diagramutil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.BallerinaModuleID;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ChildNodeEntry;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.text.LinePosition;
import io.ballerina.tools.text.LineRange;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Generates a Map<String, Object> for a given SyntaxTree.
 */
public class SyntaxTreeMapGenerator extends NodeTransformer<JsonElement> {
    private SemanticModel semanticModel;
    private String fileName;
    private List<JsonObject> visibleEpsForEachBlock;

    public SyntaxTreeMapGenerator(String fileName, SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
        this.fileName = fileName;
        this.visibleEpsForEachBlock = new ArrayList<>();
    }

    @Override
    protected JsonElement transformSyntaxNode(Node node) {
        JsonObject nodeJson = new JsonObject();
        NonTerminalNode nonTerminalNode = (NonTerminalNode) node;
        for (ChildNodeEntry childNodeEntry : nonTerminalNode.childEntries()) {
            if (childNodeEntry.isList()) {
                JsonArray childList = new JsonArray();
                for (Node listChildNode : childNodeEntry.nodeList()) {
                    childList.add(apply(listChildNode));
                }
                nodeJson.add(childNodeEntry.name(), childList);
            } else if (childNodeEntry.node().isPresent()) {
                nodeJson.add(childNodeEntry.name(), apply(childNodeEntry.node().get()));
            }
        }
        nodeJson.addProperty("source", node.toSourceCode());
        nodeJson.addProperty("kind", prettifyKind(node.kind().toString()));

        if (node.lineRange() != null) {
            LineRange lineRange = node.lineRange();
            LinePosition startLine = lineRange.startLine();
            LinePosition endLine = lineRange.endLine();
            JsonObject position = new JsonObject();
            position.addProperty("startLine", startLine.line());
            position.addProperty("startColumn", startLine.offset());
            position.addProperty("endLine", endLine.line());
            position.addProperty("endColumn", endLine.offset());
            nodeJson.add("position", position);

            try {
                Optional<TypeSymbol> typeSymbol = this.semanticModel.type(this.fileName, lineRange);
                Optional<Symbol> symbol = this.semanticModel.symbol(this.fileName, startLine);
                JsonObject symbolJson = new JsonObject();
                if (typeSymbol.isPresent()) {
                    TypeSymbol rawType = getRawType(typeSymbol.get());
                    if (rawType.typeKind() == TypeDescKind.OBJECT) {
                        ObjectTypeSymbol objectTypeSymbol = (ObjectTypeSymbol) rawType;
                        boolean isEndpoint = objectTypeSymbol.typeQualifiers()
                                .contains(ObjectTypeSymbol.TypeQualifier.CLIENT);
                        if (isEndpoint) {
                            symbolJson.addProperty("isEndpoint", true);
                            JsonObject ep = visibleEP(node, typeSymbol.get());
                            if (ep.size() > 0) {
                                this.visibleEpsForEachBlock.add(ep);
                            }
                        }
                    }
                    symbolJson.add("typeSymbol", generateTypeJson(typeSymbol.get()));
                }

                if (symbol.isPresent()) {
                    symbolJson.add("symbol", generateTypeJson(symbol.get()));
                }

                List<Diagnostic> diagnostics = this.semanticModel.diagnostics(lineRange);
                if (diagnostics != null) {
                    JsonArray diagnosticsArray = new JsonArray();
                    for (Diagnostic diagnostic : diagnostics) {
                        JsonObject diagnosticJson = new JsonObject();
                        diagnosticJson.addProperty("message", diagnostic.message());
                        DiagnosticInfo diagnosticInfo = diagnostic.diagnosticInfo();
                        if (diagnosticInfo != null) {
                            JsonObject diagnosticInfoJson = new JsonObject();
                            diagnosticInfoJson.addProperty("code", diagnosticInfo.code());
                            diagnosticInfoJson.addProperty("severity", diagnosticInfo.severity().name());
                            diagnosticJson.add("diagnosticInfo", diagnosticInfoJson);
                        }
                        diagnosticsArray.add(diagnosticJson);
                    }
                    symbolJson.add("diagnostics", diagnosticsArray);
                }

                nodeJson.add("typeData", symbolJson);
            } catch (NoSuchElementException | JSONGenerationException | AssertionError e) {
                // TODO: Remove the AssertionError catcher when fix the symbolVisitor to be extended from BaseVisitor.
                // Ignore as semantic API calls cannot break the ST JSON creation.
            }

            if (node.kind() == SyntaxKind.FUNCTION_BODY_BLOCK && this.visibleEpsForEachBlock.size() > 0
                    && nodeJson.get("typeData") != null) {
                JsonArray eps = new JsonArray();
                this.visibleEpsForEachBlock.forEach(eps::add);
                nodeJson.add("VisibleEndpoints", eps);
                this.visibleEpsForEachBlock = new ArrayList<>();
            }
        }

        return nodeJson;
    }

    private JsonObject visibleEP(Node node, TypeSymbol typeSymbol) {
        JsonObject symbolMetaInfo = new JsonObject();
        if (node.kind() == SyntaxKind.REQUIRED_PARAM) {
            RequiredParameterNode requiredParameterNode = (RequiredParameterNode) node;
            Optional<Token> paramName = requiredParameterNode.paramName();
            symbolMetaInfo.addProperty("name", paramName.isPresent() ? paramName.get().text() : "");
            symbolMetaInfo.addProperty("isCaller", typeSymbol.name().equals("Caller"));
            symbolMetaInfo.addProperty("typeName", typeSymbol.name());
            symbolMetaInfo.addProperty("orgName", typeSymbol.moduleID().orgName());
            symbolMetaInfo.addProperty("moduleName", typeSymbol.moduleID().moduleName());
        } else if (node.kind() == SyntaxKind.LOCAL_VAR_DECL) {
            VariableDeclarationNode variableDeclarationNode = (VariableDeclarationNode) node;
            CaptureBindingPatternNode captureBindingPatternNode =
                    (CaptureBindingPatternNode) variableDeclarationNode.typedBindingPattern().bindingPattern();
            symbolMetaInfo.addProperty("name", captureBindingPatternNode.variableName().text());
            symbolMetaInfo.addProperty("isCaller", typeSymbol.name().equals("Caller"));
            symbolMetaInfo.addProperty("typeName", typeSymbol.name());
            symbolMetaInfo.addProperty("orgName", typeSymbol.moduleID().orgName());
            symbolMetaInfo.addProperty("moduleName", typeSymbol.moduleID().moduleName());
        } else if (node.kind() == SyntaxKind.ASSIGNMENT_STATEMENT) {
            AssignmentStatementNode assignmentStatementNode = (AssignmentStatementNode) node;
            SimpleNameReferenceNode simpleNameReferenceNode =
                    (SimpleNameReferenceNode) assignmentStatementNode.varRef();
            symbolMetaInfo.addProperty("name", simpleNameReferenceNode.name().text());
            symbolMetaInfo.addProperty("isCaller", typeSymbol.name().equals("Caller"));
            symbolMetaInfo.addProperty("typeName", typeSymbol.name());
            symbolMetaInfo.addProperty("orgName", typeSymbol.moduleID().orgName());
            symbolMetaInfo.addProperty("moduleName", typeSymbol.moduleID().moduleName());
        }

        return symbolMetaInfo;
    }

    private TypeSymbol getRawType(TypeSymbol typeDescriptor) {
        return typeDescriptor.typeKind() == TypeDescKind.TYPE_REFERENCE
                ? ((TypeReferenceTypeSymbol) typeDescriptor).typeDescriptor() : typeDescriptor;
    }

    private JsonElement generateTypeJson(Symbol symbol) throws JSONGenerationException {
        if (symbol == null) {
            return JsonNull.INSTANCE;
        }

        Set<Method> methods = ClassUtils.getAllInterfaces(symbol.getClass()).stream()
                .flatMap(aClass -> Arrays.stream(aClass.getMethods()))
                .collect(Collectors.toSet());
        JsonObject nodeJson = new JsonObject();
        for (Method m : methods) {
            String jsonName = m.getName();
            if (m.getParameterCount() > 0
                    || jsonName.equals("typeDefinitions")
                    || jsonName.equals("functions")
                    || jsonName.equals("classes")
                    || jsonName.equals("constants")
                    || jsonName.equals("listeners")
                    || jsonName.equals("services")
                    || jsonName.equals("allSymbols")
                    || jsonName.equals("resources")
                    || jsonName.equals("methods")
                    || jsonName.equals("langLibMethods")
                    || jsonName.equals("location")) {
                continue;
            }

            Object prop = null;
            try {
                prop = m.invoke(symbol);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new JSONGenerationException("Error occurred while generating JSON", e);
            }

            if (prop instanceof Symbol) {
                nodeJson.add(jsonName, generateTypeJson((Symbol) prop));
            } else if (prop instanceof List) {
                List listProp = (List) prop;
                JsonArray listPropJson = new JsonArray();
                for (Object listPropItem : listProp) {
                    if (listPropItem instanceof Symbol) {
                        listPropJson.add(generateTypeJson((Symbol) listPropItem));
                    } else if (listPropItem instanceof String) {
                        listPropJson.add((String) listPropItem);
                    } else if (listPropItem instanceof Boolean) {
                        listPropJson.add((Boolean) listPropItem);
                    }
                }
            } else if (prop instanceof BallerinaModuleID) {
                BallerinaModuleID ballerinaModuleID = (BallerinaModuleID) prop;
                JsonObject moduleIdJson = new JsonObject();
                moduleIdJson.addProperty("orgName", ballerinaModuleID.orgName());
                moduleIdJson.addProperty("moduleName", ballerinaModuleID.moduleName());
                moduleIdJson.addProperty("version", ballerinaModuleID.version());
                nodeJson.add(jsonName, moduleIdJson);
            } else if (prop instanceof TypeDescKind) {
                nodeJson.addProperty(jsonName, ((TypeDescKind) prop).getName());
            } else if (prop instanceof io.ballerina.compiler.api.symbols.SymbolKind) {
                nodeJson.addProperty(jsonName, ((io.ballerina.compiler.api.symbols.SymbolKind) prop).name());
            } else if (prop instanceof String) {
                nodeJson.addProperty(jsonName, (String) prop);
            } else if (prop instanceof Boolean) {
                nodeJson.addProperty(jsonName, (Boolean) prop);
            }
        }

        return nodeJson;
    }

    private JsonElement apply(Node node) {
        JsonObject nodeInfo = new JsonObject();
        nodeInfo.addProperty("kind", prettifyKind(node.kind().toString()));
        if (node instanceof Token) {
            nodeInfo.addProperty("isToken", true);
            nodeInfo.addProperty("value", ((Token) node).text());
        } else {
            JsonElement memberValues = node.apply(this);
            memberValues.getAsJsonObject().entrySet().forEach(memberEntry -> {
                nodeInfo.add(memberEntry.getKey(), memberEntry.getValue());
            });
        }
        return nodeInfo;
    }

    private String prettifyKind(String kind) {
        return Arrays.stream(kind.split("_"))
                .map(String::toLowerCase)
                .map(StringUtils::capitalize)
                .collect(Collectors.joining());
    }
}

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
import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.CaptureBindingPatternNode;
import io.ballerina.compiler.syntax.tree.ChildNodeEntry;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.RequiredParameterNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.tools.diagnostics.Diagnostic;
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
    private List<JsonObject> visibleEpsForEachBlock;
    private List<JsonObject> visibleEpsForModule;

    public SyntaxTreeMapGenerator(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
        this.visibleEpsForEachBlock = new ArrayList<>();
        this.visibleEpsForModule = new ArrayList<>();
    }

    public SyntaxTreeMapGenerator() {
        this.visibleEpsForEachBlock = new ArrayList<>();
        this.visibleEpsForModule = new ArrayList<>();
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
        // TODO: Generalize the diagnostic implementation.
        Iterable<Diagnostic> syntaxDiagnostics = node.diagnostics();
        if (syntaxDiagnostics != null && syntaxDiagnostics.iterator().hasNext()) {
            nodeJson.add("syntaxDiagnostics", SyntaxTreeDiagnosticsUtil.getDiagnostics(syntaxDiagnostics));
        }

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

            // TODO: Check and remove the Type() API usage and replace with symbol() API;
            JsonObject symbolJson = new JsonObject();
            try {
                if (semanticModel != null) {
                    Optional<TypeSymbol> typeSymbol = this.semanticModel.type(lineRange);
                    if (typeSymbol.isPresent()) {
                        TypeSymbol rawType = getRawType(typeSymbol.get());
                        if (rawType.typeKind() == TypeDescKind.OBJECT) {
                            ObjectTypeSymbol objectTypeSymbol = (ObjectTypeSymbol) rawType;
                            boolean isEndpoint = objectTypeSymbol.qualifiers()
                                    .contains(Qualifier.CLIENT);
                            if (isEndpoint) {
                                symbolJson.addProperty("isEndpoint", true);
                                JsonObject ep = visibleEP(node, typeSymbol.get(), false);
                                if (ep.size() > 0) {
                                    this.visibleEpsForEachBlock.add(ep);
                                }
                            }
                        }
                        symbolJson.add("typeSymbol", generateTypeJson(typeSymbol.get()));

                        if (typeSymbol.get().getModule().isPresent()) {
                            // todo: check if this is the correct way to access
                            JsonObject typeDataJson = (JsonObject) generateTypeJson(typeSymbol.get().getModule().get());
                            ((JsonObject) symbolJson.get("typeSymbol")).add("moduleID", typeDataJson.get("id"));
                        } else if (typeSymbol.get() instanceof UnionTypeSymbol) {
                            JsonArray memberArray = new JsonArray();
                            ((UnionTypeSymbol) typeSymbol.get()).memberTypeDescriptors().forEach(member -> {
                                try {
                                    JsonObject memberJson = (JsonObject) generateTypeJson(member);
                                    memberArray.add(memberJson);
                                } catch (JSONGenerationException e) {
                                    // Ignore
                                }
                            });
                            ((JsonObject) symbolJson.get("typeSymbol")).add("members", memberArray);
                        }
                    }
                }
            } catch (Exception | AssertionError e) {
                // TODO: Remove the AssertionError catcher when fix the symbolVisitor to be extended from BaseVisitor.
                // Ignore as semantic API calls cannot break the ST JSON creation.
            }

            try {
                if (semanticModel != null) {
                    Optional<Symbol> symbol = this.semanticModel.symbol(node);

                    if (symbol.isPresent() && (symbol.get() instanceof VariableSymbol)) {
                        VariableSymbol variableSymbol = (VariableSymbol) symbol.get();
                        markVisibleEp(variableSymbol, symbolJson, node);
                    }

                    if (symbol.isPresent()) {
                        symbolJson.add("symbol", generateTypeJson(symbol.get()));
                    }

                    List<Diagnostic> diagnostics = this.semanticModel.diagnostics(lineRange);
                    if (diagnostics != null) {
                        symbolJson.add("diagnostics", SyntaxTreeDiagnosticsUtil.getDiagnostics(diagnostics));
                    }
                }

                nodeJson.add("typeData", symbolJson);
            } catch (NoSuchElementException | JSONGenerationException | AssertionError e) {
                // TODO: Remove the AssertionError catcher when fix the symbolVisitor to be extended from BaseVisitor.
                // Ignore as semantic API calls cannot break the ST JSON creation.
            }

            if (node.kind() == SyntaxKind.REMOTE_METHOD_CALL_ACTION) {
                RemoteMethodCallActionNode remoteMethodCallActionNode = (RemoteMethodCallActionNode) node;
                if (semanticModel != null) {
                    Optional<Symbol> expressionSymbol = this.semanticModel.symbol(
                            remoteMethodCallActionNode.expression());
                    if (expressionSymbol.isPresent() && expressionSymbol.get() instanceof VariableSymbol) {
                        VariableSymbol variableSymbol = (VariableSymbol) expressionSymbol.get();
                        markVisibleEp(variableSymbol, symbolJson, remoteMethodCallActionNode.expression(), true);
                    }
                }
            }

            nodeJson.add("typeData", symbolJson);
            if (node.kind() == SyntaxKind.FUNCTION_BODY_BLOCK
                    && (this.visibleEpsForEachBlock.size() > 0 || this.visibleEpsForModule.size() > 0)
                    && nodeJson.get("typeData") != null) {
                JsonArray eps = new JsonArray();
                this.visibleEpsForEachBlock.forEach(eps::add);
                this.visibleEpsForModule.forEach(eps::add);
                nodeJson.add("VisibleEndpoints", eps);
                this.visibleEpsForEachBlock = new ArrayList<>();
            }
        }

        return nodeJson;
    }

    private void markVisibleEp(VariableSymbol variableSymbol, JsonObject symbolJson, Node node) {
        TypeSymbol rawType = getRawType(variableSymbol.typeDescriptor());
        if (rawType.typeKind() == TypeDescKind.OBJECT) {
            ObjectTypeSymbol objectTypeSymbol = (ObjectTypeSymbol) rawType;
            boolean isEndpoint = objectTypeSymbol.qualifiers()
                    .contains(Qualifier.CLIENT);
            if (isEndpoint) {
                symbolJson.addProperty("isEndpoint", true);
                JsonObject ep = visibleEP(node, rawType, false);
                if (ep.size() > 0) {
                    this.visibleEpsForEachBlock.add(ep);
                }
            }
        }
    }

    private void markVisibleEp(VariableSymbol variableSymbol, JsonObject symbolJson, Node node,
                               boolean isRemoteAction) {
        TypeSymbol rawType = getRawType(variableSymbol.typeDescriptor());
        if (rawType.typeKind() == TypeDescKind.OBJECT) {
            ObjectTypeSymbol objectTypeSymbol = (ObjectTypeSymbol) rawType;
            boolean isEndpoint = objectTypeSymbol.qualifiers()
                    .contains(Qualifier.CLIENT);
            if (isEndpoint) {
                symbolJson.addProperty("isEndpoint", true);
                JsonObject ep = visibleEP(node, rawType, isRemoteAction);
                if (ep.size() > 0) {
                    this.visibleEpsForEachBlock.add(ep);
                }
            }
        }
    }

    private JsonObject visibleEP(Node node, TypeSymbol typeSymbol, boolean isRemoteAction) {
        JsonObject symbolMetaInfo = new JsonObject();
        ModuleID moduleID = typeSymbol.getModule().isPresent() ? typeSymbol.getModule().get().id() : null;
        String orgName = moduleID != null ? moduleID.orgName() : "";
        String moduleName = moduleID != null ? moduleID.moduleName() : "";

        if (node.kind() == SyntaxKind.REQUIRED_PARAM) {
            RequiredParameterNode requiredParameterNode = (RequiredParameterNode) node;
            Optional<Token> paramName = requiredParameterNode.paramName();
            symbolMetaInfo.addProperty("name", paramName.isPresent() ? paramName.get().text() : "");
            symbolMetaInfo.addProperty("isCaller", "Caller".equals(typeSymbol.getName().orElse(null)));
            symbolMetaInfo.addProperty("typeName", typeSymbol.getName().orElse(""));
            symbolMetaInfo.addProperty("orgName", orgName);
            symbolMetaInfo.addProperty("moduleName", moduleName);
            symbolMetaInfo.addProperty("isModuleVar", false);
        } else if (node.kind() == SyntaxKind.LOCAL_VAR_DECL) {
            VariableDeclarationNode variableDeclarationNode = (VariableDeclarationNode) node;
            CaptureBindingPatternNode captureBindingPatternNode =
                    (CaptureBindingPatternNode) variableDeclarationNode.typedBindingPattern().bindingPattern();
            symbolMetaInfo.addProperty("name", captureBindingPatternNode.variableName().text());
            symbolMetaInfo.addProperty("isCaller", "Caller".equals(typeSymbol.getName().orElse(null)));
            symbolMetaInfo.addProperty("typeName", typeSymbol.getName().orElse(""));
            symbolMetaInfo.addProperty("orgName", orgName);
            symbolMetaInfo.addProperty("moduleName", moduleName);
            symbolMetaInfo.addProperty("isModuleVar", false);
        } else if (node.kind() == SyntaxKind.ASSIGNMENT_STATEMENT) {
            AssignmentStatementNode assignmentStatementNode = (AssignmentStatementNode) node;
            if (assignmentStatementNode.varRef() instanceof SimpleNameReferenceNode) {
                SimpleNameReferenceNode simpleNameReferenceNode =
                        (SimpleNameReferenceNode) assignmentStatementNode.varRef();
                symbolMetaInfo.addProperty("name", simpleNameReferenceNode.name().text());
                symbolMetaInfo.addProperty("isCaller", "Caller".equals(typeSymbol.getName()
                        .orElse(null)));
                symbolMetaInfo.addProperty("typeName", typeSymbol.getName().orElse(""));
                symbolMetaInfo.addProperty("orgName", orgName);
                symbolMetaInfo.addProperty("moduleName", moduleName);
                symbolMetaInfo.addProperty("isModuleVar", false);
            }
        } else if (node.kind() == SyntaxKind.MODULE_VAR_DECL) {
            JsonObject metaInfoForModuleVar = new JsonObject();
            ModuleVariableDeclarationNode variableDeclarationNode = (ModuleVariableDeclarationNode) node;
            CaptureBindingPatternNode captureBindingPatternNode =
                    (CaptureBindingPatternNode) variableDeclarationNode.typedBindingPattern().bindingPattern();
            metaInfoForModuleVar.addProperty("name", captureBindingPatternNode.variableName().text());
            metaInfoForModuleVar.addProperty("isCaller", "Caller".equals(typeSymbol.getName()
                    .orElse(null)));
            metaInfoForModuleVar.addProperty("typeName", typeSymbol.getName().orElse(""));
            metaInfoForModuleVar.addProperty("orgName", orgName);
            metaInfoForModuleVar.addProperty("moduleName", moduleName);
            metaInfoForModuleVar.addProperty("isModuleVar", true);
            metaInfoForModuleVar.addProperty("isExternal", true);

            this.visibleEpsForModule.add(metaInfoForModuleVar);
        } else if (node.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE && isRemoteAction) {
            String name = ((SimpleNameReferenceNode) node).name().text();
            JsonObject metaInfoForModuleVar = new JsonObject();
            metaInfoForModuleVar.addProperty("name", name);
            boolean isAvailable = isAvailableAsEndpoint(name);

            if (!isAvailable) {
                metaInfoForModuleVar.addProperty("isCaller", "Caller".equals(typeSymbol.getName()
                        .orElse(null)));
                metaInfoForModuleVar.addProperty("typeName", typeSymbol.getName().orElse(""));
                metaInfoForModuleVar.addProperty("orgName", orgName);
                metaInfoForModuleVar.addProperty("moduleName", moduleName);
                metaInfoForModuleVar.addProperty("isExternal", true);

                this.visibleEpsForModule.add(metaInfoForModuleVar);
            }
        }

        return symbolMetaInfo;
    }

    private boolean isAvailableAsEndpoint(String name) {
        for (JsonObject ep : this.visibleEpsForEachBlock) {
            if (ep.get("name").getAsString().equals(name)) {
                return true;
            }
        }

        for (JsonObject ep : this.visibleEpsForModule) {
            if (ep.get("name").getAsString().equals(name)) {
                return true;
            }
        }

        return false;
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
                if (!jsonName.equals("typeDescriptor")) {
                    nodeJson.add(jsonName, generateTypeJson((Symbol) prop));
                }
                // TODO: verify if this is needed and enable (need to add to the nodeJson as well)
//            } else if (prop instanceof List) {
//                List listProp = (List) prop;
//                JsonArray listPropJson = new JsonArray();
//                for (Object listPropItem : listProp) {
//                    if (listPropItem instanceof Symbol) {
//                        listPropJson.add(generateTypeJson((Symbol) listPropItem));
//                    } else if (listPropItem instanceof String) {
//                        listPropJson.add((String) listPropItem);
//                    } else if (listPropItem instanceof Boolean) {
//                        listPropJson.add((Boolean) listPropItem);
//                    }
//                }
            } else if (prop instanceof Optional &&
                    ((Optional<?>) prop).isPresent() && ((Optional) prop).get() instanceof ModuleSymbol) {
                ModuleID ballerinaModuleID = ((ModuleSymbol) ((Optional) prop).get()).id();
                JsonObject moduleIdJson = new JsonObject();
                moduleIdJson.addProperty("orgName", ballerinaModuleID.orgName());
                moduleIdJson.addProperty("moduleName", ballerinaModuleID.moduleName());
                moduleIdJson.addProperty("version", ballerinaModuleID.version());
                nodeJson.add("moduleID", moduleIdJson);
            } else if (prop instanceof ModuleID) {
                ModuleID ballerinaModuleID = (ModuleID) prop;
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
            nodeInfo.addProperty("isMissing", node.isMissing());
            if (node.lineRange() != null) {
                LineRange lineRange = node.lineRange();
                LinePosition startLine = lineRange.startLine();
                LinePosition endLine = lineRange.endLine();
                JsonObject position = new JsonObject();
                position.addProperty("startLine", startLine.line());
                position.addProperty("startColumn", startLine.offset());
                position.addProperty("endLine", endLine.line());
                position.addProperty("endColumn", endLine.offset());
                nodeInfo.add("position", position);
            }
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

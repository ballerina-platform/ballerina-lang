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
import io.ballerina.compiler.syntax.tree.Minutiae;
import io.ballerina.compiler.syntax.tree.MinutiaeList;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeTransformer;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.ObjectFieldNode;
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
        if (syntaxDiagnostics != null) {
            nodeJson.add("syntaxDiagnostics", SyntaxTreeDiagnosticsUtil.getDiagnostics(syntaxDiagnostics));
        }
        nodeJson.add("leadingMinutiae", evaluateMinutiae(node.leadingMinutiae()));
        nodeJson.add("trailingMinutiae", evaluateMinutiae(node.trailingMinutiae()));

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
                                updateVisibleEP(node, typeSymbol.get(), false);
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

            if ((node.kind() == SyntaxKind.BLOCK_STATEMENT || node.kind() == SyntaxKind.FUNCTION_BODY_BLOCK ||
                    node.kind() == SyntaxKind.SERVICE_DECLARATION) && (this.visibleEpsForEachBlock.size() > 0 ||
                    this.visibleEpsForModule.size() > 0)) {

                JsonArray blockEndpoints = new JsonArray();
                // Add module level endpoints
                this.visibleEpsForModule.forEach(blockEndpoints::add);

                for (JsonObject endpoint : this.visibleEpsForEachBlock) {
                    int epStartLine = endpoint.get("position").getAsJsonObject().get("startLine").getAsInt();
                    int epEndLine = endpoint.get("position").getAsJsonObject().get("endLine").getAsInt();

                    Optional<Node> parentFunctionBlock = getParentBlock(node);
                    if (parentFunctionBlock.isPresent()
                            && epStartLine >= parentFunctionBlock.get().lineRange().startLine().line()
                            && epEndLine < node.lineRange().startLine().line()
                            && !endpoint.has("innerBlock")) {
                        blockEndpoints.add(endpoint);
                    }

                    if (epStartLine >= node.lineRange().startLine().line()
                            && epEndLine < node.lineRange().endLine().line()
                            && !endpoint.has("innerBlock")) {
                        blockEndpoints.add(endpoint);
                        // Add key to filter endpoints only visible to a block
                        endpoint.addProperty("innerBlock", true);
                    }
                }
                nodeJson.add("VisibleEndpoints", blockEndpoints);
            }
        }

        return nodeJson;
    }

    protected Optional<Node> getParentBlock(Node node) {
        try {
            if (node.kind() == SyntaxKind.FUNCTION_DEFINITION ||
                    node.kind() == SyntaxKind.SERVICE_DECLARATION) {
                return Optional.of(node);
            }
            return getParentBlock(node.parent());
        } catch (NullPointerException ex) {
            return Optional.empty();
        }
    }

    private void markVisibleEp(VariableSymbol variableSymbol, JsonObject symbolJson, Node node) {
        TypeSymbol rawType = getRawType(variableSymbol.typeDescriptor());
        if (rawType.typeKind() == TypeDescKind.OBJECT) {
            ObjectTypeSymbol objectTypeSymbol = (ObjectTypeSymbol) rawType;
            boolean isEndpoint = objectTypeSymbol.qualifiers()
                    .contains(Qualifier.CLIENT);
            if (isEndpoint) {
                symbolJson.addProperty("isEndpoint", true);
                updateVisibleEP(node, rawType, false);
            }
        }
        if (rawType.typeKind() == TypeDescKind.UNION) {
            UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) rawType;
            unionTypeSymbol.memberTypeDescriptors().forEach(member -> {
                TypeSymbol memberRawType = getRawType(member);
                if (memberRawType.typeKind() == TypeDescKind.OBJECT
                        && ((ObjectTypeSymbol) memberRawType).qualifiers().contains(Qualifier.CLIENT)) {
                    symbolJson.addProperty("isEndpoint", true);
                    updateVisibleEP(node, memberRawType, false);
                }
            });
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
                updateVisibleEP(node, rawType, isRemoteAction);
            }
        }
    }

    private JsonObject updateVisibleEP(Node node, TypeSymbol typeSymbol, boolean isRemoteAction) {
        JsonObject symbolMetaInfo = new JsonObject();

        switch (node.kind()) {
            case REQUIRED_PARAM:
                RequiredParameterNode requiredParameterNode = (RequiredParameterNode) node;
                Optional<Token> paramName = requiredParameterNode.paramName();
                String symbolName = paramName.isPresent() ? paramName.get().text() : "";
                symbolMetaInfo = getModuleMetaInfo(typeSymbol, symbolName, requiredParameterNode.lineRange(),
                        false, true);
                symbolMetaInfo.addProperty("isParameter", true);
                if (!isAvailableAsEndpoint(symbolName)) {
                    this.visibleEpsForEachBlock.add(symbolMetaInfo);
                }
                break;
            case MODULE_VAR_DECL:
                ModuleVariableDeclarationNode moduleVariableDeclarationNode = (ModuleVariableDeclarationNode) node;
                if (moduleVariableDeclarationNode.typedBindingPattern().bindingPattern().kind() ==
                        SyntaxKind.CAPTURE_BINDING_PATTERN) {
                    String moduleVarName = ((CaptureBindingPatternNode) moduleVariableDeclarationNode
                            .typedBindingPattern().bindingPattern()).variableName().text();
                    if (!isAvailableAsEndpoint(moduleVarName)) {
                        this.visibleEpsForModule.add(getModuleMetaInfo(typeSymbol, moduleVarName,
                                moduleVariableDeclarationNode.lineRange(), true, true));
                    }
                }
                break;
            case OBJECT_FIELD:
                ObjectFieldNode objectFieldNode = (ObjectFieldNode) node;
                String fieldName = objectFieldNode.fieldName().text();
                symbolMetaInfo = getModuleMetaInfo(typeSymbol, fieldName, objectFieldNode.lineRange(),
                        false, true);
                symbolMetaInfo.addProperty("isClassField", true);
                if (!isAvailableAsEndpoint(fieldName)) {
                    this.visibleEpsForEachBlock.add(symbolMetaInfo);
                }
                break;
            case LOCAL_VAR_DECL:
                VariableDeclarationNode variableDeclarationNode = (VariableDeclarationNode) node;
                if (variableDeclarationNode.typedBindingPattern().bindingPattern().kind() ==
                        SyntaxKind.CAPTURE_BINDING_PATTERN) {
                    String localVarName = ((CaptureBindingPatternNode) variableDeclarationNode
                            .typedBindingPattern().bindingPattern()).variableName().text();
                    symbolMetaInfo = getModuleMetaInfo(typeSymbol, localVarName, variableDeclarationNode.lineRange(),
                            false, false);
                    if (!isAvailableAsEndpoint(localVarName)) {
                        this.visibleEpsForEachBlock.add(symbolMetaInfo);
                    }
                }
                break;
            case ASSIGNMENT_STATEMENT:
                AssignmentStatementNode assignmentStatementNode = (AssignmentStatementNode) node;
                if (assignmentStatementNode.varRef().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
                    String asgmtVarName = ((SimpleNameReferenceNode) assignmentStatementNode.varRef()).name().text();
                    symbolMetaInfo = getModuleMetaInfo(typeSymbol, asgmtVarName, assignmentStatementNode.lineRange(),
                            false, false);
                    if (!isAvailableAsEndpoint(asgmtVarName)) {
                        this.visibleEpsForEachBlock.add(symbolMetaInfo);
                    }
                }
                break;
            case SIMPLE_NAME_REFERENCE:
                String name = ((SimpleNameReferenceNode) node).name().text();
                if (isRemoteAction && !isAvailableAsEndpoint(name)) {
                    this.visibleEpsForModule.add(getModuleMetaInfo(typeSymbol, name, node.lineRange(),
                            false, true));
                }
                break;
            default:
                return symbolMetaInfo;
        }

        return symbolMetaInfo;
    }

    private JsonObject getModuleMetaInfo(TypeSymbol typeSymbol, String name, LineRange lineRange,
                                         Boolean isModuleVar, Boolean isExternal) {
        JsonObject metaInfo = new JsonObject();
        JsonObject position = new JsonObject();

        if (!typeSymbol.getModule().isPresent()) {
            return metaInfo;
        }

        ModuleID moduleID = typeSymbol.getModule().get().id();

        metaInfo.addProperty("name", name);
        metaInfo.addProperty("isCaller", "Caller".equals(typeSymbol.getName().orElse("")));
        metaInfo.addProperty("typeName", typeSymbol.getName().orElse(""));
        metaInfo.addProperty("orgName", moduleID.orgName());
        metaInfo.addProperty("packageName", moduleID.packageName());
        metaInfo.addProperty("moduleName", moduleID.moduleName());
        metaInfo.addProperty("version", moduleID.version());
        metaInfo.addProperty("isModuleVar", isModuleVar);
        metaInfo.addProperty("isExternal", isExternal);

        position.addProperty("startLine", lineRange.startLine().line());
        position.addProperty("endLine", lineRange.endLine().line());
        metaInfo.add("position", position);

        return metaInfo;
    }

    private boolean isAvailableAsEndpoint(String name) {
        for (JsonObject ep : this.visibleEpsForEachBlock) {
            if (ep.get("name").getAsString().equals(name) && ep.get("innerBlock") == null) {
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
                moduleIdJson.addProperty("packageName", ballerinaModuleID.packageName());
                moduleIdJson.addProperty("moduleName", ballerinaModuleID.moduleName());
                moduleIdJson.addProperty("version", ballerinaModuleID.version());
                nodeJson.add("moduleID", moduleIdJson);
            } else if (prop instanceof ModuleID) {
                ModuleID ballerinaModuleID = (ModuleID) prop;
                JsonObject moduleIdJson = new JsonObject();
                moduleIdJson.addProperty("orgName", ballerinaModuleID.orgName());
                moduleIdJson.addProperty("packageName", ballerinaModuleID.packageName());
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
        nodeInfo.add("leadingMinutiae", evaluateMinutiae(node.leadingMinutiae()));
        nodeInfo.add("trailingMinutiae", evaluateMinutiae(node.trailingMinutiae()));
        return nodeInfo;
    }

    private String prettifyKind(String kind) {
        return Arrays.stream(kind.split("_"))
                .map(String::toLowerCase)
                .map(StringUtils::capitalize)
                .collect(Collectors.joining());
    }

    private JsonArray evaluateMinutiae(MinutiaeList minutiaeList) {
        JsonArray nodeMinutiae = new JsonArray();
        for (Minutiae minutiae : minutiaeList) {
            JsonObject minutiaeJson = new JsonObject();
            minutiaeJson.addProperty("kind", minutiae.kind().toString());
            minutiaeJson.addProperty("minutiae", minutiae.text());
            minutiaeJson.addProperty("isInvalid", minutiae.isInvalidNodeMinutiae());
            nodeMinutiae.add(minutiaeJson);
        }

        return nodeMinutiae;
    }
}

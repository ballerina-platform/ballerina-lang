/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.util.documentsymbol;

import io.ballerina.compiler.syntax.tree.ChildNodeList;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.MetadataNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Module;
import org.ballerinalang.langserver.commons.DocumentSymbolContext;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.SymbolTag;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Finds Symbols within a document.
 *
 * @since 2.0.0
 */
public class SymbolUtil {

    public static final Set<SyntaxKind> SUPPORTED_NODE_TYPES = Set.of(SyntaxKind.MODULE_PART,
            SyntaxKind.FUNCTION_DEFINITION, SyntaxKind.METHOD_DECLARATION, SyntaxKind.SERVICE_DECLARATION,
            SyntaxKind.CLASS_DEFINITION, SyntaxKind.RESOURCE_ACCESSOR_DEFINITION, SyntaxKind.OBJECT_METHOD_DEFINITION);

    /**
     * get document symbols given the context.
     *
     * @param context document symbol context.
     * @return {@link List<Either<SymbolInformation, DocumentSymbol>>} generated document symbols.
     */
    public static List<Either<SymbolInformation, DocumentSymbol>> documentSymbols(DocumentSymbolContext context) {
        List<Either<SymbolInformation, DocumentSymbol>> symbols = new ArrayList<>();
        Optional<SyntaxTree> syntaxTree = context.currentSyntaxTree();
        if (syntaxTree.isEmpty()) {
            return symbols;
        }
        if (context.getHierarchicalDocumentSymbolSupport()) {
            Optional<DocumentSymbol> rootSymbol = getDocumentSymbolForNode(syntaxTree.get().rootNode(),
                    context, Collections.emptyList());
            rootSymbol.ifPresent(documentSymbol -> symbols.add(Either.forRight(documentSymbol)));
        } else {
            List<DocumentSymbol> documentSymbolStore = new ArrayList<>();
            getDocumentSymbolForNode(syntaxTree.get().rootNode(), context, documentSymbolStore);
            documentSymbolStore.forEach(symbol -> symbols.add(Either.forRight(symbol)));
        }
        return symbols;
    }

    /**
     * Generate the document symbol for a node given the context.
     *
     * @param node                Syntax tree node.
     * @param context             Document symbol context.
     * @param documentSymbolStore Document symbols holder to be used in the absence of hierarchical support.
     * @return {@link Optional<DocumentSymbol>} document symbol corresponding to the node.
     */
    public static Optional<DocumentSymbol> getDocumentSymbolForNode(Node node,
                                                                    DocumentSymbolContext context,
                                                                    List<DocumentSymbol> documentSymbolStore) {
        if (!SUPPORTED_NODE_TYPES.contains(node.kind())) {
            return Optional.empty();
        }
        Range nodeRange = generateNodeRange(node);
        boolean isDeprecated = false;
        SymbolKind symbolKind;
        String name = "";
        List<DocumentSymbol> children = Collections.emptyList();
        String detail = null;
        switch (node.kind()) {
            case MODULE_PART:
                ModulePartNode rootNode = (ModulePartNode) node;
                Path filePath = context.filePath();
                Optional<Module> module = context.workspace().module(filePath);
                if (module.isPresent()) {
                    if (module.get().isDefaultModule()) {
                        name = "Main";
                    } else {
                        name = module.get().moduleName().moduleNamePart();
                    }
                } else {
                    name = "Module";
                }
                children = getDocumentSymbolsOfChildren(rootNode.children(), context, documentSymbolStore);
                symbolKind = SymbolKind.Module;
                break;
            case OBJECT_METHOD_DEFINITION:
                FunctionDefinitionNode methodDef = (FunctionDefinitionNode) node;
                Optional<MetadataNode> methodMetadata = methodDef.metadata();
                if (methodMetadata.isPresent()) {
                    isDeprecated = methodMetadata.get().annotations().stream().filter(annotation ->
                            annotation.annotReference().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE
                                    && ((SimpleNameReferenceNode) annotation.annotReference()).name().text()
                                    .equals("deprecated")).collect(Collectors.toList()).size() > 0;
                }
                name = methodDef.functionName().text();
                symbolKind = SymbolKind.Method;
                break;
            case FUNCTION_DEFINITION:
                FunctionDefinitionNode funcDef = (FunctionDefinitionNode) node;
                Optional<MetadataNode> metadata = funcDef.metadata();
                if (metadata.isPresent()) {
                    isDeprecated = metadata.get().annotations().stream().filter(annotation ->
                            annotation.annotReference().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE
                                    && ((SimpleNameReferenceNode) annotation.annotReference()).name().text()
                                    .equals("deprecated")).collect(Collectors.toList()).size() > 0;
                }
                name = funcDef.functionName().text();
                symbolKind = SymbolKind.Function;
                break;
            case RESOURCE_ACCESSOR_DEFINITION:
                FunctionDefinitionNode functionDef = (FunctionDefinitionNode) node;
                StringBuilder resourceFuncName = new StringBuilder(functionDef.functionName().text());
                for (Node child : functionDef.children()) {
                    if (child.kind() == SyntaxKind.IDENTIFIER_TOKEN &&
                            !((IdentifierToken) child).text().equals(functionDef.functionName().text())) {
                        resourceFuncName.append(":").append(((IdentifierToken) child).text());
                        break;
                    }
                }
                name = resourceFuncName.toString();
                symbolKind = SymbolKind.Function;
                Optional<MetadataNode> resourceMetadata = functionDef.metadata();
                if (resourceMetadata.isPresent()) {
                    isDeprecated = resourceMetadata.get().annotations().stream().filter(annotation ->
                            annotation.annotReference().kind() == SyntaxKind.SIMPLE_NAME_REFERENCE
                                    && ((SimpleNameReferenceNode) annotation.annotReference()).name().text()
                                    .equals("deprecated")).collect(Collectors.toList()).size() > 0;
                }
                break;
            case CLASS_DEFINITION:
                ClassDefinitionNode classDef = (ClassDefinitionNode) node;
                name = classDef.className().text();
                symbolKind = SymbolKind.Class;
                children = getDocumentSymbolsOfChildren(classDef.children(), context, documentSymbolStore);
                break;
            case SERVICE_DECLARATION:
                ServiceDeclarationNode serviceDeclarationNode = (ServiceDeclarationNode) node;
                name = "service " + serviceDeclarationNode.absoluteResourcePath().stream()
                        .map(Node::toString).collect(Collectors.joining(""))
                        + " on " + serviceDeclarationNode.expressions().stream()
                        .map(Node::toString).collect(Collectors.joining(""));
                symbolKind = SymbolKind.Interface;
                children = getDocumentSymbolsOfChildren(serviceDeclarationNode.children(),
                        context, documentSymbolStore);
                break;
            default:
                return Optional.empty();
        }
        if (name == null || name.isEmpty()) {
            return Optional.empty();
        }
        if (!context.getHierarchicalDocumentSymbolSupport()) {
            documentSymbolStore.add(createDocumentSymbol(name, symbolKind, detail, nodeRange, nodeRange,
                    isDeprecated, children, context));
            documentSymbolStore.addAll(children);
        }
        return Optional.of(createDocumentSymbol(name, symbolKind, detail, nodeRange, nodeRange,
                isDeprecated, children, context));
    }

    /**
     * Provided a Child nodes list generate the corresponding document symbols.
     *
     * @param nodes               {@link ChildNodeList} Child nodes list.
     * @param context             document symbol context.
     * @param documentSymbolStore document symbol holder.
     * @return {@link List<DocumentSymbol>} Generated list of document symbols.
     */
    private static List<DocumentSymbol> getDocumentSymbolsOfChildren(ChildNodeList nodes,
                                                                     DocumentSymbolContext context,
                                                                     List<DocumentSymbol> documentSymbolStore) {
        List<DocumentSymbol> childSymbols = new ArrayList<>();
        nodes.forEach(node -> {
            if (SUPPORTED_NODE_TYPES.contains(node.kind())) {
                Optional<DocumentSymbol> docSymbol = getDocumentSymbolForNode(node, context, documentSymbolStore);
                docSymbol.ifPresent(childSymbols::add);
            }
        });
        return childSymbols;
    }

    /**
     * Document symbol builder.
     *
     * @param name           symbol name.
     * @param kind           symbol kind.
     * @param detail         symbol detail.
     * @param range          Range of the symbol.
     * @param selectionRange selection range of the symbol.
     * @param isDeprecated   Whether the symbol is deprecated.
     * @param children       Child document symbols.
     * @param context        Document symbol context.
     * @return
     */
    private static DocumentSymbol createDocumentSymbol(String name, SymbolKind kind,
                                                       String detail, Range range,
                                                       Range selectionRange, boolean isDeprecated,
                                                       List<DocumentSymbol> children, DocumentSymbolContext context) {
        DocumentSymbol documentSymbol = new DocumentSymbol();
        documentSymbol.setName(name);
        documentSymbol.setKind(kind);
        documentSymbol.setDetail(detail);
        documentSymbol.setRange(range);
        documentSymbol.setSelectionRange(selectionRange);
        if (context.getHierarchicalDocumentSymbolSupport()) {
            documentSymbol.setChildren(children);
        }
        if (isDeprecated && context.supportedTags().isPresent() &&
                context.supportedTags().get().getValueSet().contains(SymbolTag.Deprecated)) {
            documentSymbol.setTags(List.of(SymbolTag.Deprecated));
        }
        return documentSymbol;
    }

    /**
     * Generate the node range provided the node.
     *
     * @param node Syntax tree node.
     * @return {@link Range} node range.
     */
    private static Range generateNodeRange(Node node) {
        Position startPosition = new Position(node.lineRange().startLine().line(),
                node.lineRange().startLine().offset());
        Position endPosition = new Position(node.lineRange().endLine().line(),
                node.lineRange().endLine().offset());
        return new Range(startPosition, endPosition);
    }
}

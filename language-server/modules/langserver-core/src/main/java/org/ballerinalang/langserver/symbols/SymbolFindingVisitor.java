/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langserver.symbols;

import org.ballerinalang.langserver.common.LSNodeVisitor;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSServiceOperationContext;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;

import java.util.List;

/**
 * A visitor for creating a flat list of symbols found in a compilation unit.
 */
public class SymbolFindingVisitor extends LSNodeVisitor {
    private List<SymbolInformation> symbols;
    private String uri = "";
    private String query = "";

    public SymbolFindingVisitor(LSServiceOperationContext documentServiceContext) {
        this.symbols = documentServiceContext.get(DocumentServiceKeys.SYMBOL_LIST_KEY);
        this.uri = documentServiceContext.get(DocumentServiceKeys.FILE_URI_KEY);
        this.query = documentServiceContext.get(DocumentServiceKeys.SYMBOL_QUERY);
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
        compUnit.getTopLevelNodes().forEach(node -> ((BLangNode) node).accept(this));
    }

    @Override
    public void visit(BLangFunction funcNode) {
        SymbolKind symbolKind = SymbolKind.Function;

        if (UtilSymbolKeys.NEW_KEYWORD_KEY.equals(funcNode.name.value) || funcNode.getBody() == null) {
            return;
        }
        this.addSymbol(funcNode, funcNode.symbol, symbolKind);
        if (!funcNode.getWorkers().isEmpty()) {
            funcNode.getWorkers().forEach(bLangWorker -> bLangWorker.accept(this));
            return;
        }
        funcNode.getBody().accept(this);
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        this.addSymbol(typeDefinition, typeDefinition.symbol, SymbolKind.Class);
        typeDefinition.typeNode.accept(this);
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        if (objectTypeNode.initFunction != null) {
            this.visit(objectTypeNode.initFunction);
        }
        objectTypeNode.fields.forEach(field -> this.addSymbol(field, field.symbol, SymbolKind.Field));
        objectTypeNode.functions.forEach(this::visit);
    }

    @Override
    public void visit(BLangService serviceNode) {
        this.addSymbol(serviceNode, serviceNode.symbol, SymbolKind.Class);
        serviceNode.getResources().forEach(bLangResource -> bLangResource.accept(this));
    }

    @Override
    public void visit(BLangSimpleVariable variableNode) {
        SymbolKind kind;
        String btype = variableNode.getTypeNode().toString();
        switch (btype) {
            case "boolean":
                kind = SymbolKind.Boolean;
                break;

            case "int":
                kind = SymbolKind.Number;
                break;

            case "float":
                kind = SymbolKind.Number;
                break;

            case "string":
                kind = SymbolKind.String;
                break;

            case "package":
                kind = SymbolKind.Package;
                break;

            default:
                if (btype.endsWith("[]")) {
                    kind = SymbolKind.Array;
                } else {
                    kind = SymbolKind.Variable;
                }
        }
        this.addSymbol(variableNode, variableNode.symbol, kind);
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        blockNode.stmts.forEach(stmt -> stmt.accept(this));
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        varDefNode.getVariable().accept(this);
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        this.addSymbol(xmlnsNode, xmlnsNode.symbol, SymbolKind.Namespace);
    }

    @Override
    public void visit(BLangResource resourceNode) {
        this.addSymbol(resourceNode, resourceNode.symbol, SymbolKind.Function);
        resourceNode.body.accept(this);
    }

    @Override
    public void visit(BLangWorker workerNode) {
        this.addSymbol(workerNode, workerNode.symbol, SymbolKind.Class);
        workerNode.body.accept(this);
    }

    @Override
    public void visit(BLangEndpoint endpointNode) {
        this.addSymbol(endpointNode, endpointNode.symbol, SymbolKind.Variable);
    }

    private void addSymbol(BLangNode node, BSymbol balSymbol, SymbolKind kind) {
        String symbolName = balSymbol.getName().getValue();
        if (query != null && !query.isEmpty() && !symbolName.startsWith(query)) {
            return;
        }
        SymbolInformation lspSymbol = new SymbolInformation();
        lspSymbol.setName(symbolName);
        lspSymbol.setKind(kind);
        lspSymbol.setLocation(new Location(this.uri, getRange(node)));
        this.symbols.add(lspSymbol);
    }

    private Range getRange(BLangNode node) {
        Range r = new Range();

        int startLine = node.getPosition().getStartLine() - 1; // LSP range is 0 based
        int startChar = node.getPosition().getStartColumn() - 1;
        int endLine = node.getPosition().getEndLine() - 1;
        int endChar = node.getPosition().getEndColumn() - 1;

        if (endLine <= 0) {
            endLine = startLine;
        }

        if (endChar <= 0) {
            endChar = startChar + 1;
        }

        r.setStart(new Position(startLine, startChar));
        r.setEnd(new Position(endLine, endChar));

        return r;
    }
}

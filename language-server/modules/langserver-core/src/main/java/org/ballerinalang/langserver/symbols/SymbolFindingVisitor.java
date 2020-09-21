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

import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.LSNodeVisitor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.eclipse.lsp4j.DocumentSymbol;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A visitor for creating a flat list of symbols found in a compilation unit.
 */
public class SymbolFindingVisitor extends LSNodeVisitor {
    private List<Either<SymbolInformation, DocumentSymbol>> symbols;
    private String uri;
    private String query;

    public SymbolFindingVisitor(LSContext documentServiceContext) {
        this.symbols = documentServiceContext.get(DocumentServiceKeys.SYMBOL_LIST_KEY);
        this.uri = documentServiceContext.get(DocumentServiceKeys.FILE_URI_KEY);
        this.query = documentServiceContext.get(DocumentServiceKeys.SYMBOL_QUERY);
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
        compUnit.getTopLevelNodes().stream()
                .filter(CommonUtil.checkInvalidTypesDefs())
                .forEach(node -> ((BLangNode) node).accept(this));
    }

    @Override
    public void visit(BLangFunction funcNode) {
        SymbolKind symbolKind = SymbolKind.Function;

        if (CommonKeys.NEW_KEYWORD_KEY.equals(funcNode.name.value) || !funcNode.hasBody()) {
            return;
        }
        this.addSymbol(funcNode, funcNode.symbol, symbolKind);
        if (!funcNode.getWorkers().isEmpty()) {
            funcNode.getWorkers().forEach(bLangWorker -> bLangWorker.accept(this));
            return;
        }
        funcNode.body.accept(this);
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        this.addSymbol(typeDefinition, typeDefinition.symbol, SymbolKind.Class);
        typeDefinition.typeNode.accept(this);
    }

    @Override
    public void visit(BLangConstant constant) {
        this.addSymbol(constant, constant.symbol, SymbolKind.Class);
        constant.typeNode.accept(this);
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
        List<BLangNode> serviceContent = new ArrayList<>();
        List<BLangFunction> serviceFunctions = serviceNode.serviceClass.functions;
        List<BLangSimpleVariable> serviceFields = serviceNode.serviceClass.fields.stream()
                .map(simpleVar -> (BLangSimpleVariable) simpleVar)
                .collect(Collectors.toList());
        serviceContent.addAll(serviceFunctions);
        serviceContent.addAll(serviceFields);
        serviceContent.sort(new CommonUtil.BLangNodeComparator());
        serviceContent.forEach(serviceField -> serviceField.accept(this));
    }

    @Override
    public void visit(BLangSimpleVariable variableNode) {
        SymbolKind kind;
        String btype = "";
        if (variableNode.getTypeNode() != null) {
            btype = variableNode.getTypeNode().toString();
        } else if (variableNode.symbol != null) {
            btype = variableNode.symbol.type.toString();
        }
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
    public void visit(BLangBlockFunctionBody blockFuncBody) {
        blockFuncBody.stmts.forEach(stmt -> stmt.accept(this));
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        if (varDefNode.getVariable().getName().getValue().startsWith("0")) {
            return;
        }
        varDefNode.getVariable().accept(this);
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        this.addSymbol(xmlnsNode, xmlnsNode.symbol, SymbolKind.Namespace);
    }

    @Override
    public void visit(BLangWorker workerNode) {
        this.addSymbol(workerNode, workerNode.symbol, SymbolKind.Class);
        workerNode.body.accept(this);
    }

    private void addSymbol(BLangNode node, BSymbol balSymbol, SymbolKind kind) {
        List<String> symbolNameComponents = Arrays.asList(balSymbol.getName().getValue().split("\\."));
        String symbolName = CommonUtil.getLastItem(symbolNameComponents);
        if ((query != null && !query.isEmpty() && !symbolName.startsWith(query))
                || CommonUtil.isInvalidSymbol(balSymbol)) {
            return;
        }
        SymbolInformation lspSymbol = new SymbolInformation();
        lspSymbol.setName(symbolName);
        lspSymbol.setKind(kind);
        lspSymbol.setLocation(new Location(this.uri, getRange(node)));
        this.symbols.add(Either.forLeft(lspSymbol));
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

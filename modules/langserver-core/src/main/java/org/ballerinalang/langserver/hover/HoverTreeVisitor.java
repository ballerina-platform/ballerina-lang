/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.langserver.hover;

import org.ballerinalang.langserver.DocumentServiceKeys;
import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.hover.util.HoverUtil;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangComment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangNext;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Tree visitor for the hover functionality.
 * */
public class HoverTreeVisitor extends BLangNodeVisitor {

    private String fileName;
    private Position position;
    private boolean terminateVisitor = false;
    private SymbolEnter symbolEnter;
    private TextDocumentServiceContext context;
    private Object previousNode;

    public HoverTreeVisitor(TextDocumentServiceContext context) {
        this.context = context;
        this.position = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        this.fileName = context.get(DocumentServiceKeys.FILE_NAME_KEY);
        this.symbolEnter = SymbolEnter.getInstance(context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY));
        this.position.setLine(this.position.getLine() + 1);
    }

    // Visitor methods

    public void visit(BLangPackage pkgNode) {
        // Then visit each top-level element sorted using the compilation unit
        List<TopLevelNode> topLevelNodes = pkgNode.topLevelNodes.stream().filter(node ->
                node.getPosition().getSource().getCompilationUnitName().equals(this.fileName)
        ).collect(Collectors.toList());

        if (topLevelNodes.isEmpty()) {
            terminateVisitor = true;
            acceptNode(null);
        } else {
            topLevelNodes.forEach(topLevelNode -> acceptNode((BLangNode) topLevelNode));
        }
    }

    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgSymbol);
        acceptNode(pkgEnv.node);
    }

    public void visit(BLangXMLNS xmlnsNode) {
        throw new AssertionError();
    }

    public void visit(BLangFunction funcNode) {
        // Check for native functions
        BSymbol funcSymbol = funcNode.symbol;
        if (Symbols.isNative(funcSymbol)) {
            return;
        }
        previousNode = funcNode;
        this.acceptNode(funcNode.body);

        // Process workers
        if (terminateVisitor && !funcNode.workers.isEmpty()) {
            terminateVisitor = false;
        }
        funcNode.workers.forEach(e -> this.acceptNode(e));
    }

    public void visit(BLangStruct structNode) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangVariable varNode) {
    }

    @Override
    public void visit(BLangLiteral litNode) {
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        // TODO: implement support for hover.
    }

    // Statements

    @Override
    public void visit(BLangBlockStmt blockNode) {
        if (blockNode.stmts.isEmpty()) {

        } else {
            blockNode.stmts.forEach(stmt -> this.acceptNode(stmt));
        }
    }

    @Override
    public void visit(BLangVariableDef varDefNode) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        if (assignNode.expr != null && assignNode.getPosition().sLine <= this.position.getLine()
                && assignNode.getPosition().eLine >= this.position.getLine()) {
            this.acceptNode(assignNode.expr);
        }
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        if (exprStmtNode.getPosition().sLine <= this.position.getLine()
                && exprStmtNode.getPosition().eLine >= this.position.getLine()) {
            this.acceptNode(exprStmtNode.expr);
        }
    }

    @Override
    public void visit(BLangIf ifNode) {
        // TODO: implement support for hover.
    }

    public void visit(BLangWhile whileNode) {
        // TODO: implement support for hover.
    }

    public void visit(BLangTransformer transformerNode) {
        // TODO: implement support for hover.
    }

    public void visit(BLangConnector connectorNode) {
        // TODO: implement support for hover.
    }

    public void visit(BLangAction actionNode) {
        // TODO: implement support for hover.
    }

    public void visit(BLangService serviceNode) {
        // TODO: implement support for hover.
    }

    public void visit(BLangResource resourceNode) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangTryCatchFinally tryCatchFinally) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangCatch bLangCatch) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangAbort abortNode) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangRetry retryNode) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangWorker workerNode) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangReturn returnNode) {
        // TODO: implement support for hover.
    }

    public void visit(BLangNext nextNode) {
        // TODO: implement support for hover.
    }

    @Override
    public void visit(BLangComment comment) {
        // TODO: implement support for hover.
    }

    public void visit(BLangInvocation invocationExpr) {
        if (HoverUtil.isMatchingPosition(invocationExpr.getPosition(), this.position)) {
            this.context.put(HoverKeys.HOVERING_OVER_NODE_KEY, invocationExpr);
            this.context.put(HoverKeys.PREVIOUSLY_VISITED_NODE_KEY, this.previousNode);
            this.context.put(HoverKeys.NAME_OF_HOVER_NODE_KEY, invocationExpr.name);
            this.context.put(HoverKeys.PACKAGE_OF_HOVER_NODE_KEY, invocationExpr.symbol.pkgID);
            this.context.put(HoverKeys.SYMBOL_KIND_OF_HOVER_NODE_KEY, invocationExpr.symbol.kind);
        }
    }

    private void acceptNode(BLangNode node) {
        if (this.terminateVisitor) {
            return;
        }
        node.accept(this);
    }
}

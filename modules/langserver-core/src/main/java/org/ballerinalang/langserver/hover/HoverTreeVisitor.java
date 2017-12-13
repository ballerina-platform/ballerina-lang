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
import org.ballerinalang.model.tree.statements.StatementNode;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.tree.*;
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
        SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);

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
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {

    }

    @Override
    public void visit(BLangVariable varNode) {

    }

    @Override
    public void visit(BLangLiteral litNode) {

    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {

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

    }

    @Override
    public void visit(BLangAssignment assignNode) {
        if(assignNode.expr != null) {
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
        int b = 0;
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


    public void visit(BLangWhile whileNode) {

    }

    public void visit(BLangTransformer transformerNode) {

    }

    public void visit(BLangConnector connectorNode) {

    }

    public void visit(BLangAction actionNode) {

    }

    public void visit(BLangService serviceNode) {

    }

    public void visit(BLangResource resourceNode) {

    }

    @Override
    public void visit(BLangTryCatchFinally tryCatchFinally) {

    }

    @Override
    public void visit(BLangCatch bLangCatch) {

    }

    @Override
    public void visit(BLangTransaction transactionNode) {

    }

    @Override
    public void visit(BLangAbort abortNode) {

    }

    @Override
    public void visit(BLangRetry retryNode) {

    }

    private BLangVariableDef createVarDef(BLangVariable var) {
        BLangVariableDef varDefNode = new BLangVariableDef();
        varDefNode.var = var;
        varDefNode.pos = var.pos;
        return varDefNode;
    }

    private BLangBlockStmt generateCodeBlock(StatementNode... statements) {
        BLangBlockStmt block = new BLangBlockStmt();
        for (StatementNode stmt : statements) {
            block.addStatement(stmt);
        }
        return block;
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {

    }

    @Override
    public void visit(BLangWorker workerNode) {

    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {

    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {

    }

    @Override
    public void visit(BLangReturn returnNode) {

    }

    public void visit(BLangNext nextNode) {

    }

    @Override
    public void visit(BLangComment comment) {

    }

    private void acceptNode(BLangNode node) {

        if (this.terminateVisitor) {
            return;
        }
        node.accept(this);
    }
}

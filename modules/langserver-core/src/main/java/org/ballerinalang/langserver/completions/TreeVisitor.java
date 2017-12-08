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

package org.ballerinalang.langserver.completions;

import org.ballerinalang.langserver.completions.util.positioning.resolvers.BlockStatementScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.CursorPositionResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.PackageNodeScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ResourceParamScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ServiceScopeResolver;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
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
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * @since 0.94
 */
public class TreeVisitor extends BLangNodeVisitor {
    private SymbolEnv symbolEnv;
    private SymbolResolver symbolResolver;
    private boolean terminateVisitor = false;
    private TextDocumentPositionParams positionParams;
    private SuggestionsFilterDataModel filterDataModel;
    private SymbolEnter symbolEnter;
    private Stack<Node> blockOwnerStack;
    private Stack<BLangBlockStmt> blockStmtStack;
    private Map<Class, CursorPositionResolver> cursorPositionResolvers;
    private Class cursorPositionResolver;

    public TreeVisitor(TextDocumentPositionParams positionParams, SuggestionsFilterDataModel filterDataModel) {
        this.positionParams = positionParams;
        this.filterDataModel = filterDataModel;

        init(this.filterDataModel.getCompilerContext());
    }

    private void init(CompilerContext compilerContext) {
        BlockStatementScopeResolver blockStatementScopeResolver = new BlockStatementScopeResolver();
        ResourceParamScopeResolver resourceParamScopeResolver = new ResourceParamScopeResolver();
        PackageNodeScopeResolver packageNodeScopeResolver = new PackageNodeScopeResolver();
        ServiceScopeResolver serviceScopeResolver = new ServiceScopeResolver();

        blockOwnerStack = new Stack<>();
        blockStmtStack = new Stack<>();
        cursorPositionResolvers = new HashMap<>();
        symbolEnter = SymbolEnter.getInstance(compilerContext);
        symbolResolver = SymbolResolver.getInstance(compilerContext);
        filterDataModel.setSymbolTable(SymbolTable.getInstance(compilerContext));

        cursorPositionResolvers.put(BlockStatementScopeResolver.class, blockStatementScopeResolver);
        cursorPositionResolvers.put(ResourceParamScopeResolver.class, resourceParamScopeResolver);
        cursorPositionResolvers.put(PackageNodeScopeResolver.class, packageNodeScopeResolver);
        cursorPositionResolvers.put(ServiceScopeResolver.class, serviceScopeResolver);
    }

    // Visitor methods

    public void visit(BLangPackage pkgNode) {
        SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgNode.symbol);

        // Then visit each top-level element sorted using the compilation unit
        List<TopLevelNode> topLevelNodes = pkgNode.topLevelNodes.stream().filter(node ->
                    node.getPosition().getSource().getCompilationUnitName().equals(filterDataModel.getFileName())
            ).collect(Collectors.toList());

        if (topLevelNodes.isEmpty()) {
            terminateVisitor = true;
            acceptNode(null, null);
        } else {
            cursorPositionResolver = PackageNodeScopeResolver.class;
            topLevelNodes.forEach(topLevelNode -> acceptNode((BLangNode) topLevelNode, pkgEnv));
        }
    }

    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = symbolEnter.packageEnvs.get(pkgSymbol);
        acceptNode(pkgEnv.node, pkgEnv);
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
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcSymbol.scope, symbolEnv);

        this.blockOwnerStack.push(funcNode);
        // Cursor position is calculated against the Block statement scope resolver
        cursorPositionResolver = BlockStatementScopeResolver.class;
        this.acceptNode(funcNode.body, funcEnv);
        this.blockOwnerStack.pop();

        // Process workers
        if (terminateVisitor && !funcNode.workers.isEmpty()) {
            terminateVisitor = false;
        }
        funcNode.workers.forEach(e -> this.symbolEnter.defineNode(e, funcEnv));
        funcNode.workers.forEach(e -> this.acceptNode(e, funcEnv));
    }

    public void visit(BLangStruct structNode) {
        BSymbol structSymbol = structNode.symbol;
        SymbolEnv structEnv = SymbolEnv.createPkgLevelSymbolEnv(structNode, structSymbol.scope, symbolEnv);

        if (structNode.fields.isEmpty()) {
            symbolEnv = structEnv;
            Map<Name, Scope.ScopeEntry> visibleSymbolEntries = this.resolveAllVisibleSymbols(symbolEnv);
            this.populateSymbols(visibleSymbolEntries, null);
            this.terminateVisitor = true;
        } else {
            // Since the struct definition do not have a block statement within, we push null
            this.blockStmtStack.push(null);
            this.blockOwnerStack.push(structNode);
            // Cursor position is calculated against the Block statement scope resolver
            this.cursorPositionResolver = BlockStatementScopeResolver.class;
            structNode.fields.forEach(field -> acceptNode(field, structEnv));
            this.blockStmtStack.pop();
            this.blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
    }

    @Override
    public void visit(BLangVariable varNode) {
        cursorPositionResolvers.get(cursorPositionResolver)
                .isCursorBeforeStatement(varNode.getPosition(), varNode, this);
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
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, symbolEnv);
        this.blockStmtStack.push(blockNode);
        // Cursor position is calculated against the Block statement scope resolver
        this.cursorPositionResolver = BlockStatementScopeResolver.class;
        if (blockNode.stmts.isEmpty()) {
            this.isCursorWithinBlock((DiagnosticPos) (this.blockOwnerStack.peek()).getPosition(), blockNode, blockEnv);
        } else {
            blockNode.stmts.forEach(stmt -> this.acceptNode(stmt, blockEnv));
        }
        this.blockStmtStack.pop();
    }

    @Override
    public void visit(BLangVariableDef varDefNode) {
        if (!cursorPositionResolvers.get(cursorPositionResolver)
                .isCursorBeforeStatement(varDefNode.getPosition(), varDefNode, this)) {
            this.acceptNode(varDefNode.var, symbolEnv);
        }
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        cursorPositionResolvers.get(cursorPositionResolver)
                .isCursorBeforeStatement(assignNode.getPosition(), assignNode, this);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        cursorPositionResolvers.get(cursorPositionResolver)
                .isCursorBeforeStatement(exprStmtNode.getPosition(), exprStmtNode, this);
    }

    @Override
    public void visit(BLangIf ifNode) {
        if (!cursorPositionResolvers.get(cursorPositionResolver)
                .isCursorBeforeStatement(ifNode.getPosition(), ifNode, this)) {
            this.blockOwnerStack.push(ifNode);
            this.acceptNode(ifNode.body, symbolEnv);
            this.blockOwnerStack.pop();

            if (ifNode.elseStmt != null) {
                if (!(ifNode.elseStmt instanceof BLangIf)) {
                    this.blockOwnerStack.push(ifNode.elseStmt);
                }
                acceptNode(ifNode.elseStmt, symbolEnv);
                if (!(ifNode.elseStmt instanceof BLangIf)) {
                    this.blockOwnerStack.pop();
                }
            }
        }
    }

    public void visit(BLangWhile whileNode) {
        if (!cursorPositionResolvers.get(cursorPositionResolver)
                .isCursorBeforeStatement(whileNode.getPosition(), whileNode, this)) {

            this.blockOwnerStack.push(whileNode);
            this.acceptNode(whileNode.body, symbolEnv);
            this.blockOwnerStack.pop();
        }
    }

    public void visit(BLangTransformer transformerNode) {
        this.blockOwnerStack.push(transformerNode);
        // Cursor position is calculated against the Block statement scope resolver
        cursorPositionResolver = BlockStatementScopeResolver.class;
        this.acceptNode(transformerNode.body, symbolEnv);
        this.blockOwnerStack.pop();
    }

    public void visit(BLangConnector connectorNode) {
        BSymbol connectorSymbol = connectorNode.symbol;
        SymbolEnv connectorEnv = SymbolEnv.createConnectorEnv(connectorNode, connectorSymbol.scope, symbolEnv);

        // TODO: Handle Annotation attachments

        // Cursor position is calculated against the resource parameter scope resolver
        cursorPositionResolver = ResourceParamScopeResolver.class;
        connectorNode.params.forEach(param -> this.acceptNode(param, connectorEnv));
        connectorNode.varDefs.forEach(varDef -> this.acceptNode(varDef, connectorEnv));
        connectorNode.actions.forEach(action -> this.acceptNode(action, connectorEnv));
    }

    public void visit(BLangAction actionNode) {
        BSymbol actionSymbol = actionNode.symbol;

        SymbolEnv actionEnv = SymbolEnv.createResourceActionSymbolEnv(actionNode, actionSymbol.scope, symbolEnv);

        // TODO: Handle Annotation attachments
        // Cursor position is calculated against the resource parameter scope resolver
        cursorPositionResolver = ResourceParamScopeResolver.class;
        actionNode.params.forEach(p -> this.acceptNode(p, actionEnv));
        this.blockOwnerStack.push(actionNode);
        acceptNode(actionNode.body, actionEnv);
        this.blockOwnerStack.pop();
    }

    public void visit(BLangService serviceNode) {
        BSymbol serviceSymbol = serviceNode.symbol;
        SymbolEnv serviceEnv = SymbolEnv.createPkgLevelSymbolEnv(serviceNode, serviceSymbol.scope, symbolEnv);
        this.cursorPositionResolver = ServiceScopeResolver.class;
        // Since the service does not contains a block statement, we consider the block owner only. Here it is service
        this.blockOwnerStack.push(serviceNode);
        serviceNode.vars.forEach(v -> this.acceptNode(v, serviceEnv));
        serviceNode.resources.forEach(r -> this.acceptNode(r, serviceEnv));
        if (terminateVisitor) {
            this.acceptNode(null, null);
        }
        this.blockOwnerStack.pop();
    }

    public void visit(BLangResource resourceNode) {
        if (!cursorPositionResolvers.get(cursorPositionResolver)
                .isCursorBeforeStatement(resourceNode.getPosition(), resourceNode, this)) {
            BSymbol resourceSymbol = resourceNode.symbol;
            SymbolEnv resourceEnv = SymbolEnv.createResourceActionSymbolEnv(resourceNode,
                    resourceSymbol.scope, symbolEnv);

            // TODO:Handle Annotation attachments

            // Cursor position is calculated against the resource parameter scope resolver
            cursorPositionResolver = ResourceParamScopeResolver.class;
            resourceNode.params.forEach(p -> this.acceptNode(p, resourceEnv));
            resourceNode.workers.forEach(w -> this.acceptNode(w, resourceEnv));
            this.blockOwnerStack.push(resourceNode);
            // Cursor position is calculated against the Block statement scope resolver
            cursorPositionResolver = BlockStatementScopeResolver.class;
            acceptNode(resourceNode.body, resourceEnv);
            this.blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangTryCatchFinally tryCatchFinally) {
        if (!cursorPositionResolvers.get(cursorPositionResolver)
                .isCursorBeforeStatement(tryCatchFinally.getPosition(), tryCatchFinally, this)) {

            this.blockOwnerStack.push(tryCatchFinally);
            this.acceptNode(tryCatchFinally.tryBody, symbolEnv);
            this.blockOwnerStack.pop();

            tryCatchFinally.catchBlocks.forEach(c -> {
                this.blockOwnerStack.push(c);
                this.acceptNode(c, symbolEnv);
                this.blockOwnerStack.pop();
            });
            if (tryCatchFinally.finallyBody != null) {
                // Check how we can add the blang node to stack
                this.blockOwnerStack.push(tryCatchFinally);
                this.acceptNode(tryCatchFinally.finallyBody, symbolEnv);
                this.blockOwnerStack.pop();
            }
        }
    }

    @Override
    public void visit(BLangCatch bLangCatch) {
        if (!cursorPositionResolvers.get(cursorPositionResolver)
                .isCursorBeforeStatement(bLangCatch.getPosition(), bLangCatch, this)) {
            SymbolEnv catchBlockEnv = SymbolEnv.createBlockEnv(bLangCatch.body, symbolEnv);
            this.acceptNode(bLangCatch.param, catchBlockEnv);

            this.blockOwnerStack.push(bLangCatch);
            this.acceptNode(bLangCatch.body, catchBlockEnv);
            this.blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        this.blockOwnerStack.push(transactionNode);
        this.acceptNode(transactionNode.transactionBody, symbolEnv);
        this.blockOwnerStack.pop();

        if (transactionNode.failedBody != null) {
            this.blockOwnerStack.push(transactionNode);
            this.acceptNode(transactionNode.failedBody, symbolEnv);
            this.blockOwnerStack.pop();
        }

        if (transactionNode.committedBody != null) {
            this.blockOwnerStack.push(transactionNode);
            this.acceptNode(transactionNode.committedBody, symbolEnv);
            this.blockOwnerStack.pop();
        }

        if (transactionNode.abortedBody != null) {
            this.blockOwnerStack.push(transactionNode);
            this.acceptNode(transactionNode.abortedBody, symbolEnv);
            this.blockOwnerStack.pop();
        }
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
        SymbolEnv folkJoinEnv = SymbolEnv.createFolkJoinEnv(forkJoin, this.symbolEnv);
        // TODO: check the symbolEnter.defineNode
        forkJoin.workers.forEach(e -> this.symbolEnter.defineNode(e, folkJoinEnv));
        forkJoin.workers.forEach(e -> this.acceptNode(e, folkJoinEnv));
//        if (!this.isJoinResultType(forkJoin.joinResultVar)) {
//            this.dlog.error(forkJoin.joinResultVar.pos, DiagnosticCode.INVALID_WORKER_JOIN_RESULT_TYPE);
//        }
        /* create code black and environment for join result section, i.e. (map results) */
        BLangBlockStmt joinResultsBlock = this.generateCodeBlock(this.createVarDef(forkJoin.joinResultVar));
        SymbolEnv joinResultsEnv = SymbolEnv.createBlockEnv(joinResultsBlock, this.symbolEnv);
        this.acceptNode(joinResultsBlock, joinResultsEnv);
        /* create an environment for the join body, making the enclosing environment the earlier
         * join result's environment */
        SymbolEnv joinBodyEnv = SymbolEnv.createBlockEnv(forkJoin.joinedBody, joinResultsEnv);
        this.acceptNode(forkJoin.joinedBody, joinBodyEnv);

        if (forkJoin.timeoutExpression != null) {
            /* create code black and environment for timeout section */
            BLangBlockStmt timeoutVarBlock = this.generateCodeBlock(this.createVarDef(forkJoin.timeoutVariable));
            SymbolEnv timeoutVarEnv = SymbolEnv.createBlockEnv(timeoutVarBlock, this.symbolEnv);
            this.acceptNode(timeoutVarBlock, timeoutVarEnv);
            /* create an environment for the timeout body, making the enclosing environment the earlier
             * timeout var's environment */
            SymbolEnv timeoutBodyEnv = SymbolEnv.createBlockEnv(forkJoin.timeoutBody, timeoutVarEnv);
            this.acceptNode(forkJoin.timeoutBody, timeoutBodyEnv);
        }
    }

    @Override
    public void visit(BLangWorker workerNode) {
        SymbolEnv workerEnv = SymbolEnv.createWorkerEnv(workerNode, this.symbolEnv);
        this.blockOwnerStack.push(workerNode);
        this.acceptNode(workerNode.body, workerEnv);
        this.blockOwnerStack.pop();
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        cursorPositionResolvers.get(cursorPositionResolver)
                .isCursorBeforeStatement(workerSendNode.getPosition(), workerSendNode, this);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        cursorPositionResolvers.get(cursorPositionResolver)
                .isCursorBeforeStatement(workerReceiveNode.getPosition(), workerReceiveNode, this);
    }

    @Override
    public void visit(BLangReturn returnNode) {
        cursorPositionResolvers.get(cursorPositionResolver)
                .isCursorBeforeStatement(returnNode.getPosition(), returnNode, this);
    }

    public void visit(BLangNext nextNode) {
        cursorPositionResolvers.get(cursorPositionResolver)
                .isCursorBeforeStatement(nextNode.getPosition(), nextNode, this);
    }

    @Override
    public void visit(BLangComment comment) {
        cursorPositionResolvers.get(cursorPositionResolver)
                .isCursorBeforeStatement(comment.getPosition(), comment, this);
    }

    // Private methods

    /**
     * Resolve all visible symbols.
     * @param symbolEnv symbol environment
     * @return all visible symbols for current scope
     */
    public Map<Name, Scope.ScopeEntry> resolveAllVisibleSymbols(SymbolEnv symbolEnv) {
        return symbolResolver.getAllVisibleInScopeSymbols(symbolEnv);
    }

    /**
     * Populate the symbols.
     * @param symbolEntries symbol entries
     */
    public void populateSymbols(Map<Name, Scope.ScopeEntry> symbolEntries, SymbolEnv symbolEnv) {
        if (symbolEnv != null) {
            this.filterDataModel.setSymbolEnvNode(symbolEnv.node);
        } else {
            this.filterDataModel.setSymbolEnvNode(this.symbolEnv.node);
        }

        symbolEntries.forEach((k, v) -> {
            SymbolInfo symbolInfo = new SymbolInfo(k.getValue(), v);
            this.filterDataModel.getVisibleSymbols().add(symbolInfo);
        });
    }

    private void acceptNode(BLangNode node, SymbolEnv env) {

        if (this.terminateVisitor) {
            return;
        }

        SymbolEnv prevEnv = this.symbolEnv;
        this.symbolEnv = env;
        node.accept(this);
        this.symbolEnv = prevEnv;
    }

    private boolean isCursorWithinBlock(DiagnosticPos nodePosition, Node node, SymbolEnv symbolEnv) {
        int line = positionParams.getPosition().getLine();
        int nodeSLine = nodePosition.sLine;
        int nodeELine = nodePosition.eLine;

        if ((nodeSLine <= line && nodeELine >= line)) {
            Map<Name, Scope.ScopeEntry> visibleSymbolEntries = this.resolveAllVisibleSymbols(symbolEnv);
            this.populateSymbols(visibleSymbolEntries, symbolEnv);
            this.terminateVisitor = true;
            return true;
        }

        return false;
    }

    public TextDocumentPositionParams getTextDocumentPositionParams() {
        return positionParams;
    }

    public Stack<Node> getBlockOwnerStack() {
        return blockOwnerStack;
    }

    public Stack<BLangBlockStmt> getBlockStmtStack() {
        return blockStmtStack;
    }

    public SymbolEnv getSymbolEnv() {
        return symbolEnv;
    }

    public void setTerminateVisitor(boolean terminateVisitor) {
        this.terminateVisitor = terminateVisitor;
    }
}

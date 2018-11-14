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

import org.ballerinalang.langserver.common.LSNodeVisitor;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.util.CompletionVisitorUtil;
import org.ballerinalang.langserver.completions.util.CursorPositionResolvers;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.BlockStatementScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.CursorPositionResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.FunctionNodeScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.InvocationParameterScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.MatchExpressionScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.MatchStatementScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ObjectTypeScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.RecordScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ResourceParamScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ServiceScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.TopLevelNodeScopeResolver;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.TopLevelNode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

/**
 * @since 0.94
 */
public class TreeVisitor extends LSNodeVisitor {

    private boolean terminateVisitor = false;

    private int loopCount = 0;

    private int transactionCount = 0;

    private SymbolEnv symbolEnv;

    private SymbolResolver symbolResolver;

    private SymbolTable symTable;

    private Deque<Node> blockOwnerStack;

    private Deque<BLangBlockStmt> blockStmtStack;

    private Deque<Boolean> isCurrentNodeTransactionStack;

    private Class cursorPositionResolver;

    private LSContext lsContext;

    private BLangNode previousNode = null;

    public TreeVisitor(LSContext documentServiceContext) {
        this.lsContext = documentServiceContext;
        init(this.lsContext.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY));
    }

    private void init(CompilerContext compilerContext) {
        blockOwnerStack = new ArrayDeque<>();
        blockStmtStack = new ArrayDeque<>();
        isCurrentNodeTransactionStack = new ArrayDeque<>();
        symTable = SymbolTable.getInstance(compilerContext);
        symbolResolver = SymbolResolver.getInstance(compilerContext);
        lsContext.put(DocumentServiceKeys.SYMBOL_TABLE_KEY, symTable);
    }

    ///////////////////////////////////
    /////      Visitor Methods    /////
    ///////////////////////////////////

    @Override
    public void visit(BLangPackage pkgNode) {
        boolean isTestSrc = CommonUtil.isTestSource(this.lsContext.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY));
        BLangPackage evalPkg = isTestSrc ? pkgNode.getTestablePkg() : pkgNode;
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(evalPkg.symbol);
        this.symbolEnv = pkgEnv;

        List<TopLevelNode> topLevelNodes = CommonUtil.getCurrentFileTopLevelNodes(evalPkg, lsContext);
        List<BLangImportPackage> imports = CommonUtil.getCurrentFileImports(evalPkg, lsContext);
        
        imports.forEach(bLangImportPackage -> {
            cursorPositionResolver = TopLevelNodeScopeResolver.class;
            this.blockOwnerStack.push(evalPkg);
            acceptNode(bLangImportPackage, pkgEnv);
        });

        topLevelNodes.forEach(topLevelNode -> {
            cursorPositionResolver = TopLevelNodeScopeResolver.class;
            this.blockOwnerStack.push(evalPkg);
            acceptNode((BLangNode) topLevelNode, pkgEnv);
        });

        // If the cursor is at an empty document's first line or is bellow the last construct, symbol env node is null
        if (this.lsContext.get(CompletionKeys.SYMBOL_ENV_NODE_KEY) == null) {
            this.lsContext.put(CompletionKeys.SYMBOL_ENV_NODE_KEY, evalPkg);
            this.populateSymbols(this.resolveAllVisibleSymbols(this.getSymbolEnv()), this.getSymbolEnv());
            forceTerminateVisitor();
        }
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
        CursorPositionResolvers
                .getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(importPkgNode.getPosition(), importPkgNode, this, lsContext);
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(xmlnsNode.getPosition(), xmlnsNode, this, this.lsContext);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        String functionName = funcNode.getName().getValue();
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, this.symbolEnv);
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(this.cursorPositionResolver);

        funcNode.annAttachments.forEach(annotationAttachment -> this.acceptNode(annotationAttachment, funcEnv));

        boolean withinParamContext = CompletionVisitorUtil
                .isWithinParameterContext(functionName, UtilSymbolKeys.FUNCTION_KEYWORD_KEY, funcEnv, lsContext, this);
        boolean cursorBeforeNode = cpr.isCursorBeforeNode(funcNode.getPosition(), funcNode, this, this.lsContext);

        if (terminateVisitor || cursorBeforeNode || withinParamContext) {
            return;
        }

        funcNode.endpoints.forEach(bLangEndpoint -> this.acceptNode(bLangEndpoint, funcEnv));

        if (!funcNode.getWorkers().isEmpty()) {
            funcNode.workers.forEach(e -> {
                this.blockOwnerStack.push(funcNode);
                this.cursorPositionResolver = FunctionNodeScopeResolver.class;
                this.acceptNode(e, funcEnv);
                this.blockOwnerStack.pop();
            });
        } else if (funcNode.getBody() != null) {
            this.blockOwnerStack.push(funcNode);
            this.cursorPositionResolver = BlockStatementScopeResolver.class;
            this.acceptNode(funcNode.body, funcEnv);
            this.blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (cpr.isCursorBeforeNode(typeDefinition.getPosition(), typeDefinition, this, this.lsContext)) {
            return;
        }
        this.acceptNode(typeDefinition.typeNode, symbolEnv);
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        BSymbol recordSymbol = recordTypeNode.symbol;
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        SymbolEnv recordEnv = SymbolEnv.createPkgLevelSymbolEnv(recordTypeNode, recordSymbol.scope, symbolEnv);

        // TODO: Since the position of the record type node is invalid, we pass the position of the type definition
        boolean cursorBeforeNode = cpr.isCursorBeforeNode(recordTypeNode.parent.getPosition(), recordTypeNode, this,
                this.lsContext);
        boolean cursorWithinBlock = recordTypeNode.fields.isEmpty() && 
                CompletionVisitorUtil.isCursorWithinBlock(recordTypeNode.parent.getPosition(),
                recordEnv, this.lsContext, this);

        if (recordSymbol.getName().getValue().contains(UtilSymbolKeys.DOLLAR_SYMBOL_KEY) || cursorBeforeNode
                || cursorWithinBlock) {
            return;
        }

        cursorPositionResolver = RecordScopeResolver.class;
        this.blockOwnerStack.push(recordTypeNode);
        recordTypeNode.fields.forEach(field -> acceptNode(field, recordEnv));
        cursorPositionResolver = TopLevelNodeScopeResolver.class;
        this.blockOwnerStack.pop();
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        BSymbol objectSymbol = objectTypeNode.symbol;
        SymbolEnv objectEnv = SymbolEnv.createPkgLevelSymbolEnv(objectTypeNode, objectSymbol.scope, symbolEnv);
        List<BLangNode> objectItems = CompletionVisitorUtil.getObjectItemsOrdered(objectTypeNode);

        // TODO: Currently consider the type definition's position since the object body's position is wrong
        // TODO: visit annotation and doc attachments of functions
        if (objectItems.isEmpty() && CompletionVisitorUtil
                .isCursorWithinBlock(objectTypeNode.parent.getPosition(), objectEnv, lsContext, this)) {
            return;
        }

        blockOwnerStack.push(objectTypeNode);
        objectItems.forEach(item -> {
            this.cursorPositionResolver = ObjectTypeScopeResolver.class;
            acceptNode(item, objectEnv);
        });

        blockOwnerStack.pop();
        this.cursorPositionResolver = TopLevelNodeScopeResolver.class;
    }

    @Override
    public void visit(BLangVariable varNode) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (cpr.isCursorBeforeNode(varNode.getPosition(), varNode, this, this.lsContext) || varNode.expr == null) {
            return;
        }

        // This is an endpoint definition
        this.acceptNode(varNode.expr, symbolEnv);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        binaryExpr.getLeftExpression().accept(this);
        binaryExpr.getRightExpression().accept(this);
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        bracedOrTupleExpr.getExpressions().forEach(bLangExpression -> this.acceptNode(bLangExpression, symbolEnv));
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        conversionExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, symbolEnv);
        // Reset the previous node to null
        this.setPreviousNode(null);

        if (blockNode.stmts.isEmpty() && CompletionVisitorUtil
                .isCursorWithinBlock((DiagnosticPos) (this.blockOwnerStack.peek()).getPosition(), blockEnv,
                        this.lsContext, this)) {
            return;
        }

        this.blockStmtStack.push(blockNode);
        this.cursorPositionResolver = BlockStatementScopeResolver.class;
        blockNode.stmts.forEach(stmt -> this.acceptNode(stmt, blockEnv));
        this.blockStmtStack.pop();
    }

    @Override
    public void visit(BLangVariableDef varDefNode) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (cpr.isCursorBeforeNode(varDefNode.getPosition(), varDefNode, this, this.lsContext)) {
            return;
        }

        this.acceptNode(varDefNode.var, symbolEnv);
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (cpr.isCursorBeforeNode(assignNode.getPosition(), assignNode, this, this.lsContext)) {
            return;
        }

        this.acceptNode(assignNode.expr, symbolEnv);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (cpr.isCursorBeforeNode(exprStmtNode.getPosition(), exprStmtNode, this, this.lsContext)
                || !(exprStmtNode.expr instanceof BLangInvocation)) {
            return;
        }

        this.acceptNode(exprStmtNode.expr, symbolEnv);
    }

    @Override
    public void visit(BLangInvocation invocationNode) {
        int curLine = lsContext.get(DocumentServiceKeys.POSITION_KEY).getPosition().getLine();
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        
        if (cpr.isCursorBeforeNode(invocationNode.getPosition(), invocationNode, this, this.lsContext)
                || curLine != invocationNode.getPosition().getStartLine() - 1) {
            return;
        }

        final TreeVisitor visitor = this;
        Class fallbackCursorPositionResolver = this.cursorPositionResolver;
        this.cursorPositionResolver = InvocationParameterScopeResolver.class;
        this.blockOwnerStack.push(invocationNode);
        // Visit all arguments
        invocationNode.getArgumentExpressions().forEach(expressionNode -> {
            BLangNode node = ((BLangNode) expressionNode);
            CursorPositionResolver posResolver = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
            posResolver.isCursorBeforeNode(node.getPosition(), node, visitor, visitor.lsContext);
            visitor.acceptNode(node, symbolEnv);
        });
        this.blockOwnerStack.pop();
        this.cursorPositionResolver = fallbackCursorPositionResolver;
    }

    @Override
    public void visit(BLangIf ifNode) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (cpr.isCursorBeforeNode(ifNode.getPosition(), ifNode, this, this.lsContext)) {
            return;
        }

        this.blockOwnerStack.push(ifNode);
        this.acceptNode(ifNode.body, symbolEnv);
        this.blockOwnerStack.pop();

        if (ifNode.elseStmt != null) {
            this.blockOwnerStack.push(ifNode.elseStmt);
            acceptNode(ifNode.elseStmt, symbolEnv);
            this.blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangWhile whileNode) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (cpr.isCursorBeforeNode(whileNode.getPosition(), whileNode, this, this.lsContext)) {
            return;
        }

        this.blockOwnerStack.push(whileNode);
        loopCount++;
        this.acceptNode(whileNode.body, symbolEnv);
        loopCount--;
        this.blockOwnerStack.pop();
    }

    @Override
    public void visit(BLangService serviceNode) {
        BSymbol serviceSymbol = serviceNode.symbol;
        SymbolEnv serviceEnv = SymbolEnv.createPkgLevelSymbolEnv(serviceNode, serviceSymbol.scope, symbolEnv);
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);

        serviceNode.annAttachments.forEach(annotationAttachment -> this.acceptNode(annotationAttachment, serviceEnv));
        // Reset the previous node
        this.setPreviousNode(null);
        boolean cursorWithinBlock = serviceNode.resources.isEmpty()
                && serviceNode.vars.isEmpty()
                && serviceNode.endpoints.isEmpty()
                && CompletionVisitorUtil.isCursorWithinBlock(serviceNode.getPosition(), serviceEnv, this.lsContext,
                this);

        if (cpr.isCursorBeforeNode(serviceNode.getPosition(), serviceNode, this, this.lsContext)
                || (serviceNode.resources.isEmpty() && serviceNode.vars.isEmpty() && serviceNode.endpoints.isEmpty()
                && cursorWithinBlock)) {
            return;
        }

        this.blockOwnerStack.push(serviceNode);

        serviceNode.endpoints.forEach(bLangEndpoint -> this.acceptNode(bLangEndpoint, serviceEnv));
        serviceNode.vars.forEach(v -> {
            this.cursorPositionResolver = ServiceScopeResolver.class;
            this.acceptNode(v, serviceEnv);
        });
        serviceNode.resources.forEach(r -> {
            this.cursorPositionResolver = ServiceScopeResolver.class;
            this.acceptNode(r, serviceEnv);
        });

        this.blockOwnerStack.pop();
    }

    @Override
    public void visit(BLangResource resourceNode) {
        BSymbol resourceSymbol = resourceNode.symbol;
        String resourceName = resourceNode.getName().getValue();
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        SymbolEnv resourceEnv = SymbolEnv.createResourceActionSymbolEnv(resourceNode, resourceSymbol.scope, symbolEnv);

        resourceNode.annAttachments.forEach(annotationAttachment -> this.acceptNode(annotationAttachment, resourceEnv));

        boolean cursorAtResourceIdentifier = CompletionVisitorUtil.isCursorAtResourceIdentifier(resourceNode,
                this.lsContext, this);
        boolean withinParamContext = CompletionVisitorUtil.isWithinParameterContext(resourceName,
                UtilSymbolKeys.RESOURCE_KEYWORD_KEY, resourceEnv, this.lsContext, this);
        boolean cursorBeforeNode = cpr.isCursorBeforeNode(resourceNode.getPosition(), resourceNode, this,
                this.lsContext);

        if (terminateVisitor || cursorAtResourceIdentifier || withinParamContext || cursorBeforeNode) {
            return;
        }

        resourceNode.endpoints.forEach(bLangEndpoint -> this.acceptNode(bLangEndpoint, resourceEnv));

        cursorPositionResolver = ResourceParamScopeResolver.class;
        resourceNode.workers.forEach(w -> {
            this.blockOwnerStack.push(resourceNode);
            this.cursorPositionResolver = FunctionNodeScopeResolver.class;
            this.acceptNode(w, resourceEnv);
            this.blockOwnerStack.pop();
        });

        this.blockOwnerStack.push(resourceNode);
        cursorPositionResolver = BlockStatementScopeResolver.class;
        acceptNode(resourceNode.body, resourceEnv);
        this.blockOwnerStack.pop();
    }

    @Override
    public void visit(BLangTryCatchFinally tryCatchFinally) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (cpr.isCursorBeforeNode(tryCatchFinally.getPosition(), tryCatchFinally, this, this.lsContext)) {
            return;
        }
        
        this.blockOwnerStack.push(tryCatchFinally);
        this.acceptNode(tryCatchFinally.tryBody, symbolEnv);
        this.blockOwnerStack.pop();

        tryCatchFinally.catchBlocks.forEach(c -> {
            this.blockOwnerStack.push(c);
            this.acceptNode(c, symbolEnv);
            this.blockOwnerStack.pop();
        });

        if (tryCatchFinally.finallyBody != null) {
            this.blockOwnerStack.push(tryCatchFinally);
            this.acceptNode(tryCatchFinally.finallyBody, symbolEnv);
            this.blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangCatch bLangCatch) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (cpr.isCursorBeforeNode(bLangCatch.getPosition(), bLangCatch, this, this.lsContext)) {
            return;
        }
        
        SymbolEnv catchBlockEnv = SymbolEnv.createBlockEnv(bLangCatch.body, symbolEnv);
        this.acceptNode(bLangCatch.param, catchBlockEnv);

        this.blockOwnerStack.push(bLangCatch);
        this.acceptNode(bLangCatch.body, catchBlockEnv);
        this.blockOwnerStack.pop();
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        this.blockOwnerStack.push(transactionNode);
        this.isCurrentNodeTransactionStack.push(true);
        this.transactionCount++;
        this.acceptNode(transactionNode.transactionBody, symbolEnv);
        this.blockOwnerStack.pop();
        this.isCurrentNodeTransactionStack.pop();
        this.transactionCount--;

        if (transactionNode.onRetryBody != null) {
            this.blockOwnerStack.push(transactionNode);
            this.acceptNode(transactionNode.onRetryBody, symbolEnv);
            this.blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangAbort abortNode) {
        CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(abortNode.getPosition(), abortNode, this, this.lsContext);
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        SymbolEnv folkJoinEnv = SymbolEnv.createFolkJoinEnv(forkJoin, this.symbolEnv);
        forkJoin.workers.forEach(e -> this.acceptNode(e, folkJoinEnv));

        /* create code block and environment for join result section, i.e. (map results) */
        BLangVariableDef variableDef = CompletionVisitorUtil.createVarDef(forkJoin.joinResultVar);
        BLangBlockStmt joinResultsBlock = CompletionVisitorUtil.generateCodeBlock(variableDef);
        SymbolEnv joinResultsEnv = SymbolEnv.createBlockEnv(joinResultsBlock, this.symbolEnv);
        this.acceptNode(joinResultsBlock, joinResultsEnv);
        /* create an environment for the join body, making the enclosing environment the earlier
         * join result's environment */
        SymbolEnv joinBodyEnv = SymbolEnv.createBlockEnv(forkJoin.joinedBody, joinResultsEnv);
        this.acceptNode(forkJoin.joinedBody, joinBodyEnv);

        if (forkJoin.timeoutExpression != null) {
            /* create code black and environment for timeout section */
            variableDef = CompletionVisitorUtil.createVarDef(forkJoin.timeoutVariable);
            BLangBlockStmt timeoutVarBlock = CompletionVisitorUtil.generateCodeBlock(variableDef);
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
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (cpr.isCursorBeforeNode(workerNode.getPosition(), workerNode, this, this.lsContext)) {
            return;
        }

        SymbolEnv workerEnv = SymbolEnv.createWorkerEnv(workerNode, this.symbolEnv);
        this.blockOwnerStack.push(workerNode);
        this.acceptNode(workerNode.body, workerEnv);
        this.blockOwnerStack.pop();
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(workerSendNode.getPosition(), workerSendNode, this, this.lsContext);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(workerReceiveNode.getPosition(), workerReceiveNode, this, this.lsContext);
    }

    @Override
    public void visit(BLangReturn returnNode) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (cpr.isCursorBeforeNode(returnNode.getPosition(), returnNode, this, this.lsContext)) {
            return;
        }

        this.acceptNode(returnNode.expr, symbolEnv);
    }

    @Override
    public void visit(BLangContinue continueNode) {
        CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(continueNode.getPosition(), continueNode, this, this.lsContext);
    }

    @Override
    public void visit(BLangBreak breakNode) {
        CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(breakNode.getPosition(), breakNode, this, this.lsContext);
    }

    @Override
    public void visit(BLangThrow throwNode) {
        CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(throwNode.getPosition(), throwNode, this, this.lsContext);
    }

    @Override
    public void visit(BLangLock lockNode) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (cpr.isCursorBeforeNode(lockNode.getPosition(), lockNode, this, this.lsContext)) {
            return;
        }

        this.blockOwnerStack.push(lockNode);
        this.acceptNode(lockNode.body, symbolEnv);
        this.blockOwnerStack.pop();
    }

    @Override
    public void visit(BLangForeach foreach) {
        if (!CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(foreach.getPosition(), foreach, this, this.lsContext)) {
            this.blockOwnerStack.push(foreach);
            loopCount++;
            this.acceptNode(foreach.body, symbolEnv);
            loopCount--;
            this.blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangEndpoint endpointNode) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        SymbolEnv epEnv = SymbolEnv.createPkgLevelSymbolEnv(endpointNode, symbolEnv.scope, symbolEnv);

        endpointNode.annAttachments.forEach(annotationAttachment -> this.acceptNode(annotationAttachment, epEnv));
        
        if (cpr.isCursorBeforeNode(endpointNode.getPosition(), endpointNode, this, this.lsContext)) {
            return;
        }

        CompletionVisitorUtil.isCursorWithinBlock(endpointNode.getPosition(), epEnv, this.lsContext, this);
    }

    @Override
    public void visit(BLangMatch matchNode) {
        if (!CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(matchNode.getPosition(), matchNode, this, this.lsContext)) {
            this.blockOwnerStack.push(matchNode);
            matchNode.getPatternClauses().forEach(patternClause -> {
                cursorPositionResolver = MatchStatementScopeResolver.class;
                acceptNode(patternClause, symbolEnv);
            });
            this.blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangMatch.BLangMatchStmtPatternClause patternClause) {
        if (!CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(patternClause.getPosition(), patternClause, this, this.lsContext)) {
            blockOwnerStack.push(patternClause);
            // If the variable is not equal to '_', then define the variable in the block scope
            if (!patternClause.variable.name.value.endsWith(Names.IGNORE.value)) {
                SymbolEnv blockEnv = SymbolEnv.createBlockEnv(patternClause.body, symbolEnv);
                cursorPositionResolver = BlockStatementScopeResolver.class;
                acceptNode(patternClause.body, blockEnv);
                blockOwnerStack.pop();
                return;
            }
            // TODO: Check with the semantic analyzer implementation as well.
            acceptNode(patternClause.body, symbolEnv);
            blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        SymbolEnv annotationAttachmentEnv = new SymbolEnv(annAttachmentNode, symbolEnv.scope);
        PackageID packageID = annAttachmentNode.annotationSymbol.pkgID;
        if (packageID.getOrgName().getValue().equals("ballerina") && packageID.getName().getValue().equals("grpc")
                && annAttachmentNode.annotationName.getValue().equals("ServiceDescriptor")) {
            return;
        }
        CompletionVisitorUtil.isCursorWithinBlock(annAttachmentNode.getPosition(), annotationAttachmentEnv,
                this.lsContext, this);
    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {
        if (!CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(bLangMatchExpression.getPosition(), bLangMatchExpression, this,
                        this.lsContext)) {
            SymbolEnv matchExprEnv = new SymbolEnv(bLangMatchExpression, symbolEnv.scope);
            final TreeVisitor visitor = this;
            Class fallbackCursorPositionResolver = this.cursorPositionResolver;
            this.cursorPositionResolver = MatchExpressionScopeResolver.class;
            this.blockOwnerStack.push(bLangMatchExpression);
            // Visit all pattern clauses
            if (bLangMatchExpression.patternClauses.isEmpty()) {
                CompletionVisitorUtil.isCursorWithinBlock(bLangMatchExpression.getPosition(), matchExprEnv,
                        this.lsContext, this);
            }
            bLangMatchExpression.getPatternClauses().forEach(patternClause -> {
                BLangNode node = patternClause;
                CursorPositionResolver posResolver = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
                posResolver.isCursorBeforeNode(node.getPosition(), node, visitor, visitor.lsContext);
                visitor.acceptNode(node, matchExprEnv);
            });
            this.blockOwnerStack.pop();
            this.cursorPositionResolver = fallbackCursorPositionResolver;
        } else {
            // We consider this as a special case and override the symbol environment node to be the match expression
            this.populateSymbolEnvNode(bLangMatchExpression);
        }
    }

    @Override
    public void visit(BLangMatchExpression.BLangMatchExprPatternClause matchExprPatternClause) {
        if (!CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(matchExprPatternClause.getPosition(), matchExprPatternClause, this,
                        this.lsContext)) {
            if (matchExprPatternClause.expr != null) {
                this.acceptNode(matchExprPatternClause.expr, symbolEnv);
            }
        }
    }

    @Override
    public void visit(BLangSimpleVarRef simpleVarRef) {
        CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(simpleVarRef.getPosition(), simpleVarRef, this, this.lsContext);
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        SymbolEnv annotationAttachmentEnv = new SymbolEnv(recordLiteral, symbolEnv.scope);
        CompletionVisitorUtil.isCursorWithinBlock(recordLiteral.getPosition(),
                annotationAttachmentEnv, this.lsContext, this);
    }

    ///////////////////////////////////
    /////   Other Public Methods  /////
    ///////////////////////////////////

    /**
     * Resolve all visible symbols.
     * 
     * @param symbolEnv symbol environment
     * @return all visible symbols for current scope
     */
    public Map<Name, Scope.ScopeEntry> resolveAllVisibleSymbols(SymbolEnv symbolEnv) {
        return symbolResolver.getAllVisibleInScopeSymbols(symbolEnv);
    }

    /**
     * Populate the symbols.
     * 
     * @param symbolEntries     symbol entries
     * @param symbolEnv         Symbol environment
     */
    public void populateSymbols(Map<Name, Scope.ScopeEntry> symbolEntries, @Nonnull SymbolEnv symbolEnv) {
        List<SymbolInfo> visibleSymbols = new ArrayList<>();
        this.populateSymbolEnvNode(symbolEnv.node);
        symbolEntries.forEach((k, v) -> visibleSymbols.add(new SymbolInfo(k.getValue(), v)));
        lsContext.put(CompletionKeys.VISIBLE_SYMBOLS_KEY, visibleSymbols);
    }

    public Deque<Node> getBlockOwnerStack() {
        return blockOwnerStack;
    }

    public Deque<BLangBlockStmt> getBlockStmtStack() {
        return blockStmtStack;
    }

    public SymbolEnv getSymbolEnv() {
        return symbolEnv;
    }

    public void setPreviousNode(BLangNode previousNode) {
        this.previousNode = previousNode;
    }

    public void setNextNode(BLangNode nextNode) {
        lsContext.put(CompletionKeys.NEXT_NODE_KEY,
                nextNode.getKind().toString().toLowerCase(Locale.ENGLISH));
    }

    /**
     * Forcefully terminate the visitor and at the termination, populate the context data.
     */
    public void forceTerminateVisitor() {
        lsContext.put(CompletionKeys.CURRENT_NODE_TRANSACTION_KEY, !this.isCurrentNodeTransactionStack.isEmpty());
        lsContext.put(CompletionKeys.LOOP_COUNT_KEY, this.loopCount);
        lsContext.put(CompletionKeys.TRANSACTION_COUNT_KEY, this.transactionCount);
        lsContext.put(CompletionKeys.PREVIOUS_NODE_KEY, this.previousNode);
        if (!blockOwnerStack.isEmpty()) {
            lsContext.put(CompletionKeys.BLOCK_OWNER_KEY, blockOwnerStack.peek());
        }
        this.terminateVisitor = true;
    }

    ///////////////////////////////////
    /////     Private Methods     /////
    ///////////////////////////////////
    private void acceptNode(BLangNode node, SymbolEnv env) {
        if (this.terminateVisitor) {
            return;
        }

        SymbolEnv prevEnv = this.symbolEnv;
        this.symbolEnv = env;
        node.accept(this);
        this.symbolEnv = prevEnv;
        this.setPreviousNode(node);
    }

    private void populateSymbolEnvNode(BLangNode node) {
        lsContext.put(CompletionKeys.SYMBOL_ENV_NODE_KEY, node);
    }
}

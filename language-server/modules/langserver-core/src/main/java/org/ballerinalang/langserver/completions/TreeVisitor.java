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

import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.LSNodeVisitor;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.AnnotationNodeKind;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.exceptions.CompletionContextNotSupportedException;
import org.ballerinalang.langserver.completions.util.CompletionVisitorUtil;
import org.ballerinalang.langserver.completions.util.CursorPositionResolvers;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.BlockStatementScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.CursorPositionResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ForkJoinStatementScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.InvocationParameterScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ListConstructorScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.MatchExpressionScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.MatchStatementScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ObjectTypeScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.RecordLiteralScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.RecordScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.ServiceScopeResolver;
import org.ballerinalang.langserver.completions.util.positioning.resolvers.TopLevelNodeScopeResolver;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.AnnotationSymbol;
import org.ballerinalang.model.tree.BlockNode;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BServiceSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCommitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRollback;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private Deque<BlockNode> blockStmtStack;

    private Deque<Boolean> isCurrentNodeTransactionStack;

    private Deque<BLangForkJoin> forkJoinStack;

    private Class<?> cursorPositionResolver;

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
        forkJoinStack = new ArrayDeque<>();
        symTable = SymbolTable.getInstance(compilerContext);
        symbolResolver = SymbolResolver.getInstance(compilerContext);
    }

    ///////////////////////////////////
    /////      Visitor Methods    /////
    ///////////////////////////////////

    @Override
    public void visit(BLangPackage pkgNode) {
        boolean isTestSrc = CommonUtil.isTestSource(this.lsContext.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY));
        BLangPackage evalPkg = isTestSrc ? pkgNode.getTestablePkg() : pkgNode;
        if (evalPkg.symbol != null) {

            SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(evalPkg.symbol);
            this.symbolEnv = pkgEnv;

            List<TopLevelNode> topLevelNodes = CommonUtil.getCurrentFileTopLevelNodes(evalPkg, lsContext);

            List<TopLevelNode> filteredTopLevelNodes = topLevelNodes.stream()
                    .filter(CommonUtil.checkInvalidTypesDefs())
                    .collect(Collectors.toList());

            for (int i = 0; i < filteredTopLevelNodes.size(); i++) {
                cursorPositionResolver = TopLevelNodeScopeResolver.class;
                this.blockOwnerStack.push(evalPkg);
                acceptNode((BLangNode) filteredTopLevelNodes.get(i), pkgEnv);
                if (this.terminateVisitor && this.previousNode == null) {
                    int nodeIndex = filteredTopLevelNodes.size() > 1 && i > 0 ? (i - 1) : 0;
                    this.previousNode = (BLangNode) filteredTopLevelNodes.get(nodeIndex);
                    lsContext.put(CompletionKeys.PREVIOUS_NODE_KEY, this.previousNode);
                }
            }
        } else {
            // Parser fails and evalPkg.symbol is NULL.
            throw new IllegalStateException("TreeVisitor failed, evalPkg.symbol is NULL");
        }

        // If the cursor is at an empty document's first line or is bellow the last construct, symbol env node is null
        if (this.lsContext.get(CompletionKeys.SCOPE_NODE_KEY) == null) {
            this.lsContext.put(CompletionKeys.SCOPE_NODE_KEY, evalPkg);
            this.populateSymbols(this.resolveAllVisibleSymbols(this.getSymbolEnv()), this.getSymbolEnv());
            forceTerminateVisitor();
        }
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
        CursorPositionResolvers
                .getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(importPkgNode.getPosition(), this, lsContext, importPkgNode, importPkgNode.symbol);
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(xmlnsNode.getPosition(), this, this.lsContext, xmlnsNode, xmlnsNode.symbol);
    }

    @Override
    public void visit(BLangLambdaFunction lambdaFunction) {
        this.acceptNode(lambdaFunction.function, symbolEnv);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, this.symbolEnv);
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(this.cursorPositionResolver);
        DiagnosticPos functionPos = CommonUtil.clonePosition(funcNode.getPosition());

        if (!funcNode.flagSet.contains(Flag.WORKER)) {
            // Set the current symbol environment instead of the function environment since the annotation is not
            // within the function
            funcNode.annAttachments.forEach(annotationAttachment -> this.acceptNode(annotationAttachment, symbolEnv));
        }
// TODO: Find a better approach along with the new parser implementation
//        else if (funcNode.flagSet.contains(Flag.WORKER) && CompletionVisitorUtil
//                .isWithinWorkerReturnContext(this.symbolEnv, this.lsContext, this, funcNode)) {
//            return;
//        }

        if (terminateVisitor || cpr.isCursorBeforeNode(functionPos, this, this.lsContext, funcNode, funcNode.symbol)) {
            return;
        }

        // Visit the function parameter annotation attachments
        List<BLangNode> functionParamsOrdered = CompletionVisitorUtil.getFunctionParamsOrdered(funcNode);
        functionParamsOrdered.forEach(param -> this.acceptNode(param, symbolEnv));
        funcNode.returnTypeAnnAttachments.forEach(annotation -> this.acceptNode(annotation, symbolEnv));

        if (funcNode.hasBody()) {
            this.blockOwnerStack.push(funcNode);
            this.cursorPositionResolver = BlockStatementScopeResolver.class;
            this.acceptNode(funcNode.body, funcEnv);
            this.blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        // Here we skip the type definitions associated to the services
        if (typeDefinition.symbol != null && (typeDefinition.symbol.flags & Flags.SERVICE) == Flags.SERVICE) {
            return;
        }
        typeDefinition.annAttachments.forEach(annotation -> this.acceptNode(annotation, symbolEnv));
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (cpr.isCursorBeforeNode(typeDefinition.getPosition(), this, this.lsContext, typeDefinition,
                typeDefinition.symbol)) {
            return;
        }
        this.acceptNode(typeDefinition.typeNode, symbolEnv);
    }

    @Override
    public void visit(BLangConstant constant) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (cpr.isCursorBeforeNode(constant.getPosition(), this, this.lsContext, constant, constant.symbol)) {
            return;
        }
        this.acceptNode(constant.typeNode, symbolEnv);
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        BSymbol recordSymbol = recordTypeNode.symbol;
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        SymbolEnv recordEnv = SymbolEnv.createPkgLevelSymbolEnv(recordTypeNode, recordSymbol.scope, symbolEnv);

        // TODO: Since the position of the record type node is invalid, we pass the position of the type definition
        boolean cursorBeforeNode = cpr.isCursorBeforeNode(recordTypeNode.parent.getPosition(), this, this.lsContext,
                recordTypeNode, recordSymbol);
        boolean cursorWithinBlock = recordTypeNode.fields.isEmpty() &&
                CompletionVisitorUtil.isCursorWithinBlock(recordTypeNode.parent.getPosition(),
                        recordEnv, this.lsContext, this);

        if (recordSymbol.getName().getValue().contains(CommonKeys.DOLLAR_SYMBOL_KEY) || cursorBeforeNode
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
    public void visit(BLangSimpleVariable varNode) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        varNode.annAttachments.forEach(annotationAttachment -> this.acceptNode(annotationAttachment, symbolEnv));
        if (cpr.isCursorBeforeNode(varNode.getPosition(), this, this.lsContext, varNode, varNode.symbol)
                || varNode.expr == null) {
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
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        if (listConstructorExpr.getExpressions().isEmpty()
                && CompletionVisitorUtil.isCursorWithinBlock(listConstructorExpr.pos, this.symbolEnv, lsContext,
                this)) {
            return;
        }
        this.cursorPositionResolver = ListConstructorScopeResolver.class;
        listConstructorExpr.getExpressions().forEach(bLangExpression -> this.acceptNode(bLangExpression, symbolEnv));
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        groupExpr.expression.accept(this);
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        conversionExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, symbolEnv);
        List<BLangStatement> statements = blockNode.stmts.stream()
                .filter(bLangStatement -> !CommonUtil.isWorkerDereivative(bLangStatement))
                .sorted(new CommonUtil.BLangNodeComparator())
                .collect(Collectors.toList());

        if (statements.isEmpty() && CompletionVisitorUtil
                .isCursorWithinBlock((DiagnosticPos) (this.blockOwnerStack.peek()).getPosition(), blockEnv,
                        this.lsContext, this)) {
            return;
        }

        for (int i = 0; i < statements.size(); i++) {
            this.blockStmtStack.push(blockNode);
            this.cursorPositionResolver = BlockStatementScopeResolver.class;
            this.acceptNode(statements.get(i), blockEnv);
            if (this.terminateVisitor && this.previousNode == null) {
                int nodeIndex = statements.size() > 1 && i > 0 ? (i - 1) : 0;
                this.previousNode = statements.get(nodeIndex);
                lsContext.put(CompletionKeys.PREVIOUS_NODE_KEY, this.previousNode);
            }
            this.blockStmtStack.pop();
        }
    }

    @Override
    public void visit(BLangBlockFunctionBody blockFuncBody) {
        SymbolEnv blockEnv = SymbolEnv.createFuncBodyEnv(blockFuncBody, symbolEnv);
        List<BLangStatement> statements = blockFuncBody.stmts.stream()
                .filter(bLangStatement -> !CommonUtil.isWorkerDereivative(bLangStatement))
                .sorted(new CommonUtil.BLangNodeComparator())
                .collect(Collectors.toList());

        if (statements.isEmpty() && CompletionVisitorUtil
                .isCursorWithinBlock((DiagnosticPos) (this.blockOwnerStack.peek()).getPosition(), blockEnv,
                        this.lsContext, this)) {
            return;
        }

        for (int i = 0; i < statements.size(); i++) {
            this.blockStmtStack.push(blockFuncBody);
            this.cursorPositionResolver = BlockStatementScopeResolver.class;
            this.acceptNode(statements.get(i), blockEnv);
            if (this.terminateVisitor && this.previousNode == null) {
                int nodeIndex = statements.size() > 1 && i > 0 ? (i - 1) : 0;
                this.previousNode = statements.get(nodeIndex);
                lsContext.put(CompletionKeys.PREVIOUS_NODE_KEY, this.previousNode);
            }
            this.blockStmtStack.pop();
        }
    }

    @Override
    public void visit(BLangExternalFunctionBody body) {
        for (BLangAnnotationAttachment annotation : body.annAttachments) {
            this.acceptNode(annotation, symbolEnv);
        }
    }

    @Override
    public void visit(BLangExprFunctionBody exprFuncBody) {
        SymbolEnv exprBodyEnv = SymbolEnv.createFuncBodyEnv(exprFuncBody, symbolEnv);
        this.acceptNode(exprFuncBody.expr, exprBodyEnv);
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        boolean isFuture = varDefNode.getVariable().expr != null
                && varDefNode.getVariable().expr.type instanceof BFutureType;
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (isFuture || cpr.isCursorBeforeNode(varDefNode.getPosition(), this, this.lsContext, varDefNode,
                varDefNode.getVariable().symbol)) {
            return;
        }

        this.acceptNode(varDefNode.var, symbolEnv);
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (cpr.isCursorBeforeNode(assignNode.getPosition(), this, this.lsContext, assignNode, null)) {
            return;
        }

        this.acceptNode(assignNode.expr, symbolEnv);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (cpr.isCursorBeforeNode(exprStmtNode.getPosition(), this, this.lsContext, exprStmtNode, null)
            /*|| !(exprStmtNode.expr instanceof BLangInvocation)*/) {
            return;
        }

        this.acceptNode(exprStmtNode.expr, symbolEnv);
    }

    @Override
    public void visit(BLangInvocation invocationNode) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);

        if (cpr.isCursorBeforeNode(invocationNode.getPosition(), this, this.lsContext, invocationNode,
                invocationNode.symbol)) {
            return;
        }

        final TreeVisitor visitor = this;
        Class<?> fallbackCursorPositionResolver = this.cursorPositionResolver;
        this.cursorPositionResolver = InvocationParameterScopeResolver.class;
        this.blockOwnerStack.push(invocationNode);
        // Visit all arguments
        invocationNode.getArgumentExpressions().forEach(expressionNode -> {
            BLangNode node = ((BLangNode) expressionNode);
            CursorPositionResolver posResolver = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
            posResolver.isCursorBeforeNode(node.getPosition(), visitor, visitor.lsContext, node, null);
            visitor.acceptNode(node, symbolEnv);
        });
        // Specially check for the position of the cursor to support the completion for complete sources
        // eg: string modifiedStr = sampleStr.replace("hello", "Hello").<cursor>toLower();
        if (!terminateVisitor && (CompletionVisitorUtil.withinInvocationArguments(invocationNode, this.lsContext)
                || CompletionVisitorUtil.cursorBeforeInvocationNode(invocationNode, this.lsContext))) {
            Map<Name, List<Scope.ScopeEntry>> visibleSymbolEntries
                    = this.resolveAllVisibleSymbols(this.symbolEnv);
            this.populateSymbols(visibleSymbolEntries, symbolEnv);
            this.forceTerminateVisitor();
        }
        this.blockOwnerStack.pop();
        this.cursorPositionResolver = fallbackCursorPositionResolver;
    }

    @Override
    public void visit(BLangIf ifNode) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (CompletionVisitorUtil.isWithinConditionContext(this.symbolEnv, this.lsContext, this, ifNode)
                || cpr.isCursorBeforeNode(ifNode.getPosition(), this, this.lsContext, ifNode, null)) {
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
        if (CompletionVisitorUtil.isWithinConditionContext(this.symbolEnv, this.lsContext, this, whileNode) ||
                cpr.isCursorBeforeNode(whileNode.getPosition(), this, this.lsContext, whileNode, null)) {
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
        BLangObjectTypeNode serviceType = (BLangObjectTypeNode) serviceNode.serviceTypeDefinition.typeNode;
        List<BLangNode> serviceContent = new ArrayList<>();
        SymbolEnv serviceEnv = SymbolEnv.createServiceEnv(serviceNode, serviceType.symbol.scope, symbolEnv);
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        List<BLangFunction> serviceFunctions = ((BLangObjectTypeNode) serviceNode.serviceTypeDefinition.typeNode)
                .getFunctions();
        List<BLangSimpleVariable> serviceFields = serviceType.getFields().stream()
                .map(simpleVar -> (BLangSimpleVariable) simpleVar)
                .collect(Collectors.toList());
        serviceContent.addAll(serviceFunctions);
        serviceContent.addAll(serviceFields);
        serviceContent.sort(new CommonUtil.BLangNodeComparator());

        serviceNode.annAttachments.forEach(annotationAttachment -> this.acceptNode(annotationAttachment, serviceEnv));
        boolean cursorWithinBlock = serviceFunctions.isEmpty()
                && serviceFields.isEmpty()
                && CompletionVisitorUtil.isCursorWithinBlock(serviceNode.getPosition(), serviceEnv, this.lsContext,
                this);
        boolean cursorWithinAttachedExprs = CompletionVisitorUtil.cursorWithinServiceExpressionList(serviceNode,
                serviceEnv, this.lsContext, this);

        if (cpr.isCursorBeforeNode(serviceNode.getPosition(), this, this.lsContext, serviceNode, serviceNode.symbol)
                || cursorWithinBlock || cursorWithinAttachedExprs) {
            return;
        }

        this.blockOwnerStack.push(serviceNode.serviceTypeDefinition.typeNode);

        for (int i = 0; i < serviceContent.size(); i++) {
            this.cursorPositionResolver = ServiceScopeResolver.class;
            this.acceptNode(serviceContent.get(i), serviceEnv);
            if (this.terminateVisitor && this.previousNode == null) {
                int nodeIndex = serviceContent.size() > 1 && i > 0 ? (i - 1) : 0;
                this.previousNode = serviceContent.get(nodeIndex);
                lsContext.put(CompletionKeys.PREVIOUS_NODE_KEY, this.previousNode);
            }
        }

        this.blockOwnerStack.pop();
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        SymbolEnv transactionEnv = SymbolEnv.createTransactionEnv(transactionNode, symbolEnv);
        this.blockOwnerStack.push(transactionNode.transactionBody);
        this.isCurrentNodeTransactionStack.push(true);
        this.transactionCount++;
        this.acceptNode(transactionNode.transactionBody, transactionEnv);
        this.blockOwnerStack.pop();
        this.isCurrentNodeTransactionStack.pop();
        this.transactionCount--;
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        if (CursorPositionResolvers.getResolverByClass(this.cursorPositionResolver)
                .isCursorBeforeNode(forkJoin.pos, this, this.lsContext, forkJoin, null)) {
            return;
        }
        SymbolEnv folkJoinEnv = SymbolEnv.createFolkJoinEnv(forkJoin, this.symbolEnv);
        if (forkJoin.workers.isEmpty()
                && CompletionVisitorUtil.isCursorWithinBlock(forkJoin.pos, folkJoinEnv, this.lsContext, this)) {
            return;
        }
        Class backupResolver = this.cursorPositionResolver;
        this.forkJoinStack.push(forkJoin);
        forkJoin.workers.forEach(e -> {
            this.cursorPositionResolver = ForkJoinStatementScopeResolver.class;
            this.acceptNode(e, folkJoinEnv);
        });
        this.forkJoinStack.pop();
        this.cursorPositionResolver = backupResolver;
    }

    @Override
    public void visit(BLangWorker workerNode) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (cpr.isCursorBeforeNode(workerNode.getPosition(), this, this.lsContext, workerNode, workerNode.symbol)) {
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
                .isCursorBeforeNode(workerSendNode.getPosition(), this, this.lsContext, workerSendNode, null);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        //Todo receive is an expression now and a statement
        //CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
        //        .isCursorBeforeNode(workerReceiveNode.getPosition(), this, this.lsContext, workerReceiveNode, null);
    }

    @Override
    public void visit(BLangReturn returnNode) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (cpr.isCursorBeforeNode(returnNode.getPosition(), this, this.lsContext, returnNode, null)) {
            return;
        }

        this.acceptNode(returnNode.expr, symbolEnv);
    }

    @Override
    public void visit(BLangContinue continueNode) {
        CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(continueNode.getPosition(), this, this.lsContext, continueNode, null);
    }

    @Override
    public void visit(BLangBreak breakNode) {
        CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(breakNode.getPosition(), this, this.lsContext, breakNode, null);
    }

    @Override
    public void visit(BLangPanic panicNode) {
        CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(panicNode.getPosition(), this, this.lsContext, panicNode, null);
    }

    @Override
    public void visit(BLangLock lockNode) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (cpr.isCursorBeforeNode(lockNode.getPosition(), this, this.lsContext, lockNode, null)) {
            return;
        }

        this.blockOwnerStack.push(lockNode);
        this.acceptNode(lockNode.body, symbolEnv);
        this.blockOwnerStack.pop();
    }

    @Override
    public void visit(BLangForeach foreach) {
        if (!CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(foreach.getPosition(), this, this.lsContext, foreach, null)) {
            this.blockOwnerStack.push(foreach);
            loopCount++;
            this.acceptNode(foreach.body, symbolEnv);
            loopCount--;
            this.blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangMatch matchNode) {
        if (matchNode.getPosition() != null && !CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(matchNode.getPosition(), this, this.lsContext, matchNode, null)) {
            this.blockOwnerStack.push(matchNode);
            matchNode.patternClauses.forEach(patternClause -> {
                cursorPositionResolver = MatchStatementScopeResolver.class;
                acceptNode(patternClause, symbolEnv);
            });
            this.blockOwnerStack.pop();
        }
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        SymbolEnv annotationAttachmentEnv = new SymbolEnv(annAttachmentNode, symbolEnv.scope);
        symbolEnv.copyTo(annotationAttachmentEnv);
        if (annAttachmentNode.annotationSymbol == null) {
            return;
        }
        PackageID packageID = annAttachmentNode.annotationSymbol.pkgID;
        if (packageID.getOrgName().getValue().equals("ballerina") && packageID.getName().getValue().equals("grpc")
                && annAttachmentNode.annotationName.getValue().equals("ServiceDescriptor")) {
            return;
        }
        if (CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(annAttachmentNode.getPosition(), this, this.lsContext, annAttachmentNode,
                        annAttachmentNode.annotationSymbol)) {
            return;
        }
        this.acceptNode(annAttachmentNode.expr, annotationAttachmentEnv);
    }

    @Override
    public void visit(BLangRollback rollbackNode) {
        CursorPositionResolvers.getResolverByClass(this.cursorPositionResolver)
                .isCursorBeforeNode(rollbackNode.getPosition(), this, this.lsContext, rollbackNode);
    }

    @Override
    public void visit(BLangCommitExpr commitExpr) {
        CursorPositionResolvers.getResolverByClass(this.cursorPositionResolver)
                .isCursorBeforeNode(commitExpr.getPosition(), this, this.lsContext, commitExpr);
    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {
        if (!CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(bLangMatchExpression.getPosition(), this, this.lsContext, bLangMatchExpression,
                        null)) {
            SymbolEnv matchExprEnv = new SymbolEnv(bLangMatchExpression, symbolEnv.scope);
            symbolEnv.copyTo(matchExprEnv);
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
                posResolver.isCursorBeforeNode(node.getPosition(), visitor, visitor.lsContext, node, null);
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
                .isCursorBeforeNode(matchExprPatternClause.getPosition(), this, this.lsContext, matchExprPatternClause,
                        null)) {
            if (matchExprPatternClause.expr != null) {
                this.acceptNode(matchExprPatternClause.expr, symbolEnv);
            }
        }
    }

    @Override
    public void visit(BLangSimpleVarRef simpleVarRef) {
        CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(simpleVarRef.getPosition(), this, this.lsContext, simpleVarRef);
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        if (CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(recordLiteral.getPosition(), this, this.lsContext, recordLiteral)) {
            return;
        }
        SymbolEnv recordLiteralEnv = new SymbolEnv(recordLiteral, symbolEnv.scope);
        symbolEnv.copyTo(recordLiteralEnv);
        this.blockOwnerStack.push(recordLiteral);
        List<RecordLiteralNode.RecordField> fields = recordLiteral.fields;
        if (fields.isEmpty() && CompletionVisitorUtil.isCursorWithinBlock(recordLiteral.pos, recordLiteralEnv,
                lsContext, this)) {
            return;
        }
        Class backUpResolver = this.cursorPositionResolver;
        fields.forEach(keyValue -> {
            this.cursorPositionResolver = RecordLiteralScopeResolver.class;
            this.acceptNode(keyValue, recordLiteralEnv);
        });
        this.cursorPositionResolver = backUpResolver;
        this.blockOwnerStack.pop();
    }

    @Override
    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause patternClause) {
        if (!CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(patternClause.getPosition(), this, this.lsContext, patternClause, null)) {
            this.visitMatchPatternClause(patternClause, patternClause.body);
        }
    }

    @Override
    public void visit(BLangMatch.BLangMatchStructuredBindingPatternClause patternClause) {
        if (!CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(patternClause.getPosition(), this, this.lsContext, patternClause, null)) {
            this.visitMatchPatternClause(patternClause, patternClause.body);
        }
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKeyValueField recordKeyValue) {
        if (CursorPositionResolvers.getResolverByClass(this.cursorPositionResolver)
                .isCursorBeforeNode(recordKeyValue.valueExpr.getPosition(), this, this.lsContext, recordKeyValue)) {
            return;
        }
        this.acceptNode(recordKeyValue.valueExpr, this.symbolEnv);
    }

    @Override
    public void visit(BLangTypeInit connectorInitExpr) {
        connectorInitExpr.argsExpr.forEach(bLangExpression -> this.acceptNode(bLangExpression, symbolEnv));
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        this.acceptNode(bLangNamedArgsExpression.expr, this.symbolEnv);
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        annotationNode.annAttachments.forEach(annotation -> this.acceptNode(annotation, symbolEnv));
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(this.cursorPositionResolver);
        cpr.isCursorBeforeNode(annotationNode.pos, this, this.lsContext, annotationNode, annotationNode.symbol);
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        cpr.isCursorBeforeNode(bLangTupleVariableDef.getPosition(), this, this.lsContext, bLangTupleVariableDef, null);
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        if (literalExpr.getPosition() == null) {
            return;
        }
        DiagnosticPos pos = CommonUtil.toZeroBasedPosition(literalExpr.getPosition());
        Position position = lsContext.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        int cLine = position.getLine();
        int cCol = position.getCharacter();
        int sLine = pos.sLine;
        int eLine = pos.eLine;
        int sCol = pos.sCol;
        int eCol = pos.eCol;

        if ((sLine < cLine && eLine > cLine) || (sLine == cLine && eLine == cLine && cCol >= sCol && cCol <= eCol)) {
            throw new CompletionContextNotSupportedException("Completion within Literals are not Supported");
        }
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (cpr.isCursorBeforeNode(compoundAssignNode.getPosition(), this, this.lsContext, compoundAssignNode, null)) {
            return;
        }

        this.acceptNode(compoundAssignNode.expr, symbolEnv);
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        cpr.isCursorBeforeNode(bLangErrorVariableDef.getPosition(), this, this.lsContext, bLangErrorVariableDef, null);
    }

    @Override
    public void visit(BLangQueryAction queryAction) {
        this.acceptNode(queryAction.doClause, symbolEnv);
    }

    @Override
    public void visit(BLangDoClause doClause) {
        this.acceptNode(doClause.body, symbolEnv);
    }

    @Override
    public void visit(BLangQueryExpr queryExpr) {
        CursorPositionResolver cpr = CursorPositionResolvers.getResolverByClass(cursorPositionResolver);
        if (cpr.isCursorBeforeNode(queryExpr.getPosition(), this, this.lsContext, queryExpr)) {
            return;
        }
        queryExpr.queryClauseList.forEach(node -> this.acceptNode(node, this.symbolEnv));
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        CompletionVisitorUtil.isCursorWithinBlock(waitForAllExpr.parent.getPosition(),
                this.symbolEnv, this.lsContext, this);
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
    public Map<Name, List<Scope.ScopeEntry>> resolveAllVisibleSymbols(SymbolEnv symbolEnv) {
        return symbolResolver.getAllVisibleInScopeSymbols(symbolEnv);
    }

    /**
     * Populate the symbols.
     *
     * @param symbolEntries symbol entries
     * @param symbolEnv     Symbol environment
     */
    public void populateSymbols(Map<Name, List<Scope.ScopeEntry>> symbolEntries, @Nonnull SymbolEnv symbolEnv) {
        List<Scope.ScopeEntry> visibleSymbols = new ArrayList<>();
        this.populateSymbolEnvNode(symbolEnv.node);
        symbolEntries.values().forEach(visibleSymbols::addAll);
        lsContext.put(CommonKeys.VISIBLE_SYMBOLS_KEY, visibleSymbols);
    }

    public Deque<Node> getBlockOwnerStack() {
        return blockOwnerStack;
    }

    public Deque<BlockNode> getBlockStmtStack() {
        return blockStmtStack;
    }

    public Deque<BLangForkJoin> getForkJoinStack() {
        return forkJoinStack;
    }

    public SymbolEnv getSymbolEnv() {
        return symbolEnv;
    }

    public void setNextNode(BSymbol symbol, BLangNode node) {
        if (symbol instanceof BServiceSymbol) {
            lsContext.put(CompletionKeys.NEXT_NODE_KEY, AnnotationNodeKind.SERVICE);
        } else if (symbol instanceof BInvokableSymbol && (symbol.flags & Flags.RESOURCE) == Flags.RESOURCE) {
            lsContext.put(CompletionKeys.NEXT_NODE_KEY, AnnotationNodeKind.RESOURCE);
        } else if (symbol instanceof BInvokableSymbol) {
            if (node instanceof BLangSimpleVariableDef
                    && ((BLangSimpleVariableDef) node).var.expr instanceof BLangLambdaFunction
                    && ((BLangLambdaFunction) ((BLangSimpleVariableDef) node).var.expr).function.flagSet
                    .contains(Flag.WORKER)) {
                lsContext.put(CompletionKeys.NEXT_NODE_KEY, AnnotationNodeKind.WORKER);
            } else {
                lsContext.put(CompletionKeys.NEXT_NODE_KEY, AnnotationNodeKind.FUNCTION);
            }
        } else if (symbol instanceof BVarSymbol && (symbol.flags & Flags.LISTENER) == Flags.LISTENER) {
            lsContext.put(CompletionKeys.NEXT_NODE_KEY, AnnotationNodeKind.LISTENER);
        } else if (symbol instanceof BRecordTypeSymbol) {
            lsContext.put(CompletionKeys.NEXT_NODE_KEY, AnnotationNodeKind.RECORD);
        } else if (symbol instanceof BObjectTypeSymbol) {
            lsContext.put(CompletionKeys.NEXT_NODE_KEY, AnnotationNodeKind.OBJECT);
        } else if (symbol instanceof BTypeSymbol) {
            lsContext.put(CompletionKeys.NEXT_NODE_KEY, AnnotationNodeKind.TYPE);
        } else if (symbol instanceof AnnotationSymbol) {
            lsContext.put(CompletionKeys.NEXT_NODE_KEY, AnnotationNodeKind.ANNOTATION);
        } else if (symbol instanceof BConstantSymbol) {
            lsContext.put(CompletionKeys.NEXT_NODE_KEY, AnnotationNodeKind.CONSTANT);
        }
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
    private void acceptNode(Node node, SymbolEnv env) {
        acceptNode((BLangNode) node, env);
    }

    private void acceptNode(BLangNode node, SymbolEnv env) {
        if (this.terminateVisitor || node == null) {
            return;
        }

        SymbolEnv prevEnv = this.symbolEnv;
        this.symbolEnv = env;
        node.accept(this);
        this.symbolEnv = prevEnv;
    }

    private void populateSymbolEnvNode(BLangNode node) {
        lsContext.put(CompletionKeys.SCOPE_NODE_KEY, node);
    }

    private void visitMatchPatternClause(BLangNode patternNode, BLangBlockStmt body) {
        if (!CursorPositionResolvers.getResolverByClass(cursorPositionResolver)
                .isCursorBeforeNode(patternNode.getPosition(), this, this.lsContext, patternNode, null)) {
            blockOwnerStack.push(patternNode);
            SymbolEnv blockEnv = SymbolEnv.createBlockEnv(body, symbolEnv);
            cursorPositionResolver = BlockStatementScopeResolver.class;
            acceptNode(body, blockEnv);
            blockOwnerStack.pop();
        }
    }
}

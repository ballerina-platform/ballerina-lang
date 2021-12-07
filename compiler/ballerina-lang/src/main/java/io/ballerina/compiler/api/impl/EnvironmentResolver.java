/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.api.impl;

import io.ballerina.tools.diagnostics.Location;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.model.clauses.OrderKeyNode;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.ExpressionNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BFutureType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangMatchClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnFailClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderByClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangDo;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatchStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetryTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;

import java.util.List;

/**
 * Visitor to lookup symbols based on the location.
 *
 * @since 2.0.0
 */
public class EnvironmentResolver extends BaseVisitor {
    LinePosition linePosition = null;
    private SymbolEnv symbolEnv;
    private SymbolEnv scope;

    public EnvironmentResolver(SymbolEnv pkgEnv) {
        this.symbolEnv = pkgEnv;
        this.scope = pkgEnv;
    }

    public SymbolEnv lookUp(BLangCompilationUnit compilationUnit, LinePosition linePosition) {
        this.linePosition = linePosition;
        compilationUnit.accept(this);
        return this.scope;
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {

        for (TopLevelNode node : compUnit.getTopLevelNodes()) {
            if (node.getKind() == NodeKind.FUNCTION && isWorkerOrLambdaFunction((BLangFunction) node)) {
                continue;
            }

            this.acceptNode((BLangNode) node, this.symbolEnv);
        }
    }

    @Override
    public void visit(BLangService serviceNode) {
        if (PositionUtil.withinBlock(this.linePosition, serviceNode.getPosition())
                && isNarrowerEnclosure(serviceNode.getPosition())) {
            SymbolEnv serviceEnv = SymbolEnv.createServiceEnv(serviceNode, serviceNode.getServiceClass().symbol.scope,
                                                              this.symbolEnv);
            this.scope = serviceEnv;
            serviceNode.getServiceClass().getFunctions().forEach(function -> this.acceptNode(function, serviceEnv));
            return;
        }
        serviceNode.annAttachments.forEach(annotation -> this.acceptNode(annotation, this.symbolEnv));
    }

    @Override
    public void visit(BLangFunction funcNode) {
        // If the function is Expression-bodied, then a right-inclusive position lookup would be performed.
        // TODO: Update this approach accordingly once the discussion
        //  at: https://github.com/ballerina-platform/ballerina-lang/discussions/28983 is concluded
        if ((funcNode.getBody() != null && funcNode.getBody().getKind() == NodeKind.EXPR_FUNCTION_BODY &&
                PositionUtil.withinRightInclusive(this.linePosition, funcNode.getPosition()))
                || (PositionUtil.withinBlock(this.linePosition, funcNode.getPosition())
                && isNarrowerEnclosure(funcNode.getPosition()))) {
            SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, this.symbolEnv);
            this.scope = funcEnv;
            this.acceptNode(funcNode.getBody(), funcEnv);
            return;
        }
        funcNode.getAnnotationAttachments().forEach(annotation -> this.acceptNode(annotation, this.symbolEnv));
    }

    @Override
    public void visit(BLangResourceFunction resourceFunction) {
        visit((BLangFunction) resourceFunction);
    }

    // TODO: Add the expression and the external
    @Override
    public void visit(BLangBlockFunctionBody blockFuncBody) {
        if (PositionUtil.withinBlock(this.linePosition, blockFuncBody.getPosition())
                && isNarrowerEnclosure(blockFuncBody.getPosition())) {
            SymbolEnv funcBodyEnv = SymbolEnv.createFuncBodyEnv(blockFuncBody, this.symbolEnv);
            this.scope = funcBodyEnv;
            blockFuncBody.getStatements().forEach(bLangStatement -> this.acceptNode(bLangStatement, funcBodyEnv));
        }
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        typeDefinition.getAnnotationAttachments().forEach(annotations -> this.acceptNode(annotations, this.symbolEnv));
        this.acceptNode(typeDefinition.typeNode, this.symbolEnv);
    }

    @Override
    public void visit(BLangClassDefinition classDefinition) {
        if (PositionUtil.withinBlock(this.linePosition, classDefinition.getPosition())
                && isNarrowerEnclosure(classDefinition.getPosition())) {
            SymbolEnv env = SymbolEnv.createClassEnv(classDefinition, classDefinition.symbol.scope, this.symbolEnv);
            this.scope = env;
            classDefinition.getFunctions().forEach(function -> this.acceptNode(function, env));
            if (classDefinition.initFunction != null) {
                this.acceptNode(classDefinition.initFunction, env);
            }
        }
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        if (PositionUtil.withinBlock(this.linePosition, objectTypeNode.getPosition())
                && isNarrowerEnclosure(objectTypeNode.getPosition())) {
            SymbolEnv env = SymbolEnv.createTypeEnv(objectTypeNode, objectTypeNode.symbol.scope, this.symbolEnv);
            this.scope = env;
            objectTypeNode.getFunctions().forEach(function -> this.acceptNode(function, env));
        }
    }

    @Override
    public void visit(BLangConstant constant) {
        this.acceptNode(constant.typeNode, symbolEnv);
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        if (PositionUtil.withinBlock(this.linePosition, recordTypeNode.getPosition())
                && isNarrowerEnclosure(recordTypeNode.getPosition())) {
            BSymbol recordSymbol = recordTypeNode.symbol;
            SymbolEnv recordEnv = SymbolEnv.createPkgLevelSymbolEnv(recordTypeNode, recordSymbol.scope, symbolEnv);
            this.scope = recordEnv;
            recordTypeNode.fields.forEach(field -> acceptNode(field, recordEnv));
        }
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        this.acceptNode(varNode.expr, symbolEnv);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        binaryExpr.getLeftExpression().accept(this);
        binaryExpr.getRightExpression().accept(this);
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
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
        if (PositionUtil.withinBlock(this.linePosition, blockNode.getPosition())
                && isNarrowerEnclosure(blockNode.getPosition())) {
            SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, symbolEnv);
            this.scope = blockEnv;
            for (BLangStatement statement : blockNode.stmts) {
                this.acceptNode(statement, blockEnv);
            }
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
        if (PositionUtil.withinBlock(this.linePosition, exprFuncBody.getPosition())
                && isNarrowerEnclosure(exprFuncBody.getPosition())) {
            SymbolEnv exprBodyEnv = SymbolEnv.createFuncBodyEnv(exprFuncBody, symbolEnv);
            this.scope = exprBodyEnv;
            this.acceptNode(exprFuncBody.expr, exprBodyEnv);
        }
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        boolean isFuture = varDefNode.getVariable().expr != null
                && varDefNode.getVariable().expr.getBType() instanceof BFutureType;
        if (isFuture) {
            return;
        }

        this.acceptNode(varDefNode.var, this.symbolEnv);
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        this.acceptNode(assignNode.expr, this.symbolEnv);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        if (!(exprStmtNode.expr instanceof BLangInvocation)) {
            return;
        }
        this.acceptNode(exprStmtNode.expr, symbolEnv);
    }

    @Override
    public void visit(BLangInvocation invocationNode) {
        for (ExpressionNode argExpr : invocationNode.getArgumentExpressions()) {
            this.acceptNode((BLangNode) argExpr, this.symbolEnv);
        }
        // Specially check for the position of the cursor to support the completion for complete sources
        // eg: string modifiedStr = sampleStr.replace("hello", "Hello").<cursor>toLower();
    }

    @Override
    public void visit(BLangErrorConstructorExpr errorConstructorExpr) {
        for (BLangExpression positionalArg : errorConstructorExpr.positionalArgs) {
            this.acceptNode(positionalArg, this.symbolEnv);
        }

        for (BLangNamedArgsExpression namedArg : errorConstructorExpr.namedArgs) {
            this.acceptNode(namedArg, this.symbolEnv);
        }
    }

    @Override
    public void visit(BLangIf ifNode) {
        if (PositionUtil.withinBlock(this.linePosition, ifNode.getBody().getPosition())
                && isNarrowerEnclosure(ifNode.getBody().getPosition())) {
            this.scope = this.symbolEnv;
            this.acceptNode(ifNode.body, this.symbolEnv);
        }

        if (ifNode.elseStmt != null) {
            acceptNode(ifNode.elseStmt, this.symbolEnv);
        }
    }

    @Override
    public void visit(BLangWhile whileNode) {
        if (PositionUtil.withinBlock(this.linePosition, whileNode.getPosition())
                && isNarrowerEnclosure(whileNode.getPosition())) {
            this.scope = this.symbolEnv;
            this.acceptNode(whileNode.body, this.symbolEnv);
            this.acceptNode(whileNode.onFailClause, symbolEnv);
        }
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        if (PositionUtil.withinBlock(this.linePosition, transactionNode.getPosition())
                && isNarrowerEnclosure(transactionNode.getPosition())) {
            SymbolEnv transactionEnv = SymbolEnv.createTransactionEnv(transactionNode, this.symbolEnv);
            this.acceptNode(transactionNode.transactionBody, transactionEnv);
            this.acceptNode(transactionNode.onFailClause, symbolEnv);
        }
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        if (PositionUtil.withinBlock(this.linePosition, forkJoin.getPosition())
                && isNarrowerEnclosure(forkJoin.getPosition())) {
            SymbolEnv forkJoinEnv = SymbolEnv.createFolkJoinEnv(forkJoin, this.symbolEnv);
            this.scope = forkJoinEnv;
            forkJoin.workers.forEach(e -> this.acceptNode(e, forkJoinEnv));
        }
    }

    @Override
    public void visit(BLangReturn returnNode) {
        this.acceptNode(returnNode.expr, symbolEnv);
    }

    @Override
    public void visit(BLangPanic panicNode) {
        this.acceptNode(panicNode.expr, this.symbolEnv);
    }

    @Override
    public void visit(BLangLock lockNode) {
        this.acceptNode(lockNode.body, symbolEnv);
        this.acceptNode(lockNode.onFailClause, symbolEnv);
    }

    @Override
    public void visit(BLangForeach foreach) {
        this.acceptNode(foreach.body, symbolEnv);
        this.acceptNode(foreach.onFailClause, symbolEnv);
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        if (!PositionUtil.withinBlock(this.linePosition, annAttachmentNode.getPosition())
                || !isNarrowerEnclosure(annAttachmentNode.getPosition())) {
            return;
        }
        SymbolEnv annotationAttachmentEnv = new SymbolEnv(annAttachmentNode, symbolEnv.scope);
        this.symbolEnv.copyTo(annotationAttachmentEnv);
        if (annAttachmentNode.annotationSymbol == null) {
            return;
        }
        PackageID packageID = annAttachmentNode.annotationSymbol.pkgID;
        // TODO: Do we need this still?
        if (packageID.getOrgName().getValue().equals("ballerina") && packageID.getName().getValue().equals("grpc")
                && annAttachmentNode.annotationName.getValue().equals("ServiceDescriptor")) {
            return;
        }
        this.scope = annotationAttachmentEnv;
        this.acceptNode(annAttachmentNode.expr, annotationAttachmentEnv);
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        if (!PositionUtil.withinBlock(this.linePosition, recordLiteral.getPosition())
                || !isNarrowerEnclosure(recordLiteral.getPosition())) {
            return;
        }
        SymbolEnv recordLiteralEnv = new SymbolEnv(recordLiteral, symbolEnv.scope);
        symbolEnv.copyTo(recordLiteralEnv);
        List<RecordLiteralNode.RecordField> fields = recordLiteral.fields;
        this.scope = recordLiteralEnv;
        fields.forEach(keyValue -> this.acceptNode((BLangNode) keyValue, recordLiteralEnv));
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKeyValueField recordKeyValue) {
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
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        this.acceptNode(compoundAssignNode.expr, symbolEnv);
    }

    @Override
    public void visit(BLangQueryAction queryAction) {
        if (!PositionUtil.withinRightInclusive(this.linePosition, queryAction.getPosition())) {
            return;
        }
        for (BLangNode clause : queryAction.queryClauseList) {
            this.acceptNode(clause, symbolEnv);
        }
        this.acceptNode(queryAction.doClause, symbolEnv);
    }

    @Override
    public void visit(BLangDoClause doClause) {
        if (PositionUtil.withinBlock(this.linePosition, doClause.getPosition())) {
            this.scope = doClause.env;
            this.acceptNode(doClause.body, doClause.env);
        }
    }

    @Override
    public void visit(BLangFromClause fromClause) {
        if (!PositionUtil.withinRightInclusive(this.linePosition, fromClause.getPosition())) {
            return;
        }
        this.scope = fromClause.env;
        this.acceptNode((BLangNode) fromClause.variableDefinitionNode, fromClause.env);
        this.acceptNode(fromClause.collection, fromClause.env);
    }

    @Override
    public void visit(BLangLetClause letClause) {
        if (!PositionUtil.withinRightInclusive(this.linePosition, letClause.getPosition())) {
            return;
        }
        this.scope = letClause.env;
        for (BLangLetVariable letVar : letClause.letVarDeclarations) {
            this.acceptNode((BLangNode) letVar.definitionNode, letClause.env);
        }
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        if (!PositionUtil.withinRightInclusive(this.linePosition, selectClause.getPosition())) {
            return;
        }
        this.scope = selectClause.env;
        this.acceptNode(selectClause.expression, selectClause.env);
    }

    @Override
    public void visit(BLangWhereClause whereClause) {
        if (!PositionUtil.withinRightInclusive(this.linePosition, whereClause.getPosition())) {
            return;
        }
        this.scope = whereClause.env;
        this.acceptNode(whereClause.expression, whereClause.env);
    }

    @Override
    public void visit(BLangLetExpression letExpr) {
        if (PositionUtil.withinRightInclusive(this.linePosition, letExpr.getPosition())
                && isNarrowerEnclosure(letExpr.getPosition())) {
            SymbolEnv letExprEnv = letExpr.env.createClone();
            this.scope = letExprEnv;

            for (BLangLetVariable letVarDecl : letExpr.letVarDeclarations) {
                this.acceptNode((BLangNode) letVarDecl.definitionNode, letExprEnv);
            }

            this.acceptNode(letExpr.expr, letExprEnv);
        }
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        this.acceptNode(bLangLambdaFunction.function, bLangLambdaFunction.capturedClosureEnv);
    }

    @Override
    public void visit(BLangQueryExpr queryExpr) {
        if (!PositionUtil.withinRightInclusive(this.linePosition, queryExpr.getPosition())) {
            return;
        }
        for (BLangNode clause : queryExpr.queryClauseList) {
            this.acceptNode(clause, symbolEnv);
        }
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
        this.acceptNode(bLangStatementExpression.stmt, this.symbolEnv);
    }

    @Override
    public void visit(BLangDo doNode) {
        if (PositionUtil.withinBlock(this.linePosition, doNode.getPosition())
                && isNarrowerEnclosure(doNode.getPosition())) {
            this.acceptNode(doNode.body, symbolEnv);
            this.acceptNode(doNode.onFailClause, symbolEnv);
        }
    }

    @Override
    public void visit(BLangMatchStatement matchStatementNode) {
        if (PositionUtil.withinBlock(this.linePosition, matchStatementNode.getPosition())
                && isNarrowerEnclosure(matchStatementNode.getPosition())) {
            matchStatementNode.getMatchClauses()
                    .forEach(bLangMatchClause -> this.acceptNode(bLangMatchClause, this.symbolEnv));
        }
    }

    @Override
    public void visit(BLangRetry retryNode) {
        if (PositionUtil.withinBlock(this.linePosition, retryNode.getPosition())
                && isNarrowerEnclosure(retryNode.getPosition())) {
            this.acceptNode(retryNode.retryBody, symbolEnv);
            this.acceptNode(retryNode.onFailClause, symbolEnv);
        }
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction) {
        if (PositionUtil.withinBlock(this.linePosition, retryTransaction.getPosition())
                && isNarrowerEnclosure(retryTransaction.getPosition())) {
            this.acceptNode(retryTransaction.transaction, symbolEnv);
        }
    }

    @Override
    public void visit(BLangJoinClause joinClause) {
        if (!PositionUtil.withinRightInclusive(this.linePosition, joinClause.getPosition())) {
            return;
        }
        this.scope = joinClause.env;
        this.acceptNode(joinClause.collection, joinClause.env);
        this.acceptNode((BLangNode) joinClause.onClause, joinClause.env);
    }

    @Override
    public void visit(BLangOnClause onClause) {
        if (!PositionUtil.withinRightInclusive(this.linePosition, onClause.getPosition())) {
            return;
        }
        if (onClause.equalsKeywordPos == null ||
                onClause.equalsKeywordPos.lineRange().startLine().offset() > this.linePosition.offset()) {
            this.scope = onClause.lhsEnv;
            this.acceptNode(onClause.lhsExpr, onClause.lhsEnv);

        }
        if (onClause.equalsKeywordPos != null && onClause.equalsKeywordPos
                .lineRange().endLine().offset() < this.linePosition.offset()) {
            this.scope = onClause.rhsEnv;
            this.acceptNode(onClause.rhsExpr, onClause.rhsEnv);
        }
    }

    @Override
    public void visit(BLangOrderByClause orderByClause) {
        if (!PositionUtil.withinRightInclusive(this.linePosition, orderByClause.getPosition())) {
            return;
        }
        this.scope = orderByClause.env;
        for (OrderKeyNode key : orderByClause.orderByKeyList) {
            this.acceptNode((BLangNode) key.getOrderKey(), orderByClause.env);
        }
    }

    @Override
    public void visit(BLangOnFailClause onFailClause) {
        this.acceptNode(onFailClause.body, this.symbolEnv);
    }

    @Override
    public void visit(BLangMatchClause matchClause) {
        if (PositionUtil.withinBlock(this.linePosition, matchClause.getPosition())
                && isNarrowerEnclosure(matchClause.getPosition())) {
            SymbolEnv blockEnv = SymbolEnv.createBlockEnv(matchClause.blockStmt, this.symbolEnv);
            this.scope = blockEnv;
            this.acceptNode(matchClause.blockStmt, blockEnv);
        }
    }

    private void acceptNode(BLangNode node, SymbolEnv env) {
        if (node == null) {
            return;
        }
        SymbolEnv prevEnv = this.symbolEnv;
        this.symbolEnv = env;
        node.accept(this);
        this.symbolEnv = prevEnv;
    }

    private boolean isNarrowerEnclosure(Location nodePosition) {
        // Have to special case the pkg node since its position is (0,0,0,0). Plus any other node would for sure will
        // be a narrower enclosure than the package.
        if (this.scope == null || this.scope.node.getKind() == NodeKind.PACKAGE) {
            return true;
        }

        return PositionUtil.withinRange(nodePosition.lineRange(), this.scope.node.getPosition());
    }

    private boolean isWorkerOrLambdaFunction(BLangFunction node) {
        return node.flagSet.contains(Flag.WORKER) || node.flagSet.contains(Flag.LAMBDA);
    }
}

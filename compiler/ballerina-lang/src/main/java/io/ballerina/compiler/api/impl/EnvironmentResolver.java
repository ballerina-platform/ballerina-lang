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

import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.model.elements.PackageID;
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
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownReferenceDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangRetrySpec;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeyTypeConstraint;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangCaptureBindingPattern;
import org.wso2.ballerinalang.compiler.tree.bindingpatterns.BLangListBindingPattern;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLimitClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangMatchClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnConflictClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnFailClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderByClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderKey;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCommitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIgnoreExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecatedParametersDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkDownDeprecationDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchGuard;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangObjectConstructorExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRawTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableMultiKeyExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTransactionalExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerFlushExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerSyncSendExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementFilter;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLNavigationAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLSequenceLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangConstPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangVarBindingPatternMatchPattern;
import org.wso2.ballerinalang.compiler.tree.matchpatterns.BLangWildCardMatchPattern;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangDo;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangFail;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatchStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetryTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRollback;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangIntersectionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangStreamType;
import org.wso2.ballerinalang.compiler.tree.types.BLangTableTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;

import java.util.List;

/**
 * Visitor to lookup symbols based on the location.
 *
 * @since 2.0.0
 */
public class EnvironmentResolver extends BLangNodeVisitor {
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
        compUnit.getTopLevelNodes().forEach(topLevelNode -> this.acceptNode((BLangNode) topLevelNode, this.symbolEnv));
    }

    @Override
    public void visit(BLangService serviceNode) {
        if (PositionUtil.withinBlock(this.linePosition, serviceNode.getPosition())) {
            SymbolEnv serviceEnv = SymbolEnv.createServiceEnv(serviceNode, serviceNode.symbol.scope, this.symbolEnv);
            this.scope = serviceEnv;
            serviceNode.getResources().forEach(function -> this.acceptNode(function, serviceEnv));
            return;
        }
        serviceNode.annAttachments.forEach(annotation -> this.acceptNode(annotation, this.symbolEnv));
    }

    @Override
    public void visit(BLangFunction funcNode) {
        if (PositionUtil.withinBlock(this.linePosition, funcNode.getPosition())) {
            SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, this.symbolEnv);
            this.scope = funcEnv;
            this.acceptNode(funcNode.getBody(), funcEnv);
            return;
        }
        funcNode.getAnnotationAttachments().forEach(annotation -> this.acceptNode(annotation, this.symbolEnv));
    }

    // TODO: Add the expression and the external
    @Override
    public void visit(BLangBlockFunctionBody blockFuncBody) {
        if (PositionUtil.withinBlock(this.linePosition, blockFuncBody.getPosition())) {
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
        if (PositionUtil.withinBlock(this.linePosition, classDefinition.getPosition())) {
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
        if (PositionUtil.withinBlock(this.linePosition, objectTypeNode.getPosition())) {
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
        if (PositionUtil.withinBlock(this.linePosition, recordTypeNode.getPosition())) {
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
        if (PositionUtil.withinBlock(this.linePosition, blockNode.getPosition())) {
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
        if (PositionUtil.withinBlock(this.linePosition, exprFuncBody.getPosition())) {
            SymbolEnv exprBodyEnv = SymbolEnv.createFuncBodyEnv(exprFuncBody, symbolEnv);
            this.scope = exprBodyEnv;
            this.acceptNode(exprFuncBody.expr, exprBodyEnv);
        }
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        boolean isFuture = varDefNode.getVariable().expr != null
                && varDefNode.getVariable().expr.type instanceof BFutureType;
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
    public void visit(BLangIf ifNode) {
        if (PositionUtil.withinBlock(this.linePosition, ifNode.getBody().getPosition())) {
            this.scope = this.symbolEnv;
            this.acceptNode(ifNode.body, this.symbolEnv);
        }

        if (ifNode.elseStmt != null) {
            acceptNode(ifNode.elseStmt, this.symbolEnv);
        }
    }

    @Override
    public void visit(BLangWhile whileNode) {
        if (PositionUtil.withinBlock(this.linePosition, whileNode.getPosition())) {
            this.scope = this.symbolEnv;
            this.acceptNode(whileNode.body, this.symbolEnv);
        }
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        SymbolEnv transactionEnv = SymbolEnv.createTransactionEnv(transactionNode, this.symbolEnv);
        this.acceptNode(transactionNode.transactionBody, transactionEnv);

        if (transactionNode.transactionBody != null) {
            this.acceptNode(transactionNode.transactionBody, transactionEnv);
        }
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        if (PositionUtil.withinBlock(this.linePosition, forkJoin.getPosition())) {
            SymbolEnv forkJoinEnv = SymbolEnv.createFolkJoinEnv(forkJoin, this.symbolEnv);
            this.scope = forkJoinEnv;
            forkJoin.workers.forEach(e -> this.acceptNode(e, forkJoinEnv));
        }
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
    }

    @Override
    public void visit(BLangReturn returnNode) {
        this.acceptNode(returnNode.expr, symbolEnv);
    }

    @Override
    public void visit(BLangContinue continueNode) {
    }

    @Override
    public void visit(BLangBreak breakNode) {
    }

    @Override
    public void visit(BLangPanic panicNode) {
        this.acceptNode(panicNode.expr, this.symbolEnv);
    }

    @Override
    public void visit(BLangLock lockNode) {
        this.acceptNode(lockNode.body, symbolEnv);
    }

    @Override
    public void visit(BLangForeach foreach) {
        this.acceptNode(foreach.body, symbolEnv);
    }

    @Override
    public void visit(BLangMatch matchNode) {
        if (PositionUtil.withinBlock(this.linePosition, matchNode.getPosition())) {
            this.scope = this.symbolEnv;
            matchNode.patternClauses.forEach(patternClause -> acceptNode(patternClause, this.symbolEnv));
        }
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        if (!PositionUtil.withinBlock(this.linePosition, annAttachmentNode.getPosition())) {
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
    public void visit(BLangSimpleVarRef simpleVarRef) {
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        if (!PositionUtil.withinBlock(this.linePosition, recordLiteral.getPosition())) {
            return;
        }
        SymbolEnv recordLiteralEnv = new SymbolEnv(recordLiteral, symbolEnv.scope);
        symbolEnv.copyTo(recordLiteralEnv);
        List<RecordLiteralNode.RecordField> fields = recordLiteral.fields;
        this.scope = recordLiteralEnv;
        fields.forEach(keyValue -> this.acceptNode((BLangNode) keyValue, recordLiteralEnv));
    }

    @Override
    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause patternClause) {
        this.acceptNode(patternClause.body, this.symbolEnv);
    }

    @Override
    public void visit(BLangMatch.BLangMatchStructuredBindingPatternClause patternClause) {
        this.acceptNode(patternClause.body, this.symbolEnv);
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
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        this.acceptNode(compoundAssignNode.expr, symbolEnv);
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
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
    public void visit(BLangPackage pkgNode) {
    }

    @Override
    public void visit(BLangTestablePackage testablePkgNode) {
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
    }

    @Override
    public void visit(BLangIdentifier identifierNode) {

    }

    @Override
    public void visit(BLangLock.BLangLockStmt lockStmtNode) {

    }

    @Override
    public void visit(BLangLock.BLangUnLockStmt unLockNode) {

    }

    @Override
    public void visit(BLangFromClause fromClause) {

    }

    @Override
    public void visit(BLangLetClause letClause) {

    }

    @Override
    public void visit(BLangSelectClause selectClause) {

    }

    @Override
    public void visit(BLangWhereClause whereClause) {

    }

    @Override
    public void visit(BLangTryCatchFinally tryNode) {

    }

    @Override
    public void visit(BLangTupleDestructure stmt) {

    }

    @Override
    public void visit(BLangRecordDestructure stmt) {

    }

    @Override
    public void visit(BLangErrorDestructure stmt) {

    }

    @Override
    public void visit(BLangLiteral literalExpr) {

    }

    @Override
    public void visit(BLangConstRef constRef) {

    }

    @Override
    public void visit(BLangNumericLiteral literalExpr) {

    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {

    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {

    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {

    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {

    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {

    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {

    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {

    }

    @Override
    public void visit(BLangWaitExpr awaitExpr) {

    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {

    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {

    }

    @Override
    public void visit(BLangLetExpression letExpr) {

    }

    @Override
    public void visit(BLangLetVariable letVariable) {

    }

    @Override
    public void visit(BLangListConstructorExpr.BLangTupleLiteral tupleLiteral) {

    }

    @Override
    public void visit(BLangListConstructorExpr.BLangArrayLiteral arrayLiteral) {

    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {

    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {

    }

    @Override
    public void visit(BLangXMLQName xmlQName) {

    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {

    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {

    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {

    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {

    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {

    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {

    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {

    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        this.acceptNode(bLangLambdaFunction.function, this.symbolEnv);
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {

    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {

    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {

    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {

    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {

    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {

    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {

    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {

    }

    @Override
    public void visit(BLangIsLikeExpr typeTestExpr) {

    }

    @Override
    public void visit(BLangIgnoreExpr ignoreExpr) {

    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {

    }

    @Override
    public void visit(BLangQueryExpr queryExpr) {

    }

    @Override
    public void visit(BLangValueType valueType) {

    }

    @Override
    public void visit(BLangArrayType arrayType) {

    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType) {

    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {

    }

    @Override
    public void visit(BLangStreamType streamType) {

    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {

    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {

    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {

    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {

    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {

    }

    @Override
    public void visit(BLangErrorType errorType) {

    }

    @Override
    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {

    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {

    }

    @Override
    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {

    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {

    }

    @Override
    public void visit(BLangSimpleVarRef.BLangTypeLoad typeLoad) {

    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {

    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef functionVarRef) {

    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {

    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {

    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTupleAccessExpr arrayIndexAccessExpr) {

    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlAccessExpr) {

    }

    @Override
    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {

    }

    @Override
    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {

    }

    @Override
    public void visit(BLangRecordLiteral.BLangChannelLiteral channelLiteral) {

    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation bFunctionPointerInvocation) {

    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {

    }

    @Override
    public void visit(BLangListConstructorExpr.BLangJSONArrayLiteral jsonArrayLiteral) {

    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {

    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStringAccessExpr stringAccessExpr) {

    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {

    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {

    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {

    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
        this.acceptNode(bLangStatementExpression.stmt, this.symbolEnv);
    }

    @Override
    public void visit(BLangMarkdownDocumentationLine bLangMarkdownDocumentationLine) {

    }

    @Override
    public void visit(BLangMarkdownParameterDocumentation bLangDocumentationParameter) {

    }

    @Override
    public void visit(BLangMarkdownReturnParameterDocumentation bLangMarkdownReturnParameterDocumentation) {

    }

    @Override
    public void visit(BLangMarkDownDeprecationDocumentation bLangMarkDownDeprecationDocumentation) {

    }

    @Override
    public void visit(BLangMarkdownDocumentation bLangMarkdownDocumentation) {

    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {

    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {

    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {

    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {

    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {

    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {

    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {

    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {

    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordSpreadOperatorField spreadOperatorField) {

    }

    @Override
    public void visit(BLangMarkdownReferenceDocumentation bLangMarkdownReferenceDocumentation) {

    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitKeyValue waitKeyValue) {

    }

    @Override
    public void visit(BLangXMLElementFilter xmlElementFilter) {

    }

    @Override
    public void visit(BLangXMLElementAccess xmlElementAccess) {

    }

    @Override
    public void visit(BLangXMLNavigationAccess xmlNavigation) {

    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {

    }

    @Override
    public void visit(BLangFail failNode) {

    }

    @Override
    public void visit(BLangDo doNode) {
        this.acceptNode(doNode.body, symbolEnv);
    }

    @Override
    public void visit(BLangMatchStatement matchStatementNode) {

    }

    @Override
    public void visit(BLangTableKeySpecifier tableKeySpecifierNode) {
    }

    @Override
    public void visit(BLangTableKeyTypeConstraint tableKeyTypeConstraint) {
    }

    @Override
    public void visit(BLangRetry retryNode) {
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction) {
    }

    @Override
    public void visit(BLangRetrySpec retrySpec) {
    }

    @Override
    public void visit(BLangMatchGuard matchGuard) {
    }

    @Override
    public void visit(BLangConstPattern constMatchPattern) {
    }

    @Override
    public void visit(BLangWildCardMatchPattern wildCardMatchPattern) {
    }

    @Override
    public void visit(BLangVarBindingPatternMatchPattern varBindingPattern) {
    }

    @Override
    public void visit(BLangCaptureBindingPattern captureBindingPattern) {
    }

    @Override
    public void visit(BLangListBindingPattern listBindingPattern) {
    }

    @Override
    public void visit(BLangMatch.BLangMatchTypedBindingPatternClause patternClauseNode) {
    }

    @Override
    public void visit(BLangJoinClause joinClause) {
    }

    @Override
    public void visit(BLangOnClause onClause) {
    }

    @Override
    public void visit(BLangOrderKey orderKeyClause) {
    }

    @Override
    public void visit(BLangOrderByClause orderByClause) {
    }

    @Override
    public void visit(BLangOnFailClause onFailClause) {
    }

    @Override
    public void visit(BLangOnConflictClause onConflictClause) {
    }

    @Override
    public void visit(BLangLimitClause limitClause) {
    }

    @Override
    public void visit(BLangMatchClause matchClause) {
    }

    @Override
    public void visit(BLangRollback rollbackNode) {
    }

    @Override
    public void visit(BLangTableConstructorExpr tableConstructorExpr) {
    }

    @Override
    public void visit(BLangRawTemplateLiteral rawTemplateLiteral) {
    }

    @Override
    public void visit(BLangTableMultiKeyExpr tableMultiKeyExpr) {
    }

    @Override
    public void visit(BLangTransactionalExpr transactionalExpr) {
    }

    @Override
    public void visit(BLangCommitExpr commitExpr) {
    }

    @Override
    public void visit(BLangObjectConstructorExpression bLangObjectConstructorExpression) {
    }

    @Override
    public void visit(BLangTableTypeNode tableType) {
    }

    @Override
    public void visit(BLangIntersectionTypeNode intersectionTypeNode) {
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTableAccessExpr tableKeyAccessExpr) {
    }

    @Override
    public void visit(BLangMarkDownDeprecatedParametersDocumentation bLangMarkDownDeprecatedParametersDocumentation) {
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
}

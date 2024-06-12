/*
 *  Copyright (c) 2024, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.desugar;

import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeyTypeConstraint;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.SimpleBLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangCollectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupByClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupingKey;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLimitClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnConflictClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnFailClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderByClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderKey;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAlternateWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCollectContextInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCommitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangDynamicArgExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIgnoreExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInferredTypedescDefaultNode;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMultipleWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangObjectConstructorExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRawTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRegExpTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableConstructorExpr;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerAsyncSendExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerFlushExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerSyncSendExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLNavigationAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLSequenceLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.List;

/**
 * SymbolTransformer for update variable references with newly generated symbols.

 * @since 2201.9.1
 */
public class SymbolTransformer extends SimpleBLangNodeAnalyzer<SymbolTransformer.AnalyzerData> {
    private static final CompilerContext.Key<SymbolTransformer> TRANSFORM_GENERATOR_KEY = new CompilerContext.Key<>();
    private final SymbolTable symTable;
    private final SymbolResolver symResolver;

    public static SymbolTransformer getInstance(CompilerContext context) {
        SymbolTransformer symbolTransformer = context.get(TRANSFORM_GENERATOR_KEY);
        if (symbolTransformer == null) {
            symbolTransformer = new SymbolTransformer(context);
        }

        return symbolTransformer;
    }

    private SymbolTransformer(CompilerContext context) {
        context.put(TRANSFORM_GENERATOR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
    }

    public void transformer(BLangNode node, SymbolEnv env) {
        final AnalyzerData data = new AnalyzerData();
        data.env = env;
        analyzeNode(node, data);
    }

    @Override
    public void visit(BLangPackage pkgNode, AnalyzerData data) {
        data.env = this.symTable.pkgEnvMap.get(pkgNode.symbol);
        pkgNode.globalVars.forEach(globalVar -> analyzeNode(globalVar, data));
        pkgNode.services.forEach(service -> analyzeNode(service, data));
        pkgNode.typeDefinitions.forEach(typeDefinition -> analyzeNode(typeDefinition, data));
        pkgNode.xmlnsList.forEach(xmlns -> analyzeNode(xmlns, data));
        pkgNode.constants.forEach(constant -> analyzeNode(constant, data));
        pkgNode.annotations.forEach(annotation -> analyzeNode(annotation, data));
        pkgNode.classDefinitions.forEach(classDefinition -> analyzeNode(classDefinition, data));
        analyzeNode(pkgNode.initFunction, data);
        for (int i = 0; i < pkgNode.functions.size(); i++) {
            BLangFunction bLangFunction = pkgNode.functions.get(i);
            if (!bLangFunction.flagSet.contains(Flag.LAMBDA)) {
                analyzeNode(bLangFunction, data);
            }
        }
    }

    @Override
    public void visit(BLangFunction funcNode, AnalyzerData data) {
        data.env = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, data.env);
        rewriteParamsAndReturnTypeOfFunction(funcNode, data);
        analyzeNode(funcNode.body, data);
    }

    public void rewriteParamsAndReturnTypeOfFunction(BLangFunction funcNode, AnalyzerData data) {
        for (BLangSimpleVariable bLangSimpleVariable : funcNode.requiredParams) {
            analyzeNode(bLangSimpleVariable, data);
        }

        analyzeNode(funcNode.restParam, data);
        analyzeNode(funcNode.returnTypeNode, data);
    }

    @Override
    public void visit(BLangBlockFunctionBody body, AnalyzerData data) {
        data.env = SymbolEnv.createFuncBodyEnv(body, data.env);
        for (BLangStatement statement : body.stmts) {
            analyzeNode(statement, data);
        }
    }

    @Override
    public void visit(BLangRawTemplateLiteral rawTemplateLiteral, AnalyzerData data) {
    }

    @Override
    public void visit(BLangExprFunctionBody exprBody, AnalyzerData data) {
        analyzeNode(exprBody.expr, data);
    }

    @Override
    public void visit(BLangResourceFunction resourceFunction, AnalyzerData data) {
        visit((BLangFunction) resourceFunction, data);
    }

    @Override
    public void visit(BLangExternalFunctionBody body, AnalyzerData data) {
    }

    @Override
    public void visit(BLangBlockStmt blockNode, AnalyzerData data) {
        for (BLangStatement statement : blockNode.stmts) {
            analyzeNode(statement, data);
        }
    }

    @Override
    public void visit(BLangService serviceNode, AnalyzerData data) {
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode, AnalyzerData data) {
        analyzeNode(varDefNode.var, data);
    }

    @Override
    public void visit(BLangReturn returnNode, AnalyzerData data) {
        analyzeNode(returnNode.expr, data);
    }

    @Override
    public void visit(BLangTypeDefinition typeDef, AnalyzerData data) {
        analyzeNode(typeDef.typeNode, data);
    }

    @Override
    public void visit(BLangIntersectionTypeNode intersectionTypeNode, AnalyzerData data) {
        for (BLangType constituentTypeNode : intersectionTypeNode.constituentTypeNodes) {
            analyzeNode(constituentTypeNode, data);
        }
    }

    @Override
    public void visit(BLangClassDefinition classDefinition, AnalyzerData data) {
        data.env = SymbolEnv.createClassEnv(classDefinition, classDefinition.symbol.scope, data.env);
        for (BLangSimpleVariable bLangSimpleVariable : classDefinition.fields) {
            analyzeNode(bLangSimpleVariable.typeNode, data);
            analyzeNode(bLangSimpleVariable.expr, data);
        }
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode, AnalyzerData data) {
        for (BLangSimpleVariable field : objectTypeNode.fields) {
            analyzeNode(field, data);
        }
    }

    @Override
    public void visit(BLangObjectConstructorExpression objectConstructorExpression, AnalyzerData data) {
        analyzeNode(objectConstructorExpression.typeInit, data);
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode, AnalyzerData data) {
        for (BLangSimpleVariable field : recordTypeNode.fields) {
            analyzeNode(field, data);
        }
        analyzeNode(recordTypeNode.restFieldType, data);
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode, AnalyzerData data) {
        tupleTypeNode.members.forEach(member -> analyzeNode(member, data));
        analyzeNode(tupleTypeNode.restParamType, data);
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode, AnalyzerData data) {
        finiteTypeNode.valueSpace.forEach(param -> analyzeNode(param, data));
    }

    @Override
    public void visit(BLangArrayType arrayType, AnalyzerData data) {
        analyzeNode(arrayType.elemtype, data);
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType, AnalyzerData data) {
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode, AnalyzerData data) {
        unionTypeNode.memberTypeNodes.forEach(typeNode -> analyzeNode(typeNode, data));
    }

    @Override
    public void visit(BLangValueType valueType, AnalyzerData data) {
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefTypeNode, AnalyzerData data) {
    }

    @Override
    public void visit(BLangStreamType streamType, AnalyzerData data) {
        analyzeNode(streamType.constraint, data);
        analyzeNode(streamType.error, data);
    }

    @Override
    public void visit(BLangConstrainedType constrainedType, AnalyzerData data) {
        analyzeNode(constrainedType.constraint, data);
    }

    @Override
    public void visit(BLangErrorType errorType, AnalyzerData data) {
        analyzeNode(errorType.detailType, data);
    }

    @Override
    public void visit(BLangTableTypeNode tableTypeNode, AnalyzerData data) {
        analyzeNode(tableTypeNode.constraint, data);
        analyzeNode(tableTypeNode.tableKeyTypeConstraint, data);
    }

    @Override
    public void visit(BLangInvocation.BLangResourceAccessInvocation resourceAccessInvocation, AnalyzerData data) {
    }

    @Override
    public void visit(BLangTableKeyTypeConstraint keyTypeConstraint, AnalyzerData data) {
        analyzeNode(keyTypeConstraint.keyType, data);
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode, AnalyzerData data) {
        data.env = SymbolEnv.createTypeEnv(functionTypeNode, functionTypeNode.getBType().tsymbol.scope, data.env);
        for (BLangSimpleVariable param : functionTypeNode.params) {
            analyzeNode(param, data);
        }
        analyzeNode(functionTypeNode.restParam, data);
        analyzeNode(functionTypeNode.returnTypeNode, data);
    }

    @Override
    public void visit(BLangSimpleVariable varNode, AnalyzerData data) {
        analyzeNode(varNode.typeNode, data);
        analyzeNode(varNode.expr, data);
    }

    @Override
    public void visit(BLangTupleVariable varNode, AnalyzerData data) {
        analyzeNode(varNode.restVariable, data);
    }

    @Override
    public void visit(BLangRecordVariable varNode, AnalyzerData data) {
        analyzeNode(varNode.expr, data);
    }

    @Override
    public void visit(BLangErrorVariable varNode, AnalyzerData data) {
        analyzeNode(varNode.expr, data);
    }

    @Override
    public void visit(BLangTupleVariableDef varDefNode, AnalyzerData data) {
        analyzeNode(varDefNode.var, data);
    }

    @Override
    public void visit(BLangRecordVariableDef varDefNode, AnalyzerData data) {
        analyzeNode(varDefNode.var, data);
    }

    @Override
    public void visit(BLangErrorVariableDef varDefNode, AnalyzerData data) {
        analyzeNode(varDefNode.errorVariable, data);
    }

    @Override
    public void visit(BLangAssignment assignNode, AnalyzerData data) {
        analyzeNode(assignNode.varRef, data);
        analyzeNode(assignNode.expr, data);
    }

    @Override
    public void visit(BLangTupleDestructure tupleDestructure, AnalyzerData data) {
    }

    @Override
    public void visit(BLangRecordDestructure recordDestructure, AnalyzerData data) {
    }

    @Override
    public void visit(BLangErrorDestructure errorDestructure, AnalyzerData data) {
    }

    @Override
    public void visit(BLangRetry retryNode, AnalyzerData data) {
        analyzeNode(retryNode.retryBody, data);
        analyzeNode(retryNode.onFailClause, data);
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction, AnalyzerData data) {
        analyzeNode(retryTransaction.transaction, data);
    }

    @Override
    public void visit(BLangContinue nextNode, AnalyzerData data) {
    }

    @Override
    public void visit(BLangBreak breakNode, AnalyzerData data) {
    }

    @Override
    public void visit(BLangPanic panicNode, AnalyzerData data) {
        analyzeNode(panicNode.expr, data);
    }

    @Override
    public void visit(BLangDo doNode, AnalyzerData data) {
        analyzeNode(doNode.body, data);
        analyzeNode(doNode.onFailClause, data);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode, AnalyzerData data) {
        analyzeNode(xmlnsStmtNode.xmlnsDecl, data);
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode, AnalyzerData data) {
        analyzeNode(xmlnsNode.namespaceURI, data);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode, AnalyzerData data) {
        analyzeNode(exprStmtNode.expr, data);
    }

    @Override
    public void visit(BLangFail failNode, AnalyzerData data) {
        analyzeNode(failNode.expr, data);
        analyzeNode(failNode.exprStmt, data);
    }

    @Override
    public void visit(BLangIf ifNode, AnalyzerData data) {
        analyzeNode(ifNode.expr, data);
        analyzeNode(ifNode.body, data);
        analyzeNode(ifNode.elseStmt, data);
    }

    @Override
    public void visit(BLangForeach foreach, AnalyzerData data) {
    }

    @Override
    public void visit(BLangWhile whileNode, AnalyzerData data) {
        analyzeNode(whileNode.expr, data);
        analyzeNode(whileNode.body, data);
        analyzeNode(whileNode.onFailClause, data);
    }

    @Override
    public void visit(BLangLock lockNode, AnalyzerData data) {
        analyzeNode(lockNode.body, data);
    }

    @Override
    public void visit(BLangLock.BLangLockStmt lockNode, AnalyzerData data) {
    }

    @Override
    public void visit(BLangLock.BLangUnLockStmt unLockNode, AnalyzerData data) {
    }

    @Override
    public void visit(BLangTransaction transactionNode, AnalyzerData data) {
        analyzeNode(transactionNode.transactionBody, data);
    }

    @Override
    public void visit(BLangRollback rollbackNode, AnalyzerData data) {
        analyzeNode(rollbackNode.expr, data);
    }

    @Override
    public void visit(BLangTransactionalExpr transactionalExpr, AnalyzerData data) {
    }

    @Override
    public void visit(BLangCommitExpr commitExpr, AnalyzerData data) {
    }

    @Override
    public void visit(BLangForkJoin forkJoin, AnalyzerData data) {
    }

    @Override
    public void visit(BLangLiteral literalExpr, AnalyzerData data) {
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr, AnalyzerData data) {
        for (BLangExpression expr : listConstructorExpr.exprs) {
            analyzeNode(expr, data);
        }
    }

    @Override
    public void visit(BLangTableConstructorExpr tableConstructorExpr, AnalyzerData data) {
        analyzeNodeList(tableConstructorExpr.recordLiteralList, data);
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral, AnalyzerData data) {
        for (RecordLiteralNode.RecordField field : recordLiteral.fields) {
            if (field.isKeyValueField()) {
                BLangRecordLiteral.BLangRecordKeyValueField keyValueField =
                                                                    (BLangRecordLiteral.BLangRecordKeyValueField) field;
                analyzeNode(keyValueField.key.expr, data);
                analyzeNode(keyValueField.valueExpr, data);
            } else if (field.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                analyzeNode((BLangSimpleVarRef) field, data);
            } else {
                BLangRecordLiteral.BLangRecordSpreadOperatorField spreadOpField =
                                                              (BLangRecordLiteral.BLangRecordSpreadOperatorField) field;
                analyzeNode(spreadOpField.expr, data);
            }
        }
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr, AnalyzerData data) {
        BSymbol symbol = symResolver.lookupSymbolInGivenScope(data.env, varRefExpr.symbol.name, SymTag.VARIABLE_NAME);
        if (symbol != symTable.notFoundSymbol) {
            varRefExpr.symbol = symbol;
        }
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr, AnalyzerData data) {
        analyzeNode(fieldAccessExpr.expr, data);
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr, AnalyzerData data) {
        analyzeNode(indexAccessExpr.indexExpr, data);
        analyzeNode(indexAccessExpr.expr, data);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignment, AnalyzerData data) {
        analyzeNode(compoundAssignment.varRef, data);
        analyzeNode(compoundAssignment.expr, data);
    }

    @Override
    public void visit(BLangInvocation invocation, AnalyzerData data) {
        rewriteInvocationExpr(invocation, data);
    }

    public void rewriteInvocationExpr(BLangInvocation invocation, AnalyzerData data) {
        analyzeNodeList(invocation.requiredArgs, data);
        analyzeNodeList(invocation.argExprs, data);
    }

    @Override
    public void visit(BLangQueryAction queryAction, AnalyzerData data) {
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkedExpr, AnalyzerData data) {
    }

    @Override
    public void visit(BLangErrorConstructorExpr errorConstructorExpr, AnalyzerData data) {
        analyzeNodeList(errorConstructorExpr.positionalArgs, data);
        analyzeNode(errorConstructorExpr.errorDetail, data);
    }

    @Override
    public void visit(BLangTypeInit typeInitExpr, AnalyzerData data) {
        analyzeNode(typeInitExpr.initInvocation, data);
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr, AnalyzerData data) {
        analyzeNode(ternaryExpr.expr, data);
        analyzeNode(ternaryExpr.thenExpr, data);
        analyzeNode(ternaryExpr.elseExpr, data);
    }

    @Override
    public void visit(BLangWaitExpr waitExpr, AnalyzerData data) {
        waitExpr.exprList.forEach(expression -> analyzeNode(expression, data));
    }

    @Override
    public void visit(BLangWaitForAllExpr waitExpr, AnalyzerData data) {
    }

    @Override
    public void visit(BLangTrapExpr trapExpr, AnalyzerData data) {
        analyzeNode(trapExpr.expr, data);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr, AnalyzerData data) {
        analyzeNode(binaryExpr.lhsExpr, data);
        analyzeNode(binaryExpr.rhsExpr, data);
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr, AnalyzerData data) {
    }

    @Override
    public void visit(BLangGroupExpr groupExpr, AnalyzerData data) {
        analyzeNode(groupExpr.expression, data);
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr, AnalyzerData data) {
        analyzeNode(unaryExpr.expr, data);
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr, AnalyzerData data) {
        analyzeNode(conversionExpr.expr, data);
        analyzeNode(conversionExpr.typeNode, data);
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction, AnalyzerData data) {
        data.env = bLangLambdaFunction.capturedClosureEnv;
        analyzeNode(bLangLambdaFunction.function, data);
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction, AnalyzerData data) {
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation invocation, AnalyzerData data) {
        rewriteInvocationExpr(invocation, data);
    }

    @Override
    public void visit(BLangXMLQName xmlQName, AnalyzerData data) {
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute, AnalyzerData data) {
        analyzeNode(xmlAttribute.name, data);
        analyzeNode(xmlAttribute.value, data);
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral, AnalyzerData data) {
        analyzeNode(xmlElementLiteral.startTagName, data);
        analyzeNode(xmlElementLiteral.endTagName, data);
        analyzeNodeList(xmlElementLiteral.modifiedChildren, data);
        analyzeNodeList(xmlElementLiteral.attributes, data);
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral, AnalyzerData data) {
        analyzeNodeList(xmlTextLiteral.textFragments, data);
        analyzeNode(xmlTextLiteral.concatExpr, data);
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral, AnalyzerData data) {
        analyzeNodeList(xmlCommentLiteral.textFragments, data);
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral, AnalyzerData data) {
        analyzeNode(xmlProcInsLiteral.target, data);
        analyzeNodeList(xmlProcInsLiteral.dataFragments, data);
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString, AnalyzerData data) {
        analyzeNodeList(xmlQuotedString.textFragments, data);
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral, AnalyzerData data) {
        analyzeNodeList(stringTemplateLiteral.exprs, data);
    }

    @Override
    public void visit(BLangWorkerAsyncSendExpr asyncSendExpr, AnalyzerData data) {
        analyzeNode(asyncSendExpr.expr, data);
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr, AnalyzerData data) {
        analyzeNode(syncSendExpr.expr, data);
    }

    @Override
    public void visit(BLangAlternateWorkerReceive alternateWorkerReceive, AnalyzerData data) {
    }

    @Override
    public void visit(BLangMultipleWorkerReceive multipleWorkerReceive, AnalyzerData data) {
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode, AnalyzerData data) {
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr, AnalyzerData data) {
    }

    @Override
    public void visit(BLangIgnoreExpr ignoreExpr, AnalyzerData data) {
    }

    @Override
    public void visit(BLangDynamicArgExpr dynamicParamExpr, AnalyzerData data) {
        analyzeNode(dynamicParamExpr.condition, data);
        analyzeNode(dynamicParamExpr.conditionalArgument, data);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef, AnalyzerData data) {
    }

    @Override
    public void visit(BLangConstRef constRef, AnalyzerData data) {
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef, AnalyzerData data) {
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr, AnalyzerData data) {
        analyzeNode(fieldAccessExpr.indexExpr, data);
        analyzeNode(fieldAccessExpr.expr, data);
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef functionVarRef, AnalyzerData data) {
        analyzeNode(functionVarRef.expr, data);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr, AnalyzerData data) {
        analyzeNode(mapKeyAccessExpr.indexExpr, data);
        analyzeNode(mapKeyAccessExpr.expr, data);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTableAccessExpr tableKeyAccessExpr, AnalyzerData data) {
        analyzeNode(tableKeyAccessExpr.indexExpr, data);
        analyzeNode(tableKeyAccessExpr.expr, data);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr, AnalyzerData data) {
        analyzeNode(arrayIndexAccessExpr.indexExpr, data);
        analyzeNode(arrayIndexAccessExpr.expr, data);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTupleAccessExpr arrayIndexAccessExpr, AnalyzerData data) {
        analyzeNode(arrayIndexAccessExpr.indexExpr, data);
        analyzeNode(arrayIndexAccessExpr.expr, data);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlIndexAccessExpr, AnalyzerData data) {
        analyzeNode(xmlIndexAccessExpr.indexExpr, data);
        analyzeNode(xmlIndexAccessExpr.expr, data);
    }

    @Override
    public void visit(BLangXMLElementAccess xmlElementAccess, AnalyzerData data) {
        analyzeNode(xmlElementAccess.expr, data);
    }

    @Override
    public void visit(BLangXMLNavigationAccess xmlNavigation, AnalyzerData data) {
        analyzeNode(xmlNavigation.expr, data);
        analyzeNode(xmlNavigation.childIndex, data);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr, AnalyzerData data) {
        analyzeNode(jsonAccessExpr.indexExpr, data);
        analyzeNode(jsonAccessExpr.expr, data);
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStringAccessExpr stringAccessExpr, AnalyzerData data) {
        analyzeNode(stringAccessExpr.indexExpr, data);
        analyzeNode(stringAccessExpr.expr, data);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral, AnalyzerData data) {
        for (RecordLiteralNode.RecordField field : mapLiteral.fields) {
            if (field.isKeyValueField()) {
                BLangRecordLiteral.BLangRecordKeyValueField keyValueField =
                        (BLangRecordLiteral.BLangRecordKeyValueField) field;
                analyzeNode(keyValueField.key.expr, data);
                analyzeNode(keyValueField.valueExpr, data);
                continue;
            }
            analyzeNode(((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr, data);
        }
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral, AnalyzerData data) {
        for (RecordLiteralNode.RecordField field : structLiteral.fields) {
            if (field.isKeyValueField()) {
                BLangRecordLiteral.BLangRecordKeyValueField keyValueField =
                        (BLangRecordLiteral.BLangRecordKeyValueField) field;
                analyzeNode(keyValueField.key.expr, data);
                analyzeNode(keyValueField.valueExpr, data);
                continue;
            }
            analyzeNode(((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr, data);
        }
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr, AnalyzerData data) {
        analyzeNode(assignableExpr.lhsExpr, data);
    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation fpInvocation, AnalyzerData data) {
        analyzeNode(fpInvocation.expr, data);
        rewriteInvocationExpr(fpInvocation, data);
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr, AnalyzerData data) {
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression, AnalyzerData data) {
        analyzeNode(bLangVarArgsExpression.expr, data);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression, AnalyzerData data) {
        analyzeNode(bLangNamedArgsExpression.expr, data);
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr, AnalyzerData data) {
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr, AnalyzerData data) {
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr, AnalyzerData data) {
        analyzeNode(typeTestExpr.typeNode, data);
        analyzeNode(typeTestExpr.expr, data);
    }

    @Override
    public void visit(BLangIsLikeExpr isLikeExpr, AnalyzerData data) {
        analyzeNode(isLikeExpr.expr, data);
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangNSPrefixedFieldBasedAccess nsPrefixedFieldBasedAccess,
                      AnalyzerData data) {
    }

    @Override
    public void visit(BLangLetExpression letExpression, AnalyzerData data) {
        for (BLangLetVariable letVariable : letExpression.letVarDeclarations) {
            analyzeNode((BLangNode) letVariable.definitionNode, data);
        }
        analyzeNode(letExpression.expr, data);
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr, AnalyzerData data) {
        analyzeNode(annotAccessExpr.expr, data);
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression, AnalyzerData data) {
        if (bLangStatementExpression.stmt.getKind() == NodeKind.BLOCK) {
            BLangBlockStmt bLangBlockStmt = (BLangBlockStmt) bLangStatementExpression.stmt;
            for (BLangStatement statement : bLangBlockStmt.stmts) {
                analyzeNode(statement, data);
            }
        } else {
            analyzeNode(bLangStatementExpression.stmt, data);
        }
        analyzeNode(bLangStatementExpression.expr, data);
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation invocation, AnalyzerData data) {
        rewriteInvocationExpr(invocation, data);
    }

    @Override
    public void visit(BLangIdentifier identifierNode, AnalyzerData data) {
        /* ignore */
    }

    @Override
    public void visit(BLangAnnotation annotationNode, AnalyzerData data) {
        /* ignore */
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode, AnalyzerData data) {
        /* ignore */
    }

    @Override
    public void visit(BLangConstant constant, AnalyzerData data) {
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr, AnalyzerData data) {
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr, AnalyzerData data) {
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr, AnalyzerData data) {
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangTypeLoad typeLoad, AnalyzerData data) {
    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode, AnalyzerData data) {
        analyzeNode(xmlnsNode.namespaceURI, data);
    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode, AnalyzerData data) {
        analyzeNode(xmlnsNode.namespaceURI, data);
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangListConstructorSpreadOpExpr listConstructorSpreadOpExpr,
                      AnalyzerData data) {
        analyzeNode(listConstructorSpreadOpExpr.expr, data);
    }

    @Override
    public void visit(BLangQueryExpr queryExpr, AnalyzerData data) {
        for (BLangNode clause : queryExpr.getQueryClauses()) {
            analyzeNode(clause, data);
        }
    }

    @Override
    public void visit(BLangFromClause fromClause, AnalyzerData data) {
        BLangExpression collection = fromClause.collection;
        analyzeNode(collection, data);
    }

    @Override
    public void visit(BLangJoinClause joinClause, AnalyzerData data) {
        analyzeNode(joinClause.collection, data);
        analyzeNode(joinClause.onClause, data);
    }

    @Override
    public void visit(BLangLetClause letClause, AnalyzerData data) {
        for (BLangLetVariable letVariable : letClause.letVarDeclarations) {
            analyzeNode((BLangNode) letVariable.definitionNode, data);
        }
    }

    @Override
    public void visit(BLangWhereClause whereClause, AnalyzerData data) {
        analyzeNode(whereClause.expression, data);
    }

    @Override
    public void visit(BLangOnClause onClause, AnalyzerData data) {
        analyzeNode(onClause.lhsExpr, data);
        analyzeNode(onClause.rhsExpr, data);
    }

    @Override
    public void visit(BLangOrderKey orderKeyClause, AnalyzerData data) {
        analyzeNode(orderKeyClause.expression, data);
    }

    @Override
    public void visit(BLangOrderByClause orderByClause, AnalyzerData data) {
        orderByClause.orderByKeyList.forEach(value -> analyzeNode((BLangNode) value, data));
    }

    @Override
    public void visit(BLangGroupByClause groupByClause, AnalyzerData data) {
        groupByClause.groupingKeyList.forEach(value -> analyzeNode(value, data));
    }

    @Override
    public void visit(BLangGroupingKey groupingKey, AnalyzerData data) {
        analyzeNode((BLangNode) groupingKey.getGroupingKey(), data);
    }

    @Override
    public void visit(BLangSelectClause selectClause, AnalyzerData data) {
        analyzeNode(selectClause.expression, data);
    }

    @Override
    public void visit(BLangCollectClause bLangCollectClause, AnalyzerData data) {
        analyzeNode(bLangCollectClause.expression, data);
    }

    @Override
    public void visit(BLangOnConflictClause onConflictClause, AnalyzerData data) {
        analyzeNode(onConflictClause.expression, data);
    }

    @Override
    public void visit(BLangLimitClause limitClause, AnalyzerData data) {
        analyzeNode(limitClause.expression, data);
    }

    @Override
    public void visit(BLangOnFailClause onFailClause, AnalyzerData data) {
        analyzeNode(onFailClause.body, data);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangLocalVarRef node, AnalyzerData data) {
    }

    @Override
    public void visit(BLangCollectContextInvocation collectContextInvocation, AnalyzerData data) {
        analyzeNode(collectContextInvocation.invocation, data);
    }

    @Override
    public void visit(BLangMatchStatement matchStatement, AnalyzerData data) {
    }

    @Override
    public void visit(BLangXMLSequenceLiteral xmlSequenceLiteral, AnalyzerData data) {
        analyzeNodeList(xmlSequenceLiteral.xmlItems, data);
    }

    @Override
    public void visit(BLangRegExpTemplateLiteral regExpTemplateLiteral, AnalyzerData data) {
        List<BLangExpression> interpolationsList =
                symResolver.getListOfInterpolations(regExpTemplateLiteral.reDisjunction.sequenceList);
        analyzeNodeList(interpolationsList, data);
    }

    @Override
    public void visit(BLangMarkdownDocumentationLine bLangMarkdownDocumentationLine, AnalyzerData data) {
    }

    @Override
    public void visit(BLangMarkdownParameterDocumentation bLangDocumentationParameter, AnalyzerData data) {
    }

    @Override
    public void visit(BLangMarkdownReturnParameterDocumentation bLangMarkdownReturnParameterDocumentation,
                      AnalyzerData data) {
    }

    @Override
    public void visit(BLangInferredTypedescDefaultNode inferTypedescExpr, AnalyzerData data) {
    }

    @Override
    public void visit(BLangMarkdownDocumentation bLangMarkdownDocumentation, AnalyzerData data) {
    }

    @Override
    public void analyzeNode(BLangNode node, AnalyzerData data) {
        SymbolEnv prevEnv = data.env;
        visitNode(node, data);
        data.env = prevEnv;
    }

    private <E extends BLangExpression> void analyzeNodeList(List<E> nodeList, AnalyzerData data) {
        for (E e : nodeList) {
            analyzeNode(e, data);
        }
    }

    public static class AnalyzerData {
        SymbolEnv env;
    }
}

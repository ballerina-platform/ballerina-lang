/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.desugar;

import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownReferenceDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCommitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRollback;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Visitor class to figure out the closure variables.
 *
 * @since 2.0.0-preview1
 */
public class ClosureExpressionVisitor extends BLangNodeVisitor {

    private BVarSymbol currentFrameSymbol;
    private BLangBlockFunctionBody currentLambdaBody;
    private Map<String, BSymbol> identifiers;
    private final SymbolResolver symResolver;
    private final Names names;
    private final Types types;
    private final Desugar desugar;
    private SymbolEnv env;
    private final SymbolTable symTable;
    private static final String FRAME_PARAMETER_NAME = "$frame$";
    private boolean isTransactionDesugar;

    ClosureExpressionVisitor(CompilerContext context, SymbolEnv env, boolean isTransactionDesugar) {
        this.symResolver = SymbolResolver.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
        this.names = Names.getInstance(context);
        this.types = Types.getInstance(context);
        this.desugar = Desugar.getInstance(context);
        this.env = env;
        this.isTransactionDesugar = isTransactionDesugar;
    }

    @Override
    public void visit(BLangLambdaFunction lambda) {
        BLangFunction function = lambda.function;
        if (!isTransactionDesugar) {
            currentFrameSymbol = function.requiredParams.get(0).symbol;
        }
        identifiers = new HashMap<>();
        currentLambdaBody = (BLangBlockFunctionBody) function.getBody();
        List<BLangStatement> stmts = new ArrayList<>(currentLambdaBody.getStatements());
        stmts.forEach(stmt -> {
            stmt.accept(this);
        });
        currentFrameSymbol = null;
        identifiers = null;
        currentLambdaBody = null;
    }

    @Override
    public void visit(BLangSimpleVariableDef bLangSimpleVariableDef) {
        bLangSimpleVariableDef.getVariable().accept(this);
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        bLangRecordVariableDef.var.accept(this);
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
        bLangRecordVariable.variableList.forEach(v -> v.getValue().accept(this));
        if (bLangRecordVariable.expr != null) {
            bLangRecordVariable.expr.accept(this);
        }
        if (bLangRecordVariable.hasRestParam()) {
            ((BLangNode) bLangRecordVariable.restParam).accept(this);
        }
    }

    @Override
    public void visit(BLangSimpleVariable bLangSimpleVariable) {
        identifiers.putIfAbsent(bLangSimpleVariable.name.value, bLangSimpleVariable.symbol);
        if (bLangSimpleVariable.expr != null) {
            bLangSimpleVariable.expr.accept(this);
        }
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        conversionExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        fieldAccessExpr.expr.accept(this);
        if (fieldAccessExpr.impConversionExpr != null) {
            fieldAccessExpr.impConversionExpr.expr.accept(this);
        }
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        exprStmtNode.expr.accept(this);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        List<BLangExpression> requiredArgs = invocationExpr.requiredArgs;
        if (invocationExpr.langLibInvocation) {
            requiredArgs = requiredArgs.subList(1, requiredArgs.size());
        }
        requiredArgs.forEach(arg -> arg.accept(this));
        invocationExpr.restArgs.forEach(arg -> arg.accept(this));
        if (invocationExpr.expr != null) {
            invocationExpr.expr.accept(this);
        }
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        // do nothing;
    }

    @Override
    public void visit(BLangReturn bLangReturn) {
        bLangReturn.expr.accept(this);
    }

    @Override
    public void visit(BLangBinaryExpr bLangBinaryExpr) {
        bLangBinaryExpr.lhsExpr.accept(this);
        bLangBinaryExpr.rhsExpr.accept(this);
    }

    @Override
    public void visit(BLangAssignment bLangAssignment) {
        bLangAssignment.varRef.accept(this);
        bLangAssignment.expr.accept(this);
    }

    @Override
    public void visit(BLangRecordLiteral bLangRecordLiteral) {
        for (RecordLiteralNode.RecordField field : bLangRecordLiteral.fields) {
            ((BLangNode) field).accept(this);
        }
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordKeyValueField recordKeyValue) {
        recordKeyValue.key.expr.accept(this);
        recordKeyValue.valueExpr.accept(this);
    }

    @Override
    public void visit(BLangRecordLiteral.BLangRecordSpreadOperatorField spreadOperatorField) {
        spreadOperatorField.expr.accept(this);
    }

    public void visit(BLangConstRef constRef) {
        //do nothing
    }

    public void visit(BLangNumericLiteral literalExpr) {
        //do nothing
    }

    public void visit(BLangTupleVarRef varRefExpr) {
        varRefExpr.expressions.forEach(expression -> expression.accept(this));
        if (varRefExpr.restParam != null) {
            BLangExpression restExpr = (BLangExpression) varRefExpr.restParam;
            restExpr.accept(this);
        }
    }

    public void visit(BLangRecordVarRef varRefExpr) {
        varRefExpr.recordRefFields.forEach(recordVarRefKeyValue
                -> recordVarRefKeyValue.variableReference.accept(this));
        if (varRefExpr.restParam != null) {
            BLangExpression restExpr = (BLangExpression) varRefExpr.restParam;
            restExpr.accept(this);
        }
    }

    public void visit(BLangErrorVarRef varRefExpr) {
        if (varRefExpr.reason != null) {
            varRefExpr.reason.accept(this);
        }
        if (varRefExpr.restVar != null) {
            varRefExpr.restVar.accept(this);
        }
        varRefExpr.detail.forEach(bLangNamedArgsExpression -> bLangNamedArgsExpression.accept(this));
    }

    public void visit(BLangSimpleVarRef bLangSimpleVarRef) {
        BSymbol symbol = bLangSimpleVarRef.symbol;
        BSymbol resolvedSymbol = symResolver
                .lookupClosureVarSymbol(env, names.fromIdNode(bLangSimpleVarRef.variableName),
                        SymTag.VARIABLE);
        if (symbol != null && resolvedSymbol == symTable.notFoundSymbol) {
            String identifier = bLangSimpleVarRef.variableName.getValue();
            if (!FRAME_PARAMETER_NAME.equals(identifier) && !identifiers.containsKey(identifier)) {
                DiagnosticPos pos = currentLambdaBody.pos;
                BLangFieldBasedAccess frameAccessExpr = desugar.getFieldAccessExpression(pos, identifier,
                        symTable.anyOrErrorType, currentFrameSymbol);
                frameAccessExpr.expr = desugar.addConversionExprIfRequired(frameAccessExpr.expr,
                        types.getSafeType(frameAccessExpr.expr.type, true, false));

                if (symbol instanceof BVarSymbol) {
                    ((BVarSymbol) symbol).originalSymbol = null;
                    BLangSimpleVariable variable = ASTBuilderUtil.createVariable(pos, identifier, symbol.type,
                            desugar.addConversionExprIfRequired(frameAccessExpr, symbol.type), (BVarSymbol) symbol);
                    BLangSimpleVariableDef variableDef = ASTBuilderUtil.createVariableDef(pos, variable);
                    currentLambdaBody.stmts.add(0, variableDef);
                }
                identifiers.put(identifier, symbol);
            }
        } else if (resolvedSymbol != symTable.notFoundSymbol) {
            resolvedSymbol.closure = true;
        }
    }

    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        indexAccessExpr.indexExpr.accept(this);
        indexAccessExpr.expr.accept(this);
    }

    public void visit(BLangTypeInit connectorInitExpr) {
        connectorInitExpr.argsExpr.forEach(arg -> arg.accept(this));
        connectorInitExpr.initInvocation.accept(this);
    }

    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        actionInvocationExpr.argExprs.forEach(arg -> arg.accept(this));
    }

    public void visit(BLangTernaryExpr ternaryExpr) {
        ternaryExpr.expr.accept(this);
        ternaryExpr.elseExpr.accept(this);
        ternaryExpr.thenExpr.accept(this);
    }

    public void visit(BLangWaitExpr awaitExpr) {
        awaitExpr.exprList.forEach(expression -> expression.accept(this));
    }

    public void visit(BLangTrapExpr trapExpr) {
        trapExpr.expr.accept(this);
    }

    public void visit(BLangElvisExpr elvisExpr) {
        elvisExpr.lhsExpr.accept(this);
        elvisExpr.rhsExpr.accept(this);
    }

    public void visit(BLangGroupExpr groupExpr) {
        groupExpr.expression.accept(this);
    }

    public void visit(BLangLetExpression letExpr) {
        letExpr.expr.accept(this);
        letExpr.letVarDeclarations.forEach(var -> ((BLangNode) var.definitionNode).accept(this));
    }

    public void visit(BLangLetVariable letVariable) {
        //do nothing
    }

    public void visit(BLangListConstructorExpr listConstructorExpr) {
        listConstructorExpr.exprs.forEach(expression -> expression.accept(this));
    }

    public void visit(BLangListConstructorExpr.BLangTupleLiteral tupleLiteral) {
        tupleLiteral.exprs.forEach(expression -> expression.accept(this));
    }

    public void visit(BLangListConstructorExpr.BLangArrayLiteral arrayLiteral) {
        arrayLiteral.exprs.forEach(expression -> expression.accept(this));
    }

    public void visit(BLangUnaryExpr unaryExpr) {
        unaryExpr.expr.accept(this);
    }

    public void visit(BLangTypedescExpr accessExpr) {
    }

    public void visit(BLangXMLQName xmlQName) {
    }

    public void visit(BLangXMLAttribute xmlAttribute) {
    }

    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        xmlElementLiteral.startTagName.accept(this);
        xmlElementLiteral.endTagName.accept(this);
        xmlElementLiteral.attributes.forEach(bLangXMLAttribute -> bLangXMLAttribute.accept(this));
        xmlElementLiteral.children.forEach(child -> child.accept(this));
    }

    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        xmlTextLiteral.textFragments.forEach(fragment -> fragment.accept(this));
        if (xmlTextLiteral.concatExpr != null) {
            xmlTextLiteral.concatExpr.accept(this);
        }
    }

    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        xmlCommentLiteral.textFragments.forEach(fragment -> fragment.accept(this));
        if (xmlCommentLiteral.concatExpr != null) {
            xmlCommentLiteral.concatExpr.accept(this);
        }
    }

    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        xmlProcInsLiteral.dataFragments.forEach(fragment -> fragment.accept(this));
        if (xmlProcInsLiteral.dataConcatExpr != null) {
            xmlProcInsLiteral.dataConcatExpr.accept(this);
        }
    }

    public void visit(BLangXMLQuotedString xmlQuotedString) {
        xmlQuotedString.textFragments.forEach(fragment -> fragment.accept(this));
        if (xmlQuotedString.concatExpr != null) {
            xmlQuotedString.concatExpr.accept(this);
        }
    }

    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        stringTemplateLiteral.exprs.forEach(expression -> expression.accept(this));
    }

    public void visit(BLangArrowFunction bLangArrowFunction) {
        bLangArrowFunction.params.forEach(param -> param.accept(this));
        bLangArrowFunction.function.accept(this);
        bLangArrowFunction.body.accept(this);
    }

    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
    }

    public void visit(BLangIntRangeExpression intRangeExpression) {
        intRangeExpression.startExpr.accept(this);
        intRangeExpression.endExpr.accept(this);
    }

    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        bLangVarArgsExpression.expr.accept(this);
    }

    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        bLangNamedArgsExpression.expr.accept(this);
    }

    public void visit(BLangIsAssignableExpr assignableExpr) {
        assignableExpr.lhsExpr.accept(this);
    }

    public void visit(BLangMatchExpression bLangMatchExpression) {
        bLangMatchExpression.expr.accept(this);
        bLangMatchExpression.patternClauses.forEach(bLangMatchExprPatternClause ->
                bLangMatchExpression.patternClauses.forEach(pattern -> pattern.expr.accept(this)));
        bLangMatchExpression.patternClauses.forEach(bLangMatchExprPatternClause ->
                bLangMatchExpression.patternClauses.forEach(pattern -> pattern.variable.accept(this)));
        bLangMatchExpression.patternClauses.forEach(bLangMatchExprPatternClause ->
                bLangMatchExpression.expr.accept(this));
    }

    public void visit(BLangMatchExpression.BLangMatchExprPatternClause bLangMatchExprPatternClause) {
    }

    public void visit(BLangCheckedExpr checkedExpr) {
        checkedExpr.expr.accept(this);
    }

    public void visit(BLangCheckPanickedExpr checkPanickedExpr) {
        checkPanickedExpr.expr.accept(this);
    }

    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        serviceConstructorExpr.serviceNode.accept(this);
    }

    public void visit(BLangTypeTestExpr typeTestExpr) {
        typeTestExpr.expr.accept(this);
    }

    public void visit(BLangIsLikeExpr typeTestExpr) {
        typeTestExpr.expr.accept(this);
    }

    public void visit(BLangIgnoreExpr ignoreExpr) {
    }

    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
    }

    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
    }

    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
    }

    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
        bLangXMLSequenceLiteral.xmlItems.forEach(item -> item.accept(this));
    }

    public void visit(BLangStatementExpression bLangStatementExpression) {
        bLangStatementExpression.expr.accept(this);
        bLangStatementExpression.stmt.accept(this);
    }

    public void visit(BLangTupleVariable bLangTupleVariable) {
        if (bLangTupleVariable.restVariable != null) {
            bLangTupleVariable.restVariable.accept(this);
        }
        bLangTupleVariable.memberVariables.forEach(var -> var.accept(this));
    }

    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        if (bLangTupleVariableDef.var.restVariable != null) {
            bLangTupleVariableDef.var.restVariable.accept(this);
        }
        if (bLangTupleVariableDef.var.expr != null) {
            bLangTupleVariableDef.var.expr.accept(this);
        }
        if (bLangTupleVariableDef.var.memberVariables != null) {
            bLangTupleVariableDef.var.memberVariables.forEach(var -> var.accept(this));
        }
    }

    public void visit(BLangErrorVariable bLangErrorVariable) {
        if (bLangErrorVariable.reason != null) {
            bLangErrorVariable.reason.accept(this);
        }
        bLangErrorVariable.detail.forEach(var -> var.valueBindingPattern.accept(this));
        if (bLangErrorVariable.restDetail != null) {
            bLangErrorVariable.restDetail.accept(this);
        }
        if (bLangErrorVariable.detailExpr != null) {
            bLangErrorVariable.detailExpr.accept(this);
        }
    }

    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        bLangErrorVariableDef.errorVariable.accept(this);
    }

    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause bLangMatchStmtStaticBindingPatternClause) {
        bLangMatchStmtStaticBindingPatternClause.literal.accept(this);
    }

    public void visit(BLangMatch.BLangMatchStructuredBindingPatternClause
                              bLangMatchStmtStructuredBindingPatternClause) {
        if (bLangMatchStmtStructuredBindingPatternClause.bindingPatternVariable != null) {
            bLangMatchStmtStructuredBindingPatternClause.bindingPatternVariable.accept(this);
        }
        if (bLangMatchStmtStructuredBindingPatternClause.typeGuardExpr != null) {
            bLangMatchStmtStructuredBindingPatternClause.typeGuardExpr.accept(this);
        }
    }

    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
    }

    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
    }

    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        waitForAllExpr.keyValuePairs.forEach(pair -> pair.accept(this));
    }

    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {
    }

    public void visit(BLangMarkdownReferenceDocumentation bLangMarkdownReferenceDocumentation) {
    }

    public void visit(BLangWaitForAllExpr.BLangWaitKeyValue waitKeyValue) {
        waitKeyValue.key.accept(this);
        waitKeyValue.valueExpr.accept(this);
    }

    public void visit(BLangXMLElementFilter xmlElementFilter) {
    }

    public void visit(BLangXMLElementAccess xmlElementAccess) {
    }

    public void visit(BLangXMLNavigationAccess xmlNavigation) {
        if (xmlNavigation.childIndex != null) {
            xmlNavigation.childIndex.accept(this);
        }
    }

    //statements
    public void visit(BLangBlockStmt blockNode) {
        blockNode.stmts.forEach(statement -> statement.accept(this));
    }

    public void visit(BLangLock.BLangLockStmt lockStmtNode) {
        lockStmtNode.body.accept(this);
    }

    public void visit(BLangLock.BLangUnLockStmt unLockNode) {
        unLockNode.body.accept(this);
    }

    public void visit(BLangCompoundAssignment compoundAssignNode) {
        if (compoundAssignNode.expr != null) {
            compoundAssignNode.expr.accept(this);
        }
        if (compoundAssignNode.modifiedExpr != null) {
            compoundAssignNode.modifiedExpr.accept(this);
        }
        if (compoundAssignNode.varRef != null) {
            compoundAssignNode.varRef.accept(this);
        }
    }

    public void visit(BLangRetry retryNode) {
    }

    public void visit(BLangContinue continueNode) {
    }

    public void visit(BLangBreak breakNode) {
    }

    public void visit(BLangThrow throwNode) {
        throwNode.expr.accept(this);
    }

    public void visit(BLangPanic panicNode) {
        panicNode.expr.accept(this);
    }

    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        xmlnsStmtNode.xmlnsDecl.accept(this);
    }

    public void visit(BLangIf ifNode) {
        ifNode.expr.accept(this);
        ifNode.body.accept(this);
        if (ifNode.elseStmt != null) {
            ifNode.elseStmt.accept(this);
        }
    }

    public void visit(BLangQueryAction queryAction) {
    }

    public void visit(BLangMatch matchNode) {
        matchNode.expr.accept(this);
        matchNode.patternClauses.forEach(pattern -> pattern.accept(this));
    }

    public void visit(BLangMatch.BLangMatchTypedBindingPatternClause patternClauseNode) {
        patternClauseNode.body.accept(this);
        patternClauseNode.matchExpr.accept(this);
        patternClauseNode.variable.accept(this);
    }

    public void visit(BLangForeach foreach) {
        throw new AssertionError();
    }

    public void visit(BLangFromClause fromClause) {
    }

    public void visit(BLangLetClause letClause) {
    }

    public void visit(BLangSelectClause selectClause) {
    }

    public void visit(BLangWhereClause whereClause) {
    }

    public void visit(BLangDoClause doClause) {
    }

    public void visit(BLangWhile whileNode) {
        whileNode.expr.accept(this);
        whileNode.body.accept(this);
    }

    public void visit(BLangLock lockNode) {
        lockNode.body.accept(this);
    }

    public void visit(BLangTransaction transactionNode) {
        transactionNode.transactionBody.accept(this);
    }

    public void visit(BLangTransactionalExpr transactionalExpr) {
    }

    public void visit(BLangTryCatchFinally tryNode) {
        tryNode.tryBody.accept(this);
        tryNode.catchBlocks.forEach(block -> block.accept(this));
        if (tryNode.finallyBody != null) {
            tryNode.finallyBody.accept(this);
        }
    }

    public void visit(BLangTupleDestructure stmt) {
        stmt.varRef.accept(this);
        stmt.expr.accept(this);
    }

    public void visit(BLangRecordDestructure stmt) {
        stmt.expr.accept(this);
        stmt.varRef.accept(this);
    }

    public void visit(BLangErrorDestructure stmt) {
        stmt.expr.accept(this);
        stmt.varRef.accept(this);
    }

    public void visit(BLangCatch catchNode) {
        catchNode.param.accept(this);
        catchNode.body.accept(this);
    }

    public void visit(BLangForkJoin forkJoin) {
        forkJoin.workers.forEach(worker -> worker.accept(this));
    }

    public void visit(BLangWorkerSend workerSendNode) {
        workerSendNode.expr.accept(this);
        if (workerSendNode.keyExpr != null) {
            workerSendNode.keyExpr.accept(this);
        }
    }

    public void visit(BLangWorkerReceive workerReceiveNode) {
        workerReceiveNode.sendExpression.accept(this);
        if (workerReceiveNode.keyExpr != null) {
            workerReceiveNode.keyExpr.accept(this);
        }
    }

    @Override
    public void visit(BLangCommitExpr commitExpr) {

    }

    @Override
    public void visit(BLangRollback rollbackNode) {
        if (rollbackNode.expr != null) {
            rollbackNode.expr.accept(this);
        }
    }
}

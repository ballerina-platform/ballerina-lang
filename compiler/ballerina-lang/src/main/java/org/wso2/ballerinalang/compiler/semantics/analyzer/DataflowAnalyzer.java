/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangDeprecatedNode;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFunctionClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangGroupBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangHaving;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLimit;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderBy;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOrderByVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOutputRateLimit;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternStreamingEdgeInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectExpression;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSetAssignment;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamAction;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangTableQuery;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhere;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWindow;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWithinClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAwaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangActionInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangBuiltInMethodInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression.BLangMatchExprPatternClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableQueryExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLSequenceLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompensate;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangDone;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStaticBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStructuredBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchTypedBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangScope;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStreamingQueryStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 
 * Responsible for performing data flow analysis.
 * <p>
 * The following validations are done here:-
 * <ul>
 * <li>Uninitialized variable referencing validation</li>
 * </ul>
 * 
 * @since 0.985.0
 */
public class DataflowAnalyzer extends BLangNodeVisitor {

    private SymbolEnv env;
    private SymbolTable symTable;
    private SymbolResolver symResolver;
    private BLangDiagnosticLog dlog;
    private Names names;
    private Map<BSymbol, InitStatus> uninitializedVars;
    private boolean flowTerminated = false;
    private BLangAnonymousModelHelper anonymousModelHelper;

    private static final CompilerContext.Key<DataflowAnalyzer> DATAFLOW_ANALYZER_KEY = new CompilerContext.Key<>();

    private DataflowAnalyzer(CompilerContext context) {
        context.put(DATAFLOW_ANALYZER_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.names = Names.getInstance(context);
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
    }

    public static DataflowAnalyzer getInstance(CompilerContext context) {
        DataflowAnalyzer dataflowAnalyzer = context.get(DATAFLOW_ANALYZER_KEY);
        if (dataflowAnalyzer == null) {
            dataflowAnalyzer = new DataflowAnalyzer(context);
        }
        return dataflowAnalyzer;
    }

    /**
     * Perform data-flow analysis on a package.
     * 
     * @param pkgNode Package to perform data-flow analysis.
     * @return Data-flow analyzed package
     */
    public BLangPackage analyze(BLangPackage pkgNode) {
        this.uninitializedVars = new HashMap<>();
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgNode.symbol);
        analyzeNode(pkgNode, pkgEnv);
        return pkgNode;
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.DATAFLOW_ANALYZE)) {
            return;
        }

        pkgNode.topLevelNodes.forEach(topLevelNode -> analyzeNode((BLangNode) topLevelNode, env));
        pkgNode.completedPhases.add(CompilerPhase.DATAFLOW_ANALYZE);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);
        analyzeBranch(funcNode.body, funcEnv);
        funcNode.workers.forEach(worker -> analyzeBranch(worker, funcEnv));
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, env);
        blockNode.stmts.forEach(statement -> analyzeNode(statement, blockEnv));
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
    }

    @Override
    public void visit(BLangService service) {
        service.vars.forEach(var -> analyzeNode(var, env));
        service.resources.forEach(res -> analyzeBranch(res, env));
    }

    @Override
    public void visit(BLangResource resource) {
        SymbolEnv resourceEnv = SymbolEnv.createResourceActionSymbolEnv(resource, resource.symbol.scope, env);
        analyzeBranch(resource.body, resourceEnv);
        resource.workers.forEach(worker -> analyzeBranch(worker, resourceEnv));
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        analyzeNode(typeDefinition.typeNode, env);
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        BLangVariable var = varDefNode.var;
        if (var.expr == null) {
            addUninitializedVar(var);
            return;
        }

        analyzeNode(var, env);
    }

    @Override
    public void visit(BLangSimpleVariable variable) {
        if (variable.expr != null) {
            analyzeNode(variable.expr, env);
            this.uninitializedVars.remove(variable.symbol);
            return;
        }

        // Handle package/object level variables
        BSymbol owner = variable.symbol.owner;
        if (owner.tag != SymTag.PACKAGE && owner.tag != SymTag.OBJECT) {
            return;
        }

        addUninitializedVar(variable);
    }

    @Override
    public void visit(BLangWorker worker) {
        SymbolEnv workerEnv = SymbolEnv.createWorkerEnv(worker, this.env);
        analyzeBranch(worker.body, workerEnv);
    }

    @Override
    public void visit(BLangEndpoint endpoint) {
        analyzeNode(endpoint.configurationExpr, env);
    }

    @Override
    public void visit(BLangAssignment assignment) {
        analyzeNode(assignment.expr, env);
        checkAssignment(assignment.varRef);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        analyzeNode(compoundAssignNode.expr, env);
        analyzeNode(compoundAssignNode.varRef, env);
        this.uninitializedVars.remove(compoundAssignNode.varRef.symbol);
    }

    @Override
    public void visit(BLangBreak breakNode) {
    }

    @Override
    public void visit(BLangReturn returnNode) {
        analyzeNode(returnNode.expr, env);

        // return statement will exit from the function.
        terminateFlow();
    }

    @Override
    public void visit(BLangThrow throwNode) {
        analyzeNode(throwNode.expr, env);

        // throw statement will terminate the flow.
        terminateFlow();
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmt) {
        analyzeNode(xmlnsStmt.xmlnsDecl, env);
    }

    @Override
    public void visit(BLangIf ifNode) {
        analyzeNode(ifNode.expr, env);
        BranchResult ifResult = analyzeBranch(ifNode.body, env);
        BranchResult elseResult = analyzeBranch(ifNode.elseStmt, env);

        // If the flow was terminated within 'if' block, then after the if-else block,
        // only the results of the 'else' block matters.
        if (ifResult.flowTerminated) {
            this.uninitializedVars = elseResult.uninitializedVars;
            return;
        }

        // If the flow was terminated within 'else' block, then after the if-else block,
        // only the results of the 'if' block matters.
        if (elseResult.flowTerminated) {
            this.uninitializedVars = ifResult.uninitializedVars;
            return;
        }

        this.uninitializedVars = mergeUninitializedVars(ifResult.uninitializedVars, elseResult.uninitializedVars);
    }

    @Override
    public void visit(BLangMatch match) {
        analyzeNode(match.expr, env);
        Map<BSymbol, InitStatus> uninitVars = new HashMap<>();
        for (BLangMatch.BLangMatchBindingPatternClause patternClause : match.patternClauses) {
            BranchResult result = analyzeBranch(patternClause, env);
            // If the flow was terminated within the block, then that branch should not be considered for
            // analyzing the data-flow for the downstream code.
            if (result.flowTerminated) {
                continue;
            }
            uninitVars = mergeUninitializedVars(uninitVars, result.uninitializedVars);
        }

        this.uninitializedVars = uninitVars;
    }

    @Override
    public void visit(BLangForeach foreach) {
        analyzeNode(foreach.collection, env);
        analyzeNode(foreach.body, env);
    }

    @Override
    public void visit(BLangWhile whileNode) {
        analyzeNode(whileNode.expr, env);
        analyzeNode(whileNode.body, env);
    }

    @Override
    public void visit(BLangLock lockNode) {
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
    }

    @Override
    public void visit(BLangTryCatchFinally tryNode) {
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        analyzeNode(stmt.expr, env);
        checkAssignment(stmt.varRef);
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        forkJoin.workers.forEach(worker -> analyzeNode(worker, env));
        analyzeNode(forkJoin.joinedBody, env);
    }

    @Override
    public void visit(BLangFunctionClause functionClause) {
    }

    @Override
    public void visit(BLangSetAssignment setAssignmentClause) {
        analyzeNode((BLangNode) setAssignmentClause.getExpressionNode(), env);
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        analyzeNode(workerSendNode.expr, env);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        analyzeNode(workerReceiveNode.expr, env);
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
    }

    @Override
    public void visit(BLangTableLiteral tableLiteral) {
        analyzeNode(tableLiteral.indexColumnsArrayLiteral, env);
        analyzeNode(tableLiteral.keyColumnsArrayLiteral, env);
        tableLiteral.columns.forEach(column -> analyzeNode(column, env));
    }

    @Override
    public void visit(BLangArrayLiteral arrayLiteral) {
        arrayLiteral.exprs.forEach(expr -> analyzeNode(expr, env));
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        recordLiteral.keyValuePairs.forEach(keyValPar -> analyzeNode(keyValPar.valueExpr, env));
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        checkVarRef(varRefExpr.symbol, varRefExpr.pos);
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        if (!fieldAccessExpr.lhsVar && isObjectMemberAccess(fieldAccessExpr)) {
            checkVarRef(fieldAccessExpr.symbol, fieldAccessExpr.pos);
        }
        analyzeNode(fieldAccessExpr.expr, env);
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        analyzeNode(indexAccessExpr.expr, env);
        analyzeNode(indexAccessExpr.indexExpr, env);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        analyzeNode(invocationExpr.expr, env);
        invocationExpr.requiredArgs.forEach(expr -> analyzeNode(expr, env));
        invocationExpr.namedArgs.forEach(expr -> analyzeNode(expr, env));
        invocationExpr.restArgs.forEach(expr -> analyzeNode(expr, env));
    }

    @Override
    public void visit(BLangTypeInit typeInitExpr) {
        typeInitExpr.argsExpr.forEach(argExpr -> analyzeNode(argExpr, env));
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        analyzeNode(ternaryExpr.expr, env);
    }

    @Override
    public void visit(BLangAwaitExpr awaitExpr) {
        analyzeNode(awaitExpr.expr, env);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        analyzeNode(binaryExpr.lhsExpr, env);
        analyzeNode(binaryExpr.rhsExpr, env);
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        analyzeNode(elvisExpr.lhsExpr, env);
        analyzeNode(elvisExpr.rhsExpr, env);
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        bracedOrTupleExpr.expressions.forEach(expr -> analyzeNode(expr, env));
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        analyzeNode(unaryExpr.expr, env);
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        analyzeNode(conversionExpr.expr, env);
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        analyzeNode(xmlAttribute.value, env);
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        xmlElementLiteral.children.forEach(expr -> analyzeNode(expr, env));
        xmlElementLiteral.attributes.forEach(expr -> analyzeNode(expr, env));
        xmlElementLiteral.inlineNamespaces.forEach(expr -> analyzeNode(expr, env));
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        xmlTextLiteral.textFragments.forEach(expr -> analyzeNode(expr, env));
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        xmlCommentLiteral.textFragments.forEach(expr -> analyzeNode(expr, env));
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        xmlProcInsLiteral.dataFragments.forEach(expr -> analyzeNode(expr, env));
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        xmlQuotedString.textFragments.forEach(expr -> analyzeNode(expr, env));
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        stringTemplateLiteral.exprs.forEach(expr -> analyzeNode(expr, env));
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        analyzeNode(bLangLambdaFunction.function, env);
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        analyzeNode(xmlAttributeAccessExpr.expr, env);
        analyzeNode(xmlAttributeAccessExpr.indexExpr, env);
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        analyzeNode(intRangeExpression.startExpr, env);
        analyzeNode(intRangeExpression.endExpr, env);
    }

    @Override
    public void visit(BLangTableQueryExpression tableQueryExpression) {
        tableQueryExpression.getParams().forEach(param -> analyzeNode(param, env));
        analyzeNode((BLangNode) tableQueryExpression.getTableQuery(), env);
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        analyzeNode(bLangVarArgsExpression.expr, env);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        analyzeNode(bLangNamedArgsExpression.expr, env);
    }

    @Override
    public void visit(BLangPatternClause patternClause) {
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
    }

    @Override
    public void visit(BLangMatchExpression matchExpression) {
        analyzeNode(matchExpression.expr, env);
        matchExpression.patternClauses.forEach(pattern -> analyzeNode(pattern, env));
    }

    @Override
    public void visit(BLangMatchExprPatternClause matchExprPatternClause) {
        analyzeNode(matchExprPatternClause.expr, env);
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        analyzeNode(checkedExpr.expr, env);
    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
        bLangXMLSequenceLiteral.xmlItems.forEach(xml -> analyzeNode(xml, env));
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        analyzeNode(exprStmtNode.expr, env);
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
    }

    @Override
    public void visit(BLangDeprecatedNode deprecatedNode) {
    }

    @Override
    public void visit(BLangAbort abortNode) {
    }

    @Override
    public void visit(BLangDone doneNode) {
        // 'done' statement will exit from the worker. There will be no uninitialized
        // variables left after the 'done' statement.
        this.uninitializedVars.clear();
    }

    @Override
    public void visit(BLangRetry retryNode) {
    }

    @Override
    public void visit(BLangContinue continueNode) {
    }

    @Override
    public void visit(BLangCatch catchNode) {
    }

    @Override
    public void visit(BLangOrderBy orderBy) {
    }

    @Override
    public void visit(BLangOrderByVariable orderByVariable) {
    }

    @Override
    public void visit(BLangLimit limit) {
    }

    @Override
    public void visit(BLangGroupBy groupBy) {
    }

    @Override
    public void visit(BLangHaving having) {
    }

    @Override
    public void visit(BLangSelectExpression selectExpression) {
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
    }

    @Override
    public void visit(BLangWhere whereClause) {
    }

    @Override
    public void visit(BLangStreamingInput streamingInput) {
    }

    @Override
    public void visit(BLangJoinStreamingInput joinStreamingInput) {
    }

    @Override
    public void visit(BLangTableQuery tableQuery) {
    }

    @Override
    public void visit(BLangStreamAction streamAction) {
    }

    @Override
    public void visit(BLangPatternStreamingEdgeInput patternStreamingEdgeInput) {
    }

    @Override
    public void visit(BLangWindow windowClause) {
    }

    @Override
    public void visit(BLangPatternStreamingInput patternStreamingInput) {
    }

    @Override
    public void visit(BLangForever foreverStatement) {
    }

    @Override
    public void visit(BLangActionInvocation actionInvocationExpr) {
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
    }

    @Override
    public void visit(BLangStreamingQueryStatement streamingQueryStatement) {
    }

    @Override
    public void visit(BLangWithinClause withinClause) {
    }

    @Override
    public void visit(BLangOutputRateLimit outputRateLimit) {
    }

    @Override
    public void visit(BLangValueType valueType) {
    }

    @Override
    public void visit(BLangConstant constant) {
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
    public void visit(BLangUserDefinedType userDefinedType) {
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        SymbolEnv objectEnv = SymbolEnv.createTypeEnv(objectTypeNode, objectTypeNode.symbol.scope, env);
        objectTypeNode.fields.forEach(field -> analyzeNode(field, objectEnv));

        // Visit the constructor with the same scope as the object
        if (objectTypeNode.initFunction != null) {
            objectTypeNode.initFunction.requiredParams.forEach(param -> {
                analyzeParam(objectTypeNode, param, objectEnv);
            });

            objectTypeNode.initFunction.defaultableParams.forEach(param -> {
                analyzeParam(objectTypeNode, param.var, objectEnv);
            });

            if (objectTypeNode.initFunction.restParam != null) {
                analyzeParam(objectTypeNode, objectTypeNode.initFunction.restParam, objectEnv);
            }

            // Body should be visited after the params
            objectTypeNode.initFunction.body.stmts.forEach(statement -> analyzeNode(statement, objectEnv));
        }

        if (!anonymousModelHelper.isAnonymousType(objectTypeNode.symbol) &&
                !Symbols.isFlagOn(objectTypeNode.symbol.flags, Flags.ABSTRACT)) {
            objectTypeNode.fields.stream().filter(field -> !Symbols.isPrivate(field.symbol)).forEach(field -> {
                if (this.uninitializedVars.containsKey(field.symbol)) {
                    this.dlog.error(field.pos, DiagnosticCode.OBJECT_UNINITIALIZED_FIELD, field.name);
                }
            });
        }

        objectTypeNode.functions.forEach(function -> analyzeNode(function, env));
    }
    
    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
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
    public void visit(BLangMarkdownDocumentation bLangMarkdownDocumentation) {
    }

    @Override
    public void visit(BLangCompensate compensateNode) {
    }

    @Override
    public void visit(BLangScope scopeNode) {
        analyzeNode(scopeNode.scopeBody, env);
    }

    @Override
    public void visit(BLangTestablePackage testablePkgNode) {
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
    }

    @Override
    public void visit(BLangIdentifier identifierNode) {
    }

    @Override
    public void visit(BLangPanic panicNode) {
        analyzeNode(panicNode.expr, env);

        // panic statement will terminate the flow. There will be no uninitialized
        // variables left after the panic statement.
        terminateFlow();
    }

    @Override
    public void visit(BLangBuiltInMethodInvocation builtInMethodInvocation) {
        analyzeNode(builtInMethodInvocation.expr, env);
        builtInMethodInvocation.argExprs.forEach(argExpr -> analyzeNode(argExpr, env));
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        analyzeNode(trapExpr.expr, env);
    }

    @Override
    public void visit(BLangErrorConstructorExpr errorConstructorExpr) {
        analyzeNode(errorConstructorExpr.detailsExpr, env);
        analyzeNode(errorConstructorExpr.reasonExpr, env);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        analyzeNode(typeTestExpr.expr, env);
    }

    @Override
    public void visit(BLangErrorType errorType) {
    }

    @Override
    public void visit(BLangAction actionNode) {
    }

    @Override
    public void visit(BLangMatchTypedBindingPatternClause patternClauseNode) {
        analyzeNode(patternClauseNode.body, env);
    }

    @Override
    public void visit(BLangRecordDestructure recordDestructure) {
        analyzeNode(recordDestructure.expr, env);
        checkAssignment(recordDestructure.varRef);
    }

    @Override
    public void visit(BLangTupleVarRef tupleVarRefExpr) {
        tupleVarRefExpr.expressions.forEach(expr -> analyzeNode(expr, env));
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        BLangVariable var = bLangTupleVariableDef.var;
        if (var.expr == null) {
            addUninitializedVar(var);
            return;
        }
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        BLangVariable var = bLangRecordVariableDef.var;
        if (var.expr == null) {
            addUninitializedVar(var);
            return;
        }
    }

    @Override
    public void visit(BLangMatchStaticBindingPatternClause bLangMatchStaticBindingPatternClause) {
    }

    @Override
    public void visit(BLangMatchStructuredBindingPatternClause bLangMatchStructuredBindingPatternClause) {
    }

    private void addUninitializedVar(BLangVariable variable) {
        if (variable.symbol.type.tag != TypeTags.CHANNEL &&
                variable.symbol.type.tag != TypeTags.STREAM && !this.uninitializedVars.containsKey(variable.symbol)) {
            this.uninitializedVars.put(variable.symbol, InitStatus.UN_INIT);
        }
    }

    /**
     * Analyze a branch and returns the set of uninitialized variables for that branch.
     * This method will not update the current uninitialized variables set.
     * 
     * @param node Branch node to be analyzed
     * @param env Symbol environment
     * @return Result of the branch.
     */
    private BranchResult analyzeBranch(BLangNode node, SymbolEnv env) {
        Map<BSymbol, InitStatus> prevUninitializedVars = this.uninitializedVars;
        boolean prevFlowTerminated = this.flowTerminated;

        // Get a snapshot of the current uninitialized vars before visiting the node.
        // This is done so that the original set of uninitialized vars will not be
        // updated/marked as initialized.
        this.uninitializedVars = copyUninitializedVars();
        this.flowTerminated = false;

        analyzeNode(node, env);
        BranchResult brachResult = new BranchResult(this.uninitializedVars, this.flowTerminated);

        // Restore the original set of uninitialized vars
        this.uninitializedVars = prevUninitializedVars;
        this.flowTerminated = prevFlowTerminated;

        return brachResult;
    }

    private Map<BSymbol, InitStatus> copyUninitializedVars() {
        return new HashMap<>(this.uninitializedVars);
    }

    private void analyzeNode(BLangNode node, SymbolEnv env) {
        SymbolEnv prevEnv = this.env;
        this.env = env;
        if (node != null) {
            node.accept(this);
        }
        this.env = prevEnv;
    }

    private void analyzeParam(BLangObjectTypeNode objectTypeNode, BLangSimpleVariable param, SymbolEnv env) {
        if (param.type == symTable.noType || param.type == symTable.semanticError) {
            return;
        }

        BSymbol fieldSymbol = this.symResolver.resolveObjectField(param.pos, env, names.fromIdNode(param.name),
                objectTypeNode.type.tsymbol);
        this.uninitializedVars.remove(fieldSymbol);
    }

    private Map<BSymbol, InitStatus> mergeUninitializedVars(Map<BSymbol, InitStatus> firstUninitVars,
                                                            Map<BSymbol, InitStatus> secondUninitVars) {
        List<BSymbol> intersection = new ArrayList<>(firstUninitVars.keySet());
        intersection.retainAll(secondUninitVars.keySet());

        return Stream.concat(firstUninitVars.entrySet().stream(), secondUninitVars.entrySet().stream())
                .collect(Collectors.toMap(entry -> entry.getKey(),
                        // If only one branch have uninitialized the var, then its a partial initialization
                        entry -> intersection.contains(entry.getKey()) ? entry.getValue() : InitStatus.PARTIAL_INIT,
                        (a, b) -> {
                            // If atleast one of the branches have partially initialized the var,
                            // then merged result is also a partially initialized var
                            if (a == InitStatus.PARTIAL_INIT || b == InitStatus.PARTIAL_INIT) {
                                return InitStatus.PARTIAL_INIT;
                            }

                            return InitStatus.UN_INIT;
                        }));
    }

    private void checkVarRef(BSymbol symbol, DiagnosticPos pos) {
        InitStatus initStatus = this.uninitializedVars.get(symbol);
        if (initStatus == null) {
            return;
        }

        if (initStatus == InitStatus.UN_INIT) {
            this.dlog.error(pos, DiagnosticCode.UNINITIALIZED_VARIABLE, symbol.name);
            return;
        }

        this.dlog.error(pos, DiagnosticCode.PARTIALLY_INITIALIZED_VARIABLE, symbol.name);
    }

    private boolean isObjectMemberAccess(BLangFieldBasedAccess fieldAccessExpr) {
        if (fieldAccessExpr.expr.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return false;
        }
        return Names.SELF.value.equals(((BLangSimpleVarRef) fieldAccessExpr.expr).variableName.value);
    }

    private void checkAssignment(BLangExpression varRef) {
        if (varRef.getKind() == NodeKind.RECORD_VARIABLE_REF) {
            ((BLangRecordVarRef) varRef).recordRefFields.forEach(field -> checkAssignment(field.variableReference));
            return;
        } else if (varRef.getKind() == NodeKind.TUPLE_VARIABLE_REF) {
            ((BLangTupleVarRef) varRef).expressions.forEach(expr -> checkAssignment(expr));
            return;
        } else if (varRef.getKind() != NodeKind.SIMPLE_VARIABLE_REF &&
                varRef.getKind() != NodeKind.INDEX_BASED_ACCESS_EXPR &&
                varRef.getKind() != NodeKind.FIELD_BASED_ACCESS_EXPR &&
                varRef.getKind() != NodeKind.XML_ATTRIBUTE_ACCESS_EXPR) {
            return;
        }

        this.uninitializedVars.remove(((BLangVariableReference) varRef).symbol);
    }

    private void terminateFlow() {
        this.flowTerminated = true;
    }

    private enum InitStatus {
        UN_INIT, PARTIAL_INIT
    }

    private class BranchResult {

        Map<BSymbol, InitStatus> uninitializedVars;
        boolean flowTerminated;

        BranchResult(Map<BSymbol, InitStatus> uninitializedVars, boolean flowTerminated) {
            this.uninitializedVars = uninitializedVars;
            this.flowTerminated = flowTerminated;
        }
    }
}

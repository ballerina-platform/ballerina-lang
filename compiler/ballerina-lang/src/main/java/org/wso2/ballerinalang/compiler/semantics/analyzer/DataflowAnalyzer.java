package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangDeprecatedNode;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangLocalXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangPackageXMLNS;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral.BLangJSONArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAwaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess.BLangStructFunctionVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangArrayAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangJSONAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangMapAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangStructFieldAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangTupleAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess.BLangXMLAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BFunctionPointerInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangActionInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangAttachedFunctionInvocation;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangJSONLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangMapLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangStreamLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangStructLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangFieldVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangFunctionVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangLocalVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangPackageVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef.BLangTypeLoad;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableQueryExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStmtPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangScope;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStreamingQueryStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;

import java.util.HashMap;
import java.util.Map;

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

    private BLangDiagnosticLog dlog;
    private DataFlowScope currentScope;
    private SymbolEnv env;
    private SymbolTable symTable;
    private static final int INITIAL_BRANCHES = 1;

    private static final CompilerContext.Key<DataflowAnalyzer> DATAFLOW_ANALYZER_KEY = new CompilerContext.Key<>();

    public DataflowAnalyzer(CompilerContext context) {
        context.put(DATAFLOW_ANALYZER_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
    }

    public static DataflowAnalyzer getInstance(CompilerContext context) {
        DataflowAnalyzer dataflowAnalyzer = context.get(DATAFLOW_ANALYZER_KEY);
        if (dataflowAnalyzer == null) {
            dataflowAnalyzer = new DataflowAnalyzer(context);
        }
        return dataflowAnalyzer;
    }

    private void startScope() {
        this.currentScope = new DataFlowScope(this.currentScope, env.scope.owner);
    }

    private void startBranch() {
        // Increment branch count
        this.currentScope.pathsCount++;
    }

    private void startBranches(int branchCount) {
        // Increment branch count
        this.currentScope.pathsCount = branchCount;
    }

    private void endScope() {
        this.currentScope = this.currentScope.parent;
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgNode.symbol);
        analyzeNode(pkgNode, pkgEnv);
        return pkgNode;
    }

    private void analyzeNode(BLangNode node, SymbolEnv env) {
        SymbolEnv prevEnv = this.env;
        this.env = env;
        if (node != null) {
            node.accept(this);
        }
        this.env = prevEnv;
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.DATAFLOW_ANALYZE)) {
            return;
        }

        startScope();
        pkgNode.topLevelNodes.forEach(topLevelNode -> analyzeNode((BLangNode) topLevelNode, env));
        pkgNode.completedPhases.add(CompilerPhase.DATAFLOW_ANALYZE);
        endScope();
    }

    @Override
    public void visit(BLangFunction funcNode) {
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);
        analyzeNode(funcNode.body, funcEnv);
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        startScope();
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, env);
        blockNode.stmts.forEach(statement -> analyzeNode(statement, blockEnv));
        endScope();
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
    }

    @Override
    public void visit(BLangService service) {
        service.resources.forEach(res -> analyzeNode(res, env));
    }

    @Override
    public void visit(BLangResource resource) {
        SymbolEnv resourceEnv = SymbolEnv.createResourceActionSymbolEnv(resource, resource.symbol.scope, env);
        analyzeNode(resource.body, resourceEnv);
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        SymbolEnv tyeDefEnv = SymbolEnv.createTypeDefEnv(typeDefinition, typeDefinition.symbol.scope, env);
        analyzeNode(typeDefinition.typeNode, tyeDefEnv);
    }

    @Override
    public void visit(BLangVariable variable) {
        if (variable.expr != null) {
            analyzeNode(variable.expr, env);
            return;
        }

        // Handle package/object level variables
        BSymbol owner = variable.symbol.owner;
        if (owner.tag == SymTag.PACKAGE || owner.tag == SymTag.OBJECT) {
            this.currentScope.addUninitializedVar(variable.symbol);
            return;
        }
    }

    @Override
    public void visit(BLangWorker worker) {
        SymbolEnv workerEnv = SymbolEnv.createWorkerEnv(worker, this.env);
        analyzeNode(worker.body, workerEnv);
    }

    @Override
    public void visit(BLangEndpoint endpoint) {
        analyzeNode(endpoint.configurationExpr, env);
    }

    @Override
    public void visit(BLangVariableDef varDefNode) {
        BLangVariable var = varDefNode.var;
        if (var.expr == null) {
            this.currentScope.addUninitializedVar(var.symbol);
            return;
        }

        analyzeNode(var, env);
    }

    @Override
    public void visit(BLangAssignment assignment) {
        analyzeNode(assignment.expr, env);

        if (assignment.varRef.getKind() != NodeKind.SIMPLE_VARIABLE_REF &&
                assignment.varRef.getKind() != NodeKind.INDEX_BASED_ACCESS_EXPR &&
                assignment.varRef.getKind() != NodeKind.FIELD_BASED_ACCESS_EXPR &&
                assignment.varRef.getKind() != NodeKind.XML_ATTRIBUTE_ACCESS_EXPR) {
            return;
        }

        BLangVariableReference varRefExpr = (BLangVariableReference) assignment.varRef;
        this.currentScope.markAsInitilaized(varRefExpr.symbol);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        // TODO
    }

    @Override
    public void visit(BLangBreak breakNode) {
    }

    @Override
    public void visit(BLangReturn returnNode) {
        analyzeNode(returnNode.expr, env);

        // Having a return statement will complete that branch. Hence mark all vars
        // as initialized for this branch.
        this.currentScope.markAllAsInitilaized();
    }

    @Override
    public void visit(BLangThrow throwNode) {
        analyzeNode(throwNode.expr, env);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmt) {
        analyzeNode(xmlnsStmt.xmlnsDecl, env);
    }

    @Override
    public void visit(BLangIf ifNode) {
        analyzeNode(ifNode.expr, env);
        startBranch();
        analyzeNode(ifNode.body, env);
        analyzeNode(ifNode.elseStmt, env);
    }

    @Override
    public void visit(BLangMatch match) {
        analyzeNode(match.expr, env);
        startBranches(match.patternClauses.size());
        match.patternClauses.forEach(patternClause -> analyzeNode(patternClause, env));
    }

    @Override
    public void visit(BLangMatchStmtPatternClause patternClauseNode) {
        analyzeNode(patternClauseNode.body, env);
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
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
    }

    @Override
    public void visit(BLangFunctionClause functionClause) {
    }

    @Override
    public void visit(BLangSetAssignment setAssignmentClause) {
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
    }

    @Override
    public void visit(BLangTableLiteral tableLiteral) {
    }

    @Override
    public void visit(BLangArrayLiteral arrayLiteral) {
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        // TODO: differentiate 'is not initialized' vs 'may not initialized'
        if (!this.currentScope.isInitialized(varRefExpr.symbol)) {
            dlog.error(varRefExpr.pos, DiagnosticCode.UNITIALIZED_VARIABLE, varRefExpr.symbol.name);
        }
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
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
    public void visit(BLangTypeInit connectorInitExpr) {
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
    }

    @Override
    public void visit(BLangAwaitExpr ternaryExpr) {
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        analyzeNode(binaryExpr.lhsExpr, env);
        analyzeNode(binaryExpr.rhsExpr, env);
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        bracedOrTupleExpr.expressions.forEach(expr -> analyzeNode(expr, env));
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
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
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
    }

    @Override
    public void visit(BLangPatternClause patternClause) {
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {
    }

    @Override
    public void visit(BLangMatchExprPatternClause bLangMatchExprPatternClause) {
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
    }

    @Override
    public void visit(BLangLocalVarRef localVarRef) {
    }

    @Override
    public void visit(BLangFieldVarRef fieldVarRef) {
    }

    @Override
    public void visit(BLangPackageVarRef packageVarRef) {
    }

    @Override
    public void visit(BLangFunctionVarRef functionVarRef) {
    }

    @Override
    public void visit(BLangTypeLoad typeLoad) {
    }

    @Override
    public void visit(BLangStructFieldAccessExpr fieldAccessExpr) {
    }

    @Override
    public void visit(BLangStructFunctionVarRef functionVarRef) {
    }

    @Override
    public void visit(BLangMapAccessExpr mapKeyAccessExpr) {
    }

    @Override
    public void visit(BLangArrayAccessExpr arrayIndexAccessExpr) {
    }

    @Override
    public void visit(BLangTupleAccessExpr arrayIndexAccessExpr) {
    }

    @Override
    public void visit(BLangXMLAccessExpr xmlIndexAccessExpr) {
    }

    @Override
    public void visit(BLangJSONLiteral jsonLiteral) {
    }

    @Override
    public void visit(BLangMapLiteral mapLiteral) {
    }

    @Override
    public void visit(BLangStructLiteral structLiteral) {
    }

    @Override
    public void visit(BLangStreamLiteral streamLiteral) {
    }

    @Override
    public void visit(BFunctionPointerInvocation bFunctionPointerInvocation) {
    }

    @Override
    public void visit(BLangAttachedFunctionInvocation iExpr) {
    }

    @Override
    public void visit(BLangJSONArrayLiteral jsonArrayLiteral) {
    }

    @Override
    public void visit(BLangJSONAccessExpr jsonAccessExpr) {
    }

    @Override
    public void visit(BLangLocalXMLNS xmlnsNode) {
    }

    @Override
    public void visit(BLangPackageXMLNS xmlnsNode) {
    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        analyzeNode(exprStmtNode.expr, env);
    }

    @Override
    public void visit(BLangAction actionNode) {
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
        // TODO Auto-generated method stub
        super.visit(streamingQueryStatement);
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
        startScope();
        objectTypeNode.fields.forEach(field -> analyzeNode(field, env));

        // Visit the constructor with the same scope as the object
        objectTypeNode.initFunction.body.stmts.forEach(statement -> analyzeNode(statement, env));
//        objectTypeNode.initFunction.requiredParams.forEach(param -> this.currentScope.markAsInitilaized(param.symbol));

        objectTypeNode.functions.forEach(function -> analyzeNode(function, env));
        endScope();
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
    }

    /**
     * Represents a scope for data.
     * 
     * @since 0.985.0
     */
    private class DataFlowScope {

        private DataFlowScope parent;
        private int pathsCount = INITIAL_BRANCHES;
        private Map<BSymbol, VarInfo> unInitVars;
        private BSymbol owner;

        DataFlowScope(DataFlowScope parent, BSymbol owner) {
            this.parent = parent;
            this.owner = owner;
            this.unInitVars = new HashMap<>();
            if (parent == null) {
                return;
            }

            // Take a snapshot of all uninitialized variable symbols
            parent.unInitVars.keySet().forEach(var -> this.unInitVars.put(var, new VarInfo()));
        }

        void addUninitializedVar(BSymbol varSymbol) {
            this.unInitVars.put(varSymbol, new VarInfo());
        }

        /**
         * Mark a given variable as initialized for the current scope.
         * 
         * @param varSymbol Symbol of the variable to be marked as initialized.
         */
        void markAsInitilaized(BSymbol varSymbol) {
            this.unInitVars.remove(varSymbol);

            if (this.parent == null || this.parent.owner.tag == SymTag.PACKAGE ||
                    this.parent.owner.tag == SymTag.OBJECT || this.parent.owner.tag == SymTag.SERVICE) {
                return;
            }

            markAsInitializedForParent(varSymbol);
        }

        /**
         * Mark all the variables visible as initialized for the current scope.
         */
        void markAllAsInitilaized() {
            if (this.parent != null && this.parent.owner.tag != SymTag.PACKAGE &&
                    this.parent.owner.tag != SymTag.OBJECT && this.parent.owner.tag != SymTag.SERVICE) {
                this.unInitVars.keySet().forEach(symbol -> markAsInitializedForParent(symbol));
            }

            this.unInitVars.clear();
        }

        boolean isInitialized(BSymbol varSymbol) {
            if (!this.unInitVars.containsKey(varSymbol)) {
                return true;
            }

            return unInitVars.get(varSymbol).initCount == pathsCount;
        }

        private void markAsInitializedForParent(BSymbol varSymbol) {
            VarInfo parentScopeVarInfo = this.parent.unInitVars.get(varSymbol);
            if (parentScopeVarInfo == null) {
                return;
            }

            // If all the branches of parents are covered, mark this variable as initialized for parent also
            parentScopeVarInfo.initCount++;
            if (this.parent.pathsCount == parentScopeVarInfo.initCount) {
                parent.markAsInitilaized(varSymbol);
            }
        }
    }

    /**
     * Represents initialization info of a variable.
     * 
     * @since 0.985.0
     */
    private class VarInfo {

        int initCount = 0;

        @Override
        public String toString() {
            return "initCount: " + initCount;
        }
    }

}

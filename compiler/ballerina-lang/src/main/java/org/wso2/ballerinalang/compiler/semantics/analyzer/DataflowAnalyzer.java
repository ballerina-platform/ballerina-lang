package org.wso2.ballerinalang.compiler.semantics.analyzer;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
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
import org.wso2.ballerinalang.compiler.tree.clauses.BLangPatternClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSetAssignment;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral.BLangJSONArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAwaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess.BLangEnumeratorAccessExpr;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangAttachedFunctionInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLAttributeAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLCommentLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLSequenceLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStmtPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPostIncrement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangThrow;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTryCatchFinally;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * Responsible for doing the dataflow analysis.
 * <p>
 * The following validations are done here:-
 * <ul>
 * <li>Uninitialized variable referencing validation</li>
 * </ul>
 * </p>
 * 
 * @since 0.982.0
 */
public class DataflowAnalyzer extends BLangNodeVisitor {

    private BLangDiagnosticLog dlog;
    private Scope currentScope = null;
    private static final int INITIAL_BRANCHES = 1;

    private static final CompilerContext.Key<DataflowAnalyzer> DATAFLOW_ANALYZER_KEY = new CompilerContext.Key<>();

    public DataflowAnalyzer(CompilerContext context) {
        context.put(DATAFLOW_ANALYZER_KEY, this);
        this.dlog = BLangDiagnosticLog.getInstance(context);
    }

    public static DataflowAnalyzer getInstance(CompilerContext context) {
        DataflowAnalyzer codeGenerator = context.get(DATAFLOW_ANALYZER_KEY);
        if (codeGenerator == null) {
            codeGenerator = new DataflowAnalyzer(context);
        }
        return codeGenerator;
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        pkgNode.accept(this);
        return pkgNode;
    }

    private void analyzeNode(BLangNode node) {
        if (node != null) {
            node.accept(this);
        }
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.DATAFLOW_ANALYZE)) {
            return;
        }
        pkgNode.topLevelNodes.forEach(topLevelNode -> analyzeNode((BLangNode) topLevelNode));
        pkgNode.completedPhases.add(CompilerPhase.DATAFLOW_ANALYZE);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        analyzeNode(funcNode.body);
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        startScope();
        blockNode.stmts.forEach(statement -> analyzeNode(statement));
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
        service.resources.forEach(res -> analyzeNode(res));
    }

    @Override
    public void visit(BLangResource resource) {
        analyzeNode(resource.body);
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
    }

    @Override
    public void visit(BLangVariable variable) {
        analyzeNode(variable.expr);
    }

    @Override
    public void visit(BLangWorker worker) {
        analyzeNode(worker.body);
    }

    @Override
    public void visit(BLangEndpoint endpoint) {
        analyzeNode(endpoint.configurationExpr);
    }

    @Override
    public void visit(BLangVariableDef varDefNode) {
        BLangVariable var = varDefNode.var;
        if (var.expr == null) {
            this.currentScope.addUninitializedVar(var.symbol);
            return;
        }

        analyzeNode(var);
    }

    @Override
    public void visit(BLangAssignment assignment) {
        analyzeNode(assignment.expr);

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
    public void visit(BLangPostIncrement increment) {
        analyzeNode(increment.varRef);
        analyzeNode(increment.increment);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignNode) {
        // TODO
    }

    @Override
    public void visit(BLangReturn returnNode) {
        analyzeNode(returnNode.expr);
    }

    @Override
    public void visit(BLangThrow throwNode) {
        analyzeNode(throwNode.expr);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmt) {
        analyzeNode(xmlnsStmt.xmlnsDecl);
    }

    @Override
    public void visit(BLangIf ifNode) {
        analyzeNode(ifNode.expr);
        startBranch();
        analyzeNode(ifNode.body);
        analyzeNode(ifNode.elseStmt);
    }

    @Override
    public void visit(BLangMatch match) {
        analyzeNode(match.expr);
        this.currentScope.pathsCount = match.patternClauses.size();
        match.patternClauses.forEach(patternClause -> analyzeNode(patternClause));
    }

    @Override
    public void visit(BLangMatchStmtPatternClause patternClauseNode) {
        analyzeNode(patternClauseNode.body);
    }

    @Override
    public void visit(BLangForeach foreach) {
        analyzeNode(foreach.collection);
        analyzeNode(foreach.body);
    }

    @Override
    public void visit(BLangWhile whileNode) {
        analyzeNode(whileNode.expr);
        analyzeNode(whileNode.body);
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
        if (!this.currentScope.isInitialized(varRefExpr.symbol)) {
            dlog.error(varRefExpr.pos, DiagnosticCode.UNITIALIZED_VARIABLE, varRefExpr.symbol.name);
        }
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        analyzeNode(fieldAccessExpr.expr);
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        analyzeNode(indexAccessExpr.expr);
        analyzeNode(indexAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        analyzeNode(invocationExpr.expr);
        invocationExpr.requiredArgs.forEach(expr -> analyzeNode(expr));
        invocationExpr.namedArgs.forEach(expr -> analyzeNode(expr));
        invocationExpr.restArgs.forEach(expr -> analyzeNode(expr));
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
        analyzeNode(binaryExpr.lhsExpr);
        analyzeNode(binaryExpr.rhsExpr);
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        bracedOrTupleExpr.expressions.forEach(expr -> analyzeNode(expr));
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        analyzeNode(conversionExpr.expr);
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        analyzeNode(xmlAttribute.value);
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        xmlElementLiteral.children.forEach(expr -> analyzeNode(expr));
        xmlElementLiteral.attributes.forEach(expr -> analyzeNode(expr));
        xmlElementLiteral.inlineNamespaces.forEach(expr -> analyzeNode(expr));
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        xmlTextLiteral.textFragments.forEach(expr -> analyzeNode(expr));
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        xmlCommentLiteral.textFragments.forEach(expr -> analyzeNode(expr));
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        xmlProcInsLiteral.dataFragments.forEach(expr -> analyzeNode(expr));
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        xmlQuotedString.textFragments.forEach(expr -> analyzeNode(expr));
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        stringTemplateLiteral.exprs.forEach(expr -> analyzeNode(expr));
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        analyzeNode(xmlAttributeAccessExpr.expr);
        analyzeNode(xmlAttributeAccessExpr.indexExpr);
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        analyzeNode(intRangeExpression.startExpr);
        analyzeNode(intRangeExpression.endExpr);
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
    public void visit(BLangEnumeratorAccessExpr enumeratorAccessExpr) {
    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        analyzeNode(exprStmtNode.expr);
    }

    private void startScope() {
        // Start a new branch count for the new scope
        this.currentScope = new Scope(this.currentScope);
    }

    private void startBranch() {
        // Increment branch count
        this.currentScope.pathsCount++;
    }

    private void endScope() {
        this.currentScope = this.currentScope.parent;
    }

    /**
     * Represents a scope for data.
     * 
     * @since 0.982.0
     */
    private class Scope {

        private Scope parent;
        private int pathsCount = INITIAL_BRANCHES;
        private Map<BSymbol, VarInfo> unInitVars;

        Scope(Scope parent) {
            this.parent = parent;
            this.unInitVars = new HashMap<>();
            if (parent == null) {
                return;
            }

            // Take a copy of un-initialized variable symbols
            parent.unInitVars.keySet().forEach(var -> this.unInitVars.put(var, new VarInfo()));
        }

        void addUninitializedVar(BSymbol varSymbol) {
            this.unInitVars.put(varSymbol, new VarInfo());
        }

        void markAsInitilaized(BSymbol varSymbol) {
            this.unInitVars.remove(varSymbol);

            if (this.parent == null) {
                return;
            }

            // If all the branches of parents are covered, mark this variable as initialized for parent also
            VarInfo parentScopeVarInfo = this.parent.unInitVars.get(varSymbol);
            parentScopeVarInfo.initCount++;
            if (this.parent.pathsCount == parentScopeVarInfo.initCount) {
                parent.markAsInitilaized(varSymbol);
            }
        }

        boolean isInitialized(BSymbol varSymbol) {
            if (!this.unInitVars.containsKey(varSymbol)) {
                return true;
            }

            return unInitVars.get(varSymbol).initCount == pathsCount;
        }
    }

    /**
     * Represents initialization info of a variable.
     * 
     * @since 0.982.0
     */
    private class VarInfo {

        int initCount = 0;

        @Override
        public String toString() {
            return "initCount:" + initCount;
        }
    }
}

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
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.BLangBuiltInMethod;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.TaintRecord;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAccessExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableQueryExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWaitForAllExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerFlushExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerReceive;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerSyncSendExpr;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStreamingQueryStatement;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerUtils;
import org.wso2.ballerinalang.compiler.util.Constants;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.XMLConstants;

import static org.wso2.ballerinalang.compiler.semantics.model.symbols.TaintRecord.TaintedStatus;
import static org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef.BLangRecordVarRefKeyValue;

/**
 * Generate taint-table for each invokable node.
 * <p>
 * Taint-table will contain the tainted status of return values, depending on the tainted status of parameters.
 * <p>
 * Propagate tainted status of variables across the program.
 * <p>
 * Evaluate invocations and generate errors if:
 * (*) Tainted value has been passed to a sensitive parameter.
 * (*) Tainted value has been passed to a global variable.
 *
 * @since 0.965.0
 */
public class TaintAnalyzer extends BLangNodeVisitor {
    private static final CompilerContext.Key<TaintAnalyzer> TAINT_ANALYZER_KEY = new CompilerContext.Key<>();

    private SymbolEnv env;
    private SymbolEnv currPkgEnv;
    private Names names;
    private SymbolTable symTable;
    private BLangDiagnosticLog dlog;

    private boolean overridingAnalysis = true;
    private boolean entryPointPreAnalysis;
    private boolean entryPointAnalysis;
    private boolean stopAnalysis;

    private List<BlockedNode> blockedNodeList;
    private List<BlockedNode> blockedEntryPointNodeList;
    private List<BInvokableSymbol> ignoredInvokableSymbol;

    private Stack<AnalysisState> analysisStateStack;
    private Set<TaintRecord.TaintError> dlogSet;
    private BLangFunction currTopLevelFunction;
    private boolean topLevelFunctionAllParamsUntaintedAnalysis;

    private static final String ANNOTATION_TAINTED = "tainted";
    private static final String ANNOTATION_UNTAINTED = "untainted";
    private static final String ANNOTATION_SENSITIVE = "sensitive";

    public static final int ALL_UNTAINTED_TABLE_ENTRY_INDEX = -1;

    private enum AnalyzerPhase {
        INITIAL_ANALYSIS,
        BLOCKED_NODE_ANALYSIS,
        LOOP_ANALYSIS,
        LOOP_ANALYSIS_COMPLETE,
        LOOPS_RESOLVED_ANALYSIS
    }

    private AnalyzerPhase analyzerPhase;

    public static TaintAnalyzer getInstance(CompilerContext context) {
        TaintAnalyzer taintAnalyzer = context.get(TAINT_ANALYZER_KEY);
        if (taintAnalyzer == null) {
            taintAnalyzer = new TaintAnalyzer(context);
        }
        return taintAnalyzer;
    }

    public TaintAnalyzer(CompilerContext context) {
        context.put(TAINT_ANALYZER_KEY, this);
        names = Names.getInstance(context);
        dlog = BLangDiagnosticLog.getInstance(context);
        symTable = SymbolTable.getInstance(context);
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        pkgNode.accept(this);
        return pkgNode;
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        blockedNodeList = new ArrayList<>();
        blockedEntryPointNodeList = new ArrayList<>();
        ignoredInvokableSymbol = new ArrayList<>();
        analysisStateStack = new Stack<>();
        dlogSet = new LinkedHashSet<>();
        currTopLevelFunction = null;
        topLevelFunctionAllParamsUntaintedAnalysis = false;
        analyzerPhase = AnalyzerPhase.INITIAL_ANALYSIS;
        if (pkgNode.completedPhases.contains(CompilerPhase.TAINT_ANALYZE)) {
            return;
        }
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgNode.symbol);
        analyze(pkgNode, pkgEnv);
        pkgNode.getTestablePkgs().forEach(testablePackage -> visit((BLangPackage) testablePackage));
    }

    private void analyze(BLangPackage pkgNode, SymbolEnv pkgEnv) {
        SymbolEnv prevPkgEnv = currPkgEnv;
        currPkgEnv = pkgEnv;
        env = pkgEnv;

        // Skip all lambda functions and analyzing them as part of the enclosing function.
        pkgNode.topLevelNodes.stream().filter(node -> node.getKind() != NodeKind.FUNCTION
                || !((BLangFunction) node).flagSet.contains(Flag.LAMBDA))
                .forEach(node -> {
                    AnalysisState analysisState = new AnalysisState();
                    analysisStateStack.push(analysisState);
                    ((BLangNode) node).accept(this);
                    analysisStateStack.pop();
                });

        analyzerPhase = AnalyzerPhase.BLOCKED_NODE_ANALYSIS;
        resolveBlockedInvokable(blockedNodeList);
        resolveBlockedInvokable(blockedEntryPointNodeList);

        if (dlogSet.size() > 0) {
            dlogSet.forEach(dlogEntry -> dlog.error(dlogEntry.pos, dlogEntry.diagnosticCode, dlogEntry.paramName));
        }
        currPkgEnv = prevPkgEnv;
        pkgNode.completedPhases.add(CompilerPhase.TAINT_ANALYZE);
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
        compUnit.topLevelNodes.forEach(e -> ((BLangNode) e).accept(this));
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        if (typeDefinition.typeNode.getKind() == NodeKind.OBJECT_TYPE
                || typeDefinition.typeNode.getKind() == NodeKind.RECORD_TYPE) {
            typeDefinition.typeNode.accept(this);
        }
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = symTable.pkgEnvMap.get(pkgSymbol);
        if (pkgEnv == null) {
            return;
        }
        env = pkgEnv;
        pkgEnv.node.accept(this);
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        xmlnsNode.namespaceURI.accept(this);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        if (funcNode.flagSet.contains(Flag.LAMBDA)) {
            funcNode.symbol.taintTable = null;
        } else {
            currTopLevelFunction = funcNode;
        }
        AnalysisState analysisState = new AnalysisState();
        analysisStateStack.push(analysisState);

        List<BLangSimpleVariable> defaultableParamsVarList = new ArrayList<>();
        funcNode.defaultableParams.forEach(defaultableParam -> defaultableParamsVarList.add(defaultableParam.var));

        analysisState.requiredParams = funcNode.requiredParams;
        analysisState.defaultableParams = defaultableParamsVarList;
        analysisState.restParam = funcNode.restParam;

        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);
        if (funcNode.flagSet.contains(Flag.RESOURCE) || CompilerUtils.isMainFunction(funcNode)) {
            // This is to analyze the entry-point function and attach taint table to it.
            entryPointPreAnalysis = true;
            boolean isBlocked = visitInvokable(funcNode, funcEnv);
            entryPointPreAnalysis = false;

            if (!isBlocked) {
                visitEntryPoint(funcNode, funcEnv);
            }
        } else {
            visitInvokable(funcNode, funcEnv);
        }
        analysisStateStack.pop();
    }

    @Override
    public void visit(BLangService serviceNode) {
        /* ignored */
    }

    @Override
    public void visit(BLangResource resourceNode) {
        /* ignored */
    }

    @Override
    public void visit(BLangObjectTypeNode objectNode) {
        BSymbol objectSymbol = objectNode.symbol;
        SymbolEnv objectEnv = SymbolEnv.createPkgLevelSymbolEnv(objectNode, objectSymbol.scope, env);
        objectNode.fields.forEach(field -> analyzeNode(field, objectEnv));
        if (objectNode.initFunction != null) {
            analyzeNode(objectNode.initFunction, objectEnv);
        }
        objectNode.functions.forEach(f -> analyzeNode(f, objectEnv));
    }

    @Override
    public void visit(BLangRecordTypeNode recordNode) {
        BSymbol objectSymbol = recordNode.symbol;
        SymbolEnv objectEnv = SymbolEnv.createPkgLevelSymbolEnv(recordNode, objectSymbol.scope, env);
        recordNode.fields.forEach(field -> analyzeNode(field, objectEnv));
        analyzeNode(recordNode.initFunction, objectEnv);
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
        if (bLangTupleVariable.expr != null) {
            SymbolEnv varInitEnv = SymbolEnv.createVarInitEnv(bLangTupleVariable, env, bLangTupleVariable.symbol);
            analyzeNode(bLangTupleVariable.expr, varInitEnv);
            setTaintedStatus(bLangTupleVariable, getCurrentAnalysisState().taintedStatus);
        }
    }

    @Override
    public void visit(BLangRecordVariable bLangRecordVariable) {
        if (bLangRecordVariable.expr != null) {
            SymbolEnv varInitEnv = SymbolEnv.createVarInitEnv(bLangRecordVariable, env, bLangRecordVariable.symbol);
            analyzeNode(bLangRecordVariable.expr, varInitEnv);
            setTaintedStatus(bLangRecordVariable, getCurrentAnalysisState().taintedStatus);
        }
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {
        // TODO: Complete
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        if (varNode.expr != null) {
            SymbolEnv varInitEnv = SymbolEnv.createVarInitEnv(varNode, env, varNode.symbol);
            analyzeNode(varNode.expr, varInitEnv);
            if (varNode.expr.getKind() == NodeKind.LAMBDA) {
                Map<Integer, TaintRecord> taintTable = ((BLangLambdaFunction) varNode.expr).function.symbol.taintTable;
                if (varNode.symbol.kind == SymbolKind.FUNCTION) {
                    ((BInvokableSymbol) varNode.symbol).taintTable = taintTable;
                }
            }
            setTaintedStatus(varNode, getCurrentAnalysisState().taintedStatus);
        }
    }

    @Override
    public void visit(BLangWorker workerNode) {
        /* ignore, remove later */
    }

    @Override
    public void visit(BLangEndpoint endpoint) {
        /* ignore */
    }

    @Override
    public void visit(BLangIdentifier identifierNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangAnnotation annotationNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        /* ignore */
    }

    // Statements
    @Override
    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, env);
        for (BLangStatement stmt : blockNode.stmts) {
            if (stopAnalysis) {
                break;
            } else {
                analyzeNode(stmt, blockEnv);
            }
        }
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        varDefNode.var.accept(this);
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        assignNode.expr.accept(this);
        BLangExpression varRefExpr = assignNode.varRef;
        visitAssignment(varRefExpr, getCurrentAnalysisState().taintedStatus, assignNode.pos);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignment) {
        compoundAssignment.varRef.accept(this);
        TaintedStatus varRefTaintedStatus = getCurrentAnalysisState().taintedStatus;

        compoundAssignment.expr.accept(this);
        TaintedStatus exprTaintedStatus = getCurrentAnalysisState().taintedStatus;

        TaintedStatus combinedTaintedStatus = getCombinedTaintedStatus(varRefTaintedStatus, exprTaintedStatus);
        visitAssignment(compoundAssignment.varRef, combinedTaintedStatus, compoundAssignment.pos);
    }

    private void visitAssignment(BLangExpression varRefExpr, TaintedStatus varTaintedStatus, DiagnosticPos pos) {
        if (varTaintedStatus != TaintedStatus.IGNORED) {
            // Generate error if a global variable has been assigned with a tainted value.
            if (varTaintedStatus == TaintedStatus.TAINTED && varRefExpr instanceof BLangVariableReference) {
                BLangVariableReference varRef = (BLangVariableReference) varRefExpr;
                if (varRef.symbol != null && varRef.symbol.owner != null
                        && (varRef.symbol.owner.getKind() == SymbolKind.PACKAGE
                        || (varRef.symbol.owner.flags & Flags.SERVICE) == Flags.SERVICE)) {
                    addTaintError(pos, varRef.symbol.name.value,
                            DiagnosticCode.TAINTED_VALUE_PASSED_TO_GLOBAL_VARIABLE);
                    return;
                } else if (varRef.symbol != null && varRef.symbol.closure && varTaintedStatus == TaintedStatus.TAINTED
                        && !varRef.symbol.tainted) {
                    addTaintError(pos, varRef.symbol.name.value,
                            DiagnosticCode.TAINTED_VALUE_PASSED_TO_CLOSURE_VARIABLE);
                    return;
                }
            }
            // TODO: Re-evaluating the full data-set (array) when a change occur.
            if (varRefExpr.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR) {
                overridingAnalysis = false;
                updatedVarRefTaintedState(varRefExpr, varTaintedStatus);
                overridingAnalysis = true;
            } else if (varRefExpr.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR) {
                BLangFieldBasedAccess fieldBasedAccessExpr = (BLangFieldBasedAccess) varRefExpr;
                // Propagate tainted status to fields, when field symbols are present (Example: struct).
                if (fieldBasedAccessExpr.symbol != null) {
                    setTaintedStatus(fieldBasedAccessExpr, varTaintedStatus);
                }
                overridingAnalysis = false;
                updatedVarRefTaintedState(fieldBasedAccessExpr, varTaintedStatus);
                overridingAnalysis = true;
            } else if (varRefExpr.getKind() == NodeKind.GROUP_EXPR) {
                BLangGroupExpr groupExpr = (BLangGroupExpr) varRefExpr;
                // Propagate tainted status to fields, when field symbols are present (Example: struct).
                visitAssignment(groupExpr.expression, varTaintedStatus, groupExpr.pos);
            } else if (varRefExpr.getKind() == NodeKind.LIST_CONSTRUCTOR_EXPR) {
                BLangListConstructorExpr listConstructorExpr = (BLangListConstructorExpr) varRefExpr;
                // Propagate tainted status to fields, when field symbols are present (Example: struct).
                listConstructorExpr.exprs.forEach(expr -> visitAssignment(expr, varTaintedStatus,
                        listConstructorExpr.pos));
            } else if (varRefExpr.getKind() == NodeKind.XML_ATTRIBUTE_ACCESS_EXPR) {
                overridingAnalysis = false;
                updatedVarRefTaintedState(((BLangXMLAttributeAccess) varRefExpr).expr, varTaintedStatus);
                overridingAnalysis = true;
            } else if (varRefExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                setTaintedStatus((BLangVariableReference) varRefExpr, varTaintedStatus);
            }
        }
    }

    private void updatedVarRefTaintedState(BLangExpression varRef, TaintedStatus taintedState) {
        if (varRef.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            setTaintedStatus((BLangVariableReference) varRef, taintedState);
        } else if (varRef.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR
                || varRef.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR) {
            BLangAccessExpression accessExpr = (BLangAccessExpression) varRef;
            updatedVarRefTaintedState(accessExpr.expr, taintedState);
        }
    }

    @Override
    public void visit(BLangAbort abortNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangRetry retryNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangContinue continueNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangBreak breakNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangReturn returnNode) {
        returnNode.expr.accept(this);
        if (getCurrentAnalysisState().taintedStatus == TaintedStatus.TAINTED) {
            getCurrentAnalysisState().returnTaintedStatus = TaintedStatus.TAINTED;
        }
        getCurrentAnalysisState().taintedStatus = getCurrentAnalysisState().returnTaintedStatus;
        updateParameterTaintedStatuses();
    }

    @Override
    public void visit(BLangPanic throwNode) {
        throwNode.expr.accept(this);
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        xmlnsStmtNode.xmlnsDecl.accept(this);
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        SymbolEnv stmtEnv = new SymbolEnv(exprStmtNode, this.env.scope);
        this.env.copyTo(stmtEnv);
        analyzeNode(exprStmtNode.expr, stmtEnv);
    }

    @Override
    public void visit(BLangIf ifNode) {
        overridingAnalysis = false;
        ifNode.body.accept(this);
        if (ifNode.elseStmt != null) {
            ifNode.elseStmt.accept(this);
        }
        overridingAnalysis = true;
    }

    @Override
    public void visit(BLangMatch matchStmt) {
        matchStmt.expr.accept(this);
        TaintedStatus observedTaintedStatusOfMatchExpr = getCurrentAnalysisState().taintedStatus;
        matchStmt.patternClauses.forEach(clause -> {
            getCurrentAnalysisState().taintedStatus = observedTaintedStatusOfMatchExpr;
            clause.accept(this);
        });
    }

    @Override
    public void visit(BLangMatch.BLangMatchStaticBindingPatternClause clause) {
        clause.body.accept(this);
    }

    @Override
    public void visit(BLangMatch.BLangMatchStructuredBindingPatternClause clause) {
        clause.body.accept(this);
    }

    @Override
    public void visit(BLangForeach foreach) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(foreach.body, env);
        // Propagate the tainted status of collection to foreach variables.
        foreach.collection.accept(this);
        if (getCurrentAnalysisState().taintedStatus == TaintedStatus.TAINTED) {
            setTaintedStatus((BLangVariable) foreach.variableDefinitionNode.getVariable(),
                    getCurrentAnalysisState().taintedStatus);
        }
        analyzeNode(foreach.body, blockEnv);
    }

    @Override
    public void visit(BLangWhile whileNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(whileNode.body, env);
        analyzeNode(whileNode.body, blockEnv);
    }

    @Override
    public void visit(BLangLock lockNode) {
        lockNode.body.accept(this);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        transactionNode.transactionBody.accept(this);
        overridingAnalysis = false;
        if (transactionNode.onRetryBody != null) {
            transactionNode.onRetryBody.accept(this);
        }
        if (transactionNode.committedBody != null) {
            transactionNode.committedBody.accept(this);
        }
        if (transactionNode.abortedBody != null) {
            transactionNode.abortedBody.accept(this);
        }
        overridingAnalysis = true;
    }

    @Override
    public void visit(BLangTryCatchFinally tryNode) {
        tryNode.tryBody.accept(this);
        overridingAnalysis = false;
        tryNode.catchBlocks.forEach(c -> c.accept(this));
        if (tryNode.finallyBody != null) {
            tryNode.finallyBody.accept(this);
        }
        overridingAnalysis = true;
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        stmt.expr.accept(this);
        // Propagate tainted status of each variable separately (when multi returns are used).
        for (int varIndex = 0; varIndex < stmt.varRef.expressions.size(); varIndex++) {
            BLangExpression varRefExpr = stmt.varRef.expressions.get(varIndex);
            visitAssignment(varRefExpr, getCurrentAnalysisState().taintedStatus, stmt.pos);
        }
    }

    @Override
    public void visit(BLangRecordDestructure stmt) {
        stmt.expr.accept(this);
        // Propagate tainted status of each variable separately (when multi returns are used).
        for (int varIndex = 0; varIndex < stmt.varRef.recordRefFields.size(); varIndex++) {
            BLangRecordVarRefKeyValue varRefExpr = stmt.varRef.recordRefFields.get(varIndex);
            visitAssignment(varRefExpr.variableReference, getCurrentAnalysisState().taintedStatus, stmt.pos);
        }
    }

    @Override
    public void visit(BLangErrorDestructure stmt) {
        // TODO: Complete
    }

    @Override
    public void visit(BLangCatch catchNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        /* ignore */
    }

    @Override
    public void visit(BLangOrderBy orderBy) {
        /* ignore */
    }

    @Override
    public void visit(BLangLimit limit) {
        /* ignore */
    }

    @Override
    public void visit(BLangGroupBy groupBy) {
        /* ignore */
    }

    @Override
    public void visit(BLangHaving having) {
        /* ignore */
    }

    @Override
    public void visit(BLangSelectExpression selectExpression) {
        /* ignore */
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        /* ignore */
    }

    @Override
    public void visit(BLangWhere whereClause) {
        /* ignore */
    }

    @Override
    public void visit(BLangStreamingInput streamingInput) {
        /* ignore */
    }

    @Override
    public void visit(BLangJoinStreamingInput joinStreamingInput) {
        /* ignore */
    }

    @Override
    public void visit(BLangTableQuery tableQuery) {
        /* ignore */
    }

    @Override
    public void visit(BLangStreamAction streamAction) {
        /* ignore */
    }

    @Override
    public void visit(BLangFunctionClause functionClause) {
        /* ignore */
    }

    @Override
    public void visit(BLangSetAssignment setAssignmentClause) {
        /* ignore */
    }

    @Override
    public void visit(BLangPatternStreamingEdgeInput patternStreamingEdgeInput) {
        /* ignore */
    }

    @Override
    public void visit(BLangWindow windowClause) {
        /* ignore */
    }

    @Override
    public void visit(BLangPatternStreamingInput patternStreamingInput) {
        /* ignore */
    }

    @Override
    public void visit(BLangTableLiteral tableLiteral) {
        // TODO: Improve to include tainted status identification for table literals
        getCurrentAnalysisState().taintedStatus = TaintedStatus.UNTAINTED;
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        if (workerSendNode.isChannel) {
            List<BLangExpression> exprsList = new ArrayList<>();
            exprsList.add(workerSendNode.expr);
            if (workerSendNode.keyExpr != null) {
                exprsList.add(workerSendNode.keyExpr);
            }
            analyzeExprList(exprsList);
            return;
        }
        workerSendNode.expr.accept(this);
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        syncSendExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        if (workerReceiveNode.isChannel) {
            List<BLangExpression> exprList = new ArrayList<>();
            if (workerReceiveNode.keyExpr != null) {
                exprList.add(workerReceiveNode.keyExpr);
            }
            analyzeExprList(exprList);
            return;
        }
        workerReceiveNode.sendExpression.accept(this);
    }

    // Expressions

    @Override
    public void visit(BLangLiteral literalExpr) {
        getCurrentAnalysisState().taintedStatus = TaintedStatus.UNTAINTED;
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        if (listConstructorExpr.exprs.size() == 0) {
            // Empty arrays are untainted.
            getCurrentAnalysisState().taintedStatus = TaintedStatus.UNTAINTED;
        } else {
            TaintedStatus isTainted = TaintedStatus.UNTAINTED;
            for (BLangExpression expression : listConstructorExpr.exprs) {
                expression.accept(this);
                // Used to update the variable this literal is getting assigned to.
                if (getCurrentAnalysisState().taintedStatus == TaintedStatus.TAINTED) {
                    isTainted = TaintedStatus.TAINTED;
                }
            }
            getCurrentAnalysisState().taintedStatus = isTainted;
        }
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        groupExpr.expression.accept(this);
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        TaintedStatus isTainted = TaintedStatus.UNTAINTED;
        for (BLangRecordLiteral.BLangRecordKeyValue keyValuePair : recordLiteral.keyValuePairs) {
            keyValuePair.valueExpr.accept(this);
            // Used to update the variable this literal is getting assigned to.
            if (getCurrentAnalysisState().taintedStatus == TaintedStatus.TAINTED) {
                isTainted = TaintedStatus.TAINTED;
            }
        }
        getCurrentAnalysisState().taintedStatus = isTainted;
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        if (varRefExpr.symbol == null) {
            Name varName = names.fromIdNode(varRefExpr.variableName);
            if (varName != Names.IGNORE) {
                if (varRefExpr.pkgSymbol.tag == SymTag.XMLNS) {
                    getCurrentAnalysisState().taintedStatus = varRefExpr.pkgSymbol.tainted ?
                            TaintedStatus.TAINTED : TaintedStatus.UNTAINTED;
                    return;
                }
            }
            getCurrentAnalysisState().taintedStatus = TaintedStatus.UNTAINTED;
        } else {
            // Taint information should be always taken from the original symbol
            BSymbol symbol;
            if ((varRefExpr.symbol.tag & SymTag.VARIABLE) == SymTag.VARIABLE) {
                BVarSymbol varSymbol = (BVarSymbol) varRefExpr.symbol;
                symbol = varSymbol.originalSymbol == null ? varSymbol : varSymbol.originalSymbol;
            } else {
                symbol = varRefExpr.symbol;
            }

            getCurrentAnalysisState().taintedStatus = symbol.tainted ? TaintedStatus.TAINTED : TaintedStatus.UNTAINTED;
        }
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        BType varRefType = fieldAccessExpr.expr.type;
        switch (varRefType.tag) {
            case TypeTags.OBJECT:
                fieldAccessExpr.expr.accept(this);
                break;
            case TypeTags.RECORD:
                fieldAccessExpr.expr.accept(this);
                break;
            case TypeTags.MAP:
                fieldAccessExpr.expr.accept(this);
                break;
            case TypeTags.JSON:
                fieldAccessExpr.expr.accept(this);
                break;
        }
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        indexAccessExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        // handle error constructor invocation
        if (invocationExpr.symbol != null && invocationExpr.symbol.kind == SymbolKind.ERROR_CONSTRUCTOR) {
            ((BInvokableSymbol) invocationExpr.symbol).taintTable = createIdentityTaintTable(invocationExpr);
        }

        if (invocationExpr.functionPointerInvocation) {
            // Skip function pointers and assume returns of function pointer executions are untainted.
            // TODO: Resolving function pointers / lambda expressions and perform analysis.
            getCurrentAnalysisState().taintedStatus = TaintedStatus.UNTAINTED;
        } else if (invocationExpr.iterableOperationInvocation) {
            invocationExpr.expr.accept(this);
            TaintedStatus exprTaintedStatus = getCurrentAnalysisState().taintedStatus;
            if (invocationExpr.argExprs != null) {
                invocationExpr.argExprs.forEach(argExpr -> {
                    // If argument of iterable operation is a lambda expression, propagate the tainted status
                    // to function parameters and validate function body.
                    if (argExpr.getKind() == NodeKind.LAMBDA) {
                        analyzeIterableLambdaInvocationArgExpression(argExpr);
                    }
                });
            }
            getCurrentAnalysisState().taintedStatus = exprTaintedStatus;
        } else if (invocationExpr.builtinMethodInvocation && (invocationExpr.builtInMethod != BLangBuiltInMethod.CALL
                || invocationExpr.symbol.name.value.startsWith(Constants.WORKER_LAMBDA_VAR_PREFIX))) {
            //TODO: Remove "WORKER_LAMBDA_VAR_PREFIX" check after worker interaction analysis is in place.
            analyzeBuiltInMethodInvocation(invocationExpr);
        } else if (invocationExpr.symbol != null) {
            BInvokableSymbol invokableSymbol = (BInvokableSymbol) invocationExpr.symbol;
            if (invokableSymbol.taintTable == null) {
                boolean blocked = true;
                if (ignoredInvokableSymbol.contains(invokableSymbol)) {
                    // If the current symbol was found to be a looping invocation earlier, and was ignored in a previous
                    // block resolution attempt, ignore it at the current cycle too. This will allow correctly analyzing
                    // functions with multiple looping invocations. In this condition, we do not change `analyzerPhase`
                    // to `LOOP_ANALYSIS_COMPLETE`, allowing the next looping invocation also to be marked also as
                    // `IGNORED`.
                    getCurrentAnalysisState().taintedStatus = TaintedStatus.IGNORED;
                    blocked = false;
                } else if (analyzerPhase == AnalyzerPhase.LOOP_ANALYSIS) {
                    // If a looping invocation is being analyzed, skip analysis of next invokable that does not have
                    // a taint table already attached. This will prevent the analyzer to go in to a loop unnecessarily.
                    getCurrentAnalysisState().taintedStatus = TaintedStatus.IGNORED;
                    analyzerPhase = AnalyzerPhase.LOOP_ANALYSIS_COMPLETE;
                    ignoredInvokableSymbol.add(invokableSymbol);
                    blocked = false;
                } else if (analyzerPhase == AnalyzerPhase.LOOP_ANALYSIS_COMPLETE
                        && invocationExpr.builtinMethodInvocation
                        && invocationExpr.builtInMethod == BLangBuiltInMethod.CALL) {
                    // When "call" is use to invoke function pointers, taint-table of the actual function to be invoked
                    // is not known. Therefore, if the analyzer is blocked on such function pointer invocation, skip
                    // taint analysis and consider the outcome of the invocation as untainted.
                    // TODO: Resolving function pointers and perform analysis.
                    getCurrentAnalysisState().taintedStatus = TaintedStatus.IGNORED;
                    ignoredInvokableSymbol.add(invokableSymbol);
                    blocked = false;
                }
                if (blocked) {
                    // If taint-table of invoked function is not generated yet, add it to the blocked list for latter
                    // processing.
                    addToBlockedList(invocationExpr);
                }
            } else {
                analyzeInvocation(invocationExpr);
            }
        }
    }

    private Map<Integer, TaintRecord> createIdentityTaintTable(BLangInvocation invocationExpr) {
        Map<Integer, TaintRecord> taintTable = new HashMap<>();

        int requiredParamCount = invocationExpr.requiredArgs.size();
        int defaultableParamCount = invocationExpr.namedArgs.size();
        int totalParamCount = requiredParamCount + defaultableParamCount + (invocationExpr.restArgs == null ? 0 : 1);

        for (int i = ALL_UNTAINTED_TABLE_ENTRY_INDEX; i < totalParamCount; i++) {
            TaintRecord record = new TaintRecord(
                    i == ALL_UNTAINTED_TABLE_ENTRY_INDEX ? TaintedStatus.UNTAINTED : TaintedStatus.TAINTED,
                    new ArrayList<>());
            taintTable.put(i, record);

            for (int j = 0; j < totalParamCount; j++) {
                record.parameterTaintedStatusList.add(TaintedStatus.UNTAINTED);
            }
        }

        return taintTable;
    }

    private void analyzeBuiltInMethodInvocation(BLangInvocation invocationExpr) {
        BLangBuiltInMethod builtInMethod = invocationExpr.builtInMethod;
        switch (builtInMethod) {
            case IS_NAN:
            case IS_INFINITE:
            case IS_FINITE:
            case LENGTH:
            case IS_FROZEN:
                getCurrentAnalysisState().taintedStatus = TaintedStatus.UNTAINTED;
                break;
            case FREEZE:
            case CLONE:
                invocationExpr.expr.accept(this);
                break;
            case STAMP:
            case CONVERT:
            case CALL:
                invocationExpr.argExprs.forEach(expression -> expression.accept(this));
                break;
            case REASON:
            case DETAIL:
            case STACKTRACE:
                invocationExpr.expr.accept(this);
                break;
            default:
                throw new AssertionError("Taint checking failed for built-in method: " + builtInMethod);
        }
    }

    @Override
    public void visit(BLangTypeInit typeInit) {
        TaintedStatus typeTaintedStatus = TaintedStatus.UNTAINTED;
        for (BLangExpression expr : typeInit.argsExpr) {
            expr.accept(this);
            // TODO: Improve: If one value ot type init is tainted, the complete type is tainted.
            if (getCurrentAnalysisState().taintedStatus == TaintedStatus.TAINTED) {
                typeTaintedStatus = TaintedStatus.TAINTED;
            }
        }

        // If this is an object init using the default constructor, or a stream or channel initialization then skip the
        // taint checking.
        if (typeInit.type.tag != TypeTags.STREAM && typeInit.type.tag != TypeTags.CHANNEL &&
                (typeInit.type.tag != TypeTags.OBJECT ||
                         ((BObjectTypeSymbol) typeInit.type.tsymbol).initializerFunc != null)) {
            typeInit.initInvocation.accept(this);
        }

        getCurrentAnalysisState().taintedStatus = typeTaintedStatus;
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        /* ignore */
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        overridingAnalysis = false;
        ternaryExpr.thenExpr.accept(this);
        TaintedStatus thenTaintedCheckResult = getCurrentAnalysisState().taintedStatus;
        ternaryExpr.elseExpr.accept(this);
        overridingAnalysis = true;
        TaintedStatus elseTaintedCheckResult = getCurrentAnalysisState().taintedStatus;
        getCurrentAnalysisState().taintedStatus = getCombinedTaintedStatus(thenTaintedCheckResult,
                elseTaintedCheckResult);
    }

    @Override
    public void visit(BLangWaitExpr waitExpr) {
        waitExpr.getExpression().accept(this);
    }

    @Override
    public void visit(BLangWaitForAllExpr waitExpr) {
        TaintedStatus statusForWait = TaintedStatus.UNTAINTED;
        for (BLangWaitForAllExpr.BLangWaitKeyValue keyValue: waitExpr.keyValuePairs) {
            BLangExpression expr = keyValue.valueExpr != null ? keyValue.valueExpr : keyValue.keyExpr;
            expr.accept(this);
            statusForWait = (getCurrentAnalysisState().taintedStatus == TaintedStatus.TAINTED) ? TaintedStatus.TAINTED :
                                                                            TaintedStatus.UNTAINTED;
        }
        getCurrentAnalysisState().taintedStatus = statusForWait;
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        // Need to handle this properly. The flush expression can only return error or nil. ATM tainted status is set to
        // be untainted
        getCurrentAnalysisState().taintedStatus = TaintedStatus.UNTAINTED;
    }


    @Override
    public void visit(BLangTrapExpr trapExpr) {
        trapExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        binaryExpr.lhsExpr.accept(this);
        TaintedStatus lhsTaintedCheckResult = getCurrentAnalysisState().taintedStatus;
        binaryExpr.rhsExpr.accept(this);
        TaintedStatus rhsTaintedCheckResult = getCurrentAnalysisState().taintedStatus;
        getCurrentAnalysisState().taintedStatus = getCombinedTaintedStatus(lhsTaintedCheckResult,
                rhsTaintedCheckResult);
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        elvisExpr.lhsExpr.accept(this);
        TaintedStatus lhsTaintedCheckResult = getCurrentAnalysisState().taintedStatus;
        elvisExpr.rhsExpr.accept(this);
        TaintedStatus rhsTaintedCheckResult = getCurrentAnalysisState().taintedStatus;
        getCurrentAnalysisState().taintedStatus = getCombinedTaintedStatus(lhsTaintedCheckResult,
                rhsTaintedCheckResult);
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        switch (unaryExpr.operator) {
            case LENGTHOF:
                getCurrentAnalysisState().taintedStatus = TaintedStatus.UNTAINTED;
                break;
            case UNTAINT:
                getCurrentAnalysisState().taintedStatus = TaintedStatus.UNTAINTED;
                break;
            default:
                unaryExpr.expr.accept(this);
                break;
        }
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {
        getCurrentAnalysisState().taintedStatus = TaintedStatus.UNTAINTED;
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        // Result of the conversion is tainted if value being converted is tainted.
        conversionExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        getCurrentAnalysisState().taintedStatus = TaintedStatus.UNTAINTED;
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        xmlAttribute.name.accept(this);
        TaintedStatus attrNameTaintedStatus = getCurrentAnalysisState().taintedStatus;
        xmlAttribute.value.accept(this);
        TaintedStatus attrValueTaintedStatus = getCurrentAnalysisState().taintedStatus;
        getCurrentAnalysisState().taintedStatus = getCombinedTaintedStatus(attrNameTaintedStatus,
                attrValueTaintedStatus);
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        // Visit in-line namespace declarations
        boolean inLineNamespaceTainted = false;
        for (BLangXMLAttribute attribute : xmlElementLiteral.attributes) {
            if (attribute.name.getKind() == NodeKind.XML_QNAME && ((BLangXMLQName) attribute.name).prefix.value
                    .equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                attribute.accept(this);
                if (getCurrentAnalysisState().taintedStatus == TaintedStatus.IGNORED) {
                    return;
                }
                setTaintedStatus(attribute.symbol, getCurrentAnalysisState().taintedStatus);
                if (attribute.symbol.tainted) {
                    inLineNamespaceTainted = true;
                }
            }
        }

        // Visit attributes.
        boolean attributesTainted = false;
        for (BLangXMLAttribute attribute : xmlElementLiteral.attributes) {
            if (attribute.name.getKind() == NodeKind.XML_QNAME && !((BLangXMLQName) attribute.name).prefix.value
                    .equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                attribute.accept(this);
                if (getCurrentAnalysisState().taintedStatus == TaintedStatus.IGNORED) {
                    return;
                }
                setTaintedStatus(attribute.symbol, getCurrentAnalysisState().taintedStatus);
                if (attribute.symbol.tainted) {
                    attributesTainted = true;
                }
            }
        }

        // Visit the tag names
        xmlElementLiteral.startTagName.accept(this);
        if (getCurrentAnalysisState().taintedStatus == TaintedStatus.IGNORED) {
            return;
        }
        TaintedStatus startTagTaintedStatus = getCurrentAnalysisState().taintedStatus;
        TaintedStatus endTagTaintedStatus = TaintedStatus.UNTAINTED;
        if (xmlElementLiteral.endTagName != null) {
            xmlElementLiteral.endTagName.accept(this);
            if (getCurrentAnalysisState().taintedStatus == TaintedStatus.IGNORED) {
                return;
            }
            endTagTaintedStatus = getCurrentAnalysisState().taintedStatus;
        }
        boolean tagNamesTainted = startTagTaintedStatus == TaintedStatus.TAINTED
                || endTagTaintedStatus == TaintedStatus.TAINTED;

        // Visit the children
        boolean childrenTainted = false;
        for (BLangExpression expr : xmlElementLiteral.children) {
            expr.accept(this);
            if (getCurrentAnalysisState().taintedStatus == TaintedStatus.IGNORED) {
                return;
            }
            if (getCurrentAnalysisState().taintedStatus == TaintedStatus.TAINTED) {
                childrenTainted = true;
            }
        }

        getCurrentAnalysisState().taintedStatus = inLineNamespaceTainted || attributesTainted || tagNamesTainted
                || childrenTainted ? TaintedStatus.TAINTED : TaintedStatus.UNTAINTED;
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        analyzeExprList(xmlTextLiteral.textFragments);
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        analyzeExprList(xmlCommentLiteral.textFragments);
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        xmlProcInsLiteral.target.accept(this);
        if (getCurrentAnalysisState().taintedStatus == TaintedStatus.UNTAINTED) {
            analyzeExprList(xmlProcInsLiteral.dataFragments);
        }
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        analyzeExprList(xmlQuotedString.textFragments);
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        analyzeExprList(stringTemplateLiteral.exprs);
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        bLangLambdaFunction.function.accept(this);
        getCurrentAnalysisState().taintedStatus = TaintedStatus.UNTAINTED;
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        /* ignore */
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        xmlAttributeAccessExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        getCurrentAnalysisState().taintedStatus = TaintedStatus.UNTAINTED;
    }

    @Override
    public void visit(BLangTableQueryExpression tableQueryExpression) {
        /* ignore */
    }

    @Override
    public void visit(BLangRestArgsExpression varArgsExpression) {
        varArgsExpression.expr.accept(this);
    }

    @Override
    public void visit(BLangNamedArgsExpression namedArgsExpression) {
        namedArgsExpression.expr.accept(this);
    }

    @Override
    public void visit(BLangStreamingQueryStatement streamingQueryStatement) {
        /* ignore */
    }

    @Override
    public void visit(BLangWithinClause withinClause) {
        /* ignore */
    }

    @Override
    public void visit(BLangPatternClause patternClause) {
        /* ignore */
    }

    @Override
    public void visit(BLangForever foreverStatement) {
        /* ignore */
    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {
        bLangMatchExpression.expr.accept(this);
    }

    @Override
    public void visit(BLangCheckedExpr match) {
        match.expr.accept(this);
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanicExpr) {
        checkPanicExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        // todo: set correct tainted status of the service by checking variables within service constructor.
        getCurrentAnalysisState().taintedStatus = TaintedStatus.UNTAINTED;
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        typeTestExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        annotAccessExpr.expr.accept(this);
    }

    // Type nodes

    @Override
    public void visit(BLangValueType valueType) {
        /* ignore */
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        /* ignore */
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
        /* ignore */
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        /* ignore */
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        /* ignore */
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        /* ignore */
    }

    // expressions that will used only from the Desugar phase

    @Override
    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {
        /* ignore */
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {
        /* ignore */
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {
        /* ignore */
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {
        /* ignore */
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangTypeLoad typeLoad) {
        /* ignore */
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {
        /* ignore */
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef functionVarRef) {
        /* ignore */
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {
        /* ignore */
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {
        /* ignore */
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTupleAccessExpr arrayIndexAccessExpr) {
        /* ignore */
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlIndexAccessExpr) {
        /* ignore */
    }

    @Override
    public void visit(BLangRecordLiteral.BLangJSONLiteral jsonLiteral) {
        /* ignore */
    }

    @Override
    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {
        /* ignore */
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {
        /* ignore */
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStreamLiteral streamLiteral) {
        /* ignore */
    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation bFunctionPointerInvocation) {
        /* ignore */
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
        /* ignore */
    }

    @Override
    public void visit(BLangArrayLiteral.BLangJSONArrayLiteral jsonArrayLiteral) {
        /* ignore */
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {
        /* ignore */
    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangXMLSequenceLiteral bLangXMLSequenceLiteral) {
        /* ignore */
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
        /* ignore */
    }

    @Override
    public void visit(BLangConstant constant) {
        /* ignore */
    }

    // Private

    private <T extends BLangNode, U extends SymbolEnv> void analyzeNode(T t, U u) {
        SymbolEnv prevEnv = this.env;
        this.env = u;
        t.accept(this);
        this.env = prevEnv;
    }

    @Override
    public void visit(BLangTupleVariableDef bLangTupleVariableDef) {
        visit(bLangTupleVariableDef.var);
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        visit(bLangRecordVariableDef.var);
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        visit(bLangErrorVariableDef.errorVariable);
    }

    /**
     * If any one of the given expressions are tainted, the final result will be tainted.
     *
     * @param exprs List of expressions to analyze.
     */
    private void analyzeExprList(List<BLangExpression> exprs) {
        for (BLangExpression expr : exprs) {
            expr.accept(this);
            if (getCurrentAnalysisState().taintedStatus == TaintedStatus.TAINTED) {
                break;
            }
        }
    }

    private boolean hasAnnotation(BLangSimpleVariable variable, String expectedAnnotation) {
        return hasAnnotation(variable.annAttachments, expectedAnnotation);
    }

    private boolean hasAnnotation(List<BLangAnnotationAttachment> annotationAttachmentList, String expectedAnnotation) {
        return annotationAttachmentList.stream().anyMatch(annotation ->
                annotation.annotationName.value.equals(expectedAnnotation));
    }

    /**
     * Combines multiple tainted-statuses together and decide the final tainted-status.
     * If any one of the combined tainted-statuses is "tainted", the combined status is tainted.
     * <p>
     * Example: string x = a + b;
     * if "a" or "b" is tainted "x" is tainted.
     *
     * @param taintedStatuses list of tainted statues
     * @return tainted status after combining multiple tainted statuses
     */
    private TaintedStatus getCombinedTaintedStatus(TaintedStatus... taintedStatuses) {
        TaintedStatus combinedTaintedStatus = TaintedStatus.IGNORED;
        for (TaintedStatus taintedStatus : taintedStatuses) {
            if (taintedStatus == TaintedStatus.TAINTED) {
                combinedTaintedStatus = TaintedStatus.TAINTED;
            } else if (combinedTaintedStatus == TaintedStatus.IGNORED && taintedStatus == TaintedStatus.UNTAINTED) {
                combinedTaintedStatus = TaintedStatus.UNTAINTED;
            }
        }
        return combinedTaintedStatus;
    }

    /**
     * Set tainted status of the variable. When non-overriding analysis is in progress, this will not override "tainted"
     * status with "untainted" status. As an example, the "else" section of a "if-else" block, cannot change a value
     * marked "tainted" by the "if" block.
     *
     * @param varNode       Variable node to be updated.
     * @param taintedStatus Tainted status.
     */
    private void setTaintedStatus(BLangVariable varNode, TaintedStatus taintedStatus) {
        if (varNode.getKind() == NodeKind.RECORD_VARIABLE) {
            ((BLangRecordVariable) varNode).variableList
                    .forEach(variable -> setTaintedStatus(variable.valueBindingPattern, taintedStatus));
            return;
        }

        if (varNode.getKind() == NodeKind.TUPLE_VARIABLE) {
            ((BLangTupleVariable) varNode).memberVariables
                    .forEach(variable -> setTaintedStatus(variable, taintedStatus));
            return;
        }

        if (varNode.getKind() == NodeKind.VARIABLE) {
            BLangSimpleVariable simpleVarNode = (BLangSimpleVariable) varNode;
            if (taintedStatus != TaintedStatus.IGNORED && (overridingAnalysis || !simpleVarNode.symbol.tainted)) {
                setTaintedStatus(simpleVarNode.symbol, taintedStatus);
            }
        }
    }

    /**
     * Set tainted status of the variable. When non-overriding analysis is in progress, this will not override "tainted"
     * status with "untainted" status. As an example, the "else" section of a "if-else" block, cannot change a value
     * marked "tainted" by the "if" block.
     *
     * @param varNode       Variable node to be updated.
     * @param taintedStatus Tainted status.
     */
    private void setTaintedStatus(BLangVariableReference varNode, TaintedStatus taintedStatus) {
        if (taintedStatus != TaintedStatus.IGNORED && (overridingAnalysis || (varNode.symbol != null
                && !varNode.symbol.tainted))) {
            setTaintedStatus(varNode.symbol, taintedStatus);
        }
    }

    private void setTaintedStatus(BSymbol symbol, TaintedStatus taintedStatus) {
        if (taintedStatus != TaintedStatus.IGNORED && symbol != null) {
            if (taintedStatus == TaintedStatus.TAINTED) {
                symbol.tainted = true;
            } else {
                symbol.tainted = false;
            }
        }
    }

    // Private methods related to invokable node analysis and taint-table generation.

    private void visitEntryPoint(BLangInvokableNode invNode, SymbolEnv funcEnv) {
        if (analyzerPhase == AnalyzerPhase.LOOP_ANALYSIS) {
            return;
        }
        // Entry point input parameters are all tainted, since they contain user controlled data.
        // If any value has been marked "sensitive" generate an error.
        if (isEntryPointParamsInvalid(invNode.requiredParams)) {
            return;
        }
        List<BLangSimpleVariable> defaultableParamsVarList = new ArrayList<>();
        invNode.defaultableParams.forEach(defaultableParam -> defaultableParamsVarList.add(defaultableParam.var));
        if (isEntryPointParamsInvalid(defaultableParamsVarList)) {
            return;
        }
        if (invNode.restParam != null && isEntryPointParamsInvalid(Collections.singletonList(invNode.restParam))) {
            return;
        }
        // Perform end point analysis.
        entryPointAnalysis = true;
        analyzeReturnTaintedStatus(invNode, funcEnv);
        entryPointAnalysis = false;

        boolean isBlocked = processBlockedNode(invNode);
        if (!isBlocked) {
            // Display errors only if scan of was fully complete, so that errors will not get duplicated.
            this.dlogSet.addAll(getCurrentAnalysisState().taintErrorSet);
        }
    }

    private boolean isEntryPointParamsInvalid(List<BLangSimpleVariable> params) {
        if (params != null) {
            for (BLangSimpleVariable param : params) {
                param.symbol.tainted = true;
                if (hasAnnotation(param, ANNOTATION_SENSITIVE)) {
                    this.dlog.error(param.pos, DiagnosticCode.ENTRY_POINT_PARAMETERS_CANNOT_BE_SENSITIVE,
                            param.name.value);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Analyze the invokable body and identify the tainted status of the return value and the tainted status of
     * parameters after the completion of the invokable. Based on that, attach the created taint table explaining
     * possible taint outcomes of the function.
     *
     * @param invNode   invokable node to be analyzed
     * @param symbolEnv symbol environment for the invokable
     * @return if the invocation is blocked due to an unanalyzed invocation
     */
    private boolean visitInvokable(BLangInvokableNode invNode, SymbolEnv symbolEnv) {
        if (analyzerPhase == AnalyzerPhase.LOOPS_RESOLVED_ANALYSIS || invNode.symbol.taintTable == null
                || (invNode.getKind() == NodeKind.FUNCTION && ((BLangFunction) invNode).attachedOuterFunction)) {
            if (Symbols.isNative(invNode.symbol)
                    || (invNode.getKind() == NodeKind.FUNCTION && ((BLangFunction) invNode).interfaceFunction)) {
                attachNativeFunctionTaintTable(invNode);
                return false;
            }
            Map<Integer, TaintRecord> taintTable = new HashMap<>();

            // Check the tainted status of return values when no parameter is tainted.
            analyzeAllParamsUntaintedReturnTaintedStatus(taintTable, invNode, symbolEnv);
            if (analyzerPhase == AnalyzerPhase.LOOP_ANALYSIS_COMPLETE) {
                analyzerPhase = AnalyzerPhase.LOOP_ANALYSIS;
            }
            boolean isBlocked;
            if (invNode.flagSet.contains(Flag.LAMBDA)) {
                // If function analysis is blocked and current a lambda function is being analyzed, mark the enclosing
                // top level function as blocked.
                isBlocked = processBlockedNode(currTopLevelFunction);
            } else {
                isBlocked = processBlockedNode(invNode);
            }
            if (isBlocked) {
                return true;
            }

            int requiredParamCount = invNode.requiredParams.size();
            int defaultableParamCount = invNode.defaultableParams.size();
            int totalParamCount = requiredParamCount + defaultableParamCount + (invNode.restParam == null ? 0 : 1);
            if (getCurrentAnalysisState().taintErrorSet.size() > 0) {
                // If taint error occurred when no parameter is tainted, there is no point of checking tainted status of
                // returns when each parameter is tainted. An compiler error will get generated for the usage anyway,
                // hence adding dummy table to the function to make sure remaining analysis stays intact.
                List<TaintedStatus> paramTaintedStatus = Collections.nCopies(totalParamCount, TaintedStatus.UNTAINTED);
                taintTable.put(ALL_UNTAINTED_TABLE_ENTRY_INDEX, new TaintRecord(TaintedStatus.UNTAINTED,
                        paramTaintedStatus));
                for (int paramIndex = 0; paramIndex < totalParamCount; paramIndex++) {
                    taintTable.put(paramIndex, new TaintRecord(TaintedStatus.UNTAINTED, paramTaintedStatus));
                }
                getCurrentAnalysisState().taintErrorSet.clear();
            } else {
                for (int paramIndex = 0; paramIndex < totalParamCount; paramIndex++) {
                    BLangSimpleVariable param = getParam(invNode, paramIndex, requiredParamCount,
                            defaultableParamCount);
                    // If parameter is sensitive, it's invalid to have a case where tainted status of parameter is true.
                    if (hasAnnotation(param, ANNOTATION_SENSITIVE)) {
                        continue;
                    }
                    // Set each parameter "tainted", then analyze the body to observe the outcome of the function.
                    analyzeReturnTaintedStatus(taintTable, invNode, symbolEnv, paramIndex, requiredParamCount,
                            defaultableParamCount);
                    if (analyzerPhase == AnalyzerPhase.LOOP_ANALYSIS_COMPLETE) {
                        analyzerPhase = AnalyzerPhase.LOOP_ANALYSIS;
                    }
                    getCurrentAnalysisState().taintErrorSet.clear();
                }
            }
            invNode.symbol.taintTable = taintTable;
        }
        return false;
    }

    private void analyzeAllParamsUntaintedReturnTaintedStatus(Map<Integer, TaintRecord> taintTable,
                                                              BLangInvokableNode invokableNode, SymbolEnv symbolEnv) {
        // Identify if current analysis is to identify return tainted status when all parameters are untainted in a top
        // level function. This is to separately identify if AllParamsUntainted analysis is being done for a top level
        // function or a lambda functions / worker lambda function.
        if (currTopLevelFunction == invokableNode) {
            topLevelFunctionAllParamsUntaintedAnalysis = true;
        }
        analyzeReturnTaintedStatus(taintTable, invokableNode, symbolEnv, ALL_UNTAINTED_TABLE_ENTRY_INDEX, 0, 0);
        if (currTopLevelFunction == invokableNode) {
            topLevelFunctionAllParamsUntaintedAnalysis = false;
        }
    }

    private void analyzeReturnTaintedStatus(Map<Integer, TaintRecord> taintTable, BLangInvokableNode invokableNode,
                                            SymbolEnv symbolEnv, int paramIndex, int requiredParamCount,
                                            int defaultableParamCount) {
        getCurrentAnalysisState().returnTaintedStatus = TaintedStatus.UNTAINTED;
        getCurrentAnalysisState().parameterTaintedStatus = new ArrayList<>();
        resetTaintedStatusOfVariables(invokableNode.requiredParams);
        resetTaintedStatusOfVariableDef(invokableNode.defaultableParams);
        if (invokableNode.restParam != null) {
            resetTaintedStatusOfVariables(Collections.singletonList(invokableNode.restParam));
        }
        // Mark the given parameter "tainted".
        if (paramIndex != ALL_UNTAINTED_TABLE_ENTRY_INDEX) {
            if (paramIndex < requiredParamCount) {
                invokableNode.requiredParams.get(paramIndex).symbol.tainted = true;
            } else if (paramIndex < requiredParamCount + defaultableParamCount) {
                invokableNode.defaultableParams.get(paramIndex - requiredParamCount).var.symbol.tainted = true;
            } else {
                if (invokableNode.restParam != null) {
                    invokableNode.restParam.symbol.tainted = true;
                }
            }
        }
        analyzeReturnTaintedStatus(invokableNode, symbolEnv);
        if (getCurrentAnalysisState().taintErrorSet.size() > 0) {
            // When invocation returns an error (due to passing a tainted argument to a sensitive parameter) add current
            // error to the table for future reference. However, if taint-error is raised when analyzing all-parameters
            // are untainted, the code of the function is wrong (and passes a tainted value generated within the
            // function body to a sensitive parameter). Hence, instead of adding error to table, directly generate the
            // error and fail the compilation.
            if (paramIndex == ALL_UNTAINTED_TABLE_ENTRY_INDEX
                    && (topLevelFunctionAllParamsUntaintedAnalysis || entryPointAnalysis)
                    && (analyzerPhase == AnalyzerPhase.INITIAL_ANALYSIS
                            || analyzerPhase == AnalyzerPhase.BLOCKED_NODE_ANALYSIS
                            || analyzerPhase == AnalyzerPhase.LOOPS_RESOLVED_ANALYSIS)) {
                this.dlogSet.addAll(getCurrentAnalysisState().taintErrorSet);
            } else {
                taintTable.put(paramIndex, new TaintRecord(new ArrayList<>(getCurrentAnalysisState().taintErrorSet)));
            }
        } else if (getCurrentAnalysisState().blockedNode == null) {
            // This is when taint analysis was successful for the function body without any blocking invocations.
            if (invokableNode.returnTypeNode.type != symTable.nilType) {
                // If return values are annotated with "tainted" or "untainted" annotations, update the observed tainted
                // status with the annotated statue.
                TaintedStatus taintedStatusBasedOnAnnotations =
                        getTaintedStatusBasedOnAnnotations(invokableNode.returnTypeAnnAttachments);
                if (taintedStatusBasedOnAnnotations != TaintedStatus.IGNORED) {
                    getCurrentAnalysisState().returnTaintedStatus = taintedStatusBasedOnAnnotations;
                }
            } else {
                // If function has no return, "untainted" is written as the tainted state of the return, to denote that
                // the parameter being analyzed can be a tainted value without causing a taint-error.
                getCurrentAnalysisState().returnTaintedStatus = TaintedStatus.UNTAINTED;
            }

            updateParameterTaintedStatuses();
            taintTable.put(paramIndex, new TaintRecord(getCurrentAnalysisState().returnTaintedStatus,
                    getCurrentAnalysisState().parameterTaintedStatus));
        }
    }

    private void resetTaintedStatusOfVariables(List<BLangSimpleVariable> params) {
        if (params != null) {
            params.forEach(param -> param.symbol.tainted = false);
        }
    }

    private void resetTaintedStatusOfVariableDef(List<BLangSimpleVariableDef> params) {
        if (params != null) {
            List<BLangSimpleVariable> defaultableParamsVarList = new ArrayList<>();
            params.forEach(defaultableParam -> defaultableParamsVarList.add(defaultableParam.var));
            resetTaintedStatusOfVariables(defaultableParamsVarList);
        }
    }

    private void analyzeReturnTaintedStatus(BLangInvokableNode invokableNode, SymbolEnv symbolEnv) {
        getCurrentAnalysisState().returnTaintedStatus = TaintedStatus.UNTAINTED;
        getCurrentAnalysisState().parameterTaintedStatus = new ArrayList<>();
        analyzeNode(invokableNode.body, symbolEnv);
        if (stopAnalysis) {
            stopAnalysis = false;
        }
    }

    private void attachNativeFunctionTaintTable(BLangInvokableNode invokableNode) {
        if (invokableNode.symbol.taintTable == null) {
            // Extract tainted status of the function by looking at annotations added to returns.
            boolean hasTaintedAnnotation = hasAnnotation(invokableNode.returnTypeAnnAttachments, ANNOTATION_TAINTED);
            TaintedStatus retParamsTaintedStatus =
                    hasTaintedAnnotation ? TaintedStatus.TAINTED : TaintedStatus.UNTAINTED;

            int requiredParamCount = invokableNode.requiredParams.size();
            int defaultableParamCount = invokableNode.defaultableParams.size();
            int totalParamCount =
                    requiredParamCount + defaultableParamCount + (invokableNode.restParam == null ? 0 : 1);

            // Since this native function is being analyzed, the parameter tainted state depends on annotations.
            // If no parameter annotations are present, leave the tainted state of the argument unchanged.
            List<TaintedStatus> paramTaintedStatusList = new ArrayList<>();
            for (int paramIndex = 0; paramIndex < totalParamCount; paramIndex++) {
                BLangVariable param = getParam(invokableNode, paramIndex, requiredParamCount, defaultableParamCount);
                TaintedStatus taintedStateBasedOnAnnotations = getTaintedStatusBasedOnAnnotations(param.annAttachments);
                paramTaintedStatusList.add(taintedStateBasedOnAnnotations);
            }

            // Append taint table with tainted status when no parameter is tainted.
            Map<Integer, TaintRecord> taintTable = new HashMap<>();
            taintTable.put(ALL_UNTAINTED_TABLE_ENTRY_INDEX, new TaintRecord(retParamsTaintedStatus,
                    paramTaintedStatusList));

            // Append taint table with tainted status when each parameter is tainted.
            for (int paramIndex = 0; paramIndex < totalParamCount; paramIndex++) {
                BLangSimpleVariable param = getParam(invokableNode, paramIndex, requiredParamCount,
                        defaultableParamCount);
                // If parameter is sensitive, test for this parameter being tainted is invalid.
                if (hasAnnotation(param, ANNOTATION_SENSITIVE)) {
                    continue;
                }
                taintTable.put(paramIndex, new TaintRecord(retParamsTaintedStatus, paramTaintedStatusList));
            }
            invokableNode.symbol.taintTable = taintTable;
        }
    }

    private TaintedStatus getTaintedStatusBasedOnAnnotations(List<BLangAnnotationAttachment> annotations) {
        if (hasAnnotation(annotations, ANNOTATION_UNTAINTED)) {
            return TaintedStatus.UNTAINTED;
        }
        if (hasAnnotation(annotations, ANNOTATION_TAINTED)) {
            return TaintedStatus.TAINTED;
        }
        return TaintedStatus.IGNORED;
    }

    private boolean processBlockedNode(BLangInvokableNode invokableNode) {
        boolean isBlocked = false;
        if (getCurrentAnalysisState().blockedNode != null) {
            // Add the function being blocked into the blocked node list for later processing.
            getCurrentAnalysisState().blockedNode.invokableNode = invokableNode;
            if (analyzerPhase == AnalyzerPhase.INITIAL_ANALYSIS) {
                if (entryPointPreAnalysis || entryPointAnalysis) {
                    blockedEntryPointNodeList.add(getCurrentAnalysisState().blockedNode);
                } else {
                    blockedNodeList.add(getCurrentAnalysisState().blockedNode);
                }
            }
            getCurrentAnalysisState().blockedNode = null;
            // Discard any error generated if invokable was found to be blocked. This will avoid duplicates when
            // blocked invokable is re-examined.
            getCurrentAnalysisState().taintErrorSet.clear();
            isBlocked = true;
        }
        return isBlocked;
    }

    private void analyzeIterableLambdaInvocationArgExpression(BLangExpression argExpr) {
        BLangFunction function = ((BLangLambdaFunction) argExpr).function;
        function.accept(this);
        if (function.symbol.taintTable == null) {
            getCurrentAnalysisState().blockedNode = new BlockedNode(this.currPkgEnv, null);
            stopAnalysis = true;
            getCurrentAnalysisState().taintedStatus = TaintedStatus.UNTAINTED;
        } else if (getCurrentAnalysisState().taintedStatus == TaintedStatus.TAINTED) {
            int requiredParamCount = function.requiredParams.size();
            int defaultableParamCount = function.defaultableParams.size();
            int totalParamCount = requiredParamCount + defaultableParamCount + (function.restParam == null ? 0 : 1);
            Map<Integer, TaintRecord> taintTable = function.symbol.taintTable;
            for (int paramIndex = 0; paramIndex < totalParamCount; paramIndex++) {
                TaintRecord taintRecord = taintTable.get(paramIndex);
                BLangSimpleVariable param = getParam(function, paramIndex, requiredParamCount, defaultableParamCount);
                if (taintRecord == null) {
                    addTaintError(argExpr.pos, param.name.value,
                            DiagnosticCode.TAINTED_VALUE_PASSED_TO_SENSITIVE_PARAMETER);
                } else if (taintRecord.taintError != null && taintRecord.taintError.size() > 0) {
                    addTaintError(taintRecord.taintError);
                }
                if (stopAnalysis) {
                    break;
                }
            }
        }
    }

    private void addTaintError(DiagnosticPos diagnosticPos, String paramName, DiagnosticCode diagnosticCode) {
        TaintRecord.TaintError taintError = new TaintRecord.TaintError(diagnosticPos, paramName, diagnosticCode);
        getCurrentAnalysisState().taintErrorSet.add(taintError);
        if (!entryPointAnalysis) {
            stopAnalysis = true;
        }
    }

    private void addTaintError(List<TaintRecord.TaintError> taintErrors) {
        getCurrentAnalysisState().taintErrorSet.addAll(taintErrors);
        if (!entryPointAnalysis) {
            stopAnalysis = true;
        }
    }

    private void addToBlockedList(BLangInvocation invocationExpr) {
        BlockingNode blockingNode = new BlockingNode(invocationExpr.symbol.pkgID, invocationExpr.symbol.name);
        getCurrentAnalysisState().blockedNode = new BlockedNode(this.currPkgEnv, blockingNode);
        stopAnalysis = true;
        getCurrentAnalysisState().taintedStatus = TaintedStatus.UNTAINTED;
    }

    private void combinedParameterTaintedStatus(List<TaintedStatus> combinedArgTaintedStatus,
                                                List<TaintedStatus> argTaintedStatus) {
        if (combinedArgTaintedStatus.isEmpty()) {
            combinedArgTaintedStatus.addAll(argTaintedStatus);
        } else {
            // Merge lists together while making sure "Tainted" status of "argTaintedStatus" list is not overwritten
            // with "Untainted", whereas it is allowed to overwritten "Untainted" status with with "Tainted".
            for (int paramIndex = 0; paramIndex < argTaintedStatus.size(); paramIndex++) {
                if (argTaintedStatus.get(paramIndex) == TaintedStatus.TAINTED) {
                    combinedArgTaintedStatus.set(paramIndex, TaintedStatus.TAINTED);
                }
            }
        }
    }

    /**
     * Update the maps that hold the parameter tainted status, with the tainted statuses of the parameter for the
     * current invocation.
     */
    private void updateParameterTaintedStatuses() {
        updateParameterTaintedStatuses(getCurrentAnalysisState().requiredParams, 0);
        updateParameterTaintedStatuses(getCurrentAnalysisState().defaultableParams,
                getCurrentAnalysisState().requiredParams.size());
        if (getCurrentAnalysisState().restParam != null) {
            updateParameterTaintedStatuses(Collections.singletonList(getCurrentAnalysisState().restParam),
                    getCurrentAnalysisState().requiredParams.size() +
                            getCurrentAnalysisState().defaultableParams.size());
        }
    }

    /**
     * Update the map that hold the parameter tainted status, with the tainted statuses of the parameter for the
     * current invocation. Parameter tainted state is maintained in a single list (to persist into the compiled code).
     *
     * @param paramList list of parameters to be used to identify the tainted state of the parameter after invocation
     * @param startIndex the start position of the tainted status list corresponding to current paramList
     */
    private void updateParameterTaintedStatuses(List<BLangSimpleVariable> paramList, int startIndex) {
        if (getCurrentAnalysisState().parameterTaintedStatus.size() <= startIndex) {
            // If list do not have the tainted state of current parameter list already, add new entries to the list.
            paramList.forEach(param -> {
                TaintedStatus taintedStateBasedOnAnnotations = getTaintedStatusBasedOnAnnotations(param.annAttachments);
                if (taintedStateBasedOnAnnotations == TaintedStatus.IGNORED) {
                    // If annotations are not use, use the analyzed status.
                    getCurrentAnalysisState().parameterTaintedStatus.add(param.symbol.tainted ?
                            TaintedStatus.TAINTED : TaintedStatus.UNTAINTED);
                } else {
                    // If parameter has "tainted" or "untainted" annotation, argument should be updated according to the
                    // annotated status regardless of the analyzed status. This is useful if someone need to explicitly
                    // mark any argument that is set as a parameter "tainted" or "untainted".
                    getCurrentAnalysisState().parameterTaintedStatus.add(taintedStateBasedOnAnnotations);
                }
            });
        } else {
            // If list already contains tainted state of current parameter list, update the existing list. This can
            // happen, when there are multiple returns within the same function.
            for (int paramIndex = 0; paramIndex < paramList.size(); paramIndex++) {
                BLangVariable param = paramList.get(paramIndex);
                TaintedStatus taintedStateBasedOnAnnotations = getTaintedStatusBasedOnAnnotations(param.annAttachments);
                if (taintedStateBasedOnAnnotations == TaintedStatus.TAINTED || param.symbol.tainted) {
                    getCurrentAnalysisState().parameterTaintedStatus.set(startIndex + paramIndex,
                            TaintedStatus.TAINTED);
                }
                // Ignore if param is untainted. Where there are multiple return statements in a function, it is
                // required to get the combined tainted status of parameters. This condition is skipped to make sure we
                // do not change tainted status back to untainted.
            }
        }
    }

    // Private methods relevant to invocation analysis.

    /**
     * Analyze an invocation with the intention of determine if the arguments passed to the invocation results in a
     * taint-error. Delegates the analysis of each individual argument to analyzeInvocationArgument. After analysis of
     * all arguments, update the combined tainted status of the return value.
     *
     * @param invocationExpr invocation expression
     */
    private void analyzeInvocation(BLangInvocation invocationExpr) {
        BInvokableSymbol invokableSymbol = (BInvokableSymbol) invocationExpr.symbol;
        Map<Integer, TaintRecord> taintTable = invokableSymbol.taintTable;
        TaintedStatus returnTaintedStatus = TaintedStatus.UNTAINTED;
        List<TaintedStatus> argTaintedStatusList = new ArrayList<>();

        // Get tainted status when all parameters are untainted
        TaintRecord allParamsUntaintedRecord = taintTable.get(ALL_UNTAINTED_TABLE_ENTRY_INDEX);
        if (allParamsUntaintedRecord != null) {
            if (allParamsUntaintedRecord.taintError != null && allParamsUntaintedRecord.taintError.size() > 0) {
                // This can occur when there is a error regardless of tainted status of parameters.
                // Example: Tainted value returned by function is passed to another functions's sensitive parameter.
                addTaintError(allParamsUntaintedRecord.taintError);
            } else {
                returnTaintedStatus = allParamsUntaintedRecord.returnTaintedStatus;
                if (allParamsUntaintedRecord.parameterTaintedStatusList != null) {
                    argTaintedStatusList = new ArrayList<>(allParamsUntaintedRecord.parameterTaintedStatusList);
                }
            }
        }
        // Check tainted status of each argument. If argument is tainted, get the taint table when the given parameter
        // is tainted and use it to update "allParamsUntaintedRecord". Do same for all parameters to identify the
        // complete taint outcome of the current function.
        if (invocationExpr.argExprs != null) {
            int requiredParamCount = invokableSymbol.params.size();
            int defaultableParamCount = invokableSymbol.defaultableParams.size();

            int requiredArgsCount = invocationExpr.requiredArgs.size();
            int namedArgsCount = invocationExpr.namedArgs.size();
            int restArgsCount = invocationExpr.restArgs.size();

            for (int argIndex = 0; argIndex < requiredArgsCount; argIndex++) {
                BLangExpression argExpr = invocationExpr.requiredArgs.get(argIndex);
                TaintedStatus argumentAnalysisResult = analyzeInvocationArgument(argIndex, invocationExpr, argExpr,
                        argTaintedStatusList);
                if (argumentAnalysisResult == TaintedStatus.IGNORED) {
                    return;
                } else if (argumentAnalysisResult == TaintedStatus.TAINTED) {
                    returnTaintedStatus = TaintedStatus.TAINTED;
                }
                if (stopAnalysis) {
                    break;
                }
            }
            for (int argIndex = 0; argIndex < namedArgsCount; argIndex++) {
                BLangExpression argExpr = invocationExpr.namedArgs.get(argIndex);
                if (argExpr.getKind() == NodeKind.NAMED_ARGS_EXPR) {
                    String currentNamedArgExprName = ((BLangNamedArgsExpression) argExpr).name.value;
                    // Pick the index of this defaultable parameter in the invokable definition.
                    int paramIndex = 0;
                    for (int defaultableParamIndex = 0; defaultableParamIndex < defaultableParamCount;
                         defaultableParamIndex++) {
                        BVarSymbol defaultableParam = invokableSymbol.defaultableParams.get(defaultableParamIndex);
                        if (defaultableParam.name.value.equals(currentNamedArgExprName)) {
                            paramIndex = requiredParamCount + defaultableParamIndex;
                            break;
                        }
                    }
                    TaintedStatus argumentAnalysisResult = analyzeInvocationArgument(paramIndex, invocationExpr,
                            argExpr, argTaintedStatusList);
                    if (argumentAnalysisResult == TaintedStatus.IGNORED) {
                        return;
                    } else if (argumentAnalysisResult == TaintedStatus.TAINTED) {
                        returnTaintedStatus = TaintedStatus.TAINTED;
                    }
                    if (stopAnalysis) {
                        break;
                    }
                }
            }
            for (int argIndex = 0; argIndex < restArgsCount; argIndex++) {
                BLangExpression argExpr = invocationExpr.restArgs.get(argIndex);
                // Pick the index of the rest parameter in the invokable definition.
                int paramIndex = requiredParamCount + defaultableParamCount;
                TaintedStatus argumentAnalysisResult = analyzeInvocationArgument(paramIndex, invocationExpr, argExpr,
                        argTaintedStatusList);
                if (argumentAnalysisResult == TaintedStatus.IGNORED) {
                    return;
                } else if (argumentAnalysisResult == TaintedStatus.TAINTED) {
                    returnTaintedStatus = TaintedStatus.TAINTED;
                }
                if (stopAnalysis) {
                    break;
                }
            }
            updateArgTaintedStatus(invocationExpr, argTaintedStatusList);

        }
        // When an invocation like stringValue.trim() happens, if stringValue is tainted, the result should also be
        // tainted.
        if (!invocationExpr.builtinMethodInvocation && invocationExpr.expr != null) {
            //TODO: TaintedIf annotation, so that it's possible to define what can taint or untaint the return.
            invocationExpr.expr.accept(this);
            if (getCurrentAnalysisState().taintedStatus == TaintedStatus.IGNORED) {
                return;
            } else if (getCurrentAnalysisState().taintedStatus == TaintedStatus.TAINTED) {
                returnTaintedStatus = TaintedStatus.TAINTED;
            }
        }
        getCurrentAnalysisState().taintedStatus = returnTaintedStatus;
    }

    private void updateArgTaintedStatus(BLangInvocation invocationExpr, List<TaintedStatus> argTaintedStatusList) {
        BInvokableSymbol invokableSymbol = (BInvokableSymbol) invocationExpr.symbol;
        int requiredParamCount = invokableSymbol.params.size();
        int defaultableParamCount = invokableSymbol.defaultableParams.size();

        int requiredArgsCount = invocationExpr.requiredArgs.size();
        int namedArgsCount = invocationExpr.namedArgs.size();
        int restArgsCount = invocationExpr.restArgs.size();

        for (int argIndex = 0; argIndex < requiredArgsCount; argIndex++) {
            BLangExpression argExpr = invocationExpr.requiredArgs.get(argIndex);
            TaintedStatus argTaintedStatus = TaintedStatus.UNTAINTED;
            if (!argTaintedStatusList.isEmpty()) {
                argTaintedStatus = argTaintedStatusList.get(argIndex);
            }
            updateArgTaintedStatus(argExpr, argTaintedStatus);
        }

        for (int argIndex = 0; argIndex < namedArgsCount; argIndex++) {
            BLangExpression argExpr = invocationExpr.namedArgs.get(argIndex);
            if (argExpr.getKind() == NodeKind.NAMED_ARGS_EXPR) {
                String currentNamedArgExprName = ((BLangNamedArgsExpression) argExpr).name.value;
                // Pick the index of this defaultable parameter in the invokable definition.
                int paramIndex = 0;
                for (int defaultableParamIndex = 0; defaultableParamIndex < defaultableParamCount;
                     defaultableParamIndex++) {
                    BVarSymbol defaultableParam = invokableSymbol.defaultableParams.get(defaultableParamIndex);
                    if (defaultableParam.name.value.equals(currentNamedArgExprName)) {
                        paramIndex = requiredParamCount + defaultableParamIndex;
                        break;
                    }
                }
                TaintedStatus argTaintedStatus = argTaintedStatusList.get(paramIndex);
                updateArgTaintedStatus(argExpr, argTaintedStatus);
            }
        }

        for (int argIndex = 0; argIndex < restArgsCount; argIndex++) {
            BLangExpression argExpr = invocationExpr.restArgs.get(argIndex);
            // Pick the index of the rest parameter in the invokable definition.
            int paramIndex = requiredParamCount + defaultableParamCount;
            TaintedStatus argTaintedStatus = argTaintedStatusList.get(paramIndex);
            updateArgTaintedStatus(argExpr, argTaintedStatus);
        }
    }

    /**
     * Update the tainted state of the given argument expression of a function invocation. This will make sure tainted
     * state changes made within the invoked function is reflected back on the arguments.
     * <p>
     * XML access expressions do not have a variable symbol attached. Therefore, such simple variable references are not
     * updated. Since such expressions only result in simple values, this does not affect the accuracy of the analyzer.
     *
     * @param varRefExpr argument expressions
     * @param varTaintedStatus tainted status of the argument
     */
    private void updateArgTaintedStatus(BLangExpression varRefExpr, TaintedStatus varTaintedStatus) {
        if (varRefExpr.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR
                || varRefExpr.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR
                || varRefExpr.getKind() == NodeKind.GROUP_EXPR
                || varRefExpr.getKind() == NodeKind.LIST_CONSTRUCTOR_EXPR
                || (varRefExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF
                && ((BLangSimpleVarRef) varRefExpr).pkgSymbol.tag != SymTag.XMLNS)) {
            visitAssignment(varRefExpr, varTaintedStatus, varRefExpr.pos);
        }
    }

    /**
     * Analyze one invocation argument, determine if the argument expression is tainted, if so consult the taint table
     * of the invokable to check if a taint-error is present.
     *
     * @param paramIndex index of the parameter for the current argument
     * @param invocationExpr invocation expression relevant to the invocation
     * @param argExpr argument expression being analyzed
     * @param argTaintedStatusList the combined argument tainted status list
     * @return tainted status of the argument expression
     */
    private TaintedStatus analyzeInvocationArgument(int paramIndex, BLangInvocation invocationExpr,
                                                    BLangExpression argExpr, List<TaintedStatus> argTaintedStatusList) {
        argExpr.accept(this);
        // If current argument is tainted, look-up the taint-table for the record of return-tainted-status when the
        // given argument is in tainted state.
        if (getCurrentAnalysisState().taintedStatus == TaintedStatus.TAINTED) {
            BInvokableSymbol invokableSymbol = (BInvokableSymbol) invocationExpr.symbol;
            TaintRecord taintRecord = invokableSymbol.taintTable.get(paramIndex);
            int requiredParamCount = invokableSymbol.params.size();
            int defaultableParamCount = invokableSymbol.defaultableParams.size();
            BVarSymbol paramSymbol = getParamSymbol(invokableSymbol, paramIndex, requiredParamCount,
                    defaultableParamCount);

            if (taintRecord == null) {
                // This is when current parameter is "sensitive". Therefore, providing a tainted value to a sensitive
                // parameter is invalid and should return a compiler error.
                DiagnosticPos argPos = argExpr.pos != null ? argExpr.pos : invocationExpr.pos;
                addTaintError(argPos, paramSymbol.name.value,
                        DiagnosticCode.TAINTED_VALUE_PASSED_TO_SENSITIVE_PARAMETER);
            } else if (taintRecord.taintError != null && taintRecord.taintError.size() > 0) {
                // This is when current parameter is derived to be sensitive.
                taintRecord.taintError.forEach(error -> {
                    if (error.diagnosticCode == DiagnosticCode.TAINTED_VALUE_PASSED_TO_GLOBAL_VARIABLE) {
                        addTaintError(taintRecord.taintError);
                    } else {
                        DiagnosticPos argPos = argExpr.pos != null ? argExpr.pos : invocationExpr.pos;
                        addTaintError(argPos, paramSymbol.name.value,
                                DiagnosticCode.TAINTED_VALUE_PASSED_TO_SENSITIVE_PARAMETER);
                    }
                });
            } else {
                combinedParameterTaintedStatus(argTaintedStatusList, taintRecord.parameterTaintedStatusList);
                return taintRecord.returnTaintedStatus;
            }
        } else if (getCurrentAnalysisState().taintedStatus == TaintedStatus.IGNORED) {
            // If argument is a recursive call or a function loop ignore the invocation and proceed.
            return TaintedStatus.IGNORED;
        }
        return TaintedStatus.UNTAINTED;
    }

    private void resolveBlockedInvokable(List<BlockedNode> blockedNodeList) {
        List<BlockedNode> remainingBlockedNodeList = new ArrayList<>();
        // Revisit blocked nodes and attempt to generate the table.
        for (BlockedNode blockedNode : blockedNodeList) {
            this.env = blockedNode.pkgSymbol;
            blockedNode.invokableNode.accept(this);
            if (blockedNode.invokableNode.symbol.taintTable == null) {
                remainingBlockedNodeList.add(blockedNode);
            }
        }
        // If list is not moving, there is a recursion. Derive the tainted status of all the blocked functions by using
        // annotations and if annotations are not present generate error.
        if (remainingBlockedNodeList.size() != 0 && blockedNodeList.size() == remainingBlockedNodeList.size()) {
            for (BlockedNode blockedNode : remainingBlockedNodeList) {
                analyzerPhase = AnalyzerPhase.LOOP_ANALYSIS;
                this.env = blockedNode.pkgSymbol;
                blockedNode.invokableNode.accept(this);
            }
            analyzerPhase = AnalyzerPhase.LOOPS_RESOLVED_ANALYSIS;
            resolveBlockedInvokable(remainingBlockedNodeList);
        } else if (remainingBlockedNodeList.size() > 0) {
            resolveBlockedInvokable(remainingBlockedNodeList);
        }
        // If remainingBlockedNodeList is empty, end the recursion.
    }

    private BLangSimpleVariable getParam(BLangInvokableNode invNode, int paramIndex, int requiredParamCount,
                                         int defaultableParamCount) {
        BLangSimpleVariable param;
        if (paramIndex < requiredParamCount) {
            param = invNode.requiredParams.get(paramIndex);
        } else if (paramIndex < requiredParamCount + defaultableParamCount) {
            param = invNode.defaultableParams.get(paramIndex - requiredParamCount).var;
        } else {
            param = invNode.restParam;
        }
        return param;
    }

    private BVarSymbol getParamSymbol(BInvokableSymbol invSymbol, int paramIndex, int requiredParamCount,
                                      int defaultableParamCount) {
        BVarSymbol param;
        if (paramIndex < requiredParamCount) {
            param = invSymbol.params.get(paramIndex);
        } else if (paramIndex < requiredParamCount + defaultableParamCount) {
            param = invSymbol.defaultableParams.get(paramIndex - requiredParamCount);
        } else {
            param = invSymbol.restParam;
        }
        return param;
    }

    private AnalysisState getCurrentAnalysisState() {
        return analysisStateStack.peek();
    }

    private class BlockingNode {

        PackageID packageID;
        Name name;

        BlockingNode(PackageID packageID, Name name) {
            this.packageID = packageID;
            this.name = name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            BlockingNode that = (BlockingNode) o;

            if (!packageID.equals(that.packageID)) {
                return false;
            }
            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            int result = packageID.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }
    }

    private class BlockedNode {

        SymbolEnv pkgSymbol;
        BLangInvokableNode invokableNode;
        BlockingNode blockingNode;

        BlockedNode(SymbolEnv pkgSymbol, BlockingNode blockingNode) {
            this.pkgSymbol = pkgSymbol;
            this.blockingNode = blockingNode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            BlockedNode that = (BlockedNode) o;

            return invokableNode.symbol.pkgID.equals(that.invokableNode.symbol.pkgID) && invokableNode.symbol.name
                    .equals(that.invokableNode.symbol.name);
        }

        @Override
        public int hashCode() {
            int result = invokableNode.symbol.pkgID.hashCode();
            result = 31 * result + invokableNode.symbol.name.hashCode();
            return result;
        }
    }

    // Used to store the analysis state of each function being visited.
    //
    // Lambda functions and worker lambda functions should be analyzed as part of the enclosing function. This is
    // because such functions will have direct access to variables defined in the enclosing function. Therefore, the
    // tainted state of such function depends on the taint conditions of the enclosing function. Hence, there are
    // situations where `TaintAnalyzer` will visit a `BLangFunction` within another `BLangFunction`. To support such
    // scenarios analyzer state is maintained in a stack.
    private class AnalysisState {
        private TaintedStatus taintedStatus;
        private TaintedStatus returnTaintedStatus;

        // Used to analyze the tainted status of parameters when returning.
        private List<BLangSimpleVariable> requiredParams;
        private List<BLangSimpleVariable> defaultableParams;
        private BLangSimpleVariable restParam;
        private List<TaintedStatus> parameterTaintedStatus;

        private BlockedNode blockedNode;

        private Set<TaintRecord.TaintError> taintErrorSet = new LinkedHashSet<>();
    }
}

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
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
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
import org.wso2.ballerinalang.compiler.semantics.model.symbols.TaintRecord.TaintedStatus;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
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
import org.wso2.ballerinalang.compiler.tree.clauses.BLangInputClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangMatchClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnFailClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAccessExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCommitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRawTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef.BLangRecordVarRefKeyValue;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangIntersectionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerUtils;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;

/**
 * Generate taint-table for each invokable node.
 * <p>
 * Taint-table will contain the tainted status of return values, depending on the tainted status of parameters.
 * <p>
 * Propagate tainted status of variables across the program.
 * <p>
 * Evaluate invocations and generate errors if:
 * (*) Tainted value has been passed to a untainted parameter.
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
    private Types types;

    private boolean overridingAnalysis = true;
    private boolean entryPointPreAnalysis;
    private boolean entryPointAnalysis;
    private boolean stopAnalysis;
    private boolean attachedFunctionAnalysis;

    private List<BlockedNode> blockedNodeList;
    private List<BlockedNode> blockedEntryPointNodeList;
    private List<BInvokableSymbol> ignoredInvokableSymbol;

    private Stack<AnalysisState> analysisStateStack;
    private Set<TaintRecord.TaintError> dlogSet;
    private BLangFunction currTopLevelFunction;
    private boolean topLevelFunctionAllParamsUntaintedAnalysis;

    private static final String ANNOTATION_TAINTED = "tainted";
    public static final String ANNOTATION_UNTAINTED = "untainted";

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
        types = Types.getInstance(context);
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

        Map<Boolean, List<TopLevelNode>> topLevelNodeGroups = pkgNode.topLevelNodes.stream()
                .collect(Collectors.partitioningBy(node -> node.getKind() == NodeKind.VARIABLE
                    || node.getKind() == NodeKind.CONSTANT));

        // Analyze top level variable nodes.
        analyzeNode(topLevelNodeGroups.get(true));
        // Analyze top level nodes other than variable nodes.
        analyzeNode(topLevelNodeGroups.get(false));

        analyzerPhase = AnalyzerPhase.BLOCKED_NODE_ANALYSIS;
        resolveBlockedInvokable(blockedNodeList);
        resolveBlockedInvokable(blockedEntryPointNodeList);

        // Go through module level variable after all functions are resolved.
        analyzeModuleLevelVariableDefinitions(topLevelNodeGroups.get(true));

        if (dlogSet.size() > 0) {
            dlogSet.forEach(dlogEntry -> dlog.error(dlogEntry.pos, dlogEntry.diagnosticCode,
                    dlogEntry.paramName.toArray()));
        }
        currPkgEnv = prevPkgEnv;
        pkgNode.completedPhases.add(CompilerPhase.TAINT_ANALYZE);
    }

    private void analyzeModuleLevelVariableDefinitions(List<TopLevelNode> topLevelNodes) {
        // Only analyze variable definitions
        topLevelNodes.stream()
                .filter(node -> node.getKind() == NodeKind.VARIABLE || node.getKind() == NodeKind.VARIABLE_DEF)
                .forEach(topLevelNode -> {
                    AnalysisState analysisState = new AnalysisState();
                    analysisStateStack.push(analysisState);
                    ((BLangNode) topLevelNode).accept(this);
                    analysisStateStack.pop();
                });
    }

    private void analyzeNode(List<TopLevelNode> topLevelNodes) {
        // Skip all lambda functions and analyzing them as part of the enclosing function.
        topLevelNodes.stream().filter(node -> node.getKind() != NodeKind.FUNCTION
                || !((BLangFunction) node).flagSet.contains(Flag.LAMBDA))
                .forEach(node -> {
                    AnalysisState analysisState = new AnalysisState();
                    analysisStateStack.push(analysisState);
                    ((BLangNode) node).accept(this);
                    analysisStateStack.pop();
                });
    }

    private void restCurrentTaintPropagationSet() {
        for (Map.Entry<BSymbol, Boolean> taintStatus : getCurrentAnalysisState().prevSymbolTaintStatus.entrySet()) {
            // Reset previous taint status.
            taintStatus.getKey().tainted = taintStatus.getValue();
        }
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
    public void visit(BLangClassDefinition classDefinition) {
        BSymbol objectSymbol = classDefinition.symbol;
        SymbolEnv classDefEnv = SymbolEnv.createPkgLevelSymbolEnv(classDefinition, objectSymbol.scope, env);
        classDefinition.fields.forEach(field -> analyzeNode(field, classDefEnv));
        if (classDefinition.initFunction != null) {
            analyzeNode(classDefinition.initFunction, classDefEnv);
        }
        classDefinition.functions.forEach(f -> analyzeNode(f, classDefEnv));
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

        // If this is a attached function and we are not in attachedFunctionAnalysis mode,
        // go into attachedFunctionAnalysis mode and rerun in attached func mode.
        if (!attachedFunctionAnalysis && funcNode.flagSet.contains(Flag.ATTACHED)) {
            analysisStateStack.pop();
            visitAttachedInvokable(funcNode);
            return;
        }

        analysisState.requiredParams = funcNode.requiredParams;
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
    public void visit(BLangBlockFunctionBody body) {
        SymbolEnv bodyEnv = SymbolEnv.createFuncBodyEnv(body, env);
        for (BLangStatement stmt : body.stmts) {
            if (stopAnalysis) {
                break;
            } else {
                analyzeNode(stmt, bodyEnv);
            }
        }
    }

    @Override
    public void visit(BLangExprFunctionBody body) {
        SymbolEnv bodyEnv = SymbolEnv.createFuncBodyEnv(body, env);
        analyzeNode(body.expr, bodyEnv);

        if (getCurrentAnalysisState().taintedStatus == TaintedStatus.TAINTED) {
            getCurrentAnalysisState().returnTaintedStatus = TaintedStatus.TAINTED;
        }

        getCurrentAnalysisState().taintedStatus = getCurrentAnalysisState().returnTaintedStatus;
        updateParameterTaintedStatuses();
    }

    @Override
    public void visit(BLangExternalFunctionBody body) {
        // do nothing
    }

    @Override
    public void visit(BLangService serviceNode) {
        // Any service that is not created via service definition is considered tainted regardless of taintedness
        // of listeners.
        if (serviceNode.isAnonymousServiceValue) {
            setTaintedStatus(serviceNode.symbol, TaintedStatus.TAINTED);
            setTaintedStatus(serviceNode.serviceClass.symbol, TaintedStatus.TAINTED);
        }
    }

    @Override
    public void visit(BLangResource resourceNode) {
        /* ignored */
    }

    @Override
    public void visit(BLangLetExpression letExpression) {
        for (BLangLetVariable letVarDeclaration : letExpression.letVarDeclarations) {
            analyzeNode((BLangNode) letVarDeclaration.definitionNode, letExpression.env);
        }
        analyzeNode(letExpression.expr, letExpression.env);
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
            // blocker found while analyzing module level variable, reset stopAnalysis flag
            if (stopAnalysis && isModuleVariable(varNode.symbol)) {
                stopAnalysis = false;
            }

            if (isModuleVariable(varNode.symbol)
                    && getCurrentAnalysisState().taintedStatus == TaintedStatus.TAINTED) {
                dlogSet.add(new TaintRecord.TaintError(varNode.pos, varNode.name.value,
                        DiagnosticCode.TAINTED_VALUE_PASSED_TO_GLOBAL_VARIABLE));
            }
            setTaintedStatus(varNode, getCurrentAnalysisState().taintedStatus);
        }

        // Listener can be marked as @untainted to indicate it's listening to a safe endpoint,
        // When not annotated @untainted, it defaults to tainted.
        if (varNode.flagSet.contains(Flag.LISTENER)) {
            if (hasAnnotation(varNode, ANNOTATION_UNTAINTED)) {
                setTaintedStatus(varNode, TaintedStatus.UNTAINTED);
            } else {
                setTaintedStatus(varNode, TaintedStatus.TAINTED);
            }
        }

        if (isModuleVariable(varNode.symbol)
                || (varNode.symbol.owner.type != null && varNode.symbol.owner.type.tag == TypeTags.SERVICE)) {
            if (hasAnnotation(varNode, ANNOTATION_TAINTED)) {
                ((BVarSymbol) varNode.symbol).taintabilityAllowance = BVarSymbol.TaintabilityAllowance.TAINTED;
                setTaintedStatus(varNode.symbol, TaintedStatus.TAINTED);
            } else if (hasAnnotation(varNode, ANNOTATION_UNTAINTED)) {
                ((BVarSymbol) varNode.symbol).taintabilityAllowance = BVarSymbol.TaintabilityAllowance.UNTAINTED;
            }
        }
    }

    private boolean isModuleVariable(BSymbol symbol) {
        return symbol.tag == SymTag.VARIABLE && symbol.owner.getKind() == SymbolKind.PACKAGE;
    }

    @Override
    public void visit(BLangWorker workerNode) {
        /* ignore, remove later */
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
        if (varRefExpr == null) {
            return;
        }
        if (varTaintedStatus != TaintedStatus.IGNORED) {
            // Generate error if a global variable has been assigned with a tainted value.
            if (varTaintedStatus == TaintedStatus.TAINTED && varRefExpr instanceof BLangVariableReference) {
                BLangVariableReference varRef = (BLangVariableReference) varRefExpr;
                if (isMutableVariable(varRef) && isGlobalVarOrServiceVar(varRef) && !isMarkedTainted(varRef)) {
                    if (varRef.symbol != null && varRef.symbol.type.tag == TypeTags.OBJECT) {
                        addTaintError(pos, getVariableName(varRef),
                                DiagnosticCode.TAINTED_VALUE_PASSED_TO_MODULE_OBJECT);
                    } else {
                        addTaintError(pos, getVariableName(varRef),
                                DiagnosticCode.TAINTED_VALUE_PASSED_TO_GLOBAL_VARIABLE);
                    }
                    return;
                } else if (varRef.symbol != null && varRef.symbol.closure
                        && !varRef.symbol.tainted && notInSameScope(varRef, env)) {
                    addTaintError(pos, getVariableName(varRef),
                            DiagnosticCode.TAINTED_VALUE_PASSED_TO_CLOSURE_VARIABLE);
                    return;
                } else if (varRef.symbol != null && isMarkedUntainted(varRef)
                        && (varRef.symbol.flags & Flags.FUNCTION_FINAL) == Flags.FUNCTION_FINAL) {
                    addTaintError(pos, getVariableName(varRef),
                            DiagnosticCode.TAINTED_VALUE_PASSED_TO_UNTAINTED_PARAMETER);
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

    private String getVariableName(BLangVariableReference varRef) {
        if (isStructuredAccessOnVariableReference(varRef)) {
            return getVariableName((BLangVariableReference) ((BLangAccessExpression) varRef).expr);
        }
        return varRef.symbol.name.value;
    }

    private boolean isMutableVariable(BLangVariableReference varRef) {
        if (isStructuredAccessOnVariableReference(varRef)) {
            return isMutableVariable((BLangVariableReference) ((BLangAccessExpression) varRef).expr);
        }
        if (varRef.symbol.getKind() == SymbolKind.CONSTANT || (varRef.symbol.flags & Flags.FINAL) == Flags.FINAL) {
            return false;
        }
        return true;
    }

    private boolean notInSameScope(BLangVariableReference varRefExpr, SymbolEnv env) {
        return !varRefExpr.symbol.owner.equals(env.scope.owner);
    }

    private boolean isMarkedTainted(BLangVariableReference varRef) {
        if (isStructuredAccessOnVariableReference(varRef)) {
            return isMarkedTainted((BLangVariableReference) ((BLangAccessExpression) varRef).expr);
        }
        return ((BVarSymbol) varRef.symbol).taintabilityAllowance == BVarSymbol.TaintabilityAllowance.TAINTED;
    }

    private boolean isStructuredAccessOnVariableReference(BLangVariableReference variableReference) {
        return (variableReference.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR
                || variableReference.getKind() == NodeKind.FIELD_BASED_ACCESS_EXPR
                || variableReference.getKind() == NodeKind.XML_ATTRIBUTE_ACCESS_EXPR);
    }

    private boolean isMarkedUntainted(BLangVariableReference varRef) {
        if (isStructuredAccessOnVariableReference(varRef)) {
            return isMarkedUntainted((BLangVariableReference) ((BLangAccessExpression) varRef).expr);
        }
        return ((BVarSymbol) varRef.symbol).taintabilityAllowance == BVarSymbol.TaintabilityAllowance.UNTAINTED;
    }

    private boolean isGlobalVarOrServiceVar(BLangVariableReference varRef) {
        if (isStructuredAccessOnVariableReference(varRef)) {
            return isGlobalVarOrServiceVar((BLangVariableReference) ((BLangAccessExpression) varRef).expr);
        }
        return varRef.symbol != null && varRef.symbol.owner != null
                && (isModuleVariable(varRef.symbol)
                || (varRef.symbol.owner.flags & Flags.SERVICE) == Flags.SERVICE);
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
    public void visit(BLangRetry retryNode) {
        retryNode.retryBody.accept(this);

        if (retryNode.onFailClause != null) {
            analyzeNode(retryNode.onFailClause, env);
        }
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction) {
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
        ifNode.expr.accept(this);
        // return tainted status of if conditional expr does not affect overal taintedness.
        getCurrentAnalysisState().taintedStatus = TaintedStatus.UNTAINTED;

        ifNode.body.accept(this);
        if (ifNode.elseStmt != null) {
            ifNode.elseStmt.accept(this);
        }
        overridingAnalysis = true;
    }

    @Override
    public void visit(BLangMatchStatement matchStatement) {
        matchStatement.expr.accept(this);
        TaintedStatus observedTaintedStatusOfMatchExpr = getCurrentAnalysisState().taintedStatus;

        for (BLangMatchClause matchClause : matchStatement.matchClauses) {
            getCurrentAnalysisState().taintedStatus = observedTaintedStatusOfMatchExpr;
            matchClause.accept(this);
        }

        if (matchStatement.onFailClause != null) {
            analyzeNode(matchStatement.onFailClause, env);
        }
    }

    @Override
    public void visit(BLangMatchClause matchClause) {
        matchClause.blockStmt.accept(this);
    }

    @Override
    public void visit(BLangMatch matchStmt) {
        matchStmt.expr.accept(this);
        TaintedStatus observedTaintedStatusOfMatchExpr = getCurrentAnalysisState().taintedStatus;
        matchStmt.patternClauses.forEach(clause -> {
            getCurrentAnalysisState().taintedStatus = observedTaintedStatusOfMatchExpr;
            clause.accept(this);
        });

        if (matchStmt.onFailClause != null) {
            analyzeNode(matchStmt.onFailClause, env);
        }
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

        if (foreach.onFailClause != null) {
            analyzeNode(foreach.onFailClause, env);
        }
    }

    public void visit(BLangOnFailClause onFailClause) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(onFailClause.body, env);
        analyzeNode(onFailClause.body, blockEnv);
    }

    @Override
    public void visit(BLangQueryAction queryAction) {
        /* ignore */
    }

    @Override
    public void visit(BLangWhile whileNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(whileNode.body, env);
        analyzeNode(whileNode.body, blockEnv);

        if (whileNode.onFailClause != null) {
            analyzeNode(whileNode.onFailClause, env);
        }
    }

    @Override
    public void visit(BLangDo doNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(doNode.body, env);
        analyzeNode(doNode.body, blockEnv);

        if (doNode.onFailClause != null) {
            analyzeNode(doNode.onFailClause, env);
        }
    }

    @Override
    public void visit(BLangFail failNode) {
        failNode.expr.accept(this);
        if (getCurrentAnalysisState().taintedStatus == TaintedStatus.TAINTED) {
            getCurrentAnalysisState().returnTaintedStatus = TaintedStatus.TAINTED;
        }
    }

    @Override
    public void visit(BLangLock lockNode) {
        lockNode.body.accept(this);

        if (lockNode.onFailClause != null) {
            analyzeNode(lockNode.onFailClause, env);
        }
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        transactionNode.transactionBody.accept(this);
        if (transactionNode.onFailClause != null) {
            analyzeNode(transactionNode.onFailClause, env);
        }

        overridingAnalysis = false;
        overridingAnalysis = true;
    }

    @Override
    public void visit(BLangRollback rollbackNode) {
        if (rollbackNode.expr != null) {
            rollbackNode.expr.accept(this);
        }
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
    public void visit(BLangWorkerSend workerSendNode) {
        workerSendNode.expr.accept(this);
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        syncSendExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
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
    public void visit(BLangTableConstructorExpr tableConstructorExpr) {
        if (tableConstructorExpr.recordLiteralList.size() == 0) {
            // Empty arrays are untainted.
            getCurrentAnalysisState().taintedStatus = TaintedStatus.UNTAINTED;
        } else {
            TaintedStatus isTainted = TaintedStatus.UNTAINTED;
            for (BLangExpression expression : tableConstructorExpr.recordLiteralList) {
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
        for (RecordLiteralNode.RecordField field : recordLiteral.fields) {

            if (field.isKeyValueField()) {
                BLangRecordLiteral.BLangRecordKeyValueField keyValuePair =
                        (BLangRecordLiteral.BLangRecordKeyValueField) field;
                if (keyValuePair.key.computedKey) {
                    keyValuePair.key.expr.accept(this);
                    if (getCurrentAnalysisState().taintedStatus == TaintedStatus.TAINTED) {
                        isTainted = TaintedStatus.TAINTED;
                    }
                }

                keyValuePair.valueExpr.accept(this);
            } else if (field.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                ((BLangRecordLiteral.BLangRecordVarNameField) field).accept(this);
            } else {
                ((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr.accept(this);
            }

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
            case TypeTags.RECORD:
            case TypeTags.MAP:
            case TypeTags.JSON:
            case TypeTags.XML:
                fieldAccessExpr.expr.accept(this);
        }
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        indexAccessExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangTableMultiKeyExpr tableMultiKeyExpr) {
        analyzeExprList(tableMultiKeyExpr.multiKeyIndexExprs);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        // handle error constructor invocation
        if (isErrorConstructorInvocation(invocationExpr) || isExternalLangLibFunction(invocationExpr)) {
            ((BInvokableSymbol) invocationExpr.symbol).taintTable = createIdentityTaintTable(invocationExpr);
        }

        if (invocationExpr.symbol != null) {
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

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocation) {
        this.visit((BLangInvocation) actionInvocation);
    }

    private boolean isErrorConstructorInvocation(BLangInvocation invocationExpr) {
        return invocationExpr.symbol != null && invocationExpr.symbol.kind == SymbolKind.ERROR_CONSTRUCTOR;
    }

    private void analyzeLangLibFunctionInvocation(BLangInvocation invocationExpr) {
        for (BLangExpression requiredArg : invocationExpr.requiredArgs) {
            requiredArg.accept(this);
            if (getCurrentAnalysisState().taintedStatus == TaintedStatus.TAINTED) {
                return;
            }
        }
        for (BLangExpression expression : invocationExpr.restArgs) {
            expression.accept(this);
            if (getCurrentAnalysisState().taintedStatus == TaintedStatus.TAINTED) {
                return;
            }
        }
        getCurrentAnalysisState().taintedStatus = TaintedStatus.UNTAINTED;
    }

    private boolean isExternalLangLibFunction(BLangInvocation invocationExpr) {
        return invocationExpr.symbol.pkgID.orgName.value.equals("ballerina")
                && invocationExpr.symbol.pkgID.name.value.startsWith("lang.")
                && Symbols.isNative(invocationExpr.symbol);
    }

    private int receiverIfAttachedFunction(BLangInvocation invocationExpr) {
        return isTaintAnalyzableAttachedFunction(invocationExpr) ? 1 : 0;
    }

    private Map<Integer, TaintRecord> createIdentityTaintTable(BLangInvocation invocationExpr) {
        Map<Integer, TaintRecord> taintTable = new HashMap<>();

        int requiredParamCount = invocationExpr.requiredArgs.size();
        int restCount = invocationExpr.restArgs == null || invocationExpr.restArgs.isEmpty() ? 0 : 1;
        int totalParamCount = requiredParamCount + restCount + receiverIfAttachedFunction(invocationExpr);

        for (int i = ALL_UNTAINTED_TABLE_ENTRY_INDEX; i < totalParamCount; i++) {
            TaintRecord record = new TaintRecord(
                    i == ALL_UNTAINTED_TABLE_ENTRY_INDEX ? TaintedStatus.IGNORED : TaintedStatus.TAINTED,
                    new ArrayList<>());
            taintTable.put(i, record);

            for (int j = 0; j < totalParamCount; j++) {
                record.parameterTaintedStatusList.add(TaintedStatus.IGNORED);
            }
        }

        return taintTable;
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
        if (typeInit.type.tag != TypeTags.STREAM &&
                (typeInit.type.tag != TypeTags.OBJECT ||
                         ((BObjectTypeSymbol) typeInit.type.tsymbol).initializerFunc != null)) {
            typeInit.initInvocation.accept(this);
        }

        getCurrentAnalysisState().taintedStatus = typeTaintedStatus;
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
    public void visit(BLangXMLElementAccess xmlElementAccess) {
        xmlElementAccess.expr.accept(this);
    }

    @Override
    public void visit(BLangXMLNavigationAccess xmlNavigation) {
        xmlNavigation.expr.accept(this);

        if (xmlNavigation.childIndex != null) {
            // Although we want to analyze the taintedness of xmlNavigation's index, we don't want it to affect the
            // taintedness of return value of xml navigation expression. Hence bypassing.
            TaintedStatus exprTaintStatus = getCurrentAnalysisState().taintedStatus;
            xmlNavigation.childIndex.accept(this);
            getCurrentAnalysisState().taintedStatus = exprTaintStatus;
        }
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        // Need to handle this properly. The flush expression can only return error or nil. ATM tainted status is set to
        // be untainted
        getCurrentAnalysisState().taintedStatus = TaintedStatus.UNTAINTED;
    }

    @Override
    public void visit(BLangTransactionalExpr transactionalExpr) {
        getCurrentAnalysisState().taintedStatus = TaintedStatus.UNTAINTED;
    }

    @Override
    public void visit(BLangCommitExpr commitExpr) {
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

        // If @untainted annotation is attached, we consider resulting value after conversion is untainted
        if (!conversionExpr.annAttachments.isEmpty()) {
            TaintedStatus annotStatus = getTaintedStatusBasedOnAnnotations(conversionExpr.annAttachments);
            if (annotStatus != TaintedStatus.IGNORED) {
                getCurrentAnalysisState().taintedStatus = annotStatus;
            }
        }
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
    public void visit(BLangRawTemplateLiteral rawTemplateLiteral) {
        analyzeExprList(rawTemplateLiteral.strings);
        analyzeExprList(rawTemplateLiteral.insertions);
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
    public void visit(BLangRestArgsExpression varArgsExpression) {
        varArgsExpression.expr.accept(this);
    }

    @Override
    public void visit(BLangNamedArgsExpression namedArgsExpression) {
        namedArgsExpression.expr.accept(this);
    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {
        bLangMatchExpression.expr.accept(this);
    }

    @Override
    public void visit(BLangCheckedExpr match) {
        match.expr.accept(this);
        if (getCurrentAnalysisState().taintedStatus == TaintedStatus.TAINTED) {
            getCurrentAnalysisState().returnTaintedStatus = TaintedStatus.TAINTED;
        }
    }

    @Override
    public void visit(BLangCheckPanickedExpr checkPanicExpr) {
        checkPanicExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        // Taintedness of a service variable depends on taintedness of listeners attached to this service.
        // If any listener is tainted then the service variable is tainted.

        boolean anyListenerstainted = false;
        for (BLangExpression expr : serviceConstructorExpr.serviceNode.attachedExprs) {
            if (expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF && ((BLangSimpleVarRef) expr).symbol.tainted) {
                anyListenerstainted = true;
                break;
            }
            if (expr.getKind() == NodeKind.TYPE_INIT_EXPR) {
                anyListenerstainted = true;
                break;
            }
        }

        if (anyListenerstainted || serviceConstructorExpr.type.tsymbol.tainted) {
            // Service type tainted due to listeners being tainted.
            setTaintedStatus(serviceConstructorExpr.type.tsymbol, TaintedStatus.TAINTED);
        }
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
    public void visit(BLangIntersectionTypeNode intersectionTypeNode) {
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
    public void visit(BLangIndexBasedAccess.BLangTableAccessExpr tableKeyAccessExpr) {
        /* ignore */
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlIndexAccessExpr) {
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
    public void visit(BLangIndexBasedAccess.BLangStringAccessExpr stringAccessExpr) {
        /* ignore */
    }

    @Override
    public void visit(BLangQueryExpr queryExpr) {
        BLangInputClause inputClause = (BLangInputClause) queryExpr.getQueryClauses().get(0);
        for (BLangNode clause : queryExpr.getQueryClauses()) {
            if (clause.getKind() == NodeKind.FROM || clause.getKind() == NodeKind.JOIN) {
                inputClause  = (BLangInputClause) clause;
            }
            ((BLangExpression) inputClause.getCollection()).accept(this);
            if (getCurrentAnalysisState().taintedStatus == TaintedStatus.TAINTED) {
                setTaintedStatus((BLangVariable) inputClause.variableDefinitionNode.getVariable(),
                        getCurrentAnalysisState().taintedStatus);
            }
        }
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
    private void analyzeExprList(List<? extends BLangExpression> exprs) {
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
            for (BLangVariable variable : ((BLangTupleVariable) varNode).memberVariables) {
                setTaintedStatus(variable, taintedStatus);
            }
            return;
        }

        if (varNode.getKind() == NodeKind.VARIABLE) {
            BLangSimpleVariable simpleVarNode = (BLangSimpleVariable) varNode;
            boolean isTaintedVar = simpleVarNode.symbol != null && !simpleVarNode.symbol.tainted;
            if (taintedStatus != TaintedStatus.IGNORED && (overridingAnalysis || isTaintedVar)) {
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
            getCurrentAnalysisState().prevSymbolTaintStatus.put(symbol, symbol.tainted);
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

        boolean markParamsTainted = true;
        if (invNode.getKind() == NodeKind.FUNCTION
                && ((BLangFunction) invNode).receiver != null) {
            if (((BLangFunction) invNode).receiver.type.tag == TypeTags.SERVICE) {
                // When service definition is bound to a untainted listeners, arguments to resource functions
                // are considered untainted.
                if (!((BLangFunction) invNode).receiver.type.tsymbol.tainted) {
                    markParamsTainted = false;
                }
            }
        }
        // Entry point input parameters are all tainted, since they contain user controlled data.
        // If any value has been marked "untainted" generate an error.
        // Except when the listener is statically verifiable to be untainted.
        if (isEntryPointParamsInvalid(invNode.requiredParams, markParamsTainted)) {
            return;
        }
        if (invNode.restParam != null
                && isEntryPointParamsInvalid(Collections.singletonList(invNode.restParam), markParamsTainted)) {
            return;
        }
        // Perform end point analysis.
        entryPointAnalysis = true;
        analyzeReturnTaintedStatus(invNode, funcEnv);
        entryPointAnalysis = false;

        boolean isBlocked = processBlockedNode(invNode);
        if (!isBlocked) {
            // Display errors only if scan of was fully complete, so that errors will not get duplicated.
            for (TaintRecord.TaintError taintError : getCurrentAnalysisState().taintErrorSet) {
                if (taintError.diagnosticCode == DiagnosticCode.TAINTED_VALUE_PASSED_TO_MODULE_OBJECT) {
                    dlogSet.add(new TaintRecord.TaintError(taintError.pos, taintError.paramName,
                            DiagnosticCode.INVOCATION_TAINT_GLOBAL_OBJECT));
                } else {
                    dlogSet.add(taintError);
                }
            }
        }
    }

    private boolean isEntryPointParamsInvalid(List<BLangSimpleVariable> params, boolean markParamsTainted) {
        if (params != null) {
            for (BLangSimpleVariable param : params) {
                if (markParamsTainted) {
                    setTaintedStatus(param.symbol, TaintedStatus.TAINTED);
                } else {
                    setTaintedStatus(param.symbol, TaintedStatus.UNTAINTED);
                }

                if (hasAnnotation(param, ANNOTATION_UNTAINTED)) {
                    this.dlog.error(param.pos, DiagnosticCode.ENTRY_POINT_PARAMETERS_CANNOT_BE_UNTAINTED,
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
        if (analyzerPhase == AnalyzerPhase.LOOPS_RESOLVED_ANALYSIS || invNode.symbol.taintTable == null) {
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
                BLangFunction prevTopLevelFunction = this.currTopLevelFunction;
                if (invNode.getKind() == NodeKind.FUNCTION) {
                    this.currTopLevelFunction = (BLangFunction) invNode;
                }
                isBlocked = processBlockedNode(this.currTopLevelFunction);
                this.currTopLevelFunction = prevTopLevelFunction;
            } else {
                isBlocked = processBlockedNode(invNode);
            }
            if (isBlocked) {
                return true;
            }

            int requiredParamCount = invNode.requiredParams.size();
            int totalParamCount = requiredParamCount + (invNode.restParam == null ? 0 : 1);
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
                    BLangSimpleVariable param = getParam(invNode, paramIndex, requiredParamCount);
                    // If parameter is untainted, it's invalid to have a case where tainted status of parameter is true.
                    if (hasAnnotation(param, ANNOTATION_UNTAINTED)) {
                        continue;
                    }
                    // Set each parameter "tainted", then analyze the body to observe the outcome of the function.
                    analyzeReturnTaintedStatus(taintTable, invNode, symbolEnv, paramIndex, requiredParamCount);
                    if (analyzerPhase == AnalyzerPhase.LOOP_ANALYSIS_COMPLETE) {
                        analyzerPhase = AnalyzerPhase.LOOP_ANALYSIS;
                    }
                    getCurrentAnalysisState().taintErrorSet.clear();
                }
            }
            invNode.symbol.taintTable = taintTable;
            validateReturnAndParameterTaintedAnnotations(invNode, taintTable);
            restCurrentTaintPropagationSet();
        }
        return false;
    }

    private void visitAttachedInvokable(BLangFunction invNode) {
        List<BLangSimpleVariable> prevRequiredParam = invNode.requiredParams;
        if (invNode.symbol.receiverSymbol != null) {
            BLangSimpleVariable self = invNode.receiver;
            List<BLangSimpleVariable> requiredParamsWithSelf = Lists.of(self);
            requiredParamsWithSelf.addAll(prevRequiredParam);
            invNode.requiredParams = requiredParamsWithSelf;
        }

        attachedFunctionAnalysis = true;
        visit(invNode);
        attachedFunctionAnalysis = false;

        invNode.requiredParams = prevRequiredParam;
    }

    private void analyzeAllParamsUntaintedReturnTaintedStatus(Map<Integer, TaintRecord> taintTable,
                                                              BLangInvokableNode invokableNode, SymbolEnv symbolEnv) {
        // Identify if current analysis is to identify return tainted status when all parameters are untainted in a top
        // level function. This is to separately identify if AllParamsUntainted analysis is being done for a top level
        // function or a lambda functions / worker lambda function.
        if (currTopLevelFunction == invokableNode) {
            topLevelFunctionAllParamsUntaintedAnalysis = true;
        }
        analyzeReturnTaintedStatus(taintTable, invokableNode, symbolEnv, ALL_UNTAINTED_TABLE_ENTRY_INDEX, 0);

        if (currTopLevelFunction == invokableNode) {
            topLevelFunctionAllParamsUntaintedAnalysis = false;
        }
    }

    // Validate the taintedness of return and parameter values when no tainted value is passed into the function.
    // In this case:
    // 1. If function returns a tainted value it must annotate return as @tainted.
    // 2. If function taints a passed in argument, it must annotate respective parameter @tainted.
    // 3. Function must not return a values who's taintedness is unknown.
    private void validateReturnAndParameterTaintedAnnotations(
            BLangInvokableNode invokableNode,
            Map<Integer, TaintRecord> taintedStatusBasedOnAnnotations) {

        if (this.analyzerPhase == AnalyzerPhase.LOOP_ANALYSIS
                || this.analyzerPhase == AnalyzerPhase.LOOP_ANALYSIS_COMPLETE) {
            return;
        }

        TaintRecord taintRecord = taintedStatusBasedOnAnnotations.get(ALL_UNTAINTED_TABLE_ENTRY_INDEX);
        if (taintRecord.returnTaintedStatus == TaintedStatus.TAINTED
                && !hasAnnotation(invokableNode.returnTypeAnnAttachments, ANNOTATION_TAINTED)
                && !invokableNode.flagSet.contains(Flag.LAMBDA)) {
            dlog.error(invokableNode.returnTypeNode.pos, DiagnosticCode.TAINTED_RETURN_NOT_ANNOTATED_TAINTED,
                    invokableNode.name.getValue());
            stopAnalysis = true;
        }

        int requiredParamCount = invokableNode.requiredParams.size();
        int totalParamCount = requiredParamCount + (invokableNode.restParam == null ? 0 : 1);

        for (int i = 0; i < totalParamCount; i++) {
            TaintedStatus taintedStatus = taintRecord.parameterTaintedStatusList.get(i);
            if (taintedStatus == TaintedStatus.TAINTED) {
                BLangSimpleVariable param = getParam(invokableNode, i, requiredParamCount);
                // Although we consider self as a parameter, we can not mark it as @tainted, hence skip error for it.
                if (i == 0 && (invokableNode.symbol.flags & Flags.ATTACHED) == Flags.ATTACHED) {
                    // skip 0th param (self) for object methods.
                    continue;
                }
                if (!hasAnnotation(param, ANNOTATION_TAINTED)) {
                    dlog.error(param.pos, DiagnosticCode.TAINTED_PARAM_NOT_ANNOTATED_TAINTED, param.name,
                            invokableNode.name);
                    stopAnalysis = true;
                }
            }
        }
    }

    private void analyzeReturnTaintedStatus(Map<Integer, TaintRecord> taintTable, BLangInvokableNode invokableNode,
                                            SymbolEnv symbolEnv, int paramIndex, int requiredParamCount) {
        getCurrentAnalysisState().returnTaintedStatus = TaintedStatus.UNTAINTED;
        getCurrentAnalysisState().parameterTaintedStatus = new ArrayList<>();
        resetTaintedStatusOfVariables(invokableNode.requiredParams);
        if (invokableNode.restParam != null) {
            resetTaintedStatusOfVariables(Collections.singletonList(invokableNode.restParam));
        }
        // Mark the given parameter "tainted".
        boolean prevParamTaintedState = false;
        if (paramIndex != ALL_UNTAINTED_TABLE_ENTRY_INDEX) {
            prevParamTaintedState = markNthParamSymbolTainted(invokableNode, paramIndex, requiredParamCount);
        }
        analyzeReturnTaintedStatus(invokableNode, symbolEnv);
        if (getCurrentAnalysisState().taintErrorSet.size() > 0) {
            // When invocation returns an error (due to passing a tainted argument to a untainted parameter) add current
            // error to the table for future reference. However, if taint-error is raised when analyzing all-parameters
            // are untainted, the code of the function is wrong (and passes a tainted value generated within the
            // function body to a untainted parameter). Hence, instead of adding error to table, directly generate the
            // error and fail the compilation.
            boolean isLambdaFunc = invokableNode.flagSet.contains(Flag.LAMBDA);
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
        if (paramIndex != ALL_UNTAINTED_TABLE_ENTRY_INDEX) {
            restoreNthParamSymbolTaintedness(invokableNode, paramIndex, requiredParamCount, prevParamTaintedState);
        }
    }

    private boolean markNthParamSymbolTainted(BLangInvokableNode invokableNode, int paramIndex,
                                              int requiredParamCount) {
        return changeNthParamSymbolTo(invokableNode, paramIndex, requiredParamCount, true);
    }

    private void restoreNthParamSymbolTaintedness(BLangInvokableNode invokableNode, int paramIndex,
                                                  int requiredParamCount, boolean prevTaintedStatet) {
        changeNthParamSymbolTo(invokableNode, paramIndex, requiredParamCount, prevTaintedStatet);
    }

    private boolean changeNthParamSymbolTo(BLangInvokableNode invokableNode, int paramIndex, int requiredParamCount,
                                           boolean toTaintedState) {
        if (paramIndex < requiredParamCount) {
            boolean prevState = invokableNode.requiredParams.get(paramIndex).symbol.tainted;
            setTaintedStatus(invokableNode.requiredParams.get(paramIndex).symbol,
                    toTaintedState ? TaintedStatus.TAINTED : TaintedStatus.UNTAINTED);
            return prevState;
        } else {
            if (invokableNode.restParam != null) {
                boolean prevState = invokableNode.restParam.symbol.tainted;
                setTaintedStatus(invokableNode.restParam.symbol,
                        toTaintedState ? TaintedStatus.TAINTED : TaintedStatus.UNTAINTED);
                return prevState;
            }
        }
        return false;
    }

    private void resetTaintedStatusOfVariables(List<BLangSimpleVariable> params) {
        if (params != null) {
            params.forEach(param -> setTaintedStatus(param.symbol, TaintedStatus.UNTAINTED));
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
            int totalParamCount = requiredParamCount + (invokableNode.restParam == null ? 0 : 1);

            // Since this native function is being analyzed, the parameter tainted state depends on annotations.
            // If no parameter annotations are present, leave the tainted state of the argument unchanged.
            List<TaintedStatus> paramTaintedStatusList = new ArrayList<>();
            for (int paramIndex = 0; paramIndex < totalParamCount; paramIndex++) {
                BLangVariable param = getParam(invokableNode, paramIndex, requiredParamCount);
                TaintedStatus taintedStateBasedOnAnnotations = getTaintedStatusBasedOnAnnotations(param.annAttachments);
                paramTaintedStatusList.add(taintedStateBasedOnAnnotations);
            }

            // Append taint table with tainted status when no parameter is tainted.
            Map<Integer, TaintRecord> taintTable = new HashMap<>();
            taintTable.put(ALL_UNTAINTED_TABLE_ENTRY_INDEX, new TaintRecord(retParamsTaintedStatus,
                    paramTaintedStatusList));

            // Append taint table with tainted status when each parameter is tainted.
            for (int paramIndex = 0; paramIndex < totalParamCount; paramIndex++) {
                BLangSimpleVariable param = getParam(invokableNode, paramIndex, requiredParamCount);
                // If parameter is untainted, test for this parameter being tainted is invalid.
                if (hasAnnotation(param, ANNOTATION_UNTAINTED)) {
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
            int totalParamCount = requiredParamCount + (function.restParam == null ? 0 : 1);
            Map<Integer, TaintRecord> taintTable = function.symbol.taintTable;
            for (int paramIndex = 0; paramIndex < totalParamCount; paramIndex++) {
                TaintRecord taintRecord = taintTable.get(paramIndex);
                BLangSimpleVariable param = getParam(function, paramIndex, requiredParamCount);
                if (taintRecord == null) {
                    addTaintError(argExpr.pos, param.name.value,
                            DiagnosticCode.TAINTED_VALUE_PASSED_TO_UNTAINTED_PARAMETER);
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

    private void addTaintError(DiagnosticPos diagnosticPos, String paramName, String paramName2,
                               DiagnosticCode diagnosticCode) {
        TaintRecord.TaintError taintError = new TaintRecord.TaintError(diagnosticPos, paramName, paramName2,
                diagnosticCode);
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
        if (getCurrentAnalysisState().restParam != null) {
            updateParameterTaintedStatuses(Collections.singletonList(getCurrentAnalysisState().restParam),
                    getCurrentAnalysisState().requiredParams.size());
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
            for (BLangSimpleVariable param : paramList) {
                TaintedStatus taintedStateBasedOnAnnotations = getTaintedStatusBasedOnAnnotations(param.annAttachments);
                if (taintedStateBasedOnAnnotations == TaintedStatus.IGNORED) {
                    // If annotations are not use, use the analyzed status.
                    if (types.isValueType(param.type)) {
                        // As we can't push values out of a value typed parameter, function body can't taint
                        // value typed parameter.
                        getCurrentAnalysisState().parameterTaintedStatus.add(TaintedStatus.UNTAINTED);
                    } else {
                        getCurrentAnalysisState().parameterTaintedStatus.add(param.symbol.tainted ?
                                TaintedStatus.TAINTED : TaintedStatus.UNTAINTED);
                    }
                } else {
                    // If parameter has "tainted" or "untainted" annotation, argument should be updated according to the
                    // annotated status regardless of the analyzed status. This is useful if someone need to explicitly
                    // mark any argument that is set as a parameter "tainted" or "untainted".
                    getCurrentAnalysisState().parameterTaintedStatus.add(taintedStateBasedOnAnnotations);
                }
            }
        } else {
            // If list already contains tainted state of current parameter list, update the existing list. This can
            // happen, when there are multiple returns within the same function.
            for (int paramIndex = 0; paramIndex < paramList.size(); paramIndex++) {
                BLangVariable param = paramList.get(paramIndex);
                TaintedStatus taintedStateBasedOnAnnotations = getTaintedStatusBasedOnAnnotations(param.annAttachments);
                if (taintedStateBasedOnAnnotations == TaintedStatus.TAINTED || param.symbol.tainted) {
                    if (types.isValueType(param.type)) {
                        // As we can't push values out of a value typed parameter, function body can't taint
                        // value typed parameter.
                        getCurrentAnalysisState().parameterTaintedStatus.set(startIndex + paramIndex,
                                TaintedStatus.UNTAINTED);
                    } else {
                        getCurrentAnalysisState().parameterTaintedStatus.set(startIndex + paramIndex,
                                TaintedStatus.TAINTED);
                    }
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
        Map<Integer, TaintRecord> origTaintTable = invokableSymbol.taintTable;
        TaintedStatus returnTaintedStatus = TaintedStatus.UNTAINTED;
        List<TaintedStatus> argTaintedStatusList = new ArrayList<>();

        // Get tainted status when all parameters are untainted
        TaintRecord allParamsUntaintedRecord = origTaintTable.get(ALL_UNTAINTED_TABLE_ENTRY_INDEX);
        if (allParamsUntaintedRecord != null) {
            if (allParamsUntaintedRecord.taintError != null && allParamsUntaintedRecord.taintError.size() > 0) {
                // This can occur when there is a error regardless of tainted status of parameters.
                // Example: Tainted value returned by function is passed to another functions's untainted parameter.
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
        int requiredParamCount = invokableSymbol.params.size();

        int namedArgsCount = countNamedArgs(invocationExpr);
        int positionalArgsCount = invocationExpr.requiredArgs.size() - namedArgsCount;
        List<BLangExpression> restArgs = invocationExpr.restArgs;
        int restArgsCount = restArgs.size();

        List<Integer> paramPositionsOfProvidedArguments =
                getParamPositionsOfProvidedArguments(invocationExpr.requiredArgs,
                        ((BInvokableSymbol) invocationExpr.symbol).params);

        // How each parameter taint (verb) receiver parameter (the 0th in attached functions)
        List<TaintedStatus> paramsReceiverTainting = null;
        // Taint table of attached functions are calculated by considering the receiver as the first (0th) param.
        // Hence add the receiver to required arg count.
        if (isTaintAnalyzableAttachedFunction(invocationExpr)) {
            BVarSymbol receiverSymbol = getMethodReceiverSymbol(invocationExpr.expr);

            returnTaintedStatus = analyzeAllArgsUntaintedReceiverTaintedness(
                    invocationExpr, origTaintTable, returnTaintedStatus, receiverSymbol);

            if (stopAnalysis) {
                return;
            }

            // What happens to self (oth param) when each param (excluding 0th) is tainted.
            paramsReceiverTainting = collectSelfTaintednessForEachTaintedParam(
                    origTaintTable, paramPositionsOfProvidedArguments, restArgsCount);

            invokableSymbol.taintTable = duplicateTaintTableSkippingReceiverEntry(origTaintTable);
            if (!argTaintedStatusList.isEmpty()) {
                argTaintedStatusList.remove(0); // remove self params tainted status from list.
            }
        }

        for (int reqArgIndex = 0; reqArgIndex < positionalArgsCount; reqArgIndex++) {
            BLangExpression argExpr = invocationExpr.requiredArgs.get(reqArgIndex);
            TaintedStatus argumentTaintReturnValue = analyzeInvocationArgument(reqArgIndex, invocationExpr, argExpr,
                    argTaintedStatusList);
            if (restoreTableIfIgnored(invokableSymbol, origTaintTable, argumentTaintReturnValue))  {
                return;
            }

            returnTaintedStatus = updateAndGetTaintedStatus(invocationExpr, returnTaintedStatus, paramsReceiverTainting,
                                                            reqArgIndex, argumentTaintReturnValue);
            if (stopAnalysis) {
                break;
            }
        }
        for (BLangExpression argExpr : invocationExpr.requiredArgs) {
            if (argExpr.getKind() != NodeKind.NAMED_ARGS_EXPR) {
                continue;
            }

            String currentNamedArgExprName = ((BLangNamedArgsExpression) argExpr).name.value;
            // Pick the index of this defaultable parameter in the invokable definition.
            int paramIndex = findDefaultableParamIndex(invokableSymbol, requiredParamCount,
                    currentNamedArgExprName);
            TaintedStatus argumentTaintReturnValue = analyzeInvocationArgument(paramIndex, invocationExpr,
                    argExpr, argTaintedStatusList);
            if (restoreTableIfIgnored(invokableSymbol, origTaintTable, argumentTaintReturnValue))  {
                return;
            }

            returnTaintedStatus = updateAndGetTaintedStatus(invocationExpr, returnTaintedStatus, paramsReceiverTainting,
                                                            paramIndex, argumentTaintReturnValue);
            if (stopAnalysis) {
                break;
            }
        }

        if (restArgsCount == 1 && positionalArgsCount < requiredParamCount &&
                restArgs.get(0).getKind() == NodeKind.REST_ARGS_EXPR) {
            BLangExpression varArg = restArgs.get(0);
            for (int paramIndex = positionalArgsCount; paramIndex < requiredParamCount; paramIndex++) {
                TaintedStatus argumentTaintReturnValue = analyzeInvocationArgument(paramIndex, invocationExpr, varArg,
                                                                                   argTaintedStatusList);
                if (restoreTableIfIgnored(invokableSymbol, origTaintTable, argumentTaintReturnValue)) {
                    return;
                }

                returnTaintedStatus = updateAndGetTaintedStatus(invocationExpr, returnTaintedStatus,
                                                                paramsReceiverTainting, paramIndex,
                                                                argumentTaintReturnValue);

                if (stopAnalysis) {
                    break;
                }
            }

            if (invokableSymbol.restParam != null) {
                int paramIndex = requiredParamCount;
                TaintedStatus argumentTaintReturnValue = analyzeInvocationArgument(paramIndex, invocationExpr, varArg,
                                                                                   argTaintedStatusList);
                if (restoreTableIfIgnored(invokableSymbol, origTaintTable, argumentTaintReturnValue))  {
                    return;
                }

                returnTaintedStatus = updateAndGetTaintedStatus(invocationExpr, returnTaintedStatus,
                                                                paramsReceiverTainting, paramIndex,
                                                                argumentTaintReturnValue);
            }
        } else {
            for (int argIndex = 0; argIndex < restArgsCount; argIndex++) {
                BLangExpression argExpr = restArgs.get(argIndex);
                // Pick the index of the rest parameter in the invokable definition.
                int paramIndex = requiredParamCount;
                TaintedStatus argumentTaintReturnValue = analyzeInvocationArgument(paramIndex, invocationExpr, argExpr,
                                                                                   argTaintedStatusList);
                if (restoreTableIfIgnored(invokableSymbol, origTaintTable, argumentTaintReturnValue))  {
                    return;
                }

                returnTaintedStatus = updateAndGetTaintedStatus(invocationExpr, returnTaintedStatus,
                                                                paramsReceiverTainting, paramIndex,
                                                                argumentTaintReturnValue);

                if (stopAnalysis) {
                    break;
                }
            }
        }

        updateArgTaintedStatus(invocationExpr, argTaintedStatusList);
        invokableSymbol.taintTable = origTaintTable; // restore taint table.

        // Update receiver tainted status
        if (invocationExpr.expr != null) {
            //TODO: TaintedIf annotation, so that it's possible to define what can taint or untaint the return.
            invocationExpr.expr.accept(this);
            if (getCurrentAnalysisState().taintedStatus == TaintedStatus.IGNORED) {
                return;
            } else if (getCurrentAnalysisState().taintedStatus == TaintedStatus.TAINTED) {
                returnTaintedStatus = getTaintedStatusOfReceiverParam(invokableSymbol);
            }
        }
        getCurrentAnalysisState().taintedStatus = returnTaintedStatus;
    }

    private TaintedStatus getTaintedStatusOfReceiverParam(BInvokableSymbol invokableSymbol) {
        TaintRecord taintRecordOfReceiverParam = invokableSymbol.taintTable.get(0);
        if (taintRecordOfReceiverParam == null) {
            return TaintedStatus.TAINTED;
        } else {
            return taintRecordOfReceiverParam.returnTaintedStatus;
        }
    }

    private boolean restoreTableIfIgnored(BInvokableSymbol invokableSymbol, Map<Integer, TaintRecord> origTaintTable,
     TaintedStatus argumentTaintReturnValue) {
        if (argumentTaintReturnValue != TaintedStatus.IGNORED) {
            return false;
        }
        invokableSymbol.taintTable = origTaintTable; // restore taint table.
        return true;
    }

    private TaintedStatus updateAndGetTaintedStatus(BLangInvocation invocationExpr, TaintedStatus returnTaintedStatus,
                                                    List<TaintedStatus> paramsReceiverTainting, int reqArgIndex,
                                                    TaintedStatus argumentTaintReturnValue) {
        if (argumentTaintReturnValue == TaintedStatus.TAINTED) {
            returnTaintedStatus = TaintedStatus.TAINTED;
        }

        if (getCurrentAnalysisState().taintedStatus == TaintedStatus.TAINTED) {
            updateSelfTaintedStatusToTainted(invocationExpr, paramsReceiverTainting, reqArgIndex);
        }
        return returnTaintedStatus;
    }

    private int countNamedArgs(BLangInvocation invocationExpr) {
        int namedArgsCount = 0;
        for (BLangExpression arg : invocationExpr.requiredArgs) {
            if (arg.getKind() == NodeKind.NAMED_ARGS_EXPR) {
                namedArgsCount++;
            }
        }
        return namedArgsCount;
    }

    private List<Integer> getParamPositionsOfProvidedArguments(List<BLangExpression> args, List<BVarSymbol> params) {
        ArrayList<Integer> positions = new ArrayList<>();
        for (int i = 0; i < args.size(); i++) {
            BLangExpression arg = args.get(i);
            if (arg.getKind() == NodeKind.NAMED_ARGS_EXPR) {
                String paramName = ((BLangNamedArgsExpression) arg).name.value;
                for (int p = 0; p < params.size(); p++) {
                    BVarSymbol paramSymbol = params.get(p);
                    if (paramSymbol.name.value.equals(paramName)) {
                        positions.add(p);
                    }
                }
            } else {
                positions.add(i);
            }
        }
        return positions;
    }

    private int findDefaultableParamIndex(BInvokableSymbol invokableSymbol,
                                          int requiredParamCount, String currentNamedArgExprName) {
        int paramIndex = 0;
        for (int defaultableParamIndex = 0; defaultableParamIndex < requiredParamCount;
             defaultableParamIndex++) {
            BVarSymbol defaultableParam = invokableSymbol.params.get(defaultableParamIndex);
            if (defaultableParam.name.value.equals(currentNamedArgExprName)) {
                paramIndex = defaultableParamIndex;
                break;
            }
        }
        return paramIndex;
    }

    private TaintedStatus analyzeAllArgsUntaintedReceiverTaintedness(BLangInvocation invocationExpr,
                                                                     Map<Integer, TaintRecord> taintTable,
                                                                     TaintedStatus returnTaintedStatus,
                                                                     BVarSymbol receiverSymbol) {
        TaintRecord receiverTaintRecord = taintTable.get(0);
        if (receiverSymbol != null) {
            // receiverSymbol == null for method invocations on literals "str".substring()
            returnTaintedStatus = checkTaintErrorsInObjectMethods(invocationExpr,
                    returnTaintedStatus, receiverSymbol, receiverTaintRecord);
        }

        TaintRecord allUntaintedEntry = taintTable.get(-1); // when everything is untainted
        if (allUntaintedEntry != null
                && allUntaintedEntry.parameterTaintedStatusList != null
                && !allUntaintedEntry.parameterTaintedStatusList.isEmpty()
                && allUntaintedEntry.parameterTaintedStatusList.get(0) == TaintedStatus.TAINTED) {
            // Method invocation taint self even when non of the inputs args are tainted.
            visitAssignment(invocationExpr.expr, TaintedStatus.TAINTED, invocationExpr.pos);
        }

        if (receiverTaintRecord != null && receiverTaintRecord.parameterTaintedStatusList != null) {
            List<TaintedStatus> actualArgTaintStatus = receiverTaintRecord.parameterTaintedStatusList
                    .subList(1, receiverTaintRecord.parameterTaintedStatusList.size());
            updateArgTaintedStatus(invocationExpr, actualArgTaintStatus);
        }
        return returnTaintedStatus;
    }

    private List<TaintedStatus> collectSelfTaintednessForEachTaintedParam(Map<Integer, TaintRecord> taintTable,
                                                                          List<Integer> paramPositionsOfProvidedArgs,
                                                                          int restArgsCount) {
        // Collect the tainted status of self parameter when each provided parameter is tainted.
        // This list does not consider what self param do to it self.
        int paramsCount = paramPositionsOfProvidedArgs.size() - restArgsCount;
        // -1 for all param untainted status and -1 for self taint status.
        int paramSize = taintTable.size() - 1 - (taintTable.containsKey(0) ? -1 : 0);
        List<TaintedStatus> selfTaintedStatusList = new ArrayList<>(
                Collections.nCopies(paramSize, TaintedStatus.IGNORED));

        for (int i = 0; i < paramsCount; i++) {
            Integer argPos = paramPositionsOfProvidedArgs.get(i);
            // Skip self params taint record and only operate on externally provided args.
            TaintRecord taintRecord = taintTable.get(argPos + 1);
            if (taintRecord == null) {
                // No record in the taint table, this is a untainted param.
                selfTaintedStatusList.add(TaintedStatus.UNTAINTED);
                continue;
            }

            if (taintRecord.parameterTaintedStatusList != null) {
                selfTaintedStatusList.add(argPos, taintRecord.parameterTaintedStatusList.get(0));
            } else {
                selfTaintedStatusList.add(argPos, TaintedStatus.TAINTED);
            }
        }
        return selfTaintedStatusList;
    }

    private TaintedStatus checkTaintErrorsInObjectMethods(BLangInvocation invocationExpr,
                                                          TaintedStatus returnTaintedStatus,
                                                          BVarSymbol receiverSymbol,
                                                          TaintRecord receiverTaintRecord) {
        if (receiverSymbol.tainted) {
            if (receiverTaintRecord == null) {
                // This scenario indicate that passing a tainted value to the receiver causes tainted value to be
                // passed to a untainted parameter somewhere inside the function.
                addTaintError(invocationExpr.pos, receiverSymbol.name.value,
                        DiagnosticCode.TAINTED_VALUE_PASSED_TO_UNTAINTED_PARAMETER_ORIGINATING_AT);
                return returnTaintedStatus;
            }

            // does this taint other params and return?
            if (receiverTaintRecord.returnTaintedStatus == TaintedStatus.TAINTED) {
                returnTaintedStatus = TaintedStatus.TAINTED;
            }
            if (receiverTaintRecord.taintError != null && !receiverTaintRecord.taintError.isEmpty()) {
                for (TaintRecord.TaintError error : receiverTaintRecord.taintError) {
                    if (error.diagnosticCode == DiagnosticCode.TAINTED_VALUE_PASSED_TO_GLOBAL_VARIABLE) {
                        addTaintError(receiverTaintRecord.taintError);
                    } else if (error.diagnosticCode == DiagnosticCode.TAINTED_VALUE_PASSED_TO_MODULE_OBJECT) {
                        addTaintError(invocationExpr.pos,
                                getMethodReceiverSymbol(invocationExpr).name.value, error.diagnosticCode);
                    } else {
                        // Indicate that at this point receiver/self being tainted cause tainted value
                        // (via receiver/self) passing to a untainted parameter down the call.
                        addTaintError(invocationExpr.pos, error.paramName.get(0), invocationExpr.name.value,
                                DiagnosticCode.TAINTED_VALUE_PASSED_TO_UNTAINTED_PARAMETER_ORIGINATING_AT);
                    }
                }
            }
        }
        return returnTaintedStatus;
    }

    private boolean isTaintAnalyzableAttachedFunction(BLangInvocation invocationExpr) {
        boolean isAttachedFunction = (invocationExpr.symbol.flags & Flags.ATTACHED) == Flags.ATTACHED;
        boolean hasExpr = invocationExpr.expr != null;
        boolean isTypeInit = invocationExpr.parent != null
                && invocationExpr.parent.getKind() == NodeKind.TYPE_INIT_EXPR;
        return isAttachedFunction && (hasExpr || isTypeInit);
    }

    private void updateSelfTaintedStatusToTainted(BLangInvocation invocationExpr,
                                                  List<TaintedStatus> paramVsSelfTaintedStatus, int argIndex) {
        if (paramVsSelfTaintedStatus != null && paramVsSelfTaintedStatus.get(argIndex) == TaintedStatus.TAINTED) {
            visitAssignment(invocationExpr.expr, TaintedStatus.TAINTED, invocationExpr.pos);
        }
    }

    private BVarSymbol getMethodReceiverSymbol(BLangExpression expr) {
        if (expr == null) {
            return null;
        }
        switch (expr.getKind()) {
            case SIMPLE_VARIABLE_REF:
                return (BVarSymbol) ((BLangSimpleVarRef) expr).symbol;
            case FIELD_BASED_ACCESS_EXPR:
                return getMethodReceiverSymbol(((BLangFieldBasedAccess) expr).expr);
            case INDEX_BASED_ACCESS_EXPR:
                return getMethodReceiverSymbol(((BLangIndexBasedAccess) expr).expr);
            case INVOCATION:
                return (BVarSymbol) ((BLangInvocation) expr).symbol;
            default:
                return null;
        }
    }

    private Map<Integer, TaintRecord> duplicateTaintTableSkippingReceiverEntry(Map<Integer, TaintRecord> taintTable) {
        Map<Integer, TaintRecord> taintTableWithoutReceiver = new HashMap<>();
        if (taintTable.containsKey(-1)) {
            taintTableWithoutReceiver.put(-1, skipTaintRecSelfParam(taintTable.get(-1)));
        }
        for (Map.Entry<Integer, TaintRecord> entry : taintTable.entrySet()) {
            if (entry.getKey() > 0) {
                taintTableWithoutReceiver.put(entry.getKey() - 1, skipTaintRecSelfParam(entry.getValue()));
            }
        }
        return taintTableWithoutReceiver;
    }

    private TaintRecord skipTaintRecSelfParam(TaintRecord taintRecord) {
        if (taintRecord.parameterTaintedStatusList == null || taintRecord.parameterTaintedStatusList.isEmpty()) {
            return taintRecord;
        }
        TaintRecord newRec = new TaintRecord(taintRecord.taintError);
        newRec.returnTaintedStatus = taintRecord.returnTaintedStatus;
        newRec.parameterTaintedStatusList =
                taintRecord.parameterTaintedStatusList.subList(1, taintRecord.parameterTaintedStatusList.size());
        return newRec;
    }

    private void updateArgTaintedStatus(BLangInvocation invocationExpr, List<TaintedStatus> argTaintedStatusList) {
        BInvokableSymbol invokableSymbol = (BInvokableSymbol) invocationExpr.symbol;
        int requiredParamCount = invokableSymbol.params.size();
        List<BLangExpression> positionalArgs = invocationExpr.requiredArgs.stream()
                .filter(a -> a.getKind() != NodeKind.NAMED_ARGS_EXPR).collect(Collectors.toList());
        List<BLangExpression> namedArgs = invocationExpr.requiredArgs.stream()
                .filter(a -> a.getKind() == NodeKind.NAMED_ARGS_EXPR).collect(Collectors.toList());
        int namedArgsCount = namedArgs.size();
        int positionalArgsCount = positionalArgs.size();

        List<BLangExpression> restArgs = invocationExpr.restArgs;
        int restArgsCount = restArgs.size();

        for (int argIndex = 0; argIndex < positionalArgsCount; argIndex++) {
            BLangExpression argExpr = positionalArgs.get(argIndex);
            TaintedStatus argTaintedStatus = TaintedStatus.IGNORED;
            if (!argTaintedStatusList.isEmpty()) {
                argTaintedStatus = argTaintedStatusList.get(argIndex);
            }
            if (argTaintedStatus == TaintedStatus.TAINTED) {
                updateArgTaintedStatus(argExpr, argTaintedStatus);
            }
        }

        for (int argIndex = 0; argIndex < namedArgsCount; argIndex++) {
            BLangExpression argExpr = namedArgs.get(argIndex);
            String currentNamedArgExprName = ((BLangNamedArgsExpression) argExpr).name.value;
            // Pick the index of this defaultable parameter in the invokable definition.
            int paramIndex = findDefaultableParamIndex(invokableSymbol, requiredParamCount, currentNamedArgExprName);
            TaintedStatus argTaintedStatus = argTaintedStatusList.get(paramIndex);
            updateArgTaintedStatus(argExpr, argTaintedStatus);
        }

        if (positionalArgsCount < requiredParamCount &&
                restArgsCount == 1 && restArgs.get(0).getKind() == NodeKind.REST_ARGS_EXPR) {
            // Some of the required/defaultable args have been provided for via the rest arg.
            BLangExpression restArgExpr = restArgs.get(0);
            for (int paramIndex = positionalArgsCount; paramIndex < requiredParamCount; paramIndex++) {
                TaintedStatus argTaintedStatus = argTaintedStatusList.get(paramIndex);
                if (argTaintedStatus == TaintedStatus.TAINTED) {
                    updateArgTaintedStatus(restArgExpr, argTaintedStatus);
                }
            }

            if (invokableSymbol.restParam != null) {
                TaintedStatus argTaintedStatus = argTaintedStatusList.get(requiredParamCount);
                updateArgTaintedStatus(restArgExpr, argTaintedStatus);
            }
        } else {
            for (int argIndex = 0; argIndex < restArgsCount; argIndex++) {
                BLangExpression argExpr = restArgs.get(argIndex);
                // Pick the index of the rest parameter in the invokable definition.
                TaintedStatus argTaintedStatus = argTaintedStatusList.get(requiredParamCount);
                updateArgTaintedStatus(argExpr, argTaintedStatus);
            }
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
        } else if (varRefExpr.getKind() == NodeKind.TYPE_CONVERSION_EXPR) {
            visitAssignment(((BLangTypeConversionExpr) varRefExpr).expr, varTaintedStatus, varRefExpr.pos);
        }
    }

    /**
     * Analyze one invocation argument, determine if the argument expression is tainted, if so consult the taint table
     * of the invokable to check if a taint-error is present.
     *
     * If argument is tainted and it cause the function to return a tainted value, indicate that via return value.
     *
     * @param paramIndex index of the parameter for the current argument
     * @param invocationExpr invocation expression relevant to the invocation
     * @param argExpr argument expression being analyzed
     * @param argTaintedStatusList the combined argument tainted status list
     * @return tainted status of function due to the argument expression
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
            BVarSymbol paramSymbol = getParamSymbol(invokableSymbol, paramIndex, requiredParamCount);

            if (taintRecord == null) {
                // This is when current parameter is "untainted". Therefore, providing a tainted value to a untainted
                // parameter is invalid and should return a compiler error.
                DiagnosticPos argPos = argExpr.pos != null ? argExpr.pos : invocationExpr.pos;
                addTaintError(argPos, paramSymbol.name.value,
                        DiagnosticCode.TAINTED_VALUE_PASSED_TO_UNTAINTED_PARAMETER);
                this.stopAnalysis = false;
            } else if (taintRecord.taintError != null && taintRecord.taintError.size() > 0) {
                // This is when current parameter is derived to be untainted.
                taintRecord.taintError.forEach(error -> {
                    if (error.diagnosticCode == DiagnosticCode.TAINTED_VALUE_PASSED_TO_GLOBAL_VARIABLE
                        || error.diagnosticCode == DiagnosticCode.TAINTED_VALUE_PASSED_TO_MODULE_OBJECT) {
                        addTaintError(taintRecord.taintError);
                    } else {
                        DiagnosticPos argPos = argExpr.pos != null ? argExpr.pos : invocationExpr.pos;
                        addTaintError(argPos, paramSymbol.name.value,
                                DiagnosticCode.TAINTED_VALUE_PASSED_TO_UNTAINTED_PARAMETER);
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

    private BLangSimpleVariable getParam(BLangInvokableNode invNode, int paramIndex, int requiredParamCount) {
        BLangSimpleVariable param;
        if (paramIndex < requiredParamCount) {
            param = invNode.requiredParams.get(paramIndex);
        } else {
            param = invNode.restParam;
        }
        return param;
    }

    private BVarSymbol getParamSymbol(BInvokableSymbol invSymbol, int paramIndex, int requiredParamCount) {
        BVarSymbol param;
        if (paramIndex < requiredParamCount) {
            param = invSymbol.params.get(paramIndex);
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
        private BLangSimpleVariable restParam;
        private List<TaintedStatus> parameterTaintedStatus;

        private BlockedNode blockedNode;

        private Set<TaintRecord.TaintError> taintErrorSet = new LinkedHashSet<>();
        private Map<BSymbol, Boolean> prevSymbolTaintStatus = new HashMap<>();
    }
}

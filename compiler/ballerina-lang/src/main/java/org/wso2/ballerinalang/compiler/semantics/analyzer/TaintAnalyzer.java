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
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.TaintRecord;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotAttribute;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangDeprecatedNode;
import org.wso2.ballerinalang.compiler.tree.BLangDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangEnum;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangObject;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecord;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.tree.BLangTransformer;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttributeValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAwaitExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangDocumentationAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableQueryExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangBind;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangCompoundAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangDone;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangNext;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPostIncrement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
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
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangTupleTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUnionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerUtils;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.XMLConstants;

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
    private PackageID mainPkgId;
    private Names names;
    private SymbolTable symTable;
    private BLangDiagnosticLog dlog;

    private boolean nonOverridingAnalysis;
    private boolean entryPointAnalysis;
    private boolean stopAnalysis;
    private boolean blockedOnWorkerInteraction;
    private BlockedNode blockedNode;
    private Boolean taintedStatus;
    private Boolean returnTaintedStatus;
    private Set<TaintRecord.TaintError> taintErrorSet = new LinkedHashSet<>();
    private Map<BlockingNode, List<BlockedNode>> blockedNodeMap = new HashMap<>();
    private Map<BLangIdentifier, Boolean> workerInteractionTaintedStatusMap;
    private BLangIdentifier currWorkerIdentifier;
    private BLangIdentifier currForkIdentifier;

    private static final String ANNOTATION_TAINTED = "tainted";
    private static final String ANNOTATION_UNTAINTED = "untainted";
    private static final String ANNOTATION_SENSITIVE = "sensitive";

    private static final int ALL_UNTAINTED_TABLE_ENTRY_INDEX = -1;
    private static final int RETURN_TAINTED_STATUS_COLUMN_INDEX = 0;

    private enum AnalyzerPhase { INITIAL_ANALYSIS, BLOCKED_NODE_ANALYSIS }
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
        this.names = Names.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.symTable = SymbolTable.getInstance(context);
    }

    public BLangPackage analyze(BLangPackage pkgNode) {
        blockedNode = null;
        taintErrorSet = new LinkedHashSet<>();
        blockedNodeMap = new HashMap<>();
        this.mainPkgId = pkgNode.packageID;
        pkgNode.accept(this);
        return pkgNode;
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        analyzerPhase = AnalyzerPhase.INITIAL_ANALYSIS;
        if (pkgNode.completedPhases.contains(CompilerPhase.TAINT_ANALYZE)) {
            return;
        }
        SymbolEnv pkgEnv = symTable.pkgEnvMap.get(pkgNode.symbol);
        SymbolEnv prevPkgEnv = this.currPkgEnv;
        this.currPkgEnv = pkgEnv;
        this.env = pkgEnv;

        pkgNode.topLevelNodes.forEach(e -> {
            ((BLangNode) e).accept(this);
            this.blockedNode = null;
        });

        analyzerPhase = AnalyzerPhase.BLOCKED_NODE_ANALYSIS;
        resolveBlockedInvokable();

        this.currPkgEnv = prevPkgEnv;
        pkgNode.completedPhases.add(CompilerPhase.TAINT_ANALYZE);
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
        compUnit.topLevelNodes.forEach(e -> ((BLangNode) e).accept(this));
    }

    @Override
    public void visit(BLangTypeDefinition typeDefinition) {
        /* ignore */
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = symTable.pkgEnvMap.get(pkgSymbol);
        if (pkgEnv == null) {
            return;
        }
        this.env = pkgEnv;
        pkgEnv.node.accept(this);
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        xmlnsNode.namespaceURI.accept(this);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);
        if (funcNode.attachedOuterFunction) {
            // Clear taint table of the interface deceleration when, declaration is found.
            funcNode.symbol.taintTable = null;
        }
        if (CompilerUtils.isMainFunction(funcNode)) {
            visitEntryPoint(funcNode, funcEnv);
            // Following statements are used only when main method is called from a different function (test execution).
            if (funcNode.symbol.taintTable != null) {
                // Since main method has no return values, set the all untainted entry to empty, denoting that all
                // untainted case is not invalid for the an invocation.
                funcNode.symbol.taintTable.put(ALL_UNTAINTED_TABLE_ENTRY_INDEX,
                        new TaintRecord(new ArrayList<>(), null));
                // It is valid to have a case where first argument of main is tainted. Hence manually adding such
                // scenario.
                funcNode.symbol.taintTable.put(0, new TaintRecord(new ArrayList<>(), null));
            }
        } else {
            visitInvokable(funcNode, funcEnv);
        }
    }

    @Override
    public void visit(BLangService serviceNode) {
        BSymbol serviceSymbol = serviceNode.symbol;
        SymbolEnv serviceEnv = SymbolEnv.createPkgLevelSymbolEnv(serviceNode, serviceSymbol.scope, env);
        serviceNode.vars.forEach(var -> analyzeNode(var, serviceEnv));
        analyzeNode(serviceNode.initFunction, serviceEnv);
        serviceNode.resources.forEach(resource -> analyzeNode(resource, serviceEnv));
    }

    @Override
    public void visit(BLangResource resourceNode) {
        BSymbol resourceSymbol = resourceNode.symbol;
        SymbolEnv resourceEnv = SymbolEnv.createResourceActionSymbolEnv(resourceNode, resourceSymbol.scope, env);
        visitEntryPoint(resourceNode, resourceEnv);
    }

    @Override
    public void visit(BLangConnector connectorNode) {
        BSymbol connectorSymbol = connectorNode.symbol;
        SymbolEnv connectorEnv = SymbolEnv.createConnectorEnv(connectorNode, connectorSymbol.scope, env);
        attachTaintTableBasedOnAnnotations(connectorNode);
        connectorNode.varDefs.forEach(var -> var.accept(this));
        analyzeNode(connectorNode.initFunction, connectorEnv);
        analyzeNode(connectorNode.initAction, connectorEnv);
        connectorNode.actions.forEach(action -> analyzeNode(action, connectorEnv));
    }

    @Override
    public void visit(BLangAction actionNode) {
        BSymbol actionSymbol = actionNode.symbol;
        SymbolEnv actionEnv = SymbolEnv.createResourceActionSymbolEnv(actionNode, actionSymbol.scope, env);
        visitInvokable(actionNode, actionEnv);
    }

    @Override
    public void visit(BLangStruct structNode) {
        BSymbol structSymbol = structNode.symbol;
        SymbolEnv structEnv = SymbolEnv.createPkgLevelSymbolEnv(structNode, structSymbol.scope, env);
        structNode.fields.forEach(field -> analyzeNode(field, structEnv));
    }

    @Override
    public void visit(BLangObject objectNode) {
        BSymbol objectSymbol = objectNode.symbol;
        SymbolEnv objectEnv = SymbolEnv.createPkgLevelSymbolEnv(objectNode, objectSymbol.scope, env);
        objectNode.fields.forEach(field -> analyzeNode(field, objectEnv));
        analyzeNode(objectNode.initFunction, objectEnv);
        objectNode.functions.forEach(f -> analyzeNode(f, objectEnv));
    }

    @Override
    public void visit(BLangRecord recordNode) {
        BSymbol objectSymbol = recordNode.symbol;
        SymbolEnv objectEnv = SymbolEnv.createPkgLevelSymbolEnv(recordNode, objectSymbol.scope, env);
        recordNode.fields.forEach(field -> analyzeNode(field, objectEnv));
        analyzeNode(recordNode.initFunction, objectEnv);
    }

    @Override
    public void visit(BLangEnum enumNode) {
        enumNode.symbol.tainted = false;
    }

    @Override
    public void visit(BLangEnum.BLangEnumerator enumeratorNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangVariable varNode) {
        int ownerSymTag = env.scope.owner.tag;
        if (varNode.expr != null) {
            SymbolEnv varInitEnv = SymbolEnv.createVarInitEnv(varNode, env, varNode.symbol);
            // If the variable is a package/service/connector level variable, we don't need to check types.
            // It will we done during the init-function of the respective construct is visited.
            if ((ownerSymTag & SymTag.PACKAGE) != SymTag.PACKAGE && (ownerSymTag & SymTag.SERVICE) != SymTag.SERVICE
                    && (ownerSymTag & SymTag.CONNECTOR) != SymTag.CONNECTOR) {
                analyzeNode(varNode.expr, varInitEnv);
                setTaintedStatus(varNode, getObservedTaintedStatus());
            }
        }
    }

    @Override
    public void visit(BLangWorker workerNode) {
        currWorkerIdentifier = workerNode.name;
        SymbolEnv workerEnv = SymbolEnv.createWorkerEnv(workerNode, this.env);
        analyzeNode(workerNode.body, workerEnv);
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
    public void visit(BLangAnnotAttribute annotationAttribute) {
        /* ignore */
    }

    @Override
    public void visit(BLangAnnotationAttachment annAttachmentNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangAnnotAttachmentAttributeValue annotAttributeValue) {
        /* ignore */
    }

    @Override
    public void visit(BLangAnnotAttachmentAttribute annotAttachmentAttribute) {
        /* ignore */
    }

    @Override
    public void visit(BLangTransformer transformerNode) {
        SymbolEnv transformerEnv = SymbolEnv.createTransformerEnv(transformerNode, transformerNode.symbol.scope, env);
        visitInvokable(transformerNode, transformerEnv);
    }

    @Override
    public void visit(BLangDocumentationAttribute docAttribute) {
        /* ignore */
    }

    @Override
    public void visit(BLangDocumentation doc) {
        /* ignore */
    }

    @Override
    public void visit(BLangDeprecatedNode deprecatedNode) {
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
    public void visit(BLangVariableDef varDefNode) {
        varDefNode.var.accept(this);
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        assignNode.expr.accept(this);
        BLangExpression varRefExpr = assignNode.varRef;
        boolean varTaintedStatus = getObservedTaintedStatus();
        visitAssignment(varRefExpr, varTaintedStatus, assignNode.pos);
    }

    @Override
    public void visit(BLangPostIncrement postIncrement) {
        BLangExpression varRefExpr = postIncrement.varRef;
        varRefExpr.accept(this);
        boolean varTaintedStatus = getObservedTaintedStatus();
        visitAssignment(varRefExpr, varTaintedStatus, postIncrement.pos);
    }

    @Override
    public void visit(BLangCompoundAssignment compoundAssignment) {
        compoundAssignment.varRef.accept(this);
        boolean varRefTaintedStatus = getObservedTaintedStatus();
        compoundAssignment.expr.accept(this);
        boolean exprTaintedStatus = getObservedTaintedStatus();
        boolean combinedTaintedStatus = varRefTaintedStatus || exprTaintedStatus;
        visitAssignment(compoundAssignment.varRef, combinedTaintedStatus, compoundAssignment.pos);
    }

    private void visitAssignment(BLangExpression varRefExpr, boolean varTaintedStatus, DiagnosticPos pos) {
        // Generate error if a global variable has been assigned with a tainted value.
        if (varTaintedStatus && varRefExpr instanceof BLangVariableReference) {
            BLangVariableReference varRef = (BLangVariableReference) varRefExpr;
            if (varRef.symbol != null && varRef.symbol.owner != null) {
                if (varRef.symbol.owner instanceof BPackageSymbol || SymbolKind.SERVICE.equals(varRef.symbol.owner.kind)
                        || SymbolKind.CONNECTOR.equals(varRef.symbol.owner.kind)) {
                    addTaintError(pos, varRef.symbol.name.value,
                            DiagnosticCode.TAINTED_VALUE_PASSED_TO_GLOBAL_VARIABLE);
                    return;
                }
            }
        }
        // TODO: Re-evaluating the full data-set (array) when a change occur.
        if (varRefExpr instanceof BLangIndexBasedAccess) {
            nonOverridingAnalysis = true;
            updatedVarRefTaintedState((BLangIndexBasedAccess) varRefExpr, varTaintedStatus);
            nonOverridingAnalysis = false;
        } else if (varRefExpr instanceof BLangFieldBasedAccess) {
            BLangFieldBasedAccess fieldBasedAccessExpr = (BLangFieldBasedAccess) varRefExpr;
            // Propagate tainted status to fields, when field symbols are present (Example: struct).
            if (fieldBasedAccessExpr.symbol != null) {
                setTaintedStatus(fieldBasedAccessExpr, varTaintedStatus);
            }
            nonOverridingAnalysis = true;
            updatedVarRefTaintedState(fieldBasedAccessExpr, varTaintedStatus);
            nonOverridingAnalysis = false;
        } else {
            BLangVariableReference varRef = (BLangVariableReference) varRefExpr;
            setTaintedStatus(varRef, varTaintedStatus);
        }
    }

    private void updatedVarRefTaintedState(BLangVariableReference varRef, boolean taintedState) {
        if (varRef instanceof BLangSimpleVarRef) {
            setTaintedStatus(varRef, taintedState);
        } else if (varRef instanceof BLangIndexBasedAccess) {
            BLangIndexBasedAccess indexBasedAccessExpr = (BLangIndexBasedAccess) varRef;
            updatedVarRefTaintedState(indexBasedAccessExpr.expr, taintedState);
        } else if (varRef instanceof BLangFieldBasedAccess) {
            BLangFieldBasedAccess fieldBasedAccessExpr = (BLangFieldBasedAccess) varRef;
            updatedVarRefTaintedState(fieldBasedAccessExpr.expr, taintedState);
        }
    }

    @Override
    public void visit(BLangBind bindNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangAbort abortNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangDone abortNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangRetry retryNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangNext nextNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangBreak breakNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangReturn returnNode) {
        returnNode.expr.accept(this);
        if (taintedStatus != null && taintedStatus) {
            this.returnTaintedStatus = true;
        }
        taintedStatus = this.returnTaintedStatus;
    }

    @Override
    public void visit(BLangThrow throwNode) {
        /* ignore */
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
        ifNode.body.accept(this);
        nonOverridingAnalysis = true;
        if (ifNode.elseStmt != null) {
            ifNode.elseStmt.accept(this);
        }
        nonOverridingAnalysis = false;
    }

    @Override
    public void visit(BLangMatch matchStmt) {
        matchStmt.expr.accept(this);
        boolean observedTainedStatus = getObservedTaintedStatus();
        matchStmt.patternClauses.forEach(clause -> {
            if (clause.variable.symbol != null) {
                clause.variable.symbol.tainted = observedTainedStatus;
            }
            clause.body.accept(this);
        });
    }

    @Override
    public void visit(BLangMatch.BLangMatchStmtPatternClause patternClauseNode) {
        /* ignore */
    }

    @Override
    public void visit(BLangForeach foreach) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(foreach.body, env);
        // Propagate the tainted status of collection to foreach variables.
        foreach.collection.accept(this);
        if (getObservedTaintedStatus()) {
            foreach.varRefs
                    .forEach(varRef -> setTaintedStatus((BLangVariableReference) varRef, getObservedTaintedStatus()));
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
        nonOverridingAnalysis = true;
        if (transactionNode.onRetryBody != null) {
            transactionNode.onRetryBody.accept(this);
        }
        nonOverridingAnalysis = false;
    }

    @Override
    public void visit(BLangTryCatchFinally tryNode) {
        tryNode.tryBody.accept(this);
        nonOverridingAnalysis = true;
        tryNode.catchBlocks.forEach(c -> c.accept(this));
        if (tryNode.finallyBody != null) {
            tryNode.finallyBody.accept(this);
        }
        nonOverridingAnalysis = false;
    }

    @Override
    public void visit(BLangTupleDestructure stmt) {
        stmt.expr.accept(this);
        // Propagate tainted status of each variable separately (when multi returns are used).
        for (int varIndex = 0; varIndex < stmt.varRefs.size(); varIndex++) {
            BLangExpression varRefExpr = stmt.varRefs.get(varIndex);
            visitAssignment(varRefExpr, taintedStatus, stmt.pos);
        }
    }

    @Override
    public void visit(BLangCatch catchNode) {
        SymbolEnv catchBlockEnv = SymbolEnv.createBlockEnv(catchNode.body, env);
        analyzeNode(catchNode.body, catchBlockEnv);
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        analyzeWorkers(forkJoin.workers);
        if (currForkIdentifier != null) {
            Boolean taintedStatus = workerInteractionTaintedStatusMap.get(currForkIdentifier);
            if (taintedStatus != null) {
                setTaintedStatus(forkJoin.joinResultVar, taintedStatus);
            }
        }
        nonOverridingAnalysis = true;
        if (forkJoin.joinedBody != null) {
            forkJoin.joinedBody.accept(this);
        }
        if (forkJoin.timeoutBody != null) {
            forkJoin.timeoutBody.accept(this);
        }
        nonOverridingAnalysis = false;
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
        /* ignore */
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        if (workerSendNode.isForkJoinSend) {
            currForkIdentifier = workerSendNode.workerIdentifier;
        }
        workerSendNode.expr.accept(this);
        Boolean taintedStatus = workerInteractionTaintedStatusMap.get(workerSendNode.workerIdentifier);
        if (taintedStatus == null) {
            workerInteractionTaintedStatusMap.put(workerSendNode.workerIdentifier, getObservedTaintedStatus());
        } else if (getObservedTaintedStatus()) {
            workerInteractionTaintedStatusMap.put(workerSendNode.workerIdentifier, true);
        }
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        Boolean taintedStatus = workerInteractionTaintedStatusMap.get(currWorkerIdentifier);
        if (taintedStatus == null) {
            blockedOnWorkerInteraction = true;
            stopAnalysis = true;
        } else {
            visitAssignment(workerReceiveNode.expr, getObservedTaintedStatus(), workerReceiveNode.pos);
        }
    }

    // Expressions

    @Override
    public void visit(BLangLiteral literalExpr) {
        this.taintedStatus = false;
    }

    @Override
    public void visit(BLangArrayLiteral arrayLiteral) {
        if (arrayLiteral.exprs.size() == 0) {
            // Empty arrays are untainted.
            this.taintedStatus = false;
        } else {
            for (BLangExpression expr : arrayLiteral.exprs) {
                expr.accept(this);
                if (getObservedTaintedStatus()) {
                    break;
                }
            }
        }
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        boolean isTainted = false;
        for (BLangRecordLiteral.BLangRecordKeyValue keyValuePair : recordLiteral.keyValuePairs) {
            keyValuePair.valueExpr.accept(this);
            // Update field symbols with tainted status of the individual field (Example: struct).
            if (keyValuePair.key.fieldSymbol != null) {
                if (!nonOverridingAnalysis) {
                    keyValuePair.key.fieldSymbol.tainted = getObservedTaintedStatus();
                } else if (!keyValuePair.key.fieldSymbol.tainted) {
                    keyValuePair.key.fieldSymbol.tainted = getObservedTaintedStatus();
                }
            }
            // Used to update the variable this literal is getting assigned to.
            if (getObservedTaintedStatus()) {
                isTainted = true;
            }
        }
        this.taintedStatus = isTainted;
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        if (varRefExpr.symbol == null) {
            Name varName = names.fromIdNode(varRefExpr.variableName);
            if (varName != Names.IGNORE) {
                if (varRefExpr.pkgSymbol.tag == SymTag.XMLNS) {
                    this.taintedStatus = varRefExpr.pkgSymbol.tainted;
                    return;
                }
            }
            this.taintedStatus = false;
        } else {
            this.taintedStatus  = varRefExpr.symbol.tainted;
        }
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        BType varRefType = fieldAccessExpr.expr.type;
        switch (varRefType.tag) {
            case TypeTags.STRUCT:
                fieldAccessExpr.expr.accept(this);
                break;
            case TypeTags.MAP:
                fieldAccessExpr.expr.accept(this);
                break;
            case TypeTags.JSON:
                fieldAccessExpr.expr.accept(this);
                break;
            case TypeTags.ENUM:
                this.taintedStatus = false;
                break;
        }
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        indexAccessExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        if (invocationExpr.functionPointerInvocation) {
            // Skip function pointers and assume returns of function pointer executions are untainted.
            // TODO: Resolving function pointers / lambda expressions and perform analysis.
            taintedStatus = false;
        } else if (invocationExpr.iterableOperationInvocation) {
            invocationExpr.expr.accept(this);
            if (getObservedTaintedStatus()) {
                if (invocationExpr.argExprs != null) {
                    invocationExpr.argExprs.forEach(argExpr -> {
                        // If argument of iterable operation is a lambda expression, propagate the tainted status
                        // to function parameters and validate function body.
                        if (argExpr instanceof BLangLambdaFunction) {
                            analyzeLambdaExpressions(invocationExpr, argExpr);
                        }
                    });
                }
            }
        } else {
            BInvokableSymbol invokableSymbol = (BInvokableSymbol) invocationExpr.symbol;
            if (invokableSymbol.taintTable == null) {
                addToBlockedList(invocationExpr);
            } else {
                analyzeInvocation(invocationExpr);
            }
        }
    }

    @Override
    public void visit(BLangTypeInit typeInit) {
        boolean typeTaintedStatus = false;
        for (BLangExpression expr : typeInit.argsExpr) {
            expr.accept(this);
            // TODO: Improve: If one value ot type init is tainted, the complete type is tainted.
            if (getObservedTaintedStatus()) {
                typeTaintedStatus = getObservedTaintedStatus();
            }
            typeInit.objectInitInvocation.accept(this);
        }
        this.taintedStatus = typeTaintedStatus;
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation actionInvocationExpr) {
        /* ignore */
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        ternaryExpr.thenExpr.accept(this);
        boolean thenTaintedCheckResult = getObservedTaintedStatus();
        nonOverridingAnalysis = true;
        ternaryExpr.elseExpr.accept(this);
        nonOverridingAnalysis = false;
        boolean elseTaintedCheckResult = getObservedTaintedStatus();
        this.taintedStatus = thenTaintedCheckResult || elseTaintedCheckResult;
    }

    @Override
    public void visit(BLangAwaitExpr awaitExpr) {
        awaitExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        binaryExpr.lhsExpr.accept(this);
        boolean lhsTaintedCheckResult = getObservedTaintedStatus();
        binaryExpr.rhsExpr.accept(this);
        boolean rhsTaintedCheckResult = getObservedTaintedStatus();
        this.taintedStatus = lhsTaintedCheckResult || rhsTaintedCheckResult;
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        //TODO
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        bracedOrTupleExpr.expressions.forEach(expression -> expression.accept(this));
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        switch (unaryExpr.operator) {
            case LENGTHOF:
                this.taintedStatus = false;
                break;
            case UNTAINT:
                this.taintedStatus = false;
                break;
            default:
                unaryExpr.expr.accept(this);
                break;
        }
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {
        this.taintedStatus = false;
    }

    @Override
    public void visit(BLangTypeCastExpr castExpr) {
        // Result of the cast is tainted if value being casted is tainted.
        castExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        // Result of the conversion is tainted if value being converted is tainted.
        conversionExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        this.taintedStatus = false;
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        xmlAttribute.name.accept(this);
        boolean attrNameTaintedStatus = getObservedTaintedStatus();
        xmlAttribute.value.accept(this);
        boolean attrValueTaintedStatus = getObservedTaintedStatus();
        this.taintedStatus = attrNameTaintedStatus || attrValueTaintedStatus;
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        // Visit in-line namespace declarations
        boolean inLineNamespaceTainted = false;
        for (BLangXMLAttribute attribute : xmlElementLiteral.attributes) {
            if (attribute.name.getKind() == NodeKind.XML_QNAME && ((BLangXMLQName) attribute.name).prefix.value
                    .equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                attribute.accept(this);
                attribute.symbol.tainted = getObservedTaintedStatus();
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
                attribute.symbol.tainted = getObservedTaintedStatus();
                if (attribute.symbol.tainted) {
                    attributesTainted = true;
                }
            }
        }

        // Visit the tag names
        xmlElementLiteral.startTagName.accept(this);
        boolean startTagTaintedStatus = getObservedTaintedStatus();
        boolean endTagTaintedStatus = false;
        if (xmlElementLiteral.endTagName != null) {
            xmlElementLiteral.endTagName.accept(this);
            endTagTaintedStatus = getObservedTaintedStatus();
        }
        boolean tagNamesTainted = startTagTaintedStatus || endTagTaintedStatus;

        // Visit the children
        boolean childrenTainted = false;
        for (BLangExpression expr : xmlElementLiteral.children) {
            expr.accept(this);
            if (getObservedTaintedStatus()) {
                childrenTainted = true;
            }
        }

        this.taintedStatus = inLineNamespaceTainted || attributesTainted || tagNamesTainted || childrenTainted;
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
        if (!getObservedTaintedStatus()) {
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
        /* ignore */
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        xmlAttributeAccessExpr.expr.accept(this);
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        this.taintedStatus = false;
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
    public void visit(BLangFieldBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {
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
    public void visit(BLangInvocation.BLangTransformerInvocation iExpr) {
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
    public void visit(BLangFieldBasedAccess.BLangEnumeratorAccessExpr enumeratorAccessExpr) {
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

    // Private

    private <T extends BLangNode, U extends SymbolEnv> void analyzeNode(T t, U u) {
        SymbolEnv prevEnv = this.env;
        this.env = u;
        t.accept(this);
        this.env = prevEnv;
    }

    /**
     * If any one of the given expressions are tainted, the final result will be tainted.
     *
     * @param exprs List of expressions to analyze.
     */
    private void analyzeExprList(List<BLangExpression> exprs) {
        for (BLangExpression expr : exprs) {
            expr.accept(this);
            if (getObservedTaintedStatus()) {
                break;
            }
        }
    }

    private boolean hasAnnotation(BLangVariable variable, String expectedAnnotation) {
        return hasAnnotation(variable.annAttachments, expectedAnnotation);
    }

    private boolean hasAnnotation(List<BLangAnnotationAttachment> annotationAttachmentList, String expectedAnnotation) {
        return annotationAttachmentList.stream().anyMatch(annotation ->
                annotation.annotationName.value.equals(expectedAnnotation));
    }


    /**
     * Set tainted status of the variable. When non-overriding analysis is in progress, this will not override "tainted"
     * status with "untainted" status. As an example, the "else" section of a "if-else" block, cannot change a value
     * marked "tainted" by the "if" block.
     *
     * @param varNode       Variable node to be updated.
     * @param taintedStatus Tainted status.
     */
    private void setTaintedStatus(BLangVariable varNode, boolean taintedStatus) {
        if (!nonOverridingAnalysis) {
            varNode.symbol.tainted = taintedStatus;
        } else if (!varNode.symbol.tainted) {
            varNode.symbol.tainted = taintedStatus;
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
    private void setTaintedStatus(BLangVariableReference varNode, boolean taintedStatus) {
        if (!nonOverridingAnalysis) {
            varNode.symbol.tainted = taintedStatus;
        } else if (!varNode.symbol.tainted) {
            varNode.symbol.tainted = taintedStatus;
        }
    }

    private boolean getObservedTaintedStatus() {
        if (this.taintedStatus == null) {
            return false;
        } else {
            return this.taintedStatus;
        }
    }

    // Private methods related to invokable node analysis and taint-table generation.

    private void visitEntryPoint(BLangInvokableNode invNode, SymbolEnv funcEnv) {
        // Entry point input parameters are all tainted, since they contain user controlled data.
        // If any value has been marked "sensitive" generate an error.
        if (isEntryPointParamsInvalid(invNode.requiredParams)) {
            return;
        }
        List<BLangVariable> defaultableParamsVarList = new ArrayList<>();
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
        if (isBlocked) {
            return;
        } else {
            // Display errors only if scan of was fully complete, so that errors will not get duplicated.
            taintErrorSet.forEach(error -> this.dlog.error(error.pos, error.diagnosticCode, error.paramName));
            taintErrorSet.clear();
        }
        invNode.symbol.taintTable = new HashMap<>();
    }

    private boolean isEntryPointParamsInvalid(List<BLangVariable> params) {
        if (params != null) {
            for (BLangVariable param : params) {
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

    private void visitInvokable(BLangInvokableNode invNode, SymbolEnv symbolEnv) {
        if (invNode.symbol.taintTable == null) {
            if (Symbols.isNative(invNode.symbol)) {
                attachTaintTableBasedOnAnnotations(invNode);
                return;
            }
            Map<Integer, TaintRecord> taintTable = new HashMap<>();
            this.returnTaintedStatus = false;
            // Check the tainted status of return values when no parameter is tainted.
            analyzeAllParamsUntaintedReturnTaintedStatus(taintTable, invNode, symbolEnv);
            boolean isBlocked = processBlockedNode(invNode);
            if (isBlocked) {
                return;
            }
            int requiredParamCount = invNode.requiredParams.size();
            int defaultableParamCount = invNode.defaultableParams.size();
            int totalParamCount = requiredParamCount + defaultableParamCount + (invNode.restParam == null ? 0 : 1);
            for (int paramIndex = 0; paramIndex < totalParamCount; paramIndex++) {
                BLangVariable param = getParam(invNode, paramIndex, requiredParamCount, defaultableParamCount);
                // If parameter is sensitive, it is invalid to have a case where tainted status of parameter is true.
                if (hasAnnotation(param, ANNOTATION_SENSITIVE)) {
                    continue;
                }
                this.returnTaintedStatus = false;
                // Set each parameter "tainted", then analyze the body to observe the outcome of the function.
                analyzeReturnTaintedStatus(taintTable, invNode, symbolEnv, paramIndex, requiredParamCount,
                        defaultableParamCount);
            }
            invNode.symbol.taintTable = taintTable;
        }
    }

    private void visitInvokable(BLangFunction invNode, SymbolEnv symbolEnv) {
        if (invNode.symbol.taintTable == null) {
            if (Symbols.isNative(invNode.symbol) || invNode.interfaceFunction) {
                attachTaintTableBasedOnAnnotations(invNode);
                return;
            }
            Map<Integer, TaintRecord> taintTable = new HashMap<>();
            this.returnTaintedStatus = false;
            // Check the tainted status of return values when no parameter is tainted.
            analyzeAllParamsUntaintedReturnTaintedStatus(taintTable, invNode, symbolEnv);
            boolean isBlocked = processBlockedNode(invNode);
            if (isBlocked) {
                return;
            }
            int requiredParamCount = invNode.requiredParams.size();
            int defaultableParamCount = invNode.defaultableParams.size();
            int totalParamCount = requiredParamCount + defaultableParamCount + (invNode.restParam == null ? 0 : 1);
            for (int paramIndex = 0; paramIndex < totalParamCount; paramIndex++) {
                BLangVariable param = getParam(invNode, paramIndex, requiredParamCount, defaultableParamCount);
                // If parameter is sensitive, it is invalid to have a case where tainted status of parameter is true.
                if (hasAnnotation(param, ANNOTATION_SENSITIVE)) {
                    continue;
                }
                this.returnTaintedStatus = false;
                // Set each parameter "tainted", then analyze the body to observe the outcome of the function.
                analyzeReturnTaintedStatus(taintTable, invNode, symbolEnv, paramIndex, requiredParamCount,
                        defaultableParamCount);
            }
            invNode.symbol.taintTable = taintTable;
        }
    }

    private void analyzeAllParamsUntaintedReturnTaintedStatus(Map<Integer, TaintRecord> taintTable,
                                                              BLangInvokableNode invokableNode, SymbolEnv symbolEnv) {
        analyzeReturnTaintedStatus(taintTable, invokableNode, symbolEnv, ALL_UNTAINTED_TABLE_ENTRY_INDEX, 0, 0);
    }

    private void analyzeReturnTaintedStatus(Map<Integer, TaintRecord> taintTable, BLangInvokableNode invokableNode,
                                            SymbolEnv symbolEnv, int paramIndex, int requiredParamCount,
                                            int defaultableParamCount) {
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
                invokableNode.restParam.symbol.tainted = true;
            }
        }
        analyzeReturnTaintedStatus(invokableNode, symbolEnv);
        if (taintErrorSet.size() > 0) {
            // When invocation returns an error (due to passing a tainted argument to a sensitive parameter) add current
            // error to the table for future reference.
            taintTable.put(paramIndex, new TaintRecord(null, new ArrayList<>(taintErrorSet)));
            taintErrorSet.clear();
        } else if (this.blockedNode == null) {
            List<Boolean> returnTaintedStatusList = new ArrayList<>();
            if (invokableNode.returnTypeNode.type != symTable.nilType) {
                updatedReturnTaintedStatusBasedOnAnnotations(invokableNode.returnTypeAnnAttachments);
                returnTaintedStatusList.add(RETURN_TAINTED_STATUS_COLUMN_INDEX, this.returnTaintedStatus);
            }
            taintTable.put(paramIndex, new TaintRecord(returnTaintedStatusList, null));
        }
    }

    private void resetTaintedStatusOfVariables(List<BLangVariable> params) {
        if (params != null) {
            params.forEach(param -> param.symbol.tainted = false);
        }
    }

    private void resetTaintedStatusOfVariableDef(List<BLangVariableDef> params) {
        if (params != null) {
            List<BLangVariable> defaultableParamsVarList = new ArrayList<>();
            params.forEach(defaultableParam -> defaultableParamsVarList.add(defaultableParam.var));
            resetTaintedStatusOfVariables(defaultableParamsVarList);
        }
    }

    private void analyzeReturnTaintedStatus(BLangInvokableNode invokableNode, SymbolEnv symbolEnv) {
        invokableNode.endpoints.forEach(endpoint -> endpoint.accept(this));
        if (invokableNode.workers.isEmpty()) {
            analyzeNode(invokableNode.body, symbolEnv);
        } else {
            analyzeWorkers(invokableNode.workers);
        }
        if (stopAnalysis) {
            stopAnalysis = false;
        }
    }

    private void analyzeWorkers (List<BLangWorker> workers) {
        workerInteractionTaintedStatusMap = new HashMap<>();
        boolean doScan = true;
        while (doScan) {
            doScan = false;
            for (BLangWorker worker : workers) {
                blockedOnWorkerInteraction = false;
                worker.endpoints.forEach(endpoint -> endpoint.accept(this));
                worker.accept(this);
                if (this.blockedNode != null || taintErrorSet.size() > 0) {
                    return;
                } else if (blockedOnWorkerInteraction) {
                    doScan = true;
                    stopAnalysis = false;
                } else if (stopAnalysis) {
                    return;
                }
            }
        }
    }

    private void attachTaintTableBasedOnAnnotations(BLangInvokableNode invokableNode) {
        if (invokableNode.symbol.taintTable == null) {
            // Extract tainted status of the function by looking at annotations added to returns.
            List<Boolean> retParamsTaintedStatus = new ArrayList<>();
            retParamsTaintedStatus.add(hasAnnotation(invokableNode.returnTypeAnnAttachments, ANNOTATION_TAINTED));

            // Append taint table with tainted status when no parameter is tainted.
            Map<Integer, TaintRecord> taintTable = new HashMap<>();
            taintTable.put(ALL_UNTAINTED_TABLE_ENTRY_INDEX, new TaintRecord(retParamsTaintedStatus, null));
            int requiredParamCount = invokableNode.requiredParams.size();
            int defaultableParamCount = invokableNode.defaultableParams.size();
            int totalParamCount =
                    requiredParamCount + defaultableParamCount + (invokableNode.restParam == null ? 0 : 1);
            if (totalParamCount > 0) {
                // Append taint table with tainted status when each parameter is tainted.
                for (int paramIndex = 0; paramIndex < totalParamCount; paramIndex++) {
                    BLangVariable param = getParam(invokableNode, paramIndex, requiredParamCount,
                            defaultableParamCount);
                    // If parameter is sensitive, test for this parameter being tainted is invalid.
                    if (hasAnnotation(param, ANNOTATION_SENSITIVE)) {
                        continue;
                    }
                    taintTable.put(paramIndex, new TaintRecord(retParamsTaintedStatus, null));
                }
            }
            invokableNode.symbol.taintTable = taintTable;
        }
    }

    private void attachTaintTableBasedOnAnnotations(BLangConnector connectorNode) {
        if (connectorNode.symbol.taintTable == null) {
            List<Boolean> retParamsTaintedStatus = new ArrayList<>();
            Map<Integer, TaintRecord> taintTable = new HashMap<>();
            taintTable.put(ALL_UNTAINTED_TABLE_ENTRY_INDEX, new TaintRecord(retParamsTaintedStatus, null));
            if (connectorNode.params.size() > 0) {
                // Append taint table with tainted status when each parameter is tainted.
                for (int paramIndex = 0; paramIndex < connectorNode.params.size(); paramIndex++) {
                    BLangVariable param = connectorNode.params.get(paramIndex);
                    // If parameter is sensitive, test for this parameter being tainted is invalid.
                    if (hasAnnotation(param, ANNOTATION_SENSITIVE)) {
                        continue;
                    }
                    taintTable.put(paramIndex, new TaintRecord(retParamsTaintedStatus, null));
                }
            }
            connectorNode.symbol.taintTable = taintTable;
        }
    }

    private void updatedReturnTaintedStatusBasedOnAnnotations(List<BLangAnnotationAttachment> retParamsAnnotations) {
        if (hasAnnotation(retParamsAnnotations, ANNOTATION_UNTAINTED)) {
            this.returnTaintedStatus = false;
        }
        if (hasAnnotation(retParamsAnnotations, ANNOTATION_TAINTED)) {
            this.returnTaintedStatus = true;
        }
    }

    private boolean processBlockedNode(BLangInvokableNode invokableNode) {
        boolean isBlocked = false;
        if (this.blockedNode != null) {
            // Add the function being blocked into the blocked node list for later processing.
            this.blockedNode.invokableNode = invokableNode;
            if (analyzerPhase == AnalyzerPhase.INITIAL_ANALYSIS) {
                List<BlockedNode> blockedNodeList = blockedNodeMap.get(blockedNode.blockingNode);
                if (blockedNodeList == null) {
                    blockedNodeList = new ArrayList<>();
                }
                blockedNodeList.add(blockedNode);
                blockedNodeMap.put(blockedNode.blockingNode, blockedNodeList);
            }
            this.blockedNode = null;
            // Discard any error generated if invokable was found to be blocked. This will avoid duplicates when
            // blocked invokable is re-examined.
            taintErrorSet.clear();
            isBlocked = true;
        }
        return isBlocked;
    }

    private void analyzeLambdaExpressions(BLangInvocation invocationExpr, BLangExpression argExpr) {
        BLangFunction function = ((BLangLambdaFunction) argExpr).function;
        if (function.symbol.taintTable == null) {
            addToBlockedList(invocationExpr);
        } else {
            int requiredParamCount = function.requiredParams.size();
            int defaultableParamCount = function.defaultableParams.size();
            int totalParamCount = requiredParamCount + defaultableParamCount + (function.restParam == null ? 0 : 1);
            Map<Integer, TaintRecord> taintTable = function.symbol.taintTable;
            for (int paramIndex = 0; paramIndex < totalParamCount; paramIndex++) {
                TaintRecord taintRecord = taintTable.get(paramIndex);
                BLangVariable param = getParam(function, paramIndex, requiredParamCount, defaultableParamCount);
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
        taintErrorSet.add(taintError);
        if (!entryPointAnalysis) {
            stopAnalysis = true;
        }
    }

    private void addTaintError(List<TaintRecord.TaintError> taintErrors) {
        taintErrorSet.addAll(taintErrors);
        if (!entryPointAnalysis) {
            stopAnalysis = true;
        }
    }

    private void addToBlockedList(BLangInvocation invocationExpr) {
        BlockingNode blockingNode = new BlockingNode(invocationExpr.symbol.pkgID, invocationExpr.symbol.name);
        this.blockedNode = new BlockedNode(this.currPkgEnv, invocationExpr.pos, blockingNode);
        stopAnalysis = true;
        this.taintedStatus = false;
    }

    // Private methods relevant to invocation analysis.

    private void analyzeInvocation(BLangInvocation invocationExpr) {
        BInvokableSymbol invokableSymbol = (BInvokableSymbol) invocationExpr.symbol;
        Map<Integer, TaintRecord> taintTable = invokableSymbol.taintTable;
        boolean returnTaintedStatus = false;
        TaintRecord allParamsUntaintedRecord = taintTable.get(ALL_UNTAINTED_TABLE_ENTRY_INDEX);
        if (allParamsUntaintedRecord.taintError != null && allParamsUntaintedRecord.taintError.size() > 0) {
            // This can occur when there is a error regardless of tainted status of parameters.
            // Example: Tainted value returned by function is passed to another functions's sensitive parameter.
            addTaintError(allParamsUntaintedRecord.taintError);
        } else {
            if (taintTable.get(ALL_UNTAINTED_TABLE_ENTRY_INDEX).retParamTaintedStatus.size() > 0) {
                returnTaintedStatus = taintTable.get(ALL_UNTAINTED_TABLE_ENTRY_INDEX).retParamTaintedStatus
                        .get(RETURN_TAINTED_STATUS_COLUMN_INDEX);
            }
        }
        if (invocationExpr.argExprs != null) {
            int requiredParamCount = invokableSymbol.params.size();
            int defaultableParamCount = invokableSymbol.defaultableParams.size();
            for (int argIndex = 0; argIndex < invocationExpr.requiredArgs.size(); argIndex++) {
                BLangExpression argExpr = invocationExpr.requiredArgs.get(argIndex);
                if (analyzeArgument(argIndex, invokableSymbol, invocationExpr, argExpr)) {
                    returnTaintedStatus = true;
                }
                if (stopAnalysis) {
                    break;
                }
            }
            for (int argIndex = 0; argIndex < invocationExpr.namedArgs.size(); argIndex++) {
                BLangExpression argExpr = invocationExpr.namedArgs.get(argIndex);
                if (argExpr instanceof BLangNamedArgsExpression) {
                    String currentNamedArgExprName = ((BLangNamedArgsExpression) argExpr).name.value;
                    // Pick the index of this defaultable parameter in the invokable definition.
                    int paramIndex = 0;
                    for (int defaultableParamIndex = 0; defaultableParamIndex < invokableSymbol.defaultableParams
                            .size(); defaultableParamIndex++) {
                        BVarSymbol defaultableParam = invokableSymbol.defaultableParams.get(defaultableParamIndex);
                        if (defaultableParam.name.value.equals(currentNamedArgExprName)) {
                            paramIndex = requiredParamCount + defaultableParamIndex;
                            break;
                        }
                    }
                    if (analyzeArgument(paramIndex, invokableSymbol, invocationExpr, argExpr)) {
                        returnTaintedStatus = true;
                    }
                    if (stopAnalysis) {
                        break;
                    }
                }
            }
            for (int argIndex = 0; argIndex < invocationExpr.restArgs.size(); argIndex++) {
                BLangExpression argExpr = invocationExpr.restArgs.get(argIndex);
                // Pick the index of the rest parameter in the invokable definition.
                int paramIndex = requiredParamCount + defaultableParamCount;
                if (analyzeArgument(paramIndex, invokableSymbol, invocationExpr, argExpr)) {
                    returnTaintedStatus = true;
                }
                if (stopAnalysis) {
                    break;
                }
            }
        }
        if (invocationExpr.expr != null) {
            // When an invocation like stringValue.trim() happens, if stringValue is tainted, the result will
            // also be tainted.
            //TODO: TaintedIf annotation, so that it's possible to define what can taint or untaint the return.
            invocationExpr.expr.accept(this);
            if (getObservedTaintedStatus()) {
                returnTaintedStatus = true;
            }
        }
        taintedStatus = returnTaintedStatus;
    }

    private boolean analyzeArgument(int paramIndex, BInvokableSymbol invokableSymbol, BLangInvocation invocationExpr,
                                 BLangExpression argExpr) {
        Map<Integer, TaintRecord> taintTable = invokableSymbol.taintTable;
        argExpr.accept(this);
        // If current argument is tainted, look-up the taint-table for the record of
        // return-tainted-status when the given argument is in tainted state.
        if (getObservedTaintedStatus()) {
            TaintRecord taintRecord = taintTable.get(paramIndex);
            int requiredParamCount = invokableSymbol.params.size();
            int defaultableParamCount = invokableSymbol.defaultableParams.size();
            BVarSymbol paramSymbol = getParamSymbol(invokableSymbol, paramIndex, requiredParamCount,
                    defaultableParamCount);
            if (taintRecord == null) {
                // This is when current parameter is "sensitive". Therefore, providing a tainted
                // value to a sensitive parameter is invalid and should return a compiler error.
                DiagnosticPos argPos = argExpr.pos != null ? argExpr.pos : invocationExpr.pos;
                addTaintError(argPos, paramSymbol.name.value,
                        DiagnosticCode.TAINTED_VALUE_PASSED_TO_SENSITIVE_PARAMETER);
            } else if (taintRecord.taintError != null && taintRecord.taintError.size() > 0) {
                // This is when current parameter is derived to be sensitive. The error already generated
                // during taint-table generation will be used.
                if (!mainPkgId.name.getValue().startsWith(Names.BUILTIN_PACKAGE.value) &&
                        invocationExpr.symbol.pkgID.name.getValue().equals(mainPkgId.name.getValue())) {
                    addTaintError(taintRecord.taintError);
                } else {
                    DiagnosticPos argPos = argExpr.pos != null ? argExpr.pos : invocationExpr.pos;
                    addTaintError(argPos, paramSymbol.name.value,
                            DiagnosticCode.TAINTED_VALUE_PASSED_TO_SENSITIVE_PARAMETER);
                }
            } else {
                return taintRecord.retParamTaintedStatus.size() > 0
                        && taintRecord.retParamTaintedStatus.get(RETURN_TAINTED_STATUS_COLUMN_INDEX);
            }
        }
        return false;
    }

    private void resolveBlockedInvokable() {
        Map<BlockingNode, List<BlockedNode>> sortedBlockedNodeMap = sortBlockingNodesByBlockedNodeCount(blockedNodeMap);
        while (sortedBlockedNodeMap.size() > 0) {
            // Map used to collect the remaining blocked nodes, that could not be resolved in the current iteration.
            Map<BlockingNode, List<BlockedNode>> remainingBlockedNodeMap = new LinkedHashMap<>();
            int analyzedBlockedNodeCount = 0;
            int remainingBlockedNodeCount = 0;
            for (BlockingNode blockingNode : sortedBlockedNodeMap.keySet()) {
                List<BlockedNode> blockedNodeList = sortedBlockedNodeMap.get(blockingNode);
                List<BlockedNode> remainingBlockedNodeList = new ArrayList<>();
                // Revisit blocked nodes and attempt to generate the table.
                for (BlockedNode blockedNode : blockedNodeList) {
                    this.env = blockedNode.pkgSymbol;
                    blockedNode.invokableNode.accept(this);
                    analyzedBlockedNodeCount++;
                    if (blockedNode.invokableNode.symbol.taintTable == null) {
                        remainingBlockedNodeList.add(blockedNode);
                        remainingBlockedNodeCount++;
                    }
                }
                // If there are blocked nodes remaining, add them to the new map.
                if (remainingBlockedNodeList.size() > 0) {
                    remainingBlockedNodeMap.put(blockingNode, remainingBlockedNodeList);
                } else {
                    remainingBlockedNodeMap.remove(blockingNode);
                }
            }
            // If list is not moving, there is a recursion. Derive the tainted status of all the blocked
            // functions by using annotations and if annotations are not present generate error.
            if (remainingBlockedNodeCount != 0 && analyzedBlockedNodeCount == remainingBlockedNodeCount) {
                boolean partialResolutionFound = false;
                // Check each stagnated function and derive the tainted / untainted status of the return value based on
                // annotations.
                Map<BlockingNode, List<BlockedNode>> remainingBlockedNodesAfterAnnotationCheck = new LinkedHashMap<>();
                for (BlockingNode blockingNode : remainingBlockedNodeMap.keySet()) {
                    List<BlockedNode> blockedNodeList = remainingBlockedNodeMap.get(blockingNode);
                    List<BlockedNode> remainingBlockedNodeList = new ArrayList<>();
                    for (BlockedNode blockedNode : blockedNodeList) {
                        boolean retParamIsAnnotated = hasAnnotation(blockedNode.invokableNode.returnTypeAnnAttachments,
                                ANNOTATION_TAINTED) || hasAnnotation(blockedNode.invokableNode.returnTypeAnnAttachments,
                                ANNOTATION_UNTAINTED);
                        if (retParamIsAnnotated) {
                            attachTaintTableBasedOnAnnotations(blockedNode.invokableNode);
                            partialResolutionFound = true;
                        } else {
                            remainingBlockedNodeList.add(blockedNode);
                        }
                    }
                    if (remainingBlockedNodeList.size() > 0) {
                        remainingBlockedNodesAfterAnnotationCheck.put(blockingNode, remainingBlockedNodeList);
                    }
                }
                remainingBlockedNodeMap = remainingBlockedNodesAfterAnnotationCheck;
                if (!partialResolutionFound) {
                    // If returns of remaining blocked nodes are not annotated, generate errors, since
                    // taint analyzer cannot accurately finish the analysis unless required annotations were
                    // added.
                    for (BlockingNode blockingNode : remainingBlockedNodeMap.keySet()) {
                        List<BlockedNode> blockedNodeList = remainingBlockedNodeMap.get(blockingNode);
                        for (BlockedNode blockedNode : blockedNodeList) {
                            this.dlog.error(blockedNode.blockedPos,
                                    DiagnosticCode.UNABLE_TO_PERFORM_TAINT_CHECKING_WITH_RECURSION,
                                    blockedNode.invokableNode.name.value, blockingNode.name.value);
                        }
                    }
                    break;
                }
            }
            sortedBlockedNodeMap = remainingBlockedNodeMap;
        }
    }

    private BLangVariable getParam(BLangInvokableNode invNode, int paramIndex, int requiredParamCount,
                                   int defaultableParamCount) {
        BLangVariable param;
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

    /**
     * This will re-order the blocked node list, so that the node blocking most number of other nodes comes to the top.
     * This is to increase the efficiency of conflict resolution phase.
     *
     * @param unsortMap Unsorted map of blocked nodes, indexed by blocking node.
     * @return Blocking node map sorted based on the blocked node count.
     */
    private static Map<BlockingNode, List<BlockedNode>> sortBlockingNodesByBlockedNodeCount(
            Map<BlockingNode, List<BlockedNode>> unsortMap) {
        List<Map.Entry<BlockingNode, List<BlockedNode>>> blockedNodeList = new LinkedList<>(unsortMap.entrySet());
        blockedNodeList.sort((o1, o2) -> Integer.compare(o2.getValue().size(), o1.getValue().size()));
        Map<BlockingNode, List<BlockedNode>> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<BlockingNode, List<BlockedNode>> entry : blockedNodeList) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
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
        DiagnosticPos blockedPos;
        BlockingNode blockingNode;

        BlockedNode(SymbolEnv pkgSymbol, DiagnosticPos blockedPos, BlockingNode blockingNode) {
            this.pkgSymbol = pkgSymbol;
            this.blockedPos = blockedPos;
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
}

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
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.cyclefind.GlobalVariableRefAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangExprFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
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
import org.wso2.ballerinalang.compiler.tree.BLangTableKeySpecifier;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangJoinClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLimitClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangOnConflictClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAccessExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIntRangeExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangActionInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLetExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression.BLangMatchExprPatternClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStringTemplateLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTableMultiKeyExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTrapExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTupleVarRef;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangForkJoin;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStaticBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStructuredBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
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
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLogHelper;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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

    private final SymbolResolver symResolver;
    private final Names names;
    private SymbolEnv env;
    private SymbolTable symTable;
    private BLangDiagnosticLogHelper dlog;
    private Map<BSymbol, InitStatus> uninitializedVars;
    private Map<BSymbol, Set<BSymbol>> globalNodeDependsOn;
    private Map<BSymbol, Set<BSymbol>> functionToDependency;
    private boolean flowTerminated = false;

    private static final CompilerContext.Key<DataflowAnalyzer> DATAFLOW_ANALYZER_KEY = new CompilerContext.Key<>();
    private Deque<BSymbol> currDependentSymbol;
    private final GlobalVariableRefAnalyzer globalVariableRefAnalyzer;

    private DataflowAnalyzer(CompilerContext context) {
        context.put(DATAFLOW_ANALYZER_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.dlog = BLangDiagnosticLogHelper.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.names = Names.getInstance(context);
        this.currDependentSymbol = new ArrayDeque<>();
        this.globalVariableRefAnalyzer = GlobalVariableRefAnalyzer.getInstance(context);

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
        this.uninitializedVars = new LinkedHashMap<>();
        this.globalNodeDependsOn = new LinkedHashMap<>();
        this.functionToDependency = new HashMap<>();
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgNode.symbol);
        analyzeNode(pkgNode, pkgEnv);
        return pkgNode;
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.DATAFLOW_ANALYZE)) {
            return;
        }

        // Rearrange the top level nodes so that global variables come on top
        List<TopLevelNode> sortedListOfNodes = new ArrayList<>(pkgNode.globalVars);
        addModuleInitToSortedNodeList(pkgNode, sortedListOfNodes);
        addNodesToSortedNodeList(pkgNode, sortedListOfNodes);

        for (TopLevelNode topLevelNode : sortedListOfNodes) {
            if (isModuleInitFunction((BLangNode) topLevelNode)) {
                analyzeModuleInitFunc((BLangFunction) topLevelNode);
            } else {
                analyzeNode((BLangNode) topLevelNode, env);
            }
        }
        checkForUninitializedGlobalVars(pkgNode.globalVars);
        pkgNode.getTestablePkgs().forEach(testablePackage -> visit((BLangPackage) testablePackage));
        this.globalVariableRefAnalyzer.analyzeAndReOrder(pkgNode, this.globalNodeDependsOn);
        this.globalVariableRefAnalyzer.populateFunctionDependencies(this.functionToDependency);
        checkUnusedImports(pkgNode.imports);
        pkgNode.completedPhases.add(CompilerPhase.DATAFLOW_ANALYZE);
    }

    private void addModuleInitToSortedNodeList(BLangPackage pkgNode, List<TopLevelNode> sortedListOfNodes) {
        for (TopLevelNode node : pkgNode.topLevelNodes) {
            if (isModuleInitFunction((BLangNode) node)) {
                sortedListOfNodes.add(node);
                break;
            }
        }
    }

    private void addNodesToSortedNodeList(BLangPackage pkgNode, List<TopLevelNode> sortedListOfNodes) {
        pkgNode.topLevelNodes.forEach(topLevelNode -> {
            if (!sortedListOfNodes.contains(topLevelNode)) {
                sortedListOfNodes.add(topLevelNode);
            }
        });
    }

    private boolean isModuleInitFunction(BLangNode node) {
        return node.getKind() == NodeKind.FUNCTION &&
                Names.USER_DEFINED_INIT_SUFFIX.value.equals(((BLangFunction) node).name.value);
    }

    private void analyzeModuleInitFunc(BLangFunction funcNode) {
        this.currDependentSymbol.push(funcNode.symbol);
        SymbolEnv moduleInitFuncEnv = SymbolEnv.createModuleInitFunctionEnv(funcNode, funcNode.symbol.scope, env);
        for (BLangAnnotationAttachment bLangAnnotationAttachment : funcNode.annAttachments) {
            analyzeNode(bLangAnnotationAttachment.expr, env);
        }
        analyzeNode(funcNode.body, moduleInitFuncEnv);
        this.currDependentSymbol.pop();
    }

    private void checkForUninitializedGlobalVars(List<BLangSimpleVariable> globalVars) {
        for (BLangSimpleVariable globalVar : globalVars) {
            if (this.uninitializedVars.containsKey(globalVar.symbol)) {
                this.dlog.error(globalVar.pos, DiagnosticCode.UNINITIALIZED_VARIABLE, globalVar.name);
            }
        }
    }

    @Override
    public void visit(BLangFunction funcNode) {
        this.currDependentSymbol.push(funcNode.symbol);
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);
        funcNode.annAttachments.forEach(bLangAnnotationAttachment -> analyzeNode(bLangAnnotationAttachment.expr, env));
        funcNode.requiredParams.forEach(param -> analyzeNode(param, funcEnv));
        analyzeNode(funcNode.restParam, funcEnv);
        analyzeBranch(funcNode.body, funcEnv);
        this.currDependentSymbol.pop();
    }

    @Override
    public void visit(BLangBlockFunctionBody body) {
        SymbolEnv bodyEnv = SymbolEnv.createFuncBodyEnv(body, env);
        bodyEnv.isModuleInit = env.isModuleInit;
        for (BLangStatement statement : body.stmts) {
            analyzeNode(statement, bodyEnv);
        }
    }

    @Override
    public void visit(BLangExprFunctionBody body) {
        SymbolEnv bodyEnv = SymbolEnv.createFuncBodyEnv(body, env);
        analyzeNode(body.expr, bodyEnv);
    }

    @Override
    public void visit(BLangExternalFunctionBody body) {
        // do nothing
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, env);
        blockNode.stmts.forEach(statement -> analyzeNode(statement, blockEnv));
    }

    @Override
    public void visit(BLangLetExpression letExpression) {
        for (BLangLetVariable letVarDeclaration : letExpression.letVarDeclarations) {
            analyzeNode((BLangNode) letVarDeclaration.definitionNode, letExpression.env);
        }
        analyzeNode(letExpression.expr, letExpression.env);
    }

    @Override
    public void visit(BLangCompilationUnit compUnit) {
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
    }

    @Override
    public void visit(BLangService service) {
        this.currDependentSymbol.push(service.serviceTypeDefinition.symbol);
        for (BLangExpression attachedExpr : service.attachedExprs) {
            analyzeNode(attachedExpr, env);
        }

        service.annAttachments.forEach(bLangAnnotationAttachment -> analyzeNode(bLangAnnotationAttachment.expr, env));
        service.resourceFunctions.forEach(function -> analyzeNode(function, env));
        this.currDependentSymbol.pop();
    }

    @Override
    public void visit(BLangResource resource) {
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
        analyzeNode(variable.typeNode, env);
        if (variable.symbol == null) {
            if (variable.expr != null) {
                analyzeNode(variable.expr, env);
            }
            return;
        }

        this.currDependentSymbol.push(variable.symbol);
        try {
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
        } finally {
            this.currDependentSymbol.pop();
        }
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
        terminateFlow();
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
        BranchResult lastPatternResult = null;
        for (BLangMatch.BLangMatchBindingPatternClause patternClause : match.patternClauses) {
            if (patternClause.isLastPattern) {
                lastPatternResult = analyzeBranch(patternClause, env);
            } else {
                BranchResult result = analyzeBranch(patternClause, env);
                // If the flow was terminated within the block, then that branch should not be considered for
                // analyzing the data-flow for the downstream code.
                if (result.flowTerminated) {
                    continue;
                }
                uninitVars = mergeUninitializedVars(uninitVars, result.uninitializedVars);
            }
        }

        if (lastPatternResult != null) {
            // only if last pattern is present, uninitializedVars should be updated
            uninitVars = mergeUninitializedVars(uninitVars, lastPatternResult.uninitializedVars);
            this.uninitializedVars = uninitVars;
            return;
        }
        uninitVars = mergeUninitializedVars(new HashMap<>(), this.uninitializedVars);
        this.uninitializedVars = uninitVars;
    }

    @Override
    public void visit(BLangForeach foreach) {
        analyzeNode(foreach.collection, env);
        analyzeNode(foreach.body, env);
    }

    @Override
    public void visit(BLangQueryAction queryAction) {
        for (BLangNode clause : queryAction.getQueryClauses()) {
            analyzeNode(clause, env);
        }
    }

    @Override
    public void visit(BLangWhile whileNode) {
        Map<BSymbol, InitStatus> prevUninitializedVars = this.uninitializedVars;
        analyzeNode(whileNode.expr, env);
        analyzeNode(whileNode.body, env);
        for (BSymbol symbol : prevUninitializedVars.keySet()) {
            if (!this.uninitializedVars.containsKey(symbol)) {
                this.uninitializedVars.put(symbol, InitStatus.PARTIAL_INIT);
            }
        }
    }

    @Override
    public void visit(BLangLock lockNode) {
        analyzeNode(lockNode.body, this.env);
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        analyzeNode(transactionNode.transactionBody, env);
        analyzeNode(transactionNode.onRetryBody, env);
        analyzeNode(transactionNode.committedBody, env);
        analyzeNode(transactionNode.abortedBody, env);

        // marks the injected import as used
        Name transactionPkgName = names.fromString(Names.DOT.value + Names.TRANSACTION_PACKAGE.value);
        Name compUnitName = names.fromString(transactionNode.pos.getSource().getCompilationUnitName());
        this.symResolver.resolvePrefixSymbol(env, transactionPkgName, compUnitName);
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
         /* ignore */
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        analyzeNode(workerSendNode.expr, env);
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        analyzeNode(syncSendExpr.expr, env);
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
       // todo
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        listConstructorExpr.exprs.forEach(expr -> analyzeNode(expr, env));
    }

    @Override
    public void visit(BLangTableConstructorExpr tableConstructorExpr) {
        tableConstructorExpr.recordLiteralList.forEach(expr -> analyzeNode(expr, env));
        checkForDuplicateKeys(tableConstructorExpr);
    }

    private void checkForDuplicateKeys(BLangTableConstructorExpr tableConstructorExpr) {
        Set<Integer> keyHashSet = new HashSet<>();
        List<String> fieldNames = getFieldNames(tableConstructorExpr);
        if (!fieldNames.isEmpty()) {
            for (BLangRecordLiteral literal : tableConstructorExpr.recordLiteralList) {
                List<BLangExpression> keyArray = createKeyArray(literal, fieldNames);
                int hashInt = generateHash(keyArray);
                if (!keyHashSet.add(hashInt)) {
                    String fields = String.join(", ", fieldNames);
                    String values = keyArray.stream().map(Object::toString).collect(Collectors.joining(", "));
                    dlog.error(literal.pos, DiagnosticCode.DUPLICATE_KEY_IN_TABLE_LITERAL, fields, values);
                }
            }
        }
    }

    private int generateHash(List<BLangExpression> keyArray) {
        int result = 0;
        for (BLangExpression expr : keyArray) {
            result = 31 * result + hash(expr);
        }
        return result;
    }

    public Integer hash(Node node) {
        int result = 0;

        if (node.getKind() == NodeKind.RECORD_LITERAL_EXPR) {
            BLangRecordLiteral recordLiteral = (BLangRecordLiteral) node;
            for (RecordLiteralNode.RecordField entry : recordLiteral.fields) {
                result = 31 * result + hash(entry);
            }
        } else if (node.getKind() == NodeKind.RECORD_LITERAL_KEY_VALUE) {
            BLangRecordLiteral.BLangRecordKeyValueField field = (BLangRecordLiteral.BLangRecordKeyValueField) node;
            result = 31 * result + hash(field.key.expr) + hash(field.valueExpr);
        } else if (node.getKind() == NodeKind.ARRAY_LITERAL_EXPR) {
            BLangListConstructorExpr.BLangArrayLiteral arrayLiteral =
                    (BLangListConstructorExpr.BLangArrayLiteral) node;
            for (BLangExpression expr : arrayLiteral.exprs) {
                result = 31 * result + hash(expr);
            }
        } else if (node.getKind() == NodeKind.LITERAL | node.getKind() == NodeKind.NUMERIC_LITERAL) {
            BLangLiteral literal = (BLangLiteral) node;
            result = Objects.hash(literal.value);
        } else if (node.getKind() == NodeKind.XML_TEXT_LITERAL) {
            BLangXMLTextLiteral literal = (BLangXMLTextLiteral) node;
            result = 31 * result + hash(literal.concatExpr);
            for (BLangExpression expr : literal.textFragments) {
                result = result * 31 + hash(expr);
            }
        } else if (node.getKind() == NodeKind.XML_ATTRIBUTE) {
            BLangXMLAttribute attribute = (BLangXMLAttribute) node;
            result = 31 * result + hash(attribute.name) + hash(attribute.value);
        } else if (node.getKind() == NodeKind.XML_QNAME) {
            BLangXMLQName xmlqName = (BLangXMLQName) node;
            result = 31 * result + hash(xmlqName.localname) + hash(xmlqName.prefix);
        } else if (node.getKind() == NodeKind.XML_COMMENT_LITERAL) {
            BLangXMLCommentLiteral literal = (BLangXMLCommentLiteral) node;
            result = 31 * result + hash(literal.concatExpr);
            for (BLangExpression expr : literal.textFragments) {
                result = result * 31 + hash(expr);
            }
        } else if (node.getKind() == NodeKind.XML_ELEMENT_LITERAL) {
            BLangXMLElementLiteral literal = (BLangXMLElementLiteral) node;
            result = 31 * result + hash(literal.startTagName) + hash(literal.endTagName);
            for (BLangExpression expr : literal.attributes) {
                result = 31 * result + hash(expr);
            }
            for (BLangExpression expr : literal.children) {
                result = 31 * result + hash(expr);
            }
        } else if (node.getKind() == NodeKind.XML_QUOTED_STRING) {
            BLangXMLQuotedString literal = (BLangXMLQuotedString) node;
            result = 31 * result + hash(literal.concatExpr);
            for (BLangExpression expr : literal.textFragments) {
                result = result * 31 + hash(expr);
            }
        } else if (node.getKind() == NodeKind.XMLNS) {
            BLangXMLNS xmlns = (BLangXMLNS) node;
            result = result * 31 + hash(xmlns.prefix) + hash(xmlns.namespaceURI);
        } else if (node.getKind() == NodeKind.XML_PI_LITERAL) {
            BLangXMLProcInsLiteral literal = (BLangXMLProcInsLiteral) node;
            result = 31 * result + hash(literal.target) + hash(literal.dataConcatExpr);
            for (BLangExpression expr : literal.dataFragments) {
                result = result * 31 + hash(expr);
            }
        } else if (node.getKind() == NodeKind.IDENTIFIER) {
            BLangIdentifier identifier = (BLangIdentifier) node;
            result = identifier.value.hashCode();
        } else if (node.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BLangSimpleVarRef simpleVarRef = (BLangSimpleVarRef) node;
            result = simpleVarRef.variableName.hashCode();
        } else {
            dlog.error(((BLangExpression) node).pos, DiagnosticCode.EXPRESSION_IS_NOT_A_CONSTANT_EXPRESSION);
        }
        return result;
    }

    private List<BLangExpression> createKeyArray(BLangRecordLiteral literal, List<String> fieldNames) {
        //fieldNames have to be literals in table constructor's record literal list
        Map<String, BLangExpression> fieldMap =
                literal.fields.stream().map(recordField -> (BLangRecordLiteral.BLangRecordKeyValueField) recordField)
                        .map(d -> new SimpleEntry<>(d.key.expr.toString(), d.valueExpr)).
                        collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
        return fieldNames.stream().map(fieldMap::get).collect(Collectors.toList());
    }

    private List<String> getFieldNames(BLangTableConstructorExpr constructorExpr) {
        List<String> fieldNames = ((BTableType) constructorExpr.type).fieldNameList;

        if (fieldNames != null) {
            return fieldNames;
        }

        if (constructorExpr.tableKeySpecifier != null &&
                !constructorExpr.tableKeySpecifier.fieldNameIdentifierList.isEmpty()) {
            BLangTableKeySpecifier tableKeySpecifier = constructorExpr.tableKeySpecifier;
            fieldNames = tableKeySpecifier.fieldNameIdentifierList.stream().map(fieldName -> fieldName.value)
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }

        return fieldNames;
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        for (RecordLiteralNode.RecordField field : recordLiteral.fields) {
            if (field.isKeyValueField()) {
                BLangRecordLiteral.BLangRecordKeyValueField keyValuePair =
                        (BLangRecordLiteral.BLangRecordKeyValueField) field;
                if (keyValuePair.key.computedKey) {
                    analyzeNode(keyValuePair.key.expr, env);
                }
                analyzeNode(keyValuePair.valueExpr, env);
            } else if (field.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                analyzeNode((BLangRecordLiteral.BLangRecordVarNameField) field, env);
            } else {
                analyzeNode(((BLangRecordLiteral.BLangRecordSpreadOperatorField) field).expr, env);
            }
        }
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        checkVarRef(varRefExpr.symbol, varRefExpr.pos);
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        if (!fieldAccessExpr.lhsVar && isObjectMemberAccessWithSelf(fieldAccessExpr)) {
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
    public void visit(BLangTableMultiKeyExpr tableMultiKeyExpr) {
        tableMultiKeyExpr.multiKeyIndexExprs.forEach(value -> analyzeNode(value, env));
    }

    @Override
    public void visit(BLangXMLElementAccess xmlElementAccess) {
        analyzeNode(xmlElementAccess.expr, env);
    }

    @Override
    public void visit(BLangXMLNavigationAccess xmlNavigation) {
        analyzeNode(xmlNavigation.expr, env);
        if (xmlNavigation.childIndex == null) {
            analyzeNode(xmlNavigation.childIndex, env);
        }
    }

    @Override
    public void visit(BLangInvocation invocationExpr) {
        analyzeNode(invocationExpr.expr, env);
        if (!isGlobalVarsInitialized(invocationExpr.pos)) {
            return;
        }
        if (!isFieldsInitializedForSelfArgument(invocationExpr)) {
            return;
        }
        if (!isFieldsInitializedForSelfInvocation(invocationExpr.requiredArgs, invocationExpr.pos)) {
            return;
        }
        if (!isFieldsInitializedForSelfInvocation(invocationExpr.restArgs, invocationExpr.pos)) {
            return;
        }

        invocationExpr.requiredArgs.forEach(expr -> analyzeNode(expr, env));
        invocationExpr.restArgs.forEach(expr -> analyzeNode(expr, env));
        BSymbol owner = this.env.scope.owner;
        if (owner.kind == SymbolKind.FUNCTION) {
            BInvokableSymbol invokableOwnerSymbol = (BInvokableSymbol) owner;
            Name name = names.fromIdNode(invocationExpr.name);
            // Todo: we need to handle function pointer referring global variable, passed into a function.
            // i.e 'foo' is a function pointer pointing to a function referring a global variable G1, then we pass
            // that pointer into 'bar', now 'bar' may have a dependency on G1.

            // Todo: test lambdas and function arguments

            BSymbol dependsOnFunctionSym = symResolver.lookupSymbolInMainSpace(this.env, name);
            if (symTable.notFoundSymbol != dependsOnFunctionSym) {
                addDependency(invokableOwnerSymbol, dependsOnFunctionSym);
            }
        } else if (invocationExpr.symbol != null && invocationExpr.symbol.kind == SymbolKind.FUNCTION) {
            BInvokableSymbol invokableProviderSymbol = (BInvokableSymbol) invocationExpr.symbol;
            BSymbol curDependent = this.currDependentSymbol.peek();
            if (curDependent != null && isGlobalVarSymbol(curDependent)) {
                addDependency(curDependent, invokableProviderSymbol);
            }
        }
    }

    @Override
    public void visit(BLangActionInvocation actionInvocation) {
        this.visit((BLangInvocation) actionInvocation);
    }

    @Override
    public void visit(BLangQueryExpr queryExpr) {
        for (BLangNode clause : queryExpr.getQueryClauses()) {
            analyzeNode(clause, env);
        }
    }

    @Override
    public void visit(BLangFromClause fromClause) {
        analyzeNode(fromClause.collection, env);
    }

    @Override
    public void visit(BLangJoinClause joinClause) {
        analyzeNode(joinClause.collection, env);
    }

    @Override
    public void visit(BLangLetClause letClause) {
        for (BLangLetVariable letVariable : letClause.letVarDeclarations) {
            analyzeNode((BLangNode) letVariable.definitionNode, env);
        }
    }

    @Override
    public void visit(BLangWhereClause whereClause) {
        analyzeNode(whereClause.expression, env);
    }

    @Override
    public void visit(BLangOnClause onClause) {
        analyzeNode(onClause.expression, env);
    }

    @Override
    public void visit(BLangSelectClause selectClause) {
        analyzeNode(selectClause.expression, env);
    }

    @Override
    public void visit(BLangOnConflictClause onConflictClause) {
        analyzeNode(onConflictClause.expression, env);
    }

    @Override
    public void visit(BLangLimitClause limitClause) {
        analyzeNode(limitClause.expression, env);
    }

    @Override
    public void visit(BLangDoClause doClause) {
        analyzeNode(doClause.body, env);
    }

    private boolean isFieldsInitializedForSelfArgument(BLangInvocation invocationExpr) {

        if (invocationExpr.expr == null || !isSelfKeyWordExpr(invocationExpr.expr)) {
            return true;
        }
        StringBuilder uninitializedFields =
                getUninitializedFieldsForSelfKeyword((BObjectType) ((BLangSimpleVarRef)
                        invocationExpr.expr).symbol.type);
        if (uninitializedFields.length() != 0) {
            this.dlog.error(invocationExpr.pos, DiagnosticCode.CONTAINS_UNINITIALIZED_FIELDS,
                    uninitializedFields.toString());
            return false;
        }
        return true;
    }

    private boolean isFieldsInitializedForSelfInvocation(List<BLangExpression> argExpressions, DiagnosticPos pos) {

        for (BLangExpression expr : argExpressions) {
            if (isSelfKeyWordExpr(expr)) {
                StringBuilder uninitializedFields =
                        getUninitializedFieldsForSelfKeyword((BObjectType) ((BLangSimpleVarRef) expr).symbol.type);
                if (uninitializedFields.length() != 0) {
                    this.dlog.error(pos, DiagnosticCode.CONTAINS_UNINITIALIZED_FIELDS,
                            uninitializedFields.toString());
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isGlobalVarsInitialized(DiagnosticPos pos) {
        if (env.isModuleInit) {
            boolean isFirstUninitializedField = true;
            StringBuilder uninitializedFields = new StringBuilder();
            for (BSymbol symbol : this.uninitializedVars.keySet()) {
                if (isFirstUninitializedField) {
                    uninitializedFields = new StringBuilder(symbol.getName().value);
                    isFirstUninitializedField = false;
                } else {
                    uninitializedFields.append(", ").append(symbol.getName().value);
                }
            }
            if (uninitializedFields.length() != 0) {
                this.dlog.error(pos, DiagnosticCode.CONTAINS_UNINITIALIZED_VARIABLES,
                        uninitializedFields.toString());
                return false;
            }
        }
        return true;
    }

    private boolean isSelfKeyWordExpr(BLangExpression expr) {

        return expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF &&
                Names.SELF.value.equals(((BLangSimpleVarRef) expr).getVariableName().getValue());
    }

    private StringBuilder getUninitializedFieldsForSelfKeyword(BObjectType objType) {

        boolean isFirstUninitializedField = true;
        StringBuilder uninitializedFields = new StringBuilder();
        for (BField field : objType.fields.values()) {
            if (this.uninitializedVars.containsKey(field.symbol)) {
                if (isFirstUninitializedField) {
                    uninitializedFields = new StringBuilder(field.symbol.getName().value);
                    isFirstUninitializedField = false;
                } else {
                    uninitializedFields.append(", ").append(field.symbol.getName().value);
                }
            }
        }
        return uninitializedFields;
    }

    private boolean isGlobalVarSymbol(BSymbol symbol) {
        if (symbol == null) {
            return false;
        } else if (symbol.owner == null) {
            return false;
        } else if (symbol.owner.tag != SymTag.PACKAGE) {
            return false;
        }

        return isVariableOrConstant(symbol);
    }

    private boolean isVariableOrConstant(BSymbol symbol) {
        if (symbol == null) {
            return false;
        }
        return ((symbol.tag & SymTag.VARIABLE) == SymTag.VARIABLE) ||
                ((symbol.tag & SymTag.CONSTANT) == SymTag.CONSTANT);
    }

    /**
     * Register dependent symbol to the provider symbol.
     * Let global int a = b, a depend on b.
     * Let func foo() { returns b + 1; }, where b is a global var, then foo depends on b.
     *
     * @param dependent dependent.
     * @param provider object which provides a value.
     */
    private void addDependency(BSymbol dependent, BSymbol provider) {
        if (provider == null || dependent == null || dependent.pkgID != provider.pkgID) {
            return;
        }
        Set<BSymbol> providers = globalNodeDependsOn.computeIfAbsent(dependent, s -> new LinkedHashSet<>());
        providers.add(provider);

        // Store the dependencies of functions seperately for lock optimization in later stage.
        addFunctionToGlobalVarDependency(dependent, provider);
    }

    private void addFunctionToGlobalVarDependency(BSymbol dependent, BSymbol provider) {
        if (dependent.kind != SymbolKind.FUNCTION) {
            return;
        }
        if (isVariableOrConstant(provider) && !isGlobalVarSymbol(provider)) {
            return;
        }

        Set<BSymbol> providers = this.functionToDependency.computeIfAbsent(dependent, s -> new HashSet<>());
        providers.add(provider);
    }

    @Override
    public void visit(BLangTypeInit typeInitExpr) {
        typeInitExpr.argsExpr.forEach(argExpr -> analyzeNode(argExpr, env));
        if (this.currDependentSymbol.peek() != null) {
            addDependency(this.currDependentSymbol.peek(), typeInitExpr.type.tsymbol);
        }
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        analyzeNode(ternaryExpr.expr, env);
    }

    @Override
    public void visit(BLangWaitExpr waitExpr) {
        analyzeNode(waitExpr.getExpression(), env);
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        // todo
    }

    @Override
    public void visit(BLangWaitForAllExpr waitForAllExpr) {
        waitForAllExpr.keyValuePairs.forEach(keyValue -> {
            BLangExpression expr = keyValue.valueExpr != null ? keyValue.valueExpr : keyValue.keyExpr;
            analyzeNode(expr, env);
        });
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
    public void visit(BLangGroupExpr groupExpr) {
        analyzeNode(groupExpr.expression, env);
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
        Map<BSymbol, InitStatus> prevUninitializedVars = this.uninitializedVars;

        BLangFunction funcNode = bLangLambdaFunction.function;
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);

        // Get a snapshot of the current uninitialized vars before visiting the node.
        // This is done so that the original set of uninitialized vars will not be
        // updated/marked as initialized.
        this.uninitializedVars = copyUninitializedVars();
        this.flowTerminated = false;

        analyzeNode(funcNode.body, funcEnv);

        // Restore the original set of uninitialized vars
        this.uninitializedVars = prevUninitializedVars;
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
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        analyzeNode(bLangVarArgsExpression.expr, env);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        analyzeNode(bLangNamedArgsExpression.expr, env);
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
    public void visit(BLangCheckPanickedExpr checkPanicExpr) {
        analyzeNode(checkPanicExpr.expr, env);
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
    public void visit(BLangAbort abortNode) {
    }

    @Override
    public void visit(BLangRetry retryNode) {
    }

    @Override
    public void visit(BLangContinue continueNode) {
        terminateFlow();
    }

    @Override
    public void visit(BLangCatch catchNode) {
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        bLangArrowFunction.closureVarSymbols.forEach(closureVarSymbol -> {
            if (this.uninitializedVars.keySet().contains(closureVarSymbol.bSymbol)) {
                this.dlog.error(closureVarSymbol.diagnosticPos, DiagnosticCode.USAGE_OF_UNINITIALIZED_VARIABLE,
                        closureVarSymbol.bSymbol);
            }
        });
    }

    @Override
    public void visit(BLangValueType valueType) {
    }

    @Override
    public void visit(BLangConstant constant) {
        boolean validVariable = constant.symbol != null;
        if (validVariable) {
            this.currDependentSymbol.push(constant.symbol);
        }
        try {
            analyzeNode(constant.expr, env);
        } finally {
            if (validVariable) {
                this.currDependentSymbol.pop();
            }
        }
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        analyzeNode(arrayType.getElementType(), env);
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefType) {
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        analyzeNode(constrainedType.constraint, env);
    }

    @Override
    public void visit(BLangStreamType streamType) {
        analyzeNode(streamType.constraint, env);
        analyzeNode(streamType.error, env);
    }

    @Override
    public void visit(BLangTableTypeNode tableType) {
        analyzeNode(tableType.constraint, env);

        if (tableType.tableKeyTypeConstraint != null) {
            analyzeNode(tableType.tableKeyTypeConstraint.keyType, env);
        }
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        functionTypeNode.params.forEach(param -> analyzeNode(param.typeNode, env));
        analyzeNode(functionTypeNode.returnTypeNode, env);
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        unionTypeNode.memberTypeNodes.forEach(typeNode -> analyzeNode(typeNode, env));
    }

    @Override
    public void visit(BLangIntersectionTypeNode intersectionTypeNode) {
        for (BLangType constituentTypeNode : intersectionTypeNode.constituentTypeNodes) {
            analyzeNode(constituentTypeNode, env);
        }
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        SymbolEnv objectEnv = SymbolEnv.createTypeEnv(objectTypeNode, objectTypeNode.symbol.scope, env);
        this.currDependentSymbol.push(objectTypeNode.symbol);

        objectTypeNode.fields.forEach(field -> analyzeNode(field, objectEnv));
        objectTypeNode.referencedFields.forEach(field -> analyzeNode(field, objectEnv));

        // Visit the constructor with the same scope as the object
        if (objectTypeNode.initFunction != null) {
            if (objectTypeNode.initFunction.body == null) {
                // if the __init() function is defined as an outside function definition
                Optional<BLangFunction> outerFuncDef =
                        objectEnv.enclPkg.functions.stream()
                                .filter(f -> f.symbol.name.equals((objectTypeNode.initFunction).symbol.name))
                                .findFirst();
                outerFuncDef.ifPresent(bLangFunction -> objectTypeNode.initFunction = bLangFunction);
            }

            if (objectTypeNode.initFunction.body != null) {
                if (objectTypeNode.initFunction.body.getKind() == NodeKind.BLOCK_FUNCTION_BODY) {
                    for (BLangStatement statement :
                            ((BLangBlockFunctionBody) objectTypeNode.initFunction.body).stmts) {
                        analyzeNode(statement, objectEnv);
                    }
                } else if (objectTypeNode.initFunction.body.getKind() == NodeKind.EXPR_FUNCTION_BODY) {
                    analyzeNode(((BLangExprFunctionBody) objectTypeNode.initFunction.body).expr, objectEnv);
                }
            }
        }

        if (!Symbols.isFlagOn(objectTypeNode.symbol.flags, Flags.ABSTRACT)) {
            Stream.concat(objectTypeNode.fields.stream(), objectTypeNode.referencedFields.stream())
                .filter(field -> !Symbols.isPrivate(field.symbol))
                .forEach(field -> {
                    if (this.uninitializedVars.containsKey(field.symbol)) {
                        this.dlog.error(field.pos, DiagnosticCode.OBJECT_UNINITIALIZED_FIELD, field.name);
                    }
                });
        }

        objectTypeNode.functions.forEach(function -> analyzeNode(function, env));
        objectTypeNode.getTypeReferences().forEach(type -> analyzeNode((BLangType) type, env));
        this.currDependentSymbol.pop();
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        recordTypeNode.getTypeReferences().forEach(type -> analyzeNode((BLangType) type, env));
        recordTypeNode.fields.forEach(field -> analyzeNode(field, env));
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        finiteTypeNode.valueSpace.forEach(value -> analyzeNode(value, env));
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        tupleTypeNode.memberTypeNodes.forEach(type -> analyzeNode(type, env));
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
    public void visit(BLangTrapExpr trapExpr) {
        analyzeNode(trapExpr.expr, env);
    }

    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        if (this.currDependentSymbol.peek() != null) {
            addDependency(this.currDependentSymbol.peek(), serviceConstructorExpr.type.tsymbol);
        }

        addDependency(serviceConstructorExpr.type.tsymbol, serviceConstructorExpr.serviceNode.symbol);
        analyzeNode(serviceConstructorExpr.serviceNode, env);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        analyzeNode(typeTestExpr.expr, env);
        analyzeNode(typeTestExpr.typeNode, env);
    }

    @Override
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
    }

    @Override
    public void visit(BLangErrorType errorType) {
    }

    @Override
    public void visit(BLangRecordDestructure recordDestructure) {
        analyzeNode(recordDestructure.expr, env);
        checkAssignment(recordDestructure.varRef);
    }

    @Override
    public void visit(BLangErrorDestructure errorDestructure) {
        analyzeNode(errorDestructure.expr, env);
        checkAssignment(errorDestructure.varRef);
    }

    @Override
    public void visit(BLangTupleVarRef tupleVarRefExpr) {
        tupleVarRefExpr.expressions.forEach(expr -> analyzeNode(expr, env));
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
        varRefExpr.recordRefFields.forEach(expr -> analyzeNode(expr.variableReference, env));
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
        analyzeNode(varRefExpr.message, env);
        if (varRefExpr.cause != null) {
            analyzeNode(varRefExpr.cause, env);
        }
        for (BLangNamedArgsExpression args : varRefExpr.detail) {
            analyzeNode(args.expr, env);
        }
        analyzeNode(varRefExpr.restVar, env);
    }

    @Override
    public void visit(BLangTupleVariable bLangTupleVariable) {
        analyzeNode(bLangTupleVariable.typeNode, env);
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
        analyzeNode(bLangRecordVariable.typeNode, env);
    }

    @Override
    public void visit(BLangRecordVariableDef bLangRecordVariableDef) {
        BLangVariable var = bLangRecordVariableDef.var;
        if (var.expr == null) {
            addUninitializedVar(var);
        }
    }

    @Override
    public void visit(BLangErrorVariable bLangErrorVariable) {
        analyzeNode(bLangErrorVariable.typeNode, env);
    }

    @Override
    public void visit(BLangErrorVariableDef bLangErrorVariableDef) {
        BLangVariable var = bLangErrorVariableDef.errorVariable;
        if (var.expr == null) {
            addUninitializedVar(var);
        }
    }

    @Override
    public void visit(BLangMatchStaticBindingPatternClause bLangMatchStaticBindingPatternClause) {
        analyzeNode(bLangMatchStaticBindingPatternClause.body, env);
    }

    @Override
    public void visit(BLangMatchStructuredBindingPatternClause bLangMatchStructuredBindingPatternClause) {
        analyzeNode(bLangMatchStructuredBindingPatternClause.body, env);
    }

    private void addUninitializedVar(BLangVariable variable) {
        if (!this.uninitializedVars.containsKey(variable.symbol)) {
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
        recordGlobalVariableReferenceRelationship(symbol);

        InitStatus initStatus = this.uninitializedVars.get(symbol);
        if (initStatus == null) {
            return;
        }

        if (initStatus == InitStatus.UN_INIT) {
            this.dlog.error(pos, DiagnosticCode.USAGE_OF_UNINITIALIZED_VARIABLE, symbol.name);
            return;
        }

        this.dlog.error(pos, DiagnosticCode.PARTIALLY_INITIALIZED_VARIABLE, symbol.name);
    }

    private void recordGlobalVariableReferenceRelationship(BSymbol symbol) {
        BSymbol ownerSymbol = this.env.scope.owner;
        boolean isInPkgLevel = ownerSymbol.getKind() == SymbolKind.PACKAGE;
        // Restrict to observations made in pkg level.
        if (isInPkgLevel && isGlobalVarSymbol(symbol)) {
            BSymbol dependent = this.currDependentSymbol.peek();
            addDependency(dependent, symbol);
        } else if (ownerSymbol.kind == SymbolKind.FUNCTION && isGlobalVarSymbol(symbol)) {
            // Global variable ref from non package level.
            BInvokableSymbol invokableOwnerSymbol = (BInvokableSymbol) ownerSymbol;
            addDependency(invokableOwnerSymbol, symbol);
        } else if (ownerSymbol.kind == SymbolKind.OBJECT && isGlobalVarSymbol(symbol)) {
            // Global variable reference from a field assignment of an object or a service.
            // Or global variable reference from a __init function of an object or a service.
            addDependency(ownerSymbol, symbol);
        }
    }

    private boolean isObjectMemberAccessWithSelf(BLangAccessExpression fieldAccessExpr) {
        if (fieldAccessExpr.expr.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
            return false;
        }
        return Names.SELF.value.equals(((BLangSimpleVarRef) fieldAccessExpr.expr).variableName.value);
    }

    private void checkAssignment(BLangExpression varRef) {
        switch (varRef.getKind()) {
            case RECORD_VARIABLE_REF:
                BLangRecordVarRef recordVarRef = (BLangRecordVarRef) varRef;
                recordVarRef.recordRefFields.forEach(field -> checkAssignment(field.variableReference));
                if (recordVarRef.restParam != null) {
                    checkAssignment((BLangExpression) recordVarRef.restParam);
                }
                return;
            case TUPLE_VARIABLE_REF:
                BLangTupleVarRef tupleVarRef = (BLangTupleVarRef) varRef;
                tupleVarRef.expressions.forEach(this::checkAssignment);
                if (tupleVarRef.restParam != null) {
                    checkAssignment((BLangExpression) tupleVarRef.restParam);
                }
                return;
            case ERROR_VARIABLE_REF:
                BLangErrorVarRef errorVarRef = (BLangErrorVarRef) varRef;
                if (errorVarRef.message != null) {
                    checkAssignment(errorVarRef.message);
                }
                if (errorVarRef.cause != null) {
                    checkAssignment(errorVarRef.cause);
                }
                for (BLangNamedArgsExpression expression : errorVarRef.detail) {
                    checkAssignment(expression);
                    this.uninitializedVars.remove(((BLangVariableReference) expression.expr).symbol);
                }
                if (errorVarRef.restVar != null) {
                    checkAssignment(errorVarRef.restVar);
                }
                return;
            case INDEX_BASED_ACCESS_EXPR:
            case FIELD_BASED_ACCESS_EXPR:
                if (isObjectMemberAccessWithSelf((BLangAccessExpression) varRef)) {
                    this.uninitializedVars.remove(((BLangVariableReference) varRef).symbol);
                } else {
                    analyzeNode(((BLangAccessExpression) varRef).expr, env);
                }
                return;
            default:
                break;
        }

        if (varRef.getKind() != NodeKind.SIMPLE_VARIABLE_REF &&
                varRef.getKind() != NodeKind.XML_ATTRIBUTE_ACCESS_EXPR) {
            return;
        }

        // So global variable assignments happen in functions.
        if (varRef.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            BSymbol owner = this.env.scope.owner;
            addFunctionToGlobalVarDependency(owner, ((BLangSimpleVarRef) varRef).symbol);
        }

        this.uninitializedVars.remove(((BLangVariableReference) varRef).symbol);
    }

    private void terminateFlow() {
        this.flowTerminated = true;
    }

    private void checkUnusedImports(List<BLangImportPackage> imports) {
        for (BLangImportPackage importStmt : imports) {
            if (importStmt.symbol == null || importStmt.symbol.isUsed ||
                    Names.IGNORE.value.equals(importStmt.alias.value)) {
                continue;
            }
            dlog.error(importStmt.pos, DiagnosticCode.UNUSED_IMPORT_MODULE, importStmt.getQualifiedPackageName());
        }
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

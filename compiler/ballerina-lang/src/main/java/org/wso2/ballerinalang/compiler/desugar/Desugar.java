/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.TableColumnFlag;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.RecordVariableNode.BLangRecordVariableKeyValueNode;
import org.ballerinalang.model.tree.clauses.JoinStreamingInput;
import org.ballerinalang.model.tree.expressions.NamedArgNode;
import org.ballerinalang.model.tree.statements.BlockNode;
import org.ballerinalang.model.tree.statements.StreamingQueryStatementNode;
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.TaintAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.BLangBuiltInMethod;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.iterable.IterableContext;
import org.wso2.ballerinalang.compiler.semantics.model.iterable.IterableKind;
import org.wso2.ballerinalang.compiler.semantics.model.iterable.Operation;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BCastOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConstantSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BXMLNSSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.TaintRecord;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntermediateCollectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BRecordType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructureType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable.BLangRecordVariableKeyValue;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangWorker;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangLocalXMLNS;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS.BLangPackageXMLNS;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAccessExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral.BLangJSONArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangElvisExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangErrorConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangAttachedFunctionInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation.BLangBuiltInMethodInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression.BLangMatchExprPatternClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangChannelLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangJSONLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangMapLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangStreamLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral.BLangStructLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef.BLangRecordVarRefKeyValue;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRestArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLElementLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLProcInsLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQName;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLQuotedString;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAbort;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBreak;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStaticBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStructuredBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchTypedBindingPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangPanic;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRetry;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement.BLangStatementLink;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTransaction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.DefaultValueLiteral;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.programfile.InstructionCodes;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import static org.wso2.ballerinalang.compiler.util.Names.GEN_VAR_PREFIX;
import static org.wso2.ballerinalang.compiler.util.Names.IGNORE;

/**
 * @since 0.94
 */
public class Desugar extends BLangNodeVisitor {

    private static final CompilerContext.Key<Desugar> DESUGAR_KEY =
            new CompilerContext.Key<>();
    private static final String QUERY_TABLE_WITH_JOIN_CLAUSE = "queryTableWithJoinClause";
    private static final String QUERY_TABLE_WITHOUT_JOIN_CLAUSE = "queryTableWithoutJoinClause";
    private static final String CREATE_FOREVER = "startForever";
    private static final String BASE_64 = "base64";

    private SymbolTable symTable;
    private SymbolResolver symResolver;
    private IterableCodeDesugar iterableCodeDesugar;
    private StreamingCodeDesugar streamingCodeDesugar;
    private AnnotationDesugar annotationDesugar;
    private InMemoryTableQueryBuilder inMemoryTableQueryBuilder;
    private Types types;
    private Names names;
    private SiddhiQueryBuilder siddhiQueryBuilder;
    private ServiceDesugar serviceDesugar;

    private BLangNode result;

    private BLangStatementLink currentLink;
    private Stack<BLangWorker> workerStack = new Stack<>();

    public Stack<BLangLock> enclLocks = new Stack<>();

    private SymbolEnv env;
    private int lambdaFunctionCount = 0;
    private int recordCount = 0;

    // Safe navigation related variables
    private Stack<BLangMatch> matchStmtStack = new Stack<>();
    Stack<BLangAccessExpression> accessExprStack = new Stack<>();
    private BLangMatchTypedBindingPatternClause successPattern;
    private BLangAssignment safeNavigationAssignment;

    public static Desugar getInstance(CompilerContext context) {
        Desugar desugar = context.get(DESUGAR_KEY);
        if (desugar == null) {
            desugar = new Desugar(context);
        }

        return desugar;
    }

    private Desugar(CompilerContext context) {
        context.put(DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.iterableCodeDesugar = IterableCodeDesugar.getInstance(context);
        this.streamingCodeDesugar = StreamingCodeDesugar.getInstance(context);
        this.annotationDesugar = AnnotationDesugar.getInstance(context);
        this.inMemoryTableQueryBuilder = InMemoryTableQueryBuilder.getInstance(context);
        this.types = Types.getInstance(context);
        this.names = Names.getInstance(context);
        this.siddhiQueryBuilder = SiddhiQueryBuilder.getInstance(context);
        this.names = Names.getInstance(context);
        this.serviceDesugar = ServiceDesugar.getInstance(context);
    }

    public BLangPackage perform(BLangPackage pkgNode) {
        // Initialize the annotation map
        annotationDesugar.initializeAnnotationMap(pkgNode);
        return rewrite(pkgNode, env);
    }

    private void addAttachedFunctionsToPackageLevel(BLangPackage pkgNode, SymbolEnv env) {
        for (BLangTypeDefinition typeDef : pkgNode.typeDefinitions) {
            if (typeDef.typeNode.getKind() == NodeKind.USER_DEFINED_TYPE) {
                continue;
            }
            if (typeDef.symbol.tag == SymTag.OBJECT) {
                BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) typeDef.typeNode;

                objectTypeNode.functions.forEach(f -> {
                    if (!pkgNode.objAttachedFunctions.contains(f.symbol)) {
                        pkgNode.functions.add(f);
                        pkgNode.topLevelNodes.add(f);
                    }
                });

                if (objectTypeNode.flagSet.contains(Flag.ABSTRACT)) {
                    continue;
                }

                if (objectTypeNode.initFunction == null) {
                    objectTypeNode.initFunction = createDefaultObjectConstructor(objectTypeNode, env);
                }
                pkgNode.functions.add(objectTypeNode.initFunction);
                pkgNode.topLevelNodes.add(objectTypeNode.initFunction);
            } else if (typeDef.symbol.tag == SymTag.RECORD) {
                BLangRecordTypeNode recordTypeNod = (BLangRecordTypeNode) typeDef.typeNode;
                pkgNode.functions.add(recordTypeNod.initFunction);
                pkgNode.topLevelNodes.add(recordTypeNod.initFunction);
            }
        }
    }

    /**
     * Create package init functions.
     *
     * @param pkgNode package node
     * @param env     symbol environment of package
     */
    private void createPackageInitFunctions(BLangPackage pkgNode, SymbolEnv env) {
        String alias = pkgNode.symbol.pkgID.toString();
        pkgNode.initFunction = ASTBuilderUtil.createInitFunction(pkgNode.pos, alias, Names.INIT_FUNCTION_SUFFIX);
        // Add package level namespace declarations to the init function
        pkgNode.xmlnsList.forEach(xmlns -> {
            pkgNode.initFunction.body.addStatement(createNamespaceDeclrStatement(xmlns));
        });
        pkgNode.startFunction = ASTBuilderUtil.createInitFunction(pkgNode.pos, alias, Names.START_FUNCTION_SUFFIX);
        pkgNode.stopFunction = ASTBuilderUtil.createInitFunction(pkgNode.pos, alias, Names.STOP_FUNCTION_SUFFIX);
        // Create invokable symbol for init function
        createInvokableSymbol(pkgNode.initFunction, env);
        // Create invokable symbol for start function
        createInvokableSymbol(pkgNode.startFunction, env);
        addInitReturnStatement(pkgNode.startFunction.body);
        // Create invokable symbol for stop function
        createInvokableSymbol(pkgNode.stopFunction, env);
        addInitReturnStatement(pkgNode.stopFunction.body);
    }

    /**
     * Create invokable symbol for function.
     *
     * @param bLangFunction function node
     * @param env           Symbol environment
     */
    private void createInvokableSymbol(BLangFunction bLangFunction, SymbolEnv env) {
        LinkedHashSet<BType> members = new LinkedHashSet<>();
        members.add(symTable.errorType);
        members.add(symTable.nilType);
        final BUnionType returnType = new BUnionType(null, members, true);
        BInvokableType invokableType = new BInvokableType(new ArrayList<>(), returnType, null);

        BInvokableSymbol functionSymbol = Symbols.createFunctionSymbol(Flags.asMask(bLangFunction.flagSet),
                new Name(bLangFunction.name.value), env.enclPkg.packageID, invokableType, env.enclPkg.symbol, true);
        functionSymbol.retType = bLangFunction.returnTypeNode.type;
        // Add parameters
        for (BLangVariable param : bLangFunction.requiredParams) {
            functionSymbol.params.add(param.symbol);
        }

        functionSymbol.scope = new Scope(functionSymbol);
        bLangFunction.symbol = functionSymbol;
    }

    /**
     * Add init return statments.
     *
     * @param bLangBlockStmt block statement node
     */
    private void addInitReturnStatement(BLangBlockStmt bLangBlockStmt) {
        BLangReturn returnStmt = ASTBuilderUtil.createNilReturnStmt(bLangBlockStmt.pos, symTable.nilType);
        bLangBlockStmt.addStatement(returnStmt);
    }

    /**
     * Create namespace declaration statement for XMNLNS.
     *
     * @param xmlns XMLNS node
     * @return XMLNS statement
     */
    private BLangXMLNSStatement createNamespaceDeclrStatement(BLangXMLNS xmlns) {
        BLangXMLNSStatement xmlnsStmt = (BLangXMLNSStatement) TreeBuilder.createXMLNSDeclrStatementNode();
        xmlnsStmt.xmlnsDecl = xmlns;
        xmlnsStmt.pos = xmlns.pos;
        return xmlnsStmt;
    }
    // visitors

    @Override
    public void visit(BLangPackage pkgNode) {
        if (pkgNode.completedPhases.contains(CompilerPhase.DESUGAR)) {
            result = pkgNode;
            return;
        }
        SymbolEnv env = this.symTable.pkgEnvMap.get(pkgNode.symbol);
        createPackageInitFunctions(pkgNode, env);
        // Adding object functions to package level.
        addAttachedFunctionsToPackageLevel(pkgNode, env);

        pkgNode.constants.forEach(constant -> pkgNode.typeDefinitions.add(constant.associatedTypeDefinition));

        BLangBlockStmt serviceAttachments = serviceDesugar.rewriteServiceVariables(pkgNode.services, env);

        pkgNode.globalVars.forEach(globalVar -> {
            BLangAssignment assignment = createAssignmentStmt(globalVar);
            if (assignment.expr != null) {
                pkgNode.initFunction.body.stmts.add(assignment);
            }
        });
        annotationDesugar.rewritePackageAnnotations(pkgNode);
        //Sort type definitions with precedence
        pkgNode.typeDefinitions.sort(Comparator.comparing(t -> t.precedence));

        pkgNode.typeDefinitions = rewrite(pkgNode.typeDefinitions, env);
        pkgNode.xmlnsList = rewrite(pkgNode.xmlnsList, env);
        pkgNode.globalVars = rewrite(pkgNode.globalVars, env);

        pkgNode.services.forEach(service -> serviceDesugar.engageCustomServiceDesugar(service, env));

        pkgNode.functions = rewrite(pkgNode.functions, env);

        serviceDesugar.rewriteListeners(pkgNode.globalVars, env);
        serviceDesugar.rewriteServiceAttachments(serviceAttachments, env);

        pkgNode.initFunction = rewrite(pkgNode.initFunction, env);
        pkgNode.startFunction = rewrite(pkgNode.startFunction, env);
        pkgNode.stopFunction = rewrite(pkgNode.stopFunction, env);
        pkgNode.getTestablePkgs().forEach(testablePackage -> visit((BLangPackage) testablePackage));
        pkgNode.completedPhases.add(CompilerPhase.DESUGAR);
        result = pkgNode;
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
        BPackageSymbol pkgSymbol = importPkgNode.symbol;
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgSymbol);
        rewrite(pkgEnv.node, pkgEnv);
        result = importPkgNode;
    }

    @Override
    public void visit(BLangTypeDefinition typeDef) {
        if (typeDef.typeNode.getKind() == NodeKind.OBJECT_TYPE
                || typeDef.typeNode.getKind() == NodeKind.RECORD_TYPE) {
            typeDef.typeNode = rewrite(typeDef.typeNode, env);
        }
        result = typeDef;
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        // Merge the fields defined within the object and the fields that
        // get inherited via the type references.
        objectTypeNode.fields.addAll(objectTypeNode.referencedFields);

        if (objectTypeNode.flagSet.contains(Flag.ABSTRACT)) {
            result = objectTypeNode;
            return;
        }

        // Add object level variables to the init function.
        Map<BSymbol, BLangStatement> initFunctionStmts = objectTypeNode.initFunction.initFunctionStmts;
        objectTypeNode.fields.stream()
                // skip if the field is already have an value set by the constructor.
                .filter(field -> !initFunctionStmts.containsKey(field.symbol))
                .filter(field -> field.expr != null)
                .forEachOrdered(field -> {
                    initFunctionStmts.put(field.symbol, createAssignmentStmt(field));
                });

        // Adding init statements to the init function.
        BLangStatement[] initStmts = initFunctionStmts.values().toArray(new BLangStatement[0]);
        for (int i = 0; i < initFunctionStmts.size(); i++) {
            objectTypeNode.initFunction.body.stmts.add(i, initStmts[i]);
        }

        result = objectTypeNode;
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        recordTypeNode.fields.addAll(recordTypeNode.referencedFields);
        // Add struct level variables to the init function.
        recordTypeNode.fields.stream()
                // Only add a field if it is required. Checking if it's required is enough since non-defaultable
                // required fields will have been caught in the type checking phase.
                .filter(field -> !recordTypeNode.initFunction.initFunctionStmts.containsKey(field.symbol) &&
                        !Symbols.isOptional(field.symbol))
                .filter(field -> field.expr != null)
                .forEachOrdered(field -> {
                    recordTypeNode.initFunction.initFunctionStmts.put(field.symbol, createAssignmentStmt(field));
                });

        //Adding init statements to the init function.
        BLangStatement[] initStmts = recordTypeNode.initFunction.initFunctionStmts
                .values().toArray(new BLangStatement[0]);
        for (int i = 0; i < recordTypeNode.initFunction.initFunctionStmts.size(); i++) {
            recordTypeNode.initFunction.body.stmts.add(i, initStmts[i]);
        }

        // Add invocations for the initializers of each of the type referenced records. Here, the initializers of the
        // referenced types are invoked on the current record type.
        for (BLangType typeRef : recordTypeNode.typeRefs) {
            BVarSymbol receiverSym = recordTypeNode.initFunction.receiver.symbol;
            BInvokableSymbol dupInitFuncSym = ASTBuilderUtil.duplicateInvokableSymbol(
                    ((BRecordTypeSymbol) typeRef.type.tsymbol).initializerFunc.symbol);
            dupInitFuncSym.receiverSymbol = receiverSym;

            BLangInvocation initInv = ASTBuilderUtil.createInvocationExpr(typeRef.pos, dupInitFuncSym,
                                                                          new ArrayList<>(), symResolver);
            initInv.expr = ASTBuilderUtil.createVariableRef(recordTypeNode.initFunction.receiver.pos, receiverSym);
            initInv = rewriteExpr(initInv);

            BLangExpressionStmt exprStmt = ASTBuilderUtil.createExpressionStmt(typeRef.pos,
                                                                               recordTypeNode.initFunction.body);
            exprStmt.expr = initInv;
            initInv.parent = exprStmt;
            exprStmt.parent = recordTypeNode.initFunction.body;
        }

        result = recordTypeNode;
    }

    @Override
    public void visit(BLangFunction funcNode) {
        SymbolEnv fucEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.symbol.scope, env);
        if (!funcNode.interfaceFunction) {
            addReturnIfNotPresent(funcNode);
        }

        // Duplicate the invokable symbol and the invokable type.
        funcNode.originalFuncSymbol = funcNode.symbol;
        BInvokableSymbol dupFuncSymbol = ASTBuilderUtil.duplicateInvokableSymbol(funcNode.symbol);
        funcNode.symbol = dupFuncSymbol;
        BInvokableType dupFuncType = (BInvokableType) dupFuncSymbol.type;

        //write closure vars
        funcNode.closureVarSymbols.stream()
                .filter(symbol -> !isFunctionArgument(symbol, funcNode.symbol.params))
                .forEach(symbol -> {
                    symbol.closure = true;
                    dupFuncSymbol.params.add(0, symbol);
                    dupFuncType.paramTypes.add(0, symbol.type);
                });

        funcNode.body = rewrite(funcNode.body, fucEnv);
        funcNode.workers = rewrite(funcNode.workers, fucEnv);
        result = funcNode;
    }

    private boolean isFunctionArgument(BVarSymbol symbol, List<BVarSymbol> params) {
        return params.stream().anyMatch(param -> (param.name.equals(symbol.name) && param.type.tag == symbol.type.tag));
    }

    public void visit(BLangForever foreverStatement) {
        if (foreverStatement.isSiddhiRuntimeEnabled()) {
            siddhiQueryBuilder.visit(foreverStatement);
            BLangExpressionStmt stmt = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
            stmt.expr = createInvocationForForeverBlock(foreverStatement);
            stmt.pos = foreverStatement.pos;
            stmt.addWS(foreverStatement.getWS());
            result = rewrite(stmt, env);
        } else {
            result = streamingCodeDesugar.desugar(foreverStatement);
            result = rewrite(result, env);
        }
    }

    @Override
    public void visit(BLangResource resourceNode) {
    }

    @Override
    public void visit(BLangWorker workerNode) {
        this.workerStack.push(workerNode);
        workerNode.body = rewrite(workerNode.body, env);
        this.workerStack.pop();
        result = workerNode;
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        if ((varNode.symbol.owner.tag & SymTag.INVOKABLE) != SymTag.INVOKABLE) {
            varNode.expr = null;
            result = varNode;
            return;
        }

        // Return if this assignment is not a safe assignment
        varNode.expr = rewriteExpr(varNode.expr);
        result = varNode;

    }

    @Override
    public void visit(BLangTupleVariable varNode) {
        result = varNode;
    }

    @Override
    public void visit(BLangRecordVariable varNode) {
        result = varNode;
    }

    @Override
    public void visit(BLangErrorVariable varNode) {
        result = varNode;
    }

    // Statements

    @Override
    public void visit(BLangBlockStmt block) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(block, env);
        block.stmts = rewriteStmt(block.stmts, blockEnv);
        result = block;
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        varDefNode.var = rewrite(varDefNode.var, env);
        result = varDefNode;
    }

    @Override
    public void visit(BLangTupleVariableDef varDefNode) {
        //  case 1:
        //  (string, int) (a, b) = (tuple)
        //
        //  any[] x = (tuple);
        //  string a = x[0];
        //  int b = x[1];
        //
        //  case 2:
        //  ((string, float) int)) ((a, b), c)) = (tuple)
        //
        //  any[] x = (tuple);
        //  string a = x[0][0];
        //  float b = x[0][1];
        //  int c = x[1];
        varDefNode.var = rewrite(varDefNode.var, env);
        BLangTupleVariable tupleVariable = varDefNode.var;

        //create tuple destruct block stmt
        final BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(varDefNode.pos);

        //create a array of any-type based on the dimension
        BType runTimeType = new BArrayType(symTable.anyType);

        //create a simple var for the array 'any[] x = (tuple)' based on the dimension for x
        final BLangSimpleVariable tuple = ASTBuilderUtil.createVariable(varDefNode.pos, "", runTimeType, null,
                new BVarSymbol(0, names.fromString("tuple"), this.env.scope.owner.pkgID, runTimeType,
                        this.env.scope.owner));
        tuple.expr = tupleVariable.expr;
        final BLangSimpleVariableDef variableDef = ASTBuilderUtil.createVariableDefStmt(varDefNode.pos, blockStmt);
        variableDef.var = tuple;

        //create the variable definition statements using the root block stmt created
        createVarDefStmts(tupleVariable, blockStmt, tuple.symbol, null);

        //finally rewrite the populated block statement
        result = rewrite(blockStmt, env);
    }

    @Override
    public void visit(BLangRecordVariableDef varDefNode) {

        BLangRecordVariable varNode = varDefNode.var;

        final BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(varDefNode.pos);

        BType runTimeType = new BMapType(TypeTags.RECORD, symTable.anyType, null);

        final BLangSimpleVariable mapVariable = ASTBuilderUtil.createVariable(varDefNode.pos, "", runTimeType,
                null, new BVarSymbol(0, names.fromString("$map$0"), this.env.scope.owner.pkgID,
                        runTimeType, this.env.scope.owner));
        mapVariable.expr = varDefNode.var.expr;
        final BLangSimpleVariableDef variableDef = ASTBuilderUtil.createVariableDefStmt(varDefNode.pos, blockStmt);
        variableDef.var = mapVariable;

        createVarDefStmts(varNode, blockStmt, mapVariable.symbol, null);

        result = rewrite(blockStmt, env);
    }

    @Override
    public void visit(BLangErrorVariableDef varDefNode) {
        // TODO: complete
    }

    /**
     * This method iterate through each member of the tupleVar and create the relevant var def statements. This method
     * does the check for node kind of each member and call the related var def creation method.
     *
     * Example:
     * ((string, float) int)) ((a, b), c)) = (tuple)
     *
     * (a, b) is again a tuple, so it is a recursive var def creation.
     *
     * c is a simple var, so a simple var def will be created.
     *
     */
    private void createVarDefStmts(BLangTupleVariable parentTupleVariable, BLangBlockStmt parentBlockStmt,
                                   BVarSymbol tupleVarSymbol, BLangIndexBasedAccess parentIndexAccessExpr) {

        final List<BLangVariable> memberVars = parentTupleVariable.memberVariables;
        for (int index = 0; index < memberVars.size(); index++) {
            BLangVariable variable = memberVars.get(index);
            if (NodeKind.VARIABLE == variable.getKind()) { //if this is simple var, then create a simple var def stmt
                BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(variable.pos, symTable.intType, (long) index);
                createSimpleVarDefStmt((BLangSimpleVariable) variable, parentBlockStmt, indexExpr, tupleVarSymbol,
                        parentIndexAccessExpr);
            } else if (variable.getKind() == NodeKind.TUPLE_VARIABLE) { //else recursively create the var def statements
                BLangTupleVariable tupleVariable = (BLangTupleVariable) variable;
                BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(tupleVariable.pos, symTable.intType,
                        (long) index);
                BLangIndexBasedAccess arrayAccessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(tupleVariable.pos,
                        new BArrayType(symTable.anyType), tupleVarSymbol, indexExpr);
                if (parentIndexAccessExpr != null) {
                    arrayAccessExpr.expr = parentIndexAccessExpr;
                }
                createVarDefStmts((BLangTupleVariable) variable, parentBlockStmt, tupleVarSymbol, arrayAccessExpr);
            } else if (variable.getKind() == NodeKind.RECORD_VARIABLE) {
                BLangRecordVariable recordVariable = (BLangRecordVariable) variable;
                BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(recordVariable.pos, symTable.intType,
                        (long) index);
                BLangIndexBasedAccess arrayAccessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(
                        parentTupleVariable.pos, new BMapType(TypeTags.RECORD, symTable.anyType, null),
                        tupleVarSymbol, indexExpr);
                if (parentIndexAccessExpr != null) {
                    arrayAccessExpr.expr = parentIndexAccessExpr;
                }
                createVarDefStmts((BLangRecordVariable) variable, parentBlockStmt, tupleVarSymbol, arrayAccessExpr);
            }
        }
    }

    /**
     * Overloaded method to handle record variables.
     * This method iterate through each member of the recordVar and create the relevant var def statements. This method
     * does the check for node kind of each member and call the related var def creation method.
     *
     * Example:
     * type Foo record {
     *     string name;
     *     (int, string) age;
     *     Address address;
     * };
     *
     * Foo {name: a, age: (b, c), address: d} = {record literal}
     *
     *  a is a simple var, so a simple var def will be created.
     *
     * (b, c) is a tuple, so it is a recursive var def creation.
     *
     * d is a record, so it is a recursive var def creation.
     *
     */
    private void createVarDefStmts(BLangRecordVariable parentRecordVariable, BLangBlockStmt parentBlockStmt,
                                   BVarSymbol recordVarSymbol, BLangIndexBasedAccess parentIndexAccessExpr) {

        List<BLangRecordVariableKeyValue> variableList = parentRecordVariable.variableList;
        for (BLangRecordVariableKeyValue recordFieldKeyValue : variableList) {
            if (recordFieldKeyValue.valueBindingPattern.getKind() == NodeKind.VARIABLE) {
                BLangLiteral indexExpr = ASTBuilderUtil.
                        createLiteral((recordFieldKeyValue.valueBindingPattern).pos, symTable.stringType,
                                recordFieldKeyValue.key.value);
                createSimpleVarDefStmt((BLangSimpleVariable) recordFieldKeyValue.valueBindingPattern, parentBlockStmt,
                        indexExpr, recordVarSymbol, parentIndexAccessExpr);
            } else if (recordFieldKeyValue.valueBindingPattern.getKind() == NodeKind.TUPLE_VARIABLE) {
                BLangTupleVariable tupleVariable = (BLangTupleVariable) recordFieldKeyValue.valueBindingPattern;
                BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(tupleVariable.pos, symTable.stringType,
                        recordFieldKeyValue.key.value);
                BLangIndexBasedAccess arrayAccessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(tupleVariable.pos,
                        new BArrayType(symTable.anyType), recordVarSymbol, indexExpr);
                if (parentIndexAccessExpr != null) {
                    arrayAccessExpr.expr = parentIndexAccessExpr;
                }
                createVarDefStmts((BLangTupleVariable) recordFieldKeyValue.valueBindingPattern,
                        parentBlockStmt, recordVarSymbol, arrayAccessExpr);
            } else if (recordFieldKeyValue.valueBindingPattern.getKind() == NodeKind.RECORD_VARIABLE) {
                BLangRecordVariable recordVariable = (BLangRecordVariable) recordFieldKeyValue.valueBindingPattern;
                BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(recordVariable.pos, symTable.stringType,
                        recordFieldKeyValue.key.value);
                BLangIndexBasedAccess arrayAccessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(
                        parentRecordVariable.pos, new BMapType(TypeTags.RECORD, symTable.anyType, null),
                        recordVarSymbol, indexExpr);
                if (parentIndexAccessExpr != null) {
                    arrayAccessExpr.expr = parentIndexAccessExpr;
                }
                createVarDefStmts((BLangRecordVariable) recordFieldKeyValue.valueBindingPattern, parentBlockStmt,
                        recordVarSymbol, arrayAccessExpr);
            }
        }

        if (parentRecordVariable.restParam != null) {
            // The restParam is desugared to a filter iterable operation that filters out the fields provided in the
            // record variable
            // map<any> restParam = $map$0.filter($lambdaArg$0);

            DiagnosticPos pos = parentBlockStmt.pos;
            BMapType restParamType = (BMapType) ((BLangVariable) parentRecordVariable.restParam).type;
            BLangVariableReference variableReference;

            if (parentIndexAccessExpr != null) {
                BLangSimpleVariable mapVariable = ASTBuilderUtil.createVariable(pos, "$map$1", restParamType,
                        null, new BVarSymbol(0, names.fromString("$map$1"), this.env.scope.owner.pkgID,
                                restParamType, this.env.scope.owner));
                mapVariable.expr = parentIndexAccessExpr;
                BLangSimpleVariableDef variableDef = ASTBuilderUtil.createVariableDefStmt(pos, parentBlockStmt);
                variableDef.var = mapVariable;

                variableReference = ASTBuilderUtil.createVariableRef(pos, mapVariable.symbol);
            } else {
                variableReference = ASTBuilderUtil.createVariableRef(pos,
                        ((BLangSimpleVariableDef) parentBlockStmt.stmts.get(0)).var.symbol);
            }

            // Create rest param variable definition
            BLangSimpleVariable restParam = (BLangSimpleVariable) parentRecordVariable.restParam;
            BLangSimpleVariableDef restParamVarDef = ASTBuilderUtil.createVariableDefStmt(pos,
                    parentBlockStmt);
            restParamVarDef.var = restParam;
            restParamVarDef.var.type = restParamType;

            // Create lambda function to be passed into the filter iterable operation (i.e. $lambdaArg$0)
            BLangLambdaFunction lambdaFunction = createFuncToFilterOutRestParam(parentRecordVariable, pos);

            // Create filter iterator operation
            BLangInvocation filterIterator = (BLangInvocation) TreeBuilder.createInvocationNode();
            restParam.expr = filterIterator;

            filterIterator.iterableOperationInvocation = true;
            filterIterator.argExprs.add(lambdaFunction);
            filterIterator.requiredArgs.add(lambdaFunction);

            // Variable reference to the 1st variable of this block. i.e. the map ..
            filterIterator.expr = variableReference;

            filterIterator.type = new BIntermediateCollectionType(getStringAnyTupleType());

            IterableContext iterableContext = new IterableContext(filterIterator.expr, env);
            iterableContext.foreachTypes = getStringAnyTupleType().tupleTypes;
            filterIterator.iContext = iterableContext;

            iterableContext.resultType = restParamType;
            Operation filterOperation = new Operation(IterableKind.FILTER, filterIterator, iterableContext.resultType);
            filterOperation.pos = pos;
            filterOperation.collectionType = filterOperation.expectedType = restParamType;
            filterOperation.inputType = filterOperation.outputType = getStringAnyTupleType();
            iterableContext.operations.add(filterOperation);
        }
    }

    private BLangLambdaFunction createFuncToFilterOutRestParam(BLangRecordVarRef recordVarRef, DiagnosticPos pos) {

        // Creates following anonymous function
        //
        // function ((string, any) $lambdaArg$0) returns boolean {
        //     Following if block is generated for all parameters given in the record variable
        //     if ($lambdaArg$0[0] == "name") {
        //         return false;
        //     }
        //     if ($lambdaArg$0[0] == "age") {
        //         return false;
        //     }
        //      return true;
        // }

        BLangFunction function = ASTBuilderUtil.createFunction(pos, "$anonFunc$" + lambdaFunctionCount++);
        BVarSymbol keyValSymbol = new BVarSymbol(0, names.fromString("$lambdaArg$0"), this.env.scope.owner.pkgID,
                getStringAnyTupleType(), this.env.scope.owner);
        BLangBlockStmt functionBlock = createAnonymousFunctionBlock(pos, function, keyValSymbol);

        // Create the if statements
        for (BLangRecordVarRefKeyValue variableKeyValueNode : recordVarRef.recordRefFields) {
            createIfStmt(pos, keyValSymbol, functionBlock, variableKeyValueNode.variableName.getValue());
        }

        // Create the final return true statement
        BInvokableSymbol functionSymbol = createReturnTrueStatement(pos, function, functionBlock);

        // Create and return a lambda function
        return createLambdaFunction(function, functionSymbol);
    }

    private BLangLambdaFunction createFuncToFilterOutRestParam(BLangRecordVariable recordVariable, DiagnosticPos pos) {

        // Creates following anonymous function
        //
        // function ((string, any) $lambdaArg$0) returns boolean {
        //     Following if block is generated for all parameters given in the record variable
        //     if ($lambdaArg$0[0] == "name") {
        //         return false;
        //     }
        //     if ($lambdaArg$0[0] == "age") {
        //         return false;
        //     }
        //      return true;
        // }

        BLangFunction function = ASTBuilderUtil.createFunction(pos, "$anonFunc$" + lambdaFunctionCount++);
        BVarSymbol keyValSymbol = new BVarSymbol(0, names.fromString("$lambdaArg$0"), this.env.scope.owner.pkgID,
                getStringAnyTupleType(), this.env.scope.owner);
        BLangBlockStmt functionBlock = createAnonymousFunctionBlock(pos, function, keyValSymbol);

        // Create the if statements
        for (BLangRecordVariableKeyValueNode variableKeyValueNode : recordVariable.variableList) {
            createIfStmt(pos, keyValSymbol, functionBlock, variableKeyValueNode.getKey().getValue());
        }

        // Create the final return true statement
        BInvokableSymbol functionSymbol = createReturnTrueStatement(pos, function, functionBlock);

        // Create and return a lambda function
        return createLambdaFunction(function, functionSymbol);
    }

    private void createIfStmt(DiagnosticPos pos, BVarSymbol keyValSymbol, BLangBlockStmt functionBlock, String key) {
        BLangIf ifStmt = ASTBuilderUtil.createIfStmt(pos, functionBlock);

        BLangBlockStmt ifBlock = ASTBuilderUtil.createBlockStmt(pos, new ArrayList<>());
        BLangReturn returnStmt = ASTBuilderUtil.createReturnStmt(pos, ifBlock);
        returnStmt.expr = ASTBuilderUtil.createLiteral(pos, symTable.booleanType, false);
        ifStmt.body = ifBlock;

        BLangBracedOrTupleExpr tupleExpr = new BLangBracedOrTupleExpr();
        tupleExpr.isBracedExpr = true;
        tupleExpr.type = symTable.booleanType;

        BLangIndexBasedAccess indexBasesAccessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(pos,
                symTable.stringType, keyValSymbol, ASTBuilderUtil.createLiteral(pos, symTable.intType, (long) 0));

        BLangBinaryExpr binaryExpr = ASTBuilderUtil.createBinaryExpr(pos, indexBasesAccessExpr,
                ASTBuilderUtil.createLiteral(pos, symTable.stringType, key),
                symTable.booleanType, OperatorKind.EQUAL, null);

        binaryExpr.opSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(
                binaryExpr.opKind, binaryExpr.lhsExpr.type, binaryExpr.rhsExpr.type);

        tupleExpr.expressions.add(binaryExpr);
        ifStmt.expr = tupleExpr;
    }

    private BLangLambdaFunction createLambdaFunction(BLangFunction function, BInvokableSymbol functionSymbol) {
        BLangLambdaFunction lambdaFunction = (BLangLambdaFunction) TreeBuilder.createLambdaFunctionNode();
        lambdaFunction.function = function;
        lambdaFunction.type = functionSymbol.type;
        return lambdaFunction;
    }

    private BInvokableSymbol createReturnTrueStatement(DiagnosticPos pos, BLangFunction function,
                                                       BLangBlockStmt functionBlock) {
        BLangReturn trueReturnStmt = ASTBuilderUtil.createReturnStmt(pos, functionBlock);
        trueReturnStmt.expr = ASTBuilderUtil.createLiteral(pos, symTable.booleanType, true);

        // Create function symbol before visiting desugar phase for the function
        BInvokableSymbol functionSymbol = Symbols.createFunctionSymbol(Flags.asMask(function.flagSet),
                new Name(function.name.value), env.enclPkg.packageID, function.type, env.enclEnv.enclVarSym, true);
        functionSymbol.retType = function.returnTypeNode.type;
        functionSymbol.params = function.requiredParams.stream()
                .map(param -> param.symbol)
                .collect(Collectors.toList());
        functionSymbol.scope = env.scope;
        functionSymbol.type = new BInvokableType(Collections.singletonList(getStringAnyTupleType()),
                symTable.booleanType, null);
        function.symbol = functionSymbol;
        rewrite(function, env);
        env.enclPkg.addFunction(function);
        return functionSymbol;
    }

    private BLangBlockStmt createAnonymousFunctionBlock(DiagnosticPos pos, BLangFunction function,
                                                        BVarSymbol keyValSymbol) {
        BLangSimpleVariable inputParameter = ASTBuilderUtil.createVariable(pos, null, getStringAnyTupleType(),
                null, keyValSymbol);
        function.requiredParams.add(inputParameter);
        BLangValueType booleanTypeKind = new BLangValueType();
        booleanTypeKind.typeKind = TypeKind.BOOLEAN;
        function.returnTypeNode = booleanTypeKind;

        BLangBlockStmt functionBlock = ASTBuilderUtil.createBlockStmt(pos, new ArrayList<>());
        function.body = functionBlock;
        return functionBlock;
    }

    private BTupleType getStringAnyTupleType() {
        ArrayList<BType> typeList = new ArrayList<BType>() {{
            add(symTable.stringType);
            add(symTable.anyType);
        }};
        return new BTupleType(typeList);
    }

    /**
     * This method creates a simple variable def and assigns and array expression based on the given indexExpr.
     *
     *  case 1: when there is no parent array access expression, but with the indexExpr : 1
     *  string s = x[1];
     *
     *  case 2: when there is a parent array expression : x[2] and indexExpr : 3
     *  string s = x[2][3];
     *
     *  case 3: when there is no parent array access expression, but with the indexExpr : name
     *  string s = x[name];
     *
     *  case 4: when there is a parent map expression : x[name] and indexExpr : fName
     *  string s = x[name][fName]; // record variable inside record variable
     *
     *  case 5: when there is a parent map expression : x[name] and indexExpr : 1
     *  string s = x[name][1]; // tuple variable inside record variable
     */
    private void createSimpleVarDefStmt(BLangSimpleVariable simpleVariable, BLangBlockStmt parentBlockStmt,
                                        BLangLiteral indexExpr, BVarSymbol tupleVarSymbol,
                                        BLangIndexBasedAccess parentArrayAccessExpr) {

        Name varName = names.fromIdNode(simpleVariable.name);
        if (varName == Names.IGNORE) {
            return;
        }

        final BLangSimpleVariableDef simpleVariableDef = ASTBuilderUtil.createVariableDefStmt(simpleVariable.pos,
                parentBlockStmt);
        simpleVariableDef.var = simpleVariable;

        simpleVariable.expr = createIndexBasedAccessExpr(simpleVariable.type, simpleVariable.pos,
                indexExpr, tupleVarSymbol, parentArrayAccessExpr);
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        if (safeNavigateLHS(assignNode.varRef)) {
            BLangAccessExpression accessExpr = (BLangAccessExpression) assignNode.varRef;
            accessExpr.leafNode = true;
            result = rewriteSafeNavigationAssignment(accessExpr, assignNode.expr, assignNode.safeAssignment);
            result = rewrite(result, env);
            return;
        }

        assignNode.varRef = rewriteExpr(assignNode.varRef);
        assignNode.expr = rewriteExpr(assignNode.expr);
        result = assignNode;
    }

    @Override
    public void visit(BLangTupleDestructure tupleDestructure) {
        //  case 1:
        //  a is string, b is float
        //  (a, b) = (tuple)
        //
        //  any[] x = (tuple);
        //  string a = x[0];
        //  int b = x[1];
        //
        //  case 2:
        //  a is string, b is float, c is int
        //  ((a, b), c)) = (tuple)
        //
        //  any[] x = (tuple);
        //  string a = x[0][0];
        //  float b = x[0][1];
        //  int c = x[1];


        //create tuple destruct block stmt
        final BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(tupleDestructure.pos);

        //create a array of any-type based on the dimension
        BType runTimeType = new BArrayType(symTable.anyType);

        //create a simple var for the array 'any[] x = (tuple)' based on the dimension for x
        final BLangSimpleVariable tuple = ASTBuilderUtil.createVariable(tupleDestructure.pos, "", runTimeType, null,
                new BVarSymbol(0, names.fromString("tuple"), this.env.scope.owner.pkgID, runTimeType,
                        this.env.scope.owner));
        tuple.expr = tupleDestructure.expr;
        final BLangSimpleVariableDef variableDef = ASTBuilderUtil.createVariableDefStmt(tupleDestructure.pos,
                blockStmt);
        variableDef.var = tuple;

        //create the variable definition statements using the root block stmt created
        createVarRefAssignmentStmts(tupleDestructure.varRef, blockStmt, tuple.symbol, null);

        //finally rewrite the populated block statement
        result = rewrite(blockStmt, env);
    }

    /**
     * This method iterate through each member of the tupleVarRef and create the relevant var ref assignment statements.
     * This method does the check for node kind of each member and call the related var ref creation method.
     *
     * Example:
     * ((a, b), c)) = (tuple)
     *
     * (a, b) is again a tuple, so it is a recursive var ref creation.
     *
     * c is a simple var, so a simple var def will be created.
     *
     */
    private void createVarRefAssignmentStmts(BLangTupleVarRef parentTupleVariable, BLangBlockStmt parentBlockStmt,
                                             BVarSymbol tupleVarSymbol, BLangIndexBasedAccess parentIndexAccessExpr) {

        final List<BLangExpression> expressions = parentTupleVariable.expressions;
        for (int index = 0; index < expressions.size(); index++) {
            BLangExpression expression = expressions.get(index);
            if (NodeKind.SIMPLE_VARIABLE_REF == expression.getKind() ||
                    NodeKind.FIELD_BASED_ACCESS_EXPR == expression.getKind() ||
                    NodeKind.INDEX_BASED_ACCESS_EXPR == expression.getKind() ||
                    NodeKind.XML_ATTRIBUTE_ACCESS_EXPR == expression.getKind()) {
                //if this is simple var, then create a simple var def stmt
                BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(expression.pos, symTable.intType, (long) index);
                createSimpleVarRefAssignmentStmt((BLangVariableReference) expression, parentBlockStmt, indexExpr,
                        tupleVarSymbol, parentIndexAccessExpr);
            } else if (expression.getKind() == NodeKind.TUPLE_VARIABLE_REF) {
                //else recursively create the var def statements for tuple var ref
                BLangTupleVarRef tupleVarRef = (BLangTupleVarRef) expression;
                BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(tupleVarRef.pos, symTable.intType, (long) index);
                BLangIndexBasedAccess arrayAccessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(tupleVarRef.pos,
                        new BArrayType(symTable.anyType), tupleVarSymbol, indexExpr);
                if (parentIndexAccessExpr != null) {
                    arrayAccessExpr.expr = parentIndexAccessExpr;
                }
                createVarRefAssignmentStmts((BLangTupleVarRef) expression, parentBlockStmt, tupleVarSymbol,
                        arrayAccessExpr);
            } else if (expression.getKind() == NodeKind.RECORD_VARIABLE_REF) {
                //else recursively create the var def statements for record var ref
                BLangRecordVarRef recordVarRef = (BLangRecordVarRef) expression;
                BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(recordVarRef.pos, symTable.intType,
                        (long) index);
                BLangIndexBasedAccess arrayAccessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(
                        parentTupleVariable.pos, new BMapType(TypeTags.RECORD, symTable.anyType, null),
                        tupleVarSymbol, indexExpr);
                if (parentIndexAccessExpr != null) {
                    arrayAccessExpr.expr = parentIndexAccessExpr;
                }
                createVarRefAssignmentStmts((BLangRecordVarRef) expression, parentBlockStmt, tupleVarSymbol,
                        arrayAccessExpr);
            }
        }
    }

    /**
     * This method creates a assignment statement and assigns and array expression based on the given indexExpr.
     *
     */
    private void createSimpleVarRefAssignmentStmt(BLangVariableReference simpleVarRef, BLangBlockStmt parentBlockStmt,
                                                  BLangLiteral indexExpr, BVarSymbol tupleVarSymbol,
                                                  BLangIndexBasedAccess parentArrayAccessExpr) {

        if (simpleVarRef.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            Name varName = names.fromIdNode(((BLangSimpleVarRef) simpleVarRef).variableName);
            if (varName == Names.IGNORE) {
                return;
            }
        }

        final BLangExpression assignmentExpr = createIndexBasedAccessExpr(simpleVarRef.type, simpleVarRef.pos,
                indexExpr, tupleVarSymbol, parentArrayAccessExpr);

        final BLangAssignment assignmentStmt = ASTBuilderUtil.createAssignmentStmt(parentBlockStmt.pos,
                parentBlockStmt);
        assignmentStmt.varRef = simpleVarRef;
        assignmentStmt.expr = assignmentExpr;
    }

    private BLangExpression createIndexBasedAccessExpr(BType varType, DiagnosticPos varPos, BLangLiteral indexExpr,
                                                       BVarSymbol tupleVarSymbol, BLangIndexBasedAccess parentExpr) {

        BLangIndexBasedAccess arrayAccess = ASTBuilderUtil.createIndexBasesAccessExpr(varPos,
                symTable.anyType, tupleVarSymbol, indexExpr);

        if (parentExpr != null) {
            arrayAccess.expr = parentExpr;
        }

        final BLangExpression assignmentExpr;
        if (types.isValueType(varType)) {
            BLangTypeConversionExpr castExpr = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
            castExpr.expr = arrayAccess;
            castExpr.conversionSymbol = Symbols.createUnboxValueTypeOpSymbol(symTable.anyType, varType);
            castExpr.type = varType;
            assignmentExpr = castExpr;
        } else {
            assignmentExpr = arrayAccess;
        }
        return assignmentExpr;
    }

    @Override
    public void visit(BLangRecordDestructure recordDestructure) {

        final BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(recordDestructure.pos);

        BType runTimeType = new BMapType(TypeTags.RECORD, symTable.anyType, null);

        final BLangSimpleVariable mapVariable = ASTBuilderUtil.createVariable(recordDestructure.pos, "", runTimeType,
                null, new BVarSymbol(0, names.fromString("$map$0"), this.env.scope.owner.pkgID,
                        runTimeType, this.env.scope.owner));
        mapVariable.expr = recordDestructure.expr;
        final BLangSimpleVariableDef variableDef = ASTBuilderUtil.
                createVariableDefStmt(recordDestructure.pos, blockStmt);
        variableDef.var = mapVariable;

        //create the variable definition statements using the root block stmt created
        createVarRefAssignmentStmts(recordDestructure.varRef, blockStmt, mapVariable.symbol, null);

        //finally rewrite the populated block statement
        result = rewrite(blockStmt, env);
    }

    @Override
    public void visit(BLangErrorDestructure errorDestructure) {
        // TODO: Complete
        result = errorDestructure;
    }

    private void createVarRefAssignmentStmts(BLangRecordVarRef parentRecordVarRef, BLangBlockStmt parentBlockStmt,
                                             BVarSymbol recordVarSymbol, BLangIndexBasedAccess parentIndexAccessExpr) {
        final List<BLangRecordVarRefKeyValue> variableRefList = parentRecordVarRef.recordRefFields;
        for (BLangRecordVarRefKeyValue varRefKeyValue : variableRefList) {
            BLangExpression variableReference = varRefKeyValue.variableReference;
            if (NodeKind.SIMPLE_VARIABLE_REF == variableReference.getKind() ||
                    NodeKind.FIELD_BASED_ACCESS_EXPR == variableReference.getKind() ||
                    NodeKind.INDEX_BASED_ACCESS_EXPR == variableReference.getKind() ||
                    NodeKind.XML_ATTRIBUTE_ACCESS_EXPR == variableReference.getKind()) {
                BLangLiteral indexExpr = ASTBuilderUtil.
                        createLiteral(variableReference.pos, symTable.stringType,
                                varRefKeyValue.variableName.getValue());
                createSimpleVarRefAssignmentStmt((BLangVariableReference) variableReference, parentBlockStmt,
                        indexExpr, recordVarSymbol, parentIndexAccessExpr);
            } else if (NodeKind.RECORD_VARIABLE_REF == variableReference.getKind()) {
                BLangRecordVarRef recordVariable = (BLangRecordVarRef) variableReference;
                BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(recordVariable.pos, symTable.stringType,
                        varRefKeyValue.variableName.getValue());
                BLangIndexBasedAccess arrayAccessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(
                        parentRecordVarRef.pos, new BMapType(TypeTags.RECORD, symTable.anyType, null),
                        recordVarSymbol, indexExpr);
                if (parentIndexAccessExpr != null) {
                    arrayAccessExpr.expr = parentIndexAccessExpr;
                }
                createVarRefAssignmentStmts(recordVariable, parentBlockStmt, recordVarSymbol, arrayAccessExpr);
            } else if (NodeKind.TUPLE_VARIABLE_REF == variableReference.getKind()) {
                BLangTupleVarRef tupleVariable = (BLangTupleVarRef) variableReference;
                BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(tupleVariable.pos, symTable.stringType,
                        varRefKeyValue.variableName.getValue());
                BLangIndexBasedAccess arrayAccessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(tupleVariable.pos,
                        new BArrayType(symTable.anyType), recordVarSymbol, indexExpr);
                if (parentIndexAccessExpr != null) {
                    arrayAccessExpr.expr = parentIndexAccessExpr;
                }
                createVarRefAssignmentStmts(tupleVariable, parentBlockStmt, recordVarSymbol, arrayAccessExpr);
            }
        }

        if (parentRecordVarRef.restParam != null) {
            // The restParam is desugared to a filter iterable operation that filters out the fields provided in the
            // record variable
            // map<any> restParam = $map$0.filter($lambdaArg$0);

            DiagnosticPos pos = parentBlockStmt.pos;
            BMapType restParamType = (BMapType) ((BLangSimpleVarRef) parentRecordVarRef.restParam).type;
            BLangVariableReference variableReference;

            if (parentIndexAccessExpr != null) {
                BLangSimpleVariable mapVariable = ASTBuilderUtil.createVariable(pos, "$map$1", restParamType,
                        null, new BVarSymbol(0, names.fromString("$map$1"), this.env.scope.owner.pkgID,
                                restParamType, this.env.scope.owner));
                mapVariable.expr = parentIndexAccessExpr;
                BLangSimpleVariableDef variableDef = ASTBuilderUtil.createVariableDefStmt(pos, parentBlockStmt);
                variableDef.var = mapVariable;

                variableReference = ASTBuilderUtil.createVariableRef(pos, mapVariable.symbol);
            } else {
                variableReference = ASTBuilderUtil.createVariableRef(pos,
                        ((BLangSimpleVariableDef) parentBlockStmt.stmts.get(0)).var.symbol);
            }

            // Create rest param variable definition
            BLangSimpleVarRef restParam = (BLangSimpleVarRef) parentRecordVarRef.restParam;
            BLangAssignment restParamAssignment = ASTBuilderUtil.createAssignmentStmt(pos, parentBlockStmt);
            restParamAssignment.varRef = restParam;
            restParamAssignment.varRef.type = restParamType;

            // Create lambda function to be passed into the filter iterable operation (i.e. $lambdaArg$0)
            BLangLambdaFunction lambdaFunction = createFuncToFilterOutRestParam(parentRecordVarRef, pos);

            // Create filter iterator operation
            BLangInvocation filterIterator = (BLangInvocation) TreeBuilder.createInvocationNode();
            restParamAssignment.expr = filterIterator;

            filterIterator.iterableOperationInvocation = true;
            filterIterator.argExprs.add(lambdaFunction);
            filterIterator.requiredArgs.add(lambdaFunction);

            // Variable reference to the 1st variable of this block. i.e. the map ..
            filterIterator.expr = variableReference;

            filterIterator.type = new BIntermediateCollectionType(getStringAnyTupleType());

            IterableContext iterableContext = new IterableContext(filterIterator.expr, env);
            iterableContext.foreachTypes = getStringAnyTupleType().tupleTypes;
            filterIterator.iContext = iterableContext;

            iterableContext.resultType = restParamType;
            Operation filterOperation = new Operation(IterableKind.FILTER, filterIterator, iterableContext.resultType);
            filterOperation.pos = pos;
            filterOperation.collectionType = filterOperation.expectedType = restParamType;
            filterOperation.inputType = filterOperation.outputType = getStringAnyTupleType();
            iterableContext.operations.add(filterOperation);
        }
    }

    @Override
    public void visit(BLangAbort abortNode) {
        result = abortNode;
    }

    @Override
    public void visit(BLangRetry retryNode) {
        result = retryNode;
    }

    @Override
    public void visit(BLangContinue nextNode) {
        result = nextNode;
    }

    @Override
    public void visit(BLangBreak breakNode) {
        result = breakNode;
    }

    @Override
    public void visit(BLangReturn returnNode) {
        // If the return node do not have an expression, we add `done` statement instead of a return statement. This is
        // to distinguish between returning nil value specifically and not returning any value.
        if (returnNode.expr != null) {
            returnNode.expr = rewriteExpr(returnNode.expr);
        }
        result = returnNode;
    }

    @Override
    public void visit(BLangPanic panicNode) {
        panicNode.expr = rewriteExpr(panicNode.expr);
        result = panicNode;
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        xmlnsStmtNode.xmlnsDecl = rewrite(xmlnsStmtNode.xmlnsDecl, env);
        result = xmlnsStmtNode;
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        BLangXMLNS generatedXMLNSNode;
        xmlnsNode.namespaceURI = rewriteExpr(xmlnsNode.namespaceURI);
        BSymbol ownerSymbol = xmlnsNode.symbol.owner;

        // Local namespace declaration in a function/resource/action/worker
        if ((ownerSymbol.tag & SymTag.INVOKABLE) == SymTag.INVOKABLE ||
                (ownerSymbol.tag & SymTag.SERVICE) == SymTag.SERVICE) {
            generatedXMLNSNode = new BLangLocalXMLNS();
        } else {
            generatedXMLNSNode = new BLangPackageXMLNS();
        }

        generatedXMLNSNode.namespaceURI = xmlnsNode.namespaceURI;
        generatedXMLNSNode.prefix = xmlnsNode.prefix;
        generatedXMLNSNode.symbol = xmlnsNode.symbol;
        result = generatedXMLNSNode;
    }

    public void visit(BLangCompoundAssignment compoundAssignment) {
        BLangAssignment assignStmt = (BLangAssignment) TreeBuilder.createAssignmentNode();
        assignStmt.pos = compoundAssignment.pos;
        BLangVariableReference varRef = compoundAssignment.varRef;

        // Create a new varRef if this is a simpleVarRef. Because this can be a
        // narrowed type var. In that case, lhs and rhs must be visited in two
        // different manners.
        if (varRef.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            varRef = ASTBuilderUtil.createVariableRef(compoundAssignment.varRef.pos, varRef.symbol);
            varRef.lhsVar = true;
        }

        assignStmt.setVariable(rewriteExpr(varRef));
        assignStmt.expr = rewriteExpr(compoundAssignment.modifiedExpr);
        result = assignStmt;
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        exprStmtNode.expr = rewriteExpr(exprStmtNode.expr);
        result = exprStmtNode;
    }

    @Override
    public void visit(BLangIf ifNode) {
        ifNode.expr = rewriteExpr(ifNode.expr);
        ifNode.body = rewrite(ifNode.body, env);
        ifNode.elseStmt = rewrite(ifNode.elseStmt, env);
        result = ifNode;
    }

    @Override
    public void visit(BLangMatch matchStmt) {
        // Here we generate an if-else statement for the match statement
        // Here is an example match statement
        //
        //  case 1 (old match)
        //
        //      match expr {
        //          int k => io:println("int value: " + k);
        //          string s => io:println("string value: " + s);
        //          json j => io:println("json value: " + s);
        //
        //      }
        //
        //  Here is how we convert the match statement to an if-else statement. The last clause should always be the
        //  else clause
        //
        //  string | int | json | any _$$_matchexpr = expr;
        //  if ( _$$_matchexpr isassignable int ){
        //      int k = (int) _$$_matchexpr; // unbox
        //      io:println("int value: " + k);
        //
        //  } else if (_$$_matchexpr isassignable string ) {
        //      string s = (string) _$$_matchexpr; // unbox
        //      io:println("string value: " + s);
        //
        //  } else if ( _$$_matchexpr isassignable float ||    // should we consider json[] as well
        //                  _$$_matchexpr isassignable boolean ||
        //                  _$$_matchexpr isassignable json) {
        //
        //  } else {
        //      // handle the last pattern
        //      any case..
        //  }
        //
        //  case 2 (new match)
        //      match expr {
        //          12 => io:println("Matched Int Value 12");
        //          35 => io:println("Matched Int Value 35");
        //          true => io:println("Matched Boolean Value true");
        //          "Hello" => io:println("Matched String Value Hello");
        //      }
        //
        //  This will be desugared as below :
        //
        //  string | int | boolean _$$_matchexpr = expr;
        //  if ((<int>_$$_matchexpr) == 12){
        //      io:println("Matched Int Value 12");
        //
        //  } else if ((<int>_$$_matchexpr) == 35) {
        //      io:println("Matched Int Value 35");
        //
        //  } else if ((<boolean>_$$_matchexpr) == true) {
        //      io:println("Matched Boolean Value true");
        //
        //  } else if ((<string>_$$_matchexpr) == "Hello") {
        //      io:println("Matched String Value Hello");
        //
        //  }

        // First create a block statement to hold generated statements
        BLangBlockStmt matchBlockStmt = (BLangBlockStmt) TreeBuilder.createBlockNode();
        matchBlockStmt.pos = matchStmt.pos;

        // Create a variable definition to store the value of the match expression
        String matchExprVarName = GEN_VAR_PREFIX.value;
        BLangSimpleVariable matchExprVar = ASTBuilderUtil.createVariable(matchStmt.expr.pos,
                matchExprVarName, matchStmt.expr.type, matchStmt.expr, new BVarSymbol(0,
                        names.fromString(matchExprVarName),
                        this.env.scope.owner.pkgID, matchStmt.expr.type, this.env.scope.owner));

        // Now create a variable definition node
        BLangSimpleVariableDef matchExprVarDef = ASTBuilderUtil.createVariableDef(matchBlockStmt.pos, matchExprVar);

        // Add the var def statement to the block statement
        //      string | int _$$_matchexpr = expr;
        matchBlockStmt.stmts.add(matchExprVarDef);

        // Create if/else blocks with typeof binary expressions for each pattern
        matchBlockStmt.stmts.add(generateIfElseStmt(matchStmt, matchExprVar));

        rewrite(matchBlockStmt, this.env);
        result = matchBlockStmt;
    }

    @Override
    public void visit(BLangForeach foreach) {
        BLangBlockStmt blockNode;

        // We need to create a new variable for the expression as well. This is needed because integer ranges can be
        // added as the expression so we cannot get the symbol in such cases.
        BVarSymbol dataSymbol = new BVarSymbol(0, names.fromString("$data$"), this.env.scope.owner.pkgID,
                foreach.collection.type, this.env.scope.owner);
        BLangSimpleVariable dataVariable = ASTBuilderUtil.createVariable(foreach.pos, "$data$",
                foreach.collection.type, foreach.collection, dataSymbol);
        BLangSimpleVariableDef dataVariableDefinition = ASTBuilderUtil.createVariableDef(foreach.pos, dataVariable);

        // Get the symbol of the variable (collection).
        BVarSymbol collectionSymbol = dataVariable.symbol;
        switch (foreach.collection.type.tag) {
            case TypeTags.ARRAY:
            case TypeTags.TUPLE:
            case TypeTags.XML:
            case TypeTags.MAP:
            case TypeTags.TABLE:
            case TypeTags.RECORD:
                blockNode = desugarForeachToWhile(foreach, collectionSymbol);
                break;
            default:
                blockNode = ASTBuilderUtil.createBlockStmt(foreach.pos);
                break;
        }

        blockNode.stmts.add(0, dataVariableDefinition);
        // Rewrite the block.
        rewrite(blockNode, this.env);
        result = blockNode;
    }

    private BLangBlockStmt desugarForeachToWhile(BLangForeach foreach, BVarSymbol collectionSymbol) {

        // We desugar the foreach statement to a while loop here.
        //
        // int[] data = [1, 2, 3];
        //
        // // Before desugaring.
        // foreach int i in data {
        //     io:println(i);
        // }
        //
        // // After desugaring.
        //
        // int[] $data$ = data;
        //
        // any $iterator$ = $data$.iterate();
        // map<T>? $result$ = $iterator$.next();
        //
        // while $result$ != () {
        //     if $result$ is () {
        //         break;
        //     } else {
        //         T i = $result$.value;
        //         $result$ = $iterator$.next();
        //         ....
        //         [foreach node body]
        //         ....
        //     }
        // }

        // Note - any $iterator$ = $data$.iterate(); -------------------------------------------------------------------

        // Get the variable definition from the foreach statement. Later we add this to the while statement's body.
        VariableDefinitionNode variableDefinitionNode = foreach.variableDefinitionNode;

        // Create a new symbol for the $size$.
        BVarSymbol iteratorSymbol = new BVarSymbol(0, names.fromString("$iterator$"), this.env.scope.owner.pkgID,
                symTable.anyType, this.env.scope.owner);

        // Note - $data$.iterate();
        BLangSimpleVariableDef iteratorVariableDefinition = getIteratorVariableDefinition(foreach, collectionSymbol,
                iteratorSymbol);

        // Create a new symbol for the $result$.
        BVarSymbol resultSymbol = new BVarSymbol(0, names.fromString("$result$"), this.env.scope.owner.pkgID,
                foreach.nillableResultType, this.env.scope.owner);

        // Note - map<T>? $result$ = $iterator$.next();
        BLangSimpleVariableDef resultVariableDefinition = getIteratorNextVariableDefinition(foreach, collectionSymbol,
                iteratorSymbol, resultSymbol);

        // Note - $result$ is ()
        BLangTypeTestExpr typeTestExpressionNode = getTypeTestExpression(foreach, resultSymbol);

        // Note - If statement
        BLangBlockStmt ifStatementBody = ASTBuilderUtil.createBlockStmt(foreach.pos);
        BLangBreak breakNode = (BLangBreak) TreeBuilder.createBreakNode();
        breakNode.pos = foreach.pos;
        ifStatementBody.addStatement(breakNode);

        // Note - if $result$ is ()
        BLangIf ifStatement = getIfStatement(foreach, resultSymbol, typeTestExpressionNode, ifStatementBody);

        // T i = $result$.value;
        variableDefinitionNode.getVariable().setInitialExpression(getValueAccessExpression(foreach, resultSymbol));

        BLangBlockStmt elseStatement = foreach.body;

        // Note - while ... { if ... { ... } else { T i = $result$.value; } };
        elseStatement.stmts.add(0, (BLangStatement) variableDefinitionNode);

        // Note - $result$ = $iterator$.next();
        BLangAssignment resultAssignment = getIteratorNextAssignment(foreach, collectionSymbol, iteratorSymbol,
                resultSymbol);

        // Note - while ... { if ... { ... } else { T i = $result$.value; $result$ = $iterator$.next(); } };
        elseStatement.stmts.add(1, resultAssignment);

        ifStatement.elseStmt = elseStatement;

        // Note - $result$ != ()
        BLangSimpleVarRef resultReferenceInWhile = ASTBuilderUtil.createVariableRef(foreach.pos, resultSymbol);
        BLangLiteral nilLiteral = ASTBuilderUtil.createLiteral(foreach.pos, symTable.nilType, Names.NIL_VALUE);

        BOperatorSymbol operatorSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.NOT_EQUAL,
                symTable.anyType, nilLiteral.type);
        BLangBinaryExpr binaryExpr = ASTBuilderUtil.createBinaryExpr(foreach.pos, resultReferenceInWhile, nilLiteral,
                symTable.booleanType, OperatorKind.NOT_EQUAL, operatorSymbol);

        // Note - while $result$ != ()
        BLangWhile whileNode = (BLangWhile) TreeBuilder.createWhileNode();
        whileNode.pos = foreach.pos;
        whileNode.expr = binaryExpr;
        whileNode.body = ASTBuilderUtil.createBlockStmt(foreach.pos);
        whileNode.body.addStatement(ifStatement);

        // Create a new block statement node.
        BLangBlockStmt blockNode = ASTBuilderUtil.createBlockStmt(foreach.pos);

        // Add iterator variable to the block.
        blockNode.addStatement(iteratorVariableDefinition);

        // Add result variable to the block.
        blockNode.addStatement(resultVariableDefinition);

        // Add the while node to the block.
        blockNode.addStatement(whileNode);

        return blockNode;
    }

    @Override
    public void visit(BLangWhile whileNode) {
        whileNode.expr = rewriteExpr(whileNode.expr);
        whileNode.body = rewrite(whileNode.body, env);
        result = whileNode;
    }

    @Override
    public void visit(BLangLock lockNode) {
        enclLocks.push(lockNode);
        lockNode.body = rewrite(lockNode.body, env);
        enclLocks.pop();
        lockNode.lockVariables = lockNode.lockVariables.stream().sorted((v1, v2) -> {
            String o1FullName = String.join(":", v1.pkgID.getName().getValue(), v1.name.getValue());
            String o2FullName = String.join(":", v2.pkgID.getName().getValue(), v2.name.getValue());
            return o1FullName.compareTo(o2FullName);
        }).collect(Collectors.toSet());

        //check both a field and parent are in locked variables
        if (!lockNode.lockVariables.isEmpty()) {
            lockNode.fieldVariables.values().forEach(exprSet -> exprSet.removeIf(expr -> isParentLocked(lockNode,
                    expr)));
        }
        result = lockNode;
    }

    boolean isParentLocked(BLangLock lock, BLangVariableReference expr) {
        if (lock.lockVariables.contains(expr.symbol)) {
            return true;
        } else if (expr instanceof BLangStructFieldAccessExpr) {
            return isParentLocked(lock, (BLangVariableReference) ((BLangStructFieldAccessExpr) expr).expr);
        }
        return false;
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        transactionNode.transactionBody = rewrite(transactionNode.transactionBody, env);
        transactionNode.onRetryBody = rewrite(transactionNode.onRetryBody, env);
        transactionNode.committedBody = rewrite(transactionNode.committedBody, env);
        transactionNode.abortedBody = rewrite(transactionNode.abortedBody, env);
        transactionNode.retryCount = rewriteExpr(transactionNode.retryCount);
        result = transactionNode;
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
         result = forkJoin;
    }

    // Expressions

    @Override
    public void visit(BLangLiteral literalExpr) {
        if (literalExpr.type.tag == TypeTags.ARRAY && ((BArrayType) literalExpr.type).eType.tag == TypeTags.BYTE) {
            // this is blob literal as byte array
            result = rewriteBlobLiteral(literalExpr);
            return;
        }
        result = literalExpr;
    }

    private BLangNode rewriteBlobLiteral(BLangLiteral literalExpr) {
        String[] result = getBlobTextValue((String) literalExpr.value);
        if (BASE_64.equals(result[0])) {
            literalExpr.value = Base64.getDecoder().decode(result[1].getBytes(StandardCharsets.UTF_8));
        } else {
            literalExpr.value = hexStringToByteArray(result[1]);
        }
        return literalExpr;
    }

    private String[] getBlobTextValue(String blobLiteralNodeText) {
        String nodeText = blobLiteralNodeText.replaceAll(" ", "");
        String[] result = new String[2];
        result[0] = nodeText.substring(0, nodeText.indexOf('`'));
        result[1] = nodeText.substring(nodeText.indexOf('`') + 1, nodeText.lastIndexOf('`'));
        return result;
    }

    private static byte[] hexStringToByteArray(String str) {
        int len = str.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(str.charAt(i), 16) << 4) + Character.digit(str.charAt(i + 1), 16));
        }
        return data;
    }

    @Override
    public void visit(BLangArrayLiteral arrayLiteral) {
        arrayLiteral.exprs = rewriteExprs(arrayLiteral.exprs);

        if (arrayLiteral.type.tag == TypeTags.JSON) {
            result = new BLangJSONArrayLiteral(arrayLiteral.exprs, new BArrayType(arrayLiteral.type));
            return;
        } else if (getElementType(arrayLiteral.type).tag == TypeTags.JSON) {
            result = new BLangJSONArrayLiteral(arrayLiteral.exprs, arrayLiteral.type);
            return;
        }
        result = arrayLiteral;
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        // Process the key-val pairs in the record literal
        recordLiteral.keyValuePairs.forEach(keyValue -> {
            BLangExpression keyExpr = keyValue.key.expr;
            if (keyExpr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                BLangSimpleVarRef varRef = (BLangSimpleVarRef) keyExpr;
                keyValue.key.expr = createStringLiteral(varRef.pos, varRef.variableName.value);
            } else {
                keyValue.key.expr = rewriteExpr(keyValue.key.expr);
            }

            keyValue.valueExpr = rewriteExpr(keyValue.valueExpr);
        });

        BLangExpression expr;
        if (recordLiteral.type.tag == TypeTags.RECORD) {
            expr = new BLangStructLiteral(recordLiteral.keyValuePairs, recordLiteral.type);
        } else if (recordLiteral.type.tag == TypeTags.MAP) {
            expr = new BLangMapLiteral(recordLiteral.keyValuePairs, recordLiteral.type);
        } else {
            expr = new BLangJSONLiteral(recordLiteral.keyValuePairs, recordLiteral.type);
        }

        result = rewriteExpr(expr);
    }

    @Override
    public void visit(BLangTableLiteral tableLiteral) {
        tableLiteral.tableDataRows = rewriteExprs(tableLiteral.tableDataRows);
        //Generate key columns Array
        List<String> keyColumns = new ArrayList<>();
        for (BLangTableLiteral.BLangTableColumn column : tableLiteral.columns) {
            if (column.flagSet.contains(TableColumnFlag.PRIMARYKEY)) {
                keyColumns.add(column.columnName);
            }
        }
        BLangArrayLiteral keyColumnsArrayLiteral = createArrayLiteralExprNode();
        keyColumnsArrayLiteral.exprs = keyColumns.stream()
                .map(expr -> ASTBuilderUtil.createLiteral(tableLiteral.pos, symTable.stringType, expr))
                .collect(Collectors.toList());
        keyColumnsArrayLiteral.type = new BArrayType(symTable.stringType);
        tableLiteral.keyColumnsArrayLiteral = keyColumnsArrayLiteral;
        //Generate index columns Array
        List<String> indexColumns = new ArrayList<>();
        for (BLangTableLiteral.BLangTableColumn column : tableLiteral.columns) {
            if (column.flagSet.contains(TableColumnFlag.INDEX)) {
                indexColumns.add(column.columnName);
            }
        }
        BLangArrayLiteral indexColumnsArrayLiteral = createArrayLiteralExprNode();
        indexColumnsArrayLiteral.exprs = indexColumns.stream()
                .map(expr -> ASTBuilderUtil.createLiteral(tableLiteral.pos, symTable.stringType, expr))
                .collect(Collectors.toList());
        indexColumnsArrayLiteral.type = new BArrayType(symTable.stringType);
        tableLiteral.indexColumnsArrayLiteral = indexColumnsArrayLiteral;
        result = tableLiteral;
    }

    private BLangInvocation createInvocationForForeverBlock(BLangForever forever) {
        List<BLangExpression> args = new ArrayList<>();
        BLangLiteral streamingQueryLiteral = ASTBuilderUtil.createLiteral(forever.pos, symTable.stringType,
                forever.getSiddhiQuery());
        args.add(streamingQueryLiteral);
        addReferenceVariablesToArgs(args, siddhiQueryBuilder.getInStreamRefs());
        addReferenceVariablesToArgs(args, siddhiQueryBuilder.getInTableRefs());
        addReferenceVariablesToArgs(args, siddhiQueryBuilder.getOutStreamRefs());
        addReferenceVariablesToArgs(args, siddhiQueryBuilder.getOutTableRefs());
        addFunctionPointersToArgs(args, forever.getStreamingQueryStatements());
        return createInvocationNode(CREATE_FOREVER, args, symTable.noType);
    }

    private void addReferenceVariablesToArgs(List<BLangExpression> args, List<BLangExpression> varRefs) {
        BLangArrayLiteral localRefs = createArrayLiteralExprNode();
        varRefs.forEach(varRef -> localRefs.exprs.add(rewrite(varRef, env)));
        args.add(localRefs);
    }

    private void addFunctionPointersToArgs(List<BLangExpression> args, List<StreamingQueryStatementNode>
            streamingStmts) {
        BLangArrayLiteral funcPointers = createArrayLiteralExprNode();
        for (StreamingQueryStatementNode stmt : streamingStmts) {
            funcPointers.exprs.add(rewrite((BLangExpression) stmt.getStreamingAction().getInvokableBody(), env));
        }
        args.add(funcPointers);
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        BLangSimpleVarRef genVarRefExpr = varRefExpr;

        // XML qualified name reference. e.g: ns0:foo
        if (varRefExpr.pkgSymbol != null && varRefExpr.pkgSymbol.tag == SymTag.XMLNS) {
            BLangXMLQName qnameExpr = new BLangXMLQName(varRefExpr.variableName);
            qnameExpr.nsSymbol = (BXMLNSSymbol) varRefExpr.pkgSymbol;
            qnameExpr.localname = varRefExpr.variableName;
            qnameExpr.prefix = varRefExpr.pkgAlias;
            qnameExpr.namespaceURI = qnameExpr.nsSymbol.namespaceURI;
            qnameExpr.isUsedInXML = false;
            qnameExpr.pos = varRefExpr.pos;
            qnameExpr.type = symTable.stringType;
            result = qnameExpr;
            return;
        }

        // Restore the original symbol
        if ((varRefExpr.symbol.tag & SymTag.VARIABLE) == SymTag.VARIABLE) {
            BVarSymbol varSymbol = (BVarSymbol) varRefExpr.symbol;
            if (varSymbol.originalSymbol != null) {
                varRefExpr.symbol = varSymbol.originalSymbol;
            }
        }

        BSymbol ownerSymbol = varRefExpr.symbol.owner;
        if ((varRefExpr.symbol.tag & SymTag.FUNCTION) == SymTag.FUNCTION &&
                varRefExpr.symbol.type.tag == TypeTags.INVOKABLE) {
            genVarRefExpr = new BLangFunctionVarRef((BVarSymbol) varRefExpr.symbol);
        } else if ((varRefExpr.symbol.tag & SymTag.TYPE) == SymTag.TYPE) {
            genVarRefExpr = new BLangTypeLoad(varRefExpr.symbol);
        } else if ((ownerSymbol.tag & SymTag.INVOKABLE) == SymTag.INVOKABLE) {
            // Local variable in a function/resource/action/worker
            genVarRefExpr = new BLangLocalVarRef((BVarSymbol) varRefExpr.symbol);
        } else if ((ownerSymbol.tag & SymTag.STRUCT) == SymTag.STRUCT) {
            genVarRefExpr = new BLangFieldVarRef((BVarSymbol) varRefExpr.symbol);
        } else if ((ownerSymbol.tag & SymTag.PACKAGE) == SymTag.PACKAGE ||
                (ownerSymbol.tag & SymTag.SERVICE) == SymTag.SERVICE) {
            if (varRefExpr.symbol.tag == SymTag.CONSTANT) {
                BConstantSymbol symbol = (BConstantSymbol) varRefExpr.symbol;
                // We need to get a copy of the literal value and set it as the result. Otherwise there will be
                // issues because registry allocation will be only done one time.
                BLangLiteral literal = ASTBuilderUtil
                        .createLiteral(varRefExpr.pos, symbol.literalValueType, symbol.literalValue);
                literal.type.tag = symbol.literalValueTypeTag;
                result = rewriteExpr(addConversionExprIfRequired(literal, varRefExpr.type));
                return;
            } else {
                // Package variable | service variable.
                // We consider both of them as package level variables.
                genVarRefExpr = new BLangPackageVarRef((BVarSymbol) varRefExpr.symbol);

                if (!enclLocks.isEmpty()) {
                    enclLocks.peek().addLockVariable((BVarSymbol) varRefExpr.symbol);
                }
            }
        }

        genVarRefExpr.type = varRefExpr.type;
        genVarRefExpr.pos = varRefExpr.pos;

        if (varRefExpr.lhsVar || !types.isValueType(genVarRefExpr.type)) {
            result = genVarRefExpr;
            return;
        }

        // If the the variable is not used in lhs, and if the current type
        // is a value type, then add a conversion if required. This is done
        // to unbox a narrowed type.
        BType targetType = genVarRefExpr.type;
        genVarRefExpr.type = genVarRefExpr.symbol.type;
        result = addConversionExprIfRequired(genVarRefExpr, targetType);
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        if (safeNavigate(fieldAccessExpr)) {
            result = rewriteExpr(rewriteSafeNavigationExpr(fieldAccessExpr));
            return;
        }

        BLangVariableReference targetVarRef = fieldAccessExpr;
        fieldAccessExpr.expr = rewriteExpr(fieldAccessExpr.expr);
        BLangLiteral stringLit = createStringLiteral(fieldAccessExpr.pos, fieldAccessExpr.field.value);
        BType varRefType = fieldAccessExpr.expr.type;
        if (varRefType.tag == TypeTags.OBJECT) {
            if (fieldAccessExpr.symbol != null && fieldAccessExpr.symbol.type.tag == TypeTags.INVOKABLE &&
                    ((fieldAccessExpr.symbol.flags & Flags.ATTACHED) == Flags.ATTACHED)) {
                targetVarRef = new BLangStructFunctionVarRef(fieldAccessExpr.expr, (BVarSymbol) fieldAccessExpr.symbol);
            } else {
                targetVarRef = new BLangStructFieldAccessExpr(fieldAccessExpr.pos, fieldAccessExpr.expr, stringLit,
                        (BVarSymbol) fieldAccessExpr.symbol, false);

                // expr symbol is null when their is a array as the field
                if (!enclLocks.isEmpty() && (((BLangVariableReference) fieldAccessExpr.expr).symbol != null)) {
                    enclLocks.peek().addFieldVariable((BLangStructFieldAccessExpr) targetVarRef);
                }
            }
        } else if (varRefType.tag == TypeTags.RECORD) {
            if (fieldAccessExpr.symbol != null && fieldAccessExpr.symbol.type.tag == TypeTags.INVOKABLE
                    && ((fieldAccessExpr.symbol.flags & Flags.ATTACHED) == Flags.ATTACHED)) {
                targetVarRef = new BLangStructFunctionVarRef(((BLangVariableReference) fieldAccessExpr.expr),
                        (BVarSymbol) fieldAccessExpr.symbol);
            } else {
                targetVarRef = new BLangStructFieldAccessExpr(fieldAccessExpr.pos, fieldAccessExpr.expr, stringLit,
                        (BVarSymbol) fieldAccessExpr.symbol, true);

                // expr symbol is null when their is a array as the field
                if (!enclLocks.isEmpty() && (((BLangVariableReference) fieldAccessExpr.expr).symbol != null)) {
                    enclLocks.peek().addFieldVariable((BLangStructFieldAccessExpr) targetVarRef);
                }
            }
        } else if (varRefType.tag == TypeTags.MAP) {
            targetVarRef = new BLangMapAccessExpr(fieldAccessExpr.pos, fieldAccessExpr.expr,
                    stringLit);
        } else if (varRefType.tag == TypeTags.JSON) {
            targetVarRef = new BLangJSONAccessExpr(fieldAccessExpr.pos, fieldAccessExpr.expr,
                    stringLit);
        } else if (varRefType.tag == TypeTags.XML) {
            targetVarRef = new BLangXMLAccessExpr(fieldAccessExpr.pos, fieldAccessExpr.expr,
                    stringLit, fieldAccessExpr.fieldKind);
        }

        targetVarRef.lhsVar = fieldAccessExpr.lhsVar;
        targetVarRef.type = fieldAccessExpr.type;
        result = targetVarRef;
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        if (safeNavigate(indexAccessExpr)) {
            result = rewriteExpr(rewriteSafeNavigationExpr(indexAccessExpr));
            return;
        }

        BLangVariableReference targetVarRef = indexAccessExpr;
        indexAccessExpr.indexExpr = rewriteExpr(indexAccessExpr.indexExpr);
        indexAccessExpr.expr = rewriteExpr(indexAccessExpr.expr);
        BType varRefType = indexAccessExpr.expr.type;
        if (varRefType.tag == TypeTags.OBJECT || varRefType.tag == TypeTags.RECORD) {
            targetVarRef = new BLangStructFieldAccessExpr(indexAccessExpr.pos,
                    (BLangVariableReference) indexAccessExpr.expr, indexAccessExpr.indexExpr,
                    (BVarSymbol) indexAccessExpr.symbol, false);
        } else if (varRefType.tag == TypeTags.MAP) {
            targetVarRef = new BLangMapAccessExpr(indexAccessExpr.pos, indexAccessExpr.expr,
                    indexAccessExpr.indexExpr, !indexAccessExpr.type.isNullable());
        } else if (varRefType.tag == TypeTags.JSON || getElementType(varRefType).tag == TypeTags.JSON) {
            targetVarRef = new BLangJSONAccessExpr(indexAccessExpr.pos, indexAccessExpr.expr,
                    indexAccessExpr.indexExpr);
        } else if (varRefType.tag == TypeTags.ARRAY) {
            targetVarRef = new BLangArrayAccessExpr(indexAccessExpr.pos, indexAccessExpr.expr,
                    indexAccessExpr.indexExpr);
        } else if (varRefType.tag == TypeTags.XML) {
            targetVarRef = new BLangXMLAccessExpr(indexAccessExpr.pos, indexAccessExpr.expr,
                    indexAccessExpr.indexExpr);
        } else if (varRefType.tag == TypeTags.TUPLE) {
            targetVarRef = new BLangTupleAccessExpr(indexAccessExpr.pos, indexAccessExpr.expr,
                    indexAccessExpr.indexExpr);
        }

        targetVarRef.lhsVar = indexAccessExpr.lhsVar;
        targetVarRef.type = indexAccessExpr.type;
        result = targetVarRef;
    }

    @Override
    public void visit(BLangInvocation iExpr) {
        BLangInvocation genIExpr = iExpr;

        if (safeNavigate(iExpr)) {
            result = rewriteExpr(rewriteSafeNavigationExpr(iExpr));
            return;
        }

        // Reorder the arguments to match the original function signature.
        reorderArguments(iExpr);
        iExpr.requiredArgs = rewriteExprs(iExpr.requiredArgs);
        iExpr.namedArgs = rewriteExprs(iExpr.namedArgs);
        iExpr.restArgs = rewriteExprs(iExpr.restArgs);

        if (iExpr.functionPointerInvocation) {
            visitFunctionPointerInvocation(iExpr);
            return;
        } else if (iExpr.iterableOperationInvocation) {
            visitIterableOperationInvocation(iExpr);
            return;
        }
        iExpr.expr = rewriteExpr(iExpr.expr);
        if (iExpr.builtinMethodInvocation) {
            visitBuiltInMethodInvocation(iExpr);
            return;
        }
        result = genIExpr;
        if (iExpr.expr == null) {
            if (iExpr.exprSymbol == null) {
                return;
            }
            iExpr.expr = ASTBuilderUtil.createVariableRef(iExpr.pos, iExpr.exprSymbol);
            iExpr.expr = rewriteExpr(iExpr.expr);
        }

        switch (iExpr.expr.type.tag) {
            case TypeTags.BOOLEAN:
            case TypeTags.STRING:
            case TypeTags.INT:
            case TypeTags.FLOAT:
            case TypeTags.DECIMAL:
            case TypeTags.JSON:
            case TypeTags.XML:
            case TypeTags.MAP:
            case TypeTags.TABLE:
            case TypeTags.STREAM:
            case TypeTags.FUTURE:
            case TypeTags.OBJECT:
            case TypeTags.RECORD:
                List<BLangExpression> argExprs = new ArrayList<>(iExpr.requiredArgs);
                argExprs.add(0, iExpr.expr);
                final BLangAttachedFunctionInvocation attachedFunctionInvocation =
                        new BLangAttachedFunctionInvocation(iExpr.pos, argExprs, iExpr.namedArgs, iExpr.restArgs,
                                iExpr.symbol, iExpr.type, iExpr.expr, iExpr.async);
                attachedFunctionInvocation.actionInvocation = iExpr.actionInvocation;
                result = attachedFunctionInvocation;
                break;
        }
    }

    public void visit(BLangTypeInit typeInitExpr) {
        switch (typeInitExpr.type.tag) {
            case TypeTags.STREAM:
            case TypeTags.CHANNEL:
                result = getInitExpr(typeInitExpr.type, typeInitExpr);
                break;
            default:
                if (typeInitExpr.type.tag == TypeTags.OBJECT && typeInitExpr.initInvocation.symbol == null) {
                    typeInitExpr.initInvocation.symbol =
                            ((BObjectTypeSymbol) typeInitExpr.type.tsymbol).initializerFunc.symbol;
                }
                typeInitExpr.initInvocation = rewriteExpr(typeInitExpr.initInvocation);
                result = typeInitExpr;
        }
    }

    @Override
    public void visit(BLangTernaryExpr ternaryExpr) {
        ternaryExpr.expr = rewriteExpr(ternaryExpr.expr);
        ternaryExpr.thenExpr = rewriteExpr(ternaryExpr.thenExpr);
        ternaryExpr.elseExpr = rewriteExpr(ternaryExpr.elseExpr);
        result = ternaryExpr;
    }

    @Override
    public void visit(BLangWaitExpr waitExpr) {
        // Wait for any
        if (waitExpr.getExpression().getKind() == NodeKind.BINARY_EXPR) {
            waitExpr.exprList = collectAllBinaryExprs((BLangBinaryExpr) waitExpr.getExpression(), new ArrayList<>());
        } else { // Wait for one
            waitExpr.exprList = Collections.singletonList(rewriteExpr(waitExpr.getExpression()));
        }
        result = waitExpr;
    }

    private List<BLangExpression> collectAllBinaryExprs(BLangBinaryExpr binaryExpr, List<BLangExpression> exprs) {
        visitBinaryExprOfWait(binaryExpr.lhsExpr, exprs);
        visitBinaryExprOfWait(binaryExpr.rhsExpr, exprs);
        return exprs;
    }

    private void visitBinaryExprOfWait(BLangExpression expr, List<BLangExpression> exprs) {
        if (expr.getKind() == NodeKind.BINARY_EXPR) {
            collectAllBinaryExprs((BLangBinaryExpr) expr, exprs);
        } else {
            expr = rewriteExpr(expr);
            exprs.add(expr);
        }
    }

    @Override
    public void visit(BLangWaitForAllExpr waitExpr) {
        waitExpr.keyValuePairs.forEach(keyValue -> {
            if (keyValue.valueExpr != null) {
                keyValue.valueExpr = rewriteExpr(keyValue.valueExpr);
            } else {
                keyValue.keyExpr = rewriteExpr(keyValue.keyExpr);
            }
        });
        BLangExpression expr = new BLangWaitForAllExpr.BLangWaitLiteral(waitExpr.keyValuePairs, waitExpr.type);
        result = rewriteExpr(expr);
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        trapExpr.expr = rewriteExpr(trapExpr.expr);
        result = trapExpr;
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        if (binaryExpr.opKind == OperatorKind.HALF_OPEN_RANGE) {
            binaryExpr.rhsExpr = getModifiedIntRangeEndExpr(binaryExpr.rhsExpr);
        }

        binaryExpr.lhsExpr = rewriteExpr(binaryExpr.lhsExpr);
        binaryExpr.rhsExpr = rewriteExpr(binaryExpr.rhsExpr);
        result = binaryExpr;

        int rhsExprTypeTag = binaryExpr.rhsExpr.type.tag;
        int lhsExprTypeTag = binaryExpr.lhsExpr.type.tag;

        // Check for bitwise shift operator and add type conversion to int
        if (isBitwiseShiftOperation(binaryExpr) && TypeTags.BYTE == rhsExprTypeTag) {
            binaryExpr.rhsExpr = createTypeCastExpr(binaryExpr.rhsExpr, binaryExpr.rhsExpr.type,
                                                    symTable.intType);
            return;
        }

        // Check for int and byte ==, != or === comparison and add type conversion to int for byte
        if (rhsExprTypeTag != lhsExprTypeTag && (binaryExpr.opKind == OperatorKind.EQUAL ||
                                                         binaryExpr.opKind == OperatorKind.NOT_EQUAL ||
                                                         binaryExpr.opKind == OperatorKind.REF_EQUAL ||
                                                         binaryExpr.opKind == OperatorKind.REF_NOT_EQUAL)) {
            if (lhsExprTypeTag == TypeTags.INT && rhsExprTypeTag == TypeTags.BYTE) {
                binaryExpr.rhsExpr = createTypeCastExpr(binaryExpr.rhsExpr, binaryExpr.rhsExpr.type,
                                                        symTable.intType);
                return;
            }

            if (lhsExprTypeTag == TypeTags.BYTE && rhsExprTypeTag == TypeTags.INT) {
                binaryExpr.lhsExpr = createTypeCastExpr(binaryExpr.lhsExpr, binaryExpr.lhsExpr.type,
                                                        symTable.intType);
                return;
            }
        }

        // Check lhs and rhs type compatibility
        if (lhsExprTypeTag == rhsExprTypeTag) {
            return;
        }

        if (lhsExprTypeTag == TypeTags.STRING && binaryExpr.opKind == OperatorKind.ADD) {
            binaryExpr.rhsExpr = createTypeCastExpr(binaryExpr.rhsExpr, binaryExpr.rhsExpr.type,
                                                    binaryExpr.lhsExpr.type);
            return;
        }

        if (rhsExprTypeTag == TypeTags.STRING && binaryExpr.opKind == OperatorKind.ADD) {
            binaryExpr.lhsExpr = createTypeCastExpr(binaryExpr.lhsExpr, binaryExpr.lhsExpr.type,
                                                    binaryExpr.rhsExpr.type);
            return;
        }

        if (lhsExprTypeTag == TypeTags.DECIMAL) {
            binaryExpr.rhsExpr = createTypeCastExpr(binaryExpr.rhsExpr, binaryExpr.rhsExpr.type,
                                                    binaryExpr.lhsExpr.type);
            return;
        }

        if (rhsExprTypeTag == TypeTags.DECIMAL) {
            binaryExpr.lhsExpr = createTypeCastExpr(binaryExpr.lhsExpr, binaryExpr.lhsExpr.type,
                                                    binaryExpr.rhsExpr.type);
            return;
        }

        if (lhsExprTypeTag == TypeTags.FLOAT) {
            binaryExpr.rhsExpr = createTypeCastExpr(binaryExpr.rhsExpr, binaryExpr.rhsExpr.type,
                                                    binaryExpr.lhsExpr.type);
            return;
        }

        if (rhsExprTypeTag == TypeTags.FLOAT) {
            binaryExpr.lhsExpr = createTypeCastExpr(binaryExpr.lhsExpr, binaryExpr.lhsExpr.type,
                                                    binaryExpr.rhsExpr.type);
        }
    }

    /**
     * This method checks whether given binary expression is related to shift operation.
     * If its true, then both lhs and rhs of the binary expression will be converted to 'int' type.
     * <p>
     * byte a = 12;
     * byte b = 34;
     * int i = 234;
     * int j = -4;
     * <p>
     * true: where binary expression's expected type is 'int'
     * int i1 = a >> b;
     * int i2 = a << b;
     * int i3 = a >> i;
     * int i4 = a << i;
     * int i5 = i >> j;
     * int i6 = i << j;
     */
    private boolean isBitwiseShiftOperation(BLangBinaryExpr binaryExpr) {
        return binaryExpr.opKind == OperatorKind.BITWISE_LEFT_SHIFT ||
                binaryExpr.opKind == OperatorKind.BITWISE_RIGHT_SHIFT ||
                binaryExpr.opKind == OperatorKind.BITWISE_UNSIGNED_RIGHT_SHIFT;
    }

    public void visit(BLangElvisExpr elvisExpr) {
        BLangMatchExpression matchExpr = ASTBuilderUtil.createMatchExpression(elvisExpr.lhsExpr);
        matchExpr.patternClauses.add(getMatchNullPatternGivenExpression(elvisExpr.pos,
                rewriteExpr(elvisExpr.rhsExpr)));
        matchExpr.type = elvisExpr.type;
        matchExpr.pos = elvisExpr.pos;
        result = rewriteExpr(matchExpr);
    }

    @Override
    public void visit(BLangBracedOrTupleExpr bracedOrTupleExpr) {
        if (bracedOrTupleExpr.isTypedescExpr) {
            final BLangTypedescExpr typedescExpr = new BLangTypedescExpr();
            typedescExpr.resolvedType = bracedOrTupleExpr.typedescType;
            typedescExpr.type = symTable.typeDesc;
            result = rewriteExpr(typedescExpr);
            return;
        }
        if (bracedOrTupleExpr.isBracedExpr) {
            result = rewriteExpr(bracedOrTupleExpr.expressions.get(0));
            return;
        }
        bracedOrTupleExpr.expressions.forEach(expr -> {
            BType expType = expr.impConversionExpr == null ? expr.type : expr.impConversionExpr.type;
            types.setImplicitCastExpr(expr, expType, symTable.anyType);
        });
        bracedOrTupleExpr.expressions = rewriteExprs(bracedOrTupleExpr.expressions);
        result = bracedOrTupleExpr;
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        if (OperatorKind.BITWISE_COMPLEMENT == unaryExpr.operator) {
            // If this is a bitwise complement (~) expression, then we desugar it to a binary xor expression with -1,
            // which is same as doing a bitwise 2's complement operation.
            rewriteBitwiseComplementOperator(unaryExpr);
            return;
        }
        unaryExpr.expr = rewriteExpr(unaryExpr.expr);
        result = unaryExpr;
    }

    /**
     * This method desugar a bitwise complement (~) unary expressions into a bitwise xor binary expression as below.
     * Example : ~a  -> a ^ -1;
     * ~ 11110011 -> 00001100
     * 11110011 ^ 11111111 -> 00001100
     *
     * @param unaryExpr the bitwise complement expression
     */
    private void rewriteBitwiseComplementOperator(BLangUnaryExpr unaryExpr) {
        final DiagnosticPos pos = unaryExpr.pos;
        final BLangBinaryExpr binaryExpr = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        binaryExpr.pos = pos;
        binaryExpr.opKind = OperatorKind.BITWISE_XOR;
        binaryExpr.lhsExpr = unaryExpr.expr;
        if (TypeTags.BYTE == unaryExpr.type.tag) {
            binaryExpr.type = symTable.byteType;
            binaryExpr.rhsExpr = ASTBuilderUtil.createLiteral(pos, symTable.byteType, (byte) -1);
            binaryExpr.opSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.BITWISE_XOR,
                    symTable.byteType, symTable.byteType);
        } else {
            binaryExpr.type = symTable.intType;
            binaryExpr.rhsExpr = ASTBuilderUtil.createLiteral(pos, symTable.intType, -1L);
            binaryExpr.opSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.BITWISE_XOR,
                    symTable.intType, symTable.intType);
        }
        result = rewriteExpr(binaryExpr);
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        conversionExpr.expr = rewriteExpr(conversionExpr.expr);
        result = conversionExpr;
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        result = bLangLambdaFunction;
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        BLangFunction bLangFunction = (BLangFunction) TreeBuilder.createFunctionNode();
        bLangFunction.setName(bLangArrowFunction.functionName);

        BLangLambdaFunction lambdaFunction = (BLangLambdaFunction) TreeBuilder.createLambdaFunctionNode();
        lambdaFunction.pos = bLangArrowFunction.pos;
        bLangFunction.addFlag(Flag.LAMBDA);
        lambdaFunction.function = bLangFunction;

        // Create function body with return node
        BLangValueType returnType = (BLangValueType) TreeBuilder.createValueTypeNode();
        returnType.type = bLangArrowFunction.expression.type;
        bLangFunction.setReturnTypeNode(returnType);
        bLangFunction.setBody(populateArrowExprBodyBlock(bLangArrowFunction));

        bLangArrowFunction.params.forEach(bLangFunction::addParameter);
        lambdaFunction.parent = bLangArrowFunction.parent;
        lambdaFunction.type = bLangArrowFunction.funcType;

        // Create function symbol
        BLangFunction function = lambdaFunction.function;
        BInvokableSymbol functionSymbol = Symbols.createFunctionSymbol(Flags.asMask(lambdaFunction.function.flagSet),
                new Name(function.name.value), env.enclPkg.packageID, function.type, env.enclEnv.enclVarSym, true);
        functionSymbol.retType = function.returnTypeNode.type;
        functionSymbol.params = function.requiredParams.stream()
                .map(param -> param.symbol)
                .collect(Collectors.toList());
        functionSymbol.scope = env.scope;
        functionSymbol.type = bLangArrowFunction.funcType;
        function.symbol = functionSymbol;

        lambdaFunction.function.closureVarSymbols = bLangArrowFunction.closureVarSymbols;
        rewrite(lambdaFunction.function, env);
        env.enclPkg.addFunction(lambdaFunction.function);
        bLangArrowFunction.function = lambdaFunction.function;
        result = lambdaFunction;
    }

    @Override
    public void visit(BLangXMLQName xmlQName) {
        result = xmlQName;
    }

    @Override
    public void visit(BLangXMLAttribute xmlAttribute) {
        xmlAttribute.name = rewriteExpr(xmlAttribute.name);
        xmlAttribute.value = rewriteExpr(xmlAttribute.value);
        result = xmlAttribute;
    }

    @Override
    public void visit(BLangXMLElementLiteral xmlElementLiteral) {
        xmlElementLiteral.startTagName = rewriteExpr(xmlElementLiteral.startTagName);
        xmlElementLiteral.endTagName = rewriteExpr(xmlElementLiteral.endTagName);
        xmlElementLiteral.modifiedChildren = rewriteExprs(xmlElementLiteral.modifiedChildren);
        xmlElementLiteral.attributes = rewriteExprs(xmlElementLiteral.attributes);

        // Separate the in-line namepsace declarations and attributes.
        Iterator<BLangXMLAttribute> attributesItr = xmlElementLiteral.attributes.iterator();
        while (attributesItr.hasNext()) {
            BLangXMLAttribute attribute = attributesItr.next();
            if (!attribute.isNamespaceDeclr) {
                continue;
            }

            // Create namepace declaration for all in-line namespace declarations
            BLangXMLNS xmlns;
            if ((xmlElementLiteral.scope.owner.tag & SymTag.PACKAGE) == SymTag.PACKAGE) {
                xmlns = new BLangPackageXMLNS();
            } else {
                xmlns = new BLangLocalXMLNS();
            }
            xmlns.namespaceURI = attribute.value.concatExpr;
            xmlns.prefix = ((BLangXMLQName) attribute.name).localname;
            xmlns.symbol = attribute.symbol;

            xmlElementLiteral.inlineNamespaces.add(xmlns);
            attributesItr.remove();
        }

        result = xmlElementLiteral;
    }

    @Override
    public void visit(BLangXMLTextLiteral xmlTextLiteral) {
        xmlTextLiteral.concatExpr = rewriteExpr(xmlTextLiteral.concatExpr);
        result = xmlTextLiteral;
    }

    @Override
    public void visit(BLangXMLCommentLiteral xmlCommentLiteral) {
        xmlCommentLiteral.concatExpr = rewriteExpr(xmlCommentLiteral.concatExpr);
        result = xmlCommentLiteral;
    }

    @Override
    public void visit(BLangXMLProcInsLiteral xmlProcInsLiteral) {
        xmlProcInsLiteral.target = rewriteExpr(xmlProcInsLiteral.target);
        xmlProcInsLiteral.dataConcatExpr = rewriteExpr(xmlProcInsLiteral.dataConcatExpr);
        result = xmlProcInsLiteral;
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        xmlQuotedString.concatExpr = rewriteExpr(xmlQuotedString.concatExpr);
        result = xmlQuotedString;
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        stringTemplateLiteral.concatExpr = rewriteExpr(stringTemplateLiteral.concatExpr);
        result = stringTemplateLiteral;
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        List<BLangExpression> list = Lists.of(rewriteExpr(workerSendNode.expr));
        workerSendNode.expr = appendCloneMethod(workerSendNode.expr.pos, list);
        if (workerSendNode.keyExpr != null) {
            workerSendNode.keyExpr = rewriteExpr(workerSendNode.keyExpr);
        }
        result = workerSendNode;
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        List<BLangExpression> list = Lists.of(rewriteExpr(syncSendExpr.expr));
        syncSendExpr.expr = appendCloneMethod(syncSendExpr.expr.pos, list);
        result = syncSendExpr;
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        if (workerReceiveNode.keyExpr != null) {
            workerReceiveNode.keyExpr = rewriteExpr(workerReceiveNode.keyExpr);
        }
        result = workerReceiveNode;
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        workerFlushExpr.workerIdentifierList = workerFlushExpr.cachedWorkerSendStmts
                .stream().map(send -> send.workerIdentifier).distinct().collect(Collectors.toList());
        result = workerFlushExpr;
    }

    @Override
    public void visit(BLangXMLAttributeAccess xmlAttributeAccessExpr) {
        xmlAttributeAccessExpr.indexExpr = rewriteExpr(xmlAttributeAccessExpr.indexExpr);
        xmlAttributeAccessExpr.expr = rewriteExpr(xmlAttributeAccessExpr.expr);

        if (xmlAttributeAccessExpr.indexExpr != null
                && xmlAttributeAccessExpr.indexExpr.getKind() == NodeKind.XML_QNAME) {
            ((BLangXMLQName) xmlAttributeAccessExpr.indexExpr).isUsedInXML = true;
        }

        result = xmlAttributeAccessExpr;
    }

    // Generated expressions. Following expressions are not part of the original syntax
    // tree which is coming out of the parser

    @Override
    public void visit(BLangLocalVarRef localVarRef) {
        result = localVarRef;
    }

    @Override
    public void visit(BLangFieldVarRef fieldVarRef) {
        result = fieldVarRef;
    }

    @Override
    public void visit(BLangPackageVarRef packageVarRef) {
        result = packageVarRef;
    }

    @Override
    public void visit(BLangFunctionVarRef functionVarRef) {
        result = functionVarRef;
    }

    @Override
    public void visit(BLangStructFieldAccessExpr fieldAccessExpr) {
        BType expType = fieldAccessExpr.type;
        fieldAccessExpr.type = symTable.anyType;
        result = addConversionExprIfRequired(fieldAccessExpr, expType);
    }

    @Override
    public void visit(BLangStructFunctionVarRef functionVarRef) {
        result = functionVarRef;
    }

    @Override
    public void visit(BLangMapAccessExpr mapKeyAccessExpr) {
        result = mapKeyAccessExpr;
    }

    @Override
    public void visit(BLangArrayAccessExpr arrayIndexAccessExpr) {
        result = arrayIndexAccessExpr;
    }

    @Override
    public void visit(BLangTupleAccessExpr arrayIndexAccessExpr) {
        result = arrayIndexAccessExpr;
    }

    @Override
    public void visit(BLangJSONLiteral jsonLiteral) {
        result = jsonLiteral;
    }

    @Override
    public void visit(BLangMapLiteral mapLiteral) {
        result = mapLiteral;
    }

    public void visit(BLangStreamLiteral streamLiteral) {
        result = streamLiteral;
    }

    @Override
    public void visit(BLangStructLiteral structLiteral) {
        result = structLiteral;
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {
        result = waitLiteral;
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        assignableExpr.lhsExpr = rewriteExpr(assignableExpr.lhsExpr);
        result = assignableExpr;
    }

    @Override
    public void visit(BFunctionPointerInvocation fpInvocation) {
        result = fpInvocation;
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {
        result = accessExpr;
    }

    @Override
    public void visit(BLangIntRangeExpression intRangeExpression) {
        if (!intRangeExpression.includeStart) {
            intRangeExpression.startExpr = getModifiedIntRangeStartExpr(intRangeExpression.startExpr);
        }
        if (!intRangeExpression.includeEnd) {
            intRangeExpression.endExpr = getModifiedIntRangeEndExpr(intRangeExpression.endExpr);
        }

        intRangeExpression.startExpr = rewriteExpr(intRangeExpression.startExpr);
        intRangeExpression.endExpr = rewriteExpr(intRangeExpression.endExpr);
        result = intRangeExpression;
    }

    @Override
    public void visit(BLangRestArgsExpression bLangVarArgsExpression) {
        result = rewriteExpr(bLangVarArgsExpression.expr);
    }

    @Override
    public void visit(BLangNamedArgsExpression bLangNamedArgsExpression) {
        bLangNamedArgsExpression.expr = rewriteExpr(bLangNamedArgsExpression.expr);
        result = bLangNamedArgsExpression.expr;
    }

    public void visit(BLangTableQueryExpression tableQueryExpression) {
        inMemoryTableQueryBuilder.visit(tableQueryExpression);

        /*replace the table expression with a function invocation,
         so that we manually call a native function "queryTable". */
        result = createInvocationFromTableExpr(tableQueryExpression);
    }

    @Override
    public void visit(BLangMatchExpression bLangMatchExpression) {
        // Add the implicit default pattern, that returns the original expression's value.
        addMatchExprDefaultCase(bLangMatchExpression);

        // Create a temp local var to hold the temp result of the match expression
        // eg: T a;
        String matchTempResultVarName = GEN_VAR_PREFIX.value + "temp_result";
        BLangSimpleVariable tempResultVar = ASTBuilderUtil.createVariable(bLangMatchExpression.pos,
                matchTempResultVarName, bLangMatchExpression.type, null,
                new BVarSymbol(0, names.fromString(matchTempResultVarName), this.env.scope.owner.pkgID,
                        bLangMatchExpression.type, this.env.scope.owner));

        BLangSimpleVariableDef tempResultVarDef =
                ASTBuilderUtil.createVariableDef(bLangMatchExpression.pos, tempResultVar);
        tempResultVarDef.desugared = true;

        BLangBlockStmt stmts = ASTBuilderUtil.createBlockStmt(bLangMatchExpression.pos, Lists.of(tempResultVarDef));
        List<BLangMatchTypedBindingPatternClause> patternClauses = new ArrayList<>();

        for (int i = 0; i < bLangMatchExpression.patternClauses.size(); i++) {
            BLangMatchExprPatternClause pattern = bLangMatchExpression.patternClauses.get(i);
            pattern.expr = rewriteExpr(pattern.expr);

            // Create var ref for the temp result variable
            // eg: var ref for 'a'
            BLangVariableReference tempResultVarRef =
                    ASTBuilderUtil.createVariableRef(bLangMatchExpression.pos, tempResultVar.symbol);

            // Create an assignment node. Add a conversion from rhs to lhs of the pattern, if required.
            pattern.expr = addConversionExprIfRequired(pattern.expr, tempResultVarRef.type);
            BLangAssignment assignmentStmt =
                    ASTBuilderUtil.createAssignmentStmt(pattern.pos, tempResultVarRef, pattern.expr);
            BLangBlockStmt patternBody = ASTBuilderUtil.createBlockStmt(pattern.pos, Lists.of(assignmentStmt));

            // Create the pattern
            // R b => a = b;
            patternClauses.add(ASTBuilderUtil.createMatchStatementPattern(pattern.pos, pattern.variable, patternBody));
        }

        stmts.addStatement(ASTBuilderUtil.createMatchStatement(bLangMatchExpression.pos, bLangMatchExpression.expr,
                patternClauses));
        BLangVariableReference tempResultVarRef =
                ASTBuilderUtil.createVariableRef(bLangMatchExpression.pos, tempResultVar.symbol);
        BLangStatementExpression statementExpr = ASTBuilderUtil.createStatementExpression(stmts, tempResultVarRef);
        statementExpr.type = bLangMatchExpression.type;
        result = rewriteExpr(statementExpr);
    }

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {

        //
        //  person p = bar(check foo()); // foo(): person | error
        //
        //    ==>
        //
        //  person _$$_;
        //  switch foo() {
        //      person p1 => _$$_ = p1;
        //      error e1 => return e1 or throw e1
        //  }
        //  person p = bar(_$$_);

        // Create a temporary variable to hold the checked expression result value e.g. _$$_
        String checkedExprVarName = GEN_VAR_PREFIX.value;
        BLangSimpleVariable checkedExprVar = ASTBuilderUtil.createVariable(checkedExpr.pos,
                checkedExprVarName, checkedExpr.type, null, new BVarSymbol(0,
                        names.fromString(checkedExprVarName),
                        this.env.scope.owner.pkgID, checkedExpr.type, this.env.scope.owner));
        BLangSimpleVariableDef checkedExprVarDef = ASTBuilderUtil.createVariableDef(checkedExpr.pos, checkedExprVar);
        checkedExprVarDef.desugared = true;

        // Create the pattern to match the success case
        BLangMatchTypedBindingPatternClause patternSuccessCase =
                getSafeAssignSuccessPattern(checkedExprVar.pos, checkedExprVar.symbol.type, true,
                        checkedExprVar.symbol, null);
        BLangMatchTypedBindingPatternClause patternErrorCase =
                getSafeAssignErrorPattern(checkedExpr.pos, this.env.scope.owner, checkedExpr.equivalentErrorTypeList);

        // Create the match statement
        BLangMatch matchStmt = ASTBuilderUtil.createMatchStatement(checkedExpr.pos, checkedExpr.expr,
                new ArrayList<BLangMatchTypedBindingPatternClause>() {{
                    add(patternSuccessCase);
                    add(patternErrorCase);
                }});

        // Create the block statement
        BLangBlockStmt generatedStmtBlock = ASTBuilderUtil.createBlockStmt(checkedExpr.pos,
                new ArrayList<BLangStatement>() {{
                    add(checkedExprVarDef);
                    add(matchStmt);
                }});

        // Create the variable ref expression for the checkedExprVar
        BLangSimpleVarRef tempCheckedExprVarRef = ASTBuilderUtil.createVariableRef(
                checkedExpr.pos, checkedExprVar.symbol);

        BLangStatementExpression statementExpr = ASTBuilderUtil.createStatementExpression(
                generatedStmtBlock, tempCheckedExprVarRef);
        statementExpr.type = checkedExpr.type;
        result = rewriteExpr(statementExpr);
    }
    @Override
    public void visit(BLangErrorConstructorExpr errConstExpr) {
        errConstExpr.reasonExpr = rewriteExpr(errConstExpr.reasonExpr);
        errConstExpr.detailsExpr = rewriteExpr(Optional.ofNullable(errConstExpr.detailsExpr)
                .orElseGet(() -> ASTBuilderUtil.createEmptyRecordLiteral(errConstExpr.pos, symTable.mapType)));
        result = errConstExpr;
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        final BLangTypeInit typeInit = ASTBuilderUtil.createEmptyTypeInit(serviceConstructorExpr.pos,
                serviceConstructorExpr.serviceNode.serviceTypeDefinition.symbol.type);
        result = rewriteExpr(typeInit);
    }

    @Override
    public void visit(BLangTypeTestExpr typeTestExpr) {
        typeTestExpr.expr = rewriteExpr(typeTestExpr.expr);
        result = typeTestExpr;
    }

    @Override
    public void visit(BLangIsLikeExpr isLikeExpr) {
        isLikeExpr.expr = rewriteExpr(isLikeExpr.expr);
        result = isLikeExpr;
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
        bLangStatementExpression.expr = rewriteExpr(bLangStatementExpression.expr);
        bLangStatementExpression.stmt = rewrite(bLangStatementExpression.stmt, env);
        result = bLangStatementExpression;
    }

    @Override
    public void visit(BLangJSONArrayLiteral jsonArrayLiteral) {
        jsonArrayLiteral.exprs = rewriteExprs(jsonArrayLiteral.exprs);
        result = jsonArrayLiteral;
    }

    // private functions

    // Foreach desugar helper method.
    private BLangSimpleVariableDef getIteratorVariableDefinition(BLangForeach foreach, BVarSymbol collectionSymbol,
                                                                 BVarSymbol iteratorSymbol) {
        BLangIdentifier iterateIdentifier = ASTBuilderUtil.createIdentifier(foreach.pos, "iterate");
        BLangSimpleVarRef dataReference = ASTBuilderUtil.createVariableRef(foreach.pos, collectionSymbol);

        BLangInvocation iteratorInvocation = (BLangInvocation) TreeBuilder.createInvocationNode();
        iteratorInvocation.pos = foreach.pos;
        iteratorInvocation.name = iterateIdentifier;
        iteratorInvocation.builtinMethodInvocation = true;
        iteratorInvocation.builtInMethod = BLangBuiltInMethod.ITERATE;
        iteratorInvocation.expr = dataReference;
        LinkedList<BType> paramTypes = new LinkedList<>();
        paramTypes.add(collectionSymbol.type);
        iteratorInvocation.symbol = symTable.createOperator(names.fromIdNode(iterateIdentifier), paramTypes,
                symTable.anyType, InstructionCodes.ITR_NEW);
        iteratorInvocation.type = symTable.intType;

        // Note - any $iterator$ = $data$.iterate();
        BLangSimpleVariable iteratorVariable = ASTBuilderUtil.createVariable(foreach.pos, "$iterator$",
                symTable.anyType, iteratorInvocation, iteratorSymbol);
        return ASTBuilderUtil.createVariableDef(foreach.pos, iteratorVariable);
    }

    // Foreach desugar helper method.
    private BLangSimpleVariableDef getIteratorNextVariableDefinition(BLangForeach foreach, BVarSymbol collectionSymbol,
                                                                     BVarSymbol iteratorSymbol,
                                                                     BVarSymbol resultSymbol) {
        BLangIdentifier nextIdentifier = ASTBuilderUtil.createIdentifier(foreach.pos, "next");
        BLangSimpleVarRef iteratorReferenceInNext = ASTBuilderUtil.createVariableRef(foreach.pos, iteratorSymbol);

        BLangInvocation nextInvocation = (BLangInvocation) TreeBuilder.createInvocationNode();
        nextInvocation.pos = foreach.pos;
        nextInvocation.name = nextIdentifier;
        nextInvocation.builtinMethodInvocation = true;
        nextInvocation.builtInMethod = BLangBuiltInMethod.NEXT;
        nextInvocation.expr = iteratorReferenceInNext;

        LinkedList<BType> paramTypes = new LinkedList<>();
        paramTypes.add(collectionSymbol.type);

        nextInvocation.symbol = symTable.createOperator(names.fromIdNode(nextIdentifier), paramTypes, symTable.anyType,
                InstructionCodes.ITR_NEXT);

        nextInvocation.type = foreach.nillableResultType;
        nextInvocation.originalType = foreach.nillableResultType;

        BLangSimpleVariable resultVariable = ASTBuilderUtil.createVariable(foreach.pos, "$result$",
                foreach.nillableResultType, nextInvocation, resultSymbol);
        return ASTBuilderUtil.createVariableDef(foreach.pos, resultVariable);
    }

    // Foreach desugar helper method.
    private BLangAssignment getIteratorNextAssignment(BLangForeach foreach, BVarSymbol collectionSymbol,
                                                      BVarSymbol iteratorSymbol, BVarSymbol resultSymbol) {
        BLangIdentifier nextIdentifier = ASTBuilderUtil.createIdentifier(foreach.pos, "next");

        BLangSimpleVarRef resultReferenceInAssignment = ASTBuilderUtil.createVariableRef(foreach.pos, resultSymbol);
        BLangSimpleVarRef iteratorReferenceAssignment = ASTBuilderUtil.createVariableRef(foreach.pos, iteratorSymbol);

        // Note - $iterator$.next();
        BLangInvocation nextInvocationInAssignment = (BLangInvocation) TreeBuilder.createInvocationNode();
        nextInvocationInAssignment.pos = foreach.pos;
        nextInvocationInAssignment.name = nextIdentifier;
        nextInvocationInAssignment.builtinMethodInvocation = true;
        nextInvocationInAssignment.builtInMethod = BLangBuiltInMethod.NEXT;
        nextInvocationInAssignment.expr = iteratorReferenceAssignment;
        LinkedList<BType> paramTypes = new LinkedList<>();
        paramTypes.add(collectionSymbol.type);
        nextInvocationInAssignment.symbol = symTable.createOperator(names.fromIdNode(nextIdentifier), paramTypes,
                symTable.anyType, InstructionCodes.ITR_NEXT);
        nextInvocationInAssignment.type = foreach.nillableResultType;
        nextInvocationInAssignment.originalType = foreach.nillableResultType;

        return ASTBuilderUtil.createAssignmentStmt(foreach.pos, resultReferenceInAssignment, nextInvocationInAssignment,
                false);
    }

    // Foreach desugar helper method.
    private BLangIf getIfStatement(BLangForeach foreach, BVarSymbol resultSymbol,
                                   BLangTypeTestExpr typeTestExpressionNode, BLangBlockStmt ifStatementBody) {
        BLangIf ifStatement = (BLangIf) TreeBuilder.createIfElseStatementNode();
        ifStatement.pos = foreach.pos;
        ifStatement.type = symTable.booleanType;
        ifStatement.expr = typeTestExpressionNode;
        ifStatement.body = ifStatementBody;
        return ifStatement;
    }

    // Foreach desugar helper method.
    private BLangTypeTestExpr getTypeTestExpression(BLangForeach foreach, BVarSymbol resultSymbol) {
        BLangSimpleVarRef resultReferenceInTypeTest = ASTBuilderUtil.createVariableRef(foreach.pos, resultSymbol);

        BLangValueType nilTypeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        nilTypeNode.pos = foreach.pos;
        nilTypeNode.type = symTable.nilType;
        nilTypeNode.typeKind = TypeKind.NIL;

        BLangTypeTestExpr typeTestExpressionNode = (BLangTypeTestExpr) TreeBuilder.createTypeTestExpressionNode();
        typeTestExpressionNode.pos = foreach.pos;
        typeTestExpressionNode.expr = resultReferenceInTypeTest;
        typeTestExpressionNode.typeNode = nilTypeNode;
        typeTestExpressionNode.type = symTable.booleanType;
        return typeTestExpressionNode;
    }

    // Foreach desugar helper method.
    private BLangFieldBasedAccess getValueAccessExpression(BLangForeach foreach, BVarSymbol resultSymbol) {
        BLangSimpleVarRef resultReferenceInVariableDef = ASTBuilderUtil.createVariableRef(foreach.pos, resultSymbol);
        BLangIdentifier valueIdentifier = ASTBuilderUtil.createIdentifier(foreach.pos, "value");

        BLangFieldBasedAccess fieldBasedAccessExpression =
                ASTBuilderUtil.createFieldAccessExpr(resultReferenceInVariableDef, valueIdentifier);
        fieldBasedAccessExpression.pos = foreach.pos;
        fieldBasedAccessExpression.type = foreach.varType;
        fieldBasedAccessExpression.originalType = foreach.varType;
        return fieldBasedAccessExpression;
    }

    private BlockNode populateArrowExprBodyBlock(BLangArrowFunction bLangArrowFunction) {
        BlockNode blockNode = TreeBuilder.createBlockNode();
        BLangReturn returnNode = (BLangReturn) TreeBuilder.createReturnNode();
        returnNode.pos = bLangArrowFunction.expression.pos;
        returnNode.setExpression(bLangArrowFunction.expression);
        blockNode.addStatement(returnNode);
        return blockNode;
    }

    private BLangInvocation createInvocationFromTableExpr(BLangTableQueryExpression tableQueryExpression) {
        List<BLangExpression> args = new ArrayList<>();
        String functionName = QUERY_TABLE_WITHOUT_JOIN_CLAUSE;
        //Order matters, because these are the args for a function invocation.
        args.add(getSQLPreparedStatement(tableQueryExpression));
        args.add(getFromTableVarRef(tableQueryExpression));
        // BLangTypeofExpr
        BType retType = tableQueryExpression.type;
        BLangSimpleVarRef joinTable = getJoinTableVarRef(tableQueryExpression);
        if (joinTable != null) {
            args.add(joinTable);
            functionName = QUERY_TABLE_WITH_JOIN_CLAUSE;
        }
        args.add(getSQLStatementParameters(tableQueryExpression));
        args.add(getReturnType(tableQueryExpression));
        return createInvocationNode(functionName, args, retType);
    }

    private BLangInvocation createInvocationNode(String functionName, List<BLangExpression> args, BType retType) {
        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        BLangIdentifier name = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        name.setLiteral(false);
        name.setValue(functionName);
        invocationNode.name = name;
        invocationNode.pkgAlias = (BLangIdentifier) TreeBuilder.createIdentifierNode();

        // TODO: 2/28/18 need to find a good way to refer to symbols
        invocationNode.symbol = symTable.rootScope.lookup(new Name(functionName)).symbol;
        invocationNode.type = retType;
        invocationNode.requiredArgs = args;
        return invocationNode;
    }

    private BLangLiteral getSQLPreparedStatement(BLangTableQueryExpression
                                                         tableQueryExpression) {
        //create a literal to represent the sql query.
        BLangLiteral sqlQueryLiteral = (BLangLiteral) TreeBuilder.createLiteralExpression();

        //assign the sql query from table expression to the literal.
        sqlQueryLiteral.value = tableQueryExpression.getSqlQuery();
        sqlQueryLiteral.type = symTable.stringType;
        return sqlQueryLiteral;
    }

    private BLangStructLiteral getReturnType(BLangTableQueryExpression
                                                     tableQueryExpression) {
        //create a literal to represent the sql query.
        BTableType tableType = (BTableType) tableQueryExpression.type;
        BStructureType structType = (BStructureType) tableType.constraint;
        return new BLangStructLiteral(new ArrayList<>(), structType);
    }

    private BLangArrayLiteral getSQLStatementParameters(BLangTableQueryExpression tableQueryExpression) {
        BLangArrayLiteral expr = createArrayLiteralExprNode();
        List<BLangExpression> params = tableQueryExpression.getParams();

        params.stream().map(param -> (BLangLiteral) param).forEach(literal -> {
            Object value = literal.getValue();
            int type = TypeTags.STRING;
            if (value instanceof Integer || value instanceof Long) {
                type = TypeTags.INT;
            } else if (value instanceof Double || value instanceof Float) {
                type = TypeTags.FLOAT;
            } else if (value instanceof Boolean) {
                type = TypeTags.BOOLEAN;
            } else if (value instanceof Object[]) {
                type = TypeTags.ARRAY;
            }
            literal.type = symTable.getTypeFromTag(type);
            types.setImplicitCastExpr(literal, new BType(type, null), symTable.anyType);
            expr.exprs.add(literal.impConversionExpr);
        });
        return expr;
    }

    private BLangArrayLiteral createArrayLiteralExprNode() {
        BLangArrayLiteral expr = (BLangArrayLiteral) TreeBuilder.createArrayLiteralNode();
        expr.exprs = new ArrayList<>();
        expr.type = new BArrayType(symTable.anyType);
        return expr;
    }

    private BLangSimpleVarRef getJoinTableVarRef(BLangTableQueryExpression tableQueryExpression) {
        JoinStreamingInput joinStreamingInput = tableQueryExpression.getTableQuery().getJoinStreamingInput();
        BLangSimpleVarRef joinTable = null;
        if (joinStreamingInput != null) {
            joinTable = (BLangSimpleVarRef) joinStreamingInput.getStreamingInput().getStreamReference();
            joinTable = rewrite(joinTable, env);
        }
        return joinTable;
    }

    private BLangSimpleVarRef getFromTableVarRef(BLangTableQueryExpression tableQueryExpression) {
        BLangSimpleVarRef fromTable = (BLangSimpleVarRef) tableQueryExpression.getTableQuery().getStreamingInput()
                .getStreamReference();
        return rewrite(fromTable, env);
    }

    private void visitFunctionPointerInvocation(BLangInvocation iExpr) {
        BLangVariableReference expr;
        if (iExpr.expr == null) {
            expr = new BLangSimpleVarRef();
        } else {
            BLangFieldBasedAccess fieldBasedAccess = new BLangFieldBasedAccess();
            fieldBasedAccess.expr = iExpr.expr;
            fieldBasedAccess.field = iExpr.name;
            expr = fieldBasedAccess;
        }
        expr.symbol = iExpr.symbol;
        expr.type = iExpr.symbol.type;
        expr = rewriteExpr(expr);
        result = new BFunctionPointerInvocation(iExpr, expr);
    }

    private void visitBuiltInMethodInvocation(BLangInvocation iExpr) {
        switch (iExpr.builtInMethod) {
            case IS_NAN:
                if (iExpr.expr.type.tag == TypeTags.FLOAT) {
                    BOperatorSymbol notEqSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(
                            OperatorKind.NOT_EQUAL, symTable.floatType, symTable.floatType);
                    BLangBinaryExpr binaryExprNaN = ASTBuilderUtil.createBinaryExpr(iExpr.pos, iExpr.expr, iExpr.expr,
                                                                                    symTable.booleanType,
                                                                                    OperatorKind.NOT_EQUAL,
                                                                                    notEqSymbol);
                    result = rewriteExpr(binaryExprNaN);
                } else {
                    BOperatorSymbol greaterEqualSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(
                            OperatorKind.GREATER_EQUAL, symTable.decimalType, symTable.decimalType);
                    BOperatorSymbol lessThanSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(
                            OperatorKind.LESS_THAN, symTable.decimalType, symTable.decimalType);
                    BOperatorSymbol orSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(
                            OperatorKind.OR, symTable.booleanType, symTable.booleanType);
                    BOperatorSymbol notSymbol = (BOperatorSymbol) symResolver.resolveUnaryOperator(
                            iExpr.pos, OperatorKind.NOT, symTable.booleanType);
                    BLangLiteral literalZero = ASTBuilderUtil.createLiteral(iExpr.pos, symTable.decimalType, "0");
                    // v >= 0
                    BLangBinaryExpr binaryExprLHS = ASTBuilderUtil.createBinaryExpr(iExpr.pos, iExpr.expr, literalZero,
                                                                                    symTable.booleanType,
                                                                                    OperatorKind.GREATER_EQUAL,
                                                                                    greaterEqualSymbol);
                    // v < 0
                    BLangBinaryExpr binaryExprRHS = ASTBuilderUtil.createBinaryExpr(iExpr.pos, iExpr.expr, literalZero,
                                                                                    symTable.booleanType,
                                                                                    OperatorKind.LESS_THAN,
                                                                                    lessThanSymbol);
                    // v >= 0 || v < 0
                    BLangBinaryExpr binaryExpr = ASTBuilderUtil.createBinaryExpr(iExpr.pos, binaryExprLHS,
                                                                                    binaryExprRHS,
                                                                                    symTable.booleanType,
                                                                                    OperatorKind.OR, orSymbol);
                    // Final expression: !(v >= 0 || v < 0)
                    BLangUnaryExpr finalExprNaN = ASTBuilderUtil.createUnaryExpr(iExpr.pos, binaryExpr,
                                                                                    symTable.booleanType,
                                                                                    OperatorKind.NOT, notSymbol);
                    result = rewriteExpr(finalExprNaN);
                }
                break;
            case IS_FINITE:
                if (iExpr.expr.type.tag == TypeTags.FLOAT) {
                    BOperatorSymbol equalSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(
                            OperatorKind.EQUAL, symTable.floatType, symTable.floatType);
                    BOperatorSymbol notEqualSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(
                            OperatorKind.NOT_EQUAL, symTable.floatType, symTable.floatType);
                    BOperatorSymbol andEqualSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(
                            OperatorKind.AND, symTable.booleanType, symTable.booleanType);
                    // v==v
                    BLangBinaryExpr binaryExprLHS = ASTBuilderUtil.createBinaryExpr(iExpr.pos, iExpr.expr, iExpr.expr,
                                                                                    symTable.booleanType,
                                                                                    OperatorKind.EQUAL, equalSymbol);
                    // v != positive_infinity
                    BLangLiteral posInfLiteral = ASTBuilderUtil.createLiteral(iExpr.pos, symTable.floatType,
                                                                                    Double.POSITIVE_INFINITY);
                    BLangBinaryExpr nestedLHSExpr = ASTBuilderUtil.createBinaryExpr(iExpr.pos, posInfLiteral,
                                                                                    iExpr.expr, symTable.booleanType,
                                                                                    OperatorKind.NOT_EQUAL,
                                                                                    notEqualSymbol);

                    // v != negative_infinity
                    BLangLiteral negInfLiteral = ASTBuilderUtil.createLiteral(iExpr.pos, symTable.floatType,
                                                                                    Double.NEGATIVE_INFINITY);
                    BLangBinaryExpr nestedRHSExpr = ASTBuilderUtil.createBinaryExpr(iExpr.pos, negInfLiteral,
                                                                                    iExpr.expr, symTable.booleanType,
                                                                                    OperatorKind.NOT_EQUAL,
                                                                                    notEqualSymbol);
                    // v != positive_infinity && v != negative_infinity
                    BLangBinaryExpr binaryExprRHS = ASTBuilderUtil.createBinaryExpr(iExpr.pos, nestedLHSExpr,
                                                                                    nestedRHSExpr,
                                                                                    symTable.booleanType,
                                                                                    OperatorKind.AND, andEqualSymbol);
                    // Final expression : v==v && v != positive_infinity && v != negative_infinity
                    BLangBinaryExpr binaryExpr = ASTBuilderUtil.createBinaryExpr(iExpr.pos, binaryExprLHS,
                                                                                    binaryExprRHS, symTable.booleanType,
                                                                                    OperatorKind.AND, andEqualSymbol);
                    result = rewriteExpr(binaryExpr);
                } else {
                    BOperatorSymbol isEqualSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(
                            OperatorKind.EQUAL, symTable.decimalType, symTable.decimalType);
                    // v == v
                    BLangBinaryExpr finalExprFinite = ASTBuilderUtil.createBinaryExpr(iExpr.pos, iExpr.expr, iExpr.expr,
                                                                                    symTable.booleanType,
                                                                                    OperatorKind.EQUAL, isEqualSymbol);
                    result = rewriteExpr(finalExprFinite);
                }
                break;
            case IS_INFINITE:
                if (iExpr.expr.type.tag == TypeTags.FLOAT) {
                    BOperatorSymbol eqSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(
                            OperatorKind.EQUAL, symTable.floatType, symTable.floatType);
                    BOperatorSymbol orSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(
                            OperatorKind.OR, symTable.booleanType, symTable.booleanType);
                    // v == positive_infinity
                    BLangLiteral posInflitExpr = ASTBuilderUtil.createLiteral(iExpr.pos, symTable.floatType,
                                                                                    Double.POSITIVE_INFINITY);
                    BLangBinaryExpr binaryExprPosInf = ASTBuilderUtil.createBinaryExpr(iExpr.pos, iExpr.expr,
                                                                                    posInflitExpr, symTable.booleanType,
                                                                                    OperatorKind.EQUAL, eqSymbol);
                    // v == negative_infinity
                    BLangLiteral negInflitExpr = ASTBuilderUtil.createLiteral(iExpr.pos, symTable.floatType,
                                                                                    Double.NEGATIVE_INFINITY);
                    BLangBinaryExpr binaryExprNegInf = ASTBuilderUtil.createBinaryExpr(iExpr.pos, iExpr.expr,
                                                                                    negInflitExpr, symTable.booleanType,
                                                                                    OperatorKind.EQUAL, eqSymbol);
                    // v == positive_infinity || v == negative_infinity
                    BLangBinaryExpr binaryExprInf = ASTBuilderUtil.createBinaryExpr(iExpr.pos, binaryExprPosInf,
                                                                                    binaryExprNegInf,
                                                                                    symTable.booleanType,
                                                                                    OperatorKind.OR, orSymbol);
                    result = rewriteExpr(binaryExprInf);
                } else {
                    BLangLiteral literalZero = ASTBuilderUtil.createLiteral(iExpr.pos, symTable.decimalType, "0");
                    BLangLiteral literalOne = ASTBuilderUtil.createLiteral(iExpr.pos, symTable.decimalType, "1");
                    BOperatorSymbol isEqualSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(
                            OperatorKind.EQUAL, symTable.decimalType, symTable.decimalType);
                    BOperatorSymbol divideSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(
                            OperatorKind.DIV, symTable.decimalType, symTable.decimalType);
                    // 1/v
                    BLangBinaryExpr divideExpr = ASTBuilderUtil.createBinaryExpr(iExpr.pos, literalOne, iExpr.expr,
                                                                                    symTable.decimalType,
                                                                                    OperatorKind.DIV, divideSymbol);
                    // Final expression: 1/v == 0
                    BLangBinaryExpr finalExprInf = ASTBuilderUtil.createBinaryExpr(iExpr.pos, divideExpr, literalZero,
                                                                                    symTable.booleanType,
                                                                                    OperatorKind.EQUAL, isEqualSymbol);
                    result = rewriteExpr(finalExprInf);
                }
                break;
            case CLONE:
                if (types.isValueType(iExpr.expr.type)) {
                    result = iExpr.expr;
                    break;
                }
                result = new BLangBuiltInMethodInvocation(iExpr, iExpr.builtInMethod);
                break;
            case FREEZE:
            case IS_FROZEN:
                visitFreezeBuiltInMethodInvocation(iExpr);
                break;
            case CONVERT:
                if (iExpr.symbol.kind == SymbolKind.CAST_OPERATOR) {
                    BCastOperatorSymbol symbol = (BCastOperatorSymbol) iExpr.symbol;
                    BInvokableType type = (BInvokableType) symbol.type;
                    result = createTypeCastExpr(appendCloneMethod(iExpr.pos, iExpr.requiredArgs),
                                                type.paramTypes.get(1), symbol);
                } else if (iExpr.symbol.kind == SymbolKind.CONVERSION_OPERATOR) {
                    result = new BLangBuiltInMethodInvocation(iExpr, iExpr.builtInMethod);
                } else {
                    result = visitConvertStampMethod(iExpr.pos, iExpr.expr,
                                                     iExpr.requiredArgs, (BInvokableSymbol) iExpr.symbol);
                }
                break;
            case CALL:
                visitCallBuiltInMethodInvocation(iExpr);
                break;
            default:
                result = new BLangBuiltInMethodInvocation(iExpr, iExpr.builtInMethod);
        }
    }

    private void visitFreezeBuiltInMethodInvocation(BLangInvocation iExpr) {
        if (types.isValueType(iExpr.expr.type)) {
            if (iExpr.builtInMethod == BLangBuiltInMethod.FREEZE) {
                // since x.freeze() === x, replace the invocation with the invocation expression
                result = iExpr.expr;
            } else {
                // iExpr.builtInMethod == BLangBuiltInMethod.IS_FROZEN, set true since value types are always frozen
                result = ASTBuilderUtil.createLiteral(iExpr.pos, symTable.booleanType, true);
            }
            return;
        }
        result = new BLangBuiltInMethodInvocation(iExpr, iExpr.builtInMethod);
    }

    private void visitCallBuiltInMethodInvocation(BLangInvocation iExpr) {
        Name funcPointerName = ((BLangVariableReference) iExpr.expr).symbol.name;
        if (iExpr.expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
            iExpr.symbol = ((BLangVariableReference) iExpr.expr).symbol;
            iExpr.expr = null;
        } else {
            iExpr.expr = ((BLangAccessExpression) iExpr.expr).expr;
        }

        iExpr.name = ASTBuilderUtil.createIdentifier(iExpr.pos, funcPointerName.value);
        iExpr.builtinMethodInvocation = false;
        iExpr.functionPointerInvocation = true;

        visitFunctionPointerInvocation(iExpr);
    }

    private void visitIterableOperationInvocation(BLangInvocation iExpr) {
        IterableContext iContext = iExpr.iContext;
        if (iContext.operations.getLast().iExpr != iExpr) {
            result = null;
            return;
        }
        iContext.operations.forEach(operation -> rewrite(operation.iExpr.argExprs, env));
        iterableCodeDesugar.desugar(iContext);
        result = rewriteExpr(iContext.iteratorCaller);
    }


    @SuppressWarnings("unchecked")
    private <E extends BLangNode> E rewrite(E node, SymbolEnv env) {
        if (node == null) {
            return null;
        }

        if (node.desugared) {
            return node;
        }

        SymbolEnv previousEnv = this.env;
        this.env = env;

        node.accept(this);
        BLangNode resultNode = this.result;
        this.result = null;
        resultNode.desugared = true;

        this.env = previousEnv;
        return (E) resultNode;
    }

    @SuppressWarnings("unchecked")
    private <E extends BLangExpression> E rewriteExpr(E node) {
        if (node == null) {
            return null;
        }

        if (node.desugared) {
            return node;
        }

        BLangExpression expr = node;
        if (node.impConversionExpr != null) {
            expr = node.impConversionExpr;
            node.impConversionExpr = null;
        }

        expr.accept(this);
        BLangNode resultNode = this.result;
        this.result = null;
        resultNode.desugared = true;
        return (E) resultNode;
    }

    @SuppressWarnings("unchecked")
    private <E extends BLangStatement> E rewrite(E statement, SymbolEnv env) {
        if (statement == null) {
            return null;
        }
        BLangStatementLink link = new BLangStatementLink();
        link.parent = currentLink;
        currentLink = link;
        BLangStatement stmt = (BLangStatement) rewrite((BLangNode) statement, env);
        // Link Statements.
        link.statement = stmt;
        stmt.statementLink = link;
        currentLink = link.parent;
        return (E) stmt;
    }

    private <E extends BLangStatement> List<E> rewriteStmt(List<E> nodeList, SymbolEnv env) {
        for (int i = 0; i < nodeList.size(); i++) {
            nodeList.set(i, rewrite(nodeList.get(i), env));
        }
        return nodeList;
    }

    private <E extends BLangNode> List<E> rewrite(List<E> nodeList, SymbolEnv env) {
        for (int i = 0; i < nodeList.size(); i++) {
            nodeList.set(i, rewrite(nodeList.get(i), env));
        }
        return nodeList;
    }

    private <E extends BLangExpression> List<E> rewriteExprs(List<E> nodeList) {
        for (int i = 0; i < nodeList.size(); i++) {
            nodeList.set(i, rewriteExpr(nodeList.get(i)));
        }
        return nodeList;
    }

    private BLangLiteral createStringLiteral(DiagnosticPos pos, String value) {
        BLangLiteral stringLit = new BLangLiteral();
        stringLit.pos = pos;
        stringLit.value = value;
        stringLit.type = symTable.stringType;
        return stringLit;
    }

    private BLangExpression createTypeCastExpr(BLangExpression expr, BType sourceType, BType targetType) {
        BOperatorSymbol symbol = (BOperatorSymbol) symResolver.resolveConversionOperator(sourceType, targetType);
        return createTypeCastExpr(expr, targetType, symbol);
    }

    private BLangExpression createTypeCastExpr(BLangExpression expr, BType targetType,
                                               BOperatorSymbol symbol) {
        BLangTypeConversionExpr conversionExpr = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        conversionExpr.pos = expr.pos;
        conversionExpr.expr = expr;
        conversionExpr.type = targetType;
        conversionExpr.conversionSymbol = symbol;
        return conversionExpr;
    }

    private BLangNode visitConvertStampMethod(DiagnosticPos pos, BLangExpression expr,
                                              List<BLangExpression> requiredArgs, BInvokableSymbol invokableSymbol) {
        BLangExpression sourceExpression = requiredArgs.get(0);
        BType sourceType = sourceExpression.type;
        if (types.isValueType(sourceType)) {
            return ASTBuilderUtil.createBuiltInMethod(pos, expr, invokableSymbol, requiredArgs, symResolver,
                                                      BLangBuiltInMethod.STAMP);
        }
        
        List<BType> args = Lists.of(sourceType);
        BInvokableType opType = new BInvokableType(args, sourceType, null);
        BOperatorSymbol cloneSymbol = new BOperatorSymbol(names.fromString(BLangBuiltInMethod.CLONE.getName()),
                                                          null, opType, null, InstructionCodes.CLONE);
        BLangBuiltInMethodInvocation cloneInvocation =
                ASTBuilderUtil.createBuiltInMethod(pos, sourceExpression, cloneSymbol, new ArrayList<>(),
                                                   symResolver, BLangBuiltInMethod.CLONE);
        return ASTBuilderUtil.createBuiltInMethod(pos, expr, invokableSymbol, Lists.of(cloneInvocation),
                                                  symResolver, BLangBuiltInMethod.STAMP);

    }

    private BLangExpression appendCloneMethod(DiagnosticPos pos, List<BLangExpression> requiredArgs) {
        BLangExpression sourceExpression = requiredArgs.get(0);
        if (types.isValueType(sourceExpression.type)) {
            return sourceExpression;
        }
        BType sourceType = sourceExpression.type;
        List<BType> args = Lists.of(sourceType);
        BInvokableType opType = new BInvokableType(args, sourceType, null);
        BOperatorSymbol cloneSymbol = new BOperatorSymbol(names.fromString(BLangBuiltInMethod.CLONE.getName()),
                                                          null, opType, null, InstructionCodes.CLONE);
        return ASTBuilderUtil.createBuiltInMethod(pos, sourceExpression, cloneSymbol,
                                                  new ArrayList<>(), symResolver, BLangBuiltInMethod.CLONE);

    }

    private BType getElementType(BType type) {
        if (type.tag != TypeTags.ARRAY) {
            return type;
        }

        return getElementType(((BArrayType) type).getElementType());
    }

    private void addReturnIfNotPresent(BLangInvokableNode invokableNode) {
        if (Symbols.isNative(invokableNode.symbol)) {
            return;
        }
        //This will only check whether last statement is a return and just add a return statement.
        //This won't analyse if else blocks etc to see whether return statements are present
        BLangBlockStmt blockStmt = invokableNode.body;
        if (invokableNode.workers.size() == 0 &&
                invokableNode.symbol.type.getReturnType().isNullable()
                && (blockStmt.stmts.size() < 1 ||
                blockStmt.stmts.get(blockStmt.stmts.size() - 1).getKind() != NodeKind.RETURN)) {

            DiagnosticPos invPos = invokableNode.pos;
            DiagnosticPos returnStmtPos = new DiagnosticPos(invPos.src,
                    invPos.eLine, invPos.eLine, invPos.sCol, invPos.sCol);
            BLangReturn returnStmt = ASTBuilderUtil.createNilReturnStmt(returnStmtPos, symTable.nilType);
            blockStmt.addStatement(returnStmt);
        }
    }

    /**
     * Reorder the invocation arguments to match the original function signature.
     *
     * @param iExpr Function invocation expressions to reorder the arguments
     */
    private void reorderArguments(BLangInvocation iExpr) {
        BSymbol symbol = iExpr.symbol;

        if (symbol == null || symbol.type.tag != TypeTags.INVOKABLE) {
            return;
        }

        BInvokableSymbol invocableSymbol = (BInvokableSymbol) symbol;
        if (invocableSymbol.defaultableParams != null && !invocableSymbol.defaultableParams.isEmpty()) {
            // Re-order the named args
            reorderNamedArgs(iExpr, invocableSymbol);
        }

        if (invocableSymbol.restParam == null) {
            return;
        }

        // Create an array out of all the rest arguments, and pass it as a single argument.
        // If there is only one optional argument and its type is restArg (i.e: ...x), then
        // leave it as is.
        if (iExpr.restArgs.size() == 1 && iExpr.restArgs.get(0).getKind() == NodeKind.REST_ARGS_EXPR) {
            return;
        }
        BLangArrayLiteral arrayLiteral = (BLangArrayLiteral) TreeBuilder.createArrayLiteralNode();
        arrayLiteral.exprs = iExpr.restArgs;
        arrayLiteral.type = invocableSymbol.restParam.type;
        iExpr.restArgs = new ArrayList<>();
        iExpr.restArgs.add(arrayLiteral);
    }

    private void reorderNamedArgs(BLangInvocation iExpr, BInvokableSymbol invokableSymbol) {
        Map<String, BLangExpression> namedArgs = new HashMap<>();
        iExpr.namedArgs.forEach(expr -> namedArgs.put(((NamedArgNode) expr).getName().value, expr));

        // Re-order the named arguments
        List<BLangExpression> args = new ArrayList<>();
        for (BVarSymbol param : invokableSymbol.defaultableParams) {
            // If some named parameter is not passed when invoking the function, get the 
            // default value for that parameter from the parameter symbol.
            BLangExpression expr;
            if (namedArgs.containsKey(param.name.value)) {
                expr = namedArgs.get(param.name.value);
            } else {
                int paramTypeTag = param.type.tag;
                expr = getDefaultValueLiteral(param.defaultValue, paramTypeTag);
                expr = addConversionExprIfRequired(expr, param.type);
            }
            args.add(expr);
        }
        iExpr.namedArgs = args;
    }

    private BLangMatchTypedBindingPatternClause getSafeAssignErrorPattern(
            DiagnosticPos pos, BSymbol invokableSymbol, List<BType> equivalentErrorTypes) {
        // From here onwards we assume that this function has only one return type
        // Owner of the variable symbol must be an invokable symbol
        BType enclosingFuncReturnType = ((BInvokableType) invokableSymbol.type).retType;
        Set<BType> returnTypeSet = enclosingFuncReturnType.tag == TypeTags.UNION ?
                ((BUnionType) enclosingFuncReturnType).memberTypes :
                new LinkedHashSet<BType>() {{
                    add(enclosingFuncReturnType);
                }};

        // For each error type, there has to be at least one equivalent return type in the enclosing function
        boolean returnOnError = equivalentErrorTypes.stream()
                .allMatch(errorType -> returnTypeSet.stream()
                        .anyMatch(retType -> types.isAssignable(errorType, retType)));

        // Create the pattern to match the error type
        //      1) Create the pattern variable
        String patternFailureCaseVarName = GEN_VAR_PREFIX.value + "t_failure";
        BLangSimpleVariable patternFailureCaseVar = ASTBuilderUtil.createVariable(pos,
                patternFailureCaseVarName, symTable.errorType, null, new BVarSymbol(0,
                        names.fromString(patternFailureCaseVarName),
                        this.env.scope.owner.pkgID, symTable.errorType, this.env.scope.owner));

        //      2) Create the pattern block
        BLangVariableReference patternFailureCaseVarRef = ASTBuilderUtil.createVariableRef(pos,
                patternFailureCaseVar.symbol);

        BLangBlockStmt patternBlockFailureCase = (BLangBlockStmt) TreeBuilder.createBlockNode();
        patternBlockFailureCase.pos = pos;
        if (returnOnError) {
            //return e;
            BLangReturn returnStmt = (BLangReturn) TreeBuilder.createReturnNode();
            returnStmt.pos = pos;
            returnStmt.expr = patternFailureCaseVarRef;
            patternBlockFailureCase.stmts.add(returnStmt);
        } else {
            // throw e
            BLangPanic panicNode = (BLangPanic) TreeBuilder.createPanicNode();
            panicNode.pos = pos;
            panicNode.expr = patternFailureCaseVarRef;
            patternBlockFailureCase.stmts.add(panicNode);
        }

        return ASTBuilderUtil.createMatchStatementPattern(pos, patternFailureCaseVar, patternBlockFailureCase);
    }

    private BLangMatchTypedBindingPatternClause getSafeAssignSuccessPattern(DiagnosticPos pos, BType lhsType,
            boolean isVarDef, BVarSymbol varSymbol, BLangExpression lhsExpr) {
        //  File _$_f1 => f = _$_f1;
        // 1) Create the pattern variable
        String patternSuccessCaseVarName = GEN_VAR_PREFIX.value + "t_match";
        BLangSimpleVariable patternSuccessCaseVar = ASTBuilderUtil.createVariable(pos,
                patternSuccessCaseVarName, lhsType, null, new BVarSymbol(0,
                        names.fromString(patternSuccessCaseVarName),
                        this.env.scope.owner.pkgID, lhsType, this.env.scope.owner));

        //2) Create the pattern body
        BLangExpression varRefExpr;
        if (isVarDef) {
            varRefExpr = ASTBuilderUtil.createVariableRef(pos, varSymbol);
        } else {
            varRefExpr = lhsExpr;
        }

        BLangVariableReference patternSuccessCaseVarRef = ASTBuilderUtil.createVariableRef(pos,
                patternSuccessCaseVar.symbol);
        BLangAssignment assignmentStmtSuccessCase = ASTBuilderUtil.createAssignmentStmt(pos,
                varRefExpr, patternSuccessCaseVarRef, false);

        BLangBlockStmt patternBlockSuccessCase = ASTBuilderUtil.createBlockStmt(pos,
                new ArrayList<BLangStatement>() {{
                    add(assignmentStmtSuccessCase);
                }});
        return ASTBuilderUtil.createMatchStatementPattern(pos,
                patternSuccessCaseVar, patternBlockSuccessCase);
    }

    private BLangStatement generateIfElseStmt(BLangMatch matchStmt, BLangSimpleVariable matchExprVar) {
        List<BLangMatchBindingPatternClause> patterns = matchStmt.patternClauses;

        BLangIf parentIfNode = generateIfElseStmt(patterns.get(0), matchExprVar);
        BLangIf currentIfNode = parentIfNode;
        for (int i = 1; i < patterns.size(); i++) {
            BLangMatchBindingPatternClause patternClause = patterns.get(i);
            if (i == patterns.size() - 1 && patternClause.isLastPattern) { // This is the last pattern
                currentIfNode.elseStmt = getMatchPatternElseBody(patternClause, matchExprVar);
            } else {
                currentIfNode.elseStmt = generateIfElseStmt(patternClause, matchExprVar);
                currentIfNode = (BLangIf) currentIfNode.elseStmt;
            }
        }

        // TODO handle json and any
        // only one pattern no if just a block
        // last one just a else block..
        // json handle it specially
        //
        return parentIfNode;
    }


    /**
     * Generate an if-else statement from the given match statement.
     *
     * @param pattern match pattern statement node
     * @param matchExprVar  variable node of the match expression
     * @return if else statement node
     */
    private BLangIf generateIfElseStmt(BLangMatchBindingPatternClause pattern, BLangSimpleVariable matchExprVar) {
        BLangExpression ifCondition = createPatternIfCondition(pattern, matchExprVar.symbol);
        if (NodeKind.MATCH_TYPED_PATTERN_CLAUSE == pattern.getKind()) {
            BLangBlockStmt patternBody = getMatchPatternBody(pattern, matchExprVar);
            return ASTBuilderUtil.createIfElseStmt(pattern.pos, ifCondition, patternBody, null);
        }

        // Create a variable reference for _$$_
        BLangSimpleVarRef matchExprVarRef = ASTBuilderUtil.createVariableRef(pattern.pos, matchExprVar.symbol);

        if (NodeKind.MATCH_STRUCTURED_PATTERN_CLAUSE == pattern.getKind()) { // structured match patterns
            BLangMatchStructuredBindingPatternClause structuredPattern =
                    (BLangMatchStructuredBindingPatternClause) pattern;

            structuredPattern.bindingPatternVariable.expr = matchExprVarRef;

            BLangStatement varDefStmt;
            if (NodeKind.TUPLE_VARIABLE == structuredPattern.bindingPatternVariable.getKind()) {
                varDefStmt = ASTBuilderUtil.createTupleVariableDef(pattern.pos,
                        (BLangTupleVariable) structuredPattern.bindingPatternVariable);
            } else if (NodeKind.RECORD_VARIABLE == structuredPattern.bindingPatternVariable.getKind()) {
                varDefStmt = ASTBuilderUtil.createRecordVariableDef(pattern.pos,
                        (BLangRecordVariable) structuredPattern.bindingPatternVariable);
            } else {
                varDefStmt = ASTBuilderUtil
                        .createVariableDef(pattern.pos, (BLangSimpleVariable) structuredPattern.bindingPatternVariable);
            }

            if (structuredPattern.typeGuardExpr != null) {
                BLangStatementExpression stmtExpr = ASTBuilderUtil
                        .createStatementExpression(varDefStmt, structuredPattern.typeGuardExpr);
                stmtExpr.type = symTable.booleanType;

                ifCondition = ASTBuilderUtil
                        .createBinaryExpr(pattern.pos, ifCondition, stmtExpr, symTable.booleanType, OperatorKind.AND,
                                (BOperatorSymbol) symResolver
                                        .resolveBinaryOperator(OperatorKind.AND, symTable.booleanType,
                                                symTable.booleanType));
            } else {
                structuredPattern.body.stmts.add(0, varDefStmt);
            }
        }

        BLangIf ifNode = ASTBuilderUtil.createIfElseStmt(pattern.pos, ifCondition, pattern.body, null);
        return ifNode;
    }

    private BLangBlockStmt getMatchPatternBody(BLangMatchBindingPatternClause pattern,
                                               BLangSimpleVariable matchExprVar) {

        BLangBlockStmt body;

        BLangMatchTypedBindingPatternClause patternClause = (BLangMatchTypedBindingPatternClause) pattern;
        // Add the variable definition to the body of the pattern` clause
        if (patternClause.variable.name.value.equals(Names.IGNORE.value)) {
            return patternClause.body;
        }

        // create TypeName i = <TypeName> _$$_
        // Create a variable reference for _$$_
        BLangSimpleVarRef matchExprVarRef = ASTBuilderUtil.createVariableRef(patternClause.pos,
                matchExprVar.symbol);
        BLangExpression patternVarExpr = addConversionExprIfRequired(matchExprVarRef, patternClause.variable.type);

        // Add the variable def statement
        BLangSimpleVariable patternVar = ASTBuilderUtil.createVariable(patternClause.pos, "",
                patternClause.variable.type, patternVarExpr, patternClause.variable.symbol);
        BLangSimpleVariableDef patternVarDef = ASTBuilderUtil.createVariableDef(patternVar.pos, patternVar);
        patternClause.body.stmts.add(0, patternVarDef);
        body = patternClause.body;

        return body;
    }

    private BLangBlockStmt getMatchPatternElseBody(BLangMatchBindingPatternClause pattern,
            BLangSimpleVariable matchExprVar) {

        BLangBlockStmt body = pattern.body;

        if (NodeKind.MATCH_STRUCTURED_PATTERN_CLAUSE == pattern.getKind()) { // structured match patterns

            // Create a variable reference for _$$_
            BLangSimpleVarRef matchExprVarRef = ASTBuilderUtil.createVariableRef(pattern.pos, matchExprVar.symbol);

            BLangMatchStructuredBindingPatternClause structuredPattern =
                    (BLangMatchStructuredBindingPatternClause) pattern;

            structuredPattern.bindingPatternVariable.expr = matchExprVarRef;

            BLangStatement varDefStmt;
            if (NodeKind.TUPLE_VARIABLE == structuredPattern.bindingPatternVariable.getKind()) {
                varDefStmt = ASTBuilderUtil.createTupleVariableDef(pattern.pos,
                        (BLangTupleVariable) structuredPattern.bindingPatternVariable);
            } else if (NodeKind.RECORD_VARIABLE == structuredPattern.bindingPatternVariable.getKind()) {
                varDefStmt = ASTBuilderUtil.createRecordVariableDef(pattern.pos,
                        (BLangRecordVariable) structuredPattern.bindingPatternVariable);
            } else {
                varDefStmt = ASTBuilderUtil
                        .createVariableDef(pattern.pos, (BLangSimpleVariable) structuredPattern.bindingPatternVariable);
            }
            structuredPattern.body.stmts.add(0, varDefStmt);
            body = structuredPattern.body;
        }

        return body;
    }

    BLangExpression addConversionExprIfRequired(BLangExpression expr, BType lhsType) {
        BType rhsType = expr.type;
        if (types.isSameType(rhsType, lhsType)) {
            return expr;
        }

        types.setImplicitCastExpr(expr, rhsType, lhsType);
        if (expr.impConversionExpr != null) {
            return expr;
        }

        if (lhsType.tag == TypeTags.JSON && rhsType.tag == TypeTags.NIL) {
            return expr;
        }

        if (lhsType.tag == TypeTags.NIL && rhsType.isNullable()) {
            return expr;
        }

        BCastOperatorSymbol conversionSymbol;
        if (types.isValueType(lhsType)) {
            conversionSymbol = Symbols.createUnboxValueTypeOpSymbol(rhsType, lhsType);
        } else if (lhsType.tag == TypeTags.UNION || rhsType.tag == TypeTags.UNION) {
            conversionSymbol = Symbols.createCastOperatorSymbol(rhsType, lhsType, symTable.errorType, false, true,
                                                                InstructionCodes.NOP, null, null);
        } else if (lhsType.tag == TypeTags.MAP || rhsType.tag == TypeTags.MAP) {
            conversionSymbol = Symbols.createCastOperatorSymbol(rhsType, lhsType, symTable.errorType, false, true,
                                                                InstructionCodes.NOP, null, null);
        } else if (lhsType.tag == TypeTags.TABLE || rhsType.tag == TypeTags.TABLE) {
            conversionSymbol = Symbols.createCastOperatorSymbol(rhsType, lhsType, symTable.errorType, false, true, 
                                                                InstructionCodes.NOP, null, null);
        } else {
            conversionSymbol = (BCastOperatorSymbol) symResolver.resolveCastOperator(rhsType, lhsType);
        }

        // Create a type cast expression
        BLangTypeConversionExpr conversionExpr = (BLangTypeConversionExpr)
                TreeBuilder.createTypeConversionNode();
        conversionExpr.expr = expr;
        conversionExpr.targetType = lhsType;
        conversionExpr.conversionSymbol = conversionSymbol;
        conversionExpr.type = lhsType;
        conversionExpr.pos = expr.pos;
        return conversionExpr;
    }

    private BLangExpression createPatternIfCondition(BLangMatchBindingPatternClause patternClause,
                                                     BVarSymbol varSymbol) {
        BType patternType;

        switch (patternClause.getKind()) {
            case MATCH_STATIC_PATTERN_CLAUSE:
                BLangMatchStaticBindingPatternClause staticPattern =
                        (BLangMatchStaticBindingPatternClause) patternClause;
                patternType = staticPattern.literal.type;
                break;
            case MATCH_STRUCTURED_PATTERN_CLAUSE:
                BLangMatchStructuredBindingPatternClause structuredPattern =
                        (BLangMatchStructuredBindingPatternClause) patternClause;
                patternType = getStructuredBindingPatternType(structuredPattern.bindingPatternVariable);
                break;
            default:
                BLangMatchTypedBindingPatternClause simplePattern = (BLangMatchTypedBindingPatternClause) patternClause;
                patternType = simplePattern.variable.type;
                break;
        }

        BLangExpression binaryExpr;
        BType[] memberTypes;
        if (patternType.tag == TypeTags.UNION) {
            BUnionType unionType = (BUnionType) patternType;
            memberTypes = unionType.memberTypes.toArray(new BType[0]);
        } else {
            memberTypes = new BType[1];
            memberTypes[0] = patternType;
        }

        if (memberTypes.length == 1) {
            binaryExpr = createPatternMatchBinaryExpr(patternClause, varSymbol, memberTypes[0]);
        } else {
            BLangExpression lhsExpr = createPatternMatchBinaryExpr(patternClause, varSymbol, memberTypes[0]);
            BLangExpression rhsExpr = createPatternMatchBinaryExpr(patternClause, varSymbol, memberTypes[1]);
            binaryExpr = ASTBuilderUtil.createBinaryExpr(patternClause.pos, lhsExpr, rhsExpr,
                    symTable.booleanType, OperatorKind.OR,
                    (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.OR,
                            lhsExpr.type, rhsExpr.type));
            for (int i = 2; i < memberTypes.length; i++) {
                lhsExpr = createPatternMatchBinaryExpr(patternClause, varSymbol, memberTypes[i]);
                rhsExpr = binaryExpr;
                binaryExpr = ASTBuilderUtil.createBinaryExpr(patternClause.pos, lhsExpr, rhsExpr,
                        symTable.booleanType, OperatorKind.OR,
                        (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.OR,
                                lhsExpr.type, rhsExpr.type));
            }
        }
        return binaryExpr;
    }

    private BType getStructuredBindingPatternType(BLangVariable bindingPatternVariable) {
        if (NodeKind.TUPLE_VARIABLE == bindingPatternVariable.getKind()) {
            BLangTupleVariable tupleVariable = (BLangTupleVariable) bindingPatternVariable;
            List<BType> memberTypes = new ArrayList<>();
            for (int i = 0; i < tupleVariable.memberVariables.size(); i++) {
                memberTypes.add(getStructuredBindingPatternType(tupleVariable.memberVariables.get(i)));
            }
            return new BTupleType(memberTypes);
        }

        if (NodeKind.RECORD_VARIABLE == bindingPatternVariable.getKind()) {
            BLangRecordVariable recordVariable = (BLangRecordVariable) bindingPatternVariable;

            BRecordTypeSymbol recordSymbol = Symbols
                    .createRecordSymbol(0, names.fromString("$anonType$" + recordCount++), env.enclPkg.symbol.pkgID,
                            null, env.scope.owner);
            List<BField> fields = new ArrayList<>();
            List<BLangSimpleVariable> typeDefFields = new ArrayList<>();

            for (int i = 0; i < recordVariable.variableList.size(); i++) {
                String fieldName = recordVariable.variableList.get(i).key.value;
                BType fieldType = getStructuredBindingPatternType(
                        recordVariable.variableList.get(i).valueBindingPattern);
                BVarSymbol fieldSymbol = new BVarSymbol(Flags.REQUIRED, names.fromString(fieldName),
                        env.enclPkg.symbol.pkgID, fieldType, recordSymbol);

                fields.add(new BField(names.fromString(fieldName), fieldSymbol));
                typeDefFields.add(ASTBuilderUtil.createVariable(null, fieldName, fieldType, null, fieldSymbol));
            }

            BRecordType recordVarType = new BRecordType(recordSymbol);
            recordVarType.fields = fields;
            if (recordVariable.isClosed) {
                recordVarType.sealed = true;
            } else {
                // if rest param is null we treat it as an open record with anydata rest param
                recordVarType.restFieldType = recordVariable.restParam != null ?
                        ((BMapType) ((BLangSimpleVariable) recordVariable.restParam).type).constraint :
                        symTable.anydataType;
            }
            recordSymbol.type = recordVarType;
            recordVarType.tsymbol = recordSymbol;

            createTypeDefinition(recordVarType, recordSymbol, createRecordTypeNode(typeDefFields, recordVarType));

            return recordVarType;
        }

        return bindingPatternVariable.type;
    }

    private BLangRecordTypeNode createRecordTypeNode(List<BLangSimpleVariable> typeDefFields,
            BRecordType recordVarType) {
        BLangRecordTypeNode recordTypeNode = (BLangRecordTypeNode) TreeBuilder.createRecordTypeNode();
        recordTypeNode.type = recordVarType;
        recordTypeNode.fields = typeDefFields;
        return recordTypeNode;
    }

    private void createTypeDefinition(BType type, BTypeSymbol symbol, BLangType typeNode) {
        BLangTypeDefinition typeDefinition = (BLangTypeDefinition) TreeBuilder.createTypeDefinition();
        env.enclPkg.addTypeDefinition(typeDefinition);
        typeDefinition.typeNode = typeNode;
        typeDefinition.type = type;
        typeDefinition.symbol = symbol;
    }

    private BLangExpression createPatternMatchBinaryExpr(BLangMatchBindingPatternClause patternClause,
                                                         BVarSymbol varSymbol, BType patternType) {
        DiagnosticPos pos = patternClause.pos;

        BLangSimpleVarRef varRef = ASTBuilderUtil.createVariableRef(pos, varSymbol);

        if (NodeKind.MATCH_STATIC_PATTERN_CLAUSE == patternClause.getKind()) {
            BLangMatchStaticBindingPatternClause pattern = (BLangMatchStaticBindingPatternClause) patternClause;
            return createBinaryExpression(pos, varRef, pattern.literal);
        }

        if (NodeKind.MATCH_STRUCTURED_PATTERN_CLAUSE == patternClause.getKind()) {
            return createIsLikeExpression(pos, ASTBuilderUtil.createVariableRef(pos, varSymbol), patternType);
        }

        if (patternType == symTable.nilType) {
            BLangLiteral bLangLiteral = ASTBuilderUtil.createLiteral(pos, symTable.nilType, null);
            return ASTBuilderUtil.createBinaryExpr(pos, varRef, bLangLiteral, symTable.booleanType,
                    OperatorKind.EQUAL, (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.EQUAL,
                            symTable.anyType, symTable.nilType));
        } else {
            return createIsAssignableExpression(pos, varSymbol, patternType);
        }
    }

    private BLangBinaryExpr createBinaryExpression(DiagnosticPos pos, BLangSimpleVarRef varRef,
            BLangExpression expression) {

        BLangBinaryExpr binaryExpr;
        if (NodeKind.BRACED_TUPLE_EXPR == expression.getKind() && ((BLangBracedOrTupleExpr) expression).isBracedExpr) {
            return createBinaryExpression(pos, varRef, ((BLangBracedOrTupleExpr) expression).expressions.get(0));
        }

        if (NodeKind.BINARY_EXPR == expression.getKind()) {
            binaryExpr = (BLangBinaryExpr) expression;
            BLangExpression lhsExpr = createBinaryExpression(pos, varRef, binaryExpr.lhsExpr);
            BLangExpression rhsExpr = createBinaryExpression(pos, varRef, binaryExpr.rhsExpr);

            binaryExpr = ASTBuilderUtil.createBinaryExpr(pos, lhsExpr, rhsExpr, symTable.booleanType, OperatorKind.OR,
                    (BOperatorSymbol) symResolver
                            .resolveBinaryOperator(OperatorKind.OR, symTable.booleanType, symTable.booleanType));
        } else {
            binaryExpr = ASTBuilderUtil
                    .createBinaryExpr(pos, varRef, expression, symTable.booleanType, OperatorKind.EQUAL, null);

            BSymbol opSymbol = symResolver.resolveBinaryOperator(OperatorKind.EQUAL, varRef.type, expression.type);
            if (opSymbol == symTable.notFoundSymbol) {
                opSymbol = symResolver
                        .getBinaryEqualityForTypeSets(OperatorKind.EQUAL, symTable.anydataType, expression.type,
                                binaryExpr);
            }
            binaryExpr.opSymbol = (BOperatorSymbol) opSymbol;
        }
        return binaryExpr;
    }

    private BLangIsAssignableExpr createIsAssignableExpression(DiagnosticPos pos,
                                                               BVarSymbol varSymbol,
                                                               BType patternType) {
        //  _$$_ isassignable patternType
        // Create a variable reference for _$$_
        BLangSimpleVarRef varRef = ASTBuilderUtil.createVariableRef(pos, varSymbol);

        // Binary operator for equality
        return ASTBuilderUtil.createIsAssignableExpr(pos, varRef, patternType, symTable.booleanType, names);
    }

    private BLangIsLikeExpr createIsLikeExpression(DiagnosticPos pos, BLangExpression expr, BType type) {
        return ASTBuilderUtil.createIsLikeExpr(pos, expr, ASTBuilderUtil.createTypeNode(type), symTable.booleanType);
    }

    private BLangAssignment createAssignmentStmt(BLangSimpleVariable variable) {
        BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        varRef.pos = variable.pos;
        varRef.variableName = variable.name;
        varRef.symbol = variable.symbol;
        varRef.type = variable.type;

        BLangAssignment assignmentStmt = (BLangAssignment) TreeBuilder.createAssignmentNode();
        assignmentStmt.expr = variable.expr;
        assignmentStmt.pos = variable.pos;
        assignmentStmt.setVariable(varRef);
        return assignmentStmt;
    }

    private void addMatchExprDefaultCase(BLangMatchExpression bLangMatchExpression) {
        List<BType> exprTypes;
        List<BType> unmatchedTypes = new ArrayList<>();

        if (bLangMatchExpression.expr.type.tag == TypeTags.UNION) {
            BUnionType unionType = (BUnionType) bLangMatchExpression.expr.type;
            exprTypes = new ArrayList<>(unionType.memberTypes);
        } else {
            exprTypes = Lists.of(bLangMatchExpression.type);
        }

        // find the types that do not match to any of the patterns.
        for (BType type : exprTypes) {
            boolean assignable = false;
            for (BLangMatchExprPatternClause pattern : bLangMatchExpression.patternClauses) {
                if (this.types.isAssignable(type, pattern.variable.type)) {
                    assignable = true;
                    break;
                }
            }

            if (!assignable) {
                unmatchedTypes.add(type);
            }
        }

        if (unmatchedTypes.isEmpty()) {
            return;
        }

        BType defaultPatternType;
        if (unmatchedTypes.size() == 1) {
            defaultPatternType = unmatchedTypes.get(0);
        } else {
            defaultPatternType = new BUnionType(null, new LinkedHashSet<>(unmatchedTypes), false);
        }

        String patternCaseVarName = GEN_VAR_PREFIX.value + "t_match_default";
        BLangSimpleVariable patternMatchCaseVar = ASTBuilderUtil.createVariable(bLangMatchExpression.pos,
                patternCaseVarName, defaultPatternType, null, new BVarSymbol(0, names.fromString(patternCaseVarName),
                        this.env.scope.owner.pkgID, defaultPatternType, this.env.scope.owner));

        BLangMatchExprPatternClause defaultPattern =
                (BLangMatchExprPatternClause) TreeBuilder.createMatchExpressionPattern();
        defaultPattern.variable = patternMatchCaseVar;
        defaultPattern.expr = ASTBuilderUtil.createVariableRef(bLangMatchExpression.pos, patternMatchCaseVar.symbol);
        defaultPattern.pos = bLangMatchExpression.pos;
        bLangMatchExpression.patternClauses.add(defaultPattern);
    }

    private boolean safeNavigate(BLangAccessExpression accessExpr) {
        if (accessExpr.lhsVar || accessExpr.expr == null) {
            return false;
        }

        if (accessExpr.safeNavigate) {
            return true;
        }

        if (safeNavigateType(accessExpr.expr.type)) {
            if (accessExpr.getKind() == NodeKind.INVOCATION && ((BLangInvocation) accessExpr).builtinMethodInvocation) {
                return isSafeNavigationAllowedBuiltinInvocation((BLangInvocation) accessExpr);
            }
            return true;
        }

        NodeKind kind = accessExpr.expr.getKind();
        if (kind == NodeKind.FIELD_BASED_ACCESS_EXPR ||
                kind == NodeKind.INDEX_BASED_ACCESS_EXPR ||
                kind == NodeKind.INVOCATION) {
            return safeNavigate((BLangAccessExpression) accessExpr.expr);
        }

        return false;
    }

    private boolean safeNavigateType(BType type) {
        // Do not add safe navigation checks for JSON. Because null is a valid value for json,
        // we handle it at runtime. This is also required to make function on json such as
        // j.toString(), j.keys() to work.
        if (type.tag == TypeTags.JSON) {
            return false;
        }

        if (type.isNullable()) {
            return true;
        }

        if (type.tag != TypeTags.UNION) {
            return false;
        }

        return ((BUnionType) type).memberTypes.contains(symTable.nilType);
    }

    private BLangExpression rewriteSafeNavigationExpr(BLangAccessExpression accessExpr) {
        BType originalExprType = accessExpr.type;
        // Create a temp variable to hold the intermediate result of the acces expression.
        String matchTempResultVarName = GEN_VAR_PREFIX.value + "temp_result";
        BLangSimpleVariable tempResultVar = ASTBuilderUtil.createVariable(accessExpr.pos, matchTempResultVarName,
                accessExpr.type, null, new BVarSymbol(0, names.fromString(matchTempResultVarName),
                        this.env.scope.owner.pkgID, accessExpr.type, this.env.scope.owner));
        BLangSimpleVariableDef tempResultVarDef = ASTBuilderUtil.createVariableDef(accessExpr.pos, tempResultVar);
        BLangVariableReference tempResultVarRef =
                ASTBuilderUtil.createVariableRef(accessExpr.pos, tempResultVar.symbol);

        // Create a chain of match statements
        handleSafeNavigation(accessExpr, accessExpr.type, tempResultVar);

        // Create a statement-expression including the match statement
        BLangMatch matcEXpr = this.matchStmtStack.firstElement();
        BLangBlockStmt blockStmt =
                ASTBuilderUtil.createBlockStmt(accessExpr.pos, Lists.of(tempResultVarDef, matcEXpr));
        BLangStatementExpression stmtExpression = ASTBuilderUtil.createStatementExpression(blockStmt, tempResultVarRef);
        stmtExpression.type = originalExprType;

        // Reset the variables
        this.matchStmtStack = new Stack<>();
        this.accessExprStack = new Stack<>();
        this.successPattern = null;
        this.safeNavigationAssignment = null;
        return stmtExpression;
    }

    private void handleSafeNavigation(BLangAccessExpression accessExpr, BType type, BLangSimpleVariable tempResultVar) {
        if (accessExpr.expr == null) {
            return;
        }

        // If the parent of current expr is the root, terminate
        NodeKind kind = accessExpr.expr.getKind();
        if (kind == NodeKind.FIELD_BASED_ACCESS_EXPR ||
                kind == NodeKind.INDEX_BASED_ACCESS_EXPR ||
                kind == NodeKind.INVOCATION) {
            handleSafeNavigation((BLangAccessExpression) accessExpr.expr, type, tempResultVar);
        }

        if (!accessExpr.safeNavigate && !accessExpr.expr.type.isNullable()) {
            accessExpr.type = accessExpr.originalType;
            if (this.safeNavigationAssignment != null) {
                this.safeNavigationAssignment.expr = addConversionExprIfRequired(accessExpr, tempResultVar.type);
            }
            return;
        }

        /*
         * If the field access is a safe navigation, create a match expression.
         * Then chain the current expression as the success-pattern of the parent
         * match expr, if available.
         * eg:
         * x but {              <--- parent match expr
         *   error e => e,
         *   T t => t.y but {   <--- current expr
         *      error e => e,
         *      R r => r.z
         *   }
         * }
         */

        // Add pattern to lift nil
        BLangMatch matchStmt = ASTBuilderUtil.createMatchStatement(accessExpr.pos, accessExpr.expr, new ArrayList<>());
        matchStmt.patternClauses.add(getMatchNullPattern(accessExpr, tempResultVar));
        matchStmt.type = type;

        // Add pattern to lift error, only if the safe navigation is used
        if (accessExpr.safeNavigate) {
            matchStmt.patternClauses.add(getMatchErrorPattern(accessExpr, tempResultVar));
            matchStmt.type = type;
            matchStmt.pos = accessExpr.pos;

        }

        // Create the pattern for success scenario. i.e: not null and not error (if applicable).
        BLangMatchTypedBindingPatternClause successPattern =
                getSuccessPattern(accessExpr, tempResultVar, accessExpr.safeNavigate);
        matchStmt.patternClauses.add(successPattern);
        this.matchStmtStack.push(matchStmt);
        if (this.successPattern != null) {
            this.successPattern.body = ASTBuilderUtil.createBlockStmt(accessExpr.pos, Lists.of(matchStmt));
        }
        this.successPattern = successPattern;
    }

    private boolean isSafeNavigationAllowedBuiltinInvocation(BLangInvocation iExpr) {
        if (iExpr.builtInMethod == BLangBuiltInMethod.FREEZE) {
            if (iExpr.expr.type.tag == TypeTags.UNION && iExpr.expr.type.isNullable()) {
                BUnionType unionType = (BUnionType) iExpr.expr.type;
                return unionType.memberTypes.size() == 2 && unionType.memberTypes.stream()
                        .noneMatch(type -> type.tag != TypeTags.NIL && types.isValueType(type));
            }
        } else if (iExpr.builtInMethod == BLangBuiltInMethod.IS_FROZEN) {
            return false;
        }
        return true;
    }

    private BLangMatchTypedBindingPatternClause getMatchErrorPattern(BLangExpression expr,
                                                                         BLangSimpleVariable tempResultVar) {
        String errorPatternVarName = GEN_VAR_PREFIX.value + "t_match_error";
        BLangSimpleVariable errorPatternVar = ASTBuilderUtil.createVariable(expr.pos, errorPatternVarName,
                symTable.errorType, null, new BVarSymbol(0, names.fromString(errorPatternVarName),
                        this.env.scope.owner.pkgID, symTable.errorType, this.env.scope.owner));

        // Create assignment to temp result
        BLangSimpleVarRef assignmentRhsExpr = ASTBuilderUtil.createVariableRef(expr.pos, errorPatternVar.symbol);
        BLangVariableReference tempResultVarRef = ASTBuilderUtil.createVariableRef(expr.pos, tempResultVar.symbol);
        BLangAssignment assignmentStmt =
                ASTBuilderUtil.createAssignmentStmt(expr.pos, tempResultVarRef, assignmentRhsExpr, false);
        BLangBlockStmt patternBody = ASTBuilderUtil.createBlockStmt(expr.pos, Lists.of(assignmentStmt));

        // Create the pattern
        // R b => a = b;
        BLangMatchTypedBindingPatternClause errorPattern = ASTBuilderUtil
                .createMatchStatementPattern(expr.pos, errorPatternVar, patternBody);
        return errorPattern;
    }

    private BLangMatchExprPatternClause getMatchNullPatternGivenExpression(DiagnosticPos pos,
                                                                           BLangExpression expr) {
        String nullPatternVarName = IGNORE.toString();
        BLangSimpleVariable errorPatternVar = ASTBuilderUtil.createVariable(pos, nullPatternVarName, symTable.nilType,
                null, new BVarSymbol(0, names.fromString(nullPatternVarName),
                        this.env.scope.owner.pkgID, symTable.nilType, this.env.scope.owner));

        BLangMatchExprPatternClause nullPattern =
                (BLangMatchExprPatternClause) TreeBuilder.createMatchExpressionPattern();
        nullPattern.variable = errorPatternVar;
        nullPattern.expr = expr;
        nullPattern.pos = pos;
        return nullPattern;
    }

    private BLangMatchTypedBindingPatternClause getMatchNullPattern(BLangExpression expr,
            BLangSimpleVariable tempResultVar) {
        // TODO: optimize following by replacing var with underscore, and assigning null literal
        String nullPatternVarName = GEN_VAR_PREFIX.value + "t_match_null";
        BLangSimpleVariable nullPatternVar = ASTBuilderUtil.createVariable(expr.pos, nullPatternVarName,
                symTable.nilType, null, new BVarSymbol(0, names.fromString(nullPatternVarName),
                        this.env.scope.owner.pkgID, symTable.nilType, this.env.scope.owner));

        // Create assignment to temp result
        BLangSimpleVarRef assignmentRhsExpr = ASTBuilderUtil.createVariableRef(expr.pos, nullPatternVar.symbol);
        BLangVariableReference tempResultVarRef = ASTBuilderUtil.createVariableRef(expr.pos, tempResultVar.symbol);
        BLangAssignment assignmentStmt =
                ASTBuilderUtil.createAssignmentStmt(expr.pos, tempResultVarRef, assignmentRhsExpr, false);
        BLangBlockStmt patternBody = ASTBuilderUtil.createBlockStmt(expr.pos, Lists.of(assignmentStmt));

        // Create the pattern
        // R b => a = b;
        BLangMatchTypedBindingPatternClause nullPattern = ASTBuilderUtil
                .createMatchStatementPattern(expr.pos, nullPatternVar, patternBody);
        return nullPattern;
    }

    private BLangMatchTypedBindingPatternClause getSuccessPattern(BLangAccessExpression accessExpr,
            BLangSimpleVariable tempResultVar, boolean liftError) {
        BType type = types.getSafeType(accessExpr.expr.type, liftError);
        String successPatternVarName = GEN_VAR_PREFIX.value + "t_match_success";
        BLangSimpleVariable successPatternVar = ASTBuilderUtil.createVariable(accessExpr.pos, successPatternVarName,
                type, null, new BVarSymbol(0, names.fromString(successPatternVarName), this.env.scope.owner.pkgID, type,
                        this.env.scope.owner));

        // Create x.foo, by replacing the varRef expr of the current expression, with the new temp var ref
        accessExpr.expr = ASTBuilderUtil.createVariableRef(accessExpr.pos, successPatternVar.symbol);
        accessExpr.safeNavigate = false;

        // Type of the field access expression should be always taken from the child type.
        // Because the type assigned to expression contains the inherited error/nil types,
        // and may not reflect the actual type of the child/field expr.
        accessExpr.type = accessExpr.originalType;

        BLangVariableReference tempResultVarRef =
                ASTBuilderUtil.createVariableRef(accessExpr.pos, tempResultVar.symbol);

        BLangExpression assignmentRhsExpr = addConversionExprIfRequired(accessExpr, tempResultVarRef.type);
        BLangAssignment assignmentStmt =
                ASTBuilderUtil.createAssignmentStmt(accessExpr.pos, tempResultVarRef, assignmentRhsExpr, false);
        BLangBlockStmt patternBody = ASTBuilderUtil.createBlockStmt(accessExpr.pos, Lists.of(assignmentStmt));

        // Create the pattern
        // R b => a = x.foo;
        BLangMatchTypedBindingPatternClause successPattern =
                ASTBuilderUtil.createMatchStatementPattern(accessExpr.pos, successPatternVar, patternBody);
        this.safeNavigationAssignment = assignmentStmt;
        return successPattern;
    }

    private boolean safeNavigateLHS(BLangExpression expr) {
        if (expr.getKind() != NodeKind.FIELD_BASED_ACCESS_EXPR && expr.getKind() != NodeKind.INDEX_BASED_ACCESS_EXPR) {
            return false;
        }

        BLangExpression varRef = ((BLangAccessExpression) expr).expr;
        if (varRef.type.isNullable()) {
            return true;
        }

        return safeNavigateLHS(varRef);
    }

    private BLangStatement rewriteSafeNavigationAssignment(BLangAccessExpression accessExpr, BLangExpression rhsExpr,
                                                           boolean safeAssignment) {
        List<BLangStatement> stmts = createLHSSafeNavigation(accessExpr, accessExpr.type, rhsExpr, safeAssignment);
        BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(accessExpr.pos, stmts);
        return blockStmt;
    }

    private List<BLangStatement> createLHSSafeNavigation(BLangExpression expr, BType type,
                                                         BLangExpression rhsExpr, boolean safeAssignment) {
        List<BLangStatement> stmts = new ArrayList<>();
        NodeKind kind = expr.getKind();
        if (kind == NodeKind.FIELD_BASED_ACCESS_EXPR || kind == NodeKind.INDEX_BASED_ACCESS_EXPR ||
                kind == NodeKind.INVOCATION) {
            BLangAccessExpression accessExpr = (BLangAccessExpression) expr;
            if (accessExpr.expr != null) {
                this.accessExprStack.push(accessExpr);
                // If the parent of current expr is the root, terminate
                stmts.addAll(createLHSSafeNavigation(accessExpr.expr, type, rhsExpr, safeAssignment));

                this.accessExprStack.pop();
            }
            accessExpr.type = accessExpr.originalType;

            // if its the leaf node, assign the original rhs expression to the access expression
            if (accessExpr.leafNode) {
                BLangVariableReference accessExprForFinalAssignment = cloneExpression(accessExpr);
                BLangAssignment assignmentStmt = ASTBuilderUtil.createAssignmentStmt(accessExpr.pos,
                        accessExprForFinalAssignment, rhsExpr, false);
                assignmentStmt.safeAssignment = safeAssignment;
                stmts.add(assignmentStmt);
                return stmts;
            }
        } else if (expr.type.tag != TypeTags.JSON) {
            // Do not create any default init statement for the very first varRef, unless its a JSON.
            // i.e: In a field access expression a.b.c.d, do not create an init for 'a', if it is not JSON
            return stmts;
        }

        if (expr.type.isNullable() && isDefaultableMappingType(expr.type)) {
            BLangIf ifStmt = getSafeNaviDefaultInitStmt(expr);
            stmts.add(ifStmt);
        }

        return stmts;
    }

    private BLangIf getSafeNaviDefaultInitStmt(BLangExpression accessExpr) {
        // Create if-condition. eg:
        // if (a.b == () )
        BLangVariableReference accessExprForNullCheck = cloneExpression(accessExpr);
        BLangLiteral bLangLiteral = ASTBuilderUtil.createLiteral(accessExpr.pos, symTable.nilType, null);
        BLangBinaryExpr ifCondition = ASTBuilderUtil.createBinaryExpr(accessExpr.pos, accessExprForNullCheck,
                bLangLiteral, symTable.booleanType, OperatorKind.EQUAL, (BOperatorSymbol) symResolver
                        .resolveBinaryOperator(OperatorKind.EQUAL, symTable.anyType, symTable.nilType));

        // Create if body. eg:
        // a.b = {};
        BLangVariableReference accessExprForInit = cloneExpression(accessExpr);
        // Look one step ahead to determine the type of the child, and get the default value expression
        BLangExpression defaultValue = getDefaultValueExpr(this.accessExprStack.peek());
        BLangAssignment assignmentStmt =
                ASTBuilderUtil.createAssignmentStmt(accessExpr.pos, accessExprForInit, defaultValue, false);

        // Create If-statement
        BLangBlockStmt ifBody = ASTBuilderUtil.createBlockStmt(accessExpr.pos, Lists.of(assignmentStmt));
        return ASTBuilderUtil.createIfElseStmt(accessExpr.pos, ifCondition, ifBody, null);
    }

    private BLangVariableReference cloneExpression(BLangExpression expr) {
        switch (expr.getKind()) {
            case SIMPLE_VARIABLE_REF:
                return ASTBuilderUtil.createVariableRef(expr.pos, ((BLangSimpleVarRef) expr).symbol);
            case FIELD_BASED_ACCESS_EXPR:
            case INDEX_BASED_ACCESS_EXPR:
            case INVOCATION:
                return cloneAccessExpr((BLangAccessExpression) expr);
            default:
                throw new IllegalStateException();
        }
    }

    private BLangAccessExpression cloneAccessExpr(BLangAccessExpression originalAccessExpr) {
        if (originalAccessExpr.expr == null) {
            return originalAccessExpr;
        }

        BLangVariableReference varRef;
        NodeKind kind = originalAccessExpr.expr.getKind();
        if (kind == NodeKind.FIELD_BASED_ACCESS_EXPR || kind == NodeKind.INDEX_BASED_ACCESS_EXPR ||
                kind == NodeKind.INVOCATION) {
            varRef = cloneAccessExpr((BLangAccessExpression) originalAccessExpr.expr);
        } else {
            varRef = cloneExpression((BLangVariableReference) originalAccessExpr.expr);
        }
        varRef.type = types.getSafeType(originalAccessExpr.expr.type, false);

        BLangAccessExpression accessExpr;
        switch (originalAccessExpr.getKind()) {
            case FIELD_BASED_ACCESS_EXPR:
                accessExpr = ASTBuilderUtil.createFieldAccessExpr(varRef,
                        ((BLangFieldBasedAccess) originalAccessExpr).field);
                break;
            case INDEX_BASED_ACCESS_EXPR:
                accessExpr = ASTBuilderUtil.createIndexAccessExpr(varRef,
                        ((BLangIndexBasedAccess) originalAccessExpr).indexExpr);
                break;
            case INVOCATION:
                // TODO
                accessExpr = null;
                break;
            default:
                throw new IllegalStateException();
        }

        accessExpr.originalType = originalAccessExpr.originalType;
        accessExpr.pos = originalAccessExpr.pos;
        accessExpr.lhsVar = originalAccessExpr.lhsVar;
        accessExpr.symbol = originalAccessExpr.symbol;
        accessExpr.safeNavigate = false;

        // Type of the field access expression should be always taken from the child type.
        // Because the type assigned to expression contains the inherited error/nil types,
        // and may not reflect the actual type of the child/field expr.
        accessExpr.type = originalAccessExpr.originalType;
        return accessExpr;
    }

    private BLangBinaryExpr getModifiedIntRangeStartExpr(BLangExpression expr) {
        BLangLiteral constOneLiteral = ASTBuilderUtil.createLiteral(expr.pos, symTable.intType, 1L);
        return ASTBuilderUtil.createBinaryExpr(expr.pos, expr, constOneLiteral, symTable.intType, OperatorKind.ADD,
                (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.ADD,
                        symTable.intType,
                        symTable.intType));
    }

    private BLangBinaryExpr getModifiedIntRangeEndExpr(BLangExpression expr) {
        BLangLiteral constOneLiteral = ASTBuilderUtil.createLiteral(expr.pos, symTable.intType, 1L);
        return ASTBuilderUtil.createBinaryExpr(expr.pos, expr, constOneLiteral, symTable.intType, OperatorKind.SUB,
                (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.SUB,
                        symTable.intType,
                        symTable.intType));
    }

    private BLangExpression getDefaultValueExpr(BLangAccessExpression accessExpr) {
        BType fieldType = accessExpr.originalType;
        BType type = types.getSafeType(accessExpr.expr.type, false);
        switch (type.tag) {
            case TypeTags.JSON:
                if (accessExpr.getKind() == NodeKind.INDEX_BASED_ACCESS_EXPR &&
                        ((BLangIndexBasedAccess) accessExpr).indexExpr.type.tag == TypeTags.INT) {
                    return new BLangJSONArrayLiteral(new ArrayList<>(), new BArrayType(fieldType));
                }
                return new BLangJSONLiteral(new ArrayList<>(), fieldType);
            case TypeTags.MAP:
                return new BLangMapLiteral(new ArrayList<>(), type);
            case TypeTags.RECORD:
                return new BLangRecordLiteral(type);
            default:
                throw new IllegalStateException();
        }
    }

    private BLangExpression getDefaultValueLiteral(DefaultValueLiteral defaultValue, int paramTypeTag) {
        if (defaultValue == null || defaultValue.getValue() == null) {
            return getNullLiteral();
        }
        Object value = defaultValue.getValue();
        int literalTypeTag = defaultValue.getLiteralTypeTag();

        if (value instanceof Long) {
            switch (paramTypeTag) {
                case TypeTags.FLOAT:
                    return getFloatLiteral(((Long) value).doubleValue());
                case TypeTags.DECIMAL:
                    return getDecimalLiteral(String.valueOf(value));
                default:
                    return getIntLiteral((Long) value);
            }
        }
        if (value instanceof String) {
            switch (paramTypeTag) {
                case TypeTags.FLOAT:
                    return getFloatLiteral(Double.parseDouble((String) value));
                case TypeTags.DECIMAL:
                    return getDecimalLiteral(String.valueOf(value));
                case TypeTags.FINITE:
                case TypeTags.UNION:
                    if (literalTypeTag == TypeTags.FLOAT) {
                        return getFloatLiteral(Double.parseDouble((String) value));
                    }
                    return getStringLiteral((String) value);
                default:
                    return getStringLiteral((String) value);
            }
        }
        if (value instanceof Boolean) {
            return getBooleanLiteral((Boolean) value);
        }
        throw new IllegalStateException("Unsupported default value type");
    }

    private BLangLiteral getStringLiteral(String value) {
        BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
        literal.value = value;
        literal.type = symTable.stringType;
        return literal;
    }

    private BLangLiteral getIntLiteral(long value) {
        BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
        literal.value = value;
        literal.type = symTable.intType;
        return literal;
    }

    private BLangLiteral getFloatLiteral(double value) {
        BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
        literal.value = value;
        literal.type = symTable.floatType;
        return literal;
    }

    private BLangLiteral getDecimalLiteral(String value) {
        BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
        literal.value = value;
        literal.type = symTable.decimalType;
        return literal;
    }

    private BLangLiteral getBooleanLiteral(boolean value) {
        BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
        literal.value = value;
        literal.type = symTable.booleanType;
        return literal;
    }

    private BLangLiteral getNullLiteral() {
        BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
        literal.type = symTable.nilType;
        return literal;
    }

    private boolean isDefaultableMappingType(BType type) {
        switch (types.getSafeType(type, false).tag) {
            case TypeTags.JSON:
            case TypeTags.MAP:
            case TypeTags.RECORD:
                return true;
            default:
                return false;
        }
    }

    private BLangFunction createDefaultObjectConstructor(BLangObjectTypeNode objectTypeNode, SymbolEnv env) {
        BLangFunction initFunction = ASTBuilderUtil
                .createInitFunction(objectTypeNode.pos, Names.EMPTY.value, Names.OBJECT_INIT_SUFFIX);

        // Create the receiver
        initFunction.receiver = ASTBuilderUtil.createReceiver(objectTypeNode.pos, objectTypeNode.type);
        BVarSymbol receiverSymbol = new BVarSymbol(Flags.asMask(EnumSet.noneOf(Flag.class)),
                names.fromIdNode(initFunction.receiver.name), env.enclPkg.symbol.pkgID, objectTypeNode.type,
                env.scope.owner);
        env.scope.define(receiverSymbol.name, receiverSymbol);
        initFunction.receiver.symbol = receiverSymbol;

        initFunction.type = new BInvokableType(new ArrayList<>(), symTable.nilType, null);
        initFunction.attachedFunction = true;
        initFunction.flagSet.add(Flag.ATTACHED);

        // Create function symbol
        Name funcSymbolName = names.fromString(Symbols.getAttachedFuncSymbolName(objectTypeNode.type.tsymbol.name.value,
                Names.OBJECT_INIT_SUFFIX.value));
        initFunction.symbol = Symbols
                .createFunctionSymbol(Flags.asMask(initFunction.flagSet), funcSymbolName, env.enclPkg.symbol.pkgID,
                        initFunction.type, env.scope.owner, initFunction.body != null);
        initFunction.symbol.scope = new Scope(initFunction.symbol);
        initFunction.symbol.receiverSymbol = receiverSymbol;

        // Set the taint information to the constructed init function
        initFunction.symbol.taintTable = new HashMap<>();
        TaintRecord taintRecord = new TaintRecord(TaintRecord.TaintedStatus.UNTAINTED, new ArrayList<>());
        initFunction.symbol.taintTable.put(TaintAnalyzer.ALL_UNTAINTED_TABLE_ENTRY_INDEX, taintRecord);

        // Update Object type with attached function details
        BObjectTypeSymbol objectSymbol = ((BObjectTypeSymbol) objectTypeNode.type.tsymbol);
        objectSymbol.initializerFunc = new BAttachedFunction(Names.OBJECT_INIT_SUFFIX, initFunction.symbol,
                (BInvokableType) initFunction.type);
        objectSymbol.attachedFuncs.add(objectSymbol.initializerFunc);
        objectTypeNode.initFunction = initFunction;
        return initFunction;
    }

    private BLangExpression getInitExpr(BType type, BLangTypeInit typeInitExpr) {
        String identifier;
        switch (typeInitExpr.parent.getKind()) {
            case ASSIGNMENT:
                identifier = ((BLangSimpleVarRef) ((BLangAssignment) typeInitExpr.parent).varRef).symbol.name.value;
                break;
            case VARIABLE:
                identifier = ((BLangSimpleVariable) typeInitExpr.parent).name.value;
                break;
            default:
                return null;
                // shouldn't reach here - todo need to fix as param
        }
        switch (type.tag) {
            case TypeTags.STREAM:
                return new BLangStreamLiteral(type, identifier);
            case TypeTags.CHANNEL:
                return new BLangChannelLiteral(type, identifier);
            default:
                return null;
        }
    }
}

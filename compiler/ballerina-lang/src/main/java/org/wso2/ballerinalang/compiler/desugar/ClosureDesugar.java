/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BAttachedFunction;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BRecordTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangInvokableNode;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTableKeyTypeConstraint;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.BLangXMLNS;
import org.wso2.ballerinalang.compiler.tree.OCEDynamicEnvironmentData;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAccessExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrowFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr.BLangListConstructorSpreadOpExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock.BLangLockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangLock.BLangUnLockStmt;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangWorkerSend;
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangArrayType;
import org.wso2.ballerinalang.compiler.tree.types.BLangBuiltInRefTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangConstrainedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangErrorType;
import org.wso2.ballerinalang.compiler.tree.types.BLangFiniteTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangFunctionTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangIntersectionTypeNode;
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
import org.wso2.ballerinalang.compiler.util.FieldKind;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import static io.ballerina.runtime.api.constants.RuntimeConstants.UNDERSCORE;
import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;
import static org.wso2.ballerinalang.compiler.semantics.model.Scope.NOT_FOUND_ENTRY;

/**
 * Closure desugar for closure related scenarios.
 *
 * @since 0.990.5
 */
public class ClosureDesugar extends BLangNodeVisitor {
    private static final CompilerContext.Key<ClosureDesugar> CLOSURE_DESUGAR_KEY = new CompilerContext.Key<>();

    private static final String BLOCK_MAP_SYM_NAME = "$map$block$" + UNDERSCORE;
    private static final String FUNCTION_MAP_SYM_NAME = "$map$func$" + UNDERSCORE;
    private static final String OBJECT_CTOR_MAP_SYM_NAME = "$map$objectCtor" + UNDERSCORE;
    private static final String PARAMETER_MAP_NAME = "$paramMap$" + UNDERSCORE;
    private static final BVarSymbol CLOSURE_MAP_NOT_FOUND;

    private SymbolResolver symResolver;
    private SymbolTable symTable;
    private SymbolEnv env;
    private BLangNode result;
    private Types types;
    private Desugar desugar;
    private Names names;
    private ClassClosureDesugar classClosureDesugar;
    private int funClosureMapCount = 1;
    private int blockClosureMapCount = 1;

    static {
        CLOSURE_MAP_NOT_FOUND = new BVarSymbol(0, new Name("$not$found"), null, null, null, null, VIRTUAL);
    }

    public static ClosureDesugar getInstance(CompilerContext context) {
        ClosureDesugar desugar = context.get(CLOSURE_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new ClosureDesugar(context);
        }

        return desugar;
    }

    private ClosureDesugar(CompilerContext context) {
        context.put(CLOSURE_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.types = Types.getInstance(context);
        this.desugar = Desugar.getInstance(context);
        this.names = Names.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.classClosureDesugar = ClassClosureDesugar.getInstance(context);
        this.CLOSURE_MAP_NOT_FOUND.pos = this.symTable.builtinPos;
        this.symResolver = SymbolResolver.getInstance(context);
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        SymbolEnv pkgEnv = this.symTable.pkgEnvMap.get(pkgNode.symbol);

        pkgNode.services.forEach(service -> rewrite(service, pkgEnv));
        pkgNode.globalVars.forEach(globalVar -> rewrite(globalVar, pkgEnv));
        pkgNode.typeDefinitions.forEach(typeDefinition -> rewrite(typeDefinition, pkgEnv));
        pkgNode.xmlnsList.forEach(xmlns -> rewrite(xmlns, pkgEnv));
        pkgNode.constants.forEach(constant -> rewrite(constant, pkgEnv));
        pkgNode.annotations.forEach(annotation -> rewrite(annotation, pkgEnv));
        pkgNode.initFunction = rewrite(pkgNode.initFunction, pkgEnv);

        for (BLangFunction bLangFunction : pkgNode.functions) {
            if (!bLangFunction.flagSet.contains(Flag.LAMBDA)) {
                rewrite(bLangFunction, pkgEnv);
            }
        }

        for (BLangClassDefinition classDef : pkgNode.classDefinitions) {
            if (classDef.isObjectContructorDecl) {
                updateClassClosureMap(classDef);
            }
        }

        // Update function parameters.
        for (BLangFunction function : pkgNode.functions) {
            updateFunctionParams(function);
        }
        for (BLangTypeDefinition typeDef : pkgNode.typeDefinitions) {
            if (typeDef.typeNode.getKind() == NodeKind.RECORD_TYPE) {
                updateRecordInitFunction(typeDef);
            }
        }
        result = pkgNode;
    }

    private boolean isObjectConstructorRelated(TopLevelNode pkgLevelNode) {

        if ((pkgLevelNode.getKind() == NodeKind.CLASS_DEFN &&
                ((BLangClassDefinition) pkgLevelNode).isObjectContructorDecl)) {
            return true;
        }
        if (pkgLevelNode.getKind() == NodeKind.FUNCTION &&
                ((BLangFunction) pkgLevelNode).flagSet.contains(Flag.OBJECT_CTOR)) {
            return false;
        }
        return false;
    }

    private void createClosureMapUpdateExpression(BLangClassDefinition classDef,
                                                  BVarSymbol blockMap,
                                                  BVarSymbol classMapSymbol) {

        OCEDynamicEnvironmentData oceEnvData = classDef.oceEnvData;
        BVarSymbol oceMap = oceEnvData.mapBlockMapSymbol;

        BLangFunction function = classDef.generatedInitFunction;

        // self
        BVarSymbol selfSymbol = function.receiver.symbol;
        BLangSimpleVarRef selfVarRef = ASTBuilderUtil.createVariableRef(function.pos, selfSymbol);
        // $map$block$_2
        BLangSimpleVarRef refToBlockClosureMap = ASTBuilderUtil.createVariableRef(function.pos, blockMap);

        // self.$map$objectCtor
        BLangIdentifier identifierNode = ASTBuilderUtil.createIdentifier(function.pos, blockMap.name.value);
        BLangFieldBasedAccess fieldAccess = ASTBuilderUtil.createFieldAccessExpr(selfVarRef, identifierNode);
        fieldAccess.symbol = oceMap;
        fieldAccess.setBType(classMapSymbol.type);
        fieldAccess.expectedType = classMapSymbol.type;
        fieldAccess.isStoreOnCreation = true;
        fieldAccess.isLValue = true;
        fieldAccess.fieldKind = FieldKind.SINGLE;
        fieldAccess.leafNode = true;

        // self.$map$objectCtor = $map$block$_2
        BLangAssignment assignmentStmt = (BLangAssignment) TreeBuilder.createAssignmentNode();
        assignmentStmt.expr = refToBlockClosureMap;
        assignmentStmt.pos = function.pos;
        assignmentStmt.setVariable(fieldAccess);
        fieldAccess.parent = assignmentStmt;

        BLangFunction generatedInitFunction = classDef.generatedInitFunction;
        BLangBlockFunctionBody generatedInitFnBody = (BLangBlockFunctionBody) generatedInitFunction.body;

        // self[$map$objectCtor$_<num1>] = $map$block$_<num2>
        generatedInitFnBody.stmts.add(0, assignmentStmt);
        assignmentStmt.parent = generatedInitFnBody;

        desugar.rewrite(assignmentStmt, oceEnvData.objMethodsEnv);
    }

    // TODO: filter out only the needed variables, otherwise OCE has indrect access to all block level
    private void addClosureMapToInit(BLangClassDefinition classDef, BVarSymbol mapSymbol) {

        OCEDynamicEnvironmentData oceData = classDef.oceEnvData;

        // eg : $map$block$_<num2>
        BLangSimpleVarRef refToBlockClosureMap = ASTBuilderUtil.createVariableRef(classDef.pos, mapSymbol);
        BLangTypeInit typeInit = oceData.typeInit;
        BLangInvocation initInvocation = (BLangInvocation) oceData.initInvocation;
        if (typeInit.argsExpr == null) {
            typeInit.argsExpr = new ArrayList<>();
        }

        // update new expression
        typeInit.argsExpr.add(refToBlockClosureMap);
        initInvocation.requiredArgs.add(refToBlockClosureMap);
        initInvocation.argExprs.add(refToBlockClosureMap);

        // add closure map as a argument to init method of the object
        BLangSimpleVariable blockClosureMap = ASTBuilderUtil.createVariable(symTable.builtinPos, mapSymbol.name.value,
                mapSymbol.getType(), null, mapSymbol);
        blockClosureMap.flagSet.add(Flag.REQUIRED_PARAM);
        BObjectTypeSymbol typeSymbol = ((BObjectTypeSymbol) classDef.getBType().tsymbol);
        if (typeSymbol.generatedInitializerFunc != null) {
            BAttachedFunction attachedInitFunction = typeSymbol.generatedInitializerFunc;
            attachedInitFunction.symbol.params.add(mapSymbol);
        }

        // add closure map as argument to class init method
        BLangFunction initFunction = classDef.generatedInitFunction;

        initFunction.desugared = false;
        initFunction.requiredParams.add(blockClosureMap);
        SymbolEnv env = oceData.objMethodsEnv;

        BVarSymbol paramSym = new BVarSymbol(Flags.FINAL, mapSymbol.name, env.scope.owner.pkgID, mapSymbol.type,
                initFunction.symbol, classDef.pos, VIRTUAL);

        // update symbols for init method
        initFunction.symbol.scope.define(paramSym.name, paramSym);
        initFunction.symbol.params.add(paramSym);
        BInvokableType initFuncSymbolType = (BInvokableType) initFunction.symbol.type;
        initFuncSymbolType.paramTypes.add(mapSymbol.type);

        BInvokableType initFnType = (BInvokableType) initFunction.getBType();
        initFnType.paramTypes.add(mapSymbol.type);

        BAttachedFunction attachedFunction = ((BObjectTypeSymbol) classDef.getBType().tsymbol).generatedInitializerFunc;
        attachedFunction.symbol.params.add(paramSym);
        classDef.generatedInitFunction = desugar.rewrite(classDef.generatedInitFunction, oceData.objMethodsEnv);

        // Add map to the callee expression
        addMapToCalleeExpression(mapSymbol, oceData);
    }

    private void addMapToCalleeExpression(BVarSymbol mapSymbol, OCEDynamicEnvironmentData oceData) {
        BLangSimpleVarRef.BLangLocalVarRef blockLevelMapLocalVarRef = new BLangSimpleVarRef.BLangLocalVarRef(mapSymbol);
        oceData.attachedFunctionInvocation.requiredArgs.add(blockLevelMapLocalVarRef);
    }

    private void addMapSymbolAsAField(BLangClassDefinition classDef, BVarSymbol mapSymbol) {
        BLangSimpleVariable mapVar = ASTBuilderUtil.createVariable(symTable.builtinPos, mapSymbol.name.value,
                mapSymbol.type, null, mapSymbol);

        BObjectType object = (BObjectType) classDef.getBType();
        object.fields.put(mapSymbol.name.value, new BField(mapSymbol.name, classDef.pos, mapSymbol));
        classDef.fields.add(0, mapVar);
    }

    private BVarSymbol addFunctionMapMapToClassDefinition(BLangClassDefinition classDef,
                                                          BVarSymbol functionMapSymbol) {
        if (classDef.oceEnvData.classEnclosedFunctionMap != null) {
            return classDef.oceEnvData.classEnclosedFunctionMap;
        }

        BVarSymbol functionMap = functionMapSymbol;

        BVarSymbol classFunctionMapSymbol = createFunctionMapSymbolIfAbsent(classDef, functionMapSymbol.name.value);
        classDef.oceEnvData.classEnclosedFunctionMap = classFunctionMapSymbol;

        addMapSymbolAsAField(classDef, classFunctionMapSymbol);
        createClosureMapUpdateExpression(classDef, functionMap, classFunctionMapSymbol);
        addClosureMapToInit(classDef, functionMap);

        classDef.oceEnvData.functionMapUpdatedInInitMethod = true;
        return classDef.oceEnvData.classEnclosedFunctionMap;
    }

    private BLangNode getNextPossibleNode(SymbolEnv envArg) {
        SymbolEnv localEnv = envArg;
        BLangNode node = localEnv.node;
        while (localEnv != null) {
            NodeKind kind = node.getKind();
            if (kind == NodeKind.PACKAGE) {
                break;
            }
            if (kind == NodeKind.CLASS_DEFN) {
                break;
            }

            if (kind == NodeKind.BLOCK_FUNCTION_BODY || kind == NodeKind.BLOCK ||
                    kind == NodeKind.FUNCTION || kind == NodeKind.RESOURCE_FUNC) {
                break;
            }

            localEnv = localEnv.enclEnv;
            node = localEnv.node;
        }
        return node;
    }

    private void updateClassClosureMap(BLangClassDefinition classDef) {

        if (!classDef.hasClosureVars) {
            return;
        }
        OCEDynamicEnvironmentData oceData = classDef.oceEnvData;
        SymbolEnv env = oceData.capturedClosureEnv;
        if (env.enclEnv.node == null) {
            return;
        }
        if (!oceData.closureBlockSymbols.isEmpty()) {
            BLangNode node = getNextPossibleNode(env.enclEnv);
            if (node.getKind() == NodeKind.FUNCTION) {
                BLangFunction function = (BLangFunction) node;
                node = function.body;
            }
            BVarSymbol blockMap = createMapSymbolIfAbsent(node, oceData.closureBlockSymbols.size());
            addClosureMapToInit(classDef, blockMap);
        }
        if (!oceData.closureFuncSymbols.isEmpty()) {
            BLangFunction function = (BLangFunction) oceData.capturedClosureEnv.enclInvokable;
            BVarSymbol functionMap = createMapSymbolIfAbsent(function, oceData.closureFuncSymbols.size());
            addClosureMapToInit(classDef, functionMap);
        }

        if (oceData.mapBlockMapSymbol == null && oceData.mapFunctionMapSymbol == null) {
            return;
        }

        BVarSymbol selfSymbol = classDef.generatedInitFunction.receiver.symbol;
        BLangBlockFunctionBody initBody = (BLangBlockFunctionBody) classDef.generatedInitFunction.body;

        int i = initBody.stmts.size() - 1;
        for (BLangSimpleVariable field : classDef.fields) {
            if (!field.symbol.isDefaultable) {
                continue;
            }
            if (field.expr.getKind() != NodeKind.SIMPLE_VARIABLE_REF) {
                continue;
            }
            BLangSimpleVarRef varRef = (BLangSimpleVarRef) field.expr;
            if (!varRef.symbol.closure) {
                continue;
            }
            BVarSymbol mapSymbol = oceData.mapBlockMapSymbol;
            BSymbol fieldExprSymbol = ((BLangSimpleVarRef) field.expr).symbol;
            if (oceData.closureFuncSymbols.contains(fieldExprSymbol)) {
                mapSymbol = oceData.mapFunctionMapSymbol;
            }

            // self[x]
            BLangLiteral mapIndex = ASTBuilderUtil.createLiteral(field.pos, symTable.stringType, field.name);
            BLangSimpleVarRef.BLangLocalVarRef localSelfVarRefForMap =
                    new BLangSimpleVarRef.BLangLocalVarRef(selfSymbol);
            localSelfVarRefForMap.setBType(classDef.getBType());
            BLangIndexBasedAccess.BLangStructFieldAccessExpr mapAccessExpr =
                    new BLangIndexBasedAccess.BLangStructFieldAccessExpr(field.pos, localSelfVarRefForMap, mapIndex,
                            field.symbol, true);
            mapAccessExpr.isStoreOnCreation = true;
            mapAccessExpr.setBType(field.getBType());

            // self[$map$objectCtor$_<num>]
            BLangSimpleVarRef.BLangLocalVarRef localSelfVarRef = new BLangSimpleVarRef.BLangLocalVarRef(selfSymbol);
            localSelfVarRef.setBType(classDef.getBType());
            localSelfVarRef.closureDesugared = true;
            BLangIndexBasedAccess.BLangStructFieldAccessExpr accessExprForClassField =
                    new BLangIndexBasedAccess.BLangStructFieldAccessExpr(field.pos, localSelfVarRef,
                            ASTBuilderUtil.createLiteral(field.pos, symTable.stringType,
                                    mapSymbol.name), mapSymbol, false, true);

            accessExprForClassField.setBType(mapSymbol.type);

            //  self[$map$objectCtor$_<num>][i]
            BLangIndexBasedAccess.BLangMapAccessExpr closureMapAccessForField =
                    new BLangIndexBasedAccess.BLangMapAccessExpr(field.pos, accessExprForClassField,
                            ASTBuilderUtil.createLiteral(field.pos, symTable.stringType, varRef.symbol.name));

            closureMapAccessForField.setBType(field.getBType());

            BLangTypeConversionExpr castExpr = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
            castExpr.expr = closureMapAccessForField;
            castExpr.setBType(field.getBType());
            castExpr.targetType = field.getBType();

            // self[x] =  self[$map$objectCtor$_<num>][i];
            BLangAssignment assignFromPassedMapToInternalMap = ASTBuilderUtil.createAssignmentStmt(field.pos,
                    mapAccessExpr, castExpr);
            assignFromPassedMapToInternalMap.parent = classDef.generatedInitFunction.body;

            initBody.stmts.add(i, assignFromPassedMapToInternalMap);
            i = i + 1;
        }
        BLangFunction initFunction = classDef.generatedInitFunction;
        desugar.visit((BLangBlockFunctionBody) initFunction.body);
    }

    @Override
    public void visit(BLangResourceFunction resourceFunction) {
        visit((BLangFunction) resourceFunction);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.originalFuncSymbol.scope, env);
        funClosureMapCount++;

        // Check if function parameters are exposed as closures.
        Optional<BVarSymbol> paramsExposed = funcNode.symbol.params.stream().filter(bVarSymbol -> bVarSymbol.closure)
                .findAny();
        int position = 1;
        if (paramsExposed.isPresent()) {
            createFunctionMap(funcNode, funcEnv);
            // Add the parameters of the functions that are exposed as closures to the function map.
            for (BVarSymbol paramSymbol : funcNode.symbol.params) {
                if (!paramSymbol.closure) {
                    continue;
                }
                addToFunctionMap(funcNode, funcEnv, position, paramSymbol, paramSymbol.type);
                position++;
            }
        }

        // Check if the rest param is a closure var
        if (funcNode.symbol.restParam != null && funcNode.symbol.restParam.closure) {
            createFunctionMap(funcNode, funcEnv);
            addToFunctionMap(funcNode, funcEnv, position, funcNode.symbol.restParam,
                             funcNode.symbol.restParam.type);
            position++;
        }

        // For attached functions add the receiver to the function map if it has been exposed as a closure.
        BLangSimpleVariable receiver = funcNode.receiver;
        if (receiver != null && receiver.symbol.closure && funcNode.flagSet.contains(Flag.ATTACHED)) {
            createFunctionMap(funcNode, funcEnv);
            addToFunctionMap(funcNode, funcEnv, position, receiver.symbol, receiver.getBType());
        }
        rewriteParamsAndReturnTypeOfFunction(funcNode, funcEnv);
        funcNode.body = rewrite(funcNode.body, funcEnv);
        result = funcNode;
    }

    public void rewriteParamsAndReturnTypeOfFunction(BLangFunction funcNode, SymbolEnv funcEnv) {
        for (BLangSimpleVariable bLangSimpleVariable : funcNode.requiredParams) {
            bLangSimpleVariable.typeNode = rewrite(bLangSimpleVariable.typeNode, funcEnv);
        }
        if (funcNode.restParam != null) {
            funcNode.restParam = rewrite(funcNode.restParam, funcEnv);
        }
        if (funcNode.returnTypeNode != null) {
            funcNode.returnTypeNode = rewrite(funcNode.returnTypeNode, funcEnv);
        }
    }

    @Override
    public void visit(BLangBlockFunctionBody body) {
        SymbolEnv blockEnv = SymbolEnv.createFuncBodyEnv(body, env);
        blockClosureMapCount++;
        rewriteStmts(body.stmts, blockEnv);

        // Add block map to the 0th position if a block map symbol is there.
        if (body.mapSymbol != null) {
            body.stmts.add(0, getClosureMap(body.mapSymbol, blockEnv));
        }

        result = body;
    }

    @Override
    public void visit(BLangExternalFunctionBody body) {
        result = body;
    }

    /**
     * Create a function map to add parameters of the function that are used as closures.
     *
     * @param funcNode function node
     * @param funcEnv  function environment
     */
    private void createFunctionMap(BLangFunction funcNode, SymbolEnv funcEnv) {
        if (funcNode.mapSymbol == null) {
            funcNode.mapSymbol = createMapSymbol(FUNCTION_MAP_SYM_NAME  + funClosureMapCount, funcEnv);
        }
        if (funcNode.mapSymbolUpdated) {
            return;
        }
        BLangRecordLiteral emptyRecord = ASTBuilderUtil.createEmptyRecordLiteral(funcNode.pos, symTable.mapType);
        BLangSimpleVariable mapVar = ASTBuilderUtil.createVariable(funcNode.pos, funcNode.mapSymbol.name.value,
                                                                   funcNode.mapSymbol.type, emptyRecord,
                                                                   funcNode.mapSymbol);
        mapVar.typeNode = ASTBuilderUtil.createTypeNode(funcNode.mapSymbol.type);
        BLangSimpleVariableDef mapVarDef = ASTBuilderUtil.createVariableDef(funcNode.pos, mapVar);
        // Add the map variable to the top of the statements in the block node.
        mapVarDef = desugar.rewrite(mapVarDef, funcEnv);
        // Add the map variable to the top of the statements in the block node.
        if (funcNode.body == null) {
            funcNode.body = ASTBuilderUtil.createBlockFunctionBody(funcNode.pos);
        }
        if (funcNode.body.getKind() == NodeKind.BLOCK_FUNCTION_BODY) {
            ((BLangBlockFunctionBody) funcNode.body).stmts.add(0, mapVarDef);
        }
        funcNode.mapSymbolUpdated = true;
    }

    /**
     * Update the function parameters with closure parameter maps passed.
     *
     * @param funcNode function node
     */
    private static void updateFunctionParams(BLangFunction funcNode) {
        // Add closure params to the required param list if there are any.
        BInvokableSymbol dupFuncSymbol = ASTBuilderUtil.duplicateInvokableSymbol(funcNode.symbol);
        funcNode.symbol = dupFuncSymbol;
        BInvokableType dupFuncType = (BInvokableType) dupFuncSymbol.type;

        int i = 0;
        for (Map.Entry<Integer, BVarSymbol> entry : funcNode.paramClosureMap.entrySet()) {
            BVarSymbol mapSymbol = entry.getValue();
            dupFuncSymbol.params.add(i, mapSymbol);
            dupFuncType.paramTypes.add(i, mapSymbol.type);
            i++;
        }
    }

    private static void updateRecordInitFunction(BLangTypeDefinition typeDef) {
        BLangRecordTypeNode recordTypeNode = (BLangRecordTypeNode) typeDef.typeNode;
        BInvokableSymbol initFnSym = recordTypeNode.initFunction.symbol;
        BRecordTypeSymbol recordTypeSymbol;
        if (typeDef.symbol.kind == SymbolKind.TYPE_DEF) {
            recordTypeSymbol = (BRecordTypeSymbol) typeDef.symbol.type.tsymbol;
        } else {
            recordTypeSymbol = (BRecordTypeSymbol) typeDef.symbol;
        }
        recordTypeSymbol.initializerFunc.symbol = initFnSym;
        recordTypeSymbol.initializerFunc.type = (BInvokableType) initFnSym.type;
    }

    /**
     * Add function parameters exposed as closures to the function map.
     *
     * @param funcNode    function node
     * @param symbolEnv   symbol environment
     * @param position    position to be added
     * @param paramSymbol parameter symbol
     * @param type        parameter type
     */
    private void addToFunctionMap(BLangFunction funcNode, SymbolEnv symbolEnv, int position, BVarSymbol paramSymbol,
                                  BType type) {
        BLangSimpleVarRef.BLangLocalVarRef localVarRef = new BLangSimpleVarRef.BLangLocalVarRef(paramSymbol);
        // Added the flag so it will not be desugared again.
        localVarRef.closureDesugared = true;
        localVarRef.setBType(type);
        // $map$func$_num[localVarRef]
        BLangIndexBasedAccess accessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(funcNode.pos, type,
                funcNode.mapSymbol, ASTBuilderUtil.createLiteral(funcNode.pos, symTable.stringType,
                        paramSymbol.name.value));
        accessExpr.setBType(((BMapType) funcNode.mapSymbol.type).constraint);
        accessExpr.isLValue = true;
        // $map$func$_num[localVarRef] = <tyoe> localVarRef;
        BLangAssignment stmt = desugar.rewrite(ASTBuilderUtil.createAssignmentStmt(funcNode.pos, accessExpr,
                localVarRef), symbolEnv);
        if (funcNode.body.getKind() == NodeKind.BLOCK_FUNCTION_BODY) {
            ((BLangBlockFunctionBody) funcNode.body).stmts.add(position, stmt);
        }
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, env);
        blockClosureMapCount++;
        rewriteStmts(blockNode.stmts, blockEnv);

        // Add block map to the 0th position if a block map symbol is there.
        if (blockNode.mapSymbol != null) {
            // Add the closure map var as the first statement in the sequence statement.
            blockNode.stmts.add(0, getClosureMap(blockNode.mapSymbol, blockEnv));
        }

        result = blockNode;
    }

    private BLangSimpleVariableDef getClosureMap(BVarSymbol mapSymbol, SymbolEnv blockEnv) {
        BLangRecordLiteral emptyRecord = ASTBuilderUtil.createEmptyRecordLiteral(symTable.builtinPos, mapSymbol.type);
        BLangSimpleVariable mapVar = ASTBuilderUtil.createVariable(symTable.builtinPos, mapSymbol.name.value,
                                                                   mapSymbol.type, emptyRecord, mapSymbol);
        mapVar.typeNode = ASTBuilderUtil.createTypeNode(mapSymbol.type);
        BLangSimpleVariableDef mapVarDef = ASTBuilderUtil.createVariableDef(symTable.builtinPos, mapVar);
        return desugar.rewrite(mapVarDef, blockEnv);
    }

    @Override
    public void visit(BLangService serviceNode) {
        /* Ignore */
    }

    @Override
    public void visit(BLangSimpleVariableDef varDefNode) {
        if (!varDefNode.var.symbol.closure) {
            varDefNode.var = rewrite(varDefNode.var, env);
            result = varDefNode;
            return;
        }

        // If its a variable declaration with a RHS value, and also a closure.
        varDefNode.var.typeNode = rewrite(varDefNode.var.typeNode, env);
        if (varDefNode.var.expr != null) {
            BLangAssignment stmt = createAssignmentToClosureMap(varDefNode);
            result = rewrite(stmt, env);
        } else {
            // Note: Although it's illegal to use a closure variable without initializing it in it's declared scope,
            // when we access (initialize) a variable from outer scope, since we desugar transaction block into a
            // lambda invocation, we need to create the `mapSymbol` in the outer node.
            createMapSymbolIfAbsent(env.node, blockClosureMapCount);
            result = varDefNode;
        }
    }

    /**
     * Replace the variable definition statement with as assignment statement for closures.
     *
     * @param varDefNode variable definition node
     * @return assignment statement created
     */
    private BLangAssignment createAssignmentToClosureMap(BLangSimpleVariableDef varDefNode) {
        BVarSymbol mapSymbol = createMapSymbolIfAbsent(env.node, blockClosureMapCount);

        // Add the variable to the created map.
        BLangIndexBasedAccess accessExpr =
                ASTBuilderUtil.createIndexBasesAccessExpr(varDefNode.pos, varDefNode.getBType(), mapSymbol,
                                                          ASTBuilderUtil
                                                                  .createLiteral(varDefNode.pos, symTable.stringType,
                                                                                 varDefNode.var.name.value));
        accessExpr.setBType(((BMapType) mapSymbol.type).constraint);
        accessExpr.isLValue = true;
        // Written to: 'map["x"] = 8'.
        return ASTBuilderUtil.createAssignmentStmt(varDefNode.pos, accessExpr, varDefNode.var.expr);
    }

    private BVarSymbol createMapSymbolIfAbsent(BLangNode node, int closureMapCount) {
        NodeKind kind = node.getKind();
        switch (kind) {
            case BLOCK_FUNCTION_BODY:
                return createMapSymbolIfAbsent((BLangBlockFunctionBody) node, closureMapCount);
            case BLOCK:
                return createMapSymbolIfAbsent((BLangBlockStmt) node, closureMapCount);
            case FUNCTION:
                return createMapSymbolIfAbsent((BLangFunction) node, closureMapCount);
            case RESOURCE_FUNC:
                return createMapSymbolIfAbsent((BLangResourceFunction) node, closureMapCount);
            case CLASS_DEFN:
                return createMapSymbolIfAbsent((BLangClassDefinition) node, closureMapCount);
        }
        return null;
    }

    private BVarSymbol createMapSymbolIfAbsent(BLangBlockFunctionBody body, int closureMapCount) {
        if (body.mapSymbol == null) {
            body.mapSymbol = createMapSymbol(BLOCK_MAP_SYM_NAME + closureMapCount, env);
        }
        return body.mapSymbol;
    }

    private BVarSymbol createMapSymbolIfAbsent(BLangBlockStmt blockStmt, int closureMapCount) {
        if (blockStmt.mapSymbol == null) {
            blockStmt.mapSymbol = createMapSymbol(BLOCK_MAP_SYM_NAME + closureMapCount, env);
        }
        return blockStmt.mapSymbol;
    }

    private BVarSymbol createMapSymbolIfAbsent(BLangFunction function, int closureMapCount) {
        if (function.mapSymbol == null) {
            function.mapSymbol = createMapSymbol(FUNCTION_MAP_SYM_NAME + closureMapCount, env);
        }
        return function.mapSymbol;
    }

    private BVarSymbol createMapSymbolIfAbsent(BLangClassDefinition classDef, int closureMapCount) {
        if (classDef.oceEnvData.mapBlockMapSymbol == null) {
            classDef.oceEnvData.mapBlockMapSymbol = createMapSymbol(OBJECT_CTOR_MAP_SYM_NAME + closureMapCount,
                    classDef.oceEnvData.capturedClosureEnv);
        }
        return classDef.oceEnvData.mapBlockMapSymbol;
    }

    private BVarSymbol createMapSymbolIfAbsent(BLangClassDefinition classDef, String mapName, boolean blockMap) {
        BVarSymbol mapSymbol;
        if (blockMap) {
            if (classDef.oceEnvData.mapBlockMapSymbol == null) {
                classDef.oceEnvData.mapBlockMapSymbol =
                        createMapSymbol(mapName, classDef.oceEnvData.capturedClosureEnv);
            }
            mapSymbol = classDef.oceEnvData.mapBlockMapSymbol;
        } else {
            if (classDef.oceEnvData.mapFunctionMapSymbol == null) {
                classDef.oceEnvData.mapFunctionMapSymbol =
                        createMapSymbol(mapName, classDef.oceEnvData.capturedClosureEnv);
            }
            mapSymbol = classDef.oceEnvData.mapFunctionMapSymbol;
        }
        return mapSymbol;
    }

    private BVarSymbol createFunctionMapSymbolIfAbsent(BLangClassDefinition classDef, String mapName) {
        if (classDef.oceEnvData.classEnclosedFunctionMap == null) {
            classDef.oceEnvData.classEnclosedFunctionMap =
                    createMapSymbol(mapName, classDef.oceEnvData.capturedClosureEnv);
        }
        return classDef.oceEnvData.classEnclosedFunctionMap;
    }

    private static BVarSymbol getMapSymbol(BLangNode node) {
        switch (node.getKind()) {
            case BLOCK_FUNCTION_BODY:
                return ((BLangBlockFunctionBody) node).mapSymbol;
            case BLOCK:
                return ((BLangBlockStmt) node).mapSymbol;
            case FUNCTION:
            case RESOURCE_FUNC:
                return ((BLangFunction) node).mapSymbol;
            case CLASS_DEFN:
                BLangClassDefinition classNode = (BLangClassDefinition) node;
                if (!classNode.isObjectContructorDecl) {
                    return CLOSURE_MAP_NOT_FOUND;
                }
                return classNode.oceEnvData.mapBlockMapSymbol;
            default:
                return CLOSURE_MAP_NOT_FOUND;
        }
    }

    @Override
    public void visit(BLangReturn returnNode) {
        if (returnNode.expr != null) {
            returnNode.expr = rewriteExpr(returnNode.expr);
        }
        result = returnNode;
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation invocation) {
        rewriteInvocationExpr(invocation);
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
        result = importPkgNode;
    }

    @Override
    public void visit(BLangTypeDefinition typeDef) {
        typeDef.typeNode = rewrite(typeDef.typeNode, env);
        result = typeDef;
    }

    @Override
    public void visit(BLangIntersectionTypeNode intersectionTypeNode) {
        List<BLangType> rewrittenConstituents = new ArrayList<>();
        for (BLangType constituentTypeNode : intersectionTypeNode.constituentTypeNodes) {
            rewrittenConstituents.add(rewrite(constituentTypeNode, env));
        }
        intersectionTypeNode.constituentTypeNodes = rewrittenConstituents;
        result = intersectionTypeNode;
    }

    @Override
    public void visit(BLangClassDefinition classDefinition) {
        if (!classDefinition.isObjectContructorDecl) {
            result = classDefinition;
            return;
        }

        OCEDynamicEnvironmentData oceData = classDefinition.oceEnvData;
        BVarSymbol mapSymbol = oceData.mapBlockMapSymbol;
        if (mapSymbol == null) {
            // This cannot happen.
            result = classDefinition;
            return;
        }

        desugar.visit(classDefinition);
        result = classDefinition;
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        for (BLangSimpleVariable field : objectTypeNode.fields) {
            rewrite(field, env);
        }
        result = objectTypeNode;
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        for (BLangSimpleVariable field : recordTypeNode.fields) {
            field.typeNode = rewrite(field.typeNode, env);
        }
        recordTypeNode.restFieldType = rewrite(recordTypeNode.restFieldType, env);
        result = recordTypeNode;
    }

    @Override
    public void visit(BLangTupleTypeNode tupleTypeNode) {
        List<BLangSimpleVariable> rewrittenMembers = new ArrayList<>();
        tupleTypeNode.members.forEach(member -> rewrittenMembers.add(rewrite(member, env)));
        tupleTypeNode.members = rewrittenMembers;
        tupleTypeNode.restParamType = rewrite(tupleTypeNode.restParamType, env);
        result = tupleTypeNode;
    }

    @Override
    public void visit(BLangFiniteTypeNode finiteTypeNode) {
        result = finiteTypeNode;
    }

    @Override
    public void visit(BLangArrayType arrayType) {
        arrayType.elemtype = rewrite(arrayType.elemtype, env);
        result = arrayType;
    }

    @Override
    public void visit(BLangUserDefinedType userDefinedType) {
        result = userDefinedType;
    }

    @Override
    public void visit(BLangUnionTypeNode unionTypeNode) {
        List<BLangType> rewrittenMembers = new ArrayList<>();
        unionTypeNode.memberTypeNodes.forEach(typeNode -> rewrittenMembers.add(rewrite(typeNode, env)));
        unionTypeNode.memberTypeNodes = rewrittenMembers;
        result = unionTypeNode;
    }

    @Override
    public void visit(BLangValueType valueType) {
        result = valueType;
    }

    @Override
    public void visit(BLangBuiltInRefTypeNode builtInRefTypeNode) {
        result = builtInRefTypeNode;
    }

    @Override
    public void visit(BLangStreamType streamType) {
        streamType.constraint = rewrite(streamType.constraint, env);
        streamType.error = rewrite(streamType.error, env);
        result = streamType;
    }

    @Override
    public void visit(BLangConstrainedType constrainedType) {
        constrainedType.constraint = rewrite(constrainedType.constraint, env);
        result = constrainedType;
    }

    @Override
    public void visit(BLangErrorType errorType) {
        errorType.detailType = rewrite(errorType.detailType, env);
        result = errorType;
    }

    @Override
    public void visit(BLangTableTypeNode tableTypeNode) {
        tableTypeNode.constraint = rewrite(tableTypeNode.constraint, env);
        tableTypeNode.tableKeyTypeConstraint = rewrite(tableTypeNode.tableKeyTypeConstraint, env);
        result = tableTypeNode;
    }

    @Override
    public void visit(BLangTableKeyTypeConstraint keyTypeConstraint) {
        keyTypeConstraint.keyType = rewrite(keyTypeConstraint.keyType, env);
        result = keyTypeConstraint;
    }

    @Override
    public void visit(BLangFunctionTypeNode functionTypeNode) {
        SymbolEnv typeDefEnv =
                            SymbolEnv.createTypeEnv(functionTypeNode, functionTypeNode.getBType().tsymbol.scope, env);
        for (BLangVariable bLangSimpleVariable : functionTypeNode.params) {
            bLangSimpleVariable.typeNode = rewrite(bLangSimpleVariable.typeNode, typeDefEnv);
        }
        if (functionTypeNode.restParam != null) {
            functionTypeNode.restParam.typeNode = rewrite(functionTypeNode.restParam.typeNode, env);
        }
        if (functionTypeNode.returnTypeNode != null) {
            functionTypeNode.returnTypeNode = rewrite(functionTypeNode.returnTypeNode, env);
        }

        result = functionTypeNode;
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
        varNode.typeNode = rewrite(varNode.typeNode, env);
        varNode.expr = rewriteExpr(varNode.expr);
        result = varNode;
    }

    @Override
    public void visit(BLangTupleVariable varNode) {
        varNode.expr = rewriteExpr(varNode.expr);
        result = varNode;
    }

    @Override
    public void visit(BLangRecordVariable varNode) {
        varNode.expr = rewriteExpr(varNode.expr);
        result = varNode;
    }

    @Override
    public void visit(BLangErrorVariable varNode) {
        varNode.expr = rewriteExpr(varNode.expr);
        result = varNode;
    }

    @Override
    public void visit(BLangTupleVariableDef varDefNode) {
        varDefNode.var = rewrite(varDefNode.var, env);
        result = varDefNode;
    }

    @Override
    public void visit(BLangRecordVariableDef varDefNode) {
        varDefNode.var = rewrite(varDefNode.var, env);
        result = varDefNode;
    }

    @Override
    public void visit(BLangErrorVariableDef varDefNode) {
        varDefNode.errorVariable = rewrite(varDefNode.errorVariable, env);
        result = varDefNode;
    }

    @Override
    public void visit(BLangAssignment assignNode) {
        assignNode.varRef = rewriteExpr(assignNode.varRef);
        if (assignNode.expr.impConversionExpr != null) {
            types.setImplicitCastExpr(assignNode.expr.impConversionExpr, assignNode.expr.impConversionExpr.getBType(),
                                      assignNode.varRef.getBType());
        } else {
            types.setImplicitCastExpr(assignNode.expr, assignNode.expr.getBType(), assignNode.varRef.getBType());
        }
        assignNode.expr = rewriteExpr(assignNode.expr);
        result = assignNode;
    }

    @Override
    public void visit(BLangTupleDestructure tupleDestructure) {
        result = tupleDestructure;
    }

    @Override
    public void visit(BLangRecordDestructure recordDestructure) {
        result = recordDestructure;
    }

    @Override
    public void visit(BLangErrorDestructure errorDestructure) {
        result = errorDestructure;
    }

    @Override
    public void visit(BLangRetry retryNode) {
        retryNode.retryBody = rewrite(retryNode.retryBody, env);
        result = retryNode;
    }

    @Override
    public void visit(BLangRetryTransaction retryTransaction) {
        retryTransaction.transaction = rewrite(retryTransaction.transaction, env);
        result = retryTransaction;
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
    public void visit(BLangPanic panicNode) {
        panicNode.expr = rewriteExpr(panicNode.expr);
        result = panicNode;
    }
    public void visit(BLangDo doNode) {
        doNode.body = rewrite(doNode.body, env);
        result = doNode;
    }

    @Override
    public void visit(BLangXMLNSStatement xmlnsStmtNode) {
        xmlnsStmtNode.xmlnsDecl = rewrite(xmlnsStmtNode.xmlnsDecl, env);
        result = xmlnsStmtNode;
    }

    @Override
    public void visit(BLangXMLNS xmlnsNode) {
        xmlnsNode.namespaceURI = rewriteExpr(xmlnsNode.namespaceURI);
        result = xmlnsNode;
    }

    @Override
    public void visit(BLangExpressionStmt exprStmtNode) {
        exprStmtNode.expr = rewriteExpr(exprStmtNode.expr);
        result = exprStmtNode;
    }

    @Override
    public void visit(BLangFail failNode) {
        if (failNode.exprStmt != null) {
            failNode.exprStmt = rewrite(failNode.exprStmt, env);
        }
        result = failNode;
    }

    @Override
    public void visit(BLangIf ifNode) {
        ifNode.expr = rewriteExpr(ifNode.expr);
        ifNode.body = rewrite(ifNode.body, env);
        ifNode.elseStmt = rewrite(ifNode.elseStmt, env);
        result = ifNode;
    }

    @Override
    public void visit(BLangForeach foreach) {
        result = foreach;
    }

    @Override
    public void visit(BLangWhile whileNode) {
        whileNode.expr = rewriteExpr(whileNode.expr);
        whileNode.body = rewrite(whileNode.body, env);
        result = whileNode;
    }

    @Override
    public void visit(BLangLock lockNode) {
        lockNode.body = rewrite(lockNode.body, env);
        result = lockNode;
    }

    @Override
    public void visit(BLangLockStmt lockNode) {
        result = lockNode;
    }

    @Override
    public void visit(BLangUnLockStmt unLockNode) {
        result = unLockNode;
    }

    @Override
    public void visit(BLangTransaction transactionNode) {
        transactionNode.transactionBody = rewrite(transactionNode.transactionBody, env);
        result = transactionNode;
    }

    @Override
    public void visit(BLangRollback rollbackNode) {
        rollbackNode.expr = rewriteExpr(rollbackNode.expr);
        result = rollbackNode;
    }

    @Override
    public void visit(BLangTransactionalExpr transactionalExpr) {
        result = transactionalExpr;
    }

    @Override
    public void visit(BLangCommitExpr commitExpr) {
        result = commitExpr;
    }

    @Override
    public void visit(BLangForkJoin forkJoin) {
        result = forkJoin;
    }

    @Override
    public void visit(BLangLiteral literalExpr) {
        result = literalExpr;
    }

    @Override
    public void visit(BLangListConstructorSpreadOpExpr listConstructorSpreadOpExpr) {
        rewriteExpr(listConstructorSpreadOpExpr.expr);
        result = listConstructorSpreadOpExpr;
    }

    @Override
    public void visit(BLangListConstructorExpr listConstructorExpr) {
        rewriteExprs(listConstructorExpr.exprs);
        result = listConstructorExpr;
    }

    @Override
    public void visit(BLangTableConstructorExpr tableConstructorExpr) {
        rewriteExprs(tableConstructorExpr.recordLiteralList);
        result = tableConstructorExpr;
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangJSONArrayLiteral jsonArrayLiteral) {
        rewriteExprs(jsonArrayLiteral.exprs);
        result = jsonArrayLiteral;
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangTupleLiteral tupleLiteral) {
        rewriteExprs(tupleLiteral.exprs);
        result = tupleLiteral;
    }

    @Override
    public void visit(BLangListConstructorExpr.BLangArrayLiteral arrayLiteral) {
        rewriteExprs(arrayLiteral.exprs);
        result = arrayLiteral;
    }

    @Override
    public void visit(BLangRecordLiteral recordLiteral) {
        // Process the key-val pairs in the record literal.
        recordLiteral.fields.forEach(field -> {
            BLangRecordLiteral.BLangRecordKeyValueField keyValue = (BLangRecordLiteral.BLangRecordKeyValueField) field;
            keyValue.key.expr = rewriteExpr(keyValue.key.expr);
            keyValue.valueExpr = rewriteExpr(keyValue.valueExpr);
        });
        result = recordLiteral;
    }

    @Override
    public void visit(BLangSimpleVarRef varRefExpr) {
        result = varRefExpr;
    }

    @Override
    public void visit(BLangFieldBasedAccess fieldAccessExpr) {
        fieldAccessExpr.expr = rewriteExpr(fieldAccessExpr.expr);
        result = fieldAccessExpr;
    }

    @Override
    public void visit(BLangIndexBasedAccess indexAccessExpr) {
        result = desugar.rewriteExpr(indexAccessExpr);
    }

    public BPackageSymbol getPackageSymbol(BSymbol symbol) {
        BSymbol owner = symbol.owner;
        if (owner.getKind() == SymbolKind.PACKAGE) {
            return (BPackageSymbol) owner;
        }
        return getPackageSymbol(owner);
    }

    @Override
    public void visit(BLangInvocation invocation) {
        rewriteInvocationExpr(invocation);
    }

    public void rewriteInvocationExpr(BLangInvocation invocation) {
        rewriteExprs(invocation.requiredArgs);
        rewriteExprs(invocation.restArgs);
        result = invocation;
    }

    public BLangSimpleVariableDef createVarDef(String name, BType type, BLangExpression expr, Location location) {
        BVarSymbol symbol = new BVarSymbol(0, names.fromString(name), this.env.scope.owner.pkgID, type,
                ((BLangFunction) this.env.enclInvokable).originalFuncSymbol, location, VIRTUAL);
        BLangSimpleVariable simpleVariable = ASTBuilderUtil.createVariable(location, name, type, expr, symbol);
        BLangSimpleVariableDef simpleVariableDef = ASTBuilderUtil.createVariableDef(location);
        simpleVariableDef.var = simpleVariable;
        simpleVariableDef.setBType(simpleVariable.getBType());
        return simpleVariableDef;
    }

    @Override
    public void visit(BLangErrorConstructorExpr errorConstructorExpr) {
        rewriteExprs(errorConstructorExpr.positionalArgs);
        errorConstructorExpr.errorDetail = rewriteExpr(errorConstructorExpr.errorDetail);
        result = errorConstructorExpr;
    }

    public void visit(BLangTypeInit typeInitExpr) {
        typeInitExpr.initInvocation = rewriteExpr(typeInitExpr.initInvocation);
        result = typeInitExpr;
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
        List<BLangExpression> exprList = new ArrayList<>();
        waitExpr.exprList.forEach(expression -> exprList.add(rewriteExpr(expression)));
        waitExpr.exprList = exprList;
        result = waitExpr;
    }

    @Override
    public void visit(BLangWaitForAllExpr waitExpr) {
        result = waitExpr;
    }

    @Override
    public void visit(BLangTrapExpr trapExpr) {
        trapExpr.expr = rewriteExpr(trapExpr.expr);
        result = trapExpr;
    }

    @Override
    public void visit(BLangBinaryExpr binaryExpr) {
        binaryExpr.lhsExpr.parent = binaryExpr;
        binaryExpr.lhsExpr = rewriteExpr(binaryExpr.lhsExpr);
        binaryExpr.rhsExpr.parent = binaryExpr;
        binaryExpr.rhsExpr = rewriteExpr(binaryExpr.rhsExpr);

        result = binaryExpr;
    }

    @Override
    public void visit(BLangElvisExpr elvisExpr) {
        result = elvisExpr;
    }

    @Override
    public void visit(BLangGroupExpr groupExpr) {
        groupExpr.expression = rewriteExpr(groupExpr.expression);
        result = groupExpr;
    }

    @Override
    public void visit(BLangUnaryExpr unaryExpr) {
        unaryExpr.expr = rewriteExpr(unaryExpr.expr);
        result = unaryExpr;
    }

    @Override
    public void visit(BLangTypeConversionExpr conversionExpr) {
        // If there is an implicit cast on the expr of the type conversion expr already we do not rewrite it. This is
        // to avoid stackoverflow error.
        if (conversionExpr.expr.impConversionExpr != null) {
            result = conversionExpr;
            return;
        }
        conversionExpr.expr = rewriteExpr(conversionExpr.expr);
        conversionExpr.typeNode = rewrite(conversionExpr.typeNode, env);
        result = conversionExpr;
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {

        SymbolEnv symbolEnv = env.createClone();
        bLangLambdaFunction.capturedClosureEnv = symbolEnv;
        bLangLambdaFunction.function = rewrite(bLangLambdaFunction.function, symbolEnv);

        if (symbolEnv.enclInvokable != null) {
            BLangFunction enclInvokable = (BLangFunction) symbolEnv.enclInvokable;
            // Save param closure map of the encl invokable.
            bLangLambdaFunction.paramMapSymbolsOfEnclInvokable = enclInvokable.paramClosureMap;
            boolean isWorker = bLangLambdaFunction.function.flagSet.contains(Flag.WORKER);
            bLangLambdaFunction.enclMapSymbols = collectClosureMapSymbols(symbolEnv, enclInvokable, isWorker);
        }
        result = bLangLambdaFunction;
    }

    private TreeMap<Integer, BVarSymbol> collectClosureMapSymbols(SymbolEnv symbolEnv, BLangInvokableNode enclInvokable,
                                                                  boolean isWorker) {
        // Recursively iterate back to the encl invokable and get all map symbols visited.
        TreeMap<Integer, BVarSymbol> enclMapSymbols = new TreeMap<>();
        while (symbolEnv != null && symbolEnv.enclInvokable == enclInvokable) {
            BVarSymbol mapSym = getMapSymbol(symbolEnv.node);

            // Skip non-block bodies
            if (mapSym == CLOSURE_MAP_NOT_FOUND) {
                symbolEnv = symbolEnv.enclEnv;
                continue;
            }

            if (mapSym != null) {
                enclMapSymbols.putIfAbsent(symbolEnv.envCount, mapSym);
            } else if (isWorker) {
                // Create mapSymbol in outer function node when it contain workers and it's not already created.
                // We need this to allow worker identifier to be used as a future.
                BVarSymbol mapSymbolIfAbsent = createMapSymbolIfAbsent(env.node, blockClosureMapCount);
                enclMapSymbols.putIfAbsent(symbolEnv.envCount, mapSymbolIfAbsent);
            }

            symbolEnv = symbolEnv.enclEnv;
        }
        return enclMapSymbols;
    }

    @Override
    public void visit(BLangArrowFunction bLangArrowFunction) {
        result = bLangArrowFunction;
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
        rewriteExprs(xmlElementLiteral.modifiedChildren);
        rewriteExprs(xmlElementLiteral.attributes);
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
        xmlProcInsLiteral.dataFragments.forEach(this::rewriteExpr);
        result = xmlProcInsLiteral;
    }

    @Override
    public void visit(BLangXMLQuotedString xmlQuotedString) {
        xmlQuotedString.concatExpr = rewriteExpr(xmlQuotedString.concatExpr);
        result = xmlQuotedString;
    }

    @Override
    public void visit(BLangStringTemplateLiteral stringTemplateLiteral) {
        stringTemplateLiteral.exprs.forEach(this::rewriteExpr);
        result = stringTemplateLiteral;
    }

    @Override
    public void visit(BLangWorkerSend workerSendNode) {
        workerSendNode.expr = rewriteExpr(workerSendNode.expr);
        result = workerSendNode;
    }

    @Override
    public void visit(BLangWorkerSyncSendExpr syncSendExpr) {
        syncSendExpr.expr = rewriteExpr(syncSendExpr.expr);
        result = syncSendExpr;
    }

    @Override
    public void visit(BLangWorkerReceive workerReceiveNode) {
        result = workerReceiveNode;
    }

    @Override
    public void visit(BLangWorkerFlushExpr workerFlushExpr) {
        result = workerFlushExpr;
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangLocalVarRef localVarRef) {
        BSymbol varRefSym = localVarRef.symbol;
        BLangInvokableNode enclInvokable = env.enclInvokable;
        if (!varRefSym.closure || localVarRef.closureDesugared || enclInvokable == null) {
            result = localVarRef;
            return;
        }
        // If it is marked as a closure variable then the following calculations are carried out.
        // 1) Find the resolved level i.e. the absolute level : level the variable was resolved from.
        int absoluteLevel = findResolvedLevel(env, (BVarSymbol) localVarRef.varSymbol);

        // self absolute level : level I'm currently in.
        int selfAbsoluteLevel = env.envCount;

        // self relative count : block nodes above me in the same function.
        int selfRelativeCount = env.relativeEnvCount;

        // 2) Update the variable reference.

        // selfRelativeCount >= selfAbsoluteLevel - absoluteLevel ==> resolved within the same function.
        if (selfRelativeCount >= selfAbsoluteLevel - absoluteLevel) {

            // Go up within the block node
            SymbolEnv symbolEnv = env;
            NodeKind nodeKind = symbolEnv.node.getKind();
            while (symbolEnv != null && nodeKind != NodeKind.PACKAGE) {
                // Check if the node is a sequence statement
                if (symbolEnv.envCount == absoluteLevel) {
                    BVarSymbol mapSym = createMapSymbolIfAbsent(symbolEnv.node, symbolEnv.envCount);

                    // `mapSym` will not be null for block function bodies, block stmts, functions and classes. And we
                    // only need to update closure vars for those nodes.
                    if (mapSym != null) {
                        updateClosureVars(localVarRef, mapSym);
                        return;
                    }
                }

                symbolEnv = symbolEnv.enclEnv;
                nodeKind = symbolEnv.node.getKind();
            }
        } else {
            // It is resolved from a parameter map.
            // Add parameter map symbol if one is not added.
            BLangFunction invokableFunc = (BLangFunction) env.enclInvokable;
            if (invokableFunc.flagSet.contains(Flag.OBJECT_CTOR) && invokableFunc.attachedFunction) {
                // a + b ==> a + $map$objectCtor_4[i]
                BLangClassDefinition classDef = (BLangClassDefinition) invokableFunc.parent;
                BLangFunction enclosedFunction = (BLangFunction) classDef.oceEnvData.capturedClosureEnv.enclInvokable;
                BInvokableSymbol enclosedFuncSymbol = enclosedFunction.symbol;
                if (enclosedFuncSymbol.params.contains(localVarRef.symbol)) {
                    // I am a parameter
                    BVarSymbol classFunctionMapSymbol = addFunctionMapMapToClassDefinition(classDef,
                            enclosedFunction.mapSymbol);
                    updateClosureVarsWithMapAccessExpression(classDef, invokableFunc.receiver.symbol,
                            localVarRef, classFunctionMapSymbol);
                } else {
                    updateClosureVarsForAttachedObjects(classDef, invokableFunc.receiver.symbol, localVarRef);
                }
            } else {
                // Update the closure vars.
                invokableFunc.paramClosureMap.putIfAbsent(absoluteLevel, createMapSymbol(
                        PARAMETER_MAP_NAME + absoluteLevel, env));
                updateClosureVars(localVarRef, ((BLangFunction) env.enclInvokable).paramClosureMap.get(absoluteLevel));
            }
        }

        // 3) Add the resolved level of the closure variable to the preceding function maps.
        updatePrecedingFunc(env, absoluteLevel);
    }

    @Override
    public void visit(BLangIgnoreExpr ignoreExpr) {
        result = ignoreExpr;
    }

    @Override
    public void visit(BLangDynamicArgExpr dynamicParamExpr) {
        dynamicParamExpr.condition = rewriteExpr(dynamicParamExpr.condition);
        dynamicParamExpr.conditionalArgument = rewriteExpr(dynamicParamExpr.conditionalArgument);
        result = dynamicParamExpr;
    }

    @Override
    public void visit(BLangRegExpTemplateLiteral regExpTemplateLiteral) {
        List<BLangExpression> interpolationsList =
                symResolver.getListOfInterpolations(regExpTemplateLiteral.reDisjunction.sequenceList);
        interpolationsList.forEach(this::rewriteExpr);
        result = regExpTemplateLiteral;
    }

    /**
     * Find the resolved level of the closure variable.
     *
     * @param symbolEnv symbol environment
     * @param varSymbol symbol of the closure
     * @return level it was resolved
     */
    private static int findResolvedLevel(SymbolEnv symbolEnv, BVarSymbol varSymbol) {
        SymbolEnv resolvedSymbolEnv = symbolEnv;
        while (resolvedSymbolEnv != null && resolvedSymbolEnv.node.getKind() != NodeKind.PACKAGE) {
            Scope.ScopeEntry entry = resolvedSymbolEnv.scope.lookup(varSymbol.name);
            if (entry != NOT_FOUND_ENTRY && varSymbol == entry.symbol &&
                    varSymbol.owner == resolvedSymbolEnv.scope.owner) {
                return resolvedSymbolEnv.envCount;
            }
            resolvedSymbolEnv = resolvedSymbolEnv.enclEnv;
        }
        // 0 is returned if it was not found.
        return 0;
    }

    /**
     * Update the preceeding functions.
     *
     * @param symbolEnv     symbol environment
     * @param resolvedLevel resolved level of the closure variable
     */
    private void updatePrecedingFunc(SymbolEnv symbolEnv, int resolvedLevel) {
        while (symbolEnv != null && symbolEnv.node.getKind() != NodeKind.PACKAGE) {
            // If the symbol env count equals to the resolved level then return.
            if (symbolEnv.envCount == resolvedLevel) {
                return;
            }

            // If the node is not a function, then get its enclosing env and continue.
            if (symbolEnv.node.getKind() != NodeKind.FUNCTION) {
                symbolEnv = symbolEnv.enclEnv;
                continue;
            }

            // If the node is a function, update it accordingly.
            BLangFunction bLangFunction = (BLangFunction) symbolEnv.node;
            if (symbolEnv.enclInvokable == env.enclInvokable) {
                symbolEnv = symbolEnv.enclEnv;
                continue;
            }
            if (bLangFunction.paramClosureMap.containsKey(resolvedLevel)) {
                return;
            }
            bLangFunction.paramClosureMap
                    .put(resolvedLevel, createMapSymbol(PARAMETER_MAP_NAME + resolvedLevel, symbolEnv));

            symbolEnv = symbolEnv.enclEnv;
        }
    }

    /**
     * Create the map symbol required for the function node and block statements.
     *
     * @param mapName   name of the map to be created
     * @param symbolEnv symbol environment
     * @return map symbol created
     */
    private BVarSymbol createMapSymbol(String mapName, SymbolEnv symbolEnv) {
        return new BVarSymbol(0, names.fromString(mapName), symbolEnv.scope.owner.pkgID,
                              symTable.mapAllType, symbolEnv.scope.owner, symTable.builtinPos, VIRTUAL);
    }

    /**
     * Update the closure maps with the relevant map access expression.
     *
     * @param varRefExpr closure variable reference to be updated
     * @param mapSymbol  map symbol to be used
     */
    private void updateClosureVars(BLangSimpleVarRef varRefExpr, BVarSymbol mapSymbol) {
        // Create the index based access expression.
        // [varRefExpr]
        BLangLiteral indexExpr = ASTBuilderUtil.createLiteral(varRefExpr.pos, symTable.stringType,
                varRefExpr.varSymbol.name.value);
        // mapSymbol[varRefExpr]
        BLangIndexBasedAccess accessExpr = ASTBuilderUtil.createIndexBasesAccessExpr(varRefExpr.pos,
                varRefExpr.getBType(),
                mapSymbol, indexExpr);

        result = rewriteExpr(accessExpr);
    }

    /**
     * Update the closure variables with the relevant map access expression.
     *  a + b can become a + self[$map$func_4][b]
     *  a + b ==> a + self[$map$func_4][b]
     *  we are dealing with the simple var ref `b` here
     * @param classDef   OCE class definition
     * @param selfSymbol self symbol of attached method
     * @param varRefExpr closure variable reference to be updated
     */
    private void updateClosureVarsWithMapAccessExpression(BLangClassDefinition classDef,
                                                          BVarSymbol selfSymbol,
                                                          BLangSimpleVarRef varRefExpr,
                                                          BVarSymbol mapSymbol) {
        // self
        BLangSimpleVarRef.BLangLocalVarRef localSelfVarRef = new BLangSimpleVarRef.BLangLocalVarRef(selfSymbol);
        localSelfVarRef.setBType(classDef.getBType());
        Location pos = varRefExpr.pos;

        // self[mapSymbol]
        BLangIndexBasedAccess.BLangStructFieldAccessExpr accessExprForClassField =
                new BLangIndexBasedAccess.BLangStructFieldAccessExpr(pos, localSelfVarRef,
                        ASTBuilderUtil.createLiteral(pos, symTable.stringType,
                                mapSymbol.name), mapSymbol, false, true);
        accessExprForClassField.setBType(mapSymbol.type);
        accessExprForClassField.isLValue = true;

        // self[mapSymbol][varRefExpr]
        BLangIndexBasedAccess.BLangMapAccessExpr closureMapAccessForField =
                new BLangIndexBasedAccess.BLangMapAccessExpr(pos, accessExprForClassField,
                        ASTBuilderUtil.createLiteral(pos, symTable.stringType, varRefExpr.symbol.name));
        closureMapAccessForField.setBType(varRefExpr.symbol.type);
        closureMapAccessForField.isLValue = false;

        // <varRefExpr.type> self[mapSymbol][varRefExpr]
        BLangTypeConversionExpr castExpr = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        castExpr.expr = closureMapAccessForField;
        castExpr.setBType(varRefExpr.symbol.type);
        castExpr.targetType = varRefExpr.symbol.type;
        result = desugar.rewrite(castExpr, classDef.oceEnvData.objMethodsEnv);
    }

    /**
     * Update the closure variables with the relevant map access expression.
     *  a + b can become a + self[$map$objectCtor_3][b]
     *  a + b ==> a + self[$map$objectCtor_3][b]
     *  we are dealing with the simple var ref `b` here
     * @param classDef   OCE class definition
     * @param classSelfSymbol self symbol of attached method
     * @param varRefExpr closure variable reference to be updated
     */
    private void updateClosureVarsForAttachedObjects(BLangClassDefinition classDef,
                                                     BVarSymbol classSelfSymbol,
                                                     BLangSimpleVarRef varRefExpr) {
        OCEDynamicEnvironmentData oceEnvData = classDef.oceEnvData;
        SymbolEnv enclEnv = oceEnvData.capturedClosureEnv.enclEnv;
        BLangNode nodeKeepingClosureMap = enclEnv.node;
        if (!enclEnv.scope.entries.containsKey(varRefExpr.symbol.name)) {
            if (nodeKeepingClosureMap.getKind() == NodeKind.FUNCTION) {
                BLangBlockFunctionBody blockFunctionBody =
                        (BLangBlockFunctionBody) ((BLangFunction) nodeKeepingClosureMap).body;
                nodeKeepingClosureMap = blockFunctionBody;
            }
        }
        BVarSymbol blockMap = createMapSymbolIfAbsent(nodeKeepingClosureMap, blockClosureMapCount);
        BVarSymbol classMapSymbol = createMapSymbolIfAbsent(classDef, blockMap.name.value, true);
        BVarSymbol parentBlockMap = null;
        if (!oceEnvData.parents.isEmpty()) {
            BLangClassDefinition parentClass = oceEnvData.parents.get(0);
            BVarSymbol symbol = (BVarSymbol) varRefExpr.symbol;
            OCEDynamicEnvironmentData parentData = parentClass.oceEnvData;
            if (parentData.closureBlockSymbols.contains(symbol)) {
                // symbol should come from previous class block map
                updateClassClosureMap(parentClass);
                parentBlockMap = parentData.mapBlockMapSymbol; // TODO: use createMapSymbolIfAbsent
            }

            // This is a work-around. Every function which has closures which are not arguments has a block level
            // closure map. This should not be added attached methods. But at the moment since we leverage closure
            // desugar infrastructure they are being populated. This is fixed by reassigning block level closure map
            // again to block function body map.
            // TODO: ideally this should be done for all needed attached functions properly and we dont need to
            //  populate block map. Block map is currently initialized with a empty literal in desugar.
            //
            // map<any|error> blockFunctionBodyMap = {}
            // map<any|error> $passThroughMap = self[blockLevelClosureMap]
            // blockMap = $$passThroughMap;
            if (nodeKeepingClosureMap.getKind() == NodeKind.BLOCK_FUNCTION_BODY) {
                BLangFunction function = (BLangFunction) nodeKeepingClosureMap.parent;
                BLangBlockFunctionBody body = (BLangBlockFunctionBody) nodeKeepingClosureMap;
                if (function.flagSet.contains(Flag.OBJECT_CTOR) && function.flagSet.contains(Flag.ATTACHED)) {
                    if (parentBlockMap != null) {
                        BVarSymbol selfSymbol = function.receiver.symbol;
                        BLangSimpleVarRef selfVarRef = ASTBuilderUtil.createVariableRef(function.pos, selfSymbol);

                        BLangIdentifier identifierNode = ASTBuilderUtil.createIdentifier(function.pos,
                                parentBlockMap.name.value);
                        BLangFieldBasedAccess fieldAccess = ASTBuilderUtil.createFieldAccessExpr(selfVarRef,
                                identifierNode);
                        fieldAccess.symbol = parentBlockMap;
                        fieldAccess.setBType(classMapSymbol.type);
                        fieldAccess.expectedType = classMapSymbol.type;
                        fieldAccess.isStoreOnCreation = true;
                        fieldAccess.isLValue = false;
                        fieldAccess.fieldKind = FieldKind.SINGLE;
                        fieldAccess.leafNode = true;

                        SymbolEnv env = parentData.capturedClosureEnv;

                        BVarSymbol parentBlockSymbol = new BVarSymbol(0,
                                names.fromString("$passThroughMap"),
                                env.scope.owner.pkgID, parentBlockMap.getType(), env.scope.owner, body.pos, VIRTUAL);

                        BLangSimpleVariable blockMapVar = ASTBuilderUtil.createVariable(body.pos,
                                parentBlockMap.name.value,
                                parentBlockMap.getType(), fieldAccess, parentBlockSymbol);

                        body.scope.define(blockMapVar.symbol.name, parentBlockSymbol);
                        BLangSimpleVariableDef returnResultDef = ASTBuilderUtil.createVariableDef(body.pos,
                                blockMapVar);
                        returnResultDef = desugar.rewrite(returnResultDef, env);

                        BLangSimpleVarRef blockRef = ASTBuilderUtil.createVariableRef(function.pos, blockMap);
                        BLangSimpleVarRef rempVar = ASTBuilderUtil.createVariableRef(function.pos, parentBlockSymbol);

                        BLangAssignment assignmentStmt = (BLangAssignment) TreeBuilder.createAssignmentNode();
                        assignmentStmt.expr = rempVar;
                        assignmentStmt.pos = function.pos;
                        assignmentStmt.setVariable(blockRef);

                        assignmentStmt = desugar.rewrite(assignmentStmt, env);

                        body.stmts.add(0, returnResultDef);
                        body.stmts.add(1, assignmentStmt);
                    }
                }
            }
        }

        updateClosureVarsWithMapAccessExpression(classDef, classSelfSymbol, varRefExpr, classMapSymbol);
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFieldVarRef fieldVarRef) {
        result = fieldVarRef;
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangPackageVarRef packageVarRef) {
        result = packageVarRef;
    }

    @Override
    public void visit(BLangConstRef constRef) {
        result = constRef;
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangFunctionVarRef functionVarRef) {
        result = functionVarRef;
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStructFieldAccessExpr fieldAccessExpr) {
        fieldAccessExpr.indexExpr = rewriteExpr(fieldAccessExpr.indexExpr);
        fieldAccessExpr.expr = rewriteExpr(fieldAccessExpr.expr);
        result = fieldAccessExpr;
    }

    @Override
    public void visit(BLangFieldBasedAccess.BLangStructFunctionVarRef functionVarRef) {
        functionVarRef.expr = rewriteExpr(functionVarRef.expr);
        result = functionVarRef;
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangMapAccessExpr mapKeyAccessExpr) {
        mapKeyAccessExpr.indexExpr = rewriteExpr(mapKeyAccessExpr.indexExpr);
        mapKeyAccessExpr.expr = rewriteExpr(mapKeyAccessExpr.expr);
        result = mapKeyAccessExpr;
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTableAccessExpr tableKeyAccessExpr) {
        tableKeyAccessExpr.indexExpr = rewriteExpr(tableKeyAccessExpr.indexExpr);
        tableKeyAccessExpr.expr = rewriteExpr(tableKeyAccessExpr.expr);
        result = tableKeyAccessExpr;
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangArrayAccessExpr arrayIndexAccessExpr) {
        arrayIndexAccessExpr.indexExpr = rewriteExpr(arrayIndexAccessExpr.indexExpr);
        arrayIndexAccessExpr.expr = rewriteExpr(arrayIndexAccessExpr.expr);
        result = arrayIndexAccessExpr;
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangTupleAccessExpr arrayIndexAccessExpr) {
        arrayIndexAccessExpr.indexExpr = rewriteExpr(arrayIndexAccessExpr.indexExpr);
        arrayIndexAccessExpr.expr = rewriteExpr(arrayIndexAccessExpr.expr);
        result = arrayIndexAccessExpr;
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangXMLAccessExpr xmlIndexAccessExpr) {
        xmlIndexAccessExpr.indexExpr = rewriteExpr(xmlIndexAccessExpr.indexExpr);
        xmlIndexAccessExpr.expr = rewriteExpr(xmlIndexAccessExpr.expr);
        result = xmlIndexAccessExpr;
    }


    @Override
    public void visit(BLangXMLElementAccess xmlElementAccess) {
        xmlElementAccess.expr = rewriteExpr(xmlElementAccess.expr);
        result = xmlElementAccess;
    }

    @Override
    public void visit(BLangXMLNavigationAccess xmlNavigation) {
        xmlNavigation.expr = rewriteExpr(xmlNavigation.expr);
        xmlNavigation.childIndex = rewriteExpr(xmlNavigation.childIndex);
        result = xmlNavigation;
    }


    @Override
    public void visit(BLangIndexBasedAccess.BLangJSONAccessExpr jsonAccessExpr) {
        jsonAccessExpr.indexExpr = rewriteExpr(jsonAccessExpr.indexExpr);
        jsonAccessExpr.expr = rewriteExpr(jsonAccessExpr.expr);
        result = jsonAccessExpr;
    }

    @Override
    public void visit(BLangIndexBasedAccess.BLangStringAccessExpr stringAccessExpr) {
        stringAccessExpr.indexExpr = rewriteExpr(stringAccessExpr.indexExpr);
        stringAccessExpr.expr = rewriteExpr(stringAccessExpr.expr);
        result = stringAccessExpr;
    }

    @Override
    public void visit(BLangRecordLiteral.BLangMapLiteral mapLiteral) {
        for (RecordLiteralNode.RecordField field : mapLiteral.fields) {
            if (field.isKeyValueField()) {
                BLangRecordLiteral.BLangRecordKeyValueField keyValueField =
                        (BLangRecordLiteral.BLangRecordKeyValueField) field;
                keyValueField.key.expr = rewriteExpr(keyValueField.key.expr);
                keyValueField.valueExpr = rewriteExpr(keyValueField.valueExpr);
                continue;
            }

            BLangRecordLiteral.BLangRecordSpreadOperatorField spreadField =
                    (BLangRecordLiteral.BLangRecordSpreadOperatorField) field;
            spreadField.expr = rewriteExpr(spreadField.expr);
        }
        result = mapLiteral;
    }

    @Override
    public void visit(BLangRecordLiteral.BLangStructLiteral structLiteral) {
        SymbolEnv symbolEnv = env.createClone();
        BLangFunction enclInvokable = (BLangFunction) symbolEnv.enclInvokable;
        structLiteral.enclMapSymbols = collectClosureMapSymbols(symbolEnv, enclInvokable, false);

        for (RecordLiteralNode.RecordField field : structLiteral.fields) {
            if (field.isKeyValueField()) {
                BLangRecordLiteral.BLangRecordKeyValueField keyValueField =
                        (BLangRecordLiteral.BLangRecordKeyValueField) field;
                keyValueField.key.expr = rewriteExpr(keyValueField.key.expr);
                keyValueField.valueExpr = rewriteExpr(keyValueField.valueExpr);
                continue;
            }

            BLangRecordLiteral.BLangRecordSpreadOperatorField spreadField =
                    (BLangRecordLiteral.BLangRecordSpreadOperatorField) field;
            spreadField.expr = rewriteExpr(spreadField.expr);
        }

        result = structLiteral;
    }

    @Override
    public void visit(BLangWaitForAllExpr.BLangWaitLiteral waitLiteral) {
        waitLiteral.keyValuePairs.forEach(keyValue -> {
            if (keyValue.valueExpr != null) {
                keyValue.valueExpr = rewriteExpr(keyValue.valueExpr);
            } else {
                keyValue.keyExpr = rewriteExpr(keyValue.keyExpr);
            }
        });
        result = waitLiteral;
    }

    @Override
    public void visit(BLangIsAssignableExpr assignableExpr) {
        assignableExpr.lhsExpr = rewriteExpr(assignableExpr.lhsExpr);
        result = assignableExpr;
    }

    @Override
    public void visit(BLangInvocation.BFunctionPointerInvocation fpInvocation) {
        fpInvocation.expr = rewriteExpr(fpInvocation.expr);
        rewriteInvocationExpr(fpInvocation);
    }

    @Override
    public void visit(BLangTypedescExpr accessExpr) {
        result = accessExpr;
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

    @Override
    public void visit(BLangCheckedExpr checkedExpr) {
        result = checkedExpr;
    }

    @Override
    public void visit(BLangServiceConstructorExpr serviceConstructorExpr) {
        result = serviceConstructorExpr;
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
    public void visit(BLangAnnotAccessExpr annotAccessExpr) {
        annotAccessExpr.expr = rewriteExpr(annotAccessExpr.expr);
        result = annotAccessExpr;
    }

    @Override
    public void visit(BLangStatementExpression bLangStatementExpression) {
        BLangStatement exprStmt = bLangStatementExpression.stmt;
        if (exprStmt.getKind() == NodeKind.BLOCK) {
            BLangBlockStmt bLangBlockStmt = (BLangBlockStmt) exprStmt;
            if (bLangBlockStmt.isLetExpr) {
                // In let expression's object constructor expression, the function block already has an OCE mapSymbol,
                // which should be propagated to block statement's mapSymbol.
                if (bLangBlockStmt.mapSymbol == null
                        && Types.getReferredType(bLangStatementExpression.getBType()).tag == TypeTags.OBJECT
                        && env.node.getKind() == NodeKind.BLOCK_FUNCTION_BODY) {
                    bLangBlockStmt.mapSymbol = ((BLangBlockFunctionBody) env.node).mapSymbol;
                }
                bLangStatementExpression.stmt = rewrite(exprStmt, env);
            } else {
                for (int i = 0; i < bLangBlockStmt.stmts.size(); i++) {
                    BLangStatement stmt = bLangBlockStmt.stmts.remove(i);
                    bLangBlockStmt.stmts.add(i, rewrite(stmt, env));
                }
            }
        } else {
            bLangStatementExpression.stmt = rewrite(exprStmt, env);
        }
        bLangStatementExpression.expr = rewriteExpr(bLangStatementExpression.expr);
        result = bLangStatementExpression;
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation invocation) {
        rewriteInvocationExpr(invocation);
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

    @Override
    public void visit(BLangConstant constant) {
        result = constant;
    }

    @Override
    public void visit(BLangNumericLiteral literalExpr) {
        result = literalExpr;
    }

    @Override
    public void visit(BLangTupleVarRef varRefExpr) {
        result = varRefExpr;
    }

    @Override
    public void visit(BLangRecordVarRef varRefExpr) {
        result = varRefExpr;
    }

    @Override
    public void visit(BLangErrorVarRef varRefExpr) {
        result = varRefExpr;
    }

    @Override
    public void visit(BLangSimpleVarRef.BLangTypeLoad typeLoad) {
        result = typeLoad;
    }

    @Override
    public void visit(BLangRecordLiteral.BLangChannelLiteral channelLiteral) {
        channelLiteral.fields.forEach(field -> {
            BLangRecordLiteral.BLangRecordKeyValueField keyValue = (BLangRecordLiteral.BLangRecordKeyValueField) field;
            keyValue.key.expr = rewriteExpr(keyValue.key.expr);
            keyValue.valueExpr = rewriteExpr(keyValue.valueExpr);
        });
        result = channelLiteral;
    }

    @Override
    public void visit(BLangXMLNS.BLangLocalXMLNS xmlnsNode) {
        xmlnsNode.namespaceURI = rewriteExpr(xmlnsNode.namespaceURI);
        result = xmlnsNode;
    }

    @Override
    public void visit(BLangXMLNS.BLangPackageXMLNS xmlnsNode) {
        xmlnsNode.namespaceURI = rewriteExpr(xmlnsNode.namespaceURI);
        result = xmlnsNode;
    }

    @Override
    public void visit(BLangXMLSequenceLiteral xmlSequenceLiteral) {
        xmlSequenceLiteral.xmlItems.forEach(this::rewriteExpr);
        result = xmlSequenceLiteral;
    }

    @Override
    public void visit(BLangMarkdownDocumentationLine bLangMarkdownDocumentationLine) {
        /* Ignore */
    }

    @Override
    public void visit(BLangMarkdownParameterDocumentation bLangDocumentationParameter) {
        /* Ignore */
    }

    @Override
    public void visit(BLangMarkdownReturnParameterDocumentation bLangMarkdownReturnParameterDocumentation) {
        /* Ignore */
    }

    @Override
    public void visit(BLangMarkdownDocumentation bLangMarkdownDocumentation) {
        /* Ignore */
    }

    // Rewrite methods
    @SuppressWarnings("unchecked")
    private <E extends BLangNode> E rewrite(E node, SymbolEnv env) {
        if (node == null) {
            return null;
        }

        SymbolEnv previousEnv = this.env;
        this.env = env;

        node.accept(this);
        BLangNode resultNode = this.result;
        this.result = null;

        this.env = previousEnv;
        return (E) resultNode;
    }

    @SuppressWarnings("unchecked")
    private <E extends BLangExpression> E rewriteExpr(E node, SymbolEnv env) {
        SymbolEnv previousEnv = this.env;
        this.env = env;
        BLangExpression expr = node;
        if (node.impConversionExpr != null) {
            expr = node.impConversionExpr;
            node.impConversionExpr = null;
        }

        expr.accept(this);
        BLangNode resultNode = this.result;
        this.result = null;
        this.env = previousEnv;
        return (E) resultNode;
    }

    @SuppressWarnings("unchecked")
    private <E extends BLangExpression> E rewriteExpr(E node) {
        if (node == null) {
            return null;
        }

        BLangExpression expr = node;
        if (node.impConversionExpr != null) {
            expr = node.impConversionExpr;
            node.impConversionExpr = null;
        }

        expr.accept(this);
        BLangNode resultNode = this.result;
        this.result = null;
        return (E) resultNode;
    }

    private <E extends BLangStatement> void rewriteStmts(List<E> nodeList, SymbolEnv env) {
        for (int i = 0; i < nodeList.size(); i++) {
            nodeList.set(i, rewrite(nodeList.get(i), env));
        }
    }

    private <E extends BLangExpression> void rewriteExprs(List<E> nodeList) {
        for (int i = 0; i < nodeList.size(); i++) {
            nodeList.set(i, rewriteExpr(nodeList.get(i)));
        }
    }
}

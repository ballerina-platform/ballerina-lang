/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.expressions.RecordLiteralNode;
import org.ballerinalang.util.diagnostic.DiagnosticErrorCode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BField;
import org.wso2.ballerinalang.compiler.semantics.model.types.BMapType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BObjectType;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangClassDefinition;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangExternalFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangMarkdownDocumentation;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangResourceFunction;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExtendedXMLNavigationAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIgnoreExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownDocumentationLine;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMarkdownReturnParameterDocumentation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNumericLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangObjectConstructorExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordVarRef;
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
import org.wso2.ballerinalang.compiler.tree.expressions.BLangWorkerAsyncSendExpr;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangXMLNSStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.FieldKind;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;

import static io.ballerina.runtime.api.constants.RuntimeConstants.UNDERSCORE;
import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;

/**
 * ClassClosure Desugar for object ctor closure related scenarios.
 *
 * @since 2.0.0
 */
public class ClassClosureDesugar extends BLangNodeVisitor {
    // TODO: use the new visitors

    private static final CompilerContext.Key<ClassClosureDesugar> CLASS_CLOSURE_DESUGAR_KEY =
            new CompilerContext.Key<>();

    private static final String BLOCK_MAP_SYM_NAME = "$map$block$oce$" + UNDERSCORE;
    private static final String FUNCTION_MAP_SYM_NAME = "$map$func$oce$" + UNDERSCORE;
    private static final String OBJECT_CTOR_MAP_SYM_NAME = "$map$objectCtor" + UNDERSCORE;
    private static final String PARAMETER_MAP_NAME = "$paramMap$oce$" + UNDERSCORE;
    private static final String OBJECT_CTOR_BLOCK_MAP_SYM_NAME = "$map$objectCtor" + UNDERSCORE + "block";
    private static final String OBJECT_CTOR_FUNCTION_MAP_SYM_NAME = "$map$objectCtor" + UNDERSCORE + "function";
    private static final BVarSymbol CLOSURE_MAP_NOT_FOUND;

    private int blockClosureMapCount = 1;
    private BLangClassDefinition classDef;
    private BLangNode result;
    private SymbolEnv env;

    private final SymbolTable symTable;
    private final Desugar desugar;
    private final Names names;
    private final BLangDiagnosticLog dlog;

    static {
        CLOSURE_MAP_NOT_FOUND = new BVarSymbol(0, new Name("$not$found"), null, null, null, null, VIRTUAL);
    }

    public static ClassClosureDesugar getInstance(CompilerContext context) {
        ClassClosureDesugar desugar = context.get(CLASS_CLOSURE_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new ClassClosureDesugar(context);
        }
        return desugar;
    }

    private ClassClosureDesugar(CompilerContext context) {
        context.put(CLASS_CLOSURE_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.desugar = Desugar.getInstance(context);
        this.names = Names.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        CLOSURE_MAP_NOT_FOUND.pos = this.symTable.builtinPos;
    }

    private void reset() {
        this.classDef = null;
        this.env = null;
    }

    @Override
    public void visit(BLangPackage pkgNode) {
        result = pkgNode;
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
        BLangIdentifier identifierNode = ASTBuilderUtil.createIdentifier(function.pos, classMapSymbol.name.value);
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

    private void addMapSymbolAsAField(BLangClassDefinition classDef, BVarSymbol mapSymbol) {
        BLangSimpleVariable mapVar = ASTBuilderUtil.createVariable(symTable.builtinPos, mapSymbol.name.value,
                mapSymbol.type, null, mapSymbol);

        BObjectType object = (BObjectType) classDef.getBType();
        object.fields.put(mapSymbol.name.value, new BField(mapSymbol.name, classDef.pos, mapSymbol));
        classDef.fields.add(0, mapVar);
    }

    @Override
    public void visit(BLangResourceFunction resourceFunction) {
        visit((BLangFunction) resourceFunction);
    }

    @Override
    public void visit(BLangFunction funcNode) {
        SymbolEnv funcEnv = SymbolEnv.createFunctionEnv(funcNode, funcNode.originalFuncSymbol.scope, env);

        funcNode.body = rewrite(funcNode.body, funcEnv);
        result = funcNode;
    }

    @Override
    public void visit(BLangBlockFunctionBody body) {
        SymbolEnv blockEnv = SymbolEnv.createFuncBodyEnv(body, env);
        rewriteStmts(body.stmts, blockEnv);


        if (body.mapSymbol != null) {
            body.stmts.add(0, getClosureMap(body.mapSymbol, blockEnv));

            // TODO: add body map reassignment statement
        }

        result = body;
    }

    @Override
    public void visit(BLangExternalFunctionBody body) {
        result = body;
    }

    @Override
    public void visit(BLangBlockStmt blockNode) {
        SymbolEnv blockEnv = SymbolEnv.createBlockEnv(blockNode, env);
        rewriteStmts(blockNode.stmts, blockEnv);

        // Add block map to the 0th position if a block map symbol is there.
        if (blockNode.mapSymbol != null) {
            // Add the closure map var as the first statement in the sequence statement.
            blockNode.stmts.add(0, getClosureMap(blockNode.mapSymbol, blockEnv));

            // TODO: add class map replacement statement
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
        if (varDefNode.var.expr != null) {
            BLangAssignment stmt = createAssignmentToClosureMap(varDefNode);
            result = rewrite(stmt, env);
        } else {
            // Note: Although it's illegal to use a closure variable without initializing it in it's declared scope,
            // when we access (initialize) a variable from outer scope, since we desugar transaction block into a
            // lambda invocation, we need to create the `mapSymbol` in the outer node.
//            createMapSymbolIfAbsent(env.node, blockClosureMapCount);
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

    @Override
    public void visit(BLangReturn returnNode) {
        if (returnNode.expr != null) {
            returnNode.expr = rewriteExpr(returnNode.expr);
        }
        result = returnNode;
    }

    @Override
    public void visit(BLangInvocation.BLangAttachedFunctionInvocation iExpr) {
        iExpr.expr = rewriteExpr(iExpr.expr);
        if (!iExpr.requiredArgs.isEmpty()) {
            iExpr.requiredArgs.set(0, iExpr.expr);
        }
        rewriteExprs(iExpr.requiredArgs);
        rewriteExprs(iExpr.restArgs);
        result = iExpr;
    }

    @Override
    public void visit(BLangImportPackage importPkgNode) {
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
    public void visit(BLangClassDefinition classDefinition) {
        if (!classDefinition.isObjectContructorDecl) {
            result = classDefinition;
            return;
        }
        var preClass = this.classDef;
        var prevEnv = this.env;

        desugar(classDefinition);
        result = classDefinition;

        this.classDef = preClass;
        this.env = prevEnv;
    }

    @Override
    public void visit(BLangObjectTypeNode objectTypeNode) {
        result = objectTypeNode;
    }

    @Override
    public void visit(BLangRecordTypeNode recordTypeNode) {
        result = recordTypeNode;
    }

    @Override
    public void visit(BLangSimpleVariable varNode) {
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

    @Override
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
        BVarSymbol symbol = (BVarSymbol) varRefExpr.symbol;
        if (symbol.closure && classDef.oceEnvData.isDirty) {
            dlog.error(varRefExpr.pos, DiagnosticErrorCode.UNSUPPORTED_MULTILEVEL_CLOSURES,
                    varRefExpr);
            varRefExpr.setBType(symTable.semanticError);
            result = null;
            return;
        }
        BVarSymbol mapSymbol = getVarMapSymbol(symbol);

        if (!symbol.closure || mapSymbol == null) {
            result = varRefExpr;
            return;
        }
        BLangSimpleVarRef.BLangLocalVarRef localVarRef = new BLangSimpleVarRef.BLangLocalVarRef(symbol);
        localVarRef.closureDesugared = true;
        localVarRef.desugared = true;
        localVarRef.setBType(varRefExpr.getBType());
        updateClosureVars(localVarRef, mapSymbol);
        classDef.oceEnvData.desugaredClosureVars.add(varRefExpr);
    }

    private BVarSymbol getVarMapSymbol(BVarSymbol symbol) {
        BVarSymbol mapSymbol = null;
        if (classDef.oceEnvData.closureFuncSymbols.contains(symbol)) {
            mapSymbol = classDef.oceEnvData.mapFunctionMapSymbol;
        } else if (classDef.oceEnvData.closureBlockSymbols.contains(symbol)) {
            mapSymbol = classDef.oceEnvData.mapBlockMapSymbol;
        }
        return mapSymbol;
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

    @Override
    public void visit(BLangInvocation iExpr) {
        iExpr.expr = rewriteExpr(iExpr.expr);
        rewriteExprs(iExpr.requiredArgs);
        rewriteExprs(iExpr.restArgs);
        result = iExpr;
    }

    @Override
    public void visit(BLangErrorConstructorExpr errorConstructorExpr) {
        rewriteExprs(errorConstructorExpr.positionalArgs);
        rewriteExpr(errorConstructorExpr.errorDetail);
        result = errorConstructorExpr;
    }

    @Override
    public void visit(BLangTypeInit typeInitExpr) {
        typeInitExpr.initInvocation = rewriteExpr(typeInitExpr.initInvocation);
        BLangNode parent = typeInitExpr.initInvocation.parent;
        if (parent != null && parent.getKind() == NodeKind.OBJECT_CTOR_EXPRESSION) {
            BLangObjectConstructorExpression oce = (BLangObjectConstructorExpression) parent;
            BLangClassDefinition dirtyOceClass = oce.classNode;
            if (dirtyOceClass.hasClosureVars && classDef.oceEnvData.closureDesugaringInProgress) {
                // below lines are probably not needed after proper handling
                dirtyOceClass.hasClosureVars = false;
                OCEDynamicEnvironmentData dirtyOceEnvData = dirtyOceClass.oceEnvData;
                dirtyOceEnvData.isDirty = true;
                OCEDynamicEnvironmentData currentClassOceData = classDef.oceEnvData;
                currentClassOceData.closureBlockSymbols.removeAll(dirtyOceEnvData.closureBlockSymbols);
                currentClassOceData.closureFuncSymbols.removeAll(dirtyOceEnvData.closureFuncSymbols);
                dirtyOceEnvData.parents.remove(classDef);
                for (BLangSimpleVarRef simpleVarRef : dirtyOceEnvData.desugaredClosureVars) {
                    dlog.error(simpleVarRef.pos, DiagnosticErrorCode.UNSUPPORTED_MULTILEVEL_CLOSURES, simpleVarRef);
                }
                visit(dirtyOceClass);
            }
        }
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
        result = conversionExpr;
    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        result = bLangLambdaFunction;
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
    public void visit(BLangWorkerAsyncSendExpr asyncSendExpr) {
        asyncSendExpr.expr = rewriteExpr(asyncSendExpr.expr);
        result = asyncSendExpr;
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
        // Chek
        if (!localVarRef.symbol.closure || localVarRef.closureDesugared) {
            result = localVarRef;
            return;
        }

        BVarSymbol mapSym = null;
        //createMapSymbolIfAbsent(symbolEnv.node, symbolEnv.envCount);
        updateClosureVars(localVarRef, mapSym);
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



    /**
     * Create the map symbol required for the function node and block statements.
     *
     * @param mapName   name of the map to be created
     * @param symbolEnv symbol environment
     * @return map symbol created
     */
    private BVarSymbol createMapSymbol(String mapName, SymbolEnv symbolEnv) {
        return new BVarSymbol(0, Names.fromString(mapName), symbolEnv.scope.owner.pkgID,
                              symTable.mapAllType, symbolEnv.scope.owner, symTable.builtinPos, VIRTUAL);
    }

    /**
     * Update the closure maps with the relevant map access expression.
     *
     * @param varRefExpr closure variable reference to be updated
     * @param mapSymbol  map symbol to be used
     */
    private void updateClosureVars(BLangSimpleVarRef varRefExpr, BVarSymbol mapSymbol) {
        BVarSymbol selfSymbol = classDef.generatedInitFunction.receiver.symbol;
        BLangSimpleVarRef.BLangLocalVarRef localSelfVarRef = new BLangSimpleVarRef.BLangLocalVarRef(selfSymbol);
        localSelfVarRef.setBType(classDef.getBType());
        localSelfVarRef.closureDesugared = true;
        BLangIndexBasedAccess.BLangStructFieldAccessExpr accessExprForClassField =
                new BLangIndexBasedAccess.BLangStructFieldAccessExpr(varRefExpr.pos, localSelfVarRef,
                        ASTBuilderUtil.createLiteral(varRefExpr.pos, symTable.stringType,
                                mapSymbol.name), mapSymbol, false, true);

        accessExprForClassField.setBType(mapSymbol.type);

        //  self[$map$objectCtor$_<num>][i]
        BLangIndexBasedAccess.BLangMapAccessExpr closureMapAccessForField =
                new BLangIndexBasedAccess.BLangMapAccessExpr(varRefExpr.pos, accessExprForClassField,
                        ASTBuilderUtil.createLiteral(varRefExpr.pos, symTable.stringType, varRefExpr.symbol.name));

        closureMapAccessForField.setBType(varRefExpr.getBType());

        BLangTypeConversionExpr castExpr = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        castExpr.expr = closureMapAccessForField;
        castExpr.setBType(varRefExpr.getBType());
        castExpr.targetType = varRefExpr.getBType();

        result = desugar.rewriteExpr(castExpr);
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
    public void visit(BLangExtendedXMLNavigationAccess extendedXMLNavigationAccess) {
        extendedXMLNavigationAccess.stepExpr = rewriteExpr(extendedXMLNavigationAccess.stepExpr);
        result = extendedXMLNavigationAccess;
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
        rewriteExprs(fpInvocation.requiredArgs);
        rewriteExprs(fpInvocation.restArgs);
        result = fpInvocation;
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
        if (bLangStatementExpression.stmt.getKind() == NodeKind.BLOCK) {
            BLangBlockStmt bLangBlockStmt = (BLangBlockStmt) bLangStatementExpression.stmt;
            for (int i = 0; i < bLangBlockStmt.stmts.size(); i++) {
                BLangStatement stmt = bLangBlockStmt.stmts.remove(i);
                bLangBlockStmt.stmts.add(i, rewrite(stmt, env));
            }
        } else {
            bLangStatementExpression.stmt = rewrite(bLangStatementExpression.stmt, env);
        }
        bLangStatementExpression.expr = rewriteExpr(bLangStatementExpression.expr);
        result = bLangStatementExpression;
    }

    @Override
    public void visit(BLangInvocation.BLangActionInvocation aIExpr) {
        aIExpr.expr = rewriteExpr(aIExpr.expr);
        rewriteExprs(aIExpr.requiredArgs);
        rewriteExprs(aIExpr.restArgs);
        result = aIExpr;
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

    public void desugar(BLangClassDefinition classDef) {
        this.classDef = classDef;
        if (!classDef.hasClosureVars) {
            if (classDef.oceEnvData.isDirty) {
                updateFields(classDef);
            }
            return;
        }
        createMapSymbolsIfAbsent(classDef);
        addFunctionLevelClosureMapToClassDefinition(classDef);
        addBlockLevelClosureMapToClassDefinition(classDef);
        updateFields(classDef);

        reset();
    }

    private void updateFields(BLangClassDefinition classDef) {

        classDef.oceEnvData.closureDesugaringInProgress = true;
        for (BLangSimpleVariable field : classDef.fields) {
            if (!field.symbol.isDefaultable) {
                continue;
            }
            if (field.expr.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                BLangSimpleVarRef varRef = (BLangSimpleVarRef) field.expr;
                if (classDef.oceEnvData.isDirty && varRef.symbol.closure) {
                    dlog.error(varRef.pos, DiagnosticErrorCode.UNSUPPORTED_MULTILEVEL_CLOSURES, varRef);
                }
                continue;
            }
            field.expr = rewrite(field.expr, this.env);
        }
        classDef.oceEnvData.closureDesugaringInProgress = false;
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

    private void addBlockLevelClosureMapToClassDefinition(BLangClassDefinition classDef) {
        OCEDynamicEnvironmentData oceData = classDef.oceEnvData;
        if (oceData.blockMapUpdatedInInitMethod || oceData.mapBlockMapSymbol == null) {
            return;
        }
        SymbolEnv env = oceData.capturedClosureEnv;
        this.env = env;
        BLangNode node = getNextPossibleNode(env.enclEnv);
        if (node == null) {
            return;
        }
        if (oceData.closureBlockSymbols.isEmpty()) {
            return;
        }
        if (node.getKind() == NodeKind.FUNCTION) {
            BLangFunction function = (BLangFunction) node;
            node = function.body;
        }
        BVarSymbol blockMap = createMapSymbolIfAbsent(node, oceData.closureBlockSymbols.size());
        BVarSymbol mapSymbol = oceData.mapBlockMapSymbol;

        addMapSymbolAsAField(classDef, mapSymbol);
        createClosureMapUpdateExpression(classDef, blockMap, mapSymbol);

        oceData.blockMapUpdatedInInitMethod = true;
    }

    private void addFunctionLevelClosureMapToClassDefinition(BLangClassDefinition classDef) {
        OCEDynamicEnvironmentData oceData = classDef.oceEnvData;
        if (oceData.functionMapUpdatedInInitMethod || oceData.mapFunctionMapSymbol == null) {
            return;
        }
        SymbolEnv env = oceData.capturedClosureEnv;
        this.env = env;
        if (env.enclEnv.node == null) {
            return;
        }
        if (oceData.closureFuncSymbols.isEmpty()) {
            return;
        }
        BLangFunction function = (BLangFunction) oceData.capturedClosureEnv.enclInvokable;
        BVarSymbol functionMap = createMapSymbolIfAbsent(function, oceData.closureFuncSymbols.size());
        BVarSymbol mapSymbol = oceData.mapFunctionMapSymbol;

        addMapSymbolAsAField(classDef, mapSymbol);
        createClosureMapUpdateExpression(classDef, functionMap, mapSymbol);

        oceData.functionMapUpdatedInInitMethod = true;
    }


    private void createMapSymbolsIfAbsent(BLangClassDefinition classDef) {
        OCEDynamicEnvironmentData oceData = classDef.oceEnvData;
        if (!oceData.closureBlockSymbols.isEmpty() && classDef.oceEnvData.mapBlockMapSymbol == null) {
            classDef.oceEnvData.mapBlockMapSymbol =
                    createMapSymbol(OBJECT_CTOR_BLOCK_MAP_SYM_NAME, classDef.oceEnvData.capturedClosureEnv);
        }
        if (!oceData.closureFuncSymbols.isEmpty() && classDef.oceEnvData.mapFunctionMapSymbol == null) {
            classDef.oceEnvData.mapFunctionMapSymbol =
                    createMapSymbol(OBJECT_CTOR_FUNCTION_MAP_SYM_NAME, classDef.oceEnvData.capturedClosureEnv);
        }
    }
}

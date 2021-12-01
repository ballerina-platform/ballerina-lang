/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.ballerinalang.compiler.desugar;

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.ballerinalang.model.tree.BlockNode;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BIntersectionType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangBlockFunctionBody;
import org.wso2.ballerinalang.compiler.tree.BLangErrorVariable;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeTransformer;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangRecordVariable;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTupleVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckPanickedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangCheckedExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangDynamicArgExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsLikeExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangNamedArgsExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangServiceConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeInit;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangXMLTextLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangContinue;
import org.wso2.ballerinalang.compiler.tree.statements.BLangErrorVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangRecordVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleVariableDef;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.tree.types.BLangUserDefinedType;
import org.wso2.ballerinalang.compiler.tree.types.BLangValueType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.ballerinalang.model.symbols.SymbolOrigin.VIRTUAL;

/**
 * Some utils methods for building AST nodes at desugar phase.
 *
 * @since 0.965.0
 */
public class ASTBuilderUtil {

    /**
     * Prepend generated code to given body.
     *
     * @param generatedCode generated code.
     * @param target        prepend target
     */
    static void prependStatements(BLangBlockStmt generatedCode, BLangBlockStmt target) {
        int index = 0;
        for (BLangStatement stmt : generatedCode.stmts) {
            target.stmts.add(index++, stmt);
        }
    }

    /**
     * Append generated code to given body.
     *
     * @param generatedCode generated code.
     * @param target        prepend target
     */
    static void appendStatements(BlockNode generatedCode, BlockNode target) {
        int index = 0;
        List<StatementNode> generatedCodeStmts = (List<StatementNode>) generatedCode.getStatements();
        List<StatementNode> targetStmts = (List<StatementNode>) target.getStatements();

        if (targetStmts.get(targetStmts.size() - 1).getKind() == NodeKind.RETURN) {
            index = targetStmts.size() - 1;
        }

        targetStmts.addAll(index, generatedCodeStmts);
    }

    static void appendStatement(BLangStatement stmt, BLangBlockStmt target) {
        int index = 0;
        if (target.stmts.size() > 0 && target.stmts.get(target.stmts.size() - 1).getKind() == NodeKind.RETURN) {
            index = target.stmts.size() - 1;
        }
        target.stmts.add(index, stmt);
    }

    static void defineVariable(BLangSimpleVariable variable, BSymbol targetSymbol, Names names) {
        variable.symbol = new BVarSymbol(0, names.fromIdNode(variable.name),
                                         names.originalNameFromIdNode(variable.name),
                                         targetSymbol.pkgID, variable.getBType(), targetSymbol, variable.pos, VIRTUAL);
        targetSymbol.scope.define(variable.symbol.name, variable.symbol);
    }

    private static boolean isValueType(BType type) {
        return type.tag < TypeTags.JSON;
    }

    static BLangExpression wrapToConversionExpr(BType sourceType, BLangExpression exprToWrap,
                                                SymbolTable symTable, Types types) {
        if (types.isSameType(sourceType, exprToWrap.getBType()) || !isValueType(exprToWrap.getBType())) {
            // No conversion needed.
            return exprToWrap;
        }
        BLangTypeConversionExpr castExpr = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        castExpr.expr = exprToWrap;
        castExpr.setBType(symTable.anyType);
        castExpr.targetType = castExpr.getBType();
        return castExpr;
    }

    static BLangFunction createFunction(Location pos, String name) {
        final BLangFunction bLangFunction = (BLangFunction) TreeBuilder.createFunctionNode();
        final IdentifierNode funcName = createIdentifier(pos, name);
        bLangFunction.setName(funcName);
        bLangFunction.flagSet = EnumSet.of(Flag.LAMBDA);
        bLangFunction.pos = pos;
        // Create body of the function
        bLangFunction.body = createBlockFunctionBody(pos);
        return bLangFunction;
    }

    static BLangType createTypeNode(BType type) {
        BLangType bLangType = new BLangType() {
            @Override
            public void accept(BLangNodeVisitor visitor) {
            }

            @Override
            public <T> void accept(BLangNodeAnalyzer<T> analyzer, T props) {
            }

            @Override
            public <T, R> R apply(BLangNodeTransformer<T, R> modifier, T props) {
                return null;
            }

            @Override
            public NodeKind getKind() {
                return null;
            }
        };
        bLangType.setBType(type);
        return bLangType;
    }

    static BLangUserDefinedType createUserDefineTypeNode(String typeName, BType type, Location pos) {
        BLangUserDefinedType userDefinedType = (BLangUserDefinedType) TreeBuilder.createUserDefinedTypeNode();
        userDefinedType.typeName = (BLangIdentifier) createIdentifier(typeName);
        userDefinedType.setBType(type);
        userDefinedType.pos = pos;
        userDefinedType.pkgAlias = (BLangIdentifier) createIdentifier("");
        return userDefinedType;
    }

    static BLangIf createIfStmt(Location pos, BlockNode target) {
        final BLangIf ifNode = (BLangIf) TreeBuilder.createIfElseStatementNode();
        ifNode.pos = pos;
        target.addStatement(ifNode);
        return ifNode;
    }

    static BLangIf createIfElseStmt(Location pos,
                                    BLangExpression conditionExpr,
                                    BLangBlockStmt thenBody,
                                    BLangStatement elseStmt) {
        final BLangIf ifNode = (BLangIf) TreeBuilder.createIfElseStatementNode();
        ifNode.pos = pos;
        ifNode.expr = conditionExpr;
        ifNode.body = thenBody;
        ifNode.elseStmt = elseStmt;
        return ifNode;
    }

    static BLangTypeTestExpr createTypeTestExpr(Location pos, BLangExpression expr, BLangType type) {
        final BLangTypeTestExpr typeTestExpr = (BLangTypeTestExpr) TreeBuilder.createTypeTestExpressionNode();
        typeTestExpr.pos = pos;
        typeTestExpr.expr = expr;
        typeTestExpr.typeNode = type;
        return typeTestExpr;
    }

    static BLangForeach createForeach(Location pos,
                                      BLangBlockStmt target,
                                      BLangSimpleVarRef collectionVarRef) {
        final BLangForeach foreach = (BLangForeach) TreeBuilder.createForeachNode();
        foreach.pos = pos;
        target.addStatement(foreach);
        foreach.body = ASTBuilderUtil.createBlockStmt(pos);
        foreach.collection = collectionVarRef;
        return foreach;
    }

    static BLangSimpleVariableDef createVariableDefStmt(Location pos, BlockNode target) {
        final BLangSimpleVariableDef variableDef = createVariableDef(pos);
        target.addStatement(variableDef);
        return variableDef;
    }

    static BLangSimpleVariableDef createVariableDef(Location pos) {
        final BLangSimpleVariableDef variableDef =
                (BLangSimpleVariableDef) TreeBuilder.createSimpleVariableDefinitionNode();
        variableDef.pos = pos;
        return variableDef;
    }

    static BLangAssignment createAssignmentStmt(Location pos, BlockNode target) {
        final BLangAssignment assignment = (BLangAssignment) TreeBuilder.createAssignmentNode();
        assignment.pos = pos;
        target.addStatement(assignment);
        return assignment;
    }

    static BLangAssignment createAssignmentStmt(Location pos, BLangExpression varRef,
                                                BLangExpression rhsExpr) {
        final BLangAssignment assignment = (BLangAssignment) TreeBuilder.createAssignmentNode();
        assignment.pos = pos;
        assignment.varRef = varRef;
        assignment.expr = rhsExpr;
        return assignment;
    }

    static BLangExpressionStmt createExpressionStmt(Location pos, BlockNode target) {
        final BLangExpressionStmt exprStmt = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
        exprStmt.pos = pos;
        target.addStatement(exprStmt);
        return exprStmt;
    }

    static BLangReturn createReturnStmt(Location pos, BlockNode target) {
        final BLangReturn returnStmt = (BLangReturn) TreeBuilder.createReturnNode();
        returnStmt.pos = pos;
        target.addStatement(returnStmt);
        return returnStmt;
    }

    public static BLangReturn createNilReturnStmt(Location pos, BType nilType) {
        final BLangReturn returnStmt = (BLangReturn) TreeBuilder.createReturnNode();
        returnStmt.pos = pos;
        returnStmt.expr = createLiteral(pos, nilType, Names.NIL_VALUE);
        return returnStmt;
    }

    public static BLangReturn createReturnStmt(Location pos, BType returnType, Object value) {
        final BLangReturn returnStmt = (BLangReturn) TreeBuilder.createReturnNode();
        returnStmt.pos = pos;
        returnStmt.expr = createLiteral(pos, returnType, value);
        return returnStmt;
    }

    public static BLangReturn createReturnStmt(Location pos, BLangExpression expr) {
        final BLangReturn returnStmt = (BLangReturn) TreeBuilder.createReturnNode();
        returnStmt.pos = pos;
        returnStmt.expr = expr;
        return returnStmt;
    }

    static void createContinueStmt(Location pos, BLangBlockStmt target) {
        final BLangContinue nextStmt = (BLangContinue) TreeBuilder.createContinueNode();
        nextStmt.pos = pos;
        target.addStatement(nextStmt);
    }

    static BLangBlockFunctionBody createBlockFunctionBody(Location pos) {
        final BLangBlockFunctionBody blockNode = (BLangBlockFunctionBody) TreeBuilder.createBlockFunctionBodyNode();
        blockNode.pos = pos;
        return blockNode;
    }

    static BLangBlockFunctionBody createBlockFunctionBody(Location pos, List<BLangStatement> stmts) {
        final BLangBlockFunctionBody blockNode = (BLangBlockFunctionBody) TreeBuilder.createBlockFunctionBodyNode();
        blockNode.pos = pos;
        blockNode.stmts = stmts;
        return blockNode;
    }

    static BLangBlockStmt createBlockStmt(Location pos) {
        final BLangBlockStmt blockNode = (BLangBlockStmt) TreeBuilder.createBlockNode();
        blockNode.pos = pos;
        return blockNode;
    }

    static BLangBlockStmt createBlockStmt(Location pos, List<BLangStatement> stmts) {
        final BLangBlockStmt blockNode = (BLangBlockStmt) TreeBuilder.createBlockNode();
        blockNode.pos = pos;
        blockNode.stmts = stmts;
        return blockNode;
    }

    static BLangMatch.BLangMatchTypedBindingPatternClause createMatchStatementPattern(Location pos,
                                                                                      BLangSimpleVariable variable,
                                                                                      BLangBlockStmt body) {
        BLangMatch.BLangMatchTypedBindingPatternClause patternClause =
                (BLangMatch.BLangMatchTypedBindingPatternClause)
                        TreeBuilder.createMatchStatementSimpleBindingPattern();
        patternClause.pos = pos;
        patternClause.variable = variable;
        patternClause.body = body;
        return patternClause;

    }

    static BLangMatch createMatchStatement(Location pos,
                                           BLangExpression expr,
                                           List<BLangMatch.BLangMatchTypedBindingPatternClause> patternClauses) {
        BLangMatch matchStmt = (BLangMatch) TreeBuilder.createMatchStatement();
        matchStmt.pos = pos;
        matchStmt.expr = expr;
        matchStmt.patternClauses.addAll(patternClauses);
        return matchStmt;
    }

    static BLangUnaryExpr createUnaryExpr(Location pos) {
        return createUnaryExpr(pos, null, null, null, null);
    }

    static BLangUnaryExpr createUnaryExpr(Location pos,
                                          BLangExpression expr,
                                          BType type,
                                          OperatorKind kind,
                                          BOperatorSymbol symbol) {
        final BLangUnaryExpr unaryExpr = (BLangUnaryExpr) TreeBuilder.createUnaryExpressionNode();
        unaryExpr.pos = pos;
        unaryExpr.expr = expr;
        unaryExpr.setBType(type);
        unaryExpr.operator = kind;
        unaryExpr.opSymbol = symbol;
        return unaryExpr;
    }

    static BLangTypedescExpr createTypedescExpr(Location pos, BType type, BType resolvedType) {
        final BLangTypedescExpr typeofExpr = (BLangTypedescExpr) TreeBuilder.createTypeAccessNode();
        typeofExpr.pos = pos;
        typeofExpr.setBType(type);
        typeofExpr.resolvedType = resolvedType;
        return typeofExpr;
    }

    static BLangIndexBasedAccess createIndexBasesAccessExpr(Location location, BType type,
                                                            BVarSymbol varSymbol,
                                                            BLangExpression indexExpr) {
        final BLangIndexBasedAccess arrayAccess = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        arrayAccess.pos = location;
        arrayAccess.expr = createVariableRef(location, varSymbol);
        arrayAccess.indexExpr = indexExpr;
        arrayAccess.setBType(type);
        return arrayAccess;
    }

    static BLangExpression generateConversionExpr(BLangExpression varRef, BType target, SymbolResolver symResolver) {
        if (varRef.getBType().tag == target.tag || varRef.getBType().tag > TypeTags.BOOLEAN) {
            return varRef;
        }
        // Box value using cast expression.
        final BLangTypeConversionExpr conversion = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        conversion.pos = varRef.pos;
        conversion.expr = varRef;
        conversion.setBType(target);
        conversion.targetType = target;
        return conversion;
    }

    static List<BLangExpression> generateArgExprs(Location pos, List<BLangSimpleVariable> args,
                                                  List<BVarSymbol> formalParams, SymbolResolver symResolver) {
        List<BLangExpression> argsExpr = new ArrayList<>();
        final List<BLangSimpleVarRef> variableRefList = createVariableRefList(pos, args);
        for (int i = 0; i < variableRefList.size(); i++) {
            BLangSimpleVarRef varRef = variableRefList.get(i);
            BType target = formalParams.get(i).type;
            BType source = varRef.symbol.type;
            if (source != target) {
                argsExpr.add(generateConversionExpr(varRef, target, symResolver));
                continue;
            }
            argsExpr.add(varRef);
        }
        return argsExpr;
    }

    public static BLangInvocation createInvocationExpr(Location location,
                                                       BInvokableSymbol invokableSymbol,
                                                       List<BLangSimpleVariable> requiredArgs,
                                                       SymbolResolver symResolver) {
        return createInvocationExpr(location, invokableSymbol, requiredArgs, new ArrayList<>(), symResolver);
    }

    static BLangInvocation createInvocationExpr(Location pos, BInvokableSymbol invokableSymbol,
                                                List<BLangSimpleVariable> requiredArgs,
                                                List<BLangSimpleVariable> restArgs, SymbolResolver symResolver) {
        final BLangInvocation invokeLambda = (BLangInvocation) TreeBuilder.createInvocationNode();
        invokeLambda.pos = pos;
        invokeLambda.requiredArgs.addAll(generateArgExprs(pos, requiredArgs, invokableSymbol.params, symResolver));
        invokeLambda.restArgs
                .addAll(generateArgExprs(pos, restArgs, Lists.of(invokableSymbol.restParam), symResolver));

        invokeLambda.symbol = invokableSymbol;
        invokeLambda.setBType(((BInvokableType) invokableSymbol.type).retType);
        return invokeLambda;
    }

    public static BLangInvocation createLangLibInvocationNode(String name, ArrayList<BLangExpression> argExprs,
                                                              BLangExpression onExpr, Location location) {
        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.name = createIdentifier(location, name);
        invocationNode.expr = onExpr;
        invocationNode.pos = location;
        invocationNode.argExprs = argExprs;
        invocationNode.langLibInvocation = true;

        return invocationNode;
    }

    static BLangInvocation createInvocationExprForMethod(Location pos, BInvokableSymbol invokableSymbol,
                                                List<BLangExpression> requiredArgs, SymbolResolver symResolver) {
        return createInvocationExprMethod(pos, invokableSymbol, requiredArgs, new ArrayList<>(), symResolver);
    }

    static BLangInvocation createInvocationExprMethod(Location pos, BInvokableSymbol invokableSymbol,
                                                      List<BLangExpression> requiredArgs,
                                                      List<BLangSimpleVariable> restArgs, SymbolResolver symResolver) {
        final BLangInvocation invokeLambda = (BLangInvocation) TreeBuilder.createInvocationNode();
        invokeLambda.pos = pos;
        invokeLambda.name = createIdentifier(pos, invokableSymbol.name.value);
        invokeLambda.requiredArgs.addAll(requiredArgs);
        invokeLambda.restArgs
                .addAll(generateArgExprs(pos, restArgs, Lists.of(invokableSymbol.restParam), symResolver));

        invokeLambda.symbol = invokableSymbol;
        invokeLambda.setBType(((BInvokableType) invokableSymbol.type).retType);
        return invokeLambda;
    }

    static List<BLangSimpleVarRef> createVariableRefList(Location pos, List<BLangSimpleVariable> args) {
        final List<BLangSimpleVarRef> varRefs = new ArrayList<>();
        args.forEach(variable -> varRefs.add(createVariableRef(pos, variable.symbol)));
        return varRefs;
    }

    static BLangSimpleVarRef createVariableRef(Location pos, BSymbol varSymbol) {
        final BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        varRef.pos = pos;
        varRef.variableName = createIdentifier(pos, varSymbol.name.value);
        varRef.symbol = varSymbol;
        varRef.setBType(varSymbol.type);
        return varRef;
    }

    public static BLangSimpleVariable createVariable(Location pos,
                                                     String name,
                                                     BType type,
                                                     BLangExpression expr,
                                                     BVarSymbol varSymbol) {
        final BLangSimpleVariable varNode = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        varNode.pos = pos;
        varNode.name = createIdentifier(pos, name);
        varNode.setBType(type);
        varNode.expr = expr;
        varNode.symbol = varSymbol;
        return varNode;
    }

    public static BLangSimpleVariable createVariable(Location pos, String name, BType type) {
        return createVariable(pos, name, type, null, null);
    }

    public static BLangSimpleVariableDef createVariableDef(Location pos, BLangSimpleVariable variable) {
        final BLangSimpleVariableDef variableDef =
                (BLangSimpleVariableDef) TreeBuilder.createSimpleVariableDefinitionNode();
        variableDef.pos = pos;
        variableDef.var = variable;
        return variableDef;
    }

    static BLangTupleVariableDef createTupleVariableDef(Location pos, BLangTupleVariable variable) {
        final BLangTupleVariableDef variableDef =
                (BLangTupleVariableDef) TreeBuilder.createTupleVariableDefinitionNode();
        variableDef.pos = pos;
        variableDef.var = variable;
        return variableDef;
    }

    static BLangRecordVariableDef createRecordVariableDef(Location pos, BLangRecordVariable variable) {
        final BLangRecordVariableDef variableDef =
                (BLangRecordVariableDef) TreeBuilder.createRecordVariableDefinitionNode();
        variableDef.pos = pos;
        variableDef.var = variable;
        return variableDef;
    }

    static BLangErrorVariableDef createErrorVariableDef(Location pos, BLangErrorVariable variable) {
        final BLangErrorVariableDef variableDef =
                (BLangErrorVariableDef) TreeBuilder.createErrorVariableDefinitionNode();
        variableDef.pos = pos;
        variableDef.errorVariable = variable;
        return variableDef;
    }

    static BLangCheckedExpr createCheckExpr(Location pos, BLangExpression expr, BType returnType) {
        final BLangCheckedExpr checkExpr = (BLangCheckedExpr) TreeBuilder.createCheckExpressionNode();
        checkExpr.pos = pos;
        checkExpr.expr = expr;
        checkExpr.setBType(returnType);
        checkExpr.equivalentErrorTypeList = new ArrayList<>();
        return checkExpr;
    }

    static BLangCheckPanickedExpr createCheckPanickedExpr(Location location, BLangExpression expr,
                                                          BType returnType) {
        final BLangCheckPanickedExpr checkExpr = (BLangCheckPanickedExpr) TreeBuilder.createCheckPanicExpressionNode();
        checkExpr.pos = location;
        checkExpr.expr = expr;
        checkExpr.setBType(returnType);
        checkExpr.equivalentErrorTypeList = new ArrayList<>();
        return checkExpr;
    }

    static BLangBinaryExpr createBinaryExpr(Location pos,
                                            BLangExpression lhsExpr,
                                            BLangExpression rhsExpr,
                                            BType type,
                                            OperatorKind opKind,
                                            BOperatorSymbol symbol) {
        final BLangBinaryExpr binaryExpr = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        binaryExpr.pos = pos;
        binaryExpr.lhsExpr = lhsExpr;
        binaryExpr.rhsExpr = rhsExpr;
        binaryExpr.setBType(type);
        binaryExpr.opKind = opKind;
        binaryExpr.opSymbol = symbol;
        return binaryExpr;
    }

    static BLangIsAssignableExpr createIsAssignableExpr(Location pos,
                                                        BLangExpression lhsExpr,
                                                        BType targetType,
                                                        BType type,
                                                        Names names,
                                                        Location opSymPos) {
        final BLangIsAssignableExpr assignableExpr = new BLangIsAssignableExpr();
        assignableExpr.pos = pos;
        assignableExpr.lhsExpr = lhsExpr;
        assignableExpr.targetType = targetType;
        assignableExpr.setBType(type);
        assignableExpr.opSymbol = new BOperatorSymbol(names.fromString(assignableExpr.opKind.value()),
                                                      null, targetType, null, opSymPos, VIRTUAL);
        return assignableExpr;
    }

    static BLangIsLikeExpr createIsLikeExpr(Location pos, BLangExpression expr, BLangType typeNode,
                                            BType retType) {
        BLangIsLikeExpr isLikeExpr = (BLangIsLikeExpr) TreeBuilder.createIsLikeExpressionNode();
        isLikeExpr.pos = pos;
        isLikeExpr.expr = expr;
        isLikeExpr.typeNode = typeNode;
        isLikeExpr.setBType(retType);
        return isLikeExpr;
    }

    static BLangLiteral createLiteral(Location pos, BType type, Object value) {
        final BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
        literal.pos = pos;
        literal.value = value;
        literal.setBType(type);
        return literal;
    }

    static BLangConstRef createBLangConstRef(Location pos, BType type, Object value) {
        final BLangConstRef constRef = (BLangConstRef) TreeBuilder.createConstLiteralNode();
        constRef.pos = pos;
        constRef.value = value;
        constRef.setBType(type);
        return constRef;
    }

    static BLangRecordLiteral createEmptyRecordLiteral(Location pos, BType type) {
        final BLangRecordLiteral recordLiteralNode = (BLangRecordLiteral) TreeBuilder.createRecordLiteralNode();
        recordLiteralNode.pos = pos;
        recordLiteralNode.setBType(type);
        return recordLiteralNode;
    }

    static BLangRecordLiteral.BLangRecordKeyValueField createBLangRecordKeyValue(BLangExpression key,
                                                                                 BLangExpression value) {
        final BLangRecordLiteral.BLangRecordKeyValueField recordKeyValue =
                (BLangRecordLiteral.BLangRecordKeyValueField) TreeBuilder.createRecordKeyValue();
        recordKeyValue.key = new BLangRecordLiteral.BLangRecordKey(key);
        recordKeyValue.valueExpr = value;
        return recordKeyValue;
    }

    static BLangListConstructorExpr.BLangArrayLiteral createEmptyArrayLiteral(Location location,
                                                                              BArrayType type) {
        final BLangListConstructorExpr.BLangArrayLiteral arrayLiteralNode =
                (BLangListConstructorExpr.BLangArrayLiteral) TreeBuilder.createArrayLiteralExpressionNode();
        arrayLiteralNode.pos = location;
        arrayLiteralNode.setBType(type);
        arrayLiteralNode.exprs = new ArrayList<>();
        return arrayLiteralNode;
    }

    static BLangListConstructorExpr createListConstructorExpr(Location pos, BType type) {
        if (type.tag == TypeTags.INTERSECTION) {
            type = ((BIntersectionType) type).effectiveType;
        }

        if (type.tag != TypeTags.ARRAY && type.tag != TypeTags.TUPLE) {
            throw new IllegalArgumentException("Expected a 'BArrayType' instance or a 'BTupleType' instance");
        }

        final BLangListConstructorExpr listConstructorExpr =
                (BLangListConstructorExpr) TreeBuilder.createListConstructorExpressionNode();
        listConstructorExpr.pos = pos;
        listConstructorExpr.setBType(type);
        listConstructorExpr.exprs = new ArrayList<>();
        listConstructorExpr.internal = true;
        return listConstructorExpr;
    }

    static BLangTypeInit createEmptyTypeInit(Location pos, BType type) {
        BLangTypeInit objectInitNode = (BLangTypeInit) TreeBuilder.createInitNode();
        objectInitNode.pos = pos;
        objectInitNode.setBType(type);
        objectInitNode.internal = true;

        BLangInvocation invocationNode = (BLangInvocation) TreeBuilder.createInvocationNode();
        invocationNode.symbol = ((BObjectTypeSymbol) type.tsymbol).generatedInitializerFunc.symbol;
        invocationNode.setBType(type);
        invocationNode.pos = pos;
        invocationNode.internal = true;

        BLangIdentifier pkgNameNode = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        BLangIdentifier nameNode = (BLangIdentifier)  TreeBuilder.createIdentifierNode();

        nameNode.setLiteral(false);
        nameNode.setValue(Names.GENERATED_INIT_SUFFIX.getValue());
        nameNode.pos = pos;
        invocationNode.name = nameNode;
        invocationNode.pkgAlias = pkgNameNode;

        objectInitNode.initInvocation = invocationNode;
        return objectInitNode;
    }

    public static BLangIdentifier createIdentifier(Location pos, String value) {
        final BLangIdentifier node = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        node.pos = pos;
        if (value != null) {
            node.setValue(value);
            node.setOriginalValue(value);
        }
        return node;
    }

    public static BLangStatementExpression createStatementExpression(BLangStatement stmt, BLangExpression expr) {
        BLangStatementExpression stmtExpr = (BLangStatementExpression) TreeBuilder.creatStatementExpression();
        stmtExpr.stmt = stmt;
        stmtExpr.expr = expr;
        stmtExpr.pos = stmt.pos;
        return stmtExpr;
    }

    public static BLangMatchExpression createMatchExpression(BLangExpression expr) {
        BLangMatchExpression matchExpr = (BLangMatchExpression) TreeBuilder.createMatchExpression();
        matchExpr.expr = expr;
        return matchExpr;
    }

    public static BLangFieldBasedAccess createFieldAccessExpr(BLangExpression varRef, BLangIdentifier field) {
        return createFieldAccessExpr(varRef, field, false);
    }

    public static BLangFieldBasedAccess createFieldAccessExpr(BLangExpression varRef, BLangIdentifier field,
                                                              boolean except) {
        BLangFieldBasedAccess fieldAccessExpr = (BLangFieldBasedAccess) TreeBuilder.createFieldBasedAccessNode();
        fieldAccessExpr.expr = varRef;
        fieldAccessExpr.field = field;
        return fieldAccessExpr;
    }

    public static BLangIndexBasedAccess createIndexAccessExpr(BLangExpression varRef,
                                                              BLangExpression indexExpr) {
        BLangIndexBasedAccess fieldAccessExpr = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        fieldAccessExpr.expr = varRef;
        fieldAccessExpr.indexExpr = indexExpr;
        return fieldAccessExpr;
    }

    public static BLangFunction createInitFunctionWithNilReturn(Location pos, String name, Name suffix) {
        BLangValueType typeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        typeNode.pos = pos;
        typeNode.typeKind = TypeKind.NIL;
        return createInitFunction(pos, name, suffix, typeNode);
    }

    static BLangFunction createInitFunctionWithErrorOrNilReturn(Location pos, String name, Name suffix,
                                                                SymbolTable symTable) {
        if (symTable.errorOrNilType == null) {
            // The error type may not have been loaded when compiling modules such as annotations.
            // Such modules are builtin and can never return error, so we set a nil returning init function.
            return createInitFunctionWithNilReturn(pos, name, suffix);
        }

        BLangValueType typeNode = (BLangValueType) TreeBuilder.createValueTypeNode();
        typeNode.pos = pos;
        typeNode.typeKind = TypeKind.UNION;
        typeNode.setBType(symTable.errorOrNilType);
        return createInitFunction(pos, name, suffix, typeNode);
    }

    private static BLangFunction createInitFunction(Location pos, String name, Name suffix,
                                                    BLangValueType returnTypeNode) {
        BLangFunction initFunction = (BLangFunction) TreeBuilder.createFunctionNode();
        initFunction.setName(createIdentifier(name + suffix.getValue()));
        initFunction.flagSet = EnumSet.of(Flag.PUBLIC);
        initFunction.pos = pos;

        initFunction.returnTypeNode = returnTypeNode;

        // Create body of the init function
        BLangBlockFunctionBody body = (BLangBlockFunctionBody) TreeBuilder.createBlockFunctionBodyNode();
        body.pos = pos;
        initFunction.setBody(body);
        return initFunction;
    }

    static BLangServiceConstructorExpr createServiceConstructor(BLangService service) {
        BLangServiceConstructorExpr constExpr = (BLangServiceConstructorExpr) TreeBuilder
                .createServiceConstructorNode();
        constExpr.pos = service.pos;
        constExpr.serviceNode = service;
        constExpr.setBType(service.symbol.type);
        return constExpr;
    }

    public static BLangSimpleVariable createReceiver(Location pos, BType type) {
        BLangSimpleVariable receiver = (BLangSimpleVariable) TreeBuilder.createSimpleVariableNode();
        receiver.pos = pos;
        IdentifierNode identifier = createIdentifier(pos, Names.SELF.getValue());
        receiver.setName(identifier);
        receiver.setBType(type);
        return receiver;
    }

    public static BLangNamedArgsExpression createNamedArg(String argName, BLangExpression expr) {
        BLangNamedArgsExpression argExpr = new BLangNamedArgsExpression();
        argExpr.name = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        argExpr.name.value = argName;
        argExpr.expr = expr;
        return argExpr;
    }

    private static IdentifierNode createIdentifier(String value) {
        IdentifierNode node = TreeBuilder.createIdentifierNode();
        if (value != null) {
            node.setValue(value);
        }
        return node;
    }

    public static BInvokableSymbol duplicateInvokableSymbol(BInvokableSymbol invokableSymbol) {
        BInvokableSymbol dupFuncSymbol =
                Symbols.createFunctionSymbol(invokableSymbol.flags, invokableSymbol.name, invokableSymbol.originalName,
                                             invokableSymbol.pkgID, invokableSymbol.type, invokableSymbol.owner,
                                             invokableSymbol.bodyExist, invokableSymbol.pos, invokableSymbol.origin);
        dupFuncSymbol.receiverSymbol = invokableSymbol.receiverSymbol;
        dupFuncSymbol.retType = invokableSymbol.retType;
        dupFuncSymbol.restParam = invokableSymbol.restParam;
        dupFuncSymbol.params = new ArrayList<>(invokableSymbol.params);
        dupFuncSymbol.tainted = invokableSymbol.tainted;
        dupFuncSymbol.closure = invokableSymbol.closure;
        dupFuncSymbol.markdownDocumentation = invokableSymbol.markdownDocumentation;
        dupFuncSymbol.scope = invokableSymbol.scope;
        dupFuncSymbol.tag = invokableSymbol.tag;
        dupFuncSymbol.schedulerPolicy = invokableSymbol.schedulerPolicy;
        dupFuncSymbol.strandName = invokableSymbol.strandName;

        BInvokableType prevFuncType = (BInvokableType) invokableSymbol.type;
        BInvokableType dupInvokableType = new BInvokableType(new ArrayList<>(prevFuncType.paramTypes),
                                                          prevFuncType.restType, prevFuncType.retType,
                                                          prevFuncType.tsymbol);

        if (Symbols.isFlagOn(invokableSymbol.flags, Flags.ISOLATED)) {
            dupFuncSymbol.flags |= Flags.ISOLATED;
            dupInvokableType.flags |= Flags.ISOLATED;
        }

        if (Symbols.isFlagOn(invokableSymbol.flags, Flags.TRANSACTIONAL)) {
            dupFuncSymbol.flags |= Flags.TRANSACTIONAL;
            dupInvokableType.flags |= Flags.TRANSACTIONAL;
        }

        dupFuncSymbol.type = dupInvokableType;
        dupFuncSymbol.dependentGlobalVars = invokableSymbol.dependentGlobalVars;

        return dupFuncSymbol;
    }

    public static BInvokableSymbol duplicateFunctionDeclarationSymbol(BInvokableSymbol invokableSymbol,
                                                                      BSymbol owner,
                                                                      Name newName,
                                                                      PackageID newPkgID,
                                                                      Location location,
                                                                      SymbolOrigin origin) {
        BInvokableSymbol dupFuncSymbol = Symbols.createFunctionSymbol(invokableSymbol.flags, newName, newName, newPkgID,
                                                                      null, owner, invokableSymbol.bodyExist,
                                                                      location, origin);
        dupFuncSymbol.receiverSymbol = invokableSymbol.receiverSymbol;
        dupFuncSymbol.retType = invokableSymbol.retType;
        dupFuncSymbol.receiverSymbol = null;
        dupFuncSymbol.flags |= Flags.INTERFACE;

        dupFuncSymbol.params = invokableSymbol.params.stream()
                .map(param -> duplicateParamSymbol(param, dupFuncSymbol))
                .collect(Collectors.toList());
        if (dupFuncSymbol.restParam != null) {
            dupFuncSymbol.restParam = duplicateParamSymbol(invokableSymbol.restParam, dupFuncSymbol);
        }

        dupFuncSymbol.tainted = invokableSymbol.tainted;
        dupFuncSymbol.closure = invokableSymbol.closure;
        dupFuncSymbol.tag = invokableSymbol.tag;
        dupFuncSymbol.markdownDocumentation = invokableSymbol.markdownDocumentation;

        BInvokableType prevFuncType = (BInvokableType) invokableSymbol.type;
        BType newFuncType = new BInvokableType(new ArrayList<>(prevFuncType.paramTypes), prevFuncType.restType,
                                               prevFuncType.retType, prevFuncType.tsymbol);
        newFuncType.flags |= prevFuncType.flags;
        dupFuncSymbol.type = newFuncType;
        return dupFuncSymbol;
    }

    private static BVarSymbol duplicateParamSymbol(BVarSymbol paramSymbol, BInvokableSymbol owner) {
        BVarSymbol newParamSymbol = new BVarSymbol(paramSymbol.flags, paramSymbol.name, paramSymbol.pkgID,
                                                   paramSymbol.type, owner, paramSymbol.pos, paramSymbol.origin);
        newParamSymbol.tainted = paramSymbol.tainted;
        newParamSymbol.isDefaultable = paramSymbol.isDefaultable;
        newParamSymbol.markdownDocumentation = paramSymbol.markdownDocumentation;
        return newParamSymbol;
    }

    private static List<BLangExpression> generateArgExprsForLambdas(Location location,
                                                                    List<BLangSimpleVariable> args,
                                                                    List<BVarSymbol> formalParams,
                                                                    SymbolResolver symResolver) {
        List<BLangExpression> argsExpr = new ArrayList<>();
        final List<BLangSimpleVarRef> variableRefList = createVariableRefList(location, args);
        int mapSymbolsParams = formalParams.size() - args.size();
        for (int i = 0; i < variableRefList.size(); i++) {
            BLangSimpleVarRef varRef = variableRefList.get(i);
            BType target = formalParams.get(i + mapSymbolsParams).type;
            BType source = varRef.symbol.type;
            if (source != target) {
                argsExpr.add(generateConversionExpr(varRef, target, symResolver));
                continue;
            }
            argsExpr.add(varRef);
        }
        return argsExpr;
    }

    static BLangXMLTextLiteral createXMLTextLiteralNode(BLangBinaryExpr parent, BLangExpression concatExpr,
                                                        Location pos, BType type) {
        BLangXMLTextLiteral xmlTextLiteral = new BLangXMLTextLiteral();
        xmlTextLiteral.concatExpr = concatExpr;
        xmlTextLiteral.pos = pos;
        xmlTextLiteral.parent = parent;
        xmlTextLiteral.setBType(type);
        return xmlTextLiteral;
    }

    public static BLangDynamicArgExpr createDynamicParamExpression(BLangExpression condition,
                                                                   BLangExpression conditionalArg) {
        BLangDynamicArgExpr dynamicExpression = new BLangDynamicArgExpr();
        dynamicExpression.condition = condition;
        dynamicExpression.conditionalArgument = conditionalArg;
        return dynamicExpression;
    }

    public static BLangTernaryExpr createTernaryExprNode(BType type, BLangExpression expr, BLangExpression thenExpr,
                                                         BLangExpression elseExpr) {
        BLangTernaryExpr ternaryExpr = (BLangTernaryExpr) TreeBuilder.createTernaryExpressionNode();
        ternaryExpr.elseExpr = elseExpr;
        ternaryExpr.thenExpr = thenExpr;
        ternaryExpr.expr = expr;
        ternaryExpr.setBType(type);
        return ternaryExpr;
    }

    public static BLangIndexBasedAccess createMemberAccessExprNode(BType type, BLangExpression expr,
                                                                   BLangExpression indexExpr) {
        BLangIndexBasedAccess memberAccessExpr = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        memberAccessExpr.expr = expr;
        memberAccessExpr.indexExpr = indexExpr;
        memberAccessExpr.setBType(type);
        return memberAccessExpr;
    }

    public static BLangExpression createIgnoreExprNode(BType type) {
        BLangExpression ignoreExpr = (BLangExpression) TreeBuilder.createIgnoreExprNode();
        ignoreExpr.setBType(type);
        return ignoreExpr;
    }
}

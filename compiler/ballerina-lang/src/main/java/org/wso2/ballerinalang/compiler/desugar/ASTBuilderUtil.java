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

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.OperatorKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BConversionOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIsAssignableExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeConversionExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypedescExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangVariableReference;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch.BLangMatchStmtPatternClause;
import org.wso2.ballerinalang.compiler.tree.statements.BLangNext;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.programfile.InstructionCodes;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

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
    static void appendStatements(BLangBlockStmt generatedCode, BLangBlockStmt target) {
        int index = 0;
        if (target.stmts.get(target.stmts.size() - 1).getKind() == NodeKind.RETURN) {
            index = target.stmts.size() - 1;
        }
        for (BLangStatement stmt : generatedCode.stmts) {
            target.stmts.add(index++, stmt);
        }
    }

    static void defineVariable(BLangVariable variable, BSymbol targetSymbol, Names names) {
        variable.symbol = new BVarSymbol(0, names.fromIdNode(variable.name), targetSymbol.pkgID, variable.type,
                targetSymbol);
        targetSymbol.scope.define(variable.symbol.name, variable.symbol);
    }

    private static boolean isValueType(BType type) {
        return type.tag < TypeTags.TYPEDESC;
    }

    static BLangExpression wrapToConversionExpr(BType sourceType, BLangExpression exprToWrap,
                                                SymbolTable symTable, Types types) {
        if (types.isSameType(sourceType, exprToWrap.type) || !isValueType(exprToWrap.type)) {
            // No conversion needed.
            return exprToWrap;
        }
        BLangTypeConversionExpr castExpr = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        castExpr.expr = exprToWrap;
        castExpr.conversionSymbol = Symbols.createUnboxValueTypeOpSymbol(symTable.anyType, exprToWrap.type);
        castExpr.type = exprToWrap.type;
        return castExpr;
    }

    static BLangFunction createFunction(DiagnosticPos pos, String name) {
        final BLangFunction bLangFunction = (BLangFunction) TreeBuilder.createFunctionNode();
        final IdentifierNode funcName = createIdentifier(pos, name);
        bLangFunction.setName(funcName);
        bLangFunction.flagSet = EnumSet.of(Flag.LAMBDA);
        bLangFunction.pos = pos;
        // Create body of the function
        bLangFunction.body = createBlockStmt(pos);
        return bLangFunction;
    }

    static BLangType createTypeNode(BType type) {
        BLangType bLangType = new BLangType() {
            @Override
            public void accept(BLangNodeVisitor visitor) {
            }

            @Override
            public NodeKind getKind() {
                return null;
            }
        };
        bLangType.type = type;
        return bLangType;
    }

    static BLangIf createIfStmt(DiagnosticPos pos, BLangBlockStmt target) {
        final BLangIf ifNode = (BLangIf) TreeBuilder.createIfElseStatementNode();
        ifNode.pos = pos;
        target.addStatement(ifNode);
        return ifNode;
    }

    static BLangIf createIfElseStmt(DiagnosticPos pos,
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

    static BLangForeach createForeach(DiagnosticPos pos,
                                      BLangBlockStmt target,
                                      BLangSimpleVarRef collectionVarRef,
                                      List<BLangSimpleVarRef> varRefs,
                                      List<BType> inputTypes) {
        final BLangForeach foreach = (BLangForeach) TreeBuilder.createForeachNode();
        foreach.pos = pos;
        target.addStatement(foreach);
        foreach.body = ASTBuilderUtil.createBlockStmt(pos);
        foreach.collection = collectionVarRef;
        foreach.varRefs.addAll(varRefs);
        foreach.varTypes = inputTypes;
        return foreach;
    }

    static BLangVariableDef createVariableDefStmt(DiagnosticPos pos, BLangBlockStmt target) {
        final BLangVariableDef variableDef = createVariableDef(pos);
        target.addStatement(variableDef);
        return variableDef;
    }

    static BLangVariableDef createVariableDef(DiagnosticPos pos) {
        final BLangVariableDef variableDef = (BLangVariableDef) TreeBuilder.createVariableDefinitionNode();
        variableDef.pos = pos;
        return variableDef;
    }

    static BLangAssignment createAssignmentStmt(DiagnosticPos pos, BLangBlockStmt target) {
        final BLangAssignment assignment = (BLangAssignment) TreeBuilder.createAssignmentNode();
        assignment.pos = pos;
        target.addStatement(assignment);
        return assignment;
    }

    static BLangAssignment createAssignmentStmt(DiagnosticPos pos, BLangExpression varRef,
                                                BLangExpression rhsExpr, boolean declaredWithVar) {
        final BLangAssignment assignment = (BLangAssignment) TreeBuilder.createAssignmentNode();
        assignment.pos = pos;
        assignment.varRef = varRef;
        assignment.expr = rhsExpr;
        assignment.declaredWithVar = declaredWithVar;
        return assignment;
    }

    static BLangExpressionStmt createExpressionStmt(DiagnosticPos pos, BLangBlockStmt target) {
        final BLangExpressionStmt exprStmt = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
        exprStmt.pos = pos;
        target.addStatement(exprStmt);
        return exprStmt;
    }

    static BLangReturn createReturnStmt(DiagnosticPos pos, BLangBlockStmt target) {
        final BLangReturn returnStmt = (BLangReturn) TreeBuilder.createReturnNode();
        returnStmt.pos = pos;
        target.addStatement(returnStmt);
        return returnStmt;
    }

    public static BLangReturn createNilReturnStmt(DiagnosticPos pos, BType nilType) {
        final BLangReturn returnStmt = (BLangReturn) TreeBuilder.createReturnNode();
        returnStmt.pos = pos;
        returnStmt.expr = createLiteral(pos, nilType, Names.NIL_VALUE);
        return returnStmt;
    }

    static void createNextStmt(DiagnosticPos pos, BLangBlockStmt target) {
        final BLangNext nextStmt = (BLangNext) TreeBuilder.createNextNode();
        nextStmt.pos = pos;
        target.addStatement(nextStmt);
    }

    static BLangBlockStmt createBlockStmt(DiagnosticPos pos) {
        final BLangBlockStmt blockNode = (BLangBlockStmt) TreeBuilder.createBlockNode();
        blockNode.pos = pos;
        return blockNode;
    }

    static BLangBlockStmt createBlockStmt(DiagnosticPos pos, List<BLangStatement> stmts) {
        final BLangBlockStmt blockNode = (BLangBlockStmt) TreeBuilder.createBlockNode();
        blockNode.pos = pos;
        blockNode.stmts = stmts;
        return blockNode;
    }

    static BLangMatchStmtPatternClause createMatchStatementPattern(DiagnosticPos pos,
                                                                   BLangVariable variable,
                                                                   BLangBlockStmt body) {
        BLangMatchStmtPatternClause patternClause =
                (BLangMatchStmtPatternClause) TreeBuilder.createMatchStatementPattern();
        patternClause.pos = pos;
        patternClause.variable = variable;
        patternClause.body = body;
        return patternClause;

    }

    static BLangMatch createMatchStatement(DiagnosticPos pos,
                                           BLangExpression expr,
                                           List<BLangMatchStmtPatternClause> patternClauses) {
        BLangMatch matchStmt = (BLangMatch) TreeBuilder.createMatchStatement();
        matchStmt.pos = pos;
        matchStmt.expr = expr;
        matchStmt.patternClauses = patternClauses;
        return matchStmt;
    }

    static BLangUnaryExpr createUnaryExpr(DiagnosticPos pos) {
        return createUnaryExpr(pos, null, null, null, null);
    }

    static BLangUnaryExpr createUnaryExpr(DiagnosticPos pos,
                                          BLangExpression expr,
                                          BType type,
                                          OperatorKind kind,
                                          BOperatorSymbol symbol) {
        final BLangUnaryExpr unaryExpr = (BLangUnaryExpr) TreeBuilder.createUnaryExpressionNode();
        unaryExpr.pos = pos;
        unaryExpr.expr = expr;
        unaryExpr.type = type;
        unaryExpr.operator = kind;
        unaryExpr.opSymbol = symbol;
        return unaryExpr;
    }

    static BLangTypedescExpr createTypeofExpr(DiagnosticPos pos, BType type, BType resolvedType) {
        final BLangTypedescExpr typeofExpr = (BLangTypedescExpr) TreeBuilder.createTypeAccessNode();
        typeofExpr.pos = pos;
        typeofExpr.type = type;
        typeofExpr.resolvedType = resolvedType;
        return typeofExpr;
    }

    static BLangIndexBasedAccess createIndexBasesAccessExpr(DiagnosticPos pos, BType type, BVarSymbol varSymbol,
                                                            BLangExpression indexExpr) {
        final BLangIndexBasedAccess arrayAccess = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        arrayAccess.pos = pos;
        arrayAccess.expr = createVariableRef(pos, varSymbol);
        arrayAccess.indexExpr = indexExpr;
        arrayAccess.type = type;
        return arrayAccess;
    }

    static BLangExpression generateConversionExpr(BLangExpression varRef, BType target, SymbolResolver symResolver) {
        if (varRef.type.tag == target.tag || varRef.type.tag > TypeTags.TYPEDESC) {
            return varRef;
        }
        // Box value using cast expression.
        final BLangTypeConversionExpr conversion = (BLangTypeConversionExpr) TreeBuilder.createTypeConversionNode();
        conversion.pos = varRef.pos;
        conversion.expr = varRef;
        conversion.type = target;
        conversion.targetType = target;
        conversion.conversionSymbol = (BConversionOperatorSymbol) symResolver.resolveConversionOperator(varRef.type,
                target);
        return conversion;
    }

    static List<BLangExpression> generateArgExprs(DiagnosticPos pos, List<BLangVariable> args,
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

    static BLangInvocation createInvocationExpr(DiagnosticPos pos, BInvokableSymbol invokableSymbol,
                                                List<BLangVariable> requiredArgs, SymbolResolver symResolver) {
        return createInvocationExpr(pos, invokableSymbol, requiredArgs, new ArrayList<>(), new ArrayList<>(),
                symResolver);
    }

    static BLangInvocation createInvocationExpr(DiagnosticPos pos, BInvokableSymbol invokableSymbol,
                                                List<BLangVariable> requiredArgs, List<BLangVariable> namedArgs,
                                                List<BLangVariable> restArgs, SymbolResolver symResolver) {
        final BLangInvocation invokeLambda = (BLangInvocation) TreeBuilder.createInvocationNode();
        invokeLambda.pos = pos;
        invokeLambda.requiredArgs.addAll(generateArgExprs(pos, requiredArgs, invokableSymbol.params, symResolver));
        invokeLambda.namedArgs
                .addAll(generateArgExprs(pos, namedArgs, invokableSymbol.defaultableParams, symResolver));
        invokeLambda.restArgs
                .addAll(generateArgExprs(pos, restArgs, Lists.of(invokableSymbol.restParam), symResolver));

        invokeLambda.symbol = invokableSymbol;
        invokeLambda.type = ((BInvokableType) invokableSymbol.type).retType;
        return invokeLambda;
    }

    static List<BLangSimpleVarRef> createVariableRefList(DiagnosticPos pos, List<BLangVariable> args) {
        final List<BLangSimpleVarRef> varRefs = new ArrayList<>();
        args.forEach(variable -> varRefs.add(createVariableRef(pos, variable.symbol)));
        return varRefs;
    }

    static BLangSimpleVarRef createVariableRef(DiagnosticPos pos, BVarSymbol varSymbol) {
        final BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        varRef.pos = pos;
        varRef.variableName = createIdentifier(pos, varSymbol.name.value);
        varRef.symbol = varSymbol;
        varRef.type = varSymbol.type;
        return varRef;
    }

    static BLangVariable createVariable(DiagnosticPos pos,
                                        String name,
                                        BType type,
                                        BLangExpression expr,
                                        BVarSymbol varSymbol) {
        final BLangVariable varNode = (BLangVariable) TreeBuilder.createVariableNode();
        varNode.pos = pos;
        varNode.name = createIdentifier(pos, name);
        varNode.type = type;
        varNode.expr = expr;
        varNode.symbol = varSymbol;
        return varNode;
    }

    static BLangVariable createVariable(DiagnosticPos pos, String name, BType type) {
        return createVariable(pos, name, type, null, null);
    }

    static BLangVariableDef createVariableDef(DiagnosticPos pos, BLangVariable variable) {
        final BLangVariableDef variableDef = (BLangVariableDef) TreeBuilder.createVariableDefinitionNode();
        variableDef.pos = pos;
        variableDef.var = variable;
        return variableDef;
    }

    static BLangBinaryExpr createBinaryExpr(DiagnosticPos pos,
                                            BLangExpression lhsExpr,
                                            BLangExpression rhsExpr,
                                            BType type,
                                            OperatorKind opKind,
                                            BOperatorSymbol symbol) {
        final BLangBinaryExpr binaryExpr = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        binaryExpr.pos = pos;
        binaryExpr.lhsExpr = lhsExpr;
        binaryExpr.rhsExpr = rhsExpr;
        binaryExpr.type = type;
        binaryExpr.opKind = opKind;
        binaryExpr.opSymbol = symbol;
        return binaryExpr;
    }

    static BLangIsAssignableExpr createIsAssignableExpr(DiagnosticPos pos,
                                                        BLangExpression lhsExpr,
                                                        BType targetType,
                                                        BType type,
                                                        Names names) {
        final BLangIsAssignableExpr assignableExpr = new BLangIsAssignableExpr();
        assignableExpr.pos = pos;
        assignableExpr.lhsExpr = lhsExpr;
        assignableExpr.targetType = targetType;
        assignableExpr.type = type;
        assignableExpr.opSymbol = new BOperatorSymbol(names.fromString(assignableExpr.opKind.value()),
                null, targetType, null, InstructionCodes.IS_ASSIGNABLE);
        return assignableExpr;
    }

    static BLangLiteral createLiteral(DiagnosticPos pos, BType type, Object value) {
        final BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
        literal.pos = pos;
        literal.value = value;
        literal.typeTag = type.tag;
        literal.type = type;
        return literal;
    }

    static BLangRecordLiteral createEmptyRecordLiteral(DiagnosticPos pos, BType type) {
        final BLangRecordLiteral recordLiteralNode = (BLangRecordLiteral) TreeBuilder.createRecordLiteralNode();
        recordLiteralNode.pos = pos;
        recordLiteralNode.type = type;
        return recordLiteralNode;
    }

    static BLangIdentifier createIdentifier(DiagnosticPos pos, String value) {
        final BLangIdentifier node = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        node.pos = pos;
        if (value != null) {
            node.setValue(value);
        }
        return node;
    }

    public static BLangStatementExpression creatStatementExpression(BLangStatement stmt, BLangExpression expr) {
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

    public static BLangFieldBasedAccess createFieldAccessExpr(BLangVariableReference varRef, BLangIdentifier field) {
        BLangFieldBasedAccess fieldAccessExpr = (BLangFieldBasedAccess) TreeBuilder.createFieldBasedAccessNode();
        fieldAccessExpr.expr = varRef;
        fieldAccessExpr.field = field;
        return fieldAccessExpr;
    }
    
    public static BLangIndexBasedAccess createIndexAccessExpr(BLangVariableReference varRef, BLangExpression indexExpr) {
        BLangIndexBasedAccess fieldAccessExpr = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        fieldAccessExpr.expr = varRef;
        fieldAccessExpr.indexExpr = indexExpr;
        return fieldAccessExpr;
    }
}

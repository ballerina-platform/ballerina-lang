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
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.model.tree.OperatorKind;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.iterable.IterableContext;
import org.wso2.ballerinalang.compiler.semantics.model.iterable.IterableKind;
import org.wso2.ballerinalang.compiler.semantics.model.iterable.Operation;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeCastExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangNext;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class responsible for desugar an iterable chain into actual Ballerina code.
 *
 * @since 0.961.0
 */
public class IterableCodeDesugar {

    private static final String FUNC_CALLER = "$lambda$iterable";
    private static final String FUNC_STREAM = "$lambda$stream";
    private static final String VAR_ARG = "arg";
    private static final String VAR_SKIP = "skip";
    private static final String VAR_RESULT = "result";
    private static final String VAR_COUNT = "count";
    private static final String VAR_COLLECTION = "collection";

    private static final CompilerContext.Key<IterableCodeDesugar> ITERABLE_DESUGAR_KEY =
            new CompilerContext.Key<>();

    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final SymbolEnter symbolEnter;
    private final Names names;

    private int lambdaFunctionCount = 0;
    private int variableCount = 0;

    public static IterableCodeDesugar getInstance(CompilerContext context) {
        IterableCodeDesugar desugar = context.get(ITERABLE_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new IterableCodeDesugar(context);
        }

        return desugar;
    }

    private IterableCodeDesugar(CompilerContext context) {
        context.put(ITERABLE_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.names = Names.getInstance(context);
    }

    public void desugar(IterableContext ctx) {
        rewrite(ctx);
        generateIteratorFunction(ctx);

        // create invocation expression to invoke iterable operation.
        final BLangSimpleVarRef collectionRef = (BLangSimpleVarRef) ctx.collectionExpr;
        final BLangVariable collectionVar = createVariable(collectionRef.pos, collectionRef.variableName.value,
                collectionRef.type);
        collectionVar.symbol = collectionRef.symbol;
        ctx.iteratorCaller = createInvocationExpr(collectionRef.pos, ctx.iteratorFuncSymbol, Lists.of(collectionVar));
    }

    private void rewrite(IterableContext ctx) {
        variableCount = 0;
        ctx.operations.forEach(this::rewrite);
        ctx.collectionExpr = ctx.getFirstOperation().iExpr.expr;
    }

    private void rewrite(Operation operation) {
        // Update desugered lambda expression.
        operation.lambda = operation.lambda != null ? operation.iExpr.argExprs.get(0) : null;
        generateVariables(operation);
    }

    /**
     * calculate and generate each input and output variables of each operation.
     *
     * @param operation current operation
     */
    private void generateVariables(Operation operation) {
        // Add input types.
        if (operation.previous == null) {
            // first Operation.
            for (BType varType : operation.argTypes) {
                operation.argVars.add(createVariable(operation.pos, VAR_ARG + variableCount++, varType));
            }
        } else {
            Operation lastOperation = operation.previous;
            if (lastOperation.kind == IterableKind.FILTER) {
                operation.argVars.addAll(lastOperation.argVars);
            } else {
                operation.argVars.addAll(lastOperation.retVars);
            }
        }
        // Add output types.
        for (BType varType : operation.retArgTypes) {
            operation.retVars.add(createVariable(operation.pos, VAR_ARG + variableCount++, varType));
        }
    }

    private void defineVariable(BLangVariable variable, PackageID pkgID, BLangFunction funcNode) {
        variable.symbol = new BVarSymbol(0, names.fromIdNode(variable.name), pkgID, variable.type, funcNode.symbol);
        funcNode.symbol.scope.define(variable.symbol.name, variable.symbol);
    }

    private void generateIteratorFunction(IterableContext ctx) {
        final Operation firstOperation = ctx.getFirstOperation();
        final Operation lastOperation = ctx.getLastOperation();
        final DiagnosticPos pos = firstOperation.pos;

        // Create and define function signature.
        final BLangFunction funcNode = createFunction(pos, FUNC_CALLER);
        funcNode.params.add(createVariable(pos, VAR_COLLECTION, ctx.collectionExpr.type));
        if (isReturningIteratorFunction(ctx)) {
            funcNode.retParams.add(ctx.resultVar = createVariable(pos, VAR_RESULT, ctx.resultType));
        }

        final BPackageSymbol packageSymbol = firstOperation.env.enclPkg.symbol;
        final SymbolEnv packageEnv = symbolEnter.packageEnvs.get(packageSymbol);
        symbolEnter.defineNode(funcNode, packageEnv);
        ctx.iteratorFuncSymbol = funcNode.symbol;
        packageEnv.enclPkg.functions.add(funcNode);
        packageEnv.enclPkg.topLevelNodes.add(funcNode);

        // Generate function Body.
        if (isReturningIteratorFunction(ctx)) {
            generateCounterVariable(funcNode.body, ctx, funcNode);
            generateResultVariable(funcNode.body, ctx, ctx.resultVar);
        }
        // create and define required variables.
        generateVarDefForStream(funcNode.body, ctx, funcNode);

        // Generate foreach iteration.
        final BLangForeach foreach = createForeach(pos, funcNode.body);
        final List<BLangVariable> foreachVars = copyOf(ctx.getFirstOperation().argVars);
        foreachVars.forEach(variable -> defineVariable(variable, packageSymbol.pkgID, funcNode));
        foreach.varRefs.addAll(createVariableRefList(pos, foreachVars));
        foreach.collection = createVariableRef(pos, funcNode.params.get(0).symbol);
        foreach.varTypes = firstOperation.argTypes;
        foreach.body = createBlockStmt(pos);

        // Call Stream function and its assignment.
        generateStreamFunction(ctx);
        final BLangInvocation iExpr = createInvocationExpr(pos, ctx.streamFuncSymbol, foreachVars);
        BLangAssignment assignment = createAssignmentStmt(pos, foreach.body);
        assignment.expr = iExpr;
        assignment.varRefs.addAll(createVariableRefList(pos, ctx.streamRetVars));

        // Generate aggregator and result
        if (isReturningIteratorFunction(ctx)) {
            generateNextCondition(foreach.body, ctx);
            generateAggregator(foreach.body, ctx);
            generateFinalResult(funcNode.body, ctx);
        }

        final BLangReturn returnStmt = createReturnStmt(lastOperation.pos, funcNode.body);
        returnStmt.addExpression(createVariableRef(pos, ctx.resultVar.symbol));
    }

    private boolean isReturningIteratorFunction(IterableContext ctx) {
        return ctx.resultType != symTable.noType;
    }

    /**
     * Generates following.
     *
     * int count = 0;
     *
     * @param blockStmt target
     * @param ctx       current context
     * @param funcNode  functionNode
     */
    private void generateCounterVariable(BLangBlockStmt blockStmt, IterableContext ctx, BLangFunction funcNode) {
        if (ctx.resultType.tag != TypeTags.ARRAY && ctx.getLastOperation().kind != IterableKind.AVERAGE) {
            return;
        }
        final DiagnosticPos pos = blockStmt.pos;
        ctx.countVar = createVariable(pos, VAR_COUNT, symTable.intType);
        ctx.countVar.expr = createLiteral(pos, symTable.intType, 0L);
        defineVariable(ctx.countVar, funcNode.symbol.pkgID, funcNode);
        createVariableDefStmt(pos, blockStmt).var = ctx.countVar;
    }

    /**
     * Generates following.
     *
     * array:   result =[];
     * map:     result = {};
     *
     * @param blockStmt target
     * @param ctx       current context
     * @param varResult result variable
     */
    private void generateResultVariable(BLangBlockStmt blockStmt, IterableContext ctx, BLangVariable varResult) {
        if (ctx.resultType.tag != TypeTags.ARRAY && ctx.resultType.tag != TypeTags.MAP) {
            return;
        }
        final DiagnosticPos pos = blockStmt.pos;
        final BLangAssignment assignment = createAssignmentStmt(pos, blockStmt);
        assignment.varRefs.add(createVariableRef(pos, varResult.symbol));
        switch (ctx.resultType.tag) {
            case TypeTags.ARRAY:
                final BLangArrayLiteral arrayInit = (BLangArrayLiteral) TreeBuilder.createArrayLiteralNode();
                arrayInit.pos = pos;
                arrayInit.exprs = new ArrayList<>();
                arrayInit.type = ctx.resultType;
                assignment.expr = arrayInit;
                break;
            case TypeTags.MAP:
                final BLangRecordLiteral record = (BLangRecordLiteral) TreeBuilder.createRecordLiteralNode();
                record.pos = pos;
                record.type = ctx.resultType;
                assignment.expr = record;
                break;
        }
    }

    /**
     * Generates following.
     *
     * var arg1;
     * var arg2;
     * ...
     *
     * @param blockStmt target
     * @param ctx       current context
     * @param funcNode  functionNode
     */
    private void generateVarDefForStream(BLangBlockStmt blockStmt, IterableContext ctx, BLangFunction funcNode) {
        ctx.streamRetVars = new ArrayList<>();
        ctx.streamRetVars.add(ctx.skipVar = createVariable(blockStmt.pos, VAR_SKIP, symTable.booleanType));
        ctx.streamRetVars.addAll(copyOf(ctx.getLastOperation().retVars));
        ctx.streamRetVars.forEach(variable -> {
            defineVariable(variable, funcNode.symbol.pkgID, funcNode);
            createVariableDefStmt(blockStmt.pos, funcNode.body).var = variable;
        });
    }

    /**
     * Generate Stream function from operation chain.
     *
     * @param ctx current context
     */
    private void generateStreamFunction(IterableContext ctx) {
        final DiagnosticPos pos = ctx.getFirstOperation().pos;
        // Create and define function signature.
        final BLangFunction funcNode = createFunction(pos, FUNC_STREAM);
        funcNode.params.addAll(ctx.getFirstOperation().argVars);
        funcNode.retParams.add(createVariable(pos, VAR_SKIP, symTable.booleanType));
        funcNode.retParams.addAll(ctx.getLastOperation().retVars);

        final BPackageSymbol packageSymbol = ctx.getFirstOperation().env.enclPkg.symbol;
        final SymbolEnv packageEnv = symbolEnter.packageEnvs.get(packageSymbol);
        symbolEnter.defineNode(funcNode, packageEnv);
        ctx.streamFuncSymbol = funcNode.symbol;
        packageEnv.enclPkg.functions.add(funcNode);
        packageEnv.enclPkg.topLevelNodes.add(funcNode);

        // Define all undefined variables.
        Set<BLangVariable> unusedVars = new HashSet<>();
        ctx.operations.forEach(operation -> {
            unusedVars.addAll(operation.argVars);
            unusedVars.addAll(operation.retVars);
        });
        unusedVars.removeAll(funcNode.params);
        unusedVars.removeAll(funcNode.retParams);
        unusedVars.forEach(variable -> defineVariable(variable, packageSymbol.pkgID, funcNode));
        unusedVars.forEach(variable -> {
            BLangVariableDef variableDefStmt = createVariableDefStmt(pos, funcNode.body);
            variableDefStmt.var = variable;
        });
        // Generate function Body.
        ctx.operations.forEach(operation -> generateOperationCode(funcNode.body, operation, ctx));
        generateStreamReturnStmt(funcNode.body, ctx.getLastOperation().retVars);
    }

    /**
     * Generates following.
     *
     * return true, vx... ;
     *
     * @param blockStmt target
     * @param retArgs   return variables
     */
    private void generateStreamReturnStmt(BLangBlockStmt blockStmt, List<BLangVariable> retArgs) {
        final DiagnosticPos pos = blockStmt.pos;
        final BLangReturn returnStmt = createReturnStmt(pos, blockStmt);

        returnStmt.exprs.add(createLiteral(pos, symTable.booleanType, false));
        returnStmt.exprs.addAll(createVariableRefList(pos, retArgs));
    }

    /**
     * Generates following.
     *
     * if(skip){
     * next;
     * }
     *
     * @param blockStmt target
     * @param ctx       current context
     */
    private void generateNextCondition(BLangBlockStmt blockStmt, IterableContext ctx) {
        final DiagnosticPos pos = ctx.getLastOperation().pos;
        final BLangIf ifNode = createIfStmt(pos, blockStmt);
        ifNode.expr = createVariableRef(pos, ctx.skipVar.symbol);
        ifNode.body = createBlockStmt(pos);
        createNextStmt(pos, ifNode.body);
    }


    /* Aggregator related code generation */

    /**
     * Generates target aggregator logic.
     *
     * @param blockStmt target
     * @param ctx       current context
     */
    private void generateAggregator(BLangBlockStmt blockStmt, IterableContext ctx) {
        switch (ctx.getLastOperation().kind) {
            case COUNT:
                generateCountAggregator(blockStmt, ctx.resultVar);
                return;
            case SUM:
                generateSumAggregator(blockStmt, ctx);
                return;
            case AVERAGE:
                generateSumAggregator(blockStmt, ctx);
                generateCountAggregator(blockStmt, ctx.countVar);
                return;
            case MAX:
                generateCompareAggregator(blockStmt, ctx, OperatorKind.LESS_THAN);
                return;
            case MIN:
                generateCompareAggregator(blockStmt, ctx, OperatorKind.GREATER_THAN);
                return;
        }
        switch (ctx.resultType.tag) {
            case TypeTags.ARRAY:
                generateArrayAggregator(blockStmt, ctx);
                break;
            case TypeTags.MAP:
                generateMapAggregator(blockStmt, ctx);
                break;
        }
    }

    /**
     * Generate result from aggregator logic.
     *
     * @param blockStmt target
     * @param ctx       current context
     */
    private void generateFinalResult(BLangBlockStmt blockStmt, IterableContext ctx) {
        switch (ctx.getLastOperation().kind) {
            case AVERAGE:
                generateCalculateAverage(blockStmt, ctx);
                break;
            default:
                break;
        }
    }

    /**
     * Generates following.
     *
     * variable = variable + 1;
     *
     * @param blockStmt target
     * @param variable  variable to increment
     */
    private void generateCountAggregator(BLangBlockStmt blockStmt, BLangVariable variable) {
        final DiagnosticPos pos = blockStmt.pos;
        // create count = count + 1;
        final BLangBinaryExpr add = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        add.pos = pos;
        add.type = symTable.intType;
        add.opKind = OperatorKind.ADD;
        add.lhsExpr = createVariableRef(pos, variable.symbol);
        add.rhsExpr = createLiteral(pos, symTable.intType, 1L);
        add.opSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.ADD, symTable.intType,
                symTable.intType);
        final BLangAssignment countAdd = createAssignmentStmt(pos, blockStmt);
        countAdd.varRefs.add(createVariableRef(pos, variable.symbol));
        countAdd.expr = add;
    }

    /**
     * Generates following.
     *
     * result = result + value
     *
     * @param blockStmt target
     * @param ctx       current context
     */
    private void generateSumAggregator(BLangBlockStmt blockStmt, IterableContext ctx) {
        final DiagnosticPos pos = blockStmt.pos;
        final BLangBinaryExpr add = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        add.pos = pos;
        add.type = ctx.resultVar.symbol.type;
        add.opKind = OperatorKind.ADD;
        add.lhsExpr = createVariableRef(pos, ctx.resultVar.symbol);
        add.rhsExpr = createVariableRef(pos, ctx.streamRetVars.get(1).symbol);
        add.opSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.ADD, add.type, add.type);
        final BLangAssignment countAdd = createAssignmentStmt(pos, blockStmt);
        countAdd.varRefs.add(createVariableRef(pos, ctx.resultVar.symbol));
        countAdd.expr = add;
    }

    /**
     * Generates following.
     *
     * result = result (Operator) value ? result : value
     *
     * @param blockStmt target
     * @param ctx       current context
     * @param operator  compare operator
     */
    private void generateCompareAggregator(BLangBlockStmt blockStmt, IterableContext ctx, OperatorKind operator) {
        final DiagnosticPos pos = blockStmt.pos;
        final BLangSimpleVarRef resultVar = createVariableRef(pos, ctx.resultVar.symbol);
        final BLangSimpleVarRef valueVar = createVariableRef(pos, ctx.streamRetVars.get(1).symbol);

        final BLangBinaryExpr compare = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        compare.pos = pos;
        compare.type = ctx.resultVar.symbol.type;
        compare.opKind = operator;
        compare.lhsExpr = resultVar;
        compare.rhsExpr = valueVar;
        compare.opSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(operator, compare.type, compare.type);

        final BLangTernaryExpr ternaryExpr = (BLangTernaryExpr) TreeBuilder.createTernaryExpressionNode();
        ternaryExpr.pos = pos;
        ternaryExpr.expr = compare;
        ternaryExpr.thenExpr = resultVar;
        ternaryExpr.elseExpr = valueVar;
        ternaryExpr.type = compare.type;

        final BLangAssignment countAdd = createAssignmentStmt(pos, blockStmt);
        countAdd.varRefs.add(resultVar);
        countAdd.expr = ternaryExpr;
    }

    /**
     * Generates following.
     *
     * result = result / count
     *
     * @param blockStmt target
     * @param ctx       current context
     */
    private void generateCalculateAverage(BLangBlockStmt blockStmt, IterableContext ctx) {
        final DiagnosticPos pos = blockStmt.pos;
        final BLangBinaryExpr divide = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        divide.pos = pos;
        divide.type = ctx.resultVar.symbol.type;
        divide.opKind = OperatorKind.ADD;
        divide.lhsExpr = createVariableRef(pos, ctx.resultVar.symbol);
        divide.rhsExpr = createVariableRef(pos, ctx.countVar.symbol);
        divide.opSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.DIV, divide.type,
                ctx.countVar.symbol.type);
        final BLangAssignment countAdd = createAssignmentStmt(pos, blockStmt);
        countAdd.varRefs.add(createVariableRef(pos, ctx.resultVar.symbol));
        countAdd.expr = divide;
    }

    /**
     * Generates following.
     *
     * result[count] = value;
     * count = count + 1;
     *
     * @param blockStmt target
     * @param ctx       current context
     */
    private void generateArrayAggregator(BLangBlockStmt blockStmt, IterableContext ctx) {
        final DiagnosticPos pos = blockStmt.pos;
        // create assignment result[count] = value;
        final BLangIndexBasedAccess indexAccessNode = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        indexAccessNode.pos = pos;
        indexAccessNode.indexExpr = createVariableRef(pos, ctx.countVar.symbol);
        indexAccessNode.expr = createVariableRef(pos, ctx.resultVar.symbol);
        indexAccessNode.type = ctx.streamRetVars.get(1).symbol.type;
        final BLangAssignment valueAssign = createAssignmentStmt(pos, blockStmt);
        valueAssign.varRefs.add(indexAccessNode);
        valueAssign.expr = createVariableRef(pos, ctx.streamRetVars.get(1).symbol);

        // create count = count + 1;
        generateCountAggregator(blockStmt, ctx.countVar);
    }

    /**
     * Generates following.
     *
     * result[key] = value;
     *
     * @param blockStmt target
     * @param ctx       current context
     */
    private void generateMapAggregator(BLangBlockStmt blockStmt, IterableContext ctx) {
        final DiagnosticPos pos = blockStmt.pos;
        // create assignment result[key] = value
        final BLangIndexBasedAccess indexAccessNode = (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        indexAccessNode.pos = pos;
        indexAccessNode.indexExpr = createVariableRef(pos, ctx.streamRetVars.get(1).symbol);
        indexAccessNode.expr = createVariableRef(pos, ctx.resultVar.symbol);
        indexAccessNode.type = ctx.streamRetVars.get(2).symbol.type;
        final BLangAssignment valueAssign = createAssignmentStmt(pos, blockStmt);
        valueAssign.varRefs.add(indexAccessNode);
        valueAssign.expr = generateMapValueCastExpr(createVariableRef(pos, ctx.streamRetVars.get(2).symbol));
    }

    /**
     * Generates Operation related code.
     *
     * @param blockStmt target
     * @param operation current operation
     * @param ctx       current context
     */
    private void generateOperationCode(BLangBlockStmt blockStmt, Operation operation, IterableContext ctx) {
        switch (operation.kind) {
            case FOREACH:
                generateForeach(blockStmt, operation);
                break;
            case FILTER:
                generateFilter(blockStmt, operation);
                break;
            case MAP:
                generateMap(blockStmt, operation);
                break;
        }
    }


    /* Lambda based Operation related code generation */

    /**
     * Generates statements for foreach operation.
     *
     * lambda(...)
     *
     * @param blockStmt target
     * @param operation operation instance
     */
    private void generateForeach(BLangBlockStmt blockStmt, Operation operation) {
        final DiagnosticPos pos = operation.pos;
        final BLangExpressionStmt exprStmt = createExpressionStmt(pos, blockStmt);
        exprStmt.expr = createInvocationExpr(pos, (BInvokableSymbol) ((BLangSimpleVarRef) operation.lambda).symbol,
                operation.argVars);
    }

    /**
     * Generates statements for filter operation.
     *
     * if(!lambda(...)){
     * skip = true;
     * return;
     * }
     *
     * @param blockStmt target
     * @param operation operation instance
     */
    private void generateFilter(BLangBlockStmt blockStmt, Operation operation) {
        final DiagnosticPos pos = operation.pos;

        final BLangIf ifNode = createIfStmt(pos, blockStmt);
        final BLangUnaryExpr notExpr = (BLangUnaryExpr) TreeBuilder.createUnaryExpressionNode();
        notExpr.pos = pos;
        notExpr.operator = OperatorKind.NOT;
        notExpr.opSymbol = (BOperatorSymbol) symResolver.resolveUnaryOperator(pos, notExpr.operator,
                symTable.booleanType);
        notExpr.expr = createInvocationExpr(pos, (BInvokableSymbol) ((BLangSimpleVarRef) operation.lambda).symbol,
                operation.argVars);
        notExpr.type = symTable.booleanType;
        ifNode.expr = notExpr;
        ifNode.body = createBlockStmt(pos);

        final BLangReturn returnStmt = createReturnStmt(pos, ifNode.body);
        returnStmt.exprs.add(createLiteral(pos, symTable.booleanType, true));
    }

    /**
     * Generates statements for filter operation.
     *
     * v3,v4 = lambda(v1,v2);
     *
     * @param blockStmt target
     * @param operation operation instance
     */
    private void generateMap(BLangBlockStmt blockStmt, Operation operation) {
        final DiagnosticPos pos = operation.pos;
        final BLangAssignment assignment = createAssignmentStmt(pos, blockStmt);
        assignment.varRefs.addAll(createVariableRefList(operation.pos, operation.retVars));
        assignment.expr = createInvocationExpr(pos, (BInvokableSymbol) ((BLangSimpleVarRef) operation.lambda).symbol,
                operation.argVars);
    }


    /* Util methods to create model nodes */

    private BLangFunction createFunction(DiagnosticPos pos, String name) {
        final BLangFunction bLangFunction = (BLangFunction) TreeBuilder.createFunctionNode();
        final IdentifierNode funcName = createIdentifier(pos, getFunctionName(name));
        bLangFunction.setName(funcName);
        bLangFunction.flagSet = EnumSet.of(Flag.LAMBDA);
        bLangFunction.pos = pos;
        //Create body of the function
        bLangFunction.body = createBlockStmt(pos);
        return bLangFunction;
    }

    private BLangIf createIfStmt(DiagnosticPos pos, BLangBlockStmt target) {
        final BLangIf ifNode = (BLangIf) TreeBuilder.createIfElseStatementNode();
        ifNode.pos = pos;
        target.addStatement(ifNode);
        return ifNode;
    }

    private BLangForeach createForeach(DiagnosticPos pos, BLangBlockStmt target) {
        final BLangForeach foreach = (BLangForeach) TreeBuilder.createForeachNode();
        foreach.pos = pos;
        target.addStatement(foreach);
        return foreach;
    }

    private BLangVariableDef createVariableDefStmt(DiagnosticPos pos, BLangBlockStmt target) {
        final BLangVariableDef variableDef = (BLangVariableDef) TreeBuilder.createVariableDefinitionNode();
        variableDef.pos = pos;
        target.addStatement(variableDef);
        return variableDef;
    }

    private BLangAssignment createAssignmentStmt(DiagnosticPos pos, BLangBlockStmt target) {
        final BLangAssignment assignment = (BLangAssignment) TreeBuilder.createAssignmentNode();
        assignment.pos = pos;
        target.addStatement(assignment);
        return assignment;
    }

    private BLangExpressionStmt createExpressionStmt(DiagnosticPos pos, BLangBlockStmt target) {
        final BLangExpressionStmt exprStmt = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
        exprStmt.pos = pos;
        target.addStatement(exprStmt);
        return exprStmt;
    }

    private BLangReturn createReturnStmt(DiagnosticPos pos, BLangBlockStmt target) {
        final BLangReturn returnStmt = (BLangReturn) TreeBuilder.createReturnNode();
        returnStmt.pos = pos;
        target.addStatement(returnStmt);
        return returnStmt;
    }

    private void createNextStmt(DiagnosticPos pos, BLangBlockStmt target) {
        final BLangNext nextStmt = (BLangNext) TreeBuilder.createNextNode();
        nextStmt.pos = pos;
        target.addStatement(nextStmt);
    }

    private BLangBlockStmt createBlockStmt(DiagnosticPos pos) {
        final BLangBlockStmt blockNode = (BLangBlockStmt) TreeBuilder.createBlockNode();
        blockNode.pos = pos;
        return blockNode;
    }

    private BLangExpression generateMapValueCastExpr(BLangSimpleVarRef varRef) {
        if (varRef.type.tag > TypeTags.TYPE) {
            return varRef;
        }
        // Box value using cast expression.
        final BLangTypeCastExpr implicitCastExpr = (BLangTypeCastExpr) TreeBuilder.createTypeCastNode();
        implicitCastExpr.pos = varRef.pos;
        implicitCastExpr.expr = varRef;
        implicitCastExpr.type = symTable.anyType;
        implicitCastExpr.types = Lists.of(symTable.anyType);
        implicitCastExpr.castSymbol = (BOperatorSymbol) symResolver.resolveImplicitCastOperator(
                varRef.type, symTable.anyType);
        return implicitCastExpr;
    }

    private BLangInvocation createInvocationExpr(DiagnosticPos pos, BInvokableSymbol invokableSymbol,
                                                 List<BLangVariable> args) {
        final BLangInvocation invokeLambda = (BLangInvocation) TreeBuilder.createInvocationNode();
        invokeLambda.pos = pos;
        invokeLambda.argExprs.addAll(createVariableRefList(pos, args));
        invokeLambda.symbol = invokableSymbol;
        invokeLambda.types.addAll(((BInvokableType) invokableSymbol.type).retTypes);
        return invokeLambda;
    }

    private List<BLangSimpleVarRef> createVariableRefList(DiagnosticPos pos, List<BLangVariable> args) {
        final List<BLangSimpleVarRef> varRefs = new ArrayList<>();
        args.forEach(variable -> varRefs.add(createVariableRef(pos, variable.symbol)));
        return varRefs;
    }

    private BLangSimpleVarRef createVariableRef(DiagnosticPos pos, BVarSymbol variable) {
        final BLangSimpleVarRef varRef = (BLangSimpleVarRef) TreeBuilder.createSimpleVariableReferenceNode();
        varRef.pos = pos;
        varRef.variableName = createIdentifier(pos, variable.name.value);
        varRef.symbol = variable;
        varRef.type = variable.type;
        return varRef;
    }

    private BLangVariable createVariable(DiagnosticPos pos, String name, BType type) {
        final BLangVariable varNode = (BLangVariable) TreeBuilder.createVariableNode();
        varNode.setName(createIdentifier(pos, name));
        varNode.type = type;
        varNode.pos = pos;
        return varNode;
    }

    private BLangLiteral createLiteral(DiagnosticPos pos, BType type, Object value) {
        final BLangLiteral literal = (BLangLiteral) TreeBuilder.createLiteralExpression();
        literal.pos = pos;
        literal.value = value;
        literal.typeTag = type.tag;
        literal.type = type;
        return literal;
    }

    private BLangIdentifier createIdentifier(DiagnosticPos pos, String value) {
        final BLangIdentifier node = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        node.pos = pos;
        if (value != null) {
            node.setValue(value);
        }
        return node;
    }

    private String getFunctionName(String name) {
        return name + lambdaFunctionCount++;
    }

    private List<BLangVariable> copyOf(List<BLangVariable> variables) {
        List<BLangVariable> copy = new ArrayList<>();
        variables.forEach(variable -> copy.add(createVariable(variable.pos, variable.name.value, variable.type)));
        return copy;
    }
}

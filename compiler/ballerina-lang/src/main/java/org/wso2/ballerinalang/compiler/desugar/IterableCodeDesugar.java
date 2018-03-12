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
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.NodeKind;
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
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTernaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangUnaryExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangReturn;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createAssignmentStmt;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createBlockStmt;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createExpressionStmt;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createForeach;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createFunction;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createIfStmt;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createInvocationExpr;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createLiteral;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createNextStmt;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createReturnStmt;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createVariable;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createVariableDefStmt;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createVariableRef;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.createVariableRefList;
import static org.wso2.ballerinalang.compiler.desugar.ASTBuilderUtil.generateCastExpr;

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
    private static final String COPY_OF = "copy";
    private static final String EMPTY = "";

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
        final BLangInvocation iExpr = createInvocationExpr(ctx.collectionExpr.pos,
                ctx.iteratorFuncSymbol, Collections.emptyList(), symResolver);
        iExpr.requiredArgs.add(ctx.collectionExpr);
        if (ctx.getLastOperation().expectedTypes.isEmpty() ||
                ctx.getLastOperation().expectedTypes.get(0) == symTable.noType) {
            ctx.iteratorCaller = iExpr;
        } else {
            ctx.iteratorCaller = generateCastExpr(iExpr, ctx.getLastOperation().expectedTypes.get(0), symResolver);
        }
    }

    private void rewrite(IterableContext ctx) {
        variableCount = 0;
        ctx.operations.forEach(this::rewrite);
        ctx.collectionExpr = ctx.getFirstOperation().iExpr.expr;
    }

    private void rewrite(Operation operation) {
        if (operation.iExpr.requiredArgs.size() > 0) {
            final BLangExpression langExpression = operation.iExpr.requiredArgs.get(0);
            if (langExpression.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                operation.lambdaSymbol = (BInvokableSymbol) ((BLangSimpleVarRef) langExpression).symbol;
            } else if (langExpression.getKind() == NodeKind.LAMBDA) {
                operation.lambdaSymbol = ((BLangLambdaFunction) langExpression).function.symbol;
            }
        }
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
        final DiagnosticPos pos = firstOperation.pos;

        // Create and define function signature.
        final BLangFunction funcNode = createFunction(pos, getFunctionName(FUNC_CALLER));
        funcNode.requiredParams.add(createVariable(pos, VAR_COLLECTION, ctx.collectionExpr.type));
        if (isReturningIteratorFunction(ctx)) {
            funcNode.retParams.add(ctx.resultVar = createVariable(pos, VAR_RESULT, ctx.resultType));
        }

        final BPackageSymbol packageSymbol = firstOperation.env.enclPkg.symbol;
        final SymbolEnv packageEnv = this.symTable.pkgEnvMap.get(packageSymbol);
        symbolEnter.defineNode(funcNode, packageEnv);
        ctx.iteratorFuncSymbol = funcNode.symbol;
        packageEnv.enclPkg.functions.add(funcNode);
        packageEnv.enclPkg.topLevelNodes.add(funcNode);

        LinkedList<Operation> streamOperations = new LinkedList<>();
        ctx.operations.stream().filter(op -> op.kind.isLambdaRequired()).forEach(streamOperations::add);
        if (streamOperations.isEmpty()) {
            // Generate simple iterator function body.
            generateSimpleIteratorBlock(ctx, funcNode);
            return;
        }
        generateStreamingIteratorBlock(ctx, funcNode, streamOperations);
    }

    private void generateSimpleIteratorBlock(IterableContext ctx, BLangFunction funcNode) {
        final Operation firstOperation = ctx.getFirstOperation();
        final DiagnosticPos pos = firstOperation.pos;
        if (isReturningIteratorFunction(ctx)) {
            generateCounterVariable(funcNode.body, ctx, funcNode);
            generateResultVariable(funcNode.body, ctx, ctx.resultVar);
        }
        // Create variables required.
        final List<BLangVariable> foreachVars = copyOf(ctx.getFirstOperation().argVars, COPY_OF);
        ctx.streamRetVars = new ArrayList<>();
        ctx.streamRetVars.add(ctx.skipVar = createVariable(pos, VAR_SKIP, symTable.booleanType));   // Not used.
        ctx.streamRetVars.addAll(foreachVars);

        final BLangForeach foreach = createForeach(pos, funcNode.body);
        foreachVars.forEach(variable -> defineVariable(variable, firstOperation.env.enclPkg.symbol.pkgID, funcNode));
        foreach.varRefs.addAll(createVariableRefList(pos, foreachVars));
        foreach.collection = createVariableRef(pos, funcNode.requiredParams.get(0).symbol);
        foreach.varTypes = firstOperation.argTypes;
        foreach.body = createBlockStmt(pos);

        if (isReturningIteratorFunction(ctx)) {
            generateAggregator(foreach.body, ctx);
            generateFinalResult(funcNode.body, ctx);
        }
        final BLangReturn returnStmt = createReturnStmt(firstOperation.pos, funcNode.body);
        if (isReturningIteratorFunction(ctx)) {
            returnStmt.addExpression(createVariableRef(pos, ctx.resultVar.symbol));
        }
    }

    private void generateStreamingIteratorBlock(IterableContext ctx, BLangFunction funcNode,
                                                LinkedList<Operation> streamOperations) {
        final Operation firstOperation = ctx.getFirstOperation();
        final DiagnosticPos pos = firstOperation.pos;

        // Generate streaming based function Body.
        if (isReturningIteratorFunction(ctx)) {
            generateCounterVariable(funcNode.body, ctx, funcNode);
            generateResultVariable(funcNode.body, ctx, ctx.resultVar);
        }
        // create and define required variables.
        generateVarDefForStream(funcNode.body, ctx, funcNode, streamOperations);

        // Generate foreach iteration.
        final BLangForeach foreach = createForeach(pos, funcNode.body);
        final List<BLangVariable> foreachVars = copyOf(ctx.getFirstOperation().argVars, COPY_OF);
        foreachVars.forEach(variable -> defineVariable(variable, firstOperation.env.enclPkg.symbol.pkgID, funcNode));
        foreach.varRefs.addAll(createVariableRefList(pos, foreachVars));
        foreach.collection = createVariableRef(pos, funcNode.requiredParams.get(0).symbol);
        foreach.varTypes = firstOperation.argTypes;
        foreach.body = createBlockStmt(pos);

        // Call Stream function and its assignment.
        generateStreamFunction(ctx, streamOperations);
        final BLangInvocation iExpr = createInvocationExpr(pos, ctx.streamFuncSymbol, foreachVars, symResolver);
        BLangAssignment assignment = createAssignmentStmt(pos, foreach.body);
        assignment.expr = iExpr;
        assignment.varRefs.addAll(createVariableRefList(pos, ctx.streamRetVars));

        // Generate aggregator and result
        if (isReturningIteratorFunction(ctx)) {
            generateNextCondition(foreach.body, ctx);
            generateAggregator(foreach.body, ctx);
            generateFinalResult(funcNode.body, ctx);
        }

        final BLangReturn returnStmt = createReturnStmt(firstOperation.pos, funcNode.body);
        if (isReturningIteratorFunction(ctx)) {
            returnStmt.addExpression(createVariableRef(pos, ctx.resultVar.symbol));
        }
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
        final IterableKind kind = ctx.getLastOperation().kind;
        if (ctx.resultType.tag != TypeTags.ARRAY
                && ctx.resultType.tag != TypeTags.MAP
                && kind != IterableKind.MAX
                && kind != IterableKind.MIN) {
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
                assignment.expr = ASTBuilderUtil.createEmptyRecordLiteral(pos, ctx.resultType);
                break;
            case TypeTags.INT:
                if (kind == IterableKind.MAX) {
                    assignment.expr = createLiteral(pos, symTable.intType, Long.MIN_VALUE);
                } else if (kind == IterableKind.MIN) {
                    assignment.expr = createLiteral(pos, symTable.intType, Long.MAX_VALUE);
                }
                break;
            case TypeTags.FLOAT:
                if (kind == IterableKind.MAX) {
                    assignment.expr = createLiteral(pos, symTable.floatType, Double.MIN_NORMAL);
                } else if (kind == IterableKind.MIN) {
                    assignment.expr = createLiteral(pos, symTable.floatType, Double.MAX_VALUE);
                }
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
     * @param blockStmt        target
     * @param ctx              current context
     * @param funcNode         functionNode
     * @param streamOperations streaming operation list
     */
    private void generateVarDefForStream(BLangBlockStmt blockStmt, IterableContext ctx, BLangFunction funcNode,
                                         LinkedList<Operation> streamOperations) {
        ctx.streamRetVars = new ArrayList<>();
        ctx.streamRetVars.add(ctx.skipVar = createVariable(blockStmt.pos, VAR_SKIP, symTable.booleanType));
        ctx.streamRetVars.addAll(copyOf(getStreamFunctionVariableList(streamOperations), EMPTY));
        ctx.streamRetVars.forEach(variable -> {
            defineVariable(variable, funcNode.symbol.pkgID, funcNode);
            createVariableDefStmt(blockStmt.pos, funcNode.body).var = variable;
        });
    }

    private List<BLangVariable> getStreamFunctionVariableList(LinkedList<Operation> streamOperations) {
        List<BLangVariable> retVars = streamOperations.getLast().retVars;
        Operation lastOperation = streamOperations.getLast();
        while (lastOperation.kind == IterableKind.FILTER) {
            if (lastOperation.previous == null) {
                retVars = lastOperation.argVars;
                break;
            }
            lastOperation = lastOperation.previous;
            retVars = lastOperation.retVars;
        }
        return retVars;
    }

    /**
     * Generate Stream function from operation chain.
     *
     * @param ctx              current context
     * @param streamOperations streaming operation list
     */
    private void generateStreamFunction(IterableContext ctx, LinkedList<Operation> streamOperations) {
        final DiagnosticPos pos = ctx.getFirstOperation().pos;
        List<BLangVariable> retVars = getStreamFunctionVariableList(streamOperations);
        List<BLangVariable> retParmVars = retVars.stream()
                .map(variable -> copyOf(variable, EMPTY))
                .collect(Collectors.toList());

        // Create and define function signature.
        final BLangFunction funcNode = createFunction(pos, getFunctionName(FUNC_STREAM));
        funcNode.requiredParams.addAll(ctx.getFirstOperation().argVars);
        funcNode.retParams.add(createVariable(pos, EMPTY, symTable.booleanType));
        funcNode.retParams.addAll(retParmVars);

        final BPackageSymbol packageSymbol = ctx.getFirstOperation().env.enclPkg.symbol;
        final SymbolEnv packageEnv = this.symTable.pkgEnvMap.get(packageSymbol);
        symbolEnter.defineNode(funcNode, packageEnv);
        ctx.streamFuncSymbol = funcNode.symbol;
        packageEnv.enclPkg.functions.add(funcNode);
        packageEnv.enclPkg.topLevelNodes.add(funcNode);

        // Define all undefined variables.
        Set<BLangVariable> unusedVars = new HashSet<>();
        streamOperations.forEach(operation -> {
            unusedVars.addAll(operation.argVars);
            if (operation.kind != IterableKind.FILTER) {
                unusedVars.addAll(operation.retVars);
            }
        });
        unusedVars.removeAll(funcNode.requiredParams);
        unusedVars.removeAll(funcNode.retParams);
        unusedVars.forEach(variable -> defineVariable(variable, packageSymbol.pkgID, funcNode));
        unusedVars.forEach(variable -> {
            BLangVariableDef variableDefStmt = createVariableDefStmt(pos, funcNode.body);
            variableDefStmt.var = variable;
        });
        // Generate function Body.
        ctx.operations.forEach(operation -> generateOperationCode(funcNode.body, operation));
        generateStreamReturnStmt(funcNode.body, retVars);
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
        final DiagnosticPos pos = blockStmt.pos;
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
        switch (ctx.resultType.tag) {
            case TypeTags.ARRAY:
                generateArrayAggregator(blockStmt, ctx);
                return;
            case TypeTags.MAP:
                generateMapAggregator(blockStmt, ctx);
                return;
        }
        generateCountAggregator(blockStmt, ctx.countVar);
        switch (ctx.getLastOperation().kind) {
            case COUNT:
                generateCountAggregator(blockStmt, ctx.resultVar);
                break;
            case SUM:
                generateSumAggregator(blockStmt, ctx);
                break;
            case AVERAGE:
                generateSumAggregator(blockStmt, ctx);
                break;
            case MAX:
                generateCompareAggregator(blockStmt, ctx, OperatorKind.GREATER_THAN);
                break;
            case MIN:
                generateCompareAggregator(blockStmt, ctx, OperatorKind.LESS_THAN);
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
        compare.type = symTable.booleanType;
        compare.opKind = operator;
        compare.lhsExpr = resultVar;
        compare.rhsExpr = valueVar;
        compare.opSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(operator, resultVar.symbol.type,
                valueVar.symbol.type);

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
        valueAssign.expr = generateCastExpr(createVariableRef(pos, ctx.streamRetVars.get(2).symbol), symTable.anyType,
                symResolver);
    }

    /**
     * Generate result from aggregator logic.
     *
     * @param blockStmt target
     * @param ctx       current context
     */
    private void generateFinalResult(BLangBlockStmt blockStmt, IterableContext ctx) {
        generateDefaultIfEmpty(blockStmt, ctx);
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
     * if(count == 0){
     * return;
     * }
     *
     * @param blockStmt target
     * @param ctx       current context
     */
    private void generateDefaultIfEmpty(BLangBlockStmt blockStmt, IterableContext ctx) {
        if (ctx.resultVar.symbol.type.tag > TypeTags.TYPE) {
            return;
        }
        final DiagnosticPos pos = blockStmt.pos;
        final BLangBinaryExpr equality = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        equality.pos = pos;
        equality.type = symTable.booleanType;
        equality.opKind = OperatorKind.EQUAL;
        equality.lhsExpr = createVariableRef(pos, ctx.countVar.symbol);
        equality.rhsExpr = createLiteral(pos, symTable.intType, 0L);
        equality.opSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.EQUAL, symTable.intType,
                symTable.intType);

        final BLangIf ifNode = createIfStmt(pos, blockStmt);
        ifNode.expr = equality;
        ifNode.body = createBlockStmt(pos);
        if (ctx.resultVar.symbol.type.tag <= TypeTags.FLOAT) {
            final BLangAssignment assign = createAssignmentStmt(pos, ifNode.body);
            assign.varRefs.add(createVariableRef(pos, ctx.resultVar.symbol));
            switch (ctx.resultVar.symbol.type.tag) {
                case TypeTags.INT:
                    assign.expr = createLiteral(pos, symTable.intType, 0L);
                    break;
                case TypeTags.FLOAT:
                    assign.expr = createLiteral(pos, symTable.floatType, 0D);
                    break;
            }
        }
        createReturnStmt(pos, ifNode.body);
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


    /* Lambda based Operation related code generation */

    /**
     * Generates Operation related code.
     *
     * @param blockStmt target
     * @param operation current operation
     */
    private void generateOperationCode(BLangBlockStmt blockStmt, Operation operation) {
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
        exprStmt.expr = createInvocationExpr(pos, operation.lambdaSymbol, operation.argVars, symResolver);
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
        notExpr.expr = createInvocationExpr(pos, operation.lambdaSymbol, operation.argVars, symResolver);
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
        assignment.expr = createInvocationExpr(pos, operation.lambdaSymbol, operation.argVars, symResolver);
    }

    private String getFunctionName(String name) {
        return name + lambdaFunctionCount++;
    }

    private List<BLangVariable> copyOf(List<BLangVariable> variables, String postFix) {
        List<BLangVariable> copy = new ArrayList<>();
        variables.forEach(variable -> copy.add(copyOf(variable, variable.name.value + postFix)));
        return copy;
    }

    private BLangVariable copyOf(BLangVariable variable, String name) {
        return createVariable(variable.pos, name, variable.type);
    }
}

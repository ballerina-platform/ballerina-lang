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
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.iterable.IterableContext;
import org.wso2.ballerinalang.compiler.semantics.model.iterable.IterableKind;
import org.wso2.ballerinalang.compiler.semantics.model.iterable.Operation;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BOperatorSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BTupleType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangArrayLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBracedOrTupleExpr;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangTupleDestructure;
import org.wso2.ballerinalang.compiler.tree.statements.BLangVariableDef;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Class responsible for desugar an iterable chain into actual Ballerina code.
 *
 * @since 0.961.0
 */
public class IterableCodeDesugar {

    private static final String FUNC_CALLER = "$lambda$iterable";
    private static final String VAR_ARG = "arg";
    private static final String VAR_FOREACH_VAL = "val";
    private static final String VAR_RESULT_VAL = "val";
    private static final String VAR_RESULT = "result";
    private static final String VAR_COUNT = "count";
    private static final String VAR_COLLECTION = "collection";
    private static final String TABLE_CONFIG = "TableConfig";

    private static final CompilerContext.Key<IterableCodeDesugar> ITERABLE_DESUGAR_KEY =
            new CompilerContext.Key<>();

    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final SymbolEnter symbolEnter;
    private final Names names;
    private final Types types;

    private int lambdaFunctionCount = 0;
    private int variableCount = 0;

    private static final String TABLE_ADD_FUNCTION = "table.add";

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
        this.types = Types.getInstance(context);
    }

    public void desugar(IterableContext ctx) {
        // Gather required data for code generation.
        processIterableContext(ctx);
        // Generate Iterable Iteration.
        generateIteratorFunction(ctx);
        // Create invocation expression to invoke iterable operation.
        final BLangInvocation iExpr = ASTBuilderUtil.createInvocationExpr(ctx.collectionExpr.pos,
                ctx.iteratorFuncSymbol, Collections.emptyList(), symResolver);
        iExpr.requiredArgs.add(ctx.collectionExpr);
        if (ctx.getLastOperation().expectedType == symTable.noType
                || ctx.getLastOperation().expectedType == symTable.nilType) {
            ctx.iteratorCaller = iExpr;
        } else {
            ctx.iteratorCaller = ASTBuilderUtil.wrapToConversionExpr(ctx.getLastOperation().expectedType, iExpr,
                    symTable, types);
        }
    }

    private void processIterableContext(IterableContext ctx) {
        variableCount = 0;
        ctx.operations.forEach(this::processOperation);
        ctx.collectionExpr = ctx.getFirstOperation().iExpr.expr;
        ctx.collectionVar = ASTBuilderUtil.createVariable(ctx.getFirstOperation().pos, VAR_COLLECTION,
                ctx.collectionExpr.type);
        ctx.resultVar = ASTBuilderUtil.createVariable(ctx.getFirstOperation().pos, VAR_RESULT, ctx.resultType);
    }

    private void processOperation(Operation operation) {
        if (operation.iExpr.requiredArgs.size() > 0) {
            // Get Lambda function symbol, for lambda based operations.
            final BLangExpression lambdaExpression = operation.iExpr.requiredArgs.get(0);
            if (lambdaExpression.getKind() == NodeKind.SIMPLE_VARIABLE_REF) {
                operation.lambdaSymbol = (BInvokableSymbol) ((BLangSimpleVarRef) lambdaExpression).symbol;
            } else if (lambdaExpression.getKind() == NodeKind.LAMBDA) {
                operation.lambdaSymbol = ((BLangLambdaFunction) lambdaExpression).function.symbol;
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
            final String varName = VAR_ARG + variableCount++;
            operation.argVar = ASTBuilderUtil.createVariable(operation.pos, varName, operation.inputType);
        } else {
            Operation lastOperation = operation.previous;
            if (lastOperation.kind == IterableKind.FILTER) {
                operation.argVar = lastOperation.argVar;
            } else {
                operation.argVar = lastOperation.retVar;
            }
        }
        if (operation.kind == IterableKind.FILTER) {
            operation.retVar = operation.argVar;
            return;
        } else if (operation.kind == IterableKind.FOREACH) {
            return;
        }
        // Add output types.
        final String varName = VAR_ARG + variableCount++;
        operation.retVar = ASTBuilderUtil.createVariable(operation.pos, varName, operation.outputType);
    }

    private void generateIteratorFunction(IterableContext ctx) {
        final Operation firstOperation = ctx.getFirstOperation();
        final DiagnosticPos pos = firstOperation.pos;

        // Create and define function signature.
        final BLangFunction funcNode = ASTBuilderUtil.createFunction(pos, getFunctionName(FUNC_CALLER));
        funcNode.requiredParams.add(ctx.collectionVar);
        final BType returnType;
        if (isReturningIteratorFunction(ctx)) {
            returnType = ctx.resultType;
        } else {
            returnType = symTable.nilType;
        }
        funcNode.returnTypeNode = ASTBuilderUtil.createTypeNode(returnType);
        funcNode.desugaredReturnType = true;

        defineFunction(funcNode, ctx.env.enclPkg);
        ctx.iteratorFuncSymbol = funcNode.symbol;

        LinkedList<Operation> streamableOperations = new LinkedList<>();
        ctx.operations.stream().filter(op -> op.kind.isLambdaRequired()).forEach(streamableOperations::add);
        if (streamableOperations.isEmpty()) {
            // Generate simple iterator function body.
            generateSimpleIteratorBlock(ctx, funcNode);
            return;
        }
        // Generate Caller Function.
        generateStreamingIteratorBlock(ctx, funcNode, streamableOperations);
    }

    private void defineFunction(BLangFunction funcNode, BLangPackage targetPkg) {
        final BPackageSymbol packageSymbol = targetPkg.symbol;
        final SymbolEnv packageEnv = this.symTable.pkgEnvMap.get(packageSymbol);
        symbolEnter.defineNode(funcNode, packageEnv);
        packageEnv.enclPkg.functions.add(funcNode);
        packageEnv.enclPkg.topLevelNodes.add(funcNode);
    }

    private void generateSimpleIteratorBlock(IterableContext ctx, BLangFunction funcNode) {
        final Operation firstOperation = ctx.getFirstOperation();
        final DiagnosticPos pos = firstOperation.pos;
        //  ? var count = 0;
        //  ? var result = default_value;
        //  foreach a in collection {
        //      count += 1;
        //  }
        //  return result;
        if (isReturningIteratorFunction(ctx)) {
            createCounterVarDefStmt(funcNode, ctx);
            createResultVarDefStmt(funcNode, ctx);
        }
        // Create variables required.
        final List<BLangVariable> foreachVariables = createForeachVariables(ctx, ctx.getFirstOperation().argVar,
                funcNode);
        ctx.iteratorResultVariables = foreachVariables;

        final BLangForeach foreachStmt = ASTBuilderUtil.createForeach(pos,
                funcNode.body,
                ASTBuilderUtil.createVariableRef(pos, ctx.collectionVar.symbol),
                ASTBuilderUtil.createVariableRefList(pos, foreachVariables),
                ctx.foreachTypes);
        // foreach variable are the result variables.

        if (isReturningIteratorFunction(ctx)) {
            generateAggregator(foreachStmt.body, ctx);
            generateFinalResult(funcNode.body, ctx);
        }
        final BLangReturn returnStmt = ASTBuilderUtil.createReturnStmt(firstOperation.pos, funcNode.body);
        if (isReturningIteratorFunction(ctx)) {
            returnStmt.expr = ASTBuilderUtil.createVariableRef(pos, ctx.resultVar.symbol);
        } else {
            returnStmt.expr = ASTBuilderUtil.createLiteral(pos, symTable.nilType, Names.NIL_VALUE);
        }
    }

    private void generateStreamingIteratorBlock(IterableContext ctx, BLangFunction funcNode,
                                                LinkedList<Operation> streamOperations) {
        final Operation firstOperation = ctx.getFirstOperation();
        final DiagnosticPos pos = firstOperation.pos;

        // Generate streaming based function Body.
        if (isReturningIteratorFunction(ctx)) {
            if (ctx.resultType.tag != TypeTags.TABLE) {
                // This should be the select operation. No need of a count variable.
                createCounterVarDefStmt(funcNode, ctx);
            }
            createResultVarDefStmt(funcNode, ctx);
            ctx.iteratorResultVariables = createIteratorResultVariables(ctx, streamOperations.getLast().retVar,
                    funcNode);
        } else {
            ctx.iteratorResultVariables = Collections.emptyList();
        }

        // Create variables required.
        final List<BLangVariable> foreachVariables = createForeachVariables(ctx, ctx.getFirstOperation().argVar,
                funcNode);

        // Define all undefined variables.
        defineRequiredVariables(ctx, streamOperations, foreachVariables, funcNode);

        // Generate foreach iteration.
        final BLangForeach foreachStmt = ASTBuilderUtil.createForeach(pos,
                funcNode.body,
                ASTBuilderUtil.createVariableRef(pos, ctx.collectionVar.symbol),
                ASTBuilderUtil.createVariableRefList(pos, foreachVariables),
                ctx.foreachTypes);

        if (foreachVariables.size() > 1) {
            // Create tuple, for lambda invocation.
            final BLangAssignment assignmentStmt = ASTBuilderUtil.createAssignmentStmt(pos, foreachStmt.body);
            assignmentStmt.declaredWithVar = true;
            assignmentStmt.varRef = ASTBuilderUtil.createVariableRef(pos, ctx.getFirstOperation().argVar.symbol);

            final BLangBracedOrTupleExpr tupleExpr = (BLangBracedOrTupleExpr) TreeBuilder
                    .createBracedOrTupleExpression();
            for (BLangVariable foreachVariable : foreachVariables) {
                tupleExpr.expressions.add(ASTBuilderUtil.createVariableRef(pos, foreachVariable.symbol));
            }
            tupleExpr.isBracedExpr = foreachVariables.size() == 1;
            tupleExpr.type = new BTupleType(getTupleTypeList(ctx.getFirstOperation().inputType));
            assignmentStmt.expr = tupleExpr;
        }

        // Generate Operations related
        ctx.operations.forEach(operation -> generateOperationCode(foreachStmt.body, operation));

        // Generate aggregator and result
        if (isReturningIteratorFunction(ctx)) {
            if (ctx.iteratorResultVariables.size() > 1) {
                // Destructure return Values.
                final BLangTupleDestructure tupleAssign = (BLangTupleDestructure) TreeBuilder
                        .createTupleDestructureStatementNode();
                tupleAssign.pos = pos;
                tupleAssign.declaredWithVar = true;
                foreachStmt.body.addStatement(tupleAssign);
                tupleAssign.expr = ASTBuilderUtil.createVariableRef(pos, ctx.getLastOperation().retVar.symbol);
                tupleAssign.varRefs.addAll(ASTBuilderUtil.createVariableRefList(pos, ctx.iteratorResultVariables));
            }
            generateAggregator(foreachStmt.body, ctx);
            generateFinalResult(funcNode.body, ctx);
        }

        final BLangReturn returnStmt = ASTBuilderUtil.createReturnStmt(firstOperation.pos, funcNode.body);
        if (isReturningIteratorFunction(ctx)) {
            returnStmt.expr = ASTBuilderUtil.createVariableRef(pos, ctx.resultVar.symbol);
        } else {
            returnStmt.expr = ASTBuilderUtil.createLiteral(pos, symTable.nilType, Names.NIL_VALUE);
        }
    }

    private void defineRequiredVariables(IterableContext ctx,
                                         LinkedList<Operation> streamOperations,
                                         List<BLangVariable> foreachVariables,
                                         BLangFunction funcNode) {
        Set<BLangVariable> notDefinedVars = new HashSet<>();
        streamOperations.forEach(operation -> {
            notDefinedVars.add(operation.argVar);
            if (operation.kind != IterableKind.FILTER && operation.retVar != null) {
                notDefinedVars.add(operation.retVar);
            }
        });
        notDefinedVars.addAll(ctx.iteratorResultVariables);
        notDefinedVars.removeAll(foreachVariables);
        notDefinedVars.forEach(var -> defineVariable(var, ctx.env.enclPkg.symbol.pkgID, funcNode));
        notDefinedVars.forEach(var -> {
            BLangVariableDef variableDefStmt = ASTBuilderUtil.createVariableDefStmt(funcNode.pos, funcNode.body);
            variableDefStmt.var = var;
        });
    }

    private boolean isReturningIteratorFunction(IterableContext ctx) {
        return !(ctx.resultType == symTable.noType || ctx.resultType == symTable.nilType);
    }

    /**
     * Generates following.
     *
     * int count = 0;
     *
     * @param funcNode functionNode
     * @param ctx      current context
     */
    private void createCounterVarDefStmt(BLangFunction funcNode, IterableContext ctx) {
        BLangBlockStmt blockStmt = funcNode.body;
        final DiagnosticPos pos = blockStmt.pos;
        ctx.countVar = ASTBuilderUtil.createVariable(pos, VAR_COUNT, symTable.intType);
        ctx.countVar.expr = ASTBuilderUtil.createLiteral(pos, symTable.intType, 0L);
        defineVariable(ctx.countVar, funcNode.symbol.pkgID, funcNode);
        final BLangVariableDef variableDefStmt = ASTBuilderUtil.createVariableDefStmt(pos, blockStmt);
        variableDefStmt.var = ctx.countVar;
    }

    /**
     * Generates following.
     *
     * array:   result =[];
     * map:     result = {};
     *
     * @param funcNode functionNode
     * @param ctx      current context
     */
    private void createResultVarDefStmt(BLangFunction funcNode, IterableContext ctx) {
        BLangBlockStmt blockStmt = funcNode.body;
        final IterableKind kind = ctx.getLastOperation().kind;
//        if (ctx.resultType.tag != TypeTags.ARRAY
//                && ctx.resultType.tag != TypeTags.MAP
//                && ctx.resultType.tag != TypeTags.TABLE
//                && kind != IterableKind.MAX
//                && kind != IterableKind.MIN) {
//            return;
//        }
        defineVariable(ctx.resultVar, funcNode.symbol.pkgID, funcNode);
        final DiagnosticPos pos = blockStmt.pos;
        final BLangVariableDef defStmt = ASTBuilderUtil.createVariableDefStmt(pos, blockStmt);
        defStmt.var = ctx.resultVar;
        switch (ctx.resultType.tag) {
            case TypeTags.ARRAY:
                final BLangArrayLiteral arrayInit = (BLangArrayLiteral) TreeBuilder.createArrayLiteralNode();
                arrayInit.pos = pos;
                arrayInit.exprs = new ArrayList<>();
                arrayInit.type = ctx.resultType;
                defStmt.var.expr = arrayInit;
                break;
            case TypeTags.MAP:
                defStmt.var.expr = ASTBuilderUtil.createEmptyRecordLiteral(pos, ctx.resultType);
                break;
            case TypeTags.TABLE:
                BLangVariable retVars = ctx.getFirstOperation().retVar;
                BType tableType = new BTableType(TypeTags.TABLE, retVars.type, symTable.tableType.tsymbol);
                BType tableConfigType = symTable.rootScope.lookup(new Name(TABLE_CONFIG)).symbol.type;
                defStmt.var.expr = ASTBuilderUtil.createEmptyTableLiteral(pos, tableType, tableConfigType);
                break;
            case TypeTags.INT:
                if (kind == IterableKind.MAX) {
                    defStmt.var.expr = ASTBuilderUtil.createLiteral(pos, symTable.intType, Long.MIN_VALUE);
                } else if (kind == IterableKind.MIN) {
                    defStmt.var.expr = ASTBuilderUtil.createLiteral(pos, symTable.intType, Long.MAX_VALUE);
                }
                break;
            case TypeTags.FLOAT:
                if (kind == IterableKind.MAX) {
                    defStmt.var.expr = ASTBuilderUtil.createLiteral(pos, symTable.floatType, Double.MIN_NORMAL);
                } else if (kind == IterableKind.MIN) {
                    defStmt.var.expr = ASTBuilderUtil.createLiteral(pos, symTable.floatType, Double.MAX_VALUE);
                }
                break;
        }
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
            case TypeTags.TABLE:
                generateTableAggregator(blockStmt, ctx);
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
        add.lhsExpr = ASTBuilderUtil.createVariableRef(pos, variable.symbol);
        add.rhsExpr = ASTBuilderUtil.createLiteral(pos, symTable.intType, 1L);
        add.opSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.ADD, symTable.intType,
                symTable.intType);
        final BLangAssignment countAdd = ASTBuilderUtil.createAssignmentStmt(pos, blockStmt);
        countAdd.varRef = ASTBuilderUtil.createVariableRef(pos, variable.symbol);
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
        add.lhsExpr = ASTBuilderUtil.createVariableRef(pos, ctx.resultVar.symbol);
        add.rhsExpr = ASTBuilderUtil.createVariableRef(pos, ctx.iteratorResultVariables.get(0).symbol);
        add.opSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.ADD, add.type, add.type);
        final BLangAssignment countAdd = ASTBuilderUtil.createAssignmentStmt(pos, blockStmt);
        countAdd.varRef = ASTBuilderUtil.createVariableRef(pos, ctx.resultVar.symbol);
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
        final BLangSimpleVarRef resultVar = ASTBuilderUtil.createVariableRef(pos, ctx.resultVar.symbol);
        final BLangSimpleVarRef valueVar = ASTBuilderUtil.createVariableRef(pos, ctx.iteratorResultVariables.get(0)
                .symbol);

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

        final BLangAssignment countAdd = ASTBuilderUtil.createAssignmentStmt(pos, blockStmt);
        countAdd.varRef = resultVar;
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
        indexAccessNode.indexExpr = ASTBuilderUtil.createVariableRef(pos, ctx.countVar.symbol);
        indexAccessNode.expr = ASTBuilderUtil.createVariableRef(pos, ctx.resultVar.symbol);
        indexAccessNode.type = ctx.iteratorResultVariables.get(0).symbol.type;
        final BLangAssignment valueAssign = ASTBuilderUtil.createAssignmentStmt(pos, blockStmt);
        valueAssign.varRef = indexAccessNode;
        valueAssign.expr = ASTBuilderUtil.createVariableRef(pos, ctx.iteratorResultVariables.get(0).symbol);

        // create count = count + 1;
        generateCountAggregator(blockStmt, ctx.countVar);
    }

    private void generateTableAggregator(BLangBlockStmt blockStmt, IterableContext ctx) {
        final DiagnosticPos pos = blockStmt.pos;

        List<BLangVariable> variables = new ArrayList<>(1);
        variables.add(ctx.iteratorResultVariables.get(0));
        BInvokableSymbol addSymbol = (BInvokableSymbol) symTable.rootScope.lookup(names.fromString(TABLE_ADD_FUNCTION))
                .symbol;
        BLangInvocation addFunctionInvocation = ASTBuilderUtil.createInvocationExpr(pos, addSymbol, variables,
                symResolver);
        addFunctionInvocation.exprSymbol = ctx.resultVar.symbol;
        BLangExpressionStmt expressionStmt = ASTBuilderUtil.createExpressionStmt(pos, blockStmt);
        expressionStmt.expr = addFunctionInvocation;
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
        final BLangIndexBasedAccess indexAccessNode =
                (BLangIndexBasedAccess) TreeBuilder.createIndexBasedAccessNode();
        indexAccessNode.pos = pos;
        indexAccessNode.indexExpr = ASTBuilderUtil.createVariableRef(pos, ctx.iteratorResultVariables.get(0).symbol);
        indexAccessNode.expr = ASTBuilderUtil.createVariableRef(pos, ctx.resultVar.symbol);
        indexAccessNode.type = ctx.iteratorResultVariables.get(1).symbol.type;
        final BLangAssignment valueAssign = ASTBuilderUtil.createAssignmentStmt(pos, blockStmt);
        valueAssign.varRef = indexAccessNode;
        valueAssign.expr = ASTBuilderUtil.generateConversionExpr(ASTBuilderUtil.createVariableRef(pos,
                ctx.iteratorResultVariables.get(1).symbol), symTable.anyType, symResolver);
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
        if (ctx.resultVar.symbol.type.tag > TypeTags.TYPEDESC) {
            return;
        }
        final DiagnosticPos pos = blockStmt.pos;
        final BLangBinaryExpr equality = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        equality.pos = pos;
        equality.type = symTable.booleanType;
        equality.opKind = OperatorKind.EQUAL;
        equality.lhsExpr = ASTBuilderUtil.createVariableRef(pos, ctx.countVar.symbol);
        equality.rhsExpr = ASTBuilderUtil.createLiteral(pos, symTable.intType, 0L);
        equality.opSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.EQUAL, symTable.intType,
                symTable.intType);

        final BLangIf ifNode = ASTBuilderUtil.createIfStmt(pos, blockStmt);
        ifNode.expr = equality;
        ifNode.body = ASTBuilderUtil.createBlockStmt(pos);
        if (ctx.resultVar.symbol.type.tag <= TypeTags.FLOAT) {
            final BLangAssignment assign = ASTBuilderUtil.createAssignmentStmt(pos, ifNode.body);
            assign.varRef = ASTBuilderUtil.createVariableRef(pos, ctx.resultVar.symbol);
            switch (ctx.resultVar.symbol.type.tag) {
                case TypeTags.INT:
                    assign.expr = ASTBuilderUtil.createLiteral(pos, symTable.intType, 0L);
                    break;
                case TypeTags.FLOAT:
                    assign.expr = ASTBuilderUtil.createLiteral(pos, symTable.floatType, 0D);
                    break;
            }
        }
        final BLangReturn returnStmt = ASTBuilderUtil.createReturnStmt(pos, ifNode.body);
        returnStmt.expr = ASTBuilderUtil.createVariableRef(pos, ctx.resultVar.symbol);
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
        divide.lhsExpr = ASTBuilderUtil.createVariableRef(pos, ctx.resultVar.symbol);
        divide.rhsExpr = ASTBuilderUtil.createVariableRef(pos, ctx.countVar.symbol);
        divide.opSymbol = (BOperatorSymbol) symResolver.resolveBinaryOperator(OperatorKind.DIV, divide.type,
                ctx.countVar.symbol.type);
        final BLangAssignment countAdd = ASTBuilderUtil.createAssignmentStmt(pos, blockStmt);
        countAdd.varRef = ASTBuilderUtil.createVariableRef(pos, ctx.resultVar.symbol);
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
            case SELECT:
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
        final BLangExpressionStmt exprStmt = ASTBuilderUtil.createExpressionStmt(pos, blockStmt);
        exprStmt.expr = ASTBuilderUtil.createInvocationExpr(pos, operation.lambdaSymbol, Lists.of(operation.argVar),
                symResolver);
    }

    /**
     * Generates statements for filter operation.
     *
     * if(!lambda(...)){
     * next;
     * }
     *
     * @param blockStmt target
     * @param operation operation instance
     */
    private void generateFilter(BLangBlockStmt blockStmt, Operation operation) {
        final DiagnosticPos pos = operation.pos;

        final BLangIf ifNode = ASTBuilderUtil.createIfStmt(pos, blockStmt);
        final BLangUnaryExpr notExpr = (BLangUnaryExpr) TreeBuilder.createUnaryExpressionNode();
        notExpr.pos = pos;
        notExpr.operator = OperatorKind.NOT;
        notExpr.opSymbol = (BOperatorSymbol) symResolver.resolveUnaryOperator(pos, notExpr.operator,
                symTable.booleanType);
        notExpr.expr = ASTBuilderUtil.createInvocationExpr(pos, operation.lambdaSymbol, Lists.of(operation.argVar),
                symResolver);
        notExpr.type = symTable.booleanType;
        ifNode.expr = notExpr;
        ifNode.body = ASTBuilderUtil.createBlockStmt(pos);
        ASTBuilderUtil.createNextStmt(pos, ifNode.body);
    }

    /**
     * Generates statements for Map/Select operation. Select operation is similar to Map except that Select returns
     * a Table where as Map returns an Array.
     *
     * v3,v4 = lambda(v1,v2);
     *
     * @param blockStmt target
     * @param operation operation instance
     */
    private void generateMap(BLangBlockStmt blockStmt, Operation operation) {
        final DiagnosticPos pos = operation.pos;
        final BLangAssignment assignment = ASTBuilderUtil.createAssignmentStmt(pos, blockStmt);
        assignment.varRef = ASTBuilderUtil.createVariableRef(operation.pos, operation.retVar.symbol);
        assignment.expr = ASTBuilderUtil.createInvocationExpr(pos, operation.lambdaSymbol, Lists.of(operation.argVar)
                , symResolver);
    }


    /* Some Utils methods */

    private List<BLangVariable> createForeachVariables(IterableContext ctx,
                                                       BLangVariable firstOperationArg,
                                                       BLangFunction funcNode) {
        List<BLangVariable> foreachVariables = new ArrayList<>();
        if (firstOperationArg.type.tag != TypeTags.TUPLE) {
            foreachVariables.add(firstOperationArg);
            defineVariable(firstOperationArg, ctx.env.enclPkg.symbol.pkgID, funcNode);
            return foreachVariables;
        }
        final List<BType> tupleTypes = ((BTupleType) firstOperationArg.type).tupleTypes;
        int index = 0;
        for (BType type : tupleTypes) {
            String varName = VAR_FOREACH_VAL + index++;
            final BLangVariable variable = ASTBuilderUtil.createVariable(funcNode.pos, varName, type);
            foreachVariables.add(variable);
            defineVariable(variable, ctx.env.enclPkg.symbol.pkgID, funcNode);
        }
        return foreachVariables;
    }

    private List<BLangVariable> createIteratorResultVariables(IterableContext ctx,
                                                              BLangVariable lastOperationArg,
                                                              BLangFunction funcNode) {
        List<BLangVariable> resultVariables = new ArrayList<>();
        if (lastOperationArg.type.tag != TypeTags.TUPLE) {
            resultVariables.add(lastOperationArg);
            defineVariable(lastOperationArg, ctx.env.enclPkg.symbol.pkgID, funcNode);
            return resultVariables;
        }
        final List<BType> tupleTypes = ((BTupleType) lastOperationArg.type).tupleTypes;
        int index = 0;
        for (BType type : tupleTypes) {
            String varName = VAR_RESULT_VAL + index++;
            final BLangVariable variable = ASTBuilderUtil.createVariable(funcNode.pos, varName, type);
            resultVariables.add(variable);
            defineVariable(variable, ctx.env.enclPkg.symbol.pkgID, funcNode);
        }
        return resultVariables;
    }

    private List<BType> getTupleTypeList(BType firstOperationInputType) {
        if (firstOperationInputType.tag != TypeTags.TUPLE) {
            return Lists.of(firstOperationInputType);
        }
        return ((BTupleType) firstOperationInputType).tupleTypes;
    }

    private void defineVariable(BLangVariable variable, PackageID pkgID, BLangFunction funcNode) {
        variable.symbol = new BVarSymbol(0, names.fromIdNode(variable.name), pkgID, variable.type, funcNode.symbol);
    }

    private String getFunctionName(String name) {
        return name + lambdaFunctionCount++;
    }

}

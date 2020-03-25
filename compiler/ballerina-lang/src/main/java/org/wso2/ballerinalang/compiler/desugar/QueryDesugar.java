/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.model.tree.statements.VariableDefinitionNode;
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
import org.wso2.ballerinalang.compiler.tree.types.BLangType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLogHelper;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Lists;

import java.util.List;

/**
 * Class responsible for desugar query pipeline into actual Ballerina code.
 *
 * @since 1.2.0
 */
public class QueryDesugar extends BLangNodeVisitor {

    private static final CompilerContext.Key<QueryDesugar> QUERY_DESUGAR_KEY =
            new CompilerContext.Key<>();
    private final SymbolEnter symbolEnter;
    private final Desugar desugar;
    private final SymbolTable symTable;
    private final BLangAnonymousModelHelper anonymousModelHelper;
    private BLangDiagnosticLogHelper dlog;
    private final SymbolResolver symResolver;
    private final Names names;
    private final Types types;
    private BLangBlockStmt parentBlock = null;
    private SymbolEnv env;

    private QueryDesugar(CompilerContext context) {
        context.put(QUERY_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.names = Names.getInstance(context);
        this.types = Types.getInstance(context);
        this.dlog = BLangDiagnosticLogHelper.getInstance(context);
        this.desugar = Desugar.getInstance(context);
        this.anonymousModelHelper = BLangAnonymousModelHelper.getInstance(context);
    }

    public static QueryDesugar getInstance(CompilerContext context) {
        QueryDesugar desugar = context.get(QUERY_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new QueryDesugar(context);
        }

        return desugar;
    }

    // Create While statement
    //
    // Below query expression :
    //    Person[]|error? outputDataArray = from var person in personList
    //                                        select person;
    //
    // changes as,
    //    Person[]|error? outputDataArray = ();
    //    Person[] $tempDataArray$ = [];
    //
    //    Person[] $data$ = personList;
    //    abstract object {public function next() returns record {|Person value;|}? $iterator$ = $data$.iterator();
    //    record {|Person value;|}|error? $result$ = $iterator$.next();
    //
    //    while (true) {
    //        if ($result$ is ()) {
    //            break;
    //        } else if ($result$ is error) {
    //            outputDataArray = $result$;
    //            break;
    //        } else {
    //            var $value$ = $result$.value;
    //        }
    //        $result$ = $iterator$.next();
    //    }
    //
    //    if (outputDataArray is ()) {
    //        outputDataArray = tempDataArray;
    //    }
    BLangStatementExpression desugarQueryExpr(BLangQueryExpr queryExpr, SymbolEnv env) {
        this.env = env;
        List<BLangFromClause> fromClauseList = queryExpr.fromClauseList;
        BLangFromClause fromClause = fromClauseList.get(0);
        BLangSelectClause selectClause = queryExpr.selectClause;
        List<BLangWhereClause> whereClauseList = queryExpr.whereClauseList;
        List<BLangLetClause> letClauseList = queryExpr.letClausesList;
        DiagnosticPos pos = fromClause.pos;
        parentBlock = ASTBuilderUtil.createBlockStmt(fromClause.pos);

        // Create output data array variable
        // Person[]|error ? $outputDataArray$ = ();
        BArrayType outputArrayType = new BArrayType(selectClause.expression.type);
        BType outputUnionType = BUnionType.create(null, symTable.errorType, outputArrayType);
        BLangLiteral nillLiteral = ASTBuilderUtil.createLiteral(fromClause.pos, symTable.nilType,
                null);
        BVarSymbol outputArrayVarSymbol = new BVarSymbol(0, new Name("$outputDataArray$"),
                env.scope.owner.pkgID, outputUnionType, env.scope.owner);
        BLangSimpleVariable outputArrayVariable =
                ASTBuilderUtil.createVariable(pos, "$outputDataArray$", outputUnionType,
                        nillLiteral, outputArrayVarSymbol);
        BLangSimpleVariableDef outputVariableDef =
                ASTBuilderUtil.createVariableDef(pos, outputArrayVariable);
        BLangSimpleVarRef outputVarRef = ASTBuilderUtil.createVariableRef(pos, outputArrayVariable.symbol);

        // Create temp array variable
        // Person[] $tempDataArray$ = [];
        BVarSymbol tempArrayVarSymbol = new BVarSymbol(0, new Name("$tempDataArray$"),
                env.scope.owner.pkgID, outputArrayType, env.scope.owner);
        BLangListConstructorExpr emptyArrayExpr = ASTBuilderUtil.createEmptyArrayLiteral(pos,
                outputArrayType);
        BLangSimpleVariable tempArrayVariable =
                ASTBuilderUtil.createVariable(pos, "$tempDataArray$", outputArrayType,
                        emptyArrayExpr, tempArrayVarSymbol);
        BLangSimpleVariableDef tempArrayVariableDef =
                ASTBuilderUtil.createVariableDef(pos, tempArrayVariable);
        BLangSimpleVarRef tempArrayVarRef = ASTBuilderUtil.createVariableRef(pos, tempArrayVariable.symbol);

        parentBlock.addStatement(outputVariableDef);
        parentBlock.addStatement(tempArrayVariableDef);

        BLangBlockStmt leafElseBlock = buildFromClauseBlock(fromClauseList, outputVarRef);

        // Create indexed based access expression statement
        //      $tempDataArray$[$tempDataArray$.length()] = {
        //         firstName: person.firstName,
        //         lastName: person.lastName
        //      };
        BLangInvocation lengthInvocation = createLengthInvocation(selectClause.pos, tempArrayVariable.symbol);
        lengthInvocation.expr = tempArrayVarRef;
        BLangIndexBasedAccess indexAccessExpr = ASTBuilderUtil.createIndexAccessExpr(tempArrayVarRef, lengthInvocation);
        indexAccessExpr.type = selectClause.expression.type;

        // Set the indexed based access expression statement as foreach body
        BLangAssignment tempVarAssignment = ASTBuilderUtil.createAssignmentStmt(pos, indexAccessExpr,
                selectClause.expression);
        BLangBlockStmt bodyBlock = ASTBuilderUtil.createBlockStmt(pos);
        bodyBlock.addStatement(tempVarAssignment);
        buildWhereClauseBlock(whereClauseList, letClauseList, leafElseBlock, bodyBlock, selectClause.pos);

        // if (outputDataArray is ()) {
        //     outputDataArray = tempDataArray;
        // }
        BLangBlockStmt nullCheckIfBody = ASTBuilderUtil.createBlockStmt(fromClause.pos);
        BLangAssignment outputAssignment = ASTBuilderUtil
                .createAssignmentStmt(fromClause.pos, outputVarRef, tempArrayVarRef);
        nullCheckIfBody.addStatement(outputAssignment);
        BLangIf nullCheckIf = createTypeCheckIfNode(fromClause.pos, outputVarRef,
                desugar.getNillTypeNode(), nullCheckIfBody);

        parentBlock.addStatement(nullCheckIf);

        // Create statement expression with temp variable definition statements & while statement
        BLangStatementExpression stmtExpr = ASTBuilderUtil.createStatementExpression(parentBlock, outputVarRef);

        stmtExpr.type = outputArrayType;
        return stmtExpr;
    }

    BLangStatementExpression desugarQueryAction(BLangQueryAction queryAction, SymbolEnv env) {
        this.env = env;
        List<BLangFromClause> fromClauseList = queryAction.fromClauseList;
        List<BLangLetClause> letClauseList = queryAction.letClauseList;
        BLangFromClause fromClause = fromClauseList.get(0);
        BLangDoClause doClause = queryAction.doClause;
        List<BLangWhereClause> whereClauseList = queryAction.whereClauseList;
        DiagnosticPos pos = fromClause.pos;
        parentBlock = ASTBuilderUtil.createBlockStmt(fromClause.pos);

        BLangExpression nilExpression = ASTBuilderUtil.createLiteral(pos, symTable.nilType, Names.NIL_VALUE);
        BVarSymbol outputVarSymbol = new BVarSymbol(0, new Name("$outputVar$"),
                env.scope.owner.pkgID, symTable.errorOrNilType, env.scope.owner);
        BLangSimpleVariable outputVariable =
                ASTBuilderUtil.createVariable(pos, "$outputVar$", symTable.errorOrNilType,
                        nilExpression, outputVarSymbol);
        BLangSimpleVariableDef outputVariableDef =
                ASTBuilderUtil.createVariableDef(pos, outputVariable);
        BLangSimpleVarRef outputVarRef = ASTBuilderUtil.createVariableRef(pos, outputVariable.symbol);
        parentBlock.addStatement(outputVariableDef);

        BLangBlockStmt leafElseBlock = buildFromClauseBlock(fromClauseList, outputVarRef);
        buildWhereClauseBlock(whereClauseList, letClauseList, leafElseBlock, doClause.body, doClause.pos);

        BLangStatementExpression stmtExpr = ASTBuilderUtil.createStatementExpression(parentBlock, outputVarRef);
        stmtExpr.type = symTable.errorOrNilType;
        return stmtExpr;
    }

    private BLangBlockStmt buildFromClauseBlock(List<BLangFromClause> fromClauseList, BLangSimpleVarRef outputVarRef) {
        BLangBlockStmt leafElseBody = null;
        for (BLangFromClause fromClause : fromClauseList) {
            // int[] $data$ = personList;
            BVarSymbol dataSymbol = new BVarSymbol(0, names.fromString("$data$"), env.scope.owner.pkgID,
                    fromClause.collection.type, this.env.scope.owner);
            BLangSimpleVariable dataVariable = ASTBuilderUtil.createVariable(fromClause.pos, "$data$",
                    fromClause.collection.type, fromClause.collection, dataSymbol);
            BLangSimpleVariableDef dataVarDef = ASTBuilderUtil.createVariableDef(fromClause.pos, dataVariable);

            // abstract object {public function next() returns record {|Person value;|}? $iterator$ = $data$.iterator();
            BVarSymbol collectionSymbol = dataVariable.symbol;
            BInvokableSymbol iteratorInvSymbol = desugar.getLangLibIteratorInvokableSymbol(collectionSymbol);
            BLangSimpleVariableDef iteratorVarDef = desugar.getIteratorVariableDefinition(fromClause.pos,
                    collectionSymbol, iteratorInvSymbol, true);
            BVarSymbol iteratorSymbol = iteratorVarDef.var.symbol;

            // Create a new symbol for the $result$.
            BVarSymbol resultSymbol = new BVarSymbol(0, names.fromString("$result$"),
                    this.env.scope.owner.pkgID, fromClause.nillableResultType, this.env.scope.owner);

            // Note - map<T>? $result$ = $iterator$.next();
            BLangSimpleVariableDef resultVariableDefinition = desugar.getIteratorNextVariableDefinition(fromClause.pos,
                    fromClause.nillableResultType, iteratorSymbol, resultSymbol);
            BLangSimpleVarRef resultReferenceInWhile = ASTBuilderUtil.createVariableRef(fromClause.pos, resultSymbol);

            // create while loop: while (true)
            BLangLiteral conditionLiteral = ASTBuilderUtil.createLiteral(fromClause.pos, symTable.booleanType,
                    true);
            BLangGroupExpr whileCondition = new BLangGroupExpr();
            whileCondition.type = symTable.booleanType;
            whileCondition.expression = conditionLiteral;

            BLangWhile whileNode = (BLangWhile) TreeBuilder.createWhileNode();
            whileNode.expr = whileCondition;

            // if ($result$ is()){
            //     break;
            // }
            BLangBlockStmt nullCheckIfBody = ASTBuilderUtil.createBlockStmt(fromClause.pos);
            nullCheckIfBody.addStatement(TreeBuilder.createBreakNode());
            BLangIf nullCheckIf = createTypeCheckIfNode(fromClause.pos, resultReferenceInWhile,
                    desugar.getNillTypeNode(), nullCheckIfBody);

            // if ($result$ is error){
            //    outputDataArray = $result$;
            //    break;
            // }
            BLangBlockStmt errorCheckIfBody = ASTBuilderUtil.createBlockStmt(fromClause.pos);
            BLangAssignment errorValueAssignment = ASTBuilderUtil.createAssignmentStmt(fromClause.pos,
                    outputVarRef, resultReferenceInWhile);
            errorCheckIfBody.addStatement(errorValueAssignment);
            errorCheckIfBody.addStatement(TreeBuilder.createBreakNode());
            BLangIf errorCheckIf = createTypeCheckIfNode(fromClause.pos, resultReferenceInWhile,
                    desugar.getErrorTypeNode(), errorCheckIfBody);

            nullCheckIf.elseStmt = errorCheckIf;

            // else{
            //     var value = $result$.value;
            //     $tempDataArray$[$tempDataArray$.length()] = value;
            // }
            BLangBlockStmt elseBody = ASTBuilderUtil.createBlockStmt(fromClause.pos);
            // Note - $result$ = $iterator$.next(); < this should go after initial assignment of `value`
            BLangAssignment resultAssignment = desugar.getIteratorNextAssignment(fromClause.pos,
                    iteratorSymbol, resultSymbol);
            VariableDefinitionNode variableDefinitionNode = fromClause.variableDefinitionNode;

            // var $value$ = $result$.value;
            BLangFieldBasedAccess valueAccessExpr = desugar.getValueAccessExpression(fromClause.pos,
                    fromClause.varType, resultSymbol);
            valueAccessExpr.expr = desugar.addConversionExprIfRequired(valueAccessExpr.expr,
                    types.getSafeType(valueAccessExpr.expr.type, true, false));
            variableDefinitionNode.getVariable()
                    .setInitialExpression(desugar.addConversionExprIfRequired(valueAccessExpr, fromClause.varType));

            elseBody.stmts.add(0, (BLangStatement) variableDefinitionNode);
            errorCheckIf.elseStmt = elseBody;

            // if($outputDataArray$ is error) {
            //     break;
            // }
            BLangBlockStmt outputErrorCheckIfBody = ASTBuilderUtil.createBlockStmt(fromClause.pos);
            outputErrorCheckIfBody.addStatement(TreeBuilder.createBreakNode());
            BLangIf outputErrorCheckIf = createTypeCheckIfNode(fromClause.pos, outputVarRef, desugar.getErrorTypeNode(),
                    outputErrorCheckIfBody);

            BLangBlockStmt whileBody = ASTBuilderUtil.createBlockStmt(fromClause.pos);
            whileBody.addStatement(nullCheckIf);
            whileBody.addStatement(outputErrorCheckIf);
            whileBody.addStatement(resultAssignment);
            whileNode.body = whileBody;

            if (leafElseBody != null) {
                BLangBlockStmt childBlock = ASTBuilderUtil.createBlockStmt(fromClause.pos);
                childBlock.addStatement(dataVarDef);
                childBlock.addStatement(iteratorVarDef);
                childBlock.addStatement(resultVariableDefinition);
                childBlock.addStatement(whileNode);
                leafElseBody.addStatement(childBlock);
            } else {
                parentBlock.addStatement(dataVarDef);
                parentBlock.addStatement(iteratorVarDef);
                parentBlock.addStatement(resultVariableDefinition);
                parentBlock.addStatement(whileNode);
            }
            leafElseBody = elseBody;
        }
        return leafElseBody;
    }

    private BLangIf createTypeCheckIfNode(DiagnosticPos pos, BLangExpression expr, BLangType type,
                                          BLangBlockStmt body) {
        BLangTypeTestExpr testExpr = ASTBuilderUtil.createTypeTestExpr(pos, expr, type);
        testExpr.type = symTable.booleanType;
        BLangIf typeCheckIf = (BLangIf) TreeBuilder.createIfElseStatementNode();
        typeCheckIf.pos = pos;
        typeCheckIf.expr = testExpr;
        typeCheckIf.body = body;
        return typeCheckIf;
    }

    private void buildLetClauseBlock(List<BLangLetClause> letClauseList, BLangBlockStmt bLangBlockStmt) {
        // Create variable definitions for the let variable declarations
        if (letClauseList != null) {
            for (BLangLetClause letClause : letClauseList) {
                for (BLangLetVariable letVariable  : letClause.letVarDeclarations) {
                    bLangBlockStmt.addStatement(letVariable.definitionNode);
                }
            }
        }
    }

    private void buildWhereClauseBlock(List<BLangWhereClause> whereClauseList, List<BLangLetClause> letClauseList,
                                       BLangBlockStmt elseBlock, BLangBlockStmt bodyBlock, DiagnosticPos pos) {
        BLangBlockStmt stmtBlock = ASTBuilderUtil.createBlockStmt(pos);
        if (whereClauseList.size() > 0) {
            // Create If Statement with Where expression and foreach body
            BLangIf outerIf = null;
            BLangIf innerIf = null;
            for (BLangWhereClause whereClause : whereClauseList) {
                BLangIf bLangIf = (BLangIf) TreeBuilder.createIfElseStatementNode();
                bLangIf.pos = whereClause.pos;
                bLangIf.expr = whereClause.expression;
                if (innerIf != null) {
                    BLangBlockStmt bLangBlockStmt = ASTBuilderUtil.createBlockStmt(whereClause.pos);
                    bLangBlockStmt.addStatement(bLangIf);
                    innerIf.setBody(bLangBlockStmt);
                } else {
                    outerIf = bLangIf;
                }
                innerIf = bLangIf;
            }
            innerIf.setBody(bodyBlock);

            buildLetClauseBlock(letClauseList, stmtBlock);
            stmtBlock.addStatement(outerIf);
        } else {
            buildLetClauseBlock(letClauseList, stmtBlock);
            stmtBlock.stmts.addAll(bodyBlock.getStatements());
        }
        elseBlock.getStatements().addAll(stmtBlock.getStatements());
    }

    private BLangInvocation createLengthInvocation(DiagnosticPos pos, BVarSymbol collectionSymbol) {
        BInvokableSymbol lengthInvokableSymbol =
                (BInvokableSymbol) symResolver.lookupLangLibMethod(collectionSymbol.type,
                        names.fromString("length"));
        BLangSimpleVarRef collection = ASTBuilderUtil.createVariableRef(pos, collectionSymbol);
        BLangInvocation lengthInvocation = ASTBuilderUtil.createInvocationExprForMethod(pos, lengthInvokableSymbol,
                Lists.of(collection), symResolver);
        lengthInvocation.type = lengthInvokableSymbol.type.getReturnType();
        // Note: No need to set lengthInvocation.expr for langLib functions as they are in requiredArgs
        return lengthInvocation;
    }
}

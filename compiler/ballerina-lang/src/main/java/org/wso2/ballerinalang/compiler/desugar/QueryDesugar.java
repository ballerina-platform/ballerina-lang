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
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangGroupExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangTypeTestExpr;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangWhile;
import org.wso2.ballerinalang.compiler.tree.types.BLangLetVariable;
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
    private BLangForeach parentForeach = null;
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

    BLangStatementExpression desugarQueryExpr(BLangQueryExpr queryExpr, SymbolEnv env) {
        this.env = env;
        List<BLangFromClause> fromClauseList = queryExpr.fromClauseList;
        BLangFromClause fromClause = fromClauseList.get(0);
        BLangSelectClause selectClause = queryExpr.selectClause;
        List<BLangWhereClause> whereClauseList = queryExpr.whereClauseList;
        List<BLangLetClause> letClauseList = queryExpr.letClausesList;
        DiagnosticPos pos = fromClause.pos;
        parentBlock =  ASTBuilderUtil.createBlockStmt(fromClause.pos);

        // Create Foreach statement
        //
        // Below query expression :
        //      from var person in personList
        //
        // changes as,
        //      foreach var person in personList {
        //          ....
        //      }
        BArrayType outputArrayType = new BArrayType(selectClause.expression.type);
//        Person[]|error ? $outputDataArray$ = ();
        BType outputUnionType = BUnionType.create(null, symTable.errorType, outputArrayType);
        BLangLiteral nillLiteral = ASTBuilderUtil.createLiteral(fromClause.pos, symTable.nilType,
                null);
        BVarSymbol outputArrayVarSymbol = new BVarSymbol(0, new Name("$outputDataArray$"),
                env.scope.owner.pkgID, outputArrayType, env.scope.owner);
        BLangSimpleVariable outputArrayVariable =
                ASTBuilderUtil.createVariable(pos, "$outputDataArray$", outputUnionType,
                        nillLiteral, outputArrayVarSymbol);
        BLangSimpleVariableDef outputVariableDef =
                ASTBuilderUtil.createVariableDef(pos, outputArrayVariable);
        BLangSimpleVarRef outputVarRef = ASTBuilderUtil.createVariableRef(pos, outputArrayVariable.symbol);

//        Create temp array variable
//        Person[] $tempDataArray$ = [];
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

        BLangBlockStmt elseBlock = buildFromClauseBlockStmt(fromClauseList, outputVarRef);
//        $tempDataArray$[$tempDataArray$.length()] = value;


//        BLangForeach leafForeach = buildFromClauseBlock(fromClauseList);

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
        buildWhereClauseBlock(whereClauseList, letClauseList, elseBlock, tempVarAssignment, selectClause.pos);

        BLangTypeTestExpr typeNullExpr = ASTBuilderUtil
                .createTypeTestExpr(fromClause.pos, outputVarRef, desugar.getNillTypeNode());
        BLangBlockStmt nullCheckIfBody = ASTBuilderUtil.createBlockStmt(fromClause.pos);
        BLangAssignment outputAssignment = ASTBuilderUtil.createAssignmentStmt(fromClause.pos, outputVarRef, tempArrayVarRef);
        nullCheckIfBody.addStatement(outputAssignment);
        BLangIf nullCheckIf = (BLangIf) TreeBuilder.createIfElseStatementNode();
        nullCheckIf.pos = fromClause.pos;
        nullCheckIf.expr = typeNullExpr;
        nullCheckIf.body = nullCheckIfBody;

        parentBlock.addStatement(nullCheckIf);

        // Create statement with temp variable definition statements & while statement
        BLangStatementExpression stmtExpr = ASTBuilderUtil.createStatementExpression(parentBlock, outputVarRef);

        stmtExpr.type = outputArrayType;
        return stmtExpr;
    }

    private BLangFieldBasedAccess getValueAccessExpression(BLangFromClause fromClause, BVarSymbol resultSymbol) {
        BLangSimpleVarRef resultReferenceInVariableDef = ASTBuilderUtil.createVariableRef(fromClause.pos, resultSymbol);
        BLangIdentifier valueIdentifier = ASTBuilderUtil.createIdentifier(fromClause.pos, "value");

        BLangFieldBasedAccess fieldBasedAccessExpression =
                ASTBuilderUtil.createFieldAccessExpr(resultReferenceInVariableDef, valueIdentifier);
        fieldBasedAccessExpression.pos = fromClause.pos;
        fieldBasedAccessExpression.type = fromClause.varType;
        fieldBasedAccessExpression.originalType = fieldBasedAccessExpression.type;
        return fieldBasedAccessExpression;
    }

    BLangBlockStmt desugarQueryAction(BLangQueryAction queryAction, SymbolEnv env) {
        this.env = env;
        BLangBlockStmt blockNode = ASTBuilderUtil.createBlockStmt(queryAction.pos);
        List<BLangFromClause> fromClauseList = queryAction.fromClauseList;
        List<BLangLetClause> letClauseList = queryAction.letClauseList;
        BLangFromClause fromClause = fromClauseList.get(0);
        BLangDoClause doClause = queryAction.doClause;
        List<BLangWhereClause> whereClauseList = queryAction.whereClauseList;
        DiagnosticPos pos = fromClause.pos;

        BLangForeach leafForeach = buildFromClauseBlock(fromClauseList);
        BLangBlockStmt foreachBody = ASTBuilderUtil.createBlockStmt(pos);
        buildWhereClauseBlock(whereClauseList, letClauseList, null, null, doClause.pos);
        foreachBody.addStatement(doClause.body);
        blockNode.stmts.add(parentForeach);
        return blockNode;
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

/*    Person[]|error? outputList = ();
    Person[] tempList = [];
    stream<Person> streamedPersons = personList.toStream();

    record {|Person value;|}|error? nextVal = streamedPersons.next();
    while (true) {
        if (nextVal is ()) {
            break;
        } else if (nextVal is error) {
            outputList = nextVal;
            break;
        } else {
            var value = nextVal.value;
            tempList[tempList.length()] = value;
        }
        nextVal = streamedPersons.next();
    }

    if (outputList is ()) {
        outputList = tempList;
    }*/
    private BLangBlockStmt buildFromClauseBlockStmt(List<BLangFromClause> fromClauseList, BLangSimpleVarRef outputVarRef) {
        BLangWhile leafWhile = null;
        BLangBlockStmt elseBody = null;
        for (BLangFromClause fromClause : fromClauseList) {
//            int[] $data$ = data;
            BVarSymbol dataSymbol = new BVarSymbol(0, names.fromString("$data$"), env.scope.owner.pkgID,
                    fromClause.collection.type, this.env.scope.owner);
            BLangSimpleVariable dataVariable = ASTBuilderUtil.createVariable(fromClause.pos, "$data$",
                    fromClause.collection.type, fromClause.collection, dataSymbol);
            BLangSimpleVariableDef dataVarDef = ASTBuilderUtil.createVariableDef(fromClause.pos, dataVariable);

//            abstract object {public function next() returns record {|int value;|}? iteratorObj = $data$.iterator();
            BVarSymbol collectionSymbol = dataVariable.symbol;
            BInvokableSymbol iteratorInvSymbol = desugar.getLangLibIteratorInvokableSymbol(collectionSymbol);
            BLangSimpleVariableDef iteratorVarDef = desugar.getIteratorVariableDefinition(fromClause.pos, collectionSymbol,
                    iteratorInvSymbol, true);
            BVarSymbol iteratorSymbol = iteratorVarDef.var.symbol;

            // Create a new symbol for the $result$.
            BVarSymbol resultSymbol = new BVarSymbol(0, names.fromString("$result$"), this.env.scope.owner.pkgID,
                    fromClause.nillableResultType, this.env.scope.owner);

            // Note - map<T>? $result$ = $iterator$.next();
            BLangSimpleVariableDef resultVariableDefinition = getIteratorNextVariableDefinition(fromClause, iteratorSymbol,
                    resultSymbol);
            BLangSimpleVarRef resultReferenceInWhile = ASTBuilderUtil.createVariableRef(fromClause.pos, resultSymbol);

            // create while loop: while (true)
            BLangLiteral conditionLiteral = ASTBuilderUtil.createLiteral(fromClause.pos, symTable.booleanType,
                    true);
            BLangGroupExpr whileCondition = new BLangGroupExpr();
            whileCondition.type = symTable.booleanType;
            whileCondition.expression = conditionLiteral;

            BLangWhile whileNode = (BLangWhile) TreeBuilder.createWhileNode();
            whileNode.expr = whileCondition;
//            if ($result$ is ()) {
//                break;
//            }
            BLangTypeTestExpr typeNullTestExpr = ASTBuilderUtil
                    .createTypeTestExpr(fromClause.pos, resultReferenceInWhile, desugar.getNillTypeNode());
            BLangBlockStmt nullCheckIfBody = ASTBuilderUtil.createBlockStmt(fromClause.pos);
            nullCheckIfBody.addStatement(TreeBuilder.createBreakNode());
            BLangIf nullCheckIf = (BLangIf) TreeBuilder.createIfElseStatementNode();
            nullCheckIf.pos = fromClause.pos;
            nullCheckIf.expr = typeNullTestExpr;
            nullCheckIf.body = nullCheckIfBody;

//            if ($result$ is error){
//                $output$ = $result$;
//                break;
//            }
            BLangBlockStmt errorCheckIfBody = ASTBuilderUtil.createBlockStmt(fromClause.pos);
            BLangTypeTestExpr isErrorTest =
                    ASTBuilderUtil.createTypeTestExpr(fromClause.pos, resultReferenceInWhile, desugar.getErrorTypeNode());
            isErrorTest.type = symTable.booleanType;
            BLangAssignment errorValueAssignment = ASTBuilderUtil.createAssignmentStmt(fromClause.pos, outputVarRef, resultReferenceInWhile);
            errorCheckIfBody.addStatement(errorValueAssignment);
            errorCheckIfBody.addStatement(TreeBuilder.createBreakNode());
            BLangIf erroCheckIf = (BLangIf) TreeBuilder.createIfElseStatementNode();
            erroCheckIf.pos = fromClause.pos;
            erroCheckIf.expr = isErrorTest;
            erroCheckIf.body = errorCheckIfBody;

            nullCheckIf.elseStmt = erroCheckIf;

/*            else{
                var value = $result$.value;
                $tempDataArray$[$tempDataArray$.length()] = value;
            }*/
            elseBody = ASTBuilderUtil.createBlockStmt(fromClause.pos);
            // Note - $result$ = $iterator$.next(); < this should go after initial assignment of `item`
            BLangAssignment resultAssignment = desugar.getIteratorNextAssignment(fromClause.pos, iteratorSymbol, resultSymbol);
            VariableDefinitionNode variableDefinitionNode = fromClause.variableDefinitionNode;

            BLangFieldBasedAccess valueAccessExpr = getValueAccessExpression(fromClause, resultSymbol);
            valueAccessExpr.expr = desugar.addConversionExprIfRequired(valueAccessExpr.expr,
                    types.getSafeType(valueAccessExpr.expr.type, true, false));
            variableDefinitionNode.getVariable().setInitialExpression(desugar.addConversionExprIfRequired(valueAccessExpr, fromClause.varType));

            elseBody.stmts.add(0, (BLangStatement) variableDefinitionNode);
            erroCheckIf.elseStmt = elseBody;

            BLangBlockStmt whileBody = ASTBuilderUtil.createBlockStmt(fromClause.pos);
            whileBody.addStatement(nullCheckIf);
            whileBody.addStatement(resultAssignment);

            whileNode.body = whileBody;
            parentBlock.addStatement(dataVarDef);
            parentBlock.addStatement(iteratorVarDef);
            parentBlock.addStatement(resultVariableDefinition);
            parentBlock.addStatement(whileNode);
        }
        return elseBody;
    }

    private BLangSimpleVariableDef getIteratorNextVariableDefinition(BLangFromClause fromClause,
                                                                     BVarSymbol iteratorSymbol,
                                                                     BVarSymbol resultSymbol) {
        BLangInvocation nextInvocation = desugar.createIteratorNextInvocation(fromClause.pos, iteratorSymbol);
        BLangSimpleVariable resultVariable = ASTBuilderUtil.createVariable(fromClause.pos, "$result$",
                fromClause.nillableResultType, nextInvocation, resultSymbol);
        return ASTBuilderUtil.createVariableDef(fromClause.pos, resultVariable);
    }

    private BLangForeach buildFromClauseBlock(List<BLangFromClause> fromClauseList) {
        BLangForeach leafForeach = null;
        for (BLangFromClause fromClause : fromClauseList) {
            BLangForeach foreach = (BLangForeach) TreeBuilder.createForeachNode();
            foreach.pos = fromClause.pos;
            foreach.collection = fromClause.collection;
            types.setForeachTypedBindingPatternType(foreach);

            foreach.variableDefinitionNode = fromClause.variableDefinitionNode;
            foreach.isDeclaredWithVar = fromClause.isDeclaredWithVar;

            if (leafForeach != null) {
                BLangBlockStmt foreachBody = ASTBuilderUtil.createBlockStmt(fromClause.pos);
                foreachBody.addStatement(foreach);
                leafForeach.setBody(foreachBody);
            } else {
                parentForeach = foreach;
            }

            leafForeach = foreach;
        }

        return leafForeach;
    }

    private void buildWhereClauseBlock(List<BLangWhereClause> whereClauseList, List<BLangLetClause> letClauseList,
                                       BLangBlockStmt elseBlock, BLangAssignment selectAssignment, DiagnosticPos pos) {
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
            BLangBlockStmt innerIfBlock = ASTBuilderUtil.createBlockStmt(pos);
            innerIfBlock.addStatement(selectAssignment);
            innerIf.setBody(innerIfBlock);

            buildLetClauseBlock(letClauseList, stmtBlock);
            stmtBlock.addStatement(outerIf);
        } else {
            buildLetClauseBlock(letClauseList, stmtBlock);
            stmtBlock.addStatement(selectAssignment);
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

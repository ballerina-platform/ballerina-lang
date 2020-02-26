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
import org.wso2.ballerinalang.compiler.parser.BLangAnonymousModelHelper;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangDoClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangFromClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangLetClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangSelectClause;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhereClause;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangIndexBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangQueryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangStatementExpression;
import org.wso2.ballerinalang.compiler.tree.statements.BLangAssignment;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForeach;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangQueryAction;
import org.wso2.ballerinalang.compiler.tree.statements.BLangSimpleVariableDef;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
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
    private BLangDiagnosticLog dlog;
    private final SymbolResolver symResolver;
    private final Names names;
    private final Types types;
    private BLangForeach parentForeach = null;

    private QueryDesugar(CompilerContext context) {
        context.put(QUERY_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.names = Names.getInstance(context);
        this.types = Types.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
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
        List<BLangFromClause> fromClauseList = queryExpr.fromClauseList;
        BLangFromClause fromClause = fromClauseList.get(0);
        BLangSelectClause selectClause = queryExpr.selectClause;
        List<BLangWhereClause> whereClauseList = queryExpr.whereClauseList;
        List<BLangLetClause> letClauseList = queryExpr.letClausesList;
        DiagnosticPos pos = fromClause.pos;

        // Create Foreach statement
        //
        // Below query expression :
        //      from var person in personList
        //
        // changes as,
        //      foreach var person in personList {
        //          ....
        //      }
        BLangForeach leafForeach = buildFromClauseBlock(fromClauseList);
        BLangBlockStmt foreachBody = ASTBuilderUtil.createBlockStmt(pos);

        BArrayType outputArrayType = new BArrayType(selectClause.expression.type);
        BLangListConstructorExpr emptyArrayExpr = ASTBuilderUtil.createEmptyArrayLiteral(pos,
                outputArrayType);
        BVarSymbol emptyArrayVarSymbol = new BVarSymbol(0, new Name("$outputDataArray$"),
                env.scope.owner.pkgID, outputArrayType, env.scope.owner);
        BLangSimpleVariable outputArrayVariable =
                ASTBuilderUtil.createVariable(pos, "$outputDataArray$", outputArrayType,
                        emptyArrayExpr, emptyArrayVarSymbol);

        // Create temp array variable
        //      Person[] x = [];

        BLangSimpleVariableDef outputVariableDef =
                ASTBuilderUtil.createVariableDef(pos, outputArrayVariable);
        BLangSimpleVarRef outputVarRef = ASTBuilderUtil.createVariableRef(pos, outputArrayVariable.symbol);

        // Create indexed based access expression statement
        //      x[x.length()] = {
        //         firstName: person.firstName,
        //         lastName: person.lastName
        //      };

        BLangInvocation lengthInvocation = createLengthInvocation(selectClause.pos, outputArrayVariable.symbol);
        lengthInvocation.expr = outputVarRef;
        BLangIndexBasedAccess indexAccessExpr = ASTBuilderUtil.createIndexAccessExpr(outputVarRef, lengthInvocation);
        indexAccessExpr.type = selectClause.expression.type;

        buildWhereClauseBlock(whereClauseList, letClauseList, leafForeach, foreachBody, selectClause.pos);

        // Set the indexed based access expression statement as foreach body
        BLangAssignment outputVarAssignment = ASTBuilderUtil.createAssignmentStmt(pos, indexAccessExpr,
                selectClause.expression);
        foreachBody.addStatement(outputVarAssignment);

        // Create block statement with temp variable definition statement & foreach statement
        BLangBlockStmt blockStmt = ASTBuilderUtil.createBlockStmt(pos);
        blockStmt.addStatement(outputVariableDef);
        blockStmt.addStatement(parentForeach);
        BLangStatementExpression stmtExpr = ASTBuilderUtil.createStatementExpression(blockStmt, outputVarRef);

        stmtExpr.type = outputArrayType;
        return stmtExpr;
    }

    BLangBlockStmt desugarQueryAction(BLangQueryAction queryAction, SymbolEnv env) {
        BLangBlockStmt blockNode = ASTBuilderUtil.createBlockStmt(queryAction.pos);
        List<BLangFromClause> fromClauseList = queryAction.fromClauseList;
        BLangFromClause fromClause = fromClauseList.get(0);
        BLangDoClause doClause = queryAction.doClause;
        List<BLangWhereClause> whereClauseList = queryAction.whereClauseList;
        DiagnosticPos pos = fromClause.pos;

        BLangForeach leafForeach = buildFromClauseBlock(fromClauseList);
        BLangBlockStmt foreachBody = ASTBuilderUtil.createBlockStmt(pos);
        buildWhereClauseBlock(whereClauseList, null, leafForeach, foreachBody, doClause.pos);

        leafForeach.setBody(doClause.body);
        blockNode.stmts.add(parentForeach);
        return blockNode;
    }

    private void buildLetClauseBlock(List<BLangLetClause> letClauseList, BLangBlockStmt bLangBlockStmt) {
        // Create variable definitions for the let variable declarations
        if (letClauseList != null) {
            for (BLangLetClause letClause : letClauseList) {
                for (BLangVariable var : letClause.letVarDeclarations) {
                    BLangSimpleVariableDef varDef = ASTBuilderUtil.createVariableDef(letClause.pos);
                    varDef.var = (BLangSimpleVariable) var;
                    varDef.type = var.type;
                    bLangBlockStmt.addStatement(varDef);
                }
            }
        }
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
                                       BLangForeach leafForEach, BLangBlockStmt foreachBody, DiagnosticPos pos) {
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
            innerIf.setBody(foreachBody);
            BLangBlockStmt bLangBlockStmt = ASTBuilderUtil.createBlockStmt(pos);
            buildLetClauseBlock(letClauseList, bLangBlockStmt);
            bLangBlockStmt.addStatement(outerIf);
            leafForEach.setBody(bLangBlockStmt);
        } else {
            buildLetClauseBlock(letClauseList, foreachBody);
            leafForEach.setBody(foreachBody);
        }
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

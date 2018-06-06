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
import org.ballerinalang.model.tree.clauses.StreamingInput;
import org.ballerinalang.model.tree.clauses.WhereNode;
import org.ballerinalang.model.tree.statements.StatementNode;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolEnv;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamAction;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangStreamingInput;
import org.wso2.ballerinalang.compiler.tree.clauses.BLangWhere;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangBinaryExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangFieldBasedAccess;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangInvocation;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLambdaFunction;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.tree.statements.BLangExpressionStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangForever;
import org.wso2.ballerinalang.compiler.tree.statements.BLangIf;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStreamingQueryStatement;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for desugar an iterable chain into actual Ballerina code.
 *
 * @since 0.961.0
 */
public class StreamingCodeDesugar extends BLangNodeVisitor {

    private static final String FUNC_CALLER = "$lambda$streaming";

    private static final CompilerContext.Key<StreamingCodeDesugar> STREAMING_DESUGAR_KEY =
            new CompilerContext.Key<>();

    private Desugar parentDesugar;
    private final SymbolTable symTable;
    private final SymbolResolver symResolver;
    private final SymbolEnter symbolEnter;
    private final Names names;
    private final Types types;
    private int lambdaFunctionCount = 0;
    private BLangFunction funcNode;
    private BLangIf ifNode;
    private BLangExpressionStmt foreverReplaceStatement;

    private StreamingCodeDesugar(CompilerContext context) {
        context.put(STREAMING_DESUGAR_KEY, this);
        this.symTable = SymbolTable.getInstance(context);
        this.symResolver = SymbolResolver.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.names = Names.getInstance(context);
        this.types = Types.getInstance(context);
    }

    public static StreamingCodeDesugar getInstance(CompilerContext context) {
        StreamingCodeDesugar desugar = context.get(STREAMING_DESUGAR_KEY);
        if (desugar == null) {
            desugar = new StreamingCodeDesugar(context);
        }

        return desugar;
    }

    public BLangExpressionStmt desugar(BLangForever foreverStatement, Desugar desugar) {

        this.parentDesugar = desugar;
        List<? extends StatementNode> statementNodes = foreverStatement.getStreamingQueryStatements();

        // Generate Streaming Consumer Function
        generateStreamConsumerFunction(foreverStatement);
        statementNodes.forEach(statementNode -> ((BLangStatement) statementNode).accept(this));
        return foreverReplaceStatement;
    }

    @Override
    public void visit(BLangStreamingQueryStatement streamingQueryStatement) {
        StreamingInput streamingInput = streamingQueryStatement.getStreamingInput();
        if (streamingInput != null) {
            ((BLangStreamingInput) streamingInput).accept(this);

            foreverReplaceStatement = (BLangExpressionStmt) TreeBuilder.createExpressionStatementNode();
            foreverReplaceStatement.pos = streamingQueryStatement.pos;
            BInvokableSymbol subscribeMethodSymbol = (BInvokableSymbol) symTable.rootScope.
                    lookup(names.fromString("stream.subscribe"))
                    .symbol;
            BVarSymbol functionVarSymbol = funcNode.symbol;
            BLangSimpleVarRef simpleVarRef = new BLangSimpleVarRef.BLangFunctionVarRef(functionVarSymbol);
            simpleVarRef.type = ((funcNode).symbol).type;

            List<BLangSimpleVarRef> variables = new ArrayList<>(1);
            variables.add(simpleVarRef);
            BLangInvocation invocationExpr = ASTBuilderUtil.
                    createInvocationExprForMethod(streamingQueryStatement.pos, subscribeMethodSymbol, variables,
                            symResolver);

            BVarSymbol varSymbol = ((BVarSymbol) ((BLangSimpleVarRef) (streamingInput).getStreamReference()).symbol);
            invocationExpr.expr = ASTBuilderUtil.createVariableRef(streamingQueryStatement.pos, varSymbol);
            foreverReplaceStatement.expr = invocationExpr;
        }

        BLangStreamAction streamingAction = (BLangStreamAction) streamingQueryStatement.getStreamingAction();
        streamingAction.accept(this);
    }

    @Override
    public void visit(BLangStreamAction streamAction) {
        BLangLambdaFunction lambdaFunction = (BLangLambdaFunction) streamAction.getInvokableBody();
        lambdaFunction.accept(this);
    }

    @Override
    public void visit(BLangStreamingInput streamingInput) {
        WhereNode beforeWhereNode = streamingInput.getBeforeStreamingCondition();
        WhereNode afterWhereNode = streamingInput.getAfterStreamingCondition();
        if (beforeWhereNode != null) {
            ((BLangWhere) beforeWhereNode).accept(this);
        }

        if (afterWhereNode != null) {
            ((BLangWhere) afterWhereNode).accept(this);
        }
    }


    @Override
    public void visit(BLangWhere where) {

        //Create IF Clause
        ifNode = ASTBuilderUtil.createIfStmt(where.pos, funcNode.body);
        ifNode.expr = getBinaryExpr((BLangBinaryExpr) where.getExpression());
        ifNode.body = ASTBuilderUtil.createBlockStmt(where.pos);
    }

    public BLangBinaryExpr getBinaryExpr(BLangBinaryExpr binaryExpr) {
        final BLangBinaryExpr equality = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
        equality.pos = binaryExpr.pos;
        equality.type = symTable.booleanType;
        equality.opKind = (binaryExpr).getOperatorKind();
        BLangSimpleVarRef varRef = ASTBuilderUtil.createVariableRef(binaryExpr.pos, (funcNode).requiredParams.get(0).symbol);

        if (binaryExpr.getLeftExpression() instanceof BLangBinaryExpr) {
            equality.lhsExpr = getBinaryExpr((BLangBinaryExpr) binaryExpr.getLeftExpression());
        } else {
            equality.lhsExpr = ASTBuilderUtil.createFieldAccessExpr(varRef,
                    ((BLangFieldBasedAccess) (binaryExpr).lhsExpr).field);
            ((BLangFieldBasedAccess) equality.lhsExpr).symbol = ((BLangFieldBasedAccess) (binaryExpr).lhsExpr).symbol;
            ((BLangFieldBasedAccess) equality.lhsExpr).type = binaryExpr.rhsExpr.type;
        }

        if (binaryExpr.getRightExpression() instanceof BLangBinaryExpr) {
            equality.rhsExpr = getBinaryExpr((BLangBinaryExpr) binaryExpr.getRightExpression());
        } else {

            if ((binaryExpr).rhsExpr instanceof BLangLiteral) {
                equality.rhsExpr = (binaryExpr).rhsExpr;
            } else {
                equality.rhsExpr = ASTBuilderUtil.createFieldAccessExpr(varRef,
                        ((BLangFieldBasedAccess) (binaryExpr).rhsExpr).field);
            }
        }
        equality.opSymbol = (binaryExpr).opSymbol;

        return equality;
    }





//    @Override
//    public void visit(BLangWhere where) {
//
//        //Create IF Clause
//        ifNode = ASTBuilderUtil.createIfStmt(where.pos, funcNode.body);
//        ifNode.expr = getBinaryExpr((BLangBinaryExpr) where.getExpression());
//        ifNode.body = ASTBuilderUtil.createBlockStmt(where.pos);
//    }
//
//    public BLangBinaryExpr getBinaryExpr(BLangBinaryExpr binaryExpr) {
//        final BLangBinaryExpr equality = (BLangBinaryExpr) TreeBuilder.createBinaryExpressionNode();
//        equality.pos = binaryExpr.pos;
//        equality.type = symTable.booleanType;
//        equality.opKind = (binaryExpr).getOperatorKind();
//        BLangSimpleVarRef varRef = ASTBuilderUtil.createVariableRef(binaryExpr.pos, (funcNode).requiredParams.get(0).symbol);
//
//        if (binaryExpr.getLeftExpression() instanceof BLangBinaryExpr) {
//            equality.lhsExpr = getBinaryExpr((BLangBinaryExpr) binaryExpr.getLeftExpression());
//        } else {
//            equality.lhsExpr = ASTBuilderUtil.createFieldAccessExpr(varRef,
//                    ((BLangFieldBasedAccess) (binaryExpr).lhsExpr).field);
//            ((BLangFieldBasedAccess) equality.lhsExpr).symbol = ((BLangFieldBasedAccess) (binaryExpr).lhsExpr).symbol;
//            ((BLangFieldBasedAccess) equality.lhsExpr).type = binaryExpr.rhsExpr.type;
//        }
//
//        if (binaryExpr.getRightExpression() instanceof BLangBinaryExpr) {
//            equality.rhsExpr = getBinaryExpr((BLangBinaryExpr) binaryExpr.getRightExpression());
//        } else {
//
//            if ((binaryExpr).rhsExpr instanceof BLangLiteral) {
//                equality.rhsExpr = (binaryExpr).rhsExpr;
//            } else {
//                equality.rhsExpr = ASTBuilderUtil.createFieldAccessExpr(varRef,
//                        ((BLangFieldBasedAccess) (binaryExpr).rhsExpr).field);
//            }
//        }
//        equality.opSymbol = (binaryExpr).opSymbol;
//
//        return equality;
//    }

    @Override
    public void visit(BLangLambdaFunction bLangLambdaFunction) {
        for (int i = 0; i < bLangLambdaFunction.getFunctionNode().getBody().getStatements().size() - 1; i++) {
            StatementNode statementNode = bLangLambdaFunction.getFunctionNode().getBody().getStatements().get(i);
            final BLangExpressionStmt exprStmt = ASTBuilderUtil.createExpressionStmt(bLangLambdaFunction.pos, ifNode.body);
            exprStmt.expr = ((BLangExpressionStmt) statementNode).expr;
        }
    }


    //-------------------------------------- Private methods ----------------------------------------------

    private void generateStreamConsumerFunction(BLangForever foreverStatement) {

        // Here we generate an function and function call for the forever statement.
        // Here is an example query

        // -- Below is the sample Ballerina query
        // from employeeStream
        // where age > 30
        // select *
        // => (Teacher[] emp) {
        //      outputEmployeeStream.publish(emp);
        //}

        // Above Ballerina query is desugared to below statements

        // employeeStream.subscribe(processEmployees);

        // -- Desugared version
        // function processEmployees(Teacher e) {
        //
        //      if(e.age > 25) {
        //          outputEmployeeStream.publish(e);
        //      }
        //
        // }

        final DiagnosticPos pos = foreverStatement.pos;
        funcNode = ASTBuilderUtil.createFunction(pos, getFunctionName(FUNC_CALLER));

        funcNode.requiredParams.add((BLangVariable) foreverStatement.getStreamingQueryStatements().get(0).
                getStreamingAction().getInvokableBody().getFunctionNode().getParameters().get(0));
        funcNode.returnTypeNode = ASTBuilderUtil.createTypeNode(symTable.nilType);
        funcNode.desugaredReturnType = true;
        defineFunction(funcNode, foreverStatement.getEnv().enclPkg);
    }

    private String getFunctionName(String name) {
        return name + lambdaFunctionCount++;
    }

    private void defineFunction(BLangFunction funcNode, BLangPackage targetPkg) {
        final BPackageSymbol packageSymbol = targetPkg.symbol;
        final SymbolEnv packageEnv = this.symTable.pkgEnvMap.get(packageSymbol);
        symbolEnter.defineNode(funcNode, packageEnv);
        packageEnv.enclPkg.functions.add(funcNode);
        packageEnv.enclPkg.topLevelNodes.add(funcNode);
    }

}

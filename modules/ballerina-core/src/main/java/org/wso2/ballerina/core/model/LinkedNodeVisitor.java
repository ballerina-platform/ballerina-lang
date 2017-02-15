/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.ballerina.core.model;

import org.wso2.ballerina.core.model.expressions.ActionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.ArrayInitExpr;
import org.wso2.ballerina.core.model.expressions.ArrayMapAccessExpr;
import org.wso2.ballerina.core.model.expressions.BacktickExpr;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.BinaryExpression;
import org.wso2.ballerina.core.model.expressions.ConnectorInitExpr;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.InstanceCreationExpr;
import org.wso2.ballerina.core.model.expressions.MapInitExpr;
import org.wso2.ballerina.core.model.expressions.MapStructInitKeyValueExpr;
import org.wso2.ballerina.core.model.expressions.RefTypeInitExpr;
import org.wso2.ballerina.core.model.expressions.ResourceInvocationExpr;
import org.wso2.ballerina.core.model.expressions.StructFieldAccessExpr;
import org.wso2.ballerina.core.model.expressions.StructInitExpr;
import org.wso2.ballerina.core.model.expressions.TypeCastExpression;
import org.wso2.ballerina.core.model.expressions.UnaryExpression;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.nodes.EndNode;
import org.wso2.ballerina.core.model.nodes.ExitNode;
import org.wso2.ballerina.core.model.nodes.GotoNode;
import org.wso2.ballerina.core.model.nodes.IfElseNode;
import org.wso2.ballerina.core.model.nodes.fragments.expressions.ActionInvocationExprStartNode;
import org.wso2.ballerina.core.model.nodes.fragments.expressions.ArrayInitExprEndNode;
import org.wso2.ballerina.core.model.nodes.fragments.expressions.ArrayMapAccessExprEndNode;
import org.wso2.ballerina.core.model.nodes.fragments.expressions.BacktickExprEndNode;
import org.wso2.ballerina.core.model.nodes.fragments.expressions.BinaryExpressionEndNode;
import org.wso2.ballerina.core.model.nodes.fragments.expressions.CallableUnitEndNode;
import org.wso2.ballerina.core.model.nodes.fragments.expressions.ConnectorInitExprEndNode;
import org.wso2.ballerina.core.model.nodes.fragments.expressions.FunctionInvocationExprStartNode;
import org.wso2.ballerina.core.model.nodes.fragments.expressions.InvokeNativeActionNode;
import org.wso2.ballerina.core.model.nodes.fragments.expressions.InvokeNativeFunctionNode;
import org.wso2.ballerina.core.model.nodes.fragments.expressions.InvokeNativeTypeMapperNode;
import org.wso2.ballerina.core.model.nodes.fragments.expressions.MapInitExprEndNode;
import org.wso2.ballerina.core.model.nodes.fragments.expressions.RefTypeInitExprEndNode;
import org.wso2.ballerina.core.model.nodes.fragments.expressions.StructFieldAccessExprEndNode;
import org.wso2.ballerina.core.model.nodes.fragments.expressions.StructInitExprEndNode;
import org.wso2.ballerina.core.model.nodes.fragments.expressions.TypeCastExpressionEndNode;
import org.wso2.ballerina.core.model.nodes.fragments.expressions.UnaryExpressionEndNode;
import org.wso2.ballerina.core.model.nodes.fragments.statements.AssignStmtEndNode;
import org.wso2.ballerina.core.model.nodes.fragments.statements.ForkJoinStartNode;
import org.wso2.ballerina.core.model.nodes.fragments.statements.ReplyStmtEndNode;
import org.wso2.ballerina.core.model.nodes.fragments.statements.ReturnStmtEndNode;
import org.wso2.ballerina.core.model.nodes.fragments.statements.ThrowStmtEndNode;
import org.wso2.ballerina.core.model.nodes.fragments.statements.TryCatchStmtEndNode;
import org.wso2.ballerina.core.model.nodes.fragments.statements.VariableDefStmtEndNode;
import org.wso2.ballerina.core.model.statements.ActionInvocationStmt;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.BreakStmt;
import org.wso2.ballerina.core.model.statements.ForeachStmt;
import org.wso2.ballerina.core.model.statements.ForkJoinStmt;
import org.wso2.ballerina.core.model.statements.FunctionInvocationStmt;
import org.wso2.ballerina.core.model.statements.IfElseStmt;
import org.wso2.ballerina.core.model.statements.ReplyStmt;
import org.wso2.ballerina.core.model.statements.ReturnStmt;
import org.wso2.ballerina.core.model.statements.ThrowStmt;
import org.wso2.ballerina.core.model.statements.TryCatchStmt;
import org.wso2.ballerina.core.model.statements.VariableDefStmt;
import org.wso2.ballerina.core.model.statements.WhileStmt;

/**
 * LinkedNode Visitor Interface.
 */
public interface LinkedNodeVisitor extends NodeVisitor {

    /* Statement Nodes */

    void visit(ActionInvocationStmt actionInvocationStmt);

    void visit(AssignStmt assignStmt);

    void visit(BlockStmt blockStmt);

    void visit(BreakStmt breakStmt);

    void visit(ForeachStmt foreachStmt);

    void visit(ForkJoinStmt forkJoinStmt);

    void visit(FunctionInvocationStmt functionInvocationStmt);

    void visit(IfElseStmt ifElseStmt);

    void visit(ReplyStmt replyStmt);

    void visit(ReturnStmt returnStmt);

    void visit(ThrowStmt throwStmt);

    void visit(TryCatchStmt tryCatchStmt);

    void visit(VariableDefStmt variableDefStmt);

    void visit(WhileStmt whileStmt);

    /* Expression Nodes */

    void visit(ActionInvocationExpr actionInvocationExpr);

    void visit(ArrayInitExpr arrayInitExpr);

    void visit(ArrayMapAccessExpr arrayMapAccessExpr);

    void visit(BacktickExpr backtickExpr);

    void visit(BasicLiteral basicLiteral);

    void visit(BinaryExpression expression);

    void visit(ConnectorInitExpr connectorInitExpr);

    void visit(FunctionInvocationExpr functionInvocationExpr);

    void visit(InstanceCreationExpr instanceCreationExpr);

    void visit(MapInitExpr mapInitExpr);

    void visit(MapStructInitKeyValueExpr mapStructInitKeyValueExpr);

    void visit(RefTypeInitExpr refTypeInitExpr);

    void visit(ResourceInvocationExpr resourceInvocationExpr);

    void visit(StructFieldAccessExpr structFieldAccessExpr);

    void visit(StructInitExpr structInitExpr);

    void visit(TypeCastExpression typeCastExpression);

    void visit(UnaryExpression unaryExpression);

    void visit(VariableRefExpr variableRefExpr);

    /* Other Nodes */

    void visit(EndNode endNode);

    void visit(ExitNode exitNode);

    void visit(GotoNode gotoNode);

    void visit(IfElseNode ifElseNode);

    /* Node Fragments - Statements */

    void visit(AssignStmtEndNode assignStmtEndNode);

    void visit(ForkJoinStartNode forkJoinStartNode);

    void visit(ThrowStmtEndNode throwStmtEndNode);

    void visit(TryCatchStmtEndNode tryCatchStmtEndNode);

    void visit(ReplyStmtEndNode replyStmtEndNode);

    void visit(ReturnStmtEndNode returnStmtEndNode);

    void visit(VariableDefStmtEndNode variableDefStmtEndNode);

    /* Node Fragments - Expressions */

    void visit(ActionInvocationExprStartNode actionInvocationExprStartNode);

    void visit(ArrayInitExprEndNode arrayInitExprEndNode);

    void visit(ArrayMapAccessExprEndNode arrayMapAccessExprEndNode);

    void visit(BacktickExprEndNode backtickExprEndNode);

    void visit(BinaryExpressionEndNode binaryExpressionEndNode);

    void visit(CallableUnitEndNode callableUnitEndNode);

    void visit(ConnectorInitExprEndNode connectorInitExprEndNode);

    void visit(FunctionInvocationExprStartNode functionInvocationExprStartNode);

    void visit(InvokeNativeActionNode invokeNativeActionNode);

    void visit(InvokeNativeFunctionNode invokeNativeFunctionNode);

    void visit(InvokeNativeTypeMapperNode invokeNativeTypeMapperNode);

    void visit(MapInitExprEndNode mapInitExprEndNode);

    void visit(RefTypeInitExprEndNode refTypeInitExprEndNode);

    void visit(StructFieldAccessExprEndNode structFieldAccessExprEndNode);

    void visit(StructInitExprEndNode structInitExprEndNode);

    void visit(TypeCastExpressionEndNode typeCastExpressionEndNode);

    void visit(UnaryExpressionEndNode unaryExpressionEndNode);
}

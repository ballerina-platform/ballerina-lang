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
package org.ballerinalang.model;

import org.ballerinalang.model.expressions.ActionInvocationExpr;
import org.ballerinalang.model.expressions.ArrayInitExpr;
import org.ballerinalang.model.expressions.ArrayMapAccessExpr;
import org.ballerinalang.model.expressions.BacktickExpr;
import org.ballerinalang.model.expressions.BasicLiteral;
import org.ballerinalang.model.expressions.BinaryExpression;
import org.ballerinalang.model.expressions.ConnectorInitExpr;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.expressions.InstanceCreationExpr;
import org.ballerinalang.model.expressions.MapInitExpr;
import org.ballerinalang.model.expressions.MapStructInitKeyValueExpr;
import org.ballerinalang.model.expressions.RefTypeInitExpr;
import org.ballerinalang.model.expressions.ResourceInvocationExpr;
import org.ballerinalang.model.expressions.StructFieldAccessExpr;
import org.ballerinalang.model.expressions.StructInitExpr;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.expressions.UnaryExpression;
import org.ballerinalang.model.expressions.VariableRefExpr;
import org.ballerinalang.model.nodes.EndNode;
import org.ballerinalang.model.nodes.ExitNode;
import org.ballerinalang.model.nodes.GotoNode;
import org.ballerinalang.model.nodes.IfElseNode;
import org.ballerinalang.model.nodes.fragments.expressions.ActionInvocationExprStartNode;
import org.ballerinalang.model.nodes.fragments.expressions.ArrayInitExprEndNode;
import org.ballerinalang.model.nodes.fragments.expressions.ArrayMapAccessExprEndNode;
import org.ballerinalang.model.nodes.fragments.expressions.BacktickExprEndNode;
import org.ballerinalang.model.nodes.fragments.expressions.BinaryExpressionEndNode;
import org.ballerinalang.model.nodes.fragments.expressions.CallableUnitEndNode;
import org.ballerinalang.model.nodes.fragments.expressions.ConnectorInitExprEndNode;
import org.ballerinalang.model.nodes.fragments.expressions.FunctionInvocationExprStartNode;
import org.ballerinalang.model.nodes.fragments.expressions.InvokeNativeActionNode;
import org.ballerinalang.model.nodes.fragments.expressions.InvokeNativeFunctionNode;
import org.ballerinalang.model.nodes.fragments.expressions.InvokeNativeTypeMapperNode;
import org.ballerinalang.model.nodes.fragments.expressions.MapInitExprEndNode;
import org.ballerinalang.model.nodes.fragments.expressions.RefTypeInitExprEndNode;
import org.ballerinalang.model.nodes.fragments.expressions.StructFieldAccessExprEndNode;
import org.ballerinalang.model.nodes.fragments.expressions.StructInitExprEndNode;
import org.ballerinalang.model.nodes.fragments.expressions.TypeCastExpressionEndNode;
import org.ballerinalang.model.nodes.fragments.expressions.UnaryExpressionEndNode;
import org.ballerinalang.model.nodes.fragments.statements.AssignStmtEndNode;
import org.ballerinalang.model.nodes.fragments.statements.ForkJoinStartNode;
import org.ballerinalang.model.nodes.fragments.statements.ReplyStmtEndNode;
import org.ballerinalang.model.nodes.fragments.statements.ReturnStmtEndNode;
import org.ballerinalang.model.nodes.fragments.statements.ThrowStmtEndNode;
import org.ballerinalang.model.nodes.fragments.statements.TryCatchStmtEndNode;
import org.ballerinalang.model.nodes.fragments.statements.VariableDefStmtEndNode;
import org.ballerinalang.model.statements.ActionInvocationStmt;
import org.ballerinalang.model.statements.AssignStmt;
import org.ballerinalang.model.statements.BlockStmt;
import org.ballerinalang.model.statements.BreakStmt;
import org.ballerinalang.model.statements.ForeachStmt;
import org.ballerinalang.model.statements.ForkJoinStmt;
import org.ballerinalang.model.statements.FunctionInvocationStmt;
import org.ballerinalang.model.statements.IfElseStmt;
import org.ballerinalang.model.statements.ReplyStmt;
import org.ballerinalang.model.statements.ReturnStmt;
import org.ballerinalang.model.statements.ThrowStmt;
import org.ballerinalang.model.statements.TryCatchStmt;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.statements.WhileStmt;

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

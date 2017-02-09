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
package org.wso2.ballerina.core.interpreter.nonblocking;

import org.wso2.ballerina.core.interpreter.ConnectorVarLocation;
import org.wso2.ballerina.core.interpreter.ConstantLocation;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.RuntimeEnvironment;
import org.wso2.ballerina.core.interpreter.ServiceVarLocation;
import org.wso2.ballerina.core.interpreter.StackVarLocation;
import org.wso2.ballerina.core.interpreter.StructVarLocation;
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
import org.wso2.ballerina.core.model.nodes.fragments.statements.ReplyStmtEndNode;
import org.wso2.ballerina.core.model.nodes.fragments.statements.ReturnStmtEndNode;
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
import org.wso2.ballerina.core.model.values.BValue;

/**
 * {@link BLangNonBlockingExecutor} is a non-blocking and self driven Ballerina Executor.
 *
 * @since 0.8.0
 */
public class BLangNonBlockingExecutor extends BLangAbstractLinkedExecutor {

    public BLangNonBlockingExecutor(RuntimeEnvironment runtimeEnv, Context bContext) {
        super(runtimeEnv, bContext);
    }

    @Override
    public void visit(ActionInvocationStmt actionIStmt) {
        super.visit(actionIStmt);
        actionIStmt.next.executeLNode(this);
    }

    @Override
    public void visit(AssignStmt assignStmt) {
        super.visit(assignStmt);
        assignStmt.next.executeLNode(this);
    }

    @Override
    public void visit(BlockStmt blockStmt) {
        super.visit(blockStmt);
        blockStmt.next.executeLNode(this);
    }

    @Override
    public void visit(BreakStmt breakStmt) {
        super.visit(breakStmt);
        breakStmt.next.executeLNode(this);
    }

    @Override
    public void visit(ForeachStmt foreachStmt) {
        super.visit(foreachStmt);
        foreachStmt.next.executeLNode(this);
    }

    @Override
    public void visit(ForkJoinStmt forkJoinStmt) {
        super.visit(forkJoinStmt);
        forkJoinStmt.next.executeLNode(this);
    }

    @Override
    public void visit(FunctionInvocationStmt funcIStmt) {
        super.visit(funcIStmt);
        funcIStmt.next.executeLNode(this);
    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        super.visit(ifElseStmt);
        ifElseStmt.next.executeLNode(this);
    }

    @Override
    public void visit(ReplyStmt replyStmt) {
        super.visit(replyStmt);
        replyStmt.next.executeLNode(this);
    }

    @Override
    public void visit(ReturnStmt returnStmt) {
        super.visit(returnStmt);
        returnStmt.next.executeLNode(this);
    }

    @Override
    public void visit(ThrowStmt throwStmt) {
        super.visit(throwStmt);
        throwStmt.next.executeLNode(this);
    }

    @Override
    public void visit(TryCatchStmt tryCatchStmt) {
        super.visit(tryCatchStmt);
        tryCatchStmt.next.executeLNode(this);
    }

    @Override
    public void visit(VariableDefStmt variableDefStmt) {
        super.visit(variableDefStmt);
        variableDefStmt.next.executeLNode(this);
    }

    @Override
    public void visit(WhileStmt whileStmt) {
        super.visit(whileStmt);
        whileStmt.next.executeLNode(this);
    }

    @Override
    public void visit(ActionInvocationExpr actionInvocationExpr) {
        super.visit(actionInvocationExpr);
        actionInvocationExpr.next.executeLNode(this);
    }

    @Override
    public void visit(ArrayInitExpr arrayInitExpr) {
        super.visit(arrayInitExpr);
        arrayInitExpr.next.executeLNode(this);
    }

    @Override
    public void visit(ArrayMapAccessExpr arrayMapAccessExpr) {
        super.visit(arrayMapAccessExpr);
        arrayMapAccessExpr.next.executeLNode(this);
    }

    @Override
    public void visit(BacktickExpr backtickExpr) {
        super.visit(backtickExpr);
        backtickExpr.next.executeLNode(this);
    }

    @Override
    public void visit(BasicLiteral basicLiteral) {
        super.visit(basicLiteral);
        basicLiteral.next.executeLNode(this);
    }

    @Override
    public void visit(BinaryExpression expression) {
        super.visit(expression);
        expression.next.executeLNode(this);
    }

    @Override
    public void visit(ConnectorInitExpr connectorInitExpr) {
        super.visit(connectorInitExpr);
        connectorInitExpr.next.executeLNode(this);
    }

    @Override
    public void visit(FunctionInvocationExpr functionInvocationExpr) {
        super.visit(functionInvocationExpr);
        functionInvocationExpr.next.executeLNode(this);
    }

    @Override
    public void visit(InstanceCreationExpr instanceCreationExpr) {
        super.visit(instanceCreationExpr);
        instanceCreationExpr.next.executeLNode(this);
    }

    @Override
    public void visit(MapInitExpr mapInitExpr) {
        super.visit(mapInitExpr);
        mapInitExpr.next.executeLNode(this);
    }

    @Override
    public void visit(MapStructInitKeyValueExpr mapStructInitKeyValueExpr) {
        super.visit(mapStructInitKeyValueExpr);
        mapStructInitKeyValueExpr.next.executeLNode(this);
    }

    @Override
    public void visit(RefTypeInitExpr refTypeInitExpr) {
        super.visit(refTypeInitExpr);
        refTypeInitExpr.next.executeLNode(this);
    }

    @Override
    public void visit(ResourceInvocationExpr resourceInvocationExpr) {
        super.visit(resourceInvocationExpr);
        resourceInvocationExpr.getResource().getResourceBody().executeLNode(this);
    }

    @Override
    public void visit(StructFieldAccessExpr structFieldAccessExpr) {
        super.visit(structFieldAccessExpr);
        structFieldAccessExpr.next.executeLNode(this);
    }

    @Override
    public void visit(StructInitExpr structInitExpr) {
        super.visit(structInitExpr);
        structInitExpr.next.executeLNode(this);
    }

    @Override
    public void visit(TypeCastExpression typeCastExpression) {
        super.visit(typeCastExpression);
        typeCastExpression.next.executeLNode(this);
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        super.visit(unaryExpression);
        unaryExpression.next.executeLNode(this);
    }

    @Override
    public void visit(VariableRefExpr variableRefExpr) {
        super.visit(variableRefExpr);
        variableRefExpr.next.executeLNode(this);
    }

    @Override
    public BValue visit(StackVarLocation stackVarLocation) {
        return super.visit(stackVarLocation);
    }

    @Override
    public BValue visit(ConstantLocation constantLocation) {
        return super.visit(constantLocation);
    }

    @Override
    public BValue visit(ServiceVarLocation serviceVarLocation) {
        return super.visit(serviceVarLocation);
    }

    @Override
    public BValue visit(StructVarLocation structLocation) {
        return super.visit(structLocation);
    }

    @Override
    public BValue visit(ConnectorVarLocation connectorVarLocation) {
        return super.visit(connectorVarLocation);
    }

    @Override
    public void visit(EndNode endNode) {
        super.visit(endNode);
    }

    @Override
    public void visit(ExitNode exitNode) {
        super.visit(exitNode);
    }

    @Override
    public void visit(GotoNode gotoNode) {
        super.visit(gotoNode);
        Integer pop = branchIDStack.pop();
        gotoNode.next(pop).executeLNode(this);
    }

    @Override
    public void visit(IfElseNode ifElseNode) {
        super.visit(ifElseNode);
    }

    @Override
    public void visit(AssignStmtEndNode assignStmtEndNode) {
        super.visit(assignStmtEndNode);
        assignStmtEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(ReplyStmtEndNode replyStmtEndNode) {
        super.visit(replyStmtEndNode);
        replyStmtEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(ReturnStmtEndNode returnStmtEndNode) {
        super.visit(returnStmtEndNode);
        returnStmtEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(VariableDefStmtEndNode variableDefStmtEndNode) {
        super.visit(variableDefStmtEndNode);
        variableDefStmtEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(ActionInvocationExprStartNode actionInvocationExprStartNode) {
        super.visit(actionInvocationExprStartNode);
        actionInvocationExprStartNode.next.executeLNode(this);
    }

    @Override
    public void visit(ArrayInitExprEndNode arrayInitExprEndNode) {
        super.visit(arrayInitExprEndNode);
        arrayInitExprEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(ArrayMapAccessExprEndNode arrayMapAccessExprEndNode) {
        super.visit(arrayMapAccessExprEndNode);
        arrayMapAccessExprEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(BacktickExprEndNode backtickExprEndNode) {
        super.visit(backtickExprEndNode);
        backtickExprEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(BinaryExpressionEndNode binaryExpressionEndNode) {
        super.visit(binaryExpressionEndNode);
        binaryExpressionEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(FunctionInvocationExprStartNode functionInvocationExprStartNode) {
        super.visit(functionInvocationExprStartNode);
        functionInvocationExprStartNode.next.executeLNode(this);
    }

    @Override
    public void visit(StructFieldAccessExprEndNode structFieldAccessExprEndNode) {
        super.visit(structFieldAccessExprEndNode);
        structFieldAccessExprEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(StructInitExprEndNode structInitExprEndNode) {
        super.visit(structInitExprEndNode);
        structInitExprEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(TypeCastExpressionEndNode typeCastExpressionEndNode) {
        super.visit(typeCastExpressionEndNode);
        typeCastExpressionEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(UnaryExpressionEndNode unaryExpressionEndNode) {
        super.visit(unaryExpressionEndNode);
        unaryExpressionEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(RefTypeInitExprEndNode refTypeInitExprEndNode) {
        super.visit(refTypeInitExprEndNode);
        refTypeInitExprEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(CallableUnitEndNode callableUnitEndNode) {
        super.visit(callableUnitEndNode);
        callableUnitEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(ConnectorInitExprEndNode connectorInitExprEndNode) {
        super.visit(connectorInitExprEndNode);
        connectorInitExprEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(InvokeNativeActionNode invokeNativeActionNode) {
        super.visit(invokeNativeActionNode);
    }

    @Override
    public void visit(InvokeNativeFunctionNode invokeNativeFunctionNode) {
        super.visit(invokeNativeFunctionNode);
        invokeNativeFunctionNode.next.executeLNode(this);
    }

    @Override
    public void visit(InvokeNativeTypeMapperNode invokeNativeTypeMapperNode) {
        super.visit(invokeNativeTypeMapperNode);
        invokeNativeTypeMapperNode.next.executeLNode(this);
    }

    @Override
    public void visit(MapInitExprEndNode mapInitExprEndNode) {
        super.visit(mapInitExprEndNode);
        mapInitExprEndNode.next.executeLNode(this);
    }

}

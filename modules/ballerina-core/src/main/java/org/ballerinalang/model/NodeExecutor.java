/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.model;

import org.ballerinalang.bre.ConnectorVarLocation;
import org.ballerinalang.bre.ConstantLocation;
import org.ballerinalang.bre.ServiceVarLocation;
import org.ballerinalang.bre.StackVarLocation;
import org.ballerinalang.bre.StructVarLocation;
import org.ballerinalang.bre.WorkerVarLocation;
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
import org.ballerinalang.model.expressions.RefTypeInitExpr;
import org.ballerinalang.model.expressions.ResourceInvocationExpr;
import org.ballerinalang.model.expressions.StructFieldAccessExpr;
import org.ballerinalang.model.expressions.StructInitExpr;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.expressions.UnaryExpression;
import org.ballerinalang.model.expressions.VariableRefExpr;
import org.ballerinalang.model.statements.ActionInvocationStmt;
import org.ballerinalang.model.statements.AssignStmt;
import org.ballerinalang.model.statements.BlockStmt;
import org.ballerinalang.model.statements.BreakStmt;
import org.ballerinalang.model.statements.ForkJoinStmt;
import org.ballerinalang.model.statements.FunctionInvocationStmt;
import org.ballerinalang.model.statements.IfElseStmt;
import org.ballerinalang.model.statements.ReplyStmt;
import org.ballerinalang.model.statements.ReturnStmt;
import org.ballerinalang.model.statements.ThrowStmt;
import org.ballerinalang.model.statements.TryCatchStmt;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.statements.WhileStmt;
import org.ballerinalang.model.statements.WorkerInvocationStmt;
import org.ballerinalang.model.statements.WorkerReplyStmt;
import org.ballerinalang.model.values.BValue;

/**
 * {@code NodeExecutor} responsible for executing a Ballerina applications by traversing statements and expressions.
 *
 * @since 0.8.0
 */
public interface NodeExecutor {

    void visit(BlockStmt blockStmt);

    void visit(VariableDefStmt varDefStmt);

    void visit(AssignStmt assignStmt);

    void visit(IfElseStmt ifElseStmt);

    void visit(WhileStmt whileStmt);

    void visit(BreakStmt breakStmt);

    void visit(TryCatchStmt tryCatchStmt);

    void visit(ThrowStmt throwStmt);

    void visit(FunctionInvocationStmt funcIStmt);

    void visit(ActionInvocationStmt actionIStmt);

    void visit(WorkerInvocationStmt workerInvocationStmt);

    void visit(WorkerReplyStmt workerReplyStmt);

    void visit(ReturnStmt returnStmt);

    void visit(ReplyStmt replyStmt);

    void visit(ForkJoinStmt forkJoinStmt);

    BValue[] visit(FunctionInvocationExpr funcIExpr);

    BValue[] visit(ActionInvocationExpr actionIExpr);

    BValue[] visit(ResourceInvocationExpr resourceIExpr);

    BValue visit(InstanceCreationExpr instanceCreationExpr);

    BValue visit(UnaryExpression unaryExpr);

    BValue visit(BinaryExpression binaryExpr);

    BValue visit(ArrayMapAccessExpr arrayMapAccessExpr);

    BValue visit(StructFieldAccessExpr structAttributeAccessExpr);

    BValue visit(ArrayInitExpr arrayInitExpr);

    BValue visit(RefTypeInitExpr refTypeInitExpr);

    BValue visit(ConnectorInitExpr connectorInitExpr);

    BValue visit(BacktickExpr backtickExpr);

    BValue visit(StructInitExpr structInitExpr);

    BValue visit(MapInitExpr mapInitExpr);

    BValue visit(VariableRefExpr variableRefExpr);

    BValue visit(TypeCastExpression typeCastExpression);

    BValue visit(BasicLiteral basicLiteral);

    BValue visit(StackVarLocation stackVarLocation);

    BValue visit(ConstantLocation constantLocation);

    BValue visit(ServiceVarLocation serviceVarLocation);

    BValue visit(ConnectorVarLocation connectorVarLocation);

    BValue visit(StructVarLocation structVarLocation);

    BValue visit(WorkerVarLocation workerVarLocation);
}

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
package org.wso2.ballerina.core.model;

import org.wso2.ballerina.core.interpreter.ConnectorVarLocation;
import org.wso2.ballerina.core.interpreter.ConstantLocation;
import org.wso2.ballerina.core.interpreter.ServiceVarLocation;
import org.wso2.ballerina.core.interpreter.StackVarLocation;
import org.wso2.ballerina.core.interpreter.StructVarLocation;
import org.wso2.ballerina.core.interpreter.WorkerVarLocation;
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
import org.wso2.ballerina.core.model.expressions.RefTypeInitExpr;
import org.wso2.ballerina.core.model.expressions.ResourceInvocationExpr;
import org.wso2.ballerina.core.model.expressions.StructFieldAccessExpr;
import org.wso2.ballerina.core.model.expressions.StructInitExpr;
import org.wso2.ballerina.core.model.expressions.TypeCastExpression;
import org.wso2.ballerina.core.model.expressions.UnaryExpression;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.statements.ActionInvocationStmt;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.BreakStmt;
import org.wso2.ballerina.core.model.statements.ForkJoinStmt;
import org.wso2.ballerina.core.model.statements.FunctionInvocationStmt;
import org.wso2.ballerina.core.model.statements.IfElseStmt;
import org.wso2.ballerina.core.model.statements.ReplyStmt;
import org.wso2.ballerina.core.model.statements.ReturnStmt;
import org.wso2.ballerina.core.model.statements.ThrowStmt;
import org.wso2.ballerina.core.model.statements.TryCatchStmt;
import org.wso2.ballerina.core.model.statements.VariableDefStmt;
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.model.statements.WorkerInvocationStmt;
import org.wso2.ballerina.core.model.statements.WorkerReplyStmt;
import org.wso2.ballerina.core.model.values.BValue;

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

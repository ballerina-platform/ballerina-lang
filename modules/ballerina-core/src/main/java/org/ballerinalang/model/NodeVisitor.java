/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.model.expressions.AddExpression;
import org.ballerinalang.model.expressions.AndExpression;
import org.ballerinalang.model.expressions.ArrayInitExpr;
import org.ballerinalang.model.expressions.ArrayMapAccessExpr;
import org.ballerinalang.model.expressions.BacktickExpr;
import org.ballerinalang.model.expressions.BasicLiteral;
import org.ballerinalang.model.expressions.ConnectorInitExpr;
import org.ballerinalang.model.expressions.DivideExpr;
import org.ballerinalang.model.expressions.EqualExpression;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.expressions.GreaterEqualExpression;
import org.ballerinalang.model.expressions.GreaterThanExpression;
import org.ballerinalang.model.expressions.InstanceCreationExpr;
import org.ballerinalang.model.expressions.LessEqualExpression;
import org.ballerinalang.model.expressions.LessThanExpression;
import org.ballerinalang.model.expressions.MapInitExpr;
import org.ballerinalang.model.expressions.MapStructInitKeyValueExpr;
import org.ballerinalang.model.expressions.ModExpression;
import org.ballerinalang.model.expressions.MultExpression;
import org.ballerinalang.model.expressions.NotEqualExpression;
import org.ballerinalang.model.expressions.OrExpression;
import org.ballerinalang.model.expressions.RefTypeInitExpr;
import org.ballerinalang.model.expressions.ResourceInvocationExpr;
import org.ballerinalang.model.expressions.StructFieldAccessExpr;
import org.ballerinalang.model.expressions.StructInitExpr;
import org.ballerinalang.model.expressions.SubtractExpression;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.expressions.UnaryExpression;
import org.ballerinalang.model.expressions.VariableRefExpr;
import org.ballerinalang.model.invokers.MainInvoker;
import org.ballerinalang.model.statements.ActionInvocationStmt;
import org.ballerinalang.model.statements.AssignStmt;
import org.ballerinalang.model.statements.BlockStmt;
import org.ballerinalang.model.statements.BreakStmt;
import org.ballerinalang.model.statements.CommentStmt;
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

/**
 * {@code NodeVisitor} responsible for executing a Ballerina applications by traversing statements and expressions.
 *
 * @since 0.8.0
 */
public interface NodeVisitor {

    void visit(BLangProgram bLangProgram);

    void visit(BLangPackage bLangPackage);

    void visit(BallerinaFile bFile);

    void visit(ImportPackage importPkg);

    void visit(ConstDef constant);

    void visit(Service service);

    void visit(BallerinaConnectorDef connector);

    void visit(Resource resource);

    void visit(BallerinaFunction function);

    void visit(BTypeMapper typeMapper);

    void visit(BallerinaAction action);

    void visit(Worker worker);

    void visit(Annotation annotation);

    void visit(ParameterDef parameterDef);

    void visit(VariableDef variableDef);

    void visit(StructDef structDef);


    // Statements

    void visit(VariableDefStmt varDefStmt);

    void visit(AssignStmt assignStmt);

    void visit(BlockStmt blockStmt);

    void visit(CommentStmt commentStmt);

    void visit(IfElseStmt ifElseStmt);

    void visit(ReplyStmt replyStmt);

    void visit(ReturnStmt returnStmt);

    void visit(WhileStmt whileStmt);

    void visit(BreakStmt breakStmt);

    void visit(TryCatchStmt tryCatchStmt);

    void visit(ThrowStmt throwStmt);

    void visit(FunctionInvocationStmt functionInvocationStmt);

    void visit(ActionInvocationStmt actionInvocationStmt);

    void visit(WorkerInvocationStmt workerInvocationStmt);

    void visit(WorkerReplyStmt workerReplyStmt);

    void visit(ForkJoinStmt forkJoinStmt);

    // Expressions

    void visit(AddExpression addExpr);

    void visit(AndExpression andExpression);

    void visit(BasicLiteral basicLiteral);

    void visit(DivideExpr divideExpr);

    void visit(ModExpression modExpression);

    void visit(EqualExpression equalExpression);

    void visit(FunctionInvocationExpr functionInvocationExpr);

    void visit(ActionInvocationExpr actionInvocationExpr);

    void visit(GreaterEqualExpression greaterEqualExpression);

    void visit(GreaterThanExpression greaterThanExpression);

    void visit(LessEqualExpression lessEqualExpression);

    void visit(LessThanExpression lessThanExpression);

    void visit(MultExpression multExpression);

    void visit(InstanceCreationExpr instanceCreationExpr);

    void visit(NotEqualExpression notEqualExpression);

    void visit(OrExpression orExpression);

    void visit(SubtractExpression subtractExpression);

    void visit(UnaryExpression unaryExpression);

    void visit(TypeCastExpression typeCastExpression);

    void visit(ArrayMapAccessExpr arrayMapAccessExpr);

    void visit(StructFieldAccessExpr structAttributeAccessExpr);

    void visit(BacktickExpr backtickExpr);

    void visit(ArrayInitExpr arrayInitExpr);

    void visit(RefTypeInitExpr refTypeInitExpr);

    void visit(ConnectorInitExpr connectorInitExpr);

    void visit(StructInitExpr structInitExpr);

    void visit(MapInitExpr mapInitExpr);

    void visit(MapStructInitKeyValueExpr keyValueExpr);

    void visit(VariableRefExpr variableRefExpr);


    void visit(StackVarLocation stackVarLocation);

    void visit(ServiceVarLocation serviceVarLocation);

    void visit(ConnectorVarLocation connectorVarLocation);

    void visit(ConstantLocation constantLocation);

    void visit(StructVarLocation structVarLocation);

    void visit(ResourceInvocationExpr resourceIExpr);

    void visit(MainInvoker mainInvoker);

    void visit(WorkerVarLocation workerVarLocation);
}

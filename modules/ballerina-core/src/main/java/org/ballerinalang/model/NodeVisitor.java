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

import org.ballerinalang.model.expressions.ActionInvocationExpr;
import org.ballerinalang.model.expressions.AddExpression;
import org.ballerinalang.model.expressions.AndExpression;
import org.ballerinalang.model.expressions.ArrayInitExpr;
import org.ballerinalang.model.expressions.ArrayLengthExpression;
import org.ballerinalang.model.expressions.ArrayMapAccessExpr;
import org.ballerinalang.model.expressions.BasicLiteral;
import org.ballerinalang.model.expressions.ConnectorInitExpr;
import org.ballerinalang.model.expressions.DivideExpr;
import org.ballerinalang.model.expressions.EqualExpression;
import org.ballerinalang.model.expressions.FieldAccessExpr;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.expressions.GreaterEqualExpression;
import org.ballerinalang.model.expressions.GreaterThanExpression;
import org.ballerinalang.model.expressions.InstanceCreationExpr;
import org.ballerinalang.model.expressions.JSONArrayInitExpr;
import org.ballerinalang.model.expressions.JSONFieldAccessExpr;
import org.ballerinalang.model.expressions.JSONInitExpr;
import org.ballerinalang.model.expressions.KeyValueExpr;
import org.ballerinalang.model.expressions.LessEqualExpression;
import org.ballerinalang.model.expressions.LessThanExpression;
import org.ballerinalang.model.expressions.MapInitExpr;
import org.ballerinalang.model.expressions.ModExpression;
import org.ballerinalang.model.expressions.MultExpression;
import org.ballerinalang.model.expressions.NotEqualExpression;
import org.ballerinalang.model.expressions.NullLiteral;
import org.ballerinalang.model.expressions.OrExpression;
import org.ballerinalang.model.expressions.RefTypeInitExpr;
import org.ballerinalang.model.expressions.StructInitExpr;
import org.ballerinalang.model.expressions.SubtractExpression;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.expressions.TypeConversionExpr;
import org.ballerinalang.model.expressions.UnaryExpression;
import org.ballerinalang.model.expressions.VariableRefExpr;
import org.ballerinalang.model.statements.AbortStmt;
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
import org.ballerinalang.model.statements.TransactionStmt;
import org.ballerinalang.model.statements.TransformStmt;
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

    void visit(GlobalVariableDef globalVar);

    void visit(Service service);

    void visit(BallerinaConnectorDef connector);

    void visit(Resource resource);

    void visit(BallerinaFunction function);

    void visit(BTypeMapper typeMapper);

    void visit(BallerinaAction action);

    void visit(Worker worker);

    void visit(AnnotationAttachment annotation);

    void visit(ParameterDef parameterDef);

    void visit(VariableDef variableDef);

    void visit(StructDef structDef);
    
    void visit(AnnotationAttributeDef annotationAttributeDef);

    void visit(AnnotationDef annotationDef);

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

    void visit(TransformStmt transformStmt);

    void visit(TransactionStmt transactionStmt);

    void visit(AbortStmt abortStmt);

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
    
    void visit(TypeConversionExpr typeConversionExpression);

    void visit(ArrayMapAccessExpr arrayMapAccessExpr);

    void visit(ArrayLengthExpression arrayLengthExpression);
    
    void visit(FieldAccessExpr structAttributeAccessExpr);
    
    void visit(JSONFieldAccessExpr jsonPathExpr);

    void visit(ArrayInitExpr arrayInitExpr);

    void visit(RefTypeInitExpr refTypeInitExpr);

    void visit(ConnectorInitExpr connectorInitExpr);

    void visit(StructInitExpr structInitExpr);
    
    void visit(MapInitExpr mapInitExpr);

    void visit(JSONInitExpr jsonInitExpr);
    
    void visit(JSONArrayInitExpr jsonArrayInitExpr);

    void visit(KeyValueExpr keyValueExpr);

    void visit(VariableRefExpr variableRefExpr);

    void visit(NullLiteral nullLiteral);
}

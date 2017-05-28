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

package org.ballerinalang.composer.service.workspace.rest.datamodel;

import org.ballerinalang.bre.ConnectorVarLocation;
import org.ballerinalang.bre.ConstantLocation;
import org.ballerinalang.bre.GlobalVarLocation;
import org.ballerinalang.bre.ServiceVarLocation;
import org.ballerinalang.bre.StackVarLocation;
import org.ballerinalang.bre.StructVarLocation;
import org.ballerinalang.bre.WorkerVarLocation;
import org.ballerinalang.model.AnnotationAttachment;
import org.ballerinalang.model.AnnotationAttributeDef;
import org.ballerinalang.model.AnnotationDef;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.BTypeMapper;
import org.ballerinalang.model.BallerinaAction;
import org.ballerinalang.model.BallerinaConnectorDef;
import org.ballerinalang.model.BallerinaFile;
import org.ballerinalang.model.BallerinaFunction;
import org.ballerinalang.model.ConstDef;
import org.ballerinalang.model.GlobalVariableDef;
import org.ballerinalang.model.ImportPackage;
import org.ballerinalang.model.NodeVisitor;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.Service;
import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.Worker;
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
import org.ballerinalang.model.expressions.Expression;
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
import org.ballerinalang.model.expressions.ResourceInvocationExpr;
import org.ballerinalang.model.expressions.StructInitExpr;
import org.ballerinalang.model.expressions.SubtractExpression;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.expressions.TypeConversionExpr;
import org.ballerinalang.model.expressions.UnaryExpression;
import org.ballerinalang.model.expressions.VariableRefExpr;
import org.ballerinalang.model.invokers.MainInvoker;
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
import org.ballerinalang.model.statements.Statement;
import org.ballerinalang.model.statements.ThrowStmt;
import org.ballerinalang.model.statements.TransactionRollbackStmt;
import org.ballerinalang.model.statements.TransformStmt;
import org.ballerinalang.model.statements.TryCatchStmt;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.statements.WhileStmt;
import org.ballerinalang.model.statements.WorkerInvocationStmt;
import org.ballerinalang.model.statements.WorkerReplyStmt;
import org.ballerinalang.model.types.BTypes;
import java.util.Stack;


/**
 * Serializes ballerina statement/expression java object to a line of string.
 */
public class BLangExpressionModelBuilder implements NodeVisitor {
    
    private static final String SPACE_CHAR = " ";
    private Stack<StringBuffer> bufferStack = new Stack<>();
    
    public StringBuffer getBuffer() {
        return bufferStack.pop();
    }
    
    public BLangExpressionModelBuilder() {
    }
    
    @Override
    public void visit(BLangProgram bLangProgram) {
        
    }
    
    @Override
    public void visit(BLangPackage bLangPackage) {
        
    }
    
    @Override
    public void visit(BallerinaFile bFile) {
    }
    
    @Override
    public void visit(ImportPackage importPackage) {
    }
    
    @Override
    public void visit(Service service) {
    }
    
    @Override
    public void visit(BallerinaConnectorDef connector) {
    }
    
    @Override
    public void visit(Resource resource) {
    }
    
    @Override
    public void visit(BallerinaFunction function) {
    }
    
    @Override
    public void visit(BTypeMapper typeMapper) {
        
    }
    
    @Override
    public void visit(BallerinaAction action) {
    }
    
    @Override
    public void visit(Worker worker) {
    }

    @Override
    public void visit(AnnotationAttachment annotation) {

    }

    @Override
    public void visit(ParameterDef parameterDef) {
    }
    
    //    @Override
    //    public void visit(ConnectorDcl connectorDcl) {
    //        StringBuffer buffer = new StringBuffer();
    //        bufferStack.push(buffer);
    //        buffer.append(connectorDcl.getConnectorName()).append(SPACE_CHAR)
    //                .append(connectorDcl.getVarName().getName()).append(SPACE_CHAR).append("=")
    //                .append(SPACE_CHAR).append("new").append(SPACE_CHAR)
    //                .append(connectorDcl.getConnectorName()).append("(");
    //        boolean isFirstItr = true;
    //        for (Expression expr : connectorDcl.getArgExprs()) {
    //            if (!isFirstItr) {
    //                buffer.append(",");
    //            } else {
    //                isFirstItr = false;
    //            }
    //            expr.accept(this);
    //            buffer.append(bufferStack.peek());
    //            bufferStack.pop();
    //        }
    //        buffer.append(SPACE_CHAR).append(");");
    //    }
    
    @Override
    public void visit(VariableDef variableDef) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        buffer.append(variableDef.getType().toString()).append(SPACE_CHAR).append(variableDef.getName()).append(";");
    }
    
    @Override
    public void visit(BlockStmt blockStmt) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        buffer.append("\n{\n");
        for (Statement statement : blockStmt.getStatements()) {
            statement.accept(this);
            buffer.append(bufferStack.peek());
            bufferStack.pop();
            buffer.append("\n");
        }
        buffer.append("}\n");
    }
    
    @Override
    public void visit(AssignStmt assignStmt) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        assignStmt.getLExprs()[0].accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
        buffer.append(" = ");
        assignStmt.getRExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
        buffer.append(";");
    }
    
    @Override
    public void visit(CommentStmt commentStmt) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        buffer.append("//").append(commentStmt.getComment());
    }
    
    @Override
    public void visit(IfElseStmt ifElseStmt) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        ifElseStmt.getCondition().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
        if (ifElseStmt.getThenBody() != null) {
            ifElseStmt.getThenBody().accept(this);
        }
        buffer.append(bufferStack.peek());
        bufferStack.pop();
        if (ifElseStmt.getElseBody() != null) {
            ifElseStmt.getElseBody().accept(this);
        }
        buffer.append(bufferStack.peek());
        bufferStack.pop();
    }
    
    @Override
    public void visit(WhileStmt whileStmt) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        buffer.append("while (");
        whileStmt.getCondition().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
        buffer.append(" ) ");
        if (whileStmt.getBody() != null) {
            whileStmt.getBody().accept(this);
        }
        buffer.append(bufferStack.peek());
        bufferStack.pop();
    }
    
    @Override
    public void visit(BreakStmt breakStmt) {
        
    }
    
    @Override
    public void visit(TryCatchStmt tryCatchStmt) {
        
    }
    
    @Override
    public void visit(ThrowStmt throwStmt) {
        
    }
    
    @Override
    public void visit(FunctionInvocationStmt functionInvocationStmt) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        functionInvocationStmt.getFunctionInvocationExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
        buffer.append(";");
    }
    
    @Override
    public void visit(ActionInvocationStmt actionInvocationStmt) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        actionInvocationStmt.getActionInvocationExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
        buffer.append(";");
    }
    
    @Override
    public void visit(WorkerInvocationStmt workerInvocationStmt) {
        
    }
    
    @Override
    public void visit(WorkerReplyStmt workerReplyStmt) {
        
    }
    
    @Override
    public void visit(ForkJoinStmt forkJoinStmt) {
        
    }
    
    @Override
    public void visit(ReplyStmt replyStmt) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        replyStmt.getReplyExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
    }
    
    @Override
    public void visit(ReturnStmt returnStmt) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        buffer.append("reply ");
        boolean isFirstItr = true;
        for (Expression expression : returnStmt.getExprs()) {
            if (!isFirstItr) {
                buffer.append(" , ");
            }
            expression.accept(this);
            buffer.append(bufferStack.peek());
            bufferStack.pop();
        }
        buffer.append(" ;");
    }
    
    @Override
    public void visit(FunctionInvocationExpr funcIExpr) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        if (funcIExpr.getPackageName() != null) {
            buffer.append(funcIExpr.getPackageName()).append(":");
        }
        boolean isFirstItr = true;
        buffer.append(funcIExpr.getName()).append("( ");
        if (funcIExpr.getArgExprs() != null) {
            if (!isFirstItr) {
                
            } else {
                isFirstItr = false;
            }
            for (Expression expression : funcIExpr.getArgExprs()) {
                expression.accept(this);
            }
        }
        buffer.append(" )");
    }
    
    @Override
    public void visit(ActionInvocationExpr actionIExpr) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        boolean isFirstItr = true;
        if (actionIExpr.getPackageName() != null) {
            buffer.append(actionIExpr.getPackageName()).append(":");
        }
        buffer.append(actionIExpr.getConnectorName()).append(".").append(actionIExpr.getName()).append("( ");
        if (actionIExpr.getArgExprs() != null) {
            if (!isFirstItr) {
                
            } else {
                isFirstItr = false;
            }
            for (Expression expression : actionIExpr.getArgExprs()) {
                expression.accept(this);
            }
        }
        buffer.append(" )");
    }
    
    @Override
    public void visit(BasicLiteral basicLiteral) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        if (basicLiteral.getType() == BTypes.typeString) {
            buffer.append("\"").append(basicLiteral.getBValue().stringValue()).append("\"");
        } else {
            buffer.append(basicLiteral.getBValue().stringValue());
        }
    }
    
    @Override
    public void visit(UnaryExpression unaryExpression) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        buffer.append(unaryExpression.getOperator().toString()).append(SPACE_CHAR);
        unaryExpression.getRExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
    }
    
    @Override
    public void visit(AddExpression addExpr) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        addExpr.getLExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
        buffer.append(" + ");
        addExpr.getRExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
    }
    
    @Override
    public void visit(SubtractExpression subExpr) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        subExpr.getLExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
        buffer.append(" - ");
        subExpr.getRExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
    }
    
    @Override
    public void visit(MultExpression multExpr) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        multExpr.getLExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
        buffer.append(" x ");
        multExpr.getRExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
    }
    
    @Override
    public void visit(DivideExpr divideExpr) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        divideExpr.getLExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
        buffer.append(" % ");
        divideExpr.getRExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
    }
    
    @Override
    public void visit(AndExpression andExpr) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        andExpr.getLExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
        buffer.append(" && ");
        andExpr.getRExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
    }
    
    @Override
    public void visit(OrExpression orExpr) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        orExpr.getLExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
        buffer.append(" || ");
        orExpr.getRExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
    }
    
    @Override
    public void visit(EqualExpression equalExpr) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        equalExpr.getLExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
        buffer.append(" == ");
        equalExpr.getRExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
    }
    
    @Override
    public void visit(NotEqualExpression notEqualExpression) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        notEqualExpression.getLExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
        buffer.append(" != ");
        notEqualExpression.getRExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
    }
    
    @Override
    public void visit(GreaterEqualExpression greaterEqualExpression) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        greaterEqualExpression.getLExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
        buffer.append(" >= ");
        greaterEqualExpression.getRExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
    }
    
    @Override
    public void visit(GreaterThanExpression greaterThanExpression) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        greaterThanExpression.getLExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
        buffer.append(" > ");
        greaterThanExpression.getRExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
    }
    
    @Override
    public void visit(LessEqualExpression lessEqualExpression) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        lessEqualExpression.getLExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
        buffer.append(" <= ");
        lessEqualExpression.getRExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
    }
    
    @Override
    public void visit(LessThanExpression lessThanExpression) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        lessThanExpression.getLExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
        buffer.append(" < ");
        lessThanExpression.getRExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
    }
    
    @Override
    public void visit(VariableRefExpr variableRefExpr) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        buffer.append(variableRefExpr.getSymbolName().getName());
    }

    @Override
    public void visit(TypeConversionExpr typeConversionExpression){

    }

    @Override
    public void visit(NullLiteral nullLiteral) {

    }
    
    @Override
    public void visit(TypeCastExpression typeCastExpression) {
        
    }

    @Override
    public void visit(ArrayInitExpr arrayInitExpr) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        buffer.append("[ ");
        if (arrayInitExpr.getArgExprs() != null) {
            for (Expression expression : arrayInitExpr.getArgExprs()) {
                expression.accept(this);
                buffer.append(bufferStack.peek());
                bufferStack.pop();
            }
        }
        buffer.append(" ]");
    }
    
    @Override
    public void visit(BacktickExpr backtickExpr) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        buffer.append("'" + backtickExpr.getTemplateStr() + "'").append(";");
    }
    
    @Override
    public void visit(InstanceCreationExpr instanceCreationExpr) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        buffer.append("new ");
        instanceCreationExpr.getRExpr().accept(this);
        buffer.append(bufferStack.peek());
        bufferStack.pop();
    }
    
    @Override
    public void visit(MainInvoker mainInvoker) {
        //TODO
    }
    
    @Override
    public void visit(ResourceInvocationExpr resourceInvokerExpr) {
        //TODO
    }
    
    @Override
    public void visit(RefTypeInitExpr refTypeInitExpr) {
        //TODO
    }
    
    @Override
    public void visit(ConnectorInitExpr connectorInitExpr) {
        
    }
    
    @Override
    public void visit(ConstantLocation constantLocation) {
        //TODO
    }
    
    @Override
    public void visit(StackVarLocation stackVarLocation) {
        //TODO
    }
    
    @Override
    public void visit(ConnectorVarLocation connectorVarLocation) {
        //TODO
    }
    
    @Override
    public void visit(ServiceVarLocation serviceVarLocation) {
        //TODO
    }

    @Override
    public void visit(ConstDef constant) {
        //TODO
    }

    @Override
    public void visit(ArrayMapAccessExpr arrayMapAccessExpr) {
        //TODO
    }
    
    @Override
    public void visit(KeyValueExpr keyValueExpr) {
        //TODO
    }
    
    @Override
    public void visit(StructDef structDef) {
        //TODO
    }

    @Override
    public void visit(AnnotationAttributeDef annotationAttributeDef) {

    }

    @Override
    public void visit(AnnotationDef annotationDef) {

    }

    @Override
    public void visit(VariableDefStmt varDefStmt) {
        
    }
    
    @Override
    public void visit(StructVarLocation structVarLocation) {
        //TODO
    }
    
    @Override
    public void visit(WorkerVarLocation workerVarLocation) {
        
    }
    
    @Override
    public void visit(StructInitExpr structInitExpr) {
        //TODO
    }
    
    @Override
    public void visit(MapInitExpr mapInitExpr) {
        
    }

    @Override
    public void visit(FieldAccessExpr fieldAccessExpr) {
        //TODO
    }

    @Override
    public void visit(ModExpression modExpression) {
        
    }

    @Override
    public void visit(GlobalVarLocation globalVarLocation) {

    }

    @Override
    public void visit(GlobalVariableDef globalVariableDef) {

    }

    @Override
    public void visit(TransformStmt transformStmt) {

    }

    @Override
    public void visit(TransactionRollbackStmt transactionRollbackStmt) {

    }

    @Override
    public void visit(AbortStmt abortStmt) {

    }

    @Override
    public void visit(JSONInitExpr jsonInitExpr) {

    }

    @Override
    public void visit(JSONArrayInitExpr jsonArrayInitExpr) {

    }

    @Override
    public void visit(JSONFieldAccessExpr jsonFieldAccessExpr) {

    }
}

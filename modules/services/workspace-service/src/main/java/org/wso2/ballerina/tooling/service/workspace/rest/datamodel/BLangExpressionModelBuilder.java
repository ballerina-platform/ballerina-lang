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

package org.wso2.ballerina.tooling.service.workspace.rest.datamodel;

import org.wso2.ballerina.core.interpreter.*;
import org.wso2.ballerina.core.model.*;
import org.wso2.ballerina.core.model.expressions.*;
import org.wso2.ballerina.core.interpreter.ConnectorVarLocation;
import org.wso2.ballerina.core.interpreter.ConstantLocation;
import org.wso2.ballerina.core.interpreter.StackVarLocation;
import org.wso2.ballerina.core.interpreter.ServiceVarLocation;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BTypeMapper;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaConnectorDef;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.ConnectorDcl;
import org.wso2.ballerina.core.model.ConstDef;
import org.wso2.ballerina.core.model.ImportPackage;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.VariableDef;
import org.wso2.ballerina.core.model.Worker;
import org.wso2.ballerina.core.model.invokers.MainInvoker;
import org.wso2.ballerina.core.model.statements.ActionInvocationStmt;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.CommentStmt;
import org.wso2.ballerina.core.model.statements.FunctionInvocationStmt;
import org.wso2.ballerina.core.model.statements.IfElseStmt;
import org.wso2.ballerina.core.model.statements.ReplyStmt;
import org.wso2.ballerina.core.model.statements.ReturnStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.statements.VariableDefStmt;
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.model.statements.BreakStmt;
import org.wso2.ballerina.core.model.statements.TryCatchStmt;
import org.wso2.ballerina.core.model.statements.WorkerInvocationStmt;
import org.wso2.ballerina.core.model.statements.ThrowStmt;
import org.wso2.ballerina.core.model.statements.WorkerReplyStmt;
import org.wso2.ballerina.core.model.statements.ForkJoinStmt;
import org.wso2.ballerina.core.model.types.BTypes;

import java.util.Stack;


/**
 * Serializes ballerina statement/expression java object to a line of string.
 */
public class BLangExpressionModelBuilder implements NodeVisitor {

    private String SPACE_CHAR = " ";
    private Stack<StringBuffer> bufferStack = new Stack<>();

    public StringBuffer getBuffer(){
        return bufferStack.pop();
    }

    public BLangExpressionModelBuilder() {}

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
    public void visit(Annotation annotation) {
    }

    @Override
    public void visit(ParameterDef parameterDef) {
    }

    @Override
    public void visit(ConnectorDcl connectorDcl) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        buffer.append(connectorDcl.getConnectorName()).append(SPACE_CHAR)
                .append(connectorDcl.getVarName().getName()).append(SPACE_CHAR).append("=")
                .append(SPACE_CHAR).append("new").append(SPACE_CHAR)
                .append(connectorDcl.getConnectorName()).append("(");
        boolean isFirstItr = true;
        for (Expression expr : connectorDcl.getArgExprs()) {
            if (!isFirstItr) {
                buffer.append(",");
            } else {
                isFirstItr = false;
            }
            expr.accept(this);
            buffer.append(bufferStack.peek());
            bufferStack.pop();
        }
        buffer.append(SPACE_CHAR).append(");");
    }

    @Override
    public void visit(VariableDef variableDef) {
        StringBuffer buffer = new StringBuffer();
        bufferStack.push(buffer);
        buffer.append(variableDef.getType().toString()).append(SPACE_CHAR)
                .append(variableDef.getName()).append(";");
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
            if(!isFirstItr){
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
        if(funcIExpr.getPackageName() != null){
            buffer.append(funcIExpr.getPackageName()).append(":");
        }
        boolean isFirstItr = true;
        buffer.append(funcIExpr.getName()).append("( ");
        if (funcIExpr.getArgExprs() != null) {
            if(!isFirstItr){

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
        if(actionIExpr.getPackageName() != null){
            buffer.append(actionIExpr.getPackageName()).append(":");
        }
        buffer.append(actionIExpr.getConnectorName())
                .append(".").append(actionIExpr.getName()).append("( ");
        if (actionIExpr.getArgExprs() != null) {
            if(!isFirstItr){

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
        if(basicLiteral.getType() == BTypes.typeString) {
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
        buffer.append("'"+ backtickExpr.getTemplateStr()+"'").append(";");
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
    public void visit(MapStructInitKeyValueExpr arrayMapAccessExpr) {
        //TODO
    }

    @Override
    public void visit(StructDef structDef) {
        //TODO
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
    public void visit(StructFieldAccessExpr structFieldAccessExpr) {
        //TODO
    }

    @Override
    public void visit(ModExpression modExpression) {
        
    }

}

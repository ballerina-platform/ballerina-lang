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
package org.wso2.ballerina.core.interpreter;

import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaConnector;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.ConnectorDcl;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.Worker;
import org.wso2.ballerina.core.model.expressions.AddExpression;
import org.wso2.ballerina.core.model.expressions.AndExpression;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.EqualExpression;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.GreaterEqualExpression;
import org.wso2.ballerina.core.model.expressions.GreaterThanExpression;
import org.wso2.ballerina.core.model.expressions.LessEqualExpression;
import org.wso2.ballerina.core.model.expressions.LessThanExpression;
import org.wso2.ballerina.core.model.expressions.MultExpression;
import org.wso2.ballerina.core.model.expressions.NotEqualExpression;
import org.wso2.ballerina.core.model.expressions.OrExpression;
import org.wso2.ballerina.core.model.expressions.SubtractExpression;
import org.wso2.ballerina.core.model.expressions.UnaryExpression;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.CommentStmt;
import org.wso2.ballerina.core.model.statements.IfElseStmt;
import org.wso2.ballerina.core.model.statements.ReplyStmt;
import org.wso2.ballerina.core.model.statements.ReturnStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.model.types.TypeC;
import org.wso2.ballerina.core.model.values.BValueRef;

import java.util.List;

/**
 * {@code BLangInterpreter} executes a Ballerina program
 *
 * @since 1.0.0
 */
public class BLangInterpreter implements NodeVisitor {

    private Context bContext;
    private ControlStack controlStack;

    public BLangInterpreter(Context bContext) {
        this.bContext = bContext;
        this.controlStack = bContext.getControlStack();
    }

    @Override
    public void visit(BallerinaFile bFile) {

    }

    @Override
    public void visit(Service service) {

    }

    @Override
    public void visit(BallerinaConnector connector) {

    }

    @Override
    public void visit(Resource resource) {
        resource.getResourceBody().accept(this);
    }

    @Override
    public void visit(BallerinaFunction function) {
        function.getFunctionBody().accept(this);
    }

    @Override
    public void visit(BallerinaAction action) {

    }

    @Override
    public void visit(Worker worker) {
        List<Statement> stmts = worker.getStatements();
        for (Statement stmt : stmts) {
            stmt.accept(this);
        }
    }

    @Override
    public void visit(Annotation annotation) {

    }

    @Override
    public void visit(Parameter parameter) {

    }

    @Override
    public void visit(ConnectorDcl connectorDcl) {

    }

    @Override
    public void visit(VariableDcl variableDcl) {

    }

    @Override
    public void visit(BlockStmt blockStmt) {
        //TODO Improve this to support non-blocking behaviour.
        //TODO Possibly a linked set of statements would do.

        Statement[] stmts = blockStmt.getStatements();
        for (Statement stmt : stmts) {
            stmt.accept(this);
        }
    }

    @Override
    public void visit(AssignStmt assignStmt) {
        Expression rExpr = assignStmt.getRExpr();
        rExpr.accept(this);

        Expression lExpr = assignStmt.getLExpr();
        lExpr.accept(this);

        BValueRef rValue = getValue(rExpr);
        BValueRef lValue = getValue(lExpr);

        lValue.setBValue(rValue.getBValue());

        // TODO this optional .. we need think about this BValueRef thing again
        setValue(lExpr, lValue);
    }

    @Override
    public void visit(CommentStmt commentStmt) {

    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        Expression expr = ifElseStmt.getCondition();
        expr.accept(this);
        BValueRef result = getValue(expr);

        if (result.getBoolean()) {
            ifElseStmt.getThenBody().accept(this);
            return;
        }

        for (IfElseStmt.ElseIfBlock elseIfBlock : ifElseStmt.getElseIfBlocks()) {
            Expression elseIfCondition = elseIfBlock.getElseIfCondition();
            elseIfCondition.accept(this);
            result = getValue(elseIfCondition);

            if (result.getBoolean()) {
                elseIfBlock.getElseIfBody().accept(this);
                return;
            }
        }

        Statement elseBody = ifElseStmt.getElseBody();
        if (elseBody != null) {
            elseBody.accept(this);
        }
    }

    @Override
    public void visit(WhileStmt whileStmt) {
        Expression expr = whileStmt.getCondition();
        expr.accept(this);
        BValueRef result = getValue(expr);

        while (result.getBoolean()) {
            // Interpret the statements in the while body.
            whileStmt.getBody().accept(this);

            // Now evaluate the condition again to decide whether to continue the loop or not.
            expr.accept(this);
            result = getValue(expr);
        }
    }

    @Override
    public void visit(ReplyStmt replyStmt) {

    }

    @Override
    public void visit(ReturnStmt returnStmt) {
        Expression[] exprs = returnStmt.getExprs();

        for (int i = 0; i < exprs.length; i++) {
            Expression expr = exprs[i];
            expr.accept(this);
            controlStack.setReturnValue(i, getValue(expr));
        }
    }

    @Override
    public void visit(FunctionInvocationExpr funcIExpr) {
        // Create the Stack frame
        BallerinaFunction function = (BallerinaFunction) funcIExpr.getFunction();

        int sizeOfValueArray = function.getStackFrameSize();
        BValueRef[] localVals = new BValueRef[sizeOfValueArray];

        // Create default values for all declared local variables
        int i = 0;
        for (Expression arg : funcIExpr.getExprs()) {

            // Evaluate the argument expression
            arg.accept(this);
            TypeC argType = arg.getType();
            BValueRef argValue = getValue(arg);

            // Here we need to handle value types differently from reference types
            // Value types need to be cloned before passing ot the function : pass by value.
            // TODO Implement copy-on-write mechanism to improve performance
            if (BValueRef.isValueType(argType)) {
                argValue = BValueRef.clone(argType, argValue);
            }

            // Setting argument value in the stack frame
            localVals[i] = argValue;

            i++;
        }

        // Create default values for all declared local variables
        VariableDcl[] variableDcls = function.getVariableDcls();
        for (VariableDcl variableDcl : variableDcls) {
            localVals[i] = BValueRef.getDefaultValue(variableDcl.getTypeC());
            i++;
        }

        // Create an array in the stack frame to hold return values;
        BValueRef[] rVals = new BValueRef[function.getReturnTypesC().length];

        // Create a new stack frame with memory locations to hold parameters, local values, temp expression valuse and
        // return values;
        StackFrame stackFrame = new StackFrame(localVals, rVals);
        controlStack.pushFrame(stackFrame);

        function.accept(this);

        controlStack.popFrame();

        // Setting return values to function invocation expression
        // TODO At the moment we only support single return value
        if (rVals.length >= 1) {
            controlStack.setValue(funcIExpr.getOffset(), rVals[0]);
        }
    }

    @Override
    public void visit(BasicLiteral basicLiteral) {

    }

    @Override
    public void visit(AddExpression addExpr) {
        Expression rExpr = addExpr.getRExpr();
        rExpr.accept(this);

        Expression lExpr = addExpr.getLExpr();
        lExpr.accept(this);

        BValueRef rValue = getValue(rExpr);
        BValueRef lValue = getValue(lExpr);

        BValueRef result = addExpr.getEvalFunc().apply(lValue, rValue);
        controlStack.setValue(addExpr.getOffset(), result);
    }

    @Override
    public void visit(AndExpression andExpression) {

    }

    @Override
    public void visit(EqualExpression equalExpr) {
        Expression rExpr = equalExpr.getRExpr();
        rExpr.accept(this);

        Expression lExpr = equalExpr.getLExpr();
        lExpr.accept(this);

        BValueRef rValue = getValue(rExpr);
        BValueRef lValue = getValue(lExpr);

        BValueRef result = equalExpr.getEvalFunc().apply(lValue, rValue);
        controlStack.setValue(equalExpr.getOffset(), result);
    }

    @Override
    public void visit(GreaterEqualExpression greaterEqualExpression) {

    }

    @Override
    public void visit(GreaterThanExpression greaterThanExpression) {

    }

    @Override
    public void visit(LessEqualExpression lessEqualExpression) {

    }

    @Override
    public void visit(LessThanExpression lessThanExpression) {

    }

    @Override
    public void visit(MultExpression multExpression) {

    }

    @Override
    public void visit(NotEqualExpression notEqualExpression) {

    }

    @Override
    public void visit(OrExpression orExpression) {

    }

    @Override
    public void visit(SubtractExpression subtractExpression) {

    }

    @Override
    public void visit(UnaryExpression unaryExpression) {

    }

    @Override
    public void visit(VariableRefExpr variableRefExpr) {

    }

    public BValueRef getValue(Expression expr) {
        if (expr instanceof BasicLiteral) {
            return expr.getBValueRef();
        }

        return controlStack.getValue(expr.getOffset());
    }

    public void setValue(Expression expr, BValueRef valueRef) {
        controlStack.setValue(expr.getOffset(), valueRef);
    }
}

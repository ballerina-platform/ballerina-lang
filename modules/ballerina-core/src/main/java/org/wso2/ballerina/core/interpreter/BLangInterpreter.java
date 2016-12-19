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

import org.wso2.ballerina.core.model.Action;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaConnector;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.ConnectorDcl;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.Worker;
import org.wso2.ballerina.core.model.expressions.ActionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.AddExpression;
import org.wso2.ballerina.core.model.expressions.AndExpression;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.BinaryExpression;
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
import org.wso2.ballerina.core.model.statements.FunctionInvocationStmt;
import org.wso2.ballerina.core.model.statements.IfElseStmt;
import org.wso2.ballerina.core.model.statements.ReplyStmt;
import org.wso2.ballerina.core.model.statements.ReturnStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.model.types.TypeC;
import org.wso2.ballerina.core.model.values.BValueRef;
import org.wso2.ballerina.core.model.values.ConnectorValue;
import org.wso2.ballerina.core.model.values.MessageValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeAction;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;

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
        for (Action action : connector.getActions()) {
            ((BallerinaAction) action).accept(this);
        }
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
        action.interpret(bContext);

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
    public void visit(FunctionInvocationStmt functionInvocationStmt) {
        functionInvocationStmt.getFunctionInvocationExpr().accept(this);
    }

    @Override
    public void visit(ReplyStmt replyStmt) {
        MessageValue messageValue =
                (MessageValue) controlStack.getCurrentFrame().values[replyStmt.getReplyExpr().getOffset()].getBValue();
        bContext.getBalCallback().done(messageValue.getValue());
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

    // Expressions

    @Override
    public void visit(FunctionInvocationExpr funcIExpr) {
        // Create the Stack frame
        Function function = funcIExpr.getFunction();

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

        // Check whether we are invoking a native function or not.
        if (function instanceof BallerinaFunction) {
            BallerinaFunction bFunction = (BallerinaFunction) function;
            bFunction.accept(this);
        } else {
            AbstractNativeFunction nativeFunction = (AbstractNativeFunction) function;
            nativeFunction.executeNative(bContext);
        }

        controlStack.popFrame();

        // Setting return values to function invocation expression
        // TODO At the moment we only support single return value
        if (rVals.length >= 1) {
            controlStack.setValue(funcIExpr.getOffset(), rVals[0]);
        }
    }

    // TODO Duplicate code. fix me
    @Override
    public void visit(ActionInvocationExpr actionIExpr) {
        // Create the Stack frame
        Action action = actionIExpr.getAction();

        int sizeOfValueArray = action.getStackFrameSize();
        BValueRef[] localVals = new BValueRef[sizeOfValueArray];

        // Create default values for all declared local variables
        int i = 0;
        for (Expression arg : actionIExpr.getExprs()) {

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

            if (argType == TypeC.CONNECTOR_TYPE) {
                ConnectorValue connectorValue = (ConnectorValue) argValue.getBValue();
                Connector connector = connectorValue.getValue();
                if (connector instanceof AbstractNativeConnector) {
                    AbstractNativeConnector abstractNativeConnector = (AbstractNativeConnector) connector;

                    Expression[] argExpressions = connectorValue.getArgExprs();
                    BValueRef[] bValueRefs = new BValueRef[argExpressions.length];
                    for (int j = 0; j < argExpressions.length; j++) {
                        bValueRefs[j] = argExpressions[j].evaluate(bContext);
                    }
                    abstractNativeConnector.init(bValueRefs);
                }
            }

            // Setting argument value in the stack frame
            localVals[i] = argValue;

            i++;
        }

        // Create default values for all declared local variables
        VariableDcl[] variableDcls = action.getVariableDcls();
        for (VariableDcl variableDcl : variableDcls) {
            localVals[i] = BValueRef.getDefaultValue(variableDcl.getTypeC());
            i++;
        }

        // Create an array in the stack frame to hold return values;
        BValueRef[] rVals = new BValueRef[action.getReturnTypesC().length];

        // Create a new stack frame with memory locations to hold parameters, local values, temp expression values and
        // return values;
        StackFrame stackFrame = new StackFrame(localVals, rVals);
        controlStack.pushFrame(stackFrame);

        // Check whether we are invoking a native action or not.
        if (action instanceof BallerinaAction) {
            BallerinaAction bAction = (BallerinaAction) action;
            bAction.accept(this);
        } else {
            AbstractNativeAction nativeAction = (AbstractNativeAction) action;
            nativeAction.execute(bContext);
        }

        controlStack.popFrame();

        // Setting return values to function invocation expression
        // TODO At the moment we only support single return value
        if (rVals.length >= 1) {
            controlStack.setValue(actionIExpr.getOffset(), rVals[0]);
        }
    }

    @Override
    public void visit(BasicLiteral basicLiteral) {

    }

    @Override
    public void visit(UnaryExpression unaryExpression) {

    }

    @Override
    public void visit(AddExpression addExpr) {
        visitBinaryExpr(addExpr);
    }

    @Override
    public void visit(SubtractExpression subExpr) {
        visitBinaryExpr(subExpr);
    }

    @Override
    public void visit(MultExpression multExpr) {
        visitBinaryExpr(multExpr);
    }

    @Override
    public void visit(AndExpression andExpr) {
        visitBinaryExpr(andExpr);
    }

    @Override
    public void visit(OrExpression orExpr) {
        visitBinaryExpr(orExpr);
    }

    @Override
    public void visit(EqualExpression equalExpr) {
        visitBinaryExpr(equalExpr);
    }

    @Override
    public void visit(NotEqualExpression notEqualExpression) {
        visitBinaryExpr(notEqualExpression);
    }

    @Override
    public void visit(GreaterEqualExpression greaterEqualExpression) {
       visitBinaryExpr(greaterEqualExpression);
    }

    @Override
    public void visit(GreaterThanExpression greaterThanExpression) {
        visitBinaryExpr(greaterThanExpression);
    }

    @Override
    public void visit(LessEqualExpression lessEqualExpression) {
        visitBinaryExpr(lessEqualExpression);
    }

    @Override
    public void visit(LessThanExpression lessThanExpression) {
        visitBinaryExpr(lessThanExpression);
    }

    @Override
    public void visit(VariableRefExpr variableRefExpr) {
    }

    // Private methods

    private BValueRef getValue(Expression expr) {
        if (expr instanceof BasicLiteral) {
            return expr.getBValueRef();
        }

        return controlStack.getValue(expr.getOffset());
    }

    private void setValue(Expression expr, BValueRef valueRef) {
        controlStack.setValue(expr.getOffset(), valueRef);
    }

    private void visitBinaryExpr(BinaryExpression binaryExpr) {
        Expression rExpr = binaryExpr.getRExpr();
        rExpr.accept(this);

        Expression lExpr = binaryExpr.getLExpr();
        lExpr.accept(this);

        BValueRef rValue = getValue(rExpr);
        BValueRef lValue = getValue(lExpr);

        BValueRef result = binaryExpr.getEvalFunc().apply(lValue, rValue);
        controlStack.setValue(binaryExpr.getOffset(), result);
    }
}

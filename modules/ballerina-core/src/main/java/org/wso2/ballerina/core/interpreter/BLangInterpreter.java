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

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.Action;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaConnector;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.ConnectorDcl;
import org.wso2.ballerina.core.model.Const;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.ImportPackage;
import org.wso2.ballerina.core.model.NodeVisitor;
import org.wso2.ballerina.core.model.Parameter;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.VariableDcl;
import org.wso2.ballerina.core.model.Worker;
import org.wso2.ballerina.core.model.expressions.ActionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.AddExpression;
import org.wso2.ballerina.core.model.expressions.AndExpression;
import org.wso2.ballerina.core.model.expressions.ArrayInitExpr;
import org.wso2.ballerina.core.model.expressions.ArrayMapAccessExpr;
import org.wso2.ballerina.core.model.expressions.BackquoteExpr;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.BinaryExpression;
import org.wso2.ballerina.core.model.expressions.EqualExpression;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.GreaterEqualExpression;
import org.wso2.ballerina.core.model.expressions.GreaterThanExpression;
import org.wso2.ballerina.core.model.expressions.InstanceCreationExpr;
import org.wso2.ballerina.core.model.expressions.KeyValueExpression;
import org.wso2.ballerina.core.model.expressions.LessEqualExpression;
import org.wso2.ballerina.core.model.expressions.LessThanExpression;
import org.wso2.ballerina.core.model.expressions.MapInitExpr;
import org.wso2.ballerina.core.model.expressions.MultExpression;
import org.wso2.ballerina.core.model.expressions.NotEqualExpression;
import org.wso2.ballerina.core.model.expressions.OrExpression;
import org.wso2.ballerina.core.model.expressions.SubtractExpression;
import org.wso2.ballerina.core.model.expressions.UnaryExpression;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.invokers.MainInvoker;
import org.wso2.ballerina.core.model.invokers.ResourceInvocationExpr;
import org.wso2.ballerina.core.model.statements.ActionInvocationStmt;
import org.wso2.ballerina.core.model.statements.AssignStmt;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.CommentStmt;
import org.wso2.ballerina.core.model.statements.FunctionInvocationStmt;
import org.wso2.ballerina.core.model.statements.IfElseStmt;
import org.wso2.ballerina.core.model.statements.ReplyStmt;
import org.wso2.ballerina.core.model.statements.ReturnStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.model.types.BType;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.util.BValueUtils;
import org.wso2.ballerina.core.model.values.BArray;
import org.wso2.ballerina.core.model.values.BBoolean;
import org.wso2.ballerina.core.model.values.BConnector;
import org.wso2.ballerina.core.model.values.BInteger;
import org.wso2.ballerina.core.model.values.BJSON;
import org.wso2.ballerina.core.model.values.BMap;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BString;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BValueType;
import org.wso2.ballerina.core.model.values.BXML;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeFunction;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeAction;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;

import java.util.List;

import static org.wso2.ballerina.core.runtime.Constants.SYSTEM_PROP_BAL_ARGS;

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
    public void visit(ImportPackage importPkg) {

    }

    @Override
    public void visit(Const constant) {
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
        action.getActionBody().accept(this);
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
        BValue rValue = getValue(rExpr);

        Expression lExpr = assignStmt.getLExpr();
        lExpr.accept(this);

        if (lExpr instanceof ArrayMapAccessExpr) {
            ArrayMapAccessExpr accessExpr = (ArrayMapAccessExpr) lExpr;
            if (!(accessExpr.getType() == BTypes.MAP_TYPE)) {
                BArray arrayVal = (BArray) getValue(accessExpr.getRExpr());
                BInteger indexVal = (BInteger) getValue(accessExpr.getIndexExpr());
                arrayVal.add(indexVal.intValue(), rValue);
            } else {
                BMap<BString, BValue> mapVal = new BMap<>();
                BString indexVal = (BString) getValue(accessExpr.getIndexExpr());
                mapVal.put(indexVal, rValue);
                // set the type of this expression here
                accessExpr.setType(rExpr.getType());
            }

        } else {
            setValue(lExpr, rValue);

        }
    }

    @Override
    public void visit(CommentStmt commentStmt) {

    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        Expression expr = ifElseStmt.getCondition();
        expr.accept(this);
        BBoolean condition = (BBoolean) getValue(expr);

        if (condition.booleanValue()) {
            ifElseStmt.getThenBody().accept(this);
            return;
        }

        for (IfElseStmt.ElseIfBlock elseIfBlock : ifElseStmt.getElseIfBlocks()) {
            Expression elseIfCondition = elseIfBlock.getElseIfCondition();
            elseIfCondition.accept(this);
            condition = (BBoolean) getValue(elseIfCondition);

            if (condition.booleanValue()) {
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
        BBoolean condition = (BBoolean) getValue(expr);

        while (condition.booleanValue()) {
            // Interpret the statements in the while body.
            whileStmt.getBody().accept(this);

            // Now evaluate the condition again to decide whether to continue the loop or not.
            expr.accept(this);
            condition = (BBoolean) getValue(expr);
        }
    }

    @Override
    public void visit(FunctionInvocationStmt functionInvocationStmt) {
        functionInvocationStmt.getFunctionInvocationExpr().accept(this);
    }

    @Override
    public void visit(ActionInvocationStmt actionInvocationStmt) {

    }

    @Override
    public void visit(ReplyStmt replyStmt) {
        // TODO revisit this logic
        BMessage bMessage =
                (BMessage) controlStack.getCurrentFrame().values[replyStmt.getReplyExpr().getOffset()];
        bContext.getBalCallback().done(bMessage.value());
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
    public void visit(InstanceCreationExpr instanceCreationExpr) {
        BValue bValue = instanceCreationExpr.getType().getDefaultValue();
        controlStack.setValue(instanceCreationExpr.getOffset(), bValue);
    }

    @Override
    public void visit(FunctionInvocationExpr funcIExpr) {
        // Create the Stack frame
        Function function = funcIExpr.getFunction();

        int sizeOfValueArray = function.getStackFrameSize();
        BValue[] localVals = new BValue[sizeOfValueArray];

        // Get values for all the function arguments
        int valuesCounter = populateArgumentValues(funcIExpr.getExprs(), localVals);

        // Create default values for all declared local variables
        VariableDcl[] variableDcls = function.getVariableDcls();
        for (VariableDcl variableDcl : variableDcls) {
            localVals[valuesCounter] = variableDcl.getType().getDefaultValue();
            valuesCounter++;
        }

        // Populate values for Connector declarations
        if (function instanceof BallerinaFunction) {
            BallerinaFunction ballerinaFunction = (BallerinaFunction) function;
            populateConnectorDclValues(ballerinaFunction.getConnectorDcls(), localVals, valuesCounter);
        }

        // Create an array in the stack frame to hold return values;
        BValue[] rVals = new BValue[function.getReturnTypes().length];

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

    @Override
    public void visit(ActionInvocationExpr actionIExpr) {
        // Create the Stack frame
        Action action = actionIExpr.getAction();

        BValue[] localVals = new BValue[action.getStackFrameSize()];

        // Create default values for all declared local variables
        int valueCounter = populateArgumentValues(actionIExpr.getExprs(), localVals);

        // Create default values for all declared local variables
        VariableDcl[] variableDcls = action.getVariableDcls();
        for (VariableDcl variableDcl : variableDcls) {
            localVals[valueCounter] = variableDcl.getType().getDefaultValue();
            valueCounter++;
        }

        // Populate values for Connector declarations
        if (action instanceof BallerinaAction) {
            BallerinaAction ballerinaAction = (BallerinaAction) action;
            populateConnectorDclValues(ballerinaAction.getConnectorDcls(), localVals, valueCounter);
        }

        // Create an array in the stack frame to hold return values;
        BValue[] rVals = new BValue[action.getReturnTypes().length];

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
    public void visit(ArrayMapAccessExpr arrayMapAccessExpr) {
        Expression arrayVarRefExpr = arrayMapAccessExpr.getRExpr();
        arrayVarRefExpr.accept(this);

        Expression indexExpr = arrayMapAccessExpr.getIndexExpr();
        indexExpr.accept(this);

        BValue collectionValue = getValue(arrayVarRefExpr);
        BValue indexValue  = getValue(indexExpr);

        // Check whether this collection access expression is in the left hand of an assignment expression
        // If yes skip setting the value;
        if (!arrayMapAccessExpr.isLHSExpr()) {

            if (arrayMapAccessExpr.getType() != BTypes.MAP_TYPE) {
                // Get the value stored in the index
                BValue val = ((BArray) collectionValue).get(((BInteger) indexValue).intValue());
                setValue(arrayMapAccessExpr, val);
            } else {
                // Get the value stored in the index
                BValue val;
                if (indexValue instanceof BString) {
                    val = ((BMap) collectionValue).get(indexValue);
                } else {
                    val = ((BMap) collectionValue).get(indexValue.toString());
                }
                setValue(arrayMapAccessExpr, val);
            }

        }
    }

    @Override
    public void visit(ArrayInitExpr arrayInitExpr) {
        Expression[] argExprs = arrayInitExpr.getArgExprs();

        // Creating a new array
        BArray bArray = arrayInitExpr.getType().getDefaultValue();

        for (int i = 0; i < argExprs.length; i++) {
            Expression expr = argExprs[i];
            expr.accept(this);
            bArray.add(i, getValue(expr));
        }

        setValue(arrayInitExpr, bArray);
    }

    @Override
    public void visit(MapInitExpr mapInitExpr) {
        Expression[] argExprs = mapInitExpr.getArgExprs();
        // Creating a new map
        BMap<BString, BValue> bMap = new BMap<>();

        for (int i = 0; i < argExprs.length; i++) {
            KeyValueExpression expr = (KeyValueExpression) argExprs[i];
            BString key = new BString(expr.getKey());
            Expression expression = expr.getValueExpression();
            expression.accept(this);
            bMap.put(key, getValue(expression));
        }
        setValue(mapInitExpr, bMap);
    }

    @Override
    public void visit(KeyValueExpression keyValueExpr) {

    }

    @Override
    public void visit(BackquoteExpr backquoteExpr) {
        BValue bValue;

        if (backquoteExpr.getType() == BTypes.JSON_TYPE) {
            bValue = new BJSON(backquoteExpr.getTemplateStr());
        } else {
            bValue = new BXML(backquoteExpr.getTemplateStr());
        }

        controlStack.setValue(backquoteExpr.getOffset(), bValue);
    }

    @Override
    public void visit(VariableRefExpr variableRefExpr) {
        variableRefExpr.getLocation().accept(this);
    }

    @Override
    public void visit(LocalVarLocation localVarLocation) {
//        int offset = localVarLocation.getStackFrameOffset();
    }

    @Override
    public void visit(ServiceVarLocation serviceVarLocation) {

    }

    @Override
    public void visit(ConnectorVarLocation connectorVarLocation) {

    }

    @Override
    public void visit(ConstantLocation constantLocation) {

    }

    public void visit(ResourceInvocationExpr resourceIExpr) {

        Resource resource = resourceIExpr.getResource();

        ControlStack controlStack = bContext.getControlStack();
        BValue[] valueParams = new BValue[resource.getStackFrameSize()];

        BMessage messageValue = new BMessage(bContext.getCarbonMessage());

        valueParams[0] = messageValue;

        int valuesCounter = 1;
        // Create default values for all declared local variables
        VariableDcl[] variableDcls = resource.getVariableDcls();
        for (VariableDcl variableDcl : variableDcls) {
            valueParams[valuesCounter] = variableDcl.getType().getDefaultValue();
            valuesCounter++;
        }

        // Populate values for Connector declarations
        populateConnectorDclValues(resource.getConnectorDcls(), valueParams, valuesCounter);

        BValue[] ret = new BValue[1];

        StackFrame stackFrame = new StackFrame(valueParams, ret);
        controlStack.pushFrame(stackFrame);

        resource.accept(this);
    }

    public void visit(MainInvoker mainInvoker) {

        BallerinaFunction function = mainInvoker.getMainFunction();
        Parameter[] parameters = function.getParameters();

        if (parameters.length != 1 || parameters[0].getType() != BTypes.INT_TYPE) {
            throw new BallerinaException("Main function does not comply with standard main function in ballerina");
        }

        int sizeOfValueArray = function.getStackFrameSize();
        BValue[] values = new BValue[sizeOfValueArray];
        int valuesCounter = 0;

        // Main function only have one input parameter
        // Read from command line arguments
        String balArgs = System.getProperty(SYSTEM_PROP_BAL_ARGS);

        // Only integers allowed at the moment
        if (balArgs != null) {
            int intValue = Integer.parseInt(balArgs);
            values[valuesCounter++] = new BInteger(intValue);
        } else {
            values[valuesCounter++] = new BInteger(0);
        }

        // Create default values for all declared local variables
        VariableDcl[] variableDcls = function.getVariableDcls();
        for (VariableDcl variableDcl : variableDcls) {
            values[valuesCounter] = variableDcl.getType().getDefaultValue();
            valuesCounter++;
        }

        populateConnectorDclValues(function.getConnectorDcls(), values, valuesCounter);

        BValue[] returnVals = new BValue[function.getReturnTypes().length];
        StackFrame stackFrame = new StackFrame(values, returnVals);

        controlStack.pushFrame(stackFrame);
        function.accept(this);
        controlStack.popFrame();
    }


    // Private methods

    private void populateConnectorDclValues(ConnectorDcl[] connectorDcls, BValue[] valueParams, int valuesCounter) {

        for (ConnectorDcl connectorDcl : connectorDcls) {

            Connector connector = connectorDcl.getConnector();

            Expression[] argExpressions = connectorDcl.getArgExprs();
            BValue[] bValueRefs = new BValue[argExpressions.length];
            for (int j = 0; j < argExpressions.length; j++) {
                argExpressions[j].accept(this);
                bValueRefs[j] = getValue(argExpressions[j]);
            }

            if (connector instanceof AbstractNativeConnector) {
                //TODO Fix Issue#320
                AbstractNativeConnector nativeConnector = ((AbstractNativeConnector) connector).getInstance();
                nativeConnector.init(bValueRefs);
                connector = nativeConnector;
            }
            BConnector connectorValue = new BConnector(connector, bValueRefs);

            valueParams[valuesCounter] = connectorValue;
            valuesCounter++;
        }
    }

    private BValue getValue(Expression expr) {
        if (expr instanceof BasicLiteral) {
            return ((BasicLiteral) expr).getBValue();
        }

        return controlStack.getValue(expr.getOffset());
    }

    private void setValue(Expression expr, BValue bValue) {
        controlStack.setValue(expr.getOffset(), bValue);
    }

    private void visitBinaryExpr(BinaryExpression binaryExpr) {
        Expression rExpr = binaryExpr.getRExpr();
        rExpr.accept(this);

        Expression lExpr = binaryExpr.getLExpr();
        lExpr.accept(this);

        BValueType rValue = (BValueType) getValue(rExpr);
        BValueType lValue = (BValueType) getValue(lExpr);

        BValue result = binaryExpr.getEvalFunc().apply(lValue, rValue);
        controlStack.setValue(binaryExpr.getOffset(), result);
    }

    private int populateArgumentValues(Expression[] expressions, BValue[] localVals) {
        int i = 0;
        for (Expression arg : expressions) {
            // Evaluate the argument expression
            arg.accept(this);
            BType argType = arg.getType();
            BValue argValue = getValue(arg);

            // Here we need to handle value types differently from reference types
            // Value types need to be cloned before passing ot the function : pass by value.
            // TODO Implement copy-on-write mechanism to improve performance
            if (BTypes.isValueType(argType)) {
                argValue = BValueUtils.clone(argType, argValue);
            }

            // Setting argument value in the stack frame
            localVals[i] = argValue;

            i++;
        }
        return i;
    }
}

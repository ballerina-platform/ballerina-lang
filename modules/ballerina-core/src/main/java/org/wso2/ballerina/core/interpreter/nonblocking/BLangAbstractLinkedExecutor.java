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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.CallableUnitInfo;
import org.wso2.ballerina.core.interpreter.ConnectorVarLocation;
import org.wso2.ballerina.core.interpreter.ConstantLocation;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.ControlStack;
import org.wso2.ballerina.core.interpreter.MemoryLocation;
import org.wso2.ballerina.core.interpreter.RuntimeEnvironment;
import org.wso2.ballerina.core.interpreter.ServiceVarLocation;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.interpreter.StackVarLocation;
import org.wso2.ballerina.core.interpreter.StructVarLocation;
import org.wso2.ballerina.core.model.Action;
import org.wso2.ballerina.core.model.BallerinaAction;
import org.wso2.ballerina.core.model.BallerinaConnectorDef;
import org.wso2.ballerina.core.model.Connector;
import org.wso2.ballerina.core.model.ConnectorDcl;
import org.wso2.ballerina.core.model.Function;
import org.wso2.ballerina.core.model.LinkedNodeExecutor;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.StructDcl;
import org.wso2.ballerina.core.model.StructDef;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.TypeConvertor;
import org.wso2.ballerina.core.model.VariableDef;
import org.wso2.ballerina.core.model.expressions.ActionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.ArrayInitExpr;
import org.wso2.ballerina.core.model.expressions.ArrayMapAccessExpr;
import org.wso2.ballerina.core.model.expressions.BacktickExpr;
import org.wso2.ballerina.core.model.expressions.BasicLiteral;
import org.wso2.ballerina.core.model.expressions.BinaryExpression;
import org.wso2.ballerina.core.model.expressions.ConnectorInitExpr;
import org.wso2.ballerina.core.model.expressions.Expression;
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
import org.wso2.ballerina.core.model.values.BStruct;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.model.values.BValueType;
import org.wso2.ballerina.core.model.values.BXML;
import org.wso2.ballerina.core.nativeimpl.connectors.AbstractNativeConnector;
import org.wso2.ballerina.core.runtime.Constants;

import java.util.Stack;

/**
 * {@link BLangAbstractLinkedExecutor} defines execution steps of a Ballerina program in Linked Node based execution.
 *
 * @since 0.8.0
 */
public abstract class BLangAbstractLinkedExecutor implements LinkedNodeExecutor {

    private static final Logger logger = LoggerFactory.getLogger(Constants.BAL_LINKED_INTERPRETER_LOGGER);
    protected Stack<Integer> branchIDStack;
    private RuntimeEnvironment runtimeEnv;
    private Context bContext;
    private ControlStack controlStack;

    public BLangAbstractLinkedExecutor(RuntimeEnvironment runtimeEnv, Context bContext) {
        this.runtimeEnv = runtimeEnv;
        this.bContext = bContext;
        this.controlStack = bContext.getControlStack();
        this.branchIDStack = new Stack<>();
    }

    /* Statement nodes. */

    @Override
    public void visit(ActionInvocationStmt actionIStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ActionInvocationStmt :"
                    + actionIStmt.getActionInvocationExpr().getCallableUnit().getName());
        }
        clearTempValue();
    }

    @Override
    public void visit(AssignStmt assignStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing AssignStmt {}:{}", assignStmt.getNodeLocation().getFileName(),
                    assignStmt.getNodeLocation().getLineNumber());
        }
        clearTempValue();
    }

    @Override
    public void visit(BlockStmt blockStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing BlockStmt - MultiParent={}", blockStmt.getGotoNode() != null);
        }
    }

    @Override
    public void visit(BreakStmt breakStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing BreakStmt {}:{}", breakStmt.getNodeLocation().getFileName(),
                    breakStmt.getNodeLocation().getLineNumber());
        }
        clearTempValue();
    }

    @Override
    public void visit(ForeachStmt foreachStmt) {
    }

    @Override
    public void visit(ForkJoinStmt forkJoinStmt) {
    }

    @Override
    public void visit(FunctionInvocationStmt funcIStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing FunctionInvocationStmt :" + funcIStmt.getFunctionInvocationExpr().getCallableUnit()
                    .getName());
        }
        clearTempValue();
    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing IfElseStmt {}:{}", ifElseStmt.getNodeLocation().getFileName(),
                    ifElseStmt.getNodeLocation().getLineNumber());
        }
        clearTempValue();
    }

    @Override
    public void visit(ReplyStmt replyStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ReplyStmt {}:{}", replyStmt.getNodeLocation().getFileName(),
                    replyStmt.getNodeLocation().getLineNumber());
        }
        clearTempValue();
    }

    @Override
    public void visit(ReturnStmt returnStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ReturnStmt {}:{}", returnStmt.getNodeLocation().getFileName(),
                    returnStmt.getNodeLocation().getLineNumber());
        }
        clearTempValue();
    }

    @Override
    public void visit(ThrowStmt throwStmt) {
    }

    @Override
    public void visit(TryCatchStmt tryCatchStmt) {
    }

    @Override
    public void visit(VariableDefStmt variableDefStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing VariableDefStmt {}:{}", variableDefStmt.getNodeLocation().getFileName(),
                    variableDefStmt.getNodeLocation().getLineNumber());
        }
        clearTempValue();
    }

    @Override
    public void visit(WhileStmt whileStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing WhileStmt {}:{}", whileStmt.getNodeLocation().getFileName(),
                    whileStmt.getNodeLocation().getLineNumber());
        }
        clearTempValue();
    }

    /* Expression nodes */

    @Override
    public void visit(ActionInvocationExpr actionInvocationExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ActionInvocationExpr - {}", actionInvocationExpr.getCallableUnit().getName());
        }
    }

    @Override
    public void visit(ArrayInitExpr arrayInitExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ArrayInitExpr");
        }
    }

    @Override
    public void visit(ArrayMapAccessExpr arrayMapAccessExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ArrayMapAccessExpr");
        }
    }

    @Override
    public void visit(BacktickExpr backtickExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing BacktickExpr");
        }
    }

    @Override
    public void visit(BasicLiteral basicLiteral) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing BasicLiteral \"{}\"", basicLiteral.getBValue().stringValue());
        }
        setTempResult(basicLiteral.getTempOffset(), basicLiteral.getBValue());
    }

    @Override
    public void visit(BinaryExpression expression) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing BinaryExpression");
        }
    }

    @Override
    public void visit(ConnectorInitExpr connectorInitExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ConnectorInitExpr");
        }
    }

    @Override
    public void visit(FunctionInvocationExpr functionInvocationExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing FunctionInvocationExpr - {}", functionInvocationExpr.getCallableUnit().getName());
        }
    }

    @Override
    public void visit(InstanceCreationExpr instanceCreationExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing InstanceCreationExpr");
        }
        setTempResult(instanceCreationExpr.getTempOffset(), instanceCreationExpr.getType().getDefaultValue());
    }

    @Override
    public void visit(MapInitExpr mapInitExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing MapInitExpr");
        }
    }

    @Override
    public void visit(MapStructInitKeyValueExpr mapStructInitKeyValueExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing MapStructInitKeyValueExpr");
        }
    }

    @Override
    public void visit(RefTypeInitExpr refTypeInitExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing RefTypeInitExpr");
        }
    }

    @Override
    public void visit(ResourceInvocationExpr resourceInvocationExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ResourceInvocationExpr");
        }

        Resource resource = resourceInvocationExpr.getResource();

        ControlStack controlStack = bContext.getControlStack();
        BValue[] valueParams = new BValue[resource.getStackFrameSize()];

        BMessage messageValue = new BMessage(bContext.getCarbonMessage());

        valueParams[0] = messageValue;

        int valueCounter = 1;

        // Populate values for Connector declarations
        valueCounter = populateConnectorDclValues(resource.getConnectorDcls(), valueParams, valueCounter);

        // Create default values for all declared local variables
        VariableDef[] variableDefs = resource.getVariableDefs();
        for (VariableDef variableDef : variableDefs) {
            valueParams[valueCounter] = variableDef.getType().getDefaultValue();
            valueCounter++;
        }

        BValue[] ret = new BValue[1];

        SymbolName resourceSymbolName = resource.getSymbolName();
        CallableUnitInfo resourceInfo = new CallableUnitInfo(resourceSymbolName.getName(),
                resourceSymbolName.getPkgPath(), resource.getNodeLocation());
        BValue[] tempValues = new BValue[resource.getTempStackFrameSize() + 1];
        StackFrame stackFrame = new StackFrame(valueParams, ret, tempValues, resourceInfo);
        controlStack.pushFrame(stackFrame);

        resource.getResourceBody().executeLNode(this);
    }

    @Override
    public void visit(StructFieldAccessExpr structFieldAccessExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing StructFieldAccessExpr");
        }
    }

    @Override
    public void visit(StructInitExpr structInitExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing StructInitExpr");
        }
    }

    @Override
    public void visit(TypeCastExpression typeCastExpression) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing  {}->{}", typeCastExpression.getType(), typeCastExpression.getTargetType());
        }
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing UnaryExpression");
        }
    }

    @Override
    public void visit(VariableRefExpr variableRefExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing VariableRefExpr");
        }
        MemoryLocation memoryLocation = variableRefExpr.getMemoryLocation();
        setTempResult(variableRefExpr.getTempOffset(), memoryLocation.executeLNode(this));
    }

    /* Memory Location */

    @Override
    public BValue visit(StackVarLocation stackVarLocation) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing StackVarLocation");
        }
        int offset = stackVarLocation.getStackFrameOffset();
        return controlStack.getValue(offset);
    }

    @Override
    public BValue visit(ConstantLocation constantLocation) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ConstantLocation");
        }
        int offset = constantLocation.getStaticMemAddrOffset();
        RuntimeEnvironment.StaticMemory staticMemory = runtimeEnv.getStaticMemory();
        return staticMemory.getValue(offset);
    }

    @Override
    public BValue visit(ServiceVarLocation serviceVarLocation) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ServiceVarLocation");
        }
        int offset = serviceVarLocation.getStaticMemAddrOffset();
        RuntimeEnvironment.StaticMemory staticMemory = runtimeEnv.getStaticMemory();
        return staticMemory.getValue(offset);
    }

    @Override
    public BValue visit(StructVarLocation structLocation) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing StructVarLocation");
        }
        BStruct bStruct = (BStruct) controlStack.getValue(structLocation.getStructMemAddrOffset());
        return bStruct.getValue(structLocation.getStructMemAddrOffset());
    }

    @Override
    public BValue visit(ConnectorVarLocation connectorVarLocation) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ConnectorVarLocation");
        }
        // Fist the get the BConnector object. In an action invocation first argument is always the connector
        BConnector bConnector = (BConnector) controlStack.getValue(0);
        if (bConnector == null) {
            throw new BallerinaException("Connector argument value is null");
        }

        // Now get the connector variable value from the memory block allocated to the BConnector instance.
        return bConnector.getValue(connectorVarLocation.getConnectorMemAddrOffset());
    }

    /* Helper Nodes. */

    @Override
    public void visit(EndNode endNode) {
        // Done.
        if (logger.isDebugEnabled()) {
            logger.debug("Executing EndNode");
        }
    }

    @Override
    public void visit(ExitNode exitNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ExitNode");
        }
        Runtime.getRuntime().exit(0);
    }

    @Override
    public void visit(GotoNode gotoNode) {
        Integer pop = branchIDStack.peek();
        if (logger.isDebugEnabled()) {
            logger.debug("Executing GotoNode branch:{}", pop);
        }
    }

    @Override
    public void visit(IfElseNode ifElseNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing IfElseNode");
        }
        Expression expr = ifElseNode.getCondition();
        BBoolean condition = (BBoolean) getTempResult(expr.getTempOffset());

        if (condition.booleanValue()) {
            ifElseNode.next.executeLNode(this);
        } else {
            ifElseNode.nextAfterBreak().executeLNode(this);
        }
    }

    @Override
    public void visit(AssignStmtEndNode assignStmtEndNode) {
        AssignStmt assignStmt = assignStmtEndNode.getStatement();
        if (logger.isDebugEnabled()) {
            logger.debug("Executing AssignStmt EndNode - L size{},R type:{}",
                    assignStmt.getLExprs().length,
                    assignStmt.getRExpr().getType() != null ? assignStmt.getRExpr().getType().toString() : null);
        }
        Expression rExpr = assignStmt.getRExpr();
        Expression[] lExprs = assignStmt.getLExprs();
        for (int i = 0; i < lExprs.length; i++) {
            Expression lExpr = lExprs[i];
            BValue rVal = getTempResult(rExpr.getTempOffset() + i);
            if (lExpr instanceof VariableRefExpr) {
                assignValueToVarRefExpr(rVal, (VariableRefExpr) lExpr);
            } else if (lExpr instanceof ArrayMapAccessExpr) {
                assignValueToArrayMapAccessExpr(rVal, (ArrayMapAccessExpr) lExpr);
            } else if (lExpr instanceof StructFieldAccessExpr) {
                assignValueToStructFieldAccessExpr(rVal, (StructFieldAccessExpr) lExpr);
            }
        }
    }

    @Override
    public void visit(ReplyStmtEndNode replyStmtEndNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ReplyStmt - EndNode");
        }
        Expression expr = replyStmtEndNode.getStatement().getReplyExpr();
        BMessage bMessage = (BMessage) getTempResult(expr.getTempOffset());
        bContext.getBalCallback().done(bMessage.value());
    }

    @Override
    public void visit(ReturnStmtEndNode returnStmtEndNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ReturnStmt - EndNode");
        }
        ReturnStmt returnStmt = returnStmtEndNode.getStatement();
        Expression[] exprs = returnStmt.getExprs();

        // Check whether the first argument is a multi-return function
        if (exprs.length == 1 && exprs[0] instanceof FunctionInvocationExpr) {
            FunctionInvocationExpr funcIExpr = (FunctionInvocationExpr) exprs[0];
            int returnLength = funcIExpr.getCallableUnit().getReturnParameters().length;
            for (int i = 0; i < returnLength; i++) {
                controlStack.setReturnValue(i, getTempResult(funcIExpr.getTempOffset() + i));
            }
        } else {
            for (int i = 0; i < exprs.length; i++) {
                Expression expr = exprs[i];
                controlStack.setReturnValue(i, getTempResult(expr.getTempOffset()));
            }
        }
    }

    @Override
    public void visit(VariableDefStmtEndNode variableDefStmtEndNode) {
        VariableDefStmt varDefStmt = variableDefStmtEndNode.getStatement();
        if (logger.isDebugEnabled()) {
            logger.debug("Executing VariableDefStmt EndNode - L size{},R type:{}",
                    varDefStmt.getLExpr().getType().toString(),
                    varDefStmt.getRExpr() != null ? varDefStmt.getLExpr().getType().toString() : null);
        }
        BValue rValue;
        Expression lExpr = varDefStmt.getLExpr();
        Expression rExpr = varDefStmt.getRExpr();
        if (rExpr == null) {
            if (BTypes.isValueType(lExpr.getType())) {
                rValue = lExpr.getType().getDefaultValue();
            } else {
                // TODO Implement BNull here ..
                rValue = null;
            }
        } else {
            rValue = getTempResult(rExpr.getTempOffset());
        }
        if (lExpr instanceof VariableRefExpr) {
            assignValueToVarRefExpr(rValue, (VariableRefExpr) lExpr);
        } else if (lExpr instanceof ArrayMapAccessExpr) {
            assignValueToArrayMapAccessExpr(rValue, (ArrayMapAccessExpr) lExpr);
        } else if (lExpr instanceof StructFieldAccessExpr) {
            assignValueToStructFieldAccessExpr(rValue, (StructFieldAccessExpr) lExpr);
        }
    }


    @Override
    public void visit(ActionInvocationExprStartNode actionInvocationExprStartNode) {
        ActionInvocationExpr actionIExpr = actionInvocationExprStartNode.getExpression();
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ActionInvocationExpr StartNode " + actionIExpr.getCallableUnit().getName());
        }
        // Create the Stack frame
        Action action = actionIExpr.getCallableUnit();

        BValue[] localVals = new BValue[action.getStackFrameSize()];

        // Create default values for all declared local variables
        int valueCounter = populateArgumentValues(actionIExpr.getArgExprs(), localVals);

        // Populate values for Connector declarations
        if (action instanceof BallerinaAction) {
            BallerinaAction ballerinaAction = (BallerinaAction) action;
            valueCounter = populateConnectorDclValues(ballerinaAction.getConnectorDcls(), localVals, valueCounter);
        }

        // Create default values for all declared local variables
        for (VariableDef variableDef : action.getVariableDefs()) {
            localVals[valueCounter] = variableDef.getType().getDefaultValue();
            valueCounter++;
        }

        for (ParameterDef returnParam : action.getReturnParameters()) {
            // Check whether these are unnamed set of return types.
            // If so break the loop. You can't have a mix of unnamed and named returns parameters.
            if (returnParam.getName() == null) {
                break;
            }

            localVals[valueCounter] = returnParam.getType().getDefaultValue();
            valueCounter++;
        }

        // Create an array in the stack frame to hold return values;
        BValue[] returnVals = new BValue[action.getReturnParameters().length];

        // Create a new stack frame with memory locations to hold parameters, local values, temp expression values and
        // return values;
        SymbolName actionSymbolName = actionIExpr.getCallableUnit().getSymbolName();
        CallableUnitInfo actionInfo = new CallableUnitInfo(actionSymbolName.getName(), actionSymbolName.getPkgPath(),
                actionIExpr.getNodeLocation());
        BValue[] tempValues = new BValue[actionIExpr.getCallableUnit().getTempStackFrameSize() + 1];
        StackFrame stackFrame = new StackFrame(localVals, returnVals, tempValues, actionInfo);
        controlStack.pushFrame(stackFrame);
        if (actionIExpr.hasGotoBranchID()) {
            branchIDStack.push(actionIExpr.getGotoBranchID());
        }
    }

    @Override
    public void visit(ArrayInitExprEndNode arrayInitExprEndNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ArrayInitExpr - EndNode");
        }
        Expression[] argExprs = arrayInitExprEndNode.getExpression().getArgExprs();

        // Creating a new array
        BArray bArray = arrayInitExprEndNode.getExpression().getType().getDefaultValue();

        for (int i = 0; i < argExprs.length; i++) {
            Expression expr = argExprs[i];
            BValue value = getTempResult(expr.getTempOffset());
            bArray.add(i, value);
        }
        setTempResult(arrayInitExprEndNode.getExpression().getTempOffset(), bArray);
    }

    @Override
    public void visit(ArrayMapAccessExprEndNode arrayMapAccessExprEndNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ArrayMapAccessExpr - EndNode");
        }

        ArrayMapAccessExpr arrayMapAccessExpr = arrayMapAccessExprEndNode.getExpression();
        if (!arrayMapAccessExpr.isLHSExpr()) {
            Expression arrayVarRefExpr = arrayMapAccessExpr.getRExpr();
            BValue collectionValue = getTempResult(arrayVarRefExpr.getTempOffset());
            Expression indexExpr = arrayMapAccessExpr.getIndexExpr();
            BValue indexValue = getTempResult(indexExpr.getTempOffset());
            // Check whether this collection access expression is in the left hand of an assignment expression
            // If yes skip setting the value;
            BValue result;
            if (arrayMapAccessExpr.getType() != BTypes.typeMap) {
                // Get the value stored in the index
                if (collectionValue instanceof BArray) {
                    result = ((BArray) collectionValue).get(((BInteger) indexValue).intValue());
                } else {
                    result = collectionValue;
                }
            } else {
                // Get the value stored in the index
                if (indexValue instanceof BString) {
                    result = ((BMap) collectionValue).get(indexValue);
                } else {
                    throw new IllegalStateException("Index of a map should be string type");
                }
            }
            setTempResult(arrayMapAccessExpr.getTempOffset(), result);
        }
        // Else Nothing to do. (Assignment Statement will handle rest.
    }

    @Override
    public void visit(BacktickExprEndNode backtickExprEndNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing BacktickExpr - EndNode");
        }
        String evaluatedString = evaluteBacktickString(backtickExprEndNode.getExpression());
        if (backtickExprEndNode.getExpression().getType() == BTypes.typeJSON) {
            setTempResult(backtickExprEndNode.getExpression().getTempOffset(), new BJSON(evaluatedString));
        } else {
            setTempResult(backtickExprEndNode.getExpression().getTempOffset(), new BXML(evaluatedString));
        }
    }

    @Override
    public void visit(BinaryExpressionEndNode binaryExpressionEndNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing BinaryExpression - EndNode");
        }
        BinaryExpression binaryExpr = binaryExpressionEndNode.getExpression();
        Expression rExpr = binaryExpr.getRExpr();
        BValueType rValue = (BValueType) getTempResult(rExpr.getTempOffset());

        Expression lExpr = binaryExpr.getLExpr();
        BValueType lValue = (BValueType) getTempResult(lExpr.getTempOffset());
        setTempResult(binaryExpr.getTempOffset(), binaryExpr.getEvalFunc().apply(lValue, rValue));
    }

    @Override
    public void visit(FunctionInvocationExprStartNode functionInvocationExprStartNode) {
        FunctionInvocationExpr funcIExpr = functionInvocationExprStartNode.getExpression();
        if (logger.isDebugEnabled()) {
            logger.debug("Executing FunctionInvocationExpr StartNode - {}", funcIExpr.getCallableUnit().getName());
        }
        // Create the Stack frame
        Function function = funcIExpr.getCallableUnit();

        int sizeOfValueArray = function.getStackFrameSize();
        BValue[] localVals = new BValue[sizeOfValueArray];

        // Get values for all the function arguments
        int valueCounter = populateArgumentValues(funcIExpr.getArgExprs(), localVals);

        for (ParameterDef returnParam : function.getReturnParameters()) {
            // Check whether these are unnamed set of return types.
            // If so break the loop. You can't have a mix of unnamed and named returns parameters.
            if (returnParam.getName() == null) {
                break;
            }

            localVals[valueCounter] = returnParam.getType().getDefaultValue();
            valueCounter++;
        }

        // Create an array in the stack frame to hold return values;
        BValue[] returnVals = new BValue[function.getReturnParameters().length];

        // Create a new stack frame with memory locations to hold parameters, local values, temp expression value,
        // return values and function invocation location;
        SymbolName functionSymbolName = funcIExpr.getCallableUnit().getSymbolName();
        CallableUnitInfo functionInfo = new CallableUnitInfo(functionSymbolName.getName(),
                functionSymbolName.getPkgPath(), funcIExpr.getNodeLocation());
        BValue[] tempValues = new BValue[funcIExpr.getCallableUnit().getTempStackFrameSize() + 1];
        StackFrame stackFrame = new StackFrame(localVals, returnVals, tempValues, functionInfo);
        controlStack.pushFrame(stackFrame);
        if (funcIExpr.hasGotoBranchID()) {
            branchIDStack.push(funcIExpr.getGotoBranchID());
        }
    }

    @Override
    public void visit(StructFieldAccessExprEndNode structFieldAccessExprEndNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing StructFieldAccess - EndNode");
        }
        StructFieldAccessExpr structFieldAccessExpr = structFieldAccessExprEndNode.getExpression();
        Expression varRef = structFieldAccessExpr.getVarRef();
        BValue value = getTempResult(varRef.getTempOffset());
        setTempResult(structFieldAccessExpr.getTempOffset(), getFieldExprValue(structFieldAccessExpr, value));
    }

    @Override
    public void visit(StructInitExprEndNode structInitExprEndNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing StructInitExpr - EndNode");
        }
        StructInitExpr structInitExpr = structInitExprEndNode.getExpression();

        StructDcl structDcl = structInitExpr.getStructDcl();
        BValue[] structMemBlock;
        int offset = 0;
        StructDef structDef = structDcl.getStructDef();
        structMemBlock = new BValue[structDef.getStructMemorySize()];

        // create a memory block to hold field of the struct, and populate it with default values
        VariableDef[] fields = structDef.getFields();
        for (VariableDef field : fields) {
            structMemBlock[offset] = field.getType().getDefaultValue();
            offset++;
        }
        setTempResult(structInitExpr.getTempOffset(), new BStruct(structDef, structMemBlock));
    }

    @Override
    public void visit(TypeCastExpressionEndNode typeCastExpressionEndNode) {
        TypeCastExpression typeCastExpression = typeCastExpressionEndNode.getExpression();
        if (logger.isDebugEnabled()) {
            logger.debug("Executing TypeCastExpression - EndNode {}->{}, source-{}", typeCastExpression.getType(),
                    typeCastExpression.getTargetType(), typeCastExpression.getRExpr() != null);
        }
        // Check for native type casting
        if (typeCastExpression.getEvalFunc() != null) {
            BValueType result = (BValueType) getTempResult(typeCastExpression.getRExpr().getTempOffset());
            setTempResult(typeCastExpression.getTempOffset(), typeCastExpression.getEvalFunc().apply(result));
        } else {
            TypeConvertor typeConvertor = typeCastExpression.getCallableUnit();

            int sizeOfValueArray = typeConvertor.getStackFrameSize();
            BValue[] localVals = new BValue[sizeOfValueArray];

            // Get values for all the function arguments
            int valueCounter = populateArgumentValues(typeCastExpression.getArgExprs(), localVals);

            // Create default values for all declared local variables
            for (VariableDef variableDef : typeConvertor.getVariableDefs()) {
                localVals[valueCounter] = variableDef.getType().getDefaultValue();
                valueCounter++;
            }

            for (ParameterDef returnParam : typeConvertor.getReturnParameters()) {
                // Check whether these are unnamed set of return types.
                // If so break the loop. You can't have a mix of unnamed and named returns parameters.
                if (returnParam.getName() == null) {
                    break;
                }

                localVals[valueCounter] = returnParam.getType().getDefaultValue();
                valueCounter++;
            }

            // Create an array in the stack frame to hold return values;
            BValue[] returnVals = new BValue[1];

            // Create a new stack frame with memory locations to hold parameters, local values, temp expression value,
            // return values and function invocation location;
            SymbolName functionSymbolName = typeCastExpression.getCallableUnit().getSymbolName();
            CallableUnitInfo functionInfo = new CallableUnitInfo(functionSymbolName.getName(),
                    functionSymbolName.getPkgPath(), typeCastExpression.getNodeLocation());
            BValue[] tempValues = new BValue[typeCastExpression.getCallableUnit().getTempStackFrameSize() + 1];
            StackFrame stackFrame = new StackFrame(localVals, returnVals, tempValues, functionInfo);
            controlStack.pushFrame(stackFrame);
            if (typeCastExpression.hasGotoBranchID()) {
                branchIDStack.push(typeCastExpression.getGotoBranchID());
            }
        }
    }

    @Override
    public void visit(UnaryExpressionEndNode unaryExpressionEndNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing UnaryExpressionEnd[Link]");
        }
        UnaryExpression unaryExpr = unaryExpressionEndNode.getExpression();
        BValueType rValue = (BValueType) getTempResult(unaryExpr.getRExpr().getTempOffset());
        BValue result = unaryExpr.getEvalFunc().apply(null, rValue);
        setTempResult(unaryExpr.getTempOffset(), result);
    }

    @Override
    public void visit(RefTypeInitExprEndNode refTypeInitExprEndNode) {
        RefTypeInitExpr refTypeInitExpr = refTypeInitExprEndNode.getExpression();
        if (logger.isDebugEnabled()) {
            logger.debug("Executing RefTypeInitExpr - EndNode");
        }
        Expression[] argExprs = refTypeInitExpr.getArgExprs();
        // Creating a new map
        BMap<BString, BValue> bMap = new BMap<>();

        for (int i = 0; i < argExprs.length; i++) {
            MapStructInitKeyValueExpr expr = (MapStructInitKeyValueExpr) argExprs[i];
            BString key = new BString(expr.getKey());
            Expression expression = expr.getValueExpr();
            BValue value = getTempResult(expression.getTempOffset());
            bMap.put(key, value);
        }
        setTempResult(refTypeInitExpr.getTempOffset(), bMap);
    }

    @Override
    public void visit(CallableUnitEndNode callableUnitEndNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing CallableUnit - EndNode native:{} ,type:{} ",
                    callableUnitEndNode.isNativeInvocation(), callableUnitEndNode.getExpression().getClass());
        }
        StackFrame stackFrame = controlStack.popFrame();
        if (stackFrame.returnValues.length > 0) {
            int i = 0;
            for (BValue value : stackFrame.returnValues) {
                setTempResult(callableUnitEndNode.getExpression().getTempOffset() + i, value);
                i++;
            }
        }
    }

    @Override
    public void visit(ConnectorInitExprEndNode connectorInitExprEndNode) {

    }

    @Override
    public void visit(InvokeNativeActionNode invokeNativeActionNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing Native Action - " + invokeNativeActionNode.getCallableUnit().getName());
        }
        BalNativeActionCallback balNativeActionCallback = new BalNativeActionCallback(bContext, this,
                invokeNativeActionNode);
        invokeNativeActionNode.getCallableUnit().execute(bContext, balNativeActionCallback);
    }

    @Override
    public void visit(InvokeNativeFunctionNode invokeNativeFunctionNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing Native Function - " + invokeNativeFunctionNode.getCallableUnit().getName());
        }
        invokeNativeFunctionNode.getCallableUnit().executeNative(bContext);
    }

    @Override
    public void visit(InvokeNativeTypeMapperNode invokeNativeTypeMapperNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing Native TypeConverter - " + invokeNativeTypeMapperNode.getCallableUnit()
                    .getName());
        }
        invokeNativeTypeMapperNode.getCallableUnit().convertNative(bContext);
    }

    @Override
    public void visit(MapInitExprEndNode mapInitExprEndNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing MapInitExprEndNode - EndNode");
        }
        Expression[] argExprs = mapInitExprEndNode.getExpression().getArgExprs();

        // Creating a new array
        BMap bMap = mapInitExprEndNode.getExpression().getType().getDefaultValue();

        for (int i = 0; i < argExprs.length; i++) {
            MapStructInitKeyValueExpr expr = (MapStructInitKeyValueExpr) argExprs[i];
            BValue keyVal = getTempResult(expr.getKeyExpr().getTempOffset());
            BValue value = getTempResult(expr.getValueExpr().getTempOffset());
            bMap.put(keyVal, value);
        }
        setTempResult(mapInitExprEndNode.getExpression().getTempOffset(), bMap);
    }

    // Private methods

    private int populateArgumentValues(Expression[] expressions, BValue[] localVals) {
        int i = 0;
        for (Expression arg : expressions) {
            // Evaluate the argument expression
            BValue argValue = getTempResult(arg.getTempOffset());
            BType argType = arg.getType();

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

    private int populateConnectorDclValues(ConnectorDcl[] connectorDcls, BValue[] valueParams, int valueCounter) {
        for (ConnectorDcl connectorDcl : connectorDcls) {

            BValue[] connectorMemBlock;
            Connector connector = connectorDcl.getConnector();
            if (connector instanceof AbstractNativeConnector) {

                //TODO Fix Issue#320
                AbstractNativeConnector nativeConnector = ((AbstractNativeConnector) connector).getInstance();
                Expression[] argExpressions = connectorDcl.getArgExprs();
                connectorMemBlock = new BValue[argExpressions.length];

                for (int j = 0; j < argExpressions.length; j++) {
                    connectorMemBlock[j] = getTempResult(argExpressions[j].getTempOffset());
                }

                nativeConnector.init(connectorMemBlock);
                connector = nativeConnector;

            } else {
                BallerinaConnectorDef connectorDef = (BallerinaConnectorDef) connector;

                int offset = 0;
                connectorMemBlock = new BValue[connectorDef.getSizeOfConnectorMem()];
                for (Expression expr : connectorDcl.getArgExprs()) {
                    connectorMemBlock[offset] = getTempResult(expr.getTempOffset());
                    offset++;
                }

                // Populate all connector declarations
                offset = populateConnectorDclValues(connectorDef.getConnectorDcls(), connectorMemBlock, offset);

                for (VariableDef variableDef : connectorDef.getVariableDefs()) {
                    connectorMemBlock[offset] = variableDef.getType().getDefaultValue();
                    offset++;
                }
            }

            BConnector connectorValue = new BConnector(connector, connectorMemBlock);
            valueParams[valueCounter] = connectorValue;
            valueCounter++;
        }

        return valueCounter;
    }

    private String evaluteBacktickString(BacktickExpr backtickExpr) {
        String varString = "";
        for (Expression expression : backtickExpr.getArgExprs()) {
            varString = varString + getTempResult(expression.getTempOffset()).stringValue();
        }
        return varString;
    }

    private void assignValueToArrayMapAccessExpr(BValue rValue, ArrayMapAccessExpr lExpr) {
        ArrayMapAccessExpr accessExpr = lExpr;
        if (!(accessExpr.getType() == BTypes.typeMap)) {
            BArray arrayVal = (BArray) getTempResult(accessExpr.getRExpr().getTempOffset());
            BInteger indexVal = (BInteger) getTempResult(accessExpr.getIndexExpr().getTempOffset());
            arrayVal.add(indexVal.intValue(), rValue);

        } else {
            BMap<BString, BValue> mapVal = (BMap<BString, BValue>) getTempResult(accessExpr.getRExpr().getTempOffset());
            BString indexVal = (BString) getTempResult(accessExpr.getIndexExpr().getTempOffset());
            mapVal.put(indexVal, rValue);
            // set the type of this expression here
            // accessExpr.setType(rExpr.getType());
        }
    }

    private void assignValueToVarRefExpr(BValue rValue, VariableRefExpr lExpr) {
        VariableRefExpr variableRefExpr = lExpr;
        MemoryLocation memoryLocation = variableRefExpr.getMemoryLocation();
        if (memoryLocation instanceof StackVarLocation) {
            int stackFrameOffset = ((StackVarLocation) memoryLocation).getStackFrameOffset();
            controlStack.setValue(stackFrameOffset, rValue);
        } else if (memoryLocation instanceof ServiceVarLocation) {
            int staticMemOffset = ((ServiceVarLocation) memoryLocation).getStaticMemAddrOffset();
            runtimeEnv.getStaticMemory().setValue(staticMemOffset, rValue);

        } else if (memoryLocation instanceof ConnectorVarLocation) {
            // Fist the get the BConnector object. In an action invocation first argument is always the connector
            BConnector bConnector = (BConnector) controlStack.getValue(0);
            if (bConnector == null) {
                throw new BallerinaException("Connector argument value is null");
            }

            int connectorMemOffset = ((ConnectorVarLocation) memoryLocation).getConnectorMemAddrOffset();
            bConnector.setValue(connectorMemOffset, rValue);
        }
    }

    /**
     * Assign a value to a field of a struct, represented by a {@link StructFieldAccessExpr}.
     *
     * @param rValue Value to be assigned
     * @param lExpr  {@link StructFieldAccessExpr} which represents the field of the struct
     */
    private void assignValueToStructFieldAccessExpr(BValue rValue, StructFieldAccessExpr lExpr) {
        Expression lExprVarRef = lExpr.getVarRef();
        BValue value = getTempResult(lExprVarRef.getTempOffset());
        setFieldValue(rValue, lExpr, value);
    }

    /**
     * Recursively traverse and set the value of the access expression of a field of a struct.
     *
     * @param rValue     Value to be set
     * @param expr       StructFieldAccessExpr of the current field
     * @param currentVal Value of the expression evaluated so far.
     */
    private void setFieldValue(BValue rValue, StructFieldAccessExpr expr, BValue currentVal) {
        // currentVal is a BStruct or array/map of BStruct. hence get the element value of it.
        BStruct currentStructVal = (BStruct) getUnitValue(currentVal, expr);

        StructFieldAccessExpr fieldExpr = expr.getFieldExpr();
        int fieldLocation = ((StructVarLocation) getMemoryLocation(fieldExpr)).getStructMemAddrOffset();

        if (fieldExpr.getFieldExpr() == null) {
            if (currentStructVal.value() == null) {
                throw new BallerinaException("cannot set field '" + fieldExpr.getSymbolName().getName() +
                        "' of non-initialized variable '" + fieldExpr.getParent().getSymbolName().getName() + "'.");
            }
            setUnitValue(rValue, currentStructVal, fieldLocation, fieldExpr);
            return;
        }

        // At this point, field of the field is not null. Means current element,
        // and its field are both struct types.

        if (currentStructVal.value() == null) {
            throw new BallerinaException("cannot set field '" + fieldExpr.getSymbolName().getName() +
                    "' of non-initialized variable '" + fieldExpr.getParent().getSymbolName().getName() + "'.");
        }

        // get the unit value of the struct field,
        BValue value = currentStructVal.getValue(fieldLocation);

        setFieldValue(rValue, fieldExpr, value);
    }

    /**
     * Get the memory location for a expression.
     *
     * @param expression Expression to get the memory location
     * @return Memory location of the expression
     */
    private MemoryLocation getMemoryLocation(Expression expression) {
        // If the expression is an array-map expression, then get the location of the variable-reference-expression
        // of the array-map-access-expression.
        if (expression instanceof ArrayMapAccessExpr) {
            return getMemoryLocation(((ArrayMapAccessExpr) expression).getRExpr());
        }

        // If the expression is a struct-field-access-expression, then get the memory location of the variable
        // referenced by the struct-field-access-expression
        if (expression instanceof StructFieldAccessExpr) {
            return getMemoryLocation(((StructFieldAccessExpr) expression).getVarRef());
        }

        // Set the memory location of the variable-reference-expression
        return ((VariableRefExpr) expression).getMemoryLocation();
    }

    /**
     * Set the unit value of the current value.
     * <br/>
     * i.e: Value represented by a field-access-expression can be one of:
     * <ul>
     * <li>A variable</li>
     * <li>An element of an array/map variable.</li>
     * </ul>
     * But the value get after evaluating the field-access-expression (<b>lExprValue</b>) contains the whole
     * variable. This methods set the unit value (either the complete array/map or the referenced element of an
     * array/map), using the index expression of the 'fieldExpr'.
     *
     * @param rValue         Value to be set
     * @param lExprValue     Value of the field access expression evaluated so far. This is always of struct
     *                       type.
     * @param memoryLocation Location of the field to be set, in the struct 'lExprValue'
     * @param fieldExpr      Field Access Expression of the current field
     */
    private void setUnitValue(BValue rValue, BStruct lExprValue, int memoryLocation,
                              StructFieldAccessExpr fieldExpr) {

        Expression indexExpr;
        if (fieldExpr.getVarRef() instanceof ArrayMapAccessExpr) {
            indexExpr = ((ArrayMapAccessExpr) fieldExpr.getVarRef()).getIndexExpr();
        } else {
            // If the lExprValue value is not a struct array/map, then set the value to the struct
            lExprValue.setValue(memoryLocation, rValue);
            return;
        }

        // Evaluate the index expression and get the value.
        BValue indexValue = getTempResult(indexExpr.getTempOffset());

        // Get the array/map value from the mermory location
        BValue arrayMapValue = lExprValue.getValue(memoryLocation);

        // Set the value to array/map's index location
        if (fieldExpr.getRefVarType() == BTypes.typeMap) {
            ((BMap) arrayMapValue).put(indexValue, rValue);
        } else {
            ((BArray) arrayMapValue).add(((BInteger) indexValue).intValue(), rValue);
        }
    }

    /**
     * Recursively traverse and get the value of the access expression of a field of a struct.
     *
     * @param expr       StructFieldAccessExpr of the current field
     * @param currentVal Value of the expression evaluated so far.
     * @return Value of the expression after evaluating the current field.
     */
    private BValue getFieldExprValue(StructFieldAccessExpr expr, BValue currentVal) {
        // currentVal is a BStruct or array/map of BStruct. hence get the element value of it.
        BStruct currentStructVal = (BStruct) getUnitValue(currentVal, expr);

        StructFieldAccessExpr fieldExpr = expr.getFieldExpr();
        int fieldLocation = ((StructVarLocation) getMemoryLocation(fieldExpr)).getStructMemAddrOffset();

        // If this is the last field, return the value from memory location
        if (fieldExpr.getFieldExpr() == null) {
            if (currentStructVal.value() == null) {
                throw new BallerinaException("cannot access field '" + fieldExpr.getSymbolName().getName() +
                        "' of non-initialized variable '" + fieldExpr.getParent().getSymbolName().getName() + "'.");
            }
            // Value stored in the struct can be also an array. Hence if its an arrray access,
            // get the aray element value
            return getUnitValue(currentStructVal.getValue(fieldLocation), fieldExpr);
        }

        if (currentStructVal.value() == null) {
            throw new BallerinaException("cannot access field '" + fieldExpr.getSymbolName().getName() +
                    "' of non-initialized variable '" + fieldExpr.getParent().getSymbolName().getName() + "'.");
        }
        BValue value = currentStructVal.getValue(fieldLocation);

        // Recursively travel through the struct and get the value
        return getFieldExprValue(fieldExpr, value);
    }

    /**
     * Get the unit value of the current value.
     * <br/>
     * i.e: Value represented by a field-access-expression can be one of:
     * <ul>
     * <li>A variable</li>
     * <li>An element of an array/map variable.</li>
     * </ul>
     * But the value stored in memory (<b>currentVal</b>) contains the entire variable. This methods
     * retrieves the unit value (either the complete array/map or the referenced element of an array/map),
     * using the index expression of the 'fieldExpr'.
     *
     * @param currentVal Value of the field expression evaluated so far
     * @param fieldExpr  Field access expression for the current value
     * @return Unit value of the current value
     */
    private BValue getUnitValue(BValue currentVal, StructFieldAccessExpr fieldExpr) {
        if (!(currentVal instanceof BArray || currentVal instanceof BMap<?, ?>)) {
            return currentVal;
        }

        // If the lExprValue value is not a struct array/map, then the unit value is same as the struct
        Expression indexExpr;
        if (fieldExpr.getVarRef() instanceof ArrayMapAccessExpr) {
            indexExpr = ((ArrayMapAccessExpr) fieldExpr.getVarRef()).getIndexExpr();
        } else {
            return currentVal;
        }

        // Evaluate the index expression and get the value
        BValue indexValue = getTempResult(indexExpr.getTempOffset());

        // Get the value from array/map's index location
        if (fieldExpr.getRefVarType() == BTypes.typeMap) {
            return ((BMap) currentVal).get(indexValue);
        } else {
            return ((BArray) currentVal).get(((BInteger) indexValue).intValue());
        }
    }

    private BValue getTempResult(int offset) {
        return bContext.getControlStack().getCurrentFrame().tempValues[offset];
    }

    private void setTempResult(int offset, BValue result) {
        bContext.getControlStack().getCurrentFrame().tempValues[offset] = result;
    }

    private void clearTempValue() {
        bContext.getControlStack().getCurrentFrame().tempValues =
                new BValue[bContext.getControlStack().getCurrentFrame().tempValues.length];
    }
}

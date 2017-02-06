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
package org.wso2.ballerina.core.interpreter.nonblocking.debugger;

import org.wso2.ballerina.core.interpreter.ConnectorVarLocation;
import org.wso2.ballerina.core.interpreter.ConstantLocation;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.RuntimeEnvironment;
import org.wso2.ballerina.core.interpreter.ServiceVarLocation;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.interpreter.StackVarLocation;
import org.wso2.ballerina.core.interpreter.StructVarLocation;
import org.wso2.ballerina.core.interpreter.nonblocking.BLangAbstractLinkedExecutor;
import org.wso2.ballerina.core.model.LinkedNode;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.SymbolName;
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
import org.wso2.ballerina.core.model.statements.AbstractStatement;
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
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.statements.ThrowStmt;
import org.wso2.ballerina.core.model.statements.TryCatchStmt;
import org.wso2.ballerina.core.model.statements.VariableDefStmt;
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.model.values.BConnector;
import org.wso2.ballerina.core.model.values.BValue;

import java.util.AbstractMap;
import java.util.HashMap;

/**
 * {@link BLangExecutionDebugger} is the debug enabled BLangExecutor.
 *
 * @since 0.8.0
 */
public class BLangExecutionDebugger extends BLangAbstractLinkedExecutor {

    private HashMap<String, NodeLocation> positionHashMap;

    private Context bContext;
    private RuntimeEnvironment runtimeEnvironment;

    private LinkedNode currentHaltNode;
    private Statement nextStep;
    private boolean done, stepNextStatement, stepOutFromCallableUnit;
    private DebugSessionObserver observer;

    public BLangExecutionDebugger(RuntimeEnvironment runtimeEnv, Context bContext) {
        super(runtimeEnv, bContext);
        this.bContext = bContext;
        this.runtimeEnvironment = runtimeEnv;
        this.positionHashMap = new HashMap<>();
        this.done = false;
        this.stepNextStatement = false;
        this.stepOutFromCallableUnit = false;
    }

    public void setDebugSessionObserver(DebugSessionObserver observer) {
        this.observer = observer;
    }

    public void addDebugPoint(NodeLocation nodeLocation) {
        positionHashMap.put(nodeLocation.toString(), nodeLocation);
    }

    public void addDebugPoints(NodeLocation[] nodeLocations) {
        for (NodeLocation nodeLocation : nodeLocations) {
            positionHashMap.put(nodeLocation.toString(), nodeLocation);
        }
    }

    public void removeDebugPoint(NodeLocation position) {
        positionHashMap.remove(position.toString());
    }

    /**
     * Resume Execution.
     */
    public synchronized void resume() {
        if (done) {
            throw new IllegalStateException("Can't resume. Ballerina Program execution completed.");
        } else {
            LinkedNode current = currentHaltNode;
            this.currentHaltNode = null;
            this.stepNextStatement = false;
            current.next().executeLNode(this);
        }
    }

    /**
     * Step Over to next sibling statement. If there is no next sibling statement, try to execute parents's next
     * sibling.
     */
    public synchronized void stepOver() {
        if (done) {
            throw new IllegalStateException("Can't Step Over. Ballerina Program execution completed.");
        } else {
            LinkedNode current = currentHaltNode;
            currentHaltNode = null;
            // Currently we are stepping though statements, But this has to stop at current statement's next sibling.
            LinkedNode previous = current;
            while (nextStep == null) {
                if (previous instanceof BlockStmt) {
                    if (((BlockStmt) previous).getGotoNode() != null) {
                        // This is a callable Unit. We have to step-in here.
                        this.stepNextStatement = true;
                        break;
                    } else {
                        // This is not a callable Unit.
                        previous = previous.getParent();
                        continue;
                    }
                }
                if (previous.getNextSibling() instanceof Statement) {
                    nextStep = (Statement) previous.getNextSibling();
                    break;
                } else {
                    previous = previous.getParent();
                }
            }
            current.next().executeLNode(this);
        }
    }

    /**
     * Step into the first statement of current statement's callable unit.(Function, Action, TypeMapper)
     */
    public synchronized void stepIn() {
        if (done) {
            throw new IllegalStateException("Can't Step In. Ballerina Program execution completed.");
        } else {
            LinkedNode current = currentHaltNode;
            this.currentHaltNode = null;
            this.stepNextStatement = true;
            // Currently we are stepping though statements.
            // We always step in to next Statement.
            current.next().executeLNode(this);
        }
    }

    /**
     * Step out from current callable unit, and move to next sibling statement of the parent.
     */
    public synchronized void stepOut() {
        if (done) {
            throw new IllegalStateException("Can't Step Out. Ballerina Program execution completed.");
        } else {
            LinkedNode current = currentHaltNode;
            this.currentHaltNode = null;
            this.stepOutFromCallableUnit = true;
            current.next().executeLNode(this);
        }
    }

    /**
     * Check whether execution is completed.
     *
     * @return true, if debug execution is completed.
     */
    public boolean isDone() {
        return done;
    }

    /**
     * Try to execute given statement.
     *
     * @param statement to be executed.
     */
    private synchronized void tryNext(AbstractStatement statement) {
        if (stepOutFromCallableUnit) {
            statement.next.executeLNode(this);
        } else if (stepNextStatement || positionHashMap.containsKey(statement.getNodeLocation().toString()) ||
                statement == nextStep) {
            currentHaltNode = statement;
            stepOutFromCallableUnit = false;
            stepNextStatement = false;
            nextStep = null;
            if (observer != null) {
                observer.notifyHalt(getBreakPointInfo());
            }
        } else {
            statement.next.executeLNode(this);
        }
    }

    /**
     * Return BreakPointInfo object for current debug execution.
     *
     * @return calculated BreakPoint info object.
     */
    public BreakPointInfo getBreakPointInfo() {
        NodeLocation current;
        if (this.currentHaltNode != null) {
            Statement statement = (Statement) this.currentHaltNode;
            current = statement.getNodeLocation();
        } else {
            current = new NodeLocation("unknown", -1);
        }
        BreakPointInfo breakPointInfo = new BreakPointInfo(current);
        for (StackFrame stackFrame : bContext.getControlStack().getStack()) {
            String pck =
                    (stackFrame.getNodeInfo().getPackage() == null ? "default" : stackFrame.getNodeInfo().getPackage());
            String functionName = stackFrame.getNodeInfo().getName();
            NodeLocation location = stackFrame.getNodeInfo().getNodeLocation();
            FrameInfo frameInfo = new FrameInfo(pck, functionName, location.getFileName(), location.getLineNumber());
            HashMap<SymbolName, AbstractMap.SimpleEntry<Integer, String>> variables = stackFrame.variables;
            for (SymbolName name : variables.keySet()) {
                AbstractMap.SimpleEntry<Integer, String> offSet = variables.get(name);
                BValue value = null;
                switch (offSet.getValue()) {
                    case "Arg":
                    case "Local":
                        value = stackFrame.values[offSet.getKey()];
                        break;
                    case "Service":
                    case "Const":
                        value = runtimeEnvironment.getStaticMemory().getValue(offSet.getKey());
                        break;
                    case "Connector":
                        BConnector bConnector = (BConnector) stackFrame.values[0];
                        if (bConnector != null) {
                            bConnector.getValue(offSet.getKey());
                        }
                        break;
                    default:
                        value = null;
                }
                VariableInfo variableInfo = new VariableInfo(name.getName(), offSet.getValue());
                variableInfo.setBValue(value);
                frameInfo.addVariableInfo(variableInfo);
            }
            breakPointInfo.addFrameInfo(frameInfo);
        }
        return breakPointInfo;
    }
    
    /* Overwritten Classes */

    @Override
    public void visit(ActionInvocationStmt actionIStmt) {
        super.visit(actionIStmt);
        tryNext(actionIStmt);
    }

    @Override
    public void visit(AssignStmt assignStmt) {
        super.visit(assignStmt);
        tryNext(assignStmt);
    }

    @Override
    public void visit(BlockStmt blockStmt) {
        super.visit(blockStmt);
        blockStmt.next.executeLNode(this);
    }

    @Override
    public void visit(BreakStmt breakStmt) {
        super.visit(breakStmt);
        tryNext(breakStmt);
    }

    @Override
    public void visit(ForeachStmt foreachStmt) {
        super.visit(foreachStmt);
        tryNext(foreachStmt);
    }

    @Override
    public void visit(ForkJoinStmt forkJoinStmt) {
        super.visit(forkJoinStmt);
        tryNext(forkJoinStmt);
    }

    @Override
    public void visit(FunctionInvocationStmt funcIStmt) {
        super.visit(funcIStmt);
        tryNext(funcIStmt);
    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        super.visit(ifElseStmt);
        tryNext(ifElseStmt);
    }

    @Override
    public void visit(ReplyStmt replyStmt) {
        super.visit(replyStmt);
        tryNext(replyStmt);
    }

    @Override
    public void visit(ReturnStmt returnStmt) {
        super.visit(returnStmt);
        tryNext(returnStmt);
    }

    @Override
    public void visit(ThrowStmt throwStmt) {
        super.visit(throwStmt);
        tryNext(throwStmt);
    }

    @Override
    public void visit(TryCatchStmt tryCatchStmt) {
        super.visit(tryCatchStmt);
        tryNext(tryCatchStmt);
    }

    @Override
    public void visit(VariableDefStmt variableDefStmt) {
        super.visit(variableDefStmt);
        tryNext(variableDefStmt);
    }

    @Override
    public void visit(WhileStmt whileStmt) {
        super.visit(whileStmt);
        tryNext(whileStmt);
    }

    @Override
    public void visit(ActionInvocationExpr actionInvocationExpr) {
        super.visit(actionInvocationExpr);
        actionInvocationExpr.next.executeLNode(this);
    }

    @Override
    public void visit(ArrayInitExpr arrayInitExpr) {
        super.visit(arrayInitExpr);
        arrayInitExpr.next.executeLNode(this);
    }

    @Override
    public void visit(ArrayMapAccessExpr arrayMapAccessExpr) {
        super.visit(arrayMapAccessExpr);
        arrayMapAccessExpr.next.executeLNode(this);
    }

    @Override
    public void visit(BacktickExpr backtickExpr) {
        super.visit(backtickExpr);
        backtickExpr.next.executeLNode(this);
    }

    @Override
    public void visit(BasicLiteral basicLiteral) {
        super.visit(basicLiteral);
        basicLiteral.next.executeLNode(this);
    }

    @Override
    public void visit(BinaryExpression expression) {
        super.visit(expression);
        expression.next.executeLNode(this);
    }

    @Override
    public void visit(ConnectorInitExpr connectorInitExpr) {
        super.visit(connectorInitExpr);
        connectorInitExpr.next.executeLNode(this);
    }

    @Override
    public void visit(FunctionInvocationExpr functionInvocationExpr) {
        super.visit(functionInvocationExpr);
        functionInvocationExpr.next.executeLNode(this);
    }

    @Override
    public void visit(InstanceCreationExpr instanceCreationExpr) {
        super.visit(instanceCreationExpr);
        instanceCreationExpr.next.executeLNode(this);
    }

    @Override
    public void visit(MapInitExpr mapInitExpr) {
        super.visit(mapInitExpr);
        mapInitExpr.next.executeLNode(this);
    }

    @Override
    public void visit(MapStructInitKeyValueExpr mapStructInitKeyValueExpr) {
        super.visit(mapStructInitKeyValueExpr);
        mapStructInitKeyValueExpr.next.executeLNode(this);
    }

    @Override
    public void visit(RefTypeInitExpr refTypeInitExpr) {
        super.visit(refTypeInitExpr);
        refTypeInitExpr.next.executeLNode(this);
    }

    @Override
    public void visit(ResourceInvocationExpr resourceInvocationExpr) {
        super.visit(resourceInvocationExpr);
        // There is next Statement for ResourceInvocationExpr.
    }

    @Override
    public void visit(StructFieldAccessExpr structFieldAccessExpr) {
        super.visit(structFieldAccessExpr);
        structFieldAccessExpr.next.executeLNode(this);
    }

    @Override
    public void visit(StructInitExpr structInitExpr) {
        super.visit(structInitExpr);
        structInitExpr.next.executeLNode(this);
    }

    @Override
    public void visit(TypeCastExpression typeCastExpression) {
        super.visit(typeCastExpression);
        typeCastExpression.next.executeLNode(this);
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        super.visit(unaryExpression);
        unaryExpression.next.executeLNode(this);
    }

    @Override
    public void visit(VariableRefExpr variableRefExpr) {
        super.visit(variableRefExpr);
        variableRefExpr.next.executeLNode(this);
    }

    @Override
    public BValue visit(StackVarLocation stackVarLocation) {
        return super.visit(stackVarLocation);
    }

    @Override
    public BValue visit(ConstantLocation constantLocation) {
        return super.visit(constantLocation);
    }

    @Override
    public BValue visit(ServiceVarLocation serviceVarLocation) {
        return super.visit(serviceVarLocation);
    }

    @Override
    public BValue visit(StructVarLocation structLocation) {
        return super.visit(structLocation);
    }

    @Override
    public BValue visit(ConnectorVarLocation connectorVarLocation) {
        return super.visit(connectorVarLocation);
    }

    @Override
    public void visit(EndNode endNode) {
        super.visit(endNode);
        this.done = true;
        this.currentHaltNode = null;
        if (observer != null) {
            observer.notifyComplete();
        }
    }

    @Override
    public void visit(ExitNode exitNode) {
        // We don't want to call system exit here.
        this.done = true;
        this.currentHaltNode = null;
        if (observer != null) {
            observer.notifyExit();
        }
    }

    @Override
    public void visit(GotoNode gotoNode) {
        super.visit(gotoNode);
        Integer pop = branchIDStack.pop();
        if (stepOutFromCallableUnit) {
            this.stepNextStatement = true;
            this.stepOutFromCallableUnit = false;
        }
        gotoNode.next(pop).executeLNode(this);
    }

    @Override
    public void visit(IfElseNode ifElseNode) {
        super.visit(ifElseNode);
    }

    @Override
    public void visit(AssignStmtEndNode assignStmtEndNode) {
        super.visit(assignStmtEndNode);
        Expression[] lExprs = assignStmtEndNode.getStatement().getLExprs();
        for (Expression expression : lExprs) {
            if (expression instanceof VariableRefExpr) {
                VariableRefExpr variableRefExpr = (VariableRefExpr) expression;
                String scope = "Unknown";
                int offset = -1;
                if (variableRefExpr.getMemoryLocation() instanceof StackVarLocation) {
                    scope = "Local";
                    offset = ((StackVarLocation) variableRefExpr.getMemoryLocation()).getStackFrameOffset();
                } else if (variableRefExpr.getMemoryLocation() instanceof ConstantLocation) {
                    scope = "Const";
                    offset = ((ConstantLocation) variableRefExpr.getMemoryLocation()).getStaticMemAddrOffset();
                } else if (variableRefExpr.getMemoryLocation() instanceof ServiceVarLocation) {
                    scope = "Service";
                    offset = ((ServiceVarLocation) variableRefExpr.getMemoryLocation()).getStaticMemAddrOffset();
                } else if (variableRefExpr.getMemoryLocation() instanceof ConnectorVarLocation) {
                    scope = "Connector";
                    offset = ((ConnectorVarLocation) variableRefExpr.getMemoryLocation())
                            .getConnectorMemAddrOffset();
                } else if (variableRefExpr.getMemoryLocation() instanceof StructVarLocation) {
                    scope = "Struct";
                    offset = ((StructVarLocation) variableRefExpr.getMemoryLocation())
                            .getStructMemAddrOffset();
                }
                bContext.getControlStack().getCurrentFrame().variables.put(
                        variableRefExpr.getSymbolName(), new AbstractMap.SimpleEntry<>(offset, scope));
            }
        }
        assignStmtEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(ReplyStmtEndNode replyStmtEndNode) {
        super.visit(replyStmtEndNode);
        replyStmtEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(ReturnStmtEndNode returnStmtEndNode) {
        super.visit(returnStmtEndNode);
        returnStmtEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(VariableDefStmtEndNode variableDefStmtEndNode) {
        super.visit(variableDefStmtEndNode);
        variableDefStmtEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(ActionInvocationExprStartNode startNode) {
        super.visit(startNode);
        int i = 0;
        for (ParameterDef parameter : startNode.getExpression().getCallableUnit().getParameterDefs()) {
            bContext.getControlStack().getCurrentFrame().variables.put(
                    parameter.getSymbolName(), new AbstractMap.SimpleEntry<>(i, "Arg"));
            i++;
        }
        startNode.next.executeLNode(this);
    }

    @Override
    public void visit(ArrayInitExprEndNode arrayInitExprEndNode) {
        super.visit(arrayInitExprEndNode);
        arrayInitExprEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(ArrayMapAccessExprEndNode arrayMapAccessExprEndNode) {
        super.visit(arrayMapAccessExprEndNode);
        arrayMapAccessExprEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(BacktickExprEndNode backtickExprEndNode) {
        super.visit(backtickExprEndNode);
        backtickExprEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(BinaryExpressionEndNode binaryExpressionEndNode) {
        super.visit(binaryExpressionEndNode);
        binaryExpressionEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(FunctionInvocationExprStartNode startNode) {
        super.visit(startNode);
        int i = 0;
        for (ParameterDef parameter : startNode.getExpression().getCallableUnit().getParameterDefs()) {
            bContext.getControlStack().getCurrentFrame().variables.put(
                    parameter.getSymbolName(), new AbstractMap.SimpleEntry<>(i, "Arg"));
            i++;
        }
        startNode.next.executeLNode(this);
    }

    @Override
    public void visit(StructFieldAccessExprEndNode structFieldAccessExprEndNode) {
        super.visit(structFieldAccessExprEndNode);
        structFieldAccessExprEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(StructInitExprEndNode structInitExprEndNode) {
        super.visit(structInitExprEndNode);
        structInitExprEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(TypeCastExpressionEndNode endNode) {
        super.visit(endNode);
        int i = 0;
        for (ParameterDef parameter : endNode.getExpression().getCallableUnit().getParameterDefs()) {
            bContext.getControlStack().getCurrentFrame().variables.put(
                    parameter.getSymbolName(), new AbstractMap.SimpleEntry<>(i, "Arg"));
            i++;
        }
        endNode.next.executeLNode(this);
    }

    @Override
    public void visit(UnaryExpressionEndNode unaryExpressionEndNode) {
        super.visit(unaryExpressionEndNode);
        unaryExpressionEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(RefTypeInitExprEndNode refTypeInitExprEndNode) {
        super.visit(refTypeInitExprEndNode);
        refTypeInitExprEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(CallableUnitEndNode callableUnitEndNode) {
        super.visit(callableUnitEndNode);
        callableUnitEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(ConnectorInitExprEndNode connectorInitExprEndNode) {
        super.visit(connectorInitExprEndNode);
        connectorInitExprEndNode.next.executeLNode(this);
    }

    @Override
    public void visit(InvokeNativeActionNode invokeNativeActionNode) {
        super.visit(invokeNativeActionNode);
        invokeNativeActionNode.next.executeLNode(this);
    }

    @Override
    public void visit(InvokeNativeFunctionNode invokeNativeFunctionNode) {
        super.visit(invokeNativeFunctionNode);
        invokeNativeFunctionNode.next.executeLNode(this);
    }

    @Override
    public void visit(InvokeNativeTypeMapperNode invokeNativeTypeMapperNode) {
        super.visit(invokeNativeTypeMapperNode);
        invokeNativeTypeMapperNode.next.executeLNode(this);
    }

    @Override
    public void visit(MapInitExprEndNode mapInitExprEndNode) {
        super.visit(mapInitExprEndNode);
        mapInitExprEndNode.next.executeLNode(this);
    }

}

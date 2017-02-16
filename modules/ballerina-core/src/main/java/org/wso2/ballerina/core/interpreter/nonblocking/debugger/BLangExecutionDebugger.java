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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.interpreter.ConnectorVarLocation;
import org.wso2.ballerina.core.interpreter.ConstantLocation;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.interpreter.RuntimeEnvironment;
import org.wso2.ballerina.core.interpreter.ServiceVarLocation;
import org.wso2.ballerina.core.interpreter.StackFrame;
import org.wso2.ballerina.core.interpreter.StackVarLocation;
import org.wso2.ballerina.core.interpreter.StructVarLocation;
import org.wso2.ballerina.core.interpreter.nonblocking.BLangAbstractExecutionVisitor;
import org.wso2.ballerina.core.model.LinkedNode;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.expressions.Expression;
import org.wso2.ballerina.core.model.expressions.FunctionInvocationExpr;
import org.wso2.ballerina.core.model.expressions.ResourceInvocationExpr;
import org.wso2.ballerina.core.model.expressions.VariableRefExpr;
import org.wso2.ballerina.core.model.nodes.EndNode;
import org.wso2.ballerina.core.model.nodes.ExitNode;
import org.wso2.ballerina.core.model.nodes.GotoNode;
import org.wso2.ballerina.core.model.nodes.fragments.expressions.ActionInvocationExprStartNode;
import org.wso2.ballerina.core.model.nodes.fragments.expressions.FunctionInvocationExprStartNode;
import org.wso2.ballerina.core.model.nodes.fragments.expressions.TypeCastExpressionEndNode;
import org.wso2.ballerina.core.model.nodes.fragments.statements.AssignStmtEndNode;
import org.wso2.ballerina.core.model.nodes.fragments.statements.VariableDefStmtEndNode;
import org.wso2.ballerina.core.model.statements.AbstractStatement;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.statements.CommentStmt;
import org.wso2.ballerina.core.model.statements.IfElseStmt;
import org.wso2.ballerina.core.model.statements.Statement;
import org.wso2.ballerina.core.model.statements.WhileStmt;
import org.wso2.ballerina.core.model.statements.WorkerInvocationStmt;
import org.wso2.ballerina.core.model.statements.WorkerReplyStmt;
import org.wso2.ballerina.core.model.values.BConnector;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.runtime.Constants;

import java.util.AbstractMap;
import java.util.HashMap;

/**
 * {@link  BLangExecutionDebugger} is the debug enabled BLangExecutor.
 *
 * @since 0.8.0
 */
public class BLangExecutionDebugger extends BLangAbstractExecutionVisitor {

    private static final Logger logger = LoggerFactory.getLogger(Constants.BAL_LINKED_INTERPRETER_LOGGER);
    private HashMap<String, NodeLocation> positionHashMap;

    private Context bContext;
    private RuntimeEnvironment runtimeEnvironment;

    private LinkedNode currentHaltNode;
    private Statement nextStep;
    private boolean done, stepInToStatement, stepOutFromCallableUnit;
    private DebugSessionObserver observer;

    public BLangExecutionDebugger(RuntimeEnvironment runtimeEnv, Context bContext) {
        super(runtimeEnv, bContext);
        this.bContext = bContext;
        this.runtimeEnvironment = runtimeEnv;
        this.positionHashMap = new HashMap<>();
        this.done = false;
        this.stepInToStatement = false;
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
            this.stepInToStatement = false;
            continueExecution(current);
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
            if (current instanceof IfElseStmt || current instanceof WhileStmt) {
                // Step Over has to goto into IfElseStmt/WhileStmt body.
                stepInToStatement = true;
            } else {
                // Currently we are stepping though statements, But this has to stop at current statement's next
                // sibling.
                LinkedNode previous = current;
                while (nextStep == null) {
                    if (previous instanceof BlockStmt) {
                        if (((BlockStmt) previous).getGotoNode() != null) {
                            // This is a callable Unit. We have to step-in here.
                            this.stepInToStatement = true;
                            break;
                        } else {
                            // This is not a callable Unit.
                            previous = previous.getParent();
                            continue;
                        }
                    }
                    if (previous.getNextSibling() instanceof Statement &&
                            !(previous.getNextSibling() instanceof CommentStmt)) {
                        nextStep = (Statement) previous.getNextSibling();
                        break;
                    } else {
                        previous = previous.getParent();
                    }
                }
            }
            continueExecution(current);
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
            this.stepInToStatement = true;
            // Currently we are stepping though statements.
            // We always step in to next Statement.
            continueExecution(current);
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
            continueExecution(current);
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
        if (statement instanceof CommentStmt || stepOutFromCallableUnit) {
            statement.accept(this);
        } else if (stepInToStatement || positionHashMap.containsKey(statement.getNodeLocation().toString()) ||
                statement == nextStep) {
            currentHaltNode = statement;
            stepOutFromCallableUnit = false;
            stepInToStatement = false;
            nextStep = null;
            next = null;
            if (observer != null) {
                observer.notifyHalt(getBreakPointInfo());
            }
        } else {
            statement.accept(this);
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
            if (null != variables) {
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
            }
            breakPointInfo.addFrameInfo(frameInfo);
        }
        return breakPointInfo;
    }

    public void execute(ResourceInvocationExpr resourceIExpr) {
        continueExecution(resourceIExpr);
    }

    public void execute(FunctionInvocationExpr functionInvocationExpr) {
        continueExecution(functionInvocationExpr);
    }

    public void continueExecution(LinkedNode linkedNode) {
        linkedNode.accept(this);
        while (next != null) {
            if (next instanceof AbstractStatement && !(next instanceof BlockStmt)) {
                tryNext((AbstractStatement) next);
            } else {
                next.accept(this);
            }
        }
    }

    /* Overwritten Classes */

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
        next = null;
        this.done = true;
        this.currentHaltNode = null;
        if (observer != null) {
            observer.notifyExit();
        }
    }

    @Override
    public void visit(GotoNode gotoNode) {
        Integer pop = branchIDStack.pop();
        if (logger.isDebugEnabled()) {
            logger.debug("Executing GotoNode branch:{}", pop);
        }
        if (stepOutFromCallableUnit) {
            this.stepInToStatement = true;
            this.stepOutFromCallableUnit = false;
        }
        next = gotoNode.next(pop);
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
    }

    @Override
    public void visit(VariableDefStmtEndNode variableDefStmtEndNode) {
        super.visit(variableDefStmtEndNode);
        Expression expression = variableDefStmtEndNode.getStatement().getLExpr();
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

    @Override
    public void visit(WorkerInvocationStmt workerInvocationStmt) {
        super.visit(workerInvocationStmt);
        tryNext(workerInvocationStmt);
    }

    @Override
    public void visit(WorkerReplyStmt workerReplyStmt) {
        super.visit(workerReplyStmt);
        tryNext(workerReplyStmt);
    }

    @Override
    public void visit(ActionInvocationExprStartNode startNode) {
        super.visit(startNode);
        if (startNode.getExpression().getCallableUnit() != null) {
            int i = 0;
            for (ParameterDef parameter : startNode.getExpression().getCallableUnit().getParameterDefs()) {
                bContext.getControlStack().getCurrentFrame().variables.put(
                        parameter.getSymbolName(), new AbstractMap.SimpleEntry<>(i, "Arg"));
                i++;
            }
        }
    }

    @Override
    public void visit(FunctionInvocationExprStartNode startNode) {
        super.visit(startNode);
        int i = 0;
        if (startNode.getExpression().getCallableUnit() != null) {
            for (ParameterDef parameter : startNode.getExpression().getCallableUnit().getParameterDefs()) {
                bContext.getControlStack().getCurrentFrame().variables.put(
                        parameter.getSymbolName(), new AbstractMap.SimpleEntry<>(i, "Arg"));
                i++;
            }
        }
    }

    @Override
    public void visit(TypeCastExpressionEndNode endNode) {
        super.visit(endNode);
        if (endNode.getExpression().getCallableUnit() != null) {
            int i = 0;
            for (ParameterDef parameter : endNode.getExpression().getCallableUnit().getParameterDefs()) {
                bContext.getControlStack().getCurrentFrame().variables.put(
                        parameter.getSymbolName(), new AbstractMap.SimpleEntry<>(i, "Arg"));
                i++;
            }
        }
    }

    public void clearDebugPoints() {
        positionHashMap.clear();
    }
}

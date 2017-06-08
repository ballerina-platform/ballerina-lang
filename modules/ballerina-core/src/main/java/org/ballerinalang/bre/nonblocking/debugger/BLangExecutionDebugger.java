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
package org.ballerinalang.bre.nonblocking.debugger;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.RuntimeEnvironment;
import org.ballerinalang.model.LinkedNode;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.expressions.ResourceInvocationExpr;
import org.ballerinalang.model.statements.AbstractStatement;
import org.ballerinalang.model.statements.TransformStmt;

/**
 * {@link  BLangExecutionDebugger} is the debug enabled BLangExecutor.
 *
 * @since 0.8.0
 */
public class BLangExecutionDebugger { // extends BLangAbstractExecutionVisitor {

//    private static final Logger logger = LoggerFactory.getLogger(Constants.BAL_LINKED_INTERPRETER_LOGGER);
//    private HashMap<String, NodeLocation> positionHashMap;
//
//    private LinkedNode currentHaltNode;
//    private Statement nextStep;
//    private boolean done, stepInToStatement, stepOutFromCallableUnit;
//    private DebugSessionObserver observer;

    public BLangExecutionDebugger(RuntimeEnvironment runtimeEnv, Context bContext) {
//        this.positionHashMap = new HashMap<>();
//        this.done = false;
//        this.stepInToStatement = false;
//        this.stepOutFromCallableUnit = false;
    }

    public void setDebugSessionObserver(DebugSessionObserver observer) {
//        this.observer = observer;
    }

    public void addDebugPoint(NodeLocation nodeLocation) {
//        positionHashMap.put(nodeLocation.toString(), nodeLocation);
    }

    public void addDebugPoints(NodeLocation[] nodeLocations) {
//        for (NodeLocation nodeLocation : nodeLocations) {
//            positionHashMap.put(nodeLocation.toString(), nodeLocation);
//        }
    }

    public void removeDebugPoint(NodeLocation position) {
//        positionHashMap.remove(position.toString());
    }

    /**
     * Resume Execution.
     */
    public synchronized void resume() {
//        if (done) {
//            throw new IllegalStateException("Can't resume. Ballerina Program execution completed.");
//        } else {
//            LinkedNode current = currentHaltNode;
//            this.currentHaltNode = null;
//            this.stepInToStatement = false;
//            continueExecution(current);
//        }
    }

    /**
     * Step Over to next sibling statement. If there is no next sibling statement, try to execute parents's next
     * sibling.
     */
    public synchronized void stepOver() {
//        if (done) {
//            throw new IllegalStateException("Can't Step Over. Ballerina Program execution completed.");
//        } else {
//            LinkedNode current = currentHaltNode;
//            currentHaltNode = null;
//            if (current instanceof IfElseStmt || current instanceof WhileStmt) {
//                // Step Over has to goto into IfElseStmt/WhileStmt body.
//                stepInToStatement = true;
//            } else {
//                // Currently we are stepping though statements, But this has to stop at current statement's next
//                // sibling.
//                LinkedNode previous = current;
//                while (nextStep == null) {
//                    if (previous instanceof BlockStmt) {
//                        if (((BlockStmt) previous).getGotoNode() != null) {
//                            // This is a callable Unit. We have to step-in here.
//                            this.stepInToStatement = true;
//                            break;
//                        } else {
//                            // This is not a callable Unit.
//                            previous = previous.getParent();
//                            continue;
//                        }
//                    }
//                    if (null == previous) {
//                        break;
//                    } else if (previous.getNextSibling() instanceof Statement &&
//                            !(previous.getNextSibling() instanceof CommentStmt)) {
//                        nextStep = (Statement) previous.getNextSibling();
//                        break;
//                    } else {
//                        previous = previous.getParent();
//                    }
//                }
//            }
//            continueExecution(current);
//        }
    }

    /**
     * Step into the first statement of current statement's callable unit (Function, Action, TypeMapper).
     */
    public synchronized void stepIn() {
//        if (done) {
//            throw new IllegalStateException("Can't Step In. Ballerina Program execution completed.");
//        } else {
//            LinkedNode current = currentHaltNode;
//            this.currentHaltNode = null;
//            this.stepInToStatement = true;
//            // Currently we are stepping though statements.
//            // We always step in to next Statement.
//            continueExecution(current);
//        }
    }

    /**
     * Step out from current callable unit, and move to next sibling statement of the parent.
     */
    public synchronized void stepOut() {
//        if (done) {
//            throw new IllegalStateException("Can't Step Out. Ballerina Program execution completed.");
//        } else {
//            LinkedNode current = currentHaltNode;
//            this.currentHaltNode = null;
//            this.stepOutFromCallableUnit = true;
//            continueExecution(current);
//        }
    }

    /**
     * Check whether execution is completed.
     *
     * @return true, if debug execution is completed.
     */
    public boolean isDone() {
        return true;
//        return done;
    }

    /**
     * Try to execute given statement.
     *
     * @param statement to be executed.
     */
    private synchronized void tryNext(AbstractStatement statement) {
//        if (statement instanceof CommentStmt || stepOutFromCallableUnit) {
//            statement.accept(this);
//        } else if (stepInToStatement || positionHashMap.containsKey(statement.getNodeLocation().toString()) ||
//                statement == nextStep) {
//            currentHaltNode = statement;
//            stepOutFromCallableUnit = false;
//            stepInToStatement = false;
//            nextStep = null;
//            next = null;
//            if (observer != null) {
//                observer.notifyHalt(getBreakPointInfo());
//            }
//        } else {
//            statement.accept(this);
//        }
    }

    /**
     * Return BreakPointInfo object for current debug execution.
     *
     * @return calculated BreakPoint info object.
     */
    public BreakPointInfo getBreakPointInfo() {
        return null;
//        NodeLocation current;
//        if (this.currentHaltNode != null) {
//            Statement statement = (Statement) this.currentHaltNode;
//            current = statement.getNodeLocation();
//        } else {
//            current = new NodeLocation("unknown", -1);
//        }
//        BreakPointInfo breakPointInfo = new BreakPointInfo(current);
//        for (StackFrame stackFrame : bContext.getControlStack().getStack()) {
//            String pck =
//                    (stackFrame.getNodeInfo().getPackage() == null ? "default" : stackFrame.getNodeInfo()
// .getPackage());
//            String functionName = stackFrame.getNodeInfo().getName();
//            NodeLocation location = stackFrame.getNodeInfo().getNodeLocation();
//            FrameInfo frameInfo = new FrameInfo(pck, functionName, location.getFileName(), location.getLineNumber());
//            HashMap<SymbolName, AbstractMap.SimpleEntry<Integer, String>> variables = stackFrame.variables;
//            if (null != variables) {
//                for (SymbolName name : variables.keySet()) {
//                    AbstractMap.SimpleEntry<Integer, String> offSet = variables.get(name);
//                    BValue value = null;
//                    switch (offSet.getValue()) {
//                        case "Arg":
//                        case "Local":
//                            value = stackFrame.values[offSet.getKey()];
//                            break;
//                        case "Service":
//                        case "Const":
//                            value = runtimeEnvironment.getStaticMemory().getValue(offSet.getKey());
//                            break;
//                        case "Connector":
//                            BConnector bConnector = (BConnector) stackFrame.values[0];
//                            if (bConnector != null) {
//                                bConnector.getValue(offSet.getKey());
//                            }
//                            break;
//                        default:
//                            value = null;
//                    }
//                    VariableInfo variableInfo = new VariableInfo(name.getName(), offSet.getValue());
//                    variableInfo.setBValue(value);
//                    frameInfo.addVariableInfo(variableInfo);
//                }
//            }
//            breakPointInfo.addFrameInfo(frameInfo);
//        }
//        return breakPointInfo;
    }

    public void execute(ResourceInvocationExpr resourceIExpr) {
//        continueExecution(resourceIExpr);
    }

    public void execute(FunctionInvocationExpr functionInvocationExpr) {
//        continueExecution(functionInvocationExpr);
    }

    public void continueExecution(LinkedNode linkedNode) {
//        linkedNode.accept(this);
//        while (next != null) {
//            try {
//                if (next instanceof AbstractStatement && !(next instanceof BlockStmt)) {
//                    tryNext((AbstractStatement) next);
//                } else {
//                    next.accept(this);
//                }
//            } catch (RuntimeException e) {
//                createBErrorFromException(e, null);
//                handleBException();
//            }
//        }
    }

    public void continueExecution() {
//        while (next != null) {
//            try {
//                if (next instanceof AbstractStatement && !(next instanceof BlockStmt)) {
//                    tryNext((AbstractStatement) next);
//                } else {
//                    next.accept(this);
//                }
//            } catch (RuntimeException e) {
//                createBErrorFromException(e, null);
//                handleBException();
//            }
//        }
    }

    /* Overwritten Classes */

//    public void visit(AssignStmtEndNode assignStmtEndNode) {
//        Expression[] lExprs = assignStmtEndNode.getStatement().getLExprs();
//        for (Expression expression : lExprs) {
//            if (expression instanceof VariableRefExpr) {
//                VariableRefExpr variableRefExpr = (VariableRefExpr) expression;
//                String scope = "Unknown";
//                int offset = -1;
//                if (variableRefExpr.getMemoryLocation() instanceof StackVarLocation) {
//                    scope = "Local";
//                    offset = ((StackVarLocation) variableRefExpr.getMemoryLocation()).getStackFrameOffset();
//                } else if (variableRefExpr.getMemoryLocation() instanceof ConstantLocation) {
//                    scope = "Const";
//                    offset = ((ConstantLocation) variableRefExpr.getMemoryLocation()).getStaticMemAddrOffset();
//                } else if (variableRefExpr.getMemoryLocation() instanceof ServiceVarLocation) {
//                    scope = "Service";
//                    offset = ((ServiceVarLocation) variableRefExpr.getMemoryLocation()).getStaticMemAddrOffset();
//                } else if (variableRefExpr.getMemoryLocation() instanceof ConnectorVarLocation) {
//                    scope = "Connector";
//                    offset = ((ConnectorVarLocation) variableRefExpr.getMemoryLocation())
//                            .getConnectorMemAddrOffset();
//                } else if (variableRefExpr.getMemoryLocation() instanceof StructVarLocation) {
//                    scope = "Struct";
//                    offset = ((StructVarLocation) variableRefExpr.getMemoryLocation())
//                            .getStructMemAddrOffset();
//                }
////                bContext.getControlStack().getCurrentFrame().variables.put(
////                        variableRefExpr.getSymbolName(), new AbstractMap.SimpleEntry<>(offset, scope));
//            }
//        }
//    }

//    public void visit(VariableDefStmtEndNode variableDefStmtEndNode) {
//        Expression expression = variableDefStmtEndNode.getStatement().getLExpr();
//        if (expression instanceof VariableRefExpr) {
//            VariableRefExpr variableRefExpr = (VariableRefExpr) expression;
//            String scope = "Unknown";
//            int offset = -1;
//            if (variableRefExpr.getMemoryLocation() instanceof StackVarLocation) {
//                scope = "Local";
//                offset = ((StackVarLocation) variableRefExpr.getMemoryLocation()).getStackFrameOffset();
//            } else if (variableRefExpr.getMemoryLocation() instanceof ConstantLocation) {
//                scope = "Const";
//                offset = ((ConstantLocation) variableRefExpr.getMemoryLocation()).getStaticMemAddrOffset();
//            } else if (variableRefExpr.getMemoryLocation() instanceof ServiceVarLocation) {
//                scope = "Service";
//                offset = ((ServiceVarLocation) variableRefExpr.getMemoryLocation()).getStaticMemAddrOffset();
//            } else if (variableRefExpr.getMemoryLocation() instanceof ConnectorVarLocation) {
//                scope = "Connector";
//                offset = ((ConnectorVarLocation) variableRefExpr.getMemoryLocation())
//                        .getConnectorMemAddrOffset();
//            } else if (variableRefExpr.getMemoryLocation() instanceof StructVarLocation) {
//                scope = "Struct";
//                offset = ((StructVarLocation) variableRefExpr.getMemoryLocation())
//                        .getStructMemAddrOffset();
//            }
//            bContext.getControlStack().getCurrentFrame().variables.put(
//                    variableRefExpr.getSymbolName(), new AbstractMap.SimpleEntry<>(offset, scope));
//        }
//    }


//    public void visit(ResourceInvocationExpr resourceIExpr) {
//        if (resourceIExpr.getResource().getParameterDefs() != null) {
//            int i = 0;
//            for (ParameterDef parameter : resourceIExpr.getResource().getParameterDefs()) {
//                bContext.getControlStack().getCurrentFrame().variables.put(
//                        parameter.getSymbolName(), new AbstractMap.SimpleEntry<>(i, "Arg"));
//                i++;
//            }
//        }
//    }

//    public void visit(ActionInvocationExprStartNode startNode) {
//        if (startNode.getExpression().getCallableUnit() != null) {
//            int i = 0;
//            for (ParameterDef parameter : startNode.getExpression().getCallableUnit().getParameterDefs()) {
//                bContext.getControlStack().getCurrentFrame().variables.put(
//                        parameter.getSymbolName(), new AbstractMap.SimpleEntry<>(i, "Arg"));
//                i++;
//            }
//        }
//    }

//    public void visit(FunctionInvocationExprStartNode startNode) {
//        int i = 0;
//        if (startNode.getExpression().getCallableUnit() != null) {
//            for (ParameterDef parameter : startNode.getExpression().getCallableUnit().getParameterDefs()) {
//                bContext.getControlStack().getCurrentFrame().variables.put(
//                        parameter.getSymbolName(), new AbstractMap.SimpleEntry<>(i, "Arg"));
//                i++;
//            }
//        }
//    }

//    public void visit(TypeCastExpressionEndNode endNode) {
//        if (endNode.getExpression().getCallableUnit() != null) {
//            int i = 0;
//            for (ParameterDef parameter : endNode.getExpression().getCallableUnit().getParameterDefs()) {
//                bContext.getControlStack().getCurrentFrame().variables.put(
//                        parameter.getSymbolName(), new AbstractMap.SimpleEntry<>(i, "Arg"));
//                i++;
//            }
//        }
//    }

    public void visit(TransformStmt transformStmt){

    }

    public void clearDebugPoints() {
//        positionHashMap.clear();
    }
}

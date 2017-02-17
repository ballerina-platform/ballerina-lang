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
package org.ballerinalang.bre.nonblocking;

import org.ballerinalang.bre.BLangExecutor;
import org.ballerinalang.bre.CallableUnitInfo;
import org.ballerinalang.bre.ConnectorVarLocation;
import org.ballerinalang.bre.ConstantLocation;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.ControlStack;
import org.ballerinalang.bre.MemoryLocation;
import org.ballerinalang.bre.RuntimeEnvironment;
import org.ballerinalang.bre.ServiceVarLocation;
import org.ballerinalang.bre.StackFrame;
import org.ballerinalang.bre.StackVarLocation;
import org.ballerinalang.bre.StructVarLocation;
import org.ballerinalang.bre.WorkerRunner;
import org.ballerinalang.bre.WorkerVarLocation;
import org.ballerinalang.model.Action;
import org.ballerinalang.model.BallerinaConnectorDef;
import org.ballerinalang.model.Connector;
import org.ballerinalang.model.Function;
import org.ballerinalang.model.LinkedNode;
import org.ballerinalang.model.NodeLocation;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.TypeMapper;
import org.ballerinalang.model.VariableDef;
import org.ballerinalang.model.Worker;
import org.ballerinalang.model.expressions.ActionInvocationExpr;
import org.ballerinalang.model.expressions.ArrayInitExpr;
import org.ballerinalang.model.expressions.ArrayMapAccessExpr;
import org.ballerinalang.model.expressions.BacktickExpr;
import org.ballerinalang.model.expressions.BasicLiteral;
import org.ballerinalang.model.expressions.BinaryExpression;
import org.ballerinalang.model.expressions.ConnectorInitExpr;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.expressions.InstanceCreationExpr;
import org.ballerinalang.model.expressions.MapInitExpr;
import org.ballerinalang.model.expressions.MapStructInitKeyValueExpr;
import org.ballerinalang.model.expressions.RefTypeInitExpr;
import org.ballerinalang.model.expressions.ReferenceExpr;
import org.ballerinalang.model.expressions.ResourceInvocationExpr;
import org.ballerinalang.model.expressions.StructFieldAccessExpr;
import org.ballerinalang.model.expressions.StructInitExpr;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.expressions.UnaryExpression;
import org.ballerinalang.model.expressions.VariableRefExpr;
import org.ballerinalang.model.nodes.EndNode;
import org.ballerinalang.model.nodes.ExitNode;
import org.ballerinalang.model.nodes.GotoNode;
import org.ballerinalang.model.nodes.IfElseNode;
import org.ballerinalang.model.nodes.fragments.expressions.ActionInvocationExprStartNode;
import org.ballerinalang.model.nodes.fragments.expressions.ArrayInitExprEndNode;
import org.ballerinalang.model.nodes.fragments.expressions.ArrayMapAccessExprEndNode;
import org.ballerinalang.model.nodes.fragments.expressions.BacktickExprEndNode;
import org.ballerinalang.model.nodes.fragments.expressions.BinaryExpressionEndNode;
import org.ballerinalang.model.nodes.fragments.expressions.CallableUnitEndNode;
import org.ballerinalang.model.nodes.fragments.expressions.ConnectorInitExprEndNode;
import org.ballerinalang.model.nodes.fragments.expressions.FunctionInvocationExprStartNode;
import org.ballerinalang.model.nodes.fragments.expressions.InvokeNativeActionNode;
import org.ballerinalang.model.nodes.fragments.expressions.InvokeNativeFunctionNode;
import org.ballerinalang.model.nodes.fragments.expressions.InvokeNativeTypeMapperNode;
import org.ballerinalang.model.nodes.fragments.expressions.MapInitExprEndNode;
import org.ballerinalang.model.nodes.fragments.expressions.RefTypeInitExprEndNode;
import org.ballerinalang.model.nodes.fragments.expressions.StructFieldAccessExprEndNode;
import org.ballerinalang.model.nodes.fragments.expressions.StructInitExprEndNode;
import org.ballerinalang.model.nodes.fragments.expressions.TypeCastExpressionEndNode;
import org.ballerinalang.model.nodes.fragments.expressions.UnaryExpressionEndNode;
import org.ballerinalang.model.nodes.fragments.statements.AssignStmtEndNode;
import org.ballerinalang.model.nodes.fragments.statements.ForkJoinStartNode;
import org.ballerinalang.model.nodes.fragments.statements.ReplyStmtEndNode;
import org.ballerinalang.model.nodes.fragments.statements.ReturnStmtEndNode;
import org.ballerinalang.model.nodes.fragments.statements.ThrowStmtEndNode;
import org.ballerinalang.model.nodes.fragments.statements.TryCatchStmtEndNode;
import org.ballerinalang.model.nodes.fragments.statements.VariableDefStmtEndNode;
import org.ballerinalang.model.statements.ActionInvocationStmt;
import org.ballerinalang.model.statements.AssignStmt;
import org.ballerinalang.model.statements.BlockStmt;
import org.ballerinalang.model.statements.BreakStmt;
import org.ballerinalang.model.statements.CommentStmt;
import org.ballerinalang.model.statements.ForeachStmt;
import org.ballerinalang.model.statements.ForkJoinStmt;
import org.ballerinalang.model.statements.FunctionInvocationStmt;
import org.ballerinalang.model.statements.IfElseStmt;
import org.ballerinalang.model.statements.ReplyStmt;
import org.ballerinalang.model.statements.ReturnStmt;
import org.ballerinalang.model.statements.ThrowStmt;
import org.ballerinalang.model.statements.TryCatchStmt;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.statements.WhileStmt;
import org.ballerinalang.model.statements.WorkerInvocationStmt;
import org.ballerinalang.model.statements.WorkerReplyStmt;
import org.ballerinalang.model.types.BMapType;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.util.BValueUtils;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BException;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueType;
import org.ballerinalang.model.values.BXML;
import org.ballerinalang.natives.connectors.AbstractNativeConnector;
import org.ballerinalang.natives.connectors.BalConnectorCallback;
import org.ballerinalang.runtime.Constants;
import org.ballerinalang.runtime.worker.WorkerCallback;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.FlowBuilderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * {@link BLangAbstractExecutionVisitor} defines execution steps of a Ballerina program in Linked Node based execution.
 *
 * @since 0.8.0
 */
public abstract class BLangAbstractExecutionVisitor extends BLangExecutionVisitor {

    private static final Logger logger = LoggerFactory.getLogger(Constants.BAL_LINKED_INTERPRETER_LOGGER);
    protected Stack<Integer> branchIDStack;
    private RuntimeEnvironment runtimeEnv;
    private Context bContext;
    private ControlStack controlStack;
    private Stack<TryCatchStackRef> tryCatchStackRefs;
    protected LinkedNode next;
    private ExecutorService executor;
    private ForkJoinInvocationStatus forkJoinInvocationStatus;
    private boolean completed;

    public BLangAbstractExecutionVisitor(RuntimeEnvironment runtimeEnv, Context bContext) {
        this.runtimeEnv = runtimeEnv;
        this.bContext = bContext;
        this.controlStack = bContext.getControlStack();
        this.branchIDStack = new Stack<>();
        this.tryCatchStackRefs = new Stack<>();
    }

    /* Statement nodes. */

    @Override
    public void visit(ActionInvocationStmt actionIStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ActionInvocationStmt {}-{}", getNodeLocation(actionIStmt.getNodeLocation()),
                    actionIStmt.getActionInvocationExpr().getCallableUnit().getName());
        }
        next = actionIStmt.next;
    }


    @Override
    public void visit(AssignStmt assignStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing AssignStmt {}", getNodeLocation(assignStmt.getNodeLocation()));
        }
        next = assignStmt.next;
    }

    @Override
    public void visit(BlockStmt blockStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing BlockStmt {}-MultiParent={}", getNodeLocation(blockStmt.getNodeLocation()),
                    blockStmt.getGotoNode() != null);
        }
        next = blockStmt.next;
    }

    @Override
    public void visit(BreakStmt breakStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing BreakStmt {}", getNodeLocation(breakStmt.getNodeLocation()));
        }
        next = breakStmt.next;
    }

    @Override
    public void visit(CommentStmt commentStmt) {
        next = commentStmt.next;
    }

    @Override
    public void visit(ForeachStmt foreachStmt) {
        next = foreachStmt.next;
    }

    @Override
    public void visit(ForkJoinStmt forkJoinStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing forkJoinStmt {}", getNodeLocation(forkJoinStmt.getNodeLocation()));
        }
        next = forkJoinStmt.next;
    }

    @Override
    public void visit(FunctionInvocationStmt funcIStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing FunctionInvocationStmt {}-{}", getNodeLocation(funcIStmt.getNodeLocation()),
                    funcIStmt.getFunctionInvocationExpr().getCallableUnit().getName());
        }
        next = funcIStmt.next;
    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing IfElseStmt {}", getNodeLocation(ifElseStmt.getNodeLocation()));
        }
        next = ifElseStmt.next;
    }

    @Override
    public void visit(ReplyStmt replyStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ReplyStmt {}", getNodeLocation(replyStmt.getNodeLocation()));
        }
        next = replyStmt.next;
    }

    @Override
    public void visit(ReturnStmt returnStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ReturnStmt {}", getNodeLocation(returnStmt.getNodeLocation()));
        }
        next = returnStmt.next;
    }

    @Override
    public void visit(ThrowStmt throwStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ThrowStmt {}", getNodeLocation(throwStmt.getNodeLocation()));
        }
        next = throwStmt.next;
    }

    @Override
    public void visit(TryCatchStmt tryCatchStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing TryCatchStmt {}", getNodeLocation(tryCatchStmt.getNodeLocation()));
        }
        this.tryCatchStackRefs.push(new TryCatchStackRef(tryCatchStmt.getCatchBlock(),
                bContext.getControlStack().getCurrentFrame()));
        next = tryCatchStmt.next;
    }

    @Override
    public void visit(VariableDefStmt variableDefStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing VariableDefStmt {}", getNodeLocation(variableDefStmt.getNodeLocation()));
        }
        next = variableDefStmt.next;
    }

    @Override
    public void visit(WorkerInvocationStmt workerInvocationStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing WorkerInvocationStmt {}", getNodeLocation(workerInvocationStmt.getNodeLocation()));
        }
        next = workerInvocationStmt.next;
        // Create the Stack frame
        Worker worker = workerInvocationStmt.getCallableUnit();

        int sizeOfValueArray = worker.getStackFrameSize();
        BValue[] localVals = new BValue[sizeOfValueArray];

        // Evaluate the argument expression
        BValue argValue = getTempValue(workerInvocationStmt.getInMsg());

        if (argValue instanceof BMessage) {
            argValue = ((BMessage) argValue).clone();
        }

        // Setting argument value in the stack frame
        localVals[0] = argValue;

        // Get values for all the worker arguments
        int valueCounter = 1;

        for (ParameterDef returnParam : worker.getReturnParameters()) {
            // Check whether these are unnamed set of return types.
            // If so break the loop. You can't have a mix of unnamed and named returns parameters.
            if (returnParam.getName() == null) {
                break;
            }

            localVals[valueCounter] = returnParam.getType().getDefaultValue();
            valueCounter++;
        }


        // Create an arrays in the stack frame to hold return values;
        BValue[] returnVals = new BValue[1];

        // Create a new stack frame with memory locations to hold parameters, local values, temp expression value,
        // return values and worker invocation location;
        CallableUnitInfo functionInfo = new CallableUnitInfo(worker.getName(), worker.getPackagePath(),
                workerInvocationStmt.getNodeLocation());

        StackFrame stackFrame = new StackFrame(localVals, returnVals, functionInfo);
        Context workerContext = new Context();
        workerContext.getControlStack().pushFrame(stackFrame);
        WorkerCallback workerCallback = new WorkerCallback(workerContext);
        workerContext.setBalCallback(workerCallback);
        BLangExecutor workerExecutor = new BLangExecutor(runtimeEnv, workerContext);

        executor = Executors.newSingleThreadExecutor();
        WorkerRunner workerRunner = new WorkerRunner(workerExecutor, workerContext, worker);
        Future<BMessage> future = executor.submit(workerRunner);
        worker.setResultFuture(future);
    }

    @Override
    public void visit(WorkerReplyStmt workerReplyStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing WorkerReplyStmt {}", getNodeLocation(workerReplyStmt.getNodeLocation()));
        }
        next = workerReplyStmt.next;
        Worker worker = workerReplyStmt.getWorker();
        Future<BMessage> future = worker.getResultFuture();
        try {
            BMessage result = future.get(60, TimeUnit.SECONDS);
            VariableRefExpr variableRefExpr = workerReplyStmt.getReceiveExpr();
            assignValueToVarRefExpr(result, variableRefExpr);
            executor.shutdown();
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (Exception e) {
            // If there is an exception in the worker, set an empty value to the return variable
            BMessage result = BTypes.typeMessage.getDefaultValue();
            VariableRefExpr variableRefExpr = workerReplyStmt.getReceiveExpr();
            assignValueToVarRefExpr(result, variableRefExpr);
        } finally {
            // Finally, try again to shutdown if not done already
            executor.shutdownNow();
        }
    }

    @Override
    public void visit(WhileStmt whileStmt) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing WhileStmt {}", getNodeLocation(whileStmt.getNodeLocation()));
        }
        next = whileStmt.next;
    }

    /* Expression nodes */

    @Override
    public void visit(ActionInvocationExpr actoinIExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ActionInvocationExpr {}-{}", getNodeLocation(actoinIExpr.getNodeLocation()),
                    actoinIExpr.getCallableUnit().getName());
        }
        next = actoinIExpr.next;
    }

    @Override
    public void visit(ArrayInitExpr arrayInitExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ArrayInitExpr {}", getNodeLocation(arrayInitExpr.getNodeLocation()));
        }
        next = arrayInitExpr.next;
    }

    @Override
    public void visit(ArrayMapAccessExpr arrayMapAccessExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ArrayMapAccessExpr {}", getNodeLocation(arrayMapAccessExpr.getNodeLocation()));
        }
        next = arrayMapAccessExpr.next;
    }

    @Override
    public void visit(BacktickExpr backtickExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing BacktickExpr {}", getNodeLocation(backtickExpr.getNodeLocation()));
        }
        next = backtickExpr.next;
    }

    @Override
    public void visit(BasicLiteral basicLiteral) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing BasicLiteral {}-\"{}\"", basicLiteral.getTypeName(),
                    basicLiteral.getBValue().stringValue());
        }
        setTempValue(basicLiteral.getTempOffset(), basicLiteral.getBValue());
        next = basicLiteral.next;
    }

    @Override
    public void visit(BinaryExpression expression) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing BinaryExpression {}", getNodeLocation(expression.getNodeLocation()));
        }
        next = expression.next;
    }

    @Override
    public void visit(ConnectorInitExpr connectorInitExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ConnectorInitExpr {}", getNodeLocation(connectorInitExpr.getNodeLocation()));
        }
        next = connectorInitExpr.next;
    }

    @Override
    public void visit(FunctionInvocationExpr functionIExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing FunctionInvocationExpr {}-{}", getNodeLocation(functionIExpr.getNodeLocation()),
                    functionIExpr.getCallableUnit().getName());
        }
        next = functionIExpr.next;
    }

    @Override
    public void visit(InstanceCreationExpr instanceCreationExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing InstanceCreationExpr {}", getNodeLocation(instanceCreationExpr.getNodeLocation()));
        }
        setTempValue(instanceCreationExpr.getTempOffset(), instanceCreationExpr.getType().getDefaultValue());
        next = instanceCreationExpr.next;
    }

    @Override
    public void visit(MapInitExpr mapInitExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing MapInitExpr {}", getNodeLocation(mapInitExpr.getNodeLocation()));
        }
        next = mapInitExpr.next;
    }

    @Override
    public void visit(MapStructInitKeyValueExpr expr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing MapStructInitKeyValueExpr {}", getNodeLocation(expr.getNodeLocation()));
        }
        next = expr.next;
    }

    @Override
    public void visit(RefTypeInitExpr refTypeInitExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing RefTypeInitExpr {}", getNodeLocation(refTypeInitExpr.getNodeLocation()));
        }
        next = refTypeInitExpr.next;
    }

    @Override
    public void visit(ResourceInvocationExpr resourceIExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ResourceInvocationExpr {}", getNodeLocation(resourceIExpr.getNodeLocation()));
        }

        Resource resource = resourceIExpr.getResource();
        BValue[] valueParams = new BValue[resource.getStackFrameSize()];
        BMessage messageValue = new BMessage(bContext.getCarbonMessage());

        valueParams[0] = messageValue;
        int i = 0;
        for (Expression arg : resourceIExpr.getArgExprs()) {
            // Evaluate the argument expression
            VariableRefExpr variableRefExpr = (VariableRefExpr) arg;
            MemoryLocation memoryLocation = variableRefExpr.getVariableDef().getMemoryLocation();
            BValue argValue = memoryLocation.access(this);
            BType argType = arg.getType();
            if (BTypes.isValueType(argType)) {
                argValue = BValueUtils.clone(argType, argValue);
            }
            // Setting argument value in the stack frame
            valueParams[i] = argValue;

            i++;
        }
        BValue[] ret = new BValue[1];
        CallableUnitInfo resourceInfo = new CallableUnitInfo(resource.getName(), resource.getPackagePath(),
                resource.getNodeLocation());

        BValue[] cacheValues = new BValue[resource.getTempStackFrameSize() + 1];
        StackFrame stackFrame = new StackFrame(valueParams, ret, cacheValues, resourceInfo);
        controlStack.pushFrame(stackFrame);
        next = resourceIExpr.getResource().getResourceBody();
    }

    @Override
    public void visit(StructFieldAccessExpr accessExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing StructFieldAccessExpr {}", getNodeLocation(accessExpr.getNodeLocation()));
        }
        next = accessExpr.next;
    }

    @Override
    public void visit(StructInitExpr structInitExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing StructInitExpr {}", getNodeLocation(structInitExpr.getNodeLocation()));
        }
        next = structInitExpr.next;
    }

    @Override
    public void visit(TypeCastExpression typeCastExpression) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing typeCast {}->{}", typeCastExpression.getType(), typeCastExpression.getTargetType());
        }
        next = typeCastExpression.next;
    }

    @Override
    public void visit(UnaryExpression unaryExpression) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing UnaryExpression {}", getNodeLocation(unaryExpression.getNodeLocation()));
        }
        next = unaryExpression.next;
    }

    @Override
    public void visit(VariableRefExpr variableRefExpr) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing VariableRefExpr - {}, loc-{}", variableRefExpr.getSymbolName().getName(),
                    variableRefExpr.getMemoryLocation().getClass().getSimpleName());
        }
        next = variableRefExpr.next;
    }

    /* Memory Location */

    @Override
    public BValue access(StackVarLocation stackVarLocation) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing StackVarLocation");
        }
        int offset = stackVarLocation.getStackFrameOffset();
        return controlStack.getValue(offset);
    }

    @Override
    public BValue access(ConstantLocation constantLocation) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ConstantLocation");
        }
        int offset = constantLocation.getStaticMemAddrOffset();
        RuntimeEnvironment.StaticMemory staticMemory = runtimeEnv.getStaticMemory();
        return staticMemory.getValue(offset);
    }

    @Override
    public BValue access(ServiceVarLocation serviceVarLocation) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ServiceVarLocation");
        }
        int offset = serviceVarLocation.getStaticMemAddrOffset();
        RuntimeEnvironment.StaticMemory staticMemory = runtimeEnv.getStaticMemory();
        return staticMemory.getValue(offset);
    }

    @Override
    public BValue access(StructVarLocation structLocation) {
        throw new IllegalArgumentException("struct value is required to get the value of a field");
    }

    @Override
    public BValue access(ConnectorVarLocation connectorVarLocation) {
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

    @Override
    public BValue access(WorkerVarLocation workerVarLocation) {
        int offset = workerVarLocation.getworkerMemAddrOffset();
        return controlStack.getValue(offset);
    }

    /* Helper Nodes. */

    @Override
    public void visit(EndNode endNode) {
        // Done.
        if (logger.isDebugEnabled()) {
            logger.debug("Executing EndNode");
        }
        completed = true;
        next = null;
    }

    @Override
    public void visit(ExitNode exitNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ExitNode");
        }
        completed = true;
        next = null;
        Runtime.getRuntime().exit(0);
    }

    @Override
    public void visit(GotoNode gotoNode) {
        Integer pop = branchIDStack.pop();
        if (logger.isDebugEnabled()) {
            logger.debug("Executing GotoNode branch:{}", pop);
        }
        next = gotoNode.next(pop);
    }

    @Override
    public void visit(IfElseNode ifElseNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing IfElseNode");
        }
        Expression expr = ifElseNode.getCondition();
        BBoolean condition = (BBoolean) getTempValue(expr);

        if (condition.booleanValue()) {
            next = ifElseNode.next;
        } else {
            next = ifElseNode.nextAfterBreak();
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
        next = assignStmtEndNode.next;
        Expression rExpr = assignStmt.getRExpr();
        Expression[] lExprs = assignStmt.getLExprs();
        if (rExpr instanceof FunctionInvocationExpr || rExpr instanceof ActionInvocationExpr) {
            for (int i = 0; i < lExprs.length; i++) {
                Expression lExpr = lExprs[i];
                BValue rValue = getTempValue(rExpr.getTempOffset() + i);
                assignValue(rValue, lExpr);
            }
        } else {
            Expression lExpr = lExprs[0];
            BValue rValue = getTempValue(rExpr);
            assignValue(rValue, lExpr);
        }
    }

    @Override
    public void visit(ForkJoinStartNode forkJoinStartNode) {
        forkJoinInvocationStatus = new ForkJoinInvocationStatus();
        ForkJoinStmt forkJoinStmt = forkJoinStartNode.getStatement();
        VariableRefExpr expr = forkJoinStmt.getMessageReference();
        BMessage inMsg = (BMessage) getTempValue(expr);
        List<WorkerRunner> workerRunnerList = new ArrayList<>();

        int timeout = ((BInteger) getTempValue(forkJoinStmt.getTimeout().getTimeoutExpression())).intValue();

        Worker[] workers = forkJoinStmt.getWorkers();
        Map<String, WorkerRunner> triggeredWorkers = new HashMap<>();
        for (Worker worker : workers) {
            int sizeOfValueArray = worker.getStackFrameSize();
            BValue[] localVals = new BValue[sizeOfValueArray];

            BValue argValue = inMsg.clone();
            // Setting argument value in the stack frame
            localVals[0] = argValue;

            // Get values for all the worker arguments
            int valueCounter = 1;

            // Create default values for all declared local variables
            for (ParameterDef variableDcl : worker.getParameterDefs()) {
                localVals[valueCounter] = variableDcl.getType().getDefaultValue();
                valueCounter++;
            }

            // Create an arrays in the stack frame to hold return values;
            BValue[] returnVals = new BValue[1];

            // Create a new stack frame with memory locations to hold parameters, local values, temp expression value,
            // return values and worker invocation location;
            SymbolName functionSymbolName = worker.getSymbolName();
            CallableUnitInfo functionInfo = new CallableUnitInfo(functionSymbolName.getName(),
                    functionSymbolName.getPkgPath(), worker.getNodeLocation());

            StackFrame stackFrame = new StackFrame(localVals, returnVals, functionInfo);
            Context workerContext = new Context();
            workerContext.getControlStack().pushFrame(stackFrame);
            WorkerCallback workerCallback = new WorkerCallback(workerContext);
            workerContext.setBalCallback(workerCallback);
            BLangExecutor workerExecutor = new BLangExecutor(runtimeEnv, workerContext);
            WorkerRunner workerRunner = new WorkerRunner(workerExecutor, workerContext, worker);
            workerRunnerList.add(workerRunner);
            triggeredWorkers.put(worker.getName(), workerRunner);
        }

        if (forkJoinStmt.getJoin().getJoinType().equalsIgnoreCase("any")) {
            String[] joinWorkerNames = forkJoinStmt.getJoin().getJoinWorkers();
            if (joinWorkerNames.length == 0) {
                // If there are no workers specified, wait for any of all the workers
                BMessage res = invokeAnyWorker(workerRunnerList, timeout);
                if (res != null) {
                    forkJoinInvocationStatus.resultMsgs.add(res);
                }
            } else {
                List<WorkerRunner> workerRunnersSpecified = new ArrayList<>();
                for (String workerName : joinWorkerNames) {
                    workerRunnersSpecified.add(triggeredWorkers.get(workerName));
                }
                BMessage res = invokeAnyWorker(workerRunnersSpecified, timeout);
                if (res != null) {
                    forkJoinInvocationStatus.resultMsgs.add(res);
                }
            }
        } else {
            String[] joinWorkerNames = forkJoinStmt.getJoin().getJoinWorkers();
            if (joinWorkerNames.length == 0) {
                // If there are no workers specified, wait for all of all the workers
                forkJoinInvocationStatus.resultMsgs.addAll(invokeAllWorkers(workerRunnerList, timeout));
            } else {
                List<WorkerRunner> workerRunnersSpecified = new ArrayList<>();
                for (String workerName : joinWorkerNames) {
                    workerRunnersSpecified.add(triggeredWorkers.get(workerName));
                }
                forkJoinInvocationStatus.resultMsgs.addAll(
                        invokeAllWorkers(workerRunnersSpecified, timeout));
            }
        }

        if (forkJoinInvocationStatus.timedOut) {
            // Execute the timeout block

            // Creating a new arrays
            BArray bArray = forkJoinStmt.getJoin().getJoinResult().getType().getDefaultValue();

            for (int i = 0; i < forkJoinInvocationStatus.resultMsgs.size(); i++) {
                BValue value = forkJoinInvocationStatus.resultMsgs.get(i);
                bArray.add(i, value);
            }

            int offsetJoin = ((StackVarLocation) forkJoinStmt.getTimeout().getTimeoutResult().getMemoryLocation()).
                    getStackFrameOffset();

            controlStack.setValue(offsetJoin, bArray);
            next = forkJoinStmt.getTimeout().getTimeoutBlock();
        } else {
            // Assign values to join block message arrays

            // Creating a new arrays
            BArray bArray = forkJoinStmt.getJoin().getJoinResult().getType().getDefaultValue();
            for (int i = 0; i < forkJoinInvocationStatus.resultMsgs.size(); i++) {
                BValue value = forkJoinInvocationStatus.resultMsgs.get(i);
                bArray.add(i, value);
            }

            int offsetJoin = ((StackVarLocation) forkJoinStmt.getJoin().getJoinResult().getMemoryLocation()).
                    getStackFrameOffset();
            controlStack.setValue(offsetJoin, bArray);
            next = forkJoinStmt.getJoin().getJoinBlock();
        }
        forkJoinInvocationStatus = null;
    }

    private BMessage invokeAnyWorker(List<WorkerRunner> workerRunnerList, int timeout) {
        ExecutorService anyExecutor = Executors.newWorkStealingPool();
        BMessage result;
        try {
            result = anyExecutor.invokeAny(workerRunnerList, timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            return null;
        } catch (TimeoutException e) {
            forkJoinInvocationStatus.timedOut = true;
            return null;
            // throw new BallerinaException("Fork-Join statement at " + position + " timed out", e);
        }
        return result;
    }

    private List<BMessage> invokeAllWorkers(List<WorkerRunner> workerRunnerList, int timeout) {
        ExecutorService allExecutor = Executors.newWorkStealingPool();
        List<BMessage> result = new ArrayList<>();
        try {
            allExecutor.invokeAll(workerRunnerList, timeout, TimeUnit.SECONDS).stream().map(bMessageFuture -> {
                try {
                    return bMessageFuture.get();
                } catch (CancellationException e) {
                    // This means task has been timedout and cancelled by system.
                    forkJoinInvocationStatus.timedOut = true;
                    return null;
                } catch (Exception e) {
                    return null;
                }

            }).forEach((BMessage b) -> {
                if (b != null) {
                    result.add(b);
                }
            });
        } catch (InterruptedException e) {
            return result;
        }
        return result;
    }

    @Override
    public void visit(ThrowStmtEndNode throwStmtEndNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ThrowStmt - EndNode");
        }
        BException exception = (BException) getTempValue(throwStmtEndNode.getStatement().getExpr());
        exception.value().setStackTrace(ErrorHandlerUtils.getMainFuncStackTrace(bContext, null));
        this.handleBException(exception);
    }

    @Override
    public void visit(TryCatchStmtEndNode tryCatchStmtEndNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing TryCatchStmt - EndNode");
        }
        next = tryCatchStmtEndNode.next;
        tryCatchStackRefs.pop();
    }

    @Override
    public void visit(ReplyStmtEndNode replyStmtEndNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ReplyStmt - EndNode");
        }
        next = replyStmtEndNode.next;
        Expression expr = replyStmtEndNode.getStatement().getReplyExpr();
        BMessage bMessage = (BMessage) getTempValue(expr);
        bContext.getBalCallback().done(bMessage.value());
    }

    @Override
    public void visit(ReturnStmtEndNode returnStmtEndNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ReturnStmt - EndNode");
        }
        next = returnStmtEndNode.next;
        ReturnStmt returnStmt = returnStmtEndNode.getStatement();
        Expression[] exprs = returnStmt.getExprs();

        // Check whether the first argument is a multi-return function
        if (exprs.length == 1 && exprs[0] instanceof FunctionInvocationExpr) {
            FunctionInvocationExpr funcIExpr = (FunctionInvocationExpr) exprs[0];
            if (funcIExpr.getTypes().length > 1) {
                for (int i = 0; i < funcIExpr.getTypes().length; i++) {
                    controlStack.setReturnValue(i, getTempValue(funcIExpr.getTempOffset() + i));
                }
                return;
            }
        }
        for (int i = 0; i < exprs.length; i++) {
            Expression expr = exprs[i];
            BValue returnVal = getTempValue(expr);
            controlStack.setReturnValue(i, returnVal);
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
        next = variableDefStmtEndNode.next;
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
            rValue = getTempValue(rExpr);
        }
        assignValue(rValue, lExpr);
    }

    @Override
    public void visit(ActionInvocationExprStartNode actionInvocationExprStartNode) {
        ActionInvocationExpr actionIExpr = actionInvocationExprStartNode.getExpression();
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ActionInvocationExpr StartNode " + actionIExpr.getCallableUnit().getName());
        }
        next = actionInvocationExprStartNode.next;
        // Create the Stack frame
        Action action = actionIExpr.getCallableUnit();
        BValue[] localVals = new BValue[action.getStackFrameSize()];

        // Create default values for all declared local variables
        int valueCounter = populateArgumentValues(actionIExpr.getArgExprs(), localVals);

        for (ParameterDef returnParam : action.getReturnParameters()) {
            // Check whether these are unnamed set of return types.
            // If so break the loop. You can't have a mix of unnamed and named returns parameters.
            if (returnParam.getName() == null) {
                break;
            }

            localVals[valueCounter] = returnParam.getType().getDefaultValue();
            valueCounter++;
        }

        // Create an arrays in the stack frame to hold return values;
        BValue[] returnVals = new BValue[action.getReturnParamTypes().length];

        // Create a new stack frame with memory locations to hold parameters, local values, temp expression values and
        // return values;
        CallableUnitInfo actionInfo = new CallableUnitInfo(action.getName(), action.getPackagePath(),
                actionIExpr.getNodeLocation());

        BValue[] cacheValues = new BValue[actionIExpr.getCallableUnit().getTempStackFrameSize() + 1];
        StackFrame stackFrame = new StackFrame(localVals, returnVals, cacheValues, actionInfo);
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
        next = arrayInitExprEndNode.next;
        Expression[] argExprs = arrayInitExprEndNode.getExpression().getArgExprs();

        // Creating a new arrays
        BArray bArray = arrayInitExprEndNode.getExpression().getType().getDefaultValue();

        for (int i = 0; i < argExprs.length; i++) {
            Expression expr = argExprs[i];
            BValue value = getTempValue(expr);
            bArray.add(i, value);
        }
        setTempValue(arrayInitExprEndNode.getExpression().getTempOffset(), bArray);
    }

    @Override
    public void visit(ArrayMapAccessExprEndNode arrayMapAccessExprEndNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ArrayMapAccessExpr - EndNode");
        }
        next = arrayMapAccessExprEndNode.next;
        ArrayMapAccessExpr arrayMapAccessExpr = arrayMapAccessExprEndNode.getExpression();
        if (!arrayMapAccessExpr.isLHSExpr()) {
            VariableRefExpr arrayVarRefExpr = (VariableRefExpr) arrayMapAccessExpr.getRExpr();
            BValue collectionValue = getTempValue(arrayVarRefExpr);

            if (collectionValue == null) {
                throw new BallerinaException("variable '" + arrayVarRefExpr.getVarName() + "' is null");
            }

            Expression indexExpr = arrayMapAccessExpr.getIndexExpr();
            BValue indexValue = getTempValue(indexExpr);

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
            setTempValue(arrayMapAccessExpr.getTempOffset(), result);
        }
        // Else Nothing to do. (Assignment Statement will handle rest.
    }

    @Override
    public void visit(BacktickExprEndNode backtickExprEndNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing BacktickExpr - EndNode");
        }
        next = backtickExprEndNode.next;
        String evaluatedString = evaluteBacktickString(backtickExprEndNode.getExpression());
        if (backtickExprEndNode.getExpression().getType() == BTypes.typeJSON) {
            setTempValue(backtickExprEndNode.getExpression().getTempOffset(), new BJSON(evaluatedString));
        } else {
            setTempValue(backtickExprEndNode.getExpression().getTempOffset(), new BXML(evaluatedString));
        }
    }

    @Override
    public void visit(BinaryExpressionEndNode binaryExpressionEndNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing BinaryExpression - EndNode");
        }
        BinaryExpression binaryExpr = binaryExpressionEndNode.getExpression();
        Expression rExpr = binaryExpr.getRExpr();
        BValueType rValue = (BValueType) getTempValue(rExpr);

        Expression lExpr = binaryExpr.getLExpr();
        BValueType lValue = (BValueType) getTempValue(lExpr);
        try {
            setTempValue(binaryExpr.getTempOffset(), binaryExpr.getEvalFunc().apply(lValue, rValue));
            next = binaryExpressionEndNode.next;
        } catch (RuntimeException e) {
            handleBException(new BException(e.getMessage()));
        }
    }

    @Override
    public void visit(FunctionInvocationExprStartNode functionInvocationExprStartNode) {
        FunctionInvocationExpr funcIExpr = functionInvocationExprStartNode.getExpression();
        if (logger.isDebugEnabled()) {
            logger.debug("Executing FunctionInvocationExpr StartNode - {}", funcIExpr.getCallableUnit().getName());
        }
        next = functionInvocationExprStartNode.next;
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

        // Create an arrays in the stack frame to hold return values;
        BValue[] returnVals = new BValue[function.getReturnParamTypes().length];

        // Create a new stack frame with memory locations to hold parameters, local values, temp expression value,
        // return values and function invocation location;
        CallableUnitInfo functionInfo = new CallableUnitInfo(function.getName(), function.getPackagePath(),
                funcIExpr.getNodeLocation());

        BValue[] cacheValue = new BValue[funcIExpr.getCallableUnit().getTempStackFrameSize() + 1];
        StackFrame stackFrame = new StackFrame(localVals, returnVals, cacheValue, functionInfo);
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
        next = structFieldAccessExprEndNode.next;
        StructFieldAccessExpr structFieldAccessExpr = structFieldAccessExprEndNode.getExpression();
        Expression varRef = structFieldAccessExpr.getVarRef();
        BValue value = getTempValue(varRef);
        setTempValue(structFieldAccessExpr.getTempOffset(), getFieldExprValue(structFieldAccessExpr, value));
    }

    @Override
    public void visit(StructInitExprEndNode structInitExprEndNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing StructInitExpr - EndNode");
        }
        next = structInitExprEndNode.next;
        StructInitExpr structInitExpr = structInitExprEndNode.getExpression();
        StructDef structDef = (StructDef) structInitExpr.getType();
        BValue[] structMemBlock;
        int offset = 0;
        structMemBlock = new BValue[structDef.getStructMemorySize()];

        Expression[] argExprs = structInitExpr.getArgExprs();

        // create a memory block to hold field of the struct, and populate it with default values
        VariableDef[] fields = structDef.getFields();
        for (VariableDef field : fields) {
            structMemBlock[offset] = field.getType().getDefaultValue();
            offset++;
        }

        // iterate through initialized values and re-populate the memory block
        for (int i = 0; i < argExprs.length; i++) {
            MapStructInitKeyValueExpr expr = (MapStructInitKeyValueExpr) argExprs[i];
            VariableRefExpr varRefExpr = (VariableRefExpr) expr.getKeyExpr();
            StructVarLocation structVarLoc = (StructVarLocation) (varRefExpr).getVariableDef().getMemoryLocation();
            structMemBlock[structVarLoc.getStructMemAddrOffset()] = getTempValue(expr.getValueExpr());
        }
        setTempValue(structInitExpr.getTempOffset(), new BStruct(structDef, structMemBlock));
    }

    @Override
    public void visit(TypeCastExpressionEndNode typeCastExpressionEndNode) {
        TypeCastExpression typeCastExpression = typeCastExpressionEndNode.getExpression();
        if (logger.isDebugEnabled()) {
            logger.debug("Executing TypeCastExpression - EndNode {}->{}, source-{}", typeCastExpression.getType(),
                    typeCastExpression.getTargetType(), typeCastExpression.getRExpr() != null);
        }
        next = typeCastExpressionEndNode.next;
        // Check for native type casting
        if (typeCastExpression.getEvalFunc() != null) {
            BValueType result = (BValueType) getTempValue(typeCastExpression.getRExpr());
            setTempValue(typeCastExpression.getTempOffset(), typeCastExpression.getEvalFunc().apply(result));
        } else {
            TypeMapper typeMapper = typeCastExpression.getCallableUnit();

            int sizeOfValueArray = typeMapper.getStackFrameSize();
            BValue[] localVals = new BValue[sizeOfValueArray];

            // Get values for all the function arguments
            int valueCounter = populateArgumentValues(typeCastExpression.getArgExprs(), localVals);

            for (ParameterDef returnParam : typeMapper.getReturnParameters()) {
                // Check whether these are unnamed set of return types.
                // If so break the loop. You can't have a mix of unnamed and named returns parameters.
                if (returnParam.getName() == null) {
                    break;
                }

                localVals[valueCounter] = returnParam.getType().getDefaultValue();
                valueCounter++;
            }

            // Create an arrays in the stack frame to hold return values;
            BValue[] returnVals = new BValue[1];

            // Create a new stack frame with memory locations to hold parameters, local values, temp expression value,
            // return values and function invocation location;
            CallableUnitInfo functionInfo = new CallableUnitInfo(typeMapper.getTypeMapperName(),
                    typeMapper.getPackagePath(), typeCastExpression.getNodeLocation());

            BValue[] cacheValue = new BValue[typeCastExpression.getCallableUnit().getTempStackFrameSize() + 1];
            StackFrame stackFrame = new StackFrame(localVals, returnVals, cacheValue, functionInfo);
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
        next = unaryExpressionEndNode.next;
        UnaryExpression unaryExpr = unaryExpressionEndNode.getExpression();
        BValueType rValue = (BValueType) getTempValue(unaryExpr.getRExpr());
        BValue result = unaryExpr.getEvalFunc().apply(null, rValue);
        setTempValue(unaryExpr.getTempOffset(), result);
    }

    @Override
    public void visit(RefTypeInitExprEndNode refTypeInitExprEndNode) {
        RefTypeInitExpr refTypeInitExpr = refTypeInitExprEndNode.getExpression();
        if (logger.isDebugEnabled()) {
            logger.debug("Executing RefTypeInitExpr - EndNode");
        }
        next = refTypeInitExprEndNode.next;
        BType bType = refTypeInitExpr.getType();
        setTempValue(refTypeInitExpr.getTempOffset(), bType.getDefaultValue());
    }

    @Override
    public void visit(CallableUnitEndNode callableUnitEndNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing CallableUnit - EndNode native:{} ,type:{} ",
                    callableUnitEndNode.isNativeInvocation(), callableUnitEndNode.getExpression().getClass());
        }
        StackFrame stackFrame = controlStack.popFrame();
        if (stackFrame.returnValues.length > 0) {
            for (int i = 0; i < stackFrame.returnValues.length; i++) {
                setTempValue(callableUnitEndNode.getExpression().getTempOffset() + i, stackFrame.returnValues[i]);
            }
        }
        next = callableUnitEndNode.next;
    }

    @Override
    public void visit(ConnectorInitExprEndNode connectorInitExprEndNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing ConnectorInitExpr - EndNode");
        }
        next = connectorInitExprEndNode.next;
        ConnectorInitExpr connectorInitExpr = connectorInitExprEndNode.getExpression();
        BConnector bConnector;
        BValue[] connectorMemBlock;
        Connector connector = (Connector) connectorInitExpr.getType();

        if (connector instanceof AbstractNativeConnector) {

            AbstractNativeConnector nativeConnector = ((AbstractNativeConnector) connector).getInstance();
            Expression[] argExpressions = connectorInitExpr.getArgExprs();
            connectorMemBlock = new BValue[argExpressions.length];
            for (int j = 0; j < argExpressions.length; j++) {
                connectorMemBlock[j] = getTempValue(argExpressions[j]);
            }

            nativeConnector.init(connectorMemBlock);
            bConnector = new BConnector(nativeConnector, connectorMemBlock);

//            //TODO Fix Issue#320
//            NativeUnit nativeUnit = ((NativeUnitProxy) connector).load();
//            AbstractNativeConnector nativeConnector = (AbstractNativeConnector) ((NativeUnitProxy) connector).load();
//            Expression[] argExpressions = connectorDcl.getArgExprs();
//            connectorMemBlock = new BValue[argExpressions.length];
//
//            for (int j = 0; j < argExpressions.length; j++) {
//                connectorMemBlock[j] = argExpressions[j].execute(this);
//            }
//
//            nativeConnector.init(connectorMemBlock);
//            connector = nativeConnector;
            setTempValue(connectorInitExpr.getTempOffset(), bConnector);
        } else {
            BallerinaConnectorDef connectorDef = (BallerinaConnectorDef) connector;

            int offset = 0;
            connectorMemBlock = new BValue[connectorDef.getSizeOfConnectorMem()];
            for (Expression expr : connectorInitExpr.getArgExprs()) {
                connectorMemBlock[offset] = getTempValue(expr);
                offset++;
            }

            bConnector = new BConnector(connector, connectorMemBlock);
            setTempValue(connectorInitExpr.getTempOffset(), bConnector);
            // Create the Stack frame
            Function initFunction = connectorDef.getInitFunction();
            BValue[] localVals = new BValue[1];
            localVals[0] = bConnector;

            // Create an arrays in the stack frame to hold return values;
            BValue[] returnVals = new BValue[0];

            // Create a new stack frame with memory locations to hold parameters, local values, temp expression value,
            // return values and function invocation location;
            CallableUnitInfo functionInfo = new CallableUnitInfo(initFunction.getName(), initFunction.getPackagePath(),
                    initFunction.getNodeLocation());

            BValue[] cacheValue = new BValue[initFunction.getTempStackFrameSize() + 1];
            StackFrame stackFrame = new StackFrame(localVals, returnVals, cacheValue, functionInfo);
            controlStack.pushFrame(stackFrame);
            if (connectorInitExprEndNode.hasGotoBranchID()) {
                branchIDStack.push(connectorInitExprEndNode.getGotoBranchID());
            }
        }

    }

    @Override
    public void visit(InvokeNativeActionNode invokeNativeActionNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing Native Action - " + invokeNativeActionNode.getCallableUnit().getName());
        }
        try {
            if (invokeNativeActionNode.getCallableUnit().isNonBlockingAction()) {
                BalConnectorCallback connectorCallback = new BalConnectorCallback(bContext, invokeNativeActionNode);
                invokeNativeActionNode.getCallableUnit().execute(bContext, connectorCallback);
                // Release current thread.
                next = null;
            } else {
                invokeNativeActionNode.getCallableUnit().execute(bContext);
                next = invokeNativeActionNode.next;
            }
        } catch (RuntimeException e) {
            BException bException = new BException(e.getMessage());
            handleBException(bException);
        }
    }

    @Override
    public void visit(InvokeNativeFunctionNode invokeNativeFunctionNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing Native Function - " + invokeNativeFunctionNode.getCallableUnit().getName());
        }
        next = invokeNativeFunctionNode.next;
        invokeNativeFunctionNode.getCallableUnit().executeNative(bContext);
    }

    @Override
    public void visit(InvokeNativeTypeMapperNode invokeNativeTypeMapperNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing Native TypeMapperNode - " + invokeNativeTypeMapperNode.getCallableUnit()
                    .getName());
        }
        next = invokeNativeTypeMapperNode.next;
        invokeNativeTypeMapperNode.getCallableUnit().convertNative(bContext);
    }

    @Override
    public void visit(MapInitExprEndNode mapInitExprEndNode) {
        if (logger.isDebugEnabled()) {
            logger.debug("Executing MapInitExprEndNode - EndNode");
        }
        next = mapInitExprEndNode.next;
        Expression[] argExprs = mapInitExprEndNode.getExpression().getArgExprs();

        // Creating a new arrays
        BMap bMap = mapInitExprEndNode.getExpression().getType().getDefaultValue();

        for (int i = 0; i < argExprs.length; i++) {
            MapStructInitKeyValueExpr expr = (MapStructInitKeyValueExpr) argExprs[i];
            BValue keyVal = getTempValue(expr.getKeyExpr());
            BValue value = getTempValue(expr.getValueExpr());
            bMap.put(keyVal, value);
        }
        setTempValue(mapInitExprEndNode.getExpression().getTempOffset(), bMap);
    }

    /**
     * Util method handle Ballerina exception. Native implementations <b>Must</b> use method to handle errors.
     *
     * @param bException
     */
    public void handleBException(BException bException) {
        // SaveStack current StackTrace.
        bException.value().setStackTrace(ErrorHandlerUtils.getMainFuncStackTrace(bContext, null));
        if (tryCatchStackRefs.size() == 0) {
            // There is no tryCatch block to handle this exception. Pass this to handle at root.
            throw new BallerinaException(bException.value().getMessage().stringValue());
        }
        TryCatchStackRef ref = tryCatchStackRefs.pop();
        // unwind stack till we found the current frame.
        while (controlStack.getCurrentFrame() != ref.stackFrame) {
            if (controlStack.getStack().size() > 0) {
                controlStack.popFrame();
            } else {
                // Something has gone wrong. No StackFrame to pop ? this shouldn't be executed.
                throw new FlowBuilderException("Not handle catch statement in execution builder phase");
            }
        }
        MemoryLocation memoryLocation = ref.getCatchBlock().getParameterDef().getMemoryLocation();
        if (memoryLocation instanceof StackVarLocation) {
            int stackFrameOffset = ((StackVarLocation) memoryLocation).getStackFrameOffset();
            controlStack.setValue(stackFrameOffset, bException);
        }
        // Execute Catch block.
        next = ref.getCatchBlock().getCatchBlockStmt();
    }

    // Private methods

    private int populateArgumentValues(Expression[] expressions, BValue[] localVals) {
        int i = 0;
        for (Expression arg : expressions) {
            // Evaluate the argument expression
            BValue argValue = getTempValue(arg);
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

    private String evaluteBacktickString(BacktickExpr backtickExpr) {
        StringBuilder builder = new StringBuilder();
        boolean isJson = backtickExpr.getType() == BTypes.typeJSON;
        String strVal;
        BValue bVal;
        for (Expression expression : backtickExpr.getArgExprs()) {
            bVal = getTempValue(expression);
            strVal = bVal.stringValue();
            if (isJson && bVal instanceof BString && expression instanceof ReferenceExpr) {
                builder.append("\"" + strVal + "\"");
            } else {
                builder.append(strVal);
            }
        }
        return builder.toString();
    }

    private void assignValue(BValue rValue, Expression lExpr) {
        if (lExpr instanceof VariableRefExpr) {
            assignValueToVarRefExpr(rValue, (VariableRefExpr) lExpr);
        } else if (lExpr instanceof ArrayMapAccessExpr) {
            assignValueToArrayMapAccessExpr(rValue, (ArrayMapAccessExpr) lExpr);
        } else if (lExpr instanceof StructFieldAccessExpr) {
            assignValueToStructFieldAccessExpr(rValue, (StructFieldAccessExpr) lExpr);
        }
    }

    private void assignValueToArrayMapAccessExpr(BValue rValue, ArrayMapAccessExpr lExpr) {
        ArrayMapAccessExpr accessExpr = lExpr;
        if (!(accessExpr.getType() == BTypes.typeMap)) {
            BArray arrayVal = (BArray) getTempValue(accessExpr.getRExpr());
            BInteger indexVal = (BInteger) getTempValue(accessExpr.getIndexExpr());
            arrayVal.add(indexVal.intValue(), rValue);

        } else {
            BMap<BString, BValue> mapVal = (BMap<BString, BValue>) getTempValue(accessExpr.getRExpr());
            BString indexVal = (BString) getTempValue(accessExpr.getIndexExpr());
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
        } else if (memoryLocation instanceof WorkerVarLocation) {
            int stackFrameOffset = ((WorkerVarLocation) memoryLocation).getworkerMemAddrOffset();
            controlStack.setValue(stackFrameOffset, rValue);
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
        BValue value = getTempValue(lExprVarRef);
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
        // currentVal is a BStruct or arrays/map of BStruct. hence get the element value of it.
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
        // If the expression is an arrays-map expression, then get the location of the variable-reference-expression
        // of the arrays-map-access-expression.
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
     * <li>An element of an arrays/map variable.</li>
     * </ul>
     * But the value get after evaluating the field-access-expression (<b>lExprValue</b>) contains the whole
     * variable. This methods set the unit value (either the complete arrays/map or the referenced element of an
     * arrays/map), using the index expression of the 'fieldExpr'.
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
            // If the lExprValue value is not a struct arrays/map, then set the value to the struct
            lExprValue.setValue(memoryLocation, rValue);
            return;
        }

        // Evaluate the index expression and get the value.
        BValue indexValue = getTempValue(indexExpr);

        // Get the arrays/map value from the mermory location
        BValue arrayMapValue = lExprValue.getValue(memoryLocation);

        // Set the value to arrays/map's index location
        if (fieldExpr.getRefVarType() instanceof BMapType) {
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
        // currentVal is a BStruct or arrays/map of BStruct. hence get the element value of it.
        BStruct currentStructVal = (BStruct) getUnitValue(currentVal, expr);

        StructFieldAccessExpr fieldExpr = expr.getFieldExpr();
        int fieldLocation = ((StructVarLocation) getMemoryLocation(fieldExpr)).getStructMemAddrOffset();

        // If this is the last field, return the value from memory location
        if (fieldExpr.getFieldExpr() == null) {
            if (currentStructVal.value() == null) {
                throw new BallerinaException("cannot access field '" + fieldExpr.getSymbolName().getName() +
                        "' of non-initialized variable '" + fieldExpr.getParent().getSymbolName().getName() + "'.");
            }
            // Value stored in the struct can be also an arrays. Hence if its an arrray access,
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
     * <li>An element of an arrays/map variable.</li>
     * </ul>
     * But the value stored in memory (<b>currentVal</b>) contains the entire variable. This methods
     * retrieves the unit value (either the complete arrays/map or the referenced element of an arrays/map),
     * using the index expression of the 'fieldExpr'.
     *
     * @param currentVal Value of the field expression evaluated so far
     * @param fieldExpr  Field access expression for the current value
     * @return Unit value of the current value
     */
    private BValue getUnitValue(BValue currentVal, StructFieldAccessExpr fieldExpr) {
        ReferenceExpr currentVarRefExpr = fieldExpr.getVarRef();
        if (currentVal == null) {
            throw new BallerinaException("field '" + currentVarRefExpr.getVarName() + "' is null");
        }

        if (!(currentVal instanceof BArray || currentVal instanceof BMap<?, ?>)) {
            return currentVal;
        }

        // If the lExprValue value is not a struct arrays/map, then the unit value is same as the struct
        Expression indexExpr;
        if (currentVarRefExpr instanceof ArrayMapAccessExpr) {
            indexExpr = ((ArrayMapAccessExpr) currentVarRefExpr).getIndexExpr();
        } else {
            return currentVal;
        }

        // Evaluate the index expression and get the value
        BValue indexValue = getTempValue(indexExpr);

        BValue unitVal;
        // Get the value from arrays/map's index location
        if (fieldExpr.getRefVarType() instanceof BMapType) {
            unitVal = ((BMap) currentVal).get(indexValue);
        } else {
            unitVal = ((BArray) currentVal).get(((BInteger) indexValue).intValue());
        }

        if (unitVal == null) {
            throw new BallerinaException("field '" + currentVarRefExpr.getSymbolName().getName() + "[" +
                    indexValue.stringValue() + "]' is null");
        }

        return unitVal;
    }

    /**
     * Get Temporary value from temporary location.
     *
     * @param expression to be evaluated.
     * @return temporary BValue.
     */
    private BValue getTempValue(Expression expression) {
        if (expression.hasTemporaryValues()) {
            return bContext.getControlStack().getCurrentFrame().tempValues[expression.getTempOffset()];
        } else {
            MemoryLocation memoryLocation = ((VariableRefExpr) expression).getVariableDef().getMemoryLocation();
            return memoryLocation.access(this);
        }
    }

    /**
     * Get Temporary value from providing temporary Offset.
     *
     * @param tempOffSet of the value.
     * @return temporary BValue.
     */
    private BValue getTempValue(int tempOffSet) {
        return bContext.getControlStack().getCurrentFrame().tempValues[tempOffSet];
    }

    private void setTempValue(int offset, BValue result) {
        bContext.getControlStack().getCurrentFrame().tempValues[offset] = result;
    }

    private String getNodeLocation(NodeLocation nodeLocation) {
        return nodeLocation != null ? nodeLocation.getFileName() + ":" + nodeLocation.getLineNumber() : "";
    }

    /**
     * Indicate whether execution is completed or not.
     *
     * @return true, if execution is completed.
     */
    public boolean isExecutionCompleted() {
        return completed;
    }
}

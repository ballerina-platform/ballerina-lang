/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bre;

import org.ballerinalang.model.Action;
import org.ballerinalang.model.BTypeMapper;
import org.ballerinalang.model.BallerinaAction;
import org.ballerinalang.model.BallerinaConnectorDef;
import org.ballerinalang.model.BallerinaFunction;
import org.ballerinalang.model.Connector;
import org.ballerinalang.model.Function;
import org.ballerinalang.model.NodeExecutor;
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
import org.ballerinalang.model.expressions.CallableUnitInvocationExpr;
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
import org.ballerinalang.model.statements.ActionInvocationStmt;
import org.ballerinalang.model.statements.AssignStmt;
import org.ballerinalang.model.statements.BlockStmt;
import org.ballerinalang.model.statements.BreakStmt;
import org.ballerinalang.model.statements.ForkJoinStmt;
import org.ballerinalang.model.statements.FunctionInvocationStmt;
import org.ballerinalang.model.statements.IfElseStmt;
import org.ballerinalang.model.statements.ReplyStmt;
import org.ballerinalang.model.statements.ReturnStmt;
import org.ballerinalang.model.statements.Statement;
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
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.AbstractNativeTypeMapper;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.ballerinalang.natives.connectors.AbstractNativeConnector;
import org.ballerinalang.runtime.worker.WorkerCallback;
import org.ballerinalang.services.ErrorHandlerUtils;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * {@code BLangExecutor} executes a Ballerina application.
 *
 * @since 0.8.0
 */
public class BLangExecutor implements NodeExecutor {

    private RuntimeEnvironment runtimeEnv;
    private Context bContext;
    private ControlStack controlStack;
    private boolean returnedOrReplied;
    private boolean isForkJoinTimedOut;
    private boolean isBreakCalled;
    private ExecutorService executor;

    public BLangExecutor(RuntimeEnvironment runtimeEnv, Context bContext) {
        this.runtimeEnv = runtimeEnv;
        this.bContext = bContext;
        this.controlStack = bContext.getControlStack();
    }

    @Override
    public void visit(BlockStmt blockStmt) {
        Statement[] stmts = blockStmt.getStatements();
        for (Statement stmt : stmts) {
            if (returnedOrReplied || isBreakCalled) {
                break;
            }
            stmt.execute(this);
        }
    }

    @Override
    public void visit(VariableDefStmt varDefStmt) {
        // TODO This variable definition statement can be modeled exactly same as the assignment statement.
        // TODO Remove the following duplicate code segments soon.
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
            rValue = rExpr.execute(this);
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
    public void visit(AssignStmt assignStmt) {
        // TODO WARN: Implementation of this method is inefficient
        // TODO We are in the process of refactoring this method, please bear with us.
        BValue[] rValues;
        Expression rExpr = assignStmt.getRExpr();

        Expression[] lExprs = assignStmt.getLExprs();
        if (lExprs.length > 1) {
            // This statement contains multiple assignments
            rValues = ((CallableUnitInvocationExpr) rExpr).executeMultiReturn(this);
        } else {
            rValues = new BValue[]{rExpr.execute(this)};
        }

        for (int i = 0; i < lExprs.length; i++) {
            Expression lExpr = lExprs[i];
            BValue rValue = rValues[i];
            if (lExpr instanceof VariableRefExpr) {
                assignValueToVarRefExpr(rValue, (VariableRefExpr) lExpr);
            } else if (lExpr instanceof ArrayMapAccessExpr) {
                assignValueToArrayMapAccessExpr(rValue, (ArrayMapAccessExpr) lExpr);
            } else if (lExpr instanceof StructFieldAccessExpr) {
                assignValueToStructFieldAccessExpr(rValue, (StructFieldAccessExpr) lExpr);
            }
        }
    }

    @Override
    public void visit(IfElseStmt ifElseStmt) {
        Expression expr = ifElseStmt.getCondition();
        BBoolean condition = (BBoolean) expr.execute(this);

        if (condition.booleanValue()) {
            ifElseStmt.getThenBody().execute(this);
            return;
        }

        for (IfElseStmt.ElseIfBlock elseIfBlock : ifElseStmt.getElseIfBlocks()) {
            Expression elseIfCondition = elseIfBlock.getElseIfCondition();
            condition = (BBoolean) elseIfCondition.execute(this);

            if (condition.booleanValue()) {
                elseIfBlock.getElseIfBody().execute(this);
                return;
            }
        }

        Statement elseBody = ifElseStmt.getElseBody();
        if (elseBody != null) {
            elseBody.execute(this);
        }
    }

    @Override
    public void visit(WhileStmt whileStmt) {
        Expression expr = whileStmt.getCondition();
        BBoolean condition = (BBoolean) expr.execute(this);

        while (condition.booleanValue()) {
            // Interpret the statements in the while body.
            whileStmt.getBody().execute(this);
            if (returnedOrReplied || isBreakCalled) {
                break;
            }
            // Now evaluate the condition again to decide whether to continue the loop or not.
            condition = (BBoolean) expr.execute(this);
        }
        isBreakCalled = false;
    }

    @Override
    public void visit(BreakStmt breakStmt) {
        isBreakCalled = true;
    }

    @Override
    public void visit(TryCatchStmt tryCatchStmt) {
        // Note: This logic is based on Java exception and hence not recommended.
        // This is added only to make it work with blocking executor. and will be removed in a future release.
        StackFrame current = bContext.getControlStack().getCurrentFrame();
        try {
            tryCatchStmt.getTryBlock().execute(this);
        } catch (BallerinaException be) {
            BException exception;
            if (be.getBException() != null) {
                exception = be.getBException();
            } else {
                exception = new BException(be.getMessage());
            }
            exception.value().setStackTrace(ErrorHandlerUtils.getMainFuncStackTrace(bContext, null));
            while (bContext.getControlStack().getCurrentFrame() != current) {
                if (controlStack.getStack().size() > 0) {
                    controlStack.popFrame();
                } else {
                    // Throw this to handle at root error handler.
                    throw new BallerinaException(be);
                }
            }
            MemoryLocation memoryLocation = tryCatchStmt.getCatchBlock().getParameterDef().getMemoryLocation();
            if (memoryLocation instanceof StackVarLocation) {
                int stackFrameOffset = ((StackVarLocation) memoryLocation).getStackFrameOffset();
                controlStack.setValue(stackFrameOffset, exception);
            }
            tryCatchStmt.getCatchBlock().getCatchBlockStmt().execute(this);
        }
    }

    @Override
    public void visit(ThrowStmt throwStmt) {
        // Note: This logic is based on Java exception and hence not recommended.
        // This is added only to make it work with blocking executor. and will be removed in a future release.
        BException exception = (BException) throwStmt.getExpr().execute(this);
        exception.value().setStackTrace(ErrorHandlerUtils.getMainFuncStackTrace(bContext, null));
        throw new BallerinaException(exception);
    }

    @Override
    public void visit(FunctionInvocationStmt funcIStmt) {
        funcIStmt.getFunctionInvocationExpr().executeMultiReturn(this);
    }

    @Override
    public void visit(ActionInvocationStmt actionIStmt) {
        actionIStmt.getActionInvocationExpr().executeMultiReturn(this);
    }

    @Override
    public void visit(WorkerInvocationStmt workerInvocationStmt) {
        // Create the Stack frame
        Worker worker = workerInvocationStmt.getCallableUnit();

        int sizeOfValueArray = worker.getStackFrameSize();
        BValue[] localVals = new BValue[sizeOfValueArray];

        // Evaluate the argument expression
        BValue argValue = workerInvocationStmt.getInMsg().execute(this);

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


        //controlStack.popFrame();
    }

    @Override
    public void visit(WorkerReplyStmt workerReplyStmt) {
        Worker worker = workerReplyStmt.getWorker();
        Future<BMessage> future = worker.getResultFuture();
        try {
            // TODO: Make this value configurable - need grammar level rethink
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
    public void visit(ReturnStmt returnStmt) {
        Expression[] exprs = returnStmt.getExprs();

        // Check whether the first argument is a multi-return function
        if (exprs.length == 1 && exprs[0] instanceof FunctionInvocationExpr) {
            FunctionInvocationExpr funcIExpr = (FunctionInvocationExpr) exprs[0];
            if (funcIExpr.getTypes().length > 1) {
                BValue[] returnVals = funcIExpr.executeMultiReturn(this);
                for (int i = 0; i < returnVals.length; i++) {
                    controlStack.setReturnValue(i, returnVals[i]);
                }
                returnedOrReplied = true;
                return;
            }
        }

        for (int i = 0; i < exprs.length; i++) {
            Expression expr = exprs[i];
            BValue returnVal = expr.execute(this);
            controlStack.setReturnValue(i, returnVal);
        }

        returnedOrReplied = true;
    }

    @Override
    public void visit(ReplyStmt replyStmt) {
        // TODO revisit this logic
        Expression expr = replyStmt.getReplyExpr();
        BMessage bMessage = (BMessage) expr.execute(this);
        bContext.getBalCallback().done(bMessage.value());
        returnedOrReplied = true;
    }

    @Override
    public void visit(ForkJoinStmt forkJoinStmt) {
        VariableRefExpr expr = forkJoinStmt.getMessageReference();
        BMessage inMsg = (BMessage) expr.execute(this);
        List<WorkerRunner> workerRunnerList = new ArrayList<>();
        List<BMessage> resultMsgs = new ArrayList<>();
        int timeout = ((BInteger) forkJoinStmt.getTimeout().getTimeoutExpression().execute(this)).intValue();

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
                    resultMsgs.add(res);
                }
            } else {
                List<WorkerRunner> workerRunnersSpecified = new ArrayList<>();
                for (String workerName : joinWorkerNames) {
                    workerRunnersSpecified.add(triggeredWorkers.get(workerName));
                }
                BMessage res = invokeAnyWorker(workerRunnersSpecified, timeout);
                if (res != null) {
                    resultMsgs.add(res);
                }
            }
        } else {
            String[] joinWorkerNames = forkJoinStmt.getJoin().getJoinWorkers();
            if (joinWorkerNames.length == 0) {
                // If there are no workers specified, wait for all of all the workers
                resultMsgs.addAll(invokeAllWorkers(workerRunnerList, timeout));
            } else {
                List<WorkerRunner> workerRunnersSpecified = new ArrayList<>();
                for (String workerName : joinWorkerNames) {
                    workerRunnersSpecified.add(triggeredWorkers.get(workerName));
                }
                resultMsgs.addAll(invokeAllWorkers(workerRunnersSpecified, timeout));
            }
        }

        if (isForkJoinTimedOut) {
            // Execute the timeout block

            // Creating a new arrays
            BArray bArray = forkJoinStmt.getJoin().getJoinResult().getType().getDefaultValue();

            for (int i = 0; i < resultMsgs.size(); i++) {
                BValue value = resultMsgs.get(i);
                bArray.add(i, value);
            }

            int offsetJoin = ((StackVarLocation) forkJoinStmt.getTimeout().getTimeoutResult().getMemoryLocation()).
                    getStackFrameOffset();

            controlStack.setValue(offsetJoin, bArray);
            forkJoinStmt.getTimeout().getTimeoutBlock().execute(this);
            isForkJoinTimedOut = false;

        } else {
            // Assign values to join block message arrays

            // Creating a new arrays
            BArray bArray = forkJoinStmt.getJoin().getJoinResult().getType().getDefaultValue();
            for (int i = 0; i < resultMsgs.size(); i++) {
                BValue value = resultMsgs.get(i);
                bArray.add(i, value);
            }

            int offsetJoin = ((StackVarLocation) forkJoinStmt.getJoin().getJoinResult().getMemoryLocation()).
                    getStackFrameOffset();
            controlStack.setValue(offsetJoin, bArray);
            forkJoinStmt.getJoin().getJoinBlock().execute(this);
        }

    }

    private BMessage invokeAnyWorker(List<WorkerRunner> workerRunnerList, int timeout) {
        ExecutorService anyExecutor = Executors.newWorkStealingPool();
        BMessage result;
        try {
            result = anyExecutor.invokeAny(workerRunnerList, timeout, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException e) {
            return null;
        } catch (TimeoutException e) {
            isForkJoinTimedOut = true;
            return null;
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
                    isForkJoinTimedOut = true;
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
    public BValue[] visit(FunctionInvocationExpr funcIExpr) {

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

        StackFrame stackFrame = new StackFrame(localVals, returnVals, functionInfo);
        controlStack.pushFrame(stackFrame);

        // Check whether we are invoking a native function or not.
        if (function instanceof BallerinaFunction) {
            BallerinaFunction bFunction = (BallerinaFunction) function;
            bFunction.getCallableUnitBody().execute(this);
        } else {
            AbstractNativeFunction nativeFunction = (AbstractNativeFunction) function;
            nativeFunction.executeNative(bContext);
        }

        controlStack.popFrame();

        // Setting return values to function invocation expression
        returnedOrReplied = false;
        return returnVals;
    }

    @Override
    public BValue[] visit(ActionInvocationExpr actionIExpr) {
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
        StackFrame stackFrame = new StackFrame(localVals, returnVals, actionInfo);
        controlStack.pushFrame(stackFrame);

        // Check whether we are invoking a native action or not.
        if (action instanceof BallerinaAction) {
            BallerinaAction bAction = (BallerinaAction) action;
            bAction.getCallableUnitBody().execute(this);
        } else {
            AbstractNativeAction nativeAction = (AbstractNativeAction) action;
            nativeAction.execute(bContext);
        }

        controlStack.popFrame();

        // Setting return values to function invocation expression
        returnedOrReplied = false;
        return returnVals;
    }

    // TODO Check the possibility of removing this from the executor since this is not part of the executor.
    @Override
    public BValue[] visit(ResourceInvocationExpr resourceIExpr) {

        Resource resource = resourceIExpr.getResource();

        ControlStack controlStack = bContext.getControlStack();
        BValue[] valueParams = new BValue[resource.getStackFrameSize()];

        int valueCounter = populateArgumentValues(resourceIExpr.getArgExprs(), valueParams);
        // Populate values for Connector declarations
//        valueCounter = populateConnectorDclValues(resource.getConnectorDcls(), valueParams, valueCounter);

        // Create default values for all declared local variables
//        VariableDef[] variableDefs = resource.getVariableDefs();
//        for (VariableDef variableDef : variableDefs) {
//            valueParams[valueCounter] = variableDef.getType().getDefaultValue();
//            valueCounter++;
//        }

        BValue[] ret = new BValue[1];

        CallableUnitInfo resourceInfo = new CallableUnitInfo(resource.getName(), resource.getPackagePath(),
                resource.getNodeLocation());
        StackFrame stackFrame = new StackFrame(valueParams, ret, resourceInfo);
        controlStack.pushFrame(stackFrame);

        resource.getResourceBody().execute(this);

        return ret;
    }

    @Override
    public BValue visit(InstanceCreationExpr instanceCreationExpr) {
        return instanceCreationExpr.getType().getDefaultValue();
    }

    @Override
    public BValue visit(UnaryExpression unaryExpr) {
        Expression rExpr = unaryExpr.getRExpr();
        BValueType rValue = (BValueType) rExpr.execute(this);
        //ToDO this has to be improved property in UnaryExpression class since Unary does not need BiFunction
        return unaryExpr.getEvalFunc().apply(null, rValue);
    }

    @Override
    public BValue visit(BinaryExpression binaryExpr) {
        Expression rExpr = binaryExpr.getRExpr();
        BValueType rValue = (BValueType) rExpr.execute(this);

        Expression lExpr = binaryExpr.getLExpr();
        BValueType lValue = (BValueType) lExpr.execute(this);

        return binaryExpr.getEvalFunc().apply(lValue, rValue);
    }

    @Override
    public BValue visit(ArrayMapAccessExpr arrayMapAccessExpr) {

        VariableRefExpr arrayVarRefExpr = (VariableRefExpr) arrayMapAccessExpr.getRExpr();
        BValue collectionValue = arrayVarRefExpr.execute(this);

        if (collectionValue == null) {
            throw new BallerinaException("variable '" + arrayVarRefExpr.getVarName() + "' is null");
        }

        Expression indexExpr = arrayMapAccessExpr.getIndexExpr();
        BValue indexValue = indexExpr.execute(this);


        // Check whether this collection access expression is in the left hand of an assignment expression
        // If yes skip setting the value;
        if (!arrayMapAccessExpr.isLHSExpr()) {

            if (arrayMapAccessExpr.getType() != BTypes.typeMap) {
                // Get the value stored in the index
                if (collectionValue instanceof BArray) {
                    return ((BArray) collectionValue).get(((BInteger) indexValue).intValue());
                } else {
                    return collectionValue;
                }
            } else {
                // Get the value stored in the index
                if (indexValue instanceof BString) {
                    return ((BMap) collectionValue).get(indexValue);
                } else {
                    throw new IllegalStateException("Index of a map should be string type");
                }
            }
        } else {
            throw new IllegalStateException("This branch shouldn't be executed. ");
        }
    }

    @Override
    public BValue visit(ArrayInitExpr arrayInitExpr) {
        Expression[] argExprs = arrayInitExpr.getArgExprs();

        // Creating a new arrays
        BArray bArray = arrayInitExpr.getType().getDefaultValue();

        for (int i = 0; i < argExprs.length; i++) {
            Expression expr = argExprs[i];
            BValue value = expr.execute(this);
            bArray.add(i, value);
        }

        return bArray;
    }

    @Override
    public BValue visit(MapInitExpr mapInitExpr) {
        Expression[] argExprs = mapInitExpr.getArgExprs();

        // Creating a new arrays
        BMap bMap = mapInitExpr.getType().getDefaultValue();

        for (int i = 0; i < argExprs.length; i++) {
            MapStructInitKeyValueExpr expr = (MapStructInitKeyValueExpr) argExprs[i];
            BValue keyVal = expr.getKeyExpr().execute(this);
            BValue value = expr.getValueExpr().execute(this);
            bMap.put(keyVal, value);
        }

        return bMap;
    }

    @Override
    public BValue visit(RefTypeInitExpr refTypeInitExpr) {
        BType bType = refTypeInitExpr.getType();
        return bType.getDefaultValue();
    }

    @Override
    public BValue visit(ConnectorInitExpr connectorInitExpr) {
        BConnector bConnector;
        BValue[] connectorMemBlock;
        Connector connector = (Connector) connectorInitExpr.getType();

        if (connector instanceof AbstractNativeConnector) {

            AbstractNativeConnector nativeConnector = ((AbstractNativeConnector) connector).getInstance();
            Expression[] argExpressions = connectorInitExpr.getArgExprs();
            connectorMemBlock = new BValue[argExpressions.length];
            for (int j = 0; j < argExpressions.length; j++) {
                connectorMemBlock[j] = argExpressions[j].execute(this);
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

        } else {
            BallerinaConnectorDef connectorDef = (BallerinaConnectorDef) connector;

            int offset = 0;
            connectorMemBlock = new BValue[connectorDef.getSizeOfConnectorMem()];
            for (Expression expr : connectorInitExpr.getArgExprs()) {
                connectorMemBlock[offset] = expr.execute(this);
                offset++;
            }

            bConnector = new BConnector(connector, connectorMemBlock);

            // Invoke the <init> function
            invokeConnectorInitFunction(connectorDef, bConnector);

        }

        return bConnector;
    }

    @Override
    public BValue visit(BacktickExpr backtickExpr) {
        // Evaluate the variable references before creating objects
        String evaluatedString = evaluteBacktickString(backtickExpr);
        if (backtickExpr.getType() == BTypes.typeJSON) {
            return new BJSON(evaluatedString);

        } else {
            return new BXML(evaluatedString);
        }
    }

    @Override
    public BValue visit(VariableRefExpr variableRefExpr) {
        MemoryLocation memoryLocation = variableRefExpr.getMemoryLocation();
        return memoryLocation.execute(this);
    }

    @Override
    public BValue visit(TypeCastExpression typeCastExpression) {
        // Check for native type casting
        if (typeCastExpression.getEvalFunc() != null) {
            BValueType result = (BValueType) typeCastExpression.getRExpr().execute(this);
            return typeCastExpression.getEvalFunc().apply(result);
        } else {
            TypeMapper typeMapper = typeCastExpression.getCallableUnit();

            int sizeOfValueArray = typeMapper.getStackFrameSize();
            BValue[] localVals = new BValue[sizeOfValueArray];

            // Get values for all the function arguments
            int valueCounter = populateArgumentValues(typeCastExpression.getArgExprs(), localVals);

//            // Create default values for all declared local variables
//            for (VariableDef variableDef : typeMapper.getVariableDefs()) {
//                localVals[valueCounter] = variableDef.getType().getDefaultValue();
//                valueCounter++;
//            }

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

            StackFrame stackFrame = new StackFrame(localVals, returnVals, functionInfo);
            controlStack.pushFrame(stackFrame);

            // Check whether we are invoking a native function or not.
            if (typeMapper instanceof BTypeMapper) {
                BTypeMapper bTypeMapper = (BTypeMapper) typeMapper;
                bTypeMapper.getCallableUnitBody().execute(this);
            } else {
                AbstractNativeTypeMapper nativeTypeMapper = (AbstractNativeTypeMapper) typeMapper;
                nativeTypeMapper.convertNative(bContext);
            }

            controlStack.popFrame();

            // Setting return values to function invocation expression
            returnedOrReplied = false;
            return returnVals[0];
        }
    }

    @Override
    public BValue visit(BasicLiteral basicLiteral) {
        return basicLiteral.getBValue();
    }

    @Override
    public BValue visit(StackVarLocation stackVarLocation) {
        int offset = stackVarLocation.getStackFrameOffset();
        return controlStack.getValue(offset);
    }

    @Override
    public BValue visit(ConstantLocation constantLocation) {
        int offset = constantLocation.getStaticMemAddrOffset();
        RuntimeEnvironment.StaticMemory staticMemory = runtimeEnv.getStaticMemory();
        return staticMemory.getValue(offset);
    }

    @Override
    public BValue visit(ServiceVarLocation serviceVarLocation) {
        int offset = serviceVarLocation.getStaticMemAddrOffset();
        RuntimeEnvironment.StaticMemory staticMemory = runtimeEnv.getStaticMemory();
        return staticMemory.getValue(offset);
    }

    @Override
    public BValue visit(StructVarLocation structLocation) {
        throw new IllegalArgumentException("struct value is required to get the value of a field");
    }

    @Override
    public BValue visit(ConnectorVarLocation connectorVarLocation) {
        // Fist the get the BConnector object. In an action invocation first argument is always the connector
        BConnector bConnector = (BConnector) controlStack.getValue(0);
        if (bConnector == null) {
            throw new BallerinaException("Connector argument value is null");
        }

        // Now get the connector variable value from the memory block allocated to the BConnector instance.
        return bConnector.getValue(connectorVarLocation.getConnectorMemAddrOffset());
    }


    // Private methods

    private int populateArgumentValues(Expression[] expressions, BValue[] localVals) {
        int i = 0;
        for (Expression arg : expressions) {
            // Evaluate the argument expression
            BValue argValue = arg.execute(this);
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
            bVal = expression.execute(this);
            strVal = bVal.stringValue();
            if (isJson && bVal instanceof BString && expression instanceof ReferenceExpr) {
                builder.append("\"" + strVal + "\"");
            } else {
                builder.append(strVal);
            }
        }
        return builder.toString();
    }

    private void assignValueToArrayMapAccessExpr(BValue rValue, ArrayMapAccessExpr lExpr) {
        ArrayMapAccessExpr accessExpr = lExpr;
        if (!(accessExpr.getType() == BTypes.typeMap)) {
            BArray arrayVal = (BArray) accessExpr.getRExpr().execute(this);
            BInteger indexVal = (BInteger) accessExpr.getIndexExpr().execute(this);
            arrayVal.add(indexVal.intValue(), rValue);

        } else {
            BMap<BString, BValue> mapVal = (BMap<BString, BValue>) accessExpr.getRExpr().execute(this);
            BString indexVal = (BString) accessExpr.getIndexExpr().execute(this);
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
     * Initialize a user defined struct type.
     */
    @Override
    public BValue visit(StructInitExpr structInitExpr) {
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
            structMemBlock[structVarLoc.getStructMemAddrOffset()] = expr.getValueExpr().execute(this);
        }

        return new BStruct(structDef, structMemBlock);
    }

    /**
     * Evaluate and return the value of a struct field accessing expression.
     */
    @Override
    public BValue visit(StructFieldAccessExpr structFieldAccessExpr) {
        Expression varRef = structFieldAccessExpr.getVarRef();
        BValue value = varRef.execute(this);
        return getFieldExprValue(structFieldAccessExpr, value);
    }

    @Override
    public BValue visit(WorkerVarLocation workerVarLocation) {
        int offset = workerVarLocation.getworkerMemAddrOffset();
        return controlStack.getValue(offset);
    }

    /**
     * Assign a value to a field of a struct, represented by a {@link StructFieldAccessExpr}.
     *
     * @param rValue Value to be assigned
     * @param lExpr  {@link StructFieldAccessExpr} which represents the field of the struct
     */
    private void assignValueToStructFieldAccessExpr(BValue rValue, StructFieldAccessExpr lExpr) {
        Expression lExprVarRef = lExpr.getVarRef();
        BValue value = lExprVarRef.execute(this);
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
            setUnitValue(rValue, currentStructVal, fieldLocation, fieldExpr);
            return;
        }

        // At this point, field of the field is not null. Means current element,
        // and its field are both struct types.
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
        BValue indexValue = indexExpr.execute(this);

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
            // Value stored in the struct can be also an arrays. Hence if its an arrray access,
            // get the aray element value
            return getUnitValue(currentStructVal.getValue(fieldLocation), fieldExpr);
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
        BValue indexValue = indexExpr.execute(this);

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

    private void invokeConnectorInitFunction(BallerinaConnectorDef connectorDef, BConnector bConnector) {
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

        StackFrame stackFrame = new StackFrame(localVals, returnVals, functionInfo);
        controlStack.pushFrame(stackFrame);
        initFunction.getCallableUnitBody().execute(this);
        controlStack.popFrame();
    }
}

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

import com.fasterxml.jackson.databind.JsonNode;
import org.ballerinalang.model.Action;
import org.ballerinalang.model.BallerinaAction;
import org.ballerinalang.model.BallerinaConnectorDef;
import org.ballerinalang.model.BallerinaFunction;
import org.ballerinalang.model.ExecutableMultiReturnExpr;
import org.ballerinalang.model.Function;
import org.ballerinalang.model.NodeExecutor;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.Resource;
import org.ballerinalang.model.StructDef;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.SymbolScope;
import org.ballerinalang.model.Worker;
import org.ballerinalang.model.expressions.ActionInvocationExpr;
import org.ballerinalang.model.expressions.ArrayInitExpr;
import org.ballerinalang.model.expressions.ArrayLengthExpression;
import org.ballerinalang.model.expressions.ArrayMapAccessExpr;
import org.ballerinalang.model.expressions.BacktickExpr;
import org.ballerinalang.model.expressions.BasicLiteral;
import org.ballerinalang.model.expressions.BinaryEqualityExpression;
import org.ballerinalang.model.expressions.BinaryExpression;
import org.ballerinalang.model.expressions.ConnectorInitExpr;
import org.ballerinalang.model.expressions.Expression;
import org.ballerinalang.model.expressions.FieldAccessExpr;
import org.ballerinalang.model.expressions.FunctionInvocationExpr;
import org.ballerinalang.model.expressions.InstanceCreationExpr;
import org.ballerinalang.model.expressions.JSONArrayInitExpr;
import org.ballerinalang.model.expressions.JSONFieldAccessExpr;
import org.ballerinalang.model.expressions.JSONInitExpr;
import org.ballerinalang.model.expressions.KeyValueExpr;
import org.ballerinalang.model.expressions.MapInitExpr;
import org.ballerinalang.model.expressions.NullLiteral;
import org.ballerinalang.model.expressions.RefTypeInitExpr;
import org.ballerinalang.model.expressions.ReferenceExpr;
import org.ballerinalang.model.expressions.ResourceInvocationExpr;
import org.ballerinalang.model.expressions.StructInitExpr;
import org.ballerinalang.model.expressions.TypeCastExpression;
import org.ballerinalang.model.expressions.TypeConversionExpr;
import org.ballerinalang.model.expressions.UnaryExpression;
import org.ballerinalang.model.expressions.VariableRefExpr;
import org.ballerinalang.model.statements.AbortStmt;
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
import org.ballerinalang.model.statements.TransactionStmt;
import org.ballerinalang.model.statements.TransformStmt;
import org.ballerinalang.model.statements.TryCatchStmt;
import org.ballerinalang.model.statements.VariableDefStmt;
import org.ballerinalang.model.statements.WhileStmt;
import org.ballerinalang.model.statements.WorkerInvocationStmt;
import org.ballerinalang.model.statements.WorkerReplyStmt;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeLattice;
import org.ballerinalang.model.util.BValueUtils;
import org.ballerinalang.model.util.JSONUtils;
import org.ballerinalang.model.util.XMLUtils;
import org.ballerinalang.model.values.BArray;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BConnector;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BMessage;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueType;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;
import org.ballerinalang.runtime.worker.WorkerCallback;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringJoiner;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private boolean isAbortCalled;
    public BStruct thrownError;
    public boolean isErrorThrown;
    private boolean inFinalBlock;
    private boolean inRollbackBlock;

    private StructDef error, stackTraceItemDef, stackTraceDef;
    private SymbolScope parentScope;

    public BLangExecutor(RuntimeEnvironment runtimeEnv, Context bContext) {
        this.runtimeEnv = runtimeEnv;
        this.bContext = bContext;
        this.controlStack = bContext.getControlStack();
    }

    public void setParentScope(SymbolScope parentScope) {
        this.parentScope = parentScope;
    }

    @Override
    public void visit(BlockStmt blockStmt) {
        Statement[] stmts = blockStmt.getStatements();
        for (Statement stmt : stmts) {
            if (isBreakCalled || isAbortCalled) {
                break;
            }
            if (inFinalBlock || inRollbackBlock) {
            } else if (isErrorThrown || returnedOrReplied) {
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
            rValue = lExpr.getType().getZeroValue();
        } else {
            rValue = rExpr.execute(this);
        }

        if (lExpr instanceof VariableRefExpr) {
            assignValueToVarRefExpr(rValue, (VariableRefExpr) lExpr);
        } else if (lExpr instanceof ArrayMapAccessExpr) {
            assignValueToArrayMapAccessExpr(rValue, (ArrayMapAccessExpr) lExpr);
        } else if (lExpr instanceof FieldAccessExpr) {
            assignValueToFieldAccessExpr(rValue, (FieldAccessExpr) lExpr);
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
            rValues = ((ExecutableMultiReturnExpr) rExpr).executeMultiReturn(this);
        } else if (rExpr == null) {
            rValues = new BValue[]{lExprs[0].getType().getZeroValue()};
        } else {
            rValues = new BValue[]{rExpr.execute(this)};
        }

        for (int i = 0; i < lExprs.length; i++) {
            Expression lExpr = lExprs[i];
            BValue rValue = rValues[i];
            if (lExpr instanceof VariableRefExpr) {
                if ("_".equals(((VariableRefExpr) lExpr).getVarName())) {
                    continue;
                }
                assignValueToVarRefExpr(rValue, (VariableRefExpr) lExpr);
            } else if (lExpr instanceof ArrayMapAccessExpr) {
                assignValueToArrayMapAccessExpr(rValue, (ArrayMapAccessExpr) lExpr);
            } else if (lExpr instanceof FieldAccessExpr) {
                assignValueToFieldAccessExpr(rValue, (FieldAccessExpr) lExpr);
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
            if (isBreakCalled || isAbortCalled) {
                break;
            }
            if (inFinalBlock || inRollbackBlock) {
            } else if (isErrorThrown || returnedOrReplied) {
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
        StackFrame current = bContext.getControlStack().getCurrentFrame();
        try {
            tryCatchStmt.getTryBlock().execute(this);
        } catch (RuntimeException be) {
            createBErrorFromException(be);
        }
        // Engage Catch statement.
        if (isErrorThrown) {
            TryCatchStmt.CatchBlock equalCatchType = null;
            TryCatchStmt.CatchBlock equivalentCatchBlock = null;
            for (TryCatchStmt.CatchBlock catchBlock : tryCatchStmt.getCatchBlocks()) {
                if (thrownError.getType().equals(catchBlock.getParameterDef().getType())) {
                    equalCatchType = catchBlock;
                    break;
                }
                if (equivalentCatchBlock == null && (TypeLattice.getExplicitCastLattice().getEdgeFromTypes
                        (thrownError.getType(), catchBlock.getParameterDef().getType(), null) != null)) {
                    equivalentCatchBlock = catchBlock;
                }
            }
            if (equalCatchType != null || equivalentCatchBlock != null) {
                handleError(equalCatchType != null ? equalCatchType : equivalentCatchBlock, current);
            }
        }
        // Invoke Finally Block.
        TryCatchStmt.FinallyBlock finallyBlock = tryCatchStmt.getFinallyBlock();
        if (finallyBlock != null) {
            inFinalBlock = true;
            finallyBlock.getFinallyBlockStmt().execute(this);
            inFinalBlock = false;
        }
    }

    @Override
    public void visit(ThrowStmt throwStmt) {
        thrownError = (BStruct) throwStmt.getExpr().execute(this);
        thrownError.setStackTrace(generateStackTrace());
        isErrorThrown = true;
    }

    private BStruct generateStackTrace() {

        if (stackTraceDef == null) {
            stackTraceItemDef = (StructDef) parentScope.resolve(new SymbolName("StackTraceItem",
                    "ballerina.lang.errors"));
            stackTraceDef = (StructDef) parentScope.resolve(new SymbolName("StackTrace",
                    "ballerina.lang.errors"));
            if (stackTraceDef == null) {
                throw new BLangRuntimeException("Unresolved type ballerina.lang.errors:StackTraceItem");
            }
        }
        BArray<BStruct> bArray = stackTraceDef.getFieldDefStmts()[0].getVariableDef().getType().getEmptyValue();
        Stack<StackFrame> stack = bContext.getControlStack().getStack();
        BStruct stackTrace = new BStruct(stackTraceDef, new BValue[]{bArray});
        for (int i = stack.size(); i > 0; i--) {
            StackFrame currentFrame = stack.get(i - 1);
            BValue[] structInfo = {
                    new BString(currentFrame.getNodeInfo().getName()),
                    new BString(currentFrame.getNodeInfo().getPackage()),
                    new BString(currentFrame.getNodeInfo().getNodeLocation().getFileName()),
                    new BInteger(currentFrame.getNodeInfo().getNodeLocation().getLineNumber()),
            };
            BStruct frameItem = new BStruct(stackTraceItemDef, structInfo);
            bArray.add((stack.size() - i), frameItem);
        }
        return stackTrace;
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

        Expression[] expressions = workerInvocationStmt.getExpressionList();
        // Extract the outgoing expressions
        BValue[] arguments = new BValue[expressions.length];
        populateArgumentValuesForWorker(expressions, arguments);
        if (workerInvocationStmt.getWorkerDataChannel() != null) {
            workerInvocationStmt.getWorkerDataChannel().putData(arguments);
        } else {
            BArray<BValue> bArray = new BArray<>(BValue.class);
            for (int j = 0; j < arguments.length; j++) {
                BValue returnVal = arguments[j];
                bArray.add(j, returnVal);
            }
            controlStack.setReturnValue(0, bArray);
        }

//        // Create the Stack frame
//        Worker worker = workerInvocationStmt.getCallableUnit();
//
//        int sizeOfValueArray = worker.getStackFrameSize();
//        BValue[] localVals = new BValue[sizeOfValueArray];
//
//        // Get values for all the worker arguments
//        int valueCounter = 0;
//        // Evaluate the argument expression
//        Expression[] expressions = workerInvocationStmt.getExpressionList();
//        for (Expression expression : expressions) {
//            localVals[valueCounter++] = expression.execute(this);
//        }
//
//        for (ParameterDef returnParam : worker.getReturnParameters()) {
//            // Check whether these are unnamed set of return types.
//            // If so break the loop. You can't have a mix of unnamed and named returns parameters.
//            if (returnParam.getName() == null) {
//                break;
//            }
//
//            localVals[valueCounter] = returnParam.getType().getDefaultValue();
//            valueCounter++;
//        }
//
//
//        // Create an arrays in the stack frame to hold return values;
//        BValue[] returnVals = new BValue[1];
//
//        // Create a new stack frame with memory locations to hold parameters, local values, temp expression value,
//        // return values and worker invocation location;
//        CallableUnitInfo functionInfo = new CallableUnitInfo(worker.getName(), worker.getPackagePath(),
//                workerInvocationStmt.getNodeLocation());
//
//        StackFrame stackFrame = new StackFrame(localVals, returnVals, functionInfo);
//        Context workerContext = new Context();
//        workerContext.getControlStack().pushFrame(stackFrame);
//        WorkerCallback workerCallback = new WorkerCallback(workerContext);
//        workerContext.setBalCallback(workerCallback);
//        BLangExecutor workerExecutor = new BLangExecutor(runtimeEnv, workerContext);
//
//        executor = Executors.newSingleThreadExecutor(new BLangThreadFactory(worker.getName()));
//        WorkerRunner workerRunner = new WorkerRunner(workerExecutor, workerContext, worker);
//        Future<BMessage> future = executor.submit(workerRunner);
//        worker.setResultFuture(future);
//
//
//        //controlStack.popFrame();
    }

    @Override
    public void visit(WorkerReplyStmt workerReplyStmt) {
        Expression[] localVars = workerReplyStmt.getExpressionList();
        BValue[] passedInValues = (BValue[]) workerReplyStmt.getWorkerDataChannel().takeData();

        for (int i = 0; i < localVars.length; i++) {
            Expression lExpr = localVars[i];
            BValue rValue = passedInValues[i];
            if (lExpr instanceof VariableRefExpr) {
                assignValueToVarRefExpr(rValue, (VariableRefExpr) lExpr);
            } else if (lExpr instanceof ArrayMapAccessExpr) {
                assignValueToArrayMapAccessExpr(rValue, (ArrayMapAccessExpr) lExpr);
            } else if (lExpr instanceof FieldAccessExpr) {
                assignValueToFieldAccessExpr(rValue, (FieldAccessExpr) lExpr);
            }
        }

//        Worker worker = workerReplyStmt.getWorker();
//        Future<BMessage> future = worker.getResultFuture();
//        try {
//            // TODO: Make this value configurable - need grammar level rethink
//            BMessage result = future.get(60, TimeUnit.SECONDS);
//            Expression[] expressions = workerReplyStmt.getExpressionList();
//            for (Expression expression: expressions) {
//                assignValueToVarRefExpr(result, (VariableRefExpr)expression);
//            }
//            executor.shutdown();
//            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
//                executor.shutdownNow();
//            }
//        } catch (Exception e) {
//            // If there is an exception in the worker, set an empty value to the return variable
//            BMessage result = BTypes.typeMessage.getDefaultValue();
//            Expression[] expressions = workerReplyStmt.getExpressionList();
//            for (Expression expression: expressions) {
//                assignValueToVarRefExpr(result, (VariableRefExpr)expression);
//            }
//        } finally {
//            // Finally, try again to shutdown if not done already
//            executor.shutdownNow();
//        }
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
        BMessage bMessage = null;
        if (expr != null) {
            bMessage = (BMessage) expr.execute(this);
        }
        bContext.getBalCallback().done(bMessage != null ? bMessage.value() : null);
        returnedOrReplied = true;
    }

    @Override
    public void visit(ForkJoinStmt forkJoinStmt) {
        List<WorkerRunner> workerRunnerList = new ArrayList<>();
        List<BValue[]> resultMsgs = new ArrayList<>();
        long timeout = 120; // Default value is 2 minutes for timeout
        if (forkJoinStmt.getTimeout().getTimeoutExpression() != null) {
            timeout = ((BInteger) forkJoinStmt.getTimeout().getTimeoutExpression().execute(this)).intValue();
        }

        Worker[] workers = forkJoinStmt.getWorkers();
        Map<String, WorkerRunner> triggeredWorkers = new HashMap<>();
        for (Worker worker : workers) {
            int sizeOfValueArray = worker.getStackFrameSize();
            BValue[] localVals = new BValue[sizeOfValueArray];

            // Copying the values of current stack frame to the local values array at the beginning
            BValue[] currentStackValues = this.controlStack.getCurrentFrame().values;
            System.arraycopy(currentStackValues, 0, localVals, 0, worker.getAccessibleStackFrameSize());

            // TODO: This is not needed anymore. Remove when cleaning up the code.
            int valueCounter = worker.getAccessibleStackFrameSize();
            for (ParameterDef returnParam : worker.getReturnParameters()) {
                // Check whether these are unnamed set of return types.
                // If so break the loop. You can't have a mix of unnamed and named returns parameters.
                if (returnParam.getName() == null) {
                    break;
                }

                localVals[valueCounter] = returnParam.getType().getEmptyValue();
                valueCounter++;
            }

            // Create an array in the stack frame to hold return values;
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
            workerExecutor.setParentScope(worker);
            WorkerRunner workerRunner = new WorkerRunner(workerExecutor, workerContext, worker);
            workerRunnerList.add(workerRunner);
            triggeredWorkers.put(worker.getName(), workerRunner);
        }

        if (forkJoinStmt.getJoin().getJoinType().equalsIgnoreCase("any")) {
            String[] joinWorkerNames = forkJoinStmt.getJoin().getJoinWorkers();
            if (joinWorkerNames.length == 0) {
                // If there are no workers specified, wait for any of all the workers
                BValue[] res = invokeAnyWorker(workerRunnerList, timeout);
                resultMsgs.add(res);
            } else {
                List<WorkerRunner> workerRunnersSpecified = new ArrayList<>();
                for (String workerName : joinWorkerNames) {
                    workerRunnersSpecified.add(triggeredWorkers.get(workerName));
                }
                BValue[] res = invokeAnyWorker(workerRunnersSpecified, timeout);
                resultMsgs.add(res);
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

            int offsetTimeout = ((StackVarLocation) forkJoinStmt.getTimeout().getTimeoutResult().getMemoryLocation()).
                    getStackFrameOffset();
            BArray<BArray> bbArray = new BArray<>(BArray.class);

            for (int i = 0; i < resultMsgs.size(); i++) {
                BValue[] value = resultMsgs.get(i);
                BArray<BValue> bArray = new BArray<>(BValue.class);
                for (int j = 0; j < value.length; j++) {
                    BValue returnVal = value[j];
                    bArray.add(j, returnVal);
                }
                bbArray.add(i, bArray);
            }
            controlStack.setValue(offsetTimeout, bbArray);
            forkJoinStmt.getTimeout().getTimeoutBlock().execute(this);
            isForkJoinTimedOut = false;

        } else {
            // Assign values to join block message arrays
            int offsetJoin = ((StackVarLocation) forkJoinStmt.getJoin().getJoinResult().getMemoryLocation()).
                    getStackFrameOffset();
            BArray<BArray> bbArray = new BArray<>(BArray.class);

            for (int i = 0; i < resultMsgs.size(); i++) {
                BValue[] value = resultMsgs.get(i);
                BArray<BValue> bArray = new BArray<>(BValue.class);
                for (int j = 0; j < value.length; j++) {
                    BValue returnVal = value[j];
                    bArray.add(j, returnVal);
                }
                bbArray.add(i, bArray);
            }
            controlStack.setValue(offsetJoin, bbArray);
            forkJoinStmt.getJoin().getJoinBlock().execute(this);
        }

    }

    @Override
    public void visit(TransformStmt transformStmt) {
        transformStmt.getBody().execute(this);
    }

    @Override
    public void visit(TransactionStmt transactionStmt) {
        BallerinaTransactionManager ballerinaTransactionManager = bContext.getBallerinaTransactionManager();
        if (ballerinaTransactionManager == null) {
            ballerinaTransactionManager = new BallerinaTransactionManager();
            bContext.setBallerinaTransactionManager(ballerinaTransactionManager);
        }
        StackFrame current = bContext.getControlStack().getCurrentFrame();
        //execute transaction block
        ballerinaTransactionManager.beginTransactionBlock();
        try {
            transactionStmt.getTransactionBlock().execute(this);
            if (isErrorThrown) {
                ballerinaTransactionManager.setTransactionError(true);
            }
            ballerinaTransactionManager.commitTransactionBlock();
        } catch (Exception e) {
            createBErrorFromException(e);
            while (bContext.getControlStack().getCurrentFrame() != current) {
                if (controlStack.getStack().size() > 0) {
                    controlStack.popFrame();
                } else {
                    throw new BallerinaException(e);
                }
            }
        }
        isAbortCalled = false;
        //execute onRollback if necessary
        try {
            if (ballerinaTransactionManager.isTransactionError()) {
                ballerinaTransactionManager.rollbackTransactionBlock();
                inRollbackBlock = true;
                TransactionStmt.AbortedBlock abortedBlock = transactionStmt.getAbortedBlock();
                if (abortedBlock != null) {
                    abortedBlock.getAbortedBlockStmt().execute(this);
                }
            } else {
                TransactionStmt.CommittedBlock committedBlock = transactionStmt.getCommittedBlock();
                if (committedBlock != null) {
                    committedBlock.getCommittedBlockStmt().execute(this);
                }
            }
        } catch (Exception e) {
            throw new BallerinaException(e);
        } finally {
            inRollbackBlock = false;
            ballerinaTransactionManager.endTransactionBlock();
            if (ballerinaTransactionManager.isOuterTransaction()) {
                bContext.setBallerinaTransactionManager(null);
            }
        }
    }

    @Override
    public void visit(AbortStmt abortStmt) {
        isAbortCalled = true;
        BallerinaTransactionManager ballerinaTransactionManager = bContext.getBallerinaTransactionManager();
        if (ballerinaTransactionManager != null) {
            ballerinaTransactionManager.setTransactionError(true);
        }
    }

    private BValue[] invokeAnyWorker(List<WorkerRunner> workerRunnerList, long timeout) {
        ExecutorService anyExecutor = Executors.newWorkStealingPool();
        BValue[] result;
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

    private List<BValue[]> invokeAllWorkers(List<WorkerRunner> workerRunnerList, long timeout) {
        ExecutorService allExecutor = Executors.newWorkStealingPool();
        List<BValue[]> result = new ArrayList<>();
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

            }).forEach((BValue[] b) -> {
                result.add(b);
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

        if (function instanceof BallerinaFunction) {
            // Start the workers defined within the function
            for (Worker worker : ((BallerinaFunction) function).getWorkers()) {
                executeWorker(worker, funcIExpr.getArgExprs());
            }
        }

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

            localVals[valueCounter] = returnParam.getType().getZeroValue();
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

        // Start the workers within the action
        if (action instanceof BallerinaAction) {
            for (Worker worker : ((BallerinaAction) action).getWorkers()) {
                executeWorker(worker, actionIExpr.getArgExprs());
            }
        }

        BValue[] localVals = new BValue[action.getStackFrameSize()];

        // Create default values for all declared local variables
        int valueCounter = populateArgumentValues(actionIExpr.getArgExprs(), localVals);

        for (ParameterDef returnParam : action.getReturnParameters()) {
            // Check whether these are unnamed set of return types.
            // If so break the loop. You can't have a mix of unnamed and named returns parameters.
            if (returnParam.getName() == null) {
                break;
            }

            localVals[valueCounter] = returnParam.getType().getZeroValue();
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

        // Start the workers within the resource
        for (Worker worker : resource.getWorkers()) {
            executeWorker(worker, resourceIExpr.getArgExprs());
        }

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
        return instanceCreationExpr.getType().getZeroValue();
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
        BValue rValue = rExpr.execute(this);

        Expression lExpr = binaryExpr.getLExpr();
        BValue lValue = lExpr.execute(this);

        return binaryExpr.getEvalFunc().apply(lValue, rValue);
    }

    @Override
    public BValue visit(BinaryEqualityExpression binaryEqualityExpr) {
        Expression rExpr = binaryEqualityExpr.getRExpr();
        Expression lExpr = binaryEqualityExpr.getLExpr();

        BValue rValue = rExpr.execute(this);
        BValue lValue = lExpr.execute(this);

        // if this is a null check, then need to pass the BValue
        if (rExpr.getType() == BTypes.typeNull || lExpr.getType() == BTypes.typeNull) {
            return binaryEqualityExpr.getRefTypeEvalFunc().apply(lValue, rValue);
        }

        return binaryEqualityExpr.getEvalFunc().apply((BValueType) lValue, (BValueType) rValue);
    }

    @Override
    public BValue visit(ArrayMapAccessExpr arrayMapAccessExpr) {
        VariableRefExpr arrayVarRefExpr = (VariableRefExpr) arrayMapAccessExpr.getRExpr();
        BValue collectionValue = arrayVarRefExpr.execute(this);

        if (collectionValue == null) {
            throw new BallerinaException("variable '" + arrayVarRefExpr.getSymbolName() + "' is null");
        }

        Expression[] indexExprs = arrayMapAccessExpr.getIndexExprs();

        // Check whether this collection access expression is in the left hand of an assignment expression
        // If yes skip setting the value;
        if (!arrayMapAccessExpr.isLHSExpr()) {

            if (arrayMapAccessExpr.getRExpr().getType() != BTypes.typeMap) {
                // Get the value stored in the index
                if (collectionValue instanceof BArray) {
                    BArray bArray = (BArray) collectionValue;
                    bArray = retrieveArray(bArray, indexExprs);
                    return bArray.get(((BInteger) indexExprs[0].execute(this)).intValue());
                } else {
                    return collectionValue;
                }
            } else {
                // Get the value stored in the index
                BValue indexValue = indexExprs[0].execute(this);
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
        BArray bArray = arrayInitExpr.getType().getEmptyValue();

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

        // Creating a new map
        BMap bMap = mapInitExpr.getType().getEmptyValue();

        for (int i = 0; i < argExprs.length; i++) {
            KeyValueExpr expr = (KeyValueExpr) argExprs[i];
            BValue keyVal = expr.getKeyExpr().execute(this);
            BValue value = expr.getValueExpr().execute(this);
            bMap.put(keyVal, value);
        }
        return bMap;
    }

    @Override
    public BValue visit(JSONInitExpr jsonInitExpr) {
        Expression[] argExprs = jsonInitExpr.getArgExprs();
        StringJoiner stringJoiner = new StringJoiner(",", "{", "}");
        for (int i = 0; i < argExprs.length; i++) {
            KeyValueExpr expr = (KeyValueExpr) argExprs[i];
            BValue keyVal = expr.getKeyExpr().execute(this);
            BValue value = expr.getValueExpr().execute(this);
            String stringVal;
            if (value == null) {
                stringVal = null;
            } else if (value instanceof BString) {
                stringVal = "\"" + value.stringValue() + "\"";
            } else if (value instanceof BJSON) {
                JsonNode jsonNode = ((BJSON) value).value();
                stringVal = jsonNode.toString();
            } else  {
                stringVal = value.stringValue();
            }
            stringJoiner.add("\"" + keyVal.stringValue() + "\" : " + stringVal + "");
        }
        return new BJSON(stringJoiner.toString());
    }

    @Override
    public BValue visit(JSONArrayInitExpr jsonArrayInitExpr) {
        Expression[] argExprs = jsonArrayInitExpr.getArgExprs();
        StringJoiner stringJoiner = new StringJoiner(",", "[", "]");
        for (int i = 0; i < argExprs.length; i++) {
            BValue value = argExprs[i].execute(this);
            String stringVal;
            if (value == null) {
                stringVal = null;
            } else if (value instanceof BString) {
                stringVal = "\"" + value.stringValue() + "\"";
            } else if (value instanceof BJSON) {
                JsonNode jsonNode = ((BJSON) value).value();
                stringVal = jsonNode.toString();
            } else {
                stringVal = value.stringValue();
            }
            stringJoiner.add(stringVal);
        }
        return new BJSON(stringJoiner.toString());
    }

    @Override
    public BValue visit(RefTypeInitExpr refTypeInitExpr) {
        BType bType = refTypeInitExpr.getType();
        return bType.getEmptyValue();
    }

    @Override
    public BValue visit(ConnectorInitExpr connectorInitExpr) {
        BConnector bConnector;
        BValue[] connectorMemBlock;
        BallerinaConnectorDef connectorDef = (BallerinaConnectorDef) connectorInitExpr.getType();

        int offset = 0;
        connectorMemBlock = new BValue[connectorDef.getSizeOfConnectorMem()];
        for (Expression expr : connectorInitExpr.getArgExprs()) {
            connectorMemBlock[offset] = expr.execute(this);
            offset++;
        }

        bConnector = new BConnector(connectorDef, connectorMemBlock);

        // Invoke the <init> function
        invokeConnectorInitFunction(connectorDef, bConnector);

        // Invoke the <init> action
        invokeConnectorInitAction(connectorDef, bConnector);
        return bConnector;
    }

    @Override
    public BValue visit(BacktickExpr backtickExpr) {
        // Evaluate the variable references before creating objects
        String evaluatedString = evaluteBacktickString(backtickExpr);
        if (backtickExpr.getType() == BTypes.typeJSON) {
            return new BJSON(evaluatedString);

        } else {
            return XMLUtils.parse(evaluatedString);
        }
    }

    @Override
    public BValue visit(VariableRefExpr variableRefExpr) {
        MemoryLocation memoryLocation = variableRefExpr.getMemoryLocation();
        return memoryLocation.execute(this);
    }

    @Override
    public BValue[] visit(TypeCastExpression typeCastExpression) {
        // Check for native type casting
        BValue result = (BValue) typeCastExpression.getRExpr().execute(this);
        return typeCastExpression.getEvalFunc().apply(result, typeCastExpression.getType(), 
                typeCastExpression.isMultiReturnExpr());
    }

    @Override
    public BValue[] visit(TypeConversionExpr nativeTransformExpression) {
        BValue result = (BValue) nativeTransformExpression.getRExpr().execute(this);
        return nativeTransformExpression.getEvalFunc().apply(result, nativeTransformExpression.getType(),
                nativeTransformExpression.isMultiReturnExpr());
    }
    
    @Override
    public BValue visit(BasicLiteral basicLiteral) {
        return basicLiteral.getBValue();
    }

    @Override
    public BValue visit(NullLiteral nullLiteral) {
        return nullLiteral.getBValue();
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
    public BValue visit(GlobalVarLocation globalVarLocation) {
        int offset = globalVarLocation.getStaticMemAddrOffset();
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
            throw new BallerinaException("connector argument value is null");
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

    private int populateArgumentValuesForWorker(Expression[] expressions, BValue[] localVals) {
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

            // If the type is message, then clone and set the value
            if (argType.equals(BTypes.typeMessage)) {
                if (argValue != null) {
                    argValue = ((BMessage) argValue).clone();
                }
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
        if (!(accessExpr.getRExpr().getType() == BTypes.typeMap)) {
            BArray arrayVal = (BArray) accessExpr.getRExpr().execute(this);

            if (arrayVal == null) {
                throw new BallerinaException("variable '" + accessExpr.getSymbolName() + "' is null");
            }

            Expression[] indexExprs = accessExpr.getIndexExprs();
            if (indexExprs.length > 1) {
                arrayVal = retrieveArray(arrayVal, indexExprs);
            }

            BInteger indexVal = (BInteger) indexExprs[0].execute(this);
            arrayVal.add(indexVal.intValue(), rValue);

        } else {
            BMap<BString, BValue> mapVal = (BMap<BString, BValue>) accessExpr.getRExpr().execute(this);
            BString indexVal = (BString) accessExpr.getIndexExprs()[0].execute(this);
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
                throw new BallerinaException("connector argument value is null");
            }

            int connectorMemOffset = ((ConnectorVarLocation) memoryLocation).getConnectorMemAddrOffset();
            bConnector.setValue(connectorMemOffset, rValue);
        } else if (memoryLocation instanceof WorkerVarLocation) {
            int stackFrameOffset = ((WorkerVarLocation) memoryLocation).getworkerMemAddrOffset();
            controlStack.setValue(stackFrameOffset, rValue);
        } else if (memoryLocation instanceof StructVarLocation) {
            int structMemOffset = ((StructVarLocation) memoryLocation).getStructMemAddrOffset();
            controlStack.setValue(structMemOffset, rValue);
        } else if (memoryLocation instanceof GlobalVarLocation) {
            int globalMemOffset = ((GlobalVarLocation) memoryLocation).getStaticMemAddrOffset();
            runtimeEnv.getStaticMemory().setValue(globalMemOffset, rValue);
        } else if (memoryLocation instanceof ConstantLocation) {
            // This is invoked only during the package.<init> function
            int constMemOffset = ((ConstantLocation) memoryLocation).getStaticMemAddrOffset();
            runtimeEnv.getStaticMemory().setValue(constMemOffset, rValue);
        }
    }

    /**
     * Initialize a user defined struct type.
     */
    @Override
    public BValue visit(StructInitExpr structInitExpr) {
        StructDef structDef = (StructDef) structInitExpr.getType();
        BValue[] structMemBlock;
        structMemBlock = new BValue[structDef.getStructMemorySize()];

        // Invoke the <init> function
        invokeStructInitFunction(structDef, structMemBlock);

        // iterate through initialized values and re-populate the memory block
        Expression[] argExprs = structInitExpr.getArgExprs();
        for (int i = 0; i < argExprs.length; i++) {
            KeyValueExpr expr = (KeyValueExpr) argExprs[i];
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
    public BValue visit(FieldAccessExpr fieldAccessExpr) {
        Expression varRef = fieldAccessExpr.getVarRef();
        BValue value = varRef.execute(this);

        if (value instanceof BArray) {
            FieldAccessExpr childFieldExpr = fieldAccessExpr.getFieldExpr();
            if (childFieldExpr != null && childFieldExpr.getVarRef() instanceof ArrayLengthExpression) {
                return new BInteger(((BArray) value).size());
            }
        }
        
        return getFieldExprValue(fieldAccessExpr, value);
    }

    @Override
    public BValue visit(JSONFieldAccessExpr jsonFieldExpr) {
        FieldAccessExpr varRefExpr = (FieldAccessExpr) jsonFieldExpr.getVarRef();
        Expression jsonVarRef = varRefExpr.getVarRef();
        BValue json = jsonVarRef.execute(this);

        return getJSONElementValue((BJSON) json, varRefExpr.getFieldExpr());
    }

    @Override
    public BValue visit(WorkerVarLocation workerVarLocation) {
        int offset = workerVarLocation.getworkerMemAddrOffset();
        return controlStack.getValue(offset);
    }

    /**
     * Assign a value to a field of a struct, represented by a {@link FieldAccessExpr}.
     *
     * @param rValue Value to be assigned
     * @param lExpr  {@link FieldAccessExpr} which represents the field of the struct
     */
    private void assignValueToFieldAccessExpr(BValue rValue, FieldAccessExpr lExpr) {
        Expression lExprVarRef = lExpr.getVarRef();

        if (lExprVarRef instanceof ArrayMapAccessExpr) {
            assignValueToArrayMapAccessExpr(rValue, (ArrayMapAccessExpr) lExprVarRef);
            return;
        }

        BValue value = lExprVarRef.execute(this);
        setFieldValue(rValue, lExpr, value);
    }

    /**
     * Recursively traverse and set the value of the access expression of a field of a struct.
     *
     * @param rValue Value to be set
     * @param currentExpr StructFieldAccessExpr of the current field
     * @param currentVal Value of the expression evaluated so far.
     */
    private void setFieldValue(BValue rValue, FieldAccessExpr currentExpr, BValue currentVal) {
        // currentVal is a unitValue or a array/map. hence get the element value of it.
        BValue unitVal = getUnitValue(currentVal, currentExpr);

        if (unitVal == null) {
            throw new BallerinaException("field '" + currentExpr.getSymbolName() + "' is null");
        }

        if (currentExpr.getRefVarType() == BTypes.typeJSON) {
            setJSONElementValue((BJSON) unitVal, currentExpr.getFieldExpr(), rValue);
            return;
        }

        BStruct currentStructVal = (BStruct) unitVal;
        FieldAccessExpr fieldExpr = (FieldAccessExpr) currentExpr.getFieldExpr();
        int fieldLocation = ((StructVarLocation) getMemoryLocation(fieldExpr)).getStructMemAddrOffset();

        if (fieldExpr.getFieldExpr() == null) {
            setStructFieldValue(rValue, currentStructVal, fieldLocation, fieldExpr);
            return;
        }

        // At this point, field of the field is not null. Means current element,
        // and its field are both complex (struct/map/json) types.
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
        if (expression instanceof FieldAccessExpr) {
            return getMemoryLocation(((FieldAccessExpr) expression).getVarRef());
        }

        // Set the memory location of the variable-reference-expression
        return ((VariableRefExpr) expression).getMemoryLocation();
    }

    /**
     * Set the unit value of the struct field.
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
    private void setStructFieldValue(BValue rValue, BStruct lExprValue, int memoryLocation,
                              FieldAccessExpr fieldExpr) {

        if (!(fieldExpr.getVarRef() instanceof ArrayMapAccessExpr)) {
            // If the lExprValue value is not a struct array/map, then set the value to the struct field
            lExprValue.setValue(memoryLocation, rValue);
            return;
        }

        ArrayMapAccessExpr varRef = (ArrayMapAccessExpr) fieldExpr.getVarRef();
        Expression[] indexExprs = varRef.getIndexExprs();

        // Get the arrays/map value from the mermory location
        BValue arrayMapValue = lExprValue.getValue(memoryLocation);
        if (arrayMapValue == null) {
            throw new BallerinaException("field '" + varRef.getSymbolName() + " is null");
        }

        // Set the value to arrays/map's index location

        if (varRef.getRExpr().getType() == BTypes.typeMap) {
            BValue indexValue = indexExprs[0].execute(this);
            ((BMap) arrayMapValue).put(indexValue, rValue);
        } else {
            BArray arrayVal = (BArray) arrayMapValue;
            if (indexExprs.length > 1) {
                arrayVal = retrieveArray(arrayVal, indexExprs);
            }

            BInteger indexVal = (BInteger) indexExprs[0].execute(this);
            arrayVal.add(indexVal.intValue(), rValue);
        }
    }

    /**
     * Recursively traverse and get the value of the access expression of a field.
     *
     * @param currentExpr FieldAccessExpr of the current field
     * @param currentVal Value of the expression evaluated so far.
     * @return Value of the expression after evaluating the current field.
     */
    private BValue getFieldExprValue(FieldAccessExpr currentExpr, BValue currentVal) {
        FieldAccessExpr fieldExpr = currentExpr.getFieldExpr();
        if (fieldExpr == null) {
            return currentVal;
        }

        if (currentExpr.getRefVarType() == BTypes.typeJSON) {
            return getJSONElementValue((BJSON) currentVal, fieldExpr);
        }

        // currentVal could be a value type or a array/map. Hence get the single element value of it.
        BValue unitVal = getUnitValue(currentVal, currentExpr);

        if (unitVal == null) {
            throw new BallerinaException("field '" + currentExpr.getVarName() + "' is null");
        }

        // if fieldExpr exist means this is a struct.
        BStruct currentStructVal = (BStruct) unitVal;

        int fieldLocation = ((StructVarLocation) getMemoryLocation(fieldExpr)).getStructMemAddrOffset();

        // If this is the last field, return the value from memory location
        FieldAccessExpr nestedFieldExpr = fieldExpr.getFieldExpr();
        if (nestedFieldExpr == null) {
            // Value stored in the struct can be also an array. Hence if its an array access,
            // get the array element value
            return getUnitValue(currentStructVal.getValue(fieldLocation), fieldExpr);
        }

        BValue value = currentStructVal.getValue(fieldLocation);
        if (value instanceof BArray && nestedFieldExpr.getVarRef() instanceof ArrayLengthExpression) {
            return new BInteger(((BArray) value).size());
        }

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
     * retrieves the unit value (either the complete arrays/map or the referenced element of an arrays/map),
     * using the index expression of the 'fieldExpr'.
     *
     * @param currentVal Value of the field expression evaluated so far
     * @param currentExpr  Field access expression for the current value
     * @return Unit value of the current value
     */
    private BValue getUnitValue(BValue currentVal, FieldAccessExpr currentExpr) {
        ReferenceExpr currentVarRefExpr = (ReferenceExpr) currentExpr.getVarRef();
        //if (currentVal == null) {
        //    throw new BallerinaException("field '" + generateErrorSymbolName(currentVarRefExpr.getSymbolName())
        //            + "' is null");
        //}


        if (!(currentVal instanceof BArray || currentVal instanceof BMap<?, ?>)) {
            return currentVal;
        }

        // If the lExprValue value is not a array/map, then the unit value is same as the struct
        Expression[] indexExprs;
        if (currentVarRefExpr instanceof ArrayMapAccessExpr) {
            indexExprs = ((ArrayMapAccessExpr) currentVarRefExpr).getIndexExprs();
        } else {
            return currentVal;
        }

        // Evaluate the index expression and get the value
        BValue indexValue;
        BValue unitVal;
        // Get the value from arrays/map's index location
        ArrayMapAccessExpr varRef = (ArrayMapAccessExpr) currentExpr.getVarRef();
        if (varRef.getRExpr().getType() == BTypes.typeMap) {
            indexValue = indexExprs[0].execute(this);
            unitVal = ((BMap) currentVal).get(indexValue);
        } else {
            BArray bArray = (BArray) currentVal;
            for (int i = indexExprs.length - 1; i >= 1; i--) {
                indexValue = indexExprs[i].execute(this);
                bArray = (BArray) bArray.get(((BInteger) indexValue).intValue());
            }
            indexValue = indexExprs[0].execute(this);
            unitVal = bArray.get(((BInteger) indexValue).intValue());
        }

        if (unitVal == null && currentExpr.getFieldExpr() != null) {
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

        // Setting return values to function invocation expression
        returnedOrReplied = false;
    }

    private void invokeConnectorInitAction(BallerinaConnectorDef connectorDef, BConnector bConnector) {
        Action action = connectorDef.getInitAction();
        if (action == null) {
            return;
        }

        BValue[] localVals = new BValue[1];
        localVals[0] = bConnector;

        BValue[] returnVals = new BValue[0];

        CallableUnitInfo functionInfo = new CallableUnitInfo(action.getName(), action.getPackagePath(),
                action.getNodeLocation());

        StackFrame stackFrame = new StackFrame(localVals, returnVals, functionInfo);
        controlStack.pushFrame(stackFrame);
        AbstractNativeAction nativeAction = (AbstractNativeAction) action;
        nativeAction.execute(bContext);
        controlStack.popFrame();

        // Setting return values to function invocation expression
        returnedOrReplied = false;
    }

    private BArray retrieveArray(BArray arrayVal, Expression[] exprs) {
        for (int i = exprs.length - 1; i >= 1; i--) {
            BInteger indexVal = (BInteger) exprs[i].execute(this);

            // TODO: Remove this part if we don't need dynamically create arrays
            // Will have to dynamically populate
//            while (arrayVal.size() <= indexVal.intValue()) {
//                if (i != 1 || rValue instanceof BArray) {
//                    BArray newBArray = new BArray<>(BArray.class);
//                    arrayVal.add(arrayVal.size(), newBArray);
//                } else {
//                    BArray bArray = new BArray<>(rValue.getClass());
//                    arrayVal.add(arrayVal.size(), bArray);
//                }
//            }

            arrayVal = (BArray) arrayVal.get(indexVal.intValue());
        }

        return arrayVal;
    }

//    public void shutdownWorker(Worker worker) {
//        Future<BMessage> future = worker.getResultFuture();
//        try {
//            // TODO: Make this value configurable - need grammar level rethink
//            BMessage result = future.get(60, TimeUnit.SECONDS);
//            executor.shutdown();
//            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
//                executor.shutdownNow();
//            }
//        } catch (Exception e) {
//            // If there is an exception in the worker, set an empty value to the return variable
//            BMessage result = BTypes.typeMessage.getDefaultValue();
//        } finally {
//            // Finally, try again to shutdown if not done already
//            executor.shutdownNow();
//        }
//    }

    public void executeWorker(Worker worker, Expression[] parentParameters) {
        int sizeOfValueArray = worker.getStackFrameSize();
        BValue[] localVals = new BValue[sizeOfValueArray];

        int valueCounter = 0;

        if (parentParameters != null) {
            for (Expression arg : parentParameters) {
                // Evaluate the argument expression
                BValue argValue = arg.execute(this);
                // Setting argument value in the stack frame
                localVals[valueCounter] = argValue;
                valueCounter++;
            }
        }

        for (ParameterDef returnParam : worker.getReturnParameters()) {
            // Check whether these are unnamed set of return types.
            // If so break the loop. You can't have a mix of unnamed and named returns parameters.
            if (returnParam.getName() == null) {
                break;
            }

            localVals[valueCounter] = returnParam.getType().getEmptyValue();
            valueCounter++;
        }


        // Create an arrays in the stack frame to hold return values;
        BValue[] returnVals = new BValue[1];

        // Create a new stack frame with memory locations to hold parameters, local values, temp expression value,
        // return values and worker invocation location;
        CallableUnitInfo functionInfo = new CallableUnitInfo(worker.getName(), worker.getPackagePath(),
                worker.getNodeLocation());

        StackFrame stackFrame = new StackFrame(localVals, returnVals, functionInfo);
        Context workerContext = new Context();
        workerContext.getControlStack().pushFrame(stackFrame);
        WorkerCallback workerCallback = new WorkerCallback(workerContext);
        workerContext.setBalCallback(workerCallback);
        BLangExecutor workerExecutor = new BLangExecutor(runtimeEnv, workerContext);
        //ExecutorService executor = Executors.newSingleThreadExecutor(new BLangThreadFactory(worker.getName()));
        ExecutorService executor = ThreadPoolFactory.getInstance().getWorkerExecutor();
        WorkerExecutor workerRunner = new WorkerExecutor(workerExecutor, workerContext, worker);
        executor.submit(workerRunner);
//        Future<BMessage> future = executor.submit(workerRunner);
//        worker.setResultFuture(future);

        // Start workers within the worker
        for (Worker worker1 : worker.getWorkers()) {
            executeWorker(worker1, null);
        }
    }

    /**
     * Invoke the init function of the struct. This will populate the default values for struct fields.
     * 
     * @param structDef Struct definition
     * @param structMemBlock Memory block to be assigned for the new struct instance
     */
    private void invokeStructInitFunction(StructDef structDef, BValue[] structMemBlock) {
        Function initFunction = structDef.getInitFunction();
        CallableUnitInfo functionInfo = new CallableUnitInfo(initFunction.getName(), initFunction.getPackagePath(),
            initFunction.getNodeLocation());
        StackFrame stackFrame = new StackFrame(structMemBlock, null, functionInfo);
        controlStack.pushFrame(stackFrame);
        initFunction.getCallableUnitBody().execute(this);
        controlStack.popFrame();

        // Setting return values to function invocation expression
        returnedOrReplied = false;
    }

    /**
     * Get the value of element from a given json object.
     *
     * @param json JSON to get the value
     * @param fieldExpr Field expression represent the element of the json to be extracted
     * @return value of the element represented by the field expression
     */
    private BValue getJSONElementValue(BJSON json, FieldAccessExpr fieldExpr) {
        if (fieldExpr == null) {
            return json;
        }

        BJSON jsonElement;
        BValue elementIndex = fieldExpr.getVarRef().execute(this);
        if (json == null) {
            throw new BallerinaException("cannot get '" + elementIndex.stringValue() + "' from null");
        }

        if (elementIndex.getType() == BTypes.typeInt) {
            jsonElement = JSONUtils.getArrayElement(json, ((BInteger) elementIndex).intValue());
        } else {
            jsonElement = JSONUtils.getElement(json, elementIndex.stringValue());
        }
        return getJSONElementValue(jsonElement, fieldExpr.getFieldExpr());
    }

    /**
     * Recursively traverse and set the value of the access expression of a field of a json.
     *
     * @param json JSON to set the value
     * @param fieldExpr Expression represents the field
     * @param rValue Value to be set
     */
    private void setJSONElementValue(BJSON json, FieldAccessExpr fieldExpr, BValue rValue) {
        BValue elementIndex = fieldExpr.getVarRef().execute(this);
        if (json == null) {
            throw new BallerinaException("cannot set '" + elementIndex.stringValue() + "' of null");
        }

        FieldAccessExpr childField = fieldExpr.getFieldExpr();
        BJSON jsonElement;

        if (childField == null) {
            if (elementIndex.getType() == BTypes.typeInt) {
                JSONUtils.setArrayElement(json, ((BInteger) elementIndex).intValue(), (BJSON) rValue);
            } else {
                JSONUtils.setElement(json, elementIndex.stringValue(), (BJSON) rValue);
            }
            return;
        }

        if (elementIndex.getType() == BTypes.typeInt) {
            jsonElement = JSONUtils.getArrayElement(json, ((BInteger) elementIndex).intValue());
        } else {
            jsonElement = JSONUtils.getElement(json, elementIndex.stringValue());
        }
        setJSONElementValue(jsonElement, childField, rValue);
    }

    private void handleError(TryCatchStmt.CatchBlock catchBlock, StackFrame tryCatchScope) {
        while (bContext.getControlStack().getCurrentFrame() != tryCatchScope) {
            if (controlStack.getStack().size() > 0) {
                controlStack.popFrame();
            } else {
                // Something wrong. This shouldn't execute.
                throw new BallerinaException("fatal : unexpected error occurred. No stack frame found.");
            }
        }
        // Assign Exception value.
        MemoryLocation memoryLocation = catchBlock.getParameterDef().getMemoryLocation();
        if (memoryLocation instanceof StackVarLocation) {
            int stackFrameOffset = ((StackVarLocation) memoryLocation).getStackFrameOffset();
            controlStack.setValue(stackFrameOffset, thrownError);
        }
        thrownError = null;
        isErrorThrown = false;
        // Invoke Catch Block.
        catchBlock.getCatchBlockStmt().execute(this);
    }

    private void createBErrorFromException(Throwable t) {
        if (error == null) {
            error = (StructDef) parentScope.resolve(new SymbolName("Error", "ballerina.lang.errors"));
            if (error == null) {
                throw new BLangRuntimeException("Unresolved type Error");
            }
        }
        BString msg = new BString(t.getMessage());
        thrownError = new BStruct(error, new BValue[]{msg});
        thrownError.setStackTrace(generateStackTrace());
        isErrorThrown = true;
        if (bContext.isInTransaction()) {
            bContext.getBallerinaTransactionManager().setTransactionError(true);
        }
    }
}

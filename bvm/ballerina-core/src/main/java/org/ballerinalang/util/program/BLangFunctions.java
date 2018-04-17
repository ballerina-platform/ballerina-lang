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
package org.ballerinalang.util.program;

import org.ballerinalang.bre.BLangCallableUnitCallback;
import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.NativeCallContext;
import org.ballerinalang.bre.bvm.AsyncInvocableWorkerResponseContext;
import org.ballerinalang.bre.bvm.AsyncTimer;
import org.ballerinalang.bre.bvm.BLangScheduler;
import org.ballerinalang.bre.bvm.BLangVMErrors;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.bre.bvm.CallableWorkerResponseContext;
import org.ballerinalang.bre.bvm.ForkJoinTimeoutCallback;
import org.ballerinalang.bre.bvm.ForkJoinWorkerResponseContext;
import org.ballerinalang.bre.bvm.InitWorkerResponseContext;
import org.ballerinalang.bre.bvm.SyncCallableWorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerData;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.bvm.WorkerResponseContext;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BCallableFuture;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.FunctionFlags;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.codegen.CallableUnitInfo.WorkerSet;
import org.ballerinalang.util.codegen.ForkjoinInfo;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.WorkerInfo;
import org.ballerinalang.util.codegen.attributes.CodeAttributeInfo;
import org.ballerinalang.util.exceptions.BLangNullReferenceException;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.observability.CallableUnitCallbackObserver;
import org.ballerinalang.util.observability.CallbackObserver;
import org.ballerinalang.util.observability.ObservabilityUtils;
import org.ballerinalang.util.observability.ObserverContext;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import static org.ballerinalang.util.observability.ObservabilityConstants.KEY_OBSERVER_CONTEXT;

/**
 * This class contains helper methods to invoke Ballerina functions.
 *
 * @since 0.8.0
 */
public class BLangFunctions {

    private static final String JOIN_TYPE_SOME = "some";

    private BLangFunctions() { }

    /**
     * This method calls a program callable, considering it as an entry point callable. Which means, this callable will 
     * be invoked as the first callable in a program, and after it is called, all the cleanup will be done for it to 
     * exit from the program. That is, this callable will wait for the response to be fully available, and it will wait
     * till all the workers in the system to finish executing.
     * @param bLangProgram the program file
     * @param packageName the package the callable is residing
     * @param callableName the callable name
     * @param args the callable arguments
     * @return
     */
    public static BValue[] invokeEntrypointCallable(ProgramFile bLangProgram, String packageName, String callableName,
                                     BValue[] args) {
        PackageInfo packageInfo = bLangProgram.getPackageInfo(packageName);
        FunctionInfo functionInfo = packageInfo.getFunctionInfo(callableName);
        if (functionInfo == null) {
            throw new RuntimeException("Function '" + callableName + "' is not defined");
        }
        return invokeEntrypointCallable(bLangProgram, packageInfo, functionInfo, args);
    }
    
    public static BValue[] invokeEntrypointCallable(ProgramFile programFile, PackageInfo packageInfo,
            FunctionInfo functionInfo, BValue[] args) {
        WorkerExecutionContext parentCtx = new WorkerExecutionContext(programFile);
        if (functionInfo.getParamTypes().length != args.length) {
            throw new RuntimeException("Size of input argument arrays is not equal to size of function parameters");
        }
        invokePackageInitFunction(packageInfo.getInitFunctionInfo(), parentCtx);
        invokeVMUtilFunction(packageInfo.getStartFunctionInfo(), parentCtx);
        BValue[] result = invokeCallable(functionInfo, parentCtx, args);
        BLangScheduler.waitForWorkerCompletion();
        return result;
    }
    
    public static void invokeCallable(CallableUnitInfo callableUnitInfo, WorkerExecutionContext parentCtx) {
        invokeCallable(callableUnitInfo, parentCtx, new int[0], new int[0], false);
    }
    
    public static BValue[] invokeCallable(CallableUnitInfo callableUnitInfo, BValue[] args) {
        return invokeCallable(callableUnitInfo, new WorkerExecutionContext(callableUnitInfo.getPackageInfo()
                .getProgramFile()), args);
    }
    
    public static BValue[] invokeCallable(CallableUnitInfo callableUnitInfo, WorkerExecutionContext parentCtx, 
            BValue[] args) {
        int[][] regs = BLangVMUtils.populateArgAndReturnData(parentCtx, callableUnitInfo, args);
        invokeCallable(callableUnitInfo, parentCtx, regs[0], regs[1], true);
        return BLangVMUtils.populateReturnData(parentCtx, callableUnitInfo, regs[1]);
    }
    
    public static void invokeServiceCallable(CallableUnitInfo callableUnitInfo, WorkerExecutionContext parentCtx,
                                             ObserverContext observerContext, BValue[] args,
                                             CallableUnitCallback responseCallback) {
        int[][] regs = BLangVMUtils.populateArgAndReturnData(parentCtx, callableUnitInfo, args);
        invokeServiceCallable(callableUnitInfo, parentCtx, observerContext, regs[0], regs[1], responseCallback);
    }

    /**
     * This method does not short circuit the execution of the first worker to execute in the
     * same calling thread, but rather executes all the workers in their own separate threads.
     * This is specifically useful in executing service resources, where the calling transport
     * threads shouldn't be blocked, but rather the worker threads should be used.
     */
    public static void invokeServiceCallable(CallableUnitInfo callableUnitInfo, WorkerExecutionContext parentCtx,
                                             ObserverContext observerContext, int[] argRegs, int[] retRegs,
                                             CallableUnitCallback responseCallback) {
        WorkerSet workerSet = callableUnitInfo.getWorkerSet();
        int generalWorkersCount = workerSet.generalWorkers.length;
        CallableWorkerResponseContext respCtx = createWorkerResponseContext(callableUnitInfo.getRetParamTypes(),
                generalWorkersCount);
        respCtx.registerResponseCallback(responseCallback);
        respCtx.joinTargetContextInfo(parentCtx, retRegs);
        WorkerDataIndex wdi = callableUnitInfo.retWorkerIndex;
        checkAndObserveServiceCallable(parentCtx, observerContext, respCtx);

        /* execute the init worker and extract the local variables created by it */
        WorkerData initWorkerLocalData = null;
        CodeAttributeInfo initWorkerCAI = null;
        if (workerSet.initWorker != null) {
            initWorkerLocalData = executeInitWorker(parentCtx, argRegs, callableUnitInfo, workerSet.initWorker, wdi,
                    observerContext);
            if (initWorkerLocalData == null) {
                handleError(parentCtx);
                return;
            }
            initWorkerCAI = workerSet.initWorker.getCodeAttributeInfo();
        }

        for (int i = 0; i < workerSet.generalWorkers.length; i++) {
            executeWorker(respCtx, parentCtx, argRegs, callableUnitInfo, workerSet.generalWorkers[i],
                    wdi, initWorkerLocalData, initWorkerCAI, false, observerContext);
        }
    }
    
    public static WorkerExecutionContext invokeCallable(CallableUnitInfo callableUnitInfo,
            WorkerExecutionContext parentCtx, int[] argRegs, int[] retRegs, boolean waitForResponse) {
        return invokeCallable(callableUnitInfo, parentCtx, argRegs, retRegs, waitForResponse, FunctionFlags.NOTHING);
    }

    public static WorkerExecutionContext invokeCallable(CallableUnitInfo callableUnitInfo,
            WorkerExecutionContext parentCtx, int[] argRegs, int[] retRegs, boolean waitForResponse,
            int flags) {
        BLangScheduler.workerWaitForResponse(parentCtx);
        WorkerExecutionContext resultCtx;
        if (callableUnitInfo.isNative()) {
            if (FunctionFlags.isAsync(flags)) {
                invokeNativeCallableAsync(callableUnitInfo, parentCtx, argRegs, retRegs, flags);
                resultCtx = parentCtx;
            } else {
                resultCtx = invokeNativeCallable(callableUnitInfo, parentCtx, argRegs, retRegs, flags);
            }
        } else {
            if (FunctionFlags.isAsync(flags)) {
                invokeNonNativeCallableAsync(callableUnitInfo, parentCtx, argRegs, retRegs, flags);
                resultCtx = parentCtx;
            } else {
                resultCtx = invokeNonNativeCallable(callableUnitInfo, parentCtx, argRegs, retRegs, waitForResponse,
                        flags);
            }
        }
        resultCtx = BLangScheduler.resume(resultCtx, true);
        return resultCtx;
    }

    private static CallableWorkerResponseContext createWorkerResponseContext(BType[] retParamTypes,
            int generalWorkersCount) {
        if (generalWorkersCount == 1) {
            return new CallableWorkerResponseContext(retParamTypes, generalWorkersCount);
        } else {
            return new SyncCallableWorkerResponseContext(retParamTypes, generalWorkersCount);
        }
    }

    public static WorkerExecutionContext invokeNonNativeCallable(CallableUnitInfo callableUnitInfo,
            WorkerExecutionContext parentCtx, int[] argRegs, int[] retRegs, boolean waitForResponse, int flags) {
        WorkerSet workerSet = callableUnitInfo.getWorkerSet();
        int generalWorkersCount = workerSet.generalWorkers.length;
        CallableWorkerResponseContext respCtx = createWorkerResponseContext(callableUnitInfo.getRetParamTypes(),
                generalWorkersCount);
        WaitForResponseCallback respCallback = null;
        if (waitForResponse) {
            respCallback = new WaitForResponseCallback();
            respCtx.registerResponseCallback(respCallback);
        }
        ObserverContext observerContext = checkAndObserveNonNativeCallable(parentCtx, respCtx, callableUnitInfo, flags);
        respCtx.joinTargetContextInfo(parentCtx, retRegs);
        WorkerDataIndex wdi = callableUnitInfo.retWorkerIndex;

        /* execute the init worker and extract the local variables created by it */
        WorkerData initWorkerLocalData = null;
        CodeAttributeInfo initWorkerCAI = null;
        if (workerSet.initWorker != null) {
            initWorkerLocalData = executeInitWorker(parentCtx, argRegs, callableUnitInfo, workerSet.initWorker, wdi,
                    observerContext);
            if (initWorkerLocalData == null) {
                handleError(parentCtx);
                return null;
            }
            initWorkerCAI = workerSet.initWorker.getCodeAttributeInfo();
        }

        for (int i = 1; i < generalWorkersCount; i++) {
            executeWorker(respCtx, parentCtx, argRegs, callableUnitInfo, workerSet.generalWorkers[i],
                    wdi, initWorkerLocalData, initWorkerCAI, false, observerContext);
        }
        WorkerExecutionContext runInCallerCtx = executeWorker(respCtx, parentCtx, argRegs, callableUnitInfo, 
                workerSet.generalWorkers[0], wdi, initWorkerLocalData, initWorkerCAI, true, observerContext);
        if (waitForResponse) {
            BLangScheduler.executeNow(runInCallerCtx);
            respCallback.waitForResponse();
            // An error in the context at this point means an unhandled runtime error has propagated
            // all the way up to the entry point. Hence throw a {@link BLangRuntimeException} and
            // terminate the execution.
            BStruct error = parentCtx.getError();
            if (error != null) {
                handleError(parentCtx);
            }
            return null;
        } else {
            return runInCallerCtx;
        }
    }
    
    public static void invokeNonNativeCallableAsync(CallableUnitInfo callableUnitInfo,
            WorkerExecutionContext parentCtx, int[] argRegs, int[] retRegs, int flags) {
        WorkerSet workerSet = callableUnitInfo.getWorkerSet();
        int generalWorkersCount = workerSet.generalWorkers.length;
        AsyncInvocableWorkerResponseContext respCtx = new AsyncInvocableWorkerResponseContext(callableUnitInfo,
                generalWorkersCount);
        WorkerDataIndex wdi = callableUnitInfo.retWorkerIndex;
        ObserverContext observerContext = checkAndObserveNonNativeCallable(parentCtx, respCtx, callableUnitInfo, flags);
        /* execute the init worker and extract the local variables created by it */
        WorkerData initWorkerLocalData = null;
        CodeAttributeInfo initWorkerCAI = null;
        if (workerSet.initWorker != null) {
            initWorkerLocalData = executeInitWorker(parentCtx, argRegs, callableUnitInfo, workerSet.initWorker, wdi,
                    observerContext);
            if (initWorkerLocalData == null) {
                handleError(parentCtx);
                return;
            }
            initWorkerCAI = workerSet.initWorker.getCodeAttributeInfo();
        }

        List<WorkerExecutionContext> workerExecutionContexts = new ArrayList<>();
        /* execute all the workers in their own threads */
        for (int i = 0; i < generalWorkersCount; i++) {
            workerExecutionContexts.add(executeWorker(respCtx, parentCtx, argRegs, callableUnitInfo,
                    workerSet.generalWorkers[i], wdi, initWorkerLocalData, initWorkerCAI, false,
                    observerContext));
        }
        /* set the worker execution contexts in the response context, so it can use them to do later
         * operations such as cancel */
        respCtx.setWorkerExecutionContexts(workerExecutionContexts);
        /* create the future encapsulating the worker response context, and set it as the return value
         * to the parent */
        BLangVMUtils.populateWorkerDataWithValues(parentCtx.workerLocal, retRegs,
                new BValue[] { new BCallableFuture(callableUnitInfo.getName(), respCtx) },
                new BType[] { BTypes.typeFuture });
        return;
    }

    private static WorkerExecutionContext invokeNativeCallable(CallableUnitInfo callableUnitInfo,
            WorkerExecutionContext parentCtx, int[] argRegs, int[] retRegs, int flags) {
        WorkerData parentLocalData = parentCtx.workerLocal;
        BType[] retTypes = callableUnitInfo.getRetParamTypes();
        WorkerData caleeSF = BLangVMUtils.createWorkerDataForLocal(callableUnitInfo.getDefaultWorkerInfo(), parentCtx,
                argRegs, callableUnitInfo.getParamTypes());
        Context ctx = new NativeCallContext(parentCtx, callableUnitInfo, caleeSF);
        NativeCallableUnit nativeCallable = callableUnitInfo.getNativeCallableUnit();        
        if (nativeCallable == null) {
            return parentCtx;
        }
        try {
            ObserverContext observerContext = checkAndStartNativeCallableObservation(ctx, callableUnitInfo, flags);
            if (nativeCallable.isBlocking()) {
                nativeCallable.execute(ctx, null);
                BLangVMUtils.populateWorkerDataWithValues(parentLocalData, retRegs, ctx.getReturnValues(), retTypes);
                checkAndStopCallableObservation(observerContext, flags);
                /* we want the parent to continue, since we got the response of the native call already */
                return parentCtx;
            } else {
                CallableUnitCallback callback = getNativeCallableUnitCallback(parentCtx, ctx, observerContext,
                        retRegs, retTypes, flags);
                nativeCallable.execute(ctx, callback);
                /* we want the parent to suspend (i.e. go to wait for response state) and stay until notified */
                return null;
            }
        } catch (BLangNullReferenceException e) {
            return BLangVMUtils.handleNativeInvocationError(parentCtx,
                    BLangVMErrors.createNullRefException(callableUnitInfo));
        } catch (Throwable e) {
            return BLangVMUtils.handleNativeInvocationError(parentCtx,
                    BLangVMErrors.createError(callableUnitInfo, e.getMessage()));
        }
    }

    private static void invokeNativeCallableAsync(CallableUnitInfo callableUnitInfo,
            WorkerExecutionContext parentCtx, int[] argRegs, int[] retRegs, int flags) {
        WorkerData caleeSF = BLangVMUtils.createWorkerDataForLocal(callableUnitInfo.getDefaultWorkerInfo(), parentCtx,
                argRegs, callableUnitInfo.getParamTypes());
        Context nativeCtx = new NativeCallContext(parentCtx, callableUnitInfo, caleeSF);
        NativeCallableUnit nativeCallable = callableUnitInfo.getNativeCallableUnit();
        if (nativeCallable == null) {
            return;
        }
        AsyncInvocableWorkerResponseContext respCtx;
        if (nativeCallable.isBlocking()) {
            respCtx = BLangScheduler.executeBlockingNativeAsync(nativeCallable, nativeCtx, flags);
        } else {
            respCtx = BLangScheduler.executeNonBlockingNativeAsync(nativeCallable, nativeCtx, flags);
        }
        BLangVMUtils.populateWorkerDataWithValues(parentCtx.workerLocal, retRegs,
                new BValue[] { new BCallableFuture(callableUnitInfo.getName(), respCtx) },
                new BType[] { BTypes.typeFuture });
    }
    
    private static void handleError(WorkerExecutionContext ctx) {
        throw new BLangRuntimeException("error: " + BLangVMErrors.getPrintableStackTrace(ctx.getError()));
    }
    
    private static WorkerExecutionContext executeWorker(WorkerResponseContext respCtx, 
            WorkerExecutionContext parentCtx, int[] argRegs, CallableUnitInfo callableUnitInfo, 
            WorkerInfo workerInfo, WorkerDataIndex wdi, WorkerData initWorkerLocalData, 
            CodeAttributeInfo initWorkerCAI, boolean runInCaller, ObserverContext observerContext) {
        WorkerData workerLocal = BLangVMUtils.createWorkerDataForLocal(workerInfo, parentCtx, argRegs,
                callableUnitInfo.getParamTypes());
        if (initWorkerLocalData != null) {
            BLangVMUtils.mergeInitWorkertData(initWorkerLocalData, workerLocal, initWorkerCAI);
        }
        WorkerData workerResult = BLangVMUtils.createWorkerData(wdi);
        WorkerExecutionContext ctx = new WorkerExecutionContext(parentCtx, respCtx, callableUnitInfo, workerInfo,
                workerLocal, workerResult, wdi.retRegs, runInCaller);
        setObserverContextToWorkerExecutionContext(ctx, observerContext);
        BLangScheduler.schedule(ctx);
        return ctx;
    }
    
    private static WorkerData executeInitWorker(WorkerExecutionContext parentCtx, int[] argRegs,
            CallableUnitInfo callableUnitInfo, WorkerInfo workerInfo, WorkerDataIndex wdi,
            ObserverContext observerContext) {
        InitWorkerResponseContext respCtx = new InitWorkerResponseContext(parentCtx);
        WorkerExecutionContext ctx = executeWorker(respCtx, parentCtx, argRegs, callableUnitInfo,
                workerInfo, wdi, null, null, true, observerContext);
        BLangScheduler.executeNow(ctx);
        WorkerData workerLocal = ctx.workerLocal;
        if (respCtx.isErrored()) {
            return null;
        } else {
            return workerLocal;
        }
    }
    
    public static void invokePackageInitFunction(FunctionInfo initFuncInfo, WorkerExecutionContext context) {
        invokeCallable(initFuncInfo, context, new int[0], new int[0], true);
        if (context.getError() != null) {
            String stackTraceStr = BLangVMErrors.getPrintableStackTrace(context.getError());
            throw new BLangRuntimeException("error: " + stackTraceStr);
        }
    }

    public static void invokePackageInitFunction(FunctionInfo initFuncInfo) {
        WorkerExecutionContext context = new WorkerExecutionContext(initFuncInfo.getPackageInfo().getProgramFile());
        invokePackageInitFunction(initFuncInfo, context);
    }

    public static void invokeVMUtilFunction(FunctionInfo utilFuncInfo, WorkerExecutionContext context) {
        invokeCallable(utilFuncInfo, context, new int[0], new int[0], true);
        if (context.getError() != null) {
            String stackTraceStr = BLangVMErrors.getPrintableStackTrace(context.getError());
            throw new BLangRuntimeException("error: " + stackTraceStr);
        }
    }

    public static void invokeVMUtilFunction(FunctionInfo initFuncInfo) {
        WorkerExecutionContext context = new WorkerExecutionContext(initFuncInfo.getPackageInfo().getProgramFile());
        invokeVMUtilFunction(initFuncInfo, context);
    }

    public static void invokeServiceInitFunction(FunctionInfo initFuncInfo) {
        WorkerExecutionContext context = new WorkerExecutionContext(initFuncInfo.getPackageInfo().getProgramFile());
        invokeCallable(initFuncInfo, context, new int[0], new int[0], true);
        if (context.getError() != null) {
            String stackTraceStr = BLangVMErrors.getPrintableStackTrace(context.getError());
            throw new BLangRuntimeException("error: " + stackTraceStr);
        }
    }

    public static WorkerExecutionContext invokeForkJoin(WorkerExecutionContext parentCtx, ForkjoinInfo forkjoinInfo,
            int joinTargetIp, int joinVarReg, int timeoutRegIndex, int timeoutTargetIp, int timeoutVarReg) {
        WorkerInfo[] workerInfos = forkjoinInfo.getWorkerInfos();

        Set<String> joinWorkerNames = new LinkedHashSet<>(Lists.of(forkjoinInfo.getJoinWorkerNames()));
        if (joinWorkerNames.isEmpty()) {
            /* if no join workers are specified, that means, all should be considered */
            joinWorkerNames.addAll(forkjoinInfo.getWorkerInfoMap().keySet());
        }

        Map<String, String> channels = getChannels(forkjoinInfo);

        int reqJoinCount;
        if (forkjoinInfo.getJoinType().equalsIgnoreCase(JOIN_TYPE_SOME)) {
            reqJoinCount = forkjoinInfo.getWorkerCount();
        } else {
            reqJoinCount = joinWorkerNames.size();
        }

        SyncCallableWorkerResponseContext respCtx = new ForkJoinWorkerResponseContext(parentCtx, joinTargetIp,
                joinVarReg, timeoutTargetIp, timeoutVarReg, workerInfos.length, reqJoinCount, 
                joinWorkerNames, channels);

        if (forkjoinInfo.isTimeoutAvailable()) {
            long timeout = parentCtx.workerLocal.longRegs[timeoutRegIndex];
            //fork join timeout is in seconds, hence converting to milliseconds
            AsyncTimer.schedule(new ForkJoinTimeoutCallback(respCtx), timeout * 1000);
        }
        Map<String, Object> globalProps = parentCtx.globalProps;
        BLangScheduler.workerWaitForResponse(parentCtx);
        for (int i = 1; i < workerInfos.length; i++) {
            executeWorker(respCtx, parentCtx, forkjoinInfo.getArgRegs(), workerInfos[i], globalProps, false);
        }

        return executeWorker(respCtx, parentCtx, forkjoinInfo.getArgRegs(),
                workerInfos[0], globalProps, true);
    }

    private static WorkerExecutionContext executeWorker(WorkerResponseContext respCtx,
            WorkerExecutionContext parentCtx, int[] argRegs, WorkerInfo workerInfo,
            Map<String, Object> globalProps, boolean runInCaller) {
        WorkerData workerLocal = BLangVMUtils.createWorkerDataForLocal(workerInfo, parentCtx, argRegs);
        WorkerExecutionContext ctx = new WorkerExecutionContext(parentCtx, respCtx, parentCtx.callableUnitInfo,
                workerInfo, workerLocal, runInCaller);
        return BLangScheduler.schedule(ctx);
    }

    private static Map<String, String> getChannels(ForkjoinInfo forkjoinInfo) {
        Map<String, String> channels = new HashMap<>();
        forkjoinInfo.getWorkerInfoMap().forEach((k, v) -> channels.put(k, v.getWorkerDataChannelInfoForForkJoin()
                != null ? v.getWorkerDataChannelInfoForForkJoin().getChannelName() : null));
        return channels;
    }

    private static void checkAndObserveServiceCallable(WorkerExecutionContext parentCtx,
                                                       ObserverContext observerContext,
                                                       CallableWorkerResponseContext respCtx) {
        observerContext = (observerContext != null) ? observerContext : new ObserverContext();
        respCtx.registerResponseCallback(new CallbackObserver(observerContext));
        ObservabilityUtils.continueServerObservation(observerContext, parentCtx);
    }

    private static ObserverContext checkAndObserveNonNativeCallable(WorkerExecutionContext parentCtx,
                                                                    CallableWorkerResponseContext respCtx,
                                                                    CallableUnitInfo callableUnitInfo, int flags) {
        if (FunctionFlags.isObserved(flags)) {
            ObserverContext observerContext = startCallableObservation(parentCtx, callableUnitInfo);
            respCtx.registerResponseCallback(new CallbackObserver(observerContext));
            return observerContext;
        }
        return null;
    }

    private static void checkAndStopCallableObservation(ObserverContext ctx, int flags) {
        if (FunctionFlags.isObserved(flags)) {
            ObservabilityUtils.stopObservation(ctx);
        }
    }

    private static ObserverContext checkAndStartNativeCallableObservation(Context ctx,
                                                                          CallableUnitInfo callableUnitInfo,
                                                                          int flags) {
        if (FunctionFlags.isObserved(flags)) {
            ObserverContext observerContext = startCallableObservation(ctx.getParentWorkerExecutionContext(),
                    callableUnitInfo);
            WorkerExecutionContext workerExecutionContext = ctx.getParentWorkerExecutionContext();
            if (workerExecutionContext.localProps == null) {
                workerExecutionContext.localProps = new HashMap<>();
            }
            workerExecutionContext.localProps.put(KEY_OBSERVER_CONTEXT, observerContext);
            return observerContext;
        }
        return null;
    }

    private static CallableUnitCallback getNativeCallableUnitCallback(WorkerExecutionContext parentCtx, Context ctx,
                                                                      ObserverContext observerContext, int[] retRegs,
                                                                      BType[] retTypes, int flags) {
        if (FunctionFlags.isObserved(flags)) {
            return new CallableUnitCallbackObserver(observerContext, new BLangCallableUnitCallback(ctx, parentCtx,
                    retRegs, retTypes));
        } else {
            return new BLangCallableUnitCallback(ctx, parentCtx, retRegs, retTypes);
        }
    }

    private static ObserverContext startCallableObservation(WorkerExecutionContext parentCtx,
                                                            CallableUnitInfo callableUnitInfo) {
        return ObservabilityUtils.startClientObservation(callableUnitInfo.attachedToType.toString(),
                callableUnitInfo.getName(), parentCtx);
    }

    private static void setObserverContextToWorkerExecutionContext(WorkerExecutionContext workerExecutionContext,
                                                                   ObserverContext observerContext) {
        if (workerExecutionContext.localProps == null) {
            workerExecutionContext.localProps = new HashMap<>();
        }
        workerExecutionContext.localProps.put(KEY_OBSERVER_CONTEXT, observerContext);
    }
    
    /**
     * Callback handler to check for callable response availability.
     */
    private static class WaitForResponseCallback implements CallableUnitCallback {

        private Semaphore check = new Semaphore(0);
        
        @Override
        public void notifySuccess() {
            this.check.release();
        }

        @Override
        public void notifyFailure(BStruct error) {
            this.check.release();
        }
        
        public void waitForResponse() {
            try {
                this.check.acquire();
            } catch (InterruptedException ignore) { /* ignore */ }
        }
        
    }

}

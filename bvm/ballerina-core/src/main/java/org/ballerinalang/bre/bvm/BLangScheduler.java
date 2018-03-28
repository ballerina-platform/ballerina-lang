/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bre.bvm;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.CPU.HandleErrorException;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.exceptions.BLangNullReferenceException;
import org.ballerinalang.util.program.BLangVMUtils;

import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This represents the Ballerina worker scheduling functionality. 
 * 
 * @since 0.965.0
 */
public class BLangScheduler {
    
    private static AtomicInteger workerCount = new AtomicInteger(0);
    
    private static Semaphore workersDoneSemaphore = new Semaphore(1);
    
    public static WorkerExecutionContext schedule(WorkerExecutionContext ctx) {
        return schedule(ctx, ctx.runInCaller);
    }
    
    /**
     * This method executes a scheduled worker execution context which is in
     * CREATED state, which means, it is not yet ready for executing. This
     * method will immediately execute the worker in the current thread. This
     * method must be called after a call to the 
     * BLangScheduler#schedule(WorkerExecutionContext method.
     * 
     * @param ctx the worker execution context
     */
    public static void executeNow(WorkerExecutionContext ctx) {
        CPU.exec(ctx);
    }
    
    private static void workerCountUp() {
        int count = workerCount.incrementAndGet();
        if (count == 1) {
            try {
                workersDoneSemaphore.acquire();
            } catch (InterruptedException ignore) { /* ignore */ }
        }
    }
    
    private static void workerCountDown() {
        int count = workerCount.decrementAndGet();
        if (count <= 0) {
            workersDoneSemaphore.release();
        }
    }
    
    public static WorkerExecutionContext schedule(WorkerExecutionContext ctx, boolean runInCaller) {
        ctx.state = WorkerState.READY;
        workerCountUp();
        if (runInCaller) {
            return ctx;
        } else {
            ExecutorService executor = ThreadPoolFactory.getInstance().getWorkerExecutor();
            executor.submit(new WorkerExecutor(ctx));
            return null;
        }
    }
    
    public static WorkerExecutionContext resume(WorkerExecutionContext ctx) {
        return resume(ctx, false);
    }
    
    public static WorkerExecutionContext resume(WorkerExecutionContext ctx, boolean runInCaller) {
        if (ctx == null) {
            return null;
        }
        ctx.state = WorkerState.READY;
        if (runInCaller) {
            return ctx;
        } else {
            ThreadPoolFactory.getInstance().getWorkerExecutor().submit(new WorkerExecutor(ctx));
            return null;
        }
    }

    public static WorkerExecutionContext resume(WorkerExecutionContext ctx, int targetIp, boolean runInCaller) {
        ctx.ip = targetIp;
        return resume(ctx, runInCaller);
    }
    
    public static WorkerExecutionContext errorThrown(WorkerExecutionContext ctx, BStruct error) {
        ctx.setError(error);
        if (!ctx.isRootContext()) {
            try {
                CPU.handleError(ctx);
                return ctx;
            } catch (HandleErrorException e) {
                return e.ctx;
            }
        } else {
            return null;
        }
    }
    
    public static void stopWorker(WorkerExecutionContext ctx) {
        ctx.stop = true;
    }
    
    public static void workerDone(WorkerExecutionContext ctx) {
        ctx.state = WorkerState.DONE;
        workerCountDown();
    }

    public static void workerPaused(WorkerExecutionContext ctx) {
        ctx.state = WorkerState.PAUSED;
    }
    
    public static void switchToWaitForResponse(WorkerExecutionContext ctx) {
        ctx.state = WorkerState.WAITING_FOR_RESPONSE;
    }

    public static void workerWaitForLock(WorkerExecutionContext ctx) {
        ctx.state = WorkerState.WAITING_FOR_LOCK;
    }
    
    public static void waitForWorkerCompletion() {
        try {
            workersDoneSemaphore.acquire();
            workersDoneSemaphore.release();
        } catch (InterruptedException ignore) { /* ignore */ }
    }
    
    public static void workerExcepted(WorkerExecutionContext ctx) {
        ctx.state = WorkerState.EXCEPTED;
        workerCountDown();
    }
    
    public static void dumpCallStack(WorkerExecutionContext ctx) {
        PrintStream out = System.out;
        while (ctx != null && ctx.code != null) {
            out.println(ctx.callableUnitInfo.getPkgPath() + "." + ctx.callableUnitInfo.getName() + "[worker="
                    + ctx.workerInfo.getWorkerName() + "][state=" + ctx.state + "]");
            ctx = ctx.parent;
        }
    }
    
    public static AsyncInvocableWorkerResponseContext executeBlockingNativeAsync(NativeCallableUnit nativeCallable, 
            Context nativeCtx) {
        CallableUnitInfo callableUnitInfo = nativeCtx.getCallableUnitInfo();
        AsyncInvocableWorkerResponseContext respCtx = new AsyncInvocableWorkerResponseContext(callableUnitInfo);
        NativeCallExecutor exec = new NativeCallExecutor(nativeCallable, nativeCtx, respCtx);
        ThreadPoolFactory.getInstance().getWorkerExecutor().submit(exec);
        return respCtx;
    }
    
    public static AsyncInvocableWorkerResponseContext executeNonBlockingNativeAsync(NativeCallableUnit nativeCallable,
            Context nativeCtx) {
        CallableUnitInfo callableUnitInfo = nativeCtx.getCallableUnitInfo();
        AsyncInvocableWorkerResponseContext respCtx = new AsyncInvocableWorkerResponseContext(callableUnitInfo);
        BLangAsyncCallableUnitCallback callback = new BLangAsyncCallableUnitCallback(respCtx, nativeCtx);
        nativeCallable.execute(nativeCtx, callback);
        return respCtx;
    }
    
    /**
     * This represents the thread used to execute a runnable worker.
     */
    private static class WorkerExecutor implements Runnable {

        private WorkerExecutionContext ctx;

        public WorkerExecutor(WorkerExecutionContext ctx) {
            this.ctx = ctx;
        }
        
        @Override
        public void run() {
            CPU.exec(this.ctx);
        }
        
    }
    
    /**
     * This represents the thread used to run a blocking native call in async mode.
     */
    private static class NativeCallExecutor implements Runnable {

        private NativeCallableUnit nativeCallable;
        
        private Context nativeCtx;
        
        private WorkerResponseContext respCtx;
        
        public NativeCallExecutor(NativeCallableUnit nativeCallable, Context nativeCtx, 
                WorkerResponseContext respCtx) {
            this.nativeCallable = nativeCallable;
            this.nativeCtx = nativeCtx;
            this.respCtx = respCtx;
            /* worker count needs to be incremented, since this represents a new execution, similar to
             * scheduling a worker execution context. Afterwards, we should not call workerCountDown,
             * since it will be automatically be called by the signals sent to response context */
            workerCountUp();
        }
        
        @Override
        public void run() {
            WorkerExecutionContext runInCaller = null;
            CallableUnitInfo cui = this.nativeCtx.getCallableUnitInfo();
            WorkerData result = BLangVMUtils.createWorkerData(cui.retWorkerIndex);
            BType[] retTypes = cui.getRetParamTypes();
            try {
                this.nativeCallable.execute(this.nativeCtx, null);
                BLangVMUtils.populateWorkerResultWithValues(result, this.nativeCtx.getReturnValues(), retTypes);
                runInCaller = this.respCtx.signal(new WorkerSignal(null, SignalType.RETURN, result));
            } catch (BLangNullReferenceException e) {
                BStruct error = BLangVMErrors.createNullRefException(this.nativeCtx.getCallableUnitInfo());
                runInCaller = this.respCtx.signal(new WorkerSignal(new WorkerExecutionContext(error), 
                        SignalType.ERROR, result));
            } catch (Throwable e) {
                BStruct error = BLangVMErrors.createError(this.nativeCtx.getCallableUnitInfo(), e.getMessage());
                runInCaller = this.respCtx.signal(new WorkerSignal(new WorkerExecutionContext(error), 
                        SignalType.ERROR, result));
            } finally {
                workerCountDown();
            }
            executeNow(runInCaller);
        }
        
    }
    
    /**
     * This class represents the callback functionality for async non-blocking native calls.
     */
    public static class BLangAsyncCallableUnitCallback implements CallableUnitCallback {

        private WorkerResponseContext respCtx;
        
        private Context nativeCallCtx;

        public BLangAsyncCallableUnitCallback(WorkerResponseContext respCtx, Context nativeCallCtx) {
            this.respCtx = respCtx;
            this.nativeCallCtx = nativeCallCtx;
            workerCountUp();
        }
        
        @Override
        public synchronized void notifySuccess() {
            CallableUnitInfo cui = this.nativeCallCtx.getCallableUnitInfo();
            WorkerData result = BLangVMUtils.createWorkerData(cui.retWorkerIndex);
            BType[] retTypes = cui.getRetParamTypes();
            BLangVMUtils.populateWorkerResultWithValues(result, this.nativeCallCtx.getReturnValues(), retTypes);
            WorkerExecutionContext ctx = this.respCtx.signal(new WorkerSignal(null, SignalType.RETURN, result));
            workerCountDown();
            BLangScheduler.resume(ctx);
        }

        @Override
        public synchronized void notifyFailure(BStruct error) {
            CallableUnitInfo cui = this.nativeCallCtx.getCallableUnitInfo();
            WorkerData result = BLangVMUtils.createWorkerData(cui.retWorkerIndex);
            BType[] retTypes = cui.getRetParamTypes();
            BLangVMUtils.populateWorkerResultWithValues(result, this.nativeCallCtx.getReturnValues(), retTypes);
            WorkerExecutionContext ctx = this.respCtx.signal(new WorkerSignal(
                    new WorkerExecutionContext(error), SignalType.ERROR, result));
            workerCountDown();
            BLangScheduler.resume(ctx);
        }

    }
    
}

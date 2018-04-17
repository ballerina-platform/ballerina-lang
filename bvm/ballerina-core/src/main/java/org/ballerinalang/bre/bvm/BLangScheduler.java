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
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.model.NativeCallableUnit;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;
import org.ballerinalang.util.FunctionFlags;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.exceptions.BLangNullReferenceException;
import org.ballerinalang.util.observability.CallbackObserver;
import org.ballerinalang.util.observability.ObservabilityUtils;
import org.ballerinalang.util.program.BLangVMUtils;

import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * This represents the Ballerina worker scheduling functionality. 
 * 
 * @since 0.965.0
 */
public class BLangScheduler {
    
    private static final String SCHEDULER_STATS_CONFIG_PROP = "b7a.runtime.scheduler.statistics";

    private static AtomicInteger workerCount = new AtomicInteger(0);
    
    private static Semaphore workersDoneSemaphore = new Semaphore(1);
    
    private static SchedulerStats schedulerStats = new SchedulerStats();
    
    private static boolean schedulerStatsEnabled;
    
    static {
        String statsConfigProp = ConfigRegistry.getInstance().getAsString(SCHEDULER_STATS_CONFIG_PROP);
        if (statsConfigProp != null) {
            schedulerStatsEnabled = Boolean.parseBoolean(statsConfigProp);
        }
    }
    
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
    
    public static void workerCountUp() {
        int count = workerCount.incrementAndGet();
        if (count == 1) {
            try {
                workersDoneSemaphore.acquire();
            } catch (InterruptedException ignore) { /* ignore */ }
        }
    }
    
    public static void workerCountDown() {
        int count = workerCount.decrementAndGet();
        if (count <= 0) {
            workersDoneSemaphore.release();
        }
    }
    
    public static WorkerExecutionContext schedule(WorkerExecutionContext ctx, boolean runInCaller) {
        workerReady(ctx);
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
        workerReady(ctx);
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
        schedulerStats.stateTransition(ctx, WorkerState.DONE);
        ctx.state = WorkerState.DONE;
        workerCountDown();
    }
    
    public static void workerReady(WorkerExecutionContext ctx) {
        schedulerStats.stateTransition(ctx, WorkerState.READY);
        ctx.state = WorkerState.READY;
    }

    public static void workerPaused(WorkerExecutionContext ctx) {
        schedulerStats.stateTransition(ctx, WorkerState.PAUSED);
        ctx.state = WorkerState.PAUSED;
    }
    
    public static void workerWaitForResponse(WorkerExecutionContext ctx) {
        schedulerStats.stateTransition(ctx, WorkerState.WAITING_FOR_RESPONSE);
        ctx.state = WorkerState.WAITING_FOR_RESPONSE;
    }

    public static void workerWaitForLock(WorkerExecutionContext ctx) {
        schedulerStats.stateTransition(ctx, WorkerState.WAITING_FOR_LOCK);
        ctx.state = WorkerState.WAITING_FOR_LOCK;
    }
    
    public static void workerRunning(WorkerExecutionContext ctx) {
        schedulerStats.stateTransition(ctx, WorkerState.RUNNING);
        ctx.state = WorkerState.RUNNING;
    }
    
    public static void workerExcepted(WorkerExecutionContext ctx) {
        schedulerStats.stateTransition(ctx, WorkerState.EXCEPTED);
        ctx.state = WorkerState.EXCEPTED;
        workerCountDown();
    }
    
    public static void waitForWorkerCompletion() {
        try {
            workersDoneSemaphore.acquire();
            workersDoneSemaphore.release();
        } catch (InterruptedException ignore) { /* ignore */ }
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
            Context nativeCtx, int flags) {
        CallableUnitInfo callableUnitInfo = nativeCtx.getCallableUnitInfo();
        AsyncInvocableWorkerResponseContext respCtx = new AsyncInvocableWorkerResponseContext(callableUnitInfo);
        checkAndObserveNativeAsync(respCtx, callableUnitInfo, flags);
        NativeCallExecutor exec = new NativeCallExecutor(nativeCallable, nativeCtx, respCtx);
        ThreadPoolFactory.getInstance().getWorkerExecutor().submit(exec);
        return respCtx;
    }
    
    public static AsyncInvocableWorkerResponseContext executeNonBlockingNativeAsync(NativeCallableUnit nativeCallable,
            Context nativeCtx, int flags) {
        CallableUnitInfo callableUnitInfo = nativeCtx.getCallableUnitInfo();
        AsyncInvocableWorkerResponseContext respCtx = new AsyncInvocableWorkerResponseContext(callableUnitInfo);
        checkAndObserveNativeAsync(respCtx, callableUnitInfo, flags);
        BLangAsyncCallableUnitCallback callback = new BLangAsyncCallableUnitCallback(respCtx, nativeCtx);
        nativeCallable.execute(nativeCtx, callback);
        return respCtx;
    }
    
    public static SchedulerStats getStats() {
        return schedulerStats;
    }

    private static void checkAndObserveNativeAsync(AsyncInvocableWorkerResponseContext respCtx,
                                                   CallableUnitInfo callableUnitInfo, int flags) {
        if (FunctionFlags.isObserved(flags)) {
            respCtx.registerResponseCallback(new CallbackObserver(respCtx));
            ObservabilityUtils.startClientObservation(callableUnitInfo.attachedToType.toString(),
                    callableUnitInfo.getName(), respCtx);
        }
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
    
    /**
     * This class represents the scheduler statistics.
     */
    public static class SchedulerStats {
        
        private LongAdder[] stateCounts;
        
        public SchedulerStats() {
            this.stateCounts = new LongAdder[6];
            for (int i = 0; i < this.stateCounts.length; i++) {
                this.stateCounts[i] = new LongAdder();
            }
        }

        public long getReadyWorkerCount() {
            return this.stateCounts[0].longValue();
        }

        public long getRunningWorkerCount() {
            return this.stateCounts[1].longValue();
        }

        public long getExceptedWorkerCount() {
            return this.stateCounts[2].longValue();
        }

        public long getWaitingForResponseWorkerCount() {
            return this.stateCounts[3].longValue();
        }

        public long getPausedWorkerCount() {
            return this.stateCounts[4].longValue();
        }

        public long getWaitingForLockWorkerCount() {
            return this.stateCounts[5].longValue();
        }
        
        public void stateTransition(WorkerExecutionContext currentCtx, WorkerState newState) {
            if (!schedulerStatsEnabled || currentCtx.isRootContext()) {
                return;
            }
            WorkerState oldState = currentCtx.state;
            /* we are not considering CREATED state */
            if (oldState != WorkerState.CREATED) {
                this.stateCounts[oldState.ordinal()].decrement();
            }
            /* we are not counting the DONE state, since it is an ever increasing value */
            if (newState != WorkerState.DONE) {
                this.stateCounts[newState.ordinal()].increment();
            }
        }
        
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Worker Status:- \n");
            builder.append("\tREADY: " + this.getReadyWorkerCount() + "\n");
            builder.append("\tRUNNING: " + this.getRunningWorkerCount() + "\n");
            builder.append("\tEXCEPTED: " + this.getExceptedWorkerCount() + "\n");
            builder.append("\tWAITING FOR RESPONSE: " + this.getWaitingForResponseWorkerCount() + "\n");
            builder.append("\tPAUSED: " + this.getPausedWorkerCount() + "\n");
            builder.append("\tWAITING FOR LOCK: " + this.getWaitingForLockWorkerCount() + "\n");
            return builder.toString();
        }
        
    }
    
}

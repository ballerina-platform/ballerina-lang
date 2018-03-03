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

import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;
import org.ballerinalang.util.program.BLangVMUtils;

import java.io.PrintStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This represents the Ballerina worker scheduling functionality. 
 * @since 0.964.0
 */
public class BLangScheduler {

    private static AtomicInteger workerCount = new AtomicInteger(0);
    
    private static Semaphore workersDoneSemaphore = new Semaphore(1);
    
    public static WorkerExecutionContext schedule(WorkerExecutionContext ctx) {
        return schedule(ctx, ctx.runInCaller);
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
        ExecutorService executor = ThreadPoolFactory.getInstance().getWorkerExecutor();
        if (runInCaller) {
            return ctx;
        } else {
            executor.submit(new WorkerExecutor(ctx, false));
            return null;
        }
    }
    
    public static WorkerExecutionContext resume(WorkerExecutionContext ctx) {
        return resume(ctx, false);
    }
    
    public static WorkerExecutionContext resume(WorkerExecutionContext ctx, boolean runInCaller) {
        ctx.state = WorkerState.READY;
        if (runInCaller) {
            ctx.restoreIP();
            return ctx;
        } else {
            ThreadPoolFactory.getInstance().getWorkerExecutor().submit(new WorkerExecutor(ctx, true));
            return null;
        }
    }

    public static WorkerExecutionContext resume(WorkerExecutionContext ctx, int targetIp, boolean runInCaller) {
        ctx.backupIP = targetIp;
        return resume(ctx, runInCaller);
    }
    
    public static void errorThrown(WorkerExecutionContext ctx, BStruct error) {
        ctx.setError(error);
        schedule(ctx, false);
    }
    
    public static void workerDone(WorkerExecutionContext ctx) {
        ctx.ip = -1;
        ctx.state = WorkerState.DONE;
        workerCountDown();
    }
    
    public static void switchToWaitForResponse(WorkerExecutionContext ctx) {
        ctx.state = WorkerState.WAITING_FOR_RESPONSE;
        ctx.backupIP();
        /* the setting to -1 is needed, specially in situations like worker receive scenarios,
         * where you need to return the current executing thread, where this is not critical 
         * in function call scenario, where the calling thread is continued as the first callee
         * worker */
        ctx.ip = -1;
    }
    
    public static void waitForWorkerCompletion() {
        try {
            workersDoneSemaphore.acquire();
            workersDoneSemaphore.release();
        } catch (InterruptedException ignore) { /* ignore */ }
    }
    
    public static void workerExcepted(WorkerExecutionContext ctx) {
        ctx.ip = -1;
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
    
    /**
     * This represents the thread used to execute a runnable worker.
     */
    private static class WorkerExecutor implements Runnable {

        private WorkerExecutionContext ctx;
        
        private boolean restoreIP;
        
        public WorkerExecutor(WorkerExecutionContext ctx, boolean restoreIP) {
            this.ctx = ctx;
            this.restoreIP = restoreIP;
        }
        
        @Override
        public void run() {
            try {
                this.ctx.lockExecution();
                if (this.restoreIP) {
                    ctx.restoreIP();
                }
                this.ctx.state = WorkerState.RUNNING;
                CPU.exec(this.ctx);
            } catch (Throwable e) {
                this.ctx.setError(BLangVMUtils.createErrorStruct(e));
                this.ctx.respCtx.signal(new WorkerSignal(this.ctx, SignalType.ERROR, null));
                BLangScheduler.workerExcepted(this.ctx);
            } finally {
                ctx.unlockExecution();
            }
        }
        
    }
    
}

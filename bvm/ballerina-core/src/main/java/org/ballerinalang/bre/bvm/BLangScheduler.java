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

import org.ballerinalang.runtime.threadpool.ThreadPoolFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This represents the Ballerina worker scheduling functionality. 
 * @since 0.963.0
 */
public class BLangScheduler {

    private static Set<WorkerExecutionContext> activeContexts = new HashSet<>();

    public static WorkerExecutionContext schedule(WorkerExecutionContext ctx) {
        ctx.state = WorkerState.READY;
        ExecutorService executor = ThreadPoolFactory.getInstance().getWorkerExecutor();
        activeContexts.add(ctx);
        if (ctx.runInCaller) {
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
    
    public static void workerDone(WorkerExecutionContext ctx) {
        ctx.ip = -1;
        activeContexts.remove(ctx);
        ctx.state = WorkerState.DONE;
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
    
    public static void waitForCompletion() {
        try {
            ThreadPoolFactory.getInstance().getWorkerExecutor().shutdown();
            ThreadPoolFactory.getInstance().getWorkerExecutor().awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException ignore) { /* ignore */ }
    }
    
    public static void workerExcepted(WorkerExecutionContext ctx, Throwable e) {
        System.out.println("Worker Exception: " + ctx + " -> " + e.getMessage());
        e.printStackTrace();
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
                ctx.lockExecution();
                if (this.restoreIP) {
                    ctx.restoreIP();
                }
                ctx.state = WorkerState.RUNNING;
                CPU.exec(ctx);
            } catch (Throwable e) {
                ctx.state = WorkerState.EXCEPTED;
                BLangScheduler.workerExcepted(this.ctx, e);
            } finally {
                ctx.unlockExecution();
            }
        }
        
    }
    
}

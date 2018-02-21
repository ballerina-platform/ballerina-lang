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

    public static void schedule(WorkerExecutionContext ctx) {
        ctx.state = WorkerState.READY;
        ExecutorService executor = ThreadPoolFactory.getInstance().getWorkerExecutor();
        activeContexts.add(ctx);
        executor.submit(new WorkerExecutor(ctx, false));
    }
    
    public static void resume(WorkerExecutionContext ctx) {
        ctx.state = WorkerState.READY;
        ExecutorService executor = ThreadPoolFactory.getInstance().getWorkerExecutor();
        executor.submit(new WorkerExecutor(ctx, true));
    }
    
    public static void workerDone(WorkerExecutionContext ctx) {
        ctx.ip = -1;
        activeContexts.remove(ctx);
    }
    
    public static void switchToWaitForResponse(WorkerExecutionContext ctx) {
        ctx.state = WorkerState.WAITING_FOR_RESPONSE;
        ctx.backupIP();
        ctx.ip = -1;
    }
    
    public static void waitForCompletion() {
        try {
            ThreadPoolFactory.getInstance().getWorkerExecutor().shutdown();
            ThreadPoolFactory.getInstance().getWorkerExecutor().awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException ignore) { /* ignore */ }
    }
    
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
                ctx.executionLock.lock();
                if (this.restoreIP) {
                    ctx.restoreIP();
                }
                ctx.state = WorkerState.RUNNING;
                CPU.exec(ctx);
            } catch (Throwable e) {
                System.out.println("*** ERROR: " + e.getMessage());
                ctx.state = WorkerState.EXCEPTED;
            } finally {
                ctx.executionLock.unlock();
            }
        }
        
    }
    
}

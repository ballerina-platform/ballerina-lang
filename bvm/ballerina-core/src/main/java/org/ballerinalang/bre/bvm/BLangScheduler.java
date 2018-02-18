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

import java.util.concurrent.ExecutorService;

/**
 * This represents the Ballerina worker scheduling functionality. 
 * @since 0.963.0
 */
public class BLangScheduler {

    public static void schedule(WorkerExecutionContext ctx) {
        ctx.state = WorkerState.READY;
        ExecutorService executor = ThreadPoolFactory.getInstance().getWorkerExecutor();
        executor.submit(new WorkerExecutor(ctx));
    }
    
    public static void workerReturn(WorkerExecutionContext ctx) {
        ctx.ip = -1;
    }
    
    private static class WorkerExecutor implements Runnable {

        private WorkerExecutionContext ctx;
        
        public WorkerExecutor(WorkerExecutionContext ctx) {
            this.ctx = ctx;
        }
        
        @Override
        public void run() {
            try {
                System.out.println("EXEC START");
                ctx.state = WorkerState.RUNNING;
                System.out.println("EXEC END");
                CPU.exec(ctx);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        
    }
    
}

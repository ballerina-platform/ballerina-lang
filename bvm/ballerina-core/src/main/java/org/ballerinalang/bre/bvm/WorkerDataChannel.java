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

import org.ballerinalang.model.values.BRefType;

import java.util.LinkedList;
import java.util.Queue;

/**
 * This represents a worker data channel that is created for each worker to
 * worker interaction for each function call.
 */
public class WorkerDataChannel {

    private WorkerExecutionContext pendingCtx;

    @SuppressWarnings("rawtypes")
    private Queue<WorkerResult> channel = new LinkedList<>();

    @SuppressWarnings("rawtypes")
    public synchronized void putData(BRefType data) {
        this.channel.add(new WorkerResult(data));
        if (this.pendingCtx != null) {
            BLangScheduler.resume(this.pendingCtx);
            this.pendingCtx = null;
        }
    }
    
    @SuppressWarnings("rawtypes")
    public synchronized WorkerResult tryTakeData(WorkerExecutionContext ctx) {
        WorkerResult result = this.channel.peek();
        if (result != null) {
            this.channel.remove();
            return result;
        } else {
            this.pendingCtx = ctx;
            ctx.ip--; // we are going to execute the same worker receive operation later
            BLangScheduler.workerWaitForResponse(ctx);
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    public synchronized WorkerResult tryTakeData() {
        return this.channel.poll();
    }

    /**
     * This represents a worker result value. This is done as a value to be used in the
     * queues used for worker communication. In this way, the queue can distinguish the
     * case of a value not there when the peek, by returning null, and then, if they
     * return a WorkerResult, the value inside it can be either null or not to mention
     * Ballerina null value and non-null value.
     */
    public static class WorkerResult {

        public BRefType value;

        public WorkerResult(BRefType value) {
            this.value = value;
        }

    }

}

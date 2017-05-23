/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.runtime.worker;


import org.ballerinalang.model.statements.WorkerInvocationStmt;
import org.ballerinalang.model.statements.WorkerReplyStmt;

/**
 * A {@code WorkerInteractionDataHolder} Class to hold data related to worker interaction. If worker1 sends data
 * to worker2, it is written as 2 statements in the 2 worker definitions as below. This class holds data for the
 * complete interaction.
 * <br>
 * expression -&gt; worker2; (Code within worker1)
 * expression &lt;- worker1; (Code within worker2)
 *
 *  @since 0.87
 */
public class WorkerInteractionDataHolder {

    private WorkerInvocationStmt workerInvocationStmt;
    private WorkerReplyStmt workerReplyStmt;
    private String sourceWorker;
    private String targetWorker;
    private WorkerDataChannel dataChannel;

    public WorkerReplyStmt getWorkerReplyStmt() {
        return workerReplyStmt;
    }

    public void setWorkerReplyStmt(WorkerReplyStmt workerReplyStmt) {
        this.workerReplyStmt = workerReplyStmt;
    }

    public String getSourceWorker() {
        return sourceWorker;
    }

    public void setSourceWorker(String sourceWorker) {
        this.sourceWorker = sourceWorker;
    }

    public String getTargetWorker() {
        return targetWorker;
    }

    public void setTargetWorker(String targetWorker) {
        this.targetWorker = targetWorker;
    }

    public WorkerInvocationStmt getWorkerInvocationStmt() {
        return workerInvocationStmt;
    }

    public void setWorkerInvocationStmt(WorkerInvocationStmt workerInvocationStmt) {
        this.workerInvocationStmt = workerInvocationStmt;
    }

    public WorkerDataChannel getDataChannel() {
        return dataChannel;
    }

    public void setDataChannel(WorkerDataChannel dataChannel) {
        this.dataChannel = dataChannel;
    }

}

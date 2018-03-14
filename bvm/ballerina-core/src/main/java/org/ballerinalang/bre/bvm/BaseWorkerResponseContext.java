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

import org.ballerinalang.util.debugger.DebugCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

/**
 * This class represents the base implementation for a {@link WorkerResponseContext}.
 */
public abstract class BaseWorkerResponseContext implements WorkerResponseContext {

    protected int[] retRegIndexes;

    protected WorkerSignal currentSignal;

    protected Semaphore responseChecker;

    protected WorkerExecutionContext targetCtx;

    protected Map<String, WorkerDataChannel> workerDataChannels;
    
    protected int workerCount;

    public BaseWorkerResponseContext(int workerCount, boolean checkResponse) {
        this.workerCount = workerCount;
        if (checkResponse) {
            this.responseChecker = new Semaphore(0);
        }
    }
    
    @Override
    public WorkerExecutionContext signal(WorkerSignal signal) {
        switch (signal.getType()) {
        case ERROR:
            return this.onError(signal);
        case MESSAGE:
            this.onMessage(signal);
            break;
        case HALT:
            return this.onHalt(signal);
        case RETURN:
            return this.onReturn(signal);
        case TIMEOUT:
            return this.onTimeout(signal);
        default:
            break;
        }
        return null;
    }
    
    protected WorkerExecutionContext onTimeout(WorkerSignal signal) {
        return null;
    }
        
    protected void onMessage(WorkerSignal signal) { }

    protected WorkerExecutionContext onHalt(WorkerSignal signal) {
        return null;
    }
    
    protected WorkerExecutionContext onError(WorkerSignal signal) {
        return null;
    }
    
    protected void notifyResponseChecker() {
        if (this.responseChecker != null) {
            this.responseChecker.release();
        }
    }

    protected WorkerExecutionContext onReturn(WorkerSignal signal) {
        return null;
    }

    protected void modifyDebugCommands(WorkerExecutionContext parent, WorkerExecutionContext child) {
        if (child.programFile == null || !child.programFile.getDebugger().isDebugEnabled()
                || parent == null || parent.getDebugContext() == null) {
            return;
        }
        DebugCommand crntCommand = child.getDebugContext().getCurrentCommand();
        if (!child.getDebugContext().isCmdChanged()) {
            return;
        }
        if (crntCommand == DebugCommand.STEP_OUT) {
            parent.getDebugContext().setCurrentCommand(DebugCommand.STEP_OVER);
        } else {
            parent.getDebugContext().setCurrentCommand(crntCommand);
        }
    }

    @Override
    public WorkerExecutionContext joinTargetContextInfo(WorkerExecutionContext targetCtx, int[] retRegIndexes) {
        this.targetCtx = targetCtx;
        this.retRegIndexes = retRegIndexes;
        return null;
    }

    public void waitForResponse() {
        if (this.responseChecker == null) {
            return;
        }
        try {
            this.responseChecker.acquire();
        } catch (InterruptedException ignore) { /* ignore */ }
    }

    @Override
    public synchronized WorkerDataChannel getWorkerDataChannel(String name) {
        if (this.workerDataChannels == null) {
            this.workerDataChannels = new HashMap<>();
        }
        WorkerDataChannel channel = this.workerDataChannels.get(name);
        if (channel == null) {
            channel = new WorkerDataChannel();
            this.workerDataChannels.put(name, channel);
        }
        return channel;
    }

}

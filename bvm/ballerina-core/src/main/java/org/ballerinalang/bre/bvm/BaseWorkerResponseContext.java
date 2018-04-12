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
import org.ballerinalang.util.debugger.DebugCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the base implementation for a {@link WorkerResponseContext}.
 */
public abstract class BaseWorkerResponseContext implements WorkerResponseContext {

    protected int[] retRegIndexes;

    protected WorkerSignal currentSignal;
    
    protected List<CallableUnitCallback> responseCallbacks;

    protected WorkerExecutionContext targetCtx;

    protected Map<String, WorkerDataChannel> workerDataChannels;
    
    protected int workerCount;

    public Map<String, Object> localProps;

    public BaseWorkerResponseContext(int workerCount) {
        this.workerCount = workerCount;
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
    
    protected WorkerExecutionContext onReturn(WorkerSignal signal) {
        return null;
    }

    protected void modifyDebugCommands(WorkerExecutionContext parent, WorkerExecutionContext child) {
        /* the child context can be null in a situation like a native async call signaling */
        if (child == null || child.programFile == null || !child.programFile.getDebugger().isDebugEnabled()
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
    
    /**
     * Registers a response callback handler with this context. This must be registered before
     * any actions are done on the response context, or else, the events will not be triggered
     * on the callback. 
     * 
     * @param responseCallback the response callback
     */
    public synchronized void registerResponseCallback(CallableUnitCallback responseCallback) {
        if (this.responseCallbacks == null) {
            this.responseCallbacks = new ArrayList<CallableUnitCallback>();
        }
        this.responseCallbacks.add(responseCallback);
    }
    
    protected void doSuccessCallbackNotify() {
        if (this.responseCallbacks != null) {
            for (CallableUnitCallback callback : this.responseCallbacks) {
                callback.notifySuccess();
            }
        }
    }
    
    protected void doFailCallbackNotify(BStruct error) {
        if (this.responseCallbacks != null) {
            for (CallableUnitCallback callback : this.responseCallbacks) {
                callback.notifyFailure(error);
            }
        }
    }

    @Override
    public void setLocalProperty(String key, Object val) {
        if (localProps == null) {
            localProps = new HashMap<>();
        }
        localProps.put(key, val);
    }

    @Override
    public Object getLocalProperty(String key) {
        if (localProps != null) {
            return localProps.get(key);
        }
        return null;
    }

}

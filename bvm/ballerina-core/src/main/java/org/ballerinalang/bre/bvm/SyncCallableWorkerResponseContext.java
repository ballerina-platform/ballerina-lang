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

import org.ballerinalang.model.types.BType;

/**
 * This represents a invocation worker result context for supporting
 * handling multiple workers, i.e. synchronized.
 * 
 * @since 0.965.0
 */
public class SyncCallableWorkerResponseContext extends CallableWorkerResponseContext {

    public SyncCallableWorkerResponseContext(BType[] responseTypes, int workerCount) {
        super(responseTypes, workerCount);
    }
    
    @Override
    protected synchronized void onMessage(WorkerSignal signal) {
        super.onMessage(signal);
    }

    @Override
    protected synchronized WorkerExecutionContext onHalt(WorkerSignal signal) {
        return super.onHalt(signal);
    }

    @Override
    protected synchronized WorkerExecutionContext onError(WorkerSignal signal) {
        return super.onError(signal);
    }

    @Override
    protected synchronized WorkerExecutionContext onReturn(WorkerSignal signal) {
        return super.onReturn(signal);
    }

}

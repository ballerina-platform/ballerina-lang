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
import org.ballerinalang.model.values.BStruct;

/**
 * This class represents a {@link WorkerResponseContext} implementation which supports
 * a callback mechanism to notify an interested party when the response is available.
 */
public class CallbackedInvocableWorkerResponseContext extends SyncCallableWorkerResponseContext {

    private CallableUnitCallback responseCallback;
    
    public CallbackedInvocableWorkerResponseContext(BType[] responseTypes, int workerCount, boolean checkResponse,
            CallableUnitCallback responseCallback) {
        super(responseTypes, workerCount, checkResponse);
        this.responseCallback = responseCallback;
    }
    
    @Override
    public WorkerExecutionContext onFulfillment(boolean runInCaller) {
        WorkerExecutionContext retCtx = super.onFulfillment(runInCaller);
        this.responseCallback.notifySuccess();
        return retCtx;
    }
    
    @Override
    protected WorkerExecutionContext onFinalizedError(WorkerExecutionContext targetCtx, BStruct error) {
        WorkerExecutionContext ctx = super.onFinalizedError(targetCtx, error);
        this.responseCallback.notifyFailure(error);
        return ctx;
    }

}

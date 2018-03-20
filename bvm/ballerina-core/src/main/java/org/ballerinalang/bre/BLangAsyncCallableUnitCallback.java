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
package org.ballerinalang.bre;

import org.ballerinalang.bre.bvm.BLangScheduler;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.bre.bvm.SignalType;
import org.ballerinalang.bre.bvm.WorkerData;
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.bre.bvm.WorkerResponseContext;
import org.ballerinalang.bre.bvm.WorkerSignal;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.codegen.CallableUnitInfo;
import org.ballerinalang.util.program.BLangVMUtils;

/**
 * This class represents the callback functionality for async non-blocking native calls.
 * 
 * @since 0.964
 */
public class BLangAsyncCallableUnitCallback implements CallableUnitCallback {

    private WorkerResponseContext respCtx;
    
    private Context nativeCallCtx;
        
    public BLangAsyncCallableUnitCallback(WorkerResponseContext respCtx, Context nativeCallCtx) {
        this.respCtx = respCtx;
        this.nativeCallCtx = nativeCallCtx;
    }
    
    @Override
    public void notifySuccess() {
        CallableUnitInfo cui = this.nativeCallCtx.getCallableUnitInfo();
        WorkerData result = BLangVMUtils.createWorkerData(cui.retWorkerIndex);
        BType[] retTypes = cui.getRetParamTypes();        
        BLangVMUtils.populateWorkerResultWithValues(result, this.nativeCallCtx.getReturnValues(), retTypes);
        WorkerExecutionContext ctx = this.respCtx.signal(new WorkerSignal(null, SignalType.RETURN, result));
        BLangScheduler.resume(ctx);
    }

    @Override
    public void notifyFailure(BStruct error) {
        CallableUnitInfo cui = this.nativeCallCtx.getCallableUnitInfo();
        WorkerData result = BLangVMUtils.createWorkerData(cui.retWorkerIndex);
        BType[] retTypes = cui.getRetParamTypes();        
        BLangVMUtils.populateWorkerResultWithValues(result, this.nativeCallCtx.getReturnValues(), retTypes);
        WorkerExecutionContext ctx = this.respCtx.signal(new WorkerSignal(
                new WorkerExecutionContext(error), SignalType.ERROR, result));
        BLangScheduler.resume(ctx);
    }

}

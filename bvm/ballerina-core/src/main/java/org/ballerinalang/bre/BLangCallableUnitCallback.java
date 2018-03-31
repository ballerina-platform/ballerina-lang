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
import org.ballerinalang.bre.bvm.WorkerExecutionContext;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.util.program.BLangVMUtils;

/**
 * This class represents the callback functionality for non-blocking native calls.
 *
 * @since 0.964
 */
public class BLangCallableUnitCallback implements CallableUnitCallback {

    private WorkerExecutionContext parentCtx;

    private Context nativeCallCtx;

    private int[] retRegs;

    private BType[] retTypes;

    public BLangCallableUnitCallback(Context nativeCallCtx, WorkerExecutionContext parentCtx,
                                     int[] retRegs, BType[] retTypes) {
        this.parentCtx = parentCtx;
        this.nativeCallCtx = nativeCallCtx;
        this.retRegs = retRegs;
        this.retTypes = retTypes;
    }

    @Override
    public void notifySuccess() {
        BLangVMUtils.populateWorkerDataWithValues(this.parentCtx.workerLocal, this.retRegs,
                this.nativeCallCtx.getReturnValues(), this.retTypes);
        BLangScheduler.resume(this.parentCtx);
    }

    @Override
    public void notifyFailure(BStruct error) {
        BLangScheduler.resume(BLangScheduler.errorThrown(this.parentCtx, error));
    }

}

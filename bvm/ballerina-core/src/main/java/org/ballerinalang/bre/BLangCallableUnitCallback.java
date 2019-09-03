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

import org.ballerinalang.bre.bvm.BVM;
import org.ballerinalang.bre.bvm.BVMScheduler;
import org.ballerinalang.bre.bvm.CallableUnitCallback;
import org.ballerinalang.bre.bvm.StackFrame;
import org.ballerinalang.bre.bvm.Strand;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.util.program.BLangVMUtils;

/**
 * This class represents the callback functionality for non-blocking native calls.
 *
 * @since 0.964
 */
public class BLangCallableUnitCallback implements CallableUnitCallback {

    private Strand strand;

    private Context nativeCallCtx;

    private int retReg;

    private BType retType;

    public BLangCallableUnitCallback(Context nativeCallCtx, Strand strand,
                                     int retReg, BType retType) {
        this.strand = strand;
        this.nativeCallCtx = nativeCallCtx;
        this.retReg = retReg;
        this.retType = retType;
    }

    @Override
    public void notifySuccess() {
        if (strand.fp > 0) {
            if (BVM.checkIsType(this.nativeCallCtx.getReturnValue(), BTypes.typeError)) {
                strand.currentFrame.handleChannelError((BRefType) this.nativeCallCtx.getReturnValue(),
                        strand.peekFrame(1).wdChannels);
            }
            strand.popFrame();
            StackFrame sf = strand.currentFrame;
            BLangVMUtils.populateWorkerDataWithValues(sf, this.retReg,
                    this.nativeCallCtx.getReturnValue(), this.retType);
            BVMScheduler.schedule(strand);
            return;
        }
        if (BVM.checkIsType(this.nativeCallCtx.getReturnValue(), BTypes.typeError)) {
            strand.currentFrame.handleChannelError((BRefType) this.nativeCallCtx.getReturnValue(),
                    strand.respCallback.parentChannels);
        }
        strand.respCallback.signal();
    }

    @Override
    public void notifyFailure(BError error) {
        if (strand.fp > 0) {
            strand.currentFrame.handleChannelPanic(error, strand.peekFrame(1).wdChannels);
            strand.popFrame();
            strand.setError(error);
            BVM.handleError(strand);
            BVMScheduler.schedule(strand);
            return;
        }
        strand.currentFrame.handleChannelPanic(error, strand.respCallback.parentChannels);
        strand.respCallback.setError(error);
        strand.respCallback.signal();
    }

}

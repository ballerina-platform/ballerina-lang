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
package org.ballerinalang.model.values;

import org.ballerinalang.bre.bvm.BVM;
import org.ballerinalang.bre.bvm.BVMScheduler;
import org.ballerinalang.bre.bvm.StackFrame;
import org.ballerinalang.bre.bvm.Strand;
import org.ballerinalang.bre.bvm.Strand.State;
import org.ballerinalang.bre.bvm.StrandCallback.CallbackStatus;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;

import java.util.List;
import java.util.Map;

/**
 * Ballerina value for the callable "future" type.
 */
public class BCallableFuture implements BFuture {

    private String callableName;

    private Strand strand;
    
    public BCallableFuture(String callableName, Strand strand) {
        this.callableName = callableName;
        this.strand = strand;
    }

    @Override
    public String stringValue() {
        return "callable future: " + this.callableName;
    }

    @Override
    public BType getType() {
        return BTypes.typeFuture;
    }

    @Override
    public void stamp(BType type, List<BVM.TypeValuePair> unresolvedValues) {

    }

    @Override
    public BValue copy(Map<BValue, BValue> refs) {
        return new BCallableFuture(this.callableName, this.strand);
    }

    @Override
    public Strand value() {
        return this.strand;
    }

    @Override
    public boolean cancel() {
        if (this.isDone()) {
            return false;
        }
        //TODO double check below logic, current frame may be already dropped.
        /* only non-native workers can be cancelled */
        StackFrame currentFrame = strand.currentFrame;
        if (currentFrame != null &&  currentFrame.callableUnitInfo.isNative()) {
            return false;
        }
        BVMScheduler.stateChange(strand, State.RUNNABLE, State.TERMINATED);
        strand.aborted = true;
        return true;
    }

    @Override
    public boolean isDone() {
        return strand.respCallback.getStatus() != CallbackStatus.NOT_RETURNED;
    }

    @Override
    public boolean isCancelled() {
        return strand.aborted;
    }

}

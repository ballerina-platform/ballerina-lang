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

import org.ballerinalang.bre.bvm.StrandCallback.CallbackStatus;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.util.BLangConstants;
import org.ballerinalang.util.codegen.cpentries.UTF8CPEntry;

import java.util.List;

/**
 * This represents handler class which handles callback returns.
 *
 * @since 0.985.0
 */
public class WaitCallbackHandler {

    //TODO right now, from the execution point of view, these values copies may happen in the child strand,
    //TODO basically child copies the value to parent strand's relevant location and schedule the strand
    //TODO it would be better if we can just schedule the parent, so that parent will do his value copying part
    //TODO himself, from the execution point of view that would be more accurate.
    static boolean handleReturnInWait(Strand strand, BType expType, int retReg, SafeStrandCallback... callbacks) {
        try {
            strand.acquireExecutionLock();
            if (strand.strandWaitHandler.waitCompleted) {
                return false;
            }
            for (SafeStrandCallback callback : callbacks) {
                callback.acquireDataLock();
                if (callback.getStatus() == CallbackStatus.NOT_RETURNED) {
                    callback.configureWaitHandler(strand, false, expType, retReg, -1);
                    callback.releaseDataLock();
                    continue;
                }
                if (callback.getStatus() == CallbackStatus.VALUE_RETURNED) {
                    copyReturnsInWait(strand, callback, expType, retReg);
                    strand.strandWaitHandler.waitCompleted = true;
                    callback.releaseDataLock();
                    return true;
                }
                if (callback.getStatus() == CallbackStatus.ERROR_RETURN) {
                    strand.strandWaitHandler.callBacksRemaining--;
                    if (strand.strandWaitHandler.callBacksRemaining == 0) {
                        copyReturnsInWait(strand, callback, expType, retReg);
                        strand.strandWaitHandler.waitCompleted = true;
                        callback.releaseDataLock();
                        return true;
                    }
                    callback.releaseDataLock();
                    continue;
                }
                strand.strandWaitHandler.callBacksRemaining--;
                if (strand.strandWaitHandler.callBacksRemaining == 0) {
                    strand.setError(callback.getErrorVal());
                    strand.strandWaitHandler.waitCompleted = true;
                    callback.releaseDataLock();
                    BVM.handleError(strand);
                    return true;
                }
                callback.releaseDataLock();
            }
        } finally {
            strand.releaseExecutionLock();
        }
        return false;
    }

    static boolean handleReturnInWaitMultiple(Strand strand, int retReg,
                                             List<SafeStrandCallback.WaitMultipleCallback> callbacks) {
        try {
            strand.acquireExecutionLock();
            if (strand.strandWaitHandler.waitCompleted) {
                return false;
            }
            for (SafeStrandCallback.WaitMultipleCallback waitMultipleCallback: callbacks) {
                Integer keyReg = waitMultipleCallback.getKeyRegIndex();
                SafeStrandCallback callback = waitMultipleCallback.getCallback();
                callback.acquireDataLock();

                if (callback.getStatus() == CallbackStatus.NOT_RETURNED) {
                    callback.configureWaitHandler(strand, true, null, retReg, keyReg);
                    callback.releaseDataLock();
                    continue;
                }

                if (callback.getStatus().returned) {
                    copyReturnsInWaitMultiple(strand.currentFrame, callback, retReg, keyReg);
                    strand.strandWaitHandler.callbacksToWaitFor.remove(keyReg);
                    callback.releaseDataLock();
                    // Check if there are no strands to wait for
                    if (strand.strandWaitHandler.callbacksToWaitFor.isEmpty()) {
                        return true;
                    }
                    continue;
                }
                strand.setError(callback.getErrorVal());
                strand.strandWaitHandler.waitCompleted = true;
                callback.releaseDataLock();
                BVM.handleError(strand);
                return true;
            }

        } finally {
            strand.releaseExecutionLock();
        }
        return false;
    }

    static boolean handleFlush(Strand strand, int retReg, String[] channels) {
        strand.configureFlushDetails(channels);
        for (int i = 0; i < channels.length; i++) {
            WorkerDataChannel dataChannel;
            if (strand.currentFrame.callableUnitInfo.getDefaultWorkerInfo()
                    .getWorkerName().equals(BLangConstants.DEFAULT_WORKER_NAME)) {
                dataChannel = strand.currentFrame.wdChannels.getWorkerDataChannel(channels[i]);
            } else {
                if (strand.fp > 0) {
                    dataChannel = strand.peekFrame(1).wdChannels.getWorkerDataChannel(channels[i]);
                } else {
                    dataChannel = strand.respCallback.parentChannels.getWorkerDataChannel(channels[i]);
                }

            }
            if (dataChannel.flushChannel(strand, retReg)) {
                return true;
            }
        }
        return false;
    }

    private static void copyReturnsInWaitMultiple(StackFrame sf, SafeStrandCallback strandCallback, int retReg,
                                                  int keyReg) {
        String keyValue = ((UTF8CPEntry) sf.constPool[keyReg]).getValue();
        switch (strandCallback.retType.getTag()) {
            case TypeTags.INT_TAG:
                ((BMap) sf.refRegs[retReg]).put(keyValue, new BInteger (strandCallback.getIntRetVal()));
                break;
            case TypeTags.BYTE_TAG:
                ((BMap) sf.refRegs[retReg]).put(keyValue, new BByte(strandCallback.getByteRetVal()));
                break;
            case TypeTags.FLOAT_TAG:
                ((BMap) sf.refRegs[retReg]).put(keyValue, new BFloat(strandCallback.getFloatRetVal()));
                break;
            case TypeTags.STRING_TAG:
                ((BMap) sf.refRegs[retReg]).put(keyValue, new BString(strandCallback.getStringRetVal()));
                break;
            case TypeTags.BOOLEAN_TAG:
                ((BMap) sf.refRegs[retReg]).put(keyValue, new BBoolean(strandCallback.getBooleanRetVal() == 1));
                break;
            default:
                ((BMap) sf.refRegs[retReg]).put(keyValue, strandCallback.getRefRetVal());
                break;
        }
    }

    private static void copyReturnsInWait(Strand strand, SafeStrandCallback strandCallback, BType expType,
                                             int retReg) {
        StackFrame sf = strand.currentFrame;
        if (expType.getTag() == TypeTags.UNION_TAG) {
            switch (strandCallback.retType.getTag()) {
                case TypeTags.INT_TAG:
                    sf.refRegs[retReg] = new BInteger(strandCallback.getIntRetVal());
                    break;
                case TypeTags.BYTE_TAG:
                    sf.refRegs[retReg] = new BByte(strandCallback.getByteRetVal());
                    break;
                case TypeTags.FLOAT_TAG:
                    sf.refRegs[retReg] = new BFloat(strandCallback.getFloatRetVal());
                    break;
                case TypeTags.STRING_TAG:
                    sf.refRegs[retReg] = new BString(strandCallback.getStringRetVal());
                    break;
                case TypeTags.BOOLEAN_TAG:
                    sf.refRegs[retReg] = new BBoolean(strandCallback.getBooleanRetVal() == 1);
                    break;
                default:
                    sf.refRegs[retReg] = strandCallback.getRefRetVal();
                    break;
            }
            return;
        }
        switch (strandCallback.retType.getTag()) {
            case TypeTags.INT_TAG:
                sf.longRegs[retReg] = strandCallback.getIntRetVal();
                break;
            case TypeTags.BYTE_TAG:
                sf.longRegs[retReg] = strandCallback.getByteRetVal();
                break;
            case TypeTags.FLOAT_TAG:
                sf.doubleRegs[retReg] = strandCallback.getFloatRetVal();
                break;
            case TypeTags.STRING_TAG:
                sf.stringRegs[retReg] = strandCallback.getStringRetVal();
                break;
            case TypeTags.BOOLEAN_TAG:
                sf.intRegs[retReg] = strandCallback.getBooleanRetVal();
                break;
            default:
                sf.refRegs[retReg] = strandCallback.getRefRetVal();
                break;
        }
    }
}

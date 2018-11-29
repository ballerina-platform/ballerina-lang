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
package org.ballerinalang.bre.vm;

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.util.codegen.cpentries.UTF8CPEntry;
import java.util.Map;

/**
 * This represents handler class which handles callback returns.
 *
 * @since 0.985.0
 */
public class WaitCallbackHandler {

    static Strand handleReturnInWait(Strand strand, BType expType, int retReg, SafeStrandCallback... callbacks) {
        try {
            strand.acquireExecutionLock();
            if (strand.strandWaitHandler.waitCompleted) {
                return null;
            }
            for (SafeStrandCallback callback : callbacks) {
                callback.acquireDataLock();
                if (!callback.isDone()) {
                    callback.configureWaitHandler(strand, false, expType, retReg, -1);
                    callback.releaseDataLock();
                    continue;
                }
                if (callback.getErrorVal() == null) {
                    strand.strandWaitHandler.callBacksRemaining--;
                    if (copyReturnsInWait(strand, callback, expType, retReg)) {
                        strand.strandWaitHandler.waitCompleted = true;
                        callback.releaseDataLock();
                        return strand;
                    } else {
                        // If one callback is of error type, we wait for the others
                        callback.releaseDataLock();
                        continue;
                    }
                }
                strand.strandWaitHandler.callBacksRemaining--;
                if (strand.strandWaitHandler.callBacksRemaining == 0) {
                    strand.setError(callback.getErrorVal());
                    strand.strandWaitHandler.waitCompleted = true;
                    callback.releaseDataLock();
                    BVM.handleError(strand);
                    return strand;
                }
                callback.releaseDataLock();
            }
        } finally {
            strand.releaseExecutionLock();
        }
        return null;
    }

    static Strand handleReturnInWaitMultiple(Strand strand, int retReg, Map<Integer, SafeStrandCallback> callbacks) {
        try {
            strand.acquireExecutionLock();
            if (strand.strandWaitHandler.waitCompleted) {
                return null;
            }
            //TODO check whether keyReg can have same value
            for (Map.Entry<Integer, SafeStrandCallback> entry : callbacks.entrySet()) {
                Integer keyReg = entry.getKey();
                SafeStrandCallback callback = entry.getValue();
                callback.acquireDataLock();

                if (!callback.isDone()) {
                    callback.configureWaitHandler(strand, true, null, retReg, keyReg);
                    callback.releaseDataLock();
                    continue;
                }

                if (callback.getErrorVal() != null) {
                    strand.setError(callback.getErrorVal());
                    strand.strandWaitHandler.waitCompleted = true;
                    callback.releaseDataLock();
                    BVM.handleError(strand);
                    return strand;
                }

                copyReturnsInWaitMultiple(strand.currentFrame, callback, retReg, keyReg);
                strand.strandWaitHandler.callbacksToWaitFor.remove(keyReg);
                callback.releaseDataLock();

                // Check if there are no strands to wait for
                if (strand.strandWaitHandler.callbacksToWaitFor.isEmpty()) {
                    return strand;
                }
            }

        } finally {
            strand.releaseExecutionLock();
        }
        return null;
    }

    private static void copyReturnsInWaitMultiple(StackFrame sf, SafeStrandCallback strandCallback, int retReg, int keyReg) {
        String keyValue = ((UTF8CPEntry) sf.constPool[keyReg]).getValue();
        switch (strandCallback.retType.getTag()) {
            case TypeTags.INT_TAG:
                ((BMap) sf.refRegs[retReg]).put(keyValue, new BInteger (strandCallback.getIntRetVal()));
                break;
            case TypeTags.BYTE_TAG:
                ((BMap) sf.refRegs[retReg]).put(keyValue, new BByte((byte) strandCallback.getByteRetVal()));
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

    private static boolean copyReturnsInWait(Strand strand, SafeStrandCallback strandCallback, BType expType, int retReg) {
        StackFrame sf = strand.currentFrame;
        if (expType.getTag() == TypeTags.UNION_TAG) {
            switch (strandCallback.retType.getTag()) {
                case TypeTags.INT_TAG:
                    sf.refRegs[retReg] = new BInteger(strandCallback.getIntRetVal());
                    break;
                case TypeTags.BYTE_TAG:
                    sf.refRegs[retReg] = new BByte((byte) strandCallback.getByteRetVal());
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
                    BRefType<?> refRetVal = strandCallback.getRefRetVal();
                    if (BVM.checkIsType(refRetVal, BTypes.typeError) && strand.strandWaitHandler.callBacksRemaining != 0) {
                        return false;
                    }
                    sf.refRegs[retReg] = strandCallback.getRefRetVal();
                    break;
            }
            return true;
        }
        switch (strandCallback.retType.getTag()) {
            case TypeTags.INT_TAG:
                sf.longRegs[retReg] = strandCallback.getIntRetVal();
                break;
            case TypeTags.BYTE_TAG:
                sf.intRegs[retReg] = strandCallback.getByteRetVal();
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
        return true;
    }
}

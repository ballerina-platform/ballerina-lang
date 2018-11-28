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
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BByte;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.util.codegen.cpentries.UTF8CPEntry;
import java.util.Map;

/**
 * This represents handler class which handles callback returns.
 *
 * @since 0.985.0
 */
public class CallbackReturnHandler {

    static Strand handleReturn(Strand strand, BType expType, int retReg, SafeStrandCallback... callbacks) {
        try {
            strand.acquireExecutionLock();
            if (strand.waitCompleted) {
                    return null;
            }
            for (SafeStrandCallback callback : callbacks) {
                callback.acquireDataLock();
                if (callback.getErrorVal() != null) {
                    strand.callBacksRemaining--;
                    if (strand.callBacksRemaining == 0) {
                        return setErrorToStrand(strand, callback);
                    }
                    callback.releaseDataLock();
                    continue;
                }
                if (callback.returnDataAvailable()) {
                    handleReturn(strand.currentFrame, callback, expType, retReg);
                    strand.waitCompleted = true;
                    strand.callBacksRemaining--;
                    callback.releaseDataLock();
                    return strand;
                }

                callback.setRetData(strand, expType, retReg, false, 0);
                callback.releaseDataLock();
            }

        } finally {
            strand.releaseExecutionLock();
        }
        return null;
    }

    static Strand handleReturn(Strand strand, int retReg, Map<Integer, SafeStrandCallback> callbacks) {
        try {
            strand.acquireExecutionLock();
            if (strand.waitCompleted) {
                return null;
            }
            for (Map.Entry<Integer, SafeStrandCallback> entry : callbacks.entrySet()) {
                Integer keyReg = entry.getKey();
                SafeStrandCallback strandCallback = entry.getValue();
                strandCallback.acquireDataLock();

                if (strandCallback.getErrorVal() != null) {
                    return setErrorToStrand(strand, strandCallback);
                }

                if (strandCallback.returnDataAvailable()) {
                    handleWaitAllReturn(strand.currentFrame, strandCallback, retReg, keyReg);
                    strand.callbacksToWaitFor.remove(keyReg);
                    strandCallback.releaseDataLock();

                    // Check if there are no strands to wait for
                    if (strand.callbacksToWaitFor.isEmpty()) {
                        return strand;
                    }
                } else {
                    strandCallback.setRetData(strand, null, retReg, true, keyReg);
                    strandCallback.releaseDataLock();
                }
            }

        } finally {
            strand.releaseExecutionLock();
        }
        return null;
    }

    private static Strand setErrorToStrand(Strand strand, SafeStrandCallback callback) {
        strand.setError(callback.getErrorVal());
        callback.releaseDataLock();
        BVM.handleError(strand);
        strand.waitCompleted = true;
        return strand;
    }

    private static void handleWaitAllReturn(StackFrame sf, SafeStrandCallback strandCallback, int retReg, int keyReg) {
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

    private static void handleReturn(StackFrame sf, SafeStrandCallback strandCallback, BType expType, int retReg) {
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
    }
}

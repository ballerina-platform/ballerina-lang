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
import org.ballerinalang.model.values.BString;

/**
 * This represents handler class which handles callback returns.
 *
 * @since 0.985.0
 */
public class CallbackReturnHandler {

    // TODO: fix bug second strand may also return a value
    static Strand handleReturn(Strand strand, BType expType, int retReg, SafeStrandCallback... callbacks) {
        try {
            strand.acquireExecutionLock();
            for (SafeStrandCallback callback : callbacks) {
                callback.acquireDataLock();

                if (callback.returnDataAvailable()) {
                    handleReturn(strand.currentFrame, callback, expType, retReg);
                    callback.releaseDataLock();
                    return strand;
                }

                callback.setRetData(strand, expType, retReg);
                callback.releaseDataLock();
            }

        } finally {
            strand.releaseExecutionLock();
        }
        return null;
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

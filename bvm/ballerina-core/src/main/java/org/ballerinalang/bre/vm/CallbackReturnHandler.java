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

import org.ballerinalang.model.types.TypeTags;

/**
 * This represents handler class which handles callback returns.
 *
 * @since 0.985.0
 */
public class CallbackReturnHandler {

    static Strand handleReturn(Strand strand, int retReg, SafeStrandCallback... callbacks) {
        try {
            strand.acquireExecutionLock();
            for (SafeStrandCallback callback : callbacks) {
                callback.acquireDataLock();

                if (callback.returnDataAvailable()) {
                    handleReturn(strand.currentFrame, callback, retReg);
                    callback.releaseDataLock();
                    return strand;
                }

                callback.setRetData(strand, retReg);
                callback.releaseDataLock();
            }

        } finally {
            strand.releaseExecutionLock();

        }
        return null;
    }

    private static void handleReturn(DataFrame sf, SafeStrandCallback strandCallback, int retReg) {
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

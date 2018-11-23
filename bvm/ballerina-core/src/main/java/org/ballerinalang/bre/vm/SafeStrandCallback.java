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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Safe BVM callback implementation.
 *
 * @since 0.985.0
 */
public class SafeStrandCallback extends StrandCallback {

    private Lock dataLock;

    private AtomicBoolean returnValueAvailable;

    private Strand contStrand;

    private BType expType;

    private int keyReg;

    private int retReg;

    private boolean multipleWait;

    SafeStrandCallback(BType retType) {
        super(retType);
        this.dataLock = new ReentrantLock();
        this.returnValueAvailable = new AtomicBoolean(false);
    }

    @Override
    public void signal() {
        try {
            dataLock.lock();
            this.returnValueAvailable.set(true);
            if (contStrand == null) {
                return;
            }
            Strand strand;
            if (multipleWait) {
                // Create the hashmap with the keyReg and callback
                Map<Integer, SafeStrandCallback> callbackHashMap = new HashMap();
                callbackHashMap.put(keyReg, this);

                strand = CallbackReturnHandler.handleReturn(contStrand, retReg, callbackHashMap);
            } else {
                strand = CallbackReturnHandler.handleReturn(contStrand, expType, retReg, this);
            }
            if (strand != null) {
                BVMScheduler.schedule(strand);
            }
        } finally {
            dataLock.unlock();
        }
    }

    void acquireDataLock() {
        dataLock.lock();
    }

    void releaseDataLock() {
        dataLock.unlock();
    }

    void setRetData(Strand contStrand, BType expType, int retReg, boolean multipleWait, int keyReg) {
        this.contStrand = contStrand;
        this.expType = expType;
        this.retReg = retReg;
        this.multipleWait = multipleWait;
        this.keyReg = keyReg;
    }

    boolean returnDataAvailable() {
        return this.returnValueAvailable.get();
    }
}

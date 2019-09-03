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

import org.ballerinalang.model.types.BType;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.util.codegen.CallableUnitInfo.ChannelDetails;

/**
 * Default VM callback implementation to report back VM related callback events.
 *
 * @since 0.985.0
 */
public abstract class StrandCallback {

    //Return value holders
    private long longVal;
    private double doubleVal;
    private String stringVal;
    private int intVal;
    private BRefType<?> refVal;
    private BError error;
    //TODO try to generalize below to normal data channels

    private volatile CallbackStatus status;

    private CallbackStatus valueStatus;

    ChannelDetails[] sendIns;

    protected BType retType; //TODO may be this is wrong, we should take the type in wait expression -check this

    public WDChannels parentChannels;

    StrandCallback(BType retType, ChannelDetails[] sendIns, WDChannels parentChannels) {
        this.retType = retType;
        this.status = CallbackStatus.NOT_RETURNED;
        this.sendIns = sendIns;
        this.parentChannels = parentChannels;
        BVMScheduler.strandCountUp();
    }

    /**
     * Method to signal the callback once done.
     */
    public void signal() {
        this.status = valueStatus;
        if (this.status == null) {
            this.status =  CallbackStatus.VALUE_RETURNED;
        }
        BVMScheduler.strandCountDown();
    }

    public CallbackStatus getStatus() {
        return this.status;
    }

    /**
     * Method to set int return value.
     *
     * @param value to be returned
     */
    public void setIntReturn(long value) {
        this.longVal = value;
        this.valueStatus = CallbackStatus.VALUE_RETURNED;
    }

    /**
     * Method to set float return value.
     *
     * @param value to be returned
     */
    public void setFloatReturn(double value) {
        this.doubleVal = value;
        this.valueStatus = CallbackStatus.VALUE_RETURNED;
    }

    /**
     * Method to set string return value.
     *
     * @param value to be returned
     */
    public void setStringReturn(String value) {
        this.stringVal = value;
        this.valueStatus = CallbackStatus.VALUE_RETURNED;
    }

    /**
     * Method to set boolean return value.
     *
     * @param value to be returned
     */
    public void setBooleanReturn(int value) {
        this.intVal = value;
        this.valueStatus = CallbackStatus.VALUE_RETURNED;
    }

    /**
     * Method to set byte return value.
     *
     * @param value to be returned
     */
    public void setByteReturn(long value) {
        this.longVal = value;
        this.valueStatus = CallbackStatus.VALUE_RETURNED;
    }

    /**
     * Method to set reference type return value.
     *
     * @param value to be returned
     */
    public void setRefReturn(BRefType<?> value) {
        this.refVal = value;
        if (BVM.checkIsType(this.refVal, BTypes.typeError)) {
            this.valueStatus = CallbackStatus.ERROR_RETURN;
        } else {
            this.valueStatus = CallbackStatus.VALUE_RETURNED;
        }
    }

    /**
     * Method to set error return value.
     *
     * @param error to be returned
     */
    public void setError(BError error) {
        this.error = error;
        this.valueStatus = CallbackStatus.PANIC;
    }

    /**
     * Method to get int return value.
     *
     * @return value
     */
    public long getIntRetVal() {
        return longVal;
    }

    /**
     * Method to get float return value.
     *
     * @return value
     */
    public double getFloatRetVal() {
        return doubleVal;
    }

    /**
     * Method to get string return value.
     *
     * @return value
     */
    public String getStringRetVal() {
        return stringVal;
    }

    /**
     * Method to get boolean return value.
     *
     * @return value
     */
    public int getBooleanRetVal() {
        return intVal;
    }

    /**
     * Method to get byte return value.
     *
     * @return value
     */
    public long getByteRetVal() {
        return longVal;
    }

    /**
     * Method to get reference type return value.
     *
     * @return value
     */
    public BRefType<?> getRefRetVal() {
        return refVal;
    }

    /**
     * Method to get error return value.
     *
     * @return value
     */
    public BError getErrorVal() {
        return error;
    }

    /**
     * Callback statuses.
     */
    public static enum CallbackStatus {
        NOT_RETURNED(false), VALUE_RETURNED(true), ERROR_RETURN(true), PANIC(false);

        public final boolean returned;

        CallbackStatus(boolean returned) {
            this.returned = returned;
        }
    }
}

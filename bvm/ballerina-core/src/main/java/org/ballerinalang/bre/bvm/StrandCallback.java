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
import org.ballerinalang.model.values.BError;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.util.observability.ObserveUtils;
import org.ballerinalang.util.observability.ObserverContext;

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
    private ObserverContext observerContext;
    //TODO try to generalize below to normal data channels
    //channels are only used in SafeStrandCallback
    WDChannels parentChannels;
    WDChannels wdChannels;

    protected BType retType; //TODO may be this is wrong, we should take the type in wait expression -check this

    StrandCallback(BType retType) {
        this.retType = retType;
        this.wdChannels = new WDChannels();
    }

    /**
     * Method to signal the callback once done.
     */
    public void signal() {
        // Stop observation
        ObserveUtils.stopObservation(observerContext);
    }

    /**
     * Method to set int return value.
     *
     * @param value to be returned
     */
    public void setIntReturn(long value) {
        this.longVal = value;
    }

    /**
     * Method to set float return value.
     *
     * @param value to be returned
     */
    public void setFloatReturn(double value) {
        this.doubleVal = value;
    }

    /**
     * Method to set string return value.
     *
     * @param value to be returned
     */
    public void setStringReturn(String value) {
        this.stringVal = value;
    }

    /**
     * Method to set boolean return value.
     *
     * @param value to be returned
     */
    public void setBooleanReturn(int value) {
        this.intVal = value;
    }

    /**
     * Method to set byte return value.
     *
     * @param value to be returned
     */
    public void setByteReturn(int value) {
        this.intVal = value;
    }

    /**
     * Method to set reference type return value.
     *
     * @param value to be returned
     */
    public void setRefReturn(BRefType<?> value) {
        this.refVal = value;
    }

    /**
     * Method to set error return value.
     *
     * @param error to be returned
     */
    public void setError(BError error) {
        this.error = error;
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
    public int getByteRetVal() {
        return intVal;
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
     * Method to set the observation context of the callback.
     *
     * @param context observer context
     */
    public void setObserverContext(ObserverContext context) {
        this.observerContext = context;
    }

    /**
     * Method to get the observation context of the callback.
     *
     * @return observer context of the callback
     */
    public ObserverContext getObserverContext() {
        return this.observerContext;
    }

    /**
     * Method to get the worker data channels of the strand this callback is associated with.
     * @return worker data channels or null
     */
    WDChannels getWorkerDataChannels() {
        //Used in SafeStrandCallback, override if required
        return this.wdChannels;
    }

    /**
     * Method to get the parent worker data channels of the strand this callback is associated with.
     * @return worker data channels or null
     */
    WDChannels getParentWorkerDataChannels() {
        //used in SafeStrandCallback, override if required
        return this.parentChannels;
    }
}

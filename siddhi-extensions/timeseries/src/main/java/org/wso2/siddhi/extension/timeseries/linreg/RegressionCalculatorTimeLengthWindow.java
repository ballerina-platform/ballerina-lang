/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.extension.timeseries.linreg;

import java.util.PriorityQueue;
import java.util.Queue;

/*
 * Abstract class for regression calculation
 */
public abstract  class RegressionCalculatorTimeLengthWindow {
    protected long duration;
    protected int batchSize;
    protected int calcInterval = 1;
    protected double confidenceInterval = 0.95;
    protected int eventCount = 0;
    protected int xParameterCount = 0;
    protected int incCounter =0;
    protected long currentEventTime;
    protected Queue<Long> expiryTimeList = new PriorityQueue<Long>();

    /**
     * Constructor
     *
     * @param paramCount   Number of X variables + 1 (Y variable)
     * @param timeWindow   Time window which is used to constrain number of events
     * @param lengthWindow Length window which is used to constrain number of events
     * @param calcInt      Frequency of regression calculation
     * @param ci           Confidence interval
     */
    public RegressionCalculatorTimeLengthWindow(int paramCount, long timeWindow, int lengthWindow,
                                                int calcInt, double ci) {
        duration = timeWindow;
        batchSize = lengthWindow;
        calcInterval = calcInt;
        confidenceInterval = ci;
        xParameterCount = paramCount -1;
    }

    /**
     * Abstract method for accumulating events
     *
     * @param data       Array which holds Y and X values
     * @param expiryTime Time at which each event should expire (based on time window)
     */
    protected abstract void addEvent(Object[] data, long expiryTime);

    /**
     * Abstract method for removing events which have expired based on time and/or length
     */
    protected abstract void removeExpiredEvents();

    /**
     * Abstract method for performing linear regression
     * @return Array containing standard error and beta values
     */
    protected abstract Object[] processData();

    /**
     * Method which returns linear regression results
     *
     * @param data       Array which holds Y and X values
     * @param expiryTime Time at which each event should expire (based on time window)
     * @return Array containing standard error and beta values
     */
    public Object[] calculateLinearRegression(Object[] data, long expiryTime) {
        addEvent(data, expiryTime);
        removeExpiredEvents();
        // processing at a user specified calculation interval
        if(incCounter % calcInterval != 0){
            return null;
        }
        return  processData();
    }
}

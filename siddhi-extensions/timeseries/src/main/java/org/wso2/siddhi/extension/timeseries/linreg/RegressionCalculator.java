/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public abstract class RegressionCalculator {

    protected double confidenceInterval = 0.95;
    protected int eventCount = 0;
    protected int xParameterCount = 0;
    protected int batchSize = 1000000000;
    protected int incCounter =0;
    protected int calcInterval;

    public RegressionCalculator(int paramCount, int calcInt, int limit, double ci) {
        confidenceInterval = ci;
        batchSize = limit;
        xParameterCount = paramCount -1;
        calcInterval = calcInt;

    }
    protected abstract void addEvent(Object[] data);

    protected abstract void removeEvent();

    protected abstract Object[] processData();

    public Object[] calculateLinearRegression(Object[] data) {

        addEvent(data);

        // removing oldest events in order to maintain batchsize
        if(eventCount > batchSize){
            eventCount--;
            removeEvent();
        }

        // processing at a user specified calculation interval
        if(incCounter % calcInterval != 0){
            return null;
        }
        return  processData();
    }
}

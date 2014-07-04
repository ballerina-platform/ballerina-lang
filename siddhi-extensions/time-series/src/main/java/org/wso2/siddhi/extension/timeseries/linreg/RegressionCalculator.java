package org.wso2.siddhi.extension.timeseries.linreg;

import org.wso2.siddhi.core.event.in.InEvent;

import java.util.Map;

/**
 * Created by waruna on 4/9/14.
 */

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
    protected abstract void addEvent(InEvent inEvent, Map<Integer, String> paramPositions, int paramCount);

    protected abstract void removeEvent();

    protected abstract Object[] processData();

    public Object[] calculateLinearRegression(InEvent inEvent, Map<Integer, String> paramPositions) {

        addEvent(inEvent, paramPositions, xParameterCount+1);

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

package org.wso2.siddhi.extension.timeseries.linreg;

import org.wso2.siddhi.core.event.in.InEvent;

import java.util.Map;

/**
 * Created by waruna on 4/9/14.
 */
public abstract class RegressionCalculator {

    public abstract void init();

    public abstract void close();

    public abstract void addEvent(InEvent inEvent, Map<Integer, String> paramPositions, int dataCount);

    public abstract Object[] processData();

    public abstract Object[] linearRegressionCalculation ( InEvent inEvent, Map<Integer, String> paramPositions, int dataCount);

}

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

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.math3.distribution.TDistribution;

/*
 * This class handles simple linear regression in real time
 * Simple linear regression concerns single dependent variable (Y) and single independent variable (X)
 */
public class SimpleLinearRegressionCalculatorTimeLengthWindow extends
        RegressionCalculatorTimeLengthWindow {
    private List<Double> xValueList = new LinkedList<Double>();
    private List<Double> yValueList = new LinkedList<Double>();
    private double sumX = 0.0;
    private double sumY = 0.0;
    private double sumXsquared = 0.0;

    /**
     * This method calls the constructor of super class
     *
     * @param paramCount   Number of X variables + 1 (Y variable)
     * @param timeWindow   Time window which is used to constrain number of events
     * @param lengthWindow Length window which is used to constrain number of events
     * @param calcInt      Frequency of regression calculation
     * @param ci           Confidence interval
     */
    public SimpleLinearRegressionCalculatorTimeLengthWindow(int paramCount, long timeWindow,
                                                            int lengthWindow, int calcInt, double ci) {
        super(paramCount, timeWindow, lengthWindow, calcInt, ci);
    }

    /**
     * Method to accumulate new events: dependent (Y) and independent (X) variable values
     *
     * @param data       Array which holds Y and X values
     * @param expiryTime Time at which each event should expire (based on time window)
     */
    @Override
    protected void addEvent(Object[] data, long expiryTime) {
        currentEventTime = expiryTime - duration;
        incCounter++;
        eventCount++;
        double yValue = ((Number) data[0]).doubleValue();
        yValueList.add(yValue);
        double xValue = ((Number) data[1]).doubleValue();
        xValueList.add(xValue);
        expiryTimeList.add(expiryTime);
        sumX += xValue;
        sumXsquared += (xValue * xValue);
        sumY += yValue;
    }

    /**
     * Method to remove events which have expired based on time and/or length
     */
    @Override
    protected void removeExpiredEvents() {
        // removing oldest events in order to maintain time window
        while (expiryTimeList.peek() <= currentEventTime) {
            double xValueRemoved = xValueList.remove(0);
            sumX -= xValueRemoved;
            sumXsquared -= xValueRemoved * xValueRemoved;
            sumY -= yValueList.remove(0);
            expiryTimeList.poll();
            eventCount--;
        }
        // removing oldest event in order to maintain length window
        if (eventCount > batchSize) {
            double xValueRemoved = xValueList.remove(0);
            sumX -= xValueRemoved;
            sumXsquared -= xValueRemoved * xValueRemoved;
            sumY -= yValueList.remove(0);
            expiryTimeList.poll();
            eventCount--;
        }
    }

    /**
     * Method which performs simple linear regression
     *
     * @return Array containing standard error and beta values
     */
    @Override
    protected Object[] processData() {
        Object[] regResult;
        try {
            Double[] xArray = xValueList.toArray(new Double[eventCount]);
            Double[] yArray = yValueList.toArray(new Double[eventCount]);
            double meanX, meanY, varianceX = 0.0, varianceY = 0.0, covarXY = 0.0, beta1, beta0, R2,
                    stderr, beta1err, beta0err, t_beta0, t_beta1, fit;
            double resss = 0.0; // residual sum of squares
            // double regss = 0.0; // regression sum of squares [Required to calculate R2]
            int df = eventCount - 2; // degrees of freedom (n-k-1)
            TDistribution t = new TDistribution(df);
            // compute meanX and meanY
            meanX = sumX / eventCount;
            meanY = sumY / eventCount;
            // compute summary statistics
            for (int i = 0; i < eventCount; i++) {
                varianceX += (xArray[i] - meanX) * (xArray[i] - meanX);
                // varianceY += (yArray[i] - meanY) * (yArray[i] - meanY);//Required to calculate R2
                covarXY += (xArray[i] - meanX) * (yArray[i] - meanY);
            }
            // compute coefficients
            beta1 = covarXY / varianceX;
            beta0 = meanY - beta1 * meanX;
            // analyze results
            for (int i = 0; i < eventCount; i++) {
                fit = beta1 * xArray[i] + beta0;
                resss += (fit - yArray[i]) * (fit - yArray[i]);
                // regss += (fit - meanY) * (fit - meanY); // Required to calculate R2
            }
            // R2 = regss / varianceY; // Will calculate only if a customer asks for it
            // calculating standard errors
            stderr = Math.sqrt(resss / df);
            beta1err = stderr / Math.sqrt(varianceX);
            beta0err = stderr * Math.sqrt(sumXsquared / (eventCount * varianceX));
            // calculating tstats
            t_beta0 = beta0 / beta0err;
            t_beta1 = beta1 / beta1err;
            // Eliminating weak coefficients
            double pValue = 2 * (1 - t.cumulativeProbability(Math.abs(t_beta0)));
            if (pValue > (1 - confidenceInterval)) {
                beta0 = 0;
            }
            pValue = 2 * (1 - t.cumulativeProbability(Math.abs(t_beta1)));
            if (pValue > (1 - confidenceInterval)) {
                beta1 = 0;
            }
            regResult = new Object[] { stderr, beta0, beta1 };
        } catch (Exception e) {
            regResult = new Object[] { 0.0, 0.0, 0.0 };
        }
        return regResult;
    }
}

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
import java.util.PriorityQueue;
import java.util.Queue;
import org.apache.commons.math3.distribution.TDistribution;
import Jama.Matrix;

/*
 * This class handles multiple linear regression in real time
 * Multiple linear regression concerns single dependent variable (Y) and multiple independent
 * variables (X1, X2....Xn)
 */
public class LengthTimeMultipleLinearRegression extends LengthTimeRegressionCalculator {
    private List<double[]> yValueList = new LinkedList<double[]>();
    private List<double[]> xValueList = new LinkedList<double[]>();
    private Queue<Long> expiryTimeList = new PriorityQueue<Long>();

    /**
     * This method calls the constructor of super class
     *
     * @param paramCount   Number of X variables + 1 (Y variable)
     * @param timeWindow   Time window which is used to constrain number of events
     * @param lengthWindow Length window which is used to constrain number of events
     * @param calcInt      Frequency of regression calculation
     * @param ci           Confidence interval
     */
    public LengthTimeMultipleLinearRegression(int paramCount, long timeWindow, int lengthWindow,
                                              int calcInt, double ci) {
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
        double[] dataX = new double[xParameterCount + 1];
        double[] dataY = new double[1];
        dataX[0] = 1.0;
        dataY[0] = ((Number) data[0]).doubleValue();
        for (int i = 1; i <= xParameterCount; i++) {
            dataX[i] = ((Number) data[i]).doubleValue();
        }
        xValueList.add(dataX);
        yValueList.add(dataY);
        expiryTimeList.add(expiryTime);
    }

    /**
     * Method to remove events which have expired based on time and/or length
     */
    @Override
    protected void removeExpiredEvents() {
        // removing oldest events in order to maintain time window
        while (expiryTimeList.peek() <= currentEventTime) {
            yValueList.remove(0);
            xValueList.remove(0);
            expiryTimeList.poll();
            eventCount--;
        }
        // removing oldest event in order to maintain length window
        if (eventCount > batchSize) {
            yValueList.remove(0);
            xValueList.remove(0);
            expiryTimeList.poll();
            eventCount--;
        }
    }

    /**
     * Method which performs multiple linear regression
     *
     * @return Array containing standard error and beta values
     */
    @Override
    protected Object[] processData() {
        double[][] xArray = xValueList.toArray(new double[eventCount][xParameterCount + 1]);
        double[][] yArray = yValueList.toArray(new double[eventCount][1]);
        double[] betaErrors = new double[xParameterCount + 1];
        double[] tStats = new double[xParameterCount + 1];
        double sse = 0.0; // sum of square error
        double df = eventCount - xParameterCount - 1; // Degrees of Freedom for Confidence Interval
        double p = 1 - confidenceInterval; // P value of specified confidence interval
        double pValue;
        Object[] regResults = new Object[xParameterCount + 2];
        // Calculate Betas
        try {
            Matrix matY = new Matrix(yArray);
            Matrix matX = new Matrix(xArray);
            Matrix matXTranspose = matX.transpose();
            Matrix matXTXInverse = matXTranspose.times(matX).inverse();
            Matrix matBetas = matXTXInverse.times(matXTranspose).times(matY);
            Matrix yHat = matX.times(matBetas);
            // Calculate Sum of Squares
            for (int i = 0; i < eventCount; i++) {
                sse += (yHat.get(i, 0) - yArray[i][0]) * (yHat.get(i, 0) - yArray[i][0]);
            }
            // Calculating Errors
            double mse = sse / df;
            regResults[0] = Math.sqrt(mse); // Standard Error of Regression
            TDistribution t = new TDistribution(df);
            // Calculating beta errors and tstats
            for (int j = 0; j <= xParameterCount; j++) {
                betaErrors[j] = Math.sqrt(matXTXInverse.get(j, j) * mse);
                tStats[j] = matBetas.get(j, 0) / betaErrors[j];
                // Eliminating statistically weak coefficients
                pValue = 2 * (1 - t.cumulativeProbability(Math.abs(tStats[j])));
                if (pValue > p) {
                    regResults[j + 1] = 0.0;
                } else {
                    regResults[j + 1] = matBetas.get(j, 0);
                }
            }
        } catch (RuntimeException e) {
            regResults[0] = 0.0;
            for (int j = 0; j <= xParameterCount; j++) {
                regResults[j + 1] = 0.0;
            }
            return regResults;
        }
        return regResults;
    }
}

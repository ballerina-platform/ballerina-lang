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
 */age org.wso2.siddhi.extension.timeseries.linreg;

import org.apache.commons.math3.distribution.TDistribution;

import java.util.LinkedList;
import java.util.List;

public class SimpleLinearRegressionCalculator extends RegressionCalculator{

    private List<Double> xValueList = new LinkedList<Double>();
    private List<Double> yValueList = new LinkedList<Double>();
    private double sumX = 0.0, sumY = 0.0, sumXsquared = 0.0, xValue, yValue;

    public SimpleLinearRegressionCalculator(int paramCount, int calcInt, int limit, double ci) {
        super(paramCount, calcInt, limit, ci);
    }

    @Override
    protected void addEvent(Object[] data) {
        eventCount++;
        incCounter++;

        yValue = ((Number) data[0]).doubleValue();
        yValueList.add(yValue);

        xValue = ((Number) data[1]).doubleValue();
        xValueList.add(xValue);

        sumX += xValue;
        sumXsquared += (xValue*xValue);
        sumY  += yValue;
    }

    @Override
    protected void removeEvent() {

        double xValue = xValueList.remove(0);

        sumX -= xValue;
        sumXsquared += xValue * xValue;
        sumY  -= yValueList.remove(0);
    }

    @Override
    protected Object[] processData() {

        Object[] regResult;
        try {
            Double[] xArray = xValueList.toArray(new Double[eventCount]);
            Double[] yArray = yValueList.toArray(new Double[eventCount]);

            double meanX, meanY, varianceX = 0.0, varianceY = 0.0, covarXY = 0.0, beta1, beta0, R2, stderr, beta1err, beta0err, t_beta0, t_beta1, fit;

            double resss = 0.0;      // residual sum of squares
            // double regss = 0.0;      // regression sum of squares [Required to calculate R2]
            int df = eventCount - 2; // degrees of freedom (n-k-1)
            TDistribution t = new TDistribution(df);

            //  compute meanX and meanY
            meanX = sumX / eventCount;
            meanY = sumY / eventCount;

            // compute summary statistics
            for (int i = 0; i < eventCount; i++) {
                varianceX += (xArray[i] - meanX) * (xArray[i] - meanX);
                // varianceY += (yArray[i] - meanY) * (yArray[i] - meanY);  // Required to calculate R2
                covarXY += (xArray[i] - meanX) * (yArray[i] - meanY);
            }


            //compute coefficients
            beta1 = covarXY / varianceX;
            beta0 = meanY - beta1 * meanX;

            // analyze results
            for (int i = 0; i < eventCount; i++) {
                fit = beta1 * xArray[i] + beta0;
                resss += (fit - yArray[i]) * (fit - yArray[i]);
                // regss += (fit - meanY) * (fit - meanY);  // Required to calculate R2
            }

            // R2 = regss / varianceY;  // Will calculate only if a customer asks for it
            //calculating standard errors
            stderr = Math.sqrt(resss / df);
            beta1err = stderr / Math.sqrt(varianceX);
            beta0err = stderr * Math.sqrt(sumXsquared / (eventCount * varianceX));

            //calculating tstats
            t_beta0 = beta0 / beta0err;
            t_beta1 = beta1 / beta1err;

            // Eliminating weak coefficients
            double pValue = 2 * (1-t.cumulativeProbability(Math.abs(t_beta0)));
            if (pValue > (1 - confidenceInterval)) {
                beta0 = 0;
            }
            pValue = 2 * (1-t.cumulativeProbability(Math.abs(t_beta1)));
            if (pValue > (1 - confidenceInterval)) {
                beta1 = 0;
            }

            regResult = new Object[]{ stderr, beta0, beta1};
        }
        catch (Exception e) {
            regResult = new Object[]{0.0, 0.0, 0.0};
        }
        return regResult;
    }
}

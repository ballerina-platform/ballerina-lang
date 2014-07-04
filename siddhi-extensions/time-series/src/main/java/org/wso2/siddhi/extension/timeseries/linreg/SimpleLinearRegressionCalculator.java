package org.wso2.siddhi.extension.timeseries.linreg;

import org.apache.commons.math3.distribution.TDistribution;
import org.apache.log4j.Logger;
import org.wso2.siddhi.core.event.in.InEvent;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by seshika on 4/9/14.
 */
public class SimpleLinearRegressionCalculator extends RegressionCalculator
{

    static final Logger log = Logger.getLogger(SimpleLinearRegressionCalculator.class);
    private List<Double> xValueList = new LinkedList<Double>();
    private List<Double> yValueList = new LinkedList<Double>();
    private double sumX = 0.0, sumY = 0.0, sumXsquared = 0.0, xValue, yValue;


    public SimpleLinearRegressionCalculator(int paramCount, int calcInt, int limit, double ci)
    {
        super(paramCount, calcInt, limit, ci);
    }


    public void addEvent (InEvent inEvent, Map<Integer, String> paramPositions, int paramCount) {

        eventCount++;
        incCounter++;

        Iterator<Map.Entry<Integer, String>> it = paramPositions.entrySet().iterator();
        yValue = ((Number) inEvent.getData(it.next().getKey())).doubleValue();
        yValueList.add(yValue);

        xValue = ((Number) inEvent.getData(it.next().getKey())).doubleValue();
        xValueList.add(xValue);

        sumX += xValue;
        sumXsquared += (xValue*xValue);
        sumY  += yValue;
    }

    public void removeEvent(){

        double xValue = xValueList.remove(0);

        sumX -= xValue;
        sumXsquared += xValue * xValue;
        sumY  -= yValueList.remove(0);
    }

    public Object[] processData () {

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

package org.wso2.siddhi.extension.timeseries.linreg;

import Jama.Matrix;
import org.apache.commons.math3.distribution.TDistribution;
import org.wso2.siddhi.core.event.in.InEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by seshika on 4/9/14.
 */
public class MultipleLinearRegressionCalculator extends RegressionCalculator{

    private List<double[]> yValueList = new ArrayList<double[]>();
    private List<double[]> xValueList = new ArrayList<double[]>();
    private double confidenceInterval = 0.0;
    private int eventCount = 0;
    private int xArrayLength = 0;

    public MultipleLinearRegressionCalculator() {
//        init();
    }

    public void init() {
    }

    public void close() {
    }

    public Object[] linearRegressionCalculation ( InEvent inEvent, Map<Integer, String> paramPositions, int dataCount) {

        addEvent(inEvent, paramPositions, dataCount);
        return processData();
    }

    public void addEvent (InEvent inEvent, Map<Integer, String> paramPositions, int dataCount) {

        eventCount++;
        double[] dataX = new double[dataCount - 1];
        double[] dataY = new double[1];
        int itr = 0;

        dataX[0] = 1.0;

        for (Map.Entry<Integer, String> entry : paramPositions.entrySet()) {

            if (itr == 0) {
                confidenceInterval = Double.parseDouble(inEvent.getData(entry.getKey()).toString());
            }
            else if (itr == 1) {
                dataY[0] = Double.parseDouble(inEvent.getData(entry.getKey()).toString());
            }
            else {
                dataX[itr - 1] = Double.parseDouble(inEvent.getData(entry.getKey()).toString());
            }
            itr++;
        }

        xValueList.add(dataX);
        yValueList.add(dataY);
        xArrayLength = dataX.length;
    }

    public Object[] processData() {

        double[][] xArray = xValueList.toArray(new double[eventCount][xArrayLength]);
        double[][] yArray = yValueList.toArray(new double[eventCount][1]);

        int parameterCount = xArrayLength - 1; // number of parameters for a given y value
        double [] betaErrors = new double[parameterCount+1];
        double [] tStats = new double[parameterCount+1];
        double sse = 0.0; // sum of square error
        double df = eventCount - parameterCount - 1; // Degrees of Freedom for Confidence Interval
        double p = 1- confidenceInterval; // P value of specified confidence interval
        double pValue;
        int outputDataCount = 1 + (parameterCount + 1) * 2;
        Object[] dataObjArray = new Object[outputDataCount];

        // Calculate Betas
        try{
            Matrix matY = new Matrix(yArray);
            Matrix matX = new Matrix(xArray);
            Matrix matXTranspose = matX.transpose();
            Matrix matXTXInverse = matXTranspose.times(matX).inverse();
            Matrix matBetas = matXTXInverse.times(matXTranspose).times(matY);

            Matrix yHat = matX.times(matBetas);

            // Calculate Sum of Squares
            for (int i = 0; i < eventCount; i++) {
                sse += Math.pow((yHat.get(i,0) - yArray[i][0]),2);
            }

            // Calculating Errors
            double mse = sse/df;
            double stdErr = Math.sqrt(mse);
            dataObjArray[0] = stdErr;
            TDistribution t = new TDistribution(df);

            //Calculating beta errors and tstats
            for(int j=0; j <= parameterCount; j++) {
                betaErrors[j] = Math.sqrt(matXTXInverse.get(j,j) * mse);
                tStats[j] = matBetas.get(j,0)/betaErrors[j];

                pValue = 1-(t.cumulativeProbability(Math.abs(tStats[j])) - 1 + t.cumulativeProbability(Math.abs(tStats[j])));
                if ( pValue > p) {
                    matBetas.set(j,0,0);
                }

                dataObjArray[j+1] = matBetas.get(j,0);
                dataObjArray[j+2+parameterCount] = tStats[j];
            }
        }
        catch(RuntimeException e){
            dataObjArray[0]="Insufficient Data";
        }

        return dataObjArray;
    }
}

/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org)
 * All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.timeseries.extrema.util;


import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class ExtremaCalculator {

    private double bw = 0; // bandwidth
    private double[] kernelValues = null;

    private double Q = 0.000001; //standard deviation of the process noise
    private double R = 0.0001;   //standard deviation of the measurement noise
    private double P = 1;        //prior error covariance
    private double X = 0;        //prior estimate
    private double K;            //Kalman Gain


    public ExtremaCalculator(double Q, double R) {
        this.Q = Q;
        this.R = R;
    }

    public ExtremaCalculator() {

    }

    /**
     * @param x X value for calculate Gaussian Kernel
     * @return Double value of calculated Gaussian kernel
     */
    private Double gaussianKernel(int x) {
        Double kernVal;

        kernVal = (Math.pow(Math.E, (-x * x) / (2.0 * bw * bw)))
                / (bw * Math.sqrt(2 * Math.PI));

        return kernVal;
    }

    /**
     * @param input input Queue
     * @param bw    bandwidth
     * @return Gaussian kernel regression smoothed Queue
     */
    public Queue<Double> smooth(Queue<Double> input, double bw) {
        this.bw = bw;
        Queue<Double> output;

        if (kernelValues == null) {
            kernelValues = new double[input.size() + 2];
            for (int i = 0; i <= input.size(); ++i) {
                kernelValues[i] = gaussianKernel(i);
            }
        }

        int size = input.size();
        Double kernUp;
        Double kernDown;
        Iterator<Double> itr;
        Double price;
        Double kern;
        output = new LinkedList<Double>();

        for (int i = 0; i < size; ++i) {
            kernUp = 0.0;
            kernDown = 0.0;
            itr = input.iterator();
            int j = 0;

            while (itr.hasNext()) {
                price = itr.next();
                if (i - j >= 0) {
                    kern = kernelValues[i - j];
                } else {
                    kern = kernelValues[j - i];
                }
                kernUp += kern * price;
                kernDown += kern;
                ++j;
            }
            output.add(kernUp / kernDown);
        }

        return output;
    }

    /**
     * Finds maximum Local value of a Queue uses rule based approach
     *
     * @param input     queue
     * @param bandwidth Considering neighborhood
     * @return location of the maximum if exist or returns nulls
     */
    public Integer findMax(Queue<Double> input, int bandwidth) {
        int size = input.size();
        Object[] inputArray = input.toArray();
        for (int i = (size - bandwidth) - 1; i >= bandwidth; --i) {
            Double max = Double.MIN_VALUE;
            for (int j = i - bandwidth; j <= i + bandwidth; ++j) {
                if ((Double) inputArray[j] > max) {
                    max = (Double) inputArray[j];
                }
            }
            if (inputArray[i].equals(max)) {
                return i;
            }

        }
        return null;
    }

    /**
     * Finds minimum Local value of a Queue uses rule based approach
     *
     * @param input     queue
     * @param bandwidth Considering neighborhood
     * @return location of the minimum if exist or returns nulls
     */
    public Integer findMin(Queue<Double> input, int bandwidth) {
        int size = input.size();
        Object[] inputArray = input.toArray();
        for (int i = (size - bandwidth) - 1; i >= bandwidth; --i) {
            Double min = Double.MAX_VALUE;
            for (int j = i - bandwidth; j <= i + bandwidth; ++j) {
                if ((Double) inputArray[j] < min) {
                    min = (Double) inputArray[j];
                }
            }

            if (inputArray[i].equals(min)) {
                return i;
            }

        }
        return null;
    }

    /**
     * This is used to have two different bandwidths when finding maximum.
     *
     * @param input
     * @param leftBandwidth
     * @param rightBandwidth
     * @return
     */
    public Integer findMax(Queue<Double> input, int leftBandwidth,
                           int rightBandwidth) {
        int size = input.size();
        Object[] inputArray = input.toArray();
        for (int i = (size - rightBandwidth) - 1; i >= leftBandwidth; --i) {
            Double max = Double.MIN_VALUE;
            for (int j = i - leftBandwidth; j <= i + rightBandwidth; ++j) {
                if ((Double) inputArray[j] > max) {
                    max = (Double) inputArray[j];
                }
            }

            if (inputArray[i].equals(max)) {
                return i;
            }

        }
        return null;
    }

    /**
     * This is used to have two different bandwidths when finding minimum.
     *
     * @param input
     * @param leftBandwidth
     * @param rightBandwidth
     * @return
     */
    public Integer findMin(Queue<Double> input, int leftBandwidth,
                           int rightBandwidth) {
        int size = input.size();
        Object[] inputArray = input.toArray();
        for (int i = (size - rightBandwidth) - 1; i >= leftBandwidth; --i) {
            Double min = Double.MAX_VALUE;
            for (int j = i - leftBandwidth; j <= i + rightBandwidth; ++j) {
                if ((Double) inputArray[j] < min) {
                    min = (Double) inputArray[j];
                }
            }

            if (inputArray[i].equals(min)) {
                return i;
            }

        }
        return null;
    }


    // Kalman filter
    private void measurementUpdate() {
        K = (P + Q) / (P + Q + R);
        P = R * (P + Q) / (R + P + Q);
    }

    public double update(double measurement) {
        measurementUpdate();
        double result = X + (measurement - X) * K;
        X = result;
        return result;
    }

    public Queue<Double> kalmanFilter(Queue<Double> input) {
        Queue<Double> dataOutput = new LinkedList<Double>();
        for (double d : input) {
            dataOutput.add(update(d));
        }
        return dataOutput;
    }


}

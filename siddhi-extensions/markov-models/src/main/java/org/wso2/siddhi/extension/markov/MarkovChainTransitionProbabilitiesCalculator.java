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

package org.wso2.siddhi.extension.markov;

import org.apache.log4j.Logger;
import org.wso2.siddhi.core.exception.OperationNotSupportedException;

import java.io.Serializable;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class MarkovChainTransitionProbabilitiesCalculator implements Serializable {

    private static final long serialVersionUID = -7632888859752532839L;
    private static final Logger log = Logger.getLogger(MarkovChainTransitionProbabilitiesCalculator.class);
    private long durationToKeep;
    private long notificationsHoldLimit;
    private double alertThresholdProbability;
    private int eventCount;
    private Map<String, String> idToLastStates = new HashMap<String, String>();
    private Map<String, Long> idToLastStatesExpiryTime = new LinkedHashMap<String, Long>();
    private MarkovMatrix markovMatrix = new MarkovMatrix();

    /**
     * Constructor for real time training method
     *
     * @param durationToKeep
     * @param alertThresholdProbability
     * @param notificationsHoldLimit
     */
    public MarkovChainTransitionProbabilitiesCalculator(long durationToKeep, double alertThresholdProbability,
            long notificationsHoldLimit) {
        this.durationToKeep = durationToKeep;
        this.notificationsHoldLimit = notificationsHoldLimit;
        this.alertThresholdProbability = alertThresholdProbability;
    }

    /**
     * Constructor for predefined markov matrix method
     *
     * @param durationToKeep
     * @param alertThresholdProbability
     * @param markovMatrixStorageLocation
     */
    public MarkovChainTransitionProbabilitiesCalculator(long durationToKeep, double alertThresholdProbability,
            String markovMatrixStorageLocation) {
        this.durationToKeep = durationToKeep;
        this.alertThresholdProbability = alertThresholdProbability;
        populateMarkovMatrix(markovMatrixStorageLocation);
    }

    /**
     * Method to process new events
     *
     * @param id
     * @param currentState
     * @param hasTrainingEnabled
     * @return Array containing output results
     */
    protected Object[] processData(String id, String currentState, boolean hasTrainingEnabled) {

        eventCount++;
        Object[] markovChainAlert = null;

        String lastState = idToLastStates.get(id);
        updateStates(id, currentState);

        if (eventCount > notificationsHoldLimit) {
            markovChainAlert = getMarkovChainAlert(lastState, currentState);
        }

        if (hasTrainingEnabled && lastState != null) {
            markovMatrix.updateStartStateCount(lastState, 1);
            markovMatrix.updateTransitionCount(lastState, currentState, 1);
        }
        return markovChainAlert;
    }

    /**
     * Method to updateTransitionCount States
     */
    private void updateStates(String id, String currentState) {

        // updateTransitionCount the last state to current state
        idToLastStates.put(id, currentState);

        // updating expiry times
        long currentEventTime = System.currentTimeMillis();
        long expiryTime = currentEventTime + durationToKeep;
        idToLastStatesExpiryTime.put(id, expiryTime);
    }

    /**
     * Method which updateTransitionCount markov matrix and return notifications
     *
     * @return Array containing last state, transition probability and notify values
     */
    private Object[] getMarkovChainAlert(String lastState, String currentState) {

        Double stateTransitionProbability = 0.0;
        String key;
        Double totalCount;
        Double transitionCount;
        boolean notify = false;

        if (lastState != null) {
            key = markovMatrix.getKey(lastState, currentState);
            totalCount = markovMatrix.getStartStateCount().get(lastState);
            transitionCount = markovMatrix.getTransitionCount().get(key);

            if (totalCount != null && totalCount != 0 && transitionCount != null) {
                stateTransitionProbability = transitionCount / totalCount;
            }

            if (stateTransitionProbability <= alertThresholdProbability) {
                notify = true;
            }
        }
        return new Object[] { lastState, stateTransitionProbability, notify };
    }

    /**
     * Method to remove events which have expired based on time
     */
    protected void removeExpiredEvents(long currentEventTime) {

        for (Map.Entry<String, Long> entry : idToLastStatesExpiryTime.entrySet()) {
            if (entry.getValue() <= currentEventTime) {
                idToLastStates.remove(entry.getKey());
            } else {
                break;
            }
        }
    }

    /**
     * Method to populate the markov matrix using a file input
     */
    private void populateMarkovMatrix(String markovMatrixStorageLocation) {

        File file = new File(markovMatrixStorageLocation);
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        BufferedReader bufferedReader = null;
        String[] statesNames = null;
        String key;
        String startState;
        String endState;

        Map<String, Double> transitionCount = new HashMap<String, Double>();
        Map<String, Double> startStateCount = new HashMap<String, Double>();

        try {

            fileInputStream = new FileInputStream(file);
            bufferedInputStream = new BufferedInputStream(fileInputStream);
            bufferedReader = new BufferedReader(new InputStreamReader(bufferedInputStream));
            int rowNumber = 0;
            String row = bufferedReader.readLine();
            if (row != null) {
                statesNames = row.split(",");
            }

            while ((row = bufferedReader.readLine()) != null) {

                if (rowNumber >= statesNames.length) {
                    throw new OperationNotSupportedException(
                            "Number of rows in the matrix should be equal to number of states. please provide a "
                                    + statesNames.length + " x " + statesNames.length + " matrix");
                }

                startState = statesNames[rowNumber];
                String[] values = row.split(",");
                double totalCount = 0.0;

                if (values.length != statesNames.length) {
                    throw new OperationNotSupportedException(
                            "Number of columns in the matrix should be equal to number of states. please provide a "
                                    + statesNames.length + " x " + statesNames.length + " matrix");
                }

                for (String value : values) {
                    try {
                        totalCount = totalCount + Double.parseDouble(value);
                    } catch (NumberFormatException e) {
                        log.error("Exception occurred while reading the data file " + markovMatrixStorageLocation
                                + ". All values in the matrix should be in double", e);
                    }
                }
                startStateCount.put(startState, totalCount);

                for (int j = 0; j < statesNames.length; j++) {
                    endState = statesNames[j];
                    key = markovMatrix.getKey(startState, endState);
                    transitionCount.put(key, Double.parseDouble(values[j]));
                }
                rowNumber++;
            }

        } catch (IOException e) {
            log.error("Exception occurred while reading the data file " + markovMatrixStorageLocation, e);
        } finally {
            closeQuietly(bufferedReader, bufferedInputStream, fileInputStream);
        }
        markovMatrix.setStartStateCount(startStateCount);
        markovMatrix.setTransitionCount(transitionCount);
    }

    private void closeQuietly(BufferedReader bufferedReader, BufferedInputStream bufferedInputStream,
            FileInputStream fileInputStream) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
        } catch (IOException ex) {
            log.error("Exception occurred when closing the file stream of the data file.", ex);
        }

        try {
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
        } catch (IOException ex) {
            log.error("Exception occurred when closing the file stream of the data file.", ex);
        }

        try {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
        } catch (IOException ex) {
            log.error("Exception occurred when closing the file stream of the data file.", ex);
        }
    }
    
}

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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * This implements the markov matrix which is used in markovChain function.
 */
public class MarkovMatrix implements Serializable {

    private static final Logger log = Logger.getLogger(MarkovMatrix.class);
    private static final long serialVersionUID = -6731569100412292454L;
    private Map<String, Double> transitionCount = new HashMap<String, Double>();
    private Map<String, Double> startStateCount = new HashMap<String, Double>();

    public void updateStartStateCount(String startState, double increment) {
        Double currentCount = startStateCount.get(startState);
        startStateCount.put(startState, (currentCount == null ? 0 : currentCount) + increment);

        if (log.isDebugEnabled()) {
            log.debug(String.format("updateStartStateCount: start state: %s count: %s", startState,
                    startStateCount.get(startState)));
        }
    }

    public void updateTransitionCount(String startState, String endState, double increment) {
        String key = getKey(startState, endState);
        Double currentTransitionCount = transitionCount.get(key);
        transitionCount.put(key, (currentTransitionCount == null ? 0 : currentTransitionCount) + increment);

        if (log.isDebugEnabled()) {
            log.debug(String.format("updateTransitionCount: start state: %s end state: %s count: %s total count: %s",
                    startState, endState, increment, transitionCount.get(startState)));
        }
    }

    public Map<String, Double> getTransitionCount() {
        return transitionCount;
    }

    public void setTransitionCount(Map<String, Double> transitionCount) {
        this.transitionCount = transitionCount;
    }

    public Map<String, Double> getStartStateCount() {
        return startStateCount;
    }

    public void setStartStateCount(Map<String, Double> startStateCount) {
        this.startStateCount = startStateCount;
    }

    public String getKey(String startState, String endState) {
        return startState + "%%" + endState;
    }

}

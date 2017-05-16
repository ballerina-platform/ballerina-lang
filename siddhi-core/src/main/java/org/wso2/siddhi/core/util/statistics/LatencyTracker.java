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

package org.wso2.siddhi.core.util.statistics;

/**
 * Calculates the process latency. markIn and markOut is used to denote start
 * and end of processing events respectively. Latency is the time gap between
 * markIn and markOut calls of given thread.
 */
public interface LatencyTracker {
    /**
     * This is to be called when starting the latency calculation
     */
    void markIn();

    /**
     * This is to be called when latency calculation should be stopped
     */
    void markOut();

    /**
     * @return Name of the latency tracker
     */
    String getName();

}


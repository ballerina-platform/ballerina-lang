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

package org.wso2.siddhi.core.config;

import org.wso2.siddhi.core.util.statistics.StatisticsTrackerFactory;

/**
 * Siddhi statistics module related configuration holder
 */
public class StatisticsConfiguration {

    private String matricPrefix = "org.wso2.siddhi";
    private StatisticsTrackerFactory factory;

    public StatisticsConfiguration(StatisticsTrackerFactory factory) {
        this.factory = factory;
    }

    public StatisticsTrackerFactory getFactory() {
        return factory;
    }

    public String getMatricPrefix() {
        return matricPrefix;
    }

    public void setMatricPrefix(String matricPrefix) {
        this.matricPrefix = matricPrefix;
    }
}



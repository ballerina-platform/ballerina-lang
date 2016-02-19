/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.transport.http.netty.statistics;

import org.wso2.carbon.metrics.common.MetricsConfiguration;

import java.util.Locale;

/**
 * Read Metrics Configurations from environment.
 */
public class MetricsEnvConfiguration extends MetricsConfiguration {

    private static final String PREFIX = "METRICS_";

    public MetricsEnvConfiguration() {
    }

    @Override
    public String getProperty(String key) {
        key = convertKey(key);
        String value = null;
        if (System.getProperty(key) != null) {
            value = System.getProperty(key);
        } else if (System.getenv(key) != null) {
            value = System.getenv(key);
        }
        return value;
    }

    private String convertKey(String key) {
        // Convert the key to uppercase and replace all dots with underscore
        return PREFIX.concat(key.toUpperCase(Locale.getDefault()).replaceAll("\\.", "_"));
    }

}

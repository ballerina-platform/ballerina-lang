/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.logging.util;

/**
 * This class defines constants for log levels.
 *
 * @since 0.95.0
 */
public enum BLogLevel {

    OFF(Integer.MAX_VALUE), ERROR(1000), WARN(900), INFO(800), DEBUG(700), TRACE(600), ALL(Integer.MIN_VALUE);

    private int levelValue;

    BLogLevel(int levelValue) {
        this.levelValue = levelValue;
    }

    public int value() {
        return levelValue;
    }

    public static BLogLevel toBLogLevel(String logLevel) {
        BLogLevel level;
        try {
            level = BLogLevel.valueOf(logLevel);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("invalid log level: " + logLevel);
        }
        return level;
    }
}


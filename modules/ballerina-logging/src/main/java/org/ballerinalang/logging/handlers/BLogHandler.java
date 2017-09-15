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

package org.ballerinalang.logging.handlers;

import org.ballerinalang.logging.BLogRecord;
import org.ballerinalang.logging.util.BLogLevel;

/**
 * Base log handler class for Ballerina log API
 *
 * @since 0.94
 */
public abstract class BLogHandler {

    protected BLogLevel level;

    public BLogHandler() {
        this.level = BLogLevel.ALL; // By default, allow any log record to be logged
    }

    /**
     * Writes the log message to a specific destination (i.e: console)
     *
     * @param logRecord BLogRecord instance containing all the necessary data for the log message
     */
    public abstract void publish(BLogRecord logRecord);

    protected boolean isLoggable(BLogLevel level) {
        return this.level.value() <= level.value() ? true : false;
    }

    public BLogLevel getLogLevel() {
        return level;
    }
}

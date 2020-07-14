/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jvm.scheduling;

import org.ballerinalang.logging.util.BLogLevel;

/**
 * Holds log config data of the @{@link Strand}.
 *
 * @since 2.0.0
 */

public class LogContext {

    /**
     * Log level of the @{@link Strand}.
     */
    private BLogLevel logLevel;

    /**
     * Whether the log level should be propagated to the child @{@link Strand}.
     */
    private boolean propagate;

    /**
     * Get the log level.
     *
     * @return log level
     */
    public BLogLevel getLogLevel() {
        return logLevel;
    }

    /**
     * Check whether the log level should be propagated to the child @{@link Strand}.
     *
     * @return propagate
     */
    public boolean isPropagate() {
        return propagate;
    }

    public LogContext(BLogLevel logLevel, boolean prpagate) {
        this.logLevel = logLevel;
        this.propagate = prpagate;
    }
}

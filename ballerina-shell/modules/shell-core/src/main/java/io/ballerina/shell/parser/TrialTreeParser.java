/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.shell.parser;

/**
 * Parses the source code line using a trial based method.
 * The source code is placed in several places and is attempted to parse.
 * This continues until the correct type can be determined.
 *
 * @since 2.0.0
 */
public abstract class TrialTreeParser extends TreeParser {
    protected static final long DEFAULT_TIMEOUT_MS = 100;
    private final long timeOutDurationMs;

    public TrialTreeParser(long timeOutDurationMs) {
        this.timeOutDurationMs = timeOutDurationMs;
    }

    public static TrialTreeParser defaultParser() {
        return defaultParser(DEFAULT_TIMEOUT_MS);
    }

    public static TrialTreeParser defaultParser(long timeOutDurationMs) {
        return new SerialTreeParser(timeOutDurationMs);
    }

    public long getTimeOutDurationMs() {
        return timeOutDurationMs;
    }
}

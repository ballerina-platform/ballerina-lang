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
package org.ballerinalang.siddhi.query.compiler.exception;

import org.ballerinalang.siddhi.query.api.exception.SiddhiAppContextException;
import org.ballerinalang.siddhi.query.api.util.ExceptionUtil;

/**
 * Exception thrown when passer errors occur in siddhi.
 */
public class SiddhiParserException extends RuntimeException implements SiddhiAppContextException {

    private String message;
    private int[] queryContextStartIndex;
    private int[] queryContextEndIndex;
    private String siddhiAppName = null;
    private String siddhiAppPortion = null;

    public SiddhiParserException(String message) {
        super(message);
        this.message = message;
    }

    public SiddhiParserException(String message, Throwable throwable) {
        super(message, throwable);
        this.message = message;
    }

    public SiddhiParserException(Throwable throwable) {
        super(throwable);
    }

    public SiddhiParserException(String message, int[] queryContextStartIndex,
                                 int[] queryContextEndIndex, String siddhiAppName, String siddhiAppString) {
        super(message);
        this.message = message;
        setQueryContextIndexIfAbsent(queryContextStartIndex, queryContextEndIndex, siddhiAppName, siddhiAppString);
    }

    public SiddhiParserException(String message, Throwable throwable, int[] queryContextStartIndex,
                                 int[] queryContextEndIndex) {
        super(message, throwable);
        this.message = message;
        setQueryContextIndexIfAbsent(queryContextStartIndex, queryContextEndIndex, siddhiAppName, null);
    }

    public SiddhiParserException(String message, int[] queryContextStartIndex,
                                 int[] queryContextEndIndex) {
        super(message);
        this.message = message;
        setQueryContextIndexIfAbsent(queryContextStartIndex, queryContextEndIndex, siddhiAppName, null);
    }

    public SiddhiParserException(String message, Throwable throwable, int[] queryContextStartIndex,
                                 int[] queryContextEndIndex, String siddhiAppName, String siddhiAppString) {
        super(message, throwable);
        this.message = message;
        setQueryContextIndexIfAbsent(queryContextStartIndex, queryContextEndIndex, siddhiAppName, siddhiAppString);
    }

    public void setQueryContextIndexIfAbsent(int[] queryContextStartIndex,
                                             int[] queryContextEndIndex, String siddhiAppName, String siddhiAppString) {
        if (this.siddhiAppName == null) {
            this.siddhiAppName = siddhiAppName;
        }
        if (this.queryContextStartIndex == null && this.queryContextEndIndex == null &&
                queryContextStartIndex != null && queryContextEndIndex != null) {
            this.queryContextStartIndex = queryContextStartIndex;
            this.queryContextEndIndex = queryContextEndIndex;
        }
        if (siddhiAppPortion == null && this.queryContextStartIndex != null && this.queryContextEndIndex != null &&
                siddhiAppString != null) {
            this.siddhiAppPortion = ExceptionUtil.getContext(this.queryContextStartIndex, this.queryContextEndIndex,
                    siddhiAppString);
        }
    }

    public int[] getQueryContextStartIndex() {
        return queryContextStartIndex;
    }

    public int[] getQueryContextEndIndex() {
        return queryContextEndIndex;
    }

    @Override
    public String getMessageWithOutContext() {
        return this.message;
    }

    public String getMessage() {
        return ExceptionUtil.getMessageWithContext(siddhiAppName, queryContextStartIndex, queryContextEndIndex,
                siddhiAppPortion, message);
    }
}

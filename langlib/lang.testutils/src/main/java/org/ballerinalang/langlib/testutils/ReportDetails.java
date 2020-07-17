/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.testutils;

/**
 * Class for holding report details.
 *
 * @since 2.0.0
 */

public class ReportDetails {
    private String functionName;
    private String className;
    private String executionTime;
    private String stackTrace;
    private String testStatus;
    private String logFile;

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public void setExecutionTime(String executionTime) {
        this.executionTime = executionTime;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public void setTestStatus(String testStatus) {
        this.testStatus = testStatus;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public String getFunctionName() {
        return this.functionName;
    }

    public String getClassName() {
        return this.className;
    }

    public String getExecutionTime() {
        return this.executionTime;
    }

    public String getStackTrace() {
        return this.stackTrace;
    }

    public String getTestStatus() {
        return this.testStatus;
    }

    public String getLogFile() {
        return this.logFile;
    }
}

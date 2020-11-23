/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects;

/**
 * Abstract class that contains options to consider when compiling the project.
 *
 * The public getters and setters for common options are exposed from this class. Other applicable options
 * be exposed by available project types accordingly.
 */
public abstract class BuildOptions {
    protected String b7aConfigFile;
    protected boolean offline;
    protected boolean skipTests;
    protected boolean experimental;
    protected boolean testReport;
    protected boolean skipLock;
    protected boolean codeCoverage;
    protected boolean observabilityIncluded;

    public boolean isSkipTests() {
        return skipTests;
    }

    public void setSkipTests(boolean skipTests) {
        this.skipTests = skipTests;
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }

    public boolean isExperimental() {
        return experimental;
    }

    public void setExperimental(boolean experimental) {
        this.experimental = experimental;
    }

    public boolean isTestReport() {
        return testReport;
    }

    public void setTestReport(boolean testReport) {
        this.testReport = testReport;
    }

    public String getB7aConfigFile() {
        return b7aConfigFile;
    }

    public void setB7aConfigFile(String b7aConfigFile) {
        this.b7aConfigFile = b7aConfigFile;
    }

    public boolean isSkipLock() {
        return skipLock;
    }

    public boolean isCodeCoverage() {
        return codeCoverage;
    }

    public boolean isObservabilityIncluded() {
        return observabilityIncluded;
    }
}

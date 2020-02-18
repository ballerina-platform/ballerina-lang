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
package org.ballerinalang.test.launcher.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Java class to store and get data from a json (for a test run).
 */
public class TestMetaData {

    private String orgName;
    private String version;
    private String packageName;
    private String packageId;

    private String initFunctionName;
    private String startFunctionName;
    private String stopFunctionName;

    private String testInitFunctionName;
    private String testStartFunctionName;
    private String testStopFunctionName;

    private boolean hasTestablePackages;

    private String sourceRootPath;
    private String sourceFileName;
    private String jarPath;
    private String moduleJarPath;
    private List<String> dependencyJarPaths;

    private HashMap<String, String> callableFunctionNames;
    private HashMap<String, String> testFunctionNames;
    private List<String> beforeSuiteFunctionNames = new ArrayList<>();
    private List<String> afterSuiteFunctionNames = new ArrayList<>();
    private List<String> beforeEachFunctionNames = new ArrayList<>();
    private List<String> afterEachFunctionNames = new ArrayList<>();
    private List<Test> tests = new ArrayList<>();

    public List<String> getDependencyJarPaths() {
        return this.dependencyJarPaths;
    }

    public void setDependencyJarPaths(List<String> dependencyJarPaths) {
        this.dependencyJarPaths = dependencyJarPaths;
    }

    public String getPackageID() {
        return packageId;
    }

    public void setPackageId(String packageId) {

        this.packageId = packageId;
    }

    public String getSourceRootPath() {
        return sourceRootPath;
    }

    public void setSourceRootPath(String sourceRootPath) {
        this.sourceRootPath = sourceRootPath;
    }

    public String getJarPath() {
        return jarPath;
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath;
    }

    public String getModuleJarPath() {
        return moduleJarPath;
    }

    public void setModuleJarPath(String moduleJarPath) {
        this.moduleJarPath = moduleJarPath;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getInitFunctionName() {
        return initFunctionName;
    }

    public void setInitFunctionName(String initFunctionName) {
        this.initFunctionName = initFunctionName;
    }

    public String getStartFunctionName() {
        return startFunctionName;
    }

    public void setStartFunctionName(String startFunctionName) {
        this.startFunctionName = startFunctionName;
    }

    public String getStopFunctionName() {
        return stopFunctionName;
    }

    public void setStopFunctionName(String stopFunctionName) {
        this.stopFunctionName = stopFunctionName;
    }

    public String getTestInitFunctionName() {
        return testInitFunctionName;
    }

    public void setTestInitFunctionName(String testInitFunctionName) {
        this.testInitFunctionName = testInitFunctionName;
    }

    public String getTestStartFunctionName() {
        return testStartFunctionName;
    }

    public void setTestStartFunctionName(String testStartFunctionName) {
        this.testStartFunctionName = testStartFunctionName;
    }

    public String getTestStopFunctionName() {
        return testStopFunctionName;
    }

    public void setTestStopFunctionName(String testStopFunctionName) {
        this.testStopFunctionName = testStopFunctionName;
    }

    public Boolean isHasTestablePackages() {
        return hasTestablePackages;
    }

    public void setHasTestablePackages(Boolean hasTestablePackages) {
        this.hasTestablePackages = hasTestablePackages;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public HashMap<String, String> getCallableFunctionNames() {
        return callableFunctionNames;
    }

    public void setCallableFunctionNames(HashMap<String, String> callableFunctionNames) {
        this.callableFunctionNames = callableFunctionNames;
    }

    public HashMap<String, String> getTestFunctionNames() {
        return testFunctionNames;
    }

    public void setTestFunctionNames(HashMap<String, String> testFunctionNames) {
        this.testFunctionNames = testFunctionNames;
    }

    public String getPackageId() {
        return packageId;
    }

    public List<String> getBeforeSuiteFunctionNames() {
        return beforeSuiteFunctionNames;
    }

    public List<String> getAfterSuiteFunctionNames() {
        return afterSuiteFunctionNames;
    }

    public void setHasTestablePackages(boolean hasTestablePackages) {
        this.hasTestablePackages = hasTestablePackages;
    }

    public void setBeforeSuiteFunctionNames(List<String> beforeSuiteFunctionNames) {
        this.beforeSuiteFunctionNames = beforeSuiteFunctionNames;
    }

    public void setAfterSuiteFunctionNames(List<String> afterSuiteFunctionNames) {
        this.afterSuiteFunctionNames = afterSuiteFunctionNames;
    }

    public List<String> getBeforeEachFunctionNames() {
        return beforeEachFunctionNames;
    }

    public void setBeforeEachFunctionNames(List<String> beforeEachFunctionNames) {
        this.beforeEachFunctionNames = beforeEachFunctionNames;
    }

    public List<String> getAfterEachFunctionNames() {
        return afterEachFunctionNames;
    }

    public void setAfterEachFunctionNames(List<String> afterEachFunctionNames) {
        this.afterEachFunctionNames = afterEachFunctionNames;
    }

    public List<Test> getTests() {
        return tests;
    }

    public void setTests(List<Test> tests) {
        this.tests = tests;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }
}

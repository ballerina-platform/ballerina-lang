/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.testerina.core;

import org.ballerinalang.test.runtime.entity.TestSuite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keep a registry of instances.
 * This is required to modify the runtime behavior.
 */
public class TesterinaRegistry {

    private String orgName = null;
    private String version = null;
    private List<String> groups = new ArrayList<>();
    private boolean shouldIncludeGroups;
    private Map<String, TestSuite> testSuites = new HashMap<>();

    // This is use to keep track of packages that are already inited.
    private List<String> initializedPackages = new ArrayList<>();
    /**
     * This is required to stop the annotation processor from processing annotations upon the compilation of the
     * service skeleton. This flag will make sure that @{@link TestAnnotationProcessor}'s methods will skip the
     * annotation processing once the test suites are compiled.
     */
    private boolean testSuitesCompiled;
    private static TesterinaRegistry instance = new TesterinaRegistry();

    public static TesterinaRegistry getInstance() {
        return instance;
    }

    public Map<String, TestSuite> getTestSuites() {
        return testSuites;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public boolean shouldIncludeGroups() {
        return shouldIncludeGroups;
    }

    public void setShouldIncludeGroups(boolean shouldIncludeGroups) {
        this.shouldIncludeGroups = shouldIncludeGroups;
    }

    public boolean isTestSuitesCompiled() {
        return testSuitesCompiled;
    }

    public void setTestSuitesCompiled(boolean testSuitesCompiled) {
        this.testSuitesCompiled = testSuitesCompiled;
    }

    public void setTestSuites(Map<String, TestSuite> testSuites) {
        this.testSuites = testSuites;
        this.testSuitesCompiled = false;
    }

    public List<String> getInitializedPackages() {
        return initializedPackages;
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
 }

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
package org.ballerinalang.test.runtime.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Final test report with coverage (if enabled).
 *
 * @since 1.2.0
 */
public class TestReport {
    private String projectName;
    private Map<String, ModuleStatus> moduleStatus = new HashMap<>();
    private Map<String, ModuleCoverage> moduleCoverage = new HashMap<>();

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Map<String, ModuleStatus> getModuleStatus() {
        return moduleStatus;
    }

    public void setModuleStatus(Map<String, ModuleStatus> moduleStatus) {
        this.moduleStatus = moduleStatus;
    }

    public void addModuleStatus(String moduleName, ModuleStatus status) {
        this.moduleStatus.put(moduleName, status);
    }

    public Map<String, ModuleCoverage> getModuleCoverage() {
        return moduleCoverage;
    }

    public void setModuleCoverage(Map<String, ModuleCoverage> moduleCoverage) {
        this.moduleCoverage = moduleCoverage;
    }

    public void addCoverage(String moduleName, ModuleCoverage coverage) {
        this.moduleCoverage.put(moduleName, coverage);
    }
}

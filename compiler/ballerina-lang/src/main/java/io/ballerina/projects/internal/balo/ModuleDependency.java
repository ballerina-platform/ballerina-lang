/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.projects.internal.balo;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

/**
 * {@code Dependency} Model for Module Dependency.
 *
 * @since 2.0.0
 */
public class ModuleDependency {
    private static final String PACKAGE_NAME_FIELD = "package_name";
    private static final String MODULE_NAME_FIELD = "module_name";

    private final String org;
    @SerializedName(PACKAGE_NAME_FIELD)
    private final String packageName;
    private final String version;
    @SerializedName(MODULE_NAME_FIELD)
    private final String moduleName;
    private final List<ModuleDependency> dependencies;

    public ModuleDependency(String org,
                            String packageName,
                            String version,
                            String moduleName,
                            List<ModuleDependency> dependencies) {
        this.org = org;
        this.packageName = packageName;
        this.version = version;
        this.moduleName = moduleName;
        this.dependencies = dependencies;
    }

    public ModuleDependency(String org,
                            String packageName,
                            String version,
                            String moduleName) {
        this.org = org;
        this.packageName = packageName;
        this.version = version;
        this.moduleName = moduleName;
        this.dependencies = Collections.emptyList();
    }

    public String getOrg() {
        return org;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getVersion() {
        return version;
    }

    public String getModuleName() {
        return moduleName;
    }

    public List<ModuleDependency> getDependencies() {
        return dependencies;
    }
}

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
package org.ballerinalang.jvm.api.runtime;

import org.ballerinalang.jvm.scheduling.Strand;

/**
 * Represent ballerina module at runtime.
 *
 * @since 2.0.0
 */

public class Module {

    /**
     * Organization name of module @{@link Strand} was initiated.
     */
    private final String moduleOrg;

    /**
     * Name of module @{@link Strand} was initiated.
     */
    private final String moduleName;

    /**
     * Version of module @{@link Strand} was initiated.
     */
    private final String moduleVersion;


    public Module(String moduleOrg, String moduleName, String moduleVersion) {
        this.moduleOrg = moduleOrg;
        this.moduleName = moduleName;
        this.moduleVersion = moduleVersion;
    }


    /**
     * Gets the organization name of module @{@link Strand} was initiated.
     *
     * @return Strand module org name.
     */
    public String getModuleOrg() {
        return moduleOrg;
    }

    /**
     * Gets the name of module @{@link Strand} was initiated.
     *
     * @return Strand module name.
     */
    public String getModuleName() {
        return moduleName;
    }

    /**
     * Gets the version of module @{@link Strand} was initiated.
     *
     * @return Strand module version.
     */
    public String getModuleVersion() {
        return moduleVersion;
    }

}

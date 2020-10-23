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
package io.ballerina.runtime.api.async;

import io.ballerina.runtime.scheduling.Strand;

/**
 * Holds metadata of the @{@link Strand}.
 *
 * @since 2.0.0
 */

public class StrandMetadata {

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

    /**
     * Type name if @{@link Strand} was initiated inside type.
     */
    private final String typeName;

    /**
     * Parent function name where @{@link Strand} was initiated.
     */
    private final String parentFunctionName;

    public StrandMetadata(String moduleOrg, String moduleName, String moduleVersion, String typeName,
                          String parentFunctionName) {
        this.moduleOrg = moduleOrg;
        this.moduleName = moduleName;
        this.moduleVersion = moduleVersion;
        this.typeName = typeName;
        this.parentFunctionName = parentFunctionName;
    }

    public StrandMetadata(String moduleOrg, String moduleName, String moduleVersion, String parentFunctionName) {
        this(moduleOrg, moduleName, moduleVersion, null, parentFunctionName);
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

    /**
     * Gets the type name if @{@link Strand} was initiated inside type.
     *
     * @return Strand type name.
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * Gets the parent function name where @{@link Strand} was initiated.
     *
     * @return Strand parent function name.
     */
    public String getParentFunctionName() {
        return parentFunctionName;
    }
}

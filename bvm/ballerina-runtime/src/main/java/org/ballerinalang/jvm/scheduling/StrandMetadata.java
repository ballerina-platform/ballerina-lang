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
package org.ballerinalang.jvm.scheduling;

import org.ballerinalang.jvm.api.runtime.Module;

/**
 * Holds metadata of the @{@link Strand}.
 *
 * @since 2.0.0
 */

public class StrandMetadata {

    /**
     * Runtime module @{@link Strand} was initiated.
     */
    private final Module bModule;

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
        this.bModule = new Module(moduleOrg, moduleName, moduleVersion);
        this.typeName = typeName;
        this.parentFunctionName = parentFunctionName;
    }

    public StrandMetadata(String moduleOrg, String moduleName, String moduleVersion, String parentFunctionName) {
        this(moduleOrg, moduleName, moduleVersion, null, parentFunctionName);
    }


    /**
     * Gets the runtime module @{@link Strand} was initiated.
     *
     * @return Strand module name.
     */
    public Module getModule() {
        return bModule;
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

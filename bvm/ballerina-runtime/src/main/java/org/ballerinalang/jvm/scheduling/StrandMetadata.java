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

/**
 * Holds metadata of the @{@link Strand}.
 *
 * @since 2.0.0
 */

public class StrandMetadata {

    /**
     * Organization name of module @{@link Strand} was initiated.
     */
    public final String moduleOrg;

    /**
     * Name of module @{@link Strand} was initiated.
     */
    public final String moduleName;

    /**
     * Version of module @{@link Strand} was initiated.
     */
    public final String moduleVersion;

    /**
     * Type Name if @{@link Strand} was initiated inside type.
     */
    public final String typeName;

    /**
     * Parent Function Name where @{@link Strand} was initiated.
     */
    public final String parentFunctionName;

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
}

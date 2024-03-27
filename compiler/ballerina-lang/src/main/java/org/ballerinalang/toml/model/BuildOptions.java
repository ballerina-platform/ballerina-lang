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

package org.ballerinalang.toml.model;

/**
 * Model for Build Options config in Ballerina.toml.
 *
 * @since 2.0.0
 */
public class BuildOptions {
    private boolean observabilityIncluded;
    private boolean runtimeManagementIncluded;
    private boolean enableServicePublish;

    public boolean isObservabilityIncluded() {
        return observabilityIncluded;
    }

    public boolean isRuntimeManagementIncluded() {
        return runtimeManagementIncluded;
    }
    public boolean isEnableServicePublish() {
        return enableServicePublish;
    }

    public void setObservabilityIncluded(boolean observabilityIncluded) {
        this.observabilityIncluded = observabilityIncluded;
    }

    public void setRuntimeManagementIncluded(boolean runtimeManagementIncluded) {
        this.runtimeManagementIncluded = runtimeManagementIncluded;
    }

    public void setEnableServicePublish(boolean enableServicePublish) {
        this.enableServicePublish = enableServicePublish;
    }
}

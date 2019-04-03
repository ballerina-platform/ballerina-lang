/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.compiler.backend.jvm;

/**
 * JBallerina runtime dependency list.
 *
 * @since 0.995.0
 */
public enum RuntimeDependency {

    BALLERINA_RUNTIME("ballerina-runtime"),

    // Todo : Remove ballerina-lang and ballerina-core from jar once native functions are refactored to depend only on
    // ballerina runtime module.
    BALLERINA_CORE("ballerina-core"),

    BALLERINA_LANG("ballerina-lang"),

    BALLERINA_UTILS("ballerina-utils");

    private String name;

    RuntimeDependency(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

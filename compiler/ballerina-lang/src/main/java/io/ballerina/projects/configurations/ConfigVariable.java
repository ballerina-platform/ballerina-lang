/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.configurations;

import org.wso2.ballerinalang.compiler.semantics.model.types.BType;

/**
 * Holder class for configurable variable details.
 */
public class ConfigVariable {

    String name;
    BType type;
    boolean isRequired;

    public ConfigVariable(String name, BType type, boolean isRequired) {
        this.name = name;
        this.type = type;
        this.isRequired = isRequired;
    }

    public String getName() {
        return name;
    }

    public BType getType() {
        return type;
    }

    public boolean isRequired() {
        return isRequired;
    }

}

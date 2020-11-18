/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.internal.configurable;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.types.Type;

import java.util.Objects;

/**
 * Class that represents the key for configurable variables.
 *
 * @since 2.0.0
 */
public class VariableKey {
    Module module;
    String variable;
    Type type;

    public VariableKey(String org, String module, String version, String variable) {
        this.module = new Module(org, module, version);
        this.variable = variable;
        this.type = null;
    }

    public VariableKey(Module module, String variable) {
        this.module = module;
        this.variable = variable;
        this.type = null;
    }

    public VariableKey(String org, String module, String version, String variable, Type type) {
        this.module = new Module(org, module, version);
        this.variable = variable;
        this.type = type;
    }

    public VariableKey(Module module, String variable, Type type) {
        this.module = module;
        this.variable = variable;
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        VariableKey variableKey = (VariableKey) o;
        boolean isEqual = Objects.equals(module, variableKey.module) &&
                Objects.equals(variable, variableKey.variable);
        if (type == null || variableKey.type == null) {
            return isEqual;
        }
        return isEqual && Objects.equals(type, variableKey.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module, variable);
    }
}

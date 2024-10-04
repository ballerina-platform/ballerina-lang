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
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static io.ballerina.runtime.api.constants.RuntimeConstants.ORG_NAME_SEPARATOR;

/**
 * Class that represents the key for configurable variables.
 *
 * @since 2.0.0
 */
public class VariableKey {
    public final Module module;
    public final String variable;
    public final Type type;
    public final boolean isRequired;
    public final String location;

    public VariableKey(String org, String module, String version, String variable, Type type) {
        this(new Module(org, module, version), variable, type, null, false);
    }

    public VariableKey(Module module, String variable, Type type, boolean isRequired) {
        this(module, variable, type, null, isRequired);
    }

    public VariableKey(Module module, String variable, Type type, @Nullable String location, boolean isRequired) {
        this.module = module;
        this.variable = variable;
        this.type = type;
        this.location = location;
        this.isRequired = isRequired;
    }

    public boolean isRequired() {
        return isRequired;
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
        return Objects.equals(module, variableKey.module) &&
                Objects.equals(variable, variableKey.variable);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module, variable);
    }

    @Override
    public String toString() {
        return module.getOrg() + ORG_NAME_SEPARATOR + module.getName() + ":" + variable;
    }
}

/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.runtime.internal.configurable;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.internal.diagnostics.RuntimeDiagnosticLog;

import java.util.Optional;

/**
 *  This interface represents the providers that will be used to retrieve the configuration from user.
 *
 *  @since 2.0.0
 */
public interface ConfigProvider {

    void initialize();

    void complete(RuntimeDiagnosticLog diagnosticLog);

    boolean hasConfigs();

    Optional<ConfigValue> getAsIntAndMark(Module module, VariableKey key);

    Optional<ConfigValue> getAsByteAndMark(Module module, VariableKey key);

    Optional<ConfigValue> getAsBooleanAndMark(Module module, VariableKey key);

    Optional<ConfigValue> getAsFloatAndMark(Module module, VariableKey key);

    Optional<ConfigValue> getAsDecimalAndMark(Module module, VariableKey key);

    Optional<ConfigValue> getAsStringAndMark(Module module, VariableKey key);

    Optional<ConfigValue> getAsArrayAndMark(Module module, VariableKey key);

    Optional<ConfigValue> getAsRecordAndMark(Module module, VariableKey key);

    Optional<ConfigValue> getAsMapAndMark(Module module, VariableKey key);

    Optional<ConfigValue> getAsTableAndMark(Module module, VariableKey key);

    Optional<ConfigValue> getAsUnionAndMark(Module module, VariableKey key);

    Optional<ConfigValue> getAsFiniteAndMark(Module module, VariableKey key);

    Optional<ConfigValue> getAsXmlAndMark(Module module, VariableKey key);

}

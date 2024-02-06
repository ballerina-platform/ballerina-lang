/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.runtime.internal.configurable.providers.env;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.internal.configurable.ConfigProvider;
import io.ballerina.runtime.internal.configurable.ConfigValue;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.diagnostics.RuntimeDiagnosticLog;

import java.util.Map;
import java.util.Optional;

/**
 * This class implements @{@link ConfigProvider} to provide values for configurable variables through environment
 * variables.
 *
 * @since 2201.9.0
 */
public class EnvVarProvider implements ConfigProvider {

    private final Module rootModule;
    private final Map<String, String> envVariables;

    public EnvVarProvider(Module rootModule, Map<String, String> envVariables) {
        this.rootModule = rootModule;
        this.envVariables = envVariables;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void complete(RuntimeDiagnosticLog diagnosticLog) {

    }

    @Override
    public boolean hasConfigs() {
        return false;
    }

    @Override
    public Optional<ConfigValue> getAsIntAndMark(Module module, VariableKey key) {
        return Optional.empty();
    }

    @Override
    public Optional<ConfigValue> getAsByteAndMark(Module module, VariableKey key) {
        return Optional.empty();
    }

    @Override
    public Optional<ConfigValue> getAsBooleanAndMark(Module module, VariableKey key) {
        return Optional.empty();
    }

    @Override
    public Optional<ConfigValue> getAsFloatAndMark(Module module, VariableKey key) {
        return Optional.empty();
    }

    @Override
    public Optional<ConfigValue> getAsDecimalAndMark(Module module, VariableKey key) {
        return Optional.empty();
    }

    @Override
    public Optional<ConfigValue> getAsStringAndMark(Module module, VariableKey key) {
        return Optional.empty();
    }

    @Override
    public Optional<ConfigValue> getAsArrayAndMark(Module module, VariableKey key) {
        return Optional.empty();
    }

    @Override
    public Optional<ConfigValue> getAsRecordAndMark(Module module, VariableKey key) {
        return Optional.empty();
    }

    @Override
    public Optional<ConfigValue> getAsMapAndMark(Module module, VariableKey key) {
        return Optional.empty();
    }

    @Override
    public Optional<ConfigValue> getAsTableAndMark(Module module, VariableKey key) {
        return Optional.empty();
    }

    @Override
    public Optional<ConfigValue> getAsUnionAndMark(Module module, VariableKey key) {
        return Optional.empty();
    }

    @Override
    public Optional<ConfigValue> getAsFiniteAndMark(Module module, VariableKey key) {
        return Optional.empty();
    }

    @Override
    public Optional<ConfigValue> getAsXmlAndMark(Module module, VariableKey key) {
        return Optional.empty();
    }

    @Override
    public Optional<ConfigValue> getAsTupleAndMark(Module module, VariableKey key) {
        return Optional.empty();
    }
}

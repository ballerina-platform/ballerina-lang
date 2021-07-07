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
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
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

    Optional<Long> getAsIntAndMark(Module module, VariableKey key);

    Optional<Integer> getAsByteAndMark(Module module, VariableKey key);

    Optional<Boolean> getAsBooleanAndMark(Module module, VariableKey key);

    Optional<Double> getAsFloatAndMark(Module module, VariableKey key);

    Optional<BDecimal> getAsDecimalAndMark(Module module, VariableKey key);

    Optional<BString> getAsStringAndMark(Module module, VariableKey key);

    Optional<Object> getAsArrayAndMark(Module module, VariableKey key);

    Optional<Object> getAsRecordAndMark(Module module, VariableKey key);

    Optional<Object> getAsMapAndMark(Module module, VariableKey key);

    Optional<Object> getAsTableAndMark(Module module, VariableKey key);

    Optional<Object> getAsUnionAndMark(Module module, VariableKey key);

    Optional<BXml> getAsXmlAndMark(Module module, VariableKey key);

}

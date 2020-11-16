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

package org.ballerinalang.langlib.internal;

import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.configurable.ConfigurableMapHolder;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;

import java.util.Map;

/**
 * Native implementation of lang.internal:configurable.
 *
 * @since 2.0
 */
public class Configurable {
    private static Map<VariableKey, Object> configMap = ConfigurableMapHolder.getConfigurationMap();

    public static Object hasConfigurableValue(BString orgName, BString moduleName, BString versionNumber,
                                              BString configVarName) {
        VariableKey key = new VariableKey(orgName.getValue(), moduleName.getValue(), versionNumber.getValue(),
                configVarName.getValue());
        return configMap.containsKey(key);
    }

    public static Object getConfigurableValue(BString orgName, BString moduleName, BString versionNumber,
                                              BString configVarName) {
        VariableKey key = new VariableKey(orgName.getValue(), moduleName.getValue(), versionNumber.getValue(),
                configVarName.getValue());
        if (configMap.containsKey(key)) {
            return configMap.get(key);
        }
        throw new BLangRuntimeException("Value not found for required configurable variable '" + configVarName +
                "'");
    }
}

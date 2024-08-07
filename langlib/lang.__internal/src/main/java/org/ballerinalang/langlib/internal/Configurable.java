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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTypedesc;
import io.ballerina.runtime.internal.configurable.ConfigMap;
import io.ballerina.runtime.internal.configurable.VariableKey;

import static io.ballerina.runtime.api.creators.ErrorCreator.createError;

/**
 * Native implementation of lang.internal:configurable.
 *
 * @since 2.0
 */
public final class Configurable {

    public static Object hasConfigurableValue(BString orgName, BString moduleName, BString versionNumber,
                                              BString configVarName, BTypedesc t) {
        VariableKey key = new VariableKey(orgName.getValue(), moduleName.getValue(), versionNumber.getValue(),
                configVarName.getValue(), t.getDescribingType());
        return ConfigMap.containsKey(key);
    }

    public static Object getConfigurableValue(BString orgName, BString moduleName, BString versionNumber,
                                              BString configVarName, BTypedesc t) {
        VariableKey key = new VariableKey(orgName.getValue(), moduleName.getValue(), versionNumber.getValue(),
                configVarName.getValue(), t.getDescribingType());
        if (ConfigMap.containsKey(key)) {
            return ConfigMap.get(key);
        }
        configVarName = (moduleName.getValue().equals(".")) ? configVarName :
                StringUtils.fromString(moduleName + ":" + configVarName);
        throw createError(StringUtils
                .fromString("Value not provided for required configurable variable '" + configVarName + "'"));
    }

    private Configurable() {
    }
}

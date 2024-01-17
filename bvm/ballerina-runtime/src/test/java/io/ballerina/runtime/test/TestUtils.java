/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.runtime.test;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.util.RuntimeUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utils class for runtime unit tests.
 * @since 2.0.0
 */
public class TestUtils {

    public static Path getConfigPath(String configFileName) {
        return Paths.get(RuntimeUtils.USER_DIR, "src", "test", "resources", "config_files", configFileName);
    }

    public static Path getConfigPathForNegativeCases(String configFileName) {
        return Paths.get(RuntimeUtils.USER_DIR, "src", "test", "resources", "config_files", "negative", configFileName);
    }

     public static VariableKey[] getSimpleVariableKeys(Module module) {
        VariableKey intVar = new VariableKey(module, "intVar", PredefinedTypes.TYPE_INT, true);
        VariableKey stringVar = new VariableKey(module, "stringVar", PredefinedTypes.TYPE_STRING, true);
        return new VariableKey[]{intVar, stringVar};
    }

    public static BIntersectionType getIntersectionType(Module module, Type type) {
        return new BIntersectionType(module, new Type[]{type, PredefinedTypes.TYPE_READONLY}, type, 1, true);
    }

}

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
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.configurable.ConfigProvider;
import io.ballerina.runtime.internal.configurable.ConfigResolver;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.providers.toml.ConfigTomlProvider;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.util.RuntimeUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Test cases for configuration related implementations.
 */
public class ConfigTest {

    @Test
    public void testTomlConfigProviderWithSimpleTypes() {
        Module module = new Module("myorg", "simple_types", "1.0.0");
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey[] keys = {
                new VariableKey(module, "intVar", PredefinedTypes.TYPE_INT, true),
                new VariableKey(module, "byteVar", PredefinedTypes.TYPE_BYTE, true),
                new VariableKey(module, "floatVar", PredefinedTypes.TYPE_FLOAT, true),
                new VariableKey(module, "stringVar", PredefinedTypes.TYPE_STRING, true),
                new VariableKey(module, "booleanVar", PredefinedTypes.TYPE_BOOLEAN, true),
                new VariableKey(module, "decimalVar", PredefinedTypes.TYPE_DECIMAL, true),
        };
        configVarMap.put(module, keys);

        List<ConfigProvider> supportedConfigProviders = new LinkedList<>();
        supportedConfigProviders.add(new ConfigTomlProvider(getConfigPath("Simple_Types_Config.toml"), configVarMap));
        ConfigResolver configResolver = new ConfigResolver(configVarMap, supportedConfigProviders);
        Map<VariableKey, Object> configValueMap = configResolver.resolveConfigs();

        Assert.assertTrue(configValueMap.get(keys[0]) instanceof Long);
        Assert.assertTrue(configValueMap.get(keys[1]) instanceof Integer);
        Assert.assertTrue(configValueMap.get(keys[2]) instanceof Double);
        Assert.assertTrue(configValueMap.get(keys[3]) instanceof BString);
        Assert.assertTrue(configValueMap.get(keys[4]) instanceof Boolean);
        Assert.assertTrue(configValueMap.get(keys[5]) instanceof BDecimal);

        Assert.assertEquals(((Long) configValueMap.get(keys[0])).intValue(), 42);
        Assert.assertEquals(((Integer) configValueMap.get(keys[1])).intValue(), 5);
        Assert.assertEquals(configValueMap.get(keys[2]), 3.5);
        Assert.assertEquals(((BString) configValueMap.get(keys[3])).getValue(), "abc");
        Assert.assertTrue((Boolean) configValueMap.get(keys[4]));
        Assert.assertEquals(((BDecimal) configValueMap.get(keys[5])).decimalValue(), new BigDecimal("24.87"));
    }

    @Test
    public void testTomlConfigProviderWithArrays() {
        Module module = new Module("myorg", "arrays", "1.0.0");
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey[] keys = {
                new VariableKey(module, "intArr", new BIntersectionType(module, new BType[]{}, TypeCreator
                        .createArrayType(PredefinedTypes.TYPE_INT), 0, false), true),
                new VariableKey(module, "byteArr", new BIntersectionType(module, new BType[]{}, TypeCreator
                        .createArrayType(PredefinedTypes.TYPE_BYTE), 0, false), true),
                new VariableKey(module, "floatArr", new BIntersectionType(module, new BType[]{}, TypeCreator
                        .createArrayType(PredefinedTypes.TYPE_FLOAT), 0, false), true),
                new VariableKey(module, "stringArr", new BIntersectionType(module, new BType[]{}, TypeCreator
                        .createArrayType(PredefinedTypes.TYPE_STRING), 0, false), true),
                new VariableKey(module, "booleanArr", new BIntersectionType(module, new BType[]{}, TypeCreator
                        .createArrayType(PredefinedTypes.TYPE_BOOLEAN), 0, false), true),
                new VariableKey(module, "decimalArr", new BIntersectionType(module, new BType[]{}, TypeCreator
                        .createArrayType(PredefinedTypes.TYPE_DECIMAL), 0, false), true)
        };
        configVarMap.put(module, keys);

        List<ConfigProvider> supportedConfigProviders = new LinkedList<>();
        supportedConfigProviders.add(new ConfigTomlProvider(getConfigPath("Array_Config.toml"), configVarMap));
        ConfigResolver configResolver = new ConfigResolver(configVarMap, supportedConfigProviders);
        Map<VariableKey, Object> configValueMap = configResolver.resolveConfigs();

        Assert.assertTrue(configValueMap.get(keys[0]) instanceof BArray);
        Assert.assertTrue(configValueMap.get(keys[1]) instanceof BArray);
        Assert.assertTrue(configValueMap.get(keys[2]) instanceof BArray);
        Assert.assertTrue(configValueMap.get(keys[3]) instanceof BArray);
        Assert.assertTrue(configValueMap.get(keys[4]) instanceof BArray);
        Assert.assertTrue(configValueMap.get(keys[5]) instanceof BArray);

        Assert.assertEquals(((BArray) configValueMap.get(keys[0])).getIntArray(),
                            new long[]{123456, 1234567, 987654321});
        Assert.assertEquals(((BArray) configValueMap.get(keys[1])).getByteArray(),
                            new byte[]{1, 2, 3});
        Assert.assertEquals(((BArray) configValueMap.get(keys[2])).getFloatArray(),
                            new double[]{9.0, 5.6});
        Assert.assertEquals(((BArray) configValueMap.get(keys[3])).getStringArray(),
                            new String[]{"red", "yellow", "green"});
        Assert.assertEquals(((BArray) configValueMap.get(keys[4])).getBooleanArray(),
                            new boolean[]{true, false, false, true});
        BDecimal[] expectedDecimalArray = new BDecimal[100];
        expectedDecimalArray[0] = ValueCreator.createDecimalValue("8.9");
        expectedDecimalArray[1] = ValueCreator.createDecimalValue("4.5");
        expectedDecimalArray[2] = ValueCreator.createDecimalValue("6.2");
        Assert.assertEquals(((BArray) configValueMap.get(keys[5])).getValues(), expectedDecimalArray);
    }

    private Path getConfigPath(String configFileName) {
        return Paths.get(RuntimeUtils.USER_DIR, "src", "test", "resources", "config_files", configFileName);
    }
}

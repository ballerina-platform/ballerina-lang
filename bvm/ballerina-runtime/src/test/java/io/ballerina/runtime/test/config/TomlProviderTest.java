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

package io.ballerina.runtime.test.config;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BMapInitialValueEntry;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.configurable.ConfigProvider;
import io.ballerina.runtime.internal.configurable.ConfigResolver;
import io.ballerina.runtime.internal.configurable.ConfigValue;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.providers.toml.TomlContentProvider;
import io.ballerina.runtime.internal.configurable.providers.toml.TomlFileProvider;
import io.ballerina.runtime.internal.diagnostics.RuntimeDiagnosticLog;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BType;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static io.ballerina.runtime.api.PredefinedTypes.TYPE_ANYDATA;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_BYTE;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_DECIMAL;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_FLOAT;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_INT;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_STRING;
import static io.ballerina.runtime.api.utils.StringUtils.fromString;
import static io.ballerina.runtime.test.TestUtils.getConfigPath;
import static io.ballerina.runtime.test.TestUtils.getSimpleVariableKeys;
import static io.ballerina.runtime.test.config.ConfigTest.COLOR_ENUM_UNION;

/**
 * Test cases for toml configuration related implementations.
 */
public class TomlProviderTest {

    private static final Module ROOT_MODULE = new Module("rootOrg", "test_module", "1");
    private final Module subModule = new Module("rootOrg", "test_module.util.foo", "1");
    private final Module importedModule = new Module("myOrg", "mod12", "1");

    @Test(dataProvider = "arrays-data-provider")
    public void testConfigurableArrays(VariableKey arrayKey,
                                       Function<BArray, Object[]> arrayGetFunction, Object[] expectedArray) {
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey[] keys = new VariableKey[]{arrayKey};
        configVarMap.put(ROOT_MODULE, keys);
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(configVarMap, diagnosticLog,
                List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("ArrayConfig.toml"),
                        configVarMap.keySet())));
        Map<VariableKey, ConfigValue> configValueMap = configResolver.resolveConfigs();
        Object value = configValueMap.get(arrayKey).getValue();
        Assert.assertTrue(value instanceof BArray, "Non-array value received for variable : " + arrayKey.variable);
        Object[] configuredArrayValues = arrayGetFunction.apply((BArray) value);
        for (int i = 0; i < configuredArrayValues.length; i++) {
            Assert.assertEquals(configuredArrayValues[i], expectedArray[i]);
        }
    }

    @DataProvider(name = "arrays-data-provider")
    public Object[][] arrayDataProvider() {
        Function<BArray, Object[]> intArrayGetFunction = bArray -> new Object[]{bArray.getIntArray()};
        Function<BArray, Object[]> byteArrayGetFunction = bArray -> new Object[]{bArray.getByteArray()};
        Function<BArray, Object[]> floatArrayGetFunction = bArray -> new Object[]{bArray.getFloatArray()};
        Function<BArray, Object[]> stringArrayGetFunction = BArray::getStringArray;
        Function<BArray, Object[]> booleanArrayGetFunction = bArray -> new Object[]{bArray.getBooleanArray()};
        Function<BArray, Object[]> decimalArrayGetFunction = BArray::getValues;
        BDecimal[] expectedDecimalArray = new BDecimal[100];
        expectedDecimalArray[0] = ValueCreator.createDecimalValue("8.9");
        expectedDecimalArray[1] = ValueCreator.createDecimalValue("4.5");
        expectedDecimalArray[2] = ValueCreator.createDecimalValue("6.2");
        return new Object[][]{
                // Int array
                {new VariableKey(ROOT_MODULE, "intArr", new BIntersectionType(ROOT_MODULE, new BType[]{}, TypeCreator
                        .createArrayType(TYPE_INT), 0, false), true), intArrayGetFunction,
                        new long[]{123456, 1234567, 987654321}
                },
                // Byte array
                {new VariableKey(ROOT_MODULE, "byteArr", new BIntersectionType(ROOT_MODULE, new BType[]{}, TypeCreator
                        .createArrayType(TYPE_BYTE), 0, false), true), byteArrayGetFunction,
                        new byte[]{1, 2, 3}},
                // Float array
                {new VariableKey(ROOT_MODULE, "floatArr", new BIntersectionType(ROOT_MODULE, new BType[]{}, TypeCreator
                        .createArrayType(TYPE_FLOAT), 0, false), true), floatArrayGetFunction,
                        new double[]{9.0, 5.6}},
                // String array
                {new VariableKey(ROOT_MODULE, "stringArr", new BIntersectionType(ROOT_MODULE, new BType[]{}, TypeCreator
                        .createArrayType(TYPE_STRING), 0, false), true), stringArrayGetFunction,
                        new String[]{"red", "yellow", "green"}},
                // Boolean array
                {new VariableKey(
                        ROOT_MODULE, "booleanArr", new BIntersectionType(ROOT_MODULE, new BType[]{}, TypeCreator
                        .createArrayType(PredefinedTypes.TYPE_BOOLEAN), 0, false), true), booleanArrayGetFunction,
                        new boolean[]{true, false, false, true}},
                // Decimal array
                {new VariableKey(
                        ROOT_MODULE, "decimalArr", new BIntersectionType(ROOT_MODULE, new BType[]{}, TypeCreator
                        .createArrayType(TYPE_DECIMAL), 0, false), true), decimalArrayGetFunction,
                        expectedDecimalArray}
        };
    }

    @Test(dataProvider = "multi-module-data-provider")
    public void testTomlProviderWithMultipleModules(Map<Module, VariableKey[]> variableMap,
                                                    Map<VariableKey, Object> expectedValues,
                                                    List<ConfigProvider> providers) {
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(variableMap, diagnosticLog, providers);
        Map<VariableKey, ConfigValue> configValueMap = configResolver.resolveConfigs();

        for (Map.Entry<VariableKey, Object> keyEntry : expectedValues.entrySet()) {
            VariableKey key = keyEntry.getKey();
            Object value = configValueMap.get(key).getValue();
            Assert.assertNotNull(value, "value not found for variable : " + key.variable);
            Assert.assertEquals(value, keyEntry.getValue());
        }
    }

    @DataProvider(name = "multi-module-data-provider")
    public Object[][] multiModuleDataProvider() {
        VariableKey[] rootVariableKeys = getSimpleVariableKeys(ROOT_MODULE);
        VariableKey[] subVariableKeys = getSimpleVariableKeys(subModule);
        VariableKey[] importedVariableKeys = getSimpleVariableKeys(importedModule);

        Module subModule2 = new Module("rootOrg", "test_module.mod1", "1");
        VariableKey[] subVariableKeys2 = getSimpleVariableKeys(subModule2);

        Module clashingModule1 = new Module("myOrg", "test_module", "1");
        VariableKey[] clashingVariableKeys1 = getSimpleVariableKeys(clashingModule1);

        Module clashingModule2 = new Module("myOrg", "test_module.util.foo", "1");
        VariableKey[] clashingVariableKeys2 = getSimpleVariableKeys(clashingModule2);

        Module clashingModule3 = new Module("test_module", "util.foo", "1");
        VariableKey[] clashingVariableKeys3 = getSimpleVariableKeys(clashingModule3);

        Module clashingModule4 = new Module("test_module", "util", "1");
        VariableKey[] clashingVariableKeys4 = getSimpleVariableKeys(clashingModule4);

        VariableKey[] rootVariables = {new VariableKey(ROOT_MODULE, "a", PredefinedTypes.TYPE_STRING, true),
                new VariableKey(ROOT_MODULE, "b", PredefinedTypes.TYPE_STRING, true),
                new VariableKey(ROOT_MODULE, "c", PredefinedTypes.TYPE_STRING, true)};
        VariableKey[] subModuleVariables = {new VariableKey(subModule, "a", PredefinedTypes.TYPE_STRING, true),
                new VariableKey(subModule, "b", PredefinedTypes.TYPE_STRING, true),
                new VariableKey(subModule, "c", PredefinedTypes.TYPE_STRING, true)};
        VariableKey[] importedVariables = {new VariableKey(importedModule, "a", PredefinedTypes.TYPE_STRING, true),
                new VariableKey(importedModule, "b", PredefinedTypes.TYPE_STRING, true),
                new VariableKey(importedModule, "c", PredefinedTypes.TYPE_STRING, true)};

        Set<Module> moduleSet = Set.of(ROOT_MODULE);
        return new Object[][]{
                {Map.ofEntries(Map.entry(ROOT_MODULE, rootVariableKeys), Map.entry(subModule, subVariableKeys),
                        Map.entry(importedModule, importedVariableKeys)),
                        Map.ofEntries(Map.entry(rootVariableKeys[0], 42L),
                                Map.entry(rootVariableKeys[1], fromString("abc")),
                                Map.entry(subVariableKeys[0], 24L), Map.entry(subVariableKeys[1],
                                        fromString("world")), Map.entry(importedVariableKeys[0], 99L),
                                Map.entry(importedVariableKeys[1], fromString("imported"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("MultiModuleConfig.toml"),
                                Set.of(ROOT_MODULE, subModule, importedModule)))},
                {Map.ofEntries(Map.entry(subModule, subVariableKeys)),
                        Map.ofEntries(Map.entry(subVariableKeys[0], 89L), Map.entry(subVariableKeys[1],
                                fromString("Hello World!"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("SubModuleConfig.toml"),
                                Set.of(subModule)))},
                {Map.ofEntries(Map.entry(ROOT_MODULE, rootVariableKeys)),
                        Map.ofEntries(Map.entry(rootVariableKeys[0], 77L), Map.entry(rootVariableKeys[1],
                                fromString("test string"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("Config_A.toml"), moduleSet),
                                new TomlFileProvider(ROOT_MODULE, getConfigPath("Config_B.toml"), moduleSet))},
                {Map.ofEntries(Map.entry(ROOT_MODULE, rootVariableKeys)),
                        Map.ofEntries(Map.entry(rootVariableKeys[0], 54L), Map.entry(rootVariableKeys[1],
                                fromString("final string"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("Config_2.toml"), moduleSet),
                                new TomlFileProvider(ROOT_MODULE, getConfigPath("Config_1.toml"), moduleSet))
                },
                {Map.ofEntries(Map.entry(ROOT_MODULE, rootVariableKeys)),
                        Map.ofEntries(Map.entry(rootVariableKeys[0], 26L), Map.entry(rootVariableKeys[1],
                                fromString("root module string"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("Config_root1.toml"),
                                moduleSet))
                },
                {Map.ofEntries(Map.entry(ROOT_MODULE, rootVariableKeys)),
                        Map.ofEntries(Map.entry(rootVariableKeys[0], 26L), Map.entry(rootVariableKeys[1],
                                fromString("root module string"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("Config_root2.toml"),
                                moduleSet))
                },
                {Map.ofEntries(Map.entry(ROOT_MODULE, rootVariableKeys)),
                        Map.ofEntries(Map.entry(rootVariableKeys[0], 26L), Map.entry(rootVariableKeys[1],
                                fromString("root module string"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("Config_root3.toml"),
                                moduleSet))
                },
                {Map.ofEntries(Map.entry(subModule, subVariableKeys)),
                        Map.ofEntries(Map.entry(subVariableKeys[0], 11L), Map.entry(subVariableKeys[1],
                                fromString("module string"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("Config_module1.toml"),
                                Set.of(subModule)))
                },
                {Map.ofEntries(Map.entry(subModule, subVariableKeys)),
                        Map.ofEntries(Map.entry(subVariableKeys[0], 11L), Map.entry(subVariableKeys[1],
                                fromString("module string"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("Config_module2.toml"),
                                Set.of(subModule)))
                },
                {Map.ofEntries(Map.entry(importedModule, importedVariableKeys)),
                        Map.ofEntries(Map.entry(importedVariableKeys[0], 89L),
                                Map.entry(importedVariableKeys[1], fromString("imported string"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("Config_imported.toml"),
                                Set.of(importedModule)))
                },
                {Map.ofEntries(Map.entry(ROOT_MODULE, rootVariableKeys),
                        Map.entry(clashingModule1, clashingVariableKeys1)),
                        Map.ofEntries(Map.entry(rootVariableKeys[0], 98L),
                                Map.entry(rootVariableKeys[1], fromString("xyz")),
                                Map.entry(clashingVariableKeys1[0], 76L),
                                Map.entry(clashingVariableKeys1[1], fromString("lmn"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("ConfigClashingModule1.toml"),
                                Set.of(ROOT_MODULE, clashingModule1)))
                },
                {Map.ofEntries(Map.entry(ROOT_MODULE, rootVariableKeys),
                        Map.entry(clashingModule1, clashingVariableKeys1)),
                        Map.ofEntries(Map.entry(rootVariableKeys[0], 98L),
                                Map.entry(rootVariableKeys[1], fromString("xyz")),
                                Map.entry(clashingVariableKeys1[0], 76L),
                                Map.entry(clashingVariableKeys1[1], fromString("lmn"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("ConfigClashingModuleInline1.toml"),
                                Set.of(ROOT_MODULE, clashingModule1)))
                },
                {Map.ofEntries(Map.entry(ROOT_MODULE, rootVariableKeys),
                        Map.entry(clashingModule1, clashingVariableKeys1)),
                        Map.ofEntries(Map.entry(rootVariableKeys[0], 54L),
                                Map.entry(rootVariableKeys[1], fromString("abc")),
                                Map.entry(clashingVariableKeys1[0], 32L),
                                Map.entry(clashingVariableKeys1[1], fromString("pqr"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("ConfigClashingModule2.toml"),
                                Set.of(ROOT_MODULE, clashingModule1)))
                },
                {Map.ofEntries(Map.entry(ROOT_MODULE, rootVariableKeys),
                        Map.entry(clashingModule1, clashingVariableKeys1)),
                        Map.ofEntries(Map.entry(rootVariableKeys[0], 54L),
                                Map.entry(rootVariableKeys[1], fromString("abc")),
                                Map.entry(clashingVariableKeys1[0], 32L),
                                Map.entry(clashingVariableKeys1[1], fromString("pqr"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("ConfigClashingModuleInline2.toml"),
                                Set.of(ROOT_MODULE, clashingModule1)))
                },
                {Map.ofEntries(Map.entry(subModule, subVariableKeys),
                        Map.entry(clashingModule2, clashingVariableKeys2)),
                        Map.ofEntries(Map.entry(subVariableKeys[0], 12L),
                                Map.entry(subVariableKeys[1], fromString("apple")),
                                Map.entry(clashingVariableKeys2[0], 34L),
                                Map.entry(clashingVariableKeys2[1], fromString("orange"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("ConfigClashingModule3.toml"),
                                Set.of(subModule, clashingModule2)))
                },
                {Map.ofEntries(Map.entry(subModule, subVariableKeys),
                        Map.entry(clashingModule2, clashingVariableKeys2)),
                        Map.ofEntries(Map.entry(subVariableKeys[0], 12L),
                                Map.entry(subVariableKeys[1], fromString("apple")),
                                Map.entry(clashingVariableKeys2[0], 34L),
                                Map.entry(clashingVariableKeys2[1], fromString("orange"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("ConfigClashingModuleInline3.toml"),
                                Set.of(subModule, clashingModule2)))
                },
                {Map.ofEntries(Map.entry(subModule, subVariableKeys),
                        Map.entry(clashingModule2, clashingVariableKeys2)),
                        Map.ofEntries(Map.entry(subVariableKeys[0], 56L),
                                Map.entry(subVariableKeys[1], fromString("green")),
                                Map.entry(clashingVariableKeys2[0], 78L),
                                Map.entry(clashingVariableKeys2[1], fromString("blue"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("ConfigClashingModule4.toml"),
                                Set.of(subModule, clashingModule2)))
                },
                {Map.ofEntries(Map.entry(subModule, subVariableKeys),
                        Map.entry(clashingModule2, clashingVariableKeys2)),
                        Map.ofEntries(Map.entry(subVariableKeys[0], 56L),
                                Map.entry(subVariableKeys[1], fromString("green")),
                                Map.entry(clashingVariableKeys2[0], 78L),
                                Map.entry(clashingVariableKeys2[1], fromString("blue"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("ConfigClashingModuleInline4.toml"),
                                Set.of(subModule, clashingModule2)))
                },
                {Map.ofEntries(Map.entry(subModule, subVariableKeys),
                        Map.entry(clashingModule3, clashingVariableKeys3)),
                        Map.ofEntries(Map.entry(subVariableKeys[0], 11L),
                                Map.entry(subVariableKeys[1], fromString("white")),
                                Map.entry(clashingVariableKeys3[0], 99L),
                                Map.entry(clashingVariableKeys3[1], fromString("black"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("ConfigClashingModule5.toml"),
                                Set.of(subModule, clashingModule3)))
                },
                {Map.ofEntries(Map.entry(ROOT_MODULE, rootVariableKeys),
                        Map.entry(clashingModule3, clashingVariableKeys3)),
                        Map.ofEntries(Map.entry(rootVariableKeys[0], 56L),
                                Map.entry(rootVariableKeys[1], fromString("green")),
                                Map.entry(clashingVariableKeys3[0], 78L),
                                Map.entry(clashingVariableKeys3[1], fromString("blue"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("ConfigClashingModule6.toml"),
                                Set.of(ROOT_MODULE, clashingModule2)))
                },
                {Map.ofEntries(Map.entry(subModule, subVariableKeys), Map.entry(clashingModule2, clashingVariableKeys2),
                        Map.entry(ROOT_MODULE, rootVariableKeys)),
                        Map.ofEntries(Map.entry(subVariableKeys[0], 56L),
                                Map.entry(subVariableKeys[1], fromString("green")),
                                Map.entry(clashingVariableKeys2[0], 78L),
                                Map.entry(clashingVariableKeys2[1], fromString("blue")),
                                Map.entry(rootVariableKeys[0], 90L),
                                Map.entry(rootVariableKeys[1], fromString("red"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("ConfigClashingModule7.toml"),
                                Set.of(ROOT_MODULE, subModule, clashingModule2)))
                },
                {Map.ofEntries(Map.entry(ROOT_MODULE, rootVariableKeys),
                        Map.entry(clashingModule4, clashingVariableKeys4)),
                        Map.ofEntries(Map.entry(rootVariableKeys[0], 100L),
                                Map.entry(rootVariableKeys[1], fromString("aaa")),
                                Map.entry(clashingVariableKeys4[0], 200L),
                                Map.entry(clashingVariableKeys4[1], fromString("bbb"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("ConfigClashingModule8.toml"),
                                Set.of(ROOT_MODULE, clashingModule4)))
                },
                {Map.ofEntries(Map.entry(subModule, subVariableKeys), Map.entry(subModule2, subVariableKeys2),
                        Map.entry(clashingModule3, clashingVariableKeys3)),
                        Map.ofEntries(Map.entry(subVariableKeys[0], 100L),
                                Map.entry(subVariableKeys[1], fromString("aaa")),
                                Map.entry(clashingVariableKeys3[0], 200L),
                                Map.entry(clashingVariableKeys3[1], fromString("bbb")),
                        Map.entry(subVariableKeys2[0], 300L),
                        Map.entry(subVariableKeys2[1], fromString("ccc"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("ConfigClashingModule9.toml"),
                                Set.of(subModule, subModule2, clashingModule3)))
                },
                {Map.ofEntries(Map.entry(ROOT_MODULE, rootVariableKeys), Map.entry(subModule, subVariableKeys),
                        Map.entry(clashingModule3, clashingVariableKeys3), Map.entry(importedModule,
                                importedVariableKeys)),
                        Map.ofEntries(Map.entry(rootVariableKeys[0], 111L),
                                Map.entry(rootVariableKeys[1], fromString("one")),
                                Map.entry(subVariableKeys[0], 222L),
                                Map.entry(subVariableKeys[1], fromString("two")),
                                Map.entry(clashingVariableKeys3[0], 333L),
                                Map.entry(clashingVariableKeys3[1], fromString("three")),
                                Map.entry(importedVariableKeys[0], 444L),
                                Map.entry(importedVariableKeys[1], fromString("four"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("ConfigClashingModule10.toml"),
                                Set.of(ROOT_MODULE, subModule, clashingModule3)))
                },
                {Map.ofEntries(Map.entry(ROOT_MODULE, rootVariables)),
                        Map.ofEntries(Map.entry(rootVariables[0], fromString("value a")),
                                Map.entry(rootVariables[1], fromString("value b")),
                                Map.entry(rootVariables[2], fromString("value c"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("DifferentModuleSections_root.toml"),
                                Set.of(ROOT_MODULE)))
                },
                {Map.ofEntries(Map.entry(subModule, subModuleVariables)),
                        Map.ofEntries(Map.entry(subModuleVariables[0], fromString("value a")),
                                Map.entry(subModuleVariables[1], fromString("value b")),
                                Map.entry(subModuleVariables[2], fromString("value c"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("DifferentModuleSections_sub.toml"),
                                Set.of(subModule)))
                },
                {Map.ofEntries(Map.entry(importedModule, importedVariables)),
                        Map.ofEntries(Map.entry(importedVariables[0], fromString("value a")),
                                Map.entry(importedVariables[1], fromString("value b")),
                                Map.entry(importedVariables[2], fromString("value c"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("DifferentModuleSections_imported" +
                                ".toml"), Set.of(importedModule)))
                },
                {Map.ofEntries(Map.entry(ROOT_MODULE, rootVariables), Map.entry(subModule, subModuleVariables),
                        Map.entry(importedModule, importedVariables)),
                        Map.ofEntries(Map.entry(rootVariables[0], fromString("value a")),
                                Map.entry(rootVariables[1], fromString("value b")),
                                Map.entry(rootVariables[2], fromString("value c")),
                                Map.entry(subModuleVariables[0], fromString("value a")),
                                Map.entry(subModuleVariables[1], fromString("value b")),
                                Map.entry(subModuleVariables[2], fromString("value c")),
                                Map.entry(importedVariables[0], fromString("value a")),
                                Map.entry(importedVariables[1], fromString("value b")),
                                Map.entry(importedVariables[2], fromString("value c"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("DifferentModuleSections.toml"),
                                Set.of(ROOT_MODULE, subModule, importedModule)))
                },
                {Map.ofEntries(Map.entry(ROOT_MODULE, rootVariableKeys),
                        Map.entry(clashingModule1, clashingVariableKeys1)),
                        Map.ofEntries(Map.entry(rootVariableKeys[0], 54L),
                                Map.entry(rootVariableKeys[1], fromString("abc")),
                                Map.entry(clashingVariableKeys1[0], 32L),
                                Map.entry(clashingVariableKeys1[1], fromString("pqr"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("DifferentModuleSections_clash1.toml"),
                                Set.of(ROOT_MODULE, clashingModule1)))
                },
                {Map.ofEntries(Map.entry(subModule, subVariableKeys),
                        Map.entry(clashingModule2, clashingVariableKeys2)),
                        Map.ofEntries(Map.entry(subVariableKeys[0], 12L),
                                Map.entry(subVariableKeys[1], fromString("apple")),
                                Map.entry(clashingVariableKeys2[0], 34L),
                                Map.entry(clashingVariableKeys2[1], fromString("orange"))),
                        List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("DifferentModuleSections_clash2.toml"),
                                Set.of(subModule, clashingModule2)))
                },
        };

    }

    @Test()
    public void testMultiDimensionalArray() {
        ArrayType arrayElementType = TypeCreator.createArrayType(TYPE_INT, true);
        BType elementType =
                new BIntersectionType(ROOT_MODULE, new Type[]{arrayElementType, PredefinedTypes.TYPE_READONLY},
                        arrayElementType, 0, true);
        ArrayType arrayType = TypeCreator.createArrayType(elementType, true);
        VariableKey intArr =
                new VariableKey(ROOT_MODULE, "complexArr", new BIntersectionType(ROOT_MODULE, new Type[]{arrayType
                        , PredefinedTypes.TYPE_READONLY}, arrayType, 0, true), true);
        Map<Module, VariableKey[]> configVarMap = Map.ofEntries(Map.entry(ROOT_MODULE, new VariableKey[]{intArr}));
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        List<ConfigProvider> providers = List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath(
                "MultiDimentionalArray.toml"), Set.of(ROOT_MODULE)));
        ConfigResolver configResolver = new ConfigResolver(configVarMap, diagnosticLog, providers);
        Map<VariableKey, ConfigValue> variableKeyObjectMap = configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getErrorCount(), 0);
        Assert.assertEquals(diagnosticLog.getWarningCount(), 0);
        Object bValue = variableKeyObjectMap.get(intArr).getValue();
        Assert.assertTrue(bValue instanceof BArray);
        BArray bArray = (BArray) bValue;
        Assert.assertTrue(bArray.get(0) instanceof BArray);
        Assert.assertTrue(bArray.get(1) instanceof BArray);
        BArray bArray1 = (BArray) bArray.get(0);
        BArray bArray2 = (BArray) bArray.get(1);
        Assert.assertEquals(bArray1.get(0), 55L);
        Assert.assertEquals(bArray1.get(1), 66L);
        Assert.assertEquals(bArray2.get(0), 11L);
        Assert.assertEquals(bArray2.get(1), 22L);
        Assert.assertEquals(bArray2.get(2), 33L);
    }

    @Test
    public void testModuleAmbiguities() {
        VariableKey[] rootVariableKeys = getSimpleVariableKeys(ROOT_MODULE);

        Module clashingModule3 = new Module("test_module", "util.foo", "1");
        VariableKey[] clashingVariableKeys3 = getSimpleVariableKeys(clashingModule3);

        Map<Module, VariableKey[]> variableMap = Map.ofEntries(Map.entry(ROOT_MODULE, rootVariableKeys),
                Map.entry(clashingModule3, clashingVariableKeys3));
                Map<VariableKey, Object> expectedValues = Map.ofEntries(Map.entry(rootVariableKeys[0], 56L),
                        Map.entry(rootVariableKeys[1], fromString("green")),
                        Map.entry(clashingVariableKeys3[0], 78L),
                        Map.entry(clashingVariableKeys3[1], fromString("blue")));
               List<ConfigProvider> providers =  List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath(
                       "ConfigClashingModule6.toml"), Set.of(ROOT_MODULE, clashingModule3)));
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(variableMap, diagnosticLog, providers);
        Map<VariableKey, ConfigValue> configValueMap = configResolver.resolveConfigs();

        for (Map.Entry<VariableKey, Object> keyEntry : expectedValues.entrySet()) {
            VariableKey key = keyEntry.getKey();
            Object value = configValueMap.get(key).getValue();
            Assert.assertNotNull(value, "value not found for variable : " + key.variable);
            Assert.assertEquals(value, keyEntry.getValue());
        }
    }

    @Test
    public void testRootModuleAmbiguities() {
        VariableKey[] subVariableKeys = getSimpleVariableKeys(subModule);

        Module clashingModule3 = new Module("test_module", "util.foo", "1");
        VariableKey[] clashingVariableKeys3 = getSimpleVariableKeys(clashingModule3);

        Map<Module, VariableKey[]> variableMap = Map.ofEntries(Map.entry(subModule, subVariableKeys),
                Map.entry(clashingModule3, clashingVariableKeys3));
        Map<VariableKey, Object> expectedValues = Map.ofEntries(Map.entry(subVariableKeys[0], 11L),
                        Map.entry(subVariableKeys[1], fromString("white")),
                        Map.entry(clashingVariableKeys3[0], 99L),
                        Map.entry(clashingVariableKeys3[1], fromString("black")));
        List<ConfigProvider> providers =
                List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("ConfigClashingModule5.toml"),
                Set.of(subModule, clashingModule3)));
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(variableMap, diagnosticLog, providers);
        Map<VariableKey, ConfigValue> configValueMap = configResolver.resolveConfigs();

        for (Map.Entry<VariableKey, Object> keyEntry : expectedValues.entrySet()) {
            VariableKey key = keyEntry.getKey();
            Object value = configValueMap.get(key).getValue();
            Assert.assertNotNull(value, "value not found for variable : " + key.variable);
            Assert.assertEquals(value, keyEntry.getValue());
        }
    }

    @Test
    public void testTomlProviderWithString() {
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey intVar = new VariableKey(ROOT_MODULE, "intVar", TYPE_INT, true);
        VariableKey stringVar = new VariableKey(ROOT_MODULE, "stringVar", TYPE_STRING, true);
        VariableKey stringArr = new VariableKey(ROOT_MODULE, "stringArr", new BIntersectionType(ROOT_MODULE,
                new BType[]{}, TypeCreator.createArrayType(TYPE_STRING), 0, false), true);
        VariableKey booleanArr = new VariableKey(ROOT_MODULE, "booleanArr", new BIntersectionType(ROOT_MODULE,
                new BType[]{}, TypeCreator.createArrayType(PredefinedTypes.TYPE_BOOLEAN), 0, false), true);
        configVarMap.put(ROOT_MODULE, new VariableKey[]{intVar, stringVar, stringArr, booleanArr});
        String tomlContent = "[rootOrg.test_module] intVar = 33 stringVar = \"xyz\" " +
                "stringArr = [\"aa\", \"bb\", \"cc\"] booleanArr = [false, true, true, false]";
        ConfigResolver configResolver = new ConfigResolver(configVarMap, new RuntimeDiagnosticLog(),
                List.of(new TomlContentProvider(ROOT_MODULE, tomlContent, configVarMap.keySet())));
        Map<VariableKey, ConfigValue> configValueMap = configResolver.resolveConfigs();

        Object configValue = configValueMap.get(intVar).getValue();
        Assert.assertTrue(configValue instanceof Long, "Value received for variable : " + intVar.variable + " is not " +
                "'Long'");
        Assert.assertEquals(((Long) configValue).intValue(), 33);

        Object bValue = configValueMap.get(stringVar).getValue();
        Assert.assertTrue(bValue instanceof BString,
                "Value received for variable : " + stringVar.variable + " is not 'BString'");
        Assert.assertEquals(((BString) bValue).getValue(), "xyz");

        bValue = configValueMap.get(stringArr).getValue();
        Assert.assertTrue(bValue instanceof BArray, "Value received for variable : " + stringArr.variable + " is not " +
                "'Array'");
        Assert.assertEquals(((BArray) bValue).getStringArray(), new String[]{"aa", "bb", "cc"});

        bValue = configValueMap.get(booleanArr).getValue();
        Assert.assertTrue(bValue instanceof BArray, "Value received for variable : " + booleanArr + " is not 'Array'");
        Assert.assertEquals(((BArray) bValue).getBooleanArray(), new boolean[]{false, true, true, false});
    }

    @Test(dataProvider = "map-data-provider")
    public void testTomlProviderMaps(String variableName, Type constraint, Map<String, Object> expectedValues) {
        MapType type = TypeCreator.createMapType("MapType", constraint, ROOT_MODULE, false);
        IntersectionType mapType = new BIntersectionType(ROOT_MODULE, new Type[]{type, PredefinedTypes.TYPE_READONLY}
                , type, 1, true);
        VariableKey mapVar = new VariableKey(ROOT_MODULE, variableName, mapType, true);
        Map<Module, VariableKey[]> configVarMap = Map.ofEntries(Map.entry(ROOT_MODULE, new VariableKey[]{mapVar}));
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(configVarMap, diagnosticLog,
                List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("MapTypeConfig.toml"),
                        configVarMap.keySet())));
        Map<VariableKey, ConfigValue> configValueMap = configResolver.resolveConfigs();

        Object value = configValueMap.get(mapVar).getValue();
        Assert.assertTrue(value instanceof BMap<?, ?>, "Non-map value received for variable : " + variableName);
        BMap<?, ?> mapValue = (BMap<?, ?>) value;
        for (Map.Entry<String, Object> expectedField : expectedValues.entrySet()) {
            BString fieldName = fromString(expectedField.getKey());
            Assert.assertEquals((mapValue.get(fieldName)), expectedField.getValue());
        }
    }

    @DataProvider(name = "map-data-provider")
    public Object[][] mapDataProvider() {
        return new Object[][]{
                {"intMap", TYPE_INT, Map.ofEntries(Map.entry("int1", 12L), Map.entry("int2", 34L))},
                {"decimalMap", TYPE_DECIMAL,
                        Map.ofEntries(Map.entry("d1", ValueCreator.createDecimalValue("56.78")),
                        Map.entry("d2", ValueCreator.createDecimalValue("32.94")))}
        };
    }

    @Test(dataProvider = "union-data-provider")
    public void testTomlProviderUnions(String variableName, Type type, Object expectedValues) {
        IntersectionType unionType = new BIntersectionType(ROOT_MODULE, new Type[]{type, PredefinedTypes.TYPE_READONLY}
                , type, 1, true);
        VariableKey unionVar = new VariableKey(ROOT_MODULE, variableName, unionType, true);
        Map<Module, VariableKey[]> configVarMap = Map.ofEntries(Map.entry(ROOT_MODULE, new VariableKey[]{unionVar}));
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(configVarMap, diagnosticLog,
                                                           List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath(
                                                                   "UnionTypeConfig.toml"), configVarMap.keySet())));
        Map<VariableKey, ConfigValue> configValueMap = configResolver.resolveConfigs();

        Object value = configValueMap.get(unionVar).getValue();
        Assert.assertEquals(expectedValues, value);
    }

    @DataProvider(name = "union-data-provider")
    public Object[][] unionDataProvider() {
        return new Object[][]{
                // union variable with int value
                {"intStringVar", TypeCreator.createUnionType(List.of(TYPE_INT, TYPE_FLOAT), true), 123456L},
                // union variable with byte value
                {"byteStringVar", TypeCreator.createUnionType(List.of(TYPE_STRING, TYPE_BYTE), true), 5L},
                // union variable with float value
                {"floatIntStringVar", TypeCreator.createUnionType(List.of(TYPE_STRING, TYPE_INT,
                                                                          TYPE_FLOAT), true), 123.45},
                // union variable with string value
                {"stringIntVar", TypeCreator.createUnionType(List.of(TYPE_INT, TYPE_STRING), true),
                        fromString("hello")},
                // union variable with map value
                {"mapUnionVar", TypeCreator.createUnionType(List.of(TYPE_INT,
                                                                    TypeCreator.createMapType(TYPE_ANYDATA)), true),
                        ValueCreator
                                .createMapValue(TypeCreator.createMapType(TYPE_ANYDATA),
                                                new BMapInitialValueEntry[]
                                                        {
                                                                ValueCreator.createKeyFieldEntry(fromString("name"),
                                                                                                 fromString("Waruna")),
                                                                ValueCreator.createKeyFieldEntry(fromString("age"),
                                                                                                 14L),
                                                        })},
                // anydata variable with float value
                {"anyDataPrimitiveTypeVar", TYPE_ANYDATA, 9.87},
                // anydata variable with map value
                {"anyDataStructuredTypeVar",
                        TYPE_ANYDATA, ValueCreator
                        .createMapValue(TypeCreator.createMapType(TYPE_ANYDATA),
                                        new BMapInitialValueEntry[]
                                                {
                                                        ValueCreator.createKeyFieldEntry(fromString("name"),
                                                                                         fromString("Riyafa")),
                                                        ValueCreator.createKeyFieldEntry(fromString("age"), 10L),
                                                })},
                // Enum value given with toml
                {"color", COLOR_ENUM_UNION, StringUtils.fromString("RED")},
        };
    }
}

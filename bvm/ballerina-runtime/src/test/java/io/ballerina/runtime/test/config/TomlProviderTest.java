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
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.internal.configurable.ConfigProvider;
import io.ballerina.runtime.internal.configurable.ConfigResolver;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.providers.toml.TomlContentProvider;
import io.ballerina.runtime.internal.configurable.providers.toml.TomlFileProvider;
import io.ballerina.runtime.internal.diagnostics.DiagnosticLog;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BType;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.ballerina.runtime.test.TestUtils.getConfigPath;

/**
 * Test cases for toml configuration related implementations.
 */
public class TomlProviderTest {

    private static final Module ROOT_MODULE = new Module("rootOrg", "mod12", "1.0.0");
    private final Module module = new Module("myOrg", "test_module", "1.0.0");
    private final Module subModule = new Module("myOrg", "test_module.util.foo", "1.0.0");

    @Test(dataProvider = "arrays-data-provider")
    public void testConfigurableArrays(VariableKey arrayKey,
                                       Function<BArray, Object[]> arrayGetFunction, Object[] expectedArray) {
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey[] keys = new VariableKey[]{arrayKey};
        configVarMap.put(module, keys);
        DiagnosticLog diagnosticLog = new DiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap, diagnosticLog,
                List.of(new TomlFileProvider(getConfigPath("ArrayConfig.toml"))));
        Map<VariableKey, Object> configValueMap = configResolver.resolveConfigs();
        Assert.assertTrue(configValueMap.get(arrayKey) instanceof BArray,
                "Non-array value received for variable : " + arrayKey.variable);
        Object[] configuredArrayValues = arrayGetFunction.apply((BArray) configValueMap.get(arrayKey));
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
                {new VariableKey(module, "intArr", new BIntersectionType(module, new BType[]{}, TypeCreator
                        .createArrayType(PredefinedTypes.TYPE_INT), 0, false), true), intArrayGetFunction,
                        new long[]{123456, 1234567, 987654321}
                },
                // Byte array
                {new VariableKey(module, "byteArr", new BIntersectionType(module, new BType[]{}, TypeCreator
                        .createArrayType(PredefinedTypes.TYPE_BYTE), 0, false), true), byteArrayGetFunction,
                        new byte[]{1, 2, 3}},
                // Float array
                {new VariableKey(module, "floatArr", new BIntersectionType(module, new BType[]{}, TypeCreator
                        .createArrayType(PredefinedTypes.TYPE_FLOAT), 0, false), true), floatArrayGetFunction,
                        new double[]{9.0, 5.6}},
                // String array
                {new VariableKey(module, "stringArr", new BIntersectionType(module, new BType[]{}, TypeCreator
                        .createArrayType(PredefinedTypes.TYPE_STRING), 0, false), true), stringArrayGetFunction,
                        new String[]{"red", "yellow", "green"}},
                // Boolean array
                {new VariableKey(module, "booleanArr", new BIntersectionType(module, new BType[]{}, TypeCreator
                        .createArrayType(PredefinedTypes.TYPE_BOOLEAN), 0, false), true), booleanArrayGetFunction,
                        new boolean[]{true, false, false, true}},
                // Decimal array
                {new VariableKey(module, "decimalArr", new BIntersectionType(module, new BType[]{}, TypeCreator
                        .createArrayType(PredefinedTypes.TYPE_DECIMAL), 0, false), true), decimalArrayGetFunction,
                        expectedDecimalArray}
        };
    }

    @Test(dataProvider = "record-data-provider")
    public void testTomlProviderRecords(String variableName, Map<String, Field> fields,
                                        Map<String, Object> expectedValues) {
        RecordType type = TypeCreator.createRecordType("Person", module, SymbolFlags.READONLY, fields, null, true, 6);
        VariableKey recordVar = new VariableKey(module, variableName, type, true);
        Map<Module, VariableKey[]> configVarMap = Map.ofEntries(Map.entry(module, new VariableKey[]{recordVar}));
        DiagnosticLog diagnosticLog = new DiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap, diagnosticLog,
                List.of(new TomlFileProvider(getConfigPath("RecordTypeConfig.toml"))));
        Map<VariableKey, Object> configValueMap = configResolver.resolveConfigs();

        Assert.assertTrue(configValueMap.get(recordVar) instanceof BMap<?, ?>,
                "Non-map value received for variable : " + variableName);
        BMap<?, ?> record = (BMap<?, ?>) configValueMap.get(recordVar);
        for (Map.Entry<String, Object> expectedField : expectedValues.entrySet()) {
            BString fieldName = StringUtils.fromString(fields.get(expectedField.getKey()).getFieldName());
            Assert.assertEquals((record.get(fieldName)), expectedField.getValue());
        }
    }

    @DataProvider(name = "record-data-provider")
    public Object[][] recordDataProvider() {
        Field name = TypeCreator.createField(PredefinedTypes.TYPE_STRING, "name", SymbolFlags.REQUIRED);
        Field nameReadOnly = TypeCreator.createField(PredefinedTypes.TYPE_STRING, "name", SymbolFlags.READONLY);
        Field age = TypeCreator.createField(PredefinedTypes.TYPE_INT, "age", SymbolFlags.OPTIONAL);
        return new Object[][]{
                {"requiredFieldRecord", Map.ofEntries(Map.entry("name", name)),
                        Map.ofEntries(Map.entry("name", StringUtils.fromString("John")))},
                {"readonlyFieldRecord", Map.ofEntries(Map.entry("name", nameReadOnly)),
                        Map.ofEntries(Map.entry("name", StringUtils.fromString("Jade")))},
                {"optionalFieldRecord", Map.ofEntries(Map.entry("name", nameReadOnly), Map.entry("age", age)),
                        Map.ofEntries(Map.entry("name", StringUtils.fromString("Anna")), Map.entry("age", 21L))},
                {"optionalMissingField", Map.ofEntries(Map.entry("name", nameReadOnly), Map.entry("age", age)),
                        Map.ofEntries(Map.entry("name", StringUtils.fromString("Peter")))},
        };
    }

    @Test(dataProvider = "table-data-provider")
    public void testTomlProviderTables(String variableName, Map<String, Field> fields, String key, Map<String,
            Object>[] expectedValues) {
        RecordType type = TypeCreator.createRecordType("Person", module, SymbolFlags.READONLY, fields, null, true, 6);
        TableType tableType = TypeCreator.createTableType(type, new String[]{key}, true);
        IntersectionType intersectionType = new BIntersectionType(module, new Type[]{tableType,
                PredefinedTypes.TYPE_READONLY}, tableType, 1, true);

        VariableKey tableVar = new VariableKey(module, variableName, intersectionType, true);
        Map<Module, VariableKey[]> configVarMap = Map.ofEntries(Map.entry(module, new VariableKey[]{tableVar}));
        DiagnosticLog diagnosticLog = new DiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap, diagnosticLog,
                List.of(new TomlFileProvider(getConfigPath("TableTypeConfig.toml"))));
        Map<VariableKey, Object> configValueMap = configResolver.resolveConfigs();

        Assert.assertTrue(configValueMap.get(tableVar) instanceof BTable<?, ?>, "Non-table value received for " +
                "variable : " + variableName);
        BTable<?, ?> table = (BTable<?, ?>) configValueMap.get(tableVar);

        for (Map<String, Object> tableEntry : expectedValues) {
            Object keyValue = tableEntry.get(key);
            Assert.assertTrue(table.containsKey(keyValue), "The key '" + key + "'is not found in table");
            BMap<?, ?> record = (BMap<?, ?>) table.get(keyValue);
            for (Map.Entry<String, Object> expectedField : tableEntry.entrySet()) {
                BString fieldName = StringUtils.fromString(fields.get(expectedField.getKey()).getFieldName());
                Assert.assertEquals((record.get(fieldName)), expectedField.getValue());
            }
        }
    }

    @DataProvider(name = "table-data-provider")
    public Object[][] tableDataProvider() {
        Field name = TypeCreator.createField(PredefinedTypes.TYPE_STRING, "name", SymbolFlags.REQUIRED);
        Field nameReadOnly = TypeCreator.createField(PredefinedTypes.TYPE_STRING, "name", SymbolFlags.READONLY);
        Field age = TypeCreator.createField(PredefinedTypes.TYPE_INT, "age", SymbolFlags.OPTIONAL);
        return new Object[][]{
                {"requiredFieldTable", Map.ofEntries(Map.entry("name", name)), "name",
                        new Map[]{Map.ofEntries(Map.entry("name", StringUtils.fromString("AAA"))),
                                Map.ofEntries(Map.entry("name", StringUtils.fromString("BBB"))),
                                Map.ofEntries(Map.entry("name", StringUtils.fromString("CCC")))}},
                {"readonlyFieldTable", Map.ofEntries(Map.entry("name", nameReadOnly)), "name",
                        new Map[]{Map.ofEntries(Map.entry("name", StringUtils.fromString("Tom"))),
                                Map.ofEntries(Map.entry("name", StringUtils.fromString("Daniel"))),
                                Map.ofEntries(Map.entry("name", StringUtils.fromString("Emma")))}},
                {"optionalFieldTable", Map.ofEntries(Map.entry("name", nameReadOnly), Map.entry("age", age)), "name",
                        new Map[]{
                                Map.ofEntries(Map.entry("name", StringUtils.fromString("Ann")), Map.entry("age", 21L)),
                                Map.ofEntries(Map.entry("name", StringUtils.fromString("Bob"))),
                                Map.ofEntries(Map.entry("name", StringUtils.fromString("Charlie")), Map.entry("age",
                                        23L))}},
        };
    }

    @Test(dataProvider = "multi-module-data-provider")
    public void testTomlProviderWithMultipleModules(Map<Module, VariableKey[]> variableMap,
                                                    Map<VariableKey, Object> expectedValues,
                                                    List<ConfigProvider> providers) {
        DiagnosticLog diagnosticLog = new DiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, variableMap, diagnosticLog, providers);
        Map<VariableKey, Object> configValueMap = configResolver.resolveConfigs();

        for (Map.Entry<VariableKey, Object> keyEntry : expectedValues.entrySet()) {
            VariableKey key = keyEntry.getKey();
            Object value = configValueMap.get(key);
            Assert.assertNotNull(value, "value not found for variable : " + key.variable);
            Assert.assertEquals(value, keyEntry.getValue());
        }
    }

    @DataProvider(name = "multi-module-data-provider")
    public Object[][] multiModuleDataProvider() {
        VariableKey intVar = new VariableKey(module, "intVar", PredefinedTypes.TYPE_INT, true);
        VariableKey stringVar = new VariableKey(module, "stringVar", PredefinedTypes.TYPE_STRING, true);
        VariableKey[] variableKeys = {intVar, stringVar};

        VariableKey subIntVar = new VariableKey(subModule, "intVar", PredefinedTypes.TYPE_INT, true);
        VariableKey subStringVar = new VariableKey(subModule, "stringVar", PredefinedTypes.TYPE_STRING, true);
        VariableKey[] subVariableKeys = {subIntVar, subStringVar};

        return new Object[][]{
                {Map.ofEntries(Map.entry(module, variableKeys), Map.entry(subModule, subVariableKeys)),
                        Map.ofEntries(Map.entry(intVar, 42L), Map.entry(stringVar, StringUtils.fromString("abc")),
                                Map.entry(subIntVar, 24L), Map.entry(subStringVar, StringUtils.fromString("world"))),
                        List.of(new TomlFileProvider(getConfigPath("MultiModuleConfig.toml")))},
                {Map.ofEntries(Map.entry(subModule, subVariableKeys)),
                        Map.ofEntries(Map.entry(subIntVar, 89L), Map.entry(subStringVar, StringUtils.fromString(
                                "Hello World!"))),
                        List.of(new TomlFileProvider(getConfigPath("SubModuleConfig.toml")))},
                {Map.ofEntries(Map.entry(module, variableKeys)),
                        Map.ofEntries(Map.entry(intVar, 77L), Map.entry(stringVar, StringUtils.fromString(
                                "test string"))),
                        List.of(new TomlFileProvider(getConfigPath("Config_A.toml")),
                                new TomlFileProvider(getConfigPath("Config_B.toml")))},
                {Map.ofEntries(Map.entry(module, variableKeys)),
                        Map.ofEntries(Map.entry(intVar, 54L), Map.entry(stringVar, StringUtils.fromString(
                                "final string"))),
                        List.of(new TomlFileProvider(getConfigPath("Config_2.toml")),
                                new TomlFileProvider(getConfigPath("Config_1.toml")))
                }

        };

    }

    @Test
    public void testTomlProviderWithString() {
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey[] keys = {
                new VariableKey(module, "intVar", PredefinedTypes.TYPE_INT, true),
                new VariableKey(module, "stringVar", PredefinedTypes.TYPE_STRING, true),
                new VariableKey(module, "stringArr", new BIntersectionType(module, new BType[]{}, TypeCreator
                        .createArrayType(PredefinedTypes.TYPE_STRING), 0, false), true),
                new VariableKey(module, "booleanArr", new BIntersectionType(module, new BType[]{}, TypeCreator
                        .createArrayType(PredefinedTypes.TYPE_BOOLEAN), 0, false), true),
        };
        configVarMap.put(module, keys);
        String tomlContent = "[test_module] intVar = 33 stringVar = \"xyz\" " +
                "stringArr = [\"aa\", \"bb\", \"cc\"] booleanArr = [false, true, true, false]";
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap, new DiagnosticLog(),
                List.of(new TomlContentProvider(tomlContent)));
        Map<VariableKey, Object> configValueMap = configResolver.resolveConfigs();

        Assert.assertTrue(configValueMap.get(keys[0]) instanceof Long,
                "Value received for variable : " + keys[0].variable + "is not 'Long'");
        Assert.assertTrue(configValueMap.get(keys[1]) instanceof BString,
                "Value received for variable : " + keys[1].variable + "is not 'BString'");
        Assert.assertTrue(configValueMap.get(keys[2]) instanceof BArray,
                "Value received for variable : " + keys[2].variable + "is not 'Array'");
        Assert.assertTrue(configValueMap.get(keys[3]) instanceof BArray,
                "Value received for variable : " + keys[3].variable + "is not 'Array'");

        Assert.assertEquals(((Long) configValueMap.get(keys[0])).intValue(), 33);
        Assert.assertEquals(((BString) configValueMap.get(keys[1])).getValue(), "xyz");
        Assert.assertEquals(((BArray) configValueMap.get(keys[2])).getStringArray(),
                new String[]{"aa", "bb", "cc"});
        Assert.assertEquals(((BArray) configValueMap.get(keys[3])).getBooleanArray(),
                new boolean[]{false, true, true, false});
    }

    @Test
    public void testTomlProviderWithStringNegative() {
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey[] keys = getSimpleVariableKeys(module);
        String tomlContent = "[test_module] intVar = 42.22 floatVar = 3 stringVar = 11";
        configVarMap.put(module, keys);
        DiagnosticLog diagnosticLog = new DiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap, diagnosticLog,
                List.of(new TomlContentProvider(tomlContent)));
        configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getErrorCount(), 2);
        Assert.assertEquals(diagnosticLog.getDiagnosticList().get(0).toString(), "error: [BAL_CONFIG_DATA:(1:24,1:29)" +
                "] configurable variable 'test_module:intVar' is expected to be of type 'int', but found 'float'");
        Assert.assertEquals(diagnosticLog.getDiagnosticList().get(1).toString(), "error: [BAL_CONFIG_DATA:(1:55,1:57)" +
                "] configurable variable 'test_module:stringVar' is expected to be of type 'string', but found 'int'");
    }

    @Test
    public void testMultipleTomlProvidersNegative() {
        Map<Module, VariableKey[]> configVarMap = Map.ofEntries(Map.entry(module, getSimpleVariableKeys(module)));
        DiagnosticLog diagnosticLog = new DiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap, diagnosticLog,
                List.of(new TomlFileProvider(getConfigPath("Config_First.toml")),
                        new TomlFileProvider(getConfigPath("Config_Second.toml"))));
        configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getErrorCount(), 1);
        Assert.assertEquals(diagnosticLog.getDiagnosticList().get(0).toString(), "error: value not provided for " +
                "required configurable variable 'myOrg/test_module:stringVar'");
    }

    private VariableKey[] getSimpleVariableKeys(Module module) {
        VariableKey intVar = new VariableKey(module, "intVar", PredefinedTypes.TYPE_INT, true);
        VariableKey stringVar = new VariableKey(module, "stringVar", PredefinedTypes.TYPE_STRING, true);
        return new VariableKey[]{intVar, stringVar};
    }

}

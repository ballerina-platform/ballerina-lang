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

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
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
        Assert.assertTrue(configValueMap.get(arrayKey) instanceof BArray);
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

    @Test
    public void testTomlProviderTables() {
        Field name = TypeCreator.createField(PredefinedTypes.TYPE_STRING, "name", SymbolFlags.REQUIRED);
        Field age = TypeCreator.createField(PredefinedTypes.TYPE_INT, "age", SymbolFlags.OPTIONAL);
        Map<String, Field> fields = Map.ofEntries(Map.entry("name", name), Map.entry("age", age));
        RecordType type = TypeCreator.createRecordType("Person", module, SymbolFlags.READONLY, fields, null, true, 6);
        TableType tableType = TypeCreator.createTableType(type, new String[]{"name"}, true);
        IntersectionType intersectionType = new BIntersectionType(module, new Type[]{tableType,
                PredefinedTypes.TYPE_READONLY}, tableType, 1, true);

        VariableKey tableVar = new VariableKey(module, "tableVar", intersectionType, true);
        Map<Module, VariableKey[]> configVarMap = Map.ofEntries(Map.entry(module, new VariableKey[]{tableVar}));
        DiagnosticLog diagnosticLog = new DiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap, diagnosticLog,
                List.of(new TomlFileProvider(getConfigPath("TableTypeConfig.toml"))));
        Map<VariableKey, Object> configValueMap = configResolver.resolveConfigs();

        Assert.assertTrue(configValueMap.get(tableVar) instanceof BTable<?, ?>);
        BTable<?, ?> table = (BTable<?, ?>) configValueMap.get(tableVar);

        Assert.assertTrue(table.containsKey("Elsa"));
        Assert.assertTrue(table.get("Elsa") instanceof BMap);
        BMap<BString, Object> person1 = (BMap<BString, Object>) table.get("Elsa");

        BString nameField = StringUtils.fromString("name");
        Assert.assertTrue(person1.containsKey(nameField));
        Assert.assertTrue(person1.get(nameField) instanceof BString);
        Assert.assertEquals(((BString) person1.get(nameField)).getValue(), "Elsa");

        BString ageField = StringUtils.fromString("age");
        Assert.assertTrue(person1.containsKey(ageField));
        Assert.assertTrue(person1.get(ageField) instanceof Long);
        Assert.assertEquals(((Long) person1.get(ageField)).intValue(), 34);


        Assert.assertTrue(table.containsKey("Jack"));
        Assert.assertTrue(table.get("Jack") instanceof BMap);
        BMap<BString, Object> person2 = (BMap<BString, Object>) table.get("Jack");

        BString nameField2 = StringUtils.fromString("name");
        Assert.assertTrue(person2.containsKey(nameField2));
        Assert.assertTrue(person2.get(nameField2) instanceof BString);
        Assert.assertEquals(((BString) person2.get(nameField2)).getValue(), "Jack");

        BString ageField2 = StringUtils.fromString("age");
        Assert.assertFalse(person2.containsKey(ageField2));
        Assert.assertNull(person2.get(ageField2));
    }

    @Test
    public void testTomlProviderWithMultipleModules() {
        VariableKey intVar = new VariableKey(module, "intVar", PredefinedTypes.TYPE_INT, true);
        VariableKey stringVar = new VariableKey(module, "stringVar", PredefinedTypes.TYPE_STRING, true);
        VariableKey subIntVar = new VariableKey(subModule, "intVar", PredefinedTypes.TYPE_INT, true);
        VariableKey subStringVar = new VariableKey(subModule, "stringVar", PredefinedTypes.TYPE_STRING, true);
        Map<Module, VariableKey[]> configVarMap =
                Map.ofEntries(Map.entry(module, new VariableKey[]{intVar, stringVar}), Map.entry(subModule,
                        new VariableKey[]{subIntVar, subStringVar}));

        DiagnosticLog diagnosticLog = new DiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap, diagnosticLog,
                List.of(new TomlFileProvider(getConfigPath("MultiModuleConfig.toml"))));
        Map<VariableKey, Object> configValueMap = configResolver.resolveConfigs();

        Object intValue = configValueMap.get(intVar);
        Assert.assertTrue(intValue instanceof Long);
        Assert.assertEquals(((Long) intValue).intValue(), 42);

        Object stringValue = configValueMap.get(stringVar);
        Assert.assertTrue(stringValue instanceof BString);
        Assert.assertEquals(((BString) stringValue).getValue(), "abc");

        Object subIntValue = configValueMap.get(subIntVar);
        Assert.assertTrue(subIntValue instanceof Long);
        Assert.assertEquals(((Long) subIntValue).intValue(), 24);

        Object subStringValue = configValueMap.get(subStringVar);
        Assert.assertTrue(subStringValue instanceof BString);
        Assert.assertEquals(((BString) subStringValue).getValue(), "world");
    }

    @Test
    public void testTomlProviderForOnlySubModules() {
        VariableKey byteVar = new VariableKey(subModule, "byteVar", PredefinedTypes.TYPE_BYTE, true);
        VariableKey decimalVar = new VariableKey(subModule, "decimalVar", PredefinedTypes.TYPE_DECIMAL, true);
        Map<Module, VariableKey[]> configVarMap =
                Map.ofEntries(Map.entry(subModule, new VariableKey[]{byteVar, decimalVar}));
        DiagnosticLog diagnosticLog = new DiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap, diagnosticLog,
                List.of(new TomlFileProvider(getConfigPath("SubModuleConfig.toml"))));
        Map<VariableKey, Object> configValueMap = configResolver.resolveConfigs();

        Object byteValue = configValueMap.get(byteVar);
        Assert.assertTrue(byteValue instanceof Integer);
        Assert.assertEquals(((Integer) byteValue).intValue(), 67);

        Object decimalValue = configValueMap.get(decimalVar);
        Assert.assertTrue(decimalValue instanceof BDecimal);
        Assert.assertEquals(((BDecimal) decimalValue).decimalValue(), new BigDecimal("4.88"));
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
        List<ConfigProvider> supportedConfigProviders = new LinkedList<>();
        supportedConfigProviders.add(new TomlContentProvider(tomlContent));
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap, new DiagnosticLog(),
                supportedConfigProviders);
        Map<VariableKey, Object> configValueMap = configResolver.resolveConfigs();

        Assert.assertTrue(configValueMap.get(keys[0]) instanceof Long);
        Assert.assertTrue(configValueMap.get(keys[1]) instanceof BString);
        Assert.assertTrue(configValueMap.get(keys[2]) instanceof BArray);
        Assert.assertTrue(configValueMap.get(keys[3]) instanceof BArray);

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
        VariableKey[] keys = {
                new VariableKey(module, "intVar", PredefinedTypes.TYPE_INT, true),
                new VariableKey(module, "stringVar", PredefinedTypes.TYPE_STRING, true),
        };
        String tomlContent = "[test_module] intVar = 42.22 floatVar = 3 stringVar = 11";
        configVarMap.put(module, keys);
        List<ConfigProvider> supportedConfigProviders = new LinkedList<>();
        supportedConfigProviders.add(new TomlContentProvider(tomlContent));
        DiagnosticLog diagnosticLog = new DiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap, diagnosticLog,
                supportedConfigProviders);
        configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getErrorCount(), 2);
        Assert.assertEquals(diagnosticLog.getDiagnosticList().get(0).toString(), "error: [BAL_CONFIG_DATA:(1:24,1:29)" +
                "] configurable variable 'test_module:intVar' is expected to be of type 'int', but found 'float'");
        Assert.assertEquals(diagnosticLog.getDiagnosticList().get(1).toString(), "error: [BAL_CONFIG_DATA:(1:55,1:57)" +
                "] configurable variable 'test_module:stringVar' is expected to be of type 'string', but found 'int'");
    }

    @Test
    public void testMultipleTomlProviders() {
        VariableKey intVar = new VariableKey(module, "intVar", PredefinedTypes.TYPE_INT, true);
        VariableKey stringVar = new VariableKey(module, "stringVar", PredefinedTypes.TYPE_STRING, true);
        Map<Module, VariableKey[]> configVarMap =
                Map.ofEntries(Map.entry(module, new VariableKey[]{intVar, stringVar}));
        List<ConfigProvider> supportedConfigProviders = new LinkedList<>();
        supportedConfigProviders.add(new TomlFileProvider(getConfigPath("Config_A.toml")));
        supportedConfigProviders.add(new TomlFileProvider(getConfigPath("Config_B.toml")));
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap, new DiagnosticLog(),
                supportedConfigProviders);
        Map<VariableKey, Object> configValueMap = configResolver.resolveConfigs();

        Assert.assertTrue(configValueMap.get(intVar) instanceof Long);
        Assert.assertTrue(configValueMap.get(stringVar) instanceof BString);
        Assert.assertEquals(((Long) configValueMap.get(intVar)).intValue(), 77);
        Assert.assertEquals(((BString) configValueMap.get(stringVar)).getValue(), "test string");
    }

    @Test
    public void testMultipleTomlProvidersOverriding() {
        VariableKey intVar = new VariableKey(module, "intVar", PredefinedTypes.TYPE_INT, true);
        VariableKey stringVar = new VariableKey(module, "stringVar", PredefinedTypes.TYPE_STRING, true);
        Map<Module, VariableKey[]> configVarMap =
                Map.ofEntries(Map.entry(module, new VariableKey[]{intVar, stringVar}));
        List<ConfigProvider> supportedConfigProviders = new LinkedList<>();
        supportedConfigProviders.add(new TomlFileProvider(getConfigPath("Config_2.toml")));
        supportedConfigProviders.add(new TomlFileProvider(getConfigPath("Config_1.toml")));
        DiagnosticLog diagnosticLog = new DiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap, diagnosticLog ,
                supportedConfigProviders);
        Map<VariableKey, Object> configValueMap = configResolver.resolveConfigs();

        Assert.assertTrue(configValueMap.get(intVar) instanceof Long);
        Assert.assertTrue(configValueMap.get(stringVar) instanceof BString);
        Assert.assertEquals(((Long) configValueMap.get(intVar)).intValue(), 54);
        Assert.assertEquals(((BString) configValueMap.get(stringVar)).getValue(), "final string");
    }

    @Test
    public void testMultipleTomlProvidersNegative() {
        VariableKey intVar = new VariableKey(module, "intVar", PredefinedTypes.TYPE_INT, true);
        VariableKey stringVar = new VariableKey(module, "stringVar", PredefinedTypes.TYPE_STRING, true);
        Map<Module, VariableKey[]> configVarMap =
                Map.ofEntries(Map.entry(module, new VariableKey[]{intVar, stringVar}));
        List<ConfigProvider> supportedConfigProviders = new LinkedList<>();
        supportedConfigProviders.add(new TomlFileProvider(getConfigPath("Config_First.toml")));
        supportedConfigProviders.add(new TomlFileProvider(getConfigPath("Config_Second.toml")));
        DiagnosticLog diagnosticLog = new DiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap, diagnosticLog,
                supportedConfigProviders);
        configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getErrorCount(), 1);
        Assert.assertEquals(diagnosticLog.getDiagnosticList().get(0).toString(), "error: value not provided for " +
                "required configurable variable 'myOrg/test_module:stringVar'");
    }

}

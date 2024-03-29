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
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.TupleType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.configurable.ConfigProvider;
import io.ballerina.runtime.internal.configurable.ConfigResolver;
import io.ballerina.runtime.internal.configurable.ConfigValue;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.providers.cli.CliProvider;
import io.ballerina.runtime.internal.configurable.providers.env.EnvVarProvider;
import io.ballerina.runtime.internal.configurable.providers.toml.TomlFileProvider;
import io.ballerina.runtime.internal.diagnostics.RuntimeDiagnosticLog;
import io.ballerina.runtime.internal.types.BFiniteType;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BUnionType;
import io.ballerina.runtime.internal.values.DecimalValue;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.runtime.api.PredefinedTypes.TYPE_ANYDATA;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_STRING;
import static io.ballerina.runtime.test.TestUtils.getConfigPath;
import static io.ballerina.runtime.test.TestUtils.getConfigPathForNegativeCases;

/**
 * Test cases for configuration related implementations.
 */
public class ConfigTest {

    private static final Module module = new Module("myOrg", "test_module", "1");

    private static final Module ROOT_MODULE = new Module("rootOrg", "mod12", "1");
    private static final List<Type> COLOR_ENUM_MEMBERS = List.of(
            new BFiniteType("COLOR_RED", Set.of(StringUtils.fromString("RED")), 0),
            new BFiniteType("COLOR_GREEN", Set.of(StringUtils.fromString("GREEN")), 0));
    public static final Type COLOR_ENUM_UNION = new BUnionType(COLOR_ENUM_MEMBERS, "Colors", ROOT_MODULE,
            0, false, SymbolFlags.ENUM);
    public static final Type COLOR_ENUM = new BIntersectionType(module, new Type[]{}, COLOR_ENUM_UNION, 0, true);
    public static final Type AMBIGUOUS_UNION = new BUnionType(Arrays.asList(TypeCreator.createMapType(TYPE_ANYDATA),
            TypeCreator.createMapType(TYPE_STRING)), true);
    private final Set<Module> moduleSet = Set.of(module);

    @Test(dataProvider = "simple-type-values-data-provider")
    public void testTomlConfigProviderWithSimpleTypes(VariableKey key, Class<?> expectedJClass,
                                                      Object expectedValue, ConfigProvider... configProvider) {
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey[] keys = {key};
        configVarMap.put(module, keys);
        ConfigResolver configResolver = new ConfigResolver(configVarMap, diagnosticLog,
                Arrays.asList(configProvider));
        Map<VariableKey, ConfigValue> configValueMap = configResolver.resolveConfigs();
        Assert.assertTrue(expectedJClass.isInstance(configValueMap.get(key).getValue()),
                "Invalid value provided for variable : " + key.variable);
        Assert.assertEquals(configValueMap.get(key).getValue(), expectedValue);
    }

    @DataProvider(name = "simple-type-values-data-provider")
    public Object[][] simpleTypeConfigProviders() {
        return new Object[][]{
                // Int value given only with toml
                {new VariableKey(module, "intVar", PredefinedTypes.TYPE_INT, true), Long.class, 42L,
                        new TomlFileProvider(ROOT_MODULE, getConfigPath("SimpleTypesConfig.toml"), moduleSet)},
                // Byte value given only with toml
                {new VariableKey(module, "byteVar", PredefinedTypes.TYPE_BYTE, true), Integer.class, 5,
                        new TomlFileProvider(ROOT_MODULE, getConfigPath("SimpleTypesConfig.toml"), moduleSet)},
                // Float value given only with toml
                {new VariableKey(module, "floatVar", PredefinedTypes.TYPE_FLOAT, true), Double.class, 3.5,
                        new TomlFileProvider(ROOT_MODULE, getConfigPath("SimpleTypesConfig.toml"), moduleSet)},
                // String value given only with toml
                {new VariableKey(module, "stringVar", PredefinedTypes.TYPE_STRING, true), BString.class,
                        StringUtils.fromString("abc"), new TomlFileProvider(ROOT_MODULE,
                        getConfigPath("SimpleTypesConfig.toml"), moduleSet)},
                // Boolean value given only with toml
                {new VariableKey(module, "booleanVar", PredefinedTypes.TYPE_BOOLEAN, true), Boolean.class, true,
                        new TomlFileProvider(ROOT_MODULE, getConfigPath("SimpleTypesConfig.toml"), moduleSet)},
                // Decimal value given only with toml
                {new VariableKey(module, "decimalVar", PredefinedTypes.TYPE_DECIMAL, true), DecimalValue.class,
                        new DecimalValue("24.87"), new TomlFileProvider(ROOT_MODULE,
                        getConfigPath("SimpleTypesConfig.toml"), moduleSet)},
                // Int value given only with cli
                {new VariableKey(module, "intVar", PredefinedTypes.TYPE_INT, true), Long.class, 123L,
                        new CliProvider(ROOT_MODULE, "-CmyOrg.test_module.intVar=123")},
                // Byte value given only with cli
                {new VariableKey(module, "byteVar", PredefinedTypes.TYPE_BYTE, true), Integer.class, 7,
                        new CliProvider(ROOT_MODULE, "-CmyOrg.test_module.byteVar=7")},
                // Float value given only with cli
                {new VariableKey(module, "floatVar", PredefinedTypes.TYPE_FLOAT, true), Double.class, 99.9,
                        new CliProvider(ROOT_MODULE, "-CmyOrg.test_module.floatVar=99.9")},
                // String value given only with cli
                {new VariableKey(module, "stringVar", PredefinedTypes.TYPE_STRING, true), BString.class,
                        StringUtils.fromString("efg"),
                        new CliProvider(ROOT_MODULE, "-CmyOrg.test_module.stringVar=efg")},
                // Boolean value given only with cli
                {new VariableKey(module, "booleanVar", PredefinedTypes.TYPE_BOOLEAN, true), Boolean.class, false,
                        new CliProvider(ROOT_MODULE, "-CmyOrg.test_module.booleanVar=0")},
                // Decimal value given only with cli
                {new VariableKey(module, "decimalVar", PredefinedTypes.TYPE_DECIMAL, true), DecimalValue.class,
                        new DecimalValue("876.54"),
                        new CliProvider(ROOT_MODULE, "-CmyOrg.test_module.decimalVar=876.54")},
                // Multiple provider but use the first registered provider ( CLI arg as final value)
                {new VariableKey(module, "intVar", PredefinedTypes.TYPE_INT, true), Long.class, 42L,
                        new CliProvider(ROOT_MODULE, "-CmyOrg.test_module.intVar=13579"),
                        new TomlFileProvider(ROOT_MODULE, getConfigPath("SimpleTypesConfig.toml"), moduleSet)},
                // Multiple provider but use the first registered provider ( Toml file value as final value)
                {new VariableKey(module, "intVar", PredefinedTypes.TYPE_INT, true), Long.class, 13579L,
                        new TomlFileProvider(ROOT_MODULE, getConfigPath("SimpleTypesConfig.toml"), moduleSet),
                        new CliProvider(ROOT_MODULE, "-CmyOrg.test_module.intVar=13579")},
                // Enum value given with cli
                {new VariableKey(module, "color", COLOR_ENUM,
                        true), BString.class, StringUtils.fromString("GREEN"),
                        new CliProvider(ROOT_MODULE, "-CmyOrg.test_module.color=GREEN")},
                // Int value given only with env var
                {new VariableKey(module, "intVar", PredefinedTypes.TYPE_INT, true), Long.class, 123L,
                        new EnvVarProvider(ROOT_MODULE, Map.of("BAL_CONFIG_VAR_MYORG_TEST_MODULE_INTVAR", "123"))},
                // Byte value given only with env var
                {new VariableKey(module, "byteVar", PredefinedTypes.TYPE_BYTE, true), Integer.class, 7,
                        new EnvVarProvider(ROOT_MODULE, Map.of("BAL_CONFIG_VAR_MYORG_TEST_MODULE_BYTEVAR", "7"))},
                // Float value given only with env var
                {new VariableKey(module, "floatVar", PredefinedTypes.TYPE_FLOAT, true), Double.class, 99.9,
                        new EnvVarProvider(ROOT_MODULE, Map.of("BAL_CONFIG_VAR_MYORG_TEST_MODULE_FLOATVAR", "99.9"))},
                // String value given only with env var
                {new VariableKey(module, "stringVar", PredefinedTypes.TYPE_STRING, true), BString.class,
                        StringUtils.fromString("efg"),
                        new EnvVarProvider(ROOT_MODULE, Map.of("BAL_CONFIG_VAR_MYORG_TEST_MODULE_STRINGVAR", "efg"))},
                // Boolean value given only with env var
                {new VariableKey(module, "booleanVar", PredefinedTypes.TYPE_BOOLEAN, true), Boolean.class, false,
                        new EnvVarProvider(ROOT_MODULE, Map.of("BAL_CONFIG_VAR_MYORG_TEST_MODULE_BOOLEANVAR", "0"))},
                // Decimal value given only with env var
                {new VariableKey(module, "decimalVar", PredefinedTypes.TYPE_DECIMAL, true), DecimalValue.class,
                        new DecimalValue("876.54"),
                        new EnvVarProvider(ROOT_MODULE, Map.of("BAL_CONFIG_VAR_MYORG_TEST_MODULE_DECIMALVAR",
                                "876.54"))},
                // Multiple provider but use the first registered provider ( env var as final value)
                {new VariableKey(module, "intVar", PredefinedTypes.TYPE_INT, true), Long.class, 42L,
                        new EnvVarProvider(ROOT_MODULE, Map.of("BAL_CONFIG_VAR_MYORG_TEST_MODULE_INTVAR", "13579")),
                        new CliProvider(ROOT_MODULE, "-CmyOrg.test_module.intVar=13677"),
                        new TomlFileProvider(ROOT_MODULE, getConfigPath("SimpleTypesConfig.toml"), moduleSet)},
                // Multiple provider but use the first registered provider ( Toml file value as final value)
                {new VariableKey(module, "intVar", PredefinedTypes.TYPE_INT, true), Long.class, 13579L,
                        new TomlFileProvider(ROOT_MODULE, getConfigPath("SimpleTypesConfig.toml"), moduleSet),
                        new CliProvider(ROOT_MODULE, "-CmyOrg.test_module.intVar=13677"),
                        new EnvVarProvider(ROOT_MODULE, Map.of("BAL_CONFIG_VAR_MYORG_TEST_MODULE_INTVAR", "13579"))},
                // Enum value given with env var
                {new VariableKey(module, "color", COLOR_ENUM,
                        true), BString.class, StringUtils.fromString("GREEN"),
                        new EnvVarProvider(ROOT_MODULE, Map.of("BAL_CONFIG_VAR_MYORG_TEST_MODULE_COLOR", "GREEN"))},
        };
    }

    @Test(dataProvider = "not-supported-provider")
    public void testCLIArgUnsupportedErrors(String variableName, Type type, String expectedValue, int errors) {
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey variableKey = new VariableKey(ROOT_MODULE, variableName, type, null, true);
        configVarMap.put(ROOT_MODULE,
                new VariableKey[]{new VariableKey(ROOT_MODULE, "intVar", PredefinedTypes.TYPE_INT, null
                        , false), variableKey});
        ConfigResolver configResolver = new ConfigResolver(configVarMap, diagnosticLog,
                List.of(new CliProvider(ROOT_MODULE, "-CintVar=22"),
                        new TomlFileProvider(ROOT_MODULE, getConfigPathForNegativeCases(
                                "UnsupportedCliConfig.toml"), Set.of(ROOT_MODULE))));
        Map<VariableKey, ConfigValue> valueMap = configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getWarningCount(), 0);
        Assert.assertEquals(diagnosticLog.getErrorCount(), errors);
        ConfigValue configValue = valueMap.get(variableKey);
        if (configValue == null) {
            Assert.assertNull(expectedValue);
        } else {
            Assert.assertEquals(configValue.getValue().toString(), expectedValue);
        }

    }

    @DataProvider(name = "not-supported-provider")
    public Object[][] notSupportedDataProvider() {
        ArrayType arrayType = TypeCreator.createArrayType(PredefinedTypes.TYPE_INT);
        TupleType tupleType =
                TypeCreator.createTupleType(List.of(PredefinedTypes.TYPE_INT, PredefinedTypes.TYPE_STRING),
                        null, 0, true);
        Field name = TypeCreator.createField(PredefinedTypes.TYPE_STRING, "name", SymbolFlags.REQUIRED);
        Map<String, Field> fields = Map.ofEntries(Map.entry("name", name));
        RecordType recordType = TypeCreator.createRecordType("Person", ROOT_MODULE, SymbolFlags.READONLY, fields,
                null, true, 6);
        MapType mapType = TypeCreator.createMapType(PredefinedTypes.TYPE_INT, true);
        TableType tableType = TypeCreator.createTableType(mapType, true);

        return new Object[][]{
                {"a", new BIntersectionType(ROOT_MODULE, new Type[]{arrayType, PredefinedTypes.TYPE_READONLY},
                        arrayType, 0, true), "[2,3,4]", 5},
                {"b", new BIntersectionType(ROOT_MODULE, new Type[]{tupleType, PredefinedTypes.TYPE_READONLY},
                        tupleType, 0, true), "[5,\"hello\"]", 5},
                {"c", new BIntersectionType(ROOT_MODULE, new Type[]{recordType, PredefinedTypes.TYPE_READONLY},
                        recordType, 0, true), null, 7},
                {"d", new BIntersectionType(ROOT_MODULE, new Type[]{mapType, PredefinedTypes.TYPE_READONLY},
                        mapType, 0, true), "{\"a\":1}", 4},
                {"e", new BIntersectionType(ROOT_MODULE, new Type[]{tableType, PredefinedTypes.TYPE_READONLY},
                        tableType, 0, true), "[{\"aa\":2}]", 4},
        };
    }

    @Test(dataProvider = "not-supported-provider")
    public void testEnvVarUnsupportedErrors(String variableName, Type type, String expectedValue, int errors) {
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey variableKey = new VariableKey(ROOT_MODULE, variableName, type, null, true);
        configVarMap.put(ROOT_MODULE,
                new VariableKey[]{new VariableKey(ROOT_MODULE, "intVar", PredefinedTypes.TYPE_INT, null
                        , false), variableKey});
        ConfigResolver configResolver = new ConfigResolver(configVarMap, diagnosticLog,
                List.of(new EnvVarProvider(ROOT_MODULE, Map.of("BAL_CONFIG_VAR_INTVAR", "22")),
                        new TomlFileProvider(ROOT_MODULE, getConfigPathForNegativeCases(
                                "UnsupportedCliConfig.toml"), Set.of(ROOT_MODULE))));
        Map<VariableKey, ConfigValue> valueMap = configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getWarningCount(), 0);
        Assert.assertEquals(diagnosticLog.getErrorCount(), errors);
        ConfigValue configValue = valueMap.get(variableKey);
        if (configValue == null) {
            Assert.assertNull(expectedValue);
        } else {
            Assert.assertEquals(configValue.getValue().toString(), expectedValue);
        }

    }
}

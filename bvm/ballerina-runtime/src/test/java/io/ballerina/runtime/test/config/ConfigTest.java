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
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.configurable.ConfigProvider;
import io.ballerina.runtime.internal.configurable.ConfigResolver;
import io.ballerina.runtime.internal.configurable.ConfigValue;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.providers.cli.CliProvider;
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
import java.util.Map;
import java.util.Set;

import static io.ballerina.runtime.test.TestUtils.getConfigPath;

/**
 * Test cases for configuration related implementations.
 */
public class ConfigTest {

    private static final Module module = new Module("myOrg", "test_module", "1");

    private static final Module ROOT_MODULE = new Module("rootOrg", "mod12", "1");
    private static final Type[] COLOR_ENUM_MEMBERS = new Type[]{
            new BFiniteType("Colors", Set.of(StringUtils.fromString("RED")), 0),
            new BFiniteType("Colors", Set.of(StringUtils.fromString("GREEN")), 0)};
    public static final Type COLOR_ENUM_UNION = new BUnionType(COLOR_ENUM_MEMBERS, COLOR_ENUM_MEMBERS, 0, false,
                                                               SymbolFlags.ENUM);
    public static final Type COLOR_ENUM = new BIntersectionType(module, new Type[]{}, COLOR_ENUM_UNION, 0, true);
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
        };
    }
}

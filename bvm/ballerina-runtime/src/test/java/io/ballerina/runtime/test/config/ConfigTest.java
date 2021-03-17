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
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.utils.XmlUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.configurable.ConfigProvider;
import io.ballerina.runtime.internal.configurable.ConfigResolver;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.providers.cli.CliConfigProvider;
import io.ballerina.runtime.internal.configurable.providers.toml.ConfigTomlProvider;
import io.ballerina.runtime.internal.diagnostics.DiagnosticLog;
import io.ballerina.runtime.internal.values.DecimalValue;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.ballerina.runtime.test.TestUtils.getConfigPath;

/**
 * Test cases for configuration related implementations.
 */
public class ConfigTest {

    private static final Module ROOT_MODULE = new Module("rootOrg", "mod12", "1.0.0");

    @Test(dataProvider = "simple-type-values-data-provider")
    public void testTomlConfigProviderWithSimpleTypes(ConfigProvider configProvider, VariableKey key,
                                                      Class<?> expectedJClass, Object expectedValue) {
        Module module = new Module("myorg", "simple_types", "1.0.0");
        DiagnosticLog diagnosticLog = new DiagnosticLog();
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey[] keys = {key};
        configVarMap.put(module, keys);
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap, diagnosticLog, configProvider);
        Map<VariableKey, Object> configValueMap = configResolver.resolveConfigs();
        Assert.assertTrue(expectedJClass.isInstance(configValueMap.get(key)));
        Assert.assertEquals(configValueMap.get(key), expectedValue);
    }

    @DataProvider(name = "simple-type-values-data-provider")
    public Object[][] simpleTypeConfigProviders() {
        Module module = new Module("myorg", "simple_types", "1.0.0");
        return new Object[][]{
                {new ConfigTomlProvider(getConfigPath("Simple_Types_Config.toml")),
                        new VariableKey(module, "intVar", PredefinedTypes.TYPE_INT, true), Long.class, 42L},
                {new ConfigTomlProvider(getConfigPath("Simple_Types_Config.toml")),
                        new VariableKey(module, "byteVar", PredefinedTypes.TYPE_BYTE, true), Integer.class, 5},
                {new ConfigTomlProvider(getConfigPath("Simple_Types_Config.toml")),
                        new VariableKey(module, "floatVar", PredefinedTypes.TYPE_FLOAT, true), Double.class, 3.5},
                {new ConfigTomlProvider(getConfigPath("Simple_Types_Config.toml")),
                        new VariableKey(module, "stringVar", PredefinedTypes.TYPE_STRING, true), BString.class,
                        StringUtils.fromString("abc")},
                {new ConfigTomlProvider(getConfigPath("Simple_Types_Config.toml")),
                        new VariableKey(module, "booleanVar", PredefinedTypes.TYPE_BOOLEAN, true), Boolean.class, true},
                {new ConfigTomlProvider(getConfigPath("Simple_Types_Config.toml")),
                        new VariableKey(module, "decimalVar", PredefinedTypes.TYPE_DECIMAL, true), DecimalValue.class,
                        new DecimalValue("24.87")},
                {new CliConfigProvider(ROOT_MODULE, "-Cmyorg.simple_types.intVar=123"),
                        new VariableKey(module, "intVar", PredefinedTypes.TYPE_INT, true), Long.class, 123L},
                {new CliConfigProvider(ROOT_MODULE, "-Cmyorg.simple_types.byteVar=7"),
                        new VariableKey(module, "byteVar", PredefinedTypes.TYPE_BYTE, true), Integer.class, 7},
                {new CliConfigProvider(ROOT_MODULE, "-Cmyorg.simple_types.floatVar=99.9"),
                        new VariableKey(module, "floatVar", PredefinedTypes.TYPE_FLOAT, true), Double.class, 99.9},
                {new CliConfigProvider(ROOT_MODULE, "-Cmyorg.simple_types.stringVar=efg"),
                        new VariableKey(module, "stringVar", PredefinedTypes.TYPE_STRING, true), BString.class,
                        StringUtils.fromString("efg")},
                {new CliConfigProvider(ROOT_MODULE, "-Cmyorg.simple_types.booleanVar=false"),
                        new VariableKey(module, "booleanVar", PredefinedTypes.TYPE_BOOLEAN, true), Boolean.class,
                        false},
                {new CliConfigProvider(ROOT_MODULE, "-Cmyorg.simple_types.decimalVar=876.54"),
                        new VariableKey(module, "decimalVar", PredefinedTypes.TYPE_DECIMAL, true), DecimalValue.class,
                        new DecimalValue("876.54")},
                {new CliConfigProvider(ROOT_MODULE,
                                       "-Cmyorg.simple_types.xmlVar=<book>The Lost World</book>\n<!--I am a comment-->"),
                        new VariableKey(module, "xmlVar", PredefinedTypes.TYPE_XML, true), BXml.class,
                        XmlUtils.parse("<book>The Lost World</book>\n<!--I am a comment-->")}
        };
    }

    @Test(dataProvider = "special-characters-data-provider")
    public void testConfigVarsWithSpecialCharacters(ConfigProvider configProvider,
                                                    String orgName,
                                                    String moduleName,
                                                    String variableName,
                                                    Type type,
                                                    Object expectedValue) {
        Module module = new Module(orgName, moduleName, "1.0.0");
        DiagnosticLog diagnosticLog = new DiagnosticLog();
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey[] keys = {
                new VariableKey(module, variableName, type, true),
        };
        configVarMap.put(module, keys);
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap, diagnosticLog,
                                                           configProvider);
        Map<VariableKey, Object> configValueMap = configResolver.resolveConfigs();
        Assert.assertEquals(configValueMap.get(keys[0]), expectedValue);
    }

    @DataProvider(name = "special-characters-data-provider")
    public Object[][] specialCharactersConfigProviders() {
        return new Object[][]{
                {new CliConfigProvider(ROOT_MODULE,
                                       "-Corg453.io.http2.socket_transport." +
                                               "uti123ls.i\\$nt\\=va/*r\\===abc~!@#$%^&*()_+=-210|}{?>\\=<"),
                        "org453", "io.http2.socket_transport.uti123ls", "i\\$nt\\=va/*r\\=",
                        PredefinedTypes.TYPE_STRING,
                        StringUtils.fromString("=abc~!@#$%^&*()_+=-210|}{?>\\=<")}
        };
    }
}

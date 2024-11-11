/*
 * Copyright (c) 2024, WSO2 LLC. (http://wso2.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.runtime.test.config;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.FiniteType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.UnionType;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BXml;
import io.ballerina.runtime.internal.configurable.ConfigResolver;
import io.ballerina.runtime.internal.configurable.ConfigValue;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.providers.env.EnvVarProvider;
import io.ballerina.runtime.internal.diagnostics.RuntimeDiagnosticLog;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.runtime.api.utils.StringUtils.fromString;
import static io.ballerina.runtime.test.TestUtils.getIntersectionType;

/**
 * Test cases specific for configuration provided via environment variables.
 */
public class EnvProviderTest {

    private static final Module ROOT_MODULE = new Module("rootOrg", "rootMod", "1");

    @Test(dataProvider = "different-env-vars-data-provider")
    public void testDifferentUserProvidedEnvVarConfig(String envVarKey, String envVarValue, String orgName,
                                                      String moduleName, String variableName, Type type,
                                                      Object expectedValue) {
        Module module = new Module(orgName, moduleName, "1");
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey[] keys = {
                new VariableKey(module, variableName, type, true),
        };
        configVarMap.put(module, keys);
        Map<String, String> envVarMap = new HashMap<>();
        envVarMap.put(envVarKey, envVarValue);
        ConfigResolver configResolver = new ConfigResolver(configVarMap, diagnosticLog,
                List.of(new EnvVarProvider(ROOT_MODULE, envVarMap)));
        Map<VariableKey, ConfigValue> configValueMap = configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getErrorCount(), 0);
        Assert.assertEquals(diagnosticLog.getWarningCount(), 0);
        Assert.assertEquals(configValueMap.get(keys[0]).getValue(), expectedValue);
    }

    @DataProvider(name = "different-env-vars-data-provider")
    public Object[][] differentEnvVarsProvider() {
        return new Object[][]{
                // Variable with no special characters
                {"BAL_CONFIG_VAR_MYORG_MOD_INTVAR", "123", "myorg", "mod", "intVar", PredefinedTypes.TYPE_INT, 123L},
                // Variable with space
                {"BAL_CONFIG_VAR_MYORG_MOD_INTVAR\\ ", "123", "myorg", "mod", "intVar\\ ", PredefinedTypes.TYPE_INT,
                        123L},
                // key with space in float value
                {"BAL_CONFIG_VAR_MYORG_MOD_X", "  4.675", "myorg", "mod", "x", PredefinedTypes.TYPE_FLOAT, 4.675},
                // key with space in string value
                {"BAL_CONFIG_VAR_MYORG_MOD_X", "  Hello World  ", "myorg", "mod", "x", PredefinedTypes.TYPE_STRING,
                        StringUtils.fromString("  Hello World  ")},
                // module = root Module
                {"BAL_CONFIG_VAR_INTVAR", "123", "rootOrg", "rootMod", "intVar", PredefinedTypes.TYPE_INT, 123L},
                // module = root Module and full org-module structure
                {"BAL_CONFIG_VAR_ROOTORG_ROOTMOD_INTVAR", "123", "rootOrg", "rootMod", "intVar",
                        PredefinedTypes.TYPE_INT, 123L},
                // module org = root Module org
                {"BAL_CONFIG_VAR_MOD_INTVAR", "123", "rootOrg", "mod", "intVar", PredefinedTypes.TYPE_INT, 123L},
                // Module and Value with multiple special characters
                {"BAL_CONFIG_VAR_ORG453_IO_HTTP2_SOCKET_TRANSPORT_UTI123LS_I\\$NT\\=VA/*R\\==",
                        "=abc~!@#$%^&*()" + "_+=-210|}{?>\\=<", "org453", "io.http2.socket_transport.uti123ls",
                        "i\\$nt\\=va/*r\\==", PredefinedTypes.TYPE_STRING,
                        StringUtils.fromString("=abc~!@#$%^&*()_+=-210|}{?>\\=<")},
        };
    }

    @Test
    public void testMultipleEnvVarPrefixForModuleConfig() {
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        VariableKey a = new VariableKey(ROOT_MODULE, "a", PredefinedTypes.TYPE_STRING, true);
        VariableKey b = new VariableKey(ROOT_MODULE, "b", PredefinedTypes.TYPE_STRING, true);
        VariableKey c = new VariableKey(ROOT_MODULE, "c", PredefinedTypes.TYPE_STRING, true);
        Map<String, String> envVars = new HashMap<>();
        envVars.put("BAL_CONFIG_VAR_A", "aaa");
        envVars.put("BAL_CONFIG_VAR_ROOTMOD_B", "bbb");
        envVars.put("BAL_CONFIG_VAR_ROOTORG_ROOTMOD_C", "ccc");
        ConfigResolver configResolver =
                new ConfigResolver(Map.ofEntries(Map.entry(ROOT_MODULE, new VariableKey[]{a, b, c})), diagnosticLog,
                        List.of(new EnvVarProvider(ROOT_MODULE, envVars)));
        Map<VariableKey, ConfigValue> configValueMap = configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getErrorCount(), 0);
        Assert.assertEquals(diagnosticLog.getWarningCount(), 0);
        Assert.assertEquals(configValueMap.get(a).getValue(), StringUtils.fromString("aaa"));
        Assert.assertEquals(configValueMap.get(b).getValue(), StringUtils.fromString("bbb"));
        Assert.assertEquals(configValueMap.get(c).getValue(), StringUtils.fromString("ccc"));
    }

    @Test(dataProvider = "finite-data-provider")
    public void testEnvProviderFiniteType(String variableName, Type type, Object expectedValues, String envKey,
                                          String envVal) {
        VariableKey finiteVar = new VariableKey(ROOT_MODULE, variableName, type, false);
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        Map<String, String> envVarMap = new HashMap<>();
        envVarMap.put(envKey, envVal);
        ConfigResolver configResolver =
                new ConfigResolver(Map.ofEntries(Map.entry(ROOT_MODULE, new VariableKey[]{finiteVar})), diagnosticLog
                        , List.of(new EnvVarProvider(ROOT_MODULE, envVarMap)));
        Map<VariableKey, ConfigValue> configValueMap = configResolver.resolveConfigs();
        Object value = configValueMap.get(finiteVar).getValue();
        Assert.assertEquals(expectedValues, value);
    }

    @DataProvider(name = "finite-data-provider")
    public Object[][] finiteDataProvider() {
        String typeName = "Singleton";
        BString strVal = fromString("test");
        BDecimal decimalVal = ValueCreator.createDecimalValue("3.23");
        FiniteType stringFinite = TypeCreator.createFiniteType(typeName, Set.of(strVal), 0);
        FiniteType intFinite = TypeCreator.createFiniteType(typeName, Set.of(2L), 0);
        FiniteType floatFinite = TypeCreator.createFiniteType(typeName, Set.of(2.2d), 0);
        FiniteType decimalFinite = TypeCreator.createFiniteType(typeName, Set.of(decimalVal), 0);
        FiniteType booleanFinite = TypeCreator.createFiniteType(typeName, Set.of(true), 0);
        FiniteType unionFinite1 = TypeCreator.createFiniteType(typeName, Set.of(strVal, 1.34d, decimalVal), 0);
        FiniteType unionFinite2 = TypeCreator.createFiniteType(typeName, Set.of(3.24d, decimalVal), 0);
        FiniteType unionFinite3 = TypeCreator.createFiniteType(typeName, Set.of(3.23d, decimalVal, strVal), 0);
        return new Object[][]{
                {"intSingleton", intFinite, 2L, "BAL_CONFIG_VAR_INTSINGLETON", "2"},
                {"floatSingleton", floatFinite, 2.2d, "BAL_CONFIG_VAR_FLOATSINGLETON", "2.2"},
                {"decimalSingleton", decimalFinite, decimalVal, "BAL_CONFIG_VAR_DECIMALSINGLETON", "3.23"},
                {"booleanSingleton", booleanFinite, true, "BAL_CONFIG_VAR_BOOLEANSINGLETON", "true"},
                {"unionVar1", unionFinite1, 1.34d, "BAL_CONFIG_VAR_UNIONVAR1", "1.34"},
                {"unionVar2", unionFinite2, decimalVal, "BAL_CONFIG_VAR_UNIONVAR2", "3.23"},
                {"unionVar3", unionFinite3, strVal, "BAL_CONFIG_VAR_UNIONVAR3", "test"},
                {"stringSingleton", getIntersectionType(ROOT_MODULE, stringFinite), strVal,
                        "BAL_CONFIG_VAR_STRINGSINGLETON", "test"},
                {"intSingleton", getIntersectionType(ROOT_MODULE, intFinite), 2L, "BAL_CONFIG_VAR_INTSINGLETON", "2"},
                {"floatSingleton", getIntersectionType(ROOT_MODULE, floatFinite), 2.2d, "BAL_CONFIG_VAR_FLOATSINGLETON",
                        "2.2"},
                {"decimalSingleton", getIntersectionType(ROOT_MODULE, decimalFinite), decimalVal,
                        "BAL_CONFIG_VAR_DECIMALSINGLETON", "3.23"},
                {"booleanSingleton", getIntersectionType(ROOT_MODULE, booleanFinite), true,
                        "BAL_CONFIG_VAR_BOOLEANSINGLETON", "true"},
                {"unionVar1", getIntersectionType(ROOT_MODULE, unionFinite1), 1.34d, "BAL_CONFIG_VAR_UNIONVAR1",
                        "1.34"},
                {"unionVar2", getIntersectionType(ROOT_MODULE, unionFinite2), decimalVal, "BAL_CONFIG_VAR_UNIONVAR2",
                        "3.23"},
                {"unionVar3", getIntersectionType(ROOT_MODULE, unionFinite3), strVal, "BAL_CONFIG_VAR_UNIONVAR3",
                        "test"},
        };
    }

    @Test
    public void testEnvProviderFiniteTypeOptional() {
        VariableKey finiteVar = new VariableKey(ROOT_MODULE, "intSingleton",
                TypeCreator.createFiniteType("Singleton", Set.of(2L), 0), false);
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        ConfigResolver configResolver =
                new ConfigResolver(Map.ofEntries(Map.entry(ROOT_MODULE, new VariableKey[]{finiteVar})), diagnosticLog
                        , List.of(new EnvVarProvider(ROOT_MODULE, Map.of("BAL_CONFIG_VAR_UNIONVAR", "1.34"))));
        Map<VariableKey, ConfigValue> configValueMap = configResolver.resolveConfigs();
        Assert.assertFalse(configValueMap.containsKey(finiteVar));
    }

    @Test(dataProvider = "union-data-provider")
    public void testEnvProviderUnionType(String variableName, Type type, Object expectedValues, String key,
                                         String envValue) {
        VariableKey unionVar = new VariableKey(ROOT_MODULE, variableName, type, false);
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        ConfigResolver configResolver =
                new ConfigResolver(Map.ofEntries(Map.entry(ROOT_MODULE, new VariableKey[]{unionVar})), diagnosticLog
                        , List.of(new EnvVarProvider(ROOT_MODULE, Map.of(key, envValue))));
        Map<VariableKey, ConfigValue> configValueMap = configResolver.resolveConfigs();
        Object value = configValueMap.get(unionVar).getValue();
        Assert.assertEquals(expectedValues, value);
    }

    @DataProvider(name = "union-data-provider")
    public Object[][] unionDataProvider() {
        BString strVal = fromString("test");
        BDecimal decimalVal = ValueCreator.createDecimalValue("3.23");
        UnionType stringInt =
                TypeCreator.createUnionType(List.of(PredefinedTypes.TYPE_STRING, PredefinedTypes.TYPE_INT), true);
        UnionType intFloat = TypeCreator.createUnionType(List.of(PredefinedTypes.TYPE_INT,
                PredefinedTypes.TYPE_FLOAT), true);
        UnionType intBoolean = TypeCreator.createUnionType(List.of(PredefinedTypes.TYPE_INT,
                PredefinedTypes.TYPE_BOOLEAN), true);
        UnionType byteBoolean =
                TypeCreator.createUnionType(List.of(PredefinedTypes.TYPE_BOOLEAN, PredefinedTypes.TYPE_BYTE), true);
        UnionType intDecimal = TypeCreator.createUnionType(List.of(PredefinedTypes.TYPE_INT,
                PredefinedTypes.TYPE_DECIMAL), true);
        UnionType floatBoolen = TypeCreator.createUnionType(List.of(PredefinedTypes.TYPE_FLOAT,
                PredefinedTypes.TYPE_BOOLEAN), true);
        return new Object[][]{
                {"stringInt", getIntersectionType(ROOT_MODULE, stringInt), strVal, "BAL_CONFIG_VAR_STRINGINT", "test"},
                {"intFloat", getIntersectionType(ROOT_MODULE, intFloat), 2.2d, "BAL_CONFIG_VAR_INTFLOAT", "2.2"},
                {"byteBoolean", getIntersectionType(ROOT_MODULE, byteBoolean), 222, "BAL_CONFIG_VAR_BYTEBOOLEAN",
                        "222"},
                {"intBoolean", getIntersectionType(ROOT_MODULE, intBoolean), 2L, "BAL_CONFIG_VAR_INTBOOLEAN", "2"},
                {"intDecimal", getIntersectionType(ROOT_MODULE, intDecimal), decimalVal, "BAL_CONFIG_VAR_INTDECIMAL",
                        "3.23"},
                {"floatBoolean", getIntersectionType(ROOT_MODULE, floatBoolen), true, "BAL_CONFIG_VAR_FLOATBOOLEAN",
                        "true"},
        };
    }

    @Test
    public void testEnvProviderXmlUnionType() {
        BXml xmlVal = ValueCreator.createXmlComment(StringUtils.fromString("I am a comment"));
        UnionType intXml = TypeCreator.createUnionType(List.of(PredefinedTypes.TYPE_INT,
                PredefinedTypes.TYPE_XML), true);
        VariableKey unionVar = new VariableKey(ROOT_MODULE, "intXml",
                getIntersectionType(ROOT_MODULE, intXml), false);
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        ConfigResolver configResolver =
                new ConfigResolver(Map.ofEntries(Map.entry(ROOT_MODULE, new VariableKey[]{unionVar})), diagnosticLog
                        , List.of(new EnvVarProvider(ROOT_MODULE, Map.of("BAL_CONFIG_VAR_INTXML",
                        "<!--I am a comment-->"))));
        Map<VariableKey, ConfigValue> configValueMap = configResolver.resolveConfigs();
        Object value = configValueMap.get(unionVar).getValue();
        Assert.assertEquals(xmlVal.toString(), value.toString());
    }

    @Test
    public void testEnvProviderXmlType() {
        BXml xmlVal = ValueCreator.createXmlComment(StringUtils.fromString("I am a comment"));
        VariableKey xmlVar = new VariableKey(ROOT_MODULE, "xmlVar",
                getIntersectionType(ROOT_MODULE, PredefinedTypes.TYPE_READONLY_XML), false);
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        ConfigResolver configResolver =
                new ConfigResolver(Map.ofEntries(Map.entry(ROOT_MODULE, new VariableKey[]{xmlVar})), diagnosticLog
                        , List.of(new EnvVarProvider(ROOT_MODULE, Map.of("BAL_CONFIG_VAR_XMLVAR",
                        "<!--I am a comment-->"))));
        Map<VariableKey, ConfigValue> configValueMap = configResolver.resolveConfigs();
        Object value = configValueMap.get(xmlVar).getValue();
        Assert.assertEquals(xmlVal.toString(), value.toString());
    }
}

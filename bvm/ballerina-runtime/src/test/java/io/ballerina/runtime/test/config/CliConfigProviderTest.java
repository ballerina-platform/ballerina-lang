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
import io.ballerina.runtime.api.types.FiniteType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BDecimal;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.configurable.ConfigResolver;
import io.ballerina.runtime.internal.configurable.ConfigValue;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.providers.cli.CliProvider;
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
 * Test cases specific for configuration provided via cli.
 */
public class CliConfigProviderTest {

    private static final Module ROOT_MODULE = new Module("rootOrg", "rootMod", "1");

    @Test(dataProvider = "different-cli_args-data-provider")
    public void testDifferentUserProvidedCLIConfig(String arg,
                                                   String orgName,
                                                   String moduleName,
                                                   String variableName,
                                                   Type type,
                                                   Object expectedValue) {
        Module module = new Module(orgName, moduleName, "1");
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey[] keys = {
                new VariableKey(module, variableName, type, true),
        };
        configVarMap.put(module, keys);
        ConfigResolver configResolver = new ConfigResolver(configVarMap,
                                                           diagnosticLog,
                                                           List.of(new CliProvider(ROOT_MODULE, arg)));
        Map<VariableKey, ConfigValue> configValueMap = configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getErrorCount(), 0);
        Assert.assertEquals(diagnosticLog.getWarningCount(), 0);
        Assert.assertEquals(configValueMap.get(keys[0]).getValue(), expectedValue);
    }

    @DataProvider(name = "different-cli_args-data-provider")
    public Object[][] differentCliArgsProvider() {
        return new Object[][]{
                // Variable with no special characters
                {"-Cmyorg.mod.intVar=123", "myorg", "mod", "intVar", PredefinedTypes.TYPE_INT, 123L},
                // Variable with space
                {"-Cmyorg.mod.intVar\\ =123", "myorg", "mod", "intVar\\ ", PredefinedTypes.TYPE_INT, 123L},
                // Key with space and float value with space
                {"-Cmyorg.mod.x =   4.675   ", "myorg", "mod", "x", PredefinedTypes.TYPE_FLOAT, 4.675},
                // Key with space and string value with space
                {"-Cmyorg.mod.x = hello world ", "myorg", "mod", "x", PredefinedTypes.TYPE_STRING,
                        StringUtils.fromString(" hello world ")},
                // module = root Module
                {"-CintVar=123", "rootOrg", "rootMod", "intVar", PredefinedTypes.TYPE_INT, 123L},
                // module = root Module and full command line argument
                {"-CrootOrg.rootMod.intVar=123", "rootOrg", "rootMod", "intVar", PredefinedTypes.TYPE_INT, 123L},
                // module org = root Module org
                {"-Cmod.intVar=123", "rootOrg", "mod", "intVar", PredefinedTypes.TYPE_INT, 123L},
                // Variable with '='
                {"-Cmyorg.mod.stringVar\\==hello", "myorg", "mod", "stringVar\\=", PredefinedTypes.TYPE_STRING,
                        StringUtils.fromString("hello")},
                // Variable and value with '='
                {"-Cmyorg.mod.stringVar\\==myorg.mod.stringVar\\=", "myorg", "mod", "stringVar\\=",
                        PredefinedTypes.TYPE_STRING, StringUtils.fromString("myorg.mod.stringVar\\=")},
                // Variable and value with multiple '='
                {"-Cmyorg.mod.stringVar\\==myorg.mod.stringVar\\=", "myorg", "mod", "stringVar\\=",
                        PredefinedTypes.TYPE_STRING, StringUtils.fromString("myorg.mod.stringVar\\=")},
                // Module and Value with multiple special characters
                {"-Corg453.io.http2.socket_transport.uti123ls.i\\$nt\\=va/*r\\===abc~!@#$%^&*()_+=-210|}{?>\\=<",
                        "org453", "io.http2.socket_transport.uti123ls", "i\\$nt\\=va/*r\\=",
                        PredefinedTypes.TYPE_STRING, StringUtils.fromString("=abc~!@#$%^&*()_+=-210|}{?>\\=<")}
        };
    }

    @Test
    public void testMultipleArgumentPrefixForModuleConfig() {
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        VariableKey a = new VariableKey(ROOT_MODULE, "a", PredefinedTypes.TYPE_STRING, true);
        VariableKey b = new VariableKey(ROOT_MODULE, "b", PredefinedTypes.TYPE_STRING, true);
        VariableKey c = new VariableKey(ROOT_MODULE, "c", PredefinedTypes.TYPE_STRING, true);
        ConfigResolver configResolver =
                new ConfigResolver(Map.ofEntries(Map.entry(ROOT_MODULE, new VariableKey[]{a, b, c})), diagnosticLog,
                        List.of(new CliProvider(ROOT_MODULE, "-Ca=aaa", "-CrootMod.b=bbb", "-CrootOrg.rootMod.c=ccc")));
        Map<VariableKey, ConfigValue> configValueMap = configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getErrorCount(), 0);
        Assert.assertEquals(diagnosticLog.getWarningCount(), 0);
        Assert.assertEquals(configValueMap.get(a).getValue(), StringUtils.fromString("aaa"));
        Assert.assertEquals(configValueMap.get(b).getValue(), StringUtils.fromString("bbb"));
        Assert.assertEquals(configValueMap.get(c).getValue(), StringUtils.fromString("ccc"));
    }

    @Test(dataProvider = "finite-data-provider")
    public void testCliProviderFiniteType(String variableName, Type type, Object expectedValues, String cliArg) {
        VariableKey finiteVar = new VariableKey(ROOT_MODULE, variableName, type, false);
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        ConfigResolver configResolver =
                new ConfigResolver(Map.ofEntries(Map.entry(ROOT_MODULE, new VariableKey[]{finiteVar})), diagnosticLog
                        , List.of(new CliProvider(ROOT_MODULE, cliArg)));
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
                {"intSingleton", intFinite, 2L, "-CintSingleton=2"},
                {"floatSingleton", floatFinite, 2.2d, "-CfloatSingleton=2.2"},
                {"decimalSingleton", decimalFinite, decimalVal, "-CdecimalSingleton=3.23"},
                {"booleanSingleton", booleanFinite, true, "-CbooleanSingleton=true"},
                {"unionVar1", unionFinite1, 1.34d, "-CunionVar1=1.34"},
                {"unionVar2", unionFinite2, decimalVal, "-CunionVar2=3.23"},
                {"unionVar3", unionFinite3, strVal, "-CunionVar3=test"},
                {"stringSingleton", getIntersectionType(ROOT_MODULE, stringFinite), strVal, "-CstringSingleton=test"},
                {"intSingleton", getIntersectionType(ROOT_MODULE, intFinite), 2L, "-CintSingleton=2"},
                {"floatSingleton", getIntersectionType(ROOT_MODULE, floatFinite), 2.2d, "-CfloatSingleton=2.2"},
                {"decimalSingleton", getIntersectionType(ROOT_MODULE, decimalFinite), decimalVal,
                        "-CdecimalSingleton=3.23"},
                {"booleanSingleton", getIntersectionType(ROOT_MODULE, booleanFinite), true, "-CbooleanSingleton=true"},
                {"unionVar1", getIntersectionType(ROOT_MODULE, unionFinite1), 1.34d, "-CunionVar1=1.34"},
                {"unionVar2", getIntersectionType(ROOT_MODULE, unionFinite2), decimalVal, "-CunionVar2=3.23"},
                {"unionVar3", getIntersectionType(ROOT_MODULE, unionFinite3), strVal, "-CunionVar3=test"},
        };
    }

    @Test
    public void testCliProviderFiniteTypeOptional() {
        VariableKey finiteVar = new VariableKey(ROOT_MODULE, "intSingleton", TypeCreator.createFiniteType("Singleton",
                Set.of(2L), 0), false);
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        ConfigResolver configResolver =
                new ConfigResolver(Map.ofEntries(Map.entry(ROOT_MODULE, new VariableKey[]{finiteVar})), diagnosticLog
                        , List.of(new CliProvider(ROOT_MODULE, "-CunionVar1=1.34")));
        Map<VariableKey, ConfigValue> configValueMap = configResolver.resolveConfigs();
        Assert.assertFalse(configValueMap.containsKey(finiteVar));
    }

}

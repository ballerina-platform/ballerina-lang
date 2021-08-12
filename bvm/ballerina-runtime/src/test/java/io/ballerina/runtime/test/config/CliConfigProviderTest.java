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
}

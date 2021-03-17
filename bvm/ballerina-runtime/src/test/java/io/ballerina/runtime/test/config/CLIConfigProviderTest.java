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
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.internal.TypeConverter;
import io.ballerina.runtime.internal.configurable.ConfigResolver;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.providers.cli.CliConfigProvider;
import io.ballerina.runtime.internal.diagnostics.DiagnosticLog;
import io.ballerina.runtime.internal.values.ArrayValueImpl;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static io.ballerina.runtime.internal.configurable.providers.cli.CliConfigProvider.CLI_ARG_REGEX;

/**
 * Test cases specific for configuration provided via cli.
 */
public class CLIConfigProviderTest {

    private static final Module ROOT_MODULE = new Module("rootOrg", "rootMod", "1.0.0");

    @Test(dataProvider = "different-cli_args-data-provider")
    public void testDifferentUserProvidedCLIConfig(String arg,
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
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap,
                                                           diagnosticLog,
                                                           new CliConfigProvider(ROOT_MODULE, arg));
        Map<VariableKey, Object> configValueMap = configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getErrorCount(), 0);
        Assert.assertEquals(diagnosticLog.getWarningCount(), 0);
        Assert.assertEquals(configValueMap.get(keys[0]), expectedValue);
    }

    @DataProvider(name = "different-cli_args-data-provider")
    public Object[][] specialCharactersConfigProvider() {
        return new Object[][]{
                {"-Cmyorg.mod.intVar=123", "myorg", "mod", "intVar", PredefinedTypes.TYPE_INT, 123L},
                {"-Cmyorg.mod.intVar\\ =123", "myorg", "mod", "intVar\\ ", PredefinedTypes.TYPE_INT, 123L},
                {"-Cmyorg.mod.x =   4.675   ", "myorg", "mod", "x", PredefinedTypes.TYPE_FLOAT, 4.675},
                {"-Cmyorg.mod.x = hello world ", "myorg", "mod", "x", PredefinedTypes.TYPE_STRING,
                        StringUtils.fromString(" hello world ")},
                {"-Cmyorg.mod.x = <book>The Lost World</book> ", "myorg", "mod", "x", PredefinedTypes.TYPE_XML,
                        TypeConverter.stringToXml("<book>The Lost World</book>")},
                {"-CintVar=123", "rootOrg", "rootMod", "intVar", PredefinedTypes.TYPE_INT, 123L},
                {"-Cmod.intVar=123", "rootOrg", "mod", "intVar", PredefinedTypes.TYPE_INT, 123L}
        };
    }

    @Test(dataProvider = "cli_args-data-provider")
    public void testCliArgRegex(String cliArg, String[] expectedKeyValuePair) {
        String[] keyValuePair = cliArg.split(CLI_ARG_REGEX, 2);
        Assert.assertEquals(keyValuePair.length, expectedKeyValuePair.length);
        for (int i = 0; i < keyValuePair.length; i++) {
            Assert.assertEquals(keyValuePair[i], expectedKeyValuePair[i]);
        }
    }

    @DataProvider(name = "cli_args-data-provider")
    public Object[][] cliArgsProvider() {
        return new Object[][]{
                {"myorg.mod.stringVar=123", new String[]{"myorg.mod.stringVar", "123"}},
                {"myorg.mod.stringVar\\==123", new String[]{"myorg.mod.stringVar\\=", "123"}},
                {"myorg.mod.stringVar\\==myorg.mod.stringVar\\=",
                        new String[]{"myorg.mod.stringVar\\=", "myorg.mod.stringVar\\="}},
                {"myorg.mod.stringVar=myorg=mod=stringVar", new String[]{"myorg.mod.stringVar", "myorg=mod=stringVar"}},
                {"myorg.mod.stringVar", new String[]{"myorg.mod.stringVar"}},
        };
    }
}

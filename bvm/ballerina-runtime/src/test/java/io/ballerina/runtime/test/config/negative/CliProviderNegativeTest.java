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

package io.ballerina.runtime.test.config.negative;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.internal.configurable.ConfigResolver;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.providers.cli.CliProvider;
import io.ballerina.runtime.internal.diagnostics.DiagnosticLog;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test cases specific for configuration provided via cli.
 */
public class CliProviderNegativeTest {

    private static final Module ROOT_MODULE = new Module("rootOrg", "rootMod", "1.0.0");

    @Test(dataProvider = "different-cli_args-data-provider")
    public void testCLIArgErrors(String[] args,
                                 String orgName,
                                 String moduleName,
                                 String variableName,
                                 Type type,
                                 String[] expectedErrorMessages) {
        Module module = new Module(orgName, moduleName, "1.0.0");
        DiagnosticLog diagnosticLog = new DiagnosticLog();
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey[] keys = {
                new VariableKey(module, variableName, type, true),
        };
        configVarMap.put(module, keys);
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap,
                                                           diagnosticLog,
                                                           List.of(new CliProvider(ROOT_MODULE, args)));
        configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getErrorCount(), expectedErrorMessages.length);
        for (int i = 0; i < expectedErrorMessages.length; i++) {
            Assert.assertEquals(diagnosticLog.getDiagnosticList().get(i).toString(), expectedErrorMessages[i]);
        }
    }

    @DataProvider(name = "different-cli_args-data-provider")
    public Object[][] cliArgsDataProvider() {
        return new Object[][]{
                // Config value with invalid type value
                {new String[]{"-Cmyorg.mod.x = hello  "}, "myorg", "mod", "x", PredefinedTypes.TYPE_INT,
                        "error: [myorg.mod.x= hello  ] configurable variable 'x' is expected to be of type 'int', but" +
                                " found ' hello  '"},
                // Config int value with spaces
                {new String[]{"-Cmyorg.mod.x = 123  "}, "myorg", "mod", "x", PredefinedTypes.TYPE_INT,
                        "error: [myorg.mod.x= 123  ] configurable variable 'x' is expected to be of type 'int', but " +
                                "found ' 123  '"},
                // Config byte value with spaces
                {new String[]{"-Cmyorg.mod.x = 5 "}, "myorg", "mod", "x", PredefinedTypes.TYPE_BYTE,
                        "error: [myorg.mod.x= 5 ] configurable variable 'x' is expected to be of type 'byte', but " +
                                "found ' 5 '"},
                // Config boolean value with spaces
                {new String[]{"-Cmyorg.mod.x = true "}, "myorg", "mod", "x", PredefinedTypes.TYPE_BOOLEAN,
                        "error: [myorg.mod.x= true ] configurable variable 'x' is expected to be of type 'boolean', " +
                                "but found ' true '"},
                // Config decimal value with spaces
                {new String[]{"-Cmyorg.mod.x = 27.5 "}, "myorg", "mod", "x", PredefinedTypes.TYPE_DECIMAL,
                        "error: [myorg.mod.x= 27.5 ] configurable variable 'x' is expected to be of type 'decimal', " +
                                "but found ' 27.5 '"},
                // Config byte value with invalid byte range
                {new String[]{"-Cmyorg.mod.x=345"}, "myorg", "mod", "x", PredefinedTypes.TYPE_BYTE,
                        "error: [myorg.mod.x=345] value provided for byte variable 'x' is out of range. Expected " +
                                "range is (0-255), found '345'"}
        };
    }

    @Test
    public void testUnusedCliArgs() {
        Module module = new Module("myorg", "mod", "1.0.0");
        DiagnosticLog diagnosticLog = new DiagnosticLog();
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey[] keys = {
                new VariableKey(module, "x", PredefinedTypes.TYPE_INT, true),
        };
        configVarMap.put(module, keys);
        String[] args = {"-Cmyorg.mod.x=123", "-Cmyorg.mod.y=apple", "-Cmyorg.mod.z=27.5"};
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap,
                                                           diagnosticLog,
                                                           List.of(new CliProvider(ROOT_MODULE, args)));
        Map<VariableKey, Object> varKeyValueMap =  configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getWarningCount(), 1);
        Assert.assertEquals(diagnosticLog.getErrorCount(), 0);
        Assert.assertEquals(diagnosticLog.getDiagnosticList().get(0).toString(),
                            "warning: unused command line arguments found \n" +
                                    "\tmyorg.mod.z=27.5\n" +
                                    "\tmyorg.mod.y=apple");
        Assert.assertEquals(varKeyValueMap.get(new VariableKey(module, "x")), 123L);
    }

    @Test
    public void testAmbiguityWithModuleImportsCliArgs() {
        Module importedModule = new Module("a", "b", "1.0.0");
        Module rootModule = new Module("testOrg", "a.b", "1.0.0");
        DiagnosticLog diagnosticLog = new DiagnosticLog();
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey[] keys = {
                new VariableKey(importedModule, "c", PredefinedTypes.TYPE_INT, true),
                new VariableKey(rootModule, "c", PredefinedTypes.TYPE_INT, true),
                new VariableKey(rootModule, "y", PredefinedTypes.TYPE_STRING, true),
        };
        configVarMap.put(rootModule, keys);
        String[] args = {"-Ca.b.c=123", "-Ca.b.y=apple",};
        ConfigResolver configResolver = new ConfigResolver(rootModule, configVarMap,
                                                           diagnosticLog,
                                                           List.of(new CliProvider(rootModule, args)));
        Map<VariableKey, Object> varKeyValueMap = configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getWarningCount(), 0);
        Assert.assertEquals(diagnosticLog.getErrorCount(), 1);
        Assert.assertEquals(diagnosticLog.getDiagnosticList().get(0).toString(), "error: configurable value for " +
                "variable 'testOrg/a.b:c' clashes with variable 'a/b:c'. Please provide the command line argument as " +
                "'[-CtestOrg.a.b.c=<value>]'");
        Assert.assertEquals(varKeyValueMap.get(new VariableKey(rootModule, "y")), StringUtils.fromString("apple"));
    }


    @Test
    public void testAmbiguityWithRootModuleCliArgs() {

        DiagnosticLog diagnosticLog = new DiagnosticLog();
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey[] keys = {
                new VariableKey(ROOT_MODULE, "intVar", PredefinedTypes.TYPE_INT, true)
        };
        configVarMap.put(ROOT_MODULE, keys);
        String[] args = {"-CrootMod.intVar=123", "-CintVar=321",};
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap,
                                                           diagnosticLog,
                                                           List.of(new CliProvider(ROOT_MODULE, args)));
        configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getWarningCount(), 0);
        Assert.assertEquals(diagnosticLog.getErrorCount(), 1);
        Assert.assertEquals(diagnosticLog.getDiagnosticList().get(0).toString(), "error: configurable value for " +
                "variable 'intVar' clashes with multiple command line arguments [intVar=321, rootMod.intVar=123]");
    }
}

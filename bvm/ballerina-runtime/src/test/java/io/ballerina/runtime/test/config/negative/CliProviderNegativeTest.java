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
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.internal.configurable.ConfigResolver;
import io.ballerina.runtime.internal.configurable.ConfigValue;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.providers.cli.CliProvider;
import io.ballerina.runtime.internal.diagnostics.RuntimeDiagnosticLog;
import io.ballerina.runtime.internal.types.BIntersectionType;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.test.config.ConfigTest.COLOR_ENUM;

/**
 * Test cases specific for configuration provided via cli.
 */
public class CliProviderNegativeTest {

    private static final Module ROOT_MODULE = new Module("rootOrg", "rootMod", "1");

    @Test(dataProvider = "different-cli_args-data-provider")
    public void testCLIArgErrors(String[] args,
                                 String orgName,
                                 String moduleName,
                                 String variableName,
                                 Type type,
                                 String[] expectedErrorMessages) {
        Module module = new Module(orgName, moduleName, "1");
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey[] keys = {
                new VariableKey(module, variableName, type, null, true),
        };
        configVarMap.put(module, keys);
        ConfigResolver configResolver = new ConfigResolver(configVarMap,
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
                                "range is (0-255), found '345'"},
                // Config array value which is not supported as cli arg
                {new String[]{"-Cmyorg.mod.x=345"}, "myorg", "mod", "x",
                        new BIntersectionType(ROOT_MODULE, new Type[]{},
                                              TypeCreator.createArrayType(PredefinedTypes.TYPE_INT), 0, true),
                        "error: value for configurable variable 'x' with type 'int[]' is not supported as a command " +
                                "line argument"},
                // Config record value which is not supported as cli arg
                {new String[]{"-Cmyorg.mod.x=345"}, "myorg", "mod", "x",
                        new BIntersectionType(ROOT_MODULE, new Type[]{},
                                              TypeCreator.createRecordType("customType", ROOT_MODULE, 0, false, 0), 0,
                                              true),
                        "error: value for configurable variable 'x' with type 'rootMod:customType' is not supported " +
                                "as a command line argument"},
                // Config table value which is not supported as cli arg
                {new String[]{"-Cmyorg.mod.x=345"}, "myorg", "mod", "x",
                        new BIntersectionType(ROOT_MODULE, new Type[]{},
                                              TypeCreator.createTableType(PredefinedTypes.TYPE_STRING, false), 0, true),
                        "error: value for configurable variable 'x' with type 'table<string>' is not supported as a " +
                                "command line argument"},
                {new String[]{"-CxmlVar=<book"}, "rootOrg", "rootMod", "xmlVar",
                        new BIntersectionType(ROOT_MODULE, new Type[]{}, PredefinedTypes.TYPE_XML, 0, true),
                        "error: [xmlVar=<book] configurable variable 'xmlVar' is expected to be of type 'xml<(lang" +
                                ".xml:Element|lang.xml:Comment|lang.xml:ProcessingInstruction|lang.xml:Text)>', but " +
                                "found '<book'"},
                // Invalid config enum value
                {new String[]{"-Ccolor=red"}, "rootOrg", "rootMod", "color", COLOR_ENUM,
                        "error: [color=red] configurable variable 'color' is expected to be of type 'Colors', but " +
                                "found 'red'"}
        };
    }

    @Test
    public void testUnusedCliArgs() {
        Module module = new Module("myorg", "mod", "1");
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey x = new VariableKey(module, "x", PredefinedTypes.TYPE_INT, true);

        configVarMap.put(module, new VariableKey[] {x});
        String[] args = {"-Cmyorg.mod.x=123", "-Cmyorg.mod.y=apple", "-Cmyorg.mod.z=27.5"};
        ConfigResolver configResolver = new ConfigResolver(configVarMap,
                                                           diagnosticLog,
                                                           List.of(new CliProvider(ROOT_MODULE, args)));
        Map<VariableKey, ConfigValue> varKeyValueMap =  configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getWarningCount(), 2);
        Assert.assertEquals(diagnosticLog.getErrorCount(), 0);
        Assert.assertEquals(diagnosticLog.getDiagnosticList().get(0).toString(),
                            "warning: [myorg.mod.z=27.5] unused command line argument");
        Assert.assertEquals(diagnosticLog.getDiagnosticList().get(1).toString(),
                            "warning: [myorg.mod.y=apple] unused command line argument");
        Assert.assertEquals(varKeyValueMap.get(x).getValue(), 123L);
    }

    @Test
    public void testAmbiguityWithModuleImportsCliArgs() {
        Module importedModule = new Module("a", "b", "1");
        Module rootModule = new Module("testOrg", "a.b", "1");
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey validKey = new VariableKey(rootModule, "y", PredefinedTypes.TYPE_STRING, true);
        VariableKey[] keys = {
                new VariableKey(importedModule, "c", PredefinedTypes.TYPE_INT, true),
                new VariableKey(rootModule, "c", PredefinedTypes.TYPE_INT, true), validKey
        };
        configVarMap.put(rootModule, keys);
        String[] args = {"-Ca.b.c=123", "-Ca.b.y=apple"};
        ConfigResolver configResolver = new ConfigResolver(configVarMap,
                                                           diagnosticLog,
                                                           List.of(new CliProvider(rootModule, args)));
        Map<VariableKey, ConfigValue> varKeyValueMap = configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getWarningCount(), 0);
        Assert.assertEquals(diagnosticLog.getErrorCount(), 1);
        Assert.assertEquals(diagnosticLog.getDiagnosticList().get(0).toString(), "error: configurable value for " +
                "variable 'testOrg/a.b:c' clashes with variable 'a/b:c'. Please provide the command line argument as " +
                "'[-CtestOrg.a.b.c=<value>]'");
        Assert.assertEquals(varKeyValueMap.get(validKey).getValue(), StringUtils.fromString("apple"));
    }


    @Test
    public void testAmbiguityWithRootModuleCliArgs() {

        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey[] keys = {
                new VariableKey(ROOT_MODULE, "intVar", PredefinedTypes.TYPE_INT, true)
        };
        configVarMap.put(ROOT_MODULE, keys);
        String[] args = {"-CrootMod.intVar=123", "-CintVar=321"};
        ConfigResolver configResolver = new ConfigResolver(configVarMap,
                                                           diagnosticLog,
                                                           List.of(new CliProvider(ROOT_MODULE, args)));
        configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getWarningCount(), 0);
        Assert.assertEquals(diagnosticLog.getErrorCount(), 1);
        Assert.assertEquals(diagnosticLog.getDiagnosticList().get(0).toString(), "error: configurable value for " +
                "variable 'intVar' clashes with multiple command line arguments [intVar=321, rootMod.intVar=123]");
    }
}

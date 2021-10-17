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
import io.ballerina.runtime.internal.configurable.ConfigResolver;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.providers.cli.CliProvider;
import io.ballerina.runtime.internal.configurable.providers.toml.TomlFileProvider;
import io.ballerina.runtime.internal.diagnostics.RuntimeDiagnosticLog;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BType;
import io.ballerina.runtime.internal.types.BUnionType;
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
import static io.ballerina.runtime.test.TestUtils.getConfigPathForNegativeCases;

/**
 * Test cases specific for configuration.
 */
public class ConfigNegativeTest {

    private static final Module ROOT_MODULE = new Module("rootOrg", "rootMod", "1");
    private static final Module MODULE = new Module("org", "mod1", "1");

    @Test(dataProvider = "different-config-use-cases-data-provider")
    public void testConfigErrors(String[] args, String tomlFilePath, VariableKey[] varKeys, int errorCount,
                                 int warnCount, String[] expectedDiagnosticMsgs) {
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        configVarMap.put(MODULE, varKeys);
        ConfigResolver configResolver;
        if (tomlFilePath != null) {
            configResolver = new ConfigResolver(configVarMap,
                                                diagnosticLog, List.of(
                                                new CliProvider(ROOT_MODULE, args),
                                                new TomlFileProvider(ROOT_MODULE,
                                                        getConfigPathForNegativeCases(tomlFilePath), Set.of(MODULE))));

        } else {
            configResolver = new ConfigResolver(configVarMap,
                                                diagnosticLog, List.of(new CliProvider(ROOT_MODULE, args)));
        }
        configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getErrorCount(), errorCount);
        Assert.assertEquals(diagnosticLog.getWarningCount(), warnCount);
        for (int i = 0; i < expectedDiagnosticMsgs.length; i++) {
            Assert.assertEquals(diagnosticLog.getDiagnosticList().get(i).toString(), expectedDiagnosticMsgs[i]);
        }
    }

    @DataProvider(name = "different-config-use-cases-data-provider")
    public Object[][] configErrorCases() {
        Type incompatibleUnionType = new BIntersectionType(MODULE, new Type[]{},
                                                           new BUnionType(Arrays.asList(PredefinedTypes.TYPE_INT,
                                                                                        PredefinedTypes.TYPE_STRING)),
                                                           0, true);
        Type ambiguousUnionType = new BIntersectionType(MODULE, new Type[]{},
                                                        new BUnionType(
                                                                Arrays.asList(TypeCreator.createMapType(TYPE_ANYDATA),
                                                                              TypeCreator.createMapType(TYPE_STRING))),
                                                        0, true);
        return new Object[][]{
                // Required but not given
                {new String[]{}, null,
                        new VariableKey[]{new VariableKey(MODULE, "intVar", PredefinedTypes.TYPE_INT, null, true)}, 1
                        , 0,
                        new String[]{
                                "error: value not provided for required configurable variable 'intVar'"}},
                // Invalid toml value only
                {new String[]{}, "MismatchedTypeValues.toml",
                        new VariableKey[]{new VariableKey(MODULE, "intVar", PredefinedTypes.TYPE_INT, null, true)}, 1
                        , 0,
                        new String[]{
                                "error: [MismatchedTypeValues.toml:(3:10,3:18)] configurable variable 'intVar' " +
                                        "is expected to be of type 'int', but found 'string'"
                        }},
                // Invalid cli value only
                {new String[]{"-Corg.mod1.intVar=waruna"}, null,
                        new VariableKey[]{new VariableKey(MODULE, "intVar", PredefinedTypes.TYPE_INT, null, true)}, 1
                        , 0,
                        new String[]{
                                "error: [org.mod1.intVar=waruna] configurable variable 'intVar' is expected to be of " +
                                        "type 'int', but found 'waruna'"
                        }},
                // valid cli value invalid toml
                {new String[]{"-Corg.mod1.intVar=1"}, "MismatchedTypeValues.toml",
                        new VariableKey[]{new VariableKey(MODULE, "intVar", PredefinedTypes.TYPE_INT, null, true)}, 0
                        , 1, new String[]{
                                "warning: [MismatchedTypeValues.toml:(3:10,3:18)] configurable variable 'intVar'" +
                                        " is expected to be of type 'int', but found 'string'"
                        }},
                // valid toml value invalid cli
                {new String[]{"-Corg.mod1.intVar=waruna"}, "MatchedTypeValues.toml",
                        new VariableKey[]{new VariableKey(MODULE, "intVar", PredefinedTypes.TYPE_INT, true)}, 0, 3,
                        new String[]{
                                "warning: [org.mod1.intVar=waruna] configurable variable 'intVar' is expected to be " +
                                        "of type 'int', but found 'waruna'"
                        }},
                // invalid toml value invalid cli
                {new String[]{"-Corg.mod1.intVar=waruna"}, "MismatchedTypeValues.toml",
                        new VariableKey[]{new VariableKey(MODULE, "intVar", PredefinedTypes.TYPE_INT, null, true)}, 2
                        , 0, new String[]{
                                "error: [org.mod1.intVar=waruna] configurable variable 'intVar' is expected to be " +
                                        "of type 'int', but found 'waruna'",
                                "error: [MismatchedTypeValues.toml:(3:10,3:18)] configurable variable 'intVar'" +
                                        " is expected to be of type 'int', but found 'string'"
                        }},
                // invalid toml but valid cli
                {new String[]{"-Corg.mod1.intVar=2"}, "Invalid.toml",
                        new VariableKey[]{new VariableKey(MODULE, "intVar", PredefinedTypes.TYPE_INT, true)}, 0, 1,
                        new String[]{
                                "warning: invalid toml file : \n" +
                                        "[Invalid.toml:(3:1,3:1)] missing equal token\n" +
                                        "[Invalid.toml:(3:1,3:1)] missing value\n"}},
                // supported toml type but not cli type and cli value given
                {new String[]{"-Corg.mod1.intArr=3"}, "MatchedTypeValues.toml",
                        new VariableKey[]{new VariableKey(MODULE, "intArr",
                                                          new BIntersectionType(MODULE, new BType[]{}, TypeCreator
                                                                  .createArrayType(PredefinedTypes.TYPE_INT), 0, false),
                                                          null, true)}, 0, 3,
                        new String[]{
                                "warning: value for configurable variable 'intArr' with type '" +
                                        "int[]' is not supported as a command line argument",
                                "warning: [MatchedTypeValues.toml:(3:1,3:14)] unused configuration value 'org.mod1" +
                                        ".intVar'"}},
                // supported toml type but not cli type and cli value not given
                {new String[]{""}, "MatchedTypeValues.toml",
                        new VariableKey[]{new VariableKey(MODULE, "intArr",
                                                          new BIntersectionType(MODULE, new BType[]{}, TypeCreator
                                                                  .createArrayType(PredefinedTypes.TYPE_INT), 0, false),
                                                          null, true)}, 0, 2,
                        new String[]{
                                "warning: [MatchedTypeValues.toml:(3:1,3:14)] unused configuration value 'org.mod1" +
                                        ".intVar'"}},
                // not supported both toml type and cli type
                {new String[]{"-Corg.mod1.myMap=4"}, "MatchedTypeValues.toml",
                        new VariableKey[]{new VariableKey(MODULE, "myMap", PredefinedTypes.TYPE_MAP, null, true)}, 1
                        , 5, new String[]{"error: configurable variable 'myMap' with type 'map' is not supported",
                        "warning: [org.mod1.myMap=4] unused command line argument"}},
                // not supported cli union type
                {new String[]{"-Corg.mod1.myUnion=5"}, null, new VariableKey[]{
                        new VariableKey(MODULE, "myUnion", incompatibleUnionType, null, true)}, 1, 1,
                        new String[]{"error: value for configurable variable 'myUnion' with type '(int|string)' is " +
                                "not supported as a command line argument"}},
                // not supported cli type
                {new String[]{"-Corg.mod1.myMap=5"}, null,
                        new VariableKey[]{
                                new VariableKey(MODULE, "myMap",
                                                new BIntersectionType(MODULE, new BType[]{}, PredefinedTypes.TYPE_MAP
                                                        , 0, true), null, true)}, 1
                        , 1, new String[]{"error: value for configurable variable 'myMap' with type 'map' is not " +
                "supported as a command line argument"}},
                // not supported union type
                {new String[]{""}, "InvalidUnionType.toml", new VariableKey[]{
                        new VariableKey(MODULE, "floatUnionVar", incompatibleUnionType, null, true)}, 1, 2,
                        new String[]{"error: [InvalidUnionType.toml:(2:1,2:19)] configurable variable 'floatUnionVar'" +
                                " is expected to be of type '(int|string)', but found 'float'"}},
                {new String[]{""}, "InvalidUnionType.toml", new VariableKey[]{
                        new VariableKey(MODULE, "ambiguousUnionVar", ambiguousUnionType, null, true)}, 1, 1,
                        new String[]{"error: [InvalidUnionType.toml:(4:1,5:16)] ambiguous target types found for " +
                                "configurable variable 'ambiguousUnionVar' with type '(map<anydata>|map<string>)'"}},
        };
    }
}

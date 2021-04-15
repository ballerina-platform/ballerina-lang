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
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.IntersectionType;
import io.ballerina.runtime.api.types.MapType;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.TableType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.api.values.BTable;
import io.ballerina.runtime.internal.configurable.ConfigProvider;
import io.ballerina.runtime.internal.configurable.ConfigResolver;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.providers.toml.TomlContentProvider;
import io.ballerina.runtime.internal.configurable.providers.toml.TomlFileProvider;
import io.ballerina.runtime.internal.diagnostics.DiagnosticLog;
import io.ballerina.runtime.internal.types.BIntersectionType;
import io.ballerina.runtime.internal.types.BType;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.test.TestUtils.getConfigPath;
import static io.ballerina.runtime.test.TestUtils.getConfigPathForNegativeCases;

/**
 * Test cases specific for configuration provided via TOML files/content.
 */
public class TomlProviderNegativeTest {

    private static final Module ROOT_MODULE = new Module("rootOrg", "mod12", "1.0.0");
    private final Module module = new Module("myOrg", "test_module", "1.0.0");
    private final Module subModule = new Module("myOrg", "test_module.util.foo", "1.0.0");

    @Test(dataProvider = "path-test-provider")
    public void testPathErrors(String tomlFileName, String errorMsg, int warningCount) {
        DiagnosticLog diagnosticLog = new DiagnosticLog();
        VariableKey intVar = new VariableKey(module, "byteVar", PredefinedTypes.TYPE_BYTE, true);
        ConfigResolver configResolver =
                new ConfigResolver(module, Map.ofEntries(Map.entry(module, new VariableKey[]{intVar})), diagnosticLog,
                        List.of(new TomlFileProvider(getConfigPathForNegativeCases(tomlFileName))));
        configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getErrorCount(), 1);
        Assert.assertEquals(diagnosticLog.getWarningCount(), warningCount);
        Assert.assertTrue(diagnosticLog.getDiagnosticList().get(0).toString().matches(errorMsg), "Error message does " +
                "not match. Expected : " + errorMsg);
    }

    @DataProvider(name = "path-test-provider")
    public Object[][] getPathErrorTests() {
        return new Object[][]{
                {"NoConfig.toml", "warning: configuration file is not found in path '(.*)'", 1},
                {"Empty.toml",
                        "warning: an empty configuration file is found in path '(.*)'. Please provide values " +
                                "for configurable variables", 1},
                {"InvalidConfig.toml", "warning: invalid `Config.toml` file : \n" +
                        "\\[InvalidConfig.toml:\\(0:7,0:7\\)] missing identifier\n" +
                        "\\[InvalidConfig.toml:\\(0:25,0:25\\)] missing identifier\n" +
                        "\\[InvalidConfig.toml:\\(0:26,0:26\\)] missing identifier\n", 1},
                {"InvalidByteRange.toml", "error: value provided for byte variable 'test_module:byteVar' is out of " +
                        "range. Expected range is \\(0-255\\), found '355'", 0 }
        };
    }

    @Test(dataProvider = "simple-negative-tests")
    public void testSimpleNegativeConfig(String tomlFileName, String errorMsg, int errorCount) {
        VariableKey intVar = new VariableKey(module, "intVar", PredefinedTypes.TYPE_INT, true);
        VariableKey stringVar = new VariableKey(module, "stringVar", PredefinedTypes.TYPE_STRING, true);
        Map<Module, VariableKey[]> configVarMap =
                Map.ofEntries(Map.entry(module, new VariableKey[]{intVar, stringVar}));
        validateTomlProviderErrors(tomlFileName, errorMsg, configVarMap, errorCount);
    }

    @DataProvider(name = "simple-negative-tests")
    public Object[][] getSimpleNegativeTests() {
        return new Object[][]{
                {"NoModuleConfig", "[NoModuleConfig.toml:(1:1,2:12)] invalid module structure found for module " +
                        "'test_module'. Please provide the module name as '[test_module]'", 2},
                {"InvalidOrgName", "[InvalidOrgName.toml:(1:1,3:21)] invalid module structure found for module " +
                        "'test_module'. Please provide the module name as '[test_module]'", 2},
                {"InvalidOrgStructure", "[InvalidOrgStructure.toml:(1:1,1:11)] invalid module structure found for" +
                        " module 'myOrg.test_module'. Please provide the module name as '[myOrg.test_module]'", 2},
                {"InvalidModuleStructure", "[InvalidModuleStructure.toml:(1:1,1:24)] invalid module structure " +
                        "found for module 'test_module'. Please provide the module name as '[test_module]'", 2},
                {"RequiredNegative", "value not provided for required configurable variable 'stringVar'", 1},
                {"PrimitiveTypeError", "[PrimitiveTypeError.toml:(2:10,2:14)] configurable variable " +
                        "'test_module:intVar' is expected to be of type 'int', but found 'float'", 2},
                {"PrimitiveStructureError", "[PrimitiveStructureError.toml:(2:1,2:24)] configurable variable " +
                        "'test_module:intVar' is expected to be of type 'int', but found 'record'", 2},
        };
    }

    @Test()
    public void testInvalidSubModuleStructureInConfigFile() {
        VariableKey intVar = new VariableKey(subModule, "intVar", PredefinedTypes.TYPE_INT, true);
        VariableKey stringVar = new VariableKey(subModule, "stringVar", PredefinedTypes.TYPE_STRING, true);
        Map<Module, VariableKey[]> configVarMap =
                Map.ofEntries(Map.entry(subModule, new VariableKey[]{intVar, stringVar}));
        String errorMsg = "[InvalidSubModuleStructure.toml:(3:1,3:14)] invalid module structure found for module " +
                "'test_module.util.foo'. Please provide the module name as '[test_module.util.foo]'";
        validateTomlProviderErrors("InvalidSubModuleStructure", errorMsg, configVarMap, 2);
    }

    @Test(dataProvider = "array-negative-tests")
    public void testArrayNegativeConfig(String tomlFileName, String errorMsg) {
        ArrayType arrayType =  TypeCreator.createArrayType(PredefinedTypes.TYPE_INT, true);
        VariableKey intArr = new VariableKey(module, "intArr",
                new BIntersectionType(module, new Type[]{arrayType, PredefinedTypes.TYPE_READONLY}, arrayType, 0, true),
                true);
        Map<Module, VariableKey[]> configVarMap = Map.ofEntries(Map.entry(module, new VariableKey[]{intArr}));
        validateTomlProviderErrors(tomlFileName, errorMsg, configVarMap, 1);
    }

    @DataProvider(name = "array-negative-tests")
    public Object[][] getArrayNegativeTests() {
        return new Object[][]{
                {"ArrayTypeError", "[ArrayTypeError.toml:(2:10,2:17)] configurable variable 'test_module:intArr'" +
                        " is expected to be of type 'int[] & readonly', but found 'string'"},
                {"ArrayStructureError", "[ArrayStructureError.toml:(2:1,2:32)] configurable variable" +
                        " 'test_module:intArr' is expected to be of type 'int[] & readonly', but found 'record'"},
                {"ArrayElementStructure", "[ArrayElementStructure.toml:(2:19,2:26)] configurable variable " +
                        "'test_module:intArr[2]' is expected to be of type 'int', but found 'array'"},
                {"ArrayMultiType", "[ArrayMultiType.toml:(2:15,2:21)] configurable variable " +
                        "'test_module:intArr[1]' is expected to be of type 'int', but found 'string'"},
        };
    }

    @Test(dataProvider = "record-negative-tests")
    public void testRecordNegativeConfig(String tomlFileName, String errorMsg) {
        Field name = TypeCreator.createField(PredefinedTypes.TYPE_STRING, "name", SymbolFlags.REQUIRED);
        Map<String, Field> fields = Map.ofEntries(Map.entry("name", name));
        RecordType type = TypeCreator.createRecordType("Person", module, SymbolFlags.READONLY, fields, null, true, 6);

        VariableKey recordVar = new VariableKey(module, "recordVar", type, true);

        Map<Module, VariableKey[]> configVarMap = Map.ofEntries(Map.entry(module, new VariableKey[]{recordVar}));

        validateTomlProviderErrors(tomlFileName, errorMsg, configVarMap, 1);
    }

    @DataProvider(name = "record-negative-tests")
    public Object[][] getRecordNegativeTests() {
        return new Object[][]{
                {"RecordTypeError", "[RecordTypeError.toml:(2:1,2:40)] configurable variable 'test_module:recordVar' " +
                        "is expected to be of type 'test_module:Person', but found 'string'"},
                {"RecordFieldTypeError", "[RecordFieldTypeError.toml:(2:8,2:12)] field 'name' from configurable " +
                        "variable 'test_module:recordVar' is expected to be of type 'string', but found 'int'"},
                {"RecordFieldStructureError", "[RecordFieldStructureError.toml:(2:1,2:24)] field 'name' from " +
                        "configurable variable 'test_module:recordVar' is expected to be of type 'string', but found " +
                        "'record'"},
                {"AdditionalFieldRecord", "[AdditionalFieldRecord.toml:(3:1,3:9)] additional field 'age' provided for" +
                        " configurable variable 'test_module:recordVar' of record 'test_module:Person' is not " +
                        "supported"},
                {"MissingRecordField", "[MissingRecordField.toml:(1:1,1:24)] value not provided for non-defaultable " +
                        "required field 'name' of record 'test_module:Person' in configurable variable " +
                        "'test_module:recordVar'"},
        };
    }

    @Test()
    public void testInvalidMapType() {
        MapType mapType = TypeCreator.createMapType(PredefinedTypes.TYPE_INT, true);
        VariableKey mapInt = new VariableKey(module, "mapVar", mapType, true);
        Map<Module, VariableKey[]> configVarMap = Map.ofEntries(Map.entry(module, new VariableKey[]{mapInt}));
        String errorMsg =  "configurable variable 'test_module:mapVar' with type 'map<int> & readonly' is not " +
                "supported";
        validateTomlProviderErrors("InvalidMapType", errorMsg, configVarMap, 1);
    }

    @Test(dataProvider = "table-negative-tests")
    public void testTableNegativeConfig(String tomlFileName, String errorMsg) {
        Field name = TypeCreator.createField(PredefinedTypes.TYPE_STRING, "name", SymbolFlags.REQUIRED);
        Field id = TypeCreator.createField(PredefinedTypes.TYPE_INT, "id", SymbolFlags.REQUIRED);
        Field age = TypeCreator.createField(PredefinedTypes.TYPE_INT, "age", SymbolFlags.OPTIONAL);
        Map<String, Field> fields = Map.ofEntries(Map.entry("name", name), Map.entry("age", age), Map.entry("id", id));
        RecordType type = TypeCreator.createRecordType("Person", module, SymbolFlags.READONLY, fields, null, true, 6);
        TableType tableType = TypeCreator.createTableType(type, new String[]{"name"}, true);
        IntersectionType intersectionType = new BIntersectionType(module, new Type[]{tableType,
                PredefinedTypes.TYPE_READONLY}, tableType, 1, true);

        VariableKey tableVar = new VariableKey(module, "tableVar", intersectionType, true);
        Map<Module, VariableKey[]> configVarMap = Map.ofEntries(Map.entry(module, new VariableKey[]{tableVar}));

        validateTomlProviderErrors(tomlFileName, errorMsg, configVarMap, 1);
    }

    @DataProvider(name = "table-negative-tests")
    public Object[][] getTableNegativeTests() {
        return new Object[][]{
                {"MissingTableKey", "[MissingTableKey.toml:(6:1,8:9)] value required for key 'name' of " +
                        "type 'table<test_module:Person> key(name) & readonly' in configurable variable" +
                        " 'test_module:tableVar'"},
                {"TableTypeError", "[TableTypeError.toml:(1:1,3:9)] configurable variable 'test_module:tableVar'" +
                        " is expected to be of type 'table<test_module:Person> key(name) & readonly'," +
                        " but found 'record'"},
                {"TableFieldTypeError", "[TableFieldTypeError.toml:(2:8,2:11)] field 'name' " +
                        "from configurable variable 'test_module:tableVar' is expected to be of type 'string'," +
                        " but found 'int'"},
                {"TableFieldStructureError", "[TableFieldStructureError.toml:(2:1,2:24)] field 'name' " +
                        "from configurable variable 'test_module:tableVar' is expected to be of type 'string'," +
                        " but found 'record'"},
                {"AdditionalField", "[AdditionalField.toml:(4:1,4:17)] additional field 'city' provided for" +
                        " configurable variable 'test_module:tableVar' of record 'test_module:Person'" +
                        " is not supported"},
                {"MissingTableField", "[MissingTableField.toml:(1:1,3:9)] value not provided for " +
                        "non-defaultable required field 'id' of record 'test_module:Person' in configurable" +
                        " variable 'test_module:tableVar'"},
        };
    }

    @Test()
    public void testInvalidTableConstraint() {
        MapType mapType =  TypeCreator.createMapType(PredefinedTypes.TYPE_STRING, true);
        TableType tableType = TypeCreator.createTableType(mapType,  true);
        IntersectionType intersectionType = new BIntersectionType(module, new Type[]{tableType,
                PredefinedTypes.TYPE_READONLY}, tableType, 1, true);

        VariableKey tableVar = new VariableKey(module, "tableVar", intersectionType, true);

        Map<Module, VariableKey[]> configVarMap = Map.ofEntries(Map.entry(module, new VariableKey[]{tableVar}));
        String errorMsg = "[InvalidTableConstraint.toml:(1:1,2:16)] table constraint type 'map<string> & readonly'" +
                " in configurable variable 'test_module:tableVar' is not supported";
        validateTomlProviderErrors("InvalidTableConstraint", errorMsg, configVarMap, 1);
    }

    private void validateTomlProviderErrors(String tomlFileName, String errorMsg,
                                            Map<Module, VariableKey[]> configVarMap, int errorCount) {
        DiagnosticLog diagnosticLog = new DiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(module, configVarMap, diagnosticLog,
                List.of(new TomlFileProvider(getConfigPathForNegativeCases(tomlFileName + ".toml"))));
        configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getErrorCount(), errorCount);
        Assert.assertEquals(diagnosticLog.getWarningCount(), 0);
        Assert.assertEquals(diagnosticLog.getDiagnosticList().get(0).toString(), "error: " + errorMsg);
    }

    @Test
    public void testTomlProviderWithStringNegative() {
        Map<Module, VariableKey[]> configVarMap = new HashMap<>();
        VariableKey[] keys = getSimpleVariableKeys(module);
        String tomlContent = "[test_module] intVar = 42.22 floatVar = 3 stringVar = 11";
        configVarMap.put(module, keys);
        DiagnosticLog diagnosticLog = new DiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap, diagnosticLog,
                                                           List.of(new TomlContentProvider(tomlContent)));
        configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getErrorCount(), 2);
        Assert.assertEquals(diagnosticLog.getDiagnosticList().get(0).toString(), "error: [BAL_CONFIG_DATA:(1:24,1:29)" +
                "] configurable variable 'test_module:intVar' is expected to be of type 'int', but found 'float'");
        Assert.assertEquals(diagnosticLog.getDiagnosticList().get(1).toString(), "error: [BAL_CONFIG_DATA:(1:55,1:57)" +
                "] configurable variable 'test_module:stringVar' is expected to be of type 'string', but found 'int'");
    }

    @Test
    public void testMultipleTomlProvidersNegative() {
        Map<Module, VariableKey[]> configVarMap = Map.ofEntries(Map.entry(module, getSimpleVariableKeys(module)));
        DiagnosticLog diagnosticLog = new DiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(ROOT_MODULE, configVarMap, diagnosticLog,
                                                           List.of(new TomlFileProvider(getConfigPath("Config_First.toml")),
                                                                   new TomlFileProvider(getConfigPath("Config_Second.toml"))));
        configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getErrorCount(), 1);
        Assert.assertEquals(diagnosticLog.getDiagnosticList().get(0).toString(), "error: value not provided for " +
                "required configurable variable 'myOrg/test_module:stringVar'");
    }

    private VariableKey[] getSimpleVariableKeys(Module module) {
        VariableKey intVar = new VariableKey(module, "intVar", PredefinedTypes.TYPE_INT, true);
        VariableKey stringVar = new VariableKey(module, "stringVar", PredefinedTypes.TYPE_STRING, true);
        return new VariableKey[]{intVar, stringVar};
    }

}

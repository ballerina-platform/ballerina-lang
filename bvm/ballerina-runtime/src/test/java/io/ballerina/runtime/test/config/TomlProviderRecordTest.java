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
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.configurable.ConfigResolver;
import io.ballerina.runtime.internal.configurable.VariableKey;
import io.ballerina.runtime.internal.configurable.providers.toml.ConfigValueCreator;
import io.ballerina.runtime.internal.configurable.providers.toml.TomlFileProvider;
import io.ballerina.runtime.internal.diagnostics.RuntimeDiagnosticLog;
import io.ballerina.toml.semantic.ast.TomlNode;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.ballerina.runtime.api.PredefinedTypes.TYPE_ANYDATA;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_BOOLEAN;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_FLOAT;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_INT;
import static io.ballerina.runtime.api.PredefinedTypes.TYPE_STRING;
import static io.ballerina.runtime.api.utils.StringUtils.fromString;
import static io.ballerina.runtime.test.TestUtils.getConfigPath;

/**
 * Test cases for toml configuration with record type.
 */
public class TomlProviderRecordTest {

    private static final Module ROOT_MODULE = new Module("rootOrg", "test_module", "1.0.0");
    private static final ConfigValueCreator valueCreator = new ConfigValueCreator();

    @Test(dataProvider = "record-data-provider")
    public void testTomlProviderRecords(String variableName, Map<String, Field> fields,
                                        Map<String, Object> expectedValues) {

        RecordType type =
                TypeCreator.createRecordType("Person", ROOT_MODULE, SymbolFlags.READONLY, fields, null, true, 6);
        VariableKey recordVar = new VariableKey(ROOT_MODULE, variableName, type, true);
        Map<Module, VariableKey[]> configVarMap = Map.ofEntries(Map.entry(ROOT_MODULE, new VariableKey[]{recordVar}));
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(configVarMap, diagnosticLog,
                List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath("RecordTypeConfig.toml"),
                        configVarMap.keySet())));
        Map<VariableKey, Object> configValueMap = configResolver.resolveConfigs();
        Object recordValue = valueCreator.retrieveValue((TomlNode) configValueMap.get(recordVar), recordVar.variable,
                recordVar.type);
        Assert.assertTrue(recordValue instanceof BMap<?, ?>, "Non-map value received for variable : " + variableName);
        BMap<?, ?> record = (BMap<?, ?>) recordValue;
        for (Map.Entry<String, Object> expectedField : expectedValues.entrySet()) {
            BString fieldName = fromString(fields.get(expectedField.getKey()).getFieldName());
            Assert.assertEquals((record.get(fieldName)), expectedField.getValue());
        }
    }

    @DataProvider(name = "record-data-provider")
    public Object[][] recordDataProvider() {
        Field name = TypeCreator.createField(TYPE_STRING, "name", SymbolFlags.REQUIRED);
        Field nameReadOnly = TypeCreator.createField(TYPE_STRING, "name", SymbolFlags.READONLY);
        Field age = TypeCreator.createField(TYPE_INT, "age", SymbolFlags.OPTIONAL);
        return new Object[][]{
                {"requiredFieldRecord", Map.ofEntries(Map.entry("name", name)),
                        Map.ofEntries(Map.entry("name", fromString("John")))},
                {"readonlyFieldRecord", Map.ofEntries(Map.entry("name", nameReadOnly)),
                        Map.ofEntries(Map.entry("name", fromString("Jade")))},
                {"optionalFieldRecord", Map.ofEntries(Map.entry("name", nameReadOnly), Map.entry("age", age)),
                        Map.ofEntries(Map.entry("name", fromString("Anna")), Map.entry("age", 21L))},
                {"optionalMissingField", Map.ofEntries(Map.entry("name", nameReadOnly), Map.entry("age", age)),
                        Map.ofEntries(Map.entry("name", fromString("Peter")))},
        };
    }

    @Test(dataProvider = "record-rest-provider")
    public void testRestFieldsInRecords(Type restFieldType, Map<String, Object> expectedValues, String varName) {
        RecordType recordType = TypeCreator.createRecordType("Person", ROOT_MODULE, SymbolFlags.READONLY,
                new HashMap<>(), restFieldType, false, 6);
        VariableKey recordVar = new VariableKey(ROOT_MODULE, varName, recordType, true);
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(Map.ofEntries(Map.entry(ROOT_MODULE,
                new VariableKey[]{recordVar})), diagnosticLog, List.of(new TomlFileProvider(ROOT_MODULE,
                getConfigPath("RestFieldConfig.toml"), Set.of(ROOT_MODULE))));
        Map<VariableKey, Object> configValueMap = configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getErrorCount(), 0);
        Object recordValue = valueCreator.retrieveValue((TomlNode) configValueMap.get(recordVar), recordVar.variable,
                recordVar.type);
        Assert.assertTrue(recordValue instanceof BMap<?, ?>, "Non-map value received");
        BMap<?, ?> record = (BMap<?, ?>) recordValue;
        for (Map.Entry<String, Object> expectedField : expectedValues.entrySet()) {
            BString fieldName = fromString(expectedField.getKey());
            Assert.assertEquals((record.get(fieldName)), expectedField.getValue());
        }
    }

    @DataProvider(name = "record-rest-provider")
    public Object[][] recordRestDataProvider() {
        BMap<BString, Object> mapValue = ValueCreator.createMapValue();
        mapValue.put(fromString("m"), fromString("value"));
        mapValue.put(fromString("n"), 608L);
        return new Object[][]{
                {TYPE_INT, Map.ofEntries(Map.entry("id", 11L), Map.entry("age", 26L)), "intRest"},
                {TYPE_FLOAT, Map.ofEntries(Map.entry("height", 161.5), Map.entry("weight", 62.3)), "floatRest"},
                {TYPE_STRING, Map.ofEntries(Map.entry("name", fromString("Hinduja")), Map.entry("city", fromString(
                        "Colombo"))), "stringRest"},
                {TYPE_BOOLEAN, Map.ofEntries(Map.entry("isBoolean", true), Map.entry("isByte", false)), "booleanRest"},
                {TYPE_ANYDATA, Map.ofEntries(Map.entry("intVal", 100L), Map.entry("floatVal", 103.507),
                        Map.entry("stringVal", fromString("string")), Map.entry("mapVal", mapValue)), "anydataRest"},
        };
    }


    @Test
    public void testAdditionalFieldsInOpenRecords() {
        RecordType recordType = TypeCreator.createRecordType("Person", ROOT_MODULE, SymbolFlags.READONLY,
                new HashMap<>(), TYPE_ANYDATA, false, 6);
        VariableKey recordVar = new VariableKey(ROOT_MODULE, "recordVar", recordType, true);
        RuntimeDiagnosticLog diagnosticLog = new RuntimeDiagnosticLog();
        ConfigResolver configResolver = new ConfigResolver(Map.ofEntries(Map.entry(ROOT_MODULE,
                new VariableKey[]{recordVar})), diagnosticLog, List.of(new TomlFileProvider(ROOT_MODULE, getConfigPath(
                "OpenRecordConfig.toml"), Set.of(ROOT_MODULE))));
        Map<VariableKey, Object> configValueMap = configResolver.resolveConfigs();
        Assert.assertEquals(diagnosticLog.getErrorCount(), 0);
        Assert.assertEquals(diagnosticLog.getWarningCount(), 0);
        Object recordValue = valueCreator.retrieveValue((TomlNode) configValueMap.get(recordVar), recordVar.variable,
                recordVar.type);
        Assert.assertTrue(recordValue instanceof BMap<?, ?>, "Non-map value received");
        BMap<?, ?> record = (BMap<?, ?>) recordValue;
        Assert.assertEquals(record.getIntValue(fromString("intValue")), (Long) 25L);
        Assert.assertEquals(record.getFloatValue(fromString("floatValue")), 25.54);
        Assert.assertEquals(record.getStringValue(fromString("stringValue")), fromString("test"));
        Assert.assertEquals(record.getBooleanValue(fromString("booleanValue")), (Boolean) true);
    }

}

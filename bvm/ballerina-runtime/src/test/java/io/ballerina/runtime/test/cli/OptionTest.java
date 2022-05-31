/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.runtime.test.cli;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.PredefinedTypes;
import io.ballerina.runtime.api.creators.TypeCreator;
import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.Field;
import io.ballerina.runtime.api.types.RecordType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BError;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.internal.cli.CliSpec;
import io.ballerina.runtime.internal.cli.Operand;
import io.ballerina.runtime.internal.cli.Option;
import io.ballerina.runtime.internal.values.DecimalValue;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Tests the options feature in bal cli commands.
 */
public class OptionTest {
    private static final Module module = new Module("$anon", ".", "0");
    public static final String RECORD_NAME = "Student";
    public static final String ARRAY_NAME = "array";

    @Test
    public void testOperandWithOption() {
        Field stringField = TypeCreator.createField(PredefinedTypes.TYPE_STRING, "name", 1);
        Map<String, Field> fields = Map.ofEntries(Map.entry("name", stringField));
        RecordType type = TypeCreator.createRecordType(RECORD_NAME, module, 1, fields, null, true, 6);
        Operand[] operands = {new Operand(false, "birth", PredefinedTypes.TYPE_STRING)};
        Option option = new Option(type, ValueCreator.createRecordValue(type), operands.length);
        String[] args = {"--name", "Riyafa", "-Ckey=value", "--", "--Sri Lanka"};
        CliSpec cliSpec = new CliSpec(option, operands, args);
        Object[] mainArgs = cliSpec.getMainArgs();
        Assert.assertEquals(StringUtils.fromString("--Sri Lanka"), mainArgs[1]);
        BMap map = (BMap) mainArgs[3];
        Assert.assertEquals(map.get(StringUtils.fromString("name")), StringUtils.fromString("Riyafa"));
        args[0] = "--na";
        try {
            new CliSpec(option, operands, args).getMainArgs();
            Assert.fail();
        } catch (BError err) {
            Assert.assertEquals(err.getMessage(), "undefined option: 'na'");
        }
    }

    @Test
    public void testOptionArgs() {
        assertOptionArgTypes(new String[]{"--union", "e-fac", "--name", "Riyafa", "--age", "25", "--score", "99.99",
                "--value", "1e10"});
    }

    @Test
    public void testNamedArgOptions() {
        assertOptionArgTypes(new String[]{"--union=e-fac", "--name=Riyafa", "--age=25", "--score=99.99",
                "--value=1e10"});
    }

    private void assertOptionArgTypes(String[] args) {
        Field unionWithNilField = TypeCreator.createField(
                TypeCreator.createUnionType(PredefinedTypes.TYPE_STRING, PredefinedTypes.TYPE_NULL), "union", 1);
        Field stringField = TypeCreator.createField(PredefinedTypes.TYPE_STRING, "name", 1);
        Field intField = TypeCreator.createField(PredefinedTypes.TYPE_INT, "age", 1);
        Field floatField = TypeCreator.createField(PredefinedTypes.TYPE_FLOAT, "score", 1);
        Field decimalField = TypeCreator.createField(PredefinedTypes.TYPE_DECIMAL, "value", 1);
        Map<String, Field> fields = new HashMap<>();
        fields.put(unionWithNilField.getFieldName(), unionWithNilField);
        fields.put(stringField.getFieldName(), stringField);
        fields.put(intField.getFieldName(), intField);
        fields.put(floatField.getFieldName(), floatField);
        fields.put(decimalField.getFieldName(), decimalField);
        RecordType type = TypeCreator.createRecordType(RECORD_NAME, module, 1, fields, null, true, 6);
        Option option = new Option(type, ValueCreator.createRecordValue(type));
        CliSpec cliSpec = new CliSpec(option, new Operand[0], args);
        Object[] mainArgs = cliSpec.getMainArgs();
        BMap map = (BMap) mainArgs[1];
        Assert.assertEquals(map.get(StringUtils.fromString(unionWithNilField.getFieldName())),
                            StringUtils.fromString("e-fac"));
        Assert.assertEquals(map.get(StringUtils.fromString(stringField.getFieldName())),
                            StringUtils.fromString("Riyafa"));
        Assert.assertEquals(map.get(StringUtils.fromString(intField.getFieldName())), 25L);
        Assert.assertEquals(map.get(StringUtils.fromString(floatField.getFieldName())), 99.99);
        Assert.assertEquals(((DecimalValue) map.get(StringUtils.fromString(decimalField.getFieldName()))).value(),
                            new BigDecimal("1e10"));
    }

    @Test
    public void testInvalidType() {
        Field anyField = TypeCreator.createField(PredefinedTypes.TYPE_ANY, "union", 1);
        Map<String, Field> fields = new HashMap<>();
        fields.put(anyField.getFieldName(), anyField);
        RecordType type = TypeCreator.createRecordType(RECORD_NAME, module, 1, fields, null, true, 6);
        Option option = new Option(type, ValueCreator.createRecordValue(type));
        String[] args = {"--union=e-fac"};
        CliSpec cliSpec = new CliSpec(option, new Operand[0], args);
        try {
            cliSpec.getMainArgs();
            Assert.fail();
        } catch (BError error) {
            Assert.assertEquals(error.getMessage(), "unsupported type expected with main function 'any'");
        }
    }

    @Test
    public void testUnionWithNilOnly() {
        Type unionType = TypeCreator.createUnionType(PredefinedTypes.TYPE_STRING, PredefinedTypes.TYPE_NULL);
        Option option = getOption(unionType, "union");
        CliSpec cliSpec = new CliSpec(option, new Operand[0], "--union=hello");
        Object[] mainArgs = cliSpec.getMainArgs();
        BMap map = (BMap) mainArgs[1];
        Assert.assertEquals(map.get(StringUtils.fromString("union")), StringUtils.fromString("hello"));
    }

    @Test
    public void testInvalidUnionType() {
        Type unionType = TypeCreator.createUnionType(PredefinedTypes.TYPE_STRING, PredefinedTypes.TYPE_INT);
        CliSpec cliSpec = new CliSpec(getOption(unionType, "union"), new Operand[0], "--union=e-fac");
        try {
            cliSpec.getMainArgs();
            Assert.fail();
        } catch (BError error) {
            Assert.assertEquals(error.getMessage(), "unsupported type expected with main function '(string|int)'");
        }
    }

    @Test
    public void testRepeatedOption() {
        String optionName = "stringVal";
        CliSpec cliSpec = new CliSpec(getOption(PredefinedTypes.TYPE_STRING, optionName), new Operand[0], "--stringVal",
                                      "val1", "--stringVal", "val2");
        try {
            cliSpec.getMainArgs();
            Assert.fail();
        } catch (BError error) {
            Assert.assertEquals(error.getMessage(), "The option 'stringVal' cannot be repeated");
        }
    }

    @Test
    public void testRepeatedNamedOption() {
        String optionName = "stringVal";
        CliSpec cliSpec = new CliSpec(getOption(PredefinedTypes.TYPE_STRING, optionName), new Operand[0],
                                      "--stringVal=val1", "--stringVal=val2");
        try {
            cliSpec.getMainArgs();
            Assert.fail();
        } catch (BError error) {
            Assert.assertEquals(error.getMessage(), "The option 'stringVal' cannot be repeated");
        }
    }

    @Test
    public void testRepeatedBooleanOption() {
        String optionName = "booleanVal";
        Option option = getOption(PredefinedTypes.TYPE_BOOLEAN, optionName);
        CliSpec cliSpec = new CliSpec(option, new Operand[0], "--booleanVal", "--booleanVal");
        try {
            cliSpec.getMainArgs();
            Assert.fail();
        } catch (BError error) {
            Assert.assertEquals(error.getMessage(), "The option 'booleanVal' cannot be repeated");
        }
    }

    @Test
    public void testArrayType() {
        Field arrayField = TypeCreator.createField(
                TypeCreator.createArrayType(PredefinedTypes.TYPE_STRING), ARRAY_NAME, 1);
        Map<String, Field> fields = new HashMap<>();
        fields.put(arrayField.getFieldName(), arrayField);
        RecordType type = TypeCreator.createRecordType(RECORD_NAME, module, 1, fields, null, true, 6);
        BArray array = getMap(type, new String[]{"--array=e-fac"});
        Assert.assertEquals(array.get(0).toString(), "e-fac");
        array = getMap(type, new String[]{"--array=e-fac", "--array=hello"});
        Assert.assertEquals(array.get(0).toString(), "e-fac");
        Assert.assertEquals(array.get(1).toString(), "hello");
        array = getMap(type, new String[]{"--array", "e-fac"});
        Assert.assertEquals(array.get(0).toString(), "e-fac");
        array = getMap(type, new String[]{"--array", "e-fac", "--array", "hello"});
        Assert.assertEquals(array.get(0).toString(), "e-fac");
        Assert.assertEquals(array.get(1).toString(), "hello");
    }

    @Test
    public void testInvalidArrayType() {
        Field arrayField = TypeCreator.createField(
                TypeCreator.createArrayType(PredefinedTypes.TYPE_INT), ARRAY_NAME, 1);
        Map<String, Field> fields = new HashMap<>();
        fields.put(arrayField.getFieldName(), arrayField);
        RecordType type = TypeCreator.createRecordType(RECORD_NAME, module, 1, fields, null, true, 6);
        try {
            getMap(type, new String[]{"--array", "10", "--array", "arr-val"});
            Assert.fail();
        } catch (BError error) {
            Assert.assertEquals(error.getMessage(),
                                "invalid argument 'arr-val' for parameter 'array', expected integer value");
        }
    }
    private BArray getMap(RecordType type, String[] args) {
        Option option = new Option(type, ValueCreator.createRecordValue(type));
        CliSpec cliSpec = new CliSpec(option, new Operand[0], args);
        Object[] mainArgs = cliSpec.getMainArgs();
        return (BArray) ((BMap) mainArgs[1]).get(StringUtils.fromString(ARRAY_NAME));
    }

    @Test
    public void testBooleanArrayType() {
        Field arrayField = TypeCreator.createField(
                TypeCreator.createArrayType(PredefinedTypes.TYPE_BOOLEAN), ARRAY_NAME, 1);
        Map<String, Field> fields = new HashMap<>();
        fields.put(arrayField.getFieldName(), arrayField);
        RecordType type = TypeCreator.createRecordType(RECORD_NAME, module, 1, fields, null, true, 6);
        BArray array = getMap(type, new String[]{"--array"});
        Assert.assertEquals(array.get(0), true);
        array = getMap(type, new String[]{"--array", "--array"});
        Assert.assertEquals(array.get(0), true);
        Assert.assertEquals(array.get(1), true);
        try {
            getMap(type, new String[]{"--array=true"});
            Assert.fail();
        } catch (BError error) {
            Assert.assertEquals(error.getMessage(),
                                "the option 'array' of type 'boolean' is expected without a value");
        }
        try {
            getMap(type, new String[]{"--array=false"});
            Assert.fail();
        } catch (BError error) {
            Assert.assertEquals(error.getMessage(),
                                "the option 'array' of type 'boolean' is expected without a value");
        }
    }

    @Test
    public void testBooleanType() {
        Option option = getOption(PredefinedTypes.TYPE_BOOLEAN, "bool");
        CliSpec cliSpec = new CliSpec(option, new Operand[0], "--bool");
        Object[] mainArgs = cliSpec.getMainArgs();
        BMap map = (BMap) mainArgs[1];
        Assert.assertEquals(map.get(StringUtils.fromString("bool")), true);
    }

    private Option getOption(Type optionType, String paramName) {
        Field field = TypeCreator.createField(optionType, paramName, 1);
        Map<String, Field> fields = Map.ofEntries(Map.entry(field.getFieldName(), field));
        RecordType type = TypeCreator.createRecordType(RECORD_NAME, module, 1, fields, null, true, 6);
        return new Option(type, ValueCreator.createRecordValue(type));
    }


    @Test
    public void testBooleanNamedArgument() {
        Option option = getOption(PredefinedTypes.TYPE_BOOLEAN, "bool");
        try {
            CliSpec cliSpec = new CliSpec(option, new Operand[0], "--bool=false");
            cliSpec.getMainArgs();
            Assert.fail();
        } catch (BError error) {
            Assert.assertEquals(error.getMessage(),
                                "the option 'bool' of type 'boolean' is expected without a value");
        }
    }

    @DataProvider(name = "invalidTypes")
    public Object[][] invalidTypes() {
        return new Object[][]{{PredefinedTypes.TYPE_DECIMAL, "decimal"}, {PredefinedTypes.TYPE_FLOAT, "float"},
                {PredefinedTypes.TYPE_INT, "integer"}};
    }

    @Test(dataProvider = "invalidTypes")
    public void testInvalidTypes(Type type, String typeStr) {
        testInvalid(type, new String[]{"--val=name"},
                    "invalid argument 'name' for parameter 'val', expected " + typeStr + " value");
    }

    @Test()
    public void testInvalidDecimal() {
        testInvalid(PredefinedTypes.TYPE_DECIMAL, new String[]{"--val=99999999.9e9999999999"},
                "invalid argument '99999999.9e9999999999' for parameter 'val', expected decimal value");
    }

    @DataProvider(name = "invalid")
    public Object[][] testInvalidOption() {
        return new Object[][]{{PredefinedTypes.TYPE_STRING, new String[]{"-val"}, "undefined CLI argument: '-val'"},
                {PredefinedTypes.TYPE_STRING, new String[]{"--val"}, "Missing option argument for '--val'"},
                {PredefinedTypes.TYPE_STRING, new String[]{"--val", "--name"},
                        "Missing option argument for '--val'"}};
    }

    @Test(dataProvider = "invalid")
    public void testInvalid(Type type, String[] args, String errMessage) {
        Option option = getOption(type, "val");
        CliSpec cliSpec = new CliSpec(option, new Operand[0], args);
        try {
            cliSpec.getMainArgs();
            Assert.fail();
        } catch (BError error) {
            Assert.assertEquals(error.getMessage(), errMessage);
        }
    }
}

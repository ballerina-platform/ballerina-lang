/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.record;

import io.ballerina.runtime.api.StringUtils;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.values.MapValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.StringJoiner;

/**
 * Test cases for the generated class and methods for record types.
 */
public class RecordValueTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/record/record_value.bal");
    }

    @Test
    public void testRecordOperations_1() {
        Object result = BRunUtil.invokeAndGetJVMResult(compileResult, "getDefaultPerson");
        Assert.assertTrue(result instanceof MapValue);
        MapValue<BString, Object> person = (MapValue<BString, Object>) result;

        // test contains key, for required field
        Assert.assertTrue(person.containsKey(StringUtils.fromString("name")));

        // get existing field
        Assert.assertEquals(person.get(StringUtils.fromString("name")).toString(), "John");

        // test contains key, for non-existing optional field
        Assert.assertFalse(person.containsKey(StringUtils.fromString("spouse")));

        // get optional field that is not set
        Assert.assertNull(person.get(StringUtils.fromString("spouse")));

        // set rest field
        Assert.assertNull(person.put(StringUtils.fromString("last-name"), StringUtils.fromString("Doe")));
        Assert.assertEquals(person.get(StringUtils.fromString("last-name")), StringUtils.fromString("Doe"));

        // test contains key, for non-existing rest field
        Assert.assertFalse(person.containsKey(StringUtils.fromString("first-name")));

        // test contains key, for existing rest field
        Assert.assertTrue(person.containsKey(StringUtils.fromString("last-name")));

        // test entry set
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        for (Map.Entry<BString, Object> entry : person.entrySet()) {
            sj.add("{" + entry.getKey() + ":" + entry.getValue() + "}");
        }
        Assert.assertEquals(sj.toString(), "[{name:John}, {age:30}, {last-name:Doe}]");

        // test key set
        sj = new StringJoiner(", ", "[", "]");
        for (BString key : person.getKeys()) {
            sj.add(key.getValue());
        }
        Assert.assertEquals(sj.toString(), "[name, age, last-name]");

        // test get values
        sj = new StringJoiner(", ", "[", "]");
        for (Object value : person.values()) {
            sj.add(value.toString());
        }
        Assert.assertEquals(sj.toString(), "[John, 30, Doe]");

        Assert.assertEquals(person.size(), 3);
        Assert.assertFalse(person.isEmpty());
        Assert.assertEquals(person.stringValue(null), "{\"name\":\"John\",\"age\":30,\"last-name\":\"Doe\"}");

        // set optional field
        person.put(StringUtils.fromString("spouse"), StringUtils.fromString("Jane"));
        Assert.assertEquals(person.get(StringUtils.fromString("spouse")).toString(), "Jane");

        // test contains key, for existing optional field
        Assert.assertTrue(person.containsKey(StringUtils.fromString("spouse")));

    }

    @Test
    public void testRecordOperations_2() {
        Object result = BRunUtil.invokeAndGetJVMResult(compileResult, "getNewPerson");
        Assert.assertTrue(result instanceof MapValue);
        MapValue<BString, Object> person = (MapValue<BString, Object>) result;

        // test contains key, for required field
        Assert.assertTrue(person.containsKey(StringUtils.fromString("name")));

        // get existing field
        Assert.assertEquals(person.get(StringUtils.fromString("name")).toString(), "Jane");

        // test contains key, for non-existing optional field
        Assert.assertTrue(person.containsKey(StringUtils.fromString("spouse")));

        // get optional field that is already set
        Assert.assertEquals(person.get(StringUtils.fromString("spouse")).toString(), "John");

        // get optional rest field that is already set
        Assert.assertEquals(person.get(StringUtils.fromString("gender")).toString(), "female");

        // test entry set
        StringJoiner sj = new StringJoiner(", ", "[", "]");
        for (Map.Entry<BString, Object> entry : person.entrySet()) {
            sj.add("{" + entry.getKey() + ":" + entry.getValue() + "}");
        }
        Assert.assertEquals(sj.toString(), "[{name:Jane}, {age:25}, {spouse:John}, {gender:female}]");

        // test key set
        sj = new StringJoiner(", ", "[", "]");
        for (BString key : person.getKeys()) {
            sj.add(key.getValue());
        }
        Assert.assertEquals(sj.toString(), "[name, age, spouse, gender]");

        // test get values
        sj = new StringJoiner(", ", "[", "]");
        for (Object value : person.values()) {
            sj.add(value.toString());
        }
        Assert.assertEquals(sj.toString(), "[Jane, 25, John, female]");

        Assert.assertEquals(person.size(), 4);
        Assert.assertFalse(person.isEmpty());

        Assert.assertEquals(person.stringValue(null),
                "{\"name\":\"Jane\",\"age\":25,\"spouse\":\"John\",\"gender\":\"female\"}");
    }

    @Test(expectedExceptions = {UnsupportedOperationException.class})
    public void testRecordRemove() {
        Object result = BRunUtil.invokeAndGetJVMResult(compileResult, "getDefaultPerson");
        Assert.assertTrue(result instanceof MapValue);
        MapValue<BString, Object> person = (MapValue<BString, Object>) result;
        person.remove(StringUtils.fromString("name"));
    }

    @Test(expectedExceptions = { UnsupportedOperationException.class })
    public void testRecordClear() {
        Object result = BRunUtil.invokeAndGetJVMResult(compileResult, "getDefaultPerson");
        Assert.assertTrue(result instanceof MapValue);
        MapValue<String, Object> person = (MapValue<String, Object>) result;
        person.clear();
    }
}

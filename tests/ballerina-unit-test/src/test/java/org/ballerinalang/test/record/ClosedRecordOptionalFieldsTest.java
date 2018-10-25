/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for optional fields in closed records.
 *
 * @since 0.982.1
 */
public class ClosedRecordOptionalFieldsTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/record/closed_record_optional_fields.bal");
    }

    @Test(description = "Test for the compile errors")
    public void testNegatives() {
        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/record/closed_record_optional_fields_negatives.bal");
        int i = 0;
        Assert.assertEquals(negativeResult.getErrorCount(), 3);
        BAssertUtil.validateError(negativeResult, i++, "a default value specified for optional field 'age'", 22, 5);
        BAssertUtil.validateError(negativeResult, i++, "missing non-defaultable required record field 'adrs'", 35, 17);
        BAssertUtil.validateError(negativeResult, i, "variable 'p2' is not initialized", 36, 5);
    }

    @Test(description = "Test creating a record with a non-defaultable required field")
    public void testNonDefReqField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNonDefReqField");

        BMap person = (BMap) returns[0];
        Assert.assertEquals(person.get("fname").stringValue(), "default");
        Assert.assertNull(person.get("lname"));
        Assert.assertNull(person.get("age"));

        BMap adrs = (BMap) person.get("adrs");
        Assert.assertEquals(adrs.get("street").stringValue(), "");
        Assert.assertEquals(adrs.get("city").stringValue(), "");
        Assert.assertEquals(adrs.get("country").stringValue(), "LK");
    }

    @Test(description = "Test creating a record with a non-defaultable required field with explicit values for " +
            "required fields")
    public void testNonDefReqField2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testNonDefReqField2");

        BMap person = (BMap) returns[0];
        Assert.assertEquals(person.get("fname").stringValue(), "John");
        Assert.assertEquals(person.get("lname").stringValue(), "Doe");
        Assert.assertNull(person.get("age"));

        BMap adrs = (BMap) person.get("adrs");
        Assert.assertEquals(adrs.get("street").stringValue(), "");
        Assert.assertEquals(adrs.get("city").stringValue(), "");
        Assert.assertEquals(adrs.get("country").stringValue(), "LK");
    }

    @Test(description = "Test defaultable user defined type as a required field")
    public void testDefaultableReqField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testDefaultableReqField");

        BMap person = (BMap) returns[0];
        Assert.assertEquals(person.get("fname").stringValue(), "default");
        Assert.assertNull(person.get("lname"));
        Assert.assertNull(person.get("age"));

        BMap adrs = (BMap) person.get("adrs");
        Assert.assertEquals(adrs.get("street").stringValue(), "");
        Assert.assertEquals(adrs.get("city").stringValue(), "");
        Assert.assertEquals(adrs.get("country").stringValue(), "LK");
    }

    @Test(description = "Test non-defaultable user defined type as an optional field")
    public void testOptionalNonDefField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testOptionalNonDefField");
        BMap person = (BMap) returns[0];
        Assert.assertEquals(person.get("fname").stringValue(), "default");
        Assert.assertNull(person.get("lname"));
        Assert.assertNull(person.get("age"));
        Assert.assertNull(person.get("adrs"));
    }

    @Test(description = "Test non-defaultable optional field access", expectedExceptions =
            BLangRuntimeException.class, expectedExceptionsMessageRegExp = ".*cannot find key 'adrs'.*")
    public void testOptionalNonDefField2() {
        BRunUtil.invoke(compileResult, "testOptionalNonDefField2");
    }

    @Test(description = "Test defaultable user defined type as an optional field")
    public void testOptionalDefaultableField() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testOptionalDefaultableField");

        BMap person = (BMap) returns[0];
        Assert.assertEquals(person.get("fname").stringValue(), "default");
        Assert.assertNull(person.get("lname"));
        Assert.assertNull(person.get("age"));
        Assert.assertNull(person.get("adrs"));
    }
}

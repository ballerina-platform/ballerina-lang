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

import org.ballerinalang.core.model.types.TypeTags;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for optional fields in open records.
 *
 * @since 0.982.1
 */
public class OpenRecordOptionalFieldsTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/record/open_record_optional_fields.bal");
    }

    @Test(description = "Test for the compile errors", groups = { "disableOnOldParser" })
    public void testNegatives() {
        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/record/open_record_optional_fields_negatives.bal");
        int i = 0;
        Assert.assertEquals(negativeResult.getErrorCount(), 3);
        BAssertUtil.validateError(negativeResult, i++, "invalid token '999'", 22, 19);
        BAssertUtil.validateError(negativeResult, i++, "invalid token '='", 22, 19);
        BAssertUtil.validateError(negativeResult, i, "missing non-defaultable required record field 'adrs'", 33, 17);
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

    @Test(description = "Test non-defaultable optional field access",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*TypeCastError \\{\"message\":\"incompatible types: '\\(\\)' " +
                  "cannot be cast to 'Address3'.*")
    public void testOptionalNonDefField2() {
        BRunUtil.invoke(compileResult, "testOptionalNonDefField2");
    }

    @Test(description = "Test non-defaultable optional field access",
          expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*KeyNotFound \\{\"message\":\"cannot find key 'adrs'.*")
    public void testOptionalNonDefField3() {
        BRunUtil.invoke(compileResult, "testOptionalNonDefField3");
    }

    @Test(description = "Test non-defaultable optional field access")
    public void testOptionalNonDefField4() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testOptionalNonDefField4");
        Assert.assertEquals(returns[0].stringValue(), "{street:\"Palm Grove\", city:\"Colombo 3\", country:\"LK\"}");
        Assert.assertEquals(returns[0].getType().getTag(), TypeTags.RECORD_TYPE_TAG);
        Assert.assertEquals(returns[0].getType().getName(), "Address3");
        Assert.assertEquals(returns[1].stringValue(), "{street:\"Palm Grove\", city:\"Colombo 3\", country:\"LK\"}");
        Assert.assertEquals(returns[1].getType().getTag(), TypeTags.RECORD_TYPE_TAG);
        Assert.assertEquals(returns[1].getType().getName(), "Address3");
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

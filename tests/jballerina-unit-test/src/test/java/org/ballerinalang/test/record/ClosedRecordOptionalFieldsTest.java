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

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.exceptions.BLangTestException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;

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
        BAssertUtil.validateError(negativeResult, i++, "invalid token '='", 22, 14);
        BAssertUtil.validateError(negativeResult, i++, "invalid token '999'", 22, 16);
        BAssertUtil.validateError(negativeResult, i, "missing non-defaultable required record field 'adrs'", 33, 17);
    }

    @Test(description = "Test creating a record with a non-defaultable required field")
    public void testNonDefReqField() {
        Object returns = BRunUtil.invoke(compileResult, "testNonDefReqField");

        BMap person = (BMap) returns;
        Assert.assertEquals(person.get(StringUtils.fromString("fname")).toString(), "default");
        Assert.assertNull(person.get(StringUtils.fromString("lname")));
        Assert.assertNull(person.get(StringUtils.fromString("age")));

        BMap adrs = (BMap) person.get(StringUtils.fromString("adrs"));
        Assert.assertEquals(adrs.get(StringUtils.fromString("street")).toString(), "");
        Assert.assertEquals(adrs.get(StringUtils.fromString("city")).toString(), "");
        Assert.assertEquals(adrs.get(StringUtils.fromString("country")).toString(), "LK");
    }

    @Test(description = "Test creating a record with a non-defaultable required field with explicit values for " +
            "required fields")
    public void testNonDefReqField2() {
        Object returns = BRunUtil.invoke(compileResult, "testNonDefReqField2");

        BMap person = (BMap) returns;
        Assert.assertEquals(person.get(StringUtils.fromString("fname")).toString(), "John");
        Assert.assertEquals(person.get(StringUtils.fromString("lname")).toString(), "Doe");
        Assert.assertNull(person.get(StringUtils.fromString("age")));

        BMap adrs = (BMap) person.get(StringUtils.fromString("adrs"));
        Assert.assertEquals(adrs.get(StringUtils.fromString("street")).toString(), "");
        Assert.assertEquals(adrs.get(StringUtils.fromString("city")).toString(), "");
        Assert.assertEquals(adrs.get(StringUtils.fromString("country")).toString(), "LK");
    }

    @Test(description = "Test defaultable user defined type as a required field")
    public void testDefaultableReqField() {
        Object returns = BRunUtil.invoke(compileResult, "testDefaultableReqField");

        BMap person = (BMap) returns;
        Assert.assertEquals(person.get(StringUtils.fromString("fname")).toString(), "default");
        Assert.assertNull(person.get(StringUtils.fromString("lname")));
        Assert.assertNull(person.get(StringUtils.fromString("age")));

        BMap adrs = (BMap) person.get(StringUtils.fromString("adrs"));
        Assert.assertEquals(adrs.get(StringUtils.fromString("street")).toString(), "");
        Assert.assertEquals(adrs.get(StringUtils.fromString("city")).toString(), "");
        Assert.assertEquals(adrs.get(StringUtils.fromString("country")).toString(), "LK");
    }

    @Test(description = "Test non-defaultable user defined type as an optional field")
    public void testOptionalNonDefField() {
        Object returns = BRunUtil.invoke(compileResult, "testOptionalNonDefField");
        BMap person = (BMap) returns;
        Assert.assertEquals(person.get(StringUtils.fromString("fname")).toString(), "default");
        Assert.assertNull(person.get(StringUtils.fromString("lname")));
        Assert.assertNull(person.get(StringUtils.fromString("age")));
        Assert.assertNull(person.get(StringUtils.fromString("adrs")));
    }

    @Test(description = "Test non-defaultable optional field access", expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*TypeCastError \\{\"message\":\"incompatible types: '\\(\\)' " +
                    "cannot be cast to 'Address3'.*")
    public void testOptionalNonDefField2() {
        BRunUtil.invoke(compileResult, "testOptionalNonDefField2");
    }

    @Test(description = "Test non-defaultable optional field access",
            expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*KeyNotFound \\{\"message\":\"cannot find key 'adrs'.*")
    public void testOptionalNonDefField3() {
        BRunUtil.invoke(compileResult, "testOptionalNonDefField3");
    }

    @Test(description = "Test non-defaultable optional field access")
    public void testOptionalNonDefField4() {
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testOptionalNonDefField4");
        Assert.assertEquals(returns.get(0).toString(), "{\"street\":\"Palm Grove\",\"city\":\"Colombo 3\"," +
                "\"country\":\"LK\"}");
        Assert.assertEquals(getType(returns.get(0)).getTag(), TypeTags.RECORD_TYPE_TAG);
        Assert.assertEquals(getType(returns.get(0)).getName(), "Address3");
        Assert.assertEquals(returns.get(1).toString(), "{\"street\":\"Palm Grove\",\"city\":\"Colombo 3\"," +
                "\"country\":\"LK\"}");
        Assert.assertEquals(getType(returns.get(1)).getTag(), TypeTags.RECORD_TYPE_TAG);
        Assert.assertEquals(getType(returns.get(1)).getName(), "Address3");
    }

    @Test(description = "Test defaultable user defined type as an optional field")
    public void testOptionalDefaultableField() {
        Object returns = BRunUtil.invoke(compileResult, "testOptionalDefaultableField");

        BMap person = (BMap) returns;
        Assert.assertEquals(person.get(StringUtils.fromString("fname")).toString(), "default");
        Assert.assertNull(person.get(StringUtils.fromString("lname")));
        Assert.assertNull(person.get(StringUtils.fromString("age")));
        Assert.assertNull(person.get(StringUtils.fromString("adrs")));
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}

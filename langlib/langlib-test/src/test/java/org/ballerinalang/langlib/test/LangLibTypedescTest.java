/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.test;


import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for the lang.typedesc library.
 *
 * @since 1.0
 */
public class LangLibTypedescTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/typedesclib_test.bal");
    }

    @Test
    public void jsonConstructFromRecTest() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testRecToJson");
        Assert.assertEquals(returns[0].getType().toString(), "map<json>");
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"N\", \"age\":3}");
    }

    @Test
    public void jsonConstructFromRecTest2() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testOnTypeName");
        BValueArray array = (BValueArray) returns[0];
        Assert.assertEquals(array.getRefValue(0).getType().toString(), "Person");
        Assert.assertEquals(array.getRefValue(0).stringValue(), "{name:\"tom\", age:2}");
        Assert.assertEquals(array.getRefValue(1).getType().toString(), "map<json>");
        Assert.assertEquals(array.getRefValue(1).stringValue(), "{\"name\":\"bob\", \"age\":4}");
    }

    @Test
    public void testOptionalFieldToMandotoryField() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testOptionalFieldToMandotoryField");
        Assert.assertEquals(returns[0].stringValue(),
                "{ballerina/lang.typedesc}ConversionError {message:\"'CRec' value cannot be converted to 'BRec'\"}");
    }

    @Test
    public void testAmbiguousTargetType() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testAmbiguousTargetType");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testConvertingToUnionConstrainedType() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testConvertingToUnionConstrainedType");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testConstructFromForNil() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testConstructFromForNilPositive");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());

        returns = BRunUtil.invokeFunction(compileResult, "testConstructFromForNilNegative");
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test(dataProvider = "constructFromWithNumericConversionFunctions")
    public void testConstructFromWithNumericConversion(String function) {
        BValue[] returns = BRunUtil.invoke(compileResult, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @DataProvider(name = "constructFromWithNumericConversionFunctions")
    public Object[][] mergeJsonFunctions() {
        return new Object[][] {
            { "testConstructFromWithNumericConversion1" },
            { "testConstructFromWithNumericConversion2" },
            { "testConstructFromWithNumericConversion3" },
            { "testConstructFromWithNumericConversion4" },
            { "testConstructFromWithNumericConversion5" },
            { "testConstructFromWithNumericConversion6" },
            { "testConstructFromWithNumericConversion7" },
            { "testConstructFromSuccessWithMoreThanOneNumericTarget" },
            { "testConstructFromFailureWithAmbiguousNumericConversionTarget" },
            { "testSettingRecordDefaultValuesOnConversion" }
        };
    }
}

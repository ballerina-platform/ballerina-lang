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
 *
 */
package org.ballerinalang.test.functions;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Function Arguments with Default Expressions.
 *
 * @since 0.995.0
 */
public class FunctionsWithDefaultableArguments {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/functions/functions_with_default_parameters.bal");
    }

    @Test(description = "Test functions arguments with function calls as default value")
    public void testFunctionCallAsDefaultExpr() {
        BValue[] returns = BRunUtil.invoke(result, "testFunctionCallAsDefaultExpr");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertTrue(returns[1] instanceof BValueArray);
        Assert.assertTrue(returns[2] instanceof BValueArray);

        BValueArray bValueArray = (BValueArray) returns[0];
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(0)).intValue(), 100);
        Assert.assertEquals(bValueArray.getRefValue(1).stringValue(), "default");
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(2)).floatValue(), 1.1);
        Assert.assertTrue(((BBoolean) bValueArray.getRefValue(3)).booleanValue());

        bValueArray = (BValueArray) returns[1];
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(0)).intValue(), 200);
        Assert.assertEquals(bValueArray.getRefValue(1).stringValue(), "given");
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(2)).floatValue(), 4.4);
        Assert.assertFalse(((BBoolean) bValueArray.getRefValue(3)).booleanValue());

        bValueArray = (BValueArray) returns[2];
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(0)).intValue(), 500);
        Assert.assertEquals(bValueArray.getRefValue(1).stringValue(), "default");
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(2)).floatValue(), 5.5);
        Assert.assertTrue(((BBoolean) bValueArray.getRefValue(3)).booleanValue());
    }

    @Test(description = "Test functions arguments with record literal as default value")
    public void testRecordAsDefaultExpr() {
        BValue[] returns = BRunUtil.invoke(result, "testRecordAsDefaultExpr");
        Assert.assertTrue(returns[0] instanceof BMap);
        Assert.assertTrue(returns[1] instanceof BMap);
        Assert.assertTrue(returns[2] instanceof BMap);
        Assert.assertTrue(returns[3] instanceof BMap);

        BMap bMap = (BMap) returns[0];
        Assert.assertEquals(bMap.get("a").stringValue(), "default");
        Assert.assertEquals(((BInteger) bMap.get("b")).intValue(), 50);
        Assert.assertFalse(((BBoolean) bMap.get("c")).booleanValue());
        Assert.assertEquals(((BFloat) bMap.get("d")).floatValue(), 11.1);

        bMap = (BMap) returns[1];
        Assert.assertEquals(bMap.get("a").stringValue(), "f2");
        Assert.assertEquals(((BInteger) bMap.get("b")).intValue(), 200);
        Assert.assertFalse(((BBoolean) bMap.get("c")).booleanValue());
        Assert.assertEquals(((BFloat) bMap.get("d")).floatValue(), 44.4);

        bMap = (BMap) returns[2];
        Assert.assertEquals(bMap.get("a").stringValue(), "f1");
        Assert.assertEquals(((BInteger) bMap.get("b")).intValue(), 100);
        Assert.assertTrue(((BBoolean) bMap.get("c")).booleanValue());
        Assert.assertEquals(((BFloat) bMap.get("d")).floatValue(), 22.2);

        bMap = (BMap) returns[3];
        Assert.assertEquals(bMap.get("a").stringValue(), "default2");
        Assert.assertEquals(((BInteger) bMap.get("b")).intValue(), 150);
        Assert.assertTrue(((BBoolean) bMap.get("c")).booleanValue());
        Assert.assertEquals(((BFloat) bMap.get("d")).floatValue(), 33.3);
    }

    @Test(description = "Test function pointer arguments")
    public void testDefaultExprInFunctionPointers() {
        BValue[] returns = BRunUtil.invoke(result, "testDefaultExprInFunctionPointers");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 200);
        Assert.assertEquals(returns[1].stringValue(), "given");
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 4.4);
        Assert.assertFalse(((BBoolean) returns[3]).booleanValue());
    }

    @Test(description = "Test functions arguments with combined expressions for default values")
    public void testCombinedExprAsDefaultValue() {
        BValue[] returns = BRunUtil.invoke(result, "testCombinedExprAsDefaultValue");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertTrue(returns[1] instanceof BValueArray);
        Assert.assertTrue(returns[2] instanceof BValueArray);

        BValueArray bValueArray = (BValueArray) returns[0];
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(0)).intValue(), 205);
        Assert.assertEquals(bValueArray.getRefValue(1).stringValue(), "defdefault");
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(2)).floatValue(), 101.1);

        bValueArray = (BValueArray) returns[1];
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(0)).intValue(), 50);
        Assert.assertEquals(bValueArray.getRefValue(1).stringValue(), "defdefault");
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(2)).floatValue(), 101.1);

        bValueArray = (BValueArray) returns[2];
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(0)).intValue(), 205);
        Assert.assertEquals(bValueArray.getRefValue(1).stringValue(), "givendefault");
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(2)).floatValue(), 9.9);
    }

    @Test(description = "Test functions arguments with error as default values")
    public void testDefaultValueWithError() {
        BValue[] returns = BRunUtil.invoke(result, "testDefaultValueWithError");
        Assert.assertTrue(returns[0] instanceof BError);
        Assert.assertTrue(returns[1] instanceof BError);
        Assert.assertTrue(returns[2] instanceof BError);

        BError bError = (BError) returns[0];
        Assert.assertEquals(bError.getReason(), "Generated Error");

        bError = (BError) returns[1];
        Assert.assertEquals(bError.getReason(), "Not Error 100");

        bError = (BError) returns[2];
        Assert.assertEquals(bError.getReason(), "Not Error 2");
    }

    @Test(description = "Test functions arguments with error as default values")
    public void testDefaultValueWithTernary() {
        BValue[] returns = BRunUtil.invoke(result, "testDefaultValueWithTernary");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertTrue(returns[1] instanceof BValueArray);
        Assert.assertTrue(returns[2] instanceof BValueArray);

        BValueArray bValueArray = (BValueArray) returns[0];
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(0)).floatValue(), 1.1);
        Assert.assertEquals(bValueArray.getRefValue(1).stringValue(), "string");

        bValueArray = (BValueArray) returns[1];
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(0)).floatValue(), 2.2);
        Assert.assertEquals(bValueArray.getRefValue(1).stringValue(), "empty");

        bValueArray = (BValueArray) returns[2];
        Assert.assertEquals(((BFloat) bValueArray.getRefValue(0)).floatValue(), 3.3);
        Assert.assertEquals(bValueArray.getRefValue(1).stringValue(), "given");
    }

    @Test(description = "Test functions arguments default value panicing",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: Panic!.*")
    public void testPanicWithinDefaultExpr() {
        BValue[] returns = BRunUtil.invoke(result, "testPanicWithinDefaultExpr");
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 0);
    }

    @Test(description = "Test functions arguments object type as default value")
    public void testDefaultObject() {
        BValue[] returns = BRunUtil.invoke(result, "testDefaultObject");
        Assert.assertTrue(returns[0] instanceof BValueArray);
        Assert.assertTrue(returns[1] instanceof BValueArray);

        BValueArray bValueArray = (BValueArray) returns[0];
        Assert.assertEquals(bValueArray.getRefValue(0).stringValue(), "default");
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(1)).intValue(), 100);

        bValueArray = (BValueArray) returns[1];
        Assert.assertEquals(bValueArray.getRefValue(0).stringValue(), "given");
        Assert.assertEquals(((BInteger) bValueArray.getRefValue(1)).intValue(), 200);
    }

    @Test
    public void testFuncWithAsyncDefaultParamExpression() {
        BValue[] returns = BRunUtil.invoke(result, "testFuncWithAsyncDefaultParamExpression");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "hellohelloworldhellosamplevalue");
    }

    @Test
    public void testUsingParamValues() {
        BValue[] returns = BRunUtil.invoke(result, "testUsingParamValues");
        Assert.assertTrue(returns[0] instanceof BString);

        Assert.assertEquals(returns[0].stringValue(), "hellohelloasyncworldworldasyncsamplevalue");
    }

    @Test
    public void testAttachedAsyncDefaultParam() {
        BValue[] returns = BRunUtil.invoke(result, "testAttachedAsyncDefaultParam");
        Assert.assertTrue(returns[0] instanceof BString);

        Assert.assertEquals(returns[0].stringValue(), "hellohelloworldhellosamplevalue");
    }

    @Test
    public void testUsingParamValuesInAttachedFunc() {
        BValue[] returns = BRunUtil.invoke(result, "testUsingParamValuesInAttachedFunc");
        Assert.assertTrue(returns[0] instanceof BString);

        Assert.assertEquals(returns[0].stringValue(), "hellohelloasyncworldworldasyncsamplevalue");
    }
}

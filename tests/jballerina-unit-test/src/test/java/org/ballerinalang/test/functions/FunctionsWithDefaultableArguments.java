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

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
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

    @Test
    public void testFunctionCallInImportedModuleWithDefaultArguments() {
        CompileResult compileResult = BCompileUtil.compile("test-src/functions/testPackage");
        BRunUtil.runMain(compileResult);
    }

    @Test(description = "Test functions arguments with function calls as default value")
    public void testFunctionCallAsDefaultExpr() {
        Object arr = BRunUtil.invoke(result, "testFunctionCallAsDefaultExpr");
        BArray returns = (BArray) arr;
        Assert.assertTrue(returns.get(0) instanceof  BArray);
        Assert.assertTrue(returns.get(1) instanceof  BArray);
        Assert.assertTrue(returns.get(2) instanceof  BArray);

        BArray bValueArray = (BArray) returns.get(0);
        Assert.assertEquals(bValueArray.getRefValue(0), 100L);
        Assert.assertEquals(bValueArray.getRefValue(1).toString(), "default");
        Assert.assertEquals(bValueArray.getRefValue(2), 1.1);
        Assert.assertTrue((Boolean) bValueArray.getRefValue(3));

        bValueArray = (BArray) returns.get(1);
        Assert.assertEquals(bValueArray.getRefValue(0), 200L);
        Assert.assertEquals(bValueArray.getRefValue(1).toString(), "given");
        Assert.assertEquals(bValueArray.getRefValue(2), 4.4);
        Assert.assertFalse((Boolean) bValueArray.getRefValue(3));

        bValueArray = (BArray) returns.get(2);
        Assert.assertEquals(bValueArray.getRefValue(0), 500L);
        Assert.assertEquals(bValueArray.getRefValue(1).toString(), "default");
        Assert.assertEquals(bValueArray.getRefValue(2), 5.5);
        Assert.assertTrue((Boolean) bValueArray.getRefValue(3));
    }

    @Test(description = "Test functions arguments with record literal as default value")
    public void testRecordAsDefaultExpr() {
        Object arr = BRunUtil.invoke(result, "testRecordAsDefaultExpr");
        BArray returns = (BArray) arr;
        Assert.assertTrue(returns.get(0) instanceof BMap);
        Assert.assertTrue(returns.get(1) instanceof BMap);
        Assert.assertTrue(returns.get(2) instanceof BMap);
        Assert.assertTrue(returns.get(3) instanceof BMap);

        BMap bMap = (BMap) returns.get(0);
        Assert.assertEquals(bMap.get(StringUtils.fromString("a")).toString(), "default");
        Assert.assertEquals(bMap.get(StringUtils.fromString("b")), 50L);
        Assert.assertFalse((Boolean) bMap.get(StringUtils.fromString("c")));
        Assert.assertEquals(bMap.get(StringUtils.fromString("d")), 11.1);

        bMap = (BMap) returns.get(1);
        Assert.assertEquals(bMap.get(StringUtils.fromString("a")).toString(), "f2");
        Assert.assertEquals(bMap.get(StringUtils.fromString("b")), 200L);
        Assert.assertFalse((Boolean) bMap.get(StringUtils.fromString("c")));
        Assert.assertEquals(bMap.get(StringUtils.fromString("d")), 44.4);

        bMap = (BMap) returns.get(2);
        Assert.assertEquals(bMap.get(StringUtils.fromString("a")).toString(), "f1");
        Assert.assertEquals(bMap.get(StringUtils.fromString("b")), 100L);
        Assert.assertTrue((Boolean) bMap.get(StringUtils.fromString("c")));
        Assert.assertEquals(bMap.get(StringUtils.fromString("d")), 22.2);

        bMap = (BMap) returns.get(3);
        Assert.assertEquals(bMap.get(StringUtils.fromString("a")).toString(), "default2");
        Assert.assertEquals(bMap.get(StringUtils.fromString("b")), 150L);
        Assert.assertTrue((Boolean) bMap.get(StringUtils.fromString("c")));
        Assert.assertEquals(bMap.get(StringUtils.fromString("d")), 33.3);
    }

    @Test(description = "Test function pointer arguments")
    public void testDefaultExprInFunctionPointers() {
        Object arr = BRunUtil.invoke(result, "testDefaultExprInFunctionPointers");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.get(0), 200L);
        Assert.assertEquals(returns.get(1).toString(), "given");
        Assert.assertEquals(returns.get(2), 4.4);
        Assert.assertFalse((Boolean) returns.get(3));
    }

    @Test(description = "Test functions arguments with combined expressions for default values")
    public void testCombinedExprAsDefaultValue() {
        Object arr = BRunUtil.invoke(result, "testCombinedExprAsDefaultValue");
        BArray returns = (BArray) arr;
        Assert.assertTrue(returns.get(0) instanceof  BArray);
        Assert.assertTrue(returns.get(1) instanceof  BArray);
        Assert.assertTrue(returns.get(2) instanceof  BArray);

        BArray bValueArray = (BArray) returns.get(0);
        Assert.assertEquals(bValueArray.getRefValue(0), 205L);
        Assert.assertEquals(bValueArray.getRefValue(1).toString(), "defdefault");
        Assert.assertEquals(bValueArray.getRefValue(2), 101.1);

        bValueArray = (BArray) returns.get(1);
        Assert.assertEquals(bValueArray.getRefValue(0), 50L);
        Assert.assertEquals(bValueArray.getRefValue(1).toString(), "defdefault");
        Assert.assertEquals(bValueArray.getRefValue(2), 101.1);

        bValueArray = (BArray) returns.get(2);
        Assert.assertEquals(bValueArray.getRefValue(0), 205L);
        Assert.assertEquals(bValueArray.getRefValue(1).toString(), "givendefault");
        Assert.assertEquals(bValueArray.getRefValue(2), 9.9);
    }

    @Test(description = "Test functions arguments with error as default values")
    public void testDefaultValueWithError() {
        BRunUtil.invoke(result, "testDefaultValueWithError");
    }

    @Test(description = "Test functions arguments with error as default values")
    public void testDefaultValueWithTernary() {
        Object arr = BRunUtil.invoke(result, "testDefaultValueWithTernary");
        BArray returns = (BArray) arr;
        Assert.assertTrue(returns.get(0) instanceof  BArray);
        Assert.assertTrue(returns.get(1) instanceof  BArray);
        Assert.assertTrue(returns.get(2) instanceof  BArray);

        BArray bValueArray = (BArray) returns.get(0);
        Assert.assertEquals(bValueArray.getRefValue(0), 1.1);
        Assert.assertEquals(bValueArray.getRefValue(1).toString(), "string");

        bValueArray = (BArray) returns.get(1);
        Assert.assertEquals(bValueArray.getRefValue(0), 2.2);
        Assert.assertEquals(bValueArray.getRefValue(1).toString(), "empty");

        bValueArray = (BArray) returns.get(2);
        Assert.assertEquals(bValueArray.getRefValue(0), 3.3);
        Assert.assertEquals(bValueArray.getRefValue(1).toString(), "given");
    }

    @Test(description = "Test functions arguments default value panicing",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: Panic!.*")
    public void testPanicWithinDefaultExpr() {
        Object returns = BRunUtil.invoke(result, "testPanicWithinDefaultExpr");
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 0L);
    }

    @Test(description = "Test functions arguments object type as default value")
    public void testDefaultObject() {
        Object arr = BRunUtil.invoke(result, "testDefaultObject");
        BArray returns = (BArray) arr;
        Assert.assertTrue(returns.get(0) instanceof  BArray);
        Assert.assertTrue(returns.get(1) instanceof  BArray);

        BArray bValueArray = (BArray) returns.get(0);
        Assert.assertEquals(bValueArray.getRefValue(0).toString(), "default");
        Assert.assertEquals(bValueArray.getRefValue(1), 100L);

        bValueArray = (BArray) returns.get(1);
        Assert.assertEquals(bValueArray.getRefValue(0).toString(), "given");
        Assert.assertEquals(bValueArray.getRefValue(1), 200L);
    }

    @Test(dataProvider = "functionsWithDefaultByteValues")
    public void testFuncWithDefaultByteParamValues(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider
    public  Object[] functionsWithDefaultByteValues() {
        return new String[] {
                "testFuncWithDefaultByteValue",
                "testDefaultByteValueInFunctionPointers"
        };
    }

    @Test
    public void testFuncWithAsyncDefaultParamExpression() {
        Object returns = BRunUtil.invoke(result, "testFuncWithAsyncDefaultParamExpression");
        Assert.assertTrue(returns instanceof BString);
        Assert.assertEquals(returns.toString(), "hellohelloworldhellosamplevalue");
    }

    @Test
    public void testUsingParamValues() {
        Object returns = BRunUtil.invoke(result, "testUsingParamValues");
        Assert.assertTrue(returns instanceof BString);

        Assert.assertEquals(returns.toString(), "hellohelloasyncworldworldasyncsamplevalue");
    }

    @Test
    public void testAttachedAsyncDefaultParam() {
        Object returns = BRunUtil.invoke(result, "testAttachedAsyncDefaultParam");
        Assert.assertTrue(returns instanceof BString);

        Assert.assertEquals(returns.toString(), "hellohelloworldhellosamplevalue");
    }

    @Test
    public void testResourceFunctionDefaultParam() {
        Object returns = BRunUtil.invoke(result, "testResourceFunctionDefaultParam");
        Assert.assertTrue(returns instanceof BString);

        Assert.assertEquals(returns.toString(), "foo");
    }

    @Test
    public void testUsingParamValuesInAttachedFunc() {
        Object returns = BRunUtil.invoke(result, "testUsingParamValuesInAttachedFunc");
        Assert.assertTrue(returns instanceof BString);

        Assert.assertEquals(returns.toString(), "hellohelloasyncworldworldasyncsamplevalue");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}

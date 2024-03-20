/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.closures;

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test cases for closure related scenarios in ballerina.
 */
public class ClosureTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/closures/closure.bal");
    }

    @Test(description = "Test basic closure operations")
    public void testBasicClosure() {
        Object returns = BRunUtil.invoke(compileResult, "test1");
        Assert.assertEquals(returns, 18L);
    }

    @Test(description = "Test two level closure operations")
    public void testTwoLevelClosure() {
        Object returns = BRunUtil.invoke(compileResult, "test2");
        Assert.assertEquals(returns, 36L);
    }

    @Test(description = "Test three level operations")
    public void testThreeLevelClosure() {
        Object returns = BRunUtil.invoke(compileResult, "test3");
        Assert.assertEquals(returns, 50L);
    }

    @Test(description = "Test with if block")
    public void testClosureWithIfBlock() {
        Object returns = BRunUtil.invoke(compileResult, "test4");
        Assert.assertEquals(returns, 43L);
    }

    @Test(description = "Test multi level function pointer call")
    public void testMultiLevelFPCallWithClosure() {
        Object returns = BRunUtil.invoke(compileResult, "test5");
        Assert.assertEquals(returns, 10L);
    }

    @Test(description = "Test multi function pointer with same closure call")
    public void testMultiFunctionCallWithSameClosure() {
        Object returns = BRunUtil.invoke(compileResult, "test6");
        Assert.assertEquals(returns, 316L);
    }

    @Test(description = "Test closure with different param ordering")
    public void testDifferentParamOrder() {
        Object returns = BRunUtil.invoke(compileResult, "test7");
        Assert.assertEquals(returns, 22L);
    }

    @Test(description = "Test multi level function")
    public void testMultiLevelFunction() {
        Object returns = BRunUtil.invoke(compileResult, "test8");
        Assert.assertEquals(returns, 50L);
    }

    @Test(description = "Test global var access with closure")
    public void testGlobalVarAccessWithClosure() {
        Object returns = BRunUtil.invoke(compileResult, "test9");
        Assert.assertEquals(returns, 23L);
    }

    @Test(description = "Test different type args")
    public void testDifferentTypeArgs1() {
        Object returns = BRunUtil.invoke(compileResult, "test10");
        Assert.assertEquals(returns, 20L);
    }

    @Test(description = "Test different type args 2")
    public void testDifferentTypeArgs2() {
        Object returns = BRunUtil.invoke(compileResult, "test11");
        Assert.assertEquals(returns, 10L);
    }

    @Test(description = "Test different type args 3")
    public void testDifferentTypeArgs3() {
        Object returns = BRunUtil.invoke(compileResult, "test12");
        Assert.assertEquals(returns, 10L);
    }

    @Test(description = "Test different type args 4")
    public void testDifferentTypeArgs4() {
        Object returns = BRunUtil.invoke(compileResult, "test13");
        Assert.assertEquals(returns, 9L);
    }

    @Test(description = "Test string args with a closure")
    public void testStringArgsWithClosure() {
        Object returns = BRunUtil.invoke(compileResult, "test14");
        Assert.assertEquals((returns).toString(), "HelloBallerinaWorld!!!");
    }

    @Test(description = "Test tuple type as argument")
    public void testTupleArgsWithClosure() {
        Object returns = BRunUtil.invoke(compileResult, "test16");
        Assert.assertEquals((returns).toString(), "ImBallerina15.0Program !!!Hello11.1World !!!");
    }

    @Test(description = "Test tuple type as argument with an order")
    public void testTupleTypesOrderWithClosure() {
        Object returns = BRunUtil.invoke(compileResult, "test17");
        Assert.assertEquals((returns).toString(),
                "I'mHello11.1World !!!Ballerina15.0Program!!!HelloInner44.8World Inner!!!");
    }

    @Test(description = "Test global var modify and access")
    public void testGlobalVarModifyAndAccess() {
        Object returns = BRunUtil.invoke(compileResult, "test18");
        Assert.assertEquals(returns, 16L);
    }

    @Test(description = "Test closure with object attached function references")
    public void testClosureWithObjectAttachedFuncReferences() {
        Object returns = BRunUtil.invoke(compileResult, "test19");
        Assert.assertEquals((returns).toString(), "Hello Ballerina7.4K43");
    }

    @Test(description = "Test closure with object attached function pointer references")
    public void testClosureWithObjectAttachedFuncPointerReferences() {
        Object returns = BRunUtil.invoke(compileResult, "test20");
        Assert.assertEquals((returns).toString(), "7.354Ballerina !!!");
    }

    @Test(description = "Test closure with different type args references")
    public void testClosureWithDifferentArgsReferences() {
        Object returns = BRunUtil.invoke(compileResult, "test22");
        Assert.assertEquals((returns).toString(), "7InnerInt41.2InnerFloat4.5Ballerina !!!");
    }

    @Test(description = "Test closure with variable shadowing")
    public void testClosureWithVariableShadowing1() {
        Object returns = BRunUtil.invoke(compileResult, "test23");
        Assert.assertEquals((returns).toString(), "Ballerina31");
    }

    @Test(description = "Test two level closure with variable shadowing")
    public void testClosureWithVariableShadowing2() {
        Object returns = BRunUtil.invoke(compileResult, "test24");
        Assert.assertEquals((returns).toString(), "Out31In65Ballerina!!!");
    }

    @Test(description = "Test three level closure with variable shadowing")
    public void testClosureWithVariableShadowing3() {
        Object returns = BRunUtil.invoke(compileResult, "test25");
        Assert.assertEquals((returns).toString(), "OutMost31Out46In80Ballerina!!!");
    }

    @Test(description = "Test three level closure with variable shadowing another test case")
    public void testClosureWithVariableShadowing4() {
        Object returns = BRunUtil.invoke(compileResult, "test26");
        Assert.assertEquals((returns).toString(), "OutMost47Out47In36Ballerina!!!");
    }

    @Test(description = "Test iterable operations with lambda. This will verify whether local referred vars are " +
            "modified within closure")
    public void testIterableOperationsVarModification() {
        Object returns = BRunUtil.invoke(compileResult, "testLocalVarModifyWithinClosureScope");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns, 9.9);
    }

    @Test(description = "Test byte and boolean")
    public void testByteAndBoolean() {
        Object result = BRunUtil.invoke(compileResult, "test27");
        BArray returns = (BArray) result;
        Assert.assertEquals(returns.size(), 3);
        Assert.assertTrue(returns.get(0) instanceof BArray);
        Assert.assertTrue(returns.get(1) instanceof BArray);
        Assert.assertTrue(returns.get(2) instanceof BArray);
        BArray blob1 = (BArray) returns.get(0);
        BArray blob2 = (BArray) returns.get(1);
        BArray blob3 = (BArray) returns.get(2);
        assertJBytesWithBBytes(new byte[]{13, 7, 4, 3}, blob1);
        assertJBytesWithBBytes(new byte[]{3, 13, 5, 7, 3}, blob2);
        assertJBytesWithBBytes(new byte[]{1, 2, 3, 13, 7}, blob3);
    }

    private void assertJBytesWithBBytes(byte[] jBytes, BArray bBytes) {
        for (int i = 0; i < jBytes.length; i++) {
            Assert.assertEquals(bBytes.getByte(i), jBytes[i], "Invalid byte value returned.");
        }
    }

    @Test(description = "Test multi level block statements with closure test case")
    public void testMultiLevelBlockStatements() {
        Object result = BRunUtil.invoke(compileResult, "test28");
        BArray returns = (BArray) result;
        Assert.assertEquals(returns.get(0), 58L);
        Assert.assertEquals(returns.get(1), 167L);
    }


    @Test(description = "Test any and var access with closure")
    public void testAnyVarWithClosure() {
        Object result = BRunUtil.invoke(compileResult, "test29");
        BArray returns = (BArray) result;
        Assert.assertTrue((Boolean) returns.get(0));
        Assert.assertFalse((Boolean) returns.get(1));
        Assert.assertTrue((Boolean) returns.get(2));
        Assert.assertFalse((Boolean) returns.get(3));
        Assert.assertTrue((Boolean) returns.get(4));
        Assert.assertFalse((Boolean) returns.get(5));
        Assert.assertTrue((Boolean) returns.get(6));
        Assert.assertFalse((Boolean) returns.get(7));
    }

    @Test(description = "Test closure capture of variable not initialized at declaration")
    public void testClosureCaptureLaterInitializedVar() {
        Object returns = BRunUtil.invoke(compileResult, "laterInitCapture");
        Assert.assertEquals(returns.toString(), "aa");
    }

    @Test(description = "Test closure capture of rest params")
    public void testRestParamsAsClosureVars() {
        Object returns = BRunUtil.invoke(compileResult, "testRestParamsAsClosureVars");
        Assert.assertEquals(returns.toString(), "Hello, From, Ballerina");
    }

    @Test(description = "Test closure capture of rest params")
    public void testRestParamsAsClosureVars2() {
        Object returns = BRunUtil.invoke(compileResult, "testRestParamsAsClosureVars2");
        Assert.assertEquals(returns, 60L);
    }

    @Test(description = "Test closure within resource function")
    public void testClosureWithinResource() {
        CompileResult result = BCompileUtil.compile("test-src/closures/closures_in_resource.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
        BRunUtil.invoke(result, "testClosureWithinResource");
    }

    @Test(description = "Test error constructor with closure")
    public void errorConstructorWithClosureTest() {
        BRunUtil.invoke(compileResult, "errorConstructorWithClosureTest");
    }


    @Test(description = "Test closure levels with forEach")
    public void forEachWithClosure() {
        BRunUtil.invoke(compileResult, "test30");
    }

    @Test(description = "Test closure with binding type param", dataProvider = "closureWithBindingPatternTypeParam")
    public void testClosureWithBindingPatternTypeParam(String functionName) {
        BRunUtil.invoke(compileResult, functionName);
    }

    @DataProvider(name = "closureWithBindingPatternTypeParam")
    private Object[] closureWithBindingPatternTypeParam() {
        return new String[]{
                "testClosureWithStructuredBindingTypeParams",
                "testClosureWithTupleBindingTypeParams",
                "testClosureWithBindingPatternDefaultValues",
                "testClosureWithErrorBindingPatterns",
                "testClosureWithBindingPatternsInForEach"
        };
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}

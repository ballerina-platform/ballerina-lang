/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.balo.globalvar;

import org.ballerinalang.core.model.values.BByte;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.balo.BaloCreator;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.test.utils.ByteArrayUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Global variable functions in BALO test.
 * 
 * @since 0.975.0
 */
public class GlobalVarFunctionInBaloTest {

    CompileResult result;

    @BeforeClass
    public void setup() throws IOException {
        BaloCreator.cleanCacheDirectories();
        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_project", "testorg", "foo");
        result = BCompileUtil.compile("test-src/balo/test_balo/globalvar/test_global_var_function.bal");
    }

    @Test(description = "Test Defining global variables")
    public void testDefiningGlobalVar() {
        BValue[] args = new BValue[0];
        BValue[] returns = BRunUtil.invoke(result, "getGlobalVars", args);
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BString.class);
        Assert.assertSame(returns[2].getClass(), BFloat.class);
        Assert.assertSame(returns[3].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 800);
        Assert.assertEquals(returns[1].stringValue(), "value");
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), 99.34323);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 88343);
    }

    @Test(description = "Test access global variable within function")
    public void testAccessGlobalVarWithinFunctions() {
        BValue[] returns = BRunUtil.invoke(result, "accessGlobalVar");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 89143);
    }

    @Test(description = "Test change global var within functions")
    public void testChangeGlobalVarWithinFunction() {
        BValue[] args = {new BInteger(88)};
        BValue[] returns = BRunUtil.invoke(result, "changeGlobalVar", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 165.0);


        CompileResult resultGlobalVar = BCompileUtil
                .compile("test-src/statements/variabledef/global-var-function.bal");

        BValue[] returnsChanged = BRunUtil.invoke(resultGlobalVar, "getGlobalFloatVar");

        Assert.assertEquals(returnsChanged.length, 1);
        Assert.assertSame(returnsChanged[0].getClass(), BFloat.class);

        Assert.assertEquals(((BFloat) returnsChanged[0]).floatValue(), 80.0);
    }

    @Test(description = "Test assigning global variable to another global variable")
    public void testAssignGlobalVarToAnotherGlobalVar() {
        BValue[] returns = BRunUtil.invoke(result, "getGlobalVarFloat1");

        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BFloat.class);

        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 99.34323);
    }

    @Test(description = "Test assigning global var within a function")
    public void testInitializingGlobalVarWithinFunction() {
        BValue[] returns = BRunUtil.invoke(result, "initializeGlobalVarSeparately");

        Assert.assertEquals(returns.length, 2);
        Assert.assertSame(returns[0].getClass(), BMap.class);
        Assert.assertSame(returns[1].getClass(), BFloat.class);

        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"James\", \"age\":30}");
        Assert.assertEquals(((BFloat) returns[1]).floatValue(), 3432.3423);
    }

    @Test(description = "Test global variable byte")
    public void testGlobalVarByte() {
        BValue[] returns = BRunUtil.invoke(result, "getGlobalVarByte");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BByte.class);
        Assert.assertEquals(((BByte) returns[0]).byteValue(), 234);
    }

    @Test(description = "Test global variable byte array1")
    public void testGlobalVarByteArray1() {
        byte[] bytes1 = new byte[]{2, 3, 4, 67, 89};
        BValue[] returns = BRunUtil.invoke(result, "getGlobalVarByteArray1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BValueArray.class);
        BValueArray blob1 = (BValueArray) returns[0];
        ByteArrayUtils.assertJBytesWithBBytes(bytes1, blob1);
    }

    @Test(description = "Test global variable byte array2")
    public void testGlobalVarByteArray2() {
        String b1 = "afcd34abcdef+dfginermkmf123w/bc234cd/1a4bdfaaFGTdaKMN8923as=";
        byte[] bytes1 = ByteArrayUtils.decodeBase64(b1);
        BValue[] returns = BRunUtil.invoke(result, "getGlobalVarByteArray2");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BValueArray.class);
        BValueArray blob1 = (BValueArray) returns[0];
        ByteArrayUtils.assertJBytesWithBBytes(bytes1, blob1);
    }

    @Test(description = "Test global variable byte array3")
    public void testGlobalVarByteArray3() {
        String b1 = "afcd34abcdef123abc234bcd1a4bdfaaabadabcd892312df";
        byte[] bytes1 = ByteArrayUtils.hexStringToByteArray(b1);
        BValue[] returns = BRunUtil.invoke(result, "getGlobalVarByteArray3");
        Assert.assertEquals(returns.length, 1);
        Assert.assertSame(returns[0].getClass(), BValueArray.class);
        BValueArray blob1 = (BValueArray) returns[0];
        ByteArrayUtils.assertJBytesWithBBytes(bytes1, blob1);
    }

    @Test(description = "Test access global arrays within functions")
    public void testGlobalArraysWithinFunction() {
        BValue[] returns = BRunUtil.invoke(result, "getGlobalArrays");

        Assert.assertEquals(returns.length, 7);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 2);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 3);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 4);
        Assert.assertEquals(((BInteger) returns[3]).intValue(), 2);
        Assert.assertEquals(((BInteger) returns[4]).intValue(), 3);
        Assert.assertEquals(((BInteger) returns[5]).intValue(), 3);
        Assert.assertEquals(((BInteger) returns[6]).intValue(), 2);
    }

    @AfterClass
    public void tearDown() {
        BaloCreator.clearPackageFromRepository("test-src/balo/test_projects/test_project", "testorg", "foo");
    }
}

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

package org.ballerinalang.test.bala.globalvar;

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.utils.ByteArrayUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Global variable functions in BALA test.
 * 
 * @since 0.975.0
 */
public class GlobalVarFunctionInBalaTest {

    CompileResult compileResult;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
        compileResult = BCompileUtil.compile("test-src/bala/test_bala/globalvar/test_global_var_function.bal");
    }

    @Test(description = "Test Defining global variables")
    public void testDefiningGlobalVar() {
        Object[] args = new Object[0];
        Object result = BRunUtil.invoke(compileResult, "getGlobalVars", args);
        BArray returns = (BArray) result;
        Assert.assertEquals(returns.size(), 4);
        Assert.assertTrue(returns.get(0) instanceof Long);
        Assert.assertTrue(returns.get(1) instanceof BString);
        Assert.assertTrue(returns.get(2) instanceof Double);
        Assert.assertTrue(returns.get(3) instanceof Long);
        Assert.assertEquals(returns.get(0), 800L);
        Assert.assertEquals(returns.get(1).toString(), "value");
        Assert.assertEquals(returns.get(2), 99.34323);
        Assert.assertEquals(returns.get(3), 88343L);
    }

    @Test(description = "Test access global variable within function")
    public void testAccessGlobalVarWithinFunctions() {
        Object returns = BRunUtil.invoke(compileResult, "accessGlobalVar");
        Assert.assertTrue(returns instanceof Long);
        Assert.assertEquals(returns, 89143L);
    }

    @Test(description = "Test change global var within functions")
    public void testChangeGlobalVarWithinFunction() {
        Object[] args = {(88)};
        Object returns = BRunUtil.invoke(compileResult, "changeGlobalVar", args);

        Assert.assertTrue(returns instanceof Double);

        Assert.assertEquals(returns, 165.0);


        CompileResult resultGlobalVar = BCompileUtil
                .compile("test-src/statements/variabledef/global-var-function.bal");

        Object returnsChanged = BRunUtil.invoke(resultGlobalVar, "getGlobalFloatVar");

        Assert.assertTrue(returnsChanged instanceof Double);

        Assert.assertEquals(returnsChanged, 80.0);
    }

    @Test(description = "Test assigning global variable to another global variable")
    public void testAssignGlobalVarToAnotherGlobalVar() {
        Object returns = BRunUtil.invoke(compileResult, "getGlobalVarFloat1");

        Assert.assertTrue(returns instanceof Double);

        Assert.assertEquals(returns, 99.34323);
    }

    @Test(description = "Test assigning global var within a function")
    public void testInitializingGlobalVarWithinFunction() {
        Object result = BRunUtil.invoke(compileResult, "initializeGlobalVarSeparately");
        BArray returns = (BArray) result;

        Assert.assertEquals(returns.size(), 2);
        Assert.assertTrue(returns.get(0) instanceof BMap);
        Assert.assertTrue(returns.get(1) instanceof Double);

        Assert.assertEquals(returns.get(0).toString(), "{\"name\":\"James\",\"age\":30}");
        Assert.assertEquals(returns.get(1), 3432.3423);
    }

    @Test(description = "Test global variable byte")
    public void testGlobalVarByte() {
        Object returns = BRunUtil.invoke(compileResult, "getGlobalVarByte");
        Assert.assertTrue(returns instanceof Integer);
        Assert.assertEquals(returns, 234);
    }

    @Test(description = "Test global variable byte array1")
    public void testGlobalVarByteArray1() {
        byte[] bytes1 = new byte[]{2, 3, 4, 67, 89};
        Object returns = BRunUtil.invoke(compileResult, "getGlobalVarByteArray1");
        Assert.assertTrue(returns instanceof BArray);
        BArray blob1 = (BArray) returns;
        ByteArrayUtils.assertJBytesWithBBytes(bytes1, blob1.getBytes());
    }

    @Test(description = "Test global variable byte array2")
    public void testGlobalVarByteArray2() {
        String b1 = "afcd34abcdef+dfginermkmf123w/bc234cd/1a4bdfaaFGTdaKMN8923as=";
        byte[] bytes1 = ByteArrayUtils.decodeBase64(b1);
        Object returns = BRunUtil.invoke(compileResult, "getGlobalVarByteArray2");
        Assert.assertTrue(returns instanceof BArray);
        BArray blob1 = (BArray) returns;
        ByteArrayUtils.assertJBytesWithBBytes(bytes1, blob1.getBytes());
    }

    @Test(description = "Test global variable byte array3")
    public void testGlobalVarByteArray3() {
        String b1 = "afcd34abcdef123abc234bcd1a4bdfaaabadabcd892312df";
        byte[] bytes1 = ByteArrayUtils.hexStringToByteArray(b1);
        Object returns = BRunUtil.invoke(compileResult, "getGlobalVarByteArray3");
        Assert.assertTrue(returns instanceof BArray);
        BArray blob1 = (BArray) returns;
        ByteArrayUtils.assertJBytesWithBBytes(bytes1, blob1.getBytes());
    }

    @Test(description = "Test access global arrays within functions")
    public void testGlobalArraysWithinFunction() {
        Object result = BRunUtil.invoke(compileResult, "getGlobalArrays");
        BArray returns = (BArray) result;

        Assert.assertEquals(returns.size(), 7);
        Assert.assertEquals(returns.get(0), 2L);
        Assert.assertEquals(returns.get(1), 3L);
        Assert.assertEquals(returns.get(2), 4L);
        Assert.assertEquals(returns.get(3), 2L);
        Assert.assertEquals(returns.get(4), 3L);
        Assert.assertEquals(returns.get(5), 3L);
        Assert.assertEquals(returns.get(6), 2L);
    }
}

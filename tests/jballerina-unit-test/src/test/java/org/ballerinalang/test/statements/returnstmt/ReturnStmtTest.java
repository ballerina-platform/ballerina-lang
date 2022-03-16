/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.statements.returnstmt;

import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for return statement.
 */
public class ReturnStmtTest {

    private static final double DELTA = 0.01;
    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/statements/returnstmt/return-stmt-positive.bal");
    }

    @Test(description = "Test void return")
    public void testReturn() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(compileResult, "testReturn", args);
        Assert.assertNull(returns);
    }

    @Test(description = "Test void return")
    public void testReturnOneVarDcl() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(compileResult, "testReturnOneVarDcl", args);
        Assert.assertNull(returns);
    }

    @Test(description = "Test one return value")
    public void testReturnOneReturnArg() {
        Object[] args = {};
        Object returns = BRunUtil.invoke(compileResult, "testReturnOneReturnArg", args);

        Assert.assertEquals(5L, returns);
    }

    @Test(description = "Test one parameter but void return")
    public void testReturnOneParam() {
        Object[] args = {10};
        Object returns = BRunUtil.invoke(compileResult, "testReturnOneParam", args);
        Assert.assertNull(returns);
    }

    @Test(description = "Test one parameter and one return value")
    public void testReturnOneParamOneReturnArg() {
        Object[] args = {10};
        Object returns = BRunUtil.invoke(compileResult, "testReturnOneParamOneReturnArg", args);

        Assert.assertEquals(10L, returns);
    }

    @Test(description = "Test one parameter and one return value")
    public void testReturnOneParamOneVarDclOneReturnArg() {
        Object[] args = {10};
        Object returns = BRunUtil.invoke(compileResult, "testReturnOneParamOneVarDclOneReturnArg", args);

        Assert.assertEquals(20L, returns);
    }

    @Test(description = "Test two return values")
    public void testReturnTwoVarDclsTwoReturnArgs() {
        Object[] args = {};
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testReturnNoParamTwoVarDclsTwoReturnArgs", args);

        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals(10L, returns.get(0));
        Assert.assertEquals("john", returns.get(1).toString());
    }

    @Test(description = "Test three return values")
    public void testReturnThreeVarDclsThreeReturnArgs() {
        Object[] args = {10, 30};
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testReturnThreeVarDclsThreeReturnArgs", args);

        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(50L, returns.get(0));
        Assert.assertEquals("john", returns.get(1).toString());
        Assert.assertEquals(1.0d, (double) returns.get(2), DELTA);
    }

    @Test(description = "Test one parameter and one return value")
    public void testSplitString() {
        Object[] args = {};
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testSplitString", args);

        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals("section1", returns.get(0).toString());
        Assert.assertEquals("section2", returns.get(1).toString());
        Assert.assertEquals("section3", returns.get(2).toString());
    }

    @Test(description = "Test one parameter and one return value")
    public void testToUpperUtil() {
        Object[] args = {StringUtils.fromString("section")};
        Object returns = BRunUtil.invoke(compileResult, "testToUpperUtil", args);

        Assert.assertEquals("SECTION", returns.toString());
    }

    @Test(description = "Test one parameter and one return value")
    public void testToUpperUtilDouble() {
        Object[] args = {StringUtils.fromString("name1"), StringUtils.fromString("name2")};
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testToUpperUtilDouble", args);

        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals("NAME1", returns.get(0).toString());
        Assert.assertEquals("NAME2", returns.get(1).toString());
    }

    @Test(description = "Test one parameter and one return value")
    public void testToUpper() {
        Object[] args = {StringUtils.fromString("section")};
        Object returns = BRunUtil.invoke(compileResult, "testToUpper", args);

        Assert.assertEquals("SECTION", returns.toString());
    }

    @Test(description = "Test one parameter and one return value")
    public void testToUpper1() {
        Object[] args = {StringUtils.fromString("name1"), StringUtils.fromString("name2")};
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testToUpper1", args);

        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals("SECTION", returns.get(0).toString());
        Assert.assertEquals("name2", returns.get(1).toString());
    }

    @Test(description = "Test one parameter and one return value")
    public void testToUpper2() {
        Object[] args = {StringUtils.fromString("name1"), StringUtils.fromString("name2")};
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testToUpper2", args);

        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals("name1", returns.get(0).toString());
        Assert.assertEquals("SECTION", returns.get(1).toString());
    }

    @Test(description = "Test one parameter and one return value")
    public void testToUpper3() {
        Object[] args = {StringUtils.fromString("name1"), StringUtils.fromString("name2")};
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testToUpper3", args);

        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals("SECTION", returns.get(0).toString());
        Assert.assertEquals("SECTION", returns.get(1).toString());
    }

    @Test(description = "Test one parameter and one return value")
    public void testToUpper4() {
        Object[] args = {StringUtils.fromString("name1"), StringUtils.fromString("name2")};
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testToUpper4", args);

        Assert.assertEquals(returns.size(), 2);
        Assert.assertEquals("NAME1", returns.get(0).toString());
        Assert.assertEquals("NAME2", returns.get(1).toString());
    }

    @Test(description = "Test return with three return arguments")
    public void testReturnWithThreeArguments() {
        Object[] args = {};
        BArray returns = (BArray) BRunUtil.invoke(compileResult, "testReturnWithThreeArguments", args);

        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(10L, returns.get(0));
        Assert.assertEquals("foo", returns.get(1).toString());
        Assert.assertEquals(4L, returns.get(2));
    }

    @Test(description = "Test return statement in resource")
    public void testReturnInResource() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/return-in-resource.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}

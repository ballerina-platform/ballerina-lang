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

import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
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
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testReturn", args);
        Assert.assertNull(returns[0]);
    }

    @Test(description = "Test void return")
    public void testReturnOneVarDcl() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testReturnOneVarDcl", args);
        Assert.assertNull(returns[0]);
    }

    @Test(description = "Test one return value")
    public void testReturnOneReturnArg() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testReturnOneReturnArg", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(5, ((BInteger) returns[0]).intValue());
    }

    @Test(description = "Test one parameter but void return")
    public void testReturnOneParam() {
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testReturnOneParam", args);
        Assert.assertNull(returns[0]);
    }

    @Test(description = "Test one parameter and one return value")
    public void testReturnOneParamOneReturnArg() {
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testReturnOneParamOneReturnArg", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(10, ((BInteger) returns[0]).intValue());
    }

    @Test(description = "Test one parameter and one return value")
    public void testReturnOneParamOneVarDclOneReturnArg() {
        BValue[] args = {new BInteger(10)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testReturnOneParamOneVarDclOneReturnArg", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(20, ((BInteger) returns[0]).intValue());
    }

    @Test(description = "Test two return values")
    public void testReturnTwoVarDclsTwoReturnArgs() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testReturnNoParamTwoVarDclsTwoReturnArgs", args);

        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals(10, ((BInteger) returns[0]).intValue());
        Assert.assertEquals("john", returns[1].stringValue());
    }

    @Test(description = "Test three return values")
    public void testReturnThreeVarDclsThreeReturnArgs() {
        BValue[] args = {new BInteger(10), new BInteger(30)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testReturnThreeVarDclsThreeReturnArgs", args);

        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(50, ((BInteger) returns[0]).intValue());
        Assert.assertEquals("john", returns[1].stringValue());
        Assert.assertEquals(1.0f, ((BFloat) returns[2]).floatValue(), DELTA);
    }

    @Test(description = "Test one parameter and one return value")
    public void testSplitString() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testSplitString", args);

        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals("section1", returns[0].stringValue());
        Assert.assertEquals("section2", returns[1].stringValue());
        Assert.assertEquals("section3", returns[2].stringValue());
    }

    @Test(description = "Test one parameter and one return value")
    public void testToUpperUtil() {
        BValue[] args = {new BString("section")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToUpperUtil", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals("SECTION", returns[0].stringValue());
    }

    @Test(description = "Test one parameter and one return value")
    public void testToUpperUtilDouble() {
        BValue[] args = {new BString("name1"), new BString("name2")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToUpperUtilDouble", args);

        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals("NAME1", returns[0].stringValue());
        Assert.assertEquals("NAME2", returns[1].stringValue());
    }

    @Test(description = "Test one parameter and one return value")
    public void testToUpper() {
        BValue[] args = {new BString("section")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToUpper", args);

        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals("SECTION", returns[0].stringValue());
    }

    @Test(description = "Test one parameter and one return value")
    public void testToUpper1() {
        BValue[] args = {new BString("name1"), new BString("name2")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToUpper1", args);

        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals("SECTION", returns[0].stringValue());
        Assert.assertEquals("name2", returns[1].stringValue());
    }

    @Test(description = "Test one parameter and one return value")
    public void testToUpper2() {
        BValue[] args = {new BString("name1"), new BString("name2")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToUpper2", args);

        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals("name1", returns[0].stringValue());
        Assert.assertEquals("SECTION", returns[1].stringValue());
    }

    @Test(description = "Test one parameter and one return value")
    public void testToUpper3() {
        BValue[] args = {new BString("name1"), new BString("name2")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToUpper3", args);

        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals("SECTION", returns[0].stringValue());
        Assert.assertEquals("SECTION", returns[1].stringValue());
    }

    @Test(description = "Test one parameter and one return value")
    public void testToUpper4() {
        BValue[] args = {new BString("name1"), new BString("name2")};
        BValue[] returns = BRunUtil.invoke(compileResult, "testToUpper4", args);

        Assert.assertEquals(returns.length, 2);
        Assert.assertEquals("NAME1", returns[0].stringValue());
        Assert.assertEquals("NAME2", returns[1].stringValue());
    }

    @Test(description = "Test return with three return arguments")
    public void testReturnWithThreeArguments() {
        BValue[] args = {};
        BValue[] returns = BRunUtil.invoke(compileResult, "testReturnWithThreeArguments", args);

        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(10, ((BInteger) returns[0]).intValue());
        Assert.assertEquals("foo", returns[1].stringValue());
        Assert.assertEquals(4, ((BInteger) returns[2]).intValue());
    }

    @Test(description = "Test return statement in resource")
    public void testReturnInResource() {
        CompileResult result = BCompileUtil.compile("test-src/statements/returnstmt/return-in-resource.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
    }

}

/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.jvm;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases to cover some basic types related tests on JBallerina.
 *
 * @since 0.955.0
 */
public class TypesTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/jvm/types.bal");
    }

    @Test
    public void testInt1() {
        BValue[] result = BRunUtil.invoke(compileResult, "testIntWithoutArgs", new BValue[]{});
        Assert.assertEquals(((BInteger) result[0]).intValue(), 7);
    }

    @Test
    public void testInt2() {
        BValue[] result = BRunUtil.invoke(compileResult, "testIntWithArgs", new BValue[]{new BInteger(5)});
        Assert.assertEquals(((BInteger) result[0]).intValue(), 10);
    }

    @Test
    public void testString1() {
        BValue[] result = BRunUtil.invoke(compileResult, "testStringWithoutArgs", new BValue[]{});
        Assert.assertEquals((result[0]).stringValue(), "Hello");
    }

    @Test
    public void testString2() {
        BValue[] result = BRunUtil.invoke(compileResult, "testStringWithArgs", new BValue[]{new BString("World")});
        Assert.assertEquals((result[0]).stringValue(), "HelloWorld");
    }

    @Test
    public void testArray() {
        BValue[] result = BRunUtil.invoke(compileResult, "testArray", new BValue[]{new BString("World")});
        Assert.assertEquals((result[0]).stringValue(), "3");
    }

    @Test
    public void getGlobalVar() {
        BValue[] returns = BRunUtil.invoke(compileResult, "getGlobalVar");
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 7);
    }

    @Test
    public void testTuple() {
        //Todo: revisit when tuple access and var type supported
        BValue[] result = BRunUtil.invoke(compileResult, "tupleTest");
        Assert.assertEquals((result[0]).stringValue(), "10");
    }

    @Test
    public void testRecords() {
        BValue[] result = BRunUtil.invoke(compileResult, "recordsTest");
        Assert.assertEquals((result[0]).stringValue(), "JBallerina");
    }

    @Test
    public void testUnions() {
        BValue[] result = BRunUtil.invoke(compileResult, "unionTest");
        Assert.assertEquals((result[0]).stringValue(), "10.5");
    }
}

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

package org.ballerinalang.test.types.var;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BByte;
import org.ballerinalang.core.model.values.BDecimal;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.math.BigDecimal;

/**
 * Class to test functionality of top level variables declarations with var.
 */
public class TopLevelVarDeclarationTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/var/top-level-var-declaration.bal");
    }

    @Test
    public void testGetInt() {
        BValue[] returns = BRunUtil.invoke(result, "testGetInt");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 10);
    }

    @Test
    public void testGetString() {
        BValue[] returns = BRunUtil.invoke(result, "testGetString");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals((returns[0]).stringValue(), "Ballerina");
    }

    @Test
    public void testGetDecimal() {
        BValue[] returns = BRunUtil.invoke(result, "testGetDecimal");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BDecimal) returns[0]).decimalValue().compareTo(new BigDecimal(100.0)), 0);
    }

    @Test
    public void testGetBoolean() {
        BValue[] returns = BRunUtil.invoke(result, "testGetBoolean");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testGetByte() {
        BValue[] returns = BRunUtil.invoke(result, "testGetByte");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BByte) returns[0]).byteValue(), 2);
    }

    @Test
    public void testGetFloat() {
        BValue[] returns = BRunUtil.invoke(result, "testGetFloat");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(((BFloat) returns[0]).floatValue(), 2.0);
    }

    @Test
    public void testFunctionInvocation() {
        BValue[] returns = BRunUtil.invoke(result, "testFunctionInvocation");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "{\"k\":\"v\"}");
    }

    @Test
    public void testVarAssign() {
        BValue[] returns = BRunUtil.invoke(result, "testVarAssign");
        Assert.assertEquals(returns.length, 1);
        Assert.assertEquals(returns[0].stringValue(), "{\"x\":\"y\"}");
    }
}

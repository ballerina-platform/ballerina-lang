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

import io.ballerina.runtime.api.values.BDecimal;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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
        Object returns = BRunUtil.invoke(result, "testGetInt");
        Assert.assertEquals(returns, 10L);
    }

    @Test
    public void testGetString() {
        Object returns = BRunUtil.invoke(result, "testGetString");
        Assert.assertEquals((returns).toString(), "Ballerina");
    }

    @Test
    public void testGetDecimal() {
        BDecimal returns = (BDecimal) BRunUtil.invoke(result, "testGetDecimal");
        Assert.assertEquals(returns.value().compareTo(new BigDecimal(100.0)), 0);
    }

    @Test
    public void testGetBoolean() {
        Object returns = BRunUtil.invoke(result, "testGetBoolean");
        Assert.assertTrue((Boolean) returns);
    }

    @Test
    public void testGetByte() {
        Object returns = BRunUtil.invoke(result, "testGetByte");
        Assert.assertEquals(returns, 2);
    }

    @Test
    public void testGetFloat() {
        Object returns = BRunUtil.invoke(result, "testGetFloat");
        Assert.assertEquals(returns, 2.0);
    }

    @Test
    public void testFunctionInvocation() {
        Object returns = BRunUtil.invoke(result, "testFunctionInvocation");
        Assert.assertEquals(returns.toString(), "{\"k\":\"v\"}");
    }

    @Test
    public void testVarAssign() {
        Object returns = BRunUtil.invoke(result, "testVarAssign");
        Assert.assertEquals(returns.toString(), "{\"x\":\"y\"}");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}

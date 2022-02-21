/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.types.string;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.JvmRunUtil;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test StringValue impl of ballerina string.
 */
public class StringValueRecordTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/string/string-value-record-test.bal");
    }

    @Test
    public void testRecordStringValue() {
        testAndAssert("testRecordStringValue", 5);
    }

    @Test
    public void testRecordGetKeys() {
        testAndAssert("testRecordGetKeys", 31);
    }

    @Test
    public void testMapToKeys() {
        testAndAssert("testMapToKeys", 38);
    }

    @Test
    public void testOpenRecord() {
        testAndAssert("testOpenRecord", 18);
    }

    private void testAndAssert(String funcName, long i) {
        Object returns = JvmRunUtil.invoke(result, funcName);
        Assert.assertEquals(returns.getClass(), Long.class);
        Assert.assertEquals(returns, i);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}

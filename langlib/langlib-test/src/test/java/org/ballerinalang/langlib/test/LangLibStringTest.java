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
 */

package org.ballerinalang.langlib.test;


import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for the lang.string library.
 *
 * @since 1.0
 */
public class LangLibStringTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/stringlib_test.bal");
    }

    @Test
    public void testToLower() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testToLower");
        assertEquals(returns[0].stringValue(), "hello ballerina!");
    }

    @Test
    public void testLength() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testLength");
        assertEquals(((BInteger) returns[0]).intValue(), "Hello Ballerina!".length());
    }

    @Test
    public void testSubString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testSubString");
        assertEquals(returns[0].stringValue(), "Bal");
    }

    @Test(enabled = false)
    public void testIterator() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIterator");
        assertEquals(returns[0].stringValue(), "Bal");
    }

    @Test
    public void testConcat() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConcat");
        assertEquals(returns[0].stringValue(), "Hello from Ballerina");
    }

    @Test
    public void testStartsWith() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testStartsWith");
        assertTrue(((BBoolean) returns[0]).booleanValue());
    }
}

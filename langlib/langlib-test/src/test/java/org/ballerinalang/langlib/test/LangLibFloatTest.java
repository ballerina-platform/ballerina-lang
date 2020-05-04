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


import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for the lang.float library.
 *
 * @since 1.0
 */
public class LangLibFloatTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/floatlib_test.bal");
    }

    @Test
    public void testIsFinite() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIsFinite");
        assertTrue(((BBoolean) returns[0]).booleanValue());
        assertFalse(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testIsInfinite() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIsInfinite");
        assertFalse(((BBoolean) returns[0]).booleanValue());
        assertTrue(((BBoolean) returns[1]).booleanValue());
    }

    @Test
    public void testSum() {

        BValue[] returns = BRunUtil.invoke(compileResult, "testSum");
        assertEquals(((BFloat) returns[0]).floatValue(), 70.35);
    }

    @Test
    public void testFloatConsts() {

        BValue[] returns = BRunUtil.invoke(compileResult, "testFloatConsts");
        assertEquals(((BFloat) returns[0]).floatValue(), Double.NaN);
        assertEquals(((BFloat) returns[1]).floatValue(), Double.POSITIVE_INFINITY);
    }
}

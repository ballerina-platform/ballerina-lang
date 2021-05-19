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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.expressions.literals;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Test cases for the usage of `null` literal in Ballerina code. `null` is only allowed in JSON contexts.
 *
 * @since 0.990.4
 */
public class NullLiteralUsageTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/literals/null_literal_usage.bal");
    }

    @Test
    public void testNullAssignment() {
        BValue[] returns = BRunUtil.invoke(result, "testNullAssignment");
        assertNull(returns[0]);
    }

    @Test
    public void testNullInField() {
        BValue[] returns = BRunUtil.invoke(result, "testNullInField");
        assertNull(returns[0]);
    }

    @Test
    public void testNullStringRepresentation() {
        BValue[] returns = BRunUtil.invoke(result, "testNullStringRepresentation");
        assertEquals(returns[0].stringValue(), "");
    }

    @Test
    public void testNullStringRepresentation2() {
        BValue[] returns = BRunUtil.invoke(result, "testNullStringRepresentation2");
        assertEquals(returns[0].stringValue(), "null");
    }

    @Test
    public void testNullStringRepresentation3() {
        BValue[] returns = BRunUtil.invoke(result, "testNullStringRepresentation3");
        assertEquals(returns[0].stringValue(), "{\"name\":\"John Doe\",\"age\":25,\"location\":null}");
    }

    @Test
    public void testNullStringRepresentation4() {
        BValue[] returns = BRunUtil.invoke(result, "testNullStringRepresentation4");
        assertEquals(returns[0].stringValue(), "{\"name\":\"John Doe\",\"age\":25,\"location\":null}");
    }

    @Test
    public void testNullStringRepresentation5() {
        BValue[] returns = BRunUtil.invoke(result, "testNullStringRepresentation5");
        assertEquals(returns[0].stringValue(), "()");
    }

    @Test
    public void testNullReturn() {
        BValue[] returns = BRunUtil.invoke(result, "testNullReturn");
        assertNull(returns[0]);
    }

    @Test
    public void testNullReturn2() {
        BValue[] returns = BRunUtil.invoke(result, "testNullReturn2");
        assertNull(returns[0]);
    }

    @Test
    public void testNullInFnParams() {
        BValue[] returns = BRunUtil.invoke(result, "testNullInFnParams");
        assertNull(returns[0]);
    }

    @Test
    public void testNullInATuple() {
        BValue[] returns = BRunUtil.invoke(result, "testNullInATuple");
        assertEquals(((BInteger) returns[0]).intValue(), 50);
        assertNull(returns[1]);
        assertEquals(returns[2].stringValue(), "foo");
    }

    @Test
    public void testNullWithTypeGuard() {
        BValue[] returns = BRunUtil.invoke(result, "testNullWithTypeGuard");
        assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @Test
    public void testNullWithMatch() {
        BValue[] returns = BRunUtil.invoke(result, "testNullWithMatch");
        assertEquals(returns[0].stringValue(), "null");
    }

    @Test
    public void testNullInArray() {
        BValue[] returns = BRunUtil.invoke(result, "testNullInArray");
        assertNull(returns[0]);
    }

    @Test
    public void testNullInNestedTernaryExpr() {
        BValue[] returns = BRunUtil.invoke(result, "testNullInNestedTernaryExpr");
        assertNull(returns[0]);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}

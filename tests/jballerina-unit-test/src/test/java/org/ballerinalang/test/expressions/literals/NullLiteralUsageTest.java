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
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;
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

    @Test(enabled = false)
    public void testInvalidNullLiteralUsage() {
        final String errorMsg = "'null' literal is only supported for 'json'";
        CompileResult result = BCompileUtil.compile("test-src/expressions/literals/null_literal_usage_negative.bal");
        int index = 0;

        assertEquals(result.getErrorCount(), 20);
        validateError(result, index++, errorMsg, 17, 13);
        validateError(result, index++, errorMsg, 20, 18);
        validateError(result, index++, errorMsg, 22, 15);
        validateError(result, index++, errorMsg, 27, 38);
        validateError(result, index++, errorMsg, 29, 22);
        validateError(result, index++, errorMsg, 31, 27);
        validateError(result, index++, errorMsg, 31, 37);
        validateError(result, index++, errorMsg, 32, 27);
        validateError(result, index++, errorMsg, 34, 14);
        validateError(result, index++, errorMsg, 38, 12);
        validateError(result, index++, errorMsg, 42, 45);
        validateError(result, index++, errorMsg, 50, 20);
        validateError(result, index++, errorMsg, 54, 17);
        validateError(result, index++, errorMsg, 62, 9);
        validateError(result, index++, errorMsg, 68, 15);
        validateError(result, index++, errorMsg, 73, 14);
        validateError(result, index++, "incompatible types: expected '()', found 'string'", 78, 18);
        validateError(result, index++, errorMsg, 78, 67);
        validateError(result, index++, errorMsg, 78, 74);
        validateError(result, index, errorMsg, 78, 81);
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

    // disabled due to https://github.com/ballerina-platform/ballerina-lang/issues/13384
    @Test(enabled = false)
    public void testNullStringRepresentation() {
        BValue[] returns = BRunUtil.invoke(result, "testNullStringRepresentation");
        assertEquals(returns[0].stringValue(), "null");
    }

    // disabled due to https://github.com/ballerina-platform/ballerina-lang/issues/13384
    @Test(enabled = false)
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
}

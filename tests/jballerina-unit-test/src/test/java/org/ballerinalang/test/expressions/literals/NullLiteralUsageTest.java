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

import io.ballerina.runtime.api.values.BArray;
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
        Object returns = BRunUtil.invoke(result, "testNullAssignment");
        assertNull(returns);
    }

    @Test
    public void testNullInField() {
        Object returns = BRunUtil.invoke(result, "testNullInField");
        assertNull(returns);
    }

    @Test
    public void testNullStringRepresentation() {
        Object returns = BRunUtil.invoke(result, "testNullStringRepresentation");
        assertEquals(returns.toString(), "");
    }

    @Test
    public void testNullStringRepresentation2() {
        Object returns = BRunUtil.invoke(result, "testNullStringRepresentation2");
        assertEquals(returns.toString(), "null");
    }

    @Test
    public void testNullStringRepresentation3() {
        Object returns = BRunUtil.invoke(result, "testNullStringRepresentation3");
        assertEquals(returns.toString(), "{\"name\":\"John Doe\",\"age\":25,\"location\":null}");
    }

    @Test
    public void testNullStringRepresentation4() {
        Object returns = BRunUtil.invoke(result, "testNullStringRepresentation4");
        assertEquals(returns.toString(), "{\"name\":\"John Doe\",\"age\":25,\"location\":null}");
    }

    @Test
    public void testNullStringRepresentation5() {
        Object returns = BRunUtil.invoke(result, "testNullStringRepresentation5");
        assertEquals(returns.toString(), "()");
    }

    @Test
    public void testNullReturn() {
        Object returns = BRunUtil.invoke(result, "testNullReturn");
        assertNull(returns);
    }

    @Test
    public void testNullReturn2() {
        Object returns = BRunUtil.invoke(result, "testNullReturn2");
        assertNull(returns);
    }

    @Test
    public void testNullInFnParams() {
        Object returns = BRunUtil.invoke(result, "testNullInFnParams");
        assertNull(returns);
    }

    @Test
    public void testNullInATuple() {
        Object arr = BRunUtil.invoke(result, "testNullInATuple");
        BArray returns = (BArray) arr;
        assertEquals(returns.get(0), 50L);
        assertNull(returns.get(1));
        assertEquals(returns.get(2).toString(), "foo");
    }

    @Test
    public void testNullWithTypeGuard() {
        Object returns = BRunUtil.invoke(result, "testNullWithTypeGuard");
        assertTrue((Boolean) returns);
    }

    @Test
    public void testNullWithMatch() {
        Object returns = BRunUtil.invoke(result, "testNullWithMatch");
        assertEquals(returns.toString(), "null");
    }

    @Test
    public void testNullInArray() {
        Object returns = BRunUtil.invoke(result, "testNullInArray");
        assertNull(returns);
    }

    @Test
    public void testNullInNestedTernaryExpr() {
        Object returns = BRunUtil.invoke(result, "testNullInNestedTernaryExpr");
        assertNull(returns);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}

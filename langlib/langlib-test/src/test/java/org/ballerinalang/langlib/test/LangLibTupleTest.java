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


import org.ballerinalang.core.model.types.TypeTags;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
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
 * Test cases for array lib function usage for tuples.
 *
 * @since 1.0
 */
public class LangLibTupleTest {

    private CompileResult compileResult, negativeResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/tuplelib_test.bal");
        negativeResult = BCompileUtil.compile("test-src/tuplelib_test_negative.bal");
    }

    @Test
    public void testEnumerate() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testEnumerate");
        assertEquals(returns[0].getType().getTag(), TypeTags.ARRAY_TAG);

        BValueArray arr = (BValueArray) returns[0];
        assertEquals(arr.elementType.getTag(), TypeTags.TUPLE_TAG);
        assertEquals(arr.size(), 4);
        assertEquals(arr.getRefValue(0).stringValue(), "[0, 1]");
        assertEquals(arr.getRefValue(1).stringValue(), "[1, \"John Doe\"]");
        assertEquals(arr.getRefValue(2).stringValue(), "[2, 25]");
        assertEquals(arr.getRefValue(3).stringValue(), "[3, 5.9]");
    }

    @Test
    public void testLength() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testLength");
        assertEquals(((BInteger) returns[0]).intValue(), 4);
    }

    @Test
    public void testMap() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testMap");
        assertEquals(returns[0].getType().getTag(), TypeTags.ARRAY_TAG);

        BValueArray arr = (BValueArray) returns[0];
        assertEquals(arr.elementType.getTag(), TypeTags.UNION_TAG);
        assertEquals(((BInteger) arr.getRefValue(0)).intValue(), 10);
        assertEquals(arr.getRefValue(1).stringValue(), "mapped John Doe");
        assertEquals(((BInteger) arr.getRefValue(2)).intValue(), 250);
        assertEquals(((BFloat) arr.getRefValue(3)).floatValue(), 29.5);
    }

    @Test
    public void testForeach() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testForeach");
        assertEquals(((BInteger) returns[0]).intValue(), 26);
    }

    @Test
    public void testSlice() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testSlice");
        assertEquals(returns[0].getType().getTag(), TypeTags.ARRAY_TAG);

        BValueArray arr = (BValueArray) returns[0];
        assertEquals(arr.size(), 3);
        assertEquals(arr.getRefValue(0).stringValue(), "Foo");
        assertEquals(((BFloat) arr.getRefValue(1)).floatValue(), 12.34);
        assertTrue(((BBoolean) arr.getRefValue(2)).booleanValue());
    }

    @Test(expectedExceptions = RuntimeException.class,
          expectedExceptionsMessageRegExp = ".*error: \\{ballerina/lang.array\\}OperationNotSupported.*")
    public void testRemove() {
        BRunUtil.invoke(compileResult, "testRemove");
    }

    @Test(expectedExceptions = RuntimeException.class,
          expectedExceptionsMessageRegExp = ".*error: \\{ballerina/lang.array\\}OperationNotSupported.*")
    public void testSort() {
        BRunUtil.invoke(compileResult, "testSort");
    }

    @Test
    public void testReduce() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testReduce");
        assertEquals(((BFloat) returns[0]).floatValue(), 13.8);
    }

    @Test
    public void testIterableOpChain() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIterableOpChain");
        assertEquals(((BFloat) returns[0]).floatValue(), 3.25);
    }

    @Test
    public void testIndexOf() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testIndexOf");
        assertEquals(((BInteger) returns[0]).intValue(), 4);
        assertNull(returns[1]);
    }

    @Test
    public void testPush1() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testPush1");
        assertEquals(((BInteger) returns[0]).intValue(), 4);
    }

    @Test
    public void testPush2() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testPush2");
        assertEquals(((BInteger) returns[0]).intValue(), 4);
    }

    @Test
    public void testNegativeCases() {
        assertEquals(negativeResult.getErrorCount(), 1);
        validateError(negativeResult, 0, "incompatible types: expected '[int,string][]', " +
                "found '[int,(string|int)][]'", 20, 25);
    }

}

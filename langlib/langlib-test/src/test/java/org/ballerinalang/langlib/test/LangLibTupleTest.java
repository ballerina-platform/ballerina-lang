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

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.types.ArrayType;
import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.JvmRunUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static io.ballerina.runtime.api.utils.TypeUtils.getType;
import static org.ballerinalang.test.BAssertUtil.validateError;
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
        Object[] returns = JvmRunUtil.invoke(compileResult, "testEnumerate");
        assertEquals(getType(returns[0]).getTag(), TypeTags.ARRAY_TAG);

        BArray arr = (BArray) returns[0];
        assertEquals(((ArrayType) arr.getType()).getElementType().getTag(), TypeTags.TUPLE_TAG);
        assertEquals(arr.size(), 4);
        assertEquals(arr.getRefValue(0).toString(), "[0,1]");
        assertEquals(arr.getRefValue(1).toString(), "[1,\"John Doe\"]");
        assertEquals(arr.getRefValue(2).toString(), "[2,25]");
        assertEquals(arr.getRefValue(3).toString(), "[3,5.9]");
    }

    @Test
    public void testLength() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testLength");
        assertEquals(returns[0], 4L);
    }

    @Test
    public void testMap() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testMap");
        assertEquals(getType(returns[0]).getTag(), TypeTags.ARRAY_TAG);

        BArray arr = (BArray) returns[0];
        assertEquals(((ArrayType) arr.getType()).getElementType().getTag(), TypeTags.UNION_TAG);
        assertEquals(arr.getRefValue(0), 10L);
        assertEquals(arr.getRefValue(1).toString(), "mapped John Doe");
        assertEquals(arr.getRefValue(2), 250L);
        assertEquals(arr.getRefValue(3), 29.5d);
    }

    @Test
    public void testForeach() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testForeach");
        assertEquals(returns[0], 26L);
    }

    @Test
    public void testSlice() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testSlice");
        assertEquals(getType(returns[0]).getTag(), TypeTags.ARRAY_TAG);

        BArray arr = (BArray) returns[0];
        assertEquals(arr.size(), 3);
        assertEquals(arr.getRefValue(0).toString(), "Foo");
        assertEquals(arr.getRefValue(1), 12.34d);
        assertTrue((Boolean) arr.getRefValue(2));
    }

    @Test(expectedExceptions = RuntimeException.class,
          expectedExceptionsMessageRegExp = ".*error: \\{ballerina/lang.array\\}OperationNotSupported.*")
    public void testRemove() {
        JvmRunUtil.invoke(compileResult, "testRemove");
    }

    @Test(expectedExceptions = RuntimeException.class,
          expectedExceptionsMessageRegExp = ".*error: \\{ballerina/lang.array\\}OperationNotSupported.*")
    public void testSort() {
        JvmRunUtil.invoke(compileResult, "testSort");
    }

    @Test
    public void testReduce() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testReduce");
        assertEquals(returns[0], 13.8d);
    }

    @Test
    public void testIterableOpChain() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testIterableOpChain");
        assertEquals(returns[0], 3.25d);
    }

    @Test
    public void testIndexOf() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testIndexOf");
        BArray result = (BArray) returns[0];
        assertEquals(result.get(0), 4L);
        assertNull(result.get(1));
    }

    @Test
    public void testPush1() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testPush1");
        assertEquals(returns[0], 4L);
    }

    @Test
    public void testPush2() {
        Object[] returns = JvmRunUtil.invoke(compileResult, "testPush2");
        assertEquals(returns[0], 4L);
    }

    @Test(dataProvider = "testToStreamFunctionList")
    public void testToStream(String funcName) {
        JvmRunUtil.invoke(compileResult, funcName);
    }

    @Test
    public void testNegativeCases() {
        assertEquals(negativeResult.getErrorCount(), 1);
        validateError(negativeResult, 0, "incompatible types: expected '[int,string][]', " +
                "found '[int,(string|int)][]'", 20, 25);
    }

    @DataProvider(name = "testToStreamFunctionList")
    public Object[] testToStreamFunctions() {
        return new Object[]{
                "testToStream",
                "testToStreamOnImmutableTuple"
        };
    }
}

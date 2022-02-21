/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.query;

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * TestCases for query expression with iterable objects.
 *
 * @since 1.2.1
 */
public class QueryExpressionIterableObjectTest {

    private CompileResult program;

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/query/query-exp-iterable-objects.bal");
    }

    @Test
    public void testIterableObject() {
        Object returns = BRunUtil.invoke(program, "testIterableObject");
        BArray array = (BArray) returns;
        int i = 0;
        Assert.assertEquals(array.getInt(i++), 12);
        Assert.assertEquals(array.getInt(i++), 34);
        Assert.assertEquals(array.getInt(i++), 56);
        Assert.assertEquals(array.getInt(i++), 34);
        Assert.assertEquals(array.getInt(i++), 78);
        Assert.assertEquals(array.getInt(i++), 21);
        Assert.assertEquals(array.getInt(i), 90);

    }

    @Test
    public void testNestedIterableObject() {
        Object returns = BRunUtil.invoke(program, "testNestedIterableObject");
        BArray array = (BArray) returns;
        int i = 0;
        Assert.assertEquals(array.getInt(i++), 12);
        Assert.assertEquals(array.getInt(i++), 34);
        Assert.assertEquals(array.getInt(i++), 56);
        Assert.assertEquals(array.getInt(i++), 34);
        Assert.assertEquals(array.getInt(i++), 78);
        Assert.assertEquals(array.getInt(i++), 21);
        Assert.assertEquals(array.getInt(i++), 90);
        Assert.assertEquals(array.getInt(i++), 12);
        Assert.assertEquals(array.getInt(i++), 34);
        Assert.assertEquals(array.getInt(i++), 56);
        Assert.assertEquals(array.getInt(i++), 34);
        Assert.assertEquals(array.getInt(i++), 78);
        Assert.assertEquals(array.getInt(i++), 21);
        Assert.assertEquals(array.getInt(i), 90);

    }

    @Test
    public void testIterableWithError() {
        BRunUtil.invoke(program, "testIterableWithError");
    }

    @Test
    public void testStreamOfStreams() {
        Object returns = BRunUtil.invoke(program, "testStreamOfStreams");
        BArray array = (BArray) returns;
        int i = 0;
        Assert.assertEquals(array.getInt(i++), 1);
        Assert.assertEquals(array.getInt(i++), 2);
        Assert.assertEquals(array.getInt(i++), 3);
        Assert.assertEquals(array.getInt(i++), 4);
        Assert.assertEquals(array.getInt(i++), 1);
        Assert.assertEquals(array.getInt(i++), 2);
        Assert.assertEquals(array.getInt(i++), 3);
        Assert.assertEquals(array.getInt(i++), 4);
        Assert.assertEquals(array.getInt(i++), 1);
        Assert.assertEquals(array.getInt(i++), 2);
        Assert.assertEquals(array.getInt(i++), 3);
        Assert.assertEquals(array.getInt(i), 4);
    }

    @Test
    public void testIteratorInStream() {
        Object returns = BRunUtil.invoke(program, "testIteratorInStream");
        BArray array = (BArray) returns;
        int i = 0;
        Assert.assertEquals(array.getInt(i++), 1);
        Assert.assertEquals(array.getInt(i++), 2);
        Assert.assertEquals(array.getInt(i++), 3);
        Assert.assertEquals(array.getInt(i++), 4);
        Assert.assertEquals(array.getInt(i), 5);
    }

    @Test
    public void testObjectIterator() {
        BRunUtil.invoke(program, "testObjectIterator");
    }

    @Test
    public void testIterableObjectQueryNegative() {
        CompileResult negativeResult =
                BCompileUtil.compile("test-src/query/query_exp_iterable_objects_negative.bal");
        Assert.assertEquals(negativeResult.getErrorCount(), 3);
        int index = 0;
        validateError(negativeResult, index++, "invalid iterable type 'IterableObject': an iterable object must be" +
                " a subtype of 'ballerina/lang.object:0.0.0:Iterable'", 43, 39);
        validateError(negativeResult, index++, "invalid iterable type 'IterableObject': an iterable object must be" +
                " a subtype of 'ballerina/lang.object:0.0.0:Iterable'", 73, 39);
        validateError(negativeResult, index++, "mismatched function signatures: expected 'public function iterator()" +
                " returns object { public function next () returns ((record {| (any|error) value; " +
                "|}|error)?); }', found 'public function iterator() returns _Iterator'", 90, 9);
    }

    @AfterClass
    public void tearDown() {
        program = null;
    }
}

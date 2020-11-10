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

import org.ballerinalang.core.model.values.BError;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
        BValue[] returns = BRunUtil.invoke(program, "testIterableObject");
        BValueArray array = (BValueArray) returns[0];
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
        BValue[] returns = BRunUtil.invoke(program, "testNestedIterableObject");
        BValueArray array = (BValueArray) returns[0];
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
        BValue[] returnValues = BRunUtil.invoke(program, "testIterableWithError");
        Assert.assertNotNull(returnValues);
        Assert.assertEquals(returnValues.length, 1, "Expected events are not received");
        Assert.assertTrue(returnValues[0] instanceof BError, "Expected BErrorType type value");
    }

    @Test
    public void testStreamOfStreams() {
        BValue[] returns = BRunUtil.invoke(program, "testStreamOfStreams");
        BValueArray array = (BValueArray) returns[0];
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
        BValue[] returns = BRunUtil.invoke(program, "testIteratorInStream");
        BValueArray array = (BValueArray) returns[0];
        int i = 0;
        Assert.assertEquals(array.getInt(i++), 1);
        Assert.assertEquals(array.getInt(i++), 2);
        Assert.assertEquals(array.getInt(i++), 3);
        Assert.assertEquals(array.getInt(i++), 4);
        Assert.assertEquals(array.getInt(i), 5);
    }
}

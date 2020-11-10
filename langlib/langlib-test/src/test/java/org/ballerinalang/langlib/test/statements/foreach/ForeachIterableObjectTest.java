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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langlib.test.statements.foreach;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TestCases for foreach with iterable objects.
 *
 * @since 1.1.0
 */
public class ForeachIterableObjectTest {

    private CompileResult program;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        program = BCompileUtil.compile("test-src/statements/foreach/foreach-iterable-objects.bal");
        negativeResult = BCompileUtil.compile("test-src/statements/foreach/foreach-iterable-object-negative.bal");
    }

    @Test
    public void testIterableObject() {
        BValue[] returns = BRunUtil.invoke(program, "testIterableObject");

        BValueArray arr = (BValueArray) returns[0];
        Assert.assertEquals(arr.size(), 7);
        int i = 0;
        Assert.assertEquals(arr.getInt(i++), 12);
        Assert.assertEquals(arr.getInt(i++), 34);
        Assert.assertEquals(arr.getInt(i++), 56);
        Assert.assertEquals(arr.getInt(i++), 34);
        Assert.assertEquals(arr.getInt(i++), 78);
        Assert.assertEquals(arr.getInt(i++), 21);
        Assert.assertEquals(arr.getInt(i), 90);

    }

    @Test
    public void testNestedIterableObject() {
        BValue[] returns = BRunUtil.invoke(program, "testNestedIterableObject");

        BValueArray arr = (BValueArray) returns[0];
        Assert.assertEquals(arr.size(), 14);
        int i = 0;
        Assert.assertEquals(arr.getInt(i++), 12);
        Assert.assertEquals(arr.getInt(i++), 34);
        Assert.assertEquals(arr.getInt(i++), 56);
        Assert.assertEquals(arr.getInt(i++), 34);
        Assert.assertEquals(arr.getInt(i++), 78);
        Assert.assertEquals(arr.getInt(i++), 21);
        Assert.assertEquals(arr.getInt(i++), 90);
        Assert.assertEquals(arr.getInt(i++), 12);
        Assert.assertEquals(arr.getInt(i++), 34);
        Assert.assertEquals(arr.getInt(i++), 56);
        Assert.assertEquals(arr.getInt(i++), 34);
        Assert.assertEquals(arr.getInt(i++), 78);
        Assert.assertEquals(arr.getInt(i++), 21);
        Assert.assertEquals(arr.getInt(i), 90);

    }

    @Test
    public void testIterableObjectErrors() {
        Assert.assertEquals(negativeResult.getErrorCount(), 9);
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'object { public function next " +
                "() returns (record {| int value; |}?); }', found 'object { int[] integers; int cursorIndex; public " +
                "function next () returns ((record {| int value; |}|CustomError)?); }'", 120, 16);
        BAssertUtil.validateError(negativeResult, i++, "iterable objects must have a __iterator function with " +
                "signature,  public function __iterator() returns (object { public function next () returns (record " +
                "{| T value; |}?); });", 220, 25);
        BAssertUtil.validateError(negativeResult, i++, "iterable objects must have a __iterator function with " +
                "signature,  public function __iterator() returns (object { public function next () returns (record " +
                "{| T value; |}?); });", 222, 25);
        BAssertUtil.validateError(negativeResult, i++, "iterable objects must have a __iterator function with " +
                "signature,  public function __iterator() returns (object { public function next () returns (record " +
                "{| T value; |}?); });", 224, 25);
        BAssertUtil.validateError(negativeResult, i++, "iterable objects must have a __iterator function with " +
                "signature,  public function __iterator() returns (object { public function next () returns (record " +
                "{| T value; |}?); });", 226, 25);
        BAssertUtil.validateError(negativeResult, i++, "iterable objects must have a __iterator function with " +
                "signature,  public function __iterator() returns (object { public function next () returns (record " +
                "{| T value; |}?); });", 228, 25);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found '(int|CustomError)" +
                "'", 232, 25);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found '(int|CustomError)" +
                "'", 234, 25);
        BAssertUtil.validateError(negativeResult, i++, "incompatible types: expected 'int', found '(int|CustomError)" +
                "'", 237, 25);
    }
}

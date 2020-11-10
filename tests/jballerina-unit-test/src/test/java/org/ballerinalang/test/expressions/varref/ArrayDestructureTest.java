/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.expressions.varref;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test case for list destructure bindings.
 *
 * @since 1.3.0
 */
public class ArrayDestructureTest {

    CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/varref/array_destructure_test.bal");
    }

    @Test
    public void testSimpleArrayDestructureBinding() {
        BValue[] result = BRunUtil.invoke(compileResult, "testSimpleListBindingPattern");
    }

    @Test
    public void testSimpleArrayDestructureWithUndefinedSize() {
        BValue[] result = BRunUtil.invoke(compileResult, "testSimpleListBindingPatternWithUndefinedSize");
    }

    @Test
    public void testReferenceArrayDestructure() {
        BValue[] result = BRunUtil.invoke(compileResult, "testReferenceListBindingPattern");
    }

    @Test
    public void testReferenceArrayDestructureWithUndefinedSize() {
        BValue[] result = BRunUtil.invoke(compileResult, "testReferenceListBindingPatternWithUndefinedSize");
    }

    @Test
    public void testReferenceArrayDestructureWithRecordDestructure() {
        BValue[] result = BRunUtil.invoke(compileResult, "testReferenceListBindingPatternWithRecordDestructure");
    }

    @Test
    public void testReferenceArrayDestructureWithUndefinedSizeAndDifferentType() {
        BValue[] result = BRunUtil.invoke(compileResult
                , "testReferenceListBindingPatternForUndefinedSizeWithDifferentType");
    }

    @Test
    public void testReferenceListBindingPatternWithTuples() {
        BValue[] result = BRunUtil.invoke(compileResult, "testReferenceListBindingPatternWithTuples");
    }

    @Test
    public void testSimpleArrayDestructureNegative() {
        CompileResult negativeTestCompile = BCompileUtil
                .compile("test-src/expressions/varref/array_destructure_negative.bal");
        Assert.assertEquals(negativeTestCompile.getErrorCount(), 5);

        int index = 0;
        BAssertUtil.validateError(negativeTestCompile, index++,
                "incompatible types: expected '[int,int,int,int,int]', found 'int[4]'", 27, 23);

        BAssertUtil.validateError(negativeTestCompile, index++,
                "incompatible types: expected '[int,int,int,boolean]', found 'int[4]'", 30, 20);

        BAssertUtil.validateError(negativeTestCompile, index++,
                "incompatible types: expected '[int,int...]', found 'int[]'", 33, 17);

        BAssertUtil.validateError(negativeTestCompile, index++,
                "incompatible types: expected '[Foo]', found 'Foo[2]'", 36, 11);

        BAssertUtil.validateError(negativeTestCompile, index++,
                "incompatible types: expected '[Bar,Bar]', found 'Foo[2]'", 39, 14);
    }

}

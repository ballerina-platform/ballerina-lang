/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.test.types.future;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * This class contains future type related test cases.
 */
public class FuturePositiveTests {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/future/future_positive.bal");
    }

    @Test
    public void testBasicTypes() {
        BRunUtil.invoke(result, "testBasicTypes");
    }

    @Test(enabled = false) // https://github.com/ballerina-platform/ballerina-lang/issues/28758
    public void testBasicTypesWithoutFutureConstraint() {
        BRunUtil.invoke(result, "testBasicTypesWithoutFutureConstraint");
    }

    @Test
    public void testRefTypes() {
        BRunUtil.invoke(result, "testRefTypes");
    }

    @Test(enabled = false) // https://github.com/ballerina-platform/ballerina-lang/issues/28758
    public void testRefTypesWithoutFutureConstraint() {
        BRunUtil.invoke(result, "testRefTypesWithoutFutureConstraint");
    }

    @Test
    public void testArrayTypes() {
        BRunUtil.invoke(result, "testArrayTypes");
    }

    @Test(enabled = false) // https://github.com/ballerina-platform/ballerina-lang/issues/28758
    public void testArrayTypesWithoutFutureConstraint() {
        BRunUtil.invoke(result, "testArrayTypesWithoutFutureConstraint");
    }

    @Test
    public void testRecordTypes() {
        BRunUtil.invoke(result, "testRecordTypes");
    }

    @Test(enabled = false) // https://github.com/ballerina-platform/ballerina-lang/issues/28758
    public void testRecordTypesWithoutFutureConstraint() {
        BRunUtil.invoke(result, "testRecordTypesWithoutFutureConstraint");
    }

    @Test
    public void testObjectTypes() {
        BRunUtil.invoke(result, "testObjectTypes");
    }

    @Test(enabled = false) // https://github.com/ballerina-platform/ballerina-lang/issues/28758
    public void testObjectTypesWithoutFutureConstraint() {
        BRunUtil.invoke(result, "testObjectTypesWithoutFutureConstraint");
    }

    @Test
    public void testCustomErrorFuture() {
        BRunUtil.invoke(result, "testCustomErrorFuture");
    }

    @Test(enabled = false) // https://github.com/ballerina-platform/ballerina-lang/issues/28758
    public void testCustomErrorFutureWithoutConstraint() {
        BRunUtil.invoke(result, "testCustomErrorFutureWithoutConstraint");
    }

    @Test
    public void testFutureTyping() {
        BRunUtil.invoke(result, "testFutureTyping");
    }

    @Test
    public void testNegatives() {
        CompileResult errors = BCompileUtil.compile("test-src/types/future/future_negative_compilation.bal");
        int index = 0;
        validateError(errors, index++, "incompatible types: expected 'future<Bar>', found 'future<Foo>'", 18, 22);
        validateError(errors, index++,
                      "incompatible types: expected 'future<(int|string|error)>', found 'future<(Foo|Bar)>'", 19, 35);
        validateError(errors, index++, "incompatible types: expected 'future<Foo>', found 'future<Bar>'", 21, 22);
        validateError(errors, index++,
                      "incompatible types: expected 'future<(int|string)>', found 'future<(int|string|error)>'",
                      22, 29);
        // https://github.com/ballerina-platform/ballerina-lang/issues/28758
        // validateError(errors, index++, "incompatible types: expected 'future<Bar>', found 'future<(any|error)>'",
        //              25, 22);
        Assert.assertEquals(errors.getErrorCount(), index);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}

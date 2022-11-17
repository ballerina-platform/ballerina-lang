/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.record;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;

/**
 * Test cases for record type inclusion.
 *
 * @since 2201.4.0
 */
public class RecordTypeInclusionTest {

    @Test(dataProvider = "cyclicInclusionViaFieldTests")
    public void testCyclicInclusionViaField(String testFunction) {
        CompileResult result = BCompileUtil.compile("test-src/record/test_record_cyclic_inclusion_via_field.bal");
        BRunUtil.invoke(result, testFunction);
    }

    @DataProvider
    public Object[][] cyclicInclusionViaFieldTests() {
        return new Object[][]{
                {"testCyclicInclusionViaFieldWithTypeReferenceTypes"},
                {"testCyclicInclusionViaFieldWithAnonymousTypes"},
                {"testRuntimeStringRepresentationForCyclicInclusionViaFieldWithAnonymousTypes"}
        };
    }

    @Test
    public void testCyclicInclusionViaFieldNegative() {
        CompileResult compileResult = BCompileUtil.compile(
                "test-src/record/test_record_cyclic_inclusion_via_field_negative.bal");
        int index = 0;
        validateError(compileResult, index++, "missing non-defaultable required record field 'x'", 26, 13);
        validateError(compileResult, index++, "incompatible types: expected 'Bar', found 'int'", 27, 11);
        validateError(compileResult, index++, "incompatible types: expected 'Bar', found 'int'", 29, 17);
        validateError(compileResult, index++, "incompatible types: expected 'Bar', found 'int'", 31, 21);
        validateError(compileResult, index++, "missing non-defaultable required record field 'innerError'", 42, 21);
        validateError(compileResult, index++, "incompatible types: expected 'record {| string code?; ... innerError; " +
                "anydata...; |}', found 'int'", 43, 20);
        validateError(compileResult, index++, "incompatible types: expected 'int', found 'record {| string code?; ..." +
                " innerError; anydata...; |}'", 46, 13);
        Assert.assertEquals(compileResult.getErrorCount(), index);
    }
}

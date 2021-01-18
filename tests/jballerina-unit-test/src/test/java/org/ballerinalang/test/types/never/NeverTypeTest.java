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
package org.ballerinalang.test.types.never;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for ballerina never type.
 */
public class NeverTypeTest {

    private CompileResult neverTypeTestResult;
    private CompileResult negativeCompileResult;

    @BeforeClass
    public void setup() {
        neverTypeTestResult = BCompileUtil.compile("test-src/types/never/never-type.bal");
        negativeCompileResult = BCompileUtil.compile("test-src/types/never/never-type-negative.bal");
    }

    @Test(description = "Test type of the function with 'never' return type")
    public void testTypeOfNeverReturnTypedFunction() {
        BRunUtil.invoke(neverTypeTestResult, "testTypeOfNeverReturnTypedFunction");
    }

    @Test(description = "Test calling function with 'never' return type")
    public void testNeverReturnTypedFunctionCall() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverReturnTypedFunctionCall");
    }

    @Test(description = "Test inclusive record type with 'never' typed field")
    public void testInclusiveRecordTypeWithNeverTypedField() {
        BRunUtil.invoke(neverTypeTestResult, "testInclusiveRecord");
    }

    @Test(description = "Test exclusive record type with 'never' typed field")
    public void testExclusiveRecordTypeWithNeverTypedField() {
        BRunUtil.invoke(neverTypeTestResult, "testExclusiveRecord");
    }

    @Test(description = "Test XML with 'never' type constraint")
    public void testXMLWithNeverTypeConstraint() {
        BRunUtil.invoke(neverTypeTestResult, "testXMLWithNeverType");
    }

    @Test(description = "Test union type with 'never' type: 1")
    public void testNeverWithUnionType1() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverWithUnionType1");
    }

    @Test(description = "Test union type with 'never' type: 2")
    public void testNeverWithUnionType2() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverWithUnionType1");
    }

    @Test(description = "Test union type with 'never' type: 3")
    public void testNeverWithUnionType3() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverWithUnionType3");
    }

    @Test(description = "Test table's key constraint with 'never' type")
    public void testNeverWithKeyLessTable() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverWithKeyLessTable");
    }

    @Test(description = "Test table key constraint with 'never' type")
    public void testNeverInUnionTypedKeyConstraints() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverInUnionTypedKeyConstraints");
    }

    @Test(description = "Test 'never' type as future type param")
    public void testNeverAsFutureTypeParam() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverAsFutureTypeParam");
    }

    @Test(description = "Test 'never' type as mapping type param")
    public void testNeverAsMappingTypeParam() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverAsMappingTypeParam");
    }

    @Test
    public void testNeverTypeNegative() {
        Assert.assertEquals(negativeCompileResult.getErrorCount(), 25);
        int i = 0;
        BAssertUtil.validateError(negativeCompileResult, i++,
                "cannot define a variable of type 'never'", 2, 5);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "cannot define a variable of type 'never'", 12, 5);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected '()', found 'never'", 16, 12);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'string'", 25, 12);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'string'", 31, 16);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found '()'", 36, 12);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found '()'", 41, 20);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "a required field cannot be of type 'never', define 'y' as an optional field instead",
                49, 5);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'string'", 58, 23);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'string'", 62, 23);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'int'", 76, 16);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found '()'", 81, 16);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'string'", 91, 16);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found '()'", 96, 16);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'int'", 105, 38);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'int'", 112, 14);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found '()'", 117, 14);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "table key specifier '[name]' does not match with key constraint type '[never]'", 129, 34);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "table key specifier mismatch with key constraint. expected: '1' fields but found '0'", 138, 37);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'xml:Text', found 'never'", 147, 19);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'xml<never>', found 'never'", 148, 20);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'xml<never>', found 'xml:Text'", 150, 25);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'string', found '(xml|xml:Text)'", 152, 18);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'string', found '(xml<never>|xml)'", 154, 18);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected '(int|float)', found 'xml<never>'", 156, 19);
    }

    @AfterClass
    public void tearDown() {
        neverTypeTestResult = null;
        negativeCompileResult = null;
    }
}

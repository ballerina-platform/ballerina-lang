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

import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test class for ballerina never type.
 */
public class NeverTypeTest {

    private CompileResult neverTypeTestResult;
    private CompileResult negativeCompileResult;
    private CompileResult runtimeResult;

    @BeforeClass
    public void setup() {
        neverTypeTestResult = BCompileUtil.compile("test-src/types/never/never-type.bal");
        negativeCompileResult = BCompileUtil.compile("test-src/types/never/never-type-negative.bal");
        runtimeResult = BCompileUtil.compile("test-src/types/never/never_type_runtime.bal");
    }


    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: Panic occured in function with never return.*")
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
        int i = 0;
        BAssertUtil.validateError(negativeCompileResult, i++,
                "cannot define a variable of type 'never' or equivalent to type 'never'", 2, 5);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "cannot define a variable of type 'never' or equivalent to type 'never'", 12, 5);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'string'", 21, 12);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'string'", 27, 16);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found '()'", 32, 12);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found '()'", 37, 20);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "cannot define a variable of type 'never' or equivalent to type 'never'", 54, 5);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'string'", 54, 23);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'string'", 58, 23);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "cannot define a variable of type 'never' or equivalent to type 'never'", 62, 5);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'int'", 72, 16);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found '()'", 77, 16);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'string'", 87, 16);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found '()'", 92, 16);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'int'", 101, 38);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found 'int'", 108, 14);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'never', found '()'", 113, 14);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "table key specifier '[name]' does not match with key constraint type '[never]'", 125, 34);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "table key specifier mismatch with key constraint. " +
                        "expected: '[(never|string)]' fields but key specifier is empty", 134, 37);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'xml<never>', found 'xml:Text'", 143, 21);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'string', found '(xml|xml:Text)'", 145, 17);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'string', found '(xml|xml:Text)'", 147, 17);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected '(int|float)', found 'xml<never>'", 149, 20);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected 'string', found '(string|xml:Text)'", 151, 18);
        BAssertUtil.validateError(negativeCompileResult, i++,
                "incompatible types: expected '(int|string)', found 'xml<never>'", 152, 21);
        BAssertUtil.validateError(negativeCompileResult, i++, "cannot define a variable of type " +
                        "'never' or equivalent to type 'never'", 156, 5);
        BAssertUtil.validateError(negativeCompileResult, i++, "cannot define a variable of type 'never' " +
                        "or equivalent to type 'never'", 159, 1);
        BAssertUtil.validateError(negativeCompileResult, i++, "cannot declare a constant with type 'never', " +
                "expected a subtype of 'anydata' that is not 'never'", 161, 7);
        BAssertUtil.validateError(negativeCompileResult, i++, "incompatible types: expected 'never', found '()'",
                161, 17);
        BAssertUtil.validateError(negativeCompileResult, i++, "cannot define a variable of type 'never' " +
                        "or equivalent to type 'never'", 164, 5);
        BAssertUtil.validateError(negativeCompileResult, i++, "cannot define a variable of type 'never' " +
                        "or equivalent to type 'never'", 165, 5);
        BAssertUtil.validateError(negativeCompileResult, i++, "a required parameter or a defaultable parameter " +
                        "cannot be of type 'never' or equivalent to type 'never'", 173, 16);
        BAssertUtil.validateError(negativeCompileResult, i++, "a required parameter or a defaultable parameter " +
                "cannot be of type 'never' or equivalent to type 'never'", 176, 16);
        BAssertUtil.validateError(negativeCompileResult, i++, "a required parameter or a defaultable parameter " +
                "cannot be of type 'never' or equivalent to type 'never'", 179, 16);
        BAssertUtil.validateError(negativeCompileResult, i++, "a required parameter or a defaultable parameter " +
                "cannot be of type 'never' or equivalent to type 'never'", 182, 25);
        BAssertUtil.validateError(negativeCompileResult, i++, "cannot call a remote method with return type 'never' " +
                        "or equivalent to type 'never'",
                187, 5);
        BAssertUtil.validateError(negativeCompileResult, i++, "cannot call a remote method with return type 'never' " +
                        "or equivalent to type 'never'",
                188, 5);
        BAssertUtil.validateError(negativeCompileResult, i++, "cannot call a remote method with return type 'never' " +
                        "or equivalent to type 'never'",
                189, 5);
        BAssertUtil.validateError(negativeCompileResult, i++, "cannot define a variable of type 'never' " +
                "or equivalent to type 'never'", 207, 5);
        BAssertUtil.validateError(negativeCompileResult, i++, "a required parameter or a defaultable parameter" +
                " cannot be of type 'never' or equivalent to type 'never'", 210, 48);
        BAssertUtil.validateError(negativeCompileResult, i++, "a required parameter or a defaultable parameter" +
                " cannot be of type 'never' or equivalent to type 'never'", 214, 48);
        BAssertUtil.validateError(negativeCompileResult, i++, "cannot define an object field of type 'never'" +
                " or equivalent to type 'never'", 214, 82);
        BAssertUtil.validateError(negativeCompileResult, i++, "cannot define a variable of type 'never' " +
                "or equivalent to type 'never'", 229, 5);
        BAssertUtil.validateError(negativeCompileResult, i++, "cannot define a variable of type 'never' " +
                "or equivalent to type 'never'", 230, 5);
        BAssertUtil.validateError(negativeCompileResult, i++, "incompatible types: expected " +
                "'record {| never x?; never y?; anydata...; |}', found 'record {| never x?; anydata...; |}'", 244, 39);
        BAssertUtil.validateError(negativeCompileResult, i++, "incompatible types: expected " +
                "'record {| never x?; anydata...; |}', found 'record {| anydata...; |}'", 247, 29);
        Assert.assertEquals(negativeCompileResult.getErrorCount(), i);
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: Bad Sad!!.*")
    public void testNeverWithCallStmt() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverWithCallStmt");
    }

    @Test(dataProvider = "dataToTestNeverWithExpressions", description = "Test never type with expressions")
    public void testNeverWithExpressions(String functionName) {
        BRunUtil.invoke(neverTypeTestResult, functionName);
    }

    @DataProvider
    public Object[] dataToTestNeverWithExpressions() {
        return new Object[]{
                "testNeverWithStartAction1",
                "testNeverWithStartAction2",
                "testNeverWithTrapExpr1",
                "testNeverWithTrapExpr2",
                "testValidNeverReturnFuncAssignment",
                "testValidNeverReturnFuncAssignment2",
                "testNeverWithAnydata",
                "testNeverInSubsequentInvocations",
                "testNeverInGroupedExpr"
        };
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: Bad Sad!!.*")
    public void testNeverWithMethodCallExpr() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverWithMethodCallExpr");
    }


    @Test(dataProvider = "dataToTestNeverTypeWithIterator", description = "Test never type with iterator")
    public void testNeverTypeWithIterator(String functionName) {
       BRunUtil.invoke(neverTypeTestResult, functionName);
    }

    @DataProvider
    public Object[] dataToTestNeverTypeWithIterator() {
        return new Object[]{
                "testNeverWithIterator1",
                "testNeverWithIterator2",
                "testNeverWithIterator3",
                "testNeverWithIterator4",
                "testNeverWithIterator5",
                "testNeverWithIterator6"
        };
    }

    @Test(dataProvider = "dataToTestNeverTypeWithForeach", description = "Test never type with foreach")
    public void testNeverTypeWithForeach(String functionName) {
        BRunUtil.invoke(neverTypeTestResult, functionName);
    }

    @DataProvider
    public Object[] dataToTestNeverTypeWithForeach() {
        return new Object[]{
                "testNeverWithForeach1",
                "testNeverWithForeach2",
                "testNeverWithForeach3",
                "testNeverWithForeach4",
                "testNeverWithForeach5",
                "testNeverWithForeach6",
                "testNeverWithForeach7",
                "testNeverWithForeach8"
        };
    }

    @Test(dataProvider = "dataToTestNeverTypeWithFromClauseInQueryExpr", description = "Test never type in from " +
            "clause in query expr")
    public void testNeverTypeWithFromClauseInQueryExpr(String functionName) {
        BRunUtil.invoke(neverTypeTestResult, functionName);
    }

    @DataProvider
    public Object[] dataToTestNeverTypeWithFromClauseInQueryExpr() {
        return new Object[]{
                "testNeverWithFromClauseInQueryExpr1",
                "testNeverWithFromClauseInQueryExpr2",
                "testNeverWithFromClauseInQueryExpr3",
                "testNeverWithFromClauseInQueryExpr4",
                "testNeverWithFromClauseInQueryExpr5"
        };
    }

    @Test(description = "Test never type in rest params and fields")
    public void testNeverWithRestParams() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverWithRestParamsAndFields");
    }


    @Test(description = "Test never type in remote method return type of service object")
    public void testNeverWithServiceObjFunc() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverWithServiceObjFunc");
    }

    @Test(description = "Test never sub typing")
    public void testNeverSubtyping() {
        BRunUtil.invoke(neverTypeTestResult, "testNeverSubtyping");
    }

    @Test(dataProvider = "dataToTestNeverRuntime", description = "Test never runtime")
    public void testNeverRuntime(String functionName) {
        BRunUtil.invoke(runtimeResult, functionName);
    }

    @DataProvider
    public Object[] dataToTestNeverRuntime() {
        return new Object[]{
                "testNeverRuntime1",
                "testNeverRuntime2",
                "testNeverRuntime3",
                "testNeverRuntime4",
                "testNeverRuntime5",
                "testNeverRuntime6",
                "testNeverRuntime7",
                "testNeverRuntime8",
                "testNeverRuntime9",
                "testNeverRuntime10",
                "testNeverRuntime11",
                "testNeverRuntime12",
                "testNeverWithAnyAndAnydataRuntime",
                "testNeverFieldTypeCheck"
        };
    }

    @Test
    public void testNeverTypeCodeAnalysisNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/never/never_code_analysis_negative.bal");
        int i = 0;
        BAssertUtil.validateError(compileResult, i++, "function with return type 'never' or equivalent to " +
                "type 'never' cannot implicitly return 'nil' by falling off the end of the function body", 17, 1);
        BAssertUtil.validateError(compileResult, i++, "expression of type 'never' or equivalent to " +
                "type 'never' not allowed here", 22, 11);
        BAssertUtil.validateError(compileResult, i++, "expression of type 'never' or equivalent to " +
                "type 'never' not allowed here", 30, 13);
        BAssertUtil.validateError(compileResult, i++, "expression of type 'never' or equivalent to " +
                "type 'never' not allowed here", 30, 19);
        BAssertUtil.validateError(compileResult, i++, "expression of type 'never' or equivalent to " +
                "type 'never' not allowed here", 31, 13);
        BAssertUtil.validateError(compileResult, i++, "expression of type 'never' or equivalent to " +
                "type 'never' not allowed here", 31, 24);
        BAssertUtil.validateError(compileResult, i++, "expression of type 'never' or equivalent to " +
                "type 'never' not allowed here", 36, 17);
        BAssertUtil.validateError(compileResult, i++, "expression of type 'never' or equivalent to " +
                "type 'never' not allowed here", 36, 30);
        BAssertUtil.validateError(compileResult, i++, "expression of type 'never' or equivalent to " +
                "type 'never' not allowed here", 44, 12);
        BAssertUtil.validateWarning(compileResult, i++, "unused variable 'x'", 48, 5);
        BAssertUtil.validateError(compileResult, i++, "expression of type 'never' or equivalent to " +
                "type 'never' not allowed here", 48, 16);
        BAssertUtil.validateError(compileResult, i++, "expression of type 'never' or equivalent to " +
                "type 'never' not allowed here", 48, 23);
        Assert.assertEquals(compileResult.getErrorCount(), i - 1);
        Assert.assertEquals(compileResult.getWarnCount(), 1);
    }

    @AfterClass
    public void tearDown() {
        neverTypeTestResult = null;
        negativeCompileResult = null;
        runtimeResult = null;
    }
}

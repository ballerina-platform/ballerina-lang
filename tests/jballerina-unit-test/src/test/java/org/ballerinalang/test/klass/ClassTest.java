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
package org.ballerinalang.test.klass;

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
 * Test cases for class in ballerina.
 * @since 2.0
 */
public class ClassTest {

    private CompileResult compileResult;
    private CompileResult distinctCompUnit;
    private CompileResult classDefCompileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/klass/simple_class.bal");
        distinctCompUnit = BCompileUtil.compile("test-src/klass/distinct-class-def.bal");
        classDefCompileResult = BCompileUtil.compile("test-src/klass/class_def_test.bal");
    }

    @Test
    public void testBasicStructAsObject() {
        BRunUtil.invoke(compileResult, "testSimpleObjectAsStruct");
    }

    @Test
    public void testBasicStructAsObjectObjectInitExprWithoutName() {
        BRunUtil.invoke(compileResult, "testSimpleObjectAsStructWithNew");
    }

    @Test
    public void testUsingClassValueAsRecordField() {
        BRunUtil.invoke(compileResult, "testUsingClassValueAsRecordField");
    }

    @Test
    public void testTypeRef() {
        BRunUtil.invoke(compileResult, "testTypeRefInClass");
    }

    @Test
    public void testSimpleDistinctClass() {
        BRunUtil.invoke(distinctCompUnit, "testDistinctAssignability");
    }

    @Test
    public void testRuntimeIsTypeCheckWithDistinctObjectTypes() {
        BRunUtil.invoke(distinctCompUnit, "testDistinctTypeIsType");
    }

    @Test(description = "Class definition negative")
    public void classDefNegative() {
        CompileResult negative = BCompileUtil.compile("test-src/klass/class_def_negative_test.bal");
        int i = 0;
        String expectedErrorMsgPrefix = "invalid cyclic type reference in ";
        BAssertUtil.validateError(negative, i++, expectedErrorMsgPrefix + "'[A, B, A]'", 17, 1);
        BAssertUtil.validateError(negative, i++, expectedErrorMsgPrefix + "'[A, B, C, A]'", 17, 1);
        BAssertUtil.validateError(negative, i++, expectedErrorMsgPrefix + "'[B, A, B]'", 22, 1);
        BAssertUtil.validateError(negative, i++, expectedErrorMsgPrefix + "'[B, C, A, B]'", 22, 1);
        BAssertUtil.validateError(negative, i++, expectedErrorMsgPrefix + "'[A, B, A]'", 28, 1);
        BAssertUtil.validateError(negative, i++, expectedErrorMsgPrefix + "'[C, A, B, C]'", 28, 1);
        BAssertUtil.validateError(negative, i++, "no implementation found for the method 'resource " +
                "function get name() returns (string)' of class 'ServiceClass'", 36, 1);
        BAssertUtil.validateError(negative, i++, "no implementation found for the method 'resource " +
                "function get name() returns (string)' of class 'ClientClass'", 44, 1);
        BAssertUtil.validateError(negative, i++, "no implementation found for the method 'resource " +
                "function get name() returns (string)' of class 'ClientClass2'", 52, 1);
        BAssertUtil.validateError(negative, i++, "no implementation found for the method 'resource " +
                "function get name() returns (string)' of class 'ClientClass3'", 56, 1);
        Assert.assertEquals(negative.getErrorCount(), i);
    }

    @Test(description = "Dataflow negative tests for class defn")
    public void classDefNegativeSemanticErrors() {
        CompileResult negative = BCompileUtil.compile("test-src/klass/class-def-negative2.bal");
        int i = 0;
        String missingRecordField = "missing non-defaultable required record field ";
        String missingRequiredParam = "missing required parameter '%s' in call to 'new()'";
        BAssertUtil.validateError(negative, i++, missingRecordField + "'p'", 27, 14);
        BAssertUtil.validateError(negative, i++, missingRecordField + "'s'", 27, 14);
        BAssertUtil.validateError(negative, i++, missingRecordField + "'p'", 32, 19);
        BAssertUtil.validateError(negative, i++, missingRecordField + "'s'", 32, 19);
        BAssertUtil.validateError(negative, i++, String.format(missingRequiredParam, "age"), 48, 17);
        BAssertUtil.validateError(negative, i++, String.format(missingRequiredParam, "month"), 48, 17);
        BAssertUtil.validateError(negative, i++, String.format(missingRequiredParam, "name"), 48, 17);
        BAssertUtil.validateError(negative, i++, String.format(missingRequiredParam, "year"), 48, 17);
        BAssertUtil.validateError(negative, i++, String.format(missingRequiredParam, "age"), 49, 14);
        BAssertUtil.validateError(negative, i++, String.format(missingRequiredParam, "month"), 49, 14);
        BAssertUtil.validateError(negative, i++, String.format(missingRequiredParam, "name"), 49, 14);
        BAssertUtil.validateError(negative, i++, String.format(missingRequiredParam, "year"), 49, 14);
        Assert.assertEquals(negative.getErrorCount(), i);
    }

    @Test(description = "Negative tests to check fields that have been initialized using other fields")
    public void classDefFieldsInitializedUsingAnotherField() {
        CompileResult negative = BCompileUtil.compile("test-src/klass/class_def_field_negative.bal");
        int i = 0;
        BAssertUtil.validateError(negative, i++, "undefined symbol 'a'", 19, 13);
        BAssertUtil.validateError(negative, i++, "undefined symbol 'a'", 27, 19);
        BAssertUtil.validateError(negative, i++, "undefined symbol 'name'", 36, 20);
        BAssertUtil.validateError(negative, i++, "undefined symbol 'name'", 44, 20);
        Assert.assertEquals(negative.getErrorCount(), i);
    }

    @Test(description = "Dataflow negative tests for class defn")
    public void classDefDataflowNegative() {
        CompileResult negative = BCompileUtil.compile("test-src/klass/class-dataflow-negative.bal");
        int i = 0;
        String uninitializedMessage = "variable '%s' is not initialized";
        BAssertUtil.validateError(negative, i++, String.format(uninitializedMessage, "k"), 11, 13);
        BAssertUtil.validateError(negative, i++, String.format(uninitializedMessage, "q"), 16, 13);
        BAssertUtil.validateError(negative, i++, String.format(uninitializedMessage, "q"), 17, 13);
        BAssertUtil.validateError(negative, i++, String.format(uninitializedMessage, "q"), 18, 13);
        Assert.assertEquals(negative.getErrorCount(), i);
    }

    @Test(dataProvider = "classDefTestFunctions")
    public void testClassDef(String functionName) {
        BRunUtil.invoke(classDefCompileResult, functionName);
    }

    @DataProvider(name = "classDefTestFunctions")
    public Object[] classDefTestFunctions() {
        return new String[]{
                "testUnionTypeInInitParameter",
                "testModuleLevelVariableAsFieldDefault"
        };
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
        distinctCompUnit = null;
    }
}

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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.jvm;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.ballerinalang.test.exceptions.BLangTestException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.ballerinalang.test.BAssertUtil.validateWarning;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for testing the object subtyping rules.
 *
 * @since 0.995.0
 */
public class ObjectSubtypingTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/ObjectSubtypingTestProject");
        compileResult = BCompileUtil.compile("test-src/jvm/objects_subtyping.bal");
    }

    @Test
    public void testAdditionalMethodsInSourceType() {
        Object result = BRunUtil.invoke(compileResult, "testAdditionalMethodsInSourceType");
        assertEquals(result.toString(), "{name:John Doe, age:25}");
    }

    @Test(expectedExceptions = BLangTestException.class,
            expectedExceptionsMessageRegExp = ".*incompatible types: 'Person1' cannot be cast to 'Employee1'.*")
    public void testCastingRuntimeError() {
        BRunUtil.invoke(compileResult, "testCastingRuntimeError");
    }

    @Test
    public void testSubtypingAPublicAbstractObject() {
        Object result = BRunUtil.invoke(compileResult, "testSubtypingAPublicAbstractObject");
        assertEquals(result.toString(), "Student1{John Doe, 25, Ballerina Academy}");
    }

    @Test
    public void testSubtypingAPublicAbsObjectInAnotherModule() {
        Object result = BRunUtil.invoke(compileResult, "testSubtypingAPublicAbsObjectInAnotherModule");
        assertEquals(result.toString(), "Student{Jane Doe, 22, BA}");
    }

    @Test
    public void testSubtypingAPublicObjectInAnotherModule() {
        Object result = BRunUtil.invoke(compileResult, "testSubtypingAPublicObjectInAnotherModule");
        assertEquals(result.toString(), "Student{Jane Doe, 22, BA, CS}");
    }

    @Test
    public void testSubtypingAnAbsObjectInSameModule() {
        Object result = BRunUtil.invoke(compileResult, "testSubtypingAnAbsObjectInSameModule");
        assertEquals(result.toString(), "Rocky walked 50 meters");
    }

    @Test(description = "Test object subtyping")
    public void testObjectAssignabilityBetweenNonClientAndClientObject() {
        BRunUtil.invoke(compileResult, "testObjectAssignabilityBetweenNonClientAndClientObject");
    }

    @Test
    public void testNegatives() {
        CompileResult result = BCompileUtil.compile("test-src/jvm/object_negatives.bal");
        String msgFormat = "incompatible types: expected '%s', found '%s'";
        int i = 0;
        validateError(result, i++, "private qualifier in object member descriptor", 21, 5);
        validateError(result, i++, "private qualifier in object member descriptor", 28, 5);
        assertEquals(result.getErrorCount(), i);
    }

    @Test
    public void testObjSubtypingSemanticsNegative() {
        CompileResult result = BCompileUtil.compile("test-src/jvm/object-subtype-semantics-negative.bal");
        int i = 0;
        validateError(result, i++, "incompatible types: expected 'myType', found 'testObj'", 22, 17);
        validateError(result, i++, "incompatible types: expected 'myType1', found 'testObj'", 23, 19);
        assertEquals(result.getErrorCount(), i);
    }

    @Test
    public void testObjSubtypingNegatives() {
        CompileResult result = BCompileUtil.compile("test-src/jvm/object-subtype-negative.bal");
        int i = 0;
        validateWarning(result, i++, "unused variable 'mt'", 22, 5);
        validateError(result, i++, "uninitialized field 'intField1'", 27, 5);
        validateError(result, i++, "uninitialized field 'intField2'", 28, 5);
        assertEquals(result.getDiagnostics().length, i);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}

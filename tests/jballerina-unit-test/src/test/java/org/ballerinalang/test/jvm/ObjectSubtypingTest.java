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

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.util.exceptions.BLangRuntimeException;
import org.ballerinalang.test.balo.BaloCreator;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static java.lang.String.format;
import static org.ballerinalang.test.util.BAssertUtil.validateError;
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
        BaloCreator.cleanCacheDirectories();
        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_project", "testorg", "subtyping");
        compileResult = BCompileUtil.compile("test-src/jvm/objects_subtyping.bal");
    }

    @Test
    public void testAdditionalMethodsInSourceType() {
        BValue[] result = BRunUtil.invoke(compileResult, "testAdditionalMethodsInSourceType");
        assertEquals(result[0].stringValue(), "{name:\"John Doe\", age:25}");
    }

    @Test(expectedExceptions = BLangRuntimeException.class,
          expectedExceptionsMessageRegExp = ".*incompatible types: 'Person1' cannot be cast to 'Employee1'.*")
    public void testCastingRuntimeError() {
        BRunUtil.invoke(compileResult, "testCastingRuntimeError");
    }

    @Test
    public void testSubtypingAPublicAbstractObject() {
        BValue[] result = BRunUtil.invoke(compileResult, "testSubtypingAPublicAbstractObject");
        assertEquals(result[0].stringValue(), "Student1{John Doe, 25, Ballerina Academy}");
    }

    @Test
    public void testSubtypingAPublicAbsObjectInAnotherModule() {
        BValue[] result = BRunUtil.invoke(compileResult, "testSubtypingAPublicAbsObjectInAnotherModule");
        assertEquals(result[0].stringValue(), "Student{Jane Doe, 22, BA}");
    }

    @Test
    public void testSubtypingAPublicObjectInAnotherModule() {
        BValue[] result = BRunUtil.invoke(compileResult, "testSubtypingAPublicObjectInAnotherModule");
        assertEquals(result[0].stringValue(), "Student{Jane Doe, 22, BA, CS}");
    }

    @Test
    public void testSubtypingAnAbsObjectInSameModule() {
        BValue[] result = BRunUtil.invoke(compileResult, "testSubtypingAnAbsObjectInSameModule");
        assertEquals(result[0].stringValue(), "Rocky walked 50 meters");
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
        validateError(result, i++, "'private' qualifier not allowed", 21, 13);
        validateError(result, i++, "'private' qualifier not allowed", 28, 13);
        validateError(result, i++, format(msgFormat, "ObjWithPvtField", "AnotherObjWithAPvtField"), 45, 26);
        validateError(result, i++, format(msgFormat, "ObjWithPvtMethod", "AnotherObjWithPvtMethod"), 46, 27);
        validateError(result, i++, format(msgFormat, "testorg/subtyping:1.0.0:ModuleLevelSubtypableObj", "Subtype1"),
                      65, 45);
        validateError(result, i++, format(msgFormat, "testorg/subtyping:1.0.0:ModuleLevelSubtypableObj2", "Subtype2"),
                      66, 46);
        assertEquals(result.getErrorCount(), i);
    }

    @Test(groups = { "disableOnOldParser" })
    public void testObjSubtypingSemanticsNegative() {
        CompileResult result = BCompileUtil.compile("test-src/jvm/object-subtype-semantics-negative.bal");
        int i = 0;
        validateError(result, i++,
                "incompatible types: expected '(object { int intField1; int intField2; }|record" +
                        " {| int i; anydata...; |}|4)', found 'testObj'", 22, 17);
        validateError(result, i++,
                "incompatible types: expected '(object { int intField1; int intField2; }|string" +
                        "|boolean|1)', found 'testObj'", 23, 19);
        assertEquals(result.getErrorCount(), i);
    }

    @Test
    public void testObjSubtypingNegatives() {
        CompileResult result = BCompileUtil.compile("test-src/jvm/object-subtype-negative.bal");
        int i = 0;
        validateError(result, i++, "uninitialized field 'intField1'", 27, 5);
        validateError(result, i++, "uninitialized field 'intField2'", 28, 5);
        assertEquals(result.getErrorCount(), i);
    }
}

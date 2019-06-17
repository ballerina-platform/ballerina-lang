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

import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

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
//        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_project", "testorg", "subtyping");
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

    // TODO: 5/10/19 Enable once imports are working on jBallerina
    @Test(enabled = false)
    public void testSubtypingAPublicAbsObjectInAnotherModule() {
        BValue[] result = BRunUtil.invoke(compileResult, "testSubtypingAPublicAbsObjectInAnotherModule");
        assertEquals(result[0].stringValue(), "Student{Jane Doe, 22, BA}");
    }

    // TODO: 5/10/19 Enable once imports are working on jBallerina
    @Test(enabled = false)
    public void testSubtypingAPublicObjectInAnotherModule() {
        BValue[] result = BRunUtil.invoke(compileResult, "testSubtypingAPublicObjectInAnotherModule");
        assertEquals(result[0].stringValue(), "Student{Jane Doe, 22, BA, CS}");
    }

    @Test
    public void testSubtypingAnAbsObjectInSameModule() {
        BValue[] result = BRunUtil.invoke(compileResult, "testSubtypingAnAbsObjectInSameModule");
        assertEquals(result[0].stringValue(), "Rocky walked 50 meters");
    }

    @Test
    public void testNegatives() {
        CompileResult result = BCompileUtil.compile("test-src/jvm/object_negatives.bal");
        int i = 0;
        assertEquals(result.getErrorCount(), 4);
        validateError(result, i++, "abstract object field: 'ssn' can not be declared as private", 21, 5);
        validateError(result, i++,
                      "interface function: 'test' of abstract object 'ObjWithPvtMethod' can not be declared as private",
                      28, 5);
        validateError(result, i++, "incompatible types: expected 'ObjWithPvtField', found 'AnotherObjWithAPvtField'",
                      45, 26);
        validateError(result, i, "incompatible types: expected 'ObjWithPvtMethod', found 'AnotherObjWithPvtMethod'", 46,
                      27);
        // TODO: 5/10/19 Add the rest of the cases once module importing is working 
    }
}

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
package org.ballerinalang.test.bala.object;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Test cases for user defined object types in ballerina.
 */
public class ObjectIncludeOverrideBalaTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
        result = BCompileUtil.compile("test-src/bala/test_bala/object/object_override_includes.bal");
    }

    @Test
    public void testSimpleObjectOverridingSimilarObject() {
        BRunUtil.invoke(result, "testSimpleObjectOverridingSimilarObject");
    }

    @Test
    public void testObjectOverrideInterfaceWithInterface() {
        BRunUtil.invoke(result, "testObjectOverrideInterfaceWithInterface");
    }

    @Test
    public void testObjectWithOverriddenFieldsAndMethods() {
        BRunUtil.invoke(result, "testObjectWithOverriddenFieldsAndMethods");
    }

    @Test
    public void testIsolationNegative() {
        CompileResult negativeRes =
                BCompileUtil.compile("test-src/bala/test_bala/object/test_object_type_inclusion_negative.bal");
        int index = 0;

        validateError(negativeRes, index++, "mismatched visibility qualifiers for field 'salary' " +
                        "with object type inclusion", 23, 5);
        validateError(negativeRes, index++, "mismatched function signatures: expected 'public function " +
                "getBonus(float ratio, int months) returns float', found 'function getBonus(float ratio, " +
                "int months) returns float'", 25, 5);
        validateError(negativeRes, index++, "mismatched visibility qualifiers for field 'salary' " +
                "with object type inclusion", 34, 5);
        validateError(negativeRes, index++, "mismatched function signatures: expected 'public function " +
                "getBonus(float ratio, int months) returns float', found 'private function " +
                "getBonus(float ratio, int months) returns float'", 36, 5);
        validateError(negativeRes, index++, "incompatible type reference 'foo:Employee4': " +
                "a referenced type across modules cannot have non-public fields or methods", 42, 6);
        validateError(negativeRes, index++, "incompatible type reference 'foo:Employee5': " +
                "a referenced type across modules cannot have non-public fields or methods", 48, 6);
        assertEquals(negativeRes.getErrorCount(), index);
    }
}

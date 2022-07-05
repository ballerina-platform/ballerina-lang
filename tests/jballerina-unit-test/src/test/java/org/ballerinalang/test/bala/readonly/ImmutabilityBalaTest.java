/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.bala.readonly;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Tests for immutability with the `readonly` type.
 *
 * @since 2.0.0
 */
public class ImmutabilityBalaTest {

    private CompileResult result;
    private CompileResult inherentlyImmutableResult;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project_selectively_immutable");
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project_immutable");
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project_records");
        result = BCompileUtil.compile("test-src/bala/test_bala/readonly/test_selectively_immutable_type.bal");
        inherentlyImmutableResult = BCompileUtil.compile(
                "test-src/bala/test_bala/readonly/test_intersection_with_inherently_immutable_type.bal");
    }

    @Test(dataProvider = "immutableTypesTestFunctions")
    public void testSelectivelyImmutableTypes(String functionName) {
        BRunUtil.invoke(result, functionName);
    }

    @DataProvider(name = "immutableTypesTestFunctions")
    public Object[] immutableTypesTestFunctions() {
        return new String[]{
                "testImmutableTypes",
                "testIterationWithImportedImmutableType",
                "testReadOnlyObjectIntersectionMethodParams"
        };
    }

    @Test
    public void testIntersectionOfInherentlyImmutableTypes() {
        BRunUtil.invoke(inherentlyImmutableResult, "testEnumIntersectionWithReadOnly");
    }

    @Test
    public void testImmutableTypesNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/bala/test_bala/readonly/test_selectively_immutable_type_negative.bal");
        int index = 0;

        // Assignment and initialization.
        validateError(result, index++, "incompatible types: expected '(testorg/selectively_immutable:1" +
                ":MixedRecord & readonly)', found 'testorg/selectively_immutable:1.0.0:MixedRecord'", 20, 38);
        validateError(result, index++, "incompatible types: expected 'map<(json & readonly)> & readonly', " +
                              "found 'map<json>'", 23, 31);
        validateError(result, index++, "incompatible types: expected '(testorg/selectively_immutable:1:Details & " +
                "readonly)', found 'testorg/selectively_immutable:1.0.0:Details'", 31, 18);
        validateError(result, index++,
                "incompatible types: expected 'testorg/selectively_immutable:1.0.0:ReadOnlyStudent', " +
                              "found 'testorg/selectively_immutable:1.0.0:Student'", 43, 29);
        validateError(result, index++, "incompatible types: expected '(ABAny & readonly)', found 'Obj'", 57, 26);

        // Updates.
        validateError(result, index++, "cannot update 'readonly' value of type 'testorg/selectively_immutable:1.0" +
                ".0:ReadOnlyStudent'", 62, 5);
        validateError(result, index++, "cannot update 'readonly' value of type 'testorg/selectively_immutable:1.0" +
                ".0:ReadOnlyStudent'", 66, 5);
        validateError(result, index++, "cannot update 'readonly' value of type '(testorg/selectively_immutable:1" +
                ":Details & readonly)'", 76, 5);
        validateError(result, index++, "cannot update 'readonly' value of type '(testorg/selectively_immutable:1" +
                ":Details & readonly)'", 77, 5);
        validateError(result, index++, "cannot update 'readonly' value of type 'testorg/selectively_immutable:1.0.0:" +
                "(testorg/selectively_immutable:1:Config & readonly)'", 82, 5);
        validateError(result, index++, "cannot update 'readonly' value of type '(testorg/selectively_immutable:1" +
                ":Config & readonly)'", 85, 5);
        validateError(result, index++, "cannot update 'readonly' value of type 'testorg/selectively_immutable:1.0.0" +
                ":MyConfig'", 88, 5);
        validateError(result, index++, "missing required parameter '' in call to 'utcToCivil()'", 92, 18);

        assertEquals(result.getErrorCount(), index);
    }

    @AfterClass
    public void tearDown() {
        result = null;
        inherentlyImmutableResult = null;
    }
}

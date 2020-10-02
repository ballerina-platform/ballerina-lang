/*
 *   Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.expressions.access;

import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Test cases for optional field access.
 *
 * @since 1.0
 */
public class OptionalFieldAccessTest {

    private CompileResult result;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/expressions/access/optional_field_access.bal");
        negativeResult = BCompileUtil.compile("test-src/expressions/access/optional_field_access_negative.bal");
    }

    @Test
    public void testNegativeCases() {
        Assert.assertEquals(negativeResult.getErrorCount(), 11);
        int i = 0;
        validateError(negativeResult, i++, "invalid operation: type 'Foo' does not support optional field access",
                      23, 19);
        validateError(negativeResult, i++, "invalid operation: type 'Foo?' does not support optional field access",
                      26, 21);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found 'int?'", 41, 14);
        validateError(negativeResult, i++, "invalid operation: type 'Employee' does not support optional field access" +
                " for field 'salary'", 42, 9);
        validateError(negativeResult, i++, "incompatible types: expected 'json?', found '(json|error)'", 47, 16);
        validateError(negativeResult, i++, "incompatible types: expected 'int', found '(int|float)?'", 53, 14);
        validateError(negativeResult, i++, "invalid operation: type 'map<int>' does not support optional field " +
                "access", 58, 14);
        validateError(negativeResult, i++, "invalid operation: type 'map<string>?' does not support optional field " +
                "access", 61, 19);
        validateError(negativeResult, i++, "incompatible types: expected 'json', found '(json|error)'", 71, 15);
        validateError(negativeResult, i++, "invalid operation: type 'Qux' does not support optional field access", 87
                , 9);
        validateError(negativeResult, i, "invalid operation: type 'string[]' does not support optional field access",
                      91, 9);
    }

    @Test(dataProvider = "recordOptionalFieldAccessFunctions")
    public void testRecordOptionalFieldAccess(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @DataProvider(name = "recordOptionalFieldAccessFunctions")
    public Object[][] recordOptionalFieldAccessFunctions() {
        return new Object[][] {
            { "testOptionalFieldAccessOnRequiredRecordField" },
            { "testOptionalFieldAccessOnRequiredRecordFieldInRecordUnion" },
            { "testOptionalFieldAccessOnRequiredRecordFieldInNillableUnion" },
            { "testOptionalFieldAccessOnOptionalRecordField1" },
            { "testOptionalFieldAccessOnOptionalRecordField2" },
            { "testOptionalFieldAccessOnOptionalRecordFieldInRecordUnion1" },
            { "testOptionalFieldAccessOnOptionalRecordFieldInRecordUnion2" },
            { "testOptionalFieldAccessOnOptionalRecordFieldInNillableRecordUnion1" },
            { "testOptionalFieldAccessOnOptionalRecordFieldInNillableRecordUnion2" }
        };
    }

    @Test(dataProvider = "laxOptionalFieldAccessFunctions", enabled = false)
    public void testLaxOptionalFieldAccess(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @DataProvider(name = "laxOptionalFieldAccessFunctions")
    public Object[][] laxOptionalFieldAccessFunctions() {
        return new Object[][] {
                { "testOptionalFieldAccessNilLiftingOnJson1" },
                { "testOptionalFieldAccessNilLiftingOnJson2" },
                { "testOptionalFieldAccessNilLiftingOnMapJson" },
                { "testOptionalFieldAccessErrorOnNonMappingJson" },
                { "testOptionalFieldAccessErrorLiftingOnNonMappingJson" },
                { "testOptionalFieldAccessErrorLiftingOnMapJson" },
                { "testOptionalFieldAccessOnJsonMappingPositive" },
                { "testOptionalFieldAccessOnMapJsonPositive" },
                { "testOptionalFieldAccessNilReturnOnMissingKey" },
                { "testOptionalFieldAccessNilReturnOnMissingKeyInJsonMap" },
                { "testOptionalFieldAccessOnLaxUnionPositive" },
                { "testOptionalFieldAccessNilReturnOnLaxUnion" },
                { "testOptionalFieldAccessNilLiftingOnLaxUnion" },
                { "testOptionalFieldAccessErrorReturnOnLaxUnion" },
                { "testOptionalFieldAccessErrorLiftingOnLaxUnion" }
        };
    }

    @Test(dataProvider = "optionalFieldAccessOnInvocationFunctions")
    public void testOptionalFieldAccessOnInvocation(String function) {
        BValue[] returns = BRunUtil.invoke(result, function);
        Assert.assertTrue(((BBoolean) returns[0]).booleanValue());
    }

    @DataProvider(name = "optionalFieldAccessOnInvocationFunctions")
    public Object[][] optionalFieldAccessOnInvocationFunctions() {
        return new Object[][] {
                { "testOptionalFieldAccessForRequiredFieldOnInvocation" },
                { "testOptionalFieldAccessOnNillableTypeInvocation" },
                { "testNilLiftingWithOptionalFieldAccessOnNillableTypeInvocation" },
                { "testJsonOptionalFieldAccessOnInvocation" }
        };
    }

    @Test
    public void testOptionalFieldAccessInUnionType() {
        BRunUtil.invoke(result, "testOptionalFieldAccessInUnionType");
    }
}

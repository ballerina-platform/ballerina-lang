/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.json;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for constraining json types with structs.
 */
public class ConstrainedJSONTest {

    private CompileResult compileResult;
    private CompileResult negativeResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/jsontype/constrained-json.bal");
        negativeResult = BCompileUtil.compile("test-src/types/jsontype/constrained-json-negative.bal");
    }

    @Test(description = "Test basic json struct constraint")
    public void testConstrainedJSONNegative() {
        Assert.assertEquals(negativeResult.getErrorCount(), 9);
        
        // testStructConstraintInInitializationInvalid
        BAssertUtil.validateError(negativeResult, 0, "undefined field 'firstName' in struct 'Person'", 15, 23);
        
        // testInvalidStructFieldConstraintLhs
        BAssertUtil.validateError(negativeResult, 1, "undefined field 'firstName' in struct 'Person'", 21, 5);
        
        // tesInvalidStructFieldConstraintRhs
        BAssertUtil.validateError(negativeResult, 2, "undefined field 'firstName' in struct 'Person'", 28, 17);
        
        // TODO: testInvalidStructConstraintInPkg
        
        // testConstraintJSONIndexing
        BAssertUtil.validateError(negativeResult, 3, "undefined field 'bus' in struct 'Student'", 34, 12);
        
        // tesInvalidNestedStructFieldAccess
        BAssertUtil.validateError(negativeResult, 4, "undefined field 'foo' in struct 'PhoneNumber'", 58, 14);
        
        // tesInvalidNestedStructFieldIndexAccess
        BAssertUtil.validateError(negativeResult, 5, "undefined field 'bar' in struct 'PhoneNumber'", 63, 14);
        
        // tesInitializationWithInvalidNestedStruct
        BAssertUtil.validateError(negativeResult, 6, "undefined field 'foo' in struct 'PhoneNumber'", 67, 107);
        
        BAssertUtil.validateError(negativeResult, 7,
                "incompatible types: 'json<Person>[]' cannot be convert to 'json<Student>[]'", 72, 14);

        BAssertUtil.validateError(negativeResult, 8, "incompatible types: expected 'json', found 'blob[]'", 78, 14);
    }

    // disabled due to json to string conversion fails
    @Test(description = "Test basic json struct constraint")
    public void testStructConstraint() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonStructConstraint");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertTrue((((BJSON) returns[0]).value()).isString());
        Assert.assertEquals(returns[0].stringValue(), "John Doe");

        Assert.assertTrue(returns[1] instanceof BJSON);
        Assert.assertTrue((((BJSON) returns[1]).value()).isLong());
        Assert.assertEquals((((BJSON) returns[1]).value()).longValue(), 30);

        Assert.assertTrue(returns[2] instanceof BJSON);
        Assert.assertTrue((((BJSON) returns[2]).value()).isString());
        Assert.assertEquals(returns[2].stringValue(), "London");

        Assert.assertTrue(returns[3] instanceof BString);
        Assert.assertEquals(returns[3].stringValue(), "John Doe");

        Assert.assertTrue(returns[4] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[4]).intValue(), 30);

        Assert.assertTrue(returns[5] instanceof BString);
        Assert.assertEquals((returns[5]).stringValue(), "London");
    }

    @Test(description = "Test basic json struct constraint during json initialization")
    public void testStructConstraintInInitialization() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJsonInitializationWithStructConstraint");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertTrue((((BJSON) returns[0]).value()).isString());
        Assert.assertEquals(returns[0].stringValue(), "John Doe");

        Assert.assertTrue(returns[1] instanceof BJSON);
        Assert.assertTrue((((BJSON) returns[1]).value()).isLong());
        Assert.assertEquals((((BJSON) returns[1]).value()).longValue(), 30);

        Assert.assertTrue(returns[2] instanceof BJSON);
        Assert.assertTrue((((BJSON) returns[2]).value()).isString());
        Assert.assertEquals(returns[2].stringValue(), "London");
    }

    @Test(description = "Test json imported struct constraint")
    public void testStructConstraintInPkg() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src/types/jsontype/pkg", "main");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 0);
    }

    @Test(description = "Test invalid json imported struct constraint")
    public void testInvalidStructConstraintInPkg() {
        CompileResult compileResult = BCompileUtil.compile(this, "test-src/types/jsontype/pkginvalid", "main");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 2);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(),
                            "undefined field 'firstname' in struct 'structdef:Person'");
        Assert.assertEquals(compileResult.getDiagnostics()[1].getMessage(),
                            "undefined field 'firstname' in struct 'structdef:Person'");
    }

    @Test(description = "Test trivial JSON return.")
    public void testGetPlainJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetPlainJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"firstName\":\"John Doe\",\"age\":30,\"address\":\"London\"}");
    }

    @Test(description = "Test trivial Constraint JSON return.")
    public void testGetConstraintJson() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testGetConstraintJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"John Doe\",\"age\":30,\"address\":\"London\"}");
    }

    @Test(description = "Test casting constraint JSON to an unconstrained JSON.")
    public void testConstraintJSONToJSONCast() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstraintJSONToJSONCast");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"John Doe\",\"age\":30,\"address\":\"London\"}");
    }

    @Test
    public void testContrainingWithNestedStructs() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testContrainingWithNestedStructs");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "{\"first_name\":\"John\",\"last_name\":\"Doe\",\"age\":30," +
                "\"address\":{\"phoneNumber\":{\"number\":\"1234\"},\"street\":\"York St\"}}");

        Assert.assertTrue(returns[1] instanceof BJSON);
        Assert.assertEquals(returns[1].stringValue(), "1234");

        Assert.assertTrue(returns[1] instanceof BJSON);
        Assert.assertEquals(returns[2].stringValue(), "1234");
    }

    @Test(description = "Test JSON to Constaint JSON unsafe cast.")
    public void testJSONToConstraintJsonUnsafeCast() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJSONToConstraintJsonUnsafeCast");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "'json' cannot be cast to 'json<Person>'");
    }

    @Test(description = "Test JSON to Constaint unsafe cast positive.")
    public void testJSONToConstraintJsonUnsafeCastPositive() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJSONToConstraintJsonUnsafeCastPositive");
        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertEquals(returns[0].stringValue(), "John Doe");
        Assert.assertTrue(returns[1] instanceof BJSON);
        Assert.assertEquals(returns[1].stringValue(), "30");
        Assert.assertTrue(returns[2] instanceof BJSON);
        Assert.assertEquals(returns[2].stringValue(), "London");
    }

    @Test(description = "Test Constaint JSON to Constaint JSON safe cast.")
    public void testConstraintJSONToConstraintJsonCast() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstraintJSONToConstraintJsonCast");
        Assert.assertNotNull(returns[0]);

        // TODO: in the resulting json, "class" field should not be visible.
        // This test case should be updated once the https://github.com/ballerinalang/ballerina/issues/4252
        Assert.assertEquals(returns[0].stringValue(),
                "{\"name\":\"John Doe\",\"age\":30,\"address\":\"Colombo\",\"class\":\"5\"}");
    }

    @Test(description = "Test Constaint JSON to Constaint JSON unsafe cast postive scenario.")
    public void testConstraintJSONToConstraintJsonUnsafePositiveCast() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstraintJSONToConstraintJsonUnsafePositiveCast");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(),
                "{\"name\":\"John Doe\",\"age\":30,\"address\":\"Colombo\",\"class\":\"5\"}");
    }

    @Test(description = "Test Constaint JSON to Constaint JSON unsafe cast negative scenario.")
    public void testConstraintJSONToConstraintJsonUnsafeNegativeCast() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstraintJSONToConstraintJsonUnsafeNegativeCast");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0),
                "'json<Employee>' cannot be cast to 'json<Student>'");
    }

    @Test
    public void testJSONArrayToConstraintJsonArrayCastPositive() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJSONArrayToConstraintJsonArrayCastPositive");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"name\":\"John Doe\",\"age\":30,\"address\":\"Colombo\",\"class\":\"5\"}]");
    }

    @Test
    public void testJSONArrayToConstraintJsonArrayCastNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJSONArrayToConstraintJsonArrayCastNegative");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "'json[]' cannot be cast to 'json<Student>[]'");
    }

    @Test
    public void testJSONArrayToCJsonArrayCast() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJSONArrayToCJsonArrayCast");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(),
                "[{\"name\":\"John Doe\",\"age\":30,\"address\":\"London\",\"class\":\"B\"}]");
    }

    @Test
    public void testJSONArrayToCJsonArrayCastNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testJSONArrayToCJsonArrayCastNegative");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "'json[]' cannot be cast to 'json<Student>[]'");
    }

    @Test
    public void testCJSONArrayToJsonAssignment() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testCJSONArrayToJsonAssignment");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "[{\"name\":\"John Doe\",\"age\":30,\"address\":\"London\"}," +
                "{\"name\":\"John Doe\",\"age\":40,\"address\":\"London\"}]");
    }

    @Test
    public void testMixedTypeJSONArrayToCJsonArrayCastNegative() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testMixedTypeJSONArrayToCJsonArrayCastNegative");
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(((BStruct) returns[0]).getStringField(0), "'json[]' cannot be cast to 'json<Student>[]'");
    }

    @Test
    public void testConstrainedJsonWithFunctionToString() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstrainedJsonWithFunctions");
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"John Doe\",\"age\":30,\"address\":\"London\"}");
    }

    @Test
    public void testConstrainedJsonWithFunctionGetKeys() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testConstrainedJsonWithFunctionGetKeys");
        Assert.assertEquals(((BStringArray) returns[0]).get(0), "name");
        Assert.assertEquals(((BStringArray) returns[0]).get(1), "age");
        Assert.assertEquals(((BStringArray) returns[0]).get(2), "address");
    }
}

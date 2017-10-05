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

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for constraining json types with structs.
 */
public class ConstrainedJSONTest {

    private CompileResult compileResult;
    private CompileResult negativeResult;
    private static final double DELTA = 0.01;

    @BeforeClass
    public void setup() {
        compileResult = BTestUtils.compile("test-src/types/jsontype/constrained-json.bal");
        negativeResult = BTestUtils.compile("test-src/types/jsontype/constrained-json-negative.bal");
    }

    @Test(description = "Test basic json struct constraint")
    public void testConstrainedJSONNegative() {
        // testStructConstraintInInitializationInvalid
        BTestUtils.validateError(negativeResult, 0, "undefined field 'firstName' in struct 'Person'", 15, 23);
        
        // testInvalidStructFieldConstraintLhs
        BTestUtils.validateError(negativeResult, 1, "undefined field 'firstName' in struct 'Person'", 21, 5);
        
        // tesInvalidStructFieldConstraintRhs
        BTestUtils.validateError(negativeResult, 2, "undefined field 'firstName' in struct 'Person'", 28, 17);
        
        // TODO: testInvalidStructConstraintInPkg
        
        // testConstraintJSONIndexing
        BTestUtils.validateError(negativeResult, 3, "undefined field 'bus' in struct 'Student'", 34, 12);
    }

    @Test(description = "Test basic json struct constraint")
    public void testStructConstraint() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testJsonStructConstraint");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertTrue((((BJSON) returns[0]).value()).isTextual());
        Assert.assertEquals(returns[0].stringValue(), "John Doe");

        Assert.assertTrue(returns[1] instanceof BJSON);
        Assert.assertTrue((((BJSON) returns[1]).value()).isInt());
        Assert.assertEquals((((BJSON) returns[1]).value()).asInt(), 30);

        Assert.assertTrue(returns[2] instanceof BJSON);
        Assert.assertTrue((((BJSON) returns[2]).value()).isTextual());
        Assert.assertEquals(returns[2].stringValue(), "London");

        // Todo - Fix incorrect return value issue
        Assert.assertTrue(returns[3] instanceof BString);
        Assert.assertEquals(returns[3].stringValue(), "John Doe");

        Assert.assertTrue(returns[4] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[4]).intValue(), 30);

        Assert.assertTrue(returns[5] instanceof BString);
        Assert.assertEquals((returns[5]).stringValue(), "London");
    }

    @Test(description = "Test basic json struct constraint during json initialization")
    public void testStructConstraintInInitialization() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testJsonInitializationWithStructConstraint");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertTrue((((BJSON) returns[0]).value()).isTextual());
        Assert.assertEquals(returns[0].stringValue(), "John Doe");

        Assert.assertTrue(returns[1] instanceof BJSON);
        Assert.assertTrue((((BJSON) returns[1]).value()).isInt());
        Assert.assertEquals((((BJSON) returns[1]).value()).asInt(), 30);

        Assert.assertTrue(returns[2] instanceof BJSON);
        Assert.assertTrue((((BJSON) returns[2]).value()).isTextual());
        Assert.assertEquals(returns[2].stringValue(), "London");
    }

    @Test(description = "Test json imported struct constraint", enabled = false)
    public void testStructConstraintInPkg() {
        CompileResult compileResult = BTestUtils.compile("test-src/types/jsontype/pkg", "main");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 0);
    }

    @Test(description = "Test invalid json imported struct constraint")
    public void testInvalidStructConstraintInPkg() {
        CompileResult compileResult = BTestUtils.compile("test-src/types/jsontype/pkginvalid", "main");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 2);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(),
                            "undefined field 'firstname' in struct 'structdef:Person'");
        Assert.assertEquals(compileResult.getDiagnostics()[1].getMessage(),
                            "undefined field 'firstname' in struct 'structdef:Person'");
    }

    @Test(description = "Test trivial JSON return.")
    public void testGetPlainJson() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testGetPlainJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
    }

    @Test(description = "Test trivial Constraint JSON return.")
    public void testGetConstraintJson() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testGetConstraintJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
    }

    @Test(description = "Test JSON to Constaint JSON unsafe cast.", enabled = false)
    public void testJSONToConstraintJsonUnsafeCast() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testJSONToConstraintJsonUnsafeCast");
        Assert.assertNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(((BStruct) returns[1]).getStringField(0), "'json' cannot be cast to 'json<Person>'");
    }

    @Test(description = "Test JSON to Constaint unsafe cast positive.", enabled = false)
    public void testJSONToConstraintJsonUnsafeCastPositive() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testJSONToConstraintJsonUnsafeCastPositive");
        Assert.assertTrue(returns[0] instanceof BJSON);
    }

    @Test(description = "Test Constaint JSON to Constaint JSON Assignment.", enabled = false)
    public void testConstraintJSONToConstraintJsonAssignment() {
        BValue[] returns = BTestUtils.invoke(compileResult, "testConstraintJSONToConstraintJsonAssignment");
        Assert.assertNotNull(returns[0]);
    }
    
    /*
        TODO: Add the below test cases once the constrained-json to un-constrained-json cast is implemented

        function testJSONToConstraintJsonUnsafeCast() (json, TypeCastError) {
            json<Person> j;
            TypeCastError err;
            j,err = (json<Person>)getPlainJson();
            return j,err;
        }
        
        function testJSONToConstraintJsonUnsafeCastPositive() (json) {
            json<Person> j;
            j,_ = (json<Person>)getPersonEquivalentPlainJson();
            return j;
        }
        
        function testConstraintJSONToConstraintJsonAssignment() (json) {
            json<Person> j = (json<Person>)getStudent();
            return j;
        }
     */
     
}

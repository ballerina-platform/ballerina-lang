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
package org.ballerinalang.model.json;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for constraining json types with structs.
 */
public class JSONStructConstraintTest {

    @Test(description = "Test basic json struct constraint")
    public void testStructConstraint() {
        BValue[] returns = BLangFunctions.invokeNew(BTestUtils.getProgramFile(
                "lang/jsontype/json-struct-constraint.bal"),
                "testJsonStructConstraint");

        Assert.assertTrue(returns[0] instanceof BJSON);
        Assert.assertTrue((((BJSON) returns[0]).value()).isTextual());
        Assert.assertEquals(returns[0].stringValue(), "John Doe");

        Assert.assertTrue(returns[1] instanceof BJSON);
        Assert.assertTrue((((BJSON) returns[1]).value()).isInt());
        Assert.assertEquals((((BJSON) returns[1]).value()).asInt(), 30);

        Assert.assertTrue(returns[2] instanceof BJSON);
        Assert.assertTrue((((BJSON) returns[2]).value()).isTextual());
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
        BValue[] returns = BLangFunctions.invokeNew(BTestUtils.getProgramFile(
                "lang/jsontype/json-struct-constraint.bal"),
                "testJsonInitializationWithStructConstraint");

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

    @Test(description = "Test invalid json imported struct constraint",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp =
                    "json-struct-constraint-initialize-invalid.bal:8: unknown field 'firstName' in json with struct " +
                            "constraint 'Person'")
    public void testStructConstraintInInitializationInvalid() {
        BLangFunctions.invokeNew(BTestUtils.getProgramFile(
                "lang/jsontype/json-struct-constraint-initialize-invalid.bal"),
                "testJsonInitializationWithStructConstraintInvalid");
    }

    @Test(description = "Test invalid field accessing in lhs as per struct constraint in json",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp =
                    "json-struct-constraint-invalid-lhs.bal:9: unknown field 'firstName' in json with "
                            + "struct constraint 'Person'")
    public void testInvalidStructFieldConstraintLhs() {
        BLangFunctions.invokeNew(BTestUtils.getProgramFile("lang/jsontype/json-struct-constraint-invalid-lhs.bal"),
                "testJsonStructConstraintInvalid");
    }

    @Test(description = "Test invalid field accessing in rhs as per struct constraint in json",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp =
                    "json-struct-constraint-invalid-rhs.bal:10: unknown field 'firstName' in json with "
                            + "struct constraint 'Person'")
    public void tesInvalidStructFieldConstraintRhs() {
        BLangFunctions.invokeNew(BTestUtils.getProgramFile("lang/jsontype/json-struct-constraint-invalid-rhs.bal"),
                "testJsonStructConstraintInvalid");
    }

    @Test(description = "Test json imported struct constraint")
    public void testStructConstraintInPkg() {
        BLangFunctions.invokeNew(BTestUtils.getProgramFile("lang/jsontype/pkg/main"), "lang.jsontype.pkg.main",
                "testJsonStructConstraint");
    }

    @Test(description = "Test invalid json imported struct constraint",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp =
                    "lang/jsontype/pkginvalid/main/json-struct-constraint.bal:7: unknown field 'firstname' "
                            + "in json with struct constraint 'Person'")
    public void testInvalidStructConstraintInPkg() {
        BLangFunctions.invokeNew(BTestUtils.getProgramFile("lang/jsontype/pkginvalid/main"),
                "lang.jsontype.pkginvalid.main", "testJsonStructConstraintInvalid");
    }

    @Test(description = "Test trivial JSON return.")
    public void testGetPlainJson() {
        BValue[] returns = BLangFunctions.invokeNew(BTestUtils
                        .getProgramFile("lang/jsontype/json-struct-constraint-function-returns.bal"),
                "testGetPlainJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
    }

    @Test(description = "Test trivial Constraint JSON return.")
    public void testGetConstraintJson() {
        BValue[] returns = BLangFunctions.invokeNew(BTestUtils
                        .getProgramFile("lang/jsontype/json-struct-constraint-function-returns.bal"),
                "testGetConstraintJson");
        Assert.assertTrue(returns[0] instanceof BJSON);
    }

    @Test(description = "Test JSON to Constaint JSON unsafe cast.")
    public void testJSONToConstraintJsonUnsafeCast() {
        BValue[] returns = BLangFunctions.invokeNew(BTestUtils
                        .getProgramFile("lang/jsontype/json-struct-constraint-function-returns.bal"),
                "testJSONToConstraintJsonUnsafeCast");
        Assert.assertNull(returns[0]);
        Assert.assertNotNull(returns[1]);
        Assert.assertEquals(((BStruct) returns[1]).getStringField(0), "'json' cannot be cast to 'json<Person>'");
    }

    @Test(description = "Test JSON to Constaint unsafe cast positive.")
    public void testJSONToConstraintJsonUnsafeCastPositive() {
        BValue[] returns = BLangFunctions.invokeNew(BTestUtils
                        .getProgramFile("lang/jsontype/json-struct-constraint-function-returns.bal"),
                "testJSONToConstraintJsonUnsafeCastPositive");
        Assert.assertTrue(returns[0] instanceof BJSON);
    }


    @Test(description = "Test Constaint JSON to Constaint JSON Assignment.")
    public void testConstraintJSONToConstraintJsonAssignment() {
        BValue[] returns = BLangFunctions.invokeNew(BTestUtils
                        .getProgramFile("lang/jsontype/json-struct-constraint-function-returns.bal"),
                "testConstraintJSONToConstraintJsonAssignment");
        Assert.assertNotNull(returns[0]);
    }

    @Test(description = "Test Constaint JSON indexing.",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp =
                    ".*invalid operation: type 'json<Student>' does not support indexing")
    public void testConstraintJSONIndexing() {
       BTestUtils.getProgramFile("lang/jsontype/json-struct-contraint-indexing-negative.bal");

    }



}

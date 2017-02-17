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
package org.ballerinalang.model.structs;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.model.BLangProgram;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for user defined struct types in ballerina
 */
public class StructTest {

    private BLangProgram bLangProgram;
    
    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("lang/structs/struct.bal");
    }
    
    @Test(description = "Test Basic struct operations")
    public void testBasicStruct() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testCreateStruct");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Jack");

        Assert.assertTrue(returns[1] instanceof BMap);
        BMap<BString, ?> adrsMap = ((BMap) returns[1]);
        Assert.assertEquals(adrsMap.get(new BString("country")), new BString("USA"));
        Assert.assertEquals(adrsMap.get(new BString("state")), new BString("CA"));

        Assert.assertTrue(returns[2] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 25);
    }

    @Test(description = "Test using expressions as index for struct arrays")
    public void testExpressionAsIndex() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testExpressionAsIndex");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Jane");
    }
    
/*    @Test(description = "Test struct operations inside a connector")
    public void testStructInConnector() {
        BValue[] returns = Functions.invoke(bLangProgram, "testAction1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Jack");
    }*/
    
    @Test(description = "Test using structs inside structs")
    public void testStructOfStructs() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testStructOfStruct");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "USA");
    }
    
    @Test(description = "Test returning fields of a struct")
    public void testReturnStructAttributes() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testReturnStructAttributes");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "emily");
    }
    
    @Test(description = "Test using struct expression as a index in another struct expression")
    public void testStructExpressionAsIndex() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testStructExpressionAsIndex");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "emily");
    }
    
    /*
     *  Negative tests
     */
    
    @Test(description = "Test accessing an field of a noninitialized struct",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "field 'employees\\[0\\]' is null")
    public void testGetNonInitField() {
        BLangFunctions.invoke(bLangProgram, "testGetNonInitAttribute");
    }
    
    @Test(description = "Test accessing an arrays field of a noninitialized struct",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "arrays index out of range: Index: 0, Size: 0")
    public void testGetNonInitArrayField() {
        BLangFunctions.invoke(bLangProgram, "testGetNonInitArrayAttribute");
    }
    
    @Test(description = "Test accessing the field of a noninitialized struct",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "field 'dpt' is null")
    public void testGetNonInitLastField() {
        BLangFunctions.invoke(bLangProgram, "testGetNonInitLastAttribute");
    }
    
    @Test(description = "Test setting an field of a noninitialized child struct",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "field 'family' is null")
    public void testSetNonInitField() {
        BLangFunctions.invoke(bLangProgram, "testSetFieldOfNonInitChildStruct");
    }
    
    @Test(description = "Test setting the field of a noninitialized root struct",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "field 'dpt' is null")
    public void testSetNonInitLastField() {
        BLangFunctions.invoke(bLangProgram, "testSetFieldOfNonInitStruct");
    }
    
    @Test(description = "Test defining structs with duplicate name",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "duplicate-structs.bal:7: redeclared symbol 'Department'")
    public void testDuplicateStructDefinitions() {
        BTestUtils.parseBalFile("lang/structs/duplicate-structs.bal");
    }
    
    @Test(description = "Test defining structs with duplicate fields",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "duplicate-fields.bal:4: redeclared symbol 'id'")
    public void testStructWithDuplicateFields() {
        BTestUtils.parseBalFile("lang/structs/duplicate-fields.bal");
    }
    
    @Test(description = "Test initializing an undeclraed structs",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "undeclared-struct-init.bal:2: undefined type 'Department'")
    public void testUndeclaredStructInit() {
        BTestUtils.parseBalFile("lang/structs/undeclared-struct-init.bal");
    }
    
    @Test(description = "Test accessing an undeclared struct",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "undeclared-struct-access.bal:4: struct 'dpt1' not found")
    public void testUndeclaredStructAccess() {
        BTestUtils.parseBalFile("lang/structs/undeclared-struct-access.bal");
    }
    
    @Test(description = "Test accessing an undeclared field of a struct",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "undeclared-attribute-access.bal:5: unknown field 'id' in struct "
                    + "'Department'")
    public void testUndeclaredFieldAccess() {
        BTestUtils.parseBalFile("lang/structs/undeclared-attribute-access.bal");
    }
    
    @Test(description = "Test defining a struct constant",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "lang/structs/constants/struct-constants.bal:3: invalid type 'Person'")
    public void testStructConstant() {
        BTestUtils.parseBalFile("lang/structs/constants");
    }
    
    @Test(description = "Test initializing a struct with undeclared field",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "undeclared-attribute-init.bal:3: unknown field 'age' in struct" +
            " 'Department'")
    public void testUndeclareFieldInit() {
        BTestUtils.parseBalFile("lang/structs/undeclared-attribute-init.bal");
    }
    
    @Test(description = "Test initializing a struct with mismatching field type",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "invalid-type-attribute-init.bal:3: incompatible types: expected "
                    + "'string', found 'int'")
    public void testMismatchingTypeFieldInit() {
        BTestUtils.parseBalFile("lang/structs/invalid-type-attribute-init.bal");
    }
    
    @Test(description = "Test initializing a struct with invalid field name",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "invalid-field-name-init.bal:3: invalid field name in struct " +
            "initializer")
    public void testInvalidFieldNameInit() {
        BTestUtils.parseBalFile("lang/structs/invalid-field-name-init.bal");
    }
}

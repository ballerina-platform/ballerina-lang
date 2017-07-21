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
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.ParserException;
import org.ballerinalang.util.exceptions.SemanticException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for user defined struct types in ballerina.
 */
public class StructTest {

    private ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("lang/structs/struct.bal");
    }
    
    @Test(description = "Test Basic struct operations")
    public void testBasicStruct() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testCreateStruct");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "Jack");

        Assert.assertTrue(returns[1] instanceof BMap);
        BMap<String, ?> adrsMap = ((BMap) returns[1]);
        Assert.assertEquals(adrsMap.get("country"), new BString("USA"));
        Assert.assertEquals(adrsMap.get("state"), new BString("CA"));

        Assert.assertTrue(returns[2] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 25);
    }

    @Test(description = "Test using expressions as index for struct arrays")
    public void testExpressionAsIndex() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testExpressionAsIndex");
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
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testStructOfStruct");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "USA");
    }
    
    @Test(description = "Test returning fields of a struct")
    public void testReturnStructAttributes() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testReturnStructAttributes");

        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "emily");
    }

    @Test(description = "Test using struct expression as a index in another struct expression")
    public void testStructExpressionAsIndex() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testStructExpressionAsIndex");
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "emily");
    }

    @Test(description = "Test default value of a struct field")
    public void testDefaultValue() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testDefaultVal");
        
        // Check default value of a field where the default value is set
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "default first name");
        
        // Check the default value of a field where the default value is not set
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "");
        
        Assert.assertTrue(returns[2] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 999);
    }

    @Test(description = "Test default value of a nested struct field")
    public void testNestedFieldDefaultValue() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testNestedFieldDefaultVal");
        
        Assert.assertTrue(returns[0] instanceof BString);
        Assert.assertEquals(returns[0].stringValue(), "default first name");
        
        Assert.assertTrue(returns[1] instanceof BString);
        Assert.assertEquals(returns[1].stringValue(), "Smith");
        
        Assert.assertTrue(returns[2] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[2]).intValue(), 999);
    }

    @Test(description = "Test default value of a nested struct field")
    public void testNestedStructInit() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "testNestedStructInit");
        
        Assert.assertTrue(returns[0] instanceof BStruct);
        BStruct person = ((BStruct) returns[0]);
        Assert.assertEquals(person.getStringField(0), "aaa");
        Assert.assertEquals(person.getIntField(0), 25);
        
        Assert.assertTrue(person.getRefField(2) instanceof BStruct);
        BStruct parent = ((BStruct) person.getRefField(2));
        Assert.assertEquals(parent.getStringField(0), "bbb");
        Assert.assertEquals(parent.getIntField(0), 50);
    }

    @Test(description = "Test negative default values in struct")
    public void testNegativeDefaultValue() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "getStructNegativeValues");
        Assert.assertEquals(returns.length, 4);
        Assert.assertSame(returns[0].getClass(), BInteger.class);
        Assert.assertSame(returns[1].getClass(), BInteger.class);
        Assert.assertSame(returns[2].getClass(), BFloat.class);
        Assert.assertSame(returns[3].getClass(), BFloat.class);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), -9);
        Assert.assertEquals(((BInteger) returns[1]).intValue(), -8);
        Assert.assertEquals(((BFloat) returns[2]).floatValue(), -88.234);
        Assert.assertEquals(((BFloat) returns[3]).floatValue(), -24.99);
    }
    
    @Test(description = "Test negative default values in struct")
    public void testStructToString() {
        BValue[] returns = BLangFunctions.invokeNew(programFile, "getStruct");
        Assert.assertEquals(returns[0].stringValue(), "{name:\"aaa\",lname:\"\",adrs:null,age:25,family:null,parent:" +
                "{name:\"bbb\",lname:\"ccc\",adrs:null,age:50,family:null,parent:null}}");
    }

    /*
     *  Negative tests
     */

    @Test(description = "Test defining structs with duplicate name",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "duplicate-structs.bal:7: redeclared symbol 'Department'")
    public void testDuplicateStructDefinitions() {
        BTestUtils.getProgramFile("lang/structs/duplicate-structs.bal");
    }
    
    @Test(description = "Test defining structs with duplicate fields",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "duplicate-fields.bal:4: redeclared symbol 'id'")
    public void testStructWithDuplicateFields() {
        BTestUtils.getProgramFile("lang/structs/duplicate-fields.bal");
    }
    
    @Test(description = "Test initializing an undeclraed structs",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "undeclared-struct-init.bal:2: undefined type 'Department'")
    public void testUndeclaredStructInit() {
        BTestUtils.getProgramFile("lang/structs/undeclared-struct-init.bal");
    }
    
    @Test(description = "Test accessing an undeclared struct",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "undeclared-struct-access.bal:4: undefined symbol 'dpt1'")
    public void testUndeclaredStructAccess() {
        BTestUtils.getProgramFile("lang/structs/undeclared-struct-access.bal");
    }
    
    @Test(description = "Test accessing an undeclared field of a struct",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "undeclared-attribute-access.bal:5: unknown field 'id' in struct "
                    + "'Department'")
    public void testUndeclaredFieldAccess() {
        BTestUtils.getProgramFile("lang/structs/undeclared-attribute-access.bal");
    }
    
    @Test(description = "Test defining a struct constant",
            expectedExceptions = {ParserException.class},
            expectedExceptionsMessageRegExp = "lang[/\\\\]structs[/\\\\]constants[/\\\\]struct-constants.bal:3:6: " +
            "missing \\{'int', 'float', 'boolean', 'string', 'blob'\\} before 'Person'")
    public void testStructConstant() {
        BTestUtils.getProgramFile("lang/structs/constants");
    }
    
    @Test(description = "Test initializing a struct with undeclared field",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "undeclared-attribute-init.bal:3: unknown field 'age' in struct" +
            " 'Department'")
    public void testUndeclareFieldInit() {
        BTestUtils.getProgramFile("lang/structs/undeclared-attribute-init.bal");
    }
    
    @Test(description = "Test initializing a struct with mismatching field type",
            expectedExceptions = {SemanticException.class},
            expectedExceptionsMessageRegExp = "invalid-type-attribute-init.bal:3: incompatible types: expected "
                    + "'map', found 'int'")
    public void testMismatchingTypeFieldInit() {
        BTestUtils.getProgramFile("lang/structs/invalid-type-attribute-init.bal");
    }
    
    @Test(description = "Test initializing a struct with invalid field name",
            expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "invalid-field-name-init.bal:3: invalid field name in struct " +
            "initializer")
    public void testInvalidFieldNameInit() {
        BTestUtils.getProgramFile("lang/structs/invalid-field-name-init.bal");
    }
}

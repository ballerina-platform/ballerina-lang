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
package org.ballerinalang.test.structs;

import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for user defined struct types in ballerina.
 */
public class StructNegativeTest {

    @Test(description = "Test defining structs with duplicate name")
    public void testDuplicateStructDefinitions() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs/duplicate-structs.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 2);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(), "redeclared symbol 'Department'");
        Assert.assertEquals(compileResult.getDiagnostics()[1].getMessage(), "unknown type 'Person'");
    }

    @Test(description = "Test defining structs with duplicate fields")
    public void testStructWithDuplicateFields() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs/duplicate-fields.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 2);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(), "redeclared symbol 'id'");
        Assert.assertEquals(compileResult.getDiagnostics()[1].getMessage(), "unknown type 'Person'");
    }

    @Test(description = "Test initializing an undeclraed structs")
    public void testUndeclaredStructInit() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs/undeclared-struct-init.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(), "unknown type 'Department'");
    }

    @Test(description = "Test accessing an undeclared struct")
    public void testUndeclaredStructAccess() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs/undeclared-struct-access.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(), "undefined symbol 'dpt1'");
    }

    @Test(description = "Test accessing an undeclared field of a struct")
    public void testUndeclaredFieldAccess() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs/undeclared-attribute-access.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(),
                            "undefined field 'id' in struct 'Department'");
    }

    //    @Test(description = "Test defining a struct constant")
    public void testStructConstant() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs", "constants");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(), "");
    }

    @Test(description = "Test initializing a struct with undeclared field")
    public void testUndeclareFieldInit() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs/undeclared-attribute-init.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(),
                            "undefined field 'age' in struct 'Department'");
    }

    @Test(description = "Test initializing a struct with mismatching field type")
    public void testMismatchingTypeFieldInit() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs/invalid-type-attribute-init.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(),
                            "incompatible types: expected 'map', found 'int'");
    }

    @Test(description = "Test initializing a struct with invalid field name")
    public void testInvalidFieldNameInit() {
        CompileResult compileResult = BTestUtils.compile("test-src/structs/invalid-field-name-init.bal");
        Assert.assertEquals(compileResult.getWarnCount(), 0);
        Assert.assertEquals(compileResult.getErrorCount(), 1);
        Assert.assertEquals(compileResult.getDiagnostics()[0].getMessage(),
                            "invalid field name in 'struct' literal, identifier or string literal expected");
    }
}

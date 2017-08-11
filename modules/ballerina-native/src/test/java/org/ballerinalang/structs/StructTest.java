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
package org.ballerinalang.structs;

import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.program.BLangFunctions;
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

    
    /*
     *  Negative tests
     */
    
    @Test(description = "Test accessing an field of a noninitialized struct",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:NullReferenceError.*")
    public void testGetNonInitField() {
        BLangFunctions.invokeNew(programFile, "testGetNonInitAttribute");
    }
    
    @Test(description = "Test accessing an arrays field of a noninitialized struct",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:NullReferenceError.*")
    public void testGetNonInitArrayField() {
        BLangFunctions.invokeNew(programFile, "testGetNonInitArrayAttribute");
    }

    @Test(description = "Test accessing the field of a noninitialized struct",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:NullReferenceError.*")
    public void testGetNonInitLastField() {
        BLangFunctions.invokeNew(programFile, "testGetNonInitLastAttribute");
    }

    @Test(description = "Test setting an field of a noninitialized child struct",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:NullReferenceError.*")
    public void testSetNonInitField() {
        BLangFunctions.invokeNew(programFile, "testSetFieldOfNonInitChildStruct");
    }

    @Test(description = "Test setting the field of a noninitialized root struct",
            expectedExceptions = {BLangRuntimeException.class},
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:NullReferenceError.*")
    public void testSetNonInitLastField() {
        BLangFunctions.invokeNew(programFile, "testSetFieldOfNonInitStruct");
    }
    
}

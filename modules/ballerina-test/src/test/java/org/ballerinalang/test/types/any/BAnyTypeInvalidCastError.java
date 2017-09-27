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
package org.ballerinalang.any;

import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for assignment statement.
 */
public class BAnyTypeInvalidCastError {
    private ProgramFile programFile;

    @BeforeClass
    public void setup() {
        programFile = BTestUtils.getProgramFile("lang/any/any-type-invalid-cast.bal");
    }
    //todo below exception should be BLangRuntimeException, but current nonBlocking behavior throw BallerinaException
    //so for the time being, using that.
    @Test(expectedExceptions = {BallerinaException.class},
          expectedExceptionsMessageRegExp = "cannot cast 'any' with type 'int' to type 'float'")
    public void testInvalidDirectCastFromAnyToDouble() {
        BLangFunctions.invokeNew(programFile, "invalidCastingError");
    }

    @Test(expectedExceptions = {BallerinaException.class},
          expectedExceptionsMessageRegExp = "cannot cast 'null' value to type 'int'")
    public void testCastingUndefinedAnyValue() {
        BLangFunctions.invokeNew(programFile, "undefinedCasting");
    }
}

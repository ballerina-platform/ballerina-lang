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
package org.ballerinalang.expressions;

import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Array access expression test.
 *
 * @since 0.88
 */
public class ArrayAccessExprTest {

    @BeforeClass
    public void setup() {
    }

    //TODO try to validate all the lines in the exception message
    @Test(description = "Test access an non-initialized arrays",
            expectedExceptions = BLangRuntimeException.class,
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:NullReferenceError.*")
    public void testNonInitArrayAccess() {
        ProgramFile bLangProgram = BTestUtils.getProgramFile("lang/expressions/array-access-expr.bal");
        BLangFunctions.invokeNew(bLangProgram, "testNonInitArrayAccess");
        Assert.fail("Test should fail at this point.");
    }
}

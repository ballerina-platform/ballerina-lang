/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.ballerinalang.util.program.BLangFunctions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * 'lengthof' Unary expression test.
 *
 * @since 0.87
 */
public class LengthOfUnaryExpressionTest {

    private ProgramFile bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.getProgramFile("lang/expressions/length-of-unary-null-reference.bal");
    }

    @Test(description = "Test lengthof unary expression when array is null.",
            expectedExceptions = {BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:NullReferenceError.*")
    public void testArrayLengthAccessExpArrayNullCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BLangFunctions.invokeNew(bLangProgram, "arrayLengthAccessNullArrayCase", args);
    }


    @Test(description = "Test lengthof unary expression when reference point to json null.",
            expectedExceptions = {BLangRuntimeException.class },
            expectedExceptionsMessageRegExp = "error: ballerina.lang.errors:NullReferenceError.*")
    public void testArrayLengthAccessTestJSONArrayNegativeNullCase() {
        BValue[] args = {new BInteger(100), new BInteger(5)};
        BLangFunctions.invokeNew(bLangProgram, "arrayLengthAccessTestJSONArrayNegativeNullCase", args);
    }
}

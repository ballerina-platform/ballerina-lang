/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.var;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.Test;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Test usage of `var` where the inferred type includes `error`.
 *
 * @since 2.0.0
 */
public class VariableDeclaredWithWithVarIncludingErrorTest {

    @Test
    public void testVarWithInferredTypeIncludingError() {
        CompileResult result = BCompileUtil.compile(
                "test-src/types/var/var_with_inferred_type_including_error.bal");
        assertEquals(result.getErrorCount(), 0);
    }

    @Test
    public void testVarWithInferredTypeIncludingErrorNegative() {
        CompileResult negativeResult = BCompileUtil.compile(
                "test-src/types/var/var_with_inferred_type_including_error_negative.bal");
        int index = 0;
        validateError(negativeResult, index++, getInvalidUnusedVarErrorMessage("y"), 17, 9);
        validateError(negativeResult, index++, getInvalidUnusedVarErrorMessage("a"), 20, 6);
        validateError(negativeResult, index++, getInvalidUnusedVarErrorMessage("m"), 23, 1);
        validateError(negativeResult, index++, getInvalidUnusedVarErrorMessage("y1"), 26, 14);
        validateError(negativeResult, index++, getInvalidUnusedVarErrorMessage("a1"), 28, 10);
        validateError(negativeResult, index++, getInvalidUnusedVarErrorMessage("m1"), 30, 5);
        validateError(negativeResult, index++, getInvalidUnusedVarErrorMessage("e1"), 33, 20);
        validateError(negativeResult, index++, getInvalidUnusedVarErrorMessage("e2"), 33, 43);
        validateError(negativeResult, index++, getInvalidUnusedVarErrorMessage("e1"), 36, 25);
        validateError(negativeResult, index++, getInvalidUnusedVarErrorMessage("e3"), 36, 76);
        validateError(negativeResult, index++, getInvalidUnusedVarErrorMessage("e"), 44, 27);
        validateError(negativeResult, index++, getInvalidUnusedVarErrorMessage("r"), 44, 34);
        validateError(negativeResult, index++, getInvalidUnusedVarErrorMessage("err"), 44, 60);
        validateError(negativeResult, index++, getInvalidUnusedVarErrorMessage("e"), 47, 32);
        validateError(negativeResult, index++, getInvalidUnusedVarErrorMessage("r"), 47, 39);
        validateError(negativeResult, index++, getInvalidUnusedVarErrorMessage("err"), 47, 65);
        assertEquals(index, negativeResult.getErrorCount());
    }

    private String getInvalidUnusedVarErrorMessage(String varName) {
        return "unused variable '" + varName + "' with inferred type including error";
    }
}

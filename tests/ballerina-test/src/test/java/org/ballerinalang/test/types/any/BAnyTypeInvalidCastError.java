/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.types.any;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Negative tests for 'any' type.
 */
public class BAnyTypeInvalidCastError {

    @Test
    public void testInvalidAnyCasting() {
        CompileResult resultNegative = BCompileUtil.compile("test-src/types/any/any-type-cast-negative.bal");

        Assert.assertEquals(resultNegative.getErrorCount(), 2);

        BAssertUtil.validateError(resultNegative, 0, 3, 15);
        BAssertUtil.validateErrorMessageOnly(resultNegative, 0,
                "incompatible types: expected 'float', found ");
        BAssertUtil.validateErrorMessageOnly(resultNegative, 0, new String[] {"float|error", "error|float"});
        BAssertUtil.validateError(resultNegative, 1, 14, 11);
        BAssertUtil.validateErrorMessageOnly(resultNegative, 1,
                "incompatible types: expected 'int', found ");
        BAssertUtil.validateErrorMessageOnly(resultNegative, 1, new String[] {"int|error", "error|int"});
        //TODO: This needs to have another error, for casting a null value. Add that check when it's fixed.
    }
}

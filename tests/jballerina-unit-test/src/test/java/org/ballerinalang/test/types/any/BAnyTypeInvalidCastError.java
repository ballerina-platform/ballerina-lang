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

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Negative tests for 'any' type.
 */
public class BAnyTypeInvalidCastError {

    @Test
    public void testInvalidAnyCasting() {
        CompileResult resultNegative = BCompileUtil.compile("test-src/types/any/any-type-cast-negative.bal");

        Assert.assertEquals(resultNegative.getErrorCount(), 0);
        BValue[] returns = BRunUtil.invoke(resultNegative, "invalidCastingError", new BValue[]{});
        Assert.assertEquals(returns[0].stringValue(), "{ballerina}TypeCastError {\"message\":\"incompatible types:" +
                " 'string' cannot be cast to 'float'\"}");
    }
}

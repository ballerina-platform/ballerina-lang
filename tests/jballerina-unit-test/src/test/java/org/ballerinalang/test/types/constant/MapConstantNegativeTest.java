/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.constant;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;

/**
 * Constant negative tests.
 */
public class MapConstantNegativeTest {

    @Test()
    public void testNegative() {
        CompileResult compileResult = BCompileUtil.compile("test-src/types/constant/map_constant_negative.bal");

        Assert.assertEquals(compileResult.getErrorCount(), 5);

        validateError(compileResult, 0, "expression is not a constant expression", 18, 35);
        validateError(compileResult, 1, "incompatible types: expected 'boolean', found 'boolean?'", 18, 35);
        validateError(compileResult, 2, "expression is not a constant expression", 20, 27);
        validateError(compileResult, 3, "expression is not a constant expression", 20, 38);
        validateError(compileResult, 4, "cannot update constant value", 34, 5);
    }
}

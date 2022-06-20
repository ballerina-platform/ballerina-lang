/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.types.readonly;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for inherently immutable values with the `readonly` type.
 *
 * @since 1.3.0
 */
public class InherentlyImmutableTypeTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/readonly/test_inherently_immutable_type.bal");
    }

    @Test
    public void testReadonlyType() {
        BRunUtil.invoke(result, "testReadonlyType");
    }

    @Test
    public void testReadonlyTypeNegative() {
        CompileResult negativeResult = BCompileUtil.compile("test-src/types/readonly" +
                "/test_inherently_immutable_type_negative.bal");
        int i = 0;
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'any', found 'readonly'", 19, 14);
        BAssertUtil.validateError(negativeResult, i++,
                "operator '==' not defined for 'readonly' and '[int,int,int]'", 24, 14);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'error?', found 'readonly'", 29, 26);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected '(int|any)', found 'readonly'", 30, 27);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected '(string|readonly)', found '(readonly|any)'", 32, 43);
        BAssertUtil.validateError(negativeResult, i++,
                "incompatible types: expected 'any', found '(readonly|string)'", 34, 17);
        Assert.assertEquals(negativeResult.getErrorCount(), 6);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}

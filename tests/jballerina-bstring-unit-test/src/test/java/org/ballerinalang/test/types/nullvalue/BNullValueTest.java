/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.types.nullvalue;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for ballerina null value.
 */
public class BNullValueTest {

    private CompileResult resultNegative, resultSemanticsNegative;

    @BeforeClass
    public void setup() {
        resultNegative = BCompileUtil.compile("test-src/types/null/null-value-negative.bal");
        resultSemanticsNegative = BCompileUtil.compile("test-src/types/null/null-value-semantics-negative.bal");
    }

    @Test(description = "Test negative test cases")
    void testNullValueSemanticsNegative() {
        Assert.assertEquals(resultSemanticsNegative.getErrorCount(), 5);
        BAssertUtil.validateError(resultSemanticsNegative, 0, "operator '>' not defined for '()' and 'xml?'", 12, 13);
        BAssertUtil.validateError(resultSemanticsNegative, 1, "incompatible types: expected 'int', found '()'", 16, 13);
        BAssertUtil.validateError(resultSemanticsNegative, 2, "operator '+' not defined for '()' and '()'", 20, 13);
        BAssertUtil.validateError(resultSemanticsNegative, 3, "incompatible types: expected 'string', found '()'", 24
                , 16);
        BAssertUtil.validateError(resultSemanticsNegative, 4, "operator '+' not defined for '()' and '()'", 32, 13);
    }

    @Test(description = "Test negative test cases")
    void testNullValueNegative() {
        Assert.assertEquals(resultNegative.getErrorCount(), 1);
        BAssertUtil.validateError(resultNegative, 0, "'null' literal is only supported for 'json'", 20, 12);
    }
}

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

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for ballerina null value.
 */
public class BNullValueTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/types/null/null-value-negative.bal");
    }

    @Test(description = "Test negative test cases")
    void testNullValueNegative() {
        Assert.assertEquals(result.getErrorCount(), 7);
        BAssertUtil.validateError(result, 0, "operator '>' not defined for '()' and 'xml?'", 12, 13);
        BAssertUtil.validateError(result, 1, "incompatible types: expected 'int', found '()'", 16, 13);
        BAssertUtil.validateError(result, 2, "operator '+' not defined for '()' and '()'", 20, 13);
        BAssertUtil.validateError(result, 3, "'null' literal is only supported for 'json'", 20, 13);
        BAssertUtil.validateError(result, 4, "'null' literal is only supported for 'json'", 20, 20);
        BAssertUtil.validateError(result, 5, "incompatible types: expected 'string', found '()'", 24, 16);
        BAssertUtil.validateError(result, 6, "operator '+' not defined for '()' and '()'", 32, 13);
    }
}

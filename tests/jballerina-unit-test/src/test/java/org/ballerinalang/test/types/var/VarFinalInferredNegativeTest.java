/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 * Class to test final var declarations type inferring.
 *
 * @since 3.0.0
 */
public class VarFinalInferredNegativeTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/var/var-final-inferred-negative.bal");
    }

    @Test
    public void testVarFinalInferredNegative() {
        int i = 0;
        BAssertUtil.validateError(compileResult, i++, "incompatible types: expected '1', found '2'", 28, 13);
        BAssertUtil.validateError(compileResult, i++, "incompatible types: expected 'hello', found 'hi'", 33, 19);
        BAssertUtil.validateError(compileResult, i++, "incompatible types: expected '[1,2]', found '[1,3]'", 39, 18);
        BAssertUtil.validateError(compileResult, i++, "incompatible types: expected '[1,hello]', found '[1,hi]'", 40,
                20);
        BAssertUtil.validateError(compileResult, i++, "incompatible types: expected '1', found '2'", 45, 15);
        BAssertUtil.validateError(compileResult, i++, "incompatible types: expected 'hi', found 'ballerina'", 50, 15);
        assertEquals(compileResult.getErrorCount(), i);
    }
}

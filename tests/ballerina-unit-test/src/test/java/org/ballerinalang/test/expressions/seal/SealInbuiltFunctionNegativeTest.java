/*
*   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.test.expressions.seal;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative test cases for sealing variables.
 *
 * @since 0.983.0
 */
public class SealInbuiltFunctionNegativeTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/expressions/seal/seal-expr-negative-test.bal");
    }


    //----------------------------- NegativeTest cases ------------------------------------------------------

    @Test
    public void testSealNegativeTest() {

        Assert.assertEquals(compileResult.getErrorCount(), 5);

        //Negative test case to verify the unsupported type for seal operation.
        BAssertUtil.validateError(compileResult, 1,
                "function 'seal' defined on not supported type 'stream<Employee>'",
                18, 5);

        //Negative test case to verify the no of arguments get passed to seal function.
        BAssertUtil.validateError(compileResult, 2,
                "too many arguments in call to 'seal()'",
                24, 5);

        //Negative test case to confirm primitive types are not supported for seal operation.
        BAssertUtil.validateError(compileResult, 3,
                "function 'seal' defined on not supported type 'string'",
                31, 5);

        //Negative test case to confirm primitive types are not supported for seal operation.
        BAssertUtil.validateError(compileResult, 4,
                "function 'seal' defined on not supported type 'string[]'",
                38, 5);
    }

}


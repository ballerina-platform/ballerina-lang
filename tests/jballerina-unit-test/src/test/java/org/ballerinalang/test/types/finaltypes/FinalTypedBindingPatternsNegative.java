/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.finaltypes;

import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases final typed binding patterns.
 */
public class FinalTypedBindingPatternsNegative {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/finaltypes/final-typed-binding-patterns-negative.bal");
    }

    @Test
    public void testNegative() {
        int index = 0;
        BAssertUtil.validateWarning(compileResult, index++, "unused variable 'b'", 33, 17);
        BAssertUtil.validateWarning(compileResult, index++, "unused variable 'j'", 33, 23);
        BAssertUtil.validateError(compileResult, index++, "cannot assign a value to final 'b'", 34, 5);
        BAssertUtil.validateError(compileResult, index++, "cannot assign a value to final 'j'", 35, 5);
        BAssertUtil.validateWarning(compileResult, index++, "unused variable 'b'", 39, 18);
        BAssertUtil.validateWarning(compileResult, index++, "unused variable 'j'", 39, 24);
        BAssertUtil.validateError(compileResult, index++, "cannot assign a value to final 'b'", 40, 5);
        BAssertUtil.validateError(compileResult, index++, "cannot assign a value to final 'j'", 41, 5);
        BAssertUtil.validateWarning(compileResult, index++, "unused variable 'a'", 47, 17);
        BAssertUtil.validateWarning(compileResult, index++, "unused variable 'b'", 47, 24);
        BAssertUtil.validateWarning(compileResult, index++, "unused variable 's'", 47, 27);
        BAssertUtil.validateError(compileResult, index++, "cannot assign a value to final 'a'", 48, 5);
        BAssertUtil.validateError(compileResult, index++, "cannot assign a value to final 's'", 49, 5);
        BAssertUtil.validateWarning(compileResult, index++, "unused variable 'a'", 53, 19);
        BAssertUtil.validateWarning(compileResult, index++, "unused variable 'b'", 53, 27);
        BAssertUtil.validateWarning(compileResult, index++, "unused variable 's'", 53, 30);
        BAssertUtil.validateError(compileResult, index++, "cannot assign a value to final 'a'", 54, 5);
        BAssertUtil.validateError(compileResult, index++, "cannot assign a value to final 'b'", 55, 5);
        BAssertUtil.validateError(compileResult, index++, "cannot assign a value to final 's'", 56, 5);
        BAssertUtil.validateWarning(compileResult, index++, "unused variable 'i'", 62, 16);
        BAssertUtil.validateWarning(compileResult, index++, "unused variable 'j'", 62, 19);
        BAssertUtil.validateError(compileResult, index++, "cannot assign a value to final 'i'", 63, 5);
        BAssertUtil.validateError(compileResult, index++, "cannot assign a value to final 'j'", 64, 5);
        BAssertUtil.validateWarning(compileResult, index++, "unused variable 'i'", 68, 26);
        BAssertUtil.validateWarning(compileResult, index++, "unused variable 'j'", 68, 29);
        BAssertUtil.validateError(compileResult, index++, "cannot assign a value to final 'i'", 69, 5);
        BAssertUtil.validateError(compileResult, index++, "cannot assign a value to final 'j'", 70, 5);
        BAssertUtil.validateWarning(compileResult, index++, "unused variable 'i'", 76, 16);
        BAssertUtil.validateWarning(compileResult, index++, "unused variable 'j'", 76, 19);
        BAssertUtil.validateError(compileResult, index++, "cannot assign a value to final 'i'", 77, 5);
        BAssertUtil.validateError(compileResult, index++, "cannot assign a value to final 'j'", 78, 5);
        BAssertUtil.validateWarning(compileResult, index++, "unused variable 'i'", 82, 35);
        BAssertUtil.validateWarning(compileResult, index++, "unused variable 'j'", 82, 39);
        BAssertUtil.validateWarning(compileResult, index++, "unused variable 'k'", 82, 42);
        BAssertUtil.validateError(compileResult, index++, "cannot assign a value to final 'i'", 83, 5);
        BAssertUtil.validateError(compileResult, index++, "cannot assign a value to final 'j'", 84, 5);
        BAssertUtil.validateError(compileResult, index++, "cannot assign a value to final 'k'", 85, 5);

        Assert.assertEquals(compileResult.getDiagnostics().length, index);
    }

    @AfterClass
    public void tearDown() {
        compileResult = null;
    }
}

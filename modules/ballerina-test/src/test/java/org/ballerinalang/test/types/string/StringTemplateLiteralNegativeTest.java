/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.types.string;

import org.ballerinalang.test.utils.BTestUtils;
import org.ballerinalang.test.utils.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for String Template Literal negative tests.
 */
public class StringTemplateLiteralNegativeTest {

    private CompileResult resultNegative;

    @BeforeClass
    public void setup() {
        resultNegative = BTestUtils.compile("test-src/types/string/string-template-literal-negative.bal");
    }

    @Test(description = "Test string template literal with errors")
    public void testStringTemplateLiteralNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 3);
        //testUndefinedSymbol
        BTestUtils.validateError(resultNegative, 0, "undefined symbol 'name'", 2, 31);
        //testIncompatibleTypes
        BTestUtils.validateError(resultNegative, 1, "incompatible types: expected 'string', found 'json'", 8, 31);
        //testMismatchedInputs
        BTestUtils.validateError(resultNegative, 2, "this function must return a result", 12, 0);
    }
}

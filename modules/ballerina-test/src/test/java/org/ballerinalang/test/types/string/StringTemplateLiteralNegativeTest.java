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
import org.ballerinalang.util.exceptions.ParserException;
import org.ballerinalang.util.exceptions.SemanticException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test class for String Template Literal negative tests.
 */
public class StringTemplateLiteralNegativeTest {

    private CompileResult resultNegative;
    //TODO setup fails
    @BeforeClass
    public void setup() {
        resultNegative = BTestUtils.compile("test-src/types/string/string-template-literal-negative.bal");
    }

    @Test(description = "Test string template literal with errors")
    public void testBlockStmtNegativeCases() {
        Assert.assertEquals(resultNegative.getErrorCount(), 5);
        //testUnreachableStmtInIfFunction1
        BTestUtils.validateError(resultNegative, 0, "unreachable code", 9, 4);
        //testUnreachableStmtInIfFunction2
        BTestUtils.validateError(resultNegative, 1, "unreachable code", 25, 4);
        //testUnreachableStmtInIfBlock
        BTestUtils.validateError(resultNegative, 2, "unreachable code", 33, 8);
     }

    @Test(expectedExceptions = {SemanticException.class},
          expectedExceptionsMessageRegExp = "stringTemplateNegativeTest.bal:2: undefined symbol 'name'")
    public void testStringTemplate() {
        BTestUtils.compile("samples/stringTemplate/stringTemplateNegativeTest.bal");
    }

    @Test(expectedExceptions = {SemanticException.class},
          expectedExceptionsMessageRegExp = "stringTemplateNegativeTest2.bal:3: invalid operation: incompatible types" +
                  " 'string' and 'json'")
    public void testStringTemplate2() {
        BTestUtils.compile("samples/stringTemplate/stringTemplateNegativeTest2.bal");
    }

    @Test(expectedExceptions = {ParserException.class},
          expectedExceptionsMessageRegExp = "stringTemplateNegativeTest3.bal:4:70: mismatched input '}'. Expecting .*")
    public void testStringTemplate3() {
        BTestUtils.compile("samples/stringTemplate/stringTemplateNegativeTest3.bal");
    }
}

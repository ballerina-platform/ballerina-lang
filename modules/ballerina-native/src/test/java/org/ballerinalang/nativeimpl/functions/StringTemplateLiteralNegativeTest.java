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
package org.ballerinalang.nativeimpl.functions;

import org.ballerinalang.nativeimpl.util.BTestUtils;
import org.ballerinalang.util.exceptions.ParserException;
import org.ballerinalang.util.exceptions.SemanticException;
import org.testng.annotations.Test;

/**
 * Test class for String Template Literal negative tests.
 */
public class StringTemplateLiteralNegativeTest {

    @Test(expectedExceptions = {SemanticException.class},
          expectedExceptionsMessageRegExp = "stringTemplateNegativeTest.bal:2: undefined symbol 'name'")
    public void testStringTemplate() {
        BTestUtils.getProgramFile("samples/stringTemplate/stringTemplateNegativeTest.bal");
    }

    @Test(expectedExceptions = {SemanticException.class},
          expectedExceptionsMessageRegExp = "stringTemplateNegativeTest2.bal:3: invalid operation: incompatible types" +
                  " 'string' and 'json'")
    public void testStringTemplate2() {
        BTestUtils.getProgramFile("samples/stringTemplate/stringTemplateNegativeTest2.bal");
    }

    @Test(expectedExceptions = {ParserException.class},
          expectedExceptionsMessageRegExp = "stringTemplateNegativeTest3.bal:4:70: mismatched input '}'. Expecting .*")
    public void testStringTemplate3() {
        BTestUtils.getProgramFile("samples/stringTemplate/stringTemplateNegativeTest3.bal");
    }
}

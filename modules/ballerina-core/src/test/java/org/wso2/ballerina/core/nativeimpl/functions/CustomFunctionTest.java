/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerina.core.nativeimpl.functions;

import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.utils.ParserUtils;

/**
 * Test Custom function.
 */
public class CustomFunctionTest {

    @Test(description = "Test defining duplicate ballerina function",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "duplicate-function.bal:5: redeclared symbol 'foo'")
    public void testDuplicateFunction() {
        ParserUtils.parseBalFile("lang/functions/duplicate-function.bal");
    }
    
    @Test(description = "Test defining ballerina function with duplicate parameters",
            expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "duplicate-parameters.bal:1: redeclared symbol 'param'")
    public void testDuplicateParameters() {
        ParserUtils.parseBalFile("lang/functions/duplicate-parameters.bal");
    }

}

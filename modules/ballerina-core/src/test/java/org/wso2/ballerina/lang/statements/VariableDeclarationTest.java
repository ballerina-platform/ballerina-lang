/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerina.lang.statements;

import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.ParserException;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.utils.ParserUtils;

/**
 * Tests covering variable declarations.
 */
public class VariableDeclarationTest {

    /*
     * Negative tests
     */
    
    @Test(expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "Duplicate variable declaration with name: b in " +
            "duplicate-variables.bal:7")
    public void testDuplicateVariables() {
        ParserUtils.parseBalFile("lang/statements/duplicate-variables.bal");
    }
    
    @Test(expectedExceptions = {ParserException.class },
            expectedExceptionsMessageRegExp = "Unsupported type: Foo in unsupported-type-variable.bal:6")
    public void testUnsupportedTypeVariable() {
        ParserUtils.parseBalFile("lang/statements/unsupported-type-variable.bal");
    }
}

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

package org.wso2.ballerina.core.parser.negative;

import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.ParserException;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.utils.ParserUtils;

/**
 * Semantic Errors test class for ballerina filers.
 * This class test error handling for violations of semantic rules.
 */
public class InvalidSemanticParserTest {

    @Test(expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "Duplicate variable declaration with name: b in DuplicateVariables.bal:7")
    public void testDuplicateVariables() {
        ParserUtils.parseBalFile("samples/parser/invalidSemantic/DuplicateVariables.bal");
    }

    @Test(expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "Incompatible types: int cannot be converted to boolean in " +
                "IncompatibleTypeAssignment.bal:7")
    public void testIncompatibleTypeAssignment() {
        ParserUtils.parseBalFile("samples/parser/invalidSemantic/IncompatibleTypeAssignment.bal");
    }
    
    @Test(expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "Incompatible types in binary expression: int vs float in " +
                "IncompatibleTypeComparison.bal:11")
    public void testIncompatibleTypeComparison() {
        ParserUtils.parseBalFile("samples/parser/invalidSemantic/IncompatibleTypeComparison.bal");
    }
    
    @Test(expectedExceptions = {SemanticException.class },
            expectedExceptionsMessageRegExp = "Incompatible types in binary expression: int vs boolean in " +
                "IncompatibleTypeBinOperation.bal:7")
    public void testIncompatibleTypeBinOperation() {
        ParserUtils.parseBalFile("samples/parser/invalidSemantic/IncompatibleTypeBinOperation.bal");
    }
    
    @Test(expectedExceptions = {ParserException.class },
            expectedExceptionsMessageRegExp = "Unsupported type: Foo in UnsupportedVariableType.bal:6")
    public void testUnsupportedTypeVariable() {
        ParserUtils.parseBalFile("samples/parser/invalidSemantic/UnsupportedVariableType.bal");
    }
    
}

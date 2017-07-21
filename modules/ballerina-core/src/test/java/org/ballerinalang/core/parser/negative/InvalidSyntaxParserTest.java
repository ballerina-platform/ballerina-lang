/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.core.parser.negative;

import org.ballerinalang.core.utils.BTestUtils;
import org.ballerinalang.util.exceptions.BallerinaException;
//import org.ballerinalang.util.exceptions.ParserException;
import org.testng.annotations.Test;

/**
 * Syntax Errors test class for ballerina filers.
 * This class test error handling for violations of grammar.
 */
public class InvalidSyntaxParserTest {

    /**
     * Test missing expected syntax.
     */

    @Test(expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "SemicolonMissingService.bal:14:6: missing ';' before 'reply'")
    public void testParseSemicolonMissingSerivce() {
        BTestUtils.getProgramFile("samples/parser/invalidSyntax/SemicolonMissingService.bal");
    }

    @Test(expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "SemicolonMissingMainFunc.bal:7:1: missing ';' before 'reply'")
    public void testParseSemicolonMissingMainFunc() {
        BTestUtils.getProgramFile("samples/parser/invalidSyntax/SemicolonMissingMainFunc.bal");
    }


//    /**
//     * Test invalid identifier. i.e: {@link org.antlr.v4.runtime.NoViableAltException}
//     */
//
//    @Test(expectedExceptions = {ParserException.class},
//            expectedExceptionsMessageRegExp = "IdentifierMissingService.bal:12:9: mismatched input ';'. " +
//                    "Expecting one of \\{'\\[', Identifier\\}")
//    public void testParseIdentifierMissingSerivce() {
//         BTestUtils.parseBalFile("samples/parser/invalidSyntax/IdentifierMissingService.bal");
//    }

    @Test(expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "IdentifierMissingMainFunc.bal:5:1: invalid identifier 'b'")
    public void testParseIdentifierMissingMainFunc() {
         BTestUtils.getProgramFile("samples/parser/invalidSyntax/IdentifierMissingMainFunc.bal");
    }

//    @Test(expectedExceptions = {BallerinaException.class},
//            expectedExceptionsMessageRegExp = "ReservedWordVariable.bal:5:8: " +
//                    "mismatched input 'resource'. Expecting one of \\{'\\[', Identifier\\}")
//    public void testReservedWordVariable() {
//        BTestUtils.parseBalFile("samples/parser/invalidSyntax/ReservedWordVariable.bal");
//    }

    
    /**
     * Test unwanted token.
     */

    @Test(expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "ServiceWithoutResourceName.bal:8:11: " +
                    "mismatched input '\\{'. Expecting one of Identifier")
    public void testServiceWithoutResourceName() {
        BTestUtils.getProgramFile("samples/parser/invalidSyntax/ServiceWithoutResourceName.bal");
    }

    @Test(expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "MainFuncWithoutName.bal:3:9: " +
                    "mismatched input '\\{'. Expecting one of Identifier")
    public void testParseMainFuncWithoutName() {
        BTestUtils.getProgramFile("samples/parser/invalidSyntax/MainFuncWithoutName.bal");
    }


    /**
     * Test mismatched input. i.e. {@link org.antlr.v4.runtime.InputMismatchException}
     */

    @Test(expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "ServiceWithoutResourceParams.bal:8:17: mismatched input '\\{'. " +
                    "Expecting one of '\\('")
    public void testServiceWithoutResourceParams() {
        BTestUtils.getProgramFile("samples/parser/invalidSyntax/ServiceWithoutResourceParams.bal");
    }

    @Test(expectedExceptions = {BallerinaException.class},
            expectedExceptionsMessageRegExp = "MainFuncWithoutParams.bal:3:14: mismatched input '\\{'. Expecting " +
                    "one of '\\('")
    public void testParseMainFuncWithoutParams() {
        BTestUtils.getProgramFile("samples/parser/invalidSyntax/MainFuncWithoutParams.bal");
    }

//    @Test(expectedExceptions = {BallerinaException.class },
//            expectedExceptionsMessageRegExp = "ResourceWithEmptyReply.bal:11:6: mismatched input ';'\\. " +
//                    "Expecting one of \\{'\\{', '\\(', '[]', '[', '\\+', '-', '!', 'create', IntegerLiteral, " +
//                    "FloatingPointLiteral, BooleanLiteral, QuotedStringLiteral, BacktickStringLiteral, " +
//                    "NullLiteral, Identifier\\}")
//    public void testResourceWithEmptyReply() {
//        getParserForFile("samples/parser/invalidSyntax/ResourceWithEmptyReply.bal");
//    }

}

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

package org.wso2.ballerina.core.parser.negative;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.model.builder.BLangModelBuilder;
import org.wso2.ballerina.core.parser.BallerinaParser;
import org.wso2.ballerina.core.parser.BallerinaParserErrorStrategy;
import org.wso2.ballerina.core.parser.antlr4.BLangAntlr4Listener;
import org.wso2.ballerina.core.utils.ParserUtils;

/**
 * Syntax Errors test class for ballerina filers.
 * This class test error handling for violations of grammar.
 */
public class InvalidSyntaxParserTest {

   /**
    * Test missing expected syntax.
    */
    
    @Test(expectedExceptions = {ParseCancellationException.class },
            expectedExceptionsMessageRegExp = "Missing ';' before 'reply' in SemicolonMissingService.bal\\[13:6\\]")
    public void testParseSemicolonMissingSerivce() {
        getParserForFile("samples/parser/invalidSyntax/SemicolonMissingService.bal").compilationUnit();
    }

    @Test(expectedExceptions = {ParseCancellationException.class },
            expectedExceptionsMessageRegExp = "Missing ';' before 'reply' in SemicolonMissingMainFunc.bal\\[7:1\\]")
    public void testParseSemicolonMissingMainFunc() {
        getParserForFile("samples/parser/invalidSyntax/SemicolonMissingMainFunc.bal").compilationUnit();
    }
    
    
    /**
     *  Test invalid identifier. i.e: {@link org.antlr.v4.runtime.NoViableAltException} 
     */
    
    @Test(expectedExceptions = {ParseCancellationException.class },
            expectedExceptionsMessageRegExp = "Invalid identifier 'int' in IdentifierMissingService.bal\\[12:6\\]")
    public void testParseIdentifierMissingSerivce() {
        getParserForFile("samples/parser/invalidSyntax/IdentifierMissingService.bal").compilationUnit();
    }

    @Test(expectedExceptions = {ParseCancellationException.class },
            expectedExceptionsMessageRegExp = "Invalid identifier 'b' in IdentifierMissingMainFunc.bal\\[5:1\\]")
    public void testParseIdentifierMissingMainFunc() {
        getParserForFile("samples/parser/invalidSyntax/IdentifierMissingMainFunc.bal").compilationUnit();
    }
    
    @Test(expectedExceptions = {ParseCancellationException.class },
            expectedExceptionsMessageRegExp = "Invalid identifier 'string' in ReservedWordVariable.bal\\[5:1\\]")
    public void testReservedWordVariable() {
        getParserForFile("samples/parser/invalidSyntax/ReservedWordVariable.bal").compilationUnit();
    }
    
    
    
    /**
     *  Test unwanted token.
     */
    
    @Test(expectedExceptions = {ParseCancellationException.class },
            expectedExceptionsMessageRegExp = "Unwanted token '\\{' in ServiceWithoutResourceName.bal\\[7:11\\]")
    public void testServiceWithoutResourceName() {
        getParserForFile("samples/parser/invalidSyntax/ServiceWithoutResourceName.bal").compilationUnit();
    }

    @Test(expectedExceptions = {ParseCancellationException.class },
            expectedExceptionsMessageRegExp = "Unwanted token '\\{' in MainFuncWithoutName.bal\\[4:9\\]")
    public void testParseMainFuncWithoutName() {
        getParserForFile("samples/parser/invalidSyntax/MainFuncWithoutName.bal").compilationUnit();
    }

    
    /**
     *  Test mismatched input. i.e. {@link org.antlr.v4.runtime.InputMismatchException}
     */
    
    @Test(expectedExceptions = {ParseCancellationException.class },
            expectedExceptionsMessageRegExp = "Mismatched input '\\{' in ServiceWithoutResourceParams.bal\\[7:17\\]." +
                " Expecting one of '\\('")
    public void testServiceWithoutResourceParams() {
        getParserForFile("samples/parser/invalidSyntax/ServiceWithoutResourceParams.bal").compilationUnit();
    }

    @Test(expectedExceptions = {ParseCancellationException.class },
            expectedExceptionsMessageRegExp = "Mismatched input '\\{' in MainFuncWithoutParams.bal\\[4:14\\]. " +
                "Expecting one of '\\('")
    public void testParseMainFuncWithoutParams() {
        getParserForFile("samples/parser/invalidSyntax/MainFuncWithoutParams.bal").compilationUnit();
    }
    
    @Test(expectedExceptions = {ParseCancellationException.class },
            expectedExceptionsMessageRegExp = "Mismatched input ';' in ResourceWithEmptyReply.bal\\[11:6\\]. " +
            "Expecting one of \\{'new', '\\(', '\\{', '\\[', '!', '\\+', '-', IntegerLiteral, FloatingPointLiteral, " +
            "BooleanLiteral, QuotedStringLiteral, BacktickStringLiteral, 'null', Identifier}")
    public void testResourceWithEmptyReply() {
        getParserForFile("samples/parser/invalidSyntax/ResourceWithEmptyReply.bal").compilationUnit();
    }
    
    
    private BallerinaParser getParserForFile(String path) {
        BallerinaParser ballerinaParser = ParserUtils.getBallerinaParser(path);

        // Create Ballerina model builder class
        BLangModelBuilder modelBuilder = new BLangModelBuilder();
        BLangAntlr4Listener langModelBuilder = new BLangAntlr4Listener(modelBuilder);

        ballerinaParser.addParseListener(langModelBuilder);
        ballerinaParser.setErrorHandler(new BallerinaParserErrorStrategy());
        return ballerinaParser;
    }
}

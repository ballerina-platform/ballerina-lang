/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.ballerina.core.parser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.exception.ParserException;
import org.wso2.ballerina.core.model.builder.BLangModelBuilder;
import org.wso2.ballerina.core.parser.antlr4.BLangAntlr4Listener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class InvalidSyntaxParserTest {

    private BallerinaParser ballerinaParser;

    @BeforeTest
    public void setup() {
        try {
            File file = new File(getClass().getClassLoader().getResource("samples/parser/InvalidSyntaxSample.bal")
                .getFile());
            ANTLRInputStream antlrInputStream = new ANTLRInputStream(new FileInputStream(file));
            antlrInputStream.name = file.getName();
            BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
            CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);

            BLangModelBuilder modelBuilder = new BLangModelBuilder();
            BLangAntlr4Listener langModelBuilder = new BLangAntlr4Listener(modelBuilder);

            ballerinaParser = new BallerinaParser(ballerinaToken);
            ballerinaParser.setErrorHandler(new BallerinaParserErrorStrategy());
            ballerinaParser.addParseListener(langModelBuilder);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
        }
    }

    @Test(expectedExceptions = ParserException.class)
    public void testParsingInvalidFile() {
        ballerinaParser.compilationUnit();
    }
}

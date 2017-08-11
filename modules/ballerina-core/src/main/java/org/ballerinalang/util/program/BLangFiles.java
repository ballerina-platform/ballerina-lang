/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.util.program;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BallerinaFile;
import org.ballerinalang.model.builder.BLangModelBuilder;
import org.ballerinalang.util.exceptions.ParserException;
import org.ballerinalang.util.parser.BallerinaLexer;
import org.ballerinalang.util.parser.BallerinaParser;
import org.ballerinalang.util.parser.BallerinaParserErrorStrategy;
import org.ballerinalang.util.parser.antlr4.BLangAntlr4Listener;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 */
public class BLangFiles {

    public static BallerinaFile loadFile(String sourceFileName,
                                         Path packagePath,
                                         InputStream inputStream,
                                         BLangPackage.PackageBuilder packageBuilder) {

        Path sourceFilePath;
        if (packagePath == null || packagePath.toString().equals(".")) {
            sourceFilePath = Paths.get(sourceFileName);
        } else {
            sourceFilePath = packagePath.resolve(sourceFileName);
        }

        try {
            ANTLRInputStream antlrInputStream = new ANTLRInputStream(inputStream);

            // Setting the name of the source file being parsed, to the ANTLR input stream.
            // This is required by the parser-error strategy.
            antlrInputStream.name = sourceFilePath.toString();

            BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
            CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);

            BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);
            ballerinaParser.setErrorHandler(new BallerinaParserErrorStrategy());

            BLangModelBuilder bLangModelBuilder = new BLangModelBuilder(packageBuilder, sourceFileName);
            BLangAntlr4Listener antlr4Listener = new BLangAntlr4Listener(bLangModelBuilder, sourceFilePath);
            ballerinaParser.addParseListener(antlr4Listener);
            ballerinaParser.compilationUnit();
            return bLangModelBuilder.build();

        } catch (IOException e) {
            throw new IllegalStateException("error in reading source file '" +
                    sourceFilePath + "': " + e.getMessage());

        } catch (ParseCancellationException e) {
            throw new ParserException(e.getMessage(), e);
        }
    }
}

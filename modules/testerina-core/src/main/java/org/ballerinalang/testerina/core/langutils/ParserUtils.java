/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.testerina.core.langutils;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.exception.LinkerException;
import org.wso2.ballerina.core.exception.SemanticException;
import org.wso2.ballerina.core.model.BLangPackage;
import org.wso2.ballerina.core.model.BLangProgram;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.GlobalScope;
import org.wso2.ballerina.core.model.builder.BLangModelBuilder;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.parser.BallerinaLexer;
import org.wso2.ballerina.core.parser.BallerinaParser;
import org.wso2.ballerina.core.parser.BallerinaParserErrorStrategy;
import org.wso2.ballerina.core.parser.antlr4.BLangAntlr4Listener;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Path;

import static org.ballerinalang.launcher.LauncherUtils.createUsageException;

/**
 * Utility methods for Ballerina Parser.
 *
 * @since 0.8.0
 */
public class ParserUtils {

    private ParserUtils() {
    }

    /**
     * Get parsed, analyzed and linked Ballerina object model.
     *
     * @param sourceFilePath Path to Bal file.
     * @return BallerinaFile instance.
     */
    public static BallerinaFile buildLangModel(Path sourceFilePath) {
        ANTLRInputStream antlrInputStream = getAntlrInputStream(sourceFilePath);

        try {
            // Setting the name of the source file being parsed, to the ANTLR input stream.
            // This is required by the parser-error strategy.
            antlrInputStream.name = getFileName(sourceFilePath);

            BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
            CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);

            BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);
            ballerinaParser.setErrorHandler(new BallerinaParserErrorStrategy());

            GlobalScope globalScope = GlobalScope.getInstance();
            loadGlobalSymbols(globalScope);
            BLangProgram bLangProgram = new BLangProgram(globalScope, BLangProgram.Category.SERVICE_PROGRAM);
            BLangPackage bLangPackage = new BLangPackage(bLangProgram);

            BLangModelBuilder bLangModelBuilder = null;
            //            new BLangModelBuilder(bLangPackage, getFileName(sourceFilePath));
            BLangAntlr4Listener ballerinaBaseListener = new BLangAntlr4Listener(bLangModelBuilder);
            ballerinaParser.addParseListener(ballerinaBaseListener);
            ballerinaParser.compilationUnit();
            BallerinaFile balFile = bLangModelBuilder.build();

            BuiltInNativeConstructLoader.loadConstructs(globalScope);

            //            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(balFile, bLangPackage);
            //            balFile.accept(semanticAnalyzer);

            return balFile;
        } catch (ParseCancellationException | SemanticException | LinkerException e) {
            throw new BallerinaException(e.getMessage());
        } catch (Throwable e) {
            throw new BallerinaException(getFileName(sourceFilePath) + ": " + e.getMessage());
        }
    }

    public static String getFileName(Path sourceFilePath) {
        Path fileNamePath = sourceFilePath.getFileName();
        return (fileNamePath != null) ? fileNamePath.toString() : sourceFilePath.toString();
    }

    private static ANTLRInputStream getAntlrInputStream(Path sourceFilePath) {
        try {
            InputStream inputStream = new FileInputStream(sourceFilePath.toFile());
            return new ANTLRInputStream(inputStream);
        } catch (FileNotFoundException e) {
            throw new BallerinaException("ballerina: no such file or directory '" + getFileName(sourceFilePath) + "'");
        } catch (Throwable e) {
            throw createUsageException(
                    "error reading file or directory'" + getFileName(sourceFilePath) + "': " + e.getMessage());
        }
    }

    private static void loadGlobalSymbols(GlobalScope globalScope) {
        BTypes.loadBuiltInTypes(globalScope);
    }

}

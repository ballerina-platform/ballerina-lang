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
package org.wso2.ballerina.core.utils;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.linker.BLangLinker;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.Symbol;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.builder.BLangModelBuilder;
import org.wso2.ballerina.core.model.util.SymbolUtils;
import org.wso2.ballerina.core.nativeimpl.lang.system.Log;
import org.wso2.ballerina.core.nativeimpl.lang.system.Println;
import org.wso2.ballerina.core.parser.BallerinaLexer;
import org.wso2.ballerina.core.parser.BallerinaParser;
import org.wso2.ballerina.core.parser.antlr4.BLangAntlr4Listener;
import org.wso2.ballerina.core.semantics.SemanticAnalyzer;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

public class TestUtils {

    public static BallerinaFile parseBalFile(String path) {
        URL fileResource = TestUtils.class.getClassLoader().getResource(path);
        if (fileResource == null) {
            throw new RuntimeException("Source file is not available: " + path);
        }

        ANTLRInputStream antlrInputStream = null;
        try {
            antlrInputStream = new ANTLRInputStream(
                    new FileInputStream(fileResource.getFile()));
        } catch (IOException e) {
            throw new RuntimeException("Unable read file: " + path, e);
        }

        BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
        CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);
        BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);

        // Create Ballerina model builder class
        BLangModelBuilder modelBuilder = new BLangModelBuilder();
        BLangAntlr4Listener langModelBuilder = new BLangAntlr4Listener(modelBuilder);

        ballerinaParser.addParseListener(langModelBuilder);
        ballerinaParser.compilationUnit();
//        ballerinaParser.setErrorHandler();

        // Get the model for source file
        BallerinaFile bFile = modelBuilder.build();

        // Analyze semantic properties of the source code
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(bFile);
        bFile.accept(semanticAnalyzer);


        // Create a global symbol scope
        SymScope symScope = new SymScope(null);


        Println println = new Println();
        SymbolName symbolName = SymbolUtils.generateSymbolName(
                "ballerina.lang.system:println",
                println.getParameters());

        Symbol symbol = new Symbol(println,
                SymbolUtils.getTypesOfParams(println.getParameters()), println.getReturnTypesC());

        symScope.insert(symbolName, symbol);

        Log log = new Log();
        symbolName = SymbolUtils.generateSymbolName(
                "ballerina.lang.system:log",
                log.getParameters());

        symbol = new Symbol(log,
                SymbolUtils.getTypesOfParams(log.getParameters()), log.getReturnTypesC());

        symScope.insert(symbolName, symbol);


        // Linker
        BLangLinker linker = new BLangLinker(bFile);
        linker.link(symScope);

        return bFile;
    }
}

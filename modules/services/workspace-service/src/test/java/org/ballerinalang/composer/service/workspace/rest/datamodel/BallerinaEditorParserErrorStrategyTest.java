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

package org.ballerinalang.composer.service.workspace.rest.datamodel;


import com.google.gson.JsonObject;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BallerinaFile;
import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.model.builder.BLangModelBuilder;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.natives.BuiltInNativeConstructLoader;
import org.ballerinalang.util.parser.BallerinaLexer;
import org.ballerinalang.util.parser.BallerinaParser;
import org.ballerinalang.util.parser.antlr4.BLangAntlr4Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Paths;

public class BallerinaEditorParserErrorStrategyTest {
    private static final Logger logger = LoggerFactory.getLogger(BallerinaEditorParserErrorStrategyTest.class);
    public static void main(String[] args) {
        try {
            File file = new File(BallerinaEditorParserErrorStrategyTest.class.getClassLoader()
                    .getResource("samples/parser/InvalidBal.bal").getFile());
            InputStream stream = new FileInputStream(file);
            ANTLRInputStream antlrInputStream = new ANTLRInputStream(stream);
            BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
            CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);

            BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);
            BallerinaComposerErrorStrategy errorStrategy = new BallerinaComposerErrorStrategy();
            ballerinaParser.setErrorHandler(errorStrategy);

            GlobalScope globalScope = GlobalScope.getInstance();
            BTypes.loadBuiltInTypes(globalScope);
            BLangPackage bLangPackage = new BLangPackage(globalScope);

            BLangPackage.PackageBuilder packageBuilder = new BLangPackage.PackageBuilder(bLangPackage);
            BLangModelBuilder bLangModelBuilder = new BLangModelBuilder(packageBuilder, StringUtils.EMPTY);

            BLangAntlr4Listener ballerinaBaseListener = new BLangAntlr4Listener(bLangModelBuilder, Paths.get(file.getAbsolutePath()));
            ballerinaParser.addParseListener(ballerinaBaseListener);
            ballerinaParser.compilationUnit();
            BallerinaFile bFile = bLangModelBuilder.build();

            BuiltInNativeConstructLoader.loadConstructs(globalScope);

            JsonObject response = new JsonObject();
            BLangJSONModelBuilder jsonModelBuilder = new BLangJSONModelBuilder(response);
            bFile.accept(jsonModelBuilder);

            String responseString = response.toString();
            logger.info(responseString);
        } catch (Exception ex) {
            String error = ex.getMessage();
            logger.error(error);
        }
    }
}

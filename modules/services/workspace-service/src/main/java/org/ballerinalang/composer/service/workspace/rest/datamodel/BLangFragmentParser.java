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
package org.ballerinalang.composer.service.workspace.rest.datamodel;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.model.BLangPackage;
import org.ballerinalang.model.BallerinaFile;
import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.util.parser.BallerinaLexer;
import org.ballerinalang.util.parser.BallerinaParser;
import org.ballerinalang.util.parser.antlr4.BLangAntlr4Listener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Utility for parsing BLang source fragments
 */
public class BLangFragmentParser {

    private static final Logger logger = LoggerFactory.getLogger(BLangFragmentParser.class);

    public static String parseFragment(BLangSourceFragment sourceFragment) {
        try {
            String parsableString = getParsableString(sourceFragment);
            JsonObject jsonModel = getJsonModel(parsableString);
            JsonObject jsonNodeForFragment = getJsonNodeForFragment(jsonModel, sourceFragment);
            return jsonNodeForFragment.toString();
        } catch (Exception e) {
            logger.error("Error while parsing BLang fragment.", e);
            JsonObject errObj = new JsonObject();
            errObj.addProperty("error", e.getMessage());
            return errObj.toString();
        }
    }

    protected static JsonObject getJsonNodeForFragment(JsonObject jsonModel, BLangSourceFragment fragment) {
        JsonObject fragmentNode = null;
        JsonArray jsonArray = jsonModel.getAsJsonArray(BLangJSONModelConstants.ROOT);
        JsonObject functionObj = jsonArray.get(1).getAsJsonObject(); // 0 is package def
        switch (fragment.getExpectedNodeType()) {
            case BLangFragmentParserConstants.EXPRESSION :
                // 0 & 1 are function args and return types, 2 is the var def stmt
                JsonObject varDef = functionObj.getAsJsonArray(BLangJSONModelConstants.CHILDREN)
                        .get(2).getAsJsonObject();
                // 0th child is the var ref expression of var def stmt
                fragmentNode = varDef.getAsJsonArray(BLangJSONModelConstants.CHILDREN)
                    .get(1).getAsJsonObject();
                break;
            case BLangFragmentParserConstants.STATEMENT :
                // 0 & 1 are function args and return types, 2 is the statement came from source fragment
                fragmentNode = functionObj.getAsJsonArray(BLangJSONModelConstants.CHILDREN).get(2).getAsJsonObject();
                break;
            default: fragmentNode = new JsonObject();
                     fragmentNode.addProperty("error", "cannot find node for given fragment");
        }
        return fragmentNode;
    }

    protected static JsonObject getJsonModel(String source) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(source.getBytes(StandardCharsets.UTF_8));
        ANTLRInputStream antlrInputStream = new ANTLRInputStream(inputStream);
        BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
        CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);

        BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);
        BallerinaComposerErrorStrategy errorStrategy = new BallerinaComposerErrorStrategy();
        ballerinaParser.setErrorHandler(errorStrategy);

        GlobalScope globalScope = GlobalScope.getInstance();
        BTypes.loadBuiltInTypes(globalScope);
        BLangPackage bLangPackage = new BLangPackage(globalScope);
        BLangPackage.PackageBuilder packageBuilder = new BLangPackage.PackageBuilder(bLangPackage);
        BallerinaComposerModelBuilder bLangModelBuilder = new BallerinaComposerModelBuilder(packageBuilder,
                StringUtils.EMPTY);
        BLangAntlr4Listener ballerinaBaseListener = new BLangAntlr4Listener(true, ballerinaToken, bLangModelBuilder,
                new File("temp/untitled").toPath());
        ballerinaParser.addParseListener(ballerinaBaseListener);
        ballerinaParser.compilationUnit();
        BallerinaFile bFile = bLangModelBuilder.build();

        JsonObject jsonModelRoot = new JsonObject();
        BLangJSONModelBuilder jsonModelBuilder = new BLangJSONModelBuilder(jsonModelRoot);
        bFile.accept(jsonModelBuilder);

        return jsonModelRoot;
    }

    protected static String getParsableString(BLangSourceFragment sourceFragment) {
        String parsableText = null;
        switch (sourceFragment.getExpectedNodeType()) {
            case BLangFragmentParserConstants.EXPRESSION :
                    parsableText = getFromTemplate(
                            BLangFragmentParserConstants.VAR_DEF_STMT_EXPR_WRAPPER, sourceFragment.getSource());
                    break;
            case BLangFragmentParserConstants.STATEMENT :
                    parsableText = getFromTemplate(
                            BLangFragmentParserConstants.FUNCTION_BODY_STMT_WRAPPER, sourceFragment.getSource());
                    break;
            default: parsableText = "";
        }
        return parsableText;
    }

    protected static String getFromTemplate(String template, String source) {
        return template.replace(BLangFragmentParserConstants.FRAGMENT_PLACE_HOLDER, source);
    }
}


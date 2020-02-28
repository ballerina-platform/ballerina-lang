/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaLexer;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

/**
 * Subtree Parser for Parsing selected erroneous statements in order to identify the Parser Rule Context for Error. 
 */
public class CompletionSubRuleParser {
    private static BallerinaParser getParser(LSContext context, String rule) {
        ANTLRInputStream inputStream = new ANTLRInputStream(rule);
        BallerinaLexer lexer = new BallerinaLexer(inputStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        BallerinaParser parser = new BallerinaParser(commonTokenStream);
        parser.setErrorHandler(new SubRuleParserErrorStrategy(context));
        
        return parser;
    }

    public static void parseWithinCompilationUnit(String subRule, LSContext context) {
        getParser(context, subRule).compilationUnit();
    }

    public static void parseWithinFunctionDefinition(String subRule, LSContext context) {
        String functionRule = "function testFunction () {" + CommonUtil.LINE_SEPARATOR + "\t" + subRule +
                CommonUtil.LINE_SEPARATOR + "}";
        getParser(context, functionRule).functionDefinition();
    }

    public static void parseWithinObjectTypeDefinition(String subRule, LSContext context) {
        String functionRule = "type testObject object {" + CommonUtil.LINE_SEPARATOR + "\t" + subRule +
                CommonUtil.LINE_SEPARATOR + "};";
        getParser(context, functionRule).typeDefinition();
    }

    public static void parseWithinServiceDefinition(String subRule, LSContext context) {
        String functionRule = "service testService on new http:Listener(8080) {" + CommonUtil.LINE_SEPARATOR + "\t"
                + subRule + CommonUtil.LINE_SEPARATOR + "}";
        getParser(context, functionRule).serviceDefinition();
    }
}

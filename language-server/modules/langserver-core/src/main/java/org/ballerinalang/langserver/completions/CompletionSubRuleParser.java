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
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.AnnotationNodeKind;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaLexer;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.stream.Collectors;

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

    public static void parseWithinServiceDefinition(String subRule, LSContext context) {
        if (!isCursorWithinAnnotationContext(context)) {
            String functionRule = "service testService on new http:Listener(8080) {" + CommonUtil.LINE_SEPARATOR + "\t"
                    + subRule + CommonUtil.LINE_SEPARATOR + "}";
            getParser(context, functionRule).serviceDefinition();
        }
    }
    
    // Utility methods
    
    private static AnnotationNodeKind getNextNodeTypeFlag(TokenStream tokenStream, int index, LSContext context) {
        AnnotationNodeKind nodeType = context.get(CompletionKeys.NEXT_NODE_KEY);
        Map<String, AnnotationNodeKind> flagsMap = new HashMap<>();
        flagsMap.put(UtilSymbolKeys.SERVICE_KEYWORD_KEY, AnnotationNodeKind.SERVICE);
        flagsMap.put(UtilSymbolKeys.RESOURCE_KEYWORD_KEY, AnnotationNodeKind.RESOURCE);
        
        while (true) {
            if (tokenStream == null || index >= tokenStream.size()) {
                break;
            }
            Optional<Token> token = CommonUtil.getNextDefaultToken(tokenStream, index);
            if (!token.isPresent()) {
                break;
            }
            if (token.get().getText().equals(UtilSymbolKeys.SEMI_COLON_SYMBOL_KEY)) {
                break;
            } else if (flagsMap.containsKey(token.get().getText())) {
                nodeType = flagsMap.get(token.get().getText());
                break;
            }
            index = token.get().getTokenIndex();
        }
        
        return nodeType;
    }
    
    private static boolean isCursorWithinAnnotationContext(LSContext ctx) {
        boolean withinAnnotationContext = false;

        TokenStream tokenStream = ctx.get(CompletionKeys.TOKEN_STREAM_KEY);
        if (tokenStream != null) {
            int currentTokenIndex = CommonUtil.getCurrentTokenFromTokenStream(ctx);
            List<Token> poppedTokens = CommonUtil.getNDefaultTokensToLeft(tokenStream, 4, currentTokenIndex + 1);
            List<String> poppedTokenStrings = poppedTokens
                    .stream()
                    .map(Token::getText)
                    .collect(Collectors.toList());
            if (poppedTokenStrings.contains(UtilSymbolKeys.ANNOTATION_START_SYMBOL_KEY)) {
                Stack<Token> poppedTokenStack = new Stack<>();
                poppedTokenStack.addAll(poppedTokens);
                ctx.put(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY, poppedTokenStack);
                ctx.put(CompletionKeys.NEXT_NODE_KEY, getNextNodeTypeFlag(tokenStream, currentTokenIndex, ctx));
                withinAnnotationContext = true;
            } else {
                withinAnnotationContext = false;
            }
        }
        return withinAnnotationContext;
    }
}

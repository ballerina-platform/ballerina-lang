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
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.completion.AnnotationAttachmentMetaInfo;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaLexer;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * Subtree Parser for Parsing selected erroneous statements in order to identify the Parser Rule Context for Error. 
 */
public class CompletionSubRuleParser {
    
    private static List<Class> moderateContextTypes;
    
    static {
        moderateContextTypes = new ArrayList<>();
        List<Method> methods = new ArrayList<>();
        Class<?> statementContextClass = BallerinaParser.StatementContext.class;
        Class<?> compilationUnitContextClass = BallerinaParser.CompilationUnitContext.class;
        Class<?> definitionContextClass = BallerinaParser.DefinitionContext.class;
        methods.addAll(Arrays.asList(statementContextClass.getDeclaredMethods()));
        methods.addAll(Arrays.asList(compilationUnitContextClass.getDeclaredMethods()));
        methods.addAll(Arrays.asList(definitionContextClass.getDeclaredMethods()));
        for (Method method : methods) {
            Class returnType = method.getReturnType();
            if (ParserRuleContext.class.isAssignableFrom(returnType)
                    && !returnType.equals(BallerinaParser.DefinitionContext.class)) {
                moderateContextTypes.add(returnType);
            }
        }
    }
    
    public static void parse(LSContext context) {
        List<String> poppedTokens = CommonUtil.popNFromList(CommonUtil.getPoppedTokenStrings(context), 4);
        
        // If the deleted tokens contains annotation start (@) symbol, ignore the parser rule context.
        // We can be sure that the writing start is for annotation attachment.
        // Relevant symbol environment node will handle this properly
        if (poppedTokens.contains(UtilSymbolKeys.ANNOTATION_START_SYMBOL_KEY)) {
            // In the ideal scenario, next node key should be populated at the symbol visitor.
            // If not (Parser unable to build the next node and not available in the bLangPackage) get it from tokens
            // Sometimes the node key can be null and for a special case of resource, node type can be invalid (need a
            // fix in the custom error strategy)
            String nextNodeType = getNextNodeTypeString(context.get(CompletionKeys.TOKEN_STREAM_KEY),
                        CommonUtil.getCurrentTokenFromTokenStream(context), context);
            context.put(CompletionKeys.NEXT_NODE_KEY, nextNodeType);
            return;
        }
        BLangNode symbolEnvNode = context.get(CompletionKeys.SYMBOL_ENV_NODE_KEY);
        if (symbolEnvNode == null || symbolEnvNode instanceof BLangPackage) {
            // Parse with compilation unit context
            parseWithinCompilationUnit(context);
        } else if (symbolEnvNode instanceof BLangBlockStmt) {
            // Parse with function definition context
            parseWithinFunctionDefinition(context);
        } else if (symbolEnvNode instanceof BLangService) {
            // Parse with service definition context
            parseWithinServiceDefinition(context);
        }

        moderateParserRuleContext(context);
    }
    
    private static BallerinaParser getParser(LSContext context, String rule) {
        ANTLRInputStream inputStream = new ANTLRInputStream(rule);
        BallerinaLexer lexer = new BallerinaLexer(inputStream);
        CommonTokenStream commonTokenStream = new CommonTokenStream(lexer);
        BallerinaParser parser = new BallerinaParser(commonTokenStream);
        parser.setErrorHandler(new SubRuleParserErrorStrategy(context));
        
        return parser;
    }

    private static void parseWithinCompilationUnit(LSContext context) {
        List<String> poppedTokens = context.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY)
                .stream()
                .map(Token::getText)
                .collect(Collectors.toList());
        // If the popped token list contains the import declaration,
        // then make the parser rule context import declaration without parsing.
        // Note: "import ba" can be identified as global variable definition by parser
        if (poppedTokens.contains(UtilSymbolKeys.IMPORT_KEYWORD_KEY)) {
            context.put(CompletionKeys.PARSER_RULE_CONTEXT_KEY, new BallerinaParser.ImportDeclarationContext(null, 0));
        } else if (!isCursorWithinAnnotationContext(context)) {
            // If for any completion error strategy issue popped tokens are empty, we recalculate
            if (poppedTokens.isEmpty()) {
                reCalculatePoppedTokensForTopLevel(context);
                poppedTokens = context.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY)
                        .stream()
                        .map(Token::getText)
                        .collect(Collectors.toList());
            }
            String rule = getCombinedTokenString(context);
            getParser(context, rule).compilationUnit();
            // This is to alter the parser rule context found in the parser, when it is service definition
            // and incorrectly identified as globalVariableDefCtx
            if (!poppedTokens.isEmpty() && poppedTokens.get(0).equals(UtilSymbolKeys.SERVICE_KEYWORD_KEY)
                    && context.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY)
                    instanceof BallerinaParser.GlobalVariableDefinitionContext) {
                context.put(CompletionKeys.PARSER_RULE_CONTEXT_KEY,
                        new BallerinaParser.ServiceDefinitionContext(null, 0));
            }
        }
    }

    private static void parseWithinFunctionDefinition(LSContext context) {
        String tokenString = getCombinedTokenString(context);
        String functionRule = "function testFunction () {" + CommonUtil.LINE_SEPARATOR + "\t" + tokenString +
                CommonUtil.LINE_SEPARATOR + "}";
        getParser(context, functionRule).functionDefinition();
    }

    private static void parseWithinServiceDefinition(LSContext context) {
        if (!isCursorWithinAnnotationContext(context)) {
            String tokenString = getCombinedTokenString(context);
            String functionRule = "service<http:Service> serviceName {" + CommonUtil.LINE_SEPARATOR + "\t"
                    + tokenString + CommonUtil.LINE_SEPARATOR + "}";
            getParser(context, functionRule).serviceDefinition();
        }
    }

    /**
     * When the Antlr grasps a parser rule context we need to moderate the context.
     * This is because contexts below expression or statement is too fine grain for completion.
     * Therefore we grab the most recent/suitable expression/ statement.
     * 
     * Note: If within a statement or expression resolver we need more fine grain parsing based on the parser rule
     * context, we can use the children rules for computations.
     * 
     * @param context   Language Server Completion Context
     */
    private static void moderateParserRuleContext(LSContext context) {
        ParserRuleContext parserRuleContext = context.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY);
        if (parserRuleContext == null) {
            return;
        }
        
        while (true) {
            if (parserRuleContext == null
                    || parserRuleContext.getClass().equals(BallerinaParser.ExpressionContext.class)
                    || moderateContextTypes.contains(parserRuleContext.getClass())
                    || parserRuleContext instanceof BallerinaParser.EndpointDeclarationContext
                    || parserRuleContext instanceof BallerinaParser.StatementContext
                    || parserRuleContext instanceof BallerinaParser.DefinitionContext) {
                context.put(CompletionKeys.PARSER_RULE_CONTEXT_KEY, parserRuleContext);
                break;
            }
            parserRuleContext = parserRuleContext.getParent();
        }
    }
    
    private static String getCombinedTokenString(LSContext context) {
        return String.join(" ", CommonUtil.getPoppedTokenStrings(context));
    }
    
    // Utility methods
    
    private static boolean fillAnnotationAttachmentMetaInfo(LSContext ctx) {
        if (ctx.get(CompletionKeys.TOKEN_STREAM_KEY) == null) {
            return false;
        }
        TokenStream tokenStream = ctx.get(CompletionKeys.TOKEN_STREAM_KEY);
        Position cursorPosition = ctx.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        Queue<String> fieldsQueue = new LinkedList<>();
        ArrayList<String> terminalTokens = new ArrayList<>(Arrays.asList(
                UtilSymbolKeys.FUNCTION_KEYWORD_KEY,
                UtilSymbolKeys.SERVICE_KEYWORD_KEY,
                UtilSymbolKeys.ENDPOINT_KEYWORD_KEY,
                UtilSymbolKeys.OPEN_BRACKET_KEY)
        );
        int currentTokenIndex = CommonUtil.getCurrentTokenFromTokenStream(ctx);
        int startIndex = currentTokenIndex;
        int line = cursorPosition.getLine();
        int col = cursorPosition.getCharacter();
        String annotationName;
        String pkgAlias = "";

        while (true) {
            if (startIndex > tokenStream.size()
                    || CommonUtil.getPreviousDefaultToken(tokenStream, startIndex) == null) {
                return false;
            }
            Token token = CommonUtil.getPreviousDefaultToken(tokenStream, startIndex);
            if (token == null || terminalTokens.contains(token.getText())) {
                return false;
            }
            if (token.getText().equals(UtilSymbolKeys.ANNOTATION_START_SYMBOL_KEY)) {
                // Breaks when we meet the first annotation start before the given start index
                break;
            } else if (token.getText().equals(UtilSymbolKeys.OPEN_BRACE_KEY)) {
                startIndex = token.getTokenIndex();
                // For each annotation found, capture the package alias
                if (UtilSymbolKeys.PKG_DELIMITER_KEYWORD
                        .equals(CommonUtil.getNthDefaultTokensToLeft(tokenStream, startIndex, 2).getText())) {
                    pkgAlias = CommonUtil.getNthDefaultTokensToLeft(tokenStream, startIndex, 3).getText();
                }
            } else {
                startIndex = token.getTokenIndex();
            }
        }

        String tempTokenString = "";
        while (true) {
            if (startIndex > tokenStream.size()) {
                return false;
            }
            Token token = CommonUtil.getNextDefaultToken(tokenStream, startIndex);
            int tokenLine = token.getLine() - 1;
            int tokenCol = token.getCharPositionInLine();
            if (terminalTokens.contains(token.getText())
                    || line < tokenLine
                    || (line == tokenLine - 1 && col - 1 < tokenCol)) {
                break;
            } else if (UtilSymbolKeys.OPEN_BRACE_KEY.equals(token.getText())) {
                fieldsQueue.add(tempTokenString);
            } else if (UtilSymbolKeys.CLOSE_BRACE_KEY.equals(token.getText())) {
                fieldsQueue.remove();
            } else if (!UtilSymbolKeys.PKG_DELIMITER_KEYWORD.equals(token.getText())) {
                tempTokenString = token.getText();
            }
            startIndex = token.getTokenIndex();
        }

        annotationName = fieldsQueue.poll();
        ctx.put(CompletionKeys.ANNOTATION_ATTACHMENT_META_KEY,
                new AnnotationAttachmentMetaInfo(annotationName, fieldsQueue, pkgAlias,
                        getNextNodeTypeString(tokenStream, currentTokenIndex, ctx)));
        ctx.put(CompletionKeys.SYMBOL_ENV_NODE_KEY, new BLangAnnotationAttachment());
        
        return true;
    }
    
    private static String getNextNodeTypeString(TokenStream tokenStream, int index, LSContext context) {
        String nodeType = context.get(CompletionKeys.NEXT_NODE_KEY);
        List<String> nodeTypes = new ArrayList<>(Arrays.asList(
                UtilSymbolKeys.SERVICE_KEYWORD_KEY,
                UtilSymbolKeys.FUNCTION_KEYWORD_KEY,
                UtilSymbolKeys.ENDPOINT_KEYWORD_KEY)
        );
        
        while (true) {
            if (tokenStream == null || index >= tokenStream.size()) {
                break;
            }
            Token token = CommonUtil.getNextDefaultToken(tokenStream, index);
            if (token.getText().equals(UtilSymbolKeys.SEMI_COLON_SYMBOL_KEY)) {
                break;
            } else if (nodeTypes.contains(token.getText())) {
                if (token.getText().equals(UtilSymbolKeys.ENDPOINT_KEYWORD_KEY)
                        && CommonUtil.getPreviousDefaultToken(tokenStream, token.getTokenIndex()).getText()
                        .equals(UtilSymbolKeys.OPEN_BRACKET_KEY)) {
                    nodeType = UtilSymbolKeys.RESOURCE_KEYWORD_KEY;
                } else {
                    nodeType = token.getText();
                }
                break;
            }
            index = token.getTokenIndex();
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
                ctx.put(CompletionKeys.NEXT_NODE_KEY, getNextNodeTypeString(tokenStream, currentTokenIndex, ctx));
                withinAnnotationContext = true;
            } else {
                withinAnnotationContext = fillAnnotationAttachmentMetaInfo(ctx);
            }
        }
        return withinAnnotationContext;
    }
    
    private static void reCalculatePoppedTokensForTopLevel(LSContext ctx) {
        if (ctx.get(CompletionKeys.TOKEN_STREAM_KEY) == null) {
            return;
        }
        int currentTokenIndex = CommonUtil.getCurrentTokenFromTokenStream(ctx);
        TokenStream tokenStream = ctx.get(CompletionKeys.TOKEN_STREAM_KEY);
        Stack<Token> tokenStack = new Stack<>();
        while (true) {
            if (currentTokenIndex < 0) {
                break;
            }
            Token token = CommonUtil.getPreviousDefaultToken(tokenStream, currentTokenIndex);
            if (token == null) {
                return;
            }
            String tokenString = token.getText();
            tokenStack.push(token);
            if (tokenString.equals(ItemResolverConstants.SERVICE)
                    || tokenString.equals(ItemResolverConstants.FUNCTION)
                    || tokenString.equals(ItemResolverConstants.TYPE)
                    || tokenString.equals(ItemResolverConstants.ENDPOINT)) {
                break;
            }
            currentTokenIndex = token.getTokenIndex();
        }
        Collections.reverse(tokenStack);
        ctx.put(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY, tokenStack);
    }
}

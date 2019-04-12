/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.resolvers;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.CompletionKeys;
import org.ballerinalang.langserver.completions.util.CompletionItemResolver;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a Completion Item Context.
 */
public enum CompletionItemsContext {
    TOP_LEVEL,
    STATEMENT,
    SERVICE,
    RESOURCE,
    PARAMETER,
    PACKAGE_NAME,
    OBJECT_TYPE,
    FUNCTION,
    ENDPOINT_DECLARATION,
    BLOCK_STATEMENT,
    RECORD_LITERAL,
    RECORD,
    MATCH_EXPRESSION,
    MATCH,
    ANNOTATION_ATTACHMENT,
    PR_WORKER_REPLY,
    PR_WORKER_INTERACTION,
    PR_WORKER_TRIGGER_WORKER_STMT,
    PR_WORKER_STMT,
    PR_SERVICE_ENDPOINT_ATTACHMENT,
    PR_SERVICE_DEFINITION,
    PR_RETURN_STMT_DEFINITION,
    PR_PANIC_STATEMENT,
    PR_MATCH_STATEMENT,
    PR_GLOBAL_VARIABLE_DEFINITION,
    PR_FUNCTION_DEFINITION,
    PR_EXPRESSION,
    PR_DEFINITION,
    PR_CONDITIONAL_CLAUSE,
    PR_ATTACHMENT_POINT,
    PR_ASSIGNMENT_STATEMENT,
    PR_ANNOTATION_ATTACHMENT,
    PR_ENDPOINT_DECLARATION,
    PR_STATEMENT,
    PR_VARIABLE_DEFINITION,
    ;

    /**
     * Resolves into the finest completion context.
     *
     * @param ctx {@link LSContext}
     * @return {@link CompletionItemsContext}
     */
    public CompletionItemsContext resolve(LSContext ctx) {
        ParserRuleContext prCtx = ctx.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY);
        ParserRuleContext contextParent = (prCtx != null) ? prCtx.getParent() : null;
        List<String> poppedTokens = CommonUtil.getPoppedTokenStrings(ctx);
        switch (this) {
            case TOP_LEVEL:
                CompletionItemsContext resolver = prCtx == null ? this : CompletionItemResolver.get(
                        prCtx.getClass(), ctx);
                if (isAnnotationStart(ctx)) {
                    return CompletionItemResolver.get(BallerinaParser.AnnotationAttachmentContext.class, ctx);
                }
                return resolver;
            case SERVICE:
                if (isAnnotationStart(ctx)) {
                    return CompletionItemResolver.get(BallerinaParser.AnnotationAttachmentContext.class, ctx);
                } else if ((prCtx == null
                        || prCtx instanceof BallerinaParser.ObjectFieldDefinitionContext)
                        && poppedTokens.contains(UtilSymbolKeys.EQUAL_SYMBOL_KEY)) {
                    return CompletionItemResolver.get(BallerinaParser.VariableDefinitionStatementContext.class, ctx);
                }
                return this;
            case RESOURCE:
                if (prCtx != null) {
                    return CompletionItemResolver.get(prCtx.getClass(), ctx);
                }
                return this;
            case OBJECT_TYPE:
                if (poppedTokens.contains(UtilSymbolKeys.EQUAL_SYMBOL_KEY)) {
                    // If the popped tokens contains the equal symbol, then the variable definition is being writing
                    ctx.put(CompletionKeys.PARSER_RULE_CONTEXT_KEY,
                            new BallerinaParser.VariableDefinitionStatementContext(null, -1));
                    return CompletionItemResolver
                            .get(BallerinaParser.VariableDefinitionStatementContext.class, ctx);

                }
                return this;
            case FUNCTION:
                if (prCtx == null) {
                    return this;
                }
                CompletionItemsContext contextResolver = CompletionItemResolver.get(prCtx.getClass(), ctx);
                return (contextResolver == null) ? this : contextResolver;
            case BLOCK_STATEMENT:
                ParserRuleContext parserRuleContext = ctx.get(CompletionKeys.PARSER_RULE_CONTEXT_KEY);
                if (parserRuleContext != null) {
                    return CompletionItemResolver.get(parserRuleContext.getClass(), ctx);
                } else {
                    return CompletionItemResolver.get(BLangStatement.class, ctx);
                }
            case RECORD:
                if (poppedTokens.contains(UtilSymbolKeys.EQUAL_SYMBOL_KEY)) {
                    // If the popped tokens contains the equal symbol, then the variable definition is being writing
                    // This parser rule context is used to select the proper sorter.
                    ctx.put(CompletionKeys.PARSER_RULE_CONTEXT_KEY,
                            new BallerinaParser.VariableDefinitionStatementContext(null, -1));
                    return CompletionItemResolver.get(BallerinaParser.VariableDefinitionStatementContext.class, ctx);
                }
                return this;
            case PR_WORKER_INTERACTION:
                if (contextParent instanceof BallerinaParser.BinaryEqualExpressionContext) {
                    contextParent = contextParent.getParent();
                }
                if (contextParent != null) {
                    return CompletionItemResolver.get(contextParent.getClass(), ctx);
                }
                return this;
            case PR_WORKER_STMT:
                if (isInvocationOrInteractionOrFieldAccess(ctx)) {
                    return this;
                }
                return CompletionItemResolver.get(BLangStatement.class, ctx);
            case PR_RETURN_STMT_DEFINITION:
                return CompletionItemResolver.get(BallerinaParser.VariableDefinitionStatementContext.class, ctx);
            case PR_GLOBAL_VARIABLE_DEFINITION:
                List<String> consumedTokens = ctx.get(CompletionKeys.FORCE_CONSUMED_TOKENS_KEY).stream()
                        .map(Token::getText)
                        .collect(Collectors.toList());
                if (consumedTokens.get(0).equals(UtilSymbolKeys.FUNCTION_KEYWORD_KEY)) {
                    return CompletionItemResolver.get(BallerinaParser.DefinitionContext.class, ctx);
                }
                return this;
            case PR_EXPRESSION:
                if (contextParent instanceof BallerinaParser.BinaryEqualExpressionContext
                        || contextParent instanceof BallerinaParser.ExpressionListContext) {
                    contextParent = contextParent.getParent();
                }
                if (contextParent != null) {
                    return CompletionItemResolver.get(contextParent.getClass(), ctx);
                }
                return this;
            case PR_ASSIGNMENT_STATEMENT:
                return CompletionItemResolver.get(BallerinaParser.VariableDefinitionStatementContext.class, ctx);
            case PR_STATEMENT:
                if (isInvocationOrInteractionOrFieldAccess(ctx)) {
                    return this;
                }
                return CompletionItemResolver.get(BLangStatement.class, ctx);
            case STATEMENT:
            case PARAMETER:
            case PACKAGE_NAME:
            case RECORD_LITERAL:
            case MATCH_EXPRESSION:
            case MATCH:
            case ANNOTATION_ATTACHMENT:
            case PR_WORKER_REPLY:
            case PR_WORKER_TRIGGER_WORKER_STMT:
            case PR_SERVICE_ENDPOINT_ATTACHMENT:
            case PR_SERVICE_DEFINITION:
            case PR_PANIC_STATEMENT:
            case PR_MATCH_STATEMENT:
            case PR_FUNCTION_DEFINITION:
            case PR_DEFINITION:
            case PR_CONDITIONAL_CLAUSE:
            case PR_ATTACHMENT_POINT:
            case PR_ANNOTATION_ATTACHMENT:
            case PR_ENDPOINT_DECLARATION:
            case PR_VARIABLE_DEFINITION:
                return this;
        }
        return null;
    }

    /**
     * Check whether the token stream contains an annotation start (@).
     *
     * @param ctx Completion operation context
     * @return {@link Boolean}      Whether annotation context start or not
     */
    private boolean isAnnotationStart(LSContext ctx) {
        List<String> poppedTokens = CommonUtil.popNFromList(CommonUtil.getPoppedTokenStrings(ctx), 4);
        return poppedTokens.contains(UtilSymbolKeys.ANNOTATION_START_SYMBOL_KEY);
    }

    /**
     * Check whether the token stream corresponds to a action invocation or a function invocation.
     *
     * @param context Completion operation context
     * @return {@link Boolean}      Whether invocation or Field Access
     */
    private boolean isInvocationOrInteractionOrFieldAccess(LSContext context) {
        List<String> poppedTokens = CommonUtil.popNFromList(CommonUtil.getPoppedTokenStrings(context), 2);
        return poppedTokens.contains(UtilSymbolKeys.DOT_SYMBOL_KEY)
                || poppedTokens.contains(UtilSymbolKeys.PKG_DELIMITER_KEYWORD)
                || poppedTokens.contains(UtilSymbolKeys.RIGHT_ARROW_SYMBOL_KEY)
                || poppedTokens.contains(UtilSymbolKeys.LEFT_ARROW_SYMBOL_KEY)
                || poppedTokens.contains(UtilSymbolKeys.BANG_SYMBOL_KEY);
    }
}

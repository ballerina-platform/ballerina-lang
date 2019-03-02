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
package org.ballerinalang.langserver.completions.util;

import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.resolvers.AbstractItemContext;
import org.ballerinalang.langserver.completions.resolvers.BLangAnnotationAttachmentContext;
import org.ballerinalang.langserver.completions.resolvers.BLangMatchContext;
import org.ballerinalang.langserver.completions.resolvers.BLangMatchExpressionContext;
import org.ballerinalang.langserver.completions.resolvers.BLangRecordContext;
import org.ballerinalang.langserver.completions.resolvers.BLangRecordLiteralContext;
import org.ballerinalang.langserver.completions.resolvers.BlockStatementContextContext;
import org.ballerinalang.langserver.completions.resolvers.CompletionItemsContext;
import org.ballerinalang.langserver.completions.resolvers.FunctionContext;
import org.ballerinalang.langserver.completions.resolvers.ObjectTypeContext;
import org.ballerinalang.langserver.completions.resolvers.PackageNameContext;
import org.ballerinalang.langserver.completions.resolvers.ParameterContext;
import org.ballerinalang.langserver.completions.resolvers.ServiceContext;
import org.ballerinalang.langserver.completions.resolvers.StatementContext;
import org.ballerinalang.langserver.completions.resolvers.TopLevelContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleAnnotationAttachmentContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleAssignmentStatementContextContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleAttachmentPointContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleConditionalClauseContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleDefinitionContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleExpressionContextContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleFunctionDefinitionContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleGlobalVariableDefinitionContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleMatchStatementContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRulePanicStatementContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleReturnStatementContextContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleServiceDefinitionContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleStatementContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleVariableDefinitionStatementContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleWorkerReplyContext;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangTestablePackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangMatchExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum for the completion item resolvers.
 */
public enum CompletionItemResolver {
    STATEMENT_CONTEXT(StatementContext.class,
                      new StatementContext()),
    PACKAGE_LEVEL_CONTEXT(BLangPackage.class,
                          new TopLevelContext()),
    TESTABLE_PACKAGE_LEVEL_CONTEXT(BLangTestablePackage.class,
                                   new TopLevelContext()),
    PACKAGE_NAME_CONTEXT(BallerinaParser.PackageNameContext.class,
                         new PackageNameContext()),
    IMPORT_DECLARATION_CONTEXT(BallerinaParser.ImportDeclarationContext.class,
                               new PackageNameContext()),
    PARAMETER_CONTEXT(BallerinaParser.ParameterContext.class,
                      new ParameterContext()),
    PARAMETER_LIST_CONTEXT(BallerinaParser.ParameterListContext.class,
                           new ParameterContext()),
    BLOCK_STATEMENT_CONTEXT(BLangBlockStmt.class,
                            new BlockStatementContextContext()),
    ANNOTATION_ATTACHMENT(BallerinaParser.AnnotationAttachmentContext.class,
                          new ParserRuleAnnotationAttachmentContext()),
    RECORD_CONTEXT(BLangRecordTypeNode.class,
                   new BLangRecordContext()),
    SERVICE_CONTEXT(BLangService.class,
                    new ServiceContext()),
    FUNCTION_DEF_CONTEXT(BLangFunction.class,
                         new FunctionContext()),
    OBJECT_TYPE_CONTEXT(BLangObjectTypeNode.class,
                        new ObjectTypeContext()),
    RECORD_LITERAL_CONTEXT(BLangRecordLiteral.class,
                           new BLangRecordLiteralContext()),
    MATCH_STATEMENT_CONTEXT(BLangMatch.class,
                            new BLangMatchContext()),
    MATCH_EXPRESSION_CONTEXT(BLangMatchExpression.class,
                             new BLangMatchExpressionContext()),
    BLANG_ANNOTATION_ATTACHMENT_CONTEXT(BLangAnnotationAttachment.class,
                                        new BLangAnnotationAttachmentContext()),

    PARSER_RULE_STATEMENT_CONTEXT(BallerinaParser.StatementContext.class,
                                  new ParserRuleStatementContext()),
    PARSER_RULE_VAR_DEF_STMT_CONTEXT(BallerinaParser.VariableDefinitionStatementContext.class,
                                     new ParserRuleVariableDefinitionStatementContext()),
    // todo we have removed this from the grammar
//    PARSER_RULE_TRIGGER_WORKER_CONTEXT(BallerinaParser.TriggerWorkerContext.class,
//            new ParserRuleTriggerWorkerContext()),
    PARSER_RULE_WORKER_REPLY_CONTEXT(BallerinaParser.WorkerReceiveExpressionContext.class,
            new ParserRuleWorkerReplyContext()),
    PARSER_RULE_GLOBAL_VAR_DEF_CONTEXT(BallerinaParser.GlobalVariableDefinitionContext.class,
                                       new ParserRuleGlobalVariableDefinitionContext()),
    PARSER_RULE_ATTACHMENT_POINT_CONTEXT(BallerinaParser.AttachmentPointContext.class,
                                         new ParserRuleAttachmentPointContext()),
    PARSER_RULE_ASSIGN_STMT_CONTEXT(BallerinaParser.AssignmentStatementContext.class,
                                    new ParserRuleAssignmentStatementContextContext()),
    PARSER_RULE_EXPRESSION_CONTEXT(BallerinaParser.ExpressionContext.class,
                                   new ParserRuleExpressionContextContext()),
    PARSER_RULE_IF_CLAUSE_CONTEXT(BallerinaParser.IfElseStatementContext.class,
                                  new ParserRuleConditionalClauseContext()),
    PARSER_RULE_WHILE_CLAUSE_CONTEXT(BallerinaParser.WhileStatementContext.class,
                                     new ParserRuleConditionalClauseContext()),
    PARSER_RULE_SERVICE_DEF_CONTEXT(BallerinaParser.ServiceDefinitionContext.class,
                                    new ParserRuleServiceDefinitionContext()),
    PARSER_RULE_DEFINITION_CONTEXT(BallerinaParser.DefinitionContext.class,
                                   new ParserRuleDefinitionContext()),
    PARSER_RULE_FUNCTION_DEF_CONTEXT(BallerinaParser.FunctionDefinitionContext.class,
                                     new ParserRuleFunctionDefinitionContext()),
    PARSER_RULE_RETURN_STMT_CONTEXT(BallerinaParser.ReturnStatementContext.class,
                                    new ParserRuleReturnStatementContextContext()),
    // TODO: Can be removed
    PARSER_RULE_PANIC_STATEMENT_CONTEXT(BallerinaParser.PanicStatementContext.class,
            new ParserRulePanicStatementContext()),
    PARSER_RULE_MATCH_STATEMENT_CONTEXT(BallerinaParser.MatchStatementContext.class,
                                        new ParserRuleMatchStatementContext());

    private final Class context;
    private static final Map<Class, CompletionItemsContext> classToResolverMap = new HashMap<>();

    static {
        for (CompletionItemResolver resolver : CompletionItemResolver.values()) {
            classToResolverMap.put(resolver.getContext(), resolver.getCompletionItemResolver());
        }
    }

    private final CompletionItemsContext completionItemResolver;

    CompletionItemResolver(Class context, CompletionItemsContext itemResolver) {
        this.context = context;
        this.completionItemResolver = itemResolver;
    }

    private Class getContext() {
        return context;
    }

    /**
     * Get the resolver by the class.
     *
     * @param context                       context class to extract the relevant resolver
     * @param ctx                           LS Context
     * @return {@link AbstractItemContext}     Item resolver for the given context
     */
    public static CompletionItemsContext get(Class context, LSContext ctx) {
        return classToResolverMap.get(context).resolve(ctx);
    }

    private CompletionItemsContext getCompletionItemResolver() {
        return completionItemResolver;
    }
}

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
import org.ballerinalang.langserver.completions.resolvers.CompletionItemScope;
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
import org.wso2.ballerinalang.compiler.tree.statements.BLangStatement;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum for the completion item resolvers.
 */
public enum CompletionItemResolver {
    STATEMENT_CONTEXT(BLangStatement.class,
                      CompletionItemScope.STATEMENT),
    PACKAGE_LEVEL_CONTEXT(BLangPackage.class,
                          CompletionItemScope.TOP_LEVEL),
    TESTABLE_PACKAGE_LEVEL_CONTEXT(BLangTestablePackage.class,
                                   CompletionItemScope.TOP_LEVEL),
    PACKAGE_NAME_CONTEXT(BallerinaParser.PackageNameContext.class,
                         CompletionItemScope.PACKAGE_NAME),
    IMPORT_DECLARATION_CONTEXT(BallerinaParser.ImportDeclarationContext.class,
                               CompletionItemScope.PACKAGE_NAME),
    PARAMETER_CONTEXT(BallerinaParser.ParameterContext.class,
                      CompletionItemScope.PARAMETER),
    PARAMETER_LIST_CONTEXT(BallerinaParser.ParameterListContext.class,
                           CompletionItemScope.PARAMETER),
    BLOCK_STATEMENT_CONTEXT(BLangBlockStmt.class,
                            CompletionItemScope.BLOCK_STATEMENT),
    ANNOTATION_ATTACHMENT(BallerinaParser.AnnotationAttachmentContext.class,
                          CompletionItemScope.PR_ANNOTATION_ATTACHMENT),
    RECORD_CONTEXT(BLangRecordTypeNode.class,
                   CompletionItemScope.RECORD),
    SERVICE_CONTEXT(BLangService.class,
                    CompletionItemScope.SERVICE),
    FUNCTION_DEF_CONTEXT(BLangFunction.class,
                         CompletionItemScope.FUNCTION),
    OBJECT_TYPE_CONTEXT(BLangObjectTypeNode.class,
                        CompletionItemScope.OBJECT_TYPE),
    RECORD_LITERAL_CONTEXT(BLangRecordLiteral.class,
                           CompletionItemScope.RECORD_LITERAL),
    MATCH_STATEMENT_CONTEXT(BLangMatch.class,
                            CompletionItemScope.MATCH),
    MATCH_EXPRESSION_CONTEXT(BLangMatchExpression.class,
                             CompletionItemScope.MATCH_EXPRESSION),
    BLANG_ANNOTATION_ATTACHMENT_CONTEXT(BLangAnnotationAttachment.class,
                                        CompletionItemScope.ANNOTATION_ATTACHMENT),

    PARSER_RULE_STATEMENT_CONTEXT(BallerinaParser.StatementContext.class,
                                  CompletionItemScope.PR_STATEMENT),
    PARSER_RULE_VAR_DEF_STMT_CONTEXT(BallerinaParser.VariableDefinitionStatementContext.class,
                                     CompletionItemScope.PR_VARIABLE_DEFINITION),
    PARSER_RULE_WORKER_REPLY_CONTEXT(BallerinaParser.WorkerReceiveExpressionContext.class,
                                     CompletionItemScope.PR_WORKER_REPLY),
    PARSER_RULE_GLOBAL_VAR_DEF_CONTEXT(BallerinaParser.GlobalVariableDefinitionContext.class,
                                       CompletionItemScope.PR_GLOBAL_VARIABLE_DEFINITION),
    PARSER_RULE_ATTACHMENT_POINT_CONTEXT(BallerinaParser.AttachmentPointContext.class,
                                         CompletionItemScope.PR_ANNOTATION_ATTACHMENT),
    PARSER_RULE_ASSIGN_STMT_CONTEXT(BallerinaParser.AssignmentStatementContext.class,
                                    CompletionItemScope.PR_ASSIGNMENT_STATEMENT),
    PARSER_RULE_EXPRESSION_CONTEXT(BallerinaParser.ExpressionContext.class,
                                   CompletionItemScope.PR_EXPRESSION),
    PARSER_RULE_IF_CLAUSE_CONTEXT(BallerinaParser.IfElseStatementContext.class,
                                  CompletionItemScope.PR_CONDITIONAL_CLAUSE),
    PARSER_RULE_WHILE_CLAUSE_CONTEXT(BallerinaParser.WhileStatementContext.class,
                                     CompletionItemScope.PR_CONDITIONAL_CLAUSE),
    PARSER_RULE_SERVICE_DEF_CONTEXT(BallerinaParser.ServiceDefinitionContext.class,
                                    CompletionItemScope.PR_SERVICE_DEFINITION),
    PARSER_RULE_DEFINITION_CONTEXT(BallerinaParser.DefinitionContext.class,
                                   CompletionItemScope.PR_DEFINITION),
    PARSER_RULE_FUNCTION_DEF_CONTEXT(BallerinaParser.FunctionDefinitionContext.class,
                                     CompletionItemScope.PR_FUNCTION_DEFINITION),
    PARSER_RULE_RETURN_STMT_CONTEXT(BallerinaParser.ReturnStatementContext.class,
                                    CompletionItemScope.PR_RETURN_STMT_DEFINITION),
    // TODO: Can be removed
    PARSER_RULE_PANIC_STATEMENT_CONTEXT(BallerinaParser.PanicStatementContext.class,
                                        CompletionItemScope.PR_PANIC_STATEMENT),
    PARSER_RULE_MATCH_STATEMENT_CONTEXT(BallerinaParser.MatchStatementContext.class,
                                        CompletionItemScope.PR_MATCH_STATEMENT);

    private final Class context;
    private static final Map<Class, CompletionItemScope> classToResolverMap = new HashMap<>();

    static {
        for (CompletionItemResolver resolver : CompletionItemResolver.values()) {
            classToResolverMap.put(resolver.getContext(), resolver.getCompletionItemResolver());
        }
    }

    private final CompletionItemScope completionItemResolver;

    CompletionItemResolver(Class context, CompletionItemScope itemResolver) {
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
    public static CompletionItemScope get(Class context, LSContext ctx) {
        return classToResolverMap.get(context).resolve(ctx);
    }

    private CompletionItemScope getCompletionItemResolver() {
        return completionItemResolver;
    }
}

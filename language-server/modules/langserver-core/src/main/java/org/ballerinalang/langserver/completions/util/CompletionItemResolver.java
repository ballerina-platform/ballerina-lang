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

import org.ballerinalang.langserver.completions.resolvers.AbstractItemResolver;
import org.ballerinalang.langserver.completions.resolvers.AnnotationAttachmentResolver;
import org.ballerinalang.langserver.completions.resolvers.BLangEndpointContextResolver;
import org.ballerinalang.langserver.completions.resolvers.BLangMatchContextResolver;
import org.ballerinalang.langserver.completions.resolvers.BLangRecordContextResolver;
import org.ballerinalang.langserver.completions.resolvers.BlockStatementContextResolver;
import org.ballerinalang.langserver.completions.resolvers.ConnectorActionContextResolver;
import org.ballerinalang.langserver.completions.resolvers.ConnectorDefinitionContextResolver;
import org.ballerinalang.langserver.completions.resolvers.DefaultResolver;
import org.ballerinalang.langserver.completions.resolvers.FunctionContextResolver;
import org.ballerinalang.langserver.completions.resolvers.ObjectTypeContextResolver;
import org.ballerinalang.langserver.completions.resolvers.PackageNameContextResolver;
import org.ballerinalang.langserver.completions.resolvers.ParameterContextResolver;
import org.ballerinalang.langserver.completions.resolvers.ResourceContextResolver;
import org.ballerinalang.langserver.completions.resolvers.ServiceContextResolver;
import org.ballerinalang.langserver.completions.resolvers.StatementContextResolver;
import org.ballerinalang.langserver.completions.resolvers.TopLevelResolver;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleAssignmentStatementContextResolver;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleAttachmentPointContextResolver;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleCallableUnitBodyContextResolver;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleConditionalClauseContextResolver;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleEndpointTypeContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleExpressionContextResolver;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleGlobalVariableDefinitionContextResolver;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleMatchStatementContextResolver;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleResourceDefinitionContextResolver;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleServiceBodyContextResolver;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleServiceEndpointAttachmentContextResolver;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleStatementContextResolver;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleTriggerWorkerContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleTypeNameContextResolver;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleVariableDefinitionStatementContextResolver;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleWorkerReplyContext;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.tree.BLangAction;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangConnector;
import org.wso2.ballerinalang.compiler.tree.BLangEndpoint;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangObject;
import org.wso2.ballerinalang.compiler.tree.BLangRecord;
import org.wso2.ballerinalang.compiler.tree.BLangResource;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.statements.BLangBlockStmt;
import org.wso2.ballerinalang.compiler.tree.statements.BLangMatch;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum for the completion item resolvers.
 */
public enum CompletionItemResolver {

    DEFAULT(DefaultResolver.class,
            new DefaultResolver()),
    STATEMENT_CONTEXT(StatementContextResolver.class,
            new StatementContextResolver()),
    TOP_LEVEL_CONTEXT(TopLevelResolver.class,
            new TopLevelResolver()),
    PACKAGE_NAME_CONTEXT(BallerinaParser.PackageNameContext.class,
            new PackageNameContextResolver()),
    IMPORT_DECLARATION_CONTEXT(BallerinaParser.ImportDeclarationContext.class,
            new PackageNameContextResolver()),
    PARAMETER_CONTEXT(BallerinaParser.ParameterContext.class,
            new ParameterContextResolver()),
    PARAMETER_LIST_CONTEXT(BallerinaParser.ParameterListContext.class,
            new ParameterContextResolver()),
    BLOCK_STATEMENT_CONTEXT(BLangBlockStmt.class,
            new BlockStatementContextResolver()),
    ANNOTATION_ATTACHMENT(AnnotationAttachmentResolver.class,
            new AnnotationAttachmentResolver()),
    B_LANG_ANNOTATION_ATTACHMENT(BLangAnnotationAttachment.class,
            new AnnotationAttachmentResolver()),
    RECORD_CONTEXT(BLangRecord.class,
            new BLangRecordContextResolver()),
    SERVICE_CONTEXT(BLangService.class,
            new ServiceContextResolver()),
    RESOURCE_CONTEXT(BLangResource.class,
            new ResourceContextResolver()),
    CONNECTOR_DEF_CONTEXT(BLangConnector.class,
            new ConnectorDefinitionContextResolver()),
    ACTION_DEF_CONTEXT(BLangAction.class,
            new ConnectorActionContextResolver()),
    BLANG_ENDPOINT_CONTEXT(BLangEndpoint.class,
            new BLangEndpointContextResolver()),
    FUNCTION_DEF_CONTEXT(BLangFunction.class,
            new FunctionContextResolver()),
    OBJECT_TYPE_CONTEXT(BLangObject.class,
            new ObjectTypeContextResolver()),
    MATCH_STATEMENT_CONTEXT(BLangMatch.class,
            new BLangMatchContextResolver()),

    PARSER_RULE_STATEMENT_CONTEXT(BallerinaParser.StatementContext.class,
            new ParserRuleStatementContextResolver()),
    PARSER_RULE_VAR_DEF_STMT_CONTEXT(BallerinaParser.VariableDefinitionStatementContext.class,
            new ParserRuleVariableDefinitionStatementContextResolver()),
    PARSER_RULE_TRIGGER_WORKER_CONTEXT(BallerinaParser.TriggerWorkerContext.class,
            new ParserRuleTriggerWorkerContext()),
    PARSER_RULE_WORKER_REPLY_CONTEXT(BallerinaParser.WorkerReplyContext.class,
            new ParserRuleWorkerReplyContext()),
    PARSER_RULE_TYPE_NAME_CONTEXT(BallerinaParser.TypeNameContext.class,
            new ParserRuleTypeNameContextResolver()),
    PARSER_RULE_GLOBAL_VAR_DEF_CONTEXT(BallerinaParser.GlobalVariableDefinitionContext.class,
            new ParserRuleGlobalVariableDefinitionContextResolver()),
    PARSER_RULE_ATTACHMENT_POINT_CONTEXT(BallerinaParser.AttachmentPointContext.class,
            new ParserRuleAttachmentPointContextResolver()),
    PARSER_RULE_ASSIGN_STMT_CONTEXT(BallerinaParser.AssignmentStatementContext.class,
            new ParserRuleAssignmentStatementContextResolver()),
    PARSER_RULE_EXPRESSION_CONTEXT(BallerinaParser.ExpressionContext.class,
            new ParserRuleExpressionContextResolver()),
    PARSER_RULE_CALLABLE_UNIT_BODY_CONTEXT(BallerinaParser.CallableUnitBodyContext.class,
            new ParserRuleCallableUnitBodyContextResolver()),
    PARSER_RULE_IF_CLAUSE_CONTEXT(BallerinaParser.IfClauseContext.class,
            new ParserRuleConditionalClauseContextResolver()),
    PARSER_RULE_WHILE_CLAUSE_CONTEXT(BallerinaParser.WhileStatementContext.class,
            new ParserRuleConditionalClauseContextResolver()),
    PARSER_RULE_SERVICE_BODY_CONTEXT(BallerinaParser.ServiceBodyContext.class,
            new ParserRuleServiceBodyContextResolver()),
    PARSER_RULE_RESOURCE_DEF_CONTEXT(BallerinaParser.ResourceDefinitionContext.class,
            new ParserRuleResourceDefinitionContextResolver()),
    PARSER_RULE_SERVICE_EP_CONTEXT(BallerinaParser.ServiceEndpointAttachmentsContext.class,
            new ParserRuleServiceEndpointAttachmentContextResolver()),
    PARSER_RULE_EP_TYPE_CONTEXT(BallerinaParser.EndpointTypeContext.class,
            new ParserRuleEndpointTypeContext()),
    PARSER_RULE_EP_DECLARATION_CONTEXT(BallerinaParser.EndpointDeclarationContext.class,
            new ParserRuleEndpointTypeContext()),
    PARSER_RULE_MATCH_STATEMENT_CONTEXT(BallerinaParser.MatchStatementContext.class,
            new ParserRuleMatchStatementContextResolver());

    private final Class context;
    private final AbstractItemResolver completionItemResolver;
    private static final Map<Class, AbstractItemResolver> resolverMap =
            Collections.unmodifiableMap(initializeMapping());

    CompletionItemResolver(Class context, AbstractItemResolver itemResolver) {
        this.context = context;
        this.completionItemResolver = itemResolver;
    }

    private Class getContext() {
        return context;
    }

    private AbstractItemResolver getCompletionItemResolver() {
        return completionItemResolver;
    }

    /**
     * Get the resolver by the class.
     * @param context - context class to extract the relevant resolver
     * @return {@link AbstractItemResolver} - Item resolver for the given context
     */
    public static AbstractItemResolver getResolverByClass(Class context) {
        return resolverMap.get(context);
    }

    private static Map<Class, AbstractItemResolver> initializeMapping() {
        Map<Class, AbstractItemResolver> map = new HashMap<>();
        for (CompletionItemResolver resolver : CompletionItemResolver.values()) {
            map.put(resolver.getContext(), resolver.getCompletionItemResolver());
        }
        return map;
    }
}

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
package org.ballerinalang.langserver.completions.providers;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.completions.LSCompletionProvider;
import org.ballerinalang.langserver.completions.LSCompletionProviderException;
import org.ballerinalang.langserver.completions.providers.subproviders.AbstractSubCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.BLangAnnotationCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.BLangMatchCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.BLangMatchExpressionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.BLangRecordICompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.BLangRecordLiteralCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.FunctionCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.ObjectTypeCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.PackageNameCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.ParameterCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.ResourceCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.ServiceCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.StatementCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.TopLevelCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.parsercontext.EndpointDeclarationCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.parsercontext.ParserRuleAnnotationAttachmentCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.parsercontext.ParserRuleAttachmentPointCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.parsercontext.ParserRuleConditionalClauseCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.parsercontext.ParserRuleDefinitionCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.parsercontext.ParserRuleFunctionDefinitionCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.parsercontext.ParserRuleGlobalVariableDefinitionCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.parsercontext.ParserRuleMatchStatementCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.parsercontext.ParserRulePanicStatementCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.parsercontext.ParserRuleServiceDefinitionCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.parsercontext.ParserRuleServiceEndpointAttachmentCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.parsercontext.ParserRuleStatementCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.parsercontext.ParserRuleTriggerWorkerCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.parsercontext.ParserRuleVariableDefinitionCompletionProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.parsercontext.ParserRuleWorkerReplyCompletionProvider;
import org.ballerinalang.langserver.completions.resolvers.BLangAnnotationAttachmentContext;
import org.ballerinalang.langserver.completions.resolvers.BLangMatchContext;
import org.ballerinalang.langserver.completions.resolvers.BLangMatchExpressionContext;
import org.ballerinalang.langserver.completions.resolvers.BLangRecordContext;
import org.ballerinalang.langserver.completions.resolvers.BLangRecordLiteralContext;
import org.ballerinalang.langserver.completions.resolvers.CompletionItemsContext;
import org.ballerinalang.langserver.completions.resolvers.FunctionContext;
import org.ballerinalang.langserver.completions.resolvers.ObjectTypeContext;
import org.ballerinalang.langserver.completions.resolvers.PackageNameContext;
import org.ballerinalang.langserver.completions.resolvers.ParameterContext;
import org.ballerinalang.langserver.completions.resolvers.ResourceContext;
import org.ballerinalang.langserver.completions.resolvers.ServiceContext;
import org.ballerinalang.langserver.completions.resolvers.StatementContext;
import org.ballerinalang.langserver.completions.resolvers.TopLevelContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.EndpointDeclarationContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleAnnotationAttachmentContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleAttachmentPointContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleConditionalClauseContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleDefinitionContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleFunctionDefinitionContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleGlobalVariableDefinitionContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleMatchStatementContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRulePanicStatementContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleServiceDefinitionContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleServiceEndpointAttachmentContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleStatementContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleTriggerWorkerContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleVariableDefinitionStatementContext;
import org.ballerinalang.langserver.completions.resolvers.parsercontext.ParserRuleWorkerReplyContext;
import org.eclipse.lsp4j.CompletionItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default Language server implementation for the completion item resolvers.
 */
@JavaSPIService("org.ballerinalang.langserver.completions.LSCompletionProvider")
public class LSDefaultCompletionItemProvider implements LSCompletionProvider {
    private static Map<Class<? extends CompletionItemsContext>, AbstractSubCompletionProvider> contextToProvider =
            new HashMap<>();

    public LSDefaultCompletionItemProvider() {
        contextToProvider.put(ParserRuleAnnotationAttachmentContext.class,
                              new ParserRuleAnnotationAttachmentCompletionProvider());
        contextToProvider.put(EndpointDeclarationContext.class,
                              new EndpointDeclarationCompletionProvider());
        contextToProvider.put(ParserRuleAttachmentPointContext.class,
                              new ParserRuleAttachmentPointCompletionProvider());
        contextToProvider.put(ParserRuleConditionalClauseContext.class,
                              new ParserRuleConditionalClauseCompletionProvider());
        contextToProvider.put(ParserRuleDefinitionContext.class,
                              new ParserRuleDefinitionCompletionProvider());
        contextToProvider.put(ParserRuleFunctionDefinitionContext.class,
                              new ParserRuleFunctionDefinitionCompletionProvider());
        contextToProvider.put(ParserRuleMatchStatementContext.class,
                              new ParserRuleMatchStatementCompletionProvider());
        contextToProvider.put(ParserRulePanicStatementContext.class,
                              new ParserRulePanicStatementCompletionProvider());
        contextToProvider.put(ParserRuleServiceDefinitionContext.class,
                              new ParserRuleServiceDefinitionCompletionProvider());
        contextToProvider.put(ParserRuleServiceEndpointAttachmentContext.class,
                              new ParserRuleServiceEndpointAttachmentCompletionProvider());
        contextToProvider.put(ParserRuleStatementContext.class,
                              new ParserRuleStatementCompletionProvider());
        contextToProvider.put(ParserRuleTriggerWorkerContext.class,
                              new ParserRuleTriggerWorkerCompletionProvider());
        contextToProvider.put(ParserRuleVariableDefinitionStatementContext.class,
                              new ParserRuleVariableDefinitionCompletionProvider());
        contextToProvider.put(ParserRuleGlobalVariableDefinitionContext.class,
                              new ParserRuleGlobalVariableDefinitionCompletionProvider());
        contextToProvider.put(ParserRuleWorkerReplyContext.class,
                              new ParserRuleWorkerReplyCompletionProvider());
        contextToProvider.put(BLangAnnotationAttachmentContext.class,
                              new BLangAnnotationCompletionProvider());
        contextToProvider.put(BLangMatchContext.class,
                              new BLangMatchCompletionProvider());
        contextToProvider.put(BLangMatchExpressionContext.class,
                              new BLangMatchExpressionProvider());
        contextToProvider.put(BLangRecordContext.class,
                              new BLangRecordICompletionProvider());
        contextToProvider.put(BLangRecordLiteralContext.class,
                              new BLangRecordLiteralCompletionProvider());
        contextToProvider.put(FunctionContext.class,
                              new FunctionCompletionProvider());
        contextToProvider.put(ObjectTypeContext.class,
                              new ObjectTypeCompletionProvider());
        contextToProvider.put(PackageNameContext.class,
                              new PackageNameCompletionProvider());
        contextToProvider.put(ParameterContext.class,
                              new ParameterCompletionProvider());
        contextToProvider.put(ResourceContext.class,
                              new ResourceCompletionProvider());
        contextToProvider.put(ServiceContext.class,
                              new ServiceCompletionProvider());
        contextToProvider.put(StatementContext.class,
                              new StatementCompletionProvider());
        contextToProvider.put(TopLevelContext.class,
                              new TopLevelCompletionProvider());
    }

    @Override
    public String getName() {
        return "defaultCompletionItemProvider";
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext context, CompletionItemsContext completionItemsContext)
            throws LSCompletionProviderException {
        AbstractSubCompletionProvider subItemProvider = contextToProvider.get(completionItemsContext.getClass());
        return subItemProvider.resolveItems(context);
    }
}

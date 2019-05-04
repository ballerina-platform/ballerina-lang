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
import org.ballerinalang.langserver.completions.resolvers.CompletionItemsContext;
import org.eclipse.lsp4j.CompletionItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Default Language server implementation for the completion item resolvers.
 */
@JavaSPIService("org.ballerinalang.langserver.completions.LSCompletionProvider")
public class LSDefaultCompletionItemProvider implements LSCompletionProvider {
    private static Map<CompletionItemsContext, AbstractSubCompletionProvider> contextToProvider =
            new HashMap<>();

    public LSDefaultCompletionItemProvider() {
        contextToProvider.put(CompletionItemsContext.PR_ANNOTATION_ATTACHMENT,
                              new ParserRuleAnnotationAttachmentCompletionProvider());
        contextToProvider.put(CompletionItemsContext.ENDPOINT_DECLARATION,
                              new EndpointDeclarationCompletionProvider());
        contextToProvider.put(CompletionItemsContext.PR_ATTACHMENT_POINT,
                              new ParserRuleAttachmentPointCompletionProvider());
        contextToProvider.put(CompletionItemsContext.PR_CONDITIONAL_CLAUSE,
                              new ParserRuleConditionalClauseCompletionProvider());
        contextToProvider.put(CompletionItemsContext.PR_DEFINITION,
                              new ParserRuleDefinitionCompletionProvider());
        contextToProvider.put(CompletionItemsContext.PR_FUNCTION_DEFINITION,
                              new ParserRuleFunctionDefinitionCompletionProvider());
        contextToProvider.put(CompletionItemsContext.PR_MATCH_STATEMENT,
                              new ParserRuleMatchStatementCompletionProvider());
        contextToProvider.put(CompletionItemsContext.PR_PANIC_STATEMENT,
                              new ParserRulePanicStatementCompletionProvider());
        contextToProvider.put(CompletionItemsContext.PR_SERVICE_DEFINITION,
                              new ParserRuleServiceDefinitionCompletionProvider());
        contextToProvider.put(CompletionItemsContext.PR_SERVICE_ENDPOINT_ATTACHMENT,
                              new ParserRuleServiceEndpointAttachmentCompletionProvider());
        contextToProvider.put(CompletionItemsContext.PR_STATEMENT,
                              new ParserRuleStatementCompletionProvider());
        contextToProvider.put(CompletionItemsContext.PR_WORKER_TRIGGER_WORKER_STMT,
                              new ParserRuleTriggerWorkerCompletionProvider());
        contextToProvider.put(CompletionItemsContext.PR_VARIABLE_DEFINITION,
                              new ParserRuleVariableDefinitionCompletionProvider());
        contextToProvider.put(CompletionItemsContext.PR_GLOBAL_VARIABLE_DEFINITION,
                              new ParserRuleGlobalVariableDefinitionCompletionProvider());
        contextToProvider.put(CompletionItemsContext.PR_WORKER_REPLY,
                              new ParserRuleWorkerReplyCompletionProvider());
        contextToProvider.put(CompletionItemsContext.ANNOTATION_ATTACHMENT,
                              new BLangAnnotationCompletionProvider());
        contextToProvider.put(CompletionItemsContext.MATCH,
                              new BLangMatchCompletionProvider());
        contextToProvider.put(CompletionItemsContext.MATCH_EXPRESSION,
                              new BLangMatchExpressionProvider());
        contextToProvider.put(CompletionItemsContext.RECORD,
                              new BLangRecordICompletionProvider());
        contextToProvider.put(CompletionItemsContext.RECORD_LITERAL,
                              new BLangRecordLiteralCompletionProvider());
        contextToProvider.put(CompletionItemsContext.FUNCTION,
                              new FunctionCompletionProvider());
        contextToProvider.put(CompletionItemsContext.OBJECT_TYPE,
                              new ObjectTypeCompletionProvider());
        contextToProvider.put(CompletionItemsContext.PACKAGE_NAME,
                              new PackageNameCompletionProvider());
        contextToProvider.put(CompletionItemsContext.PARAMETER,
                              new ParameterCompletionProvider());
        contextToProvider.put(CompletionItemsContext.RESOURCE,
                              new ResourceCompletionProvider());
        contextToProvider.put(CompletionItemsContext.SERVICE,
                              new ServiceCompletionProvider());
        contextToProvider.put(CompletionItemsContext.STATEMENT,
                              new StatementCompletionProvider());
        contextToProvider.put(CompletionItemsContext.TOP_LEVEL,
                              new TopLevelCompletionProvider());
    }

    @Override
    public String getName() {
        return "defaultCompletionItemProvider";
    }

    @Override
    public List<CompletionItem> getCompletions(LSContext context, CompletionItemsContext completionItemsContext)
            throws LSCompletionProviderException {
        AbstractSubCompletionProvider subItemProvider = contextToProvider.get(completionItemsContext);
        if (subItemProvider != null) {
            return subItemProvider.resolveItems(context);
        }
        throw new LSCompletionProviderException(
                "Couldn't find completion item provider for the context: " + completionItemsContext.name());
    }
}

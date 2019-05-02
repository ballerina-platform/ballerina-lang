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

import org.ballerinalang.langserver.completions.spi.LSCompletionProvider;
import org.ballerinalang.langserver.completions.providers.scopeproviders.BLangAnnotationAttachmentProvider;
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
import org.ballerinalang.langserver.completions.providers.scopeproviders.TopLevelProvider;
import org.ballerinalang.langserver.completions.providers.subproviders.parsercontext.EndpointDeclarationCompletionProvider;
import org.ballerinalang.langserver.completions.providers.contextproviders.ParserRuleAnnotationAttachmentCompletionProvider;
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
import org.ballerinalang.langserver.completions.resolvers.CompletionItemScope;

import java.util.HashMap;
import java.util.Map;

/**
 * Default Language server implementation for the completion item resolvers.
 */
//@JavaSPIService("org.ballerinalang.langserver.completions.LSCompletionProvider")
public class LSDefaultCompletionItemProvider {
    private static Map<CompletionItemScope, LSCompletionProvider> contextToProvider =
            new HashMap<>();

    public LSDefaultCompletionItemProvider() {
        contextToProvider.put(CompletionItemScope.PR_ANNOTATION_ATTACHMENT,
                              new ParserRuleAnnotationAttachmentCompletionProvider());
        contextToProvider.put(CompletionItemScope.ENDPOINT_DECLARATION,
                              new EndpointDeclarationCompletionProvider());
        contextToProvider.put(CompletionItemScope.PR_ATTACHMENT_POINT,
                              new ParserRuleAttachmentPointCompletionProvider());
        contextToProvider.put(CompletionItemScope.PR_CONDITIONAL_CLAUSE,
                              new ParserRuleConditionalClauseCompletionProvider());
        contextToProvider.put(CompletionItemScope.PR_DEFINITION,
                              new ParserRuleDefinitionCompletionProvider());
        contextToProvider.put(CompletionItemScope.PR_FUNCTION_DEFINITION,
                              new ParserRuleFunctionDefinitionCompletionProvider());
        contextToProvider.put(CompletionItemScope.PR_MATCH_STATEMENT,
                              new ParserRuleMatchStatementCompletionProvider());
        contextToProvider.put(CompletionItemScope.PR_PANIC_STATEMENT,
                              new ParserRulePanicStatementCompletionProvider());
        contextToProvider.put(CompletionItemScope.PR_SERVICE_DEFINITION,
                              new ParserRuleServiceDefinitionCompletionProvider());
        contextToProvider.put(CompletionItemScope.PR_SERVICE_ENDPOINT_ATTACHMENT,
                              new ParserRuleServiceEndpointAttachmentCompletionProvider());
        contextToProvider.put(CompletionItemScope.PR_STATEMENT,
                              new ParserRuleStatementCompletionProvider());
        contextToProvider.put(CompletionItemScope.PR_WORKER_TRIGGER_WORKER_STMT,
                              new ParserRuleTriggerWorkerCompletionProvider());
        contextToProvider.put(CompletionItemScope.PR_VARIABLE_DEFINITION,
                              new ParserRuleVariableDefinitionCompletionProvider());
        contextToProvider.put(CompletionItemScope.PR_GLOBAL_VARIABLE_DEFINITION,
                              new ParserRuleGlobalVariableDefinitionCompletionProvider());
        contextToProvider.put(CompletionItemScope.PR_WORKER_REPLY,
                              new ParserRuleWorkerReplyCompletionProvider());
        contextToProvider.put(CompletionItemScope.ANNOTATION_ATTACHMENT,
                              new BLangAnnotationAttachmentProvider());
        contextToProvider.put(CompletionItemScope.MATCH,
                              new BLangMatchCompletionProvider());
        contextToProvider.put(CompletionItemScope.MATCH_EXPRESSION,
                              new BLangMatchExpressionProvider());
        contextToProvider.put(CompletionItemScope.RECORD,
                              new BLangRecordICompletionProvider());
        contextToProvider.put(CompletionItemScope.RECORD_LITERAL,
                              new BLangRecordLiteralCompletionProvider());
        contextToProvider.put(CompletionItemScope.FUNCTION,
                              new FunctionCompletionProvider());
        contextToProvider.put(CompletionItemScope.OBJECT_TYPE,
                              new ObjectTypeCompletionProvider());
        contextToProvider.put(CompletionItemScope.PACKAGE_NAME,
                              new PackageNameCompletionProvider());
        contextToProvider.put(CompletionItemScope.PARAMETER,
                              new ParameterCompletionProvider());
        contextToProvider.put(CompletionItemScope.RESOURCE,
                              new ResourceCompletionProvider());
        contextToProvider.put(CompletionItemScope.SERVICE,
                              new ServiceCompletionProvider());
        contextToProvider.put(CompletionItemScope.STATEMENT,
                              new StatementCompletionProvider());
        contextToProvider.put(CompletionItemScope.TOP_LEVEL,
                              new TopLevelProvider());
    }

//    @Override
//    public String getName() {
//        return "defaultCompletionItemProvider";
//    }

//    @Override
//    public List<CompletionItem> getCompletions(LSContext context)
//            throws LSCompletionProviderException {
////        CompletionItemScope scope = context.get(CompletionKeys.SCOPE_KEY);
////        LSCompletionProvider subItemProvider = contextToProvider.get(scope);
////        if (subItemProvider != null) {
////            return subItemProvider.resolveItems(context);
////        }
//        throw new LSCompletionProviderException(
//                "Couldn't find completion item provider for the context: " + scope.name());
//    }
}

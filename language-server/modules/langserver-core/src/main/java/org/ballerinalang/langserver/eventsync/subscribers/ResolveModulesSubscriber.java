/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com)
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

package org.ballerinalang.langserver.eventsync.subscribers;

import io.ballerina.compiler.api.SemanticModel;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.command.executors.PullModuleExecutor;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.eventsync.EventKind;
import org.ballerinalang.langserver.commons.eventsync.spi.EventSubscriber;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageType;
import org.eclipse.lsp4j.ShowMessageRequestParams;

import java.util.List;
import java.util.Optional;

/**
 * Publisher to popup notification and resolve the missing dependencies on project updates
 *
 * @since 2201.12.3
 */
@JavaSPIService("org.ballerinalang.langserver.commons.eventsync.spi.EventSubscriber")
public class ResolveModulesSubscriber implements EventSubscriber {

    public static final String NAME = "Resolve modules subscriber";
    private static final String UNRESOLVED_MODULE_CODE = "BCE2003";
    private static final String PULL_MODULES_ACTION = "Pull Modules";

    @Override
    public EventKind eventKind() {
        return EventKind.PROJECT_UPDATE;
    }

    @Override
    public void onEvent(ExtendedLanguageClient client, DocumentServiceContext context,
                        LanguageServerContext serverContext) {
        Optional<SemanticModel> semanticModel = context.currentSemanticModel();
        if (semanticModel.isEmpty()) {
            return;
        }

        boolean hasUnresolvedModules = semanticModel.get().diagnostics().stream()
                .anyMatch(diagnostic -> UNRESOLVED_MODULE_CODE.equals(diagnostic.diagnosticInfo().code()));
        if (!hasUnresolvedModules) {
            return;
        }

        ShowMessageRequestParams showMessageRequestParams = new ShowMessageRequestParams();
        showMessageRequestParams.setType(MessageType.Warning);
        showMessageRequestParams.setMessage(
                "There are unresolved modules in your project. Some of the features may not work as expected.");
        showMessageRequestParams.setActions(List.of(new MessageActionItem(PULL_MODULES_ACTION)));

        client.showMessageRequest(showMessageRequestParams).thenAccept(action -> {
            if (action != null && PULL_MODULES_ACTION.equals(action.getTitle())) {
                PullModuleExecutor.resolveModules(context.fileUri(), client, context.workspace(),
                        context.languageServercontext());
            }
        });
    }

    @Override
    public String getName() {
        return NAME;
    }
}

/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.codelenses.providers;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.client.config.BallerinaClientConfigHolder;
import org.ballerinalang.langserver.codelenses.CodeLensUtil;
import org.ballerinalang.langserver.command.CommandUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codelenses.CodeLensesProviderKeys;
import org.ballerinalang.langserver.commons.codelenses.LSCodeLensesProviderException;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Code lenses provider for adding code lenses for all services.
 *
 * @since 0.990.3
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codelenses.spi.LSCodeLensesProvider")
public class ServicesBasedCodeLensesProvider extends AbstractCodeLensesProvider {
    public ServicesBasedCodeLensesProvider() {
        super("services.CodeLenses");
        BallerinaClientConfigHolder.getInstance().register((oldConfig, newConfig) -> {
            isEnabled = newConfig.getCodeLens().getServices().isEnabled();
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeLens> getLenses(LSContext context) throws LSCodeLensesProviderException {
        List<CodeLens> lenses = new ArrayList<>();
        BLangCompilationUnit cUnit = context.get(CodeLensesProviderKeys.COMPILATION_UNIT_KEY);
        for (TopLevelNode topLevelNode : cUnit.getTopLevelNodes()) {
            addServiceLenses(lenses, topLevelNode);
        }
        return lenses;
    }

    private void addServiceLenses(List<CodeLens> lenses, TopLevelNode topLevelNode) {
        if (topLevelNode instanceof BLangService) {
            BLangService service = (BLangService) topLevelNode;
            String owner = (service.listenerType != null) ? service.listenerType.tsymbol.owner.name.value : null;
            String serviceTypeName = (service.listenerType != null) ? service.listenerType.tsymbol.name.value : null;
            if (!("http".equals(owner) && "Listener".equals(serviceTypeName))) {
                // Skip non-HTTP services
                return;
            }
            int sLine = service.pos.sLine - 1;
            sLine = CodeLensUtil.getTopMostLocOfAnnotations(service.annAttachments, sLine);
            sLine = CodeLensUtil.getTopMostLocOfDocs(service.markdownDocumentationAttachment, sLine);
            Position pos = new Position(sLine, 0);
            // Show API Editor
            CommandUtil.CommandArgument serviceNameArg = new CommandUtil.CommandArgument(
                    CommandConstants.ARG_KEY_SERVICE_NAME, service.name.value);
            List<Object> args = new ArrayList<>(Collections.singletonList(serviceNameArg));
            Command showApiEditor = new Command("Show API Design", "ballerina.showAPIEditor", args);
            CodeLens apiEditorLens = new CodeLens(new Range(pos, pos), showApiEditor, null);
            lenses.add(apiEditorLens);
        }
    }
}

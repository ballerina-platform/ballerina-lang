/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.langserver.diagnostics;

import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.eventsync.exceptions.EventSyncException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.ballerinalang.langserver.contexts.LanguageServerContextImpl;
import org.ballerinalang.langserver.diagnostic.DiagnosticsHelper;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.workspace.BallerinaWorkspaceManager;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.mockito.Mockito;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.util.List;

/**
 * Diagnostic tests related to cyclic dependencies.
 *
 * @since 2201.9.0
 */
public class CyclicDependenciesTest {

    private final Path projectRoot = FileUtils.RES_DIR.resolve("diagnostics").resolve("sources");
    private final LanguageServerContext serverContext = new LanguageServerContextImpl();
    private final BallerinaWorkspaceManager workspaceManager = new BallerinaWorkspaceManager(serverContext);

    @Test(dataProvider = "cyclic-package-provider")
    public void testCyclicDependenciesOnOpen(String packageName, List<String> expectedMessages)
            throws WorkspaceDocumentException, EventSyncException,
            InterruptedException {
        Path projectPath = projectRoot.resolve(packageName);
        ExtendedLanguageClient mockClient = Mockito.mock(ExtendedLanguageClient.class);

        DocumentServiceContext serviceContext =
                ContextBuilder.buildDocumentServiceContext(projectPath.toUri().toString(),
                        this.workspaceManager,
                        LSContextOperation.TXT_DID_OPEN,
                        this.serverContext);

        this.workspaceManager.loadProject(projectPath);
        DiagnosticsHelper diagnosticsHelper = DiagnosticsHelper.getInstance(this.serverContext);
        diagnosticsHelper.schedulePublishDiagnostics(mockClient, serviceContext);
        Thread.sleep(2000);

        Mockito.verify(mockClient, Mockito.times(expectedMessages.size())).showMessage(Mockito.any());
        expectedMessages.forEach(expectedMessage -> Mockito.verify(mockClient)
                .showMessage(new MessageParams(MessageType.Error, expectedMessage)));
    }

    @DataProvider(name = "cyclic-package-provider")
    public Object[][] cyclicDataProvider() {
        return new Object[][]{
                {"cyclic_simple",
                        List.of("cyclic module imports detected 'lsorg/cyclic.mod1:0.1.0 -> lsorg/cyclic.mod2:0.1.0" +
                                " -> lsorg/cyclic.mod1:0.1.0'")},
                {"cyclic_multi",
                        List.of("cyclic module imports detected 'lsorg/cyclic_multi.abc:0.1.0 -> lsorg/cyclic_multi" +
                                        ".def:0.1.0 -> lsorg/cyclic_multi.ghi:0.1.0 -> lsorg/cyclic_multi.jkl:0.1.0 " +
                                        "-> lsorg/cyclic_multi.abc:0.1.0'",
                                "cyclic module imports detected 'lsorg/cyclic_multi.def:0.1.0 -> lsorg/cyclic_multi" +
                                        ".ghi:0.1.0 -> lsorg/cyclic_multi.def:0.1.0'",
                                "cyclic module imports detected 'lsorg/cyclic_multi.abc:0.1.0 -> lsorg/cyclic_multi" +
                                        ".def:0.1.0 -> lsorg/cyclic_multi.ghi:0.1.0 -> lsorg/cyclic_multi.abc:0.1.0'")}
        };
    }
}

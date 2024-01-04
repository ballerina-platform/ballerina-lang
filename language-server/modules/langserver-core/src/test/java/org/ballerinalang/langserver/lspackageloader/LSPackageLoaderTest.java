/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.lspackageloader;

import io.ballerina.projects.Project;
import org.ballerinalang.langserver.AbstractLSTest;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.LSPackageLoader;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.eventsync.EventKind;
import org.ballerinalang.langserver.commons.eventsync.exceptions.EventSyncException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.ballerinalang.langserver.eventsync.EventSyncPubSubHolder;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Tests {@link org.ballerinalang.langserver.LSPackageLoader}.
 *
 * @since 2201.2.1
 */
public class LSPackageLoaderTest extends AbstractLSTest {

    private final Path testRoot = FileUtils.RES_DIR.resolve("lspackageloader");
    private static final Map<String, String> REMOTE_PROJECTS = Map.of("project3", "main.bal");
    private List<LSPackageLoader.ModuleInfo> remoteRepoPackages = new ArrayList<>(getRemotePackages());

    @Test(dataProvider = "data-provider")
    public void test(String source) throws IOException, EventSyncException, WorkspaceDocumentException {
        //Open the source text document and load project
        Path sourcePath = testRoot.resolve("source").resolve(source);
        Endpoint endpoint = getServiceEndpoint();
        TestUtil.openDocument(endpoint, sourcePath);

        BallerinaLanguageServer languageServer = this.getLanguageServer();
        EventSyncPubSubHolder eventSyncPubSubHolder =
                EventSyncPubSubHolder.getInstance(languageServer.getServerContext());
        DocumentServiceContext documentServiceContext =
                ContextBuilder.buildDocumentServiceContext(sourcePath.toUri().toString(),
                        languageServer.getWorkspaceManager(), LSContextOperation.WS_EXEC_CMD,
                        languageServer.getServerContext());
        //ModuleInfo count before adding a new package
        int packageCount = getLoadedPackagesFromLoader(documentServiceContext).size();

        //Publish a mock event using pull module publisher.
        eventSyncPubSubHolder.getPublisher(EventKind.PULL_MODULE).publish(this.getLanguageServer().getClient(),
                languageServer.getServerContext(), documentServiceContext);

        //Assert if the package info map is updated with the newly added package.
        int packageCountAfterPullModule = getLoadedPackagesFromLoader(documentServiceContext).size();
        Assert.assertTrue(packageCount < packageCountAfterPullModule);
    }

    @Override
    public void setUp() {
        LSPackageLoader lsPackageLoader = Mockito.mock(LSPackageLoader.class, Mockito.withSettings().stubOnly());
        setLsPackageLoader(lsPackageLoader);
        this.getLanguageServer().getServerContext().put(LSPackageLoader.LS_PACKAGE_LOADER_KEY, getLSPackageLoader());
        Mockito.when(lsPackageLoader.getLocalRepoModules()).thenReturn(getLocalPackages());
        Mockito.when(lsPackageLoader.getDistributionRepoModules()).thenReturn(getDistributionPackages());
        Mockito.when(lsPackageLoader.getRemoteRepoModules()).thenReturn(this.remoteRepoPackages);
        Mockito.when(lsPackageLoader.getAllVisiblePackages(Mockito.any())).thenCallRealMethod();
        Mockito.when(lsPackageLoader.getPackagesFromBallerinaUserHome(Mockito.any())).thenCallRealMethod();
        Mockito.doNothing().when(lsPackageLoader).loadModules(Mockito.any());
        Mockito.doAnswer(invocation -> {
            invocation.getArguments();
            Object[] arguments = invocation.getArguments();
            if (arguments != null && arguments.length == 1 && arguments[0] != null) {
                DocumentServiceContext context = (DocumentServiceContext) arguments[0];
                LSPackageLoader.ModuleInfo moduleInfo = getPackages(REMOTE_PROJECTS,
                        context.workspace(), context.languageServercontext()).stream()
                        .map(LSPackageLoader.ModuleInfo::new)
                        .collect(Collectors.toList()).get(0);
                this.remoteRepoPackages.add(moduleInfo);
                return List.of(moduleInfo);
            }
            return null;
        }).when(lsPackageLoader).updatePackageMap(Mockito.any());
    }

    private List<LSPackageLoader.ModuleInfo> getLoadedPackagesFromLoader(DocumentServiceContext context) {
        Optional<Project> project = context.workspace().project(context.filePath());
        if (project.isEmpty()) {
            return Collections.emptyList();
        }
        return getLSPackageLoader().getRemoteRepoModules();
    }

    @DataProvider(name = "data-provider")
    public Object[][] dataProvider() {
        return new Object[][]{
                {"pull_module_event_source.bal"}
        };
    }

    @Override
    public boolean loadMockedPackages() {
        return true;
    }
}

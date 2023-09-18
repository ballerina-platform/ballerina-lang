/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.lspackageloader;

import org.ballerinalang.langserver.AbstractLSTest;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.LSPackageLoader;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.eventsync.EventKind;
import org.ballerinalang.langserver.commons.eventsync.exceptions.EventSyncException;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.ballerinalang.langserver.eventsync.EventSyncPubSubHolder;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PullModulePackageRefreshTest extends AbstractLSTest {

    private final Path testRoot = FileUtils.RES_DIR.resolve("lspackageloader");
    private static final Map<String, String> REMOTE_PROJECTS = Map.of("project3", "main.bal");
    private final List<LSPackageLoader.ModuleInfo> remoteRepoPackages = new ArrayList<>(getRemoteModules());
    
    @BeforeClass
    @Override
    public void init() throws Exception {
        LSPackageLoader lsPackageLoader = Mockito.mock(LSPackageLoader.class, Mockito.withSettings().stubOnly());
        setLsPackageLoader(lsPackageLoader);
        BallerinaLanguageServer languageServer = new BallerinaLanguageServer();
        languageServer.getServerContext().put(LSPackageLoader.LS_PACKAGE_LOADER_KEY, getLSPackageLoader());
        setLanguageServer(languageServer);
        
        TestUtil.LanguageServerBuilder builder = TestUtil.newLanguageServer().withLanguageServer(languageServer);
        setupLanguageServer(builder);
        setServiceEndpoint(builder.build());
        
        Mockito.when(lsPackageLoader.getRemoteRepoModules())
                .thenAnswer(invocation -> getRemoteRepoPackages());
        Mockito.doAnswer(invocation -> {
            invocation.getArguments();
            Object[] arguments = invocation.getArguments();
            if (arguments != null && arguments.length == 1 && arguments[0] != null) {
                DocumentServiceContext context = (DocumentServiceContext) arguments[0];
                getPackages(REMOTE_PROJECTS,
                        context.workspace(), context.languageServercontext())
                        .forEach(pkg -> pkg.modules().forEach(module ->
                                this.remoteRepoPackages.add(new LSPackageLoader.ModuleInfo(module))));
            }
            return null;
        }).when(lsPackageLoader).refreshRemoteModules(Mockito.any());
    }

    public List<LSPackageLoader.ModuleInfo> getRemoteRepoPackages() {
        return remoteRepoPackages;
    }
    
    private List<LSPackageLoader.ModuleInfo> getLoadedPackagesFromLoader(DocumentServiceContext context) {
        return getLSPackageLoader().getRemoteRepoModules();
    }

    @Test
    public void testPullModuleEvent() throws IOException, EventSyncException {
        //Open the source text document and load project
        Path sourcePath = testRoot.resolve("source").resolve("pull_module_event_source.bal");
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
        int packageCount = getLSPackageLoader().getRemoteRepoModules().size();

        //Publish a mock event using pull module publisher.
        eventSyncPubSubHolder.getPublisher(EventKind.PULL_MODULE).publish(this.getLanguageServer().getClient(),
                languageServer.getServerContext(), documentServiceContext);

        //Assert if the package info map is updated with the newly added package.
        int packageCountAfterPullModule = getLSPackageLoader().getRemoteRepoModules().size();
        Assert.assertTrue(packageCount < packageCountAfterPullModule);
    }
}

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

import org.ballerinalang.langserver.AbstractLSTest;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.LSPackageLoader;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.capability.InitializationOptions;
import org.ballerinalang.langserver.contexts.ContextBuilder;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Tests {@link org.ballerinalang.langserver.LSPackageLoader}.
 *
 * @since 2201.2.1
 */
public class LSPackageLoaderTest extends AbstractLSTest {
    private final Path testRoot = FileUtils.RES_DIR.resolve("lspackageloader");

    @BeforeClass
    @Override
    public void init() throws Exception {
        Path userHome = testRoot.resolve("user-home");
        setLanguageServer(new BallerinaLanguageServer());
        setLsPackageLoader(LSPackageLoader.getInstance(getLanguageServer().getServerContext(), userHome));
        TestUtil.LanguageServerBuilder builder = TestUtil.newLanguageServer().withLanguageServer(getLanguageServer());
        setupLanguageServer(builder);
        setServiceEndpoint(builder.build());
        //Wait for LS Package loader to load the modules
        long initTime = System.currentTimeMillis();
        while (!getLSPackageLoader().isInitialized() && System.currentTimeMillis() < initTime + 60 * 1000) {
            Thread.sleep(2000);
        }
        if (!getLSPackageLoader().isInitialized()) {
            Assert.fail("LS Package Loader initialization failed!");
        }
    }

    @Test
    public void testPackageLoading() throws IOException {
        BallerinaLanguageServer languageServer = getLanguageServer();
        List<LSPackageLoader.ModuleInfo> localRepoModules =
                LSPackageLoader.getInstance(languageServer.getServerContext()).getLocalRepoModules();
        List<LSPackageLoader.ModuleInfo> remoteRepoModules =
                LSPackageLoader.getInstance(languageServer.getServerContext()).getRemoteRepoModules();

        //Open the source text document and load project
        Path sourcePath = testRoot.resolve("source")
                .resolve("package_test_source.bal");
        Endpoint endpoint = getServiceEndpoint();
        TestUtil.openDocument(endpoint, sourcePath);

        DocumentServiceContext documentServiceContext =
                ContextBuilder.buildDocumentServiceContext(sourcePath.toUri().toString(),
                        languageServer.getWorkspaceManager(), LSContextOperation.TXT_DID_OPEN,
                        languageServer.getServerContext());

        List<LSPackageLoader.ModuleInfo> allVisibleModules =
                LSPackageLoader.getInstance(languageServer.getServerContext())
                        .getAllVisibleModules(documentServiceContext);

        //The module info content is verified by completion and code action tests, 
        // therefore, we only assert the size of the list of modules in each repo. 
        Assert.assertTrue(allVisibleModules.size() == 30);
        Assert.assertTrue(localRepoModules.size() == 4);
        Assert.assertTrue(remoteRepoModules.size() == 4);
    }
    

    @Override
    protected void setupLanguageServer(TestUtil.LanguageServerBuilder builder) {
        builder.withInitOption(InitializationOptions.KEY_ENABLE_INDEX_USER_HOME, true);
    }
    
}

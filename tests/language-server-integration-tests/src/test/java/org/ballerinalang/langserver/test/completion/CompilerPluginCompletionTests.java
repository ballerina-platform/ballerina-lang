/*
 * Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.test.completion;

import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.LSClientCapabilitiesImpl;
import org.ballerinalang.langserver.LSPackageLoader;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.completion.CompletionTest;
import org.ballerinalang.langserver.util.TestUtil;
import org.ballerinalang.test.BCompileUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Completion tests for compiler plugin based completion providers.
 */
public class CompilerPluginCompletionTests extends CompletionTest {

    @BeforeClass
    @Override
    public void init() throws Exception {
        BallerinaLanguageServer server = new BallerinaLanguageServer();
        LanguageServerContext context = server.getServerContext();
        TestUtil.LanguageServerBuilder builder = TestUtil.newLanguageServer()
                .withLanguageServer(server)
                .withInitOption(LSClientCapabilitiesImpl.InitializationOptionsImpl.KEY_ENABLE_INDEX_USER_HOME, false);

        //Build the LS. This will populate the init options and load the packages from distribution 
        //into the LSPackage Loader.
        long initTime = System.currentTimeMillis();
        Endpoint endPoint = builder.build();
        LSPackageLoader lsPackageLoader = LSPackageLoader.getInstance(context);
        
        //Wait for LS Package loader to load the modules form distribution
        while (!lsPackageLoader.isInitialized() && System.currentTimeMillis() < initTime + 60 * 1000) {
            Thread.sleep(2000);
        }
        if (!lsPackageLoader.isInitialized()) {
            Assert.fail("LS Package Loader initialization failed!");
        }

        //Build and start the LS
        this.setLanguageServer(server);
        this.setServiceEndpoint(endPoint);
    }

    @BeforeSuite
    public void compilePlugins() {
        BCompileUtil.compileAndCacheBala("compiler_plugin_tests/package_comp_plugin_with_completions");
    }

    @Override
    @Test(dataProvider = "completion-data-provider")
    public void test(String config, String configPath) throws WorkspaceDocumentException, IOException {
        super.test(config, configPath);
    }

    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"compiler_plugin_with_completions_config1.json", getTestResourceDir()},
                {"compiler_plugin_with_completions_config2.json", getTestResourceDir()},
                {"compiler_plugin_completion_single_file_config1.json", getTestResourceDir()}
        };
    }

    @Override
    public String getTestResourceDir() {
        return "compiler-plugins";
    }
}


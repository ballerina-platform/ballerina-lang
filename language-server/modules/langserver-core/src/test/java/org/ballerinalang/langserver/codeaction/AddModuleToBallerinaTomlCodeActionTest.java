/*
 *  Copyright (c) 2023, WSO2 LLC. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.langserver.codeaction;

import io.ballerina.projects.Package;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.LSPackageLoader;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.capability.InitializationOptions;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.contexts.LanguageServerContextImpl;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Test class to test module addition to Ballerina.toml.
 *
 * @since 2201.8.0
 */
public class AddModuleToBallerinaTomlCodeActionTest extends AbstractCodeActionTest {
    
    protected void setupLanguageServer(TestUtil.LanguageServerBuilder builder) {
        builder.withInitOption(InitializationOptions.KEY_POSITIONAL_RENAME_SUPPORT, true);
    }

    @BeforeClass
    @Override
    public void setup() {
        super.setup();

        LanguageServerContext context = new LanguageServerContextImpl();
        BallerinaLanguageServer languageServer = new BallerinaLanguageServer();
        Endpoint endpoint = TestUtil.initializeLanguageSever(languageServer);
        try {
            Map<String, String> localProjects = Map.of("pkg1", "main.bal", "pkg2", "main.bal", "x", "main.bal", 
                    "x.y", "main.bal");
            List<LSPackageLoader.ModuleInfo> localPackages = getLocalPackages(localProjects,
                    languageServer.getWorkspaceManager(), context).stream().map(LSPackageLoader.ModuleInfo::new)
                    .collect(Collectors.toList());
            Mockito.when(getLSPackageLoader().getLocalRepoModules()).thenReturn(localPackages);
        } catch (Exception e) {
            //ignore
        } finally {
            TestUtil.shutdownLanguageServer(endpoint);
        }
    }

    @Test(dataProvider = "codeaction-data-provider")
    @Override
    public void test(String config) throws IOException, WorkspaceDocumentException {
        super.test(config);
    }

    @DataProvider(name = "codeaction-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"add_module1.json"},
                {"add_module2.json"},
                {"add_module3.json"},
                {"add_module4.json"},
                {"add_module5.json"},
                {"add_module6.json"},
                {"add_module7.json"},
                {"add_module8.json"},
                {"add_module9.json"},
                {"add_module10.json"},
        };
    }

    @Test(dataProvider = "negative-test-data-provider")
    @Override
    public void negativeTest(String config) throws IOException, WorkspaceDocumentException {
        super.negativeTest(config);
    }

    @DataProvider(name = "negative-test-data-provider")
    public Object[][] negativeDataProvider() {
        return new Object[][]{
                {"add_module_negative_1.json"},
                {"add_module_negative_2.json"}
        };
    }

    @Override
    public String getResourceDir() {
        return "add-module-ballerina-toml";
    }

    @Override
    public boolean loadMockedPackages() {
        return true;
    }

    private static List<Package> getLocalPackages(Map<String, String> projects, WorkspaceManager workspaceManager,
                                                  LanguageServerContext context)
            throws WorkspaceDocumentException, IOException {
        List<Package> packages = new ArrayList<>();
        for (Map.Entry<String, String> entry : projects.entrySet()) {
            Path path = FileUtils.RES_DIR.resolve("local_projects").resolve(entry.getKey())
                    .resolve(entry.getValue());
            TestUtil.compileAndGetPackage(path, workspaceManager, context).ifPresent(packages::add);
        }
        return packages;
    }
}

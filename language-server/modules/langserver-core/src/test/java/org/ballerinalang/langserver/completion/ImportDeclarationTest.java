/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.completion;

import io.ballerina.projects.Package;
import org.ballerinalang.langserver.BallerinaLanguageServer;
import org.ballerinalang.langserver.LSPackageLoader;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.contexts.LanguageServerContextImpl;
import org.ballerinalang.langserver.util.TestUtil;
import org.mockito.Mockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Import Declaration Context tests. 
 */
public class ImportDeclarationTest extends CompletionTest {

    LanguageServerContext languageServerContext;
    BallerinaLanguageServer languageServer;
    private Map<String, String> remoteProjects = Map.of("project1", "main", "project2", "main");
    private Map<String, String> localProjects = Map.of("local_project1", "main", "local_project2", "main");

    @Test(dataProvider = "completion-data-provider")
    @Override
    public void test(String config, String configPath) throws WorkspaceDocumentException, IOException {
        super.test(config, configPath);
    }

    @BeforeClass
    @Override
    public void init() throws Exception {
        try {
            this.languageServerContext = new LanguageServerContextImpl();
            LSPackageLoader packageLoader = Mockito.mock(LSPackageLoader.class);
            this.languageServerContext.put(LSPackageLoader.LS_PACKAGE_LOADER_KEY, packageLoader);
            Mockito.when(packageLoader.getRemoteRepoPackages(Mockito.any())).thenReturn(getPackages(this.remoteProjects));
            Mockito.when(packageLoader.getLocalRepoPackages(Mockito.any())).thenReturn(getPackages(this.localProjects));
            Mockito.when(packageLoader.getAllVisiblePackages(Mockito.any())).thenCallRealMethod();
            this.serviceEndpoint = TestUtil.initializeLanguageSever(
                    new BallerinaLanguageServer(this.languageServerContext));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        return this.getConfigsList();
    }

    @Override
    public String getTestResourceDir() {
        return "import_decl";
    }

    @Override
    public List<String> skipList() {
        return Collections.singletonList("config6.json");
    }

    private List<Package> getPackages(Map<String, String> projects) throws WorkspaceDocumentException, IOException {
        List<Package> packages = new ArrayList<>();
        for (Map.Entry<String, String> entry : projects.entrySet()) {
            Path path = this.testRoot.resolve(getTestResourceDir())
                    .resolve("source").resolve("repository_projects").resolve(entry.getKey()).resolve(entry.getValue());
            getPackage(path).ifPresent(packages::add);
        }
        return packages;
    }

    private Optional<Package> getPackage(Path path) throws WorkspaceDocumentException, IOException {
        if (this.languageServer == null) {
            BallerinaLanguageServer languageServer = new BallerinaLanguageServer();
            TestUtil.initializeLanguageSever(languageServer);
        }
        return TestUtil.compileAndGetPackage(path, languageServer.getWorkspaceManager(),
                this.languageServerContext);
    }
}

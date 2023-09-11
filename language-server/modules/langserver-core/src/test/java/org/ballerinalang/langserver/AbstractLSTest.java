/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver;

import com.google.gson.Gson;
import io.ballerina.projects.Package;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.contexts.LanguageServerContextImpl;
import org.ballerinalang.langserver.extensions.ballerina.connector.CentralPackageListResult;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An abstract class for LS unit tests.
 *
 * @since 2201.1.0
 */
public abstract class AbstractLSTest {

    private static final Gson GSON = new Gson();
    private static final Map<String, String> REMOTE_PROJECTS = Map.of("project1", "main.bal", "project2", "main.bal");
    private static final Map<String, String> LOCAL_PROJECTS =
            Map.of("local_project1", "main.bal", "local_project2", "main.bal");
    private static final List<LSPackageLoader.ModuleInfo> REMOTE_MODULES = new ArrayList<>();
    private static final List<LSPackageLoader.ModuleInfo> LOCAL_MODULES = new ArrayList<>();
    private static final List<org.ballerinalang.central.client.model.Package> CENTRAL_PACKAGES = new ArrayList<>();

    private Endpoint serviceEndpoint;

    private LSPackageLoader lsPackageLoader;

    private static CentralPackageDescriptorLoader centralPackageLoader;
    private static LSPackageLoader unmockedLSPackageLoader;
    private BallerinaLanguageServer languageServer;

    static {
        LanguageServerContext context = new LanguageServerContextImpl();
        BallerinaLanguageServer languageServer = new BallerinaLanguageServer();
        try {
            preparePackageLoaders(context, languageServer);
        } catch (Exception e) {
            String s  = e.getMessage();
        }
    }

    private static void preparePackageLoaders(LanguageServerContext context,
                                              BallerinaLanguageServer languageServer)
            throws InterruptedException, WorkspaceDocumentException, IOException {
        //Mock central package loader
        FileReader fileReader = new FileReader(FileUtils.RES_DIR.resolve("central/centralPackages.json").toFile());
        CENTRAL_PACKAGES.addAll(GSON.fromJson(fileReader, CentralPackageListResult.class).getPackages());
        centralPackageLoader = Mockito.mock(CentralPackageDescriptorLoader.class, Mockito.withSettings().stubOnly());
        Mockito.when(centralPackageLoader.getCentralPackages(Mockito.any())).thenReturn(CENTRAL_PACKAGES);
        context.put(CentralPackageDescriptorLoader.CENTRAL_PACKAGE_HOLDER_KEY, centralPackageLoader);

        unmockedLSPackageLoader = LSPackageLoader.getInstance(context);
        //Wait for LS Package loader to load the modules form distribution
        long initTime = System.currentTimeMillis();
        while (!unmockedLSPackageLoader.isInitialized() && System.currentTimeMillis() < initTime + 2 * 60 * 1000) {
            Thread.sleep(2000);
        }
        if (!unmockedLSPackageLoader.isInitialized()) {
            Assert.fail("LS Package Loader initialization failed!");
        }
        getPackages(REMOTE_PROJECTS, languageServer.getWorkspaceManager(), context)
                .forEach(pkg -> pkg.modules().forEach(module ->
                        REMOTE_MODULES.add(new LSPackageLoader.ModuleInfo(module))));
        getPackages(LOCAL_PROJECTS, languageServer.getWorkspaceManager(), context).forEach(pkg ->
                pkg.modules().forEach(module ->
                        LOCAL_MODULES.add(new LSPackageLoader.ModuleInfo(module))));
    }

    public boolean loadMockedPackages() {
        return false;
    }

    @BeforeClass
    public void init() throws Exception {
        this.languageServer = new BallerinaLanguageServer();
        if (this.loadMockedPackages()) {
            setUp();
        }
        TestUtil.LanguageServerBuilder builder = TestUtil.newLanguageServer()
                .withLanguageServer(languageServer);
        setupLanguageServer(builder);
        this.serviceEndpoint = builder.build();
    }

    protected void setupLanguageServer(TestUtil.LanguageServerBuilder builder) {
    }

    public void setUp() {
        this.lsPackageLoader = Mockito.spy(unmockedLSPackageLoader);
        this.languageServer.getServerContext().put(CentralPackageDescriptorLoader.CENTRAL_PACKAGE_HOLDER_KEY, 
                centralPackageLoader);
        this.languageServer.getServerContext().put(LSPackageLoader.LS_PACKAGE_LOADER_KEY, this.lsPackageLoader);
        Mockito.when(this.lsPackageLoader.getRemoteRepoModules()).thenReturn(REMOTE_MODULES);
        Mockito.when(this.lsPackageLoader.getLocalRepoModules()).thenReturn(LOCAL_MODULES);
    }

    protected static List<Package> getPackages(Map<String, String> projects,
                                               WorkspaceManager workspaceManager,
                                               LanguageServerContext context)
            throws WorkspaceDocumentException, IOException {
        List<Package> packages = new ArrayList<>();
        for (Map.Entry<String, String> entry : projects.entrySet()) {
            Path path = FileUtils.RES_DIR.resolve("repository_projects").resolve(entry.getKey())
                    .resolve(entry.getValue());
            TestUtil.compileAndGetPackage(path, workspaceManager, context).ifPresent(packages::add);
        }
        return packages;
    }

    @AfterClass
    public void cleanMocks() {
        if (this.lsPackageLoader != null) {
            Mockito.reset(this.lsPackageLoader);
            this.lsPackageLoader = null;
        }
        if (centralPackageLoader != null) {
            Mockito.reset(centralPackageLoader);
            centralPackageLoader = null;
        }
    }

    @AfterClass
    public void shutDownLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
        this.languageServer = null;
        this.serviceEndpoint = null;
    }

    public BallerinaLanguageServer getLanguageServer() {
        return this.languageServer;
    }

    public Endpoint getServiceEndpoint() {
        return this.serviceEndpoint;
    }

    public void setLsPackageLoader(LSPackageLoader lsPackageLoader) {
        this.lsPackageLoader = lsPackageLoader;
    }

    public LSPackageLoader getLSPackageLoader() {
        return this.lsPackageLoader;
    }

    public static List<LSPackageLoader.ModuleInfo> getLocalModules() {
        return LOCAL_MODULES;
    }

    public static List<LSPackageLoader.ModuleInfo> getRemoteModules() {
        return REMOTE_MODULES;
    }
}

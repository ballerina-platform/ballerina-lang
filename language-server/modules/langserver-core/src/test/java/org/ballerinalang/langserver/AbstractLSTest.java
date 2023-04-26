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

import io.ballerina.projects.Package;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.contexts.LanguageServerContextImpl;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * An abstract class for LS unit tests.
 *
 * @since 2201.1.0
 */
@PrepareForTest({LSPackageLoader.class})
public abstract class AbstractLSTest {

    private static final Map<String, String> REMOTE_PROJECTS = Map.of("project1", "main.bal", "project2", "main.bal");
    private static final Map<String, String> LOCAL_PROJECTS =
            Map.of("local_project1", "main.bal", "local_project2", "main.bal");
    private static final List<LSPackageLoader.ModuleInfo> REMOTE_PACKAGES = new ArrayList<>();
    private static final List<LSPackageLoader.ModuleInfo> LOCAL_PACKAGES = new ArrayList<>();

    private Endpoint serviceEndpoint;

    private LSPackageLoader lsPackageLoader;

    private BallerinaLanguageServer languageServer;

    static {
        LanguageServerContext context = new LanguageServerContextImpl();
        BallerinaLanguageServer languageServer = new BallerinaLanguageServer();
        Endpoint endpoint = TestUtil.initializeLanguageSever(languageServer);
        try {
            REMOTE_PACKAGES.addAll(getPackages(REMOTE_PROJECTS,
                    languageServer.getWorkspaceManager(), context).stream().map(LSPackageLoader.ModuleInfo::new)
                    .collect(Collectors.toList()));
            LOCAL_PACKAGES.addAll(getPackages(LOCAL_PROJECTS,
                    languageServer.getWorkspaceManager(), context).stream().map(LSPackageLoader.ModuleInfo::new)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            //ignore
        } finally {
            TestUtil.shutdownLanguageServer(endpoint);
        }
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
        this.lsPackageLoader = Mockito.mock(LSPackageLoader.class, Mockito.withSettings().stubOnly());
        this.languageServer.getServerContext().put(LSPackageLoader.LS_PACKAGE_LOADER_KEY, this.lsPackageLoader);
        Mockito.when(this.lsPackageLoader.getRemoteRepoPackages(Mockito.any())).thenReturn(REMOTE_PACKAGES);
        Mockito.when(this.lsPackageLoader.getLocalRepoPackages(Mockito.any())).thenReturn(LOCAL_PACKAGES);
        Mockito.when(this.lsPackageLoader.getDistributionRepoPackages()).thenCallRealMethod();
        Mockito.when(this.lsPackageLoader.getAllVisiblePackages(Mockito.any())).thenCallRealMethod();
        Mockito.when(this.lsPackageLoader.getPackagesFromBallerinaUserHome(Mockito.any())).thenCallRealMethod();
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

    public static List<LSPackageLoader.ModuleInfo> getLocalPackages() {
        return LOCAL_PACKAGES;
    }

    public static List<LSPackageLoader.ModuleInfo> getRemotePackages() {
        return REMOTE_PACKAGES;
    }
}

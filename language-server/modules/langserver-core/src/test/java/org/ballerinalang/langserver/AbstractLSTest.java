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
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.capability.InitializationOptions;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
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

    private Endpoint serviceEndpoint;
    private static LSPackageLoader unMockedlsPackageLoader;
    private BallerinaLanguageServer languageServer;

    private LSPackageLoader lsPackageLoader;
    private static CentralPackageDescriptorLoader centralPackageLoader;

    static {
        try {
            preparePackageLoaders();
        } catch (Exception e) {
            //ignore
        }
    }

    private static void preparePackageLoaders()
            throws InterruptedException, WorkspaceDocumentException, IOException {

        BallerinaLanguageServer server = new BallerinaLanguageServer();
        LanguageServerContext context = server.getServerContext();
        TestUtil.LanguageServerBuilder builder = TestUtil.newLanguageServer()
                .withLanguageServer(server)
                .withInitOption(LSClientCapabilitiesImpl.InitializationOptionsImpl.KEY_ENABLE_INDEX_USER_HOME, false);

        //Mock central package loader
        FileReader fileReader = new FileReader(FileUtils.RES_DIR.resolve("central/centralPackages.json").toFile());
        List<LSPackageLoader.ModuleInfo> packages =
                GSON.fromJson(fileReader, CentralPackageListResult.class).getPackages().stream().map(packageInfo -> {
                    PackageOrg packageOrg = PackageOrg.from(packageInfo.getOrganization());
                    PackageName packageName = PackageName.from(packageInfo.getName());
                    PackageVersion packageVersion = PackageVersion.from(packageInfo.getVersion());
                    PackageDescriptor packageDescriptor =
                            PackageDescriptor.from(packageOrg, packageName, packageVersion, null);
                    return new LSPackageLoader.ModuleInfo(packageDescriptor);
                }).toList();
        centralPackageLoader = Mockito.mock(CentralPackageDescriptorLoader.class, Mockito.withSettings().stubOnly());
        Mockito.when(centralPackageLoader.getCentralModules()).thenReturn(packages);
        context.put(CentralPackageDescriptorLoader.CENTRAL_PACKAGE_HOLDER_KEY, centralPackageLoader);

        //Build the LS. This will populate the init options and load the packages from distribution 
        //into the LSPackage Loader.
        long initTime = System.currentTimeMillis();
        Endpoint tempEndPoint = builder.build();
        unMockedlsPackageLoader = LSPackageLoader.getInstance(context);
        //Wait for LS Package loader to load the modules form distribution
        while (!unMockedlsPackageLoader.isInitialized() && System.currentTimeMillis() < initTime + 60 * 1000) {
            Thread.sleep(2000);
        }
        TestUtil.shutdownLanguageServer(tempEndPoint);
        if (!unMockedlsPackageLoader.isInitialized()) {
            Assert.fail("LS Package Loader initialization failed!");
        }

        //Load mock Ballerina projects
        getPackages(REMOTE_PROJECTS, server.getWorkspaceManager(), context)
                .forEach(pkg -> pkg.modules().forEach(module ->
                        REMOTE_MODULES.add(new LSPackageLoader.ModuleInfo(module))));
        getPackages(LOCAL_PROJECTS, server.getWorkspaceManager(), context).forEach(pkg ->
                pkg.modules().forEach(module ->
                        LOCAL_MODULES.add(new LSPackageLoader.ModuleInfo(module))));

    }

    @BeforeClass
    public void init() throws Exception {
        this.languageServer = new BallerinaLanguageServer();
        this.languageServer.getServerContext().put(LSPackageLoader.LS_PACKAGE_LOADER_KEY, this.lsPackageLoader);
        this.languageServer.getServerContext().put(CentralPackageDescriptorLoader.CENTRAL_PACKAGE_HOLDER_KEY,
                centralPackageLoader);

        if (loadMockedPackages()) {
            //Add mocks to the language server context
            this.lsPackageLoader = Mockito.spy(unMockedlsPackageLoader);
            Mockito.when(this.lsPackageLoader.getRemoteRepoModules()).thenReturn(REMOTE_MODULES);
            Mockito.when(this.lsPackageLoader.getLocalRepoModules()).thenReturn(LOCAL_MODULES);
        } else {
            this.lsPackageLoader = unMockedlsPackageLoader;
        }
        this.languageServer.getServerContext().put(LSPackageLoader.LS_PACKAGE_LOADER_KEY, this.lsPackageLoader);

        //Build and start the LS
        TestUtil.LanguageServerBuilder builder = TestUtil.newLanguageServer()
                .withLanguageServer(this.languageServer);
        setupLanguageServer(builder);
        this.serviceEndpoint = builder.build();
    }

    /**
     * Whether the LS Package loader should be mocked!
     *
     * @return {@link Boolean} should mock
     */
    protected boolean loadMockedPackages() {
        return false;
    }

    /**
     * Set up the Language Server with custom initialization options.
     *
     * @param builder
     */
    protected void setupLanguageServer(TestUtil.LanguageServerBuilder builder) {
        builder.withInitOption(InitializationOptions.KEY_ENABLE_INDEX_USER_HOME, false);
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
    public void shutDownLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
        this.languageServer = null;
        this.serviceEndpoint = null;
        if (this.lsPackageLoader != null) {
            if (loadMockedPackages()) {
                Mockito.reset(this.lsPackageLoader);
            }
            this.lsPackageLoader = null;
        }
    }

    public BallerinaLanguageServer getLanguageServer() {
        return this.languageServer;
    }

    public Endpoint getServiceEndpoint() {
        return this.serviceEndpoint;
    }

    public void setServiceEndpoint(Endpoint endpoint) {
        this.serviceEndpoint = endpoint;
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

    public void setLanguageServer(BallerinaLanguageServer languageServer) {
        this.languageServer = languageServer;
    }
}

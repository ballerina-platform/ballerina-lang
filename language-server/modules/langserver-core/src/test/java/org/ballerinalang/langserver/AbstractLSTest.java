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
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.environment.PackageRepository;
import io.ballerina.projects.internal.environment.BallerinaDistribution;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceManager;
import org.ballerinalang.langserver.contexts.LanguageServerContextImpl;
import org.ballerinalang.langserver.extensions.ballerina.connector.CentralPackageListResult;
import org.ballerinalang.langserver.util.FileUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.jsonrpc.Endpoint;
import org.mockito.Mockito;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private static final List<LSPackageLoader.ModuleInfo> REMOTE_PACKAGES = new ArrayList<>();
    private static final List<LSPackageLoader.ModuleInfo> LOCAL_PACKAGES = new ArrayList<>();
    private static final List<LSPackageLoader.ModuleInfo> CENTRAL_PACKAGES = new ArrayList<>();
    private static final List<LSPackageLoader.ModuleInfo> DISTRIBUTION_PACKAGES = new ArrayList<>();

    private Endpoint serviceEndpoint;

    private LSPackageLoader lsPackageLoader;

    private CentralPackageDescriptorLoader descriptorLoader;

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
            DISTRIBUTION_PACKAGES.addAll(mockDistRepoPackages(LSPackageLoader.getInstance(context)));
            mockCentralPackages();
        } catch (Exception e) {
            //ignore
        } finally {
            TestUtil.shutdownLanguageServer(endpoint);
        }
    }

    private static void mockCentralPackages() {
        try {
            FileReader fileReader = new FileReader(FileUtils.RES_DIR.resolve("central/centralPackages.json").toFile());
            List<org.ballerinalang.central.client.model.Package> packages =
                    GSON.fromJson(fileReader, CentralPackageListResult.class).getPackages();
            packages.forEach(packageInfo -> {
                PackageOrg packageOrg = PackageOrg.from(packageInfo.getOrganization());
                PackageName packageName = PackageName.from(packageInfo.getName());
                PackageVersion packageVersion = PackageVersion.from(packageInfo.getVersion());
                CENTRAL_PACKAGES.add(new LSPackageLoader.ModuleInfo(packageOrg, packageName, packageVersion, null));
            });
        } catch (Exception e) {
            //ignore
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
                .withLanguageServer(languageServer)
                .withInitOption(LSClientCapabilitiesImpl.InitializationOptionsImpl.KEY_ENABLE_INDEX_PACKAGES, true);
        setupLanguageServer(builder);
        this.serviceEndpoint = builder.build();
    }

    protected void setupLanguageServer(TestUtil.LanguageServerBuilder builder) {
    }

    public void setUp() {
        this.lsPackageLoader = Mockito.mock(LSPackageLoader.class, Mockito.withSettings().stubOnly());
        this.languageServer.getServerContext().put(LSPackageLoader.LS_PACKAGE_LOADER_KEY, this.lsPackageLoader);
        Mockito.when(this.lsPackageLoader.getRemoteRepoModules()).thenReturn(REMOTE_PACKAGES);
        Mockito.when(this.lsPackageLoader.getLocalRepoModules()).thenReturn(LOCAL_PACKAGES);
        Mockito.when(this.lsPackageLoader.getCentralPackages()).thenReturn(CENTRAL_PACKAGES);
        Mockito.when(this.lsPackageLoader.getDistributionRepoModules()).thenReturn(DISTRIBUTION_PACKAGES);
        Mockito.when(this.lsPackageLoader.checkAndResolvePackagesFromRepository(Mockito.any(), Mockito.any(),
                Mockito.any())).thenCallRealMethod();
        Mockito.doNothing().when(this.lsPackageLoader).loadModules(Mockito.any());
        Mockito.when(this.lsPackageLoader.getAllVisiblePackages(Mockito.any())).thenCallRealMethod();
        Mockito.when(this.lsPackageLoader.getPackagesFromBallerinaUserHome(Mockito.any())).thenCallRealMethod();
    }

    private static List<LSPackageLoader.ModuleInfo> mockDistRepoPackages(LSPackageLoader lsPackageLoader) {
        Environment environment = EnvironmentBuilder.getBuilder().build();
        BallerinaDistribution ballerinaDistribution = BallerinaDistribution.from(environment);
        PackageRepository packageRepository = ballerinaDistribution.packageRepository();
        List<String> skippedLangLibs = Arrays.asList("lang.annotations", "lang.__internal", "lang.query");
        return lsPackageLoader.checkAndResolvePackagesFromRepository(packageRepository,
                skippedLangLibs, Collections.emptySet());
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
        if (this.descriptorLoader != null) {
            Mockito.reset(this.descriptorLoader);
            this.descriptorLoader = null;
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

    public static List<LSPackageLoader.ModuleInfo> getDistributionPackages() {
        return DISTRIBUTION_PACKAGES;
    }
}

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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * An abstract class for LS unit tests.
 *
 * @since 2201.0.3
 */
public abstract class AbstractLSTest {

    private static final Map<String, String> REMOTE_PROJECTS = Map.of("project1", "main.bal", "project2", "main.bal");
    private static final Map<String, String> LOCAL_PROJECTS = 
            Map.of("local_project1", "main.bal", "local_project2", "main.bal");
    private static final List<Package> REMOTE_PACKAGES = new ArrayList<>();
    private static final List<Package> LOCAL_PACKAGES = new ArrayList<>();

    protected Endpoint serviceEndpoint;

    private LanguageServerContext lsContext;

    private LSPackageLoader lsPackageLoader;

    static {
        LanguageServerContext context = new LanguageServerContextImpl();
        BallerinaLanguageServer languageServer = new BallerinaLanguageServer(context);
        Endpoint endpoint = TestUtil.initializeLanguageSever(languageServer);
        try {
            REMOTE_PACKAGES.addAll(getPackages(REMOTE_PROJECTS, languageServer.getWorkspaceManager(), context));
            LOCAL_PACKAGES.addAll(getPackages(LOCAL_PROJECTS, languageServer.getWorkspaceManager(), context));
        } catch (Exception e) {
            //ignore
        } finally {
            TestUtil.shutdownLanguageServer(endpoint);
        }
    }

    @BeforeClass
    public void init() throws Exception {
        this.lsContext = new LanguageServerContextImpl();
        BallerinaLanguageServer lsInstance = new BallerinaLanguageServer(this.lsContext);
        this.serviceEndpoint = TestUtil.initializeLanguageSever(lsInstance);
        setUp();
    }

    public void setUp() {
        this.lsPackageLoader = Mockito.mock(LSPackageLoader.class);
        lsContext.put(LSPackageLoader.LS_PACKAGE_LOADER_KEY, lsPackageLoader);
        Mockito.when(lsPackageLoader.getRemoteRepoPackages(Mockito.any())).thenReturn(REMOTE_PACKAGES);
        Mockito.when(lsPackageLoader.getLocalRepoPackages(Mockito.any())).thenReturn(LOCAL_PACKAGES);
        Mockito.when(lsPackageLoader.getDistributionRepoPackages()).thenCallRealMethod();
        Mockito.when(lsPackageLoader.getAllVisiblePackages(Mockito.any())).thenCallRealMethod();
    }

    private static List<Package> getPackages(Map<String, String> projects,
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
        if (lsPackageLoader != null) {
            Mockito.reset(lsPackageLoader);
        }
    }

    @AfterClass
    public void cleanupLanguageServer() {
        TestUtil.shutdownLanguageServer(this.serviceEndpoint);
    }
}

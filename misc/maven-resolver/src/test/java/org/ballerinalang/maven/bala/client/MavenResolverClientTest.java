/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.maven.bala.client;

import org.ballerinalang.maven.bala.client.model.ConnectorSearchMavenMetadata;
import org.ballerinalang.maven.bala.client.model.PkgSearchMavenMetadata;
import org.ballerinalang.maven.bala.client.model.PkgSearchSolrMavenMetadata;
import org.ballerinalang.maven.bala.client.model.SymbolSearchMavenMetadata;
import org.ballerinalang.maven.bala.client.model.ToolMavenMetadata;
import org.ballerinalang.maven.bala.client.model.ToolSearchMavenMetadata;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Unit tests for {@link MavenResolverClient}.
 *
 * @since 2201.13.3
 */
public class MavenResolverClientTest {

    private static final String BALLERINA_VERSION = "2201.13.2";

    private Path localCacheDir;
    private MavenResolverClient client;
    private String testRepoUrl;

    @BeforeMethod
    public void setup() throws IOException, URISyntaxException {
        localCacheDir = Files.createTempDirectory("maven-resolver-test-cache");
        client = new MavenResolverClient();
        URL resourceUrl = getClass().getClassLoader().getResource("test-maven-repo");
        Assert.assertNotNull(resourceUrl, "test-maven-repo resource directory not found");
        testRepoUrl = Paths.get(resourceUrl.toURI()).toAbsolutePath().toUri().toString();
        client.addRepository("test-repo", testRepoUrl);
    }

    @AfterMethod
    public void teardown() throws IOException {
        if (localCacheDir != null && Files.exists(localCacheDir)) {
            try (Stream<Path> walk = Files.walk(localCacheDir)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(p -> {
                            try {
                                Files.delete(p);
                            } catch (IOException ignored) {
                            }
                        });
            }
        }
    }

    @Test
    public void testAddRepository() {
        MavenResolverClient newClient = new MavenResolverClient();
        newClient.addRepository("test-id", "https://example.com/repo");
        // No exception should be thrown
    }

    @Test
    public void testAddRepositoryWithCredentials() {
        MavenResolverClient newClient = new MavenResolverClient();
        newClient.addRepository("test-id", "https://example.com/repo", "user", "pass");
        // No exception should be thrown
    }

    @Test
    public void testSetProxy_emptyUrl_noOp() {
        // Empty URL should be a no-op; no exception expected
        client.setProxy("", 8080, "user", "pass");
    }

    @Test
    public void testSetProxy_zeroPort_noOp() {
        // Port 0 should be a no-op; no exception expected
        client.setProxy("proxy.example.com", 0, "user", "pass");
    }

    @Test
    public void testSetProxy_withAuthentication() {
        client.setProxy("proxy.example.com", 8080, "proxyuser", "proxypass");
        // No exception should be thrown; proxy with auth configured
    }

    @Test
    public void testSetProxy_withoutAuthentication() {
        client.setProxy("proxy.example.com", 8080, "", "");
        // No exception should be thrown; unauthenticated proxy configured
    }

    // -------------------------------------------------------------------------
    // Group 2: Package metadata (via local file repo)
    // -------------------------------------------------------------------------

    @Test
    public void testGetPackageVersionsInCentralProxy() throws MavenResolverClientException {
        List<String> versions = client.getPackageVersionsInCentralProxy(
                "myorg", "mypkg", BALLERINA_VERSION, localCacheDir);

        Assert.assertNotNull(versions);
        Assert.assertEquals(versions.size(), 2);
        Assert.assertTrue(versions.contains("1.0.0"));
        Assert.assertTrue(versions.contains("0.9.0"));
    }

    @Test
    public void testGetPackageVersionsInCentralProxy_versionFiltering() throws MavenResolverClientException {
        // filterpkg has 1.0.0 (requires 2201.13.0) and 2.0.0 (requires 2201.14.0).
        // With current 2201.13.2: 1.0.0 is compatible (13 >= 13), 2.0.0 is not (13 < 14).
        List<String> versions = client.getPackageVersionsInCentralProxy(
                "myorg", "filterpkg", BALLERINA_VERSION, localCacheDir);

        Assert.assertNotNull(versions);
        Assert.assertEquals(versions.size(), 1);
        Assert.assertTrue(versions.contains("1.0.0"));
        Assert.assertFalse(versions.contains("2.0.0"));
    }

    @Test
    public void testGetPackageVersionsInCentralProxy_caching() throws MavenResolverClientException {
        List<String> firstCall = client.getPackageVersionsInCentralProxy(
                "myorg", "mypkg", BALLERINA_VERSION, localCacheDir);
        List<String> secondCall = client.getPackageVersionsInCentralProxy(
                "myorg", "mypkg", BALLERINA_VERSION, localCacheDir);

        Assert.assertEquals(firstCall, secondCall);
    }

    @Test
    public void testGetBallerinaVersionForPackage() throws MavenResolverClientException {
        String ballerinaVersion = client.getBallerinaVersionForPackage(
                "myorg", "mypkg", "1.0.0", BALLERINA_VERSION, localCacheDir);

        Assert.assertEquals(ballerinaVersion, "2201.13.0");
    }

    @Test
    public void testGetDeprecationStatus_notDeprecated() throws MavenResolverClientException {
        boolean deprecated = client.getDeprecationStatus(
                "myorg", "mypkg", "1.0.0", BALLERINA_VERSION, localCacheDir);

        Assert.assertFalse(deprecated);
    }

    @Test
    public void testGetDeprecationStatus_deprecated() throws MavenResolverClientException {
        boolean deprecated = client.getDeprecationStatus(
                "myorg", "mypkg", "0.9.0", BALLERINA_VERSION, localCacheDir);

        Assert.assertTrue(deprecated);
    }


    @Test
    public void testGetPkgSearchMetadata() throws MavenResolverClientException {
        PkgSearchMavenMetadata metadata = client.getPkgSearchMetadata(
                "myquery", BALLERINA_VERSION, localCacheDir);

        Assert.assertNotNull(metadata);
        Assert.assertEquals(metadata.getPackages().size(), 1);

        var pkg = metadata.getPackages().get(0);
        Assert.assertEquals(pkg.getOrg(), "myorg");
        Assert.assertEquals(pkg.getName(), "mypkg");
        Assert.assertEquals(pkg.getVersion(), "1.0.0");
        Assert.assertEquals(pkg.getAuthors().size(), 2);
        Assert.assertTrue(pkg.getAuthors().contains("Alice"));
        Assert.assertTrue(pkg.getAuthors().contains("Bob"));
    }

    @Test
    public void testGetPkgSearchSolrMetadata() throws MavenResolverClientException {
        PkgSearchSolrMavenMetadata metadata = client.getPkgSearchSolrMetadata(
                "myquery", BALLERINA_VERSION, localCacheDir);

        Assert.assertNotNull(metadata);
        Assert.assertEquals(metadata.getCount(), 1);
        Assert.assertEquals(metadata.getLimit(), 10);
        Assert.assertEquals(metadata.getOffset(), 0);
        Assert.assertEquals(metadata.getPackages().size(), 1);

        var pkg = metadata.getPackages().get(0);
        Assert.assertEquals(pkg.getId(), 42L);
        Assert.assertEquals(pkg.getOrg(), "myorg");
        Assert.assertEquals(pkg.getName(), "mypkg");
        Assert.assertEquals(pkg.getPullCount(), 100L);
        Assert.assertEquals(pkg.getBalToolId(), "my-tool");
        Assert.assertEquals(pkg.getKeywords().size(), 2);
        Assert.assertTrue(pkg.getKeywords().contains("network"));
        Assert.assertTrue(pkg.getKeywords().contains("http"));
    }

    @Test
    public void testGetToolSearchMetadata() throws MavenResolverClientException {
        ToolSearchMavenMetadata metadata = client.getToolSearchMetadata(
                "myquery", BALLERINA_VERSION, localCacheDir);

        Assert.assertNotNull(metadata);
        Assert.assertEquals(metadata.getCount(), 1);
        Assert.assertEquals(metadata.getTools().size(), 1);

        var tool = metadata.getTools().get(0);
        Assert.assertEquals(tool.getBalToolId(), "my-tool");
        Assert.assertEquals(tool.getOrg(), "myorg");
        Assert.assertEquals(tool.getName(), "toolpkg");
    }

    @Test
    public void testGetSymbolSearchMetadata() throws MavenResolverClientException {
        SymbolSearchMavenMetadata metadata = client.getSymbolSearchMetadata(
                "myquery", BALLERINA_VERSION, localCacheDir);

        Assert.assertNotNull(metadata);
        Assert.assertEquals(metadata.getCount(), 1);
        Assert.assertEquals(metadata.getSymbols().size(), 1);

        var symbol = metadata.getSymbols().get(0);
        Assert.assertEquals(symbol.getId(), "sym-001");
        Assert.assertEquals(symbol.getName(), "MyFunction");
        Assert.assertEquals(symbol.getSymbolType(), "FUNCTION");
        Assert.assertTrue(symbol.isIsolated());
        Assert.assertFalse(symbol.isRemote());
        Assert.assertTrue(symbol.isResource());
        Assert.assertFalse(symbol.isClosed());
        Assert.assertTrue(symbol.isDistinct());
        Assert.assertFalse(symbol.isReadOnly());
    }

    @Test
    public void testGetConnectorSearchMetadata() throws MavenResolverClientException {
        ConnectorSearchMavenMetadata metadata = client.getConnectorSearchMetadata(
                "myquery", BALLERINA_VERSION, localCacheDir);

        Assert.assertNotNull(metadata);
        Assert.assertEquals(metadata.getCount(), 1);
        Assert.assertEquals(metadata.getLimit(), 5);
        Assert.assertEquals(metadata.getConnectors().size(), 1);

        var connector = metadata.getConnectors().get(0);
        Assert.assertEquals(connector.getId(), "conn-001");
        Assert.assertEquals(connector.getName(), "MyConnector");
        Assert.assertEquals(connector.getDisplayName(), "My Connector");

        var pkgInfo = connector.getPackageInfo();
        Assert.assertNotNull(pkgInfo);
        Assert.assertEquals(pkgInfo.getOrganization(), "myorg");
        Assert.assertEquals(pkgInfo.getName(), "mypkg");
        Assert.assertEquals(pkgInfo.getVersion(), "1.0.0");
        Assert.assertEquals(pkgInfo.getLicenses().size(), 1);
        Assert.assertTrue(pkgInfo.getLicenses().contains("Apache-2.0"));
        Assert.assertEquals(pkgInfo.getAuthors().size(), 1);
        Assert.assertTrue(pkgInfo.getAuthors().contains("Alice"));
    }

    @Test
    public void testGetToolMetadata() throws MavenResolverClientException {
        ToolMavenMetadata metadata = client.getToolMetadata(
                "my-tool", BALLERINA_VERSION, localCacheDir);

        Assert.assertNotNull(metadata);
        Assert.assertEquals(metadata.getOrg(), "myorg");
        Assert.assertEquals(metadata.getName(), "toolpkg");
        Assert.assertEquals(metadata.getVersions().size(), 2);
    }

    @Test
    public void testGetCompatibleToolVersions() throws MavenResolverClientException {
        List<String> versions = client.getCompatibleToolVersions(
                "my-tool", BALLERINA_VERSION, localCacheDir);

        Assert.assertNotNull(versions);
        Assert.assertEquals(versions.size(), 2);
        Assert.assertTrue(versions.contains("1.0.0"));
        Assert.assertTrue(versions.contains("0.5.0"));
    }

    @Test
    public void testGetCompatibleToolVersions_futureTool() throws MavenResolverClientException {
        List<String> versions = client.getCompatibleToolVersions(
                "future-tool", BALLERINA_VERSION, localCacheDir);

        Assert.assertNotNull(versions);
        Assert.assertEquals(versions.size(), 2);
        Assert.assertTrue(versions.contains("1.0.0"));
        Assert.assertTrue(versions.contains("2.0.0"));
    }

    @Test(expectedExceptions = MavenResolverClientException.class)
    public void testGetPackageVersionsInCentralProxy_metadataNotFound() throws MavenResolverClientException {
        MavenResolverClient errorClient = new MavenResolverClient();
        errorClient.addRepository("bad-repo", "file:///nonexistent-path-xyz-does-not-exist");
        errorClient.getPackageVersionsInCentralProxy("myorg", "mypkg", BALLERINA_VERSION, localCacheDir);
    }

    @Test(expectedExceptions = MavenResolverClientException.class)
    public void testGetPkgSearchMetadata_metadataNotFound() throws MavenResolverClientException {
        MavenResolverClient errorClient = new MavenResolverClient();
        errorClient.addRepository("bad-repo", "file:///nonexistent-path-xyz-does-not-exist");
        errorClient.getPkgSearchMetadata("myquery", BALLERINA_VERSION, localCacheDir);
    }

    @Test(expectedExceptions = MavenResolverClientException.class)
    public void testGetToolSearchMetadata_metadataNotFound() throws MavenResolverClientException {
        MavenResolverClient errorClient = new MavenResolverClient();
        errorClient.addRepository("bad-repo", "file:///nonexistent-path-xyz-does-not-exist");
        errorClient.getToolSearchMetadata("myquery", BALLERINA_VERSION, localCacheDir);
    }
}

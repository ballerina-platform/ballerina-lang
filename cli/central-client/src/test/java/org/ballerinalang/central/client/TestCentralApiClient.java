/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.central.client;

import org.awaitility.Duration;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.NoPackageException;
import org.ballerinalang.central.client.model.Package;
import org.ballerinalang.central.client.model.PackageSearchResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.given;
import static org.ballerinalang.central.client.CentralClientConstants.CONTENT_DISPOSITION;
import static org.ballerinalang.central.client.CentralClientConstants.LOCATION;
import static org.ballerinalang.central.client.TestUtils.cleanDirectory;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases to test central api client.
 */
public class TestCentralApiClient extends CentralAPIClient {

    private HttpURLConnection connection = mock(HttpURLConnection.class);
    private ByteArrayOutputStream console;

    private static final Path UTILS_TEST_RESOURCES = Paths.get("src/test/resources/test-resources/utils");
    private static final Path TMP_DIR = UTILS_TEST_RESOURCES.resolve("temp-test-central-api-client");
    private static final String TEST_BAL_VERSION = "slp5";
    private static final String TEST_BALA_NAME = "sf-any.bala";
    private static final String OUTPUT_BALA = "output.bala";
    private static final String WINERY = "winery";
    private static final String ACCESS_TOKEN = "273cc9f6-c333-36ab-aa2q-f08e9513ff5y";

    public TestCentralApiClient() {
        super("", null);
    }

    @Override
    protected HttpURLConnection createHttpUrlConnection(String a) {
        return connection;
    }

    @BeforeClass
    public void setup() throws IOException {
        if (!TMP_DIR.toFile().exists()) {
            Files.createDirectory(TMP_DIR);
        }
    }

    private String readOutput() throws IOException {
        String output;
        output = this.console.toString();
        this.console.close();
        this.console = new ByteArrayOutputStream();
        this.outStream = new PrintStream(this.console);
        return output;
    }

    @BeforeMethod
    public void beforeMethod() {
        this.console = new ByteArrayOutputStream();
        this.outStream = new PrintStream(this.console);
    }

    private void cleanTmpDir() {
        cleanDirectory(TMP_DIR);
    }

    @AfterClass
    public void cleanup() throws IOException {
        cleanTmpDir();
        Files.deleteIfExists(TMP_DIR);
    }

    @Test(description = "Test pull package", enabled = false)
    public void testPullPackage() throws IOException, CentralClientException {
        final String balaUrl = "https://fileserver.dev-central.ballerina.io/2.0/wso2/sf/1.3.5/sf-2020r2-any-1.3.5.bala";
        Path balaPath = UTILS_TEST_RESOURCES.resolve(TEST_BALA_NAME);
        File balaFile = new File(String.valueOf(balaPath));
        InputStream balaStream = null;

        try {
            balaStream = new FileInputStream(balaFile);
            when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_MOVED_TEMP);
            when(connection.getHeaderField(LOCATION)).thenReturn(balaUrl);
            when(connection.getHeaderField(CONTENT_DISPOSITION))
                    .thenReturn("attachment; filename=sf-2020r2-any-1.3.5.bala");
            when(connection.getContentLengthLong()).thenReturn(Files.size(balaPath));
            when(connection.getInputStream()).thenReturn(balaStream);

            this.pullPackage("foo", "sf", "1.3.5", TMP_DIR, "any", TEST_BAL_VERSION, false);

            Assert.assertTrue(TMP_DIR.resolve("1.3.5").resolve("sf-2020r2-any-1.3.5.bala").toFile().exists());
            String buildLog = readOutput();
            given().with().pollInterval(Duration.ONE_SECOND).and().with().pollDelay(Duration.ONE_SECOND).await()
                    .atMost(10, SECONDS)
                    .until(() -> buildLog.contains("foo/sf:1.3.5 pulled from central successfully"));
        } finally {
            if (balaStream != null) {
                balaStream.close();
            }
            cleanTmpDir();
        }
    }

    @Test(description = "Test pull non existing package", expectedExceptions = CentralClientException.class,
            expectedExceptionsMessageRegExp = "error: package not found: foo/sf.*")
    public void testPullNonExistingPackage() throws IOException, CentralClientException {
        String resString = "{\"message\": \"package not found: foo/sf:*_any\"}";
        InputStream resStream = new ByteArrayInputStream(resString.getBytes());

        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
        when(connection.getErrorStream()).thenReturn(resStream);

        this.pullPackage("foo", "sf", "1.3.5", TMP_DIR, "any", TEST_BAL_VERSION, false);
    }

    @Test(description = "Test get package")
    public void testGetPackage() throws IOException, CentralClientException {
        Path packageJsonPath = UTILS_TEST_RESOURCES.resolve("package.json");
        File packageJson = new File(String.valueOf(packageJsonPath));

        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(connection.getInputStream()).thenReturn(new FileInputStream(packageJson));

        Package aPackage = this.getPackage("foo", WINERY, "1.3.5", "any");
        Assert.assertNotNull(aPackage);
        Assert.assertEquals(aPackage.getOrganization(), "foo");
        Assert.assertEquals(aPackage.getName(), WINERY);
        Assert.assertEquals(aPackage.getVersion(), "1.3.5");
    }

    @Test(description = "Test get non existing package", expectedExceptions = NoPackageException.class,
            expectedExceptionsMessageRegExp = "package not found for: bar/winery:2.0.0_any")
    public void testGetNonExistingPackage() throws IOException, CentralClientException {
        String resString = "{\"message\": \"package not found for: bar/winery:2.0.0_any\"}";

        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
        when(connection.getErrorStream()).thenReturn(new ByteArrayInputStream(resString.getBytes()));

        this.getPackage("bar", WINERY, "2.0.0", "any");
    }

    @Test(description = "Test get package with bad request", expectedExceptions = CentralClientException.class,
            expectedExceptionsMessageRegExp = "invalid request received. invaild/unsupported semver version: v2")
    public void testGetPackageWithBadRequest() throws IOException, CentralClientException {
        String resString = "{\"message\": \"invalid request received. invaild/unsupported semver version: v2\"}";

        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_BAD_REQUEST);
        when(connection.getInputStream()).thenReturn(new ByteArrayInputStream(resString.getBytes()));

        this.getPackage("bar", WINERY, "v2", "any");
    }

    @Test(description = "Test push package", enabled = false)
    public void testPushPackage() throws IOException, CentralClientException {
        Path balaPath = UTILS_TEST_RESOURCES.resolve(TEST_BALA_NAME);
        File outputBala = new File(String.valueOf(TMP_DIR.resolve(OUTPUT_BALA)));

        setBallerinaHome();

        try (FileOutputStream outputStream = new FileOutputStream(outputBala)) {
            when(connection.getOutputStream()).thenReturn(outputStream);
            when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);

            this.pushPackage(balaPath, "foo", "sf", "1.3.5", ACCESS_TOKEN, TEST_BAL_VERSION);
            String buildLog = readOutput();
            given().with().pollInterval(Duration.ONE_SECOND).and()
                    .with().pollDelay(Duration.ONE_SECOND)
                    .await().atMost(10, SECONDS)
                    .until(() -> buildLog.contains("foo/sf:1.3.5 pushed to central successfully"));
        }
    }

    @Test(description = "Test push existing package", expectedExceptions = CentralClientException.class,
            expectedExceptionsMessageRegExp = "package already exists: foo/github:1.8.3_2020r2_any")
    public void testPushExistingPackage() throws IOException, CentralClientException {
        String resString = "{\"message\": \"package already exists: foo/github:1.8.3_2020r2_any\"}";
        Path balaPath = UTILS_TEST_RESOURCES.resolve(TEST_BALA_NAME);
        File outputBala = new File(String.valueOf(TMP_DIR.resolve(OUTPUT_BALA)));

        setBallerinaHome();

        try (FileOutputStream outputStream = new FileOutputStream(outputBala)) {
            when(connection.getOutputStream()).thenReturn(outputStream);
            when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_BAD_REQUEST);
            when(connection.getErrorStream()).thenReturn(new ByteArrayInputStream(resString.getBytes()));

            this.pushPackage(balaPath, "foo", "github", "1.8.3", ACCESS_TOKEN, TEST_BAL_VERSION);
        }
    }

    @Test(description = "Test push package request failure", expectedExceptions = CentralClientException.class,
            expectedExceptionsMessageRegExp = "error: failed to push the package: "
                    + "'foo/sf:1.3.5' to the remote repository 'https://api.central.ballerina.io/registry'")
    public void testPushPackageRequestFailure() throws IOException, CentralClientException {
        Path balaPath = UTILS_TEST_RESOURCES.resolve(TEST_BALA_NAME);
        File outputBala = new File(String.valueOf(TMP_DIR.resolve(OUTPUT_BALA)));

        setBallerinaHome();

        try (FileOutputStream outputStream = new FileOutputStream(outputBala)) {
            when(connection.getOutputStream()).thenReturn(outputStream);
            when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_NOT_FOUND);
            when(connection.getURL()).thenReturn(new URL("https://api.central.ballerina.io/registry"));

            this.pushPackage(balaPath, "foo", "sf", "1.3.5", ACCESS_TOKEN, TEST_BAL_VERSION);
        }
    }

    @Test(description = "Test search package")
    public void testSearchPackage() throws IOException, CentralClientException {
        Path packageSearchJsonPath = UTILS_TEST_RESOURCES.resolve("packageSearch.json");
        File packageSearchJson = new File(String.valueOf(packageSearchJsonPath));

        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_OK);
        when(connection.getInputStream()).thenReturn(new FileInputStream(packageSearchJson));

        PackageSearchResult pkgSearchResult = this.searchPackage("org=foo", TEST_BAL_VERSION);
        Assert.assertNotNull(pkgSearchResult);
        Assert.assertEquals(pkgSearchResult.getCount(), 1);
        Assert.assertFalse(pkgSearchResult.getPackages().isEmpty());
        Assert.assertEquals(pkgSearchResult.getPackages().get(0).getOrganization(), "foo");
    }

    @Test(description = "Test search package with bad request", expectedExceptions = CentralClientException.class,
            expectedExceptionsMessageRegExp = "invalid request received. invaild/unsupported org name: foo-org")
    public void testSearchPackageWithBadRequest() throws IOException, CentralClientException {
        String resString = "{\"message\": \"invalid request received. invaild/unsupported org name: foo-org\"}";

        when(connection.getResponseCode()).thenReturn(HttpURLConnection.HTTP_BAD_REQUEST);
        when(connection.getErrorStream()).thenReturn(new ByteArrayInputStream(resString.getBytes()));

        this.searchPackage("org=foo-org", TEST_BAL_VERSION);
    }

    private void setBallerinaHome() {
        final String ballerinaInstallDirProp = "ballerina.home";
        if (System.getProperty(ballerinaInstallDirProp) == null) {
            System.setProperty(ballerinaInstallDirProp, String.valueOf(Paths.get("build")));
        }
    }
}

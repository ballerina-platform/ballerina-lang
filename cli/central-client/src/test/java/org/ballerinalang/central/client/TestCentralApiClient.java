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

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.awaitility.Duration;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.exceptions.NoPackageException;
import org.ballerinalang.central.client.model.Package;
import org.ballerinalang.central.client.model.PackageNameResolutionRequest;
import org.ballerinalang.central.client.model.PackageNameResolutionResponse;
import org.ballerinalang.central.client.model.PackageResolutionRequest;
import org.ballerinalang.central.client.model.PackageResolutionResponse;
import org.ballerinalang.central.client.model.PackageSearchResult;
import org.ballerinalang.central.client.model.ToolResolutionCentralRequest;
import org.ballerinalang.central.client.model.ToolResolutionCentralResponse;
import org.ballerinalang.central.client.model.ToolSearchResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.given;
import static org.ballerinalang.central.client.CentralClientConstants.ACCEPT;
import static org.ballerinalang.central.client.CentralClientConstants.ACCEPT_ENCODING;
import static org.ballerinalang.central.client.CentralClientConstants.APPLICATION_JSON;
import static org.ballerinalang.central.client.CentralClientConstants.APPLICATION_OCTET_STREAM;
import static org.ballerinalang.central.client.CentralClientConstants.AUTHORIZATION;
import static org.ballerinalang.central.client.CentralClientConstants.CONTENT_DISPOSITION;
import static org.ballerinalang.central.client.CentralClientConstants.DIGEST;
import static org.ballerinalang.central.client.CentralClientConstants.IDENTITY;
import static org.ballerinalang.central.client.CentralClientConstants.LOCATION;
import static org.ballerinalang.central.client.TestUtils.cleanDirectory;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases to test central api client.
 */
public class TestCentralApiClient extends CentralAPIClient {

    private ByteArrayOutputStream console;

    private static final Path UTILS_TEST_RESOURCES = Paths.get("src/test/resources/test-resources/utils");
    private static final Path TMP_DIR = UTILS_TEST_RESOURCES.resolve("temp-test-central-api-client");
    private static final String TEST_BAL_VERSION = "slp5";
    private static final String ANY_PLATFORM = "any";
    private static final String TEST_BALA_NAME = "sf-any.bala";
    private static final String TEST_TOOL_BALA_NAME = "baz-toolbox-java17-0.1.0.bala";
    private static final String OUTPUT_BALA = "output.bala";
    private static final String WINERY = "winery";
    private static final String ACCESS_TOKEN = "273cc9f6-c333-36ab-aa2q-f08e9513ff5y";
    private final String balaUrl =
            "https://fileserver.dev-central.ballerina.io/2.0/wso2/sf/1.3.5/sf-2020r2-any-1.3.5.bala";
    private final Call remoteCall = mock(Call.class);
    private final OkHttpClient client = mock(OkHttpClient.class);

    public TestCentralApiClient() {
        super("https://localhost:9090/registry", null, ACCESS_TOKEN);
    }

    @Override
    protected OkHttpClient getClient() {
        return this.client;
    }

    @Override
    protected void closeClient(OkHttpClient client) {
        // do nothing
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

    @Test(description = "Test pull package")
    public void testPullPackage() throws IOException, CentralClientException {
        Path balaPath = UTILS_TEST_RESOURCES.resolve(TEST_BALA_NAME);
        File balaFile = new File(String.valueOf(balaPath));
        String balaFileName = "attachment; filename=sf-2020r2-any-1.3.5.bala";

        try (InputStream ignored = new FileInputStream(balaFile)) {

            Request mockRequest = new Request.Builder()
                    .get()
                    .url("https://localhost:9090/registry/packages/foo/sf/1.3.5")
                    .addHeader(ACCEPT_ENCODING, IDENTITY)
                    .addHeader(ACCEPT, APPLICATION_OCTET_STREAM)
                    .build();
            Response mockResponse = new Response.Builder()
                    .request(mockRequest)
                    .protocol(Protocol.HTTP_1_1)
                    .code(HttpURLConnection.HTTP_MOVED_TEMP)
                    .addHeader(LOCATION, this.balaUrl)
                    .addHeader(CONTENT_DISPOSITION, balaFileName)
                    .addHeader(DIGEST, "sha-256=47e043c80d516234b1e6bd93140f126c9d9e79b5c7c0600cc6316d12504c2cf4")
                    .message("")
                    .body(null)
                    .build();

            Request mockDownloadBalaRequest = new Request.Builder()
                    .get()
                    .url(this.balaUrl)
                    .header(ACCEPT_ENCODING, IDENTITY)
                    .addHeader(CONTENT_DISPOSITION, balaFileName)
                    .build();
            Response mockDownloadBalaResponse = new Response.Builder()
                    .request(mockDownloadBalaRequest)
                    .protocol(Protocol.HTTP_1_1)
                    .code(HttpURLConnection.HTTP_OK)
                    .message("")
                    .body(ResponseBody.create(
                            MediaType.get(APPLICATION_OCTET_STREAM),
                            Files.readAllBytes(balaPath)
                    ))
                    .build();

            when(this.remoteCall.execute()).thenReturn(mockResponse, mockDownloadBalaResponse);
            when(this.client.newCall(any())).thenReturn(this.remoteCall);

            System.setProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM, "true");
            this.pullPackage("foo", "sf", "1.3.5", TMP_DIR, ANY_PLATFORM, TEST_BAL_VERSION, false);

            Path balaDir = TMP_DIR.resolve("1.3.5").resolve("2020r2-any");
            Assert.assertTrue(balaDir.toFile().exists());
            Assert.assertTrue(balaDir.resolve("bala.json").toFile().exists());
            Assert.assertTrue(balaDir.resolve("modules").toFile().exists());
            Assert.assertTrue(balaDir.resolve("dependency-graph.json").toFile().exists());
            Assert.assertTrue(balaDir.resolve("package.json").toFile().exists());

            String buildLog = readOutput();
            given().with().pollInterval(Duration.ONE_SECOND).and().with().pollDelay(Duration.ONE_SECOND).await()
                    .atMost(10, SECONDS)
                    .until(() -> buildLog.contains("foo/sf:1.3.5 pulled from central successfully"));
        } finally {
            cleanTmpDir();
        }
    }

    @Test(description = "Test pull non existing package", expectedExceptions = CentralClientException.class,
            expectedExceptionsMessageRegExp = "error: package not found: foo/sf.*")
    public void testPullNonExistingPackage() throws IOException, CentralClientException {
        String resString = "{\"message\": \"package not found: foo/sf:*_any\"}";

        Request mockRequest = new Request.Builder()
                .get()
                .url("https://localhost:9090/registry/packages/foo/sf/1.3.5")
                .addHeader(ACCEPT_ENCODING, IDENTITY)
                .addHeader(ACCEPT, APPLICATION_OCTET_STREAM)
                .build();
        Response mockResponse = new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_NOT_FOUND)
                .message("")
                .body(ResponseBody.create(
                        MediaType.get(APPLICATION_JSON),
                        resString
                ))
                .build();

        when(this.remoteCall.execute()).thenReturn(mockResponse);
        when(this.client.newCall(any())).thenReturn(this.remoteCall);

        this.pullPackage("foo", "sf", "1.3.5", TMP_DIR, ANY_PLATFORM, TEST_BAL_VERSION, false);
    }

    @DataProvider (name = "provideDataForBalaDownloadFailHttpCodes")
    private Object[][] provideDataForBalaDownloadFailHttpCodes() {

        // invalidRequestErrorMessage here might not be identical to the actual HTTP response error message
        String invalidResponseErrorMessage = "package not found: foo/sf:*_any";
        String unexpectedResponseErrorMessage = "error: failed to pull the package: 'foo/sf:1.3.5'. " +
                "BALA content download from '" + this.balaUrl + "' failed.";
        String remoteRepositoryErrorMessage = unexpectedResponseErrorMessage + " reason:" + invalidResponseErrorMessage;
        String responseString = "{\"message\": \"" + invalidResponseErrorMessage + "\"}";

        Object[][] data = new Object[7][3];
        data[0] = new Object[]{HttpURLConnection.HTTP_NOT_FOUND, invalidResponseErrorMessage,
                getResponseBody(responseString)};
        data[1] = new Object[]{HttpURLConnection.HTTP_INTERNAL_ERROR, remoteRepositoryErrorMessage,
                getResponseBody(responseString)};
        data[2] = new Object[]{HttpURLConnection.HTTP_BAD_GATEWAY, remoteRepositoryErrorMessage,
                getResponseBody(responseString)};
        data[3] = new Object[]{HttpURLConnection.HTTP_UNAVAILABLE, remoteRepositoryErrorMessage,
                getResponseBody(responseString)};
        data[4] = new Object[]{HttpURLConnection.HTTP_GATEWAY_TIMEOUT, remoteRepositoryErrorMessage,
                getResponseBody(responseString)};

        // test BALA download with a response status code not handled specifically
        data[5] = new Object[]{HttpURLConnection.HTTP_FORBIDDEN, unexpectedResponseErrorMessage,
                getResponseBody(responseString)};

        // test BALA download with no response body
        data[6] = new Object[]{HttpURLConnection.HTTP_UNAVAILABLE, unexpectedResponseErrorMessage, null};
        return data;
    }

    private ResponseBody getResponseBody(String responseString) {
        return ResponseBody.create(MediaType.get(APPLICATION_JSON), responseString);
    }

    @Test(description = "Test pull package with different bala download failure response codes",
            dataProvider = "provideDataForBalaDownloadFailHttpCodes")
    public void testPullPackageWithBalaDownloadFailStatusCode(
            int downloadBalaResponseCode, String errorMessage, ResponseBody responseBody)
            throws IOException {
        Path balaPath = UTILS_TEST_RESOURCES.resolve(TEST_BALA_NAME);
        String balaFileName = "attachment; filename=sf-2020r2-any-1.3.5.bala";

        try {
            Request mockPullRequest = new Request.Builder()
                    .get()
                    .url("https://localhost:9090/registry/packages/foo/sf/1.3.5")
                    .addHeader(ACCEPT_ENCODING, IDENTITY)
                    .addHeader(ACCEPT, APPLICATION_OCTET_STREAM)
                    .build();
            Response mockPullResponse = new Response.Builder()
                    .request(mockPullRequest)
                    .protocol(Protocol.HTTP_1_1)
                    .code(HttpURLConnection.HTTP_MOVED_TEMP)
                    .addHeader(LOCATION, this.balaUrl)
                    .addHeader(CONTENT_DISPOSITION, balaFileName)
                    .message("")
                    .body(ResponseBody.create(
                            MediaType.get(APPLICATION_OCTET_STREAM),
                            Files.readAllBytes(balaPath)
                    ))
                    .build();

            Request mockDownloadBalaRequest = new Request.Builder()
                    .get()
                    .url(this.balaUrl)
                    .header(ACCEPT_ENCODING, IDENTITY)
                    .addHeader(CONTENT_DISPOSITION, balaFileName)
                    .build();
            Response mockDownloadBalaResponse = new Response.Builder()
                    .request(mockDownloadBalaRequest)
                    .protocol(Protocol.HTTP_1_1)
                    .code(downloadBalaResponseCode)
                    .message("")
                    .body(responseBody)
                    .build();

            when(this.remoteCall.execute()).thenReturn(mockPullResponse, mockDownloadBalaResponse);
            when(this.client.newCall(any())).thenReturn(this.remoteCall);

            try {
                this.pullPackage("foo", "sf", "1.3.5", TMP_DIR, ANY_PLATFORM, TEST_BAL_VERSION, false);
                Assert.fail("pullPackage should throw a CentralClientException");
            } catch (CentralClientException e) {
                Assert.assertEquals(e.getMessage(), errorMessage);
            }
        } finally {
            cleanTmpDir();
        }
    }

    @Test(description = "Test get package")
    public void testGetPackage() throws IOException, CentralClientException {
        Path packageJsonPath = UTILS_TEST_RESOURCES.resolve("package.json");
        File packageJson = new File(String.valueOf(packageJsonPath));
        Request mockRequest = new Request.Builder()
                .get()
                .url("https://localhost:9090/registry/packages/foo/" + WINERY + "/1.3.5")
                .build();
        Response mockResponse = new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_OK)
                .message("")
                .body(ResponseBody.create(
                        MediaType.get(APPLICATION_JSON),
                        Files.readString(packageJson.toPath())
                ))
                .build();

        when(this.remoteCall.execute()).thenReturn(mockResponse);
        when(this.client.newCall(any())).thenReturn(this.remoteCall);

        Package aPackage = this.getPackage("foo", WINERY, "1.3.5", ANY_PLATFORM, TEST_BAL_VERSION);
        Assert.assertNotNull(aPackage);
        Assert.assertEquals(aPackage.getOrganization(), "foo");
        Assert.assertEquals(aPackage.getName(), WINERY);
        Assert.assertEquals(aPackage.getVersion(), "1.3.5");
    }

    @Test(description = "Test get non existing package", expectedExceptions = NoPackageException.class,
            expectedExceptionsMessageRegExp = "package not found for: bar/winery:2.0.0_any")
    public void testGetNonExistingPackage() throws IOException, CentralClientException {
        String resString = "{\"message\": \"package not found for: bar/winery:2.0.0_any\"}";

        Request mockRequest = new Request.Builder()
                .get()
                .url("https://localhost:9090/registry/packages/bar/" + WINERY + "/2.0.0")
                .build();
        Response mockResponse = new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_NOT_FOUND)
                .message("")
                .body(ResponseBody.create(
                        MediaType.get(APPLICATION_JSON),
                        resString
                ))
                .build();

        when(this.remoteCall.execute()).thenReturn(mockResponse);
        when(this.client.newCall(any())).thenReturn(this.remoteCall);

        this.getPackage("bar", WINERY, "2.0.0", ANY_PLATFORM, TEST_BAL_VERSION);
    }

    @Test(description = "Test get package with bad request", expectedExceptions = CentralClientException.class,
            expectedExceptionsMessageRegExp = "invalid request received. invaild/unsupported semver version: v2")
    public void testGetPackageWithBadRequest() throws IOException, CentralClientException {
        String resString = "{\"message\": \"invalid request received. invaild/unsupported semver version: v2\"}";

        Request mockRequest = new Request.Builder()
                .get()
                .url("https://localhost:9090/registry/packages/bar/" + WINERY + "/v2")
                .build();
        Response mockResponse = new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_BAD_REQUEST)
                .message("")
                .body(ResponseBody.create(
                        MediaType.get(APPLICATION_JSON),
                        resString
                ))
                .build();

        when(this.remoteCall.execute()).thenReturn(mockResponse);
        when(this.client.newCall(any())).thenReturn(this.remoteCall);

        this.getPackage("bar", WINERY, "v2", ANY_PLATFORM, TEST_BAL_VERSION);
    }

    @Test(description = "Test get package with bad gateway", expectedExceptions = CentralClientException.class,
            expectedExceptionsMessageRegExp = "package pushing is temporarily disabled. please try again later.")
    public void testGetPackageWithBadGateway() throws IOException, CentralClientException {
        String resString = "{message: \"package pushing is temporarily disabled. please try again later.\"}";

        Request mockRequest = new Request.Builder()
                .get()
                .url("https://localhost:9090/registry/packages/bar/" + WINERY + "/v2")
                .build();
        Response mockResponse = new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_UNAVAILABLE)
                .message("")
                .body(ResponseBody.create(
                        MediaType.get(APPLICATION_JSON),
                        resString
                ))
                .build();

        when(this.remoteCall.execute()).thenReturn(mockResponse);
        when(this.client.newCall(any())).thenReturn(this.remoteCall);

        this.getPackage("bar", WINERY, "v2", ANY_PLATFORM, TEST_BAL_VERSION);
    }
    
    @Test(description = "Test get package versions")
    public void testGetPackageVersions() throws IOException, CentralClientException {
        Request mockRequest = new Request.Builder()
                .get()
                .url("https://localhost:9090/registry/packages/foo/" + WINERY)
                .build();
        Response mockResponse = new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_2)
                .code(HttpURLConnection.HTTP_OK)
                .message("")
                .body(ResponseBody.create(
                        MediaType.get(APPLICATION_JSON),
                        "[\"2.3.4\", \"1.2.3\"]"
                ))
                .build();
        
        when(this.remoteCall.execute()).thenReturn(mockResponse);
        when(this.client.newCall(any())).thenReturn(this.remoteCall);
        
        List<String> versions = this.getPackageVersions("foo", WINERY, ANY_PLATFORM, TEST_BAL_VERSION);
        Assert.assertEquals(versions.size(), 2);
        Assert.assertEquals(versions.get(0), "2.3.4");
        Assert.assertEquals(versions.get(1), "1.2.3");
    }
    
    @Test(description = "Test get versions of non existing package")
    public void testGetNonExistingPackageVersions() throws IOException, CentralClientException {
        String resString = "{\"message\":\"package not found: ballerina/jballerina:*_any\"}";
        
        Request mockRequest = new Request.Builder()
                .get()
                .url("https://localhost:9090/registry/packages/bar/" + WINERY)
                .build();
        Response mockResponse = new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_2)
                .code(HttpURLConnection.HTTP_NOT_FOUND)
                .message("")
                .body(ResponseBody.create(
                        MediaType.get(APPLICATION_JSON),
                        resString
                ))
                .build();
        
        when(this.remoteCall.execute()).thenReturn(mockResponse);
        when(this.client.newCall(any())).thenReturn(this.remoteCall);
    
        List<String> versions = this.getPackageVersions("bar", WINERY, ANY_PLATFORM, TEST_BAL_VERSION);
        Assert.assertEquals(versions.size(), 0);
    }

    @Test(description = "Test push package")
    public void testPushPackage() throws IOException, CentralClientException {
        System.setProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM, "true");
        Path balaPath = UTILS_TEST_RESOURCES.resolve(TEST_BALA_NAME);

        setBallerinaHome();

        RequestBody balaFileReqBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("bala-file", TEST_BALA_NAME,
                        RequestBody.create(MediaType.parse(APPLICATION_OCTET_STREAM), balaPath.toFile()))
                .build();

        Request mockRequest = new Request.Builder()
                .post(balaFileReqBody)
                .url("https://localhost:9090/registry/packages")
                .addHeader(AUTHORIZATION, "Bearer " + ACCESS_TOKEN)
                .build();
        Response mockResponse = new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_NO_CONTENT)
                .message("")
                .build();

        when(this.remoteCall.execute()).thenReturn(mockResponse);
        when(this.client.newCall(any())).thenReturn(this.remoteCall);

        this.pushPackage(balaPath, "foo", "sf", "1.3.5", ANY_PLATFORM, TEST_BAL_VERSION);
        String buildLog = readOutput();
        given().with().pollInterval(Duration.ONE_SECOND).and()
                .with().pollDelay(Duration.ONE_SECOND)
                .await().atMost(10, SECONDS)
                .until(() -> buildLog.contains("foo/sf:1.3.5 pushed to central successfully"));
    }

    @Test(description = "Test push existing package", expectedExceptions = CentralClientException.class,
            expectedExceptionsMessageRegExp = "package already exists: foo/github:1.8.3_2020r2_any")
    public void testPushExistingPackage() throws IOException, CentralClientException {
        String resString = "{\"message\": \"package already exists: foo/github:1.8.3_2020r2_any\"}";
        Path balaPath = UTILS_TEST_RESOURCES.resolve(TEST_BALA_NAME);
        File outputBala = new File(String.valueOf(TMP_DIR.resolve(OUTPUT_BALA)));

        setBallerinaHome();
        RequestBody balaFileReqBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("bala-file", TEST_BALA_NAME,
                        RequestBody.create(MediaType.parse(APPLICATION_OCTET_STREAM), balaPath.toFile()))
                .build();

        Request mockRequest = new Request.Builder()
                .post(balaFileReqBody)
                .url("https://localhost:9090/registry/packages")
                .addHeader(AUTHORIZATION, "Bearer " + ACCESS_TOKEN)
                .build();
        Response mockResponse = new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_BAD_REQUEST)
                .message("")
                .body(ResponseBody.create(
                        MediaType.get(APPLICATION_JSON),
                        resString
                ))
                .build();

        when(this.remoteCall.execute()).thenReturn(mockResponse);
        when(this.client.newCall(any())).thenReturn(this.remoteCall);

        this.pushPackage(balaPath, "foo", "github", "1.8.3", ANY_PLATFORM, TEST_BAL_VERSION);
    }

    @Test(description = "Test push package request failure", expectedExceptions = CentralClientException.class,
            expectedExceptionsMessageRegExp = "error: failed to push the package: "
                    + "'foo/sf:1.3.5' to the remote repository 'https://localhost:9090/registry/packages'.")
    public void testPushPackageRequestFailure() throws IOException, CentralClientException {
        Path balaPath = UTILS_TEST_RESOURCES.resolve(TEST_BALA_NAME);

        setBallerinaHome();
        RequestBody balaFileReqBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("bala-file", TEST_BALA_NAME,
                        RequestBody.create(MediaType.parse(APPLICATION_OCTET_STREAM), balaPath.toFile()))
                .build();

        Request mockRequest = new Request.Builder()
                .post(balaFileReqBody)
                .url("https://localhost:9090/registry/packages")
                .addHeader(AUTHORIZATION, "Bearer " + ACCESS_TOKEN)
                .build();
        Response mockResponse = new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .message("")
                .build();

        when(this.remoteCall.execute()).thenReturn(mockResponse);
        when(this.client.newCall(any())).thenReturn(this.remoteCall);

        this.pushPackage(balaPath, "foo", "sf", "1.3.5", ANY_PLATFORM, TEST_BAL_VERSION);
    }

    @Test(description = "Test search package")
    public void testSearchPackage() throws IOException, CentralClientException {
        Path packageSearchJsonPath = UTILS_TEST_RESOURCES.resolve("packageSearch.json");

        Request mockRequest = new Request.Builder()
                .get()
                .url("https://localhost:9090/registry/packages/?q=foo")
                .build();
        Response mockResponse = new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_OK)
                .message("")
                .body(ResponseBody.create(
                        MediaType.get(APPLICATION_JSON),
                        Files.readString(packageSearchJsonPath)
                ))
                .build();

        when(this.remoteCall.execute()).thenReturn(mockResponse);
        when(this.client.newCall(any())).thenReturn(this.remoteCall);

        PackageSearchResult pkgSearchResult = this.searchPackage("foo", ANY_PLATFORM, TEST_BAL_VERSION);
        Assert.assertNotNull(pkgSearchResult);
        Assert.assertEquals(pkgSearchResult.getCount(), 1);
        Assert.assertFalse(pkgSearchResult.getPackages().isEmpty());
        Assert.assertEquals(pkgSearchResult.getPackages().get(0).getOrganization(), "foo");
    }

    @Test(description = "Test search package with bad request", expectedExceptions = CentralClientException.class,
            expectedExceptionsMessageRegExp = "invalid request received. invaild/unsupported org name: foo-org")
    public void testSearchPackageWithBadRequest() throws IOException, CentralClientException {
        String resString = "{\"message\": \"invalid request received. invaild/unsupported org name: foo-org\"}";

        Request mockRequest = new Request.Builder()
                .get()
                .url("https://localhost:9090/registry/packages/?q=org=foo-org")
                .build();
        Response mockResponse = new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_BAD_REQUEST)
                .message("")
                .body(ResponseBody.create(
                        MediaType.get(APPLICATION_JSON),
                        resString
                ))
                .build();

        when(this.remoteCall.execute()).thenReturn(mockResponse);
        when(this.client.newCall(any())).thenReturn(this.remoteCall);

        this.searchPackage("org=foo-org", ANY_PLATFORM, TEST_BAL_VERSION);
    }

    private void setBallerinaHome() {
        final String ballerinaInstallDirProp = "ballerina.home";
        if (System.getProperty(ballerinaInstallDirProp) == null) {
            System.setProperty(ballerinaInstallDirProp, String.valueOf(Paths.get("build")));
        }
    }

    @Test(description = "Test push package without logs")
    public void testPushPackageWithoutLogs() throws IOException, CentralClientException {
        System.setProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM, "false");
        Path balaPath = UTILS_TEST_RESOURCES.resolve(TEST_BALA_NAME);

        setBallerinaHome();

        RequestBody balaFileReqBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("bala-file", TEST_BALA_NAME,
                        RequestBody.create(MediaType.parse(APPLICATION_OCTET_STREAM), balaPath.toFile()))
                .build();

        Request mockRequest = new Request.Builder()
                .post(balaFileReqBody)
                .url("https://localhost:9090/registry/packages")
                .addHeader(AUTHORIZATION, "Bearer " + ACCESS_TOKEN)
                .build();
        Response mockResponse = new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_NO_CONTENT)
                .message("")
                .build();

        when(this.remoteCall.execute()).thenReturn(mockResponse);
        when(this.client.newCall(any())).thenReturn(this.remoteCall);

        this.pushPackage(balaPath, "foo", "sf", "1.3.5", ANY_PLATFORM, TEST_BAL_VERSION);
        String buildLog = readOutput();
        given().with().pollInterval(Duration.ONE_SECOND).and()
                .with().pollDelay(Duration.ONE_SECOND)
                .await().atMost(10, SECONDS)
                .until(() -> !(buildLog.contains("foo/sf:1.3.5 pushed to central successfully")));
        System.setProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM, "true");
    }

    @Test(description = "Test search package resolution")
    public void testPackageResolution() throws IOException, CentralClientException {
        String resString = "{\"resolved\":" +
                "[{\"org\":\"ballerina\", " +
                "\"name\":\"http\", " +
                "\"version\":\"1.1.0\", " +
                "\"dependencies\":[]}], " +
                "\"unresolved\":[]}";
        Request mockRequest = new Request.Builder()
                .get()
                .url("https://localhost:9090/registry/packages/resolve-dependencies")
                .build();
        Response mockResponse = new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_OK)
                .message("")
                .body(ResponseBody.create(
                        MediaType.get(APPLICATION_JSON),
                        resString
                ))
                .build();

        when(this.remoteCall.execute()).thenReturn(mockResponse);
        when(this.client.newCall(any())).thenReturn(this.remoteCall);

        PackageResolutionRequest packageResolutionRequest = new PackageResolutionRequest();
        packageResolutionRequest.addPackage("ballerina", "http", "1.0.0", PackageResolutionRequest.Mode.MEDIUM);
        PackageResolutionResponse packageResolutionResponse = this.resolveDependencies(
                packageResolutionRequest, ANY_PLATFORM, TEST_BAL_VERSION);
        PackageResolutionResponse.Package pkg = packageResolutionResponse.resolved().get(0);
        Assert.assertEquals(pkg.org(), "ballerina");
        Assert.assertEquals(pkg.name(), "http");
        Assert.assertEquals(pkg.version(), "1.1.0");
    }

    @Test(description = "Test search package name resolution")
    public void testPackageNameResolution() throws IOException, CentralClientException {
        String resString = """
                {
                   "resolvedModules":[
                      {
                         "organization":"shehanpa",
                         "moduleName":"fb",
                         "version":"0.7.0",
                         "packageName":"fb"
                      }
                   ],
                   "unresolvedModules":[
                      {
                         "organization":"hevayo",
                         "moduleName":"fb",
                         "version":null,
                         "reason":"package not found"
                      }
                   ]
                }""";
        Request mockRequest = new Request.Builder()
                .get()
                .url("https://localhost:9090/registry/packages/resolve-modules")
                .build();
        Response mockResponse = new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_OK)
                .message("")
                .body(ResponseBody.create(
                        MediaType.get(APPLICATION_JSON),
                        resString
                ))
                .build();

        when(this.remoteCall.execute()).thenReturn(mockResponse);
        when(this.client.newCall(any())).thenReturn(this.remoteCall);


        PackageNameResolutionRequest packageResolutionRequest = new PackageNameResolutionRequest();
        PackageNameResolutionResponse packageResolutionResponse = this.resolvePackageNames(
                packageResolutionRequest, ANY_PLATFORM, TEST_BAL_VERSION);
        PackageNameResolutionResponse.Module module = packageResolutionResponse.resolvedModules().get(0);
        Assert.assertEquals(module.getPackageName(), "fb");

        PackageNameResolutionResponse.Module unresolvedModule = packageResolutionResponse.unresolvedModules().get(0);
        Assert.assertNull(unresolvedModule.getPackageName());
    }

    @Test(description = "Test tool version resolution")
    public void testToolResolution() throws IOException, CentralClientException {
        String resString = "{\"resolved\":" +
                "[{\"id\":\"health\", \"orgName\":\"ballerinax\", \"name\":\"health\", \"version\":\"2.1.1\"}, " +
                "{\"id\":\"edi\", \"orgName\":\"ballerina\", \"name\":\"editoolspackage\", \"version\":\"1.0.0\"}], " +
                "\"unresolved\":" +
                "[{\"id\":\"blah\", \"version\":\"1.0.0\", " +
                "\"reason\":\"unexpected error occured while searching for org name of tool id: blah\"}]}";
        Request mockRequest = new Request.Builder()
                .get()
                .url("https://localhost:9090/registry/tools/resolve-dependencies")
                .build();
        Response mockResponse = new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_OK)
                .message("")
                .body(ResponseBody.create(
                        MediaType.get(APPLICATION_JSON),
                        resString
                ))
                .build();

        when(this.remoteCall.execute()).thenReturn(mockResponse);
        when(this.client.newCall(any())).thenReturn(this.remoteCall);

        ToolResolutionCentralRequest toolResolutionRequest = new ToolResolutionCentralRequest();
        toolResolutionRequest.addTool("health", "2.1.0", ToolResolutionCentralRequest.Mode.MEDIUM);
        toolResolutionRequest.addTool("edi", "1.0.0", ToolResolutionCentralRequest.Mode.SOFT);
        toolResolutionRequest.addTool("blah", "1.0.0", ToolResolutionCentralRequest.Mode.HARD);
        ToolResolutionCentralResponse toolResolutionResponse = this.resolveToolDependencies(
                toolResolutionRequest, ANY_PLATFORM, TEST_BAL_VERSION);

        ToolResolutionCentralResponse.ResolvedTool resolvedTool = toolResolutionResponse.resolved().get(0);
        Assert.assertEquals(resolvedTool.id(), "health");
        Assert.assertEquals(resolvedTool.org(), "ballerinax");
        Assert.assertEquals(resolvedTool.name(), "health");
        Assert.assertEquals(resolvedTool.version(), "2.1.1");

        resolvedTool = toolResolutionResponse.resolved().get(1);
        Assert.assertEquals(resolvedTool.id(), "edi");
        Assert.assertEquals(resolvedTool.org(), "ballerina");
        Assert.assertEquals(resolvedTool.name(), "editoolspackage");
        Assert.assertEquals(resolvedTool.version(), "1.0.0");

        ToolResolutionCentralResponse.UnresolvedTool unresolvedTool = toolResolutionResponse.unresolved().get(0);
        Assert.assertEquals(unresolvedTool.id(), "blah");
        Assert.assertEquals(unresolvedTool.version(), "1.0.0");
    }

    @Test(description = "Test search tool")
    public void testSearchTool() throws IOException, CentralClientException {
        Path toolSearchJsonPath = UTILS_TEST_RESOURCES.resolve("toolSearch.json");

        Request mockRequest = new Request.Builder()
                .get()
                .url("https://localhost:9090/registry/tools/?q=foo")
                .build();
        Response mockResponse = new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_OK)
                .message("")
                .body(ResponseBody.create(
                        MediaType.get(APPLICATION_JSON),
                        Files.readString(toolSearchJsonPath)
                ))
                .build();

        when(this.remoteCall.execute()).thenReturn(mockResponse);
        when(this.client.newCall(any())).thenReturn(this.remoteCall);

        ToolSearchResult toolSearchResult = this.searchTool("health", ANY_PLATFORM, TEST_BAL_VERSION);
        Assert.assertNotNull(toolSearchResult);
        Assert.assertEquals(toolSearchResult.getTools().size(), 1);
        Assert.assertEquals(toolSearchResult.getTools().get(0).getBalToolId(), "health");
        Assert.assertEquals(toolSearchResult.getTools().get(0).getOrganization(), "ballerinax");
    }

    @Test(description = "Test pull tool")
    public void testPullTool() throws IOException, CentralClientException {
        Path balaPath = UTILS_TEST_RESOURCES.resolve(TEST_TOOL_BALA_NAME);
        File balaFile = new File(String.valueOf(balaPath));
        String balaFileName = "attachment; filename=baz-toolbox-java17-0.1.0.bala";

        try (InputStream ignored = new FileInputStream(balaFile)) {
            Request mockRequest = new Request.Builder()
                    .get()
                    .url("https://localhost:9090/registry/tools/sample_tool/0.1.0")
                    .addHeader(ACCEPT_ENCODING, IDENTITY)
                    .addHeader(ACCEPT, APPLICATION_OCTET_STREAM)
                    .build();
            String toolBalaUrl = "https://fileserver.dev-central.ballerina.io/2.0/wso2/toolbox/0.1.0/" +
                    "baz-toolbox-java17-0.1.0.bala";
            ResponseBody mockResponseBody = ResponseBody.create(MediaType.parse("application/json"), "{\n" +
                    "    \"organization\": \"baz\",\n" +
                    "    \"name\": \"toolbox\",\n" +
                    "    \"version\": \"0.1.0\",\n" +
                    "    \"balaURL\": \"" + toolBalaUrl + "\",\n" +
                    "    \"platform\": \"java17\"\n}");
            Response mockResponse = new Response.Builder()
                    .request(mockRequest)
                    .protocol(Protocol.HTTP_1_1)
                    .code(HttpURLConnection.HTTP_OK)
                    .addHeader(LOCATION, this.balaUrl)
                    .addHeader(CONTENT_DISPOSITION, balaFileName)
                    .addHeader(DIGEST, "sha-256=47e043c80d516234b1e6bd93140f126c9d9e79b5c7c0600cc6316d12504c2cf4")
                    .message("")
                    .body(mockResponseBody)
                    .build();
            Request mockDownloadBalaRequest = new Request.Builder()
                    .get()
                    .url(toolBalaUrl)
                    .header(ACCEPT_ENCODING, IDENTITY)
                    .addHeader(CONTENT_DISPOSITION, balaFileName)
                    .build();
            Response mockDownloadBalaResponse = new Response.Builder()
                    .request(mockDownloadBalaRequest)
                    .protocol(Protocol.HTTP_1_1)
                    .code(HttpURLConnection.HTTP_OK)
                    .message("")
                    .body(ResponseBody.create(
                            MediaType.get(APPLICATION_OCTET_STREAM),
                            Files.readAllBytes(balaPath)
                    ))
                    .build();

            when(this.remoteCall.execute()).thenReturn(mockResponse, mockDownloadBalaResponse);
            when(this.client.newCall(any())).thenReturn(this.remoteCall);

            System.setProperty(CentralClientConstants.ENABLE_OUTPUT_STREAM, "true");
            this.pullTool("sample_tool", "0.1.0", TMP_DIR, ANY_PLATFORM, TEST_BAL_VERSION, false);

            Path balaDir = TMP_DIR.resolve("baz").resolve("toolbox").resolve("0.1.0").resolve("java17");
            Assert.assertTrue(balaDir.toFile().exists());
            Assert.assertTrue(balaDir.resolve("bala.json").toFile().exists());
            Assert.assertTrue(balaDir.resolve("modules").toFile().exists());
            Assert.assertTrue(balaDir.resolve("dependency-graph.json").toFile().exists());
            Assert.assertTrue(balaDir.resolve("package.json").toFile().exists());
            Assert.assertTrue(balaDir.resolve("package.json").toFile().exists());
            Assert.assertTrue(balaDir.resolve("tool").resolve("bal-tool.json").toFile().exists());
            Assert.assertTrue(balaDir.resolve("tool").resolve("libs").resolve("CompPluginRunnerCommand-1.0.0.jar")
                    .toFile().exists());
        } finally {
            cleanTmpDir();
        }
    }
}

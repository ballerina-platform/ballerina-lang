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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
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
import static org.ballerinalang.central.client.CentralClientConstants.IDENTITY;
import static org.ballerinalang.central.client.CentralClientConstants.LOCATION;
import static org.ballerinalang.central.client.TestUtils.cleanDirectory;
import static org.mockito.Matchers.any;
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
    private static final String OUTPUT_BALA = "output.bala";
    private static final String WINERY = "winery";
    private static final String ACCESS_TOKEN = "273cc9f6-c333-36ab-aa2q-f08e9513ff5y";
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

    @Test(description = "Test pull package", enabled = false)
    public void testPullPackage() throws IOException, CentralClientException {
        final String balaUrl = "https://fileserver.dev-central.ballerina.io/2.0/wso2/sf/1.3.5/sf-2020r2-any-1.3.5.bala";
        Path balaPath = UTILS_TEST_RESOURCES.resolve(TEST_BALA_NAME);
        File balaFile = new File(String.valueOf(balaPath));
        InputStream balaStream = null;

        try {
            balaStream = new FileInputStream(balaFile);

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
                    .addHeader(LOCATION, balaUrl)
                    .addHeader(CONTENT_DISPOSITION, "attachment; filename=sf-2020r2-any-1.3.5.bala")
                    .message("")
                    .body(ResponseBody.create(
                            MediaType.get(APPLICATION_OCTET_STREAM),
                            Files.readAllBytes(balaPath)
                    ))
                    .build();

            when(this.remoteCall.execute()).thenReturn(mockResponse);
            when(this.client.newCall(any())).thenReturn(this.remoteCall);

            this.pullPackage("foo", "sf", "1.3.5", TMP_DIR, ANY_PLATFORM, TEST_BAL_VERSION, false);

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
                "[{\"orgName\":\"ballerina\", " +
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
                packageResolutionRequest, ANY_PLATFORM, TEST_BAL_VERSION, true);
        PackageResolutionResponse.Package pkg = packageResolutionResponse.resolved().get(0);
        Assert.assertEquals(pkg.orgName(), "ballerina");
        Assert.assertEquals(pkg.name(), "http");
        Assert.assertEquals(pkg.version(), "1.1.0");
    }

    @Test(description = "Test search package name resolution")
    public void testPackageNameResolution() throws IOException, CentralClientException {
        String resString = "{\n" +
                "   \"resolvedModules\":[\n" +
                "      {\n" +
                "         \"organization\":\"shehanpa\",\n" +
                "         \"moduleName\":\"fb\",\n" +
                "         \"version\":\"0.7.0\",\n" +
                "         \"packageName\":\"fb\"\n" +
                "      }\n" +
                "   ],\n" +
                "   \"unresolvedModules\":[\n" +
                "      {\n" +
                "         \"organization\":\"hevayo\",\n" +
                "         \"moduleName\":\"fb\",\n" +
                "         \"version\":null,\n" +
                "         \"reason\":\"package not found\"\n" +
                "      }\n" +
                "   ]\n" +
                "}";
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
                packageResolutionRequest, ANY_PLATFORM, TEST_BAL_VERSION, true);
        PackageNameResolutionResponse.Module module = packageResolutionResponse.resolvedModules().get(0);
        Assert.assertEquals(module.getPackageName(), "fb");

        PackageNameResolutionResponse.Module unresolvedModule = packageResolutionResponse.unresolvedModules().get(0);
        Assert.assertEquals(unresolvedModule.getPackageName(), null);
    }
}

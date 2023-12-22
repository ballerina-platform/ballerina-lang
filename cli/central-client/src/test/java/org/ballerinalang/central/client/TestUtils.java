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

import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static org.ballerinalang.central.client.CentralClientConstants.ACCEPT;
import static org.ballerinalang.central.client.CentralClientConstants.ACCEPT_ENCODING;
import static org.ballerinalang.central.client.CentralClientConstants.APPLICATION_JSON;
import static org.ballerinalang.central.client.CentralClientConstants.APPLICATION_OCTET_STREAM;
import static org.ballerinalang.central.client.CentralClientConstants.IDENTITY;
import static org.ballerinalang.central.client.Utils.createBalaInHomeRepo;
import static org.ballerinalang.central.client.Utils.getAsList;
import static org.ballerinalang.central.client.Utils.isApplicationJsonContentType;
import static org.ballerinalang.central.client.Utils.writeBalaFile;

/**
 * Test cases to test utilities.
 */
public class TestUtils {

    private static final Path UTILS_TEST_RESOURCES = Paths.get("src/test/resources/test-resources/utils");
    private final Path tempBalaCache = Paths.get("build").resolve("temp-test-utils-bala-cache");

    @BeforeClass
    public void setUp() throws IOException {
        Files.createDirectories(tempBalaCache);
    }

    @DataProvider(name = "validatePackageVersion")
    public Object[][] provideValidatePackageVersion() {
        return new Object[][] {
                { "1.0.0", true },
                { "1.1.11", true },
                { "2.2.2-snapshot", true },
                { "2.2.2-snapshot-1", true },
                { "2.2.2-alpha", true },
                { "200", false },
                { "2.2.2.2", false }
        };
    }

    @DataProvider(name = "versionsLists")
    public Object[][] provideVersionLists() {
        return new Object[][] {
                { "[]", new ArrayList<>(Collections.emptyList()) },
                { "[\"1.1.11\"]", Collections.singletonList("1.1.11") },
                { "[\"1.0.0\", \"1.2.0\"]", Arrays.asList("1.0.0", "1.2.0") }
        };
    }

    @Test(description = "Test get as list from array given as a string", dataProvider = "versionsLists")
    public void testGetAsList(String versionList, List<String> expectedArray) {
        List<String> versions = getAsList(versionList);
        Assert.assertEquals(versions.size(), expectedArray.size());

        Iterator<String> versionsItr = versions.iterator();
        Iterator<String> expectedItr = expectedArray.iterator();

        while (versionsItr.hasNext() && expectedItr.hasNext()) {
            Assert.assertEquals(versionsItr.next(), expectedItr.next());
        }
    }

    @Test(description = "Test writing bala file from http response")
    public void testWriteBalaFile() throws IOException, CentralClientException {
        final String balaName = "sf-any.bala";
        Path balaFile = UTILS_TEST_RESOURCES.resolve(balaName);
        File initialFile = new File(String.valueOf(balaFile));
        InputStream targetStream = new FileInputStream(initialFile);

        Request mockRequest = new Request.Builder()
                .get()
                .url("https://localhost:9090/registry/packages/wso2/sf/*")
                .build();
        Response mockResponse = new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_BAD_REQUEST)
                .message("")
                .body(ResponseBody.create(
                        MediaType.get(APPLICATION_JSON),
                        Files.readAllBytes(balaFile)
                ))
                .build();

        writeBalaFile(mockResponse, tempBalaCache.resolve(balaName), "wso2/sf:1.1.0",
                10000, System.out, new LogFormatter(), UTILS_TEST_RESOURCES,
                "sha-256=47e043c80d516234b1e6bd93140f126c9d9e79b5c7c0600cc6316d12504c2cf4");

        Assert.assertTrue(tempBalaCache.resolve("package.json").toFile().exists());
        Assert.assertTrue(tempBalaCache.resolve("bala.json").toFile().exists());
        Assert.assertTrue(tempBalaCache.resolve("dependency-graph.json").toFile().exists());
        Assert.assertTrue(tempBalaCache.resolve("modules").toFile().exists());
        cleanBalaCache();
    }

    @Test(description = "Test validate package version", dataProvider = "validatePackageVersion")
    public void testValidatePackageVersion(String version, boolean isValid) throws CentralClientException {
        if (isValid) {
            Utils.validatePackageVersion(version, new LogFormatter());
        } else {
            boolean exceptionThrown = false;
            try {
                Utils.validatePackageVersion(version, new LogFormatter());
            } catch (CentralClientException e) {
                exceptionThrown = true;
                Assert.assertTrue(e.getMessage().contains("Invalid version:"));
            }

            if (!exceptionThrown) {
                Assert.fail("command exception not thrown for invalid version:" + version);
            }
        }
    }

    @Test(description = "Test create bala in given directory")
    public void testCreateBalaInHomeRepo() throws IOException, CentralClientException {
        final String balaName = "sf-any.bala";
        Path balaFile = UTILS_TEST_RESOURCES.resolve(balaName);
        File initialFile = new File(String.valueOf(balaFile));
        InputStream targetStream = new FileInputStream(initialFile);

        Request mockRequest = new Request.Builder()
                .get()
                .url("https://localhost:9090/registry/packages/wso2/sf/*")
                .addHeader(ACCEPT_ENCODING, IDENTITY)
                .addHeader(ACCEPT, APPLICATION_OCTET_STREAM)
                .build();
        Response mockResponse = new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_OK)
                .message("")
                .body(ResponseBody.create(
                        MediaType.get(APPLICATION_JSON),
                        Files.readAllBytes(balaFile)))
                .build();

        final String balaUrl = "https://fileserver.dev-central.ballerina.io/2.0/wso2/sf/1.3.5/sf-2020r2-any-1.3.5.bala";
        createBalaInHomeRepo(mockResponse, tempBalaCache.resolve("wso2").resolve("sf"),
                "wso2", "sf", false, null, balaUrl, "", System.out, new LogFormatter(),
                "sha-256=47e043c80d516234b1e6bd93140f126c9d9e79b5c7c0600cc6316d12504c2cf4");

        Assert.assertTrue(tempBalaCache.resolve("wso2").resolve("sf").resolve("1.3.5").toFile().exists());
        cleanBalaCache();
    }

    @Test(description = "Test create bala when same bala exists in the given directory", 
          dependsOnMethods = "testCreateBalaInHomeRepo")
    public void testCreateBalaInHomeRepoWhenBalaExists() throws IOException {
        final String balaName = "sf-any.bala";
        Path balaFile = UTILS_TEST_RESOURCES.resolve(balaName);
        File initialFile = new File(String.valueOf(balaFile));
        InputStream targetStream = new FileInputStream(initialFile);

        Request mockRequest = new Request.Builder()
                .get()
                .url("https://localhost:9090/registry/packages/wso2/sf/*")
                .addHeader(ACCEPT_ENCODING, IDENTITY)
                .addHeader(ACCEPT, APPLICATION_OCTET_STREAM)
                .build();
        Response mockResponse = new Response.Builder()
                .request(mockRequest)
                .protocol(Protocol.HTTP_1_1)
                .code(HttpURLConnection.HTTP_OK)
                .message("")
                .body(ResponseBody.create(
                        MediaType.get(APPLICATION_JSON),
                        Files.readAllBytes(balaFile)))
                .build();

        final String balaUrl = "https://fileserver.dev-central.ballerina.io/2.0/wso2/sf/1.3.5/sf-2020r2-any-1.3.5.bala";
        try {
            createBalaInHomeRepo(mockResponse,
                    tempBalaCache.resolve("wso2").resolve("sf"), "wso2", "sf", false, null,
                    balaUrl, "", System.out, new LogFormatter(),
                    "sha-256=47e043c80d516234b1e6bd93140f126c9d9e79b5c7c0600cc6316d12504c2cf4");
        } catch (CentralClientException e) {
            Assert.assertTrue(e.getMessage().contains("package already exists in the home repository:"));
        } finally {
            cleanBalaCache();
        }
    }

    @Test
    public void testJsonContentTypeChecker() {
        Assert.assertTrue(isApplicationJsonContentType(APPLICATION_JSON));
        Assert.assertTrue(isApplicationJsonContentType("application/json; charset=utf-8"));
        Assert.assertFalse(isApplicationJsonContentType(APPLICATION_OCTET_STREAM));
    }

    private void cleanBalaCache() {
        cleanDirectory(tempBalaCache);
    }

    static void cleanDirectory(Path path) {
        try {
            Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .filter(item -> !item.getPath().equals(path.toString()))
                    .forEach(File::delete);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create directory:" + path, e);
        }
    }
}

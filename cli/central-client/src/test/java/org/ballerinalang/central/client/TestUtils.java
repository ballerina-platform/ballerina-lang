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

import org.ballerinalang.toml.model.Settings;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.testng.PowerMockTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import static org.ballerinalang.central.client.Utils.createBaloInHomeRepo;
import static org.ballerinalang.central.client.Utils.writeBaloFile;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.wso2.ballerinalang.util.TomlParserUtils.readSettings;

/**
 * Test cases to test utilities.
 */
@PrepareForTest({ RepoUtils.class, System.class })
@PowerMockIgnore("jdk.internal.reflect.*")
public class TestUtils extends PowerMockTestCase {

    private static final Path UTILS_TEST_RESOURCES = Paths.get("src/test/resources/test-resources/utils");
    private static final String TEMP_BALO_CACHE = "temp-test-utils-balo-cache";

    @BeforeClass
    public void setUp() throws IOException {
        Files.createDirectory(UTILS_TEST_RESOURCES.resolve(TEMP_BALO_CACHE));
    }

    @AfterClass
    public void cleanUp() throws IOException {
        Files.deleteIfExists(UTILS_TEST_RESOURCES.resolve(TEMP_BALO_CACHE));
    }

    @DataProvider(name = "validatePackageVersion")
    public Object[][] provideValidatePackageVersion() {
        return new Object[][] {
                { "1.0.0", true },
                { "1.1.11", true },
                { "200", false },
                { "2.2.2.2", false }
        };
    }

    @Test(description = "Test read settings")
    public void testReadSettings() {
        PowerMockito.mockStatic(RepoUtils.class);
        PowerMockito.when(RepoUtils.createAndGetHomeReposPath()).thenReturn(UTILS_TEST_RESOURCES);

        Settings settings = readSettings();
        Assert.assertEquals(settings.getCentral().getAccessToken(), "273cc9f6-c333-36ab-aa2q-f08e9513ff5y");
    }

    @Test(description = "Test initialize proxy")
    public void testInitializeProxy() {
        org.ballerinalang.toml.model.Proxy proxy = new org.ballerinalang.toml.model.Proxy();
        proxy.setHost("http://localhost");
        proxy.setPort(8080);
        proxy.setUserName("pramodya");
        proxy.setPassword("Welcome@123");

        Proxy netProxy = Utils.initializeProxy(proxy);
        Assert.assertNotNull(netProxy);
        Assert.assertEquals(netProxy.type().toString(), "HTTP");
        Assert.assertEquals(netProxy.address().toString(), "http://localhost:8080");
    }

    @Test(description = "Test get access token from Settings.toml")
    public void testGetAccessTokenOfCliFromSettings() {
        PowerMockito.mockStatic(RepoUtils.class);
        PowerMockito.when(RepoUtils.createAndGetHomeReposPath()).thenReturn(UTILS_TEST_RESOURCES);
        Settings settings = Utils.readSettings();

        Assert.assertEquals(Utils.getAccessTokenOfCLI(settings), "273cc9f6-c333-36ab-aa2q-f08e9513ff5y");
    }

    @Test(description = "Test writing balo file from http response")
    public void testWriteBaloFile() throws IOException {
        final String baloName = "sf-any.balo";
        Path baloFile = UTILS_TEST_RESOURCES.resolve(baloName);
        File initialFile = new File(String.valueOf(baloFile));
        InputStream targetStream = new FileInputStream(initialFile);

        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getInputStream()).thenReturn(targetStream);

        writeBaloFile(connection, UTILS_TEST_RESOURCES.resolve(TEMP_BALO_CACHE).resolve(baloName), "wso2/sf:1.1.0",
                10000, System.out, new LogFormatter());

        Assert.assertTrue(UTILS_TEST_RESOURCES.resolve(TEMP_BALO_CACHE).resolve(baloName).toFile().exists());
        cleanBaloCache();
    }

    @Test(description = "Test validate package version", dataProvider = "validatePackageVersion")
    public void testValidatePackageVersion(String version, boolean isValid) {
        if (isValid) {
            Utils.validatePackageVersion(version, new LogFormatter());
        } else {
            boolean exceptionThrown = false;
            try {
                Utils.validatePackageVersion(version, new LogFormatter());
            } catch (CentralClientException e) {
                exceptionThrown = true;
                Assert.assertTrue(e.getMessage().contains("package version could not be detected"));
            }

            if (!exceptionThrown) {
                Assert.fail("command exception not thrown for invalid version:" + version);
            }
        }
    }

    @Test(description = "Test create balo in given directory")
    public void testCreateBaloInHomeRepo() throws IOException {
        final String baloName = "sf-any.balo";
        Path baloFile = UTILS_TEST_RESOURCES.resolve(baloName);
        File initialFile = new File(String.valueOf(baloFile));
        InputStream targetStream = new FileInputStream(initialFile);

        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getContentLengthLong()).thenReturn(Files.size(baloFile));
        when(connection.getInputStream()).thenReturn(targetStream);

        final String baloUrl = "https://fileserver.dev-central.ballerina.io/2.0/wso2/sf/1.3.5/sf-2020r2-any-1.3.5.balo";
        createBaloInHomeRepo(connection, UTILS_TEST_RESOURCES.resolve(TEMP_BALO_CACHE).resolve("wso2").resolve("sf"),
                "wso2/sf", false, baloUrl, "", System.out, new LogFormatter());

        Assert.assertTrue(UTILS_TEST_RESOURCES.resolve(TEMP_BALO_CACHE).resolve("wso2").resolve("sf").resolve("1.3.5")
                .resolve("sf-2020r2-any-1.3.5.balo").toFile().exists());
        cleanBaloCache();
    }

    @Test(description = "Test create balo when same balo exists in the given directory",
            dependsOnMethods = "testCreateBaloInHomeRepo")
    public void testCreateBaloInHomeRepoWhenBaloExists() throws IOException {
        final String baloName = "sf-any.balo";
        Path baloFile = UTILS_TEST_RESOURCES.resolve(baloName);
        File initialFile = new File(String.valueOf(baloFile));
        InputStream targetStream = new FileInputStream(initialFile);

        HttpURLConnection connection = mock(HttpURLConnection.class);
        when(connection.getContentLengthLong()).thenReturn(Files.size(baloFile));
        when(connection.getInputStream()).thenReturn(targetStream);

        final String baloUrl = "https://fileserver.dev-central.ballerina.io/2.0/wso2/sf/1.3.5/sf-2020r2-any-1.3.5.balo";
        try {
            createBaloInHomeRepo(connection,
                    UTILS_TEST_RESOURCES.resolve(TEMP_BALO_CACHE).resolve("wso2").resolve("sf"), "wso2/sf", false,
                    baloUrl, "", System.out, new LogFormatter());
        } catch (CentralClientException e) {
            Assert.assertTrue(e.getMessage().contains("package already exists in the home repository:"));
        } finally {
            cleanBaloCache();
        }
    }

    private void cleanBaloCache() {
        cleanDirectory(UTILS_TEST_RESOURCES.resolve(TEMP_BALO_CACHE));
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

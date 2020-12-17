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

import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
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

import static org.ballerinalang.central.client.Utils.createBaloInHomeRepo;
import static org.ballerinalang.central.client.Utils.getAsList;
import static org.ballerinalang.central.client.Utils.writeBaloFile;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test cases to test utilities.
 */
public class TestUtils {

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

    @Test(description = "Test writing balo file from http response")
    public void testWriteBaloFile() throws IOException, CentralClientException {
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

    @Test(description = "Test create balo in given directory")
    public void testCreateBaloInHomeRepo() throws IOException, CentralClientException {
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

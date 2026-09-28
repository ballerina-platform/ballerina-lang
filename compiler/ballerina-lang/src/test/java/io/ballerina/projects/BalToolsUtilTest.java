/*
 * Copyright (c) 2026, WSO2 LLC. (http://wso2.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.projects;

import io.ballerina.projects.util.BalToolsUtil;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.maven.bala.client.MavenResolverClientException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Tests for {@link BalToolsUtil}.
 */
public class BalToolsUtilTest {

    @Test(description = "tests that a tool bala with a path-traversal platform value is rejected")
    public void testExtractAndPlaceToolWithPathTraversalPlatformRejected()
            throws IOException, MavenResolverClientException {
        Path testRoot = Files.createTempDirectory("bal-tool-platform-traversal-test");
        try {
            String org = "testorg";
            String name = "attacktool";
            String version = "0.1.0";
            Path localRepoPath = testRoot.resolve("local-repo");
            Path balaPath = localRepoPath.resolve(org).resolve(name).resolve(version)
                    .resolve(name + "-" + version + ProjectConstants.BALA_EXTENSION);
            Files.createDirectories(balaPath.getParent());

            // The malicious "platform" value points outside the intended tool repository entirely.
            Path outsideDir = testRoot.resolve("outside-tool-repo").toAbsolutePath();
            String maliciousPlatform = outsideDir.toString().replace("\\", "\\\\");
            String packageJson = "{\"organization\":\"" + org + "\",\"name\":\"" + name + "\",\"version\":\""
                    + version + "\",\"platform\":\"" + maliciousPlatform + "\"}";
            try (ZipOutputStream zipOut = new ZipOutputStream(Files.newOutputStream(balaPath))) {
                zipOut.putNextEntry(new ZipEntry("package.json"));
                zipOut.write(packageJson.getBytes(StandardCharsets.UTF_8));
                zipOut.closeEntry();
            }

            try {
                BalToolsUtil.extractAndPlaceTool(org, name, version, localRepoPath);
                Assert.fail("expected a ProjectException due to the path traversal platform value");
            } catch (ProjectException e) {
                Assert.assertTrue(e.getMessage().contains("invalid platform identifier"),
                        "unexpected exception message: " + e.getMessage());
            }

            Assert.assertFalse(Files.exists(outsideDir),
                    "platform path traversal wrote outside the intended tool repository directory");
        } finally {
            ProjectUtils.deleteDirectory(testRoot);
        }
    }
}

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
package io.ballerina.projects.test.environment;

import io.ballerina.projects.internal.environment.DependencyCachePruner;
import io.ballerina.projects.internal.environment.FingerprintCompilationCache;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;

/**
 * Tests the removal of compiled dependency variants that no build used for a period.
 *
 * @since 2201.14.1
 */
public class DependencyCachePrunerTest {

    private static final String RECENT = "aaaaaaaaaaaaaaaa";
    private static final String OLD = "bbbbbbbbbbbbbbbb";

    private Path userHome;
    private Path cache;

    @BeforeMethod
    public void setUp() throws IOException {
        userHome = Files.createTempDirectory("dependency-cache-pruner");
        cache = userHome.resolve("repositories").resolve("central.ballerina.io").resolve("cache-2201.14.1");
    }

    @AfterMethod
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(userHome.toFile());
    }

    @Test(description = "Variants unused for the period are removed, and recently used ones are kept")
    public void testRemovesOnlyUnusedVariants() throws IOException {
        Path recent = variant("ballerina", "http", "2.18.2", RECENT, Duration.ofDays(1));
        Path old = variant("ballerina", "http", "2.18.2", OLD, Duration.ofDays(40));
        Path oldOnly = variant("ballerina", "uuid", "1.10.2", OLD, Duration.ofDays(40));

        DependencyCachePruner.Result result = DependencyCachePruner.prune(userHome, Duration.ofDays(30));

        Assert.assertEquals(result.removedVariants(), 2);
        Assert.assertTrue(result.freedBytes() > 0);
        Assert.assertTrue(Files.exists(recent), "A recently used variant should be kept");
        Assert.assertFalse(Files.exists(old));
        Assert.assertFalse(Files.exists(oldOnly));
        Assert.assertFalse(Files.exists(cache.resolve("ballerina").resolve("uuid")),
                "Directories that no longer hold a variant should be removed");
        try (var children = Files.list(cache)) {
            Assert.assertTrue(children.noneMatch(path -> path.getFileName().toString().startsWith(".trash-")),
                    "Removed variants should not be left in the cache");
        }
    }

    @Test(description = "Caches that are not variant directories are left as they are")
    public void testKeepsOtherCacheLayouts() throws IOException {
        // A cache written before compiled dependencies were cached per fingerprint.
        Path oldLayout = cache.resolve("ballerina").resolve("http").resolve("2.18.0").resolve("bir");
        Files.createDirectories(oldLayout);
        Files.writeString(oldLayout.resolve("http.bir"), "bir");
        setTime(oldLayout.getParent(), Duration.ofDays(100));

        DependencyCachePruner.Result result = DependencyCachePruner.prune(userHome, Duration.ofDays(30));

        Assert.assertEquals(result.removedVariants(), 0);
        Assert.assertTrue(Files.exists(oldLayout.resolve("http.bir")));
    }

    @Test(description = "A zero period removes every variant")
    public void testZeroPeriodRemovesAllVariants() throws IOException {
        variant("ballerina", "http", "2.18.2", RECENT, Duration.ZERO);
        variant("ballerina", "http", "2.18.2", OLD, Duration.ofDays(40));

        DependencyCachePruner.Result result = DependencyCachePruner.prune(userHome, Duration.ZERO);

        Assert.assertEquals(result.removedVariants(), 2);
        Assert.assertFalse(Files.exists(cache.resolve("ballerina")));
    }

    @Test(description = "Variants of all repositories and distribution versions are considered")
    public void testRemovesFromAllRepositories() throws IOException {
        variant("ballerina", "http", "2.18.2", OLD, Duration.ofDays(40));
        Path local = userHome.resolve("repositories").resolve("local").resolve("cache").resolve("myorg")
                .resolve("lib").resolve("1.0.0").resolve(OLD);
        Files.createDirectories(local.resolve("bir"));
        Files.writeString(local.resolve(FingerprintCompilationCache.LAST_USED_FILE), "");
        setTime(local.resolve(FingerprintCompilationCache.LAST_USED_FILE), Duration.ofDays(40));

        DependencyCachePruner.Result result = DependencyCachePruner.prune(userHome, Duration.ofDays(30));

        Assert.assertEquals(result.removedVariants(), 2);
        Assert.assertFalse(Files.exists(local));
    }

    @Test(description = "Temporary files left by an interrupted write are removed from variants that are kept")
    public void testRemovesStaleTemporaryFiles() throws IOException {
        Path recent = variant("ballerina", "http", "2.18.2", RECENT, Duration.ofDays(1));
        Path staleTemp = recent.resolve("bir").resolve("http.bir.0123.tmp");
        Path currentTemp = recent.resolve("bir").resolve("http.bir.4567.tmp");
        Files.writeString(staleTemp, "partial");
        Files.writeString(currentTemp, "partial");
        setTime(staleTemp, Duration.ofDays(2));

        DependencyCachePruner.prune(userHome, Duration.ofDays(30));

        Assert.assertFalse(Files.exists(staleTemp), "A temporary file of an interrupted write should be removed");
        Assert.assertTrue(Files.exists(currentTemp), "A temporary file of a write in progress should be kept");
        Assert.assertTrue(Files.exists(recent.resolve("bir").resolve("http.bir")));
    }

    @Test(description = "Variants that an earlier removal moved out but could not delete are removed")
    public void testRemovesLeftoverTrash() throws IOException {
        Path recent = variant("ballerina", "http", "2.18.2", RECENT, Duration.ofDays(1));
        // A removal moves a variant out of its version directory before deleting it, e.g. while another process
        // still holds one of its files open.
        Path trash = cache.resolve(".trash-0123");
        Files.createDirectories(trash.resolve("bir"));
        Files.writeString(trash.resolve("bir").resolve("http.bir"), "bir");

        DependencyCachePruner.Result result = DependencyCachePruner.prune(userHome, Duration.ofDays(30));

        Assert.assertEquals(result.removedVariants(), 0, "Leftover trash is not counted as a removed variant");
        Assert.assertFalse(Files.exists(trash), "Leftover trash should be removed");
        Assert.assertTrue(Files.exists(recent), "A recently used variant should be kept");
    }

    /** Creates a variant whose last use was the given period ago. */
    private Path variant(String org, String name, String version, String fingerprint, Duration lastUsedAgo)
            throws IOException {
        Path variant = cache.resolve(org).resolve(name).resolve(version).resolve(fingerprint);
        Files.createDirectories(variant.resolve("bir"));
        Files.writeString(variant.resolve("bir").resolve(name + ".bir"), "bir");
        Path lastUsed = variant.resolve(FingerprintCompilationCache.LAST_USED_FILE);
        Files.writeString(lastUsed, "");
        setTime(lastUsed, lastUsedAgo);
        return variant;
    }

    private static void setTime(Path path, Duration ago) throws IOException {
        Files.setLastModifiedTime(path, FileTime.from(Instant.now().minus(ago)));
    }
}

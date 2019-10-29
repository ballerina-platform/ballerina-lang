/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */
package org.ballerinalang.test.balo.imports;

import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.balo.BaloCreator;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BFileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Test cases for multiple version support.
 */
public class ImportsTests {

    private static final String USER_HOME = "user.home";
    private String previousUserHome;

    @BeforeClass(description = "Build the necessary modules.")
    public void setup() throws IOException {
        Path tempDir = Files.createTempDirectory("temp-bal-home");
        Path tempBir = Paths.get(System.getProperty("user.dir"), "build", "test-bir-temp");
        Path imports = Paths.get("test-src", "balo", "imports");
        Path homeBirCache = tempDir.resolve(Paths.get(".ballerina", "bir_cache" + "-" +
                RepoUtils.getBallerinaVersion()));
        List<Path> birFiles;

        previousUserHome = System.getProperty(USER_HOME);
        System.setProperty(USER_HOME, tempDir.toString());

        // Delete all existing bir files.
        BFileUtil.deleteAll(tempBir, "testModule.bir");

        BaloCreator.createAndSetupBalo(imports.resolve(Paths.get("test_module1", "test_module")).toString(),
                "testOrg", "testModule");
        birFiles = Files.find(tempBir, Integer.MAX_VALUE, (path, attribute) ->
                path.toString().contains("testModule.bir")
                        && !path.toString().contains("imports")).collect(Collectors.toList());
        Path birDir = Files.createDirectories(homeBirCache.resolve(Paths.get("testOrg", "testModule", "1.1.0")));
        BFileUtil.copy(birFiles.get(0), birDir.resolve(Paths.get("testModule.bir")));
        BFileUtil.delete(birFiles.get(0));

        BaloCreator.createAndSetupBalo(imports.resolve(Paths.get("test_module2", "test_module")).toString(),
                "testOrg", "testModule");
        birFiles = Files.find(tempBir, Integer.MAX_VALUE, (path, attribute) ->
                path.toString().contains("testModule.bir")
                        && !path.toString().contains("imports")).collect(Collectors.toList());
        birDir = Files.createDirectories(homeBirCache.resolve(Paths.get("testOrg", "testModule", "1.1.1")));
        BFileUtil.copy(birFiles.get(0), birDir.resolve(Paths.get("testModule.bir")));
        BFileUtil.delete(birFiles.get(0));

        BaloCreator.createAndSetupBalo(imports.resolve(Paths.get("test_module3", "test_module")).toString(),
                "testOrg", "testModule");
        birFiles = Files.find(tempBir, Integer.MAX_VALUE, (path, attribute) ->
                path.toString().contains("testModule.bir")
                        && !path.toString().contains("imports")).collect(Collectors.toList());
        birDir = Files.createDirectories(homeBirCache.resolve(Paths.get("testOrg", "testModule", "1.1.2")));
        BFileUtil.copy(birFiles.get(0), birDir.resolve(Paths.get("testModule.bir")));
        BFileUtil.delete(birFiles.get(0));
    }

    @Test(description = "Get the version from the source file.")
    public void testVersionSupportImportFromSourceFile() {
        CompileResult result = BCompileUtil.compile(this, "/test-src/balo/imports/test-case1/", "testCase1");
        BValue[] returns = BRunUtil.invoke(result, "cal");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 20);
    }

    @Test(description = "Get the version from the toml file.")
    public void testVersionSupportImportFromTomlFile() {
        CompileResult result = BCompileUtil.compile(this, "/test-src/balo/imports/test-case2/", "testCase2");
        BValue[] returns = BRunUtil.invoke(result, "cal");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 25);
    }

    @Test(description = "Get the version from the lock file.")
    public void testVersionSupportImportFromLockFile() {
        CompileResult result = BCompileUtil.compile(this, "/test-src/balo/imports/test-case3/", "testCase3");
        BValue[] returns = BRunUtil.invoke(result, "cal");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 30);
    }

    @Test(description = "Get the version from a single source file.")
    public void testVersionSupportImportInSingleFile() {
        CompileResult result = BCompileUtil.compile("test-src/balo/imports/test-case4/main.bal");
        BValue[] returns = BRunUtil.invoke(result, "cal");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 20);
    }

    @Test(description = "Get the version from a single source file without alias.")
    public void testVersionSupportImportInSingleFileWithoutAlias() {
        CompileResult result = BCompileUtil.compile("test-src/balo/imports/test-case5/main.bal");
        BValue[] returns = BRunUtil.invoke(result, "cal");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BInteger);
        Assert.assertEquals(((BInteger) returns[0]).intValue(), 25);
    }

    @AfterClass(description = "Set the home to previous location.", alwaysRun = true)
    public void cleanup() {
        System.setProperty(USER_HOME, previousUserHome);
    }
}



/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.packerina.init;

import org.ballerinalang.packerina.init.models.FileType;
import org.ballerinalang.packerina.init.models.ModuleMdFile;
import org.ballerinalang.packerina.init.models.SrcFile;
import org.ballerinalang.toml.model.Manifest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * Test cases for ballerina init command.
 */
public class InitHandlerTest {
    private Path tmpDir;

    @BeforeClass
    public void setup() throws IOException {
        tmpDir = Files.createTempDirectory("init-command-files-");
    }

    @Test(description = "Test if the generated source are runnable.")
    public void testGeneratedSourceContent() throws IOException {
        Manifest manifest = new Manifest();
        manifest.getProject().setOrgName("wso2");
        manifest.getProject().setVersion("1.0.0");

        SrcFile packageFile = new SrcFile("wso2_abc", FileType.SERVICE);
        SrcFile mainFile = new SrcFile("main_runner", FileType.MAIN);
        List<SrcFile> srcFiles = new ArrayList<>();
        srcFiles.add(packageFile);
        srcFiles.add(mainFile);

        List<ModuleMdFile> moduleMdFiles = new ArrayList<>();
        ModuleMdFile moduleMdFileForService = new ModuleMdFile("wso2_abc", FileType.SERVICE);
        ModuleMdFile moduleMdFileForMain = new ModuleMdFile("main_runner", FileType.MAIN);

        moduleMdFiles.add(moduleMdFileForService);
        moduleMdFiles.add(moduleMdFileForMain);

        InitHandler.initialize(tmpDir, manifest, srcFiles, moduleMdFiles);

        Path tomlFile = tmpDir.resolve("Ballerina.toml");
        byte[] tomlFileBytes = Files.readAllBytes(tomlFile);

        String tomlFileContents = new String(tomlFileBytes, Charset.defaultCharset());
        Assert.assertTrue(tomlFileContents.contains("[project]"), "Project header missing in Ballerina.toml");
        Assert.assertTrue(tomlFileContents.contains("orgName = \"" + manifest.getProject().getOrgName() + "\""),
                          "orgName missing in Ballerina.toml");
        Assert.assertTrue(tomlFileContents.contains("version = \"" + manifest.getProject().getVersion() + "\""),
                          "version missing in Ballerina.toml");

        Path servicesBalFile = tmpDir.resolve(packageFile.getName()).resolve("hello_service.bal");
        Path mainBalFile = tmpDir.resolve(mainFile.getName());

        Assert.assertTrue(Files.exists(servicesBalFile), "Module not generated.");
        Assert.assertTrue(Files.exists(mainBalFile), "Main file not generated.");
    }

    @AfterClass
    public void cleanup() throws IOException {
        Files.walk(tmpDir)
             .sorted(Comparator.reverseOrder())
             .forEach(path -> {
                 try {
                     Files.delete(path);
                 } catch (IOException e) {
                     Assert.fail(e.getMessage(), e);
                 }
             });
    }
}

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

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.packerina.init.models.SrcFile;
import org.ballerinalang.toml.model.Manifest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;


/**
 * Test cases for ballerina init command.
 */
public class InitHandlerTest {
    Path tmpDir;
    @BeforeClass
    public void setup() throws IOException {
        tmpDir = Files.createTempDirectory("init-command-files-");
    }
    
    @Test(description = "Test if the generated source are runnable.", enabled = false)
    public void testGeneratedSourceContent() throws IOException {
        Manifest manifest = new Manifest();
        manifest.setName("wso2");
        manifest.setVersion("1.0.0");
    
        SrcFile packageFile = new SrcFile("wso2_abc", SrcFile.SrcFileType.SERVICE);
        SrcFile mainFile = new SrcFile("main_runner", SrcFile.SrcFileType.MAIN);
        List<SrcFile> srcFiles = new ArrayList<>();
        srcFiles.add(packageFile);
        srcFiles.add(mainFile);
        
        InitHandler.initialize(tmpDir, manifest, srcFiles);
    
        Path tomlFile = tmpDir.resolve("Ballerina.toml");
        byte[] tomlFileBytes = Files.readAllBytes(tomlFile);
    
        String tomlFileContents = new String(tomlFileBytes, Charset.defaultCharset());
        Assert.assertTrue(tomlFileContents.contains("[project]"), "Project header missing in Ballerina.toml");
        Assert.assertTrue(tomlFileContents.contains("org-name = \"" + manifest.getName() + "\""),
                "Org-Name missing in Ballerina.toml");
        Assert.assertTrue(tomlFileContents.contains("version = \"" + manifest.getVersion() + "\""),
                "Version missing in Ballerina.toml");
        
        Path servicesBalFile = tmpDir.resolve(packageFile.getName()).resolve("services.bal");
        Path mainBalFile = tmpDir.resolve(mainFile.getName());
    
        Assert.assertTrue(Files.exists(servicesBalFile), "Package not generated.");
        Assert.assertTrue(Files.exists(mainBalFile), "Main file not generated.");
        
        CompileResult serviceFileCompileResult = BCompileUtil.compile(servicesBalFile.getParent().toString());
        Assert.assertFalse(serviceFileCompileResult.getDiagnostics().length > 0,
                "Errors found in the generated service files.");
    
        CompileResult mainFileCompileResult = BCompileUtil.compile(mainBalFile.toString());
        Assert.assertFalse(mainFileCompileResult.getDiagnostics().length > 0,
                "Errors found in the generated service files.");
    }
    
    @AfterClass
    public void teardown() throws IOException {
        Files.walkFileTree(tmpDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}

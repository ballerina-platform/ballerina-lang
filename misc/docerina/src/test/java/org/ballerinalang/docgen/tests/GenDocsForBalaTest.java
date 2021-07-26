/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.docgen.tests;

import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.bala.BalaProject;
import io.ballerina.projects.repos.TempDirCompilationCache;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

/**
 * Test generating API docs for a bala.
 */
public class GenDocsForBalaTest {
    private final Path resourceDir = Paths.get("src", "test", "resources");
    private Path docsPath;
    
    @BeforeMethod
    public void setup() throws IOException {
        this.docsPath = Files.createTempDirectory("bala-doc-gen-" + System.currentTimeMillis());
    }
    
    @Test
    public void generatingDocsForBalaTest() throws IOException {
        Path balaPath = this.resourceDir.resolve("balas").resolve("foo-sf-any-1.3.5.bala");
        
        ProjectEnvironmentBuilder defaultBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
        defaultBuilder.addCompilationCacheFactory(TempDirCompilationCache::from);
        BalaProject balaProject = BalaProject.loadProject(defaultBuilder, balaPath);

        BallerinaDocGenerator.generateAPIDocs(balaProject, this.docsPath.toString(), true);
    
        String sfModuleApiDocsJsonAsString = Files.readString(
                this.docsPath.resolve("foo").resolve("sf").resolve("1.3.5")
                        .resolve(BallerinaDocGenerator.API_DOCS_JSON));
        Assert.assertTrue(sfModuleApiDocsJsonAsString.contains("## Module Overview\\n\\n" +
                        "Module.md content [Ballerina](https://ballerina.io)."),
                "Module.md content is missing");
        Assert.assertTrue(sfModuleApiDocsJsonAsString.contains("Block"), "Block type is missing");
        Assert.assertTrue(sfModuleApiDocsJsonAsString.contains("\"summary\":" +
                        "\"Module.md content [Ballerina](https://ballerina.io).\\n\""),
                "Module summary missing");

        String sfWorldModuleApiDocsJsonAsString = Files.readString(
                this.docsPath.resolve("foo").resolve("sf.world").resolve("1.3.5")
                        .resolve(BallerinaDocGenerator.API_DOCS_JSON));
        Assert.assertTrue(sfWorldModuleApiDocsJsonAsString.contains("PersonZ"), "PersonZ class is missing");
    }
    
    @AfterMethod
    public void cleanUp() throws IOException {
        if (Files.exists(this.docsPath)) {
            Files.walk(this.docsPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
    }
}

/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects.test;

import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.model.Target;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Contains tests related to the target directory of a project.
 *
 * @since 2.0.0
 */
public class TestTarget {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/");

    @Test(description = "tests writing of the BIR")
    public void testTarget() throws IOException {
        Path projectPath = RESOURCE_DIRECTORY.resolve("myproject");

        // 1) Initialize the project instance
        Project project = null;
        try {
            project = BuildProject.loadProject(projectPath);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        // 2) Load the package
        Package currentPackage = project.currentPackage();

        Target projectTarget = new Target(project.sourceRoot());
        Path baloCachePath = projectTarget.getBaloCachePath(currentPackage);
        Assert.assertEquals(baloCachePath.toString(),
                projectPath.toAbsolutePath().resolve("target").resolve("caches").resolve("balo_cache").resolve(
                        "sameera-myproject-any-0.1.0.balo").toString());

        // 3) Load the default module
        Module defaultModule = currentPackage.getDefaultModule();

        Path birCachePath = projectTarget.getBirCachePath(defaultModule);
        Path executablePath = projectTarget.getExecutablePath(defaultModule);

        Assert.assertEquals(birCachePath.toString(),
                projectPath.toAbsolutePath().resolve("target").resolve("caches").resolve("bir_cache")
                        .resolve("sameera").resolve("myproject").resolve("0.1.0").resolve("myproject.bir").toString());
        Assert.assertEquals(executablePath.toString(),
                projectPath.toAbsolutePath().resolve("target").resolve("caches").resolve("jar_cache")
                        .resolve("sameera").resolve("myproject").resolve("0.1.0").resolve("myproject.jar").toString());

        Module storageModule = null;
        for (ModuleId moduleId : currentPackage.moduleIds()) {
            if (moduleId != defaultModule.moduleId()
                    && currentPackage.module(moduleId).moduleName().moduleNamePart().equals("storage")) {
                storageModule = currentPackage.module(moduleId);
                break;
            }
        }
        Assert.assertNotNull(storageModule);
        Path otherBirCachePath = projectTarget.getBirCachePath(storageModule);

        Assert.assertEquals(otherBirCachePath.toString(),
                projectPath.toAbsolutePath().resolve("target").resolve("caches").resolve("bir_cache")
                        .resolve("sameera").resolve("myproject").resolve("0.1.0").resolve("storage").resolve(
                                "storage.bir").toString());
    }
}

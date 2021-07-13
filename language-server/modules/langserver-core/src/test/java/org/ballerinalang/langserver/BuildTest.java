/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver;

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.BuildOptionsBuilder;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.ProjectLoader;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;

public class BuildTest {
    
    @Test
    public void testOnlineBuild() {
        System.clearProperty("LANG_REPO_BUILD");
        Path srcRoot = Path.of("/home/imesha/Documents/WSO2/Ballerina/Projects/test_project/projects/test");
        BuildOptions options = new BuildOptionsBuilder().offline(false).build();
        Project project = ProjectLoader.loadProject(srcRoot, options);
        System.out.println(project.sourceRoot());

        PackageCompilation compilation = project.currentPackage().getCompilation();
        System.out.println(compilation.diagnosticResult().errorCount());
        compilation.diagnosticResult().diagnostics()
                .forEach(diagnostic -> System.out.println(diagnostic.message()));
        Assert.assertFalse(compilation.diagnosticResult().hasErrors());
    }

}

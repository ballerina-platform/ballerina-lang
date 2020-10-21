/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test;

import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.model.Target;
import org.ballerinalang.packerina.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.projects.util.ProjectConstants.BLANG_COMPILED_JAR_EXT;

/**
 * Helper to drive test source compilation.
 *
 * @since 2.0.0
 */
public class BCompileUtil {

    private static Path testSourcesDirectory = Paths.get("src/test/resources").toAbsolutePath();
    private static Path testBuildDirectory = Paths.get("build").toAbsolutePath().normalize();

    public static CompileResult compile(String sourceFilePath) {

        Path sourcePath = Paths.get(sourceFilePath);
        String sourceFileName = sourcePath.getFileName().toString();
        Path sourceRoot = testSourcesDirectory.resolve(sourcePath.getParent());

        Path projectPath = Paths.get(sourceRoot.toString(), sourceFileName);
        Project project = ProjectLoader.loadProject(projectPath);
        Package currentPackage = project.currentPackage();
        PackageCompilation packageCompilation = currentPackage.getCompilation();

        if (packageCompilation.diagnostics().size() > 0) {
            return new CompileResult(currentPackage);
        }

        Path jarFilePath;
        try {
            jarFilePath = jarFilePath(project);
        } catch (IOException e) {
            throw new RuntimeException("error while creating the jar target directory at " + testBuildDirectory, e);
        }

        CompileResult compileResult = new CompileResult(currentPackage, jarFilePath);

        packageCompilation.emit(PackageCompilation.OutputType.JAR, jarFilePath);

        try {
            BRunUtil.runInit(compileResult);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("error while invoking init method of " + project.sourceRoot(), e);
        }

        return compileResult;
    }

    private static Path jarFilePath(Project project) throws IOException {
        Target testTarget = new Target(testBuildDirectory);

        Path jarCachePath = testTarget.getJarCachePath();
        if (!(project instanceof SingleFileProject)) {
            return jarCachePath;
        }

        Package currentPackage = project.currentPackage();
        Module defaultModule = currentPackage.getDefaultModule();
        DocumentId documentId = defaultModule.documentIds().iterator().next();
        String documentName = defaultModule.document(documentId).name();
        String executableName = FileUtils.geFileNameWithoutExtension(Paths.get(documentName));
        if (executableName == null) {
            throw new RuntimeException("cannot identify executable name for : " + defaultModule.moduleName());
        }
        return jarCachePath.resolve(executableName + BLANG_COMPILED_JAR_EXT).toAbsolutePath().normalize();
    }

}

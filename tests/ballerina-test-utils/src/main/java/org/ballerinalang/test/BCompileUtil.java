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
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JdkVersion;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.model.Target;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.packerina.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.projects.util.ProjectConstants.DIST_CACHE_DIRECTORY;

/**
 * Helper to drive test source compilation.
 *
 * @since 2.0.0
 */
public class BCompileUtil {

    private static Path testSourcesDirectory = Paths.get("src/test/resources").toAbsolutePath().normalize();
    private static Path testBuildDirectory = Paths.get("build").toAbsolutePath().normalize();

    public static CompileResult compile(String sourceFilePath) {
        Path sourcePath = Paths.get(sourceFilePath);
        String sourceFileName = sourcePath.getFileName().toString();
        Path sourceRoot = testSourcesDirectory.resolve(sourcePath.getParent());

        Path projectPath = Paths.get(sourceRoot.toString(), sourceFileName);
        Project project = ProjectLoader.loadProject(projectPath);

        Package currentPackage = project.currentPackage();
        PackageCompilation packageCompilation = currentPackage.getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JdkVersion.JAVA_11);
        if (jBallerinaBackend.hasDiagnostics()) {
            return new CompileResult(currentPackage, jBallerinaBackend.diagnostics());
        }

        Path jarTargetPath = jarTargetPath(currentPackage);
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.JAR, jarTargetPath);

        CompileResult compileResult = new CompileResult(currentPackage, jBallerinaBackend.diagnostics(), jarTargetPath);
        invokeModuleInit(compileResult);
        return compileResult;
    }

    public static CompileResult compileAndCacheBalo(String sourceFilePath) {
        Path sourcePath = Paths.get(sourceFilePath);
        String sourceFileName = sourcePath.getFileName().toString();
        Path sourceRoot = testSourcesDirectory.resolve(sourcePath.getParent());

        Path projectPath = Paths.get(sourceRoot.toString(), sourceFileName);
        Project project = ProjectLoader.loadProject(projectPath);

        if (isSingleFileProject(project)) {
            throw new RuntimeException("single file project is given for compilation at " + project.sourceRoot());
        }

        Package currentPackage = project.currentPackage();
        PackageCompilation packageCompilation = currentPackage.getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JdkVersion.JAVA_11);
        if (jBallerinaBackend.hasDiagnostics()) {
            return new CompileResult(currentPackage, jBallerinaBackend.diagnostics());
        }

        Path jarCachePath = jarCachePath(currentPackage);
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.JAR, jarCachePath);

        Path birCachePath = birCachePath(currentPackage);
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.BIR, birCachePath);

        Path baloCachePath = baloCachePath(currentPackage);
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALO, baloCachePath);

        CompileResult compileResult = new CompileResult(currentPackage, jBallerinaBackend.diagnostics(), jarCachePath);
        invokeModuleInit(compileResult);
        return compileResult;
    }

    private static void invokeModuleInit(CompileResult compileResult) {
        if (compileResult.getDiagnostics().length > 0) {
            return;
        }

        try {
            BRunUtil.runInit(compileResult);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("error while invoking init method of " + compileResult.projectSourceRoot(), e);
        }
    }

    private static Path jarTargetPath(Package pkg) {
        try {
            Target target = new Target(testBuildDirectory);
            Path jarTargetPath = target.getJarCachePath();

            if (isSingleFileProject(pkg.project())) {
                Module defaultModule = pkg.getDefaultModule();
                DocumentId documentId = defaultModule.documentIds().iterator().next();
                String documentName = defaultModule.document(documentId).name();
                String executableName = FileUtils.geFileNameWithoutExtension(Paths.get(documentName));
                if (executableName == null) {
                    throw new RuntimeException("cannot identify executable name for " + defaultModule.moduleName());
                }
                jarTargetPath = jarTargetPath.resolve(executableName).toAbsolutePath().normalize();
            } else {
                jarTargetPath = jarTargetPath.resolve(pkg.packageOrg().toString()).resolve(pkg.packageName().value()).
                        resolve(pkg.packageVersion().version().toString());
            }

            Files.createDirectories(jarTargetPath);
            return jarTargetPath;
        } catch (IOException e) {
            throw new RuntimeException("error while creating the jar cache directory at " + testBuildDirectory, e);
        }
    }

    private static boolean isSingleFileProject(Project project) {
        return project instanceof SingleFileProject;
    }

    private static Path jarCachePath(Package pkg) {
        try {
            Path cache = cachePathForPackage(pkg);
            Path jarCache = cache.resolve("jar");
            Files.createDirectories(jarCache);
            return jarCache;
        } catch (IOException e) {
            throw new RuntimeException("error while creating the jar cache directory at " + testBuildDirectory, e);
        }
    }

    private static Path birCachePath(Package pkg) {
        try {
            Path cache = cachePathForPackage(pkg);
            Path birCache = cache.resolve("bir");
            Files.createDirectories(birCache);

            return birCache;
        } catch (IOException e) {
            throw new RuntimeException("error while creating the bir cache directory at " + testBuildDirectory, e);
        }
    }

    private static Path cachePathForPackage(Package pkg) throws IOException {
        Path distributionCache = testBuildDirectory.resolve(DIST_CACHE_DIRECTORY);
        Path cache = distributionCache.resolve("cache")
                .resolve(pkg.packageOrg().toString())
                .resolve(pkg.packageName().value())
                .resolve(pkg.packageVersion().version().toString());
        Files.createDirectories(cache);

        return cache;
    }

    private static Path baloCachePath(Package pkg) {
        try {
            Path distributionCache = testBuildDirectory.resolve(DIST_CACHE_DIRECTORY);
            Path balos = distributionCache.resolve("balo")
                    .resolve(pkg.packageOrg().toString())
                    .resolve(pkg.packageName().value())
                    .resolve(pkg.packageVersion().version().toString());
            Files.createDirectories(balos);

            String baloName = ProjectUtils.getBaloName(pkg);
            return balos.resolve(baloName);
        } catch (IOException e) {
            throw new RuntimeException("error while creating the balo distribution cache directory at " +
                    testBuildDirectory, e);
        }
    }
}

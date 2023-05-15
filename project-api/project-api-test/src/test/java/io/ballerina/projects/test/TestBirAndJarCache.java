/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.projects.test;

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.CompilationCache;
import io.ballerina.projects.CompilationCacheFactory;
import io.ballerina.projects.CompilerBackend;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.internal.model.Target;
import io.ballerina.projects.repos.FileSystemCache;
import io.ballerina.projects.util.ProjectConstants;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.projects.util.ProjectConstants.BLANG_COMPILED_JAR_EXT;
import static io.ballerina.projects.util.ProjectUtils.getThinJarFileName;

/**
 * Contains cases to test the BirWriter.
 *
 * @since 2.0.0
 */
public class TestBirAndJarCache {
    private static final Path RESOURCE_DIRECTORY = Paths.get("src/test/resources/");

    @Test(description = "tests writing of the BIR and Jar files")
    public void testBirAndJarCaching() throws IOException {
        Path projectPath = RESOURCE_DIRECTORY.resolve("balawriter").resolve("projectOne");

        // 1) Initialize the project instance
        Path cacheDirPath = null;
        TestCompilationCacheFactory testCompCacheFactory = null;
        BuildProject project = null;
        try {
            // Create a project with a custom compilation cache
            cacheDirPath = Files.createTempDirectory("test-compilation-cache" + System.nanoTime());
            testCompCacheFactory = new TestCompilationCacheFactory(cacheDirPath);
            ProjectEnvironmentBuilder environmentBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
            environmentBuilder.addCompilationCacheFactory(testCompCacheFactory);
            BuildOptions buildOptions = BuildOptions.builder().setEnableCache(true).build();
            project = TestUtils.loadBuildProject(environmentBuilder, projectPath, buildOptions);
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        // 2) Issue a compilation and code generation
        Package currentPackage = project.currentPackage();
        PackageCompilation pkgCompilation = currentPackage.getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(pkgCompilation, JvmTarget.JAVA_11);

        int numOfModules = currentPackage.moduleIds().size();
        TestCompilationCache testCompilationCache = testCompCacheFactory.compilationCache();
        Assert.assertEquals(testCompilationCache.birCachedCount, numOfModules);
        // numOfModules * 2 : This includes testable jars as well
        Assert.assertEquals(testCompilationCache.jarCachedCount, numOfModules);

        Stream<Path> pathStream = Files.find(cacheDirPath, 100,
                (path, fileAttributes) -> !Files.isDirectory(path) &&
                        (path.getFileName().toString().endsWith(".bir") ||
                                path.getFileName().toString().endsWith(".jar")));

        List<String> foundPaths = pathStream
                .map(path -> path.getFileName().toString())
                .collect(Collectors.toList());
        for (ModuleId moduleId : currentPackage.moduleIds()) {
            Module module = currentPackage.module(moduleId);
            ModuleName moduleName = module.moduleName();
            String jarName = getThinJarFileName(module.descriptor().org(),
                                                moduleName.toString(),
                                                module.descriptor().version());
            Assert.assertTrue(foundPaths.contains(jarName + BLANG_COMPILED_JAR_EXT));
        }
    }

    @Test
    public void testCachingWhenCodeGenHasErrors() throws IOException {
        Path projectPath = RESOURCE_DIRECTORY.resolve("project_with_nonexisting_interop");
        BuildProject buildProject = TestUtils.loadBuildProject(projectPath);
        PackageCompilation compilation = buildProject.currentPackage().getCompilation();
        Assert.assertFalse(compilation.diagnosticResult().hasErrors(),
                TestUtils.getDiagnosticsAsString(compilation.diagnosticResult()));

        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(compilation, JvmTarget.JAVA_11);
        Assert.assertEquals(jBallerinaBackend.diagnosticResult().errorCount(), 1,
                TestUtils.getDiagnosticsAsString(jBallerinaBackend.diagnosticResult()));
        Path cacheDir = new Target(buildProject.targetDir()).cachesPath();
        Assert.assertFalse(Files.exists(cacheDir.resolve(ProjectConstants.REPO_BIR_CACHE_NAME)));
        Assert.assertFalse(Files.exists(cacheDir.resolve(jBallerinaBackend.targetPlatform().code())));
    }

    /**
     * An instance of {@code CompilationCacheFactory} used for testing purposes.
     */
    private static class TestCompilationCacheFactory implements CompilationCacheFactory {
        private TestCompilationCache compilationCache;
        private final Path cacheDirPath;

        TestCompilationCacheFactory(Path cacheDirPath) {
            this.cacheDirPath = cacheDirPath;
        }

        @Override
        public CompilationCache createCompilationCache(Project project) {
            compilationCache = new TestCompilationCache(project, cacheDirPath);
            return compilationCache;
        }

        public TestCompilationCache compilationCache() {
            return compilationCache;
        }
    }

    /**
     * An instance of {@code CompilationCache} used for testing purposes.
     */
    private static class TestCompilationCache extends FileSystemCache {
        public int birCachedCount;
        public int jarCachedCount;

        public TestCompilationCache(Project project, Path cacheDirPath) {
            super(project, cacheDirPath.resolve(ProjectConstants.CACHES_DIR_NAME));
        }

        @Override
        public void cacheBir(ModuleName moduleName, ByteArrayOutputStream birContent) {
            super.cacheBir(moduleName, birContent);
            birCachedCount++;
        }

        @Override
        public void cachePlatformSpecificLibrary(CompilerBackend compilerBackend,
                                                 String libraryName,
                                                 ByteArrayOutputStream libraryContent) {
            super.cachePlatformSpecificLibrary(compilerBackend, libraryName, libraryContent);
            jarCachedCount++;
        }
    }
}

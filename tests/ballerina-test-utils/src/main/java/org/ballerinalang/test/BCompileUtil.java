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

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.NullBackend;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.repos.FileSystemCache;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static io.ballerina.projects.util.ProjectConstants.CACHES_DIR_NAME;
import static io.ballerina.projects.util.ProjectConstants.DIST_CACHE_DIRECTORY;

/**
 * Helper to drive test source compilation.
 *
 * @since 2.0.0
 */
public class BCompileUtil {

    private static final Path testSourcesDirectory = Paths.get("src/test/resources").toAbsolutePath().normalize();
    private static final Path testBuildDirectory = Paths.get("build").toAbsolutePath().normalize();

    private static final Logger logger = LoggerFactory.getLogger(BCompileUtil.class);

    public static Project loadProject(String sourceFilePath) {
        BuildOptions.BuildOptionsBuilder buildOptionsBuilder = BuildOptions.builder();
        return loadProject(sourceFilePath, buildOptionsBuilder.build());
    }

    public static Project loadProject(String sourceFilePath, BuildOptions buildOptions) {
        Path sourcePath = Paths.get(sourceFilePath);
        String sourceFileName = sourcePath.getFileName().toString();
        Path sourceRoot = testSourcesDirectory.resolve(sourcePath.getParent());

        Path projectPath = Paths.get(sourceRoot.toString(), sourceFileName);

        BuildOptions defaultOptions = BuildOptions.builder().setOffline(true).setDumpBirFile(true).build();
        BuildOptions mergedOptions = buildOptions.acceptTheirs(defaultOptions);
        return ProjectLoader.loadProject(projectPath, mergedOptions);
    }

    public static CompileResult compile(String sourceFilePath) {
        Project project = loadProject(sourceFilePath);

        Package currentPackage = project.currentPackage();
        JBallerinaBackend jBallerinaBackend = jBallerinaBackend(currentPackage);
        if (jBallerinaBackend.diagnosticResult().hasErrors()) {
            return new CompileResult(currentPackage, jBallerinaBackend);
        }

        CompileResult compileResult = new CompileResult(currentPackage, jBallerinaBackend);
        invokeModuleInit(compileResult);
        return compileResult;
    }

    public static CompileResult compileOffline(String sourceFilePath) {
        BuildOptions.BuildOptionsBuilder buildOptionsBuilder = BuildOptions.builder();
        BuildOptions buildOptions = buildOptionsBuilder.setOffline(Boolean.TRUE).build();
        Project project = loadProject(sourceFilePath, buildOptions);

        Package currentPackage = project.currentPackage();
        JBallerinaBackend jBallerinaBackend = jBallerinaBackend(currentPackage);
        if (jBallerinaBackend.diagnosticResult().hasErrors()) {
            return new CompileResult(currentPackage, jBallerinaBackend);
        }

        CompileResult compileResult = new CompileResult(currentPackage, jBallerinaBackend);
        invokeModuleInit(compileResult);
        return compileResult;
    }

    public static BIRCompileResult generateBIR(String sourceFilePath) {
        Project project = loadProject(sourceFilePath);
        NullBackend nullBackend = NullBackend.from(project.currentPackage().getCompilation());
        Package currentPackage = project.currentPackage();
        if (currentPackage.getCompilation().diagnosticResult().hasErrors() || nullBackend.hasErrors()) {
            return null;
        }

        BPackageSymbol bPackageSymbol = currentPackage.getCompilation().defaultModuleBLangPackage().symbol;
        if (bPackageSymbol == null) {
            return null;
        }

        CompiledBinaryFile.BIRPackageFile birPackageFile = bPackageSymbol.birPackageFile;
        if (birPackageFile == null) {
            return null;
        }

        return new BIRCompileResult(bPackageSymbol.bir, birPackageFile.pkgBirBinaryContent);
    }

    public static CompileResult compileWithoutInitInvocation(String sourceFilePath) {
        Project project = loadProject(sourceFilePath);

        Package currentPackage = project.currentPackage();
        JBallerinaBackend jBallerinaBackend = jBallerinaBackend(currentPackage);
        if (jBallerinaBackend.diagnosticResult().hasErrors()) {
            return new CompileResult(currentPackage, jBallerinaBackend);
        }

        return new CompileResult(currentPackage, jBallerinaBackend);
    }

    public static CompileResult compileAndCacheBala(String sourceFilePath) {
        return compileAndCacheBala(sourceFilePath, testBuildDirectory.resolve(DIST_CACHE_DIRECTORY));
    }

    public static CompileResult compileAndCacheBala(String sourceFilePath, Path repoPath) {
        Path sourcePath = Paths.get(sourceFilePath);
        String sourceFileName = sourcePath.getFileName().toString();
        Path sourceRoot = testSourcesDirectory.resolve(sourcePath.getParent());

        Path projectPath = Paths.get(sourceRoot.toString(), sourceFileName);
        BuildOptions defaultOptions = BuildOptions.builder().setOffline(true).setDumpBirFile(true).build();
        Project project = ProjectLoader.loadProject(projectPath, getTestProjectEnvironmentBuilder(), defaultOptions);

        if (isSingleFileProject(project)) {
            throw new RuntimeException("single file project is given for compilation at " + project.sourceRoot());
        }

        Package currentPackage = project.currentPackage();
        JBallerinaBackend jBallerinaBackend = jBallerinaBackend(currentPackage);
        if (jBallerinaBackend.diagnosticResult().hasErrors()) {
            return new CompileResult(currentPackage, jBallerinaBackend);
        }

        Path balaCachePath = balaCachePath(currentPackage.packageOrg().toString(),
                currentPackage.packageName().toString(), currentPackage.packageVersion().toString(), repoPath);
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALA, balaCachePath);
        Path balaFilePath;
        try {
            balaFilePath = Files.list(balaCachePath).filter(path ->
                    path.toString().endsWith(ProjectConstants.BLANG_COMPILED_PKG_BINARY_EXT)).findAny().orElseThrow();
            String platform = getPlatformFromBala(balaFilePath.getFileName().toString(),
                    currentPackage.packageName().toString(), currentPackage.packageVersion().toString());
            if (Files.isDirectory(balaCachePath.resolve(platform))) {
                ProjectUtils.deleteDirectory(balaCachePath.resolve(platform));
            }
            ProjectUtils.extractBala(balaFilePath, balaCachePath.resolve(platform));
            Files.delete(balaFilePath);
        } catch (IOException e) {
            throw new RuntimeException("bala extraction failed " + project.sourceRoot() + ". ", e);
        }

        CompileResult compileResult = new CompileResult(currentPackage, jBallerinaBackend);
        invokeModuleInit(compileResult);
        return compileResult;
    }

    private static JBallerinaBackend jBallerinaBackend(Package currentPackage) {
        PackageCompilation packageCompilation = currentPackage.getCompilation();
        if (packageCompilation.diagnosticResult().errorCount() > 0) {
            logger.error("compilation failed with errors: " + currentPackage.project().sourceRoot());
        }
        return JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_11);
    }

    /**
     * Copy the given bala to the distribution repository.
     *
     * @param srcPath Path of the source bala.
     * @param org     organization name
     * @param pkgName package name
     * @param version Bala version
     * @throws IOException is thrown if the file copy failed
     */
    public static void copyBalaToDistRepository(Path srcPath,
                                                String org,
                                                String pkgName,
                                                String version) throws IOException {
        Path targetPath = balaCachePath(org, pkgName, version, testBuildDirectory.resolve(DIST_CACHE_DIRECTORY))
                .resolve("any");
        if (Files.isDirectory(targetPath)) {
            ProjectUtils.deleteDirectory(targetPath);
        }
        ProjectUtils.extractBala(srcPath, targetPath);
    }

    public static ProjectEnvironmentBuilder getTestProjectEnvironmentBuilder() {
        ProjectEnvironmentBuilder environmentBuilder = ProjectEnvironmentBuilder.getBuilder(
                EnvironmentBuilder.buildDefault());
        return environmentBuilder.addCompilationCacheFactory(TestCompilationCache::from);
    }

    private static void invokeModuleInit(CompileResult compileResult) {
        if (compileResult.getErrorCount() != 0) {
            return;
        }

        try {
            BRunUtil.runInit(compileResult);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("error while invoking init method of " + compileResult.projectSourceRoot(), e);
        }
    }

    private static boolean isSingleFileProject(Project project) {
        return project instanceof SingleFileProject;
    }

    private static Path balaCachePath(String org,
                                      String pkgName,
                                      String version,
                                      Path repoPath) {
        try {
            Path balaDirPath = repoPath.resolve("bala")
                    .resolve(org)
                    .resolve(pkgName)
                    .resolve(version);
            Files.createDirectories(balaDirPath);
            return balaDirPath;
        } catch (IOException e) {
            throw new RuntimeException("error while creating the bala distribution cache directory at " +
                    testBuildDirectory, e);
        }
    }

    /**
     * Compilation cache that dumps bir and jars inside the build directory.
     */
    private static class TestCompilationCache extends FileSystemCache {

        private TestCompilationCache(Project project, Path cacheDirPath) {
            super(project, cacheDirPath.resolve(CACHES_DIR_NAME));
        }

        private static TestCompilationCache from(Project project) {
            Path testCompilationCachePath = testBuildDirectory.resolve(DIST_CACHE_DIRECTORY);
            return new TestCompilationCache(project, testCompilationCachePath);
        }
    }

    /**
     * Class to hold both expected and actual compile result of BIR.
     */
    public static class BIRCompileResult {

        private BIRNode.BIRPackage expectedBIR;
        private byte[] actualBIRBinary;

        BIRCompileResult(BIRNode.BIRPackage expectedBIR, byte[] actualBIRBinary) {
            this.expectedBIR = expectedBIR;
            this.actualBIRBinary = actualBIRBinary;
        }

        public BIRNode.BIRPackage getExpectedBIR() {
            return expectedBIR;
        }

        public byte[] getActualBIR() {
            return actualBIRBinary;
        }
    }

    public static String getPlatformFromBala(String balaName, String packageName, String version) {
        return balaName.split(packageName + "-")[1].split("-" + version)[0];
    }
}

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

package org.ballerinalang.stdlib.utils;

import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.BuildOptionsBuilder;
import io.ballerina.projects.CompilationCache;
import io.ballerina.projects.CompilerBackend;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.repos.FileSystemCache;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static io.ballerina.projects.util.ProjectConstants.BLANG_COMPILED_JAR_EXT;
import static io.ballerina.projects.util.ProjectUtils.getThinJarFileName;

/**
 * Class providing utility methods to generate bala from package.
 *
 * @since 2.0.0
 */
public class BuildLangLib {

    static Path projectDir;
    static Path distCache;
    static boolean skipBootstrap = false;


    public static void main(String[] args) throws IOException {
        PrintStream out = System.out;
        try {
            projectDir = Paths.get(args[0]);
            distCache = Paths.get(args[1]);
            String pkgName = args[2];
            // Following is to compile stdlib Modules
            if (args.length >= 4 && args[3].equals("true")) {
                skipBootstrap = true;
            }
            System.setProperty(ProjectConstants.BALLERINA_HOME, distCache.toString());
            System.setProperty("LANG_REPO_BUILD", "true");
            out.println("Building langlib ...");
            out.println("Project Dir: " + projectDir);

            if (!skipBootstrap) {
                System.setProperty("BOOTSTRAP_LANG_LIB", pkgName);
            }

            Path targetPath = projectDir.resolve(ProjectConstants.TARGET_DIR_NAME);
            clearTarget(targetPath);
            Path pkgTargetPath = targetPath.resolve(pkgName);
            ProjectEnvironmentBuilder environmentBuilder = createProjectEnvBuilder(pkgTargetPath);

            BuildOptions defaultOptions = new BuildOptionsBuilder().offline(true).dumpBirFile(true).build();
            Project project = BuildProject.load(environmentBuilder, projectDir, defaultOptions);
            Package pkg = project.currentPackage();
            PackageCompilation packageCompilation = pkg.getCompilation();
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_11);
            if (jBallerinaBackend.diagnosticResult().hasErrors()) {
                out.println("Error building Ballerina package: " + pkg.packageName());
                jBallerinaBackend.diagnosticResult().diagnostics().forEach(d -> out.println(d.toString()));
                System.exit(1);
            }

            PackageManifest pkgDesc = pkg.manifest();
            Path balaDirPath = pkgTargetPath.resolve("bala");

            // Create bala cache directory
            Path balaPath = balaDirPath.resolve(pkgDesc.org().toString())
                    .resolve(pkgDesc.name().value())
                    .resolve(pkgDesc.version().toString())
                    .resolve("any");
            Files.createDirectories(balaPath);
            jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALA, balaPath);

            Path balaFilePath = Files.list(balaPath).findAny().orElseThrow();
            ProjectUtils.extractBala(balaFilePath, balaPath);
            Files.delete(balaFilePath);


            // Create zip file
            Path zipFilePath = targetPath.resolve(pkgDesc.name().value() + ".zip");
            try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(zipFilePath))) {
                Files.walk(pkgTargetPath)
                        .filter(path -> !Files.isDirectory(path))
                        .forEach(path -> {
                            ZipEntry zipEntry = new ZipEntry(pkgTargetPath.relativize(path).toString());
                            try {
                                zs.putNextEntry(zipEntry);
                                Files.copy(path, zs);
                                zs.closeEntry();
                            } catch (IOException e) {
                                PrintStream err = System.err;
                                err.println(e.getMessage());
                            }
                        });
            }

            // Copy generated jar to the target dir
            Path cacheDirPath = pkgTargetPath.resolve("cache");
            String jarFileName = getThinJarFileName(pkgDesc.org(), pkgDesc.name().value(), pkgDesc.version())
                    + BLANG_COMPILED_JAR_EXT;
            Path generatedJarFilePath = cacheDirPath.resolve(pkgDesc.org().toString())
                    .resolve(pkgDesc.name().value())
                    .resolve(pkgDesc.version().toString())
                    .resolve(jBallerinaBackend.targetPlatform().code())
                    .resolve(jarFileName);
            Path targetJarFilePath = targetPath.resolve(jarFileName);
            Files.copy(generatedJarFilePath, targetJarFilePath);

            //Generate docs
            out.println("Generating docs...");
            BallerinaDocGenerator.generateAPIDocs(project, targetPath.resolve(ProjectConstants.TARGET_API_DOC_DIRECTORY)
                    .toString(), true);

        } catch (Exception e) {
            out.println("Unknown error building : " + projectDir.toString());
            e.printStackTrace();
            throw e;
        }
    }

    private static void clearTarget(Path targetPath) throws IOException {
        if (Files.exists(targetPath)) {
            deleteDirectory(targetPath);
        }
    }

    private static ProjectEnvironmentBuilder createProjectEnvBuilder(Path targetPath) {
        ProjectEnvironmentBuilder environmentBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
        environmentBuilder.addCompilationCacheFactory(project -> new CompilationCache(project) {
            private final FileSystemCache fsCache =
                    new FileSystemCache(project, targetPath.resolve(ProjectConstants.CACHES_DIR_NAME));

            @Override
            public byte[] getBir(ModuleName moduleName) {
                return new byte[0];
            }

            @Override
            public void cacheBir(ModuleName moduleName, ByteArrayOutputStream birContent) {
                fsCache.cacheBir(moduleName, birContent);
            }

            @Override
            public Optional<Path> getPlatformSpecificLibrary(CompilerBackend compilerBackend,
                                                             String libraryName) {
                return Optional.empty();
            }

            @Override
            public void cachePlatformSpecificLibrary(CompilerBackend compilerBackend,
                                                     String libraryName,
                                                     ByteArrayOutputStream libraryContent) {
                fsCache.cachePlatformSpecificLibrary(compilerBackend, libraryName, libraryContent);
            }
        });

        return environmentBuilder;
    }

    /**
     * Iterate and delete all files and subdirectories of a given path.
     *
     * @param directory Directory path.
     * @throws IOException Exception when walking the file tree.
     */
    public static void deleteDirectory(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
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

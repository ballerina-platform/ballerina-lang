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

import io.ballerina.projects.CompilationCache;
import io.ballerina.projects.CompilerBackend;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JdkVersion;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.repos.FileSystemCache;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Class providing utility methods to generate balo from package.
 *
 * @since 2.0.0
 */
public class BuildLangLib {

    static Path projectDir;
    static Path distCache;
    static boolean skipBootstrap = false;

    public static void main(String[] args) throws IOException {
        PrintStream out = System.out;
        projectDir = Paths.get(args[0]);
        distCache = Paths.get(args[1]);
        // Following is to compile stdlib Modules
        if (args.length >= 3 && args[2].equals("true")) {
            skipBootstrap = true;
        }
        System.setProperty(ProjectConstants.BALLERINA_HOME, distCache.toString());
        out.println("Building langlib ...");
        out.println("Project Dir: " + projectDir);

        // TODO Temporary fix
        String pkgName = Optional.ofNullable(projectDir.getFileName()).orElse(Paths.get("annon")).toString();
        if (!skipBootstrap) {
            System.setProperty("BOOTSTRAP_LANG_LIB", pkgName);
        }

        Path targetPath = projectDir.resolve(ProjectConstants.TARGET_DIR_NAME);
        Path pkgTargetPath = targetPath.resolve(pkgName);
        ProjectEnvironmentBuilder environmentBuilder = createProjectEnvBuilder(pkgTargetPath);

        Project project = BuildProject.load(environmentBuilder, projectDir);
        Package pkg = project.currentPackage();
        PackageCompilation packageCompilation = pkg.getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JdkVersion.JAVA_11);
        if (jBallerinaBackend.diagnosticResult().hasErrors()) {
            out.println("Error building Ballerina package: " + pkg.packageName());
            jBallerinaBackend.diagnosticResult().diagnostics().forEach(d -> out.println(d.toString()));
            System.exit(1);
        }

        PackageDescriptor pkgDesc = pkg.packageDescriptor();
        String baloName = ProjectUtils.getBaloName(pkgDesc);
        Path baloDirPath = pkgTargetPath.resolve("balo");

        // Create balo cache directory
        Path balrPath = baloDirPath.resolve(pkgDesc.org().toString())
                .resolve(pkgDesc.name().value())
                .resolve(pkgDesc.version().toString());
        Files.createDirectories(balrPath);
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALO, balrPath.resolve(baloName));

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
        String jarFileName = pkgDesc.name().value() + ".jar";
        Path generatedJarFilePath = cacheDirPath.resolve(pkgDesc.org().toString())
                .resolve(pkgDesc.name().value())
                .resolve(pkgDesc.version().toString())
                .resolve(jBallerinaBackend.targetPlatform().code())
                .resolve(jarFileName);
        Path targetJarFilePath = targetPath.resolve(jarFileName);
        Files.copy(generatedJarFilePath, targetJarFilePath);
    }

    private static ProjectEnvironmentBuilder createProjectEnvBuilder(Path targetPath) {
        ProjectEnvironmentBuilder environmentBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
        environmentBuilder.addCompilationCacheFactory(project -> new CompilationCache(project) {
            private final FileSystemCache fsCache = new FileSystemCache(project, targetPath);

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
}

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

import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JdkVersion;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.model.Target;
import io.ballerina.projects.utils.ProjectConstants;
import io.ballerina.projects.utils.ProjectUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
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
        System.setProperty(ProjectConstants.BALLERINA_INSTALL_DIR_PROP, distCache.toString());
        out.println("Building langlib ...");
        out.println("Project Dir: " + projectDir);

        // TODO Temporary fix
        String pkgName = projectDir.getFileName().toString();
        if (!skipBootstrap) {
            System.setProperty("BOOTSTRAP_LANG_LIB", pkgName);
        }
        Project project = BuildProject.loadProject(projectDir);
        Target target = new Target(projectDir);
        Package pkg = project.currentPackage();
        PackageCompilation packageCompilation = pkg.getCompilation();
        if (packageCompilation.diagnostics().size() > 0) {
            out.println("Error building module");
            packageCompilation.diagnostics().forEach(d -> out.println(d.toString()));
            System.exit(1);
        }
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JdkVersion.JAVA_11);
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.JAR, target.path());
        LangLibArchive langLibArchive = new LangLibArchive(target.path(), pkg);
    }
}


class LangLibArchive {
    Path archive;
    Path balos;
    Path cache;
    Path bir;
    Path jar;
    Package aPackage;
    Path zipFile;

    LangLibArchive(Path target, Package pkg) throws IOException {
        archive = target.resolve(pkg.getDefaultModule().moduleName().toString());
        Files.createDirectories(archive);
        aPackage = pkg;
        zipFile = target.resolve(pkg.packageName().value()+".zip");
        createDirectoryStructure();
        createArtafacts();
        createZip();
        deleteFiles();
    }

    private void deleteFiles() throws IOException {
        Files.walk(archive)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    private void createZip() throws IOException {
        try (ZipOutputStream zs = new ZipOutputStream(Files.newOutputStream(zipFile))) {
            Files.walk(archive)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(archive.relativize(path).toString());
                        try {
                            zs.putNextEntry(zipEntry);
                            Files.copy(path, zs);
                            zs.closeEntry();
                        } catch (IOException e) {
                            System.err.println(e);
                        }
                    });
        }
    }

    private void createArtafacts() {
        String baloName = ProjectUtils.getBaloName(aPackage);
        PackageCompilation packageCompilation = aPackage.getCompilation();
        JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JdkVersion.JAVA_11);
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.BALO, balos.resolve(baloName));
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.BIR, bir);
        jBallerinaBackend.emit(JBallerinaBackend.OutputType.JAR, jar);
    }

    void createDirectoryStructure() throws IOException {
        /* The exported archive will contain the following
         * lang.annotations.zip
         * - balo
         *    - org
         *      - package-name
         *         - version
         *            - org-package-name-version-any.balo
         * - cache
         *    - org
         *       - package-name
         *          - version
         *            - bir
         *               - mod1.bir
         *               - mod2.bir
         *            - jar
         *               - org-package-name-version.jar
         */
        this.balos = archive.resolve("balo")
                .resolve(aPackage.packageOrg().toString())
                .resolve(aPackage.packageName().value())
                .resolve(aPackage.packageVersion().version().toString());
        this.cache = archive.resolve("cache")
                .resolve(aPackage.packageOrg().toString())
                .resolve(aPackage.packageName().value())
                .resolve(aPackage.packageVersion().version().toString());
        this.bir = this.cache.resolve("bir");
        this.jar = this.cache.resolve("jar");
        Files.createDirectories(this.cache);
        Files.createDirectories(this.balos);
        Files.createDirectories(this.bir);
        Files.createDirectories(this.jar);
    }
}

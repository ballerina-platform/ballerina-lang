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
package org.ballerinalang.bindgen.utils;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.ballerinalang.bindgen.exceptions.BindgenException;
import org.ballerinalang.maven.Dependency;
import org.ballerinalang.maven.MavenResolver;
import org.ballerinalang.maven.exceptions.MavenResolverException;
import org.ballerinalang.toml.model.Library;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.model.Platform;
import org.wso2.ballerinalang.util.TomlParserUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import static org.ballerinalang.bindgen.command.BindingsGenerator.getOutputPath;
import static org.ballerinalang.bindgen.command.BindingsGenerator.setClassPaths;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_TOML;
import static org.ballerinalang.bindgen.utils.BindgenConstants.FILE_SEPARATOR;
import static org.ballerinalang.bindgen.utils.BindgenConstants.MVN_REPO;
import static org.ballerinalang.bindgen.utils.BindgenConstants.TARGET_DIR;
import static org.ballerinalang.bindgen.utils.BindgenConstants.USER_DIR;
import static org.wso2.ballerinalang.compiler.util.ProjectDirs.isModuleExist;

/**
 * This class contains the util methods related to the maven support of Ballerina Bindgen CLI tool.
 *
 * @since 1.2.5
 */
public class BindgenMvnResolver {

    private PrintStream outStream;

    public BindgenMvnResolver(PrintStream outStream) {
        this.outStream = outStream;
    }

    public void mavenResolver(String groupId, String artifactId, String version, Path projectRoot,
                              boolean resolve) throws BindgenException {
        Path mvnRepository;
        if (projectRoot == null) {
            if (getOutputPath() != null) {
                mvnRepository = Paths.get(getOutputPath(), TARGET_DIR, MVN_REPO);
            } else {
                mvnRepository = Paths.get(System.getProperty(USER_DIR), TARGET_DIR, MVN_REPO);
            }
        } else {
            mvnRepository = Paths.get(projectRoot.toString(), TARGET_DIR, MVN_REPO);
        }
        handleDependency(groupId, artifactId, version, mvnRepository.toString(), projectRoot, null);
        if (resolve) {
            outStream.println("\nResolving maven dependencies...");
            Dependency dependency = resolveDependency(groupId, artifactId, version, mvnRepository.toString());
            dependencyTraversal(dependency, mvnRepository.toString(), projectRoot);
            if (projectRoot != null) {
                outStream.println("\nUpdated the Ballerina.toml file with new platform libraries.");
            }
        }
    }

    private static void dependencyTraversal(Dependency dependency, String mvnRepository, Path projectRoot)
            throws BindgenException {
        if (dependency.getDepedencies() == null) {
            return;
        }
        String parent = dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getVersion();
        for (Dependency transitive : dependency.getDepedencies()) {
            handleDependency(transitive.getGroupId(), transitive.getArtifactId(), transitive.getVersion(),
                    mvnRepository, projectRoot, parent);
            dependencyTraversal(transitive, mvnRepository, projectRoot);
        }
    }

    private static Dependency resolveDependency(String groupId, String artifactId, String version,
                                                String mvnRepository) throws BindgenException {
        MavenResolver resolver = new MavenResolver(mvnRepository);
        try {
            return resolver.resolve(groupId, artifactId, version, true);
        } catch (MavenResolverException e) {
            throw new BindgenException(e.getMessage());
        }
    }

    private static void handleDependency(String groupId, String artifactId, String version, String mvnRepository,
                                         Path projectRoot, String parent) throws BindgenException {
        Path mvnPath = Paths.get(mvnRepository, getPathFromGroupId(groupId), artifactId, version);
        setClassPaths(mvnPath.toString());
        if (projectRoot != null) {
            File tomlFile = new File(Paths.get(projectRoot.toString(), BALLERINA_TOML).toString());
            if (tomlFile.exists() && !tomlFile.isDirectory()) {
                populateBallerinaToml(groupId, artifactId, version, tomlFile, projectRoot, parent);
            }
        }
    }

    private static void populateBallerinaToml(String groupId, String artifactId, String version, File tomlFile,
                                              Path projectRoot, String parent) throws BindgenException {
        Manifest manifest = TomlParserUtils.getManifest(projectRoot);
        try (FileWriterWithEncoding fileWriter = new FileWriterWithEncoding(tomlFile, StandardCharsets.UTF_8, true)) {
            Platform platform = manifest.getPlatform();
            if (platform == null || (platform.target == null && platform.libraries == null)) {
                fileWriter.write("\n\n[platform]\n");
                fileWriter.write("target = \"java11\"\n");
            } else if (platform.getLibraries() != null) {
                for (Library library : platform.getLibraries()) {
                    if (library.path == null && library.groupId != null && library.artifactId != null &&
                            library.version != null && library.groupId.equals(groupId) &&
                            library.artifactId.equals(artifactId) && library.version.equals(version)) {
                        return;
                    }
                }
            }
            fileWriter.write("\n");
            if (parent != null) {
                fileWriter.write("    # transitive dependency of " + parent + "\n");
            }
            fileWriter.write("    [[platform.libraries]]\n");
            String moduleName = getModuleName(projectRoot, getOutputPath());
            if (moduleName != null) {
                fileWriter.write("    modules = [\"" + moduleName + "\"]\n");
            }
            fileWriter.write("    groupId = \"" + groupId + "\"\n");
            fileWriter.write("    artifactId = \"" + artifactId + "\"\n");
            fileWriter.write("    version = \"" + version + "\"\n");
        } catch (IOException io) {
            throw new BindgenException("Error while updating the Ballerina.toml file.", io);
        }
    }

    private static String getPathFromGroupId(String groupId) {
        String[] paths = groupId.split("\\.");
        if (paths.length == 0) {
            return groupId;
        }
        File combined = new File(paths[0]);
        for (int i = 1; i < paths.length; i++) {
            combined = new File(combined, paths[i]);
        }
        return combined.getPath();
    }

    private static String getModuleName(Path projectRoot, String outputPath) {
        if (outputPath == null) {
            outputPath = Paths.get(System.getProperty(USER_DIR)).toString();
        }
        String splitRegex = Pattern.quote(System.getProperty(FILE_SEPARATOR));
        String[] splittedPath = outputPath.split(splitRegex);
        String moduleName = splittedPath[splittedPath.length - 1];
        if (isModuleExist(projectRoot, moduleName)) {
            return moduleName;
        }
        return null;
    }
}

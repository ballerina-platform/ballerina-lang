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

package io.ballerina.projects;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.moandjiezana.toml.Toml;
import io.ballerina.projects.internal.model.CompilerPluginToml;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipOutputStream;

import static io.ballerina.projects.util.ProjectConstants.COMPILER_PLUGIN_TOML;

/**
 * This class knows how to create a bala containing jballerina platform libs.
 *
 * @since 2.0.0
 */
public class JBallerinaBalaWriter extends BalaWriter {

    private JBallerinaBackend backend;

    public JBallerinaBalaWriter(JBallerinaBackend backend) {
        this.backend = backend;
        this.packageContext = backend.packageContext();
        this.target = getTargetPlatform(packageContext.getResolution()).code();
        this.compilerPluginToml = readCompilerPluginToml();
    }


    @Override
    protected Optional<JsonArray> addPlatformLibs(ZipOutputStream balaOutputStream)
            throws IOException {
        // retrieve platform dependencies that have default scope
        Collection<PlatformLibrary> jars = backend.platformLibraryDependencies(packageContext.packageId(),
                PlatformLibraryScope.DEFAULT);
        if (jars.isEmpty()) {
            return Optional.empty();
        }
        // Iterate through native dependencies and add them to bala
        // organization would be
        // -- Bala Root
        //   - libs
        //     - platform
        //       - java11
        //         - java-library1.jar
        //         - java-library2.jar
        JsonArray newPlatformLibs = new JsonArray();
        // Iterate jars and create directories for each target
        for (PlatformLibrary platformLibrary : jars) {
            JarLibrary jar = (JarLibrary) platformLibrary;
            Path libPath = jar.path();
            // null check is added for spot bug with the toml validation filename cannot be null
            String fileName = Optional.ofNullable(libPath.getFileName())
                    .map(p -> p.toString()).orElse("annon");
            Path entryPath = Paths.get(PLATFORM)
                    .resolve(target)
                    .resolve(fileName);
            // create a zip entry for each file
            putZipEntry(balaOutputStream, entryPath, new FileInputStream(libPath.toString()));

            // Create the Package.json entry
            JsonObject newDependency = new JsonObject();
            newDependency.addProperty(JarLibrary.KEY_PATH, entryPath.toString());
            if (jar.artifactId().isPresent() && jar.groupId().isPresent() && jar.version().isPresent()) {
                newDependency.addProperty(JarLibrary.KEY_ARTIFACT_ID, jar.artifactId().get());
                newDependency.addProperty(JarLibrary.KEY_GROUP_ID, jar.groupId().get());
                newDependency.addProperty(JarLibrary.KEY_VERSION, jar.version().get());
            }
            newPlatformLibs.add(newDependency);
        }

        return Optional.of(newPlatformLibs);
    }

    @Override
    protected void addCompilerPluginLibs(ZipOutputStream balaOutputStream)
            throws IOException {
        if (this.compilerPluginToml.isPresent()) {
            List<String> compilerPluginDependencies = this.compilerPluginToml.get().getCompilerPluginDependencies();

            if (!compilerPluginDependencies.isEmpty()) {

                // Iterate through compiler plugin dependencies and add them to bala
                // organization would be
                // -- Bala Root
                //   - compiler-plugin/
                //     - libs
                //       - java-library1.jar
                //       - java-library2.jar

                // Iterate jars and create directories for each target
                for (String compilerPluginLib : compilerPluginDependencies) {
                    Path libPath = this.packageContext.project().sourceRoot().resolve(compilerPluginLib);
                    // null check is added for spot bug with the toml validation filename cannot be null
                    String fileName = Optional.ofNullable(libPath.getFileName())
                            .map(p -> p.toString()).orElse("annon");
                    Path entryPath = Paths.get("compiler-plugin").resolve("libs").resolve(fileName);
                    // create a zip entry for each file
                    putZipEntry(balaOutputStream, entryPath, new FileInputStream(libPath.toString()));
                }
            }
        }
    }

    private CompilerBackend.TargetPlatform getTargetPlatform(PackageResolution pkgResolution) {
        ResolvedPackageDependency resolvedPackageDependency = new ResolvedPackageDependency(
                this.packageContext.project().currentPackage(), PackageDependencyScope.DEFAULT);
        Collection<ResolvedPackageDependency> resolvedPackageDependencies = pkgResolution.dependencyGraph()
                .getDirectDependencies(resolvedPackageDependency);
        for (ResolvedPackageDependency dependency : resolvedPackageDependencies) {
            if (dependency.packageInstance().packageOrg().value().equals("ballerina")
                    && dependency.packageInstance().packageName().value().equals("jballerina.java")) {
                return this.backend.targetPlatform();
            }
        }
        return AnyTarget.ANY;
    }

    private Optional<CompilerPluginToml> readCompilerPluginToml() {
        Path compilerPluginTomlPath = backend.packageContext().project().sourceRoot().resolve(COMPILER_PLUGIN_TOML);
        if (compilerPluginTomlPath.toFile().exists()) {
            Toml toml;
            try (InputStream inputStream = new FileInputStream(compilerPluginTomlPath.toFile())) {
                toml = new Toml().read(inputStream);
            } catch (IllegalStateException e) {
                throw new ProjectException("invalid '" + COMPILER_PLUGIN_TOML + "' due to " + e.getMessage());
            } catch (IOException e) {
                throw new ProjectException("cannot read '" + COMPILER_PLUGIN_TOML + "' due to " + e.getMessage());
            }
            return Optional.ofNullable(toml.to(CompilerPluginToml.class));
        }
        return Optional.empty();
    }
}

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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.ballerina.projects.internal.bala.BalToolJson;
import io.ballerina.projects.internal.bala.CompilerPluginJson;
import io.ballerina.projects.internal.bala.adaptors.JsonCollectionsAdaptor;
import io.ballerina.projects.internal.bala.adaptors.JsonStringsAdaptor;
import io.ballerina.projects.internal.model.BalToolDescriptor;
import io.ballerina.projects.internal.model.CompilerPluginDescriptor;
import org.wso2.ballerinalang.compiler.util.Names;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipOutputStream;

import static io.ballerina.projects.util.ProjectConstants.BAL_TOOL_JSON;
import static io.ballerina.projects.util.ProjectConstants.COMPILER_PLUGIN_JSON;

/**
 * This class knows how to create a bala containing jballerina platform libs.
 *
 * @since 2.0.0
 */
public class JBallerinaBalaWriter extends BalaWriter {

    public static final String TOOL = "tool";
    public static final String LIBS = "libs";
    public static final String COMPILER_PLUGIN = "compiler-plugin";
    public static final String ANNON = "annon";
    private JBallerinaBackend backend;

    public JBallerinaBalaWriter(JBallerinaBackend backend) {
        this.backend = backend;
        this.packageContext = backend.packageContext();
        this.compilerPluginToml = readCompilerPluginToml();
        this.balToolToml = readBalToolToml();
        this.target = getTargetPlatform(packageContext.getResolution()).code();
    }


    @Override
    protected Optional<JsonArray> addPlatformLibs(ZipOutputStream balaOutputStream)
            throws IOException {
        // retrieve platform dependencies
        Collection<PlatformLibrary> jars = backend.platformLibraryDependencies(packageContext.packageId(),
                PlatformLibraryScope.DEFAULT);
        Collection<PlatformLibrary> providedJars = backend.platformLibraryDependencies(packageContext.packageId(),
                PlatformLibraryScope.PROVIDED);
        if (jars.isEmpty() && providedJars.isEmpty()) {
            return Optional.empty();
        }
        // Iterate through native dependencies with default scope and add them to bala
        // Native dependencies with provided scope are not added to the bala
        // organization would be
        // -- Bala Root
        //   - libs
        //     - platform
        //       - java17
        //         - java-library1.jar
        //         - java-library2.jar
        JsonArray newPlatformLibs = new JsonArray();
        // Iterate jars and create directories for each target
        for (PlatformLibrary platformLibrary : jars) {
            JarLibrary jar = (JarLibrary) platformLibrary;
            Path libPath = jar.path();
            // null check is added for spot bug with the toml validation filename cannot be null
            String fileName = Optional.ofNullable(libPath.getFileName())
                    .map(Path::toString).orElse(ANNON);
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

        // include platform dependencies with provided scope in the Package.json
        for (PlatformLibrary platformLibrary : providedJars) {
            JarLibrary jar = (JarLibrary) platformLibrary;
            JsonObject newDependency = new JsonObject();

            // Create the Package.json entry
            if (jar.artifactId().isPresent() && jar.groupId().isPresent() && jar.version().isPresent()) {
                newDependency.addProperty(JarLibrary.KEY_ARTIFACT_ID, jar.artifactId().get());
                newDependency.addProperty(JarLibrary.KEY_GROUP_ID, jar.groupId().get());
                newDependency.addProperty(JarLibrary.KEY_VERSION, jar.version().get());
            }
            newDependency.addProperty(JarLibrary.KEY_SCOPE, jar.scope().getStringValue());
            newPlatformLibs.add(newDependency);
        }

        return Optional.of(newPlatformLibs);
    }

    @Override
    protected void addCompilerPlugin(ZipOutputStream balaOutputStream) throws IOException {
        if (this.compilerPluginToml.isPresent()) {
            List<String> compilerPluginLibPaths = new ArrayList<>();
            List<String> compilerPluginDependencies = this.compilerPluginToml.get().getCompilerPluginDependencies();

            if (compilerPluginDependencies.get(0) == null) {
                throw new ProjectException("No dependencies found in CompilerPlugin.toml file");
            }

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
                            .map(Path::toString).orElse(ANNON);
                    Path entryPath = Paths.get("compiler-plugin").resolve("libs").resolve(fileName);
                    // create a zip entry for each file
                    putZipEntry(balaOutputStream, entryPath, new FileInputStream(libPath.toString()));
                    compilerPluginLibPaths.add(entryPath.toString());
                }
            }

            CompilerPluginJson compilerPluginJson = new CompilerPluginJson(
                    this.compilerPluginToml.get().plugin().getId(),
                    this.compilerPluginToml.get().plugin().getClassName(),
                    compilerPluginLibPaths);

            // Remove fields with empty values from `compiler-plugin.json`
            Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(Collection.class, new JsonCollectionsAdaptor())
                    .registerTypeHierarchyAdapter(String.class, new JsonStringsAdaptor()).setPrettyPrinting().create();

            try {
                putZipEntry(balaOutputStream, Paths.get(COMPILER_PLUGIN, COMPILER_PLUGIN_JSON),
                        new ByteArrayInputStream(
                                gson.toJson(compilerPluginJson).getBytes(Charset.defaultCharset())));
            } catch (IOException e) {
                throw new ProjectException("Failed to write '" + COMPILER_PLUGIN_JSON + "' file: " + e.getMessage(), e);
            }
        }
    }

    @Override
    protected void addBalTool(ZipOutputStream balaOutputStream) throws IOException {
        if (this.balToolToml.isPresent()) {
            List<String> balToolLibPaths = new ArrayList<>();
            List<String> balToolDependencies = this.balToolToml.get().getBalToolDependencies();

            if (balToolDependencies.isEmpty()) {
                throw new ProjectException("No dependencies found in BalTool.toml file");
            }
         // Iterate through bal tool dependencies and add them to bala
            // organization would be
            // -- Bala Root
            //   - tool/
            //     - libs
            //       - java-library1.jar
            //       - java-library2.jar


            // Iterate jars and create directories for each target
            for (String balToolLib : balToolDependencies) {
                Path libPath = this.packageContext.project().sourceRoot().resolve(balToolLib);
                // null check is added for spot bug with the toml validation filename cannot be null
                String fileName = Optional.ofNullable(libPath.getFileName())
                        .map(Path::toString).orElse(ANNON);
                Path entryPath = Paths.get(TOOL).resolve(LIBS).resolve(fileName);
                // create a zip entry for each file
                putZipEntry(balaOutputStream, entryPath, new FileInputStream(libPath.toString()));
                balToolLibPaths.add(entryPath.toString());
            }

        BalToolJson balToolJson = new BalToolJson(this.balToolToml.get().tool().getId(), balToolLibPaths);

        // Remove fields with empty values from `BalTool.json`
        Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(Collection.class, new JsonCollectionsAdaptor())
                .registerTypeHierarchyAdapter(String.class, new JsonStringsAdaptor()).setPrettyPrinting().create();

        try {
            putZipEntry(balaOutputStream, Paths.get(TOOL, BAL_TOOL_JSON),
                    new ByteArrayInputStream(
                            gson.toJson(balToolJson).getBytes(Charset.defaultCharset())));
        } catch (IOException e) {
            throw new ProjectException("Failed to write '" + BAL_TOOL_JSON + "' file: " + e.getMessage(), e);
        }
        }
    }

    /**
     * Mark target platform as `java17` if one of the following condition fulfils.
     * 1) Direct dependencies of imports in the package have any `ballerina/java` dependency.
     * 2) Package has defined any platform dependency.
     *
     * @param pkgResolution package resolution
     * @return target platform
     */
    private CompilerBackend.TargetPlatform getTargetPlatform(PackageResolution pkgResolution) {
        ResolvedPackageDependency resolvedPackageDependency = new ResolvedPackageDependency(
                this.packageContext.project().currentPackage(), PackageDependencyScope.DEFAULT);
        Collection<ResolvedPackageDependency> resolvedPackageDependencies = pkgResolution.dependencyGraph()
                .getDirectDependencies(resolvedPackageDependency);

        // 1) Check direct dependencies of imports in the package have any `ballerina/java` dependency
        for (ResolvedPackageDependency dependency : resolvedPackageDependencies) {
            if (dependency.packageInstance().packageOrg().value().equals(Names.BALLERINA_ORG.value) &&
                    dependency.packageInstance().packageName().value().equals(Names.JAVA.value) &&
                    !dependency.scope().equals(PackageDependencyScope.TEST_ONLY)) {
                return this.backend.targetPlatform();
            }
        }

        // 2) Check package has defined any platform dependency
        PackageManifest manifest = this.packageContext.project().currentPackage().manifest();
        if (hasPlatformDependencies(manifest.platforms())) {
            return this.backend.targetPlatform();
        }

        // 3) Check if the package has a BalTool.toml or a CompilerPlugin.toml
        if (this.balToolToml.isPresent() || this.compilerPluginToml.isPresent()) {
            return this.backend.targetPlatform();
        }
        return AnyTarget.ANY;
    }

    private boolean hasPlatformDependencies(Map<String, PackageManifest.Platform> platforms) {
        for (PackageManifest.Platform value: platforms.values()) {
            if (!value.dependencies().isEmpty() && !isPlatformDependenciesTestOnly(value.dependencies())) {
                return true;
            }
        }
        return false;
    }

    private Optional<CompilerPluginDescriptor> readCompilerPluginToml() {
        Optional<io.ballerina.projects.CompilerPluginToml> compilerPluginToml = backend.packageContext().project()
                .currentPackage().compilerPluginToml();

        if (compilerPluginToml.isPresent()) {
            TomlDocument tomlDocument = compilerPluginToml.get().compilerPluginTomlContext().tomlDocument();
            return Optional.of(CompilerPluginDescriptor.from(tomlDocument));
        }
        return Optional.empty();
    }

    private Optional<BalToolDescriptor> readBalToolToml() {
        Optional<BalToolToml> balToolToml = backend.packageContext().project()
                .currentPackage().balToolToml();
        if (balToolToml.isPresent()) {
            TomlDocument tomlDocument = balToolToml.get().balToolTomlContext().tomlDocument();
            Path sourceRoot = packageContext.project().sourceRoot();
            return Optional.of(BalToolDescriptor.from(tomlDocument, sourceRoot));
        }
        return Optional.empty();
    }

    private boolean isPlatformDependenciesTestOnly(List<Map<String, Object>> dependencies) {
        for (Map<String, Object> dependency : dependencies) {
            if (!Objects.equals(PlatformLibraryScope.TEST_ONLY.getStringValue(), dependency.get("scope"))) {
                return false;
            }
        }
        return true;
    }
}

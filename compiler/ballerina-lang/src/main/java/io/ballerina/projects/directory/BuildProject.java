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
package io.ballerina.projects.directory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageConfig;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageResolution;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.ResolvedPackageDependency;
import io.ballerina.projects.internal.BalaFiles;
import io.ballerina.projects.internal.PackageConfigCreator;
import io.ballerina.projects.internal.ProjectFiles;
import io.ballerina.projects.internal.model.BuildJson;
import io.ballerina.projects.internal.model.Dependency;
import io.ballerina.projects.util.FileUtils;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectPaths;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.ballerina.projects.util.ProjectConstants.BUILD_FILE;
import static io.ballerina.projects.util.ProjectConstants.DEPENDENCIES_TOML;
import static io.ballerina.projects.util.ProjectUtils.getDependenciesTomlContent;
import static io.ballerina.projects.util.ProjectUtils.readBuildJson;

/**
 * {@code BuildProject} represents Ballerina project instance created from the project directory.
 *
 * @since 2.0.0
 */
public class BuildProject extends Project {

    /**
     * Loads a BuildProject from the provided path.
     *
     * @param projectPath Ballerina project path
     * @return build project
     */
    public static BuildProject load(ProjectEnvironmentBuilder environmentBuilder, Path projectPath) {
        return load(environmentBuilder, projectPath, BuildOptions.builder().build());
    }

    /**
     * Loads a BuildProject from the provided path.
     *
     * @param projectPath Ballerina project path
     * @return BuildProject instance
     */
    public static BuildProject load(Path projectPath) {
        return load(projectPath, BuildOptions.builder().build());
    }

    /**
     * Loads a BuildProject from provided path and build options.
     *
     * @param projectPath  Ballerina project path
     * @param buildOptions build options
     * @return BuildProject instance
     */
    public static BuildProject load(Path projectPath, BuildOptions buildOptions) {
        ProjectEnvironmentBuilder environmentBuilder = ProjectEnvironmentBuilder.getDefaultBuilder();
        return load(environmentBuilder, projectPath, buildOptions);
    }

    /**
     * Loads a BuildProject from provided environment builder, path, build options.
     *
     * @param environmentBuilder custom environment builder
     * @param projectPath Ballerina project path
     * @param buildOptions build options
     * @return BuildProject instance
     */
    public static BuildProject load(ProjectEnvironmentBuilder environmentBuilder, Path projectPath,
                                    BuildOptions buildOptions) {
        PackageConfig packageConfig = PackageConfigCreator.createBuildProjectConfig(projectPath);
        BuildOptions mergedBuildOptions = ProjectFiles.createBuildOptions(packageConfig, buildOptions, projectPath);

        BuildProject buildProject = new BuildProject(environmentBuilder, projectPath, mergedBuildOptions);
        buildProject.addPackage(packageConfig);
        return buildProject;
    }

    private BuildProject(ProjectEnvironmentBuilder environmentBuilder, Path projectPath, BuildOptions buildOptions) {
        super(ProjectKind.BUILD_PROJECT, projectPath, environmentBuilder, buildOptions);
        populateCompilerContext();
    }

    private Optional<Path> modulePath(ModuleId moduleId) {
        if (currentPackage().moduleIds().contains(moduleId)) {
            if (currentPackage().getDefaultModule().moduleId() == moduleId) {
                return Optional.of(sourceRoot);
            } else {
                return Optional.of(sourceRoot.resolve(ProjectConstants.MODULES_ROOT).resolve(
                        currentPackage().module(moduleId).moduleName().moduleNamePart()));
            }
        }
        return Optional.empty();
    }

    private Optional<Path> generatedModulePath(ModuleId moduleId) {
        if (currentPackage().moduleIds().contains(moduleId)) {
            Optional<Path> generatedModulePath = Optional.of(sourceRoot.
                    resolve(ProjectConstants.GENERATED_MODULES_ROOT));
            if (currentPackage().getDefaultModule().moduleId() == moduleId && generatedModulePath.isPresent() &&
                    Files.isDirectory(generatedModulePath.get())) {
                return generatedModulePath;
            }
            String moduleName = currentPackage().module(moduleId).moduleName().moduleNamePart();
            if (generatedModulePath.isPresent() && Files.isDirectory(generatedModulePath.get())) {
                Optional<Path> generatedModuleDirPath = Optional.of(generatedModulePath.get().resolve(moduleName));
                if (generatedModuleDirPath.isPresent() && Files.isDirectory(generatedModuleDirPath.get())) {
                    return Optional.of(generatedModulePath.get().resolve(moduleName));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public Optional<Path> documentPath(DocumentId documentId) {
        for (ModuleId moduleId : currentPackage().moduleIds()) {
            Module module = currentPackage().module(moduleId);
            Optional<Path> modulePath = modulePath(moduleId);
            if (module.documentIds().contains(documentId)) {
                Optional<Path> generatedModulePath = generatedModulePath(moduleId);
                if (generatedModulePath.isPresent() && Files.exists(
                        generatedModulePath.get().resolve(module.document(documentId).name()))) {
                    return Optional.of(generatedModulePath.get().resolve(module.document(documentId).name()));
                }
                if (modulePath.isPresent()) {
                    return Optional.of(modulePath.get().resolve(module.document(documentId).name()));
                }
            } else if (module.testDocumentIds().contains(documentId)) {
                if (modulePath.isPresent()) {
                    return Optional.of(modulePath.get()
                            .resolve(ProjectConstants.TEST_DIR_NAME).resolve(
                                    module.document(documentId).name().split(ProjectConstants.TEST_DIR_NAME + "/")[1]));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void clearCaches() {
        resetPackage(this);
        this.projectEnvironment = ProjectEnvironmentBuilder.getDefaultBuilder().build(this);
    }

    @Override
    public Project duplicate() {
        BuildOptions duplicateBuildOptions = BuildOptions.builder().build().acceptTheirs(buildOptions());
        BuildProject buildProject = new BuildProject(
                ProjectEnvironmentBuilder.getDefaultBuilder(), this.sourceRoot, duplicateBuildOptions);
        return resetPackage(buildProject);
    }

    @Override
    public DocumentId documentId(Path file) {
        if (isFilePathInProject(file)) {
            Path parent = Optional.of(file.toAbsolutePath().getParent()).get();
            String parentFileName = Optional.of(parent.getFileName()).get().toString();
            boolean isDefaultModule = false;
            for (ModuleId moduleId : this.currentPackage().moduleIds()) {
                String moduleDirName;
                // Check for the module name contains a dot and not being the default module
                if (!this.currentPackage().getDefaultModule().moduleId().equals(moduleId)) {
                    moduleDirName = currentPackage().module(moduleId).moduleName().toString()
                            .split(this.currentPackage().packageName().toString() + "\\.")[1];
                } else {
                    moduleDirName = Optional.of(this.sourceRoot.getFileName()).get().toString();
                    isDefaultModule = true;
                }

                Module module = this.currentPackage().module(moduleId);
                if (parentFileName.equals(moduleDirName) ||
                        (isDefaultModule && ProjectConstants.GENERATED_MODULES_ROOT.equals(parentFileName))) {
                    // this is a source file
                    for (DocumentId documentId : module.documentIds()) {
                        if (module.document(documentId).name().equals(
                                Optional.of(file.getFileName()).get().toString())) {
                            return documentId;
                        }
                    }
                } else if (ProjectConstants.TEST_DIR_NAME.equals(parentFileName)) {
                    // this is a test file
                    if (Optional.of(Optional.of(parent.getParent()).get().getFileName()).get().toString()
                            .equals(moduleDirName)) {
                        for (DocumentId documentId : module.testDocumentIds()) {
                            String[] splitName = module.document(documentId).name()
                                    .split(ProjectConstants.TEST_DIR_NAME + "/");
                            if (splitName.length > 1 && splitName[1]
                                    .equals(Optional.of(file.getFileName()).get().toString())) {
                                return documentId;
                            }
                        }
                    }
                }
            }
        }
        throw new ProjectException("provided path does not belong to the project");
    }

    private boolean isFilePathInProject(Path filepath) {
        try {
            ProjectPaths.packageRoot(filepath);
        } catch (ProjectException e) {
            return false;
        }
        return true;
    }

    public void save() {
        Path buildFilePath = this.targetDir().resolve(BUILD_FILE);
        boolean shouldUpdate = this.currentPackage().getResolution().autoUpdate();

        // if build file does not exists
        if (!buildFilePath.toFile().exists()) {
            createBuildFile(buildFilePath);
            writeBuildFile(buildFilePath);
            writeDependencies();
        } else {
            BuildJson buildJson = null;
            try {
                buildJson = readBuildJson(buildFilePath);
            } catch (JsonSyntaxException | IOException e) {
                // ignore
            }

            // need to update Dependencies toml
            writeDependencies();

            // check whether buildJson is null and last updated time has expired
            if (buildJson != null && !shouldUpdate) {
                buildJson.setLastBuildTime(System.currentTimeMillis());

                Path projectPath = this.currentPackage().project().sourceRoot();
                Map<String, Long> lastModifiedTime = new HashMap<>();
                lastModifiedTime.put(this.currentPackage().packageName().value(),
                        FileUtils.lastModifiedTimeOfBalProject(projectPath));
                buildJson.setLastModifiedTime(lastModifiedTime);

                writeBuildFile(buildFilePath, buildJson);
            } else {
                writeBuildFile(buildFilePath);
            }
        }
    }

    private void writeDependencies() {
        Package currentPackage = this.currentPackage();
        if (currentPackage != null) {
            Comparator<Dependency> comparator = (o1, o2) -> {
                if (o1.getOrg().equals(o2.getOrg())) {
                    return o1.getName().compareTo(o2.getName());
                }
                return o1.getOrg().compareTo(o2.getOrg());
            };

            List<Dependency> pkgDependencies = getPackageDependencies();
            pkgDependencies.sort(comparator);

            Path dependenciesTomlFile = currentPackage.project().sourceRoot().resolve(DEPENDENCIES_TOML);
            String dependenciesContent = getDependenciesTomlContent(pkgDependencies);
            if (!pkgDependencies.isEmpty()) {
                // write content to Dependencies.toml file
                createIfNotExists(dependenciesTomlFile);
                writeContent(dependenciesTomlFile, dependenciesContent);
            } else {
                // when there are no package dependencies to write
                // if Dependencies.toml does not exists ---> Dependencies.toml will not be created
                // if Dependencies.toml exists          ---> content will be written to existing Dependencies.toml
                if (dependenciesTomlFile.toFile().exists()) {
                    writeContent(dependenciesTomlFile, dependenciesContent);
                }
            }
        }
    }

    private List<Dependency> getPackageDependencies() {
        PackageResolution packageResolution = this.currentPackage().getResolution();
        ResolvedPackageDependency rootPkgNode = new ResolvedPackageDependency(this.currentPackage(),
                                                                              PackageDependencyScope.DEFAULT);
        DependencyGraph<ResolvedPackageDependency> dependencyGraph = packageResolution.dependencyGraph();
        Collection<ResolvedPackageDependency> directDependencies = dependencyGraph.getDirectDependencies(rootPkgNode);

        List<Dependency> dependencies = new ArrayList<>();

        // 1. set root package as a dependency
        Package rootPackage = rootPkgNode.packageInstance();
        Dependency rootPkgDependency = new Dependency(rootPackage.packageOrg().value(),
                                                      rootPackage.packageName().value(),
                                                      rootPackage.packageVersion().value().toString());
        // get modules of the root package
        List<Dependency.Module> rootPkgModules = new ArrayList<>();
        for (ModuleId moduleId : rootPackage.moduleIds()) {
            Module module = rootPackage.module(moduleId);
            Dependency.Module depsModule = new Dependency.Module(module.descriptor().org().value(),
                                                                 module.descriptor().packageName().value(),
                                                                 module.descriptor().name().toString());
            rootPkgModules.add(depsModule);
        }
        // sort modules
        rootPkgModules.sort(Comparator.comparing(Dependency.Module::moduleName));
        rootPkgDependency.setModules(rootPkgModules);
        // get transitive dependencies of the root package
        rootPkgDependency.setDependencies(getTransitiveDependencies(dependencyGraph, rootPkgNode));
        // set transitive and scope
        rootPkgDependency.setTransitive(false);
        rootPkgDependency.setScope(rootPkgNode.scope());
        dependencies.add(rootPkgDependency);

        // 2. set direct dependencies
        for (ResolvedPackageDependency directDependency : directDependencies) {
            Package aPackage = directDependency.packageInstance();
            Dependency dependency = new Dependency(aPackage.packageOrg().toString(), aPackage.packageName().value(),
                                                   aPackage.packageVersion().toString());

            // get modules of the direct dependency package
            BalaFiles.DependencyGraphResult packageDependencyGraph = BalaFiles
                    .createPackageDependencyGraph(directDependency.packageInstance().project().sourceRoot());
            Set<ModuleDescriptor> moduleDescriptors = packageDependencyGraph.moduleDependencies().keySet();

            List<Dependency.Module> modules = new ArrayList<>();
            for (ModuleDescriptor moduleDescriptor : moduleDescriptors) {
                Dependency.Module module = new Dependency.Module(moduleDescriptor.org().value(),
                                                                 moduleDescriptor.packageName().value(),
                                                                 moduleDescriptor.name().toString());
                modules.add(module);
            }
            // sort modules
            modules.sort(Comparator.comparing(Dependency.Module::moduleName));
            dependency.setModules(modules);
            // get transitive dependencies of the direct dependency package
            dependency.setDependencies(getTransitiveDependencies(dependencyGraph, directDependency));
            // set transitive and scope
            dependency.setScope(directDependency.scope());
            dependency.setTransitive(false);
            dependencies.add(dependency);
        }

        // 3. set transitive dependencies
        Collection<ResolvedPackageDependency> allDependencies = dependencyGraph.getNodes();
        for (ResolvedPackageDependency transDependency : allDependencies) {
            // check whether it's a direct dependency, skip it since it is already added
            if (directDependencies.contains(transDependency)) {
                continue;
            }
            if (transDependency.packageInstance() != this.currentPackage()) {
                Package aPackage = transDependency.packageInstance();
                Dependency dependency = new Dependency(aPackage.packageOrg().toString(),
                                                       aPackage.packageName().value(),
                                                       aPackage.packageVersion().toString());
                // get transitive dependencies of the transitive dependency package
                dependency.setDependencies(getTransitiveDependencies(dependencyGraph, transDependency));
                // set transitive and scope
                dependency.setScope(transDependency.scope());
                dependency.setTransitive(true);
                dependencies.add(dependency);
            }
        }

        return dependencies;
    }

    private List<Dependency> getTransitiveDependencies(DependencyGraph<ResolvedPackageDependency> dependencyGraph,
                                                       ResolvedPackageDependency directDependency) {
        List<Dependency> dependencyList = new ArrayList<>();
        Collection<ResolvedPackageDependency> pkgDependencies = dependencyGraph
                .getDirectDependencies(directDependency);
        for (ResolvedPackageDependency resolvedTransitiveDep : pkgDependencies) {
            Package dependencyPkgContext = resolvedTransitiveDep.packageInstance();
            Dependency dep = new Dependency(dependencyPkgContext.packageOrg().toString(),
                                            dependencyPkgContext.packageName().value(),
                                            dependencyPkgContext.packageVersion().toString());
            dependencyList.add(dep);
        }
        // sort transitive dependencies list
        Comparator<Dependency> comparator = (o1, o2) -> {
            if (o1.getOrg().equals(o2.getOrg())) {
                return o1.getName().compareTo(o2.getName());
            }
            return o1.getOrg().compareTo(o2.getOrg());
        };
        dependencyList.sort(comparator);
        return dependencyList;
    }

    private static void createIfNotExists(Path filePath) {
        if (!filePath.toFile().exists()) {
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                throw new ProjectException("Failed to create 'Dependencies.toml' file to write dependencies");
            }
        }
    }

    private static void writeContent(Path filePath, String content) {
        try {
            Files.write(filePath, Collections.singleton(content));
        } catch (IOException e) {
            throw new ProjectException("Failed to write dependencies to the 'Dependencies.toml' file");
        }
    }

    private static void createBuildFile(Path buildFilePath) {
        try {
            if (!buildFilePath.getParent().toFile().exists()) {
                // create target directory if not exists
                Files.createDirectory(buildFilePath.getParent());
            }
            Files.createFile(buildFilePath);
        } catch (IOException e) {
            throw new ProjectException("Failed to create '" + BUILD_FILE + "' file");
        }
    }

    private void writeBuildFile(Path buildFilePath) {
        Path projectPath = this.currentPackage().project().sourceRoot();
        Map<String, Long> lastModifiedTime = new HashMap<>();
        lastModifiedTime.put(this.currentPackage().packageName().value(),
                FileUtils.lastModifiedTimeOfBalProject(projectPath));

        BuildJson buildJson = new BuildJson(System.currentTimeMillis(), System.currentTimeMillis(),
                RepoUtils.getBallerinaShortVersion(), lastModifiedTime);
        writeBuildFile(buildFilePath, buildJson);
    }

    private static void writeBuildFile(Path buildFilePath, BuildJson buildJson) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        // Check write permissions
        if (!buildFilePath.toFile().canWrite()) {
            return;
        }
        // write build file
        try {
            Files.write(buildFilePath, Collections.singleton(gson.toJson(buildJson)));
        } catch (IOException e) {
            // ignore
        }
    }

    @Override
    public Path targetDir() {
        if (this.buildOptions().getTargetPath() == null) {
            return this.sourceRoot.resolve(ProjectConstants.TARGET_DIR_NAME);
        } else {
            return Paths.get(this.buildOptions().getTargetPath());
        }
    }
}

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

package io.ballerina.projects.internal;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.ballerina.projects.DependencyGraph;
import io.ballerina.projects.DependencyManifest;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.internal.bala.CompilerPluginJson;
import io.ballerina.projects.internal.bala.DependencyGraphJson;
import io.ballerina.projects.internal.bala.ModuleDependency;
import io.ballerina.projects.internal.model.CompilerPluginDescriptor;
import io.ballerina.projects.internal.model.PackageJson;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.projects.DependencyGraph.DependencyGraphBuilder.getBuilder;
import static io.ballerina.projects.internal.ProjectFiles.loadDocuments;
import static io.ballerina.projects.internal.ProjectFiles.loadResources;
import static io.ballerina.projects.util.ProjectConstants.BALA_DOCS_DIR;
import static io.ballerina.projects.util.ProjectConstants.COMPILER_PLUGIN_DIR;
import static io.ballerina.projects.util.ProjectConstants.COMPILER_PLUGIN_JSON;
import static io.ballerina.projects.util.ProjectConstants.DEPENDENCY_GRAPH_JSON;
import static io.ballerina.projects.util.ProjectConstants.MODULES_ROOT;
import static io.ballerina.projects.util.ProjectConstants.MODULE_NAME_SEPARATOR;
import static io.ballerina.projects.util.ProjectConstants.PACKAGE_JSON;

/**
 * Contains a set of utility methods that create an in-memory representation of a Ballerina project using a bala.
 *
 * @since 2.0.0
 */
public class BalaFiles {
    private static final Gson gson = new Gson();

    // TODO change class name to utils
    private BalaFiles() {
    }

    static PackageData loadPackageData(Path balaPath, PackageManifest packageManifest) {
        if (balaPath.toFile().isDirectory()) {
            return loadPackageDataFromBalaDir(balaPath, packageManifest);
        } else {
            return loadPackageDataFromBalaFile(balaPath, packageManifest);
        }
    }

    private static PackageData loadPackageDataFromBalaDir(Path balaPath, PackageManifest packageManifest) {
        // Load default module
        String pkgName = packageManifest.name().toString();
        ModuleData defaultModule = loadModule(pkgName, pkgName, balaPath);
        DocumentData packageMd = loadDocument(balaPath.resolve(BALA_DOCS_DIR)
                .resolve(ProjectConstants.PACKAGE_MD_FILE_NAME));
        // load other modules
        List<ModuleData> otherModules = loadOtherModules(pkgName, balaPath);
        return PackageData.from(balaPath, defaultModule, otherModules, null, null, null, null, packageMd);
    }

    private static PackageData loadPackageDataFromBalaFile(Path balaPath, PackageManifest packageManifest) {
        URI zipURI = URI.create("jar:" + balaPath.toUri().toString());
        try (FileSystem zipFileSystem = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
            // Load default module
            String pkgName = packageManifest.name().toString();
            Path packageRoot = zipFileSystem.getPath("/");
            ModuleData defaultModule = loadModule(pkgName, pkgName, packageRoot);
            DocumentData packageMd = loadDocument(zipFileSystem.getPath(BALA_DOCS_DIR)
                    .resolve(ProjectConstants.PACKAGE_MD_FILE_NAME));
            // load other modules
            List<ModuleData> otherModules = loadOtherModules(pkgName, packageRoot);
            return PackageData.from(balaPath, defaultModule, otherModules, null, null, null, null, packageMd);
        } catch (IOException e) {
            throw new ProjectException("Failed to read bala file:" + balaPath);
        }
    }

    private static void validatePackageJson(PackageJson packageJson, Path balaPath) {
        if (packageJson.getOrganization() == null || "".equals(packageJson.getOrganization())) {
            throw new ProjectException("'organization' does not exists in 'package.json': " + balaPath);
        }
        if (packageJson.getName() == null || "".equals(packageJson.getName())) {
            throw new ProjectException("'name' does not exists in 'package.json': " + balaPath);
        }
        if (packageJson.getVersion() == null || "".equals(packageJson.getVersion())) {
            throw new ProjectException("'version' does not exists in 'package.json': " + balaPath);
        }
    }

    public static DocumentData loadDocument(Path documentFilePath) {
        if (Files.notExists(documentFilePath)) {
            return null;
        } else {
            String content;
            try {
                content = Files.readString(documentFilePath, Charset.defaultCharset());
            } catch (IOException e) {
                throw new ProjectException(e);
            }
            return DocumentData.from(Optional.of(documentFilePath.getFileName()).get().toString(), content);
        }
    }

    private static ModuleData loadModule(String pkgName, String fullModuleName,  Path packagePath) {
        Path modulePath = packagePath.resolve(MODULES_ROOT).resolve(fullModuleName);
        Path moduleDocPath = packagePath.resolve(BALA_DOCS_DIR).resolve(MODULES_ROOT).resolve(fullModuleName);
        // check module path exists
        if (Files.notExists(modulePath)) {
            throw new ProjectException("The 'modules' directory does not exists in '" + modulePath + "'");
        }

        String moduleName = fullModuleName;
        if (!pkgName.equals(moduleName)) {
            // not default module
            moduleName = fullModuleName.substring(pkgName.length() + 1);
        }

        // validate moduleName
        if (!ProjectUtils.validateModuleName(moduleName)) {
            throw new ProjectException("Invalid module name : '" + moduleName + "' :\n" +
                    "Module name can only contain alphanumerics, underscores and periods: " + modulePath);
        }
        if (!ProjectUtils.validateNameLength(moduleName)) {
            throw new ProjectException("Invalid module name : '" + moduleName + "' :\n" +
                    "Maximum length of module name is 256 characters: " + modulePath);
        }

        List<DocumentData> srcDocs = loadDocuments(modulePath);
        List<DocumentData> testSrcDocs = Collections.emptyList();
        DocumentData moduleMd = loadDocument(moduleDocPath.resolve(ProjectConstants.MODULE_MD_FILE_NAME));
        List<Path> resources = loadResources(modulePath);

        return ModuleData.from(modulePath, moduleName, srcDocs, testSrcDocs, moduleMd, resources,
                Collections.emptyList());
    }

    private static List<ModuleData> loadOtherModules(String pkgName, Path packagePath) {
        Path modulesDirPath = packagePath.resolve(MODULES_ROOT);
        try (Stream<Path> pathStream = Files.walk(modulesDirPath, 1)) {
            return pathStream
                    .filter(path -> !path.equals(modulesDirPath))
                    .filter(path -> path.getFileName() != null
                            && !path.getFileName().toString().equals(pkgName))
                    .filter(Files::isDirectory)
                    .map(modulePath -> modulePath.getFileName().toString())
                    .map(fullModuleName -> loadModule(pkgName, fullModuleName, packagePath))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ProjectException("Failed to read modules from directory: " + modulesDirPath, e);
        }
    }

    public static PackageManifest createPackageManifest(Path balaPath) {
        if (balaPath.toFile().isDirectory()) {
            return createPackageManifestFromBalaDir(balaPath);
        } else {
            return createPackageManifestFromBalaFile(balaPath);
        }
    }

    public static DependencyManifest createDependencyManifest(Path balaPath) {
        if (balaPath.toFile().isDirectory()) {
            return createDependencyManifestFromBalaDir(balaPath);
        } else {
            return createDependencyManifestFromBalaFile(balaPath);
        }
    }

    public static DependencyGraphResult createPackageDependencyGraph(Path balaPath) {
        DependencyGraphResult dependencyGraphResult;
        if (balaPath.toFile().isDirectory()) {
            Path dependencyGraphJsonPath = balaPath.resolve(DEPENDENCY_GRAPH_JSON);
            dependencyGraphResult = createPackageDependencyGraphFromJson(dependencyGraphJsonPath);
        } else {
            URI zipURI = URI.create("jar:" + balaPath.toAbsolutePath().toUri().toString());
            try (FileSystem zipFileSystem = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
                Path dependencyGraphJsonPath = zipFileSystem.getPath(DEPENDENCY_GRAPH_JSON);
                dependencyGraphResult = createPackageDependencyGraphFromJson(dependencyGraphJsonPath);
            } catch (IOException e) {
                throw new ProjectException("Failed to read balr file:" + balaPath);
            }
        }
        return dependencyGraphResult;
    }

    static DependencyGraphResult createPackageDependencyGraphFromJson(Path dependencyGraphJsonPath) {
        if (Files.notExists(dependencyGraphJsonPath)) {
            throw new ProjectException(dependencyGraphJsonPath + " does not exist.'");
        }

        // Load `dependency-graph.json`
        DependencyGraphJson dependencyGraphJson = readDependencyGraphJson(dependencyGraphJsonPath);

        DependencyGraph<PackageDescriptor> packageDependencyGraph = createPackageDependencyGraph(
                dependencyGraphJson.getPackageDependencyGraph());
        Map<ModuleDescriptor, List<ModuleDescriptor>> moduleDescriptorListMap = createModuleDescDependencies(
                dependencyGraphJson.getModuleDependencies());

        return new DependencyGraphResult(packageDependencyGraph, moduleDescriptorListMap);
    }

    private static PackageManifest createPackageManifestFromBalaFile(Path balrPath) {
        URI zipURI = URI.create("jar:" + balrPath.toAbsolutePath().toUri().toString());
        try (FileSystem zipFileSystem = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
            Path packageJsonPath = zipFileSystem.getPath(PACKAGE_JSON);
            if (Files.notExists(packageJsonPath)) {
                throw new ProjectException("package.json does not exists in '" + balrPath + "'");
            }

            // Load `package.json`
            PackageJson packageJson = readPackageJson(balrPath, packageJsonPath);
            validatePackageJson(packageJson, balrPath);
            extractPlatformLibraries(packageJson, balrPath, zipFileSystem);

            // Load `compiler-plugin.json`
            Path compilerPluginJsonPath = zipFileSystem.getPath(COMPILER_PLUGIN_DIR, COMPILER_PLUGIN_JSON);
            if (!Files.notExists(compilerPluginJsonPath)) {
                CompilerPluginJson compilerPluginJson = readCompilerPluginJson(balrPath, compilerPluginJsonPath);
                extractCompilerPluginLibraries(compilerPluginJson, balrPath, zipFileSystem);
                return getPackageManifest(packageJson, Optional.of(compilerPluginJson));
            }
            return getPackageManifest(packageJson, Optional.empty());
        } catch (IOException e) {
            throw new ProjectException("Failed to read balr file:" + balrPath);
        }
    }

    private static PackageManifest createPackageManifestFromBalaDir(Path balrPath) {
        Path packageJsonPath = balrPath.resolve(PACKAGE_JSON);
        if (Files.notExists(packageJsonPath)) {
            throw new ProjectException("package.json does not exists in '" + balrPath + "'");
        }

        // Load `package.json`
        PackageJson packageJson = readPackageJson(balrPath, packageJsonPath);
        validatePackageJson(packageJson, balrPath);

        // Load `compiler-plugin.json`
        Path compilerPluginJsonPath = balrPath.resolve(COMPILER_PLUGIN_DIR).resolve(COMPILER_PLUGIN_JSON);
        if (!Files.notExists(compilerPluginJsonPath)) {
            CompilerPluginJson compilerPluginJson = readCompilerPluginJson(balrPath, compilerPluginJsonPath);
            setCompilerPluginDependencyPaths(compilerPluginJson, balrPath);
            return getPackageManifest(packageJson, Optional.of(compilerPluginJson));
        }
        return getPackageManifest(packageJson, Optional.empty());
    }

    private static DependencyManifest createDependencyManifestFromBalaFile(Path balrPath) {
        URI zipURI = URI.create("jar:" + balrPath.toAbsolutePath().toUri());
        try (FileSystem zipFileSystem = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
            Path depsGraphJsonPath = zipFileSystem.getPath(DEPENDENCY_GRAPH_JSON);
            if (Files.notExists(depsGraphJsonPath)) {
                throw new ProjectException(DEPENDENCY_GRAPH_JSON + " does not exists in '" + balrPath + "'");
            }

            // Load `dependency-graph.json`
            DependencyGraphJson dependencyGraphJson = readDependencyGraphJson(balrPath, depsGraphJsonPath);
            return getDependencyManifest(dependencyGraphJson);
        } catch (IOException e) {
            throw new ProjectException("Failed to read balr file:" + balrPath);
        }
    }

    private static DependencyManifest createDependencyManifestFromBalaDir(Path balrPath) {
        Path depsGraphJsonPath = balrPath.resolve(DEPENDENCY_GRAPH_JSON);
        if (Files.notExists(depsGraphJsonPath)) {
            throw new ProjectException(DEPENDENCY_GRAPH_JSON + " does not exists in '" + balrPath + "'");
        }

        // Load `dependency-graph.json`
        DependencyGraphJson dependencyGraphJson = readDependencyGraphJson(balrPath, depsGraphJsonPath);
        return getDependencyManifest(dependencyGraphJson);
    }

    private static void extractPlatformLibraries(PackageJson packageJson, Path balaPath, FileSystem zipFileSystem) {
        if (packageJson.getPlatformDependencies() == null) {
            return;
        }
        packageJson.getPlatformDependencies().forEach(dependency -> {
            Path libPath = balaPath.getParent().resolve(dependency.getPath());
            if (!Files.exists(libPath)) {
                try {
                    Files.createDirectories(libPath.getParent());
                    Files.copy(zipFileSystem.getPath(dependency.getPath()), libPath);
                } catch (IOException e) {
                    throw new ProjectException("Failed to extract platform dependency:" + libPath.getFileName(), e);
                }
            }
            dependency.setPath(libPath.toString());
        });
    }

    private static void extractCompilerPluginLibraries(CompilerPluginJson compilerPluginJson, Path balaPath,
            FileSystem zipFileSystem) {
        if (compilerPluginJson.dependencyPaths() == null) {
            return;
        }
        List<String> dependencyLibPaths = new ArrayList<>();
        compilerPluginJson.dependencyPaths().forEach(dependencyPath -> {
            Path libPath = balaPath.getParent().resolve(dependencyPath).normalize();
            if (!Files.exists(libPath)) {
                try {
                    Files.createDirectories(libPath.getParent());
                    // TODO: Need to refactor this fix
                    Path libPathInZip = Paths.get(dependencyPath);
                    if (!dependencyPath.contains(COMPILER_PLUGIN_DIR)) {
                        libPathInZip = Paths.get(COMPILER_PLUGIN_DIR, String.valueOf(libPathInZip));
                    }
                    Files.copy(zipFileSystem.getPath(String.valueOf(libPathInZip)), libPath);
                } catch (IOException e) {
                    throw new ProjectException(
                            "Failed to extract compiler plugin dependency:" + libPath.getFileName(), e);
                }
            }
            dependencyLibPaths.add(libPath.toString());
        });
        compilerPluginJson.setDependencyPaths(dependencyLibPaths);
    }

    private static void setCompilerPluginDependencyPaths(CompilerPluginJson compilerPluginJson, Path balaPath) {
        if (compilerPluginJson.dependencyPaths() == null) {
            return;
        }
        List<String> dependencyLibPaths = new ArrayList<>();
        compilerPluginJson.dependencyPaths().forEach(dependencyPath -> {
            Path libPath = balaPath.resolve(dependencyPath);
            dependencyLibPaths.add(libPath.toString());
        });
        compilerPluginJson.setDependencyPaths(dependencyLibPaths);
    }

    private static PackageManifest getPackageManifest(PackageJson packageJson,
            Optional<CompilerPluginJson> compilerPluginJson) {
        PackageDescriptor pkgDesc = PackageDescriptor.from(PackageOrg.from(packageJson.getOrganization()),
                PackageName.from(packageJson.getName()), PackageVersion.from(packageJson.getVersion()));

        Map<String, PackageManifest.Platform> platforms = new HashMap<>(Collections.emptyMap());
        if (packageJson.getPlatformDependencies() != null) {
            List<Map<String, Object>> platformDependencies = new ArrayList<>();
            packageJson.getPlatformDependencies().forEach(dependency -> {
                String jsonStr = gson.toJson(dependency);
                platformDependencies.add(gson.fromJson(jsonStr, Map.class));

            });
            PackageManifest.Platform platform = new PackageManifest.Platform(platformDependencies);
            platforms.put(packageJson.getPlatform(), platform);
        }

        List<PackageManifest.Dependency> dependencies = new ArrayList<>();
        if (packageJson.getLocalDependencies() != null) {
            packageJson.getLocalDependencies().forEach(localDependency -> {
                PackageManifest.Dependency dependency = new PackageManifest.Dependency(
                        PackageName.from(localDependency.getName()), PackageOrg.from(localDependency.getOrg()),
                        PackageVersion.from(localDependency.getVersion()), localDependency.getRepository());
                dependencies.add(dependency);
            });
        }

        return compilerPluginJson.map(pluginJson -> PackageManifest
                        .from(pkgDesc, CompilerPluginDescriptor.from(pluginJson), platforms, dependencies,
                                packageJson.getLicenses(), packageJson.getAuthors(), packageJson.getKeywords(),
                                packageJson.getExport(), packageJson.getInclude(), packageJson.getSourceRepository(),
                                packageJson.getBallerinaVersion(), packageJson.getVisibility(),
                                packageJson.getTemplate()))
                .orElseGet(() -> PackageManifest
                        .from(pkgDesc, null, platforms, dependencies, packageJson.getLicenses(),
                                packageJson.getAuthors(), packageJson.getKeywords(), packageJson.getExport(),
                                packageJson.getInclude(), packageJson.getSourceRepository(),
                                packageJson.getBallerinaVersion(), packageJson.getVisibility(),
                                packageJson.getTemplate()));
    }

    private static DependencyManifest getDependencyManifest(DependencyGraphJson dependencyGraphJson) {
        List<DependencyManifest.Package> packages = new ArrayList<>();
        for (io.ballerina.projects.internal.model.Dependency dependency :
                dependencyGraphJson.getPackageDependencyGraph()) {
            List<DependencyManifest.Dependency> dependencies = new ArrayList<>();
            List<DependencyManifest.Module> modules = new ArrayList<>();
            for (io.ballerina.projects.internal.model.Dependency transDependency : dependency.getDependencies()) {
                DependencyManifest.Dependency dep = new DependencyManifest.Dependency(
                        PackageName.from(transDependency.getName()), PackageOrg.from(transDependency.getOrg()));
                dependencies.add(dep);
            }

            if (dependency.getModules() != null && !dependency.getModules().isEmpty()) {
                for (io.ballerina.projects.internal.model.Dependency.Module depModule : dependency.getModules()) {
                    DependencyManifest.Module module = new DependencyManifest.Module(depModule.org(),
                                                                                     depModule.packageName(),
                                                                                     depModule.moduleName());
                    modules.add(module);
                }
            }

            DependencyManifest.Package pkg = new
                    DependencyManifest.Package(PackageName.from(dependency.getName()),
                                               PackageOrg.from(dependency.getOrg()),
                                               PackageVersion.from(dependency.getVersion()),
                                               dependency.getScope() != null ? dependency.getScope().name() : null,
                                               dependency.isTransitive(),
                                               dependencies,
                                               modules);
            packages.add(pkg);
        }

        return DependencyManifest.from(null, packages);
    }

    private static PackageJson readPackageJson(Path balaPath, Path packageJsonPath) {
        PackageJson packageJson;
        try (BufferedReader bufferedReader = Files.newBufferedReader(packageJsonPath)) {
            packageJson = gson.fromJson(bufferedReader, PackageJson.class);
        } catch (JsonSyntaxException e) {
            throw new ProjectException("Invalid package.json format in '" + balaPath + "'");
        } catch (IOException e) {
            throw new ProjectException("Failed to read the package.json in '" + balaPath + "'");
        }
        return packageJson;
    }

    public static PackageJson readPkgJson(Path packageJsonPath) {
        PackageJson packageJson;
        try (BufferedReader bufferedReader = Files.newBufferedReader(packageJsonPath)) {
            packageJson = gson.fromJson(bufferedReader, PackageJson.class);
        } catch (JsonSyntaxException e) {
            throw new ProjectException("Invalid package.json format");
        } catch (IOException e) {
            throw new ProjectException("Failed to read the package.json");
        }

        return packageJson;
    }

    private static DependencyGraphJson readDependencyGraphJson(Path balaPath, Path depsGraphJsonPath) {
        DependencyGraphJson dependencyGraphJson;
        try (BufferedReader bufferedReader = Files.newBufferedReader(depsGraphJsonPath)) {
            dependencyGraphJson = gson.fromJson(bufferedReader, DependencyGraphJson.class);
        } catch (JsonSyntaxException e) {
            throw new ProjectException("Invalid " + DEPENDENCY_GRAPH_JSON + " format in '" + balaPath + "'");
        } catch (IOException e) {
            throw new ProjectException("Failed to read the " + DEPENDENCY_GRAPH_JSON + " in '" + balaPath + "'");
        }
        return dependencyGraphJson;
    }

    private static CompilerPluginJson readCompilerPluginJson(Path balaPath, Path compilerPluginJsonPath) {
        CompilerPluginJson pluginJson;
        try (BufferedReader bufferedReader = Files.newBufferedReader(compilerPluginJsonPath)) {
            pluginJson = gson.fromJson(bufferedReader, CompilerPluginJson.class);
        } catch (JsonSyntaxException e) {
            throw new ProjectException("Invalid " + COMPILER_PLUGIN_JSON + " format in '" + balaPath + "'");
        } catch (IOException e) {
            throw new ProjectException("Failed to read the " + COMPILER_PLUGIN_JSON + " in '" + balaPath + "'");
        }
        return pluginJson;
    }

    private static DependencyGraph<PackageDescriptor> createPackageDependencyGraph(
            List<io.ballerina.projects.internal.model.Dependency> packageDependencyGraph) {
        DependencyGraph.DependencyGraphBuilder<PackageDescriptor> graphBuilder = getBuilder();

        for (io.ballerina.projects.internal.model.Dependency dependency : packageDependencyGraph) {
            PackageDescriptor pkg = PackageDescriptor.from(PackageOrg.from(dependency.getOrg()),
                    PackageName.from(dependency.getName()), PackageVersion.from(dependency.getVersion()));
            Set<PackageDescriptor> dependentPackages = new HashSet<>();
            for (io.ballerina.projects.internal.model.Dependency dependencyPkg : dependency.getDependencies()) {
                dependentPackages.add(PackageDescriptor.from(PackageOrg.from(dependencyPkg.getOrg()),
                        PackageName.from(dependencyPkg.getName()),
                        PackageVersion.from(dependencyPkg.getVersion())));
            }
            graphBuilder.addDependencies(pkg, dependentPackages);
        }

        return graphBuilder.build();
    }

    private static Map<ModuleDescriptor, List<ModuleDescriptor>> createModuleDescDependencies(
            List<ModuleDependency> modDepEntries) {
        return modDepEntries.stream()
                .collect(Collectors.toMap(BalaFiles::getModuleDescriptorFromDependencyEntry,
                        modDepEntry -> createModDescriptorList(modDepEntry.getDependencies())));
    }

    private static ModuleDescriptor getModuleDescriptorFromDependencyEntry(ModuleDependency modDepEntry) {
        PackageDescriptor pkgDesc = PackageDescriptor.from(PackageOrg.from(modDepEntry.getOrg()),
                PackageName.from(modDepEntry.getPackageName()),
                PackageVersion.from(modDepEntry.getVersion()));
        final ModuleName moduleName;
        if (modDepEntry.getModuleName().equals(pkgDesc.name().toString())) {
            moduleName = ModuleName.from(pkgDesc.name());
        } else {
            String moduleNamePart = modDepEntry.getModuleName()
                    .split(modDepEntry.getPackageName() + MODULE_NAME_SEPARATOR, 2)[1];
            moduleName = ModuleName.from(pkgDesc.name(), moduleNamePart);
        }
        return ModuleDescriptor.from(moduleName, pkgDesc);
    }

    private static List<ModuleDescriptor> createModDescriptorList(List<ModuleDependency> modDepEntries) {
        return modDepEntries.stream()
                .map(BalaFiles::getModuleDescriptorFromDependencyEntry)
                .collect(Collectors.toList());
    }

    private static DependencyGraphJson readDependencyGraphJson(Path dependencyGraphJsonPath) {
        DependencyGraphJson dependencyGraphJson;
        try (BufferedReader bufferedReader = Files.newBufferedReader(dependencyGraphJsonPath)) {
            dependencyGraphJson = gson
                    .fromJson(bufferedReader, DependencyGraphJson.class);
        } catch (JsonSyntaxException e) {
            throw new ProjectException(
                    "Invalid " + DEPENDENCY_GRAPH_JSON + " format in '" + dependencyGraphJsonPath + "'");
        } catch (IOException e) {
            throw new ProjectException(
                    "Failed to read the " + DEPENDENCY_GRAPH_JSON + " in '" + dependencyGraphJsonPath + "'");
        }
        return dependencyGraphJson;
    }

    /**
     * {@code DependencyGraphResult} contains package and module dependency graphs.
     */
    public static class DependencyGraphResult {
        private final DependencyGraph<PackageDescriptor> packageDependencyGraph;
        private final Map<ModuleDescriptor, List<ModuleDescriptor>> moduleDependencies;

        DependencyGraphResult(DependencyGraph<PackageDescriptor> packageDependencyGraph,
                              Map<ModuleDescriptor, List<ModuleDescriptor>> moduleDependencies) {
            this.packageDependencyGraph = packageDependencyGraph;
            this.moduleDependencies = moduleDependencies;
        }

        public DependencyGraph<PackageDescriptor> packageDependencyGraph() {
            return packageDependencyGraph;
        }

        public Map<ModuleDescriptor, List<ModuleDescriptor>> moduleDependencies() {
            return moduleDependencies;
        }
    }

    /**
     * Returns a PacakgeJson instance from the provided bala.
     *
     * @param balaPath path to .bala file or extracted directory
     * @return a PackageJson instance
     */
    public static PackageJson readPackageJson(Path balaPath) {
        PackageJson packageJson;
        if (balaPath.toFile().isDirectory()) {
            Path packageJsonPath = balaPath.resolve(PACKAGE_JSON);
            if (Files.notExists(packageJsonPath)) {
                throw new ProjectException("package.json does not exists in '" + balaPath + "'");
            }
            packageJson = readPackageJson(balaPath, packageJsonPath);
        } else {
            URI zipURI = URI.create("jar:" + balaPath.toAbsolutePath().toUri().toString());
            try (FileSystem zipFileSystem = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
                Path packageJsonPath = zipFileSystem.getPath(PACKAGE_JSON);
                if (Files.notExists(packageJsonPath)) {
                    throw new ProjectException("package.json does not exists in '" + balaPath + "'");
                }
                packageJson = readPackageJson(balaPath, packageJsonPath);
            } catch (IOException e) {
                throw new ProjectException("Failed to read balr file:" + balaPath);
            }
        }
        return packageJson;
    }
}

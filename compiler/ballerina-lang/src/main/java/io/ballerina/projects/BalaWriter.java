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
import io.ballerina.projects.environment.PackageCache;
import io.ballerina.projects.internal.bala.BalaJson;
import io.ballerina.projects.internal.bala.DependencyGraphJson;
import io.ballerina.projects.internal.bala.ModuleDependency;
import io.ballerina.projects.internal.bala.PackageJson;
import io.ballerina.projects.internal.bala.adaptors.JsonCollectionsAdaptor;
import io.ballerina.projects.internal.bala.adaptors.JsonStringsAdaptor;
import io.ballerina.projects.internal.model.BalToolDescriptor;
import io.ballerina.projects.internal.model.CompilerPluginDescriptor;
import io.ballerina.projects.internal.model.Dependency;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.tools.text.TextDocument;
import io.ballerina.tools.text.TextDocuments;
import org.apache.commons.compress.utils.IOUtils;
import org.ballerinalang.compiler.BLangCompilerException;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import static io.ballerina.projects.util.ProjectConstants.BALA_DOCS_DIR;
import static io.ballerina.projects.util.ProjectConstants.BALA_JSON;
import static io.ballerina.projects.util.ProjectConstants.DEPENDENCY_GRAPH_JSON;
import static io.ballerina.projects.util.ProjectConstants.PACKAGE_JSON;
import static io.ballerina.projects.util.ProjectUtils.getBalaName;

/**
 * {@code BalaWriter} writes a package to bala format.
 *
 * @since 2.0.0
 */
public abstract class BalaWriter {
    private static final String MODULES_ROOT = "modules";
    private static final String RESOURCE_DIR_NAME = "resources";
    private static final String BLANG_SOURCE_EXT = ".bal";
    protected static final String PLATFORM = "platform";
    protected static final String PATH = "path";
    private static final String MAIN_BAL = "main.bal";

    // Set the target as any for default bala.
    protected String target = "any";
    private static final String IMPLEMENTATION_VENDOR = "WSO2";
    private static final String BALLERINA_SHORT_VERSION = RepoUtils.getBallerinaShortVersion();
    private static final String BALLERINA_SPEC_VERSION = RepoUtils.getBallerinaSpecVersion();
    protected PackageContext packageContext;
    Optional<CompilerPluginDescriptor> compilerPluginToml;
    protected Optional<BalToolDescriptor> balToolToml;

    protected BalaWriter() {
    }

    /**
     * Write a package to a .bala and return the created .bala path.
     *
     * @param balaPath Directory where the .bala should be created.
     */
    public Path write(Path balaPath) {
        String balaName = getBalaName(this.packageContext.packageOrg().value(),
                this.packageContext.packageName().value(),
                this.packageContext.packageVersion().value().toString(),
                this.target);
        // Create the archive over write if exists
        try (ZipOutputStream balaOutputStream = new ZipOutputStream(
                new FileOutputStream(String.valueOf(balaPath.resolve(balaName))))) {
            // Now lets put stuff in
            populateBalaArchive(balaOutputStream);
        } catch (IOException e) {
            throw new ProjectException("Failed to create bala :" + e.getMessage(), e);
        } catch (BLangCompilerException be) {
            // clean up if an error occur
            try {
                Files.delete(balaPath);
            } catch (IOException e) {
                // We ignore this error and throw out the original blang compiler error to the user
            }
            throw be;
        }
        return balaPath.resolve(balaName);
    }

    private void populateBalaArchive(ZipOutputStream balaOutputStream)
            throws IOException {

        addBalaJson(balaOutputStream);
        addPackageDoc(balaOutputStream,
                this.packageContext.project().sourceRoot(),
                this.packageContext.packageName().toString());
        addPackageSource(balaOutputStream);
        addIncludes(balaOutputStream);
        Optional<JsonArray> platformLibs = addPlatformLibs(balaOutputStream);
        addPackageJson(balaOutputStream, platformLibs);

        addCompilerPlugin(balaOutputStream);
        addBalTool(balaOutputStream);
        addDependenciesJson(balaOutputStream);
    }

    private void addBalaJson(ZipOutputStream balaOutputStream) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String balaJson = gson.toJson(new BalaJson());
        try {
            putZipEntry(balaOutputStream, Paths.get(BALA_JSON),
                    new ByteArrayInputStream(balaJson.getBytes(Charset.defaultCharset())));
        } catch (IOException e) {
            throw new ProjectException("Failed to write 'bala.json' file: " + e.getMessage(), e);
        }
    }

    private void addPackageJson(ZipOutputStream balaOutputStream, Optional<JsonArray> platformLibs) {
        PackageJson packageJson = new PackageJson(this.packageContext.packageOrg().toString(),
                this.packageContext.packageName().toString(),
                this.packageContext.packageVersion().toString());

        PackageManifest packageManifest = this.packageContext.packageManifest();
        packageJson.setLicenses(packageManifest.license());
        packageJson.setAuthors(packageManifest.authors());
        packageJson.setSourceRepository(packageManifest.repository());
        packageJson.setKeywords(packageManifest.keywords());
        packageJson.setExport(packageManifest.exportedModules());
        packageJson.setInclude(packageManifest.includes());
        packageJson.setVisibility(packageManifest.visibility());
        packageJson.setTemplate(packageManifest.template());

        packageJson.setPlatform(target);
        packageJson.setBallerinaVersion(BALLERINA_SHORT_VERSION);
        packageJson.setLanguageSpecVersion(BALLERINA_SPEC_VERSION);
        packageJson.setImplementationVendor(IMPLEMENTATION_VENDOR);

        if (!platformLibs.isEmpty()) {
            packageJson.setPlatformDependencies(platformLibs.get());
        }

        // Set icon in bala path in the package.json
        if (packageManifest.icon() != null && !packageManifest.icon().isEmpty()) {
            Path iconPath = getIconPath(packageManifest.icon());
            packageJson.setIcon(String.valueOf(Paths.get(BALA_DOCS_DIR).resolve(iconPath.getFileName())));
        }
        // Set graalvmCompatibility property in package.json
        setGraalVMCompatibilityProperty(packageJson, packageManifest);

        // Remove fields with empty values from `package.json`
        Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(Collection.class, new JsonCollectionsAdaptor())
                .registerTypeHierarchyAdapter(String.class, new JsonStringsAdaptor()).setPrettyPrinting().create();

        try {
            putZipEntry(balaOutputStream, Paths.get(PACKAGE_JSON),
                    new ByteArrayInputStream(gson.toJson(packageJson).getBytes(Charset.defaultCharset())));
        } catch (IOException e) {
            throw new ProjectException("Failed to write 'package.json' file: " + e.getMessage(), e);
        }
    }

    private void setGraalVMCompatibilityProperty(PackageJson packageJson, PackageManifest packageManifest) {
        Map<String, PackageManifest.Platform> platforms = packageManifest.platforms();
        PackageManifest.Platform targetPlatform = packageManifest.platform(target);
        if (platforms != null) {
            if (targetPlatform != null) {
                Boolean graalvmCompatible = targetPlatform.graalvmCompatible();
                if (graalvmCompatible != null) {
                    // If the package explicitly specifies the graalvmCompatibility property, then use it
                    packageJson.setGraalvmCompatible(graalvmCompatible);
                    return;
                }
            }
            if (!otherPlatformGraalvmCompatibleVerified(target, packageManifest.platforms()).equals("")) {
                Boolean otherGraalvmCompatible = packageManifest.platform(otherPlatformGraalvmCompatibleVerified(target,
                        packageManifest.platforms())).graalvmCompatible();
                packageJson.setGraalvmCompatible(otherGraalvmCompatible);
            } else if (!hasExternalPlatformDependencies(packageManifest.platforms())) {
                // If the package uses only distribution provided platform libraries, then package is graalvm compatible
                packageJson.setGraalvmCompatible(true);
            }
        } else {
            // If the package uses only distribution provided platform libraries
            // or has only ballerina dependencies, then the package is graalvm compatible
            packageJson.setGraalvmCompatible(true);
        }
    }

    private String otherPlatformGraalvmCompatibleVerified(String target,
                                                          Map<String, PackageManifest.Platform> platforms) {
        for (Map.Entry<String, PackageManifest.Platform> platform : platforms.entrySet()) {
            if (!platform.getKey().equals(target) && platform.getValue().graalvmCompatible() != null) {
                return platform.getKey();
            }
        }
        return "";
    }

    private boolean hasExternalPlatformDependencies(Map<String, PackageManifest.Platform> platforms) {
        // Check if external platform dependencies are defined
        for (PackageManifest.Platform platformVal: platforms.values()) {
            if (!platformVal.dependencies().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    // TODO when iterating and adding source files should create source files from Package sources

    private void addPackageDoc(ZipOutputStream balaOutputStream, Path packageSourceDir, String pkgName)
            throws IOException {
        final String packageMdFileName = "Package.md";
        final String moduleMdFileName = "Module.md";
        final String readmeMdFileName = "README.md";

        Path docsDirInBala = Paths.get(BALA_DOCS_DIR);
        Path packageMd = packageSourceDir.resolve(packageMdFileName);

        // If `icon` mentioned in the Ballerina.toml, add it to docs directory
        String icon = this.packageContext.packageManifest().icon();
        if (icon != null && !icon.isEmpty()) {
            Path iconPath = getIconPath(icon);
            Path iconInBala = docsDirInBala.resolve(iconPath.getFileName());
            try (FileInputStream inputStream = new FileInputStream(String.valueOf(iconPath))) {
                putZipEntry(balaOutputStream, iconInBala, inputStream);
            }
        }

        // If Package.md and Module.md does not exist, pack README.md
        if (!packageMd.toFile().exists()) {
            packModulesToBala(pkgName, readmeMdFileName, balaOutputStream, packageSourceDir);
        } else {
            // Creates the docs directory & add `Package.md`
            Path packageMdInBala = docsDirInBala.resolve(packageMdFileName);
            try (FileInputStream inputStream = new FileInputStream(String.valueOf(packageMd))) {
                putZipEntry(balaOutputStream, packageMdInBala, inputStream);
            }

            // Packs the module.md of default and non-default modules
            packModulesToBala(pkgName, moduleMdFileName, balaOutputStream, packageSourceDir);
        }
    }

    private void packModulesToBala(String pkgName, String fileName, ZipOutputStream balaOutputStream,
                                   Path packageSourceDir)
            throws IOException {

        Path defaultMd = packageSourceDir.resolve(fileName);
        File modulesSourceDir = new File(String.valueOf(packageSourceDir.resolve(MODULES_ROOT)));
        Path modulesDirInBalaDocs = Paths.get(BALA_DOCS_DIR).resolve(MODULES_ROOT);

        // Packs default Module
        if (defaultMd.toFile().exists()) {
            Path defaultMdInBala = modulesDirInBalaDocs.resolve(pkgName).resolve(fileName);
            try (FileInputStream inputStream = new FileInputStream(String.valueOf(defaultMd))) {
                putZipEntry(balaOutputStream, defaultMdInBala, inputStream);
            }
        }
        // Packs non-default modules
        File[] directoryListing = modulesSourceDir.listFiles();
        if (directoryListing != null) {
            for (File moduleDir : directoryListing) {
                if (moduleDir.isDirectory()) {
                    // Gets filename path
                    Path nonDefaultModuleMd = packageSourceDir.resolve(MODULES_ROOT).resolve(moduleDir.getName())
                            .resolve(fileName);
                    // Creates `package.module` folder, if filename path exists
                    if (nonDefaultModuleMd.toFile().exists()) {
                        Path nonDefaultModuleMdInBalaDocs = modulesDirInBalaDocs
                                .resolve(pkgName + "." + moduleDir.getName()).resolve(fileName);
                        try (FileInputStream inputStream = new FileInputStream(String.valueOf(nonDefaultModuleMd))) {
                            putZipEntry(balaOutputStream, nonDefaultModuleMdInBalaDocs, inputStream);
                        }
                    }
                }
            }
        }
    }

    private void addPackageSource(ZipOutputStream balaOutputStream) throws IOException {
        // add module sources
        for (ModuleId moduleId : this.packageContext.moduleIds()) {
            Module module = this.packageContext.project().currentPackage().module(moduleId);

            // copy resources
            for (DocumentId documentId : module.resourceIds()) {
                Resource resource = module.resource(documentId);
                Path resourcePath = Paths.get(ProjectConstants.MODULES_ROOT).resolve(module.moduleName().toString())
                        .resolve(RESOURCE_DIR_NAME).resolve(resource.name());
                putZipEntry(balaOutputStream, resourcePath, new ByteArrayInputStream(resource.content()));
            }

            // Generate empty bal file for default module in tools
            if (module.isDefaultModule() && !packageContext.balToolTomlContext().isEmpty() &&
                    module.documentIds().isEmpty()) {
                String emptyBalContent = "// AUTO-GENERATED FILE.\n" +
                        "\n" +
                        "// This file is auto-generated by Ballerina for packages with empty default modules. \n";

                TextDocument emptyBalTextDocument = TextDocuments.from(emptyBalContent);
                DocumentId documentId = DocumentId.create(MAIN_BAL, moduleId);
                DocumentConfig documentConfig = DocumentConfig.from(documentId, emptyBalTextDocument.toString(),
                        MAIN_BAL);
                module = module.modify().addDocument(documentConfig).apply();
            }

            // only add .bal files of module
            for (DocumentId docId : module.documentIds()) {
                Document document = module.document(docId);
                if (document.name().endsWith(BLANG_SOURCE_EXT)) {
                    Path documentPath = Paths.get(MODULES_ROOT, module.moduleName().toString(), document.name());
                    char[] documentContent = document.textDocument().toCharArray();

                    putZipEntry(balaOutputStream, documentPath,
                            new ByteArrayInputStream(new String(documentContent).getBytes(StandardCharsets.UTF_8)));
                }
            }
        }
    }

    private void addIncludes(ZipOutputStream balaOutputStream) throws IOException {
        List<String> includePatterns = this.packageContext.packageManifest().includes();
        List<Path> includePaths = ProjectUtils.getPathsMatchingIncludePatterns(
                includePatterns, this.packageContext.project().sourceRoot());

        for (Path includePath: includePaths) {
            Path includePathInPackage = this.packageContext.project().sourceRoot().resolve(includePath)
                    .toAbsolutePath();
            Path includeInBala = updateModuleDirectoryToMatchNamingInBala(includePath);
            try {
                if (includePathInPackage.toFile().isDirectory()) {
                    putDirectoryToZipFile(includePathInPackage, includeInBala, balaOutputStream);
                } else {
                    putZipEntry(balaOutputStream, includeInBala,
                            new FileInputStream(String.valueOf(includePathInPackage)));
                }
            } catch (ZipException e) {
                if (!e.getMessage().contains("duplicate entry")) {
                    throw e;
                }
            }
        }
    }

    private void addDependenciesJson(ZipOutputStream balaOutputStream) {
        PackageCache packageCache = this.packageContext.project().projectEnvironmentContext()
                .getService(PackageCache.class);
        List<Dependency> packageDependencyGraph = getPackageDependencies(
                this.packageContext.getResolution().dependencyGraph());
        List<ModuleDependency> moduleDependencyGraph = getModuleDependencies(
                this.packageContext.project().currentPackage(), packageCache);

        DependencyGraphJson depGraphJson = new DependencyGraphJson(packageDependencyGraph, moduleDependencyGraph);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            putZipEntry(balaOutputStream, Paths.get(DEPENDENCY_GRAPH_JSON),
                    new ByteArrayInputStream(gson.toJson(depGraphJson).getBytes(Charset.defaultCharset())));
        } catch (IOException e) {
            throw new ProjectException("Failed to write '" + DEPENDENCY_GRAPH_JSON + "' file: " + e.getMessage(), e);
        }
    }

    private Path updateModuleDirectoryToMatchNamingInBala(Path relativePath) {
        // a project with non-default module dir modules/<submodule_name> when packed into a BALA has the structure
        // modules/<package_name>.<submodule_name>
        Path moduleRootPath = Path.of(MODULES_ROOT);
        if (relativePath.startsWith(moduleRootPath)) {
            String packageName = this.packageContext.packageName().toString();
            Path modulePath = moduleRootPath.resolve(moduleRootPath.relativize(relativePath).subpath(0, 1));
            Path pathInsideModule = modulePath.relativize(relativePath);
            String moduleName = Optional.ofNullable(modulePath.getFileName()).orElse(Paths.get("")).toString();
            String updatedModuleName = packageName + ProjectConstants.DOT + moduleName;
            Path updatedModulePath = moduleRootPath.resolve(updatedModuleName);
            return updatedModulePath.resolve(pathInsideModule);
        }
        return relativePath;
    }

    private List<Dependency> getPackageDependencies(DependencyGraph<ResolvedPackageDependency> dependencyGraph) {
        List<Dependency> dependencies = new ArrayList<>();
        for (ResolvedPackageDependency resolvedDep : dependencyGraph.getNodes()) {
            if (resolvedDep.scope() == PackageDependencyScope.TEST_ONLY) {
                // We don't add the test dependencies to the bala file.
                continue;
            }

            PackageContext packageContext = resolvedDep.packageInstance().packageContext();
            Dependency dependency = new Dependency(packageContext.packageOrg().toString(),
                    packageContext.packageName().toString(), packageContext.packageVersion().toString());

            List<Dependency> dependencyList = new ArrayList<>();
            Collection<ResolvedPackageDependency> pkgDependencies = dependencyGraph.getDirectDependencies(resolvedDep);
            for (ResolvedPackageDependency resolvedTransitiveDep : pkgDependencies) {
                if (resolvedTransitiveDep.scope() == PackageDependencyScope.TEST_ONLY) {
                    // We don't add the test dependencies to the bala file.
                    continue;
                }
                PackageContext dependencyPkgContext = resolvedTransitiveDep.packageInstance().packageContext();
                Dependency dep = new Dependency(dependencyPkgContext.packageOrg().toString(),
                        dependencyPkgContext.packageName().toString(),
                        dependencyPkgContext.packageVersion().toString());
                dependencyList.add(dep);
            }
            dependency.setDependencies(dependencyList);
            dependencies.add(dependency);
        }
        return dependencies;
    }

    private List<ModuleDependency> getModuleDependencies(Package pkg, PackageCache packageCache) {
        List<ModuleDependency> modules = new ArrayList<>();
        for (ModuleId moduleId : pkg.moduleIds()) {
            Module module = pkg.module(moduleId);
            List<ModuleDependency> moduleDependencies = new ArrayList<>();
            for (io.ballerina.projects.ModuleDependency moduleDependency : module.moduleDependencies()) {
                if (moduleDependency.packageDependency().scope() == PackageDependencyScope.TEST_ONLY) {
                    // Do not test_only scope dependencies
                    continue;
                }
                Package pkgDependency = packageCache.getPackageOrThrow(
                        moduleDependency.packageDependency().packageId());
                Module moduleInPkgDependency = pkgDependency.module(moduleDependency.descriptor().name());
                moduleDependencies.add(createModuleDependencyEntry(pkgDependency, moduleInPkgDependency,
                        Collections.emptyList()));
            }
            modules.add(createModuleDependencyEntry(pkg, module, moduleDependencies));
        }
        return modules;
    }

    private ModuleDependency createModuleDependencyEntry(Package pkg,
                                                         Module module,
                                                         List<ModuleDependency> moduleDependencies) {
        return new ModuleDependency(pkg.packageOrg().value(), pkg.packageName().value(),
                pkg.packageVersion().toString(), module.moduleName().toString(), moduleDependencies);
    }

    private Path getIconPath(String icon) {
        Path iconPath = Paths.get(icon);
        if (!iconPath.isAbsolute()) {
            iconPath = this.packageContext.project().sourceRoot().resolve(iconPath);
        }
        return iconPath;
    }

    protected void putZipEntry(ZipOutputStream balaOutputStream, Path fileName, InputStream in)
            throws IOException {
        ZipEntry entry = new ZipEntry(convertPathSeperator(fileName));
        balaOutputStream.putNextEntry(entry);

        IOUtils.copy(in, balaOutputStream);
        IOUtils.closeQuietly(in);
    }

    protected void putDirectoryToZipFile(Path sourceDir, Path pathInZipFile, ZipOutputStream out)
            throws IOException {
        if (sourceDir.toFile().exists()) {
            File[] files = new File(sourceDir.toString()).listFiles();

            if (files != null && files.length > 0) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        putDirectoryToZipFile(sourceDir.resolve(file.getName()), pathInZipFile, out);
                    } else {
                        Path fileNameInBala =
                                pathInZipFile.resolve(sourceDir.relativize(Paths.get(file.getPath())));
                        putZipEntry(out, fileNameInBala,
                                new FileInputStream(sourceDir + File.separator + file.getName()));
                    }
                }
            }
        }
    }

    protected abstract Optional<JsonArray> addPlatformLibs(ZipOutputStream balaOutputStream)
            throws IOException;

    protected abstract void addCompilerPlugin(ZipOutputStream balaOutputStream) throws IOException;

    protected abstract void addBalTool(ZipOutputStream balaOutputStream) throws IOException;

    // Following function was put in to handle a bug in windows zipFileSystem
    // Refer https://bugs.openjdk.java.net/browse/JDK-8195141
    private String convertPathSeperator(Path file) {
        if (file == null) {
            return null;
        } else {
            if (File.separatorChar == '\\') {
                String replaced = "";
                // Following is to evade spotbug issue if file is null
                replaced = Optional.ofNullable(file.getFileName()).orElse(Paths.get("")).toString();
                Path parent = file.getParent();
                while (parent != null) {
                    replaced = parent.getFileName() + "/" + replaced;
                    parent = parent.getParent();
                }
                return replaced;
            }
            return file.toString();
        }
    }
}

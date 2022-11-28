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
import io.ballerina.projects.internal.model.CompilerPluginDescriptor;
import io.ballerina.projects.internal.model.Dependency;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.projects.util.ProjectUtils;
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

    // Set the target as any for default bala.
    protected String target = "any";
    private static final String IMPLEMENTATION_VENDOR = "WSO2";
    private static final String BALLERINA_SHORT_VERSION = RepoUtils.getBallerinaShortVersion();
    private static final String BALLERINA_SPEC_VERSION = RepoUtils.getBallerinaSpecVersion();
    protected PackageContext packageContext;
    Optional<CompilerPluginDescriptor> compilerPluginToml;

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

    // TODO when iterating and adding source files should create source files from Package sources

    private void addPackageDoc(ZipOutputStream balaOutputStream, Path packageSourceDir, String pkgName)
            throws IOException {
        final String packageMdFileName = "Package.md";
        final String moduleMdFileName = "Module.md";

        Path packageMd = packageSourceDir.resolve(packageMdFileName);
        Path docsDirInBala = Paths.get(BALA_DOCS_DIR);

        // If `Package.md` exists, create the docs directory & add `Package.md`
        if (packageMd.toFile().exists()) {
            Path packageMdInBala = docsDirInBala.resolve(packageMdFileName);
            putZipEntry(balaOutputStream, packageMdInBala,
                    new FileInputStream(String.valueOf(packageMd)));
        }

        // If `icon` mentioned in the Ballerina.toml, add it to docs directory
        String icon = this.packageContext.packageManifest().icon();
        if (icon != null && !icon.isEmpty()) {
            Path iconPath = getIconPath(icon);
            Path iconInBala = docsDirInBala.resolve(iconPath.getFileName());
            putZipEntry(balaOutputStream, iconInBala, new FileInputStream(String.valueOf(iconPath)));
        }

        // If `Module.md` of default module exists, create `docs/modules` directory & add `Module.md`
        Path defaultModuleMd = packageSourceDir.resolve(moduleMdFileName);
        Path modulesDirInBalaDocs = docsDirInBala.resolve(MODULES_ROOT);

        if (defaultModuleMd.toFile().exists()) {
            Path defaultModuleMdInBalaDocs = modulesDirInBalaDocs.resolve(pkgName).resolve(moduleMdFileName);
            putZipEntry(balaOutputStream, defaultModuleMdInBalaDocs,
                    new FileInputStream(String.valueOf(defaultModuleMd)));
        }

        // Add other module docs
        File modulesSourceDir = new File(String.valueOf(packageSourceDir.resolve(MODULES_ROOT)));
        File[] directoryListing = modulesSourceDir.listFiles();

        if (directoryListing != null) {
            for (File moduleDir : directoryListing) {
                if (moduleDir.isDirectory()) {
                    // Get `Module.md` path
                    Path otherModuleMd = packageSourceDir.resolve(MODULES_ROOT).resolve(moduleDir.getName())
                            .resolve(moduleMdFileName);
                    // Create `package.module` folder, if `Module.md` path exists
                    if (otherModuleMd.toFile().exists()) {
                        Path otherModuleMdInBalaDocs = modulesDirInBalaDocs.resolve(pkgName + "." + moduleDir.getName())
                                .resolve(moduleMdFileName);
                        putZipEntry(balaOutputStream, otherModuleMdInBalaDocs,
                                new FileInputStream(String.valueOf(otherModuleMd)));
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

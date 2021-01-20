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
import io.ballerina.projects.internal.balo.BaloJson;
import io.ballerina.projects.internal.balo.DependencyGraphJson;
import io.ballerina.projects.internal.balo.ModuleDependency;
import io.ballerina.projects.internal.balo.PackageJson;
import io.ballerina.projects.internal.balo.adaptors.JsonCollectionsAdaptor;
import io.ballerina.projects.internal.balo.adaptors.JsonStringsAdaptor;
import io.ballerina.projects.internal.model.Dependency;
import org.apache.commons.compress.utils.IOUtils;
import org.ballerinalang.compiler.BLangCompilerException;

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
import java.util.zip.ZipOutputStream;

import static io.ballerina.projects.util.ProjectConstants.BALO_JSON;
import static io.ballerina.projects.util.ProjectConstants.DEPENDENCY_GRAPH_JSON;
import static io.ballerina.projects.util.ProjectConstants.PACKAGE_JSON;
import static io.ballerina.projects.util.ProjectUtils.getBaloName;

/**
 * {@code BaloWriter} writes a package to balo format.
 *
 * @since 2.0.0
 */
public abstract class BaloWriter {
    private static final String MODULES_ROOT = "modules";
    private static final String RESOURCE_DIR_NAME = "resources";
    private static final String BLANG_SOURCE_EXT = ".bal";
    protected static final String PLATFORM = "platform";
    protected static final String DEPENDENCY = "dependency";
    protected static final String PATH = "path";

    // Set the target as any for default balo.
    protected String target = "any";
    protected String langSpecVersion = "2020r2";
    protected String ballerinaVersion = "Ballerina 2.0.0";
    protected String implemetationVendor = "WSO2";
    protected PackageContext packageContext;

    protected BaloWriter() {
    }

    /**
     * Write a package to a .balo and return the created .balo path.
     *
     * @param baloPath Directory where the .balo should be created.
     */
    public Path write(Path baloPath) {
        String baloName = getBaloName(this.packageContext.packageOrg().value(),
                                      this.packageContext.packageName().value(),
                                      this.packageContext.packageVersion().value().toString(),
                                      this.target);
        // Create the archive over write if exists
        try (ZipOutputStream baloOutputStream = new ZipOutputStream(
                new FileOutputStream(String.valueOf(baloPath.resolve(baloName))))) {
            // Now lets put stuff in
            populateBaloArchive(baloOutputStream);
        } catch (IOException e) {
            throw new ProjectException("Failed to create balo :" + e.getMessage(), e);
        } catch (BLangCompilerException be) {
            // clean up if an error occur
            try {
                Files.delete(baloPath);
            } catch (IOException e) {
                // We ignore this error and throw out the original blang compiler error to the user
            }
            throw be;
        }
        return baloPath.resolve(baloName);
    }

    private void populateBaloArchive(ZipOutputStream baloOutputStream)
            throws IOException {

        addBaloJson(baloOutputStream);
        addPackageDoc(baloOutputStream,
                      this.packageContext.project().sourceRoot(),
                      this.packageContext.packageName().toString());
        addPackageSource(baloOutputStream);
        Optional<JsonArray> platformLibs = addPlatformLibs(baloOutputStream);
        addPackageJson(baloOutputStream, platformLibs);
        addDependenciesJson(baloOutputStream);
    }

    private void addBaloJson(ZipOutputStream baloOutputStream) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String baloJson = gson.toJson(new BaloJson());
        try {
            putZipEntry(baloOutputStream, Paths.get(BALO_JSON),
                    new ByteArrayInputStream(baloJson.getBytes(Charset.defaultCharset())));
        } catch (IOException e) {
            throw new ProjectException("Failed to write 'balo.json' file: " + e.getMessage(), e);
        }
    }

    private void addPackageJson(ZipOutputStream baloOutputStream, Optional<JsonArray> platformLibs) {
        PackageJson packageJson = new PackageJson(this.packageContext.packageOrg().toString(),
                                                  this.packageContext.packageName().toString(),
                                                  this.packageContext.packageVersion().toString());

        PackageManifest packageManifest = this.packageContext.manifest();
        packageJson.setLicenses(packageManifest.license());
        packageJson.setAuthors(packageManifest.authors());
        packageJson.setSourceRepository(packageManifest.repository());
        packageJson.setKeywords(packageManifest.keywords());

        packageJson.setPlatform(target);
        packageJson.setLanguageSpecVersion(langSpecVersion);
        packageJson.setBallerinaVersion(ballerinaVersion);
        packageJson.setImplementationVendor(implemetationVendor);

        if (!platformLibs.isEmpty()) {
            packageJson.setPlatformDependencies(platformLibs.get());
        }
        // Remove fields with empty values from `package.json`
        Gson gson = new GsonBuilder().registerTypeHierarchyAdapter(Collection.class, new JsonCollectionsAdaptor())
                .registerTypeHierarchyAdapter(String.class, new JsonStringsAdaptor()).setPrettyPrinting().create();

        try {
            putZipEntry(baloOutputStream, Paths.get(PACKAGE_JSON),
                    new ByteArrayInputStream(gson.toJson(packageJson).getBytes(Charset.defaultCharset())));
        } catch (IOException e) {
            throw new ProjectException("Failed to write 'package.json' file: " + e.getMessage(), e);
        }
    }

    // TODO when iterating and adding source files should create source files from Package sources

    private void addPackageDoc(ZipOutputStream baloOutputStream, Path packageSourceDir, String pkgName)
            throws IOException {
        final String packageMdFileName = "Package.md";
        final String moduleMdFileName = "Module.md";

        Path packageMd = packageSourceDir.resolve(packageMdFileName);
        Path docsDirInBalo = Paths.get("docs");

        // If `Package.md` exists, create the docs directory & add `Package.md`
        if (packageMd.toFile().exists()) {
            Path packageMdInBalo = docsDirInBalo.resolve(packageMdFileName);
            putZipEntry(baloOutputStream, packageMdInBalo,
                    new FileInputStream(String.valueOf(packageMd)));
        }

        // If `Module.md` of default module exists, create `docs/modules` directory & add `Module.md`
        Path defaultModuleMd = packageSourceDir.resolve(moduleMdFileName);
        Path modulesDirInBaloDocs = docsDirInBalo.resolve(MODULES_ROOT);

        if (defaultModuleMd.toFile().exists()) {
            Path defaultModuleMdInBaloDocs = modulesDirInBaloDocs.resolve(pkgName).resolve(moduleMdFileName);
            putZipEntry(baloOutputStream, defaultModuleMdInBaloDocs,
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
                        Path otherModuleMdInBaloDocs = modulesDirInBaloDocs.resolve(pkgName + "." + moduleDir.getName())
                                .resolve(moduleMdFileName);
                        putZipEntry(baloOutputStream, otherModuleMdInBaloDocs,
                                new FileInputStream(String.valueOf(otherModuleMd)));
                    }
                }
            }
        }
    }

    private void addPackageSource(ZipOutputStream baloOutputStream) throws IOException {

        // add module sources
        for (ModuleId moduleId : this.packageContext.moduleIds()) {
            Module module = this.packageContext.project().currentPackage().module(moduleId);

            // copy resources directory
            Path moduleRoot = this.packageContext.project().sourceRoot();
            if (module.moduleName() != this.packageContext.project().currentPackage().getDefaultModule().moduleName()) {
                moduleRoot = moduleRoot.resolve(MODULES_ROOT).resolve(module.moduleName().moduleNamePart());
            }
            Path resourcesPathInBalo = Paths.get(MODULES_ROOT, module.moduleName().toString(), RESOURCE_DIR_NAME);
            putDirectoryToZipFile(moduleRoot.resolve(RESOURCE_DIR_NAME), resourcesPathInBalo, baloOutputStream);

            // only add .bal files of module
            for (DocumentId docId : module.documentIds()) {
                Document document = module.document(docId);
                if (document.name().endsWith(BLANG_SOURCE_EXT)) {
                    Path documentPath = Paths.get(MODULES_ROOT, module.moduleName().toString(), document.name());
                    char[] documentContent = document.textDocument().toCharArray();

                    putZipEntry(baloOutputStream, documentPath,
                                new ByteArrayInputStream(new String(documentContent).getBytes(StandardCharsets.UTF_8)));
                }
            }
        }
    }

    private void addDependenciesJson(ZipOutputStream baloOutputStream) {
        PackageCache packageCache = this.packageContext.project().projectEnvironmentContext()
                .getService(PackageCache.class);
        List<Dependency> packageDependencyGraph = getPackageDependencies(
                this.packageContext.getResolution().dependencyGraph());
        List<ModuleDependency> moduleDependencyGraph = getModuleDependencies(
                this.packageContext.project().currentPackage(), packageCache);

        DependencyGraphJson depGraphJson = new DependencyGraphJson(packageDependencyGraph, moduleDependencyGraph);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            putZipEntry(baloOutputStream, Paths.get(DEPENDENCY_GRAPH_JSON),
                        new ByteArrayInputStream(gson.toJson(depGraphJson).getBytes(Charset.defaultCharset())));
        } catch (IOException e) {
            throw new ProjectException("Failed to write '" + DEPENDENCY_GRAPH_JSON + "' file: " + e.getMessage(), e);
        }
    }

    private List<Dependency> getPackageDependencies(DependencyGraph<ResolvedPackageDependency> dependencyGraph) {
        List<Dependency> dependencies = new ArrayList<>();
        for (ResolvedPackageDependency resolvedDep : dependencyGraph.getNodes()) {
            if (resolvedDep.scope() == PackageDependencyScope.TEST_ONLY) {
                // We don't add the test dependencies to the balr file.
                continue;
            }

            PackageContext packageContext = resolvedDep.packageInstance().packageContext();
            Dependency dependency = new Dependency(packageContext.packageOrg().toString(),
                    packageContext.packageName().toString(), packageContext.packageVersion().toString());

            List<Dependency> dependencyList = new ArrayList<>();
            Collection<ResolvedPackageDependency> pkgDependencies = dependencyGraph.getDirectDependencies(resolvedDep);
            for (ResolvedPackageDependency resolvedTransitiveDep : pkgDependencies) {
                if (resolvedTransitiveDep.scope() == PackageDependencyScope.TEST_ONLY) {
                    // We don't add the test dependencies to the balr file.
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
                Module moduleInPkgDependency = pkgDependency.module(moduleDependency.moduleId());
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

    protected void putZipEntry(ZipOutputStream baloOutputStream, Path fileName, InputStream in)
            throws IOException {
        ZipEntry entry = new ZipEntry(convertPathSeperator(fileName));
        baloOutputStream.putNextEntry(entry);

        IOUtils.copy(in, baloOutputStream);
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
                        Path fileNameInBalo =
                                pathInZipFile.resolve(sourceDir.relativize(Paths.get(file.getPath())));
                        putZipEntry(out, fileNameInBalo,
                                new FileInputStream(sourceDir + File.separator + file.getName()));
                    }
                }
            }
        }
    }

    protected abstract Optional<JsonArray> addPlatformLibs(ZipOutputStream baloOutputStream)
            throws IOException;

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

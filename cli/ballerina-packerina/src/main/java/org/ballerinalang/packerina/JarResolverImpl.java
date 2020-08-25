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
package org.ballerinalang.packerina;

import com.moandjiezana.toml.Toml;
import org.ballerinalang.compiler.JarResolver;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SourceType;
import org.ballerinalang.packerina.model.DependencyJar;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.Library;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.wso2.ballerinalang.compiler.SourceDirectoryManager;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.programfile.ProgramFileConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALO_PLATFORM_LIB_DIR_NAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_PKG_DEFAULT_VERSION;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.DIST_BIR_CACHE_DIR_NAME;

/**
 * Resolves jars and native libs for modules.
 *
 * @since 2.0.0
 */
public class JarResolverImpl implements JarResolver {

    private List<String> supportedPlatforms = Arrays.stream(ProgramFileConstants.SUPPORTED_PLATFORMS)
            .collect(Collectors.toList());
    private final BuildContext buildContext;
    private final SourceDirectoryManager sourceDirectoryManager;
    private final Path sourceRootPath;
    private final String balHomePath;
    private final Manifest manifest;
    private boolean skipCopyLibsFromDist;
    private boolean isObservabilitySkipped;

    public static JarResolver getInstance(BuildContext buildContext, boolean skipCopyLibsFromDist, boolean
            observabilityIncluded) {
        CompilerContext context = buildContext.get(BuildContextField.COMPILER_CONTEXT);
        JarResolver jarResolver = context.get(JAR_RESOLVER_KEY);
        if (jarResolver == null) {
            jarResolver = new JarResolverImpl(buildContext, skipCopyLibsFromDist, observabilityIncluded);
        }
        context.put(JAR_RESOLVER_KEY, jarResolver);
        return jarResolver;
    }

    private JarResolverImpl(BuildContext buildContext, boolean skipCopyLibsFromDist, boolean observabilityIncluded) {
        CompilerContext context = buildContext.get(BuildContextField.COMPILER_CONTEXT);
        this.buildContext = buildContext;
        this.sourceDirectoryManager = SourceDirectoryManager.getInstance(context);
        this.manifest = ManifestProcessor.getInstance(context).getManifest();
        this.sourceRootPath = buildContext.get(BuildContextField.SOURCE_ROOT);
        this.skipCopyLibsFromDist = skipCopyLibsFromDist;
        this.isObservabilitySkipped = !(observabilityIncluded ||
                (manifest.getBuildOptions() != null && manifest.getBuildOptions().isObservabilityIncluded()));
        this.balHomePath = buildContext.get(BuildContextField.HOME_REPO).toString();
        supportedPlatforms.add(ProgramFileConstants.ANY_PLATFORM);
    }

    @Override
    public Path moduleJar(PackageID packageID) {
        if (packageID.toString().equals(".") || isProjectModule(packageID) || isPathDependency(packageID)) {
            // If so fetch from project jar cache
            return buildContext.getJarPathFromTargetCache(packageID);
        } else if (isModuleInDistribution(packageID)) {
            // If so fetch from distribution
            return getJarFromDistribution(packageID);
        } else {
            // If not fetch from home jar cache.
            return buildContext.getJarPathFromHomeCache(packageID);
        }
    }

    @Override
    public Path moduleTestJar(BLangPackage bLangPackage) {
        if (bLangPackage.containsTestablePkg()) {
            return buildContext.getTestJarPathFromTargetCache(bLangPackage.packageID);
        } else {
            // Single bal file test code will be in module jar
            return buildContext.getJarPathFromTargetCache(bLangPackage.packageID);
        }
    }

    @Override
    public HashSet<Path> nativeDependencies(PackageID packageID) {
        HashSet<Path> modulePlatformLibs = new HashSet<>();
        // copy platform libs for all modules(imported modules as well)
        addPlatformLibs(packageID, modulePlatformLibs);
        Path runtimeJar = getRuntimeJar();
        if (runtimeJar != null) {
            modulePlatformLibs.add(getRuntimeJar());
        }

        if (isProjectModule(packageID)) {
            return modulePlatformLibs;
        } else if (isPathDependency(packageID)) {
            addLibsFromBaloDependency(packageID, modulePlatformLibs);
        } else if (isModuleInDistribution(packageID)) {
            addLibsFromDistribution(packageID, modulePlatformLibs);
        } else {
            addLibsFromHomeBaloCache(packageID, modulePlatformLibs);
        }
        return modulePlatformLibs;
    }

    @Override
    public HashSet<Path> nativeDependenciesForTests(PackageID packageID) {
        HashSet<Path> testPlatformLibs = new HashSet<>(nativeDependencies(packageID));
        List<Library> libraries = manifest.getPlatform().libraries;
        if (libraries != null) {
            for (Library library : libraries) {
                if ((library.getModules() == null ||
                        Arrays.asList(library.getModules()).contains(packageID.name.value)) &&
                        (library.getScope() != null && library.getScope().equalsIgnoreCase("testOnly"))) {
                    String libFilePath = library.getPath();
                    if (libFilePath == null) {
                        continue;
                    }
                    Path nativeFile = sourceRootPath.resolve(Paths.get(libFilePath));
                    testPlatformLibs.add(nativeFile);
                }
            }
        }
        return testPlatformLibs;
    }

    @Override
    public HashSet<Path> allDependencies(BLangPackage bLangPackage) {
        HashSet<PackageID> alreadyImportedSet = new HashSet<>();
        PackageID pkgId = bLangPackage.packageID;
        HashSet<Path> modulePlatformLibs = new HashSet<>(nativeDependencies(pkgId));
        copyImportedLibs(pkgId, bLangPackage.symbol.imports, modulePlatformLibs, alreadyImportedSet);
        // copy choreo extension if --observability-included flag is added
        if (!isObservabilitySkipped) {
            modulePlatformLibs.add(getChoreoRuntimeJar());
        }
        return modulePlatformLibs;
    }

    @Override
    public HashSet<Path> allTestDependencies(BLangPackage bLangPackage) {
        HashSet<PackageID> alreadyImportedSet = new HashSet<>();
        PackageID pkgId = bLangPackage.packageID;
        Path runtimeJar = getRuntimeJar();
        HashSet<Path> moduleTestLibs = new HashSet<>(nativeDependenciesForTests(pkgId));
        moduleTestLibs.addAll(allDependencies(bLangPackage));
        moduleTestLibs.add(moduleTestJar(bLangPackage));
        if (runtimeJar != null) {
            moduleTestLibs.add(getRuntimeJar());
        }
        // Copy native libs imported by testable package
        for (BLangPackage testPkg : bLangPackage.getTestablePkgs()) {
            copyImportedLibs(testPkg.packageID, testPkg.symbol.imports, moduleTestLibs, alreadyImportedSet);
        }

        if (buildContext.getSourceType() != SourceType.SINGLE_BAL_FILE) {
            moduleTestLibs.remove(buildContext.getJarPathFromTargetCache(pkgId));
        }
        return moduleTestLibs;
    }

    public Path getRuntimeJar() {
        if (skipCopyLibsFromDist) {
            return null;
        }
        String ballerinaVersion = RepoUtils.getBallerinaPackVersion();
        String runtimeJarName = "ballerina-rt-" + ballerinaVersion + BLANG_COMPILED_JAR_EXT;
        return Paths.get(balHomePath, "bre", "lib", runtimeJarName);
    }

    private void copyImportedLibs(PackageID packageID, List<BPackageSymbol> imports, HashSet<Path> moduleDependencySet,
                                  HashSet<PackageID> alreadyImportedSet) {
        Path moduleJar = moduleJar(packageID);
        if (moduleJar != null) {
            moduleDependencySet.add(moduleJar);
        }

        for (BPackageSymbol importSymbol : imports) {
            PackageID pkgId = importSymbol.pkgID;
            if (!alreadyImportedSet.contains(pkgId)) {
                alreadyImportedSet.add(pkgId);
                moduleDependencySet.addAll(nativeDependencies(pkgId));
                copyImportedLibs(importSymbol.pkgID, importSymbol.imports, moduleDependencySet, alreadyImportedSet);
            }
        }
    }

    private boolean isModuleInDistribution(PackageID packageID) {
        return getTomlFilePath(packageID).exists();
    }

    private File getTomlFilePath(PackageID packageID) {
        String version = BLANG_PKG_DEFAULT_VERSION;
        if (!packageID.version.value.equals("")) {
            version = packageID.version.value;
        }

        return Paths.get(balHomePath, DIST_BIR_CACHE_DIR_NAME, packageID.orgName.value,
                packageID.name.value, version, "Ballerina.toml").toFile();
    }

    private boolean isPathDependency(PackageID packageID) {
        return buildContext.getImportPathDependency(packageID).isPresent();
    }

    private boolean isProjectModule(PackageID packageID) {
        return manifest.getProject().getOrgName().equals(packageID.orgName.value) &&
                ProjectDirs.isModuleExist(sourceRootPath, packageID.name.value);
    }

    private Path getBaloPathFromHomeCache(PackageID packageID) {
        for (String platform : supportedPlatforms) {
            Path baloPath = buildContext.getBaloFromHomeCache(packageID, platform);
            if (baloPath != null && baloPath.toFile().exists()) {
                return baloPath;
            }
        }
        return null;
    }

    private void addLibsFromHomeBaloCache(PackageID packageID, HashSet<Path> modulePlatformLibs) {
        Path baloPath = getBaloPathFromHomeCache(packageID);
        if (baloPath != null) {
            addLibsFromBalo(baloPath, modulePlatformLibs);
        }
    }

    private void addLibsFromBaloDependency(PackageID packageID, HashSet<Path> modulePlatformLibs) {
        addLibsFromBalo(buildContext.getImportPathDependency(packageID).get().getMetadata().getPath(),
                modulePlatformLibs);
    }

    private void addLibsFromDistribution(PackageID packageID, HashSet<Path> modulePlatformLibs) {
        HashSet<Path> dependencies = getDependenciesFromDist(packageID);
        if (dependencies != null) {
            modulePlatformLibs.addAll(dependencies);
        }
    }

    private void addPlatformLibs(PackageID packageID, HashSet<Path> modulePlatformLibs) {
        HashSet<Path> platformLibs = new HashSet<>();
        List<Library> libraries = manifest.getPlatform().libraries;

        Optional<Dependency> importPathDependency = buildContext.getImportPathDependency(packageID);
        if (libraries != null) {
            for (Library library : libraries) {
                if ((library.getModules() == null ||
                        Arrays.asList(library.getModules()).contains(packageID.name.value) ||
                        Arrays.asList(library.getModules()).contains(packageID.orgName.value + "/" +
                                packageID.name.value)) &&
                        !(library.getScope() != null && library.getScope().equalsIgnoreCase("testOnly"))) {
                    String libFilePath = library.getPath();
                    if (libFilePath == null) {
                        continue;
                    }

                    Path nativeFile = sourceRootPath.resolve(Paths.get(libFilePath));
                    if (importPathDependency.isPresent()) {
                        platformLibs.add(nativeFile.getFileName());
                    }
                    modulePlatformLibs.add(nativeFile);
                }
            }
        }

        importPathDependency.ifPresent(dependency -> validateBaloDependencies(packageID, platformLibs,
                dependency.getMetadata().getPath()));
    }

    private void validateBaloDependencies(PackageID packageID, HashSet<Path> platformLibs, Path importDependencyPath) {
        buildContext.missedJarMap.put(packageID, new DependencyJar());
        Manifest manifestFromBalo = RepoUtils.getManifestFromBalo(importDependencyPath);
        List<Library> baloDependencies = manifestFromBalo.getPlatform().libraries;
        HashSet<Path> baloCompileScopeDependencies = new HashSet<>();
        DependencyJar jar = buildContext.missedJarMap.get(packageID);
        if (baloDependencies == null) {
            return;
        }

        for (Library baloTomlLib : baloDependencies) {
            if (baloTomlLib.getScope() != null && baloTomlLib.getScope().equalsIgnoreCase("provided")) {
                baloCompileScopeDependencies.add(Paths.get(baloTomlLib.getPath()).getFileName());
            }
        }

        for (Path baloTomlLib : baloCompileScopeDependencies) {
            if (!platformLibs.contains(baloTomlLib)) {
                jar.nativeLibs.add(baloTomlLib);
            }
        }
    }

    private void addLibsFromBalo(Path baloFilePath, HashSet<Path> moduleDependencySet) {
        String fileName = baloFilePath.getFileName().toString();
        Path baloFileUnzipDirectory = Paths.get(baloFilePath.getParent().toString(),
                fileName.substring(0, fileName.lastIndexOf('.')));
        File destFile = baloFileUnzipDirectory.toFile();

        // Read from .balo file if directory not exist.
        if (!destFile.mkdir()) {
            // Read from already unzipped balo directory.
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(destFile.toString()))) {
                for (Path path : stream) {
                    moduleDependencySet.add(path);
                }
            } catch (IOException e) {
                throw createLauncherException("unable to copy native jar: " + e.getMessage());
            }
            return;
        }
        try (JarFile jar = new JarFile(baloFilePath.toFile())) {
            Enumeration<JarEntry> enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                JarEntry file = enumEntries.nextElement();
                String entryName = file.getName();
                if (!entryName.endsWith(BLANG_COMPILED_JAR_EXT) || !entryName.contains(BALO_PLATFORM_LIB_DIR_NAME)) {
                    continue;
                }
                File f = Paths.get(baloFileUnzipDirectory.toString(),
                        entryName.split(BALO_PLATFORM_LIB_DIR_NAME)[1]).toFile();
                if (!f.exists()) { // if file already copied or its a directory, ignore
                    // get the input stream
                    try (InputStream is = jar.getInputStream(file)) {
                        Files.copy(is, f.toPath());
                    }
                }
                moduleDependencySet.add(f.toPath());
            }
        } catch (IOException e) {
            throw createLauncherException("unable to copy native jar: " + e.getMessage());
        }
    }

    private HashSet<Path> getDependenciesFromDist(PackageID packageID) {
        // Get the jar paths
        HashSet<Path> libPaths = new HashSet<>();
        File tomlFile = getTomlFilePath(packageID);

        if (skipCopyLibsFromDist) {
            return null;
        }

        Toml tomlConfig = new Toml().read(tomlFile);
        Toml platform = tomlConfig.getTable("platform");
        if (platform == null) {
            return null;
        }

        List<Object> libraries = platform.getList("libraries");
        if (libraries == null) {
            return null;
        }

        for (Object lib : libraries) {
            HashMap libInfo = (HashMap) lib;
            if (!((libInfo.get("scope") != null) &&
                    (libInfo.get("scope").toString().equalsIgnoreCase("testOnly")))) {
                Path fileName = Paths.get(libInfo.get("path").toString()).getFileName();
                libPaths.add(Paths.get(balHomePath, "bre", "lib", fileName.toString()));
            }
        }
        return libPaths;
    }

    private Path getJarFromDistribution(PackageID packageID) {
        String moduleJarName = packageID.orgName.value + "-" + packageID.name.value + "-" + packageID.version.value +
                BLANG_COMPILED_JAR_EXT;
        if (skipCopyLibsFromDist) {
            return null;
        }
        return Paths.get(balHomePath, "bre", "lib", moduleJarName);
    }

    private Path getChoreoRuntimeJar() {
        if (skipCopyLibsFromDist) {
            return null;
        }
        String ballerinaVersion = RepoUtils.getBallerinaPackVersion();
        String runtimeJarName = "ballerina-choreo-extension-rt-" + ballerinaVersion + BLANG_COMPILED_JAR_EXT;
        return Paths.get(balHomePath, "bre", "lib", runtimeJarName);
    }
}

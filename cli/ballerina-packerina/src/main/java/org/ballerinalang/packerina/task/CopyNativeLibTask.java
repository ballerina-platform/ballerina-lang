/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.task;

import com.moandjiezana.toml.Toml;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.model.ExecutableJar;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.Library;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.programfile.ProgramFileConstants;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import static org.ballerinalang.packerina.buildcontext.sourcecontext.SourceType.SINGLE_BAL_FILE;
import static org.ballerinalang.tool.LauncherUtils.createLauncherException;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALO_PLATFORM_LIB_DIR_NAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_PKG_DEFAULT_VERSION;

/**
 * Copy native libraries to target/tmp.
 */
public class CopyNativeLibTask implements Task {
    private List<String> supportedPlatforms = Arrays.stream(ProgramFileConstants.SUPPORTED_PLATFORMS)
            .collect(Collectors.toList());
    private boolean skipCopyLibsFromDist;
    private Manifest manifest;

    public CopyNativeLibTask(boolean skipCopyLibsFromDist) {
        this.skipCopyLibsFromDist = skipCopyLibsFromDist;
        supportedPlatforms.add("any");
    }

    public CopyNativeLibTask() {
        this(false);
    }
    
    @Override
    public void execute(BuildContext buildContext) {

        Path sourceRootPath = buildContext.get(BuildContextField.SOURCE_ROOT);
        String balHomePath = buildContext.get(BuildContextField.HOME_REPO).toString();
        this.manifest = ManifestProcessor.getInstance(buildContext.get(BuildContextField.COMPILER_CONTEXT)).
                getManifest();
        List<BLangPackage> moduleBirMap  = buildContext.getModules();
        if (buildContext.getSourceType() == SINGLE_BAL_FILE) {
            copyImportedJarsForSingleBalFile(buildContext, moduleBirMap, sourceRootPath, balHomePath);
            return;
        }
        copyImportedJarsForModules(buildContext, moduleBirMap, sourceRootPath, balHomePath);
    }

    private void copyImportedJarsForSingleBalFile(BuildContext buildContext, List<BLangPackage> moduleBirMap,
                                                  Path sourceRootPath, String balHomePath) {
        // Iterate through the imports and copy dependencies.
        HashSet<PackageID> alreadyImportedSet = new HashSet<>();
        for (BLangPackage pkg : moduleBirMap) {
            copyImportedLibs(pkg.symbol.imports, buildContext.moduleDependencyPathMap.get(pkg.packageID).platformLibs,
                             buildContext, sourceRootPath, balHomePath, alreadyImportedSet);
        }
    }

    private void copyImportedJarsForModules(BuildContext buildContext,  List<BLangPackage> moduleBirMap,
                                            Path sourceRootPath, String balHomePath) {
        // Iterate through the imports and copy dependencies.
        HashSet<PackageID> alreadyImportedSet = new HashSet<>();
        for (BLangPackage pkg : moduleBirMap) {
            // Copy jars from imported modules.
            copyImportedLibs(pkg.symbol.imports, buildContext.moduleDependencyPathMap.get(pkg.packageID).platformLibs,
                             buildContext, sourceRootPath, balHomePath, alreadyImportedSet);
        }
    }

    private void copyImportedLibs(List<BPackageSymbol> imports, HashSet<Path> moduleDependencySet,
                                  BuildContext buildContext, Path sourceRootPath, String balHomePath,
                                  HashSet<PackageID> alreadyImportedSet) {
        for (BPackageSymbol importSymbol : imports) {
            PackageID pkgId = importSymbol.pkgID;
            ExecutableJar jar = buildContext.moduleDependencyPathMap.get(pkgId);
            if (!alreadyImportedSet.contains(pkgId)) {
                alreadyImportedSet.add(pkgId);
                if (jar == null) {
                    jar = new ExecutableJar();
                    buildContext.moduleDependencyPathMap.put(pkgId, jar);
                }
                copyImportedLib(buildContext, importSymbol, sourceRootPath, balHomePath, jar.platformLibs);
                copyImportedLibs(importSymbol.imports, jar.platformLibs, buildContext, sourceRootPath,
                                 balHomePath, alreadyImportedSet);
            }
            moduleDependencySet.addAll(jar.platformLibs);
        }
    }

    private void copyImportedLib(BuildContext buildContext, BPackageSymbol importz, Path project, String balHomePath,
                                 HashSet<Path> moduleDependencySet) {
        // Get the balo paths
        for (String platform : supportedPlatforms) {
            Path importJar = findImportBaloPath(buildContext, importz, project, platform);
            if (importJar != null && Files.exists(importJar)) {
                copyLibsFromBalo(importJar, moduleDependencySet);
                return;
            }
        }

        // If platform libs are defined, copy them to target
        List<Library> libraries = manifest.getPlatform().libraries;
        if (libraries != null && libraries.size() > 0) {
            for (Library library : libraries) {

                if (library.getGroupId() == null || library.getModules() == null || library.getPath() == null) {
                    continue;
                }

                if (library.getGroupId().equals(importz.pkgID.orgName.value) &&
                        Arrays.asList(library.getModules()).contains(importz.pkgID.name.value)) {
                    Path libFilePath = Paths.get(library.getPath());
                    Path libFile = project.resolve(libFilePath);
                    Path libFileName = libFilePath.getFileName();

                    if (libFileName == null) {
                        continue;
                    }
                    Path path = Paths.get(libFile.toUri());
                    moduleDependencySet.add(path);
                    return;
                }
            }
        }

        // If balo cannot be found from target, cache or platform-libs, get dependencies from distribution toml.
        copyDependenciesFromToml(importz, balHomePath, moduleDependencySet);
    }

    private static Path findImportBaloPath(BuildContext buildContext, BPackageSymbol importz, Path project,
                                           String platform) {
        // Get the jar paths
        PackageID id = importz.pkgID;
    
        Optional<Dependency> importPathDependency = buildContext.getImportPathDependency(id);
        // Look if it is a project module.
        if (ProjectDirs.isModuleExist(project, id.name.value)) {
            // If so fetch from project balo cache
            return buildContext.getBaloFromTarget(id);
        } else if (importPathDependency.isPresent()) {
            return importPathDependency.get().getMetadata().getPath();
        } else {
            // If not fetch from home balo cache.
            return buildContext.getBaloFromHomeCache(id, platform);
        }
    }

    private void copyLibsFromBalo(Path baloFilePath, HashSet<Path> moduleDependencySet) {

        String fileName = baloFilePath.getFileName().toString();
        Path baloFileUnzipDirectory = Paths.get(baloFilePath.getParent().toString(),
                                                fileName.substring(0, fileName.lastIndexOf(".")));
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
            java.util.Enumeration enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                JarEntry file = (JarEntry) enumEntries.nextElement();
                if (!file.getName().endsWith(BLANG_COMPILED_JAR_EXT)) {
                    continue;
                }
                File f = Paths.get(baloFileUnzipDirectory.toString(),
                                   file.getName().split(BALO_PLATFORM_LIB_DIR_NAME)[1]).toFile();
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

    private void copyDependenciesFromToml(BPackageSymbol importz, String balHomePath,
                                          HashSet<Path> moduleDependencySet) {
        // Get the jar paths
        PackageID id = importz.pkgID;
        String version = BLANG_PKG_DEFAULT_VERSION;
        if (!id.version.value.equals("")) {
            version = id.version.value;
        }
        if (skipCopyLibsFromDist) {
            return;
        }
        File tomlfile = Paths.get(balHomePath, "bir-cache", id.orgName.value, id.name.value,
                                  version, "Ballerina.toml").toFile();

        if (!tomlfile.exists()) {
            return;
        }
        Toml tomlConfig = new Toml().read(tomlfile);
        Toml platform = tomlConfig.getTable("platform");
        if (platform == null) {
            return;
        }
        List<Object> libraries = platform.getList("libraries");
        if (libraries == null) {
            return;
        }
        for (Object lib : libraries) {
            Path fileName = Paths.get(((HashMap) lib).get("path").toString()).getFileName();
            Path libPath = Paths.get(balHomePath, "bre", "lib", fileName.toString());
            moduleDependencySet.add(libPath);
        }
    }
}

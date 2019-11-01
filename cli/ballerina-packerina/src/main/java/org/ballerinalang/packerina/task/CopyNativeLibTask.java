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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_PKG_DEFAULT_VERSION;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.TARGET_TMP_DIRECTORY;

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

        Path targetDir = buildContext.get(BuildContextField.TARGET_DIR);
        Path tmpDir = targetDir.resolve(TARGET_TMP_DIRECTORY);
        Path sourceRootPath = buildContext.get(BuildContextField.SOURCE_ROOT);
        String balHomePath = buildContext.get(BuildContextField.HOME_REPO).toString();
        this.manifest = ManifestProcessor.getInstance(buildContext.get(BuildContextField.COMPILER_CONTEXT)).
                getManifest();
        // Create target/tmp folder
        try {
            if (!tmpDir.toFile().exists()) {
                Files.createDirectory(tmpDir);
            }
        } catch (IOException e) {
            throw createLauncherException("unable to copy the native library: " + e.getMessage());
        }
        List<BLangPackage> moduleBirMap = buildContext.getModules();
        if (buildContext.getSourceType() == SINGLE_BAL_FILE) {
            copyImportedJarsForSingleBalFile(buildContext, moduleBirMap, sourceRootPath, tmpDir, balHomePath);
            return;
        }
        copyImportedJarsForModules(buildContext, moduleBirMap, sourceRootPath, tmpDir, balHomePath);
    }

    private void copyImportedJarsForSingleBalFile(BuildContext buildContext, List<BLangPackage> moduleBirMap,
                                                  Path sourceRootPath, Path tmpDir, String balHomePath) {
        // Iterate through the imports and copy dependencies.
        HashSet<String> alreadyImportedSet = new HashSet<>();
        for (BLangPackage pkg : moduleBirMap) {
            copyImportedLibs(pkg.symbol.imports, buildContext, sourceRootPath, tmpDir, balHomePath, alreadyImportedSet);
        }
    }

    private void copyImportedJarsForModules(BuildContext buildContext, List<BLangPackage> moduleBirMap,
                                            Path sourceRootPath, Path tmpDir, String balHomePath) {
        // Iterate through the imports and copy dependencies.
        HashSet<String> alreadyImportedSet = new HashSet<>();
        for (BLangPackage pkg : moduleBirMap) {
            // Copy jars from imported modules.
            copyImportedLibs(pkg.symbol.imports, buildContext, sourceRootPath, tmpDir, balHomePath, alreadyImportedSet);
            // Copy jars from module balo.
            Path baloAbsolutePath = buildContext.getBaloFromTarget(pkg.packageID);
            copyLibsFromBalo(baloAbsolutePath.toString(), tmpDir.toString());
        }
    }

    private void copyImportedLibs(List<BPackageSymbol> imports, BuildContext buildContext, Path sourceRootPath,
                                  Path tmpDir, String balHomePath, HashSet<String> alreadyImportedSet) {
        for (BPackageSymbol importSymbol : imports) {
            PackageID pkgId = importSymbol.pkgID;
            String id = pkgId.toString();
            if (alreadyImportedSet.contains(id)) {
                continue;
            }
            alreadyImportedSet.add(id);
            copyImportedLib(buildContext, importSymbol, sourceRootPath, tmpDir, balHomePath);
            copyImportedLibs(importSymbol.imports, buildContext, sourceRootPath,
                             tmpDir, balHomePath, alreadyImportedSet);
        }
    }

    private void copyImportedLib(BuildContext buildContext, BPackageSymbol importz, Path project,
                                 Path tmpDir, String balHomePath) {
        // Get the balo paths
        for (String platform : supportedPlatforms) {
            Path importJar = findImportBaloPath(buildContext, importz, project, platform);
            if (importJar != null && Files.exists(importJar)) {
                copyLibsFromBalo(importJar.toString(), tmpDir.toString());
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

                    Path targetPath = tmpDir.resolve(libFileName.toString());

                    if (targetPath.toFile().exists()) {
                        return;
                    }

                    try {
                        Files.copy(libFile, targetPath);
                        return;
                    } catch (IOException e) {
                        throw createLauncherException("dependency jar '" + libFilePath.toString() + "' cannot be " +
                                "copied due to " + e.getMessage());
                    }
                }
            }
        }

        // If balo cannot be found from target, cache or platform-libs, get dependencies from distribution toml.
        copyDependenciesFromToml(importz, balHomePath, tmpDir);
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

    private void copyLibsFromBalo(String jarFileName, String destFile) {
        try (JarFile jar = new JarFile(jarFileName)) {

            java.util.Enumeration enumEntries = jar.entries();

            while (enumEntries.hasMoreElements()) {
                JarEntry file = (JarEntry) enumEntries.nextElement();
                if (file.getName().contains(BALO_PLATFORM_LIB_DIR_NAME)) {
                    File f = new File(destFile + File.separator +
                                              file.getName().split(BALO_PLATFORM_LIB_DIR_NAME)[1]);
                    if (f.exists() || file.isDirectory()) { // if file already copied or its a directory, ignore
                        continue;
                    }
                    // get the input stream
                    try (InputStream is = jar.getInputStream(file)) {
                        Files.copy(is, f.toPath());
                    }
                }
            }
        } catch (IOException e) {
            throw createLauncherException("unable to copy native jar: " + e.getMessage());
        }
    }

    private void copyDependenciesFromToml(BPackageSymbol importz, String balHomePath, Path tmpDir) {
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
            try {
                Path jarTarget = tmpDir.resolve(fileName);
                Files.copy(libPath, jarTarget, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw createLauncherException("unable to find the dependency jar from the distribution: " +
                        e.getMessage());
            }
        }
    }
}

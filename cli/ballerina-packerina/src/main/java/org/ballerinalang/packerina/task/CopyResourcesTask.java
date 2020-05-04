/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.programfile.ProgramFileConstants;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.ballerinalang.tool.LauncherUtils.createLauncherException;

/**
 * Copy resources to module jar.
 *
 * @since 1.1.2
 */
public class CopyResourcesTask implements Task {

    @Override
    public void execute(BuildContext buildContext) {
        CompilerContext context = buildContext.get(BuildContextField.COMPILER_CONTEXT);
        PackageCache packageCache = PackageCache.getInstance(context);
        boolean skipTests = buildContext.skipTests();

        Path sourceRoot = buildContext.get(BuildContextField.SOURCE_ROOT);
        List<BLangPackage> moduleBirMap = buildContext.getModules();
        for (BLangPackage module : moduleBirMap) {
            PackageID packageID = module.packageID;
            BLangPackage bLangPackage = packageCache.get(packageID);
            copyResourcesToJar(buildContext, sourceRoot, module, true);
            copyImportModuleResourcesToJar(buildContext, module, true);

            if (skipTests || !bLangPackage.hasTestablePackage()) {
                continue;
            }

            // Copy tests resources to testable jar.
            for (BLangPackage testPkg : bLangPackage.getTestablePkgs()) {
                if (!buildContext.moduleDependencyPathMap.containsKey(testPkg.packageID)) {
                    continue;
                }
                copyResourcesToJar(buildContext, sourceRoot, module, false);
                copyImportModuleResourcesToJar(buildContext, testPkg, false);
            }
            copyModuleResourcesToTestableJar(buildContext, module);
        }
    }

    private void copyResourcesToJar(BuildContext buildContext, Path sourceRootPath, BLangPackage module,
                                    boolean isModuleResource) {
        Path resourceDir;
        Path moduleJarPath;

        // Get resources directory
        if (isModuleResource) {
            resourceDir = sourceRootPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME)
                    .resolve(module.packageID.name.value)
                    .resolve(ProjectDirConstants.RESOURCE_DIR_NAME);
        } else {
            resourceDir = sourceRootPath.resolve(ProjectDirConstants.SOURCE_DIR_NAME)
                    .resolve(module.packageID.name.value)
                    .resolve(ProjectDirConstants.TEST_DIR_NAME)
                    .resolve(ProjectDirConstants.RESOURCE_DIR_NAME);
        }

        if (!resourceDir.toFile().exists()) {
            return;
        }
        // Get the module jar
        if (isModuleResource) {
            moduleJarPath = buildContext.getJarPathFromTargetCache(module.packageID);
        } else {
            moduleJarPath = buildContext.getTestJarPathFromTargetCache(module.packageID);
        }

        URI uberJarUri = URI.create("jar:" + moduleJarPath.toUri().toString());
        try (FileSystem toFs = FileSystems.newFileSystem(uberJarUri, Collections.emptyMap())) {
            Path to = toFs.getRootDirectories().iterator().next().resolve("resources")
                    .resolve(module.packageID.orgName.value)
                    .resolve(module.packageID.name.value)
                    .resolve(module.packageID.version.value);
            Files.walkFileTree(resourceDir, new Copy(resourceDir, to));
        } catch (IOException e) {
            throw createLauncherException("error while adding resources to module jar :" + e.getMessage());
        }
    }

    private void copyImportModuleResourcesToJar(BuildContext buildContext, BLangPackage module,
                                                boolean isModuleResource) {
        List<String> supportedPlatforms = Arrays.stream(ProgramFileConstants.SUPPORTED_PLATFORMS)
                .collect(Collectors.toList());
        supportedPlatforms.add("any");
        List<BPackageSymbol> imports = module.symbol.imports;
        for (BPackageSymbol importSymbol : imports) {
            PackageID pkgId = importSymbol.pkgID;
            for (String platform : supportedPlatforms) {
                Path importJar = buildContext.getBaloFromHomeCache(pkgId, platform);
                if (importJar != null && Files.exists(importJar)) {
                    copyImportModuleResourcesFromBalo(buildContext, importJar, module, importSymbol, isModuleResource);
                }
            }
        }
    }

    private void copyImportModuleResourcesFromBalo(BuildContext buildContext, Path baloFilePath, BLangPackage module,
                                                   BPackageSymbol importSymbol, boolean isModuleResource) {
        Path moduleJarPath;
        if (isModuleResource) {
            moduleJarPath = buildContext.getJarPathFromTargetCache(module.packageID);
        } else {
            moduleJarPath = buildContext.getTestJarPathFromTargetCache(module.packageID);
        }
        URI uberJarUri = URI.create(ProjectDirConstants.JAR_FILE_SYSTEM + moduleJarPath.toUri().toString());
        URI baloUri = URI.create(ProjectDirConstants.JAR_FILE_SYSTEM + baloFilePath.toUri().toString());

        try (FileSystem baloFs = getFileSystem(baloUri);
             FileSystem uberJarFs = FileSystems.newFileSystem(uberJarUri, Collections.emptyMap())) {
            Path fromBalo = baloFs.getRootDirectories().iterator().next()
                    .resolve(ProjectDirConstants.RESOURCE_DIR_NAME);
            Path toUberJar = uberJarFs.getRootDirectories().iterator().next()
                    .resolve(ProjectDirConstants.RESOURCE_DIR_NAME)
                    .resolve(importSymbol.pkgID.orgName.value)
                    .resolve(importSymbol.pkgID.name.value)
                    .resolve(importSymbol.pkgID.version.value);
            Files.walkFileTree(fromBalo, new Copy(fromBalo, toUberJar));
        } catch (IOException e) {
            throw createLauncherException("error while adding imported resources to module jar :" + e.getMessage());
        }
    }

    private void copyModuleResourcesToTestableJar(BuildContext buildContext, BLangPackage module) {
        Path moduleJarPath = buildContext.getJarPathFromTargetCache(module.packageID);
        Path moduleTestableJarPath = buildContext.getTestJarPathFromTargetCache(module.packageID);
        URI uberJarUri = URI.create(ProjectDirConstants.JAR_FILE_SYSTEM + moduleJarPath.toUri().toString());
        URI testableJarUri = URI.create(ProjectDirConstants.JAR_FILE_SYSTEM + moduleTestableJarPath.toUri().toString());

        try (FileSystem uberJarFs = getFileSystem(uberJarUri);
             FileSystem testableJarFs = getFileSystem(testableJarUri)) {
            Path fromUberJar = uberJarFs.getRootDirectories().iterator().next()
                    .resolve(ProjectDirConstants.RESOURCE_DIR_NAME);
            Path toTestableJar = testableJarFs.getRootDirectories().iterator().next()
                    .resolve(ProjectDirConstants.RESOURCE_DIR_NAME);

            if (Files.exists(fromUberJar)) {
                Files.walkFileTree(fromUberJar, new Copy(fromUberJar, toTestableJar));
            }
        } catch (IOException e) {
            throw createLauncherException("error while adding resources to module testable jar :" + e.getMessage());
        }
    }

    private FileSystem getFileSystem(URI uri) {
        try {
            return FileSystems.newFileSystem(uri, Collections.<String, String>emptyMap());
        } catch (FileSystemAlreadyExistsException | IOException e) {
            return FileSystems.getFileSystem(uri);
        }
    }

    static class Copy extends SimpleFileVisitor<Path> {

        private Path fromPath;
        private Path toPath;
        private StandardCopyOption copyOption;

        Copy(Path fromPath, Path toPath, StandardCopyOption copyOption) {
            this.fromPath = fromPath;
            this.toPath = toPath;
            this.copyOption = copyOption;
        }

        Copy(Path fromPath, Path toPath) {
            this(fromPath, toPath, StandardCopyOption.REPLACE_EXISTING);
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            Path targetPath = toPath.resolve(fromPath.relativize(dir).toString());
            if (!Files.exists(targetPath)) {
                Files.createDirectories(targetPath);
            }
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Path toFile = toPath.resolve(fromPath.relativize(file).toString());
            if (!Files.exists(toFile)) {
                Files.copy(file, toFile, copyOption);
            }
            return FileVisitResult.CONTINUE;
        }
    }
}

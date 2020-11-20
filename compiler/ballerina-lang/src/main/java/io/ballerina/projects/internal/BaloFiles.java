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
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageManifest;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.model.PackageJson;
import io.ballerina.projects.util.ProjectUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.ballerina.projects.internal.ProjectFiles.loadDocuments;
import static io.ballerina.projects.util.ProjectConstants.MODULES_ROOT;
import static io.ballerina.projects.util.ProjectConstants.PACKAGE_JSON;

/**
 * Contains a set of utility methods that create an in-memory representation of a Ballerina project using a balo.
 *
 * @since 2.0.0
 */
public class BaloFiles {
    private static final Gson gson = new Gson();

    // TODO change class name to utils
    private BaloFiles() {
    }

    static PackageData loadPackageData(Path balrPath, PackageManifest packageManifest) {
        URI zipURI = URI.create("jar:" + balrPath.toUri().toString());
        try (FileSystem zipFileSystem = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
            // Load default module
            String pkgName = packageManifest.name().toString();
            Path defaultModulePathInBalo = zipFileSystem.getPath(MODULES_ROOT, pkgName);
            ModuleData defaultModule = loadModule(defaultModulePathInBalo);

            // load other modules
            Path modulesPathInBalo = zipFileSystem.getPath(MODULES_ROOT);
            List<ModuleData> otherModules = loadOtherModules(modulesPathInBalo, defaultModulePathInBalo);
            return PackageData.from(balrPath, defaultModule, otherModules);
        } catch (IOException e) {
            throw new ProjectException("Failed to read balr file:" + balrPath);
        }
    }

    private static void validatePackageJson(PackageJson packageJson, Path balrPath) {
        if (packageJson.getOrganization() == null || "".equals(packageJson.getOrganization())) {
            throw new ProjectException("'organization' does not exists in 'package.json': " + balrPath);
        }
        if (packageJson.getName() == null || "".equals(packageJson.getName())) {
            throw new ProjectException("'name' does not exists in 'package.json': " + balrPath);
        }
        if (packageJson.getVersion() == null || "".equals(packageJson.getVersion())) {
            throw new ProjectException("'version' does not exists in 'package.json': " + balrPath);
        }
    }

    private static ModuleData loadModule(Path modulePath) {
        // check module path exists
        if (Files.notExists(modulePath)) {
            throw new ProjectException("The 'modules' directory does not exists in '" + modulePath + "'");
        }

        String moduleName = String.valueOf(modulePath.getFileName());
        if (moduleName.contains(".")) { // not default module
            moduleName = moduleName.split("\\.")[1];
            moduleName = moduleName.replace("/", "");
        }

        // validate moduleName
        if (!ProjectUtils.validateModuleName(moduleName)) {
            throw new ProjectException("Invalid module name : '" + moduleName + "' :\n" +
                    "Module name can only contain alphanumerics, underscores and periods " +
                    "and the maximum length is 256 characters: " + modulePath);
        }

        List<DocumentData> srcDocs = loadDocuments(modulePath);
        List<DocumentData> testSrcDocs = Collections.emptyList();

        // TODO Read Module.md file. Do we need to? Balo creator may need to package Module.md
        return ModuleData.from(modulePath, moduleName, srcDocs, testSrcDocs);
    }

    private static List<ModuleData> loadOtherModules(Path modulesDirPath,
                                                     Path defaultModulePath) {
        try (Stream<Path> pathStream = Files.walk(modulesDirPath, 1)) {
            return pathStream
                    .filter(path -> !path.equals(modulesDirPath))
                    .filter(path -> path.getFileName() != null
                            && !path.getFileName().equals(defaultModulePath.getFileName()))
                    .filter(Files::isDirectory)
                    .map(BaloFiles::loadModule)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new ProjectException("Failed to read modules from directory: " + modulesDirPath, e);
        }
    }

    public static PackageManifest createPackageManifest(Path balrPath) {
        URI zipURI = URI.create("jar:" + balrPath.toAbsolutePath().toUri().toString());
        try (FileSystem zipFileSystem = FileSystems.newFileSystem(zipURI, new HashMap<>())) {
            Path packageJsonPath = zipFileSystem.getPath(PACKAGE_JSON);
            if (Files.notExists(packageJsonPath)) {
                throw new ProjectException("package.json does not exists in '" + balrPath + "'");
            }

            // Load `package.json`
            PackageJson packageJson = readPackageJson(balrPath, packageJsonPath);
            validatePackageJson(packageJson, balrPath);
            extractPlatformLibraries(zipFileSystem, packageJson, balrPath);
            return getPackageManifest(packageJson);
        } catch (IOException e) {
            throw new ProjectException("Failed to read balr file:" + balrPath);
        }
    }

    private static void extractPlatformLibraries(FileSystem zipFileSystem, PackageJson packageJson, Path balrPath) {
        if (packageJson.getPlatformDependencies() != null) {
            packageJson.getPlatformDependencies().forEach(dependency -> {
                Path libPath = balrPath.getParent().resolve(dependency.getPath());
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
    }

    private static PackageManifest getPackageManifest(PackageJson packageJson) {
        PackageDescriptor pkgDesc = PackageDescriptor.from(packageJson.getName(),
                packageJson.getOrganization(), packageJson.getVersion());
        List<PackageManifest.Dependency> dependencies;
        if (packageJson.getDependencies() != null) {
            dependencies = packageJson.getDependencies();
        } else {
            dependencies = Collections.emptyList();
        }

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

        return PackageManifest.from(pkgDesc, dependencies, platforms, Collections.emptyMap());
    }

    private static PackageJson readPackageJson(Path balrPath, Path packageJsonPath) {
        PackageJson packageJson;
        try {
            packageJson = gson.fromJson(Files.newBufferedReader(packageJsonPath), PackageJson.class);
        } catch (JsonSyntaxException e) {
            throw new ProjectException("Invalid package.json format in '" + balrPath + "'");
        } catch (IOException | JsonIOException e) {
            throw new ProjectException("Failed to read the package.json in '" + balrPath + "'");
        }
        return packageJson;
    }
}

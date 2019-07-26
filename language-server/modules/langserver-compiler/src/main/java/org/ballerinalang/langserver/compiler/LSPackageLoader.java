/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.compiler;

import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.PackageLoader;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Loads the Ballerina builtin core and builtin packages.
 */
public class LSPackageLoader {

    private static final String LIB_REPO_DIR = "lib/repo";
    private static final String DOT = ".";
    private static final String BALLERINA_HOME = "ballerina.home";
    private static List<BallerinaPackage> sdkPackages = getSDKPackagesFromSrcDir();
    private static List<BallerinaPackage> homeRepoPackages = getPackagesFromHomeRepo();

    /**
     * Get the package by ID via Package loader.
     *
     * @param context   Compiler context
     * @param packageID Package ID to resolve
     * @return {@link BLangPackage} Resolved BLang Package
     */
    @Deprecated
    public static BLangPackage getPackageById(CompilerContext context, PackageID packageID) {
        BLangPackage bLangPackage = LSPackageCache.getInstance(context).get(packageID);
        if (bLangPackage == null) {
            synchronized (LSPackageLoader.class) {
                bLangPackage = LSPackageCache.getInstance(context).get(packageID);
                if (bLangPackage == null) {
                    PackageLoader pkgLoader = PackageLoader.getInstance(context);
                    bLangPackage = pkgLoader.loadAndDefinePackage(packageID);
                }
            }
        }
        return bLangPackage;
    }

    /**
     * Get the package by ID via Package loader.
     *
     * @param context   Compiler context
     * @param packageID Package ID to resolve
     * @return {@link BLangPackage} Resolved BLang Package
     */
    public static Optional<BPackageSymbol> getPackageSymbolById(CompilerContext context, PackageID packageID) {
        if (isLangLib(packageID)) {
            return Optional.empty();
        }
        BPackageSymbol packageSymbol;
        synchronized (LSPackageLoader.class) {
            PackageLoader pkgLoader = PackageLoader.getInstance(context);
            packageSymbol = pkgLoader.loadPackageSymbol(packageID, null, null);
        }
        return Optional.ofNullable(packageSymbol);
    }

    /**
     * Get packages from the source directory.
     *
     * @return {@link List} array of package names available in SDK source directory
     */
    private static List<BallerinaPackage> getSDKPackagesFromSrcDir() {
        List<BallerinaPackage> ballerinaPackages = new ArrayList<>();
        String ballerinaSDKHome = System.getProperty(BALLERINA_HOME);
        if (ballerinaSDKHome != null) {
            String ballerinaLibRepoDir = Paths.get(ballerinaSDKHome, LIB_REPO_DIR).toString();
            File reposDir = new File(ballerinaLibRepoDir);
            String[] repos = reposDir.list((dir, name) -> !name.startsWith(DOT));
            if (repos != null) {
                for (String repo : repos) {
                    String repoDir = Paths.get(ballerinaLibRepoDir, repo).toString();
                    File packageDir = new File(repoDir);
                    String[] packageNames = packageDir.list(((dir, name) -> !name.startsWith(DOT)));
                    if (packageNames != null) {
                        for (String name : packageNames) {
                            if ("ballerina".equals(repo) && name != null && name.contains("__internal")) {
                                continue;
                            }
                            BallerinaPackage ballerinaPackage = new BallerinaPackage(repo, name, null);
                            ballerinaPackages.add(ballerinaPackage);
                        }
                    }
                }
            }
        }
        return ballerinaPackages;
    }

    /**
     * Get packages available in the home repo.
     *
     * @return {@link List} list of ballerina package details
     */
    private static List<BallerinaPackage> getPackagesFromHomeRepo() {
        List<BallerinaPackage> ballerinaPackages = new ArrayList<>();
        String homeRepoPath = Paths.get(RepoUtils.createAndGetHomeReposPath().toString(),
                ProjectDirConstants.CACHES_DIR_NAME, ProjectDirConstants.BALLERINA_CENTRAL_DIR_NAME).toString();
        File homeRepo = new File(homeRepoPath);
        if (homeRepo.exists() && homeRepo.isDirectory()) {
            File[] orgNames = homeRepo.listFiles(((dir, name) -> !name.startsWith(DOT)));
            if (orgNames != null) {
                for (File orgDir : orgNames) {
                    if (orgDir.isDirectory()) {
                        String orgName = orgDir.getName();
                        File[] packageNames = orgDir.listFiles(((dir, name) -> !name.startsWith(DOT)));
                        if (packageNames != null) {
                            for (File pkgDir : packageNames) {
                                if (pkgDir.isDirectory()) {
                                    String pkgName = pkgDir.getName();
                                    File[] versionNames = pkgDir.listFiles(((dir, name) -> !name.startsWith(DOT)));
                                    if (versionNames != null) {
                                        for (File versionDir : versionNames) {
                                            if (versionDir.isDirectory()) {
                                                String version = versionDir.getName();
                                                ballerinaPackages.add(new BallerinaPackage(orgName, pkgName, version));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return ballerinaPackages;
    }

    public static List<BallerinaPackage> getSdkPackages() {
        return new ArrayList<>(sdkPackages);
    }

    public static List<BallerinaPackage> getHomeRepoPackages() {
        return new ArrayList<>(homeRepoPackages);
    }

    /**
     * Clear the home repo packages.
     */
    public static void clearHomeRepoPackages() {
        homeRepoPackages.clear();
    }

    /**
     * Returns a list of packages available for the current project.
     *
     * @param pkg        Built {@link BLangPackage}
     * @param sourceRoot Source Root
     * @param fileUri file uri
     * @return List of Ballerina Packages
     */
    public static List<BallerinaPackage> getCurrentProjectImportPackages(BLangPackage pkg, String sourceRoot,
                                                                         String fileUri) {
        Path sourceRootPath = Paths.get(sourceRoot);
        List<BallerinaPackage> packageList = new ArrayList<>();
        if (!RepoUtils.isBallerinaProject(sourceRootPath)) {
            // Skip for non-projects
            return packageList;
        }
        // Get current module name
        Path currentModulePath = getCurrentModulePath(Paths.get(fileUri), sourceRootPath);

        File sourceRootFile = sourceRootPath.toFile();
        if (!sourceRootFile.exists() || !sourceRootFile.isDirectory()) {
            // Skip for non directories
            return packageList;
        }
        File[] moduleNames = sourceRootFile.listFiles(((dir, name) -> !name.startsWith(DOT)));
        if (moduleNames == null) {
            // Skip when no modules found
            return packageList;
        }
        for (File moduleDir : moduleNames) {
            if (!moduleDir.isDirectory()) {
                // Skip when not a module
                continue;
            }
            String moduleName = moduleDir.getName();
            if (currentModulePath.toFile().getName().equals(moduleName)) {
                // Skip current module
                continue;
            }
            File[] packageNames = moduleDir.listFiles(((dir, name) -> !name.startsWith(DOT)));
            if (packageNames == null) {
                continue;
            }
            packageList.add(new BallerinaPackage(pkg.packageID.orgName.value, moduleName, pkg.packageID.version.value));
        }
        return packageList;
    }

    /**
     * Returns current module path.
     *
     * @param sourceFilePath source file path
     * @param projectRoot    project root
     * @return currentModule path
     */
    private static Path getCurrentModulePath(Path sourceFilePath, Path projectRoot) {
        if (sourceFilePath == null || projectRoot == null) {
            return null;
        }
        Path currentModulePath = projectRoot;
        Path prevSourceRoot = sourceFilePath.getParent();
        try {
            while (prevSourceRoot != null) {
                Path newSourceRoot = prevSourceRoot.getParent();
                currentModulePath = prevSourceRoot;
                if (newSourceRoot == null || Files.isSameFile(newSourceRoot, projectRoot)) {
                    // We have reached the project root
                    break;
                }
                prevSourceRoot = newSourceRoot;
            }
        } catch (IOException e) {
            // do nothing
        }
        return currentModulePath;
    }

    /**
     * Whether the given package is a lang lib.
     *
     * @param packageID Package ID to evaluate
     * @return {@link Boolean} whether package is a lang lib
     */
    private static boolean isLangLib(PackageID packageID) {
        String orgName = packageID.orgName.value;
        List<String> nameComps = packageID.nameComps.stream().map(Name::getValue).collect(Collectors.toList());

        return orgName.equals("ballerina") && nameComps.get(0).equals("lang");
    }
}

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

import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaPackage;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.PackageLoader;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
                            if (name == null || ("ballerina".equals(repo) && name.contains("__internal"))
                                    || name.contains(".balx")) {
                                continue;
                            }
                            File versionDir = Paths.get(packageDir.getAbsolutePath(), name).toFile();
                            String[] versions = versionDir.list();
                            if (versions != null) {
                                for (String version : versions) {
                                    BallerinaPackage ballerinaPackage = new BallerinaPackage(repo, name, version);
                                    ballerinaPackages.add(ballerinaPackage);
                                }
                            }
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
     * Returns a list of modules available for the current project.
     *
     * @param pkg Built {@link BLangPackage}
     * @param context Built {@link LSContext}
     * @return List of Ballerina Packages
     */
    public static List<BallerinaPackage> getCurrentProjectModules(BLangPackage pkg, LSContext context) {
        List<BallerinaPackage> packageList = new ArrayList<>();
        LSDocumentIdentifier lsDocument = context.get(DocumentServiceKeys.LS_DOCUMENT_KEY);
        
        /*
        If the lsDocument instance is null or not within a project, we skip processing
         */
        if (lsDocument == null || !lsDocument.isWithinProject()) {
            return packageList;
        }
        String currentModuleName = context.get(DocumentServiceKeys.CURRENT_PKG_NAME_KEY);
        for (String moduleName : lsDocument.getProjectModules()) {
            if (currentModuleName.equals(moduleName)) {
                continue;
            }
            packageList.add(new BallerinaPackage(pkg.packageID.orgName.value, moduleName, pkg.packageID.version.value));
        }
        return packageList;
    }
}

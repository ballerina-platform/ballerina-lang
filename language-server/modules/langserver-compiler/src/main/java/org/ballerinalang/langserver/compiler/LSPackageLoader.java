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
import org.wso2.ballerinalang.compiler.semantics.analyzer.CodeAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SemanticAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the Ballerina builtin core and builtin packages.
 */
public class LSPackageLoader {

    private static final String SOURCE_DIR = "src";
    private static final String BALLERINA_ORG = "ballerina";
    private static final String DOT = ".";
    private static final String BALLERINA_HOME = "ballerina.home";
    @Deprecated
    private static final String[] STATIC_PKG_NAMES = {"http", "swagger", "mime", "auth", "cache", "config", "sql",
            "file", "internal", "io", "log", "math", "system", "reflect", "runtime", "crypto", "task",
            "time", "transactions", "builtin"};
    private static List<BallerinaPackage> sdkPackages = getSDKPackagesFromSrcDir();
    private static List<BallerinaPackage> homeRepoPackages = getPackagesFromHomeRepo();

    /**
     * Get the Builtin Package.
     *
     * @return {@link BLangPackage} Builtin BLang package
     */
    public static List<BLangPackage> getBuiltinPackages(CompilerContext context) {
        List<BLangPackage> builtins = new ArrayList<>();
        PackageLoader pkgLoader = PackageLoader.getInstance(context);
        SemanticAnalyzer semAnalyzer = SemanticAnalyzer.getInstance(context);
        CodeAnalyzer codeAnalyzer = CodeAnalyzer.getInstance(context);
        BLangPackage builtInPkg =
                codeAnalyzer.analyze(semAnalyzer.analyze(pkgLoader.loadAndDefinePackage(Names.BUILTIN_ORG.getValue(),
                        Names.BUILTIN_PACKAGE.getValue(), Names.EMPTY.getValue())));
        builtins.add(builtInPkg);

        return builtins;
    }

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
    public static BPackageSymbol getPackageSymbolById(CompilerContext context, PackageID packageID) {
        BPackageSymbol packageSymbol;
        synchronized (LSPackageLoader.class) {
            PackageLoader pkgLoader = PackageLoader.getInstance(context);
            packageSymbol = pkgLoader.loadPackageSymbol(packageID, null, null);
        }
        return packageSymbol;
    }

    /**
     * Returns a static packages list.
     *
     * @return static packages list
     */
    @Deprecated
    public static String[] getStaticPkgNames() {
        return STATIC_PKG_NAMES.clone();
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
            String ballerinaSDKSrcDir = Paths.get(ballerinaSDKHome, SOURCE_DIR).toString();
            File projectDir = new File(ballerinaSDKSrcDir);
            String[] packageNames = projectDir.list(((dir, name) -> !name.startsWith(DOT)));
            if (packageNames != null) {
                for (String name : packageNames) {
                    BallerinaPackage ballerinaPackage = new BallerinaPackage(BALLERINA_ORG, name, null);
                    ballerinaPackages.add(ballerinaPackage);
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
        return sdkPackages;
    }

    public static List<BallerinaPackage> getHomeRepoPackages() {
        return homeRepoPackages;
    }
}

/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.toml.model.Manifest;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Write Ballerina.lock to target after every build.
 *
 * @since 0.973.1
 */
public class LockFileWriter {
    private static final CompilerContext.Key<LockFileWriter> LOCK_FILE_WRITER_KEY = new CompilerContext.Key<>();
    private final SourceDirectory sourceDirectory;
    private TreeMap<PackageID, LockFilePackage> ballerinaLockPackages = new TreeMap<>
            (Comparator.comparing((PackageID p) -> p.orgName.value)
                       .thenComparing(p -> p.name.value)
                       .thenComparing(p -> p.version.value)
            );
    private List<String> entryPackages = new ArrayList<>();

    /**
     * Constructor of LockFileWriter.
     *
     * @param context compiler context
     */
    private LockFileWriter(CompilerContext context) {
        context.put(LOCK_FILE_WRITER_KEY, this);
        this.sourceDirectory = context.get(SourceDirectory.class);
        if (this.sourceDirectory == null) {
            throw new IllegalArgumentException("source directory has not been initialized");
        }
    }

    /**
     * Get an instance of the LockFileWriter.
     *
     * @param context compiler context
     * @return instance of the LockFileWriter
     */
    public static LockFileWriter getInstance(CompilerContext context) {
        LockFileWriter lockFileWriter = context.get(LOCK_FILE_WRITER_KEY);
        if (lockFileWriter == null) {
            lockFileWriter = new LockFileWriter(context);
        }
        return lockFileWriter;
    }

    /**
     * Get entry package dependencies.
     *
     * @param packageSymbol package symbol
     */
    public void addEntryPkg(BPackageSymbol packageSymbol) {
        String entryPkg = packageSymbol.pkgID.name.value;
        if (packageSymbol.pkgID.isUnnamed) {
            entryPkg = packageSymbol.pkgID.sourceFileName.value;
        }
        entryPackages.add(entryPkg);
        getPkgDependencies(packageSymbol);
    }

    /**
     * Generate list of dependencies of package.
     *
     * @param packageSymbol package symbol
     */
    private void getPkgDependencies(BPackageSymbol packageSymbol) {
        LockFilePackage lockFilePackage = new LockFilePackage(packageSymbol.pkgID.orgName.value,
                                                              packageSymbol.pkgID.name.value,
                                                              packageSymbol.pkgID.version.value);
        if (packageSymbol.pkgID.isUnnamed) {
            lockFilePackage = new LockFilePackage(packageSymbol.pkgID.orgName.value,
                                                  packageSymbol.pkgID.sourceFileName.value,
                                                  packageSymbol.pkgID.version.value);
        }
        List<BPackageSymbol> importPackages = getImportPackages(packageSymbol);
        lockFilePackage.setDependencyPackages(getImports(importPackages));

        ballerinaLockPackages.put(packageSymbol.pkgID, lockFilePackage);
        if (importPackages.size() > 0) {
            for (BPackageSymbol importPackage : importPackages) {
                getPkgDependencies(importPackage);
            }
        }
    }

    /**
     * Get import package list.
     *
     * @param packageSymbols list of package symbols of imported packages
     * @return list of packages
     */
    private List<LockFilePackage> getImports(List<BPackageSymbol> packageSymbols) {
        return packageSymbols.stream().map(symbol -> new LockFilePackage(symbol.pkgID.orgName.value,
                                                                         symbol.pkgID.name.value,
                                                                         symbol.pkgID.version.value))
                             .collect(Collectors.toList());
    }

    /**
     * Get external import packages of package.
     *
     * @param packageNode packafe node
     * @return import package list
     */
    private List<BPackageSymbol> getImportPackages(BPackageSymbol packageNode) {
        return packageNode.imports.stream()
                                  .filter(pkg -> !pkg.pkgID.orgName.value.equals(LockFileConstants.BALLERINA))
                                  .collect(Collectors.toList());
    }

    /**
     * Generate the project description.
     *
     * @param manifest manfiest object
     * @return string with the project description
     */
    private StringBuilder generateProjectDesc(Manifest manifest) {
        return new StringBuilder()
                .append("[" + LockFileConstants.LOCK_FILE_PROJECT + "]").append("\n")
                .append(LockFileConstants.LOCK_FILE_PACKAGE_NAME).append(" = ")
                .append(String.format("\"%s\"", manifest.getProject().getOrgName())).append("\n")
                .append(LockFileConstants.LOCK_FILE_PACKAGE_VERSION).append(" = ")
                .append(String.format("\"%s\"", manifest.getProject().getVersion())).append("\n")
                .append(LockFileConstants.LOCK_FILE_VERSION).append(" = ")
                .append(String.format("\"%s\"", "1")).append("\n")
                .append(LockFileConstants.BALLERINA_VERSION).append(" = ")
                .append(String.format("\"%s\"", RepoUtils.getBallerinaVersion())).append("\n")
                .append(LockFileConstants.LOCK_FILE_PACKAGES).append(" = [")
                .append(entryPackages.stream()
                                     .map(str -> " \"" + str + "\" ")
                                     .collect(Collectors.joining(","))).append("]\n");
    }

    /**
     * Write Ballerina.lock file.
     *
     * @param manifest manifest object
     */
    public void writeLockFile(Manifest manifest) {
        List<StringBuilder> builderList = new ArrayList<>();
        for (LockFilePackage lockFilePackage : ballerinaLockPackages.values()) {

            StringBuilder builder = new StringBuilder().append("[[").append(LockFileConstants.LOCK_FILE_PACKAGE)
                                                       .append("]] \n");

            if (!lockFilePackage.getOrg().isEmpty() && !lockFilePackage.getOrg()
                                                                       .equals(Names.DEFAULT_PACKAGE.getValue())
                    && !lockFilePackage.getOrg().equals(Names.ANON_ORG.getValue())) {
                builder.append(LockFileConstants.LOCK_FILE_ORG_NAME)
                       .append(" = ")
                       .append(String.format("\"%s\"", lockFilePackage.getOrg()))
                       .append("\n");
            }
            builder.append(LockFileConstants.LOCK_FILE_PACKAGE_NAME)
                   .append(" = ")
                   .append(String.format("\"%s\"", lockFilePackage.getName()))
                   .append("\n");
            if (!lockFilePackage.getVersion().isEmpty() && !lockFilePackage.getVersion()
                                                                           .equals(Names.DEFAULT_VERSION.getValue())) {
                builder.append(LockFileConstants.LOCK_FILE_PACKAGE_VERSION)
                       .append(" = ")
                       .append(String.format("\"%s\"", lockFilePackage.getVersion()))
                       .append("\n");
            }
            if (!lockFilePackage.getDependencies().isEmpty()) {
                builder.append(LockFileConstants.LOCK_FILE_IMPORTS)
                       .append(" = [")
                       .append(lockFilePackage.getDependencies()
                                              .stream()
                                              .map(this::getImportAsString)
                                              .collect(Collectors.joining(",")))
                       .append("]").append("\n");

            } else {
                builder.append("");
            }
            builderList.add(builder);
        }

        Path ballerinaLockFilePath = this.sourceDirectory.getPath()
                                         .resolve(Paths.get(ProjectDirConstants.LOCK_FILE_NAME));
        builderList.add(0, generateProjectDesc(manifest));

        StringBuilder sb = new StringBuilder(builderList.size() * 10);
        for (StringBuilder line : builderList) {
            sb.append(line);
            sb.append("\n");
        }
        try {
            Files.write(ballerinaLockFilePath, sb.toString().getBytes());
        } catch (IOException ignore) {
        }
    }

    /**
     * Get import of package as a string.
     *
     * @return import package as a string
     */
    private String getImportAsString(LockFilePackage lockFilePackage) {
        return "{" + LockFileConstants.LOCK_FILE_ORG_NAME + "=\"" + lockFilePackage.getOrg() + "\", " +
                LockFileConstants.LOCK_FILE_PACKAGE_NAME + "=\"" + lockFilePackage.getName() + "\"," +
                LockFileConstants.LOCK_FILE_PACKAGE_VERSION + "=\"" + lockFilePackage.getVersion() + "\"}";
    }
}

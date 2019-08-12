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

import com.moandjiezana.toml.TomlWriter;
import org.ballerinalang.toml.model.LockFileImport;
import org.ballerinalang.toml.model.LockFile;
import org.ballerinalang.toml.model.Manifest;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Write Ballerina.lock to target after every build.
 *
 * @since 0.973.1
 */
public class LockFileWriter {
    private static final CompilerContext.Key<LockFileWriter> LOCK_FILE_WRITER_KEY = new CompilerContext.Key<>();
    private LockFile lockFile = new LockFile();

    /**
     * Constructor of LockFileWriter.
     *
     * @param context compiler context
     */
    private LockFileWriter(CompilerContext context) {
        context.put(LOCK_FILE_WRITER_KEY, this);
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
     * Generate list of dependencies of package.
     *
     * @param packageSymbol package symbol
     */
    private void getPkgDependencies(BPackageSymbol packageSymbol) {
        LockFileImport module = new LockFileImport(packageSymbol.pkgID.orgName.value,
                                                              packageSymbol.pkgID.name.value,
                                                              packageSymbol.pkgID.version.value);
        List<BPackageSymbol> importPackages = getImportPackages(packageSymbol);
        module.setImports(getImports(importPackages));
        this.lockFile.getImports().add(module);
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
    private List<LockFileImport> getImports(List<BPackageSymbol> packageSymbols) {
        return packageSymbols.stream().map(symbol -> new LockFileImport(symbol.pkgID.orgName.value,
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
                                  .filter(pkg -> !pkg.pkgID.orgName.value.equals(LockFileConstants.BALLERINAX))
                                  .collect(Collectors.toList());
    }

    /**
     * Generate the project description.
     *
     * @param manifest Manifest object
     */
    private void updateProjectBlock(Manifest manifest) {
        this.lockFile.setOrgName(manifest.getProject().getOrgName());
        this.lockFile.setVersion(manifest.getProject().getVersion());
        this.lockFile.setLockfileVersion("1");
        this.lockFile.setBallerinaVersion(RepoUtils.getBallerinaVersion());
    }
    
    private void updateDependencies(List<BLangPackage> modules) {
        // update this.ballerinaLockModules with dependencies.
        for (BLangPackage module : modules) {
            getPkgDependencies(module.symbol);
        }
    }
    
    /**
     * Write Ballerina.lock file.
     *
     * @param manifest     Manifest object.
     * @param modules      Modules to lock dependencies.
     * @param lockFilePath Path to the lock file.
     */
    public void writeLockFile(Manifest manifest, List<BLangPackage> modules, Path lockFilePath) {
        updateProjectBlock(manifest);
        updateDependencies(modules);
        try {
            TomlWriter tomlLockWriter = new TomlWriter();
            String tomlString = tomlLockWriter.write(this.lockFile);
            Files.write(lockFilePath, tomlString.getBytes());
        } catch (IOException ignore) {
        }
    }
}

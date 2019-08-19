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
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.LockFile;
import org.ballerinalang.toml.model.LockFileImport;
import org.ballerinalang.toml.model.Manifest;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Write Ballerina.lock to target after every build.
 *
 * @since 0.973.1
 */
public class LockFileWriter {
    private static final CompilerContext.Key<LockFileWriter> LOCK_FILE_WRITER_KEY = new CompilerContext.Key<>();
    private final Manifest manifest;
    private LockFile lockFile = new LockFile();

    /**
     * Constructor of LockFileWriter.
     *
     * @param context compiler context
     * @param manifest The manifest of the project(Ballerina.toml
     */
    private LockFileWriter(CompilerContext context, Manifest manifest) {
        context.put(LOCK_FILE_WRITER_KEY, this);
        this.manifest = manifest;
    }

    /**
     * Get an instance of the LockFileWriter.
     *
     * @param context compiler context
     * @param manifest The manifest of the project(Ballerina.toml)
     * @return instance of the LockFileWriter
     */
    public static LockFileWriter getInstance(CompilerContext context, Manifest manifest) {
        LockFileWriter lockFileWriter = context.get(LOCK_FILE_WRITER_KEY);
        if (lockFileWriter == null) {
            lockFileWriter = new LockFileWriter(context, manifest);
        }
        return lockFileWriter;
    }
    
    /**
     * Generate list of imports of dependencies.
     *
     * @param moduleSymbol Module symbol.
     */
    private void getPkgDependencies(BPackageSymbol moduleSymbol) {
        if (moduleSymbol.pkgID.version.value.isEmpty() || "*".equals(moduleSymbol.pkgID.version.value)) {
            return;
        }
        
        Optional<Dependency> hasPathDependency = manifest.getDependencies().stream()
                .filter(dep -> dep.getOrgName().equals(moduleSymbol.pkgID.orgName.value) &&
                               dep.getModuleName().equals(moduleSymbol.pkgID.name.value) &&
                               null != dep.getMetadata().getPath())
                .findFirst();
        
        if (!hasPathDependency.isPresent()) {
            LockFileImport module = new LockFileImport(moduleSymbol.pkgID.orgName.value,
                    moduleSymbol.pkgID.name.value,
                    moduleSymbol.pkgID.version.value);
            List<BPackageSymbol> importPackages = moduleSymbol.imports;
            module.setImports(getImports(importPackages));
            this.lockFile.getImports().add(module);
            if (importPackages.size() > 0) {
                for (BPackageSymbol importPackage : importPackages) {
                    getPkgDependencies(importPackage);
                }
            }
        }
    }
    
    /**
     * Get import module list as {@link LockFileImport}.
     *
     * @param moduleSymbols list of module symbols of imported modules
     * @return list of packages
     */
    private List<LockFileImport> getImports(List<BPackageSymbol> moduleSymbols) {
        return moduleSymbols.stream().map(symbol -> new LockFileImport(symbol.pkgID.orgName.value,
                                                                         symbol.pkgID.name.value,
                                                                         symbol.pkgID.version.value))
                             .collect(Collectors.toList());
    }
    
    /**
     * Update the project of the lock file.
     */
    private void updateProject() {
        this.lockFile.setOrgName(this.manifest.getProject().getOrgName());
        this.lockFile.setVersion(this.manifest.getProject().getVersion());
        this.lockFile.setLockfileVersion("1.0.0");
        this.lockFile.setBallerinaVersion(RepoUtils.getBallerinaVersion());
    }
    
    /**
     * Update module names of the lockfile model.
     *
     * @param modules Module objects.
     */
    private void updateDependencies(List<BLangPackage> modules) {
        // update this.ballerinaLockModules with dependencies.
        for (BLangPackage module : modules) {
            getPkgDependencies(module.symbol);
        }
    }
    
    /**
     * Write Ballerina.lock file overwriting existing Ballerina.lock file.
     *
     * @param modules      Modules to lock dependencies.
     * @param lockFilePath Path to the lock file.
     */
    public void writeLockFile(List<BLangPackage> modules, Path lockFilePath) {
        updateProject();
        updateDependencies(modules);
        try {
            TomlWriter tomlLockWriter = new TomlWriter();
            String tomlString = tomlLockWriter.write(this.lockFile);
            Files.write(lockFilePath, tomlString.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ignore) {
            // ignore
        }
    }
}

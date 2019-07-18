/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompiledPackage;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.wso2.ballerinalang.compiler.codegen.CodeGenerator;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.programfile.PackageFileWriter;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BIR_CACHE_DIR_NAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BIR_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.CACHES_DIR_NAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.TARGET_DIR_NAME;

/**
 * Write a bir to cache.
 *
 * @since 0.965.0
 */
public class BIRFileWriter {
    private static final CompilerContext.Key<BIRFileWriter> BIR_FILE_WRITER_KEY =
            new CompilerContext.Key<>();

    private final CodeGenerator codeGenerator;
    private final SourceDirectory sourceDirectory;
    private final Manifest manifest;
    private final CompilerPhase compilerPhase;

    public static BIRFileWriter getInstance(CompilerContext context) {
        BIRFileWriter binaryFileWriter = context.get(BIR_FILE_WRITER_KEY);
        if (binaryFileWriter == null) {
            binaryFileWriter = new BIRFileWriter(context);
        }
        return binaryFileWriter;
    }

    private BIRFileWriter(CompilerContext context) {
        context.put(BIR_FILE_WRITER_KEY, this);
        this.codeGenerator = CodeGenerator.getInstance(context);
        this.sourceDirectory = context.get(SourceDirectory.class);
        if (this.sourceDirectory == null) {
            throw new IllegalArgumentException("source directory has not been initialized");
        }
        this.compilerPhase = CompilerOptions.getInstance(context).getCompilerPhase();
        this.manifest = ManifestProcessor.getInstance(context).getManifest();
    }

    public void write(BLangPackage module) {
        BPackageSymbol symbol = module.symbol;
        PackageID packageID = symbol.pkgID;

        // Filter out packages which loaded from BALOs
        CompiledPackage compiledPackage = symbol.compiledPackage;
        if (compiledPackage.getKind() == CompiledPackage.Kind.FROM_BINARY) {
            return;
        }

        // Filter out unnamed packages
        if (packageID.isUnnamed) {
            return;
        }

        // Get the project directory
        Path projectDirectory = this.sourceDirectory.getPath();
        // Check if it is a valid project
        if (!ProjectDirs.isProject(projectDirectory)) {
            // Usually this scenario should be avoided from higher level
            // if this happens we ignore
            return;
        }

        // Check if the module is part of the project
        String moduleName = module.packageID.name.value;
        if (!ProjectDirs.isModuleExist(projectDirectory, moduleName)) {
            return;
        }

        // If so write to project cache
        try {
            writeBIRToProjectCache(module);
        } catch (IOException e) {
            throw new BLangCompilerException("Unable to save bir for module `"
                    + moduleName + "` : " + e.getMessage(), e);
        }
    }

    private void writeImportsToHomeCache(List<BPackageSymbol> imports) {
        for (BPackageSymbol bimport : imports) {
            PackageID id = bimport.pkgID;
            Path cacheDir = RepoUtils.createAndGetHomeReposPath().resolve(ProjectDirConstants.BIR_CACHE_DIR_NAME)
                    .resolve(id.orgName.value).resolve(id.name.value).resolve(id.version.value);
            try {
                Files.createDirectories(cacheDir);
                Path birFile = cacheDir.resolve(id.name.value + BLANG_COMPILED_PKG_BIR_EXT);
                byte[] pkgBirBinaryContent = PackageFileWriter.writePackage(bimport.birPackageFile);
                Files.write(birFile, pkgBirBinaryContent);
                writeImportsToHomeCache(bimport.imports);
            } catch (IOException e) {
                String msg = "error writing the compiled module(bir) of '" +
                        id.name.value + "' to '" + cacheDir + "': " + e.getMessage();
                throw new BLangCompilerException(msg, e);
            }
        }
    }

    private void writeBIRToProjectCache(BLangPackage module) throws IOException {
        // Find the module name
        String orgName = module.packageID.orgName.value;
        String moduleName = module.packageID.name.value;
        String birFileName = moduleName + BLANG_COMPILED_PKG_BIR_EXT;
        // Create the cache directory
        Path cacheDir = createCacheDirectory(orgName, moduleName);
        Path birFile = cacheDir.resolve(birFileName);
        // Write the bir file
        //TODO: Investigate why birPackageFile can be null
        if (module.symbol.birPackageFile == null) {
            return;
        }
        try {
            byte[] pkgBirBinaryContent = PackageFileWriter.writePackage(module.symbol.birPackageFile);
            Files.write(birFile, pkgBirBinaryContent);
            writeImportsToHomeCache(module.symbol.imports);
        } catch (IOException e) {
            String msg = "error writing the compiled module(bir) of '" +
                    module.packageID + "' to '" + cacheDir + "': " + e.getMessage();
            throw new BLangCompilerException(msg, e);
        }
    }

    private Path createCacheDirectory(String orgName, String moduleName) throws IOException {
        // Get project source root
        Path projectDirectory = this.sourceDirectory.getPath();
        String versionNo = manifest.getProject().getVersion();
        // Cache directory will be created up to this level at high level
        Path cacheDir = projectDirectory.resolve(TARGET_DIR_NAME)
                .resolve(CACHES_DIR_NAME)
                .resolve(BIR_CACHE_DIR_NAME)
                .resolve(orgName)
                .resolve(moduleName)
                .resolve(versionNo);
        Files.createDirectories(cacheDir);
        return cacheDir;
    }

}

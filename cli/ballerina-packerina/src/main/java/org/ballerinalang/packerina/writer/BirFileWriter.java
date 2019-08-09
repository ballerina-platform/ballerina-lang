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

package org.ballerinalang.packerina.writer;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompiledPackage;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile;
import org.wso2.ballerinalang.programfile.PackageFileWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Write a bir to cache.
 *
 * @since 0.965.0
 */
public class BirFileWriter {
    private static final CompilerContext.Key<BirFileWriter> BIR_FILE_WRITER_KEY =
            new CompilerContext.Key<>();

    public static BirFileWriter getInstance(CompilerContext context) {
        BirFileWriter binaryFileWriter = context.get(BIR_FILE_WRITER_KEY);
        if (binaryFileWriter == null) {
            binaryFileWriter = new BirFileWriter(context);
        }
        return binaryFileWriter;
    }

    private BirFileWriter(CompilerContext context) {
        context.put(BIR_FILE_WRITER_KEY, this);
    }

    public void write(BLangPackage module, Path birFilePath) {
        BPackageSymbol symbol = module.symbol;

        // Filter out packages which loaded from BALOs
        CompiledPackage compiledPackage = symbol.compiledPackage;
        if (compiledPackage.getKind() == CompiledPackage.Kind.FROM_BINARY) {
            return;
        }

        // Check if the module is part of the project
//        String moduleName = module.packageID.name.value;
//        if (!ProjectDirs.isModuleExist(projectDirectory, moduleName)) {
//            return;
//        }

        // If so write to project cache
        writeBIRToProjectCache(module, birFilePath);
    }
    
    /**
     * Write the compiled module to a bir file.
     *
     * @param module      The compiled module.
     * @param birFilePath The path to the bir file.
     */
    private void writeBIRToProjectCache(BLangPackage module, Path birFilePath) {
        // Create the cache directory
        // Write the bir file
        //TODO: Investigate why birPackageFile can be null
        if (module.symbol.birPackageFile == null) {
            return;
        }
        try {
            byte[] pkgBirBinaryContent = PackageFileWriter.writePackage(module.symbol.birPackageFile);
            Files.write(birFilePath, pkgBirBinaryContent);
        } catch (IOException e) {
            String msg = "error writing the compiled module(bir) of '" +
                    module.packageID + "' to '" + birFilePath + "': " + e.getMessage();
            throw new BLangCompilerException(msg, e);
        }
    }

    public void writeBIRToPath(CompiledBinaryFile.BIRPackageFile birPackageFile, PackageID id, Path birFilePath) {

        try {
            byte[] pkgBirBinaryContent = PackageFileWriter.writePackage(birPackageFile);
            Files.write(birFilePath, pkgBirBinaryContent);
        } catch (IOException e) {
            String msg = "error writing the compiled module(bir) of '" +
                    id + "' to '" + birFilePath + "': " + e.getMessage();
            throw new BLangCompilerException(msg, e);
        }
    }

}

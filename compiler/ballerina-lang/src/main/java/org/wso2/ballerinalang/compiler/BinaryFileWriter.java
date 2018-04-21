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

import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.compiler.plugins.CompilerPlugin;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompiledPackage;
import org.ballerinalang.repository.CompilerOutputEntry;
import org.wso2.ballerinalang.compiler.codegen.CodeGenerator;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile.ProgramFile;
import org.wso2.ballerinalang.programfile.PackageFileWriter;
import org.wso2.ballerinalang.programfile.ProgramFileWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ServiceLoader;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PROG_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_SOURCE_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.DOT_BALLERINA_DIR_NAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME;

/**
 * Write a compiled executable program(.balx) or a compiled package(balo.) to a file.
 *
 * @since 0.965.0
 */
public class BinaryFileWriter {
    private static final CompilerContext.Key<BinaryFileWriter> BINARY_FILE_WRITER_KEY =
            new CompilerContext.Key<>();

    private final CodeGenerator codeGenerator;
    private final SourceDirectory sourceDirectory;

    public static BinaryFileWriter getInstance(CompilerContext context) {
        BinaryFileWriter binaryFileWriter = context.get(BINARY_FILE_WRITER_KEY);
        if (binaryFileWriter == null) {
            binaryFileWriter = new BinaryFileWriter(context);
        }
        return binaryFileWriter;
    }

    private BinaryFileWriter(CompilerContext context) {
        context.put(BINARY_FILE_WRITER_KEY, this);
        this.codeGenerator = CodeGenerator.getInstance(context);
        this.sourceDirectory = context.get(SourceDirectory.class);
        if (this.sourceDirectory == null) {
            throw new IllegalArgumentException("source directory has not been initialized");
        }
    }

    public ProgramFile genExecutable(BLangPackage entryPackageNode) {
        return this.codeGenerator.generateBALX(entryPackageNode);
    }

    public void write(BLangPackage packageNode) {
        writeLibraryPackage(packageNode);
        writeExecutableBinary(packageNode);
    }

    public void write(BLangPackage packageNode, String fileName) {
        // TODO Reuse binary content in PackageFile when writing the program file..
        writeLibraryPackage(packageNode);
        writeExecutableBinary(packageNode, fileName);
    }

    public void writeExecutableBinary(BLangPackage packageNode) {
        String fileName = getOutputFileName(packageNode, BLANG_COMPILED_PROG_EXT);
        writeExecutableBinary(packageNode, fileName);
    }

    public void writeExecutableBinary(BLangPackage packageNode, String fileName) {
        // Filter out package which doesn't have entry points
        if (!packageNode.symbol.entryPointExists) {
            return;
        }

        String execFileName = cleanupExecFileName(fileName);

        // Generate code for the given executable
        ProgramFile programFile = this.codeGenerator.generateBALX(packageNode);
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        try {
            ProgramFileWriter.writeProgram(programFile, byteArrayOS);
        } catch (IOException e) {
            throw new BLangCompilerException("error writing program file '" + execFileName + "'", e);
        }

        final Path execFilePath = this.sourceDirectory.saveCompiledProgram(new ByteArrayInputStream(byteArrayOS
                .toByteArray()), execFileName);
        ServiceLoader<CompilerPlugin> processorServiceLoader = ServiceLoader.load(CompilerPlugin.class);
        processorServiceLoader.forEach(plugin -> {
            plugin.codeGenerated(execFilePath);
        });
    }

    public void writeLibraryPackage(BLangPackage packageNode) {
        String fileName = getOutputFileName(packageNode, BLANG_COMPILED_PKG_EXT);
        writeLibraryPackage(packageNode, fileName);
    }

    public void writeLibraryPackage(BLangPackage packageNode, String compiledPackageFileName) {
        // Filter out packages which loaded from BALOs
        CompiledPackage compiledPackage = packageNode.symbol.compiledPackage;
        if (compiledPackage.getKind() == CompiledPackage.Kind.FROM_BINARY) {
            return;
        }

        // Filter out unnamed packages
        if (packageNode.packageID.isUnnamed) {
            return;
        }

        if (compiledPackageFileName == null || compiledPackageFileName.isEmpty()) {
            throw new IllegalArgumentException("invalid target file name");
        }

        if (!compiledPackageFileName.endsWith(BLANG_COMPILED_PKG_EXT)) {
            compiledPackageFileName += BLANG_COMPILED_PKG_EXT;
        }

        Path destDirPath = getPackageDirPathInProjectRepo(packageNode.packageID);
        try {
            addPackageBinaryContent(packageNode.packageID,
                    packageNode.symbol.packageFile, compiledPackage);
            this.sourceDirectory.saveCompiledPackage(compiledPackage, destDirPath, compiledPackageFileName);
        } catch (IOException e) {
            String msg = "error writing the compiled package(balo) of '" +
                    packageNode.packageID + "' to '" + destDirPath + "': " + e.getMessage();
            throw new BLangCompilerException(msg, e);
        }
    }


    // private methods

    private String getOutputFileName(BLangPackage packageNode, String suffix) {
        if (packageNode.packageID.isUnnamed) {
            String sourceFileName = packageNode.packageID.sourceFileName.value;
            if (sourceFileName.endsWith(BLANG_SOURCE_EXT)) {
                sourceFileName = StringUtils.removeEnd(sourceFileName,
                        BLANG_SOURCE_EXT).concat(BLANG_COMPILED_PROG_EXT);
            }
            return sourceFileName;
        }

        return packageNode.packageID.name.value + suffix;
    }

    private Path getPackageDirPathInProjectRepo(PackageID pkgId) {
        Path relativePkgPath = Paths.get(DOT_BALLERINA_DIR_NAME, DOT_BALLERINA_REPO_DIR_NAME,
                pkgId.getOrgName().getValue(), pkgId.getName().getValue(), pkgId.getPackageVersion().getValue());
        return this.sourceDirectory.getPath().resolve(relativePkgPath);
    }

    private void addPackageBinaryContent(PackageID pkgId,
                                         CompiledBinaryFile.PackageFile packageFile,
                                         CompiledPackage compiledPackage) throws IOException {
        byte[] pkgBinaryContent = PackageFileWriter.writePackage(packageFile);
        ByteArrayBasedCompiledPackageEntry pkgBinaryEntry = new ByteArrayBasedCompiledPackageEntry(
                pkgBinaryContent, getPackageBinaryName(pkgId), CompilerOutputEntry.Kind.OBJ);
        compiledPackage.setPackageBinaryEntry(pkgBinaryEntry);
    }

    private String getPackageBinaryName(PackageID packageID) {
        return packageID.getName().value + ProjectDirConstants.BLANG_COMPILED_PKG_BINARY_EXT;
    }

    private String cleanupExecFileName(String fileName) {
        String updatedFileName = fileName;
        if (updatedFileName == null || updatedFileName.isEmpty()) {
            throw new IllegalArgumentException("invalid target file name");
        }

        if (updatedFileName.endsWith(BLANG_SOURCE_EXT)) {
            updatedFileName = updatedFileName.substring(0,
                    updatedFileName.length() - BLANG_SOURCE_EXT.length());
        }

        if (!updatedFileName.endsWith(BLANG_COMPILED_PROG_EXT)) {
            updatedFileName += BLANG_COMPILED_PROG_EXT;
        }
        return updatedFileName;
    }
}

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
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompiledPackage;
import org.ballerinalang.repository.CompilerOutputEntry;
import org.ballerinalang.repository.CompilerOutputEntry.Kind;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile.BIRPackageFile;
import org.wso2.ballerinalang.programfile.PackageFileWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_BIR_EXT;
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
    private static final String JAVA_IO_TMP_DIR = "java.io.tmpdir";
    private static PrintStream outStream = System.out;

    private final SourceDirectory sourceDirectory;
    private final CompilerPhase compilerPhase;

    public static BinaryFileWriter getInstance(CompilerContext context) {
        BinaryFileWriter binaryFileWriter = context.get(BINARY_FILE_WRITER_KEY);
        if (binaryFileWriter == null) {
            binaryFileWriter = new BinaryFileWriter(context);
        }
        return binaryFileWriter;
    }

    private BinaryFileWriter(CompilerContext context) {
        context.put(BINARY_FILE_WRITER_KEY, this);
        this.sourceDirectory = context.get(SourceDirectory.class);
        if (this.sourceDirectory == null) {
            throw new IllegalArgumentException("source directory has not been initialized");
        }
        this.compilerPhase = CompilerOptions.getInstance(context).getCompilerPhase();
    }

    public void write(BLangPackage packageNode) {
        if (packageNode.symbol.entryPointExists) {
            writeExecutableBinary(packageNode);
        }
        writeLibraryPackage(packageNode);
    }

    public void write(BLangPackage packageNode, String fileName) {
        if (this.compilerPhase == CompilerPhase.BIR_GEN) {
            if (packageNode.packageID.isUnnamed) {
                writeBIR(packageNode, fileName);
            } else {
                writePackageBIR(packageNode);
            }
        }
        // TODO Reuse binary content in PackageFile when writing the program file..
        if (packageNode.symbol.entryPointExists) {
            outStream.println("Generating executable");
            writeExecutableBinary(packageNode, fileName);
        }
        writeLibraryPackage(packageNode);
    }

    private void writeBIR(BLangPackage packageNode, String fileName) {
        if (packageNode.symbol.birPackageFile != null) {
            String birFilename = cleanupExecFileName(fileName, BLANG_COMPILED_PKG_EXT);
            Path destDirPath = createAndGetTempDir(packageNode); // bir will be written to a temp directory.
            try {
                addFileBirContent(cleanupExecFileName(fileName, BLANG_COMPILED_PKG_BIR_EXT),
                        packageNode.symbol.birPackageFile, packageNode.symbol.compiledPackage);
                this.sourceDirectory.saveCompiledPackage(packageNode.symbol.compiledPackage, destDirPath, birFilename);
            } catch (IOException e) {
                String msg = "error writing the compiled module(bir) of '" +
                        packageNode.packageID + "' to '" + destDirPath + "': " + e.getMessage();
                throw new BLangCompilerException(msg, e);
            }
        }
    }

    private void writePackageBIR(BLangPackage packageNode) {
        if (packageNode.symbol.birPackageFile != null) {
            String birFilename = cleanupExecFileName(packageNode.packageID.name.value, BLANG_COMPILED_PKG_EXT);
            Path destDirPath = getPackageDirPathInProjectRepo(packageNode.packageID);
            try {
                addPackageBirContent(packageNode.packageID,
                        packageNode.symbol.birPackageFile, packageNode.symbol.compiledPackage);
                this.sourceDirectory.saveCompiledPackage(packageNode.symbol.compiledPackage, destDirPath, birFilename);
            } catch (IOException e) {
                String msg = "error writing the compiled module(bir) of '" +
                        packageNode.packageID + "' to '" + destDirPath + "': " + e.getMessage();
                throw new BLangCompilerException(msg, e);
            }
        }
    }

    private void writeExecutableBinary(BLangPackage packageNode) {
        String fileName = getOutputFileName(packageNode, BLANG_COMPILED_PROG_EXT);
        writeExecutableBinary(packageNode, fileName);
    }

    private void writeExecutableBinary(BLangPackage packageNode, String fileName) {

        if (this.compilerPhase == CompilerPhase.BIR_GEN && packageNode.jarBinaryContent != null) {
            String jarFilename = cleanupExecFileName(fileName, BLANG_COMPILED_JAR_EXT);
            this.sourceDirectory.saveCompiledProgram(new ByteArrayInputStream(packageNode.jarBinaryContent),
                    jarFilename);
            return;
        }
        String birFilename = cleanupExecFileName(fileName, BLANG_COMPILED_PKG_EXT);
        Path destDirPath = createAndGetTempDir(packageNode); // bir will be written to a temp directory.
        try {
            addFileBirContent(cleanupExecFileName(fileName, BLANG_COMPILED_PKG_BIR_EXT),
                    packageNode.symbol.birPackageFile, packageNode.symbol.compiledPackage);
            this.sourceDirectory.saveCompiledPackage(packageNode.symbol.compiledPackage, destDirPath, birFilename);
        } catch (IOException e) {
            String msg = "error writing the compiled module(bir) of '" +
                    packageNode.packageID + "' to '" + destDirPath + "': " + e.getMessage();
            throw new BLangCompilerException(msg, e);
        }
    }

    private void writeLibraryPackage(BLangPackage packageNode) {
        String fileName = getOutputFileName(packageNode, BLANG_COMPILED_PKG_EXT);
        writeLibraryPackage(packageNode.symbol, fileName);
    }

    public void writeLibraryPackage(BPackageSymbol symbol, String compiledPackageFileName) {
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

        if (compiledPackageFileName == null || compiledPackageFileName.isEmpty()) {
            throw new IllegalArgumentException("invalid target file name");
        }

        if (!compiledPackageFileName.endsWith(BLANG_COMPILED_PKG_EXT)) {
            compiledPackageFileName += BLANG_COMPILED_PKG_EXT;
        }

        Path destDirPath = getPackageDirPathInProjectRepo(packageID);
        try {
            if (symbol.birPackageFile != null) {
                addPackageBirContent(packageID, symbol.birPackageFile, compiledPackage);
            }
            if (symbol.packageFile != null) {
                addPackageBinaryContent(packageID, symbol.packageFile, compiledPackage);
            }
            this.sourceDirectory.saveCompiledPackage(compiledPackage, destDirPath, compiledPackageFileName);
        } catch (IOException e) {
            String msg = "error writing the compiled module(balo) of '" +
                    packageID + "' to '" + destDirPath + "': " + e.getMessage();
            throw new BLangCompilerException(msg, e);
        }
    }

    // private methods

    private Path createAndGetTempDir(BLangPackage packageNode) {
        Path tempDir = Paths.get(System.getProperty(JAVA_IO_TMP_DIR))
                .resolve(packageNode.packageID.orgName.value)
                .resolve(packageNode.packageID.version.value)
                .resolve(packageNode.packageID.name.value);

        if (!Files.exists(tempDir)) {
            createDirectory(tempDir);
        }

        return tempDir;
    }

    private void createDirectory(Path tempDir) {
        try {
            Files.createDirectories(tempDir);
        } catch (IOException e) {
            throw new BLangCompilerException("failed create directory '" + tempDir.toString() + "'", e);
        }
    }

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

    private void addPackageBirContent(PackageID pkgId, BIRPackageFile birPackageFile,
                                         CompiledPackage compiledPackage) throws IOException {
        byte[] pkgBirBinaryContent = PackageFileWriter.writePackage(birPackageFile);
        ByteArrayBasedCompiledPackageEntry pkgBinaryEntry = new ByteArrayBasedCompiledPackageEntry(
                pkgBirBinaryContent, getPackageBirName(pkgId), Kind.BIR);
        compiledPackage.setPackageBirEntry(pkgBinaryEntry);
    }

    private void addFileBirContent(String fileName, BIRPackageFile birPackageFile,
                                   CompiledPackage compiledPackage) throws IOException {
        byte[] pkgBirBinaryContent = PackageFileWriter.writePackage(birPackageFile);
        ByteArrayBasedCompiledPackageEntry pkgBinaryEntry = new ByteArrayBasedCompiledPackageEntry(
                pkgBirBinaryContent, fileName, Kind.BIR);
        compiledPackage.setPackageBirEntry(pkgBinaryEntry);
    }

    private String getPackageBinaryName(PackageID packageID) {
        return packageID.getName().value + ProjectDirConstants.BLANG_COMPILED_PKG_BINARY_EXT;
    }

    private String getPackageBirName(PackageID packageID) {
        return packageID.getName().value + BLANG_COMPILED_PKG_BIR_EXT;
    }

    private String cleanupExecFileName(String fileName, String extension) {
        String updatedFileName = fileName;
        if (updatedFileName == null || updatedFileName.isEmpty()) {
            throw new IllegalArgumentException("invalid target file name");
        }

        if (updatedFileName.endsWith(BLANG_SOURCE_EXT)) {
            updatedFileName = updatedFileName.substring(0,
                    updatedFileName.length() - BLANG_SOURCE_EXT.length());
        }

        if (!updatedFileName.endsWith(extension)) {
            updatedFileName += extension;
        }
        return updatedFileName;
    }
}

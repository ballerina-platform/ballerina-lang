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
import org.wso2.ballerinalang.compiler.codegen.CodeGenerator;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.repo.ProjectSourceRepo;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile.ProgramFile;
import org.wso2.ballerinalang.programfile.PackageFileWriter;
import org.wso2.ballerinalang.programfile.ProgramFileWriter;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import static org.wso2.ballerinalang.compiler.packaging.Patten.path;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PACKAGE_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_EXEC_FILE_SUFFIX;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_SOURCE_EXT;


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

    public void writeExecutableBinary(BLangPackage packageNode) {
        String fileName = getOutputFileName(packageNode, BLANG_EXEC_FILE_SUFFIX);
        writeExecutableBinary(packageNode, fileName);

        // Generate balo
        Path path = this.sourceDirectory.getPath();
        if (RepoUtils.hasProjectRepo(path)) {
            ProjectSourceRepo projectSourceRepo = new ProjectSourceRepo(path);
            Patten packageIDPattern = projectSourceRepo.calculate(packageNode.packageID);
            Stream<Path> pathStream = packageIDPattern.convert(projectSourceRepo.getConverterInstance());
            pathStream = Stream.concat(pathStream, packageIDPattern.sibling(path("Package.md")).convert
                    (projectSourceRepo.getConverterInstance()));
            String prjPath = projectSourceRepo.getConverterInstance().toString();
            ZipUtils.generateBalo(packageNode, prjPath, pathStream);
        }
    }

    public void writeExecutableBinary(BLangPackage packageNode, String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("invalid target file name");
        }

        if (!fileName.endsWith(BLANG_EXEC_FILE_SUFFIX)) {
            fileName += BLANG_EXEC_FILE_SUFFIX;
        }

        // Generate code for the given executable
        ProgramFile programFile = this.codeGenerator.generateBALX(packageNode);
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        try {
            ProgramFileWriter.writeProgram(programFile, byteArrayOS);
        } catch (IOException e) {
            throw new BLangCompilerException("error writing program file '" + fileName + "'", e);
        }

        final Path execFilePath = this.sourceDirectory.saveCompiledProgram(new ByteArrayInputStream(byteArrayOS
                .toByteArray()), fileName);
        ServiceLoader<CompilerPlugin> processorServiceLoader = ServiceLoader.load(CompilerPlugin.class);
        processorServiceLoader.forEach(plugin -> {
            plugin.codeGenerated(execFilePath);
        });
    }

    public void writeLibraryPackage(BLangPackage packageNode) {
        String fileName = getOutputFileName(packageNode, BLANG_COMPILED_PACKAGE_FILE_SUFFIX);
        CompiledBinaryFile.PackageFile packageFile = this.codeGenerator.generateBALO(packageNode);
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        try {
            PackageFileWriter.writePackage(packageFile, byteArrayOS);
        } catch (IOException e) {
            throw new BLangCompilerException("error writing package file '" + fileName + "'", e);
        }
        this.sourceDirectory.saveCompiledPackage(new ByteArrayInputStream(byteArrayOS.toByteArray()), fileName);
    }


    // private methods

    private String getOutputFileName(BLangPackage packageNode, String suffix) {
        if (packageNode.packageID.isUnnamed) {
            String sourceFileName = packageNode.packageID.sourceFileName.value;
            if (sourceFileName.endsWith(BLANG_SOURCE_EXT)) {
                sourceFileName = StringUtils.removeEnd(sourceFileName, BLANG_SOURCE_EXT).concat(BLANG_EXEC_FILE_SUFFIX);
            }
            return sourceFileName;
        }

        return packageNode.packageID.name.value + suffix;
    }

}

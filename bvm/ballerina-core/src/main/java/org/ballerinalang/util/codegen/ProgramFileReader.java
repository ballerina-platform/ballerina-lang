/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.util.codegen;

import org.ballerinalang.util.exceptions.BLangRuntimeException;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static org.ballerinalang.util.BLangConstants.BALLERINA_BUILTIN_PKG;
import static org.ballerinalang.util.BLangConstants.BALLERINA_RUNTIME_PKG;
import static org.ballerinalang.util.BLangConstants.MAGIC_NUMBER;
import static org.ballerinalang.util.BLangConstants.VERSION_NUMBER;

/**
 * Reads a Ballerina program from a file.
 *
 * @since 0.90
 */
public class ProgramFileReader {

    private ProgramFile programFile;

    public ProgramFile readProgram(Path programFilePath) throws IOException {
        InputStream fileIS = null;
        try {
            programFile = new ProgramFile();
            programFile.setProgramFilePath(programFilePath);
            fileIS = Files.newInputStream(programFilePath, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);
            BufferedInputStream bufferedIS = new BufferedInputStream(fileIS);
            DataInputStream dataInStream = new DataInputStream(bufferedIS);
            return readProgramInternal(dataInStream);
        } finally {
            if (fileIS != null) {
                fileIS.close();
            }
        }
    }

    public ProgramFile readProgram(InputStream programFileInStream) throws IOException {
        programFile = new ProgramFile();
        DataInputStream dataInStream = new DataInputStream(programFileInStream);
        return readProgramInternal(dataInStream);
    }

    private ProgramFile readProgramInternal(DataInputStream dataInStream) throws IOException {
        loadBuiltinPackage();
        loadRuntimePackage();
        PackageInfoReader pkgInfoReader = new PackageInfoReader(dataInStream, programFile);
        int magicNumber = dataInStream.readInt();
        if (magicNumber != MAGIC_NUMBER) {
            throw new BLangRuntimeException("ballerina: invalid magic number " + magicNumber);
        }

        short version = dataInStream.readShort();
        if (version != VERSION_NUMBER) {
            throw new BLangRuntimeException("ballerina: unsupported program file version " + version);
        }
        programFile.setVersion(version);

        pkgInfoReader.readConstantPool(programFile);
        pkgInfoReader.readEntryPoint();

        // Read PackageInfo entries
        int pkgInfoCount = dataInStream.readShort();
        for (int i = 0; i < pkgInfoCount; i++) {
            PackageInfoReader pkgReader = new PackageInfoReader(dataInStream, programFile);
            pkgReader.readPackageInfo();
        }

        //Resolve program file CP entries
        pkgInfoReader.resolveCPEntries(programFile.getEntryPackage());

        PackageInfo entryPkg = programFile.getPackageInfo(programFile.getEntryPkgName());
        programFile.setEntryPackage(entryPkg);
        entryPkg.setProgramFile(programFile);

        // Read program level attributes
        pkgInfoReader.readAttributeInfoEntries(entryPkg, programFile, programFile);

        // TODO This needs to be moved out of this class
        programFile.initializeGlobalMemArea();
        return programFile;
    }

    private void loadBuiltinPackage() throws IOException {
        PackageFileReader pkgFileReader = new PackageFileReader(this.programFile);
        pkgFileReader.readPackage(BALLERINA_BUILTIN_PKG);
    }

    private void loadRuntimePackage() throws IOException {
        PackageFileReader pkgFileReader = new PackageFileReader(this.programFile);
        pkgFileReader.readPackage(BALLERINA_RUNTIME_PKG);
    }
}

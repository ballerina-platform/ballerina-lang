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
package org.ballerinalang.util.codegen;

import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile.PackageFile;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Reads a Ballerina package from a BALO.
 *
 * @since 0.975.0
 */
public class PackageFileReader {

    private ProgramFile programFile;
    private static final String BALLERINA_HOME = "ballerina.home";

    public PackageFileReader(ProgramFile programFile) {
        this.programFile = programFile;
    }

    public void readPackage(String packageId) {
        // TODO
        if (!"ballerina.builtin".equals(packageId)) {
            throw new UnsupportedOperationException("unsupport package read from balo: " + packageId);
        } 

        try {
            readPackage(getCompiledPackageBinary());
        } catch (IOException e) {
            throw new BLangRuntimeException("error while reading package: " + packageId);
        }
    }

    public void readPackage(Path programFilePath) throws IOException {
        InputStream fileIS = null;
        try {
            this.programFile.setProgramFilePath(programFilePath);
            fileIS = Files.newInputStream(programFilePath, StandardOpenOption.READ, LinkOption.NOFOLLOW_LINKS);
            BufferedInputStream bufferedIS = new BufferedInputStream(fileIS);
            DataInputStream dataInStream = new DataInputStream(bufferedIS);
            readPackageInternal(dataInStream);
        } finally {
            if (fileIS != null) {
                fileIS.close();
            }
        }
    }

    public void readPackage(InputStream programFileInStream) throws IOException {
        DataInputStream dataInStream = new DataInputStream(programFileInStream);
        readPackageInternal(dataInStream);
    }

    private void readPackageInternal(DataInputStream dataInStream) throws IOException {
        PackageInfoReader pkgInfoReader = new PackageInfoReader(dataInStream, this.programFile);
        int magicNumber = dataInStream.readInt();
        if (magicNumber != PackageFile.MAGIC_VALUE) {
            throw new BLangRuntimeException("ballerina: invalid magic number " + magicNumber);
        }

        short version = dataInStream.readShort();
        if (version != PackageFile.LANG_VERSION) {
            throw new BLangRuntimeException("ballerina: unsupported program file version " + version);
        }

        this.programFile.setVersion(version);
        pkgInfoReader.readPackageInfo();
    }

    private InputStream getCompiledPackageBinary() throws IOException {
        String ballerinaHome = System.getProperty(BALLERINA_HOME);
        // TODO: read the zip file and get the binary content
        Path libsPath = Paths.get(ballerinaHome, "lib", "stdlib-compile-0.971.1-SNAPSHOT.zip");
        Path fileToExtract = Paths.get("obj", "builtin.balo");

        ZipFile zipFile = new ZipFile(libsPath.toFile());
        ZipEntry zipEntry = zipFile.getEntry(fileToExtract.toString());
        InputStream is = zipFile.getInputStream(zipEntry);
        return is;
    }
}

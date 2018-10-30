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
import org.wso2.ballerinalang.compiler.util.Names;
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

import static org.ballerinalang.util.BLangConstants.BALLERINA_BUILTIN_PKG_PREFIX;
import static org.ballerinalang.util.BLangConstants.BALLERINA_HOME;
import static org.ballerinalang.util.BLangConstants.BALLERINA_PACKAGE_PREFIX;
import static org.ballerinalang.util.BLangConstants.BLANG_COMPILED_PACKAGE_FILE_SUFFIX;
import static org.ballerinalang.util.BLangConstants.USER_REPO_OBJ_DIRNAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_LIB;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.DOT_BALLERINA_REPO_DIR_NAME;

/**
 * Reads a Ballerina package from a BALO.
 *
 * @since 0.975.0
 */
public class PackageFileReader {

    private ProgramFile programFile;

    public PackageFileReader(ProgramFile programFile) {
        this.programFile = programFile;
    }

    public void readPackage(String packageId) throws IOException {
        if (!packageId.startsWith(BALLERINA_PACKAGE_PREFIX)) {
            throw new UnsupportedOperationException("only internal modules can be loaded at runtime: " + packageId);
        }
        String pkgName = packageId.replaceFirst("^ballerina\\/", "");
        readPackage(getCompiledPackageBinary(pkgName));
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

    private InputStream getCompiledPackageBinary(String pkgName) throws IOException {
        String ballerinaHome = System.getProperty(BALLERINA_HOME);
        Path libsPath = Paths.get(ballerinaHome, BALLERINA_HOME_LIB, DOT_BALLERINA_REPO_DIR_NAME,
                BALLERINA_BUILTIN_PKG_PREFIX, pkgName, Names.DEFAULT_VERSION.value, pkgName + BLANG_COMPILED_PKG_EXT);

        ZipFile zipFile = new ZipFile(libsPath.toFile());
        ZipEntry zipEntry =
                zipFile.getEntry(USER_REPO_OBJ_DIRNAME + "/" + pkgName + BLANG_COMPILED_PACKAGE_FILE_SUFFIX);
        InputStream is = new BufferedInputStream(zipFile.getInputStream(zipEntry));
        return is;
    }
}

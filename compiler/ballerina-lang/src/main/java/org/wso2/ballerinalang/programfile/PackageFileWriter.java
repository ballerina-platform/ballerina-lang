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
package org.wso2.ballerinalang.programfile;

import org.wso2.ballerinalang.programfile.CompiledBinaryFile.PackageFile;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Dump Ballerina {@code PackageFile} model (BALO) to a file.
 *
 * @since 0.963.0
 */
public class PackageFileWriter {

    public static void writePackage(PackageFile packageFile, Path packageFilePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(packageFilePath));
        writePackage(packageFile, bos);
    }

    public static void writePackage(PackageFile packageFile, OutputStream programOutStream) throws IOException {
        DataOutputStream dataOutStream = null;
        try {
            dataOutStream = new DataOutputStream(programOutStream);

            dataOutStream.writeInt(PackageFile.MAGIC_VALUE);
            dataOutStream.writeShort(packageFile.langVersion);

            PackageInfoWriter.writeCP(dataOutStream, packageFile.getConstPoolEntries());
            PackageInfo packageInfo = packageFile.packageInfo;
            dataOutStream.writeInt(packageInfo.nameCPIndex);

            // Emit package dependencies
            dataOutStream.writeShort(packageInfo.importPkgInfoSet.size());
            for (ImportPackageInfo importPkgInfo : packageInfo.importPkgInfoSet) {
                dataOutStream.writeInt(importPkgInfo.nameCPIndex);
            }

            PackageInfoWriter.writePackageInfo(dataOutStream, packageInfo);
            PackageInfoWriter.writeAttributeInfoEntries(dataOutStream, packageFile.getAttributeInfoEntries());

            dataOutStream.flush();
            dataOutStream.close();
        } finally {
            if (dataOutStream != null) {
                dataOutStream.close();
            }
        }
    }
}

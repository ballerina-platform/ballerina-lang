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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Dump Ballerina {@code PackageFile} model (BALO) to a file.
 *
 * @since 0.963.0
 */
public class PackageFileWriter {

    public static byte[] writePackage(PackageInfo packageInfo) throws IOException {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        writePackage(packageInfo, byteArrayOS);
        return byteArrayOS.toByteArray();
    }

    public static void writePackage(PackageInfo packageInfo, OutputStream programOutStream) throws IOException {
        DataOutputStream dataOutStream = null;
        try {
            dataOutStream = new DataOutputStream(programOutStream);
            dataOutStream.writeInt(PackageFile.MAGIC_VALUE);
            dataOutStream.writeShort(PackageFile.LANG_VERSION);

            PackageInfoWriter.writeCP(dataOutStream, packageInfo.getConstPoolEntries());
            dataOutStream.writeInt(packageInfo.nameCPIndex);
            dataOutStream.writeInt(packageInfo.versionCPIndex);

            // Emit package dependencies
            // TODO Move this to the packageInfoWriter class.. This should be part of the packageInfo
            dataOutStream.writeShort(packageInfo.importPkgInfoSet.size());
            for (ImportPackageInfo importPkgInfo : packageInfo.importPkgInfoSet) {
                dataOutStream.writeInt(importPkgInfo.nameCPIndex);
                dataOutStream.writeInt(importPkgInfo.versionCPIndex);
            }

            // Write the package info structure
            PackageInfoWriter.writePackageInfo(dataOutStream, packageInfo);

            dataOutStream.flush();
            dataOutStream.close();
        } finally {
            if (dataOutStream != null) {
                dataOutStream.close();
            }
        }
    }
}

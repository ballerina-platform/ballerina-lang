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

/**
 * Dump Ballerina {@code PackageFile} model (BALO) to a file.
 *
 * @since 0.963.0
 */
public class PackageFileWriter {

    public static byte[] writePackage(PackageFile packageFile) throws IOException {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        try (DataOutputStream dataOutStream = new DataOutputStream(byteArrayOS)) {
            dataOutStream.writeInt(PackageFile.MAGIC_VALUE);
            dataOutStream.writeShort(PackageFile.LANG_VERSION);

            dataOutStream.write(packageFile.pkgBinaryContent);
            return byteArrayOS.toByteArray();
        }
    }
}

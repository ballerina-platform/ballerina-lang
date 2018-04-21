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

import org.wso2.ballerinalang.programfile.CompiledBinaryFile.ProgramFile;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Dump Ballerina program file model (BALX) to a file.
 *
 * @since 0.963.0
 */
public class ProgramFileWriter {

    public static void writeProgram(ProgramFile programFile, OutputStream programOutStream) throws IOException {
        DataOutputStream dataOutStream = null;
        try {
            dataOutStream = new DataOutputStream(programOutStream);

            dataOutStream.writeInt(programFile.getMagicValue());
            dataOutStream.writeShort(programFile.getVersion());

            // Write CP entries of the program
            PackageInfoWriter.writeCP(dataOutStream, programFile.getConstPoolEntries());
            writeEntryPoint(dataOutStream, programFile);

            // Emit package info entries;
            writePackageInfoEntries(programFile, dataOutStream);

            // Write attributes of the of the program
            PackageInfoWriter.writeAttributeInfoEntries(dataOutStream, programFile.getAttributeInfoEntries());
            dataOutStream.flush();
            dataOutStream.close();
        } finally {
            if (dataOutStream != null) {
                dataOutStream.close();
            }
        }
    }

    private static void writeEntryPoint(DataOutputStream dataOutStream,
                                        ProgramFile programFile) throws IOException {
        dataOutStream.writeInt(programFile.entryPkgCPIndex);
        int flags = 0;
        flags = programFile.isMainEPAvailable() ? flags | ProgramFile.EP_MAIN_FLAG : flags;
        flags = programFile.isServiceEPAvailable() ? flags | ProgramFile.EP_SERVICE_FLAG : flags;
        dataOutStream.writeByte(flags);
    }

    private static void writePackageInfoEntries(ProgramFile programFile,
                                                DataOutputStream dataOutStream) throws IOException {
        dataOutStream.writeShort(programFile.packageFileMap.size());
        for (CompiledBinaryFile.PackageFile packageFile : programFile.packageFileMap.values()) {
            dataOutStream.write(packageFile.pkgBinaryContent);
        }
    }
}

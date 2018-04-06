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

import org.ballerinalang.compiler.plugins.CompilerPlugin;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile.ProgramFile;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ServiceLoader;

/**
 * Dump Ballerina program file model (BALX) to a file.
 *
 * @since 0.963.0
 */
public class ProgramFileWriter {

    public static void writeProgram(ProgramFile programFile, Path execFilePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(execFilePath));
        writeProgram(programFile, bos);

        // TODO Fix this properly. Load and invoke compiler plugins
        // TODO This will get properly fixed with the new packerina
        ServiceLoader<CompilerPlugin> processorServiceLoader = ServiceLoader.load(CompilerPlugin.class);
        processorServiceLoader.forEach(plugin -> {
            plugin.codeGenerated(execFilePath);
        });
    }

    public static void writeProgram(ProgramFile programFile, OutputStream programOutStream) throws IOException {
        DataOutputStream dataOutStream = null;
        try {
            dataOutStream = new DataOutputStream(programOutStream);

            dataOutStream.writeInt(programFile.getMagicValue());
            dataOutStream.writeShort(programFile.getVersion());

            PackageInfoWriter.writeCP(dataOutStream, programFile.getConstPoolEntries());
            writeEntryPoint(dataOutStream, programFile);

            // Emit package info entries;
            PackageInfo[] packageInfoEntries = programFile.getPackageInfoEntries();
            dataOutStream.writeShort(packageInfoEntries.length);
            for (PackageInfo packageInfo : packageInfoEntries) {
                PackageInfoWriter.writeCP(dataOutStream, packageInfo.getConstPoolEntries());
                dataOutStream.writeInt(packageInfo.nameCPIndex);
                dataOutStream.writeInt(packageInfo.versionCPIndex);
                PackageInfoWriter.writePackageInfo(dataOutStream, packageInfo);
            }

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
}

/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.compiler.backend.jvm;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.ballerinalang.model.types.TypeKind.STRING;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_SOURCE_EXT;

/**
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "compiler_backend_jvm",
        functionName = "writeExecutableJarToFile",
        args = {
                @Argument(name = "jarFile", type = RECORD),
                @Argument(name = "fileName", type = STRING),
                @Argument(name = "birHome", type = STRING),
        }
)
public class WriteExecutableJarFile extends BlockingNativeCallableUnit {

    private static final String JAR_ENTRIES = "jarEntries";
    private static final String MANIFEST_ENTRIES = "manifestEntries";

    @Override
    public void execute(Context context) {

        BValue jarFile = context.getRefArgument(0);
        String fileName = context.getStringArgument(0);
        String balHome = context.getStringArgument(1);
        Map<String, BValue> jarEntries = ((BMap<String, BValue>) jarFile).getMap();
        try {
            byte[] jarInBytes = getJarContent(jarEntries);
            SourceDirectory sourceDirectory = new FileSystemProjectDirectory(Paths.get(balHome));
            String jarFilename = cleanupExecFileName(fileName);
            sourceDirectory.saveCompiledProgram(new ByteArrayInputStream(jarInBytes), jarFilename);
        } catch (IOException e) {
            throw new BLangCompilerException("jar file generation failed: " + e.getMessage(), e);
        }
    }

    private String cleanupExecFileName(String fileName) {
        String updatedFileName = fileName;
        if (updatedFileName == null || updatedFileName.isEmpty()) {
            throw new IllegalArgumentException("invalid target file name");
        }

        if (updatedFileName.endsWith(BLANG_SOURCE_EXT)) {
            updatedFileName = updatedFileName.substring(0,
                    updatedFileName.length() - BLANG_SOURCE_EXT.length());
        }

        if (!updatedFileName.endsWith(BLANG_COMPILED_JAR_EXT)) {
            updatedFileName += BLANG_COMPILED_JAR_EXT;
        }
        return updatedFileName;
    }

    private static byte[] getJarContent(Map<String, BValue> entries) throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        if (entries.containsKey(MANIFEST_ENTRIES)) {
            Map<String, BValue> manifestEntries = ((BMap<String, BValue>) entries.get(MANIFEST_ENTRIES)).
                    getMap();
            manifestEntries.forEach((k, v) -> manifest.getMainAttributes().
                    put(new Attributes.Name(k), v.stringValue()));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (JarOutputStream target = new JarOutputStream(baos, manifest)) {

            if (!entries.containsKey(JAR_ENTRIES)) {
                throw new BLangCompilerException("no class file entries found in the record");
            }
            Map<String, BValue> jarEntries = ((BMap<String, BValue>) entries.get(JAR_ENTRIES)).getMap();
            for (String entryName : jarEntries.keySet()) {
                byte[] entryContent = ((BValueArray) jarEntries.get(entryName)).getBytes();
                JarEntry entry = new JarEntry(entryName);
                target.putNextEntry(entry);
                target.write(entryContent);
                target.closeEntry();
            }
        }

        return baos.toByteArray();
    }
}

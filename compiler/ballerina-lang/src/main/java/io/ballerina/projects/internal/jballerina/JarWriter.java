/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.projects.internal.jballerina;

import org.apache.commons.compress.archivers.jar.JarArchiveEntry;
import org.wso2.ballerinalang.compiler.CompiledJarFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * Write jar binary content to target path.
 *
 * @since 2.0.0
 */
public class JarWriter {

    public static ByteArrayOutputStream write(CompiledJarFile compiledJarFile, Map<String, byte[]> resources)
            throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        writeJar(compiledJarFile, byteArrayOutputStream, resources);
        return byteArrayOutputStream;
    }

    private static Manifest getManifest(CompiledJarFile compiledJarFile) {
        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        compiledJarFile.getMainClassName().ifPresent(mainClassName ->
                mainAttributes.put(Attributes.Name.MAIN_CLASS, mainClassName));
        return manifest;
    }

    private static void writeJar(CompiledJarFile compiledJarFile, OutputStream outputStream,
                                 Map<String, byte[]> resources) throws IOException {
        Manifest manifest = getManifest(compiledJarFile);
        try (JarOutputStream target = new JarOutputStream(outputStream, manifest)) {
            Map<String, byte[]> jarEntries = compiledJarFile.getJarEntries();
            for (Map.Entry<String, byte[]> keyVal : jarEntries.entrySet()) {
                byte[] entryContent = keyVal.getValue();
                JarEntry entry = new JarEntry(keyVal.getKey());
                target.putNextEntry(entry);
                target.write(entryContent);
                target.closeEntry();
            }

            // Copy resources
            for (Map.Entry<String, byte[]> entry : resources.entrySet()) {
                JarArchiveEntry e = new JarArchiveEntry(entry.getKey());
                target.putNextEntry(e);
                target.write(entry.getValue());
                target.closeEntry();
            }
        }
    }
}

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
package io.ballerina.projects;

import org.wso2.ballerinalang.compiler.CompiledJarFile;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
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
class JarWriter {

    /**
     * Write the compiled module to a jar file.
     *
     * @param compiledJarFile compiled module
     * @param jarFilePath     path to the jar file
     * @throws IOException if jar creation fails
     */
    public static void write(CompiledJarFile compiledJarFile, Path jarFilePath) throws IOException {
        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        compiledJarFile.getMainClassName().ifPresent(mainClassName ->
                mainAttributes.put(Attributes.Name.MAIN_CLASS, mainClassName));

        try (JarOutputStream target = new JarOutputStream(new BufferedOutputStream(
                new FileOutputStream(jarFilePath.toString())), manifest)) {
            Map<String, byte[]> jarEntries = compiledJarFile.getJarEntries();
            for (Map.Entry<String, byte[]> keyVal : jarEntries.entrySet()) {
                byte[] entryContent = keyVal.getValue();
                JarEntry entry = new JarEntry(keyVal.getKey());
                target.putNextEntry(entry);
                target.write(entryContent);
                target.closeEntry();
            }
        }
    }
}

/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.ballerinalang.compiler;

import io.ballerina.projects.ProjectException;
import org.apache.commons.compress.archivers.jar.JarArchiveEntry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

/**
 * This class holds .class files as entries of a program jar file and convert them to a ByteArrayOutputStream.
 *
 * @since 2201.10.0
 */
public class JAREntries {
    private ByteArrayOutputStream byteArrayOutputStream;
    private JarOutputStream entries;

    public JAREntries(String mainClassName) {
        this.byteArrayOutputStream = new ByteArrayOutputStream();
        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        if (mainClassName != null) {
            mainAttributes.put(Attributes.Name.MAIN_CLASS, mainClassName);
        }
        try {
            entries = new JarOutputStream(this.byteArrayOutputStream, manifest);
        } catch (IOException e) {
            throw new ProjectException("Failed create JarOutputStream to cache jar entries", e);
        }
    }

    public void put(String key, byte[] value) {
        JarEntry entry = new JarEntry(key);
        try {
            entries.putNextEntry(entry);
            entries.write(value);
            entries.closeEntry();
        } catch (IOException e) {
            throw new ProjectException("Failed put jar entry", e);
        }
    }

    public void putJarArchiveEntry(String key, byte[] value) {
        JarArchiveEntry entry = new JarArchiveEntry(key);
        try {
            entries.putNextEntry(entry);
            entries.write(value);
            entries.closeEntry();
        } catch (IOException e) {
            throw new ProjectException("Failed to put jar archive entry", e);
        }
    }

    public void end() {
        try {
            entries.close();
        } catch (IOException e) {
            throw new ProjectException("Failed to close jarOutputStream", e);
        }
    }

    public ByteArrayOutputStream getByteArrayOutputStream() {
        return byteArrayOutputStream;
    }
}

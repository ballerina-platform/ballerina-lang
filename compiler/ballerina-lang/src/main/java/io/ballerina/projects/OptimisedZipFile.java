/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.projects;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntryPredicate;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;

import static java.util.Objects.requireNonNull;

/**
 * Eliminates the unused methods from the output stream of the given zip file
 *
 */
public class OptimisedZipFile extends ZipFile {
    private final HashMap<String, HashSet<String>> deadFunctionMap;
    private static final String INIT_CLASS_IDENTIFIER = "init.class";

    public OptimisedZipFile(File file, HashMap<String, HashSet<String>> deadFunctionMap) throws IOException {
        super(file);
        this.deadFunctionMap = deadFunctionMap;
    }

    public void copyOptimisedRawEntries(final ZipArchiveOutputStream target, final ZipArchiveEntryPredicate predicate)
            throws IOException {
        final Enumeration<ZipArchiveEntry> src = getEntriesInPhysicalOrder();
        while (src.hasMoreElements()) {
            final ZipArchiveEntry entry = src.nextElement();
            if (predicate.test(entry)) {
                if (!entry.getName().contains(INIT_CLASS_IDENTIFIER) && deadFunctionMap.containsKey(entry.getName())) {
                    target.putArchiveEntry(entry);
                    target.write(getOptimizedEntry(entry));
                    target.closeArchiveEntry();
                } else {
                    target.addRawArchiveEntry(entry, getRawInputStream(entry));
                }
            }
        }
    }

    public byte[] getOptimizedEntry(ZipArchiveEntry originalEntry) throws IOException {
        InputStream inputStream = getInputStream(originalEntry);

        ClassReader classReader = new ClassReader(requireNonNull(inputStream));
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassVisitor classVisitor = new DeadFunctionVisitor(Opcodes.ASM9, classWriter, deadFunctionMap.get(originalEntry.getName()));
        classReader.accept(classVisitor, 0);

        return classWriter.toByteArray();
    }
}

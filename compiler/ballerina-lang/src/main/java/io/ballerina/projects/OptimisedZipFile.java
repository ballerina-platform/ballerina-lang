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

public class OptimisedZipFile extends ZipFile {
    private final HashMap<String, HashSet<String>> deadFunctionMap;

    public OptimisedZipFile(File file, HashMap<String, HashSet<String>> deadFunctionMap) throws IOException {
        super(file);
        this.deadFunctionMap = deadFunctionMap;
    }

    public void copyOptimisedRawEntries(final ZipArchiveOutputStream target, final ZipArchiveEntryPredicate predicate) throws IOException {
        final Enumeration<ZipArchiveEntry> src = getEntriesInPhysicalOrder();
        while (src.hasMoreElements()) {
            final ZipArchiveEntry entry = src.nextElement();
            if (predicate.test( entry)) {
                if (!entry.getName().contains("init.class") && deadFunctionMap.containsKey(entry.getName())) {
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
        ClassVisitor classVisitor = new DeadFunctionVisitor(Opcodes.ASM7, classWriter, deadFunctionMap.get(originalEntry.getName()));
        classReader.accept(classVisitor, 0);

        return classWriter.toByteArray();
    }
}

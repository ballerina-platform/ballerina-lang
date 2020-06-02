/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.packerina.writer;

import org.ballerinalang.compiler.BLangCompilerException;
import org.wso2.ballerinalang.compiler.CompiledJarFile;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

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
public class JarFileWriter {

    private static final CompilerContext.Key<JarFileWriter> JAR_FILE_WRITER_KEY = new CompilerContext.Key<>();

    public static JarFileWriter getInstance(CompilerContext context) {

        JarFileWriter jarFileWriter = context.get(JAR_FILE_WRITER_KEY);
        if (jarFileWriter == null) {
            jarFileWriter = new JarFileWriter(context);
        }
        return jarFileWriter;
    }

    private JarFileWriter(CompilerContext context) {

        context.put(JAR_FILE_WRITER_KEY, this);
    }

    /**
     * Write the compiled module to a jar file.
     *
     * @param bLangPackage The compiled module.
     * @param jarFilePath  The path to the jar file.
     */
    public void write(BLangPackage bLangPackage, Path jarFilePath) {

        write(bLangPackage.symbol, jarFilePath);
    }

    /**
     * Write the compiled module to a jar file.
     *
     * @param packageSymbol The symbol of the compiled module.
     * @param jarFilePath   The path to the jar file.
     */
    public void write(BPackageSymbol packageSymbol, Path jarFilePath) {

        if (packageSymbol.compiledJarFile == null) {
            return;
        }

        CompiledJarFile compiledJarFile = packageSymbol.compiledJarFile;
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
        } catch (IOException e) {
            String msg = "error writing the compiled jar of '" + packageSymbol.pkgID + "' to '" + jarFilePath + "': " +
                    e.getMessage();
            throw new BLangCompilerException(msg, e);
        }
    }
}

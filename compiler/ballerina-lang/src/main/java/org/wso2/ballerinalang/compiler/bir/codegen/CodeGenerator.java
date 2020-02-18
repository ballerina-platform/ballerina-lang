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
package org.wso2.ballerinalang.compiler.bir.codegen;

import org.ballerinalang.compiler.BLangCompilerException;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropValidator;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.compiledPkgCache;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.generatePackage;

/**
 * JVM byte code generator from BIR model.
 *
 * @since 1.2.0
 */
public class CodeGenerator {
    private static final CompilerContext.Key<CodeGenerator> CODE_GEN = new CompilerContext.Key<>();

    //TODO: remove static
    static SymbolTable symbolTable;
    static PackageCache packageCache;

    public CodeGenerator(CompilerContext context) {
        context.put(CODE_GEN, this);
        symbolTable = SymbolTable.getInstance(context);
        packageCache = PackageCache.getInstance(context);
    }

    public static CodeGenerator getInstance(CompilerContext context) {

        CodeGenerator codeGenerator = context.get(CODE_GEN);
        if (codeGenerator == null) {
            codeGenerator = new CodeGenerator(context);
        }

        return codeGenerator;
    }

    public void generate(BIRNode.BIRPackage entryMod, Path target) {
        JvmPackageGen.symbolTable = this.symbolTable;
        compiledPkgCache.put(entryMod.org.value + entryMod.name.value, entryMod);
        JvmPackageGen.JarFile jarFile = new JvmPackageGen.JarFile();
        InteropValidator interopValidator = new InteropValidator();
        generatePackage(entryMod, jarFile, interopValidator, true);
        writeJarFile(jarFile, target);
    }


    private static void writeJarFile(JvmPackageGen.JarFile entries, Path targetPath) {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        if (entries.manifestEntries != null) {
            Map<String, String> manifestEntries = entries.manifestEntries;
            manifestEntries.forEach((key, value) -> manifest.getMainAttributes().put(new Attributes.Name(key), value));
        }

        try (JarOutputStream target = new JarOutputStream(new FileOutputStream(targetPath.toString()), manifest)) {
            if (entries.pkgEntries == null) {
                throw new BLangCompilerException("no class file entries found in the record");
            }

            Map<String, byte[]> jarEntries = entries.pkgEntries;
            for (Map.Entry<String, byte[]> keyVal : jarEntries.entrySet()) {
                byte[] entryContent = keyVal.getValue();
                JarEntry entry = new JarEntry(keyVal.getKey());
                target.putNextEntry(entry);
                target.write(entryContent);
                target.closeEntry();
            }
        } catch (IOException e) {
            throw new BLangCompilerException("jar file generation failed: " + e.getMessage(), e);
        }
    }

}

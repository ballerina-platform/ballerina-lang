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
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.generatePackage;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmPackageGen.intiPackageGen;

/**
 * JVM byte code generator from BIR model.
 *
 * @since 1.2.0
 */
public class CodeGenerator {

    private static final CompilerContext.Key<CodeGenerator> CODE_GEN = new CompilerContext.Key<>();

    public static BLangDiagnosticLog dlog;

    //TODO: remove static
    static SymbolTable symbolTable;
    static PackageCache packageCache;

    private Map<String, BIRNode.BIRPackage> compiledPkgCache = new HashMap<>();

    private CodeGenerator(CompilerContext context) {

        context.put(CODE_GEN, this);
        symbolTable = SymbolTable.getInstance(context);
        packageCache = PackageCache.getInstance(context);
        dlog = BLangDiagnosticLog.getInstance(context);
    }

    public static CodeGenerator getInstance(CompilerContext context) {

        CodeGenerator codeGenerator = context.get(CODE_GEN);
        if (codeGenerator == null) {
            codeGenerator = new CodeGenerator(context);
        }

        return codeGenerator;
    }

    public void generate(BIRNode.BIRPackage entryMod, Path target, Set<Path> moduleDependencies) {

        if (compiledPkgCache.containsValue(entryMod)) {
            return;
        }

        intiPackageGen();
        JvmPackageGen.symbolTable = symbolTable;
        JvmMethodGen.errorOrNilType = BUnionType.create(null, symbolTable.errorType, symbolTable.nilType);
        compiledPkgCache.put(entryMod.org.value + entryMod.name.value, entryMod);
        JvmPackageGen.JarFile jarFile = new JvmPackageGen.JarFile();
        populateExternalMap();

        List<URL> dependentJars = new ArrayList<>();
        for (Path dependency : moduleDependencies) {
            try {
                dependentJars.add(dependency.toUri().toURL());
            } catch (MalformedURLException e) {
                // ignore
            }
        }

        ClassLoader classLoader = new URLClassLoader(dependentJars.toArray(new URL[]{}), null);
        InteropValidator interopValidator = new InteropValidator(classLoader, symbolTable);
        generatePackage(entryMod, jarFile, interopValidator, true);
        writeJarFile(jarFile, target);
    }

    private void populateExternalMap() {

        String nativeMap = System.getenv("BALLERINA_NATIVE_MAP");
        if (nativeMap == null) {
            return;
        }
        File mapFile = new File(nativeMap);
        if (!mapFile.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(mapFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("\"")) {
                    int firstQuote = line.indexOf('"', 1);
                    String key = line.substring(1, firstQuote);
                    String value = line.substring(line.indexOf('"', firstQuote + 1) + 1, line.lastIndexOf('"'));
                    JvmPackageGen.externalMapCache.put(key, value);
                }
            }
        } catch (IOException e) {
            //ignore because this is only important in langlibs users shouldn't see this error
        }
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

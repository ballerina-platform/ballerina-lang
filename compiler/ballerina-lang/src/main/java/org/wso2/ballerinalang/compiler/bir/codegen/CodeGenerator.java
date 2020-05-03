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
import org.wso2.ballerinalang.compiler.bir.codegen.internal.JarFile;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropValidator;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.BufferedOutputStream;
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

/**
 * JVM byte code generator from BIR model.
 *
 * @since 1.2.0
 */
public class CodeGenerator {

    private static final CompilerContext.Key<CodeGenerator> CODE_GEN = new CompilerContext.Key<>();

    private SymbolTable symbolTable;

    private Map<String, BIRNode.BIRPackage> compiledPkgCache = new HashMap<>();

    private JvmPackageGen jvmPackageGen;

    private CodeGenerator(CompilerContext context) {

        context.put(CODE_GEN, this);
        symbolTable = SymbolTable.getInstance(context);
        jvmPackageGen = JvmPackageGen.getInstance(context);
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

        compiledPkgCache.put(entryMod.org.value + entryMod.name.value, entryMod);

        populateExternalMap();

        ClassLoader classLoader = makeClassLoader(moduleDependencies);
        InteropValidator interopValidator = new InteropValidator(classLoader, symbolTable);
        JarFile jarFile = jvmPackageGen.generate(entryMod, interopValidator, true);
        writeJarFile(jarFile, target);
    }

    private ClassLoader makeClassLoader(Set<Path> moduleDependencies) {

        if (moduleDependencies == null) {
            return Thread.currentThread().getContextClassLoader();
        }
        List<URL> dependentJars = new ArrayList<>();
        for (Path dependency : moduleDependencies) {
            try {
                dependentJars.add(dependency.toUri().toURL());
            } catch (MalformedURLException e) {
                // ignore
            }
        }

        return new URLClassLoader(dependentJars.toArray(new URL[]{}), null);
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
                    jvmPackageGen.addExternClassMapping(key, value);
                }
            }
        } catch (IOException e) {
            //ignore because this is only important in langlibs users shouldn't see this error
        }
    }

    private void writeJarFile(JarFile jarFile, Path targetPath) {

        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        mainAttributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        jarFile.getMainClassName().ifPresent(mainClassName ->
                mainAttributes.put(Attributes.Name.MAIN_CLASS, mainClassName));

        try (JarOutputStream target = new JarOutputStream(new BufferedOutputStream(
                new FileOutputStream(targetPath.toString())), manifest)) {
            Map<String, byte[]> jarEntries = jarFile.getJarEntries();
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

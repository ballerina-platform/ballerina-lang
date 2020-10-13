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
import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.compiler.JarResolver;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropValidator;
import org.wso2.ballerinalang.compiler.bir.emit.BIREmitter;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Names;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.ballerinalang.compiler.JarResolver.JAR_RESOLVER_KEY;
import static org.wso2.ballerinalang.compiler.bir.codegen.JvmDesugarPhase.encodeModuleIdentifiers;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME;

/**
 * JVM byte code generator from BIR model.
 *
 * @since 1.2.0
 */
public class CodeGenerator {

    private static final CompilerContext.Key<CodeGenerator> CODE_GEN = new CompilerContext.Key<>();
    private SymbolTable symbolTable;
    private PackageCache packageCache;
    private BLangDiagnosticLog dlog;
    private BIREmitter birEmitter;
    private boolean baloGen;
    private CompilerContext compilerContext;
    private boolean skipTests;
    private boolean dumbBIR;
    private final String dumpBIRFile;
    private boolean skipModuleDependencies;
    private Path ballerinaHome = Paths.get(System.getProperty(BALLERINA_HOME));

    private CodeGenerator(CompilerContext compilerContext) {

        compilerContext.put(CODE_GEN, this);
        this.symbolTable = SymbolTable.getInstance(compilerContext);
        this.packageCache = PackageCache.getInstance(compilerContext);
        this.dlog = BLangDiagnosticLog.getInstance(compilerContext);
        this.birEmitter = BIREmitter.getInstance(compilerContext);
        this.compilerContext = compilerContext;
        CompilerOptions compilerOptions = CompilerOptions.getInstance(compilerContext);
        this.skipTests = getBooleanValueIfSet(compilerOptions, CompilerOptionName.SKIP_TESTS);
        this.baloGen = getBooleanValueIfSet(compilerOptions, CompilerOptionName.BALO_GENERATION);
        this.dumbBIR = getBooleanValueIfSet(compilerOptions, CompilerOptionName.DUMP_BIR);
        this.dumpBIRFile = compilerOptions.get(CompilerOptionName.DUMP_BIR_FILE);
        this.skipModuleDependencies = getBooleanValueIfSet(compilerOptions,
                CompilerOptionName.SKIP_MODULE_DEPENDENCIES);
    }

    public static CodeGenerator getInstance(CompilerContext context) {

        CodeGenerator codeGenerator = context.get(CODE_GEN);
        if (codeGenerator == null) {
            codeGenerator = new CodeGenerator(context);
        }

        return codeGenerator;
    }

    private boolean getBooleanValueIfSet(CompilerOptions compilerOptions, CompilerOptionName optionName) {

        return compilerOptions.isSet(optionName) && Boolean.parseBoolean(compilerOptions.get(optionName));
    }

    public BLangPackage generate(BLangPackage bLangPackage) {

        if (dumbBIR) {
            birEmitter.emit(bLangPackage.symbol.bir);
        }

        if (dumpBIRFile != null) {
            try {
                Files.write(Paths.get(dumpBIRFile),
                            bLangPackage.symbol.birPackageFile.pkgBirBinaryContent);
            } catch (IOException e) {
                throw new BLangCompilerException("BIR file dumping failed", e);
            }
        }

        // find module dependencies path
        Set<Path> moduleDependencies = findDependencies(bLangPackage.packageID);

        // generate module jar
        generate(bLangPackage.symbol, moduleDependencies);

        if (skipTests || !bLangPackage.hasTestablePackage()) {
            return bLangPackage;
        }

        bLangPackage.getTestablePkgs().forEach(testablePackage -> {

            // find module dependencies path
            Set<Path> testDependencies = findTestDependencies(testablePackage.packageID, moduleDependencies);

            // generate test module jar
            generate(testablePackage.symbol, testDependencies);
        });

        return bLangPackage;
    }

    private void generate(BPackageSymbol packageSymbol, Set<Path> moduleDependencies) {

        final JvmPackageGen jvmPackageGen = new JvmPackageGen(symbolTable, packageCache, dlog);

        populateExternalMap(jvmPackageGen);

        ClassLoader interopValidationClassLoader = makeClassLoader(moduleDependencies);
        InteropValidator interopValidator = new InteropValidator(interopValidationClassLoader, symbolTable);

        //Rewrite identiifier names with encoding special characters
        encodeModuleIdentifiers(packageSymbol.bir, Names.getInstance(this.compilerContext));

        packageSymbol.compiledJarFile = jvmPackageGen.generate(packageSymbol.bir, interopValidator, true);
    }

    private Set<Path> findDependencies(PackageID packageID) {

        Set<Path> moduleDependencies = new HashSet<>();

        if (skipModuleDependencies) {
            return moduleDependencies;
        }

        if (baloGen) {
            moduleDependencies.addAll(readInteropDependencies());
        }

        JarResolver jarResolver = compilerContext.get(JAR_RESOLVER_KEY);

        if (jarResolver != null) {
            moduleDependencies.addAll(jarResolver.nativeDependencies(packageID));
        }

        return moduleDependencies;
    }

    private Set<Path> findTestDependencies(PackageID testPackageId, Set<Path> moduleDependencies) {

        Set<Path> testDependencies = new HashSet<>(moduleDependencies);

        JarResolver jarResolver = compilerContext.get(JAR_RESOLVER_KEY);

        if (jarResolver != null) {
            testDependencies.addAll(jarResolver.nativeDependenciesForTests(testPackageId));
        }

        return testDependencies;
    }

    private HashSet<Path> readInteropDependencies() {

        HashSet<Path> interopDependencies = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream("build/interopJars.txt"), Charset.forName("UTF-8")))) {
            String line;
            while ((line = br.readLine()) != null) {
                interopDependencies.add(Paths.get(line));
            }
        } catch (IOException e) {
            throw new BLangCompilerException("error reading interop jar file names", e);
        }
        return interopDependencies;
    }

    private ClassLoader makeClassLoader(Set<Path> moduleDependencies) {

        if (moduleDependencies == null || moduleDependencies.size() == 0) {
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

        return new URLClassLoader(dependentJars.toArray(new URL[]{}), ClassLoader.getPlatformClassLoader());
    }

    private void populateExternalMap(JvmPackageGen jvmPackageGen) {

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
}

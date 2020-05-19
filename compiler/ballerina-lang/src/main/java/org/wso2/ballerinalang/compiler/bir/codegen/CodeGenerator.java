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
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.toml.model.Library;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.bir.codegen.interop.InteropValidator;
import org.wso2.ballerinalang.compiler.bir.emit.BIREmitter;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLogHelper;
import org.wso2.ballerinalang.util.RepoUtils;

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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_BRE;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_LIB;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_JAR_EXT;

/**
 * JVM byte code generator from BIR model.
 *
 * @since 1.2.0
 */
public class CodeGenerator {

    private static final CompilerContext.Key<CodeGenerator> CODE_GEN = new CompilerContext.Key<>();
    private final Set<Path> projectDependencies = new HashSet<>();
    private SymbolTable symbolTable;
    private PackageCache packageCache;
    private BLangDiagnosticLogHelper dlog;
    private BIREmitter birEmitter;
    private Path projectRoot;
    private boolean isBaloGen;
    private Manifest manifest;
    private String sourceType;
    private CompilerOptions compilerOptions;
    private boolean skipTests;
    private boolean dumbBIR;
    private boolean skipAddDependencies;
    private Path ballerinaHome = Paths.get(System.getProperty(BALLERINA_HOME));

    private CodeGenerator(CompilerContext context) {

        context.put(CODE_GEN, this);
        this.symbolTable = SymbolTable.getInstance(context);
        this.packageCache = PackageCache.getInstance(context);
        this.dlog = BLangDiagnosticLogHelper.getInstance(context);
        this.birEmitter = BIREmitter.getInstance(context);
        this.manifest = ManifestProcessor.getInstance(context).getManifest();
        this.compilerOptions = CompilerOptions.getInstance(context);
        init();
    }

    public static CodeGenerator getInstance(CompilerContext context) {

        CodeGenerator codeGenerator = context.get(CODE_GEN);
        if (codeGenerator == null) {
            codeGenerator = new CodeGenerator(context);
        }

        return codeGenerator;
    }

    private void init() {

        // skip initialization of this instance if the phase is not codegen
        if (this.compilerOptions.getCompilerPhase().compareTo(CompilerPhase.CODE_GEN) < 0) {
            return;
        }

        this.projectRoot = Paths.get(this.compilerOptions.get(CompilerOptionName.PROJECT_DIR));
        this.sourceType = this.compilerOptions.get(CompilerOptionName.SOURCE_TYPE);
        this.skipTests = getBooleanValueIfSet(this.compilerOptions, CompilerOptionName.SKIP_TESTS);
        this.isBaloGen = getBooleanValueIfSet(this.compilerOptions, CompilerOptionName.BALO_GENERATION);
        this.dumbBIR = getBooleanValueIfSet(this.compilerOptions, CompilerOptionName.DUMP_BIR);
        this.skipAddDependencies = getBooleanValueIfSet(this.compilerOptions, CompilerOptionName.SKIP_ADD_DEPENDENCIES);
        findProjectDependenciesInManifest();
    }

    private boolean getBooleanValueIfSet(CompilerOptions compilerOptions, CompilerOptionName optionName) {

        return compilerOptions.isSet(optionName) && Boolean.parseBoolean(compilerOptions.get(optionName));
    }

    public BLangPackage generate(BLangPackage bLangPackage) {

        if (dumbBIR) {
            birEmitter.emit(bLangPackage.symbol.bir);
        }

        // find module dependencies path
        Set<Path> moduleDependencies = findDependencies(bLangPackage);

        // generate module jar
        generate(bLangPackage.symbol, moduleDependencies);

        if (skipTests || !bLangPackage.hasTestablePackage()) {
            return bLangPackage;
        }

        bLangPackage.getTestablePkgs().forEach(testablePackage -> {

            // find module dependencies path
            Set<Path> testDependencies = findDependencies(testablePackage);

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
        packageSymbol.compiledJarFile = jvmPackageGen.generate(packageSymbol.bir, interopValidator, true);
    }

    private Set<Path> findDependencies(BLangPackage bLangPackage) {

        //TODO : this is not the correct way and we need to use the project API to resolve dependencies

        Set<Path> moduleDependencies = new HashSet<>();

        if (skipAddDependencies) {
            return moduleDependencies;
        }

        if (projectDependencies.size() > 0) {
            moduleDependencies.addAll(projectDependencies);
        }

        if (isBaloGen) {
            moduleDependencies.addAll(readInteropDependencies());
        }

        return moduleDependencies;
    }

    private void findProjectDependenciesInManifest() {

        //TODO : this is not the correct way and we need to use the project API to resolve dependencies

        // add runtime all jar to the classpath
        projectDependencies.add(getRuntimeAllJarPath());

        if (!"SINGLE_MODULE".equals(sourceType) && !"ALL_MODULES".equals(sourceType)) {
            return;
        }

        // add platform libs, if they are defined
        List<Library> libraries = manifest.getPlatform().libraries;
        if (libraries != null && libraries.size() > 0) {
            for (Library library : libraries) {

                if (library.getPath() == null) {
                    continue;
                }

                Path libFilePath = Paths.get(library.getPath());
                Path libFile = projectRoot.resolve(libFilePath);

                if (!libFile.toFile().exists()) {
                    continue;
                }
                Path path = Paths.get(libFile.toUri());
                projectDependencies.add(path);
            }
        }
    }

    private Path getRuntimeAllJarPath() {

        String ballerinaVersion = RepoUtils.getBallerinaVersion();
        String runtimeJarName = "ballerina-rt-" + ballerinaVersion + BLANG_COMPILED_JAR_EXT;
        return Paths.get(ballerinaHome.toString(), BALLERINA_HOME_BRE, BALLERINA_HOME_LIB, runtimeJarName);
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

        return new URLClassLoader(dependentJars.toArray(new URL[]{}), null);
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

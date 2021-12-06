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

import org.wso2.ballerinalang.compiler.CompiledJarFile;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.bir.codegen.optimizer.LargeMethodOptimizer;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

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
    private CompilerContext compilerContext;
    private LargeMethodOptimizer largeMethodOptimizer;

    private CodeGenerator(CompilerContext compilerContext) {

        compilerContext.put(CODE_GEN, this);
        this.symbolTable = SymbolTable.getInstance(compilerContext);
        this.packageCache = PackageCache.getInstance(compilerContext);
        this.dlog = BLangDiagnosticLog.getInstance(compilerContext);
        this.compilerContext = compilerContext;
    }

    public static CodeGenerator getInstance(CompilerContext context) {

        CodeGenerator codeGenerator = context.get(CODE_GEN);
        if (codeGenerator == null) {
            codeGenerator = new CodeGenerator(context);
        }

        return codeGenerator;
    }

    public CompiledJarFile generate(BLangPackage bLangPackage) {
        // generate module
        return generate(bLangPackage.symbol);
    }

    public CompiledJarFile generateTestModule(BLangPackage bLangTestablePackage) {
        return generate(bLangTestablePackage.symbol);
    }

    private CompiledJarFile generate(BPackageSymbol packageSymbol) {

        // Split large BIR functions into smaller methods
        largeMethodOptimizer = new LargeMethodOptimizer();
        largeMethodOptimizer.splitLargeBIRFunctions(packageSymbol.bir);

        // Desugar BIR to include the observations
        JvmObservabilityGen jvmObservabilityGen = new JvmObservabilityGen(packageCache, symbolTable);
        jvmObservabilityGen.instrumentPackage(packageSymbol.bir);
        dlog.setCurrentPackageId(packageSymbol.pkgID);
        final JvmPackageGen jvmPackageGen = new JvmPackageGen(symbolTable, packageCache, dlog, compilerContext);

        populateExternalMap(jvmPackageGen);

        //Rewrite identifier names with encoding special characters
        HashMap<String, String> originalIdentifierMap = JvmDesugarPhase.encodeModuleIdentifiers(packageSymbol.bir);

        // TODO Get-rid of the following assignment
        packageSymbol.compiledJarFile = jvmPackageGen.generate(packageSymbol.bir, true);

        //Revert encoding identifier names
        JvmDesugarPhase.replaceEncodedModuleIdentifiers(packageSymbol.bir, originalIdentifierMap);
        return packageSymbol.compiledJarFile;
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

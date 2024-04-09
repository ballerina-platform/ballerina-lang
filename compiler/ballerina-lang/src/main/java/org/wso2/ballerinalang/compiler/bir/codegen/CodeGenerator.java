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

import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.bir.BIRGenUtils;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnosticLog;
import org.wso2.ballerinalang.compiler.semantics.analyzer.Types;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.HashMap;

/**
 * JVM byte code generator from BIR model.
 *
 * @since 1.2.0
 */
public class CodeGenerator {

    private static final CompilerContext.Key<CodeGenerator> CODE_GEN = new CompilerContext.Key<>();
    private final SymbolTable symbolTable;
    private final PackageCache packageCache;
    private final BLangDiagnosticLog dlog;
    private final Types types;

    private CodeGenerator(CompilerContext compilerContext) {
        compilerContext.put(CODE_GEN, this);
        this.symbolTable = SymbolTable.getInstance(compilerContext);
        this.packageCache = PackageCache.getInstance(compilerContext);
        this.dlog = BLangDiagnosticLog.getInstance(compilerContext);
        this.types = Types.getInstance(compilerContext);
    }

    public static CodeGenerator getInstance(CompilerContext context) {
        CodeGenerator codeGenerator = context.get(CODE_GEN);
        if (codeGenerator == null) {
            codeGenerator = new CodeGenerator(context);
        }
        return codeGenerator;
    }

    public CompiledJarFile generate(BLangPackage bLangPackage, boolean isRemoteMgtEnabled, boolean isDuplicateGeneration) {
        // generate module
        return generate(bLangPackage.symbol, isRemoteMgtEnabled, isDuplicateGeneration);
    }

    public CompiledJarFile generateTestModule(BLangPackage bLangTestablePackage, boolean isRemoteMgtEnabled) {
        return generate(bLangTestablePackage.symbol, isRemoteMgtEnabled, false);
    }

    private CompiledJarFile generate(BPackageSymbol packageSymbol, boolean isRemoteMgtEnabled, boolean isDuplicateGeneration) {
        // Desugar BIR to include the observations
        JvmObservabilityGen jvmObservabilityGen = new JvmObservabilityGen(packageCache, symbolTable);
        jvmObservabilityGen.instrumentPackage(packageSymbol.bir);

        // TODO merge this with jvmPackageGen part
        JvmCodeGenUtil.isOptimizedCodegen = isDuplicateGeneration;

        // Re-arrange basic blocks and error entries
        BIRGenUtils.rearrangeBasicBlocks(packageSymbol.bir);

        dlog.setCurrentPackageId(packageSymbol.pkgID);
        final JvmPackageGen jvmPackageGen = new JvmPackageGen(symbolTable, packageCache, dlog, types, 
                isRemoteMgtEnabled);

        //Rewrite identifier names with encoding special characters
        HashMap<String, String> originalIdentifierMap = JvmDesugarPhase.encodeModuleIdentifiers(packageSymbol.bir);

        // TODO Get-rid of the following assignment
        CompiledJarFile compiledJarFile = jvmPackageGen.generate(packageSymbol.bir);
        cleanUpBirPackage(packageSymbol);
        //Revert encoding identifier names
        JvmDesugarPhase.replaceEncodedModuleIdentifiers(packageSymbol.bir, originalIdentifierMap);

        JvmCodeGenUtil.isOptimizedCodegen = false;
        JvmCodeGenUtil.isRootPkgCodeGen = false;
        return compiledJarFile;
    }

    private static void cleanUpBirPackage(BPackageSymbol packageSymbol) {
        packageSymbol.birPackageFile = null;
        BIRNode.BIRPackage bir = packageSymbol.bir;
        for (BIRNode.BIRTypeDefinition typeDef : bir.typeDefs) {
            for (BIRNode.BIRFunction attachedFunc : typeDef.attachedFuncs) {
                cleanUpBirFunction(attachedFunc);
            }
            typeDef.annotAttachments = null;
        }
        bir.importedGlobalVarsDummyVarDcls.clear();
        for (BIRNode.BIRFunction function : bir.functions) {
            cleanUpBirFunction(function);
        }
        // FIXME: common code
        bir.annotations.clear();
        bir.constants.clear();
        bir.serviceDecls.clear();
    }
    public CompiledJarFile generateOptimized(BPackageSymbol packageSymbol) {
        dlog.setCurrentPackageId(packageSymbol.pkgID);
        final JvmPackageGen jvmPackageGen = new JvmPackageGen(symbolTable, packageCache, dlog, types);

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
        bir.importedGlobalVarsDummyVarDcls.clear();
        for (BIRNode.BIRFunction function : bir.functions) {
            cleanUpBirFunction(function);
        }
        bir.annotations.clear();
        bir.constants.clear();
        bir.serviceDecls.clear();
    }

    private static void cleanUpBirFunction(BIRNode.BIRFunction function) {
        function.receiver = null;
        function.localVars = null;
        function.returnVariable = null;
        function.parameters = null;
        function.basicBlocks = null;
        function.errorTable = null;
        function.workerChannels = null;
        function.annotAttachments = null;
        function.returnTypeAnnots = null;
        function.dependentGlobalVars = null;
        function.pathParams = null;
        function.restPathParam = null;
    }
}

/*
*   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.ballerinalang.compiler;

import org.wso2.ballerinalang.compiler.desugar.Desugar;
import org.wso2.ballerinalang.compiler.semantics.analyzer.CodeAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SemanticAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.model.Scope;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BStructType;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangStruct;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Names;

import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.SOURCE_ROOT;

/**
 * Loads ballerina.builtin package and required packages for compilation.
 *
 * @since 0.94
 */
public class BuiltinPackageLoader {

    private static final CompilerContext.Key<BuiltinPackageLoader> BUILTIN_PKG_LOADER_KEY = new CompilerContext.Key<>();

    private Names names;
    private CompilerOptions options;
    private PackageLoader pkgLoader;
    private SymbolEnter symbolEnter;
    private SymbolTable symbolTable;
    private SemanticAnalyzer semAnalyzer;
    private CodeAnalyzer codeAnalyzer;
    private Desugar desugar;
    private BLangPackage builtInPkg, errorPkg;


    private BuiltinPackageLoader(CompilerContext context) {
        context.put(BUILTIN_PKG_LOADER_KEY, this);

        CompilerContext newContext = new CompilerContext();
        this.names = Names.getInstance(newContext);
        this.options = CompilerOptions.getInstance(newContext);
        options.put(SOURCE_ROOT, Names.DOT.value);
        options.put(PRESERVE_WHITESPACE, "false");
        this.pkgLoader = PackageLoader.getInstance(newContext);
        this.symbolEnter = SymbolEnter.getInstance(newContext);
        this.symbolTable = SymbolTable.getInstance(newContext);
        this.semAnalyzer = SemanticAnalyzer.getInstance(newContext);
        this.codeAnalyzer = CodeAnalyzer.getInstance(newContext);
        this.desugar = Desugar.getInstance(newContext);
    }

    public static BuiltinPackageLoader getInstance(CompilerContext context) {
        BuiltinPackageLoader builtinPackageLoader = context.get(BUILTIN_PKG_LOADER_KEY);
        if (builtinPackageLoader == null) {
            builtinPackageLoader = new BuiltinPackageLoader(context);
        }

        return builtinPackageLoader;
    }

    /**
     * Load ballerina.builtin and other required symbols into given symbol table.
     *
     * @param symTable given SymbolTable to load builtin pkg.
     */
    public void loadBuiltinPackage(SymbolTable symTable) {
        builtInPkg = loadPackage("ballerina.builtin");
        // merge builtInPkg symbol's into given rootScope.
        merge(symTable.rootScope, builtInPkg);
        updateErrorTypes(symTable, builtInPkg);

        // merge builtInPkg symbol's into current compiler's rootScope.
        merge(this.symbolTable.rootScope, builtInPkg);
        updateErrorTypes(this.symbolTable, builtInPkg);

        // parse ballerina.lang.errors.
        errorPkg = loadPackage("ballerina.lang.errors");
        updateErrorTypes(symTable, errorPkg);

    }

    /**
     * Merge loaded packages in given symbolEnter.
     * TODO : Remove this logic. these packages should be part of the VM.
     *
     * @param symEnter given symbolEnter instance.
     */
    public void mergePackages(SymbolEnter symEnter) {
        for (BPackageSymbol packageSymbol : this.symbolEnter.packageEnvs.keySet()) {
            symEnter.packageEnvs.put(packageSymbol, this.symbolEnter.packageEnvs.get(packageSymbol));
        }
    }

    public BLangPackage getBuiltInPkg() {
        return builtInPkg;
    }

    public BLangPackage getErrorPkg() {
        return errorPkg;
    }

    // Private methods.

    private void merge(Scope rootScope, BLangPackage builtInPkg) {
        builtInPkg.getAnnotations().forEach(a -> rootScope.define(names.fromIdNode(a.name), a.symbol));
        builtInPkg.getStructs().forEach(s -> rootScope.define(names.fromIdNode(s.name), s.symbol));
        builtInPkg.getFunctions().forEach(f -> rootScope.define(names.fromIdNode(f.name), f.symbol));
    }

    private void updateErrorTypes(SymbolTable symbolTable, BLangPackage builtInPkg) {
        for (BLangStruct bLangStruct : builtInPkg.getStructs()) {
            if (Names.ERROR.value.equals(bLangStruct.name.value)) {
                symbolTable.errStructType.tsymbol = ((BStructType) bLangStruct.symbol.type).tsymbol;
                symbolTable.errStructType.fields = ((BStructType) bLangStruct.symbol.type).fields;
                symbolTable.errStructType.tag = ((BStructType) bLangStruct.symbol.type).tag;
            } else if (Names.ERROR_TYPE_CAST.value.equals(bLangStruct.name.value)) {
                symbolTable.errTypeCastType.tsymbol = ((BStructType) bLangStruct.symbol.type).tsymbol;
                symbolTable.errTypeCastType.fields = ((BStructType) bLangStruct.symbol.type).fields;
                symbolTable.errTypeCastType.tag = ((BStructType) bLangStruct.symbol.type).tag;
            } else if (Names.ERROR_TYPE_CONVERSION.value.equals(bLangStruct.name.value)) {
                symbolTable.errTypeConversionType.tsymbol = ((BStructType) bLangStruct.symbol.type).tsymbol;
                symbolTable.errTypeConversionType.fields = ((BStructType) bLangStruct.symbol.type).fields;
                symbolTable.errTypeConversionType.tag = ((BStructType) bLangStruct.symbol.type).tag;
            }
        }
    }

    private BLangPackage loadPackage(String pkg) {
        // TODO: Initial implementation. Refactor this to read from balo.
        try {
            return desugar.perform(codeAnalyzer.analyze(semAnalyzer.analyze(pkgLoader.loadEntryPackage(pkg))));
        } catch (RuntimeException e) {
            // Ignore error for now. Ideally this shouldn't throw any errors.
            return new BLangPackage();
        }
    }
}

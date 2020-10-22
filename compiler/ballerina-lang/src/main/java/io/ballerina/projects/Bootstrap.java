/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.projects;

import io.ballerina.projects.environment.ModuleLoadRequest;
import io.ballerina.projects.environment.ModuleLoadResponse;
import io.ballerina.projects.environment.PackageResolver;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Collection;
import java.util.Collections;

import static org.ballerinalang.model.elements.PackageID.ANNOTATIONS;
import static org.ballerinalang.model.elements.PackageID.ARRAY;
import static org.ballerinalang.model.elements.PackageID.BOOLEAN;
import static org.ballerinalang.model.elements.PackageID.DECIMAL;
import static org.ballerinalang.model.elements.PackageID.ERROR;
import static org.ballerinalang.model.elements.PackageID.FLOAT;
import static org.ballerinalang.model.elements.PackageID.FUTURE;
import static org.ballerinalang.model.elements.PackageID.INT;
import static org.ballerinalang.model.elements.PackageID.INTERNAL;
import static org.ballerinalang.model.elements.PackageID.JAVA;
import static org.ballerinalang.model.elements.PackageID.MAP;
import static org.ballerinalang.model.elements.PackageID.OBJECT;
import static org.ballerinalang.model.elements.PackageID.QUERY;
import static org.ballerinalang.model.elements.PackageID.STREAM;
import static org.ballerinalang.model.elements.PackageID.STRING;
import static org.ballerinalang.model.elements.PackageID.TABLE;
import static org.ballerinalang.model.elements.PackageID.TRANSACTION;
import static org.ballerinalang.model.elements.PackageID.TYPEDESC;
import static org.ballerinalang.model.elements.PackageID.VALUE;
import static org.ballerinalang.model.elements.PackageID.XML;

/**
 * Load lang libs and define their symbols.
 *
 * @since 2.0.0
 */
public class Bootstrap {

    private final PackageResolver packageResolver;
    private boolean langLibLoaded = false;

    public Bootstrap(PackageResolver packageResolver) {
        this.packageResolver = packageResolver;
    }

    void loadLangLib(CompilerContext compilerContext, PackageID langLib) {
        if (langLibLoaded) {
            return;
        }

        langLibLoaded = true;

        if (langLib.equals(ANNOTATIONS)) {
            return; // Nothing else to load.
        }

        SymbolResolver symResolver = SymbolResolver.getInstance(compilerContext);
        SymbolTable symbolTable = SymbolTable.getInstance(compilerContext);

        // load annotation
        symbolTable.langAnnotationModuleSymbol = loadLib(ANNOTATIONS, compilerContext);

        symResolver.reloadErrorAndDependentTypes();

        if (langLib.equals(JAVA)) {
            return; // Nothing else to load.
        }

        // load java
        symbolTable.langJavaModuleSymbol = loadLib(JAVA, compilerContext);

        if (langLib.equals(INTERNAL)) {
            return; // Nothing else to load.
        }

        // load internal
        symbolTable.langInternalModuleSymbol = loadLib(INTERNAL, compilerContext);

        if (langLib.equals(QUERY)) {
            // Query module requires stream, array, map, string, table, xml & value modules. Hence loading them.
            symbolTable.langArrayModuleSymbol = loadLib(ARRAY, compilerContext);
            symbolTable.langMapModuleSymbol = loadLib(MAP, compilerContext);
            symbolTable.langStringModuleSymbol = loadLib(STRING, compilerContext);
            symbolTable.langValueModuleSymbol = loadLib(VALUE, compilerContext);
            symbolTable.langXmlModuleSymbol = loadLib(XML, compilerContext);
            symbolTable.langTableModuleSymbol = loadLib(TABLE, compilerContext);
            symbolTable.langStreamModuleSymbol = loadLib(STREAM, compilerContext);
        }

        if (langLib.equals(TRANSACTION)) {
            // Transaction module requires array, map, string, value modules. Hence loading them.
            symbolTable.langArrayModuleSymbol = loadLib(ARRAY, compilerContext);
            symbolTable.langMapModuleSymbol = loadLib(MAP, compilerContext);
            symbolTable.langStringModuleSymbol = loadLib(STRING, compilerContext);
            symbolTable.langValueModuleSymbol = loadLib(VALUE, compilerContext);
            symbolTable.langErrorModuleSymbol = loadLib(ERROR, compilerContext);
        }

        symResolver.reloadIntRangeType();
    }

    public void loadLangLibSymbols(CompilerContext compilerContext) {
        SymbolResolver symResolver = SymbolResolver.getInstance(compilerContext);
        SymbolTable symbolTable = SymbolTable.getInstance(compilerContext);

        // we will load any lang.lib found in cache directory
        symbolTable.langAnnotationModuleSymbol = loadLib(ANNOTATIONS, compilerContext);
        symbolTable.langJavaModuleSymbol = loadLib(JAVA, compilerContext);
        symbolTable.langInternalModuleSymbol = loadLib(INTERNAL, compilerContext);
        symResolver.reloadErrorAndDependentTypes();
        symResolver.reloadIntRangeType();
        symbolTable.langArrayModuleSymbol = loadLib(ARRAY, compilerContext);
        symbolTable.langDecimalModuleSymbol = loadLib(DECIMAL, compilerContext);
        symbolTable.langErrorModuleSymbol = loadLib(ERROR, compilerContext);
        symbolTable.langFloatModuleSymbol = loadLib(FLOAT, compilerContext);
        symbolTable.langFutureModuleSymbol = loadLib(FUTURE, compilerContext);
        symbolTable.langIntModuleSymbol = loadLib(INT, compilerContext);
        symbolTable.langMapModuleSymbol = loadLib(MAP, compilerContext);
        symbolTable.langObjectModuleSymbol = loadLib(OBJECT, compilerContext);
        symResolver.loadRawTemplateType();
        symbolTable.langStreamModuleSymbol = loadLib(STREAM, compilerContext);
        symbolTable.langTableModuleSymbol = loadLib(TABLE, compilerContext);
        symbolTable.langStringModuleSymbol = loadLib(STRING, compilerContext);
        symbolTable.langTypedescModuleSymbol = loadLib(TYPEDESC, compilerContext);
        symbolTable.langValueModuleSymbol = loadLib(VALUE, compilerContext);
        symbolTable.langXmlModuleSymbol = loadLib(XML, compilerContext);
        symbolTable.langBooleanModuleSymbol = loadLib(BOOLEAN, compilerContext);
        symbolTable.langQueryModuleSymbol = loadLib(QUERY, compilerContext);
        symbolTable.langTransactionModuleSymbol = loadLib(TRANSACTION, compilerContext);
        symbolTable.loadPredeclaredModules();
        symResolver.loadFunctionalConstructors();
    }

    private BPackageSymbol loadLib(PackageID langLib, CompilerContext compilerContext) {
        ModuleLoadRequest langLibLoadRequest = toModuleRequest(langLib);
        loadLib(langLibLoadRequest);

        return getSymbolFromCache(compilerContext, langLib);
    }

    private void loadLib(ModuleLoadRequest lib) {
        Collection<ModuleLoadResponse> modules = packageResolver.loadPackages(Collections.singletonList(lib));
        modules.forEach(module -> {
            Package pkg = packageResolver.getPackage(module.packageId());
            PackageCompilation compilation = pkg.getCompilation();
            if (compilation.diagnostics().size() > 0) {
                throw new RuntimeException("Error while bootstrapping :" + pkg.packageId().toString() +
                        " diagnostics: " + compilation.diagnostics());
            }
        });
    }

    private ModuleLoadRequest toModuleRequest(PackageID packageID) {
        PackageName packageName = PackageName.from(packageID.name.getValue());
        ModuleName moduleName = ModuleName.from(packageName);
        SemanticVersion version = SemanticVersion.from(packageID.getPackageVersion().toString());
        return new ModuleLoadRequest(packageID.orgName.getValue(), packageName, moduleName, version);
    }

    private BPackageSymbol getSymbolFromCache(CompilerContext context, PackageID packageID) {
        PackageCache pkgCache = PackageCache.getInstance(context);
        BLangPackage bLangPackage = pkgCache.get(packageID);
        if (bLangPackage != null) {
            return bLangPackage.symbol;
        }
        return pkgCache.getSymbol(packageID);
    }
}

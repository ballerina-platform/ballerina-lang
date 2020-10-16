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
import static org.ballerinalang.model.elements.PackageID.ERROR;
import static org.ballerinalang.model.elements.PackageID.INTERNAL;
import static org.ballerinalang.model.elements.PackageID.JAVA;
import static org.ballerinalang.model.elements.PackageID.MAP;
import static org.ballerinalang.model.elements.PackageID.QUERY;
import static org.ballerinalang.model.elements.PackageID.STREAM;
import static org.ballerinalang.model.elements.PackageID.STRING;
import static org.ballerinalang.model.elements.PackageID.TABLE;
import static org.ballerinalang.model.elements.PackageID.TRANSACTION;
import static org.ballerinalang.model.elements.PackageID.VALUE;
import static org.ballerinalang.model.elements.PackageID.XML;

/**
 * Load lang libs and define their symbols.
 *
 * @since 2.0.0
 */
class Bootstrap {

    private boolean langLibLoaded = false;

    void loadLangLib(CompilerContext compilerContext, PackageResolver packageResolver, PackageID langLib) {
        if (langLibLoaded) {
            return;
        }

        langLibLoaded = true;
        // we will load any lang.lib found in cache directory

//        if (!PackageID.isLangLibPackageID(langLib)) {
//            return;
//        }

        SymbolResolver symResolver = SymbolResolver.getInstance(compilerContext);
        SymbolTable symbolTable = SymbolTable.getInstance(compilerContext);

        // load annotation
        symbolTable.langAnnotationModuleSymbol = loadLib(ANNOTATIONS, packageResolver, compilerContext);

        if (langLib.equals(ANNOTATIONS)) {
            return; // Nothing else to load.
        }

        symResolver.reloadErrorAndDependentTypes();

        // load java
        symbolTable.langJavaModuleSymbol = loadLib(JAVA, packageResolver, compilerContext);

        if (langLib.equals(JAVA)) {
            return; // Nothing else to load.
        }

        symbolTable.langInternalModuleSymbol = loadLib(INTERNAL, packageResolver, compilerContext);

        if (langLib.equals(INTERNAL)) {
            return; // Nothing else to load.
        }

        symbolTable.langArrayModuleSymbol = loadLib(ARRAY, packageResolver, compilerContext);

        if (langLib.equals(QUERY)) {
            // Query module requires stream, array, map, string, table, xml & value modules. Hence loading them.
            symbolTable.langArrayModuleSymbol = loadLib(ARRAY, packageResolver, compilerContext);
            symbolTable.langMapModuleSymbol = loadLib(MAP, packageResolver, compilerContext);
            symbolTable.langStringModuleSymbol = loadLib(STRING, packageResolver, compilerContext);
            symbolTable.langValueModuleSymbol = loadLib(VALUE, packageResolver, compilerContext);
            symbolTable.langXmlModuleSymbol = loadLib(XML, packageResolver, compilerContext);
            symbolTable.langTableModuleSymbol = loadLib(TABLE, packageResolver, compilerContext);
            symbolTable.langStreamModuleSymbol = loadLib(STREAM, packageResolver, compilerContext);
        }

        if (langLib.equals(TRANSACTION)) {
            // Transaction module requires array, map, string, value modules. Hence loading them.
            symbolTable.langArrayModuleSymbol = loadLib(ARRAY, packageResolver, compilerContext);
            symbolTable.langMapModuleSymbol = loadLib(MAP, packageResolver, compilerContext);
            symbolTable.langStringModuleSymbol = loadLib(STRING, packageResolver, compilerContext);
            symbolTable.langValueModuleSymbol = loadLib(VALUE, packageResolver, compilerContext);
            symbolTable.langErrorModuleSymbol = loadLib(ERROR, packageResolver, compilerContext);
        }

        symResolver.reloadIntRangeType();

        loadLib(langLib, packageResolver, compilerContext);
    }

    private BPackageSymbol loadLib(PackageID langLib, PackageResolver packageResolver,
                                   CompilerContext compilerContext) {
        ModuleLoadRequest langLibLoadRequest = toModuleRequest(langLib);
        loadLib(langLibLoadRequest, packageResolver);

        return getSymbolFromCache(compilerContext, langLib);
    }

    private void loadLib(ModuleLoadRequest lib, PackageResolver packageResolver) {
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
        return null;
    }
}

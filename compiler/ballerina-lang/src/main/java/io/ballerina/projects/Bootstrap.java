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

import io.ballerina.projects.environment.PackageResolver;
import io.ballerina.projects.environment.ResolutionResponse;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolResolver;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.Collections;
import java.util.List;

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
        symbolTable.langAnnotationModuleSymbol = loadLangLibFromBala(ANNOTATIONS, compilerContext);

        symResolver.bootstrapJsonType();
        symResolver.bootstrapAnydataType();
        symResolver.boostrapErrorType();
        symResolver.defineOperators();

        if (langLib.equals(JAVA)) {
            return; // Nothing else to load.
        }

        // load java
        symbolTable.langJavaModuleSymbol = loadLangLibFromBala(JAVA, compilerContext);

        if (langLib.equals(INTERNAL)) {
            symbolTable.langObjectModuleSymbol = loadLangLibFromBala(OBJECT, compilerContext);
            symResolver.bootstrapIterableType();
            return;
        }

        // load internal
        if (langLib.equals(OBJECT)) {
            return; // Nothing else to load.
        }

        symbolTable.langObjectModuleSymbol = loadLangLibFromBala(OBJECT, compilerContext);
        symbolTable.langInternalModuleSymbol = loadLangLibFromBala(INTERNAL, compilerContext);
        symResolver.bootstrapIntRangeType();
        symResolver.bootstrapIterableType();

        if (langLib.equals(QUERY)) {
            // Query module requires stream, array, map, string, table, xml & value modules. Hence loading them.
            symbolTable.langXmlModuleSymbol = loadLangLibFromBala(XML, compilerContext);
            symbolTable.langTableModuleSymbol = loadLangLibFromBala(TABLE, compilerContext);
            symbolTable.langStreamModuleSymbol = loadLangLibFromBala(STREAM, compilerContext);
            symbolTable.updateStringSubtypeOwners();
            symbolTable.updateXMLSubtypeOwners();
        }

        if (langLib.equals(TRANSACTION)) {
            // Transaction module requires array, map, string, value modules. Hence loading them.
            symbolTable.langErrorModuleSymbol = loadLangLibFromBala(ERROR, compilerContext);
            symbolTable.langObjectModuleSymbol = loadLangLibFromBala(OBJECT, compilerContext);
        }

        if (langLib.equals(TRANSACTION) || langLib.equals(QUERY)) {
            symbolTable.langArrayModuleSymbol = loadLangLibFromBala(ARRAY, compilerContext);
            symbolTable.langMapModuleSymbol = loadLangLibFromBala(MAP, compilerContext);
            symbolTable.langValueModuleSymbol = loadLangLibFromBala(VALUE, compilerContext);
            symbolTable.langStringModuleSymbol = loadLangLibFromBala(STRING, compilerContext);
            symbolTable.updateStringSubtypeOwners();
        }

        if (langLib.equals(ERROR)) {
            symbolTable.langValueModuleSymbol = loadLangLibFromBala(VALUE, compilerContext);
        }

        symResolver.bootstrapCloneableType();
        symResolver.defineOperators();
    }

    public void loadLangLibSymbols(CompilerContext compilerContext) {
        SymbolResolver symResolver = SymbolResolver.getInstance(compilerContext);
        SymbolTable symbolTable = SymbolTable.getInstance(compilerContext);

        // we will load any lang.lib found in cache directory
        symbolTable.langAnnotationModuleSymbol = loadLangLibFromBala(ANNOTATIONS, compilerContext);
        symbolTable.langJavaModuleSymbol = loadLangLibFromBala(JAVA, compilerContext);
        symbolTable.langInternalModuleSymbol = loadLangLibFromBala(INTERNAL, compilerContext);
        symbolTable.langValueModuleSymbol = loadLangLibFromBala(VALUE, compilerContext);
        symResolver.bootstrapJsonType();
        symResolver.bootstrapAnydataType();
        symResolver.boostrapErrorType();
        symResolver.bootstrapCloneableType();
        symResolver.bootstrapIntRangeType();
        symbolTable.langArrayModuleSymbol = loadLangLibFromBala(ARRAY, compilerContext);
        symbolTable.langDecimalModuleSymbol = loadLangLibFromBala(DECIMAL, compilerContext);
        symbolTable.langErrorModuleSymbol = loadLangLibFromBala(ERROR, compilerContext);
        symbolTable.langFloatModuleSymbol = loadLangLibFromBala(FLOAT, compilerContext);
        symbolTable.langFutureModuleSymbol = loadLangLibFromBala(FUTURE, compilerContext);
        symbolTable.langIntModuleSymbol = loadLangLibFromBala(INT, compilerContext);
        symbolTable.langMapModuleSymbol = loadLangLibFromBala(MAP, compilerContext);
        symbolTable.langObjectModuleSymbol = loadLangLibFromBala(OBJECT, compilerContext);
        symResolver.loadRawTemplateType();
        symResolver.bootstrapIterableType();
        symbolTable.langStreamModuleSymbol = loadLangLibFromBala(STREAM, compilerContext);
        symbolTable.langTableModuleSymbol = loadLangLibFromBala(TABLE, compilerContext);
        symbolTable.langStringModuleSymbol = loadLangLibFromBala(STRING, compilerContext);
        symbolTable.langTypedescModuleSymbol = loadLangLibFromBala(TYPEDESC, compilerContext);
        symbolTable.langXmlModuleSymbol = loadLangLibFromBala(XML, compilerContext);
        symbolTable.langBooleanModuleSymbol = loadLangLibFromBala(BOOLEAN, compilerContext);
        symbolTable.langQueryModuleSymbol = loadLangLibFromBala(QUERY, compilerContext);
        symbolTable.langTransactionModuleSymbol = loadLangLibFromBala(TRANSACTION, compilerContext);
        symbolTable.loadPredeclaredModules();
        symResolver.bootstrapIntRangeType();
        symbolTable.updateBuiltinSubtypeOwners();
        symResolver.defineOperators();
    }

    private BPackageSymbol loadLangLibFromBala(PackageID langLib, CompilerContext compilerContext) {
        PackageDescriptor packageDescriptor = toPackageLoadRequest(langLib);
        loadLangLibFromBala(packageDescriptor);

        return getSymbolFromCache(compilerContext, langLib);
    }

    private void loadLangLibFromBala(PackageDescriptor packageDescriptor) {
        List<ResolutionResponse> resolutionResponses = packageResolver.resolvePackages(
                Collections.singletonList(packageDescriptor), true);
        resolutionResponses.forEach(pkgLoadResp -> {
            Package pkg = pkgLoadResp.resolvedPackage();
            PackageCompilation compilation = pkg.getCompilation();
            if (compilation.diagnosticResult().hasErrors()) {
                throw new ProjectException("Error while bootstrapping :" + pkg.packageId().toString() +
                        " diagnostics: " + compilation.diagnosticResult());
            }
        });
    }

    private PackageDescriptor toPackageLoadRequest(PackageID packageID) {
        PackageOrg pkgOrg = PackageOrg.from(packageID.orgName.getValue());
        PackageName pkgName = PackageName.from(packageID.name.getValue());
        PackageVersion pkgVersion = PackageVersion.from(packageID.getPackageVersion().toString());
        return PackageDescriptor.from(pkgOrg, pkgName, pkgVersion);
    }

    private BPackageSymbol getSymbolFromCache(CompilerContext context, PackageID packageID) {
        org.wso2.ballerinalang.compiler.PackageCache pkgCache =
                org.wso2.ballerinalang.compiler.PackageCache.getInstance(context);
        BLangPackage bLangPackage = pkgCache.get(packageID);
        if (bLangPackage != null) {
            return bLangPackage.symbol;
        }
        return pkgCache.getSymbol(packageID);
    }
}

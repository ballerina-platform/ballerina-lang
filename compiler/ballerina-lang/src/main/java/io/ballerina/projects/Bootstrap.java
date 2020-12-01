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
import io.ballerina.projects.environment.ResolutionRequest;
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
        symbolTable.langAnnotationModuleSymbol = loadLangLibFromBalr(ANNOTATIONS, compilerContext);

        symResolver.reloadErrorAndDependentTypes();

        if (langLib.equals(JAVA)) {
            return; // Nothing else to load.
        }

        // load java
        symbolTable.langJavaModuleSymbol = loadLangLibFromBalr(JAVA, compilerContext);

        if (langLib.equals(INTERNAL)) {
            return; // Nothing else to load.
        }

        // load internal
        symbolTable.langInternalModuleSymbol = loadLangLibFromBalr(INTERNAL, compilerContext);

        if (langLib.equals(QUERY)) {
            // Query module requires stream, array, map, string, table, xml & value modules. Hence loading them.
            symbolTable.langArrayModuleSymbol = loadLangLibFromBalr(ARRAY, compilerContext);
            symbolTable.langMapModuleSymbol = loadLangLibFromBalr(MAP, compilerContext);
            symbolTable.langStringModuleSymbol = loadLangLibFromBalr(STRING, compilerContext);
            symbolTable.langValueModuleSymbol = loadLangLibFromBalr(VALUE, compilerContext);
            symbolTable.langXmlModuleSymbol = loadLangLibFromBalr(XML, compilerContext);
            symbolTable.langTableModuleSymbol = loadLangLibFromBalr(TABLE, compilerContext);
            symbolTable.langStreamModuleSymbol = loadLangLibFromBalr(STREAM, compilerContext);
        }

        if (langLib.equals(TRANSACTION)) {
            // Transaction module requires array, map, string, value modules. Hence loading them.
            symbolTable.langArrayModuleSymbol = loadLangLibFromBalr(ARRAY, compilerContext);
            symbolTable.langMapModuleSymbol = loadLangLibFromBalr(MAP, compilerContext);
            symbolTable.langStringModuleSymbol = loadLangLibFromBalr(STRING, compilerContext);
            symbolTable.langValueModuleSymbol = loadLangLibFromBalr(VALUE, compilerContext);
            symbolTable.langErrorModuleSymbol = loadLangLibFromBalr(ERROR, compilerContext);
        }

        symResolver.reloadIntRangeType();
    }

    public void loadLangLibSymbols(CompilerContext compilerContext) {
        SymbolResolver symResolver = SymbolResolver.getInstance(compilerContext);
        SymbolTable symbolTable = SymbolTable.getInstance(compilerContext);

        // we will load any lang.lib found in cache directory
        symbolTable.langAnnotationModuleSymbol = loadLangLibFromBalr(ANNOTATIONS, compilerContext);
        symbolTable.langJavaModuleSymbol = loadLangLibFromBalr(JAVA, compilerContext);
        symbolTable.langInternalModuleSymbol = loadLangLibFromBalr(INTERNAL, compilerContext);
        symResolver.reloadErrorAndDependentTypes();
        symResolver.reloadIntRangeType();
        symbolTable.langArrayModuleSymbol = loadLangLibFromBalr(ARRAY, compilerContext);
        symbolTable.langDecimalModuleSymbol = loadLangLibFromBalr(DECIMAL, compilerContext);
        symbolTable.langErrorModuleSymbol = loadLangLibFromBalr(ERROR, compilerContext);
        symbolTable.langFloatModuleSymbol = loadLangLibFromBalr(FLOAT, compilerContext);
        symbolTable.langFutureModuleSymbol = loadLangLibFromBalr(FUTURE, compilerContext);
        symbolTable.langIntModuleSymbol = loadLangLibFromBalr(INT, compilerContext);
        symbolTable.langMapModuleSymbol = loadLangLibFromBalr(MAP, compilerContext);
        symbolTable.langObjectModuleSymbol = loadLangLibFromBalr(OBJECT, compilerContext);
        symResolver.loadRawTemplateType();
        symbolTable.langStreamModuleSymbol = loadLangLibFromBalr(STREAM, compilerContext);
        symbolTable.langTableModuleSymbol = loadLangLibFromBalr(TABLE, compilerContext);
        symbolTable.langStringModuleSymbol = loadLangLibFromBalr(STRING, compilerContext);
        symbolTable.langTypedescModuleSymbol = loadLangLibFromBalr(TYPEDESC, compilerContext);
        symbolTable.langValueModuleSymbol = loadLangLibFromBalr(VALUE, compilerContext);
        symbolTable.langXmlModuleSymbol = loadLangLibFromBalr(XML, compilerContext);
        symbolTable.langBooleanModuleSymbol = loadLangLibFromBalr(BOOLEAN, compilerContext);
        symbolTable.langQueryModuleSymbol = loadLangLibFromBalr(QUERY, compilerContext);
        symbolTable.langTransactionModuleSymbol = loadLangLibFromBalr(TRANSACTION, compilerContext);
        symbolTable.loadPredeclaredModules();
    }

    private BPackageSymbol loadLangLibFromBalr(PackageID langLib, CompilerContext compilerContext) {
        ResolutionRequest packageLoadRequest = toPackageLoadRequest(langLib);
        loadLangLibFromBalr(packageLoadRequest);

        return getSymbolFromCache(compilerContext, langLib);
    }

    private void loadLangLibFromBalr(ResolutionRequest packageLoadRequest) {
        List<ResolutionResponse> resolutionResponses = packageResolver.resolvePackages(
                Collections.singletonList(packageLoadRequest));
        resolutionResponses.forEach(pkgLoadResp -> {
            Package pkg = pkgLoadResp.resolvedPackage();
            PackageCompilation compilation = pkg.getCompilation();
            if (compilation.diagnosticResult().hasErrors()) {
                throw new ProjectException("Error while bootstrapping :" + pkg.packageId().toString() +
                        " diagnostics: " + compilation.diagnosticResult());
            }
        });
    }

    private ResolutionRequest toPackageLoadRequest(PackageID packageID) {
        PackageOrg pkgOrg = PackageOrg.from(packageID.orgName.getValue());
        PackageName pkgName = PackageName.from(packageID.name.getValue());
        PackageVersion pkgVersion = PackageVersion.from(packageID.getPackageVersion().toString());
        PackageDescriptor packageDescriptor = PackageDescriptor.from(pkgOrg, pkgName, pkgVersion);
        return ResolutionRequest.from(packageDescriptor, PackageDependencyScope.DEFAULT);
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

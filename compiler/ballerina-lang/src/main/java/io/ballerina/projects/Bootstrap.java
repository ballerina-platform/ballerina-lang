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
import static org.ballerinalang.model.elements.PackageID.JAVA;

/**
 * Load lang libs and define their symbols.
 *
 * @since 2.0.0
 */
class Bootstrap {

    private static final Bootstrap instance = new Bootstrap();
    public static Bootstrap getInstance() {
        return instance;
    }

    private boolean langLibLoaded = false;

    void loadLangLib(CompilerContext compilerContext, PackageResolver packageResolver, PackageID langLib) {
        if (langLibLoaded) {
            return;
        }

        langLibLoaded = true;
        // we will load any lang.lib found in cache directory

        if (!PackageID.isLangLibPackageID(langLib)) {
            return;
        }

        SymbolResolver symResolver = SymbolResolver.getInstance(compilerContext);
        SymbolTable symbolTable = SymbolTable.getInstance(compilerContext);

        // load annotation
        ModuleLoadRequest annotation = toModuleRequest(ANNOTATIONS);
        loadLib(annotation, packageResolver);
        if (getSymbolFromCache(compilerContext, ANNOTATIONS) != null) {
            symbolTable.langAnnotationModuleSymbol = getSymbolFromCache(compilerContext, ANNOTATIONS);
        }

        if (langLib.equals(ANNOTATIONS)) {
            return;
        }

        symResolver.reloadErrorAndDependentTypes();

        // load java
        ModuleLoadRequest java = toModuleRequest(JAVA);
        loadLib(java, packageResolver);

        if (getSymbolFromCache(compilerContext, JAVA) != null) {
            symbolTable.langJavaModuleSymbol = getSymbolFromCache(compilerContext, JAVA);
        }

        // TODO add the rest of the langlibs to load
    }

    private void loadLib(ModuleLoadRequest lib, PackageResolver packageResolver) {
        Collection<ModuleLoadResponse> modules = packageResolver.loadPackages(Collections.singletonList(lib));
        modules.forEach(module -> {
            Package pkg = packageResolver.getPackage(module.packageId());
            PackageCompilation compilation = pkg.getCompilation();
            if (compilation.diagnostics().size() > 0) {
                throw new RuntimeException("Error while bootstraping :" + pkg.packageId().toString());
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
        if (bLangPackage != null){
            return bLangPackage.symbol;
        }
        return null;
    }
}

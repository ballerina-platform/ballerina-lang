/**
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.compiler;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.PackageLoader;
import org.wso2.ballerinalang.compiler.semantics.analyzer.CodeAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SemanticAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads the Ballerina builtin core and builtin packages.
 */
public class LSPackageLoader {

    private static final String[] STATIC_PKG_NAMES = {"http", "swagger", "mime", "auth", "cache", "config", "sql",
            "file", "internal", "io", "jwt", "log", "math", "os", "reflect", "runtime", "security.crypto", "task",
            "time", "transactions", "user", "util", "builtin"};

    /**
     * Get the Builtin Package.
     * @return {@link BLangPackage} Builtin BLang package
     */
    public static List<BLangPackage> getBuiltinPackages(CompilerContext context) {
        List<BLangPackage> builtins = new ArrayList<>();
        PackageLoader pkgLoader = PackageLoader.getInstance(context);
        SemanticAnalyzer semAnalyzer = SemanticAnalyzer.getInstance(context);
        CodeAnalyzer codeAnalyzer = CodeAnalyzer.getInstance(context);
        BLangPackage builtInPkg = codeAnalyzer
                .analyze(semAnalyzer.analyze(pkgLoader
                        .loadAndDefinePackage(Names.BUILTIN_ORG.value, Names.BUILTIN_PACKAGE.getValue())));
        builtins.add(builtInPkg);

        return builtins;
    }

    /**
     * Get the package by ID via Package loader.
     * @param context               Compiler context
     * @param packageID             Package ID to resolve
     * @return {@link BLangPackage} Resolved BLang Package
     */
    public static BLangPackage getPackageById(CompilerContext context, PackageID packageID) {
        PackageLoader pkgLoader = PackageLoader.getInstance(context);
        return pkgLoader.loadAndDefinePackage(packageID);
    }

    /**
     * Returns a static packages list.
     *
     * @return static packages list
     */
    public static String[] getStaticPkgNames() {
        return STATIC_PKG_NAMES.clone();
    }
}

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
package org.ballerinalang.langserver;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.PackageLoader;
import org.wso2.ballerinalang.compiler.semantics.analyzer.CodeAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SemanticAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.SOURCE_ROOT;

/**
 * Loads the Ballerina builtin core and builtin packages.
 */
public class BallerinaPackageLoader {
    
    private static final int MAX_DEPTH = 10;

    /**
     * Get the Builtin Package.
     * @return {@link BLangPackage} Builtin BLang package
     */
    public static List<BLangPackage> getBuiltinPackages() {
        List<BLangPackage> builtins = new ArrayList<>();
        CompilerContext context = prepareCompilerContext();
        BLangPackage builtInPkg = getPackageByName(context, Names.BUILTIN_PACKAGE.getValue());
        builtins.add(builtInPkg);
 
        return builtins;
    }

    /**
     * Get the packages by name.
     *
     * @param name                  name of the package
     * @return {@link BLangPackage} blang package
     */
    public static BLangPackage getPackageByName(CompilerContext context, String name) {
        PackageLoader pkgLoader = PackageLoader.getInstance(context);
        SemanticAnalyzer semAnalyzer = SemanticAnalyzer.getInstance(context);
        CodeAnalyzer codeAnalyzer = CodeAnalyzer.getInstance(context);
        return codeAnalyzer.analyze(semAnalyzer.analyze(pkgLoader.loadEntryPackage(name)));
    }
    
    public static CompilerContext prepareCompilerContext() {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(SOURCE_ROOT, "");
        options.put(COMPILER_PHASE, CompilerPhase.DESUGAR.toString());
        options.put(PRESERVE_WHITESPACE, "false");
        
        return context;
    }

    /**
     * Get the packages set.
     * @param context       Current CompilerContext
     * @param maxDepth      Max depth to be searched
     * @return              {@link Set} set of packages
     */
    public static Set<PackageID> getPackageList(CompilerContext context, int maxDepth) {
        PackageLoader pkgLoader = PackageLoader.getInstance(context);
        return pkgLoader.listPackages(Math.max(MAX_DEPTH, maxDepth));
    }
}

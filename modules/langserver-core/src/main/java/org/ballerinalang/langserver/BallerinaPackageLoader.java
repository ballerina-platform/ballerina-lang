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
import org.wso2.ballerinalang.compiler.PackageLoader;
import org.wso2.ballerinalang.compiler.semantics.analyzer.CodeAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SemanticAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.SOURCE_ROOT;

/**
 * Loads the Ballerina builtin core and builtin packages.
 */
public class BallerinaPackageLoader {

    /**
     * Get the Builtin Package.
     * @return {@link BLangPackage} Builtin BLang package
     */
    public static List<BLangPackage> getBuiltinPackages() {
        List<BLangPackage> builtins = new ArrayList<>();
        CompilerContext context = prepareCompilerContext();
        BLangPackage builtInCorePkg = getPackageByName(context, Names.BUILTIN_CORE_PACKAGE.getValue());
        BLangPackage builtInPkg = getPackageByName(context, Names.BUILTIN_PACKAGE.getValue());
        builtins.add(builtInCorePkg);
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
}

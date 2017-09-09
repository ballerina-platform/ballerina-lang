/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler;

import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

/**
 * @since 0.94
 */
public class Compiler {

    private static final CompilerContext.Key<Compiler> COMPILER_KEY =
            new CompilerContext.Key<>();

    private PackageLoader pkgLoader;

    public static Compiler getInstance(CompilerContext context) {
        Compiler compiler = context.get(COMPILER_KEY);
        if (compiler == null) {
            compiler = new Compiler(context);
        }
        return compiler;
    }

    public Compiler(CompilerContext context) {
        //TODO This constructor should accept command line arguments and other compiler arguments
        context.put(COMPILER_KEY, this);

        this.pkgLoader = PackageLoader.getInstance(context);
    }

    public void compile(String sourcePkg) {
        BPackageSymbol pSymbol = pkgLoader.loadEntryPackage(sourcePkg);
        // TODO Impliment CompilerPolicy.. Phases, PARSE, SEMANTIC_ANALYSIS, CODE_ANALYSIS, CODEGEN etc.
    }
}

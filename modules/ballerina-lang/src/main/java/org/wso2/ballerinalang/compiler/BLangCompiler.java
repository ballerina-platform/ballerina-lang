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

import org.wso2.ballerinalang.compiler.parser.BLangParser;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

/**
 * @since 0.94
 */
public class BLangCompiler {
    private static final CompilerContext.Key<BLangCompiler> bLangCompilerKey = new CompilerContext.Key<>();

    private CompilerContext context;
    private BLangParser parser;

    public static BLangCompiler getInstance(CompilerContext context) {
        BLangCompiler compiler = context.get(bLangCompilerKey);
        if (compiler == null) {
            compiler = new BLangCompiler(context);
        }

        return compiler;
    }

    public BLangCompiler(CompilerContext context) {
        //TODO This constructor should accept command line arguments and other compiler arguments
        this.context = context;
        this.context.put(bLangCompilerKey, this);

        parser = BLangParser.getInstance(context);
    }

    public void compile() {

        // TODO Parse entry package
        // TODO Define all the symbols in the entry package

    }

    // TODO Define Scopes and Symbols
    // TODO Then Enter symbols to scopes
    // TODO During the above process load imported packages.


}

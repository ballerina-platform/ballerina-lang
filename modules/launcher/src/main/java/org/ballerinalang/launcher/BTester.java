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
package org.ballerinalang.launcher;

import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PRESERVE_WHITESPACE;
import static org.ballerinalang.compiler.CompilerOptionName.SOURCE_ROOT;

/**
 * Tester class.
 */
public class BTester {

    private static CompilerOptions options;

    public static void main(String[] args) throws Exception {
        // -sorceroot == current directory

        CompilerContext context = new CompilerContext();
        options = CompilerOptions.getInstance(context);
//        options.put(SOURCE_ROOT, System.getProperty("user.dir"));
        options.put(SOURCE_ROOT, System.getProperty("user.dir") + "/bal-src");
        options.put(COMPILER_PHASE, "codeGen");
        options.put(PRESERVE_WHITESPACE, "false");

        // How to set a custom diagnostic listener
//        DiagnosticListener listener = diagnostic -> out.println(diagnostic.getMessage());
        //context.put(DiagnosticListener.class, listener);


        // How to set a custom program dir package repository
        //context.put(PackageRepository.class, repo);

        Compiler compiler = Compiler.getInstance(context);
//        compiler.compile("bar.bal");
//        compiler.compile("pkg1.bal");
        compiler.compile("org.old.man");
//        compiler.compile("a.b.c");
    }

}

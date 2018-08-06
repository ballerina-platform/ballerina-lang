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
package org.ballerinalang.packerina;

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.testerina.util.Utils;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.ballerinalang.compiler.CompilerOptionName.BUILD_COMPILED_PACKAGE;
import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
/**
 * This class provides util methods for building Ballerina programs and packages.
 *
 * @since 0.95.2
 */
public class BuilderUtils {
    private static PrintStream outStream = System.out;

    public static void compileWithTestsAndWrite(Path sourceRootPath,
                                                String packagePath,
                                                String targetPath,
                                                boolean buildCompiledPkg,
                                                boolean offline,
                                                boolean lockEnabled,
                                                boolean skiptests) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRootPath.toString());
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());
        options.put(BUILD_COMPILED_PACKAGE, Boolean.toString(buildCompiledPkg));
        options.put(OFFLINE, Boolean.toString(offline));
        options.put(LOCK_ENABLED, Boolean.toString(lockEnabled));
        options.put(SKIP_TESTS, Boolean.toString(skiptests));

        Compiler compiler = Compiler.getInstance(context);
        BLangPackage bLangPackage = compiler.build(packagePath);

        if (skiptests) {
            outStream.println();
            compiler.write(bLangPackage, packagePath);
        } else {
            Utils.testWithBuild(sourceRootPath, Collections.singletonList(packagePath));
            compiler.write(bLangPackage, targetPath);
        }
    }

    public static void compileWithTestsAndWrite(Path sourceRootPath, boolean offline, boolean lockEnabled,
                                                boolean skiptests) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRootPath.toString());
        options.put(OFFLINE, Boolean.toString(offline));
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());
        options.put(LOCK_ENABLED, Boolean.toString(lockEnabled));
        options.put(SKIP_TESTS, Boolean.toString(skiptests));

        Compiler compiler = Compiler.getInstance(context);
        List<BLangPackage> packages = compiler.build();

        if (skiptests) {
            if (packages.size() > 0) {
                outStream.println();
                compiler.write(packages);
            } else {
                outStream.println("No ballerina source files to compile");
            }
        } else {
            if (packages.size() > 0) {
                Utils.testWithBuild(sourceRootPath, null);
                compiler.write(packages);
            } else {
                outStream.println("No ballerina source files to compile");
            }
        }
    }
}

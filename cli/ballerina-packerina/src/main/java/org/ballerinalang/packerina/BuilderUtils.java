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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.testerina.util.TesterinaUtils;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.programfile.CompiledBinaryFile;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.ballerinalang.compiler.CompilerOptionName.BUILD_COMPILED_MODULE;
import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;
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
        options.put(BUILD_COMPILED_MODULE, Boolean.toString(buildCompiledPkg));
        options.put(OFFLINE, Boolean.toString(offline));
        options.put(LOCK_ENABLED, Boolean.toString(lockEnabled));
        options.put(SKIP_TESTS, Boolean.toString(skiptests));
        options.put(TEST_ENABLED, "true");

        Compiler compiler = Compiler.getInstance(context);
        BLangPackage bLangPackage = compiler.build(packagePath);

        if (skiptests) {
            outStream.println();
            compiler.write(bLangPackage, targetPath);
        } else {
            runTests(compiler, sourceRootPath, Collections.singletonList(bLangPackage));
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
        options.put(TEST_ENABLED, "true");

        Compiler compiler = Compiler.getInstance(context);
        List<BLangPackage> packages = compiler.build();

        if (skiptests) {
            if (packages.size() == 0) {
                throw new BLangCompilerException("no ballerina source files found to compile");
            }
            outStream.println();
            compiler.write(packages);
        } else {
            if (packages.size() == 0) {
                throw new BLangCompilerException("no ballerina source files found to compile");
            }
            runTests(compiler, sourceRootPath, packages);
            compiler.write(packages);
        }
    }

    /**
     * Run tests in the build.
     *
     * @param compiler       compiler instance
     * @param sourceRootPath source root path
     * @param packageList    list of compiled packages
     */
    private static void runTests(Compiler compiler, Path sourceRootPath, List<BLangPackage> packageList) {
        Map<BLangPackage, CompiledBinaryFile.ProgramFile> programFileMap = new HashMap<>();
        // Only tests in packages are executed so default packages i.e. single bal files which has the package name
        // as "." are ignored. This is to be consistent with the "ballerina test" command which only executes tests
        // in packages.
        packageList.stream().filter(bLangPackage -> !bLangPackage.packageID.getName().equals(Names.DEFAULT_PACKAGE))
                   .forEach(bLangPackage -> {
                       CompiledBinaryFile.ProgramFile programFile;
                       if (bLangPackage.containsTestablePkg()) {
                           programFile = compiler.getExecutableProgram(bLangPackage.getTestablePkg());
                       } else {
                           // In this package there are no tests to be executed. But we need to say to the users that
                           // there are no tests found in the package to be executed as :
                           // Running tests
                           //     <org-name>/<package-name>:<version>
                           //         No tests found
                           programFile = compiler.getExecutableProgram(bLangPackage);
                       }

                       programFileMap.put(bLangPackage, programFile);
                   });

        if (programFileMap.size() > 0) {
            TesterinaUtils.executeTests(sourceRootPath, programFileMap);
        }
    }
}

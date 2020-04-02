/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.ballerinalang.toml.model.Manifest;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;

/**
 * This class provides util methods for listing dependencies of Ballerina packages.
 *
 * @since 0.97.0
 */
public class ListUtils {

    public static void list(Path sourceRootPath, String packagePath) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRootPath.toString());
        options.put(COMPILER_PHASE, CompilerPhase.DEFINE.toString());

        Compiler compiler = Compiler.getInstance(context);
        compiler.list(packagePath);
    }

    public static void list(Path sourceRootPath) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRootPath.toString());
        options.put(COMPILER_PHASE, CompilerPhase.DEFINE.toString());

        Compiler compiler = Compiler.getInstance(context);
        compiler.list();
    }

    /**
     * Get the non-default scope libraries defined in toml File.
     *
     * @param manifest Manifest object for a ballerina project.
     * @return Non-default scope module dependencies.
     */
    public static List<Path> getNonDefaultScopeLibraries(Manifest manifest) {
        List<Path> nonDefaultScopeLibs = new ArrayList<>();
        if (null != manifest.getPlatform().libraries) {
            nonDefaultScopeLibs = manifest.getPlatform().libraries.stream()
                    .filter(lib -> (lib.getScope() != null && (lib.getScope().equals("compile") ||
                            lib.getScope().equals("test"))))
                    .map(lib -> Paths.get(lib.getPath()).toAbsolutePath()).collect(Collectors.toList());
        }
        return nonDefaultScopeLibs;
    }

    /**
     * Remove the non-default scope libraries from module dependencies.
     *
     * @param manifest Manifest object for a ballerina project.
     * @param moduleDependencies Dependencies of a module.
     * @return Module dependencies without non default libraries.
     */
    public static HashSet<Path> removeNonDefaultScopeLibraries(Manifest manifest, HashSet<Path> moduleDependencies) {
        List<Path> nonDefaultScopeLibs = getNonDefaultScopeLibraries(manifest);
        HashSet<Path> moduleLibs = new HashSet<>();
        for (Path lib: moduleDependencies) {
            if (!nonDefaultScopeLibs.contains(lib)) {
                moduleLibs.add(lib);
            }
        }
        return moduleLibs;
    }

    /**
     * Remove the test-default scope libraries from module dependencies.
     *
     * @param manifest Manifest object for a ballerina project.
     * @param moduleDependencies Dependencies of a module.
     * @return Module dependencies without test scope libraries.
     */
    public static HashSet<Path> removeTestScopeLibraries(Manifest manifest, HashSet<Path> moduleDependencies) {
        HashSet<Path> moduleLibs = new HashSet<>();
        List<Path> testScopeLibs = new ArrayList<>();
        if (null != manifest.getPlatform().libraries) {
            testScopeLibs = manifest.getPlatform().libraries.stream()
                    .filter(lib -> lib.getScope() != null && lib.getScope().equals("test"))
                    .map(lib -> Paths.get(lib.getPath()).toAbsolutePath()).collect(Collectors.toList());
        }

        for (Path lib : moduleDependencies) {
            if (!testScopeLibs.contains(lib)) {
                moduleLibs.add(lib);
            }
        }
        return moduleLibs;
    }
}

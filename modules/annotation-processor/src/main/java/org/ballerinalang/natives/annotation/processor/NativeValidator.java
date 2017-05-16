/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.natives.annotation.processor;

import org.ballerinalang.model.GlobalScope;
import org.ballerinalang.model.NativeScope;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.BTypes;
import org.ballerinalang.natives.NativeConstructLoader;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.ballerinalang.util.exceptions.NativeException;
import org.ballerinalang.util.repository.BuiltinPackageRepository;
import org.ballerinalang.util.repository.PackageRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Class to process built-in ballerina sources and validate them.
 */
public class NativeValidator {

    /**
     * Process built-in ballerina sources and validate them.
     * 
     * @param args string array of arguments
     */
    public static void main(String[] args) {
        
        // Get package list
        String targetDir = args[0];
        List<String> builtInPackages = getBuiltInBalPackages(targetDir);
        
        // Load all natives to globalscope
        GlobalScope globalScope = GlobalScope.getInstance();
        NativeScope nativeScope = NativeScope.getInstance();

        loadConstructs(globalScope, nativeScope);

        //BuiltinPackageRepository[] pkgRepos = loadPackageRepositories();

        // create program
        // BLangProgram bLangProgram = new BLangProgram(globalScope, nativeScope, BLangProgram.Category.MAIN_PROGRAM);
        
        // turn off skipping native function parsing
        System.setProperty("skipNatives", "false");
        
        // process each package separately
        for (String builtInPkg : builtInPackages) {
            BLangSymbol pkgSymbol = nativeScope.resolve(new SymbolName(builtInPkg));
            //  BLangPackage nativePackage = (BLangPackage) ((NativePackageProxy) pkgSymbol).load();
            //  Path packagePath = Paths.get(builtInPkg.replace(".", File.separator));
            
            //  BLangPackage mainPackage = BLangPackages.loadPackage(packagePath, nativePackage.getPackageRepository(),
            //                    bLangProgram);
            //  bLangProgram.define(new SymbolName(mainPackage.getPackagePath()), mainPackage);
            //  bLangProgram.setMainPackage(mainPackage);

            //  Analyze the semantic properties of the Ballerina program
            //  SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer(bLangProgram);
            //  bLangProgram.accept(semanticAnalyzer);
        }
    }

    public PackageRepository.PackageSource resolveBuiltinPkg(BuiltinPackageRepository[] pkgRepos, Path pkgDirPath) {
        for (BuiltinPackageRepository pkgRepository : pkgRepos) {
            PackageRepository.PackageSource packageSource = pkgRepository.loadPackage(pkgDirPath);
            if (packageSource != null) {
                return packageSource;
            }
        }
        return null;
    }

    private static void loadConstructs(GlobalScope globalScope, NativeScope nativeScope) {
        BTypes.loadBuiltInTypes(globalScope);
        Iterator<NativeConstructLoader> nativeConstructLoaders =
            ServiceLoader.load(NativeConstructLoader.class).iterator();
        while (nativeConstructLoaders.hasNext()) {
            NativeConstructLoader constructLoader = nativeConstructLoaders.next();
            try {
                constructLoader.load(nativeScope);
            } catch (NativeException e) {
                throw e;
            } catch (Throwable t) {
                throw new NativeException("internal error occured", t);
            }
        }
    }

    /**
     * Load the native constructs to the provided symbol scope.
     */
    public static BuiltinPackageRepository[] loadPackageRepositories() {
        Iterator<BuiltinPackageRepository> ballerinaBuiltinPackageRepositories =
                ServiceLoader.load(BuiltinPackageRepository.class).iterator();
        List<BuiltinPackageRepository> pkgRepositories = new ArrayList<>();
        while (ballerinaBuiltinPackageRepositories.hasNext()) {
            BuiltinPackageRepository constructLoader = ballerinaBuiltinPackageRepositories.next();
            pkgRepositories.add(constructLoader);
        }
        return pkgRepositories.toArray(new BuiltinPackageRepository[0]);
    }
    
    private static List<String> getBuiltInBalPackages(String targetDir) {
        List<String> builtInPackages  = new ArrayList<String>();
        Path source = Paths.get(targetDir);

        // Traverse through built-in ballerina files and identify the packages
        try {
            Files.walkFileTree(source, new PackageFinder(source, builtInPackages));
        } catch (IOException e) {
            throw new BallerinaException("error while reading built-in packages: " + e.getMessage());
        }
        return builtInPackages;
    }
}

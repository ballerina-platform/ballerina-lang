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

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.CompilationUnitNode;
import org.ballerinalang.repository.CompositePackageRepository;
import org.ballerinalang.repository.PackageEntity;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.repository.PackageSource;
import org.ballerinalang.repository.fs.FSPackageRepository;
import org.wso2.ballerinalang.compiler.parser.Parser;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains methods to load a given package symbol.
 * It knows how to load source package as well as a package from a compiled version (.balo).
 *
 * @since 0.94
 */
public class PackageLoader {

    private static final CompilerContext.Key<PackageLoader> PACKAGE_LOADER_KEY =
            new CompilerContext.Key<>();

    private CompilerContext context;

    private Parser parser;

    private SymbolEnter symbolEnter;

    private PackageRepository programRepo;

    private Map<PackageID, BPackageSymbol> packages;

    public static PackageLoader getInstance(CompilerContext context) {
        PackageLoader loader = context.get(PACKAGE_LOADER_KEY);
        if (loader == null) {
            loader = new PackageLoader(context);
        }

        return loader;
    }

    public PackageLoader(CompilerContext context) {
        this.context = context;
        this.context.put(PACKAGE_LOADER_KEY, this);

        this.parser = Parser.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.packages = new HashMap<>();

        Path sourceRoot = Paths.get("/home/laf/Desktop/test");
        this.programRepo = loadFSProgramRepository(sourceRoot);
    }

    public BPackageSymbol loadEntryPackage(String sourcePkg) {
        // TODO Implement the support for loading a source package
        BLangIdentifier version = new BLangIdentifier();
        version.setValue("0.0.0");
        PackageID pkgId = new PackageID(new ArrayList<>(), version);
        PackageEntity pkgEntity = this.programRepo.loadPackage(pkgId, sourcePkg);
        log("* Package Entity: " + pkgEntity);

        BPackageSymbol pSymbol;
        if (pkgEntity.getKind() == PackageEntity.Kind.SOURCE) {
            BLangPackage pkgNode = this.sourceCompile((PackageSource) pkgEntity);

            pSymbol = symbolEnter.definePackage(pkgNode);
            packages.put(pkgId, pSymbol);
        } else {
            // This is a compiled package.
            pSymbol = null;
        }

        return pSymbol;
    }

    private BLangPackage sourceCompile(PackageSource pkgSource) {
        log("* Package Source: " + pkgSource);
        BLangPackage pkgNode = this.parser.parse(pkgSource);
        log("* Package Node: " + pkgNode);
        log("* Compilation Units:- \n" + pkgNode.getCompilationUnits());

        return pkgNode;
    }

    private void log(Object obj) {
        PrintStream printer = System.out;
        printer.println(obj);
    }

    public PackageRepository loadFSProgramRepository(Path basePath) {
        return new CompositePackageRepository(this.loadSystemRepository(), this.loadUserRepository(),
                new FSPackageRepository(basePath));
    }

    private PackageRepository loadSystemRepository() {
        return null;
    }

    private PackageRepository loadExtensionRepository() {
        return null;
    }

    private PackageRepository loadUserRepository() {
        this.loadExtensionRepository();
        return null;
    }
}

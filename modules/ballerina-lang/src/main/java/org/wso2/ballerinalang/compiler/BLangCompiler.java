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
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.repository.CompositePackageRepository;
import org.ballerinalang.repository.PackageEntity;
import org.ballerinalang.repository.PackageEntity.Kind;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.repository.PackageSource;
import org.ballerinalang.repository.fs.FSPackageRepository;
import org.wso2.ballerinalang.compiler.parser.BLangParser;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * @since 0.94
 */
public class BLangCompiler {
    
    private static final CompilerContext.Key<BLangCompiler> bLangCompilerKey = new CompilerContext.Key<>();

    private CompilerContext context;
    
    private BLangParser parser;

    private PackageRepository programRepo;
    private SymbolEnter symbolEnter;

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

        this.parser = BLangParser.getInstance(context);
        
        Path basePath = Paths.get("/Users/sameera/rewrite-compiler/bal");
        this.programRepo = this.loadFSProgramRepository(basePath);
        parser = BLangParser.getInstance(context);
        symbolEnter = SymbolEnter.getInstance(context);
    }


    private PackageRepository loadFSProgramRepository(Path basePath) {
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
    
    public void compile() {
        // TODO Parse entry package
        // TODO Define all the symbols in the entry package
        
        BLangIdentifier version = new BLangIdentifier();
        version.setValue("1.0.0");
        PackageID pkgId = new PackageID(new ArrayList<>(), version);
        PackageEntity pkgEntity = this.programRepo.loadPackage(pkgId, "foo.bal");
        log("* Package Entity: " + pkgEntity);
        if (pkgEntity.getKind() == Kind.SOURCE) {
            this.sourceCompile((PackageSource) pkgEntity);
        }
    }
    
    private void sourceCompile(PackageSource pkgSource) {
        log("* Package Source: " + pkgSource);
        PackageNode pkgNode = parser.parse(pkgSource);
        log("* Package Node: " + pkgNode);
    }

    // TODO Define Scopes and Symbols
    // TODO Then Enter symbols to scopes
    // TODO During the above process load imported packages.

    private void log(Object obj) {
        PrintStream printer = System.out;
        printer.println(obj);
    }

}

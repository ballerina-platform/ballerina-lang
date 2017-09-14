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
import org.ballerinalang.repository.AggregatedPackageRepository;
import org.ballerinalang.repository.CompositePackageRepository;
import org.ballerinalang.repository.PackageEntity;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.repository.PackageSource;
import org.ballerinalang.repository.fs.LocalFSPackageRepository;
import org.ballerinalang.spi.SystemPackageRepositoryProvider;
import org.wso2.ballerinalang.compiler.parser.Parser;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import static org.ballerinalang.compiler.CompilerOptionName.SOURCE_ROOT;

/**
 * This class contains methods to load a given package symbol.
 * It knows how to load source package as well as a package from a compiled version (.balo).
 *
 * @since 0.94
 */
public class PackageLoader {

    private static final CompilerContext.Key<PackageLoader> PACKAGE_LOADER_KEY =
            new CompilerContext.Key<>();

    private CompilerOptions options;
    private Parser parser;
    private SymbolEnter symbolEnter;

    private Map<PackageID, BPackageSymbol> packages;
    private PackageRepository packageRepo;

    public static PackageLoader getInstance(CompilerContext context) {
        PackageLoader loader = context.get(PACKAGE_LOADER_KEY);
        if (loader == null) {
            loader = new PackageLoader(context);
        }

        return loader;
    }

    public PackageLoader(CompilerContext context) {
        context.put(PACKAGE_LOADER_KEY, this);

        this.options = CompilerOptions.getInstance(context);
        this.parser = Parser.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);

        this.packages = new HashMap<>();
        loadPackageRepository(context);
    }

    public BLangPackage loadEntryPackage(String sourcePkg) {
        // TODO Implement the support for loading a source package
        PackageID pkgId = new PackageID(new Name(""), new Name("0.0.0"));
        PackageEntity pkgEntity = this.packageRepo.loadPackage(pkgId, sourcePkg);
        log("* Package Entity: " + pkgEntity);

        BLangPackage pkgNode;
        if (pkgEntity.getKind() == PackageEntity.Kind.SOURCE) {
            pkgNode = this.sourceCompile((PackageSource) pkgEntity);

            BPackageSymbol pSymbol = symbolEnter.definePackage(pkgNode);
            pkgNode.symbol = pSymbol;
            packages.put(pkgId, pSymbol);
        } else {
            // This is a compiled package.
            // TODO Throw an error. Entry package cannot be a compiled package
            throw new RuntimeException("TODO Entry package cannot be a compiled package");
        }

        return pkgNode;
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

    private void loadPackageRepository(CompilerContext context) {
        // Initialize program dir repository a.k.a entry package repository
        PackageRepository programRepo = context.get(PackageRepository.class);
        if (programRepo == null) {
            // create the default program repo
            String sourceRoot = options.get(SOURCE_ROOT);
            programRepo = new LocalFSPackageRepository(sourceRoot);
        }

        this.packageRepo = new CompositePackageRepository(
                this.loadSystemRepository(),
                this.loadUserRepository(),
                programRepo);
    }

    private PackageRepository loadSystemRepository() {
        ServiceLoader<SystemPackageRepositoryProvider> loader = ServiceLoader.load(
                SystemPackageRepositoryProvider.class);
        AggregatedPackageRepository repo = new AggregatedPackageRepository();
        loader.forEach(e -> repo.addRepository(e.loadRepository()));
        log("* System Repo: " + repo.getRepositories());
        return repo;
    }

    private PackageRepository loadExtensionRepository() {
        return null;
    }

    private PackageRepository loadUserRepository() {
        this.loadExtensionRepository();
        return null;
    }

    public static Path validateAndResolveSourcePath(Path programDirPath, Path sourcePath) {
        if (sourcePath == null) {
            throw new IllegalArgumentException("source package/file cannot be null");
        }

        try {
            Path realSourcePath = programDirPath.resolve(sourcePath).toRealPath();

            if (Files.isDirectory(realSourcePath, LinkOption.NOFOLLOW_LINKS)) {
                return realSourcePath;
            }

            if (!realSourcePath.toString().endsWith(PackageEntity.Kind.SOURCE.getExtension())) {
                throw new IllegalArgumentException("invalid file: " + sourcePath);
            }

            return realSourcePath;
        } catch (NoSuchFileException x) {
            throw new IllegalArgumentException("no such file or directory: " + sourcePath);
        } catch (IOException e) {
            throw new RuntimeException("error reading from file: " + sourcePath +
                    " reason: " + e.getMessage(), e);
        }
    }
}

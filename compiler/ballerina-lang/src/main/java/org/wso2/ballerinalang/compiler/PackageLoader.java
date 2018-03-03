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

import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.repository.AggregatedPackageRepository;
import org.ballerinalang.repository.CompositePackageRepository;
import org.ballerinalang.repository.PackageEntity;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.repository.PackageSource;
import org.ballerinalang.repository.fs.LocalFSPackageRepository;
import org.ballerinalang.spi.ExtensionPackageRepositoryProvider;
import org.ballerinalang.spi.SystemPackageRepositoryProvider;
import org.ballerinalang.spi.UserRepositoryProvider;
import org.wso2.ballerinalang.compiler.parser.Parser;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

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
    private Names names;

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
        this.names = Names.getInstance(context);

        this.packages = new HashMap<>();
        loadPackageRepository(context);
    }

    public BLangPackage loadEntryPackage(String source) {
        if (source == null || source.isEmpty()) {
            throw new IllegalArgumentException("source package/file cannot be null");
        }

        PackageEntity pkgEntity;
        PackageID pkgId = PackageID.DEFAULT;
        BLangPackage bLangPackage;
        if (source.endsWith(PackageEntity.Kind.SOURCE.getExtension())) {
            pkgEntity = this.packageRepo.loadPackage(pkgId, source);
            bLangPackage = loadPackage(pkgEntity);
            if (bLangPackage == null) {
                throw new IllegalArgumentException("cannot resolve file '" + source + "'");
            }
        } else {
            pkgId = getPackageID(source);
            pkgEntity = getPackageEntity(pkgId);
            bLangPackage = loadPackage(pkgEntity);
            if (bLangPackage == null) {
                throw new IllegalArgumentException("cannot resolve package '" + source + "'");
            }
        }

        // Add runtime and transaction packages.
        addImportPkg(bLangPackage, Names.RUNTIME_PACKAGE.value);

        // Define Package
        definePackage(pkgId, bLangPackage);
        return bLangPackage;
    }

    public BLangPackage loadPackage(String sourcePkg) {
        if (sourcePkg == null || sourcePkg.isEmpty()) {
            throw new IllegalArgumentException("source package/file cannot be null");
        }

        PackageID pkgId = getPackageID(sourcePkg);
        return loadPackage(getPackageEntity(pkgId));
    }

    public BLangPackage loadAndDefinePackage(String sourcePkg) {
        PackageID pkgId = getPackageID(sourcePkg);
        return loadAndDefinePackage(pkgId);
    }

    public BLangPackage loadAndDefinePackage(PackageID pkgId) {
        BLangPackage bLangPackage = loadPackage(pkgId);
        if (bLangPackage == null) {
            return null;
        }

        definePackage(pkgId, bLangPackage);
        return bLangPackage;
    }

    public BLangPackage loadAndDefinePackage(BLangIdentifier orgName, List<BLangIdentifier> pkgNameComps,
                                             BLangIdentifier version) {
        List<Name> nameComps = pkgNameComps.stream()
                .map(identifier -> names.fromIdNode(identifier))
                .collect(Collectors.toList());
        PackageID pkgID = new PackageID(names.fromIdNode(orgName), nameComps, names.fromIdNode(version));
        return loadAndDefinePackage(pkgID);
    }


    public BPackageSymbol getPackageSymbol(PackageID pkgId) {
        return packages.get(pkgId);
    }

    /**
     * List all the packages of packageRepo.
     *
     * @param maxDepth the maximum depth of directories to search in
     * @return a set of PackageIDs
     */
    public Set<PackageID> listPackages(int maxDepth) {
        return this.packageRepo.listPackages(maxDepth);
    }


    // Private methods

    private BLangPackage sourceCompile(PackageSource pkgSource) {
        return this.parser.parse(pkgSource);
    }

    private void addImportPkg(BLangPackage bLangPackage, String sourcePkgName) {
        List<Name> nameComps = getPackageNameComps(sourcePkgName);
        List<BLangIdentifier> pkgNameComps = new ArrayList<>();
        nameComps.forEach(comp -> {
            IdentifierNode node = TreeBuilder.createIdentifierNode();
            node.setValue(comp.value);
            pkgNameComps.add((BLangIdentifier) node);
        });

        BLangIdentifier orgNameNode = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        orgNameNode.setValue(Names.ANON_ORG.value);
        BLangIdentifier versionNode = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        versionNode.setValue(Names.DEFAULT_VERSION.value);
        BLangImportPackage importDcl = (BLangImportPackage) TreeBuilder.createImportPackageNode();
        importDcl.pos = bLangPackage.pos;
        importDcl.pkgNameComps = pkgNameComps;
        importDcl.orgName = orgNameNode;
        importDcl.version = versionNode;
        BLangIdentifier alias = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        alias.setValue(names.merge(Names.DOT, nameComps.get(nameComps.size() - 1)).value);
        importDcl.alias = alias;
        bLangPackage.imports.add(importDcl);
    }

    private PackageEntity getPackageEntity(PackageID pkgId) {
        return this.packageRepo.loadPackage(pkgId);
    }

    private PackageID getPackageID(String sourcePkg) {
        // split from '.', '\' and '/'
        List<Name> pkgNameComps = getPackageNameComps(sourcePkg);
        return new PackageID(Names.ANON_ORG, pkgNameComps, Names.DEFAULT_VERSION);
    }

    private List<Name> getPackageNameComps(String sourcePkg) {
        String[] pkgParts = sourcePkg.split("\\.|\\\\|\\/");
        return Arrays.stream(pkgParts)
                .map(part -> names.fromString(part))
                .collect(Collectors.toList());
    }

    private void definePackage(PackageID pkgId, BLangPackage bLangPackage) {
        BPackageSymbol pSymbol = symbolEnter.definePackage(bLangPackage, pkgId);
        bLangPackage.symbol = pSymbol;
        packages.put(pkgId, pSymbol);
    }

    private BLangPackage loadPackage(PackageEntity pkgEntity) {
        if (pkgEntity == null) {
            return null;
        }
        return sourceCompile((PackageSource) pkgEntity);
    }

    private BLangPackage loadPackage(PackageID pkgId) {
        return loadPackage(getPackageEntity(pkgId));
    }

    private void loadPackageRepository(CompilerContext context) {
        // Initialize program dir repository a.k.a entry package repository
        PackageRepository programRepo = context.get(PackageRepository.class);
        if (programRepo == null) {
            // create the default program repo
            String sourceRoot = options.get(SOURCE_ROOT);
            // TODO: replace by the org read form TOML.
            programRepo = new LocalFSPackageRepository(sourceRoot, Names.ANON_ORG.getValue());
        }

        PackageRepository systemRepo = this.loadSystemRepository();
        this.packageRepo = new CompositePackageRepository(systemRepo, this.loadUserRepository(systemRepo), programRepo);
    }

    private PackageRepository loadSystemRepository() {
        ServiceLoader<SystemPackageRepositoryProvider> loader = ServiceLoader.load(
                SystemPackageRepositoryProvider.class);
        AggregatedPackageRepository repo = new AggregatedPackageRepository();
        loader.forEach(e -> repo.addRepository(e.loadRepository()));
        return repo;
    }

    private PackageRepository loadExtensionRepository() {
        ServiceLoader<ExtensionPackageRepositoryProvider> loader = ServiceLoader.load(
                ExtensionPackageRepositoryProvider.class);
        AggregatedPackageRepository repo = new AggregatedPackageRepository();
        loader.forEach(e -> repo.addRepository(e.loadRepository()));
        return repo;
    }

    private PackageRepository loadUserRepository(PackageRepository systemRepo) {
        ServiceLoader<UserRepositoryProvider> loader = ServiceLoader.load(UserRepositoryProvider.class);
        AggregatedPackageRepository userRepo = new AggregatedPackageRepository();
        loader.forEach(e -> userRepo.addRepository(e.loadRepository()));

        return new CompositePackageRepository(systemRepo, this.loadExtensionRepository(), userRepo);
    }
}

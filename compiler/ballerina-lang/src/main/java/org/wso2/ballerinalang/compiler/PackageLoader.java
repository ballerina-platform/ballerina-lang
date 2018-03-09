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
import org.ballerinalang.repository.PackageEntity;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.repository.PackageSource;
import org.ballerinalang.repository.PackageSourceEntry;
import org.ballerinalang.spi.SystemPackageRepositoryProvider;
import org.wso2.ballerinalang.compiler.packaging.RepoDAG;
import org.wso2.ballerinalang.compiler.packaging.Resolution;
import org.wso2.ballerinalang.compiler.packaging.repo.ProjectSourceRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.Repo;
import org.wso2.ballerinalang.compiler.parser.Parser;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;

/**
 * This class contains methods to load a given package symbol.
 * It knows how to load source package as well as a package from a compiled version (.balo).
 *
 * @since 0.94
 */
public class PackageLoader {

    private static final CompilerContext.Key<PackageLoader> PACKAGE_LOADER_KEY =
            new CompilerContext.Key<>();
    private final RepoDAG repos;

    private CompilerOptions options;
    private Parser parser;
    private SourceDirectory sourceDirectory;
    private PackageCache packageCache;
    private SymbolEnter symbolEnter;
    private Names names;

//    private PackageRepository packageRepo;

    public static PackageLoader getInstance(CompilerContext context) {
        PackageLoader loader = context.get(PACKAGE_LOADER_KEY);
        if (loader == null) {
            loader = new PackageLoader(context);
        }

        return loader;
    }

    private PackageLoader(CompilerContext context) {
        context.put(PACKAGE_LOADER_KEY, this);

        this.sourceDirectory = context.get(SourceDirectory.class);
        if (this.sourceDirectory == null) {
            throw new IllegalArgumentException("source directory has not been initialized");
        }

        this.options = CompilerOptions.getInstance(context);
        this.parser = Parser.getInstance(context);
        this.packageCache = PackageCache.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.names = Names.getInstance(context);
        Path projectDir = Paths.get(options.get(PROJECT_DIR));

        ServiceLoader<SystemPackageRepositoryProvider> loader =
                ServiceLoader.load(SystemPackageRepositoryProvider.class);

        RepoDAG system = null;
        for (SystemPackageRepositoryProvider systemPackageRepositoryProvider : loader) {
            Repo repo = systemPackageRepositoryProvider.loadRepository();
            if (repo != null) {
                system = new RepoDAG(repo, system);
            }
        }
        ProjectSourceRepo project = new ProjectSourceRepo(projectDir);
        this.repos = new RepoDAG(project, system);
//        loadPackageRepository(context);
    }

    public BLangPackage loadPackage(String source, PackageRepository packageRepo) {
        if (source == null || source.isEmpty()) {
            throw new IllegalArgumentException("source package/file cannot be null");
        }

        // TODO Clean up this code.
        PackageEntity pkgEntity;
        PackageID pkgId = PackageID.DEFAULT;
        BLangPackage bLangPackage;
        if (source.endsWith(PackageEntity.Kind.SOURCE.getExtension())) {
            bLangPackage = packageCache.get(pkgId);
            if (bLangPackage != null) {
                return bLangPackage;
            }

            pkgEntity = loadPackageEntity(pkgId, source);
            bLangPackage = loadPackageFromEntity(pkgId, pkgEntity); //loadPackage(pkgId, pkgEntity);
            if (bLangPackage == null) {
                throw new IllegalArgumentException("cannot resolve file '" + source + "'");
            }
        } else {
            pkgId = getPackageID(source);
            bLangPackage = packageCache.get(pkgId);
            if (bLangPackage != null) {
                return bLangPackage;
            }

            pkgEntity = getPackageEntity(pkgId);
            bLangPackage = loadPackageFromEntity(pkgId, pkgEntity); // loadPackage(pkgId, pkgEntity);
            if (bLangPackage == null) {
                throw new IllegalArgumentException("cannot resolve package '" + source + "'");
            }
        }

        // Add runtime.
        addImportPkg(bLangPackage, Names.RUNTIME_PACKAGE.value);
        return bLangPackage;
    }

    private PackageEntity loadPackageEntity(PackageID pkgId, String source) {
        Resolution resolution = repos.resolve(pkgId);
        if (resolution == Resolution.EMPTY) {
            throw new RuntimeException("Package not found " + pkgId);
        }
        return pathToEntity(pkgId, resolution);
    }

    private PackageEntity pathToEntity(PackageID pkgId, Resolution resolution) {
        return new PackageSource() {
            @Override
            public Kind getKind() {
                return null;
            }

            @Override
            public String getName() {
                return pkgId.getName().value;
            }

            @Override
            public PackageRepository getPackageRepository() {
                return null;
            }

            @Override
            public PackageID getPackageId() {
                return pkgId;
            }

            @Override
            public List<String> getEntryNames() {
                return null;
            }

            @Override
            public RepoDAG getRepoDag() {
                return resolution.resolvedBy;
            }

            @Override
            public PackageSourceEntry getPackageSourceEntry(String name) {
                return null;
            }

            @Override
            public List<PackageSourceEntry> getPackageSourceEntries() {
                return resolution.paths.stream().map(p -> new PackageSourceEntry() {
                    @Override
                    public PackageID getPackageID() {
                        return pkgId;
                    }

                    @Override
                    public String getEntryName() {
                        return p.getName(p.getNameCount() - 1).toString();
                    }

                    @Override
                    public byte[] getCode() {
                        try {
                            return Files.readAllBytes(p);
                        } catch (IOException e) {
                            return new byte[]{};
                        }
                    }
                }).collect(Collectors.toList());
            }
        };
    }

    public BLangPackage loadPackage(PackageID pkgId, PackageRepository packageRepo) {
        BLangPackage bLangPackage = packageCache.get(pkgId);
        if (bLangPackage != null) {
            return bLangPackage;
        }

        return loadPackageFromEntity(pkgId, getPackageEntity(pkgId)); // loadPackage(pkgId, getPackageEntity(pkgId));
    }

    public BLangPackage loadPackage(PackageID pkgId) {
        BLangPackage packageNode = loadPackage(pkgId, null);
        addImportPkg(packageNode, Names.RUNTIME_PACKAGE.value);
        return packageNode;
    }

    public BLangPackage loadEntryPackage(String source) {
        if (source == null || source.isEmpty()) {
            throw new IllegalArgumentException("source package/file cannot be null");
        }

        PackageEntity pkgEntity;
        PackageID pkgId = PackageID.DEFAULT;
        BLangPackage bLangPackage;
        if (source.endsWith(PackageEntity.Kind.SOURCE.getExtension())) {
            pkgEntity = loadPackageEntity(pkgId, source);
            bLangPackage = loadPackageFromEntity(pkgId, pkgEntity); // loadPackage(pkgId, pkgEntity);
            if (bLangPackage == null) {
                throw new IllegalArgumentException("cannot resolve file '" + source + "'");
            }
        } else {
            pkgId = getPackageID(source);
            pkgEntity = getPackageEntity(pkgId);
            bLangPackage = loadPackageFromEntity(pkgId, pkgEntity);
            if (bLangPackage == null) {
                throw new IllegalArgumentException("cannot resolve package '" + source + "'");
            }
        }

        // Add runtime.
        addImportPkg(bLangPackage, Names.RUNTIME_PACKAGE.value);

        // Define Package
        this.symbolEnter.definePackage(bLangPackage, pkgId);
        return bLangPackage;
    }

    public BLangPackage loadAndDefinePackage(String sourcePkg) {
        // TODO This is used only to load the builtin package.
        PackageID pkgId = getPackageID(sourcePkg);
        return loadAndDefinePackage(pkgId);
    }

    public BLangPackage loadAndDefinePackage(BLangIdentifier orgName, List<BLangIdentifier> pkgNameComps,
                                             BLangIdentifier version) {
        // TODO This method is only used by the composer. Can we refactor the composer code?
        List<Name> nameComps = pkgNameComps.stream()
                                           .map(identifier -> names.fromIdNode(identifier))
                                           .collect(Collectors.toList());
        PackageID pkgID = new PackageID(names.fromIdNode(orgName), nameComps, names.fromIdNode(version));
        return loadAndDefinePackage(pkgID);
    }

    private BLangPackage loadAndDefinePackage(PackageID pkgId) {
        BLangPackage bLangPackage = loadPackage(pkgId, null);
        if (bLangPackage == null) {
            return null;
        }

        this.symbolEnter.definePackage(bLangPackage, pkgId);
        return bLangPackage;
    }

    /**
     * List all the packages of packageRepo.
     *
     * @param maxDepth the maximum depth of directories to search in
     * @return a set of PackageIDs
     */
    public Set<PackageID> listPackages(int maxDepth) {
        return Collections.emptySet();
//        return this.packageRepo.listPackages(maxDepth);
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
        if (pkgId.isUnnamed) {
            return loadPackageEntity(pkgId, pkgId.sourceFileName.value);
        }
        return loadPackageEntity(pkgId);
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

    private BLangPackage loadPackageFromEntity(PackageID pkgId, PackageEntity pkgEntity) {
        if (pkgEntity == null) {
            return null;
        }

        BLangPackage bLangPackage = sourceCompile((PackageSource) pkgEntity);
        this.packageCache.put(pkgId, bLangPackage);
        return bLangPackage;
    }

    private void loadPackageRepository(CompilerContext context) {
        // Initialize program dir repository a.k.a entry package repository
//        PackageRepository programRepo = context.get(PackageRepository.class);
//        if (programRepo == null) {
//            // create the default program repo
//            String sourceRoot = options.get(PROJECT_DIR);
//            // TODO: replace by the org read form TOML.
//            programRepo = new LocalFSPackageRepository(sourceRoot, Names.ANON_ORG.getValue());
//        }

//        this.packageRepo =
// new CompositePackageRepository(systemRepo, this.loadUserRepository(systemRepo), programRepo);
    }

//    private PackageRepository loadSystemRepository() {
//        ServiceLoader<SystemPackageRepositoryProvider> loader = ServiceLoader.load(
//                SystemPackageRepositoryProvider.class);
//        AggregatedPackageRepository repo = new AggregatedPackageRepository();
//        loader.forEach(e -> repo.addRepository(e.loadRepository()));
//        return repo;
//    }
//
//    private PackageRepository loadExtensionRepository() {
//        ServiceLoader<ExtensionPackageRepositoryProvider> loader = ServiceLoader.load(
//                ExtensionPackageRepositoryProvider.class);
//        AggregatedPackageRepository repo = new AggregatedPackageRepository();
//        loader.forEach(e -> repo.addRepository(e.loadRepository()));
//        return repo;
//    }
//
//    private PackageRepository loadUserRepository(PackageRepository systemRepo) {
//        ServiceLoader<UserRepositoryProvider> loader = ServiceLoader.load(UserRepositoryProvider.class);
//        AggregatedPackageRepository userRepo = new AggregatedPackageRepository();
//        loader.forEach(e -> userRepo.addRepository(e.loadRepository()));
//
//        return new CompositePackageRepository(systemRepo, this.loadExtensionRepository(), userRepo);
//    }
}

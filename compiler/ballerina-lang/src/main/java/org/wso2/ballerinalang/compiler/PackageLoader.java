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
import org.ballerinalang.spi.SystemPackageRepositoryProvider;
import org.wso2.ballerinalang.compiler.packaging.PathListPackageSource;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchy;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchyBuilder;
import org.wso2.ballerinalang.compiler.packaging.Resolution;
import org.wso2.ballerinalang.compiler.packaging.repo.CacheRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.ObjRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.ProgramingSourceRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.ProjectSourceRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.Repo;
import org.wso2.ballerinalang.compiler.packaging.repo.ZipRepo;
import org.wso2.ballerinalang.compiler.parser.Parser;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.wso2.ballerinalang.compiler.packaging.RepoHierarchyBuilder.node;

/**
 * This class contains methods to load a given package symbol.
 * It knows how to load source package as well as a package from a compiled version (.balo).
 *
 * @since 0.94
 */
public class PackageLoader {

    private static final CompilerContext.Key<PackageLoader> PACKAGE_LOADER_KEY =
            new CompilerContext.Key<>();
    private final RepoHierarchy repos;

    private CompilerOptions options;
    private Parser parser;
    private SourceDirectory sourceDirectory;
    private PackageCache packageCache;
    private SymbolEnter symbolEnter;
    private Names names;

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
        this.repos = genRepoHierarchy(Paths.get(options.get(PROJECT_DIR)));
    }

    private RepoHierarchy genRepoHierarchy(Path sourceRoot) {
        Path balHomeDir = Paths.get("~/.ballerina_home");
        Path projectHiddenDir = sourceRoot.resolve(".ballerina");
        RepoHierarchyBuilder.RepoNode[] systemArr = loadSystemRepos();

        Repo homeCacheRepo = new CacheRepo(balHomeDir);
        Repo homeRepo = new ObjRepo(balHomeDir);
        Repo projectCacheRepo = new CacheRepo(projectHiddenDir);
        Repo projectRepo = new ZipRepo(projectHiddenDir.toUri()); //new ObjRepo(projectHiddenDir);
        Repo projectSource = new ProjectSourceRepo(sourceRoot);
        Repo programingSource = new ProgramingSourceRepo(sourceRoot);

        RepoHierarchyBuilder.RepoNode homeCacheNode;
        homeCacheNode = node(homeCacheRepo, systemArr);
        return RepoHierarchyBuilder.build(node(programingSource,
                                               node(projectSource,
                                                    node(projectRepo,
                                                         node(projectCacheRepo, homeCacheNode),
                                                         node(homeRepo, homeCacheNode)))));
    }

    private RepoHierarchyBuilder.RepoNode[] loadSystemRepos() {
        List<RepoHierarchyBuilder.RepoNode> systemList;
        ServiceLoader<SystemPackageRepositoryProvider> loader
                = ServiceLoader.load(SystemPackageRepositoryProvider.class);
        systemList = StreamSupport.stream(loader.spliterator(), false)
                                  .map(SystemPackageRepositoryProvider::loadRepository)
                                  .filter(Objects::nonNull)
                                  .map(r -> node(r))
                                  .collect(Collectors.toList());
        return systemList.toArray(new RepoHierarchyBuilder.RepoNode[systemList.size()]);
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

            pkgEntity = loadPackageEntity(pkgId);
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

    private PackageEntity loadPackageEntity(PackageID pkgId, String value) {
        return loadPackageEntity(pkgId);
    }

    private PackageEntity loadPackageEntity(PackageID pkgId) {
        Resolution resolution = repos.resolve(pkgId);
        if (resolution == Resolution.NOT_FOUND) {
            throw new RuntimeException("Package not found " + pkgId);
        }
        return pathToEntity(pkgId, resolution);
    }

    private PackageEntity pathToEntity(PackageID pkgId, Resolution resolution) {
        return new PathListPackageSource(pkgId, resolution.paths, resolution.resolvedBy);
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
            pkgEntity = loadPackageEntity(pkgId);
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

}

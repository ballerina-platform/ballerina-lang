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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.TreeBuilder;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.repository.PackageEntity;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.repository.PackageSource;
import org.ballerinalang.spi.SystemPackageRepositoryProvider;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.wso2.ballerinalang.compiler.packaging.GenericPackageSource;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchy;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchyBuilder;
import org.wso2.ballerinalang.compiler.packaging.Resolution;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.repo.CacheRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.ProgramingSourceRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.ProjectSourceRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.RemoteRepo;
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
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.util.HomeRepoUtils;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
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
    private final boolean offline;
    private final Manifest manifest;

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
        this.offline = Boolean.parseBoolean(options.get(OFFLINE));
        this.repos = genRepoHierarchy(Paths.get(options.get(PROJECT_DIR)));
        this.manifest = ManifestProcessor.parseTomlContentAsStream(sourceDirectory.getManifestContent());
    }

    private RepoHierarchy genRepoHierarchy(Path sourceRoot) {
        Path balHomeDir = HomeRepoUtils.createAndGetHomeReposPath();
        Path projectHiddenDir = sourceRoot.resolve(".ballerina");
        RepoHierarchyBuilder.RepoNode[] systemArr = loadSystemRepos();
        Converter<Path> converter = sourceDirectory.getConverter();

        Repo remote = new RemoteRepo(URI.create("https://api.central.ballerina.io/packages/"));
        Repo homeCacheRepo = new CacheRepo(balHomeDir);
        Repo homeRepo = new ZipRepo(balHomeDir);
        Repo projectCacheRepo = new CacheRepo(projectHiddenDir);
        Repo projectRepo = new ZipRepo(projectHiddenDir);


        RepoHierarchyBuilder.RepoNode homeCacheNode;

        if (offline) {
            homeCacheNode = node(homeCacheRepo, systemArr);
        } else {
            homeCacheNode = node(homeCacheRepo, node(remote, systemArr));
        }
        RepoHierarchyBuilder.RepoNode nonLocalRepos = node(projectRepo,
                                                           node(projectCacheRepo, homeCacheNode),
                                                           node(homeRepo, homeCacheNode));
        RepoHierarchyBuilder.RepoNode fullRepoGraph;
        if (converter != null) {
            Repo programingSource = new ProgramingSourceRepo(converter);
            Repo projectSource = new ProjectSourceRepo(converter);
            fullRepoGraph = node(programingSource,
                                 node(projectSource,
                                      nonLocalRepos));
        } else {
            fullRepoGraph = nonLocalRepos;
        }
        return RepoHierarchyBuilder.build(fullRepoGraph);

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

    private PackageEntity loadPackageEntity(PackageID pkgId) {
        updateVersionFromToml(pkgId);
        Resolution resolution = repos.resolve(pkgId);
        if (resolution == Resolution.NOT_FOUND) {
            return null;
        }
        return new GenericPackageSource(pkgId, resolution.sources, resolution.resolvedBy);
    }

    private void updateVersionFromToml(PackageID pkgId) {
        String orgName = pkgId.orgName.value;
        String pkgName = pkgId.name.value;
        String pkgAlias = orgName + "/" + pkgName;

        // TODO: make getDependencies return a map
        Optional<Dependency> dependency = manifest.getDependencies()
                                                  .stream()
                                                  .filter(d -> d.getPackageName().equals(pkgAlias))
                                                  .findFirst();
        if (dependency.isPresent()) {
            if (pkgId.version.value.isEmpty()) {
                pkgId.version = new Name(dependency.get().getVersion());
            } else {
                throw new BLangCompilerException("dependency version in Ballerina.toml mismatches" +
                                                 " with the version in the source for package " + pkgAlias);
            }
        }
    }

    public BLangPackage loadPackage(PackageID pkgId) {
        BLangPackage packageNode = loadPackage(pkgId, null);
        addImportPkg(packageNode, Names.BUILTIN_ORG.value, Names.RUNTIME_PACKAGE.value, Names.EMPTY.value);
        return packageNode;
    }

    public BLangPackage loadPackage(PackageID pkgId, PackageRepository packageRepo) {
        BLangPackage bLangPackage = packageCache.get(pkgId);
        if (bLangPackage != null) {
            return bLangPackage;
        }

        BLangPackage packageNode = loadPackageFromEntity(pkgId, loadPackageEntity(pkgId));
        if (packageNode == null) {
            throw ProjectDirs.getPackageNotFoundError(pkgId);
        }
        return packageNode;
    }

    public BLangPackage loadAndDefinePackage(String orgName, String pkgName) {
        // TODO This is used only to load the builtin package.
        PackageID pkgId = getPackageID(orgName, pkgName);
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

    public BLangPackage loadAndDefinePackage(PackageID pkgId) {
        BLangPackage bLangPackage = loadPackage(pkgId, null);
        if (bLangPackage == null) {
            return null;
        }

        this.symbolEnter.definePackage(bLangPackage);
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

    private void addImportPkg(BLangPackage bLangPackage, String orgName, String sourcePkgName, String version) {
        List<Name> nameComps = getPackageNameComps(sourcePkgName);
        List<BLangIdentifier> pkgNameComps = new ArrayList<>();
        nameComps.forEach(comp -> {
            IdentifierNode node = TreeBuilder.createIdentifierNode();
            node.setValue(comp.value);
            pkgNameComps.add((BLangIdentifier) node);
        });

        BLangIdentifier orgNameNode = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        orgNameNode.setValue(orgName);
        BLangIdentifier versionNode = (BLangIdentifier) TreeBuilder.createIdentifierNode();
        versionNode.setValue(version);
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

    private PackageID getPackageID(String org, String sourcePkg) {
        // split from '.', '\' and '/'
        List<Name> pkgNameComps = getPackageNameComps(sourcePkg);
        Name orgName = new Name(org);
        return new PackageID(orgName, pkgNameComps, Names.DEFAULT_VERSION);
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

    private BLangPackage sourceCompile(PackageSource pkgSource) {
        return this.parser.parse(pkgSource);
    }
}

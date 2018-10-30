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
import org.ballerinalang.repository.CompiledPackage;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.repository.CompilerOutputEntry;
import org.ballerinalang.repository.PackageBinary;
import org.ballerinalang.repository.PackageEntity;
import org.ballerinalang.repository.PackageSource;
import org.ballerinalang.spi.SystemPackageRepositoryProvider;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.LockFile;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.LockFileProcessor;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.wso2.ballerinalang.compiler.packaging.GenericPackageSource;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchy;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchyBuilder;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchyBuilder.RepoNode;
import org.wso2.ballerinalang.compiler.packaging.Resolution;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.repo.BinaryRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.CacheRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.ProgramingSourceRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.ProjectSourceRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.RemoteRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.Repo;
import org.wso2.ballerinalang.compiler.packaging.repo.ZipRepo;
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
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLog;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;
import static org.wso2.ballerinalang.compiler.packaging.Patten.path;
import static org.wso2.ballerinalang.compiler.packaging.RepoHierarchyBuilder.node;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.MODULE_MD_FILE_NAME;

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
    private final boolean testEnabled;
    private final boolean lockEnabled;
    private final Manifest manifest;
    private final LockFile lockFile;

    private final CompilerOptions options;
    private final Parser parser;
    private final SourceDirectory sourceDirectory;
    private final PackageCache packageCache;
    private final SymbolEnter symbolEnter;
    private final CompiledPackageSymbolEnter compiledPkgSymbolEnter;
    private final Names names;
    private final BLangDiagnosticLog dlog;
    private static final boolean shouldReadBalo = true;
    private static PrintStream outStream = System.out;

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
        this.compiledPkgSymbolEnter = CompiledPackageSymbolEnter.getInstance(context);
        this.names = Names.getInstance(context);
        this.dlog = BLangDiagnosticLog.getInstance(context);
        this.offline = Boolean.parseBoolean(options.get(OFFLINE));
        this.testEnabled = Boolean.parseBoolean(options.get(TEST_ENABLED));
        this.lockEnabled = Boolean.parseBoolean(options.get(LOCK_ENABLED));
        this.repos = genRepoHierarchy(Paths.get(options.get(PROJECT_DIR)));
        this.manifest = ManifestProcessor.getInstance(context).getManifest();
        this.lockFile = LockFileProcessor.getInstance(context, this.lockEnabled).getLockFile();
    }
    
    /**
     * Generates the repository hierarchy. Following is the hierarchy.
     * 1. Program Source
     * 2. Project Repo
     * 3.1. Project Cache
     * 3.2. Home Repo
     * 4. Home Cache
     * 5. System Repo
     * 6. Central
     * 7. System Repo
     * @param sourceRoot Project path.
     * @return Repository Hierarchy.
     */
    private RepoHierarchy genRepoHierarchy(Path sourceRoot) {
        Path balHomeDir = RepoUtils.createAndGetHomeReposPath();
        Path projectHiddenDir = sourceRoot.resolve(".ballerina");
        Converter<Path> converter = sourceDirectory.getConverter();

        Repo systemRepo = new BinaryRepo(RepoUtils.getLibDir());
        Repo remoteRepo = new RemoteRepo(URI.create(RepoUtils.getRemoteRepoURL()));
        Repo homeCacheRepo = new CacheRepo(balHomeDir, ProjectDirConstants.BALLERINA_CENTRAL_DIR_NAME);
        Repo homeRepo = shouldReadBalo ? new BinaryRepo(balHomeDir) : new ZipRepo(balHomeDir);
        Repo projectCacheRepo = new CacheRepo(projectHiddenDir, ProjectDirConstants.BALLERINA_CENTRAL_DIR_NAME);
        Repo projectRepo = shouldReadBalo ? new BinaryRepo(projectHiddenDir) : new ZipRepo(projectHiddenDir);
        Repo secondarySystemRepo = new BinaryRepo(RepoUtils.getLibDir());

        RepoNode homeCacheNode;

        if (offline) {
            homeCacheNode = node(homeCacheRepo, node(systemRepo));
        } else {
            homeCacheNode = node(homeCacheRepo, node(systemRepo, node(remoteRepo, node(secondarySystemRepo))));
        }
        RepoNode nonLocalRepos = node(projectRepo,
                                      node(projectCacheRepo, homeCacheNode),
                                      node(homeRepo, homeCacheNode));
        RepoNode fullRepoGraph;
        if (converter != null) {
            Repo programingSource = new ProgramingSourceRepo(converter);
            Repo projectSource = new ProjectSourceRepo(converter, testEnabled);
            fullRepoGraph = node(programingSource,
                                 node(projectSource,
                                      nonLocalRepos));
        } else {
            fullRepoGraph = nonLocalRepos;
        }
        return RepoHierarchyBuilder.build(fullRepoGraph);

    }

    private RepoNode[] loadSystemRepos() {
        List<RepoNode> systemList;
        ServiceLoader<SystemPackageRepositoryProvider> loader
                = ServiceLoader.load(SystemPackageRepositoryProvider.class);
        systemList = StreamSupport.stream(loader.spliterator(), false)
                                  .map(SystemPackageRepositoryProvider::loadRepository)
                                  .filter(Objects::nonNull)
                                  .map(r -> node(r))
                                  .collect(Collectors.toList());
        return systemList.toArray(new RepoNode[systemList.size()]);
    }
    
    private PackageEntity loadPackageEntity(PackageID pkgId) {
        return loadPackageEntity(pkgId, null, null);
    }
    
    private PackageEntity loadPackageEntity(PackageID pkgId, PackageID enclPackageId,
                                            RepoHierarchy encPkgRepoHierarchy) {
        updateVersionFromToml(pkgId, enclPackageId);
        Resolution resolution;
        if (null != encPkgRepoHierarchy) {
            resolution = encPkgRepoHierarchy.resolve(pkgId);
        } else {
            resolution = repos.resolve(pkgId);
        }
        
        if (resolution == Resolution.NOT_FOUND) {
            return null;
        }
        
        CompilerInput firstEntry = resolution.inputs.get(0);
        if (firstEntry.getEntryName().endsWith(PackageEntity.Kind.COMPILED.getExtension())) {
            // Binary package has only one file, so using first entry
            return new GenericPackageBinary(pkgId, firstEntry, resolution.resolvedBy);
        } else {
            return new GenericPackageSource(pkgId, resolution.inputs, resolution.resolvedBy);
        }
    }

    private void updateVersionFromToml(PackageID pkgId, PackageID enclPackageId) {
        String orgName = pkgId.orgName.value;
        String pkgName = pkgId.name.value;
        String pkgAlias = orgName + "/" + pkgName;
        if (!lockEnabled) {
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
                                                             " with the version in the source for module " + pkgAlias);
                }
            }
        } else {
            // Read from lock file
            if (enclPackageId != null) { // Not a top level package or bal
                String enclPkgAlias = enclPackageId.orgName.value + "/" + enclPackageId.name.value;

                Optional<LockFilePackage> lockFilePackage = lockFile.getPackageList()
                                                                    .stream()
                                                                    .filter(pkg -> {
                                                                        String org = pkg.getOrg();
                                                                        if (org.isEmpty()) {
                                                                            org = manifest.getName();
                                                                        }
                                                                        String alias = org + "/" + pkg.getName();
                                                                        return alias.equals(enclPkgAlias);
                                                                    })
                                                                    .findFirst();
                if (lockFilePackage.isPresent()) {
                    Optional<LockFilePackage> dependency = lockFilePackage.get().getDependencies()
                                                                          .stream()
                                                                          .filter(pkg -> {
                                                                              String alias = pkg.getOrg() + "/"
                                                                                      + pkg.getName();
                                                                              return alias.equals(pkgAlias);
                                                                          })
                                                                          .findFirst();
                    dependency.ifPresent(dependencyPkg -> pkgId.version = new Name(dependencyPkg.getVersion()));
                }
            }
        }
    }

    public BLangPackage loadEntryPackage(PackageID pkgId, PackageID enclPackageId, boolean isBuild) {
        if (isBuild) {
            outStream.println("    " + (pkgId.isUnnamed ? pkgId.sourceFileName.value : pkgId.toString()));
        }
        //even entry package may be already loaded through an import statement.
        BLangPackage bLangPackage = packageCache.get(pkgId);
        if (bLangPackage != null) {
            return bLangPackage;
        }
        PackageEntity pkgEntity = loadPackageEntity(pkgId, enclPackageId, null);
        if (pkgEntity == null) {
            // Do not throw an error here. Otherwise package build will terminate immediately if
            // there are errors in atleast one package during the build. But instead we should
            // continue compiling the other packages as well, and check for their errors.
            return null;
        }

        BLangPackage packageNode = parse(pkgId, (PackageSource) pkgEntity);
        if (packageNode.diagCollector.hasErrors()) {
            return packageNode;
        }

        define(packageNode);
        return packageNode;
    }

    private BLangPackage loadPackage(PackageID pkgId) {
        // TODO Remove this method()
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

    public BLangPackage loadAndDefinePackage(String orgName, String pkgName, String version) {
        // TODO This is used only to load the builtin package.
        PackageID pkgId = getPackageID(orgName, pkgName, version);
        return loadAndDefinePackage(pkgId);
    }

    public BLangPackage loadAndDefinePackage(PackageID pkgId) {
        // TODO this used only by the language server component and the above method.
        BLangPackage bLangPackage = loadPackage(pkgId);
        if (bLangPackage == null) {
            return null;
        }

        this.symbolEnter.definePackage(bLangPackage);
        bLangPackage.symbol.compiledPackage = createInMemoryCompiledPackage(bLangPackage);
        return bLangPackage;
    }

    public BPackageSymbol loadPackageSymbol(PackageID packageId, PackageID enclPackageId,
                                            RepoHierarchy encPkgRepoHierarchy) {
        BPackageSymbol packageSymbol = this.packageCache.getSymbol(packageId);
        if (packageSymbol != null) {
            return packageSymbol;
        }

        PackageEntity pkgEntity = loadPackageEntity(packageId, enclPackageId, encPkgRepoHierarchy);
        if (pkgEntity == null) {
            return null;
        }

        if (pkgEntity.getKind() == PackageEntity.Kind.SOURCE) {
            return parseAndDefine(packageId, (PackageSource) pkgEntity);
        } else if (pkgEntity.getKind() == PackageEntity.Kind.COMPILED) {
            return loadCompiledPackageAndDefine(packageId, (PackageBinary) pkgEntity);
        }

        return null;
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
        alias.setValue(names.merge(Names.ORG_NAME_SEPARATOR, nameComps.get(nameComps.size() - 1)).value);
        importDcl.alias = alias;
        bLangPackage.imports.add(importDcl);
    }

    private PackageID getPackageID(String org, String sourcePkg, String version) {
        // split from '.', '\' and '/'
        List<Name> pkgNameComps = getPackageNameComps(sourcePkg);
        Name orgName = new Name(org);
        return new PackageID(orgName, pkgNameComps, new Name(version));
    }

    private List<Name> getPackageNameComps(String sourcePkg) {
        String[] pkgParts = sourcePkg.split("\\.|\\\\|\\/");
        return Arrays.stream(pkgParts)
                     .map(names::fromString)
                     .collect(Collectors.toList());
    }

    private BPackageSymbol parseAndDefine(PackageID pkgId, PackageSource pkgSource) {
        // 1) Parse the source package
        BLangPackage pkgNode = parse(pkgId, pkgSource);
        return define(pkgNode);
    }

    private BPackageSymbol define(BLangPackage pkgNode) {
        // 2) Define all package-level symbols
        this.symbolEnter.definePackage(pkgNode);
        this.packageCache.putSymbol(pkgNode.packageID, pkgNode.symbol);

        // 3) Create the compiledPackage structure
        pkgNode.symbol.compiledPackage = createInMemoryCompiledPackage(pkgNode);
        return pkgNode.symbol;
    }

    private BLangPackage loadPackageFromEntity(PackageID pkgId, PackageEntity pkgEntity) {
        if (pkgEntity == null) {
            return null;
        }

        BLangPackage bLangPackage = parse(pkgId, (PackageSource) pkgEntity);
        this.packageCache.put(pkgId, bLangPackage);
        return bLangPackage;
    }

    private BLangPackage parse(PackageID pkgId, PackageSource pkgSource) {
        BLangPackage packageNode = this.parser.parse(pkgSource, this.sourceDirectory.getPath());
        packageNode.packageID = pkgId;
        // Set the same packageId to the testable node
        packageNode.getTestablePkgs().forEach(testablePkg -> testablePkg.packageID = pkgId);
        this.packageCache.put(pkgId, packageNode);
        return packageNode;
    }

    private BPackageSymbol loadCompiledPackageAndDefine(PackageID pkgId, PackageBinary pkgBinary) {
        byte[] pkgBinaryContent = pkgBinary.getCompilerInput().getCode();
        BPackageSymbol pkgSymbol = this.compiledPkgSymbolEnter.definePackage(
                pkgId, pkgBinary.getRepoHierarchy(), pkgBinaryContent);
        this.packageCache.putSymbol(pkgId, pkgSymbol);

        // TODO create CompiledPackage
        return pkgSymbol;
    }

    private CompiledPackage createInMemoryCompiledPackage(BLangPackage pkgNode) {
        PackageID packageID = pkgNode.packageID;
        InMemoryCompiledPackage compiledPackage = new InMemoryCompiledPackage(packageID);

        // Get the list of source entries.
        Path projectPath = this.sourceDirectory.getPath();
        ProjectSourceRepo projectSourceRepo = new ProjectSourceRepo(projectPath, testEnabled);
        Patten packageIDPattern = projectSourceRepo.calculate(packageID);
        if (packageIDPattern != Patten.NULL) {
            Stream<Path> srcPathStream = packageIDPattern.convert(projectSourceRepo.getConverterInstance(), packageID);
            // Filter the tests files
            compiledPackage.srcEntries = srcPathStream
                    .filter(path -> Files.exists(path, LinkOption.NOFOLLOW_LINKS))
                    .filter(path -> !ProjectDirs.isTestSource(path, projectPath, packageID.getName().getValue()))
                    .map(projectPath::relativize)
                    .map(path -> new PathBasedCompiledPackageEntry(projectPath, path, CompilerOutputEntry.Kind.SRC))
                    .collect(Collectors.toList());

            // Get the Module.md file
            Patten pkgMDPattern = packageIDPattern.sibling(path(MODULE_MD_FILE_NAME));
            pkgMDPattern.convert(projectSourceRepo.getConverterInstance(), packageID)
                    .filter(pkgMDPath -> Files.exists(pkgMDPath, LinkOption.NOFOLLOW_LINKS))
                    .map(projectPath::relativize)
                    .map(pkgMDPath -> new PathBasedCompiledPackageEntry(projectPath, pkgMDPath,
                                                                        CompilerOutputEntry.Kind.ROOT))
                    .findAny()
                    .ifPresent(pkgEntry -> compiledPackage.pkgMDEntry = pkgEntry);
        }
        return compiledPackage;
    }
}

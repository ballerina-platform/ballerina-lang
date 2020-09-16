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

import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompiledPackage;
import org.ballerinalang.repository.CompilerInput;
import org.ballerinalang.repository.CompilerOutputEntry;
import org.ballerinalang.repository.PackageBinary;
import org.ballerinalang.repository.PackageEntity;
import org.ballerinalang.repository.PackageEntity.Kind;
import org.ballerinalang.repository.PackageSource;
import org.ballerinalang.spi.SystemPackageRepositoryProvider;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.LockFile;
import org.ballerinalang.toml.model.LockFileImport;
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
import org.wso2.ballerinalang.compiler.packaging.converters.URIDryConverter;
import org.wso2.ballerinalang.compiler.packaging.repo.BinaryRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.BirRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.HomeBaloRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.HomeBirRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.PathBaloRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.ProgramingSourceRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.ProjectSourceRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.RemoteRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.Repo;
import org.wso2.ballerinalang.compiler.parser.Parser;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SymbolEnter;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * Manifest of the current project.
     */
    private final Manifest manifest;
    private final LockFile lockFile;

    private final CompilerOptions options;
    private final Parser parser;
    private final SourceDirectory sourceDirectory;
    private final PackageCache packageCache;
    private final SymbolEnter symbolEnter;
    private final BIRPackageSymbolEnter birPackageSymbolEnter;
    private final Names names;
    private static final boolean shouldReadBalo = true;
    private final CompilerPhase compilerPhase;
    
    /**
     * Holds the manifests of modules resolved by dependency paths.
     */
    private Map<PackageID, Manifest> dependencyManifests = new HashMap<>();

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
        this.compilerPhase = this.options.getCompilerPhase();
        this.parser = Parser.getInstance(context);
        this.packageCache = PackageCache.getInstance(context);
        this.symbolEnter = SymbolEnter.getInstance(context);
        this.birPackageSymbolEnter = BIRPackageSymbolEnter.getInstance(context);
        this.names = Names.getInstance(context);
        this.offline = Boolean.parseBoolean(options.get(OFFLINE));
        this.testEnabled = Boolean.parseBoolean(options.get(TEST_ENABLED));
        this.lockEnabled = Boolean.parseBoolean(options.get(LOCK_ENABLED));
        this.manifest = ManifestProcessor.getInstance(context).getManifest();
        this.repos = genRepoHierarchy(Paths.get(options.get(PROJECT_DIR)));
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
        Converter<Path> converter = sourceDirectory.getConverter();
    
        Path ballerinaHome = Paths.get(System.getProperty(ProjectDirConstants.BALLERINA_HOME));
        Repo systemBirRepo = new BirRepo(ballerinaHome);
        Repo systemZipRepo = new BinaryRepo(RepoUtils.getLibDir(), compilerPhase);
        Repo remoteRepo = new RemoteRepo(URI.create(RepoUtils.getRemoteRepoURL()),
                                         this.dependencyManifests, ballerinaHome);
        Repo remoteDryRepo = new RemoteRepo(new URIDryConverter(URI.create(RepoUtils.getRemoteRepoURL()),
                this.dependencyManifests), ballerinaHome);
        Repo homeBaloCache = new HomeBaloRepo(this.dependencyManifests);
        Repo homeBirRepo = new HomeBirRepo();
        Repo secondarySystemRepo = new BinaryRepo(RepoUtils.getLibDir(), compilerPhase);

        RepoNode homeCacheNode;

        if (offline) {
            homeCacheNode = node(homeBaloCache,
                                node(systemBirRepo,
                                    node(systemZipRepo)));
        } else {
            homeCacheNode = node(homeBaloCache,
                                node (systemBirRepo,
                                    node(systemZipRepo,
                                        node(remoteRepo,
                                            node(homeBaloCache,
                                                node(systemBirRepo,
                                                    node(secondarySystemRepo)))))));
        }
        
        if (null != this.manifest) {
            // Skip checking home bir cache if there are dependencies to be resolved by path. This is because when a
            // module is resolved by BIR, it cannot resolve it's imports using BALO source. This happens when replacing
            // transitive dependencies using balo path.
            Optional<Dependency> pathDependency = this.manifest.getDependencies().stream()
                    .filter(dep -> null != dep.getMetadata())
                    .filter(dep -> null != dep.getMetadata().getPath())
                    .findAny();
            if (!pathDependency.isPresent()) {
                homeCacheNode = node(homeBirRepo, homeCacheNode);
            }
        }
    
        // check latest in central if not offline. if a module's gets resolved by toml or lock then remote is not
        // checked for latest version.
        if (!this.offline) {
            homeCacheNode = node(remoteDryRepo, homeCacheNode);
        }
        
        RepoNode fullRepoGraph;
        if (converter != null) {
            Repo programingSource = new ProgramingSourceRepo(converter);
            Repo projectSource = new ProjectSourceRepo(converter, this.manifest, testEnabled);
            fullRepoGraph = node(programingSource,
                                 node(projectSource, homeCacheNode));
        } else {
            fullRepoGraph = homeCacheNode;
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
        updateModuleIDVersion(pkgId, enclPackageId);
        Resolution resolution = resolveModuleByPath(pkgId);
        // if a resolution is found by dependency path
        if (resolution != Resolution.NOT_FOUND) {
            // update repo hierarchy of the resolution back to normal.
            if (null != encPkgRepoHierarchy) {
                resolution.resolvedBy = encPkgRepoHierarchy;
            } else {
                resolution.resolvedBy = this.repos;
            }
        } else {
            if (null != encPkgRepoHierarchy) {
                resolution = encPkgRepoHierarchy.resolve(pkgId);
            } else {
                resolution = this.repos.resolve(pkgId);
            }
        }
    
        if (resolution == Resolution.NOT_FOUND) {
            return null;
        }
        
        CompilerInput firstEntry = resolution.inputs.get(0);
        if (firstEntry.getEntryName().endsWith(Kind.COMPILED.getExtension())) {
            // Binary package has only one file, so using first entry
            return new GenericPackageBinary(pkgId, firstEntry, resolution.resolvedBy, Kind.COMPILED);
        } else if (firstEntry.getEntryName().endsWith(Kind.COMPILED_BIR.getExtension())) {
            return new GenericPackageBinary(pkgId, firstEntry, resolution.resolvedBy, Kind.COMPILED_BIR);
        } else {
            return new GenericPackageSource(pkgId, resolution.inputs, resolution.resolvedBy);
        }
    }

    /**
     * Resolve a module by path if given.
     *
     * @param moduleID The module's ID
     * @return The resolution.
     */
    private Resolution resolveModuleByPath(PackageID moduleID) {
        Repo pathBaloRepo = new PathBaloRepo(this.manifest, this.dependencyManifests);
        RepoHierarchy pathRepoHierarchy = RepoHierarchyBuilder.build(node(pathBaloRepo));
        return pathRepoHierarchy.resolve(moduleID);
    }
    
    /**
     * Update the version of a moduleID if a version is available. Priority order:
     * 1. If a version is available in the Ballerina.lock file.
     * 2. If a version is available on the Ballerina.toml
     * 3. If a version is available in the Ballerina.toml of the enclosing module's balo.
     *
     * @param moduleID      The ID of the module.
     * @param enclPackageId The ID of the parent module.
     */
    private void updateModuleIDVersion(PackageID moduleID, PackageID enclPackageId) {
        String orgName = moduleID.orgName.value;
        String moduleName = moduleID.name.value;

        // Set the version from the Ballerina.lock file found in the current project.
        if (enclPackageId != null && this.hasLockFile(Paths.get(this.options.get(PROJECT_DIR)))) {
            // Not a top level package or bal
            if (this.lockFile.getImports().containsKey(enclPackageId.toString())) {
                List<LockFileImport> foundBaseImport = lockFile.getImports().get(enclPackageId.toString());

                for (LockFileImport nestedImport : foundBaseImport) {
                    if (moduleID.orgName.value.equals(nestedImport.getOrgName()) &&
                            moduleID.name.value.equals(nestedImport.getName())) {
                        moduleID.version = new Name(nestedImport.getVersion());
                        return;
                    }
                }
            }
        }

        // Set version from the Ballerina.toml of the current project.
        if (enclPackageId != null && this.manifest != null) {

            for (Dependency dependency : this.manifest.getDependencies()) {
                if (dependency.getModuleName().equals(moduleName) && dependency.getOrgName().equals(orgName) &&
                        dependency.getMetadata().getVersion() != null &&
                        !"*".equals(dependency.getMetadata().getVersion())) {
                    moduleID.version = new Name(dependency.getMetadata().getVersion());
                    return;
                }
            }
        }

        // Set the version from Ballerina.toml found in dependent balos.
        if (enclPackageId != null && this.dependencyManifests.size() > 0
                && this.dependencyManifests.containsKey(enclPackageId)) {

            for (Dependency manifestDependency : this.dependencyManifests.get(enclPackageId).getDependencies()) {
                if (manifestDependency.getOrgName().equals(moduleID.orgName.value) &&
                        manifestDependency.getModuleName().equals(moduleID.name.value) &&
                        manifestDependency.getMetadata().getVersion() != null &&
                        !"*".equals(manifestDependency.getMetadata().getVersion())) {
                    moduleID.version = new Name(manifestDependency.getMetadata().getVersion());
                    return;
                }
            }
        }
    }

    public BLangPackage loadEntryPackage(PackageID pkgId, PackageID enclPackageId, PrintStream outStream) {
        if (null == outStream) {
            outStream = System.out;
        }
    
        outStream.println("\t" + (pkgId.isUnnamed ? pkgId.sourceFileName.value : pkgId.toString()));
    
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
        define(packageNode);
        return packageNode;
    }

    public BLangPackage loadPackage(PackageID pkgId) {
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

        // lookup symbol cache again as the updated pkg from repo resolving can reside in the cache
        packageSymbol = this.packageCache.getSymbol(pkgEntity.getPackageId());
        if (packageSymbol != null) {
            return packageSymbol;
        }

        if (pkgEntity.getKind() == PackageEntity.Kind.SOURCE) {
            return parseAndDefine(packageId, (PackageSource) pkgEntity);
        } else if (pkgEntity.getKind() == Kind.COMPILED || pkgEntity.getKind() == Kind.COMPILED_BIR) {
            return loadCompiledPackageAndDefine(packageId, (PackageBinary) pkgEntity);
        }

        return null;
    }


    // Private methods

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
        if (pkgNode.hasTestablePackage()) {
            BLangPackage testablePackage = pkgNode.getTestablePkg();
            testablePackage.symbol.compiledPackage = createInMemoryCompiledPackage(testablePackage);
        }
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
        BPackageSymbol pkgSymbol;
        pkgSymbol = this.birPackageSymbolEnter.definePackage(pkgId, pkgBinary.getRepoHierarchy(), pkgBinaryContent);
        this.packageCache.putSymbol(pkgSymbol.pkgID, pkgSymbol);

        // TODO create CompiledPackage
        return pkgSymbol;
    }

    private CompiledPackage createInMemoryCompiledPackage(BLangPackage pkgNode) {
        PackageID packageID = pkgNode.packageID;
        InMemoryCompiledPackage compiledPackage = new InMemoryCompiledPackage(packageID);

        // Get the list of source entries.
        Path projectPath = this.sourceDirectory.getPath();
        ProjectSourceRepo projectSourceRepo = new ProjectSourceRepo(projectPath, this.manifest, testEnabled);
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
    
    /**
     * Check if lock file is empty.
     *
     * @param sourceRoot The sourceroot of the project.
     * @return True if lock file is valid, else false.
     */
    public boolean hasLockFile(Path sourceRoot) {
        return RepoUtils.isBallerinaProject(sourceRoot) &&
               null != this.lockFile &&
               null != this.lockFile.getImports() &&
               this.lockFile.getImports().size() > 0;
    }
}

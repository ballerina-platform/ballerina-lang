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

import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompiledPackage;
import org.ballerinalang.repository.CompilerOutputEntry;
import org.ballerinalang.repository.PackageBinary;
import org.ballerinalang.repository.PackageEntity;
import org.ballerinalang.repository.PackageEntity.Kind;
import org.ballerinalang.repository.PackageSource;
import org.ballerinalang.toml.model.Dependency;
import org.ballerinalang.toml.model.LockFile;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.parser.LockFileProcessor;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.wso2.ballerinalang.compiler.packaging.Patten;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchy;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchyBuilder;
import org.wso2.ballerinalang.compiler.packaging.RepoHierarchyBuilder.RepoNode;
import org.wso2.ballerinalang.compiler.packaging.converters.Converter;
import org.wso2.ballerinalang.compiler.packaging.converters.URIDryConverter;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.ModuleResolver;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.ModuleResolverImpl;
import org.wso2.ballerinalang.compiler.packaging.module.resolver.model.Project;
import org.wso2.ballerinalang.compiler.packaging.repo.BinaryRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.BirRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.HomeBaloRepo;
import org.wso2.ballerinalang.compiler.packaging.repo.HomeBirRepo;
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
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;
import org.wso2.ballerinalang.compiler.util.diagnotic.BLangDiagnosticLogHelper;
import org.wso2.ballerinalang.util.RepoUtils;

import java.io.PrintStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
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
    private final boolean newParserEnabled;

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
    private final BLangDiagnosticLogHelper dlog;
    private static final boolean shouldReadBalo = true;
    private final CompilerPhase compilerPhase;

    /**
     * Module loader implementation.
     */
    private Project project;
    private org.wso2.ballerinalang.compiler.packaging.module.resolver.RepoHierarchy repoHierarchy;
    private ModuleResolver moduleResolver;

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
        this.dlog = BLangDiagnosticLogHelper.getInstance(context);
        this.offline = Boolean.parseBoolean(options.get(OFFLINE));
        this.testEnabled = Boolean.parseBoolean(options.get(TEST_ENABLED));
        this.lockEnabled = Boolean.parseBoolean(options.get(LOCK_ENABLED));
        this.newParserEnabled = Boolean.parseBoolean(options.get(CompilerOptionName.NEW_PARSER_ENABLED));
        this.manifest = ManifestProcessor.getInstance(context).getManifest();
        this.repos = genRepoHierarchy();
        this.lockFile = LockFileProcessor.getInstance(context, this.lockEnabled).getLockFile();

        this.project = new Project(this.manifest, this.lockFile);
        this.repoHierarchy = new org.wso2.ballerinalang.compiler.packaging.module.resolver.RepoHierarchy(
                this.project.getProject().getOrgName(), this.project.getProject().getVersion(), this.offline,
                this.sourceDirectory);
        this.moduleResolver = new ModuleResolverImpl(this.project, this.repoHierarchy, this.lockEnabled);
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
     * @return Repository Hierarchy.
     */
    private RepoHierarchy genRepoHierarchy() {
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

        // resolve the module version
        pkgId = moduleResolver.resolveVersion(pkgId, enclPackageId);
        // if module resolve version fails return null
        if (pkgId == null || pkgId.version.getValue() == null || "".equals(pkgId.version.getValue())) {
            return null;
        }
        // resolve module
        PackageEntity pkgEntity = moduleResolver.resolveModule(pkgId);

        if (pkgEntity == null) {
            // Do not throw an error here. Otherwise package build will terminate immediately if
            // there are errors in atleast one package during the build. But instead we should
            // continue compiling the other packages as well, and check for their errors.
            return null;
        }

        // No need to parse
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

        // resolve the module version
        pkgId = moduleResolver.resolveVersion(pkgId, null);

        // resolve module
        PackageEntity pkgEntity = moduleResolver.resolveModule(pkgId);

        BLangPackage packageNode = loadPackageFromEntity(pkgId, pkgEntity);
        if (packageNode == null) {
            throw ProjectDirs.getPackageNotFoundError(pkgId);
        }
        return packageNode;
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

    public BPackageSymbol loadPackageSymbol(PackageID packageId, PackageID enclPackageId) {
        // check if the module version exists in the package cache
        BPackageSymbol packageSymbol = this.packageCache.getSymbol(packageId);
        if (packageSymbol != null) {
            return packageSymbol;
        }

        // resolve the module version
        packageId = moduleResolver.resolveVersion(packageId, enclPackageId);
        // resolve module
        PackageEntity pkgEntity = moduleResolver.resolveModule(packageId);

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
        BLangPackage packageNode;
        if (this.newParserEnabled) {
            packageNode = this.parser.parseNew(pkgSource, this.sourceDirectory.getPath());
        } else {
            packageNode = this.parser.parse(pkgSource, this.sourceDirectory.getPath());
        }
        packageNode.packageID = pkgId;
        // Set the same packageId to the testable node
        packageNode.getTestablePkgs().forEach(testablePkg -> testablePkg.packageID = pkgId);
        this.packageCache.put(pkgId, packageNode);
        return packageNode;
    }

    private BPackageSymbol loadCompiledPackageAndDefine(PackageID pkgId, PackageBinary pkgBinary) {
        byte[] pkgBinaryContent = pkgBinary.getCompilerInput().getCode();
        BPackageSymbol pkgSymbol;
        pkgSymbol = this.birPackageSymbolEnter.definePackage(pkgId, pkgBinaryContent);
        this.packageCache.putSymbol(pkgId, pkgSymbol);

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
}

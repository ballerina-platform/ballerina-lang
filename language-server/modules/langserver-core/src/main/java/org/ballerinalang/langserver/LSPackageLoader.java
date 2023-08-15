/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.Module;
import io.ballerina.projects.ModuleDescriptor;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.ModuleName;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageDependencyScope;
import io.ballerina.projects.PackageDescriptor;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.environment.PackageRepository;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.environment.ResolutionRequest;
import io.ballerina.projects.internal.environment.BallerinaDistribution;
import io.ballerina.projects.internal.environment.BallerinaUserHome;
import org.ballerinalang.langserver.codeaction.CodeActionModuleId;
import org.ballerinalang.langserver.common.utils.ModuleUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.completions.providers.context.util.ServiceTemplateGenerator;
import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.WorkDoneProgressBegin;
import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.WorkDoneProgressEnd;
import org.eclipse.lsp4j.WorkDoneProgressReport;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.util.Names;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * An in-memory cache for Ballerina modules(packages) in local, remote and central repositories.
 */
public class LSPackageLoader {
    public static final LanguageServerContext.Key<LSPackageLoader> LS_PACKAGE_LOADER_KEY =
            new LanguageServerContext.Key<>();

    private final List<ModuleInfo> distRepoModuleDescriptors = new ArrayList<>();
    private final List<ModuleInfo> remoteRepoModuleDescriptors = new ArrayList<>();
    private final List<ModuleInfo> localRepoModuleDescriptors = new ArrayList<>();
    private final List<ModuleInfo> centralPackages = new ArrayList<>();
    private final LSClientLogger clientLogger;

    ExtendedLanguageClient languageClient;

    private String notificationTaskId;

    private boolean initialized = false;

    public static LSPackageLoader getInstance(LanguageServerContext context) {
        LSPackageLoader lsPackageLoader = context.get(LS_PACKAGE_LOADER_KEY);
        if (lsPackageLoader == null) {
            lsPackageLoader = new LSPackageLoader(context);
        }
        return lsPackageLoader;
    }

    private LSPackageLoader(LanguageServerContext context) {
        this.clientLogger = LSClientLogger.getInstance(context);
        context.put(LS_PACKAGE_LOADER_KEY, this);
        loadModules(context);
    }

    public boolean isInitialized() {
        return Boolean.TRUE.equals(initialized);
    }

    /**
     * Load modules from the Ballerina environment.
     *
     * @param lsContext language server context.
     */
    public void loadModules(LanguageServerContext lsContext) {
        if (isInitialized()) {
            return;
        }
        String taskId = UUID.randomUUID().toString();
        notificationTaskId = taskId;
        this.languageClient = lsContext.get(ExtendedLanguageClient.class);
        LSClientLogger clientLogger = LSClientLogger.getInstance(lsContext);
        CompletableFuture.runAsync(() -> {
            if (languageClient != null) {
                // Initialize progress notification
                WorkDoneProgressCreateParams workDoneProgressCreateParams = new WorkDoneProgressCreateParams();
                workDoneProgressCreateParams.setToken(taskId);
                languageClient.createProgress(workDoneProgressCreateParams);

                // Start progress
                WorkDoneProgressBegin beginNotification = new WorkDoneProgressBegin();
                beginNotification.setTitle("Package Loader");
                beginNotification.setCancellable(false);
                beginNotification.setMessage("Initializing...");
                languageClient.notifyProgress(new ProgressParams(Either.forLeft(taskId),
                        Either.forLeft(beginNotification)));
            }
        }).thenRunAsync(() -> {
            clientLogger.logTrace("Loading packages from Ballerina distribution");
            Environment environment = EnvironmentBuilder.getBuilder().build();
            BallerinaDistribution ballerinaDistribution = BallerinaDistribution.from(environment);
            PackageRepository packageRepository = ballerinaDistribution.packageRepository();
            List<String> skippedLangLibs = Arrays.asList("lang.annotations", "lang.__internal", "lang.query");
            this.distRepoModuleDescriptors.addAll(resolveModulesFromRepository(packageRepository, REPO.DIST, 
                    skippedLangLibs, Collections.emptySet()));
            Set<String> distRepoModuleIdentifiers = distRepoModuleDescriptors.stream().map(ModuleInfo::moduleIdentifier)
                    .collect(Collectors.toSet());
            clientLogger.logTrace("Successfully loaded packages from Ballerina distribution");


            clientLogger.logTrace("Loading packages from Ballerina User Home");
            BallerinaUserHome ballerinaUserHome = BallerinaUserHome.from(environment);
            //Load modules from local repo
            PackageRepository localRepository = ballerinaUserHome.localPackageRepository();
            this.localRepoModuleDescriptors.addAll(resolveModulesFromRepository(localRepository, REPO.LOCAL,
                    Collections.emptyList(), distRepoModuleIdentifiers));

            //Load modules from remote repo
            PackageRepository remoteRepository = ballerinaUserHome.remotePackageRepository();
            Set<String> loadedModules = new HashSet<>();
            loadedModules.addAll(distRepoModuleIdentifiers);
            loadedModules.addAll(distRepoModuleDescriptors.stream().map(ModuleInfo::moduleIdentifier)
                    .collect(Collectors.toSet()));
            this.remoteRepoModuleDescriptors.addAll(resolveModulesFromRepository(remoteRepository, REPO.REMOTE,
                    Collections.emptyList(),
                    loadedModules));
            clientLogger.logTrace("Successfully loaded packages from Ballerina User Home");

            //Load packages from central
            clientLogger.logTrace("Loading packages from Ballerina Central");
            this.centralPackages.addAll(CentralPackageDescriptorLoader.getInstance(lsContext)
                    .getCentralPackages(lsContext).stream()
                    .map(packageInfo -> {
                        PackageOrg packageOrg = PackageOrg.from(packageInfo.getOrganization());
                        PackageName packageName = PackageName.from(packageInfo.getName());
                        PackageVersion packageVersion = PackageVersion.from(packageInfo.getVersion());
                        PackageDescriptor packageDescriptor =
                                PackageDescriptor.from(packageOrg, packageName, packageVersion, null);
                        return new ModuleInfo(packageDescriptor);
                    }).toList());
            clientLogger.logTrace("Successfully loaded packages from Ballerina Central");
            this.initialized = true;
        }).thenRunAsync(() -> {
            WorkDoneProgressEnd endNotification = new WorkDoneProgressEnd();
            endNotification.setMessage("Initialized Successfully!");
            languageClient.notifyProgress(new ProgressParams(Either.forLeft(taskId),
                    Either.forLeft(endNotification)));
        }).exceptionally(e -> {
            WorkDoneProgressEnd endNotification = new WorkDoneProgressEnd();
            endNotification.setMessage("Initialization Failed!");
            languageClient.notifyProgress(new ProgressParams(Either.forLeft(taskId),
                    Either.forLeft(endNotification)));
            clientLogger.logTrace("Failed initializing the Package Loader due to "
                    + e.getMessage());
            return null;
        });
    }

    /**
     * Get the local repo module descriptors.
     *
     * @return {@link List} of local repo package module descriptors.
     */
    public List<ModuleInfo> getLocalRepoModules() {
        return this.localRepoModuleDescriptors;
    }

    /**
     * Get the module descriptors from remote repo packages.
     *
     * @return {@link List} of remote repo module descriptors.
     */
    public List<ModuleInfo> getRemoteRepoModules() {
        return this.remoteRepoModuleDescriptors;
    }

    /**
     * Get the module descriptors available in the Ballerina distribution.
     * Here, the list of modules descriptors does not
     * the certain langlib package modules
     * and ballerinai package modules
     *
     * @return {@link List} of modules in Ballerina distribution
     */
    public List<ModuleInfo> getDistributionRepoModules() {
        return this.distRepoModuleDescriptors;
    }

    /**
     * Returns the list of packages that reside in the BallerinaUserHome (.ballerina) directory.
     *
     * @return {@link List< ModuleInfo >} List of package info.
     */
    public List<ModuleInfo> getModulesFromBallerinaUserHome() {
        List<ModuleInfo> moduleDescriptors = new ArrayList<>();
        moduleDescriptors.addAll(this.getLocalRepoModules());
        moduleDescriptors.addAll(this.getRemoteRepoModules());
        return moduleDescriptors;
    }

    /**
     * Get all visible repository and distribution module descriptors.
     *
     * @return {@link List} list of module descriptors.
     */
    public List<ModuleInfo> getAllVisibleModules(DocumentServiceContext ctx) {

        Map<String, ModuleInfo> moduleInfoMap = new HashMap<>();
        this.getDistributionRepoModules().forEach(moduleInfo ->
                moduleInfoMap.put(moduleInfo.moduleIdentifier(), moduleInfo));

        this.getModulesFromBallerinaUserHome().forEach(moduleInfo ->
                moduleInfoMap.put(moduleInfo.moduleIdentifier(), moduleInfo));

        //Load modules from the current package
        Optional<Project> project = ctx.workspace().project(ctx.filePath());
        if (project.isEmpty()) {
            return moduleInfoMap.values().stream().toList();
        }
        Package currentPackage = project.get().currentPackage();
        currentPackage.modules().forEach(module -> {
            ModuleInfo moduleInfo = new ModuleInfo(module, true);
            Optional<Module> currentModule = ctx.currentModule();
            if (currentModule.isEmpty() || module.isDefaultModule() || module.equals(currentModule.get()) ||
                    ModuleUtil.matchingImportedModule(ctx, "", moduleInfo.moduleName).isPresent()) {
                return;
            }
            moduleInfoMap.put(moduleInfo.moduleIdentifier, moduleInfo);
        });
        return new ArrayList<>(moduleInfoMap.values());
    }

    /**
     * Returns the list of modules available in Ballerina central.
     *
     * @return {@link List<ModuleInfo>} list of module descriptors.
     */
    public List<ModuleInfo> getCentralPackages() {
        return centralPackages;
    }

    /**
     * Update the remote module cache when a change in the remote repo has occurred.
     * Eg: Package pull
     *
     * @param context Document service context
     * @return {@link List<ModuleInfo>} The list of modules in the remote repo.
     */
    public List<ModuleInfo> refreshRemoteModules(DocumentServiceContext context) {
        Optional<Project> project = context.workspace().project(context.filePath());
        if (project.isEmpty()) {
            return Collections.emptyList();
        }
        BallerinaUserHome ballerinaUserHome = BallerinaUserHome
                .from(project.get().projectEnvironmentContext().environment());
        PackageRepository remoteRepository = ballerinaUserHome.remotePackageRepository();
        List<ModuleInfo> moduleDescriptors =
                resolveModulesFromRepository(remoteRepository, REPO.REMOTE, Collections.emptyList(),
                        this.remoteRepoModuleDescriptors.stream().map(ModuleInfo::moduleIdentifier)
                                .collect(Collectors.toSet()));
        this.remoteRepoModuleDescriptors.addAll(moduleDescriptors);
        return moduleDescriptors;
    }

    private List<ModuleInfo> resolveModulesFromRepository(PackageRepository repository,
                                                          REPO repoKind,
                                                          List<String> skipList,
                                                          Set<String> loadedModules) {
        Map<String, List<String>> packageMap = repository.getPackages();
        Map<String, ModuleInfo> moduleDescriptors = new LinkedHashMap<>();
        packageMap.forEach((key, value) -> {
            if (key.equals(Names.BALLERINA_INTERNAL_ORG.getValue())) {
                return;
            }
            value.forEach(nameEntry -> {
                String[] components = nameEntry.split(":");
                if (components.length != 2 || skipList.contains(components[0])) {
                    return;
                }
                String nameComponent = components[0];
                String version = components[1];
                PackageOrg packageOrg = PackageOrg.from(key);
                PackageName packageName = PackageName.from(nameComponent);
                String packageIdentifier = packageOrg.toString() + "/" + packageName;
                PackageVersion pkgVersion = PackageVersion.from(version);
                if (loadedModules.contains(packageIdentifier) || moduleDescriptors.containsKey(packageIdentifier)
                        && moduleDescriptors.get(packageIdentifier).packageVersion().equals(pkgVersion)) {
                    return;
                }

                try {
                    PackageDescriptor pkdDesc = PackageDescriptor.from(packageOrg, packageName, pkgVersion);
                    ResolutionRequest request = ResolutionRequest.from(pkdDesc, PackageDependencyScope.DEFAULT);
                    Optional<Package> repoPackage = repository.getPackage(request,
                            ResolutionOptions.builder().setOffline(true).build());
                    repoPackage.map(pkg -> ProjectLoader.loadProject(pkg.project().sourceRoot(),
                                    BuildOptions.builder().setOffline(true).build()))
                            .ifPresent(project -> {
                                CompletableFuture.runAsync(() -> {
                                    // Notify progress
                                    WorkDoneProgressReport reportNotification = new WorkDoneProgressReport();
                                    reportNotification.setMessage("Indexing " + packageOrg + "/" + packageName);
                                    reportNotification.setCancellable(false);
                                    languageClient.notifyProgress(new ProgressParams(Either.forLeft(notificationTaskId),
                                            Either.forLeft(reportNotification)));
                                });
                                Package pkg = project.currentPackage();
                                pkg.modules().forEach(module -> {
                                    //Skip modules that are not exported
                                    if (!pkg.manifest().exportedModules().contains(module.moduleName().toString())) {
                                        return;
                                    }
                                    ModuleInfo moduleInfo = new ModuleInfo(module);
                                    if (!loadedModules.contains(moduleInfo.moduleIdentifier)) {
                                        if (!moduleDescriptors.containsKey(moduleInfo.moduleIdentifier)) {
                                            moduleDescriptors.put(moduleInfo.moduleIdentifier(), moduleInfo);
                                        }
                                        moduleDescriptors.get(moduleInfo.moduleIdentifier())
                                                .addVersion(moduleInfo.packageVersion(), repoKind);
                                    }
                                });
                            });
                } catch (Throwable e) {
                    clientLogger.logTrace("Failed to resolve package "
                            + packageOrg + (!packageOrg.value().isEmpty() ? "/" : packageName + ":" + pkgVersion));
                }
            });

        });
        return moduleDescriptors.values().stream().toList();
    }


    /**
     * A light-weight module information holder.
     */
    public static class ModuleInfo {

        private ModuleDescriptor moduleDescriptor;

        private String moduleName;

        private String moduleIdentifier;

        private ModuleId moduleId;

        private Path sourceRoot;

        private boolean moduleFromCurrentPackage = false;

        private final Map<REPO, List<PackageVersion>> versionMap = new LinkedHashMap<>();

        private final List<ServiceTemplateGenerator.ListenerMetaData> listenerMetaDataList = new ArrayList<>();

        public ModuleInfo(PackageDescriptor packageDescriptor) {
            this.moduleName = packageDescriptor.name().value();
            this.moduleDescriptor = ModuleDescriptor.from(ModuleName.from(packageDescriptor.name(), moduleName),
                    packageDescriptor);
            this.moduleIdentifier = packageDescriptor.org().toString().isEmpty() ? packageDescriptor.name().value() :
                    packageDescriptor.name().value() + "/" + moduleName;
        }

        public ModuleInfo(Module module) {
            this.moduleDescriptor = module.descriptor();
            this.moduleName = this.moduleDescriptor.name().toString();
            this.moduleIdentifier = this.moduleDescriptor.org().toString().isEmpty() ? moduleName
                    : this.moduleDescriptor.org().toString() + "/" + moduleName;
            this.sourceRoot = module.project().sourceRoot();
            this.moduleId = module.moduleId();
            addServiceSnippetMetaData(module);
        }

        public ModuleInfo(Module module, boolean moduleFromCurrentPackage) {
            this.moduleDescriptor = module.descriptor();
            this.moduleName = this.moduleDescriptor.name().toString();
            this.moduleIdentifier = this.moduleDescriptor.org().toString().isEmpty() ? moduleName
                    : this.moduleDescriptor.org().toString() + "/" + moduleName;
            this.sourceRoot = module.project().sourceRoot();
            this.moduleId = module.moduleId();
            this.moduleFromCurrentPackage = moduleFromCurrentPackage;
        }

        public PackageName packageName() {
            return this.moduleDescriptor.packageName();
        }

        public PackageOrg packageOrg() {
            if (moduleFromCurrentPackage) {
                return PackageOrg.from("");
            }
            return this.moduleDescriptor.org();
        }

        public PackageVersion packageVersion() {
            return this.moduleDescriptor.version();
        }

        public Optional<Path> sourceRoot() {
            return Optional.ofNullable(this.sourceRoot);
        }

        public String moduleIdentifier() {
            return this.moduleIdentifier;
        }

        public String moduleName() {
            return this.moduleName;
        }

        public Optional<ModuleId> getModuleId() {
            return Optional.ofNullable(this.moduleId);
        }

        public Optional<ModuleDescriptor> descriptor() {
            return Optional.ofNullable(moduleDescriptor);
        }

        public List<ServiceTemplateGenerator.ListenerMetaData> getListenerMetaDataList() {
            return listenerMetaDataList;
        }

        public boolean isModuleFromCurrentPackage() {
            return moduleFromCurrentPackage;
        }

        public Map<REPO, List<PackageVersion>> getVersionMap() {
            return versionMap;
        }

        public void addVersion(PackageVersion packageVersion, REPO repoKind) {
            versionMap.computeIfAbsent(repoKind, (repo) -> Collections.emptyList()).add(packageVersion);
        }

        private void addServiceSnippetMetaData(Module module) {
            SemanticModel semanticModel = module.getCompilation().getSemanticModel();
            ModuleID moduleID = CodeActionModuleId.from(this.packageOrg().value(), this.moduleName(),
                    this.packageVersion().toString());
            semanticModel.moduleSymbols().stream().filter(listenerPredicate())
                    .forEach(listener -> ServiceTemplateGenerator.generateServiceSnippetMetaData(listener, moduleID)
                            .ifPresent(this.listenerMetaDataList::add));
        }
    }

    /**
     * Represents package repositories.
     */
    public enum REPO {
        LOCAL,
        DIST,
        REMOTE
    }

    private static Predicate<Symbol> listenerPredicate() {
        return symbol -> SymbolUtil.isListener(symbol) && symbol.kind() == SymbolKind.CLASS;
    }
}

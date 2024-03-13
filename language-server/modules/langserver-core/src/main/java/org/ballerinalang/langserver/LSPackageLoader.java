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
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
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
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.completions.providers.context.util.ServiceTemplateGenerator;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * An in-memory cache for Ballerina modules(packages) in local, remote and central repositories.
 */
public class LSPackageLoader {

    public static final LanguageServerContext.Key<LSPackageLoader> LS_PACKAGE_LOADER_KEY =
            new LanguageServerContext.Key<>();

    private List<ModuleInfo> distRepoPackages = new ArrayList<>();
    private final List<ModuleInfo> remoteRepoPackages = new ArrayList<>();
    private final List<ModuleInfo> localRepoPackages = new ArrayList<>();
    private List<ModuleInfo> centralPackages = new ArrayList<>();
    private final LSClientLogger clientLogger;

    ExtendedLanguageClient languageClient;

    private String notificationTaskId;

    private boolean initialized = false;

    private CentralPackageDescriptorLoader centralPackageDescriptorLoader;

    public static LSPackageLoader getInstance(LanguageServerContext context) {
        LSPackageLoader lsPackageLoader = context.get(LS_PACKAGE_LOADER_KEY);
        if (lsPackageLoader == null) {
            lsPackageLoader = new LSPackageLoader(context);
        }

        return lsPackageLoader;
    }

    private LSPackageLoader(LanguageServerContext context) {
        this.clientLogger = LSClientLogger.getInstance(context);
        this.centralPackageDescriptorLoader = CentralPackageDescriptorLoader.getInstance(context);
        context.put(LS_PACKAGE_LOADER_KEY, this);
    }

    public boolean isInitialized() {
        return Boolean.TRUE.equals(initialized);
    }

    /**
     * Load modules from the Ballerina environment.
     *
     * @param context language server context.
     */
    public void loadModules(LanguageServerContext context) {
            LSClientLogger lsClientLogger = LSClientLogger.getInstance(context);
            String taskId = UUID.randomUUID().toString();
            notificationTaskId = taskId;
            Map<String, ModuleInfo> packagesList = new HashMap<>();
            CompletableFuture.runAsync(() -> {
                this.languageClient = context.get(ExtendedLanguageClient.class);
                if (languageClient == null) {
                    return;
                }
                this.languageClient.showMessage(
                        new MessageParams(MessageType.Info, "Indexing Ballerina packages: " +
                                "some completions may not be available until indexing is complete."));
                // Initialize progress notification
                WorkDoneProgressCreateParams workDoneProgressCreateParams = new WorkDoneProgressCreateParams();
                workDoneProgressCreateParams.setToken(taskId);
                languageClient.createProgress(workDoneProgressCreateParams);

                // Start progress
                WorkDoneProgressBegin beginNotification = new WorkDoneProgressBegin();
                beginNotification.setTitle("Indexing");
                beginNotification.setCancellable(false);
                beginNotification.setMessage("Loading packages from Ballerina home...");
                languageClient.notifyProgress(new ProgressParams(Either.forLeft(taskId),
                        Either.forLeft(beginNotification)));
            }).thenRunAsync(() -> {
                Environment environment = EnvironmentBuilder.getBuilder().build();
                BallerinaDistribution ballerinaDistribution = BallerinaDistribution.from(environment);
                PackageRepository packageRepository = ballerinaDistribution.packageRepository();
                List<String> skippedLangLibs = Arrays.asList("lang.annotations", "lang.__internal", "lang.query");
                lsClientLogger.logTrace("Loading packages from Ballerina distribution");
                this.distRepoPackages.addAll(checkAndResolvePackagesFromRepository(packageRepository,
                        skippedLangLibs, Collections.emptySet()));
                Set<String> distRepoModuleIdentifiers = distRepoPackages.stream().map(ModuleInfo::packageIdentifier)
                        .collect(Collectors.toSet());
                lsClientLogger.logTrace("Successfully loaded packages from Ballerina distribution");

                lsClientLogger.logTrace("Loading packages from Ballerina User Home");
                BallerinaUserHome ballerinaUserHome = BallerinaUserHome.from(environment);
                //Load modules from local repo
                PackageRepository localRepository = ballerinaUserHome.localPackageRepository();
                this.localRepoPackages.addAll(checkAndResolvePackagesFromRepository(localRepository,
                        Collections.emptyList(), distRepoModuleIdentifiers));

                //Load modules from remote repo
                PackageRepository remoteRepository = ballerinaUserHome.remotePackageRepository();
                Set<String> loadedModules = new HashSet<>();
                loadedModules.addAll(distRepoModuleIdentifiers);
                loadedModules.addAll(localRepoPackages.stream().map(ModuleInfo::packageIdentifier)
                        .collect(Collectors.toSet()));
                this.remoteRepoPackages.addAll(checkAndResolvePackagesFromRepository(remoteRepository,
                        Collections.emptyList(),
                        loadedModules));
                lsClientLogger.logTrace("Successfully loaded packages from Ballerina User Home");

                this.getDistributionRepoModules().forEach(packageInfo ->
                        packagesList.put(packageInfo.packageIdentifier(), packageInfo));
                List<ModuleInfo> repoPackages = new ArrayList<>();
                repoPackages.addAll(this.getRemoteRepoModules());
                repoPackages.addAll(this.getLocalRepoModules());
                repoPackages.stream().filter(packageInfo -> !packagesList.containsKey(packageInfo.packageIdentifier()))
                        .forEach(packageInfo -> packagesList.put(packageInfo.packageIdentifier(), packageInfo));
            }).thenRunAsync(() -> {
                WorkDoneProgressCreateParams workDoneProgressCreateParams = new WorkDoneProgressCreateParams();
                workDoneProgressCreateParams.setToken(taskId);
                languageClient.createProgress(workDoneProgressCreateParams);

                // Start progress
                WorkDoneProgressReport progressNotification = new WorkDoneProgressReport();
                progressNotification.setMessage("Loading packages from Ballerina central");
                progressNotification.setCancellable(false);
                languageClient.notifyProgress(new ProgressParams(Either.forLeft(taskId),
                        Either.forLeft(progressNotification)));
            }).thenRunAsync(() -> {
                try {
                    this.centralPackages.addAll(this.centralPackageDescriptorLoader.getCentralPackages().get());
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }).thenRunAsync(() -> {
                WorkDoneProgressEnd endNotification = new WorkDoneProgressEnd();
                endNotification.setMessage("Initialized Successfully!");
                languageClient.notifyProgress(new ProgressParams(Either.forLeft(taskId),
                        Either.forLeft(endNotification)));
                languageClient.showMessage(new MessageParams(MessageType.Info, "Indexing completed successfully."));
            }).exceptionally(e -> {
                WorkDoneProgressEnd endNotification = new WorkDoneProgressEnd();
                endNotification.setMessage("Initialization Failed!");
                languageClient.notifyProgress(new ProgressParams(Either.forLeft(taskId),
                        Either.forLeft(endNotification)));
                clientLogger.logTrace("Failed initializing the Package Loader due to "
                        + e.getMessage());
                return null;
            });

            this.initialized = true;
    }

    /**
     * Get the local repo module descriptors.
     *
     * @return {@link List} of local repo package module descriptors.
     */
    public List<ModuleInfo> getLocalRepoModules() {
        return this.localRepoPackages;
    }

    /**
     * Get the module descriptors from remote repo packages.
     *
     * @return {@link List} of remote repo module descriptors.
     */
    public List<ModuleInfo> getRemoteRepoModules() {
        return this.remoteRepoPackages;
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
        return this.distRepoPackages;
    }

    /**
     * Returns the list of modules available in Ballerina central.
     *
     * @return {@link List<ModuleInfo>} list of module descriptors.
     */
    public List<ModuleInfo> getCentralPackages() {
        return this.centralPackages;
    }

    /**
     * Get all visible repository and distribution packages.
     *
     * @return {@link List} packages
     */
    public List<ModuleInfo> getAllVisiblePackages(DocumentServiceContext ctx) {
        Map<String, ModuleInfo> packagesList = new HashMap<>();
        this.getDistributionRepoModules().forEach(packageInfo ->
                packagesList.put(packageInfo.packageIdentifier(), packageInfo));
        List<ModuleInfo> repoPackages = new ArrayList<>();
        repoPackages.addAll(this.getRemoteRepoModules());
        repoPackages.addAll(this.getLocalRepoModules());
        repoPackages.stream().filter(packageInfo -> !packagesList.containsKey(packageInfo.packageIdentifier()))
                .forEach(packageInfo -> packagesList.put(packageInfo.packageIdentifier(), packageInfo));
        Package currentPackage = ctx.workspace().project(ctx.filePath()).get().currentPackage();
        currentPackage.modules().forEach(module -> {
            Package packageInstance = module.packageInstance();
            ModuleInfo moduleInfo = new ModuleInfo(PackageOrg.from(""), packageInstance.packageName(),
                    packageInstance.packageVersion(), packageInstance.project().sourceRoot());
            moduleInfo.setModuleFromCurrentPackage(true);
            Optional<Module> currentModule = ctx.currentModule();
            String packageName = moduleInfo.packageName().value();
            String moduleName = module.descriptor().name().moduleNamePart();
            String qualifiedModName = packageName + Names.DOT + moduleName;
            if (currentModule.isEmpty() || module.isDefaultModule() || module.equals(currentModule.get()) ||
                    ModuleUtil.matchingImportedModule(ctx, "", qualifiedModName).isPresent()) {
                return;
            } else {
                moduleInfo.packageName = PackageName.from(packageName + "." + moduleName);
            }
            packagesList.put(moduleInfo.packageName.value(), moduleInfo);
        });
        return new ArrayList<>(packagesList.values());
    }

    /**
     * Returns the list of packages that reside in the BallerinaUserHome (.ballerina) directory.
     *
     * @param ctx Document service context.
     * @return {@link List<ModuleInfo>} List of package info.
     */
    public List<ModuleInfo> getPackagesFromBallerinaUserHome(DocumentServiceContext ctx) {
        List<ModuleInfo> packagesList = new ArrayList<>();
        Optional<Project> project = ctx.workspace().project(ctx.filePath());
        if (project.isEmpty()) {
            return Collections.emptyList();
        }
        return packagesList;
    }

    public List<ModuleInfo> checkAndResolvePackagesFromRepository(PackageRepository repository, List<String> skipList,
                                                                   Set<String> loadedPackages) {
        Map<String, List<String>> packageMap = repository.getPackages();
        List<ModuleInfo> packages = new ArrayList<>();
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
                if (loadedPackages.contains(packageIdentifier)) {
                    return;
                }
                PackageVersion pkgVersion = PackageVersion.from(version);

                try {
                    PackageDescriptor pkdDesc = PackageDescriptor.from(packageOrg, packageName, pkgVersion);
                    ResolutionRequest request = ResolutionRequest.from(pkdDesc, PackageDependencyScope.DEFAULT);

                    Optional<Package> repoPackage = repository.getPackage(request,
                            ResolutionOptions.builder().setOffline(true).build());
                    repoPackage.ifPresent(pkg -> packages.add(new ModuleInfo(pkg)));
                } catch (Throwable e) {
                    clientLogger.logTrace("Failed to resolve package "
                            + packageOrg + (!packageOrg.value().isEmpty() ? "/" : "" 
                            + packageName + ":" + pkgVersion));
                }
            });

        });
        return packages;
    }

    public List<ModuleInfo> updatePackageMap(DocumentServiceContext context) {
        Optional<Project> project = context.workspace().project(context.filePath());
        if (project.isEmpty()) {
            return Collections.emptyList();
        }
        BallerinaUserHome ballerinaUserHome = BallerinaUserHome
                .from(project.get().projectEnvironmentContext().environment());
        PackageRepository remoteRepository = ballerinaUserHome.remotePackageRepository();
        List<ModuleInfo> moduleInfos =
                checkAndResolvePackagesFromRepository(remoteRepository, Collections.emptyList(),
                        this.remoteRepoPackages.stream().map(ModuleInfo::packageIdentifier)
                                .collect(Collectors.toSet()));
        this.remoteRepoPackages.addAll(moduleInfos);
        return moduleInfos;
    }

    /**
     * A light-weight package information holder.
     */
    public static class ModuleInfo {

        private PackageOrg packageOrg;
        private PackageName packageName;
        private PackageVersion packageVersion;
        private Path sourceRoot;

        private String moduleIdentifier;

        private boolean isModuleFromCurrentPackage = false;

        private final List<ServiceTemplateGenerator.ListenerMetaData> listenerMetaData = new ArrayList<>();

        public ModuleInfo(PackageOrg packageOrg, PackageName packageName, PackageVersion version, Path path) {
            this.packageOrg = packageOrg;
            this.packageName = packageName;
            this.packageVersion = version;
            this.sourceRoot = path;
            this.moduleIdentifier = packageOrg.toString().isEmpty() ? packageName.toString() :
                    packageOrg + "/" + packageName.toString();
        }

        public ModuleInfo(Package pkg) {
            this.packageOrg = pkg.packageOrg();
            this.packageName = pkg.packageName();
            this.packageVersion = pkg.packageVersion();
            this.sourceRoot = pkg.project().sourceRoot();
            this.moduleIdentifier = packageOrg.toString() + "/" + packageName.toString();
            addServiceTemplateMetaData();
        }

        public List<ServiceTemplateGenerator.ListenerMetaData> getListenerMetaData() {
            return listenerMetaData;
        }

        public String getModuleIdentifier() {
            return moduleIdentifier;
        }

        public boolean isModuleFromCurrentPackage() {
            return isModuleFromCurrentPackage;
        }

        public void setModuleFromCurrentPackage(boolean moduleFromCurrentPackage) {
            isModuleFromCurrentPackage = moduleFromCurrentPackage;
        }

        public PackageName packageName() {
            return packageName;
        }

        public PackageOrg packageOrg() {
            return packageOrg;
        }

        public PackageVersion packageVersion() {
            return packageVersion;
        }

        public Path sourceRoot() {
            return sourceRoot;
        }

        public String packageIdentifier() {
            return moduleIdentifier;
        }

        private void addServiceTemplateMetaData() {
            String orgName = ModuleUtil.escapeModuleName(this.packageOrg().value());
            Project project = ProjectLoader.loadProject(this.sourceRoot());
            //May take some time as we are compiling projects.
            PackageCompilation packageCompilation = project.currentPackage().getCompilation();
            Module module = project.currentPackage().getDefaultModule();

            String moduleName = module.descriptor().name().toString();
            String version = module.packageInstance().descriptor().version().value().toString();
            ModuleID moduleID = CodeActionModuleId.from(orgName, moduleName, version);

            SemanticModel semanticModel = packageCompilation.getSemanticModel(module.moduleId());
            semanticModel.moduleSymbols().stream().filter(ServiceTemplateGenerator.listenerPredicate())
                    .forEach(listener ->
                            ServiceTemplateGenerator.generateServiceSnippetMetaData(listener, moduleID)
                                    .ifPresent(listenerMetaData::add));
        }
    }
}

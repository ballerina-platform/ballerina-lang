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

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PackageName;
import io.ballerina.projects.PackageOrg;
import io.ballerina.projects.PackageVersion;
import io.ballerina.projects.Project;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.projects.environment.PackageRepository;
import io.ballerina.projects.internal.environment.BallerinaDistribution;
import io.ballerina.projects.internal.environment.BallerinaUserHome;
import org.ballerinalang.langserver.codeaction.CodeActionModuleId;
import org.ballerinalang.langserver.common.utils.ModuleUtil;
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

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
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
    private Map<String, List<ServiceTemplateGenerator.IndexedListenerMetaData>> cachedListenerMetaData = new HashMap<>();

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

//                Load modules from remote repo
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
                try {
                    this.loadListeners();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
            String packageName = moduleInfo.packageName();
            String moduleName = module.descriptor().name().moduleNamePart();
            String qualifiedModName = packageName + Names.DOT + moduleName;
            if (currentModule.isEmpty() || module.isDefaultModule() || module.equals(currentModule.get()) ||
                    ModuleUtil.matchingImportedModule(ctx, "", qualifiedModName).isPresent()) {
                return;
            } else {
                moduleInfo.packageName = packageName + "." + moduleName;
            }
            packagesList.put(moduleInfo.packageName, moduleInfo);
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
                packages.add(new ModuleInfo(key, nameComponent, version));
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

    private final Path BALLERINA_USER_HOME_INDEX = Path.of(System.getProperty("user.home"))
            .resolve(".ballerina")
            .resolve(".config")
            .resolve("ls-index.json");

    private final Path BALLERINA_HOME_INDEX = Path.of(System.getProperty("ballerina.home"))
            .resolve("resources")
            .resolve("ls-index.json");

    public record LSListenerIndex(String checksum, List<LSPackage> ballerina, List<LSPackage> ballerinax) {
    }

    public record LSPackage(@SerializedName(value = "organization") String orgName,
                            @SerializedName(value = "name") String module, String version, List<LSListener> listeners) {
    }

    public record LSListener(String name, List<Parameter> parameters, List<Record> records) {
    }

    public record Parameter(String name, String type, String category) {
    }

    public record Record(String name, List<Field> fields) {
    }

    public record Field(String name, String type, String category) {
    }

    public Map<String, List<ServiceTemplateGenerator.IndexedListenerMetaData>> getCachedListenerMetaData() {
        return cachedListenerMetaData;
    }

    private static String getFileChecksum(String filePath) throws Exception {
        FileInputStream fileInputStream = new FileInputStream(filePath);

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[1024];

        int n = 0;
        while ((n = fileInputStream.read(buffer)) != -1) {
            messageDigest.update(buffer, 0, n);
        }

        byte[] checksumBytes = messageDigest.digest();
        StringBuilder checksum = new StringBuilder();
        for (byte b : checksumBytes) {
            checksum.append(String.format("%02x", b & 0xff));
        }
        return checksum.toString();
    }

    public void loadListeners() throws IOException {
        // Read the listener file from the ballerina user home
        if (Files.exists(BALLERINA_USER_HOME_INDEX)) {
            // read the json file
            LSListenerIndex lsListenerIndex = new Gson().fromJson(Files.newBufferedReader(BALLERINA_USER_HOME_INDEX),
                    LSListenerIndex.class);
            boolean checksumValid = "af6986b411ccb1a0b5571699b79cc37eebcfc8f66897c9384aff2153b0df7c7f"
                    .equals(lsListenerIndex.checksum);
            boolean indexUpdated = false;
            if (!checksumValid) {
                // Download the file from the central and load the listeners
                indexUpdated = true;
            }

            cacheListenerMetaData(lsListenerIndex);
            // Cache it in the memory
            if (indexUpdated) {
                try (Writer myWriter = new FileWriter(BALLERINA_USER_HOME_INDEX.toFile(), StandardCharsets.UTF_8)) {
                    myWriter.write(lsListenerIndex.toString());
                } catch (IOException e) {
                }
            }
        } else if (Files.exists(BALLERINA_HOME_INDEX)) {
            // read the file
            // get the check sum
            // validate the checksum
            boolean checksumValid = true;
            boolean indexUpdated = false;
            if (checksumValid) {
                // Load the listeners
            } else {
                // Download the file from the central and load the listeners
                indexUpdated = true;
            }
            // Cache the index in the memory
            if (indexUpdated) {
                // stroe the index in the user home
            }
        } else {
            // Download the file from the central and load the listeners
            // cache the index in the memory
            // store the index in the user home
        }
    }

    private void cacheListenerMetaData(LSListenerIndex lsListenerIndex) {
        this.cachedListenerMetaData = new HashMap<>();
        for (LSPackage lsPackage : lsListenerIndex.ballerina()) {
            String qulName = lsPackage.orgName() + ":" + lsPackage.module();
            this.cachedListenerMetaData.put(qulName,
                    ServiceTemplateGenerator.generateIndexedListenerMetaData(lsPackage));

        }
        for (LSPackage lsPackage : lsListenerIndex.ballerinax()) {
            String qulName = lsPackage.orgName() + ":" + lsPackage.module();
            this.cachedListenerMetaData.put(qulName,
                    ServiceTemplateGenerator.generateIndexedListenerMetaData(lsPackage));
        }
    }

    /**
     * A light-weight package information holder.
     */
    public static class ModuleInfo {

        private static final String JSON_PROPERTY_ORGANIZATION = "organization";
        @SerializedName(JSON_PROPERTY_ORGANIZATION)
        private final String packageOrg;

        private static final String JSON_PROPERTY_NAME = "name";
        @SerializedName(JSON_PROPERTY_NAME)
        private String packageName;

        private static final String JSON_PROPERTY_VERSION = "version";
        @SerializedName(JSON_PROPERTY_VERSION)
        private final String packageVersion;

        @Expose(deserialize = false)
        private final Path sourceRoot;

        @Expose(deserialize = false)
        private final String moduleIdentifier;

        @Expose(deserialize = false)
        private boolean isModuleFromCurrentPackage = false;

        @Expose(deserialize = false)
        private final List<ServiceTemplateGenerator.ListenerMetaData> listenerMetaData = new ArrayList<>();

        public ModuleInfo(String packageOrg, String packageName, String packageVersion) {
            this.packageOrg = packageOrg;
            this.packageName = packageName;
            this.packageVersion = packageVersion;
            this.sourceRoot = null;
            this.moduleIdentifier = packageOrg + "/" + packageName;
        }

        public ModuleInfo(PackageOrg packageOrg, PackageName packageName, PackageVersion version, Path path) {
            this.packageOrg = packageOrg.value();
            this.packageName = packageName.value();
            this.packageVersion = version.value().toString();
            this.sourceRoot = path;
            this.moduleIdentifier = packageOrg.toString().isEmpty() ? packageName.toString() :
                    packageOrg + "/" + packageName;
        }

        public ModuleInfo(Package pkg) {
            this.packageOrg = pkg.packageOrg().value();
            this.packageName = pkg.packageName().value();
            this.packageVersion = pkg.packageVersion().value().toString();
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

        public String packageName() {
            return packageName;
        }

        public String packageOrg() {
            return packageOrg;
        }

        public String packageVersion() {
            return packageVersion;
        }

        public Path sourceRoot() {
            return sourceRoot;
        }

        public String packageIdentifier() {
            return moduleIdentifier;
        }

        private void addServiceTemplateMetaData() {
            String orgName = ModuleUtil.escapeModuleName(this.packageOrg());
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

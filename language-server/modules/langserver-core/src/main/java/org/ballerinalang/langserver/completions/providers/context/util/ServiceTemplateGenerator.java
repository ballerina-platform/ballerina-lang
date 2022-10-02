/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.providers.context.util;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Module;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.ProjectLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.LSPackageLoader;
import org.ballerinalang.langserver.codeaction.CodeActionModuleId;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.common.utils.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.DefaultValueGenerationUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.common.utils.ModuleUtil;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.StaticCompletionItem;
import org.ballerinalang.langserver.completions.builder.ServiceTemplateCompletionItemBuilder;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.ProgressParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkDoneProgressBegin;
import org.eclipse.lsp4j.WorkDoneProgressCreateParams;
import org.eclipse.lsp4j.WorkDoneProgressEnd;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generates Service Template Snippet completion items.
 *
 * @since 2.0.0
 */
public class ServiceTemplateGenerator {

    private static final LanguageServerContext.Key<ServiceTemplateGenerator> SERVICE_TEMPLATE_GENERATOR_KEY =
            new LanguageServerContext.Key<>();

    private final Map<Pair<String, String>, List<ListenerMetaData>> moduleListenerMetaDataMap;

    private boolean isInitialized;
    private boolean isUserHomePackagesLoaded;
    private static final String TITLE_SERVICE_TEMPLATE_GENERATOR = "Service Template Generator";

    private ServiceTemplateGenerator(LanguageServerContext context) {
        context.put(SERVICE_TEMPLATE_GENERATOR_KEY, this);
        this.moduleListenerMetaDataMap = new ConcurrentHashMap<>();
        loadListeners(context);
    }

    /**
     * Get an instance of the Service Template Generator.
     *
     * @param context Language Server Context.
     * @return {@link ServiceTemplateGenerator} Service Template Generator.
     */
    public static ServiceTemplateGenerator getInstance(LanguageServerContext context) {
        ServiceTemplateGenerator serviceTemplateGenerator =
                context.get(SERVICE_TEMPLATE_GENERATOR_KEY);
        if (serviceTemplateGenerator == null) {
            serviceTemplateGenerator = new ServiceTemplateGenerator(context);
        }
        return serviceTemplateGenerator;
    }

    private void loadListeners(LanguageServerContext lsContext) {
        String taskId = UUID.randomUUID().toString();
        ExtendedLanguageClient languageClient = lsContext.get(ExtendedLanguageClient.class);
        LSClientLogger clientLogger = LSClientLogger.getInstance(lsContext);
        CompletableFuture.runAsync(() -> {
            if (languageClient != null) {
                // Initialize progress notification
                WorkDoneProgressCreateParams workDoneProgressCreateParams = new WorkDoneProgressCreateParams();
                workDoneProgressCreateParams.setToken(taskId);
                languageClient.createProgress(workDoneProgressCreateParams);

                // Start progress
                WorkDoneProgressBegin beginNotification = new WorkDoneProgressBegin();
                beginNotification.setTitle(TITLE_SERVICE_TEMPLATE_GENERATOR);
                beginNotification.setCancellable(false);
                beginNotification.setMessage("Initializing...");
                languageClient.notifyProgress(new ProgressParams(Either.forLeft(taskId),
                        Either.forLeft(beginNotification)));
            }
        }).thenRunAsync(() -> {
            this.loadListenersFromDistribution(lsContext);
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
            clientLogger.logTrace("Failed loading listener symbols from BallerinaUserHome due to "
                    + e.getMessage());
            return null;
        });
    }

    private void loadListeners(LanguageServerContext lsContext, DocumentServiceContext context) {
        LSClientLogger clientLogger = LSClientLogger.getInstance(lsContext);
        CompletableFuture.runAsync(() -> {
            try {
                this.loadListenersFromBallerinaUserHome(context, lsContext);
            } catch (Throwable e) {
                //ignore
                clientLogger.logTrace("Failed loading listener symbols from the  BallerinaUserHome due to "
                        + e.getMessage());
            }
        });
    }

    /**
     * Loads listener symbols from the distribution.
     *
     * @param context Language Server Context.
     */
    private void loadListenersFromDistribution(LanguageServerContext context) {
        if (!this.initialized()) {
            //Load distribution repo packages
            LSClientLogger clientLogger = LSClientLogger.getInstance(context);
            clientLogger.logTrace("Loading packages from the distribution");
            List<LSPackageLoader.PackageInfo> packages = LSPackageLoader.getInstance(context)
                    .getDistributionRepoPackages();
            loadListenersFromPackages(packages, context);
            this.isInitialized = true;
            clientLogger.logTrace("Finished loading packages from the distribution");
        }
    }

    /**
     * Loads Listeners from the BallerinaUserHome.
     *
     * @param context   Document Service context
     * @param lsContext Language Server context.
     */
    private void loadListenersFromBallerinaUserHome(DocumentServiceContext context,
                                                    LanguageServerContext lsContext) {
        if (!this.userHomePackagesLoaded()) {
            this.isUserHomePackagesLoaded = true;
            //Load packages from BallerinaUserHome
            LSClientLogger clientLogger = LSClientLogger.getInstance(lsContext);
            clientLogger.logTrace("Loading modules from the BallerinaUserHome");
            List<LSPackageLoader.PackageInfo> packages = LSPackageLoader.getInstance(lsContext)
                    .getPackagesFromBallerinaUserHome(context);
            loadListenersFromPackages(packages, lsContext);
            clientLogger.logTrace("Finished loading listener symbols from the  BallerinaUserHome");
        }
    }

    /**
     * Load projects from the distribution repo and generate service data holder.
     *
     * @param packages List of package info.
     */
    private void loadListenersFromPackages(List<LSPackageLoader.PackageInfo> packages, LanguageServerContext context) {
        packages.forEach(distPackage -> {
            String orgName = ModuleUtil.escapeModuleName(distPackage.packageOrg().value());
            Project project = ProjectLoader.loadProject(distPackage.sourceRoot());
            //May take some time as we are compiling projects.
            PackageCompilation packageCompilation = project.currentPackage().getCompilation();
            project.currentPackage().modules().forEach(module -> {

                String moduleName = module.descriptor().name().toString();
                String version = module.packageInstance().descriptor().version().value().toString();
                ModuleID moduleID = CodeActionModuleId.from(orgName, moduleName, version);

                Pair<String, String> moduleKey = Pair.of(moduleName, orgName);
                if (!this.moduleListenerMetaDataMap.containsKey(moduleKey)) {
                    SemanticModel semanticModel = packageCompilation.getSemanticModel(module.moduleId());
                    List<ListenerMetaData> items = new ArrayList<>();
                    semanticModel.moduleSymbols().stream().filter(listenerPredicate())
                            .forEach(listener ->
                                    generateServiceSnippetMetaData(listener, moduleID).ifPresent(items::add));
                    if (!items.isEmpty()) {
                        this.moduleListenerMetaDataMap.put(moduleKey, items);
                    }
                }
            });
        });
    }

    /**
     * Update the module listener meta data map.
     *
     * @param newPackages packages list
     * @param context     language server context
     */
    public void updateListenerMetaDataMap(List<LSPackageLoader.PackageInfo> newPackages,
                                          LanguageServerContext context) {
        this.loadListenersFromPackages(newPackages, context);
    }

    /**
     * Given a module symbol, find and populate service metadata into the moduleServiceTemplateMap cache.
     * Used to dynamically add new entries to the cache.
     *
     * @param moduleSymbol Module symbol.
     * @param ctx          BallerinaCompletion context.
     * @return {@link List<LSCompletionItem>} Set of completion items corresponding to the listeners
     * in the given module.
     */
    public synchronized List<LSCompletionItem> generateAndPopulateListenerMetaData(ModuleSymbol moduleSymbol,
                                                                                   Boolean shouldImport,
                                                                                   BallerinaCompletionContext ctx) {
        ModuleID moduleId = moduleSymbol.id();
        String moduleName = moduleId.moduleName();
        String orgName = moduleId.orgName();
        Pair<String, String> moduleKey = Pair.of(moduleName, orgName);
        List<ListenerMetaData> items = new ArrayList<>();
        moduleSymbol.allSymbols().stream().filter(listenerPredicate())
                .forEach(listener -> generateServiceSnippetMetaData(listener, moduleId).ifPresent(items::add));
        if (!moduleListenerMetaDataMap.containsKey(moduleKey) && !items.isEmpty()) {
            moduleListenerMetaDataMap.put(moduleKey, items);
        }
        return items.stream().map(item ->
                generateServiceSnippet(item, shouldImport, moduleId, ctx)).collect(Collectors.toList());
    }

    /**
     * Generates and returns the service templates for a given completion context.
     *
     * @param ctx Completion context.
     * @return {@link List<LSCompletionItem>} List of completion items.
     */
    public List<LSCompletionItem> getServiceTemplates(BallerinaCompletionContext ctx) {
        if (!userHomePackagesLoaded()) {
            loadListeners(ctx.languageServercontext(), ctx);
        }
        List<LSCompletionItem> completionItems = new ArrayList<>();
        Set<String> processedModuleList = new HashSet<>();

        Optional<Module> currentModule = ctx.workspace().module(ctx.filePath());
        if (currentModule.isEmpty()) {
            return completionItems;
        }
        String currentOrg = currentModule.get().packageInstance().descriptor().org().value();
        String currentModuleName = currentModule.get().descriptor().name().toString();
        String currentVersion = currentModule.get().packageInstance().descriptor().version().value().toString();
        ModuleID currentModuleID = CodeActionModuleId.from(currentOrg, currentModuleName, currentVersion);

        //Find listeners from current imports and generate completion items.
        Map<ImportDeclarationNode, ModuleSymbol> currentDocImports = ctx.currentDocImportsMap();
        currentDocImports.forEach((importNode, moduleSymbol) -> {
            String orgName = importNode.orgName().isEmpty() ? currentOrg : importNode.orgName().get().orgName().text();
            String moduleName = importNode.moduleName().stream()
                    .map(Token::text)
                    .collect(Collectors.joining("."));
            String moduleHash = generateModuleHash(orgName, moduleName);
            if (processedModuleList.contains(moduleHash)) {
                return;
            }

            Pair<String, String> key = Pair.of(moduleName, orgName);
            //check if the module has already been processed to the cache.
            if (this.moduleListenerMetaDataMap.containsKey(key)) {
                moduleListenerMetaDataMap.get(key).forEach(item ->
                        completionItems.add(generateServiceSnippet(item, false, currentModuleID, ctx)));
                processedModuleList.add(moduleHash);
                return;
            }

            //Check if the module belongs to the current project. 
            //If it is not from the current project populate to the cache.
            if (!getModuleNamesOfCurrentProject(ctx, currentOrg).contains(moduleHash)) {
                completionItems.addAll(generateAndPopulateListenerMetaData(moduleSymbol, false, ctx));
                processedModuleList.add(moduleHash);
                return;
            }

            moduleSymbol.allSymbols().stream().filter(listenerPredicate())
                    .forEach(listener -> generateServiceSnippetMetaData(listener, moduleSymbol.id())
                            .ifPresent(item ->
                                    completionItems.add(generateServiceSnippet(item, false, currentModuleID, ctx))));
            processedModuleList.add(moduleHash);
        });

        //Generate service templates for listeners from the distribution
        this.moduleListenerMetaDataMap.forEach((key, items) -> {
            String moduleName = key.getLeft();
            String orgName = key.getRight();
            String moduleHash = generateModuleHash(orgName, moduleName);
            if (processedModuleList.contains(moduleHash)) {
                return;
            }
            for (ListenerMetaData item : items) {
                completionItems.add(generateServiceSnippet(item, true, currentModuleID, ctx));
            }
            processedModuleList.add(moduleHash);
        });

        //Generate completion items for the listeners in the current project.
        Optional<Project> project = ctx.workspace().project(ctx.filePath());
        Optional<PackageCompilation> packageCompilation =
                ctx.workspace().waitAndGetPackageCompilation(ctx.filePath());
        if (project.isEmpty() || packageCompilation.isEmpty()) {
            return completionItems;
        }
        boolean isDefaultModule = currentModule.get().isDefaultModule();
        
        project.get().currentPackage().modules().forEach(module -> {
            //Symbols in the default module should not be visible to other modules.
            if (module.isDefaultModule() && !isDefaultModule) {
                return;
            }

            boolean isCurrentModule = (project.get().kind() == ProjectKind.SINGLE_FILE_PROJECT
                    || currentModule.get().equals(module));

            String moduleName = module.moduleName().toString();
            String moduleHash = generateModuleHash(currentOrg, moduleName);
            String version = currentModule.get().packageInstance().descriptor().version().value().toString();
            ModuleID moduleID = isCurrentModule ? currentModuleID :
                    CodeActionModuleId.from(currentOrg, moduleName, version);

            if (processedModuleList.contains(moduleHash)) {
                return;
            }
            SemanticModel semanticModel = packageCompilation.get().getSemanticModel(module.moduleId());
            semanticModel.moduleSymbols().stream().filter(listenerPredicate()).forEach(listener ->
                    generateServiceSnippetMetaData(listener, moduleID).ifPresent(item ->
                            completionItems.add(generateServiceSnippet(item,
                                    !isCurrentModule, currentModuleID, ctx))));
        });
        return completionItems;
    }

    public boolean initialized() {
        return this.isInitialized;
    }

    public boolean userHomePackagesLoaded() {
        return this.isUserHomePackagesLoaded;
    }

    private Predicate<Symbol> listenerPredicate() {
        return symbol -> SymbolUtil.isListener(symbol) && symbol.kind() == SymbolKind.CLASS;
    }

    private String generateModuleHash(String orgName, String moduleName) {
        return orgName.isEmpty() ? moduleName : orgName + CommonKeys.SLASH_KEYWORD_KEY + moduleName;
    }

    private Set<String> getModuleNamesOfCurrentProject(BallerinaCompletionContext ctx, String orgName) {
        Set<String> modulesHashSet = new HashSet<>();
        Optional<Project> project = ctx.workspace().project(ctx.filePath());
        if (project.isEmpty()) {
            return modulesHashSet;
        }
        project.get().currentPackage().modules().forEach(module -> {
            String hash = generateModuleHash(orgName, module.moduleName().toString());
            modulesHashSet.add(hash);
        });
        return modulesHashSet;
    }

    /**
     * Given a Symbol of a listener, process and pre-generate listener meta data.
     *
     * @param symbol   Listener symbol.
     * @param moduleID ModuleID of the module of symbol.
     * @return {@link ListenerMetaData} Pre processed metadata of the symbol.
     */
    private Optional<ListenerMetaData> generateServiceSnippetMetaData(Symbol symbol,
                                                                      ModuleID moduleID) {

        //Check if the provided symbol is a listener.
        Optional<? extends TypeSymbol> symbolTypeDesc = SymbolUtil.getTypeDescriptor(symbol);
        if (symbolTypeDesc.isEmpty() || !SymbolUtil.isListener(symbol) || symbol.kind() != SymbolKind.CLASS) {
            return Optional.empty();
        }
        ClassSymbol classSymbol = (ClassSymbol) CommonUtil.getRawType(symbolTypeDesc.get());
        if (classSymbol.getName().isEmpty()) {
            return Optional.empty();
        }

        //Get the attach method of the listener.
        MethodSymbol attachMethod = classSymbol.methods().get("attach");
        if (attachMethod == null || classSymbol.getName().isEmpty()) {
            return Optional.empty();
        }

        //Check if the first parameter of the attach method is a subtype of service object.
        Optional<List<ParameterSymbol>> params = attachMethod.typeDescriptor().params();
        if (params.isEmpty() || params.get().size() == 0) {
            return Optional.empty();
        }
        TypeSymbol typeSymbol = CommonUtil.getRawType(params.get().get(0).typeDescriptor());
        ObjectTypeSymbol serviceTypeSymbol;
        if (typeSymbol.typeKind() == TypeDescKind.UNION) {
            //Here we consider the first service type of the union. 
            Optional<TypeSymbol> memberType = ((UnionTypeSymbol) typeSymbol).memberTypeDescriptors()
                    .stream().filter(member -> member.typeKind() == TypeDescKind.OBJECT).findFirst();
            if (memberType.isEmpty()) {
                return Optional.empty();
            }
            serviceTypeSymbol = (ObjectTypeSymbol) memberType.get();
        } else if (typeSymbol.typeKind() == TypeDescKind.OBJECT) {
            serviceTypeSymbol = (ObjectTypeSymbol) typeSymbol;
        } else {
            return Optional.empty();
        }

        //Listener initialization snippet
        //Snippet index 1 is provided for attachment point in service definition.
        Optional<MethodSymbol> initMethod = classSymbol.initMethod();
        int snippetIndex = 2;
        String listenerInitArgs = "";
        if (initMethod.isPresent() && initMethod.get().typeDescriptor().params().isPresent()) {
            List<String> args = new ArrayList<>();
            List<ParameterSymbol> requiredParams = initMethod.get().typeDescriptor().params().get().stream()
                    .filter(parameterSymbol ->
                            parameterSymbol.paramKind() == ParameterKind.REQUIRED).collect(Collectors.toList());
            for (ParameterSymbol parameterSymbol : requiredParams) {
                args.add("${" + snippetIndex + ":" +
                        DefaultValueGenerationUtil.getDefaultPlaceholderForType(parameterSymbol.typeDescriptor())
                                .orElse("") + "}");
                snippetIndex += 1;
            }
            listenerInitArgs = String.join(",", args);
        }

        String symbolName = classSymbol.getName().get();
        return Optional.of(new ListenerMetaData(listenerInitArgs,
                new ArrayList<>(serviceTypeSymbol.methods().values()),
                symbolName, snippetIndex, moduleID));
    }

    private LSCompletionItem generateServiceSnippet(ListenerMetaData serviceSnippet, Boolean shouldImport,
                                                    ModuleID currentModuleID,
                                                    BallerinaCompletionContext context) {

        String symbolReference;
        ImportsAcceptor importsAcceptor = new ImportsAcceptor(context);
        String modulePrefix = ModuleUtil.getModulePrefix(importsAcceptor, currentModuleID,
                serviceSnippet.moduleID, context);
        String moduleAlias = modulePrefix.replace(":", "");
        String moduleName = ModuleUtil.escapeModuleName(serviceSnippet.moduleID.moduleName());

        if (!moduleAlias.isEmpty()) {
            symbolReference = modulePrefix + serviceSnippet.symbolName;
        } else {
            symbolReference = serviceSnippet.symbolName;
        }

        String listenerInitialization = "new " + symbolReference + "(" + serviceSnippet.listenerInitArgs + ")";
        int snippetIndex = serviceSnippet.currentSnippetIndex;
        List<String> methodSnippets = new ArrayList<>();

        if (!serviceSnippet.unimplementedMethods.isEmpty()) {
            for (MethodSymbol methodSymbol : serviceSnippet.unimplementedMethods) {
                String functionSnippet =
                        generateMethodSnippet(importsAcceptor, methodSymbol, snippetIndex, context);
                methodSnippets.add(functionSnippet);
                snippetIndex += 1;
            }
        }

        String snippet = SyntaxKind.SERVICE_KEYWORD.stringValue() + " ${1} " +
                SyntaxKind.ON_KEYWORD.stringValue() + " " + listenerInitialization +
                " {" + CommonUtil.LINE_SEPARATOR + (serviceSnippet.unimplementedMethods.isEmpty() ?
                "    ${" + snippetIndex + "}" : String.join("", methodSnippets)) + CommonUtil.LINE_SEPARATOR + "}" +
                CommonUtil.LINE_SEPARATOR;

        String label;
        String filterText;
        String detail = ItemResolverConstants.SNIPPET_TYPE;

        label = shouldImport ? "service on " + moduleName + ":" +
                serviceSnippet.symbolName : "service on " + symbolReference;
        filterText = moduleAlias.isEmpty() ? ItemResolverConstants.SERVICE :
                String.join("_", Arrays.asList(ItemResolverConstants.SERVICE, moduleName));
        if (!shouldImport && !moduleAlias.equals(moduleName) && !moduleAlias.isEmpty()) {
            filterText += "_" + moduleAlias;
        }
        filterText += "_" + serviceSnippet.symbolName;
        List<TextEdit> additionalTextEdits = new ArrayList<>(importsAcceptor.getNewImportTextEdits());
        return new StaticCompletionItem(context, ServiceTemplateCompletionItemBuilder.build(snippet, label, detail,
                filterText.replace(".", "_"), additionalTextEdits), StaticCompletionItem.Kind.OTHER);

    }

    private String generateMethodSnippet(ImportsAcceptor importsAcceptor, MethodSymbol methodSymbol, int snippetIndex,
                                         BallerinaCompletionContext context) {
        String functionTypeDesc =
                FunctionGenerator.processModuleIDsInText(importsAcceptor, methodSymbol.signature(), context);
        String returnStmt = "";
        if (methodSymbol.typeDescriptor().returnTypeDescriptor().isPresent()) {
            TypeSymbol returnTypeSymbol = methodSymbol.typeDescriptor().returnTypeDescriptor().get();
            if (returnTypeSymbol.typeKind() != TypeDescKind.COMPILATION_ERROR) {
                Optional<String> defaultReturnValueForType = DefaultValueGenerationUtil
                        .getDefaultPlaceholderForType(returnTypeSymbol);
                if (defaultReturnValueForType.isPresent()) {
                    String defaultReturnValue = defaultReturnValueForType.get();
                    if (CommonKeys.PARANTHESES_KEY.equals(defaultReturnValue)) {
                        returnStmt = "return;";
                    } else {
                        returnStmt = "return ${" + snippetIndex + ":" +
                                defaultReturnValue + "}" + CommonKeys.SEMI_COLON_SYMBOL_KEY;
                    }
                }
            }
        }

        //Build the snippet
        String paddingStr = StringUtils.repeat(" ", 4);
        StringBuilder functionSnippet = new StringBuilder();
        functionSnippet.append(CommonUtil.LINE_SEPARATOR)
                .append(paddingStr)
                .append(functionTypeDesc)
                .append(" ")
                .append(CommonKeys.OPEN_BRACE_KEY)
                .append(CommonUtil.LINE_SEPARATOR)
                .append(StringUtils.repeat(paddingStr, 2))
                .append((returnStmt.isEmpty() ? "${" + snippetIndex + "}" : returnStmt))
                .append(CommonUtil.LINE_SEPARATOR)
                .append(paddingStr)
                .append(CommonKeys.CLOSE_BRACE_KEY)
                .append(CommonUtil.LINE_SEPARATOR);
        return functionSnippet.toString();
    }

    /**
     * Holds data related to a particular listener symbol.
     */
    private static class ListenerMetaData {

        private final String listenerInitArgs;
        private final List<MethodSymbol> unimplementedMethods;
        private final String symbolName;
        private final int currentSnippetIndex;
        private final ModuleID moduleID;

        ListenerMetaData(String listenerInitialization,
                         List<MethodSymbol> unimplementedMethods,
                         String symbolReference,
                         int currentSnippetIndex,
                         ModuleID moduleID) {
            this.listenerInitArgs = listenerInitialization;
            this.unimplementedMethods = unimplementedMethods;
            this.symbolName = symbolReference;
            this.currentSnippetIndex = currentSnippetIndex;
            this.moduleID = moduleID;
        }
    }
}

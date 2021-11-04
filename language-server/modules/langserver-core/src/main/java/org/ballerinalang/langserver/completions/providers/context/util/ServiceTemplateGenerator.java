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
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import io.ballerina.projects.directory.ProjectLoader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.LSPackageLoader;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.common.utils.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.common.utils.SymbolUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.StaticCompletionItem;
import org.ballerinalang.langserver.completions.builder.ServiceTemplateCompletionItemBuilder;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    private Map<Pair<String, String>, List<ListenerMetaData>> moduleListenerMetaDataMap;

    private boolean isInitialized;

    public boolean initialized() {
        return isInitialized;
    }

    private ServiceTemplateGenerator(LanguageServerContext context) {
        context.put(SERVICE_TEMPLATE_GENERATOR_KEY, this);
        this.moduleListenerMetaDataMap = new ConcurrentHashMap<>();
        CompletableFuture.runAsync(() -> initialize(context));
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

    /**
     * Initializes the Service Template Generator.
     *
     * @param context Language Server Context.
     */
    private void initialize(LanguageServerContext context) {
        if (!this.isInitialized) {
            loadListenersFromDistribution(context);
            this.isInitialized = true;
        }
    }

    /**
     * Given a module symbol, find and populate service metadata into the moduleServiceTemplateMap cache.
     * Used to dynamically add new entries to the cache.
     *
     * @param moduleSymbol Module symbol.
     * @param orgName      Organization name of the module symbol.
     * @param moduleName   Module name of the module symbol.
     * @param modulePrefix Module Prefix of the modules symbol.
     * @param shouldImport Whether import text should be added.
     * @param ctx          BallerinaCompletion context.
     * @return {@link List<LSCompletionItem>} Set of completion items corresponding to the listeners
     * in the given module.
     */
    public synchronized List<LSCompletionItem> generateAndPopulate(ModuleSymbol moduleSymbol,
                                                                   String orgName,
                                                                   String moduleName,
                                                                   String modulePrefix,
                                                                   Boolean shouldImport,
                                                                   BallerinaCompletionContext ctx) {
        Pair<String, String> moduleKey = Pair.of(moduleName, orgName);
        List<ListenerMetaData> items = new ArrayList<>();
        moduleSymbol.allSymbols().stream().filter(listenerPredicate())
                .forEach(listener -> generateServiceSnippetMetaData(listener,
                        orgName, moduleName, modulePrefix).ifPresent(items::add));
        if (!moduleListenerMetaDataMap.containsKey(moduleKey) && !items.isEmpty()) {
            moduleListenerMetaDataMap.put(moduleKey, items);
        }
        return items.stream().map(item ->
                generateServiceSnippet(item, shouldImport, ctx)).collect(Collectors.toList());
    }

    /**
     * Generates and returns the service templates for a given completion context.
     *
     * @param ctx Completion context.
     * @return {@link List<LSCompletionItem>} List of completion items.
     */
    public List<LSCompletionItem> getServiceTemplates(BallerinaCompletionContext ctx) {
        List<LSCompletionItem> completionItems = new ArrayList<>();
        Set<String> processedModuleList = new HashSet<>();

        //Find listeners from current imports and generate completion items.
        Map<ImportDeclarationNode, ModuleSymbol> currentDocImports = ctx.currentDocImportsMap();
        currentDocImports.forEach((importNode, moduleSymbol) -> {
            String orgName = importNode.orgName().isEmpty() ? "" : importNode.orgName().get().orgName().text();
            String moduleName = importNode.moduleName().stream()
                    .map(Token::text)
                    .collect(Collectors.joining("."));
            String modulePrefix;
            if (importNode.prefix().isEmpty()) {
                modulePrefix = CommonUtil.escapeReservedKeyword(
                        importNode.moduleName().get(importNode.moduleName().size() - 1).text());
            } else {
                modulePrefix = CommonUtil.escapeReservedKeyword(importNode.prefix().get().prefix().text());
            }
            String moduleHash = generateModuleHash(orgName, moduleName);
            if (processedModuleList.contains(moduleHash)) {
                return;
            }

            Pair<String, String> key = Pair.of(moduleName, orgName);
            //check if the module has already been processed to the cache.
            if (this.moduleListenerMetaDataMap.containsKey(key)) {
                moduleListenerMetaDataMap.get(key).forEach(item ->
                        completionItems.add(generateServiceSnippet(item, false, ctx)));
                processedModuleList.add(moduleHash);
                return;
            }

            //Check if the module belongs to the current project. 
            //If it is not from the current project populate to the cache.
            if (!getModuleNamesOfCurrentProject(ctx).contains(moduleHash)) {
                completionItems.addAll(generateAndPopulate(moduleSymbol, orgName, moduleName, modulePrefix,
                        false, ctx));
                processedModuleList.add(moduleHash);
                return;
            }

            moduleSymbol.allSymbols().stream().filter(listenerPredicate())
                    .forEach(listener -> generateServiceSnippetMetaData(listener, orgName, moduleName, modulePrefix)
                            .ifPresent(item -> completionItems.add(generateServiceSnippet(item, false, ctx))));
            processedModuleList.add(moduleHash);
        });

        //Generate service templates for listeners from the distribution
        if (this.isInitialized) {
            this.moduleListenerMetaDataMap.forEach((key, items) -> {
                String moduleName = key.getLeft();
                String orgName = key.getRight();
                String modulePrefix = getModulePrefix(moduleName, ctx);
                String moduleHash = generateModuleHash(orgName, moduleName);
                if (processedModuleList.contains(moduleHash)) {
                    return;
                }
                items.forEach(item -> {
                    if (!item.modulePrefix.equals(modulePrefix)) {
                        item.modulePrefix = modulePrefix;
                    }
                    completionItems.add(generateServiceSnippet(item, true, ctx));
                });
                processedModuleList.add(moduleHash);
            });
        }

        //Generate completion items for the listeners in the current project.
        Optional<Project> project = ctx.workspace().project(ctx.filePath());
        Optional<Module> currentModule = ctx.workspace().module(ctx.filePath());
        if (project.isEmpty() || currentModule.isEmpty()) {
            return completionItems;
        }
        boolean isDefaultModule = currentModule.get().isDefaultModule();
        PackageCompilation packageCompilation = project.get().currentPackage().getCompilation();
        project.get().currentPackage().modules().forEach(module -> {
            //Symbols in the default module should not be visible to other modules.
            if (module.isDefaultModule() && !isDefaultModule) {
                return;
            }
            String orgName = "";
            String moduleName = module.moduleName().toString();
            String moduleHash = generateModuleHash(orgName, moduleName);
            if (processedModuleList.contains(moduleHash)) {
                return;
            }
            boolean shouldImport = !(project.get().kind() == ProjectKind.SINGLE_FILE_PROJECT
                    || currentModule.get().equals(module));
            String modulePrefix = shouldImport ? getModulePrefix(moduleName, ctx) : "";
            SemanticModel semanticModel = packageCompilation.getSemanticModel(module.moduleId());
            semanticModel.moduleSymbols().stream().filter(listenerPredicate())
                    .forEach(listener -> generateServiceSnippetMetaData(listener, orgName, moduleName, modulePrefix)
                            .ifPresent(item -> completionItems.add(generateServiceSnippet(item, shouldImport, ctx))));
        });
        return completionItems;
    }

    /**
     * Load projects from the distribution repo and generate service data holder.
     *
     * @param lsContext Language Server Context.
     */
    private void loadListenersFromDistribution(LanguageServerContext lsContext) {
        List<Package> packages = LSPackageLoader.getInstance(lsContext).getDistributionRepoPackages();
        packages.forEach(distPackage -> {
            String orgName = CommonUtil.escapeModuleName(distPackage.packageOrg().value());
            Project project = ProjectLoader.loadProject(distPackage.project().sourceRoot());
            PackageCompilation packageCompilation = project.currentPackage().getCompilation();
            project.currentPackage().modules().forEach(module -> {
                Pair<String, String> moduleKey = Pair.of(module.moduleName().toString(), orgName);
                String modulePrefix = getModulePrefix(module.moduleName().toString());
                SemanticModel semanticModel = packageCompilation.getSemanticModel(module.moduleId());
                List<ListenerMetaData> items = new ArrayList<>();
                semanticModel.moduleSymbols().stream().filter(listenerPredicate())
                        .forEach(listener ->
                                generateServiceSnippetMetaData(listener, orgName, module.moduleName().toString(),
                                        modulePrefix).ifPresent(items::add));
                if (!items.isEmpty() && !this.moduleListenerMetaDataMap.containsKey(moduleKey)) {
                    this.moduleListenerMetaDataMap.put(moduleKey, items);
                }
            });
        });
    }

    private Predicate<Symbol> listenerPredicate() {
        return symbol -> SymbolUtil.isListener(symbol) && symbol.kind() == SymbolKind.CLASS;
    }

    private String generateModuleHash(String orgName, String moduleName) {
        return orgName.isEmpty() ? moduleName : orgName + CommonKeys.SLASH_KEYWORD_KEY + moduleName;
    }

    private String getModulePrefix(String moduleName, CompletionContext context) {
        List<String> moduleNameComponents = Arrays.stream(moduleName.split("\\."))
                .map(CommonUtil::escapeModuleName)
                .collect(Collectors.toList());
        if (moduleNameComponents.isEmpty()) {
            return "";
        }
        String aliasComponent = moduleNameComponents.get(moduleNameComponents.size() - 1);
        String validatedName = CommonUtil.getValidatedSymbolName(context, aliasComponent);
        return !validatedName.equals(aliasComponent) ? validatedName : aliasComponent;
    }

    private String getModulePrefix(String moduleName) {
        List<String> moduleNameComponents = Arrays.stream(moduleName.split("\\."))
                .map(CommonUtil::escapeModuleName)
                .collect(Collectors.toList());
        if (moduleNameComponents.isEmpty()) {
            return "";
        }
        return moduleNameComponents.get(moduleNameComponents.size() - 1);
    }

    private Set<String> getModuleNamesOfCurrentProject(BallerinaCompletionContext ctx) {
        Set<String> modulesHashSet = new HashSet<>();
        Optional<Project> project = ctx.workspace().project(ctx.filePath());
        if (project.isEmpty()) {
            return modulesHashSet;
        }
        String orgName = "";
        project.get().currentPackage().modules().forEach(module -> {
            String hash = generateModuleHash(orgName, module.moduleName().toString());
            modulesHashSet.add(hash);
        });
        return modulesHashSet;
    }

    /**
     * Given a Symbol of a listener, process and pre-generate listener meta data.
     *
     * @param symbol       Listener symbol.
     * @param orgName      Organization name of the symbol.
     * @param moduleName   Module name of the symbol.
     * @param modulePrefix Module prefix of the symbol.
     * @return {@link ListenerMetaData} Pre processed metadata of the symbol.
     */
    private Optional<ListenerMetaData> generateServiceSnippetMetaData(Symbol symbol,
                                                                      String orgName,
                                                                      String moduleName,
                                                                      String modulePrefix) {

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
        Optional<MethodSymbol> initMethod = classSymbol.initMethod();
        String listenerInitialization;
        String symbolReference;
        if (!modulePrefix.isEmpty()) {
            symbolReference = modulePrefix + ":" + classSymbol.getName().get();
        } else {
            //Referring to a symbol in the current module. 
            symbolReference = classSymbol.getName().get();
        }
        
        //Snippet index 1 is provided for attachment point
        int snippetIndex = 2;
        if (initMethod.isEmpty() || initMethod.get().typeDescriptor().params().isEmpty()) {
            listenerInitialization = "new " + symbolReference + "()";
        } else {
            List<String> args = new ArrayList<>();
            List<ParameterSymbol> requiredParams = initMethod.get().typeDescriptor().params().get().stream()
                    .filter(parameterSymbol ->
                            parameterSymbol.paramKind() == ParameterKind.REQUIRED).collect(Collectors.toList());
            for (ParameterSymbol parameterSymbol : requiredParams) {
                args.add("${" + snippetIndex + ":" +
                        CommonUtil.getDefaultValueForType(parameterSymbol.typeDescriptor()).orElse("") + "}");
                snippetIndex += 1;
            }
            listenerInitialization = "new " + symbolReference
                    + "(" + String.join(",", args) + ")";
        }

        return Optional.of(new ListenerMetaData(listenerInitialization,
                new ArrayList<>(serviceTypeSymbol.methods().values()),
                symbolReference, snippetIndex, orgName, moduleName, modulePrefix));
    }

    private LSCompletionItem generateServiceSnippet(ListenerMetaData serviceSnippet,
                                                    Boolean shouldImport,
                                                    BallerinaCompletionContext context) {

        int snippetIndex = serviceSnippet.currentSnippetIndex;
        List<String> methodSnippets = new ArrayList<>();
        List<TextEdit> additionalTextEdits = new ArrayList<>();
        if (!serviceSnippet.unimplementedMethods.isEmpty()) {
            for (MethodSymbol methodSymbol : serviceSnippet.unimplementedMethods) {
                Pair<String, List<TextEdit>> functionSnippet =
                        generateMethodSnippet(methodSymbol, snippetIndex, context);
                additionalTextEdits.addAll(functionSnippet.getRight());
                methodSnippets.add(functionSnippet.getLeft());
                snippetIndex += 1;
            }
        }

        String snippet = SyntaxKind.SERVICE_KEYWORD.stringValue() + " ${1} " +
                SyntaxKind.ON_KEYWORD.stringValue() + " " + serviceSnippet.listenerInitialization +
                " {" + CommonUtil.LINE_SEPARATOR + (serviceSnippet.unimplementedMethods.isEmpty() ?
                "    ${" + snippetIndex + "}" : String.join("", methodSnippets)) + CommonUtil.LINE_SEPARATOR + "}" +
                CommonUtil.LINE_SEPARATOR;

        String label = "service on " + serviceSnippet.symbolReference;
        String detail = ItemResolverConstants.SNIPPET_TYPE;
        String filterText = String.join("_", Arrays.asList(ItemResolverConstants.SERVICE,
                serviceSnippet.symbolReference.replace(":", "_")));

        if (shouldImport) {
            String modulePrefix = getModulePrefix(serviceSnippet.moduleName);
            if (serviceSnippet.modulePrefix.equals(modulePrefix)) {
                modulePrefix = "";
            }
            additionalTextEdits.addAll(CommonUtil.getAutoImportTextEdits(serviceSnippet.orgName,
                    serviceSnippet.moduleName, modulePrefix, context));
        }

        return new StaticCompletionItem(context, ServiceTemplateCompletionItemBuilder.build(snippet, label, detail,
                filterText, additionalTextEdits), StaticCompletionItem.Kind.OTHER);

    }

    private Pair<String, List<TextEdit>> generateMethodSnippet(MethodSymbol methodSymbol, int snippetIndex,
                                                               BallerinaCompletionContext context) {
        ImportsAcceptor importsAcceptor = new ImportsAcceptor(context);
        String typeName = FunctionGenerator.processModuleIDsInText(importsAcceptor, methodSymbol.signature(), context);
        List<TextEdit> edits = new ArrayList<>(importsAcceptor.getNewImportTextEdits());

        String returnStmt = "";
        if (methodSymbol.typeDescriptor().returnTypeDescriptor().isPresent()) {
            TypeSymbol returnTypeSymbol = methodSymbol.typeDescriptor().returnTypeDescriptor().get();
            if (returnTypeSymbol.typeKind() != TypeDescKind.COMPILATION_ERROR) {
                Optional<String> defaultReturnValueForType = CommonUtil.getDefaultValueForType(returnTypeSymbol);
                if (defaultReturnValueForType.isPresent()) {
                    String defaultReturnValue = defaultReturnValueForType.get();
                    if (defaultReturnValue.equals(CommonKeys.PARANTHESES_KEY)) {
                        returnStmt = "return;";
                    } else {
                        returnStmt = "return ${" + snippetIndex + ":" +
                                defaultReturnValue + "}" + CommonKeys.SEMI_COLON_SYMBOL_KEY;
                    }
                }
            }
        }

        int padding = 4;
        String paddingStr = StringUtils.repeat(" ", padding);
        StringBuilder functionSnippet = new StringBuilder();
        functionSnippet.append(CommonUtil.LINE_SEPARATOR)
                .append(paddingStr)
                .append(typeName)
                .append(" ")
                .append(CommonKeys.OPEN_BRACE_KEY)
                .append(CommonUtil.LINE_SEPARATOR)
                .append(StringUtils.repeat(paddingStr, 2))
                .append((returnStmt.isEmpty() ? "${" + snippetIndex + "}" : returnStmt))
                .append(CommonUtil.LINE_SEPARATOR)
                .append(paddingStr)
                .append(CommonKeys.CLOSE_BRACE_KEY)
                .append(CommonUtil.LINE_SEPARATOR);
        return Pair.of(functionSnippet.toString(), edits);
    }

    /**
     * Holds data related to a particular listener symbol.
     */
    private static class ListenerMetaData {

        private String listenerInitialization;
        private List<MethodSymbol> unimplementedMethods;
        private String symbolReference;
        private int currentSnippetIndex;
        private String moduleName;
        private String orgName;
        private String modulePrefix;

        ListenerMetaData(String listenerInitialization,
                         List<MethodSymbol> unimplementedMethods,
                         String symbolReference,
                         int currentSnippetIndex,
                         String orgName,
                         String moduleName,
                         String modulePrefix) {
            this.listenerInitialization = listenerInitialization;
            this.unimplementedMethods = unimplementedMethods;
            this.symbolReference = symbolReference;
            this.currentSnippetIndex = currentSnippetIndex;
            this.orgName = orgName;
            this.moduleName = moduleName;
            this.modulePrefix = modulePrefix;
        }
    }
}

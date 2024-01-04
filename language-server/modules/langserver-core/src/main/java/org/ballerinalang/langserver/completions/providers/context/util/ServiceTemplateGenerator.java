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
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterKind;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.Module;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectKind;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.LSContextOperation;
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
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.SnippetContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.StaticCompletionItem;
import org.ballerinalang.langserver.completions.builder.ServiceTemplateCompletionItemBuilder;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    private boolean isInitialized = false;

    private ServiceTemplateGenerator(LanguageServerContext context) {
        context.put(SERVICE_TEMPLATE_GENERATOR_KEY, this);
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
     * Generates and returns the service templates for a given completion context.
     *
     * @param ctx Completion context.
     * @return {@link List<LSCompletionItem>} List of completion items.
     */
    public List<LSCompletionItem> getServiceTemplates(BallerinaCompletionContext ctx) {
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

            try {
                SemanticModel semanticModel = packageCompilation.get().getSemanticModel(module.moduleId());
                semanticModel.moduleSymbols().stream().filter(listenerPredicate()).forEach(listener ->
                        generateServiceSnippetMetaData(listener, moduleID).ifPresent(item ->
                                completionItems.add(generateServiceSnippet(item, ctx))));
            } catch (Throwable throwable) {
                LSClientLogger clientLogger = LSClientLogger.getInstance(ctx.languageServercontext());
                String msg = String.format("Operation 'txt/completion' failed for %s", moduleName);
                clientLogger.logError(LSContextOperation.TXT_COMPLETION, msg, throwable, null, (Position) null);
            }
        });
        List<LSPackageLoader.ModuleInfo> visibleModules =
                LSPackageLoader.getInstance(ctx.languageServercontext()).getAllVisiblePackages(ctx);
        visibleModules.forEach(moduleInfo -> {

            if (processedModuleList.contains(moduleInfo.getModuleIdentifier())
                    || moduleInfo.isModuleFromCurrentPackage()) {
                return;
            }
            moduleInfo.getListenerMetaData().forEach(listenerMetaData -> {
                completionItems.add(generateServiceSnippet(listenerMetaData, ctx));
            });
            processedModuleList.add(moduleInfo.getModuleIdentifier());
        });
        return completionItems;
    }

    private ModuleID getCurrentModuleID(CompletionContext ctx) {
        Optional<Module> currentModule = ctx.workspace().module(ctx.filePath());
        if (currentModule.isEmpty()) {
            throw new RuntimeException("Current module not found");
        }
        String currentOrg = currentModule.get().packageInstance().descriptor().org().value();
        String currentModuleName = currentModule.get().descriptor().name().toString();
        String currentVersion = currentModule.get().packageInstance().descriptor().version().value().toString();
        return CodeActionModuleId.from(currentOrg, currentModuleName, currentVersion);
    }

    public boolean initialized() {
        return this.isInitialized;
    }

    public static Predicate<Symbol> listenerPredicate() {
        return symbol -> SymbolUtil.isListener(symbol) && symbol.kind() == SymbolKind.CLASS;
    }

    private String generateModuleHash(String orgName, String moduleName) {
        return orgName.isEmpty() ? moduleName : orgName + CommonKeys.SLASH_KEYWORD_KEY + moduleName;
    }

    /**
     * Given a Symbol of a listener, process and pre-generate listener meta data.
     *
     * @param symbol   Listener symbol.
     * @param moduleID ModuleID of the module of symbol.
     * @return {@link ListenerMetaData} Pre processed metadata of the symbol.
     */
    public static Optional<ListenerMetaData> generateServiceSnippetMetaData(Symbol symbol,
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
        if (attachMethod == null) {
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
                    .stream().map(CommonUtil::getRawType)
                    .filter(member -> member.typeKind() == TypeDescKind.OBJECT).findFirst();
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

    private LSCompletionItem generateServiceSnippet(ListenerMetaData serviceSnippet,
                                                    BallerinaCompletionContext context) {

        String symbolReference;
        ImportsAcceptor importsAcceptor = new ImportsAcceptor(context);
        String modulePrefix = ModuleUtil.getModulePrefix(importsAcceptor, getCurrentModuleID(context),
                serviceSnippet.moduleID, context);
        Boolean shouldImport = importsAcceptor.getNewImports().size() > 0;
        String moduleAlias = modulePrefix.replace(":", "");
        String moduleName = ModuleUtil.escapeModuleName(serviceSnippet.moduleID.moduleName());

        if (!moduleAlias.isEmpty()) {
            symbolReference = modulePrefix + serviceSnippet.symbolName;
        } else {
            symbolReference = serviceSnippet.symbolName;
        }

        String listenerInitialization = "new " + symbolReference + "(" + serviceSnippet.listenerInitArgs + ")";
        List<String> methodSnippets = new ArrayList<>();

        SnippetContext snippetContext = new SnippetContext(serviceSnippet.currentSnippetIndex - 1);
        
        if (!serviceSnippet.unimplementedMethods.isEmpty()) {
            for (MethodSymbol methodSymbol : serviceSnippet.unimplementedMethods) {
                String functionSnippet =
                        generateMethodSnippet(importsAcceptor, methodSymbol, snippetContext, context);
                methodSnippets.add(functionSnippet);
            }
        }


        String snippet = SyntaxKind.SERVICE_KEYWORD.stringValue() + " ${1} " +
                SyntaxKind.ON_KEYWORD.stringValue() + " " + listenerInitialization +
                " {" + CommonUtil.LINE_SEPARATOR + (serviceSnippet.unimplementedMethods.isEmpty() ?
                "    ${" + snippetContext.incrementAndGetPlaceholderCount()
                        + "}" : String.join("", methodSnippets)) + CommonUtil.LINE_SEPARATOR + "}" +
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
                filterText.replace(".", "_"), additionalTextEdits), StaticCompletionItem.Kind.SERVICE_TEMPLATE);

    }

    private String generateMethodSnippet(ImportsAcceptor importsAcceptor, MethodSymbol methodSymbol, 
                                         SnippetContext snippetContext,
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
                        returnStmt = "";
                    } else {
                        returnStmt = "return ${" + snippetContext.incrementAndGetPlaceholderCount() + ":" +
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
                .append((returnStmt.isEmpty() ? "${" +
                        snippetContext.incrementAndGetPlaceholderCount() + "}" : returnStmt))
                .append(CommonUtil.LINE_SEPARATOR)
                .append(paddingStr)
                .append(CommonKeys.CLOSE_BRACE_KEY)
                .append(CommonUtil.LINE_SEPARATOR);
        return functionSnippet.toString();
    }

    /**
     * Holds data related to a particular listener symbol.
     */
    public static class ListenerMetaData {

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

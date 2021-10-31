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
import io.ballerina.projects.ModuleName;
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
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.StaticCompletionItem;
import org.ballerinalang.langserver.completions.builder.ServiceTemplateCompletionItemBuilder;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Generates Service Template completion items.
 *
 * @since 2.0.0
 */
public class ServiceTemplateGenerator {

    private static final LanguageServerContext.Key<ServiceTemplateGenerator> SERVICE_TEMPLATE_GENERATOR_KEY =
            new LanguageServerContext.Key<>();

    private Map<Pair<ModuleName, String>, List<Symbol>> moduleListenerMap;
    boolean isInitialized;

    private ServiceTemplateGenerator(LanguageServerContext context) {
        context.put(SERVICE_TEMPLATE_GENERATOR_KEY, this);
        this.moduleListenerMap = new HashMap<>();
        isInitialized = false;
    }

    /**
     * Get a singleton instance of Service Template Generator.
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
    public void initialize(LanguageServerContext context) {
        if (!this.isInitialized) {
            loadListenersFromDistribution(context);
            this.isInitialized = true;
        }
    }

    /**
     * generates and returns the service templates for a given completion context.
     *
     * @param ctx Completion context.
     * @return {@link List<LSCompletionItem>} list of completion items.
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
            String alias;
            if (importNode.prefix().isEmpty()) {
                alias = CommonUtil.escapeReservedKeyword(
                        importNode.moduleName().get(importNode.moduleName().size() - 1).text());
            } else {
                alias = CommonUtil.escapeReservedKeyword(importNode.prefix().get().prefix().text());
            }
            String moduleHash = generateModuleHash(orgName, moduleName);
            if (processedModuleList.contains(moduleHash)) {
                return;
            }
            moduleSymbol.allSymbols().stream().filter(listenerPredicate())
                    .forEach(listener -> generateServiceTemplateCompletionItem(listener,
                            orgName, moduleName, alias, false, ctx)
                            .ifPresent(completionItems::add));
            processedModuleList.add(moduleHash);
        });

        //Generate listeners from the distribution
        this.moduleListenerMap.entrySet().forEach(entry -> {
            ModuleName moduleName = entry.getKey().getLeft();
            String orgName = entry.getKey().getRight();
            List<String> moduleNameComponents = Arrays.stream(moduleName.toString().split("\\."))
                    .map(CommonUtil::escapeModuleName)
                    .collect(Collectors.toList());
            String aliasComponent = moduleNameComponents.get(moduleNameComponents.size() - 1);
            String validatedName = CommonUtil.getValidatedSymbolName(ctx, aliasComponent);
            String alias = !validatedName.equals(aliasComponent) ? validatedName : "";
            String moduleHash = generateModuleHash(orgName, moduleName.toString());
            if (processedModuleList.contains(moduleHash)) {
                return;
            }
            List<Symbol> symbolsInModule = entry.getValue();
            symbolsInModule.forEach(symbol -> generateServiceTemplateCompletionItem(symbol,
                    orgName, moduleName.toString(), alias, true, ctx)
                    .ifPresent(completionItems::add));
            processedModuleList.add(moduleHash);
        });

        //Find listeners from the current project and generate completion items
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
            String alias;
            if (shouldImport) {
                List<String> moduleNameComponents = Arrays.stream(moduleName.split("\\."))
                        .map(CommonUtil::escapeModuleName)
                        .collect(Collectors.toList());
                String aliasComponent = moduleNameComponents.get(moduleNameComponents.size() - 1);
                String validatedName = CommonUtil.getValidatedSymbolName(ctx, aliasComponent);
                alias = !validatedName.equals(aliasComponent) ? validatedName : "";
            } else {
                alias = "";
            }
            SemanticModel semanticModel = packageCompilation.getSemanticModel(module.moduleId());
            semanticModel.moduleSymbols().stream().filter(listenerPredicate())
                    .forEach(listener -> generateServiceTemplateCompletionItem(listener,
                            orgName, moduleName, alias, shouldImport, ctx)
                            .ifPresent(completionItems::add));
        });
        return completionItems;
    }

    private void loadListenersFromDistribution(LanguageServerContext lsContext) {
        List<Package> packages = LSPackageLoader.getInstance(lsContext).getDistributionRepoPackages();
        packages.forEach(distPackage -> {
            String orgName = CommonUtil.escapeModuleName(distPackage.packageOrg().value());
            Project project = ProjectLoader.loadProject(distPackage.project().sourceRoot());
            PackageCompilation packageCompilation = project.currentPackage().getCompilation();
            project.currentPackage().modules().forEach(module -> {
                Pair<ModuleName, String> moduleKey = Pair.of(module.moduleName(), orgName);
                this.moduleListenerMap.put(moduleKey, new ArrayList<>());
                SemanticModel semanticModel = packageCompilation.getSemanticModel(module.moduleId());
                moduleListenerMap.get(moduleKey)
                        .addAll(semanticModel.moduleSymbols().stream().filter(listenerPredicate())
                                .collect(Collectors.toList()));
            });
        });
    }

    private Predicate<Symbol> listenerPredicate() {
        return symbol -> SymbolUtil.isListener(symbol) && symbol.kind() == SymbolKind.CLASS;
    }

    private String generateModuleHash(String orgName, String moduleName) {
        return orgName.isEmpty() ? moduleName : orgName + CommonKeys.SLASH_KEYWORD_KEY + moduleName;
    }

    private Optional<LSCompletionItem> generateServiceTemplateCompletionItem(Symbol symbol, String orgName,
                                                                             String moduleName,
                                                                             String alias,
                                                                             Boolean addImportText,
                                                                             BallerinaCompletionContext context) {
        //Check if the provided symbol is a listener.
        Optional<? extends TypeSymbol> symbolTypeDesc = SymbolUtil.getTypeDescriptor(symbol);
        if (symbolTypeDesc.isEmpty() || !SymbolUtil.isListener(symbol) || symbol.kind() != SymbolKind.CLASS) {
            return Optional.empty();
        }
        ClassSymbol classSymbol = (ClassSymbol) CommonUtil.getRawType(symbolTypeDesc.get());

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

        //Listener initialization
        Optional<MethodSymbol> initMethod = classSymbol.initMethod();
        String listenerInitialization;
        String symbolReference;
        if (!alias.isEmpty()) {
            symbolReference = alias + ":" + classSymbol.getName().get();
        } else {
            List<String> moduleNameComponents = Arrays.stream(moduleName.split("\\."))
                    .map(CommonUtil::escapeModuleName)
                    .collect(Collectors.toList());
            if (!moduleNameComponents.isEmpty()) {
                symbolReference = moduleNameComponents.get(moduleNameComponents.size() - 1)
                        + ":" + classSymbol.getName().get();
            } else {
                symbolReference = classSymbol.getName().get();
            }
        }
        int snippetIndex = 2;
        if (initMethod.isEmpty() || initMethod.get().typeDescriptor().params().isEmpty()) {
            listenerInitialization = "new " + symbolReference + "()";
        } else {
            //check for rest params
            List<String> args = new ArrayList<>();
            for (ParameterSymbol parameterSymbol : initMethod.get().typeDescriptor().params().get()) {
                args.add("${" + snippetIndex + ":" +
                        CommonUtil.getDefaultValueForType(parameterSymbol.typeDescriptor()).orElse("") + "}");
                snippetIndex += 1;
            }
            listenerInitialization = "new " + symbolReference
                    + "(" + args.stream().collect(Collectors.joining(",")) + ")";
        }

        List<String> methodSnippets = new ArrayList<>();
        List<TextEdit> additionalTextEdits = new ArrayList<>();
        if (!serviceTypeSymbol.methods().isEmpty()) {
            for (MethodSymbol methodSymbol : serviceTypeSymbol.methods().values()) {
                Pair<String, List<TextEdit>> functionSnippet =
                        generateFunctionSnippet(methodSymbol, snippetIndex, context);
                additionalTextEdits.addAll(functionSnippet.getRight());
                methodSnippets.add(functionSnippet.getLeft());
                snippetIndex += 1;
            }
        }

        //Service template snippet.
        String snippet = SyntaxKind.SERVICE_KEYWORD.stringValue() + " /${1} " +
                SyntaxKind.ON_KEYWORD.stringValue() + " " + listenerInitialization +
                " {" + CommonUtil.LINE_SEPARATOR + (methodSnippets.isEmpty() ? "    ${" + snippetIndex + "}" :
                methodSnippets.stream().collect(Collectors.joining(""))) + CommonUtil.LINE_SEPARATOR + "}" +
                CommonUtil.LINE_SEPARATOR;

        String label = "service / on " + symbolReference;
        String detail = "service template";
        if (addImportText) {
            additionalTextEdits.addAll(CommonUtil.getAutoImportTextEdits(orgName, moduleName, alias, context));
        }
        return Optional.of(new StaticCompletionItem(context,
                ServiceTemplateCompletionItemBuilder.build(snippet, label, detail,
                        additionalTextEdits), StaticCompletionItem.Kind.OTHER));
    }

    private Pair<String, List<TextEdit>> generateFunctionSnippet(MethodSymbol methodSymbol, int snippetIndex,
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

}

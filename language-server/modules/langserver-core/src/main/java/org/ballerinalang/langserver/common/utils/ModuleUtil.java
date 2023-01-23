/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.common.utils;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportPrefixNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import org.ballerinalang.langserver.LSPackageLoader;
import org.ballerinalang.langserver.codeaction.CodeActionModuleId;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.PositionedOperationContext;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.SymbolKind.MODULE;

/**
 * Carries a set of utilities used for operations on modules.
 *
 * @since 2201.1.1
 */
public class ModuleUtil {
    
    /**
     * Filter a type in the module by the name.
     *
     * @param context  language server operation context
     * @param alias    module alias
     * @param typeName type name to be filtered against
     * @return {@link Optional} type found
     */
    public static Optional<TypeSymbol> getTypeFromModule(BallerinaCompletionContext context, String alias,
                                                         String typeName) {
        Optional<ModuleSymbol> module = ModuleUtil.searchModuleForAlias(context, alias);
        if (module.isEmpty()) {
            return Optional.empty();
        }

        ModuleSymbol moduleSymbol = module.get();
        for (TypeDefinitionSymbol typeDefinitionSymbol : moduleSymbol.typeDefinitions()) {
            if (typeDefinitionSymbol.getName().isPresent() && typeDefinitionSymbol.getName().get().equals(typeName)) {
                return Optional.of(typeDefinitionSymbol.typeDescriptor());
            }
        }

        for (ClassSymbol clazz : moduleSymbol.classes()) {
            if (clazz.getName().isPresent() && clazz.getName().get().equals(typeName)) {
                return Optional.of(clazz);
            }
        }

        return Optional.empty();
    }

    /**
     * Get the module symbol associated with the given alias.
     *
     * @param context Language server operation context
     * @param alias   alias value
     * @return {@link Optional} scope entry for the module symbol
     */
    public static Optional<ModuleSymbol> searchModuleForAlias(PositionedOperationContext context, String alias) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getCursorPosition());
        for (Symbol symbol : visibleSymbols) {
            if (symbol.kind() == MODULE && Objects.equals(symbol.getName().orElse(null), alias)) {
                return Optional.of((ModuleSymbol) symbol);
            }
        }

        return Optional.empty();
    }

    /**
     * Returns module prefix and process imports required.
     *
     * @param importsAcceptor import acceptor
     * @param currentModuleId current module id
     * @param moduleID        module id
     * @param context         {@link DocumentServiceContext}
     * @return module prefix
     */
    public static String getModulePrefix(ImportsAcceptor importsAcceptor, ModuleID currentModuleId,
                                         ModuleID moduleID, DocumentServiceContext context) {
        String pkgPrefix = "";
        if (!moduleID.equals(currentModuleId)) {
            boolean preDeclaredLangLib = moduleID.orgName().equals(CommonUtil.BALLERINA_ORG_NAME) &&
                    CommonUtil.PRE_DECLARED_LANG_LIBS.contains(moduleID.moduleName());
            String escapeModuleName = escapeModuleName(moduleID.orgName() + "/" + moduleID.moduleName());
            String[] moduleParts = escapeModuleName.split("/");
            String orgName = moduleParts[0];
            String moduleName = moduleParts[1];

            pkgPrefix = moduleName.replaceAll(".*\\.", "");
            pkgPrefix = (!preDeclaredLangLib && CommonUtil.BALLERINA_KEYWORDS.contains(pkgPrefix)) ? "'" 
                    + pkgPrefix : pkgPrefix;

            // See if an alias (ex: import project.module1 as mod1) is used
            List<ImportDeclarationNode> existingModuleImports = context.currentDocImportsMap().keySet().stream()
                    .filter(importDeclarationNode ->
                            CodeActionModuleId.from(importDeclarationNode).moduleName().equals(moduleID.moduleName()))
                    .collect(Collectors.toList());

            if (existingModuleImports.size() == 1) {
                ImportDeclarationNode importDeclarationNode = existingModuleImports.get(0);
                if (importDeclarationNode.prefix().isPresent()) {
                    pkgPrefix = importDeclarationNode.prefix().get().prefix().text();
                }
            } else if (existingModuleImports.isEmpty() && context instanceof PositionedOperationContext) {
                pkgPrefix = NameUtil.getValidatedSymbolName((PositionedOperationContext) context, pkgPrefix);
            }
            CodeActionModuleId codeActionModuleId =
                    CodeActionModuleId.from(orgName, moduleName, pkgPrefix, moduleID.version());
            if (importsAcceptor != null && !preDeclaredLangLib) {
                importsAcceptor.getAcceptor(context).accept(orgName, codeActionModuleId);
            }
            return pkgPrefix + ":";
        }
        return pkgPrefix;
    }

    /**
     * Returns module prefix.
     *
     * @param context         {@link DocumentServiceContext}
     * @param orgName organization name component
     * @param modName module name component
     * @return module prefix
     */
    public static String getModulePrefix(DocumentServiceContext context, String orgName, String modName) {
        Project project = context.workspace().project(context.filePath()).orElseThrow();
        String currentProjectOrg = project.currentPackage().packageOrg().value();
        boolean isCurrentOrg = currentProjectOrg.equals(orgName);
        Optional<Module> currentModule = context.currentModule();
        String evalOrgName = isCurrentOrg ? "" : orgName;
        Optional<ImportDeclarationNode> matchedImport = matchingImportedModule(context, evalOrgName, modName);

        if (currentModule.isPresent() && modName.equals(getQualifiedModuleName(currentModule.get()))) {
            // If the module name is same as the current module, then return empty
            return "";
        }
        if (matchedImport.isPresent()) {
            Optional<ImportPrefixNode> prefix = matchedImport.get().prefix();
            if (prefix.isPresent()) {
                return prefix.get().prefix().text();
            }
            SeparatedNodeList<IdentifierToken> moduleComponents = matchedImport.get().moduleName();
            return moduleComponents.get(moduleComponents.size() - 1).text();
        }

        String[] modNameComponents = modName.split("\\.");
        return modNameComponents[modNameComponents.length - 1];
    }

    /**
     * Returns escaped module name.
     *
     * @param qualifiedModuleName Qualified module name
     * @return escaped module name
     */
    public static String escapeModuleName(String qualifiedModuleName) {
        String[] moduleNameParts = qualifiedModuleName.split("/");
        if (moduleNameParts.length > 1) {
            String orgName = moduleNameParts[0];
            String alias = moduleNameParts[1];
            String[] aliasParts = moduleNameParts[1].split("\\.");
            boolean preDeclaredLangLib = CommonUtil.BALLERINA_ORG_NAME.equals(orgName) 
                    && CommonUtil.PRE_DECLARED_LANG_LIBS.contains(alias);
            if (aliasParts.length > 1) {
                String aliasLastPart = aliasParts[aliasParts.length - 1];
                if (CommonUtil.BALLERINA_KEYWORDS.contains(aliasLastPart) && !preDeclaredLangLib) {
                    aliasLastPart = "'" + aliasLastPart;
                }
                String aliasPart = Arrays.stream(aliasParts, 0, aliasParts.length - 1)
                        .collect(Collectors.joining("."));
                alias = aliasPart + "." + aliasLastPart;
            } else {
                if (CommonUtil.BALLERINA_KEYWORDS.contains(alias) && !preDeclaredLangLib) {
                    alias = "'" + alias;
                }
            }
            return orgName + "/" + alias;
        }
        return qualifiedModuleName;
    }

    /**
     * Whether the package is already imported in the current document.
     *
     * @param context    completion context
     * @param module     Module to be evaluated against
     * @return {@link Optional}
     */
    public static Optional<ImportDeclarationNode> matchingImportedModule(CompletionContext context,
                                                                         LSPackageLoader.ModuleInfo module) {
        String name = module.packageName().value();
        String orgName = module.packageOrg().value();
        Map<ImportDeclarationNode, ModuleSymbol> currentDocImports = context.currentDocImportsMap();
        return currentDocImports.keySet().stream()
                .filter(importPkg -> importPkg.orgName().isPresent()
                        && importPkg.orgName().get().orgName().text().equals(orgName)
                        && ModuleUtil.getPackageNameComponentsCombined(importPkg).equals(name))
                .findFirst();
    }

    /**
     * Whether the package is already imported in the current document.
     *
     * @param context service operation context
     * @param orgName organization name
     * @param modName module name
     * @return {@link Optional}
     */
    public static Optional<ImportDeclarationNode> matchingImportedModule(DocumentServiceContext context, String orgName,
                                                                         String modName) {
        Map<ImportDeclarationNode, ModuleSymbol> currentDocImports = context.currentDocImportsMap();
        return currentDocImports.keySet().stream()
                .filter(importPkg -> (importPkg.orgName().isEmpty()
                        || importPkg.orgName().get().orgName().text().equals(orgName))
                        && ModuleUtil.getPackageNameComponentsCombined(importPkg).equals(modName))
                .findFirst();
    }

    /**
     * Get the package name components combined.
     *
     * @param importNode {@link ImportDeclarationNode}
     * @return {@link String}   Combined package name
     */
    public static String getPackageNameComponentsCombined(ImportDeclarationNode importNode) {
        return importNode.moduleName().stream()
                .map(Token::text)
                .collect(Collectors.joining("."));
    }

    private static String getQualifiedModuleName(Module module) {
        if (module.isDefaultModule()) {
            return module.moduleName().packageName().value();
        }
        return module.moduleName().packageName().value() + Names.DOT.getValue() + module.moduleName().moduleNamePart();
    }
}

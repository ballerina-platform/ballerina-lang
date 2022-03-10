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
package org.ballerinalang.debugadapter.completion.util;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ExplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.IdentifierToken;
import io.ballerina.compiler.syntax.tree.ImplicitNewExpressionNode;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportPrefixNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.projects.Document;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import org.ballerinalang.debugadapter.completion.context.CompletionContext;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.ballerina.compiler.api.symbols.SymbolKind.MODULE;

/**
 * Common utils to be reused in debug completion implementation.
 *
 * @since 2201.1.0
 */
public class CommonUtil {

    private static final Pattern TYPE_NAME_DECOMPOSE_PATTERN = Pattern.compile("([\\w_.]*)/([\\w._]*):([\\w.-]*)");

    private CommonUtil() {
    }

    /**
     * Get the module symbol associated with the given alias.
     *
     * @param context Debug completion context
     * @param alias   alias value
     * @return {@link Optional} scope entry for the module symbol
     */
    public static Optional<ModuleSymbol> searchModuleForAlias(CompletionContext context, String alias) {
        List<Symbol> visibleSymbols = context.visibleSymbols(context.getSuspendedContext().getLineNumber() - 1, 0);
        for (Symbol symbol : visibleSymbols) {
            if (symbol.kind() == MODULE && Objects.equals(symbol.getName().orElse(null), alias)) {
                return Optional.of((ModuleSymbol) symbol);
            }
        }

        return Optional.empty();
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

    /**
     * Whether the given module is a langlib module.
     * public static String generateParameterName(String arg, Set<String> visibleNames) {
     * visibleNames.addAll(BALLERINA_KEYWORDS);
     * String newName = arg.replaceAll(".+[\\:\\.]", "");
     * <p>
     * <p>
     * }
     *
     * @param moduleID Module ID to evaluate
     * @return {@link Boolean} whether langlib or not
     */
    public static boolean isLangLib(ModuleID moduleID) {
        return isLangLib(moduleID.orgName(), moduleID.moduleName());
    }

    public static boolean isLangLib(String orgName, String moduleName) {
        return orgName.equals("ballerina") && moduleName.startsWith("lang.");
    }

    /**
     * Get the raw type of the type descriptor. If the type descriptor is a type reference then return the associated
     * type descriptor.
     *
     * @param typeDescriptor type descriptor to evaluate
     * @return {@link TypeSymbol} extracted type descriptor
     */
    public static TypeSymbol getRawType(TypeSymbol typeDescriptor) {
        if (typeDescriptor.typeKind() == TypeDescKind.INTERSECTION) {
            return getRawType(((IntersectionTypeSymbol) typeDescriptor).effectiveTypeDescriptor());
        }
        if (typeDescriptor.typeKind() == TypeDescKind.TYPE_REFERENCE) {
            TypeReferenceTypeSymbol typeRef = (TypeReferenceTypeSymbol) typeDescriptor;
            if (typeRef.typeDescriptor().typeKind() == TypeDescKind.INTERSECTION) {
                return getRawType(((IntersectionTypeSymbol) typeRef.typeDescriptor()).effectiveTypeDescriptor());
            }
            return typeRef.typeDescriptor();
        }
        return typeDescriptor;
    }

    /**
     * Whether the package is already imported in the current document.
     *
     * @param context debug completion context
     * @param orgName organization name
     * @param modName module name
     * @return {@link Optional}
     */
    public static Optional<ImportDeclarationNode> matchingImportedModule(CompletionContext context, String orgName,
                                                                         String modName) {
        Map<ImportDeclarationNode, ModuleSymbol> currentDocImports = currentDocImportsMap(context);
        return currentDocImports.keySet().stream()
                .filter(importPkg -> (importPkg.orgName().isEmpty()
                        || importPkg.orgName().get().orgName().text().equals(orgName))
                        && CommonUtil.getPackageNameComponentsCombined(importPkg).equals(modName))
                .findFirst();
    }

    /**
     * Returns the type name (derived from signature) with version infromation removed.
     *
     * @param context    Debug completion context
     * @param typeSymbol Type symbol
     * @return Signature
     */
    public static String getModifiedTypeName(CompletionContext context, TypeSymbol typeSymbol) {
        String typeSignature = typeSymbol.signature();
        return getModifiedSignature(context, typeSignature);
    }

    /**
     * Given a signature, this method will remove the version information from the signature.
     *
     * @param context   Debug completion context
     * @param signature Signature to be modified.
     * @return Modified signature
     */
    public static String getModifiedSignature(CompletionContext context, String signature) {
        Matcher matcher = TYPE_NAME_DECOMPOSE_PATTERN.matcher(signature);
        while (matcher.find()) {
            String orgName = matcher.group(1);
            String moduleName = matcher.group(2);
            String matchedString = matcher.group();
            String modulePrefix = getModulePrefix(context, orgName, moduleName);
            String replaceText = modulePrefix.isEmpty() ? matchedString + Names.VERSION_SEPARATOR : matchedString;
            signature = signature.replace(replaceText, modulePrefix);
        }

        return signature;
    }

    public static String getModulePrefix(CompletionContext context, String orgName, String modName) {
        Project project = context.getSuspendedContext().getProject();
        String currentProjectOrg = project.currentPackage().packageOrg().value();
        boolean isCurrentOrg = currentProjectOrg.equals(orgName);
        Optional<Module> currentModule = Optional.ofNullable(context.getSuspendedContext().getModule());
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

    private static String getQualifiedModuleName(Module module) {
        if (module.isDefaultModule()) {
            return module.moduleName().packageName().value();
        }
        return module.moduleName().packageName().value() + Names.DOT.getValue() + module.moduleName().moduleNamePart();
    }

    /**
     * Given the cursor position information, returns the expected ParameterSymbol
     * information corresponding to the FunctionTypeSymbol instance.
     *
     * @param functionTypeSymbol Referenced FunctionTypeSymbol
     * @param ctx                Debug completion context.
     * @param node               Function call expression node.
     * @return {@link Optional<ParameterSymbol>} Expected Parameter Symbol.
     */
    public static Optional<ParameterSymbol> resolveFunctionParameterSymbol(FunctionTypeSymbol functionTypeSymbol,
                                                                           CompletionContext ctx,
                                                                           FunctionCallExpressionNode node) {
        return resolveParameterSymbol(functionTypeSymbol, ctx, node.arguments());
    }

    /**
     * Given the cursor position information, returns the expected ParameterSymbol
     * information corresponding to the FunctionTypeSymbol instance.
     *
     * @param functionTypeSymbol Referenced FunctionTypeSymbol
     * @param ctx                Debug completion context.
     * @param node               Remote method call action node.
     * @return {@link Optional<ParameterSymbol>} Expected Parameter Symbol.
     */
    public static Optional<ParameterSymbol> resolveFunctionParameterSymbol(FunctionTypeSymbol functionTypeSymbol,
                                                                           CompletionContext ctx,
                                                                           RemoteMethodCallActionNode node) {
        return resolveParameterSymbol(functionTypeSymbol, ctx, node.arguments());
    }

    /**
     * Given the cursor position information, returns the expected ParameterSymbol
     * information corresponding to the FunctionTypeSymbol instance.
     *
     * @param functionTypeSymbol Referenced FunctionTypeSymbol
     * @param ctx                Debug completion context.
     * @param node               Method call expression node.
     * @return {@link Optional<ParameterSymbol>} Expected Parameter Symbol.
     */
    public static Optional<ParameterSymbol> resolveFunctionParameterSymbol(FunctionTypeSymbol functionTypeSymbol,
                                                                           CompletionContext ctx,
                                                                           MethodCallExpressionNode node) {
        return resolveParameterSymbol(functionTypeSymbol, ctx, node.arguments());
    }

    /**
     * Given the cursor position information, returns the expected ParameterSymbol
     * information corresponding to the FunctionTypeSymbol instance.
     *
     * @param functionTypeSymbol Referenced FunctionTypeSymbol
     * @param ctx                Debug completion context.
     * @param node               Implicit new expression node.
     * @return {@link Optional<ParameterSymbol>} Expected Parameter Symbol.
     */
    public static Optional<ParameterSymbol> resolveFunctionParameterSymbol(FunctionTypeSymbol functionTypeSymbol,
                                                                           CompletionContext ctx,
                                                                           ImplicitNewExpressionNode node) {
        Optional<ParenthesizedArgList> args = node.parenthesizedArgList();
        if (args.isEmpty()) {
            return Optional.empty();
        }
        return resolveParameterSymbol(functionTypeSymbol, ctx, args.get().arguments());
    }

    /**
     * Given the cursor position information, returns the expected ParameterSymbol
     * information corresponding to the FunctionTypeSymbol instance.
     *
     * @param functionTypeSymbol Referenced FunctionTypeSymbol
     * @param ctx                Debug completion context.
     * @param node               Explicit new expression node.
     * @return {@link Optional<ParameterSymbol>} Expected Parameter Symbol.
     */
    public static Optional<ParameterSymbol> resolveFunctionParameterSymbol(FunctionTypeSymbol functionTypeSymbol,
                                                                           CompletionContext ctx,
                                                                           ExplicitNewExpressionNode node) {
        ParenthesizedArgList args = node.parenthesizedArgList();
        return resolveParameterSymbol(functionTypeSymbol, ctx, args.arguments());
    }

    /**
     * Check if the cursor is positioned in a function call expression parameter context.
     *
     * @param ctx  Debug completion context
     * @param node FunctionCallExpressionNode
     * @return {@link Boolean} whether the cursor is in parameter context.
     */
    public static Boolean isInFunctionCallParameterContext(CompletionContext ctx,
                                                           FunctionCallExpressionNode node) {
        return isWithinParenthesis(ctx, node.openParenToken(), node.closeParenToken());
    }

    /**
     * Check if the cursor is positioned in a method call expression parameter context.
     *
     * @param ctx  Debug completion context
     * @param node MethodCallExpressionNode
     * @return {@link Boolean} whether the cursor is in parameter context.
     */
    public static Boolean isInMethodCallParameterContext(CompletionContext ctx,
                                                         MethodCallExpressionNode node) {
        return isWithinParenthesis(ctx, node.openParenToken(), node.closeParenToken());
    }

    /**
     * Check if the cursor is positioned in a method call expression parameter context.
     *
     * @param ctx  Debug completion context
     * @param node RemoteMethodCallActionNode
     * @return {@link Boolean} whether the cursor is in parameter context.
     */
    public static Boolean isInMethodCallParameterContext(CompletionContext ctx,
                                                         RemoteMethodCallActionNode node) {
        return isWithinParenthesis(ctx, node.openParenToken(), node.closeParenToken());
    }

    /**
     * Check if the cursor is positioned in a method call expression parameter context.
     *
     * @param ctx  Debug completion context
     * @param node RemoteMethodCallActionNode
     * @return {@link Boolean} whether the cursor is in parameter context.
     */
    public static Boolean isInNewExpressionParameterContext(CompletionContext ctx,
                                                            ImplicitNewExpressionNode node) {
        Optional<ParenthesizedArgList> argList = node.parenthesizedArgList();
        if (argList.isEmpty()) {
            return false;
        }
        return isWithinParenthesis(ctx, argList.get().openParenToken(), argList.get().closeParenToken());
    }

    /**
     * Check if the cursor is positioned in a method call expression parameter context.
     *
     * @param ctx  Debug completion context
     * @param node RemoteMethodCallActionNode
     * @return {@link Boolean} whether the cursor is in parameter context.
     */
    public static Boolean isInNewExpressionParameterContext(CompletionContext ctx,
                                                            ExplicitNewExpressionNode node) {
        ParenthesizedArgList argList = node.parenthesizedArgList();
        return isWithinParenthesis(ctx, argList.openParenToken(), argList.closeParenToken());
    }

    private static boolean isWithinParenthesis(CompletionContext ctx, Token openParen, Token closedParen) {
        int cursorPosition = ctx.getCursorPositionInTree();
        return (!openParen.isMissing())
                && (openParen.textRange().endOffset() <= cursorPosition)
                && (!closedParen.isMissing())
                && (cursorPosition <= closedParen.textRange().startOffset());
    }

    private static Optional<ParameterSymbol> resolveParameterSymbol(FunctionTypeSymbol functionTypeSymbol,
                                                                    CompletionContext ctx,
                                                                    SeparatedNodeList<FunctionArgumentNode> arguments) {
        int cursorPosition = ctx.getCursorPositionInTree();
        int argIndex = -1;
        for (Node child : arguments) {
            if (child.textRange().endOffset() < cursorPosition) {
                argIndex += 1;
            }
        }
        Optional<List<ParameterSymbol>> params = functionTypeSymbol.params();
        if (params.isEmpty() || params.get().size() < argIndex + 2) {
            return Optional.empty();
        }
        return Optional.of(params.get().get(argIndex + 1));
    }

    public static Map<ImportDeclarationNode, ModuleSymbol> currentDocImportsMap(CompletionContext context) {
        Map<ImportDeclarationNode, ModuleSymbol> currentDocImportsMap = new LinkedHashMap<>();
        Optional<Document> document = Optional.ofNullable(context.getSuspendedContext().getDocument());
        if (document.isEmpty()) {
            throw new RuntimeException("Cannot find a valid document");
        }
        Optional<SemanticModel> semanticModel = Optional.ofNullable(context.getSuspendedContext().getSemanticInfo());
        if (semanticModel.isEmpty()) {
            throw new RuntimeException("Semantic Model Cannot be Empty");
        }
        ModulePartNode modulePartNode = document.get().syntaxTree().rootNode();
        for (ImportDeclarationNode importDeclaration : modulePartNode.imports()) {
            Optional<Symbol> symbol = semanticModel.get().symbol(importDeclaration);
            if (symbol.isEmpty() || symbol.get().kind() != SymbolKind.MODULE) {
                continue;
            }
            currentDocImportsMap.put(importDeclaration, (ModuleSymbol) symbol.get());
        }

        return currentDocImportsMap;
    }

    /**
     * Get the actions defined over and endpoint.
     *
     * @param symbol Endpoint variable symbol to evaluate
     * @return {@link List} List of extracted actions as Symbol Info
     */
    public static List<Symbol> getClientActions(Symbol symbol) {
        if (!SymbolUtil.isObject(symbol)) {
            return new ArrayList<>();
        }
        TypeSymbol typeDescriptor = CommonUtil.getRawType(SymbolUtil.getTypeDescriptor(symbol).orElseThrow());
        return ((ObjectTypeSymbol) typeDescriptor).methods().values().stream()
                .filter(method -> method.qualifiers().contains(Qualifier.REMOTE))
                .collect(Collectors.toList());
    }
}

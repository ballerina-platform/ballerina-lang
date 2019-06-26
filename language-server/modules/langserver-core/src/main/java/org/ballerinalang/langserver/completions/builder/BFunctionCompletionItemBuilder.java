/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.builder;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.model.elements.MarkdownDocAttachment;
import org.ballerinalang.model.symbols.SymbolKind;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.types.BArrayType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BInvokableType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BNilType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.semantics.model.types.BUnionType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class is being used to build function type completion item.
 *
 * @since 0.983.0
 */
public final class BFunctionCompletionItemBuilder {
    private BFunctionCompletionItemBuilder() {
    }

    /**
     * Creates and returns a completion item.
     *
     * @param bSymbol    BSymbol or null
     * @param label      label
     * @param insertText insert text
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(BInvokableSymbol bSymbol, String label, String insertText) {
        CompletionItem item = new CompletionItem();
        item.setLabel(label);
        item.setInsertText(insertText);
        setMeta(item, bSymbol);
        return item;
    }

    /**
     * Creates and returns a completion item.
     *
     * @param bSymbol BSymbol
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(BInvokableSymbol bSymbol) {
        CompletionItem item = new CompletionItem();
        setMeta(item, bSymbol);
        if (bSymbol != null) {
            // Override function signature
            Pair<String, String> functionSignature = getFunctionSignature(bSymbol);
            item.setInsertText(functionSignature.getLeft());
            item.setLabel(functionSignature.getRight());
        }
        return item;
    }

    private static void setMeta(CompletionItem item, BInvokableSymbol bSymbol) {
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setDetail(ItemResolverConstants.FUNCTION_TYPE);
        item.setKind(CompletionItemKind.Function);
        item.setCommand(new Command("editor.action.triggerParameterHints", "editor.action.triggerParameterHints"));
        if (bSymbol != null && bSymbol.markdownDocumentation != null) {
            item.setDocumentation(getDocumentation(bSymbol));
        }
    }

    private static Either<String, MarkupContent> getDocumentation(BInvokableSymbol bInvokableSymbol) {
        String pkgID = bInvokableSymbol.pkgID.toString();

        MarkdownDocAttachment markdownDocAttachment = bInvokableSymbol.getMarkdownDocAttachment();
        String description = markdownDocAttachment.description == null ? "" : markdownDocAttachment.description;
        List<MarkdownDocAttachment.Parameter> parameters = markdownDocAttachment.parameters;
        List<BVarSymbol> defaultParams = bInvokableSymbol.getDefaultableParameters();

        MarkupContent docMarkupContent = new MarkupContent();

        docMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        String documentation = "**Package:** " + "_" + pkgID + "_" + CommonUtil.MD_LINE_SEPARATOR
                + CommonUtil.MD_LINE_SEPARATOR + description + CommonUtil.MD_LINE_SEPARATOR
                + CommonUtil.MD_LINE_SEPARATOR + "---  " + CommonUtil.MD_LINE_SEPARATOR + "**Parameters**"
                + CommonUtil.MD_LINE_SEPARATOR
                + parameters.stream()
                .map(parameter -> {
                    Optional<BVarSymbol> defaultVal = defaultParams.stream()
                            .filter(bVarSymbol -> bVarSymbol.getName().getValue().equals(parameter.getName()))
                            .findFirst();
                    String paramDescription = "- _" + parameter.getName() + "_" + CommonUtil.MD_LINE_SEPARATOR
                            + "    " + parameter.getDescription() + CommonUtil.MD_LINE_SEPARATOR;
                    if (defaultVal.isPresent() && defaultVal.get().defaultValue != null) {
                        return paramDescription + "Default Value: " + defaultVal.get().defaultValue.getValue();
                    }

                    return paramDescription;
                })
                .collect(Collectors.joining(CommonUtil.MD_LINE_SEPARATOR));

        if (!(bInvokableSymbol.retType instanceof BNilType)
                && bInvokableSymbol.retType != null
                && bInvokableSymbol.retType.tsymbol != null) {
            documentation = documentation + CommonUtil.MD_LINE_SEPARATOR
                    + CommonUtil.MD_LINE_SEPARATOR + "**Return**" + CommonUtil.MD_LINE_SEPARATOR
                    + bInvokableSymbol.retType.toString();
        }
        docMarkupContent.setValue(documentation);

        return Either.forRight(docMarkupContent);
    }

    /**
     * Get the function signature.
     *
     * @param bInvokableSymbol - ballerina function instance
     * @return {@link Pair} of insert text(left-side) and signature label(right-side)
     */
    private static Pair<String, String> getFunctionSignature(BInvokableSymbol bInvokableSymbol) {
        String[] funcNameComponents = bInvokableSymbol.getName().getValue().split("\\.");
        String functionName = funcNameComponents[funcNameComponents.length - 1];

        // If there is a receiver symbol, then the name comes with the package name and struct name appended.
        // Hence we need to remove it
        if (bInvokableSymbol.receiverSymbol != null) {
            String receiverType = bInvokableSymbol.receiverSymbol.getType().toString();
            functionName = functionName.replace(receiverType + ".", "");
        }
        functionName = functionName.equals("__init") ? "new" : functionName;
        StringBuilder signature = new StringBuilder(functionName + "(");
        StringBuilder insertText = new StringBuilder(functionName + "(");
        List<BVarSymbol> parameterDefs = bInvokableSymbol.getParameters();

        if (bInvokableSymbol.kind == null
                && (SymbolKind.RECORD.equals(bInvokableSymbol.owner.kind)
                || SymbolKind.FUNCTION.equals(bInvokableSymbol.owner.kind))) {
            List<String> funcArguments = FunctionGenerator.getFuncArguments(bInvokableSymbol);
            if (!funcArguments.isEmpty()) {
                int funcArgumentsCount = funcArguments.size();
                for (int itr = 0; itr < funcArgumentsCount; itr++) {
                    String argument = funcArguments.get(itr);
                    signature.append(argument);

                    if (!(itr == funcArgumentsCount - 1)) {
                        signature.append(", ");
                    }
                }
                insertText.append("${1}");
            }
        } else {
            for (int itr = 0; itr < parameterDefs.size(); itr++) {
                signature.append(getParameterSignature(parameterDefs.get(itr), false));

                if (itr != parameterDefs.size() - 1) {
                    signature.append(", ");
                }
            }
            insertText.append("${1}");
        }
        signature.append(")");
        insertText.append(")");
        if (bInvokableSymbol.type.getReturnType() == null
                || bInvokableSymbol.type.getReturnType() instanceof BNilType) {
            insertText.append(";");
        }
        String initString = "(";
        String endString = ")";

        BType returnType = bInvokableSymbol.type.getReturnType();
        if (returnType != null && !(returnType instanceof BNilType)) {
            signature.append(initString).append(returnType.toString());
            signature.append(endString);
        }

        return new ImmutablePair<>(insertText.toString(), signature.toString());
    }

    private static String getParameterSignature(BVarSymbol bVarSymbol, boolean isDefault) {
        if (!isDefault) {
            return getTypeName(bVarSymbol) + " " + bVarSymbol.getName();
        } else {
            String defaultStringVal;
            if (bVarSymbol.defaultValue == null || bVarSymbol.defaultValue.getValue() == null) {
                defaultStringVal = "()";
            } else {
                defaultStringVal = bVarSymbol.defaultValue.getValue().toString();
            }
            return getTypeName(bVarSymbol) + " " + bVarSymbol.getName() + " = " + defaultStringVal;
        }
    }

    private static String getTypeName(BVarSymbol bVarSymbol) {
        BType paramType = bVarSymbol.getType();
        String typeName;
        if (paramType instanceof BInvokableType) {
            // Check for the case when we can give a function as a parameter
            typeName = bVarSymbol.type.toString();
        } else if (paramType instanceof BUnionType) {
            typeName = paramType.toString();
        } else {
            BTypeSymbol tSymbol;
            tSymbol = (paramType instanceof BArrayType) ?
                    ((BArrayType) paramType).eType.tsymbol : paramType.tsymbol;
            List<Name> nameComps = tSymbol.pkgID.nameComps;
            if (tSymbol.pkgID.getName().getValue().equals(Names.BUILTIN_PACKAGE.getValue())
                    || tSymbol.pkgID.getName().getValue().equals(Names.DOT.getValue())) {
                typeName = tSymbol.getName().getValue();
            } else {
                typeName = CommonUtil.getLastItem(nameComps).getValue() + CommonKeys.PKG_DELIMITER_KEYWORD
                        + tSymbol.getName().getValue();
            }

            if ((paramType instanceof BArrayType)) {
                typeName += "[]";
            }
        }

        return typeName;
    }
}

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
import org.ballerinalang.langserver.common.UtilSymbolKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.ballerinalang.model.symbols.SymbolKind;
import org.ballerinalang.model.types.TypeConstants;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
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
        if (bSymbol != null && bSymbol.markdownDocumentation != null) {
            item.setDocumentation(bSymbol.markdownDocumentation.description);
        }
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
        StringBuilder signature = new StringBuilder(functionName + "(");
        StringBuilder insertText = new StringBuilder(functionName + "(");
        List<BVarSymbol> parameterDefs = bInvokableSymbol.getParameters();
        List<BVarSymbol> defaultParameterDefs = bInvokableSymbol.getDefaultableParameters();

        if (bInvokableSymbol.kind == null
                && (SymbolKind.RECORD.equals(bInvokableSymbol.owner.kind)
                || SymbolKind.FUNCTION.equals(bInvokableSymbol.owner.kind))) {
            List<String> funcArguments = CommonUtil.FunctionGenerator.getFuncArguments(bInvokableSymbol);
            if (!funcArguments.isEmpty()) {
                int funcArgumentsCount = funcArguments.size();
                for (int itr = 0; itr < funcArgumentsCount; itr++) {
                    String argument = funcArguments.get(itr);
                    signature.append(argument);
                    insertText.append("${").append(itr + 1).append(":");
                    insertText.append(argument.split(" ")[1]).append("}");

                    if (!(itr == funcArgumentsCount - 1)) {
                        signature.append(", ");
                        insertText.append(", ");
                    }
                }
            }
        } else {
            for (int itr = 0; itr < parameterDefs.size(); itr++) {
                signature.append(getParameterSignature(parameterDefs.get(itr), false));
                insertText.append(getParameterInsertText(parameterDefs.get(itr), false, itr + 1));

                if (!(itr == parameterDefs.size() - 1 && defaultParameterDefs.isEmpty())) {
                    signature.append(", ");
                    insertText.append(", ");
                }
            }
            for (int itr = 0; itr < defaultParameterDefs.size(); itr++) {
                signature.append(getParameterSignature(defaultParameterDefs.get(itr), true));
                insertText.append(getParameterInsertText(defaultParameterDefs.get(itr), true,
                                                         defaultParameterDefs.size() + itr + 1));

                if (itr < defaultParameterDefs.size() - 1) {
                    signature.append(", ");
                    insertText.append(", ");
                }
            }
        }
        signature.append(")");
        insertText.append(")");
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
            if (bVarSymbol.defaultValue == null) {
                defaultStringVal = "()";
            } else {
                defaultStringVal = bVarSymbol.defaultValue.toString();
            }
            return getTypeName(bVarSymbol) + " " + bVarSymbol.getName() + " = " + defaultStringVal;
        }
    }

    private static String getParameterInsertText(BVarSymbol bVarSymbol, boolean isDefault, int iteration) {
        if (!isDefault) {
            return "${" + iteration + ":" + bVarSymbol.getName() + "}";
        } else {
            String defaultStringVal;
            if (bVarSymbol.defaultValue == null) {
                defaultStringVal = "()";
            } else {
                defaultStringVal = bVarSymbol.defaultValue.toString();
                if (bVarSymbol.getType() != null
                        && bVarSymbol.getType().toString().equals(TypeConstants.STRING_TNAME)) {
                    defaultStringVal = "\"" + defaultStringVal + "\"";
                }
            }
            return bVarSymbol.getName() + " = " + "${" + iteration + ":" + defaultStringVal + "}";
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
                typeName = CommonUtil.getLastItem(nameComps).getValue() + UtilSymbolKeys.PKG_DELIMITER_KEYWORD
                        + tSymbol.getName().getValue();
            }

            if ((paramType instanceof BArrayType)) {
                typeName += "[]";
            }
        }

        return typeName;
    }
}

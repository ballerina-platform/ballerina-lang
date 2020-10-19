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

import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.types.FunctionTypeDescriptor;
import io.ballerina.compiler.api.types.ObjectTypeDescriptor;
import io.ballerina.compiler.api.types.Parameter;
import io.ballerina.compiler.api.types.ParameterKind;
import io.ballerina.compiler.api.types.TypeDescKind;
import io.ballerina.compiler.api.types.TypeReferenceTypeDescriptor;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * This class is being used to build function type completion item.
 *
 * @since 0.983.0
 */
public final class FunctionCompletionItemBuilder {
    private FunctionCompletionItemBuilder() {
    }

    /**
     * Creates and returns a completion item.
     *
     * @param funcSymbol BSymbol or null
     * @param label      label
     * @param insertText insert text
     * @param context    {@link LSContext}
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(FunctionSymbol funcSymbol, String label, String insertText, LSContext context) {
        CompletionItem item = new CompletionItem();
        item.setLabel(label);
        item.setInsertText(insertText);
        setMeta(item, funcSymbol, context);
        return item;
    }

    /**
     * Creates and returns a completion item.
     *
     * @param functionSymbol BSymbol
     * @param context        LS context
     * @return {@link CompletionItem}
     */
    public static CompletionItem build(FunctionSymbol functionSymbol, LSContext context) {
        CompletionItem item = new CompletionItem();
        setMeta(item, functionSymbol, context);
        if (functionSymbol != null) {
            // Override function signature
            String functionName = functionSymbol.name();
            Pair<String, String> functionSignature = CommonUtil.getFunctionInvocationSignature(functionSymbol,
                    functionName, context);
            item.setInsertText(functionSignature.getLeft());
            item.setLabel(functionSignature.getRight());
        }
        return item;
    }

    public static CompletionItem build(ObjectTypeDescriptor typeDesc, InitializerBuildMode mode, LSContext ctx) {
        MethodSymbol initMethod = typeDesc.initMethod().isEmpty() ? null : typeDesc.initMethod().get();
        CompletionItem item = new CompletionItem();
        setMeta(item, initMethod, ctx);
        String functionName;
        if (mode == InitializerBuildMode.EXPLICIT && typeDesc.kind() == TypeDescKind.TYPE_REFERENCE) {
            functionName = ((TypeReferenceTypeDescriptor) typeDesc).name();
            // TODO: Following is blocked due to the Type Referencing issue in Semantic Model
//            Optional<BLangIdentifier> moduleAlias = ctx.get(DocumentServiceKeys.CURRENT_DOC_IMPORTS_KEY).stream()
//                    .filter(pkg -> pkg.symbol != null && pkg.symbol.pkgID == typeDesc.pkgID)
//                    .map(BLangImportPackage::getAlias)
//                    .findAny();
//            String moduleAlias = typeDesc.moduleID().modulePrefix()
//            if (nodeAtCursor.kind() != SyntaxKind.QUALIFIED_NAME_REFERENCE && moduleAlias.isPresent()) {
//                /*
//                Handles the following case
//                (1) ... = new <cursor>
//                (1) ... = new m<cursor> // blocked by #25210
//                 */
//                functionName = moduleAlias.get().getValue() + ":" + functionName;
//            }
        } else {
            functionName = "new";
        }
        Pair<String, String> functionSignature = CommonUtil.getFunctionInvocationSignature(initMethod,
                functionName, ctx);
        item.setInsertText(functionSignature.getLeft());
        item.setLabel(functionSignature.getRight());

        return item;
    }

    private static void setMeta(CompletionItem item, FunctionSymbol bSymbol, LSContext ctx) {
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setDetail(ItemResolverConstants.FUNCTION_TYPE);
        item.setKind(CompletionItemKind.Function);
        if (bSymbol != null) {
            List<String> funcArguments = FunctionGenerator.getFuncArguments(bSymbol, ctx);
            if (!funcArguments.isEmpty()) {
                Command cmd = new Command("editor.action.triggerParameterHints", "editor.action.triggerParameterHints");
                item.setCommand(cmd);
            }
            boolean skipFirstParam = CommonUtil.skipFirstParam(ctx, bSymbol);
            if (bSymbol.docAttachment().isPresent()) {
                item.setDocumentation(getDocumentation(bSymbol, skipFirstParam, ctx));
            }
        }
    }

    private static Either<String, MarkupContent> getDocumentation(FunctionSymbol functionSymbol,
                                                                  boolean skipFirstParam, LSContext ctx) {
        String pkgID = functionSymbol.moduleID().toString();
        FunctionTypeDescriptor functionTypeDesc = functionSymbol.typeDescriptor();

        Optional<Documentation> docAttachment = functionSymbol.docAttachment();
        String description = docAttachment.isEmpty() || docAttachment.get().description().isEmpty()
                ? "" : docAttachment.get().description().get();
        Map<String, String> docParamsMap = new HashMap<>();
        docAttachment.ifPresent(documentation -> documentation.parameterMap().forEach(docParamsMap::put));

        List<Parameter> defaultParams = functionTypeDesc.requiredParams().stream()
                .filter(parameter -> parameter.kind() == ParameterKind.DEFAULTABLE)
                .collect(Collectors.toList());

        MarkupContent docMarkupContent = new MarkupContent();
        docMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        String documentation = "**Package:** " + "_" + pkgID + "_" + CommonUtil.MD_LINE_SEPARATOR
                + CommonUtil.MD_LINE_SEPARATOR + description + CommonUtil.MD_LINE_SEPARATOR;
        StringJoiner joiner = new StringJoiner(CommonUtil.MD_LINE_SEPARATOR);
        List<Parameter> functionParameters = new ArrayList<>(functionTypeDesc.requiredParams());
        if (functionTypeDesc.restParam().isPresent()) {
            functionParameters.add(functionTypeDesc.restParam().get());
        }
        for (int i = 0; i < functionParameters.size(); i++) {
            Parameter param = functionParameters.get(i);
            String paramType = param.typeDescriptor().signature();
            if (i == 0 && skipFirstParam) {
                continue;
            }

            Optional<Parameter> defaultVal = defaultParams.stream()
                    .filter(parameter -> parameter.name().get().equals(param.name().get()))
                    .findFirst();
            String paramDescription = "- " + "`" + paramType + "` " + param.name().get();
            if (param.name().isPresent() && docParamsMap.containsKey(param.name().get())) {
                paramDescription += ": " + docParamsMap.get(param.name().get());
            }
            if (defaultVal.isPresent()) {
                joiner.add(paramDescription + "(Defaultable)");
            } else {
                joiner.add(paramDescription);
            }
        }
        String paramsStr = joiner.toString();
        if (!paramsStr.isEmpty()) {
            documentation += "**Params**" + CommonUtil.MD_LINE_SEPARATOR + paramsStr;
        }
        if (functionTypeDesc.kind() != TypeDescKind.NIL) {
            String desc = "";
            if (docAttachment.isPresent() && docAttachment.get().returnDescription().isPresent()
                    && !docAttachment.get().returnDescription().get().isEmpty()) {
                desc = "- " + CommonUtil.MD_NEW_LINE_PATTERN.matcher(docAttachment.get().returnDescription().get())
                        .replaceAll(CommonUtil.MD_LINE_SEPARATOR) + CommonUtil.MD_LINE_SEPARATOR;
            }
            documentation += CommonUtil.MD_LINE_SEPARATOR + CommonUtil.MD_LINE_SEPARATOR + "**Returns**"
                    + " `" + functionTypeDesc.returnTypeDescriptor().get().signature() + "` " +
                    CommonUtil.MD_LINE_SEPARATOR + desc + CommonUtil.MD_LINE_SEPARATOR;
        }
        docMarkupContent.setValue(documentation);

        return Either.forRight(docMarkupContent);
    }

    /**
     * Build mode, either explicit or implicit initializer.
     *
     * @since 2.0.0
     */
    public enum InitializerBuildMode {
        EXPLICIT,
        IMPLICIT
    }
}

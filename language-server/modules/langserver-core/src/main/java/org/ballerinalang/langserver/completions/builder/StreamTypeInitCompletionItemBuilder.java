/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.completions.builder;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.TypeDefinitionSymbol;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.ModuleUtil;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.completions.util.QNameRefCompletionUtil;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.Optional;

/**
 * Completion item builder for the stream type initializer.
 * Eg:
 * 1. TestStreamType test = new TestStreamType();
 *
 * @since 2.0.0
 */
public class StreamTypeInitCompletionItemBuilder {

    private StreamTypeInitCompletionItemBuilder() {
    }

    public static CompletionItem build() {
        CompletionItem item = new CompletionItem();
        String label = "new()";
        item.setInsertText(label);
        item.setLabel(label);
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setKind(CompletionItemKind.Function);

        return item;
    }

    public static CompletionItem build(TypeDefinitionSymbol symbol, BallerinaCompletionContext context) {
        CompletionItem item = new CompletionItem();
        String label = getQualifiedFunctionName(context, symbol) + "()";
        item.setInsertText(label);
        item.setLabel(label);
        item.setInsertTextFormat(InsertTextFormat.Snippet);
        item.setKind(CompletionItemKind.Function);

        if (symbol.documentation().isPresent()) {
            item.setDocumentation(getDocumentation(symbol));
        }
        return item;
    }

    private static String getQualifiedFunctionName(BallerinaCompletionContext ctx,
                                                   TypeDefinitionSymbol typeDefinitionSymbol) {
        boolean onQNameRef = QNameRefCompletionUtil.onQualifiedNameIdentifier(ctx, ctx.getNodeAtCursor());
        Optional<ModuleSymbol> module = typeDefinitionSymbol.getModule();
        if (module.isEmpty() || onQNameRef) {
            return typeDefinitionSymbol.getName().get();
        }
        ModuleID moduleID = module.get().id();
        String modulePrefix = ModuleUtil.getModulePrefix(ctx, moduleID.orgName(), moduleID.moduleName());

        // Added the annotations prefix check until #31884 is resolved
        if (modulePrefix.isEmpty() || modulePrefix.equals("annotations")) {
            return typeDefinitionSymbol.getName().get();
        }

        return modulePrefix + SyntaxKind.COLON_TOKEN.stringValue() + typeDefinitionSymbol.getName().get();
    }

    private static Either<String, MarkupContent> getDocumentation(TypeDefinitionSymbol typeDefinitionSymbol) {
        Optional<Documentation> docAttachment = typeDefinitionSymbol.documentation();
        String description = docAttachment.isEmpty() || docAttachment.get().description().isEmpty()
                ? "" : docAttachment.get().description().get();

        MarkupContent docMarkupContent = new MarkupContent();
        docMarkupContent.setKind(CommonUtil.MARKDOWN_MARKUP_KIND);
        StringBuilder documentation = new StringBuilder();
        if (typeDefinitionSymbol.getModule().isPresent()) {
            String moduleId = typeDefinitionSymbol.getModule().get().id().toString();
            documentation.append("**Package:** _")
                    .append(moduleId).append("_")
                    .append(CommonUtil.MD_LINE_SEPARATOR)
                    .append(CommonUtil.MD_LINE_SEPARATOR);
        }
        documentation.append(description).append(CommonUtil.MD_LINE_SEPARATOR);
        docMarkupContent.setValue(documentation.toString());

        return Either.forRight(docMarkupContent);
    }
}

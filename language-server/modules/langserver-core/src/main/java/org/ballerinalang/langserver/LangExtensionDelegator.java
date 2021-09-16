/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver;

import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.CodeActionExtension;
import org.ballerinalang.langserver.commons.CompletionContext;
import org.ballerinalang.langserver.commons.CompletionExtension;
import org.ballerinalang.langserver.commons.DiagnosticsExtension;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.FormattingExtension;
import org.ballerinalang.langserver.commons.LanguageExtension;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Delegate the operation to the valid extension.
 *
 * @since 2.0.0
 */
public class LangExtensionDelegator {

    private static final LangExtensionDelegator INSTANCE = new LangExtensionDelegator();

    private final List<CompletionExtension> completionExtensions = new ArrayList<>();
    private final List<CodeActionExtension> codeActionsExtensions = new ArrayList<>();
    private final List<FormattingExtension> formatExtensions = new ArrayList<>();
    private final List<DiagnosticsExtension> diagExtensions = new ArrayList<>();

    private LangExtensionDelegator() {
        ServiceLoader.load(LanguageExtension.class).forEach(languageExtension -> {
            switch (languageExtension.kind()) {
                case COMPLETION:
                    completionExtensions.add((CompletionExtension) languageExtension);
                    break;
                case CODEACTION:
                    codeActionsExtensions.add((CodeActionExtension) languageExtension);
                    break;
                case FORMAT:
                    formatExtensions.add((FormattingExtension) languageExtension);
                    break;
                case DIAGNOSTIC:
                    diagExtensions.add((DiagnosticsExtension) languageExtension);
                    break;
                default:
                    break;
            }
        });
    }

    /**
     * Get the completions.
     *
     * @param params completion parameters
     * @return {@link Either} completion results
     * @throws Throwable while executing the extension
     */
    @Deprecated
    public Either<List<CompletionItem>, CompletionList> completion(CompletionParams params,
                                                                   CompletionContext context,
                                                                   LanguageServerContext serverContext)
            throws Throwable {
        List<CompletionItem> completionItems = new ArrayList<>();
        for (CompletionExtension ext : completionExtensions) {
            if (ext.validate(params)) {
                completionItems.addAll(ext.execute(params, context, serverContext));
            }
        }

        return Either.forLeft(completionItems);
    }

    /**
     * Get the completions.
     *
     * @param params completion parameters
     * @return {@link Either} completion results
     * @throws Throwable while executing the extension
     */
    public Either<List<CompletionItem>, CompletionList> completion(CompletionParams params,
                                                                   CompletionContext context,
                                                                   LanguageServerContext serverContext,
                                                                   CancelChecker cancelChecker)
            throws Throwable {
        List<CompletionItem> completionItems = new ArrayList<>();
        for (CompletionExtension ext : completionExtensions) {
            if (ext.validate(params)) {
                completionItems.addAll(ext.execute(params, context, serverContext, cancelChecker));
            }
        }

        return Either.forLeft(completionItems);
    }

    /**
     * Get the formatting.
     *
     * @param params formatting parameters
     * @return {@link List} of text edits
     * @throws Throwable while executing the extension
     */
    public List<? extends TextEdit> formatting(DocumentFormattingParams params,
                                               DocumentServiceContext context,
                                               LanguageServerContext serverContext)
            throws Throwable {
        List<TextEdit> textEdits = new ArrayList<>();
        for (FormattingExtension ext : formatExtensions) {
            if (ext.validate(params)) {
                textEdits.addAll(ext.execute(params, context, serverContext));
            }
        }

        return textEdits;
    }

    /**
     * Get code actions.
     *
     * @param params code-action parameters
     * @return {@link List} of text edits
     * @throws Throwable while executing the extension
     */
    public List<? extends CodeAction> codeActions(CodeActionParams params, CodeActionContext context,
                                                  LanguageServerContext serverContext)
            throws Throwable {
        List<CodeAction> actions = new ArrayList<>();
        for (CodeActionExtension ext : codeActionsExtensions) {
            if (ext.validate(params)) {
                actions.addAll(ext.execute(params, context, serverContext));
            }
        }

        return actions;
    }

    /**
     * Get the Diagnostics.
     *
     * @param uri document URI
     * @return {@link PublishDiagnosticsParams} diagnostic params calculated
     * @throws Throwable while executing the extension
     */
    public List<PublishDiagnosticsParams> diagnostics(String uri,
                                                      DocumentServiceContext context,
                                                      LanguageServerContext serverContext) throws Throwable {
        List<PublishDiagnosticsParams> diagnosticsParams = new ArrayList<>();
        for (DiagnosticsExtension ext : diagExtensions) {
            if (ext.validate(uri)) {
                diagnosticsParams.addAll(ext.execute(uri, context, serverContext));
            }
        }

        return diagnosticsParams;
    }

    public static LangExtensionDelegator instance() {
        return INSTANCE;
    }
}

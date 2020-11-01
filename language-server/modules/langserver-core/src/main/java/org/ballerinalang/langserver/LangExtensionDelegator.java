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

import org.ballerinalang.langserver.commons.CompletionExtension;
import org.ballerinalang.langserver.commons.DiagnosticsExtension;
import org.ballerinalang.langserver.commons.FormattingExtension;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.LanguageExtension;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionList;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DocumentFormattingParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.TextEdit;
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
    private final List<FormattingExtension> formatExtensions = new ArrayList<>();
    private final List<DiagnosticsExtension> diagExtensions = new ArrayList<>();

    private LangExtensionDelegator() {
        ServiceLoader.load(LanguageExtension.class).forEach(languageExtension -> {
            switch (languageExtension.kind()) {
                case COMPLETION:
                    completionExtensions.add((CompletionExtension) languageExtension);
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
     */
    public Either<List<CompletionItem>, CompletionList> completion(CompletionParams params, LSContext context) {
        return completionExtensions.stream().filter(ext -> ext.validate(params))
                .map(ext -> ext.execute(params, context))
                .findFirst()
                .orElse(Either.forRight(new CompletionList()));
    }

    /**
     * Get the formatting.
     *
     * @param params formatting parameters
     * @return {@link List} of text edits
     */
    public List<? extends TextEdit> formatting(DocumentFormattingParams params, LSContext context) {
        return formatExtensions.stream().filter(ext -> ext.validate(params))
                .map(ext -> ext.execute(params, context))
                .findFirst()
                .orElse(new ArrayList<>());
    }

    /**
     * Get the Diagnostics.
     *
     * @param uri document URI
     * @return {@link PublishDiagnosticsParams} diagnostic params calculated
     */
    public PublishDiagnosticsParams diagnostics(String uri, LSContext context) {
        return diagExtensions.stream().filter(ext -> ext.validate(uri))
                .map(ext -> ext.execute(uri, context))
                .findFirst()
                .orElse(new PublishDiagnosticsParams());
    }

    public static LangExtensionDelegator instance() {
        return INSTANCE;
    }
}

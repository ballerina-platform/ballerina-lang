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
package org.ballerinalang.langserver.semantictokens;

import io.ballerina.projects.Document;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.SemanticTokensContext;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.SemanticTokens;
import org.eclipse.lsp4j.SemanticTokensLegend;
import org.eclipse.lsp4j.SemanticTokensServerFull;
import org.eclipse.lsp4j.SemanticTokensWithRegistrationOptions;
import org.eclipse.lsp4j.Unregistration;
import org.eclipse.lsp4j.UnregistrationParams;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Semantic tokens util class.
 *
 * @since 2.0.0
 */
public class SemanticTokensUtils {

    private SemanticTokensUtils() {
    }

    /**
     * Returns the semantic tokens for a given context.
     *
     * @param semanticTokensContext context
     * @return {@link SemanticTokens}
     */
    public static SemanticTokens getSemanticTokens(SemanticTokensContext semanticTokensContext) {
        String fileUri = semanticTokensContext.fileUri();
        Optional<Path> filePath = CommonUtil.getPathFromURI(fileUri);
        if (filePath.isEmpty()) {
            return new SemanticTokens(Collections.emptyList());
        }
        Optional<Document> document = semanticTokensContext.currentDocument();
        if (document.isEmpty()) {
            return new SemanticTokens(Collections.emptyList());
        }
        return new SemanticTokensVisitor(semanticTokensContext).getSemanticTokens(document.get().syntaxTree()
                .rootNode());
    }

    /**
     * Returns the list of semantic token types supported by Ballerina LS.
     *
     * @return List of token types
     */
    public static List<String> getTokenTypes() {
        return Arrays.stream(SemanticTokensContext.TokenTypes.values())
                .map(SemanticTokensContext.TokenTypes::getValue)
                .collect(Collectors.toList());
    }

    /**
     * Returns the list of semantic token type modifiers supported by Ballerina LS.
     *
     * @return List of modifiers
     */
    public static List<String> getTokenTypeModifiers() {
        return Arrays.stream(SemanticTokensContext.TokenTypeModifiers.values())
                .map(SemanticTokensContext.TokenTypeModifiers::getValue)
                .collect(Collectors.toList());
    }

    /**
     * Returns semantic tokens registration options.
     *
     * @return {@link SemanticTokensWithRegistrationOptions}
     */
    public static SemanticTokensWithRegistrationOptions getSemanticTokensRegistrationOptions() {
        SemanticTokensLegend semanticTokensLegend = new SemanticTokensLegend(getTokenTypes(), getTokenTypeModifiers());
        return new SemanticTokensWithRegistrationOptions(semanticTokensLegend, new SemanticTokensServerFull(false));
    }

    /**
     * Register semantic tokens capability.
     *
     * @param languageClient Language client instance
     */
    public static void registerSemanticTokensCapability(ExtendedLanguageClient languageClient) {
        if (languageClient == null) {
            return;
        }
        SemanticTokensWithRegistrationOptions options = SemanticTokensUtils.getSemanticTokensRegistrationOptions();
        Registration registration = new Registration(SemanticTokensConstants.REGISTRATION_ID,
                SemanticTokensConstants.REQUEST_METHOD, options);
        languageClient.registerCapability(new RegistrationParams(Collections.singletonList(registration)));
    }

    /**
     * Unregister semantic tokens capability.
     *
     * @param languageClient Language client instance
     */
    public static void unRegisterSemanticTokensCapability(ExtendedLanguageClient languageClient) {
        if (languageClient == null) {
            return;
        }
        Unregistration unregistration = new Unregistration(SemanticTokensConstants.REGISTRATION_ID,
                SemanticTokensConstants.REQUEST_METHOD);
        languageClient.unregisterCapability(
                new UnregistrationParams(Collections.singletonList(unregistration)));
    }
}

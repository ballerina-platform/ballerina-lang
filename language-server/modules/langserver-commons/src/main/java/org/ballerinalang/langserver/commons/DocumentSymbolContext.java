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
package org.ballerinalang.langserver.commons;

import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.eclipse.lsp4j.DocumentSymbolCapabilities;
import org.eclipse.lsp4j.DocumentSymbolParams;
import org.eclipse.lsp4j.SymbolTagSupportCapabilities;

import java.util.Optional;

/**
 * Represents the document symbol operation context.
 *
 * @since 2.0.0
 */
public interface DocumentSymbolContext extends DocumentServiceContext {

    /**
     * Get document symbol params.
     *
     * @return {@link DocumentSymbolParams} Document symbol params.
     */
    DocumentSymbolParams getParams();

    /**
     * Get client capabilities.
     *
     * @return {@link LSClientCapabilities} Client capabilities.
     */
    LSClientCapabilities getClientCapabilities();

    /**
     * Get document Symbol Client Capabilities.
     *
     * @return {@link DocumentSymbolCapabilities} Document symbol client capabilities.
     */
    DocumentSymbolCapabilities getDocumentSymbolClientCapabilities();

    /**
     * Whether hierarchical document symbols are supported.
     *
     * @return hierarchical document symbol support.
     */
    boolean getHierarchicalDocumentSymbolSupport();

    /**
     * Returns if the labale support is available.
     *
     * @return label support.
     */
    boolean getLabelSupport();

    /**
     * Returns the supported tag list.
     *
     * @return {@link Optional<SymbolTagSupportCapabilities>} supported tags.
     */
    Optional<SymbolTagSupportCapabilities> supportedTags();

    /**
     * Returns if the deprecated tag is supported.
     *
     * @return deprecated tag support.
     */
    boolean deprecatedSupport();
}

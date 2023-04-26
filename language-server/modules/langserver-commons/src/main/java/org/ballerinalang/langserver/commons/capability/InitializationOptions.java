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
package org.ballerinalang.langserver.commons.capability;

/**
 * Represents the initialization options sent by the client to the language server.
 *
 * @since 2.0.0
 */
public interface InitializationOptions {

    /**
     * Whether the client support bala URI scheme.
     */
    String KEY_BALA_SCHEME_SUPPORT = "supportBalaScheme";

    /**
     * Semantic tokens initialization option key.
     */
    String KEY_ENABLE_SEMANTIC_TOKENS = "enableSemanticHighlighting";

    /**
     * Whether the client supports rename popup.
     */
    String KEY_RENAME_SUPPORT = "supportRenamePopup";

    /**
     * Whether the client supports {@link org.eclipse.lsp4j.Position} based rename popup.
     */
    String KEY_POSITIONAL_RENAME_SUPPORT = "supportPositionalRenamePopup";
    
    /**
     * Whether the client supports quick picks.
     */
    String KEY_QUICKPICK_SUPPORT = "supportQuickPick";

    /**
     * Whether the LS should run in lightweight mode.
     */
    String KEY_ENABLE_LIGHTWEIGHT_MODE = "enableLightWeightMode";

    /**
     * Return if the client support bala URI scheme.
     *
     * @return True if bala URi scheme is supported.
     */
    boolean isBalaSchemeSupported();

    /**
     * Returns if Ballerina semantic tokens is enabled.
     *
     * @return True if supported, false otherwise
     */
    boolean isEnableSemanticTokens();

    /**
     * Returns if the client supports rename popup.
     *
     * @return True if supported, false otherwise
     */
    boolean isRefactorRenameSupported();

    /**
     * Returns if the client supports {@link org.eclipse.lsp4j.Position} based rename.
     * @return True if supported, false otherwise
     */
    boolean isPositionalRefactorRenameSupported();

    /**
     * Returns if the client supports quick picks.
     *
     * @return True if supported, false otherwise
     */
    boolean isQuickPickSupported();

    /**
     * Returns if the LS enable lightweight mode.
     *
     * @return True if enabled, false otherwise
     */
    boolean isEnableLightWeightMode();
}

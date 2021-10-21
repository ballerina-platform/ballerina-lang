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
}

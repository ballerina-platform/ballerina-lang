/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.signature;

import org.ballerinalang.langserver.LanguageServerContext;
import org.ballerinalang.langserver.completions.SymbolInfo;

import java.util.List;

/**
 * Text Document Service context keys for the signature help operation context.
 * @since 0.95.6
 */
class SignatureKeys {
    static final LanguageServerContext.Key<String> CALLABLE_ITEM_NAME
            = new LanguageServerContext.Key<>();
    static final LanguageServerContext.Key<String> ITEM_DELIMITER
            = new LanguageServerContext.Key<>();
    static final LanguageServerContext.Key<List<SymbolInfo>> FILTERED_FUNCTIONS
            = new LanguageServerContext.Key<>();
    /**
     * If os:get... then identifier against is os
     * If res.send(.. then identifier against is res
     */
    static final LanguageServerContext.Key<String> IDENTIFIER_AGAINST
            = new LanguageServerContext.Key<>();
    static final LanguageServerContext.Key<String> IDENTIFIER_TYPE
            = new LanguageServerContext.Key<>();
    static final LanguageServerContext.Key<String> IDENTIFIER_PKGID
            = new LanguageServerContext.Key<>();
    static final LanguageServerContext.Key<Integer> PARAMETER_COUNT
            = new LanguageServerContext.Key<>();
}

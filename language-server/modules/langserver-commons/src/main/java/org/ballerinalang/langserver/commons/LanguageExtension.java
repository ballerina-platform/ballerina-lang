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
package org.ballerinalang.langserver.commons;

import org.eclipse.lsp4j.jsonrpc.CancelChecker;

/**
 * Represents the language extension SPI to cater the multi-language support in the language server.
 *
 * @param <I> Input parameter type
 * @param <O> output parameter type
 * @param <C> execution context parameter type
 * @since 2.0.0
 */
public interface LanguageExtension<I, O, C extends DocumentServiceContext> {

    /**
     * Get the language feature kind which the particular extension caters.
     *
     * @return language feature kind.
     */
    LanguageFeatureKind kind();

    /**
     * Pre-validation to be done against the URI. Not only the document extension, but also against the document name or
     * the file path can ber utilized to validate the given document.
     *
     * @param inputParams input parameters to validate
     * @return {@link Boolean} whether the document URI matches the validation criteria
     */
    boolean validate(I inputParams);

    /**
     * Execute the operation and output the result.
     *
     * @param inputParams input params for the operation
     * @param context language server operation context
     * @param serverContext language server context
     * @return {@link O} output of the operation
     * @throws Throwable while executing. Here we throw the Throwable rather than a narrower exception since the
     *                   executor has to handle the exceptions accordingly.
     */
    O execute(I inputParams, C context, LanguageServerContext serverContext) throws Throwable;

    /**
     * Execute the operation and output the result.
     *
     * @param inputParams input params for the operation
     * @param context language server operation context
     * @param serverContext language server context
     * @return {@link O} output of the operation
     * @throws Throwable while executing. Here we throw the Throwable rather than a narrower exception since the
     *                   executor has to handle the exceptions accordingly.
     */
    default O execute(I inputParams,
              C context,
              LanguageServerContext serverContext,
              CancelChecker cancelChecker) throws Throwable {
        return execute(inputParams, context, serverContext);
    }
}

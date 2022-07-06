/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.commons.codeaction.spi;

import org.ballerinalang.langserver.commons.CodeActionResolveContext;
import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.codeaction.ResolvableCodeAction;
import org.eclipse.lsp4j.CodeAction;

/**
 * Represents the SPI interface for the Ballerina Code Action Provider.
 *
 * @since 1.2.0
 */
public interface LSCodeActionProvider {

    /**
     * Returns priority to be listed the code-action.
     *
     * @return lower the number higher the priority
     */
    default int priority() {
        return 1000;
    }

    /**
     * Returns True if code action is enabled.
     *
     * @return True if code action is enabled, False otherwise
     */
    default boolean isEnabled(LanguageServerContext serverContext) {
        return true;
    }

    /**
     * Get the name of the code action. Will be used for analytics and logging purposes.
     *
     * @return Name of the code action
     */
    String getName();

    default CodeAction resolve(ResolvableCodeAction codeAction,
                               CodeActionResolveContext resolveContext) {
        return codeAction;
    }
}

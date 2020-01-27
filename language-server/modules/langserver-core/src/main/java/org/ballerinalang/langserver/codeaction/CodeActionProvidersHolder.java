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
package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Represents the Code Action provider factory.
 *
 * @since 1.1.1
 */
public class CodeActionProvidersHolder {
    private static Map<CodeActionNodeType, List<LSCodeActionProvider>> nodeBasedProviders = new HashMap<>();
    private static List<LSCodeActionProvider> diagnosticsBasedProviders = new ArrayList<>();
    private static final CodeActionProvidersHolder INSTANCE = new CodeActionProvidersHolder();

    /**
     * Returns the instance of Holder.
     *
     * @return code action provider holder instance
     */
    public static CodeActionProvidersHolder getInstance() {
        return INSTANCE;
    }

    private CodeActionProvidersHolder() {
        ServiceLoader<LSCodeActionProvider> serviceLoader = ServiceLoader.load(LSCodeActionProvider.class);
        for (CodeActionNodeType nodeType : CodeActionNodeType.values()) {
            CodeActionProvidersHolder.getNodeBasedProviders().put(nodeType, new ArrayList<>());
        }
        for (LSCodeActionProvider codeAction : serviceLoader) {
            if (codeAction.isNodeBasedSupported()) {
                for (CodeActionNodeType nodeType : codeAction.getCodeActionNodeTypes()) {
                    switch (nodeType) {
                        case FUNCTION:
                            nodeBasedProviders.get(CodeActionNodeType.FUNCTION).add(codeAction);
                            break;
                        case OBJECT:
                            nodeBasedProviders.get(CodeActionNodeType.OBJECT).add(codeAction);
                            break;
                        case SERVICE:
                            nodeBasedProviders.get(CodeActionNodeType.SERVICE).add(codeAction);
                            break;
                        case RECORD:
                            nodeBasedProviders.get(CodeActionNodeType.RECORD).add(codeAction);
                            break;
                        case RESOURCE:
                            nodeBasedProviders.get(CodeActionNodeType.RESOURCE).add(codeAction);
                            break;
                        case OBJECT_FUNCTION:
                            nodeBasedProviders.get(CodeActionNodeType.OBJECT_FUNCTION).add(codeAction);
                            break;
                        default:
                            break;
                    }
                }
            } else if (codeAction.isDiagBasedSupported()) {
                CodeActionProvidersHolder.getDiagnosticsBasedProviders().add(codeAction);
            }
        }
    }

    /**
     * Returns node based providers.
     *
     * @return node based providers
     */
    static Map<CodeActionNodeType, List<LSCodeActionProvider>> getNodeBasedProviders() {
        return nodeBasedProviders;
    }

    /**
     * Returns diagnostic based providers.
     *
     * @return diagnostic based providers
     */
    static List<LSCodeActionProvider> getDiagnosticsBasedProviders() {
        return diagnosticsBasedProviders;
    }
}

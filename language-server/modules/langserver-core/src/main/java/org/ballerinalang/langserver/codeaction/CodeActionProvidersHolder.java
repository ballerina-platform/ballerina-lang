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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

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
            nodeBasedProviders.put(nodeType, new ArrayList<>());
        }
        for (LSCodeActionProvider provider : serviceLoader) {
            if (provider.isNodeBasedSupported()) {
                for (CodeActionNodeType nodeType : provider.getCodeActionNodeTypes()) {
                    switch (nodeType) {
                        case IMPORTS:
                            nodeBasedProviders.get(CodeActionNodeType.IMPORTS).add(provider);
                            break;
                        case FUNCTION:
                            nodeBasedProviders.get(CodeActionNodeType.FUNCTION).add(provider);
                            break;
                        case OBJECT:
                            nodeBasedProviders.get(CodeActionNodeType.OBJECT).add(provider);
                            break;
                        case CLASS:
                            nodeBasedProviders.get(CodeActionNodeType.CLASS).add(provider);
                            break;
                        case SERVICE:
                            nodeBasedProviders.get(CodeActionNodeType.SERVICE).add(provider);
                            break;
                        case RECORD:
                            nodeBasedProviders.get(CodeActionNodeType.RECORD).add(provider);
                            break;
                        case RESOURCE:
                            nodeBasedProviders.get(CodeActionNodeType.RESOURCE).add(provider);
                            break;
                        case OBJECT_FUNCTION:
                            nodeBasedProviders.get(CodeActionNodeType.OBJECT_FUNCTION).add(provider);
                            break;
                        case CLASS_FUNCTION:
                            nodeBasedProviders.get(CodeActionNodeType.CLASS_FUNCTION).add(provider);
                            break;
                        default:
                            break;
                    }
                }
            }
            if (provider.isDiagBasedSupported()) {
                diagnosticsBasedProviders.add(provider);
            }
        }
    }

    /**
     * Returns active node based providers for this node type.
     *
     * @param nodeType node type
     * @return node based providers
     */
    List<LSCodeActionProvider> getActiveNodeBasedProviders(CodeActionNodeType nodeType) {
        if (nodeBasedProviders.containsKey(nodeType)) {
            return nodeBasedProviders.get(nodeType).stream()
                    .filter(LSCodeActionProvider::isEnabled)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * Returns active diagnostic based providers.
     *
     * @return diagnostic based providers
     */
    List<LSCodeActionProvider> getActiveDiagnosticsBasedProviders() {
        return diagnosticsBasedProviders.stream()
                .filter(LSCodeActionProvider::isEnabled)
                .collect(Collectors.toList());
    }
}

/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

/**
 * Represents the Code Action provider factory.
 *
 * @since 1.1.0
 */
public class BallerinaCodeActionProviderFactory {
    private static Map<CodeActionNodeType, List<BallerinaCodeActionProvider>> nodeBasedProviders;
    private static List<BallerinaCodeActionProvider> diagnosticsBasedProviders;
    private static BallerinaCodeActionProviderFactory codeActionProviderInstance;
    private ServiceLoader<BallerinaCodeActionProvider> serviceLoader;
    private boolean isInitialized = false;

    private BallerinaCodeActionProviderFactory() {
        serviceLoader = ServiceLoader.load(BallerinaCodeActionProvider.class);
    }

    static {
        codeActionProviderInstance = new BallerinaCodeActionProviderFactory();
    }

    /**
     * @return code action provider factory instance
     */
    public static BallerinaCodeActionProviderFactory getInstance() {
        return codeActionProviderInstance;
    }

    /**
     * @return node based providers
     */
    public static Map<CodeActionNodeType, List<BallerinaCodeActionProvider>> getNodeBasedProviders() {
        return nodeBasedProviders;
    }

    public static void setNodeBasedProviders(
            Map<CodeActionNodeType, List<BallerinaCodeActionProvider>> nodeBasedProviders) {
        BallerinaCodeActionProviderFactory.nodeBasedProviders = nodeBasedProviders;
    }

    /**
     * @return diagnostic based providers
     */
    public static List<BallerinaCodeActionProvider> getDiagnosticsBasedProviders() {
        return diagnosticsBasedProviders;
    }

    public static void setDiagnosticsBasedProviders(
            List<BallerinaCodeActionProvider> diagnosticsBasedProviders) {
        BallerinaCodeActionProviderFactory.diagnosticsBasedProviders = diagnosticsBasedProviders;
    }

    /**
     * Initialize the code action provider factory Adding all node based providers in to the Hash Map and diagnostic
     * based providers in to the List.
     */
    public void initiate() {
        if (isInitialized) {
            return;
        }
        BallerinaCodeActionProviderFactory.setNodeBasedProviders(new HashMap<>());
        BallerinaCodeActionProviderFactory.setDiagnosticsBasedProviders(new ArrayList<>());
        for (CodeActionNodeType nodeType : CodeActionNodeType.values()) {
            BallerinaCodeActionProviderFactory.getNodeBasedProviders().put(nodeType, new ArrayList<>());
        }
        for (BallerinaCodeActionProvider codeAction : serviceLoader) {
            if (codeAction.isNodeBased()) {
                for (CodeActionNodeType nodeType : codeAction.
                        getCodeActionNodeTypes()) {

                    if (nodeType == (CodeActionNodeType.FUNCTION)) {
                        nodeBasedProviders.get(CodeActionNodeType.FUNCTION).add(codeAction);

                    } else if (nodeType == (CodeActionNodeType.OBJECT)) {
                        nodeBasedProviders.get(CodeActionNodeType.OBJECT).add(codeAction);

                    } else if (nodeType == (CodeActionNodeType.SERVICE)) {
                        nodeBasedProviders.get(CodeActionNodeType.SERVICE).add(codeAction);

                    } else if (nodeType == (CodeActionNodeType.RECORD)) {
                        nodeBasedProviders.get(CodeActionNodeType.RECORD).add(codeAction);

                    } else if (nodeType == (CodeActionNodeType.RESOURCE)) {
                        nodeBasedProviders.get(CodeActionNodeType.RESOURCE).add(codeAction);

                    } else if (nodeType == (CodeActionNodeType.OBJECT_FUNCTION)) {
                        nodeBasedProviders.get(CodeActionNodeType.OBJECT_FUNCTION).add(codeAction);
                    }
                }
            } else if (!codeAction.isNodeBased()) {
                BallerinaCodeActionProviderFactory.getDiagnosticsBasedProviders().add(codeAction);
            }
        }
        isInitialized = true;
    }
}

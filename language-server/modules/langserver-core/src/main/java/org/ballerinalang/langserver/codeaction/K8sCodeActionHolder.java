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
package org.ballerinalang.langserver.codeaction;

import org.ballerinalang.langserver.codeaction.extensions.K8sDiagnosticsBasedCodeAction;
import org.ballerinalang.langserver.codeaction.providers.kubernetes.InvalidLivenessPort;
import org.ballerinalang.langserver.codeaction.providers.kubernetes.InvalidLivenessResource;
import org.ballerinalang.langserver.codeaction.providers.kubernetes.InvalidLivenessServicePath;
import org.ballerinalang.langserver.codeaction.providers.kubernetes.InvalidReadinessPort;
import org.ballerinalang.langserver.codeaction.providers.kubernetes.InvalidReadinessResource;
import org.ballerinalang.langserver.codeaction.providers.kubernetes.InvalidReadinessServicePath;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the holder for Kubernetes related Code Action providers.
 *
 * @since 2.0.0
 */
public class K8sCodeActionHolder {
    private static final List<K8sDiagnosticsBasedCodeAction> codeActionList = Arrays.asList(
            new InvalidLivenessPort(),
            new InvalidLivenessResource(),
            new InvalidLivenessServicePath(),
            new InvalidReadinessPort(),
            new InvalidReadinessResource(),
            new InvalidReadinessServicePath());

    private K8sCodeActionHolder() {
    }

    public static List<K8sDiagnosticsBasedCodeAction> getAllCodeActions() {
        return codeActionList;
    }
}

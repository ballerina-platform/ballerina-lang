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
package org.ballerinalang.langserver.codeaction.providers.kubernetes;

import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.toml.Probe;
import org.ballerinalang.langserver.toml.ProbeStore;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Code Action for handling Invalid Readiness Service Path in Kubernetes.toml.
 *
 * @since 2.0.0
 */
public class InvalidReadinessServicePath extends AbstractInvalidServiceCodeAction {

    @Override
    public boolean validate(Diagnostic diagnostic, CodeActionContext ctx) {
        return diagnostic.getMessage().equals("Invalid Readiness Probe Service Path");
    }

    @Override
    public List<CodeAction> handle(Diagnostic diagnostic, CodeActionContext ctx) {
        ProbeStore store = super.getProbe(ctx);
        Optional<Probe> readinessProbe = store.getReadiness();
        return readinessProbe.map(probe -> fixServicePath(diagnostic, ctx, probe)).orElseGet(ArrayList::new);
    }
}

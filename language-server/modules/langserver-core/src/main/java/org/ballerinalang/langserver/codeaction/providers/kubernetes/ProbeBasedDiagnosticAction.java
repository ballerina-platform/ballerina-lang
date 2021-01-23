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
package org.ballerinalang.langserver.codeaction.providers.kubernetes;

import io.ballerina.toml.syntax.tree.DocumentNode;
import io.ballerina.toml.syntax.tree.SyntaxTree;
import org.ballerinalang.langserver.codeaction.extensions.K8sDiagnosticsBasedCodeAction;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.toml.ProbeStore;
import org.ballerinalang.langserver.toml.TomlProbesVisitor;

/**
 * Abstract class for handling probe related code actions.
 *
 * @since 2.0.0
 */
public abstract class ProbeBasedDiagnosticAction implements K8sDiagnosticsBasedCodeAction {

    protected ProbeStore getProbe(CodeActionContext ctx) {
        //Code Actions gets called only from Kubernetes.toml in a project.
        SyntaxTree syntaxTree = ctx.workspace().project(ctx.filePath()).orElseThrow().currentPackage().kubernetesToml()
                .orElseThrow().tomlDocument().syntaxTree();
        DocumentNode node = syntaxTree.rootNode();
        TomlProbesVisitor probesVisitor = new TomlProbesVisitor();
        node.accept(probesVisitor);
        return probesVisitor.getStore();
    }
}

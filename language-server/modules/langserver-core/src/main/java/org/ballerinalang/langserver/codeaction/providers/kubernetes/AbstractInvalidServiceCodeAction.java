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

import io.ballerina.projects.Project;
import org.ballerinalang.langserver.codeaction.toml.ProjectServiceInfo;
import org.ballerinalang.langserver.codeaction.toml.ProjectServiceInfoHolder;
import org.ballerinalang.langserver.codeaction.toml.ServiceInfo;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.toml.Probe;
import org.ballerinalang.langserver.toml.TomlSyntaxTreeUtil;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract class for handling Invalid service path related code actions.
 *
 * @since 2.0.0
 */
public abstract class AbstractInvalidServiceCodeAction extends ProbeBasedDiagnosticAction {

    public List<CodeAction> fixServicePath(Diagnostic diagnostic, CodeActionContext ctx, Probe probe) {
        Optional<Project> project = ctx.workspace().project(ctx.filePath());
        ProjectServiceInfo projectServiceInfo =
                ProjectServiceInfoHolder.getInstance(ctx.languageServercontext()).getProjectInfo(project.orElseThrow());
        Map<String, List<ServiceInfo>> serviceList = projectServiceInfo.getServiceMap();
        List<CodeAction> codeActionList = new ArrayList<>();
        for (List<ServiceInfo> serviceInfos : serviceList.values()) {
            // TODO: Listener Exists No attatched service -> Generate a service using the listener
            for (ServiceInfo service : serviceInfos) {
                int port = service.getListener().getPort();
                if (probe.getPort().getValue() == port) {
                    String servicePath = "/" + TomlSyntaxTreeUtil.trimResourcePath(service.getServiceName());
                    io.ballerina.toml.syntax.tree.Node node = probe.getPath().getNode();
                    Position startingPos = new Position(node.lineRange().startLine().line(),
                            node.lineRange().startLine().offset());
                    Position endingPos = new Position(node.lineRange().endLine().line(),
                            node.lineRange().endLine().offset());

                    CodeAction action = new CodeAction();
                    action.setKind(CodeActionKind.QuickFix);

                    TextEdit removeContent = new TextEdit(new Range(startingPos, endingPos), "");
                    TextEdit addContent = new TextEdit(new Range(startingPos, startingPos), servicePath);
                    List<TextEdit> edits = new ArrayList<>();
                    edits.add(removeContent);
                    edits.add(addContent);

                    action.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
                            new TextDocumentEdit(new VersionedTextDocumentIdentifier(ctx.fileUri(), null),
                                    edits)))));
                    action.setTitle("Modify service path");
                    List<Diagnostic> cursorDiagnostics = new ArrayList<>();
                    cursorDiagnostics.add(diagnostic);
                    action.setDiagnostics(cursorDiagnostics);
                    codeActionList.add(action);
                    break;
                }
            }
        }

        return codeActionList;
    }
}

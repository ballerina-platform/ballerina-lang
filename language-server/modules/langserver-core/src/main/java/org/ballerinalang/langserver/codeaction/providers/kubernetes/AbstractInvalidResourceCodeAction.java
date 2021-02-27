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

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.projects.Project;
import org.ballerinalang.langserver.codeaction.toml.ProjectServiceInfo;
import org.ballerinalang.langserver.codeaction.toml.ProjectServiceInfoHolder;
import org.ballerinalang.langserver.codeaction.toml.ServiceInfo;
import org.ballerinalang.langserver.common.utils.CommonUtil;
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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Abstract class for handling Invalid resource related code actions.
 *
 * @since 2.0.0
 */
public abstract class AbstractInvalidResourceCodeAction extends ProbeBasedDiagnosticAction {

    public List<CodeAction> addResourceToService(Diagnostic diagnostic, CodeActionContext ctx, Probe probe) {
        Optional<Project> project = ctx.workspace().project(ctx.filePath());
        ProjectServiceInfo projectServiceInfo =
                ProjectServiceInfoHolder.getInstance(ctx.languageServercontext()).getProjectInfo(project.orElseThrow());
        Map<String, List<ServiceInfo>> serviceList = projectServiceInfo.getServiceMap();
        List<CodeAction> codeActionList = new ArrayList<>();
        for (Map.Entry<String, List<ServiceInfo>> entry : serviceList.entrySet()) {
            String filePath = entry.getKey();
            List<ServiceInfo> serviceInfos = entry.getValue();
            for (ServiceInfo service : serviceInfos) {
                int port = service.getListener().getPort();
                if (probe.getPort().getValue() == port) {
                    Path balFilePath = ctx.workspace().projectRoot(ctx.filePath()).resolve(filePath);
                    NodeList<Node> members = service.getNode().members();
                    Node lastResource = members.get(members.size() - 1);
                    Position position = new Position(lastResource.lineRange().endLine().line() + 1, 0);

                    CodeAction action = new CodeAction();
                    action.setKind(CodeActionKind.QuickFix);
                    String importText = generateProbeFunctionText(service, probe);
                    List<TextEdit> edits = Collections.singletonList(
                            new TextEdit(new Range(position, position), importText));
                    action.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
                            new TextDocumentEdit(
                                    new VersionedTextDocumentIdentifier(balFilePath.toUri().toString(), null),
                                    edits)))));
                    action.setTitle("Add Resource to Service");
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

    private String generateProbeFunctionText(ServiceInfo service, Probe probe) {
        String tomlPath = TomlSyntaxTreeUtil.trimResourcePath(probe.getPath().getValue());
        String serviceName = TomlSyntaxTreeUtil.trimResourcePath(service.getServiceName());
        String serviceResourcePath;
        if (tomlPath.equals(serviceName)) {
            serviceResourcePath = ".";
        } else if (serviceName.equals("")) {
            serviceResourcePath = tomlPath;
        } else if (tomlPath.startsWith(serviceName)) {
            serviceResourcePath = tomlPath.substring(serviceName.length() + 1);
        } else {
            serviceResourcePath = tomlPath;
        }
        return "    resource function get " + serviceResourcePath +
                " (http:Caller caller) returns error? {" + CommonUtil.LINE_SEPARATOR +
                "        check caller->respond(\"Resource is Ready\");" + CommonUtil.LINE_SEPARATOR +
                "    }" + CommonUtil.LINE_SEPARATOR;
    }

}

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

import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.toml.Probe;
import org.ballerinalang.langserver.toml.TomlSyntaxTreeUtil;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CreateFile;
import org.eclipse.lsp4j.CreateFileOptions;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Abstract class for handling Invalid port related code actions.
 *
 * @since 2.0.0
 */
public abstract class AbstractInvalidPortCodeAction extends ProbeBasedDiagnosticAction {

    protected List<CodeAction> handleInvalidPort(Diagnostic diagnostic, CodeActionContext ctx, Probe probe) {
        List<CodeAction> codeActionList = new ArrayList<>();

        String tomlPath = probe.getPath().getValue();
        int tomlPort = probe.getPort().getValue();
        String resourcePath = "readyz";
        String servicePath = "/probes";
        if (!(tomlPath == null || tomlPath.equals(""))) {
            String trimmedResourcePath = TomlSyntaxTreeUtil.trimResourcePath(tomlPath);
            String[] split = trimmedResourcePath.split("/");
            resourcePath = split[split.length - 1];
            servicePath = "/" + String.join("/", Arrays.copyOf(split, split.length - 1));
        }
        Path balFilePath = ctx.workspace().projectRoot(ctx.filePath()).resolve("probe.bal");
        if (Files.exists(balFilePath)) {
            io.ballerina.compiler.syntax.tree.SyntaxTree syntaxTree =
                    ctx.workspace().syntaxTree(balFilePath).orElseThrow();
            ModulePartNode modulePartNode = syntaxTree.rootNode();
            NodeList<ModuleMemberDeclarationNode> members = modulePartNode.members();
            ModuleMemberDeclarationNode lastTopLevelNode = members.get(members.size() - 1);
            Position position = new Position(lastTopLevelNode.lineRange().endLine().line() + 1, 0);
            String content = getProbeServiceString(tomlPort, servicePath, resourcePath);
            List<TextEdit> edits = Collections.singletonList(
                    new TextEdit(new Range(position, position), content));

            CodeAction action = new CodeAction("Add to Probe file");
            action.setKind(CodeActionKind.QuickFix);
            action.setEdit(new WorkspaceEdit(Collections.singletonList(Either.forLeft(
                    new TextDocumentEdit(
                            new VersionedTextDocumentIdentifier(balFilePath.toUri().toString(), null),
                            edits)))));
            List<Diagnostic> cursorDiagnostics = new ArrayList<>();
            cursorDiagnostics.add(diagnostic);
            action.setDiagnostics(cursorDiagnostics);
            codeActionList.add(action);
        } else {
            String content = getProbeServiceWithImportString(tomlPort, servicePath, resourcePath);
            codeActionList.add(createFile("Create Probe file", balFilePath.toUri().toString(), content, diagnostic));
        }
        return codeActionList;
    }

    private String getProbeServiceWithImportString(int port, String servicePath, String resourcePath) {
        return String.format("import ballerina/http;%s%s",
                CommonUtil.LINE_SEPARATOR, getProbeServiceString(port, servicePath, resourcePath));
    }

    private String getProbeServiceString(int port, String servicePath, String resourcePath) {
        return String.format("%sservice http:Service %s on new http:Listener(%d) {%s    resource " +
                        "function get %s (http:Caller caller) returns error? {%s        check caller->respond" +
                        "(\"Resource is Ready\");%s    }%s}%s",
                CommonUtil.LINE_SEPARATOR, servicePath, port, CommonUtil.LINE_SEPARATOR, resourcePath,
                CommonUtil.LINE_SEPARATOR, CommonUtil.LINE_SEPARATOR, CommonUtil.LINE_SEPARATOR,
                CommonUtil.LINE_SEPARATOR);
    }

    /**
     * Makes a CodeAction to create a file and add content to the file.
     *
     * @param title      The displayed name of the CodeAction
     * @param docURI     The file to create
     * @param content    The text to put into the newly created document.
     * @param diagnostic The diagnostic that this CodeAction will fix
     */
    private CodeAction createFile(String title, String docURI, String content, Diagnostic diagnostic) {

        List<Either<TextDocumentEdit, ResourceOperation>> actionsToTake = new ArrayList<>(2);

        // 1. create an empty file
        actionsToTake.add(Either.forRight(new CreateFile(docURI, new CreateFileOptions(false, true))));

        // 2. update the created file with the given content
        VersionedTextDocumentIdentifier identifier = new VersionedTextDocumentIdentifier(docURI, 0);
        TextEdit te = new TextEdit(new Range(new Position(0, 0), new Position(0, 0)), content);
        actionsToTake.add(Either.forLeft(new TextDocumentEdit(identifier, Collections.singletonList(te))));

        WorkspaceEdit createAndAddContentEdit = new WorkspaceEdit(actionsToTake);

        CodeAction codeAction = new CodeAction(title);
        codeAction.setEdit(createAndAddContentEdit);
        codeAction.setDiagnostics(Collections.singletonList(diagnostic));
        codeAction.setKind(CodeActionKind.QuickFix);

        return codeAction;
    }
}

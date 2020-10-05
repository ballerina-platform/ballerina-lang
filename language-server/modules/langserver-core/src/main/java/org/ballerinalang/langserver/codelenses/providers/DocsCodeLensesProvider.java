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
package org.ballerinalang.langserver.codelenses.providers;

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.Token;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.tools.text.LineRange;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.command.executors.AddDocumentationExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codelenses.LSCodeLensesProviderException;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.config.LSClientConfigHolder;
import org.eclipse.lsp4j.CodeLens;
import org.eclipse.lsp4j.Command;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Code lenses provider for adding all documentation for top level items.
 *
 * @since 0.990.3
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codelenses.spi.LSCodeLensesProvider")
public class DocsCodeLensesProvider extends AbstractCodeLensesProvider {
    public DocsCodeLensesProvider() {
        super("docs.CodeLenses");
        LSClientConfigHolder.getInstance().register((oldConfig, newConfig) -> {
            isEnabled = newConfig.getCodeLens().getDocs().isEnabled();
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeLens> getLenses(LSContext context) throws LSCodeLensesProviderException {
        List<CodeLens> lenses = new ArrayList<>();
        String documentUri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        try {
            Optional<Path> filePath = CommonUtil.getPathFromURI(documentUri);
            if (!filePath.isPresent()) {
                return new ArrayList<>();
            }
            WorkspaceDocumentManager documentManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
            SyntaxTree syntaxTree = documentManager.getTree(filePath.get());
            for (ModuleMemberDeclarationNode member : ((ModulePartNode) syntaxTree.rootNode()).members()) {
                Position nodeTopMostPos = null;
                int line = 0;
                boolean showLens = false;
                switch (member.kind()) {
                    case FUNCTION_DEFINITION:
                        FunctionDefinitionNode funcDef = (FunctionDefinitionNode) member;
                        String nodeName = funcDef.functionName().text();
                        for (Token qualifier : funcDef.qualifierList()) {
                            if (qualifier.kind() == SyntaxKind.PUBLIC_KEYWORD && !"main".equals(nodeName)) {
                                showLens = true;
                                break;
                            }
                        }
                        nodeTopMostPos = CommonUtil.toRange(funcDef.lineRange()).getStart();
                        line = nodeTopMostPos.getLine();
                        if (funcDef.metadata().isPresent() && !funcDef.metadata().get().annotations().isEmpty()) {
                            LineRange topAnnotRange = funcDef.metadata().get().annotations().get(0).lineRange();
                            nodeTopMostPos = CommonUtil.toRange(topAnnotRange).getStart();
                        }
                        break;
                    case TYPE_DEFINITION:
                        TypeDefinitionNode typeDef = (TypeDefinitionNode) member;
                        showLens = typeDef.visibilityQualifier()
                                .map(s -> s.kind() == SyntaxKind.PUBLIC_KEYWORD)
                                .orElse(false);
                        nodeTopMostPos = CommonUtil.toRange(typeDef.lineRange()).getStart();
                        line = nodeTopMostPos.getLine();
                        if (typeDef.metadata().isPresent() && !typeDef.metadata().get().annotations().isEmpty()) {
                            LineRange topAnnotRange = typeDef.metadata().get().annotations().get(0).lineRange();
                            nodeTopMostPos = CommonUtil.toRange(topAnnotRange).getStart();
                        }
                        break;
                    case CLASS_DEFINITION:
                        ClassDefinitionNode classDef = (ClassDefinitionNode) member;
                        showLens = classDef.visibilityQualifier()
                                .map(s -> s.kind() == SyntaxKind.PUBLIC_KEYWORD)
                                .orElse(false);
                        nodeTopMostPos = CommonUtil.toRange(classDef.lineRange()).getStart();
                        line = nodeTopMostPos.getLine();
                        if (classDef.metadata().isPresent() && !classDef.metadata().get().annotations().isEmpty()) {
                            LineRange topAnnotRange = classDef.metadata().get().annotations().get(0).lineRange();
                            nodeTopMostPos = CommonUtil.toRange(topAnnotRange).getStart();
                        }
                        break;
                    case SERVICE_DECLARATION:
                        ServiceDeclarationNode serviceDeclr = (ServiceDeclarationNode) member;
                        showLens = false;
                        nodeTopMostPos = CommonUtil.toRange(serviceDeclr.lineRange()).getStart();
                        line = nodeTopMostPos.getLine();
                        if (serviceDeclr.metadata().isPresent() &&
                                !serviceDeclr.metadata().get().annotations().isEmpty()) {
                            LineRange topAnnotRange = serviceDeclr.metadata().get().annotations().get(0).lineRange();
                            nodeTopMostPos = CommonUtil.toRange(topAnnotRange).getStart();
                        }
                        break;
                    default:
                        break;
                }
                if (showLens) {
                    CommandArgument docUriArg = new CommandArgument(CommandConstants.ARG_KEY_DOC_URI, documentUri);
                    CommandArgument lineStart = new CommandArgument(CommandConstants.ARG_KEY_NODE_LINE,
                                                                    String.valueOf(line));
                    List<Object> args = new ArrayList<>(Arrays.asList(docUriArg, lineStart));
                    Command command = new Command(CommandConstants.ADD_DOCUMENTATION_TITLE,
                                                  AddDocumentationExecutor.COMMAND, args);
                    lenses.add(new CodeLens(new Range(nodeTopMostPos, nodeTopMostPos), command, null));
                }
            }
        } catch (
                WorkspaceDocumentException e) {
            throw new LSCodeLensesProviderException("Error when processing the 'add all documentation' code lens", e);
        }

        return lenses;
    }
}

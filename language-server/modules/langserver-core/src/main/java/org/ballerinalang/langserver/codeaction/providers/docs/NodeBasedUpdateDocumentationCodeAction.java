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
package org.ballerinalang.langserver.codeaction.providers.docs;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Documentable;
import io.ballerina.compiler.api.symbols.Documentation;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.command.docs.DocAttachmentInfo;
import org.ballerinalang.langserver.command.executors.UpdateDocumentationExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedCodeActionProvider;
import org.ballerinalang.langserver.commons.codeaction.spi.RangeBasedPositionDetails;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.langserver.command.docs.DocumentationGenerator.getDocumentableSymbol;
import static org.ballerinalang.langserver.command.docs.DocumentationGenerator.getDocumentationEditForNode;
import static org.ballerinalang.langserver.command.docs.DocumentationGenerator.hasDocs;

/**
 * Code Action for updating single documentation.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class NodeBasedUpdateDocumentationCodeAction implements RangeBasedCodeActionProvider {

    public static final String NAME = "Update Documentation";

    public List<SyntaxKind> getSyntaxKinds() {
        return Arrays.asList(SyntaxKind.FUNCTION_DEFINITION,
                SyntaxKind.OBJECT_TYPE_DESC,
                SyntaxKind.CLASS_DEFINITION,
                SyntaxKind.SERVICE_DECLARATION,
                SyntaxKind.RESOURCE_ACCESSOR_DEFINITION,
                SyntaxKind.RECORD_TYPE_DESC,
                SyntaxKind.METHOD_DECLARATION,
                SyntaxKind.OBJECT_METHOD_DEFINITION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(CodeActionContext context,
                                           RangeBasedPositionDetails posDetails) {
        String docUri = context.fileUri();
        Optional<NonTerminalNode> matchedDocumentableNode = posDetails.enclosingDocumentableNode();

        if (matchedDocumentableNode.isEmpty()
                || !hasDocs(matchedDocumentableNode.get())
                || !hasMismatch(context, matchedDocumentableNode.get())) {
            return Collections.emptyList();
        }

        CommandArgument docUriArg = CommandArgument.from(CommandConstants.ARG_KEY_DOC_URI, docUri);
        CommandArgument lineStart = CommandArgument.from(CommandConstants.ARG_KEY_NODE_RANGE, PositionUtil
                .toRange(matchedDocumentableNode.get().lineRange()));
        List<Object> args = new ArrayList<>(Arrays.asList(docUriArg, lineStart));

        CodeAction action = new CodeAction(CommandConstants.UPDATE_DOCUMENTATION_TITLE);
        Command command = new Command(CommandConstants.UPDATE_DOCUMENTATION_TITLE, UpdateDocumentationExecutor.COMMAND,
                args);
        action.setCommand(command);
        action.setKind(CodeActionKind.Refactor);
        return Collections.singletonList(action);
    }

    private boolean hasMismatch(CodeActionContext context, NonTerminalNode node) {
        SemanticModel semanticModel = context.workspace().semanticModel(context.filePath()).orElseThrow();
        Optional<Symbol> documentableSymbol = getDocumentableSymbol(node, semanticModel);
        SyntaxTree syntaxTree = context.workspace().syntaxTree(context.filePath()).orElseThrow();
        Optional<DocAttachmentInfo> docAttachmentInfo = getDocumentationEditForNode(node, syntaxTree);

        if (docAttachmentInfo.isEmpty()) {
            return false;
        }

        DocAttachmentInfo docs = docAttachmentInfo.get();
        if (documentableSymbol.isEmpty()) {
            return false;
        }

        Optional<Documentation> symbolDocumentation = ((Documentable) documentableSymbol.get()).documentation();
        if (symbolDocumentation.isEmpty()) {
            return false;
        }

        Documentation documentation = symbolDocumentation.get();
        docs = docs.mergeDocAttachment(documentation);

        return !documentation.description().equals(docs.description()) ||
                !documentation.parameterMap().equals(docs.parameterMap()) ||
                !documentation.returnDescription().equals(docs.returnDescription()) ||
                !documentation.deprecatedDescription().equals(docs.deprecatedDescription());
    }

    @Override
    public String getName() {
        return NAME;
    }
}

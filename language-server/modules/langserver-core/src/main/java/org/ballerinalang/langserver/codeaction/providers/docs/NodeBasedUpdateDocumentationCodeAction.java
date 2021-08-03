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
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.providers.AbstractCodeActionProvider;
import org.ballerinalang.langserver.command.docs.DocAttachmentInfo;
import org.ballerinalang.langserver.command.executors.UpdateDocumentationExecutor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.NodeBasedPositionDetails;
import org.ballerinalang.langserver.commons.command.CommandArgument;
import org.eclipse.lsp4j.CodeAction;
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
public class NodeBasedUpdateDocumentationCodeAction extends AbstractCodeActionProvider {

    public static final String NAME = "Update Documentation";

    public NodeBasedUpdateDocumentationCodeAction() {
        super(Arrays.asList(CodeActionNodeType.FUNCTION,
                CodeActionNodeType.OBJECT,
                CodeActionNodeType.CLASS,
                CodeActionNodeType.SERVICE,
                CodeActionNodeType.RESOURCE,
                CodeActionNodeType.RECORD,
                CodeActionNodeType.OBJECT_FUNCTION,
                CodeActionNodeType.CLASS_FUNCTION));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionContext context,
                                                    NodeBasedPositionDetails posDetails) {
        String docUri = context.fileUri();
        NonTerminalNode matchedNode = posDetails.matchedTopLevelNode();

        if (!hasDocs(matchedNode) || !hasMismatch(context, matchedNode)) {
            return Collections.emptyList();
        }

        CommandArgument docUriArg = CommandArgument.from(CommandConstants.ARG_KEY_DOC_URI, docUri);
        CommandArgument lineStart = CommandArgument.from(CommandConstants.ARG_KEY_NODE_RANGE,
                                                         CommonUtil.toRange(matchedNode.lineRange()));
        List<Object> args = new ArrayList<>(Arrays.asList(docUriArg, lineStart));

        CodeAction action = new CodeAction(CommandConstants.UPDATE_DOCUMENTATION_TITLE);        
        Command command = new Command(CommandConstants.UPDATE_DOCUMENTATION_TITLE, UpdateDocumentationExecutor.COMMAND, 
                args);
        action.setCommand(command);
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
        
        if (!documentation.description().equals(docs.description()) || 
                !documentation.parameterMap().equals(docs.parameterMap()) || 
                !documentation.returnDescription().equals(docs.returnDescription()) || 
                !documentation.deprecatedDescription().equals(docs.deprecatedDescription())) {
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return NAME;
    }
}

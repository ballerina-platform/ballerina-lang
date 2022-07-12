/*
 *  Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.RecordTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.langserver.LSClientLogger;
import org.ballerinalang.langserver.LSContextOperation;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.NameUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.spi.NodeBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The code action to extract anonymous records into type defs.
 *
 * @since 2201.1.1
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ExtractTypeCodeAction extends AbstractCodeActionProvider {

    private static final String RECORD_NAME_PREFIX = "Record";

    public ExtractTypeCodeAction() {
        super(List.of(CodeActionNodeType.RECORD));
    }

    @Override
    public List<CodeAction> getNodeBasedCodeActions(CodeActionContext context, NodeBasedPositionDetails posDetails) {
        if (posDetails.matchedCodeActionNode().kind() != SyntaxKind.RECORD_TYPE_DESC) {
            return Collections.emptyList();
        }

        RecordTypeDescriptorNode node = (RecordTypeDescriptorNode) posDetails.matchedCodeActionNode();
        if (node.parent().kind() == SyntaxKind.TYPE_DEFINITION || context.currentSyntaxTree().isEmpty()) {
            return Collections.emptyList();
        }

        Node tlNode = node;
        while (tlNode.parent().kind() != SyntaxKind.MODULE_PART) {
            tlNode = tlNode.parent();
        }

        LinePosition lastTypeDefPosition = tlNode.lineRange().endLine();
        SyntaxTree syntaxTree = context.currentSyntaxTree().get();
        ModulePartNode modulePartNode = syntaxTree.rootNode();
        for (ModuleMemberDeclarationNode member : modulePartNode.members()) {
            if (member.kind() == SyntaxKind.TYPE_DEFINITION) {
                lastTypeDefPosition = member.lineRange().endLine();
            }
        }

        Set<String> visibleSymbolNames = context.visibleSymbols(context.cursorPosition()).stream()
                .map(Symbol::getName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        String typeName = NameUtil.generateTypeName(RECORD_NAME_PREFIX, visibleSymbolNames);

        Range extractedRecordRange = new Range(PositionUtil.toPosition(lastTypeDefPosition),
                PositionUtil.toPosition(lastTypeDefPosition));
        Range originalNodeRange = new Range(PositionUtil.toPosition(node.lineRange().startLine()),
                PositionUtil.toPosition(node.lineRange().endLine()));

        // Create type def text
        String typeDesc = String.format("type %s %s;", typeName, node.toSourceCode());
        try {
            typeDesc = Formatter.format(typeDesc);
        } catch (FormatterException e) {
            LSClientLogger.getInstance(context.languageServercontext()).logError(LSContextOperation.TXT_CODE_ACTION,
                    "Failed to format extracted source: " + typeDesc, e, null, (Position) null);
            return Collections.emptyList();
        }

        typeDesc = String.format("%n%n%s", typeDesc);
        
        TextEdit typeDescEdit = new TextEdit(extractedRecordRange, typeDesc);
        TextEdit replaceEdit = new TextEdit(originalNodeRange, typeName);
        CodeAction codeAction = createCodeAction(CommandConstants.EXTRACT_TYPE, List.of(typeDescEdit, replaceEdit),
                context.fileUri(), CodeActionKind.RefactorExtract);
        return List.of(codeAction);
    }

    @Override
    public String getName() {
        return "ExtractType";
    }
}

/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.apache.commons.lang3.StringUtils;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.FunctionGenerator;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.common.utils.CommonUtil.LINE_SEPARATOR;

/**
 * Code Action for implementing functions of an object.
 *
 * @since 1.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ImplementFunctionCodeAction extends AbstractCodeActionProvider {
    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(Diagnostic diagnostic,
                                                    DiagBasedPositionDetails positionDetails,
                                                    CodeActionContext context) {
        if (!(diagnostic.message().startsWith(CommandConstants.NO_IMPL_FOUND_FOR_METHOD))) {
            return Collections.emptyList();
        }

        String methodName;
        Matcher matcher = CommandConstants.NO_IMPL_FOUND_FOR_FUNCTION_PATTERN.matcher(diagnostic.message());
        if (matcher.find() && matcher.groupCount() > 1) {
            methodName = matcher.group(1);
        } else {
            return Collections.emptyList();
        }

        Node matchedNode = positionDetails.matchedNode();
        Symbol matchedSymbol = positionDetails.matchedSymbol();
        if (!(matchedNode.kind() == SyntaxKind.CLASS_DEFINITION && matchedSymbol.kind() == SymbolKind.CLASS)) {
            return Collections.emptyList();
        }
        ClassDefinitionNode classDefNode = (ClassDefinitionNode) matchedNode;
        ClassSymbol classSymbol = (ClassSymbol) matchedSymbol;

        if (!classSymbol.methods().containsKey(methodName)) {
            return Collections.emptyList();
        }

        MethodSymbol unimplMethod = classSymbol.methods().get(methodName);
        List<FunctionDefinitionNode> concreteMethods = classDefNode.members().stream()
                .filter(member -> member.kind() == SyntaxKind.FUNCTION_DEFINITION)
                .map(member -> (FunctionDefinitionNode) member)
                .collect(Collectors.toList());

        String offsetStr;
        if (!concreteMethods.isEmpty()) {
            // If other methods exists, inherit offset
            FunctionDefinitionNode funcDefNode = concreteMethods.get(0);
            offsetStr = StringUtils.repeat(' ', funcDefNode.location().lineRange().endLine().offset());
        } else {
            // Or else, adjust offset according to the parent class
            offsetStr = StringUtils.repeat(' ', classDefNode.location().lineRange().startLine().offset() + 4);
        }

        ImportsAcceptor importsAcceptor = new ImportsAcceptor(context);
        String typeName = FunctionGenerator.processModuleIDsInText(importsAcceptor, unimplMethod.signature(), context);
        List<TextEdit> edits = new ArrayList<>(importsAcceptor.getNewImportTextEdits());
        String editText = offsetStr + typeName + " {" + LINE_SEPARATOR + offsetStr + "}" + LINE_SEPARATOR;
        Position editPos = CommonUtil.toPosition(classDefNode.closeBrace().lineRange().startLine());
        edits.add(new TextEdit(new Range(editPos, editPos), editText));
        String commandTitle = String.format(CommandConstants.IMPLEMENT_FUNCS_TITLE, unimplMethod.getName().get());
        CodeAction quickFixCodeAction = createQuickFixCodeAction(commandTitle, edits, context.fileUri());
        quickFixCodeAction.setDiagnostics(CodeActionUtil.toDiagnostics(Collections.singletonList((diagnostic))));
        return Collections.singletonList(quickFixCodeAction);
    }
}

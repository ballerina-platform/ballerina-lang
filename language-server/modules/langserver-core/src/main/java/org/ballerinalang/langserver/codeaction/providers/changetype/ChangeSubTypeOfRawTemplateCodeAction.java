/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
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
package org.ballerinalang.langserver.codeaction.providers.changetype;

import io.ballerina.compiler.api.Types;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionContextTypeResolver;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagnosticBasedCodeActionProvider;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/***
 * 
 * Code Action for adding subtype for a raw template.
 * @since 2201.2.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ChangeSubTypeOfRawTemplateCodeAction implements DiagnosticBasedCodeActionProvider {

    public static final String NAME = "Change subtype of a raw template";
    public static final Set<String> DIAGNOSTIC_CODES = Set.of("BCE3936");

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails, 
                            CodeActionContext context) {
        return DIAGNOSTIC_CODES.contains(diagnostic.diagnosticInfo().code()) &&
                CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic,
                                           DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {

        if (context.currentSemanticModel().isEmpty()) {
            return Collections.emptyList();
        }

        TemplateExpressionNode templateExpressionNode = (TemplateExpressionNode) positionDetails.matchedNode();
        Optional<TypeSymbol> typeSymbol = templateExpressionNode.apply(new CodeActionContextTypeResolver(context));

        Set<String> typeSet = new HashSet<>();
        if (typeSymbol.isPresent()) {
            if (typeSymbol.get().typeKind() == TypeDescKind.UNION) {
                UnionTypeSymbol unionTypeSymbol = (UnionTypeSymbol) typeSymbol.get();
                Types types = context.currentSemanticModel().get().types();
                if (unionTypeSymbol.memberTypeDescriptors().contains(types.STRING)) {
                    typeSet.add("string");
                }
                if (unionTypeSymbol.memberTypeDescriptors().contains(types.XML)) {
                    typeSet.add("xml");
                }
            } else if (typeSymbol.get().typeKind() == TypeDescKind.STRING) {
                typeSet.add("string");
            } else if (typeSymbol.get().typeKind() == TypeDescKind.XML) {
                typeSet.add("xml");
            } else {
                typeSet.addAll(Set.of("string", "xml"));
            }
        } else {
            typeSet.addAll(Set.of("string", "xml"));
        }

        List<CodeAction> codeActions = new ArrayList<>();
        LinePosition backtickStart = templateExpressionNode.lineRange().startLine();
        LinePosition backtickEnd = templateExpressionNode.lineRange().endLine();
        Position start = new Position(backtickStart.line(), backtickStart.offset());
        Position end = new Position(backtickEnd.line(), backtickEnd.offset());

        typeSet.forEach(type -> {
            List<TextEdit> edits = new ArrayList<>();
            String editText = type + " " + templateExpressionNode.toSourceCode();
            edits.add(new TextEdit(new Range(start, end), editText));
            
            String commandTitle = String.format(CommandConstants.CHANGE_TO_SUBTYPE_OF_RAW_TEMPLATE_TITLE, type);
            codeActions.add(CodeActionUtil.createCodeAction(commandTitle, edits, context.fileUri(),
                    CodeActionKind.QuickFix));
        });

        return codeActions;
    }

    @Override
    public String getName() {
        return NAME;
    }
}

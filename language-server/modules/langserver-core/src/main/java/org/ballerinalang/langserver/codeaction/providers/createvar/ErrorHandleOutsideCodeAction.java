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
package org.ballerinalang.langserver.codeaction.providers.createvar;

import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Qualifiable;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.projects.Module;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.CodeActionNodeValidator;
import org.ballerinalang.langserver.codeaction.CodeActionUtil;
import org.ballerinalang.langserver.common.ImportsAcceptor;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.common.utils.PositionUtil;
import org.ballerinalang.langserver.commons.CodeActionContext;
import org.ballerinalang.langserver.commons.codeaction.spi.DiagBasedPositionDetails;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Code Action for error type handle.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class ErrorHandleOutsideCodeAction extends CreateVariableCodeAction {

    public static final String NAME = "Error Handle Outside";

    /**
     * {@inheritDoc}
     */
    @Override
    public int priority() {

        return 998;
    }

    @Override
    public boolean validate(Diagnostic diagnostic, DiagBasedPositionDetails positionDetails,
                            CodeActionContext context) {

        return diagnostic.message().contains(CommandConstants.VAR_ASSIGNMENT_REQUIRED) &&
                CodeActionNodeValidator.validate(context.nodeAtRange());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getCodeActions(Diagnostic diagnostic,
                                           DiagBasedPositionDetails positionDetails,
                                           CodeActionContext context) {

        String uri = context.fileUri();

        Optional<TypeSymbol> typeSymbol = getExpectedTypeSymbol(positionDetails);
        if (typeSymbol.isEmpty() || typeSymbol.get().typeKind() != TypeDescKind.UNION
                || isCompilationErrorTyped((UnionTypeSymbol) typeSymbol.get())) {
            return Collections.emptyList();
        }
        UnionTypeSymbol unionTypeDesc = (UnionTypeSymbol) typeSymbol.get();
        List<TypeSymbol> errorMemberTypes = CommonUtil.extractErrorTypesFromUnion(unionTypeDesc);
        Path path = Path.of(URI.create(uri.replace("expr:///", "file:///")));
        Optional<Module> module = context.workspace().module(path);
        if (module.isPresent() &&
                containsModuleLevelPrivateTypes(module.get().moduleName().toString(), errorMemberTypes)) {
            return Collections.emptyList();
        }
        long nonErrorNonNilMemberCount = unionTypeDesc.memberTypeDescriptors().stream()
                .filter(member -> CommonUtil.getRawType(member).typeKind() != TypeDescKind.ERROR
                        && member.typeKind() != TypeDescKind.NIL)
                .count();
        if (errorMemberTypes.isEmpty() || nonErrorNonNilMemberCount == 0) {
            return Collections.emptyList();
        }
        ImportsAcceptor importsAcceptor = new ImportsAcceptor(context);
        CreateVariableOut modifiedTextEdits = getModifiedCreateVarTextEdits(diagnostic, unionTypeDesc, 
                positionDetails, typeSymbol.get(), context, importsAcceptor);
        List<TextEdit> edits = new ArrayList<>(modifiedTextEdits.edits);
        List<TextEdit> addCheckTextEdits = CodeActionUtil.getAddCheckTextEdits(
                PositionUtil.toRange(diagnostic.location().lineRange()).getStart(),
                positionDetails.matchedNode(), context, errorMemberTypes, importsAcceptor);
        edits.addAll(addCheckTextEdits);
        edits.addAll(importsAcceptor.getNewImportTextEdits());

        CodeAction codeAction = CodeActionUtil.createCodeAction(CommandConstants.CREATE_VAR_ADD_CHECK_TITLE,
                edits, uri, CodeActionKind.QuickFix);
        addRenamePopup(context, codeAction, modifiedTextEdits.varRenamePosition.get(0),
                modifiedTextEdits.imports.size());
        return Collections.singletonList(codeAction);
    }
    
    private static boolean containsModuleLevelPrivateTypes(String currentModule, List<TypeSymbol> errorMemberTypes) {
        for (TypeSymbol errorMemType : errorMemberTypes) {
            if (errorMemType.typeKind() != TypeDescKind.TYPE_REFERENCE) {
                continue;
            }
            Optional<ModuleSymbol> module = errorMemType.getModule();
            if (module.isEmpty() || currentModule.equals(module.get().id().moduleName())) {
                continue;
            }
            Symbol typeDef = ((TypeReferenceTypeSymbol) errorMemType).definition();
            if (typeDef instanceof Qualifiable qualifiable && !qualifiable.qualifiers().contains(Qualifier.PUBLIC)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {

        return NAME;
    }

    private CreateVariableOut getModifiedCreateVarTextEdits(Diagnostic diagnostic,
                                                            UnionTypeSymbol unionTypeDesc,
                                                            DiagBasedPositionDetails positionDetails,
                                                            TypeSymbol typeSymbol,
                                                            CodeActionContext context,
                                                            ImportsAcceptor importsAcceptor) {

        // Add create variable edits
        Range range = PositionUtil.toRange(diagnostic.location().lineRange());
        CreateVariableOut createVarTextEdits = getCreateVariableTextEdits(range, positionDetails, typeSymbol,
                context, importsAcceptor);

        // Change and add type text edit
        String typeWithError = createVarTextEdits.types.get(0);
        String typeWithoutError = getTypeWithoutError(unionTypeDesc, context, importsAcceptor);

        int lengthDiff = typeWithError.length() - typeWithoutError.length();

        Position varRenamePosition = createVarTextEdits.varRenamePosition.get(0);
        varRenamePosition.setCharacter(varRenamePosition.getCharacter() - lengthDiff);

        Integer renamePos = createVarTextEdits.renamePositions.get(0);
        createVarTextEdits.renamePositions.add(0, renamePos - lengthDiff);

        TextEdit textEdit = createVarTextEdits.edits.get(0);
        textEdit.setNewText(typeWithoutError + textEdit.getNewText().substring(typeWithError.length()));
        return createVarTextEdits;
    }

    private String getTypeWithoutError(UnionTypeSymbol unionTypeDesc, CodeActionContext context,
                                       ImportsAcceptor importsAcceptor) {

        return unionTypeDesc.memberTypeDescriptors().stream()
                .filter(member -> CommonUtil.getRawType(member).typeKind() != TypeDescKind.ERROR)
                .map(typeDesc -> CodeActionUtil.getPossibleType(typeDesc, context, importsAcceptor).orElseThrow())
                .collect(Collectors.joining("|"));
    }
}

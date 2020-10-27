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
package org.ballerinalang.langserver.codeaction.providers;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.impl.BallerinaSemanticModel;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.api.types.FunctionTypeDescriptor;
import io.ballerina.compiler.api.types.TypeDescKind;
import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.tools.text.LinePosition;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.langserver.codeaction.impl.CreateVariableCodeAction;
import org.ballerinalang.langserver.codeaction.impl.DiagBasedCodeAction;
import org.ballerinalang.langserver.codeaction.impl.ErrorTypeCodeAction;
import org.ballerinalang.langserver.codeaction.impl.IgnoreReturnCodeAction;
import org.ballerinalang.langserver.codeaction.impl.TypeGuardCodeAction;
import org.ballerinalang.langserver.common.constants.CommandConstants;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.codeaction.LSCodeActionProviderException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Names;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Code Action provider for variable assignment.
 *
 * @since 2.0.0
 */
@JavaSPIService("org.ballerinalang.langserver.commons.codeaction.spi.LSCodeActionProvider")
public class VariableAssignmentRequiredCodeActionProvider extends AbstractCodeActionProvider {

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CodeAction> getDiagBasedCodeActions(CodeActionNodeType nodeType, LSContext context,
                                                    List<Diagnostic> diagnosticsOfRange,
                                                    List<Diagnostic> allDiagnostics) {
        List<CodeAction> actions = new ArrayList<>();
        Optional<Path> filePath = CommonUtil.getPathFromURI(context.get(DocumentServiceKeys.FILE_URI_KEY));
        if (filePath.isEmpty()) {
            return actions;
        }
        for (Diagnostic diagnostic : diagnosticsOfRange) {
            String diagnosticMsg = diagnostic.getMessage().toLowerCase(Locale.ROOT);
            if (!(diagnosticMsg.contains(CommandConstants.VAR_ASSIGNMENT_REQUIRED))) {
                continue;
            }
            try {
                // Find Cursor node
                Position diagPos = diagnostic.getRange().getStart();
                NonTerminalNode cursorNode = CommonUtil.findNode(context, diagPos, filePath.get());
                final Symbol[] scopedSymbol = {null};
                final NonTerminalNode[] scopedNode = {null};
                final Optional<BallerinaTypeDescriptor>[] optTypeDesc = new Optional[]{Optional.empty()};

                BLangPackage bLangPackage = context.get(DocumentServiceKeys.CURRENT_BLANG_PACKAGE_CONTEXT_KEY);
                CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
                String relPath = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
                SemanticModel semanticModel = new BallerinaSemanticModel(bLangPackage, compilerContext);

                //TODO: Remove this when #26382 is implemented
                Optional<BallerinaTypeDescriptor> literalTypeDesc = checkForLiteralTypeDesc(cursorNode, semanticModel,
                                                                                            relPath);
                if (literalTypeDesc.isPresent()) {
                    // If it is a Literal, use the temp type-descriptor
                    scopedNode[0] = cursorNode;
                    optTypeDesc[0] = literalTypeDesc;
                } else {
                    // Or else, use the scoped-symbol's type-descriptor
                    getScopedNodeAndSymbol(cursorNode, diagnostic.getRange(), semanticModel, relPath)
                            .ifPresent(nodeAndSymbol -> {
                                scopedNode[0] = nodeAndSymbol.getLeft();
                                scopedSymbol[0] = nodeAndSymbol.getRight();
                                optTypeDesc[0] = getTypeDescriptor(scopedSymbol[0]);
                            });
                }

                if (optTypeDesc[0].isEmpty()) {
                    return actions;
                }

                // Initialize Code Actions
                BallerinaTypeDescriptor typeDesc = optTypeDesc[0].get();
                DiagBasedCodeAction createVariable = new CreateVariableCodeAction(typeDesc, scopedSymbol[0]);
                DiagBasedCodeAction errorType = new ErrorTypeCodeAction(typeDesc, scopedSymbol[0]);
                DiagBasedCodeAction ignoreReturn = new IgnoreReturnCodeAction(typeDesc);
                DiagBasedCodeAction typeGuard = new TypeGuardCodeAction(typeDesc, scopedNode[0], scopedSymbol[0]);

                // Can result multiple code-actions since RHS is ambiguous
                actions.addAll(createVariable.get(diagnostic, allDiagnostics, context));
                actions.addAll(errorType.get(diagnostic, allDiagnostics, context));
                actions.addAll(ignoreReturn.get(diagnostic, allDiagnostics, context));
                actions.addAll(typeGuard.get(diagnostic, allDiagnostics, context));
            } catch (LSCodeActionProviderException | WorkspaceDocumentException e) {
                // ignore
            }
        }
        return actions;
    }

    private Optional<Pair<NonTerminalNode, Symbol>> getScopedNodeAndSymbol(NonTerminalNode cursorNode,
                                                                           Range diagRange,
                                                                           SemanticModel semanticModel,
                                                                           String relPath) {

        // Find invocation position
        InvocationPositionFinder positionFinder = new InvocationPositionFinder(diagRange);
        positionFinder.visit(cursorNode);
        if (positionFinder.getNode().isEmpty() || positionFinder.getPosition().isEmpty()) {
            return Optional.empty();
        }
        // Get Symbol of the position
        LinePosition position = positionFinder.getPosition().get();
        LinePosition scopedNodePos = LinePosition.from(position.line(), position.offset() + 1);
        Optional<Symbol> optScopedSymbol = semanticModel.symbol(relPath, scopedNodePos);
        if (optScopedSymbol.isEmpty()) {
            return Optional.empty();
        }
        // Get TypeDesc of the symbol
        Symbol scopedSymbol = optScopedSymbol.get();
        NonTerminalNode scopedNode = positionFinder.getNode().get();
        return Optional.of(new ImmutablePair<>(scopedNode, scopedSymbol));
    }

    private Optional<BallerinaTypeDescriptor> checkForLiteralTypeDesc(NonTerminalNode cursorNode,
                                                                      SemanticModel semanticModel,
                                                                      String relPath) {
        TypeDescKind typeDescKind = null;
        ModuleID moduleID = null;
        String definitionName = "";
        switch (cursorNode.kind()) {
            // Simple Literal
            case NUMERIC_LITERAL:
                BasicLiteralNode basicLiteralNode = (BasicLiteralNode) cursorNode;
                SyntaxKind literalTokenKind = basicLiteralNode.literalToken().kind();
                if (literalTokenKind == SyntaxKind.DECIMAL_INTEGER_LITERAL_TOKEN ||
                        literalTokenKind == SyntaxKind.HEX_INTEGER_LITERAL_TOKEN) {
                    typeDescKind = TypeDescKind.INT;
                } else if (literalTokenKind == SyntaxKind.DECIMAL_FLOATING_POINT_LITERAL_TOKEN) {
                    String text = basicLiteralNode.literalToken().text();
                    char c = text.charAt(text.length() - 1);
                    typeDescKind = (c == 'd' || c == 'D') ? TypeDescKind.DECIMAL : TypeDescKind.FLOAT;
                } else if (literalTokenKind == SyntaxKind.HEX_FLOATING_POINT_LITERAL_TOKEN) {
                    typeDescKind = TypeDescKind.FLOAT;
                }
                break;
            case BOOLEAN_LITERAL:
                typeDescKind = TypeDescKind.BOOLEAN;
                break;
            case STRING_LITERAL:
            case TEMPLATE_STRING:
            case XML_TEXT_CONTENT:
            case IDENTIFIER_TOKEN:
                typeDescKind = TypeDescKind.STRING;
                break;
            case NIL_LITERAL:
            case NULL_LITERAL:
                typeDescKind = TypeDescKind.NIL;
                break;
            case BINARY_EXPRESSION:
            case BYTE_ARRAY_LITERAL:
                typeDescKind = TypeDescKind.BYTE;
                break;
            // Other literals
            case XML_TEMPLATE_EXPRESSION:
                typeDescKind = TypeDescKind.TYPE_REFERENCE;
                definitionName = "Element";
                moduleID = new TempModuleID("ballerina", "lang", "xml");
                break;
            default:
                break;
        }
        if (typeDescKind == null) {
            return Optional.empty();
        }

        definitionName = (definitionName.isEmpty()) ? typeDescKind.getName() : definitionName;
        return Optional.of(new TempTypeDescriptor(definitionName, typeDescKind, moduleID));
    }

    private Optional<BallerinaTypeDescriptor> getTypeDescriptor(Symbol scopedSymbol) {
        switch (scopedSymbol.kind()) {
            case FUNCTION: {
                FunctionSymbol functionSymbol = (FunctionSymbol) scopedSymbol;
                FunctionTypeDescriptor funTypeDesc = functionSymbol.typeDescriptor();
                return funTypeDesc.returnTypeDescriptor();
            }
            case METHOD: {
                MethodSymbol methodSymbol = (MethodSymbol) scopedSymbol;
                FunctionTypeDescriptor funTypeDesc = methodSymbol.typeDescriptor();
                return funTypeDesc.returnTypeDescriptor();
            }
            case VARIABLE: {
                return Optional.of(((VariableSymbol) scopedSymbol).typeDescriptor());
            }
            case TYPE: {
                return Optional.of(((TypeSymbol) scopedSymbol).typeDescriptor());
            }
        }
        return Optional.empty();
    }

    private static class TempTypeDescriptor implements BallerinaTypeDescriptor {
        private static final String ANON_ORG = "$anon";
        private final TypeDescKind typeDescKind;
        private final ModuleID moduleID;
        private final String definitionName;

        TempTypeDescriptor(String definitionName, TypeDescKind typeDescKind, ModuleID moduleID) {
            this.typeDescKind = typeDescKind;
            this.moduleID = moduleID;
            this.definitionName = definitionName;
        }

        @Override
        public TypeDescKind kind() {
            return typeDescKind;
        }

        @Override
        public ModuleID moduleID() {
            return moduleID;
        }

        @Override
        public String signature() {
            if (moduleID == null || (moduleID.moduleName().equals("lang.annotations") &&
                    moduleID.orgName().equals("ballerina"))) {
                return definitionName;
            }
            return !ANON_ORG.equals(moduleID.orgName()) ? moduleID.orgName() + Names.ORG_NAME_SEPARATOR +
                    moduleID.moduleName() + Names.VERSION_SEPARATOR + moduleID.version() + ":" +
                    definitionName : definitionName;
        }

        @Override
        public List<MethodSymbol> builtinMethods() {
            return Collections.emptyList();
        }
    }

    private static class TempModuleID implements ModuleID {
        private final String orgName;
        private final String[] nameComps;
        private final String version;

        TempModuleID(String orgName, String... nameComps) {
            this.orgName = orgName;
            this.nameComps = nameComps;
            this.version = "0.0.0";
        }

        @Override
        public String orgName() {
            return orgName;
        }

        @Override
        public String moduleName() {
            return String.join(".", nameComps);
        }

        @Override
        public String version() {
            return version;
        }

        @Override
        public String modulePrefix() {
            return nameComps[nameComps.length - 1];
        }
    }

    ;
}

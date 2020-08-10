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
package org.ballerinalang.langserver.codeaction;

import io.ballerinalang.compiler.syntax.tree.Token;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.codeaction.CodeActionKeys;
import org.ballerinalang.langserver.commons.codeaction.CodeActionNodeType;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.CollectDiagnosticListener;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.ballerinalang.langserver.util.TokensUtil;
import org.ballerinalang.langserver.util.references.ReferencesUtil;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel;
import org.ballerinalang.langserver.util.references.TokenOrSymbolNotFoundException;
import org.ballerinalang.model.elements.Flag;
import org.ballerinalang.model.tree.TopLevelNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangService;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.tree.types.BLangRecordTypeNode;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Code Action related Utils.
 *
 * @since 1.0.1
 */
public class CodeActionUtil {
    private static final Logger logger = LoggerFactory.getLogger(CodeActionUtil.class);

    private CodeActionUtil() {
    }

    /**
     * Get the top level node type at the cursor line.
     *
     * @param identifier Document Identifier
     * @param cursorLine Cursor line
     * @param docManager Workspace document manager
     * @return {@link String}   Top level node type
     */
    public static CodeActionNodeType topLevelNodeInLine(LSContext context, TextDocumentIdentifier identifier,
                                                        int cursorLine, WorkspaceDocumentManager docManager) {
        Optional<Path> filePath = CommonUtil.getPathFromURI(identifier.getUri());
        if (!filePath.isPresent()) {
            return null;
        }

        try {
            BLangPackage bLangPackage = LSModuleCompiler.getBLangPackage(context, docManager,
                    null, false, false, true);
            String relativeSourcePath = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
            BLangPackage evalPkg = CommonUtil.getSourceOwnerBLangPackage(relativeSourcePath, bLangPackage);

            List<Diagnostic> diagnostics = new ArrayList<>();
            CompilerContext compilerContext = context.get(DocumentServiceKeys.COMPILER_CONTEXT_KEY);
            if (compilerContext.get(DiagnosticListener.class) instanceof CollectDiagnosticListener) {
                diagnostics = ((CollectDiagnosticListener) compilerContext.get(DiagnosticListener.class))
                        .getDiagnostics();
            }
            context.put(CodeActionKeys.DIAGNOSTICS_KEY, CodeActionUtil.toDiagnostics(diagnostics));

            Optional<BLangCompilationUnit> filteredCUnit = evalPkg.compUnits.stream()
                    .filter(cUnit -> cUnit.getPosition().getSource()
                            .cUnitName.replace("/", CommonUtil.FILE_SEPARATOR)
                            .equals(relativeSourcePath))
                    .findAny();

            if (!filteredCUnit.isPresent()) {
                return null;
            }

            for (TopLevelNode topLevelNode : filteredCUnit.get().getTopLevelNodes()) {
                DiagnosticPos diagnosticPos = CommonUtil.toZeroBasedPosition(((BLangNode) topLevelNode).pos);
                if (topLevelNode instanceof BLangService) {
                    if (diagnosticPos.sLine == cursorLine) {
                        return CodeActionNodeType.SERVICE;
                    }
                    if (cursorLine > diagnosticPos.sLine && cursorLine < diagnosticPos.eLine) {
                        // Cursor within the service
                        for (BLangFunction resourceFunction : ((BLangService) topLevelNode).resourceFunctions) {
                            diagnosticPos = CommonUtil.toZeroBasedPosition(resourceFunction.getName().pos);
                            if (diagnosticPos.sLine == cursorLine) {
                                return CodeActionNodeType.RESOURCE;
                            }
                        }
                    }
                }

                if (topLevelNode instanceof BLangImportPackage && cursorLine == diagnosticPos.sLine) {
                    return CodeActionNodeType.IMPORTS;
                }

                if (topLevelNode instanceof BLangFunction
                        && !((BLangFunction) topLevelNode).flagSet.contains(Flag.ANONYMOUS)
                        && cursorLine == diagnosticPos.sLine) {
                    return CodeActionNodeType.FUNCTION;
                }

                if (topLevelNode instanceof BLangTypeDefinition
                        && ((BLangTypeDefinition) topLevelNode).typeNode instanceof BLangRecordTypeNode
                        && cursorLine == diagnosticPos.sLine) {
                    return CodeActionNodeType.RECORD;
                }
                if (topLevelNode instanceof BLangTypeDefinition
                        && ((BLangTypeDefinition) topLevelNode).typeNode instanceof BLangObjectTypeNode) {
                    if (diagnosticPos.sLine == cursorLine) {
                        return CodeActionNodeType.OBJECT;
                    }
                    if (cursorLine > diagnosticPos.sLine && cursorLine < diagnosticPos.eLine) {
                        // Cursor within the object
                        for (BLangFunction resourceFunction
                                : ((BLangObjectTypeNode) ((BLangTypeDefinition) topLevelNode).typeNode).functions) {
                            diagnosticPos = CommonUtil.toZeroBasedPosition(resourceFunction.getName().pos);
                            if (diagnosticPos.sLine == cursorLine) {
                                return CodeActionNodeType.OBJECT_FUNCTION;
                            }
                        }
                    }
                }
            }
            return null;
        } catch (CompilationFailedException e) {
            logger.error("Error while compiling the source");
            return null;
        }
    }

    /**
     * Translates ballerina diagnostics into lsp4j diagnostics.
     *
     * @param ballerinaDiags a list of {@link org.ballerinalang.util.diagnostic.Diagnostic}
     * @return a list of {@link Diagnostic}
     */
    public static List<org.eclipse.lsp4j.Diagnostic> toDiagnostics(
            List<org.ballerinalang.util.diagnostic.Diagnostic> ballerinaDiags) {
        List<org.eclipse.lsp4j.Diagnostic> lsDiagnostics = new ArrayList<>();
        ballerinaDiags.forEach(diagnostic -> {
            org.eclipse.lsp4j.Diagnostic lsDiagnostic = new org.eclipse.lsp4j.Diagnostic();
            lsDiagnostic.setSeverity(DiagnosticSeverity.Error);
            lsDiagnostic.setMessage(diagnostic.getMessage());
            Range range = new Range();

            int startLine = diagnostic.getPosition().getStartLine() - 1; // LSP diagnostics range is 0 based
            int startChar = diagnostic.getPosition().getStartColumn() - 1;
            int endLine = diagnostic.getPosition().getEndLine() - 1;
            int endChar = diagnostic.getPosition().getEndColumn() - 1;

            if (endLine <= 0) {
                endLine = startLine;
            }

            if (endChar <= 0) {
                endChar = startChar + 1;
            }

            range.setStart(new Position(startLine, startChar));
            range.setEnd(new Position(endLine, endChar));
            lsDiagnostic.setRange(range);

            lsDiagnostics.add(lsDiagnostic);
        });

        return lsDiagnostics;
    }

    /**
     * Get the Symbol at the Cursor.
     *
     * @param context  LS Operation Context
     * @param document LS Document
     * @param position Cursor Position
     * @return Symbol reference at cursor
     * @throws WorkspaceDocumentException when couldn't find file for uri
     * @throws CompilationFailedException when compilation failed
     */
    public static SymbolReferencesModel.Reference getSymbolAtCursor(LSContext context, LSDocumentIdentifier document,
                                                                    Position position)
            throws WorkspaceDocumentException, CompilationFailedException,
            TokenOrSymbolNotFoundException {
        TextDocumentIdentifier textDocIdentifier = new TextDocumentIdentifier(document.getURIString());
        TextDocumentPositionParams pos = new TextDocumentPositionParams(textDocIdentifier, position);
        context.put(DocumentServiceKeys.POSITION_KEY, pos);
        context.put(DocumentServiceKeys.FILE_URI_KEY, document.getURIString());
        context.put(DocumentServiceKeys.COMPILE_FULL_PROJECT, true);

        List<BLangPackage> modules = ReferencesUtil.compileModules(context);
        context.put(DocumentServiceKeys.BLANG_PACKAGES_CONTEXT_KEY, modules);
        return findSymbolAtCursor(modules, context);
    }

    private static SymbolReferencesModel.Reference findSymbolAtCursor(List<BLangPackage> modules, LSContext context)
            throws WorkspaceDocumentException, TokenOrSymbolNotFoundException {
        String currentPkgName = context.get(DocumentServiceKeys.CURRENT_PKG_NAME_KEY);
        /*
        In windows platform, relative file path key components are separated with "\" while antlr always uses "/"
         */
        String relativePath = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        String currentCUnitName = relativePath.replace("\\", "/");
        Optional<BLangPackage> currentPkg = modules.stream()
                .filter(pkg -> pkg.symbol.getName().getValue().equals(currentPkgName))
                .findAny();

        if (!currentPkg.isPresent()) {
            throw new UserErrorException("Not supported due to compilation failures!");
        }

        BLangPackage sourceOwnerPkg = CommonUtil.getSourceOwnerBLangPackage(relativePath, currentPkg.get());

        Optional<BLangCompilationUnit> currentCUnit = sourceOwnerPkg.getCompilationUnits().stream()
                .filter(cUnit -> cUnit.name.equals(currentCUnitName))
                .findAny();

        if (!currentCUnit.isPresent()) {
            throw new UserErrorException("Not supported due to compilation failures!");
        }

        // With the syntax-tree, find the cursor token
        Position position = context.get(DocumentServiceKeys.POSITION_KEY).getPosition();
        Token tokenAtCursor = TokensUtil.findTokenAtPosition(context, position);

        CursorSymbolFindingVisitor refVisitor = new CursorSymbolFindingVisitor(tokenAtCursor, context,
                currentPkgName, true);
        SymbolReferencesModel symbolReferencesModel = refVisitor.accept(currentCUnit.get());
        return symbolReferencesModel.getReferenceAtCursor();
    }
}

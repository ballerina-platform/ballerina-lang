/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.util.references;

import io.ballerina.compiler.syntax.tree.Token;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.CompletionKeys;
import org.ballerinalang.langserver.commons.workspace.LSDocumentIdentifier;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSModuleCompiler;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.exception.UserErrorException;
import org.ballerinalang.langserver.hover.HoverUtil;
import org.ballerinalang.langserver.util.TokensUtil;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel.Reference;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.ProjectDirConstants;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.compiler.LSCompilerUtil.getUntitledFilePath;

/**
 * Utility class for go to definition functionality of language server.
 */
public class ReferencesUtil {
    private ReferencesUtil() {
    }

    /**
     * Get the Reference at the Cursor.
     *
     * @param context  LS Operation Context
     * @param document LS Document
     * @param position Cursor Position
     * @return Symbol reference at cursor
     * @throws WorkspaceDocumentException when couldn't find file for uri
     * @throws CompilationFailedException when compilation failed
     */
    public static Reference getReferenceAtCursor(LSContext context, LSDocumentIdentifier document,
                                                 Position position)
            throws WorkspaceDocumentException, CompilationFailedException, TokenOrSymbolNotFoundException {
        TextDocumentIdentifier textDocIdentifier = new TextDocumentIdentifier(document.getURIString());
        TextDocumentPositionParams pos = new TextDocumentPositionParams(textDocIdentifier, position);
        context.put(DocumentServiceKeys.POSITION_KEY, pos);
        context.put(DocumentServiceKeys.FILE_URI_KEY, document.getURIString());
        context.put(DocumentServiceKeys.COMPILE_FULL_PROJECT, true);
        Token tokenAtCursor = TokensUtil.findTokenAtPosition(context, position);
        List<BLangPackage> modules = ReferencesUtil.compileModules(context);
        SymbolReferencesModel referencesModel = findReferencesForCurrentCUnit(tokenAtCursor, modules, context);
        context.put(DocumentServiceKeys.BLANG_PACKAGES_CONTEXT_KEY, modules);
        return referencesModel.getReferenceAtCursor();
    }

    /**
     * Get the rename workspace edits.
     *
     * @param context  Language server context
     * @param newName  New name to replace
     * @param position cursor position
     * @return {@link WorkspaceEdit}    Rename workspace edit
     * @throws WorkspaceDocumentException when couldn't find file for uri
     * @throws CompilationFailedException when compilation failed
     */
    public static WorkspaceEdit getRenameWorkspaceEdits(LSContext context, String newName, Position position)
            throws WorkspaceDocumentException, CompilationFailedException, TokenOrSymbolNotFoundException {
        Token tokenAtCursor = TokensUtil.findTokenAtPosition(context, position);
        List<BLangPackage> modules = compileModules(context);
        String nodeName = tokenAtCursor.text();
        if (CommonKeys.NEW_KEYWORD_KEY.equals(nodeName)) {
            throw new TokenOrSymbolNotFoundException(
                    "Symbol at cursor '" + nodeName + "' not supported or could not find!");
        }
        SymbolReferencesModel referencesModel = findReferencesForCurrentCUnit(tokenAtCursor, modules, context);
        SymbolReferencesModel allReferences = fillAllReferences(referencesModel, tokenAtCursor, modules, context);
        return getWorkspaceEdit(allReferences, context, newName);
    }

    public static List<Location> getReferences(LSContext context, boolean includeDeclaration, Position pos)
            throws WorkspaceDocumentException, CompilationFailedException, TokenOrSymbolNotFoundException {
        Token tokenAtCursor = TokensUtil.findTokenAtPosition(context, pos);
        List<BLangPackage> modules = compileModules(context);
        SymbolReferencesModel referencesModel = findReferencesForCurrentCUnit(tokenAtCursor, modules, context);
        SymbolReferencesModel allReferencesModel = fillAllReferences(referencesModel, tokenAtCursor, modules, context);
        List<Reference> references = new ArrayList<>();
        if (includeDeclaration) {
            references.addAll(allReferencesModel.getDefinitions());
        }
        references.addAll(allReferencesModel.getReferences());
        if (!allReferencesModel.getDefinitions().contains(allReferencesModel.getReferenceAtCursor())) {
            references.add(allReferencesModel.getReferenceAtCursor());
        }

        return getLocations(references, context.get(DocumentServiceKeys.SOURCE_ROOT_KEY));
    }

    /**
     * Get the hover content.
     *
     * @param context        Hover operation context
     * @param cursorPosition Cursor position
     * @return {@link Hover} Hover content
     * @throws WorkspaceDocumentException when couldn't find file for uri
     * @throws CompilationFailedException when compilation failed
     */
    public static Hover getHover(LSContext context, Position cursorPosition)
            throws WorkspaceDocumentException, CompilationFailedException, TokenOrSymbolNotFoundException {
        Token tokenAtCursor = TokensUtil.findTokenAtPosition(context, cursorPosition);
        context.put(CompletionKeys.TOKEN_AT_CURSOR_KEY, tokenAtCursor);
        List<BLangPackage> modules = compileModules(context);
        Reference symbolAtCursor = findReferencesForCurrentCUnit(tokenAtCursor, modules, context)
                .getReferenceAtCursor();

        // Ignore the optional check since it has been handled during prepareReference and throws exception
        BSymbol bSymbol = symbolAtCursor.getSymbol();
        return bSymbol != null
                ? HoverUtil.getHoverFromDocAttachment(HoverUtil.getMarkdownDocForSymbol(bSymbol), bSymbol, context)
                : HoverUtil.getDefaultHoverObject();
    }

    public static List<BLangPackage> compileModules(LSContext context) throws CompilationFailedException {
        String fileUri = context.get(DocumentServiceKeys.FILE_URI_KEY);
        WorkspaceDocumentManager docManager = context.get(DocumentServiceKeys.DOC_MANAGER_KEY);
        Boolean compileProject = context.get(DocumentServiceKeys.COMPILE_FULL_PROJECT);
        Optional<Path> defFilePath = CommonUtil.getPathFromURI(fileUri);
        if (!defFilePath.isPresent()) {
            return new ArrayList<>();
        }
        Path compilationPath = getUntitledFilePath(defFilePath.toString()).orElse(defFilePath.get());
        Optional<Lock> lock = docManager.lockFile(compilationPath);
        try {
            return LSModuleCompiler.getBLangPackages(context, docManager, compileProject, false, false);
        } finally {
            lock.ifPresent(Lock::unlock);
        }
    }

    private static SymbolReferencesModel fillAllReferences(SymbolReferencesModel referencesModel, Token tokenAtCursor,
                                                           List<BLangPackage> modules, LSContext context) {
        Reference symbolAtCursor = referencesModel.getReferenceAtCursor();

        // Ignore the optional check since it has been handled during prepareReference and throws exception
        String symbolOwnerPkg = symbolAtCursor.getSymbol().pkgID.toString();

        modules.forEach(bLangPackage -> {
            List<String> imports = bLangPackage.getImports().stream()
                    .map(bLangImportPackage -> bLangImportPackage.symbol.pkgID.toString())
                    .collect(Collectors.toList());
            if (!symbolOwnerPkg.equals(bLangPackage.packageID.toString()) && !imports.contains(symbolOwnerPkg)) {
                return;
            }

            for (BLangCompilationUnit compilationUnit : bLangPackage.getCompilationUnits()) {
                // Possible Reference tokens found within the cUnit
                String symbolPkgName = bLangPackage.symbol.getName().value;
                SymbolReferenceFindingVisitor refVisitor = new SymbolReferenceFindingVisitor(context,
                        tokenAtCursor,
                        symbolPkgName);
                SymbolReferencesModel symbolReferencesModel = refVisitor.accept(compilationUnit);
                referencesModel.getDefinitions().addAll(symbolReferencesModel.getDefinitions());
                referencesModel.getReferences().addAll(symbolReferencesModel.getReferences());
            }
        });
        return referencesModel;
    }

    public static SymbolReferencesModel findReferencesForCurrentCUnit(Token tokenAtCursor,
                                                                      List<BLangPackage> modules, LSContext context)
            throws TokenOrSymbolNotFoundException {
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

        SymbolReferenceFindingVisitor refVisitor = new SymbolReferenceFindingVisitor(context, tokenAtCursor,
                currentPkgName, true);
        SymbolReferencesModel symbolReferencesModel = refVisitor.accept(currentCUnit.get());

        // Prune the found symbol references
        if (symbolReferencesModel.getReferenceAtCursor() == null) {
            String nodeName = tokenAtCursor.text();
            throw new TokenOrSymbolNotFoundException(
                    "Symbol at position '" + nodeName + "' not supported or could not find!");
        }

        Reference symbolAtCursor = symbolReferencesModel.getReferenceAtCursor();
        BSymbol cursorSymbol = symbolAtCursor.getSymbol();
        symbolReferencesModel.getDefinitions()
                .removeIf(reference -> reference.getSymbol() != cursorSymbol
                        && (reference.getSymbol().type.tsymbol != cursorSymbol));
        symbolReferencesModel.getReferences()
                .removeIf(reference -> reference.getSymbol() != cursorSymbol
                        && (reference.getSymbol().type.tsymbol != cursorSymbol));
        return symbolReferencesModel;
    }

    public static List<Location> getLocations(List<Reference> references, String sourceRoot) {
        return references.stream()
                .map(reference -> {
                    DiagnosticPos position = reference.getPosition();
                    Path baseRoot = reference.getSourcePkgName().equals(".")
                            ? Paths.get(sourceRoot)
                            : Paths.get(sourceRoot).resolve(ProjectDirConstants.SOURCE_DIR_NAME)
                            .resolve(reference.getSourcePkgName());
                    String fileURI = baseRoot.resolve(reference.getCompilationUnit()).toUri().toString();
                    return new Location(fileURI, getRange(position));
                })
                .collect(Collectors.toList());
    }

    private static WorkspaceEdit getWorkspaceEdit(SymbolReferencesModel referencesModel, LSContext context,
                                                  String newName) {
        WorkspaceEdit workspaceEdit = new WorkspaceEdit();
        Reference symbolAtCursor = referencesModel.getReferenceAtCursor();
        List<Reference> references = new ArrayList<>();
        references.add(symbolAtCursor);
        if (!referencesModel.getDefinitions().contains(symbolAtCursor)) {
            references.addAll(referencesModel.getDefinitions());
        }
        references.addAll(referencesModel.getReferences());
        LSDocumentIdentifier sourceDoc = context.get(DocumentServiceKeys.LS_DOCUMENT_KEY);

        references.forEach(reference -> {
            DiagnosticPos referencePos = reference.getPosition();
            String pkgName = reference.getSourcePkgName();
            String cUnitName = reference.getCompilationUnit();
            String uri;
            Path basePath = sourceDoc.getProjectRootPath();
            if (sourceDoc.isWithinProject()) {
                basePath = basePath.resolve("src").resolve(pkgName);
            }
            basePath = basePath.resolve(cUnitName);

            uri = basePath.toUri().toString();
            TextEdit textEdit = new TextEdit(getRange(referencePos), newName);
            if (workspaceEdit.getChanges().containsKey(uri)) {
                workspaceEdit.getChanges().get(uri).add(textEdit);
            } else {
                List<TextEdit> textEdits = new ArrayList<>();
                textEdits.add(textEdit);
                workspaceEdit.getChanges().put(uri, textEdits);
            }
        });

        return workspaceEdit;
    }

    private static Range getRange(DiagnosticPos referencePos) {
        Position start = new Position(referencePos.sLine, referencePos.sCol);
        Position end = new Position(referencePos.eLine, referencePos.eCol);
        return new Range(start, end);
    }
}

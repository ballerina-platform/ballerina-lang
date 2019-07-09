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

import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSCompiler;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.langserver.compiler.common.LSDocument;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.langserver.definition.LSReferencesException;
import org.ballerinalang.langserver.hover.util.HoverUtil;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import static org.ballerinalang.langserver.compiler.LSCompilerUtil.getUntitledFilePath;

/**
 * Utility class for go to definition functionality of language server.
 */
public class ReferencesUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReferencesUtil.class);

    private ReferencesUtil() {
    }

    public static List<BLangPackage> getPreparedModules(String fileUri, WorkspaceDocumentManager docManager,
                                                        LSCompiler lsCompiler, Position position, LSContext context,
                                                        boolean compileProject) {
        Path defFilePath = new LSDocument(fileUri).getPath();
        Path compilationPath = getUntitledFilePath(defFilePath.toString()).orElse(defFilePath);
        Optional<Lock> lock = docManager.lockFile(compilationPath);
        Class errStrategy = LSCustomErrorStrategy.class;
        try {
            context.put(DocumentServiceKeys.FILE_URI_KEY, fileUri);
            context.put(NodeContextKeys.REFERENCES_KEY, new SymbolReferencesModel());

            // With the sub-rule parser, find the token
            String documentContent = docManager.getFileContent(compilationPath);
            ReferencesSubRuleParser.parserCompilationUnit(documentContent, context, position);

            return lsCompiler.getBLangPackages(context, docManager, true, errStrategy, compileProject, false);
        } catch (Exception e) {
            if (CommonUtil.LS_DEBUG_ENABLED) {
                String msg = e.getMessage();
                LOGGER.error("Error while preparing modules for references" + ((msg != null) ? ": " + msg : ""), e);
            }
            return new ArrayList<>();
        } finally {
            lock.ifPresent(Lock::unlock);
        }
    }

    /**
     * Get the definition.
     *
     * @param modules   List of project modules
     * @param context   Definition context
     * @param position  Cursor Position
     * @return {@link List}     List of definition locations
     * @throws LSReferencesException    Exception while finding the references
     */
    public static List<Location> getDefinition(List<BLangPackage> modules, LSContext context, Position position)
            throws LSReferencesException {
        prepareReferences(modules, context, position);
        SymbolReferencesModel referencesModel = context.get(NodeContextKeys.REFERENCES_KEY);
        // If the definition list contains an item after the prepare reference mode, then return it.
        // In this case, definition is in the current compilation unit it self
        if (!referencesModel.getDefinitions().isEmpty()) {
            return getLocations(Collections.singletonList(referencesModel.getDefinitions().get(0)), context);
        }
        Optional<SymbolReferencesModel.Reference> symbolAtCursor = referencesModel.getSymbolAtCursor();
        // Ignore the optional check since it has been handled during prepareReference and throws exception
        String symbolPkgName = symbolAtCursor.get().getSymbolPkgName();
        Optional<BLangPackage> module = modules.stream()
                .filter(bLangPackage -> bLangPackage.symbol.getName().getValue().equals(symbolPkgName))
                .findAny();
        if (!module.isPresent()) {
            return new ArrayList<>();
        }
        for (BLangCompilationUnit compilationUnit : module.get().getCompilationUnits()) {
            SymbolReferenceFindingVisitor refVisitor = new SymbolReferenceFindingVisitor(context, symbolPkgName,
                    position);
            refVisitor.visit(compilationUnit);
            if (!referencesModel.getDefinitions().isEmpty()) {
                break;
            }
        }

        return getLocations(referencesModel.getDefinitions(), context);
    }

    /**
     * Get the rename workspace edits.
     *
     * @param modules   Build modules in project
     * @param context   Language server context
     * @param newName   New name to replace
     * @param position  Cursor position
     * @return {@link WorkspaceEdit}    Rename workspace edit
     * @throws LSReferencesException    Exception while finding the references
     */
    public static WorkspaceEdit getRenameWorkspaceEdits(List<BLangPackage> modules, LSContext context, String newName,
                                                        Position position) throws LSReferencesException {
        SymbolReferencesModel referencesModel = context.get(NodeContextKeys.REFERENCES_KEY);
        prepareReferences(modules, context, position);
        fillAllReferences(modules, context, position);
        return getWorkspaceEdit(referencesModel, context, newName);
    }

    public static List<Location> getReferences(List<BLangPackage> modules, LSContext context, Position position)
            throws LSReferencesException {
        SymbolReferencesModel referencesModel = context.get(NodeContextKeys.REFERENCES_KEY);
        prepareReferences(modules, context, position);
        fillAllReferences(modules, context, position);
        List<SymbolReferencesModel.Reference> references = new ArrayList<>(referencesModel.getDefinitions());
        references.addAll(referencesModel.getReferences());
        references.add(referencesModel.getSymbolAtCursor().get());

        return getLocations(references, context);
    }

    /**
     * Get the hover content.
     * 
     * @param modules Only the Current blang module is contained in the list
     * @param context Hover operation context
     * @param position Cursor position
     * @return {@link Hover} Hover content
     * @throws LSReferencesException Exception while extracting the hover content
     */
    public static Hover getHover(List<BLangPackage> modules, LSContext context, Position position)
            throws LSReferencesException {
        SymbolReferencesModel referencesModel = context.get(NodeContextKeys.REFERENCES_KEY);
        prepareReferences(modules, context, position);
        Optional<SymbolReferencesModel.Reference> symbolAtCursor = referencesModel.getSymbolAtCursor();

        // Ignore the optional check since it has been handled during prepareReference and throws exception
        BSymbol bSymbol = symbolAtCursor.get().getSymbol();
        return bSymbol != null
                ? HoverUtil.getHoverFromDocAttachment(HoverUtil.getMarkdownDocForSymbol(bSymbol), bSymbol)
                : HoverUtil.getDefaultHoverObject();
    }

    private static void fillAllReferences(List<BLangPackage> modules, LSContext context, Position position) {
        SymbolReferencesModel referencesModel = context.get(NodeContextKeys.REFERENCES_KEY);
        Optional<SymbolReferencesModel.Reference> symbolAtCursor = referencesModel.getSymbolAtCursor();
        // Ignore the optional check since it has been handled during prepareReference and throws exception
        String symbolOwnerPkg = symbolAtCursor.get().getSymbol().pkgID.toString();

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
                SymbolReferenceFindingVisitor refVisitor = new SymbolReferenceFindingVisitor(context, symbolPkgName,
                        position);
                refVisitor.visit(compilationUnit);
            }
        });
    }

    private static void prepareReferences(List<BLangPackage> modules, LSContext context, Position position)
            throws LSReferencesException {
        String currentPkgName = context.get(DocumentServiceKeys.CURRENT_PKG_NAME_KEY);
        /*
        In windows platform, relative file path key components are separated with "\" while antlr always uses "/"
         */
        String currentCUnitName = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY).replace("\\", "/");
        Optional<BLangPackage> currentPkg = modules.stream()
                .filter(pkg -> pkg.symbol.getName().getValue().equals(currentPkgName))
                .findAny();

        if (!currentPkg.isPresent()) {
            throw new LSReferencesException("Current module should be present");
        }

        Optional<BLangCompilationUnit> currentCUnit = currentPkg.get().getCompilationUnits().stream()
                .filter(cUnit -> cUnit.name.equals(currentCUnitName))
                .findAny();

        SymbolReferenceFindingVisitor refVisitor = new SymbolReferenceFindingVisitor(context, currentPkgName, position,
                true);

        refVisitor.visit(currentCUnit.get());

        // Prune the found symbol references
        SymbolReferencesModel symbolReferencesModel = context.get(NodeContextKeys.REFERENCES_KEY);
        if (!symbolReferencesModel.getSymbolAtCursor().isPresent()) {
            throw new LSReferencesException("Symbol Reference at Cursor is Empty");
        }

        SymbolReferencesModel.Reference symbolAtCursor = symbolReferencesModel.getSymbolAtCursor().get();
        symbolReferencesModel.getDefinitions()
                .removeIf(reference -> reference.getSymbol() != symbolAtCursor.getSymbol()
                        && (reference.getSymbol().type.tsymbol != symbolAtCursor.getSymbol()));
        symbolReferencesModel.getReferences()
                .removeIf(reference -> reference.getSymbol() != symbolAtCursor.getSymbol()
                        && (reference.getSymbol().type.tsymbol != symbolAtCursor.getSymbol()));
    }

    private static List<Location> getLocations(List<SymbolReferencesModel.Reference> references, LSContext context) {
        return references.stream().map(reference -> {
            DiagnosticPos position = reference.getPosition();
            String sourceRoot = context.get(DocumentServiceKeys.SOURCE_ROOT_KEY);
            Path baseRoot = reference.getSourcePkgName().equals(".") ? Paths.get(sourceRoot)
                    : Paths.get(sourceRoot).resolve(reference.getSourcePkgName());
            String fileURI = baseRoot.resolve(reference.getCompilationUnit()).toUri().toString();

            return new Location(fileURI, getRange(position));
        }).collect(Collectors.toList());
    }

    private static WorkspaceEdit getWorkspaceEdit(SymbolReferencesModel referencesModel, LSContext context,
                                                  String newName) {
        WorkspaceEdit workspaceEdit = new WorkspaceEdit();
        List<SymbolReferencesModel.Reference> references = new ArrayList<>(referencesModel.getDefinitions());
        references.addAll(referencesModel.getReferences());
        references.add(referencesModel.getSymbolAtCursor().get());
        String sourceRoot = context.get(DocumentServiceKeys.SOURCE_ROOT_KEY);

        references.forEach(reference -> {
            DiagnosticPos referencePos = reference.getPosition();
            String pkgName = reference.getSourcePkgName();
            String cUnitName = reference.getCompilationUnit();
            // If evaluating a single file which is not in a project/module, we skip adding the package name to root
            Path baseRoot = pkgName.equals(".") ? Paths.get(sourceRoot) : Paths.get(sourceRoot).resolve(pkgName);
            String uri = baseRoot.resolve(cUnitName).toUri().toString();
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

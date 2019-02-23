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
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.definition.LSReferencesException;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class for go to definition functionality of language server.
 */
public class ReferencesUtil {

    private ReferencesUtil() {
    }

    private static void prepareReferences(List<BLangPackage> modules, LSContext context, Position position)
            throws LSReferencesException {
        Map<String, Map<String, List<TokenReferenceModel>>> tokenRefs
                = context.get(NodeContextKeys.TOKEN_REFERENCES_KEY);
        String currentPkgName = context.get(DocumentServiceKeys.CURRENT_PKG_NAME_KEY);
        String currentCUnitName = context.get(DocumentServiceKeys.RELATIVE_FILE_PATH_KEY);
        Optional<BLangPackage> currentPkg = modules.stream()
                .filter(pkg -> pkg.symbol.getName().getValue().equals(currentPkgName))
                .findAny();

        if (!currentPkg.isPresent()) {
            throw new LSReferencesException("Current module should be present");
        }

        Optional<BLangCompilationUnit> currentCUnit = currentPkg.get().getCompilationUnits().stream()
                .filter(cUnit -> cUnit.name.equals(currentCUnitName))
                .findAny();

        if (!tokenRefs.containsKey(currentPkgName)
                || !tokenRefs.get(currentPkgName).containsKey(currentCUnitName)
                || !currentCUnit.isPresent()) {
            throw new LSReferencesException("Current package name or current compilation unit name is invalid");
        }

        List<TokenReferenceModel> currentCUnitTokens = tokenRefs.get(currentPkgName).remove(currentCUnitName);
        context.put(NodeContextKeys.CURRENT_CUNIT_TOKENS_KEY, currentCUnitTokens);
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

    /**
     * Get the definition.
     *
     * @param modules   List of project modules
     * @param context   Definition context
     * @param position  Cursor Position
     * @return {@link List}     List of definition locations
     * @throws LSReferencesException    Exception while finding the references
     */
    public static List<Location> findDefinition(List<BLangPackage> modules, LSContext context, Position position)
            throws LSReferencesException {
        Map<String, Map<String, List<TokenReferenceModel>>> tokenRefs
                = context.get(NodeContextKeys.TOKEN_REFERENCES_KEY);
        prepareReferences(modules, context, position);

        SymbolReferencesModel referencesModel = context.get(NodeContextKeys.REFERENCES_KEY);
        // If the definition list contains an item after the prepare reference mode, then return it.
        // In this case, definition is in the current compilation unit it self
        if (!referencesModel.getDefinitions().isEmpty()) {
            return getLocations(Collections.singletonList(referencesModel.getDefinitions().get(0)), context);
        }
        Optional<SymbolReferencesModel.Reference> symbolAtCursor = referencesModel.getSymbolAtCursor();
        if (!symbolAtCursor.isPresent()) {
            return new ArrayList<>();
        }
        String symbolPkgName = symbolAtCursor.get().getPkgName();
        Optional<BLangPackage> module = modules.stream()
                .filter(bLangPackage -> bLangPackage.symbol.getName().getValue().equals(symbolPkgName))
                .findAny();
        if (!module.isPresent()) {
            return new ArrayList<>();
        }

        for (BLangCompilationUnit compilationUnit : module.get().getCompilationUnits()) {
            List<TokenReferenceModel> tokenRefModels = tokenRefs.get(symbolPkgName).get(compilationUnit.getName());
            if (tokenRefModels != null) {
                // Possible Reference tokens found within the cUnit
                SymbolReferenceFindingVisitor refVisitor = new SymbolReferenceFindingVisitor(context,
                        symbolPkgName, position);
                refVisitor.visit(compilationUnit);
                if (!referencesModel.getDefinitions().isEmpty()) {
                    break;
                }
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
                                                        Position position)
            throws LSReferencesException {
        prepareReferences(modules, context, position);
        SymbolReferencesModel referencesModel = context.get(NodeContextKeys.REFERENCES_KEY);
        if (!referencesModel.getSymbolAtCursor().isPresent()) {
            return null;
        }

        BSymbol symbolAtCursor = referencesModel.getSymbolAtCursor().get().getSymbol();
        String symbolOwnerPkg = symbolAtCursor.pkgID.toString();

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

        return getWorkspaceEdit(referencesModel, context, newName);
    }

    private static List<Location> getLocations(List<SymbolReferencesModel.Reference> references, LSContext context) {
        return references.stream().map(reference -> {
            DiagnosticPos position = reference.getPosition();
            String sourceRoot = context.get(DocumentServiceKeys.SOURCE_ROOT_KEY);
            Position start = new Position(position.sLine, position.sCol);
            Position end = new Position(position.eLine, position.eCol);
            Range range = new Range(start, end);

            String fileURI = Paths.get(sourceRoot).resolve(reference.getPkgName())
                    .resolve(reference.getCompilationUnit()).toUri().toString();

            return new Location(fileURI, range);
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
            String pkgName = reference.getSymbol().pkgID.nameComps.stream()
                .map(Name::getValue)
                .collect(Collectors.joining("."));
            String cUnitName = reference.getCompilationUnit();
            String uri = Paths.get(sourceRoot).resolve(pkgName).resolve(cUnitName).toUri().toString();
            Position start = new Position(referencePos.sLine, referencePos.sCol);
            Position end = new Position(referencePos.eLine, referencePos.eCol);
            Range range = new Range(start, end);
            TextEdit textEdit = new TextEdit(range, newName);
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
}

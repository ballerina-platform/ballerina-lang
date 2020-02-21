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
package org.ballerinalang.langserver.util.definition;

import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.exception.LSStdlibCacheException;
import org.ballerinalang.langserver.util.references.ReferencesUtil;
import org.ballerinalang.langserver.util.references.SymbolReferenceFindingVisitor;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.Location;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Utilities for the definition operations.
 * 
 * @since 1.2.0
 */
public class DefinitionUtil {

    /**
     * Get the definition.
     *
     * @param context Definition context
     * @return {@link List} List of definition locations
     * @throws WorkspaceDocumentException when couldn't find file for uri
     * @throws CompilationFailedException when compilation failed
     */
    public static List<Location> getDefinition(LSContext context) throws WorkspaceDocumentException,
            CompilationFailedException, LSStdlibCacheException {
        List<BLangPackage> modules = ReferencesUtil.findCursorTokenAndCompileModules(context);
        ReferencesUtil.fillReferences(modules, context);
        SymbolReferencesModel referencesModel = context.get(NodeContextKeys.REFERENCES_KEY);
        // If the definition list contains an item after the prepare reference mode, then return it.
        // In this case, definition is in the current compilation unit it self
        if (!referencesModel.getDefinitions().isEmpty()) {
            return ReferencesUtil.getLocations(Collections.singletonList(referencesModel.getDefinitions().get(0)),
                    context.get(DocumentServiceKeys.SOURCE_ROOT_KEY));
        }
        // If symbol at the cursor's module is a standard library module we find the module in standard library
        Optional<SymbolReferencesModel.Reference> symbolAtCursor = referencesModel.getReferenceAtCursor();
        /*
        Here we do not check whether the symbol at cursor exist since in the prepareReferences processing step
        we check the presence 
         */
        PackageID pkgID = symbolAtCursor.get().getSymbol().pkgID;
        if (isStandardLibModule(pkgID)) {
            return getStdLibDefinitionLocations(context, pkgID, symbolAtCursor.get());
        }
        // Ignore the optional check since it has been handled during prepareReference and throws exception
        String symbolPkgName = symbolAtCursor.get().getSymbolPkgName();
        Optional<BLangPackage> module = modules.stream()
                .filter(bLangPackage -> bLangPackage.symbol.getName().getValue().equals(symbolPkgName))
                .findAny();
        if (!module.isPresent()) {
            return new ArrayList<>();
        }
        for (BLangCompilationUnit compilationUnit : module.get().getCompilationUnits()) {
            SymbolReferenceFindingVisitor refVisitor = new SymbolReferenceFindingVisitor(context, symbolPkgName);
            refVisitor.visit(compilationUnit);
            if (!referencesModel.getDefinitions().isEmpty()) {
                break;
            }
        }

        return ReferencesUtil.getLocations(referencesModel.getDefinitions(),
                context.get(DocumentServiceKeys.SOURCE_ROOT_KEY));
    }

    private static boolean isStandardLibModule(PackageID packageID) {
        String orgName = packageID.getOrgName().getValue();
        return CommonUtil.BALLERINA_ORG_NAME.equals(orgName) || CommonUtil.BALLERINAX_ORG_NAME.equals(orgName);
    }

    private static List<Location> getStdLibDefinitionLocations(LSContext context, PackageID packageID,
                                                               SymbolReferencesModel.Reference symbolAtCursor)
            throws LSStdlibCacheException {
        LSStandardLibCache stdLibCache = LSStandardLibCache.getInstance();
        List<TopLevelNode> definitions = stdLibCache.getTopLevelNodesForModule(context, packageID);
        TopLevelNode extractedDef = null;
        for (TopLevelNode topLevelNode : definitions) {
            Pair<String, BSymbol> nameSymbolPair = getTopLevelNodeNameSymbolPair(topLevelNode);
            if (nameSymbolPair.getLeft().equals(symbolAtCursor.getSymbol().name.value)) {
                extractedDef = topLevelNode;
                break;
            }
        }

        if (extractedDef != null) {
            Pair<String, BSymbol> nameSymbolPair = getTopLevelNodeNameSymbolPair(extractedDef);
            DiagnosticPos diagnosticPos = CommonUtil.toZeroBasedPosition((DiagnosticPos) extractedDef.getPosition());
            SymbolReferencesModel.Reference reference = new SymbolReferencesModel.Reference(diagnosticPos,
                    nameSymbolPair.getRight(), (BLangNode) extractedDef);

            String sourceRoot = stdLibCache.getStdlibCacheRoot().toString();
            return ReferencesUtil.getLocations(Collections.singletonList(reference), sourceRoot);
        }

        return new ArrayList<>();
    }

    private static Pair<String, BSymbol> getTopLevelNodeNameSymbolPair(TopLevelNode topLevelNode) {
        switch (topLevelNode.getKind()) {
            case FUNCTION:
                BLangFunction funcNode = (BLangFunction) topLevelNode;
                return Pair.of(funcNode.getName().getValue(), funcNode.symbol);
            case TYPE_DEFINITION:
                BLangTypeDefinition defNode = (BLangTypeDefinition) topLevelNode;
                return Pair.of(defNode.getName().getValue(), defNode.symbol);
            case CONSTANT:
                BLangConstant constNode = (BLangConstant) topLevelNode;
                return Pair.of(constNode.getName().getValue(), constNode.symbol);
            // TODO: Handle XML Namespace Declarations
            case ANNOTATION:
                BLangAnnotation annotationNode = (BLangAnnotation) topLevelNode;
                return Pair.of(annotationNode.getName().getValue(), annotationNode.symbol);
            default:
                return Pair.of("", null);
        }
    }
}

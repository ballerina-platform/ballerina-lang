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

import io.ballerina.compiler.syntax.tree.Token;
import org.apache.commons.lang3.tuple.Pair;
import org.ballerinalang.langserver.common.utils.CommonUtil;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.exception.LSStdlibCacheException;
import org.ballerinalang.langserver.util.TokensUtil;
import org.ballerinalang.langserver.util.references.ReferencesUtil;
import org.ballerinalang.langserver.util.references.SymbolReferenceFindingVisitor;
import org.ballerinalang.langserver.util.references.SymbolReferencesModel;
import org.ballerinalang.langserver.util.references.TokenOrSymbolNotFoundException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.Location;
import org.eclipse.lsp4j.Position;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BInvokableSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BObjectTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotation;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.BLangSimpleVariable;
import org.wso2.ballerinalang.compiler.tree.BLangTypeDefinition;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangConstant;
import org.wso2.ballerinalang.compiler.tree.types.BLangObjectTypeNode;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.Flags;

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
     * @param context  Definition context
     * @param position cursor position
     * @return {@link List} List of definition locations
     * @throws WorkspaceDocumentException when couldn't find file for uri
     * @throws CompilationFailedException when compilation failed
     */
    public static List<Location> getDefinition(LSContext context, Position position)
            throws WorkspaceDocumentException, CompilationFailedException, LSStdlibCacheException,
                   TokenOrSymbolNotFoundException {
        Token tokenAtCursor = TokensUtil.findTokenAtPosition(context, position);
        List<BLangPackage> modules = ReferencesUtil.compileModules(context);
        SymbolReferencesModel refModel = ReferencesUtil.findReferencesForCurrentCUnit(tokenAtCursor, modules, context);
        // If the definition list contains an item after the prepare reference mode, then return it.
        // In this case, definition is in the current compilation unit it self
        if (!refModel.getDefinitions().isEmpty()) {
            return ReferencesUtil.getLocations(Collections.singletonList(refModel.getDefinitions().get(0)),
                                               context.get(DocumentServiceKeys.SOURCE_ROOT_KEY));
        }
        // If symbol at the cursor's module is a standard library module we find the module in standard library
        SymbolReferencesModel.Reference symbolAtCursor = refModel.getReferenceAtCursor();
        /*
        Here we do not check whether the symbol at cursor exist since in the prepareReferences processing step
        we check the presence 
         */
        PackageID pkgID = symbolAtCursor.getSymbol().pkgID;
        if (isStandardLibModule(pkgID)) {
            return getStdLibDefinitionLocations(context, pkgID, symbolAtCursor);
        }
        // Ignore the optional check since it has been handled during prepareReference and throws exception
        String symbolPkgName = symbolAtCursor.getSymbolPkgName();
        Optional<BLangPackage> module = modules.stream()
                .filter(bLangPackage -> bLangPackage.symbol.getName().getValue().equals(symbolPkgName))
                .findAny();
        if (!module.isPresent()) {
            return new ArrayList<>();
        }
        for (BLangCompilationUnit compilationUnit : module.get().getCompilationUnits()) {
            SymbolReferenceFindingVisitor refVisitor = new SymbolReferenceFindingVisitor(context, tokenAtCursor,
                                                                                         symbolPkgName);
            refVisitor.visit(compilationUnit);
            if (!refModel.getDefinitions().isEmpty()) {
                break;
            }
        }

        return ReferencesUtil.getLocations(refModel.getDefinitions(),
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

        // For the invokable symbols get the top level note and find the definition within the definition's scope
        if (symbolAtCursor.getSymbol() instanceof BInvokableSymbol
                && (((BInvokableSymbol) symbolAtCursor.getSymbol()).flags & Flags.ATTACHED) == Flags.ATTACHED) {
            return getStdLibInvocationLocation((BInvokableSymbol) symbolAtCursor.getSymbol(), definitions);
        } else if (symbolAtCursor.getSymbol() instanceof BVarSymbol
                && symbolAtCursor.getSymbol().owner instanceof BObjectTypeSymbol) {
            return getStdLibFieldLocation((BVarSymbol) symbolAtCursor.getSymbol(), definitions);
        }

        for (TopLevelNode topLevelNode : definitions) {
            Pair<String, BSymbol> nameSymbolPair = getTopLevelNodeNameSymbolPair(topLevelNode);
            if (nameSymbolPair.getLeft().equals(symbolAtCursor.getSymbol().name.value)) {
                extractedDef = topLevelNode;
                break;
            }
        }

        if (extractedDef != null) {
            Pair<String, BSymbol> nameSymbolPair = getTopLevelNodeNameSymbolPair(extractedDef);
            DiagnosticPos diagnosticPos = getTopLevelNodePosition(extractedDef);
            return prepareLocations(diagnosticPos, nameSymbolPair.getRight(), (BLangNode) extractedDef);
        }

        return new ArrayList<>();
    }

    private static List<Location> prepareLocations(DiagnosticPos diagPos, BSymbol symbol, BLangNode node)
            throws LSStdlibCacheException {
        String sourceRoot = LSStandardLibCache.getInstance()
                .getCachedStdlibRoot(diagPos.src.pkgID.name.value).toString();
        SymbolReferencesModel.Reference reference = new SymbolReferencesModel.Reference(diagPos, symbol, node);
        return ReferencesUtil.getLocations(Collections.singletonList(reference), sourceRoot);
    }

    private static List<Location> getStdLibInvocationLocation(BInvokableSymbol symbol, List<TopLevelNode> topLevelNodes)
            throws LSStdlibCacheException {
        Optional<BLangTypeDefinition> objectNode = getOwnerObjectTypeNode(symbol, topLevelNodes);
        if (!objectNode.isPresent()) {
            return new ArrayList<>();
        }
        BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) objectNode.get().typeNode;
        String symbolName = CommonUtil.getSymbolName(symbol);
        if ("init".equals(symbolName)) {
            // Definition is invoked over the new keyword
            BLangFunction initFunction = objectTypeNode.initFunction;
            DiagnosticPos pos;
            if (initFunction.symbol == null) {
                pos = CommonUtil.toZeroBasedPosition(objectNode.get().getName().pos);
                return prepareLocations(pos, objectNode.get().symbol, objectTypeNode);
            }
            pos = CommonUtil.toZeroBasedPosition(initFunction.getName().pos);
            return prepareLocations(pos, initFunction.symbol, initFunction);
        }
        for (BLangFunction function : objectTypeNode.functions) {
            if (symbolName.equals(function.getName().getValue())) {
                DiagnosticPos pos = CommonUtil.toZeroBasedPosition(function.name.pos);
                return prepareLocations(pos, symbol, function);
            }
        }
        return new ArrayList<>();
    }
    
    private static List<Location> getStdLibFieldLocation(BVarSymbol symbol, List<TopLevelNode> topLevelNodes)
            throws LSStdlibCacheException {
        Optional<BLangTypeDefinition> objectNode = getOwnerObjectTypeNode(symbol, topLevelNodes);
        if (!objectNode.isPresent()) {
            return new ArrayList<>();
        }
        BLangObjectTypeNode objectTypeNode = (BLangObjectTypeNode) objectNode.get().typeNode;
        String symbolName = CommonUtil.getSymbolName(symbol);

        for (BLangSimpleVariable field : objectTypeNode.fields) {
            if (symbolName.equals(field.getName().getValue())) {
                DiagnosticPos pos = CommonUtil.toZeroBasedPosition(field.name.pos);
                return prepareLocations(pos, symbol, field);
            }
        }
        
        return new ArrayList<>();
    }
    
    private static Optional<BLangTypeDefinition> getOwnerObjectTypeNode(BVarSymbol symbol,
                                                                        List<TopLevelNode> topLevelNodes) {
        if (!(symbol.owner instanceof BObjectTypeSymbol)) {
            return Optional.empty();
        }
        String objectTypeName = symbol.owner.getName().getValue();

        TopLevelNode objectNode = topLevelNodes.parallelStream()
                .filter(topLevelNode -> {
                    Pair<String, BSymbol> nameSymbolPair;
                    try {
                        nameSymbolPair = getTopLevelNodeNameSymbolPair(topLevelNode);
                    } catch (LSStdlibCacheException e) {
                        return false;
                    }
                    return objectTypeName.equals(nameSymbolPair.getLeft());
                })
                .findFirst()
                .orElse(null);


        if (!(objectNode instanceof BLangTypeDefinition)
                || !(((BLangTypeDefinition) objectNode).typeNode instanceof BLangObjectTypeNode)) {
            return Optional.empty();
        }
        
        return Optional.of((BLangTypeDefinition) objectNode);
    }

    private static Pair<String, BSymbol> getTopLevelNodeNameSymbolPair(TopLevelNode topLevelNode)
            throws LSStdlibCacheException {
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
                throw new LSStdlibCacheException("Could not find a valid Top Level Node"
                        + topLevelNode.getKind().name());
        }
    }

    private static DiagnosticPos getTopLevelNodePosition(TopLevelNode topLevelNode)
            throws LSStdlibCacheException {
        switch (topLevelNode.getKind()) {
            case FUNCTION:
                BLangFunction funcNode = (BLangFunction) topLevelNode;
                return CommonUtil.toZeroBasedPosition(funcNode.name.pos);
            case TYPE_DEFINITION:
                BLangTypeDefinition defNode = (BLangTypeDefinition) topLevelNode;
                return CommonUtil.toZeroBasedPosition(defNode.name.pos);
            case CONSTANT:
                BLangConstant constNode = (BLangConstant) topLevelNode;
                return CommonUtil.toZeroBasedPosition(constNode.name.pos);
            // TODO: Handle XML Namespace Declarations
            case ANNOTATION:
                BLangAnnotation annotationNode = (BLangAnnotation) topLevelNode;
                return CommonUtil.toZeroBasedPosition(annotationNode.name.pos);
            default:
                throw new LSStdlibCacheException("Could not find Position for Node" + topLevelNode.getKind().name());
        }
    }
}

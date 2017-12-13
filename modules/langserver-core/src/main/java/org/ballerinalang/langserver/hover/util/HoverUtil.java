/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.hover.util;

import org.ballerinalang.langserver.TextDocumentServiceContext;
import org.ballerinalang.langserver.hover.HoverKeys;
import org.ballerinalang.langserver.hover.constants.HoverConstants;
import org.ballerinalang.model.tree.Node;
import org.ballerinalang.model.tree.TopLevelNode;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentPositionParams;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.PackageLoader;
import org.wso2.ballerinalang.compiler.semantics.analyzer.CodeAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SemanticAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for Hover functionality of language server
 */
public class HoverUtil {

    public BLangPackage loadBuiltInPackage(CompilerContext context) {
        BLangPackage builtInCorePkg = getBuiltInPackage(context, Names.BUILTIN_CORE_PACKAGE);
        SymbolTable symbolTable = SymbolTable.getInstance(context);
        symbolTable.createErrorTypes();
        symbolTable.loadOperators();
        // Load built-in packages.
        BLangPackage builtInPkg = getBuiltInPackage(context, Names.BUILTIN_PACKAGE);
        builtInCorePkg.getStructs().forEach(s -> {
            builtInPkg.getStructs().add(s);
            builtInPkg.topLevelNodes.add(s);
        });
        symbolTable.builtInPackageSymbol = builtInPkg.symbol;
        return builtInPkg;
    }

    public BLangPackage getBuiltInPackage(CompilerContext context, Name name) {
        PackageLoader pkgLoader = PackageLoader.getInstance(context);
        SemanticAnalyzer semAnalyzer = SemanticAnalyzer.getInstance(context);
        CodeAnalyzer codeAnalyzer = CodeAnalyzer.getInstance(context);
        return codeAnalyzer.analyze(semAnalyzer.analyze(pkgLoader.loadEntryPackage(name.getValue())));
    }

    public Hover resolveBuiltInPackageDoc(BLangPackage bLangPackage, TextDocumentServiceContext hoverContext) {
        Hover hover = null;
        switch (hoverContext.get(HoverKeys.SYMBOL_KIND_OF_HOVER_NODE_KEY).name()) {
            case HoverConstants.FUNCTION:
                BLangFunction bLangFunction = bLangPackage.functions.stream()
                        .filter(function -> function.name.getValue()
                                .equals(hoverContext.get(HoverKeys.NAME_OF_HOVER_NODE_KEY).getValue()))
                        .findAny().orElse(null);
                if (bLangFunction != null) {
                    hover = new Hover();
                    String content = "";
                    content += getAnnotationValue(HoverConstants.DESCRIPTION, bLangFunction.annAttachments);
                    content += getAnnotationValue(HoverConstants.PARAM, bLangFunction.annAttachments);
                    List<Either<String, MarkedString>> contents = new ArrayList<>();
                    contents.add(Either.forLeft(content));
                    hover.setContents(contents);
                }
                break;
            case HoverConstants.ACTION:
                break;
            default:
                break;
        }
        return hover;
    }

    public Node resolveNodeAsToPosition(String fileName,
                                        BLangPackage bLangPackage, TextDocumentPositionParams positionParams) {
        BLangCompilationUnit bLangCompilationUnit =
                bLangPackage.compUnits.stream().filter(unit -> unit.name.equals(fileName)).findAny().orElse(null);
        TopLevelNode topLevelNode = bLangCompilationUnit.getTopLevelNodes().stream().filter(node ->
                node.getPosition().getStartLine() <= positionParams.getPosition().getLine()
                        && node.getPosition().getEndLine() >= positionParams.getPosition().getLine()).findAny().orElse(null);
        return null;
    }

    public static boolean isMatchingPosition(DiagnosticPos nodePosition, Position textPosition) {
        boolean isCorrectPosition = false;
        if (nodePosition.sLine <= textPosition.getLine() && nodePosition.eLine >= textPosition.getLine()
                && nodePosition.sCol <= textPosition.getCharacter() && nodePosition.eCol >= textPosition.getCharacter()) {
            isCorrectPosition = true;
        }
        return isCorrectPosition;
    }

    private String getAnnotationValue(String annotationName, List<BLangAnnotationAttachment> annotationAttachments) {
        String value = "";
        for (BLangAnnotationAttachment annotationAttachment : annotationAttachments) {
            if (annotationAttachment.annotationName.getValue().equals(annotationName)) {
                value += getAnnotationAttributes("value", annotationAttachment.attributes)+"\n";
            }
        }
        return value;
    }

    private String getAnnotationAttributes(String attributeName, List<BLangAnnotAttachmentAttribute> annotAttachmentAttributes) {
        String value = "";
        for(BLangAnnotAttachmentAttribute attribute : annotAttachmentAttributes){
            if(attribute.name.getValue().equals(attributeName)){
                value = ((BLangLiteral)attribute.value.value).getValue().toString();
                break;
            }
        }
        return value;
    }
}

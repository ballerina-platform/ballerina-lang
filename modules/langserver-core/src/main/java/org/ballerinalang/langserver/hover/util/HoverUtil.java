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
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.MarkedString;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.wso2.ballerinalang.compiler.PackageLoader;
import org.wso2.ballerinalang.compiler.semantics.analyzer.CodeAnalyzer;
import org.wso2.ballerinalang.compiler.semantics.analyzer.SemanticAnalyzer;
import org.wso2.ballerinalang.compiler.tree.BLangAnnotationAttachment;
import org.wso2.ballerinalang.compiler.tree.BLangFunction;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangAnnotAttachmentAttribute;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Utility class for Hover functionality of language server.
 */
public class HoverUtil {

    public BLangPackage getNativePackage(CompilerContext context, Name name) {
        BLangPackage nativePackage = null;
        PackageLoader packageLoader = PackageLoader.getInstance(context);
        // max depth for the recursive function which search for child directories
        int maxDepth = 15;
        Set<PackageID> packages = packageLoader.listPackages(maxDepth);
        for (PackageID pkg : packages) {
            Name version = pkg.getPackageVersion();
            BLangIdentifier bLangIdentifier = new BLangIdentifier();
            bLangIdentifier.setValue(version.getValue());

            List<BLangIdentifier> pkgNameComps = pkg.getNameComps().stream().map(nameToBLangIdentifier)
                    .collect(Collectors.<BLangIdentifier>toList());
            // we have already loaded ballerina.builtin and ballerina.builtin.core. hence skipping loading those
            // packages.
            if (!"ballerina.builtin".equals(pkg.getName().getValue())
                    && !"ballerina.builtin.core".equals(pkg.getName().getValue())
                    && name.getValue().equals(pkg.getName().getValue())) {
                org.wso2.ballerinalang.compiler.tree.BLangPackage bLangPackage = packageLoader
                        .loadPackage(pkgNameComps, bLangIdentifier);
                nativePackage = bLangPackage;
            }
        }

        return nativePackage;
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
                    StringBuilder content = new StringBuilder();
                    content.append(getAnnotationValue(HoverConstants.DESCRIPTION, bLangFunction.annAttachments));
                    content.append(getAnnotationValue(HoverConstants.PARAM, bLangFunction.annAttachments));
                    List<Either<String, MarkedString>> contents = new ArrayList<>();
                    contents.add(Either.forLeft(content.toString()));
                    hover.setContents(contents);
                }
                break;
            default:
                break;
        }
        return hover;
    }

    public static boolean isMatchingPosition(DiagnosticPos nodePosition, Position textPosition) {
        boolean isCorrectPosition = false;
        if (nodePosition.sLine <= textPosition.getLine()
                && nodePosition.eLine >= textPosition.getLine()
                && nodePosition.sCol <= textPosition.getCharacter()
                && nodePosition.eCol >= textPosition.getCharacter()) {
            isCorrectPosition = true;
        }
        return isCorrectPosition;
    }

    private String getAnnotationValue(String annotationName, List<BLangAnnotationAttachment> annotationAttachments) {
        StringBuilder value = new StringBuilder();
        for (BLangAnnotationAttachment annotationAttachment : annotationAttachments) {
            if (annotationAttachment.annotationName.getValue().equals(annotationName)) {
                value.append(getAnnotationAttributes("value", annotationAttachment.attributes))
                        .append("\n");
            }
        }
        return value.toString();
    }

    private String getAnnotationAttributes(String attributeName,
                                           List<BLangAnnotAttachmentAttribute> annotAttachmentAttributes) {
        String value = "";
        for (BLangAnnotAttachmentAttribute attribute : annotAttachmentAttributes) {
            if (attribute.name.getValue().equals(attributeName)) {
                value = ((BLangLiteral) attribute.value.value).getValue().toString();
                break;
            }
        }
        return value;
    }

    /**
     * Function to convert org.wso2.ballerinalang.compiler.util.Name instance to
     * org.wso2.ballerinalang.compiler.tree.BLangIdentifier instance.
     */
    private static java.util.function.Function<Name, BLangIdentifier> nameToBLangIdentifier =
            name -> {
                BLangIdentifier bLangIdentifier = new BLangIdentifier();
                bLangIdentifier.setValue(name.getValue());
                return bLangIdentifier;
            };
}

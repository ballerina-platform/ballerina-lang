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
package org.ballerinalang.langserver.common.utils;

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.symbols.AnnotationAttachPoint;
import io.ballerina.compiler.api.symbols.AnnotationSymbol;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.syntax.tree.ImportDeclarationNode;
import io.ballerina.compiler.syntax.tree.ImportOrgNameNode;
import io.ballerina.projects.Module;
import org.ballerinalang.langserver.commons.BallerinaCompletionContext;
import org.ballerinalang.langserver.commons.DocumentServiceContext;
import org.ballerinalang.langserver.commons.SnippetContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.TextEdit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

import static org.ballerinalang.langserver.common.utils.CommonKeys.CLOSE_BRACE_KEY;
import static org.ballerinalang.langserver.common.utils.CommonKeys.OPEN_BRACE_KEY;
import static org.ballerinalang.langserver.common.utils.CommonKeys.PKG_DELIMITER_KEYWORD;
import static org.ballerinalang.langserver.common.utils.CommonUtil.LINE_SEPARATOR;
import static org.ballerinalang.langserver.common.utils.ModuleUtil.getPackageNameComponentsCombined;

/**
 * Contains the utilities to generate an Annotation Completion Item.
 *
 * @since 2.0.0
 */
public class AnnotationUtil {

    private AnnotationUtil() {
    }

    /**
     * Get the module qualified Annotation completion Item.
     *
     * @param moduleID         Module ID of the annotation
     * @param annotationSymbol Annotation Symbol to extract the completion Item
     * @param ctx              LS Service operation context, in this case completion context
     * @return {@link CompletionItem} Completion item for the annotation
     */
    public static LSCompletionItem getModuleQualifiedAnnotationItem(ModuleID moduleID,
                                                                    AnnotationSymbol annotationSymbol,
                                                                    BallerinaCompletionContext ctx) {
        Optional<Module> currentModule = ctx.workspace().module(ctx.filePath());
        if (currentModule.isEmpty()) {
            throw new RuntimeException("Cannot find a valid module");
        }
        String currentProjectOrgName = currentModule.get().project().currentPackage().packageOrg().value();

        String alias;
        Optional<String> importedAlias = getAlias(ctx, moduleID);
        alias = importedAlias.orElseGet(moduleID::modulePrefix);
        String label = getAnnotationLabel(alias, annotationSymbol);
        String insertText = getAnnotationInsertText(alias, annotationSymbol);

        Map<ImportDeclarationNode, ModuleSymbol> imports = ctx.currentDocImportsMap();
        Optional<ImportDeclarationNode> pkgImport = imports.keySet().stream()
                .filter(bLangImportPackage -> {
                    Optional<ImportOrgNameNode> importOrgNameNode = bLangImportPackage.orgName();
                    if (importOrgNameNode.isEmpty()) {
                        return false;
                    }
                    String orgName = importOrgNameNode.get().orgName().text();
                    String importPkgName = (orgName.equals("") ? currentProjectOrgName : orgName) + "/"
                            + getPackageNameComponentsCombined(bLangImportPackage);
                    String annotationPkgOrgName = moduleID.orgName();
                    String annotationPkgName = annotationPkgOrgName + "/" + moduleID.moduleName();
                    return importPkgName.equals(annotationPkgName);
                })
                .findAny();
        // if the particular import statement not available we add the additional text edit to auto import
        List<TextEdit> textEdits = new ArrayList<>();
        if (pkgImport.isEmpty() && !CommonUtil.isLangLib(moduleID)) {
            textEdits.addAll(getAdditionalTextEdits(ctx, moduleID));
        }
        CompletionItem annotationItem = prepareCompletionItem(label, insertText, textEdits);
        return new SymbolCompletionItem(ctx, annotationSymbol, annotationItem);
    }

    /**
     * Get the annotation completion item for a given annotation symbol.
     * This method's usage is for the annotations within the same module or annotation completion item without the
     * module alias appended.
     *
     * @param annotationSymbol annotation symbol to evaluate
     * @param ctx              Language server context
     * @return {@link LSCompletionItem} generated for the annotation symbol
     */
    public static LSCompletionItem getAnnotationItem(AnnotationSymbol annotationSymbol,
                                                     BallerinaCompletionContext ctx) {
        String label = getAnnotationLabel(annotationSymbol);
        String insertText = getAnnotationInsertText(annotationSymbol);
        CompletionItem completionItem = prepareCompletionItem(label, insertText, new ArrayList<>());

        return new SymbolCompletionItem(ctx, annotationSymbol, completionItem);
    }

    /**
     * Get the additional text edits.
     *
     * @param context  language server context
     * @param moduleID module id
     * @return {@link List} of text edits
     */
    public static List<TextEdit> getAdditionalTextEdits(DocumentServiceContext context, ModuleID moduleID) {
        Optional<Module> currentModule = context.workspace().module(context.filePath());
        if (currentModule.isEmpty()) {
            return Collections.emptyList();
        }
        String currentProjectOrgName = currentModule.get().project().currentPackage().packageOrg().value();
        Map<ImportDeclarationNode, ModuleSymbol> imports = context.currentDocImportsMap();
        Optional<ImportDeclarationNode> pkgImport = imports.keySet().stream()
                .filter(importNode -> {
                    if (importNode.orgName().isEmpty()) {
                        return false;
                    }
                    String orgName = importNode.orgName().get().orgName().text();
                    String importPkgName = (orgName.equals("") ? currentProjectOrgName : orgName) + "/"
                            + getPackageNameComponentsCombined(importNode);
                    String annotationPkgOrgName = moduleID.orgName();
                    String annotationPkgName = annotationPkgOrgName + "/" + moduleID.moduleName();
                    return importPkgName.equals(annotationPkgName);
                })
                .findAny();
        // if the particular import statement not available we add the additional text edit to auto import
        List<TextEdit> textEdits = new ArrayList<>();
        if (pkgImport.isEmpty() && !CommonUtil.isLangLib(moduleID)) {
            textEdits.addAll(CommonUtil.getAutoImportTextEdits(moduleID.orgName(), moduleID.moduleName(), context));
        }

        return textEdits;
    }

    /**
     * Whether the annotation symbol has the given attachment point.
     *
     * @param annotation  Annotation to evaluate
     * @param attachPoint attachment point to check
     * @return {@link Boolean} having the attachment point or not
     */
    public static boolean hasAttachment(AnnotationSymbol annotation, AnnotationAttachPoint attachPoint) {
        // Annotations without attachment points can applied to any construct
        return annotation.attachPoints().isEmpty() || annotation.attachPoints().contains(attachPoint);
    }

    /**
     * Get the annotation Insert text.
     *
     * @param aliasComponent   Package ID
     * @param annotationSymbol Annotation to get the insert text
     * @return {@link String} Insert text
     */
    private static String getAnnotationInsertText(@Nonnull String aliasComponent, AnnotationSymbol annotationSymbol) {
        StringBuilder annotationStart = new StringBuilder();
        if (!aliasComponent.isEmpty()) {
            annotationStart.append(aliasComponent).append(PKG_DELIMITER_KEYWORD);
        }
        if (annotationSymbol.typeDescriptor().isPresent()) {
            annotationStart.append(annotationSymbol.getName().get());
            Optional<TypeSymbol> attachedType
                    = Optional.ofNullable(CommonUtil.getRawType(annotationSymbol.typeDescriptor().get()));
            Optional<TypeSymbol> resultType;
            if (attachedType.isPresent() && attachedType.get().typeKind() == TypeDescKind.ARRAY) {
                resultType = Optional.of(((ArrayTypeSymbol) attachedType.get()).memberTypeDescriptor());
            } else {
                resultType = attachedType;
            }
            if (resultType.isPresent() && (resultType.get().typeKind() == TypeDescKind.RECORD
                    || resultType.get().typeKind() == TypeDescKind.MAP)) {
                List<RecordFieldSymbol> requiredFields = new ArrayList<>();
                if (resultType.get().typeKind() == TypeDescKind.RECORD) {
                    requiredFields.addAll(RecordUtil.getMandatoryRecordFields((RecordTypeSymbol) 
                            resultType.get()));
                }
                if (!requiredFields.isEmpty()) {
                    annotationStart.append(" ").append(OPEN_BRACE_KEY).append(LINE_SEPARATOR);
                    List<String> insertTexts = new ArrayList<>();
                    SnippetContext snippetContext = new SnippetContext();
                    for (int i = 0; i < requiredFields.size(); i++) {
                        RecordFieldSymbol field = requiredFields.get(i);
                        String fieldInsertionText = "\t" +
                                RecordUtil.getRecordFieldCompletionInsertText(field, snippetContext);
                        insertTexts.add(fieldInsertionText);
                    }
                    annotationStart.append(String.join("," + LINE_SEPARATOR, insertTexts));
                    if (requiredFields.isEmpty()) {
                        annotationStart.append("\t").append("${1}");
                    }
                    annotationStart.append(LINE_SEPARATOR).append(CLOSE_BRACE_KEY);
                }
            }
        } else {
            annotationStart.append(annotationSymbol.getName().get());
        }

        return annotationStart.toString();
    }

    /**
     * Get the annotation Insert text.
     *
     * @param annotationSymbol Annotation to get the insert text
     * @return {@link String} Insert text
     */
    private static String getAnnotationInsertText(AnnotationSymbol annotationSymbol) {
        return getAnnotationInsertText("", annotationSymbol);
    }

    /**
     * Get the completion Label for the annotation.
     *
     * @param aliasComponent package alias
     * @param annotation     BLang annotation
     * @return {@link String} Label string
     */
    private static String getAnnotationLabel(@Nonnull String aliasComponent, AnnotationSymbol annotation) {
        String pkgComponent = !aliasComponent.isEmpty() ? aliasComponent + PKG_DELIMITER_KEYWORD : "";
        return pkgComponent + annotation.getName().get();
    }

    /**
     * Get the completion Label for the annotation.
     *
     * @param annotation BLang annotation
     * @return {@link String} Label string
     */
    private static String getAnnotationLabel(AnnotationSymbol annotation) {
        return getAnnotationLabel("", annotation);
    }

    private static CompletionItem prepareCompletionItem(String label, String insertText, List<TextEdit> textEdits) {
        CompletionItem annotationItem = new CompletionItem();
        annotationItem.setLabel(label);
        annotationItem.setInsertText(insertText);
        annotationItem.setInsertTextFormat(InsertTextFormat.Snippet);
        annotationItem.setDetail(ItemResolverConstants.ANNOTATION_TYPE);
        annotationItem.setKind(CompletionItemKind.Property);
        annotationItem.setAdditionalTextEdits(textEdits);

        return annotationItem;
    }

    private static Optional<String> getAlias(BallerinaCompletionContext context, ModuleID moduleID) {
        return context.currentDocImportsMap().keySet().stream().filter(importNode -> {
            Optional<ImportOrgNameNode> orgName = importNode.orgName();
            StringBuilder nodeName = new StringBuilder();
            orgName.ifPresent(importOrgNameNode -> nodeName.append(importOrgNameNode.orgName().text()));
            nodeName.append(getPackageNameComponentsCombined(importNode));
            return moduleID.toString().equals(nodeName.toString());
        }).map(importNode -> {
            if (importNode.prefix().isEmpty()) {
                return importNode.moduleName().get(importNode.moduleName().size() - 1).text();
            }
            return importNode.prefix().get().prefix().text();
        }).findFirst();
    }
}

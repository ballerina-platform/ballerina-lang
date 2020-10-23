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
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.types.ArrayTypeDescriptor;
import io.ballerina.compiler.api.types.BallerinaTypeDescriptor;
import io.ballerina.compiler.api.types.FieldDescriptor;
import io.ballerina.compiler.api.types.RecordTypeDescriptor;
import io.ballerina.compiler.api.types.TypeDescKind;
import org.ballerinalang.langserver.common.CommonKeys;
import org.ballerinalang.langserver.commons.LSContext;
import org.ballerinalang.langserver.commons.completion.LSCompletionItem;
import org.ballerinalang.langserver.compiler.DocumentServiceKeys;
import org.ballerinalang.langserver.completions.SymbolCompletionItem;
import org.ballerinalang.langserver.completions.util.ItemResolverConstants;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.TextEdit;
import org.wso2.ballerinalang.compiler.tree.BLangImportPackage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

import static org.ballerinalang.langserver.common.utils.CommonUtil.LINE_SEPARATOR;
import static org.ballerinalang.langserver.common.utils.CommonUtil.getRecordFieldCompletionInsertText;

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
     * @param pkgAliasMap      Package alias map for the file
     * @return {@link CompletionItem} Completion item for the annotation
     */
    public static LSCompletionItem getModuleQualifiedAnnotationItem(ModuleID moduleID,
                                                                    AnnotationSymbol annotationSymbol,
                                                                    LSContext ctx,
                                                                    Map<String, String> pkgAliasMap) {
        ModuleSymbol currentModule = ctx.get(DocumentServiceKeys.CURRENT_MODULE_KEY);
        String currentProjectOrgName = currentModule.moduleID().orgName();

        String alias;
        if (pkgAliasMap.containsKey(moduleID.toString())) {
            alias = pkgAliasMap.get(moduleID.toString());
        } else {
            alias = moduleID.modulePrefix();
        }
        String label = getAnnotationLabel(alias, annotationSymbol);
        String insertText = getAnnotationInsertText(alias, annotationSymbol);

        List<BLangImportPackage> imports = ctx.get(DocumentServiceKeys.CURRENT_DOC_IMPORTS_KEY);
        Optional<BLangImportPackage> pkgImport = imports.stream()
                .filter(bLangImportPackage -> {
                    String orgName = bLangImportPackage.orgName.value;
                    String importPkgName = (orgName.equals("") ? currentProjectOrgName : orgName) + "/"
                            + CommonUtil.getPackageNameComponentsCombined(bLangImportPackage);
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
    public static LSCompletionItem getAnnotationItem(AnnotationSymbol annotationSymbol, LSContext ctx) {
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
    public static List<TextEdit> getAdditionalTextEdits(LSContext context, ModuleID moduleID) {
        ModuleSymbol currentModule = context.get(DocumentServiceKeys.CURRENT_MODULE_KEY);
        ModuleID currentModuleId = currentModule.moduleID();
        String currentProjectOrgName = currentModuleId.orgName();
        List<BLangImportPackage> imports = context.get(DocumentServiceKeys.CURRENT_DOC_IMPORTS_KEY);
        Optional<BLangImportPackage> pkgImport = imports.stream()
                .filter(bLangImportPackage -> {
                    String orgName = bLangImportPackage.orgName.value;
                    String importPkgName = (orgName.equals("") ? currentProjectOrgName : orgName) + "/"
                            + CommonUtil.getPackageNameComponentsCombined(bLangImportPackage);
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
        return annotation.attachPoints().contains(attachPoint);
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
            annotationStart.append(aliasComponent).append(CommonKeys.PKG_DELIMITER_KEYWORD);
        }
        if (annotationSymbol.typeDescriptor().isPresent()) {
            annotationStart.append(annotationSymbol.name());
            Optional<BallerinaTypeDescriptor> attachedType
                    = Optional.ofNullable(CommonUtil.getRawType(annotationSymbol.typeDescriptor().get()));
            Optional<BallerinaTypeDescriptor> resultType;
            if (attachedType.isPresent() && attachedType.get().kind() == TypeDescKind.ARRAY) {
                resultType = Optional.of(((ArrayTypeDescriptor) attachedType.get()).memberTypeDescriptor());
            } else {
                resultType = attachedType;
            }
            if (resultType.isPresent() && (resultType.get().kind() == TypeDescKind.RECORD
                    || resultType.get().kind() == TypeDescKind.MAP)) {
                List<FieldDescriptor> requiredFields = new ArrayList<>();
                annotationStart.append(" ").append(CommonKeys.OPEN_BRACE_KEY).append(LINE_SEPARATOR);
                if (resultType.get().kind() == TypeDescKind.RECORD) {
                    requiredFields.addAll(CommonUtil.getMandatoryRecordFields((RecordTypeDescriptor) resultType.get()));
                }
                List<String> insertTexts = new ArrayList<>();
                requiredFields.forEach(field -> {
                    String fieldInsertionText = "\t" + getRecordFieldCompletionInsertText(field, 1);
                    insertTexts.add(fieldInsertionText);
                });
                annotationStart.append(String.join("," + LINE_SEPARATOR, insertTexts));
                if (requiredFields.isEmpty()) {
                    annotationStart.append("\t").append("${1}");
                }
                annotationStart.append(LINE_SEPARATOR).append(CommonKeys.CLOSE_BRACE_KEY);
            }
        } else {
            annotationStart.append(annotationSymbol.name());
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
        String pkgComponent = !aliasComponent.isEmpty() ? aliasComponent + CommonKeys.PKG_DELIMITER_KEYWORD : "";
        return pkgComponent + annotation.name();
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
}

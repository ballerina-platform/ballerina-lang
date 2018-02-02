/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.plugins.idea.completion;

import com.intellij.codeInsight.completion.AddSpaceInsertHandler;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.BallerinaIcons;
import org.ballerinalang.plugins.idea.documentation.BallerinaDocumentationProvider;
import org.ballerinalang.plugins.idea.psi.EnumFieldNode;
import org.ballerinalang.plugins.idea.psi.FieldDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.WorkerDeclarationNode;
import org.ballerinalang.plugins.idea.util.BallerinaUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class BallerinaCompletionUtils {

    private static final int VARIABLE_PRIORITY = 20;
    private static final int FUNCTION_PRIORITY = VARIABLE_PRIORITY - 1;
    private static final int VALUE_TYPES_PRIORITY = VARIABLE_PRIORITY - 1;
    private static final int REFERENCE_TYPES_PRIORITY = VARIABLE_PRIORITY - 1;
    private static final int PACKAGE_PRIORITY = VALUE_TYPES_PRIORITY - 1;
    private static final int UNIMPORTED_PACKAGE_PRIORITY = PACKAGE_PRIORITY - 1;
    private static final int STRUCT_PRIORITY = VALUE_TYPES_PRIORITY - 1;
    private static final int CONNECTOR_PRIORITY = VALUE_TYPES_PRIORITY - 1;
    private static final int ACTION_PRIORITY = VALUE_TYPES_PRIORITY - 1;
    private static final int ANNOTATION_PRIORITY = VALUE_TYPES_PRIORITY - 1;
    private static final int ENUM_PRIORITY = VALUE_TYPES_PRIORITY - 1;
    private static final int KEYWORDS_PRIORITY = VALUE_TYPES_PRIORITY - 2;

    // File level keywords
    private static final LookupElementBuilder PUBLIC;
    private static final LookupElementBuilder PACKAGE;
    private static final LookupElementBuilder IMPORT;
    private static final LookupElementBuilder CONST;
    private static final LookupElementBuilder SERVICE;
    private static final LookupElementBuilder RESOURCE;
    private static final LookupElementBuilder FUNCTION;
    private static final LookupElementBuilder CONNECTOR;
    private static final LookupElementBuilder ACTION;
    private static final LookupElementBuilder STRUCT;
    private static final LookupElementBuilder ANNOTATION;
    private static final LookupElementBuilder ATTACH;
    private static final LookupElementBuilder PARAMETER;
    private static final LookupElementBuilder XMLNS;
    private static final LookupElementBuilder ENUM;
    private static final LookupElementBuilder TRANSFORMER;

    // Simple types
    private static final LookupElementBuilder BOOLEAN;
    private static final LookupElementBuilder INT;
    private static final LookupElementBuilder FLOAT;
    private static final LookupElementBuilder STRING;
    private static final LookupElementBuilder BLOB;

    // Reference types
    private static final LookupElementBuilder MESSAGE;
    private static final LookupElementBuilder MAP;
    private static final LookupElementBuilder XML;
    private static final LookupElementBuilder JSON;
    private static final LookupElementBuilder DATATABLE;

    // Other types
    private static final LookupElementBuilder ANY;
    private static final LookupElementBuilder VAR;
    private static final LookupElementBuilder TYPE;

    // Other keywords
    private static final LookupElementBuilder TYPE_OF;
    private static final LookupElementBuilder LENGTH_OF;
    private static final LookupElementBuilder RETURN;
    private static final LookupElementBuilder IF;
    private static final LookupElementBuilder ELSE;
    private static final LookupElementBuilder CREATE;
    private static final LookupElementBuilder FORK;
    private static final LookupElementBuilder JOIN;
    private static final LookupElementBuilder ALL;
    private static final LookupElementBuilder SOME;
    private static final LookupElementBuilder TIMEOUT;
    private static final LookupElementBuilder WORKER;
    private static final LookupElementBuilder TRANSACTION;
    private static final LookupElementBuilder FAILED;
    private static final LookupElementBuilder ABORT;
    private static final LookupElementBuilder TRY;
    private static final LookupElementBuilder CATCH;
    private static final LookupElementBuilder FINALLY;
    private static final LookupElementBuilder WHILE;
    private static final LookupElementBuilder NEXT;
    private static final LookupElementBuilder BREAK;
    private static final LookupElementBuilder THROW;
    private static final LookupElementBuilder FOREACH;
    private static final LookupElementBuilder IN;
    private static final LookupElementBuilder LOCK;

    private static final LookupElementBuilder TRUE;
    private static final LookupElementBuilder FALSE;
    private static final LookupElementBuilder NULL;


    static {
        PUBLIC = createKeywordLookupElement("public");
        PACKAGE = createKeywordLookupElement("package");
        IMPORT = createKeywordLookupElement("import");
        CONST = createKeywordLookupElement("const");
        SERVICE = createKeywordLookupElement("service");
        RESOURCE = createKeywordLookupElement("resource");
        FUNCTION = createKeywordLookupElement("function");
        CONNECTOR = createKeywordLookupElement("connector");
        ACTION = createKeywordLookupElement("action");
        STRUCT = createKeywordLookupElement("struct");
        ANNOTATION = createKeywordLookupElement("annotation");
        ATTACH = createKeywordLookupElement("attach");
        PARAMETER = createKeywordLookupElement("parameter");
        XMLNS = createKeywordLookupElement("xmlns");
        ENUM = createKeywordLookupElement("enum");
        TRANSFORMER = createKeywordLookupElement("transformer");

        BOOLEAN = createTypeLookupElement("boolean", AddSpaceInsertHandler.INSTANCE);
        INT = createTypeLookupElement("int", AddSpaceInsertHandler.INSTANCE);
        FLOAT = createTypeLookupElement("float", AddSpaceInsertHandler.INSTANCE);
        STRING = createTypeLookupElement("string", AddSpaceInsertHandler.INSTANCE);
        BLOB = createTypeLookupElement("blob", AddSpaceInsertHandler.INSTANCE);

        MESSAGE = createTypeLookupElement("message", AddSpaceInsertHandler.INSTANCE);
        MAP = createTypeLookupElement("map", AddSpaceInsertHandler.INSTANCE);
        XML = createTypeLookupElement("xml", AddSpaceInsertHandler.INSTANCE);
        JSON = createTypeLookupElement("json", AddSpaceInsertHandler.INSTANCE);
        DATATABLE = createTypeLookupElement("datatable", AddSpaceInsertHandler.INSTANCE);

        ANY = createTypeLookupElement("any", AddSpaceInsertHandler.INSTANCE);
        VAR = createTypeLookupElement("var", AddSpaceInsertHandler.INSTANCE);
        TYPE = createTypeLookupElement("type", AddSpaceInsertHandler.INSTANCE);

        TYPE_OF = createKeywordLookupElement("typeof");
        LENGTH_OF = createKeywordLookupElement("lengthof");
        RETURN = createKeywordLookupElement("return");
        IF = createKeywordLookupElement("if");
        ELSE = createKeywordLookupElement("else");
        CREATE = createKeywordLookupElement("create");
        FORK = createKeywordLookupElement("fork");
        JOIN = createKeywordLookupElement("join");
        ALL = createKeywordLookupElement("all");
        SOME = createKeywordLookupElement("some");
        TIMEOUT = createKeywordLookupElement("timeout");
        WORKER = createKeywordLookupElement("worker");
        TRANSACTION = createKeywordLookupElement("transaction");
        FAILED = createKeywordLookupElement("failed");
        ABORT = createKeywordLookupElement("abort");
        TRY = createKeywordLookupElement("try");
        CATCH = createKeywordLookupElement("catch");
        FINALLY = createKeywordLookupElement("finally");
        WHILE = createKeywordLookupElement("while");
        NEXT = createKeywordLookupElement("next", ";");
        BREAK = createKeywordLookupElement("break", ";");
        THROW = createKeywordLookupElement("throw");
        FOREACH = createKeywordLookupElement("foreach");
        IN = createKeywordLookupElement("in");
        LOCK = createKeywordLookupElement("lock");

        TRUE = createKeywordLookupElement("true", null);
        FALSE = createKeywordLookupElement("false", null);
        NULL = createKeywordLookupElement("null", null);
    }

    private BallerinaCompletionUtils() {

    }

    /**
     * Creates a lookup element.
     *
     * @param name          name of the lookup
     * @param insertHandler insert handler of the lookup
     * @return {@link LookupElementBuilder} which will be used to create the lookup element.
     */
    @NotNull
    private static LookupElementBuilder createLookupElement(@NotNull String name,
                                                            @Nullable InsertHandler<LookupElement> insertHandler) {
        return LookupElementBuilder.create(name).withBoldness(true).withInsertHandler(insertHandler);
    }

    /**
     * Creates a keyword lookup element.
     *
     * @param name name of the lookup
     * @return {@link LookupElementBuilder} which will be used to create the lookup element.
     */
    @NotNull
    private static LookupElementBuilder createKeywordLookupElement(@NotNull String name) {
        return createKeywordLookupElement(name, " ");
    }

    @NotNull
    private static LookupElementBuilder createKeywordLookupElement(@NotNull String name,
                                                                   @Nullable String traileringString) {

        return createLookupElement(name, createTemplateBasedInsertHandler("ballerina_lang_" + name,
                traileringString));
    }

    @NotNull
    private static InsertHandler<LookupElement> createTemplateBasedInsertHandler(@NotNull String templateId,
                                                                                 @Nullable String traileringString) {
        return (context, item) -> {
            Template template = TemplateSettings.getInstance().getTemplateById(templateId);
            Editor editor = context.getEditor();
            if (template != null) {
                editor.getDocument().deleteString(context.getStartOffset(), context.getTailOffset());
                TemplateManager.getInstance(context.getProject()).startTemplate(editor, template);
            } else {
                int currentOffset = editor.getCaretModel().getOffset();
                CharSequence documentText = editor.getDocument().getImmutableCharSequence();
                if (documentText.length() <= currentOffset || documentText.charAt(currentOffset) != ' ') {
                    if (traileringString != null) {
                        EditorModificationUtil.insertStringAtCaret(editor, traileringString);
                    }
                } else {
                    EditorModificationUtil.moveCaretRelatively(editor, 1);
                }
            }
        };
    }

    /**
     * Creates a <b>Type</b> lookup element.
     *
     * @param name          name of the lookup
     * @param insertHandler insert handler of the lookup
     * @return {@link LookupElementBuilder} which will be used to create the lookup element.
     */
    @NotNull
    private static LookupElementBuilder createTypeLookupElement(@NotNull String name,
                                                                @Nullable InsertHandler<LookupElement> insertHandler) {
        return createLookupElement(name, insertHandler).withTypeText("Type");
    }

    /**
     * Add type names as lookups. Types include <b>any, simple types, reference types</b>.
     *
     * @param resultSet result list which is used to add lookups
     */
    static void addTypeNamesAsLookups(@NotNull CompletionResultSet resultSet) {
        addOtherTypeAsLookup(resultSet);
        addXmlnsAsLookup(resultSet);
        addValueTypesAsLookups(resultSet);
        addReferenceTypesAsLookups(resultSet);
    }

    /**
     * Adds any type as a lookup.
     *
     * @param resultSet result list which is used to add lookups
     */
    static void addOtherTypeAsLookup(@NotNull CompletionResultSet resultSet) {
        resultSet.addElement(PrioritizedLookupElement.withPriority(ANY, VALUE_TYPES_PRIORITY));
        resultSet.addElement(PrioritizedLookupElement.withPriority(VAR, VALUE_TYPES_PRIORITY));
        resultSet.addElement(PrioritizedLookupElement.withPriority(TYPE, VALUE_TYPES_PRIORITY));
    }

    static void addXmlnsAsLookup(@NotNull CompletionResultSet resultSet) {
        resultSet.addElement(PrioritizedLookupElement.withPriority(XMLNS, VALUE_TYPES_PRIORITY));
    }

    /**
     * Adds value types as lookups.
     *
     * @param resultSet result list which is used to add lookups
     */
    static void addValueTypesAsLookups(@NotNull CompletionResultSet resultSet) {
        resultSet.addElement(PrioritizedLookupElement.withPriority(BOOLEAN, VALUE_TYPES_PRIORITY));
        resultSet.addElement(PrioritizedLookupElement.withPriority(INT, VALUE_TYPES_PRIORITY));
        resultSet.addElement(PrioritizedLookupElement.withPriority(FLOAT, VALUE_TYPES_PRIORITY));
        resultSet.addElement(PrioritizedLookupElement.withPriority(STRING, VALUE_TYPES_PRIORITY));
        resultSet.addElement(PrioritizedLookupElement.withPriority(BLOB, VALUE_TYPES_PRIORITY));
    }

    /**
     * Adds reference types as lookups.
     *
     * @param resultSet result list which is used to add lookups
     */
    static void addReferenceTypesAsLookups(@NotNull CompletionResultSet resultSet) {
        resultSet.addElement(PrioritizedLookupElement.withPriority(MESSAGE, REFERENCE_TYPES_PRIORITY));
        resultSet.addElement(PrioritizedLookupElement.withPriority(MAP, REFERENCE_TYPES_PRIORITY));
        resultSet.addElement(PrioritizedLookupElement.withPriority(XML, REFERENCE_TYPES_PRIORITY));
        resultSet.addElement(PrioritizedLookupElement.withPriority(JSON, REFERENCE_TYPES_PRIORITY));
        resultSet.addElement(PrioritizedLookupElement.withPriority(DATATABLE, REFERENCE_TYPES_PRIORITY));
    }

    @NotNull
    static List<LookupElement> getFileLevelKeywordsAsLookups(boolean withPublic, boolean withPackage,
                                                             boolean withImport) {
        List<LookupElement> lookupElements = new LinkedList<>();
        if (withPublic) {
            lookupElements.add(PrioritizedLookupElement.withPriority(PUBLIC, KEYWORDS_PRIORITY));
        }
        if (withPackage) {
            lookupElements.add(PrioritizedLookupElement.withPriority(PACKAGE, KEYWORDS_PRIORITY));
        }
        if (withImport) {
            lookupElements.add(PrioritizedLookupElement.withPriority(IMPORT, KEYWORDS_PRIORITY));
        }
        lookupElements.add(PrioritizedLookupElement.withPriority(CONST, KEYWORDS_PRIORITY));
        lookupElements.add(PrioritizedLookupElement.withPriority(SERVICE, KEYWORDS_PRIORITY));
        lookupElements.add(PrioritizedLookupElement.withPriority(FUNCTION, KEYWORDS_PRIORITY));
        lookupElements.add(PrioritizedLookupElement.withPriority(CONNECTOR, KEYWORDS_PRIORITY));
        lookupElements.add(PrioritizedLookupElement.withPriority(STRUCT, KEYWORDS_PRIORITY));
        lookupElements.add(PrioritizedLookupElement.withPriority(ANNOTATION, KEYWORDS_PRIORITY));
        lookupElements.add(PrioritizedLookupElement.withPriority(XMLNS, KEYWORDS_PRIORITY));
        lookupElements.add(PrioritizedLookupElement.withPriority(ENUM, KEYWORDS_PRIORITY));
        lookupElements.add(PrioritizedLookupElement.withPriority(TRANSFORMER, KEYWORDS_PRIORITY));
        return lookupElements;
    }

    public static List<LookupElement> createAttachmentPointsAsLookups() {
        List<LookupElement> lookupElements = new LinkedList<>();
        lookupElements.add(createKeywordAsLookup(SERVICE));
        lookupElements.add(createKeywordAsLookup(RESOURCE));
        lookupElements.add(createKeywordAsLookup(CONNECTOR));
        lookupElements.add(createKeywordAsLookup(ACTION));
        lookupElements.add(createKeywordAsLookup(FUNCTION));
        lookupElements.add(createKeywordAsLookup(STRUCT));
        lookupElements.add(createKeywordAsLookup(CONST));
        lookupElements.add(createKeywordAsLookup(PARAMETER));
        lookupElements.add(createKeywordAsLookup(ANNOTATION));
        return lookupElements;
    }

    @NotNull
    static List<LookupElement> getWorkerInteractionKeywords() {
        List<LookupElement> keywords = new LinkedList<>();
        keywords.add(createLookupElement("fork", null));
        keywords.add(createLookupElement("default", null));
        return keywords;
    }

    private static LookupElement createKeywordAsLookup(@NotNull LookupElement lookupElement) {
        return PrioritizedLookupElement.withPriority(lookupElement, KEYWORDS_PRIORITY);
    }

    @NotNull
    static List<LookupElement> getCommonKeywords() {
        List<LookupElement> lookupElements = new LinkedList<>();
        lookupElements.add(createKeywordAsLookup(IF));
        lookupElements.add(createKeywordAsLookup(ELSE));
        lookupElements.add(createKeywordAsLookup(FORK));
        lookupElements.add(createKeywordAsLookup(JOIN));
        lookupElements.add(createKeywordAsLookup(TIMEOUT));
        lookupElements.add(createKeywordAsLookup(WORKER));
        lookupElements.add(createKeywordAsLookup(TRANSACTION));
        lookupElements.add(createKeywordAsLookup(FAILED));
        lookupElements.add(createKeywordAsLookup(ABORT));
        lookupElements.add(createKeywordAsLookup(TRY));
        lookupElements.add(createKeywordAsLookup(CATCH));
        lookupElements.add(createKeywordAsLookup(FINALLY));
        lookupElements.add(createKeywordAsLookup(WHILE));
        lookupElements.add(createKeywordAsLookup(NEXT));
        lookupElements.add(createKeywordAsLookup(BREAK));
        lookupElements.add(createKeywordAsLookup(THROW));
        lookupElements.add(createKeywordAsLookup(FOREACH));
        lookupElements.add(createKeywordAsLookup(IN));
        lookupElements.add(createKeywordAsLookup(LOCK));
        return lookupElements;
    }

    @NotNull
    public static List<LookupElement> getValueKeywords() {
        List<LookupElement> lookupElements = new LinkedList<>();
        lookupElements.add(createKeywordAsLookup(TRUE));
        lookupElements.add(createKeywordAsLookup(FALSE));
        lookupElements.add(createKeywordAsLookup(NULL));
        return lookupElements;
    }

    /**
     * Returns keywords used in join conditions.
     */
    static List<LookupElement> getJoinConditionKeywords() {
        List<LookupElement> lookupElements = new LinkedList<>();
        lookupElements.add(createKeywordAsLookup(ALL));
        lookupElements.add(createKeywordAsLookup(SOME));
        return lookupElements;
    }

    @NotNull
    static LookupElement getCreateKeyword() {
        return createKeywordAsLookup(CREATE);
    }

    @NotNull
    static LookupElement getTypeOfKeyword() {
        return createKeywordAsLookup(TYPE_OF);
    }

    @NotNull
    static LookupElement getLengthOfKeyword() {
        return createKeywordAsLookup(LENGTH_OF);
    }

    @NotNull
    static LookupElement getAttachKeyword() {
        return createKeywordAsLookup(ATTACH);
    }

    @NotNull
    static List<LookupElement> getFunctionSpecificKeywords() {
        List<LookupElement> lookupElements = new LinkedList<>();
        lookupElements.add(createKeywordAsLookup(RETURN));
        return lookupElements;
    }

    @NotNull
    static List<LookupElement> getResourceSpecificKeywords() {
        List<LookupElement> lookupElements = new LinkedList<>();
        return lookupElements;
    }

    @NotNull
    static List<LookupElement> getServiceSpecificKeywords() {
        List<LookupElement> lookupElements = new LinkedList<>();
        lookupElements.add(createKeywordAsLookup(RESOURCE));
        return lookupElements;
    }

    @NotNull
    static List<LookupElement> getConnectorSpecificKeywords() {
        List<LookupElement> lookupElements = new LinkedList<>();
        lookupElements.add(createKeywordAsLookup(ACTION));
        return lookupElements;
    }

    static LookupElement getPackageAsLookups(@NotNull PsiDirectory directory,
                                             @Nullable InsertHandler<LookupElement> insertHandler) {
        return LookupElementBuilder.create(directory.getName()).withTypeText("Package")
                .withIcon(BallerinaIcons.PACKAGE).withInsertHandler(insertHandler);
    }

    @NotNull
    private static LookupElementBuilder createPackageLookupElement(@NotNull PsiDirectory directory,
                                                                   @NotNull String name) {
        String suggestedImportPath = BallerinaUtil.suggestPackageNameForDirectory(directory);
        return LookupElementBuilder.createWithSmartPointer(name, directory)
                .withTypeText("Package").withIcon(BallerinaIcons.PACKAGE)
                .withTailText("(" + suggestedImportPath + ")", true)
                .withInsertHandler(directory.getFiles().length == 0 ?
                        PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP : null);
    }

    @NotNull
    public static LookupElement createImportedPackageLookupElement(@NotNull PsiDirectory directory,
                                                                   @NotNull String name,
                                                                   @Nullable InsertHandler<LookupElement> handler) {
        LookupElementBuilder builder = createPackageLookupElement(directory, name).withInsertHandler(handler);
        return PrioritizedLookupElement.withPriority(builder, PACKAGE_PRIORITY);
    }

    @NotNull
    public static LookupElement createUnimportedPackageLookupElement(@NotNull PsiDirectory directory,
                                                                     @NotNull String name,
                                                                     @Nullable InsertHandler<LookupElement> handler) {
        LookupElementBuilder builder = createPackageLookupElement(directory, name).withInsertHandler(handler);
        return PrioritizedLookupElement.withPriority(builder, UNIMPORTED_PACKAGE_PRIORITY);
    }

    @NotNull
    private static LookupElement createAnnotationLookupElement(@NotNull PsiElement element) {
        LookupElementBuilder builder = LookupElementBuilder.createWithSmartPointer(element.getText(), element)
                .withTypeText("Annotation").withIcon(BallerinaIcons.ANNOTATION)
                .withInsertHandler(BracesInsertHandler.INSTANCE_WITH_AUTO_POPUP);
        return PrioritizedLookupElement.withPriority(builder, ANNOTATION_PRIORITY);
    }

    @NotNull
    public static List<LookupElement> createAnnotationLookupElements(@NotNull List<IdentifierPSINode> annotations) {
        List<LookupElement> lookupElements = new LinkedList<>();
        for (IdentifierPSINode annotation : annotations) {
            if (annotation == null) {
                continue;
            }
            LookupElement lookupElement = BallerinaCompletionUtils.createAnnotationLookupElement(annotation);
            lookupElements.add(lookupElement);
        }
        return lookupElements;
    }

    @NotNull
    private static LookupElement createFunctionLookupElement(@NotNull PsiElement element,
                                                             @Nullable InsertHandler<LookupElement> insertHandler) {
        LookupElementBuilder builder = LookupElementBuilder.createWithSmartPointer(element.getText(), element)
                .withTypeText("Function").withIcon(BallerinaIcons.FUNCTION).bold()
                .withTailText(BallerinaDocumentationProvider.getParametersAndReturnTypes(element.getParent()))
                .withInsertHandler(insertHandler);
        return PrioritizedLookupElement.withPriority(builder, FUNCTION_PRIORITY);
    }

    @NotNull
    public static List<LookupElement> createFunctionLookupElements(@NotNull List<IdentifierPSINode> functions) {
        List<LookupElement> lookupElements = new LinkedList<>();
        for (IdentifierPSINode function : functions) {
            if (function == null) {
                continue;
            }
            LookupElement lookupElement = createFunctionLookupElement(function, ParenthesisInsertHandler.INSTANCE);
            lookupElements.add(lookupElement);
        }
        return lookupElements;
    }

    @NotNull
    public static List<LookupElement> createFunctionLookupElements(@NotNull List<IdentifierPSINode> functions,
                                                                   @Nullable InsertHandler<LookupElement>
                                                                           insertHandler) {
        List<LookupElement> lookupElements = new LinkedList<>();
        for (IdentifierPSINode function : functions) {
            if (function == null) {
                continue;
            }
            lookupElements.add(createFunctionLookupElement(function, insertHandler));
        }
        return lookupElements;
    }

    @NotNull
    private static LookupElement createAttachedFunctionsLookupElement(@NotNull PsiElement element,
                                                                      @Nullable InsertHandler<LookupElement>
                                                                              insertHandler) {
        LookupElementBuilder builder = LookupElementBuilder.createWithSmartPointer(element.getText(), element)
                .withTypeText("Function").withIcon(BallerinaIcons.FUNCTION).bold()
                .withTailText(BallerinaDocumentationProvider.getParametersAndReturnTypes(element.getParent()))
                .withInsertHandler(insertHandler);
        return PrioritizedLookupElement.withPriority(builder, FUNCTION_PRIORITY);
    }

    @NotNull
    public static List<LookupElement> createAttachedFunctionsLookupElements(@NotNull Collection<IdentifierPSINode>
                                                                                    functions) {
        List<LookupElement> lookupElements = new LinkedList<>();
        for (PsiElement function : functions) {
            if (function == null) {
                continue;
            }
            LookupElement lookupElement = createAttachedFunctionsLookupElement(function,
                    ParenthesisInsertHandler.INSTANCE);
            lookupElements.add(lookupElement);
        }
        return lookupElements;
    }

    @NotNull
    private static LookupElement createConnectorLookupElement(@NotNull IdentifierPSINode element,
                                                              @Nullable InsertHandler<LookupElement> insertHandler) {
        LookupElementBuilder builder = LookupElementBuilder.createWithSmartPointer(element.getText(), element)
                .withTypeText("Connector").withIcon(BallerinaIcons.CONNECTOR).bold()
                .withTailText(BallerinaDocumentationProvider.getParameterString(element.getParent(), true))
                .withInsertHandler(insertHandler);
        return PrioritizedLookupElement.withPriority(builder, CONNECTOR_PRIORITY);
    }

    @NotNull
    public static List<LookupElement> createConnectorLookupElements(@NotNull List<IdentifierPSINode> connectors,
                                                                    @Nullable InsertHandler<LookupElement>
                                                                            insertHandler) {
        List<LookupElement> lookupElements = new LinkedList<>();
        for (IdentifierPSINode connector : connectors) {
            LookupElement lookupElement = createConnectorLookupElement(connector, insertHandler);
            lookupElements.add(lookupElement);
        }
        return lookupElements;
    }

    @NotNull
    private static LookupElement createActionLookupElement(@NotNull IdentifierPSINode identifier) {
        LookupElementBuilder builder = LookupElementBuilder.create(identifier.getText())
                .withTypeText("Action").withIcon(BallerinaIcons.ACTION).bold()
                .withTailText(BallerinaDocumentationProvider.getParametersAndReturnTypes(identifier.getParent()))
                .withInsertHandler(ParenthesisInsertHandler.INSTANCE);
        return PrioritizedLookupElement.withPriority(builder, ACTION_PRIORITY);
    }

    @NotNull
    public static List<LookupElement> createActionLookupElements(@NotNull List<IdentifierPSINode> actions) {
        List<LookupElement> lookupElements = new LinkedList<>();
        for (IdentifierPSINode action : actions) {
            if (action == null) {
                continue;
            }
            lookupElements.add(createActionLookupElement(action));
        }
        return lookupElements;
    }

    @NotNull
    private static LookupElement createStructLookupElement(@NotNull IdentifierPSINode element) {
        LookupElementBuilder builder = LookupElementBuilder.createWithSmartPointer(element.getText(), element)
                .withTypeText("Struct").withIcon(BallerinaIcons.STRUCT)
                .withInsertHandler(AddSpaceInsertHandler.INSTANCE);
        return PrioritizedLookupElement.withPriority(builder, STRUCT_PRIORITY);
    }

    @NotNull
    public static List<LookupElement> createStructLookupElements(@NotNull List<IdentifierPSINode> structs) {
        List<LookupElement> lookupElements = new LinkedList<>();
        for (IdentifierPSINode struct : structs) {
            if (struct == null) {
                continue;
            }
            lookupElements.add(createStructLookupElement(struct));
        }
        return lookupElements;
    }

    @NotNull
    private static LookupElement createEnumLookupElement(@NotNull IdentifierPSINode element,
                                                         @Nullable InsertHandler<LookupElement> insertHandler) {
        LookupElementBuilder builder = LookupElementBuilder.createWithSmartPointer(element.getText(), element)
                .withTypeText("Enum").withIcon(BallerinaIcons.ENUM)
                .withInsertHandler(insertHandler);
        return PrioritizedLookupElement.withPriority(builder, ENUM_PRIORITY);
    }

    @NotNull
    public static List<LookupElement> createEnumLookupElements(@NotNull List<IdentifierPSINode> enums,
                                                               @Nullable InsertHandler<LookupElement> insertHandler) {
        List<LookupElement> lookupElements = new LinkedList<>();
        for (IdentifierPSINode anEnum : enums) {
            if (anEnum == null) {
                continue;
            }
            lookupElements.add(createEnumLookupElement(anEnum, insertHandler));
        }
        return lookupElements;
    }

    @NotNull
    public static LookupElement createVariableLookupElement(@NotNull IdentifierPSINode element) {
        LookupElementBuilder builder = LookupElementBuilder.createWithSmartPointer(element.getText(), element)
                .withTypeText("Variable").withIcon(BallerinaIcons.VARIABLE);
        return PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY);
    }

    @NotNull
    public static List<LookupElement> createVariableLookupElements(@NotNull List<IdentifierPSINode> variables) {
        List<LookupElement> lookupElements = new LinkedList<>();
        for (IdentifierPSINode variable : variables) {
            if (variable == null) {
                continue;
            }
            LookupElement lookupElement = BallerinaCompletionUtils.createVariableLookupElement(variable);
            lookupElements.add(lookupElement);
        }
        return lookupElements;
    }

    @NotNull
    public static LookupElement createParameterLookupElement(@NotNull IdentifierPSINode element) {
        LookupElementBuilder builder = LookupElementBuilder.createWithSmartPointer(element.getText(), element)
                .withTypeText("Parameter").withIcon(BallerinaIcons.PARAMETER);
        return PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY);
    }

    @NotNull
    public static List<LookupElement> createParameterLookupElements(@NotNull List<IdentifierPSINode> parameters) {
        List<LookupElement> lookupElements = new LinkedList<>();
        for (IdentifierPSINode parameter : parameters) {
            if (parameter == null) {
                continue;
            }
            LookupElement lookupElement = createParameterLookupElement(parameter);
            lookupElements.add(lookupElement);
        }
        return lookupElements;
    }

    @NotNull
    private static LookupElement createConstantLookupElement(@NotNull IdentifierPSINode element) {
        LookupElementBuilder builder = LookupElementBuilder.createWithSmartPointer(element.getText(), element)
                .withTypeText("Constant").withIcon(BallerinaIcons.CONSTANT);
        return PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY);
    }

    @NotNull
    public static List<LookupElement> createConstantLookupElements(@NotNull List<IdentifierPSINode> constants) {
        List<LookupElement> lookupElements = new LinkedList<>();
        for (IdentifierPSINode constant : constants) {
            if (constant == null) {
                continue;
            }
            LookupElement lookupElement = BallerinaCompletionUtils.createConstantLookupElement(constant);
            lookupElements.add(lookupElement);
        }
        return lookupElements;
    }

    @NotNull
    public static LookupElement createGlobalVariableLookupElement(@NotNull PsiElement element) {
        LookupElementBuilder builder = LookupElementBuilder.createWithSmartPointer(element.getText(), element)
                .withTypeText("Variable").withIcon(BallerinaIcons.GLOBAL_VARIABLE);
        return PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY);
    }

    @NotNull
    public static List<LookupElement> createGlobalVariableLookupElements(@NotNull List<IdentifierPSINode> variables) {
        List<LookupElement> lookupElements = new LinkedList<>();
        for (IdentifierPSINode variable : variables) {
            if (variable == null) {
                continue;
            }
            LookupElement lookupElement = createGlobalVariableLookupElement(variable);
            lookupElements.add(lookupElement);
        }
        return lookupElements;
    }

    @NotNull
    private static LookupElement createNamespaceLookupElement(@NotNull PsiElement element) {
        LookupElementBuilder builder = LookupElementBuilder.createWithSmartPointer(element.getText(), element)
                .withTypeText("Namespace").withIcon(BallerinaIcons.NAMESPACE);
        return PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY);
    }

    @NotNull
    public static List<LookupElement> createNamespaceLookupElements(@NotNull List<PsiElement> namespaces) {
        List<LookupElement> lookupElements = new LinkedList<>();
        for (PsiElement namespace : namespaces) {
            if (namespace == null) {
                continue;
            }
            LookupElement lookupElement = BallerinaCompletionUtils.createNamespaceLookupElement(namespace);
            lookupElements.add(lookupElement);
        }
        return lookupElements;
    }

    @NotNull
    private static LookupElement createEndpointLookupElement(@NotNull PsiElement element) {
        LookupElementBuilder builder = LookupElementBuilder.createWithSmartPointer(element.getText(), element)
                .withTypeText("Endpoint").withIcon(BallerinaIcons.ENDPOINT);
        return PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY);
    }

    @NotNull
    public static List<LookupElement> createEndpointLookupElements(@NotNull List<IdentifierPSINode> endpoints) {
        List<LookupElement> lookupElements = new LinkedList<>();
        for (PsiElement endpoint : endpoints) {
            if (endpoint == null) {
                continue;
            }
            LookupElement lookupElement = BallerinaCompletionUtils.createEndpointLookupElement(endpoint);
            lookupElements.add(lookupElement);
        }
        return lookupElements;
    }

    @NotNull
    private static LookupElement createTransformerLookupElement(@NotNull PsiElement element) {
        LookupElementBuilder builder = LookupElementBuilder.createWithSmartPointer(element.getText(), element)
                .withTypeText("Transformer").withIcon(BallerinaIcons.TRANSFORMER)
                .withInsertHandler(ParenthesisInsertHandler.INSTANCE);
        return PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY);
    }

    @NotNull
    public static List<LookupElement> createTransformerLookupElements(@NotNull List<IdentifierPSINode> transformers) {
        List<LookupElement> lookupElements = new LinkedList<>();
        for (IdentifierPSINode transformer : transformers) {
            if (transformer == null) {
                continue;
            }
            LookupElement lookupElement = BallerinaCompletionUtils.createTransformerLookupElement(transformer);
            lookupElements.add(lookupElement);
        }
        return lookupElements;
    }

    @NotNull
    private static LookupElementBuilder createEnumFieldLookupElement(@NotNull IdentifierPSINode fieldName,
                                                                     @NotNull IdentifierPSINode ownerName) {
        return LookupElementBuilder.createWithSmartPointer(fieldName.getText(), fieldName)
                .withTypeText(ownerName.getText()).withIcon(BallerinaIcons.FIELD);
    }

    @NotNull
    public static List<LookupElement> createEnumFieldLookupElements(@NotNull Collection<EnumFieldNode> enumFieldNodes,
                                                                    @NotNull IdentifierPSINode definitionName) {
        List<LookupElement> lookupElements = new LinkedList<>();
        for (EnumFieldNode fieldDefinitionNode : enumFieldNodes) {
            IdentifierPSINode fieldName = PsiTreeUtil.getChildOfType(fieldDefinitionNode, IdentifierPSINode.class);
            if (fieldName == null) {
                continue;
            }
            LookupElement lookupElement = BallerinaCompletionUtils.createEnumFieldLookupElement(fieldName,
                    definitionName);
            lookupElements.add(lookupElement);
        }
        return lookupElements;
    }

    @NotNull
    private static LookupElementBuilder createFieldLookupElement(@NotNull IdentifierPSINode fieldName,
                                                                 @NotNull TypeNameNode fieldType,
                                                                 @NotNull IdentifierPSINode ownerName) {
        return LookupElementBuilder.createWithSmartPointer(fieldName.getText(), fieldName)
                .withTypeText(fieldType.getText()).withIcon(BallerinaIcons.FIELD)
                .withTailText(" -> " + ownerName.getText(), true);
    }

    @NotNull
    private static LookupElement createFieldLookupElement(@NotNull IdentifierPSINode fieldName,
                                                          @NotNull TypeNameNode fieldType,
                                                          @NotNull IdentifierPSINode ownerName,
                                                          @Nullable InsertHandler<LookupElement> insertHandler) {
        LookupElementBuilder builder = createFieldLookupElement(fieldName, fieldType, ownerName)
                .withInsertHandler(insertHandler);
        return PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY);
    }

    @NotNull
    private static LookupElementBuilder createFieldLookupElement(@NotNull IdentifierPSINode fieldName,
                                                                 @NotNull TypeNameNode fieldType) {
        return LookupElementBuilder.createWithSmartPointer(fieldName.getText(), fieldName)
                .withTypeText(fieldType.getText()).withIcon(BallerinaIcons.FIELD);
    }

    @NotNull
    private static LookupElement createFieldLookupElement(@NotNull IdentifierPSINode fieldName,
                                                          @NotNull TypeNameNode fieldType,
                                                          @Nullable InsertHandler<LookupElement> insertHandler) {
        LookupElementBuilder builder = createFieldLookupElement(fieldName, fieldType)
                .withInsertHandler(insertHandler);
        return PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY);
    }

    @NotNull
    public static List<LookupElement> createFieldLookupElements(@NotNull Collection<FieldDefinitionNode>
                                                                        fieldDefinitionNodes,
                                                                @NotNull IdentifierPSINode definitionName,
                                                                @Nullable InsertHandler<LookupElement> insertHandler) {
        List<LookupElement> lookupElements = new LinkedList<>();
        for (FieldDefinitionNode fieldDefinitionNode : fieldDefinitionNodes) {
            IdentifierPSINode fieldName = PsiTreeUtil.getChildOfType(fieldDefinitionNode, IdentifierPSINode.class);
            TypeNameNode fieldType = PsiTreeUtil.getChildOfType(fieldDefinitionNode, TypeNameNode.class);
            if (fieldName == null || fieldType == null) {
                continue;
            }
            LookupElement lookupElement = BallerinaCompletionUtils.createFieldLookupElement(fieldName, fieldType,
                    definitionName, insertHandler);
            lookupElements.add(lookupElement);
        }
        return lookupElements;
    }

    @NotNull
    public static List<LookupElement> createFieldLookupElements(@NotNull Collection<FieldDefinitionNode>
                                                                        fieldDefinitionNodes,
                                                                @Nullable InsertHandler<LookupElement> insertHandler) {
        List<LookupElement> lookupElements = new LinkedList<>();
        for (FieldDefinitionNode fieldDefinitionNode : fieldDefinitionNodes) {
            IdentifierPSINode fieldName = PsiTreeUtil.getChildOfType(fieldDefinitionNode, IdentifierPSINode.class);
            TypeNameNode fieldType = PsiTreeUtil.getChildOfType(fieldDefinitionNode, TypeNameNode.class);
            if (fieldName == null || fieldType == null) {
                continue;
            }
            LookupElement lookupElement = BallerinaCompletionUtils.createFieldLookupElement(fieldName, fieldType,
                    insertHandler);
            lookupElements.add(lookupElement);
        }
        return lookupElements;
    }

    @NotNull
    private static LookupElement createWorkerLookupElement(@NotNull IdentifierPSINode workerName) {
        LookupElementBuilder builder = LookupElementBuilder.createWithSmartPointer(workerName.getText(), workerName)
                .withTypeText("Worker").withIcon(BallerinaIcons.WORKER);
        return PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY);
    }

    @NotNull
    public static List<LookupElement> createWorkerLookupElements(@NotNull Collection<WorkerDeclarationNode>
                                                                         workerDeclarationNodes) {
        List<LookupElement> lookupElements = new LinkedList<>();
        for (WorkerDeclarationNode workerDeclarationNode : workerDeclarationNodes) {
            IdentifierPSINode workerName = PsiTreeUtil.getChildOfType(workerDeclarationNode, IdentifierPSINode.class);
            if (workerName == null) {
                continue;
            }
            LookupElement lookupElement = createWorkerLookupElement(workerName);
            lookupElements.add(lookupElement);
        }
        return lookupElements;
    }
}

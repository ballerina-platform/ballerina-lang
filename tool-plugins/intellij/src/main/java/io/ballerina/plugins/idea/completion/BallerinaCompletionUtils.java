/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.plugins.idea.completion;

import com.intellij.codeInsight.completion.AddSpaceInsertHandler;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TemplateSettings;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import io.ballerina.plugins.idea.completion.inserthandlers.BracesInsertHandler;
import icons.BallerinaIcons;
import io.ballerina.plugins.idea.psi.BallerinaAnyIdentifierName;
import io.ballerina.plugins.idea.psi.BallerinaCallableUnitSignature;
import io.ballerina.plugins.idea.psi.BallerinaFormalParameterList;
import io.ballerina.plugins.idea.psi.BallerinaFunctionDefinition;
import io.ballerina.plugins.idea.psi.BallerinaObjectFunctionDefinition;
import io.ballerina.plugins.idea.psi.BallerinaReturnParameter;
import io.ballerina.plugins.idea.psi.BallerinaReturnType;
import io.ballerina.plugins.idea.psi.BallerinaUserDefineTypeName;
import io.ballerina.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import io.ballerina.plugins.idea.psi.impl.BallerinaTopLevelDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Util methods and constants related to code completion.
 */
public class BallerinaCompletionUtils {

    private static final int VARIABLE_PRIORITY = 20;
    private static final int FUNCTION_PRIORITY = VARIABLE_PRIORITY - 1;
    private static final int GLOBAL_VARIABLE_PRIORITY = FUNCTION_PRIORITY - 1;
    private static final int VALUE_TYPES_PRIORITY = VARIABLE_PRIORITY - 2;
    private static final int REFERENCE_TYPES_PRIORITY = VARIABLE_PRIORITY - 2;
    private static final int TYPE_PRIORITY = VARIABLE_PRIORITY - 2;
    private static final int PACKAGE_PRIORITY = VALUE_TYPES_PRIORITY - 1;
    private static final int UNIMPORTED_PACKAGE_PRIORITY = PACKAGE_PRIORITY - 1;
    private static final int CONNECTOR_PRIORITY = VALUE_TYPES_PRIORITY - 1;
    private static final int ACTION_PRIORITY = VALUE_TYPES_PRIORITY - 1;
    private static final int ANNOTATION_PRIORITY = VALUE_TYPES_PRIORITY - 1;
    private static final int ENUM_PRIORITY = VALUE_TYPES_PRIORITY - 1;
    public static final int KEYWORDS_PRIORITY = VALUE_TYPES_PRIORITY - 2;

    public static final Key<String> HAS_A_RETURN_VALUE = Key.create("HAS_A_RETURN_VALUE");
    public static final Key<String> REQUIRE_PARAMETERS = Key.create("REQUIRE_PARAMETERS");

    public static final Key<String> ORGANIZATION_NAME = Key.create("ORGANIZATION_NAME");

    public static final Key<String> PUBLIC_DEFINITIONS_ONLY = Key.create("PUBLIC_DEFINITIONS_ONLY");

    private BallerinaCompletionUtils() {

    }

    /**
     * Creates a lookup element.
     *
     * @param name name of the lookup
     * @return {@link LookupElementBuilder} which will be used to create the lookup element.
     */
    @NotNull
    private static LookupElementBuilder createLookupElement(@NotNull String name) {
        return LookupElementBuilder.create(name).withBoldness(true);
    }

    /**
     * Creates a lookup element.
     *
     * @param name          name of the lookup
     * @param insertHandler insert handler of the lookup
     * @return {@link LookupElementBuilder} which will be used to create the lookup element.
     */
    private static LookupElementBuilder createLookupElement(@NotNull String name,
            @Nullable InsertHandler<LookupElement> insertHandler) {
        return createLookupElement(name).withInsertHandler(insertHandler);
    }

    /**
     * Creates a keyword lookup element.
     *
     * @param name name of the lookup
     * @return {@link LookupElementBuilder} which will be used to create the lookup element.
     */
    @NotNull
    public static LookupElementBuilder createKeywordLookupElement(@NotNull String name) {
        return createKeywordLookupElement(name, " ");
    }

    @NotNull
    private static LookupElementBuilder createKeywordLookupElement(@NotNull String name,
            @Nullable String traileringString) {

        return createLookupElement(name, createTemplateBasedInsertHandler("ballerina_lang_" + name, traileringString));
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

    public static LookupElement createPackageLookup(@NotNull PsiElement identifier,
            @Nullable InsertHandler<LookupElement> insertHandler) {
        LookupElementBuilder builder = LookupElementBuilder.create(identifier.getText()).withTypeText("Package")
                .withIcon(BallerinaIcons.PACKAGE).withInsertHandler(insertHandler);
        return PrioritizedLookupElement.withPriority(builder, PACKAGE_PRIORITY);
    }

    public static LookupElement createUnImportedPackageLookup(@Nullable String organization,
            @NotNull String packageName, @NotNull PsiElement element,
            @Nullable InsertHandler<LookupElement> insertHandler) {
        if (organization != null) {
            element.putUserData(ORGANIZATION_NAME, organization);
        }
        LookupElementBuilder builder = LookupElementBuilder.create(element, packageName).withTypeText("Package")
                .withIcon(BallerinaIcons.PACKAGE).withInsertHandler(insertHandler);
        if (organization != null) {
            builder = builder.withTailText("(" + organization + "/" + packageName + ")", true);
        } else {
            builder = builder.withTailText("(" + packageName + ")", true);
        }
        return PrioritizedLookupElement.withPriority(builder, PACKAGE_PRIORITY);
    }

    // Todo - Update icon getting logic to get the icon from a util method.
    @NotNull
    public static LookupElement createFunctionLookupElement(@NotNull BallerinaTopLevelDefinition definition,
            @Nullable InsertHandler<LookupElement> insertHandler) {
        return createFunctionLookupElementWithSemicolon(definition, insertHandler, true);
    }

    @NotNull
    public static LookupElement createFunctionLookupElementWithSemicolon(
            @NotNull BallerinaTopLevelDefinition definition, @Nullable InsertHandler<LookupElement> insertHandler,
            boolean withSemicolon) {
        LookupElementBuilder builder = LookupElementBuilder
                .createWithSmartPointer(definition.getIdentifier().getText(), definition)
                .withIcon(definition.getIcon(Iconable.ICON_FLAG_VISIBILITY)).bold().withInsertHandler(insertHandler);
        if (definition instanceof BallerinaFunctionDefinition) {
            BallerinaCallableUnitSignature callableUnitSignature = ((BallerinaFunctionDefinition) definition)
                    .getCallableUnitSignature();
            if (callableUnitSignature != null) {
                // Add parameters.
                BallerinaReturnParameter returnParameter = callableUnitSignature.getReturnParameter();
                if (returnParameter != null) {
                    BallerinaReturnType returnType = returnParameter.getReturnType();
                    if (returnType != null) {
                        builder = builder
                                .withTypeText(BallerinaPsiImplUtil.formatBallerinaFunctionReturnType(returnType));
                    }
                } else {
                    builder = builder.withTypeText("nil");
                    if (withSemicolon) {
                        definition.putUserData(HAS_A_RETURN_VALUE, "nil");
                    }
                }
                // Add return type.
                BallerinaFormalParameterList formalParameterList = callableUnitSignature.getFormalParameterList();

                builder = builder
                        .withTailText(BallerinaPsiImplUtil.formatBallerinaFunctionParameters(formalParameterList));
                if (formalParameterList != null) {
                    definition.putUserData(REQUIRE_PARAMETERS, "YES");
                }
            }
        }
        return PrioritizedLookupElement.withPriority(builder, FUNCTION_PRIORITY);
    }

    @NotNull
    public static LookupElement createFunctionLookupElement(@NotNull BallerinaObjectFunctionDefinition definition,
            @NotNull PsiElement owner, @Nullable InsertHandler<LookupElement> insertHandler) {

        BallerinaCallableUnitSignature objectCallableUnitSignature = definition.getCallableUnitSignature();
        // We check and confirm that the objectCallableUnitSignature != null before calling the method.
        BallerinaAnyIdentifierName anyIdentifierName = objectCallableUnitSignature.getAnyIdentifierName();
        PsiElement identifier = anyIdentifierName.getIdentifier();
        LookupElementBuilder builder = LookupElementBuilder.createWithSmartPointer(identifier.getText(), identifier)
                .withIcon(BallerinaIcons.FUNCTION).bold().withInsertHandler(insertHandler);
        // Add parameters.
        BallerinaReturnParameter returnParameter = objectCallableUnitSignature.getReturnParameter();
        if (returnParameter != null) {
            BallerinaReturnType returnType = returnParameter.getReturnType();
            if (returnType != null) {
                builder = builder.withTypeText(BallerinaPsiImplUtil.formatBallerinaFunctionReturnType(returnType));
            }
        } else {
            builder = builder.withTypeText("nil");
            identifier.putUserData(HAS_A_RETURN_VALUE, "nil");
        }
        // Add return type.
        BallerinaFormalParameterList formalParameterList = objectCallableUnitSignature.getFormalParameterList();
        builder = builder.withTailText(
                BallerinaPsiImplUtil.formatBallerinaFunctionParameters(formalParameterList) + " -> " + owner.getText());
        if (formalParameterList != null) {
            identifier.putUserData(REQUIRE_PARAMETERS, "YES");
        }
        return PrioritizedLookupElement.withPriority(builder, FUNCTION_PRIORITY);
    }

    @NotNull
    public static LookupElement createTypeLookupElement(@NotNull BallerinaTopLevelDefinition definition) {
        return createTypeLookupElement(definition, AddSpaceInsertHandler.INSTANCE);
    }

    @NotNull
    public static LookupElement createTypeLookupElement(@NotNull BallerinaTopLevelDefinition definition,
            @Nullable InsertHandler<LookupElement> insertHandler) {
        LookupElementBuilder builder = LookupElementBuilder
                .createWithSmartPointer(definition.getIdentifier().getText(), definition)
                .withInsertHandler(insertHandler).withTypeText("Type")
                .withIcon(definition.getIcon(Iconable.ICON_FLAG_VISIBILITY)).bold();
        return PrioritizedLookupElement.withPriority(builder, TYPE_PRIORITY);
    }

    @NotNull
    public static LookupElement createVariableLookupElement(@NotNull PsiElement element, @Nullable String type) {
        LookupElementBuilder builder = LookupElementBuilder.createWithSmartPointer(element.getText(), element)
                .withIcon(BallerinaIcons.VARIABLE);
        if (type == null || type.isEmpty()) {
            builder = builder.withTypeText("Variable");
        } else {
            builder = builder.withTypeText(type);
        }
        return PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY);
    }

    @NotNull
    public static LookupElement createParameterLookupElement(@NotNull PsiElement element, @Nullable String type,
            @Nullable String defaultValue) {
        // Todo - Add support to render default value
        LookupElementBuilder builder = LookupElementBuilder.createWithSmartPointer(element.getText(), element)
                .withIcon(BallerinaIcons.PARAMETER);
        if (type == null || type.isEmpty()) {
            builder = builder.withTypeText("Parameter");
        } else {
            builder = builder.withTypeText(type);
        }

        if (defaultValue != null && !defaultValue.isEmpty()) {
            builder = builder.withTailText(" ( = " + defaultValue + " )");
        }

        return PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY);
    }

    @NotNull
    public static LookupElement createWorkerLookupElement(@NotNull PsiElement workerName) {
        LookupElementBuilder builder = LookupElementBuilder.createWithSmartPointer(workerName.getText(), workerName)
                .withTypeText("Worker").withIcon(BallerinaIcons.WORKER);
        return PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY);
    }

    @NotNull
    public static LookupElement createNamespaceLookupElement(@NotNull PsiElement element) {
        LookupElementBuilder builder = LookupElementBuilder.createWithSmartPointer(element.getText(), element)
                .withTypeText("Namespace").withIcon(BallerinaIcons.NAMESPACE);
        return PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY);
    }

    @NotNull
    public static LookupElement createServiceLookupElement(@NotNull PsiElement element) {
        LookupElementBuilder builder = LookupElementBuilder.createWithSmartPointer(element.getText(), element)
                .withTypeText("Service").withIcon(BallerinaIcons.SERVICE);
        return PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY);
    }

    @NotNull
    public static LookupElement createFieldLookupElement(@NotNull PsiElement fieldName, @NotNull PsiElement ownerName,
            @NotNull String type, @Nullable String defaultValue, @Nullable InsertHandler<LookupElement> insertHandler,
            boolean isPublic, boolean isPrivate) {
        LookupElementBuilder lookupElementBuilder = LookupElementBuilder
                .createWithSmartPointer(fieldName.getText(), fieldName).withInsertHandler(insertHandler)
                .withTypeText(type).bold();

        if (defaultValue == null || defaultValue.isEmpty()) {
            lookupElementBuilder = lookupElementBuilder.withTailText(" -> " + ownerName.getText(), true);
        } else {
            String tailText = "(= " + defaultValue + ") -> " + ownerName.getText();
            lookupElementBuilder = lookupElementBuilder.withTailText(tailText, true);
        }

        if (isPublic) {
            lookupElementBuilder = lookupElementBuilder.withIcon(BallerinaIcons.PUBLIC_FIELD);
        } else {
            lookupElementBuilder = lookupElementBuilder.withIcon(BallerinaIcons.PRIVATE_FIELD);
        }
        return PrioritizedLookupElement.withPriority(lookupElementBuilder, VARIABLE_PRIORITY);
    }

    @NotNull
    public static LookupElement createAnnotationLookupElement(@NotNull PsiElement identifier) {
        BallerinaUserDefineTypeName userDefineTypeName = PsiTreeUtil
                .getNextSiblingOfType(identifier, BallerinaUserDefineTypeName.class);
        LookupElementBuilder builder = LookupElementBuilder.createWithSmartPointer(identifier.getText(), identifier)
                .withTypeText("Annotation").withIcon(BallerinaIcons.ANNOTATION).withInsertHandler(
                        userDefineTypeName != null ? BracesInsertHandler.INSTANCE : AddSpaceInsertHandler.INSTANCE);
        return PrioritizedLookupElement.withPriority(builder, ANNOTATION_PRIORITY);
    }
}

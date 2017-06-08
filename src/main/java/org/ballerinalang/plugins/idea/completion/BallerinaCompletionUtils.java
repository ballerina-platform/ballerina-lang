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
import com.intellij.codeInsight.completion.CompletionParameters;
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
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.BallerinaIcons;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.documentation.BallerinaDocumentationProvider;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.AliasNode;
import org.ballerinalang.plugins.idea.psi.AnnotationDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ConnectorDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ConstantDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.PackagePathNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ServiceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TypeMapperNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import org.ballerinalang.plugins.idea.util.BallerinaUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
    private static final int KEYWORDS_PRIORITY = VALUE_TYPES_PRIORITY - 2;

    // File level keywords
    private static final LookupElementBuilder PACKAGE;
    private static final LookupElementBuilder IMPORT;
    private static final LookupElementBuilder CONST;
    private static final LookupElementBuilder SERVICE;
    static final LookupElementBuilder RESOURCE;
    private static final LookupElementBuilder FUNCTION;
    private static final LookupElementBuilder CONNECTOR;
    private static final LookupElementBuilder ACTION;
    private static final LookupElementBuilder STRUCT;
    private static final LookupElementBuilder TYPEMAPPER;
    private static final LookupElementBuilder ANNOTATION;
    static final LookupElementBuilder ATTACH;
    private static final LookupElementBuilder PARAMETER;

    // Any type
    private static final LookupElementBuilder ANY;

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
    private static final LookupElementBuilder XML_DOCUMENT;
    private static final LookupElementBuilder JSON;
    private static final LookupElementBuilder DATATABLE;

    // Other keywords
    private static final LookupElementBuilder REPLY;
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
    private static final LookupElementBuilder TRANSFORM;
    private static final LookupElementBuilder TRANSACTION;
    private static final LookupElementBuilder ABORT;
    private static final LookupElementBuilder ABORTED;
    private static final LookupElementBuilder TRY;
    private static final LookupElementBuilder CATCH;
    private static final LookupElementBuilder FINALLY;
    private static final LookupElementBuilder ITERATE;
    private static final LookupElementBuilder WHILE;
    private static final LookupElementBuilder CONTINUE;
    private static final LookupElementBuilder BREAK;
    private static final LookupElementBuilder THROW;

    private static final LookupElementBuilder TRUE;
    private static final LookupElementBuilder FALSE;
    private static final LookupElementBuilder NULL;


    static {
        PACKAGE = createKeywordLookupElement("package");
        IMPORT = createKeywordLookupElement("import");
        CONST = createKeywordLookupElement("const");
        SERVICE = createKeywordLookupElement("service");
        RESOURCE = createKeywordLookupElement("resource");
        FUNCTION = createKeywordLookupElement("function");
        CONNECTOR = createKeywordLookupElement("connector");
        ACTION = createKeywordLookupElement("action");
        STRUCT = createKeywordLookupElement("struct");
        TYPEMAPPER = createKeywordLookupElement("typemapper");
        ANNOTATION = createKeywordLookupElement("annotation");
        ATTACH = createKeywordLookupElement("attach");
        PARAMETER = createKeywordLookupElement("parameter");

        ANY = createTypeLookupElement("any", AddSpaceInsertHandler.INSTANCE);

        BOOLEAN = createTypeLookupElement("boolean", AddSpaceInsertHandler.INSTANCE);
        INT = createTypeLookupElement("int", AddSpaceInsertHandler.INSTANCE);
        FLOAT = createTypeLookupElement("float", AddSpaceInsertHandler.INSTANCE);
        STRING = createTypeLookupElement("string", AddSpaceInsertHandler.INSTANCE);
        BLOB = createTypeLookupElement("blob", AddSpaceInsertHandler.INSTANCE);

        MESSAGE = createTypeLookupElement("message", AddSpaceInsertHandler.INSTANCE);
        MAP = createTypeLookupElement("map", AddSpaceInsertHandler.INSTANCE);
        XML = createTypeLookupElement("xml", AddSpaceInsertHandler.INSTANCE);
        XML_DOCUMENT = createTypeLookupElement("xmlDocument", AddSpaceInsertHandler.INSTANCE);
        JSON = createTypeLookupElement("json", AddSpaceInsertHandler.INSTANCE);
        DATATABLE = createTypeLookupElement("datatable", AddSpaceInsertHandler.INSTANCE);

        REPLY = createKeywordLookupElement("reply");
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
        TRANSFORM = createKeywordLookupElement("transform");
        TRANSACTION = createKeywordLookupElement("transaction");
        ABORT = createKeywordLookupElement("abort");
        ABORTED = createKeywordLookupElement("aborted");
        TRY = createKeywordLookupElement("try");
        CATCH = createKeywordLookupElement("catch");
        FINALLY = createKeywordLookupElement("finally");
        ITERATE = createKeywordLookupElement("iterate");
        WHILE = createKeywordLookupElement("while");
        CONTINUE = createKeywordLookupElement("continue");
        BREAK = createKeywordLookupElement("break");
        THROW = createKeywordLookupElement("throw");

        TRUE = createKeywordLookupElement("true");
        FALSE = createKeywordLookupElement("false");
        NULL = createKeywordLookupElement("null");
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
        return createLookupElement(name, createTemplateBasedInsertHandler("ballerina_lang_" + name));
    }


    @Nullable
    public static InsertHandler<LookupElement> createTemplateBasedInsertHandler(@NotNull String templateId) {
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
                    EditorModificationUtil.insertStringAtCaret(editor, " ");
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
        addAnyTypeAsLookup(resultSet);
        addValueTypesAsLookups(resultSet);
        addReferenceTypesAsLookups(resultSet);
    }

    /**
     * Adds any type as a lookup.
     *
     * @param resultSet result list which is used to add lookups
     */
    private static void addAnyTypeAsLookup(@NotNull CompletionResultSet resultSet) {
        resultSet.addElement(PrioritizedLookupElement.withPriority(ANY, VALUE_TYPES_PRIORITY));
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
    private static void addReferenceTypesAsLookups(@NotNull CompletionResultSet resultSet) {
        resultSet.addElement(PrioritizedLookupElement.withPriority(MESSAGE, REFERENCE_TYPES_PRIORITY));
        resultSet.addElement(PrioritizedLookupElement.withPriority(MAP, REFERENCE_TYPES_PRIORITY));
        resultSet.addElement(PrioritizedLookupElement.withPriority(XML, REFERENCE_TYPES_PRIORITY));
        resultSet.addElement(PrioritizedLookupElement.withPriority(XML_DOCUMENT, REFERENCE_TYPES_PRIORITY));
        resultSet.addElement(PrioritizedLookupElement.withPriority(JSON, REFERENCE_TYPES_PRIORITY));
        resultSet.addElement(PrioritizedLookupElement.withPriority(DATATABLE, REFERENCE_TYPES_PRIORITY));
    }

    /**
     * Adds file level keywords as lookup elements.
     *
     * @param resultSet   result list which is used to add lookups
     * @param withPackage indicates whether to add 'package' keyword
     * @param withImport  indicates whether to add 'import' keyword
     */
    static void addFileLevelKeywordsAsLookups(@NotNull CompletionResultSet resultSet, boolean withPackage,
                                              boolean withImport) {
        if (withPackage) {
            resultSet.addElement(PrioritizedLookupElement.withPriority(PACKAGE, KEYWORDS_PRIORITY));
        }
        if (withImport) {
            resultSet.addElement(PrioritizedLookupElement.withPriority(IMPORT, KEYWORDS_PRIORITY));
        }
        resultSet.addElement(PrioritizedLookupElement.withPriority(CONST, KEYWORDS_PRIORITY));
        resultSet.addElement(PrioritizedLookupElement.withPriority(SERVICE, KEYWORDS_PRIORITY));
        resultSet.addElement(PrioritizedLookupElement.withPriority(FUNCTION, KEYWORDS_PRIORITY));
        resultSet.addElement(PrioritizedLookupElement.withPriority(CONNECTOR, KEYWORDS_PRIORITY));
        resultSet.addElement(PrioritizedLookupElement.withPriority(STRUCT, KEYWORDS_PRIORITY));
        resultSet.addElement(PrioritizedLookupElement.withPriority(TYPEMAPPER, KEYWORDS_PRIORITY));
        resultSet.addElement(PrioritizedLookupElement.withPriority(ANNOTATION, KEYWORDS_PRIORITY));
    }

    /**
     * Adds attachment points as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     */
    static void addAttachmentPointsAsLookups(@NotNull CompletionResultSet resultSet) {
        addKeywordAsLookup(resultSet, SERVICE);
        addKeywordAsLookup(resultSet, RESOURCE);
        addKeywordAsLookup(resultSet, CONNECTOR);
        addKeywordAsLookup(resultSet, ACTION);
        addKeywordAsLookup(resultSet, FUNCTION);
        addKeywordAsLookup(resultSet, TYPEMAPPER);
        addKeywordAsLookup(resultSet, STRUCT);
        addKeywordAsLookup(resultSet, CONST);
        addKeywordAsLookup(resultSet, PARAMETER);
        addKeywordAsLookup(resultSet, ANNOTATION);
    }

    /**
     * Adds a keyword as a lookup.
     *
     * @param resultSet     result list which is used to add lookups
     * @param lookupElement lookup element which needs to be added to the result list
     */
    static void addKeywordAsLookup(@NotNull CompletionResultSet resultSet, @NotNull LookupElement lookupElement) {
        resultSet.addElement(PrioritizedLookupElement.withPriority(lookupElement, KEYWORDS_PRIORITY));
    }

    /**
     * Adds common keywords like if, else as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     */
    static void addCommonKeywords(@NotNull CompletionResultSet resultSet) {
        addKeywordAsLookup(resultSet, IF);
        addKeywordAsLookup(resultSet, ELSE);
        addKeywordAsLookup(resultSet, FORK);
        addKeywordAsLookup(resultSet, JOIN);
        addKeywordAsLookup(resultSet, TIMEOUT);
        addKeywordAsLookup(resultSet, WORKER);
        addKeywordAsLookup(resultSet, TRANSFORM);
        addKeywordAsLookup(resultSet, TRANSACTION);
        addKeywordAsLookup(resultSet, ABORT);
        addKeywordAsLookup(resultSet, ABORTED);
        addKeywordAsLookup(resultSet, TRY);
        addKeywordAsLookup(resultSet, CATCH);
        addKeywordAsLookup(resultSet, FINALLY);
        addKeywordAsLookup(resultSet, ITERATE);
        addKeywordAsLookup(resultSet, WHILE);
        addKeywordAsLookup(resultSet, CONTINUE);
        addKeywordAsLookup(resultSet, BREAK);
        addKeywordAsLookup(resultSet, THROW);
    }

    static void addValueKeywords(@NotNull CompletionResultSet resultSet) {
        addKeywordAsLookup(resultSet, TRUE);
        addKeywordAsLookup(resultSet, FALSE);
        addKeywordAsLookup(resultSet, NULL);
    }

    /**
     * Adds keywords related to join conditions.
     *
     * @param resultSet result list which is used to add lookups
     */
    static void addJoinConditionKeywords(@NotNull CompletionResultSet resultSet) {
        addKeywordAsLookup(resultSet, ALL);
        addKeywordAsLookup(resultSet, SOME);
    }

    static void addCreateKeyword(@NotNull CompletionResultSet resultSet) {
        addKeywordAsLookup(resultSet, CREATE);
    }

    /**
     * Adds function specific keywords like <b>return</b> as lookup elements.
     *
     * @param parameters parameters which contain details of completion invocation
     * @param resultSet  result list which is used to add lookups
     */
    static void addFunctionSpecificKeywords(@NotNull CompletionParameters parameters,
                                            @NotNull CompletionResultSet resultSet) {
        PsiFile file = parameters.getOriginalFile();
        PsiElement element = file.findElementAt(parameters.getOffset());
        FunctionDefinitionNode functionNode = PsiTreeUtil.getParentOfType(element, FunctionDefinitionNode.class);
        if (functionNode != null) {
            addKeywordAsLookup(resultSet, RETURN);
        }
    }

    /**
     * Adds function specific keywords like <b>return</b> as lookup elements.
     *
     * @param parameters parameters which contain details of completion invocation
     * @param resultSet  result list which is used to add lookups
     */
    static void addActionSpecificKeywords(@NotNull CompletionParameters parameters,
                                          @NotNull CompletionResultSet resultSet) {
        PsiFile file = parameters.getOriginalFile();
        PsiElement element = file.findElementAt(parameters.getOffset());
        ActionDefinitionNode actionDefinitionNode = PsiTreeUtil.getParentOfType(element, ActionDefinitionNode.class);
        if (actionDefinitionNode != null) {
            addKeywordAsLookup(resultSet, RETURN);
        }
    }

    /**
     * Adds action specific keywords like <b>reply</b> as lookup elements.
     *
     * @param parameters parameters which contain details of completion invocation
     * @param resultSet  result list which is used to add lookups
     */
    static void addResourceSpecificKeywords(@NotNull CompletionParameters parameters,
                                            @NotNull CompletionResultSet resultSet) {
        PsiFile file = parameters.getOriginalFile();
        PsiElement element = file.findElementAt(parameters.getOffset());
        ResourceDefinitionNode resourceDefinitionNode = PsiTreeUtil.getParentOfType(element,
                ResourceDefinitionNode.class);
        if (resourceDefinitionNode != null) {
            addKeywordAsLookup(resultSet, REPLY);
        }
    }

    /**
     * Helper method to add packages as lookup elements which will be used in package declaration nodes and import
     * declaration nodes.
     *
     * @param resultSet     result list which is used to add lookups
     * @param directory     directory which needs to be added as a lookup element
     * @param insertHandler insert handler of the lookup element
     */
    static void addPackagesAsLookups(@NotNull CompletionResultSet resultSet, @NotNull PsiDirectory directory,
                                     InsertHandler<LookupElement> insertHandler) {
        LookupElementBuilder builder = LookupElementBuilder.create(directory.getName())
                .withTypeText("Package").withIcon(BallerinaIcons.PACKAGE).withInsertHandler(insertHandler);
        resultSet.addElement(builder);
    }

    /**
     * Suggests annotations from a package according to the current context .
     *
     * @param parameters     parameters which contain details of completion invocation
     * @param resultSet      result list which is used to add lookups
     * @param packageElement package name node
     * @param type           type of the annotation to be suggested
     */
    static void suggestAnnotationsFromPackage(@NotNull CompletionParameters parameters,
                                              @NotNull CompletionResultSet resultSet,
                                              @Nullable PsiElement packageElement, @NotNull String type) {
        PsiFile originalFile = parameters.getOriginalFile();
        if (packageElement != null) {
            // Get all imported packages in current file
            List<PsiElement> packages = BallerinaPsiImplUtil.getAllImportedPackagesInCurrentFile(originalFile);
            for (PsiElement pack : packages) {
                // Compare text to identify the correct package
                if (packageElement.getText().equals(pack.getText())) {
                    // Resolve the package and get all matching directories. But all imported packages should be
                    // unique. So the maximum size of this should be 1. If this is 0, that means the package is
                    // not imported. If this is more than 1, it means there are duplicate package imports or
                    // there are multiple packages with the same name.
                    PsiDirectory[] psiDirectories =
                            BallerinaPsiImplUtil.resolveDirectory(((PackageNameNode) pack).getNameIdentifier());
                    if (psiDirectories.length == 1) {
                        // Get all annotations in the package.
                        List<PsiElement> attachmentsForType =
                                BallerinaPsiImplUtil.getAllAnnotationAttachmentsForType(psiDirectories[0], type);
                        addAnnotationsAsLookups(resultSet, attachmentsForType);
                    }
                    // Else situation cannot/should not happen since all the imported packages are unique.
                    // This should be highlighted using an annotator.
                }
            }
        } else {
            // If the packageElement is null, that means we want to get all annotations in the current package.
            PsiDirectory containingDirectory = originalFile.getContainingDirectory();
            if (containingDirectory != null) {
                List<PsiElement> attachments =
                        BallerinaPsiImplUtil.getAllAnnotationAttachmentsForType(containingDirectory, type);
                addAnnotationsAsLookups(resultSet, attachments);
            }
        }
    }

    /**
     * Helper method to add annotations as lookup elements.
     *
     * @param resultSet   result list which is used to add lookups
     * @param annotations list of annotations which needs to be added
     */
    private static void addAnnotationsAsLookups(@NotNull CompletionResultSet resultSet, List<PsiElement> annotations) {
        for (PsiElement annotation : annotations) {
            LookupElementBuilder builder = LookupElementBuilder.create(annotation.getText())
                    .withTypeText("Annotation").withIcon(BallerinaIcons.ANNOTATION)
                    .withInsertHandler(BracesInsertHandler.INSTANCE);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, ANNOTATION_PRIORITY));
        }
    }

    /**
     * Helper method to add packages as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     * @param file      file which will be used to get imported packages
     */
    static void addAllImportedPackagesAsLookups(@NotNull CompletionResultSet resultSet, @NotNull PsiFile file) {
        Collection<ImportDeclarationNode> importDeclarationNodes = PsiTreeUtil.findChildrenOfType(file,
                ImportDeclarationNode.class);
        for (ImportDeclarationNode importDeclarationNode : importDeclarationNodes) {
            PsiElement packageNameNode;
            String packagePath;
            PackagePathNode packagePathNode = PsiTreeUtil.getChildOfType(importDeclarationNode, PackagePathNode.class);
            if (packagePathNode == null) {
                continue;
            }
            packagePath = packagePathNode.getText();

            // We need to get all package name nodes from the package path node.
            List<PackageNameNode> packageNameNodes = new ArrayList<>(
                    PsiTreeUtil.findChildrenOfType(packagePathNode, PackageNameNode.class)
            );
            if (packageNameNodes.isEmpty()) {
                continue;
            }
            packageNameNode = packageNameNodes.get(packageNameNodes.size() - 1);

            AliasNode aliasNode = PsiTreeUtil.findChildOfType(importDeclarationNode, AliasNode.class);
            if (aliasNode != null) {
                LookupElementBuilder builder = LookupElementBuilder.create(aliasNode.getText())
                        .withTailText("(" + packagePath + ")", true)
                        .withTypeText("Package").withIcon(BallerinaIcons.PACKAGE)
                        .withInsertHandler(PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP);
                resultSet.addElement(PrioritizedLookupElement.withPriority(builder, PACKAGE_PRIORITY));
            } else {
                LookupElementBuilder builder = LookupElementBuilder.create(packageNameNode.getText())
                        .withTailText("(" + packagePath + ")", true)
                        .withTypeText("Package").withIcon(BallerinaIcons.PACKAGE)
                        .withInsertHandler(PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP);
                resultSet.addElement(PrioritizedLookupElement.withPriority(builder, PACKAGE_PRIORITY));
            }
        }
    }

    static void addAllUnImportedPackagesAsLookups(@NotNull CompletionResultSet resultSet, @NotNull PsiFile file) {
        // Get all imported packages in the current file.
        Map<String, String> importsMap = BallerinaPsiImplUtil.getAllImportsInAFile(file);
        // Get all packages in the resolvable scopes (project and libraries).
        List<PsiDirectory> directories = BallerinaPsiImplUtil.getAllPackagesInResolvableScopes(file.getProject());
        // Iterate through all available  packages.
        for (PsiDirectory directory : directories) {
            // Set the default insert handler to auto popup insert handler.
            InsertHandler<LookupElement> insertHandler = BallerinaAutoImportInsertHandler.INSTANCE_WITH_AUTO_POPUP;
            // Suggest a package name for the directory.
            // Eg: ballerina/lang/system -> ballerina.lang.system
            String suggestedImportPath = BallerinaUtil.suggestPackageNameForDirectory(directory);
            // There are two possibilities.
            //
            // 1) The package is already imported.
            //
            //    import ballerina.lang.system;
            //    In this case, map will contain "system -> ballerina.lang.system". So we need to check whether the
            //    directory name (as an example "system") is a key in the map.
            //
            // 2) The package is already imported using an alias.
            //
            //    import ballerina.lang.system as builtin;
            //    In this case, map will contain "builtin -> ballerina.lang.system". So we need to check whether the
            //    suggested import path (as an example "ballerina.lang.system") is already in the map as a value.

            // Check whether the package is already in the imports.
            if (importsMap.containsKey(directory.getName())) {
                // Even though the package name is in imports, the name might be an alias.
                // Eg: import org.tools as system;
                //     In this case, map will contain "system -> org.tools". So when we type "sys", matching package
                //     will be "ballerina.lang.system". Directory name is "system" which matches the "system" in
                //     imports, but the paths are different.
                //
                // If the paths are same, we will not add it as a lookup since the {@code
                // addAllImportedPackagesAsLookups} will add it as a lookup.
                String packagePath = importsMap.get(directory.getName());
                if (packagePath.equals(suggestedImportPath) || importsMap.containsValue(suggestedImportPath)) {
                    // Condition importsMap.containsValue(suggestedImportPath) can happen when both package and
                    // alias is available.
                    // Eg: import ballerina.lang.system;
                    //     import org.system as mySystem;
                    // Now when we type "system", both above imports will be lookups if we don't continue here.
                    continue;
                }
                // If the paths does not match, that means the package is already imported as a alias, so we need to
                // get an alias for the package which we are trying to import.
                //
                // Eg: import org.tools as system;
                //     import ballerina.lang.system as builtin;
                insertHandler = BallerinaAutoImportInsertHandler.INSTANCE_WITH_ALIAS_WITH_POPUP;
            } else if (importsMap.containsValue(suggestedImportPath)) {
                // This means we have already imported this package. This will be suggested by {@code
                // addAllImportedPackagesAsLookups}. So no need to add it as a lookup element.
                continue;
            }
            // Add the package as a lookup.
            LookupElementBuilder builder = LookupElementBuilder.create(directory)
                    .withTailText("(" + suggestedImportPath + ")", true)
                    .withTypeText("Package").withIcon(BallerinaIcons.PACKAGE)
                    .withInsertHandler(insertHandler);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, UNIMPORTED_PACKAGE_PRIORITY));
        }
    }

    /**
     * Helper method to add functions as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     * @param file      file which is currently being edited
     */
    static void addFunctionsAsLookups(@NotNull CompletionResultSet resultSet, @NotNull PsiFile file) {
        List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(file.getContainingDirectory());
        addFunctionsAsLookups(resultSet, functions);
    }

    /**
     * Helper method to add functions as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     * @param functions list of functions which needs to be added
     */
    static void addFunctionsAsLookups(@NotNull CompletionResultSet resultSet, List<PsiElement> functions) {
        for (PsiElement function : functions) {
            LookupElementBuilder builder = LookupElementBuilder.create(function.getText())
                    .withTypeText("Function").withIcon(BallerinaIcons.FUNCTION).bold()
                    .withTailText(BallerinaDocumentationProvider.getParametersAndReturnTypes(function.getParent()))
                    .withInsertHandler(FunctionCompletionInsertHandler.INSTANCE);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, FUNCTION_PRIORITY));
        }
    }

    /**
     * Helper method to add connectors as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     * @param file      original file which we are editing
     */
    private static void addConnectorsAsLookups(@NotNull CompletionResultSet resultSet, @NotNull PsiFile file) {
        List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsInCurrentPackage(file);
        addConnectorsAsLookups(resultSet, connectors);
    }

    /**
     * Helper method to add connections as lookup elements.
     *
     * @param resultSet  result list which is used to add lookups
     * @param connectors list of connectors which needs to be added
     */
    static void addConnectorsAsLookups(@NotNull CompletionResultSet resultSet, List<PsiElement> connectors) {
        for (PsiElement connector : connectors) {
            LookupElementBuilder builder = LookupElementBuilder.create(connector.getText())
                    .withTypeText("Connector").withIcon(BallerinaIcons.CONNECTOR).bold()
                    .withTailText(BallerinaDocumentationProvider.getParameterString(connector.getParent(), true));
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, CONNECTOR_PRIORITY));
        }
    }

    /**
     * Helper method to add actions as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     * @param actions   list of actions which needs to be added
     */
    static void addActionsAsLookups(@NotNull CompletionResultSet resultSet, List<PsiElement> actions) {
        for (PsiElement action : actions) {
            LookupElementBuilder builder = LookupElementBuilder.create(action.getText())
                    .withTypeText("Action").withIcon(BallerinaIcons.ACTION).bold()
                    .withTailText(BallerinaDocumentationProvider.getParametersAndReturnTypes(action.getParent()))
                    .withInsertHandler(FunctionCompletionInsertHandler.INSTANCE);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, ACTION_PRIORITY));
        }
    }

    /**
     * Helper method to add structs as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     * @param file      file which will be used to get structs
     */
    static void addStructsAsLookups(@NotNull CompletionResultSet resultSet, @NotNull PsiFile file) {
        List<PsiElement> structs = BallerinaPsiImplUtil.getAllStructsInCurrentPackage(file);
        addStructsAsLookups(resultSet, structs);
    }

    /**
     * Helper method to add structs as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     * @param structs   list of structs which needs to be added
     */
    static void addStructsAsLookups(@NotNull CompletionResultSet resultSet, List<PsiElement> structs) {
        for (PsiElement struct : structs) {
            LookupElementBuilder builder = LookupElementBuilder.create(struct.getText())
                    .withTypeText("Struct").withIcon(BallerinaIcons.STRUCT);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, STRUCT_PRIORITY));
        }
    }

    /**
     * Adds variables in the resolvable scopes as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     * @param element   element in the current caret position
     */
    private static void addVariablesAsLookups(@NotNull CompletionResultSet resultSet, @NotNull PsiElement element) {
        PsiElement context = element.getContext();
        if (context == null) {
            context = element.getParent().getContext();
        }
        List<PsiElement> variables = BallerinaPsiImplUtil.getAllVariablesInResolvableScope(element, context);
        for (PsiElement variable : variables) {
            LookupElementBuilder builder = LookupElementBuilder.create(variable.getText())
                    .withTypeText("Variable").withIcon(BallerinaIcons.VARIABLE);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY));
        }
    }

    /**
     * Adds variables in the resolvable scopes as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     * @param element   element in the current caret position
     */
    private static void addParametersAsLookups(@NotNull CompletionResultSet resultSet, @NotNull PsiElement element) {
        PsiElement context = element.getContext();
        if (context == null) {
            context = element.getParent().getContext();
        }
        List<PsiElement> variables = BallerinaPsiImplUtil.getAllParametersInResolvableScope(element, context);
        for (PsiElement variable : variables) {
            LookupElementBuilder builder = LookupElementBuilder.create(variable.getText())
                    .withTypeText("Parameter").withIcon(BallerinaIcons.PARAMETER);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY));
        }
    }

    /**
     * Helper method to add constants as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     * @param file      original file which we are editing
     */
    private static void addConstantsAsLookups(@NotNull CompletionResultSet resultSet, @NotNull PsiFile file) {
        List<PsiElement> constants = BallerinaPsiImplUtil.getAllConstantsFromPackage(file.getParent());
        addConstantsAsLookups(resultSet, constants);
    }

    /**
     * Helper method to add constants as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     * @param constants list of constants which needs to be added
     */
    static void addConstantsAsLookups(@NotNull CompletionResultSet resultSet, List<PsiElement> constants) {
        for (PsiElement constant : constants) {
            LookupElementBuilder builder = LookupElementBuilder.create(constant.getText())
                    .withTypeText("Constant").withIcon(BallerinaIcons.CONSTANT);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY));
        }
    }

    /**
     * Helper method to add global variables as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     * @param file      original file which we are editing
     */
    private static void addGlobalVariablesAsLookups(@NotNull CompletionResultSet resultSet, @NotNull PsiFile file) {
        List<PsiElement> constants = BallerinaPsiImplUtil.getAllGlobalVariablesFromPackage(file.getParent());
        addGlobalVariablesAsLookups(resultSet, constants);
    }

    /**
     * Helper method to add global variables as lookup elements.
     *
     * @param resultSet result list which is used to add lookups
     * @param constants list of constants which needs to be added
     */
    static void addGlobalVariablesAsLookups(@NotNull CompletionResultSet resultSet, List<PsiElement> constants) {
        for (PsiElement constant : constants) {
            LookupElementBuilder builder = LookupElementBuilder.create(constant.getText())
                    .withTypeText("Variable").withIcon(BallerinaIcons.GLOBAL_VARIABLE);
            resultSet.addElement(PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY));
        }
    }

    /**
     * Adds variables, parameters, constants, global variables as lookups.
     *
     * @param resultSet result list which is used to add lookups
     * @param file      file which is currently being edited
     * @param element   element in the current context
     */
    static void addVariableTypesAsLookups(@NotNull CompletionResultSet resultSet, PsiFile file,
                                          PsiElement element) {
        addVariablesAsLookups(resultSet, element);
        addParametersAsLookups(resultSet, element);
        addConstantsAsLookups(resultSet, file);
        addGlobalVariablesAsLookups(resultSet, file);
        addValueKeywords(resultSet);
    }

    static void addLookups(@NotNull CompletionResultSet resultSet, @NotNull PsiFile file, boolean withPackages,
                           boolean withFunctions, boolean withConnectors, boolean withStructs) {
        if (withPackages) {
            addAllImportedPackagesAsLookups(resultSet, file);
            addAllUnImportedPackagesAsLookups(resultSet, file);
        }
        if (withFunctions) {
            addFunctionsAsLookups(resultSet, file);
        }
        if (withConnectors) {
            addConnectorsAsLookups(resultSet, file);
        }
        if (withStructs) {
            addStructsAsLookups(resultSet, file);
        }
    }

    /**
     * Adds a field as a lookup. Field can be either a struct field or annotation field.
     *
     * @param resultSet           result list which is used to add lookups
     * @param fieldNameIdentifier element which can be used to get the field text
     * @param definitionNode      struct/annotation definition node to get the tail text
     * @param typeName            type of the field
     * @param withInsertHandler   whether to add an insert handler
     * @param withAutoCompletion  whether to invoke auto complete popup
     */
    static void addFieldAsLookup(@NotNull CompletionResultSet resultSet, PsiElement fieldNameIdentifier,
                                 PsiElement definitionNode, TypeNameNode typeName, boolean withInsertHandler,
                                 boolean withAutoCompletion) {
        LookupElementBuilder builder = LookupElementBuilder.create(fieldNameIdentifier.getText())
                .withTypeText(typeName.getText()).withIcon(BallerinaIcons.FIELD)
                .withTailText(" -> " + definitionNode.getText(), true);
        if (withInsertHandler) {
            if (withAutoCompletion) {
                builder = builder.withInsertHandler(PackageCompletionInsertHandler.INSTANCE_WITH_AUTO_POPUP);
            } else {
                builder = builder.withInsertHandler(PackageCompletionInsertHandler.INSTANCE);
            }
        }
        resultSet.addElement(PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY));
    }

    static void addArrayLengthAsLookup(@NotNull CompletionResultSet resultSet) {
        LookupElementBuilder builder = LookupElementBuilder.create("length");
        resultSet.addElement(PrioritizedLookupElement.withPriority(builder, VARIABLE_PRIORITY));
    }

    /**
     * Identify the annotation attachment type of the given definition node.
     *
     * @param definitionNode a definition node which we want to check
     * @return annotation attachment type.
     */
    static String getAnnotationAttachmentType(PsiElement definitionNode) {
        String type = null;
        if (definitionNode instanceof ServiceDefinitionNode) {
            type = "service";
        } else if (definitionNode instanceof FunctionDefinitionNode) {
            type = "function";
        } else if (definitionNode instanceof ConnectorDefinitionNode) {
            type = "connector";
        } else if (definitionNode instanceof StructDefinitionNode) {
            type = "struct";
        } else if (definitionNode instanceof TypeMapperNode) {
            type = "typemapper";
        } else if (definitionNode instanceof ConstantDefinitionNode) {
            type = "const";
        } else if (definitionNode instanceof AnnotationDefinitionNode) {
            type = "annotation";
        } else if (definitionNode instanceof ResourceDefinitionNode) {
            type = "resource";
        } else if (definitionNode instanceof ActionDefinitionNode) {
            type = "action";
        }
        return type;
    }

    /**
     * Handles situations where we want to consider previous node content before adding lookup elements to the
     * current node.
     *
     * @param parameters    parameters which passed to completion contributor
     * @param resultSet     result list which is used to add lookups
     * @param offset        offset of  the current element
     * @param iNodeStrategy lookup element adding strategy if the previous element is an identifier node
     * @param lNodeStrategy lookup element adding strategy if the previous element is a leaf node
     * @param oNodeStrategy lookup element adding strategy if the previous element is not an identifier or leaf node
     */
    static void checkPrevNodeAndHandle(@NotNull CompletionParameters parameters,
                                       @NotNull CompletionResultSet resultSet, int offset,
                                       Strategy<CompletionParameters, CompletionResultSet, PsiElement> iNodeStrategy,
                                       Strategy<CompletionParameters, CompletionResultSet, PsiElement> lNodeStrategy,
                                       Strategy<CompletionParameters, CompletionResultSet, PsiElement> oNodeStrategy) {
        PsiFile originalFile = parameters.getOriginalFile();
        PsiElement prevElement = getPreviousNonEmptyElement(originalFile, offset);
        if (prevElement instanceof IdentifierPSINode && iNodeStrategy != null) {
            iNodeStrategy.execute(parameters, resultSet, prevElement);
        } else if (prevElement instanceof LeafPsiElement && lNodeStrategy != null) {
            lNodeStrategy.execute(parameters, resultSet, prevElement);
        } else {
            if (oNodeStrategy != null) {
                oNodeStrategy.execute(parameters, resultSet, prevElement);
            }
        }
    }

    /**
     * Returns the non empty element which is a previous sibling of the current node.
     *
     * @param originalFile file which contains the current element
     * @param offset       offset of the current node
     * @return {@code null} if there is no previous non empty node. Otherwise returns the corresponding
     * {@link PsiElement} node.
     */
    public static PsiElement getPreviousNonEmptyElement(PsiFile originalFile, int offset) {
        int count = 1;
        PsiElement prevElement = originalFile.findElementAt(offset - count++);
        while (prevElement instanceof PsiWhiteSpace && prevElement.getTextOffset() > 0) {
            prevElement = originalFile.findElementAt(offset - count++);
        }
        return prevElement;
    }

    /**
     * Identifies whether the given element type is an expression separator element type.
     * <p>
     * Eg: +, -, /, *, <, >, >=, <=, ==, !=, &&, ||, =
     *
     * @param elementType element type to be checked
     * @return {@code true} if the provided element type is an expression separator element type, {@code false}
     * otherwise.
     */
    static boolean isExpressionSeparator(IElementType elementType) {
        return isArithmeticOperator(elementType) || isRelationalOperator(elementType)
                || isLogicalOperator(elementType) || isAssignmentOperator(elementType);
    }

    /**
     * Identifies whether the given element type is an arithmetic operator type.
     *
     * @param elementType element type to be checked
     * @return {@code true} if the provided element type is an arithmetic operator type, {@code false} otherwise.
     */
    private static boolean isArithmeticOperator(IElementType elementType) {
        return isAdditiveOperator(elementType) || isMultiplicativeOperator(elementType);
    }

    /**
     * Identifies whether the given element type is an additive operator type.
     *
     * @param elementType element type to be checked
     * @return {@code true} if the provided element type is an additive operator type, {@code false} otherwise.
     */
    private static boolean isAdditiveOperator(IElementType elementType) {
        return elementType == BallerinaTypes.ADD || elementType == BallerinaTypes.SUB;
    }

    /**
     * Identifies whether the given element type is a multiplicative operator type.
     *
     * @param elementType element type to be checked
     * @return {@code true} if the provided element type is a multiplicative operator type, {@code false} otherwise.
     */
    private static boolean isMultiplicativeOperator(IElementType elementType) {
        return elementType == BallerinaTypes.MUL || elementType == BallerinaTypes.DIV ||
                elementType == BallerinaTypes.MOD;
    }

    /**
     * Identifies whether the given element type is a relational operator type.
     *
     * @param elementType element type to be checked
     * @return {@code true} if the provided element type is a relational operator type, {@code false} otherwise.
     */
    private static boolean isRelationalOperator(IElementType elementType) {
        return isComparisonOperator(elementType) || isEqualityOperator(elementType);
    }

    /**
     * Identifies whether the given element type is a comparison operator type.
     *
     * @param elementType element type to be checked
     * @return {@code true} if the provided element type is a comparison operator type, {@code false} otherwise.
     */
    private static boolean isComparisonOperator(IElementType elementType) {
        return elementType == BallerinaTypes.LE || elementType == BallerinaTypes.GE ||
                elementType == BallerinaTypes.GT || elementType == BallerinaTypes.LT;
    }

    /**
     * Identifies whether the given element type is an equality operator type.
     *
     * @param elementType element type to be checked
     * @return {@code true} if the provided element type is an equality operator type, {@code false} otherwise.
     */
    private static boolean isEqualityOperator(IElementType elementType) {
        return elementType == BallerinaTypes.EQUAL || elementType == BallerinaTypes.NOTEQUAL;
    }

    /**
     * Identifies whether the given element type is a logical operator type.
     *
     * @param elementType element type to be checked
     * @return {@code true} if the provided element type is a logical operator type, {@code false} otherwise.
     */
    private static boolean isLogicalOperator(IElementType elementType) {
        return elementType == BallerinaTypes.AND || elementType == BallerinaTypes.OR;
    }

    /**
     * Identifies whether the given element type is an assignment operator type.
     *
     * @param elementType element type to be checked
     * @return {@code true} if the provided element type is an assignment operator type, {@code false} otherwise.
     */
    private static boolean isAssignmentOperator(IElementType elementType) {
        return elementType == BallerinaTypes.ASSIGN;
    }
}

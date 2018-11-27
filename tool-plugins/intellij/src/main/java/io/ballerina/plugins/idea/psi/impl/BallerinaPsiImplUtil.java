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

package io.ballerina.plugins.idea.psi.impl;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveState;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.containers.MultiMap;
import io.ballerina.plugins.idea.completion.BallerinaCompletionUtils;
import io.ballerina.plugins.idea.completion.inserthandlers.AutoImportInsertHandler;
import io.ballerina.plugins.idea.psi.BallerinaAlias;
import io.ballerina.plugins.idea.psi.BallerinaAnnotationDefinition;
import io.ballerina.plugins.idea.psi.BallerinaAnyIdentifierName;
import io.ballerina.plugins.idea.psi.BallerinaArrayTypeName;
import io.ballerina.plugins.idea.psi.BallerinaAssignmentStatement;
import io.ballerina.plugins.idea.psi.BallerinaBuiltInReferenceTypeName;
import io.ballerina.plugins.idea.psi.BallerinaCallableUnitSignature;
import io.ballerina.plugins.idea.psi.BallerinaCatchClause;
import io.ballerina.plugins.idea.psi.BallerinaCompletePackageName;
import io.ballerina.plugins.idea.psi.BallerinaCompositeElement;
import io.ballerina.plugins.idea.psi.BallerinaExpression;
import io.ballerina.plugins.idea.psi.BallerinaField;
import io.ballerina.plugins.idea.psi.BallerinaFieldDefinition;
import io.ballerina.plugins.idea.psi.BallerinaFieldVariableReference;
import io.ballerina.plugins.idea.psi.BallerinaFile;
import io.ballerina.plugins.idea.psi.BallerinaForeachStatement;
import io.ballerina.plugins.idea.psi.BallerinaFormalParameterList;
import io.ballerina.plugins.idea.psi.BallerinaFunctionDefinition;
import io.ballerina.plugins.idea.psi.BallerinaFunctionInvocation;
import io.ballerina.plugins.idea.psi.BallerinaFunctionInvocationReference;
import io.ballerina.plugins.idea.psi.BallerinaFunctionNameReference;
import io.ballerina.plugins.idea.psi.BallerinaFutureTypeName;
import io.ballerina.plugins.idea.psi.BallerinaIdentifier;
import io.ballerina.plugins.idea.psi.BallerinaImportDeclaration;
import io.ballerina.plugins.idea.psi.BallerinaIndex;
import io.ballerina.plugins.idea.psi.BallerinaInvocation;
import io.ballerina.plugins.idea.psi.BallerinaInvocationReference;
import io.ballerina.plugins.idea.psi.BallerinaJsonTypeName;
import io.ballerina.plugins.idea.psi.BallerinaMapTypeName;
import io.ballerina.plugins.idea.psi.BallerinaNameReference;
import io.ballerina.plugins.idea.psi.BallerinaNamedPattern;
import io.ballerina.plugins.idea.psi.BallerinaNamespaceDeclaration;
import io.ballerina.plugins.idea.psi.BallerinaNullableTypeName;
import io.ballerina.plugins.idea.psi.BallerinaObjectFieldDefinition;
import io.ballerina.plugins.idea.psi.BallerinaObjectFunctionDefinition;
import io.ballerina.plugins.idea.psi.BallerinaOrgName;
import io.ballerina.plugins.idea.psi.BallerinaPackageName;
import io.ballerina.plugins.idea.psi.BallerinaPackageReference;
import io.ballerina.plugins.idea.psi.BallerinaPackageVersion;
import io.ballerina.plugins.idea.psi.BallerinaParameter;
import io.ballerina.plugins.idea.psi.BallerinaParameterWithType;
import io.ballerina.plugins.idea.psi.BallerinaRecordTypeName;
import io.ballerina.plugins.idea.psi.BallerinaReturnParameter;
import io.ballerina.plugins.idea.psi.BallerinaReturnType;
import io.ballerina.plugins.idea.psi.BallerinaServiceDefinition;
import io.ballerina.plugins.idea.psi.BallerinaSimpleTypeName;
import io.ballerina.plugins.idea.psi.BallerinaSimpleVariableReference;
import io.ballerina.plugins.idea.psi.BallerinaStreamTypeName;
import io.ballerina.plugins.idea.psi.BallerinaTableTypeName;
import io.ballerina.plugins.idea.psi.BallerinaTupleDestructuringStatement;
import io.ballerina.plugins.idea.psi.BallerinaTupleTypeName;
import io.ballerina.plugins.idea.psi.BallerinaTypeConversionExpression;
import io.ballerina.plugins.idea.psi.BallerinaTypeDefinition;
import io.ballerina.plugins.idea.psi.BallerinaTypeName;
import io.ballerina.plugins.idea.psi.BallerinaTypes;
import io.ballerina.plugins.idea.psi.BallerinaUnionTypeName;
import io.ballerina.plugins.idea.psi.BallerinaVariableDefinitionStatement;
import io.ballerina.plugins.idea.psi.BallerinaVariableDefinitionStatementWithAssignment;
import io.ballerina.plugins.idea.psi.BallerinaVariableDefinitionStatementWithoutAssignment;
import io.ballerina.plugins.idea.psi.BallerinaVariableReference;
import io.ballerina.plugins.idea.psi.BallerinaVariableReferenceExpression;
import io.ballerina.plugins.idea.psi.BallerinaVariableReferenceList;
import io.ballerina.plugins.idea.psi.BallerinaXmlTypeName;
import io.ballerina.plugins.idea.psi.reference.BallerinaCompletePackageNameReferenceSet;
import io.ballerina.plugins.idea.psi.reference.BallerinaNameReferenceReference;
import io.ballerina.plugins.idea.psi.reference.BallerinaPackageNameReference;
import io.ballerina.plugins.idea.sdk.BallerinaPathModificationTracker;
import io.ballerina.plugins.idea.sdk.BallerinaSdkService;
import io.ballerina.plugins.idea.sdk.BallerinaSdkUtil;
import io.ballerina.plugins.idea.stubs.BallerinaPackageVersionStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Util class which contains methods related to PSI manipulation.
 */
public class BallerinaPsiImplUtil {

    private static final List<String> BUILTIN_DIRECTORIES = new LinkedList<>();

    private static final List<String> BUILTIN_VARIABLE_TYPES = new LinkedList<>();

    static {
        BUILTIN_DIRECTORIES.add(File.separator + "builtin");

        BUILTIN_VARIABLE_TYPES.add("future"); //async
        BUILTIN_VARIABLE_TYPES.add("blob");
        BUILTIN_VARIABLE_TYPES.add("json");
        BUILTIN_VARIABLE_TYPES.add("map");
        BUILTIN_VARIABLE_TYPES.add("stream");
        BUILTIN_VARIABLE_TYPES.add("string");
        BUILTIN_VARIABLE_TYPES.add("table");
        BUILTIN_VARIABLE_TYPES.add("xml");
    }

    public static final String LOCAL_PACKAGE_PLACEHOLDER = "$LOCAL_PROJECT$";
    // Since instances of "com.intellij.openapi.project.Project" returns system-independant paths for project directory
    // File.seperator should not be used
    private static final String FILE_SEPARATOR = "/";

    @Nullable
    public static String getName(@NotNull BallerinaPackageName ballerinaPackageName) {
        PsiElement identifier = ballerinaPackageName.getIdentifier();
        return identifier.getText();
    }

    @Nullable
    public static String getName(@NotNull BallerinaOrgName ballerinaOrgName) {
        PsiElement identifier = ballerinaOrgName.getIdentifier();
        return identifier.getText();
    }

    @Nullable
    public static String getName(@NotNull BallerinaAlias ballerinaAlias) {
        PsiElement identifier = ballerinaAlias.getIdentifier();
        return identifier != null ? identifier.getText() : null;
    }

    @Nullable
    public static String getName(@NotNull BallerinaPackageVersion ballerinaPackageVersion) {
        BallerinaPackageVersionStub stub = ballerinaPackageVersion.getStub();
        if (stub != null) {
            return stub.getName();
        }
        PsiElement identifier = ballerinaPackageVersion.getIdentifier();
        return identifier != null ? identifier.getText() : null;
    }

    @Nullable
    public static PsiElement getIdentifier(BallerinaFunctionDefinition ballerinaFunctionDefinition) {
        BallerinaCallableUnitSignature callableUnitSignature = ballerinaFunctionDefinition.getCallableUnitSignature();
        return callableUnitSignature != null ? callableUnitSignature.getAnyIdentifierName().getIdentifier() : null;
    }

    @Nullable
    public static String getName(BallerinaFunctionDefinition ballerinaFunctionDefinition) {
        BallerinaCallableUnitSignature callableUnitSignature = ballerinaFunctionDefinition.getCallableUnitSignature();
        return callableUnitSignature != null && callableUnitSignature.getAnyIdentifierName().getIdentifier() != null ?
                callableUnitSignature.getAnyIdentifierName().getIdentifier().getText() : "";
    }

    @Nullable
    public static BallerinaNameReference getQualifier(@NotNull BallerinaNameReference ballerinaNameReference) {
        return null;
    }

    // todo: ?
    public static PsiElement resolve(@NotNull BallerinaNameReference ballerinaNameReference) {
        return ballerinaNameReference.getReference().resolve();
    }

    @Nullable
    public static BallerinaTypeName resolveType(@NotNull BallerinaNameReference ballerinaNameReference) {
        return null;
    }

    public static boolean isInLocalPackage(@NotNull BallerinaNameReference ballerinaNameReference) {
        return ballerinaNameReference.getPackageReference() == null;
    }

    public static boolean isInLocalPackage(@NotNull BallerinaFunctionNameReference nameReference) {
        return nameReference.getPackageReference() == null;
    }

    public static boolean hasBuiltInDefinitions(@NotNull PsiElement type) {
        return BUILTIN_VARIABLE_TYPES.contains(type.getText());
    }

    @NotNull
    public static synchronized List<BallerinaFunctionDefinition> suggestBuiltInFunctions(@NotNull PsiElement type) {
        if (!hasBuiltInDefinitions(type)) {
            return new LinkedList<>();
        }
        String key = type.getText();
        return CachedValuesManager.getCachedValue(type,
                () -> CachedValueProvider.Result.create(getBuiltInFunctions(key, type),
                        ProjectRootManager.getInstance(type.getProject())));
    }

    @NotNull
    private static List<BallerinaFunctionDefinition> getBuiltInFunctions(@NotNull String key,
                                                                         @NotNull PsiElement type) {
        // Add elements from built-in packages
        for (String builtInDirectory : BUILTIN_DIRECTORIES) {
            VirtualFile file = BallerinaPsiImplUtil.findFileInSDK(type, builtInDirectory);
            if (file == null) {
                return new LinkedList<>();
            }
            VirtualFile[] builtInFiles = file.getChildren();
            for (VirtualFile builtInFile : builtInFiles) {
                if (builtInFile.isDirectory() || !"bal".equals(builtInFile.getExtension())
                        || !builtInFile.getName().equals(key + "lib.bal")) {
                    continue;
                }
                // Find the file.
                Project project = type.getProject();
                PsiFile psiFile = PsiManager.getInstance(project).findFile(builtInFile);
                if (psiFile == null) {
                    return new LinkedList<>();
                }
                List<BallerinaFunctionDefinition> functionDefinitions =
                        new ArrayList<>(PsiTreeUtil.findChildrenOfType(psiFile, BallerinaFunctionDefinition.class));
                if (!functionDefinitions.isEmpty()) {
                    return functionDefinitions;
                }
            }
        }
        return new LinkedList<>();
    }

    @NotNull
    public static synchronized List<BallerinaAnnotationDefinition> suggestBuiltInAnnotations(@NotNull PsiElement
                                                                                                     element) {
        return CachedValuesManager.getCachedValue(element,
                () -> CachedValueProvider.Result.create(getBuiltInAnnotations(element),
                        ProjectRootManager.getInstance(element.getProject())));
    }

    @NotNull
    private static List<BallerinaAnnotationDefinition> getBuiltInAnnotations(@NotNull PsiElement element) {
        LinkedList<BallerinaAnnotationDefinition> results = new LinkedList<>();
        // Add elements from built-in packages
        for (String builtInDirectory : BUILTIN_DIRECTORIES) {
            VirtualFile file = BallerinaPsiImplUtil.findFileInSDK(element, builtInDirectory);
            if (file == null) {
                return new LinkedList<>();
            }
            VirtualFile[] builtInFiles = file.getChildren();
            for (VirtualFile builtInFile : builtInFiles) {
                if (builtInFile.isDirectory() || !"bal".equals(builtInFile.getExtension())) {
                    continue;
                }
                // Find the file.
                Project project = element.getProject();
                PsiFile psiFile = PsiManager.getInstance(project).findFile(builtInFile);
                if (psiFile == null) {
                    return new LinkedList<>();
                }
                Collection<BallerinaAnnotationDefinition> annotationDefinitions =
                        PsiTreeUtil.findChildrenOfType(psiFile, BallerinaAnnotationDefinition.class);
                if (!annotationDefinitions.isEmpty()) {
                    results.addAll(annotationDefinitions);
                }
            }
        }
        return results;
    }

    @NotNull
    public static synchronized List<BallerinaTypeDefinition> suggestBuiltInTypes(@NotNull PsiElement element) {
        return CachedValuesManager.getCachedValue(element,
                () -> CachedValueProvider.Result.create(getBuiltInTypes(element),
                        ProjectRootManager.getInstance(element.getProject())));
    }

    @NotNull
    private static List<BallerinaTypeDefinition> getBuiltInTypes(@NotNull PsiElement element) {
        List<BallerinaTypeDefinition> results = new LinkedList<>();
        // Add elements from built-in packages
        for (String builtInDirectory : BUILTIN_DIRECTORIES) {

            VirtualFile file = BallerinaPsiImplUtil.findFileInSDK(element, builtInDirectory);
            if (file == null) {
                return new LinkedList<>();
            }
            VirtualFile[] builtInFiles = file.getChildren();
            for (VirtualFile builtInFile : builtInFiles) {
                if (builtInFile.isDirectory() || !"bal".equals(builtInFile.getExtension())) {
                    continue;
                }
                // Find the file.
                Project project = element.getProject();
                PsiFile psiFile = PsiManager.getInstance(project).findFile(builtInFile);
                if (psiFile == null) {
                    return new LinkedList<>();
                }
                Collection<BallerinaTypeDefinition> typeDefinitions =
                        PsiTreeUtil.findChildrenOfType(psiFile, BallerinaTypeDefinition.class);
                for (BallerinaTypeDefinition typeDefinition : typeDefinitions) {
                    if (typeDefinition.isPublic()) {
                        results.add(typeDefinition);
                    }
                }
            }
        }
        return results;
    }

    @Nullable
    public static PsiElement getCallableUnitSignature(@NotNull BallerinaFunctionInvocation functionInvocation) {
        return CachedValuesManager.getCachedValue(functionInvocation, () -> {
            BallerinaFunctionNameReference functionNameReference = functionInvocation.getFunctionNameReference();
            PsiElement identifier = functionNameReference.getAnyIdentifierName().getIdentifier();
            if (identifier == null) {
                return CachedValueProvider.Result.create(null, functionInvocation);
            }
            PsiReference reference = identifier.getReference();
            if (reference == null) {
                return CachedValueProvider.Result.create(null, functionInvocation);
            }
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement == null) {
                return CachedValueProvider.Result.create(null, functionInvocation);
            }
            BallerinaCallableUnitSignature callableUnitSignature = PsiTreeUtil.getParentOfType(resolvedElement,
                    BallerinaCallableUnitSignature.class);
            if (callableUnitSignature != null) {
                return CachedValueProvider.Result.create(callableUnitSignature, functionInvocation);
            }
            return CachedValueProvider.Result.create(null, functionInvocation);
        });
    }

    @Nullable
    public static PsiElement getCallableUnitSignature(@NotNull BallerinaInvocation ballerinaInvocation) {
        return CachedValuesManager.getCachedValue(ballerinaInvocation, () -> {
            PsiElement identifier = ballerinaInvocation.getAnyIdentifierName().getIdentifier();
            if (identifier == null) {
                return CachedValueProvider.Result.create(null, ballerinaInvocation);
            }
            PsiReference reference = identifier.getReference();
            if (reference == null) {
                return CachedValueProvider.Result.create(null, ballerinaInvocation);
            }
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement == null) {
                return CachedValueProvider.Result.create(null, ballerinaInvocation);
            }
            BallerinaCallableUnitSignature callableUnitSignature = PsiTreeUtil.getParentOfType(resolvedElement,
                    BallerinaCallableUnitSignature.class);
            if (callableUnitSignature != null) {
                return CachedValueProvider.Result.create(callableUnitSignature, ballerinaInvocation);
            }
            return CachedValueProvider.Result.create(null, ballerinaInvocation);
        });
    }

    /**
     * Used to retrieve the type from a {@link BallerinaVariableDefinitionStatement}.
     *
     * @param ballerinaVariableDefinitionStatement a {@link BallerinaVariableDefinitionStatement} object.
     * @return Type of the definition.
     */
    @Nullable
    public static PsiElement getType(BallerinaVariableDefinitionStatement ballerinaVariableDefinitionStatement) {
        return CachedValuesManager.getCachedValue(ballerinaVariableDefinitionStatement, () -> {
            PsiElement result = null;
            BallerinaVariableDefinitionStatementWithAssignment withAssignment = ballerinaVariableDefinitionStatement
                    .getVariableDefinitionStatementWithAssignment();
            BallerinaVariableDefinitionStatementWithoutAssignment withoutAssignment =
                    ballerinaVariableDefinitionStatement.getVariableDefinitionStatementWithoutAssignment();

            PsiElement type = null;
            if (withAssignment != null) {
                type = withAssignment.getTypeName();
            } else if (withoutAssignment != null) {
                type = withoutAssignment.getTypeName();
            }
            if (type == null) {
                return CachedValueProvider.Result.create(null, ballerinaVariableDefinitionStatement);
            }
            PsiReference reference = type.findReferenceAt(type.getTextLength());
            if (reference != null) {
                PsiElement resolvedElement = reference.resolve();
                if (resolvedElement != null) {
                    result = resolvedElement;
                } else if (type instanceof BallerinaSimpleTypeName) {
                    result = type;
                }
            } else {
                result = type;
            }
            return CachedValueProvider.Result.create(result, ballerinaVariableDefinitionStatement);
        });
    }

    @Nullable
    public static PsiElement getType(@NotNull BallerinaAssignmentStatement ballerinaAssignmentStatement) {
        return CachedValuesManager.getCachedValue(ballerinaAssignmentStatement, () -> {
            PsiElement result = null;
            BallerinaVariableReference variableReference = ballerinaAssignmentStatement.getVariableReference();
            if (variableReference != null) {
                PsiElement type = variableReference.getType();
                if (type != null) {
                    PsiReference reference = type.findReferenceAt(type.getTextLength());
                    if (reference != null) {
                        result = reference.resolve();
                    } else {
                        result = type;
                    }
                }
            }
            return CachedValueProvider.Result.create(result, ballerinaAssignmentStatement);
        });
    }

    @Nullable
    public static PsiElement getCachedType(@NotNull BallerinaNameReference ballerinaNameReference) {
        return CachedValuesManager.getCachedValue(ballerinaNameReference,
                () -> CachedValueProvider.Result.create(getType(ballerinaNameReference),
                        ProjectRootManager.getInstance(ballerinaNameReference.getProject())));
    }

    @Nullable
    private static PsiElement getType(@NotNull BallerinaNameReference ballerinaNameReference) {
        return CachedValuesManager.getCachedValue(ballerinaNameReference, () -> {
            PsiElement result = null;
            PsiElement identifier = ballerinaNameReference.getIdentifier();
            PsiReference reference = identifier.getReference();
            if (reference != null) {
                PsiElement resolvedElement = reference.resolve();
                if (resolvedElement != null) {
                    PsiElement parent = resolvedElement.getParent();
                    if (parent instanceof BallerinaTypeDefinition) {
                        result = parent;
                    }
                }
            }
            return CachedValueProvider.Result.create(result, ballerinaNameReference);
        });
    }

    @Nullable
    public static BallerinaFormalParameterList getParameterFromObjectFunction(@NotNull BallerinaTypeDefinition
                                                                                      ballerinaTypeDefinition,
                                                                              @NotNull String name) {
        return CachedValuesManager.getCachedValue(ballerinaTypeDefinition, () -> {
            BallerinaFormalParameterList result = null;
            Collection<BallerinaObjectFunctionDefinition> ballerinaObjectFunctionDefinitions =
                    PsiTreeUtil.findChildrenOfType(ballerinaTypeDefinition, BallerinaObjectFunctionDefinition.class);
            for (BallerinaObjectFunctionDefinition ballerinaObjectFunctionDefinition :
                    ballerinaObjectFunctionDefinitions) {
                BallerinaCallableUnitSignature objectCallableUnitSignature =
                        ballerinaObjectFunctionDefinition.getCallableUnitSignature();
                if (objectCallableUnitSignature == null) {
                    continue;
                }
                BallerinaAnyIdentifierName anyIdentifierName = objectCallableUnitSignature.getAnyIdentifierName();
                if (name.equals(anyIdentifierName.getText())) {
                    result = objectCallableUnitSignature.getFormalParameterList();
                }
            }
            return CachedValueProvider.Result.create(result, ballerinaTypeDefinition);
        });
    }

    @Nullable
    public static BallerinaTypeDefinition getReturnTypeFromObjectFunction(@NotNull BallerinaTypeDefinition
                                                                                  ballerinaTypeDefinition,
                                                                          @NotNull String name) {
        return CachedValuesManager.getCachedValue(ballerinaTypeDefinition, () -> {
            BallerinaTypeDefinition result = null;
            Collection<BallerinaObjectFunctionDefinition> ballerinaObjectFunctionDefinitions =
                    PsiTreeUtil.findChildrenOfType(ballerinaTypeDefinition, BallerinaObjectFunctionDefinition.class);
            for (BallerinaObjectFunctionDefinition functionDefinition : ballerinaObjectFunctionDefinitions) {
                BallerinaCallableUnitSignature objectCallableUnitSignature =
                        functionDefinition.getCallableUnitSignature();
                if (objectCallableUnitSignature == null) {
                    continue;
                }
                BallerinaAnyIdentifierName anyIdentifierName = objectCallableUnitSignature.getAnyIdentifierName();
                if (name.equals(anyIdentifierName.getText())) {
                    result = getClientFromReturnType(objectCallableUnitSignature);
                }
            }
            return CachedValueProvider.Result.create(result, ballerinaTypeDefinition);
        });
    }

    @Nullable
    public static BallerinaTypeDefinition getClientFromReturnType(@NotNull BallerinaCallableUnitSignature signature) {
        return CachedValuesManager.getCachedValue(signature, () -> {
            BallerinaReturnParameter returnParameter = signature.getReturnParameter();
            if (returnParameter == null) {
                return CachedValueProvider.Result.create(null, signature);
            }
            BallerinaReturnType returnType = returnParameter.getReturnType();
            if (returnType == null) {
                return CachedValueProvider.Result.create(null, signature);
            }
            BallerinaTypeName typeName = returnType.getTypeName();
            if (typeName instanceof BallerinaTupleTypeName) {
                List<BallerinaTypeName> typeNameList = ((BallerinaTupleTypeName) typeName).getTypeNameList();
                if (typeNameList.size() != 1) {
                    return CachedValueProvider.Result.create(null, signature);
                }
                BallerinaTypeName ballerinaTypeName = typeNameList.get(0);
                if (!(ballerinaTypeName instanceof BallerinaSimpleTypeName)) {
                    return CachedValueProvider.Result.create(null, signature);
                }
                PsiElement typeFromTypeName = getTypeFromTypeName(ballerinaTypeName);
                if (typeFromTypeName == null) {
                    return CachedValueProvider.Result.create(null, signature);
                }
                if ((!(typeFromTypeName.getParent() instanceof BallerinaTypeDefinition))) {
                    return CachedValueProvider.Result.create(null, signature);
                }
                BallerinaTypeDefinition result = ((BallerinaTypeDefinition) typeFromTypeName.getParent());
                return CachedValueProvider.Result.create(result, signature);
            } else if (typeName instanceof BallerinaSimpleTypeName) {
                PsiElement typeFromTypeName = getTypeFromTypeName(typeName);
                if (typeFromTypeName != null) {
                    if ((typeFromTypeName.getParent() instanceof BallerinaTypeDefinition)) {
                        BallerinaTypeDefinition result = ((BallerinaTypeDefinition) typeFromTypeName.getParent());
                        return CachedValueProvider.Result.create(result, signature);
                    }
                    PsiReference reference = typeFromTypeName.findReferenceAt(typeFromTypeName.getTextLength());
                    if (reference == null) {
                        return CachedValueProvider.Result.create(null, signature);
                    }
                    PsiElement resolvedElement = reference.resolve();
                    if (resolvedElement == null) {
                        return CachedValueProvider.Result.create(null, signature);
                    }
                    if (resolvedElement.getParent() instanceof BallerinaTypeDefinition) {
                        BallerinaTypeDefinition result = (BallerinaTypeDefinition) resolvedElement;
                        return CachedValueProvider.Result.create(result, signature);
                    }
                }
            }
            return CachedValueProvider.Result.create(null, signature);
        });
    }

    @Nullable
    public static PsiElement getType(@NotNull BallerinaTupleDestructuringStatement ballerinaTupleDestructuringStatement,
                                     @NotNull PsiElement identifier) {
        return CachedValuesManager.getCachedValue(ballerinaTupleDestructuringStatement, () -> {
            PsiElement result = null;
            int index = getVariableIndex(identifier);
            if (index != -1) {
                BallerinaExpression expression = ballerinaTupleDestructuringStatement.getExpression();
                if ((expression instanceof BallerinaVariableReferenceExpression)) {
                    BallerinaVariableReference variableReference = ((BallerinaVariableReferenceExpression) expression)
                            .getVariableReference();
                    if (variableReference instanceof BallerinaFunctionInvocationReference) {

                        BallerinaFunctionDefinition definition =
                                getFunctionDefinition(((BallerinaFunctionInvocationReference) variableReference));
                        if (definition != null) {
                            BallerinaTypeName returnType = getReturnTypeFromFunction(definition);
                            if (returnType instanceof BallerinaTupleTypeName) {
                                List<BallerinaTypeName> typeNameList = ((BallerinaTupleTypeName) returnType)
                                        .getTypeNameList();
                                if (typeNameList.size() > index) {
                                    result = typeNameList.get(index);
                                }
                            }
                        }
                    } else if (variableReference instanceof BallerinaSimpleVariableReference) {
                        result = getBallerinaTypeFromVariableReference(variableReference, index);
                    }
                } else if (expression instanceof BallerinaTypeConversionExpression) {
                    result = ((BallerinaTypeConversionExpression) expression).getTypeName();
                }
            }
            return CachedValueProvider.Result.create(result, ballerinaTupleDestructuringStatement);
        });
    }

    private static int getVariableIndex(@NotNull PsiElement identifier) {
        return CachedValuesManager.getCachedValue(identifier, () -> {
            int index = -1;
            BallerinaVariableReferenceList ballerinaVariableReferenceList = PsiTreeUtil.getParentOfType(identifier,
                    BallerinaVariableReferenceList.class);
            if (ballerinaVariableReferenceList != null) {
                List<BallerinaVariableReference> variableReferenceList =
                        ballerinaVariableReferenceList.getVariableReferenceList();
                int count = 0;
                for (BallerinaVariableReference ballerinaVariableReference : variableReferenceList) {
                    if (!(ballerinaVariableReference instanceof BallerinaSimpleVariableReference)) {
                        count++;
                        continue;
                    }
                    BallerinaSimpleVariableReference variableReference =
                            (BallerinaSimpleVariableReference) ballerinaVariableReference;
                    BallerinaNameReference nameReference = variableReference.getNameReference();
                    PsiElement element = nameReference.getIdentifier();
                    if (element.equals(identifier)) {
                        index = count;
                        break;
                    }
                    count++;
                }
            }
            return CachedValueProvider.Result.create(index, identifier);
        });
    }

    /**
     * Used to retrieve the type from a {@link BallerinaVariableReference}.
     *
     * @param ballerinaVariableReference a {@link BallerinaVariableReference} object.
     * @return Type of the definition.
     */
    @Nullable
    public static PsiElement getType(@NotNull BallerinaVariableReference ballerinaVariableReference) {
        return CachedValuesManager.getCachedValue(ballerinaVariableReference,
                () -> CachedValueProvider.Result.create(getBallerinaTypeFromVariableReference
                                (ballerinaVariableReference),
                        ProjectRootManager.getInstance(ballerinaVariableReference.getProject())));
    }

    @Nullable
    public static PsiElement getBallerinaTypeFromExpression(@NotNull BallerinaExpression expression) {
        return CachedValuesManager.getCachedValue(expression, () -> {
            PsiElement result = null;
            // Todo - add caching
            if (expression instanceof BallerinaVariableReferenceExpression) {
                BallerinaVariableReference variableReference = ((BallerinaVariableReferenceExpression)
                        expression).getVariableReference();

                result = BallerinaPsiImplUtil.getBallerinaTypeFromVariableReference(variableReference);

            }
            return CachedValueProvider.Result.create(result, expression);
        });
    }

    @Nullable
    private static PsiElement getBallerinaTypeFromVariableReference(@NotNull BallerinaVariableReference
                                                                            variableReference, int index) {
        return CachedValuesManager.getCachedValue(variableReference, () -> {
            if (variableReference instanceof BallerinaSimpleVariableReference) {
                if (isVariableReferenceInForEach(variableReference)) {
                    BallerinaForeachStatement foreachStatement = PsiTreeUtil.getParentOfType(variableReference,
                            BallerinaForeachStatement.class);
                    if (foreachStatement != null) {
                        BallerinaVariableReferenceList variableReferenceList =
                                foreachStatement.getVariableReferenceList();
                        if (variableReferenceList != null) {
                            List<BallerinaVariableReference> referenceList =
                                    variableReferenceList.getVariableReferenceList();
                            if (referenceList.size() == 1) {
                                return CachedValueProvider.Result.create(getType(foreachStatement), variableReference);
                            } else if (referenceList.size() == 2) {
                                // This is used to get the type for the lookup element.
                                if (referenceList.get(1).getText().equals(variableReference.getText())) {
                                    return CachedValueProvider.Result.create(getType(foreachStatement),
                                            variableReference);
                                }
                            }
                        }
                    }
                }

                BallerinaNameReference ballerinaNameReference =
                        ((BallerinaSimpleVariableReference) variableReference).getNameReference();

                PsiElement identifier = ballerinaNameReference.getIdentifier();
                PsiReference reference = identifier.getReference();
                if (reference == null) {
                    return CachedValueProvider.Result.create(null, variableReference);
                }
                PsiElement resolvedElement = reference.resolve();
                if (resolvedElement == null) {
                    return CachedValueProvider.Result.create(null, variableReference);
                }
                // Todo - Move to util
                PsiElement parent = resolvedElement.getParent();
                if (parent instanceof BallerinaVariableDefinitionStatement) {
                    return CachedValueProvider.Result.create(resolveBallerinaType((
                            (BallerinaVariableDefinitionStatement) parent)), variableReference);
                } else if (parent instanceof BallerinaFieldDefinition) {
                    return CachedValueProvider.Result.create(getTypeNameFromField(((BallerinaFieldDefinition) parent)),
                            variableReference);
                } else if (parent instanceof BallerinaParameterWithType) {
                    return CachedValueProvider.Result.create(
                            getTypeNameFromParameter(((BallerinaParameterWithType) parent)), variableReference);
                } else if (parent instanceof BallerinaCatchClause) {
                    return CachedValueProvider.Result.create(
                            ((BallerinaCatchClause) parent).getTypeName(), variableReference);
                } else if (parent instanceof BallerinaNamedPattern) {
                    BallerinaNamedPattern ballerinaNamedPattern = (BallerinaNamedPattern) parent;
                    BallerinaTypeName typeName = ballerinaNamedPattern.getTypeName();
                    if (typeName instanceof BallerinaArrayTypeName && !(variableReference.getNextSibling()
                            instanceof BallerinaIndex)) {
                        return CachedValueProvider.Result.create(null, variableReference);
                    }
                    return CachedValueProvider.Result.create(getTypeNameFromNamedPattern(typeName), variableReference);
                } else if (parent instanceof BallerinaNameReference) {
                    PsiElement superParent = parent.getParent();
                    if (superParent instanceof BallerinaVariableReference) {
                        BallerinaVariableReference ballerinaVariableReference =
                                (BallerinaVariableReference) superParent;
                        if (isVariableReferenceInForEach(ballerinaVariableReference)) {
                            BallerinaForeachStatement foreachStatement =
                                    PsiTreeUtil.getParentOfType(ballerinaVariableReference,
                                            BallerinaForeachStatement.class);
                            if (foreachStatement != null) {
                                BallerinaVariableReferenceList variableReferenceList =
                                        foreachStatement.getVariableReferenceList();

                                if (variableReferenceList != null) {
                                    List<BallerinaVariableReference> referenceList = variableReferenceList
                                            .getVariableReferenceList();
                                    // If there is only one element in the list, we resolve the type.
                                    if (referenceList.size() == 1) {
                                        return CachedValueProvider.Result.create(getType(foreachStatement),
                                                variableReference);
                                    } else if (referenceList.size() == 2) {
                                        // If there are two items, the first one will be the index. So we only resolve
                                        // the second element.
                                        if (variableReference.getText().equals(referenceList.get(1).getText())) {
                                            return CachedValueProvider.Result.create(getType(foreachStatement),
                                                    variableReference);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    BallerinaAssignmentStatement ballerinaAssignmentStatement = PsiTreeUtil.getParentOfType(parent,
                            BallerinaAssignmentStatement.class);
                    if (ballerinaAssignmentStatement != null) {
                        return CachedValueProvider.Result.create(getType(ballerinaAssignmentStatement),
                                variableReference);
                    }
                    BallerinaTupleDestructuringStatement tupleDestructuringStatement =
                            PsiTreeUtil.getParentOfType(parent, BallerinaTupleDestructuringStatement.class);
                    if (tupleDestructuringStatement != null) {
                        PsiElement type = getType(tupleDestructuringStatement, resolvedElement);
                        int newIndex = getVariableIndex(resolvedElement);
                        if (!(type instanceof BallerinaTupleTypeName) || newIndex == -1) {
                            return CachedValueProvider.Result.create(type, variableReference);
                        }
                        List<BallerinaTypeName> typeNameList = ((BallerinaTupleTypeName) type).getTypeNameList();
                        if (typeNameList.size() <= newIndex) {
                            return CachedValueProvider.Result.create(null, variableReference);
                        }
                        return CachedValueProvider.Result.create(typeNameList.get(newIndex), variableReference);
                    }
                }
            } else if (variableReference instanceof BallerinaFieldVariableReference) {
                BallerinaField field = ((BallerinaFieldVariableReference) variableReference).getField();
                PsiElement identifier = field.getIdentifier();
                if (identifier == null) {
                    return CachedValueProvider.Result.create(null, variableReference);
                }
                PsiReference reference = identifier.getReference();
                if (reference == null) {
                    return CachedValueProvider.Result.create(null, variableReference);
                }
                PsiElement resolvedElement = reference.resolve();
                if (resolvedElement == null) {
                    return CachedValueProvider.Result.create(null, variableReference);
                }
                PsiElement parent = resolvedElement.getParent();
                if (parent instanceof BallerinaFieldDefinition) {
                    return CachedValueProvider.Result.create(getTypeNameFromField(((BallerinaFieldDefinition) parent)),
                            variableReference);
                }
            } else if (variableReference instanceof BallerinaFunctionInvocationReference) {
                BallerinaFunctionInvocationReference functionInvocationReference =
                        (BallerinaFunctionInvocationReference) variableReference;
                BallerinaFunctionDefinition functionDefinition = getFunctionDefinition(functionInvocationReference);
                if (functionDefinition == null) {
                    return CachedValueProvider.Result.create(null, variableReference);
                }
                BallerinaTypeName typeName = getReturnTypeFromFunction(functionDefinition);
                if (typeName instanceof BallerinaUnionTypeName) {
                    return CachedValueProvider.Result.create(typeName, variableReference);
                } else if (typeName instanceof BallerinaTupleTypeName) {
                    List<BallerinaTypeName> typeNameList = ((BallerinaTupleTypeName) typeName).getTypeNameList();
                    if (typeNameList.size() == 1) {
                        return CachedValueProvider.Result.create(typeNameList.get(0), variableReference);
                    }
                    return CachedValueProvider.Result.create(typeName, variableReference);
                } else if (typeName instanceof BallerinaSimpleTypeName) {
                    PsiReference reference = typeName.findReferenceAt(typeName.getTextLength());
                    if (reference == null) {
                        return CachedValueProvider.Result.create(typeName, variableReference);
                    }
                    PsiElement resolvedElement = reference.resolve();
                    if (resolvedElement == null) {
                        return CachedValueProvider.Result.create(typeName, variableReference);
                    }
                    return CachedValueProvider.Result.create(resolvedElement, variableReference);
                }
            } else if (variableReference instanceof BallerinaInvocationReference) {
                return CachedValueProvider.Result.create(getType((BallerinaInvocationReference) variableReference),
                        variableReference);
            }
            return CachedValueProvider.Result.create(null, variableReference);
        });
    }

    @Nullable
    private static PsiElement getType(@NotNull BallerinaInvocationReference variableReference) {
        return CachedValuesManager.getCachedValue(variableReference, () -> {
            BallerinaAnyIdentifierName anyIdentifierName = variableReference
                    .getInvocation().getAnyIdentifierName();
            PsiElement identifier = anyIdentifierName.getIdentifier();
            if (identifier == null) {
                return CachedValueProvider.Result.create(null, variableReference);
            }
            PsiReference reference = identifier.getReference();
            if (reference == null) {
                return CachedValueProvider.Result.create(null, variableReference);
            }
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement == null) {
                return CachedValueProvider.Result.create(null, variableReference);
            }
            PsiElement parent = resolvedElement.getParent();
            if (!(parent instanceof BallerinaAnyIdentifierName)) {
                return CachedValueProvider.Result.create(null, variableReference);
            }
            BallerinaFunctionDefinition definition = PsiTreeUtil.getParentOfType(parent,
                    BallerinaFunctionDefinition.class);
            if (definition != null) {
                BallerinaCallableUnitSignature callableUnitSignature = definition.getCallableUnitSignature();
                if (callableUnitSignature == null) {
                    return CachedValueProvider.Result.create(null, variableReference);
                }
                BallerinaReturnParameter returnParameter = callableUnitSignature.getReturnParameter();
                if (returnParameter == null) {
                    return CachedValueProvider.Result.create(null, variableReference);
                }
                BallerinaReturnType returnType = returnParameter.getReturnType();
                if (returnType == null) {
                    return CachedValueProvider.Result.create(null, variableReference);
                }
                BallerinaTypeName typeName = returnType.getTypeName();
                if (typeName instanceof BallerinaTupleTypeName) {
                    return CachedValueProvider.Result.create(
                            ((BallerinaTupleTypeName) typeName).getTypeNameList().get(0), variableReference);
                }
                return CachedValueProvider.Result.create(typeName, variableReference);
            }

            BallerinaObjectFunctionDefinition objectFunctionDefinition = PsiTreeUtil.getParentOfType(parent,
                    BallerinaObjectFunctionDefinition.class);
            if (objectFunctionDefinition != null) {
                BallerinaCallableUnitSignature callableUnitSignature =
                        objectFunctionDefinition.getCallableUnitSignature();
                if (callableUnitSignature == null) {
                    return CachedValueProvider.Result.create(null, variableReference);
                }
                BallerinaReturnParameter returnParameter = callableUnitSignature.getReturnParameter();
                if (returnParameter == null) {
                    return CachedValueProvider.Result.create(null, variableReference);
                }
                BallerinaReturnType returnType = returnParameter.getReturnType();
                if (returnType == null) {
                    return CachedValueProvider.Result.create(null, variableReference);
                }
                BallerinaTypeName typeName = returnType.getTypeName();
                if (typeName instanceof BallerinaTupleTypeName) {

                    return CachedValueProvider.Result.create(
                            ((BallerinaTupleTypeName) typeName).getTypeNameList().get(0), variableReference);
                }
                return CachedValueProvider.Result.create(typeName, variableReference);
            }
            return CachedValueProvider.Result.create(null, variableReference);
        });
    }

    private static boolean isVariableReferenceInForEach(@NotNull BallerinaVariableReference variableReference) {
        PsiElement parent = variableReference.getParent();
        if (!(parent instanceof BallerinaVariableReferenceList)) {
            return false;
        }
        PsiElement superParent = parent.getParent();
        return superParent instanceof BallerinaForeachStatement;
    }

    private static PsiElement getType(@NotNull BallerinaForeachStatement foreachStatement) {
        return CachedValuesManager.getCachedValue(foreachStatement, () -> {
            BallerinaExpression expression = foreachStatement.getExpression();
            if (expression == null) {
                return CachedValueProvider.Result.create(null, foreachStatement);
            }
            return CachedValueProvider.Result.create(getBallerinaTypeFromExpression(expression), foreachStatement);
        });
    }

    public static PsiElement getType(@NotNull BallerinaIdentifier identifier) {
        return CachedValuesManager.getCachedValue(identifier, () -> {
            PsiReference reference = identifier.getReference();
            if (reference == null) {
                return CachedValueProvider.Result.create(null, identifier);
            }
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement == null) {
                return CachedValueProvider.Result.create(null, identifier);
            }
            BallerinaAssignmentStatement assignmentStatement = PsiTreeUtil.getParentOfType(resolvedElement,
                    BallerinaAssignmentStatement.class);
            if (assignmentStatement != null) {
                return CachedValueProvider.Result.create(getType(assignmentStatement), identifier);
            }
            BallerinaVariableDefinitionStatement definitionStatement = PsiTreeUtil.getParentOfType(resolvedElement,
                    BallerinaVariableDefinitionStatement.class);
            if (definitionStatement != null) {
                return CachedValueProvider.Result.create(getType(definitionStatement), identifier);
            }
            return CachedValueProvider.Result.create(null, identifier);
        });
    }

    @Nullable
    private static PsiElement getBallerinaTypeFromVariableReference(@NotNull BallerinaVariableReference
                                                                            variableReference) {
        return CachedValuesManager.getCachedValue(variableReference,
                () -> CachedValueProvider.Result.create(getBallerinaTypeFromVariableReference(variableReference, -1),
                        variableReference));
    }

    @Nullable
    private static BallerinaFunctionDefinition getFunctionDefinition(@NotNull BallerinaFunctionInvocationReference
                                                                             invocationReference) {
        return CachedValuesManager.getCachedValue(invocationReference, () -> {
            BallerinaFunctionInvocation functionInvocation = invocationReference.getFunctionInvocation();
            BallerinaFunctionNameReference functionNameReference = functionInvocation.getFunctionNameReference();
            BallerinaAnyIdentifierName anyIdentifierName = functionNameReference.getAnyIdentifierName();
            PsiElement identifier = anyIdentifierName.getIdentifier();
            if (identifier == null) {
                return CachedValueProvider.Result.create(PsiTreeUtil.getParentOfType(null,
                        BallerinaFunctionDefinition.class), invocationReference);
            }
            PsiReference reference = identifier.getReference();
            if (reference == null) {
                return CachedValueProvider.Result.create(PsiTreeUtil.getParentOfType(null,
                        BallerinaFunctionDefinition.class), invocationReference);
            }
            PsiElement resolvedElement = reference.resolve();
            return CachedValueProvider.Result.create(PsiTreeUtil.getParentOfType(resolvedElement,
                    BallerinaFunctionDefinition.class), invocationReference);
        });
    }

    @Nullable
    private static BallerinaTypeName getReturnTypeFromFunction(@NotNull BallerinaFunctionDefinition definition) {
        return CachedValuesManager.getCachedValue(definition, () -> {
            BallerinaCallableUnitSignature callableUnitSignature = definition.getCallableUnitSignature();
            if (callableUnitSignature == null) {
                return CachedValueProvider.Result.create(null, definition);
            }
            BallerinaReturnParameter returnParameter = callableUnitSignature.getReturnParameter();
            if (returnParameter == null) {
                return CachedValueProvider.Result.create(null, definition);
            }
            BallerinaReturnType returnType = returnParameter.getReturnType();
            if (returnType == null) {
                return CachedValueProvider.Result.create(null, definition);
            }
            return CachedValueProvider.Result.create(returnType.getTypeName(), definition);
        });
    }

    @Nullable
    public static PsiElement resolveBallerinaType(@NotNull BallerinaVariableDefinitionStatement statement) {
        return CachedValuesManager.getCachedValue(statement, () -> {
            BallerinaVariableDefinitionStatementWithAssignment withAssignment = statement
                    .getVariableDefinitionStatementWithAssignment();
            BallerinaVariableDefinitionStatementWithoutAssignment withoutAssignment = statement
                    .getVariableDefinitionStatementWithoutAssignment();
            BallerinaTypeName type = null;
            if (withAssignment != null) {
                type = withAssignment.getTypeName();
            } else if (withoutAssignment != null) {
                type = withoutAssignment.getTypeName();
            }
            return type != null ? CachedValueProvider.Result.create(getTypeFromTypeName(type), statement) : null;
        });
    }

    @Nullable
    public static PsiElement resolveTypeToDefinition(@NotNull BallerinaTypeName typeName) {
        return CachedValuesManager.getCachedValue(typeName, () -> {
            PsiReference reference = typeName.findReferenceAt(typeName.getTextLength());
            if (reference == null) {
                return CachedValueProvider.Result.create(null, typeName);
            }
            return CachedValueProvider.Result.create(reference.resolve(), typeName);
        });
    }

    @Nullable
    private static PsiElement getTypeFromTypeName(@NotNull BallerinaTypeName type) {
        return CachedValuesManager.getCachedValue(type, () -> {
            PsiReference reference = type.findReferenceAt(type.getTextLength());
            if (reference == null) {
                BallerinaBuiltInReferenceTypeName builtInReferenceTypeName = PsiTreeUtil.findChildOfType(type,
                        BallerinaBuiltInReferenceTypeName.class);
                if (builtInReferenceTypeName == null) {
                    return CachedValueProvider.Result.create(type, type);
                }
                BallerinaFutureTypeName futureTypeName = builtInReferenceTypeName.getFutureTypeName();
                if (futureTypeName != null) {
                    return CachedValueProvider.Result.create(futureTypeName.getFuture(), type);
                }
                BallerinaJsonTypeName jsonTypeName = builtInReferenceTypeName.getJsonTypeName();
                if (jsonTypeName != null) {
                    return CachedValueProvider.Result.create(jsonTypeName.getJson(), type);
                }
                BallerinaMapTypeName mapTypeName = builtInReferenceTypeName.getMapTypeName();
                if (mapTypeName != null) {
                    return CachedValueProvider.Result.create(mapTypeName.getMap(), type);
                }
                BallerinaStreamTypeName streamTypeName = builtInReferenceTypeName.getStreamTypeName();
                if (streamTypeName != null) {
                    return CachedValueProvider.Result.create(streamTypeName.getStream(), type);
                }
                BallerinaTableTypeName tableTypeName = builtInReferenceTypeName.getTableTypeName();
                if (tableTypeName != null) {
                    return CachedValueProvider.Result.create(tableTypeName.getTable(), type);
                }
                BallerinaXmlTypeName xmlTypeName = builtInReferenceTypeName.getXmlTypeName();
                if (xmlTypeName != null) {
                    return CachedValueProvider.Result.create(xmlTypeName.getXml(), type);
                }
                return CachedValueProvider.Result.create(null, type);
            }
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement != null) {
                return CachedValueProvider.Result.create(resolvedElement, type);
            } else {
                return CachedValueProvider.Result.create(type, type);
            }
        });
    }

    @Nullable
    public static PsiElement getTypeNameFromField(@NotNull BallerinaFieldDefinition fieldDefinition) {
        return CachedValuesManager.getCachedValue(fieldDefinition, () -> {
            BallerinaTypeName typeName = fieldDefinition.getTypeName();
            if (typeName instanceof BallerinaSimpleTypeName) {
                return CachedValueProvider.Result.create(typeName, fieldDefinition);
            } else if (typeName instanceof BallerinaUnionTypeName) {
                BallerinaTypeName ballerinaTypeName = getTypeNameFromNillableType(((BallerinaUnionTypeName) typeName));
                if (ballerinaTypeName != null) {
                    // Todo - Use a util method
                    PsiReference reference = ballerinaTypeName.findReferenceAt(ballerinaTypeName.getTextLength());
                    if (reference == null) {
                        return CachedValueProvider.Result.create(null, fieldDefinition);
                    }
                    return CachedValueProvider.Result.create(reference.resolve(), fieldDefinition);
                } else {
                    return CachedValueProvider.Result.create(typeName, fieldDefinition);
                }
            } else if (typeName instanceof BallerinaNullableTypeName) {
                BallerinaTypeName typeNameFromNillableType = getTypeNameFromNillableType((BallerinaNullableTypeName)
                        typeName);
                PsiReference reference = typeNameFromNillableType.findReferenceAt(typeNameFromNillableType
                        .getTextLength());
                if (reference == null) {
                    return CachedValueProvider.Result.create(null, fieldDefinition);
                }
                return CachedValueProvider.Result.create(reference.resolve(), fieldDefinition);
            } else if (typeName instanceof BallerinaArrayTypeName) {
                return CachedValueProvider.Result.create(typeName, fieldDefinition);
            } else if (typeName instanceof BallerinaRecordTypeName) {
                return CachedValueProvider.Result.create(typeName, fieldDefinition);
            }
            return CachedValueProvider.Result.create(typeName, fieldDefinition);
        });
    }

    @Nullable
    public static PsiElement getTypeNameFromNamedPattern(@NotNull BallerinaTypeName typeName) {
        return CachedValuesManager.getCachedValue(typeName, () -> {
            if (typeName instanceof BallerinaTupleTypeName) {
                return CachedValueProvider.Result.create(PsiTreeUtil.getChildOfType(typeName,
                        BallerinaUnionTypeName.class), typeName);
            } else if (typeName instanceof BallerinaSimpleTypeName) {
                PsiReference reference = typeName.findReferenceAt(typeName.getTextLength());
                if (reference == null) {
                    return CachedValueProvider.Result.create(typeName, typeName);
                }
                return CachedValueProvider.Result.create(reference.resolve(), typeName);
            } else if (typeName instanceof BallerinaArrayTypeName) {
                return CachedValueProvider.Result.create(((BallerinaArrayTypeName) typeName).getTypeName(), typeName);
            }
            return CachedValueProvider.Result.create(null, typeName);
        });
    }

    @Nullable
    public static PsiElement getTypeNameFromParameter(@NotNull BallerinaParameterWithType parameter) {
        return CachedValuesManager.getCachedValue(parameter, () -> {
            BallerinaTypeName typeName = parameter.getTypeName();
            if (typeName instanceof BallerinaUnionTypeName) {
                return CachedValueProvider.Result.create(typeName, parameter);
            }
            PsiReference reference = typeName.findReferenceAt(typeName.getTextLength());
            if (reference == null) {
                return CachedValueProvider.Result.create(typeName, parameter);
            }
            return CachedValueProvider.Result.create(reference.resolve(), parameter);
        });
    }

    @Nullable
    public static BallerinaTypeName getTypeNameFromNillableType(@NotNull BallerinaUnionTypeName ballerinaTypeName) {
        return CachedValuesManager.getCachedValue(ballerinaTypeName, () -> {
            List<BallerinaTypeName> typeNameList = ballerinaTypeName.getTypeNameList();
            if (typeNameList.size() != 2) {
                return CachedValueProvider.Result.create(null, ballerinaTypeName);
            }
            BallerinaTypeName typeName1 = typeNameList.get(0);
            if (!(typeName1 instanceof BallerinaSimpleTypeName)) {
                return CachedValueProvider.Result.create(null, ballerinaTypeName);
            }
            BallerinaTypeName typeName2 = typeNameList.get(1);
            if (!(typeName2 instanceof BallerinaSimpleTypeName)) {
                return CachedValueProvider.Result.create(null, ballerinaTypeName);
            }

            BallerinaSimpleTypeName simpleTypeName1 = (BallerinaSimpleTypeName) typeName1;
            BallerinaSimpleTypeName simpleTypeName2 = (BallerinaSimpleTypeName) typeName2;
            if (simpleTypeName1.getReferenceTypeName() != null && simpleTypeName2.getEmptyTupleLiteral() != null) {
                return CachedValueProvider.Result.create(typeName1, ballerinaTypeName);
            }
            if (simpleTypeName1.getEmptyTupleLiteral() != null && simpleTypeName2.getReferenceTypeName() != null) {
                return CachedValueProvider.Result.create(typeName2, ballerinaTypeName);
            }
            return CachedValueProvider.Result.create(null, ballerinaTypeName);
        });
    }

    @Nullable
    public static BallerinaTypeName liftErrorAndGetType(@NotNull BallerinaUnionTypeName ballerinaTypeName) {
        return CachedValuesManager.getCachedValue(ballerinaTypeName, () -> {
            List<BallerinaTypeName> typeNameList = ballerinaTypeName.getTypeNameList();
            if (typeNameList.size() != 2) {
                return CachedValueProvider.Result.create(null, ballerinaTypeName);
            }
            BallerinaTypeName typeName1 = typeNameList.get(0);
            if (!(typeName1 instanceof BallerinaSimpleTypeName)) {
                return CachedValueProvider.Result.create(null, ballerinaTypeName);
            }
            BallerinaTypeName typeName2 = typeNameList.get(1);
            if (!(typeName2 instanceof BallerinaSimpleTypeName)) {
                return CachedValueProvider.Result.create(null, ballerinaTypeName);
            }

            BallerinaSimpleTypeName simpleTypeName1 = (BallerinaSimpleTypeName) typeName1;
            BallerinaSimpleTypeName simpleTypeName2 = (BallerinaSimpleTypeName) typeName2;
            if (simpleTypeName1.getReferenceTypeName() != null && "error".equals(simpleTypeName2.getText())) {
                return CachedValueProvider.Result.create(typeName1, ballerinaTypeName);
            }
            if ("error".equals(simpleTypeName2.getText()) && simpleTypeName2.getReferenceTypeName() != null) {
                return CachedValueProvider.Result.create(typeName2, ballerinaTypeName);
            }
            return CachedValueProvider.Result.create(null, ballerinaTypeName);
        });
    }

    @Nullable
    public static PsiElement getConfigTypeDefinitionFromServiceType(@NotNull BallerinaTypeDefinition serviceType) {
        return CachedValuesManager.getCachedValue(serviceType, () -> {
            BallerinaTypeDefinition listenerType = BallerinaPsiImplUtil.getReturnTypeFromObjectFunction(serviceType,
                    "getEndpoint");
            if (listenerType == null) {
                return CachedValueProvider.Result.create(null, serviceType);
            }
            return CachedValueProvider.Result.create(getConfigTypeDefinitionFromListener(listenerType), serviceType);
        });
    }

    @Nullable
    public static PsiElement getConfigTypeDefinitionFromListener(@NotNull BallerinaTypeDefinition listenerType) {
        return CachedValuesManager.getCachedValue(listenerType, () -> {
            BallerinaFormalParameterList parameterListNode =
                    BallerinaPsiImplUtil.getParameterFromObjectFunction(listenerType, "init");
            if (parameterListNode == null || parameterListNode.getParameterList().isEmpty()) {
                return CachedValueProvider.Result.create(null, listenerType);
            }

            BallerinaParameter firstParameter = parameterListNode.getParameterList().get(0);
            List<BallerinaParameterWithType> parameterWithTypeList = firstParameter.getParameterWithTypeList();
            if (parameterWithTypeList.isEmpty()) {
                return CachedValueProvider.Result.create(null, listenerType);
            }

            BallerinaParameterWithType parameterWithType = parameterWithTypeList.get(0);
            BallerinaTypeName typeName = parameterWithType.getTypeName();
            PsiReference reference = typeName.findReferenceAt(typeName.getTextLength());
            if (reference == null) {
                return CachedValueProvider.Result.create(null, listenerType);
            }
            return CachedValueProvider.Result.create(reference.resolve(), listenerType);
        });
    }

    @NotNull
    public static List<BallerinaFieldDefinition> resolveConfig(@NotNull PsiElement element) {
        return CachedValuesManager.getCachedValue(element, () -> {
            BallerinaServiceDefinition ballerinaServiceDefinition =
                    PsiTreeUtil.getParentOfType(element, BallerinaServiceDefinition.class);
            if (ballerinaServiceDefinition == null) {
                return CachedValueProvider.Result.create(ContainerUtil.newArrayList(), element);
            }
            return CachedValueProvider.Result.create(ContainerUtil.newArrayList(), element);
        });
    }

    @NotNull
    public static BallerinaTypeName getTypeNameFromNillableType(@NotNull BallerinaNullableTypeName ballerinaTypeName) {
        return ballerinaTypeName.getTypeName();
    }

    public static boolean processDeclarations(@NotNull BallerinaCompositeElement o,
                                              @NotNull PsiScopeProcessor processor,
                                              @NotNull ResolveState state,
                                              PsiElement lastParent,
                                              @NotNull PsiElement place) {
        return BallerinaCompositeElementImpl.processDeclarationsDefault(o, processor, state, lastParent, place);
    }

    @NotNull
    public static PsiReference[] getReferences(@NotNull BallerinaCompletePackageName o) {
        if (o.getTextLength() < 2) {
            return PsiReference.EMPTY_ARRAY;
        }
        return new BallerinaCompletePackageNameReferenceSet(o).getAllReferences();
    }

    @Nullable
    public static PsiReference getReference(@NotNull BallerinaPackageReference ballerinaPackageReference) {
        BallerinaFile containingFile = ballerinaPackageReference.getContainingFile();

        List<BallerinaNamespaceDeclaration> namespaceDeclarations =
                PsiTreeUtil.getChildrenOfTypeAsList(containingFile, BallerinaNamespaceDeclaration.class);

        PsiElement identifier = ballerinaPackageReference.getIdentifier();
        for (PsiElement definition : namespaceDeclarations) {
            if (definition instanceof BallerinaNamespaceDeclaration) {
                PsiElement namespaceIdentifier = ((BallerinaNamespaceDeclaration) definition).getIdentifier();
                if (namespaceIdentifier == null) {
                    continue;
                }
                if (identifier.getText().equals(namespaceIdentifier.getText())) {
                    return new BallerinaNameReferenceReference(((BallerinaIdentifier) identifier));
                }
            }
        }

        MultiMap<String, BallerinaImportDeclaration> importMap = containingFile.getImportMap();

        String packageName = identifier.getText();
        Collection<BallerinaImportDeclaration> ballerinaImportDeclarations = importMap.get(packageName);
        if (ballerinaImportDeclarations.isEmpty()) {
            return null;
        }
        BallerinaImportDeclaration firstItem = ContainerUtil.getFirstItem(ballerinaImportDeclarations);
        if (firstItem == null) {
            return null;
        }
        PsiElement shortPackageName = firstItem.getShortPackageName();
        if (shortPackageName == null) {
            return null;
        }
        PsiReference reference = shortPackageName.getReference();
        if (reference != null) {
            return reference;
        }
        BallerinaCompletePackageName completePackageName = firstItem.getCompletePackageName();
        if (completePackageName == null) {
            return null;
        }
        PsiReference[] references = completePackageName.getReferences();
        if (references.length == 0) {
            return null;
        }
        return new BallerinaPackageNameReference((BallerinaIdentifier)
                identifier, ArrayUtil.getFirstElement(references));
    }

    @Nullable
    public static PsiElement getShortPackageName(@NotNull BallerinaImportDeclaration ballerinaImportDeclaration) {
        return CachedValuesManager.getCachedValue(ballerinaImportDeclaration, () -> {
            BallerinaAlias alias = ballerinaImportDeclaration.getAlias();
            if (alias != null && alias.getIdentifier() != null) {
                return CachedValueProvider.Result.create(alias.getIdentifier(), ballerinaImportDeclaration);
            }
            BallerinaCompletePackageName completePackageName = ballerinaImportDeclaration.getCompletePackageName();
            if (completePackageName == null) {
                return CachedValueProvider.Result.create(null, ballerinaImportDeclaration);
            }
            List<BallerinaPackageName> packageNameList = completePackageName.getPackageNameList();
            BallerinaPackageName lastItem = ContainerUtil.getLastItem(packageNameList);
            if (lastItem != null) {
                return CachedValueProvider.Result.create(lastItem.getIdentifier(), ballerinaImportDeclaration);
            }
            return CachedValueProvider.Result.create(null, ballerinaImportDeclaration);
        });
    }

    public static boolean isAContentRoot(@Nullable PsiDirectory directory) {
        if (directory == null) {
            return true;
        }
        Module module = ModuleUtilCore.findModuleForPsiElement(directory);
        if (module == null) {
            // If we are trying to access a file which is not in the project, we should stop searching. Otherwise all
            // files in the file system will be searched.
            return true;
        }
        VirtualFile[] contentRoots = ProjectRootManager.getInstance(directory.getProject()).getContentRoots();
        for (VirtualFile contentRoot : contentRoots) {
            if (contentRoot.equals(directory.getVirtualFile())) {
                return true;
            }
        }

        Sdk moduleSdk = ModuleRootManager.getInstance(module).getSdk();
        if (moduleSdk != null) {
            VirtualFile homeDirectory = moduleSdk.getHomeDirectory();
            if (homeDirectory != null && homeDirectory.equals(directory.getVirtualFile())) {
                return true;
            }
        }

        Sdk projectSdk = ProjectRootManager.getInstance(directory.getProject()).getProjectSdk();
        if (projectSdk != null) {
            VirtualFile homeDirectory = projectSdk.getHomeDirectory();
            if (homeDirectory != null && homeDirectory.equals(directory.getVirtualFile())) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public static String formatBallerinaFunctionParameters(@Nullable BallerinaFormalParameterList parameterList) {
        if (parameterList == null) {
            return "()";
        }
        // Todo - Update formatting logic.
        // Todo - Format anonymous structs correctly.
        return "(" + parameterList.getText().replaceAll("\n", "").replaceAll("\\s+", " ") + ")";
    }

    @Nullable
    public static String formatBallerinaFunctionReturnType(@Nullable BallerinaReturnType ballerinaReturnType) {
        if (ballerinaReturnType == null) {
            return null;
        }
        BallerinaTypeName typeName = ballerinaReturnType.getTypeName();
        if (typeName instanceof BallerinaTupleTypeName) {
            List<BallerinaTypeName> typeNameList = ((BallerinaTupleTypeName) typeName).getTypeNameList();
            if (!typeNameList.isEmpty()) {
                return typeNameList.get(0).getText();
            }
        }
        return typeName.getText();
    }

    @Nullable
    public static String formatBallerinaTypeName(@Nullable BallerinaTypeName ballerinaTypeName) {
        if (ballerinaTypeName == null) {
            return null;
        }
        // Todo - Update formatting logic
        return ballerinaTypeName.getText();
    }

    @Nullable
    public static String formatParameterDefaultValue(@Nullable BallerinaExpression ballerinaExpression) {
        if (ballerinaExpression == null) {
            return null;
        }
        // Todo - Update formatting logic
        return ballerinaExpression.getText();
    }


    @Nullable
    public static String getObjectFieldDefaultValue(@Nullable BallerinaFieldDefinition ballerinaFieldDefinition) {
        if (ballerinaFieldDefinition == null) {
            return null;
        }
        BallerinaExpression expression = ballerinaFieldDefinition.getExpression();
        return formatParameterDefaultValue(expression);
    }

    @Nullable
    public static String getObjectFieldDefaultValue(@Nullable BallerinaObjectFieldDefinition ballerinaFieldDefinition) {
        if (ballerinaFieldDefinition == null) {
            return null;
        }
        BallerinaExpression expression = ballerinaFieldDefinition.getExpression();
        return formatParameterDefaultValue(expression);
    }

    public static boolean isObjectInitializer(@NotNull PsiElement element) {
        if (element instanceof BallerinaVariableDefinitionStatement) {
            PsiElement type = BallerinaPsiImplUtil.getType(((BallerinaVariableDefinitionStatement) element));
            if (type != null && type.getParent() instanceof BallerinaTypeDefinition) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds a file in project or module SDK.
     *
     * @param element a PsiElement
     * @param path    relative file path in the SDK
     * @return {@code null} if the file cannot be found, otherwise returns the corresponding {@link VirtualFile}.
     */
    @Nullable
    public static VirtualFile findFileInSDK(@NotNull PsiElement element, @NotNull String path) {
        // First we check the sources of module SDK.
        VirtualFile file = findFileInModuleSDK(element, path);
        if (file != null) {
            return file;
        }
        // Then we check the sources of project SDK.
        Project project = element.getProject();
        file = findFileInProjectSDK(project, path);
        if (file != null) {
            return file;
        }
        String sdkHomePath = BallerinaSdkService.getInstance(project).getSdkHomePath(null);
        if (sdkHomePath == null) {
            return null;
        }
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(sdkHomePath);
        if (virtualFile == null) {
            return null;
        }
        return VfsUtilCore.findRelativeFile("src/" + path, virtualFile);
    }

    /**
     * Finds a file in the module SDK.
     *
     * @param element a PsiElement
     * @param path    relative file path in the SDK
     * @return {@code null} if the file cannot be found, otherwise returns the corresponding {@link VirtualFile}.
     */
    @Nullable
    public static VirtualFile findFileInModuleSDK(@NotNull PsiElement element, @NotNull String path) {
        Module module = ModuleUtilCore.findModuleForPsiElement(element);
        if (module == null) {
            return null;
        }

        Sdk moduleSdk = ModuleRootManager.getInstance(module).getSdk();
        if (moduleSdk == null) {
            return null;
        }
        VirtualFile[] roots = moduleSdk.getSdkModificator().getRoots(OrderRootType.SOURCES);
        VirtualFile file;
        for (VirtualFile root : roots) {
            file = VfsUtilCore.findRelativeFile(path, root);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    /**
     * Finds a file in the project SDK.
     *
     * @param project current project
     * @param path    relative file path in the SDK
     * @return {@code null} if the file cannot be found, otherwise returns the corresponding {@link VirtualFile}.
     */
    @Nullable
    public static VirtualFile findFileInProjectSDK(@NotNull Project project, @NotNull String path) {
        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (projectSdk == null) {
            return null;
        }
        VirtualFile[] roots = projectSdk.getSdkModificator().getRoots(OrderRootType.SOURCES);
        VirtualFile file;
        for (VirtualFile root : roots) {
            file = VfsUtilCore.findRelativeFile(path, root);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    @Nullable
    public static VirtualFile getSDKSrcRoot(@NotNull Project project, @Nullable Module module) {
        LinkedHashSet<VirtualFile> sources = BallerinaSdkUtil.getSourcesPathsToLookup(project, module);
        if (sources.isEmpty()) {
            return null;
        }
        return ContainerUtil.getFirstItem(sources);
    }

    /**
     * Find the specified file in the project and returns the corresponding {@link PsiFile}.
     *
     * @param project a project
     * @param path    file path
     * @return corresponding psi file
     */
    @Nullable
    public static PsiFile findFileInProject(@NotNull Project project, @NotNull String path) {
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(path);
        if (virtualFile == null) {
            return null;
        }
        return PsiManager.getInstance(project).findFile(virtualFile);
    }

    @NotNull
    public static String getPackage(@NotNull PsiFile file) {
        Project project = file.getProject();
        String modulePath = project.getBasePath() + FILE_SEPARATOR;
        String filePath = file.getVirtualFile().getPath();
        filePath = filePath.replace(modulePath, "");
        if (!filePath.contains(FILE_SEPARATOR)) {
            return "";
        }
        int index = filePath.indexOf(FILE_SEPARATOR);
        return filePath.substring(0, index);
    }

    @NotNull
    public static String getPackage(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        String modulePath = project.getBasePath() + FILE_SEPARATOR;
        String filePath = virtualFile.getPath();
        filePath = filePath.replace(modulePath, "");
        if (!filePath.contains(FILE_SEPARATOR)) {
            return "";
        }
        int index = filePath.indexOf(FILE_SEPARATOR);
        return filePath.substring(0, index);
    }

    @NotNull
    public static String getFilePathInPackage(@NotNull Project project, @NotNull VirtualFile virtualFile) {
        String projectPath = project.getBasePath() + FILE_SEPARATOR;
        String filePath = virtualFile.getPath();
        filePath = filePath.replace(projectPath, "");
        if (!filePath.contains(FILE_SEPARATOR)) {
            return "";
        }
        int index = filePath.indexOf(FILE_SEPARATOR);
        return filePath.substring(index + 1);
    }

    public static boolean isAlreadyImported(@NotNull List<BallerinaImportDeclaration> allImportsInPackage,
                                            @NotNull String currentPackageName) {
        for (BallerinaImportDeclaration ballerinaImportDeclaration : allImportsInPackage) {
            BallerinaCompletePackageName completePackageName = ballerinaImportDeclaration.getCompletePackageName();
            if (completePackageName == null) {
                continue;
            }
            if (completePackageName.getText().equalsIgnoreCase(currentPackageName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAlreadyImported(@NotNull List<BallerinaImportDeclaration> allImportsInPackage,
                                            @Nullable String organization, @NotNull String packageName) {
        for (BallerinaImportDeclaration importDeclaration : allImportsInPackage) {
            // Organization can be null if the package is in the project.
            if (organization == null) {
                // If the organization is null, imported package should not have any organization.
                BallerinaOrgName orgName = importDeclaration.getOrgName();
                if (orgName != null) {
                    continue;
                }
                // Get the package name.
                BallerinaCompletePackageName completePackageName = importDeclaration.getCompletePackageName();
                if (completePackageName == null) {
                    continue;
                }
                // Check the package name. If they match, we have already imported that package.
                if (completePackageName.getText().equals(packageName)) {
                    return true;
                }
            } else {
                // Get the organization name. It should not be empty. If it is empty, we continue with the next
                // import declaration.
                BallerinaOrgName orgName = importDeclaration.getOrgName();
                if (orgName == null) {
                    continue;
                }
                // Get the package name from import declaration.
                BallerinaCompletePackageName completePackageName = importDeclaration.getCompletePackageName();
                if (completePackageName == null) {
                    continue;
                }
                // Check the organization and the package name.
                if (orgName.getText().equalsIgnoreCase(organization)
                        && completePackageName.getText().equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static List<VirtualFile> getPackagesFromProject(@NotNull Project project) {
        List<VirtualFile> packages = ContainerUtil.newArrayList();
        VirtualFile projectBaseDir = project.getBaseDir();
        VirtualFile[] children = projectBaseDir.getChildren();
        for (VirtualFile child : children) {
            // If the child is not a directory or the name starts with ".", we ignore it.
            if (!child.isDirectory() || child.getName().startsWith(".")) {
                continue;
            }
            packages.add(child);
        }
        return packages;
    }

    private static List<VirtualFile> getPackagesFromSDK(@NotNull Project project, @Nullable Module module) {
        List<VirtualFile> packages = ContainerUtil.newArrayList();
        VirtualFile sourceRoot = getSDKSrcRoot(project, module);
        if (sourceRoot == null) {
            return packages;
        }
        VirtualFile[] children = sourceRoot.getChildren();
        for (VirtualFile child : children) {
            if (!child.isDirectory()) {
                continue;
            }
            packages.add(child);
        }
        return packages;
    }

    public static List<LookupElement> getAllUnImportedImports(@NotNull Project project, @Nullable Module module,
                                                              @NotNull List<BallerinaImportDeclaration>
                                                                      allImportedPackages) {
        List<LookupElement> imports = new LinkedList<>();
        // From local project.
        List<VirtualFile> packages = getPackagesFromProject(project);
        for (VirtualFile aPackage : packages) {
            PsiDirectory directory = PsiManager.getInstance(project).findDirectory(aPackage);
            if (directory == null) {
                continue;
            }
            String packageName = directory.getName();
            if (isAlreadyImported(allImportedPackages, null, packageName)) {
                continue;
            }
            LookupElement lookup = BallerinaCompletionUtils.createUnImportedPackageLookup(null, packageName,
                    directory, AutoImportInsertHandler.INSTANCE_WITH_AUTO_POPUP);
            imports.add(lookup);
        }

        // Get packages from SDK.
        packages = getPackagesFromSDK(project, module);
        String organizationName = "ballerina";
        for (VirtualFile aPackage : packages) {
            PsiDirectory directory = PsiManager.getInstance(project).findDirectory(aPackage);
            if (directory == null) {
                continue;
            }
            String packageName = directory.getName();
            if (isAlreadyImported(allImportedPackages, organizationName, packageName)) {
                continue;
            }
            LookupElement lookup = BallerinaCompletionUtils.createUnImportedPackageLookup(organizationName,
                    packageName, directory, AutoImportInsertHandler.INSTANCE_WITH_AUTO_POPUP);
            imports.add(lookup);
        }

        // Get packages from user repository.
        List<VirtualFile> organizations = BallerinaPathModificationTracker.getAllOrganizationsInUserRepo();
        for (VirtualFile organization : organizations) {
            organizationName = organization.getName();
            packages = BallerinaPathModificationTracker.getPackagesFromOrganization(organizationName);
            for (VirtualFile aPackage : packages) {
                PsiDirectory directory = PsiManager.getInstance(project).findDirectory(aPackage);
                if (directory == null) {
                    continue;
                }
                String packageName = directory.getName();
                if (isAlreadyImported(allImportedPackages, organizationName, packageName)) {
                    continue;
                }
                LookupElement lookup = BallerinaCompletionUtils.createUnImportedPackageLookup(organizationName,
                        packageName, directory, AutoImportInsertHandler.INSTANCE_WITH_AUTO_POPUP);
                imports.add(lookup);
            }
        }
        return imports;
    }

    @NotNull
    public static Map<String, List<String>> getAllPackagesInResolvableScopes(@NotNull Project project,
                                                                             @Nullable Module module) {
        Map<String, List<String>> packageMap = ContainerUtil.newHashMap();

        // From local project.
        List<VirtualFile> packages = getPackagesFromProject(project);
        // This is used to identify that the package is in the local project.
        String organizationName = LOCAL_PACKAGE_PLACEHOLDER;
        for (VirtualFile aPackage : packages) {
            String name = aPackage.getName();
            if (packageMap.containsKey(name)) {
                packageMap.get(name).add(organizationName);
            } else {
                List<String> list = ContainerUtil.newArrayList();
                list.add(organizationName);
                packageMap.put(name, list);
            }
        }

        // Get packages from SDK.
        packages = getPackagesFromSDK(project, module);
        organizationName = "ballerina";
        for (VirtualFile aPackage : packages) {
            String name = aPackage.getName();
            if (packageMap.containsKey(name)) {
                packageMap.get(name).add(organizationName);
            } else {
                List<String> list = ContainerUtil.newArrayList();
                list.add(organizationName);
                packageMap.put(name, list);
            }
        }

        // Get packages from user repository.
        List<VirtualFile> organizations = BallerinaPathModificationTracker.getAllOrganizationsInUserRepo();
        for (VirtualFile organization : organizations) {
            organizationName = organization.getName();
            packages = BallerinaPathModificationTracker.getPackagesFromOrganization(organizationName);
            for (VirtualFile aPackage : packages) {
                String name = aPackage.getName();
                if (packageMap.containsKey(name)) {
                    packageMap.get(name).add(organizationName);
                } else {
                    List<String> list = ContainerUtil.newArrayList();
                    list.add(organizationName);
                    packageMap.put(name, list);
                }
            }
        }
        return packageMap;
    }

    @Nullable
    public static String suggestPackage(@NotNull PsiElement element) {
        PsiFile containingFile = element.getContainingFile();
        String filePath = containingFile.getVirtualFile().getPath();

        Project project = element.getProject();

        // From local project.
        List<VirtualFile> packages = getPackagesFromProject(project);
        // This is used to identify that the package is in the local project.
        for (VirtualFile aPackage : packages) {
            String packagePath = aPackage.getPath();
            if (filePath.contains(packagePath)) {
                return aPackage.getName();
            }
        }

        // Get the module for the element.
        Module module = ModuleUtilCore.findModuleForPsiElement(element);

        // Get packages from SDK.
        packages = getPackagesFromSDK(project, module);
        for (VirtualFile aPackage : packages) {
            String packagePath = aPackage.getPath();
            if (filePath.contains(packagePath)) {
                return aPackage.getName();
            }
        }

        // Get packages from user repository.
        List<VirtualFile> organizations = BallerinaPathModificationTracker.getAllOrganizationsInUserRepo();
        for (VirtualFile organization : organizations) {
            String organizationName = organization.getName();
            packages = BallerinaPathModificationTracker.getPackagesFromOrganization(organizationName);
            for (VirtualFile aPackage : packages) {
                String packagePath = aPackage.getPath();
                if (filePath.contains(packagePath)) {
                    return aPackage.getName();
                }
            }
        }
        return null;
    }

    public static boolean isConstraintableType(@NotNull PsiElement type) {
        if (!(type instanceof LeafPsiElement)) {
            return false;
        }
        IElementType elementType = ((LeafPsiElement) type).getElementType();
        return elementType == BallerinaTypes.JSON;
    }

    @Nullable
    public static PsiElement getConstrainedType(@NotNull PsiElement element) {
        BallerinaJsonTypeName jsonTypeName = PsiTreeUtil.getParentOfType(element, BallerinaJsonTypeName.class);
        if (jsonTypeName == null) {
            return null;
        }
        BallerinaNameReference nameReference = jsonTypeName.getNameReference();
        if (nameReference == null) {
            return null;
        }
        PsiElement identifier = nameReference.getIdentifier();
        if (identifier == null) {
            return null;
        }
        PsiReference reference = identifier.getReference();
        if (reference == null) {
            return null;
        }
        return reference.resolve();
    }
}

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

package org.ballerinalang.plugins.idea.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayUtil;
import org.antlr.jetbrains.adaptor.SymtabUtils;
import org.antlr.jetbrains.adaptor.psi.ScopeNode;
import org.antlr.jetbrains.adaptor.psi.Trees;
import org.antlr.jetbrains.adaptor.xpath.XPath;
import org.ballerinalang.plugins.idea.BallerinaFileType;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.psi.ActionInvocationNode;
import org.ballerinalang.plugins.idea.psi.AliasNode;
import org.ballerinalang.plugins.idea.psi.AnnotationAttachmentNode;
import org.ballerinalang.plugins.idea.psi.AnnotationDefinitionNode;
import org.ballerinalang.plugins.idea.psi.AssignmentStatementNode;
import org.ballerinalang.plugins.idea.psi.AttachmentPointNode;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.ConnectorDefinitionNode;
import org.ballerinalang.plugins.idea.psi.DefinitionNode;
import org.ballerinalang.plugins.idea.psi.ExpressionNode;
import org.ballerinalang.plugins.idea.psi.ExpressionVariableDefinitionStatementNode;
import org.ballerinalang.plugins.idea.psi.FieldDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FieldNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.MapStructKeyValueNode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.QuotedLiteralString;
import org.ballerinalang.plugins.idea.psi.StatementNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.PackagePathNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceListNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceNode;
import org.ballerinalang.plugins.idea.psi.WorkerDeclarationNode;
import org.ballerinalang.plugins.idea.psi.references.NameReference;
import org.ballerinalang.plugins.idea.util.BallerinaStringLiteralEscaper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BallerinaPsiImplUtil {

    private static final String PACKAGE_DECLARATION =
            "/compilationUnit/packageDeclaration/packagePath/packageName/Identifier";
    private static final String IMPORT_DECLARATION =
            "/compilationUnit/importDeclaration/packagePath/packageName/Identifier";
    private static final String ANNOTATION_DEFINITION = "//annotationDefinition/Identifier";
    private static final String CONSTANT_DEFINITION = "//constantDefinition/Identifier";
    private static final String GLOBAL_VARIABLE_DEFINITION = "//globalVariableDefinition/Identifier";
    private static final String FUNCTION_DEFINITION = "//functionDefinition/Identifier";
    private static final String CONNECTOR_DEFINITION = "//connectorDefinition/Identifier";
    private static final String ACTION_DEFINITION = "//actionDefinition/Identifier";
    private static final String STRUCT_DEFINITION = "//structDefinition/Identifier";
    private static final String PARAMETER_DEFINITION = "//parameter/Identifier";
    private static final String PLACEHOLDER_STRING = "IntellijIdeaRulezzz";

    private BallerinaPsiImplUtil() {

    }

    public static PsiElement findPackageNameReference(PsiNamedElement element) {
        Collection<? extends PsiElement> declarations = XPath.findAll(BallerinaLanguage.INSTANCE,
                element.getContainingFile(), IMPORT_DECLARATION);
        String id = element.getName();
        PsiElement resolvedElement = Trees.toMap(declarations).get(id);

        if (resolvedElement == null) {
            declarations = XPath.findAll(BallerinaLanguage.INSTANCE, element.getContainingFile(),
                    PACKAGE_DECLARATION);
            resolvedElement = Trees.toMap(declarations).get(id);
        }
        return resolvedElement;
    }

    /**
     * Used to resolve a package name to the directory.
     *
     * @param identifierElement the element which we need to resolve the reference
     * @return resolved element
     */
    @NotNull
    public static PsiDirectory[] resolveDirectory(PsiElement identifierElement) {
        if (identifierElement == null) {
            return new PsiDirectory[0];
        }
        PsiElement parent;
        PsiElement tempParent = identifierElement.getParent();
        if (tempParent == null) {
            return new PsiDirectory[0];
        }
        if (tempParent.getParent() instanceof AliasNode) {
            PsiElement temp = tempParent.getParent();
            while (temp != null && !(temp instanceof PackagePathNode)) {
                temp = temp.getPrevSibling();
            }
            if (temp != null) {
                parent = temp.getLastChild();
            } else {
                return new PsiDirectory[0];
            }
        } else {
            parent = tempParent;
        }

        // This is used to store all the packages which need to resolve the current package.
        List<PsiElement> packages = new ArrayList<>();
        packages.add(parent);

        // Find all previous PackageNameNode elements.
        PsiElement sibling = parent.getPrevSibling();
        while (sibling != null) {
            if (sibling instanceof PackageNameNode) {
                // Add the sibling to the index 0 because we are traversing backward.
                packages.add(0, sibling);
            }
            sibling = sibling.getPrevSibling();
        }

        Project project = identifierElement.getProject();
        List<PsiDirectory> results = new ArrayList<>();
        // Add matching directories from content roots.
        addMatchingDirectoriesFromContentRoots(packages, project, results);
        // Add matching directories from SDK sources.
        addMatchingDirectoriesFromSDK(project, packages, results, identifierElement);
        // Return the results.
        return results.toArray(new PsiDirectory[results.size()]);
    }

    private static void addMatchingDirectoriesFromContentRoots(List<PsiElement> packages, Project project,
                                                               List<PsiDirectory> results) {
        // We need to get the content roots from the project and find matching directories in each content root.
        VirtualFile[] contentRoots = ProjectRootManager.getInstance(project).getContentRoots();
        for (VirtualFile contentRoot : contentRoots) {
            // Get any matching directory from the content root.
            VirtualFile match = getMatchingDirectory(contentRoot, packages);
            // If there is a match, add it to the results.
            if (match != null) {
                results.add(PsiManager.getInstance(project).findDirectory(match));
            }
        }
    }

    private static void addMatchingDirectoriesFromSDK(Project project, List<PsiElement> packages,
                                                      List<PsiDirectory> results, PsiElement identifier) {
        // First we check the sources of module sdk.
        Module module = ModuleUtilCore.findModuleForPsiElement(identifier);
        if (module != null) {
            Sdk moduleSdk = ModuleRootManager.getInstance(module).getSdk();
            if (moduleSdk != null) {
                VirtualFile[] roots = moduleSdk.getSdkModificator().getRoots(OrderRootType.SOURCES);
                for (VirtualFile root : roots) {
                    VirtualFile match = getMatchingDirectory(root, packages);
                    if (match != null) {
                        results.add(PsiManager.getInstance(project).findDirectory(match));
                    }
                }
            }
            if (!results.isEmpty()) {
                return;
            }
        }

        // Then we check the sources of project sdk.
        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (projectSdk != null) {
            VirtualFile[] roots = projectSdk.getSdkModificator().getRoots(OrderRootType.SOURCES);
            for (VirtualFile root : roots) {
                VirtualFile match = getMatchingDirectory(root, packages);
                if (match != null) {
                    results.add(PsiManager.getInstance(project).findDirectory(match));
                }
            }
        }
    }

    /**
     * Returns a matching directory to the given package structure, starting from the given root.
     *
     * @param root     current root directory we are checking
     * @param packages list of package name elements used to get the matching directory from the given root
     * @return the matching directory
     */
    @Nullable
    private static VirtualFile getMatchingDirectory(@NotNull VirtualFile root, List<PsiElement> packages) {
        VirtualFile tempRoot = root;
        VirtualFile match = null;
        for (PsiElement element : packages) {
            match = tempRoot.findChild(element.getText());
            if (match == null) {
                break;
            }
            tempRoot = match;
        }
        return match;
    }

    @NotNull
    public static PsiDirectory[] suggestCurrentPackagePath(@NotNull PsiElement element) {

        List<PsiDirectory> results = new ArrayList<>();
        Project project = element.getProject();

        PsiElement parent = element.getParent();

        // This is used to store all the packages which need to resolve the current package.
        List<PsiElement> packages = new ArrayList<>();
        packages.add(parent);

        // Find all previous PackageNameNode elements.
        PsiElement sibling = parent.getPrevSibling();
        while (sibling != null) {
            if (sibling instanceof PackageNameNode) {
                // Add the sibling to the index 0 because we are traversing backward.
                packages.add(0, sibling);
            }
            sibling = sibling.getPrevSibling();
        }

        // We need to get the content roots from the project and find matching directories in each content root.
        VirtualFile[] contentRoots = ProjectRootManager.getInstance(project).getContentRoots();
        for (VirtualFile contentRoot : contentRoots) {
            // Get any matching directory from the content root.
            List<VirtualFile> matches = suggestDirectory(contentRoot, packages);
            // If there are matches, add it to the results.
            for (VirtualFile file : matches) {
                results.add(PsiManager.getInstance(project).findDirectory(file));
            }
        }
        return results.toArray(new PsiDirectory[results.size()]);
    }

    /**
     * Suggests packages for auto completing packages.
     *
     * @param element package name element
     * @return suggestions for auto completion
     */
    @NotNull
    public static PsiDirectory[] suggestImportPackages(PsiElement element) {
        List<PsiDirectory> results = new ArrayList<>();
        Project project = element.getProject();

        PsiElement parent = element.getParent();

        // This is used to store all the packages which need to resolve the current package.
        List<PsiElement> packages = new ArrayList<>();
        packages.add(parent);

        // Find all previous PackageNameNode elements.
        PsiElement sibling = parent.getPrevSibling();
        while (sibling != null) {
            if (sibling instanceof PackageNameNode) {
                // Add the sibling to the index 0 because we are traversing backward.
                packages.add(0, sibling);
            }
            sibling = sibling.getPrevSibling();
        }

        // We need to get the content roots from the project and find matching directories in each content root.
        VirtualFile[] contentRoots = ProjectRootManager.getInstance(project).getContentRoots();
        for (VirtualFile contentRoot : contentRoots) {
            // Get any matching directory from the content root.
            List<VirtualFile> matches = suggestDirectory(contentRoot, packages);
            // If there are matches, add it to the results.
            for (VirtualFile file : matches) {
                results.add(PsiManager.getInstance(project).findDirectory(file));
            }
        }

        // First we check the sources of module sdk.
        Module module = ModuleUtilCore.findModuleForPsiElement(element);
        if (module != null) {
            Sdk moduleSdk = ModuleRootManager.getInstance(module).getSdk();
            if (moduleSdk != null) {
                VirtualFile[] roots = moduleSdk.getSdkModificator().getRoots(OrderRootType.SOURCES);
                for (VirtualFile root : roots) {
                    List<VirtualFile> matches = suggestDirectory(root, packages);
                    for (VirtualFile file : matches) {
                        results.add(PsiManager.getInstance(project).findDirectory(file));
                    }
                }
            }
            if (!results.isEmpty()) {
                return results.toArray(new PsiDirectory[results.size()]);
            }
        }

        // Then we check the sources of project sdk.
        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (projectSdk != null) {
            VirtualFile[] roots = projectSdk.getSdkModificator().getRoots(OrderRootType.SOURCES);
            for (VirtualFile root : roots) {
                List<VirtualFile> matches = suggestDirectory(root, packages);
                for (VirtualFile file : matches) {
                    results.add(PsiManager.getInstance(project).findDirectory(file));
                }
            }
        }
        return results.toArray(new PsiDirectory[results.size()]);
    }

    /**
     * Returns all the directories matching the given package structure starting from the given root.
     *
     * @param root     current root directory
     * @param packages list of package name elements used to get the matching directory from the given root
     * @return all matching directories
     */
    @NotNull
    private static List<VirtualFile> suggestDirectory(@NotNull VirtualFile root, List<PsiElement> packages) {
        VirtualFile tempRoot = root;
        List<VirtualFile> results = new ArrayList<>();
        VirtualFile match;
        int count = 1;
        for (PsiElement element : packages) {
            if (count == packages.size()) {
                for (VirtualFile file : tempRoot.getChildren()) {
                    if (file.isDirectory() && !file.getName().startsWith(".")) {
                        results.add(file);
                    }
                }
            } else {
                match = tempRoot.findChild(element.getText());
                if (match == null) {
                    break;
                }
                tempRoot = match;
            }
            count++;
        }
        return results;
    }

    public static boolean hasSubdirectories(@NotNull PsiDirectory directory) {
        VirtualFile virtualFile = directory.getVirtualFile();
        VirtualFile[] children = virtualFile.getChildren();
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public static List<PsiElement> getAllConnectorsInCurrentPackage(PsiElement element) {
        if (element instanceof PsiDirectory) {
            return getAllConnectorsInPackage((PsiDirectory) element);
        }
        PsiElement parent = element.getParent();
        if (parent != null) {
            return getAllConnectorsInPackage((PsiDirectory) parent);
        }
        return new LinkedList<>();
    }

    @NotNull
    public static List<PsiElement> getAllConnectorsInPackage(PsiDirectory packageElement) {
        return getAllMatchingElementsFromPackage(packageElement, CONNECTOR_DEFINITION);
    }

    @NotNull
    public static List<PsiElement> getAllAnnotationsInCurrentPackage(@NotNull PsiElement element) {
        if (element instanceof PsiDirectory) {
            return getAllAnnotationsInPackage((PsiDirectory) element);
        }
        PsiElement parent = element.getParent();
        if (parent != null) {
            return getAllAnnotationsInPackage((PsiDirectory) parent);
        }
        return new LinkedList<>();
    }

    @NotNull
    public static List<PsiElement> getAllAnnotationsInPackage(PsiDirectory packageElement) {
        return getAllMatchingElementsFromPackage(packageElement, ANNOTATION_DEFINITION);
    }

    @NotNull
    public static List<PsiElement> getAllStructsInCurrentPackage(PsiElement element) {
        if (element instanceof PsiDirectory) {
            return getAllStructsFromPackage((PsiDirectory) element);
        }
        PsiElement parent = element.getParent();
        if (parent != null) {
            return getAllStructsFromPackage((PsiDirectory) parent);
        }
        return new LinkedList<>();
    }

    @NotNull
    public static List<PsiElement> getAllStructsFromPackage(PsiDirectory directory) {
        return getAllMatchingElementsFromPackage(directory, STRUCT_DEFINITION);
    }

    @NotNull
    public static List<PsiElement> getAllFunctionsFromPackage(@NotNull PsiDirectory directory) {
        return getAllMatchingElementsFromPackage(directory, FUNCTION_DEFINITION);
    }

    @NotNull
    public static List<PsiElement> getAllConstantsFromPackage(PsiDirectory directory) {
        return getAllMatchingElementsFromPackage(directory, CONSTANT_DEFINITION);
    }

    @NotNull
    public static List<PsiElement> getAllGlobalVariablesFromPackage(PsiDirectory directory) {
        return getAllMatchingElementsFromPackage(directory, GLOBAL_VARIABLE_DEFINITION);
    }

    @NotNull
    public static List<PsiElement> getAllImportedPackagesInCurrentFile(@NotNull PsiFile file) {
        ArrayList<PsiElement> filteredPackages = new ArrayList<>();
        filteredPackages.addAll(getImportedPackagesInCurrentFile(file));
        filteredPackages.addAll(getPackagesImportedAsAliasInCurrentFile(file));
        return filteredPackages;
    }

    @NotNull
    public static List<PsiElement> getImportedPackagesInCurrentFile(@NotNull PsiFile file) {
        Collection<ImportDeclarationNode> allImports = PsiTreeUtil.findChildrenOfType(file,
                ImportDeclarationNode.class);
        ArrayList<PsiElement> filteredPackages = new ArrayList<>();
        for (ImportDeclarationNode importDeclaration : allImports) {
            Collection<AliasNode> aliasNodes = PsiTreeUtil.findChildrenOfType(importDeclaration, AliasNode.class);
            if (!aliasNodes.isEmpty()) {
                continue;
            }
            Collection<PsiElement> packagePathNodes = PsiTreeUtil.findChildrenOfType(importDeclaration,
                    PackagePathNode.class);
            PsiElement packagePathNode = packagePathNodes.iterator().next();
            PsiElement lastChild = packagePathNode.getLastChild();
            if (lastChild != null) {
                filteredPackages.add(lastChild);
            }
        }
        return filteredPackages;
    }

    @NotNull
    public static List<PsiElement> getPackagesImportedAsAliasInCurrentFile(@NotNull PsiFile file) {
        Collection<ImportDeclarationNode> allImports = PsiTreeUtil.findChildrenOfType(file,
                ImportDeclarationNode.class);
        ArrayList<PsiElement> filteredPackages = new ArrayList<>();
        for (ImportDeclarationNode importDeclaration : allImports) {
            Collection<AliasNode> aliasNodes = PsiTreeUtil.findChildrenOfType(importDeclaration, AliasNode.class);
            if (aliasNodes.isEmpty()) {
                continue;
            }
            PsiElement aliasNode = aliasNodes.iterator().next();
            PsiElement firstChild = aliasNode.getFirstChild();
            if (firstChild != null) {
                filteredPackages.add(firstChild);
            }
        }
        return filteredPackages;
    }

    @Nullable
    public static IdentifierPSINode resolveAlias(@NotNull PsiElement element) {
        ImportDeclarationNode importDeclarationNode = PsiTreeUtil.getParentOfType(element,
                ImportDeclarationNode.class);
        if (importDeclarationNode == null) {
            return null;
        }
        PackagePathNode packagePathNode = PsiTreeUtil.getChildOfType(importDeclarationNode,
                PackagePathNode.class);
        if (packagePathNode == null) {
            return null;
        }
        PackageNameNode[] packageNameNodes = PsiTreeUtil.getChildrenOfType(packagePathNode,
                PackageNameNode.class);
        if (packageNameNodes == null || packageNameNodes.length == 0) {
            return null;
        }
        PackageNameNode lastPackageName = packageNameNodes[packageNameNodes.length - 1];
        if (lastPackageName == null) {
            return null;
        }
        PsiElement nameIdentifier = lastPackageName.getNameIdentifier();
        if (nameIdentifier == null || !(nameIdentifier instanceof IdentifierPSINode)) {
            return null;
        }
        return (IdentifierPSINode) nameIdentifier;
    }

    /**
     * Returns all the elements in the given directory(package) which matches the given xpath.
     *
     * @param directory which is used to get the functions
     * @param xpath     xpath to the element
     * @return all functions in the given directory(package)
     */
    @NotNull
    private static List<PsiElement> getAllMatchingElementsFromPackage(@NotNull PsiDirectory directory, String xpath) {
        Project project = directory.getProject();
        List<PsiElement> results = new ArrayList<>();
        VirtualFile virtualFile = directory.getVirtualFile();
        VirtualFile[] children = virtualFile.getChildren();
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                continue;
            }
            PsiFile psiFile = PsiManager.getInstance(project).findFile(child);
            if (!(psiFile instanceof BallerinaFile)) {
                continue;
            }
            Collection<? extends PsiElement> functions = XPath.findAll(BallerinaLanguage.INSTANCE, psiFile, xpath);

            results.addAll(functions);
        }
        return results;
    }

    private static List<PsiElement> getAllMatchingElementsFromFile(@NotNull PsiFile file, String xpath) {
        List<PsiElement> results = new ArrayList<>(
                XPath.findAll(BallerinaLanguage.INSTANCE, file, xpath)
        );
        return results;
    }

    @NotNull
    public static List<PsiElement> resolveAction(PsiElement element) {
        List<PsiElement> results = new ArrayList<>();
        // Get tht parent element.
        PsiElement parent = element.getParent();
        // Get the ActionInvocation node.
        Collection<? extends PsiElement> actionInvocationNodes = XPath.findAll(BallerinaLanguage.INSTANCE, parent,
                "//actionInvocation");
        // If there is no actionInvocationNodes, return empty results.
        if (actionInvocationNodes.isEmpty()) {
            return results;
        }
        // There can be only one actionInvocation. So we get the next item from the iterator.
        PsiElement actionInvocationNode = actionInvocationNodes.iterator().next();
        if (!(actionInvocationNode instanceof ActionInvocationNode)) {
            return results;
        }
        // Get the NameReferenceNode. We use this to resolve to the correct Connector Definition.
        NameReferenceNode nameReferenceNode = PsiTreeUtil.getChildOfType(actionInvocationNode, NameReferenceNode.class);
        if (nameReferenceNode == null) {
            return results;
        }
        // Get the name identifier of nameReferenceNode.
        PsiElement nameIdentifier = nameReferenceNode.getNameIdentifier();
        if (nameIdentifier == null) {
            return results;
        }
        // Get the references.
        PsiReference[] nameReference = nameIdentifier.getReferences();
        if (nameReference.length == 0) {
            return results;
        }
        // Iterate through all references.
        for (PsiReference reference : nameReference) {
            // Resolve the reference.
            PsiElement resolvedElement = reference.resolve();
            // Resolved element will be not null for connector variables.
            if (resolvedElement != null && !(resolvedElement.getParent() instanceof ConnectorDefinitionNode)) {
                // Get the variable definition node.
                PsiElement variableDefinitionNode = resolvedElement.getParent();
                if (variableDefinitionNode == null) {
                    return results;
                }
                // Get the TypeNameNode. This contains the connector type.
                TypeNameNode typeNameNode = PsiTreeUtil.getChildOfType(variableDefinitionNode, TypeNameNode.class);
                if (typeNameNode == null) {
                    return results;
                }
                // Get the connector name.
                NameReferenceNode typeNameReferenceNode = PsiTreeUtil.findChildOfType(variableDefinitionNode,
                        NameReferenceNode.class);
                if (typeNameReferenceNode == null) {
                    return results;
                }
                // Get the name identifier of the connector variable.
                PsiElement typeNameIdentifier = typeNameReferenceNode.getNameIdentifier();
                if (typeNameIdentifier == null) {
                    return results;
                }
                // Get the references.
                PsiReference[] references = typeNameIdentifier.getReferences();
                // Iterate through all references.
                for (PsiReference psiReference : references) {
                    // Resolve reference.
                    PsiElement resolvedReference = psiReference.resolve();
                    // If the resolvedReference is null, multi resolve the reference.
                    if (resolvedReference == null) {
                        // Multi resolve.
                        ResolveResult[] resolveResults = ((NameReference) psiReference).multiResolve(false);
                        // Iterate through each result.
                        for (ResolveResult resolveResult : resolveResults) {
                            // Get the element.
                            PsiElement resolveResultElement = resolveResult.getElement();
                            if (resolveResultElement == null) {
                                continue;
                            }
                            // Get the ConnectorDefinitionNode parent node. This is used to get all the
                            // actions/native actions.
                            PsiElement connectorNode = resolveResultElement.getParent();
                            // Get all actions/native actions.
                            List<PsiElement> allActions = getAllActionsFromAConnector(connectorNode);
                            for (PsiElement action : allActions) {
                                // Get the matching action/native action.
                                if (element.getText().equals(action.getText())) {
                                    results.add(action);
                                }
                            }
                        }
                    } else {
                        // Get the ConnectorDefinitionNode parent node. This is used to get all the
                        // actions/native actions.
                        PsiElement connectorNode = resolvedReference.getParent();
                        // Get all actions/native actions.
                        List<PsiElement> allActions = getAllActionsFromAConnector(connectorNode);
                        for (PsiElement action : allActions) {
                            // Get the matching action/native action.
                            if (element.getText().equals(action.getText())) {
                                results.add(action);
                            }
                        }
                    }
                }
            } else {
                // Try to resolve the reference. This is used to resolve actions in the same package.
                PsiElement resolved = reference.resolve();
                if (resolved != null) {
                    // Get the ConnectorDefinitionNode parent node. This is used to get all the actions/native
                    // actions.
                    ConnectorDefinitionNode connectorNode = PsiTreeUtil.getParentOfType(resolved,
                            ConnectorDefinitionNode.class);
                    // Get all actions/native actions.
                    List<PsiElement> allActions = getAllActionsFromAConnector(connectorNode);
                    for (PsiElement action : allActions) {
                        // Get the matching action/native action.
                        if (element.getText().equals(action.getText())) {
                            results.add(action);
                        }
                    }
                } else {
                    // Multi resolve each of the reference.
                    ResolveResult[] resolveResults = ((NameReference) reference).multiResolve(false);
                    for (ResolveResult resolveResult : resolveResults) {
                        // Get the element. This will represent the identifier of the Connector definition.
                        resolvedElement = resolveResult.getElement();
                        if (resolvedElement == null) {
                            continue;
                        }
                        // Get the ConnectorDefinitionNode parent node. This is used to get all the actions/native
                        // actions.
                        ConnectorDefinitionNode connectorNode = PsiTreeUtil.getParentOfType(resolvedElement,
                                ConnectorDefinitionNode.class);
                        // Get all actions/native actions.
                        List<PsiElement> allActions = getAllActionsFromAConnector(connectorNode);
                        for (PsiElement action : allActions) {
                            // Get the matching action/native action.
                            if (element.getText().equals(action.getText())) {
                                results.add(action);
                            }
                        }
                    }
                }
            }
        }
        return results;
    }

    /**
     * Returns all actions/native actions defined in the given ConnectorDefinitionNode.
     *
     * @param connectorDefinitionNode PsiElement which represent a Connector Definition
     * @return List of all actions/native actions defined in the given ConnectorDefinitionNode.
     */
    @NotNull
    public static List<PsiElement> getAllActionsFromAConnector(PsiElement connectorDefinitionNode) {
        List<PsiElement> results = new ArrayList<>();
        // Get all actions
        Collection<? extends PsiElement> allActions = XPath.findAll(BallerinaLanguage.INSTANCE, connectorDefinitionNode,
                ACTION_DEFINITION);
        results.addAll(allActions);
        return results;
    }

    @NotNull
    public static List<PsiElement> getAllVariablesInResolvableScope(PsiElement element, PsiElement context) {
        List<PsiElement> results = new ArrayList<>();
        if (context instanceof BallerinaFile) {
            return results;
        }
        // Get all variables from the context.
        Collection<VariableDefinitionNode> variableDefinitions = PsiTreeUtil.findChildrenOfType(context,
                VariableDefinitionNode.class);
        // Iterate through each definition.
        for (VariableDefinitionNode variableDefinition : variableDefinitions) {
            if (variableDefinition == null) {
                continue;
            }
            // If the variable definition or parent contains the PLACEHOLDER_STRING, then continue because that is
            // the node which we are currently editing.
            if (variableDefinition.getText().contains(PLACEHOLDER_STRING)) {
                continue;
            }
            // Add variables.
            PsiElement nameIdentifier = variableDefinition.getNameIdentifier();
            if (nameIdentifier != null) {
                checkAndAddVariable(element, results, nameIdentifier);
            }
        }

        Collection<AssignmentStatementNode> assignmentStatementNodes = PsiTreeUtil.findChildrenOfType(context,
                AssignmentStatementNode.class);

        for (AssignmentStatementNode assignmentStatementNode : assignmentStatementNodes) {
            if (!assignmentStatementNode.isVar()) {
                continue;
            }

            VariableReferenceListNode variableReferenceListNode = PsiTreeUtil.getChildOfType(assignmentStatementNode,
                    VariableReferenceListNode.class);
            if (variableReferenceListNode == null) {
                continue;
            }

            VariableReferenceNode[] variableReferenceNodes = PsiTreeUtil.getChildrenOfType(variableReferenceListNode,
                    VariableReferenceNode.class);
            if (variableReferenceNodes == null) {
                continue;
            }

            for (VariableReferenceNode variableReferenceNode : variableReferenceNodes) {

                IdentifierPSINode identifierNode = PsiTreeUtil.findChildOfType(variableReferenceNode,
                        IdentifierPSINode.class);
                if (identifierNode != null) {
                    checkAndAddVariable(element, results, identifierNode);
                }
            }

        }

        // If the context is not null, get variables from parent context as well.
        // Ex:- If the current context is function body, we need to get parameters from function definition which is
        // the parent context.
        // We need to check the context.getContext() here, otherwise all variables in the file will be suggested.
        if (context != null && !(context.getContext() instanceof BallerinaFile)) {
            List<PsiElement> allVariablesInResolvableScope = getAllVariablesInResolvableScope(element,
                    context.getContext());
            for (PsiElement psiElement : allVariablesInResolvableScope) {
                if (!results.contains(psiElement)) {
                    results.add(psiElement);
                }
            }
        }
        return results;
    }

    @NotNull
    public static List<PsiElement> getAllParametersInResolvableScope(PsiElement element, PsiElement context) {
        List<PsiElement> results = new ArrayList<>();
        if (context instanceof BallerinaFile) {
            return results;
        }
        // Get all parameters from the context.
        Collection<? extends PsiElement> parameterDefinitions =
                XPath.findAll(BallerinaLanguage.INSTANCE, context, PARAMETER_DEFINITION);
        for (PsiElement parameterDefinition : parameterDefinitions) {
            if (!parameterDefinition.getText().contains(PLACEHOLDER_STRING) &&
                    !parameterDefinition.getParent().getText().contains(PLACEHOLDER_STRING)) {
                results.add(parameterDefinition);
            }
        }
        // If the context is not null, get variables from parent context as well.
        // Ex:- If the current context is function body, we need to get parameters from function definition which is
        // the parent context.
        // We need to check the context.getContext() here, otherwise all variables in the file will be suggested.
        if (context != null && !(context.getContext() instanceof BallerinaFile)) {
            List<PsiElement> allParametersInResolvableScope = getAllParametersInResolvableScope(element,
                    context.getContext());
            for (PsiElement psiElement : allParametersInResolvableScope) {
                if (!results.contains(psiElement)) {
                    results.add(psiElement);
                }
            }
        }
        return results;
    }

    private static void checkAndAddVariable(PsiElement element, List<PsiElement> results,
                                            @NotNull PsiElement variableDefinition) {
        // Get the variable definition node from the element which we are editing.
        VariableDefinitionNode variableDefinitionNode = PsiTreeUtil.getParentOfType(element,
                VariableDefinitionNode.class);
        if (variableDefinitionNode == null) {
            handleVariableDefinition(element, results, variableDefinition);
        } else {
            if (variableDefinition.getParent() instanceof ExpressionVariableDefinitionStatementNode) {
                if (variableDefinition.getParent().getTextOffset() < variableDefinitionNode.getTextOffset()) {
                    results.add(variableDefinition);
                }
            } else {
                StatementNode statementNode = PsiTreeUtil.getParentOfType(element, StatementNode.class);
                if (statementNode != null) {
                    PsiElement prevSibling = statementNode.getPrevSibling();
                    if (prevSibling == null) {
                        if (variableDefinition.getTextOffset() < statementNode.getTextOffset()) {
                            results.add(variableDefinition);
                        }
                        return;
                    }
                    if (!prevSibling.getText().equals(variableDefinition.getParent().getText())) {
                        if (statementNode.getTextOffset() > variableDefinition.getParent().getTextOffset()) {
                            results.add(variableDefinition);
                        }
                    }
                }
            }
        }
    }

    private static void handleVariableDefinition(PsiElement element, List<PsiElement> results,
                                                 PsiElement variableDefinition) {
        StatementNode statementNode = PsiTreeUtil.getParentOfType(element, StatementNode.class);
        if (statementNode == null) {
            PsiElement prevSibling = BallerinaCompletionUtils.getPreviousNonEmptyElement(element.getContainingFile(),
                    element.getTextOffset() - 1);
            if (prevSibling != null && prevSibling instanceof LeafPsiElement) {
                IElementType elementType = ((LeafPsiElement) prevSibling).getElementType();
                boolean isValidLocation = (variableDefinition.getParent().getTextOffset() <
                        prevSibling.getParent().getTextOffset())
                        || ((variableDefinition.getParent().getTextOffset() < element.getTextOffset())
                        && elementType != BallerinaTypes.ASSIGN);
                if (isValidLocation) {
                    results.add(variableDefinition);
                }
            }
        } else if (!PLACEHOLDER_STRING.equals(statementNode.getText())) {
            handleStatementNode(results, variableDefinition, statementNode);
        }
    }

    private static void handleStatementNode(List<PsiElement> results, PsiElement variableDefinition,
                                            StatementNode statementNode) {
        PsiElement prevSibling = statementNode.getPrevSibling();
        if (prevSibling != null && prevSibling.getText().isEmpty()) {
            PsiElement prevPrevSibling = PsiTreeUtil.skipSiblingsBackward(prevSibling, PsiWhiteSpace.class);
            PsiElement parent = variableDefinition.getParent();
            if (prevPrevSibling == null) {
                if (parent.getTextOffset() < prevSibling.getTextOffset()) {
                    results.add(variableDefinition);
                }
            } else {
                if ((parent != null && parent.getParent().equals(prevPrevSibling)) ||
                        (parent != null && parent.getTextOffset() <= prevPrevSibling.getTextOffset())) {
                    results.add(variableDefinition);
                }
            }
        } else {
            results.add(variableDefinition);
        }
    }

    @Nullable
    public static PsiElement resolveElement(ScopeNode scope, PsiNamedElement element, String... xpaths) {
        PsiElement resolved = null;
        for (String xpath : xpaths) {
            // Get all the matching elements from the given scope. We don't directly call SymtabUtils.resolve() here
            // because then the last matching element is returned. So if there are duplicate definitions, it
            // would resolve the last definition. But we want the first definition since it is the matching definition.
            Collection<? extends PsiElement> elements = XPath.findAll(BallerinaLanguage.INSTANCE, scope, xpath);
            // We need to get only the 1st matching element.
            for (PsiElement psiElement : elements) {
                if (element.getText().equals(psiElement.getText())) {
                    resolved = psiElement;
                    break;
                }
            }
            // If the element is resolved, we return the element. Otherwise check the parent scope. This is done via
            // calling the SymtabUtils.resolve().
            if (resolved != null) {
                break;
            } else {
                resolved = SymtabUtils.resolve(scope, BallerinaLanguage.INSTANCE, element, xpath);
            }
        }
        // If the resolved element is still null, it might be located in another file in the same package.
        if (resolved == null) {
            // Get the parent directory which is the package.
            PsiDirectory parentDirectory = element.getContainingFile().getParent();

            // If this happens, that means the element does not have any parent directory. That means the
            // VirtualFile only exist in memory. The reason can be calling parameters.getPosition() which will
            // return the PsiElement from the file modified in the memory. So use parameters.getOriginalPosition()
            // instead in the CompletionContributor if necessary.
            if (parentDirectory == null) {
                for (String xpath : xpaths) {
                    List<PsiElement> matchingElements = getAllMatchingElementsFromFile(element.getContainingFile(),
                            xpath);
                    // If there are no matching elements for the current xpath, continue with next xpath.
                    if (!matchingElements.isEmpty()) {
                        // If there are matching elements, iterate through them and check.
                        for (PsiElement matchingElement : matchingElements) {
                            // Check the name.
                            if (element.getText().equals(matchingElement.getText())) {
                                // If the name matches, we found the element. So we assign the matching element to
                                // resolved and break the loop.
                                return matchingElement;
                            }
                        }
                    }
                }
                return null;
            }

            PsiElement parent = element.getParent();
            PackageNameNode packageNameNode = PsiTreeUtil.getChildOfType(parent, PackageNameNode.class);
            if (packageNameNode != null) {
                return null;
            }

            // Iterate through all xpaths.
            for (String xpath : xpaths) {
                // Get all matching elements from a package (functions, etc).
                List<PsiElement> matchingElements = getAllMatchingElementsFromPackage(parentDirectory, xpath);
                // If there are no matching elements for the current xpath, continue with next xpath.
                if (!matchingElements.isEmpty()) {
                    // If there are matching elements, iterate through them and check.
                    for (PsiElement matchingElement : matchingElements) {
                        // Check the name.
                        if (element.getText().equals(matchingElement.getText())) {
                            // If the name matches, we found the element. So we assign the matching element to
                            // resolved and break the loop.
                            resolved = matchingElement;
                            break;
                        }
                    }
                }
            }
        }
        // Return the resolved element.
        return resolved;
    }

    @NotNull
    public static List<PsiElement> getAllAnnotationAttachmentsForType(PsiDirectory packageElement,
                                                                      @NotNull String type) {
        List<PsiElement> results = new ArrayList<>();
        List<PsiElement> annotationDefinitions = getAllMatchingElementsFromPackage(packageElement,
                ANNOTATION_DEFINITION);
        if (annotationDefinitions.isEmpty()) {
            return results;
        }
        for (PsiElement annotationDefinition : annotationDefinitions) {
            if (annotationDefinition.getParent() instanceof AnnotationDefinitionNode) {
                AnnotationDefinitionNode annotationDefinitionNode =
                        (AnnotationDefinitionNode) annotationDefinition.getParent();

                Collection<AttachmentPointNode> attachmentPointNodes =
                        PsiTreeUtil.findChildrenOfType(annotationDefinitionNode, AttachmentPointNode.class);

                if (attachmentPointNodes.isEmpty()) {
                    results.add(annotationDefinition);
                    continue;
                }
                for (AttachmentPointNode attachmentPointNode : attachmentPointNodes) {
                    if (type.equals(attachmentPointNode.getText())) {
                        results.add(annotationDefinition);
                        break;
                    }
                }
            }
        }
        return results;
    }

    @Nullable
    public static PsiDirectory resolvePackage(@NotNull PackageNameNode packageNameNode) {
        PsiElement nameIdentifier = packageNameNode.getNameIdentifier();
        if (nameIdentifier == null) {
            return null;
        }
        PsiReference reference = nameIdentifier.getReference();
        if (reference != null) {
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement != null && resolvedElement instanceof PsiDirectory) {
                return ((PsiDirectory) resolvedElement);
            }
        }
        PsiReference[] references = nameIdentifier.getReferences();
        if (references.length == 0) {
            return null;
        }
        for (PsiReference psiReference : references) {
            PsiElement resolvedElement = psiReference.resolve();
            if (resolvedElement == null) {
                continue;
            }
            if (resolvedElement instanceof PsiDirectory) {
                return ((PsiDirectory) resolvedElement);
            }
        }
        return null;
    }

    public static List<PsiDirectory> getAllPackagesInResolvableScopes(@NotNull Project project) {
        GlobalSearchScope scope = GlobalSearchScope.allScope(project);
        // Todo - Use caching
        //        Collection<VirtualFile> files = FileTypeIndex.getFiles(BallerinaFileType.INSTANCE, scope);
        //        return CachedValuesManager.getManager(project).getCachedValue(project,
        //                () -> CachedValueProvider.Result.create(getAllPackagesInResolvableScopes(project, scope),
        //                        ProjectRootManager.getInstance(project)));
        return getAllPackagesInResolvableScopes(project, scope);
    }

    private static List<PsiDirectory> getAllPackagesInResolvableScopes(@NotNull Project project,
                                                                       @NotNull GlobalSearchScope scope) {
        LinkedList<PsiDirectory> results = new LinkedList<>();
        for (VirtualFile file : FileTypeIndex.getFiles(BallerinaFileType.INSTANCE, scope)) {
            ProgressManager.checkCanceled();
            VirtualFile directory = file.getParent();
            if (directory == null || !directory.isDirectory()) {
                continue;
            }
            boolean isContentRoot = false;
            VirtualFile[] contentRoots = ProjectRootManager.getInstance(project).getContentRoots();
            for (VirtualFile contentRoot : contentRoots) {
                if (contentRoot.equals(file.getParent())) {
                    isContentRoot = true;
                }
            }
            if (isContentRoot) {
                continue;
            }
            PsiDirectory psiDirectory = PsiManager.getInstance(project).findDirectory(directory);
            if (psiDirectory != null && !results.contains(psiDirectory) && !psiDirectory.getName().startsWith(".")
                    && !project.getBaseDir().equals(directory)) {
                results.add(psiDirectory);
            }
        }
        return results;
    }

    /**
     * Adds an import declaration node to the file.
     *
     * @param file       file which is to be used to insert the import declaration node
     * @param importPath import path to be used in the import declaration node
     * @param alias      alias if needed. If this is {@code null}, it will be ignored
     * @return import declaration node which is added
     */
    @NotNull
    public static ImportDeclarationNode addImport(@NotNull PsiFile file, @NotNull String importPath,
                                                  @Nullable String alias) {
        PsiElement addedNode;
        Collection<ImportDeclarationNode> importDeclarationNodes = PsiTreeUtil.findChildrenOfType(file,
                ImportDeclarationNode.class);
        Project project = file.getProject();
        ImportDeclarationNode importDeclaration = BallerinaElementFactory.createImportDeclaration(project,
                importPath, alias);

        if (importDeclarationNodes.isEmpty()) {
            Collection<PackageDeclarationNode> packageDeclarationNodes = PsiTreeUtil.findChildrenOfType(file,
                    PackageDeclarationNode.class);
            if (packageDeclarationNodes.isEmpty()) {
                PsiElement[] children = file.getChildren();
                // Children cannot be empty since the IDEA adds placeholder string
                PsiElement nonEmptyElement = PsiTreeUtil.skipSiblingsForward(children[0], PsiWhiteSpace.class,
                        PsiComment.class);
                if (nonEmptyElement == null) {
                    nonEmptyElement = children[0];
                }
                addedNode = file.addBefore(importDeclaration, nonEmptyElement);
                file.addBefore(BallerinaElementFactory.createDoubleNewLine(project), nonEmptyElement);
            } else {
                PackageDeclarationNode packageDeclarationNode = packageDeclarationNodes.iterator().next();
                PsiElement parent = packageDeclarationNode.getParent();
                addedNode = parent.addAfter(importDeclaration, packageDeclarationNode);
                parent.addAfter(BallerinaElementFactory.createDoubleNewLine(project), packageDeclarationNode);
            }
        } else {
            LinkedList<ImportDeclarationNode> importDeclarations = new LinkedList<>(importDeclarationNodes);
            ImportDeclarationNode lastImport = importDeclarations.getLast();
            addedNode = lastImport.getParent().addAfter(importDeclaration, lastImport);
            lastImport.getParent().addAfter(BallerinaElementFactory.createNewLine(project), lastImport);
        }
        return ((ImportDeclarationNode) addedNode);
    }

    /**
     * Returns all imports as a map. Key is the last Package Name node or Alias node. Value is the import path.
     *
     * @param file file to get imports
     * @return map of imports
     */
    public static Map<String, String> getAllImportsInAFile(@NotNull PsiFile file) {
        Map<String, String> results = new HashMap<>();
        Collection<ImportDeclarationNode> importDeclarationNodes = PsiTreeUtil.findChildrenOfType(file,
                ImportDeclarationNode.class);
        for (ImportDeclarationNode importDeclarationNode : importDeclarationNodes) {
            // There can be two possible values for the imported package name. If there is no alias node in the
            // import declaration, the imported package name is the last package name in the import path node.
            // Otherwise the package name is the alias node.
            PackagePathNode packagePathNode = PsiTreeUtil.findChildOfType(importDeclarationNode, PackagePathNode.class);
            AliasNode aliasNode = PsiTreeUtil.findChildOfType(importDeclarationNode, AliasNode.class);
            if (aliasNode != null) {
                if (packagePathNode != null) {
                    // Key is the alias name node text. Value is the package path node text.
                    // Eg:  import ballerina.utils as builtin;
                    // Map: builtin -> ballerina.utils
                    results.put(aliasNode.getText(), packagePathNode.getText());
                }
            } else {
                if (packagePathNode != null) {
                    // We need to get all package name nodes from the package path node.
                    List<PackageNameNode> packageNameNodes =
                            new ArrayList<>(PsiTreeUtil.findChildrenOfType(importDeclarationNode,
                                    PackageNameNode.class));
                    // If there is no package path node, return empty map.
                    if (packageNameNodes.isEmpty()) {
                        return results;
                    }
                    // The package node is the last package name in the package path.
                    // Eg:  import ballerina.utils;
                    // Map: utils -> ballerina.utils
                    PackageNameNode lastNode = packageNameNodes.get(packageNameNodes.size() - 1);
                    results.put(lastNode.getText(), packagePathNode.getText());
                }
            }
        }
        return results;
    }

    @Nullable
    public static PsiElement resolveStruct(PsiElement resolvedVariableDefElement) {
        TypeNameNode typeNameNode = PsiTreeUtil.findChildOfType(resolvedVariableDefElement, TypeNameNode.class);
        if (typeNameNode == null) {
            return null;
        }
        NameReferenceNode nameReferenceNode = PsiTreeUtil.findChildOfType(typeNameNode, NameReferenceNode.class);
        if (nameReferenceNode == null) {
            return null;
        }
        PsiElement nameIdentifier = nameReferenceNode.getNameIdentifier();
        if (nameIdentifier == null) {
            return null;
        }
        PsiReference typeNameNodeReference = nameIdentifier.getReference();
        if (typeNameNodeReference == null) {
            return null;
        }
        PsiElement resolvedDefElement = typeNameNodeReference.resolve();
        if (resolvedDefElement == null) {
            return null;
        }
        if (!(resolvedDefElement.getParent() instanceof StructDefinitionNode)) {
            return null;
        }
        return resolvedDefElement;
    }

    public static PsiElement resolveField(PsiNamedElement element, PsiElement prevSibling) {
        if (prevSibling == null) {
            return null;
        }

        PsiReference reference = prevSibling.findReferenceAt(prevSibling.getContainingFile().getTextOffset());
        if (reference == null) {
            return null;
        }

        PsiElement resolvedElement = reference.resolve();
        if (resolvedElement == null) {
            return null;
        }

        PsiElement resolvedStruct = BallerinaPsiImplUtil.resolveStruct(resolvedElement);

        if (resolvedStruct == null) {
            resolvedStruct = BallerinaPsiImplUtil.resolveStruct(resolvedElement.getParent());
        }
        if (resolvedStruct == null) {
            return null;
        }
        if (resolvedStruct.getParent() instanceof StructDefinitionNode) {
            return ((StructDefinitionNode) resolvedStruct.getParent()).resolve(element);
        }
        return null;
    }

    @Nullable
    public static PsiElement resolveNameReferenceNode(@NotNull ScopeNode scopeNode, @NotNull PsiNamedElement element) {

        boolean isStructField = isStructField(element);
        if (!isStructField) {
            PsiElement definition = BallerinaPsiImplUtil.resolveElement(scopeNode, element,
                    "//functionDefinition/Identifier", "//connectorDefinition/Identifier",
                    "//structDefinition/Identifier");
            if (definition != null) {
                return definition;
            }

            Collection<AssignmentStatementNode> assignmentStatementNodes = PsiTreeUtil.findChildrenOfType(scopeNode,
                    AssignmentStatementNode.class);

            for (AssignmentStatementNode assignmentStatementNode : assignmentStatementNodes) {
                if (!assignmentStatementNode.isVar()) {
                    continue;
                }

                VariableReferenceListNode variableReferenceListNode = PsiTreeUtil.getChildOfType
                        (assignmentStatementNode,
                                VariableReferenceListNode.class);
                if (variableReferenceListNode == null) {
                    continue;
                }

                VariableReferenceNode[] variableReferenceNodes = PsiTreeUtil.getChildrenOfType
                        (variableReferenceListNode,
                                VariableReferenceNode.class);
                if (variableReferenceNodes == null) {
                    continue;
                }

                for (VariableReferenceNode variableReferenceNode : variableReferenceNodes) {

                    IdentifierPSINode identifierNode = PsiTreeUtil.findChildOfType(variableReferenceNode,
                            IdentifierPSINode.class);
                    if (identifierNode != null && identifierNode.getTextOffset() < element.getTextOffset()) {

                        if (element.getText().equals(identifierNode.getText())) {
                            return identifierNode;
                        }
                    }
                }
            }
        } else {

            PsiElement elementParent = element.getParent();
            if (elementParent instanceof FieldNode) {
                PsiElement prevSibling = elementParent.getPrevSibling();
                if (prevSibling != null) {
                    return BallerinaPsiImplUtil.resolveField(element, prevSibling);
                }
            }

            VariableReferenceNode variableReferenceNode = PsiTreeUtil.getParentOfType(element,
                    VariableReferenceNode.class);
            if (variableReferenceNode == null) {
                PsiElement prevToken = BallerinaCompletionUtils.getPreviousNonEmptyElement(element
                        .getContainingFile(), element.getTextOffset());
                if (prevToken instanceof LeafPsiElement) {
                    IElementType elementType = ((LeafPsiElement) prevToken).getElementType();
                    if (elementType == BallerinaTypes.DOT) {
                        PsiElement prevSibling = BallerinaCompletionUtils.getPreviousNonEmptyElement(element
                                .getContainingFile(), prevToken.getTextOffset());
                        return BallerinaPsiImplUtil.resolveField(element, prevSibling);
                    }
                }
                return null;
            }
            PsiElement prevSibling = variableReferenceNode.getPrevSibling();
            if (prevSibling != null) {
                if (prevSibling instanceof LeafPsiElement) {
                    IElementType elementType = ((LeafPsiElement) prevSibling).getElementType();
                    if (elementType == BallerinaTypes.DOT) {
                        PsiElement prevPrevSibling = BallerinaCompletionUtils.getPreviousNonEmptyElement(element
                                .getContainingFile(), prevSibling.getTextOffset());
                        return BallerinaPsiImplUtil.resolveField(element, prevPrevSibling);
                    }
                }
            }

            PsiElement nextSibling = variableReferenceNode.getNextSibling();
            if (nextSibling != null) {
                if (nextSibling instanceof LeafPsiElement) {
                    IElementType elementType = ((LeafPsiElement) nextSibling).getElementType();
                    if (elementType == BallerinaTypes.DOT) {
                        PsiElement parent = variableReferenceNode.getParent();
                        if (parent instanceof VariableReferenceNode) {
                            prevSibling = parent.getPrevSibling();
                            if (prevSibling instanceof LeafPsiElement) {
                                elementType = ((LeafPsiElement) nextSibling).getElementType();
                                if (elementType == BallerinaTypes.DOT) {
                                    PsiElement prevPrevSibling = BallerinaCompletionUtils.getPreviousNonEmptyElement
                                            (element.getContainingFile(), prevSibling.getTextOffset());
                                    return BallerinaPsiImplUtil.resolveField(element, prevPrevSibling);
                                }
                            }
                        }
                    }
                }
            }

            VariableDefinitionNode variableDefinitionNode =
                    PsiTreeUtil.getParentOfType(element, VariableDefinitionNode.class);
            if (variableDefinitionNode != null) {

                PsiElement resolvedStruct = BallerinaPsiImplUtil.resolveStruct(variableDefinitionNode);

                if (resolvedStruct != null && resolvedStruct.getParent() instanceof StructDefinitionNode) {
                    return ((StructDefinitionNode) resolvedStruct.getParent()).resolve(element);
                }
            }

            ExpressionNode expressionNode = PsiTreeUtil.getParentOfType(element, ExpressionNode.class);
            if (expressionNode != null) {
                if (expressionNode.getParent() instanceof MapStructKeyValueNode) {
                    return null;
                }
            }
        }
        PsiElement resolvedElement = BallerinaPsiImplUtil.resolveElement(scopeNode, element,
                "//variableDefinitionStatement/Identifier",
                "/parameter/Identifier");
        if (resolvedElement == null) {
            return null;
        }
        PsiElement commonContext = PsiTreeUtil.findCommonContext(element, resolvedElement);
        if (!(commonContext instanceof BallerinaFile)) {
            return resolvedElement;
        }
        return null;
    }

    public static boolean isStructField(PsiElement element) {
        ExpressionNode expressionNode = PsiTreeUtil.getParentOfType(element, ExpressionNode.class);
        if (expressionNode == null) {
            PsiElement token = BallerinaCompletionUtils.getPreviousNonEmptyElement(element.getContainingFile(),
                    element.getTextOffset());
            if (token instanceof LeafPsiElement) {
                IElementType elementType = ((LeafPsiElement) token).getElementType();
                if (elementType == BallerinaTypes.DOT) {
                    return true;
                }
            }
            return false;
        }
        PsiElement parent = expressionNode.getParent();
        if (parent instanceof MapStructKeyValueNode) {
            PsiElement[] children = parent.getChildren();
            PsiElement firstElement = ArrayUtil.getFirstElement(children);

            if (expressionNode.equals(firstElement)) {
                return true;
            }
        }
        //Eg: return person.name;
        PsiElement[] children = expressionNode.getChildren();
        if (children.length > 0) {
            if (children[0] instanceof VariableReferenceNode) {
                IdentifierPSINode identifierNode = PsiTreeUtil.findChildOfType(children[0],
                        IdentifierPSINode.class);
                // If the element is equal to the  child in 0th index, we don't consider it as a struct field since
                // it is the struct variable reference name.
                if (element.equals(identifierNode)) {
                    return false;
                }
                PsiElement[] superChildren = children[0].getChildren();
                if (superChildren.length == 3) {
                    return ".".equals(superChildren[1].getText());
                }
            }
        }
        return false;
    }

    /**
     * Gets annotation definition node from annotation attachment node.
     *
     * @param node {@link AnnotationAttachmentNode} node
     * @return corresponding {@link AnnotationDefinitionNode} node, {@code null} if the annotation attachment cannot
     * be resolved
     */
    @Nullable
    public static AnnotationDefinitionNode getAnnotationDefinitionNode(@NotNull AnnotationAttachmentNode node) {
        NameReferenceNode nameReferenceNode = PsiTreeUtil.getChildOfType(node, NameReferenceNode.class);
        if (nameReferenceNode == null) {
            return null;
        }
        PsiElement identifier = nameReferenceNode.getNameIdentifier();
        if (identifier == null) {
            return null;
        }
        PsiReference reference = identifier.getReference();
        if (reference == null) {
            return null;
        }
        PsiElement resolvedElement = reference.resolve();
        if (resolvedElement == null) {
            return null;
        }
        if (resolvedElement.getParent() instanceof AnnotationDefinitionNode) {
            return ((AnnotationDefinitionNode) resolvedElement.getParent());
        }
        return null;
    }

    @NotNull
    public static List<FieldDefinitionNode> getAnnotationFields(@NotNull AnnotationDefinitionNode node) {
        List<FieldDefinitionNode> results = new LinkedList<>();
        results.addAll(PsiTreeUtil.findChildrenOfType(node, FieldDefinitionNode.class));
        return results;
    }

    public static boolean canSuggestArrayLength(@NotNull PsiElement definition,
                                                @NotNull PsiElement reference) {
        TypeNameNode typeNameNode = PsiTreeUtil.getChildOfType(definition, TypeNameNode.class);
        if (typeNameNode == null) {
            return false;
        }

        String definitionText = typeNameNode.getText();
        String definitionRegex = "\\[\\s*]";
        int definitionDimension = getArrayDimension(definitionText, definitionRegex);
        if (definitionDimension == 0) {
            return false;
        }

        String referenceText = reference.getText();
        String referenceRegex = "\\[\\s*\\d+\\s*]";
        int referenceDimension = getArrayDimension(referenceText, referenceRegex);

        return definitionDimension - 1 >= referenceDimension;
    }

    private static int getArrayDimension(String definitionText, String definitionRegex) {
        final Pattern definitionPattern = Pattern.compile(definitionRegex);
        final Matcher definitionMatcher = definitionPattern.matcher(definitionText);
        int definitionDimension = 0;
        while (definitionMatcher.find()) {
            definitionDimension++;
        }
        return definitionDimension;
    }

    public static boolean isArrayDefinition(@NotNull PsiElement definitionNode) {
        TypeNameNode typeNameNode = PsiTreeUtil.getChildOfType(definitionNode, TypeNameNode.class);
        if (typeNameNode == null) {
            return false;
        }
        String definitionText = typeNameNode.getText();
        String definitionRegex = "\\[\\s*]";
        int definitionDimension = getArrayDimension(definitionText, definitionRegex);
        return definitionDimension != 0;
    }

    public static List<WorkerDeclarationNode> getWorkerDeclarations(PsiElement element) {
        List<WorkerDeclarationNode> results = new LinkedList<>();
        // Since there are no workers inside workers in the new grammar, we can directly suggest all workers in the
        // current definition node.
        DefinitionNode definitionNode = PsiTreeUtil.getParentOfType(element, DefinitionNode.class);
        if (definitionNode == null) {
            return results;
        }
        Collection<WorkerDeclarationNode> workerDeclarationNodes = PsiTreeUtil.findChildrenOfType(definitionNode,
                WorkerDeclarationNode.class);
        results.addAll(workerDeclarationNodes);
        return results;
    }

    @NotNull
    public static QuotedLiteralString updateText(@NotNull QuotedLiteralString quotedLiteralString,
                                                 @NotNull String text) {
        if (text.length() > 2) {
            if (quotedLiteralString.getText() != null) {
                StringBuilder outChars = new StringBuilder();
                BallerinaStringLiteralEscaper.escapeString(text.substring(1, text.length() - 1), outChars);
                outChars.insert(0, '"');
                outChars.append('"');
                text = outChars.toString();
            }
        }

        ASTNode valueNode = quotedLiteralString.getNode();
        assert valueNode instanceof LeafElement;

        ((LeafElement) valueNode).replaceWithText(text);
        return quotedLiteralString;
    }
}

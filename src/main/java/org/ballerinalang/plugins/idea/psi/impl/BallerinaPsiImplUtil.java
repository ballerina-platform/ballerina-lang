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

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
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
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
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
import org.ballerinalang.plugins.idea.psi.AnnotationDefinitionNode;
import org.ballerinalang.plugins.idea.psi.AttachmentPointNode;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.ConnectorDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.StatementNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.PackagePathNode;
import org.ballerinalang.plugins.idea.psi.references.NameReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    private static final String PACKAGE_PATH = "//packagePath";
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
        addMatchingDirectoriesFromSDK(project, packages, results);
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
                                                      List<PsiDirectory> results) {
        // Get all project source roots and find matching directories.
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

        // Get all project source roots and find matching directories.
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
    public static List<PsiElement> getAllImportedPackagesInCurrentFile(@NotNull PsiElement element) {

        PsiFile file = element.getContainingFile();

        Collection<ImportDeclarationNode> allImports = PsiTreeUtil.findChildrenOfType(file,
                ImportDeclarationNode.class);
        ArrayList<PsiElement> filteredPackages = new ArrayList<>();

        for (ImportDeclarationNode importDeclaration : allImports) {

            Collection<? extends PsiElement> aliasNodes = XPath.findAll(BallerinaLanguage.INSTANCE, importDeclaration,
                    "//alias");

            if (aliasNodes.isEmpty()) {
                Collection<? extends PsiElement> packagePathNodes =
                        XPath.findAll(BallerinaLanguage.INSTANCE, importDeclaration, PACKAGE_PATH);

                PsiElement packagePathNode = packagePathNodes.iterator().next();
                PsiElement lastChild = packagePathNode.getLastChild();
                if (lastChild != null) {
                    filteredPackages.add(lastChild);
                }
            } else {
                PsiElement aliasNode = aliasNodes.iterator().next();
                PsiElement firstChild = aliasNode.getFirstChild();
                if (firstChild != null) {
                    filteredPackages.add(firstChild);
                }
            }
        }
        return filteredPackages;
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
        Collection<? extends PsiElement> variableDefinitions =
                XPath.findAll(BallerinaLanguage.INSTANCE, context, "//variableDefinitionStatement/Identifier");
        // Iterate through each definition.
        for (PsiElement variableDefinition : variableDefinitions) {
            if (variableDefinition == null) {
                continue;
            }
            // If the variable definition or parent contains the PLACEHOLDER_STRING, then continue because that is
            // the node which we are currently editing.
            if (variableDefinition.getText().contains(PLACEHOLDER_STRING) ||
                    variableDefinition.getParent().getText().contains(PLACEHOLDER_STRING)) {
                continue;
            }
            // Add variables.
            checkAndAddVariable(element, results, variableDefinition);
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
            StatementNode statementNode = PsiTreeUtil.getParentOfType(element, StatementNode.class);
            if (statementNode != null) {
                PsiElement prevSibling = statementNode.getPrevSibling();
                if (prevSibling == null) {
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

    private static void handleVariableDefinition(PsiElement element, List<PsiElement> results, PsiElement
            variableDefinition) {
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
            PsiElement prevPrevSibling = PsiTreeUtil.skipSiblingsBackward(prevSibling,
                    PsiWhiteSpace.class);
            if (prevPrevSibling == null) {
                if (variableDefinition.getParent().getTextOffset() < prevSibling.getTextOffset()) {
                    results.add(variableDefinition);
                }
            } else {
                if (variableDefinition.getParent().getTextOffset() <= prevPrevSibling.getTextOffset()) {
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
            if (parentDirectory == null) {
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

    public static Map<String, String> getImportMap(@NotNull PsiFile file) {
        Map<String, String> results = new HashMap<>();
        Collection<PackagePathNode> packagePathNodes = PsiTreeUtil.findChildrenOfType(file, PackagePathNode.class);
        for (PackagePathNode packagePathNode : packagePathNodes) {
            AliasNode aliasNode = PsiTreeUtil.findChildOfType(packagePathNode, AliasNode.class);
            if (aliasNode != null) {

            } else {
                PackageNameNode[] packageNameNodes = PsiTreeUtil.getChildrenOfType(packagePathNode,
                        PackageNameNode.class);
                if (packageNameNodes == null || packageNameNodes.length == 0) {
                    return results;
                }
                PackageNameNode lastNode = packageNameNodes[packageNameNodes.length - 1];
                results.put(lastNode.getText(), packagePathNode.getText());
            }
        }
        return results;
    }
}

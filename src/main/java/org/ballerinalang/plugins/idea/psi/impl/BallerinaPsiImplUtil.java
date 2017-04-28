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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.indexing.FileBasedIndex;
import org.antlr.jetbrains.adaptor.SymtabUtils;
import org.antlr.jetbrains.adaptor.psi.IdentifierDefSubtree;
import org.antlr.jetbrains.adaptor.psi.ScopeNode;
import org.antlr.jetbrains.adaptor.psi.Trees;
import org.antlr.jetbrains.adaptor.xpath.XPath;
import org.ballerinalang.plugins.idea.BallerinaFileType;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.psi.ActionInvocationNode;
import org.ballerinalang.plugins.idea.psi.AliasNode;
import org.ballerinalang.plugins.idea.psi.AnnotationDefinitionNode;
import org.ballerinalang.plugins.idea.psi.AttachmentPointNode;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.ConnectorNode;
import org.ballerinalang.plugins.idea.psi.ExpressionNode;
import org.ballerinalang.plugins.idea.psi.FunctionInvocationStatementNode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.StatementNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.references.PackageNameReference;
import org.ballerinalang.plugins.idea.psi.PackagePathNode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.references.NameReference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class BallerinaPsiImplUtil {

    private BallerinaPsiImplUtil() {

    }

    public static PsiElement findPackageNameReference(PsiNamedElement element) {
        Collection<? extends PsiElement> declarations =
                XPath.findAll(BallerinaLanguage.INSTANCE, element.getContainingFile(),
                        "/compilationUnit/importDeclaration/packagePath/packageName/Identifier");
        String id = element.getName();
        PsiElement resolvedElement = Trees.toMap(declarations).get(id);

        if (resolvedElement == null) {
            declarations = XPath.findAll(BallerinaLanguage.INSTANCE, element.getContainingFile(),
                    "/compilationUnit/packageDeclaration/packagePath/packageName/Identifier");
            resolvedElement = Trees.toMap(declarations).get(id);
        }
        return resolvedElement;
    }

    public static PsiElement findConnectorReference(PsiNamedElement element) {
        Collection<? extends PsiElement> declarations =
                XPath.findAll(BallerinaLanguage.INSTANCE, element.getContainingFile(),
                        "//simpleType/Identifier");
        String id = element.getName();
        PsiElement resolvedElement = Trees.toMap(declarations).get(id);

        if (resolvedElement == null) {
            declarations = XPath.findAll(BallerinaLanguage.INSTANCE, element.getContainingFile(),
                    "//connectorDefinition/Identifier");
            resolvedElement = Trees.toMap(declarations).get(id);
        }
        return resolvedElement;
    }

    public static PsiElement findFunctionReference(PsiNamedElement element) {

        Project project = element.getProject();

        String packageName = element.getParent().getFirstChild().getText();
        String functionName = element.getText();

        // Get all files in the project including the files in the libraries.
        Collection<VirtualFile> fileList = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME,
                BallerinaFileType.INSTANCE, GlobalSearchScope.allScope(project));

        // If no files found, return null
        if (fileList.isEmpty()) {
            return null;
        }

        // For each file
        for (VirtualFile virtualFile : fileList) {
            // Get the psi file
            PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);

            Collection<? extends PsiElement> packageDefinition =
                    XPath.findAll(BallerinaLanguage.INSTANCE, psiFile,
                            "/compilationUnit/packageDeclaration/packagePath/packageName/Identifier");
            PsiElement resolvedElement = Trees.toMap(packageDefinition).get(packageName);

            if (resolvedElement == null) {
                continue;
            }

            // Find all function definitions in the file
            Collection<? extends PsiElement> declarations =
                    XPath.findAll(BallerinaLanguage.INSTANCE, psiFile, "/compilationUnit/functionDefinition");

            // For each function definition
            for (PsiElement declaration : declarations) {

                // Get the function name
                Collection<? extends PsiElement> functionNames =
                        XPath.findAll(BallerinaLanguage.INSTANCE, declaration, "/functionDefinition/Identifier");
                resolvedElement = Trees.toMap(functionNames).get(functionName);
                // If the resolvedElement is null, continue with the next function definition
                if (resolvedElement == null) {
                    continue;
                }
                // If the function names does not match, continue with the next function definition
                if (!functionName.equals(resolvedElement.getText())) {
                    continue;
                }

                // Get the expression list
                Collection<ParameterNode> paramList = PsiTreeUtil.findChildrenOfType(declaration, ParameterNode.class);

                PsiElement[] children = getFunctionInvocationStatement(element).getChildren()[1].getChildren()[1]
                        .getChildren();

                if (paramList.size() == children.length / 2 + 1) {
                    return resolvedElement;
                }
            }
        }
        return null;
    }

    public static PsiElement getFunctionInvocationStatement(PsiElement element) {
        PsiElement parent = element;
        while (!(parent instanceof FunctionInvocationStatementNode || parent instanceof ExpressionNode)
                && parent != null) {
            parent = parent.getParent();
        }
        return parent;
    }

    public static List<PsiElement> getAllFunctions(PsiElement element) {
        ArrayList<PsiElement> results = new ArrayList<>();
        PsiFile file = element.getContainingFile();
        Collection<? extends PsiElement> all = XPath.findAll(BallerinaLanguage.INSTANCE, file,
                "//functionDefinition/Identifier");

        for (PsiElement psiElement : all) {
            if (!psiElement.getText().contains("IntellijIdeaRulezzz")) {
                results.add(psiElement);
            }
        }
        return results;
    }

    public static ArrayList<PsiElement> getAllImportedPackages(PsiElement element) {
        PsiFile file = element.getContainingFile();

        Collection<ImportDeclarationNode> allImports = PsiTreeUtil.findChildrenOfType(file,
                ImportDeclarationNode.class);
        ArrayList<PsiElement> filteredPackages = new ArrayList<>();

        for (ImportDeclarationNode importDeclaration : allImports) {

            Collection<? extends PsiElement> packagePathNodes =
                    XPath.findAll(BallerinaLanguage.INSTANCE, importDeclaration, "//packagePath");

            PsiElement packagePathNode = packagePathNodes.iterator().next();

            PsiElement lastChild = packagePathNode.getLastChild();

            filteredPackages.add(lastChild);
            //            }
        }
        return filteredPackages;
    }

    /**
     * Used to resolve a package name to the directory.
     *
     * @param identifierElement the element which we need to resolve the reference
     * @return resolved element
     */
    public static PsiDirectory[] resolveDirectory(PsiElement identifierElement) {
        List<PsiDirectory> results = new ArrayList<>();
        Project project = identifierElement.getProject();

        PsiElement parent;

        if (identifierElement.getParent().getParent() instanceof AliasNode) {
            PsiElement temp = identifierElement.getParent().getParent();
            while (temp != null && !(temp instanceof PackagePathNode)) {
                temp = temp.getPrevSibling();
            }
            if (temp != null) {
                parent = temp.getLastChild();
            } else {
                return new PsiDirectory[0];
            }
        } else {
            parent = identifierElement.getParent();
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
        return results.toArray(new PsiDirectory[results.size()]);
    }

    /**
     * Returns a matching directory to the given package structure, starting from the given root.
     *
     * @param root     current root directory we are checking
     * @param packages list of package name elements used to get the matching directory from the given root
     * @return the matching directory
     */
    @Nullable
    private static VirtualFile getMatchingDirectory(VirtualFile root, List<PsiElement> packages) {
        VirtualFile match = null;
        for (PsiElement element : packages) {
            match = root.findChild(element.getText());
            if (match == null) {
                break;
            }
            root = match;
        }
        return match;
    }


    public static PsiDirectory[] suggestCurrentPackagePath(PsiElement element) {

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
            // If there is matches, add it to the results.
            if (matches != null) {
                for (VirtualFile file : matches) {
                    results.add(PsiManager.getInstance(project).findDirectory(file));
                }
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
            // If there is matches, add it to the results.
            if (matches != null) {
                for (VirtualFile file : matches) {
                    results.add(PsiManager.getInstance(project).findDirectory(file));
                }
            }
        }

        // Get all project source roots and find matching directories.
        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (projectSdk != null) {
            VirtualFile[] roots = projectSdk.getSdkModificator().getRoots(OrderRootType.SOURCES);
            for (VirtualFile root : roots) {
                List<VirtualFile> matches = suggestDirectory(root, packages);
                if (matches != null) {
                    for (VirtualFile file : matches) {
                        results.add(PsiManager.getInstance(project).findDirectory(file));
                    }
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
    @Nullable
    private static List<VirtualFile> suggestDirectory(VirtualFile root, List<PsiElement> packages) {
        List<VirtualFile> results = new ArrayList<>();
        VirtualFile match;
        int count = 1;
        for (PsiElement element : packages) {
            if (count == packages.size()) {
                //Todo - Use caching if needed
                for (VirtualFile file : root.getChildren()) {
                    if (file.isDirectory() && !file.getName().startsWith(".")) {
                        results.add(file);
                    }
                }
            } else {
                match = root.findChild(element.getText());
                if (match == null) {
                    break;
                }
                root = match;
            }
            count++;
        }
        return results;
    }

    public static boolean hasSubdirectories(PsiDirectory directory) {
        VirtualFile virtualFile = directory.getVirtualFile();
        VirtualFile[] children = virtualFile.getChildren();
        for (VirtualFile child : children) {
            if (child.isDirectory()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolves the given function to definitions.
     *
     * @param element element which needs to be resolved
     * @return the list of all resolved elements
     */
    public static List<PsiElement> resolveFunction(PsiElement element) {
        List<PsiElement> results = new ArrayList<>();

        // Get the packagePath element.
        Collection<? extends PsiElement> packagePaths =
                XPath.findAll(BallerinaLanguage.INSTANCE, element.getParent(), "//packagePath");

        if (packagePaths.isEmpty()) {
            // Todo - Resolve function in current package
            return results;
        } else {
            // There cannot be multiple package paths. So we get the next path from the iterator.
            PsiElement packagePathNode = packagePaths.iterator().next();

            //Get the last package name node.
            PsiElement packageNameNode = packagePathNode.getLastChild();
            if (packageNameNode == null || !(packageNameNode instanceof IdentifierDefSubtree)) {
                return results;
            }

            // Get the identifier.
            PsiElement identifier = ((IdentifierDefSubtree) packageNameNode).getNameIdentifier();

            // Get the reference.
            PsiReference reference = identifier.getReference();

            // Multi resolve the reference.
            ResolveResult[] resolveResults = ((PackageNameReference) reference).multiResolve(false);

            // If resolve result is 0, that means the package was not resolved.
            if (resolveResults.length == 0) {
                // Package is not imported or cannot resolve. Return the empty results.
                return results;
            }

            // Resolve result cannot be more than one because all package imports are unique. This should be
            // annotated using an annotator.
            for (ResolveResult resolveResult : resolveResults) {
                PsiElement element1 = resolveResult.getElement();
                if (!(element1 instanceof PsiDirectory)) {
                    continue;
                }

                // Todo - Use an util function to get values of multiple xpaths.
                List<PsiElement> allFunctionsInAPackage = getAllMatchingElementsFromPackage(((PsiDirectory) element1),
                        "//functionDefinition/Identifier");
                for (PsiElement psiElement : allFunctionsInAPackage) {
                    if (element.getText().equals(psiElement.getText())) {
                        results.add(psiElement);
                    }
                }
            }
        }
        return results;
    }

    public static List<PsiElement> resolveConnector(PsiElement element) {
        List<PsiElement> results = new ArrayList<>();

        // Todo - Add null checks
        Collection<? extends PsiElement> packagePaths =
                XPath.findAll(BallerinaLanguage.INSTANCE, element.getParent().getParent().getParent(), "//packagePath");

        if (packagePaths.isEmpty()) {
            // Todo - Resolve connectors in current package
            return results;
        } else {
            PsiElement packagePathNode = packagePaths.iterator().next();
            PsiElement packageNameNode = packagePathNode.getLastChild();

            if (packageNameNode == null || !(packageNameNode instanceof IdentifierDefSubtree)) {
                return results;
            }

            PsiElement identifier = ((IdentifierDefSubtree) packageNameNode).getNameIdentifier();

            // Get the reference.
            PsiReference reference = identifier.getReference();

            // Multi resolve the reference.
            ResolveResult[] resolveResults = ((PackageNameReference) reference).multiResolve(false);

            // If resolve result is 0, that means the package was not resolved.
            if (resolveResults.length == 0) {
                // Package is not imported or cannot resolve. Return the empty results.
                return results;
            }

            // Resolve result cannot be more than one because all package imports are unique. This should be
            // annotated using an annotator.
            for (ResolveResult resolveResult : resolveResults) {
                PsiElement element1 = resolveResult.getElement();
                if (!(element1 instanceof PsiDirectory)) {
                    continue;
                }

                // Todo - Use an util function to get values of multiple xpaths.
                List<PsiElement> allConnectorsInAPackage = getAllMatchingElementsFromPackage(((PsiDirectory) element1),
                        "//connectorDefinition/Identifier");
                for (PsiElement psiElement : allConnectorsInAPackage) {
                    if (element.getText().equals(psiElement.getText())) {
                        results.add(psiElement);
                    }
                }
            }
        }
        return results;
    }

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

    public static List<PsiElement> getAllConnectorsInPackage(PsiDirectory packageElement) {
        List<PsiElement> results = new ArrayList<>();
        List<PsiElement> connectors = getAllMatchingElementsFromPackage(packageElement,
                "//connectorDefinition/Identifier");
        if (connectors != null) {
            for (PsiElement connector : connectors) {
                results.add(connector);
            }
        }
        return results;
    }

    public static List<PsiElement> getAllAnnotationsInCurrentPackage(PsiElement element) {
        if (element instanceof PsiDirectory) {
            return getAllAnnotationsInPackage((PsiDirectory) element);
        }
        PsiElement parent = element.getParent();
        if (parent != null) {
            return getAllAnnotationsInPackage((PsiDirectory) parent);
        }
        return new LinkedList<>();
    }

    public static List<PsiElement> getAllAnnotationsInPackage(PsiDirectory packageElement) {
        List<PsiElement> results = new ArrayList<>();
        List<PsiElement> annotations = getAllMatchingElementsFromPackage(packageElement,
                "//annotationDefinition/Identifier");
        if (annotations != null) {
            for (PsiElement annotation : annotations) {
                results.add(annotation);
            }
        }
        return results;
    }

    public static List<PsiElement> getAllStructsInCurrentPackage(PsiElement element) {
        if (element instanceof PsiDirectory) {
            return getAllStructsInPackage((PsiDirectory) element);
        }
        PsiElement parent = element.getParent();
        if (parent != null) {
            return getAllStructsInPackage((PsiDirectory) parent);
        }
        return new LinkedList<>();
    }

    public static List<PsiElement> getAllStructsInPackage(PsiDirectory packageElement) {
        List<PsiElement> results = new ArrayList<>();
        List<PsiElement> structs = getAllMatchingElementsFromPackage(packageElement,
                "//structDefinition/Identifier");
        if (structs != null) {
            for (PsiElement struct : structs) {
                results.add(struct);
            }
        }
        return results;
    }

    public static List<PsiElement> getAllFunctionsInCurrentPackage(PsiElement element) {
        if (element instanceof PsiDirectory) {
            return getAllFunctionsInPackage((PsiDirectory) element);
        }
        PsiElement parent = element.getParent();
        if (parent != null) {
            return getAllFunctionsInPackage((PsiDirectory) parent);
        }
        return new LinkedList<>();
    }

    public static List<PsiElement> getAllFunctionsInPackage(PsiDirectory packageElement) {
        List<PsiElement> results = new ArrayList<>();
        List<PsiElement> functions = getAllMatchingElementsFromPackage(packageElement,
                "//functionDefinition/Identifier");
        if (functions != null) {
            for (PsiElement function : functions) {
                results.add(function);
            }
        }
        return results;
    }

    public static List<PsiElement> getAllConstantsInPackage(PsiDirectory packageElement) {
        List<PsiElement> results = new ArrayList<>();
        List<PsiElement> functions = getAllMatchingElementsFromPackage(packageElement,
                "//constantDefinition/Identifier");
        if (functions != null) {
            for (PsiElement function : functions) {
                results.add(function);
            }
        }
        return results;
    }

    public static ArrayList<PsiElement> getAllImportedPackagesInCurrentFile(PsiElement element) {
        PsiFile file = element.getContainingFile();

        Collection<ImportDeclarationNode> allImports = PsiTreeUtil.findChildrenOfType(file,
                ImportDeclarationNode.class);
        ArrayList<PsiElement> filteredPackages = new ArrayList<>();

        for (ImportDeclarationNode importDeclaration : allImports) {

            Collection<? extends PsiElement> aliasNodes = XPath.findAll(BallerinaLanguage.INSTANCE, importDeclaration,
                    "//alias");

            if (aliasNodes.isEmpty()) {
                Collection<? extends PsiElement> packagePathNodes =
                        XPath.findAll(BallerinaLanguage.INSTANCE, importDeclaration, "//packagePath");

                PsiElement packagePathNode = packagePathNodes.iterator().next();
                PsiElement lastChild = packagePathNode.getLastChild();
                filteredPackages.add(lastChild);
            } else {
                PsiElement aliasNode = aliasNodes.iterator().next();
                PsiElement firstChild = aliasNode.getFirstChild();
                filteredPackages.add(firstChild);
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
    public static List<PsiElement> getAllMatchingElementsFromPackage(PsiDirectory directory, String xpath) {
        if (directory == null) {
            return new LinkedList<>();
        }
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
            if (resolvedElement != null && !(resolvedElement.getParent() instanceof ConnectorNode)) {
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
                    ConnectorNode connectorNode = PsiTreeUtil.getParentOfType(resolved, ConnectorNode.class);
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
                        ConnectorNode connectorNode = PsiTreeUtil.getParentOfType(resolvedElement, ConnectorNode.class);
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
    public static List<PsiElement> getAllActionsFromAConnector(PsiElement connectorDefinitionNode) {
        List<PsiElement> results = new ArrayList<>();
        // Get all actions
        Collection<? extends PsiElement> allActions = XPath.findAll(BallerinaLanguage.INSTANCE, connectorDefinitionNode,
                "//actionDefinition/Identifier");
        for (PsiElement action : allActions) {
            results.add(action);
        }
        return results;
    }

    public static List<PsiElement> getAllVariablesInResolvableScope(PsiElement element, PsiElement context) {
        List<PsiElement> results = new ArrayList<>();
        // Get all variables from the context.
        Collection<? extends PsiElement> variableDefinitions =
                XPath.findAll(BallerinaLanguage.INSTANCE, context, "//variableDefinitionStatement/Identifier");
        for (PsiElement variableDefinition : variableDefinitions) {
            if (!variableDefinition.getText().contains("IntellijIdeaRulezzz") &&
                    !variableDefinition.getParent().getText().contains("IntellijIdeaRulezzz")) {
                VariableDefinitionNode variableDefinitionNode = PsiTreeUtil.getParentOfType(element,
                        VariableDefinitionNode.class);
                if (variableDefinitionNode == null) {
                    StatementNode statementNode = PsiTreeUtil.getParentOfType(element, StatementNode.class);
                    if (statementNode == null || !"IntellijIdeaRulezzz".equals(statementNode.getText())) {
                        results.add(variableDefinition);
                    }
                } else {
                    if (variableDefinition.getParent().getTextOffset() < variableDefinitionNode.getTextOffset()) {
                        results.add(variableDefinition);
                    }
                }
            }
        }
        // Get all parameters from the context.
        Collection<? extends PsiElement> parameterDefinitions =
                XPath.findAll(BallerinaLanguage.INSTANCE, context, "//parameter/Identifier");
        for (PsiElement parameterDefinition : parameterDefinitions) {
            if (!parameterDefinition.getText().contains("IntellijIdeaRulezzz") &&
                    !parameterDefinition.getParent().getText().contains("IntellijIdeaRulezzz")) {
                results.add(parameterDefinition);
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

    public static List<PsiElement> getAllAnnotationAttachmentsForType(PsiDirectory packageElement,
                                                                      @NotNull String type) {
        List<PsiElement> results = new ArrayList<>();
        List<PsiElement> annotationDefinitions = getAllMatchingElementsFromPackage(packageElement,
                "//annotationDefinition/Identifier");
        if (annotationDefinitions == null) {
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
}

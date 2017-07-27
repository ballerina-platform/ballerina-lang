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

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.lookup.LookupElement;
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
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import org.antlr.jetbrains.adaptor.psi.ScopeNode;
import org.antlr.jetbrains.adaptor.xpath.XPath;
import org.ballerinalang.plugins.idea.BallerinaFileType;
import org.ballerinalang.plugins.idea.BallerinaLanguage;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.completion.AutoImportInsertHandler;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.AliasNode;
import org.ballerinalang.plugins.idea.psi.AnnotationAttachmentNode;
import org.ballerinalang.plugins.idea.psi.AnnotationDefinitionNode;
import org.ballerinalang.plugins.idea.psi.AssignmentStatementNode;
import org.ballerinalang.plugins.idea.psi.AttachmentPointNode;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.CodeBlockParameterNode;
import org.ballerinalang.plugins.idea.psi.ConnectorDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ConstantDefinitionNode;
import org.ballerinalang.plugins.idea.psi.DefinitionNode;
import org.ballerinalang.plugins.idea.psi.ExpressionVariableDefinitionStatementNode;
import org.ballerinalang.plugins.idea.psi.FieldDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FullyQualifiedPackageNameNode;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.GlobalVariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.NamespaceDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.QuotedLiteralString;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ServiceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TransformStatementNode;
import org.ballerinalang.plugins.idea.psi.TypeMapperNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceListNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceNode;
import org.ballerinalang.plugins.idea.psi.scopes.CodeBlockScope;
import org.ballerinalang.plugins.idea.psi.scopes.LowerLevelDefinition;
import org.ballerinalang.plugins.idea.psi.scopes.ParameterContainer;
import org.ballerinalang.plugins.idea.psi.scopes.TopLevelDefinition;
import org.ballerinalang.plugins.idea.psi.scopes.VariableContainer;
import org.ballerinalang.plugins.idea.util.BallerinaUtil;
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

    private static final String ANNOTATION_DEFINITION = "//annotationDefinition/Identifier";
    private static final String CONSTANT_DEFINITION = "//constantDefinition/Identifier";
    private static final String GLOBAL_VARIABLE_DEFINITION = "//globalVariableDefinition/Identifier";
    private static final String FUNCTION_DEFINITION = "//functionDefinition/Identifier";
    private static final String CONNECTOR_DEFINITION = "//connectorDefinition/Identifier";
    private static final String ACTION_DEFINITION = "//actionDefinition/Identifier";
    private static final String STRUCT_DEFINITION = "//structDefinition/Identifier";

    private BallerinaPsiImplUtil() {

    }

    /**
     * Resolves a package name to matching directories.
     *
     * @param identifier the package name identifier which we need to resolve to the directory
     * @return resolved directory
     */
    @NotNull
    public static List<PsiDirectory> resolveDirectory(@NotNull IdentifierPSINode identifier) {
        List<PsiDirectory> results = new LinkedList<>();
        PsiElement parent;
        PsiElement tempParent = identifier.getParent();
        if (tempParent == null) {
            return results;
        }
        PsiElement superParent = tempParent.getParent();
        AliasNode aliasNode = PsiTreeUtil.getParentOfType(superParent, AliasNode.class);

        if (aliasNode != null) {
            PsiElement temp = superParent;
            while (temp != null && !(temp instanceof FullyQualifiedPackageNameNode)) {
                temp = temp.getPrevSibling();
            }
            if (temp != null) {
                parent = temp.getLastChild();
            } else {
                return results;
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

        Project project = identifier.getProject();
        PsiFile containingFile = identifier.getContainingFile();
        PsiFile originalFile = containingFile.getOriginalFile();

        VirtualFile virtualFile = originalFile.getVirtualFile();
        if (virtualFile == null) {
            return results;
        }
        Module module = ModuleUtilCore.findModuleForFile(virtualFile, project);
        if (module == null) {
            return results;
        }

        results.addAll(findAllMatchingPackages(module, packages));
        return results;
    }

    @NotNull
    private static List<PsiDirectory> findAllMatchingPackages(@NotNull Module module,
                                                              @NotNull List<PsiElement> packages) {
        Project project = module.getProject();
        List<PsiDirectory> results = new ArrayList<>();

        // Get all matching packages in the current module.
        VirtualFile[] contentRoots = ModuleRootManager.getInstance(module).getSourceRoots();
        results.addAll(getMatchingPackagesFromContentRoots(contentRoots, packages, project));

        // Get all matching packages in the dependency modules.
        Module[] dependencies = ModuleRootManager.getInstance(module).getDependencies();
        for (Module dependency : dependencies) {
            contentRoots = ModuleRootManager.getInstance(dependency).getSourceRoots();
            results.addAll(getMatchingPackagesFromContentRoots(contentRoots, packages, project));
        }

        // Get all matching packages from the SDK.
        results.addAll(getMatchingPackagesFromSDK(project, module, packages));
        return results;
    }

    @NotNull
    private static List<PsiDirectory> getMatchingPackagesFromContentRoots(@NotNull VirtualFile[] contentRoots,
                                                                          @NotNull List<PsiElement> packages,
                                                                          @NotNull Project project) {
        List<PsiDirectory> results = new ArrayList<>();
        for (VirtualFile contentRoot : contentRoots) {
            // Get any matching directory from the content root.
            VirtualFile match = getMatchingDirectory(contentRoot, packages);
            // If there is a match, add it to the results.
            if (match != null) {
                results.add(PsiManager.getInstance(project).findDirectory(match));
            }
        }
        return results;
    }

    @NotNull
    private static List<PsiDirectory> getMatchingPackagesFromSDK(@NotNull Project project, @NotNull Module module,
                                                                 @NotNull List<PsiElement> packages) {
        List<PsiDirectory> results = new ArrayList<>();

        // First we check the sources of module SDK.
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
        // If the results are not empty at this moment, that means that the matches are found in the module SDK. We
        // return that result.
        if (!results.isEmpty()) {
            return results;
        }

        // Then we check the sources of project SDK.
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
        return results;
    }

    /**
     * Returns a matching directory to the given package structure, starting from the given root.
     *
     * @param root     current root directory we are checking
     * @param packages list of package name elements used to get the matching directory from the given root
     * @return the matching directory
     */
    @Nullable
    private static VirtualFile getMatchingDirectory(@NotNull VirtualFile root, @NotNull List<PsiElement> packages) {
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
            return getAllConnectorsFromPackage((PsiDirectory) element);
        }
        PsiElement parent = element.getParent();
        if (parent != null) {
            return getAllConnectorsFromPackage((PsiDirectory) parent);
        }
        return new LinkedList<>();
    }

    @NotNull
    public static List<PsiElement> getAllConnectorsFromPackage(PsiDirectory packageElement) {
        return getAllMatchingElementsFromPackage(packageElement, CONNECTOR_DEFINITION);
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
    public static List<PsiElement> getImportedPackages(@NotNull PsiFile file) {
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
                    FullyQualifiedPackageNameNode.class);
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

    /**
     * Returns all actions/native actions defined in the given ConnectorDefinitionNode.
     *
     * @param connectorDefinitionNode PsiElement which represent a Connector Definition
     * @return List of all actions/native actions defined in the given ConnectorDefinitionNode.
     */
    @NotNull
    public static List<PsiElement> getAllActionsFromAConnector(@NotNull PsiElement connectorDefinitionNode) {
        List<PsiElement> results = new ArrayList<>();
        // Todo - use PsiTreeUtil
        Collection<? extends PsiElement> allActions = XPath.findAll(BallerinaLanguage.INSTANCE, connectorDefinitionNode,
                ACTION_DEFINITION);
        results.addAll(allActions);
        return results;
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
            FullyQualifiedPackageNameNode fullyQualifiedPackageNameNode = PsiTreeUtil.findChildOfType
                    (importDeclarationNode, FullyQualifiedPackageNameNode.class);
            AliasNode aliasNode = PsiTreeUtil.findChildOfType(importDeclarationNode, AliasNode.class);
            if (aliasNode != null) {
                if (fullyQualifiedPackageNameNode != null) {
                    // Key is the alias name node text. Value is the package path node text.
                    // Eg:  import ballerina.utils as builtin;
                    // Map: builtin -> ballerina.utils
                    results.put(aliasNode.getText(), fullyQualifiedPackageNameNode.getText());
                }
            } else {
                if (fullyQualifiedPackageNameNode != null) {
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
                    results.put(lastNode.getText(), fullyQualifiedPackageNameNode.getText());
                }
            }
        }
        return results;
    }

    // Todo - change return type
    @Nullable
    public static StructDefinitionNode resolveStructFromDefinitionNode(@NotNull PsiElement definitionNode) {
        TypeNameNode typeNameNode = PsiTreeUtil.findChildOfType(definitionNode, TypeNameNode.class);
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
        return ((StructDefinitionNode) resolvedDefElement.getParent());
    }

    public static StructDefinitionNode resolveField(@NotNull FieldDefinitionNode fieldDefinitionNode) {

        TypeNameNode typeNameNode = PsiTreeUtil.getChildOfType(fieldDefinitionNode, TypeNameNode.class);
        if (typeNameNode == null) {
            return null;
        }
        PsiReference nameReference = typeNameNode.findReferenceAt(typeNameNode.getTextLength());
        if (nameReference == null) {
            return null;
        }
        PsiElement resolvedElement = nameReference.resolve();
        if (resolvedElement == null) {
            return null;
        }
        if (resolvedElement.getParent() instanceof StructDefinitionNode) {
            return ((StructDefinitionNode) resolvedElement.getParent());
        }
        return null;
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

    @NotNull
    public static List<PsiDirectory> getAllUnImportedPackages(@NotNull PsiFile currentFile) {
        List<PsiDirectory> results = new LinkedList<>();

        // Get all imported packages in the current file.
        Map<String, String> importsMap = BallerinaPsiImplUtil.getAllImportsInAFile(currentFile);
        // Get all packages in the resolvable scopes (project and libraries).
        List<PsiDirectory> directories =
                BallerinaPsiImplUtil.getAllPackagesInResolvableScopes(currentFile.getProject());
        // Iterate through all available  packages.
        for (PsiDirectory directory : directories) {
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
            } else if (importsMap.containsValue(suggestedImportPath)) {
                // This means we have already imported this package. This will be suggested by {@code
                // addAllImportedPackagesAsLookups}. So no need to add it as a lookup element.
                continue;
            }

            results.add(directory);
        }
        return results;
    }

    /**
     * Returns all local variables in provided scope and all parent contexts.
     *
     * @param scope
     * @param caretOffset
     * @return
     */
    @NotNull
    public static List<PsiElement> getAllLocalVariablesInResolvableScope(@NotNull ScopeNode scope, int caretOffset) {
        List<PsiElement> results = new LinkedList<>();
        if (scope instanceof VariableContainer || scope instanceof CodeBlockScope) {
            results.addAll(getAllLocalVariablesInScope(scope, caretOffset));
            ScopeNode context = scope.getContext();
            if (context != null) {
                results.addAll(getAllLocalVariablesInResolvableScope(context, caretOffset));
            }
        } else if (scope instanceof ParameterContainer || scope instanceof LowerLevelDefinition) {
            ScopeNode context = scope.getContext();
            if (context != null) {
                results.addAll(getAllLocalVariablesInResolvableScope(context, caretOffset));
            }
        }
        return results;
    }

    /**
     * Returns all local variables in the provided scope.
     *
     * @param scope
     * @param caretOffset
     * @return
     */
    @NotNull
    public static List<PsiElement> getAllLocalVariablesInScope(@NotNull ScopeNode scope, int caretOffset) {
        List<PsiElement> results = new LinkedList<>();
        Collection<VariableDefinitionNode> variableDefinitionNodes = PsiTreeUtil.findChildrenOfType(scope,
                VariableDefinitionNode.class);
        for (VariableDefinitionNode variableDefinitionNode : variableDefinitionNodes) {
            if (isValidVariable(variableDefinitionNode, scope, caretOffset)) {
                PsiElement identifier = variableDefinitionNode.getNameIdentifier();
                results.add(identifier);
            }
        }

        if (scope instanceof TransformStatementNode) {
            Collection<ExpressionVariableDefinitionStatementNode> nodes = PsiTreeUtil.findChildrenOfType(scope,
                    ExpressionVariableDefinitionStatementNode.class);
            for (ExpressionVariableDefinitionStatementNode node : nodes) {
                ScopeNode closestScope = PsiTreeUtil.getParentOfType(node, TransformStatementNode.class);
                if (closestScope == null || !closestScope.equals(scope)) {
                    continue;
                }

                PsiElement identifier = node.getNameIdentifier();
                results.add(identifier);
            }
        }

        Collection<AssignmentStatementNode> assignmentStatementNodes = PsiTreeUtil.findChildrenOfType(scope,
                AssignmentStatementNode.class);
        for (AssignmentStatementNode assignmentStatementNode : assignmentStatementNodes) {
            if (isValidVarAssignment(assignmentStatementNode, scope, caretOffset)) {
                results.addAll(getVariablesFromVarAssignment(assignmentStatementNode));
            }
        }
        return results;
    }

    @NotNull
    public static List<PsiElement> getAllXmlNamespacesInResolvableScope(@NotNull ScopeNode scope, int caretOffset) {
        List<PsiElement> results = new LinkedList<>();
        if (scope instanceof VariableContainer || scope instanceof CodeBlockScope || scope instanceof BallerinaFile) {
            results.addAll(getAllXmlNamespacesInScope(scope, caretOffset));
            ScopeNode context = scope.getContext();
            if (context != null) {
                results.addAll(getAllXmlNamespacesInResolvableScope(context, caretOffset));
            }
        } else if (scope instanceof ParameterContainer || scope instanceof TopLevelDefinition
                || scope instanceof LowerLevelDefinition) {
            ScopeNode context = scope.getContext();
            if (context != null) {
                results.addAll(getAllXmlNamespacesInResolvableScope(context, caretOffset));
            }
        }

        if (scope instanceof BallerinaFile) {
            PsiFile originalFile = ((BallerinaFile) scope).getOriginalFile();
            PsiDirectory containingPackage = originalFile.getParent();
            if (containingPackage == null) {
                return results;
            }
            PsiFile[] files = containingPackage.getFiles();
            for (PsiFile file : files) {
                // Do't check the current file again.
                if (file.equals(originalFile)) {
                    continue;
                }
                Collection<NamespaceDeclarationNode> namespaceDeclarationNodes = PsiTreeUtil.findChildrenOfType(file,
                        NamespaceDeclarationNode.class);
                for (NamespaceDeclarationNode namespaceDeclarationNode : namespaceDeclarationNodes) {
                    PsiElement identifier = namespaceDeclarationNode.getNameIdentifier();
                    if (identifier != null) {
                        results.add(identifier);
                    }
                }
            }
        }
        return results;
    }

    @NotNull
    public static List<PsiElement> getAllXmlNamespacesInScope(@NotNull ScopeNode scope, int caretOffset) {
        List<PsiElement> results = new LinkedList<>();
        Collection<NamespaceDeclarationNode> namespaceDeclarationNodes = PsiTreeUtil.findChildrenOfType(scope,
                NamespaceDeclarationNode.class);
        for (NamespaceDeclarationNode namespaceDeclarationNode : namespaceDeclarationNodes) {
            PsiElement identifier = namespaceDeclarationNode.getNameIdentifier();
            if (identifier != null && identifier.getTextOffset() < caretOffset) {
                results.add(identifier);
            }
        }
        return results;
    }

    private static boolean isValidVariable(@NotNull VariableDefinitionNode variableDefinitionNode,
                                           @NotNull ScopeNode scope, int caretOffset) {
        PsiElement elementAtCaret = scope.getContainingFile().findElementAt(caretOffset);
        if (elementAtCaret == null) {
            return false;
        }

        ScopeNode closestScope = PsiTreeUtil.getParentOfType(variableDefinitionNode, ScopeNode.class);
        if (closestScope == null) {
            return false;
        }
        if (!closestScope.equals(scope)) {
            return false;
        }

        PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(elementAtCaret);
        // Todo - temp fix. Might need to add {, }, etc.
        // If previous leaf is ';', that means we are in a new statement. Otherwise we are not in a new
        // statement, so there might be other elements before as well.
        if (prevVisibleLeaf == null || ";".equals(prevVisibleLeaf.getText())) {
            // If we are in a new statement,
            PsiElement identifier = variableDefinitionNode.getNameIdentifier();
            if (identifier != null && identifier.getTextOffset() < caretOffset) {
                return true;
            }
            return false;
        }

        // Check previous elements.
        PsiElement prevVisibleLeafParent = PsiTreeUtil.getParentOfType(prevVisibleLeaf,
                VariableDefinitionNode.class);
        if (prevVisibleLeafParent != null) {
            PsiElement identifier = variableDefinitionNode.getNameIdentifier();
            if (identifier != null && identifier.getTextOffset() < caretOffset
                    && !variableDefinitionNode.equals(prevVisibleLeafParent)) {
                return true;
            }
        } else {
            PsiElement identifier = variableDefinitionNode.getNameIdentifier();
            if (identifier != null && identifier.getTextOffset() < caretOffset) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidVarAssignment(@NotNull AssignmentStatementNode assignmentStatementNode,
                                                @NotNull ScopeNode scope, int caretOffset) {
        PsiElement elementAtCaret = scope.getContainingFile().findElementAt(caretOffset);
        if (elementAtCaret == null) {
            return false;
        }
        // Todo - temp fix. Might need to add {, }, etc.
        PsiElement prevVisibleLeaf = PsiTreeUtil.prevVisibleLeaf(elementAtCaret);
        if (prevVisibleLeaf != null && !";".equals(prevVisibleLeaf.getText())) {
            AssignmentStatementNode parent = PsiTreeUtil.getParentOfType(prevVisibleLeaf,
                    AssignmentStatementNode.class);
            if (parent != null && assignmentStatementNode.equals(parent)) {
                return false;
            }
        }
        if (!isVarAssignmentStatement(assignmentStatementNode)) {
            return false;
        }
        return assignmentStatementNode.getTextRange().getEndOffset() < caretOffset;
    }

    public static boolean isVarAssignmentStatement(@NotNull AssignmentStatementNode assignmentStatementNode) {
        PsiElement firstChild = assignmentStatementNode.getFirstChild();
        if (!(firstChild instanceof LeafPsiElement)) {
            return false;
        }
        IElementType elementType = ((LeafPsiElement) firstChild).getElementType();
        return elementType == BallerinaTypes.VAR;
    }

    @NotNull
    public static List<IdentifierPSINode> getVariablesFromVarAssignment(@NotNull AssignmentStatementNode
                                                                                 assignmentStatementNode) {
        List<IdentifierPSINode> results = new LinkedList<>();
        VariableReferenceListNode variableReferenceListNode = PsiTreeUtil.getChildOfType(assignmentStatementNode,
                VariableReferenceListNode.class);
        if (variableReferenceListNode == null) {
            return results;
        }
        VariableReferenceNode[] variableReferenceNodes = PsiTreeUtil.getChildrenOfType(variableReferenceListNode,
                VariableReferenceNode.class);
        if (variableReferenceNodes == null) {
            return results;
        }
        for (VariableReferenceNode variableReferenceNode : variableReferenceNodes) {

            IdentifierPSINode identifier = PsiTreeUtil.findChildOfType(variableReferenceNode, IdentifierPSINode.class);
            if (identifier != null) {
                results.add(identifier);
            }
        }
        return results;
    }

    @NotNull
    public static List<PsiElement> getAllParametersInResolvableScope(@NotNull ScopeNode scope, int caretOffset) {
        List<PsiElement> results = new LinkedList<>();
        if (scope instanceof VariableContainer || scope instanceof CodeBlockScope) {
            ScopeNode context = scope.getContext();
            if (context != null) {
                results.addAll(getAllParametersInResolvableScope(context, caretOffset));
            }
        } else if (scope instanceof ParameterContainer) {
            results.addAll(getAllParametersInScope(scope, caretOffset));
            ScopeNode context = scope.getContext();
            if (context != null) {
                results.addAll(getAllParametersInResolvableScope(context, caretOffset));
            }
        }
        return results;
    }

    @NotNull
    private static List<PsiElement> getAllParametersInScope(@NotNull ScopeNode scope, int caretOffset) {
        List<PsiElement> results = new LinkedList<>();
        Collection<ParameterNode> parameterNodes = PsiTreeUtil.findChildrenOfType(scope,
                ParameterNode.class);
        for (ParameterNode parameter : parameterNodes) {
            PsiElement identifier = parameter.getNameIdentifier();
            if (identifier != null) {
                results.add(identifier);
            }
        }

        Collection<CodeBlockParameterNode> codeBlockParameterNodes = PsiTreeUtil.findChildrenOfType(scope,
                CodeBlockParameterNode.class);
        for (CodeBlockParameterNode parameter : codeBlockParameterNodes) {

            PsiElement elementAtCaret = scope.getContainingFile().findElementAt(caretOffset);
            if (elementAtCaret == null) {
                return results;
            }

            if (parameter.getTextRange().getEndOffset() >= caretOffset) {
                return results;
            }

            ScopeNode closestScope = PsiTreeUtil.getParentOfType(parameter, ScopeNode.class);
            if (closestScope == null || !closestScope.equals(scope)) {
                continue;
            }
            PsiElement identifier = parameter.getNameIdentifier();
            if (identifier != null) {
                results.add(identifier);
            }
        }

        return results;
    }

    @NotNull
    public static List<PsiElement> getAllGlobalVariablesInResolvableScope(@NotNull ScopeNode scope) {
        List<PsiElement> results = new LinkedList<>();
        if (scope instanceof VariableContainer || scope instanceof ParameterContainer || scope instanceof CodeBlockScope
                || scope instanceof TopLevelDefinition || scope instanceof LowerLevelDefinition) {
            ScopeNode context = scope.getContext();
            if (context != null) {
                results.addAll(getAllGlobalVariablesInResolvableScope(context));
            }
        } else if (scope instanceof BallerinaFile) {
            results.addAll(getAllGlobalVariablesInScope(scope));
        }
        return results;
    }

    @NotNull
    public static List<PsiElement> getAllGlobalVariablesInScope(@NotNull ScopeNode scope) {
        List<PsiElement> results = new LinkedList<>();
        Collection<GlobalVariableDefinitionNode> variableDefinitionNodes = PsiTreeUtil.findChildrenOfType(scope,
                GlobalVariableDefinitionNode.class);
        for (GlobalVariableDefinitionNode variableDefinitionNode : variableDefinitionNodes) {
            PsiElement identifier = variableDefinitionNode.getNameIdentifier();
            if (identifier != null) {
                results.add(identifier);
            }
        }
        PsiFile originalFile = ((BallerinaFile) scope).getOriginalFile();
        PsiDirectory containingPackage = originalFile.getParent();
        if (containingPackage == null) {
            return results;
        }
        PsiFile[] files = containingPackage.getFiles();
        for (PsiFile file : files) {
            // Do't check the current file again.
            if (file.equals(originalFile)) {
                continue;
            }
            variableDefinitionNodes = PsiTreeUtil.findChildrenOfType(file, GlobalVariableDefinitionNode.class);
            for (GlobalVariableDefinitionNode variableDefinitionNode : variableDefinitionNodes) {
                PsiElement identifier = variableDefinitionNode.getNameIdentifier();
                if (identifier != null) {
                    results.add(identifier);
                }
            }
        }
        return results;
    }

    @NotNull
    public static List<PsiElement> getAllConstantsInResolvableScope(@NotNull ScopeNode scope) {
        List<PsiElement> results = new LinkedList<>();
        if (scope instanceof VariableContainer || scope instanceof ParameterContainer || scope instanceof CodeBlockScope
                || scope instanceof TopLevelDefinition || scope instanceof LowerLevelDefinition) {
            ScopeNode context = scope.getContext();
            if (context != null) {
                results.addAll(getAllConstantsInResolvableScope(context));
            }
        } else if (scope instanceof BallerinaFile) {
            results.addAll(getAllConstantsInScope(scope));
        }
        return results;
    }

    @NotNull
    public static List<PsiElement> getAllConstantsInScope(@NotNull ScopeNode scope) {
        List<PsiElement> results = new LinkedList<>();
        Collection<ConstantDefinitionNode> variableDefinitionNodes = PsiTreeUtil.findChildrenOfType(scope,
                ConstantDefinitionNode.class);
        for (ConstantDefinitionNode variableDefinitionNode : variableDefinitionNodes) {
            PsiElement identifier = variableDefinitionNode.getNameIdentifier();
            if (identifier != null) {
                results.add(identifier);
            }
        }
        PsiFile originalFile = ((BallerinaFile) scope).getOriginalFile();
        PsiDirectory containingPackage = originalFile.getParent();
        if (containingPackage == null) {
            return results;
        }
        PsiFile[] files = containingPackage.getFiles();
        for (PsiFile file : files) {
            // Do't check the current file again.
            if (file.equals(originalFile)) {
                continue;
            }

            variableDefinitionNodes = PsiTreeUtil.findChildrenOfType(file, ConstantDefinitionNode.class);
            for (ConstantDefinitionNode variableDefinitionNode : variableDefinitionNodes) {
                PsiElement identifier = variableDefinitionNode.getNameIdentifier();
                if (identifier != null) {
                    results.add(identifier);
                }
            }
        }
        return results;
    }

    @Nullable
    public static <T extends PsiElement> PsiElement resolveReference(@NotNull Collection<T> definitionNodes,
                                                                     @NotNull IdentifierPSINode referenceIdentifier) {
        for (T definitionNode : definitionNodes) {
            IdentifierPSINode fieldName = PsiTreeUtil.getChildOfType(definitionNode, IdentifierPSINode.class);
            if (fieldName == null) {
                continue;
            }
            String text = fieldName.getText();
            if (text.equals(referenceIdentifier.getText())) {
                return fieldName;
            }
        }
        return null;
    }


    @NotNull
    public static List<LookupElement> getPackagesAsLookups(@NotNull PsiFile file,
                                                           boolean withImportedPackages,
                                                           @Nullable InsertHandler<LookupElement> importedPackageIH,
                                                           boolean withUnImportedPackages,
                                                           @Nullable InsertHandler<LookupElement> unImportedPackageIH) {
        List<LookupElement> results = new LinkedList<>();
        List<String> importedPackageNames = new LinkedList<>();
        if (withImportedPackages) {
            List<PsiElement> importedPackages = BallerinaPsiImplUtil.getImportedPackages(file);
            for (PsiElement importedPackage : importedPackages) {
                PsiReference reference = importedPackage.findReferenceAt(0);
                if (reference == null) {
                    continue;
                }
                PsiElement resolvedElement = reference.resolve();
                if (resolvedElement == null) {
                    continue;
                }
                PsiDirectory resolvedPackage = (PsiDirectory) resolvedElement;
                LookupElement lookupElement = BallerinaCompletionUtils.createPackageLookupElement(resolvedPackage,
                        importedPackage.getText(), importedPackageIH);
                results.add(lookupElement);
                importedPackageNames.add(importedPackage.getText());
            }
        }
        if (withUnImportedPackages) {
            List<PsiDirectory> unImportedPackages = BallerinaPsiImplUtil.getAllUnImportedPackages(file);
            for (PsiDirectory unImportedPackage : unImportedPackages) {
                LookupElement lookupElement;
                if (importedPackageNames.contains(unImportedPackage.getName())) {
                    lookupElement = BallerinaCompletionUtils.createPackageLookupElement(unImportedPackage,
                            unImportedPackage.getName(), AutoImportInsertHandler.INSTANCE_WITH_ALIAS_WITH_POPUP);
                } else {
                    lookupElement = BallerinaCompletionUtils.createPackageLookupElement(unImportedPackage,
                            unImportedPackage.getName(), unImportedPackageIH);
                }
                results.add(lookupElement);
            }
        }
        return results;
    }

    @Nullable
    public static PsiElement resolveElementInPackage(@NotNull PsiDirectory aPackage,
                                                     @NotNull IdentifierPSINode identifier, boolean matchFunctions,
                                                     boolean matchConnectors, boolean matchStructs,
                                                     boolean matchGlobalVariables, boolean matchConstants) {
        if (matchFunctions) {
            List<PsiElement> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(aPackage);
            for (PsiElement function : functions) {
                if (identifier.getText().equals(function.getText())) {
                    return function;
                }
            }
        }
        if (matchConnectors) {
            List<PsiElement> connectors = BallerinaPsiImplUtil.getAllConnectorsFromPackage(aPackage);
            for (PsiElement connector : connectors) {
                if (identifier.getText().equals(connector.getText())) {
                    return connector;
                }
            }
        }
        if (matchStructs) {
            List<PsiElement> structs = BallerinaPsiImplUtil.getAllStructsFromPackage(aPackage);
            for (PsiElement struct : structs) {
                if (identifier.getText().equals(struct.getText())) {
                    return struct;
                }
            }
        }
        if (matchGlobalVariables) {
            List<PsiElement> globalVariables = BallerinaPsiImplUtil.getAllGlobalVariablesFromPackage(aPackage);
            for (PsiElement variable : globalVariables) {
                if (identifier.getText().equals(variable.getText())) {
                    return variable;
                }
            }
        }
        if (matchConstants) {
            List<PsiElement> constants = BallerinaPsiImplUtil.getAllConstantsFromPackage(aPackage);
            for (PsiElement constant : constants) {
                if (identifier.getText().equals(constant.getText())) {
                    return constant;
                }
            }
        }
        return null;
    }

    @Nullable
    public static PsiElement resolveElementInScope(@NotNull IdentifierPSINode identifier, boolean matchLocalVariables,
                                                   boolean matchParameters, boolean matchGlobalVariables,
                                                   boolean matchConstants) {
        ScopeNode scope = PsiTreeUtil.getParentOfType(identifier, CodeBlockScope.class, VariableContainer.class,
                TopLevelDefinition.class, LowerLevelDefinition.class);
        if (scope != null) {
            int caretOffset = identifier.getStartOffset();
            if (matchLocalVariables) {
                List<PsiElement> variables = BallerinaPsiImplUtil.getAllLocalVariablesInResolvableScope(scope,
                        caretOffset);
                for (PsiElement variable : variables) {
                    if (identifier.getText().equals(variable.getText())) {
                        return variable;
                    }
                }
            }
            if (matchParameters) {
                List<PsiElement> parameters = BallerinaPsiImplUtil.getAllParametersInResolvableScope(scope,
                        caretOffset);
                for (PsiElement parameter : parameters) {
                    if (identifier.getText().equals(parameter.getText())) {
                        return parameter;
                    }
                }
            }
            if (matchGlobalVariables) {
                List<PsiElement> globalVariables = BallerinaPsiImplUtil.getAllGlobalVariablesInResolvableScope(scope);
                for (PsiElement variable : globalVariables) {
                    if (identifier.getText().equals(variable.getText())) {
                        return variable;
                    }
                }
            }
            if (matchConstants) {
                List<PsiElement> constants = BallerinaPsiImplUtil.getAllConstantsInResolvableScope(scope);
                for (PsiElement constant : constants) {
                    if (identifier.getText().equals(constant.getText())) {
                        return constant;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns the attachment type by checking the nodes.
     *
     * @param identifier identifier node of the annotation attachment node
     * @return attachment type.
     */
    @Nullable
    public static String getAttachmentType(@NotNull IdentifierPSINode identifier) {
        PsiElement parent = identifier.getParent();
        PsiElement superParent = parent.getParent();
        PsiElement nextSibling = PsiTreeUtil.skipSiblingsForward(superParent, PsiWhiteSpace.class, PsiComment.class,
                AnnotationAttachmentNode.class);
        String type = null;
        if (nextSibling == null) {
            AnnotationAttachmentNode annotationAttachmentNode = PsiTreeUtil.getParentOfType(identifier,
                    AnnotationAttachmentNode.class);
            if (annotationAttachmentNode != null) {
                PsiElement definitionNode = annotationAttachmentNode.getParent();
                type = getAnnotationAttachmentType(definitionNode);
            }
        } else if (nextSibling instanceof DefinitionNode) {
            PsiElement[] children = nextSibling.getChildren();
            if (children.length != 0) {
                PsiElement definitionNode = children[0];
                type = getAnnotationAttachmentType(definitionNode);
            }
        } else if (nextSibling.getParent() instanceof ResourceDefinitionNode) {
            type = "resource";
        } else if (nextSibling instanceof ActionDefinitionNode
                || nextSibling.getParent() instanceof ActionDefinitionNode || parent instanceof ActionDefinitionNode) {
            type = "action";
        } else if (nextSibling.getParent() instanceof ParameterNode) {
            type = "parameter";
        }
        return type;
    }


    /**
     * Identify the annotation attachment type of the given definition node.
     *
     * @param definitionNode a definition node which we want to check
     * @return annotation attachment type.
     */
    private static String getAnnotationAttachmentType(PsiElement definitionNode) {
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

    @NotNull
    public static List<PsiElement> getAllAnnotationAttachmentsForType(@NotNull PsiDirectory packageElement,
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
                    // Replace the '<identifier?>' in the attachment point node. Otherwise, annotations for services
                    // will not be available.
                    if (type.equals(attachmentPointNode.getText().replaceAll("<\\w*>", ""))) {
                        results.add(annotationDefinition);
                        break;
                    }
                }
            }
        }
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

    public static boolean isValidVarVariable(@NotNull AssignmentStatementNode assignmentStatementNode,
                                             @NotNull IdentifierPSINode identifier) {
        List<IdentifierPSINode> variablesFromVarAssignment = getVariablesFromVarAssignment(assignmentStatementNode);
        for (IdentifierPSINode identifierNode : variablesFromVarAssignment) {
            if (identifier.equals(identifierNode)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static ConnectorDefinitionNode resolveConnectorFromVariableDefinitionNode(@NotNull PsiElement definitionNode) {
        TypeNameNode typeNameNode = PsiTreeUtil.findChildOfType(definitionNode, TypeNameNode.class);
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
        if (!(resolvedDefElement.getParent() instanceof ConnectorDefinitionNode)) {
            return null;
        }
        return ((ConnectorDefinitionNode) resolvedDefElement.getParent());
    }

    public static boolean isFunctionInvocation(@NotNull VariableReferenceNode variableReferenceNode) {
        PsiElement nextVisibleLeaf = PsiTreeUtil.nextVisibleLeaf(variableReferenceNode);
        if (nextVisibleLeaf != null && "(".equals(nextVisibleLeaf.getText())) {
            return true;
        }
        return false;
    }
}

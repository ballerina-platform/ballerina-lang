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
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtilCore;
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
import com.intellij.util.ArrayUtil;
import org.antlr.jetbrains.adaptor.psi.ScopeNode;
import org.ballerinalang.plugins.idea.BallerinaFileType;
import org.ballerinalang.plugins.idea.BallerinaTypes;
import org.ballerinalang.plugins.idea.completion.AutoImportInsertHandler;
import org.ballerinalang.plugins.idea.completion.BallerinaCompletionUtils;
import org.ballerinalang.plugins.idea.editor.BallerinaParameterInfoHandler;
import org.ballerinalang.plugins.idea.psi.ActionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.AliasNode;
import org.ballerinalang.plugins.idea.psi.AnnotationAttachmentNode;
import org.ballerinalang.plugins.idea.psi.AnnotationDefinitionNode;
import org.ballerinalang.plugins.idea.psi.AnonStructTypeNameNode;
import org.ballerinalang.plugins.idea.psi.AssignmentStatementNode;
import org.ballerinalang.plugins.idea.psi.AttachmentPointNode;
import org.ballerinalang.plugins.idea.psi.BallerinaFile;
import org.ballerinalang.plugins.idea.psi.BuiltInReferenceTypeNameNode;
import org.ballerinalang.plugins.idea.psi.CodeBlockParameterNode;
import org.ballerinalang.plugins.idea.psi.CompilationUnitNode;
import org.ballerinalang.plugins.idea.psi.ConnectorDeclarationStatementNode;
import org.ballerinalang.plugins.idea.psi.ConnectorDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ConnectorReferenceNode;
import org.ballerinalang.plugins.idea.psi.ConstantDefinitionNode;
import org.ballerinalang.plugins.idea.psi.DefinitionNode;
import org.ballerinalang.plugins.idea.psi.EndpointDeclarationNode;
import org.ballerinalang.plugins.idea.psi.EnumDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ExpressionNode;
import org.ballerinalang.plugins.idea.psi.ExpressionVariableDefinitionStatementNode;
import org.ballerinalang.plugins.idea.psi.FieldDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ForEachStatementNode;
import org.ballerinalang.plugins.idea.psi.FullyQualifiedPackageNameNode;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.FunctionInvocationNode;
import org.ballerinalang.plugins.idea.psi.FunctionReferenceNode;
import org.ballerinalang.plugins.idea.psi.FunctionTypeNameNode;
import org.ballerinalang.plugins.idea.psi.GlobalVariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.IdentifierPSINode;
import org.ballerinalang.plugins.idea.psi.ImportDeclarationNode;
import org.ballerinalang.plugins.idea.psi.InvocationNode;
import org.ballerinalang.plugins.idea.psi.NameReferenceNode;
import org.ballerinalang.plugins.idea.psi.NamespaceDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageDeclarationNode;
import org.ballerinalang.plugins.idea.psi.PackageNameNode;
import org.ballerinalang.plugins.idea.psi.ParameterListNode;
import org.ballerinalang.plugins.idea.psi.ParameterNode;
import org.ballerinalang.plugins.idea.psi.QuotedLiteralString;
import org.ballerinalang.plugins.idea.psi.ResourceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ReturnParametersNode;
import org.ballerinalang.plugins.idea.psi.ServiceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.StructDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TransformerDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TypeCastNode;
import org.ballerinalang.plugins.idea.psi.TypeConversionNode;
import org.ballerinalang.plugins.idea.psi.TypeListNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.ValueTypeNameNode;
import org.ballerinalang.plugins.idea.psi.VariableDefinitionNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceListNode;
import org.ballerinalang.plugins.idea.psi.VariableReferenceNode;
import org.ballerinalang.plugins.idea.psi.WorkerDeclarationNode;
import org.ballerinalang.plugins.idea.psi.scopes.CodeBlockScope;
import org.ballerinalang.plugins.idea.psi.scopes.LowerLevelDefinition;
import org.ballerinalang.plugins.idea.psi.scopes.ParameterContainer;
import org.ballerinalang.plugins.idea.psi.scopes.RestrictedScope;
import org.ballerinalang.plugins.idea.psi.scopes.TopLevelDefinition;
import org.ballerinalang.plugins.idea.psi.scopes.VariableContainer;
import org.ballerinalang.plugins.idea.sdk.BallerinaSdkService;
import org.ballerinalang.plugins.idea.util.BallerinaStringLiteralEscaper;
import org.ballerinalang.plugins.idea.util.BallerinaUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BallerinaPsiImplUtil {

    private static List<String> builtInDirectories = new LinkedList<>();

    static {
        builtInDirectories.add("/ballerina/builtin");
        builtInDirectories.add("/ballerina/builtin/core");
    }

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
        // Content roots can be empty in some small IDEs. In such cases, use project root directory as a content root.
        if (contentRoots.length == 0) {
            VirtualFile baseDir = module.getProject().getBaseDir();
            contentRoots = new VirtualFile[]{baseDir};
        }
        results.addAll(getMatchingPackagesFromContentRoots(contentRoots, packages, project));

        // Get all matching packages in the dependency modules.
        Module[] dependencies = ModuleRootManager.getInstance(module).getDependencies();
        for (Module dependency : dependencies) {
            contentRoots = ModuleRootManager.getInstance(dependency).getSourceRoots();
            List<PsiDirectory> matchingPackages = getMatchingPackagesFromContentRoots(contentRoots, packages, project);
            // Need to make sure not to add duplicate entries.
            for (PsiDirectory matchingPackage : matchingPackages) {
                if (!results.contains(matchingPackage)) {
                    results.add(matchingPackage);
                }
            }
        }

        // Get all matching packages from the SDK.
        List<PsiDirectory> matchingPackages = getMatchingPackagesFromSDK(project, module, packages);
        // Need to make sure not to add duplicate entries.
        for (PsiDirectory matchingPackage : matchingPackages) {
            if (!results.contains(matchingPackage)) {
                results.add(matchingPackage);
            }
        }
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

        String sdkHomePath = BallerinaSdkService.getInstance(project).getSdkHomePath(null);
        if (sdkHomePath == null) {
            return results;
        }
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(sdkHomePath + "/src");
        if (virtualFile == null) {
            return results;
        }
        VirtualFile match = getMatchingDirectory(virtualFile, packages);
        if (match != null) {
            results.add(PsiManager.getInstance(project).findDirectory(match));
        }

        // Find matching packages in BALLERINA_REPOSITORY.
        // Todo - This can be used to find all matching packages in SDK, BALLERINA_REPOSITORY, etc.
        OrderEntry[] entries = ModuleRootManager.getInstance(module).getOrderEntries();
        for (OrderEntry entry : entries) {
            for (VirtualFile file : entry.getFiles(OrderRootType.SOURCES)) {
                virtualFile = LocalFileSystem.getInstance().findFileByPath(file.getPath());
                if (virtualFile != null) {
                    match = getMatchingDirectory(virtualFile, packages);
                    if (match != null) {
                        results.add(PsiManager.getInstance(project).findDirectory(match));
                    }
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
    public static PsiDirectory[] suggestImportPackages(@NotNull PsiElement element) {
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

        String sdkHomePath = BallerinaSdkService.getInstance(project).getSdkHomePath(null);
        if (sdkHomePath == null) {
            return results.toArray(new PsiDirectory[results.size()]);
        }
        VirtualFile virtualFile = LocalFileSystem.getInstance().findFileByPath(sdkHomePath + "/src");
        if (virtualFile == null) {
            return results.toArray(new PsiDirectory[results.size()]);
        }

        List<VirtualFile> matches = suggestDirectory(virtualFile, packages);
        for (VirtualFile file : matches) {
            results.add(PsiManager.getInstance(project).findDirectory(file));
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
    private static List<VirtualFile> suggestDirectory(@NotNull VirtualFile root, @NotNull List<PsiElement> packages) {
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
    public static List<IdentifierPSINode> getAllConnectorsFromPackage(@NotNull PsiDirectory packageElement,
                                                                      boolean includePrivate, boolean includeBuiltIns) {
        return getAllMatchingElementsFromPackage(packageElement, ConnectorDefinitionNode.class, includePrivate,
                includeBuiltIns);
    }

    @NotNull
    public static List<IdentifierPSINode> getAllAnnotationsInPackage(@NotNull PsiDirectory packageElement,
                                                                     boolean includePrivate, boolean includeBuiltIns) {
        return getAllMatchingElementsFromPackage(packageElement, AnnotationDefinitionNode.class, includePrivate,
                includeBuiltIns);
    }

    @NotNull
    public static List<IdentifierPSINode> getAllStructsFromPackage(@NotNull PsiDirectory directory,
                                                                   boolean includePrivate, boolean includeBuiltIns) {
        return getAllMatchingElementsFromPackage(directory, StructDefinitionNode.class, includePrivate,
                includeBuiltIns);
    }

    @NotNull
    public static List<IdentifierPSINode> getAllEnumsFromPackage(@NotNull PsiDirectory directory,
                                                                 boolean includePrivate, boolean includeBuiltIns) {
        return getAllMatchingElementsFromPackage(directory, EnumDefinitionNode.class, includePrivate, includeBuiltIns);
    }

    @NotNull
    public static List<IdentifierPSINode> getAllFunctionsFromPackage(@NotNull PsiDirectory directory,
                                                                     boolean includePrivate, boolean includeBuiltIns) {
        return getAllMatchingElementsFromPackage(directory, FunctionDefinitionNode.class, includePrivate,
                includeBuiltIns);
    }

    @NotNull
    public static List<IdentifierPSINode> getAllConstantsFromPackage(PsiDirectory directory, boolean includePrivate,
                                                                     boolean includeBuiltIns) {
        return getAllMatchingElementsFromPackage(directory, ConstantDefinitionNode.class, includePrivate,
                includeBuiltIns);
    }

    @NotNull
    public static List<IdentifierPSINode> getAllGlobalVariablesFromPackage(@NotNull PsiDirectory directory,
                                                                           boolean includePrivate,
                                                                           boolean includeBuiltIns) {
        return getAllMatchingElementsFromPackage(directory, GlobalVariableDefinitionNode.class, includePrivate,
                includeBuiltIns);
    }

    @NotNull
    public static List<IdentifierPSINode> getAllTransformersFromPackage(@NotNull PsiDirectory directory,
                                                                        boolean includePrivate,
                                                                        boolean includeBuiltIns) {
        return getAllMatchingElementsFromPackage(directory, TransformerDefinitionNode.class, includePrivate,
                includeBuiltIns);
    }

    @NotNull
    public static List<PsiElement> getImportedPackages(@NotNull PsiFile file) {
        ArrayList<PsiElement> packages = new ArrayList<>();
        packages.addAll(getImportedPackagesInCurrentFile(file));
        packages.addAll(getPackagesImportedAsAliasInCurrentFile(file));
        return packages;
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
     * @return all functions in the given directory(package)
     */
    @NotNull
    private static <T extends PsiElement>
    List<IdentifierPSINode> getAllMatchingElementsFromPackage(@NotNull PsiDirectory directory, @NotNull Class<T> clazz,
                                                              boolean includePrivate, boolean includeBuiltIns) {
        Project project = directory.getProject();
        List<IdentifierPSINode> results = new ArrayList<>();
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
            results.addAll(getMatchingElementsFromAFile(psiFile, clazz, includePrivate));
        }

        if (includeBuiltIns) {
            // Add elements from built-in packages
            for (String builtInDirectory : builtInDirectories) {
                VirtualFile file = BallerinaPsiImplUtil.findFileInSDK(project, directory, builtInDirectory);
                if (file == null) {
                    return results;
                }
                VirtualFile[] builtInFiles = file.getChildren();
                for (VirtualFile builtInFile : builtInFiles) {
                    if (builtInFile.isDirectory() || !"bal".equals(builtInFile.getExtension())) {
                        continue;
                    }
                    // Find the file.
                    PsiFile psiFile = PsiManager.getInstance(project).findFile(builtInFile);
                    if (psiFile == null) {
                        return results;
                    }
                    results.addAll(getMatchingElementsFromAFile(psiFile, clazz, includePrivate));
                }
            }
        }
        return results;
    }

    public static <T extends PsiElement>
    List<IdentifierPSINode> getMatchingElementsFromAFile(@NotNull PsiFile psiFile, @NotNull Class<T> clazz,
                                                         boolean includePrivate) {
        List<IdentifierPSINode> results = new ArrayList<>();
        Collection<T> definitions = PsiTreeUtil.findChildrenOfType(psiFile, clazz);
        for (T definition : definitions) {
            PsiElement firstChild = definition.getFirstChild();
            if (!includePrivate) {
                if (firstChild instanceof LeafPsiElement) {
                    IElementType elementType = ((LeafPsiElement) firstChild).getElementType();
                    if (elementType != BallerinaTypes.PUBLIC) {
                        continue;
                    }
                } else {
                    // Global variables, etc. If the first element is not a LeafPsiElement, that means public keyword
                    // is not present.
                    continue;
                }
            }
            IdentifierPSINode identifier = PsiTreeUtil.getChildOfType(definition, IdentifierPSINode.class);
            results.add(identifier);
        }
        return results;
    }

    public static PsiFile findPsiFileInSDK(@NotNull Project project, @NotNull PsiElement element,
                                           @NotNull String path) {
        VirtualFile virtualFile = BallerinaPsiImplUtil.findFileInSDK(project, element, path);
        if (virtualFile == null) {
            return null;
        }
        return PsiManager.getInstance(project).findFile(virtualFile);
    }

    /**
     * Returns all actions/native actions defined in the given ConnectorDefinitionNode.
     *
     * @param connectorDefinitionNode PsiElement which represent a Connector Definition
     * @return List of all actions/native actions defined in the given ConnectorDefinitionNode.
     */
    @NotNull
    public static List<IdentifierPSINode> getAllActionsFromAConnector(@NotNull PsiElement connectorDefinitionNode) {
        List<IdentifierPSINode> results = new ArrayList<>();
        Collection<ActionDefinitionNode> actionDefinitionNodes = PsiTreeUtil.findChildrenOfType(connectorDefinitionNode,
                ActionDefinitionNode.class);
        for (ActionDefinitionNode definitionNode : actionDefinitionNodes) {
            IdentifierPSINode identifier = PsiTreeUtil.getChildOfType(definitionNode, IdentifierPSINode.class);
            results.add(identifier);
        }
        return results;
    }

    @NotNull
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
                addedNode = file.addBefore(importDeclaration, children[0]);
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

    @Nullable
    public static StructDefinitionNode resolveStructFromDefinitionNode(@NotNull PsiElement definitionNode) {
        if (definitionNode instanceof ConnectorDeclarationStatementNode) {
            NameReferenceNode nameReferenceNode = PsiTreeUtil.getChildOfType(definitionNode, NameReferenceNode.class);
            if (nameReferenceNode != null) {
                PsiReference reference = nameReferenceNode.findReferenceAt(nameReferenceNode.getTextLength());
                if (reference != null) {
                    PsiElement resolvedElement = reference.resolve();
                    if (resolvedElement != null && resolvedElement.getParent() instanceof StructDefinitionNode) {
                        return ((StructDefinitionNode) resolvedElement.getParent());
                    }
                }
            }
        }

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

    public static StructDefinitionNode resolveTypeNodeStruct(@NotNull PsiElement element) {
        TypeNameNode typeNameNode = PsiTreeUtil.getChildOfType(element, TypeNameNode.class);
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
    public static List<IdentifierPSINode> getAllLocalVariablesInResolvableScope(@NotNull ScopeNode scope,
                                                                                int caretOffset) {
        List<IdentifierPSINode> results = new LinkedList<>();
        if (scope instanceof VariableContainer || scope instanceof CodeBlockScope) {
            results.addAll(getAllLocalVariablesInScope(scope, caretOffset));
            ScopeNode context = scope.getContext();
            if (context != null && !(scope instanceof RestrictedScope)) {
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

    @NotNull
    public static List<IdentifierPSINode> getAllEndpointsInResolvableScope(@NotNull ScopeNode scope, int caretOffset) {
        List<IdentifierPSINode> results = new LinkedList<>();
        if (scope instanceof VariableContainer || scope instanceof CodeBlockScope ||
                scope instanceof TopLevelDefinition || scope instanceof LowerLevelDefinition) {
            results.addAll(getAllEndpointsInScope(scope, caretOffset));
            ScopeNode context = scope.getContext();
            if (context != null) {
                results.addAll(getAllEndpointsInResolvableScope(context, caretOffset));
            }
        }
        return results;
    }

    @NotNull
    private static List<IdentifierPSINode> getAllEndpointsInScope(@NotNull ScopeNode scope, int caretOffset) {
        List<IdentifierPSINode> results = new LinkedList<>();
        Collection<EndpointDeclarationNode> endpointDeclarationNodes = PsiTreeUtil.getChildrenOfTypeAsList(scope,
                EndpointDeclarationNode.class);
        for (EndpointDeclarationNode endpointDeclarationNode : endpointDeclarationNodes) {
            IdentifierPSINode identifier = PsiTreeUtil.getChildOfType(endpointDeclarationNode, IdentifierPSINode.class);
            if (identifier != null) {
                results.add((identifier));
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
    public static List<IdentifierPSINode> getAllLocalVariablesInScope(@NotNull ScopeNode scope, int caretOffset) {
        List<IdentifierPSINode> results = new LinkedList<>();
        Collection<VariableDefinitionNode> variableDefinitionNodes = PsiTreeUtil.findChildrenOfType(scope,
                VariableDefinitionNode.class);
        for (VariableDefinitionNode variableDefinitionNode : variableDefinitionNodes) {
            if (isValidVariable(variableDefinitionNode, scope, caretOffset)) {
                PsiElement identifier = variableDefinitionNode.getNameIdentifier();
                if (identifier != null && identifier instanceof IdentifierPSINode) {
                    results.add(((IdentifierPSINode) identifier));
                }
            }
        }

        if (scope instanceof TransformerDefinitionNode) {
            Collection<ExpressionVariableDefinitionStatementNode> nodes = PsiTreeUtil.findChildrenOfType(scope,
                    ExpressionVariableDefinitionStatementNode.class);
            for (ExpressionVariableDefinitionStatementNode node : nodes) {
                ScopeNode closestScope = PsiTreeUtil.getParentOfType(node, TransformerDefinitionNode.class);
                if (closestScope == null || !closestScope.equals(scope)) {
                    continue;
                }
                PsiElement identifier = node.getNameIdentifier();
                if (identifier != null && identifier instanceof IdentifierPSINode) {
                    results.add(((IdentifierPSINode) identifier));
                }
            }
        }

        if (scope instanceof ForEachStatementNode) {
            VariableReferenceListNode variableReferenceListNode = PsiTreeUtil.getChildOfType(scope,
                    VariableReferenceListNode.class);
            if (variableReferenceListNode != null) {
                List<VariableReferenceNode> variableReferenceNodes =
                        PsiTreeUtil.getChildrenOfTypeAsList(variableReferenceListNode, VariableReferenceNode.class);
                for (VariableReferenceNode variableReferenceNode : variableReferenceNodes) {
                    if (variableReferenceNode != null) {
                        IdentifierPSINode identifier = PsiTreeUtil.findChildOfType(variableReferenceNode,
                                IdentifierPSINode.class);
                        if (identifier != null) {
                            results.add(identifier);
                        }
                    }
                }
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

    @Nullable
    public static PsiElement getNamespaceDefinition(@NotNull IdentifierPSINode identifier) {
        ScopeNode scope = PsiTreeUtil.getParentOfType(identifier, CodeBlockScope.class, VariableContainer.class,
                TopLevelDefinition.class, LowerLevelDefinition.class);
        if (scope != null) {
            int caretOffset = identifier.getStartOffset();
            List<PsiElement> namespaces = BallerinaPsiImplUtil.getAllXmlNamespacesInResolvableScope(scope,
                    caretOffset);
            for (PsiElement namespace : namespaces) {
                if (namespace == null || namespace.getText().isEmpty()) {
                    continue;
                }
                if (namespace.getText().equals(identifier.getText())) {
                    return namespace;
                }
            }
        }
        return null;
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
            if (identifier != null && !"_".equals(identifier.getText())) {
                results.add(identifier);
            }
        }
        return results;
    }

    @NotNull
    public static List<IdentifierPSINode> getAllParametersInResolvableScope(@NotNull ScopeNode scope, int caretOffset) {
        List<IdentifierPSINode> results = new LinkedList<>();
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
    private static List<IdentifierPSINode> getAllParametersInScope(@NotNull ScopeNode scope, int caretOffset) {
        List<IdentifierPSINode> results = new LinkedList<>();
        Collection<ParameterNode> parameterNodes = PsiTreeUtil.findChildrenOfType(scope,
                ParameterNode.class);
        for (ParameterNode parameter : parameterNodes) {
            ScopeNode parentScope = PsiTreeUtil.getParentOfType(parameter, ScopeNode.class);
            if (!scope.equals(parentScope)) {
                continue;
            }
            PsiElement identifier = parameter.getNameIdentifier();
            if (identifier != null && identifier instanceof IdentifierPSINode) {
                results.add(((IdentifierPSINode) identifier));
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
            if (identifier != null && identifier instanceof IdentifierPSINode) {
                results.add(((IdentifierPSINode) identifier));
            }
        }

        return results;
    }

    @NotNull
    public static List<IdentifierPSINode> getAllGlobalVariablesInResolvableScope(@NotNull ScopeNode scope) {
        List<IdentifierPSINode> results = new LinkedList<>();
        if (scope instanceof VariableContainer || scope instanceof ParameterContainer || scope instanceof CodeBlockScope
                || scope instanceof TopLevelDefinition || scope instanceof LowerLevelDefinition) {
            ScopeNode context = scope.getContext();
            if (context != null) {
                results.addAll(getAllGlobalVariablesInResolvableScope(context));
            }
        } else if (scope instanceof BallerinaFile) {
            results.addAll(getAllGlobalVariablesInScope(scope, -1));
        }
        return results;
    }

    @NotNull
    public static List<IdentifierPSINode> getAllGlobalVariablesInResolvableScope(@NotNull ScopeNode scope,
                                                                                 int caretOffset) {
        List<IdentifierPSINode> results = new LinkedList<>();
        if (scope instanceof VariableContainer || scope instanceof ParameterContainer || scope instanceof CodeBlockScope
                || scope instanceof TopLevelDefinition || scope instanceof LowerLevelDefinition) {
            ScopeNode context = scope.getContext();
            if (context != null) {
                results.addAll(getAllGlobalVariablesInResolvableScope(context, caretOffset));
            }
        } else if (scope instanceof BallerinaFile) {
            results.addAll(getAllGlobalVariablesInScope(scope, caretOffset));
        }
        return results;
    }

    @NotNull
    public static List<IdentifierPSINode> getAllGlobalVariablesInScope(@NotNull ScopeNode scope, int caretOffset) {
        List<IdentifierPSINode> results = new LinkedList<>();
        Collection<GlobalVariableDefinitionNode> variableDefinitionNodes = PsiTreeUtil.findChildrenOfType(scope,
                GlobalVariableDefinitionNode.class);
        for (GlobalVariableDefinitionNode variableDefinitionNode : variableDefinitionNodes) {
            PsiElement identifier = variableDefinitionNode.getNameIdentifier();
            if (caretOffset != -1) {
                if (identifier == null || !(identifier instanceof IdentifierPSINode)
                        || identifier.getTextOffset() > caretOffset) {
                    continue;
                }
                PsiElement element = scope.findElementAt(caretOffset);
                if (element != null) {
                    GlobalVariableDefinitionNode definition = PsiTreeUtil.getParentOfType(element,
                            GlobalVariableDefinitionNode.class);
                    if (definition != null && definition.equals(variableDefinitionNode)) {
                        continue;
                    }
                }
            }
            results.add(((IdentifierPSINode) identifier));
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
                if (identifier != null && identifier instanceof IdentifierPSINode) {
                    results.add(((IdentifierPSINode) identifier));
                }
            }
        }
        return results;
    }

    @NotNull
    public static List<IdentifierPSINode> getAllConstantsInResolvableScope(@NotNull ScopeNode scope) {
        List<IdentifierPSINode> results = new LinkedList<>();
        if (scope instanceof VariableContainer || scope instanceof ParameterContainer || scope instanceof CodeBlockScope
                || scope instanceof TopLevelDefinition || scope instanceof LowerLevelDefinition) {
            ScopeNode context = scope.getContext();
            if (context != null) {
                results.addAll(getAllConstantsInResolvableScope(context));
            }
        } else if (scope instanceof BallerinaFile || scope instanceof CompilationUnitNode) {
            results.addAll(getAllConstantsInScope(scope, -1));
        }
        return results;
    }

    @NotNull
    public static List<IdentifierPSINode> getAllConstantsInResolvableScope(@NotNull ScopeNode scope, int caretOffset) {
        List<IdentifierPSINode> results = new LinkedList<>();
        if (scope instanceof VariableContainer || scope instanceof ParameterContainer || scope instanceof CodeBlockScope
                || scope instanceof TopLevelDefinition || scope instanceof LowerLevelDefinition) {
            ScopeNode context = scope.getContext();
            if (context != null) {
                results.addAll(getAllConstantsInResolvableScope(context, caretOffset));
            }
        } else if (scope instanceof BallerinaFile) {
            results.addAll(getAllConstantsInScope(scope, caretOffset));
        }
        return results;
    }

    @NotNull
    public static List<IdentifierPSINode> getAllConstantsInScope(@NotNull ScopeNode scope, int caretOffset) {
        List<IdentifierPSINode> results = new LinkedList<>();
        Collection<ConstantDefinitionNode> constantDefinitionNodes = PsiTreeUtil.findChildrenOfType(scope,
                ConstantDefinitionNode.class);
        for (ConstantDefinitionNode constantDefinitionNode : constantDefinitionNodes) {
            PsiElement identifier = constantDefinitionNode.getNameIdentifier();
            if (caretOffset != -1) {
                if (identifier == null || !(identifier instanceof IdentifierPSINode)
                        || identifier.getTextOffset() > caretOffset) {
                    continue;
                }
                PsiElement element = scope.findElementAt(caretOffset);
                if (element != null) {
                    ConstantDefinitionNode definition = PsiTreeUtil.getParentOfType(element,
                            ConstantDefinitionNode.class);
                    if (definition != null && definition.equals(constantDefinitionNode)) {
                        continue;
                    }
                }
            }
            results.add(((IdentifierPSINode) identifier));
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

            constantDefinitionNodes = PsiTreeUtil.findChildrenOfType(file, ConstantDefinitionNode.class);
            for (ConstantDefinitionNode variableDefinitionNode : constantDefinitionNodes) {
                PsiElement identifier = variableDefinitionNode.getNameIdentifier();
                if (identifier != null && identifier instanceof IdentifierPSINode) {
                    results.add(((IdentifierPSINode) identifier));
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
                PsiDirectory resolvedPackage;
                AliasNode aliasNode = PsiTreeUtil.getParentOfType(importedPackage, AliasNode.class);
                if (aliasNode != null) {
                    resolvedPackage = resolveAliasNode(aliasNode);
                } else {
                    PsiReference reference = importedPackage.findReferenceAt(0);
                    if (reference == null) {
                        continue;
                    }
                    PsiElement resolvedElement = reference.resolve();
                    if (resolvedElement == null) {
                        continue;
                    }
                    resolvedPackage = (PsiDirectory) resolvedElement;
                }
                if (resolvedPackage == null) {
                    continue;
                }
                LookupElement lookupElement =
                        BallerinaCompletionUtils.createImportedPackageLookupElement(resolvedPackage,
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
                    lookupElement = BallerinaCompletionUtils.createUnimportedPackageLookupElement(unImportedPackage,
                            unImportedPackage.getName(), AutoImportInsertHandler.INSTANCE_WITH_ALIAS_WITH_POPUP);
                } else {
                    lookupElement = BallerinaCompletionUtils.createUnimportedPackageLookupElement(unImportedPackage,
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
                                                     boolean matchConnectors, boolean matchStructs, boolean matchEnums,
                                                     boolean matchGlobalVariables, boolean matchConstants,
                                                     boolean includePrivate, boolean includeBuiltIns) {
        if (matchFunctions) {
            List<IdentifierPSINode> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(aPackage,
                    includePrivate, includeBuiltIns);
            for (PsiElement function : functions) {
                if (function == null) {
                    continue;
                }
                if (identifier.getText().equals(function.getText())) {
                    return function;
                }
            }
        }
        if (matchConnectors) {
            List<IdentifierPSINode> connectors = BallerinaPsiImplUtil.getAllConnectorsFromPackage(aPackage,
                    includePrivate, includeBuiltIns);
            for (PsiElement connector : connectors) {
                if (connector == null) {
                    continue;
                }
                if (identifier.getText().equals(connector.getText())) {
                    return connector;
                }
            }
        }
        if (matchStructs) {
            List<IdentifierPSINode> structs = BallerinaPsiImplUtil.getAllStructsFromPackage(aPackage, includePrivate,
                    includeBuiltIns);
            for (PsiElement struct : structs) {
                if (struct == null) {
                    continue;
                }
                if (identifier.getText().equals(struct.getText())) {
                    return struct;
                }
            }
        }
        if (matchEnums) {
            List<IdentifierPSINode> enums = BallerinaPsiImplUtil.getAllEnumsFromPackage(aPackage, includePrivate,
                    includeBuiltIns);
            for (PsiElement anEnum : enums) {
                if (anEnum == null) {
                    continue;
                }
                if (identifier.getText().equals(anEnum.getText())) {
                    return anEnum;
                }
            }
        }
        if (matchGlobalVariables) {
            List<IdentifierPSINode> globalVariables = BallerinaPsiImplUtil.getAllGlobalVariablesFromPackage(aPackage,
                    includePrivate, includeBuiltIns);
            for (PsiElement variable : globalVariables) {
                if (variable == null) {
                    continue;
                }
                if (identifier.getText().equals(variable.getText())) {
                    return variable;
                }
            }
        }
        if (matchConstants) {
            List<IdentifierPSINode> constants = BallerinaPsiImplUtil.getAllConstantsFromPackage(aPackage,
                    includePrivate, includeBuiltIns);
            for (PsiElement constant : constants) {
                if (constant == null) {
                    continue;
                }
                if (identifier.getText().equals(constant.getText())) {
                    return constant;
                }
            }
        }
        return null;
    }

    @Nullable
    public static PsiElement resolveFunctionInPackage(@NotNull IdentifierPSINode identifier,
                                                      boolean matchLocalVariables, boolean matchParameters,
                                                      boolean matchGlobalVariables) {
        PsiElement element = resolveElementInScope(identifier, matchLocalVariables, matchParameters,
                matchGlobalVariables, false, false);
        if (element == null) {
            return null;
        }
        if (isALambdaFunction(element)) {
            return element;
        }
        return null;
    }

    public static boolean isALambdaFunction(@NotNull PsiElement element) {
        BuiltInReferenceTypeNameNode builtInReferenceTypeNameNode = PsiTreeUtil.findChildOfType(element.getParent(),
                BuiltInReferenceTypeNameNode.class);
        if (builtInReferenceTypeNameNode == null) {
            return false;
        }
        FunctionTypeNameNode functionTypeNameNode = PsiTreeUtil.getChildOfType(builtInReferenceTypeNameNode,
                FunctionTypeNameNode.class);
        if (functionTypeNameNode == null) {
            return false;
        }
        return true;
    }

    @Nullable
    public static PsiElement resolveElementInScope(@NotNull IdentifierPSINode identifier, boolean matchLocalVariables,
                                                   boolean matchParameters, boolean matchGlobalVariables,
                                                   boolean matchConstants, boolean matchEndpoint) {
        ScopeNode scope = PsiTreeUtil.getParentOfType(identifier, CodeBlockScope.class, VariableContainer.class,
                TopLevelDefinition.class, LowerLevelDefinition.class);
        if (scope == null) {
            return null;
        }
        int caretOffset = identifier.getStartOffset();
        if (matchLocalVariables) {
            List<IdentifierPSINode> variables = BallerinaPsiImplUtil.getAllLocalVariablesInResolvableScope(scope,
                    caretOffset);
            PsiElement element = getMatchingElement(identifier, variables);
            if (element != null) {
                return element;
            }
        }
        if (matchEndpoint) {
            List<IdentifierPSINode> endpoints = BallerinaPsiImplUtil.getAllEndpointsInResolvableScope(scope,
                    caretOffset);
            PsiElement element = getMatchingElement(identifier, endpoints);
            if (element != null) {
                return element;
            }
        }
        if (matchParameters) {
            List<IdentifierPSINode> parameters = BallerinaPsiImplUtil.getAllParametersInResolvableScope(scope,
                    caretOffset);
            PsiElement element = getMatchingElement(identifier, parameters);
            if (element != null) {
                return element;
            }
        }
        if (matchGlobalVariables) {
            List<IdentifierPSINode> globalVariables =
                    BallerinaPsiImplUtil.getAllGlobalVariablesInResolvableScope(scope);
            PsiElement element = getMatchingElement(identifier, globalVariables);
            if (element != null) {
                return element;
            }
        }
        if (matchConstants) {
            List<IdentifierPSINode> constants = BallerinaPsiImplUtil.getAllConstantsInResolvableScope(scope);
            PsiElement element = getMatchingElement(identifier, constants);
            if (element != null) {
                return element;
            }
        }
        return null;
    }

    @Nullable
    private static PsiElement getMatchingElement(@NotNull IdentifierPSINode identifier,
                                                 @NotNull List<IdentifierPSINode> variables) {
        for (IdentifierPSINode variable : variables) {
            if (variable == null) {
                continue;
            }
            if (identifier.getText().equals(variable.getText())) {
                return variable;
            }
        }
        return null;
    }

    @NotNull
    public static List<WorkerDeclarationNode> getWorkerDeclarationsInScope(@NotNull ScopeNode scopeNode) {
        List<WorkerDeclarationNode> results = new LinkedList<>();
        WorkerDeclarationNode[] workerDeclarationNodes = PsiTreeUtil.getChildrenOfType(scopeNode,
                WorkerDeclarationNode.class);
        if (workerDeclarationNodes == null) {
            return results;
        }
        results.addAll(Arrays.asList(workerDeclarationNodes));
        return results;
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
    public static List<IdentifierPSINode> getAllAnnotationAttachmentsForType(@NotNull PsiDirectory packageElement,
                                                                             @NotNull String type,
                                                                             boolean includePrivate,
                                                                             boolean includeBuiltIns) {
        List<IdentifierPSINode> results = new ArrayList<>();
        // Todo - Do we need to add public to Annotations as well?
        List<IdentifierPSINode> annotationDefinitions = getAllMatchingElementsFromPackage(packageElement,
                AnnotationDefinitionNode.class, includePrivate, includeBuiltIns);
        if (annotationDefinitions.isEmpty()) {
            return results;
        }
        for (IdentifierPSINode annotationDefinition : annotationDefinitions) {
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
    public static ConnectorDefinitionNode resolveConnectorFromVariableDefinitionNode(@NotNull PsiElement
                                                                                             definitionNode) {

        NameReferenceNode nameReferenceNode = PsiTreeUtil.findChildOfType(definitionNode, NameReferenceNode.class);
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

    /**
     * Finds a file in project or module SDK.
     *
     * @param project a project element
     * @param element a PsiElement
     * @param path    relative file path in the SDK
     * @return {@code null} if the file cannot be found, otherwise returns the corresponding {@link VirtualFile}.
     */
    @Nullable
    public static VirtualFile findFileInSDK(@NotNull Project project, @NotNull PsiElement element,
                                            @NotNull String path) {
        // First we check the sources of module SDK.
        VirtualFile file = findFileInModuleSDK(element, path);
        if (file != null) {
            return file;
        }
        // Then we check the sources of project SDK.
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

    /**
     * Resolves the corresponding struct definition of the provided identifier node if possible.
     * Eg:
     * <pre>
     * function main (string[] args) {
     *     var person = getData();
     *     var person2 = person;
     *     var person3 = person2;
     *
     *     system:println(person3.<caret>);
     * }
     *
     * function getData () (Person) {
     *     Person person = {name:"Shan", age:26};
     *     return person;
     * }
     *
     * struct Person {
     *    string name;
     *    int age;
     * }
     * </pre>
     * <p>
     * From the above caret position, we should be able to get the type of the 'person3'. So we resolve recursively
     * until we find the struct definition. If the RHS has a variable, we resolve the definition of the variable and
     * call this method again. So in the first call of this function, 'resolvedElement' will be the
     * definition of 'person3'. Next, the definition of 'person2' and definition of 'person' after that. At that point,
     * we find a function invocation in the RHS. So we get the type from that.
     *
     * @param resolvedElement element which was resolved from the variable reference
     * @return corresponding {@link StructDefinitionNode} if the structName can be resolved, {@code null} otherwise.
     */
    @Nullable
    public static StructDefinitionNode findStructDefinition(@NotNull IdentifierPSINode resolvedElement) {
        // Since this method might get called recursively, it is better to check whether the process is cancelled in the
        // beginning.
        ProgressManager.checkCanceled();
        // Get the assignment statement parent node.
        AssignmentStatementNode assignmentStatementNode = PsiTreeUtil.getParentOfType(resolvedElement,
                AssignmentStatementNode.class);
        // If an assignment statement exists, it should be an assignment state with var type. That is because the
        // structName comes from resolving the reference to a definition and normal assignment statement does not
        // count as a definition and does not resolve to it.
        if (assignmentStatementNode != null) {
            return getStructDefinition(assignmentStatementNode, resolvedElement);
        }
        // If the resolved element is not in an AssignmentStatement, we check for VariableDefinition parent node.
        // If there is a VariableDefinition parent node, finding the type is quite straight forward.
        VariableDefinitionNode variableDefinitionNode = PsiTreeUtil.getParentOfType(resolvedElement,
                VariableDefinitionNode.class);
        if (variableDefinitionNode != null) {
            return resolveStructFromDefinitionNode(variableDefinitionNode);
        }

        ParameterNode parameterNode = PsiTreeUtil.getParentOfType(resolvedElement, ParameterNode.class);
        if (parameterNode != null) {
            return resolveStructFromDefinitionNode(parameterNode);
        }
        return null;
    }

    /**
     * Checks the RHS of the provided {@link AssignmentStatementNode} and returns the corresponding
     * {@link StructDefinitionNode}.
     *
     * @param assignmentStatementNode an assignment statement node
     * @param structReferenceNode     an identifier which is a var variable in the assignment node
     * @return {@code null} if cannot suggest a {@link StructDefinitionNode}, otherwise returns the corresponding
     * {@link StructDefinitionNode}.
     */
    @Nullable
    public static StructDefinitionNode getStructDefinition(@NotNull AssignmentStatementNode assignmentStatementNode,
                                                           @NotNull IdentifierPSINode structReferenceNode) {
        if (!BallerinaPsiImplUtil.isVarAssignmentStatement(assignmentStatementNode)) {
            return null;
        }
        ExpressionNode expressionNode = PsiTreeUtil.getChildOfType(assignmentStatementNode,
                ExpressionNode.class);
        if (expressionNode == null) {
            return null;
        }
        PsiElement expressionNodeFirstChild = expressionNode.getFirstChild();
        if (expressionNodeFirstChild instanceof VariableReferenceNode) {
            return getStructDefinition((VariableReferenceNode) expressionNodeFirstChild, assignmentStatementNode,
                    structReferenceNode);
        } else if (expressionNodeFirstChild instanceof TypeCastNode) {
            int index = getVariableIndexFromVarAssignment(assignmentStatementNode, structReferenceNode);
            if (index == 0) {
                return resolveTypeNodeStruct((expressionNodeFirstChild));
            } else if (index == 1) {
                return getErrorStruct(assignmentStatementNode, structReferenceNode, true, false);
            }
        } else if (expressionNodeFirstChild instanceof TypeConversionNode) {
            int index = getVariableIndexFromVarAssignment(assignmentStatementNode, structReferenceNode);
            if (index == 0) {
                return resolveTypeNodeStruct((expressionNodeFirstChild));
            } else if (index == 1) {
                return getErrorStruct(assignmentStatementNode, structReferenceNode, false, true);
            }
        }
        return null;
    }

    /**
     * Finds a {@link StructDefinitionNode} which corresponds to the provided {@link VariableReferenceNode}.
     *
     * @param variableReferenceNode   an variable reference node in the assignment statement node
     * @param assignmentStatementNode an assignment statement node
     * @param structReferenceNode     an identifier which is a var variable in the assignment node
     * @return
     */
    private static StructDefinitionNode getStructDefinition(@NotNull VariableReferenceNode variableReferenceNode,
                                                            @NotNull AssignmentStatementNode assignmentStatementNode,
                                                            @NotNull IdentifierPSINode structReferenceNode) {
        InvocationNode invocationNode = PsiTreeUtil.getChildOfType(variableReferenceNode, InvocationNode.class);
        if (invocationNode != null) {
            IdentifierPSINode identifier = PsiTreeUtil.getChildOfType(invocationNode, IdentifierPSINode.class);
            if (identifier != null) {
                PsiReference reference = identifier.findReferenceAt(identifier.getTextLength());
                if (reference == null) {
                    return null;
                }
                PsiElement resolvedElement = reference.resolve();
                if (resolvedElement == null) {
                    return null;
                }
                // Check whether the resolved element's parent is a connector definition.
                PsiElement definitionNode = resolvedElement.getParent();
                if (!(definitionNode instanceof ActionDefinitionNode)) {
                    return null;
                }
                return getStructDefinition(assignmentStatementNode, structReferenceNode, definitionNode);
            }
        }

        // Get the first child.
        PsiElement node = variableReferenceNode.getFirstChild();
        // If te first child is a VariableReferenceNode, it can be a function invocation.
        if (node instanceof VariableReferenceNode) {
            // Check whether the node is a function invocation.
            boolean isFunctionInvocation = isFunctionInvocation((VariableReferenceNode) node);
            if (isFunctionInvocation) {
                // If it is a function invocation, the first child node will contain the function name.
                PsiElement functionName = node.getFirstChild();
                // We need to resolve the function name to the corresponding function definition.
                PsiReference reference = functionName.findReferenceAt(functionName.getTextLength());
                if (reference == null) {
                    return null;
                }
                PsiElement resolvedFunctionIdentifier = reference.resolve();
                if (resolvedFunctionIdentifier == null) {
                    return null;
                }
                // Check whether the resolved element's parent is a function definition.
                PsiElement definitionNode = resolvedFunctionIdentifier.getParent();
                if (!(definitionNode instanceof FunctionDefinitionNode)) {
                    return null;
                }
                return getStructDefinition(assignmentStatementNode, structReferenceNode, definitionNode);
            }
        } else if (node instanceof NameReferenceNode) {
            // If the node is a NameReferenceNode, that means RHS contains a variable.
            PsiReference reference = node.findReferenceAt(node.getTextLength());
            if (reference == null) {
                return null;
            }
            // We resolve that variable to the definition.
            PsiElement resolvedDefinition = reference.resolve();
            if (resolvedDefinition == null || !(resolvedDefinition instanceof IdentifierPSINode)) {
                return null;
            }
            // Then recursively call the findStructDefinition method to get the correct definition.
            return findStructDefinition((IdentifierPSINode) resolvedDefinition);
        } else if (node instanceof FunctionInvocationNode) {
            FunctionReferenceNode functionReferenceNode = PsiTreeUtil.getChildOfType(node, FunctionReferenceNode.class);
            if (functionReferenceNode == null) {
                return null;
            }
            PsiReference reference = functionReferenceNode.findReferenceAt(functionReferenceNode.getTextLength());
            if (reference == null) {
                return null;
            }
            PsiElement resolvedDefinition = reference.resolve();
            if (resolvedDefinition == null || !(resolvedDefinition instanceof IdentifierPSINode)) {
                return null;
            }
            PsiElement definitionNode = resolvedDefinition.getParent();
            if (!(definitionNode instanceof FunctionDefinitionNode)) {
                return null;
            }
            return getStructDefinition(assignmentStatementNode, structReferenceNode, definitionNode);
        }
        return null;
    }

    @Nullable
    public static StructDefinitionNode getStructDefinition(@NotNull AssignmentStatementNode assignmentStatementNode,
                                                           @NotNull IdentifierPSINode structReferenceNode,
                                                           @NotNull PsiElement definitionNode) {
        // Now we need to know the index of the provided struct reference node in the assignment statement node.
        int index = getVariableIndexFromVarAssignment(assignmentStatementNode, structReferenceNode);
        if (index < 0) {
            return null;
        }
        // Now we get all of the return types from the function.
        List<TypeNameNode> returnTypes = new LinkedList<>();
        if (definitionNode instanceof FunctionDefinitionNode) {
            returnTypes = getReturnTypes(((FunctionDefinitionNode) definitionNode));
        }
        if (definitionNode instanceof ActionDefinitionNode) {
            returnTypes = getReturnTypes(((ActionDefinitionNode) definitionNode));
        }
        // There should be at least 'index+1' amount of elements in the list. If the index is 0, size should be
        // at least 1, etc.
        if (returnTypes.size() <= index) {
            return null;
        }
        // Get the corresponding return type.
        PsiElement elementType = returnTypes.get(index);
        // If this is a struct, we need to resolve it to the definition.
        PsiReference referenceAtElementType = elementType.findReferenceAt(elementType.getTextLength());
        if (referenceAtElementType == null) {
            return null;
        }
        PsiElement resolvedIdentifier = referenceAtElementType.resolve();
        if (resolvedIdentifier != null) {
            // If we can resolve it to a definition, parent should be a struct definition node.
            PsiElement structDefinition = resolvedIdentifier.getParent();
            if (structDefinition instanceof StructDefinitionNode) {
                return (StructDefinitionNode) structDefinition;
            }
        }
        return null;
    }

    @Nullable
    public static PsiElement getAnonymousStruct(@NotNull Object element,
                                                @NotNull List<PsiElement> parameterListNodes,
                                                int offset) {
        int index = BallerinaParameterInfoHandler.getCurrentParameterIndex(element, offset);
        PsiElement parameterListNode = parameterListNodes.get(0);

        if (parameterListNode instanceof ParameterListNode) {
            List<ParameterNode> parameterNodes = PsiTreeUtil.getChildrenOfTypeAsList(parameterListNode,
                    ParameterNode.class);

            if (index < 0 || index >= parameterNodes.size()) {
                return null;
            }
            ParameterNode parameterNode = parameterNodes.get(index);
            TypeNameNode typeNameNode = PsiTreeUtil.findChildOfType(parameterNode, TypeNameNode.class);
            if (typeNameNode == null) {
                return null;
            }
            PsiReference reference = typeNameNode.findReferenceAt(typeNameNode.getTextLength());
            if (reference == null) {
                AnonStructTypeNameNode anonStructNode =
                        PsiTreeUtil.findChildOfType(typeNameNode, AnonStructTypeNameNode.class);
                if (anonStructNode == null) {
                    return null;
                }
                return anonStructNode;
            }
            PsiElement resolvedElement = reference.resolve();
            if (resolvedElement == null || !(resolvedElement.getParent() instanceof StructDefinitionNode)) {
                return null;
            }
            return resolvedElement.getParent();
        }
        return null;
    }

    @Nullable
    public static PsiElement resolveAnonymousStruct(IdentifierPSINode identifier) {
        Object element = BallerinaParameterInfoHandler.findElement(identifier, PsiTreeUtil.prevVisibleLeaf(identifier));
        if (element == null) {
            return null;
        }
        List<PsiElement> parameters = BallerinaParameterInfoHandler.getParameters(element);
        if (parameters.isEmpty()) {
            return null;
        }
        int index = identifier.getStartOffset() + identifier.getTextLength();
        return BallerinaPsiImplUtil.getAnonymousStruct(element, parameters, index);
    }

    /**
     * Returns corresponding error struct for type casts and type conversions.
     *
     * @param assignmentStatementNode {@link AssignmentStatementNode} which we are currently checking
     * @param referenceNode           resolved variable reference node
     * @param isTypeCast              indicates whether the {@link AssignmentStatementNode} contains a type cast
     * @param isTypeConversion        indicates whether the {@link AssignmentStatementNode} contains a type conversion
     * @return {@code TypeCastError} if the provided assignment statement is a type cast operation.
     * {@code TypeConversionError} if the provided assignment statement is a type conversion operation.
     */
    @Nullable
    public static StructDefinitionNode getErrorStruct(@NotNull AssignmentStatementNode assignmentStatementNode,
                                                      @NotNull IdentifierPSINode referenceNode, boolean isTypeCast,
                                                      boolean isTypeConversion) {
        List<IdentifierPSINode> variablesFromVarAssignment =
                BallerinaPsiImplUtil.getVariablesFromVarAssignment(assignmentStatementNode);
        // If this is a type cast or a conversion, there should be at least two return values. Rest of the variables
        // might be unnecessary and identified at the semantic analyzer.
        if (variablesFromVarAssignment.size() < 2) {
            return null;
        }
        // Get the second value from the assignment statement.
        IdentifierPSINode errorVariable = variablesFromVarAssignment.get(1);
        // This should be the provided reference node. Since the error is returned as the second variable.
        if (!errorVariable.equals(referenceNode)) {
            return null;
        }
        // Now we need to find the error.bal file which contains the definition of these error structs.
        Project project = referenceNode.getProject();
        VirtualFile errorFile = BallerinaPsiImplUtil.findFileInSDK(project, referenceNode,
                "/ballerina/builtin/core/source.bal");
        if (errorFile == null) {
            return null;
        }
        // Find the file.
        PsiFile psiFile = PsiManager.getInstance(project).findFile(errorFile);
        if (psiFile == null) {
            return null;
        }
        // Get all struct definitions in the file.
        Collection<StructDefinitionNode> structDefinitionNodes = PsiTreeUtil.findChildrenOfType(psiFile,
                StructDefinitionNode.class);
        // Iterate through all struct definitions.
        for (StructDefinitionNode definitionNode : structDefinitionNodes) {
            IdentifierPSINode nameNode = PsiTreeUtil.getChildOfType(definitionNode, IdentifierPSINode.class);
            if (nameNode != null) {
                // Check and return the matching definition.
                if (isTypeCast && "TypeCastError".equals(nameNode.getText())) {
                    return definitionNode;
                }
                if (isTypeConversion && "TypeConversionError".equals(nameNode.getText())) {
                    return definitionNode;
                }
            }
        }
        return null;
    }

    /**
     * Generic function to get return types from a definition.
     *
     * @param definitionNode definition node
     * @return list of return values of the provided definition.
     */
    @NotNull
    public static List<TypeNameNode> getReturnTypes(@NotNull PsiElement definitionNode) {
        List<TypeNameNode> results = new LinkedList<>();
        // Parameters are in the ReturnParametersNode. So we first get the ReturnParametersNode from the definition
        // node.
        ReturnParametersNode node = PsiTreeUtil.findChildOfType(definitionNode, ReturnParametersNode.class);
        if (node == null) {
            return results;
        }
        // But there can be two possible scenarios. The actual return types can be in either TypeListNode or
        // ParameterListNode. This is because return types can be named parameters. In that case, ParameterListNode is
        // available.

        // First we check for TypeListNode.
        TypeListNode typeListNode = PsiTreeUtil.findChildOfType(node, TypeListNode.class);
        // If it is available, that means the return types are not named parameters.
        if (typeListNode != null) {
            // Each parameter will be of type TypeNameNode. So we get all return types.
            Collection<TypeNameNode> typeNameNodes =
                    PsiTreeUtil.findChildrenOfType(typeListNode, TypeNameNode.class);
            // Add each TypeNameNode to the result list.
            results.addAll(typeNameNodes);
            // Return the results.
            return results;
        }

        // If there is no return type node, we check for ParameterListNode.
        ParameterListNode parameterListNode = PsiTreeUtil.findChildOfType(node, ParameterListNode.class);
        if (parameterListNode != null) {
            // Actual parameters are in ParameterNodes.
            Collection<TypeNameNode> parameterNodes =
                    PsiTreeUtil.findChildrenOfType(parameterListNode, TypeNameNode.class);
            // Add each ParameterNode to the result list.
            results.addAll(parameterNodes);
            // Return the results.
            return results;
        }
        // Return empty list.
        return results;
    }

    /**
     * Returns the index of the provided identifier in the assignment statement node return values.
     *
     * @param assignmentStatementNode assignment statement node which is used to get the return values
     * @param identifier              identifier which needs to be checked
     * @return {@code -1} if the provided identifer is not found in the return values, otherwise {@code >-1}
     * representing the index.
     */
    public static int getVariableIndexFromVarAssignment(@NotNull AssignmentStatementNode assignmentStatementNode,
                                                        @NotNull IdentifierPSINode identifier) {
        List<IdentifierPSINode> variablesFromVarAssignment =
                BallerinaPsiImplUtil.getVariablesFromVarAssignment(assignmentStatementNode);
        for (int i = 0; i < variablesFromVarAssignment.size(); i++) {
            if (variablesFromVarAssignment.get(i).equals(identifier)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Resolves the given package name node to the corresponding definition. packageNameNode can be either a
     * PackageNameNode or an IdentifierPSINode.
     *
     * @param packageNameNode either a {@link PackageNameNode} or a {@link IdentifierPSINode} object which represents
     *                        the package name
     * @return {@link PsiDirectory} which is the definition.
     */
    @Nullable
    public static PsiElement resolvePackage(@NotNull PsiElement packageNameNode) {
        PsiReference reference = packageNameNode.findReferenceAt(0);
        if (reference == null) {
            return null;
        }
        PsiElement resolvedElement = reference.resolve();
        if (resolvedElement == null) {
            return null;
        }

        AliasNode aliasNode = PsiTreeUtil.getParentOfType(resolvedElement, AliasNode.class);
        if (aliasNode == null) {
            return resolvedElement;
        }
        return resolveAliasNode(aliasNode);
    }

    /**
     * Resolves the given alias node to the corresponding directory.
     *
     * @param aliasNode an alias node
     * @return {@link PsiDirectory} which is the definition of the alias node.
     */
    @Nullable
    public static PsiDirectory resolveAliasNode(@NotNull AliasNode aliasNode) {
        ImportDeclarationNode importDeclarationNode = PsiTreeUtil.getParentOfType(aliasNode,
                ImportDeclarationNode.class);
        FullyQualifiedPackageNameNode fullyQualifiedPackageNameNode =
                PsiTreeUtil.getChildOfType(importDeclarationNode, FullyQualifiedPackageNameNode.class);
        if (fullyQualifiedPackageNameNode == null) {
            return null;
        }
        PackageNameNode[] packageNameNodes =
                PsiTreeUtil.getChildrenOfType(fullyQualifiedPackageNameNode, PackageNameNode.class);
        if (packageNameNodes == null) {
            return null;
        }
        PackageNameNode lastElement = ArrayUtil.getLastElement(packageNameNodes);
        if (lastElement == null) {
            return null;
        }
        PsiElement packageName = lastElement.getNameIdentifier();
        if (!(packageName instanceof IdentifierPSINode)) {
            return null;
        }
        List<PsiDirectory> directories = BallerinaPsiImplUtil.resolveDirectory(((IdentifierPSINode) packageName));
        if (directories.isEmpty()) {
            return null;
        }
        return directories.get(0);
    }

    @NotNull
    public static List<IdentifierPSINode> getAttachedFunctions(@NotNull StructDefinitionNode structDefinitionNode) {
        List<IdentifierPSINode> attachedFunctions = new LinkedList<>();
        PsiFile containingFile = structDefinitionNode.getContainingFile();
        PsiDirectory containingPackage = containingFile.getParent();
        if (containingPackage != null) {
            ApplicationManager.getApplication().runReadAction(() -> {
                List<IdentifierPSINode> functions = BallerinaPsiImplUtil.getAllFunctionsFromPackage(containingPackage,
                        false, false);
                for (IdentifierPSINode function : functions) {
                    ProgressManager.checkCanceled();
                    if (!isAttachedFunction(function)) {
                        continue;
                    }
                    PsiElement attachedStruct = getAttachedStruct(function);
                    if (attachedStruct != null && !attachedStruct.equals(structDefinitionNode)) {
                        continue;
                    }
                    attachedFunctions.add(function);
                }
            });
        }
        return attachedFunctions;
    }

    @Nullable
    public static StructDefinitionNode getAttachedStruct(@NotNull IdentifierPSINode function) {
        CodeBlockParameterNode codeBlockParameterNode = PsiTreeUtil.getChildOfType(function.getParent(),
                CodeBlockParameterNode.class);
        if (codeBlockParameterNode == null) {
            return null;
        }
        TypeNameNode typeNameNode = PsiTreeUtil.findChildOfType(codeBlockParameterNode, TypeNameNode.class);
        if (typeNameNode == null) {
            return null;
        }
        PsiReference reference = typeNameNode.findReferenceAt(typeNameNode.getTextLength());
        if (reference == null) {
            return null;
        }
        PsiElement resolvedElement = reference.resolve();
        if (resolvedElement == null) {
            return null;
        }
        PsiElement parent = resolvedElement.getParent();
        if (!(parent instanceof StructDefinitionNode)) {
            return null;
        }
        return (StructDefinitionNode) parent;
    }

    public static boolean isAttachedFunction(@NotNull IdentifierPSINode function) {
        CodeBlockParameterNode codeBlockParameterNode = PsiTreeUtil.getChildOfType(function.getParent(),
                CodeBlockParameterNode.class);
        if (codeBlockParameterNode == null) {
            return false;
        }
        TypeNameNode typeNameNode = PsiTreeUtil.findChildOfType(codeBlockParameterNode, TypeNameNode.class);
        if (typeNameNode == null) {
            return false;
        }
        PsiReference reference = typeNameNode.findReferenceAt(typeNameNode.getTextLength());
        if (reference == null) {
            return false;
        }
        PsiElement resolvedElement = reference.resolve();
        if (resolvedElement == null) {
            return false;
        }
        return true;
    }

    /**
     * Returns the type of the provided identifier.
     *
     * @param identifier an identifier
     * @return the type of the identifier. This can be one of {@link ValueTypeNameNode} (for value types like string),
     * {@link BuiltInReferenceTypeNameNode} (for reference types like json) or {@link TypeNameNode} (for arrays).
     */
    @Nullable
    public static PsiElement getType(@NotNull IdentifierPSINode identifier) {
        PsiElement parent = identifier.getParent();
        // In case of lambda functions or functions attached to types.
        if (parent instanceof FunctionDefinitionNode) {
            CodeBlockParameterNode codeBlockParameterNode = PsiTreeUtil.getChildOfType(parent,
                    CodeBlockParameterNode.class);
            if (codeBlockParameterNode != null) {
                TypeNameNode typeNameNode = PsiTreeUtil.getChildOfType(codeBlockParameterNode, TypeNameNode.class);
                if (typeNameNode != null) {
                    return typeNameNode.getFirstChild();
                }
            }
        }

        PsiReference reference = identifier.findReferenceAt(0);
        if (reference != null) {
            // Todo - Do we need to consider this situation?
        }
        VariableDefinitionNode variableDefinitionNode = PsiTreeUtil.getParentOfType(identifier,
                VariableDefinitionNode.class);
        if (variableDefinitionNode != null) {
            PsiElement typeNode = getType(variableDefinitionNode);
            if (typeNode != null) {
                return typeNode;
            }
        }

        AssignmentStatementNode assignmentStatementNode = PsiTreeUtil.getParentOfType(identifier,
                AssignmentStatementNode.class);
        if (assignmentStatementNode != null) {
            PsiElement typeNode = getType(assignmentStatementNode, identifier);
            if (typeNode != null) {
                return typeNode;
            }
        }

        ParameterNode parameterNode = PsiTreeUtil.getParentOfType(identifier, ParameterNode.class);
        if (parameterNode != null) {
            PsiElement typeNode = getType(parameterNode);
            if (typeNode != null) {
                return typeNode;
            }
        }

        FieldDefinitionNode fieldDefinitionNode = PsiTreeUtil.getParentOfType(identifier, FieldDefinitionNode.class);
        if (fieldDefinitionNode != null) {
            PsiElement typeNode = getType(fieldDefinitionNode);
            if (typeNode != null) {
                return typeNode;
            }
        }
        return null;
    }

    @Nullable
    private static PsiElement getType(@NotNull VariableDefinitionNode variableDefinitionNode) {
        TypeNameNode typeNameNode = PsiTreeUtil.getChildOfType(variableDefinitionNode, TypeNameNode.class);
        if (typeNameNode == null) {
            return null;
        }
        return getType(typeNameNode, true);
    }

    @Nullable
    private static PsiElement getType(@NotNull AssignmentStatementNode assignmentStatementNode,
                                      @NotNull IdentifierPSINode identifier) {
        if (!BallerinaPsiImplUtil.isVarAssignmentStatement(assignmentStatementNode)) {
            return null;
        }
        ExpressionNode expressionNode = PsiTreeUtil.getChildOfType(assignmentStatementNode, ExpressionNode.class);
        if (expressionNode == null) {
            return null;
        }
        PsiElement expressionNodeFirstChild = expressionNode.getFirstChild();
        if (expressionNodeFirstChild instanceof VariableReferenceNode) {
            PsiElement typeNode = getTypeFromVariableReference(assignmentStatementNode, identifier,
                    expressionNodeFirstChild);
            if (typeNode != null) {
                return typeNode;
            }
        } else if (expressionNodeFirstChild instanceof TypeCastNode) {
            int index = getVariableIndexFromVarAssignment(assignmentStatementNode, identifier);
            if (index == 0) {
                TypeNameNode typeNameNode = PsiTreeUtil.getChildOfType(expressionNodeFirstChild, TypeNameNode.class);
                if (typeNameNode == null) {
                    return null;
                }
                return getType(typeNameNode, false);
            }
            StructDefinitionNode errorStruct = BallerinaPsiImplUtil.getErrorStruct(assignmentStatementNode,
                    identifier, true, false);
            if (errorStruct == null) {
                return null;
            }
            return errorStruct.getNameIdentifier();
        } else if (expressionNodeFirstChild instanceof TypeConversionNode) {
            int index = getVariableIndexFromVarAssignment(assignmentStatementNode, identifier);
            if (index == 0) {
                TypeNameNode typeNameNode = PsiTreeUtil.getChildOfType(expressionNodeFirstChild, TypeNameNode.class);
                if (typeNameNode == null) {
                    return null;
                }
                return getType(typeNameNode, false);
            }
            StructDefinitionNode errorStruct = BallerinaPsiImplUtil.getErrorStruct(assignmentStatementNode,
                    identifier, false, true);
            if (errorStruct == null) {
                return null;
            }
            return errorStruct.getNameIdentifier();
        }
        PsiReference firstChildReference =
                expressionNodeFirstChild.findReferenceAt(expressionNodeFirstChild.getTextLength());
        if (firstChildReference == null) {
            return null;
        }
        PsiElement resolvedElement = firstChildReference.resolve();
        if (resolvedElement == null) {
            return null;
        }
        return getType(((IdentifierPSINode) resolvedElement));
    }

    @Nullable
    private static PsiElement getTypeFromVariableReference(@NotNull AssignmentStatementNode assignmentStatementNode,
                                                           @NotNull IdentifierPSINode identifier, PsiElement
                                                                   expressionNodeFirstChild) {
        int index = getVariableIndexFromVarAssignment(assignmentStatementNode, identifier);
        if (index < 0) {
            return null;
        }
        FunctionInvocationNode functionInvocationNode = PsiTreeUtil.getChildOfType(expressionNodeFirstChild,
                FunctionInvocationNode.class);
        if (functionInvocationNode == null) {
            return null;
        }
        FunctionReferenceNode functionReferenceNode = PsiTreeUtil.getChildOfType(functionInvocationNode,
                FunctionReferenceNode.class);
        if (functionReferenceNode == null) {
            return null;
        }
        PsiReference functionReference =
                functionReferenceNode.findReferenceAt(functionReferenceNode.getTextLength());
        if (functionReference == null) {
            return null;
        }
        PsiElement resolvedElement = functionReference.resolve();
        if (resolvedElement == null || !(resolvedElement.getParent() instanceof FunctionDefinitionNode)) {
            return null;
        }
        List<TypeNameNode> returnTypes = getReturnTypes(((FunctionDefinitionNode) resolvedElement.getParent()));
        // There should be at least 'index+1' amount of elements in the list. If the
        // index is 0, size should be at least 1, etc.
        if (returnTypes.size() <= index) {
            return null;
        }
        TypeNameNode typeNameNode = returnTypes.get(index);
        PsiElement typeNode = getType(typeNameNode, true);
        if (typeNode == null) {
            return null;
        }
        return typeNode;
    }

    @Nullable
    private static PsiElement getType(@NotNull ParameterNode parameterNode) {
        TypeNameNode typeNameNode = PsiTreeUtil.getChildOfType(parameterNode, TypeNameNode.class);
        if (typeNameNode == null) {
            return null;
        }
        PsiElement typeNode = getType(typeNameNode, true);
        if (typeNode == null) {
            return null;
        }
        return typeNode;
    }

    // Todo - merge to a single method
    @Nullable
    private static PsiElement getType(@NotNull FieldDefinitionNode fieldDefinitionNode) {
        TypeNameNode typeNameNode = PsiTreeUtil.getChildOfType(fieldDefinitionNode, TypeNameNode.class);
        if (typeNameNode == null) {
            return null;
        }
        PsiElement typeNode = getType(typeNameNode, true);
        if (typeNode == null) {
            return null;
        }
        return typeNode;
    }

    @Nullable
    private static PsiElement getType(@NotNull TypeNameNode typeNameNode, boolean checkArrayType) {
        if (checkArrayType) {
            boolean isArray = isArrayType(typeNameNode);
            if (isArray) {
                return typeNameNode;
            }
        }
        NameReferenceNode nameReferenceNode = PsiTreeUtil.findChildOfType(typeNameNode, NameReferenceNode.class);
        if (nameReferenceNode != null) {
            PsiReference typeReference = nameReferenceNode.findReferenceAt(nameReferenceNode.getTextLength());
            if (typeReference != null) {
                PsiElement resolvedDefinition = typeReference.resolve();
                if (resolvedDefinition != null) {
                    return resolvedDefinition;
                }
            }
        }
        ValueTypeNameNode valueTypeNameNode = PsiTreeUtil.findChildOfType(typeNameNode, ValueTypeNameNode.class);
        if (valueTypeNameNode != null) {
            return valueTypeNameNode;
        }
        BuiltInReferenceTypeNameNode builtInReferenceTypeNameNode = PsiTreeUtil.findChildOfType(typeNameNode,
                BuiltInReferenceTypeNameNode.class);
        if (builtInReferenceTypeNameNode == null) {
            return null;
        }
        return builtInReferenceTypeNameNode;
    }

    /**
     * Checks whether the given {@code typeNameNode}  contains an array.
     *
     * @param typeNameNode a typeNameNode
     * @return {@code true} if the given typeNameNode contains an array, {@code false} otherwise.
     */
    private static boolean isArrayType(@NotNull TypeNameNode typeNameNode) {
        PsiElement[] children = typeNameNode.getChildren();
        if (children.length != 3 || !(children[1] instanceof LeafPsiElement)
                || !(children[2] instanceof LeafPsiElement)) {
            return false;
        }
        return (((LeafPsiElement) children[1]).getElementType() == BallerinaTypes.LBRACK)
                && (((LeafPsiElement) children[2]).getElementType() == BallerinaTypes.RBRACK);
    }

    @Nullable
    public static ConnectorDefinitionNode getConnectorDefinition(@NotNull EndpointDeclarationNode node) {
        ConnectorReferenceNode connectorReferenceNode = PsiTreeUtil.getChildOfType(node, ConnectorReferenceNode.class);
        if (connectorReferenceNode == null) {
            return null;
        }
        PsiReference reference = connectorReferenceNode.findReferenceAt(connectorReferenceNode.getTextLength());
        if (reference == null) {
            return null;
        }
        PsiElement resolvedElement = reference.resolve();
        if (resolvedElement == null) {
            return null;
        }
        if (resolvedElement.getParent() instanceof ConnectorDefinitionNode) {
            return ((ConnectorDefinitionNode) resolvedElement.getParent());
        }
        return null;
    }

    /**
     * Used to identify variables used in var assignment statements are redeclared variables or not.
     *
     * @param identifier an identifier
     * @return {@code true} if the variable is declared before in a resolvable scope, {@code false} otherwise.
     */
    public static boolean isRedeclaredVar(@NotNull IdentifierPSINode identifier) {
        ScopeNode scope = PsiTreeUtil.getParentOfType(identifier, CodeBlockScope.class, VariableContainer.class,
                TopLevelDefinition.class, LowerLevelDefinition.class);
        if (scope != null) {
            int caretOffset = identifier.getStartOffset();
            List<IdentifierPSINode> variables =
                    BallerinaPsiImplUtil.getAllLocalVariablesInResolvableScope(scope, caretOffset);
            for (IdentifierPSINode variable : variables) {
                if (variable != null && variable.getText().equals(identifier.getText())) {
                    return true;
                }
            }

            List<IdentifierPSINode> parameters = BallerinaPsiImplUtil.getAllParametersInResolvableScope(scope,
                    caretOffset);
            for (IdentifierPSINode parameter : parameters) {
                if (parameter != null && parameter.getText().equals(identifier.getText())) {
                    return true;
                }
            }

            List<IdentifierPSINode> globalVariables =
                    BallerinaPsiImplUtil.getAllGlobalVariablesInResolvableScope(scope);
            for (IdentifierPSINode variable : globalVariables) {
                if (variable != null && variable.getText().equals(identifier.getText())) {
                    return true;
                }
            }

            List<IdentifierPSINode> constants = BallerinaPsiImplUtil.getAllConstantsInResolvableScope(scope);
            for (IdentifierPSINode constant : constants) {
                if (constant != null && constant.getText().equals(identifier.getText())) {
                    return true;
                }
            }

            List<PsiElement> namespaces = BallerinaPsiImplUtil.getAllXmlNamespacesInResolvableScope(scope,
                    caretOffset);
            for (PsiElement namespace : namespaces) {
                if (namespace != null && namespace.getText().equals(identifier.getText())) {
                    return true;
                }
            }

            List<IdentifierPSINode> endpoints = BallerinaPsiImplUtil.getAllEndpointsInResolvableScope(scope,
                    caretOffset);
            for (IdentifierPSINode endpoint : endpoints) {
                if (endpoint != null && endpoint.getText().equals(identifier.getText())) {
                    return true;
                }
            }
        }
        return false;
    }
}

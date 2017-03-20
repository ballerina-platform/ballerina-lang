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

package org.ballerinalang.plugins.idea.run.configuration;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.ide.scratch.ScratchFileType;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.FileIndexFacade;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.TextBrowseFolderListener;
import com.intellij.openapi.ui.TextComponentAccessor;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.plugins.idea.BallerinaFileType;
import org.ballerinalang.plugins.idea.psi.FunctionNode;
import org.ballerinalang.plugins.idea.psi.ParameterListNode;
import org.ballerinalang.plugins.idea.psi.ServiceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.SimpleTypeArrayNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class BallerinaRunUtil {

    private BallerinaRunUtil() {
    }

    //    @Contract("null -> false")
    //    public static boolean isPackageContext(@Nullable PsiElement contextElement) {
    //        return PsiTreeUtil.getNonStrictParentOfType(contextElement, GoPackageClause.class) != null;
    //    }

    @Nullable
    public static PsiFile findMainFileInDirectory(@NotNull VirtualFile packageDirectory, @NotNull Project project) {
        for (VirtualFile file : packageDirectory.getChildren()) {
            if (file == null) {
                continue;
            }
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (isMainBallerinaFile(psiFile)) {
                return psiFile;
            }
        }
        return null;
    }

    @Nullable
    public static PsiElement getContextElement(@Nullable ConfigurationContext context) {
        if (context == null) {
            return null;
        }
        PsiElement psiElement = context.getPsiLocation();
        if (psiElement == null || !psiElement.isValid()) {
            return null;
        }

        FileIndexFacade indexFacade = FileIndexFacade.getInstance(psiElement.getProject());
        PsiFileSystemItem psiFile = psiElement instanceof PsiFileSystemItem ? (PsiFileSystemItem) psiElement :
                psiElement.getContainingFile();
        VirtualFile file = psiFile != null ? psiFile.getVirtualFile() : null;
        if (file != null && file.getFileType() != ScratchFileType.INSTANCE &&
                (!indexFacade.isInContent(file) || indexFacade.isExcludedFile(file))) {
            return null;
        }
        return psiElement;
    }

    public static void installBallerinaWithMainFileChooser(Project project,
                                                           @NotNull TextFieldWithBrowseButton fileField) {
        installFileChooser(project, fileField, false, false, file -> {
            if (file.getFileType() != BallerinaFileType.INSTANCE) {
                return false;
            }
            return isMainBallerinaFile(PsiManager.getInstance(project).findFile(file));
        });
    }

    @Contract("null -> false")
    public static boolean isMainBallerinaFile(@Nullable PsiFile psiFile) {
        //        if (/*!GoTestFinder.isTestFile(psiFile) &&*/ psiFile instanceof BallerinaFile) {
        //            //            return BallerinaConstants.MAIN.equals(((BallerinaFile)psiFile).getPackageName())
        // && (
        //            // (BallerinaFile)psiFile)
        //            //                    .hasMainFunction();
        //            //            return true;
        //            return hasMainFunction(psiFile) || hasServices(psiFile);
        //        }
        //        return false;

        return hasMainFunction(psiFile);
    }

    @Contract("null -> false")
    public static boolean hasMainFunction(PsiFile file) {
        Collection<FunctionNode> functionNodes = PsiTreeUtil.findChildrenOfType(file, FunctionNode.class);
        for (FunctionNode functionNode : functionNodes) {
            ParameterListNode parameterListNode = PsiTreeUtil.getChildOfType(functionNode, ParameterListNode.class);
            if (parameterListNode == null) {
                return false;
            }
            PsiElement[] children = parameterListNode.getChildren();
            if (children.length != 1) {
                return false;
            }
            SimpleTypeArrayNode simpleTypeArrayNode =
                    PsiTreeUtil.findChildOfType(children[0], SimpleTypeArrayNode.class);
            if (simpleTypeArrayNode == null) {
                return false;
            }
            PsiElement nameIdentifier = simpleTypeArrayNode.getNameIdentifier();
            if (nameIdentifier == null) {
                return false;
            }
            if ("string".equals(nameIdentifier.getText())) {
                return true;
            }
        }
        return false;
    }

    @Contract("null -> false")
    public static boolean hasServices(PsiFile file) {
        Collection<ServiceDefinitionNode> serviceDefinitionNodes =
                PsiTreeUtil.findChildrenOfType(file, ServiceDefinitionNode.class);
        return !serviceDefinitionNodes.isEmpty();
    }

    public static void installFileChooser(@NotNull Project project, @NotNull ComponentWithBrowseButton field,
                                          boolean directory) {
        installFileChooser(project, field, directory, false);
    }

    public static void installFileChooser(@NotNull Project project, @NotNull ComponentWithBrowseButton field,
                                          boolean directory, boolean showFileSystemRoots) {
        installFileChooser(project, field, directory, showFileSystemRoots, null);
    }

    public static void installFileChooser(@NotNull Project project, @NotNull ComponentWithBrowseButton field,
                                          boolean directory, boolean showFileSystemRoots,
                                          @Nullable Condition<VirtualFile> fileFilter) {
        FileChooserDescriptor chooseDirectoryDescriptor = directory
                ? FileChooserDescriptorFactory.createSingleFolderDescriptor()
                : FileChooserDescriptorFactory.createSingleLocalFileDescriptor();
        chooseDirectoryDescriptor.setRoots(project.getBaseDir());
        chooseDirectoryDescriptor.setShowFileSystemRoots(showFileSystemRoots);
        chooseDirectoryDescriptor.withFileFilter(fileFilter);
        if (field instanceof TextFieldWithBrowseButton) {
            ((TextFieldWithBrowseButton) field).addBrowseFolderListener(
                    new TextBrowseFolderListener(chooseDirectoryDescriptor, project));
        } else {
            //noinspection unchecked
            field.addBrowseFolderListener(project, new ComponentWithBrowseButton.BrowseFolderActionListener(null,
                    null, field, project,
                    chooseDirectoryDescriptor,
                    TextComponentAccessor.TEXT_FIELD_WITH_HISTORY_WHOLE_TEXT));
        }
    }

    public static void printGoEnvVariables(@NotNull GeneralCommandLine commandLine, @NotNull ProcessHandler handler) {
        Map<String, String> environment = commandLine.getEnvironment();
        //        handler.notifyTextAvailable("GOROOT=" + StringUtil.nullize(environment.get(GoConstants.GO_ROOT)) +
        // '\n', ProcessOutputTypes.SYSTEM);
        //        handler.notifyTextAvailable("GOPATH=" + StringUtil.nullize(environment.get(GoConstants.GO_PATH)) +
        // '\n', ProcessOutputTypes.SYSTEM);
    }

    @Nullable
    public static VirtualFile findByPath(@NotNull String path, @NotNull Project project, @Nullable Module module) {
        path = FileUtil.toSystemIndependentName(path);
        VirtualFile projectBaseDir = project.getBaseDir();
        if (path.isEmpty()) {
            return projectBaseDir;
        }
        return projectBaseDir.findFileByRelativePath(path);
    }
}

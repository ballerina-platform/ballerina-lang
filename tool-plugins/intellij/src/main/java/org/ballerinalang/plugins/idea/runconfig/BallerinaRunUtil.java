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

package org.ballerinalang.plugins.idea.runconfig;

import com.intellij.execution.actions.ConfigurationContext;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.ide.scratch.ScratchFileType;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
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
import org.ballerinalang.plugins.idea.BallerinaConstants;
import org.ballerinalang.plugins.idea.BallerinaFileType;
import org.ballerinalang.plugins.idea.psi.FormalParameterListNode;
import org.ballerinalang.plugins.idea.psi.FunctionDefinitionNode;
import org.ballerinalang.plugins.idea.psi.ParameterListNode;
import org.ballerinalang.plugins.idea.psi.ServiceDefinitionNode;
import org.ballerinalang.plugins.idea.psi.TypeNameNode;
import org.ballerinalang.plugins.idea.psi.ValueTypeNameNode;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/**
 * Contains util classes related to running Ballerina programs.
 */
public class BallerinaRunUtil {

    private BallerinaRunUtil() {

    }

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
    public static PsiFile findServiceFileInDirectory(@NotNull VirtualFile packageDirectory, @NotNull Project project) {
        for (VirtualFile file : packageDirectory.getChildren()) {
            if (file == null) {
                continue;
            }
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            if (hasServices(psiFile)) {
                return psiFile;
            }
        }
        return null;
    }

    @Nullable
    static PsiElement getContextElement(@Nullable ConfigurationContext context) {
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

    public static void installBallerinaWithWorkingDirectoryChooser(Project project,
                                                                   @NotNull TextFieldWithBrowseButton fileField) {
        installWorkingDirectoryChooser(project, fileField);
    }

    public static void installBallerinaWithMainFileChooser(Project project,
                                                           @NotNull TextFieldWithBrowseButton fileField) {
        installFileChooser(project, fileField, file ->
                isMainBallerinaFile(PsiManager.getInstance(project).findFile(file)));
    }

    public static void installBallerinTestFileChooser(Project project, @NotNull TextFieldWithBrowseButton fileField) {
        installFileChooser(project, fileField, file ->
                isBallerinaTestFile(PsiManager.getInstance(project).findFile(file)));
    }

    @Contract("null -> false")
    private static boolean isMainBallerinaFile(@Nullable PsiFile psiFile) {
        return hasMainFunction(psiFile);
    }

    @Contract("null -> false")
    private static boolean isBallerinaTestFile(@Nullable PsiFile psiFile) {
        if (psiFile == null) {
            return false;
        }
        return psiFile.getName().endsWith(BallerinaConstants.BALLERINA_TEST_FILE_SUFFIX);
    }

    @Contract("null -> false")
    static boolean hasMainFunction(PsiFile file) {
        Collection<FunctionDefinitionNode> functionNodes = PsiTreeUtil.findChildrenOfType(file,
                FunctionDefinitionNode.class);
        for (FunctionDefinitionNode functionNode : functionNodes) {
            if (isMainFunction(functionNode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether the given functionDefinitionNode is a main function node.
     *
     * @param functionDefinitionNode FunctionDefinitionNode which needs to be checked
     * @return {@code true} if the provided node is a main function, {@code false} otherwise.
     */
    @Contract("null -> false")
    static boolean isMainFunction(FunctionDefinitionNode functionDefinitionNode) {
        // Get the function name.
        PsiElement functionName = functionDefinitionNode.getNameIdentifier();
        if (functionName == null) {
            return false;
        }
        // Check whether the function name is "main".
        if (!BallerinaConstants.MAIN.equals(functionName.getText())) {
            return false;
        }
        // Get the ParameterListNode which contains all the parameters in the function.
        FormalParameterListNode parameterListNode = PsiTreeUtil.getChildOfType(functionDefinitionNode,
                FormalParameterListNode.class);
        if (parameterListNode == null) {
            return false;
        }
        // Get the child nodes. These are objects of ParameterNode.
        PsiElement[] parameterNodes = parameterListNode.getChildren();
        // There should be only one parameter for main function.
        if (parameterNodes.length != 1) {
            return false;
        }
        // Get the TypeNameNode which contains the type of the parameter. In this case, it will be "string[]".
        TypeNameNode arrayTypeNameNode = PsiTreeUtil.findChildOfType(parameterNodes[0], TypeNameNode.class);
        if (arrayTypeNameNode == null) {
            return false;
        }
        // "string", "[", "]" will be in 3 different child nodes.
        PsiElement[] children = arrayTypeNameNode.getChildren();
        if (children.length != 3) {
            return false;
        }
        // First child node will also be a TypeNameNode which contains the type (string).
        if (!(children[0] instanceof TypeNameNode)) {
            return false;
        }
        if (!"[".equals(children[1].getText())) {
            return false;
        }
        if (!"]".equals(children[2].getText())) {
            return false;
        }
        // ValueTypeNameNode will contain the actual value of the type (string).
        ValueTypeNameNode valueTypeNameNode = PsiTreeUtil.findChildOfType(children[0], ValueTypeNameNode.class);
        if (valueTypeNameNode == null) {
            return false;
        }
        // Get the text (string) and check and return the result.
        return valueTypeNameNode.getText() != null && "string".equals(valueTypeNameNode.getText());
    }

    @Contract("null -> false")
    static boolean isTestFunction(FunctionDefinitionNode functionDefinitionNode) {
        // Get the function name.
        PsiElement functionName = functionDefinitionNode.getNameIdentifier();
        if (functionName == null) {
            return false;
        }
        // Check whether the function name is "main".
        if (functionName.getText().startsWith(BallerinaConstants.BALLERINA_TEST_FUNCTION_PREFIX)) {
            return true;
        }
        return false;
    }

    @Contract("null -> false")
    static boolean hasServices(PsiFile file) {
        Collection<ServiceDefinitionNode> serviceDefinitionNodes =
                PsiTreeUtil.findChildrenOfType(file, ServiceDefinitionNode.class);
        return !serviceDefinitionNodes.isEmpty();
    }

    private static void installFileChooser(@NotNull Project project, @NotNull ComponentWithBrowseButton field,
                                           @Nullable Condition<VirtualFile> fileFilter) {
        FileChooserDescriptor chooseDirectoryDescriptor =
                FileChooserDescriptorFactory.createSingleFileDescriptor(BallerinaFileType.INSTANCE);
        chooseDirectoryDescriptor.setRoots(project.getBaseDir());
        chooseDirectoryDescriptor.setShowFileSystemRoots(false);
        chooseDirectoryDescriptor.withShowHiddenFiles(false);
        chooseDirectoryDescriptor.withFileFilter(fileFilter);
        if (field instanceof TextFieldWithBrowseButton) {
            ((TextFieldWithBrowseButton) field).addBrowseFolderListener(
                    new TextBrowseFolderListener(chooseDirectoryDescriptor, project));
        } else {
            //noinspection unchecked
            field.addBrowseFolderListener(project, new ComponentWithBrowseButton.BrowseFolderActionListener(null,
                    null, field, project, chooseDirectoryDescriptor,
                    TextComponentAccessor.TEXT_FIELD_WITH_HISTORY_WHOLE_TEXT));
        }
    }

    private static void installWorkingDirectoryChooser(@NotNull Project project, @NotNull ComponentWithBrowseButton
            field) {
        FileChooserDescriptor chooseDirectoryDescriptor =
                FileChooserDescriptorFactory.createSingleFolderDescriptor();
        chooseDirectoryDescriptor.setShowFileSystemRoots(true);
        chooseDirectoryDescriptor.withShowHiddenFiles(false);
        if (field instanceof TextFieldWithBrowseButton) {
            ((TextFieldWithBrowseButton) field).addBrowseFolderListener(
                    new TextBrowseFolderListener(chooseDirectoryDescriptor, project));
        } else {
            //noinspection unchecked
            field.addBrowseFolderListener(project, new ComponentWithBrowseButton.BrowseFolderActionListener(null,
                    null, field, project, chooseDirectoryDescriptor,
                    TextComponentAccessor.TEXT_FIELD_WITH_HISTORY_WHOLE_TEXT));
        }
    }

    public static void printBallerinaEnvVariables(@NotNull GeneralCommandLine commandLine,
                                                  @NotNull ProcessHandler handler) {
        Map<String, String> environment = commandLine.getEnvironment();
        // Todo - Add BALLERINA_REPOSITORY
        //        handler.notifyTextAvailable("BALLERINA_REPOSITORY=" + StringUtil.nullize(environment.get
        //                (BallerinaConstants.BALLERINA_REPOSITORY)) + '\n', ProcessOutputTypes.SYSTEM);
    }

    @Nullable
    public static VirtualFile findByPath(@NotNull String path, @NotNull Project project) {
        String systemIndependentPath = FileUtil.toSystemIndependentName(path);
        VirtualFile projectBaseDir = project.getBaseDir();
        if (systemIndependentPath.isEmpty()) {
            return projectBaseDir;
        }
        return projectBaseDir.findFileByRelativePath(systemIndependentPath);
    }
}

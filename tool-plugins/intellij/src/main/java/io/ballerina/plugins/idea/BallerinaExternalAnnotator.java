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

package io.ballerina.plugins.idea;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.PathUtil;
import io.ballerina.plugins.idea.codeinsight.semanticanalyzer.BallerinaSemanticAnalyzerSettings;
import io.ballerina.plugins.idea.psi.impl.BallerinaPsiImplUtil;
import io.ballerina.plugins.idea.sdk.BallerinaSdkService;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;

/**
 * External annotator is used to get diagnostics from an external tool and annotate the code. These operations can be
 * time consuming. So this is executed after all the pending tasks are completed.
 */
public class BallerinaExternalAnnotator extends ExternalAnnotator<BallerinaExternalAnnotator.Data, List<Diagnostic>> {

    private static Method method;
    private static URLClassLoader urlClassLoader;
    private Editor editor;

    private static final Logger LOGGER = LoggerFactory.getLogger(BallerinaExternalAnnotator.class);

    private static String lastFileContent;
    private static Data lastData;

    /**
     * This is called first to collect information.
     */
    @Override
    @Nullable
    public Data collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
        if (!BallerinaSemanticAnalyzerSettings.getInstance().useSemanticAnalyzer()) {
            return null;
        }
        // Get the file content and cleanup newlines and spaces.
        String content = file.getText().replaceAll("\n", "").replaceAll("\\s+", " ");
        // If the contents are equal, that means that the user pressed enter or added some spaces. In that case, we
        // don't need to call semantic analyzer again.
        if (content.equals(lastFileContent)) {
            return lastData;
        }
        this.editor = editor;
        // Update the last file content.
        lastFileContent = content;

        VirtualFile virtualFile = file.getVirtualFile();
        String packageNameNode = getPackageName(file);
        // If method is not null, that means we have already loaded jars.
        Data data = new Data(editor, file, packageNameNode);
        lastData = data;

        if (method != null) {
            return data;
        }
        Module module = ModuleUtilCore.findModuleForFile(virtualFile, file.getProject());
        if (module == null) {
            return null;
        }
        Sdk moduleSdk = ModuleRootManager.getInstance(module).getSdk();
        String sdkHome;
        if (moduleSdk != null) {
            sdkHome = moduleSdk.getHomePath();
        } else {
            sdkHome = BallerinaSdkService.getInstance(file.getProject()).getSdkHomePath(null);
        }

        try {
            List<URL> filesToLoad = new LinkedList<>();
            File[] files = new File(sdkHome + BallerinaConstants.BALLERINA_SDK_LIB_DIR).listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile() && f.getName().endsWith(BallerinaConstants.BALLERINA_SDK_LIB_FILE_EXTENTION)) {
                        filesToLoad.add(f.toURI().toURL());
                    }
                }
            }
            // Create a new class loader.
            urlClassLoader = new URLClassLoader(filesToLoad.toArray(new URL[filesToLoad.size()]),
                    this.getClass().getClassLoader());
            Class classToLoad = Class.forName("org.ballerinalang.launcher.util.BCompileUtil", true,
                    urlClassLoader);
            // Get the method.
            method = classToLoad.getMethod("getDiagnostics", ClassLoader.class, String.class, String.class);
        } catch (MalformedURLException | NoSuchMethodException | ClassNotFoundException e) {
            LOGGER.debug(e.getMessage(), e);
        }
        return data;
    }

    /**
     * This is called 2nd. This will retrieve diagnostics from the external tool.
     */
    @Nullable
    @Override
    public List<Diagnostic> doAnnotate(final Data data) {
        if (!BallerinaSemanticAnalyzerSettings.getInstance().useSemanticAnalyzer()) {
            return null;
        }
        if (method != null) {
            // We need to save all documents before getting diagnostics. Otherwise diagnostic will be incorrect.
            ApplicationManager.getApplication().invokeAndWait(() -> {
                FileDocumentManager.getInstance().saveAllDocuments();
            });

            // Get the current module.
            Module module = ModuleUtilCore.findModuleForPsiElement(data.psiFile);
            // Set the default value of source root as the project base path.
            Project project = data.psiFile.getProject();
            String sourceRoot = project.getBasePath();

            // Get the file name (if the file is in project root) or the package name.
            String fileName = data.packageNameNode;
            if (fileName == null) {
                VirtualFile virtualFile = data.psiFile.getVirtualFile();
                fileName = virtualFile.getName();
            }

            // If we are currently in a module, we need to set the module root as the source root.
            if (module != null && FileUtil.exists(module.getModuleFilePath())) {
                sourceRoot = StringUtil.trimEnd(PathUtil.getParentPath(module.getModuleFilePath()),
                        BallerinaConstants.IDEA_CONFIG_DIRECTORY);
            }

            try {
                // We need to set the "ballerina.home" property before invoking this method. Otherwise the
                // compilation will fail with a NPE.
                String sdkHomePath = BallerinaSdkService.getInstance(project).getSdkHomePath(null);
                if (sdkHomePath != null) {
                    System.setProperty("ballerina.home", sdkHomePath);
                }
                // Get the list of diagnostics.
                return (List<Diagnostic>) method.invoke(null, urlClassLoader, sourceRoot, fileName);
            } catch (Exception e) {
                LOGGER.debug(e.getMessage(), e);
            }
        }
        return new LinkedList<>();
    }

    /**
     * Return the package name correspond to the provided file. This method will also consider the directory structure
     * as well. If the directory structure is different than the declared package in the file, relative directory
     * structure will be converted to package name and will be returned. Following 2 scenarios need to be considered.
     * <p>
     * Scenario 1 - Incorrect package declared in file. Correct directory structure.
     * Scenario 2 - Package declared in files in project root.
     *
     * @param file a psi file
     * @return package name correspond to the provided file
     */
    @NotNull
    private String getPackageName(PsiFile file) {
        PsiDirectory parent = file.getParent();

        if (BallerinaPsiImplUtil.isAContentRoot(parent)) {
            return file.getName();
        }
        while (parent != null && parent.getParent() != null &&
                !BallerinaPsiImplUtil.isAContentRoot(parent.getParent())) {
            parent = parent.getParent();
        }

        if (parent != null) {
            return parent.getName();
        }
        return ".";
    }

    /**
     * This is called 3rd to annotate the code in editor.
     */
    @Override
    public void apply(@NotNull PsiFile file, List<Diagnostic> diagnostics, @NotNull AnnotationHolder holder) {
        if (!BallerinaSemanticAnalyzerSettings.getInstance().useSemanticAnalyzer()) {
            return;
        }

        String packageName = getPackageName(file);
        String fileName = file.getVirtualFile().getName();

        try {
            for (Diagnostic diagnostic : diagnostics) {
                // Validate the package name.
                String sourcePackageName = diagnostic.getSource().getPackageName();
                if (!sourcePackageName.equals(".") && !sourcePackageName.equals(packageName)) {
                    continue;
                }
                // Validate the file name since diagnostics are sent for all files in the package.
                if (!fileName.equals(diagnostic.getSource().getCompilationUnitName())) {
                    continue;
                }

                Diagnostic.DiagnosticPosition position = diagnostic.getPosition();
                // If the start line or start column is less than 0, it will throw an exception.
                if (position.getStartLine() <= 0 || position.getStartColumn() <= 0) {
                    continue;
                }

                // Get the logical start postion. This is used to get the offset.
                LogicalPosition startPosition = new LogicalPosition(position.getStartLine() - 1,
                        position.getStartColumn() - 1);
                int startOffset = editor.logicalPositionToOffset(startPosition);
                // Get the element at the offset.
                PsiElement elementAtOffset = file.findElementAt(startOffset);
                // If the element at the offset is a whitespace, highlight the next element.
                if (elementAtOffset instanceof PsiWhiteSpace) {
                    elementAtOffset = PsiTreeUtil.nextVisibleLeaf(elementAtOffset);
                }

                // Get the text range to be highlighted.
                TextRange textRange;
                if (elementAtOffset == null) {
                    int endColumn = position.getStartColumn() == position.getEndColumn() ?
                            position.getEndColumn() + 1 : position.getEndColumn();
                    if (position.getEndLine() <= 0) {
                        continue;
                    }
                    LogicalPosition endPosition = new LogicalPosition(position.getEndLine() - 1, endColumn);
                    int endOffset = editor.logicalPositionToOffset(endPosition);
                    textRange = new TextRange(startOffset, endOffset);
                } else {
                    int endOffset = elementAtOffset.getTextOffset() + elementAtOffset.getTextLength();
                    textRange = new TextRange(elementAtOffset.getTextOffset(), endOffset);
                }

                // Highlight the range according to the diagnostic kind.
                if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                    holder.createErrorAnnotation(textRange, "Compiler: " + diagnostic.getMessage());
                } else if (diagnostic.getKind() == Diagnostic.Kind.WARNING) {
                    holder.createWarningAnnotation(textRange, "Compiler: " + diagnostic.getMessage());
                } else if (diagnostic.getKind() == Diagnostic.Kind.NOTE) {
                    holder.createInfoAnnotation(textRange, "Compiler: " + diagnostic.getMessage());
                }
            }
        } catch (ClassCastException e) {
            LOGGER.debug(e.getMessage(), e);
        }
    }

    /**
     * This method is used to reset the method field after changing the SDK.
     */
    public static void reset() {
        method = null;
    }

    /**
     * Helper class which contains data.
     */
    static class Data {

        Editor editor;
        PsiFile psiFile;
        String packageNameNode;

        Data(@NotNull Editor editor, @NotNull PsiFile psiFile, @Nullable String packageNameNode) {
            this.editor = editor;
            this.psiFile = psiFile;
            this.packageNameNode = packageNameNode;
        }
    }
}

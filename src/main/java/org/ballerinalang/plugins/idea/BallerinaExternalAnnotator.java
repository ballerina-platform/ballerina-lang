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

package org.ballerinalang.plugins.idea;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.util.PsiTreeUtil;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * An external annotator is an object that analyzes code in a document
 * and annotates the PSI elements with errors or warnings. Because such
 * analysis can be expensive, we don't want it in the GUI event loop. Jetbrains
 * provides this external annotator mechanism to run these analyzers out of band.
 */
public class BallerinaExternalAnnotator extends ExternalAnnotator<BallerinaExternalAnnotator.Data,
        List<BallerinaExternalAnnotator.Issue>> {

    // NOTE: can't use instance vars as only 1 instance

    private static Method method;

    /**
     * Called first.
     */
    @Override
    @Nullable
    public Data collectInformation(@NotNull PsiFile file, @NotNull Editor editor, boolean hasErrors) {
        VirtualFile virtualFile = file.getVirtualFile();
        if (method == null) {
            Module module = ModuleUtilCore.findModuleForFile(virtualFile, file.getProject());
            if (module == null) {
                return null;
            }
            Sdk moduleSdk = ModuleRootManager.getInstance(module).getSdk();
            if (moduleSdk == null) {
                return null;
            }
            String sdkHome = moduleSdk.getHomePath();
            try {
                List<URL> filesToLoad = new LinkedList<>();
                File[] files = new File(sdkHome + "/bre/lib").listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isFile() && f.getName().endsWith(".jar")) {
                            filesToLoad.add(f.toURI().toURL());
                        }
                    }
                }

                URLClassLoader urlClassLoader = new URLClassLoader(filesToLoad.toArray(new URL[filesToLoad.size()]),
                        this.getClass().getClassLoader());

                Class classToLoad = Class.forName("org.ballerinalang.launcher.BTester", true, urlClassLoader);
                method = classToLoad.getMethod("getDiagnostics", String.class, String.class);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return new Data(editor, file);
    }

    /**
     * Called 2nd; look for trouble in file and return list of issues.
     * <p>
     * For most custom languages, you would not reimplement your semantic
     * analyzer using PSI trees. Instead, here is where you would call out to
     * your custom languages compiler or interpreter to get error messages
     * or other bits of information you'd like to annotate the document with.
     */
    @Nullable
    @Override
    public List<Issue> doAnnotate(final Data data) {
        List<Issue> issues = new ArrayList<>();
        if (method != null) {
            try {
                VirtualFile virtualFile = data.psiFile.getVirtualFile();
                List<Diagnostic> diagnostics = (List<Diagnostic>) method.invoke(null, virtualFile.getParent().getPath(),
                        virtualFile.getName());
                Editor editor = data.editor;
                if (editor == null) {
                    return issues;
                }
                ApplicationManager.getApplication().runWriteAction(
                        () -> PsiDocumentManager.getInstance(data.psiFile.getProject())
                                .commitDocument(editor.getDocument())
                );
                for (Diagnostic diagnostic : diagnostics) {

                    ApplicationManager.getApplication().runReadAction(() -> {
                        Diagnostic.DiagnosticPosition position = diagnostic.getPosition();
                        LogicalPosition startPosition = new LogicalPosition(position
                                .getStartLine() - 1, position.startColumn());

                        int startOffset = editor.logicalPositionToOffset(startPosition);
                        PsiElement elementAtOffset = data.psiFile.findElementAt(startOffset);
                        if (elementAtOffset instanceof PsiWhiteSpace) {
                            elementAtOffset = PsiTreeUtil.nextVisibleLeaf(elementAtOffset);
                        }
                        TextRange textRange;
                        if (elementAtOffset == null) {
                            int endColumn = position.startColumn() == position.endColumn() ?
                                    position.endColumn() + 1 : position.endColumn();

                            LogicalPosition endPosition = new LogicalPosition(position.getEndLine() - 1, endColumn);
                            int endOffset = editor.logicalPositionToOffset(endPosition);
                            textRange = new TextRange(startOffset, endOffset);
                        } else {
                            int endOffset = elementAtOffset.getTextOffset() + elementAtOffset.getTextLength();
                            textRange = new TextRange(elementAtOffset.getTextOffset(), endOffset);
                        }
                        Issue issue = new Issue(diagnostic, textRange);
                        issues.add(issue);
                    });
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return issues;
    }

    /**
     * Called 3rd to actually annotate the editor window.
     */
    @Override
    public void apply(@NotNull PsiFile file, List<Issue> issues, @NotNull AnnotationHolder holder) {
        for (Issue issue : issues) {
            TextRange range = issue.textRange;
            if (issue.diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                holder.createErrorAnnotation(range, issue.diagnostic.getMessage());
            } else if (issue.diagnostic.getKind() == Diagnostic.Kind.WARNING) {
                holder.createWarningAnnotation(range, issue.diagnostic.getMessage());
            } else if (issue.diagnostic.getKind() == Diagnostic.Kind.NOTE) {
                holder.createInfoAnnotation(range, issue.diagnostic.getMessage());
            }
        }
    }

    public static class Data {

        Editor editor;
        PsiFile psiFile;

        public Data(@NotNull Editor editor, @NotNull PsiFile psiFile) {
            this.editor = editor;
            this.psiFile = psiFile;
        }
    }

    public static class Issue {

        Diagnostic diagnostic;
        TextRange textRange;

        public Issue(@NotNull Diagnostic diagnostic, @NotNull TextRange textRange) {
            this.diagnostic = diagnostic;
            this.textRange = textRange;
        }
    }
}

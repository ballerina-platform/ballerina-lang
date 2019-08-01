/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.plugins.idea.webview.diagram.actions;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.util.Couple;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import io.ballerina.plugins.idea.BallerinaLanguage;
import io.ballerina.plugins.idea.webview.diagram.split.SplitFileEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Ballerina diagram editor toolbar utils.
 */
public class ToolbarActionUtil {
    @Nullable
    public static SplitFileEditor findSplitEditor(AnActionEvent e) {
        final FileEditor editor = e.getData(PlatformDataKeys.FILE_EDITOR);
        return findSplitEditor(editor);
    }

    @Nullable
    public static SplitFileEditor findSplitEditor(@Nullable FileEditor editor) {
        if (editor instanceof SplitFileEditor) {
            return (SplitFileEditor) editor;
        } else {
            return SplitFileEditor.PARENT_SPLIT_KEY.get(editor);
        }
    }

    @Nullable
    public static Editor findDiagramTextEditor(AnActionEvent e) {
        final SplitFileEditor splitEditor = findSplitEditor(e);
        if (splitEditor == null) {
            // This fallback is used primarily for testing

            final PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
            if (psiFile != null && psiFile.getLanguage() == BallerinaLanguage.INSTANCE && ApplicationManager
                    .getApplication().isUnitTestMode()) {
                return e.getData(CommonDataKeys.EDITOR);
            } else {
                return null;
            }
        }

        if (!(splitEditor.getMainEditor() instanceof TextEditor)) {
            return null;
        }
        final TextEditor mainEditor = (TextEditor) splitEditor.getMainEditor();
        if (!mainEditor.getComponent().isVisible()) {
            return null;
        }

        return mainEditor.getEditor();
    }

    @Nullable
    public static Couple<PsiElement> getElementsUnderCaretOrSelection(@NotNull PsiFile file, @NotNull Caret caret) {
        if (caret.getSelectionStart() == caret.getSelectionEnd()) {
            final PsiElement element = file.findElementAt(caret.getSelectionStart());
            if (element == null) {
                return null;
            }
            return Couple.of(element, element);
        } else {
            final PsiElement startElement = file.findElementAt(caret.getSelectionStart());
            final PsiElement endElement = file.findElementAt(caret.getSelectionEnd());
            if (startElement == null || endElement == null) {
                return null;
            }
            return Couple.of(startElement, endElement);
        }
    }

    @Nullable
    public static PsiElement getCommonParentOfType(@NotNull PsiElement element1, @NotNull PsiElement element2,
                                                   @NotNull final IElementType elementType) {
        final PsiElement base = PsiTreeUtil.findCommonParent(element1, element2);
        return PsiTreeUtil.findFirstParent(base, false, element -> {
            final ASTNode node = element.getNode();
            return node != null && node.getElementType() == elementType;
        });
    }
}

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

package org.ballerinalang.plugins.idea.completion;

import com.intellij.codeInsight.AutoPopupController;
import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorModificationUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import org.ballerinalang.plugins.idea.codeinsight.imports.BallerinaImportPackageQuickFix;
import org.ballerinalang.plugins.idea.util.BallerinaUtil;
import org.jetbrains.annotations.NotNull;

public class BallerinaAutoImportInsertHandler/*<T extends GoNamedElement>*/ implements InsertHandler<LookupElement> {

    public static final InsertHandler<LookupElement> INSTANCE = new BallerinaAutoImportInsertHandler(false);
    public final static InsertHandler<LookupElement> INSTANCE_WITH_AUTO_POPUP = new BallerinaAutoImportInsertHandler(true);

    private final boolean myTriggerAutoPopup;

    //  public static final InsertHandler<LookupElement> TYPE_CONVERSION_INSERT_HANDLER = new
    // BallerinaAutoImportInsertHandler<>(
    //    GoCompletionUtil.Lazy.TYPE_CONVERSION_INSERT_HANDLER, GoTypeSpec.class);
    //  public static final InsertHandler<LookupElement> FUNCTION_INSERT_HANDLER = new BallerinaAutoImportInsertHandler<>(
    //    GoCompletionUtil.Lazy.VARIABLE_OR_FUNCTION_INSERT_HANDLER, GoFunctionDeclaration.class);

    //  @Nullable private final InsertHandler<LookupElement> myDelegate;
    //  @Nullable private final Class<T> myClass;

    public BallerinaAutoImportInsertHandler(boolean triggerAutoPopup) {
        myTriggerAutoPopup = triggerAutoPopup;
    }


    //  private BallerinaAutoImportInsertHandler(@Nullable InsertHandler<LookupElement> delegate, @Nullable Class<T> clazz) {
    //    myDelegate = delegate;
    //    myClass = clazz;
    //  }

    @Override
    public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
        PsiElement element = item.getPsiElement();
        //    if (element instanceof GoNamedElement) {
        //      if (myClass != null && myDelegate != null && myClass.isInstance(element)) {
        //        myDelegate.handleInsert(context, item);
        //      }
        if (element != null) {
            autoImport(context, /*(GoNamedElement)*/ element);
        }
        Editor editor = context.getEditor();
        Project project = editor.getProject();
        if (project != null) {
            PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());
            if (!isCompletionCharAtSpace(editor)) {
                EditorModificationUtil.insertStringAtCaret(editor, ":");
                PsiDocumentManager.getInstance(project).commitDocument(editor.getDocument());
            } else {
                editor.getCaretModel().moveToOffset(editor.getCaretModel().getOffset() + 1);
            }
            if (myTriggerAutoPopup) {
                AutoPopupController.getInstance(project).autoPopupMemberLookup(editor, null);
            }
        }
        //    }
    }

    private static void autoImport(@NotNull InsertionContext context, @NotNull /*GoNamedElement*/PsiElement element) {
        PsiFile file = context.getFile();
        //    boolean vendoringEnabled = GoVendoringUtil.isVendoringEnabled(ModuleUtilCore.findModuleForPsiElement
        // (file));
        if (!(element instanceof PsiDirectory)) {
            return;
        }
        String importPath = BallerinaUtil.suggestPackageNameForDirectory(((PsiDirectory) element));
        //                    element.getContainingFile().getImportPath(vendoringEnabled);
        if (StringUtil.isEmpty(importPath)) {
            return;
        }

        PsiDocumentManager.getInstance(context.getProject()).commitDocument(context.getEditor().getDocument());
        PsiReference reference = file.findReferenceAt(context.getStartOffset());
        if (reference != null) {
            PsiElement referenceElement = reference.getElement();
            BallerinaImportPackageQuickFix fix = new BallerinaImportPackageQuickFix(referenceElement, importPath);
            fix.invoke(context.getProject(), file, context.getEditor(), referenceElement, referenceElement);
        }
    }

    private static boolean isCompletionCharAtSpace(Editor editor) {
        final int startOffset = editor.getCaretModel().getOffset();
        final Document document = editor.getDocument();
        return document.getTextLength() > startOffset && document.getCharsSequence().charAt(startOffset) == ':';
    }
}

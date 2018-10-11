package com.github.gtache.lsp.editor.listeners

import com.github.gtache.lsp.editor.EditorEventManager
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate.Result
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

/**
  * This class notifies an EditorEventManager that a character has been typed in the editor
  */
class LSPTypedHandler extends TypedHandlerDelegate {
  override def charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result = {
    EditorEventManager.forEditor(editor).foreach(m => m.characterTyped(c))
    Result.CONTINUE
  }

}

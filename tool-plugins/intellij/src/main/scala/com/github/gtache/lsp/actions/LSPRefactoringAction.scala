package com.github.gtache.lsp.actions

import com.github.gtache.lsp.editor.EditorEventManager
import com.intellij.openapi.actionSystem.{AnActionEvent, CommonDataKeys}
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.ui.{Messages, NonEmptyInputValidator}

/**
  * Action called when the user presses SHIFT+ALT+F6 to rename a symbol
  */
class LSPRefactoringAction extends DumbAwareAction {
  private val LOG: Logger = Logger.getInstance(classOf[LSPRefactoringAction])

  override def actionPerformed(e: AnActionEvent): Unit = {
    val editor = e.getData(CommonDataKeys.EDITOR)
    EditorEventManager.forEditor(editor) match {
      case Some(manager) =>
        val renameTo = Messages.showInputDialog(e.getProject, "Enter new name: ", "Rename", Messages.getQuestionIcon, "", new NonEmptyInputValidator())
        if (renameTo != null && renameTo != "") manager.rename(renameTo)
      case None =>
    }
  }
}

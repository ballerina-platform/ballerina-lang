package com.github.gtache.lsp.contributors.fixes

import com.github.gtache.lsp.contributors.psi.LSPPsiElement
import com.github.gtache.lsp.editor.EditorEventManager
import com.github.gtache.lsp.requests.WorkspaceEditHandler
import com.intellij.codeInspection.{LocalQuickFix, ProblemDescriptor}
import com.intellij.openapi.project.Project
import org.eclipse.lsp4j.CodeAction

/**
  * The Quickfix for LSP
  *
  * @param uri        The file in which the commands will be applied
  * @param codeAction The action to execute
  */
class LSPCodeActionFix(uri: String, codeAction: CodeAction) extends LocalQuickFix {
  override def applyFix(project: Project, descriptor: ProblemDescriptor): Unit = {
    descriptor.getPsiElement match {
      case _: LSPPsiElement =>
        if (codeAction.getEdit != null) WorkspaceEditHandler.applyEdit(codeAction.getEdit, codeAction.getTitle)
        EditorEventManager.forUri(uri).foreach(m => {
          m.executeCommands(Array(codeAction.getCommand))
        })
      case _ =>
    }
  }

  override def getFamilyName: String = "LSP Fixes"

  override def getName: String = {
    codeAction.getTitle
  }

}

package com.github.gtache.lsp.actions

import com.github.gtache.lsp.PluginMain
import com.github.gtache.lsp.requests.ReformatHandler
import com.intellij.codeInsight.actions.ReformatCodeAction
import com.intellij.lang.LanguageFormatting
import com.intellij.openapi.actionSystem.{AnActionEvent, CommonDataKeys}
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiDocumentManager

/**
  * Action overriding the default reformat action
  * Fallback to the default action if the language is already supported or not supported by any language server
  */
class LSPReformatAction extends ReformatCodeAction with DumbAware {

  private val LOG: Logger = Logger.getInstance(classOf[LSPReformatAction])

  override def actionPerformed(e: AnActionEvent): Unit = {
    val project = e.getData(CommonDataKeys.PROJECT)
    val editor = e.getData(CommonDataKeys.EDITOR)
    if (editor != null) {
      val file = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument)
      if (LanguageFormatting.INSTANCE.allForLanguage(file.getLanguage).isEmpty && PluginMain.isExtensionSupported(file.getVirtualFile.getExtension)) {
        ReformatHandler.reformatFile(editor)
      } else {
        super.actionPerformed(e)
      }
    }
  }

  override def update(event: AnActionEvent): Unit = super.update(event)

}

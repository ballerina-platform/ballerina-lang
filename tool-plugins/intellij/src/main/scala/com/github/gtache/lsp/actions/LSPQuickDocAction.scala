package com.github.gtache.lsp.actions

import com.github.gtache.lsp.editor.EditorEventManager
import com.intellij.codeInsight.documentation.actions.ShowQuickDocInfoAction
import com.intellij.lang.LanguageDocumentation
import com.intellij.openapi.actionSystem.{AnActionEvent, CommonDataKeys}
import com.intellij.openapi.application.ApplicationInfo
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.PlainTextLanguage
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiManager

/**
  * Action overriding QuickDoc (CTRL+Q)
  */
class LSPQuickDocAction extends ShowQuickDocInfoAction with DumbAware {
  private val LOG: Logger = Logger.getInstance(classOf[LSPQuickDocAction])

  override def actionPerformed(e: AnActionEvent): Unit = {
    val editor = e.getData(CommonDataKeys.EDITOR)
    val file = FileDocumentManager.getInstance().getFile(editor.getDocument)
    val language = PsiManager.getInstance(editor.getProject).findFile(file).getLanguage
    //Hack for IntelliJ 2018 TODO proper way
    if (LanguageDocumentation.INSTANCE.allForLanguage(language).isEmpty || (ApplicationInfo.getInstance().getMajorVersion.toInt > 2017) && PlainTextLanguage.INSTANCE == language) {
      EditorEventManager.forEditor(editor) match {
        case Some(manager) => manager.quickDoc(editor)
        case None => super.actionPerformed(e)
      }
    } else super.actionPerformed(e)
  }
}

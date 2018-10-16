package com.github.gtache.lsp.contributors.rename

import com.github.gtache.lsp.PluginMain
import com.github.gtache.lsp.contributors.psi.LSPPsiElement
import com.github.gtache.lsp.editor.EditorEventManager
import com.github.gtache.lsp.utils.FileUtils
import com.intellij.codeInsight.template.impl.TemplateManagerImpl
import com.intellij.openapi.actionSystem.{CommonDataKeys, DataContext}
import com.intellij.openapi.command.impl.StartMarkAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.{Messages, NonEmptyInputValidator}
import com.intellij.psi.impl.source.tree.injected.InjectedLanguageUtil
import com.intellij.psi.{PsiElement, PsiFile, PsiNameIdentifierOwner, PsiNamedElement}
import com.intellij.refactoring.rename.inplace.{InplaceRefactoring, MemberInplaceRenameHandler, MemberInplaceRenamer}
import com.intellij.refactoring.rename.{PsiElementRenameHandler, RenameHandler, RenameHandlerRegistry, RenamePsiElementProcessor}

class LSPRenameHandler extends RenameHandler {
  override def invoke(project: Project, elements: Array[PsiElement], dataContext: DataContext): Unit = {
    if (elements.length == 1) new MemberInplaceRenameHandler().doRename(elements(0), dataContext.getData(CommonDataKeys.EDITOR), dataContext)
    else invoke(project, dataContext.getData(CommonDataKeys.EDITOR), dataContext.getData(CommonDataKeys.PSI_FILE), dataContext)
  }

  override def invoke(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext): Unit = {
    EditorEventManager.forEditor(editor) match {
      case Some(m) =>
        if (editor.getContentComponent.hasFocus) {
          val psiElement = m.getElementAtOffset(editor.getCaretModel.getCurrentCaret.getOffset)
          if (psiElement != null) {
            doRename(psiElement, editor, dataContext)
          }
        }
      case None =>
    }
  }

  def doRename(elementToRename: PsiElement, editor: Editor, dataContext: DataContext): InplaceRefactoring = {
    elementToRename match {
      case owner: PsiNameIdentifierOwner =>
        val processor = RenamePsiElementProcessor.forElement(elementToRename)
        if (processor.isInplaceRenameSupported) {
          val startMarkAction = StartMarkAction.canStart(elementToRename.getProject)
          if (startMarkAction == null || (processor.substituteElementToRename(elementToRename, editor) eq elementToRename)) {
            processor.substituteElementToRename(elementToRename, editor, (element: PsiElement) => {
              val renamer = createMemberRenamer(element, owner, editor)
              val startedRename = renamer.performInplaceRename
              if (!startedRename) performDialogRename(elementToRename, editor, dataContext)
            })
            return null
          }
          else {
            val inplaceRefactoring = editor.getUserData(InplaceRefactoring.INPLACE_RENAMER)
            if (inplaceRefactoring != null && (inplaceRefactoring.getClass eq classOf[MemberInplaceRenamer])) {
              val templateState = TemplateManagerImpl.getTemplateState(InjectedLanguageUtil.getTopLevelEditor(editor))
              if (templateState != null) templateState.gotoEnd(true)
            }
          }
        }
      case _ =>
    }
    performDialogRename(elementToRename, editor, dataContext)
    null
  }

  protected def performDialogRename(elementToRename: PsiElement, editor: Editor, dataContext: DataContext): Unit = {
    EditorEventManager.forEditor(editor) match {
      case Some(manager) =>
        val renameTo = Messages.showInputDialog(editor.getProject, "Enter new name: ", "Rename", Messages.getQuestionIcon, "", new NonEmptyInputValidator())
        if (renameTo != null && renameTo != "") manager.rename(renameTo)
      case None =>
    }
  }

  def createMemberRenamer(element: PsiElement, elementToRename: PsiNameIdentifierOwner, editor: Editor): MemberInplaceRenamer = {
    new LSPInplaceRenamer(element.asInstanceOf[PsiNamedElement], elementToRename, editor)()
  }

  override def isRenaming(dataContext: DataContext): Boolean = isAvailableOnDataContext(dataContext)

  protected def checkAvailable(elementToRename: PsiElement, editor: Editor, dataContext: DataContext): Boolean = {
    if (!isAvailableOnDataContext(dataContext)) {
      RenameHandlerRegistry.getInstance.getRenameHandler(dataContext).invoke(elementToRename.getProject, editor, elementToRename.getContainingFile, dataContext)
      return false
    }
    true
  }

  override def isAvailableOnDataContext(dataContext: DataContext): Boolean = {
    val element = PsiElementRenameHandler.getElement(dataContext)
    val editor = CommonDataKeys.EDITOR.getData(dataContext)
    val file = CommonDataKeys.PSI_FILE.getData(dataContext)
    if (editor == null || file == null) return false
    isAvailable(element, editor, file)
  }

  def isAvailable(psiElement: PsiElement, editor: Editor, psiFile: PsiFile): Boolean = {
    psiElement match {
      case _: PsiFile => true
      case _: LSPPsiElement => true
      //IntelliJ 2018 returns psiElement null for unsupported languages
      case _ => psiElement == null && PluginMain.isExtensionSupported(FileUtils.extFromPsiFile(psiFile))
    }
  }

}
